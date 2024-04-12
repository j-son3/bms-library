package com.lmt.lib.bms.internal;

/**
 * 補間関数クラス
 *
 * <p>入出力値の関係をグラフにすると、入力値に対して特異的な曲線を描くような出力値が欲しい時に用いる。
 * 当クラスはそのための処理を行うのに必要な基本的なデータ・I/Fを提供する。</p>
 */
public abstract class InterpolateFunction {
	/** 入力値範囲 */
	protected double mInRange;
	/** 出力値範囲 */
	protected double mOutRange;

	/**
	 * 入力値範囲取得
	 * @return 入力値範囲
	 */
	public final double getInRange() {
		return mInRange;
	}

	/**
	 * 出力値範囲取得
	 * @return 出力値範囲
	 */
	public final double getOutRange() {
		return mOutRange;
	}

	/**
	 * 入力値に対する出力値計算
	 * @param in 入力値
	 * @return 出力値
	 */
	public abstract double compute(double in);
}
