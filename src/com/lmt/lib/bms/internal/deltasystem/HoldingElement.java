package com.lmt.lib.bms.internal.deltasystem;

import java.util.List;

import com.lmt.lib.bms.bemusic.BeMusicDevice;
import com.lmt.lib.bms.bemusic.BeMusicPoint;

/**
 * 譜面傾向「HOLDING」の分析データクラス
 */
class HoldingElement extends RatingElement {
	/** 長押し範囲データ */
	private HoldingRange mRange = null;

	/**
	 * コンストラクタ
	 * @param point 楽曲位置情報
	 */
	HoldingElement(BeMusicPoint point) {
		super(point);
	}

	/**
	 * 長押し範囲データ取得
	 * @return 長押し範囲データ
	 */
	final HoldingRange getRange() {
		return mRange;
	}

	/**
	 * 長押し範囲データ設定
	 * @param range 長押し範囲データ
	 */
	final void setRange(HoldingRange range) {
		mRange = range;
	}

	/** {@inheritDoc} */
	@Override
	protected void printData(int pos) {
		var s = String.format("   |%.3f|%s %s %s %s %s %s %s %s|%s",
				getTimeDelta(),
				getNoteTypeString(BeMusicDevice.SCRATCH1), getNoteTypeString(BeMusicDevice.SWITCH11),
				getNoteTypeString(BeMusicDevice.SWITCH12), getNoteTypeString(BeMusicDevice.SWITCH13),
				getNoteTypeString(BeMusicDevice.SWITCH14), getNoteTypeString(BeMusicDevice.SWITCH15),
				getNoteTypeString(BeMusicDevice.SWITCH16), getNoteTypeString(BeMusicDevice.SWITCH17),
				makePrintString(pos, DEVS));
		Ds.debug(s);
	}

	/** {@inheritDoc} */
	@Override
	protected void printMeasure() {
		var m = getMeasure();
		Ds.debug("%3d+-----+---------------------------------------+-----+------+-----++-----+-----+-----+-----+-----+-----+-----+-----+", m);
	}

	/** {@inheritDoc} */
	@Override
	protected void printHeader() {
		Ds.debug("---+-----+----+----+----+----+----+----+----+----+------------------++-----------------------------------------------+");
		Ds.debug("   |     |    |    |    |    |    |    |    |    |       RANGE      ||                  NOTE-SCORE                   |");
		Ds.debug("   |     |    |    |    |    |    |    |    |    +-----+------+-----++-----+-----+-----+-----+-----+-----+-----+-----+");
		Ds.debug("M  |DELTA|SCR1|SW1 |SW2 |SW3 |SW4 |SW5 |SW6 |SW7 |POS  |TIME  |NOTES||SCR1 |SW1  |SW2  |SW3  |SW4  |SW5  |SW6  |SW7  |");
		Ds.debug("---+-----+----+----+----+----+----+----+----+----+-----+------+-----++-----+-----+-----+-----+-----+-----+-----+-----+");
	}

	/**
	 * 長押し範囲データの文字列表現生成
	 * @param pos 要素リストのインデックス
	 * @param devs 入力デバイスリスト
	 * @return スクラッチ範囲データの文字列表現
	 */
	private String makePrintString(int pos, List<BeMusicDevice> devs) {
		// 長押しの範囲外は空白
		if (mRange == null) {
			return "     |      |     ||     |     |     |     |     |     |     |     |";
		}

		// 長押し範囲の文字列を生成する
		var sb = new StringBuilder();

		// 長押し範囲の記号
		sb.append(((pos == mRange.first) || (pos == mRange.last)) ? "<--+ |" : "   | |");

		// 長押し範囲の先頭では、範囲データの詳細を出力する
		if ((pos == mRange.first)) {
			// 長押し範囲時間
			sb.append(String.format("%-6.2f|", mRange.time));

			// 長押し範囲のノート数
			sb.append(String.format("%5d||", mRange.notes));
		} else {
			// 先頭以外では詳細部分は空白とする
			sb.append("      |     ||");
		}

		// ノート評価点
		for (var dev : devs) {
			var score = mRange.getNoteScore(pos, dev);
			if (score == null) {
				sb.append("     |");
			} else {
				sb.append(String.format("%-5.3f|", score));
			}
		}

		return sb.toString();
	}
}
