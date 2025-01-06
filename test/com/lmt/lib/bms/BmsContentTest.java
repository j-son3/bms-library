package com.lmt.lib.bms;

import java.util.HashMap;

public class BmsContentTest {
	// ※注意：このファイルでのテストコード記述を禁止とする。

	protected static final int CN_INT = Integer.valueOf("Y0", 36);
	protected static final int CN_NUM = Integer.valueOf("Y1", 36);
	protected static final int CN_STR = Integer.valueOf("Y2", 36);
	protected static final int CN_B16 = Integer.valueOf("Y3", 36);
	protected static final int CN_B36 = Integer.valueOf("Y4", 36);
	protected static final int CN_A16 = Integer.valueOf("Y5", 36);
	protected static final int CN_A36 = Integer.valueOf("Y6", 36);
	protected static final int CN_OBJ = BmsSpec.USER_CHANNEL_MIN + 0;

	protected static final int CN_INTS = Integer.valueOf("Y7", 36);
	protected static final int CN_NUMS = Integer.valueOf("Y8", 36);
	protected static final int CN_STRS = Integer.valueOf("Y9", 36);
	protected static final int CN_B16S = Integer.valueOf("YA", 36);
	protected static final int CN_B36S = Integer.valueOf("YB", 36);
	protected static final int CN_A16S = Integer.valueOf("YC", 36);
	protected static final int CN_A36S = Integer.valueOf("YD", 36);
	protected static final int CN_OBJS = BmsSpec.USER_CHANNEL_MIN + 1;

	protected static final int CN_A36_INT = Integer.valueOf("Z0", 36);
	protected static final int CN_A36_NUM = Integer.valueOf("Z1", 36);
	protected static final int CN_A36_STR = Integer.valueOf("Z2", 36);
	protected static final int CN_A36_B16 = Integer.valueOf("Z3", 36);
	protected static final int CN_A36_B36 = Integer.valueOf("Z4", 36);
	protected static final int CN_A36_A16 = Integer.valueOf("Z5", 36);
	protected static final int CN_A36_A36 = Integer.valueOf("Z6", 36);
	protected static final int CN_A36S_INT = Integer.valueOf("Z7", 36);
	protected static final int CN_A36S_NUM = Integer.valueOf("Z8", 36);
	protected static final int CN_A36S_STR = Integer.valueOf("Z9", 36);
	protected static final int CN_A36S_B16 = Integer.valueOf("ZA", 36);
	protected static final int CN_A36S_B36 = Integer.valueOf("ZB", 36);
	protected static final int CN_A36S_A16 = Integer.valueOf("ZC", 36);
	protected static final int CN_A36S_A36 = Integer.valueOf("ZD", 36);

	protected static BmsSpec spec() {
		var smetas = new HashMap<String, BmsMeta>();
		// BMルール
		smetas.put("#base", BmsMeta.single("#base", BmsType.INTEGER, "36", 0, true));
		smetas.put("#genre", BmsMeta.single("#genre", BmsType.STRING, "", 0, true));
		smetas.put("#title", BmsMeta.single("#title", BmsType.STRING, "", 0, true));
		smetas.put("#subtitle", BmsMeta.single("#subtitle", BmsType.STRING, "", 0, true));
		smetas.put("#artist", BmsMeta.single("#artist", BmsType.STRING, "", 0, true));
		smetas.put("#bpm", BmsMeta.single("#bpm", BmsType.FLOAT, "130.0", 0, true));
		smetas.put("#player", new BmsMeta("#player", BmsUnit.SINGLE, BmsType.INTEGER, "1", 0, true));
		smetas.put("#rank", new BmsMeta("#rank", BmsUnit.SINGLE, BmsType.INTEGER, "2", 0, true));
		smetas.put("#total", new BmsMeta("#total", BmsUnit.SINGLE, BmsType.FLOAT, "160.0", 0, true));
		smetas.put("#stagefile", new BmsMeta("#stagefile", BmsUnit.SINGLE, BmsType.STRING, "", 0, false));
		smetas.put("#banner", new BmsMeta("#banner", BmsUnit.SINGLE, BmsType.STRING, "", 0, false));
		smetas.put("#backbmp", new BmsMeta("#backbmp", BmsUnit.SINGLE, BmsType.STRING, "", 0, false));
		smetas.put("#playlevel", new BmsMeta("#playlevel", BmsUnit.SINGLE, BmsType.INTEGER, "3", 0, false));
		smetas.put("#difficulty", new BmsMeta("#difficulty", BmsUnit.SINGLE, BmsType.INTEGER, "5", 0, true));
		smetas.put("#lntype", new BmsMeta("#lntype", BmsUnit.SINGLE, BmsType.INTEGER, "1", 0, false));	// エラーにしないための措置
		smetas.put("#lnobj", new BmsMeta("#lnobj", BmsUnit.SINGLE, BmsType.BASE36, "00", 0, true));
		smetas.put("#%url", new BmsMeta("%url", BmsUnit.SINGLE, BmsType.STRING, "", 0, false));
		// テスト用
		smetas.put("#single_integer", new BmsMeta("#single_integer", BmsUnit.SINGLE, BmsType.INTEGER, "123456789", 0, false));
		smetas.put("#single_float", new BmsMeta("#single_float", BmsUnit.SINGLE, BmsType.FLOAT, "1234.56789", 0, false));
		smetas.put("#single_string", new BmsMeta("#single_string", BmsUnit.SINGLE, BmsType.STRING, "DEFAULT_STRING", 0, false));
		smetas.put("#single_base16", new BmsMeta("#single_base16", BmsUnit.SINGLE, BmsType.BASE16, "9F", 0, false));
		smetas.put("#single_base36", new BmsMeta("#single_base36", BmsUnit.SINGLE, BmsType.BASE36, "Z0", 0, false));
		smetas.put("#single_array16", new BmsMeta("#single_array16", BmsUnit.SINGLE, BmsType.ARRAY16, "0123456789", 0, false));
		smetas.put("#single_array36", new BmsMeta("#single_array36", BmsUnit.SINGLE, BmsType.ARRAY36, "ABCDEFGHIJ", 0, false));
		smetas.put("#single_object", new BmsMeta("#single_object", BmsUnit.SINGLE, BmsType.OBJECT, null, 0, false));

		var mmetas = new HashMap<String, BmsMeta>();
		// BMルール
		mmetas.put("#subartist", BmsMeta.multiple("#subartist", BmsType.STRING, "", 0, true));
		mmetas.put("#comment", new BmsMeta("#comment", BmsUnit.MULTIPLE, BmsType.STRING, "", 0, false));
		// テスト用
		mmetas.put("#multiple_integer", new BmsMeta("#multiple_integer", BmsUnit.MULTIPLE, BmsType.INTEGER, "123456789", 0, false));
		mmetas.put("#multiple_float", new BmsMeta("#multiple_float", BmsUnit.MULTIPLE, BmsType.FLOAT, "1234.56789", 0, false));
		mmetas.put("#multiple_string", new BmsMeta("#multiple_string", BmsUnit.MULTIPLE, BmsType.STRING, "DEFAULT_STRING", 0, false));
		mmetas.put("#multiple_base16", new BmsMeta("#multiple_base16", BmsUnit.MULTIPLE, BmsType.BASE16, "9F", 0, false));
		mmetas.put("#multiple_base36", new BmsMeta("#multiple_base36", BmsUnit.MULTIPLE, BmsType.BASE36, "Z0", 0, false));
		mmetas.put("#multiple_array16", new BmsMeta("#multiple_array16", BmsUnit.MULTIPLE, BmsType.ARRAY16, "0123456789", 0, false));
		mmetas.put("#multiple_array36", new BmsMeta("#multiple_array36", BmsUnit.MULTIPLE, BmsType.ARRAY36, "ABCDEFGHIJ", 0, false));
		mmetas.put("#multiple_object", new BmsMeta("#multiple_object", BmsUnit.MULTIPLE, BmsType.OBJECT, null, 0, false));

		var imetas = new HashMap<String, BmsMeta>();
		// BMルール
		imetas.put("#wav", new BmsMeta("#wav", BmsUnit.INDEXED, BmsType.BASE36, "00", 0, false));
		imetas.put("#bmp", new BmsMeta("#bmp", BmsUnit.INDEXED, BmsType.BASE36, "00", 0, false));
		imetas.put("#bpm", new BmsMeta("#bpm", BmsUnit.INDEXED, BmsType.FLOAT, "130.0", 0, false));
		imetas.put("#stop", new BmsMeta("#stop", BmsUnit.INDEXED, BmsType.FLOAT, "0", 0, false));
		// テスト用
		imetas.put("#indexed_integer", new BmsMeta("#indexed_integer", BmsUnit.INDEXED, BmsType.INTEGER, "123456789", 0, false));
		imetas.put("#indexed_float", new BmsMeta("#indexed_float", BmsUnit.INDEXED, BmsType.FLOAT, "1234.56789", 0, false));
		imetas.put("#indexed_string", new BmsMeta("#indexed_string", BmsUnit.INDEXED, BmsType.STRING, "DEFAULT_STRING", 0, false));
		imetas.put("#indexed_base16", new BmsMeta("#indexed_base16", BmsUnit.INDEXED, BmsType.BASE16, "9F", 0, false));
		imetas.put("#indexed_base36", new BmsMeta("#indexed_base36", BmsUnit.INDEXED, BmsType.BASE36, "Z0", 0, false));
		imetas.put("#indexed_array16", new BmsMeta("#indexed_array16", BmsUnit.INDEXED, BmsType.ARRAY16, "0123456789", 0, false));
		imetas.put("#indexed_array36", new BmsMeta("#indexed_array36", BmsUnit.INDEXED, BmsType.ARRAY36, "ABCDEFGHIJ", 0, false));
		imetas.put("#indexed_object", new BmsMeta("#indexed_object", BmsUnit.INDEXED, BmsType.OBJECT, null, 0, false));

		BmsChannel ch = null;
		var chs = new HashMap<Integer, BmsChannel>();
		chs.put(1, new BmsChannel(1, BmsType.ARRAY36, "#wav", "", true, false));   // BGM
		ch = new BmsChannel(2, BmsType.FLOAT, null, "1.0", false, true); // 小節長変更
		ch.setRelatedFlag(1);
		chs.put(2, ch);
		ch = new BmsChannel(3, BmsType.ARRAY16, null, "", false, true);     // BPM変更
		chs.put(3, ch);
		ch = new BmsChannel(8, BmsType.ARRAY36, "#bpm", "", false, true);   // BPM変更
		ch.setRelatedFlag(2);
		chs.put(8, ch);
		ch = new BmsChannel(9, BmsType.ARRAY36, "#stop", "", false, true);  // 譜面停止
		ch.setRelatedFlag(3);
		chs.put(9, ch);
		for (var i = 0; i < 9; i++) {
			chs.put(11 + i, new BmsChannel(11 + i, BmsType.ARRAY36, "#wav", "00", false, true));  // 1P可視
			chs.put(21 + i, new BmsChannel(21 + i, BmsType.ARRAY36, "#wav", "00", false, true));  // 1P不可視
			chs.put(31 + i, new BmsChannel(31 + i, BmsType.ARRAY36, "#wav", "00", false, true));  // 2P可視
			chs.put(41 + i, new BmsChannel(41 + i, BmsType.ARRAY36, "#wav", "00", false, true));  // 2P不可視
		}

		// テスト用の非標準定義
		// 重複可能チャンネル
		chs.put(CN_INT, new BmsChannel(CN_INT, BmsType.INTEGER, null, "100", true, false));
		chs.put(CN_NUM, new BmsChannel(CN_NUM, BmsType.FLOAT, null, "200.0", true, false));
		chs.put(CN_STR, new BmsChannel(CN_STR, BmsType.STRING, null, "String", true, false));
		chs.put(CN_B16, new BmsChannel(CN_B16, BmsType.BASE16, null, "7F", true, false));
		chs.put(CN_B36, new BmsChannel(CN_B36, BmsType.BASE36, null, "GZ", true, false));
		chs.put(CN_A16, new BmsChannel(CN_A16, BmsType.ARRAY16, null, "AABBCCDDEEFF", true, false));
		chs.put(CN_A36, new BmsChannel(CN_A36, BmsType.ARRAY36, null, "VVWWXXYYZZ", true, false));
		chs.put(CN_OBJ, new BmsChannel(CN_OBJ, BmsType.OBJECT, null, null, true, false));

		// 重複不可チャンネル
		chs.put(CN_INTS, new BmsChannel(CN_INTS, BmsType.INTEGER, null, "100", false, false));
		chs.put(CN_NUMS, new BmsChannel(CN_NUMS, BmsType.FLOAT, null, "200.0", false, false));
		chs.put(CN_STRS, new BmsChannel(CN_STRS, BmsType.STRING, null, "String", false, false));
		chs.put(CN_B16S, new BmsChannel(CN_B16S, BmsType.BASE16, null, "7F", false, false));
		chs.put(CN_B36S, new BmsChannel(CN_B36S, BmsType.BASE36, null, "GZ", false, false));
		chs.put(CN_A16S, new BmsChannel(CN_A16S, BmsType.ARRAY16, null, "AABBCCDDEEFF", false, false));
		chs.put(CN_A36S, new BmsChannel(CN_A36S, BmsType.ARRAY36, null, "VVWWXXYYZZ", false, false));
		chs.put(CN_OBJS, new BmsChannel(CN_OBJS, BmsType.OBJECT, null, null, false, false));

		// 参照メタ情報あり
		chs.put(CN_A36_INT, new BmsChannel(CN_A36_INT, BmsType.ARRAY36, "#indexed_integer", "00", true, false));
		chs.put(CN_A36_NUM, new BmsChannel(CN_A36_NUM, BmsType.ARRAY36, "#indexed_float", "00", true, false));
		chs.put(CN_A36_STR, new BmsChannel(CN_A36_STR, BmsType.ARRAY36, "#indexed_string", "00", true, false));
		chs.put(CN_A36_B16, new BmsChannel(CN_A36_B16, BmsType.ARRAY36, "#indexed_base16", "00", true, false));
		chs.put(CN_A36_B36, new BmsChannel(CN_A36_B36, BmsType.ARRAY36, "#indexed_base36", "00", true, false));
		chs.put(CN_A36_A16, new BmsChannel(CN_A36_A16, BmsType.ARRAY36, "#indexed_array16", "00", true, false));
		chs.put(CN_A36_A36, new BmsChannel(CN_A36_A36, BmsType.ARRAY36, "#indexed_array36", "00", true, false));
		chs.put(CN_A36S_INT, new BmsChannel(CN_A36S_INT, BmsType.ARRAY36, "#indexed_integer", "00", false, false));
		chs.put(CN_A36S_NUM, new BmsChannel(CN_A36S_NUM, BmsType.ARRAY36, "#indexed_float", "00", false, false));
		chs.put(CN_A36S_STR, new BmsChannel(CN_A36S_STR, BmsType.ARRAY36, "#indexed_string", "00", false, false));
		chs.put(CN_A36S_B16, new BmsChannel(CN_A36S_B16, BmsType.ARRAY36, "#indexed_base16", "00", false, false));
		chs.put(CN_A36S_B36, new BmsChannel(CN_A36S_B36, BmsType.ARRAY36, "#indexed_base36", "00", false, false));
		chs.put(CN_A36S_A16, new BmsChannel(CN_A36S_A16, BmsType.ARRAY36, "#indexed_array16", "00", false, false));
		chs.put(CN_A36S_A36, new BmsChannel(CN_A36S_A36, BmsType.ARRAY36, "#indexed_array36", "00", false, false));

		var bpm = smetas.get("#bpm");
		bpm.setIsInitialBpm();
		var base = smetas.get("#base");
		base.setIsBaseChanger();

		var bpmChs = new BmsChannel[] { chs.get(8), chs.get(3) };
		var stopChs = new BmsChannel[] { chs.get(9) };
		imetas.get("#bpm").setIsReferenceBpm();
		imetas.get("#stop").setIsReferenceStop();

		return new BmsSpec(smetas, mmetas, imetas, chs, bpm, base, chs.get(2), bpmChs, stopChs);
	}
}
