package com.lmt.lib.bms.bemusic;

import com.lmt.lib.bms.BmsChannel;

/**
 * 入力デバイスを表します。
 *
 * <p>BeMusicでは、1つのレーンは最大で7個のON/OFFを表すスイッチと、1個の2方向に操作可能な機器で構成されます。
 * そのようなレーンが最大で2つ存在し、プレースタイルによって1つのレーンのみ使用する場合と、2つのレーンを
 * 使用する場合に分かれます。</p>
 *
 * <p>当列挙型は機器の最小単位である「入力デバイス」1個分を表し、必要な属性・情報を保有しています。
 * チャンネル、レーン、プログラム上のインデックス値を解決するために当列挙型を使用することとなります。</p>
 */
public enum BeMusicDevice {
	/** スイッチ1-1 */
	SWITCH11(0,
			BeMusicLane.PRIMARY,
			BeMusicChannel.VISIBLE_1P_01,
			BeMusicChannel.INVISIBLE_1P_01,
			BeMusicChannel.LANDMINE_1P_01),
	/** スイッチ1-2 */
	SWITCH12(1,
			BeMusicLane.PRIMARY,
			BeMusicChannel.VISIBLE_1P_02,
			BeMusicChannel.INVISIBLE_1P_02,
			BeMusicChannel.LANDMINE_1P_02),
	/** スイッチ1-3 */
	SWITCH13(2,
			BeMusicLane.PRIMARY,
			BeMusicChannel.VISIBLE_1P_03,
			BeMusicChannel.INVISIBLE_1P_03,
			BeMusicChannel.LANDMINE_1P_03),
	/** スイッチ1-4 */
	SWITCH14(3,
			BeMusicLane.PRIMARY,
			BeMusicChannel.VISIBLE_1P_04,
			BeMusicChannel.INVISIBLE_1P_04,
			BeMusicChannel.LANDMINE_1P_04),
	/** スイッチ1-5 */
	SWITCH15(4,
			BeMusicLane.PRIMARY,
			BeMusicChannel.VISIBLE_1P_05,
			BeMusicChannel.INVISIBLE_1P_05,
			BeMusicChannel.LANDMINE_1P_05),
	/** スイッチ1-6 */
	SWITCH16(5,
			BeMusicLane.PRIMARY,
			BeMusicChannel.VISIBLE_1P_08,
			BeMusicChannel.INVISIBLE_1P_08,
			BeMusicChannel.LANDMINE_1P_08),
	/** スイッチ1-7 */
	SWITCH17(6,
			BeMusicLane.PRIMARY,
			BeMusicChannel.VISIBLE_1P_09,
			BeMusicChannel.INVISIBLE_1P_09,
			BeMusicChannel.LANDMINE_1P_09),
	/** スクラッチ1 */
	SCRATCH1(7,
			BeMusicLane.PRIMARY,
			BeMusicChannel.VISIBLE_1P_06,
			BeMusicChannel.INVISIBLE_1P_06,
			BeMusicChannel.LANDMINE_1P_06),
	/** スイッチ2-1 */
	SWITCH21(8,
			BeMusicLane.SECONDARY,
			BeMusicChannel.VISIBLE_2P_01,
			BeMusicChannel.INVISIBLE_2P_01,
			BeMusicChannel.LANDMINE_2P_01),
	/** スイッチ2-2 */
	SWITCH22(9,
			BeMusicLane.SECONDARY,
			BeMusicChannel.VISIBLE_2P_02,
			BeMusicChannel.INVISIBLE_2P_02,
			BeMusicChannel.LANDMINE_2P_02),
	/** スイッチ2-3 */
	SWITCH23(10,
			BeMusicLane.SECONDARY,
			BeMusicChannel.VISIBLE_2P_03,
			BeMusicChannel.INVISIBLE_2P_03,
			BeMusicChannel.LANDMINE_2P_03),
	/** スイッチ2-4 */
	SWITCH24(11,
			BeMusicLane.SECONDARY,
			BeMusicChannel.VISIBLE_2P_04,
			BeMusicChannel.INVISIBLE_2P_04,
			BeMusicChannel.LANDMINE_2P_04),
	/** スイッチ2-5 */
	SWITCH25(12,
			BeMusicLane.SECONDARY,
			BeMusicChannel.VISIBLE_2P_05,
			BeMusicChannel.INVISIBLE_2P_05,
			BeMusicChannel.LANDMINE_2P_05),
	/** スイッチ2-6 */
	SWITCH26(13,
			BeMusicLane.SECONDARY,
			BeMusicChannel.VISIBLE_2P_08,
			BeMusicChannel.INVISIBLE_2P_08,
			BeMusicChannel.LANDMINE_2P_08),
	/** スイッチ2-7 */
	SWITCH27(14,
			BeMusicLane.SECONDARY,
			BeMusicChannel.VISIBLE_2P_09,
			BeMusicChannel.INVISIBLE_2P_09,
			BeMusicChannel.LANDMINE_2P_09),
	/** スクラッチ2 */
	SCRATCH2(15,
			BeMusicLane.SECONDARY,
			BeMusicChannel.VISIBLE_2P_06,
			BeMusicChannel.INVISIBLE_2P_06,
			BeMusicChannel.LANDMINE_2P_06);

	/** Be Musicにおける入力デバイス数 */
	public static final int COUNT = 16;

	/** インデックスによる入力デバイスの解決用配列 */
	private static final BeMusicDevice[] DEVICES = new BeMusicDevice[] {
			SWITCH11, SWITCH12, SWITCH13, SWITCH14, SWITCH15, SWITCH16, SWITCH17, SCRATCH1,
			SWITCH21, SWITCH22, SWITCH23, SWITCH24, SWITCH25, SWITCH26, SWITCH27, SCRATCH2,
	};

	/** インデックス */
	private int mIndex;
	/** レーン */
	private BeMusicLane mLane;
	/** 可視オブジェのチャンネル */
	private BmsChannel mVisibleChannel;
	/** 不可視オブジェのチャンネル */
	private BmsChannel mInvisibleChannel;
	/** 地雷オブジェのチャンネル */
	private BmsChannel mLandmineChannel;

	/**
	 * コンストラクタ
	 * @param index インデックス
	 * @param lane レーン
	 * @param visible 可視オブジェのチャンネル
	 * @param invisible 不可視オブジェのチャンネル
	 * @param landmine 地雷オブジェのチャンネル
	 */
	private BeMusicDevice(int index, BeMusicLane lane, BmsChannel visible, BmsChannel invisible, BmsChannel landmine) {
		mIndex = index;
		mLane = lane;
		mVisibleChannel = visible;
		mInvisibleChannel = invisible;
		mLandmineChannel = landmine;
	}

	/**
	 * この入力デバイスのインデックスを取得します。
	 * <p>インデックス値は0から始まる正の整数です。BMS仕様としての意味はありませんが、プログラム的に配列にアクセス
	 * するためのインデックス値として使用することを想定しています。</p>
	 * @return ラインのインデックス
	 */
	public final int getIndex() {
		return mIndex;
	}

	/**
	 * この入力デバイスが存在するレーンを取得します。
	 * @return レーン
	 */
	public final BeMusicLane getLane() {
		return mLane;
	}

	/**
	 * この入力デバイスに対応する可視オブジェチャンネルを取得します。
	 * @return 可視オブジェチャンネル
	 */
	public final BmsChannel getVisibleChannel() {
		return mVisibleChannel;
	}

	/**
	 * この入力デバイスに対応する不可視オブジェチャンネルを取得します。
	 * @return 不可視オブジェチャンネル
	 */
	public final BmsChannel getInvisibleChannel() {
		return mInvisibleChannel;
	}

	/**
	 * この入力デバイスに対応する地雷オブジェチャンネルを取得します。
	 * @return 地雷オブジェチャンネル
	 */
	public final BmsChannel getLandmineChannel() {
		return mLandmineChannel;
	}

	/**
	 * 入力デバイスのインデックスを用いて対応する入力デバイスを取得します。
	 * @param index 入力デバイスのインデックス
	 * @return 入力デバイス
	 * @exception IndexOutOfBoundsException indexがマイナス値または{@link #COUNT}以上
	 */
	public static BeMusicDevice fromIndex(int index) {
		return DEVICES[index];
	}
}
