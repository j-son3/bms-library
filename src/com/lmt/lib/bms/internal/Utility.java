package com.lmt.lib.bms.internal;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

/**
 * BMSライブラリ用の汎用処理クラス
 */
public class Utility {
	/**
	 * テスターが真と判定する最初の要素を検索
	 * @param <T> 比較対象の任意の型
	 * @param list 比較対象データのリスト
	 * @param tester テスター
	 * @return 見つかったデータの最初のインデックス。該当なければ-1。
	 */
	public static <T> int indexOf(List<T> list, Predicate<T> tester) {
		return indexOf(list, 0, list.size(), tester);
	}

	/**
	 * テスターが真と判定する最初の要素を検索
	 * @param <T> 比較対象の任意の型
	 * @param list 比較対象データのリスト
	 * @param start 検索開始位置
	 * @param tester テスター
	 * @return 見つかったデータの最初のインデックス。該当なければ-1。
	 */
	public static <T> int indexOf(List<T> list, int start, Predicate<T> tester) {
		return indexOf(list, start, list.size(), tester);
	}

	/**
	 * テスターが真と判定する最初の要素を検索
	 * @param <T> 比較対象の任意の型
	 * @param list 比較対象データのリスト
	 * @param start 検索開始位置
	 * @param end 検索終了位置(この位置を含まない)
	 * @param tester テスター
	 * @return 見つかったデータの最初のインデックス。該当なければ-1。
	 */
	public static <T> int indexOf(List<T> list, int start, int end, Predicate<T> tester) {
		for (var i = start; i < end; i++) {
			if (tester.test(list.get(i))) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * テスターが真と判定する最後の要素を検索
	 * @param <T> 比較対象の任意の型
	 * @param list 比較対象データのリスト
	 * @param tester テスター
	 * @return 見つかったデータの最後のインデックス。該当なければ-1。
	 */
	public static <T> int lastIndexOf(List<T> list, Predicate<T> tester) {
		return lastIndexOf(list, list.size() - 1, 0, tester);
	}

	/**
	 * テスターが真と判定する最後の要素を検索
	 * @param <T> 比較対象の任意の型
	 * @param list 比較対象データのリスト
	 * @param start 検索開始位置(この位置からリストの先頭に向かって検索)
	 * @param tester テスター
	 * @return 見つかったデータの最後のインデックス。該当なければ-1。
	 */
	public static <T> int lastIndexOf(List<T> list, int start, Predicate<T> tester) {
		return lastIndexOf(list, start, 0, tester);
	}

	/**
	 * テスターが真と判定する最後の要素を検索
	 * @param <T> 比較対象の任意の型
	 * @param list 比較対象データのリスト
	 * @param start 検索開始位置(この位置からリストの先頭に向かって検索)
	 * @param last 検索終了位置(この位置を含む)
	 * @param tester テスター
	 * @return 見つかったデータの最後のインデックス。該当なければ-1。
	 */
	public static <T> int lastIndexOf(List<T> list, int start, int last, Predicate<T> tester) {
		for (var i = start; i >= last; i--) {
			if (tester.test(list.get(i))) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * バイナリサーチによる指定値以下の値を検索
	 * @param <L> 検索対象リスト型
	 * @param <T> 検索対象データ型
	 * @param list 検索対象リスト
	 * @param comparator 値比較関数
	 * @return 指定値以下で最も大きい要素のインデックス、該当なければ-1
	 */
	public static <L extends List<T>, T> int bsearchFloor(L list, ToIntFunction<T> comparator) {
		var r = -1;
		var elm = (T)null;
		for (int l = 0, h = list.size() - 1, m = h / 2; l <= h; m = l + (h - l) / 2) {
			// 比較要素と評価値を比較する
			elm = list.get(m);
			var cr = comparator.applyAsInt(elm);

			// 比較結果による処理の分岐
			if (cr == 0) {
				// 比較要素が評価値と同じ場合、この要素で確定
				r = m;
				break;
			} else if (cr > 0) {
				// 比較要素＞評価値の場合、比較対象要素以前の要素は全て対象外
				h = m - 1;
			} else if (h != m) {
				// 比較要素＜評価値で、最終要素ではない場合、暫定でこの要素を該当要素とする
				r = m;
				l = m + 1;
			} else {
				// 比較要素＜評価値で、最終要素の場合、この要素で確定
				r = m;
				break;
			}
		}
		return r;
	}

	/**
	 * バイナリサーチによる指定値以上の値を検索
	 * @param <L> 検索対象リスト型
	 * @param <T> 検索対象データ型
	 * @param list 検索対象リスト
	 * @param comparator 値比較関数
	 * @return 指定値以上で最も小さい要素のインデックス、該当なければ-1
	 */
	public static <L extends List<T>, T> int bsearchCeil(L list, ToIntFunction<T> comparator) {
		var r = -1;
		var elm = (T)null;
		for (int l = 0, h = list.size() - 1, m = h / 2; l <= h; m = l + (h - l) / 2) {
			// 比較要素と評価値を比較する
			elm = list.get(m);
			var cr = comparator.applyAsInt(elm);

			// 比較結果による処理の分岐
			if (cr == 0) {
				// 比較要素が評価値と同じ場合、この要素で確定
				r = m;
				break;
			} else if (cr < 0) {
				// 比較要素＜評価値の場合、比較対象要素以前の要素は全て対象外
				l = m + 1;
			} else if (h != m) {
				// 比較要素＞評価値で、最終要素ではない場合、暫定でこの要素を該当要素とする
				r = m;
				h = m - 1;
			} else {
				// 比較要素＞評価値で、最終要素の場合、この要素で確定
				r = m;
				break;
			}
		}
		return r;
	}

	/**
	 * 小数部の有無判定
	 * @param value 判定対象の値
	 * @return 小数部があればtrue、なければfalse
	 */
	public static boolean hasDecimal(double value) {
		return hasDecimal(value, 0.0);
	}

	/**
	 * 小数部の有無判定(差異指定付き)
	 * @param value 判定対象の値
	 * @param delta 差異(小数部の値がこの値以下の場合は「小数部なし」と判定)
	 * @return 小数部があればtrue、なければfalse
	 */
	public static boolean hasDecimal(double value, double delta) {
		return Math.abs(value - (double)Math.round(value)) > delta;
	}

	/**
	 * 2つの値の最大公約数を求める
	 * @param a 値1
	 * @param b 値2
	 * @return aとbの最大公約数
	 */
	public static int gcd(int a, int b) {
		return (b == 0) ? a : gcd(b, a % b);
	}

	/**
	 * 2つの値の最大公約数を求める
	 * @param a 値1
	 * @param b 値2
	 * @return aとbの最大公約数
	 */
	public static long gcd(long a, long b) {
		return (b == 0) ? a : gcd(b, a % b);
	}

	/**
	 * 指定された値の最大公約数を求める
	 * @param nums 値リスト
	 * @return numsの最大公約数
	 */
	public static int gcd(int...nums) {
		int len = nums.length, result = (len == 0) ? 0 : nums[0];
		for (var i = 1; i < len; i++) { result = gcd(result, nums[i]); }
		return result;
	}

	/**
	 * 指定された値の最大公約数を求める
	 * @param nums 値リスト
	 * @return numsの最大公約数
	 */
	public static long gcd(long...nums) {
		long len = nums.length, result = (len == 0) ? 0 : nums[0];
		for (var i = 1; i < len; i++) { result = gcd(result, nums[i]); }
		return result;
	}

	/**
	 * 指定BPMの時、指定刻みだけ進行するのに必要になる時間(sec)を計算
	 * <p>※BPMは0より大きい値を指定すること</p>
	 * @param tick 刻み数
	 * @param bpm BPM
	 * @return 時間(sec)
	 */
	public static double computeTime(double tick, double bpm) {
		return 1.25 * tick / bpm;

		// 計算式の解説
		// time(sec) = 1.25 * tick / bpm  ※bpm > 0
		// bpm = (1.25 * tick) / time(sec)
	}

	/**
	 * 指定BPMの時、指定時間(sec)進行した際の刻み数を計算
	 * @param timeSec 時間(sec)
	 * @param bpm BPM
	 * @return 刻み数
	 */
	public static double computeTick(double timeSec, double bpm) {
		return 0.8 * timeSec * bpm;

		// 計算式の解説
		// time = (tick / 48) * (60 / bpm)
		// time = (60 * tick) / (48 * bpm)
		// time = (5 * tick) / (4 * bpm)
		// (5 * tick) = time * (4 * bpm)
		// tick = (4 * time * bpm) / 5
		// tick = 0.8 * time * bpm ★
	}
}
