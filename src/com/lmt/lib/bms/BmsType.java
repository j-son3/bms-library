package com.lmt.lib.bms;

import static com.lmt.lib.bms.internal.Assertion.*;

import java.util.ArrayList;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * メタ情報・チャンネルのデータ型を表します。
 *
 * <p>当クラスが提供する機能は、データ型の情報参照、データの変換テスト、データ変換です。当クラスが提供する機能は、
 * 主に{@link BmsContent}から使用され、BMSコンテンツのデータに関する制御処理に用いられます。</p>
 *
 * @see BmsMeta
 * @see BmsChannel
 */
public final class BmsType {
	/** Integerから各データ型への変換処理 */
	private static final ArrayList<BiFunction<BmsType, Object, Object>> CASTERS_INTEGER = new ArrayList<>();
	/** Longから各データ型への変換処理 */
	private static final ArrayList<BiFunction<BmsType, Object, Object>> CASTERS_LONG = new ArrayList<>();
	/** Floatから各データ型への変換処理 */
	private static final ArrayList<BiFunction<BmsType, Object, Object>> CASTERS_FLOAT = new ArrayList<>();
	/** Doubleから各データ型への変換処理 */
	private static final ArrayList<BiFunction<BmsType, Object, Object>> CASTERS_DOUBLE = new ArrayList<>();
	/** Stringから各データ型への変換処理 */
	private static final ArrayList<BiFunction<BmsType, Object, Object>> CASTERS_STRING = new ArrayList<>();
	/** BmsArrayから各データ型への変換処理 */
	private static final ArrayList<BiFunction<BmsType, Object, Object>> CASTERS_ARRAY = new ArrayList<>();
	/** Numberから各データ型への変換処理 */
	private static final ArrayList<BiFunction<BmsType, Object, Object>> CASTERS_NUMBER = new ArrayList<>();

	/** 整数型 */
	private static final int TYPE_INTEGER = 0;
	/** 実数型 */
	private static final int TYPE_FLOAT = 10;
	/** 文字列型 */
	private static final int TYPE_STRING = 20;
	/** 16進数値型 */
	private static final int TYPE_BASE16 = 30;
	/** 36進数値型 */
	private static final int TYPE_BASE36 = 31;
	/** 62進数値型 */
	private static final int TYPE_BASE62 = 32;
	/** 基数選択数値型 */
	private static final int TYPE_BASE = 33;
	/** 16進数値配列型 */
	private static final int TYPE_ARRAY16 = 40;
	/** 36進数値配列 */
	private static final int TYPE_ARRAY36 = 41;
	/** 62進数値配列 */
	private static final int TYPE_ARRAY62 = 42;
	/** 基数選択数値配列 */
	private static final int TYPE_ARRAY = 43;
	/** 任意型 */
	private static final int TYPE_OBJECT = 7;

	/** ネイティブデータ型がLongであることを示します。 */
	public static final int NTYPE_LONG = 0;
	/** ネイティブデータ型がDoubleであることを示します。 */
	public static final int NTYPE_DOUBLE = 1;
	/** ネイティブデータ型がStringであることを示します。 */
	public static final int NTYPE_STRING = 2;
	/** ネイティブデータ型がBmsArrayであることを示します。 */
	public static final int NTYPE_ARRAY = 3;
	/** ネイティブデータ型がObjectであることを示します。 */
	public static final int NTYPE_OBJECT = 4;

	/**
	 * 整数型
	 * <p>このデータ型の値は内部的にはlong型として扱われ、値の範囲は{@link Long#MIN_VALUE} から
	 * {@link Long#MAX_VALUE}となります。</p>
	 * <p>文字列で許可される表現は [\+\-]?[0-9]+ となり、前後の空白文字は許可されません。文字列の内容が
	 * 値の範囲を超えている場合の変換後の値は未定義です。</p>
	 */
	public static final BmsType INTEGER = new BmsType(
			TYPE_INTEGER,
			"INTEGER",
			"^[\\+\\-]?[0-9]+$",
			10,
			NTYPE_LONG,
			n -> true);

	/**
	 * 実数型
	 * <p>このデータ型の値は内部的にはdouble型として扱われ、値の範囲は{@link Double#MIN_VALUE} から
	 * {@link Double#MAX_VALUE}となります。</p>
	 * <p>文字列で許可される表現は [\\+\\-]?[0-9]+(\\.[0-9]+)?([eE][\\+\\-]?[0-9]+)? となり、前後の空白文字は許可されません。
	 * 小数点以下の記述は必須ではありません。文字列の内容が値の範囲を超えている場合、および小数点以下の精度が
	 * double型で表現不可能な場合の変換後の値は未定義です。
	 */
	public static final BmsType FLOAT = new BmsType(
			TYPE_FLOAT,
			"FLOAT",
			"^[\\+\\-]?[0-9]+(\\.[0-9]+)?([eE][\\+\\-]?[0-9]+)?$",
			10,
			NTYPE_DOUBLE,
			n -> true);

	/**
	 * 文字列型
	 * <p>このデータ型の値は内部的にはStringとして扱われます。文字列の長さはStringが取り得る最大の長さまで
	 * 表現可能です。</p>
	 */
	public static final BmsType STRING = new BmsType(
			TYPE_STRING,
			"STRING",
			".*",
			0,
			NTYPE_STRING,
			null);

	/**
	 * 16進数値型
	 * <p>このデータ型では、1個の16進数値を表現できます。数値は2文字で表現可能な0(00)～255(FF)までとなり、
	 * その範囲を超える数値は表現できません。</p>
	 * <p>{@link #cast cast}によるデータ変換において、文字列から変換する際は基数が16になることに
	 * 注意してください。例えば、変換元データが"80"の場合の出力はlong型の128Lになります。</p>
	 */
	public static final BmsType BASE16 = new BmsType(
			TYPE_BASE16,
			"BASE16",
			"^[a-fA-F0-9]{2}$",
			16,
			NTYPE_LONG,
			n -> { long v = n.longValue(); return (v >= 0x00) && (v <= 0xff); });

	/**
	 * 36進数値型
	 * <p>このデータ型では、1個の36進数値を表現できます。数値は2文字で表現可能な0(00)～1295(ZZ)までとなり、
	 * その範囲を超える数値は表現できません。</p>
	 * <p>{@link #cast cast}によるデータ変換において、文字列から変換する際は基数が36になることに
	 * 注意してください。例えば、変換元データが"80"の場合の出力はlong型の288Lになります。</p>
	 */
	public static final BmsType BASE36 = new BmsType(
			TYPE_BASE36,
			"BASE36",
			"^[a-zA-Z0-9]{2}$",
			36,
			NTYPE_LONG,
			n -> { long v = n.longValue(); return (v >= 0) && (v <= 1295); });

	/**
	 * 62進数値型
	 * <p>このデータ型では、1個の62進数値を表現できます。数値は2文字で表現可能な0(00)～3843(zz)までとなり、
	 * その範囲を超える数値は表現できません。</p>
	 * <p>{@link #cast cast}によるデータ変換において、文字列から変換する際は基数が62になることに
	 * 注意してください。例えば、変換元データが"80"の場合の出力はlong型の496Lになります。</p>
	 */
	public static final BmsType BASE62 = new BmsType(
			TYPE_BASE62,
			"BASE62",
			"^[a-zA-Z0-9]{2}$",
			62,
			NTYPE_LONG,
			n -> { long v = n.longValue(); return (v >= 0) && (v <= 3843); });


	/**
	 * 基数選択数値型
	 * <p>このデータ型は、最大で62進数値を表現できます。{@link #BASE16}, {@link #BASE36}, {@link #BASE62}
	 * と異なる点は、BMSコンテンツまたはライブラリ利用者側の指定によって表現可能な値の範囲が変化する点にあります。</p>
	 * <p>{@link #cast cast}によるデータ変換において、文字列から変換する際は基数が62になることに
	 * 注意してください。例えば、変換元データが"80"の場合の出力はlong型の496Lになります。</p>
	 */
	public static final BmsType BASE = new BmsType(
			TYPE_BASE,
			"BASE",
			"^[a-zA-Z0-9]{2}$",
			62,
			NTYPE_LONG,
			n -> { long v = n.longValue(); return (v >= 0) && (v <= 3843); });

	/**
	 * 16進数値配列型
	 * <p>このデータ型は、0個以上の{@link #BASE16}型データを並べた配列です。内部的には
	 * {@link BmsArray}として表現されます。</p>
	 */
	public static final BmsType ARRAY16 = new BmsType(
			TYPE_ARRAY16,
			"ARRAY16",
			"^([a-fA-F0-9]{2})*$",
			16,
			NTYPE_ARRAY,
			null);

	/**
	 * 36進数値配列型
	 * <p>このデータ型は、0個以上の{@link #BASE36}型データを並べた配列です。内部的には
	 * {@link BmsArray}として表現されます。</p>
	 */
	public static final BmsType ARRAY36 = new BmsType(
			TYPE_ARRAY36,
			"ARRAY36",
			"^([a-zA-Z0-9]{2})*$",
			36,
			NTYPE_ARRAY,
			null);

	/**
	 * 62進数値配列型
	 * <p>このデータ型は、0個以上の{@link #BASE62}型データを並べた配列です。内部的には
	 * {@link BmsArray}として表現されます。</p>
	 */
	public static final BmsType ARRAY62 = new BmsType(
			TYPE_ARRAY62,
			"ARRAY62",
			"^([a-zA-Z0-9]{2})*$",
			62,
			NTYPE_ARRAY,
			null);

	/**
	 * 基数選択数値配列型
	 * <p>このデータ型は、0個以上の{@link #BASE}型データを並べた配列です。内部的には
	 * {@link BmsArray}として表現されます。</p>
	 */
	public static final BmsType ARRAY = new BmsType(
			TYPE_ARRAY,
			"ARRAY",
			"^([a-zA-Z0-9]{2})*$",
			62,
			NTYPE_ARRAY,
			null);

	/**
	 * 任意型
	 * <p>このデータ型には、あらゆるデータ型を設定可能です。但し、任意型はユーザーメタ情報、ユーザーチャンネルに対してのみ指定可能です。
	 * この制約に違反したBMS仕様は作成することができません。そのため、{@link BmsLoader}, {@link BmsSaver}での入出力には対応していません。</p>
	 */
	public static final BmsType OBJECT = new BmsType(
			TYPE_OBJECT,
			"OBJECT",
			"^.*$",
			0,
			NTYPE_OBJECT,
			null);

	/**
	 * 正規表現文字列型
	 * <p>{@link #STRING STRING}とは異なり、正規表現による書式の制約がある文字列型です。
	 * それ以外の振る舞いは文字列型と同様です。
	 * @param pattern 正規表現パターン
	 * @return 指定した正規表現パターン制約のある文字列型
	 * @exception NullPointerException patternがnull
	 * @exception PatternSyntaxException 正規表現の構文が無効である場合
	 */
	public static BmsType REGEX(String pattern) {
		return new BmsType(TYPE_STRING, "REGEX", pattern, 0, NTYPE_STRING, null);
	}

	/** データ型ID */
	private int mTypeId;
	/** データ型の名称 */
	private String mName;
	/** 当該型が認識可能な書式の正規表現パターン */
	private Pattern mPattern;
	/** 基数 */
	private int mBase;
	/** ネイティブ型(NTYPE_*) */
	private int mNativeType;
	/** 範囲チェッカー(数値型の場合のみ) */
	private Predicate<Number> mRangeChecker;
	/** 当該型が数値型であるかどうか */
	private boolean mIsNumberType;
	/** 当該型が値型であるかどうか */
	private boolean mIsValueType;
	/** 当該型が配列型であるかどうか */
	private boolean mIsArrayType;

	/** 静的データの初期化 */
	static {
		// Integer caster
		CASTERS_INTEGER.add((t, o) -> ((Number)o).longValue());    // To Long
		CASTERS_INTEGER.add((t, o) -> ((Number)o).doubleValue());  // To Double
		CASTERS_INTEGER.add((t, o) -> o.toString());               // To String
		CASTERS_INTEGER.add((t, o) -> null);                      // To BmsArray
		CASTERS_INTEGER.add((t, o) -> o);                         // To Object

		// Long caster
		CASTERS_LONG.add((t, o) -> o);                          // To Long
		CASTERS_LONG.add((t, o) -> ((Number)o).doubleValue());  // To Double
		CASTERS_LONG.add((t, o) -> o.toString());               // To String
		CASTERS_LONG.add((t, o) -> null);                      // To BmsArray
		CASTERS_LONG.add((t, o) -> o);                         // To Object

		// Float caster
		CASTERS_FLOAT.add((t, o) -> ((Number)o).longValue());    // To Long
		CASTERS_FLOAT.add((t, o) -> ((Number)o).doubleValue());  // To Double
		CASTERS_FLOAT.add((t, o) -> o.toString());               // To String
		CASTERS_FLOAT.add((t, o) -> null);                      // To BmsArray
		CASTERS_FLOAT.add((t, o) -> o);                         // To Object

		// Double caster
		CASTERS_DOUBLE.add((t, o) -> ((Number)o).longValue());  // To Long
		CASTERS_DOUBLE.add((t, o) -> o);                        // To Double
		CASTERS_DOUBLE.add((t, o) -> o.toString());             // To String
		CASTERS_DOUBLE.add((t, o) -> null);                    // To BmsArray
		CASTERS_DOUBLE.add((t, o) -> o);                       // To Object

		// String caster
		CASTERS_STRING.add((t, o) -> BmsInt.parseLong((String)o, t.getBase()));  // To Long
		CASTERS_STRING.add((t, o) -> Double.valueOf((String)o));                 // To Double
		CASTERS_STRING.add((t, o) -> o);                                         // To String
		CASTERS_STRING.add((t, o) -> new BmsArray((String)o, t.getBase()));      // To BmsArray
		CASTERS_STRING.add((t, o) -> o);                                         // To Object

		// BmsArray caster
		CASTERS_ARRAY.add((t, o) -> null);                                                 // To Long
		CASTERS_ARRAY.add((t, o) -> null);                                                 // To Double
		CASTERS_ARRAY.add((t, o) -> o.toString());                                         // To String
		CASTERS_ARRAY.add((t, o) -> castArrayToArray(t, (BmsArray)o));                     // To BmsArray
		CASTERS_ARRAY.add((t, o) -> o);                                                    // To Object

		// Number caster
		CASTERS_NUMBER.add((t, o) -> ((Number)o).longValue());    // To Long
		CASTERS_NUMBER.add((t, o) -> ((Number)o).doubleValue());  // To Double
		CASTERS_NUMBER.add((t, o) -> o.toString());               // To String
		CASTERS_NUMBER.add((t, o) -> null);                      // To BmsArray
		CASTERS_NUMBER.add((t, o) -> o);                         // To Object
	}

	/**
	 * コンストラクタ
	 * @param typeId データ型ID
	 * @param name データ型の名称
	 * @param pattern 書式の正規表現パターン
	 * @param base 基数
	 * @param nativeType ネイティブデータ型
	 * @param rangeChecker 数値型の範囲チェッカー
	 * @exception NullPointerException patternがnull
	 * @exception PatternSyntaxException 表現の構文が無効である場合
	 */
	BmsType(int typeId, String name, String pattern, int base, int nativeType, Predicate<Number> rangeChecker) {
		assertArgNotNull(pattern, "pattern");
		mTypeId = typeId;
		mName = name;
		mPattern = Pattern.compile(pattern);
		mBase = base;
		mNativeType = nativeType;
		mRangeChecker = rangeChecker;
		mIsNumberType = (nativeType == NTYPE_LONG) || (nativeType == NTYPE_DOUBLE);
		mIsValueType = (nativeType != NTYPE_ARRAY) && (nativeType != NTYPE_OBJECT);
		mIsArrayType = (nativeType == NTYPE_ARRAY);
	}

	/**
	 * BMSデータ型が同一かどうかを判定します。
	 * <p>ネイティブデータ型、および文字列解析時の許容正規表現パターンが一致するものを同一と見なします。</p>
	 * @param obj 比較対象のオブジェクト
	 * @return 同一である場合はtrue、そうでなければfalse
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		} else if (!(obj instanceof BmsType)) {
			return false;
		} else {
			var type = (BmsType)obj;
			return ((mNativeType == type.mNativeType) && mPattern.pattern().equals(type.mPattern.pattern()));
		}
	}

	/**
	 * データ型の名称を返します。
	 * @return データ型の名称
	 */
	@Override
	public String toString() {
		return mName;
	}

	/**
	 * データ型の名称を取得します。
	 * @return データ型の名称
	 */
	public final String getName() {
		return mName;
	}

	/**
	 * 文字列解析時の許容正規表現パターンを取得します。
	 * @return 文字列解析時の許容正規表現パターン
	 */
	public final Pattern getPattern() {
		return mPattern;
	}

	/**
	 * 数値データの基数を取得します。
	 * <p>このメソッドが返す基数は、文字列から当該データ型へ変換する際に、期待する基数を返します。
	 * {@link #INTEGER} / {@link #FLOAT}は10、
	 * {@link #BASE16} / {@link #ARRAY16}は16、
	 * {@link #BASE36} / {@link #ARRAY36}は36、
	 * {@link #BASE62} / {@link #BASE} / {@link #ARRAY62} / {@link #ARRAY}は62、
	 * それ以外の型は全て0を示します。</p>
	 * @return 数値データの基数
	 */
	public final int getBase() {
		return mBase;
	}

	/**
	 * BMSデータ型を示すためのデータのネイティブなデータ型を取得します。
	 * @return ネイティブデータ型
	 */
	public final int getNativeType() {
		return mNativeType;
	}

	/**
	 * BMSデータ型が整数型であるかどうかを取得します。
	 * @return 整数型である場合はtrue
	 */
	public final boolean isIntegerType() {
		return mTypeId == TYPE_INTEGER;
	}

	/**
	 * BMSデータ型が実数型であるかどうかを取得します。
	 * @return 実数型である場合はtrue
	 */
	public final boolean isFloatType() {
		return mTypeId == TYPE_FLOAT;
	}

	/**
	 * BMSデータ型が文字列型であるかどうかを取得します。
	 * @return 文字列型である場合はtrue
	 */
	public final boolean isStringType() {
		return mTypeId == TYPE_STRING;
	}

	/**
	 * BMSデータ型が16進数値型であるかどうかを取得します。
	 * @return 16進数値型である場合はtrue
	 */
	public final boolean isBase16Type() {
		return mTypeId == TYPE_BASE16;
	}

	/**
	 * BMSデータ型が36進数値型であるかどうかを取得します。
	 * @return 36進数値型である場合はtrue
	 */
	public final boolean isBase36Type() {
		return mTypeId == TYPE_BASE36;
	}

	/**
	 * BMSデータ型が62進数値型であるかどうかを取得します。
	 * @return 62進数値型である場合はtrue
	 */
	public final boolean isBase62Type() {
		return mTypeId == TYPE_BASE62;
	}

	/**
	 * BMSデータ型が16進数値配列型であるかどうかを取得します。
	 * @return 16進数値配列型である場合はtrue
	 */
	public final boolean isArray16Type() {
		return mTypeId == TYPE_ARRAY16;
	}

	/**
	 * BMSデータ型が36進数値配列型であるかどうかを取得します。
	 * @return 36進数値配列型である場合はtrue
	 */
	public final boolean isArray36Type() {
		return mTypeId == TYPE_ARRAY36;
	}

	/**
	 * BMSデータ型が62進数値配列型であるかどうかを取得します。
	 * @return 62進数値配列型である場合はtrue
	 */
	public final boolean isArray62Type() {
		return mTypeId == TYPE_ARRAY62;
	}

	/**
	 * BMSデータ型が任意型であるかどうかを取得します。
	 * @return 任意型である場合はtrue
	 */
	public final boolean isObjectType() {
		return mTypeId == TYPE_OBJECT;
	}

	/**
	 * BMSデータ型が数値型であるかどうかを取得します。
	 * <p>{@link #INTEGER} / {@link #FLOAT}が数値型に該当します。</p>
	 * @return 数値型である場合はtrue
	 */
	public final boolean isNumberType() {
		return mIsNumberType;
	}

	/**
	 * BMSデータ型が配列型であるかどうかを取得します。
	 * <p>ネイティブデータ型が{@link #NTYPE_ARRAY NTYPE_ARRAY}を示すBMS型が該当します。</p>
	 * @return 配列型である場合はtrue
	 */
	public final boolean isArrayType() {
		return mIsArrayType;
	}

	/**
	 * BMSデータ型が値型であるかどうかを取得します。
	 * <p>BMSデータ型における「値型」とは、「配列型」「任意型」ではないデータ型全てを示します。</p>
	 * @return 値型である場合はtrue
	 */
	public final boolean isValueType() {
		return mIsValueType;
	}

	/**
	 * BMSデータ型が基数選択数値型であるかどうかを取得します。
	 * @return 基数選択数値型であればtrue
	 * @see #BASE
	 */
	public final boolean isSelectableBaseType() {
		return mTypeId == TYPE_BASE;
	}

	/**
	 * BMSデータ型が基数選択数値配列型であるかどうかを取得します。
	 * @return 基数選択数値配列型であればtrue
	 * @see #ARRAY
	 */
	public final boolean isSelectableArrayType() {
		return mTypeId == TYPE_ARRAY;
	}

	/**
	 * BMSデータ型が基数選択可能なデータ型かどうかを取得します。
	 * @return 基数選択可能なデータ型であればtrue
	 * @see #isSelectableBaseType()
	 * @see #isSelectableArrayType()
	 */
	public final boolean isSelectable() {
		return isSelectableArrayType() || isSelectableBaseType();
	}

	/**
	 * BMSデータ型が通常型であるかどうかを取得します。
	 * <p>通常型とは {@link #OBJECT} 以外の全てのデータ型が該当します。<br>
	 * つまり、{@link #isObjectType()} とは常に逆の結果を返します。</p>
	 * @return BMSデータ型が通常型である場合はtrue
	 */
	public final boolean isNormalType() {
		return mTypeId != TYPE_OBJECT;
	}

	/**
	 * 指定文字列が許容正規表現パターンにマッチするかどうかをテストします。
	 * @param data テストする文字列
	 * @return マッチする場合はtrue
	 * @exception NullPointerException dataがnull
	 */
	public final boolean test(String data) {
		assertArgNotNull(data, "data");
		return mPattern.matcher(data).matches();
	}

	/**
	 * 指定オブジェクトをBMSデータ型が示す形式に変換します。
	 * <p>変換後のデータ型は、BMSデータ型が示すネイティブデータ型によって決まります。</p>
	 * <p>このメソッドの変換処理では、BMSデータ型ごとに保有する許容正規表現パターンの影響を受けません。
	 * 変換元オブジェクトのデータ型から、BMSデータ型のネイティブデータ型への純粋なデータ変換処理を
	 * 提供するのみであることに注意してください。但し、BMSデータ型に設定された値の表現可能範囲の
	 * チェックは行われ、これに違反する場合は変換失敗となり例外がスローされます。</p>
	 * <p>任意型への変換を行った場合、変換処理は行われず、戻り値はsrcと同じ参照を返します。</p>
	 * @param src 変換元オブジェクト
	 * @return 変換後オブジェクト
	 * @exception NullPointerException srcがnull
	 * @exception ClassCastException srcの変換に失敗、または数値が表現可能範囲を超えた
	 */
	public final Object cast(Object src) {
		return cast(src, this);
	}

	/**
	 * 指定オブジェクトをBMSデータ型が示す形式に変換します。
	 * <p>変換後のデータ型は、要求データ型が示すネイティブデータ型によって決まります。</p>
	 * <p>このメソッドの変換処理では、要求データ型が保有する許容正規表現パターンの影響を受けません。
	 * 変換元オブジェクトのデータ型から、要求データ型のネイティブデータ型への純粋なデータ変換処理を
	 * 提供するのみであることに注意してください。但し、要求データ型に設定された値の表現可能範囲の
	 * チェックは行われ、これに違反する場合は変換失敗となり例外がスローされます。</p>
	 * <p>任意型への変換を行った場合、変換処理は行われず、戻り値はsrcと同じ参照を返します。</p>
	 * @param src 変換元オブジェクト
	 * @param require 要求データ型
	 * @return 変換後オブジェクト
	 * @exception NullPointerException srcがnull
	 * @exception NullPointerException requireがnull
	 * @exception ClassCastException srcの変換に失敗、またはrequireの表現可能範囲を超えた
	 */
	public static Object cast(Object src, BmsType require) {
		// アサーション
		assertArgNotNull(src, "src");
		assertArgNotNull(require, "require");

		// 変換元データの型により処理を分岐する
		var nativeType = require.getNativeType();
		Object result = null;
		Exception cause = null;
		try {
			if (src instanceof Integer) {
				result = CASTERS_INTEGER.get(nativeType).apply(require, src);
			} else if (src instanceof Long) {
				result = CASTERS_LONG.get(nativeType).apply(require, src);
			} else if (src instanceof Float) {
				result = CASTERS_FLOAT.get(nativeType).apply(require, src);
			} else if (src instanceof Double) {
				result = CASTERS_DOUBLE.get(nativeType).apply(require, src);
			} else if (src instanceof String) {
				result = CASTERS_STRING.get(nativeType).apply(require, src);
			} else if (src instanceof BmsArray) {
				result = CASTERS_ARRAY.get(nativeType).apply(require, src);
			} else if (src instanceof Number) {
				result = CASTERS_NUMBER.get(nativeType).apply(require, src);
			} else if (nativeType == NTYPE_OBJECT) {
				result = src;
			}
		} catch (Exception e) {
			// 文字列→数値変換時に発生し得る
			// 一切の例外を変換エラーとして扱う
			cause = e;
		}

		// 数値型の場合は範囲チェックを行う
		if ((require.isNumberType()) && (result instanceof Number)) {
			Number number = (Number)result;
			if (!require.mRangeChecker.test(number)) {
				throw new ClassCastException(String.format("Overflow: %s", number.toString()));
			}
		}

		// 変換できない組み合わせの場合は例外をスローする
		if (result == null) {
			castException(src, require, cause);
		}

		// 変換成功
		return result;
	}

	/**
	 * BMS配列からBMS配列への変換
	 * <p>基数選択数値配列型の場合は全基数のBMS配列を許可。そうでなければ基数が一致していることが必要。</p>
	 * @param require 変換先の配列型
	 * @param src 変換元BMS配列
	 * @return 変換後BMS配列。変換不可時はnull。
	 */
	private static BmsArray castArrayToArray(BmsType require, BmsArray src) {
		return (require.isSelectableArrayType() || (require.getBase() == src.getBase())) ? src : null;
	}

	/**
	 * ClassCastExceptionをスローする
	 * @param src キャストに失敗したデータ
	 * @param require 呼び出し元が要求したデータ型
	 * @param cause 例外をスローするに至った原因(nullの場合もある)
	 * @exception ClassCastException メソッドは必ずこの例外をスローする
	 */
	private static void castException(Object src, BmsType require, Exception cause) {
		var causeMsg = "";
		if (cause != null) {
			causeMsg = String.format(" Cause=%s", cause.toString());
		}

		var msg = String.format("Source value type: %s, can NOT cast to [NTYPE:%d].%s",
				src.getClass().getName(), require.getNativeType(), causeMsg);
		throw new ClassCastException(msg);
	}
}
