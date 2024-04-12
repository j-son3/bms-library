package com.lmt.lib.bms.internal.deltasystem;

import com.lmt.lib.bms.bemusic.BeMusicPoint;

/**
 * 譜面傾向「COMPLEX」の分析データクラス
 */
class ComplexElement extends RatingElement {
	/** インデックス値：視覚効果のあるノートの数 */
	private static final int I_NUM_VISUAL = 0;
	/** インデックス値：ノート種別の数 */
	private static final int I_NUM_TYPE = 1;
	/** インデックス値：長押し継続の数 */
	private static final int I_NUM_HOLD = 2;
	/** インデックス値：地雷の数 */
	private static final int I_NUM_MINE = 3;
	/** インデックス値：連続する空域の数 */
	private static final int I_NUM_SPACE = 4;
	/** インデックス値：入力デバイス色変化回数 */
	private static final int I_CHG_COLOR = 5;
	/** インデックス値：ノート種別変化回数 */
	private static final int I_CHG_TYPE = 6;

	/** 各種「数」の値 */
	private byte[] mCounts = new byte[7];
	/** 楽曲位置複雑度評価点 */
	private float mPointScore = 0.0f;
	/** 後方複雑度評価点 */
	private float mBackwardScore = 0.0f;
	/** 後方複雑度評価点の加算回数 */
	private int mBackwardScoreCount = 0;
	/** 1つ後方と同じ配置かどうか */
	private boolean mSameBackwardPattern = false;

	/**
	 * コンストラクタ
	 * @param point 楽曲位置情報
	 */
	ComplexElement(BeMusicPoint point) {
		super(point);
	}

	/**
	 * 視覚効果のあるノートの数取得
	 * @return 視覚効果のあるノートの数
	 */
	final int getVisualEffectCount() {
		return mCounts[I_NUM_VISUAL];
	}

	/**
	 * 視覚効果のあるノートの数設定
	 * @param count 視覚効果のあるノートの数
	 */
	final void setVisualEffectCount(int count) {
		mCounts[I_NUM_VISUAL] = (byte)count;
	}

	/**
	 * ノート種別の数取得
	 * @return ノート種別の数
	 */
	final int getNoteTypeCount() {
		return mCounts[I_NUM_TYPE];
	}

	/**
	 * ノート種別の数設定
	 * @param count ノート種別の数
	 */
	final void setNoteTypeCount(int count) {
		mCounts[I_NUM_TYPE] = (byte)count;
	}

	/**
	 * 長押し継続の数取得
	 * @return 長押し継続の数
	 */
	final int getHoldingCount() {
		return mCounts[I_NUM_HOLD];
	}

	/**
	 * 長押し継続の数設定
	 * @param count 長押し継続の数
	 */
	final void setHoldingCount(int count) {
		mCounts[I_NUM_HOLD] = (byte)count;
	}

	/**
	 * 地雷の数取得
	 * @return 地雷の数
	 */
	final int getLandmineCount() {
		return mCounts[I_NUM_MINE];
	}

	/**
	 * 地雷の数設定
	 * @param count 地雷の数
	 */
	final void setLandmineCount(int count) {
		mCounts[I_NUM_MINE] = (byte)count;
	}

	/**
	 * 連続する空域の数取得
	 * @return 連続する空域の数
	 */
	final int getSpaceCount() {
		return mCounts[I_NUM_SPACE];
	}

	/**
	 * 連続する空域の数設定
	 * @param count 連続する空域の数
	 */
	final void setSpaceCount(int count) {
		mCounts[I_NUM_SPACE] = (byte)count;
	}

	/**
	 * 入力デバイス色変化回数取得
	 * @return 入力デバイス色変化回数
	 */
	final int getChangeColorCount() {
		return mCounts[I_CHG_COLOR];
	}

	/**
	 * 入力デバイス色変化回数設定
	 * @param count 入力デバイス色変化回数
	 */
	final void setChangeColorCount(int count) {
		mCounts[I_CHG_COLOR] = (byte)count;
	}

	/**
	 * ノート種別変化回数取得
	 * @return ノート種別変化回数
	 */
	final int getChangeNoteTypeCount() {
		return mCounts[I_CHG_TYPE];
	}

	/**
	 * ノート種別変化回数設定
	 * @param count ノート種別変化回数
	 */
	final void setChangeNoteTypeCount(int count) {
		mCounts[I_CHG_TYPE] = (byte)count;
	}

	/**
	 * 楽曲位置複雑度評価点取得
	 * @return 楽曲位置複雑度評価点
	 */
	final double getPointScore() {
		return mPointScore;
	}

	/**
	 * 楽曲位置複雑度評価点設定
	 * @param score 楽曲位置複雑度評価点
	 */
	final void setPointScore(double score) {
		mPointScore = (float)score;
	}

	/**
	 * 後方複雑度評価点取得
	 * @return 後方複雑度評価点
	 */
	final double getBackwardScore() {
		return mBackwardScore;
	}

	/**
	 * 後方複雑度評価点の加算回数取得
	 * @return 後方複雑度評価点の加算回数
	 */
	final int getBackwardScoreCount() {
		return mBackwardScoreCount;
	}

	/**
	 * 後方複雑度評価点投入
	 * @param score 後方複雑度評価点
	 */
	final void putBackwardScore(double score) {
		mBackwardScore += (float)score;
		mBackwardScoreCount++;
	}

	/**
	 * 1つ後方と同じ配置かどうか取得
	 * @return 1つ後方と同じ配置かどうか
	 */
	final boolean isSameBackwardPattern() {
		return mSameBackwardPattern;
	}

	/**
	 * 1つ後方と同じ配置かどうか設定
	 * @param same 1つ後方と同じ配置かどうか
	 */
	final void setSameBackwardPattern(boolean same) {
		mSameBackwardPattern = same;
	}

	/**
	 * 楽曲位置総合評価点取得
	 * @return 楽曲位置総合評価点
	 */
	final double getTotalScore() {
		return getPointScore() + getBackwardScore();
	}

	/** {@inheritDoc} */
	@Override
	protected void printData(int pos) {
		var s = String.format("   |%.3f|%s|%2d|%2d|%2d|%2d|%2d|%2d|%2d|%-8.3f|%2d:%-5.3f|%-8.3f",
				getTimeDelta(),
				makeNotesString(),
				getVisualEffectCount(), getNoteTypeCount(), getHoldingCount(), getLandmineCount(), getSpaceCount(),
				getChangeColorCount(), getChangeNoteTypeCount(),
				getPointScore(), getBackwardScoreCount(), getBackwardScore(), getTotalScore());
		Ds.debug(s);
	}

	/** {@inheritDoc} */
	@Override
	protected void printMeasure() {
		var m = getMeasure();
		Ds.debug("%3d|-----|---------------------------------------|--|--|--|--|--|--|--|--------|--------+-------", m);
	}

	/** {@inheritDoc} */
	@Override
	protected void printHeader() {
		Ds.debug("---+-----+----+----+----+----+----+----+----+----+--------------+-----+--------+--------+-------");
		Ds.debug("   |     |    |    |    |    |    |    |    |    |     NUM      | CHG |        |        |");
		Ds.debug("   |     |    |    |    |    |    |    |    |    +--+--+--+--+--+--+--+        |        |");
		Ds.debug("M  |DELTA|SCR1|SW1 |SW2 |SW3 |SW4 |SW5 |SW6 |SW7 |VI|TY|HO|MI|SP|CO|TY|PT-SCORE|BW-SCORE|TOTAL");
		Ds.debug("---+-----+----+----+----+----+----+----+----+----+--+--+--+--+--+--+--+--------+--------+-------");
	}
}
