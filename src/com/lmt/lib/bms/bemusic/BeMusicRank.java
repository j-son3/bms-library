package com.lmt.lib.bms.bemusic;

/**
 * #RANKの定義において、既知の値を示す列挙型です。
 *
 * @since 0.0.1
 */
public enum BeMusicRank {
	/** 判定ランクが4 (VERY EASY)であることを示します。 */
	VERY_EASY(4L, "VERY EASY"),

	/** 判定ランクが3 (EASY)であることを示します。 */
	EASY(3L, "EASY"),

	/** 判定ランクが2 (NORMAL)であることを示します。 */
	NORMAL(2L, "NORMAL"),

	/** 判定ランクが1 (HARD)であることを示します。 */
	HARD(1L, "HARD"),

	/** 判定ランクが0 (VERY HARD)であることを示します。 */
	VERY_HARD(0L, "VERY HARD"),

	/**
	 * この値は#RANKの値が未知の値であることを示します。
	 * <p>実装上の値は-1としていますが、ファイルなどから値を読み込んだ際、未知の値を検出した場合プログラム上は全てこの値を示します。
	 * また、この値をBeMusicコンテンツの#PLAYERにセットしてBmsSaverで外部出力を行った場合、出力される値は-1となります。</p>
	 */
	OTHER(-1L, "OTHER");

	/** 実際の値 */
	private long mNativeValue;
	/** 文字列表記 */
	private String mString;

	/**
	 * コンストラクタ
	 * @param nativeValue 実際の値
	 * @param str 文字列表記
	 */
	private BeMusicRank(long nativeValue, String str) {
		mNativeValue = nativeValue;
		mString = str;
	}

	/**
	 * 当該列挙値が示す実際の値(#RANKに設定される値)を取得します。
	 * @return 実際の値
	 */
	public long getNativeValue() {
		return mNativeValue;
	}

	/**
	 * 当該列挙値が示す文字列表記を取得します。
	 * @return 文字列表記
	 */
	public String getString() {
		return mString;
	}

	/**
	 * 実際の値に対応する列挙値を取得します。
	 * @param nativeValue 実際の値
	 * @return 列挙値
	 */
	public static BeMusicRank fromNativeValue(long nativeValue) {
		if ((nativeValue & 0xffffffff00000000L) != 0) {
			return OTHER;
		} else switch ((int)nativeValue) {
		case 0: return VERY_HARD;
		case 1: return HARD;
		case 2: return NORMAL;
		case 3: return EASY;
		case 4: return VERY_EASY;
		default: return OTHER;
		}
	}
}
