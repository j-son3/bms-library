package com.lmt.lib.bms.bemusic;

/**
 * ノートの生値に関連する機能をまとめたクラス。
 */
class RawNotes {
	// ノート生値メモリレイアウト
	// ※右側が最下位ビット
	// --+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
	// [RESERVE][KIND][D][  DEVICE  ][ NTYPE ][ LN ][R][                    TRACK                     ]
	// --+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
	// --------+-------+-----+------------+-------------------------------------------------
	// Name    | Range | Bit | Mask       | Value
	// --------+-------+-----+------------+-------------------------------------------------
	// TRACK   | 00-15 |  16 | 0x0000ffff | トラックID
	// R       |    16 |   1 | 0x00010000 | トラック継続フラグ
	// LN      | 17-18 |   2 | 0x00060000 | ロングノートモード
	// NTYPE   | 19-21 |   3 | 0x00380000 | 可視オブジェのノート種別
	// DEVICE  | 22-25 |   4 | 0x03c00000 | 入力デバイス
	// D       | 26    |   1 | 0x04000000 | 入力デバイス未定義フラグ(0:定義あり 1:未定義)
	// KIND    | 27-28 |   2 | 0x18000000 | ノートの種類(0:可視 1:不可視 2:BGA 3:BGM)
	// RESERVE | 29-31 |   3 | 0xe0000000 | 未使用

	/** 可視オブジェを表す値 */
	static final int VISIBLE = 0;
	/** 不可視オブジェを表す値 */
	static final int INVISIBLE = 1;
	/** BGAを表す値 */
	static final int BGA = 2;
	/** BGMを表す値 */
	static final int BGM = 3;

	/** トラックID・音声データの再開要否・ロングノートモードが格納されている領域のビットマスク */
	static final int MASK_VALUE = 0x0007ffff;

	/**
	 * トラックID取得
	 * @param raw ノートの生値
	 * @return トラックID
	 */
	static int getTrackId(int raw) {
		return raw & 0x0000ffff;
	}

	/**
	 * ノートの値取得
	 * @param raw ノートの生値
	 * @return ノートの値
	 */
	static int getValue(int raw) {
		return raw & 0x0007ffff;
	}

	/**
	 * 音声データの再開要否取得
	 * @param raw ノートの生値
	 * @return 音声データの再開要否
	 */
	static boolean isRestartTrack(int raw) {
		return (raw & 0x00010000) == 0;
	}

	/**
	 * ロングノートモード取得
	 * @param raw ノートの生値
	 * @param defaultMode デフォルトのロングノートモード
	 * @return ロングノートモード、またはデフォルトのロングノートモード
	 */
	static BeMusicLongNoteMode getLongNoteMode(int raw, BeMusicLongNoteMode defaultMode) {
		var lnMode = (raw & 0x00060000) >> 17;
		return (lnMode == 0) ? defaultMode : BeMusicLongNoteMode.fromNative(lnMode);
	}

	/**
	 * ノート種別取得
	 * @param raw ノートの生値
	 * @return ノート種別
	 */
	static BeMusicNoteType getNoteType(int raw) {
		var noteTypeId = (raw & 0x00380000) >> 19;
		return BeMusicNoteType.fromId(noteTypeId);
	}

	/**
	 * 入力デバイス取得
	 * @param raw ノートの生値
	 * @return 入力デバイスまたはnull
	 */
	static BeMusicDevice getDevice(int raw) {
		return ((raw & 0x04000000) == 0) ? BeMusicDevice.fromIndex((raw & 0x03c00000) >> 22) : null;
	}

	/**
	 * 生値の種類取得
	 * @param raw ノートの生値
	 * @return 生値の種類
	 */
	static int getKind(int raw) {
		return (raw & 0x18000000) >> 27;
	}

	/**
	 * 可視オブジェの生値生成
	 * @param devIndex 入力デバイスインデックス(マイナス値で割り当てなし)
	 * @param noteTypeId ノート種別ID(マイナス値で{@link BeMusicNoteType#NONE}と同等)
	 * @param value ノートの値
	 * @return 可視オブジェの生値
	 */
	static int visible(int devIndex, int noteTypeId, int value) {
		return of(VISIBLE, devIndex, noteTypeId, value);
	}

	/**
	 * 不可視オブジェの生値生成
	 * @param devIndex 入力デバイスインデックス(マイナス値で割り当てなし)
	 * @param value ノートの値
	 * @return 不可視オブジェの生値
	 */
	static int invisible(int devIndex, int value) {
		return of(INVISIBLE, devIndex, -1, value);
	}

	/**
	 * BGAの生値生成
	 * @param value ノートの値
	 * @return BGAの生値
	 */
	static int bga(int value) {
		return of(BGA, -1, -1, value);
	}

	/**
	 * BGMの生値生成
	 * @param value ノートの値
	 * @return BGMの生値
	 */
	static int bgm(int value) {
		return of(BGM, -1, -1, value);
	}

	/**
	 * サウンドに関連するノートの値生成
	 * @param trackId トラックID
	 * @param isRestartTrack 音声データの再開要否
	 * @param lnMode ロングノートモード(nullを指定すると未定義(0)とする)
	 * @return サウンドに関連するノートの値
	 */
	static int soundValue(int trackId, boolean isRestartTrack, BeMusicLongNoteMode lnMode) {
		var bitsTrackId = trackId;
		var bitsIsRestart = isRestartTrack ? 0 : 1;  // データ上は「継続フラグ」を意味するため値が逆転する
		var bitsLnMode = (lnMode == null) ? 0 : (int)lnMode.getNativeValue();
		return bitsTrackId | (bitsIsRestart << 16) | (bitsLnMode << 17);
	}

	/**
	 * ノートの生値生成
	 * @param kind ノートの種類を表す値
	 * @param devIndex 入力デバイスインデックス(マイナス値指定で入力デバイスなし)
	 * @param noteTypeId ノート種別ID(マイナス値指定で{@link BeMusicNoteType#NONE}と同値)
	 * @param value ノートの値(トラックID・音声データの再開要否・ロングノートモードの組み合わせ)
	 * @return ノートの生値
	 */
	static int of(int kind, int devIndex, int noteTypeId, int value) {
		var ntype = (noteTypeId < 0) ? 0 : noteTypeId;
		var device = (devIndex < 0) ? 0 : devIndex;
		var devDef = (devIndex < 0) ? 1 : 0;
		return (value & 0x0007ffff) | ((ntype & 0x00000007) << 19) | ((device & 0x0000000f) << 22) |
				((devDef & 0x00000001) << 26) | ((kind & 0x00000003) << 27);
	}
}
