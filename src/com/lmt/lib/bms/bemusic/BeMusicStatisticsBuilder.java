package com.lmt.lib.bms.bemusic;

import static com.lmt.lib.bms.bemusic.Assertion.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 譜面統計情報を集計するためのビルダーです。
 *
 * <p>譜面統計情報は、入力となる楽曲の情報(ヘッダ情報と譜面データ)と、複数の集計オプションから成ります。
 * 当クラスでは入力楽曲と集計オプションを取りまとめ、集計実行までの手続きを一元化する役割を担います。</p>
 *
 * <p>譜面統計情報の集計によってどのような情報が集計されるかについては{@link BeMusicStatistics}を参照してください。</p>
 *
 * @see BeMusicStatistics
 * @see BeMusicTimeSpan
 */
public class BeMusicStatisticsBuilder {
	/** 楽曲のヘッダ情報 */
	private BeMusicHeader mHeader;
	/** 譜面データ */
	private BeMusicScore mScore;
	/** 期間統計情報の長さ */
	private double mLength;
	/** ノートレイアウト */
	private BeMusicNoteLayout mLayout;

	/** 集計済みフラグ */
	private boolean mUsed = false;

	/**
	 * 譜面統計情報ビルダーオブジェクトを構築します。
	 * <p>指定された楽曲のヘッダ情報・譜面データが譜面統計情報の入力データとなります。
	 * その他の譜面統計情報集計オプションについては当クラスの各種Setterメソッドの解説を参照してください。</p>
	 * <p>ヘッダ情報の値は譜面統計情報の集計の際に必要に応じて参照されます。ヘッダ情報と譜面データが同一楽曲から
	 * 生成されたものでない場合、譜面統計情報は予期しない集計を行うことになりますので注意してください。</p>
	 * @param header ヘッダ情報
	 * @param score 譜面データ
	 * @exception NullPointerException headerまたはscoreがnull
	 */
	public BeMusicStatisticsBuilder(BeMusicHeader header, BeMusicScore score) {
		assertArgNotNull(header, "header");
		assertArgNotNull(score, "score");
		mHeader = header;
		mScore = score;
		mLength = BeMusicTimeSpan.RECOMMENDED_SPAN;
		mLayout = BeMusicNoteLayout.SP_REGULAR;
	}

	/**
	 * 期間統計情報の長さを秒単位で指定します。
	 * <p>この値は{@link BeMusicTimeSpan#MIN_SPAN}～{@link BeMusicTimeSpan#MAX_SPAN}の範囲で指定してください。
	 * 指定を省略した場合、{@link BeMusicTimeSpan#RECOMMENDED_SPAN}が使用されます。</p>
	 * <p>期間統計情報の詳細については{@link BeMusicTimeSpan}を参照してください。</p>
	 * @param length 期間統計情報の長さ
	 * @return このオブジェクトのインスタンス
	 * @see BeMusicTimeSpan
	 */
	public final BeMusicStatisticsBuilder setSpanLength(double length) {
		mLength = length;
		return this;
	}

	/**
	 * ノートレイアウトを指定します。
	 * <p>譜面統計情報は当メソッドで指定されたノートレイアウトで集計が行われます。
	 * ノートレイアウト、または譜面統計情報の集計対象の楽曲がシングルプレーの場合、シングルプレー用譜面として
	 * 譜面統計情報が集計されます。ダブルプレー用譜面として集計されるのは両者がダブルプレーであった場合のみです。</p>
	 * <p>指定を省略した場合、{@link BeMusicNoteLayout#SP_REGULAR}が使用されます。</p>
	 * @param layout ノートレイアウト
	 * @return このオブジェクトのインスタンス
	 * @see BeMusicNoteLayout
	 */
	public final BeMusicStatisticsBuilder setNoteLayout(BeMusicNoteLayout layout) {
		mLayout = layout;
		return this;
	}

	/**
	 * 指定された楽曲とオプションで譜面統計情報の集計を行います。
	 * <p>各種Setterメソッドで指定した集計オプションに誤りがある場合、集計は行われずに例外がスローされます。</p>
	 * <p>一度集計を行ったビルダーで再度集計を行うことはできません。異なるオプションで集計したい場合は
	 * ビルダーの新しいインスタンスを生成し、そのビルダーで集計を行ってください。</p>
	 * <p>譜面統計情報の詳しい内容については{@link BeMusicStatistics}を参照してください。</p>
	 * @return 譜面統計情報の集計結果
	 * @exception IllegalStateException 集計を行ったビルダーで再度集計を行おうとした
	 * @exception IllegalStateException 期間統計情報の長さが{@link BeMusicTimeSpan#MIN_SPAN}未満または{@link BeMusicTimeSpan#MAX_SPAN}超過
	 * @exception IllegalStateException ノートレイアウトがnull
	 * @see BeMusicStatistics
	 */
	public final BeMusicStatistics statistics() {
		// アサーション
		assertState(!mUsed, "This builder is already used");
		assertState((mLength >= BeMusicTimeSpan.MIN_SPAN) && (mLength <= BeMusicTimeSpan.MAX_SPAN),
				"Parameter 'Length' is out of range. length=%.3g", mLength);
		assertState(mLayout != null, "Parameter 'Layout' is null");

		// 統計情報集計開始
		var statistics = doStat();

		// ビルダー使用終了のため保有オブジェクトを全て解放する
		mUsed = true;
		mHeader = null;
		mScore = null;
		mLayout = null;

		return statistics;
	}

	/**
	 * 譜面統計情報の集計メイン処理
	 * @return 譜面統計情報
	 */
	private BeMusicStatistics doStat() {
		var curTime = 0.0;
		var termTime = mScore.getPlayTime();
		var advTime = mLength / 2.0;
		var prevTs = (BeMusicTimeSpan)null;
		var curIdx = 0;
		var isSp = mHeader.getPlayer().isSinglePlay() || mLayout.isSinglePlayLayout();
		var gazePoints = new double[2];
		var viewWidths = new double[2];
		var visuals = new double[2];
		var timeSpans = new ArrayList<BeMusicTimeSpan>();
		while (curTime < termTime) {
			// 統計情報収集範囲を計算する
			var endTime = curTime + mLength;
			var firstIdx = mScore.ceilPointOf(curTime);
			var lastIdx = mScore.floorPointOf(Math.nextDown(endTime));

			// 注視点と視野幅を計算する
			Arrays.fill(gazePoints, 0.0);
			Arrays.fill(viewWidths, 0.0);
			if (isSp) {
				// シングルプレー用処理
				computeVisual(firstIdx, lastIdx, BeMusicDevice.orderedBySpLeftScratchList(), visuals);
				gazePoints[0] = visuals[0];
				viewWidths[0] = visuals[1];
				computeVisual(firstIdx, lastIdx, BeMusicDevice.orderedBySpRightScratchList(), visuals);
				gazePoints[1] = visuals[0];
				viewWidths[1] = visuals[1];
			} else {
				// ダブルプレー用処理
				computeVisual(firstIdx, lastIdx, BeMusicDevice.orderedByDpList(), visuals);
				gazePoints[0] = visuals[0];
				gazePoints[1] = 0.0;
				viewWidths[0] = visuals[1];
				viewWidths[1] = 0.0;
			}

			// 単純カウント項目を集計する
			var noteCount = 0;
			var lnCount = 0;
			var lmCount = 0;
			for (var i = firstIdx; i <= lastIdx; i++) {
				var pt = mScore.getPoint(i);
				noteCount += pt.getNoteCount();
				lnCount += pt.getLongNoteCount();
				lmCount += pt.getLandmineCount();
			}

			// 時間範囲データを生成する
			var ts = new BeMusicTimeSpan();
			ts.setIndex(curIdx);
			ts.setBeginTime(curTime);
			ts.setEndTime(endTime);
			if (firstIdx <= lastIdx) {
				// 時間範囲内に楽曲位置情報あり
				ts.setFirstPointIndex(firstIdx);
				ts.setLastPointIndex(lastIdx);
			} else {
				// 時間範囲に楽曲位置情報が1件もない
				ts.setFirstPointIndex(-1);
				ts.setLastPointIndex(-1);
			}
			ts.setGazePoint(gazePoints[0], gazePoints[1]);
			ts.setViewWidth(viewWidths[0], viewWidths[1]);
			ts.setNoteCount(noteCount);
			ts.setLongNoteCount(lnCount);
			ts.setLandmineCount(lmCount);
			if (prevTs == null) {
				// 先頭の時間範囲データ
				ts.setPrevious(ts);
			} else {
				// 2件目以降の時間範囲データ
				ts.setPrevious(prevTs);
				prevTs.setNext(ts);
			}
			timeSpans.add(ts);

			// 次の楽曲位置を計算する準備
			curTime += advTime;
			curIdx++;
			prevTs = ts;
		}

		// 最終要素の末端処理
		if (!timeSpans.isEmpty()) {
			var lastTs = timeSpans.get(timeSpans.size() - 1);
			lastTs.setNext(lastTs);
		}

		// 注視点と視野幅の変動係数を計算する
		for (var ts : timeSpans) {
			if (ts.hasVisualEffect()) {
				// 前後の期間からの変動量の平均値を変動係数とする(0～1)
				var prev = ts.getPrevious();
				var next = ts.getNext();
				var gpL = ts.getGazePoint();
				var gpR = ts.getGazePointR();
				var vwL = ts.getViewWidth();
				var vwR = ts.getViewWidthR();
				var gazeL = (Math.abs(gpL - prev.getGazePoint()) + Math.abs(gpL - next.getGazePoint())) / 2.0;
				var gazeR = (Math.abs(gpR - prev.getGazePointR()) + Math.abs(gpR - next.getGazePointR())) / 2.0;
				var viewL = (Math.abs(vwL - prev.getViewWidth()) + Math.abs(vwL - next.getViewWidth())) / 2.0;
				var viewR = (Math.abs(vwR - prev.getViewWidthR()) + Math.abs(vwR - next.getViewWidthR())) / 2.0;
				ts.setGazeSwingley(gazeL, gazeR);
				ts.setViewSwingley(viewL, viewR);
			} else {
				// 視覚効果のない期間は計算の対象外とする
				ts.setGazeSwingley(0.0, 0.0);
				ts.setViewSwingley(0.0, 0.0);
			}
		}

		// 総合統計情報を計算する
		return computeSummary(timeSpans);
	}

	/**
	 * 指定楽曲位置での注視点と視野幅計算
	 * @param firstIndex 計算開始インデックス
	 * @param lastIndex 計算終了インデックス(この値を含む)
	 * @param deviceList 計算対象の入力デバイスリスト(見た目上の順に配置された状態)
	 * @param results 計算結果([0]=注視点, [1]=視野幅)
	 */
	private void computeVisual(int firstIndex, int lastIndex, List<BeMusicDevice> deviceList, double[] results) {
		// 視覚効果のある両端のノート位置を特定する
		var deviceCount = deviceList.size();
		var minWidth = (deviceCount > 8) ? 0.03 : 0.07;
		var maxWidth = (double)(deviceCount - 1);
		var halfWidth = maxWidth / 2.0;
		var hasVeCount = 0.0;
		var gazePointSum = 0.0;
		var viewWidthSum = 0.0;
		for (var i = firstIndex; i <= lastIndex; i++) {
			// 同一楽曲位置単位で注視点と視野幅を算出する
			var pt = mScore.getPoint(i);
			var hasVe = false;
			var left = 0.0;
			var right = 0.0;
			for (var j = 0; j < deviceCount; j++) {
				// ノートレイアウト変更後の視覚効果で計算を行う
				var actualDevice = mLayout.get(deviceList.get(j));
				var visualEffect = pt.getNoteType(actualDevice).hasVisualEffect();
				if (visualEffect) {
					left = hasVe ? left : j;
					right = j;
					hasVe = true;
				}
			}
			if (hasVe) {
				var gazePoint = (left + ((right - left) / 2.0) - halfWidth) / halfWidth;
				var viewWidth = Math.max((right - left) / maxWidth, minWidth);
				gazePointSum += gazePoint;
				viewWidthSum += viewWidth;
				hasVeCount += 1.0;
			}
		}

		// 注視点と視野幅を計算する
		Arrays.fill(results, 0.0);
		if (hasVeCount > 0) {
			// 期間内に視覚効果のあるノートがある場合
			results[0] = gazePointSum / hasVeCount;
			results[1] = viewWidthSum / hasVeCount;
		}
	}

	/**
	 * 譜面統計情報のサマリ計算
	 * @param timeSpans 期間統計情報リスト
	 * @return 譜面統計情報
	 */
	private BeMusicStatistics computeSummary(List<BeMusicTimeSpan> timeSpans) {
		// 収集された時間統計情報のサマリを行う
		var spanCount = (double)timeSpans.size();
		var playableCount = 0.0;
		var veCount = 0.0;
		var notesMax = 0.0;
		var notesSum = 0.0;
		var gazePointSum = new double[] { 0.0, 0.0 };
		var viewWidthSum = new double[] { 0.0, 0.0 };
		var gazeSwingleySum = new double[] { 0.0, 0.0 };
		var viewSwingleySum = new double[] { 0.0, 0.0 };
		for (var timeSpan : timeSpans) {
			playableCount += (timeSpan.hasCountNote()) ? 1 : 0;
			veCount += (timeSpan.hasVisualEffect()) ? 1 : 0;
			notesMax = Math.max(timeSpan.getNoteCount(), notesMax);
			notesSum += timeSpan.getNoteCount();
			gazePointSum[0] += timeSpan.getGazePoint();
			gazePointSum[1] += timeSpan.getGazePointR();
			viewWidthSum[0] += timeSpan.getViewWidth();
			viewWidthSum[1] += timeSpan.getViewWidthR();
			gazeSwingleySum[0] += timeSpan.getGazeSwingley();
			gazeSwingleySum[1] += timeSpan.getGazeSwingleyR();
			viewSwingleySum[0] += timeSpan.getViewSwingley();
			viewSwingleySum[1] += timeSpan.getViewSwingleyR();
		}

		// サマリから総合統計情報を生成する
		var stat = new BeMusicStatistics();
		stat.setTimeSpanList(timeSpans);
		stat.setSpanLength(mLength);
		stat.setNoteLayout(mLayout);
		stat.setAverageDensity((playableCount > 0.0) ? (notesSum / mLength) / playableCount : 0.0);
		stat.setMaxDensity(notesMax / mLength);
		if (timeSpans.isEmpty()) {
			// 時間範囲データなし
			stat.setNoPlayingRatio(1.0);
			stat.setNoVisualEffectRatio(1.0);
		} else {
			// 時間範囲データあり
			stat.setNoPlayingRatio((spanCount - playableCount) / spanCount);
			stat.setNoVisualEffectRatio((spanCount - veCount) / spanCount);
		}
		if (veCount == 0) {
			// 視覚効果のあるノートなし
			stat.setAverageGazePoint(0.0, 0.0);
			stat.setAverageViewWidth(0.0, 0.0);
			stat.setAverageGazeSwingley(0.0, 0.0);
			stat.setAverageViewSwingley(0.0, 0.0);
		} else {
			// 視覚効果のあるノートあり
			stat.setAverageGazePoint(gazePointSum[0] / veCount, gazePointSum[1] / veCount);
			stat.setAverageViewWidth(viewWidthSum[0] / veCount, viewWidthSum[1] / veCount);
			stat.setAverageGazeSwingley(gazeSwingleySum[0] / veCount, gazeSwingleySum[1] / veCount);
			stat.setAverageViewSwingley(viewSwingleySum[0] / veCount, viewSwingleySum[1] / veCount);
		}

		return stat;
	}
}
