package com.lmt.lib.bms.internal.deltasystem;

import java.util.Properties;

import com.lmt.lib.bms.bemusic.BeMusicRatingType;
import com.lmt.lib.bms.internal.LinearInterpolateFunction;

/**
 * 譜面傾向「POWER」の設定クラス
 */
class PowerConfig extends RatingConfig {
	/** インスタンス */
	private static final PowerConfig sInstance = new PowerConfig();

	/** ノート評価点計算用の補間関数 */
	LinearInterpolateFunction ipfnNotes = LinearInterpolateFunction.create(
			1.0, 0.9, 1.0, 0.82, 0.51, 0.35, 0.3, 0.28, 0.27, 0.26, 0.25, 0.23, 0.21, 0.19, 0.17, 0.15, 0.13, 0.11, 0.09, 0.07, 0.05, 0.03);
	/** 連動指の抵抗による評価点計算用の補間関数 */
	LinearInterpolateFunction ipfnResist = LinearInterpolateFunction.create(
			0.5, 1.0, 0.0, 0.0, 0.82, 0.87, 0.747, 0.6, 0.526, 0.43, 0.353, 0.305, 0.275, 0.24, 0.22, 0.195, 0.175, 0.165, 0.15, 0.14, 0.125, 0.11, 0.1, 0.09, 0.077, 0.064, 0.052, 0.041, 0.03, 0.02, 0.01, 0.0);
	/** 譜面密度評価点計算用の補間関数 */
	LinearInterpolateFunction ipfnDensity = LinearInterpolateFunction.create(
			0.5, 0.3, 1.0, 0.65, 0.5, 0.4, 0.32, 0.26, 0.235, 0.215, 0.195, 0.175, 0.15, 0.125, 0.1, 0.08, 0.065, 0.05, 0.04, 0.03, 0.02, 0.01);
	/** 連続操作評価点計算用の補間関数 */
	LinearInterpolateFunction ipfnRapidBeat = LinearInterpolateFunction.create(
			0.5, 0.6, 1.0, 1.0, 1.0, 0.8, 0.59, 0.18, 0.135, 0.11, 0.085, 0.065, 0.05, 0.035, 0.02, 0.01, 0.008, 0.006, 0.004, 0.002, 0.0, 0.0);
	/** 最終評価点計算用の補間関数 */
	LinearInterpolateFunction ipfnPower = LinearInterpolateFunction.create(
			0.25, 20000.0, 0.0, 0.03, 0.084, 0.155, 0.225, 0.31, 0.393, 0.5, 0.597, 0.665, 0.723, 0.77, 0.813, 0.85, 0.884, 0.91, 0.934, 0.953, 0.973, 1.0);
	/** POWER値に対する平均密度の基準値を算出する補間関数 */
	LinearInterpolateFunction ipfnAdjust = LinearInterpolateFunction.create(
			20000.0, 28.0, 0.02, 0.12, 0.19, 0.25, 0.31, 0.39, 0.48, 0.62, 0.81, 1.0);

	/** 最終評価点の飽和点 */
	double satulateTotalScore = 5.0;
	/** 最低楽曲位置数(操作を伴う楽曲位置数がこの値を下回ると最終評価点が減点される) */
	int leastPoints = 50;
	/** 指が入力デバイスに固定されている時の抵抗値 */
	double resistHolding = 0.6;
	/** 担当手評価点計算において、最大のノート評価点以外を考慮する比率 */
	double influenceOtherNoteScore = 0.15;
	/** 楽曲位置評価点計算において、低いほうの担当手評価点を反映する比率 */
	double influenceLowHandScore = 0.30;
	/** スクラッチ操作後、スクラッチを考慮した運指を継続する時間 */
	double continueScratchFingering = 0.5;
	/** スクラッチの評価点計算時の調整倍率 */
	double adjustScoreAtScratch = 0.38;
	/** POWER値に対する平均密度の許容率(基準値の何%下まで許容するか) */
	double densityAcceptableRate = 0.19;
	/** POWER値に対する平均密度が許容値を下回っていた場合、最大何%の下方修正を行うか */
	double powerDownwardRate = 0.275;

	/** ノート評価点算出に使用される最大時間 */
	double timeRangeNotes;
	/** 連動指の抵抗値において、抵抗を受ける最長の時間 */
	double timeRangeResist;
	/** 譜面密度評価点の加点が行われる最大範囲 */
	double timeRangeDensity;
	/** 連続操作評価点の加点が行われる最大範囲 */
	double timeRangeRapidBeat;

	/**
	 * インスタンス取得
	 * @return インスタンス
	 */
	static PowerConfig getInstance() {
		return sInstance;
	}

	/**
	 * コンストラクタ
	 */
	private PowerConfig() {
		setup();
	}

	/** {@inheritDoc} */
	@Override
	void load(Properties config) {
		var loader = new ConfigLoader(BeMusicRatingType.POWER, config);

		// 補間関数
		ipfnNotes = loader.ipfnLinear("ipfn_notes", ipfnNotes);
		ipfnResist = loader.ipfnLinear("ipfn_resist", ipfnResist);
		ipfnDensity = loader.ipfnLinear("ipfn_density", ipfnDensity);
		ipfnRapidBeat = loader.ipfnLinear("ipfn_rapid_beat", ipfnDensity);
		ipfnPower = loader.ipfnLinear("ipfn_power", ipfnPower);
		ipfnAdjust = loader.ipfnLinear("ipfn_adjust", ipfnAdjust);

		// 定数値
		satulateTotalScore = loader.numeric("satulate_total_score", satulateTotalScore);
		leastPoints = loader.integer("least_points", leastPoints);
		resistHolding = loader.numeric("resist_holding", resistHolding);
		influenceOtherNoteScore = loader.numeric("influence_other_note_score", influenceOtherNoteScore);
		influenceLowHandScore = loader.numeric("influence_low_hand_score", influenceLowHandScore);
		continueScratchFingering = loader.numeric("continue_scratch_fingering", continueScratchFingering);
		adjustScoreAtScratch = loader.numeric("adjust_score_at_scratch", adjustScoreAtScratch);
		densityAcceptableRate = loader.numeric("density_acceptable_rate", densityAcceptableRate);
		powerDownwardRate = loader.numeric("power_downward_rate", powerDownwardRate);

		setup();
	}

	/** {@inheritDoc} */
	@Override
	void print() {
		Ds.debug("powerConfig: {");
		Ds.debug("  ipfnNotes: %s", ipfnNotes);
		Ds.debug("  ipfnResist: %s", ipfnResist);
		Ds.debug("  ipfnDensity: %s", ipfnDensity);
		Ds.debug("  ipfnRapidBeat: %s", ipfnRapidBeat);
		Ds.debug("  ipfnPower: %s", ipfnPower);
		Ds.debug("  ipfnAdjust: %s", ipfnAdjust);
		Ds.debug("  satulateTotalScore: %s", satulateTotalScore);
		Ds.debug("  leastPoints: %s", leastPoints);
		Ds.debug("  resistHolding: %s", resistHolding);
		Ds.debug("  influenceOtherNoteScore: %s", influenceOtherNoteScore);
		Ds.debug("  influenceLowHandScore: %s", influenceLowHandScore);
		Ds.debug("  continueScratchFingering: %s", continueScratchFingering);
		Ds.debug("  adjustScoreAtScratch: %s", adjustScoreAtScratch);
		Ds.debug("  densityAcceptableRate: %s", densityAcceptableRate);
		Ds.debug("  powerDownwardRate: %s", powerDownwardRate);
		Ds.debug("  timeRangeNotes: %s", timeRangeNotes);
		Ds.debug("  timeRangeResist: %s", timeRangeResist);
		Ds.debug("  timeRangeDensity: %s", timeRangeDensity);
		Ds.debug("  timeRangeRapidBeat: %s", timeRangeRapidBeat);
		Ds.debug("}");
	}

	/**
	 * 共通セットアップ処理
	 */
	private void setup() {
		// 補間関数から抽出した定数値
		timeRangeNotes = ipfnNotes.getInRange();
		timeRangeResist = ipfnResist.getInRange();
		timeRangeDensity = ipfnDensity.getInRange();
		timeRangeRapidBeat = ipfnRapidBeat.getInRange();
	}
}
