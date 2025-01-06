package com.lmt.lib.bms.internal.deltasystem;

import com.lmt.lib.bms.bemusic.BeMusicLane;
import com.lmt.lib.bms.bemusic.BeMusicPoint;

/**
 * 譜面傾向「SCRATCH」の分析データクラス
 */
class ScratchElement extends RatingElement {
	/** レーンごとの要素データ */
	private ScratchRange[] mData = { null, null };

	/**
	 * コンストラクタ
	 * @param ctx コンテキスト
	 * @param point 楽曲位置情報
	 */
	ScratchElement(DsContext ctx, BeMusicPoint point) {
		super(ctx, point);
	}

	/**
	 * 要素データ取得
	 * @param lane レーン
	 * @return 要素データ
	 */
	ScratchRange getData(BeMusicLane lane) {
		return mData[lane.getIndex()];
	}

	/**
	 * 要素データ設定
	 * @param lane レーン
	 * @param range 要素データ
	 */
	void setData(BeMusicLane lane, ScratchRange range) {
		mData[lane.getIndex()] = range;
	}

	/** {@inheritDoc} */
	@Override
	protected void printSpData(int pos) {
		var s = String.format("   |%.3f|%s|%s",
				getTimeDelta(),
				makeNotesString(BeMusicLane.PRIMARY),
				makePrintString(pos, BeMusicLane.PRIMARY));
		Ds.debug(s);
	}

	/** {@inheritDoc} */
	@Override
	protected void printDpData(int pos) {
		var s = String.format("   |%.3f|%s| |%s|%s%s",
				getTimeDelta(),
				makeNotesString(BeMusicLane.PRIMARY), makeNotesString(BeMusicLane.SECONDARY),
				makePrintString(pos, BeMusicLane.PRIMARY), makePrintString(pos, BeMusicLane.SECONDARY));
		Ds.debug(s);
	}

	/** {@inheritDoc} */
	@Override
	protected void printSpMeasure(int m) {
		Ds.debug("%3d+-----+---------------------------------------+-----+------+-------+----------+---+----+----+----+----+----+----+------+", m);
	}

	/** {@inheritDoc} */
	@Override
	protected void printDpMeasure(int m) {
		Ds.debug("%3d+-----+-------------------------------|-|-------------------------------|-----+------+-------+----------+---+----+----+----+----+----+----+------+-----+------+-------+----------+---+----+----+----+----+----+----+------+", m);
	}

	/** {@inheritDoc} */
	@Override
	protected void printSpHeader() {
		Ds.debug("---+-----+----+----+----+----+----+----+----+----+-------------------------------+---+--------------+--------------+------+");
		Ds.debug("   |     |    |    |    |    |    |    |    |    |             RANGE             |   |    STATUS    |    BEHIND    |      |");
		Ds.debug("   |     |    |    |    |    |    |    |    |    +-----+------+-------+----------+   +----+----+----+----+----+----+      |");
		Ds.debug("M  |DELTA|SCR1|SW1 |SW2 |SW3 |SW4 |SW5 |SW6 |SW7 |POS  |TYPE  |BEHIND |DETAIL    |DIR|NEAR|FAR |OPP |NEAR|FAR |OPP |SCORE |");
		Ds.debug("---+-----+----+----+----+----+----+----+----+----+-----+------+-------+----------+---+----+----+----+----+----+----+------+");
	}

	/** {@inheritDoc} */
	@Override
	protected void printDpHeader() {
		Ds.debug("---------+-------------------------------+-+-------------------------------+------------------------------------------------------------------------+------------------------------------------------------------------------+");
		Ds.debug("         |             LEFT              | |              RIGHT            |                                PRIMARY                                 |                               SECONDARY                                |");
		Ds.debug("---+-----+---+---+---+---+---+---+---+---+-+---+---+---+---+---+---+---+---+-------------------------------+---+--------------+--------------+------+-------------------------------+---+--------------+--------------+------+");
		Ds.debug("   |     |   |   |   |   |   |   |   |   | |   |   |   |   |   |   |   |   |             RANGE             |   |    STATUS    |    BEHIND    |      |             RANGE             |   |    STATUS    |    BEHIND    |      |");
		Ds.debug("   |     |   |   |   |   |   |   |   |   | |   |   |   |   |   |   |   |   +-----+------+-------+----------+   +----+----+----+----+----+----+      |-----+------+-------+----------+   +----+----+----+----+----+----+      |");
		Ds.debug("M  |DELTA|SC1|SW1|SW2|SW3|SW4|SW5|SW6|SW7| |SW1|SW2|SW3|SW4|SW5|SW6|SW7|SC2|POS  |TYPE  |BEHIND |DETAIL    |DIR|NEAR|FAR |OPP |NEAR|FAR |OPP |SCORE |POS  |TYPE  |BEHIND |DETAIL    |DIR|NEAR|FAR |OPP |NEAR|FAR |OPP |SCORE |");
		Ds.debug("---+-----+---+---+---+---+---+---+---+---+-+---+---+---+---+---+---+---+---+-----+------+-------+----------+---+----+----+----+----+----+----+------+-----+------+-------+----------+---+----+----+----+----+----+----+------+");
	}

	/**
	 * スクラッチ範囲データの文字列表現生成
	 * @param pos 要素リストのインデックス
	 * @param lane レーン
	 * @return スクラッチ範囲データの文字列表現
	 */
	private String makePrintString(int pos, BeMusicLane lane) {
		// スクラッチの範囲外は空白
		var range = mData[lane.getIndex()];
		if (range == null) {
			return "     |      |       |          |   |    |    |    |    |    |    |      |";
		}

		// スクラッチ範囲の文字列を生成する
		var sb = new StringBuilder();

		// スクラッチ範囲の記号
		if ((pos == range.first.position) || (pos == range.last.position)) {
			sb.append((range.first.position == range.last.position) ? "<--o |" : "<--+ |");
		} else {
			sb.append("   | |");
		}

		// スクラッチ範囲の先頭では、範囲データの詳細を出力する
		if ((pos == range.first.position)) {
			// スクラッチ種別
			sb.append(range.type.shortName).append("|");

			// 後方時間
			sb.append(String.format("%-7.3f|", range.behindTime));

			// 詳細データ
			sb.append(range.detail()).append("|");
		} else {
			// 先頭以外では詳細部分は空白とする
			sb.append("      |       |          |");
		}

		// スクラッチ評価データ
		var eval = range.getEvaluation(pos);
		if (eval != null) {
			// スクラッチの操作方向
			sb.append(' ').append(eval.direction.shortName).append(" |");

			// スクラッチと同じ楽曲位置の状態
			sb.append(eval.makeStatusString(Scratch.Area.NEAR)).append("|");
			sb.append(eval.makeStatusString(Scratch.Area.FAR)).append("|");
			sb.append(eval.makeStatusString(Scratch.Area.OPPOSITE)).append("|");

			// スクラッチより手前の楽曲位置の状態
			sb.append(eval.makeBehindStatusString(Scratch.Area.NEAR)).append("|");
			sb.append(eval.makeBehindStatusString(Scratch.Area.FAR)).append("|");
			sb.append(eval.makeBehindStatusString(Scratch.Area.OPPOSITE)).append("|");

			// スクラッチ評価点
			sb.append(String.format("%-6.3f|", eval.score));
		} else {
			// 評価データのない楽曲位置では空白とする
			sb.append("   |    |    |    |    |    |    |      |");
		}

		return sb.toString();
	}
}
