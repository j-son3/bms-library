package com.lmt.lib.bms.bemusic;

/**
 * 視覚表示されるノートの種別を表す列挙型です。
 *
 * <p>視覚表示されるノートは可視オブジェ、地雷オブジェの2つです。これらは入力デバイスによる操作有無によって
 * プレーの状況が変化します。</p>
 *
 * <p>楽曲位置情報({@link BeMusicPoint})には全レーン、全入力デバイスに対応するノートの情報が格納されており、
 * ノートの種別もそこから参照可能になっています。アプリケーションはノートの種別を参照し、視覚的な演出を行うことができます。</p>
 */
public enum BeMusicNoteType {
	/**
	 * ノートがないことを示します。
	 * <p>ノート種別がこの値を示す場合、そこには何もないことを意味するためアプリケーションは何も行うべきではありません。</p>
	 */
	NONE(0, false, false),
	/**
	 * 入力デバイスを1回操作するべきノートであることを示します。(短押しノート)
	 */
	BEAT(1, true, true),
	/**
	 * 長押しノートの操作が継続中であることを示します。
	 * <p>このノートが登場する間は、{@link #LONG_ON}で開始した操作を継続することを求めます。</p>
	 */
	LONG(2, false, false),
	/**
	 * 入力デバイスの操作を行い、その操作の継続を開始するノートであることを示します。(長押しノート)
	 * <p>この操作は、スイッチであれば押しっぱなし、スクラッチであれば単一方向に動かしっぱなしすることを求めます。</p>
	 */
	LONG_ON(3, true, true),
	/**
	 * 長押しノートの操作を終了するノートであることを示します。
	 * <p>この操作は、スイッチであれば押すのをやめ、スクラッチであれば動かすのをやめることを求めます。</p>
	 */
	LONG_OFF(4, false, true),
	/**
	 * 地雷オブジェであることを示します。
	 * <p>プレイヤーはこのオブジェ付近で入力デバイスを操作してはなりません。スイッチであれば押すとミス、
	 * スクラッチであればどちらか単一の方向に動かすとミスと判定されます。</p>
	 */
	LANDMINE(5, false, true);

	/** ID */
	private int mId;
	/** ノート数としてカウントされるべきかどうか */
	private boolean mIsCountNotes;
	/** 操作可能ノートであるかどうか */
	private boolean mIsPlayable;

	/**
	 * コンストラクタ
	 * @param id ID
	 * @param isCountNotes ノート数としてカウントされるかどうか
	 * @param isPlayable 操作可能ノートであるかどうか
	 */
	private BeMusicNoteType(int id, boolean isCountNotes, boolean isPlayable) {
		mId = id;
		mIsCountNotes = isCountNotes;
		mIsPlayable = isPlayable;
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
	 * ノート種別のIDから列挙値を取得します。
	 * @param id ノート種別のID
	 * @return IDに該当するノート種別。IDに該当する列挙値が存在しない場合は{@link #NONE}
	 */
	public static BeMusicNoteType fromId(int id) {
		for (var type : values()) { if (id == type.getId()) { return type; } }
		return NONE;
	}
}
