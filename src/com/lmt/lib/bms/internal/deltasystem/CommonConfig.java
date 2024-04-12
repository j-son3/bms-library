package com.lmt.lib.bms.internal.deltasystem;

import java.util.Properties;

import com.lmt.lib.bms.internal.LinearInterpolateFunction;

/**
 * 共通設定クラス
 */
class CommonConfig extends RatingConfig {
	/** インスタンス */
	private static CommonConfig sInstance = new CommonConfig();

	/** 演奏時間によるレーティング値の倍率計算を行う補間関数 */
	LinearInterpolateFunction ipfnPlayTime = LinearInterpolateFunction.create(
			30.0, 1.0, 0.5, 0.57, 0.64, 0.7, 0.75, 0.79, 0.83, 0.86, 0.89, 0.915, 0.94, 0.96, 0.977, 0.993, 1.0);

	/**
	 * インスタンス取得
	 * @return インスタンス
	 */
	static CommonConfig getInstance() {
		return sInstance;
	}

	/** {@inheritDoc} */
	@Override
	void load(Properties config) {
		var loader = new ConfigLoader("common", config);
		ipfnPlayTime = loader.ipfnLinear("ipfn_play_time", ipfnPlayTime);
	}

	/** {@inheritDoc} */
	@Override
	void print() {
		Ds.debug("commonConfig: {");
		Ds.debug("  ipfnPlayTime: %s", ipfnPlayTime);
		Ds.debug("}");
	}
}
