package com.lmt.lib.bms.internal.deltasystem;

import java.util.Arrays;

import com.lmt.lib.bms.bemusic.BeMusicDevice;
import com.lmt.lib.bms.bemusic.BeMusicPoint;
import com.lmt.lib.bms.bemusic.BeMusicRatingType;

/**
 * 譜面傾向「POWER」の分析処理クラス
 */
public class PowerAnalyzer extends RatingAnalyzer {
	/** ノート評価点の最小値 */
	private static final double SCORE_NOTES_MIN = 0.001;

	/**
	 * コンストラクタ
	 */
	public PowerAnalyzer() {
		super(BeMusicRatingType.POWER);
	}

	/** {@inheritDoc} */
	@Override
	protected void compute(DsContext cxt) {
		// 動作のある楽曲位置を特定し、その楽曲位置に対する要素のリストを構築する
		// (BGAやBPM変化、譜面停止のみの楽曲位置等、入力デバイス操作を伴わない楽曲位置を除外したリスト)
		var elems = RatingElement.listElements(cxt, PowerElement::new, BeMusicPoint::hasMovementNote);
		var countElem = elems.size();
		if (countElem == 0) {
			cxt.stat.setRating(getRatingType(), 0);
			return;
		}

		// 各入力デバイスで、最後に操作した要素のインデックスのリスト
		var lastOps = new int[BeMusicDevice.COUNT];
		Arrays.fill(lastOps, PowerElement.NO_PREVIOUS);

		// 楽曲位置ごとの評価点を計算する
		var config = PowerConfig.getInstance();
		var devs = BeMusicDevice.orderedBySpLeftList();
		var countDev = devs.size();
		for (var i = 0; i < countElem; i++) {
			var elem = elems.get(i);
			var curTime = elem.getTime();

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
						elem.setNoteTime(dev, config.timeRangeNotes);
					} else {
						// 同入力デバイスで前回操作した楽曲位置との時間差を記録する
						var prevTime = elems.get(previous).getPoint().getTime();
						elem.setNoteTime(dev, Math.min(config.timeRangeNotes, (curTime - prevTime)));
						elem.setPrevious(dev, previous);
					}
					lastOps[di] = i;
				}
			}

			// スクラッチの有無により、前提とする運指を決定する
			var curType = elem.getNoteType(BeMusicDevice.SCRATCH1);
			var lastScrOp = lastOps[BeMusicDevice.SCRATCH1.getIndex()];  // 実際のSCRを指定する必要がある
			if (curType.hasMovement() || (curType.isHolding())) {
				// スクラッチ操作あり、または操作継続中の楽曲位置ではスクラッチを含む運指とする
				elem.setFingering(Fingering.SP_SCRATCH);
			} else if (lastScrOp == PowerElement.NO_PREVIOUS) {
				// スクラッチ操作未検出の場合はスイッチ操作に特化した運指とする
				elem.setFingering(Fingering.SP_DEFAULT);
			} else {
				// スクラッチ操作なしの場合
				var prevElem = elems.get(lastScrOp);
				var prevType = prevElem.getNoteType(BeMusicDevice.SCRATCH1);
				if (prevType.hasMovement() && ((curTime - prevElem.getTime()) <= config.continueScratchFingering)) {
					// 直近のスクラッチ操作から規定時間以内の楽曲位置ではスクラッチを含む運指とする
					elem.setFingering(Fingering.SP_SCRATCH);
				} else {
					// 完全にスクラッチ操作のない楽曲位置ではスイッチ操作に特化した運指とする
					elem.setFingering(Fingering.SP_DEFAULT);
				}
			}

			// 現在楽曲位置のノートごとのノート評価点を集計する
			for (var j = 0; j < countDev; j++) {
				// 当該楽曲位置の運指に未登録の場合は読み飛ばす
				// スイッチ専用運指ではスクラッチに指がアサインされていない
				var dev = devs.get(j);
				var fingering = elem.getFingering();
				var finger = fingering.getFinger(dev);
				if (finger == null) {
					continue;
				}

				// 連動指の抵抗による抵抗値を集計する
				var resValue = 1.0;
				for (var resist : fingering.getResists(finger)) {
					var resDev = fingering.getDevice(resist.getFinger());
					var resDi = resDev.getIndex();
					var resLastOp = lastOps[resDi];
					if (elem.getNoteType(resDev).isHolding()) {
						// 連動指がホールド中の場合は固定値の抵抗値を用いる
						resValue += (config.resistHolding * resist.getValue());
					} else if ((resLastOp < i) && (resLastOp >= 0)) {
						// 連動指が当該楽曲位置より手前で操作された場合は当該楽曲位置との時間差分から抵抗値を算出する
						var timeDiff = curTime - elems.get(resLastOp).getPoint().getTime();
						if (timeDiff < config.timeRangeResist) {
							var value = config.ipfnResist.compute(timeDiff);
							resValue += (value * resist.getValue());
						}
					} else {
						// 上記以外の場合は抵抗値の変化なし
						// Do nothing
					}
				}
				elem.setResist(dev, resValue);

				// 入力デバイスごとにノート評価点を集計する
				// ノート評価点は「前ノートからの時間」「担当指にかかる抵抗」「担当指の強さ」を入力データとする
				var time = elem.getNoteTime(dev);
				if (time > 0) {
					var ratio = dev.isScratch() ? config.adjustScoreAtScratch : 1.0;
					var seed = Math.max(config.ipfnNotes.compute(time) * ratio, SCORE_NOTES_MIN);
					var score = (seed * elem.getResist(dev)) / finger.getStrength();
					elem.setNoteScore(dev, score);
				}
			}
		}

		// 楽曲位置ごとに「譜面密度評価点」と「連続操作評価点」を計算し、最後に「楽曲位置評価点」を計算する
		var scoreMin = Double.MAX_VALUE;
		var scoreMax = Double.MIN_VALUE;
		for (var i = 0; i < countElem; i++) {
			var elem = elems.get(i);
			var curTime = elem.getPoint().getTime();

			// 譜面密度評価点の計算を行う
			// ノートの密度・配置に関わらずバタバタと忙しく手指を動かす必要のある譜面を「地力が必要」と評価するため
			var densityScore = 0.0;
			for (var j = (i - 1); j >= 0; j--) {
				var prevTime = elems.get(j).getPoint().getTime();
				var timeDiff = curTime - prevTime;
				if (timeDiff > config.timeRangeDensity) {
					// 規定時間の範囲外となったら当該楽曲位置での加点を終了する
					break;
				} else {
					// 手前の楽曲位置と操作間隔に応じた加点を行う
					var score = config.ipfnDensity.compute(timeDiff);
					densityScore += score;
				}
			}
			elem.setDensityScore(densityScore);

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
				var prev = elem.getPrevious(dev, elems);
				while (prev != null) {
					var timeDiff = curTime - prev.getPoint().getTime();
					if (timeDiff >= config.timeRangeRapidBeat) {
						// 加点範囲外になったら当該入力デバイスでの加点処理を終了する
						prev = null;
					} else {
						// 現在楽曲位置に近接した楽曲位置ほど多く加点する
						var score = config.ipfnRapidBeat.compute(timeDiff) * ratio;
						elem.putRapidBeatScore(dev, score);
						prev = prev.getPrevious(dev, elems);
					}
				}
			}

			// 楽曲位置評価点を計算する
			var scoreL = elem.computeHandScore(Hand.LEFT);
			var scoreR = elem.computeHandScore(Hand.RIGHT);
			var scoreHigh = Math.max(scoreL, scoreR);
			var scoreLow = Math.min(scoreL, scoreR);
			var scoreDensity = elem.getDensityScore();
			var pointScore = scoreHigh + (scoreLow * PowerConfig.getInstance().influenceLowHandScore) + scoreDensity;
			elem.setPointScore(pointScore);
			scoreMin = Math.min(scoreMin, pointScore);
			scoreMax = Math.max(scoreMax, pointScore);
		}

		// 最終評価点を計算する
		var summarizer = new ScoreSummarizer(config.satulateTotalScore, elems, PowerElement::getPointScore);
		var timePtRange = RatingElement.computeTimeOfPointRange(elems);
		var customRatio = elems.size() / (double)config.leastPoints;
		var powerOrg = computeRatingValue(summarizer.summary(), timePtRange, customRatio);
		var power = (int)config.ipfnPower.compute(powerOrg);

		// POWER値に対する平均密度から、最終的に出力するPOWER値の調整を行う
		// 平均的な密度が低いのに特定の箇所が極端に高密度な譜面では、高POWER値の割に密度が薄いように感じることがある。
		// また、譜面の一部だけ簡単な配置の高密度にしPOWER値を吊り上げる不正な譜面に対処するためこのような調整を行う。
		var baseDensity = config.ipfnAdjust.compute(power);
		var acceptableDensity = baseDensity - (baseDensity * config.densityAcceptableRate);
		var avgDensity = cxt.chart.getNoteCount() / cxt.chart.getPlayTime(); // 統計情報の平均密度は使用しない
		if (avgDensity < acceptableDensity) {
			// 平均密度が許容平均密度を下回っていた場合はPOWER値の調整を行う
			var downRate = ((acceptableDensity - avgDensity) / acceptableDensity) * config.powerDownwardRate;
			var downValue = power * downRate;
			power -= (int)downValue;
		}

		// デバッグ出力する
		debugOut(cxt, powerOrg, power, elems, summarizer);

		// 最終結果を設定する
		cxt.stat.setRating(getRatingType(), power);
	}

	@Override
	protected void dumpSummary(DsContext cxt, double org, int rating, Object...values) {
		var sb = new StringBuilder();
		sb.append(cxt.header.getComment());
		sb.append("\t").append(String.format("%s %s", cxt.header.getTitle(), cxt.header.getSubTitle()).strip());
		sb.append("\t").append(values[0]);
		sb.append(String.format("\t%.4f", org));
		sb.append(String.format("\t%.2f", getRatingType().toValue(rating)));
		Ds.debug(sb);
	}
}
