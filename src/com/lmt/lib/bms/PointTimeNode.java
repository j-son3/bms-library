package com.lmt.lib.bms;

/**
 * 楽曲位置の時間関連情報
 */
class PointTimeNode {
	/** 楽曲位置の実際の時間(秒単位) */
	double actualTime;
	/** 楽曲位置到達時の現在のBPM */
	double currentBpm;
	/** 楽曲位置での譜面停止時間(秒単位：停止なしは0.0) */
	double stopTime;
}
