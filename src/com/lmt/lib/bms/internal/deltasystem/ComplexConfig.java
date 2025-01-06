package com.lmt.lib.bms.internal.deltasystem;

import java.util.Properties;

import com.lmt.lib.bms.bemusic.BeMusicRatingType;
import com.lmt.lib.bms.internal.LinearInterpolateFunction;

/**
 * 譜面傾向「COMPLEX」の設定クラス
 */
class ComplexConfig extends RatingConfig {
	/** インスタンス */
	private static final ComplexConfig sInstance = new ComplexConfig();

	/** 1つ前の楽曲位置が遠い場合に、距離に応じて楽曲位置評価点を減点する補間関数 */
	LinearInterpolateFunction ipfnPtReduce = LinearInterpolateFunction.create(
			1.5, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 0.995, 0.99, 0.98, 0.95, 0.91, 0.85, 0.77, 0.7, 0.66, 0.62, 0.6);
	/** 算出された「楽曲位置評価点」を規定範囲内の値に収め、評価点を意図する値に補正する補間関数 */
	LinearInterpolateFunction ipfnBasic = LinearInterpolateFunction.create(
			10.0, 2.0, 0.0, 0.13, 0.21, 0.25, 0.28, 0.31, 0.33, 0.36, 0.39, 0.43, 0.48, 0.53, 0.59, 0.65, 0.7, 0.75, 0.8, 0.85, 0.9, 0.95);
	/** 後方楽曲位置における「楽曲位置評価点」を現在楽曲位置にどの程度加味し加点するかの割合を決定する補間関数 */
	LinearInterpolateFunction ipfnBwRef = LinearInterpolateFunction.create(
			0.7, 1.0, 0.0, 0.56, 0.42, 0.255, 0.19, 0.14, 0.11, 0.097, 0.088, 0.081, 0.074, 0.068, 0.063, 0.058, 0.053, 0.049, 0.045, 0.042, 0.039, 0.036, 0.033, 0.03, 0.027, 0.024, 0.021, 0.018, 0.015, 0.012, 0.01, 0.007);
	/** 最終評価点計算用の補間関数(SP) */
	LinearInterpolateFunction ipfnComplex = LinearInterpolateFunction.create(
			0.4, 20000.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.003, 0.013, 0.048, 0.157, 0.357, 0.56, 0.677, 0.776, 0.825, 0.87, 0.91, 0.94, 0.955, 0.97, 0.982, 0.99, 0.995, 1.0);
	/** 最終評価点計算用の補間関数(DP) */
	LinearInterpolateFunction ipfnDpComplex = LinearInterpolateFunction.create(
			0.4, 20000.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.002, 0.012, 0.035, 0.099, 0.351, 0.56, 0.677, 0.776, 0.825, 0.87, 0.91, 0.94, 0.955, 0.97, 0.982, 0.99, 0.995, 1.0);

	/** 総合複雑度評価点の飽和点 */
	double satulateTotalScore = 2.5;
	/** 最低楽曲位置数(視覚効果を伴う楽曲位置数がこの値を下回ると最終評価点が減点される) */
	int leastPoints = 50;
	/** 楽曲位置評価において、ノート種別が長押し継続時の基本点減点率(SP) */
	double spDeductionHold = 0.86;
	/** 楽曲位置評価において、ノート種別が長押し終了時の基本点減点率(SP) */
	double spDeductionLnTail = 0.92;
	/** 楽曲位置評価において、ノート種別が地雷の時の基本点減点率(SP) */
	double spDeductionMine = 0.75;
	/** 楽曲位置評価において、ノート種別が長押し継続時の基本点減点率(DP) */
	double dpDeductionHold = 0.86;
	/** 楽曲位置評価において、ノート種別が長押し終了時の基本点減点率(DP) */
	double dpDeductionLnTail = 1.73;
	/** 楽曲位置評価において、ノート種別が地雷の時の基本点減点率(DP) */
	double dpDeductionMine = -0.88;
	/** 楽曲位置評価において、ノート種別数が評価点に影響を及ぼす係数(適正値は0～0.5が目安) */
	double typeCountRate = 0.03;
	/** 楽曲位置評価において、ノート種別切替回数が評価点に影響を及ぼす係数(適正値は0～0.5が目安) */
	double typeChangeRate = 0.03;
	/** 後方複雑度評価時、配置差分がない場合の最小倍率(0にすると当該楽曲位置の後方複雑度は評価点に加算されない) */
	double minBwRatioPatternDelta = 0.02;

	/** ダブルプレーで、最終評価点が高い側のレーンの評価点影響度 */
	double dpInfluenceScoreHigh = 0.63;
	/** ダブルプレーで、最終評価点が低い側のレーンの評価点影響度 */
	double dpInfluenceScoreLow = 0.51;
	/** ダブルプレーで、評価点影響度の調整を実施する最小の楽曲位置数比率 */
	double dpAdjustInfluenceRate = 0.33;
	/** ダブルプレーで、評価点影響度の最大調整幅(ScoreHigh/Lowのどちらか以下の値にすること) */
	double dpAdjustInfluenceMaxStrength = 0.42;

	/** 後方複雑度評価を行う範囲 */
	double timeRangeBwRef;

	/**
	 * コンストラクタ
	 */
	private ComplexConfig() {
		setup();
	}

	/**
	 * インスタンス取得
	 * @return インスタンス
	 */
	static ComplexConfig getInstance() {
		return sInstance;
	}

	/** {@inheritDoc} */
	@Override
	void load(Properties config) {
		var loader = new ConfigLoader(BeMusicRatingType.COMPLEX, config);
		ipfnPtReduce = loader.ipfnLinear("ipfn_pt_reduce", ipfnPtReduce);
		ipfnBasic = loader.ipfnLinear("ipfn_basic", ipfnBasic);
		ipfnBwRef = loader.ipfnLinear("ipfn_bwref", ipfnBwRef);
		ipfnComplex = loader.ipfnLinear("ipfn_complex", ipfnComplex);
		ipfnDpComplex = loader.ipfnLinear("ipfn_dp_complex", ipfnDpComplex);
		satulateTotalScore = loader.numeric("satulate_total_score", satulateTotalScore);
		leastPoints = loader.integer("least_points", leastPoints);
		spDeductionHold = loader.numeric("sp_deduction_hold", spDeductionHold);
		spDeductionLnTail = loader.numeric("sp_deduction_ln_tail", spDeductionLnTail);
		spDeductionMine = loader.numeric("sp_deduction_mine", spDeductionMine);
		dpDeductionHold = loader.numeric("dp_deduction_hold", dpDeductionHold);
		dpDeductionLnTail = loader.numeric("dp_deduction_ln_tail", dpDeductionLnTail);
		dpDeductionMine = loader.numeric("dp_deduction_mine", dpDeductionMine);
		dpAdjustInfluenceRate = loader.numeric("dp_adjust_influence_rate", dpAdjustInfluenceRate);
		dpAdjustInfluenceMaxStrength = loader.numeric("dp_adjust_influence_max_strength", dpAdjustInfluenceMaxStrength);
		typeCountRate = loader.numeric("type_count_rate", typeCountRate);
		typeChangeRate = loader.numeric("type_change_rate", typeChangeRate);
		minBwRatioPatternDelta = loader.numeric("min_bw_ratio_pattern_delta", minBwRatioPatternDelta);
		dpInfluenceScoreHigh = loader.numeric("dp_influence_score_high", dpInfluenceScoreHigh);
		dpInfluenceScoreLow = loader.numeric("dp_influence_score_low", dpInfluenceScoreLow);
		setup();
	}

	/** {@inheritDoc} */
	@Override
	void print() {
		Ds.debug("complexConfig: {");
		Ds.debug("  ipfnPtReduce: %s", ipfnPtReduce);
		Ds.debug("  ipfnBasic: %s", ipfnBasic);
		Ds.debug("  ipfnBwRef: %s", ipfnBwRef);
		Ds.debug("  ipfnComplex: %s", ipfnComplex);
		Ds.debug("  ipfnDpComplex: %s", ipfnDpComplex);
		Ds.debug("  satulateTotalScore: %s", satulateTotalScore);
		Ds.debug("  leastPoints: %s", leastPoints);
		Ds.debug("  spDeductionHold: %s", spDeductionHold);
		Ds.debug("  spDeductionLnTail: %s", spDeductionLnTail);
		Ds.debug("  spDeductionMine: %s", spDeductionMine);
		Ds.debug("  dpDeductionHold: %s", dpDeductionHold);
		Ds.debug("  dpDeductionLnTail: %s", dpDeductionLnTail);
		Ds.debug("  dpDeductionMine: %s", dpDeductionMine);
		Ds.debug("  typeCountRate: %s", typeCountRate);
		Ds.debug("  typeChangeRate: %s", typeChangeRate);
		Ds.debug("  minBwRatioPatternDelta: %s", minBwRatioPatternDelta);
		Ds.debug("  dpInfluenceScoreHigh: %s", dpInfluenceScoreHigh);
		Ds.debug("  dpInfluenceScoreLow: %s", dpInfluenceScoreLow);
		Ds.debug("  dpAdjustInfluenceRate: %s", dpAdjustInfluenceRate);
		Ds.debug("  dpAdjustInfluenceMaxStrength: %s", dpAdjustInfluenceMaxStrength);
		Ds.debug("}");
	}

	/**
	 * 楽曲位置評価でのノート種別が長押し継続時の基本点減点率取得
	 * @param ctx Delta System用コンテキスト
	 * @return ノート種別が長押し継続時の基本点減点率
	 */
	double deductionHold(DsContext ctx) {
		return ctx.dpMode ? this.dpDeductionHold : this.spDeductionHold;
	}

	/**
	 * 楽曲位置評価でのノート種別が長押し終了時の基本点減点率取得
	 * @param ctx Delta System用コンテキスト
	 * @return ノート種別が長押し終了時の基本点減点率
	 */
	double deductionLnTail(DsContext ctx) {
		return ctx.dpMode ? this.dpDeductionLnTail : this.spDeductionLnTail;
	}

	/**
	 * 楽曲位置評価でのノート種別が地雷時の基本点減点率取得
	 * @param ctx Delta System用コンテキスト
	 * @return ノート種別が地雷時の基本点減点率
	 */
	double deductionMine(DsContext ctx) {
		return ctx.dpMode ? this.dpDeductionMine : this.spDeductionMine;
	}

	/**
	 * 共通セットアップ処理
	 */
	private void setup() {
		timeRangeBwRef = ipfnBwRef.getInRange();
	}
}
