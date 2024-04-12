package com.lmt.lib.bms;

/**
 * BMSコンテンツの時間軸を表す小節番号・小節の刻み位置を表現します。
 *
 * <p>便宜上、このクラスを「楽曲位置」と呼称します。</p>
 *
 * @see BmsAt [BmsAt] 当クラスが実装する、時間軸を参照するインターフェイス
 */
public class BmsPoint implements BmsAt {
	/** 小節番号 */
	private int mMeasure;
	/** 刻み位置 */
	private double mTick;

	/**
	 * 先頭位置を示す楽曲位置オブジェクトを構築します。
	 */
	public BmsPoint() {
		assignFrom(0, 0);
	}

	/**
	 * 指定小節番号、刻み位置に対応する楽曲位置オブジェクトを構築します。
	 * @param measure 小節番号
	 * @param tick 刻み位置
	 * @exception IllegalArgumentException 小節番号が{@link BmsSpec#MEASURE_MAX_COUNT}の時の小節の刻み位置が{@link BmsSpec#TICK_MIN}以外
	 * @exception IllegalArgumentException 小節番号が{@link BmsSpec#MEASURE_MIN}未満、または{@link BmsSpec#MEASURE_MAX}超過
	 * @exception IllegalArgumentException 刻み位置が{@link BmsSpec#TICK_MIN}未満、または{@link BmsSpec#TICK_MAX}超過
	 */
	public BmsPoint(int measure, double tick) {
		assignFrom(measure, tick);
	}

	/**
	 * 小節番号と刻み位置が分かる形式の文字列を返します。
	 * @return 小節番号と刻み位置が分かる形式の文字列
	 */
	@Override
	public String toString() {
		return String.format("{M=%d, T=%.16g}", mMeasure, mTick);
	}

	/**
	 * 指定小節番号、刻み位置(無段階対応)をアサインします。
	 * @param measure 小節番号
	 * @param tick 刻み位置
	 * @return このオブジェクトのインスタンス
	 * @exception IllegalArgumentException 小節番号が{@link BmsSpec#MEASURE_MAX_COUNT}の時の小節の刻み位置が{@link BmsSpec#TICK_MIN}以外
	 * @exception IllegalArgumentException 小節番号が{@link BmsSpec#MEASURE_MIN}未満、または{@link BmsSpec#MEASURE_MAX}超過
	 * @exception IllegalArgumentException 刻み位置が{@link BmsSpec#TICK_MIN}未満、または{@link BmsSpec#TICK_MAX}超過
	 */
	public BmsPoint assignFrom(int measure, double tick) {
		assertArgPoint(measure, tick);
		mMeasure = measure;
		mTick = tick;
		return this;
	}

	/**
	 * 指定された楽曲位置の値をアサインする。(例外を発生させない)
	 * <p>当メソッドは内部処理専用のため、外部公開禁止。</p>
	 * @param measure 小節番号
	 * @param tick 刻み位置(無段階対応)
	 */
	void assignFromWithoutException(int measure, double tick) {
		mMeasure = measure;
		mTick = tick;
	}

	/**
	 * 小節番号を設定します。
	 * <p>小節番号に{@link BmsSpec#MEASURE_MAX_COUNT}を指定すると、小節の刻み位置は0に設定されます。</p>
	 * @param measure 小節番号
	 * @exception IllegalArgumentException 小節番号が{@link BmsSpec#MEASURE_MIN}未満、または{@link BmsSpec#MEASURE_MAX_COUNT}超過
	 */
	public void setMeasure(int measure) {
		if (measure == BmsSpec.MEASURE_MAX_COUNT) {
			mMeasure = measure;
			mTick = 0.0;
		} else {
			assertArgPoint(measure, mTick);
			mMeasure = measure;
		}
	}

	/**
	 * 小節の刻み位置を設定します。
	 * @param tick 小節の刻み位置
	 * @exception IllegalArgumentException このオブジェクトの小節番号が{@link BmsSpec#MEASURE_MAX_COUNT}の時に刻み位置が{@link BmsSpec#TICK_MIN}以外
	 * @exception IllegalArgumentException 刻み位置が{@link BmsSpec#TICK_MIN}未満、または{@link BmsSpec#TICK_MAX}超過
	 */
	public void setTick(double tick) {
		assertArgPoint(mMeasure, tick);
		mTick = tick;
	}

	/**
	 * @see BmsAt#getMeasure()
	 */
	@Override
	public int getMeasure() {
		return mMeasure;
	}

	/**
	 * @see BmsAt#getTick()
	 */
	@Override
	public double getTick() {
		return mTick;
	}

	/**
	 * 楽曲位置のアサーション
	 * @param measure 小節番号
	 * @param tick 小節の刻み位置
	 * @exception IllegalArgumentException 小節番号が{@link BmsSpec#MEASURE_MAX_COUNT}の時の小節の刻み位置が{@link BmsSpec#TICK_MIN}以外
	 * @exception IllegalArgumentException 小節番号が{@link BmsSpec#MEASURE_MIN}未満、または{@link BmsSpec#MEASURE_MAX}超過
	 * @exception IllegalArgumentException 刻み位置が{@link BmsSpec#TICK_MIN}未満、または{@link BmsSpec#TICK_MAX}超過
	 */
	private static void assertArgPoint(int measure, double tick) {
		if (measure == BmsSpec.MEASURE_MAX_COUNT) {
			if (tick != (double)BmsSpec.TICK_MIN) {
				var msg = String.format("When terminate measure, tick must set %d. measure=%d, tick=%.16g",
						BmsSpec.TICK_MIN, measure, tick);
				throw new IllegalArgumentException(msg);
			}
		} else {
			if ((measure < BmsSpec.MEASURE_MIN) || (measure > BmsSpec.MEASURE_MAX)) {
				var msg = String.format(
						"Argument 'measure' is out of range. expect-measure=(%d-%d), actual-measure=%d, tick=%.16g",
						BmsSpec.MEASURE_MIN, BmsSpec.MEASURE_MAX, measure, tick);
				throw new IllegalArgumentException(msg);
			}
			if ((tick < (double)BmsSpec.TICK_MIN) || (tick > (double)BmsSpec.TICK_MAX)) {
				var msg = String.format(
						"Argument 'tick' is out of range. measure=%d, expect-tick=(%.16g-%.16g), actual-tick=%.16g",
						measure, BmsSpec.TICK_MIN, BmsSpec.TICK_MAX, tick);
				throw new IllegalArgumentException(msg);
			}
		}
	}
}
