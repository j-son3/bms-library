package com.lmt.lib.bms.bemusic;

import static com.lmt.lib.bms.internal.Assertion.*;

import com.lmt.lib.bms.BmsNote;

/**
 * サウンドに関連するノートの情報を扱うクラスです。
 *
 * <p>当クラスはタイムライン情報を含んだ{@link BmsNote}を継承したものではなく、
 * サウンド関連のノートに含まれる値にアクセスするための静的メソッド、定数値などを集めた静的なクラスです。
 * 「サウンドに関連するノート」とは具体的に以下のチャンネルのノートを示します。</p>
 *
 * <ul>
 * <li>{@link BeMusicChannel#BGM}</li>
 * <li>{@link BeMusicChannel#VISIBLE_1P_01}～{@link BeMusicChannel#VISIBLE_2P_35}</li>
 * <li>{@link BeMusicChannel#INVISIBLE_1P_01}～{@link BeMusicChannel#INVISIBLE_2P_35}</li>
 * <li>{@link BeMusicChannel#LONG_1P_01}～{@link BeMusicChannel#LONG_2P_35}</li>
 * </ul>
 */
public class BeMusicSoundNote {
	// * サウンドに関連するノートの値のデータレイアウト
	// -----------------------------
	// BitRange | Value
	// -----------------------------
	// 00 - 15  | トラックID
	// 16 - 16  | トラック継続フラグ
	// 17 - 19  | ロングノートモード

	/** ノートの値のうち、情報が格納されている領域のビットマスク */
	static final int USE_BITS_MASK = 0x000effff;

	/** 音声データのトラックID最小値 */
	public static final int TRACK_ID_MIN = 1;
	/** 音声データのトラックID最大値 */
	public static final int TRACK_ID_MAX = 65535;

	/**
	 * 音声データのトラックIDを取得します。
	 * <p>トラックIDは{@link BeMusicMeta#WAV}へアクセスするためのインデックス値と同じ意味を持ちます。</p>
	 * @param noteValue ノートの値
	 * @return 音声データのトラックID
	 */
	public static int getTrackId(int noteValue) {
		return noteValue & 0xffff;
	}

	/**
	 * 音声データの再開要否を取得します。
	 * @param noteValue ノートの値
	 * @return 音声データの再開が必要な場合true
	 */
	public static boolean isRestartTrack(int noteValue) {
		return ((noteValue >> 16) & 0x01) == 0;
	}

	/**
	 * 指定されたノートに割り当てられたロングノートモードを取得します。
	 * <p>ノート個別のロングノートモードが未定義(0)の場合、{@link BeMusicLongNoteMode#LN}を返します。</p>
	 * @param noteValue ノートの値
	 * @return ロングノートモード
	 */
	public static BeMusicLongNoteMode getLongNoteMode(int noteValue) {
		return getLongNoteMode(noteValue, BeMusicLongNoteMode.LN);
	}

	/**
	 * 指定されたノートに割り当てられたロングノートモードを取得します。
	 * <p>ノート個別のロングノートモードが未定義(0)の場合、defaultModeの値を返します。この値はnullでも構いません。</p>
	 * @param noteValue ノートの値
	 * @param defaultMode ロングノートモードが未定義の場合の代替値(null許可)
	 * @return ロングノートモード
	 */
	public static BeMusicLongNoteMode getLongNoteMode(int noteValue, BeMusicLongNoteMode defaultMode) {
		var lnMode = (noteValue >> 17) & 0x03;
		return (lnMode == 0) ? defaultMode : BeMusicLongNoteMode.fromNative(lnMode);
	}

	/**
	 * サウンドに関連するノートの値を生成します。
	 * @param trackId トラックID
	 * @param isRestartTrack 音声データの再開要否
	 * @param lnMode ロングノートモード(nullを指定すると未定義(0)とする)
	 * @return サウンドに関連するノートの値
	 * @exception IllegalArgumentException trackIdが{@link TRACK_ID_MIN}未満、または{@link TRACK_ID_MAX}超過
	 */
	public static int makeValue(int trackId, boolean isRestartTrack, BeMusicLongNoteMode lnMode) {
		assertArgRange(trackId, TRACK_ID_MIN, TRACK_ID_MAX, "trackId");
		var bitsTrackId = trackId;
		var bitsIsRestart = isRestartTrack ? 0 : 1;  // データ上は「継続フラグ」を意味するため値が逆転する
		var bitsLnMode = (lnMode == null) ? 0 : (int)lnMode.getNativeValue();
		return bitsTrackId | (bitsIsRestart << 16) | (bitsLnMode << 17);
	}
}
