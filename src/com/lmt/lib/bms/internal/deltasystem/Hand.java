package com.lmt.lib.bms.internal.deltasystem;

/**
 * 手を表す列挙型
 */
public enum Hand {
	/** 左手 */
	LEFT(0, 'L'),
	/** 右手 */
	RIGHT(1, 'R');

	/** 手の数 */
	public static final int COUNT = 2;

	/** インデックスによる手のテーブル */
	private static final Hand[] TABLE = {
			LEFT, RIGHT
	};

	/** インデックス */
	private int mIndex;
	/** 手の1文字表現 */
	private char mChar;

	/**
	 * コンストラクタ
	 * @param index インデックス
	 * @param ch 手の1文字表現
	 */
	private Hand(int index, char ch) {
		mIndex = index;
		mChar = ch;
	}

	/**
	 * インデックス取得
	 * @return インデックス
	 */
	final int getIndex() {
		return mIndex;
	}

	/**
	 * 手の1文字表現取得
	 * @return 手の1文字表現
	 */
	final char getChar() {
		return mChar;
	}

	/**
	 * 左手かどうか
	 * @return 左手であればtrue
	 */
	final boolean isLeft() {
		return this == LEFT;
	}

	/**
	 * 右手かどうか
	 * @return 右手であればtrue
	 */
	final boolean isRight() {
		return this == RIGHT;
	}

	/**
	 * インデックスから手を取得
	 * @param index インデックス
	 * @return 手
	 */
	static Hand fromIndex(int index) {
		return TABLE[index];
	}
}
