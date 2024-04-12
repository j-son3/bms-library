package com.lmt.lib.bms;

/**
 * 変更可能な参照型の数値。
 */
abstract class MutableNumber implements Comparable<MutableNumber> {
	/**
	 * 32ビット整数値取得
	 * @return 32ビット整数値
	 */
	public abstract int intValue();

	/**
	 * 64ビット整数値取得
	 * @return 64ビット整数値
	 */
	public abstract long longValue();

	/**
	 * 単精度浮動小数値取得
	 * @return 単精度浮動小数値
	 */
	public abstract float floatValue();

	/**
	 * 倍精度浮動小数値取得
	 * @return 倍精度浮動小数値
	 */
	public abstract double doubleValue();

	/**
	 * 変更可能な参照型の数値と比較
	 * @param number 変更可能な参照型の数値
	 * @return 比較結果が同じならtrue、異なるならfalse
	 */
	protected abstract boolean equalsImpl(MutableNumber number);

	/**
	 * 数値オブジェクトと比較
	 * @param number 数値オブジェクト
	 * @return 比較結果が同じならtrue、異なるならfalse
	 */
	protected abstract boolean equalsImpl(Number number);

	/**
	 * 8ビット整数値取得
	 * @return 8ビット整数値
	 */
	final byte byteValue() {
		return (byte)intValue();
	}

	/**
	 * 16ビット整数値取得
	 * @return 16ビット整数値
	 */
	final short shortValue() {
		return (short)intValue();
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		} else if (obj instanceof MutableNumber) {
			return equalsImpl((MutableNumber)obj);
		} else if (obj instanceof Number) {
			return equalsImpl((Number)obj);
		} else {
			return false;
		}
	}
}
