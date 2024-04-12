package com.lmt.lib.bms.internal.deltasystem;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.lmt.lib.bms.bemusic.BeMusicDevice;

/**
 * 長押し範囲データ
 */
class HoldingRange {
	/** 範囲開始位置 */
	int first;
	/** 範囲終了位置 */
	int last;
	/** 次の長押し範囲データ */
	HoldingRange next = null;
	/** ノート評価点マップ */
	Map<Integer, Map<BeMusicDevice, Double>> evals = new HashMap<>();
	/** この範囲のノート数 */
	int notes = 0;
	/** 長押し範囲の時間 */
	double time = 0.0;

	/**
	 * コンストラクタ
	 * @param elems 要素データリスト
	 * @param first 範囲開始位置
	 * @param last 範囲終了位置
	 */
	HoldingRange(List<HoldingElement> elems, int first, int last) {
		this.first = first;
		this.last = last;
		this.time = RatingElement.timeDelta(elems, first, last);
	}

	/**
	 * ノート評価点取得
	 * @param pos 楽曲位置インデックス
	 * @param dev 入力デバイス
	 * @return pos, devに該当するノート評価点。なければnull
	 */
	final Double getNoteScore(int pos, BeMusicDevice dev) {
		var posMap = evals.get(pos);
		return (posMap == null) ? null : posMap.get(dev);
	}

	/**
	 * ノート評価点追加
	 * @param pos 楽曲位置インデックス
	 * @param dev 入力デバイス
	 * @param countNotes ノート数としてカウントするかどうか
	 * @param score ノート評価点
	 */
	final void putNoteScore(int pos, BeMusicDevice dev, boolean countNotes, double score) {
		var posMap = evals.get(pos);
		if (posMap == null) {
			posMap = new HashMap<>();
			evals.put(pos, posMap);
		}
		posMap.put(dev, score);
		notes += countNotes ? 1 : 0;
	}
}
