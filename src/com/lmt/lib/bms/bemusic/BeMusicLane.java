package com.lmt.lib.bms.bemusic;

/**
 * 譜面のレーンを表します。
 *
 * <p>Be-Musicでは最大で2レーンまで使用するモードが存在します。シングルプレーではレーンは1つしか使用されませんが
 * ダブルプレーでは2つのレーンを使用します。当列挙型はそのレーンを区別するための値として用います。</p>
 *
 * @since 0.0.1
 */
public enum BeMusicLane {
	/** シングルプレー、またはダブルプレーの左側レーン */
	PRIMARY(0),
	/** ダブルプレーの右側レーン */
	SECONDARY(1);

	/** インデックスによるレーンの解決用配列 */
	private static BeMusicLane[] LANES = { PRIMARY, SECONDARY };

	/** 譜面の最大レーン数 */
	public static final int COUNT = LANES.length;

	/** インデックス */
	private int mIndex;

	/**
	 * コンストラクタ
	 * @param index インデックス
	 */
	private BeMusicLane(int index) {
		mIndex = index;
	}

	/**
	 * レーンのインデックスを取得します。
	 * <p>インデックス値は0から始まる正の整数です。BMS仕様としての意味はありませんが、プログラム的に配列にアクセス
	 * するためのインデックス値として使用することを想定しています。</p>
	 * @return レーンのインデックス
	 */
	public final int getIndex() {
		return mIndex;
	}

	/**
	 * レーンのインデックスを用いて対応するレーンを取得します。
	 * @param index レーンのインデックス
	 * @return レーン
	 * @exception IndexOutOfBoundsException indexがマイナス値または{@link #COUNT}以上
	 */
	public static BeMusicLane fromIndex(int index) {
		return LANES[index];
	}
}
