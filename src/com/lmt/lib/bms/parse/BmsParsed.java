package com.lmt.lib.bms.parse;

/**
 * BMSの入力元から解析された1つの要素を表すオブジェクトの抽象クラスです。
 *
 * <p><strong>※当クラスはBMSライブラリの一般利用者が参照する必要はありません。</strong></p>
 *
 * <p>当クラスはBMSコンテンツを構成する複数種類の要素の基底クラスであり、それぞれの要素が持つ共通の情報を管理します。
 * このオブジェクトはBMS読み込み時にBMSローダのパーサ部から返される一時的なオブジェクトで、
 * BMSローダのBMSコンテンツの読み込み処理部とパーサ部とのデータの橋渡しに利用されます。
 * つまり、当クラスから派生するクラスは全て内部処理用のクラスであるためBMSライブラリの一般利用者はこれらを無視して構いません。</p>
 */
public abstract class BmsParsed {
	/** 要素の種別 */
	private BmsParsedType mType;
	/** この要素が存在した入力元の行番号、または要素の登場した順番 */
	public int lineNumber;
	/** この要素の元になった行の記述内容、定義内容などのデータ */
	public Object line;

	/**
	 * 新しい解析済み要素オブジェクトを構築します。
	 * @param type 要素の種別
	 */
	protected BmsParsed(BmsParsedType type) {
		mType = type;
	}

	/**
	 * 解析済み要素の種別を取得します。
	 * @return 解析済み要素の種別
	 */
	public final BmsParsedType getType() {
		return mType;
	}

	/**
	 * この要素がエラー要素かどうかを返します。
	 * @return エラー要素であればtrue、そうでなければfalse
	 */
	public final boolean isErrorType() {
		return mType == BmsParsedType.ERROR;
	}
}