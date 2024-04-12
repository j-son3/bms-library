package com.lmt.lib.bms.internal.deltasystem;

import java.util.List;

import com.lmt.lib.bms.bemusic.BeMusicLane;

/**
 * HOLDINGのタイムライン分析結果
 */
class HoldingScore {
	/** 分析対象レーン */
	BeMusicLane lane;
	/** 分析対象要素リスト */
	List<HoldingElement> elems;
	/** 総ノート数 */
	int numNotesTotal = 0;
	/** LN範囲内の総ノート数 */
	int numNotesInLn = 0;
	/** LN数 */
	int numLn = 0;
	/** 演奏時間 */
	double playTime = 0.0;
	/** LN時間 */
	double lnTime = 0.0;
	/** ノート評価点サマリ */
	double scoreUnitNotes = 0.0;
	/** LN範囲ノート比率評価点 */
	double scoreInLnNotes = 0.0;
	/** LN数比率評価点 */
	double scoreLnRate = 0.0;
	/** LN時間評価点 */
	double scoreLnTime = 0.0;
	/** 最終評価点 */
	double scoreOrg = 0.0;
	/** HOLDING値 */
	int score = 0;

	/** ノート評価点サマリの実データ */
	ScoreSummarizer noteScoreSummary = null;

	/**
	 * コンストラクタ
	 * @param lane 分析対象レーン
	 */
	HoldingScore(BeMusicLane lane) {
		this.lane = lane;
	}

	/**
	 * LN時間比率取得
	 * @return LN時間比率
	 */
	final double lnRate() {
		return (numNotesTotal == 0) ? 0.0 : ((double)numLn / (double)numNotesTotal);
	}

	/**
	 * LN時間比率(%)取得
	 * @return LN時間比率(%)
	 */
	final double lnRatePer() {
		return lnRate() * 100.0;
	}

	/**
	 * LN内ノート数比率取得
	 * @return LN内ノート数比率
	 */
	final double notesInLnRate() {
		return (numNotesInLn == 0) ? 0.0 : ((double)numNotesInLn / (double)numNotesTotal);
	}

	/**
	 * LN内ノート数比率(%)取得
	 * @return LN内ノート数比率(%)
	 */
	final double notesInLnRatePer() {
		return notesInLnRate() * 100.0;
	}

	/**
	 * LN時間比率取得
	 * @return LN時間比率
	 */
	final double lnTimeRate() {
		return (playTime == 0.0) ? 0.0 : (lnTime / playTime);
	}

	/**
	 * LN時間比率(%)取得
	 * @return LN時間比率(%)
	 */
	final double lnTimeRatePer() {
		return lnTimeRate() * 100.0;
	}
}