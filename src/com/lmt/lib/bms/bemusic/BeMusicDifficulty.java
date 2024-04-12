package com.lmt.lib.bms.bemusic;

/**
 * #DIFFICULTYの定義において、既知の値を示す列挙型です。
 * <p>この定義が示す文字列上の表記は、代表的なものを採用しています。</p>
 */
public enum BeMusicDifficulty {
	/** #DIFFICULTY 1 (BEGINNER)を示します。 */
	BEGINNER(1L, "BEGINNER"),

	/** #DIFFICULTY 2 (NORMAL)を示します。 */
	NORMAL(2L, "NORMAL"),

	/** #DIFFICULTY 3 (HYPER)を示します。 */
	HYPER(3L, "HYPER"),

	/** #DIFFICULTY 4 (ANOTHER)を示します。 */
	ANOTHER(4L, "ANOTHER"),

	/** #DIFFICULTY 5 (INSANE)を示します。 */
	INSANE(5L, "INSANE"),

	/**
	 * この値は#DIFFICULTYの値が未知の値であることを示します。
	 * <p>実装上の値は0としていますが、ファイルなどから値を読み込んだ際、未知の値を検出した場合プログラム上は全てこの値を示します。
	 * また、この値をBeMusicコンテンツの#DIFFICULTYにセットしてBmsSaverで外部出力を行った場合、出力される値は0となります。</p>
	 */
	OTHER(0L, "OTHER");

	/** 実際の値 */
	private long mNativeValue;
	/** 文字列表記 */
	private String mString;

	/**
	 * コンストラクタ
	 * @param nativeValue 実際の値
	 * @param str 文字列表記
	 */
	private BeMusicDifficulty(long nativeValue, String str) {
		mNativeValue = nativeValue;
		mString = str;
	}

	/**
	 * 当該列挙値が示す実際の値(#DIFFICULTYに設定される値)を取得します。
	 * @return 実際の値
	 */
	public final long getNativeValue() {
		return mNativeValue;
	}

	/**
	 * 当該列挙値が示す文字列表記を取得します。
	 * @return 文字列表記
	 */
	public final String getString() {
		return mString;
	}

	/**
	 * 実際の値に対応する列挙値を取得します。
	 * @param nativeValue 実際の値
	 * @return 列挙値
	 */
	public static BeMusicDifficulty fromNativeValue(long nativeValue) {
		if ((nativeValue & 0xffffffff00000000L) != 0) {
			return OTHER;
		} else switch ((int)nativeValue) {
		case 1: return BEGINNER;
		case 2: return NORMAL;
		case 3: return HYPER;
		case 4: return ANOTHER;
		case 5: return INSANE;
		default: return OTHER;
		}
	}
}
