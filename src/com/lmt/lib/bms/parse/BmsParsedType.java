package com.lmt.lib.bms.parse;

/**
 * 解析済み要素の種別を表す列挙型です。
 *
 * <p><strong>※当クラスはBMSライブラリの一般利用者が参照する必要はありません。</strong></p>
 */
public enum BmsParsedType {
	/** BMS宣言 */
	DECLARATION,
	/** メタ情報 */
	META,
	/** 小節データ */
	MEASURE_VALUE,
	/** ノート */
	NOTE,
	/** エラー */
	ERROR,
}