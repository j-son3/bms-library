package com.lmt.lib.bms.internal.deltasystem;

import java.util.Properties;

import com.lmt.lib.bms.bemusic.BeMusicRatingType;
import com.lmt.lib.bms.internal.LinearInterpolateFunction;

/**
 * 譜面傾向「GIMMICK」の設定クラス
 */
class GimmickConfig extends RatingConfig {
	/** インスタンス */
	private static final GimmickConfig sInstance = new GimmickConfig();

	// ----- 共通の設定値 -----
	/** 速度変更、譜面停止、地雷のうち、最も高い評価点の反映率 */
	double commonInfluencePrimaryScore = 1.0;
	/** 速度変更、譜面停止、地雷のうち、2番目の評価点の反映率 */
	double commonInfluenceSecondaryScore = 0.2;
	/** 速度変更、譜面停止、地雷のうち、最も低い評価点の反映率 */
	double commonInfluenceTertiaryScore = 0.05;

	// ----- 速度変更に関する設定値 -----
	/** 速度変化前、最後の操作可能ノートから次の速度変更範囲に到達するまでの時間をギアチェン難易度計算用の係数に変換する補間関数 */
	LinearInterpolateFunction speedIpfnGcBefore = LinearInterpolateFunction.create(
			2.0, 1.0, 1.0, 0.995, 0.99, 0.97, 0.95, 0.91, 0.87, 0.83, 0.79, 0.75, 0.71, 0.675, 0.64, 0.61, 0.58, 0.56, 0.54, 0.527, 0.513, 0.5);
	/** 速度変化後、最初の操作可能ノートに到達するまでの時間をギアチェン難易度計算用の係数に変換する補間関数 */
	LinearInterpolateFunction speedIpfnGcTime = LinearInterpolateFunction.create(
			2.0, 1.0, 1.0, 0.93, 0.78, 0.64, 0.53, 0.45, 0.37, 0.31, 0.26, 0.22, 0.17, 0.135, 0.1, 0.075, 0.055, 0.035, 0.02, 0.012, 0.005, 0.0);
	/** 速度変化後、最初の操作可能ノートの位置から規定時間分の平均密度からギアチェン難易度計算用の係数に変換する補間関数 */
	LinearInterpolateFunction speedIpfnGcDensity = LinearInterpolateFunction.create(
			30.0, 1.0, 0.0, 0.16, 0.28, 0.38, 0.46, 0.53, 0.59, 0.65, 0.69, 0.735, 0.775, 0.81, 0.84, 0.865, 0.89, 0.915, 0.94, 0.96, 0.98, 1.0);
	/** 理想速度の乖離度から速度変更範囲評価点に変換する補間関数 */
	LinearInterpolateFunction speedIpfnIdeality = LinearInterpolateFunction.create(
			1.5, 1.0, 0.0, 0.2, 0.36, 0.49, 0.58, 0.66, 0.73, 0.78, 0.815, 0.845, 0.87, 0.89, 0.91, 0.925, 0.94, 0.953, 0.966, 0.978, 0.99, 1.0);
	/** 速度変化頻度から、速度変化の最終評価点計算用の係数に変換する補間関数 */
	LinearInterpolateFunction speedIpfnChgRate = LinearInterpolateFunction.create(
			0.8, 1.0, 0.0, 0.28, 0.45, 0.59, 0.68, 0.74, 0.785, 0.815, 0.845, 0.875, 0.9, 0.925, 0.94, 0.955, 0.97, 0.98, 0.987, 0.993, 0.996, 1.0);
	/** 速度変化頻度の係数を満たすのに必要な最低限の平均密度を算出する補間関数 */
	LinearInterpolateFunction speedIpfnCrAdjust = LinearInterpolateFunction.create(
			1.0, 15.0, 0.0, 0.02, 0.045, 0.07, 0.1, 0.13, 0.165, 0.2, 0.235, 0.27, 0.31, 0.345, 0.39, 0.44, 0.49, 0.55, 0.61, 0.68, 0.77, 0.87);
	/** 速度変化の最終評価点からGIMMICK値を算出する補間関数 */
	LinearInterpolateFunction speedIpfnGimmick = LinearInterpolateFunction.create(
			1.0, 20000.0, 0.0, 0.06, 0.13, 0.21, 0.28, 0.35, 0.43, 0.51, 0.58, 0.63, 0.68, 0.725, 0.765, 0.8, 0.835, 0.86, 0.88, 0.91, 0.95, 1.0);
	/** BPMがどれ位変化したらギアチェンするべきか(0～1で指定する) */
	double speedGearChangeDeltaPer = 0.1;
	/** BPM大幅変更によりギアチェンするべきと判定された場合でも、耐えて頑張る最大の楽曲位置数 */
	int speedGearChangeEndurePts = 3;
	/** ギアチェン後のノート平均密度の計算に使用する時間 */
	double speedGearChangeDensityTime = 1.0;
	/** 速度変化を知覚する最小の速度変化量 */
	double speedMinPerceive = 4.0;
	/** 速度変更範囲のスクロール方向が逆の場合の速度変更範囲難易度の増加量 */
	double speedReverseScrollAddDifficulty = 1.0;
	/** 速度変化頻度の係数補正値の最小値 */
	double speedMinChangeRateAdjust = 0.6;
	/** 速度変更範囲評価点計算時、範囲の最大占有時間(速度変更に無関係の箇所が評価対象に含まれるのを防止する措置) */
	double speedMaxEvaluateTime = 18.0;
	/** 速度変更範囲評価点、速度変化頻度評価点のうち低いほうの評価点の影響度 */
	double speedInfluenceLowScore = 0.35;
	/** 速度変更範囲評価点、速度変化頻度評価点のうち高いほうの評価点の影響度 */
	double speedInfluenceHighScore = 0.65;

	// ----- 譜面停止に関する設定値 -----
	/** 譜面停止解除～操作可能ノートまでの時間から、譜面停止の演奏への影響係数を計算する補間関数 */
	LinearInterpolateFunction stopIpfnStopEffective = LinearInterpolateFunction.create(
			1.0, 1.0, 1.0, 0.95, 0.85, 0.74, 0.64, 0.56, 0.48, 0.43, 0.375, 0.33, 0.285, 0.25, 0.215, 0.18, 0.15, 0.12, 0.09, 0.06, 0.03, 0.0);
	/** 譜面停止時間から、譜面停止への対応難易度を計算する補間関数 */
	LinearInterpolateFunction stopIpfnStopTime = LinearInterpolateFunction.create(
			0.8, 1.0, 0.0, 0.005, 0.25, 0.95, 1.0, 0.99, 0.94, 0.86, 0.77, 0.7, 0.63, 0.58, 0.51, 0.465, 0.42, 0.38, 0.35, 0.315, 0.285, 0.26, 0.24, 0.22, 0.2, 0.18, 0.16, 0.145, 0.13, 0.12, 0.11, 0.1);
	/** 停止解除後のノート平均密度から、譜面停止対応難易度評価点計算用の係数を算出する補間関数 */
	LinearInterpolateFunction stopIpfnAfterDensity = LinearInterpolateFunction.create(
			40.0, 1.0, 0.0, 0.12, 0.24, 0.35, 0.45, 0.54, 0.63, 0.7, 0.75, 0.8, 0.835, 0.865, 0.895, 0.915, 0.93, 0.95, 0.965, 0.975, 0.988, 1.0);
	/** 譜面停止範囲の効果範囲率から最終評価点計算用の係数を算出する補間関数 */
	LinearInterpolateFunction stopIpfnEffectiveRange = LinearInterpolateFunction.create(
			1.0, 1.0, 0.0, 0.06, 0.15, 0.26, 0.37, 0.46, 0.53, 0.6, 0.65, 0.7, 0.75, 0.79, 0.82, 0.855, 0.88, 0.91, 0.935, 0.96, 0.98, 1.0);
	/** 譜面停止の最終評価点からGIMMICK値を算出する補間関数 */
	LinearInterpolateFunction stopIpfnGimmick = LinearInterpolateFunction.create(
			1.0, 20000.0, 0.0, 0.07, 0.14, 0.21, 0.285, 0.35, 0.42, 0.485, 0.55, 0.61, 0.66, 0.71, 0.765, 0.81, 0.855, 0.89, 0.92, 0.95, 0.975, 1.0);
	/** 譜面停止の視覚効果が影響する最大秒数 */
	double stopInfluenceTimeStop = 0.8;
	/** 譜面停止の視覚効果により演奏に影響する最大秒数(短くしすぎると平均密度が正確に出せない) */
	double stopInfluenceTimeAfter = 1.0;
	/** 譜面停止の視覚効果を認知できる最小の譜面停止時間(この値より短い停止時間は譜面停止と認定せず評価点に含めない) */
	double stopMinEffectiveStopTime = 0.017;
	/** 譜面停止の対応難易度の評価点への反映係数(この値が大きいほど評価点への反映度が「減少」する) */
	double stopInfluenceDifficultyValue = 8.0;

	// ----- 地雷に関する設定値 -----
	/** 操作可能ノートから地雷までの時間差分から地雷回避難易度計算用の係数を算出する補間関数 */
	LinearInterpolateFunction mineIpfnMineDistance = LinearInterpolateFunction.create(
			0.5, 1.0, 1.0, 0.95, 0.78, 0.64, 0.54, 0.45, 0.37, 0.3, 0.245, 0.19, 0.14, 0.105, 0.08, 0.055, 0.04, 0.03, 0.02, 0.013, 0.006, 0.0);
	/** 地雷のダメージ量(%)から地雷回避難易度計算用の係数を算出する補間関数 */
	LinearInterpolateFunction mineIpfnMineDamage = LinearInterpolateFunction.create(
			100.0, 1.0, 0.0, 0.29, 0.44, 0.56, 0.63, 0.69, 0.74, 0.78, 0.815, 0.845, 0.87, 0.89, 0.91, 0.93, 0.945, 0.96, 0.975, 0.987, 0.995, 1.0);
	/** 地雷範囲の影響範囲率から最終評価点計算用の係数を算出する補間関数 */
	LinearInterpolateFunction mineIpfnEffectiveRange = LinearInterpolateFunction.create(
			1.0, 1.0, 0.0, 0.16, 0.32, 0.43, 0.51, 0.575, 0.64, 0.69, 0.74, 0.785, 0.83, 0.865, 0.9, 0.925, 0.945, 0.96, 0.975, 0.985, 0.993, 1.0);
	/** 地雷の最終評価点からGIMMICK値を算出する補間関数 */
	LinearInterpolateFunction mineIpfnGimmick = LinearInterpolateFunction.create(
			1.0, 20000.0, 0.0, 0.04, 0.08, 0.13, 0.18, 0.23, 0.29, 0.35, 0.415, 0.48, 0.535, 0.6, 0.66, 0.71, 0.765, 0.82, 0.865, 0.915, 0.96, 1.0);
	/** 地雷の回避難易度に影響する最大の時間差分 */
	double mineInfluenceTime = 0.3;
	/** 個別地雷回避難易度の飽和値 */
	double mineSaturateRangeScore = 15.0;
	/** 地雷回避難易度、地雷影響範囲率評価点のうち低いほうの評価点の影響度 */
	double mineInfluenceLowScore = 0.4;
	/** 地雷回避難易度、地雷影響範囲率評価点のうち高いほうの評価点の影響度 */
	double mineInfluenceHighScore = 0.6;

	/**
	 * コンストラクタ
	 */
	private GimmickConfig() {
		// Do nothing
	}

	/**
	 * インスタンス取得
	 * @return インスタンス
	 */
	static GimmickConfig getInstance() {
		return sInstance;
	}

	/** {@inheritDoc} */
	@Override
	void load(Properties config) {
		var loader = new ConfigLoader(BeMusicRatingType.HOLDING, config);
		// 共通の設定
		commonInfluencePrimaryScore = loader.numeric("common.influence_primary_score", commonInfluencePrimaryScore);
		commonInfluenceSecondaryScore = loader.numeric("common.influence_secondary_score", commonInfluenceSecondaryScore);
		commonInfluenceTertiaryScore = loader.numeric("common.influence_tertiary_score", commonInfluenceTertiaryScore);
		// 速度変更に関する設定値
		speedIpfnGcBefore = loader.ipfnLinear("speed.ipfn_gc_before", speedIpfnGcBefore);
		speedIpfnGcTime = loader.ipfnLinear("speed.ipfn_gc_time", speedIpfnGcTime);
		speedIpfnGcDensity = loader.ipfnLinear("speed.ipfn_gc_density", speedIpfnGcDensity);
		speedIpfnIdeality = loader.ipfnLinear("speed.ipfn_ideality", speedIpfnIdeality);
		speedIpfnChgRate = loader.ipfnLinear("speed.ipfn_chg_rate", speedIpfnChgRate);
		speedIpfnCrAdjust = loader.ipfnLinear("speed.ipfn_cr_adjust", speedIpfnCrAdjust);
		speedIpfnGimmick = loader.ipfnLinear("speed.ipfn_gimmick", speedIpfnGimmick);
		speedGearChangeDeltaPer = loader.numeric("speed.gear_change_delta_per", speedGearChangeDeltaPer);
		speedGearChangeEndurePts = loader.integer("speed.gear_change_endure_pts", speedGearChangeEndurePts);
		speedGearChangeDensityTime = loader.numeric("speed.gear_change_density_time", speedGearChangeDensityTime);
		speedMinPerceive = loader.numeric("speed.min_perceive", speedMinPerceive);
		speedReverseScrollAddDifficulty = loader.numeric("speed.reverse_scroll_add_difficulty", speedReverseScrollAddDifficulty);
		speedMinChangeRateAdjust = loader.numeric("speed.min_change_rate_adjust", speedMinChangeRateAdjust);
		speedMaxEvaluateTime = loader.numeric("speed.max_evaluate_time", speedMaxEvaluateTime);
		speedInfluenceLowScore = loader.numeric("speed.influence_low_score", speedInfluenceLowScore);
		speedInfluenceHighScore = loader.numeric("speed.influence_high_score", speedInfluenceHighScore);
		// 譜面停止に関する設定値
		stopIpfnStopEffective = loader.ipfnLinear("stop.ipfn_stop_effective", stopIpfnStopEffective);
		stopIpfnStopTime = loader.ipfnLinear("stop.ipfn_stop_time", stopIpfnStopTime);
		stopIpfnAfterDensity = loader.ipfnLinear("stop.ipfn_after_density", stopIpfnAfterDensity);
		stopIpfnEffectiveRange = loader.ipfnLinear("stop.ipfn_effective_range", stopIpfnEffectiveRange);
		stopIpfnGimmick = loader.ipfnLinear("stop.ipfn_gimmick", stopIpfnGimmick);
		stopInfluenceTimeStop = loader.numeric("stop.influence_time_stop", stopInfluenceTimeStop);
		stopInfluenceTimeAfter = loader.numeric("stop.influence_time_after", stopInfluenceTimeAfter);
		stopMinEffectiveStopTime = loader.numeric("stop.min_effective_stop_time", stopMinEffectiveStopTime);
		stopInfluenceDifficultyValue = loader.numeric("stop.influence_difficulty_value", stopInfluenceDifficultyValue);
		// 地雷に関する設定値
		mineIpfnMineDistance = loader.ipfnLinear("mine.ipfn_mine_distance", mineIpfnMineDistance);
		mineIpfnMineDamage = loader.ipfnLinear("mine.ipfn_mine_damage", mineIpfnMineDamage);
		mineIpfnEffectiveRange = loader.ipfnLinear("mine.ipfn_effective_range", mineIpfnEffectiveRange);
		mineIpfnGimmick = loader.ipfnLinear("mine.ipfn_gimmick", mineIpfnGimmick);
		mineInfluenceTime = loader.numeric("mine.influence_time", mineInfluenceTime);
		mineSaturateRangeScore = loader.numeric("mine.saturate_range_score", mineSaturateRangeScore);
		mineInfluenceLowScore = loader.numeric("mine.influence_low_score", mineInfluenceLowScore);
		mineInfluenceHighScore = loader.numeric("mine.influence_high_score", mineInfluenceHighScore);
	}

	/** {@inheritDoc} */
	@Override
	void print() {
		Ds.debug("holdingConfig: {");
		Ds.debug("  common: {");
		Ds.debug("    commonInfluencePrimaryScore: %s", commonInfluencePrimaryScore);
		Ds.debug("    commonInfluenceSecondaryScore: %s", commonInfluenceSecondaryScore);
		Ds.debug("    commonInfluenceTertiaryScore: %s", commonInfluenceTertiaryScore);
		Ds.debug("  }");
		Ds.debug("  speed: {");
		Ds.debug("    speedIpfnGcBefore: %s", speedIpfnGcBefore);
		Ds.debug("    speedIpfnGcTime: %s", speedIpfnGcTime);
		Ds.debug("    speedIpfnGcDensity: %s", speedIpfnGcDensity);
		Ds.debug("    speedIpfnIdeality: %s", speedIpfnIdeality);
		Ds.debug("    speedIpfnChgRate: %s", speedIpfnChgRate);
		Ds.debug("    speedIpfnCrAdjust: %s", speedIpfnCrAdjust);
		Ds.debug("    speedIpfnGimmick: %s", speedIpfnGimmick);
		Ds.debug("    speedGearChangeDeltaPer: %s", speedGearChangeDeltaPer);
		Ds.debug("    speedGearChangeEndurePts: %s", speedGearChangeEndurePts);
		Ds.debug("    speedGearChangeDensityTime: %s", speedGearChangeDensityTime);
		Ds.debug("    speedMinPerceive: %s", speedMinPerceive);
		Ds.debug("    speedReverseScrollAddDifficulty: %s", speedReverseScrollAddDifficulty);
		Ds.debug("    speedMinChangeRateAdjust: %s", speedMinChangeRateAdjust);
		Ds.debug("    speedMaxEvaluateTime: %s", speedMaxEvaluateTime);
		Ds.debug("    speedInfluenceLowScore: %s", speedInfluenceLowScore);
		Ds.debug("    speedInfluenceHighScore: %s", speedInfluenceHighScore);
		Ds.debug("  }");
		Ds.debug("  stop: {");
		Ds.debug("    stopIpfnStopEffective: %s", stopIpfnStopEffective);
		Ds.debug("    stopIpfnStopTime: %s", stopIpfnStopTime);
		Ds.debug("    stopIpfnAfterDensity: %s", stopIpfnAfterDensity);
		Ds.debug("    stopIpfnEffectiveRange: %s", stopIpfnEffectiveRange);
		Ds.debug("    stopIpfnGimmick: %s", stopIpfnGimmick);
		Ds.debug("    stopInfluenceTimeStop: %s", stopInfluenceTimeStop);
		Ds.debug("    stopInfluenceTimeAfter: %s", stopInfluenceTimeAfter);
		Ds.debug("    stopMinEffectiveStopTime: %s", stopMinEffectiveStopTime);
		Ds.debug("    stopInfluenceDifficultyValue: %s", stopInfluenceDifficultyValue);
		Ds.debug("  }");
		Ds.debug("  mine: {");
		Ds.debug("    mineIpfnMineDistance: %s", mineIpfnMineDistance);
		Ds.debug("    mineIpfnMineDamage: %s", mineIpfnMineDamage);
		Ds.debug("    mineIpfnEffectiveRange: %s", mineIpfnEffectiveRange);
		Ds.debug("    mineIpfnGimmick: %s", mineIpfnGimmick);
		Ds.debug("    mineInfluenceTime: %s", mineInfluenceTime);
		Ds.debug("    mineSaturateRangeScore: %s", mineSaturateRangeScore);
		Ds.debug("    mineInfluenceLowScore: %s", mineInfluenceLowScore);
		Ds.debug("    mineInfluenceHighScore: %s", mineInfluenceHighScore);
		Ds.debug("  }");
		Ds.debug("}");
	}
}
