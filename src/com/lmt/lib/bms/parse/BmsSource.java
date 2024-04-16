package com.lmt.lib.bms.parse;

import static com.lmt.lib.bms.internal.Assertion.*;

/**
 * BMSローダへの入力データを表します。
 *
 * <p><strong>※当クラスはBMSライブラリの一般利用者が参照する必要はありません。</strong></p>
 *
 * <p>BMSローダは入力データの分類として「バイナリフォーマット」「テキストフォーマット」の2つに対応しています。
 * この分類はローダごとに固定化されており、どちらかが確定的な入力データとしてBMSローダのパーサ部に入力されます。
 * 当クラスは入力データの分類の違いをラップし、統一されたインターフェイスをもって入力データをパーサ部まで届けます。</p>
 */
public class BmsSource {
	/** 入力データがバイナリフォーマット時の入力データ */
	private byte[] mBinary;
	/** 入力データがテキストフォーマット時の入力データ */
	private String mScript;

	/**
	 * バイナリフォーマットの入力データオブジェクトを構築します。
	 * @param binary 入力データ
	 * @exception NullPointerException binaryがnull
	 */
	public BmsSource(byte[] binary) {
		assertArgNotNull(binary, "binary");
		mBinary = binary;
		mScript = null;
	}

	/**
	 * テキストフォーマットの入力データオブジェクトを構築します。
	 * @param script 入力データ
	 * @exception NullPointerException scriptがnull
	 */
	public BmsSource(String script) {
		assertArgNotNull(script, "script");
		mBinary = null;
		mScript = script;
	}

	/**
	 * バイナリの入力データを取得します。
	 * @return バイナリの入力データ
	 */
	public byte[] getAsBinary() {
		return mBinary;
	}

	/**
	 * テキストの入力データを取得します。
	 * @return テキストの入力データ
	 */
	public String getAsScript() {
		return mScript;
	}
}
