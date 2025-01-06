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
	 * @param ctx Delta System用コンテキスト
	 */
	public void rating(DsContext ctx) {
		var debugMode = Ds.getDebugMode();
		try {
			if (debugMode.isStarterNecessary()) {
				Ds.debug("%s Start ----------", mRatingType);
			}

			compute(ctx);
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
	 * @param ctx Delta System用コンテキスト
	 */
	protected abstract void compute(DsContext ctx);

	/**
	 * デバッグ出力
	 * <p>当メソッドは現在のデバッグモードに応じて、必要となるデバッグ情報を出力する。
	 * サマリ出力は{@link #dumpSummarySp(DsContext, double, int, Object...)}、
	 * 詳細出力は{@link #dumpDetailSp(DsContext, double, int, List, Object...)}に処理を委譲する。
	 * 出力処理は各レーティング種別で実装すること。</p>
	 * @param ctx Delta System用コンテキスト
	 * @param rating orgの値をレーティング種別ごとの値の範囲にスケーリングした最終評価点の値
	 * @param elems レーティング要素リスト
	 * @param values その他、デバッグ出力のために必要なオブジェクトのリスト
	 */
	protected final void debugOut(DsContext ctx, int rating, List<? extends RatingElement> elems, Object...values) {
		var debugMode = Ds.getDebugMode();
		if (debugMode == DsDebugMode.NONE) {
			// なし
			// Do nothing
		} else if (Ds.getDebugMode() == DsDebugMode.SIMPLE) {
			// シンプル
			Ds.debug("%d", (int)rating);
		} else if (Ds.getDebugMode() == DsDebugMode.SUMMARY) {
			// サマリー
			if (ctx.dpMode) {
				dumpSummaryDp(ctx, rating, values);
			} else {
				dumpSummarySp(ctx, rating, values);
			}
		} else if (Ds.getDebugMode() == DsDebugMode.DETAIL) {
			// 詳細
			if (ctx.dpMode) {
				dumpDetailDp(ctx, rating, elems, values);
			} else {
				dumpDetailSp(ctx, rating, elems, values);
			}
		}
	}

	/**
	 * サマリされたデバッグ情報の出力(SP)
	 * @param ctx Delta System用コンテキスト
	 * @param org 算出したレーティング値のオリジナル値
	 * @param rating orgの値をレーティング種別ごとの値の範囲にスケーリングした最終評価点の値
	 * @param values その他、デバッグ出力のために必要なオブジェクトのリスト
	 */
	protected void dumpSummarySp(DsContext ctx, int rating, Object...values) {
		// Do nothing
	}

	/**
	 * サマリされたデバッグ情報の出力(DP)
	 * @param ctx Delta System用コンテキスト
	 * @param rating orgの値をレーティング種別ごとの値の範囲にスケーリングした最終評価点の値
	 * @param values その他、デバッグ出力のために必要なオブジェクトのリスト
	 */
	protected void dumpSummaryDp(DsContext ctx, int rating, Object...values) {
		// Do nothing
	}

	/**
	 * 詳細デバッグ情報の出力(SP)
	 * @param ctx Delta System用コンテキスト
	 * @param rating orgの値をレーティング種別ごとの値の範囲にスケーリングした最終評価点の値
	 * @param elems レーティング要素リスト
	 * @param values その他、デバッグ出力のために必要なオブジェクトのリスト
	 */
	protected void dumpDetailSp(DsContext ctx, int rating, List<? extends RatingElement> elems, Object...values) {
		RatingElement.print(elems);
		Ds.debug("%d", rating);
	}

	/**
	 * 詳細デバッグ情報の出力(DP)
	 * @param ctx Delta System用コンテキスト
	 * @param rating orgの値をレーティング種別ごとの値の範囲にスケーリングした最終評価点の値
	 * @param elems レーティング要素リスト
	 * @param values その他、デバッグ出力のために必要なオブジェクトのリスト
	 */
	protected void dumpDetailDp(DsContext ctx, int rating, List<? extends RatingElement> elems, Object...values) {
		RatingElement.print(elems);
		Ds.debug("%d", rating);
	}
}
