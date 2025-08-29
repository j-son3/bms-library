package com.lmt.lib.bms.internal;

import static com.lmt.lib.bms.internal.Assertion.*;

import java.util.Map;
import java.util.function.Supplier;

/**
 * 対数補間関数
 *
 * <p>対数(log)による補間を行う補間関数。対数による補間では非常に綺麗な曲線を描く出力が得られる反面、
 * 計算に時間がかかる、出力値の融通が利きにくいといったデメリットを有する。当クラスを採用するかどうかは、
 * 計算に用いられる頻度と出力値の融通を緻密に求められるかどうかで判断すること。</p>
 */
public abstract class LogInterpolateFunction extends InterpolateFunction {
	/** 曲線の強さ */
	protected double mStrength;
	/** 出力値のオフセット量 */
	protected double mOffset;
	/** logによる出力の最大値 */
	protected double mLogMax;

	/**
	 * 曲線の強さ取得
	 * @return 曲線の強さ
	 */
	public double getStrength() {
		return mStrength;
	}

	/**
	 * 出力値のオフセット量取得
	 * @return 出力値のオフセット量
	 */
	public double getOffset() {
		return mOffset;
	}

	/**
	 * 反転した出力値取得
	 * @param in 入力値
	 * @return 反転した出力値
	 */
	public double reverse(double in) {
		return mOutRange - compute(in);
	}

	/**
	 * 対数補間関数の文字列表現取得
	 * @return 対数補間関数の文字列表現取得
	 */
	@Override
	public String toString() {
		return String.format("{ inRange:%s, outRange:%s, strength:%s, offset:%s }",
				getInRange(), getOutRange(), mStrength, mOffset);
	}

	/**
	 * 初期化処理
	 * @param inRange 入力値範囲
	 * @param outRange 出力値範囲
	 * @param strength 曲線の強さ
	 * @param offset 出力値のオフセット量
	 */
	protected void init(double inRange, double outRange, double strength, double offset) {
		assertArgRange(inRange, Math.nextUp(0.0), Double.MAX_VALUE, "inRange");
		assertArgRange(outRange, Math.nextUp(0.0), Double.MAX_VALUE, "outRange");
		assertArgRange(strength, Math.nextUp(0.0), Double.MAX_VALUE, "strength");
		mInRange = inRange;
		mOutRange = outRange;
		mStrength = strength;
		mOffset = offset;
		mLogMax = Math.log(1.0 + strength);
	}

	/**
	 * インスタンス生成
	 * @param inRange 入力値範囲
	 * @param outRange 出力値範囲
	 * @param strength 曲線の強さ
	 * @param offset 出力値のオフセット量
	 * @return 当クラスのインスタンス
	 */
	public static LogInterpolateFunction create(double inRange, double outRange, double strength, double offset) {
		var constructor = PARAM_NONE;
		constructor |= ((inRange != 1.0) ? PARAM_IN_RANGE : 0);
		constructor |= ((outRange != 1.0) ? PARAM_OUT_RANGE : 0);
		constructor |= ((offset != 0.0) ? PARAM_OFFSET : 0);

		var function = LOG_CONSTRUCTOR_MAP.get(constructor).get();
		function.init(inRange, outRange, strength, offset);
		return function;
	}

	/** インスタンス生成パラメータ：なし */
	static final int PARAM_NONE = 0x00;
	/** インスタンス生成パラメータ：出力値のオフセット量指定 */
	static final int PARAM_OFFSET = 0x01;
	/** インスタンス生成パラメータ：出力値範囲指定 */
	static final int PARAM_OUT_RANGE = 0x02;
	/** インスタンス生成パラメータ：入力値範囲指定 */
	static final int PARAM_IN_RANGE = 0x04;

	/** 対数補間関数のクラス決定マップ */
	static Map<Integer, Supplier<LogInterpolateFunction>> LOG_CONSTRUCTOR_MAP = Map.ofEntries(
			Map.entry(PARAM_NONE, Log::new),
			Map.entry(PARAM_OFFSET, LogOff::new),
			Map.entry(PARAM_OUT_RANGE, LogOut::new),
			Map.entry(PARAM_OUT_RANGE | PARAM_OFFSET, LogOutOff::new),
			Map.entry(PARAM_IN_RANGE, LogIn::new),
			Map.entry(PARAM_IN_RANGE | PARAM_OFFSET, LogInOff::new),
			Map.entry(PARAM_IN_RANGE | PARAM_OUT_RANGE, LogInOut::new),
			Map.entry(PARAM_IN_RANGE | PARAM_OUT_RANGE | PARAM_OFFSET, LogInOutOff::new));

	/** 対数補間関数：入力値範囲、出力値範囲、出力値のオフセット量指定 */
	private static class LogInOutOff extends LogInterpolateFunction {
		@Override
		public double compute(double in) {
			// out = outrange * ((log(max(1, 1 + (in / inrange) * strength)) / log(1 + strength))
			return mOutRange * Math.log(Math.max(1.0, 1.0 - mOffset + ((in / mInRange) * mStrength))) / mLogMax;
		}
	}

	/** 対数補間関数：入力値範囲、出力値範囲指定 */
	private static class LogInOut extends LogInterpolateFunction {
		@Override
		public double compute(double in) {
			return mOutRange * Math.log(Math.max(1.0, 1.0 + ((in / mInRange) * mStrength))) / mLogMax;
		}
	}

	/** 対数補間関数：入力値範囲、出力値のオフセット量指定 */
	private static class LogInOff extends LogInterpolateFunction {
		@Override
		public double compute(double in) {
			return Math.log(Math.max(1.0, 1.0 - mOffset + ((in / mInRange) * mStrength))) / mLogMax;
		}
	}

	/** 対数補間関数：入力値範囲指定 */
	private static class LogIn extends LogInterpolateFunction {
		@Override
		public double compute(double in) {
			return Math.log(Math.max(1.0, 1.0 + ((in / mInRange) * mStrength))) / mLogMax;
		}
	}

	/** 対数補間関数：出力値範囲、出力値のオフセット量指定 */
	private static class LogOutOff extends LogInterpolateFunction {
		@Override
		public double compute(double in) {
			return mOutRange * Math.log(Math.max(1.0, 1.0 - mOffset + (in * mStrength))) / mLogMax;
		}
	}

	/** 対数補間関数：出力値範囲指定 */
	private static class LogOut extends LogInterpolateFunction {
		@Override
		public double compute(double in) {
			return mOutRange * Math.log(Math.max(1.0, 1.0 + (in * mStrength))) / mLogMax;
		}
	}

	/** 対数補間関数：出力値のオフセット量指定 */
	private static class LogOff extends LogInterpolateFunction {
		@Override
		public double compute(double in) {
			return Math.log(Math.max(1.0, 1.0 - mOffset + (in * mStrength))) / mLogMax;
		}
	}

	/** 対数補間関数：指定なし */
	private static class Log extends LogInterpolateFunction {
		@Override
		public double compute(double in) {
			return Math.log(Math.max(1.0, 1.0 + (in * mStrength))) / mLogMax;
		}
	}
}
