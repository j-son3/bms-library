package com.lmt.lib.bms.internal.deltasystem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Predicate;

import com.lmt.lib.bms.BmsSpec;
import com.lmt.lib.bms.bemusic.BeMusicDevice;
import com.lmt.lib.bms.bemusic.BeMusicLane;
import com.lmt.lib.bms.bemusic.BeMusicNoteType;
import com.lmt.lib.bms.bemusic.BeMusicRatingType;
import com.lmt.lib.bms.internal.Utility;

/**
 * 譜面傾向「GIMMICK」の分析処理クラス
 */
public class GimmickAnalyzer extends RatingAnalyzer {
	/** 操作可能ノート有無判定関数 */
	private static final Predicate<GimmickElement> FN_HAS_MOVEMENT = e -> e.getPoint().hasMovementNote();
	/** 速度変更有無判定関数 */
	private static final Predicate<GimmickElement> FN_HAS_CHGSPEED = e -> e.getPoint().hasChangeSpeed();
	/** 譜面停止有無判定関数 */
	private static final Predicate<GimmickElement> FN_HAS_STOP = e -> e.getPoint().hasStop();
	/** 地雷有無判定関数 */
	private static final Predicate<GimmickElement> FN_HAS_MINE = e -> e.getPoint().hasMine();

	/** コンテキスト */
	private DsContext mCxt;
	/** 分析対象入力デバイスリスト */
	private List<BeMusicDevice> mDevs;
	/** 運指 */
	private Fingering mFingering;
	/** コンフィグ */
	private GimmickConfig mConfig;

	/**
	 * コンストラクタ
	 */
	public GimmickAnalyzer() {
		super(BeMusicRatingType.GIMMICK);
		clearContext();
	}

	/** {@inheritDoc} */
	@Override
	protected void compute(DsContext cxt) {
		var score = new GimmickScore();
		if (!cxt.chart.hasGimmick()) {
			// ギミックとなる要素を含まない場合は何もしない
			debugOut(cxt, 0.0, score.gimmick, Collections.emptyList(), score);
		} else {
			// 何らかのギミックを含む場合にのみ詳細分析を行う
			setupContext(cxt);

			// 動作がある、またはギミック要素のある楽曲位置を特定し、その楽曲位置に対する要素のリストを構築する
			var elems = RatingElement.listElements(cxt, GimmickElement::new, p -> p.hasMovementNote() || p.hasGimmick());

			// 速度変更ごとの範囲を解析する
			var speeds = parseSpeedRange(elems);
			for (var range : speeds) {
				for (var i = range.first; i <= range.last; i++) {
					elems.get(i).setRange(range);
				}
			}

			// 譜面停止の範囲を解析する
			if (cxt.chart.hasStop()) {
				speeds.forEach(r -> r.stopMap = parseStopRange(elems, r.first, r.last + 1));
			}

			// 地雷の範囲を解析する
			if (cxt.chart.hasMine()) {
				speeds.forEach(r -> r.mineMap = parseMineRange(elems, r.first, r.last + 1));
			}

			// 解析結果を評価し最終評価点を計算する
			evaluate(elems, score);

			// デバッグ出力
			debugOut(cxt, 0.0, score.gimmick, elems, score);

			clearContext();
		}

		cxt.stat.setRating(getRatingType(), score.gimmick);
	}

	/**
	 * 速度変更範囲を解析する
	 * @param elems 要素データリスト
	 * @return 速度変更範囲リスト
	 */
	private List<GimmickRange.Speed> parseSpeedRange(List<GimmickElement> elems) {
		// スクロール速度/BPM変更がない譜面では、譜面全体を唯一の範囲とする
		if (!mCxt.chart.hasChangeSpeed()) {
			return List.of(new GimmickRange.Speed(
					0, (elems.size() - 1),
					Utility.indexOf(elems, FN_HAS_MOVEMENT),
					Utility.lastIndexOf(elems, FN_HAS_MOVEMENT),
					mCxt.chart.getNoteCount(),
					new TreeMap<>(Map.of(0, mCxt.header.getInitialBpm()))));
		}

		// 先頭の速度変更パート～ノート操作パート～次範囲までの空白パートを1範囲とする範囲リストを構築する
		// 先頭の速度変更パートは、複数の速度変更を含む可能性がある。変更後速度内に操作可能ノートを含まない場合、
		// 単純な視覚演出として捉えるため、1つの範囲内に複数含めるものとする。
		var speeds = new ArrayList<GimmickRange.Speed>();
		var countElem = elems.size();
		var first = 0;
		var last = 0;
		var firstMovement = -1;
		var lastMovement = -1;
		var nextFirst = first;
		while (nextFirst != -1) {
			// 範囲検出処理の前準備
			var changes = new TreeMap<Integer, Double>();

			// 先頭の速度変更パートを検出する
			for (var chgCur = first; chgCur != -1;) {
				var chgNext = Utility.indexOf(elems, chgCur + 1, FN_HAS_CHGSPEED);
				if (chgNext == -1) {
					// 速度変更が見つからない場合は末尾までが範囲となる
					last = countElem - 1;
					firstMovement = Utility.indexOf(elems, chgCur, last + 1, FN_HAS_MOVEMENT);
					lastMovement = Utility.lastIndexOf(elems, last, chgCur, FN_HAS_MOVEMENT);
					nextFirst = -1;
				} else {
					// 速度変更範囲内で操作可能ノートの有無を確認する
					firstMovement = Utility.indexOf(elems, chgCur, chgNext, FN_HAS_MOVEMENT);
					if (firstMovement != -1) {
						// 速度変更範囲内に操作可能ノートがある場合、その範囲までが区切りとなる
						last = chgNext - 1;
						lastMovement = Utility.lastIndexOf(elems, last, chgCur, FN_HAS_MOVEMENT);
						nextFirst = chgNext;
						chgNext = -1;  // 範囲検出を終了させる
					}
				}
				changes.put(chgCur, elems.get(chgCur).getPoint().getCurrentSpeed());
				chgCur = chgNext;
			}

			// 検出した速度変更範囲を登録する
			var range = new GimmickRange.Speed(first, last, firstMovement, lastMovement, 0, changes);
			speeds.add(range);
			first = nextFirst;
		}

		// 範囲同士のリンクを設定する
		RatingRange.link(speeds);

		// 範囲ごとのノート数を計算する
		for (var range : speeds) {
			if (range.hasMovement()) {
				for (var i = range.firstMovement; i <= range.lastMovement; i++) {
					range.numNotes += elems.get(i).getPoint().getNoteCount();
				}
			}
		}

		// 速度変更範囲の遷移による基本的な操作内容を解析し決定しておく
		// ここで決定した操作内容(ギアチェン有無等)を後の評価点計算で使用する。
		var countRange = speeds.size();
		var rangeFirst = speeds.get(0);
		var curSpeed = rangeFirst.speed;
		rangeFirst.ideality = 0.0;
		rangeFirst.gearChange = false;
		for (var i = 1; i < countRange; i++) {
			var range = speeds.get(i);
			var speedMax = Math.max(Math.abs(curSpeed), Math.abs(range.speed));
			var speedMin = Math.min(Math.abs(curSpeed), Math.abs(range.speed));
			var changeRate = (speedMax / Math.max(speedMin, BmsSpec.BPM_MIN)) - 1.0;
			if ((changeRate <= mConfig.speedGearChangeDeltaPer) ||
					(range.numMovementPoint() <= mConfig.speedGearChangeEndurePts)) {
				// 速度が前の速度からあまり変化しない、または我慢可能な楽曲位置数ならギアチェンを行わない
				range.ideality = changeRate;
				range.gearChange = false;
			} else {
				// 速度・譜面内容が規定を超過する変化があるためギアチェンで対応する
				range.ideality = 0.0;
				range.gearChange = true;
				curSpeed = range.speed;
			}
		}

		return speeds;
	}

	/**
	 * 譜面停止範囲を解析する
	 * @param elems 要素データリスト
	 * @param start 解析開始位置
	 * @param end 解析終了位置(この位置を含まない)
	 * @return 譜面停止範囲マップ(要素データリストインデックスで昇順ソート)
	 */
	private TreeMap<Integer, GimmickRange.Stop> parseStopRange(List<GimmickElement> elems, int start, int end) {
		var stops = new TreeMap<Integer, GimmickRange.Stop>();
		var stpFirst = Utility.indexOf(elems, start, end, FN_HAS_STOP);
		while (stpFirst != -1) {
			// 影響対象の操作可能ノートの先頭位置を検索する
			var influFirst = Utility.indexOf(elems, stpFirst + 1, end, FN_HAS_MOVEMENT);

			// 操作可能ノートに影響しない譜面停止を検出する
			var rangeEmpty = (GimmickRange.Stop)null;
			var stpPos = stpFirst;
			while ((stpPos = Utility.indexOf(elems, stpPos, end, FN_HAS_STOP)) != -1) {
				// 当該譜面停止を影響なし範囲へ登録するかどうかを判定する
				if (influFirst != -1) {
					if (stpPos < influFirst) {
						// 次の操作可能ノートより手前で、且つその位置までの時間が長い場合は影響なし範囲への登録となる
						var stopTime = elems.get(stpPos).getPoint().getStop();
						var distance = RatingElement.timeDelta(elems, stpPos, influFirst) - stopTime;
						if (distance <= mConfig.stopInfluenceTimeStop) { break; }
					} else {
						// 譜面停止位置が次の操作可能ノート以降の場合は影響なし範囲の検出を終了する
						break;
					}
				}

				// 影響なし範囲へ譜面停止を登録する
				if (rangeEmpty == null) {
					rangeEmpty = new GimmickRange.Stop(stpPos, stpPos, -1);
				}
				rangeEmpty.stops.put(stpPos, elems.get(stpPos).getPoint().getStop());
				rangeEmpty.last = stpPos;
				stpPos++;
			}

			// 操作可能ノートに影響する譜面停止の範囲を検出する
			var rangeInflu = (GimmickRange.Stop)null;
			if (influFirst != -1) {
				// 譜面停止を検出する
				stpPos = (rangeEmpty == null) ? stpFirst : (rangeEmpty.lastStopPos() + 1);
				while ((stpPos = Utility.indexOf(elems, stpPos, influFirst, FN_HAS_STOP)) != -1) {
					if (rangeInflu == null) {
						rangeInflu = new GimmickRange.Stop(stpPos, influFirst, influFirst);
					}
					rangeInflu.stops.put(stpPos, elems.get(stpPos).getPoint().getStop());
					stpPos++;
				}

				// 譜面停止範囲と、最後の譜面停止に影響する操作可能ノートの範囲を特定する
				// ※操作可能ノート範囲は譜面停止範囲を超えることがある
				if (rangeInflu != null) {
					var expandLast = true;
					var e = elems.get(rangeInflu.lastStopPos());
					var influenceTimeLast = e.getTime() + e.getPoint().getStop() + mConfig.stopInfluenceTimeAfter;
					for (var i = rangeInflu.firstInfluence; i < end; i++) {
						// 最後の譜面停止の影響範囲外になったら終了
						var p = elems.get(i).getPoint();
						if ((p.getTime() > influenceTimeLast)) {
							break;
						}

						// 操作可能ノート範囲の終了位置を更新する
						if (p.hasMovementNote()) {
							rangeInflu.notesInfluence += p.getNoteCount();
							rangeInflu.lastInfluence = i;
						}

						// 次の譜面停止以降は譜面停止範囲を拡張しない
						expandLast = expandLast && !p.hasStop();
						if (expandLast) {
							rangeInflu.last = i;
						}
					}
				}
			}

			// 検出した譜面停止範囲を登録する
			var findNext = end;
			if (rangeEmpty != null) {
				stops.put(rangeEmpty.first, rangeEmpty);
				findNext = rangeEmpty.last + 1;  // 最後の譜面停止の次の位置から
			}
			if (rangeInflu != null) {
				stops.put(rangeInflu.first, rangeInflu);
				findNext = rangeInflu.last + 1;  // 影響する操作可能ノート終端の次の位置から
			}
			stpFirst = Utility.indexOf(elems, findNext, end, FN_HAS_STOP);
		}

		// 範囲同士のリンクを設定する
		RatingRange.link(stops.values());

		return stops;
	}

	/**
	 * 地雷範囲を解析する
	 * @param elems 要素データリスト
	 * @param start 解析開始位置
	 * @param end 解析終了位置(この位置を含まない)
	 * @return 地雷範囲マップ(要素データリストインデックスで昇順ソート)
	 */
	private TreeMap<Integer, GimmickRange.MineGroup> parseMineRange(List<GimmickElement> elems, int start, int end) {
		// 検出対象範囲内に地雷が含まれない場合は空リストを返す
		var mineFirst = Utility.indexOf(elems, start, end, FN_HAS_MINE);
		if (mineFirst == -1) {
			return new TreeMap<Integer, GimmickRange.MineGroup>();
		}

		// 地雷オブジェの最終位置を特定する
		var mineLast = Utility.lastIndexOf(elems, end - 1, mineFirst, FN_HAS_MINE);

		// 操作可能ノートのある楽曲位置ごとに、地雷の影響がある範囲を解析する
		var mines = new TreeMap<Integer, GimmickRange.Mine>();
		var timeMine = mConfig.mineInfluenceTime;
		var mvPos = mineFirst;
		while ((mvPos = Utility.indexOf(elems, mvPos, mineLast + 1, FN_HAS_MOVEMENT)) != -1) {
			var hasMine = elems.get(mvPos).getPoint().hasMine();

			// 楽曲位置手前の地雷範囲を解析する
			var rangeFirst = mvPos;
			for (var i = mvPos - 1; (i >= mineFirst) && (RatingElement.timeDelta(elems, i, mvPos) <= timeMine); i--) {
				if (elems.get(i).getPoint().hasMine()) {
					rangeFirst = i;
					hasMine = true;
				}
			}

			// 楽曲位置以後の地雷範囲を解析する
			var rangeLast = mvPos;
			for (var i = mvPos + 1; (i <= mineLast) && (RatingElement.timeDelta(elems, mvPos, i) <= timeMine); i++) {
				if (elems.get(i).getPoint().hasMine()) {
					rangeLast = i;
					hasMine = true;
				}
			}

			// 地雷が検出された場合、その範囲を登録する
			if (hasMine) {
				var rangeMine = new GimmickRange.Mine(rangeFirst, rangeLast, mvPos, true);
				mines.put(rangeMine.movement, rangeMine);
			}
			mvPos++;
		}

		// 地雷範囲ごとの間に地雷オブジェのみが存在する範囲があるかを解析し、あれば操作可能ノートなし範囲として登録する
		if (mines.size() == 0) {
			// 地雷が存在する範囲内に操作可能ノートがある楽曲位置が全く存在しない
			var rangeMine = new GimmickRange.Mine(mineFirst, mineLast);
			mines.put(rangeMine.movement, rangeMine);
		} else {
			// 全ての地雷範囲の間にある未登録の範囲を操作可能ノートなし範囲とする
			var mineRangeHead = mines.firstEntry().getValue();
			if (mineRangeHead.first > mineFirst) {
				// 検出した地雷の先頭部分に空き領域がある
				var last = Utility.lastIndexOf(elems, mineRangeHead.first - 1, mineFirst, FN_HAS_MINE);
				mines.put(mineFirst, new GimmickRange.Mine(mineFirst, last));
			}
			var mineRangeTail = mines.lastEntry().getValue();
			if (mineRangeTail.last < mineLast) {
				// 検出した地雷の末尾部分に空き領域がある
				var first = Utility.indexOf(elems, mineRangeTail.last + 1, mineLast + 1, FN_HAS_MINE);
				mines.put(first, new GimmickRange.Mine(first, mineLast));
			}

			// 地雷範囲同士の間で、操作可能ノートに干渉しない地雷の範囲を検出する
			var mineCur = (GimmickRange.Mine)null;
			var minesEmpty = (List<GimmickRange.Mine>)null;
			for (var mineNext : mines.values()) {
				if (mineCur != null) {
					var emptyFirst = mineCur.last + 1;
					if (emptyFirst < mineNext.first) {
						// 地雷範囲の間に1点以上の空き領域がある
						var first = Utility.indexOf(elems, emptyFirst, mineNext.first, FN_HAS_MINE);
						if (first != -1) {
							var last = Utility.lastIndexOf(elems, mineNext.first - 1, first, FN_HAS_MINE);
							minesEmpty = (minesEmpty == null) ? new ArrayList<>() : minesEmpty;
							minesEmpty.add(new GimmickRange.Mine(first, last));
						}
					}
				}
				mineCur = mineNext;
			}

			// 操作可能ノートに干渉しない地雷の範囲をマージする
			if (minesEmpty != null) {
				for (var mine : minesEmpty) {
					mines.put(mine.first, mine);
				}
			}
		}

		// 地雷範囲が重複して連続する単位ごとにグループ化する
		var result = new TreeMap<Integer, GimmickRange.MineGroup>();
		var group = new GimmickRange.MineGroup();
		for (var mine : mines.values()) {
			if (!group.mines.isEmpty() && !group.mines.lastEntry().getValue().contains(mine.first)) {
				// ひとつ前の地雷範囲と楽曲位置が競合していなければ、同一グループではないので
				// グループをブレークし、次のグループへ投入するようにする
				group.fix();
				result.put(group.first, group);
				group = new GimmickRange.MineGroup();
			}
			group.mines.put(mine.movement, mine);
		}
		group.fix();
		result.put(group.first, group);

		// 範囲同士のリンクを設定する
		RatingRange.link(mines.values());
		RatingRange.link(result.values());

		return result;
	}

	/**
	 * GIMMICK評価処理
	 * @param elems 要素データリスト
	 * @param outScore 評価結果
	 */
	private void evaluate(List<GimmickElement> elems, GimmickScore outScore) {
		outScore.playTime = RatingElement.computeTimeOfPointRange(elems);
		outScore.totalNotes = mCxt.chart.getNoteCount();

		// 速度変更を評価する
		evaluateSpeed(elems, outScore);

		// 譜面停止を評価する
		if (mCxt.chart.hasStop()) {
			evaluateStop(elems, outScore);
		}

		// 地雷を評価する
		if (mCxt.chart.hasMine()) {
			evaluateMine(elems, outScore);
		}

		// それぞれの評価点から最終評価点を計算する
		var scores = new double[] { outScore.speedScore, outScore.stopScore, outScore.mineScore };
		Arrays.sort(scores);
		outScore.gimmick = Math.min(getRatingType().getMax(), (int)(
				(scores[2] * mConfig.commonInfluencePrimaryScore) +
				(scores[1] * mConfig.commonInfluenceSecondaryScore) +
				(scores[0] * mConfig.commonInfluenceTertiaryScore)));
	}

	/**
	 * 速度変更の評価
	 * @param elems 要素データリスト
	 * @param outScore 評価結果(速度変更の結果を更新する)
	 */
	private void evaluateSpeed(List<GimmickElement> elems, GimmickScore outScore) {
		//if (true) { return; } // ***** FOR DEBUG *****
		var numRange = 0;
		var numPerceived = 0;
		var numGearChange = 0;
		var topRange = elems.get(0).getRange();
		var prevSpeed = topRange.speed;
		var curSpeed = topRange.speed;
		var scoreRangeSum = 0.0;
		for (var range = topRange; range != null; range = range.next) {
			numRange++;

			// 速度変化知覚回数を計算する
			var nowSpeed = range.speed;
			var perceiveFromPrev = (Math.abs(nowSpeed - prevSpeed) >= mConfig.speedMinPerceive);
			var perceiveFromCur = (Math.abs(nowSpeed - curSpeed) >= mConfig.speedMinPerceive);
			if (perceiveFromPrev || perceiveFromCur) {
				// 速度変化を知覚した
				curSpeed = nowSpeed;
				numPerceived++;
			}

			// ギアチェンが必要な速度変更範囲の場合、ギアチェン難易度を計算する
			range.scoreGearChange = 0.0;
			if (range.gearChange) {
				var timeBeforeGearChange = range.timeBeforeGearChange(elems);
				var timeGearChange = range.timeGearChange(elems);
				var densityRange = range.numNotes / range.time(elems);
				var densityGearChange = range.computeGearChangeDensity(elems, mConfig.speedGearChangeDensityTime);
				var densityAvg = (densityRange + densityGearChange) / 2.0;
				var value1 = mConfig.speedIpfnGcBefore.compute(timeBeforeGearChange);
				var value2 = mConfig.speedIpfnGcTime.compute(timeGearChange);
				var value3 = mConfig.speedIpfnGcDensity.compute(densityAvg);
				range.scoreGearChange = value1 * value2 * value3;
				numGearChange++;
			}

			// 速度変更範囲対応難易度を計算する
			var rangeDifficulty = mConfig.speedIpfnIdeality.compute(range.ideality);
			if (range.isReverseScroll()) {
				rangeDifficulty += mConfig.speedReverseScrollAddDifficulty;
			}

			// 速度変更範囲評価点を計算する
			var rateAdjust = Math.min(1.0, mConfig.speedMaxEvaluateTime / range.time(elems));
			var noteRate = (double)range.numNotes / outScore.totalNotes;
			range.score = (range.scoreGearChange + rangeDifficulty) * (noteRate * rateAdjust);
			scoreRangeSum += range.score;

			prevSpeed = range.speed;
		}

		// 計算結果を集計する
		var changeRate = numPerceived / outScore.playTime;
		var changeRateValue = mConfig.speedIpfnChgRate.compute(changeRate);
		var reqDensity = Math.max(0.001, mConfig.speedIpfnCrAdjust.compute(changeRateValue));
		var avgDensity = mCxt.chart.getNoteCount() / outScore.playTime;
		var changeRateAdjust = Math.min(1.0, Math.max(mConfig.speedMinChangeRateAdjust, avgDensity / reqDensity));
		var scoreChangeRate = changeRateValue * changeRateAdjust;
		var scoreHigh = 0.0;
		var scoreLow = 0.0;
		if (scoreRangeSum > scoreChangeRate) {
			// 速度変更範囲評価点のほうが高い
			scoreHigh = scoreRangeSum * mConfig.speedInfluenceHighScore;
			scoreLow = scoreChangeRate * mConfig.speedInfluenceLowScore;
		} else {
			// 速度変化頻度評価点のほうが高い
			scoreHigh = scoreChangeRate * mConfig.speedInfluenceHighScore;
			scoreLow = scoreRangeSum * mConfig.speedInfluenceLowScore;
		}
		outScore.speedNumRange = numRange;
		outScore.speedNumChangePerceived = numPerceived;
		outScore.speedNumChangeGear = numGearChange;
		outScore.speedScoreRangeSum = scoreRangeSum;
		outScore.speedScoreChgRate = scoreChangeRate;
		outScore.speedScoreOrg = computeRatingValue(scoreHigh + scoreLow, outScore.playTime, 1.0);
		outScore.speedScore = (int)mConfig.speedIpfnGimmick.compute(outScore.speedScoreOrg);
	}

	/**
	 * 譜面停止の評価
	 * @param elems 要素データリスト
	 * @param outScore 評価結果(譜面停止の結果を更新する)
	 */
	private void evaluateStop(List<GimmickElement> elems, GimmickScore outScore) {
		//if (true) { return; } // ***** FOR DEBUG *****
		var numRange = 0;
		var numEffective = 0;
		var areaTime = 0.0;
		var stopSummary = new ScoreSummarizer(1.0);
		var topRange = elems.get(0).getRange();
		for (var speed = topRange; speed != null; speed = speed.next) {
			for (var stop : speed.stopMap.values()) {
				// 譜面停止対応難易度評価点を計算する
				// ただし、操作を伴わない範囲の譜面停止については一律0点とする(ギミック対応の難易度に結び付かないため)
				var rangeScore = 0.0;
				if (stop.hasInfluence()) {
					// 譜面停止の難易度を計算する
					var stopDifficulty = 0.0;
					for (var entry : stop.stops.entrySet()) {
						var stopIndex = entry.getKey();
						var stopTime = entry.getValue();
						var timeDelta = RatingElement.timeDelta(elems, stopIndex, stop.firstInfluence) - stopTime;
						var value1 = mConfig.stopIpfnStopEffective.compute(timeDelta);
						var value2 = mConfig.stopIpfnStopTime.compute(stopTime);
						stopDifficulty += (value1 * value2);
					}

					// 譜面停止解除後のノート平均密度から評価点計算用の係数を算出する
					var avgDensity = stop.computeInfluenceDensity(elems);
					var densityValue = mConfig.stopIpfnAfterDensity.compute(avgDensity);

					// 譜面停止範囲影響率評価点を計算する
					rangeScore = stopDifficulty * densityValue;
					areaTime += stop.computeEffectiveRangeTime(elems, mConfig.stopMinEffectiveStopTime);
					numEffective++;
				}

				// 譜面停止範囲評価点をサマリ登録
				stop.score = rangeScore;
				stopSummary.put(elems.get(stop.first).getTime(), stop.score);
				numRange++;
			}
		}

		// 計算結果を集計する
		var scoreRange = stopSummary.summary();
		var scoreArea = mConfig.stopIpfnEffectiveRange.compute(areaTime / outScore.playTime);
		var scoreHigh = 0.0;
		var scoreLow = 0.0;
		if (scoreRange > scoreArea) {
			// 譜面停止対応難易度評価点のほうが高い
			scoreHigh = scoreRange * mConfig.stopInfluenceHighScore;
			scoreLow = scoreArea * mConfig.stopInfluenceLowScore;
		} else {
			// 譜面停止影響範囲率評価点のほうが高い
			scoreHigh = scoreArea * mConfig.stopInfluenceHighScore;
			scoreLow = scoreRange * mConfig.stopInfluenceLowScore;
		}
		outScore.stopNumRange = numRange;
		outScore.stopNumEffective = numEffective;
		outScore.stopSummary = stopSummary;
		outScore.stopAreaTime = areaTime;
		outScore.stopScoreRange = scoreRange;
		outScore.stopScoreArea = scoreArea;
		outScore.stopScoreOrg = computeRatingValue(scoreHigh + scoreLow, outScore.playTime, 1.0);
		outScore.stopScore = (int)mConfig.stopIpfnGimmick.compute(outScore.stopScoreOrg);
	}

	/**
	 * 地雷の評価
	 * @param elems 要素データリスト
	 * @param outScore 評価結果(地雷の評価結果を更新する)
	 */
	private void evaluateMine(List<GimmickElement> elems, GimmickScore outScore) {
		//if (true) { return; } // ***** FOR DEBUG *****
		var layout = mCxt.layout;
		var numRange = 0;
		var numEffective = 0;
		var effectiveTime = 0.0;
		var mineSummary = new ScoreSummarizer(mConfig.mineSaturateRangeScore);
		var topRange = elems.get(0).getRange();
		for (var speed = topRange; speed != null; speed = speed.next) {
			for (var mineGrp : speed.mineMap.values()) {
				var emptyTime = 0.0;
				for (var mine : mineGrp.mines.values()) {
					if (mine.hasMovement()) {
						// 地雷範囲内に操作可能ノートがある場合は地雷の配置による操作難易度を計算する
						var score = 0.0;
						var mvTime = elems.get(mine.movement).getTime();
						for (var dev : mDevs) {
							var finger = mFingering.getFinger(dev);
							var resists = mFingering.getResists(finger);
							for (var resist : resists) {
								for (var i = mine.first; i <= mine.last; i++) {
									var devResist = layout.get(mFingering.getDevice(resist.getFinger()));
									var p = elems.get(i).getPoint();
									if (p.getVisibleNoteType(devResist) == BeMusicNoteType.MINE) {
										// 操作可能ノートと地雷の距離から、加算する評価点を計算する
										var mineDamage = p.getVisibleValue(devResist);
										var mineTime = p.getTime();
										var distance = Math.abs(mvTime - mineTime);
										var distanceValue = mConfig.mineIpfnMineDistance.compute(distance);
										var damageValue = mConfig.mineIpfnMineDamage.compute(mineDamage);
										score += (distanceValue * damageValue * resist.getValue());
									}
								}
							}
						}
						mine.score = score;
						mineSummary.put(elems.get(mine.first).getTime(), mine.score);
						numEffective++;
					} else {
						// 地雷範囲内に操作可能ノートがない場合は空白時間を計算しておく
						emptyTime += mine.time(elems);
					}
				}

				// 地雷の影響時間を更新する
				effectiveTime += (mineGrp.time(elems) - emptyTime);
				numRange += mineGrp.mines.size();
			}
		}

		// 計算結果を集計する
		var scoreRange = mineSummary.summary();
		var scoreEffective = mConfig.mineIpfnEffectiveRange.compute(effectiveTime / outScore.playTime);
		var scoreHigh = 0.0;
		var scoreLow = 0.0;
		if (scoreRange > scoreEffective) {
			// 地雷回避難易度のほうが高い
			scoreHigh = scoreRange * mConfig.mineInfluenceHighScore;
			scoreLow = scoreEffective * mConfig.mineInfluenceLowScore;
		} else {
			// 地雷影響範囲率評価点のほうが高い
			scoreHigh = scoreEffective * mConfig.mineInfluenceHighScore;
			scoreLow = scoreRange * mConfig.mineInfluenceLowScore;
		}
		outScore.mineNumRange = numRange;
		outScore.mineNumEffective = numEffective;
		outScore.mineSummary = mineSummary;
		outScore.mineEffectiveTime = effectiveTime;
		outScore.mineScoreRange = scoreRange;
		outScore.mineScoreEffective = scoreEffective;
		outScore.mineScoreOrg = computeRatingValue(scoreHigh + scoreLow, outScore.playTime, 1.0);
		outScore.mineScore = (int)mConfig.mineIpfnGimmick.compute(outScore.mineScoreOrg);
	}

	/** {@inheritDoc} */
	@Override
	protected void dumpSummary(DsContext cxt, double org, int rating, Object... values) {
		var sb = new StringBuilder();
		var s = (GimmickScore)values[0];
		sb.append(cxt.header.getComment());
		sb.append("\t").append(String.format("%s %s", cxt.header.getTitle(), cxt.header.getSubTitle()).strip());
		sb.append("\t").append(s.totalNotes);
		sb.append("\t").append(String.format("%.2f", s.playTime));
		// [SPEED]
		sb.append("\t").append(s.speedNumRange);
		sb.append("\t").append(s.speedNumChangePerceived);
		sb.append("\t").append(s.speedNumChangeGear);
		sb.append("\t").append(String.format("%.4f", s.speedScoreRangeSum));
		sb.append("\t").append(String.format("%.4f", s.speedScoreChgRate));
		sb.append("\t").append(String.format("%.4f", s.speedScoreOrg));
		sb.append("\t").append(String.format("%.2f", getRatingType().toValue(s.speedScore)));
		// [STOP]
		sb.append("\t").append(s.stopNumRange);
		sb.append("\t").append(s.stopNumEffective);
		sb.append("\t").append(String.format("%.2f", s.stopAreaRate()));
		sb.append("\t").append(String.format("%.4f", s.stopScoreRange));
		sb.append("\t").append(String.format("%.4f", s.stopScoreArea));
		sb.append("\t").append(String.format("%.4f", s.stopScoreOrg));
		sb.append("\t").append(String.format("%.4f", getRatingType().toValue(s.stopScore)));
		// [MINE]
		sb.append("\t").append(s.mineNumRange);
		sb.append("\t").append(s.mineNumEffective);
		sb.append("\t").append(String.format("%.2f", s.mineEffectiveRate()));
		sb.append("\t").append(String.format("%.4f", s.mineScoreRange));
		sb.append("\t").append(String.format("%.4f", s.mineScoreEffective));
		sb.append("\t").append(String.format("%.4f", s.mineScoreOrg));
		sb.append("\t").append(String.format("%.2f", getRatingType().toValue(s.mineScore)));
		// [GIMMICK]
		sb.append("\t").append(String.format("%.2f", getRatingType().toValue(s.gimmick)));
		Ds.debug(sb);
	}

	/** {@inheritDoc} */
	@Override
	protected void dumpDetail(DsContext cxt, double org, int rating, List<? extends RatingElement> elems,
			Object... values) {
		super.dumpDetail(cxt, org, rating, elems, values);
		var s = (GimmickScore)values[0];
		Ds.debug("[SPEED]");
		Ds.debug("  numRange=%d", s.speedNumRange);
		Ds.debug("  numPerceived=%d", s.speedNumChangePerceived);
		Ds.debug("  numGearChange=%d", s.speedNumChangeGear);
		Ds.debug("  scoreChangeRate=%.4f", s.speedScoreChgRate);
		Ds.debug("  scoreRangeSum=%.4f", s.speedScoreRangeSum);
		Ds.debug("  result=%.2f (Org=%.4f)", s.speedScore, s.speedScoreOrg);
		Ds.debug("[STOP]");
		Ds.debug("  numRange=%d", s.stopNumRange);
		Ds.debug("  numEffective=%d", s.stopNumEffective);
		Ds.debug("  areaTime=(Time=%.2fs, Rate=%.2f%%)", s.stopAreaTime, s.stopAreaRatePer());
		Ds.debug("  rangeSummary=%s", s.stopSummary);
		Ds.debug("  scoreRange=%.4f", s.stopScoreRange);
		Ds.debug("  scoreArea=%.4f", s.stopScoreArea);
		Ds.debug("  result=%.2f (Org=%.4f)", s.stopScore, s.stopScoreOrg);
		Ds.debug("[MINE]");
		Ds.debug("  numRange=%d", s.mineNumRange);
		Ds.debug("  numEffective=%d", s.mineNumEffective);
		Ds.debug("  effectiveTime=(Time=%.2fs, Rate=%.2f%%)", s.mineEffectiveTime, s.mineEffectiveRatePer());
		Ds.debug("  rangeSummary=%s", s.mineSummary);
		Ds.debug("  scoreRange=%.4f", s.mineScoreRange);
		Ds.debug("  scoreEffective=%.4f", s.mineScoreEffective);
		Ds.debug("  result=%.2f (Org=%.4f)", s.mineScore, s.mineScoreOrg);
	}

	/**
	 * 分析用データのセットアップ
	 * @param cxt コンテキスト
	 */
	private void setupContext(DsContext cxt) {
		mCxt = cxt;
		mDevs = BeMusicDevice.getDevices(BeMusicLane.PRIMARY); // TODO DPモードに対応する
		mFingering = Fingering.SP_MINE; // TODO DPモードに対応する
		mConfig = GimmickConfig.getInstance();
	}

	/**
	 * 分析用データのクリア
	 */
	private void clearContext() {
		mCxt = null;
		mDevs = null;
		mFingering = null;
		mConfig = null;
	}
}
