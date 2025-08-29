package com.lmt.lib.bms.internal.deltasystem;

import com.lmt.lib.bms.bemusic.BeMusicDevice;
import com.lmt.lib.bms.bemusic.BeMusicLane;
import com.lmt.lib.bms.bemusic.BeMusicPoint;

/**
 * 譜面傾向「HOLDING」の分析データクラス
 */
class HoldingElement extends RatingElement {
	/** 長押し範囲データ */
	private HoldingRange mRange = null;

	/**
	 * コンストラクタ
	 * @param ctx コンテキスト
	 * @param point 楽曲位置情報
	 */
	HoldingElement(DsContext ctx, BeMusicPoint point) {
		super(ctx, point);
	}

	/**
	 * 長押し範囲データ取得
	 * @return 長押し範囲データ
	 */
	HoldingRange getRange() {
		return mRange;
	}

	/**
	 * 長押し範囲データ設定
	 * @param range 長押し範囲データ
	 */
	void setRange(HoldingRange range) {
		mRange = range;
	}

	/** {@inheritDoc} */
	@Override
	protected void printSpData(int pos) {
		var s = String.format("   |%.3f|%s|%s",
				getTimeDelta(),
				makeNotesString(BeMusicLane.PRIMARY),
				makePrintString(pos));
		Ds.debug(s);
	}

	/** {@inheritDoc} */
	@Override
	protected void printDpData(int pos) {
		var s = String.format("   |%.3f|%s| |%s|%s",
				getTimeDelta(),
				makeNotesString(BeMusicLane.PRIMARY),
				makeNotesString(BeMusicLane.SECONDARY),
				makePrintString(pos));
		Ds.debug(s);
	}

	/** {@inheritDoc} */
	@Override
	protected void printSpMeasure(int m) {
		Ds.debug("%3d+-----+---------------------------------------+-----+------+-----++-----+-----+-----+-----+-----+-----+-----+-----+", m);
	}

	/** {@inheritDoc} */
	@Override
	protected void printDpMeasure(int m) {
		Ds.debug("%3d+-----|-------------------------------| |-------------------------------|-----+------+-----++-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----|", m);
	}

	/** {@inheritDoc} */
	@Override
	protected void printSpHeader() {
		Ds.debug("---+-----+----+----+----+----+----+----+----+----+------------------++-----------------------------------------------+");
		Ds.debug("   |     |    |    |    |    |    |    |    |    |       RANGE      ||                  NOTE-SCORE                   |");
		Ds.debug("   |     |    |    |    |    |    |    |    |    +-----+------+-----++-----+-----+-----+-----+-----+-----+-----+-----+");
		Ds.debug("M  |DELTA|SCR1|SW1 |SW2 |SW3 |SW4 |SW5 |SW6 |SW7 |POS  |TIME  |NOTES||SCR1 |SW1  |SW2  |SW3  |SW4  |SW5  |SW6  |SW7  |");
		Ds.debug("---+-----+----+----+----+----+----+----+----+----+-----+------+-----++-----+-----+-----+-----+-----+-----+-----+-----+");
	}

	/** {@inheritDoc} */
	@Override
	protected void printDpHeader() {
		Ds.debug("---------+-------------------------------+-+-------------------------------+------------------++-----------------------------------------------------------------------------------------------+");
		Ds.debug("         |             LEFT              | |              RIGHT            |                  ||                                          NOTE-SCORE                                           |");
		Ds.debug("---+-----+---+---+---+---+---+---+---+---+-+---+---+---+---+---+---+---+---+       RANGE      |+-----------------------------------------------+-----------------------------------------------+");
		Ds.debug("   |     |   |   |   |   |   |   |   |   | |   |   |   |   |   |   |   |   |                  ||                    PRIMARY                    |                   SECONDARY                   |");
		Ds.debug("   |     |   |   |   |   |   |   |   |   | |   |   |   |   |   |   |   |   |-----+------+-----++-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+");
		Ds.debug("M  |DELTA|SC1|SW1|SW2|SW3|SW4|SW5|SW6|SW7| |SW1|SW2|SW3|SW4|SW5|SW6|SW7|SC2|POS  |TIME  |NOTES||SCR1 |SW1-1|SW1-2|SW1-3|SW1-4|SW1-5|SW1-6|SW1-7|SW2-1|SW2-2|SW2-3|SW2-4|SW2-5|SW2-6|SW2-7|SCR2 |");
		Ds.debug("---+-----+---+---+---+---+---+---+---+---+-+---+---+---+---+---+---+---+---+-----+------+-----++-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+");
	}

	/**
	 * 長押し範囲データの文字列表現生成
	 * @param pos 要素リストのインデックス
	 * @return スクラッチ範囲データの文字列表現
	 */
	private String makePrintString(int pos) {
		// 長押しの範囲外は空白
		if (mRange == null) {
			final var emptySp = "     |      |     ||     |     |     |     |     |     |     |     |";
			final var emptyDp = "     |      |     ||     |     |     |     |     |     |     |     |     |     |     |     |     |     |     |     |";
			return getContext().dpMode ? emptyDp : emptySp;
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
		var devs = getContext().dpMode ? BeMusicDevice.orderedByDpList() : BeMusicDevice.orderedBySpLeftList();
		var devsCount = devs.size();
		for (var iDev = 0; iDev < devsCount; iDev++) {
			var score = mRange.getNoteScore(pos, devs.get(iDev));
			if (score == null) {
				sb.append("     |");
			} else {
				sb.append(String.format("%-5.3f|", score));
			}
		}

		return sb.toString();
	}
}
