package com.lmt.lib.bms.parse;

/**
 * BMSの入力元から解析された小節データを表す要素データクラスです。
 *
 * <p><strong>※当クラスはBMSライブラリの一般利用者が参照する必要はありません。</strong></p>
 *
 * @since 0.8.0
 */
public class BmsMeasureValueParsed extends BmsTimelineParsed {
	/** 小節データの値の文字列表現 */
	public String value;

	/** 小節データ要素のオブジェクトを構築します。 */
	public BmsMeasureValueParsed() {
		this(0, null, 0, 0, null);
	}

	/**
	 * 小節データ要素のオブジェクトを構築します。
	 * @param lineNumber この要素が存在した入力元の行番号、または要素の登場した順番
	 * @param line この要素の元になった行の記述内容、定義内容などのデータ
	 * @param measure 小節番号
	 * @param number チャンネル番号
	 * @param value 小節データの値の文字列表現
	 */
	public BmsMeasureValueParsed(int lineNumber, Object line, int measure, int number, String value) {
		super(BmsParsedType.MEASURE_VALUE);
		set(lineNumber, line, measure, number, value);
	}

	/**
	 * 小節データ要素のオブジェクトの内容を設定します。
	 * @param lineNumber この要素が存在した入力元の行番号、または要素の登場した順番
	 * @param line この要素の元になった行の記述内容、定義内容などのデータ
	 * @param measure 小節番号
	 * @param number チャンネル番号
	 * @param value 小節データの値の文字列表現
	 * @return このオブジェクトのインスタンス
	 */
	public BmsTimelineParsed set(int lineNumber, Object line, int measure, int number, String value) {
		this.lineNumber = lineNumber;
		this.line = line;
		this.measure = measure;
		this.number = number;
		this.value = value;
		return this;
	}
}