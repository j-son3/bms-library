package com.lmt.lib.bms.parse;

/**
 * BMSの入力元から解析されたタイムライン要素データクラスです。
 *
 * <p><strong>※当クラスはBMSライブラリの一般利用者が参照する必要はありません。</strong></p>
 *
 * @since 0.8.0
 */
public abstract class BmsTimelineParsed extends BmsParsed {
	/** 小節番号 */
	public int measure;
	/** チャンネル番号 */
	public int number;

	/**
	 * タイムライン要素のオブジェクトを構築します。
	 * @param type 要素の種別
	 */
	protected BmsTimelineParsed(BmsParsedType type) {
		super(type);
	}
}