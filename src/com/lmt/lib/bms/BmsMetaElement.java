package com.lmt.lib.bms;

import java.util.Objects;

/**
 * メタ情報とインデックス値、およびその値がセットになった要素クラスです。
 *
 * <p>当クラスはメタ情報をストリームAPIで走査する際に1つのメタ情報にアクセスするためのデータコンテナの役割を果たします。</p>
 */
public class BmsMetaElement {
	/** メタ情報 */
	private BmsMeta mMeta;
	/** インデックス値(単体メタ情報の場合は0、空の複数・索引付きメタ情報を表す場合はマイナス値) */
	private int mIndex;
	/** メタ情報の値(未設定の場合はnull) */
	private Object mValue;

	/**
	 * コンストラクタ
	 * @param meta メタ情報
	 * @param index インデックス値
	 * @param value メタ情報の値
	 */
	BmsMetaElement(BmsMeta meta, int index, Object value) {
		mMeta = meta;
		mIndex = index;
		mValue = value;
	}

	/**
	 * メタ情報を取得します。
	 * @return メタ情報
	 */
	public final BmsMeta getMeta() {
		return mMeta;
	}

	/**
	 * メタ情報の名前を取得します。
	 * @return メタ情報の名前
	 */
	public final String getName() {
		return mMeta.getName();
	}

	/**
	 * メタ情報の構成単位を取得します。
	 * @return メタ情報の構成単位
	 */
	public final BmsUnit getUnit() {
		return mMeta.getUnit();
	}

	/**
	 * メタ情報のデータ型を取得します。
	 * @return メタ情報のデータ型
	 */
	public final BmsType getType() {
		return mMeta.getType();
	}

	/**
	 * インデックス値を取得します。
	 * <p>単体メタ情報の場合、常に0を返します。</p>
	 * <p>複数・索引付きメタ情報で、データが0個の場合はマイナス値を返します。
	 * これについての詳細情報は {@link BmsContent#metas()} を参照してください。</p>
	 * @return インデックス値
	 */
	public final int getIndex() {
		return mIndex;
	}

	/**
	 * メタ情報のデフォルト値をlong型で取得します。
	 * @return メタ情報のデフォルト値のlong型表現
	 * @exception ClassCastException メタ情報のデフォルト値をlong型に変換できない
	 * @exception NullPointerException 任意型メタ情報で当メソッドを実行した
	 */
	public final long getDefaultAsLong() {
		return ((Number)mMeta.getDefaultValue()).longValue();
	}

	/**
	 * メタ情報のデフォルト値をdouble型で取得します。
	 * @return メタ情報のデフォルト値のdouble型表現
	 * @exception ClassCastException メタ情報のデフォルト値をdouble型に変換できない
	 * @exception NullPointerException 任意型メタ情報で当メソッドを実行した
	 */
	public final double getDefaultAsDouble() {
		return ((Number)mMeta.getDefaultValue()).doubleValue();
	}

	/**
	 * メタ情報のデフォルト値をString型で取得します。
	 * @return メタ情報のデフォルト値のString型表現
	 */
	public final String getDefaultAsString() {
		var defaultValue = mMeta.getDefaultValue();
		return (defaultValue == null) ? null : defaultValue.toString();
	}

	/**
	 * メタ情報のデフォルト値を{@link BmsArray}型で取得します。
	 * @return メタ情報のデフォルト値の{@link BmsArray}型表現
	 * @exception ClassCastException メタ情報のデフォルト値を{@link BmsArray}型に変換できない
	 */
	public final BmsArray getDefaultAsArray() {
		return (BmsArray)mMeta.getDefaultValue();
	}

	/**
	 * メタ情報のデフォルト値を指定した型にキャストして取得します。
	 * <p>当メソッドはデフォルト値を任意のデータ型にキャストして返します。</p>
	 * @param <T> 任意のデータ型
	 * @return メタ情報のデフォルト値
	 * @exception ClassCastException メタ情報のデフォルト値を指定された任意のデータ型に変換できない
	 */
	@SuppressWarnings("unchecked")
	public final <T> T getDefault() {
		return (T)mMeta.getDefaultValue();
	}

	/**
	 * メタ情報の値をlong型で取得します。
	 * @return メタ情報の値のlong型表現
	 * @exception ClassCastException メタ情報の値をlong型に変換できない
	 */
	public final long getValueAsLong() {
		return ((Number)getValue()).longValue();
	}

	/**
	 * メタ情報の値をdouble型で取得します。
	 * @return メタ情報の値のdouble型表現
	 * @exception ClassCastException メタ情報の値をdouble型に変換できない
	 */
	public final double getValueAsDouble() {
		return ((Number)getValue()).doubleValue();
	}

	/**
	 * メタ情報の値をString型で取得します。
	 * @return メタ情報の値のString型表現
	 */
	public final String getValueAsString() {
		return getValue().toString();
	}

	/**
	 * メタ情報の値を{@link BmsArray}型で取得します。
	 * @return メタ情報の値の{@link BmsArray}型表現
	 * @exception ClassCastException メタ情報の値を{@link BmsArray}型に変換できない
	 */
	public final BmsArray getValueAsArray() {
		return getValue();
	}

	/**
	 * メタ情報の値を指定した型にキャストして取得します。
	 * <p>当メソッドは任意型メタ情報のデータを取得する際に利用してください。</p>
	 * @param <T> 任意のデータ型
	 * @return メタ情報の値。値が未設定の場合はメタ情報のデフォルト値。
	 * @exception ClassCastException メタ情報の値を指定したデータ型に変換できない
	 */
	@SuppressWarnings("unchecked")
	public final <T> T getValue() {
		return (T)(Objects.nonNull(mValue) ? mValue : mMeta.getDefaultValue());
	}

	/**
	 * メタ情報の値取得
	 * <p>当メソッドは、値がnullの時にデフォルト値を返す処理を行わない。</p>
	 * @param <T> 任意のデータ型
	 * @return メタ情報の値
	 */
	@SuppressWarnings("unchecked")
	final <T> T getRawValue() {
		return (T)mValue;
	}

	/**
	 * このメタ情報にデータが設定されているかどうかを取得します。
	 * <p>複数・索引付きメタ情報において、そのメタ情報が0個であることを示す(インデックスがマイナス値)
	 * の場合、当メソッドはfalseを返します。</p>
	 * @return このメタ情報にデータが設定されている場合true
	 */
	public final boolean isContain() {
		return Objects.nonNull(mValue);
	}

	/**
	 * このメタ情報が単体メタ情報かどうかを取得します。
	 * @return 単体メタ情報であればtrue
	 * @see BmsMeta#isSingleUnit()
	 */
	public final boolean isSingleUnit() {
		return mMeta.isSingleUnit();
	}

	/**
	 * このメタ情報が複数メタ情報かどうかを取得します。
	 * @return 複数メタ情報であればtrue
	 * @see BmsMeta#isMultipleUnit()
	 */
	public final boolean isMultipleUnit() {
		return mMeta.isMultipleUnit();
	}

	/**
	 * このメタ情報が索引付きメタ情報かどうかを取得します。
	 * @return 索引付きメタ情報であればtrue
	 * @see BmsMeta#isIndexedUnit()
	 */
	public final boolean isIndexedUnit() {
		return mMeta.isIndexedUnit();
	}

	/**
	 * このメタ情報のデータ型が整数かどうかを取得します。
	 * @return 整数型であればtrue
	 * @see BmsMeta#isIntegerType()
	 */
	public final boolean isIntegerType() {
		return mMeta.isIntegerType();
	}

	/**
	 * このメタ情報のデータ型が実数かどうかを取得します。
	 * @return 実数型であればtrue
	 * @see BmsMeta#isFloatType()
	 */
	public final boolean isFloatType() {
		return mMeta.isFloatType();
	}

	/**
	 * このメタ情報のデータ型が文字列かどうかを取得します。
	 * @return 文字列型であればtrue
	 * @see BmsMeta#isStringType()
	 */
	public final boolean isStringType() {
		return mMeta.isStringType();
	}

	/**
	 * このメタ情報のデータ型が16進数値かどうかを取得します。
	 * @return 16進数値型であればtrue
	 * @see BmsMeta#isBase16Type()
	 */
	public final boolean isBase16Type() {
		return mMeta.isBase16Type();
	}

	/**
	 * このメタ情報のデータ型が36進数値かどうかを取得します。
	 * @return 36進数値型であればtrue
	 * @see BmsMeta#isBase36Type()
	 */
	public final boolean isBase36Type() {
		return mMeta.isBase36Type();
	}

	/**
	 * このメタ情報のデータ型が16進数値配列かどうかを取得します。
	 * @return 16進数値配列型であればtrue
	 * @see BmsMeta#isArray16Type()
	 */
	public final boolean isArray16Type() {
		return mMeta.isArray16Type();
	}

	/**
	 * このメタ情報のデータ型が36進数値配列かどうかを取得します。
	 * @return 36進数値配列型であればtrue
	 * @see BmsMeta#isArray36Type()
	 */
	public final boolean isArray36Type() {
		return mMeta.isArray36Type();
	}

	/**
	 * このメタ情報のデータ型が任意型かどうかを取得します。
	 * @return 任意型であればtrue
	 * @see BmsMeta#isObjectType()
	 */
	public final boolean isObjectType() {
		return mMeta.isObjectType();
	}

	/**
	 * このメタ情報のデータ型が数値型かどうかを取得します。
	 * @return 数値型であればtrue
	 * @see BmsMeta#isNumberType()
	 */
	public final boolean isNumberType() {
		return mMeta.isNumberType();
	}

	/**
	 * このメタ情報のデータ型が値型かどうかを取得します。
	 * @return 値型であればtrue
	 * @see BmsMeta#isValueType()
	 */
	public final boolean isValueType() {
		return mMeta.isValueType();
	}

	/**
	 * このメタ情報のデータ型が配列型かどうかを取得します。
	 * @return 配列型であればtrue
	 * @see BmsMeta#isArrayType()
	 */
	public final boolean isArrayType() {
		return mMeta.isArrayType();
	}

	/**
	 * このメタ情報のデータ型が通常型かどうかを取得します。
	 * @return 通常型であればtrue
	 * @see BmsMeta#isNormalType()
	 */
	public final boolean isNormalType() {
		return mMeta.isNormalType();
	}
}
