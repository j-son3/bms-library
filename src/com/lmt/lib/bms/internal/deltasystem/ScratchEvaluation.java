package com.lmt.lib.bms.internal.deltasystem;

/**
 * スクラッチ評価データ
 */
class ScratchEvaluation {
	/** 評価対象スクラッチの要素リストインデックス */
	int position;
	/** スクラッチの操作方向 */
	Scratch.Direction direction = null;
	/** スイッチの状態 */
	int status = 0x00000000;
	/** スクラッチ評価点 */
	double score = 0.0;

	/**
	 * コンストラクタ
	 */
	ScratchEvaluation() {
		this.position = -1;
	}

	/**
	 * コンストラクタ
	 * @param pos 評価対象スクラッチの要素リストインデックス
	 */
	ScratchEvaluation(int pos) {
		this.position = pos;
	}

	/**
	 * 指定領域・ノート種別の状態取得
	 * @param area 領域
	 * @param note ノート種別
	 * @return true:操作あり / false:操作なし
	 */
	final boolean getStatus(Scratch.Area area, Scratch.Note note) {
		return getStatus(0, area.shift, note.shift);
	}

	/**
	 * 指定領域・ノート種別の状態設定
	 * @param area 領域
	 * @param note ノート種別
	 * @param status 操作有無
	 */
	final void setStatus(Scratch.Area area, Scratch.Note note, boolean status) {
		setStatus(0, area.shift, note.shift, status);
	}

	/**
	 * 後方楽曲位置での指定領域・ノート種別の状態取得
	 * @param area 領域
	 * @param note ノート種別
	 * @return true:操作あり / false:操作なし
	 */
	final boolean getBehindStatus(Scratch.Area area, Scratch.Note note) {
		return getStatus(Scratch.Area.TOTAL_BITS, area.shift, note.shift);
	}

	/**
	 * 後方楽曲位置での指定領域・ノート種別の状態設定
	 * @param area 領域
	 * @param note ノート種別
	 * @param status 操作有無
	 */
	final void setBehindStatus(Scratch.Area area, Scratch.Note note, boolean status) {
		setStatus(Scratch.Area.TOTAL_BITS, area.shift, note.shift, status);
	}

	/**
	 * 指定領域の状態の文字列表現取得
	 * @param area 領域
	 * @return 状態の文字列表現
	 */
	final String makeStatusString(Scratch.Area area) {
		return makeStatusString(0, area.shift);
	}

	/**
	 * 後方楽曲位置での指定領域の状態の文字列表現取得
	 * @param area 領域
	 * @return 状態の文字列表現
	 */
	final String makeBehindStatusString(Scratch.Area area) {
		return makeStatusString(Scratch.Area.TOTAL_BITS, area.shift);
	}

	/**
	 * 指定領域・ノート種別の状態取得
	 * @param baseShift ベースのシフト量
	 * @param areaShift 領域のシフト量
	 * @param noteShift ノート種別のシフト量
	 * @return true:操作あり / false:操作なし
	 */
	private boolean getStatus(int baseShift, int areaShift, int noteShift) {
		return ((this.status >> (baseShift + areaShift + noteShift)) & 0x01) != 0;
	}

	/**
	 * 指定領域・ノート種別の状態設定
	 * @param baseShift ベースのシフト量
	 * @param areaShift 領域のシフト量
	 * @param noteShift ノート種別のシフト量
	 * @param status 操作有無
	 */
	private void setStatus(int baseShift, int areaShift, int noteShift, boolean status) {
		var bit = 1 << (baseShift + areaShift + noteShift);
		this.status = (this.status & ~bit) | (status ? bit : 0);
	}

	/**
	 * 指定領域の状態の文字列表現取得
	 * @param baseShift ベースのシフト量
	 * @param areaShift 領域のシフト量
	 * @return 状態の文字列表現
	 */
	private String makeStatusString(int baseShift, int areaShift) {
		return String.format("%c%c%c ",
				(getStatus(baseShift, areaShift, Scratch.Note.BEAT.shift) ? Scratch.Note.BEAT.shortName : '-'),
				(getStatus(baseShift, areaShift, Scratch.Note.LONG_ON.shift) ? Scratch.Note.LONG_ON.shortName : '-'),
				(getStatus(baseShift, areaShift, Scratch.Note.LONG.shift) ? Scratch.Note.LONG.shortName : '-'));
	}
}
