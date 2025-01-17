package com.lmt.lib.bms.internal.deltasystem;

import com.lmt.lib.bms.bemusic.BeMusicLane;
import com.lmt.lib.bms.bemusic.BeMusicPoint;

/**
 * 譜面傾向「RHYTHM」の分析データクラス
 */
class RhythmElement extends RatingElement {
	/** リズム範囲データ */
	private PulseRange[] mPulseRanges = new PulseRange[3];

	/**
	 * コンストラクタ
	 * @param ctx コンテキスト
	 * @param point 楽曲位置情報
	 */
	RhythmElement(DsContext ctx, BeMusicPoint point) {
		super(ctx, point);
	}

	/**
	 * リズム範囲取得
	 * @param side サイド
	 * @return リズム範囲
	 */
	final PulseRange getPulseRange(Rhythm.Side side) {
		return mPulseRanges[side.index];
	}

	/**
	 * リズム範囲設定
	 * @param side サイド
	 * @param pulseRange リズム範囲
	 */
	final void setPulseRange(Rhythm.Side side, PulseRange pulseRange) {
		mPulseRanges[side.index] = pulseRange;
	}

	/** {@inheritDoc} */
	@Override
	protected void printSpData(int pos) {
		var s = String.format("   |%.3f|%s|%s|%s|%s|",
				getTimeDelta(), makeNotesString(BeMusicLane.PRIMARY),
				makePulseRange(Rhythm.Side.ALL), makePulseRange(Rhythm.Side.LEFT), makePulseRange(Rhythm.Side.RIGHT));
		Ds.debug(s);
	}

	/** {@inheritDoc} */
	@Override
	protected void printDpData(int pos) {
		var s = String.format("   |%.3f|%s| |%s|%s|%s|%s|",
				getTimeDelta(), makeNotesString(BeMusicLane.PRIMARY), makeNotesString(BeMusicLane.SECONDARY),
				makePulseRange(Rhythm.Side.ALL), makePulseRange(Rhythm.Side.LEFT), makePulseRange(Rhythm.Side.RIGHT));
		Ds.debug(s);
	}

	/** {@inheritDoc} */
	@Override
	protected void printSpMeasure(int m) {
		Ds.debug("%3d+-----+---------------------------------------+-------+-------------+----------+------+-------+------+-------+-------------+----------+------+-------+------+-------+-------------+----------+------+-------+------+", m);
	}

	/** {@inheritDoc} */
	@Override
	protected void printDpMeasure(int m) {
		Ds.debug("%3d+-----+-------------------------------| |-------------------------------|-------+-------------+----------+------+-------+------+-------+-------------+----------+------+-------+------+-------+-------------+----------+------+-------+------+", m);
	}

	/** {@inheritDoc} */
	@Override
	protected void printSpHeader() {
		Ds.debug("---+-----+----+----+----+----+----+----+----+----+------------------------------------------------------+------------------------------------------------------+------------------------------------------------------+");
		Ds.debug("   |     |    |    |    |    |    |    |    |    |                       ALL                            |                       LEFT                           |                       RIGHT                          |");
		Ds.debug("   |     |    |    |    |    |    |    |    |    +-------+-------------+----------+------+-------+------+-------+-------------+----------+------+-------+------+-------+-------------+----------+------+-------+------+");
		Ds.debug("M  |DELTA|SCR1|SW1 |SW2 |SW3 |SW4 |SW5 |SW6 |SW7 |RNG/RPT|RANGE-TIME   |PULSE     |INTRVL|DENSITY|SCORE |RNG/RPT|RANGE-TIME   |PULSE     |INTRVL|DENSITY|SCORE |RNG/RPT|RANGE-TIME   |PULSE     |INTRVL|DENSITY|SCORE |");
		Ds.debug("---+-----+----+----+----+----+----+----+----+----+-------+-------------+----------+------+-------+------+-------+-------------+----------+------+-------+------+-------+-------------+----------+------+-------+------+");
	}

	/** {@inheritDoc} */
	@Override
	protected void printDpHeader() {
		Ds.debug("---+-----+-------------------------------+-+-------------------------------+------------------------------------------------------+------------------------------------------------------+------------------------------------------------------+");
		Ds.debug("   |     |             LEFT              | |              RIGHT            |                       ALL                            |                       LEFT                           |                       RIGHT                          |");
		Ds.debug("   |     +---+---+---+---+---+---+---+---+ +---+---+---+---+---+---+---+---+-------+-------------+----------+------+-------+------+-------+-------------+----------+------+-------+------+-------+-------------+----------+------+-------+------+");
		Ds.debug("M  |DELTA|SC1|SW1|SW2|SW3|SW4|SW5|SW6|SW7| |SW1|SW2|SW3|SW4|SW5|SW6|SW7|SC2|RNG/RPT|RANGE-TIME   |PULSE     |INTRVL|DENSITY|SCORE |RNG/RPT|RANGE-TIME   |PULSE     |INTRVL|DENSITY|SCORE |RNG/RPT|RANGE-TIME   |PULSE     |INTRVL|DENSITY|SCORE |");
		Ds.debug("---+-----+---+---+---+---+---+---+---+---+-+---+---+---+---+---+---+---+---+-------+-------------+----------+------+-------+------+-------+-------------+----------+------+-------+------+-------+-------------+----------+------+-------+------+");
	}

	/**
	 * リズム範囲データの文字列表現生成
	 * @param side サイド
	 * @return リズム範囲データの文字列表現
	 */
	private String makePulseRange(Rhythm.Side side) {
		// リズム範囲未設定の場合は空白を出力する
		var r = getPulseRange(side);
		if (r == null) {
			return "       |             |          |      |       |      ";
		}

		var sb = new StringBuilder(128);

		// リズム範囲の図
		if (r.firstElement == this) {
			sb.append((r.firstElement == r.lastElement) ? "<--" : "<-+");
		} else {
			sb.append((r.lastElement == this) ? "<-+" : "  |");
		}

		// リズムリピートの図
		if (((r == r.repeat.firstRange) && (r.firstElement == this)) ||
				((r == r.repeat.lastRange) && (r.lastElement == this))) {
			sb.append("<-+ |");
		} else {
			sb.append("  | |");
		}

		// リズム範囲のデータ
		if (r.firstElement == this) {
			sb.append(String.format("%-6.3f:%-5.2f%%|%4d:%-5.3f|%-6.3f|%-7.2f|%-6.4f",
					r.rangeTime, (r.rangeRate * 100.0), r.pulseCount, r.pulseTimeSec, r.intervalSec, r.density, r.score));
		} else {
			sb.append("             |          |      |       |      ");
		}

		return sb.toString();
	}
}
