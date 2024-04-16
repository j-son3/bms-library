package com.lmt.lib.bms.internal.deltasystem;

import java.util.List;
import java.util.function.Predicate;

import com.lmt.lib.bms.bemusic.BeMusicDevice;
import com.lmt.lib.bms.bemusic.BeMusicLane;
import com.lmt.lib.bms.bemusic.BeMusicLongNoteMode;
import com.lmt.lib.bms.bemusic.BeMusicNoteType;
import com.lmt.lib.bms.bemusic.BeMusicPoint;
import com.lmt.lib.bms.bemusic.BeMusicRatingType;

/**
 * 譜面傾向「SCRATCH」の分析処理クラス
 */
public class ScratchAnalyzer extends RatingAnalyzer {
	/** スイッチデバイスのリスト */
	private List<BeMusicDevice> mDevSws;
	/** スクラッチデバイス */
	private BeMusicDevice mDevScr;
	/** LNモード */
	private BeMusicLongNoteMode mLnMode;
	/** 回転操作の終点で逆回転操作が必要かどうか */
	private boolean mSpinTurn;
	/** 回転操作の全体が評価対象かどうか(LN/CNは一部、HCNは全体) */
	private boolean mSpinFull;
	/** マッピングデータ */
	private Scratch.Mapping mMapping;
	/** SCRATCHコンフィグ */
	private ScratchConfig mConfig;

	/**
	 * コンストラクタ
	 */
	public ScratchAnalyzer() {
		super(BeMusicRatingType.SCRATCH);
	}

	/** {@inheritDoc} */
	@Override
	protected void compute(DsContext cxt) {
		// プレーモードごとの算出処理を実行する
		var scratch = -1;
		if (!cxt.dpMode) {
			// シングルプレー
			var score = computeTimeline(cxt, BeMusicLane.PRIMARY);
			debugOut(cxt, score.scoreOrg, score.score, score.elems, score);
			scratch = score.score;
		} else {
			// ダブルプレー
			// TODO ダブルプレーのSCRATCH値算出に対応する
		}
		clearContext();

		cxt.stat.setRating(getRatingType(), scratch);
	}

	/**
	 * タイムライン分析メイン処理
	 * @param cxt コンテキスト
	 * @param lane 分析対象レーン
	 * @return 評価結果
	 */
	private Scratch.Score computeTimeline(DsContext cxt, BeMusicLane lane) {
		// 初期化処理
		setupContext(cxt, lane);
		var tlScore = new Scratch.Score(lane);

		// スクラッチが全く存在しない譜面では評価を実行しない
		if (cxt.chart.getNoteCount(mDevScr) == 0) {
			tlScore.playTime = cxt.chart.getPlayTime();
			return tlScore;
		}

		// 動作のある楽曲位置を特定し、その楽曲位置に対する要素のリストを構築する
		// (BGAやBPM変化、譜面停止のみの楽曲位置等、入力デバイス操作を伴わない楽曲位置を除外したリスト)
		var elems = RatingElement.listElements(cxt, ScratchElement::new, BeMusicPoint::hasMovementNote);
		var countElem = elems.size();

		// 最初のスクラッチ位置から、スクラッチの種別と範囲を特定する
		// この処理ブロックでは、スクラッチの範囲と種別の判定を行う専用の処理とする。
		// 判定された範囲に対する評価は別途、後続の処理で行うものとする。
		// ※スクラッチ有無はチェック済みのため、以降の処理は必ずスクラッチが存在する前提として処理する
		var idxBehindFirst = -1;
		var idxFirst = 0;
		var numSingle = 0;
		var numPulse = 0;
		var numRtrip = 0;
		var numSpin = 0;
		var scrRangeTop = (ScratchRange)null;
		var scrRangePrev = (ScratchRange)null;
		while ((idxFirst = nextScratchStart(elems, idxBehindFirst)) < countElem) {
			// スクラッチの範囲・種類を決定する
			var scrRange = (ScratchRange)null;
			if (elems.get(idxFirst).getNoteType(mDevScr) == BeMusicNoteType.LONG_ON) {
				// 先頭SCRが長押し開始の場合、回転操作となる
				var idxNext = -1;
				var idxLast = nextScratchEnd(elems, idxFirst);
				if (idxLast >= countElem) {
					// 末尾SCRが見つからない場合は末尾楽曲位置までの回転操作とする(フェイルセーフ)
					scrRange = new ScratchRange.Spinning(idxFirst, (countElem - 1), false, false);
					numSpin++;
				} else if (!elems.get(idxLast).getNoteType(mDevScr).isLongNoteTail()) {
					// 末尾スクラッチが長押し終了ではない場合は末尾SCRまでの回転操作とする(フェイルセーフ)
					scrRange = new ScratchRange.Spinning(idxFirst, idxLast, false, false);
					numSpin++;
				} else if (((idxNext = nextScratchStart(elems, idxLast)) >= countElem) || (mSpinTurn) ||
						(RatingElement.timeDelta(elems, idxLast, idxNext) > mConfig.maxSpinDockTime) ||
						(elems.get(idxNext).getNoteType(mDevScr) != BeMusicNoteType.BEAT)) {
					// 終端の次SCRなし、LN以外、終端～次SCRの時間が規定時間以上、次SCRが短押し以外のいずれかの場合
					if (RatingElement.timeDelta(elems, idxFirst, idxLast) >= mConfig.minSpinTime) {
						// 回転操作時間が規定時間以上の場合は回転操作とする
						scrRange = new ScratchRange.Spinning(idxFirst, idxLast, mSpinTurn, mSpinFull);
						numSpin++;
					} else {
						// 回転操作時間が規定時間未満の場合は往復操作とする
						scrRange = new ScratchRange.RoundTrip(idxFirst, idxLast);
						numRtrip++;
					}
				} else {
					// この分岐に入るということは、LNモードで疑似的な回転操作の配置が行われていることを示す。
					// その場合の回転操作の末尾は長押し終了までではなく、その次の短押しSCRまでとする。
					if (RatingElement.timeDelta(elems, idxFirst, idxNext) >= mConfig.minSpinTime) {
						// 回転操作時間が規定時間以上の場合は回転操作とする
						scrRange = new ScratchRange.Spinning(idxFirst, idxNext, true, false);
						numSpin++;
					} else {
						// 回転操作時間が規定時間未満の場合は往復操作とする
						scrRange = new ScratchRange.RoundTrip(idxFirst, idxNext);
						numRtrip++;
					}
				}
			} else {
				// 先頭SCRが短押しの場合
				var idxNext = nextScratchStart(elems, idxFirst);
				var pulseTime = 0.0;
				if (idxNext >= countElem) {
					// 次SCRがない場合は単体操作とする
					scrRange = new ScratchRange.Single(idxFirst);
					numSingle++;
				} else if ((elems.get(idxNext).getNoteType(mDevScr) != BeMusicNoteType.BEAT) ||
						((pulseTime = RatingElement.timeDelta(elems, idxFirst, idxNext)) > mConfig.maxPulseTime)) {
					// 次SCRが短押し以外、または刻み時間が規定よりも長い場合は先頭SCRの単体操作とする
					scrRange = new ScratchRange.Single(idxFirst);
					numSingle++;
				} else {
					// 短押しSCRの刻み時間が規定範囲内で連続する連続操作とする
					var idxPrev = idxFirst;
					var pulseRange = new ScratchRange.Pulse(pulseTime);
					numPulse++;
					pulseRange.put(idxFirst);
					while (true) {
						// SCRを連続操作の評価対象として追加する
						pulseRange.put(idxNext);

						// 次SCRが連続操作に含まれるかどうかを判定する
						idxPrev = idxNext;
						if (((idxNext = nextScratchStart(elems, idxNext)) >= countElem)) {
							break;  // 次SCRがない
						}
						if (elems.get(idxNext).getNoteType(mDevScr) != BeMusicNoteType.BEAT) {
							break;  // 次SCRが短押しではない
						}
						var timeDelta = RatingElement.timeDelta(elems, idxPrev, idxNext);
						if (Math.abs(pulseTime - timeDelta) > mConfig.acceptableDelta) {
							break;  // 時間差分が刻み時間範囲外
						}
					}

					// 検出した範囲を連続操作の範囲として確定する
					pulseRange.determineEnds();
					scrRange = pulseRange;
				}
			}

			// スクラッチ範囲に必要なその他の情報を設定する
			scrRange.behind = Math.max(idxBehindFirst, 0);
			if (scrRangePrev != null) {
				scrRangePrev.next = scrRange;
			}
			scrRangePrev = scrRange;
			idxBehindFirst = scrRange.last.position;
			if (scrRangeTop == null) {
				scrRangeTop = scrRange;
			}
		}

		// スクラッチの種別ごとの分析を行い、スクラッチ範囲評価点を計算する
		// ここまでの処理ではスクラッチ範囲・種類のみが決定されている状態となる。
		// 以下のループで全スクラッチ範囲のノート配置を参照して評価点を計算していく。
		var direction = Scratch.Direction.OUTER;
		var scrTotalTime = 0.0;
		for (var scrRange = scrRangeTop; scrRange != null; scrRange = scrRange.next) {
			// 後方時間(スクラッチのない期間)が長い場合はスクラッチの操作方向をリセットする
			if (RatingElement.timeDelta(elems, scrRange.behind, scrRange.first.position) >= mConfig.directionResetTime) {
				direction = Scratch.Direction.OUTER;
			}

			// スクラッチ種別ごとの評価処理
			var rangeType = scrRange.type;
			if (rangeType == Scratch.Type.SINGLE) {
				// 単体操作
				var range = (ScratchRange.Single)scrRange;
				var behind = RatingElement.timeDelta(elems, range.behind, range.first.position);
				evaluateBeat(range.first, behind, direction, elems);
				direction = direction.reverse();
			} else if (rangeType == Scratch.Type.PULSE) {
				// 連続操作
				var range = (ScratchRange.Pulse)scrRange;
				for (var eval : range.scratches.values()) {
					evaluateBeat(eval, range.pulseTime, direction, elems);
					direction = direction.reverse();
				}
			} else if (rangeType == Scratch.Type.ROUND_TRIP) {
				// 往復操作
				var range = (ScratchRange.RoundTrip)scrRange;
				var behind1 = RatingElement.timeDelta(elems, range.behind, range.first.position);
				evaluateBeat(range.first, behind1, direction, elems);
				var behind2 = RatingElement.timeDelta(elems, range.first.position, range.last.position);
				evaluateBeat(range.last, behind2, direction.reverse(), elems);
			} else if (rangeType == Scratch.Type.SPINNING) {
				// 回転操作
				var range = (ScratchRange.Spinning)scrRange;
				evaluateSpinning(range, direction, elems);
				direction = range.turn ? direction : direction.reverse();
			} else {
				// Do nothing
			}

			// 要素リストにスクラッチ範囲のデータを設定する
			for (var i = scrRange.first.position; i <= scrRange.last.position; i++) {
				elems.get(i).setRange(scrRange);
			}

			// 後方時間・スクラッチ時間を計算する
			// この時間は、スクラッチ範囲の後方時間(制限あり)と実際にスクラッチを操作する時間の合計値とする。
			scrRange.behindTime = RatingElement.timeDelta(elems, scrRange.behind, scrRange.first.position);
			scrRange.scratchingTime = RatingElement.timeDelta(elems, scrRange.first.position, scrRange.last.position);
			scrTotalTime += (Math.min(scrRange.behindTime, mConfig.minBehindTime) + scrRange.scratchingTime);
		}

		// スクラッチ操作難易度評価点を集計する
		var scrDifficulty = new ScoreSummarizer(mConfig.satulateDifficulty);
		for (var scrRange = scrRangeTop; scrRange != null; scrRange = scrRange.next) {
			scrRange.evaluations().forEach(e -> scrDifficulty.put(elems.get(e.position).getTime(), e.score));
		}

		// タイムラインの評価結果を設定する
		tlScore.elems = elems;
		tlScore.numSingle = numSingle;
		tlScore.numPulse = numPulse;
		tlScore.numRtrip = numRtrip;
		tlScore.numSpin = numSpin;
		tlScore.numScratch = cxt.chart.getNoteCount(cxt.layout.get(mDevScr));
		tlScore.numSwitch = mDevSws.stream().mapToInt(dev -> cxt.chart.getNoteCount(cxt.layout.get(dev))).sum();
		tlScore.playTime = RatingElement.computeTimeOfPointRange(elems);
		tlScore.scratchingTime = scrTotalTime;
		tlScore.scrDifficulty = scrDifficulty;

		// 最終評価点とSCRATCH値を算出する
		tlScore.scoreDifficulty = scrDifficulty.summary();
		tlScore.scoreChangeRate = 1.0 + mConfig.ipfnChangeRate.compute(tlScore.changePerSec());
		tlScore.scoreTimeRate = mConfig.ipfnTimeRate.compute(tlScore.scratchingRate());
		var timePtRange = RatingElement.computeTimeOfPointRange(elems);
		var scoreOrg = tlScore.scoreDifficulty * tlScore.scoreChangeRate * tlScore.scoreTimeRate;
		tlScore.scoreOrg = computeRatingValue(scoreOrg, timePtRange, 1.0);
		tlScore.score = (int)mConfig.ipfnScratch.compute(tlScore.scoreOrg);

		return tlScore;
	}

	/** {@inheritDoc} */
	@Override
	protected void dumpSummary(DsContext cxt, double org, int rating, Object... values) {
		var sb = new StringBuilder();
		var tlScore = (Scratch.Score)values[0];
		sb.append(cxt.header.getComment());
		sb.append("\t").append(String.format("%s %s", cxt.header.getTitle(), cxt.header.getSubTitle()).strip());
		sb.append("\t").append(tlScore.numRange());
		sb.append("\t").append(tlScore.numSingle);
		sb.append("\t").append(tlScore.numPulse);
		sb.append("\t").append(tlScore.numRtrip);
		sb.append("\t").append(tlScore.numSpin);
		sb.append(String.format("\t%.2f", tlScore.changePerSec()));
		sb.append("\t").append(tlScore.numNotes());
		sb.append("\t").append(tlScore.numSwitch);
		sb.append("\t").append(tlScore.numScratch);
		sb.append(String.format("\t%.4f", tlScore.numScratchRate()));
		sb.append(String.format("\t%.2f", tlScore.playTime));
		sb.append(String.format("\t%.2f", tlScore.scratchingTime));
		sb.append(String.format("\t%.4f", tlScore.scratchingRate()));
		sb.append(String.format("\t%.4f", tlScore.scoreDifficulty));
		sb.append(String.format("\t%.4f", tlScore.scoreChangeRate));
		sb.append(String.format("\t%.4f", tlScore.scoreTimeRate));
		sb.append(String.format("\t%.4f", org));
		sb.append(String.format("\t%.2f", getRatingType().toValue(rating)));
		Ds.debug(sb);
	}

	/** {@inheritDoc} */
	@Override
	protected void dumpDetail(DsContext cxt, double org, int rating, List<? extends RatingElement> elems,
			Object... values) {
		super.dumpDetail(cxt, org, rating, elems, values);
		var tlScore = (Scratch.Score)values[0];
		Ds.debug("RANGE(Single=%d, Pulse=%d, R-Trip=%d, Spin=%d), NOTES(Total=%d, Scratch=%d(%.2f%%), Switch=%d), TIME(Total=%.2fs, Scratching=%.2fs(%.2f%%))",
				tlScore.numSingle, tlScore.numPulse, tlScore.numRtrip, tlScore.numSpin,
				tlScore.numNotes(), tlScore.numScratch, tlScore.numScratchRatePer(), tlScore.numSwitch,
				tlScore.playTime, tlScore.scratchingTime, tlScore.scratchingRatePer());
		Ds.debug("SCORE-Difficulty=(%s, Summary=%.4f)", tlScore.scrDifficulty, tlScore.scrDifficulty.summary());
	}

	/**
	 * 分析用データのセットアップ
	 * @param cxt コンテキスト
	 * @param lane 分析対象レーン
	 */
	private void setupContext(DsContext cxt, BeMusicLane lane) {
		mDevSws = BeMusicDevice.getSwitches(lane);
		mDevScr = BeMusicDevice.getScratch(lane);
		mLnMode = cxt.header.getLnMode();
		mSpinTurn = (mLnMode != BeMusicLongNoteMode.LN);
		mSpinFull = (mLnMode == BeMusicLongNoteMode.HCN);
		mMapping = Scratch.Mapping.fromLane(lane, cxt.dpMode);
		mConfig = ScratchConfig.getInstance();
	}

	/**
	 * 分析用データのクリア
	 */
	private void clearContext() {
		mDevSws = null;
		mDevScr = null;
		mLnMode = null;
		mSpinTurn = false;
		mSpinFull = false;
		mMapping = null;
		mConfig = null;
	}

	/**
	 * 次のスクラッチ操作(短押し・長押し開始)の検索
	 * @param elems 要素リスト
	 * @param start 検索開始位置
	 * @return 次のスクラッチ操作のインデックス、なければ要素リストの数
	 */
	private int nextScratchStart(List<ScratchElement> elems, int start) {
		return nextScratch(elems, start, BeMusicNoteType::hasDownAction);
	}

	/**
	 * 次のスクラッチ操作(長押し終了)の検索
	 * @param elems 要素リスト
	 * @param start 検索開始位置
	 * @return 次のスクラッチ操作のインデックス、なければ要素リストの数
	 */
	private int nextScratchEnd(List<ScratchElement> elems, int start) {
		// フェイルセーフとして、押下操作も検索対象とする
		return nextScratch(elems, start, t -> t.isLongNoteTail() || t.hasDownAction());
	}

	/**
	 * 次のスクラッチ操作の検索
	 * @param elems 要素リストの
	 * @param start 検索開始位置(この位置の1つ次の位置から検索を行う)
	 * @param tester 「次のスクラッチ操作」を判定する判定関数
	 * @return 次のスクラッチ操作のインデックス、なければ要素リストの数
	 */
	private int nextScratch(List<ScratchElement> elems, int start, Predicate<BeMusicNoteType> tester) {
		var countElem = elems.size();
		for (var index = start + 1; index < countElem; index++) {
			var elem = elems.get(index);
			if (tester.test(elem.getNoteType(mDevScr))) {
				return index;
			}
		}
		return countElem;
	}

	/**
	 * 回転操作の評価処理
	 * @param range スクラッチ範囲(回転操作)
	 * @param direction 開始位置の操作方向
	 * @param elems 要素リスト
	 */
	private void evaluateSpinning(ScratchRange.Spinning range, Scratch.Direction direction, List<ScratchElement> elems) {
		// 回転操作開始位置の状態を解析する
		var behindTime = RatingElement.timeDelta(elems, range.behind, range.first.position);
		evaluateBeat(range.first, behindTime, direction, elems);

		// 回転操作終了位置までのノート密度を計算する
		var swCount = 0;
		var posEnd = range.last.position;
		for (var i = range.first.position; i <= posEnd; i++) {
			swCount += elems.get(i).getPoint().getNoteCount();
		}
		range.density = swCount / RatingElement.timeDelta(elems, range.first.position, posEnd);

		range.last.direction = range.turn ? direction.reverse() : direction;
		// TODO 回転操作終点の評価点を計算する
	}

	/**
	 * スクラッチ操作の評価処理
	 * @param eval 評価データ
	 * @param behindTime 後方楽曲位置の評価範囲(秒指定)
	 * @param direction スクラッチ操作の操作方向
	 * @param elems 要素リスト
	 */
	private void evaluateBeat(ScratchEvaluation eval, double behindTime, Scratch.Direction direction,
			List<ScratchElement> elems) {
		// スクラッチと同じ楽曲位置の状態を解析する
		var scrElem = elems.get(eval.position);
		parseStatus(eval, false, scrElem);

		// スクラッチよりも後方の楽曲位置の状態を解析する
		var endTime = scrElem.getTime() - Math.min(behindTime, mConfig.maxPulseTime);
		for (var i = eval.position - 1; (i >= 0) && (elems.get(i).getTime() > endTime); i--) {
			var isBehind = (RatingElement.timeDelta(elems, i, eval.position) > mConfig.acceptableDelta);
			parseStatus(eval, isBehind, elems.get(i));
		}

		// 評価点を集計する
		var score = 1.0 + direction.addScore.applyAsDouble(mConfig);
		for (var area : Scratch.Area.VALUES) {
			var addedAreaCur = false;
			var addedAreaBehind = false;
			for (var note : Scratch.Note.VALUES) {
				// スクラッチと同じ楽曲位置の状態分を加点する
				if (eval.getStatus(area, note)) {
					score += note.addScore.applyAsDouble(mConfig);
					score += addedAreaCur ? 0.0 : area.addScore.applyAsDouble(mConfig);
					addedAreaCur = true;
				}
				// スクラッチよりも後方の楽曲位置の状態分を加点する
				if (eval.getBehindStatus(area, note)) {
					score += note.addScore.applyAsDouble(mConfig);
					score += addedAreaBehind ? 0.0 : (area.addScore.applyAsDouble(mConfig) * mConfig.behindScoreRate);
					addedAreaBehind = true;
				}
			}
		}

		// 評価結果を設定する
		var timeAdjust = ((eval.position == 0) && (behindTime == 0.0)) ? mConfig.maxPulseTime : behindTime;
		eval.direction = direction;
		eval.score = score * (1.0 + mConfig.ipfnDifficultyBoost.compute(timeAdjust));
	}

	/**
	 * スイッチ操作の状態解析
	 * @param eval 解析対象の評価データ
	 * @param behind 後方楽曲位置の解析かどうか
	 * @param elem 要素データ
	 */
	private void parseStatus(ScratchEvaluation eval, boolean behind, ScratchElement elem) {
		for (var devSw : mDevSws) {
			var note = Scratch.Note.fromNoteType(elem.getNoteType(devSw));
			if (note != null) {
				var area = mMapping.areaMap.get(devSw);
				if (behind) {
					eval.setBehindStatus(area, note, true);
				} else {
					eval.setStatus(area, note, true);
				}
			}
		}
	}
}
