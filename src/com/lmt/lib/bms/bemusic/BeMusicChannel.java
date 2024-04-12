package com.lmt.lib.bms.bemusic;

import com.lmt.lib.bms.BmsChannel;
import com.lmt.lib.bms.BmsType;

/**
 * Be-MusicのBMS仕様に含まれるチャンネルを表します。
 *
 * <p>当クラスは、Be-MusicのBMS仕様に含まれるチャンネルに関する定義のプレースホルダの役割を果たします。
 * そのため、インスタンスを生成することを想定していません。</p>
 */
public class BeMusicChannel {
	/** チャンネル番号：BGM */
	public static final int NUM_BGM = ch("01");
	/** チャンネル番号：小節長変更 */
	public static final int NUM_LENGTH = ch("02");
	/** チャンネル番号：BPM変更(旧式) */
	public static final int NUM_BPM_LEGACY = ch("03");
	/** チャンネル番号：BGA */
	public static final int NUM_BGA = ch("04");
	/** チャンネル番号：Extended Object */
	public static final int NUM_EXT_OBJ = ch("05");
	/** チャンネル番号：BGA(ミスレイヤー) */
	public static final int NUM_BGA_MISS = ch("06");
	/** チャンネル番号：BGA LAYER */
	public static final int NUM_BGA_LAYER = ch("07");
	/** チャンネル番号：BPM変更 */
	public static final int NUM_BPM = ch("08");
	/** チャンネル番号：譜面停止 */
	public static final int NUM_STOP = ch("09");
	/** チャンネル番号：BGA LAYER2 */
	public static final int NUM_BGA_LAYER2 = ch("0A");
	/** チャンネル番号：BGA不透明度 */
	public static final int NUM_BGA_BASE_OPACITY = ch("0B");
	/** チャンネル番号：BGA LAYER不透明度 */
	public static final int NUM_BGA_LAYER_OPACITY = ch("0C");
	/** チャンネル番号：BGA LAYER2不透明度 */
	public static final int NUM_BGA_LAYER2_OPACITY = ch("0D");
	/** チャンネル番号：BGA(ミスレイヤー)不透明度 */
	public static final int NUM_BGA_MISS_OPACITY = ch("0E");
	/** チャンネル番号：BGM音量 */
	public static final int NUM_BGM_VOLUME = ch("97");
	/** チャンネル番号：キー音音量 */
	public static final int NUM_KEY_VOLUME = ch("98");
	/** チャンネル番号：テキスト表示 */
	public static final int NUM_TEXT = ch("99");
	/** チャンネル番号：判定ランク変更 */
	public static final int NUM_JUDGE = ch("A0");
	/** チャンネル番号：BGA aRGB */
	public static final int NUM_BGA_BASE_ARGB = ch("A1");
	/** チャンネル番号：BGA LAYER aRGB */
	public static final int NUM_BGA_LAYER_ARGB = ch("A2");
	/** チャンネル番号：BGA LAYER2 aRGB */
	public static final int NUM_BGA_LAYER2_ARGB = ch("A3");
	/** チャンネル番号：BGA(ミスレイヤー) aRGB */
	public static final int NUM_BGA_MISS_ARGB = ch("A4");
	/** チャンネル番号：キーバインドLAYERアニメーション */
	public static final int NUM_BGA_KEYBOUND = ch("A5");
	/** チャンネル番号：レイオプション変更 */
	public static final int NUM_OPTION = ch("A6");
	/** チャンネル番号：スクロール速度変更 */
	public static final int NUM_SCROLL = ch("SC");
	/** チャンネル番号：1P可視オブジェ1 */
	public static final int NUM_VISIBLE_1P_01 = ch("11");
	/** チャンネル番号：1P可視オブジェ2 */
	public static final int NUM_VISIBLE_1P_02 = ch("12");
	/** チャンネル番号：1P可視オブジェ3 */
	public static final int NUM_VISIBLE_1P_03 = ch("13");
	/** チャンネル番号：1P可視オブジェ4 */
	public static final int NUM_VISIBLE_1P_04 = ch("14");
	/** チャンネル番号：1P可視オブジェ5 */
	public static final int NUM_VISIBLE_1P_05 = ch("15");
	/** チャンネル番号：1P可視オブジェ6 */
	public static final int NUM_VISIBLE_1P_06 = ch("16");
	/** チャンネル番号：1P可視オブジェ7 */
	public static final int NUM_VISIBLE_1P_07 = ch("17");
	/** チャンネル番号：1P可視オブジェ8 */
	public static final int NUM_VISIBLE_1P_08 = ch("18");
	/** チャンネル番号：1P可視オブジェ9 */
	public static final int NUM_VISIBLE_1P_09 = ch("19");
	/** チャンネル番号：1P可視オブジェ10 */
	public static final int NUM_VISIBLE_1P_10 = ch("1A");
	/** チャンネル番号：1P可視オブジェ11 */
	public static final int NUM_VISIBLE_1P_11 = ch("1B");
	/** チャンネル番号：1P可視オブジェ12 */
	public static final int NUM_VISIBLE_1P_12 = ch("1C");
	/** チャンネル番号：1P可視オブジェ13 */
	public static final int NUM_VISIBLE_1P_13 = ch("1D");
	/** チャンネル番号：1P可視オブジェ14 */
	public static final int NUM_VISIBLE_1P_14 = ch("1E");
	/** チャンネル番号：1P可視オブジェ15 */
	public static final int NUM_VISIBLE_1P_15 = ch("1F");
	/** チャンネル番号：1P可視オブジェ16 */
	public static final int NUM_VISIBLE_1P_16 = ch("1G");
	/** チャンネル番号：1P可視オブジェ17 */
	public static final int NUM_VISIBLE_1P_17 = ch("1H");
	/** チャンネル番号：1P可視オブジェ18 */
	public static final int NUM_VISIBLE_1P_18 = ch("1I");
	/** チャンネル番号：1P可視オブジェ19 */
	public static final int NUM_VISIBLE_1P_19 = ch("1J");
	/** チャンネル番号：1P可視オブジェ20 */
	public static final int NUM_VISIBLE_1P_20 = ch("1K");
	/** チャンネル番号：1P可視オブジェ21 */
	public static final int NUM_VISIBLE_1P_21 = ch("1L");
	/** チャンネル番号：1P可視オブジェ22 */
	public static final int NUM_VISIBLE_1P_22 = ch("1M");
	/** チャンネル番号：1P可視オブジェ23 */
	public static final int NUM_VISIBLE_1P_23 = ch("1N");
	/** チャンネル番号：1P可視オブジェ24 */
	public static final int NUM_VISIBLE_1P_24 = ch("1O");
	/** チャンネル番号：1P可視オブジェ25 */
	public static final int NUM_VISIBLE_1P_25 = ch("1P");
	/** チャンネル番号：1P可視オブジェ26 */
	public static final int NUM_VISIBLE_1P_26 = ch("1Q");
	/** チャンネル番号：1P可視オブジェ27 */
	public static final int NUM_VISIBLE_1P_27 = ch("1R");
	/** チャンネル番号：1P可視オブジェ28 */
	public static final int NUM_VISIBLE_1P_28 = ch("1S");
	/** チャンネル番号：1P可視オブジェ29 */
	public static final int NUM_VISIBLE_1P_29 = ch("1T");
	/** チャンネル番号：1P可視オブジェ30 */
	public static final int NUM_VISIBLE_1P_30 = ch("1U");
	/** チャンネル番号：1P可視オブジェ31 */
	public static final int NUM_VISIBLE_1P_31 = ch("1V");
	/** チャンネル番号：1P可視オブジェ32 */
	public static final int NUM_VISIBLE_1P_32 = ch("1W");
	/** チャンネル番号：1P可視オブジェ33 */
	public static final int NUM_VISIBLE_1P_33 = ch("1X");
	/** チャンネル番号：1P可視オブジェ34 */
	public static final int NUM_VISIBLE_1P_34 = ch("1Y");
	/** チャンネル番号：1P可視オブジェ35 */
	public static final int NUM_VISIBLE_1P_35 = ch("1Z");
	/** チャンネル番号：2P可視オブジェ1 */
	public static final int NUM_VISIBLE_2P_01 = ch("21");
	/** チャンネル番号：2P可視オブジェ2 */
	public static final int NUM_VISIBLE_2P_02 = ch("22");
	/** チャンネル番号：2P可視オブジェ3 */
	public static final int NUM_VISIBLE_2P_03 = ch("23");
	/** チャンネル番号：2P可視オブジェ4 */
	public static final int NUM_VISIBLE_2P_04 = ch("24");
	/** チャンネル番号：2P可視オブジェ5 */
	public static final int NUM_VISIBLE_2P_05 = ch("25");
	/** チャンネル番号：2P可視オブジェ6 */
	public static final int NUM_VISIBLE_2P_06 = ch("26");
	/** チャンネル番号：2P可視オブジェ7 */
	public static final int NUM_VISIBLE_2P_07 = ch("27");
	/** チャンネル番号：2P可視オブジェ8 */
	public static final int NUM_VISIBLE_2P_08 = ch("28");
	/** チャンネル番号：2P可視オブジェ9 */
	public static final int NUM_VISIBLE_2P_09 = ch("29");
	/** チャンネル番号：2P可視オブジェ10 */
	public static final int NUM_VISIBLE_2P_10 = ch("2A");
	/** チャンネル番号：2P可視オブジェ11 */
	public static final int NUM_VISIBLE_2P_11 = ch("2B");
	/** チャンネル番号：2P可視オブジェ12 */
	public static final int NUM_VISIBLE_2P_12 = ch("2C");
	/** チャンネル番号：2P可視オブジェ13 */
	public static final int NUM_VISIBLE_2P_13 = ch("2D");
	/** チャンネル番号：2P可視オブジェ14 */
	public static final int NUM_VISIBLE_2P_14 = ch("2E");
	/** チャンネル番号：2P可視オブジェ15 */
	public static final int NUM_VISIBLE_2P_15 = ch("2F");
	/** チャンネル番号：2P可視オブジェ16 */
	public static final int NUM_VISIBLE_2P_16 = ch("2G");
	/** チャンネル番号：2P可視オブジェ17 */
	public static final int NUM_VISIBLE_2P_17 = ch("2H");
	/** チャンネル番号：2P可視オブジェ18 */
	public static final int NUM_VISIBLE_2P_18 = ch("2I");
	/** チャンネル番号：2P可視オブジェ19 */
	public static final int NUM_VISIBLE_2P_19 = ch("2J");
	/** チャンネル番号：2P可視オブジェ20 */
	public static final int NUM_VISIBLE_2P_20 = ch("2K");
	/** チャンネル番号：2P可視オブジェ21 */
	public static final int NUM_VISIBLE_2P_21 = ch("2L");
	/** チャンネル番号：2P可視オブジェ22 */
	public static final int NUM_VISIBLE_2P_22 = ch("2M");
	/** チャンネル番号：2P可視オブジェ23 */
	public static final int NUM_VISIBLE_2P_23 = ch("2N");
	/** チャンネル番号：2P可視オブジェ24 */
	public static final int NUM_VISIBLE_2P_24 = ch("2O");
	/** チャンネル番号：2P可視オブジェ25 */
	public static final int NUM_VISIBLE_2P_25 = ch("2P");
	/** チャンネル番号：2P可視オブジェ26 */
	public static final int NUM_VISIBLE_2P_26 = ch("2Q");
	/** チャンネル番号：2P可視オブジェ27 */
	public static final int NUM_VISIBLE_2P_27 = ch("2R");
	/** チャンネル番号：2P可視オブジェ28 */
	public static final int NUM_VISIBLE_2P_28 = ch("2S");
	/** チャンネル番号：2P可視オブジェ29 */
	public static final int NUM_VISIBLE_2P_29 = ch("2T");
	/** チャンネル番号：2P可視オブジェ30 */
	public static final int NUM_VISIBLE_2P_30 = ch("2U");
	/** チャンネル番号：2P可視オブジェ31 */
	public static final int NUM_VISIBLE_2P_31 = ch("2V");
	/** チャンネル番号：2P可視オブジェ32 */
	public static final int NUM_VISIBLE_2P_32 = ch("2W");
	/** チャンネル番号：2P可視オブジェ33 */
	public static final int NUM_VISIBLE_2P_33 = ch("2X");
	/** チャンネル番号：2P可視オブジェ34 */
	public static final int NUM_VISIBLE_2P_34 = ch("2Y");
	/** チャンネル番号：2P可視オブジェ35 */
	public static final int NUM_VISIBLE_2P_35 = ch("2Z");
	/** チャンネル番号：1P不可視オブジェ1 */
	public static final int NUM_INVISIBLE_1P_01 = ch("31");
	/** チャンネル番号：1P不可視オブジェ2 */
	public static final int NUM_INVISIBLE_1P_02 = ch("32");
	/** チャンネル番号：1P不可視オブジェ3 */
	public static final int NUM_INVISIBLE_1P_03 = ch("33");
	/** チャンネル番号：1P不可視オブジェ4 */
	public static final int NUM_INVISIBLE_1P_04 = ch("34");
	/** チャンネル番号：1P不可視オブジェ5 */
	public static final int NUM_INVISIBLE_1P_05 = ch("35");
	/** チャンネル番号：1P不可視オブジェ6 */
	public static final int NUM_INVISIBLE_1P_06 = ch("36");
	/** チャンネル番号：1P不可視オブジェ7 */
	public static final int NUM_INVISIBLE_1P_07 = ch("37");
	/** チャンネル番号：1P不可視オブジェ8 */
	public static final int NUM_INVISIBLE_1P_08 = ch("38");
	/** チャンネル番号：1P不可視オブジェ9 */
	public static final int NUM_INVISIBLE_1P_09 = ch("39");
	/** チャンネル番号：1P不可視オブジェ10 */
	public static final int NUM_INVISIBLE_1P_10 = ch("3A");
	/** チャンネル番号：1P不可視オブジェ11 */
	public static final int NUM_INVISIBLE_1P_11 = ch("3B");
	/** チャンネル番号：1P不可視オブジェ12 */
	public static final int NUM_INVISIBLE_1P_12 = ch("3C");
	/** チャンネル番号：1P不可視オブジェ13 */
	public static final int NUM_INVISIBLE_1P_13 = ch("3D");
	/** チャンネル番号：1P不可視オブジェ14 */
	public static final int NUM_INVISIBLE_1P_14 = ch("3E");
	/** チャンネル番号：1P不可視オブジェ15 */
	public static final int NUM_INVISIBLE_1P_15 = ch("3F");
	/** チャンネル番号：1P不可視オブジェ16 */
	public static final int NUM_INVISIBLE_1P_16 = ch("3G");
	/** チャンネル番号：1P不可視オブジェ17 */
	public static final int NUM_INVISIBLE_1P_17 = ch("3H");
	/** チャンネル番号：1P不可視オブジェ18 */
	public static final int NUM_INVISIBLE_1P_18 = ch("3I");
	/** チャンネル番号：1P不可視オブジェ19 */
	public static final int NUM_INVISIBLE_1P_19 = ch("3J");
	/** チャンネル番号：1P不可視オブジェ20 */
	public static final int NUM_INVISIBLE_1P_20 = ch("3K");
	/** チャンネル番号：1P不可視オブジェ21 */
	public static final int NUM_INVISIBLE_1P_21 = ch("3L");
	/** チャンネル番号：1P不可視オブジェ22 */
	public static final int NUM_INVISIBLE_1P_22 = ch("3M");
	/** チャンネル番号：1P不可視オブジェ23 */
	public static final int NUM_INVISIBLE_1P_23 = ch("3N");
	/** チャンネル番号：1P不可視オブジェ24 */
	public static final int NUM_INVISIBLE_1P_24 = ch("3O");
	/** チャンネル番号：1P不可視オブジェ25 */
	public static final int NUM_INVISIBLE_1P_25 = ch("3P");
	/** チャンネル番号：1P不可視オブジェ26 */
	public static final int NUM_INVISIBLE_1P_26 = ch("3Q");
	/** チャンネル番号：1P不可視オブジェ27 */
	public static final int NUM_INVISIBLE_1P_27 = ch("3R");
	/** チャンネル番号：1P不可視オブジェ28 */
	public static final int NUM_INVISIBLE_1P_28 = ch("3S");
	/** チャンネル番号：1P不可視オブジェ29 */
	public static final int NUM_INVISIBLE_1P_29 = ch("3T");
	/** チャンネル番号：1P不可視オブジェ30 */
	public static final int NUM_INVISIBLE_1P_30 = ch("3U");
	/** チャンネル番号：1P不可視オブジェ31 */
	public static final int NUM_INVISIBLE_1P_31 = ch("3V");
	/** チャンネル番号：1P不可視オブジェ32 */
	public static final int NUM_INVISIBLE_1P_32 = ch("3W");
	/** チャンネル番号：1P不可視オブジェ33 */
	public static final int NUM_INVISIBLE_1P_33 = ch("3X");
	/** チャンネル番号：1P不可視オブジェ34 */
	public static final int NUM_INVISIBLE_1P_34 = ch("3Y");
	/** チャンネル番号：1P不可視オブジェ35 */
	public static final int NUM_INVISIBLE_1P_35 = ch("3Z");
	/** チャンネル番号：2P不可視オブジェ1 */
	public static final int NUM_INVISIBLE_2P_01 = ch("41");
	/** チャンネル番号：2P不可視オブジェ2 */
	public static final int NUM_INVISIBLE_2P_02 = ch("42");
	/** チャンネル番号：2P不可視オブジェ3 */
	public static final int NUM_INVISIBLE_2P_03 = ch("43");
	/** チャンネル番号：2P不可視オブジェ4 */
	public static final int NUM_INVISIBLE_2P_04 = ch("44");
	/** チャンネル番号：2P不可視オブジェ5 */
	public static final int NUM_INVISIBLE_2P_05 = ch("45");
	/** チャンネル番号：2P不可視オブジェ6 */
	public static final int NUM_INVISIBLE_2P_06 = ch("46");
	/** チャンネル番号：2P不可視オブジェ7 */
	public static final int NUM_INVISIBLE_2P_07 = ch("47");
	/** チャンネル番号：2P不可視オブジェ8 */
	public static final int NUM_INVISIBLE_2P_08 = ch("48");
	/** チャンネル番号：2P不可視オブジェ9 */
	public static final int NUM_INVISIBLE_2P_09 = ch("49");
	/** チャンネル番号：2P不可視オブジェ10 */
	public static final int NUM_INVISIBLE_2P_10 = ch("4A");
	/** チャンネル番号：2P不可視オブジェ11 */
	public static final int NUM_INVISIBLE_2P_11 = ch("4B");
	/** チャンネル番号：2P不可視オブジェ12 */
	public static final int NUM_INVISIBLE_2P_12 = ch("4C");
	/** チャンネル番号：2P不可視オブジェ13 */
	public static final int NUM_INVISIBLE_2P_13 = ch("4D");
	/** チャンネル番号：2P不可視オブジェ14 */
	public static final int NUM_INVISIBLE_2P_14 = ch("4E");
	/** チャンネル番号：2P不可視オブジェ15 */
	public static final int NUM_INVISIBLE_2P_15 = ch("4F");
	/** チャンネル番号：2P不可視オブジェ16 */
	public static final int NUM_INVISIBLE_2P_16 = ch("4G");
	/** チャンネル番号：2P不可視オブジェ17 */
	public static final int NUM_INVISIBLE_2P_17 = ch("4H");
	/** チャンネル番号：2P不可視オブジェ18 */
	public static final int NUM_INVISIBLE_2P_18 = ch("4I");
	/** チャンネル番号：2P不可視オブジェ19 */
	public static final int NUM_INVISIBLE_2P_19 = ch("4J");
	/** チャンネル番号：2P不可視オブジェ20 */
	public static final int NUM_INVISIBLE_2P_20 = ch("4K");
	/** チャンネル番号：2P不可視オブジェ21 */
	public static final int NUM_INVISIBLE_2P_21 = ch("4L");
	/** チャンネル番号：2P不可視オブジェ22 */
	public static final int NUM_INVISIBLE_2P_22 = ch("4M");
	/** チャンネル番号：2P不可視オブジェ23 */
	public static final int NUM_INVISIBLE_2P_23 = ch("4N");
	/** チャンネル番号：2P不可視オブジェ24 */
	public static final int NUM_INVISIBLE_2P_24 = ch("4O");
	/** チャンネル番号：2P不可視オブジェ25 */
	public static final int NUM_INVISIBLE_2P_25 = ch("4P");
	/** チャンネル番号：2P不可視オブジェ26 */
	public static final int NUM_INVISIBLE_2P_26 = ch("4Q");
	/** チャンネル番号：2P不可視オブジェ27 */
	public static final int NUM_INVISIBLE_2P_27 = ch("4R");
	/** チャンネル番号：2P不可視オブジェ28 */
	public static final int NUM_INVISIBLE_2P_28 = ch("4S");
	/** チャンネル番号：2P不可視オブジェ29 */
	public static final int NUM_INVISIBLE_2P_29 = ch("4T");
	/** チャンネル番号：2P不可視オブジェ30 */
	public static final int NUM_INVISIBLE_2P_30 = ch("4U");
	/** チャンネル番号：2P不可視オブジェ31 */
	public static final int NUM_INVISIBLE_2P_31 = ch("4V");
	/** チャンネル番号：2P不可視オブジェ32 */
	public static final int NUM_INVISIBLE_2P_32 = ch("4W");
	/** チャンネル番号：2P不可視オブジェ33 */
	public static final int NUM_INVISIBLE_2P_33 = ch("4X");
	/** チャンネル番号：2P不可視オブジェ34 */
	public static final int NUM_INVISIBLE_2P_34 = ch("4Y");
	/** チャンネル番号：2P不可視オブジェ35 */
	public static final int NUM_INVISIBLE_2P_35 = ch("4Z");
	/** チャンネル番号：1Pロングノートオブジェ1 */
	public static final int NUM_LONG_1P_01 = ch("51");
	/** チャンネル番号：1Pロングノートオブジェ2 */
	public static final int NUM_LONG_1P_02 = ch("52");
	/** チャンネル番号：1Pロングノートオブジェ3 */
	public static final int NUM_LONG_1P_03 = ch("53");
	/** チャンネル番号：1Pロングノートオブジェ4 */
	public static final int NUM_LONG_1P_04 = ch("54");
	/** チャンネル番号：1Pロングノートオブジェ5 */
	public static final int NUM_LONG_1P_05 = ch("55");
	/** チャンネル番号：1Pロングノートオブジェ6 */
	public static final int NUM_LONG_1P_06 = ch("56");
	/** チャンネル番号：1Pロングノートオブジェ7 */
	public static final int NUM_LONG_1P_07 = ch("57");
	/** チャンネル番号：1Pロングノートオブジェ8 */
	public static final int NUM_LONG_1P_08 = ch("58");
	/** チャンネル番号：1Pロングノートオブジェ9 */
	public static final int NUM_LONG_1P_09 = ch("59");
	/** チャンネル番号：1Pロングノートオブジェ10 */
	public static final int NUM_LONG_1P_10 = ch("5A");
	/** チャンネル番号：1Pロングノートオブジェ11 */
	public static final int NUM_LONG_1P_11 = ch("5B");
	/** チャンネル番号：1Pロングノートオブジェ12 */
	public static final int NUM_LONG_1P_12 = ch("5C");
	/** チャンネル番号：1Pロングノートオブジェ13 */
	public static final int NUM_LONG_1P_13 = ch("5D");
	/** チャンネル番号：1Pロングノートオブジェ14 */
	public static final int NUM_LONG_1P_14 = ch("5E");
	/** チャンネル番号：1Pロングノートオブジェ15 */
	public static final int NUM_LONG_1P_15 = ch("5F");
	/** チャンネル番号：1Pロングノートオブジェ16 */
	public static final int NUM_LONG_1P_16 = ch("5G");
	/** チャンネル番号：1Pロングノートオブジェ17 */
	public static final int NUM_LONG_1P_17 = ch("5H");
	/** チャンネル番号：1Pロングノートオブジェ18 */
	public static final int NUM_LONG_1P_18 = ch("5I");
	/** チャンネル番号：1Pロングノートオブジェ19 */
	public static final int NUM_LONG_1P_19 = ch("5J");
	/** チャンネル番号：1Pロングノートオブジェ20 */
	public static final int NUM_LONG_1P_20 = ch("5K");
	/** チャンネル番号：1Pロングノートオブジェ21 */
	public static final int NUM_LONG_1P_21 = ch("5L");
	/** チャンネル番号：1Pロングノートオブジェ22 */
	public static final int NUM_LONG_1P_22 = ch("5M");
	/** チャンネル番号：1Pロングノートオブジェ23 */
	public static final int NUM_LONG_1P_23 = ch("5N");
	/** チャンネル番号：1Pロングノートオブジェ24 */
	public static final int NUM_LONG_1P_24 = ch("5O");
	/** チャンネル番号：1Pロングノートオブジェ25 */
	public static final int NUM_LONG_1P_25 = ch("5P");
	/** チャンネル番号：1Pロングノートオブジェ26 */
	public static final int NUM_LONG_1P_26 = ch("5Q");
	/** チャンネル番号：1Pロングノートオブジェ27 */
	public static final int NUM_LONG_1P_27 = ch("5R");
	/** チャンネル番号：1Pロングノートオブジェ28 */
	public static final int NUM_LONG_1P_28 = ch("5S");
	/** チャンネル番号：1Pロングノートオブジェ29 */
	public static final int NUM_LONG_1P_29 = ch("5T");
	/** チャンネル番号：1Pロングノートオブジェ30 */
	public static final int NUM_LONG_1P_30 = ch("5U");
	/** チャンネル番号：1Pロングノートオブジェ31 */
	public static final int NUM_LONG_1P_31 = ch("5V");
	/** チャンネル番号：1Pロングノートオブジェ32 */
	public static final int NUM_LONG_1P_32 = ch("5W");
	/** チャンネル番号：1Pロングノートオブジェ33 */
	public static final int NUM_LONG_1P_33 = ch("5X");
	/** チャンネル番号：1Pロングノートオブジェ34 */
	public static final int NUM_LONG_1P_34 = ch("5Y");
	/** チャンネル番号：1Pロングノートオブジェ35 */
	public static final int NUM_LONG_1P_35 = ch("5Z");
	/** チャンネル番号：2Pロングノートオブジェ1 */
	public static final int NUM_LONG_2P_01 = ch("61");
	/** チャンネル番号：2Pロングノートオブジェ2 */
	public static final int NUM_LONG_2P_02 = ch("62");
	/** チャンネル番号：2Pロングノートオブジェ3 */
	public static final int NUM_LONG_2P_03 = ch("63");
	/** チャンネル番号：2Pロングノートオブジェ4 */
	public static final int NUM_LONG_2P_04 = ch("64");
	/** チャンネル番号：2Pロングノートオブジェ5 */
	public static final int NUM_LONG_2P_05 = ch("65");
	/** チャンネル番号：2Pロングノートオブジェ6 */
	public static final int NUM_LONG_2P_06 = ch("66");
	/** チャンネル番号：2Pロングノートオブジェ7 */
	public static final int NUM_LONG_2P_07 = ch("67");
	/** チャンネル番号：2Pロングノートオブジェ8 */
	public static final int NUM_LONG_2P_08 = ch("68");
	/** チャンネル番号：2Pロングノートオブジェ9 */
	public static final int NUM_LONG_2P_09 = ch("69");
	/** チャンネル番号：2Pロングノートオブジェ10 */
	public static final int NUM_LONG_2P_10 = ch("6A");
	/** チャンネル番号：2Pロングノートオブジェ11 */
	public static final int NUM_LONG_2P_11 = ch("6B");
	/** チャンネル番号：2Pロングノートオブジェ12 */
	public static final int NUM_LONG_2P_12 = ch("6C");
	/** チャンネル番号：2Pロングノートオブジェ13 */
	public static final int NUM_LONG_2P_13 = ch("6D");
	/** チャンネル番号：2Pロングノートオブジェ14 */
	public static final int NUM_LONG_2P_14 = ch("6E");
	/** チャンネル番号：2Pロングノートオブジェ15 */
	public static final int NUM_LONG_2P_15 = ch("6F");
	/** チャンネル番号：2Pロングノートオブジェ16 */
	public static final int NUM_LONG_2P_16 = ch("6G");
	/** チャンネル番号：2Pロングノートオブジェ17 */
	public static final int NUM_LONG_2P_17 = ch("6H");
	/** チャンネル番号：2Pロングノートオブジェ18 */
	public static final int NUM_LONG_2P_18 = ch("6I");
	/** チャンネル番号：2Pロングノートオブジェ19 */
	public static final int NUM_LONG_2P_19 = ch("6J");
	/** チャンネル番号：2Pロングノートオブジェ20 */
	public static final int NUM_LONG_2P_20 = ch("6K");
	/** チャンネル番号：2Pロングノートオブジェ21 */
	public static final int NUM_LONG_2P_21 = ch("6L");
	/** チャンネル番号：2Pロングノートオブジェ22 */
	public static final int NUM_LONG_2P_22 = ch("6M");
	/** チャンネル番号：2Pロングノートオブジェ23 */
	public static final int NUM_LONG_2P_23 = ch("6N");
	/** チャンネル番号：2Pロングノートオブジェ24 */
	public static final int NUM_LONG_2P_24 = ch("6O");
	/** チャンネル番号：2Pロングノートオブジェ25 */
	public static final int NUM_LONG_2P_25 = ch("6P");
	/** チャンネル番号：2Pロングノートオブジェ26 */
	public static final int NUM_LONG_2P_26 = ch("6Q");
	/** チャンネル番号：2Pロングノートオブジェ27 */
	public static final int NUM_LONG_2P_27 = ch("6R");
	/** チャンネル番号：2Pロングノートオブジェ28 */
	public static final int NUM_LONG_2P_28 = ch("6S");
	/** チャンネル番号：2Pロングノートオブジェ29 */
	public static final int NUM_LONG_2P_29 = ch("6T");
	/** チャンネル番号：2Pロングノートオブジェ30 */
	public static final int NUM_LONG_2P_30 = ch("6U");
	/** チャンネル番号：2Pロングノートオブジェ31 */
	public static final int NUM_LONG_2P_31 = ch("6V");
	/** チャンネル番号：2Pロングノートオブジェ32 */
	public static final int NUM_LONG_2P_32 = ch("6W");
	/** チャンネル番号：2Pロングノートオブジェ33 */
	public static final int NUM_LONG_2P_33 = ch("6X");
	/** チャンネル番号：2Pロングノートオブジェ34 */
	public static final int NUM_LONG_2P_34 = ch("6Y");
	/** チャンネル番号：2Pロングノートオブジェ35 */
	public static final int NUM_LONG_2P_35 = ch("6Z");
	/** チャンネル番号：1P地雷オブジェ1 */
	public static final int NUM_LANDMINE_1P_01 = ch("D1");
	/** チャンネル番号：1P地雷オブジェ2 */
	public static final int NUM_LANDMINE_1P_02 = ch("D2");
	/** チャンネル番号：1P地雷オブジェ3 */
	public static final int NUM_LANDMINE_1P_03 = ch("D3");
	/** チャンネル番号：1P地雷オブジェ4 */
	public static final int NUM_LANDMINE_1P_04 = ch("D4");
	/** チャンネル番号：1P地雷オブジェ5 */
	public static final int NUM_LANDMINE_1P_05 = ch("D5");
	/** チャンネル番号：1P地雷オブジェ6 */
	public static final int NUM_LANDMINE_1P_06 = ch("D6");
	/** チャンネル番号：1P地雷オブジェ7 */
	public static final int NUM_LANDMINE_1P_07 = ch("D7");
	/** チャンネル番号：1P地雷オブジェ8 */
	public static final int NUM_LANDMINE_1P_08 = ch("D8");
	/** チャンネル番号：1P地雷オブジェ9 */
	public static final int NUM_LANDMINE_1P_09 = ch("D9");
	/** チャンネル番号：2P地雷オブジェ1 */
	public static final int NUM_LANDMINE_2P_01 = ch("E1");
	/** チャンネル番号：2P地雷オブジェ2 */
	public static final int NUM_LANDMINE_2P_02 = ch("E2");
	/** チャンネル番号：2P地雷オブジェ3 */
	public static final int NUM_LANDMINE_2P_03 = ch("E3");
	/** チャンネル番号：2P地雷オブジェ4 */
	public static final int NUM_LANDMINE_2P_04 = ch("E4");
	/** チャンネル番号：2P地雷オブジェ5 */
	public static final int NUM_LANDMINE_2P_05 = ch("E5");
	/** チャンネル番号：2P地雷オブジェ6 */
	public static final int NUM_LANDMINE_2P_06 = ch("E6");
	/** チャンネル番号：2P地雷オブジェ7 */
	public static final int NUM_LANDMINE_2P_07 = ch("E7");
	/** チャンネル番号：2P地雷オブジェ8 */
	public static final int NUM_LANDMINE_2P_08 = ch("E8");
	/** チャンネル番号：2P地雷オブジェ9 */
	public static final int NUM_LANDMINE_2P_09 = ch("E9");

	/**
	 * BGM
	 * <table>
	 * <caption>&nbsp;</caption>
	 * <tr><th style="text-align:left;">番号</th><td>01</td></tr>
	 * <tr><th style="text-align:left;">データ型</th><td>ARRAY36</td></tr>
	 * <tr><th style="text-align:left;">参照先</th><td>{@link BeMusicMeta#WAV #WAV}</td></tr>
	 * <tr><th style="text-align:left;">初期値</th><td>""</td></tr>
	 * <tr><th style="text-align:left;">複数データ</th><td>YES</td></tr>
	 * <tr><th style="text-align:left;">同一性チェック</th><td>NO</td></tr>
	 * <tr><th style="text-align:left;">説明</th>
	 * <td>指定位置で音声の再生を指示します。このチャンネルは1小節に複数の定義を持つことが出来ます。</td></tr>
	 * </table>
	 */
	public static final BmsChannel BGM = new BmsChannel(NUM_BGM, BmsType.ARRAY36, "#wav", "", true, false);
	/**
	 * 小節長変更
	 * <table><caption>&nbsp;</caption>
	 * <tr><th style="text-align:left;">番号</th><td>02</td></tr>
	 * <tr><th style="text-align:left;">データ型</th><td>NUMERIC</td></tr>
	 * <tr><th style="text-align:left;">参照先</th><td>無し</td></tr>
	 * <tr><th style="text-align:left;">初期値</th><td>1</td></tr>
	 * <tr><th style="text-align:left;">複数データ</th><td>NO</td></tr>
	 * <tr><th style="text-align:left;">同一性チェック</th><td>YES</td></tr>
	 * <tr><th style="text-align:left;">説明</th>
	 * <td>定義した小節の長さを変更します。</td></tr>
	 * </table>
	 */
	public static final BmsChannel LENGTH = new BmsChannel(NUM_LENGTH, BmsType.NUMERIC, null, "1.0", false, true);
	/**
	 * BPM変更(旧式)
	 * <table><caption>&nbsp;</caption>
	 * <tr><th style="text-align:left;">番号</th><td>03</td></tr>
	 * <tr><th style="text-align:left;">データ型</th><td>ARRAY16</td></tr>
	 * <tr><th style="text-align:left;">参照先</th><td>無し</td></tr>
	 * <tr><th style="text-align:left;">初期値</th><td>""</td></tr>
	 * <tr><th style="text-align:left;">複数データ</th><td>NO</td></tr>
	 * <tr><th style="text-align:left;">同一性チェック</th><td>YES</td></tr>
	 * <tr><th style="text-align:left;">説明</th>
	 * <td>指定位置でBPMの変更を行います。このチャンネルではBPMを1～255の範囲でしか変更出来ません。</td></tr>
	 * </table>
	 */
	public static final BmsChannel BPM_LEGACY = new BmsChannel(NUM_BPM_LEGACY, BmsType.ARRAY16, null, "", false, true);
	/**
	 * BGA
	 * <table><caption>&nbsp;</caption>
	 * <tr><th style="text-align:left;">番号</th><td>04</td></tr>
	 * <tr><th style="text-align:left;">データ型</th><td>ARRAY36</td></tr>
	 * <tr><th style="text-align:left;">参照先</th><td>{@link BeMusicMeta#BMP #BMP}</td></tr>
	 * <tr><th style="text-align:left;">初期値</th><td>""</td></tr>
	 * <tr><th style="text-align:left;">複数データ</th><td>NO</td></tr>
	 * <tr><th style="text-align:left;">同一性チェック</th><td>NO</td></tr>
	 * <tr><th style="text-align:left;">説明</th>
	 * <td>指定位置で画像を表示します。</td></tr>
	 * </table>
	 */
	public static final BmsChannel BGA = new BmsChannel(NUM_BGA, BmsType.ARRAY36, "#bmp", "", false, false);
	/**
	 * Extended Object
	 * <table><caption>&nbsp;</caption>
	 * <tr><th style="text-align:left;">番号</th><td>05</td></tr>
	 * <tr><th style="text-align:left;">データ型</th><td>ARRAY36</td></tr>
	 * <tr><th style="text-align:left;">参照先</th><td>無し</td></tr>
	 * <tr><th style="text-align:left;">初期値</th><td>""</td></tr>
	 * <tr><th style="text-align:left;">複数データ</th><td>NO</td></tr>
	 * <tr><th style="text-align:left;">同一性チェック</th><td>NO</td></tr>
	 * <tr><th style="text-align:left;">説明</th>
	 * <td>プレイ画面のUIカスタマイズを行います。</td></tr>
	 * </table>
	 */
	public static final BmsChannel EXT_OBJ = new BmsChannel(NUM_EXT_OBJ, BmsType.ARRAY36, null, "", false, false);
	/**
	 * BGA(ミスレイヤー)
	 * <table><caption>&nbsp;</caption>
	 * <tr><th style="text-align:left;">番号</th><td>06</td></tr>
	 * <tr><th style="text-align:left;">データ型</th><td>ARRAY36</td></tr>
	 * <tr><th style="text-align:left;">参照先</th><td>{@link BeMusicMeta#BMP #BMP}</td></tr>
	 * <tr><th style="text-align:left;">初期値</th><td>""</td></tr>
	 * <tr><th style="text-align:left;">複数データ</th><td>NO</td></tr>
	 * <tr><th style="text-align:left;">同一性チェック</th><td>NO</td></tr>
	 * <tr><th style="text-align:left;">説明</th>
	 * <td>ミス時に表示される画像を指定します。</td></tr>
	 * </table>
	 */
	public static final BmsChannel BGA_MISS = new BmsChannel(NUM_BGA_MISS, BmsType.ARRAY36, "#bmp", "", false, false);
	/**
	 * BGA LAYER
	 * <table><caption>&nbsp;</caption>
	 * <tr><th style="text-align:left;">番号</th><td>07</td></tr>
	 * <tr><th style="text-align:left;">データ型</th><td>ARRAY36</td></tr>
	 * <tr><th style="text-align:left;">参照先</th><td>{@link BeMusicMeta#BMP #BMP}</td></tr>
	 * <tr><th style="text-align:left;">初期値</th><td>""</td></tr>
	 * <tr><th style="text-align:left;">複数データ</th><td>NO</td></tr>
	 * <tr><th style="text-align:left;">同一性チェック</th><td>NO</td></tr>
	 * <tr><th style="text-align:left;">説明</th>
	 * <td>{@link #BGA}の上に表示する画像を指定します。</td></tr>
	 * </table>
	 */
	public static final BmsChannel BGA_LAYER = new BmsChannel(NUM_BGA_LAYER, BmsType.ARRAY36, "#bmp", "", false, false);
	/**
	 * BPM変更
	 * <table><caption>&nbsp;</caption>
	 * <tr><th style="text-align:left;">番号</th><td>08</td></tr>
	 * <tr><th style="text-align:left;">データ型</th><td>ARRAY36</td></tr>
	 * <tr><th style="text-align:left;">参照先</th><td>{@link BeMusicMeta#BPM #BPM}</td></tr>
	 * <tr><th style="text-align:left;">初期値</th><td>""</td></tr>
	 * <tr><th style="text-align:left;">複数データ</th><td>NO</td></tr>
	 * <tr><th style="text-align:left;">同一性チェック</th><td>YES</td></tr>
	 * <tr><th style="text-align:left;">説明</th>
	 * <td>指定位置でBPMの変更を行います。</td></tr>
	 * </table>
	 */
	public static final BmsChannel BPM = new BmsChannel(NUM_BPM, BmsType.ARRAY36, "#bpm", "", false, true);
	/**
	 * 譜面停止
	 * <table><caption>&nbsp;</caption>
	 * <tr><th style="text-align:left;">番号</th><td>09</td></tr>
	 * <tr><th style="text-align:left;">データ型</th><td>ARRAY36</td></tr>
	 * <tr><th style="text-align:left;">参照先</th><td>{@link BeMusicMeta#STOP #STOP}</td></tr>
	 * <tr><th style="text-align:left;">初期値</th><td>""</td></tr>
	 * <tr><th style="text-align:left;">複数データ</th><td>NO</td></tr>
	 * <tr><th style="text-align:left;">同一性チェック</th><td>YES</td></tr>
	 * <tr><th style="text-align:left;">説明</th>
	 * <td>指定位置で譜面を一時停止します。</td></tr>
	 * </table>
	 */
	public static final BmsChannel STOP = new BmsChannel(NUM_STOP, BmsType.ARRAY36, "#stop", "", false, true);
	/**
	 * BGA LAYER2
	 * <table><caption>&nbsp;</caption>
	 * <tr><th style="text-align:left;">番号</th><td>0A</td></tr>
	 * <tr><th style="text-align:left;">データ型</th><td>ARRAY36</td></tr>
	 * <tr><th style="text-align:left;">参照先</th><td>{@link BeMusicMeta#BMP #BMP}</td></tr>
	 * <tr><th style="text-align:left;">初期値</th><td>""</td></tr>
	 * <tr><th style="text-align:left;">複数データ</th><td>NO</td></tr>
	 * <tr><th style="text-align:left;">同一性チェック</th><td>NO</td></tr>
	 * <tr><th style="text-align:left;">説明</th>
	 * <td>{@link #BGA_LAYER}の上に表示する画像を指定します。</td></tr>
	 * </table>
	 */
	public static final BmsChannel BGA_LAYER2 = new BmsChannel(NUM_BGA_LAYER2, BmsType.ARRAY36, "#bmp", "", false, false);
	/**
	 * BGA不透明度
	 * <table><caption>&nbsp;</caption>
	 * <tr><th style="text-align:left;">番号</th><td>0B</td></tr>
	 * <tr><th style="text-align:left;">データ型</th><td>ARRAY16</td></tr>
	 * <tr><th style="text-align:left;">参照先</th><td>無し</td></tr>
	 * <tr><th style="text-align:left;">初期値</th><td>""</td></tr>
	 * <tr><th style="text-align:left;">複数データ</th><td>NO</td></tr>
	 * <tr><th style="text-align:left;">同一性チェック</th><td>NO</td></tr>
	 * <tr><th style="text-align:left;">説明</th>
	 * <td>{@link #BGA}の不透明度を指定します。</td></tr>
	 * </table>
	 */
	public static final BmsChannel BGA_BASE_OPACITY = new BmsChannel(NUM_BGA_BASE_OPACITY, BmsType.ARRAY16, null, "", false, false);
	/**
	 * BGA LAYER不透明度
	 * <table><caption>&nbsp;</caption>
	 * <tr><th style="text-align:left;">番号</th><td>0C</td></tr>
	 * <tr><th style="text-align:left;">データ型</th><td>ARRAY16</td></tr>
	 * <tr><th style="text-align:left;">参照先</th><td>無し</td></tr>
	 * <tr><th style="text-align:left;">初期値</th><td>""</td></tr>
	 * <tr><th style="text-align:left;">複数データ</th><td>NO</td></tr>
	 * <tr><th style="text-align:left;">同一性チェック</th><td>NO</td></tr>
	 * <tr><th style="text-align:left;">説明</th>
	 * <td>{@link #BGA_LAYER}の不透明度を指定します。</td></tr>
	 * </table>
	 */
	public static final BmsChannel BGA_LAYER_OPACITY = new BmsChannel(NUM_BGA_LAYER_OPACITY, BmsType.ARRAY16, null, "", false, false);
	/**
	 * BGA LAYER2不透明度
	 * <table><caption>&nbsp;</caption>
	 * <tr><th style="text-align:left;">番号</th><td>0D</td></tr>
	 * <tr><th style="text-align:left;">データ型</th><td>ARRAY16</td></tr>
	 * <tr><th style="text-align:left;">参照先</th><td>無し</td></tr>
	 * <tr><th style="text-align:left;">初期値</th><td>""</td></tr>
	 * <tr><th style="text-align:left;">複数データ</th><td>NO</td></tr>
	 * <tr><th style="text-align:left;">同一性チェック</th><td>NO</td></tr>
	 * <tr><th style="text-align:left;">説明</th>
	 * <td>{@link #BGA_LAYER2}の不透明度を指定します。</td></tr>
	 * </table>
	 */
	public static final BmsChannel BGA_LAYER2_OPACITY = new BmsChannel(NUM_BGA_LAYER2_OPACITY, BmsType.ARRAY16, null, "", false, false);
	/**
	 * BGA(ミスレイヤー)不透明度
	 * <table><caption>&nbsp;</caption>
	 * <tr><th style="text-align:left;">番号</th><td>0E</td></tr>
	 * <tr><th style="text-align:left;">データ型</th><td>ARRAY16</td></tr>
	 * <tr><th style="text-align:left;">参照先</th><td>無し</td></tr>
	 * <tr><th style="text-align:left;">初期値</th><td>""</td></tr>
	 * <tr><th style="text-align:left;">複数データ</th><td>NO</td></tr>
	 * <tr><th style="text-align:left;">同一性チェック</th><td>NO</td></tr>
	 * <tr><th style="text-align:left;">説明</th>
	 * <td>{@link #BGA_MISS}の不透明度を指定します。</td></tr>
	 * </table>
	 */
	public static final BmsChannel BGA_MISS_OPACITY = new BmsChannel(NUM_BGA_MISS_OPACITY, BmsType.ARRAY16, null, "", false, false);
	/**
	 * BGM音量
	 * <table><caption>&nbsp;</caption>
	 * <tr><th style="text-align:left;">番号</th><td>97</td></tr>
	 * <tr><th style="text-align:left;">データ型</th><td>ARRAY16</td></tr>
	 * <tr><th style="text-align:left;">参照先</th><td>無し</td></tr>
	 * <tr><th style="text-align:left;">初期値</th><td>""</td></tr>
	 * <tr><th style="text-align:left;">複数データ</th><td>NO</td></tr>
	 * <tr><th style="text-align:left;">同一性チェック</th><td>NO</td></tr>
	 * <tr><th style="text-align:left;">説明</th>
	 * <td>BGMの音量を指定します。</td></tr>
	 * </table>
	 */
	public static final BmsChannel BGM_VOLUME = new BmsChannel(NUM_BGM_VOLUME, BmsType.ARRAY16, null, "", false, false);
	/**
	 * キー音音量
	 * <table><caption>&nbsp;</caption>
	 * <tr><th style="text-align:left;">番号</th><td>98</td></tr>
	 * <tr><th style="text-align:left;">データ型</th><td>ARRAY16</td></tr>
	 * <tr><th style="text-align:left;">参照先</th><td>無し</td></tr>
	 * <tr><th style="text-align:left;">初期値</th><td>""</td></tr>
	 * <tr><th style="text-align:left;">複数データ</th><td>NO</td></tr>
	 * <tr><th style="text-align:left;">同一性チェック</th><td>NO</td></tr>
	 * <tr><th style="text-align:left;">説明</th>
	 * <td>キー音の音量を指定します。</td></tr>
	 * </table>
	 */
	public static final BmsChannel KEY_VOLUME = new BmsChannel(NUM_KEY_VOLUME, BmsType.ARRAY16, null, "", false, false);
	/**
	 * テキスト表示
	 * <table><caption>&nbsp;</caption>
	 * <tr><th style="text-align:left;">番号</th><td>99</td></tr>
	 * <tr><th style="text-align:left;">データ型</th><td>ARRAY36</td></tr>
	 * <tr><th style="text-align:left;">参照先</th><td>{@link BeMusicMeta#TEXT #TEXT}</td></tr>
	 * <tr><th style="text-align:left;">初期値</th><td>""</td></tr>
	 * <tr><th style="text-align:left;">複数データ</th><td>NO</td></tr>
	 * <tr><th style="text-align:left;">同一性チェック</th><td>NO</td></tr>
	 * <tr><th style="text-align:left;">説明</th>
	 * <td>表示するテキストを指定します。</td></tr>
	 * </table>
	 */
	public static final BmsChannel TEXT = new BmsChannel(NUM_TEXT, BmsType.ARRAY36, "#text", "", false, false);
	/**
	 * 判定ランク変更
	 * <table><caption>&nbsp;</caption>
	 * <tr><th style="text-align:left;">番号</th><td>A0</td></tr>
	 * <tr><th style="text-align:left;">データ型</th><td>ARRAY36</td></tr>
	 * <tr><th style="text-align:left;">参照先</th><td>{@link BeMusicMeta#EXRANK #EXRANK}</td></tr>
	 * <tr><th style="text-align:left;">初期値</th><td>""</td></tr>
	 * <tr><th style="text-align:left;">複数データ</th><td>NO</td></tr>
	 * <tr><th style="text-align:left;">同一性チェック</th><td>YES</td></tr>
	 * <tr><th style="text-align:left;">説明</th>
	 * <td>指定位置で判定ランクを変更します。</td></tr>
	 * </table>
	 */
	public static final BmsChannel JUDGE = new BmsChannel(NUM_JUDGE, BmsType.ARRAY36, "#exrank", "", false, true);
	/**
	 * BGA aRGB
	 * <table><caption>&nbsp;</caption>
	 * <tr><th style="text-align:left;">番号</th><td>A1</td></tr>
	 * <tr><th style="text-align:left;">データ型</th><td>ARRAY36</td></tr>
	 * <tr><th style="text-align:left;">参照先</th><td>{@link BeMusicMeta#ARGB #ARGB}</td></tr>
	 * <tr><th style="text-align:left;">初期値</th><td>""</td></tr>
	 * <tr><th style="text-align:left;">複数データ</th><td>NO</td></tr>
	 * <tr><th style="text-align:left;">同一性チェック</th><td>NO</td></tr>
	 * <tr><th style="text-align:left;">説明</th>
	 * <td>BGAに適用するARGBの値を指定します。</td></tr>
	 * </table>
	 */
	public static final BmsChannel BGA_BASE_ARGB = new BmsChannel(NUM_BGA_BASE_ARGB, BmsType.ARRAY36, "#argb", "", false, false);
	/**
	 * BGA LAYER aRGB
	 * <table><caption>&nbsp;</caption>
	 * <tr><th style="text-align:left;">番号</th><td>A2</td></tr>
	 * <tr><th style="text-align:left;">データ型</th><td>ARRAY36</td></tr>
	 * <tr><th style="text-align:left;">参照先</th><td>{@link BeMusicMeta#ARGB #ARGB}</td></tr>
	 * <tr><th style="text-align:left;">初期値</th><td>""</td></tr>
	 * <tr><th style="text-align:left;">複数データ</th><td>NO</td></tr>
	 * <tr><th style="text-align:left;">同一性チェック</th><td>NO</td></tr>
	 * <tr><th style="text-align:left;">説明</th>
	 * <td>BGA LAYERに適用するARGBの値を指定します。</td></tr>
	 * </table>
	 */
	public static final BmsChannel BGA_LAYER_ARGB = new BmsChannel(NUM_BGA_LAYER_ARGB, BmsType.ARRAY36, "#argb", "", false, false);
	/**
	 * BGA LAYER2 aRGB
	 * <table><caption>&nbsp;</caption>
	 * <tr><th style="text-align:left;">番号</th><td>A3</td></tr>
	 * <tr><th style="text-align:left;">データ型</th><td>ARRAY36</td></tr>
	 * <tr><th style="text-align:left;">参照先</th><td>{@link BeMusicMeta#ARGB #ARGB}</td></tr>
	 * <tr><th style="text-align:left;">初期値</th><td>""</td></tr>
	 * <tr><th style="text-align:left;">複数データ</th><td>NO</td></tr>
	 * <tr><th style="text-align:left;">同一性チェック</th><td>NO</td></tr>
	 * <tr><th style="text-align:left;">説明</th>
	 * <td>BGA LAYER2に適用するARGBの値を指定します。</td></tr>
	 * </table>
	 */
	public static final BmsChannel BGA_LAYER2_ARGB = new BmsChannel(NUM_BGA_LAYER2_ARGB, BmsType.ARRAY36, "#argb", "", false, false);
	/**
	 * BGA(ミスレイヤー) aRGB
	 * <table><caption>&nbsp;</caption>
	 * <tr><th style="text-align:left;">番号</th><td>A4</td></tr>
	 * <tr><th style="text-align:left;">データ型</th><td>ARRAY36</td></tr>
	 * <tr><th style="text-align:left;">参照先</th><td>{@link BeMusicMeta#ARGB #ARGB}</td></tr>
	 * <tr><th style="text-align:left;">初期値</th><td>""</td></tr>
	 * <tr><th style="text-align:left;">複数データ</th><td>NO</td></tr>
	 * <tr><th style="text-align:left;">同一性チェック</th><td>NO</td></tr>
	 * <tr><th style="text-align:left;">説明</th>
	 * <td>BGA(ミスレイヤー)に適用するARGBの値を指定します。</td></tr>
	 * </table>
	 */
	public static final BmsChannel BGA_MISS_ARGB = new BmsChannel(NUM_BGA_MISS_ARGB, BmsType.ARRAY36, "#argb", "", false, false);
	/**
	 * キーバインドLAYERアニメーション
	 * <table><caption>&nbsp;</caption>
	 * <tr><th style="text-align:left;">番号</th><td>A5</td></tr>
	 * <tr><th style="text-align:left;">データ型</th><td>ARRAY36</td></tr>
	 * <tr><th style="text-align:left;">参照先</th><td>{@link BeMusicMeta#SWBGA #SWBGA}</td></tr>
	 * <tr><th style="text-align:left;">初期値</th><td>""</td></tr>
	 * <tr><th style="text-align:left;">複数データ</th><td>NO</td></tr>
	 * <tr><th style="text-align:left;">同一性チェック</th><td>NO</td></tr>
	 * <tr><th style="text-align:left;">説明</th>
	 * <td>キー押下に反応するイメージシーケンスの指定を行います。</td></tr>
	 * </table>
	 */
	public static final BmsChannel BGA_KEYBOUND = new BmsChannel(NUM_BGA_KEYBOUND, BmsType.ARRAY36, "#swbga", "", false, false);
	/**
	 * プレイオプション変更
	 * <table><caption>&nbsp;</caption>
	 * <tr><th style="text-align:left;">番号</th><td>A6</td></tr>
	 * <tr><th style="text-align:left;">データ型</th><td>ARRAY36</td></tr>
	 * <tr><th style="text-align:left;">参照先</th><td>{@link BeMusicMeta#CHANGEOPTION #CHANGEOPTION}</td></tr>
	 * <tr><th style="text-align:left;">初期値</th><td>""</td></tr>
	 * <tr><th style="text-align:left;">複数データ</th><td>NO</td></tr>
	 * <tr><th style="text-align:left;">同一性チェック</th><td>YES</td></tr>
	 * <tr><th style="text-align:left;">説明</th>
	 * <td>プレイオプションの変更を指定します。</td></tr>
	 * </table>
	 */
	public static final BmsChannel OPTION = new BmsChannel(NUM_OPTION, BmsType.ARRAY36, "#changeoption", "", false, true);
	/**
	 * スクロール速度変更
	 * <table><caption>&nbsp;</caption>
	 * <tr><th style="text-align:left;">番号</th><td>SC</td></tr>
	 * <tr><th style="text-align:left;">データ型</th><td>ARRAY36</td></tr>
	 * <tr><th style="text-align:left;">参照先</th><td>{@link BeMusicMeta#SCROLL}</td></tr>
	 * <tr><th style="text-align:left;">初期値</th><td>""</td></tr>
	 * <tr><th style="text-align:left;">複数データ</th><td>NO</td></tr>
	 * <tr><th style="text-align:left;">同一性チェック</th><td>YES</td></tr>
	 * <tr><th style="text-align:left;">説明</th>
	 * <td>譜面のスクロール速度変更を指定します。</td></tr>
	 * </table>
	 */
	public static final BmsChannel SCROLL = new BmsChannel(NUM_SCROLL, BmsType.ARRAY36, "#scroll", "", false, true);

	/**
	 * 1P可視オブジェ
	 * <table><caption>&nbsp;</caption>
	 * <tr><th style="text-align:left;">番号</th><td>11～1Z</td></tr>
	 * <tr><th style="text-align:left;">データ型</th><td>ARRAY36</td></tr>
	 * <tr><th style="text-align:left;">参照先</th><td>{@link BeMusicMeta#WAV #WAV}</td></tr>
	 * <tr><th style="text-align:left;">初期値</th><td>""</td></tr>
	 * <tr><th style="text-align:left;">複数データ</th><td>NO</td></tr>
	 * <tr><th style="text-align:left;">同一性チェック</th><td>YES</td></tr>
	 * <tr><th style="text-align:left;">説明</th>
	 * <td>1Pのプレイ可能なノートを表します。チャンネルの配列に00以外の値を指定すると、その位置にノートを配置し、値に割り当てられた音声を再生します。</td></tr>
	 * </table>
	 */
	public static final BmsChannel VISIBLE_1P_01 = new BmsChannel(NUM_VISIBLE_1P_01, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 1P可視オブジェ
	 * @see #VISIBLE_1P_01
	 */
	public static final BmsChannel VISIBLE_1P_02 = new BmsChannel(NUM_VISIBLE_1P_02, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 1P可視オブジェ
	 * @see #VISIBLE_1P_01
	 */
	public static final BmsChannel VISIBLE_1P_03 = new BmsChannel(NUM_VISIBLE_1P_03, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 1P可視オブジェ
	 * @see #VISIBLE_1P_01
	 */
	public static final BmsChannel VISIBLE_1P_04 = new BmsChannel(NUM_VISIBLE_1P_04, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 1P可視オブジェ
	 * @see #VISIBLE_1P_01
	 */
	public static final BmsChannel VISIBLE_1P_05 = new BmsChannel(NUM_VISIBLE_1P_05, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 1P可視オブジェ
	 * @see #VISIBLE_1P_01
	 */
	public static final BmsChannel VISIBLE_1P_06 = new BmsChannel(NUM_VISIBLE_1P_06, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 1P可視オブジェ
	 * @see #VISIBLE_1P_01
	 */
	public static final BmsChannel VISIBLE_1P_07 = new BmsChannel(NUM_VISIBLE_1P_07, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 1P可視オブジェ
	 * @see #VISIBLE_1P_01
	 */
	public static final BmsChannel VISIBLE_1P_08 = new BmsChannel(NUM_VISIBLE_1P_08, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 1P可視オブジェ
	 * @see #VISIBLE_1P_01
	 */
	public static final BmsChannel VISIBLE_1P_09 = new BmsChannel(NUM_VISIBLE_1P_09, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 1P可視オブジェ
	 * @see #VISIBLE_1P_01
	 */
	public static final BmsChannel VISIBLE_1P_10 = new BmsChannel(NUM_VISIBLE_1P_10, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 1P可視オブジェ
	 * @see #VISIBLE_1P_01
	 */
	public static final BmsChannel VISIBLE_1P_11 = new BmsChannel(NUM_VISIBLE_1P_11, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 1P可視オブジェ
	 * @see #VISIBLE_1P_01
	 */
	public static final BmsChannel VISIBLE_1P_12 = new BmsChannel(NUM_VISIBLE_1P_12, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 1P可視オブジェ
	 * @see #VISIBLE_1P_01
	 */
	public static final BmsChannel VISIBLE_1P_13 = new BmsChannel(NUM_VISIBLE_1P_13, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 1P可視オブジェ
	 * @see #VISIBLE_1P_01
	 */
	public static final BmsChannel VISIBLE_1P_14 = new BmsChannel(NUM_VISIBLE_1P_14, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 1P可視オブジェ
	 * @see #VISIBLE_1P_01
	 */
	public static final BmsChannel VISIBLE_1P_15 = new BmsChannel(NUM_VISIBLE_1P_15, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 1P可視オブジェ
	 * @see #VISIBLE_1P_01
	 */
	public static final BmsChannel VISIBLE_1P_16 = new BmsChannel(NUM_VISIBLE_1P_16, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 1P可視オブジェ
	 * @see #VISIBLE_1P_01
	 */
	public static final BmsChannel VISIBLE_1P_17 = new BmsChannel(NUM_VISIBLE_1P_17, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 1P可視オブジェ
	 * @see #VISIBLE_1P_01
	 */
	public static final BmsChannel VISIBLE_1P_18 = new BmsChannel(NUM_VISIBLE_1P_18, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 1P可視オブジェ
	 * @see #VISIBLE_1P_01
	 */
	public static final BmsChannel VISIBLE_1P_19 = new BmsChannel(NUM_VISIBLE_1P_19, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 1P可視オブジェ
	 * @see #VISIBLE_1P_01
	 */
	public static final BmsChannel VISIBLE_1P_20 = new BmsChannel(NUM_VISIBLE_1P_20, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 1P可視オブジェ
	 * @see #VISIBLE_1P_01
	 */
	public static final BmsChannel VISIBLE_1P_21 = new BmsChannel(NUM_VISIBLE_1P_21, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 1P可視オブジェ
	 * @see #VISIBLE_1P_01
	 */
	public static final BmsChannel VISIBLE_1P_22 = new BmsChannel(NUM_VISIBLE_1P_22, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 1P可視オブジェ
	 * @see #VISIBLE_1P_01
	 */
	public static final BmsChannel VISIBLE_1P_23 = new BmsChannel(NUM_VISIBLE_1P_23, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 1P可視オブジェ
	 * @see #VISIBLE_1P_01
	 */
	public static final BmsChannel VISIBLE_1P_24 = new BmsChannel(NUM_VISIBLE_1P_24, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 1P可視オブジェ
	 * @see #VISIBLE_1P_01
	 */
	public static final BmsChannel VISIBLE_1P_25 = new BmsChannel(NUM_VISIBLE_1P_25, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 1P可視オブジェ
	 * @see #VISIBLE_1P_01
	 */
	public static final BmsChannel VISIBLE_1P_26 = new BmsChannel(NUM_VISIBLE_1P_26, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 1P可視オブジェ
	 * @see #VISIBLE_1P_01
	 */
	public static final BmsChannel VISIBLE_1P_27 = new BmsChannel(NUM_VISIBLE_1P_27, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 1P可視オブジェ
	 * @see #VISIBLE_1P_01
	 */
	public static final BmsChannel VISIBLE_1P_28 = new BmsChannel(NUM_VISIBLE_1P_28, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 1P可視オブジェ
	 * @see #VISIBLE_1P_01
	 */
	public static final BmsChannel VISIBLE_1P_29 = new BmsChannel(NUM_VISIBLE_1P_29, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 1P可視オブジェ
	 * @see #VISIBLE_1P_01
	 */
	public static final BmsChannel VISIBLE_1P_30 = new BmsChannel(NUM_VISIBLE_1P_30, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 1P可視オブジェ
	 * @see #VISIBLE_1P_01
	 */
	public static final BmsChannel VISIBLE_1P_31 = new BmsChannel(NUM_VISIBLE_1P_31, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 1P可視オブジェ
	 * @see #VISIBLE_1P_01
	 */
	public static final BmsChannel VISIBLE_1P_32 = new BmsChannel(NUM_VISIBLE_1P_32, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 1P可視オブジェ
	 * @see #VISIBLE_1P_01
	 */
	public static final BmsChannel VISIBLE_1P_33 = new BmsChannel(NUM_VISIBLE_1P_33, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 1P可視オブジェ
	 * @see #VISIBLE_1P_01
	 */
	public static final BmsChannel VISIBLE_1P_34 = new BmsChannel(NUM_VISIBLE_1P_34, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 1P可視オブジェ
	 * @see #VISIBLE_1P_01
	 */
	public static final BmsChannel VISIBLE_1P_35 = new BmsChannel(NUM_VISIBLE_1P_35, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 2P可視オブジェ
	 * <table><caption>&nbsp;</caption>
	 * <tr><th style="text-align:left;">番号</th><td>21～2Z</td></tr>
	 * <tr><th style="text-align:left;">データ型</th><td>ARRAY36</td></tr>
	 * <tr><th style="text-align:left;">参照先</th><td>{@link BeMusicMeta#WAV #WAV}</td></tr>
	 * <tr><th style="text-align:left;">初期値</th><td>""</td></tr>
	 * <tr><th style="text-align:left;">複数データ</th><td>NO</td></tr>
	 * <tr><th style="text-align:left;">同一性チェック</th><td>YES</td></tr>
	 * <tr><th style="text-align:left;">説明</th>
	 * <td><p>1Pのプレイ可能なノートを表します。チャンネルの配列に00以外の値を指定すると、その位置にノートを配置し、値に割り当てられた音声を再生します。</p>
	 * <p>BeMusicライブラリでは、2P可視オブジェに1個でもノートが割り当てられている場合、その譜面をダブルプレー用譜面として認識します。
	 * その判定は、{@link BeMusicMeta#PLAYER #PLAYER}にどの値が指定されているかは関係ありません。</p></td></tr>
	 * </table>
	 */
	public static final BmsChannel VISIBLE_2P_01 = new BmsChannel(NUM_VISIBLE_2P_01, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 2P可視オブジェ
	 * @see #VISIBLE_2P_01
	 */
	public static final BmsChannel VISIBLE_2P_02 = new BmsChannel(NUM_VISIBLE_2P_02, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 2P可視オブジェ
	 * @see #VISIBLE_2P_01
	 */
	public static final BmsChannel VISIBLE_2P_03 = new BmsChannel(NUM_VISIBLE_2P_03, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 2P可視オブジェ
	 * @see #VISIBLE_2P_01
	 */
	public static final BmsChannel VISIBLE_2P_04 = new BmsChannel(NUM_VISIBLE_2P_04, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 2P可視オブジェ
	 * @see #VISIBLE_2P_01
	 */
	public static final BmsChannel VISIBLE_2P_05 = new BmsChannel(NUM_VISIBLE_2P_05, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 2P可視オブジェ
	 * @see #VISIBLE_2P_01
	 */
	public static final BmsChannel VISIBLE_2P_06 = new BmsChannel(NUM_VISIBLE_2P_06, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 2P可視オブジェ
	 * @see #VISIBLE_2P_01
	 */
	public static final BmsChannel VISIBLE_2P_07 = new BmsChannel(NUM_VISIBLE_2P_07, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 2P可視オブジェ
	 * @see #VISIBLE_2P_01
	 */
	public static final BmsChannel VISIBLE_2P_08 = new BmsChannel(NUM_VISIBLE_2P_08, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 2P可視オブジェ
	 * @see #VISIBLE_2P_01
	 */
	public static final BmsChannel VISIBLE_2P_09 = new BmsChannel(NUM_VISIBLE_2P_09, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 2P可視オブジェ
	 * @see #VISIBLE_2P_01
	 */
	public static final BmsChannel VISIBLE_2P_10 = new BmsChannel(NUM_VISIBLE_2P_10, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 2P可視オブジェ
	 * @see #VISIBLE_2P_01
	 */
	public static final BmsChannel VISIBLE_2P_11 = new BmsChannel(NUM_VISIBLE_2P_11, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 2P可視オブジェ
	 * @see #VISIBLE_2P_01
	 */
	public static final BmsChannel VISIBLE_2P_12 = new BmsChannel(NUM_VISIBLE_2P_12, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 2P可視オブジェ
	 * @see #VISIBLE_2P_01
	 */
	public static final BmsChannel VISIBLE_2P_13 = new BmsChannel(NUM_VISIBLE_2P_13, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 2P可視オブジェ
	 * @see #VISIBLE_2P_01
	 */
	public static final BmsChannel VISIBLE_2P_14 = new BmsChannel(NUM_VISIBLE_2P_14, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 2P可視オブジェ
	 * @see #VISIBLE_2P_01
	 */
	public static final BmsChannel VISIBLE_2P_15 = new BmsChannel(NUM_VISIBLE_2P_15, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 2P可視オブジェ
	 * @see #VISIBLE_2P_01
	 */
	public static final BmsChannel VISIBLE_2P_16 = new BmsChannel(NUM_VISIBLE_2P_16, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 2P可視オブジェ
	 * @see #VISIBLE_2P_01
	 */
	public static final BmsChannel VISIBLE_2P_17 = new BmsChannel(NUM_VISIBLE_2P_17, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 2P可視オブジェ
	 * @see #VISIBLE_2P_01
	 */
	public static final BmsChannel VISIBLE_2P_18 = new BmsChannel(NUM_VISIBLE_2P_18, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 2P可視オブジェ
	 * @see #VISIBLE_2P_01
	 */
	public static final BmsChannel VISIBLE_2P_19 = new BmsChannel(NUM_VISIBLE_2P_19, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 2P可視オブジェ
	 * @see #VISIBLE_2P_01
	 */
	public static final BmsChannel VISIBLE_2P_20 = new BmsChannel(NUM_VISIBLE_2P_20, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 2P可視オブジェ
	 * @see #VISIBLE_2P_01
	 */
	public static final BmsChannel VISIBLE_2P_21 = new BmsChannel(NUM_VISIBLE_2P_21, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 2P可視オブジェ
	 * @see #VISIBLE_2P_01
	 */
	public static final BmsChannel VISIBLE_2P_22 = new BmsChannel(NUM_VISIBLE_2P_22, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 2P可視オブジェ
	 * @see #VISIBLE_2P_01
	 */
	public static final BmsChannel VISIBLE_2P_23 = new BmsChannel(NUM_VISIBLE_2P_23, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 2P可視オブジェ
	 * @see #VISIBLE_2P_01
	 */
	public static final BmsChannel VISIBLE_2P_24 = new BmsChannel(NUM_VISIBLE_2P_24, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 2P可視オブジェ
	 * @see #VISIBLE_2P_01
	 */
	public static final BmsChannel VISIBLE_2P_25 = new BmsChannel(NUM_VISIBLE_2P_25, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 2P可視オブジェ
	 * @see #VISIBLE_2P_01
	 */
	public static final BmsChannel VISIBLE_2P_26 = new BmsChannel(NUM_VISIBLE_2P_26, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 2P可視オブジェ
	 * @see #VISIBLE_2P_01
	 */
	public static final BmsChannel VISIBLE_2P_27 = new BmsChannel(NUM_VISIBLE_2P_27, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 2P可視オブジェ
	 * @see #VISIBLE_2P_01
	 */
	public static final BmsChannel VISIBLE_2P_28 = new BmsChannel(NUM_VISIBLE_2P_28, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 2P可視オブジェ
	 * @see #VISIBLE_2P_01
	 */
	public static final BmsChannel VISIBLE_2P_29 = new BmsChannel(NUM_VISIBLE_2P_29, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 2P可視オブジェ
	 * @see #VISIBLE_2P_01
	 */
	public static final BmsChannel VISIBLE_2P_30 = new BmsChannel(NUM_VISIBLE_2P_30, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 2P可視オブジェ
	 * @see #VISIBLE_2P_01
	 */
	public static final BmsChannel VISIBLE_2P_31 = new BmsChannel(NUM_VISIBLE_2P_31, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 2P可視オブジェ
	 * @see #VISIBLE_2P_01
	 */
	public static final BmsChannel VISIBLE_2P_32 = new BmsChannel(NUM_VISIBLE_2P_32, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 2P可視オブジェ
	 * @see #VISIBLE_2P_01
	 */
	public static final BmsChannel VISIBLE_2P_33 = new BmsChannel(NUM_VISIBLE_2P_33, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 2P可視オブジェ
	 * @see #VISIBLE_2P_01
	 */
	public static final BmsChannel VISIBLE_2P_34 = new BmsChannel(NUM_VISIBLE_2P_34, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 2P可視オブジェ
	 * @see #VISIBLE_2P_01
	 */
	public static final BmsChannel VISIBLE_2P_35 = new BmsChannel(NUM_VISIBLE_2P_35, BmsType.ARRAY36, "#wav", "", false, true);

	/** 1P可視オブジェリスト */
	static final BmsChannel[] VISIBLE_1P_CHANNELS = {
			VISIBLE_1P_01, VISIBLE_1P_02, VISIBLE_1P_03, VISIBLE_1P_04, VISIBLE_1P_05, VISIBLE_1P_06, VISIBLE_1P_07,
			VISIBLE_1P_08, VISIBLE_1P_09, VISIBLE_1P_10, VISIBLE_1P_11, VISIBLE_1P_12, VISIBLE_1P_13, VISIBLE_1P_14,
			VISIBLE_1P_15, VISIBLE_1P_16, VISIBLE_1P_17, VISIBLE_1P_18, VISIBLE_1P_19, VISIBLE_1P_20, VISIBLE_1P_21,
			VISIBLE_1P_22, VISIBLE_1P_23, VISIBLE_1P_24, VISIBLE_1P_25, VISIBLE_1P_26, VISIBLE_1P_27, VISIBLE_1P_28,
			VISIBLE_1P_29, VISIBLE_1P_30, VISIBLE_1P_31, VISIBLE_1P_32, VISIBLE_1P_33, VISIBLE_1P_34, VISIBLE_1P_35,
	};

	/** 2P可視オブジェリスト */
	static final BmsChannel[] VISIBLE_2P_CHANNELS = {
			VISIBLE_2P_01, VISIBLE_2P_02, VISIBLE_2P_03, VISIBLE_2P_04, VISIBLE_2P_05, VISIBLE_2P_06, VISIBLE_2P_07,
			VISIBLE_2P_08, VISIBLE_2P_09, VISIBLE_2P_10, VISIBLE_2P_11, VISIBLE_2P_12, VISIBLE_2P_13, VISIBLE_2P_14,
			VISIBLE_2P_15, VISIBLE_2P_16, VISIBLE_2P_17, VISIBLE_2P_18, VISIBLE_2P_19, VISIBLE_2P_20, VISIBLE_2P_21,
			VISIBLE_2P_22, VISIBLE_2P_23, VISIBLE_2P_24, VISIBLE_2P_25, VISIBLE_2P_26, VISIBLE_2P_27, VISIBLE_2P_28,
			VISIBLE_2P_29, VISIBLE_2P_30, VISIBLE_2P_31, VISIBLE_2P_32, VISIBLE_2P_33, VISIBLE_2P_34, VISIBLE_2P_35,
	};

	/**
	 * 不可視オブジェ
	 * <table><caption>&nbsp;</caption>
	 * <tr><th style="text-align:left;">番号</th><td>31～3Z, 41～4Z</td></tr>
	 * <tr><th style="text-align:left;">データ型</th><td>ARRAY36</td></tr>
	 * <tr><th style="text-align:left;">参照先</th><td>{@link BeMusicMeta#WAV #WAV}</td></tr>
	 * <tr><th style="text-align:left;">初期値</th><td>""</td></tr>
	 * <tr><th style="text-align:left;">複数データ</th><td>NO</td></tr>
	 * <tr><th style="text-align:left;">同一性チェック</th><td>YES</td></tr>
	 * <tr><th style="text-align:left;">説明</th>
	 * <td>プレイ不可能なノートを表します。チャンネルの配列に00以外の値を指定すると、その位置にノートを配置し、値に割り当てられた音声を再生します。</td></tr>
	 * </table>
	 */
	public static final BmsChannel INVISIBLE_1P_01 = new BmsChannel(NUM_INVISIBLE_1P_01, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 不可視オブジェ
	 * @see #INVISIBLE_1P_01
	 */
	public static final BmsChannel INVISIBLE_1P_02 = new BmsChannel(NUM_INVISIBLE_1P_02, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 不可視オブジェ
	 * @see #INVISIBLE_1P_01
	 */
	public static final BmsChannel INVISIBLE_1P_03 = new BmsChannel(NUM_INVISIBLE_1P_03, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 不可視オブジェ
	 * @see #INVISIBLE_1P_01
	 */
	public static final BmsChannel INVISIBLE_1P_04 = new BmsChannel(NUM_INVISIBLE_1P_04, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 不可視オブジェ
	 * @see #INVISIBLE_1P_01
	 */
	public static final BmsChannel INVISIBLE_1P_05 = new BmsChannel(NUM_INVISIBLE_1P_05, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 不可視オブジェ
	 * @see #INVISIBLE_1P_01
	 */
	public static final BmsChannel INVISIBLE_1P_06 = new BmsChannel(NUM_INVISIBLE_1P_06, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 不可視オブジェ
	 * @see #INVISIBLE_1P_01
	 */
	public static final BmsChannel INVISIBLE_1P_07 = new BmsChannel(NUM_INVISIBLE_1P_07, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 不可視オブジェ
	 * @see #INVISIBLE_1P_01
	 */
	public static final BmsChannel INVISIBLE_1P_08 = new BmsChannel(NUM_INVISIBLE_1P_08, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 不可視オブジェ
	 * @see #INVISIBLE_1P_01
	 */
	public static final BmsChannel INVISIBLE_1P_09 = new BmsChannel(NUM_INVISIBLE_1P_09, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 不可視オブジェ
	 * @see #INVISIBLE_1P_01
	 */
	public static final BmsChannel INVISIBLE_1P_10 = new BmsChannel(NUM_INVISIBLE_1P_10, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 不可視オブジェ
	 * @see #INVISIBLE_1P_01
	 */
	public static final BmsChannel INVISIBLE_1P_11 = new BmsChannel(NUM_INVISIBLE_1P_11, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 不可視オブジェ
	 * @see #INVISIBLE_1P_01
	 */
	public static final BmsChannel INVISIBLE_1P_12 = new BmsChannel(NUM_INVISIBLE_1P_12, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 不可視オブジェ
	 * @see #INVISIBLE_1P_01
	 */
	public static final BmsChannel INVISIBLE_1P_13 = new BmsChannel(NUM_INVISIBLE_1P_13, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 不可視オブジェ
	 * @see #INVISIBLE_1P_01
	 */
	public static final BmsChannel INVISIBLE_1P_14 = new BmsChannel(NUM_INVISIBLE_1P_14, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 不可視オブジェ
	 * @see #INVISIBLE_1P_01
	 */
	public static final BmsChannel INVISIBLE_1P_15 = new BmsChannel(NUM_INVISIBLE_1P_15, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 不可視オブジェ
	 * @see #INVISIBLE_1P_01
	 */
	public static final BmsChannel INVISIBLE_1P_16 = new BmsChannel(NUM_INVISIBLE_1P_16, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 不可視オブジェ
	 * @see #INVISIBLE_1P_01
	 */
	public static final BmsChannel INVISIBLE_1P_17 = new BmsChannel(NUM_INVISIBLE_1P_17, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 不可視オブジェ
	 * @see #INVISIBLE_1P_01
	 */
	public static final BmsChannel INVISIBLE_1P_18 = new BmsChannel(NUM_INVISIBLE_1P_18, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 不可視オブジェ
	 * @see #INVISIBLE_1P_01
	 */
	public static final BmsChannel INVISIBLE_1P_19 = new BmsChannel(NUM_INVISIBLE_1P_19, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 不可視オブジェ
	 * @see #INVISIBLE_1P_01
	 */
	public static final BmsChannel INVISIBLE_1P_20 = new BmsChannel(NUM_INVISIBLE_1P_20, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 不可視オブジェ
	 * @see #INVISIBLE_1P_01
	 */
	public static final BmsChannel INVISIBLE_1P_21 = new BmsChannel(NUM_INVISIBLE_1P_21, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 不可視オブジェ
	 * @see #INVISIBLE_1P_01
	 */
	public static final BmsChannel INVISIBLE_1P_22 = new BmsChannel(NUM_INVISIBLE_1P_22, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 不可視オブジェ
	 * @see #INVISIBLE_1P_01
	 */
	public static final BmsChannel INVISIBLE_1P_23 = new BmsChannel(NUM_INVISIBLE_1P_23, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 不可視オブジェ
	 * @see #INVISIBLE_1P_01
	 */
	public static final BmsChannel INVISIBLE_1P_24 = new BmsChannel(NUM_INVISIBLE_1P_24, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 不可視オブジェ
	 * @see #INVISIBLE_1P_01
	 */
	public static final BmsChannel INVISIBLE_1P_25 = new BmsChannel(NUM_INVISIBLE_1P_25, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 不可視オブジェ
	 * @see #INVISIBLE_1P_01
	 */
	public static final BmsChannel INVISIBLE_1P_26 = new BmsChannel(NUM_INVISIBLE_1P_26, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 不可視オブジェ
	 * @see #INVISIBLE_1P_01
	 */
	public static final BmsChannel INVISIBLE_1P_27 = new BmsChannel(NUM_INVISIBLE_1P_27, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 不可視オブジェ
	 * @see #INVISIBLE_1P_01
	 */
	public static final BmsChannel INVISIBLE_1P_28 = new BmsChannel(NUM_INVISIBLE_1P_28, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 不可視オブジェ
	 * @see #INVISIBLE_1P_01
	 */
	public static final BmsChannel INVISIBLE_1P_29 = new BmsChannel(NUM_INVISIBLE_1P_29, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 不可視オブジェ
	 * @see #INVISIBLE_1P_01
	 */
	public static final BmsChannel INVISIBLE_1P_30 = new BmsChannel(NUM_INVISIBLE_1P_30, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 不可視オブジェ
	 * @see #INVISIBLE_1P_01
	 */
	public static final BmsChannel INVISIBLE_1P_31 = new BmsChannel(NUM_INVISIBLE_1P_31, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 不可視オブジェ
	 * @see #INVISIBLE_1P_01
	 */
	public static final BmsChannel INVISIBLE_1P_32 = new BmsChannel(NUM_INVISIBLE_1P_32, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 不可視オブジェ
	 * @see #INVISIBLE_1P_01
	 */
	public static final BmsChannel INVISIBLE_1P_33 = new BmsChannel(NUM_INVISIBLE_1P_33, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 不可視オブジェ
	 * @see #INVISIBLE_1P_01
	 */
	public static final BmsChannel INVISIBLE_1P_34 = new BmsChannel(NUM_INVISIBLE_1P_34, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 不可視オブジェ
	 * @see #INVISIBLE_1P_01
	 */
	public static final BmsChannel INVISIBLE_1P_35 = new BmsChannel(NUM_INVISIBLE_1P_35, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 不可視オブジェ
	 * @see #INVISIBLE_1P_01
	 */
	public static final BmsChannel INVISIBLE_2P_01 = new BmsChannel(NUM_INVISIBLE_2P_01, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 不可視オブジェ
	 * @see #INVISIBLE_1P_01
	 */
	public static final BmsChannel INVISIBLE_2P_02 = new BmsChannel(NUM_INVISIBLE_2P_02, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 不可視オブジェ
	 * @see #INVISIBLE_1P_01
	 */
	public static final BmsChannel INVISIBLE_2P_03 = new BmsChannel(NUM_INVISIBLE_2P_03, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 不可視オブジェ
	 * @see #INVISIBLE_1P_01
	 */
	public static final BmsChannel INVISIBLE_2P_04 = new BmsChannel(NUM_INVISIBLE_2P_04, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 不可視オブジェ
	 * @see #INVISIBLE_1P_01
	 */
	public static final BmsChannel INVISIBLE_2P_05 = new BmsChannel(NUM_INVISIBLE_2P_05, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 不可視オブジェ
	 * @see #INVISIBLE_1P_01
	 */
	public static final BmsChannel INVISIBLE_2P_06 = new BmsChannel(NUM_INVISIBLE_2P_06, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 不可視オブジェ
	 * @see #INVISIBLE_1P_01
	 */
	public static final BmsChannel INVISIBLE_2P_07 = new BmsChannel(NUM_INVISIBLE_2P_07, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 不可視オブジェ
	 * @see #INVISIBLE_1P_01
	 */
	public static final BmsChannel INVISIBLE_2P_08 = new BmsChannel(NUM_INVISIBLE_2P_08, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 不可視オブジェ
	 * @see #INVISIBLE_1P_01
	 */
	public static final BmsChannel INVISIBLE_2P_09 = new BmsChannel(NUM_INVISIBLE_2P_09, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 不可視オブジェ
	 * @see #INVISIBLE_1P_01
	 */
	public static final BmsChannel INVISIBLE_2P_10 = new BmsChannel(NUM_INVISIBLE_2P_10, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 不可視オブジェ
	 * @see #INVISIBLE_1P_01
	 */
	public static final BmsChannel INVISIBLE_2P_11 = new BmsChannel(NUM_INVISIBLE_2P_11, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 不可視オブジェ
	 * @see #INVISIBLE_1P_01
	 */
	public static final BmsChannel INVISIBLE_2P_12 = new BmsChannel(NUM_INVISIBLE_2P_12, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 不可視オブジェ
	 * @see #INVISIBLE_1P_01
	 */
	public static final BmsChannel INVISIBLE_2P_13 = new BmsChannel(NUM_INVISIBLE_2P_13, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 不可視オブジェ
	 * @see #INVISIBLE_1P_01
	 */
	public static final BmsChannel INVISIBLE_2P_14 = new BmsChannel(NUM_INVISIBLE_2P_14, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 不可視オブジェ
	 * @see #INVISIBLE_1P_01
	 */
	public static final BmsChannel INVISIBLE_2P_15 = new BmsChannel(NUM_INVISIBLE_2P_15, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 不可視オブジェ
	 * @see #INVISIBLE_1P_01
	 */
	public static final BmsChannel INVISIBLE_2P_16 = new BmsChannel(NUM_INVISIBLE_2P_16, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 不可視オブジェ
	 * @see #INVISIBLE_1P_01
	 */
	public static final BmsChannel INVISIBLE_2P_17 = new BmsChannel(NUM_INVISIBLE_2P_17, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 不可視オブジェ
	 * @see #INVISIBLE_1P_01
	 */
	public static final BmsChannel INVISIBLE_2P_18 = new BmsChannel(NUM_INVISIBLE_2P_18, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 不可視オブジェ
	 * @see #INVISIBLE_1P_01
	 */
	public static final BmsChannel INVISIBLE_2P_19 = new BmsChannel(NUM_INVISIBLE_2P_19, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 不可視オブジェ
	 * @see #INVISIBLE_1P_01
	 */
	public static final BmsChannel INVISIBLE_2P_20 = new BmsChannel(NUM_INVISIBLE_2P_20, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 不可視オブジェ
	 * @see #INVISIBLE_1P_01
	 */
	public static final BmsChannel INVISIBLE_2P_21 = new BmsChannel(NUM_INVISIBLE_2P_21, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 不可視オブジェ
	 * @see #INVISIBLE_1P_01
	 */
	public static final BmsChannel INVISIBLE_2P_22 = new BmsChannel(NUM_INVISIBLE_2P_22, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 不可視オブジェ
	 * @see #INVISIBLE_1P_01
	 */
	public static final BmsChannel INVISIBLE_2P_23 = new BmsChannel(NUM_INVISIBLE_2P_23, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 不可視オブジェ
	 * @see #INVISIBLE_1P_01
	 */
	public static final BmsChannel INVISIBLE_2P_24 = new BmsChannel(NUM_INVISIBLE_2P_24, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 不可視オブジェ
	 * @see #INVISIBLE_1P_01
	 */
	public static final BmsChannel INVISIBLE_2P_25 = new BmsChannel(NUM_INVISIBLE_2P_25, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 不可視オブジェ
	 * @see #INVISIBLE_1P_01
	 */
	public static final BmsChannel INVISIBLE_2P_26 = new BmsChannel(NUM_INVISIBLE_2P_26, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 不可視オブジェ
	 * @see #INVISIBLE_1P_01
	 */
	public static final BmsChannel INVISIBLE_2P_27 = new BmsChannel(NUM_INVISIBLE_2P_27, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 不可視オブジェ
	 * @see #INVISIBLE_1P_01
	 */
	public static final BmsChannel INVISIBLE_2P_28 = new BmsChannel(NUM_INVISIBLE_2P_28, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 不可視オブジェ
	 * @see #INVISIBLE_1P_01
	 */
	public static final BmsChannel INVISIBLE_2P_29 = new BmsChannel(NUM_INVISIBLE_2P_29, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 不可視オブジェ
	 * @see #INVISIBLE_1P_01
	 */
	public static final BmsChannel INVISIBLE_2P_30 = new BmsChannel(NUM_INVISIBLE_2P_30, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 不可視オブジェ
	 * @see #INVISIBLE_1P_01
	 */
	public static final BmsChannel INVISIBLE_2P_31 = new BmsChannel(NUM_INVISIBLE_2P_31, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 不可視オブジェ
	 * @see #INVISIBLE_1P_01
	 */
	public static final BmsChannel INVISIBLE_2P_32 = new BmsChannel(NUM_INVISIBLE_2P_32, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 不可視オブジェ
	 * @see #INVISIBLE_1P_01
	 */
	public static final BmsChannel INVISIBLE_2P_33 = new BmsChannel(NUM_INVISIBLE_2P_33, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 不可視オブジェ
	 * @see #INVISIBLE_1P_01
	 */
	public static final BmsChannel INVISIBLE_2P_34 = new BmsChannel(NUM_INVISIBLE_2P_34, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * 不可視オブジェ
	 * @see #INVISIBLE_1P_01
	 */
	public static final BmsChannel INVISIBLE_2P_35 = new BmsChannel(NUM_INVISIBLE_2P_35, BmsType.ARRAY36, "#wav", "", false, true);

	/** 不可視オブジェリスト */
	static final BmsChannel[] INVISIBLE_CHANNELS = {
			INVISIBLE_1P_01, INVISIBLE_1P_02, INVISIBLE_1P_03, INVISIBLE_1P_04, INVISIBLE_1P_05, INVISIBLE_1P_06,
			INVISIBLE_1P_07, INVISIBLE_1P_08, INVISIBLE_1P_09, INVISIBLE_1P_10, INVISIBLE_1P_11, INVISIBLE_1P_12,
			INVISIBLE_1P_13, INVISIBLE_1P_14, INVISIBLE_1P_15, INVISIBLE_1P_16, INVISIBLE_1P_17, INVISIBLE_1P_18,
			INVISIBLE_1P_19, INVISIBLE_1P_20, INVISIBLE_1P_21, INVISIBLE_1P_22, INVISIBLE_1P_23, INVISIBLE_1P_24,
			INVISIBLE_1P_25, INVISIBLE_1P_26, INVISIBLE_1P_27, INVISIBLE_1P_28, INVISIBLE_1P_29, INVISIBLE_1P_30,
			INVISIBLE_1P_31, INVISIBLE_1P_32, INVISIBLE_1P_33, INVISIBLE_1P_34, INVISIBLE_1P_35,
			INVISIBLE_2P_01, INVISIBLE_2P_02, INVISIBLE_2P_03, INVISIBLE_2P_04, INVISIBLE_2P_05, INVISIBLE_2P_06,
			INVISIBLE_2P_07, INVISIBLE_2P_08, INVISIBLE_2P_09, INVISIBLE_2P_10, INVISIBLE_2P_11, INVISIBLE_2P_12,
			INVISIBLE_2P_13, INVISIBLE_2P_14, INVISIBLE_2P_15, INVISIBLE_2P_16, INVISIBLE_2P_17, INVISIBLE_2P_18,
			INVISIBLE_2P_19, INVISIBLE_2P_20, INVISIBLE_2P_21, INVISIBLE_2P_22, INVISIBLE_2P_23, INVISIBLE_2P_24,
			INVISIBLE_2P_25, INVISIBLE_2P_26, INVISIBLE_2P_27, INVISIBLE_2P_28, INVISIBLE_2P_29, INVISIBLE_2P_30,
			INVISIBLE_2P_31, INVISIBLE_2P_32, INVISIBLE_2P_33, INVISIBLE_2P_34, INVISIBLE_2P_35,
	};

	/**
	 * ロングノートオブジェ
	 * <table><caption>&nbsp;</caption>
	 * <tr><th style="text-align:left;">番号</th><td>51～5Z, 61～6Z</td></tr>
	 * <tr><th style="text-align:left;">データ型</th><td>ARRAY36</td></tr>
	 * <tr><th style="text-align:left;">参照先</th><td>{@link BeMusicMeta#WAV #WAV}</td></tr>
	 * <tr><th style="text-align:left;">初期値</th><td>""</td></tr>
	 * <tr><th style="text-align:left;">複数データ</th><td>NO</td></tr>
	 * <tr><th style="text-align:left;">同一性チェック</th><td>YES</td></tr>
	 * <tr><th style="text-align:left;">説明</th>
	 * <td>MGQ形式のロングノートを表します。</td></tr>
	 * </table>
	 */
	public static final BmsChannel LONG_1P_01 = new BmsChannel(NUM_LONG_1P_01, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * ロングノートオブジェ
	 * @see #LONG_1P_01
	 */
	public static final BmsChannel LONG_1P_02 = new BmsChannel(NUM_LONG_1P_02, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * ロングノートオブジェ
	 * @see #LONG_1P_01
	 */
	public static final BmsChannel LONG_1P_03 = new BmsChannel(NUM_LONG_1P_03, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * ロングノートオブジェ
	 * @see #LONG_1P_01
	 */
	public static final BmsChannel LONG_1P_04 = new BmsChannel(NUM_LONG_1P_04, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * ロングノートオブジェ
	 * @see #LONG_1P_01
	 */
	public static final BmsChannel LONG_1P_05 = new BmsChannel(NUM_LONG_1P_05, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * ロングノートオブジェ
	 * @see #LONG_1P_01
	 */
	public static final BmsChannel LONG_1P_06 = new BmsChannel(NUM_LONG_1P_06, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * ロングノートオブジェ
	 * @see #LONG_1P_01
	 */
	public static final BmsChannel LONG_1P_07 = new BmsChannel(NUM_LONG_1P_07, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * ロングノートオブジェ
	 * @see #LONG_1P_01
	 */
	public static final BmsChannel LONG_1P_08 = new BmsChannel(NUM_LONG_1P_08, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * ロングノートオブジェ
	 * @see #LONG_1P_01
	 */
	public static final BmsChannel LONG_1P_09 = new BmsChannel(NUM_LONG_1P_09, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * ロングノートオブジェ
	 * @see #LONG_1P_01
	 */
	public static final BmsChannel LONG_1P_10 = new BmsChannel(NUM_LONG_1P_10, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * ロングノートオブジェ
	 * @see #LONG_1P_01
	 */
	public static final BmsChannel LONG_1P_11 = new BmsChannel(NUM_LONG_1P_11, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * ロングノートオブジェ
	 * @see #LONG_1P_01
	 */
	public static final BmsChannel LONG_1P_12 = new BmsChannel(NUM_LONG_1P_12, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * ロングノートオブジェ
	 * @see #LONG_1P_01
	 */
	public static final BmsChannel LONG_1P_13 = new BmsChannel(NUM_LONG_1P_13, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * ロングノートオブジェ
	 * @see #LONG_1P_01
	 */
	public static final BmsChannel LONG_1P_14 = new BmsChannel(NUM_LONG_1P_14, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * ロングノートオブジェ
	 * @see #LONG_1P_01
	 */
	public static final BmsChannel LONG_1P_15 = new BmsChannel(NUM_LONG_1P_15, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * ロングノートオブジェ
	 * @see #LONG_1P_01
	 */
	public static final BmsChannel LONG_1P_16 = new BmsChannel(NUM_LONG_1P_16, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * ロングノートオブジェ
	 * @see #LONG_1P_01
	 */
	public static final BmsChannel LONG_1P_17 = new BmsChannel(NUM_LONG_1P_17, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * ロングノートオブジェ
	 * @see #LONG_1P_01
	 */
	public static final BmsChannel LONG_1P_18 = new BmsChannel(NUM_LONG_1P_18, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * ロングノートオブジェ
	 * @see #LONG_1P_01
	 */
	public static final BmsChannel LONG_1P_19 = new BmsChannel(NUM_LONG_1P_19, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * ロングノートオブジェ
	 * @see #LONG_1P_01
	 */
	public static final BmsChannel LONG_1P_20 = new BmsChannel(NUM_LONG_1P_20, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * ロングノートオブジェ
	 * @see #LONG_1P_01
	 */
	public static final BmsChannel LONG_1P_21 = new BmsChannel(NUM_LONG_1P_21, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * ロングノートオブジェ
	 * @see #LONG_1P_01
	 */
	public static final BmsChannel LONG_1P_22 = new BmsChannel(NUM_LONG_1P_22, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * ロングノートオブジェ
	 * @see #LONG_1P_01
	 */
	public static final BmsChannel LONG_1P_23 = new BmsChannel(NUM_LONG_1P_23, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * ロングノートオブジェ
	 * @see #LONG_1P_01
	 */
	public static final BmsChannel LONG_1P_24 = new BmsChannel(NUM_LONG_1P_24, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * ロングノートオブジェ
	 * @see #LONG_1P_01
	 */
	public static final BmsChannel LONG_1P_25 = new BmsChannel(NUM_LONG_1P_25, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * ロングノートオブジェ
	 * @see #LONG_1P_01
	 */
	public static final BmsChannel LONG_1P_26 = new BmsChannel(NUM_LONG_1P_26, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * ロングノートオブジェ
	 * @see #LONG_1P_01
	 */
	public static final BmsChannel LONG_1P_27 = new BmsChannel(NUM_LONG_1P_27, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * ロングノートオブジェ
	 * @see #LONG_1P_01
	 */
	public static final BmsChannel LONG_1P_28 = new BmsChannel(NUM_LONG_1P_28, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * ロングノートオブジェ
	 * @see #LONG_1P_01
	 */
	public static final BmsChannel LONG_1P_29 = new BmsChannel(NUM_LONG_1P_29, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * ロングノートオブジェ
	 * @see #LONG_1P_01
	 */
	public static final BmsChannel LONG_1P_30 = new BmsChannel(NUM_LONG_1P_30, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * ロングノートオブジェ
	 * @see #LONG_1P_01
	 */
	public static final BmsChannel LONG_1P_31 = new BmsChannel(NUM_LONG_1P_31, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * ロングノートオブジェ
	 * @see #LONG_1P_01
	 */
	public static final BmsChannel LONG_1P_32 = new BmsChannel(NUM_LONG_1P_32, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * ロングノートオブジェ
	 * @see #LONG_1P_01
	 */
	public static final BmsChannel LONG_1P_33 = new BmsChannel(NUM_LONG_1P_33, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * ロングノートオブジェ
	 * @see #LONG_1P_01
	 */
	public static final BmsChannel LONG_1P_34 = new BmsChannel(NUM_LONG_1P_34, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * ロングノートオブジェ
	 * @see #LONG_1P_01
	 */
	public static final BmsChannel LONG_1P_35 = new BmsChannel(NUM_LONG_1P_35, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * ロングノートオブジェ
	 * @see #LONG_1P_01
	 */
	public static final BmsChannel LONG_2P_01 = new BmsChannel(NUM_LONG_2P_01, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * ロングノートオブジェ
	 * @see #LONG_1P_01
	 */
	public static final BmsChannel LONG_2P_02 = new BmsChannel(NUM_LONG_2P_02, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * ロングノートオブジェ
	 * @see #LONG_1P_01
	 */
	public static final BmsChannel LONG_2P_03 = new BmsChannel(NUM_LONG_2P_03, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * ロングノートオブジェ
	 * @see #LONG_1P_01
	 */
	public static final BmsChannel LONG_2P_04 = new BmsChannel(NUM_LONG_2P_04, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * ロングノートオブジェ
	 * @see #LONG_1P_01
	 */
	public static final BmsChannel LONG_2P_05 = new BmsChannel(NUM_LONG_2P_05, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * ロングノートオブジェ
	 * @see #LONG_1P_01
	 */
	public static final BmsChannel LONG_2P_06 = new BmsChannel(NUM_LONG_2P_06, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * ロングノートオブジェ
	 * @see #LONG_1P_01
	 */
	public static final BmsChannel LONG_2P_07 = new BmsChannel(NUM_LONG_2P_07, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * ロングノートオブジェ
	 * @see #LONG_1P_01
	 */
	public static final BmsChannel LONG_2P_08 = new BmsChannel(NUM_LONG_2P_08, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * ロングノートオブジェ
	 * @see #LONG_1P_01
	 */
	public static final BmsChannel LONG_2P_09 = new BmsChannel(NUM_LONG_2P_09, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * ロングノートオブジェ
	 * @see #LONG_1P_01
	 */
	public static final BmsChannel LONG_2P_10 = new BmsChannel(NUM_LONG_2P_10, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * ロングノートオブジェ
	 * @see #LONG_1P_01
	 */
	public static final BmsChannel LONG_2P_11 = new BmsChannel(NUM_LONG_2P_11, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * ロングノートオブジェ
	 * @see #LONG_1P_01
	 */
	public static final BmsChannel LONG_2P_12 = new BmsChannel(NUM_LONG_2P_12, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * ロングノートオブジェ
	 * @see #LONG_1P_01
	 */
	public static final BmsChannel LONG_2P_13 = new BmsChannel(NUM_LONG_2P_13, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * ロングノートオブジェ
	 * @see #LONG_1P_01
	 */
	public static final BmsChannel LONG_2P_14 = new BmsChannel(NUM_LONG_2P_14, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * ロングノートオブジェ
	 * @see #LONG_1P_01
	 */
	public static final BmsChannel LONG_2P_15 = new BmsChannel(NUM_LONG_2P_15, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * ロングノートオブジェ
	 * @see #LONG_1P_01
	 */
	public static final BmsChannel LONG_2P_16 = new BmsChannel(NUM_LONG_2P_16, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * ロングノートオブジェ
	 * @see #LONG_1P_01
	 */
	public static final BmsChannel LONG_2P_17 = new BmsChannel(NUM_LONG_2P_17, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * ロングノートオブジェ
	 * @see #LONG_1P_01
	 */
	public static final BmsChannel LONG_2P_18 = new BmsChannel(NUM_LONG_2P_18, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * ロングノートオブジェ
	 * @see #LONG_1P_01
	 */
	public static final BmsChannel LONG_2P_19 = new BmsChannel(NUM_LONG_2P_19, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * ロングノートオブジェ
	 * @see #LONG_1P_01
	 */
	public static final BmsChannel LONG_2P_20 = new BmsChannel(NUM_LONG_2P_20, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * ロングノートオブジェ
	 * @see #LONG_1P_01
	 */
	public static final BmsChannel LONG_2P_21 = new BmsChannel(NUM_LONG_2P_21, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * ロングノートオブジェ
	 * @see #LONG_1P_01
	 */
	public static final BmsChannel LONG_2P_22 = new BmsChannel(NUM_LONG_2P_22, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * ロングノートオブジェ
	 * @see #LONG_1P_01
	 */
	public static final BmsChannel LONG_2P_23 = new BmsChannel(NUM_LONG_2P_23, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * ロングノートオブジェ
	 * @see #LONG_1P_01
	 */
	public static final BmsChannel LONG_2P_24 = new BmsChannel(NUM_LONG_2P_24, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * ロングノートオブジェ
	 * @see #LONG_1P_01
	 */
	public static final BmsChannel LONG_2P_25 = new BmsChannel(NUM_LONG_2P_25, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * ロングノートオブジェ
	 * @see #LONG_1P_01
	 */
	public static final BmsChannel LONG_2P_26 = new BmsChannel(NUM_LONG_2P_26, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * ロングノートオブジェ
	 * @see #LONG_1P_01
	 */
	public static final BmsChannel LONG_2P_27 = new BmsChannel(NUM_LONG_2P_27, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * ロングノートオブジェ
	 * @see #LONG_1P_01
	 */
	public static final BmsChannel LONG_2P_28 = new BmsChannel(NUM_LONG_2P_28, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * ロングノートオブジェ
	 * @see #LONG_1P_01
	 */
	public static final BmsChannel LONG_2P_29 = new BmsChannel(NUM_LONG_2P_29, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * ロングノートオブジェ
	 * @see #LONG_1P_01
	 */
	public static final BmsChannel LONG_2P_30 = new BmsChannel(NUM_LONG_2P_30, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * ロングノートオブジェ
	 * @see #LONG_1P_01
	 */
	public static final BmsChannel LONG_2P_31 = new BmsChannel(NUM_LONG_2P_31, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * ロングノートオブジェ
	 * @see #LONG_1P_01
	 */
	public static final BmsChannel LONG_2P_32 = new BmsChannel(NUM_LONG_2P_32, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * ロングノートオブジェ
	 * @see #LONG_1P_01
	 */
	public static final BmsChannel LONG_2P_33 = new BmsChannel(NUM_LONG_2P_33, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * ロングノートオブジェ
	 * @see #LONG_1P_01
	 */
	public static final BmsChannel LONG_2P_34 = new BmsChannel(NUM_LONG_2P_34, BmsType.ARRAY36, "#wav", "", false, true);
	/**
	 * ロングノートオブジェ
	 * @see #LONG_1P_01
	 */
	public static final BmsChannel LONG_2P_35 = new BmsChannel(NUM_LONG_2P_35, BmsType.ARRAY36, "#wav", "", false, true);

	/** ロングノートオブジェリスト */
	static final BmsChannel[] LONG_CHANNELS = {
			LONG_1P_01, LONG_1P_02, LONG_1P_03, LONG_1P_04, LONG_1P_05, LONG_1P_06, LONG_1P_07,
			LONG_1P_08, LONG_1P_09, LONG_1P_10, LONG_1P_11, LONG_1P_12, LONG_1P_13, LONG_1P_14,
			LONG_1P_15, LONG_1P_16, LONG_1P_17, LONG_1P_18, LONG_1P_19, LONG_1P_20, LONG_1P_21,
			LONG_1P_22, LONG_1P_23, LONG_1P_24, LONG_1P_25, LONG_1P_26, LONG_1P_27, LONG_1P_28,
			LONG_1P_29, LONG_1P_30, LONG_1P_31, LONG_1P_32, LONG_1P_33, LONG_1P_34, LONG_1P_35,
			LONG_2P_01, LONG_2P_02, LONG_2P_03, LONG_2P_04, LONG_2P_05, LONG_2P_06, LONG_2P_07,
			LONG_2P_08, LONG_2P_09, LONG_2P_10, LONG_2P_11, LONG_2P_12, LONG_2P_13, LONG_2P_14,
			LONG_2P_15, LONG_2P_16, LONG_2P_17, LONG_2P_18, LONG_2P_19, LONG_2P_20, LONG_2P_21,
			LONG_2P_22, LONG_2P_23, LONG_2P_24, LONG_2P_25, LONG_2P_26, LONG_2P_27, LONG_2P_28,
			LONG_2P_29, LONG_2P_30, LONG_2P_31, LONG_2P_32, LONG_2P_33, LONG_2P_34, LONG_2P_35,
	};

	/**
	 * 地雷オブジェ
	 * <table><caption>&nbsp;</caption>
	 * <tr><th style="text-align:left;">番号</th><td>D1～D9, E1～E9</td></tr>
	 * <tr><th style="text-align:left;">データ型</th><td>ARRAY36</td></tr>
	 * <tr><th style="text-align:left;">参照先</th><td>無し</td></tr>
	 * <tr><th style="text-align:left;">初期値</th><td>""</td></tr>
	 * <tr><th style="text-align:left;">複数データ</th><td>NO</td></tr>
	 * <tr><th style="text-align:left;">同一性チェック</th><td>YES</td></tr>
	 * <tr><th style="text-align:left;">説明</th>
	 * <td>地雷ノートを表します。当チャンネルの定義はノート数にはカウントされません。</td></tr>
	 * </table>
	 */
	public static final BmsChannel LANDMINE_1P_01 = new BmsChannel(NUM_LANDMINE_1P_01, BmsType.ARRAY36, null, "", false, true);
	/**
	 * 地雷オブジェ
	 * @see #LANDMINE_1P_01
	 */
	public static final BmsChannel LANDMINE_1P_02 = new BmsChannel(NUM_LANDMINE_1P_02, BmsType.ARRAY36, null, "", false, true);
	/**
	 * 地雷オブジェ
	 * @see #LANDMINE_1P_01
	 */
	public static final BmsChannel LANDMINE_1P_03 = new BmsChannel(NUM_LANDMINE_1P_03, BmsType.ARRAY36, null, "", false, true);
	/**
	 * 地雷オブジェ
	 * @see #LANDMINE_1P_01
	 */
	public static final BmsChannel LANDMINE_1P_04 = new BmsChannel(NUM_LANDMINE_1P_04, BmsType.ARRAY36, null, "", false, true);
	/**
	 * 地雷オブジェ
	 * @see #LANDMINE_1P_01
	 */
	public static final BmsChannel LANDMINE_1P_05 = new BmsChannel(NUM_LANDMINE_1P_05, BmsType.ARRAY36, null, "", false, true);
	/**
	 * 地雷オブジェ
	 * @see #LANDMINE_1P_01
	 */
	public static final BmsChannel LANDMINE_1P_06 = new BmsChannel(NUM_LANDMINE_1P_06, BmsType.ARRAY36, null, "", false, true);
	/**
	 * 地雷オブジェ
	 * @see #LANDMINE_1P_01
	 */
	public static final BmsChannel LANDMINE_1P_07 = new BmsChannel(NUM_LANDMINE_1P_07, BmsType.ARRAY36, null, "", false, true);
	/**
	 * 地雷オブジェ
	 * @see #LANDMINE_1P_01
	 */
	public static final BmsChannel LANDMINE_1P_08 = new BmsChannel(NUM_LANDMINE_1P_08, BmsType.ARRAY36, null, "", false, true);
	/**
	 * 地雷オブジェ
	 * @see #LANDMINE_1P_01
	 */
	public static final BmsChannel LANDMINE_1P_09 = new BmsChannel(NUM_LANDMINE_1P_09, BmsType.ARRAY36, null, "", false, true);
	/**
	 * 地雷オブジェ
	 * @see #LANDMINE_1P_01
	 */
	public static final BmsChannel LANDMINE_2P_01 = new BmsChannel(NUM_LANDMINE_2P_01, BmsType.ARRAY36, null, "", false, true);
	/**
	 * 地雷オブジェ
	 * @see #LANDMINE_1P_01
	 */
	public static final BmsChannel LANDMINE_2P_02 = new BmsChannel(NUM_LANDMINE_2P_02, BmsType.ARRAY36, null, "", false, true);
	/**
	 * 地雷オブジェ
	 * @see #LANDMINE_1P_01
	 */
	public static final BmsChannel LANDMINE_2P_03 = new BmsChannel(NUM_LANDMINE_2P_03, BmsType.ARRAY36, null, "", false, true);
	/**
	 * 地雷オブジェ
	 * @see #LANDMINE_1P_01
	 */
	public static final BmsChannel LANDMINE_2P_04 = new BmsChannel(NUM_LANDMINE_2P_04, BmsType.ARRAY36, null, "", false, true);
	/**
	 * 地雷オブジェ
	 * @see #LANDMINE_1P_01
	 */
	public static final BmsChannel LANDMINE_2P_05 = new BmsChannel(NUM_LANDMINE_2P_05, BmsType.ARRAY36, null, "", false, true);
	/**
	 * 地雷オブジェ
	 * @see #LANDMINE_1P_01
	 */
	public static final BmsChannel LANDMINE_2P_06 = new BmsChannel(NUM_LANDMINE_2P_06, BmsType.ARRAY36, null, "", false, true);
	/**
	 * 地雷オブジェ
	 * @see #LANDMINE_1P_01
	 */
	public static final BmsChannel LANDMINE_2P_07 = new BmsChannel(NUM_LANDMINE_2P_07, BmsType.ARRAY36, null, "", false, true);
	/**
	 * 地雷オブジェ
	 * @see #LANDMINE_1P_01
	 */
	public static final BmsChannel LANDMINE_2P_08 = new BmsChannel(NUM_LANDMINE_2P_08, BmsType.ARRAY36, null, "", false, true);
	/**
	 * 地雷オブジェ
	 * @see #LANDMINE_1P_01
	 */
	public static final BmsChannel LANDMINE_2P_09 = new BmsChannel(NUM_LANDMINE_2P_09, BmsType.ARRAY36, null, "", false, true);

	/** 地雷オブジェリスト */
	static final BmsChannel[] LANDMINE_CHANNELS = {
			LANDMINE_1P_01, LANDMINE_1P_02, LANDMINE_1P_03, LANDMINE_1P_04, LANDMINE_1P_05,
			LANDMINE_1P_06, LANDMINE_1P_07, LANDMINE_1P_08, LANDMINE_1P_09,
			LANDMINE_2P_01, LANDMINE_2P_02, LANDMINE_2P_03, LANDMINE_2P_04, LANDMINE_2P_05,
			LANDMINE_2P_06, LANDMINE_2P_07, LANDMINE_2P_08, LANDMINE_2P_09,
	};

	/**
	 * チャンネル番号生成用の内部メソッド。
	 * @param num チャンネル番号の文字列表現
	 * @return チャンネル番号
	 */
	private static final int ch(String num) {
		return Integer.parseInt(num, 36);
	}
}
