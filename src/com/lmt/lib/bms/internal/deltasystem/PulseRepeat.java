package com.lmt.lib.bms.internal.deltasystem;

/**
 * リズムリピートを表すクラス。
 */
class PulseRepeat {
	/** リピート回数 */
	int repeatCount = 1;
	/** リピート1回あたりのリズム範囲数 */
	int patternCount = 1;
	/** リピート対象の先頭リズム範囲 */
	PulseRange firstRange;
	/** リピート対象の末尾リズム範囲 */
	PulseRange lastRange;

	/**
	 * コンストラクタ
	 * @param first リピート対象の先頭リズム範囲
	 */
	PulseRepeat(PulseRange first) {
		firstRange = first;
		lastRange = first;
	}

	/**
	 * リピートしているかどうか判定
	 * @return リピートしている(リピート2回以上)の場合true
	 */
	final boolean hasRepeat() {
		return repeatCount > 1;
	}
}
