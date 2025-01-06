package com.lmt.lib.bms.internal.deltasystem;

import java.util.Arrays;
import java.util.List;

import com.lmt.lib.bms.bemusic.BeMusicDevice;
import com.lmt.lib.bms.bemusic.BeMusicPoint;
import com.lmt.lib.bms.bemusic.BeMusicRatingType;

/**
 * 譜面傾向「HOLDING」の分析処理クラス
 */
public class HoldingAnalyzer extends RatingAnalyzer {
	/** ロングノートなしを示す値 */
	private static final int NO_LN = -1;

	/**
	 * コンストラクタ
	 */
	public HoldingAnalyzer() {
		super(BeMusicRatingType.HOLDING);
	}

	/** {@inheritDoc} */
	@Override
	protected void compute(DsContext ctx) {
		// 動作のある楽曲位置を特定し、その楽曲位置に対する要素のリストを構築する
		// (BGAやBPM変化、譜面停止のみの楽曲位置等、入力デバイス操作を伴わない楽曲位置を除外したリスト)
		var elems = Ds.listElements(ctx, HoldingElement::new, BeMusicPoint::hasMovementNote);
		if (elems.isEmpty()) {
			ctx.stat.setRating(getRatingType(), 0);
			return;
		}

		// 長押しノートが全く存在しない譜面では評価を実行しない
		if (!ctx.chart.hasLongNote()) {
			var tlScore = new Holding.Score();
			tlScore.numNotesTotal = ctx.chart.getNoteCount();
			tlScore.playTime = Ds.computeTimeOfPointRange(elems);
			debugOut(ctx, 0, elems, tlScore);
			ctx.stat.setRating(getRatingType(), 0);
			return;
		}

		// 長押しの存在する範囲をグループ化する
		// このループでは範囲情報だけ設定し、実際の評価は後のステップで行う
		var hc = new Holding.Context(ctx);
		var rangeTop = (HoldingRange)null;
		var rangePrev = (HoldingRange)null;
		var countDevs = hc.devs.size();
		var countElem = elems.size();
		for (var i = 0; i < countElem;) {
			// この楽曲位置に長押しオブジェが存在しなければ何もしない
			if (!elems.get(i).getPoint().hasLongNoteType()) {
				i++;
				continue;
			}

			// 長押し範囲を解析する
			var j = i;
			for (; j < countElem; j++) {
				var p = elems.get(j).getPoint();
				if (!p.hasHolding() && !p.hasLongNoteHead()) { break; }
			}

			// モードがLNで長押し範囲が既定よりも短い場合は長押し範囲として扱わない
			// モードLNで長押し範囲が短いと短押しと同等の操作特性になるため、HOLDINGでは評価対象外とする
			var first = i;
			var last = Math.min(j, countElem - 1);
			if ((Ds.timeDelta(elems.get(first), elems.get(last)) < hc.config.minRangeLength) && hc.isLn) {
				i = last + 1;
				continue;
			}

			// 楽曲位置情報に範囲データをセットする
			var range = new HoldingRange(elems, i, Math.min(j, countElem - 1));
			for (j = range.first; j <= range.last; j++) {
				elems.get(j).setRange(range);
			}

			// 次の長押し範囲を解析する準備
			if (rangeTop == null) {
				rangeTop = range;
			}
			if (rangePrev != null) {
				rangePrev.next = range;
			}
			rangePrev = range;
			i = range.last + 1;
		}

		// グループ化された範囲で個別のノートに対してノート評価点を付ける
		var lastOps = new int[BeMusicDevice.COUNT];
		var lnHeads = new int[BeMusicDevice.COUNT];
		var selfResist = new double[BeMusicDevice.COUNT];
		for (var range = rangeTop; range != null; range = range.next) {
			Arrays.fill(lastOps, 0);
			Arrays.fill(lnHeads, NO_LN);

			// 長押し範囲のノートに抵抗値で影響する最小の楽曲位置を検索する
			var s = range.first - 1;
			for (; (s >= 0) && (Ds.timeDelta(elems, s, range.first) <= hc.config.maxResistRange); s--) {}

			// 長押し範囲の手前を走査し、操作状態の初期状態を設定する
			for (var pos = Math.max(0, s); pos < range.first; pos++) {
				var elem = elems.get(pos);
				for (var iDev = 0; iDev < countDevs; iDev++) {
					var dev = hc.devs.get(iDev);
					var ntype = elem.getNoteType(dev);
					if (ntype.hasDownAction()) {
						lastOps[dev.getIndex()] = pos;
					}
				}
			}

			for (var pos = range.first; pos <= range.last; pos++) {
				// 操作状態、長押し状態を更新する
				var elem = elems.get(pos);
				for (var iDev = 0; iDev < countDevs; iDev++) {
					var dev = hc.devs.get(iDev);
					var devIndex = dev.getIndex();
					var ntype = elem.getNoteType(dev);

					// 自デバイスの抵抗値を考慮する
					if (ntype.hasMovement()) {
						var resistRate = hc.config.resistRate(ntype);
						var timeDelta = Ds.timeDelta(elems, lastOps[devIndex], pos);
						selfResist[devIndex] = hc.config.ipfnResist.compute(timeDelta) * resistRate;
					} else {
						selfResist[devIndex] = 0.0;
					}

					// 押下操作を伴うノートに最終操作楽曲位置を設定する
					if (ntype.hasDownAction()) {
						lastOps[devIndex] = pos;
					}

					// 長押し押下・解放の状態を設定する
					if (ntype.isLongNoteHead()) {
						lnHeads[dev.getIndex()] = pos;
					}
				}

				// ノート数としてカウントされるノートに対して評価点を付ける
				for (var iDev = 0; iDev < countDevs; iDev++) {
					var dev = hc.devs.get(iDev);
					var ntype = elem.getNoteType(dev);
					if (ntype.hasMovement()) {
						// 入力操作に伴う周囲指の抵抗を算出する
						var resistSum = selfResist[dev.getIndex()] * hc.config.resistRate(ntype);
						var finger = hc.fingering.getFinger(dev);
						for (var r : hc.fingering.getResists(finger)) {
							var resistDevs = hc.fingering.getDevices(r.getFinger());
							var countResistDevs = resistDevs.size();
							for (var iResistDev = 0; iResistDev < countResistDevs; iResistDev++) {
								var resist = r.getValue();
								var resistDev = resistDevs.get(iResistDev);
								var headPos = lnHeads[resistDev.getIndex()];
								if (headPos != NO_LN) {
									// 抵抗を受ける対象の入力デバイスが長押し継続中であれば基本抵抗値を補正する
									// 長押し継続は指に対して強烈な抵抗を与えるため、通常よりも強い抵抗を受ける。
									if (hc.isHcn) {
										// HCNの場合は長押しの長さに限らず長押し時の倍率をそのまま適用する
										resist *= hc.config.resistRateHolding;
									} else {
										// LN/CNの場合はLNの長さに応じた抵抗値の減衰を行う
										var timeDelta = Ds.timeDelta(elems, headPos, pos);
										var decay = hc.config.ipfnDecay.compute(timeDelta);
										resist *= (hc.config.resistRateHolding * decay);
									}
								} else {
									// 抵抗を受ける対象の入力デバイスが長押し継続中でなければ評価対象と抵抗を受ける
									// 入力デバイスの時間差分による抵抗値の補正を行う。
									// 抵抗を受ける入力デバイスの最終操作間隔が長いほど抵抗が弱まるためこの処理を行う。
									var timeDelta = Ds.timeDelta(elems, lastOps[resistDev.getIndex()], pos);
									var adjustValue = hc.config.ipfnResist.compute(timeDelta);
									resist *= (hc.config.resistRateBeat * adjustValue);
								}
								resistSum += resist;
							}
						}

						// ノート評価点を計算する
						var base = hc.config.noteScoreBase(ntype);
						var rate = hc.config.noteScoreRate(ntype);
						var score = (base + resistSum) * rate;
						range.putNoteScore(pos, dev, ntype.isCountNotes(), score);
					}
				}

				// 長押し終了を長押し状態に反映する
				// この処理はノート評価点の計算後に行う必要がある。長押し終了と同じ楽曲位置のノート評価でも
				// LN/CNのLN長さによる減衰を適用する必要があるため。
				for (var iDev = 0; iDev < countDevs; iDev++) {
					var dev = hc.devs.get(iDev);
					if (elem.getNoteType(dev).isLongNoteTail()) {
						lnHeads[dev.getIndex()] = NO_LN;
					}
				}
			}
		}

		// 長押し範囲に関するサマリを行う
		var notesInLn = 0;
		var lnTime = 0.0;
		var noteScoreSummarizer = new ScoreSummarizer(hc.config.satulateNoteScore);
		for (var range = rangeTop; range != null; range = range.next) {
			// 長押し範囲内のノート数、長押し総時間
			notesInLn += range.notes;
			lnTime += range.time;

			// ノート評価点のサマリ
			for (var pos = range.first; pos <= range.last; pos++) {
				for (var iDev = 0; iDev < countDevs; iDev++) {
					var score = range.getNoteScore(pos, hc.devs.get(iDev));
					if (score != null) {
						noteScoreSummarizer.put(elems.get(pos).getTime(), score);
					}
				}
			}
		}

		// タイムラインの評価結果を設定する
		var tlScore = new Holding.Score();
		tlScore.numNotesTotal = ctx.chart.getNoteCount();
		tlScore.numNotesInLn = notesInLn;
		tlScore.numLn = ctx.chart.getLongNoteCount();
		tlScore.playTime = Ds.computeTimeOfPointRange(elems);
		tlScore.lnTime = lnTime;
		tlScore.noteScoreSummary = noteScoreSummarizer;

		// 採取したノート評価点を基に譜面全体に対する評価点を加味し最終評価点・HOLDING値を算出する
		tlScore.scoreUnitNotes = noteScoreSummarizer.summary();
		tlScore.scoreInLnNotes = hc.config.ipfnInLnNotes.compute(tlScore.notesInLnRate());
		tlScore.scoreLnRate = hc.config.ipfnLnCount.compute(tlScore.lnRate());
		tlScore.scoreLnTime = hc.config.ipfnLnTime.compute(tlScore.lnTimeRate());
		var scoreOrg = tlScore.scoreUnitNotes * tlScore.scoreInLnNotes * tlScore.scoreLnRate * tlScore.scoreLnTime;
		tlScore.holdingOrg = Ds.computeRatingValue(scoreOrg, tlScore.playTime, 1.0);
		var holding = (int)hc.config.ipfnHolding.compute(tlScore.holdingOrg);

		debugOut(ctx, holding, elems, tlScore);

		ctx.stat.setRating(getRatingType(), holding);
	}

	/** {@inheritDoc} */
	@Override
	protected void dumpSummarySp(DsContext ctx, int rating, Object...values) {
		dumpSummaryCommon(ctx, rating, (Holding.Score)values[0]);
	}

	/** {@inheritDoc} */
	@Override
	protected void dumpSummaryDp(DsContext ctx, int rating, Object...values) {
		dumpSummaryCommon(ctx, rating, (Holding.Score)values[0]);
	}

	/**
	 * サマリされたデバッグ情報出力の共通処理
	 * @param ctx Delta System用コンテキスト
	 * @param rating レーティング値
	 * @param tlScore 評価情報
	 */
	private void dumpSummaryCommon(DsContext ctx, int rating, Holding.Score tlScore) {
		var sb = new StringBuilder();
		sb.append(ctx.header.getComment());
		sb.append("\t").append(String.format("%s %s", ctx.header.getTitle(), ctx.header.getSubTitle()).strip());
		sb.append("\t").append(tlScore.numNotesTotal);
		sb.append("\t").append(tlScore.numNotesInLn);
		sb.append("\t").append(String.format("%.4f", tlScore.notesInLnRate()));
		sb.append("\t").append(tlScore.numLn);
		sb.append("\t").append(String.format("%.4f", tlScore.lnRate()));
		sb.append("\t").append(String.format("%.2f", tlScore.playTime));
		sb.append("\t").append(String.format("%.2f", tlScore.lnTime));
		sb.append("\t").append(String.format("%.4f", tlScore.lnTimeRate()));
		sb.append("\t").append(String.format("%.3f", tlScore.scoreUnitNotes));
		sb.append("\t").append(String.format("%.3f", tlScore.scoreInLnNotes));
		sb.append("\t").append(String.format("%.3f", tlScore.scoreLnRate));
		sb.append("\t").append(String.format("%.3f", tlScore.scoreLnTime));
		sb.append("\t").append(String.format("%.3f", tlScore.holdingOrg));
		sb.append("\t").append(String.format("%.2f", getRatingType().toValue(rating)));
		Ds.debug(sb);
	}

	/** {@inheritDoc} */
	@Override
	protected void dumpDetailSp(DsContext ctx, int rating, List<? extends RatingElement> elems, Object...values) {
		super.dumpDetailSp(ctx, rating, elems, values);
		dumpDetailCommon((Holding.Score)values[0]);
	}

	/** {@inheritDoc} */
	@Override
	protected void dumpDetailDp(DsContext ctx, int rating, List<? extends RatingElement> elems, Object...values) {
		super.dumpDetailDp(ctx, rating, elems, values);
		dumpDetailCommon((Holding.Score)values[0]);
	}

	/**
	 * 詳細デバッグ情報出力の共通処理
	 * @param tlScore 評価情報
	 */
	private void dumpDetailCommon(Holding.Score tlScore) {
		Ds.debug("UNIT-SC: %s", tlScore.noteScoreSummary);
		Ds.debug("NOTES  : Total=%d, InLn=%d(%.2f%%), Ln=%d(%.2f%%)",
				tlScore.numNotesTotal, tlScore.numNotesInLn, tlScore.notesInLnRatePer(), tlScore.numLn, tlScore.lnRatePer());
		Ds.debug("TIME   : Total=%.2f, LnTime=%.2f(%.2f%%)", tlScore.playTime, tlScore.lnTime, tlScore.lnTimeRatePer());
		Ds.debug("SCORE  : Org=%.4f, UnitNotes=%.4f, InLnNotes=%.4f, LnRate=%.4f, LnTime=%.4f",
				tlScore.holdingOrg, tlScore.scoreUnitNotes, tlScore.scoreInLnNotes, tlScore.scoreLnRate, tlScore.scoreLnTime);
	}
}
