package com.lmt.lib.bms.internal.deltasystem;

/**
 * 指の抵抗情報クラス
 */
class Resist {
	/** 指 */
	private Finger mFinger;
	/** 抵抗値 */
	private double mValue;

	/**
	 * コンストラクタ
	 * @param finger 指
	 * @param value 抵抗値
	 */
	Resist(Finger finger, double value) {
		this.mFinger = finger;
		this.mValue = value;
	}

	/**
	 * 指取得
	 * @return 指
	 */
	final Finger getFinger() {
		return mFinger;
	}

	/**
	 * 抵抗値取得
	 * @return 抵抗値
	 */
	final double getValue() {
		return mValue;
	}

	/**
	 * 抵抗情報の文字列表現取得
	 * @return 抵抗情報の文字列表現
	 */
	@Override
	public String toString() {
		return String.format("{ %s:%s }", mFinger.name(), mValue);
	}
}
