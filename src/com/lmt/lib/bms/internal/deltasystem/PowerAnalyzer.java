package com.lmt.lib.bms.internal.deltasystem;

import java.util.Arrays;
import java.util.List;

import com.lmt.lib.bms.bemusic.BeMusicDevice;
import com.lmt.lib.bms.bemusic.BeMusicLane;
import com.lmt.lib.bms.bemusic.BeMusicPoint;
import com.lmt.lib.bms.bemusic.BeMusicRatingType;

/**
 * 譜面傾向「POWER」の分析処理クラス
 */
public class PowerAnalyzer extends RatingAnalyzer {
	/** ノート評価点の最小値 */
	private static final double SCORE_NOTES_MIN = 0.001;

	/** レーンごとの評価情報 */
	private static class LaneScore {
		/** 楽曲位置評価点のサマリ */
		ScoreSummarizer summarizer;
		/** 楽曲位置の最初と最後の時間差分(演奏時間) */
		double timePtRange;
		/** 最終評価点の補正倍率 */
		double customRatio;
		/** レーンの最終評価点 */
		double powerOrg;
	}

	/**
	 * コンストラクタ
	 */
	public PowerAnalyzer() {
		super(BeMusicRatingType.POWER);
	}

	/** {@inheritDoc} */
	@Override
	protected void compute(DsContext ctx) {
		// 動作のある楽曲位置を特定し、その楽曲位置に対する要素のリストを構築する
		// (BGAやBPM変化、譜面停止のみの楽曲位置等、入力デバイス操作を伴わない楽曲位置を除外したリスト)
		var elems = Ds.listElements(ctx, PowerElement::new, BeMusicPoint::hasMovementNote);
		if (elems.isEmpty()) {
			ctx.stat.setRating(getRatingType(), 0);
			return;
		}

		// レーンごとにレーティング値を算出する
		var config = PowerConfig.getInstance();
		var power = 0;
		if (ctx.dpMode) {
			// ダブルプレーの場合、左右レーン両方を評価する
			var lsp = computeLane(ctx, BeMusicLane.PRIMARY, elems);
			var lss = computeLane(ctx, BeMusicLane.SECONDARY, elems);

			// 最終評価点が片側に偏った譜面での最終評価点の調整を行う
			var influences = Ds.adjustInfluenceForDp(
					lsp.powerOrg,
					lss.powerOrg,
					config.dpInfluenceScoreHigh,
					config.dpInfluenceScoreLow,
					config.dpAdjustInfluenceRate,
					config.dpAdjustInfluenceMaxStrength);

			// 最終評価点を合成し、レーティング値を算出する
			var scOrg = Ds.mergeValue(lsp.powerOrg, lss.powerOrg, influences[0], influences[1]);
			power = adjustPowerByDensity(ctx, (int)config.ipfnDpPower.compute(scOrg));
			debugOut(ctx, power, elems, scOrg, lsp, lss);
		} else {
			// シングルプレーの場合、主レーンのみレーティング値を算出する
			var ls = computeLane(ctx, BeMusicLane.PRIMARY, elems);
			power = adjustPowerByDensity(ctx, (int)config.ipfnPower.compute(ls.powerOrg));
			debugOut(ctx, power, elems, ls.powerOrg, ls);
		}

		// 最終結果を設定する
		ctx.stat.setRating(getRatingType(), power);
	}

	/**
	 * 指定レーンの評価
	 * @param ctx Delta System用コンテキスト
	 * @param lane レーン
	 * @param elems レーティング要素リスト
	 * @return レーン評価情報
	 */
	private LaneScore computeLane(DsContext ctx, BeMusicLane lane, List<PowerElement> elems) {
		// 各入力デバイスで、最後に操作した要素のインデックスのリスト
		var lastOps = new int[BeMusicDevice.COUNT];
		Arrays.fill(lastOps, PowerElement.NO_PREVIOUS);

		// 楽曲位置ごとの評価点を計算する
		var config = PowerConfig.getInstance();
		var devs = DsContext.orderedDevices(lane);
		var devScratch = BeMusicDevice.getScratch(lane);
		var fingeringDefault = ctx.dpMode ? Fingering.DP_DEFAULT : Fingering.SP_DEFAULT;
		var fingeringScratch = ctx.dpMode ? Fingering.DP_SCRATCH : Fingering.SP_SCRATCH;
		var resistHolding = ctx.dpMode ? config.dpResistHolding : config.resistHolding;
		var countDev = devs.size();
		var countElem = elems.size();
		for (var i = 0; i < countElem; i++) {
			var data = new PowerElement.Data(lane);
			var elem = elems.get(i);
			elem.setData(lane, data);
			if (!elem.hasMovementNote(lane)) {
				continue;
			}

			// 何らかの操作が発生する入力デバイスで、前回動作時からの時間を算出する
			// 対象は押下(DOWN)操作の発生するノートとする。解放(UP)操作は「操作の終了」を意味するため
			// 「操作した入力デバイス」としてカウントしない。解放操作を対象にするとロングノート主体の譜面で
			// 評価点が必要以上に上昇してしまう。
			for (var j = 0; j < countDev; j++) {
				var dev = devs.get(j);
				var di = dev.getIndex();
				var type = elem.getNoteType(dev);
				if (type.hasDownAction()) {
					var previous = lastOps[di];
					if (previous == PowerElement.NO_PREVIOUS) {
						// 手前の要素が存在しない場合は追加評価点は最低点になるようにする
						data.setNoteTime(dev, config.timeRangeNotes);
					} else {
						// 同入力デバイスで前回操作した楽曲位置との時間差を記録する
						var timeDelta = Ds.timeDelta(elems.get(previous), elem);
						data.setNoteTime(dev, Math.min(config.timeRangeNotes, timeDelta));
						data.setPrevious(dev, previous);
					}
					lastOps[di] = i;
				}
			}

			// スクラッチの有無により、前提とする運指を決定する
			var curType = elem.getNoteType(devScratch);
			var lastScrOp = lastOps[devScratch.getIndex()];  // 実際のSCRを指定する必要がある
			if (curType.hasMovement() || (curType.isHolding())) {
				// スクラッチ操作あり、または操作継続中の楽曲位置ではスクラッチを含む運指とする
				data.setFingering(fingeringScratch);
			} else if (lastScrOp == PowerElement.NO_PREVIOUS) {
				// スクラッチ操作未検出の場合はスイッチ操作に特化した運指とする
				data.setFingering(fingeringDefault);
			} else {
				// スクラッチ操作なしの場合
				var prevElem = elems.get(lastScrOp);
				var prevType = prevElem.getNoteType(devScratch);
				if (prevType.hasMovement() && (Ds.timeDelta(prevElem, elem) <= config.continueScratchFingering)) {
					// 直近のスクラッチ操作から規定時間以内の楽曲位置ではスクラッチを含む運指とする
					data.setFingering(fingeringScratch);
				} else {
					// 完全にスクラッチ操作のない楽曲位置ではスイッチ操作に特化した運指とする
					data.setFingering(fingeringDefault);
				}
			}

			// 現在楽曲位置のノートごとのノート評価点を集計する
			for (var j = 0; j < countDev; j++) {
				// 当該楽曲位置の運指に未登録の場合は読み飛ばす
				// スイッチ専用運指ではスクラッチに指がアサインされていない
				var dev = devs.get(j);
				var fingering = data.getFingering();
				var finger = fingering.getFinger(dev);
				if (finger == null) {
					continue;
				}

				// 連動指の抵抗による抵抗値を集計する
				var resValue = 1.0;
				var resists = fingering.getResists(finger);
				var resistCount = resists.length;
				for (var iResist = 0; iResist < resistCount; iResist++) {
					var resist = resists[iResist];
					var resDevs = fingering.getDevices(resist.getFinger());
					var resDevCount = resDevs.size();
					for (var resDevsIdx = 0; resDevsIdx < resDevCount; resDevsIdx++) {
						var resDev = resDevs.get(resDevsIdx);
						var resDi = resDev.getIndex();
						var resLastOp = lastOps[resDi];
						if (elem.getNoteType(resDev).isHolding()) {
							// 連動指がホールド中の場合は固定値の抵抗値を用いる
							resValue += (resistHolding * resist.getValue());
						} else if ((resLastOp < i) && (resLastOp >= 0)) {
							// 連動指が当該楽曲位置より手前で操作された場合は当該楽曲位置との時間差分から抵抗値を算出する
							var timeDelta = Ds.timeDelta(elems.get(resLastOp), elem);
							if (timeDelta < config.timeRangeResist) {
								var value = config.ipfnResist.compute(timeDelta);
								resValue += (value * resist.getValue());
							}
						} else {
							// 上記以外の場合は抵抗値の変化なし
							// Do nothing
						}
					}
				}
				data.setResist(dev, resValue);

				// 入力デバイスごとにノート評価点を集計する
				// ノート評価点は「前ノートからの時間」「担当指にかかる抵抗」「担当指の強さ」を入力データとする
				var time = data.getNoteTime(dev);
				if (time > 0) {
					var ratio = dev.isScratch() ? config.adjustScoreAtScratch : 1.0;
					var seed = Math.max(config.ipfnNotes.compute(time) * ratio, SCORE_NOTES_MIN);
					var score = (seed * data.getResist(dev)) / finger.getStrength();
					data.setNoteScore(dev, score);
				}
			}
		}

		// 楽曲位置ごとに「譜面密度評価点」と「連続操作評価点」を計算し、最後に「楽曲位置評価点」を計算する
		for (var i = 0; i < countElem; i++) {
			var elem = elems.get(i);
			if (!elem.hasMovementNote(lane)) {
				continue;
			}

			// 譜面密度評価点の計算を行う
			// ノートの密度・配置に関わらずバタバタと忙しく手指を動かす必要のある譜面を「地力が必要」と評価するため
			var data = elem.getData(lane);
			var densityScore = 0.0;
			for (var j = (i - 1); j >= 0; j--) {
				var elemPrev = elems.get(j);
				if (!elemPrev.hasMovementNote(lane)) {
					continue;
				}
				var timeDelta = Ds.timeDelta(elemPrev, elem);
				if (timeDelta > config.timeRangeDensity) {
					// 規定時間の範囲外となったら当該楽曲位置での加点を終了する
					break;
				} else {
					// 手前の楽曲位置と操作間隔に応じた加点を行う
					var score = config.ipfnDensity.compute(timeDelta);
					densityScore += score;
				}
			}
			data.setDensityScore(densityScore);

			// 同一入力デバイスの連続操作による地力の評価点(連続操作評価点)を集計する
			// 短い時間内に同じ指を連続で操作する動作、要するに「縦連打」が顕著な配置を「地力が必要」と評価するため
			for (var j = 0; j < countDev; j++) {
				var dev = devs.get(j);
				if (elem.getNoteType(dev).isLongNoteTail()) {
					// ロングノート終端の場合「操作の終了」を表すことから、この位置での連続操作の評価は行わない。
					// ロングノート主体の譜面において過剰に評価点が上昇することを抑制する。
					continue;
				}

				var ratio = dev.isScratch() ? config.adjustScoreAtScratch : 1.0;
				var prev = data.getPrevious(dev, elems);
				while (prev != null) {
					var timeDelta = Ds.timeDelta(prev, elem);
					if (timeDelta >= config.timeRangeRapidBeat) {
						// 加点範囲外になったら当該入力デバイスでの加点処理を終了する
						prev = null;
					} else {
						// 現在楽曲位置に近接した楽曲位置ほど多く加点する
						var score = config.ipfnRapidBeat.compute(timeDelta) * ratio;
						data.putRapidBeatScore(dev, score);
						prev = prev.getData(lane).getPrevious(dev, elems);
					}
				}
			}

			// 楽曲位置評価点を計算する
			var scoreL = data.computeHandScore(Hand.LEFT);
			var scoreR = data.computeHandScore(Hand.RIGHT);
			var scoreHigh = Math.max(scoreL, scoreR);
			var scoreLow = Math.min(scoreL, scoreR);
			var scoreDensity = data.getDensityScore();
			data.pointScore = scoreHigh + (scoreLow * config.influenceLowHandScore) + scoreDensity;
		}

		// 最終評価点を計算する
		var ls = new LaneScore();
		ls.summarizer = new ScoreSummarizer(config.satulateTotalScore, elems, e -> true, e -> e.getData(lane).pointScore);
		ls.timePtRange = Ds.computeTimeOfPointRange(elems);
		ls.customRatio = elems.size() / (double)config.leastPoints;
		ls.powerOrg = Ds.computeRatingValue(ls.summarizer.summary(), ls.timePtRange, ls.customRatio);

		return ls;
	}

	/**
	 * POWER値に対する平均密度から、最終的に出力するPOWER値の調整を行う。
	 * <p>平均的な密度が低いのに特定の箇所が極端に高密度な譜面では、高POWER値の割に密度が薄いように感じることがある。
	 * また、譜面の一部だけ簡単な配置の高密度にしPOWER値を吊り上げる不正な譜面に対処するためこのような調整を行う。</p>
	 * @param ctx Delta System用コンテキスト
	 * @param power 調整対象のPOWER値
	 * @return 調整後のPOWER値
	 */
	private int adjustPowerByDensity(DsContext ctx, int power) {
		var config = PowerConfig.getInstance();
		var baseDensity = config.ipfnAdjust.compute(power);
		var acceptableDensity = baseDensity - (baseDensity * config.densityAcceptableRate);
		var avgDensity = ctx.chart.getNoteCount() / ctx.chart.getPlayTime(); // 統計情報の平均密度は使用しない
		if (avgDensity < acceptableDensity) {
			// 平均密度が許容平均密度を下回っていた場合はPOWER値の調整を行う
			var downRate = ((acceptableDensity - avgDensity) / acceptableDensity) * config.powerDownwardRate;
			var downValue = power * downRate;
			power -= (int)downValue;
		}
		return power;
	}

	/** {@inheritDoc} */
	@Override
	protected void dumpSummarySp(DsContext ctx, int rating, Object...values) {
		dumpSummaryCommon(ctx, (double)values[0], rating, values[1]);
	}

	/** {@inheritDoc} */
	@Override
	protected void dumpSummaryDp(DsContext ctx, int rating, Object...values) {
		dumpSummaryCommon(ctx, (double)values[0], rating, values[1], values[2]);
	}

	/**
	 * サマリされたデバッグ情報の出力共通処理
	 * @param ctx Delta System用コンテキスト
	 * @param org 算出したレーティング値のオリジナル値
	 * @param rating orgの値をレーティング種別ごとの値の範囲にスケーリングした最終評価点の値
	 * @param values その他、デバッグ出力のために必要なオブジェクトのリスト
	 */
	private void dumpSummaryCommon(DsContext ctx, double org, int rating, Object...values) {
		var sb = new StringBuilder();
		sb.append(ctx.header.getComment());
		sb.append("\t").append(String.format("%s %s", ctx.header.getTitle(), ctx.header.getSubTitle()).strip());
		for (var value : values) {
			var ls = (LaneScore)value;
			sb.append("\t").append(ls.summarizer);
			sb.append(String.format("\t%.4f", ls.powerOrg));
		}
		if (ctx.dpMode) {
			sb.append(String.format("\t%.4f", org));
		}
		sb.append(String.format("\t%.2f", getRatingType().toValue(rating)));
		Ds.debug(sb);
	}

	/** {@inheritDoc} */
	@Override
	protected void dumpDetailSp(DsContext ctx, int rating, List<? extends RatingElement> elems, Object...values) {
		super.dumpDetailSp(ctx, rating, elems, values);
	}

	/** {@inheritDoc} */
	@Override
	protected void dumpDetailDp(DsContext ctx, int rating, List<? extends RatingElement> elems, Object...values) {
		super.dumpDetailDp(ctx, rating, elems, values);
	}
}
