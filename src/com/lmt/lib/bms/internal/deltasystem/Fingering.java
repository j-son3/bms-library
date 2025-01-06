package com.lmt.lib.bms.internal.deltasystem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

	/** ダブルプレー用片手：POWER値計算に使用する */
	DP_DEFAULT(
			'd',
			"4332110 ",
			"5667889 ",
			"4, 0:0.10, 1:0.10, 2:0.50, 3:1.30, 4:0.00",
			"3, 0:0.10, 1:0.20, 2:0.80, 3:1.70, 4:1.30",
			"2, 0:0.40, 1:0.50, 2:0.00, 3:0.80, 4:0.50",
			"1, 0:0.20, 1:0.70, 2:0.50, 3:0.20, 4:0.10",
			"0, 0:0.00, 1:0.20, 2:0.40, 3:0.10, 4:0.10",
			"5, 5:0.00, 6:0.20, 7:0.40, 8:0.10, 9:0.10",
			"6, 5:0.20, 6:0.70, 7:0.50, 8:0.20, 9:0.10",
			"7, 5:0.40, 6:0.50, 7:0.00, 8:0.80, 9:0.50",
			"8, 5:0.10, 6:0.20, 7:0.80, 8:1.70, 9:1.30",
			"9, 5:0.10, 6:0.10, 7:0.50, 8:1.30, 9:0.00"),

	/** ダブルプレー用スクラッチ：POWER値計算に使用する */
	DP_SCRATCH(
			's',
			"01021104",
			"56675659",
			"4, 0:1.80, 1:2.00, 2:2.50, 3:3.00, 4:0.00",
			"3, 0:0.00, 1:0.00, 2:0.00, 3:0.00, 4:3.00",
			"2, 0:0.50, 1:0.40, 2:0.00, 3:0.00, 4:2.50",
			"1, 0:1.90, 1:1.50, 2:0.40, 3:0.00, 4:2.00",
			"0, 0:0.00, 1:1.90, 2:0.50, 3:0.00, 4:1.80",
			"5, 5:0.00, 6:1.90, 7:0.50, 8:0.00, 9:1.80",
			"6, 5:1.90, 6:1.50, 7:0.40, 8:0.00, 9:2.00",
			"7, 5:0.50, 6:0.40, 7:0.00, 8:0.00, 9:2.50",
			"8, 5:0.00, 6:0.00, 7:0.00, 8:0.00, 9:3.00",
			"9, 5:1.80, 6:2.00, 7:2.50, 8:3.00, 9:0.00"),

	/** HOLDING算出用(DP)：12467をホームポジションとし、HOLDING向けに調整された抵抗値を定義する */
	DP_HOLDING(
			'h',
			"43321104",
			"56678899",
			"4, 0:0.16, 1:0.20, 2:0.29, 3:0.28, 4:0.50",
			"3, 0:0.11, 1:0.16, 2:0.30, 3:0.39, 4:0.28",
			"2, 0:0.05, 1:0.23, 2:0.00, 3:0.30, 4:0.29",
			"1, 0:0.19, 1:0.30, 2:0.23, 3:0.16, 4:0.20",
			"0, 0:0.00, 1:0.19, 2:0.05, 3:0.11, 4:0.16",
			"5, 5:0.00, 6:0.19, 7:0.05, 8:0.11, 9:0.16",
			"6, 5:0.19, 6:0.30, 7:0.23, 8:0.16, 9:0.20",
			"7, 5:0.05, 6:0.23, 7:0.00, 8:0.30, 9:0.29",
			"8, 5:0.11, 6:0.16, 7:0.30, 8:0.39, 9:0.28",
			"9, 5:0.16, 6:0.20, 7:0.29, 8:0.28, 9:0.50"),

	/** GIMMICKの地雷(DP用) */
	DP_MINE(
			'm',
			"43321104",
			"56678899",
			"4, 0:0.00, 1:0.03, 2:0.15, 3:0.25, 4:0.50",
			"3, 0:0.01, 1:0.02, 2:0.20, 3:0.50, 4:0.25",
			"2, 0:0.05, 1:0.20, 2:1.00, 3:0.20, 4:0.15",
			"1, 0:0.05, 1:0.50, 2:0.20, 3:0.02, 4:0.03",
			"0, 0:1.00, 1:0.05, 2:0.05, 3:0.01, 4:0.00",
			"5, 5:1.00, 6:0.05, 7:0.05, 8:0.01, 9:0.00",
			"6, 5:0.05, 6:0.50, 7:0.20, 8:0.02, 9:0.03",
			"7, 5:0.05, 6:0.20, 7:1.00, 8:0.20, 9:0.15",
			"8, 5:0.01, 6:0.02, 7:0.20, 8:0.50, 9:0.25",
			"9, 5:0.00, 6:0.03, 7:0.15, 8:0.25, 9:0.50"),
	;

	/** 運指の1文字表現 */
	private char mChar;
	/** 指と入力デバイス一覧のマッピングテーブル */
	private List<List<BeMusicDevice>> mDevicesMappedByFinger;
	/** 指と入力デバイスのマッピングテーブル(左手のみ) */
	private List<BeMusicDevice> mDevicesLeftHand;
	/** 指と入力デバイスのマッピングテーブル(右手のみ) */
	private List<BeMusicDevice> mDevicesRightHand;
	/** 入力デバイスと指のマッピングテーブル */
	private Finger[] mFingersFromDevice;
	/** 指同士の抵抗データテーブル */
	private Resist[][] mResistTable;

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
		mFingersFromDevice = new Finger[BeMusicDevice.COUNT];
		var devsMapFng = new ArrayList<List<BeMusicDevice>>(Collections.nCopies(Finger.COUNT, null));
		var devsL = new ArrayList<BeMusicDevice>();
		var devsR = new ArrayList<BeMusicDevice>();
		var fngsL = new ArrayList<Finger>();
		var fngsR = new ArrayList<Finger>();
		for (var e : fingerMap.entrySet()) {
			var device = e.getKey();
			var finger = e.getValue();
			var hand = finger.getHand();
			mFingersFromDevice[device.getIndex()] = finger;

			// 担当指ごとの入力デバイスリストを編集する
			var devsFng = devsMapFng.get(finger.getIndex());
			if (devsFng == null) {
				devsFng = new ArrayList<>();
				devsMapFng.set(finger.getIndex(), devsFng);
			}
			devsFng.add(device);

			// 手ごとのリストを編集する
			if (hand.isLeft()) {
				devsL.add(device);
				fngsL.add(finger);
			} else {
				devsR.add(device);
				fngsR.add(finger);
			}
		}
		mDevicesMappedByFinger = devsMapFng.stream()
				.map(d -> Optional.ofNullable(d).orElse(Collections.emptyList())).collect(Collectors.toList());
		devsL.sort((l, r) -> Integer.compare(l.getIndex(), r.getIndex()));
		devsR.sort((l, r) -> Integer.compare(l.getIndex(), r.getIndex()));
		mDevicesLeftHand = Collections.unmodifiableList(devsL);
		mDevicesRightHand = Collections.unmodifiableList(devsR);

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
	 * 指定手が担当する入力デバイス取得
	 * @param hand 手
	 * @return 担当する入力デバイス一覧
	 */
	final List<BeMusicDevice> getDevices(Hand hand) {
		return hand.isLeft() ? mDevicesLeftHand : mDevicesRightHand;
	}

	/**
	 * 指定指が担当する入力デバイス取得
	 * @param finger 指
	 * @return 担当する入力デバイス一覧
	 */
	final List<BeMusicDevice> getDevices(Finger finger) {
		return mDevicesMappedByFinger.get(finger.getIndex());
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
		Stream.of(values()).forEach(f -> Ds.debug(f.toString(1)));
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
		final var REX_ALL = String.format("^(\\d)%s%s?%s?%s?%s?$", REX_RESIST, REX_RESIST, REX_RESIST, REX_RESIST, REX_RESIST);
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
