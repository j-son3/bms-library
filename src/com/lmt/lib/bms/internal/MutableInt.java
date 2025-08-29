package com.lmt.lib.bms.internal;

/**
 * 変更可能な参照型の32ビット整数値。
 */
public class MutableInt extends MutableNumber {
	/** 値 */
	private int mValue;

	/** コンストラクタ */
	public MutableInt() {
		mValue = 0;
	}

	/**
	 * コンストラクタ
	 * @param value 値
	 */
	public MutableInt(Number value) {
		mValue = value.intValue();
	}

	/**
	 * コンストラクタ
	 * @param value 値
	 */
	public MutableInt(MutableNumber value) {
		mValue = value.intValue();
	}

	/**
	 * コンストラクタ
	 * @param value 値
	 */
	public MutableInt(int value) {
		mValue = value;
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		return Integer.hashCode(mValue);
	}

	/** {@inheritDoc} */
	@Override
	public int compareTo(MutableNumber o) {
		return Integer.compare(mValue, o.intValue());
	}

	/**
	 * 値取得
	 * @return 値
	 */
	public int get() {
		return mValue;
	}

	/**
	 * 値設定
	 * @param value 値
	 * @return このオブジェクトのインスタンス
	 */
	public MutableInt set(int value) {
		mValue = value;
		return this;
	}

	/** {@inheritDoc} */
	@Override
	public int intValue() {
		return mValue;
	}

	/** {@inheritDoc} */
	@Override
	public long longValue() {
		return mValue;
	}

	/** {@inheritDoc} */
	@Override
	public float floatValue() {
		return (float)mValue;
	}

	/** {@inheritDoc} */
	@Override
	public double doubleValue() {
		return (double)mValue;
	}

	/** {@inheritDoc} */
	@Override
	protected boolean equalsImpl(MutableNumber number) {
		return mValue == number.intValue();
	}

	/** {@inheritDoc} */
	@Override
	protected boolean equalsImpl(Number number) {
		return mValue == number.intValue();
	}
}
