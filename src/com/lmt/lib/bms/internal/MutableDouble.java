package com.lmt.lib.bms.internal;

/**
 * 変更可能な参照型の倍精度浮動小数値。
 */
public class MutableDouble extends MutableNumber {
	/** 値 */
	private double mValue;

	/** コンストラクタ */
	public MutableDouble() {
		mValue = 0.0;
	}

	/**
	 * コンストラクタ
	 * @param value 値
	 */
	public MutableDouble(Number value) {
		mValue = value.doubleValue();
	}

	/**
	 * コンストラクタ
	 * @param value 値
	 */
	public MutableDouble(MutableNumber value) {
		mValue = value.doubleValue();
	}

	/**
	 * コンストラクタ
	 * @param value 値
	 */
	public MutableDouble(double value) {
		mValue = value;
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		return Double.hashCode(mValue);
	}

	/** {@inheritDoc} */
	@Override
	public int compareTo(MutableNumber o) {
		return Double.compare(mValue, o.doubleValue());
	}

	/**
	 * 値取得
	 * @return 値
	 */
	public final double get() {
		return mValue;
	}

	/**
	 * 値設定
	 * @param value 値
	 * @return このオブジェクトのインスタンス
	 */
	public final MutableDouble set(double value) {
		mValue = value;
		return this;
	}

	/** {@inheritDoc} */
	@Override
	public final int intValue() {
		return (int)mValue;
	}

	/** {@inheritDoc} */
	@Override
	public final long longValue() {
		return (long)mValue;
	}

	/** {@inheritDoc} */
	@Override
	public final float floatValue() {
		return (float)mValue;
	}

	/** {@inheritDoc} */
	@Override
	public final double doubleValue() {
		return mValue;
	}

	/** {@inheritDoc} */
	@Override
	protected final boolean equalsImpl(MutableNumber number) {
		return mValue == number.doubleValue();
	}

	/** {@inheritDoc} */
	@Override
	protected final boolean equalsImpl(Number number) {
		return mValue == number.doubleValue();
	}
}
