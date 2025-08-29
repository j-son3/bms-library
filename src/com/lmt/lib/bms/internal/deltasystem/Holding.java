package com.lmt.lib.bms.internal.deltasystem;

import java.util.List;

import com.lmt.lib.bms.bemusic.BeMusicDevice;
import com.lmt.lib.bms.bemusic.BeMusicLongNoteMode;

/**
 * HOLDING分析用の各種列挙型、クラスの定義
 */
class Holding {
	/** HOLDING用コンテキスト(分析に必要なデータの定義) */
	static class Context {
		/** 分析対象入力デバイスリスト */
		List<BeMusicDevice> devs;
		/** HOLDING用運指データ */
		Fingering fingering;
		/** LNモードかどうか */
		boolean isLn;
		/** HCNモードかどうか */
		boolean isHcn;
		/** コンフィグ */
		HoldingConfig config;

		/**
		 * コンストラクタ
		 * @param ctx コンテキスト
		 */
		Context(DsContext ctx) {
			this.devs = ctx.dpMode ? BeMusicDevice.orderedByDpList() : BeMusicDevice.orderedBySpLeftList();
			this.fingering = ctx.dpMode ? Fingering.DP_HOLDING : Fingering.SP_HOLDING;
			this.isLn = (ctx.header.getLnMode() == BeMusicLongNoteMode.LN);
			this.isHcn = (ctx.header.getLnMode() == BeMusicLongNoteMode.HCN);
			this.config = HoldingConfig.getInstance();
		}
	}

	/** 分析結果データ */
	static class Score {
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
		double holdingOrg = 0.0;

		/** ノート評価点サマリの実データ */
		ScoreSummarizer noteScoreSummary = null;

		/**
		 * LN時間比率取得
		 * @return LN時間比率
		 */
		double lnRate() {
			return (numNotesTotal == 0) ? 0.0 : ((double)numLn / (double)numNotesTotal);
		}

		/**
		 * LN時間比率(%)取得
		 * @return LN時間比率(%)
		 */
		double lnRatePer() {
			return lnRate() * 100.0;
		}

		/**
		 * LN内ノート数比率取得
		 * @return LN内ノート数比率
		 */
		double notesInLnRate() {
			return (numNotesInLn == 0) ? 0.0 : ((double)numNotesInLn / (double)numNotesTotal);
		}

		/**
		 * LN内ノート数比率(%)取得
		 * @return LN内ノート数比率(%)
		 */
		double notesInLnRatePer() {
			return notesInLnRate() * 100.0;
		}

		/**
		 * LN時間比率取得
		 * @return LN時間比率
		 */
		double lnTimeRate() {
			return (playTime == 0.0) ? 0.0 : (lnTime / playTime);
		}

		/**
		 * LN時間比率(%)取得
		 * @return LN時間比率(%)
		 */
		double lnTimeRatePer() {
			return lnTimeRate() * 100.0;
		}
	}
}
