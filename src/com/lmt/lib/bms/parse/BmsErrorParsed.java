package com.lmt.lib.bms.parse;

import com.lmt.lib.bms.BmsScriptError;

/**
 * BMSローダのパーサ部で発生したエラーを表す要素データクラスです。
 *
 * <p><strong>※当クラスはBMSライブラリの一般利用者が参照する必要はありません。</strong></p>
 */
public class BmsErrorParsed extends BmsParsed {
	/** エラーがないことを表すエラー要素オブジェクト */
	public static final BmsErrorParsed PASS = new BmsErrorParsed();

	/** エラー発出要因となった解析対象要素の種別 */
	public BmsParsedType causeType;
	/** エラー情報 */
	public BmsScriptError error;

	/** エラー要素のオブジェクトを構築します。 */
	public BmsErrorParsed() {
		this(BmsParsedType.ERROR, null);
	}

	/**
	 * エラー要素のオブジェクトを構築します。
	 * @param error エラー情報
	 */
	public BmsErrorParsed(BmsScriptError error) {
		super(BmsParsedType.ERROR);
		set(BmsParsedType.ERROR, error);
	}

	/**
	 * エラー要素のオブジェクトを構築します。
	 * @param causeType エラー発出要因となった解析対象要素の種別
	 * @param error エラー情報
	 */
	public BmsErrorParsed(BmsParsedType causeType, BmsScriptError error) {
		super(BmsParsedType.ERROR);
		set(causeType, error);
	}

	/**
	 * エラー要素のオブジェクトの内容を設定します。
	 * @param causeType エラー発出要因となった解析対象要素の種別
	 * @param error エラー情報
	 * @return このオブジェクトのインスタンス
	 */
	public final BmsErrorParsed set(BmsParsedType causeType, BmsScriptError error) {
		this.causeType = causeType;
		this.error = error;
		return this;
	}

	/**
	 * このオブジェクトが「エラーなし」を表すかどうかを判定します。
	 * @return エラーなしであればtrue、そうでなければfalse
	 */
	public final boolean isOk() {
		return error == null;
	}

	/**
	 * このオブジェクトが「エラーあり」を表すかどうかを判定します。
	 * @return エラーありであればtrue、そうでなければfalse
	 */
	public final boolean isFail() {
		return error != null;
	}
}