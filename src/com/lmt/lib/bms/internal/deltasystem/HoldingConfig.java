package com.lmt.lib.bms.internal.deltasystem;

import java.util.Properties;

import com.lmt.lib.bms.bemusic.BeMusicNoteType;
import com.lmt.lib.bms.bemusic.BeMusicRatingType;
import com.lmt.lib.bms.internal.LinearInterpolateFunction;

/**
 * 譜面傾向「HOLDING」の設定クラス
 */
class HoldingConfig extends RatingConfig {
	/** インスタンス */
	private static final HoldingConfig sInstance = new HoldingConfig();

	/** LN/CNにおいて長押しノートの長さから抵抗値の反映率を計算する補間関数 */
	LinearInterpolateFunction ipfnDecay = LinearInterpolateFunction.create(
			3.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 0.995, 0.985, 0.975, 0.96, 0.94, 0.92, 0.89, 0.85, 0.8, 0.76, 0.71, 0.67, 0.63, 0.6, 0.575, 0.555, 0.53, 0.515, 0.5);
	/** 連動指による動作抵抗の値を計算する補間関数 */
	LinearInterpolateFunction ipfnResist = LinearInterpolateFunction.create(
			0.5, 1.0, 0.0, 0.0, 0.82, 0.87, 0.747, 0.6, 0.526, 0.43, 0.353, 0.305, 0.275, 0.24, 0.22, 0.195, 0.175, 0.165, 0.15, 0.14, 0.125, 0.11, 0.1, 0.09, 0.077, 0.064, 0.052, 0.041, 0.03, 0.02, 0.01, 0.0);
	/** 総ノート数に対する長押し範囲のノート数の比率から最終評価点の補正率を計算する補間関数 */
	LinearInterpolateFunction ipfnInLnNotes = LinearInterpolateFunction.create(
			1.0, 1.2, 0.5, 0.56, 0.61, 0.655, 0.695, 0.725, 0.75, 0.77, 0.785, 0.795, 0.805, 0.812, 0.817, 0.822, 0.827, 0.834, 0.84, 0.847, 0.855, 0.862, 0.868, 0.873, 0.88, 0.886, 0.892, 0.898, 0.904, 0.908, 0.912, 0.916);
	/** 総ノート数に対する長押しノート数の比率から最終評価点の補正率を計算する補間関数 */
	LinearInterpolateFunction ipfnLnCount = LinearInterpolateFunction.create(
			1.0, 1.2, 0.57, 0.68, 0.76, 0.81, 0.845, 0.865, 0.87, 0.875, 0.88, 0.882, 0.885, 0.887, 0.89, 0.893, 0.895, 0.897, 0.899, 0.901, 0.903, 0.905, 0.907, 0.909, 0.91, 0.911, 0.912, 0.913, 0.914, 0.915, 0.916, 0.917);
	/** 演奏時間に対する長押し時間の比率から最終評価点の補正率を計算する補間関数 */
	LinearInterpolateFunction ipfnLnTime = LinearInterpolateFunction.create(
			1.0, 1.2, 0.17, 0.42, 0.57, 0.64, 0.69, 0.725, 0.76, 0.78, 0.8, 0.813, 0.825, 0.83, 0.835, 0.839, 0.843, 0.847, 0.85, 0.854, 0.858, 0.864, 0.867, 0.873, 0.878, 0.884, 0.89, 0.895, 0.901, 0.906, 0.912, 0.917);
	/** 最終的なHOLDING値を算出する補間関数 */
	LinearInterpolateFunction ipfnHolding = LinearInterpolateFunction.create(
			0.39, 20000.0, 0.0, 0.12, 0.23, 0.35, 0.44, 0.53, 0.61, 0.68, 0.75, 0.79, 0.83, 0.86, 0.89, 0.91, 0.93, 0.95, 0.965, 0.98, 0.99, 1.0);

	/** ノート評価点の基本点：短押し */
	double noteScoreBaseBeat = 0.04;
	/** ノート評価点の基本点：長押し開始 */
	double noteScoreBaseLongOn = 0.02;
	/** ノート評価点の基本点：長押し終了 */
	double noteScoreBaseLongOff = 0.005;
	/** ノート評価点の基本点：長押し終了(CN/HCN) */
	double noteScoreBaseChargeOff = 0.01;

	/** ノート評価点倍率：短押し */
	double noteScoreRateBeat = 1.55;
	/** ノート評価点倍率：長押し開始 */
	double noteScoreRateLongOn = 0.6;
	/** ノート評価点倍率：長押し終了 */
	double noteScoreRateLongOff = 0.52;
	/** ノート評価点倍率：長押し終了(CN/HCN) */
	double noteScoreRateChargeOff = 0.62;

	/** 抵抗値倍率：短押し */
	double resistRateBeat = 0.2;
	/** 抵抗値倍率：長押し */
	double resistRateHolding = 0.9;
	/** ノート評価点の飽和値 */
	double satulateNoteScore = 1.3;

	/** 1個の長押し範囲の最小秒数 */
	double minRangeLength = 0.066;
	/** 指の動作抵抗を受ける最大秒数 */
	double maxResistRange;

	/**
	 * インスタンス取得
	 * @return インスタンス
	 */
	static HoldingConfig getInstance() {
		return sInstance;
	}

	/**
	 * コンストラクタ
	 */
	private HoldingConfig() {
		setup();
	}

	/**
	 * ノート種別に応じたノート評価点の基本点取得
	 * @param ntype ノート種別
	 * @return ノート評価点の基本点
	 */
	final double noteScoreBase(BeMusicNoteType ntype) {
		switch (ntype) {
		case BEAT: return noteScoreBaseBeat;
		case LONG_ON: return noteScoreBaseLongOn;
		case LONG_OFF: return noteScoreBaseLongOff;
		case CHARGE_OFF: return noteScoreBaseChargeOff;
		default: return 0.0; // 想定外
		}
	}

	/**
	 * ノート種別に応じたノート評価点倍率取得
	 * @param ntype ノート種別
	 * @return ノート評価点倍率
	 */
	final double noteScoreRate(BeMusicNoteType ntype) {
		switch (ntype) {
		case BEAT: return noteScoreRateBeat;
		case LONG_ON: return noteScoreRateLongOn;
		case LONG_OFF: return noteScoreRateLongOff;
		case CHARGE_OFF: return noteScoreRateChargeOff;
		default: return 0.0; // 想定外
		}
	}

	/**
	 * ノート種別に応じた抵抗値倍率取得
	 * @param ntype ノート種別
	 * @return 抵抗値倍率
	 */
	final double resistRate(BeMusicNoteType ntype) {
		return (ntype == BeMusicNoteType.BEAT) ? resistRateBeat : resistRateHolding;
	}

	/** {@inheritDoc} */
	@Override
	void load(Properties config) {
		var loader = new ConfigLoader(BeMusicRatingType.HOLDING, config);
		ipfnDecay = loader.ipfnLinear("ipfn_decay", ipfnDecay);
		ipfnResist = loader.ipfnLinear("ipfn_resist", ipfnResist);
		ipfnInLnNotes = loader.ipfnLinear("ipfn_in_ln_notes", ipfnInLnNotes);
		ipfnLnCount = loader.ipfnLinear("ipfn_ln_count", ipfnLnCount);
		ipfnLnTime = loader.ipfnLinear("ipfn_ln_time", ipfnLnTime);
		ipfnHolding = loader.ipfnLinear("ipfn_holding", ipfnHolding);
		noteScoreBaseBeat = loader.numeric("note_score_base_beat", noteScoreBaseBeat);
		noteScoreBaseLongOn = loader.numeric("note_score_base_long_on", noteScoreBaseLongOn);
		noteScoreBaseLongOff = loader.numeric("note_score_base_long_off", noteScoreBaseLongOff);
		noteScoreBaseChargeOff = loader.numeric("note_score_base_charge_off", noteScoreBaseChargeOff);
		noteScoreRateBeat = loader.numeric("note_score_rate_beat", noteScoreRateBeat);
		noteScoreRateLongOn = loader.numeric("note_score_rate_long_on", noteScoreRateLongOn);
		noteScoreRateLongOff = loader.numeric("note_score_rate_long_off", noteScoreRateLongOff);
		noteScoreRateChargeOff = loader.numeric("note_score_rate_charge_off", noteScoreRateChargeOff);
		resistRateBeat = loader.numeric("resist_rate_beat", resistRateBeat);
		resistRateHolding = loader.numeric("resist_rate_holding", resistRateHolding);
		satulateNoteScore = loader.numeric("satulate_note_score", satulateNoteScore);
		minRangeLength = loader.numeric("min_range_length", minRangeLength);
	}

	/** {@inheritDoc} */
	@Override
	void print() {
		Ds.debug("holdingConfig: {");
		Ds.debug("  ipfnDecay: %s", ipfnDecay);
		Ds.debug("  ipfnResist: %s", ipfnResist);
		Ds.debug("  ipfnInLnNotes: %s", ipfnInLnNotes);
		Ds.debug("  ipfnLnCount: %s", ipfnLnCount);
		Ds.debug("  ipfnLnTime: %s", ipfnLnTime);
		Ds.debug("  ipfnHolding: %s", ipfnHolding);
		Ds.debug("  noteScoreBaseBeat: %s", noteScoreBaseBeat);
		Ds.debug("  noteScoreBaseLongOn: %s", noteScoreBaseLongOn);
		Ds.debug("  noteScoreBaseLongOff: %s", noteScoreBaseLongOff);
		Ds.debug("  noteScoreBaseChargeOff: %s", noteScoreBaseChargeOff);
		Ds.debug("  noteScoreRateBeat: %s", noteScoreRateBeat);
		Ds.debug("  noteScoreRateLongOn: %s", noteScoreRateLongOn);
		Ds.debug("  noteScoreRateLongOff: %s", noteScoreRateLongOff);
		Ds.debug("  noteScoreRateChargeOff: %s", noteScoreRateChargeOff);
		Ds.debug("  resistRateBeat: %s", resistRateBeat);
		Ds.debug("  resistRateHolding: %s", resistRateHolding);
		Ds.debug("  satulateNoteScore: %s", satulateNoteScore);
		Ds.debug("  minRangeLength: %s", minRangeLength);
		Ds.debug("  maxResistRange: %s", maxResistRange);
		Ds.debug("}");
	}

	/**
	 * 共通セットアップ処理
	 */
	private void setup() {
		maxResistRange = ipfnResist.getInRange();
	}
}
