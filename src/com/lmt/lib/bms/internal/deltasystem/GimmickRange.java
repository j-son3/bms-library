package com.lmt.lib.bms.internal.deltasystem;

import java.util.List;
import java.util.TreeMap;

/**
 * 譜面傾向「GIMMICK」分析用の範囲データ
 * @param <R> 範囲データの具象クラス
 */
abstract class GimmickRange<R> extends RatingRange<R> {
	/** この範囲データの個別評価点 */
	double score = 0.0;

	/**
	 * 速度変更範囲
	 */
	static class Speed extends GimmickRange<Speed> {
		/** この範囲の速度変更時に影響する要素リストの最初のインデックス */
		int firstMovement;
		/** この範囲の速度変更時に影響する要素リストの最後のインデックス */
		int lastMovement;
		/** この範囲の速度(BPM：スクロール方向が逆の場合はマイナス値) */
		double speed;
		/** 速度変更データ */
		TreeMap<Integer, Double> changes;
		/** この範囲に存在する譜面停止の範囲データ */
		TreeMap<Integer, Stop> stopMap;
		/** この範囲に存在する地雷の範囲データ */
		TreeMap<Integer, MineGroup> mineMap;
		/** この範囲のノート数 */
		int numNotes;
		/** この範囲の速度の理想度(ギアチェンを行わない場合、理想値から何%離れているかの値) */
		double ideality = 0.0;
		/** この範囲を操作するにあたりギアチェン操作を行うべきかどうか */
		boolean gearChange = false;
		/** ギアチェン対応難易度 */
		double scoreGearChange = 0.0;

		/**
		 * コンストラクタ
		 * @param first 範囲データの要素データリストの最初のインデックス
		 * @param last 範囲データの要素データリストの最後のインデックス
		 * @param firstMv この範囲の速度変更時に影響する要素リストの最初のインデックス
		 * @param lastMv この範囲の速度変更時に影響する要素リストの最後のインデックス
		 * @param numNotes この範囲のノート数
		 * @param changes 速度変更データ
		 */
		Speed(int first, int last, int firstMv, int lastMv, int numNotes, TreeMap<Integer, Double> changes) {
			super(first, last);
			this.firstMovement = firstMv;
			this.lastMovement = lastMv;
			this.speed = changes.lastEntry().getValue();
			this.changes = changes;
			this.stopMap = new TreeMap<>();
			this.mineMap = new TreeMap<>();
			this.numNotes = numNotes;
		}

		/**
		 * ギアチェン対応猶予時間計算
		 * @param elems 要素データリスト
		 * @return ギアチェン対応猶予時間
		 */
		final double timeGearChange(List<GimmickElement> elems) {
			return RatingElement.timeDelta(elems, changes.lastKey(), firstMovement);
		}

		/**
		 * 前の速度変更範囲の最終ノートからこの速度変更範囲のギアチェン開始点までの時間計算
		 * @param elems 要素データリスト
		 * @return 前の速度変更範囲の最終ノートからこの速度変更範囲のギアチェン開始点までの時間
		 */
		final double timeBeforeGearChange(List<GimmickElement> elems) {
			var indexGearChange = changes.lastKey();
			if (!hasPrev()) {
				// 先頭の速度変更範囲の場合はこの範囲の先頭からの時間
				return RatingElement.timeDelta(elems, first, indexGearChange);
			} else if (!prev.hasMovement()) {
				// 前の速度変更範囲が操作可能ノートを含まない場合は前の速度範囲先頭からの時間
				return RatingElement.timeDelta(elems, prev.first, indexGearChange);
			} else {
				// 前の速度変更範囲の最終操作可能ノート位置からの時間
				return RatingElement.timeDelta(elems, prev.lastMovement, indexGearChange);
			}
		}

		/**
		 * この範囲のスクロール方向が逆かどうか判定
		 * @return スクロール方向が逆であればtrue
		 */
		final boolean isReverseScroll() {
			return speed < 0.0;
		}

		/**
		 * 速度変更に影響する操作可能ノートの有無判定
		 * @return 速度変更に影響する操作可能ノートがあればtrue
		 */
		final boolean hasMovement() {
			return (firstMovement >= 0) && (lastMovement >= 0);
		}

		/**
		 * 速度変更に影響する操作可能ノートの数取得
		 * @return 速度変更に影響する操作可能ノートの数
		 */
		final int numMovementPoint() {
			return lastMovement - firstMovement + 1;
		}

		/**
		 * 指定位置に対応する譜面停止範囲データ取得
		 * @param pos 要素データリストのインデックス
		 * @return 指定位置に対応する譜面停止範囲データ、なければnull
		 */
		final Stop inboundStop(int pos) {
			var entry = stopMap.floorEntry(pos);
			return ((entry != null) && (entry.getValue().contains(pos))) ? entry.getValue() : null;
		}

		/**
		 * 指定位置に対応する地雷範囲データ取得
		 * @param pos 要素データリストのインデックス
		 * @return 指定位置に対応する地雷範囲データ、なければnull
		 */
		final MineGroup inboundMine(int pos) {
			var entry = mineMap.floorEntry(pos);
			return ((entry != null) && (entry.getValue().contains(pos))) ? entry.getValue() : null;
		}

		/**
		 * ギアチェン後のノート平均密度計算
		 * @param elems 要素データリスト
		 * @param time 計算に使用する最大時間
		 * @return ギアチェン後のノート平均密度
		 */
		final double computeGearChangeDensity(List<GimmickElement> elems, double time) {
			var notes = 0;
			var timeLast = elems.get(firstMovement).getTime() + time;
			for (var i = firstMovement; (i <= lastMovement) && (elems.get(i).getTime() <= timeLast); i++) {
				notes += elems.get(i).getPoint().getNoteCount();
			}
			return (double)notes / time;
		}
	}

	/**
	 * 譜面停止範囲データ
	 */
	static class Stop extends GimmickRange<Stop> {
		/** 譜面停止の視覚効果の影響を受ける操作可能ノートがある要素データリストの最初のインデックス */
		int firstInfluence;
		/** 譜面停止の視覚効果の影響を受ける操作可能ノートがある要素データリストの最後のインデックス */
		int lastInfluence;
		/** 譜面停止の視覚効果の影響を受ける操作可能ノートの数 */
		int notesInfluence;
		/** 譜面停止データ */
		TreeMap<Integer, Double> stops;

		/**
		 * コンストラクタ
		 * @param first 範囲データの要素データリストの最初のインデックス
		 * @param last 範囲データの要素データリストの最後のインデックス
		 * @param firstInfluence 譜面停止の視覚効果の影響を受ける操作可能ノートがある要素データリストの最初のインデックス
		 */
		Stop(int first, int last, int firstInfluence) {
			super(first, last);
			this.firstInfluence = firstInfluence;
			this.lastInfluence = firstInfluence;
			this.notesInfluence = 0;
			this.stops = new TreeMap<>();
		}

		/**
		 * この譜面停止範囲の最後の譜面停止データが存在する要素データリストのインデックス取得
		 * @return この譜面停止範囲の最後の譜面停止データが存在する要素データリストのインデックス
		 */
		final int lastStopPos() {
			return stops.lastKey();
		}

		/**
		 * 譜面停止の視覚効果の影響を受ける操作可能ノート有無取得
		 * @return 譜面停止の視覚効果の影響を受ける操作可能ノートがあればtrue
		 */
		final boolean hasInfluence() {
			return firstInfluence != -1;
		}

		/**
		 * 譜面停止の視覚効果の影響を受ける範囲のノート平均密度計算
		 * @param elems 要素データリスト
		 * @return 譜面停止の視覚効果の影響を受ける範囲のノート平均密度
		 */
		final double computeInfluenceDensity(List<GimmickElement> elems) {
			var density = 0.0;
			if (hasInfluence()) {
				var time = RatingElement.timeDelta(elems, firstInfluence, lastInfluence);
				density = (time == 0.0) ? notesInfluence : (notesInfluence / time);
			}
			return density;
		}

		/**
		 * 譜面停止範囲の視覚効果有効時間計算
		 * @param elems 要素データリスト
		 * @param minEffectiveTime 譜面停止が視覚効果を持つ最小の時間
		 * @return 譜面停止範囲の視覚効果有効時間
		 */
		final double computeEffectiveRangeTime(List<GimmickElement> elems, double minEffectiveTime) {
			// 譜面停止の視覚効果が認知できない譜面停止は、範囲として除外して効果範囲時間を計算する
			var time = 0.0;
			if (hasInfluence()) {
				var effective = -1;
				for (var stop : stops.entrySet()) {
					if (stop.getValue() >= minEffectiveTime) {
						effective = stop.getKey();
						break;
					}
				}
				time = (effective == -1) ? 0.0 : RatingElement.timeDelta(elems, effective, last);
			}
			return time;
		}
	}

	/**
	 * 地雷範囲データグループ
	 * <p>個別の地雷範囲データは範囲が競合するため、グループ化して管理する。</p>
	 */
	static class MineGroup extends GimmickRange<MineGroup> {
		TreeMap<Integer, Mine> mines = new TreeMap<>();

		final void fix() {
			// ※地雷範囲0件は理論上あり得ない
			first = mines.firstEntry().getValue().first;
			last = mines.lastEntry().getValue().last;
		}
	}

	/**
	 * 地雷範囲データ
	 */
	static class Mine extends GimmickRange<Mine> {
		/** 操作可能ノートの要素データリストインデックス */
		int movement;
		/** 操作可能ノート有無 */
		boolean has;

		/**
		 * コンストラクタ
		 * <p>操作可能ノートのない地雷範囲データとして生成する。</p>
		 * @param first 範囲データの要素データリストの最初のインデックス
		 * @param last 範囲データの要素データリストの最後のインデックス
		 */
		Mine(int first, int last) {
			this(first, last, -1, false);
		}

		/**
		 * コンストラクタ
		 * @param first 範囲データの要素データリストの最初のインデックス
		 * @param last 範囲データの要素データリストの最後のインデックス
		 * @param movement 操作可能ノートの要素データリストインデックス
		 * @param has 操作可能ノート有無
		 */
		Mine(int first, int last, int movement, boolean has) {
			super(first, last);
			this.movement = movement;
			this.has = has;
		}

		/**
		 * 操作可能ノート有無取得
		 * @return 操作可能ノートがあればtrue
		 */
		final boolean hasMovement() {
			return has;
		}
	}

	/**
	 * コンストラクタ
	 */
	GimmickRange() {
		// Do nothing
	}

	/**
	 * コンストラクタ
	 * @param first 範囲データの要素データリストの最初のインデックス
	 * @param last 範囲データの要素データリストの最後のインデックス
	 */
	GimmickRange(int first, int last) {
		super(first, last);
	}
}
