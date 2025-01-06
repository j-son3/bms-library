package com.lmt.lib.bms.internal.deltasystem;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.lmt.lib.bms.bemusic.BeMusicDevice;
import com.lmt.lib.bms.bemusic.BeMusicLane;
import com.lmt.lib.bms.bemusic.BeMusicPoint;
import com.lmt.lib.bms.bemusic.BeMusicRatingType;
import com.lmt.lib.bms.internal.MutableInt;

/**
 * 譜面傾向「RHYTHM」の分析処理クラス
 */
public class RhythmAnalyzer extends RatingAnalyzer {
	/**
	 * コンストラクタ
	 */
	public RhythmAnalyzer() {
		super(BeMusicRatingType.RHYTHM);
	}

	/** {@inheritDoc} */
	@Override
	protected void compute(DsContext ctx) {
		// プレーモードに応じた左側・右側のフィルタリングメソッドを設定する
		Predicate<RhythmElement> filterL, filterR;
		if (!ctx.dpMode) {
			// シングルプレー
			filterL = RhythmAnalyzer::filterSpLeft;
			filterR = RhythmAnalyzer::filterSpRight;
		} else {
			// ダブルプレー
			filterL = RhythmAnalyzer::filterDpLeft;
			filterR = RhythmAnalyzer::filterDpRight;
		}

		// 譜面全体・左側・右側ごとの要素リストを構築する。
		// 対象となるのは「操作を伴うノートが存在する楽曲位置」のみ(地雷、長押し継続しかない楽曲位置は対象外)。
		// 要素オブジェクトの主体は「全体の要素リスト」にあるものをベースとする。
		// 要するに左側・右側の要素リストは全体の要素リストの参照であるため、左右の要素リストに対する変更は
		// 全て全体の要素リストに反映されることになる。
		var elemsAll = Ds.listElements(ctx, RhythmElement::new, BeMusicPoint::hasMovementNote);
		var elemsL = elemsAll.stream().filter(filterL).collect(Collectors.toList());
		var elemsR = elemsAll.stream().filter(filterR).collect(Collectors.toList());
		if (elemsAll.isEmpty()) {
			// 操作可能ノートが1個もない空譜面は0点とする
			Ds.debug("No rhythm because empty score.");
			ctx.stat.setRating(getRatingType(), 0);
			return;
		} else if (elemsAll.size() < 2) {
			// 楽曲位置が2点未満だとリズムもへったくれもないでしょw
			Ds.debug("No rhythm in this song.");
			ctx.stat.setRating(getRatingType(), 1);
			return;
		} else {
			// Do nothing
		}

		// 譜面全体・左側・右側ごとにリズム分析を行い、それぞれで評価点を算出する
		// 最終評価点は各評価点を既定の重み付けで配分される
		var config = RhythmConfig.getInstance();
		var totalTime = elemsAll.get(elemsAll.size() - 1).getTime();
		var scoreAll = computeTimeline(new Rhythm.Context(totalTime, Rhythm.Side.ALL), elemsAll);
		var scoreL = computeTimeline(new Rhythm.Context(totalTime, Rhythm.Side.LEFT), elemsL);
		var scoreR = computeTimeline(new Rhythm.Context(totalTime, Rhythm.Side.RIGHT), elemsR);
		var orgAll = scoreAll.score * config.influenceAllSide(ctx);
		var orgL = scoreL.score * config.influenceLeftSide(ctx);
		var orgR = scoreR.score * config.influenceRightSide(ctx);
		var timePtRange = Ds.computeTimeOfPointRange(elemsAll);
		var rhythmOrg = Ds.computeRatingValue(orgAll + orgL + orgR, timePtRange, 1.0);
		var rhythm = Math.max((int)config.ipfnRhythm.compute(rhythmOrg), 1);

		// デバッグ出力する
		debugOut(ctx, rhythm, elemsAll, rhythmOrg, scoreAll, scoreL, scoreR, totalTime);

		// 最終結果を設定する
		ctx.stat.setRating(getRatingType(), rhythm);
	}

	/**
	 * 指定サイドでのタイムライン分析メイン処理
	 * @param rc RHYTHM用コンテキスト
	 * @param elems 要素リスト
	 * @return 分析結果データ
	 */
	private Rhythm.Score computeTimeline(Rhythm.Context rc, List<RhythmElement> elems) {
		var config = rc.config;
		var countElem = elems.size();
		var lastIndexElem = countElem - 1;

		// 一定間隔で操作が必要な範囲のグループ化を行う
		var prevPr = (PulseRange)null;
		var nextIndex = new MutableInt();
		var nextStepCount = new MutableInt();
		var rangeNumber = 0;
		var movementTime = 0.0;
		for (var i = 0; i < countElem; i++) {
			var elem = elems.get(i);
			var curPr = new PulseRange(rangeNumber);
			rangeNumber++;

			curPr.previousRange = prevPr;
			curPr.firstElement = elem;
			curPr.lastElement = elem;
			curPr.pulseTimeSec = 0.0;
			curPr.pulseCount = 1;
			if (prevPr == null) {
				// 先頭要素の場合は前範囲との時間差分は当該楽曲位置の時間とする
				curPr.intervalSec = elem.getTime();
			} else {
				// 前範囲との時間差分、前範囲の次範囲を現在の範囲とする
				curPr.intervalSec = elem.getTime() - prevPr.lastElement.getTime();
				prevPr.nextRange = curPr;
			}

			// 次の楽曲位置までの時間が長すぎる場合はリズム範囲としてグループ化は行わない
			// 時間間隔が長すぎると、リズムと言うより単発操作となるため。
			var j = i + 1;
			var lastIndex = i;
			var pulseTimeSec = (j < lastIndexElem) ? (elems.get(j).getTime() - elem.getTime()) : 0.0;
			if (pulseTimeSec > config.maxPulseTime) {
				j = countElem;
				pulseTimeSec = 0.0;
			}

			// 1刻みあたりの時間を計算し、同一リズムの最終楽曲位置を検索する
			// 検索にあたっては、微ズレ配置の統合を考慮したうえで検索を実施する
			while (j <= lastIndexElem) {
				// 微ズレ配置の統合を考慮した次楽曲位置の判定を行う
				mergePointIfPossible(elems, lastIndexElem, j, config.acceptableTimeDelta, nextIndex, nextStepCount);

				// 次の楽曲位置との時間差分が規定範囲内であれば同一リズムであると判定する
				// 跳ねリズム楽曲において、極微ズレの譜面は通常リズムとほとんど変わりがないため、
				// 時間差分に許容範囲を設け、その範囲内であれば同一リズムであると見なす。
				// (プレイヤーにとって極微ズレはズレと認識できないことが大半であると推測でき、その考慮を加える)
				var timeDelta = elems.get(nextIndex.get()).getTime() - elems.get(lastIndex).getTime();
				if (Math.abs(timeDelta - pulseTimeSec) > config.acceptableTimeDelta) {
					// 次の楽曲位置との時間差分が以前の時間差分と離れている場合はグループ化終了
					break;
				}

				// 次の楽曲位置との時間差分が以前の時間差分と類似している場合はグループ化対象とする
				lastIndex = nextIndex.get();
				j += nextStepCount.get();
				curPr.pulseCount++;
			}
			curPr.lastElement = elems.get(lastIndex);
			curPr.pulseTimeSec = pulseTimeSec;
			curPr.rangeTime = curPr.lastElement.getTime() - curPr.firstElement.getTime();
			curPr.rangeRate = Math.max((curPr.rangeTime / rc.totalTime), config.minRangeRate);
			movementTime += curPr.rangeTime;

			// グループ化した範囲全体へ範囲オブジェクトを登録し、平均密度を算出する
			var timeDelta = curPr.lastElement.getTime() - curPr.firstElement.getTime();
			var sumNotes = 0;
			for (var k = i; k <= lastIndex; k++) {
				var elemTmp = elems.get(k);
				elemTmp.setPulseRange(rc.side, curPr);
				sumNotes += elemTmp.getPoint().getNoteCount();
			}
			var decayTime = config.densityDecayTime;
			var density = Math.min(sumNotes / ((timeDelta > 0.0) ? timeDelta : 1.0), config.maxDensity);
			curPr.density = (curPr.rangeTime >= decayTime) ? density : (density * (0.5 + ((curPr.rangeTime / decayTime) * 0.5)));

			// このリズム範囲単体の評価点を計算する
			var scoreRangeTime = config.ipfnRangeTime.compute(curPr.rangeTime) * config.influenceRangeTime;
			var scoreInterval = config.ipfnInterval.compute(curPr.intervalSec) * config.influenceInterval;
			var scoreDensity = config.ipfnDensity.compute(curPr.density) * config.influenceDensity;
			curPr.score = scoreRangeTime + scoreInterval + scoreDensity;

			// 次のリズム範囲グループ化を行う準備
			prevPr = curPr;
			i = lastIndex;
		}

		// 同一リズムのリピート回数を検出する
		var firstElem = elems.stream().filter(e -> e.getPulseRange(rc.side) != null).findFirst().orElse(null);
		var firstPr = (firstElem == null) ? null : firstElem.getPulseRange(rc.side);
		for (var pr = firstPr; pr != null;) {
			// リズム範囲のパターン数が多いリピートから先に検出しようとする
			// 仮にパターン数が多いことでリピート数が少なかったとしても、パターン数が多いほうを優先する
			var repeat = new PulseRepeat(pr);
			for (var patternCount = config.maxPatternRepeatCount;; patternCount--) {
				// 現在のリズム範囲から指定パターン数でのリピートを検出する
				var lastPr = pr.detectPatternRepeat(patternCount);
				if (pr != lastPr) {
					// 指定パターン数で2回以上のリピートを検出した
					repeat.repeatCount = (lastPr.number - pr.number + 1) / patternCount;
					repeat.patternCount = patternCount;
					repeat.lastRange = lastPr;
					break;
				} else if (patternCount <= 1) {
					// パターン数1でリピート未検出の場合は現在のリズム範囲でのリピートなしとする
					repeat.repeatCount = 1;
					repeat.patternCount = 1;
					repeat.lastRange = pr;
					break;
				}
			}

			// 検出したリピートの全範囲に対してリピート範囲を設定する
			var tmpPr = pr;
			do {
				tmpPr.repeat = repeat;
				if (tmpPr == repeat.lastRange) {
					break;
				} else {
					tmpPr = tmpPr.nextRange;
				}
			} while (tmpPr != null);

			// 次のリピートを検出する準備
			pr = repeat.lastRange.nextRange;
		}

		// 同一リズムリピートによる個別リズム範囲評価点の修正を行いつつ、リズム範囲評価点を計算する
		var adjustRate = config.adjustPatternRepeatRate;
		var maxPatternCount = config.maxPatternRepeatCount;
		var firstPr2 = elems.isEmpty() ? null : elems.get(0).getPulseRange(rc.side);
		for (var pr = firstPr2; pr != null; pr = pr.nextRange) {
			var repeat = pr.repeat;
			var adjust = 1.0 - (repeat.hasRepeat() ? adjustRate * (maxPatternCount - repeat.patternCount + 1) : 0.0);
			pr.score *= adjust;
		}

		// リズム範囲評価点を計算する
		var summarizer = new ScoreSummarizer(3.0);
		for (var pr = firstPr2; pr != null; pr = pr.nextRange) {
			summarizer.put(pr.firstElement.getTime(), pr.score);
		}

		// リズム変化回数をカウントする
		// 単純なリズム範囲数ではなく、長いリズム範囲は範囲を分割してカウントする
		var changeCount = 0;
		for (var pr = firstPr2; pr != null; pr = pr.nextRange) {
			var count = (int)(pr.rangeTime / config.maxRangeTime) + 1;
			changeCount += count;
		}

		// 各種評価点を計算する
		var tl = new Rhythm.Score();
		tl.side = rc.side;
		tl.changeCount = changeCount;
		tl.changePerSec = changeCount / rc.totalTime;
		tl.movementTime = movementTime;
		tl.movementRate = movementTime / rc.totalTime;
		tl.scoreRange = summarizer.summary();
		tl.ratioChange = config.ipfnChangeRate.compute(tl.changePerSec);
		tl.score = tl.scoreRange * tl.ratioChange;
		return tl;
	}

	/** {@inheritDoc} */
	@Override
	protected void dumpSummarySp(DsContext ctx, int rating, Object...values) {
		dumpSummaryCommon(ctx, (double)values[0], rating, values[1], values[2], values[3], values[4]);
	}

	/** {@inheritDoc} */
	@Override
	protected void dumpSummaryDp(DsContext ctx, int rating, Object...values) {
		dumpSummaryCommon(ctx, (double)values[0], rating, values[1], values[2], values[3], values[4]);
	}

	/**
	 * サマリされたデバッグ情報の出力
	 * @param ctx Delta System用コンテキスト
	 * @param org 算出したレーティング値のオリジナル値
	 * @param rating orgの値をレーティング種別ごとの値の範囲にスケーリングした最終評価点の値
	 * @param values その他、デバッグ出力のために必要なオブジェクトのリスト
	 */
	private void dumpSummaryCommon(DsContext ctx, double org, int rating, Object...values) {
		var sb = new StringBuilder();
		var sa = (Rhythm.Score)values[0];
		var sl = (Rhythm.Score)values[1];
		var sr = (Rhythm.Score)values[2];
		sb.append(ctx.header.getComment());
		sb.append("\t").append(String.format("%s %s", ctx.header.getTitle(), ctx.header.getSubTitle()).strip());
		sb.append(String.format("\t%d\t%.2f", sa.changeCount, sa.changePerSec));
		sb.append(String.format("\t%.1f\t%.1f\t%.3f", values[3], sa.movementTime, sa.movementRate));
		sb.append(String.format("\t%.4f\t%.4f\t%.4f", sa.scoreRange, sa.ratioChange, sa.score));
		sb.append(String.format("\t%.4f\t%.4f\t%.4f", sl.scoreRange, sl.ratioChange, sl.score));
		sb.append(String.format("\t%.4f\t%.4f\t%.4f", sr.scoreRange, sr.ratioChange, sr.score));
		sb.append(String.format("\t%.4f", org));
		sb.append(String.format("\t%.2f", getRatingType().toValue(rating)));
		Ds.debug(sb);
	}

	/** {@inheritDoc} */
	@Override
	protected void dumpDetailSp(DsContext ctx, int rating, List<? extends RatingElement> elems, Object...values) {
		super.dumpDetailSp(ctx, rating, elems, values);
		dumpDetailCommon(ctx, rating, elems, values[1], values[2], values[3]);
	}

	/** {@inheritDoc} */
	@Override
	protected void dumpDetailDp(DsContext ctx, int rating, List<? extends RatingElement> elems, Object...values) {
		super.dumpDetailDp(ctx, rating, elems, values);
		dumpDetailCommon(ctx, rating, elems, values[1], values[2], values[3]);
	}

	/**
	 * 詳細デバッグ情報の出力
	 * @param ctx Delta System用コンテキスト
	 * @param rating orgの値をレーティング種別ごとの値の範囲にスケーリングした最終評価点の値
	 * @param elems レーティング要素リスト
	 * @param values その他、デバッグ出力のために必要なオブジェクトのリスト
	 */
	private void dumpDetailCommon(DsContext ctx, int rating, List<? extends RatingElement> elems, Object...values) {
		Ds.debug(values[0]);
		Ds.debug(values[1]);
		Ds.debug(values[2]);
	}

	/**
	 * 極微ズレ楽曲位置のマージを実施(可能な場合に限り)
	 * @param elems 要素リスト
	 * @param last 要素リストの末尾データインデックス
	 * @param pos マージ開始位置
	 * @param acceptableDelta マージが許容される時間範囲
	 * @param outNextIndex マージした結果、中心となる要素リストインデックス
	 * @param outNextStepCount マージした結果、次の要素のインデックス
	 */
	private static void mergePointIfPossible(List<RhythmElement> elems, int last, int pos, double acceptableDelta,
			MutableInt outNextIndex, MutableInt outNextStepCount) {
		if ((pos < last) && canMergePoint2(elems, pos, acceptableDelta)) {
			if (((pos + 1) < last) && canMergePoint3(elems, pos, acceptableDelta)) {
				// 次楽曲位置を中心とした3点間マージが可能
				outNextIndex.set(pos + 1);
				outNextStepCount.set(3);
			} else {
				// 次楽曲位置との2点間マージが可能
				outNextIndex.set(pos);
				outNextStepCount.set(2);
			}
		} else {
			// マージ不可
			outNextIndex.set(pos);
			outNextStepCount.set(1);
		}
	}

	/**
	 * 2点間マージが可能かどうかチェックする
	 * @param elems 要素リスト
	 * @param pos マージ開始位置
	 * @param acceptableDelta マージが許容される時間範囲
	 * @return posを開始位置とした2点間マージが可能であればtrue
	 */
	private static boolean canMergePoint2(List<RhythmElement> elems, int pos, double acceptableDelta) {
		// 次楽曲位置との時間差分が許容範囲外の場合はマージ不可
		var e1 = elems.get(pos);
		var e2 = elems.get(pos + 1);
		if (e2.getTimeDelta() > acceptableDelta) {
			// 大半のノート配置(99%以上)では1フレームにも満たないような極微ズレ譜面は存在しない。
			// そのため、最初にチェックされる2点間マージの時間差分チェック(このチェックのこと)で弾かれることになり
			// これ以降のノート同士の競合チェック、および3点間マージ可否チェックが必要になるのは極めて稀なケースとなる。
			return false;
		}

		// 視覚効果を伴うノートが競合するかどうかをチェックする
		var countDev = BeMusicDevice.COUNT;
		for (var i = 0; i < countDev; i++) {
			var dev = BeMusicDevice.fromIndex(i);
			if (e1.getNoteType(dev).hasVisualEffect() && e2.getNoteType(dev).hasVisualEffect()) {
				return false;  // 視覚効果を伴うノートが競合する場合はマージ不可
			}
		}

		return true;
	}

	/**
	 * 3点間マージが可能かどうかチェックする
	 * @param elems 要素リスト
	 * @param pos マージ開始位置
	 * @param acceptableDelta マージが許容される時間範囲
	 * @return posを開始位置とした3点間マージが可能であればtrue
	 */
	private static boolean canMergePoint3(List<RhythmElement> elems, int pos, double acceptableDelta) {
		// 3点間の時間差分が許容範囲外の場合はマージ不可
		var e1 = elems.get(pos);
		var e2 = elems.get(pos + 1);
		var e3 = elems.get(pos + 2);
		if ((e2.getTimeDelta() > acceptableDelta) || (e3.getTimeDelta() > acceptableDelta)) {
			return false;
		}

		// 視覚効果を伴うノートが競合するかどうかをチェックする
		var countDev = BeMusicDevice.COUNT;
		for (var i = 0; i < countDev; i++) {
			var dev = BeMusicDevice.fromIndex(i);
			var veNum = (e1.getNoteType(dev).hasVisualEffect() ? 1 : 0) +
					(e2.getNoteType(dev).hasVisualEffect() ? 1 : 0) +
					(e3.getNoteType(dev).hasVisualEffect() ? 1 : 0);
			if (veNum >= 2) {
				return false;  // 視覚効果を伴うノートが競合する場合はマージ不可
			}
		}

		return true;
	}

	/**
	 * タイムライン左サイドのリズム分析対象楽曲位置のフィルタリング判定(SP用)
	 * @param elem 要素
	 * @return リズム分析対象楽曲位置の場合true
	 */
	private static boolean filterSpLeft(RhythmElement elem) {
		return elem.getNoteType(BeMusicDevice.SCRATCH1).hasMovement() ||
				elem.getNoteType(BeMusicDevice.SWITCH11).hasMovement() ||
				elem.getNoteType(BeMusicDevice.SWITCH12).hasMovement() ||
				elem.getNoteType(BeMusicDevice.SWITCH13).hasMovement();
	}

	/**
	 * タイムライン右サイドのリズム分析対象楽曲位置のフィルタリング判定(SP用)
	 * @param elem 要素
	 * @return リズム分析対象楽曲位置の場合true
	 */
	private static boolean filterSpRight(RhythmElement elem) {
		return elem.getNoteType(BeMusicDevice.SWITCH14).hasMovement() ||
				elem.getNoteType(BeMusicDevice.SWITCH15).hasMovement() ||
				elem.getNoteType(BeMusicDevice.SWITCH16).hasMovement() ||
				elem.getNoteType(BeMusicDevice.SWITCH17).hasMovement();
	}

	/**
	 * タイムライン左サイドのリズム分析対象楽曲位置のフィルタリング判定(DP用)
	 * @param elem 要素
	 * @return リズム分析対象楽曲位置の場合true
	 */
	private static boolean filterDpLeft(RhythmElement elem) {
		return elem.getPoint().hasMovementNote(BeMusicLane.PRIMARY);
	}

	/**
	 * タイムライン右サイドのリズム分析対象楽曲位置のフィルタリング判定(DP用)
	 * @param elem 要素
	 * @return リズム分析対象楽曲位置の場合true
	 */
	private static boolean filterDpRight(RhythmElement elem) {
		return elem.getPoint().hasMovementNote(BeMusicLane.SECONDARY);
	}
}
