package com.lmt.lib.bms.parse;

/**
 * BMSの入力元から解析されたBMS宣言を表す要素データクラスです。
 *
 * <p><strong>※当クラスはBMSライブラリの一般利用者が参照する必要はありません。</strong></p>
 *
 * @since 0.8.0
 */
public class BmsDeclarationParsed extends BmsParsed {
	/** BMS宣言の名前 */
	public String name;
	/** BMS宣言の値 */
	public String value;

	/** BMS宣言要素のオブジェクトを構築します。 */
	public BmsDeclarationParsed() {
		this(0, null, null, null);
	}

	/**
	 * BMS宣言要素のオブジェクトを構築します。
	 * @param lineNumber この要素が存在した入力元の行番号、または要素の登場した順番
	 * @param line この要素の元になった行の記述内容、定義内容などのデータ
	 * @param name BMS宣言の名前
	 * @param value BMS宣言の値
	 */
	public BmsDeclarationParsed(int lineNumber, Object line, String name, String value) {
		super(BmsParsedType.DECLARATION);
		set(lineNumber, line, name, value);
	}

	/**
	 * BMS宣言要素のオブジェクトの内容を設定します。
	 * @param lineNumber この要素が存在した入力元の行番号、または要素の登場した順番
	 * @param line この要素の元になった行の記述内容、定義内容などのデータ
	 * @param name BMS宣言の名前
	 * @param value BMS宣言の値
	 * @return このオブジェクトのインスタンス
	 */
	public final BmsDeclarationParsed set(int lineNumber, Object line, String name, String value) {
		this.lineNumber = lineNumber;
		this.line = line;
		this.name = name;
		this.value = value;
		return this;
	}
}