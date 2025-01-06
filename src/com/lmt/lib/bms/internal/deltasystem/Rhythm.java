package com.lmt.lib.bms.internal.deltasystem;

/**
 * RHYTHM分析用の各種列挙型、クラスの定義
 */
class Rhythm {
	/** サイド */
	enum Side {
		/** 全体 */
		ALL(0),
		/** 左サイド */
		LEFT(1),
		/** 右サイド */
		RIGHT(2);

		/** インデックス */
		int index;

		/**
		 * コンストラクタ
		 * @param i インデックス
		 */
		private Side(int i) {
			index = i;
		}
	}

	/** RHYTHM用コンテキスト(分析に必要なデータの定義) */
	static class Context {
		/** 演奏時間 */
		double totalTime;
		/** サイド */
		Rhythm.Side side;
		/** RHYTHMコンフィグ */
		RhythmConfig config;

		/**
		 * コンストラクタ
		 * @param totalTime 演奏時間
		 * @param side サイド
		 */
		Context(double totalTime, Rhythm.Side side) {
			this.totalTime = totalTime;
			this.side = side;
			this.config = RhythmConfig.getInstance();
		}
	}

	/** 分析結果データ */
	static class Score {
		/** 評価対象のサイド */
		Rhythm.Side side;
		/** リズム変化回数 */
		int changeCount;
		/** リズム変化率(回/秒) */
		double changePerSec;
		/** 要操作時間 */
		double movementTime;
		/** 要操作時間率(演奏時間に対する要操作時間の比率) */
		double movementRate;
		/** リズム範囲評価点 */
		double scoreRange;
		/** リズム変化頻度(回/秒)による評価点補正倍率 */
		double ratioChange;
		/** 最終評価点 */
		double score;

		/** {@inheritDoc} */
		@Override
		public String toString() {
			return String.format(
					"%s = change:{ count=%d, perSec=%.2f}, movement:{ time=%.2f, rate=%.2f }, score:{ range=%.4f, ratioChange=%.4f, final=%.4f }",
					side, changeCount, changePerSec, movementTime, movementRate, scoreRange, ratioChange, score);
		}
	}
}
