package com.lmt.lib.bms.internal.deltasystem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.lmt.lib.bms.bemusic.BeMusicDevice;
import com.lmt.lib.bms.bemusic.BeMusicLane;

/**
 * 運指を表現する列挙型
 *
 * <p>この列挙型は実際のプレーで一般的となった運指をカタログ化し、その運指の特性をパッケージする。
 * 主な目的としては、当該運指における指同士が受ける動作の抵抗を管理することにある。運指による指の抵抗値を評価点に
 * 反映させ、可能な限り実際のプレーに近い評価を出力できるようにすることを最終目標とする。</p>
 */
enum Fingering {
	/** 1048式：POWER値計算に使用する */
	SP_DEFAULT(
			'D',
			"0216579 ",
			"        ",
			"0, 1:0.70, 2:0.30",
			"1, 0:0.30, 2:0.70",
			"2, 0:0.30, 1:0.70",
			"5, 6:0.80, 7:0.20, 9:0.30",
			"6, 5:0.30, 7:0.80, 9:0.20",
			"7, 5:0.20, 6:0.80, 9:0.50",
			"9, 5:0.20, 6:0.10, 7:0.80"),

	/** 3・5半固定：POWER値計算に使用する */
	SP_SCRATCH(
			'S',
			"01567894",
			"        ",
			"0, 1:0.30, 4:0.70",
			"1, 0:0.20, 4:0.80",
			"4, 0:0.20, 1:0.80",
			"5, 6:0.20, 7:0.70, 9:0.10",
			"6, 5:0.20, 7:0.70, 9:0.10",
			"7, 5:0.20, 6:0.40, 8:0.90, 9:0.30",
			"8, 5:0.20, 6:0.20, 7:0.90, 9:0.50",
			"9, 5:0.10, 6:0.20, 7:0.60, 8:0.90"),

	/** HOLDING算出用：1048式完全固定として定義する */
	SP_HOLDING(
			'H',
			"02165794",
			"        ",
			"4, 0:0.20, 1:0.40, 2:0.30",
			"0, 1:0.25, 2:0.10, 4:0.50",
			"2, 0:0.10, 1:0.30, 4:0.50",
			"1, 0:0.30, 2:0.20, 4:0.50",
			"6, 5:0.40, 7:0.25, 9:0.15",
			"5, 6:0.40, 7:0.10, 9:0.20",
			"7, 6:0.25, 5:0.10, 9:0.30",
			"9, 6:0.30, 5:0.15, 7:0.30"),

	/** GIMMICKの地雷用 */
	SP_MINE(
			'M',
			"02165794",
			"        ",
			"4, 4:1.00",
			"0, 0:1.00, 2:0.10, 1:0.10",
			"2, 2:1.00, 0:0.10, 1:0.10",
			"1, 1:1.00, 0:0.10, 2:0.10",
			"6, 6:1.00, 5:0.10, 7:0.20",
			"5, 5:1.00, 6:0.10, 7:0.10, 9:0.05",
			"7, 7:1.00, 6:0.20, 5:0.10, 9:0.20",
			"9, 9:1.00, 6:0.05, 5:0.10, 7:0.20"),

	/** 左片手 */
	LEFT_HAND('L', Collections.emptyMap(), Collections.emptyMap()),

	/** DP左レーンスクラッチ */
	LEFT_SCRATCH('R', Collections.emptyMap(), Collections.emptyMap()),

	/** 右片手 */
	RIGHT_HAND('<', Collections.emptyMap(), Collections.emptyMap()),

	/** DP右レーンスクラッチ */
	RIGHT_SCRATCH('>', Collections.emptyMap(), Collections.emptyMap());

	/** インデックスによる運指のテーブル */
	private static final Fingering[] TABLE = {
			SP_DEFAULT, SP_SCRATCH, SP_HOLDING, LEFT_HAND, LEFT_SCRATCH, RIGHT_HAND, RIGHT_SCRATCH,
	};

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
	 * @param ch 運指の1文字表現
	 * @param primary 主レーンの入力デバイスと指の定義
	 * @param secondary 副レーンの入力デバイスと指の定義
	 * @param resists 動作指と抵抗値定義リスト
	 */
	private Fingering(char ch, String primary, String secondary, String...resists) {
		 var fingerMap = new HashMap<BeMusicDevice, Finger>();
		 var resistMap = new HashMap<Finger, Resist[]>();
		 parseDeviceFinger(fingerMap, BeMusicDevice.getDevices(BeMusicLane.PRIMARY), primary);
		 parseDeviceFinger(fingerMap, BeMusicDevice.getDevices(BeMusicLane.SECONDARY), secondary);
		 Stream.of(resists).forEach(r -> parseResist(resistMap, r));
		 initialize(ch, fingerMap, resistMap);
	}

	/**
	 * コンストラクタ
	 * @param ch 運指の1文字表現
	 * @param fingerMap 入力デバイスへの指のマッピング情報
	 * @param resistMap 周囲指からの抵抗情報のマッピング
	 */
	private Fingering(char ch, Map<BeMusicDevice, Finger> fingerMap, Map<Finger, Resist[]> resistMap) {
		initialize(ch, fingerMap, resistMap);
	}

	/**
	 * 初期化処理
	 * @param ch 運指の1文字表現
	 * @param fingerMap 入力デバイスへの指のマッピング情報
	 * @param resistMap 周囲指からの抵抗情報のマッピング
	 */
	private void initialize(char ch, Map<BeMusicDevice, Finger> fingerMap, Map<Finger, Resist[]> resistMap) {
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

	/**
	 * 入力デバイスと指の定義解析
	 * @param map 解析結果の追記先マップ
	 * @param devs 入力デバイスリスト
	 * @param def 定義内容
	 * @exception IllegalArgumentException 定義内容の構文エラー
	 */
	private static void parseDeviceFinger(Map<BeMusicDevice, Finger> map, List<BeMusicDevice> devs, String def) {
		try {
			// 定義は8文字でなければならない1
			if (def.length() != 8) {
				throw new Exception();
			}

			// 解析結果を読み取り、入力デバイスと指のマップを構成する
			for (var i = 0; i < 8; i++) {
				var c = def.charAt(i);
				if (c == ' ') {
					// 半角空白の場合はアサインしない
					// Do nothing
				} else if ((c >= '0') && (c <= '9')) {
					// 入力デバイスに指をアサインする
					map.put(devs.get(i), Finger.fromIndex(c - '0'));
				} else {
					// 認識不可能な文字
					throw new Exception();
				}
			}
		} catch (Exception e) {
			// 不正な定義内容を検出した
			var msg = String.format("Device-Finger format error: '%s'", def);
			throw new IllegalArgumentException(msg);
		}
	}

	/**
	 * 動作指と抵抗値定義解析
	 * @param map 解析結果の追記先マップ
	 * @param def 定義内容
	 * @exception IllegalArgumentException 定義内容の構文エラー
	 */
	private static void parseResist(Map<Finger, Resist[]> map, String def) {
		// 定義内容を解析する
		final var REX_RESIST = "( *, *(\\d):(\\d+\\.\\d+))";
		final var REX_ALL = String.format("^(\\d)%s%s?%s?%s?$", REX_RESIST, REX_RESIST, REX_RESIST, REX_RESIST);
		var matcher = Pattern.compile(REX_ALL).matcher(def);
		if (!matcher.matches()) {
			var msg = String.format("Resist format error: '%s'", def);
			throw new IllegalArgumentException(msg);
		}

		// 解析結果を読み取り、動作指と抵抗値定義のマップを構成する
		var actionFinger = Finger.fromIndex(Integer.parseInt(matcher.group(1)));
		var resistCount = 4;
		var resists = new ArrayList<Resist>(resistCount);
		for (var i = 0; i < resistCount; i++) {
			var baseGroupIndex = 2 + (i * 3);
			if (matcher.group(baseGroupIndex) != null) {
				var resistFinger = Finger.fromIndex(Integer.parseInt(matcher.group(baseGroupIndex + 1)));
				var resistValue = Double.parseDouble(matcher.group(baseGroupIndex + 2));
				resists.add(new Resist(resistFinger, resistValue));
			}
		}
		map.put(actionFinger, resists.toArray(Resist[]::new));
	}
}
