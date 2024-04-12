package com.lmt.lib.bms.internal;

import java.util.List;
import java.util.function.Predicate;

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
}
