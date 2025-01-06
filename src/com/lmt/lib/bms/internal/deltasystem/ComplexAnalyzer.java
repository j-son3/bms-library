package com.lmt.lib.bms.internal.deltasystem;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import com.lmt.lib.bms.bemusic.BeMusicDevice;
import com.lmt.lib.bms.bemusic.BeMusicLane;
import com.lmt.lib.bms.bemusic.BeMusicNoteType;
import com.lmt.lib.bms.bemusic.BeMusicPoint;
import com.lmt.lib.bms.bemusic.BeMusicRatingType;

/**
 * 譜面傾向「COMPLEX」の分析処理クラス
 */
public class ComplexAnalyzer extends RatingAnalyzer {
	/** レーンごとの評価情報 */
	private static class LaneScore {
		/** 楽曲位置総合評価点のサマリ */
		ScoreSummarizer summarizer;
		/** 操作を伴う楽曲位置の数 */
		int numMovement;
		/** 楽曲位置の最初と最後の時間差分(演奏時間) */
		double timePtRange;
		/** 最終評価点の補正倍率 */
		double customRatio;
		/** レーンの最終評価点 */
		double complexOrg;
	}

	/**
	 * コンストラクタ
	 */
	public ComplexAnalyzer() {
		super(BeMusicRatingType.COMPLEX);
	}

	/** {@inheritDoc} */
	@Override
	protected void compute(DsContext ctx) {
		// 操作変化のある楽曲位置のみを抽出し、その楽曲位置のみで要素リストを構築する
		// (操作変化のない要素のみの楽曲位置は当譜面傾向ではノイズとなるため除去する)
		var elems = Ds.listElements(ctx, ComplexElement::new, BeMusicPoint::hasMovementNote);
		if (elems.isEmpty()) {
			ctx.stat.setRating(getRatingType(), 0);
			return;
		}

		// レーンごとにレーティング値を算出する
		var config = ComplexConfig.getInstance();
		var complex = 0;
		if (ctx.dpMode) {
			// ダブルプレーの場合、左右レーン両方を評価する
			var lsp = computeLane(ctx, BeMusicLane.PRIMARY, elems);
			var lss = computeLane(ctx, BeMusicLane.SECONDARY, elems);

			// 楽曲位置数が偏った譜面での最終評価点の調整を行う
			var influences = Ds.adjustInfluenceForDp(
					lsp.numMovement,
					lss.numMovement,
					config.dpInfluenceScoreHigh,
					config.dpInfluenceScoreLow,
					config.dpAdjustInfluenceRate,
					config.dpAdjustInfluenceMaxStrength);

			// 最終評価点を合成し、レーティング値を算出する
			var scOrg = Ds.mergeValue(lsp.complexOrg, lss.complexOrg, influences[0], influences[1]);
			complex = Math.max((int)config.ipfnDpComplex.compute(scOrg), 1);
			debugOut(ctx, complex, elems, scOrg, lsp, lss);
		} else {
			// シングルプレーの場合、主レーンのみレーティング値を算出する
			var ls = computeLane(ctx, BeMusicLane.PRIMARY, elems);
			complex = Math.max((int)config.ipfnComplex.compute(ls.complexOrg), 1);
			debugOut(ctx, complex, elems, ls.complexOrg, ls);
		}

		// 最終結果を設定する
		ctx.stat.setRating(getRatingType(), complex);
	}

	/**
	 * 指定レーンの評価
	 * @param ctx Delta System用コンテキスト
	 * @param lane レーン
	 * @param elems レーティング要素リスト
	 * @return レーン評価情報
	 */
	private LaneScore computeLane(DsContext ctx, BeMusicLane lane, List<ComplexElement> elems) {
		// 楽曲位置ごとの評価点を計算する
		// 「楽曲位置複雑度評価点」は、同一楽曲位置上でのノートの配置状況から複雑さを数値化したもの。
		// 「後方複雑度評価点」は、後方の楽曲位置複雑度評価点から当該楽曲位置の総合評価点に加算する点数。
		var config = ComplexConfig.getInstance();
		var deductionHoldRatio = config.deductionHold(ctx);
		var deductionLnTailRatio = config.deductionLnTail(ctx);
		var deductionMineRatio = config.deductionMine(ctx);
		var basicScore = ComplexBasicScore.getInstance(ctx);
		var ntypeFounds = new int[BeMusicDevice.COUNT];
		var devs = DsContext.orderedDevices(lane);
		var countMovement = 0;
		var countDev = devs.size();
		var countElem = elems.size();
		for (var i = 0; i < countElem; i++) {
			var elem = elems.get(i);
			var curTime = elem.getTime();

			// 楽曲位置のノート分析を行い、各分析結果を楽曲位置要素として記録する
			// 入力デバイスレイアウトの左から右へ走査し、評価点の元となる要素を算出する
			// 一部、計算済みの要素は楽曲位置情報からデータを取り出すのみとなる
			var basic = basicScore.get(lane, elem);
			var prevVe = true;
			var prevVeSwNo = -1;
			var prevVeNtype = (BeMusicNoteType)null;
			var hasMovement = false;
			var visualEffectCount = 0;
			var mineCount = 0;
			var holdingCount = 0;
			var lnTailCount = 0;
			var noteTypeCount = 0;
			var spaceCount = 0;
			var changeColorCount = 0;
			var changeNoteTypeCount = 0;
			Arrays.fill(ntypeFounds, 0);
			for (var j = 0; j < countDev; j++) {
				var dev = devs.get(j);
				var ntype = elem.getNoteType(dev);
				var veffect = ntype.hasVisualEffect();
				var curVeSwNo = -1;
				var curVeNtype = (BeMusicNoteType)null;
				if (veffect) {
					// 視覚効果ありの場合、ノート種別数・デバイス色変化数・ノート種別変化数を更新する
					curVeSwNo = dev.getSwitchNumber();
					curVeNtype = ntype;
					ntypeFounds[ntype.getId()] = 1;
					hasMovement = hasMovement || ntype.hasMovement();
					visualEffectCount++;
					mineCount += (ntype == BeMusicNoteType.MINE) ? 1 : 0;
					holdingCount += ntype.isHolding() ? 1 : 0;
					lnTailCount += ntype.isLongNoteTail() ? 1 : 0;
					var changeColor = computeChangeColor(prevVeSwNo, curVeSwNo);
					changeColorCount += (changeColor & 0x01);
					changeNoteTypeCount += computeChangeNoteType(prevVeNtype, curVeNtype);
					prevVeSwNo = curVeSwNo;
					prevVeNtype = curVeNtype;
				} else {
					// 視覚効果なしの場合、空域数を更新する
					spaceCount += prevVe ? 1 : 0;
				}
				prevVe = veffect;
			}
			noteTypeCount = (int)IntStream.of(ntypeFounds).filter(n -> n > 0).count();

			var data = new ComplexElement.Data();
			elem.setData(lane, data);  // 後続処理を行わない場合があるので予め要素データの参照を設定しておく
			if (!hasMovement) {
				// 操作を伴わない楽曲位置の場合、当該楽曲位置の楽曲位置複雑度評価点は0とし以降の処理は省略する
				// ※シングルプレーでこの分岐に入ることはない。ダブルプレーで反対レーンのみ視覚効果がある場合を想定。
				continue;
			}

			countMovement++;
			data.hasMovement = hasMovement;
			data.numVisual = (byte)visualEffectCount;
			data.numType = (byte)noteTypeCount;
			data.numHold = (byte)holdingCount;
			data.numMine = (byte)mineCount;
			data.numSpace = (byte)spaceCount;
			data.numChgColor = (byte)changeColorCount;
			data.numChgType = (byte)changeNoteTypeCount;

			// 楽曲位置要素から「楽曲位置複雑度評価点」を計算する
			var deductionHolding = (holdingCount / 8.0) * deductionHoldRatio;
			var deductionLnTail = (lnTailCount / 8.0) * deductionLnTailRatio;
			var deductionMine = (mineCount / 8.0) * deductionMineRatio;
			var deduction = Math.max(1.0 - (deductionHolding + deductionLnTail + deductionMine), 0.0);
			var ratioNtype1 = ((noteTypeCount - 1.0) / 4.0) * config.typeCountRate;
			var ratioNtype2 = (changeNoteTypeCount / 7.0) * config.typeChangeRate;
			var ratioNtype = 1.0 + ratioNtype1 + ratioNtype2;
			var ratioPtReduce = config.ipfnPtReduce.compute(elem.getTimeDelta());
			var scorePointOrg = basic * ratioNtype * ratioPtReduce * deduction;
			var scorePoint = config.ipfnBasic.compute(scorePointOrg);
			data.pointScore = (float)scorePoint;

			// 「後方複雑度評価点」を計算する
			// 規定範囲の楽曲位置の配置が複雑であるほど、配置が詰まっているほど高評価となるようにする
			var sameBackward = false;
			var firstBwIndex = i - 1;
			for (var j = firstBwIndex; (j >= 0) && ((curTime - elems.get(j).getTime()) <= config.timeRangeBwRef); j--) {
				var prev = elems.get(j);
				var prevData = prev.getData(lane);
				if (!prevData.hasMovement) {
					continue;  // 操作を伴わない楽曲位置は無視する(DPのみを想定)
				}

				// 現在楽曲位置との差分を出す
				var diffCount = 0;
				for (var k = 0; k < countDev; k++) {
					var dev = devs.get(k);
					var typeCur = elem.getNoteType(dev);
					var typePrev = prev.getNoteType(dev);
					diffCount += ((typeCur == typePrev) ? 0 : 1);
				}

				// 1つ前の楽曲位置が同じ配置かどうかを記録しておく
				if ((j == firstBwIndex) && (diffCount == 0)) {
					sameBackward = true;
				}

				// 後方複雑度評価点を算出し、設定する
				if (!prevData.sameBackwardPattern) {
					var ratioByTime = config.ipfnBwRef.compute(curTime - prev.getTime());
					var ratioByDiff = Math.max(config.minBwRatioPatternDelta, ((double)diffCount / countDev));
					var scoreBw = prevData.pointScore * ratioByDiff * ratioByTime;
					data.backwardScore += (float)scoreBw;
					data.backwardScoreCount++;
				}
			}
			data.sameBackwardPattern = sameBackward;
		}

		// 最終評価点を計算する
		var ls = new LaneScore();
		ls.summarizer = new ScoreSummarizer(config.satulateTotalScore, elems, e -> e.getData(lane).hasMovement,
				e -> e.getTotalScore(lane));
		ls.numMovement = countMovement;
		ls.timePtRange = Ds.computeTimeOfPointRange(elems);
		ls.customRatio = countMovement / (double)(config.leastPoints / (ctx.dpMode ? 2 : 1));
		ls.complexOrg = Ds.computeRatingValue(ls.summarizer.summary(), ls.timePtRange, ls.customRatio);

		return ls;
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
		for (var i = 0; i < values.length; i++) {
			var ls = (LaneScore)values[i];
			sb.append("\t").append(ls.summarizer);
			sb.append(String.format("\t%.4f", ls.complexOrg));
		}
		if (ctx.dpMode) {
			sb.append(String.format("\t%.4f", org));
		}
		sb.append(String.format("\t%.2f", getRatingType().toValue(rating)));
		Ds.debug(sb);
	}

	/** {@inheritDoc} */
	@Override
	protected void dumpDetailSp(DsContext ctx, int rating, List<? extends RatingElement> elems, Object... values) {
		super.dumpDetailSp(ctx, rating, elems, values);
		dumpDetailCommon(values[1]);
	}

	/** {@inheritDoc} */
	@Override
	protected void dumpDetailDp(DsContext ctx, int rating, List<? extends RatingElement> elems, Object... values) {
		super.dumpDetailDp(ctx, rating, elems, values);
		dumpDetailCommon(values[1], values[2]);
	}

	/**
	 * 詳細デバッグ情報出力の共通処理
	 * @param values レーンごとの評価情報
	 */
	private void dumpDetailCommon(Object... values) {
		for (var i = 0; i < values.length; i++) {
			var ls = (LaneScore)values[i];
			Ds.debug("--- %s ---", (i == 0) ? "PRIMARY" : "SECONDARY");
			Ds.debug("SUMMARY=(%s, Result=%.4f)", ls.summarizer, ls.summarizer.summary());
			Ds.debug("MOVEMENT=%d", ls.numMovement);
			Ds.debug("TIME=%.2f", ls.timePtRange);
			Ds.debug("CUSTOM=%.4f", ls.customRatio);
			Ds.debug("SCORE=%.4f", ls.complexOrg);
		}
	}

	/**
	 * デバイス色切替回数計算
	 * <p>入力デバイスレイアウトを左から右へ走査し、視覚効果ありの入力デバイスで色切替があった回数の計算</p>
	 * @param prevVeSwNo ひとつ前の視覚効果ありスイッチ番号
	 * @param curVeSwNo 現在の視覚効果ありスイッチ番号
	 * @return デバイス色が切り替わる場合、下位1ビット目が1になり、切り替わりにスクラッチが絡む場合、下位2ビット目が1
	 */
	private static int computeChangeColor(int prevVeSwNo, int curVeSwNo) {
		if (prevVeSwNo < 0) {
			// 色変化の比較対象がない場合、色変化なしと判定する
			return 0x00;
		} else if (BeMusicDevice.isScratch(prevVeSwNo) || BeMusicDevice.isScratch(curVeSwNo)) {
			// 比較対象のどちらかがスクラッチの場合、通常とは異なる変化数とする
			// (スクラッチからの色変化は横認識難易度を大きく上昇させるため、係数を大きく取っておく)
			return 0x03;
		} else {
			// 視覚効果のある隣接デバイスと色が異なる(白⇔黒の変化あり)場合は色変化ありとする
			return ((prevVeSwNo & 0x01) == (curVeSwNo & 0x01)) ? 0x00 : 0x01;
		}
	}

	/**
	 * ノート種別切替回数計算
	 * <p>入力デバイスレイアウトを左から右へ走査し、視覚効果ありの入力デバイスでノート種別切替があった回数の計算</p>
	 * @param prevVeNtype ひとつ前の視覚効果ありノート種別
	 * @param curVeNtype 現在の視覚効果ありノート種別
	 * @return ノート種別切替回数
	 */
	private static int computeChangeNoteType(BeMusicNoteType prevVeNtype, BeMusicNoteType curVeNtype) {
		if (prevVeNtype == null) {
			// ノート種別変化の比較対象がない場合、ノート種別変化なしと判定する
			return 0;
		} else {
			// 比較対象同士のノート種別が異なる場合、ノート種別変化ありと判定する
			return (prevVeNtype == curVeNtype) ? 0 : 1;
		}
	}
}
