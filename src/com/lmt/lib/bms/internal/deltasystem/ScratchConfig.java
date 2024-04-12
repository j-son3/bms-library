package com.lmt.lib.bms.internal.deltasystem;

import java.util.Properties;

import com.lmt.lib.bms.bemusic.BeMusicRatingType;
import com.lmt.lib.bms.internal.LinearInterpolateFunction;

/**
 * 譜面傾向「SCRATCH」の設定クラス
 */
class ScratchConfig extends RatingConfig {
	/** インスタンス */
	private static final ScratchConfig sInstance = new ScratchConfig();

	/** 後方のスクラッチ密度による操作難易度評価点の増加倍率(標準値は1.0)にプラスする倍率の量を計算する補間関数 */
	LinearInterpolateFunction ipfnDifficultyBoost = LinearInterpolateFunction.create(
			0.6, 9.0, 1.0, 0.995, 0.985, 0.96, 0.93, 0.89, 0.84, 0.78, 0.72, 0.65, 0.58, 0.51, 0.46, 0.41, 0.36, 0.32, 0.28, 0.25, 0.22, 0.19, 0.17, 0.15, 0.125, 0.1, 0.08, 0.06, 0.04, 0.025, 0.012, 0.0);
	/** リズム変化頻度(回/秒)を評価点増加倍率に加算する値(標準の倍率1.0にプラスする値)に変換する補間関数 */
	LinearInterpolateFunction ipfnChangeRate = LinearInterpolateFunction.create(
			2.5, 1.0, 0.0, 0.025, 0.07, 0.13, 0.23, 0.4, 0.55, 0.64, 0.7, 0.75, 0.78, 0.81, 0.835, 0.86, 0.88, 0.895, 0.91, 0.925, 0.94, 0.955, 0.967, 0.978, 0.99, 0.996, 1.0);
	/** 演奏時間に対するスクラッチ操作時間の比率から最終評価点増加倍率に変換する補間関数 */
	LinearInterpolateFunction ipfnTimeRate = LinearInterpolateFunction.create(
			1.0, 1.3, 0.54, 0.55, 0.58, 0.63, 0.68, 0.74, 0.79, 0.825, 0.845, 0.86, 0.875, 0.89, 0.9, 0.91, 0.92, 0.93, 0.94, 0.95, 0.96, 0.968, 0.976, 0.983, 0.989, 0.995, 1.0);
	/** 最終的なSCRATCH値を算出するための補間関数 */
	LinearInterpolateFunction ipfnScratch = LinearInterpolateFunction.create(
			0.98, 20000.0, 0.0, 0.04, 0.11, 0.2, 0.32, 0.41, 0.48, 0.54, 0.58, 0.61, 0.645, 0.68, 0.715, 0.755, 0.795, 0.835, 0.865, 0.9, 0.93, 0.965, 1.0);

	/** 連続操作として認識する最大の刻み時間(この値より長い間隔のスクラッチ操作は連続操作の対象外とする) */
	double maxPulseTime = 0.43;
	/** 通常のLNモードで長押し終了の次の短押しノートを回転操作の終了位置として扱う最大の間隔(長押し終了～次の短押しまでの時間) */
	double maxSpinDockTime = 0.21;
	/** 回転操作の開始位置から終了位置までの最短時間(この値より短い間隔の回転操作は「往復操作」として扱う) */
	double minSpinTime = 0.27;
	/** スクラッチ時間に加算する最小の後方時間(単体操作におけるスクラッチ範囲時間と同義) */
	double minBehindTime = 0.15;
	/** スクラッチの操作方向をリセットする最小の時間 */
	double directionResetTime = 0.6;
	/** 許容されるズレ時間(この時間差分内では連続操作の継続、スイッチの後方操作の対象外となる) */
	double acceptableDelta = 0.021;
	/** スクラッチ評価点の最高点 */
	double satulateDifficulty = 40.0;

	/** 外回りスクラッチに対する加点量 */
	double addScoreOuter = 0.0;
	/** 内回りスクラッチに対する加点量 */
	double addScoreInner = 0.2;

	/** 最もスクラッチに近いスイッチの操作がある時の加点量 */
	double addScoreNear = 0.21;
	/** スクラッチから遠いスイッチの操作がある時の加点量 */
	double addScoreFar = 0.29;
	/** スクラッチを操作する反対の手で操作するスイッチの操作がある時の加点量 */
	double addScoreOpposite = 0.15;

	/** 短押し操作がある時の加点量 */
	double addScoreBeat = 0.1;
	/** 長押し開始がある時の加点量 */
	double addScoreLongOn = 0.15;
	/** 長押し継続がある時の加点量 */
	double addScoreLong = 0.3;
	/** スクラッチの後方楽曲位置でのスイッチ操作がある時の加点量 */
	double behindScoreRate = 1.6;

	/**
	 * インスタンス取得
	 * @return インスタンス
	 */
	static ScratchConfig getInstance() {
		return sInstance;
	}

	/** {@inheritDoc} */
	@Override
	void load(Properties config) {
		var loader = new ConfigLoader(BeMusicRatingType.SCRATCH, config);
		ipfnDifficultyBoost = loader.ipfnLinear("ipfn_difficulty_boost", ipfnDifficultyBoost);
		ipfnChangeRate = loader.ipfnLinear("ipfn_change_rate", ipfnChangeRate);
		ipfnTimeRate = loader.ipfnLinear("ipfn_time_rate", ipfnTimeRate);
		ipfnScratch = loader.ipfnLinear("ipfn_scratch", ipfnScratch);
		maxPulseTime = loader.numeric("max_pulse_time", maxPulseTime);
		maxSpinDockTime = loader.numeric("max_spin_dock_time", maxSpinDockTime);
		minSpinTime = loader.numeric("min_spin_time", minSpinTime);
		minBehindTime = loader.numeric("min_behind_time", minBehindTime);
		directionResetTime = loader.numeric("direction_reset_time", directionResetTime);
		acceptableDelta = loader.numeric("acceptable_delta", acceptableDelta);
		satulateDifficulty = loader.numeric("satulate_difficulty", satulateDifficulty);
		addScoreOuter = loader.numeric("add_score_outer", addScoreOuter);
		addScoreInner = loader.numeric("add_score_inner", addScoreInner);
		addScoreNear = loader.numeric("add_score_near", addScoreNear);
		addScoreFar = loader.numeric("add_score_far", addScoreFar);
		addScoreOpposite = loader.numeric("add_score_opposite", addScoreOpposite);
		addScoreBeat = loader.numeric("add_score_beat", addScoreBeat);
		addScoreLongOn = loader.numeric("add_score_long_on", addScoreLongOn);
		addScoreLong = loader.numeric("add_score_long", addScoreLong);
		behindScoreRate = loader.numeric("behind_score_rate", behindScoreRate);
	}

	/** {@inheritDoc} */
	@Override
	void print() {
		Ds.debug("scratchConfig: {");
		Ds.debug("  ipfnDifficultyBoost: %s", ipfnDifficultyBoost);
		Ds.debug("  ipfnChangeRate: %s", ipfnChangeRate);
		Ds.debug("  ipfnTimeRate: %s", ipfnTimeRate);
		Ds.debug("  ipfnScratch: %s", ipfnScratch);
		Ds.debug("  maxPulseTime: %s", maxPulseTime);
		Ds.debug("  maxSpinDockTime: %s", maxSpinDockTime);
		Ds.debug("  minSpinTime: %s", minSpinTime);
		Ds.debug("  minBehindTime: %s", minBehindTime);
		Ds.debug("  directionResetTime: %s", directionResetTime);
		Ds.debug("  acceptableDelta: %s", acceptableDelta);
		Ds.debug("  satulateDifficulty: %s", satulateDifficulty);
		Ds.debug("  addScoreOuter: %s", addScoreOuter);
		Ds.debug("  addScoreInner: %s", addScoreInner);
		Ds.debug("  addScoreNear: %s", addScoreNear);
		Ds.debug("  addScoreFar: %s", addScoreFar);
		Ds.debug("  addScoreOpposite: %s", addScoreOpposite);
		Ds.debug("  addScoreBeat: %s", addScoreBeat);
		Ds.debug("  addScoreLongOn: %s", addScoreLongOn);
		Ds.debug("  addScoreLong: %s", addScoreLong);
		Ds.debug("  behindScoreRate: %s", behindScoreRate);
		Ds.debug("}");
	}
}
