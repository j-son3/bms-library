package com.lmt.lib.bms.internal.deltasystem;

import java.util.Arrays;
import java.util.stream.IntStream;

import com.lmt.lib.bms.bemusic.BeMusicDevice;
import com.lmt.lib.bms.bemusic.BeMusicNoteType;
import com.lmt.lib.bms.bemusic.BeMusicPoint;
import com.lmt.lib.bms.bemusic.BeMusicRatingType;

/**
 * 譜面傾向「COMPLEX」の分析処理クラス
 */
public class ComplexAnalyzer extends RatingAnalyzer {
	/**
	 * コンストラクタ
	 */
	public ComplexAnalyzer() {
		super(BeMusicRatingType.COMPLEX);
	}

	/** {@inheritDoc} */
	@Override
	protected void compute(DsContext cxt) {
		// 操作変化のある楽曲位置のみを抽出し、その楽曲位置のみで要素リストを構築する
		// (操作変化のない要素のみの楽曲位置は当譜面傾向ではノイズとなるため除去する)
		var elems = RatingElement.listElements(cxt, ComplexElement::new, BeMusicPoint::hasMovementNote);
		var countElem = elems.size();
		if (countElem == 0) {
			cxt.stat.setRating(getRatingType(), 0);
			return;
		}

		// 楽曲位置ごとの評価点を計算する
		// 「楽曲位置複雑度評価点」は、同一楽曲位置上でのノートの配置状況から複雑さを数値化したもの。
		// 「後方複雑度評価点」は、後方の楽曲位置複雑度評価点から当該楽曲位置の総合評価点に加算する点数。
		var config = ComplexConfig.getInstance();
		var ntypeFounds = new int[BeMusicDevice.COUNT];
		var devs = BeMusicDevice.orderedBySpLeftScratchList();
		var countDev = devs.size();
		var scoreMin = Double.MAX_VALUE;
		var scoreMax = Double.MIN_VALUE;
		for (var i = 0; i < countElem; i++) {
			var elem = elems.get(i);
			var curPoint = elem.getPoint();
			var curTime = elem.getTime();

			// 楽曲位置のノート分析を行い、各分析結果を楽曲位置要素として記録する
			// 入力デバイスレイアウトの左から右へ走査し、評価点の元となる要素を算出する
			// 一部、計算済みの要素は楽曲位置情報からデータを取り出すのみとなる
			var prevVe = true;
			var prevVeSwNo = -1;
			var prevVeNtype = (BeMusicNoteType)null;
			var visualEffectCount = curPoint.getVisualEffectCount();
			var landmineCount = curPoint.getLandmineCount();
			var holdingCount = 0;
			var lnTailCount = 0;
			var noteTypeCount = 0;
			var spaceCount = 0;
			var changeColorCount = 0;
			var changeNoteTypeCount = 0;
			var changeColorScratch = false;
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
					holdingCount += ntype.isHolding() ? 1 : 0;
					lnTailCount += ntype.isLongNoteTail() ? 1 : 0;
					var changeColor = computeChangeColor(prevVeSwNo, curVeSwNo);
					changeColorCount += (changeColor & 0x01);
					changeNoteTypeCount += computeChangeNoteType(prevVeNtype, curVeNtype);
					changeColorScratch |= ((changeColor & 0x02) != 0);
					prevVeSwNo = curVeSwNo;
					prevVeNtype = curVeNtype;
				} else {
					// 視覚効果なしの場合、空域数を更新する
					spaceCount += prevVe ? 1 : 0;
				}
				prevVe = veffect;
			}
			noteTypeCount = (int)IntStream.of(ntypeFounds).filter(n -> n > 0).count();
			elem.setVisualEffectCount(visualEffectCount);
			elem.setNoteTypeCount(noteTypeCount);
			elem.setHoldingCount(holdingCount);
			elem.setLandmineCount(landmineCount);
			elem.setSpaceCount(spaceCount);
			elem.setChangeColorCount(changeColorCount);
			elem.setChangeNoteTypeCount(changeNoteTypeCount);

			// 楽曲位置要素から「楽曲位置複雑度評価点」を計算する
			var deductionHolding = (holdingCount / 8.0) * config.deductionHold;
			var deductionLnTail = (lnTailCount / 8.0) * config.deductionLnTail;
			var deductionLandmine = (landmineCount / 8.0) * config.deductionMine;
			var deduction = 1.0 - (deductionHolding + deductionLnTail + deductionLandmine);
			var basic = (2.0 + ((visualEffectCount / 8.0) * 4.0));
			var colAdjustScr = changeColorScratch ? config.adjustScratchColor : 0.0;
			var ratioSpCol1 = (changeColorCount + colAdjustScr + 1.0) / (10.0 - (spaceCount / 2.0));
			var ratioSpCol2 = 0.8 + (spaceCount / 4.0) * 0.2;
			var ratioSpCol = 0.5 + (ratioSpCol1 * ratioSpCol2);
			var ratioNtype1 = ((noteTypeCount - 1.0) / 4.0) * config.typeCountRate;
			var ratioNtype2 = (changeNoteTypeCount / 7.0) * config.typeChangeRate;
			var ratioNtype = 1.0 + ratioNtype1 + ratioNtype2;
			var ratioPtReduce = config.ipfnPtReduce.compute(elem.getTimeDelta());
			var scorePointOrg = basic * ratioSpCol * ratioNtype * ratioPtReduce * deduction;
			var scorePoint = config.ipfnBasic.compute(scorePointOrg);
			elem.setPointScore(scorePoint);

			// 「後方複雑度評価点」を計算する
			// 規定範囲の楽曲位置の配置が複雑であるほど、配置が詰まっているほど高評価となるようにする
			for (var j = i - 1; (j >= 0) && ((curTime - elems.get(j).getTime()) <= config.timeRangeBwRef); j--) {
				var prev = elems.get(j);

				// 現在楽曲位置との差分を出す
				var diffCount = 0;
				for (var k = 0; k < countDev; k++) {
					var dev = devs.get(k);
					var typeCur = elem.getNoteType(dev);
					var typePrev = prev.getNoteType(dev);
					diffCount += ((typeCur == typePrev) ? 0 : 1);
				}

				// 後方複雑度評価点を算出し、設定する
				var ratioByTime = config.ipfnBwRef.compute(curTime - prev.getTime());
				var ratioByDiff = Math.max(0.05, ((double)diffCount / countDev));
				var scoreBw = prev.getPointScore() * ratioByDiff * ratioByTime;
				elem.putBackwardScore(scoreBw);
			}

			// 集計した各種情報を基にして「総合複雑度評価点」を計算する
			var scoreTotal = elem.getTotalScore();
			scoreMin = Math.min(scoreMin, scoreTotal);
			scoreMax = Math.max(scoreMax, scoreTotal);
		}

		// 最終評価点を計算する
		var summarizer = new ScoreSummarizer(config.satulateTotalScore, elems, ComplexElement::getTotalScore);
		var timePtRange = RatingElement.computeTimeOfPointRange(elems);
		var customRatio = elems.size() / (double)config.leastPoints;
		var complexOrg = computeRatingValue(summarizer.summary(), timePtRange, customRatio);
		var complex = (int)config.ipfnComplex.compute(complexOrg);

		// デバッグ出力する
		debugOut(cxt, complexOrg, complex, elems, summarizer);

		// 最終結果を設定する
		cxt.stat.setRating(getRatingType(), complex);
	}

	/** {@inheritDoc} */
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
