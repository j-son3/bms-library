package com.lmt.lib.bms.internal.deltasystem;

import com.lmt.lib.bms.bemusic.BeMusicDevice;
import com.lmt.lib.bms.bemusic.BeMusicPoint;

/**
 * 譜面傾向「GIMMICK」の分析データクラス
 */
class GimmickElement extends RatingElement {
	/** 速度変更範囲 */
	private GimmickRange.Speed mRange = null;

	/**
	 * コンストラクタ
	 * @param point 楽曲位置情報
	 */
	GimmickElement(BeMusicPoint point) {
		super(point);
	}

	/**
	 * 速度変更範囲取得
	 * @return 速度変更範囲
	 */
	final GimmickRange.Speed getRange() {
		return mRange;
	}

	/**
	 * 速度変更範囲設定
	 * @param range 速度変更範囲
	 */
	final void setRange(GimmickRange.Speed range) {
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
				makePrintString(pos));
		Ds.debug(s);
	}

	/** {@inheritDoc} */
	@Override
	protected void printMeasure() {
		var m = getMeasure();
		Ds.debug("%3d+-----+---------------------------------------+------+-----+----------+----+--------+------+------+-------+------+------+------+", m);
	}

	/** {@inheritDoc} */
	@Override
	protected void printHeader() {
		Ds.debug("---+-----+----+----+----+----+----+----+----+----+--------------------------------------------+---------------------+-------------+");
		Ds.debug("   |     |    |    |    |    |    |    |    |    |                CHANGE-SPEED                |        STOP         |    MINE     |");
		Ds.debug("   |     |    |    |    |    |    |    |    |    +------+-----+----------+----+--------+------+-------+------+------+------+------+");
		Ds.debug("M  |DELTA|SCR1|SW1 |SW2 |SW3 |SW4 |SW5 |SW6 |SW7 |RANGE |NOTES|SPEED     |GEAR|IDEALITY|SCORE |RANGE  |TIME  |SCORE |RANGE |SCORE |");
		Ds.debug("---+-----+----+----+----+----+----+----+----+----+------+-----+----------+----+--------+------+-------+------+------+------+------+");
	}

	/**
	 * 要素データの文字列表現生成
	 * @param pos 要素リストのインデックス
	 * @return スクラッチ範囲データの文字列表現
	 */
	private String makePrintString(int pos) {
		// この楽曲位置に範囲情報が設定されていない場合は空行を出力する
		var speed = getRange();
		if (speed == null) {
			return "      |     |          |    |        |      |      |       |      |      |      |";
		}

		// 速度変更範囲
		// RANGE
		var sb = new StringBuilder();
		var spdFirst = (pos == speed.first);
		var spdEnds = spdFirst || (pos == speed.last);
		var spdMvEnds = (pos == speed.firstMovement) || (pos == speed.lastMovement);
		var spdValue = speed.changes.get(pos);
		var spdHasChg = (spdValue != null);
		sb.append(spdEnds ? '<' : ' ');
		sb.append(spdMvEnds ? "===" : (spdEnds || spdHasChg) ? "---" : "   ");
		sb.append(spdEnds ? "+ |" : "| |");

		// NOTES
		sb.append(spdFirst ? String.format("%5d|", speed.numNotes) : "     |");

		// SPEED
		sb.append(spdHasChg ? String.format("%-10.1f|", spdValue) : "          |");

		// GEAR
		sb.append(spdFirst ? (speed.gearChange ? "YES |" : "NO  |") : "    |");

		// IDEALITY
		sb.append(spdFirst ? String.format("%-8.3f|", speed.ideality) : "        |");

		// SCORE
		sb.append(spdFirst ? String.format("%-6.4f|", speed.score) : "      |");

		// 譜面停止範囲
		var stop = speed.inboundStop(pos);
		if (stop == null) {
			// 譜面停止なし
			sb.append("      |       |      |");
		} else {
			// RANGE
			var stpFirst = (pos == stop.first);
			var stpEnds = stpFirst || (pos == stop.last);
			var stpInfEnds = (pos == stop.firstInfluence) || (pos == stop.lastInfluence);
			var stpValue = stop.stops.get(pos);
			var stpExist = (stpValue != null);
			sb.append(stop.isOnePoint() ? 'O' : stpEnds ? '<' : ' ');
			sb.append(stpInfEnds ? "===" : stpExist ? "---" : "   ");
			sb.append(stpEnds ? "+ |" : "| |");

			// TIME
			sb.append(stpExist ? String.format("%-7.4f|", stpValue) : "       |");

			// SCORE
			sb.append(stpFirst ? String.format("%-6.4f|", stop.score) : "      |");
		}

		// 地雷範囲
		var mineGrp = speed.inboundMine(pos);
		if (mineGrp == null) {
			// 地雷なし
			sb.append("      |      |");
		} else {
			// RANGE
			var mineFirst = (pos == mineGrp.first);
			var mineEnds = mineFirst || (pos == mineGrp.last);
			var mine = mineGrp.mines.get(pos);
			var mineHasMv = (mine != null) && mine.hasMovement();
			var mineExist = getPoint().hasLandmine();
			sb.append(mineGrp.isOnePoint() ? 'O' : mineEnds ? '<' : ' ');
			sb.append(mineHasMv ? "==" : mineEnds ? "--" : "  ");
			sb.append(mineExist ? '*' : mineHasMv ? '=' : mineEnds ? '-' : ' ');
			sb.append(mineEnds ? "+ |" : "| |");

			// SCORE
			sb.append((mine != null) ? String.format("%-6.4f|", mine.score) : "      |");
		}

		return sb.toString();
	}
}
