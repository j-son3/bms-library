package com.lmt.lib.bms;

import static com.lmt.lib.bms.BmsAssertion.*;

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
 * <p><b>データ型一覧</b><br>
 * データ型はBMSライブラリ側で規定されており、下記が全てです。<br>
 * 「データ型名(実際のデータ型名)」で表記します。</p>
 *
 * <ul>
 * <li>{@link #INTEGER}：整数型(Long)</li>
 * <li>{@link #NUMERIC}：実数型(Double)</li>
 * <li>{@link #STRING}：文字列型(String)</li>
 * <li>{@link #BASE16}：16進数整数型(Long)</li>
 * <li>{@link #BASE36}：36進数整数型(Long)</li>
 * <li>{@link #ARRAY16}：16進数整数配列型({@link BmsArray})</li>
 * <li>{@link #ARRAY36}：36進数整数配列型({@link BmsArray})</li>
 * <li>{@link #OBJECT}：任意型(Object)</li>
 * </ul>
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
	public static final BmsType NUMERIC = new BmsType(
			"NUMERIC",
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
			"BASE36",
			"^[a-zA-Z0-9]{2}$",
			36,
			NTYPE_LONG,
			n -> { long v = n.longValue(); return (v >= 0) && (v <= 1295); });

	/**
	 * 16進数値配列型
	 * <p>このデータ型は、0個以上の{@link #BASE16 BASE16}型データを並べた配列です。内部的には
	 * {@link BmsArray}として表現されます。</p>
	 */
	public static final BmsType ARRAY16 = new BmsType(
			"ARRAY16",
			"^([a-fA-F0-9]{2})*$",
			16,
			NTYPE_ARRAY,
			null);

	/**
	 * 36進数値配列型
	 * <p>このデータ型は、0個以上の{@link #BASE36 BASE36}型データを並べた配列です。内部的には
	 * {@link BmsArray}として表現されます。</p>
	 */
	public static final BmsType ARRAY36 = new BmsType(
			"ARRAY36",
			"^([a-zA-Z0-9]{2})*$",
			36,
			NTYPE_ARRAY,
			null);

	/**
	 * 任意型
	 * <p>このデータ型には、あらゆるデータ型を設定可能です。但し、任意型はユーザーメタ情報、ユーザーチャンネルに対してのみ指定可能です。
	 * この制約に違反したBMS仕様は作成することが出来ません。そのため、{@link BmsLoader}, {@link BmsSaver}での入出力には対応していません。</p>
	 */
	public static final BmsType OBJECT = new BmsType(
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
		return new BmsType("REGEX", pattern, 0, NTYPE_STRING, null);
	}

	/** データ型の名称 */
	private String mName;
	/** 当該型が認識可能な書式の正規表現パターン */
	private Pattern mPattern;
	/** 基数 */
	private int mRadix;
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
		CASTERS_STRING.add((t, o) -> Long.valueOf((String)o, t.getRadix()));  // To Long
		CASTERS_STRING.add((t, o) -> Double.valueOf((String)o));              // To Double
		CASTERS_STRING.add((t, o) -> o);                                       // To String
		CASTERS_STRING.add((t, o) -> new BmsArray((String)o, t.getRadix()));   // To BmsArray
		CASTERS_STRING.add((t, o) -> o);                                       // To Object

		// BmsArray caster
		CASTERS_ARRAY.add((t, o) -> null);                                                 // To Long
		CASTERS_ARRAY.add((t, o) -> null);                                                 // To Double
		CASTERS_ARRAY.add((t, o) -> o.toString());                                         // To String
		CASTERS_ARRAY.add((t, o) -> t.getRadix() == ((BmsArray)o).getRadix() ? o : null);  // To BmsArray
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
	 * @param name データ型の名称
	 * @param pattern 書式の正規表現パターン
	 * @param radix 基数
	 * @param nativeType ネイティブデータ型
	 * @exception NullPointerException patternがnull
	 * @exception PatternSyntaxException 表現の構文が無効である場合
	 */
	BmsType(String name, String pattern, int radix, int nativeType, Predicate<Number> rangeChecker) {
		assertArgNotNull(pattern, "pattern");
		mName = name;
		mPattern = Pattern.compile(pattern);
		mRadix = radix;
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
	 * {@link #INTEGER INTEGER} / {@link #NUMERIC NUMERIC}は10、
	 * {@link #BASE16 BASE16} / {@link #ARRAY16 ARRAY16}は16、
	 * {@link #BASE36 BASE36} / {@link #ARRAY36 ARRAY36}は36を返し、
	 * それ以外の型は全て0を示します。</p>
	 * @return 数値データの基数
	 */
	public final int getRadix() {
		return mRadix;
	}

	/**
	 * BMSデータ型を示すためのデータのネイティブなデータ型を取得します。
	 * @return ネイティブデータ型
	 */
	public final int getNativeType() {
		return mNativeType;
	}

	/**
	 * BMSデータ型が数値型であるかどうかを取得します。
	 * <p>{@link #INTEGER} / {@link #NUMERIC}が数値型に該当します。</p>
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
