package com.lmt.lib.bms.bemusic;

/**
 * Assertionクラスでは、一般的なアサーションの処理をとりまとめる。
 * 各クラスは当クラスを静的インポートし、必要な各種メソッドを呼び出す。
 * アサーションに失敗した場合、メソッドごとに定められた例外をスローする。これらの例外は基本的には
 * RuntimeExceptionを継承した例外となっている。
 */
/*public*/ final class Assertion {
	/**
	 * 引数の汎用アサーション
	 * @param success 評価式の結果
	 * @param msgFormat アサーション失敗時のメッセージ書式
	 * @param args メッセージ引数
	 * @exception IllegalArgumentException アサーションに失敗した
	 */
	public static void assertArg(boolean success, String msgFormat, Object...args) {
		if (!success) {
			throw new IllegalArgumentException(String.format(msgFormat, args));
		}
	}

	/**
	 * 引数がnullではないことをテストするアサーション
	 * @param arg nullチェックを行う引数
	 * @param argName 引数の名前
	 * @exception NullPointerException 引数がnullだった
	 */
	public static void assertArgNotNull(Object arg, String argName) {
		if (arg == null) {
			throw new NullPointerException(String.format("Argument[%s] is null.", argName));
		}
	}

	/**
	 * 引数の値の範囲をテストするアサーション
	 * @param value テストする引数の値
	 * @param min 許容最小値
	 * @param max 許容最大値
	 * @param valueName 引数の名前
	 * @exception IndexOutOfBoundsException 引数の値が許容範囲外だった
	 */
	public static void assertArgRange(int value, int min, int max, String valueName) {
		if ((value < min) || (value > max)) assertArgRangeFail(valueName);
	}

	/**
	 * 引数の値の範囲をテストするアサーション
	 * @param value テストする引数の値
	 * @param min 許容最小値
	 * @param max 許容最大値
	 * @param valueName 引数の名前
	 * @exception IndexOutOfBoundsException 引数の値が許容範囲外だった
	 */
	public static void assertArgRange(long value, long min, long max, String valueName) {
		if ((value < min) || (value > max)) assertArgRangeFail(valueName);
	}

	/**
	 * 引数の値の範囲をテストするアサーション
	 * @param value テストする引数の値
	 * @param min 許容最小値
	 * @param max 許容最大値
	 * @param valueName 引数の名前
	 * @exception IndexOutOfBoundsException 引数の値が許容範囲外だった
	 */
	public static void assertArgRange(float value, float min, float max, String valueName) {
		if ((value < min) || (value > max)) assertArgRangeFail(valueName);
	}

	/**
	 * 引数の値の範囲をテストするアサーション
	 * @param value テストする引数の値
	 * @param min 許容最小値
	 * @param max 許容最大値
	 * @param valueName 引数の名前
	 * @exception IndexOutOfBoundsException 引数の値が許容範囲外だった
	 */
	public static void assertArgRange(double value, double min, double max, String valueName) {
		if ((value < min) || (value > max)) assertArgRangeFail(valueName);
	}

	/**
	 * IndexOutOfBoundsExceptionをスローする
	 * @param valueName 引数の名前
	 */
	private static void assertArgRangeFail(String valueName) {
		var msg = String.format("%s is out of range.", valueName);
		throw new IndexOutOfBoundsException(msg);
	}

	/**
	 * 引数の値の範囲をテストするアサーション。
	 * @param value テストする引数の値
	 * @param min 許容最小値
	 * @param max 許容最大値
	 * @param valueName 引数の名前
	 * @exception IllegalArgumentException 引数の値が許容範囲外
	 */
	public static void assertArgValueRange(byte value, byte min, byte max, String valueName) {
		if ((value < min) || (value > max)) assertArgValueRangeFail(value, min, max, valueName);
	}
	/**
	 * 引数の値の範囲をテストするアサーション。
	 * @param value テストする引数の値
	 * @param min 許容最小値
	 * @param max 許容最大値
	 * @param valueName 引数の名前
	 * @exception IllegalArgumentException 引数の値が許容範囲外
	 */
	public static void assertArgValueRange(short value, short min, short max, String valueName) {
		if ((value < min) || (value > max)) assertArgValueRangeFail(value, min, max, valueName);
	}
	/**
	 * 引数の値の範囲をテストするアサーション。
	 * @param value テストする引数の値
	 * @param min 許容最小値
	 * @param max 許容最大値
	 * @param valueName 引数の名前
	 * @exception IllegalArgumentException 引数の値が許容範囲外
	 */
	public static void assertArgValueRange(int value, int min, int max, String valueName) {
		if ((value < min) || (value > max)) assertArgValueRangeFail(value, min, max, valueName);
	}
	/**
	 * 引数の値の範囲をテストするアサーション。
	 * @param value テストする引数の値
	 * @param min 許容最小値
	 * @param max 許容最大値
	 * @param valueName 引数の名前
	 * @exception IllegalArgumentException 引数の値が許容範囲外
	 */
	public static void assertArgValueRange(long value, long min, long max, String valueName) {
		if ((value < min) || (value > max)) assertArgValueRangeFail(value, min, max, valueName);
	}
	/**
	 * 引数の値の範囲をテストするアサーション。
	 * @param value テストする引数の値
	 * @param min 許容最小値
	 * @param max 許容最大値
	 * @param valueName 引数の名前
	 * @exception IllegalArgumentException 引数の値が許容範囲外
	 */
	public static void assertArgValueRange(float value, float min, float max, String valueName) {
		if ((value < min) || (value > max)) assertArgValueRangeFail(value, min, max, valueName);
	}
	/**
	 * 引数の値の範囲をテストするアサーション。
	 * @param value テストする引数の値
	 * @param min 許容最小値
	 * @param max 許容最大値
	 * @param valueName 引数の名前
	 * @exception IllegalArgumentException 引数の値が許容範囲外
	 */
	public static void assertArgValueRange(double value, double min, double max, String valueName) {
		if ((value < min) || (value > max)) assertArgValueRangeFail(value, min, max, valueName);
	}

	/**
	 * assertArgValueRange()のアサーション失敗時の例外スロー。
	 * @param <T> 数値型
	 * @param value 値
	 * @param min 最小値
	 * @param max 最大値
	 * @param valueName 値の名称
	 * @exception IllegalArgumentException アサーション失敗
	 */
	private static <T extends Number> void assertArgValueRangeFail(T value, T min, T max, String valueName) {
		var msg = String.format("Argument[%s] is out of range: expect(%s-%s) actual(%s)", valueName, min, max, value);
		throw new IllegalArgumentException(msg);
	}

	/**
	 * 引数のインデックス値の範囲をテストするアサーション
	 * @param value テストする引数のインデックス値
	 * @param count 要素数
	 * @param valueName 引数の名前
	 * @exception IndexOutOfBoundsException 引数のインデックス値が許容範囲外だった
	 */
	public static void assertArgIndexRange(int value, int count, String valueName) {
		if ((value < 0) || (value >= count)) assertArgIndexRangeFail(value, count, valueName);
	}

	/**
	 * 引数のインデックス値の範囲をテストするアサーション
	 * @param value テストする引数のインデックス値
	 * @param count 要素数
	 * @param valueName 引数の名前
	 * @exception IndexOutOfBoundsException 引数のインデックス値が許容範囲外だった
	 */
	public static void assertArgIndexRange(long value, long count, String valueName) {
		if ((value < 0) || (value >= count)) assertArgIndexRangeFail(value, count, valueName);
	}

	/**
	 * IndexOutOfBoundsExceptionをスローする
	 * @param value 引数の値
	 * @param count 要素数
	 * @param valueName 引数の名前
	 */
	private static <T extends Number> void assertArgIndexRangeFail(T value, T count, String valueName) {
		var msg = String.format("Argument[%s] is out of range: count(%d) index(%s)", valueName, count, value);
		throw new IndexOutOfBoundsException(msg);
	}

	/**
	 * オブジェクトの状態をテストする汎用アサーション
	 * @param success クラスフィールドをテストする評価式の結果
	 * @param format アサーション失敗時のメッセージ書式
	 * @param args メッセージの引数
	 * @exception IllegalStateException アサーションに失敗した
	 */
	public static void assertState(boolean success, String format, Object...args) {
		if (!success) throw new IllegalStateException(String.format(format, args));
	}
}
