package com.lmt.lib.bms.internal.deltasystem;

import java.util.List;

import com.lmt.lib.bms.bemusic.BeMusicRatingType;

/**
 * レーティング値分析クラス
 *
 * <p>{@link BeMusicRatingType}に対応したレーティング値の分析処理を行うためのクラス。
 * 具体的な処理内容は当クラスを継承した先のクラスに記述する。</p>
 */
public abstract class RatingAnalyzer {
	/** レーティング種別 */
	private BeMusicRatingType mRatingType;

	/**
	 * コンストラクタ
	 * @param ratingType レーティング種別
	 */
	protected RatingAnalyzer(BeMusicRatingType ratingType) {
		mRatingType = ratingType;
	}

	/**
	 * レーティング値分析処理開始
	 * @param cxt Delta System用コンテキスト
	 */
	public void rating(DsContext cxt) {
		var debugMode = Ds.getDebugMode();
		try {
			if (debugMode.isStarterNecessary()) {
				Ds.debug("%s Start ----------", mRatingType);
			}

			compute(cxt);
		} finally {
			if (debugMode.isFinisherNecessary()) {
				Ds.debug("%s End ----------", mRatingType);
			}
		}
	}

	/**
	 * レーティング種別取得
	 * @return レーティング種別
	 */
	public final BeMusicRatingType getRatingType() {
		return mRatingType;
	}

	/**
	 * レーティング値の分析処理
	 * <p>Delta System用コンテキストに設定された楽曲の各種情報を入力にし、対応するレーティング種別のレーティング値を
	 * 計算し、その結果を統計情報へ設定するまでの処理を行う。</p>
	 * @param cxt Delta System用コンテキスト
	 */
	protected abstract void compute(DsContext cxt);

	/**
	 * デバッグ出力
	 * <p>当メソッドは現在のデバッグモードに応じて、必要となるデバッグ情報を出力する。
	 * サマリ出力は{@link #dumpSummary(DsContext, double, int, Object...)}、
	 * 詳細出力は{@link #dumpDetail(DsContext, double, int, List, Object...)}に処理を委譲する。
	 * 出力処理は各レーティング種別で実装すること。</p>
	 * @param cxt Delta System用コンテキスト
	 * @param org 算出したレーティング値のオリジナル値
	 * @param rating orgの値をレーティング種別ごとの値の範囲にスケーリングした最終評価点の値
	 * @param elems レーティング要素リスト
	 * @param values その他、デバッグ出力のために必要なオブジェクトのリスト
	 */
	protected final void debugOut(DsContext cxt, double org, int rating, List<? extends RatingElement> elems,
			Object...values) {
		var debugMode = Ds.getDebugMode();
		if (debugMode == DsDebugMode.NONE) {
			// なし
			// Do nothing
		} else if (Ds.getDebugMode() == DsDebugMode.SIMPLE) {
			// シンプル
			Ds.debug("%d", (int)rating);
		} else if (Ds.getDebugMode() == DsDebugMode.SUMMARY) {
			// サマリー
			dumpSummary(cxt, org, rating, values);
		} else if (Ds.getDebugMode() == DsDebugMode.DETAIL) {
			// 詳細
			dumpDetail(cxt, org, rating, elems, values);
		}
	}

	/**
	 * サマリされたデバッグ情報の出力
	 * @param cxt Delta System用コンテキスト
	 * @param org 算出したレーティング値のオリジナル値
	 * @param rating orgの値をレーティング種別ごとの値の範囲にスケーリングした最終評価点の値
	 * @param values その他、デバッグ出力のために必要なオブジェクトのリスト
	 */
	protected void dumpSummary(DsContext cxt, double org, int rating, Object...values) {
		// Do nothing
	}

	/**
	 * 詳細デバッグ情報の出力
	 * @param cxt Delta System用コンテキスト
	 * @param org 算出したレーティング値のオリジナル値
	 * @param rating orgの値をレーティング種別ごとの値の範囲にスケーリングした最終評価点の値
	 * @param elems レーティング要素リスト
	 * @param values その他、デバッグ出力のために必要なオブジェクトのリスト
	 */
	protected void dumpDetail(DsContext cxt, double org, int rating, List<? extends RatingElement> elems,
			Object...values) {
		RatingElement.print(elems);
		Ds.debug("%d", rating);
	}

	/**
	 * レーティング値補正処理
	 * <p>「一般的ではない構成の譜面」に対して評価点の補正(基本的には減点)を行う。ここで言うところの一般的ではないとは、
	 * 「演奏時間が極端に短い」「演奏内容(※)が極端に薄い」等を表す。前述したような譜面は、Delta Systemによる
	 * レーティング値の意義の崩壊を招きかねない要素となる可能性があり、そのような譜面に対してはレーティング値の減点を
	 * 行うことを仕様として規定することでレーティング値の意義を担保しようとする。</p>
	 * <p>Delta Systemでの適正なレーティングを行う譜面は「可能な限り一般的」であることを求めるものとする。</p>
	 * <p>※楽曲位置の数が異常に少ない譜面のこと</p>
	 * @param orgRating 算出したレーティング値のオリジナル値
	 * @param playTime 演奏時間(最初の楽曲位置から最後の楽曲位置までの時間範囲)
	 * @param customRatio レーティング種別ごとに定義される補正倍率(例えば、最低演奏内容に対する実際の演奏内容の比率等)
	 * @return 補正されたレーティング値
	 */
	protected static double computeRatingValue(double orgRating, double playTime, double customRatio) {
		var config = CommonConfig.getInstance();
		var playTimeRatio = config.ipfnPlayTime.compute(playTime);
		var customRatioAdjust = Math.max(0.0, Math.min(1.0, customRatio));
		return orgRating * playTimeRatio * customRatioAdjust;
	}
}
