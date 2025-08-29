package com.lmt.lib.bms.internal.deltasystem;

import com.lmt.lib.bms.bemusic.BeMusicLane;
import com.lmt.lib.bms.bemusic.BeMusicPoint;

/**
 * 譜面傾向「COMPLEX」の分析データクラス
 */
class ComplexElement extends RatingElement {
	/** レーンごとの要素データ */
	private Data[] mData = new Data[2];

	/** 要素データクラス */
	static class Data {
		/** 何らかの操作を伴うノートがあるかどうか */
		boolean hasMovement = false;
		/** 視覚効果のあるノートの数 */
		byte numVisual = 0;
		/** ノート種別の数 */
		byte numType = 0;
		/** 長押し継続の数 */
		byte numHold = 0;
		/** 地雷の数 */
		byte numMine = 0;
		/** 連続する空域の数 */
		byte numSpace = 0;
		/** 入力デバイス色変化回数 */
		byte numChgColor = 0;
		/** ノート種別変化回数 */
		byte numChgType = 0;
		/** 楽曲位置複雑度評価点 */
		float pointScore = 0.0f;
		/** 後方複雑度評価点 */
		float backwardScore = 0.0f;
		/** 後方複雑度評価点の加算回数 */
		int backwardScoreCount = 0;
		/** 1つ後方と同じ配置かどうか */
		boolean sameBackwardPattern = false;
	}

	/**
	 * コンストラクタ
	 * @param ctx コンテキスト
	 * @param point 楽曲位置情報
	 */
	ComplexElement(DsContext ctx, BeMusicPoint point) {
		super(ctx, point);
	}

	/**
	 * 要素データ取得
	 * @param lane レーン
	 * @return 要素データ
	 */
	Data getData(BeMusicLane lane) {
		return mData[lane.getIndex()];
	}

	/**
	 * 要素データ設定
	 * @param lane レーン
	 * @param data 要素データ
	 */
	void setData(BeMusicLane lane, Data data) {
		mData[lane.getIndex()] = data;
	}

	/**
	 * 楽曲位置総合評価点取得
	 * @param lane レーン
	 * @return 楽曲位置総合評価点
	 */
	double getTotalScore(BeMusicLane lane) {
		var data = getData(lane);
		return data.pointScore + data.backwardScore;
	}

	/** {@inheritDoc} */
	@Override
	protected void printSpData(int pos) {
		var lane = BeMusicLane.PRIMARY;
		var data = getData(lane);
		var s = String.format("   |%.3f|%s|%2d|%2d|%2d|%2d|%2d|%2d|%2d|%-8.3f|%2d:%-5.3f|%-8.3f",
				getTimeDelta(),
				makeNotesString(lane),
				data.numVisual, data.numType, data.numHold, data.numMine, data.numSpace, data.numChgColor,
				data.numChgType, data.pointScore, data.backwardScoreCount, data.backwardScore, getTotalScore(lane));
		Ds.debug(s);
	}

	/** {@inheritDoc} */
	@Override
	protected void printDpData(int pos) {
		var dp = getData(BeMusicLane.PRIMARY);
		var ds = getData(BeMusicLane.SECONDARY);
		var s = String.format(
				"   |%.3f|%s| |%s|%2d|%2d|%2d|%2d|%2d|%2d|%2d|%-8.3f|%2d:%-5.3f|%-8.3f|%2d|%2d|%2d|%2d|%2d|%2d|%2d|%-8.3f|%2d:%-5.3f|%-8.3f|",
				getTimeDelta(), makeNotesString(BeMusicLane.PRIMARY), makeNotesString(BeMusicLane.SECONDARY),
				dp.numVisual, dp.numType, dp.numHold, dp.numMine, dp.numSpace, dp.numChgColor, dp.numChgType,
				dp.pointScore, dp.backwardScoreCount, dp.backwardScore, getTotalScore(BeMusicLane.PRIMARY),
				ds.numVisual, ds.numType, ds.numHold, ds.numMine, ds.numSpace, ds.numChgColor, ds.numChgType,
				ds.pointScore, ds.backwardScoreCount, ds.backwardScore, getTotalScore(BeMusicLane.SECONDARY));
		Ds.debug(s);
	}

	/** {@inheritDoc} */
	@Override
	protected void printSpMeasure(int m) {
		Ds.debug("%3d|-----|---------------------------------------|--|--|--|--|--|--|--|--------|--------+-------", m);
	}

	/** {@inheritDoc} */
	@Override
	protected void printDpMeasure(int m) {
		Ds.debug("%3d|-----|-------------------------------| |-------------------------------|--+--+--+--+--+--+--+--------+--------+--------+--+--+--+--+--+--+--+--------+--------+--------+", m);
	}

	/** {@inheritDoc} */
	@Override
	protected void printSpHeader() {
		Ds.debug("---+-----+----+----+----+----+----+----+----+----+--------------+-----+--------+--------+-------");
		Ds.debug("   |     |    |    |    |    |    |    |    |    |     NUM      | CHG |        |        |");
		Ds.debug("   |     |    |    |    |    |    |    |    |    +--+--+--+--+--+--+--+        |        |");
		Ds.debug("M  |DELTA|SCR1|SW1 |SW2 |SW3 |SW4 |SW5 |SW6 |SW7 |VI|TY|HO|MI|SP|CO|TY|PT-SCORE|BW-SCORE|TOTAL");
		Ds.debug("---+-----+----+----+----+----+----+----+----+----+--+--+--+--+--+--+--+--------+--------+-------");
	}

	/** {@inheritDoc} */
	@Override
	protected void printDpHeader() {
		Ds.debug("---+-----+-------------------------------+-+-------------------------------+-----------------------------------------------+-----------------------------------------------+");
		Ds.debug("   |     |             LEFT              | |              RIGHT            |                   PRIMARY                     |                  SECONDARY                    |");
		Ds.debug("   |     +---+---+---+---+---+---+---+---+ +---+---+---+---+---+---+---+---+--------------+-----+--------+--------+--------+--------------+-----+--------+--------+--------+");
		Ds.debug("   |     |   |   |   |   |   |   |   |   | |   |   |   |   |   |   |   |   |     NUM      | CHG |        |        |        |     NUM      | CHG |        |        |        |");
		Ds.debug("   |     |   |   |   |   |   |   |   |   | |   |   |   |   |   |   |   |   +--+--+--+--+--+--+--+        |        |        |--+--+--+--+--+--+--+        |        |        |");
		Ds.debug("M  |DELTA|SC1|SW1|SW2|SW3|SW4|SW5|SW6|SW7| |SW1|SW2|SW3|SW4|SW5|SW6|SW7|SC2|VI|TY|HO|MI|SP|CO|TY|PT-SCORE|BW-SCORE|TOTAL   |VI|TY|HO|MI|SP|CO|TY|PT-SCORE|BW-SCORE|TOTAL   |");
		Ds.debug("---+-----+---+---+---+---+---+---+---+---+-+---+---+---+---+---+---+---+---+--+--+--+--+--+--+--+--------+--------+--------+--+--+--+--+--+--+--+--------+--------+--------+");
	}
}
