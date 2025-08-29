package com.lmt.lib.bms.parse;

import java.util.List;

/**
 * BMSの入力元から解析されたノートを表す要素データクラスです。
 *
 * <p><strong>※当クラスはBMSライブラリの一般利用者が参照する必要はありません。</strong></p>
 *
 * @since 0.8.0
 */
public class BmsNoteParsed extends BmsTimelineParsed {
	/** 配列データ(2文字の16, 36, 62進数値の羅列文字列) */
	public String encodedArray;
	/** 配列データ */
	public List<Integer> array;

	/** ノート要素のオブジェクトを構築します。 */
	public BmsNoteParsed() {
		this(0, null, 0, 0, (List<Integer>)null);
	}

	/**
	 * ノート要素のオブジェクトを構築します。
	 * @param lineNumber この要素が存在した入力元の行番号、または要素の登場した順番
	 * @param line この要素の元になった行の記述内容、定義内容などのデータ
	 * @param measure 小節番号
	 * @param number チャンネル番号
	 * @param array 配列データ
	 */
	public BmsNoteParsed(int lineNumber, Object line, int measure, int number, List<Integer> array) {
		super(BmsParsedType.NOTE);
		set(lineNumber, line, measure, number, array);
	}

	/**
	 * ノート要素のオブジェクトを構築します。
	 * @param lineNumber この要素が存在した入力元の行番号、または要素の登場した順番
	 * @param line この要素の元になった行の記述内容、定義内容などのデータ
	 * @param measure 小節番号
	 * @param number チャンネル番号
	 * @param array 配列データ(2文字の16, 36, 62進数値の羅列文字列)
	 */
	public BmsNoteParsed(int lineNumber, Object line, int measure, int number, String array) {
		super(BmsParsedType.NOTE);
		set(lineNumber, line, measure, number, array);
	}

	/**
	 * ノート要素のオブジェクトの内容を設定します。
	 * @param lineNumber この要素が存在した入力元の行番号、または要素の登場した順番
	 * @param line この要素の元になった行の記述内容、定義内容などのデータ
	 * @param measure 小節番号
	 * @param number チャンネル番号
	 * @param array 配列データ
	 * @return このオブジェクトのインスタンス
	 */
	public BmsNoteParsed set(int lineNumber, Object line, int measure, int number, List<Integer> array) {
		this.lineNumber = lineNumber;
		this.line = line;
		this.measure = measure;
		this.number = number;
		this.encodedArray = null;
		this.array = array;
		return this;
	}

	/**
	 * ノート要素のオブジェクトの内容を設定します。
	 * @param lineNumber この要素が存在した入力元の行番号、または要素の登場した順番
	 * @param line この要素の元になった行の記述内容、定義内容などのデータ
	 * @param measure 小節番号
	 * @param number チャンネル番号
	 * @param array 配列データ(2文字の16, 36, 62進数値の羅列文字列)
	 * @return このオブジェクトのインスタンス
	 */
	public BmsNoteParsed set(int lineNumber, Object line, int measure, int number, String array) {
		this.lineNumber = lineNumber;
		this.line = line;
		this.measure = measure;
		this.number = number;
		this.encodedArray = array;
		this.array = null;
		return this;
	}
}