package com.lmt.lib.bms.bemusic;

import static com.lmt.lib.bms.internal.Assertion.*;

/**
 * サウンドに関連するノートの情報を扱うクラスです。
 *
 * <p>当クラスはサウンド関連のノート生値に含まれる値にアクセスするための静的メソッド、定数値等を集めたものです。
 * 「サウンドに関連するノート」とは具体的に以下のチャンネルのノートを示します。</p>
 *
 * <ul>
 * <li>{@link BeMusicChannel#BGM}</li>
 * <li>{@link BeMusicChannel#VISIBLE_1P_01}～{@link BeMusicChannel#VISIBLE_2P_35}</li>
 * <li>{@link BeMusicChannel#INVISIBLE_1P_01}～{@link BeMusicChannel#INVISIBLE_2P_35}</li>
 * <li>{@link BeMusicChannel#LONG_1P_01}～{@link BeMusicChannel#LONG_2P_35}</li>
 * <li>{@link BeMusicChannel#MINE_1P_01}～{@link BeMusicChannel#MINE_1P_09}</li>
 * </ul>
 *
 * @since 0.7.0
 */
public class BeMusicSound {
	/** 音声データのトラックID最小値 */
	public static final int TRACK_ID_MIN = 1;
	/** 音声データのトラックID最大値 */
	public static final int TRACK_ID_MAX = 65535;

	/**
	 * 音声データのトラックIDを取得します。
	 * <p>トラックIDは{@link BeMusicMeta#WAV}へアクセスするためのインデックス値と同じ意味を持ちます。</p>
	 * @param raw ノートの生値
	 * @return 音声データのトラックID
	 */
	public static int getTrackId(int raw) {
		return RawNotes.getTrackId(raw);
	}

	/**
	 * 音声データの再開要否を取得します。
	 * @param raw ノートの生値
	 * @return 音声データの再開が必要な場合true
	 */
	public static boolean isRestartTrack(int raw) {
		return RawNotes.isRestartTrack(raw);
	}

	/**
	 * 指定されたノートに割り当てられたロングノートモードを取得します。
	 * <p>ノート個別のロングノートモードが未定義(0)の場合、{@link BeMusicLongNoteMode#LN}を返します。</p>
	 * @param raw ノートの生値
	 * @return ロングノートモード
	 */
	public static BeMusicLongNoteMode getLongNoteMode(int raw) {
		return RawNotes.getLongNoteMode(raw, BeMusicLongNoteMode.LN);
	}

	/**
	 * 指定されたノートに割り当てられたロングノートモードを取得します。
	 * <p>ノート個別のロングノートモードが未定義(0)の場合、defaultModeの値を返します。この値はnullでも構いません。</p>
	 * @param raw ノートの生値
	 * @param defaultMode ロングノートモードが未定義の場合の代替値(null許可)
	 * @return ロングノートモード
	 */
	public static BeMusicLongNoteMode getLongNoteMode(int raw, BeMusicLongNoteMode defaultMode) {
		return RawNotes.getLongNoteMode(raw, defaultMode);
	}

	/**
	 * ノート種別を取得します。
	 * <p>当メソッドは可視オブジェの生値に対してのみ有効です。それ以外の種類の生値を指定しても
	 * {@link BeMusicNoteType#NONE} を返します。</p>
	 * @param raw ノートの生値
	 * @return ノート種別
	 * @since 0.8.0
	 */
	public static BeMusicNoteType getNoteType(int raw) {
		return RawNotes.getNoteType(raw);
	}

	/**
	 * 入力デバイスを取得します。
	 * <p>入力デバイスは可視オブジェ、および不可視オブジェに対して割り当てられています。
	 * それ以外の生値を指定した場合nullを返します。</p>
	 * @param raw ノートの生値
	 * @return 入力デバイスまたはnull
	 * @since 0.8.0
	 */
	public static BeMusicDevice getDevice(int raw) {
		return RawNotes.getDevice(raw);
	}

	/**
	 * 入力デバイスが存在するかどうかを取得します。
	 * <p>入力デバイスは可視オブジェと不可視オブジェに割り当てられているので、それらの生値を指定するとtrueを返します。
	 * それ以外の生値ではfalseを返します。</p>
	 * @param raw ノートの生値
	 * @return 入力デバイスが存在すればtrue
	 * @since 0.8.0
	 */
	public static boolean hasDevice(int raw) {
		return RawNotes.getDevice(raw) != null;
	}

	/**
	 * トラックIDが存在するかどうかを取得します。
	 * <p>当メソッドはトラックIDが0でない時にtrueを返します。</p>
	 * @param raw ノートの生値
	 * @return トラックIDが存在すればtrue
	 * @see #getTrackId(int)
	 * @since 0.8.0
	 */
	public static boolean hasTrack(int raw) {
		return RawNotes.getTrackId(raw) != 0;
	}

	/**
	 * ノートの値の拡張情報が存在するかどうかを取得します。
	 * <p>拡張情報とは、ノートの値のうちトラックID以外の情報を指します。
	 * それらの情報いずれかのデータが0以外の値を示す時にtrueを返します。</p>
	 * @param raw ノートの生値
	 * @return 拡張情報が存在すればtrue
	 * @since 0.8.0
	 */
	public static boolean hasExtended(int raw) {
		return (RawNotes.getValue(raw) & 0x00070000) != 0;
	}

	/**
	 * 指定した生値が可視オブジェの値であるかどうかを取得します。
	 * @param raw ノートの生値
	 * @return 生値が可視オブジェの値であればtrue
	 * @since 0.8.0
	 */
	public static boolean isVisible(int raw) {
		return RawNotes.getKind(raw) == RawNotes.VISIBLE;
	}

	/**
	 * 指定した生値が不可視オブジェの値であるかどうかを取得します。
	 * @param raw ノートの生値
	 * @return 生値が不可視オブジェの値であればtrue
	 * @since 0.8.0
	 */
	public static boolean isInvisible(int raw) {
		return RawNotes.getKind(raw) == RawNotes.INVISIBLE;
	}

	/**
	 * 指定した生値がBGMの値であるかどうかを取得します。
	 * @param raw ノートの生値
	 * @return 生値がBGMの値であればtrue
	 * @since 0.8.0
	 */
	public static boolean isBgm(int raw) {
		return RawNotes.getKind(raw) == RawNotes.BGM;
	}

	/**
	 * サウンドに関連するノートの値を生成します。
	 * @param trackId トラックID
	 * @param isRestartTrack 音声データの再開要否
	 * @param lnMode ロングノートモード(nullを指定すると未定義(0)とする)
	 * @return サウンドに関連するノートの値
	 * @throws IllegalArgumentException trackIdが{@link TRACK_ID_MIN}未満、または{@link TRACK_ID_MAX}超過
	 */
	public static int makeValue(int trackId, boolean isRestartTrack, BeMusicLongNoteMode lnMode) {
		assertArgRange(trackId, TRACK_ID_MIN, TRACK_ID_MAX, "trackId");
		return RawNotes.soundValue(trackId, isRestartTrack, lnMode);
	}
}
