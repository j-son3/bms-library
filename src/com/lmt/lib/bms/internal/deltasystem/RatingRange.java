package com.lmt.lib.bms.internal.deltasystem;

import java.util.Collection;
import java.util.List;

/**
 * 範囲データのベースクラス
 * @param <R> 範囲データの具象クラス
 */
abstract class RatingRange<R> {
	/** この範囲データの前の範囲データ */
	R prev = null;
	/** この範囲データの次の範囲データ */
	R next = null;
	/** 範囲データの要素データリストの最初のインデックス */
	int first;
	/** 範囲データの要素データリストの最後のインデックス */
	int last;

	/**
	 * コンストラクタ
	 */
	protected RatingRange() {
		// Do nothing
	}

	/**
	 * コンストラクタ
	 * @param first 範囲データの要素データリストの最初のインデックス
	 * @param last 範囲データの要素データリストの最後のインデックス
	 */
	protected RatingRange(int first, int last) {
		this.first = first;
		this.last = last;
	}

	/**
	 * 指定位置がこの範囲データ内の位置を示しているか判定
	 * @param pos 位置(要素データリストのインデックス)
	 * @return 指定位置がこの範囲データ内の位置を示していればtrue
	 */
	final boolean contains(int pos) {
		return (pos >= first) && (pos <= last);
	}

	/**
	 * 範囲データが最小(最初と最後のインデックスが同一)か判定
	 * @return 範囲データが最小であればtrue
	 */
	final boolean isOnePoint() {
		return first == last;
	}

	/**
	 * この範囲データが先頭かどうか判定
	 * @return 範囲データが先頭であればtrue
	 */
	final boolean isHead() {
		return prev == null;
	}

	/**
	 * この範囲データが末尾かどうか判定
	 * @return 範囲データが末尾であればtrue
	 */
	final boolean isTail() {
		return next == null;
	}

	/**
	 * 前の範囲データがあるか判定
	 * @return 前の範囲データがあればtrue
	 */
	final boolean hasPrev() {
		return prev != null;
	}

	/**
	 * 次の範囲データがあるか判定
	 * @return 次の範囲データがあればtrue
	 */
	final boolean hasNext() {
		return next != null;
	}

	/**
	 * 範囲データの時間計算
	 * @param elems 要素データリスト
	 * @return 範囲データの時間
	 */
	final double time(List<? extends RatingElement> elems) {
		return RatingElement.timeDelta(elems, first, last);
	}

	/**
	 * 範囲データの双方向リンク設定
	 * @param <R> 範囲データの具象クラス
	 * @param ranges 範囲データコレクション
	 */
	static <R> void link(Collection<? extends RatingRange<R>> ranges) {
		var prev = (RatingRange<R>)null;
		for (var range : ranges) {
			link(range, prev);
			prev = range;
		}
	}

	/**
	 * 範囲データ同士の双方向リンク設定
	 * @param <R> 範囲データの具象クラス
	 * @param cur 設定対象範囲データ
	 * @param prev curの前に位置する範囲データ
	 */
	@SuppressWarnings("unchecked")
	static <R> void link(RatingRange<R> cur, RatingRange<R> prev) {
		cur.prev = (R)prev;
		if (prev != null) {
			prev.next = (R)cur;
		}
	}
}
