package com.lmt.lib.bms;

/**
 * BMS宣言の名前と値のセットです。
 *
 * @since 0.8.0
 */
public class BmsDeclarationElement {
	/** BMS宣言の名前 */
	private String mName;
	/** BMS宣言の値 */
	private String mValue;

	/**
	 * コンストラクタ
	 * @param name BMS宣言の名前
	 * @param value BMS宣言の値
	 */
	BmsDeclarationElement(String name, String value) {
		mName = name;
		mValue = value;
	}

	/**
	 * BMS宣言の名前を取得します。
	 * @return BMS宣言の名前
	 */
	public final String getName() {
		return mName;
	}

	/**
	 * BMS宣言の値を取得します。
	 * @return BMS宣言の値
	 */
	public final String getValue() {
		return mValue;
	}

	/**
	 * BMS宣言の名前と値の文字列表現を取得します。
	 * @return BMS宣言の名前と値の文字列表現
	 */
	@Override
	public String toString() {
		return String.format("%s=\"%s\"", mName, mValue);
	}
}
