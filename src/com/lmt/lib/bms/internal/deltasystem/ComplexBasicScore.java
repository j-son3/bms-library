package com.lmt.lib.bms.internal.deltasystem;

import com.lmt.lib.bms.bemusic.BeMusicLane;
import com.lmt.lib.bms.bemusic.BeMusicNoteType;

/**
 * 譜面傾向「COMPLEX」の基本評価点の基底クラス
 */
abstract class ComplexBasicScore {
	/** 基本評価点テーブルに要求する配列要素数 */
	private static final int REQUIRE_TABLE_SIZE = 255;

	/** インスタンス(SP) */
	private static ComplexBasicScore sInstanceSp = null;
	/** インスタンス(DP) */
	private static ComplexBasicScore sInstanceDp = null;

	/** 基本評価点テーブル */
	private double[] mScores;

	/**
	 * インスタンス取得
	 * @param ctx コンテキスト
	 * @return インスタンス
	 */
	static ComplexBasicScore getInstance(DsContext ctx) {
		if (ctx.dpMode) {
			if (sInstanceDp == null) {
				sInstanceDp = new ComplexBasicScoreDp();
			}
			return sInstanceDp;
		} else {
			if (sInstanceSp == null) {
				sInstanceSp = new ComplexBasicScoreSp();
			}
			return sInstanceSp;
		}
	}

	/**
	 * コンストラクタ
	 * @param scores 基本評価点テーブル
	 * @exception IllegalArgumentException 基本評価点テーブルサイズ不正
	 */
	protected ComplexBasicScore(double[] scores) {
		// 定義ミスを避けるために基本評価点テーブルのサイズをチェックする
		// ここで例外がスローされる場合、基本評価点テーブルの定義ミスが確定している
		var actualLength = scores.length;
		if (actualLength != REQUIRE_TABLE_SIZE) {
			var msg = String.format("SCORE_TABLE size must be %d, but %d.", REQUIRE_TABLE_SIZE, actualLength);
			throw new IllegalArgumentException(msg);
		}

		// スコア定義をコピーする
		// ノートなしは0点なので、テーブルの先頭には0をセットしておく
		mScores = new double[REQUIRE_TABLE_SIZE + 1];
		mScores[0] = 0.0;
		for (var i = 0; i < REQUIRE_TABLE_SIZE; i++) {
			mScores[i + 1] = scores[i];
		}
	}

	/**
	 * 基本評価点取得
	 * @param lane レーン
	 * @param elem COMPLEXの分析データ
	 * @return 基本評価点
	 */
	final double get(BeMusicLane lane, ComplexElement elem) {
		return mScores[index(lane, elem)];
	}

	/**
	 * 基本評価点テーブルのインデックス値計算
	 * @param lane レーン
	 * @param elem COMPLEXの分析データ
	 * @return 基本評価点テーブルのインデックス値
	 */
	private int index(BeMusicLane lane, ComplexElement elem) {
		var result = 0;
		var devices = DsContext.orderedDevices(lane);
		var count = devices.size();
		for (var i = 0; i < count; i++) {
			result += testNote(elem.getNoteType(devices.get(i))) ? (1 << i) : 0;
		}
		return result;
	}

	/**
	 * ノート種別を視覚効果ありとするかどうかの判定
	 * @param type ノート種別
	 * @return 視覚効果ありと判定する場合true
	 */
	protected abstract boolean testNote(BeMusicNoteType type);
}
