package com.lmt.lib.bms.internal.deltasystem;

import java.util.List;
import java.util.function.Predicate;

import com.lmt.lib.bms.bemusic.BeMusicLane;
import com.lmt.lib.bms.bemusic.BeMusicNoteType;
import com.lmt.lib.bms.bemusic.BeMusicPoint;
import com.lmt.lib.bms.bemusic.BeMusicRatingType;

/**
 * 譜面傾向「SCRATCH」の分析処理クラス
 */
public class ScratchAnalyzer extends RatingAnalyzer {
	/**
	 * コンストラクタ
	 */
	public ScratchAnalyzer() {
		super(BeMusicRatingType.SCRATCH);
	}

	/** {@inheritDoc} */
	@Override
	protected void compute(DsContext ctx) {
		// 動作のある楽曲位置を特定し、その楽曲位置に対する要素のリストを構築する
		// (BGAやBPM変化、譜面停止のみの楽曲位置等、入力デバイス操作を伴わない楽曲位置を除外したリスト)
		var elems = Ds.listElements(ctx, ScratchElement::new, BeMusicPoint::hasMovementNote);
		if (elems.isEmpty()) {
			ctx.stat.setRating(getRatingType(), 0);
			return;
		}

		// プレーモードごとの算出処理を実行する
		var config = ScratchConfig.getInstance();
		var scratch = -1;
		if (ctx.dpMode) {
			// ダブルプレーの場合、左右レーン両方を評価する
			var lsp = computeLane(new Scratch.Context(ctx, BeMusicLane.PRIMARY), elems);
			var lss = computeLane(new Scratch.Context(ctx, BeMusicLane.SECONDARY), elems);

			// 最終評価点が片側に偏った譜面での最終評価点の調整を行う
			var influences = Ds.adjustInfluenceForDp(
					lsp.scratchOrg,
					lss.scratchOrg,
					config.dpInfluenceScoreHigh,
					config.dpInfluenceScoreLow,
					config.dpAdjustInfluenceRate,
					config.dpAdjustInfluenceMaxStrength);

			// 最終評価点を合成し、レーティング値を算出する
			var scOrg = Ds.mergeValue(lsp.scratchOrg, lss.scratchOrg, influences[0], influences[1]);
			scratch = (int)config.ipfnScratch.compute(scOrg);
			debugOut(ctx, scratch, elems, lsp, lss);
		} else {
			// シングルプレーの場合、主レーンのみレーティング値を算出する
			var ls = computeLane(new Scratch.Context(ctx, BeMusicLane.PRIMARY), elems);
			scratch = (int)config.ipfnScratch.compute(ls.scratchOrg);
			debugOut(ctx, scratch, elems, ls);
		}

		ctx.stat.setRating(getRatingType(), scratch);
	}

	/**
	 * 指定レーンの評価
	 * @param ctx SCRATCH用コンテキスト
	 * @param elems レーティング要素リスト
	 * @return レーン評価情報
	 */
	private Scratch.Score computeLane(Scratch.Context ctx, List<ScratchElement> elems) {
		// スクラッチが全く存在しない譜面では評価を実行しない
		var dsCtx = ctx.ctx;
		var ls = new Scratch.Score();
		if (dsCtx.chart.getNoteCount(ctx.devScr) == 0) {
			ls.playTime = dsCtx.chart.getPlayTime();
			return ls;
		}

		// 最初のスクラッチ位置から、スクラッチの種別と範囲を特定する
		// この処理ブロックでは、スクラッチの範囲と種別の判定を行う専用の処理とする。
		// 判定された範囲に対する評価は別途、後続の処理で行うものとする。
		// ※スクラッチ有無はチェック済みのため、以降の処理は必ずスクラッチが存在する前提として処理する
		var countElem = elems.size();
		var idxBehindFirst = -1;
		var idxFirst = 0;
		var numSingle = 0;
		var numPulse = 0;
		var numRtrip = 0;
		var numSpin = 0;
		var scrRangeTop = (ScratchRange)null;
		var scrRangePrev = (ScratchRange)null;
		while ((idxFirst = nextScratchStart(ctx, elems, idxBehindFirst)) < countElem) {
			// スクラッチの範囲・種類を決定する
			var scrRange = (ScratchRange)null;
			if (elems.get(idxFirst).getNoteType(ctx.devScr) == BeMusicNoteType.LONG_ON) {
				// 先頭SCRが長押し開始の場合、回転操作となる
				var idxNext = -1;
				var idxLast = nextScratchEnd(ctx, elems, idxFirst);
				if (idxLast >= countElem) {
					// 末尾SCRが見つからない場合は末尾楽曲位置までの回転操作とする(フェイルセーフ)
					scrRange = new ScratchRange.Spinning(idxFirst, (countElem - 1), false, false);
					numSpin++;
				} else if (!elems.get(idxLast).getNoteType(ctx.devScr).isLongNoteTail()) {
					// 末尾スクラッチが長押し終了ではない場合は末尾SCRまでの回転操作とする(フェイルセーフ)
					scrRange = new ScratchRange.Spinning(idxFirst, idxLast, false, false);
					numSpin++;
				} else if (((idxNext = nextScratchStart(ctx, elems, idxLast)) >= countElem) || (ctx.spinTurn) ||
						(Ds.timeDelta(elems, idxLast, idxNext) > ctx.config.maxSpinDockTime) ||
						(elems.get(idxNext).getNoteType(ctx.devScr) != BeMusicNoteType.BEAT)) {
					// 終端の次SCRなし、LN以外、終端～次SCRの時間が規定時間以上、次SCRが短押し以外のいずれかの場合
					if (Ds.timeDelta(elems, idxFirst, idxLast) >= ctx.config.minSpinTime) {
						// 回転操作時間が規定時間以上の場合は回転操作とする
						scrRange = new ScratchRange.Spinning(idxFirst, idxLast, ctx.spinTurn, ctx.spinFull);
						numSpin++;
					} else {
						// 回転操作時間が規定時間未満の場合は往復操作とする
						scrRange = new ScratchRange.RoundTrip(idxFirst, idxLast);
						numRtrip++;
					}
				} else {
					// この分岐に入るということは、LNモードで疑似的な回転操作の配置が行われていることを示す。
					// その場合の回転操作の末尾は長押し終了までではなく、その次の短押しSCRまでとする。
					if (Ds.timeDelta(elems, idxFirst, idxNext) >= ctx.config.minSpinTime) {
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
				var idxNext = nextScratchStart(ctx, elems, idxFirst);
				var pulseTime = 0.0;
				if (idxNext >= countElem) {
					// 次SCRがない場合は単体操作とする
					scrRange = new ScratchRange.Single(idxFirst);
					numSingle++;
				} else if ((elems.get(idxNext).getNoteType(ctx.devScr) != BeMusicNoteType.BEAT) ||
						((pulseTime = Ds.timeDelta(elems, idxFirst, idxNext)) > ctx.config.maxPulseTime)) {
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
						if (((idxNext = nextScratchStart(ctx, elems, idxNext)) >= countElem)) {
							break;  // 次SCRがない
						}
						if (elems.get(idxNext).getNoteType(ctx.devScr) != BeMusicNoteType.BEAT) {
							break;  // 次SCRが短押しではない
						}
						var timeDelta = Ds.timeDelta(elems, idxPrev, idxNext);
						if (Math.abs(pulseTime - timeDelta) > ctx.config.acceptableDelta) {
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
			var behindTime = Ds.timeDelta(elems, scrRange.behind, scrRange.first.position);
			if (behindTime >= ctx.config.directionResetTime) {
				direction = Scratch.Direction.OUTER;
			}

			// スクラッチ種別ごとの評価処理
			var rangeType = scrRange.type;
			if (rangeType == Scratch.Type.SINGLE) {
				// 単体操作
				var range = (ScratchRange.Single)scrRange;
				var behind = Ds.timeDelta(elems, range.behind, range.first.position);
				evaluateBeat(ctx, range.first, behind, direction, elems);
				direction = direction.reverse();
			} else if (rangeType == Scratch.Type.PULSE) {
				// 連続操作
				var range = (ScratchRange.Pulse)scrRange;
				for (var eval : range.scratches.values()) {
					evaluateBeat(ctx, eval, range.pulseTime, direction, elems);
					direction = direction.reverse();
				}
			} else if (rangeType == Scratch.Type.ROUND_TRIP) {
				// 往復操作
				var range = (ScratchRange.RoundTrip)scrRange;
				var behind1 = Ds.timeDelta(elems, range.behind, range.first.position);
				evaluateBeat(ctx, range.first, behind1, direction, elems);
				var behind2 = Ds.timeDelta(elems, range.first.position, range.last.position);
				evaluateBeat(ctx, range.last, behind2, direction.reverse(), elems);
			} else if (rangeType == Scratch.Type.SPINNING) {
				// 回転操作
				var range = (ScratchRange.Spinning)scrRange;
				evaluateSpinning(ctx, range, direction, elems);
				direction = range.turn ? direction : direction.reverse();
			} else {
				// Do nothing
			}

			// 要素リストにスクラッチ範囲のデータを設定する
			for (var i = scrRange.first.position; i <= scrRange.last.position; i++) {
				elems.get(i).setData(ctx.lane, scrRange);
			}

			// 後方時間・スクラッチ時間を計算する
			// この時間は、スクラッチ範囲の後方時間(制限あり)と実際にスクラッチを操作する時間の合計値とする。
			scrRange.behindTime = Ds.timeDelta(elems, scrRange.behind, scrRange.first.position);
			scrRange.scratchingTime = Ds.timeDelta(elems, scrRange.first.position, scrRange.last.position);
			scrTotalTime += (Math.min(scrRange.behindTime, ctx.config.minBehindTime) + scrRange.scratchingTime);
		}

		// スクラッチ操作難易度評価点を集計する
		var scrDifficulty = new ScoreSummarizer(ctx.config.satulateDifficulty);
		for (var scrRange = scrRangeTop; scrRange != null; scrRange = scrRange.next) {
			scrRange.evaluations().forEach(e -> scrDifficulty.put(elems.get(e.position).getTime(), e.score));
		}

		// 評価結果を設定する
		ls.numSingle = numSingle;
		ls.numPulse = numPulse;
		ls.numRtrip = numRtrip;
		ls.numSpin = numSpin;
		ls.numScratch = dsCtx.chart.getNoteCount(dsCtx.layout.get(ctx.devScr));
		ls.numSwitch = ctx.devsNotScr.stream().mapToInt(dev -> dsCtx.chart.getNoteCount(dsCtx.layout.get(dev))).sum();
		ls.playTime = Ds.computeTimeOfPointRange(elems);
		ls.scratchingTime = scrTotalTime;
		ls.scrDifficulty = scrDifficulty;

		// 最終評価点とSCRATCH値を算出する
		ls.scoreDifficulty = scrDifficulty.summary();
		ls.scoreChangeRate = 1.0 + ctx.config.ipfnChangeRate.compute(ls.changePerSec());
		ls.scoreTimeRate = ctx.config.ipfnTimeRate.compute(ls.scratchingRate());
		ls.scratchOrg = ls.scoreDifficulty * ls.scoreChangeRate * ls.scoreTimeRate;
		var timePtRange = Ds.computeTimeOfPointRange(elems);
		ls.scratchOrg = Ds.computeRatingValue(ls.scratchOrg, timePtRange, 1.0);

		return ls;
	}

	/** {@inheritDoc} */
	@Override
	protected void dumpSummarySp(DsContext ctx, int rating, Object...values) {
		dumpSummaryCommon(ctx, rating, values);
	}

	@Override
	protected void dumpSummaryDp(DsContext ctx, int rating, Object...values) {
		dumpSummaryCommon(ctx, rating, values);
	}

	/**
	 * サマリされたデバッグ情報出力の共通処理
	 * @param ctx Delta System用コンテキスト
	 * @param rating レーティング値
	 * @param values レーンごとの評価情報
	 */
	private void dumpSummaryCommon(DsContext ctx, int rating, Object...values) {
		var sb = new StringBuilder();
		sb.append(ctx.header.getComment());
		sb.append("\t").append(String.format("%s %s", ctx.header.getTitle(), ctx.header.getSubTitle()).strip());
		for (var i = 0; i < values.length; i++) {
			var sc = (Scratch.Score)values[i];
			sb.append("\t").append(sc.numRange());
			sb.append("\t").append(sc.numSingle);
			sb.append("\t").append(sc.numPulse);
			sb.append("\t").append(sc.numRtrip);
			sb.append("\t").append(sc.numSpin);
			sb.append(String.format("\t%.2f", sc.changePerSec()));
			sb.append("\t").append(sc.numNotes());
			sb.append("\t").append(sc.numSwitch);
			sb.append("\t").append(sc.numScratch);
			sb.append(String.format("\t%.4f", sc.numScratchRate()));
			sb.append(String.format("\t%.2f", sc.playTime));
			sb.append(String.format("\t%.2f", sc.scratchingTime));
			sb.append(String.format("\t%.4f", sc.scratchingRate()));
			sb.append(String.format("\t%.4f", sc.scoreDifficulty));
			sb.append(String.format("\t%.4f", sc.scoreChangeRate));
			sb.append(String.format("\t%.4f", sc.scoreTimeRate));
			sb.append(String.format("\t%.4f", sc.scratchOrg));
		}
		sb.append(String.format("\t%.2f", getRatingType().toValue(rating)));
		Ds.debug(sb);
	}

	/** {@inheritDoc} */
	@Override
	protected void dumpDetailSp(DsContext ctx, int rating, List<? extends RatingElement> elems, Object...values) {
		super.dumpDetailSp(ctx, rating, elems, values);
		dumpDetailCommon(values);
	}

	/** {@inheritDoc} */
	@Override
	protected void dumpDetailDp(DsContext ctx, int rating, List<? extends RatingElement> elems, Object...values) {
		super.dumpDetailDp(ctx, rating, elems, values);
		dumpDetailCommon(values);
	}

	/**
	 * 詳細デバッグ情報出力の共通処理
	 * @param values レーンごとの評価情報
	 */
	private void dumpDetailCommon(Object...values) {
		for (var i = 0; i < values.length; i++) {
			var sc = (Scratch.Score)values[i];
			Ds.debug("--- %s ---", BeMusicLane.fromIndex(i));
			Ds.debug("RANGE(Single=%d, Pulse=%d, R-Trip=%d, Spin=%d), NOTES(Total=%d, Scratch=%d(%.2f%%), Switch=%d), TIME(Total=%.2fs, Scratching=%.2fs(%.2f%%))",
					sc.numSingle, sc.numPulse, sc.numRtrip, sc.numSpin,
					sc.numNotes(), sc.numScratch, sc.numScratchRatePer(), sc.numSwitch,
					sc.playTime, sc.scratchingTime, sc.scratchingRatePer());
			Ds.debug("SCORE-Difficulty=(%s, Summary=%.4f)", sc.scrDifficulty, sc.scrDifficulty.summary());
		}
	}

	/**
	 * 次のスクラッチ操作(短押し・長押し開始)の検索
	 * @param ctx SCRATCH用コンテキスト
	 * @param elems 要素リスト
	 * @param start 検索開始位置
	 * @return 次のスクラッチ操作のインデックス、なければ要素リストの数
	 */
	private int nextScratchStart(Scratch.Context ctx, List<ScratchElement> elems, int start) {
		return nextScratch(ctx, elems, start, BeMusicNoteType::hasDownAction);
	}

	/**
	 * 次のスクラッチ操作(長押し終了)の検索
	 * @param ctx SCRATCH用コンテキスト
	 * @param elems 要素リスト
	 * @param start 検索開始位置
	 * @return 次のスクラッチ操作のインデックス、なければ要素リストの数
	 */
	private int nextScratchEnd(Scratch.Context ctx, List<ScratchElement> elems, int start) {
		// フェイルセーフとして、押下操作も検索対象とする
		return nextScratch(ctx, elems, start, t -> t.isLongNoteTail() || t.hasDownAction());
	}

	/**
	 * 次のスクラッチ操作の検索
	 * @param ctx SCRATCH用コンテキスト
	 * @param elems 要素リスト
	 * @param start 検索開始位置(この位置の1つ次の位置から検索を行う)
	 * @param tester 「次のスクラッチ操作」を判定する判定関数
	 * @return 次のスクラッチ操作のインデックス、なければ要素リストの数
	 */
	private int nextScratch(Scratch.Context ctx, List<ScratchElement> elems, int start,
			Predicate<BeMusicNoteType> tester) {
		var countElem = elems.size();
		for (var index = start + 1; index < countElem; index++) {
			var elem = elems.get(index);
			if (tester.test(elem.getNoteType(ctx.devScr))) {
				return index;
			}
		}
		return countElem;
	}

	/**
	 * 回転操作の評価処理
	 * @param ctx SCRATCH用コンテキスト
	 * @param range スクラッチ範囲(回転操作)
	 * @param direction 開始位置の操作方向
	 * @param elems 要素リスト
	 */
	private void evaluateSpinning(Scratch.Context ctx, ScratchRange.Spinning range, Scratch.Direction direction,
			List<ScratchElement> elems) {
		// 回転操作開始位置の状態を解析する
		var behindTime = Ds.timeDelta(elems, range.behind, range.first.position);
		evaluateBeat(ctx, range.first, behindTime, direction, elems);

		// 回転操作終了位置までのノート密度を計算する
		var swCount = 0;
		var posEnd = range.last.position;
		for (var i = range.first.position; i <= posEnd; i++) {
			swCount += elems.get(i).getPoint().getNoteCount();
		}
		range.density = swCount / Ds.timeDelta(elems, range.first.position, posEnd);

		range.last.direction = range.turn ? direction.reverse() : direction;
		// TODO 回転操作終点の評価点を計算する
	}

	/**
	 * スクラッチ操作の評価処理
	 * @param ctx SCRATCH用コンテキスト
	 * @param eval 評価データ
	 * @param behindTime 後方楽曲位置の評価範囲(秒指定)
	 * @param direction スクラッチ操作の操作方向
	 * @param elems 要素リスト
	 */
	private void evaluateBeat(Scratch.Context ctx, ScratchEvaluation eval, double behindTime,
			Scratch.Direction direction, List<ScratchElement> elems) {
		// スクラッチと同じ楽曲位置の状態を解析する
		var scrElem = elems.get(eval.position);
		parseStatus(ctx, eval, false, scrElem);

		// スクラッチよりも後方の楽曲位置の状態を解析する
		var endTime = scrElem.getTime() - Math.min(behindTime, ctx.config.maxPulseTime);
		for (var i = eval.position - 1; (i >= 0) && (elems.get(i).getTime() > endTime); i--) {
			var isBehind = (Ds.timeDelta(elems, i, eval.position) > ctx.config.acceptableDelta);
			parseStatus(ctx, eval, isBehind, elems.get(i));
		}

		// 評価点を集計する
		var config = ctx.config;
		var score = 1.0 + direction.addScore.applyAsDouble(config);
		for (var area : Scratch.Area.VALUES) {
			var addedAreaCur = false;
			var addedAreaBehind = false;
			for (var note : Scratch.Note.VALUES) {
				// スクラッチと同じ楽曲位置の状態分を加点する
				if (eval.getStatus(area, note)) {
					score += note.addScore.applyAsDouble(config);
					score += addedAreaCur ? 0.0 : area.addScore.applyAsDouble(ctx.ctx);
					addedAreaCur = true;
				}
				// スクラッチよりも後方の楽曲位置の状態分を加点する
				if (eval.getBehindStatus(area, note)) {
					score += note.addScore.applyAsDouble(config);
					score += addedAreaBehind ? 0.0 : (area.addScore.applyAsDouble(ctx.ctx) * config.behindScoreRate);
					addedAreaBehind = true;
				}
			}
		}

		// 評価結果を設定する
		var timeAdjust = ((eval.position == 0) && (behindTime == 0.0)) ? config.maxPulseTime : behindTime;
		eval.direction = direction;
		eval.score = score * (1.0 + config.ipfnDifficultyBoost.compute(timeAdjust));
	}

	/**
	 * スイッチ操作の状態解析
	 * @param ctx SCRATCH用コンテキスト
	 * @param eval 解析対象の評価データ
	 * @param behind 後方楽曲位置の解析かどうか
	 * @param elem 要素データ
	 */
	private void parseStatus(Scratch.Context ctx, ScratchEvaluation eval, boolean behind, ScratchElement elem) {
		var countDevsNotScr = ctx.devsNotScr.size();
		for (var i = 0; i < countDevsNotScr; i++) {
			var devSw = ctx.devsNotScr.get(i);
			var note = Scratch.Note.fromNoteType(elem.getNoteType(devSw));
			if (note != null) {
				var area = ctx.mapping.areaMap.get(devSw);
				if (behind) {
					eval.setBehindStatus(area, note, true);
				} else {
					eval.setStatus(area, note, true);
				}
			}
		}
	}
}
