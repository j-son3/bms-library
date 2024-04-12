package com.lmt.lib.bms.internal;

import com.lmt.lib.bms.BmsSpec;

/**
 * Assertionクラスでは、BMSライブラリにおけるアサーションの処理をとりまとめる。
 * 当クラスは外部パッケージへは非公開であり、BMSライブラリ特有の必要な処理がまとめられている。
 * 各クラスは当クラスを静的インポートし、必要な各種メソッドを呼び出す。
 * アサーションに失敗した場合、メソッドごとに定められた例外をスローする。これらの例外は基本的には
 * RuntimeExceptionを継承した例外となっている。
 */
public final class Assertion {
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
	 * 引数の汎用アサーション
	 * @param success 評価式の結果
	 * @param msgFormat アサーション失敗時のメッセージ書式
	 * @param arg1 メッセージ引数1
	 * @exception IllegalArgumentException アサーションに失敗した
	 */
	public static void assertArg(boolean success, String msgFormat, int arg1) {
		if (!success) {
			throw new IllegalArgumentException(String.format(msgFormat, arg1));
		}
	}

	/**
	 * 引数の汎用アサーション
	 * @param success 評価式の結果
	 * @param msgFormat アサーション失敗時のメッセージ書式
	 * @param arg1 メッセージ引数1
	 * @param arg2 メッセージ引数2
	 * @exception IllegalArgumentException アサーションに失敗した
	 */
	public static void assertArg(boolean success, String msgFormat, int arg1, int arg2) {
		if (!success) {
			throw new IllegalArgumentException(String.format(msgFormat, arg1, arg2));
		}
	}

	/**
	 * 引数の汎用アサーション
	 * @param success 評価式の結果
	 * @param msgFormat アサーション失敗時のメッセージ書式
	 * @param arg1 メッセージ引数1
	 * @param arg2 メッセージ引数2
	 * @param arg3 メッセージ引数3
	 * @exception IllegalArgumentException アサーションに失敗した
	 */
	public static void assertArg(boolean success, String msgFormat, int arg1, int arg2, int arg3) {
		if (!success) {
			throw new IllegalArgumentException(String.format(msgFormat, arg1, arg2, arg3));
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
			throw new NullPointerException(String.format("Argument '%s' is null.", argName));
		}
	}

	/**
	 * 引数の値の範囲をテストするアサーション
	 * @param value テストする引数の値
	 * @param min 許容最小値
	 * @param max 許容最大値
	 * @param valueName 引数の名前
	 * @exception IllegalArgumentException 引数の値が許容範囲外だった
	 */
	public static void assertArgRange(int value, int min, int max, String valueName) {
		if ((value < min) || (value > max)) assertArgRangeFail(value, min, max, valueName);
	}

	/**
	 * 引数の値の範囲をテストするアサーション
	 * @param value テストする引数の値
	 * @param min 許容最小値
	 * @param max 許容最大値
	 * @param valueName 引数の名前
	 * @exception IllegalArgumentException 引数の値が許容範囲外だった
	 */
	public static void assertArgRange(long value, long min, long max, String valueName) {
		if ((value < min) || (value > max)) assertArgRangeFail(value, min, max, valueName);
	}

	/**
	 * 引数の値の範囲をテストするアサーション
	 * @param value テストする引数の値
	 * @param min 許容最小値
	 * @param max 許容最大値
	 * @param valueName 引数の名前
	 * @exception IllegalArgumentException 引数の値が許容範囲外だった
	 */
	public static void assertArgRange(float value, float min, float max, String valueName) {
		if ((value < min) || (value > max)) assertArgRangeFail(value, min, max, valueName);
	}

	/**
	 * 引数の値の範囲をテストするアサーション
	 * @param value テストする引数の値
	 * @param min 許容最小値
	 * @param max 許容最大値
	 * @param valueName 引数の名前
	 * @exception IllegalArgumentException 引数の値が許容範囲外だった
	 */
	public static void assertArgRange(double value, double min, double max, String valueName) {
		if ((value < min) || (value > max)) assertArgRangeFail(value, min, max, valueName);
	}

	/**
	 * IllegalArgumentExceptionをスローする
	 * @param value テストする引数の値
	 * @param min 許容最小値
	 * @param max 許容最大値
	 * @param valueName 引数の名前
	 */
	private static <T> void assertArgRangeFail(T value, T min, T max, String valueName) {
		var msg = String.format("Argument '%s' is out of range. expect=(%s-%s), actual=%s", valueName, min, max, value);
		throw new IllegalArgumentException(msg);
	}

	/**
	 * 引数のインデックス値の範囲をテストするアサーション
	 * @param index インデックス
	 * @param count 要素の最大数
	 * @param argName 引数の名前
	 * @exception IndexOutOfBoundsException インデックスが範囲外
	 */
	public static void assertArgIndexRange(int index, int count, String argName) {
		if ((index < 0) || (index >= count)) {
			var msg = String.format("Argument '%s' is out of range. 0 <= index < %d, but %d.", argName, count, index);
			throw new IndexOutOfBoundsException(msg);
		}
	}

	/**
	 * 小節データ・ノートを登録可能なチャンネルの範囲をテストするアサーション
	 * @param channel チャンネル番号
	 * @exception IllegalArgumentException 小節データ・ノートを登録できないチャンネル番号
	 */
	public static void assertChannelRange(int channel) {
		if ((channel < BmsSpec.CHANNEL_MIN) || (channel > BmsSpec.CHANNEL_MAX)) {
			var msg = String.format("Channel number is out of range. expect=(%d-%d), actual=%d",
					BmsSpec.CHANNEL_MIN, BmsSpec.CHANNEL_MAX, channel);
			throw new IllegalArgumentException(msg);
		}
	}

	/**
	 * チャンネル番号が仕様チャンネルの範囲かどうかをテストするアサーション
	 * @param channel チャンネル番号
	 * @exception IllegalArgumentException チャンネル番号が仕様チャンネルではない
	 */
	public static void assertSpecChannelRange(int channel) {
		if ((channel < BmsSpec.SPEC_CHANNEL_MIN) || (channel > BmsSpec.SPEC_CHANNEL_MAX)) {
			var msg = String.format("Channel number is NOT range of spec channel. expect=(%d-%d), actual=%d",
					BmsSpec.SPEC_CHANNEL_MIN, BmsSpec.SPEC_CHANNEL_MAX, channel);
			throw new IllegalArgumentException(msg);
		}
	}

	/**
	 * チャンネル番号がユーザーチャンネルの範囲かどうかをテストするアサーション
	 * @param channel チャンネル番号
	 * @exception IllegalArgumentException チャンネル番号がユーザーチャンネルではない
	 */
	public static void assertUserChannelRange(int channel) {
		if ((channel < BmsSpec.USER_CHANNEL_MIN) || (channel > BmsSpec.USER_CHANNEL_MAX)) {
			var msg = String.format("Channel number is NOT range of user channel. expect=(%d-%d), actual=%d",
					BmsSpec.USER_CHANNEL_MIN, BmsSpec.USER_CHANNEL_MAX, channel);
			throw new IllegalArgumentException(msg);
		}
	}

	/**
	 * 小節データ・ノートを登録可能なチャンネルの範囲(終端)をテストするアサーション
	 * @param channel チャンネル番号
	 * @exception IllegalArgumentException 小節データ・ノートを登録できないチャンネル番号
	 */
	public static void assertEndChannelRange(int channel) {
		final var min = BmsSpec.CHANNEL_MIN;
		final var max = BmsSpec.CHANNEL_MAX + 1;
		if ((channel < min) || (channel > max)) {
			var msg = String.format("Channel number (End) is out of range. expect=(%d-%d), actual=%d",
					min, max, channel);
			throw new IllegalArgumentException(msg);
		}
	}

	/**
	 * BMSコンテンツに設定可能な小節番号の範囲をテストするアサーション
	 * @param measure 小節番号
	 * @exception IllegalArgumentException 小節番号が{@link BmsSpec#MEASURE_MIN}未満、または{@link BmsSpec#MEASURE_MAX}超過
	 */
	public static void assertArgMeasureWithinRange(int measure) {
		assertArgMeasureWithinRange(measure, BmsSpec.MEASURE_MAX, Double.MIN_VALUE);
	}

	/**
	 * BMS仕様としてBMSデータに記述可能な小節番号の範囲をテストするアサーション
	 * @param measure 小節番号
	 * @param measureMax 小節番号最大値
	 * @exception IllegalArgumentException 小節番号が{@link BmsSpec#MEASURE_MIN}未満、または小節番号最大値超過
	 */
	public static void assertArgMeasureWithinRange(int measure, int measureMax) {
		assertArgMeasureWithinRange(measure, measureMax, Double.MIN_VALUE);
	}

	/**
	 * BMS仕様としてBMSデータに記述可能な小節番号の範囲をテストするアサーション
	 * @param measure 小節番号
	 * @param measureMax 小節番号最大値
	 * @param tick 刻み位置
	 * @exception IllegalArgumentException 小節番号が{@link BmsSpec#MEASURE_MIN}未満、または小節番号最大値超過
	 */
	public static void assertArgMeasureWithinRange(int measure, int measureMax, double tick) {
		if ((measure < BmsSpec.MEASURE_MIN) || (measure > measureMax)) {
			var fmt = "";
			if (tick == Double.MIN_VALUE) {
				fmt = "Argument 'measure' is out of range. expect=(%d-%d), actual=%d";
			} else {
				fmt = "Argument 'measure' is out of range. expect-measure=(%d-%d), actual-measure=%d, tick=%.16g";
			}
			throw new IllegalArgumentException(String.format(fmt, BmsSpec.MEASURE_MIN, measureMax, measure, tick));
		}
	}

	/**
	 * 小節の刻み位置の範囲をテストするアサーション
	 * @param tick 刻み位置
	 * @param measure 小節番号
	 * @exception IllegalArgumentException 刻み位置が{@link BmsSpec#TICK_MIN}未満、または{@link BmsSpec#TICK_MAX}超過
	 */
	public static void assertArgTickWithinRange(double tick, int measure) {
		assertArgTickWithinRange(tick, BmsSpec.TICK_MAX, measure);
	}

	/**
	 * 小節の刻み位置の範囲をテストするアサーション
	 * @param tick 刻み位置
	 * @param tickMax 刻み位置最大値
	 * @param measure 小節番号
	 * @exception IllegalArgumentException 刻み位置が{@link BmsSpec#TICK_MIN}未満、または刻み位置最大値超過
	 */
	public static void assertArgTickWithinRange(double tick, double tickMax, int measure) {
		if ((tick < BmsSpec.TICK_MIN) || (tick > tickMax)) {
			var msg = String.format(
					"Argument 'tick' is out of range. measure=%d, expect-tick=(%.16g-%.16g), actual-tick=%.16g",
					measure, BmsSpec.TICK_MIN, tickMax, tick);
			throw new IllegalArgumentException(msg);
		}
	}

	/**
	 * ノートの値の範囲をテストするアサーション
	 * @param value ノートの値
	 * @exception IllegalArgumentException valueが0
	 * @exception IllegalArgumentException valueが{@link BmsSpec#VALUE_MIN}未満、または{@link BmsSpec#VALUE_MAX}超過
	 */
	public static void assertValue(int value) {
		if (value == 0) {
			throw new IllegalArgumentException("Can't specify 0 as the note value.");
		} else if ((value < BmsSpec.VALUE_MIN) || (value > BmsSpec.VALUE_MAX)) {
			var msg = String.format("Note value is out of range. expect=(%d-%d), actual=%d",
					BmsSpec.VALUE_MIN, BmsSpec.VALUE_MAX, value);
			throw new IllegalArgumentException(msg);
		}
	}

	/**
	 * クラスフィールドの内容をテストする汎用アサーション
	 * @param success クラスフィールドをテストする評価式の結果
	 * @param format アサーション失敗時のメッセージ書式
	 * @param args メッセージの引数
	 * @exception IllegalStateException アサーションに失敗した
	 */
	public static void assertField(boolean success, String format, Object...args) {
		if (!success) {
			throw new IllegalStateException(String.format(format, args));
		}
	}
}
