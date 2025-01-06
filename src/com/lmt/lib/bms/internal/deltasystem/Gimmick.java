package com.lmt.lib.bms.internal.deltasystem;

import java.util.List;

import com.lmt.lib.bms.bemusic.BeMusicDevice;

/**
 * GIMMICK分析用の各種列挙型、クラスの定義
 */
class Gimmick {
	/** GIMMICK用コンテキスト(分析に必要なデータの定義) */
	static class Context {
		/** Delta System用コンテキスト */
		DsContext ctx;
		/** 要素データリスト */
		List<GimmickElement> elems;
		/** 分析対象入力デバイスリスト */
		List<BeMusicDevice> devs;
		/** 運指 */
		Fingering fingering;
		/** コンフィグ */
		GimmickConfig config;

		/**
		 * コンストラクタ
		 * @param ctx Delta System用コンテキスト
		 */
		Context(DsContext ctx, List<GimmickElement> elems) {
			this.ctx = ctx;
			this.elems = elems;
			this.devs = ctx.dpMode ? BeMusicDevice.orderedByDpList() : BeMusicDevice.orderedBySpLeftList();
			this.fingering = ctx.dpMode ? Fingering.DP_MINE : Fingering.SP_MINE;
			this.config = GimmickConfig.getInstance();
		}
	}

	/** 分析結果データ */
	static class Score {
		/** 総ノート数 */
		int totalNotes;
		/** 演奏時間 */
		double playTime;

		/** 速度変更範囲データ数 */
		int speedNumRange = 0;
		/** 速度変更知覚数 */
		int speedNumChangePerceived = 0;
		/** ギアチェン回数 */
		int speedNumChangeGear = 0;
		/** 速度変更難易度 */
		double speedScoreRangeSum = 0.0;
		/** 速度変更影響率評価点 */
		double speedScoreChgRate = 0.0;
		/** 速度変更の総合評価点 */
		double speedScoreOrg = 0.0;
		/** 速度変更のGIMMICK値 */
		int speedScore = 0;

		/** 譜面停止範囲データ数 */
		int stopNumRange = 0;
		/** 譜面停止の視覚効果影響数 */
		int stopNumEffective = 0;
		/** 譜面停止対応難易度のサマリ */
		ScoreSummarizer stopSummary = null;
		/** 譜面停止の視覚効果影響時間 */
		double stopAreaTime = 0.0;
		/** 譜面停止対応難易度 */
		double stopScoreRange = 0.0;
		/** 譜面停止範囲影響率評価点 */
		double stopScoreArea = 0.0;
		/** 譜面停止の総合評価点 */
		double stopScoreOrg = 0.0;
		/** 譜面停止のGIMMICK値 */
		int stopScore = 0;

		/** 地雷範囲データ数 */
		int mineNumRange = 0;
		/** 地雷の影響数 */
		int mineNumEffective = 0;
		/** 地雷回避難易度のサマリ */
		ScoreSummarizer mineSummary = null;
		/** 地雷の演奏への影響時間 */
		double mineEffectiveTime = 0.0;
		/** 地雷回避難易度 */
		double mineScoreRange = 0.0;
		/** 地雷範囲影響率評価点 */
		double mineScoreEffective = 0.0;
		/** 地雷の総合評価点 */
		double mineScoreOrg = 0.0;
		/** 地雷のGIMMICK値 */
		int mineScore = 0;

		/** 総合GIMMICK値 */
		int gimmick = 0;

		/**
		 * 譜面停止の視覚効果影響率取得
		 * @return 譜面停止の視覚効果影響率
		 */
		final double stopAreaRate() {
			return (playTime == 0.0) ? 0.0 : (stopAreaTime / playTime);
		}

		/**
		 * 譜面停止の視覚効果影響率(%)取得
		 * @return 譜面停止の視覚効果影響率(%)
		 */
		final double stopAreaRatePer() {
			return stopAreaRate() * 100.0;
		}

		/**
		 * 地雷範囲影響率取得
		 * @return 地雷範囲影響率
		 */
		final double mineEffectiveRate() {
			return (playTime == 0.0) ? 0.0 : (mineEffectiveTime / playTime);
		}

		/**
		 * 地雷範囲影響率(%)取得
		 * @return 地雷範囲影響率(%)
		 */
		final double mineEffectiveRatePer() {
			return mineEffectiveRate() * 100.0;
		}
	}
}
