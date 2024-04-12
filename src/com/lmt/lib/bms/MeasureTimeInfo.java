package com.lmt.lib.bms;

import java.util.TreeMap;

/**
 * 小節が保有する時間に関連する情報。
 *
 * <p>{@link BmsContent}でメタ情報・タイムライン等の情報編集後に行われる時間情報の計算時に
 * 小節ごとに保有すべき情報を出力する時のレコードクラスとして用いる。</p>
 */
class MeasureTimeInfo {
	/** 小節の開始時間(秒単位) */
	double baseTime;
	/** 小節長(秒単位) */
	double length;
	/** 小節開始時のBPM */
	double beginBpm;
	/** 小節終了時のBPM */
	double endBpm;
	/** 楽曲位置(刻み位置)ごとの時間関連情報 */
	TreeMap<Double, PointTimeNode> timeNodeMap;

	/**
	 * 刻み位置ごとの時間関連情報登録
	 * @param tick 刻み位置
	 * @param actualTime 楽曲位置の実際の時間(秒単位)
	 * @param currentBpm 楽曲位置到達時の現在のBPM
	 * @param stopTime 楽曲位置での譜面停止時間(秒単位：停止なしは0.0)
	 */
	void addTimeNode(double tick, double actualTime, double currentBpm, double stopTime) {
		var node = new PointTimeNode();
		node.actualTime = actualTime;
		node.currentBpm = currentBpm;
		node.stopTime = stopTime;
		timeNodeMap = (timeNodeMap != null) ? timeNodeMap : new TreeMap<>();
		timeNodeMap.put(tick, node);
	}
}
