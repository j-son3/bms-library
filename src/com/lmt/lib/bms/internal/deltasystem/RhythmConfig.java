package com.lmt.lib.bms.internal.deltasystem;

import java.util.Properties;

import com.lmt.lib.bms.bemusic.BeMusicRatingType;
import com.lmt.lib.bms.internal.LinearInterpolateFunction;

/**
 * 譜面傾向「RHYTHM」の設定クラス
 */
class RhythmConfig extends RatingConfig {
	/** インスタンス */
	private static RhythmConfig sInstance = new RhythmConfig();

	/** リズム範囲時間による評価点の補間を行う補間関数 */
	LinearInterpolateFunction ipfnRangeTime = LinearInterpolateFunction.create(
			1.2, 1.0, 0.0, 0.65, 0.96, 0.98, 0.99, 0.98, 0.96, 0.93, 0.9, 0.85, 0.78, 0.68, 0.57, 0.45, 0.33, 0.25, 0.19, 0.15, 0.11, 0.08, 0.06, 0.045, 0.03, 0.02, 0.015, 0.012, 0.009, 0.006, 0.003, 0.0);
	/** 前リズム範囲からの間隔による評価点の補間を行う補間関数 */
	LinearInterpolateFunction ipfnInterval = LinearInterpolateFunction.create(
			1.2, 1.0, 1.0, 0.97, 0.88, 0.69, 0.5, 0.39, 0.3, 0.24, 0.19, 0.15, 0.12, 0.1, 0.075, 0.06, 0.05, 0.04, 0.03, 0.018, 0.01, 0.005);
	/** リズム範囲のノート密度による評価点の補間を行う補間関数 */
	LinearInterpolateFunction ipfnDensity = LinearInterpolateFunction.create(
			50.0, 1.0, 0.0, 0.25, 0.4, 0.52, 0.62, 0.68, 0.74, 0.79, 0.83, 0.86, 0.88, 0.895, 0.905, 0.913, 0.922, 0.93, 0.937, 0.943, 0.95, 0.955);
	/** リズム変化頻度(回/秒)による評価点補正倍率の計算を行う補間関数 */
	LinearInterpolateFunction ipfnChangeRate = LinearInterpolateFunction.create(
			4.1, 1.5, 0.467, 0.55, 0.59, 0.615, 0.64, 0.66, 0.673, 0.686, 0.7, 0.712, 0.727, 0.75, 0.78, 0.82, 0.85, 0.88, 0.91, 0.945, 0.965, 0.975, 0.981, 0.985, 0.988, 0.991, 0.993, 0.995, 0.997, 0.998, 0.999, 1.0);
	/** 最終的なRHYTHM値を算出するための補間関数 */
	LinearInterpolateFunction ipfnRhythm = LinearInterpolateFunction.create(
			1.3, 20000.0, 0.0, 0.011, 0.018, 0.035, 0.055, 0.08, 0.12, 0.18, 0.26, 0.35, 0.45, 0.55, 0.63, 0.71, 0.77, 0.82, 0.87, 0.92, 0.96, 1.0);

	/** リズム範囲時間の評価点影響係数：小刻みのリズムに対する影響度を示す */
	double influenceRangeTime = 0.6;
	/** 前リズム範囲からの間隔の評価点影響係数：リズム切り替えの速さに対する影響度を示す */
	double influenceInterval = 1.3;
	/** リズム範囲のノート密度の評価点影響係数：リズムを刻むのに地力を必要とする際の影響度を示す */
	double influenceDensity = 1.1;

	/** タイムライン全体の最終評価点影響係数(SP) */
	double spInfluenceAllSide = 0.84;
	/** タイムライン左サイドの最終評価点影響係数(SP) */
	double spInfluenceLeftSide = 0.11;
	/** タイムライン右サイドの最終評価点影響係数(SP) */
	double spInfluenceRightSide = 0.07;
	/** レーン全体の最終評価点影響係数(DP) */
	double dpInfluenceAllSide = 0.82;
	/** 左レーンの最終評価点影響係数(DP) */
	double dpInfluenceLeftSide = 0.10;
	/** 右レーンの最終評価点影響係数(DP) */
	double dpInfluenceRightSide = 0.10;

	/** 同一楽曲位置と認識する時間のズレ幅 */
	double acceptableTimeDelta = 0.0167;
	/** リズム範囲占有率の最小値 */
	double minRangeRate = 0.0003;
	/** 平均密度の減衰開始時間(リズム範囲時間がこの値より小さい場合に密度の値を減衰させる) */
	double densityDecayTime = 0.3;

	/** 同一リズムのリピート検出時、最大何パターンのリピートを検出するか */
	int maxPatternRepeatCount = 4;
	/** 同一リズムのリピート対象となるリズム範囲評価点の減点率(パターン数が少ないほど減点率が高くなる) */
	double adjustPatternRepeatRate = 0.16;

	/** 最大刻み時間(刻み時間がこの値より大きいとリズム範囲とならない) */
	double maxPulseTime = 1.2;
	/** リズム範囲の最大時間(この値は範囲数のカウント時に長いリズム範囲を分割してカウントする際に用いる) */
	double maxRangeTime = 2.6;
	/** 評価点計算時の密度の最大値 */
	double maxDensity;

	/**
	 * インスタンス取得
	 * @return インスタンス
	 */
	static RhythmConfig getInstance() {
		return sInstance;
	}

	/**
	 * コンストラクタ
	 */
	private RhythmConfig() {
		setup();
	}

	/**
	 * タイムライン全体の最終評価点影響係数取得
	 * @param ctx Delta System用コンテキスト
	 * @return タイムライン全体の最終評価点影響係数
	 */
	double influenceAllSide(DsContext ctx) {
		return ctx.dpMode ? this.dpInfluenceAllSide : this.spInfluenceAllSide;
	}

	/**
	 * タイムライン左サイドの最終評価点影響係数取得
	 * @param ctx Delta System用コンテキスト
	 * @return タイムライン左サイドの最終評価点影響係数
	 */
	double influenceLeftSide(DsContext ctx) {
		return ctx.dpMode ? this.dpInfluenceLeftSide : this.spInfluenceLeftSide;
	}

	/**
	 * タイムライン右サイドの最終評価点影響係数取得
	 * @param ctx Delta System用コンテキスト
	 * @return タイムライン右サイドの最終評価点影響係数
	 */
	double influenceRightSide(DsContext ctx) {
		return ctx.dpMode ? this.dpInfluenceRightSide : this.spInfluenceRightSide;
	}

	/** {@inheritDoc} */
	@Override
	void load(Properties config) {
		var loader = new ConfigLoader(BeMusicRatingType.RHYTHM, config);
		ipfnRangeTime = loader.ipfnLinear("ipfn_range_time", ipfnRangeTime);
		ipfnInterval = loader.ipfnLinear("ipfn_interval", ipfnInterval);
		ipfnDensity = loader.ipfnLinear("ipfn_density", ipfnDensity);
		ipfnChangeRate = loader.ipfnLinear("ipfn_change_rate", ipfnChangeRate);
		ipfnRhythm = loader.ipfnLinear("ipfn_rhythm", ipfnRhythm);
		influenceInterval = loader.numeric("influence_interval", influenceInterval);
		influenceDensity = loader.numeric("influence_density", influenceDensity);
		spInfluenceAllSide = loader.numeric("sp_influence_all_side", spInfluenceAllSide);
		spInfluenceLeftSide = loader.numeric("sp_influence_left_side", spInfluenceLeftSide);
		spInfluenceRightSide = loader.numeric("sp_influence_right_side", spInfluenceRightSide);
		dpInfluenceAllSide = loader.numeric("dp_influence_all_side", dpInfluenceAllSide);
		dpInfluenceLeftSide = loader.numeric("dp_influence_left_side", dpInfluenceLeftSide);
		dpInfluenceRightSide = loader.numeric("dp_influence_right_side", dpInfluenceRightSide);
		acceptableTimeDelta = loader.numeric("acceptable_time_delta", acceptableTimeDelta);
		minRangeRate = loader.numeric("min_range_rate", minRangeRate);
		densityDecayTime = loader.numeric("density_decay_time", densityDecayTime);
		maxPatternRepeatCount = loader.integer("max_pattern_repeat_count", maxPatternRepeatCount);
		adjustPatternRepeatRate = loader.numeric("adjust_pattern_repeat_rate", adjustPatternRepeatRate);
		maxPulseTime = loader.numeric("max_pulse_time", maxPulseTime);
		maxRangeTime = loader.numeric("max_range_time", maxRangeTime);
		setup();
	}

	/** {@inheritDoc} */
	@Override
	void print() {
		Ds.debug("rhythmConfig: {");
		Ds.debug("  ipfnRangeTime: %s", ipfnRangeTime);
		Ds.debug("  ipfnInterval: %s", ipfnInterval);
		Ds.debug("  ipfnDensity: %s", ipfnDensity);
		Ds.debug("  ipfnChangeRate: %s", ipfnChangeRate);
		Ds.debug("  ipfnRhythm: %s", ipfnRhythm);
		Ds.debug("  influenceInterval: %s", influenceInterval);
		Ds.debug("  influenceDensity: %s", influenceDensity);
		Ds.debug("  spInfluenceAllSide: %s", spInfluenceAllSide);
		Ds.debug("  spInfluenceLeftSide: %s", spInfluenceLeftSide);
		Ds.debug("  spInfluenceRightSide: %s", spInfluenceRightSide);
		Ds.debug("  dpInfluenceAllSide: %s", dpInfluenceAllSide);
		Ds.debug("  dpInfluenceLeftSide: %s", dpInfluenceLeftSide);
		Ds.debug("  dpInfluenceRightSide: %s", dpInfluenceRightSide);
		Ds.debug("  acceptableTimeDelta: %s", acceptableTimeDelta);
		Ds.debug("  minRangeRate: %s", minRangeRate);
		Ds.debug("  densityDecayTime: %s", densityDecayTime);
		Ds.debug("  maxPatternRepeatCount: %s", maxPatternRepeatCount);
		Ds.debug("  adjustPatternRepeatRate: %s", adjustPatternRepeatRate);
		Ds.debug("  maxPulseTime: %s", maxPulseTime);
		Ds.debug("  maxRangeTime: %s", maxRangeTime);
		Ds.debug("}");
	}

	/**
	 * 共通セットアップ処理
	 */
	private void setup() {
		maxDensity = ipfnDensity.getInRange();
	}
}
