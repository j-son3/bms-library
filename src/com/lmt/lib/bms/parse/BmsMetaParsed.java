package com.lmt.lib.bms.parse;

import com.lmt.lib.bms.BmsMeta;

/**
 * BMSの入力元から解析されたメタ情報を表す要素データクラスです。
 *
 * <p><strong>※当クラスはBMSライブラリの一般利用者が参照する必要はありません。</strong></p>
 */
public class BmsMetaParsed extends BmsParsed {
	/** メタ情報 */
	public BmsMeta meta;
	/** 索引付きメタ情報のインデックス(整数値変換前) */
	public String encodedIndex;
	/** 索引付きメタ情報のインデックス(索引付き以外では0であること) */
	public int index;
	/** メタ情報の値の文字列表現 */
	public String value;

	/** メタ情報要素のオブジェクトを構築します。 */
	public BmsMetaParsed() {
		this(0, null, null, 0, null);
	}

	/**
	 * メタ情報要素のオブジェクトを構築します。
	 * @param lineNumber この要素が存在した入力元の行番号、または要素の登場した順番
	 * @param line この要素の元になった行の記述内容、定義内容などのデータ
	 * @param meta メタ情報
	 * @param index 索引付きメタ情報のインデックス
	 * @param value メタ情報の値
	 */
	public BmsMetaParsed(int lineNumber, Object line, BmsMeta meta, int index, String value) {
		super(BmsParsedType.META);
		set(lineNumber, line, meta, index, value);
	}

	/**
	 * メタ情報要素のオブジェクトを構築します。
	 * @param lineNumber この要素が存在した入力元の行番号、または要素の登場した順番
	 * @param line この要素の元になった行の記述内容、定義内容などのデータ
	 * @param meta メタ情報
	 * @param index 索引付きメタ情報のインデックス(16, 36, 62進表記の文字列)
	 * @param value メタ情報の値
	 */
	public BmsMetaParsed(int lineNumber, Object line, BmsMeta meta, String index, String value) {
		super(BmsParsedType.META);
		set(lineNumber, line, meta, index, value);
	}

	/**
	 * メタ情報要素のオブジェクトの内容を設定します。
	 * @param lineNumber この要素が存在した入力元の行番号、または要素の登場した順番
	 * @param line この要素の元になった行の記述内容、定義内容などのデータ
	 * @param meta メタ情報
	 * @param index 索引付きメタ情報のインデックス
	 * @param value メタ情報の値
	 * @return このオブジェクトのインスタンス
	 */
	public final BmsMetaParsed set(int lineNumber, Object line, BmsMeta meta, int index, String value) {
		this.lineNumber = lineNumber;
		this.line = line;
		this.encodedIndex = null;
		this.meta = meta;
		this.index = index;
		this.value = value;
		return this;
	}

	/**
	 * メタ情報要素のオブジェクトの内容を設定します。
	 * @param lineNumber この要素が存在した入力元の行番号、または要素の登場した順番
	 * @param line この要素の元になった行の記述内容、定義内容などのデータ
	 * @param meta メタ情報
	 * @param index 索引付きメタ情報のインデックス(16, 36, 62進表記の文字列)
	 * @param value メタ情報の値
	 * @return このオブジェクトのインスタンス
	 */
	public final BmsMetaParsed set(int lineNumber, Object line, BmsMeta meta, String index, String value) {
		this.lineNumber = lineNumber;
		this.line = line;
		this.encodedIndex = index;
		this.meta = meta;
		this.index = 0;
		this.value = value;
		return this;
	}
}