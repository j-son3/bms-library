package com.lmt.lib.bms.internal.deltasystem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.lmt.lib.bms.bemusic.BeMusicDevice;

/**
 * 運指を表現する列挙型
 *
 * <p>この列挙型は実際のプレーで一般的となった運指をカタログ化し、その運指の特性をパッケージする。
 * 主な目的としては、当該運指における指同士が受ける動作の抵抗を管理することにある。運指による指の抵抗値を評価点に
 * 反映させ、可能な限り実際のプレーに近い評価を出力できるようにすることを最終目標とする。</p>
 */
enum Fingering {
	/** 1048式 */
	SP_DEFAULT(0, 'D',
			Map.ofEntries(
					Map.entry(BeMusicDevice.SWITCH11, Finger.LTHUMB),
					Map.entry(BeMusicDevice.SWITCH12, Finger.LMIDDLE),
					Map.entry(BeMusicDevice.SWITCH13, Finger.LINDEX),
					Map.entry(BeMusicDevice.SWITCH14, Finger.RINDEX),
					Map.entry(BeMusicDevice.SWITCH15, Finger.RTHUMB),
					Map.entry(BeMusicDevice.SWITCH16, Finger.RMIDDLE),
					Map.entry(BeMusicDevice.SWITCH17, Finger.RLITTLE)),
			Map.ofEntries(
					Map.entry(Finger.LTHUMB, resists(r(Finger.LINDEX, 0.8), r(Finger.LMIDDLE, 0.2))),
					Map.entry(Finger.LINDEX, resists(r(Finger.LTHUMB, 0.4), r(Finger.LMIDDLE, 0.6))),
					Map.entry(Finger.LMIDDLE, resists(r(Finger.LTHUMB, 0.6), r(Finger.LINDEX, 0.4))),
					Map.entry(Finger.RTHUMB, resists(r(Finger.RINDEX, 0.5), r(Finger.RMIDDLE, 0.2), r(Finger.RLITTLE, 0.3))),
					Map.entry(Finger.RINDEX, resists(r(Finger.RTHUMB, 0.4), r(Finger.RMIDDLE, 0.4), r(Finger.RLITTLE, 0.2))),
					Map.entry(Finger.RMIDDLE, resists(r(Finger.RTHUMB, 0.1), r(Finger.RINDEX, 0.5), r(Finger.RLITTLE, 0.4))),
					Map.entry(Finger.RLITTLE, resists(r(Finger.RTHUMB, 0.3), r(Finger.RINDEX, 0.1), r(Finger.RMIDDLE, 0.6))))),

	/** 3・5半固定 */
	SP_SCRATCH(1, 'S',
			Map.ofEntries(
					Map.entry(BeMusicDevice.SCRATCH1, Finger.LLITTLE),
					Map.entry(BeMusicDevice.SWITCH11, Finger.LTHUMB),
					Map.entry(BeMusicDevice.SWITCH12, Finger.LINDEX),
					Map.entry(BeMusicDevice.SWITCH13, Finger.RTHUMB),
					Map.entry(BeMusicDevice.SWITCH14, Finger.RINDEX),
					Map.entry(BeMusicDevice.SWITCH15, Finger.RMIDDLE),
					Map.entry(BeMusicDevice.SWITCH16, Finger.RRING),
					Map.entry(BeMusicDevice.SWITCH17, Finger.RLITTLE)),
			Map.ofEntries(
					Map.entry(Finger.LTHUMB, resists(r(Finger.LINDEX, 0.4), r(Finger.LLITTLE, 0.6))),
					Map.entry(Finger.LINDEX, resists(r(Finger.LTHUMB, 0.2), r(Finger.LLITTLE, 0.8))),
					Map.entry(Finger.LLITTLE, resists(r(Finger.LTHUMB, 0.3), r(Finger.LINDEX, 0.7))),
					Map.entry(Finger.RTHUMB, resists(r(Finger.RINDEX, 0.4), r(Finger.RMIDDLE, 0.5), r(Finger.RLITTLE, 0.1))),
					Map.entry(Finger.RINDEX, resists(r(Finger.RTHUMB, 0.2), r(Finger.RMIDDLE, 0.7), r(Finger.RLITTLE, 0.1))),
					Map.entry(Finger.RMIDDLE, resists(r(Finger.RTHUMB, 0.3), r(Finger.RINDEX, 0.3), r(Finger.RRING, 0.8), r(Finger.RLITTLE, 0.4))),
					Map.entry(Finger.RRING, resists(r(Finger.RTHUMB, 0.3), r(Finger.RINDEX, 0.3), r(Finger.RMIDDLE, 0.8), r(Finger.RLITTLE, 0.4))),
					Map.entry(Finger.RLITTLE, resists(r(Finger.RTHUMB, 0.1), r(Finger.RINDEX, 0.2), r(Finger.RMIDDLE, 0.7))))),

	/** HOLDING算出用：1048式完全固定として定義する */
	SP_HOLDING(2, 'H',
			Map.ofEntries(
					Map.entry(BeMusicDevice.SCRATCH1, Finger.LLITTLE),
					Map.entry(BeMusicDevice.SWITCH11, Finger.LTHUMB),
					Map.entry(BeMusicDevice.SWITCH12, Finger.LMIDDLE),
					Map.entry(BeMusicDevice.SWITCH13, Finger.LINDEX),
					Map.entry(BeMusicDevice.SWITCH14, Finger.RINDEX),
					Map.entry(BeMusicDevice.SWITCH15, Finger.RTHUMB),
					Map.entry(BeMusicDevice.SWITCH16, Finger.RMIDDLE),
					Map.entry(BeMusicDevice.SWITCH17, Finger.RLITTLE)),
			// HOLDINGでの抵抗値は、標準抵抗1.0にプラスする値として定義する
			// 実際のプレーでBSSが来た時に1048完全固定で捌くのは通常ではあり得ないが、人によってやり方が分かれる点なので
			// BSS中はスクラッチ以外の指が思いっ切り抵抗を受けるという設定にして評価点を疑似ることにする
			Map.ofEntries(
					Map.entry(Finger.LLITTLE, resists(r(Finger.LTHUMB, 0.2), r(Finger.LINDEX, 0.4), r(Finger.LMIDDLE, 0.3))),
					Map.entry(Finger.LTHUMB, resists(r(Finger.LINDEX, 0.25), r(Finger.LMIDDLE, 0.1), r(Finger.LLITTLE, 0.5))),
					Map.entry(Finger.LMIDDLE, resists(r(Finger.LTHUMB, 0.1), r(Finger.LINDEX, 0.3), r(Finger.LLITTLE, 0.5))),
					Map.entry(Finger.LINDEX, resists(r(Finger.LTHUMB, 0.3), r(Finger.LMIDDLE, 0.2), r(Finger.LLITTLE, 0.5))),
					Map.entry(Finger.RINDEX, resists(r(Finger.RTHUMB, 0.4), r(Finger.RMIDDLE, 0.25), r(Finger.RLITTLE, 0.15))),
					Map.entry(Finger.RTHUMB, resists(r(Finger.RINDEX, 0.4), r(Finger.RMIDDLE, 0.1), r(Finger.RLITTLE, 0.2))),
					Map.entry(Finger.RMIDDLE, resists(r(Finger.RINDEX, 0.25), r(Finger.RTHUMB, 0.1), r(Finger.RLITTLE, 0.3))),
					Map.entry(Finger.RLITTLE, resists(r(Finger.RINDEX, 0.3), r(Finger.RTHUMB, 0.15), r(Finger.RMIDDLE, 0.3))))),

	/** GIMMICKの地雷用 */
	SP_MINE(3, 'M',
			Map.ofEntries(
					Map.entry(BeMusicDevice.SCRATCH1, Finger.LLITTLE),
					Map.entry(BeMusicDevice.SWITCH11, Finger.LTHUMB),
					Map.entry(BeMusicDevice.SWITCH12, Finger.LMIDDLE),
					Map.entry(BeMusicDevice.SWITCH13, Finger.LINDEX),
					Map.entry(BeMusicDevice.SWITCH14, Finger.RINDEX),
					Map.entry(BeMusicDevice.SWITCH15, Finger.RTHUMB),
					Map.entry(BeMusicDevice.SWITCH16, Finger.RMIDDLE),
					Map.entry(BeMusicDevice.SWITCH17, Finger.RLITTLE)),
			Map.ofEntries(
					Map.entry(Finger.LLITTLE, resists(r(Finger.LLITTLE, 1.0))),
					Map.entry(Finger.LTHUMB, resists(r(Finger.LTHUMB, 1.0), r(Finger.LMIDDLE, 0.1), r(Finger.LINDEX, 0.1))),
					Map.entry(Finger.LMIDDLE, resists(r(Finger.LMIDDLE, 1.0), r(Finger.LTHUMB, 0.1), r(Finger.LINDEX, 0.1))),
					Map.entry(Finger.LINDEX, resists(r(Finger.LINDEX, 1.0), r(Finger.LTHUMB, 0.1), r(Finger.LMIDDLE, 0.1))),
					Map.entry(Finger.RINDEX, resists(r(Finger.RINDEX, 1.0), r(Finger.RTHUMB, 0.1), r(Finger.RMIDDLE, 0.2))),
					Map.entry(Finger.RTHUMB, resists(r(Finger.RTHUMB, 1.0), r(Finger.RINDEX, 0.1), r(Finger.RMIDDLE, 0.1), r(Finger.RLITTLE, 0.05))),
					Map.entry(Finger.RMIDDLE, resists(r(Finger.RMIDDLE, 1.0), r(Finger.RINDEX, 0.2), r(Finger.RTHUMB, 0.1), r(Finger.RLITTLE, 0.2))),
					Map.entry(Finger.RLITTLE, resists(r(Finger.RLITTLE, 1.0), r(Finger.RINDEX, 0.05), r(Finger.RTHUMB, 0.1), r(Finger.RMIDDLE, 0.2))))),

	/** 左片手 */
	LEFT_HAND(4, 'L', Collections.emptyMap(), Collections.emptyMap()),

	/** DP左レーンスクラッチ */
	LEFT_SCRATCH(5, 'R', Collections.emptyMap(), Collections.emptyMap()),

	/** 右片手 */
	RIGHT_HAND(6, '<', Collections.emptyMap(), Collections.emptyMap()),

	/** DP右レーンスクラッチ */
	RIGHT_SCRATCH(7, '>', Collections.emptyMap(), Collections.emptyMap());

	/** インデックスによる運指のテーブル */
	private static final Fingering[] TABLE = {
			SP_DEFAULT, SP_SCRATCH, SP_HOLDING, LEFT_HAND, LEFT_SCRATCH, RIGHT_HAND, RIGHT_SCRATCH,
	};

	/** インデックス */
	private int mIndex;
	/** 運指の1文字表現 */
	private char mChar;
	/** 指と入力デバイスのマッピングテーブル */
	private BeMusicDevice[] mDevicesFromFinger;
	/** 指と入力デバイスのマッピングテーブル(左手のみ) */
	private BeMusicDevice[] mDevicesLeftHand;
	/** 指と入力デバイスのマッピングテーブル(右手のみ) */
	private BeMusicDevice[] mDevicesRightHand;
	/** 入力デバイスと指のマッピングテーブル */
	private Finger[] mFingersFromDevice;
	/** 入力デバイスと指のマッピングテーブル(左手のみ) */
	private Finger[] mFingersLeftHand;
	/** 入力デバイスと指のマッピングテーブル(右手のみ) */
	private Finger[] mFingersRightHand;
	/** 指同士の抵抗データテーブル */
	private Resist[][] mResistTable;
	/** 手ごとに指が入力デバイスにマッピングされた数 */
	private int[] mFingerCountPerHand = new int[Hand.COUNT];

	/**
	 * コンストラクタ
	 * @param index インデックス
	 * @param ch 運指の1文字表現
	 * @param fingerMap 入力デバイスへの指のマッピング情報
	 * @param resistMap 周囲指からの抵抗情報のマッピング
	 */
	private Fingering(int index, char ch, Map<BeMusicDevice, Finger> fingerMap, Map<Finger, Resist[]> resistMap) {
		mIndex = index;
		mChar = ch;

		mDevicesFromFinger = new BeMusicDevice[Finger.COUNT];
		mFingersFromDevice = new Finger[BeMusicDevice.COUNT];
		var devsL = new ArrayList<BeMusicDevice>();
		var devsR = new ArrayList<BeMusicDevice>();
		var fngsL = new ArrayList<Finger>();
		var fngsR = new ArrayList<Finger>();
		for (var e : fingerMap.entrySet()) {
			var device = e.getKey();
			var finger = e.getValue();
			var hand = finger.getHand();
			mDevicesFromFinger[finger.getIndex()] = device;
			mFingersFromDevice[device.getIndex()] = finger;
			mFingerCountPerHand[hand.getIndex()]++;
			if (hand.isLeft()) {
				devsL.add(device);
				fngsL.add(finger);
			} else {
				devsR.add(device);
				fngsR.add(finger);
			}
		}
		mDevicesLeftHand = devsL.toArray(BeMusicDevice[]::new);
		mDevicesRightHand = devsR.toArray(BeMusicDevice[]::new);
		mFingersLeftHand = fngsL.toArray(Finger[]::new);
		mFingersRightHand = fngsR.toArray(Finger[]::new);

		// 要素のソート処理
		Arrays.sort(mDevicesLeftHand, (l, r) -> Integer.compare(l.getIndex(), r.getIndex()));
		Arrays.sort(mDevicesRightHand, (l, r) -> Integer.compare(l.getIndex(), r.getIndex()));
		Arrays.sort(mFingersLeftHand, (l, r) -> Integer.compare(l.getIndex(), r.getIndex()));
		Arrays.sort(mFingersRightHand, (l, r) -> Integer.compare(l.getIndex(), r.getIndex()));

		// 抵抗情報マップを構築する
		var emptyResists = new Resist[0];
		mResistTable = new Resist[Finger.COUNT][];
		Arrays.fill(mResistTable, emptyResists);
		for (var e : resistMap.entrySet()) {
			mResistTable[e.getKey().getIndex()] = e.getValue();
		}
	}

	/**
	 * インデックス取得
	 * @return インデックス
	 */
	final int getIndex() {
		return mIndex;
	}

	/**
	 * 運指の1文字表現取得
	 * @return 運指の1文字表現
	 */
	final char getChar() {
		return mChar;
	}

	/**
	 * 指定指に対応する入力デバイス取得
	 * @param finger 指
	 * @return 対応する入力デバイス
	 */
	final BeMusicDevice getDevice(Finger finger) {
		return mDevicesFromFinger[finger.getIndex()];
	}

	/**
	 * 指定入力デバイスにアサインされた指取得
	 * @param device 入力デバイス
	 * @return アサインされた指
	 */
	final Finger getFinger(BeMusicDevice device) {
		return mFingersFromDevice[device.getIndex()];
	}

	/**
	 * 指定指が周囲指から受ける抵抗情報取得
	 * @param finger 指
	 * @return 周囲指からの抵抗情報
	 */
	final Resist[] getResists(Finger finger) {
		return mResistTable[finger.getIndex()];
	}

	/**
	 * 指定手の指が入力デバイスにアサインされた数取得
	 * @param hand 手
	 * @return 指が入力デバイスにアサインされた数
	 */
	final int getFingerCount(Hand hand) {
		return mFingerCountPerHand[hand.getIndex()];
	}

	/**
	 * 指定手の指のうち入力デバイスにアサインされた指取得
	 * @param hand 手
	 * @return 入力デバイスにアサインされた指一覧
	 */
	final Finger[] getFingers(Hand hand) {
		return hand.isLeft() ? mFingersLeftHand : mFingersRightHand;
	}

	/**
	 * 指定手が担当する入力デバイス取得
	 * @param hand 手
	 * @return 担当する入力デバイス一覧
	 */
	final BeMusicDevice[] getDevices(Hand hand) {
		return hand.isLeft() ? mDevicesLeftHand : mDevicesRightHand;
	}

	/**
	 * インデックスから運指取得
	 * @param index インデックス
	 * @return 運指
	 */
	static Fingering fromIndex(int index) {
		return TABLE[index];
	}

	/**
	 * 抵抗情報生成
	 * @param finger 指
	 * @param value 抵抗値
	 * @return 抵抗情報
	 */
	private static Resist r(Finger finger, double value) {
		return new Resist(finger, value);
	}

	/**
	 * 抵抗情報一覧生成
	 * @param resistArgs 抵抗情報一覧
	 * @return 抵抗情報一覧
	 */
	private static Resist[] resists(Resist...resistArgs) {
		return resistArgs;
	}

	/**
	 * 運指の文字列表現取得
	 * @return 運指の文字列表現
	 */
	@Override
	public String toString() {
		return toString(0);
	}

	/**
	 * 運指の文字列表現取得
	 * @param indent インデント数
	 * @return 運指の文字列表現
	 */
	public String toString(int indent) {
		var sb = new StringBuilder();
		var ind1 = String.join("", Stream.generate(() -> "  ").limit(indent).collect(Collectors.toList()));
		var ind2 = String.join("", Stream.generate(() -> "  ").limit(indent + 1).collect(Collectors.toList()));
		var ind3 = String.join("", Stream.generate(() -> "  ").limit(indent + 2).collect(Collectors.toList()));
		var ind4 = String.join("", Stream.generate(() -> "  ").limit(indent + 3).collect(Collectors.toList()));
		sb.append(ind1).append(name()).append(": {\n");
		sb.append(ind2).append("index: ").append(mIndex).append("\n");
		sb.append(ind2).append("char: ").append(mChar).append("\n");
		sb.append(ind2).append("assign: {\n");
		for (var h : Hand.values()) {
			sb.append(ind3).append(h.name()).append(": [\n");
			for (var d : getDevices(h)) {
				var f = getFinger(d);
				var rs = getResists(f);
				sb.append(ind4).append("{ ").append(d.name()).append(", ").append(f.name()).append(", [");
				for (var i = 0; i < rs.length; i++) {
					sb.append(i > 0 ? ", " : "").append(rs[i]);
				}
				sb.append("] }\n");
			}
			sb.append(ind3).append("]\n");
		}
		sb.append(ind2).append("}\n");
		sb.append(ind1).append("}");
		return sb.toString();
	}

	/**
	 * 全運指のデバッグ出力
	 */
	static void print() {
		Ds.debug("fingerings: {");
		//Stream.of(TABLE).forEach(f -> Ds.debug(f.toString(1)));
		Ds.debug(SP_DEFAULT.toString(1));
		Ds.debug(SP_SCRATCH.toString(1));
		Ds.debug("}");
	}
}
