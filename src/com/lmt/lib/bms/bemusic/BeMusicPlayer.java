package com.lmt.lib.bms.bemusic;

/**
 * #PLAYERの定義において、既知の値を示す列挙型です。
 *
 * @since 0.0.1
 */
public enum BeMusicPlayer {
	/**
	 * この値は#PLAYERの値が未知の値であることを示します。
	 * <p>実装上の値は0としていますが、ファイルなどから値を読み込んだ際、未知の値を検出した場合プログラム上は全てこの値を示します。
	 * また、この値をBeMusicコンテンツの#PLAYERにセットしてBmsSaverで外部出力を行った場合、出力される値は0となります。</p>
	 */
	OTHER(0L, "OTHER", false),

	/** シングルプレー(1Pの譜面を1人でプレーするモード)であることを示します。 */
	SINGLE(1L, "SINGLE", false),

	/** カップルプレー(1P/2Pで異なる配置の譜面を2人でプレーするモード)であることを示します。 */
	COUPLE(2L, "COUPLE", true),

	/** ダブルプレー(1P/2Pで異なる配置の譜面を1人でプレーするモード)であることを示します。 */
	DOUBLE(3L, "DOUBLE", true),

	/** バトルプレー(1P/2Pで同じ配置の譜面を2人でプレーするモード)であることを示します。 */
	BATTLE(4L, "BATTLE", true);

	/** 実際の値 */
	private long mNativeValue;
	/** 文字列表記 */
	private String mString;
	/** ダブルプレーかどうか */
	private boolean mIsDp;

	/**
	 * コンストラクタ
	 * @param nativeValue 実際の値
	 * @param str 文字列表記
	 * @param isDp ダブルプレーかどうか
	 */
	private BeMusicPlayer(long nativeValue, String str, boolean isDp) {
		mNativeValue = nativeValue;
		mString = str;
		mIsDp = isDp;
	}

	/**
	 * 当該列挙値が示す実際の値(#PLAYERに設定される値)を取得します。
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
	 * シングルプレーかどうかを判定します。
	 * @return true:シングルプレー false:ダブルプレー
	 */
	public boolean isSinglePlay() {
		return !mIsDp;
	}

	/**
	 * ダブルプレーかどうかを判定します。
	 * @return true:ダブルプレー false:シングルプレー
	 */
	public boolean isDoublePlay() {
		return mIsDp;
	}

	/**
	 * 実際の値に対応する列挙値を取得します。
	 * @param nativeValue 実際の値
	 * @return 列挙値
	 */
	public static BeMusicPlayer fromNativeValue(long nativeValue) {
		if ((nativeValue & 0xffffffff00000000L) != 0) {
			return OTHER;
		} else switch ((int)nativeValue) {
		case 1: return SINGLE;
		case 2: return COUPLE;
		case 3: return DOUBLE;
		case 4: return BATTLE;
		default: return OTHER;
		}
	}
}
