package com.lmt.lib.bms.bemusic;

/**
 * 譜面のレーンを表します。
 *
 * <p>Be-Musicでは最大で2レーンまで使用するモードが存在します。シングルプレーではレーンは1つしか使用されませんが
 * ダブルプレーでは2つのレーンを使用します。当列挙型はそのレーンを区別するための値として用います。</p>
 */
public enum BeMusicLane {
	/** シングルプレー、またはダブルプレーの左側レーン */
	PRIMARY(0),
	/** ダブルプレーの右側レーン */
	SECONDARY(1);

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
}
