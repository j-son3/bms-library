package com.lmt.lib.bms.internal.deltasystem;

import com.lmt.lib.bms.bemusic.BeMusicDevice;
import com.lmt.lib.bms.bemusic.BeMusicPoint;

/**
 * 譜面傾向「SCRATCH」の分析データクラス
 */
class ScratchElement extends RatingElement {
	/**
	 * コンストラクタ
	 * @param point 楽曲位置情報
	 */
	ScratchElement(BeMusicPoint point) {
		super(point);
	}

	/** スクラッチ範囲 */
	private ScratchRange mRange;

	/**
	 * スクラッチ範囲設定
	 * @param range スクラッチ範囲
	 */
	final void setRange(ScratchRange range) {
		mRange = range;
	}

	/**
	 * スクラッチ範囲取得
	 * @return スクラッチ範囲
	 */
	final ScratchRange getRange() {
		return mRange;
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
				makePrintString(pos));
		Ds.debug(s);
	}

	/** {@inheritDoc} */
	@Override
	protected void printMeasure() {
		var m = getMeasure();
		Ds.debug("%3d+-----+---------------------------------------+-----+------+-------+----------+---+----+----+----+----+----+----+------+", m);
	}

	/** {@inheritDoc} */
	@Override
	protected void printHeader() {
		Ds.debug("---+-----+----+----+----+----+----+----+----+----+-------------------------------+---+--------------+--------------+------+");
		Ds.debug("   |     |    |    |    |    |    |    |    |    |             RANGE             |   |    STATUS    |    BEHIND    |      |");
		Ds.debug("   |     |    |    |    |    |    |    |    |    +-----+------+-------+----------+   +----+----+----+----+----+----+      |");
		Ds.debug("M  |DELTA|SCR1|SW1 |SW2 |SW3 |SW4 |SW5 |SW6 |SW7 |POS  |TYPE  |BEHIND |DETAIL    |DIR|NEAR|FAR |OPP |NEAR|FAR |OPP |SCORE |");
		Ds.debug("---+-----+----+----+----+----+----+----+----+----+-----+------+-------+----------+---+----+----+----+----+----+----+------+");
	}

	/**
	 * スクラッチ範囲データの文字列表現生成
	 * @param pos 要素リストのインデックス
	 * @return スクラッチ範囲データの文字列表現
	 */
	private String makePrintString(int pos) {
		// スクラッチの範囲外は空白
		if (mRange == null) {
			return "     |      |       |          |   |    |    |    |    |    |    |      |";
		}

		// スクラッチ範囲の文字列を生成する
		var sb = new StringBuilder();

		// スクラッチ範囲の記号
		if ((pos == mRange.first.position) || (pos == mRange.last.position)) {
			sb.append((mRange.first.position == mRange.last.position) ? "<--o |" : "<--+ |");
		} else {
			sb.append("   | |");
		}

		// スクラッチ範囲の先頭では、範囲データの詳細を出力する
		if ((pos == mRange.first.position)) {
			// スクラッチ種別
			sb.append(mRange.type.shortName).append("|");

			// 後方時間
			sb.append(String.format("%-7.3f|", mRange.behindTime));

			// 詳細データ
			sb.append(mRange.detail()).append("|");
		} else {
			// 先頭以外では詳細部分は空白とする
			sb.append("      |       |          |");
		}

		// スクラッチ評価データ
		var eval = mRange.getEvaluation(pos);
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
