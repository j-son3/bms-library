package com.lmt.lib.bms.bemusic;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 視覚表示されるノートの種別を表す列挙型です。
 *
 * <p>視覚表示されるノートは可視オブジェ、地雷オブジェの2つです。これらは入力デバイスによる操作有無によって
 * プレーの状況が変化します。</p>
 *
 * <p>楽曲位置情報({@link BeMusicPoint})には全レーン、全入力デバイスに対応するノートの情報が格納されており、
 * ノートの種別もそこから参照可能になっています。アプリケーションはノートの種別を参照し、視覚的な演出を行うことができます。</p>
 *
 * @since 0.0.1
 */
public enum BeMusicNoteType {
	/**
	 * ノートがないことを示します。
	 * <p>ノート種別がこの値を示す場合、そこには何もないことを意味するためアプリケーションは何も行うべきではありません。</p>
	 */
	NONE(0, false, false, false, false, false, false),
	/**
	 * 入力デバイスを1回操作するべきノートであることを示します。(短押しノート)
	 */
	BEAT(1, true, false, false, true, true, true),
	/**
	 * 長押しノートの操作が継続中であることを示します。
	 * <p>このノートが登場する間は、{@link #LONG_ON}で開始した操作を継続することを求めます。</p>
	 */
	LONG(2, false, true, false, false, false, false),
	/**
	 * 入力デバイスの操作を行い、その操作の継続を開始するノートであることを示します。(長押しノート)
	 * <p>この操作は、スイッチであれば押しっぱなし、スクラッチであれば単一方向に動かしっぱなしすることを求めます。</p>
	 */
	LONG_ON(3, true, true, false, true, false, true),
	/**
	 * 長押しノートの操作を終了するノートであることを示します。
	 * <p>この操作は、スイッチであれば押すのをやめ、スクラッチであれば動かすのをやめることを求めます。</p>
	 * <p>この種別はログノートモードが{@link BeMusicLongNoteMode#LN}の時の終端であることを示します。</p>
	 */
	LONG_OFF(4, false, true, true, true, true, false),
	/**
	 * 長押しノートの操作を終了するノートであることを示します。
	 * <p>この操作は、スイッチであれば押すのをやめ、スクラッチであれば動かすのをやめることを求めます。</p>
	 * <p>この種別はロングノートモードが{@link BeMusicLongNoteMode#CN}または{@link BeMusicLongNoteMode#HCN}
	 * の時の終端であることを示します。</p>
	 */
	CHARGE_OFF(5, true, true, true, true, true, false),
	/**
	 * 地雷オブジェであることを示します。
	 * <p>プレイヤーはこのオブジェ付近で入力デバイスを操作してはなりません。スイッチであれば押すとミス、
	 * スクラッチであればどちらか単一の方向に動かすとミスと判定されます。</p>
	 */
	MINE(6, false, false, false, true, false, false);

	/** Be Musicにおけるノート種別の数 */
	public static final int COUNT = 7;

	/** IDとノート種別のマップ */
	private static final Map<Integer, BeMusicNoteType> ID_MAP = Stream.of(BeMusicNoteType.values())
			.collect(Collectors.toMap(e -> e.getId(), e -> e));

	/** ID */
	private int mId;
	/** ノート数としてカウントされるべきかどうか */
	private boolean mIsCountNotes;
	/** ロングノートに関連するノート種別かどうか */
	private boolean mIsLongNoteType;
	/** ロングノートの終端であるかどうか */
	private boolean mIsLongNoteTail;
	/** 操作可能ノートであるかどうか */
	private boolean mIsPlayable;
	/** 解放操作を伴うかどうか */
	private boolean mHasUpAction;
	/** 押下操作を伴うかどうか */
	private boolean mHasDownAction;
	/** 何らかの操作を伴うかどうか */
	private boolean mHasMovement;

	/**
	 * コンストラクタ
	 * @param id ID
	 * @param isCountNotes ノート数としてカウントされるかどうか
	 * @param isLongNoteType ロングノートに関連するノート種別かどうか
	 * @param isLongNoteTail ロングノートの終端であるかどうか
	 * @param isPlayable 操作可能ノートであるかどうか
	 * @param hasUpAction 解放操作を伴うかどうか
	 * @param hasDownAction 押下操作を伴うかどうか
	 */
	private BeMusicNoteType(int id, boolean isCountNotes, boolean isLongNoteType, boolean isLongNoteTail,
			boolean isPlayable, boolean hasUpAction, boolean hasDownAction) {
		mId = id;
		mIsCountNotes = isCountNotes;
		mIsLongNoteType = isLongNoteType;
		mIsLongNoteTail = isLongNoteTail;
		mIsPlayable = isPlayable;
		mHasUpAction = hasUpAction;
		mHasDownAction = hasDownAction;
		mHasMovement = hasUpAction || hasDownAction;
	}

	/**
	 * ノート種別のIDを取得します。
	 * <p>この値は{@link #ordinal()}と類似していますが、将来の拡張により列挙値の宣言順が変わっても
	 * 値が変化しないことを保証します。</p>
	 * @return ノート種別のID
	 */
	public final int getId() {
		return mId;
	}

	/**
	 * このノート種別がノート数としてカウントされるかどうかを判定します。
	 * @return ノート数としてカウントされるべき場合はtrue、そうでない場合はfalse
	 */
	public final boolean isCountNotes() {
		return mIsCountNotes;
	}

	/**
	 * このノート種別が長押し継続中を示すかどうかを判定します。
	 * <p>当メソッドがtrueを返す時、ノート種別は{@link #LONG}であることを示します。</p>
	 * @return 長押し継続中の場合はtrue、そうでない場合はfalse
	 * @since 0.5.0
	 */
	public final boolean isHolding() {
		return this == LONG;
	}

	/**
	 * このノート種別が長押し開始を示すかどうかを判定します。
	 * <p>当メソッドがtrueを返す時、ノート種別は{@link #LONG_ON}であることを示します。</p>
	 * @return 長押し開始の場合はtrue、そうでない場合はfalse
	 * @since 0.6.0
	 */
	public final boolean isLongNoteHead() {
		return this == LONG_ON;
	}

	/**
	 * このノート種別がロングノートの終端であるかどうかを判定します。
	 * <p>この値はロングノートモード({@link BeMusicLongNoteMode})が何であるかは問いません。</p>
	 * @return ノート種別がロングノートの終端である場合はtrue、そうでない場合はfalse
	 * @see BeMusicLongNoteMode
	 * @since 0.6.0
	 */
	public final boolean isLongNoteTail() {
		return mIsLongNoteTail;
	}

	/**
	 * このノート種別がロングノートに関連する種別であるかどうかを判定します。
	 * <p>具体的には{@link #LONG_ON}, {@link #LONG}, {@link #LONG_OFF}, {@link #CHARGE_OFF}の場合にtrueを返します。</p>
	 * @return ロングノートに関連する種別の場合はtrue、そうでない場合はfalse
	 * @since 0.6.0
	 */
	public final boolean isLongNoteType() {
		return mIsLongNoteType;
	}

	/**
	 * このノート種別が操作可能ノートであるかどうかを判定します。
	 * @return 操作可能ノートである場合はtrue、そうでない場合はfalse
	 */
	public final boolean isPlayable() {
		return mIsPlayable;
	}

	/**
	 * このノート種別が視覚効果を持つノートであるかどうかを判定します。
	 * @return 視覚効果を持つノートである場合はtrue、そうでない場合はfalse
	 */
	public final boolean hasVisualEffect() {
		return this != NONE;
	}

	/**
	 * このノート種別が入力デバイスの解放操作を伴うかどうかを判定します。
	 * <p>解放操作とは、入力デバイスから手を放すことを示します。従って、長押し終了や短押しを表します。
	 * 長押し継続中のように操作状態に変化のないノート種別は該当しません。</p>
	 * @return 解放操作を伴うノートである場合はtrue、そうでない場合はfalse
	 * @since 0.5.0
	 */
	public final boolean hasUpAction() {
		return mHasUpAction;
	}

	/**
	 * このノート種別が入力デバイスの押下操作を伴うかどうかを判定します。
	 * <p>押下操作とは、入力デバイスに触れることを示します。従って、長押し開始や短押しを表します。
	 * 長押し継続中のように操作状態に変化のないノート種別は該当しません。</p>
	 * @return 押下操作を伴うノートである場合はtrue、そうでない場合はfalse
	 * @since 0.5.0
	 */
	public final boolean hasDownAction() {
		return mHasDownAction;
	}

	/**
	 * このノート種別が何らかの操作を伴うかどうかを判定します。
	 * <p>具体的には{@link #hasUpAction()}, {@link #hasDownAction()}のいずれかがtrueを返すかを表します。</p>
	 * @return 何らかの操作を伴うノートである場合はtrue、そうでない場合はfalse
	 * @see #hasUpAction()
	 * @see #hasDownAction()
	 * @since 0.5.0
	 */
	public final boolean hasMovement() {
		return mHasMovement;
	}

	/**
	 * ノート種別のIDから列挙値を取得します。
	 * @param id ノート種別のID
	 * @return IDに該当するノート種別。IDに該当する列挙値が存在しない場合は{@link #NONE}
	 */
	public static BeMusicNoteType fromId(int id) {
		var noteType = ID_MAP.get(id);
		return Objects.requireNonNullElse(noteType, NONE);
	}
}
