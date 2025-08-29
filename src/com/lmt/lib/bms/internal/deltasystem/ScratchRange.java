package com.lmt.lib.bms.internal.deltasystem;

import java.util.TreeMap;
import java.util.stream.Stream;

/**
 * スクラッチ範囲データ
 */
abstract class ScratchRange {
	/** 単体操作 */
	static class Single extends ScratchRange {
		/**
		 * コンストラクタ
		 * @param pos 対象スクラッチの要素リストインデックス
		 */
		Single(int pos) {
			super(Scratch.Type.SINGLE);
			this.first = new ScratchEvaluation(pos);
			this.last = this.first;
		}
	}

	/** 連続操作 */
	static class Pulse extends ScratchRange {
		/** スクラッチの刻み時間 */
		double pulseTime;
		/** 要素リストインデックスとスクラッチ評価データのマップ */
		TreeMap<Integer, ScratchEvaluation> scratches = new TreeMap<>();

		/**
		 * コンストラクタ
		 * @param pulseTime スクラッチの刻み時間
		 */
		Pulse(double pulseTime) {
			super(Scratch.Type.PULSE);
			this.pulseTime = pulseTime;
		}

		/**
		 * スクラッチ評価データ追加
		 * @param pos 要素リストインデックス
		 */
		void put(int pos) {
			this.scratches.put(pos, new ScratchEvaluation(pos));
		}

		/**
		 * 現在のデータでスクラッチ範囲確定
		 */
		void determineEnds() {
			this.first = this.scratches.firstEntry().getValue();
			this.last = this.scratches.lastEntry().getValue();
		}

		/** {@inheritDoc} */
		@Override
		String detail() {
			return String.format("%3d/%-6.3f", this.scratches.size(), this.pulseTime);
		}

		/** {@inheritDoc} */
		@Override
		ScratchEvaluation getEvaluation(int pos) {
			var eval = this.scratches.floorEntry(pos);
			return ((eval != null) && (eval.getKey() == pos)) ? eval.getValue() : null;
		}

		/** {@inheritDoc} */
		@Override
		Stream<ScratchEvaluation> evaluations() {
			return this.scratches.values().stream();
		}
	}

	/** 往復操作 */
	static class RoundTrip extends ScratchRange {
		/**
		 * コンストラクタ
		 * @param first 範囲開始位置
		 * @param last 範囲終了位置
		 */
		RoundTrip(int first, int last) {
			super(Scratch.Type.ROUND_TRIP, first, last);
		}
	}

	/** 回転操作 */
	static class Spinning extends ScratchRange {
		/** 回転操作の終点で逆回転操作が必要かどうか */
		boolean turn;
		/** 回転操作の全体が評価対象かどうか */
		boolean full;
		/** このスクラッチ範囲での平均ノート密度 */
		double density = 0.0;

		/**
		 * コンストラクタ
		 * @param first 範囲開始位置
		 * @param last 範囲終了位置
		 * @param turn 回転操作の終点で逆回転操作が必要かどうか
		 * @param full 回転操作の全体が評価対象かどうか
		 */
		Spinning(int first, int last, boolean turn, boolean full) {
			super(Scratch.Type.SPINNING, first, last);
			this.turn = turn;
			this.full = full;
		}

		/** {@inheritDoc} */
		@Override
		String detail() {
			return String.format("%c/%c/%-6.2f", (this.turn ? 'T' : '-'), (this.full ? 'F' : '-'), this.density);
		}
	}

	/** スクラッチ範囲種別 */
	Scratch.Type type;
	/** 次のスクラッチ範囲データ */
	ScratchRange next = null;
	/** 範囲開始位置のスクラッチ評価 */
	ScratchEvaluation first = null;
	/** 範囲終了位置のスクラッチ評価 */
	ScratchEvaluation last = null;
	/** 後方楽曲位置の開始位置 */
	int behind = -1;
	/** 前のスクラッチ範囲からこのスクラッチ範囲開始位置までの時間(後方時間) */
	double behindTime = 0.0;
	/** このスクラッチ範囲の時間 */
	double scratchingTime = 0.0;

	/**
	 * コンストラクタ
	 * @param type スクラッチ範囲種別
	 */
	protected ScratchRange(Scratch.Type type) {
		this.type = type;
	}

	/**
	 * コンストラクタ
	 * @param type スクラッチ範囲種別
	 * @param first 範囲開始位置
	 * @param last 範囲終了位置
	 */
	protected ScratchRange(Scratch.Type type, int first, int last) {
		this.type = type;
		this.first = new ScratchEvaluation(first);
		this.last = new ScratchEvaluation(last);
	}

	/**
	 * スクラッチ範囲の詳細情報文字列生成
	 * @return スクラッチ範囲の詳細情報文字列
	 */
	String detail() {
		return "          ";
	}

	/**
	 * 指定位置のスクラッチ評価データ取得
	 * @param pos 要素リストインデックス
	 * @return posに該当するスクラッチ評価データ、該当なければnull
	 */
	ScratchEvaluation getEvaluation(int pos) {
		if (pos == this.first.position) {
			return this.first;
		} else if (pos == this.last.position) {
			return this.last;
		} else {
			return null;
		}
	}

	/**
	 * スクラッチ評価データのストリーム取得
	 * @return スクラッチ評価データのストリーム
	 */
	Stream<ScratchEvaluation> evaluations() {
		return (this.first.position == this.last.position) ? Stream.of(this.first) : Stream.of(this.first, this.last);
	}
}
