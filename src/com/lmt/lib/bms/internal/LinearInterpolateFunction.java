package com.lmt.lib.bms.internal;

import static com.lmt.lib.bms.internal.Assertion.*;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

/**
 * 線形補間による補間関数
 *
 * <p>入力値に対する出力値の比率を一定の間隔で定め、ある比率AとBの間にある入力値は比率AとBの値を補間した値で
 * 出力値を計算する。補間は線形補間で行われるため、出力値の推移を曲線に近付けるには多くの比率の値を定義する必要がある。
 * 比率の値が少ないと急激な曲線を表現することが困難になり、また出力値の精度も下がってしまう。</p>
 *
 * <p>線形補間による補間は、出力値の精度を上げることが困難である代わりに計算速度は高速になる。
 * 出力値の精度をどの程度求められるかによって当クラスを使用するかどうかを決定すること。</p>
 */
public abstract class LinearInterpolateFunction extends InterpolateFunction {
	/** 最小出力値比率点数 */
	public static final int MIN_POINT_COUNT = 3;
	/** 最大出力値比率点数 */
	public static final int MAX_POINT_COUNT = 30;

	/** 出力値比率点リスト */
	protected double[] mPoints;
	/** 出力値比率点間の出力値変化幅 */
	protected double mInWidth;
	/** 出力値比率点リストの最大インデックス値 */
	protected int mPtLastIdx;
	/** 出力値比率点リストの最大インデックス値-1 */
	protected int mPtLastPrevIdx;

	/**
	 * インスタンス生成
	 * @param inRange 入力値幅
	 * @param outRange 出力値幅
	 * @param points 出力値比率点リスト(0～1)
	 * @return 当クラスのインスタンス
	 * @throws IllegalArgumentException 出力値比率点に0～1の範囲外の値がある
	 */
	public static LinearInterpolateFunction create(double inRange, double outRange, double...points) {
		// パラメータの範囲チェック
		assertArgRange(inRange, Math.nextUp(0.0), Double.MAX_VALUE, "inRange");
		assertArgRange(outRange, Math.nextUp(0.0), Double.MAX_VALUE, "outRange");
		assertArgRange(points.length, MIN_POINT_COUNT, MAX_POINT_COUNT, "Number of points");
		for (var i = 0; i < points.length; i++) {
			assertArgRange(points[i], 0.0, 1.0, "A points");
		}

		// パラメータに応じて最適化されたクラスを選択する
		var fn = (LinearInterpolateFunction)null;
		if (outRange == 1.0) {
			fn = new Linear();
		} else {
			fn = new LinearOut();
		}

		// 入力パラメータを生成する
		fn.mInRange = inRange;
		fn.mOutRange = outRange;
		fn.mPoints = points;
		fn.mPtLastIdx = points.length - 1;
		fn.mPtLastPrevIdx = fn.mPtLastIdx - 1;
		fn.mInWidth = inRange / (double)fn.mPtLastIdx;

		return fn;
	}

	/**
	 * 出力値比率点リスト取得
	 * @return 出力値比率点リスト
	 */
	public double[] getPoints() {
		return Arrays.copyOf(mPoints, mPoints.length);
	}

	/**
	 * 線形補間関数の文字列表現取得
	 * @return 線形補間関数の文字列表現取得
	 */
	@Override
	public String toString() {
		var pts = DoubleStream.of(mPoints).mapToObj(Double::toString).collect(Collectors.joining(", ", "[", "]"));
		return String.format("{ inRange:%s, outRange:%s, points:%s }", getInRange(), getOutRange(), pts);
	}

	/**
	 * 補間処理
	 * @param in 入力値
	 * @return 出力値
	 */
	protected double interpolate(double in) {
		if (in < 0.0) {
			// 入力値が下限値未満の場合は最初の補間点の値とする
			return mPoints[0];
		} else if (in >= mInRange) {
			// 入力値が上限値以上の場合は最後の補間点の値とする
			return mPoints[mPtLastIdx];
		} else {
			// 入力値が0～範囲値未満の場合に線形補間処理を実施する
			var idx = in / mInWidth;
			var lIdx = Math.min((int)Math.floor(idx), mPtLastPrevIdx);
			var lPt = mPoints[lIdx];
			var rPt = mPoints[lIdx + 1];
			return lPt + (rPt - lPt) * (idx - lIdx);
		}
	}

	/**
	 * 通常の線形補間関数
	 */
	private static class LinearOut extends LinearInterpolateFunction {
		@Override
		public double compute(double in) {
			return interpolate(in) * mOutRange;
		}
	}

	/**
	 * 出力値が1の場合の線形補間関数
	 */
	private static class Linear extends LinearInterpolateFunction {
		@Override
		public double compute(double in) {
			return interpolate(in);
		}
	}
}
