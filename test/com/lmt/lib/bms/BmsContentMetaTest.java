package com.lmt.lib.bms;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

public class BmsContentMetaTest extends BmsContentTest {
	// setSingleMeta(String, Object)
	// 正常
	@Test
	public void testSetSingleMeta_001() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.setSingleMeta("#title", "song");
		c.setSingleMeta("#bpm", 150.8);
		c.setSingleMeta("#player", 3);
		c.setSingleMeta("#lnobj", "AZ");
		c.setSingleMeta("#single_base16", "AF");
		c.setSingleMeta("#single_array16", "0088AAFF");
		c.setSingleMeta("#single_array36", "0099FFKKPPZZ");
		c.endEdit();
		assertEquals("song", (String)c.getSingleMeta("#title"));
		assertEquals(150.8, (double)c.getSingleMeta("#bpm"), 0.0001);
		assertEquals(3L, (long)c.getSingleMeta("#player"));
		assertEquals(Long.parseLong("AZ", 36), (long)c.getSingleMeta("#lnobj"));
		assertEquals(Long.parseLong("AF", 16), (long)c.getSingleMeta("#single_base16"));
		assertEquals(new BmsArray("0088aaff", 16), (BmsArray)c.getSingleMeta("#single_array16"));
		assertEquals(new BmsArray("0099ffkkppzz", 36), (BmsArray)c.getSingleMeta("#single_array36"));

		// nullを指定すると値が消去され、そのメタ情報の初期値を返す
		c.beginEdit();
		c.setSingleMeta("#bpm", null);
		c.endEdit();
		assertEquals(
				(double)c.getSpec().getSingleMeta("#bpm").getDefaultValue(),
				(double)c.getSingleMeta("#bpm"), 0.0);
	}

	// setSingleMeta(String, Object)
	// IllegalStateException 動作モードが編集モードではない
	@Test
	public void testSetSingleMeta_002() {
		var c = new BmsContent(spec());
		assertThrows(IllegalStateException.class, () -> c.setSingleMeta("#title", "hoge"));
	}

	// setSingleMeta(String, Object)
	// NullPointerException nameがnull
	@Test
	public void testSetSingleMeta_003() {
		var c = new BmsContent(spec());
		c.beginEdit();
		assertThrows(NullPointerException.class, () -> c.setSingleMeta(null, "hoge"));
	}

	// setSingleMeta(String, Object)
	// IllegalArgumentException 名称に合致するメタ情報が存在しない
	@Test
	public void testSetSingleMeta_004() {
		var c = new BmsContent(spec());
		c.beginEdit();
		assertThrows(IllegalArgumentException.class, () -> c.setSingleMeta("#unknown_meta_name", "hoge"));
	}

	// setSingleMeta(String, Object)
	// ClassCastException valueが設定先メタ情報のデータ型に変換できない
	@Test
	public void testSetSingleMeta_005() {
		var c = new BmsContent(spec());
		c.beginEdit();
		assertThrows(ClassCastException.class, () -> c.setSingleMeta("#bpm", "not_number"));
	}

	// setSingleMeta(String, Object)
	// 正常：初期BPMにBMSライブラリ仕様の最小値・最大値が設定できること
	// IllegalArgumentException 初期BPMにBMSライブラリ仕様の最小値未満・最大値超過を設定しようとした
	@Test
	public void testSetSingleMeta_InitialBpmWithinSpec() {
		var e = IllegalArgumentException.class;
		var c = new BmsContent(spec());
		c.beginEdit();

		// BPM最小値を設定可能であること
		c.setSingleMeta("#bpm", BmsSpec.BPM_MIN);
		assertEquals(BmsSpec.BPM_MIN, ((Number)c.getSingleMeta("#bpm")).doubleValue(), 0.0);

		// BPM最大値を設定可能であること
		c.setSingleMeta("#bpm", BmsSpec.BPM_MAX);
		assertEquals(BmsSpec.BPM_MAX, ((Number)c.getSingleMeta("#bpm")).doubleValue(), 0.0);

		// BPM最小値未満が設定不可であること
		assertThrows(e, () -> c.setSingleMeta("#bpm", Math.nextDown(BmsSpec.BPM_MIN)));

		// BPM最大値超過が設定不可であること
		assertThrows(e, () -> c.setSingleMeta("#bpm", Math.nextUp(BmsSpec.BPM_MAX)));

		c.endEdit();
	}

	// setSingleMeta(String, Object)
	// 基数選択メタ情報にnull, 16, 36, 62が設定できること
	@Test
	public void testSetSingleMeta_BaseChanger_Normal() {
		var s = spec();
		var c = new BmsContent(s);
		c.edit(() -> c.setSingleMeta("#base", 16L));
		assertEquals(16L, (long)c.getSingleMeta("#base"));
		c.edit(() -> c.setSingleMeta("#base", 36L));
		assertEquals(36L, (long)c.getSingleMeta("#base"));
		c.edit(() -> c.setSingleMeta("#base", 62L));
		assertEquals(62L, (long)c.getSingleMeta("#base"));
		c.edit(() -> c.setSingleMeta("#base", null));
		assertFalse(c.containsSingleMeta("#base"));
		assertEquals((long)s.getBaseChangerMeta().getDefaultValue(), (long)c.getSingleMeta("#base"));
	}

	// setSingleMeta(String, Object)
	// IllegalArgumentException 基数選択メタ情報にnull, 16, 36, 62以外の値を設定しようとした
	@Test
	public void testSetSingleMeta_BaseChanger_WrongBase() {
		var c = new BmsContent(spec());
		c.beginEdit();
		assertThrows(IllegalArgumentException.class, () -> c.setSingleMeta("#base", 10L));
	}

	// setMultipleMeta(String, int, Object)
	// 正常
	@Test
	public void testSetMultipleMeta_001() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.setMultipleMeta("#multiple_integer", 0, 0);
		c.setMultipleMeta("#multiple_float", 0, 200.123);
		c.setMultipleMeta("#multiple_string", 0, "hoge");
		c.setMultipleMeta("#multiple_base16", 0, 0xaf);
		c.setMultipleMeta("#multiple_base36", 0, Integer.parseInt("gz", 36));
		c.setMultipleMeta("#multiple_array16", 0, "0088aaff");
		c.setMultipleMeta("#multiple_array36", 0, "0099ffkkppzz");
		c.endEdit();
		assertEquals(0L, (long)c.getMultipleMeta("#multiple_integer", 0));
		assertEquals(200.123, (double)c.getMultipleMeta("#multiple_float", 0), 0.0001);
		assertEquals("hoge", (String)c.getMultipleMeta("#multiple_string", 0));
		assertEquals(0xafL, (long)c.getMultipleMeta("#multiple_base16", 0));
		assertEquals(Long.parseLong("gz", 36), (long)c.getMultipleMeta("#multiple_base36", 0));
		assertEquals(new BmsArray("0088aaff", 16), (BmsArray)c.getMultipleMeta("#multiple_array16", 0));
		assertEquals(new BmsArray("0099ffkkppzz", 36), (BmsArray)c.getMultipleMeta("#multiple_array36", 0));

		// ひとつのメタ情報に複数のデータを入れる
		c = new BmsContent(spec());
		c.beginEdit();
		c.setMultipleMeta("#multiple_string", 0, "hoge");
		c.setMultipleMeta("#multiple_string", 1, "hage");
		c.setMultipleMeta("#multiple_string", 2, "hige");
		c.endEdit();
		assertEquals("hoge", (String)c.getMultipleMeta("#multiple_string", 0));
		assertEquals("hage", (String)c.getMultipleMeta("#multiple_string", 1));
		assertEquals("hige", (String)c.getMultipleMeta("#multiple_string", 2));

		// 歯抜けの領域にはそのメタ情報の初期値が入る
		c = new BmsContent(spec());
		c.beginEdit();
		c.setMultipleMeta("#multiple_integer", 0, 100);
		c.setMultipleMeta("#multiple_integer", 3, 500);
		c.endEdit();
		assertEquals(4, c.getMultipleMetaCount("#multiple_integer"));
		assertEquals(100L, (long)c.getMultipleMeta("#multiple_integer", 0));
		assertEquals(
				(long)c.getSpec().getMultipleMeta("#multiple_integer").getDefaultValue(),
				(long)c.getMultipleMeta("#multiple_integer", 1));
		assertEquals(
				(long)c.getSpec().getMultipleMeta("#multiple_integer").getDefaultValue(),
				(long)c.getMultipleMeta("#multiple_integer", 2));
		assertEquals(500L, (long)c.getMultipleMeta("#multiple_integer", 3));

		// nullを指定すると値が消去され、そのメタ情報の初期値を返す
		c = new BmsContent(spec());
		c.beginEdit();
		c.setMultipleMeta("#multiple_integer", 0, 300);
		c.setMultipleMeta("#multiple_integer", 0, null);
		c.endEdit();
		assertEquals(
				(long)c.getSpec().getMultipleMeta("#multiple_integer").getDefaultValue(),
				(long)c.getMultipleMeta("#multiple_integer", 0));

		// nullで消去した値より後ろの値は左詰めされる
		c = new BmsContent(spec());
		c.beginEdit();
		c.setMultipleMeta("#multiple_integer", 0, 100);
		c.setMultipleMeta("#multiple_integer", 1, 300);  // ←こいつを消す
		c.setMultipleMeta("#multiple_integer", 2, 500);
		c.setMultipleMeta("#multiple_integer", 3, 700);
		c.setMultipleMeta("#multiple_integer", 1, null);
		c.endEdit();
		assertEquals(3, c.getMultipleMetaCount("#multiple_integer"));
		assertEquals(500L, (long)c.getMultipleMeta("#multiple_integer", 1));
		assertEquals(700L, (long)c.getMultipleMeta("#multiple_integer", 2));
	}

	// setMultipleMeta(String, int, Object)
	// IllegalStateException 動作モードが編集モードではない
	@Test
	public void testSetMultipleMeta_002() {
		var c = new BmsContent(spec());
		assertThrows(IllegalStateException.class, () -> c.setMultipleMeta("#multiple_integer", 0, 0));
	}

	// setMultipleMeta(String, int, Object)
	// NullPointerException nameがnull
	@Test
	public void testSetMultipleMeta_003() {
		var c = new BmsContent(spec());
		c.beginEdit();
		assertThrows(NullPointerException.class, () -> c.setMultipleMeta(null, 0, "hoge"));
	}

	// setMultipleMeta(String, int, Object)
	// IllegalArgumentException 名称に合致するメタ情報が存在しない
	@Test
	public void testSetMultipleMeta_004() {
		var c = new BmsContent(spec());
		c.beginEdit();
		assertThrows(IllegalArgumentException.class, () -> c.setMultipleMeta("#unknown_meta_name", 0, "hoge"));
	}

	// setMultipleMeta(String, int, Object)
	// IndexOutOfBoundsException indexが0～{@link BmsSpec#MULTIPLE_META_INDEX_MAX}の範囲外
	@Test
	public void testSetMultipleMeta_005() {
		var c = new BmsContent(spec());
		var ex = IndexOutOfBoundsException.class;
		c.beginEdit();
		assertThrows(ex, () -> c.setMultipleMeta("#multiple_integer", -1, 0));
		assertThrows(ex, () -> c.setMultipleMeta("#multiple_integer", BmsSpec.MULTIPLE_META_INDEX_MAX + 1, 0));
	}

	// setMultipleMeta(String, int, Object)
	// ClassCastException valueが設定先メタ情報のデータ型に変換できない
	@Test
	public void testSetMultipleMeta_006() {
		var c = new BmsContent(spec());
		c.beginEdit();
		assertThrows(ClassCastException.class, () -> c.setMultipleMeta("#multiple_integer", 0, "not_number"));
	}

	// setIndexedMeta(String, int, Object)
	// 正常
	@Test
	public void testSetIndexedMeta_001() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.setIndexedMeta("#indexed_integer", 0, 0);
		c.setIndexedMeta("#indexed_float", 0, 200.123);
		c.setIndexedMeta("#indexed_string", 0, "hoge");
		c.setIndexedMeta("#indexed_base16", 0, 0xaf);
		c.setIndexedMeta("#indexed_base36", 0, Integer.parseInt("gz", 36));
		c.setIndexedMeta("#indexed_array16", 0, "0088aaff");
		c.setIndexedMeta("#indexed_array36", 0, "0099ffkkppzz");
		c.endEdit();
		assertEquals(0L, (long)c.getIndexedMeta("#indexed_integer", 0));
		assertEquals(200.123, (double)c.getIndexedMeta("#indexed_float", 0), 0.0001);
		assertEquals("hoge", (String)c.getIndexedMeta("#indexed_string", 0));
		assertEquals(0xafL, (long)c.getIndexedMeta("#indexed_base16", 0));
		assertEquals(Long.parseLong("gz", 36), (long)c.getIndexedMeta("#indexed_base36", 0));
		assertEquals(new BmsArray("0088aaff", 16), (BmsArray)c.getIndexedMeta("#indexed_array16", 0));
		assertEquals(new BmsArray("0099ffkkppzz", 36), (BmsArray)c.getIndexedMeta("#indexed_array36", 0));

		// ひとつのメタ情報に複数のデータを入れる
		c = new BmsContent(spec());
		c.beginEdit();
		c.setIndexedMeta("#indexed_string", 0, "hoge");
		c.setIndexedMeta("#indexed_string", 1, "hage");
		c.setIndexedMeta("#indexed_string", 2, "hige");
		c.endEdit();
		assertEquals("hoge", (String)c.getIndexedMeta("#indexed_string", 0));
		assertEquals("hage", (String)c.getIndexedMeta("#indexed_string", 1));
		assertEquals("hige", (String)c.getIndexedMeta("#indexed_string", 2));

		// 歯抜けの領域にはデータが入らないので、メタ情報の初期値が入る
		c = new BmsContent(spec());
		c.beginEdit();
		c.setIndexedMeta("#indexed_integer", 0, 100);
		c.setIndexedMeta("#indexed_integer", 3, 500);
		c.endEdit();
		assertEquals(2, c.getIndexedMetaCount("#indexed_integer"));
		assertEquals(100L, (long)c.getIndexedMeta("#indexed_integer", 0));
		assertEquals(
				(long)c.getSpec().getIndexedMeta("#indexed_integer").getDefaultValue(),
				(long)c.getIndexedMeta("#indexed_integer", 1));
		assertEquals(
				(long)c.getSpec().getIndexedMeta("#indexed_integer").getDefaultValue(),
				(long)c.getIndexedMeta("#indexed_integer", 2));
		assertEquals(500L, (long)c.getIndexedMeta("#indexed_integer", 3));

		// nullを指定すると値が消去され、そのメタ情報の初期値を返す
		c = new BmsContent(spec());
		c.beginEdit();
		c.setIndexedMeta("#indexed_integer", 0, 300);
		c.setIndexedMeta("#indexed_integer", 0, null);
		c.endEdit();
		assertEquals(
				(long)c.getSpec().getIndexedMeta("#indexed_integer").getDefaultValue(),
				(long)c.getIndexedMeta("#indexed_integer", 0));

		// nullで消去した値より後ろの値は左詰めされることはない
		c = new BmsContent(spec());
		c.beginEdit();
		c.setIndexedMeta("#indexed_integer", 0, 100);
		c.setIndexedMeta("#indexed_integer", 1, 300);  // ←こいつを消す
		c.setIndexedMeta("#indexed_integer", 2, 500);
		c.setIndexedMeta("#indexed_integer", 3, 700);
		c.setIndexedMeta("#indexed_integer", 1, null);
		c.endEdit();
		assertEquals(3, c.getIndexedMetaCount("#indexed_integer"));
		assertEquals(
				(long)c.getSpec().getIndexedMeta("#indexed_integer").getDefaultValue(),
				(long)c.getIndexedMeta("#indexed_integer", 1));
		assertEquals(500L, (long)c.getIndexedMeta("#indexed_integer", 2));
		assertEquals(700L, (long)c.getIndexedMeta("#indexed_integer", 3));
	}

	// setIndexedMeta(String, int, Object)
	// IllegalStateException 動作モードが編集モードではない
	@Test
	public void testSetIndexedMeta_002() {
		var c = new BmsContent(spec());
		assertThrows(IllegalStateException.class, () -> c.setIndexedMeta("#indexed_integer", 0, 0));
	}

	// setIndexedMeta(String, int, Object)
	// NullPointerException nameがnull
	@Test
	public void testSetIndexedMeta_003() {
		var c = new BmsContent(spec());
		c.beginEdit();
		assertThrows(NullPointerException.class, () -> c.setIndexedMeta(null, 0, "hoge"));
	}

	// setIndexedMeta(String, int, Object)
	// IllegalArgumentException 名称に合致するメタ情報が存在しない
	@Test
	public void testSetIndexedMeta_004() {
		var c = new BmsContent(spec());
		c.beginEdit();
		assertThrows(IllegalArgumentException.class, () -> c.setIndexedMeta("#unknown_meta_name", 0, "hoge"));
	}

	// setIndexedMeta(String, int, Object)
	// IndexOutOfBoundsException indexが0～{@link BmsSpec#INDEXED_META_INDEX_MAX}の範囲外
	@Test
	public void testSetIndexedMeta_005() {
		var c = new BmsContent(spec());
		var ex = IndexOutOfBoundsException.class;
		c.beginEdit();
		assertThrows(ex, () -> c.setIndexedMeta("#indexed_integer", -1, 0));
		assertThrows(ex, () -> c.setIndexedMeta("#indexed_integer", BmsSpec.INDEXED_META_INDEX_MAX + 1, 0));
	}

	// setIndexedMeta(String, int, Object)
	// ClassCastException valueが設定先メタ情報のデータ型に変換できない
	@Test
	public void testSetIndexedMeta_006() {
		var c = new BmsContent(spec());
		c.beginEdit();
		assertThrows(ClassCastException.class, () -> c.setIndexedMeta("#indexed_integer", 0, "not_number"));
	}

	// setIndexedMeta(String, int, Object)
	// 正常：BPM変更にBMSライブラリ仕様の最小値・最大値が設定できること
	// IllegalArgumentException BPM変更にBMSライブラリ仕様の最小値未満・最大値超過を設定しようとした
	@Test
	public void testSetIndexedMeta_BpmWithinSpec() {
		var e = IllegalArgumentException.class;
		var c = new BmsContent(spec());
		c.beginEdit();

		// BPM最小値を設定可能であること
		c.setIndexedMeta("#bpm", 1, BmsSpec.BPM_MIN);
		assertEquals(BmsSpec.BPM_MIN, ((Number)c.getIndexedMeta("#bpm", 1)).doubleValue(), 0.0);

		// BPM最大値を設定可能であること
		c.setIndexedMeta("#bpm", 2, BmsSpec.BPM_MAX);
		assertEquals(BmsSpec.BPM_MAX, ((Number)c.getIndexedMeta("#bpm", 2)).doubleValue(), 0.0);

		// BPM最小値未満が設定不可であること
		assertThrows(e, () -> c.setIndexedMeta("#bpm", 3, Math.nextDown(BmsSpec.BPM_MIN)));

		// BPM最大値超過が設定不可であること
		assertThrows(e, () -> c.setIndexedMeta("#bpm", 4, Math.nextUp(BmsSpec.BPM_MAX)));

		c.endEdit();
	}

	// setIndexedMeta(String, int, Object)
	// 正常：譜面停止時間にBMSライブラリ仕様の最小値・最大値が設定できること
	// IllegalArgumentException 譜面停止時間にBMSライブラリ仕様の最小値未満・最大値超過を設定しようとした
	@Test
	public void testSetIndexedMeta_StopWithinSpec() {
		var e = IllegalArgumentException.class;
		var c = new BmsContent(spec());
		c.beginEdit();

		// 譜面停止時間最小値を設定可能であること
		c.setIndexedMeta("#stop", 1, BmsSpec.STOP_MIN);
		assertEquals(BmsSpec.STOP_MIN, ((Number)c.getIndexedMeta("#stop", 1)).doubleValue(), 0.0);

		// 譜面停止時間最大値を設定可能であること
		c.setIndexedMeta("#stop", 2, BmsSpec.STOP_MAX);
		assertEquals(BmsSpec.STOP_MAX, ((Number)c.getIndexedMeta("#stop", 2)).doubleValue(), 0.0);

		// 譜面停止時間最小値未満が設定不可であること
		assertThrows(e, () -> c.setIndexedMeta("#stop", 3, Math.nextDown(BmsSpec.STOP_MIN)));

		// 譜面停止時間最大値超過が設定不可であること
		assertThrows(e, () -> c.setIndexedMeta("#stop", 4, Math.nextUp(BmsSpec.STOP_MAX)));

		c.endEdit();
	}

	// setMeta(BmsMetaKey, Object)
	// 正常
	@Test
	public void testSetMetaBmsMetaKeyObject_001() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.setMeta(new BmsMetaKey("#single_integer", BmsUnit.SINGLE), 0);
		c.setMeta(new BmsMetaKey("#single_float", BmsUnit.SINGLE), 200.123);
		c.setMeta(new BmsMetaKey("#single_string", BmsUnit.SINGLE), "hoge");
		c.setMeta(new BmsMetaKey("#single_base16", BmsUnit.SINGLE), 0xaf);
		c.setMeta(new BmsMetaKey("#single_base36", BmsUnit.SINGLE), Integer.parseInt("gz", 36));
		c.setMeta(new BmsMetaKey("#single_array16", BmsUnit.SINGLE), "0088aaff");
		c.setMeta(new BmsMetaKey("#single_array36", BmsUnit.SINGLE), "0099ffkkppzz");

		c.setMeta(new BmsMetaKey("#multiple_integer", BmsUnit.MULTIPLE), 0);
		c.setMeta(new BmsMetaKey("#multiple_float", BmsUnit.MULTIPLE), 200.123);
		c.setMeta(new BmsMetaKey("#multiple_string", BmsUnit.MULTIPLE), "hoge");
		c.setMeta(new BmsMetaKey("#multiple_base16", BmsUnit.MULTIPLE), 0xaf);
		c.setMeta(new BmsMetaKey("#multiple_base36", BmsUnit.MULTIPLE), Integer.parseInt("gz", 36));
		c.setMeta(new BmsMetaKey("#multiple_array16", BmsUnit.MULTIPLE), "0088aaff");
		c.setMeta(new BmsMetaKey("#multiple_array36", BmsUnit.MULTIPLE), "0099ffkkppzz");

		c.setMeta(new BmsMetaKey("#indexed_integer", BmsUnit.INDEXED), 0);
		c.setMeta(new BmsMetaKey("#indexed_float", BmsUnit.INDEXED), 200.123);
		c.setMeta(new BmsMetaKey("#indexed_string", BmsUnit.INDEXED), "hoge");
		c.setMeta(new BmsMetaKey("#indexed_base16", BmsUnit.INDEXED), 0xaf);
		c.setMeta(new BmsMetaKey("#indexed_base36", BmsUnit.INDEXED), Integer.parseInt("gz", 36));
		c.setMeta(new BmsMetaKey("#indexed_array16", BmsUnit.INDEXED), "0088aaff");
		c.setMeta(new BmsMetaKey("#indexed_array36", BmsUnit.INDEXED), "0099ffkkppzz");
		c.endEdit();

		assertEquals(0L, (long)c.getSingleMeta("#single_integer"));
		assertEquals(200.123, (double)c.getSingleMeta("#single_float"), 0.0001);
		assertEquals("hoge", (String)c.getSingleMeta("#single_string"));
		assertEquals(0xafL, (long)c.getSingleMeta("#single_base16"));
		assertEquals(Long.parseLong("gz", 36), (long)c.getSingleMeta("#single_base36"));
		assertEquals(new BmsArray("0088aaff", 16), (BmsArray)c.getSingleMeta("#single_array16"));
		assertEquals(new BmsArray("0099ffkkppzz", 36), (BmsArray)c.getSingleMeta("#single_array36"));

		assertEquals(0L, (long)c.getMultipleMeta("#multiple_integer", 0));
		assertEquals(200.123, (double)c.getMultipleMeta("#multiple_float", 0), 0.0001);
		assertEquals("hoge", (String)c.getMultipleMeta("#multiple_string", 0));
		assertEquals(0xafL, (long)c.getMultipleMeta("#multiple_base16", 0));
		assertEquals(Long.parseLong("gz", 36), (long)c.getMultipleMeta("#multiple_base36", 0));
		assertEquals(new BmsArray("0088aaff", 16), (BmsArray)c.getMultipleMeta("#multiple_array16", 0));
		assertEquals(new BmsArray("0099ffkkppzz", 36), (BmsArray)c.getMultipleMeta("#multiple_array36", 0));

		assertEquals(0L, (long)c.getIndexedMeta("#indexed_integer", 0));
		assertEquals(200.123, (double)c.getIndexedMeta("#indexed_float", 0), 0.0001);
		assertEquals("hoge", (String)c.getIndexedMeta("#indexed_string", 0));
		assertEquals(0xafL, (long)c.getIndexedMeta("#indexed_base16", 0));
		assertEquals(Long.parseLong("gz", 36), (long)c.getIndexedMeta("#indexed_base36", 0));
		assertEquals(new BmsArray("0088aaff", 16), (BmsArray)c.getIndexedMeta("#indexed_array16", 0));
		assertEquals(new BmsArray("0099ffkkppzz", 36), (BmsArray)c.getIndexedMeta("#indexed_array36", 0));
	}

	// setMeta(BmsMetaKey, Object)
	// IllegalStateException 動作モードが編集モードではない
	@Test
	public void testSetMetaBmsMetaKeyObject_002() {
		var c = new BmsContent(spec());
		assertThrows(IllegalStateException.class, () -> c.setMeta(new BmsMetaKey("#single_integer", BmsUnit.SINGLE), 0));
	}

	// setMeta(BmsMetaKey, Object)
	// NullPointerException keyがnull
	@Test
	public void testSetMetaBmsMetaKeyObject_003() {
		var c = new BmsContent(spec());
		c.beginEdit();
		assertThrows(NullPointerException.class, () -> c.setMeta(null, 0));
	}

	// setMeta(BmsMetaKey, Object)
	// IllegalArgumentException name, unitに合致するメタ情報が存在しない
	@Test
	public void testSetMetaBmsMetaKeyObject_004() {
		var c = new BmsContent(spec());
		var ex = IllegalArgumentException.class;
		c.beginEdit();
		assertThrows(ex, () -> c.setMeta(new BmsMetaKey("#indexed_integer", BmsUnit.SINGLE), 0));
		assertThrows(ex, () -> c.setMeta(new BmsMetaKey("#single_integer", BmsUnit.MULTIPLE), 0));
		assertThrows(ex, () -> c.setMeta(new BmsMetaKey("#multiple_integer", BmsUnit.INDEXED), 0));
	}

	// setMeta(BmsMetaKey, Object)
	// ClassCastException valueが設定先メタ情報のデータ型に変換できない
	@Test
	public void testSetMetaBmsMetaKeyObject_005() {
		var c = new BmsContent(spec());
		c.beginEdit();
		assertThrows(ClassCastException.class, () -> c.setMeta(new BmsMetaKey("#single_integer", BmsUnit.SINGLE), "not_number"));
	}

	// setMeta(BmsMetaKey, int, Object)
	// 正常
	@Test
	public void testSetMetaBmsMetaKeyIntObject_001() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.setMeta(new BmsMetaKey("#single_integer", BmsUnit.SINGLE), 0, 0);
		c.setMeta(new BmsMetaKey("#single_float", BmsUnit.SINGLE), 0, 200.123);
		c.setMeta(new BmsMetaKey("#single_string", BmsUnit.SINGLE), 0, "hoge");
		c.setMeta(new BmsMetaKey("#single_base16", BmsUnit.SINGLE), 0, 0xaf);
		c.setMeta(new BmsMetaKey("#single_base36", BmsUnit.SINGLE), 0, Integer.parseInt("gz", 36));
		c.setMeta(new BmsMetaKey("#single_array16", BmsUnit.SINGLE), 0, "0088aaff");
		c.setMeta(new BmsMetaKey("#single_array36", BmsUnit.SINGLE), 0, "0099ffkkppzz");

		c.setMeta(new BmsMetaKey("#multiple_integer", BmsUnit.MULTIPLE), 0, 0);
		c.setMeta(new BmsMetaKey("#multiple_float", BmsUnit.MULTIPLE), 0, 200.123);
		c.setMeta(new BmsMetaKey("#multiple_string", BmsUnit.MULTIPLE), 0, "hoge");
		c.setMeta(new BmsMetaKey("#multiple_base16", BmsUnit.MULTIPLE), 0, 0xaf);
		c.setMeta(new BmsMetaKey("#multiple_base36", BmsUnit.MULTIPLE), 0, Integer.parseInt("gz", 36));
		c.setMeta(new BmsMetaKey("#multiple_array16", BmsUnit.MULTIPLE), 0, "0088aaff");
		c.setMeta(new BmsMetaKey("#multiple_array36", BmsUnit.MULTIPLE), 0, "0099ffkkppzz");

		c.setMeta(new BmsMetaKey("#indexed_integer", BmsUnit.INDEXED), 0, 0);
		c.setMeta(new BmsMetaKey("#indexed_float", BmsUnit.INDEXED), 0, 200.123);
		c.setMeta(new BmsMetaKey("#indexed_string", BmsUnit.INDEXED), 0, "hoge");
		c.setMeta(new BmsMetaKey("#indexed_base16", BmsUnit.INDEXED), 0, 0xaf);
		c.setMeta(new BmsMetaKey("#indexed_base36", BmsUnit.INDEXED), 0, Integer.parseInt("gz", 36));
		c.setMeta(new BmsMetaKey("#indexed_array16", BmsUnit.INDEXED), 0, "0088aaff");
		c.setMeta(new BmsMetaKey("#indexed_array36", BmsUnit.INDEXED), 0, "0099ffkkppzz");
		c.endEdit();

		assertEquals(0L, (long)c.getSingleMeta("#single_integer"));
		assertEquals(200.123, (double)c.getSingleMeta("#single_float"), 0.0001);
		assertEquals("hoge", (String)c.getSingleMeta("#single_string"));
		assertEquals(0xafL, (long)c.getSingleMeta("#single_base16"));
		assertEquals(Long.parseLong("gz", 36), (long)c.getSingleMeta("#single_base36"));
		assertEquals(new BmsArray("0088aaff", 16), (BmsArray)c.getSingleMeta("#single_array16"));
		assertEquals(new BmsArray("0099ffkkppzz", 36), (BmsArray)c.getSingleMeta("#single_array36"));

		assertEquals(0L, (long)c.getMultipleMeta("#multiple_integer", 0));
		assertEquals(200.123, (double)c.getMultipleMeta("#multiple_float", 0), 0.0001);
		assertEquals("hoge", (String)c.getMultipleMeta("#multiple_string", 0));
		assertEquals(0xafL, (long)c.getMultipleMeta("#multiple_base16", 0));
		assertEquals(Long.parseLong("gz", 36), (long)c.getMultipleMeta("#multiple_base36", 0));
		assertEquals(new BmsArray("0088aaff", 16), (BmsArray)c.getMultipleMeta("#multiple_array16", 0));
		assertEquals(new BmsArray("0099ffkkppzz", 36), (BmsArray)c.getMultipleMeta("#multiple_array36", 0));

		assertEquals(0L, (long)c.getIndexedMeta("#indexed_integer", 0));
		assertEquals(200.123, (double)c.getIndexedMeta("#indexed_float", 0), 0.0001);
		assertEquals("hoge", (String)c.getIndexedMeta("#indexed_string", 0));
		assertEquals(0xafL, (long)c.getIndexedMeta("#indexed_base16", 0));
		assertEquals(Long.parseLong("gz", 36), (long)c.getIndexedMeta("#indexed_base36", 0));
		assertEquals(new BmsArray("0088aaff", 16), (BmsArray)c.getIndexedMeta("#indexed_array16", 0));
		assertEquals(new BmsArray("0099ffkkppzz", 36), (BmsArray)c.getIndexedMeta("#indexed_array36", 0));
	}

	// setMeta(BmsMetaKey, int, Object)
	// IllegalStateException 動作モードが編集モードではない
	@Test
	public void testSetMetaBmsMetaKeyIntObject_002() {
		var c = new BmsContent(spec());
		assertThrows(IllegalStateException.class, () -> c.setMeta(new BmsMetaKey("#single_integer", BmsUnit.SINGLE), 0, 0));
	}

	// setMeta(BmsMetaKey, int, Object)
	// NullPointerException keyがnull
	@Test
	public void testSetMetaBmsMetaKeyIntObject_003() {
		var c = new BmsContent(spec());
		c.beginEdit();
		assertThrows(NullPointerException.class, () -> c.setMeta(null, 0, 0));
	}

	// setMeta(BmsMetaKey, int, Object)
	// IllegalArgumentException メタ情報キーに合致するメタ情報が存在しない
	@Test
	public void testSetMetaBmsMetaKeyIntObject_004() {
		var c = new BmsContent(spec());
		var ex = IllegalArgumentException.class;
		c.beginEdit();
		assertThrows(ex, () -> c.setMeta(new BmsMetaKey("#indexed_integer", BmsUnit.SINGLE), 0, 0));
		assertThrows(ex, () -> c.setMeta(new BmsMetaKey("#single_integer", BmsUnit.MULTIPLE), 0, 0));
		assertThrows(ex, () -> c.setMeta(new BmsMetaKey("#multiple_integer", BmsUnit.INDEXED), 0, 0));
	}

	// setMeta(BmsMetaKey, int, Object)
	// IndexOutOfBoundsException 構成単位が単体の時、indexが0以外
	@Test
	public void testSetMetaBmsMetaKeyIntObject_005() {
		var c = new BmsContent(spec());
		var ex = IndexOutOfBoundsException.class;
		c.beginEdit();
		assertThrows(ex, () -> c.setMeta(new BmsMetaKey("#single_integer", BmsUnit.SINGLE), 1, 0));
	}

	// setMeta(BmsMetaKey, int, Object)
	// IndexOutOfBoundsException 構成単位が複数の時、indexが0～{@link BmsSpec#MULTIPLE_META_INDEX_MAX}の範囲外
	@Test
	public void testSetMetaBmsMetaKeyIntObject_006() {
		var c = new BmsContent(spec());
		var ex = IndexOutOfBoundsException.class;
		c.beginEdit();
		assertThrows(ex, () -> c.setMeta(new BmsMetaKey("#multiple_integer", BmsUnit.MULTIPLE), -1, 0));
		assertThrows(ex, () -> c.setMeta(new BmsMetaKey("#multiple_integer", BmsUnit.MULTIPLE), BmsSpec.MULTIPLE_META_INDEX_MAX + 1, 0));
	}

	// setMeta(BmsMetaKey, int, Object)
	// IndexOutOfBoundsException 構成単位が索引付きの時、indexが0～{@link BmsSpec#INDEXED_META_INDEX_MAX}の範囲外
	@Test
	public void testSetMetaBmsMetaKeyIntObject_007() {
		var c = new BmsContent(spec());
		var ex = IndexOutOfBoundsException.class;
		c.beginEdit();
		assertThrows(ex, () -> c.setMeta(new BmsMetaKey("#indexed_integer", BmsUnit.INDEXED), -1, 0));
		assertThrows(ex, () -> c.setMeta(new BmsMetaKey("#indexed_integer", BmsUnit.INDEXED), BmsSpec.INDEXED_META_INDEX_MAX + 1, 0));
	}

	// setMeta(String, BmsUnit, int, Object)
	// 正常
	@Test
	public void testSetMeta_001() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.setMeta("#single_integer", BmsUnit.SINGLE, 0, 0);
		c.setMeta("#single_float", BmsUnit.SINGLE, 0, 200.123);
		c.setMeta("#single_string", BmsUnit.SINGLE, 0, "hoge");
		c.setMeta("#single_base16", BmsUnit.SINGLE, 0, 0xaf);
		c.setMeta("#single_base36", BmsUnit.SINGLE, 0, Integer.parseInt("gz", 36));
		c.setMeta("#single_array16", BmsUnit.SINGLE, 0, "0088aaff");
		c.setMeta("#single_array36", BmsUnit.SINGLE, 0, "0099ffkkppzz");

		c.setMeta("#multiple_integer", BmsUnit.MULTIPLE, 0, 0);
		c.setMeta("#multiple_float", BmsUnit.MULTIPLE, 0, 200.123);
		c.setMeta("#multiple_string", BmsUnit.MULTIPLE, 0, "hoge");
		c.setMeta("#multiple_base16", BmsUnit.MULTIPLE, 0, 0xaf);
		c.setMeta("#multiple_base36", BmsUnit.MULTIPLE, 0, Integer.parseInt("gz", 36));
		c.setMeta("#multiple_array16", BmsUnit.MULTIPLE, 0, "0088aaff");
		c.setMeta("#multiple_array36", BmsUnit.MULTIPLE, 0, "0099ffkkppzz");

		c.setMeta("#indexed_integer", BmsUnit.INDEXED, 0, 0);
		c.setMeta("#indexed_float", BmsUnit.INDEXED, 0, 200.123);
		c.setMeta("#indexed_string", BmsUnit.INDEXED, 0, "hoge");
		c.setMeta("#indexed_base16", BmsUnit.INDEXED, 0, 0xaf);
		c.setMeta("#indexed_base36", BmsUnit.INDEXED, 0, Integer.parseInt("gz", 36));
		c.setMeta("#indexed_array16", BmsUnit.INDEXED, 0, "0088aaff");
		c.setMeta("#indexed_array36", BmsUnit.INDEXED, 0, "0099ffkkppzz");
		c.endEdit();

		assertEquals(0L, (long)c.getSingleMeta("#single_integer"));
		assertEquals(200.123, (double)c.getSingleMeta("#single_float"), 0.0001);
		assertEquals("hoge", (String)c.getSingleMeta("#single_string"));
		assertEquals(0xafL, (long)c.getSingleMeta("#single_base16"));
		assertEquals(Long.parseLong("gz", 36), (long)c.getSingleMeta("#single_base36"));
		assertEquals(new BmsArray("0088aaff", 16), (BmsArray)c.getSingleMeta("#single_array16"));
		assertEquals(new BmsArray("0099ffkkppzz", 36), (BmsArray)c.getSingleMeta("#single_array36"));

		assertEquals(0L, (long)c.getMultipleMeta("#multiple_integer", 0));
		assertEquals(200.123, (double)c.getMultipleMeta("#multiple_float", 0), 0.0001);
		assertEquals("hoge", (String)c.getMultipleMeta("#multiple_string", 0));
		assertEquals(0xafL, (long)c.getMultipleMeta("#multiple_base16", 0));
		assertEquals(Long.parseLong("gz", 36), (long)c.getMultipleMeta("#multiple_base36", 0));
		assertEquals(new BmsArray("0088aaff", 16), (BmsArray)c.getMultipleMeta("#multiple_array16", 0));
		assertEquals(new BmsArray("0099ffkkppzz", 36), (BmsArray)c.getMultipleMeta("#multiple_array36", 0));

		assertEquals(0L, (long)c.getIndexedMeta("#indexed_integer", 0));
		assertEquals(200.123, (double)c.getIndexedMeta("#indexed_float", 0), 0.0001);
		assertEquals("hoge", (String)c.getIndexedMeta("#indexed_string", 0));
		assertEquals(0xafL, (long)c.getIndexedMeta("#indexed_base16", 0));
		assertEquals(Long.parseLong("gz", 36), (long)c.getIndexedMeta("#indexed_base36", 0));
		assertEquals(new BmsArray("0088aaff", 16), (BmsArray)c.getIndexedMeta("#indexed_array16", 0));
		assertEquals(new BmsArray("0099ffkkppzz", 36), (BmsArray)c.getIndexedMeta("#indexed_array36", 0));
	}

	// setMeta(String, BmsUnit, int, Object)
	// IllegalStateException 動作モードが編集モードではない
	@Test
	public void testSetMeta_002() {
		var c = new BmsContent(spec());
		assertThrows(IllegalStateException.class, () -> c.setMeta("#single_integer", BmsUnit.SINGLE, 0, 0));
	}

	// setMeta(String, BmsUnit, int, Object)
	// NullPointerException nameがnull
	@Test
	public void testSetMeta_003() {
		var c = new BmsContent(spec());
		c.beginEdit();
		assertThrows(NullPointerException.class, () -> c.setMeta(null, BmsUnit.SINGLE, 0, 0));
	}

	// setMeta(String, BmsUnit, int, Object)
	// NullPointerException unitがnull
	@Test
	public void testSetMeta_004() {
		var c = new BmsContent(spec());
		c.beginEdit();
		assertThrows(NullPointerException.class, () -> c.setMeta("#meta", null, 0, 0));
	}

	// setMeta(String, BmsUnit, int, Object)
	// IllegalArgumentException name, unitに合致するメタ情報が存在しない
	@Test
	public void testSetMeta_005() {
		var c = new BmsContent(spec());
		var ex = IllegalArgumentException.class;
		c.beginEdit();
		assertThrows(ex, () -> c.setMeta("#indexed_integer", BmsUnit.SINGLE, 0, 0));
		assertThrows(ex, () -> c.setMeta("#single_integer", BmsUnit.MULTIPLE, 0, 0));
		assertThrows(ex, () -> c.setMeta("#multiple_integer", BmsUnit.INDEXED, 0, 0));
	}

	// setMeta(String, BmsUnit, int, Object)
	// IndexOutOfBoundsException unitが{@link BmsUnit#SINGLE SINGLE}の時、indexが0以外
	@Test
	public void testSetMeta_006() {
		var c = new BmsContent(spec());
		var ex = IndexOutOfBoundsException.class;
		c.beginEdit();
		assertThrows(ex, () -> c.setMeta("#single_integer", BmsUnit.SINGLE, 1, 0));
	}

	// setMeta(String, BmsUnit, int, Object)
	// IndexOutOfBoundsException unitが{@link BmsUnit#MULTIPLE MULTIPLE}の時、indexが0～{@link BmsSpec#MULTIPLE_META_INDEX_MAX}の範囲外
	@Test
	public void testSetMeta_007() {
		var c = new BmsContent(spec());
		var ex = IndexOutOfBoundsException.class;
		c.beginEdit();
		assertThrows(ex, () -> c.setMeta("#multiple_integer", BmsUnit.MULTIPLE, -1, 0));
		assertThrows(ex, () -> c.setMeta("#multiple_integer", BmsUnit.MULTIPLE, BmsSpec.MULTIPLE_META_INDEX_MAX + 1, 0));
	}

	// setMeta(String, BmsUnit, int, Object)
	// IndexOutOfBoundsException unitが{@link BmsUnit#INDEXED INDEXED}の時、indexが0～{@link BmsSpec#INDEXED_META_INDEX_MAX}の範囲外
	@Test
	public void testSetMeta_008() {
		var c = new BmsContent(spec());
		var ex = IndexOutOfBoundsException.class;
		c.beginEdit();
		assertThrows(ex, () -> c.setMeta("#indexed_integer", BmsUnit.INDEXED, -1, 0));
		assertThrows(ex, () -> c.setMeta("#indexed_integer", BmsUnit.INDEXED, BmsSpec.INDEXED_META_INDEX_MAX + 1, 0));
	}

	// setMeta(String, BmsUnit, int, Object)
	// ClassCastException valueが設定先メタ情報のデータ型に変換できない
	@Test
	public void testSetMeta_009() {
		var c = new BmsContent(spec());
		c.beginEdit();
		assertThrows(ClassCastException.class, () -> c.setMeta("#single_integer", BmsUnit.SINGLE, 0, "not_number"));
	}

	// putMeta(BmsMetaElement)
	// 単体メタ情報を正しく追加できること
	@Test
	public void testPutMeta_Single() {
		var s = spec();
		var c = new BmsContent(s);
		c.beginEdit();
		c.putMeta(new BmsMetaElement(s.getSingleMeta("#single_integer"), 0, 100L));
		c.putMeta(new BmsMetaElement(s.getSingleMeta("#single_float"), 0, 200.345));
		c.putMeta(new BmsMetaElement(s.getSingleMeta("#single_string"), 0, "STR"));
		c.putMeta(new BmsMetaElement(s.getSingleMeta("#single_base16"), 0, BmsInt.to16i("AB")));
		c.putMeta(new BmsMetaElement(s.getSingleMeta("#single_base36"), 0, BmsInt.to36i("XY")));
		c.putMeta(new BmsMetaElement(s.getSingleMeta("#single_array16"), 0, new BmsArray("AABB", 16)));
		c.putMeta(new BmsMetaElement(s.getSingleMeta("#single_array36"), 0, new BmsArray("XXYY", 36)));
		c.putMeta(new BmsMetaElement(s.getSingleMeta("#single_object"), 0, "OBJ"));
		c.endEdit();
		assertEquals(100L, (long)c.getSingleMeta("#single_integer"));
		assertEquals(200.345, c.getSingleMeta("#single_float"), 0.0);
		assertEquals("STR", c.getSingleMeta("#single_string"));
		assertEquals(BmsInt.to16i("AB"), (long)c.getSingleMeta("#single_base16"));
		assertEquals(BmsInt.to36i("XY"), (long)c.getSingleMeta("#single_base36"));
		assertEquals("AABB", ((BmsArray)c.getSingleMeta("#single_array16")).toString());
		assertEquals("XXYY", ((BmsArray)c.getSingleMeta("#single_array36")).toString());
		assertEquals("OBJ", c.getSingleMeta("#single_object"));
	}

	// putMeta(BmsMetaElement)
	// 複数メタ情報を正しく追加できること
	@Test
	public void testPutMeta_Multiple() {
		var s = spec();
		var c = new BmsContent(s);
		c.beginEdit();
		c.putMeta(new BmsMetaElement(s.getMultipleMeta("#multiple_integer"), 0, 100L));
		c.putMeta(new BmsMetaElement(s.getMultipleMeta("#multiple_integer"), 1, 200L));
		c.putMeta(new BmsMetaElement(s.getMultipleMeta("#multiple_integer"), 2, 300L));
		c.putMeta(new BmsMetaElement(s.getMultipleMeta("#multiple_float"), 0, 200.345));
		c.putMeta(new BmsMetaElement(s.getMultipleMeta("#multiple_float"), 1, 300.456));
		c.putMeta(new BmsMetaElement(s.getMultipleMeta("#multiple_float"), 2, 400.567));
		c.putMeta(new BmsMetaElement(s.getMultipleMeta("#multiple_string"), 0, "STR1"));
		c.putMeta(new BmsMetaElement(s.getMultipleMeta("#multiple_string"), 1, "STR2"));
		c.putMeta(new BmsMetaElement(s.getMultipleMeta("#multiple_string"), 2, "STR3"));
		c.putMeta(new BmsMetaElement(s.getMultipleMeta("#multiple_base16"), 0, BmsInt.to16i("AB")));
		c.putMeta(new BmsMetaElement(s.getMultipleMeta("#multiple_base16"), 1, BmsInt.to16i("BC")));
		c.putMeta(new BmsMetaElement(s.getMultipleMeta("#multiple_base16"), 2, BmsInt.to16i("CD")));
		c.putMeta(new BmsMetaElement(s.getMultipleMeta("#multiple_base36"), 0, BmsInt.to36i("WX")));
		c.putMeta(new BmsMetaElement(s.getMultipleMeta("#multiple_base36"), 1, BmsInt.to36i("XY")));
		c.putMeta(new BmsMetaElement(s.getMultipleMeta("#multiple_base36"), 2, BmsInt.to36i("YZ")));
		c.putMeta(new BmsMetaElement(s.getMultipleMeta("#multiple_array16"), 0, new BmsArray("AABB", 16)));
		c.putMeta(new BmsMetaElement(s.getMultipleMeta("#multiple_array16"), 1, new BmsArray("BBCC", 16)));
		c.putMeta(new BmsMetaElement(s.getMultipleMeta("#multiple_array16"), 2, new BmsArray("CCDD", 16)));
		c.putMeta(new BmsMetaElement(s.getMultipleMeta("#multiple_array36"), 0, new BmsArray("WWXX", 36)));
		c.putMeta(new BmsMetaElement(s.getMultipleMeta("#multiple_array36"), 1, new BmsArray("XXYY", 36)));
		c.putMeta(new BmsMetaElement(s.getMultipleMeta("#multiple_array36"), 2, new BmsArray("YYZZ", 36)));
		c.putMeta(new BmsMetaElement(s.getMultipleMeta("#multiple_object"), 0, "OBJ1"));
		c.putMeta(new BmsMetaElement(s.getMultipleMeta("#multiple_object"), 1, "OBJ2"));
		c.putMeta(new BmsMetaElement(s.getMultipleMeta("#multiple_object"), 2, "OBJ3"));
		c.endEdit();
		assertEquals(100L, (long)c.getMultipleMeta("#multiple_integer", 0));
		assertEquals(200L, (long)c.getMultipleMeta("#multiple_integer", 1));
		assertEquals(300L, (long)c.getMultipleMeta("#multiple_integer", 2));
		assertEquals(200.345, c.getMultipleMeta("#multiple_float", 0), 0.0);
		assertEquals(300.456, c.getMultipleMeta("#multiple_float", 1), 0.0);
		assertEquals(400.567, c.getMultipleMeta("#multiple_float", 2), 0.0);
		assertEquals("STR1", c.getMultipleMeta("#multiple_string", 0));
		assertEquals("STR2", c.getMultipleMeta("#multiple_string", 1));
		assertEquals("STR3", c.getMultipleMeta("#multiple_string", 2));
		assertEquals(BmsInt.to16i("AB"), (long)c.getMultipleMeta("#multiple_base16", 0));
		assertEquals(BmsInt.to16i("BC"), (long)c.getMultipleMeta("#multiple_base16", 1));
		assertEquals(BmsInt.to16i("CD"), (long)c.getMultipleMeta("#multiple_base16", 2));
		assertEquals(BmsInt.to36i("WX"), (long)c.getMultipleMeta("#multiple_base36", 0));
		assertEquals(BmsInt.to36i("XY"), (long)c.getMultipleMeta("#multiple_base36", 1));
		assertEquals(BmsInt.to36i("YZ"), (long)c.getMultipleMeta("#multiple_base36", 2));
		assertEquals("AABB", ((BmsArray)c.getMultipleMeta("#multiple_array16", 0)).toString());
		assertEquals("BBCC", ((BmsArray)c.getMultipleMeta("#multiple_array16", 1)).toString());
		assertEquals("CCDD", ((BmsArray)c.getMultipleMeta("#multiple_array16", 2)).toString());
		assertEquals("WWXX", ((BmsArray)c.getMultipleMeta("#multiple_array36", 0)).toString());
		assertEquals("XXYY", ((BmsArray)c.getMultipleMeta("#multiple_array36", 1)).toString());
		assertEquals("YYZZ", ((BmsArray)c.getMultipleMeta("#multiple_array36", 2)).toString());
		assertEquals("OBJ1", c.getMultipleMeta("#multiple_object", 0));
		assertEquals("OBJ2", c.getMultipleMeta("#multiple_object", 1));
		assertEquals("OBJ3", c.getMultipleMeta("#multiple_object", 2));
	}

	// putMeta(BmsMetaElement)
	// 索引付きメタ情報を正しく追加できること
	@Test
	public void testPutMeta_Indexed() {
		var s = spec();
		var c = new BmsContent(s);
		c.beginEdit();
		c.putMeta(new BmsMetaElement(s.getIndexedMeta("#indexed_integer"), 0, 100L));
		c.putMeta(new BmsMetaElement(s.getIndexedMeta("#indexed_integer"), 2, 200L));
		c.putMeta(new BmsMetaElement(s.getIndexedMeta("#indexed_integer"), 3, 300L));
		c.putMeta(new BmsMetaElement(s.getIndexedMeta("#indexed_float"), 0, 200.345));
		c.putMeta(new BmsMetaElement(s.getIndexedMeta("#indexed_float"), 2, 300.456));
		c.putMeta(new BmsMetaElement(s.getIndexedMeta("#indexed_float"), 3, 400.567));
		c.putMeta(new BmsMetaElement(s.getIndexedMeta("#indexed_string"), 0, "STR1"));
		c.putMeta(new BmsMetaElement(s.getIndexedMeta("#indexed_string"), 2, "STR2"));
		c.putMeta(new BmsMetaElement(s.getIndexedMeta("#indexed_string"), 3, "STR3"));
		c.putMeta(new BmsMetaElement(s.getIndexedMeta("#indexed_base16"), 0, BmsInt.to16i("AB")));
		c.putMeta(new BmsMetaElement(s.getIndexedMeta("#indexed_base16"), 2, BmsInt.to16i("BC")));
		c.putMeta(new BmsMetaElement(s.getIndexedMeta("#indexed_base16"), 3, BmsInt.to16i("CD")));
		c.putMeta(new BmsMetaElement(s.getIndexedMeta("#indexed_base36"), 0, BmsInt.to36i("WX")));
		c.putMeta(new BmsMetaElement(s.getIndexedMeta("#indexed_base36"), 2, BmsInt.to36i("XY")));
		c.putMeta(new BmsMetaElement(s.getIndexedMeta("#indexed_base36"), 3, BmsInt.to36i("YZ")));
		c.putMeta(new BmsMetaElement(s.getIndexedMeta("#indexed_array16"), 0, new BmsArray("AABB", 16)));
		c.putMeta(new BmsMetaElement(s.getIndexedMeta("#indexed_array16"), 2, new BmsArray("BBCC", 16)));
		c.putMeta(new BmsMetaElement(s.getIndexedMeta("#indexed_array16"), 3, new BmsArray("CCDD", 16)));
		c.putMeta(new BmsMetaElement(s.getIndexedMeta("#indexed_array36"), 0, new BmsArray("WWXX", 36)));
		c.putMeta(new BmsMetaElement(s.getIndexedMeta("#indexed_array36"), 2, new BmsArray("XXYY", 36)));
		c.putMeta(new BmsMetaElement(s.getIndexedMeta("#indexed_array36"), 3, new BmsArray("YYZZ", 36)));
		c.putMeta(new BmsMetaElement(s.getIndexedMeta("#indexed_object"), 0, "OBJ1"));
		c.putMeta(new BmsMetaElement(s.getIndexedMeta("#indexed_object"), 2, "OBJ2"));
		c.putMeta(new BmsMetaElement(s.getIndexedMeta("#indexed_object"), 3, "OBJ3"));
		c.endEdit();
		assertEquals(100L, (long)c.getIndexedMeta("#indexed_integer", 0));
		assertEquals(200L, (long)c.getIndexedMeta("#indexed_integer", 2));
		assertEquals(300L, (long)c.getIndexedMeta("#indexed_integer", 3));
		assertEquals(200.345, c.getIndexedMeta("#indexed_float", 0), 0.0);
		assertEquals(300.456, c.getIndexedMeta("#indexed_float", 2), 0.0);
		assertEquals(400.567, c.getIndexedMeta("#indexed_float", 3), 0.0);
		assertEquals("STR1", c.getIndexedMeta("#indexed_string", 0));
		assertEquals("STR2", c.getIndexedMeta("#indexed_string", 2));
		assertEquals("STR3", c.getIndexedMeta("#indexed_string", 3));
		assertEquals(BmsInt.to16i("AB"), (long)c.getIndexedMeta("#indexed_base16", 0));
		assertEquals(BmsInt.to16i("BC"), (long)c.getIndexedMeta("#indexed_base16", 2));
		assertEquals(BmsInt.to16i("CD"), (long)c.getIndexedMeta("#indexed_base16", 3));
		assertEquals(BmsInt.to36i("WX"), (long)c.getIndexedMeta("#indexed_base36", 0));
		assertEquals(BmsInt.to36i("XY"), (long)c.getIndexedMeta("#indexed_base36", 2));
		assertEquals(BmsInt.to36i("YZ"), (long)c.getIndexedMeta("#indexed_base36", 3));
		assertEquals("AABB", ((BmsArray)c.getIndexedMeta("#indexed_array16", 0)).toString());
		assertEquals("BBCC", ((BmsArray)c.getIndexedMeta("#indexed_array16", 2)).toString());
		assertEquals("CCDD", ((BmsArray)c.getIndexedMeta("#indexed_array16", 3)).toString());
		assertEquals("WWXX", ((BmsArray)c.getIndexedMeta("#indexed_array36", 0)).toString());
		assertEquals("XXYY", ((BmsArray)c.getIndexedMeta("#indexed_array36", 2)).toString());
		assertEquals("YYZZ", ((BmsArray)c.getIndexedMeta("#indexed_array36", 3)).toString());
		assertEquals("OBJ1", c.getIndexedMeta("#indexed_object", 0));
		assertEquals("OBJ2", c.getIndexedMeta("#indexed_object", 2));
		assertEquals("OBJ3", c.getIndexedMeta("#indexed_object", 3));
	}

	// putMeta(BmsMetaElement)
	// 値がnullの単体メタ情報を指定すると当該メタ情報が消去されること
	@Test
	public void testPutMeta_NullValue_Single() {
		var s = spec();
		var c = new BmsContent(s);
		c.beginEdit();
		c.putMeta(new BmsMetaElement(s.getSingleMeta("#single_integer"), 0, 100L));
		c.putMeta(new BmsMetaElement(s.getSingleMeta("#single_integer"), 0, null));
		c.putMeta(new BmsMetaElement(s.getSingleMeta("#single_float"), 0, 200.345));
		c.putMeta(new BmsMetaElement(s.getSingleMeta("#single_float"), 0, null));
		c.putMeta(new BmsMetaElement(s.getSingleMeta("#single_string"), 0, "STR"));
		c.putMeta(new BmsMetaElement(s.getSingleMeta("#single_string"), 0, null));
		c.putMeta(new BmsMetaElement(s.getSingleMeta("#single_base16"), 0, BmsInt.to16i("AB")));
		c.putMeta(new BmsMetaElement(s.getSingleMeta("#single_base16"), 0, null));
		c.putMeta(new BmsMetaElement(s.getSingleMeta("#single_base36"), 0, BmsInt.to36i("XY")));
		c.putMeta(new BmsMetaElement(s.getSingleMeta("#single_base36"), 0, null));
		c.putMeta(new BmsMetaElement(s.getSingleMeta("#single_array16"), 0, new BmsArray("AABB", 16)));
		c.putMeta(new BmsMetaElement(s.getSingleMeta("#single_array16"), 0, null));
		c.putMeta(new BmsMetaElement(s.getSingleMeta("#single_array36"), 0, new BmsArray("XXYY", 36)));
		c.putMeta(new BmsMetaElement(s.getSingleMeta("#single_array36"), 0, null));
		c.putMeta(new BmsMetaElement(s.getSingleMeta("#single_object"), 0, "OBJ"));
		c.putMeta(new BmsMetaElement(s.getSingleMeta("#single_object"), 0, null));
		c.endEdit();
		assertFalse(c.containsSingleMeta("#single_integer"));
		assertFalse(c.containsSingleMeta("#single_float"));
		assertFalse(c.containsSingleMeta("#single_string"));
		assertFalse(c.containsSingleMeta("#single_base16"));
		assertFalse(c.containsSingleMeta("#single_base36"));
		assertFalse(c.containsSingleMeta("#single_array16"));
		assertFalse(c.containsSingleMeta("#single_array36"));
		assertFalse(c.containsSingleMeta("#single_object"));
	}

	// putMeta(BmsMetaElement)
	// 値がnullの複数メタ情報を指定すると当該メタ情報が消去されること
	@Test
	public void testPutMeta_NullValue_Multiple() {
		var s = spec();
		var c = new BmsContent(s);
		c.beginEdit();
		c.putMeta(new BmsMetaElement(s.getMultipleMeta("#multiple_integer"), 0, 100L));
		c.putMeta(new BmsMetaElement(s.getMultipleMeta("#multiple_integer"), 1, 200L));
		c.putMeta(new BmsMetaElement(s.getMultipleMeta("#multiple_integer"), 0, null));
		c.putMeta(new BmsMetaElement(s.getMultipleMeta("#multiple_float"), 0, 200.345));
		c.putMeta(new BmsMetaElement(s.getMultipleMeta("#multiple_float"), 1, 300.456));
		c.putMeta(new BmsMetaElement(s.getMultipleMeta("#multiple_float"), 0, null));
		c.putMeta(new BmsMetaElement(s.getMultipleMeta("#multiple_string"), 0, "STR1"));
		c.putMeta(new BmsMetaElement(s.getMultipleMeta("#multiple_string"), 1, "STR2"));
		c.putMeta(new BmsMetaElement(s.getMultipleMeta("#multiple_string"), 0, null));
		c.putMeta(new BmsMetaElement(s.getMultipleMeta("#multiple_base16"), 0, BmsInt.to16i("AB")));
		c.putMeta(new BmsMetaElement(s.getMultipleMeta("#multiple_base16"), 1, BmsInt.to16i("BC")));
		c.putMeta(new BmsMetaElement(s.getMultipleMeta("#multiple_base16"), 0, null));
		c.putMeta(new BmsMetaElement(s.getMultipleMeta("#multiple_base36"), 0, BmsInt.to36i("WX")));
		c.putMeta(new BmsMetaElement(s.getMultipleMeta("#multiple_base36"), 1, BmsInt.to36i("XY")));
		c.putMeta(new BmsMetaElement(s.getMultipleMeta("#multiple_base36"), 0, null));
		c.putMeta(new BmsMetaElement(s.getMultipleMeta("#multiple_array16"), 0, new BmsArray("AABB", 16)));
		c.putMeta(new BmsMetaElement(s.getMultipleMeta("#multiple_array16"), 1, new BmsArray("BBCC", 16)));
		c.putMeta(new BmsMetaElement(s.getMultipleMeta("#multiple_array16"), 0, null));
		c.putMeta(new BmsMetaElement(s.getMultipleMeta("#multiple_array36"), 0, new BmsArray("WWXX", 36)));
		c.putMeta(new BmsMetaElement(s.getMultipleMeta("#multiple_array36"), 1, new BmsArray("XXYY", 36)));
		c.putMeta(new BmsMetaElement(s.getMultipleMeta("#multiple_array36"), 0, null));
		c.putMeta(new BmsMetaElement(s.getMultipleMeta("#multiple_object"), 0, "OBJ1"));
		c.putMeta(new BmsMetaElement(s.getMultipleMeta("#multiple_object"), 1, "OBJ2"));
		c.putMeta(new BmsMetaElement(s.getMultipleMeta("#multiple_object"), 0, null));
		c.endEdit();
		// 複数メタ情報は配列途中のデータが消去されると配列の敷き詰めが発生するため、[0]がtrue, [1]がfalse
		assertTrue(c.containsMultipleMeta("#multiple_integer", 0));
		assertFalse(c.containsMultipleMeta("#multiple_integer", 1));
		assertTrue(c.containsMultipleMeta("#multiple_float", 0));
		assertFalse(c.containsMultipleMeta("#multiple_float", 1));
		assertTrue(c.containsMultipleMeta("#multiple_string", 0));
		assertFalse(c.containsMultipleMeta("#multiple_string", 1));
		assertTrue(c.containsMultipleMeta("#multiple_base16", 0));
		assertFalse(c.containsMultipleMeta("#multiple_base16", 1));
		assertTrue(c.containsMultipleMeta("#multiple_base36", 0));
		assertFalse(c.containsMultipleMeta("#multiple_base36", 1));
		assertTrue(c.containsMultipleMeta("#multiple_array16", 0));
		assertFalse(c.containsMultipleMeta("#multiple_array16", 1));
		assertTrue(c.containsMultipleMeta("#multiple_array36", 0));
		assertFalse(c.containsMultipleMeta("#multiple_array36", 1));
		assertTrue(c.containsMultipleMeta("#multiple_object", 0));
		assertFalse(c.containsMultipleMeta("#multiple_object", 1));
	}

	// putMeta(BmsMetaElement)
	// 値がnullの索引付きメタ情報を指定すると当該メタ情報が消去されること
	@Test
	public void testPutMeta_NullValue_Indexed() {
		var s = spec();
		var c = new BmsContent(s);
		c.beginEdit();
		c.putMeta(new BmsMetaElement(s.getIndexedMeta("#indexed_integer"), 0, 100L));
		c.putMeta(new BmsMetaElement(s.getIndexedMeta("#indexed_integer"), 2, 200L));
		c.putMeta(new BmsMetaElement(s.getIndexedMeta("#indexed_integer"), 0, null));
		c.putMeta(new BmsMetaElement(s.getIndexedMeta("#indexed_float"), 0, 200.345));
		c.putMeta(new BmsMetaElement(s.getIndexedMeta("#indexed_float"), 2, 300.456));
		c.putMeta(new BmsMetaElement(s.getIndexedMeta("#indexed_float"), 0, null));
		c.putMeta(new BmsMetaElement(s.getIndexedMeta("#indexed_string"), 0, "STR1"));
		c.putMeta(new BmsMetaElement(s.getIndexedMeta("#indexed_string"), 2, "STR2"));
		c.putMeta(new BmsMetaElement(s.getIndexedMeta("#indexed_string"), 0, null));
		c.putMeta(new BmsMetaElement(s.getIndexedMeta("#indexed_base16"), 0, BmsInt.to16i("AB")));
		c.putMeta(new BmsMetaElement(s.getIndexedMeta("#indexed_base16"), 2, BmsInt.to16i("BC")));
		c.putMeta(new BmsMetaElement(s.getIndexedMeta("#indexed_base16"), 0, null));
		c.putMeta(new BmsMetaElement(s.getIndexedMeta("#indexed_base36"), 0, BmsInt.to36i("WX")));
		c.putMeta(new BmsMetaElement(s.getIndexedMeta("#indexed_base36"), 2, BmsInt.to36i("XY")));
		c.putMeta(new BmsMetaElement(s.getIndexedMeta("#indexed_base36"), 0, null));
		c.putMeta(new BmsMetaElement(s.getIndexedMeta("#indexed_array16"), 0, new BmsArray("AABB", 16)));
		c.putMeta(new BmsMetaElement(s.getIndexedMeta("#indexed_array16"), 2, new BmsArray("BBCC", 16)));
		c.putMeta(new BmsMetaElement(s.getIndexedMeta("#indexed_array16"), 0, null));
		c.putMeta(new BmsMetaElement(s.getIndexedMeta("#indexed_array36"), 0, new BmsArray("WWXX", 36)));
		c.putMeta(new BmsMetaElement(s.getIndexedMeta("#indexed_array36"), 2, new BmsArray("XXYY", 36)));
		c.putMeta(new BmsMetaElement(s.getIndexedMeta("#indexed_array36"), 0, null));
		c.putMeta(new BmsMetaElement(s.getIndexedMeta("#indexed_object"), 0, "OBJ1"));
		c.putMeta(new BmsMetaElement(s.getIndexedMeta("#indexed_object"), 2, "OBJ2"));
		c.putMeta(new BmsMetaElement(s.getIndexedMeta("#indexed_object"), 0, null));
		c.endEdit();
		assertFalse(c.containsIndexedMeta("#indexed_integer", 0));
		assertTrue(c.containsIndexedMeta("#indexed_integer", 2));
		assertFalse(c.containsIndexedMeta("#indexed_float", 0));
		assertTrue(c.containsIndexedMeta("#indexed_float", 2));
		assertFalse(c.containsIndexedMeta("#indexed_string", 0));
		assertTrue(c.containsIndexedMeta("#indexed_string", 2));
		assertFalse(c.containsIndexedMeta("#indexed_base16", 0));
		assertTrue(c.containsIndexedMeta("#indexed_base16", 2));
		assertFalse(c.containsIndexedMeta("#indexed_base36", 0));
		assertTrue(c.containsIndexedMeta("#indexed_base36", 2));
		assertFalse(c.containsIndexedMeta("#indexed_array16", 0));
		assertTrue(c.containsIndexedMeta("#indexed_array16", 2));
		assertFalse(c.containsIndexedMeta("#indexed_array36", 0));
		assertTrue(c.containsIndexedMeta("#indexed_array36", 2));
		assertFalse(c.containsIndexedMeta("#indexed_object", 0));
		assertTrue(c.containsIndexedMeta("#indexed_object", 2));
	}

	// putMeta(BmsMetaElement)
	// 複数メタ情報の空要素が無視され、処理が正常に完了すること
	@Test
	public void testPutMeta_MultipleEmptyElement() {
		var s = spec();
		var c = new BmsContent(s);
		c.beginEdit();
		c.putMeta(new BmsMetaElement(s.getMultipleMeta("#multiple_integer"), 0, 100L));
		c.putMeta(new BmsMetaElement(s.getMultipleMeta("#multiple_integer"), 1, 200L));
		c.putMeta(new BmsMetaElement(s.getMultipleMeta("#multiple_integer"), -1, null));
		c.endEdit();
		assertEquals(2, c.getMultipleMetaCount("#multiple_integer"));
		assertTrue(c.containsMultipleMeta("#multiple_integer", 0));
		assertTrue(c.containsMultipleMeta("#multiple_integer", 1));
	}

	// putMeta(BmsMetaElement)
	// 索引付きメタ情報の空要素が無視され、処理が正常に完了すること
	@Test
	public void testPutMeta_IndexedEmptyElement() {
		var s = spec();
		var c = new BmsContent(s);
		c.beginEdit();
		c.putMeta(new BmsMetaElement(s.getIndexedMeta("#indexed_integer"), 0, 100L));
		c.putMeta(new BmsMetaElement(s.getIndexedMeta("#indexed_integer"), 2, 200L));
		c.putMeta(new BmsMetaElement(s.getMultipleMeta("#indexed_integer"), -1, null));
		c.endEdit();
		assertEquals(2, c.getIndexedMetaCount("#indexed_integer"));
		assertTrue(c.containsIndexedMeta("#indexed_integer", 0));
		assertTrue(c.containsIndexedMeta("#indexed_integer", 2));
	}

	// putMeta(BmsMetaElement)
	// IllegalStateException 動作モードが編集モードではない
	@Test
	public void testPutMeta_NotEditMode() {
		var s = spec();
		var c = new BmsContent(s);
		var ex = IllegalStateException.class;
		assertThrows(ex, () -> c.putMeta(new BmsMetaElement(s.getSingleMeta("#single_integer"), 0, 100L)));
	}

	// putMeta(BmsMetaElement)
	// NullPointerException metaがnull
	@Test
	public void testPutMeta_NullElement() {
		var s = spec();
		var c = new BmsContent(s);
		var ex = NullPointerException.class;
		c.beginEdit();
		assertThrows(ex, () -> c.putMeta(null));
	}

	// putMeta(BmsMetaElement)
	// ClassCastException メタ情報の値が設定先メタ情報のデータ型に変換できない
	@Test
	public void testPutMeta_BadCast() {
		var s = spec();
		var c = new BmsContent(s);
		var ex = ClassCastException.class;
		c.beginEdit();
		assertThrows(ex, () -> c.putMeta(new BmsMetaElement(s.getSingleMeta("#single_integer"), 0, "BadCast")));
		// ※同じ名前の異なる型が入力された前提で要素を生成し、それを入力データとする
	}

	// putMultipleMeta(String, Object)
	// 正常
	@Test
	public void testPutMultipleMeta_001() {
		BmsContent c;

		// setMultipleMetaとの使い分け
		c = new BmsContent(spec());
		c.beginEdit();
		c.setMultipleMeta("#multiple_integer", 0, 0);
		c.setMultipleMeta("#multiple_integer", 1, 1);
		c.putMultipleMeta("#multiple_integer", 10);
		c.putMultipleMeta("#multiple_integer", 20);
		c.endEdit();
		assertEquals(4, c.getMultipleMetaCount("#multiple_integer"));
		assertEquals(0L, (long)c.getMultipleMeta("#multiple_integer", 0));
		assertEquals(1L, (long)c.getMultipleMeta("#multiple_integer", 1));
		assertEquals(10L, (long)c.getMultipleMeta("#multiple_integer", 2));
		assertEquals(20L, (long)c.getMultipleMeta("#multiple_integer", 3));

		// setMultipleMetaで歯抜け挿入した時の挙動
		c = new BmsContent(spec());
		c.beginEdit();
		c.setMultipleMeta("#multiple_integer", 0, 0);
		c.setMultipleMeta("#multiple_integer", 3, 100);
		c.putMultipleMeta("#multiple_integer", 200);
		c.putMultipleMeta("#multiple_integer", 300);
		c.endEdit();
		assertEquals(6, c.getMultipleMetaCount("#multiple_integer"));
		assertEquals(0L, (long)c.getMultipleMeta("#multiple_integer", 0));
		assertEquals((long)c.getSpec().getMultipleMeta("#multiple_integer").getDefaultValue(), (long)c.getMultipleMeta("#multiple_integer", 1));
		assertEquals((long)c.getSpec().getMultipleMeta("#multiple_integer").getDefaultValue(), (long)c.getMultipleMeta("#multiple_integer", 2));
		assertEquals(100L, (long)c.getMultipleMeta("#multiple_integer", 3));
		assertEquals(200L, (long)c.getMultipleMeta("#multiple_integer", 4));
		assertEquals(300L, (long)c.getMultipleMeta("#multiple_integer", 5));
	}

	// putMultipleMeta(String, Object)
	// IllegalStateException 動作モードが編集モードではない
	@Test
	public void testPutMultipleMeta_002() {
		var c = new BmsContent(spec());
		assertThrows(IllegalStateException.class, () -> c.putMultipleMeta("#multiple_integer", 0));
	}

	// putMultipleMeta(String, Object)
	// NullPointerException nameがnull
	@Test
	public void testPutMultipleMeta_003() {
		var c = new BmsContent(spec());
		c.beginEdit();
		assertThrows(NullPointerException.class, () -> c.putMultipleMeta(null, 0));
	}

	// putMultipleMeta(String, Object)
	// NullPointerException valueがnull
	@Test
	public void testPutMultipleMeta_004() {
		var c = new BmsContent(spec());
		c.beginEdit();
		assertThrows(NullPointerException.class, () -> c.putMultipleMeta("#multiple_integer", null));
	}

	// putMultipleMeta(String, Object)
	// IllegalArgumentException 名称に合致するメタ情報が存在しない
	@Test
	public void testPutMultipleMeta_005() {
		var c = new BmsContent(spec());
		c.beginEdit();
		assertThrows(IllegalArgumentException.class, () -> c.putMultipleMeta("#unknown", 0));
	}

	// putMultipleMeta(String, Object)
	// IndexOutOfBoundsException リストが一杯でこれ以上値を挿入できない
	@Test
	public void testPutMultipleMeta_006() {
		var c = new BmsContent(spec());
		c.beginEdit();
		for (var i = 0; i <= BmsSpec.MULTIPLE_META_INDEX_MAX; i++) {
			c.putMultipleMeta("#multiple_integer", 0);
		}
		assertThrows(IndexOutOfBoundsException.class, () -> c.putMultipleMeta("#multiple_integer", 0));
	}

	// putMultipleMeta(String, Object)
	// ClassCastException valueが設定先メタ情報のデータ型に変換できない
	@Test
	public void testPutMultipleMeta_007() {
		var c = new BmsContent(spec());
		c.beginEdit();
		assertThrows(ClassCastException.class, () -> c.putMultipleMeta("#multiple_integer", "not_number"));
	}

	// getSingleMeta(String)
	// 正常
	@Test
	public void testGetSingleMeta_001() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.setSingleMeta("#single_integer", 0);
		c.setSingleMeta("#single_float", 200.123);
		c.setSingleMeta("#single_string", "hoge");
		c.setSingleMeta("#single_base16", 0xaf);
		c.setSingleMeta("#single_base36", Integer.parseInt("gz", 36));
		c.setSingleMeta("#single_array16", "0088aaff");
		c.setSingleMeta("#single_array36", "0099ffkkppzz");
		c.endEdit();

		// 通常の取得処理
		assertEquals(0L, (long)c.getSingleMeta("#single_integer"));
		assertEquals(200.123, (double)c.getSingleMeta("#single_float"), 0.0001);
		assertEquals("hoge", (String)c.getSingleMeta("#single_string"));
		assertEquals(0xafL, (long)c.getSingleMeta("#single_base16"));
		assertEquals(Long.parseLong("gz", 36), (long)c.getSingleMeta("#single_base36"));
		assertEquals(new BmsArray("0088aaff", 16), (BmsArray)c.getSingleMeta("#single_array16"));
		assertEquals(new BmsArray("0099ffkkppzz", 36), (BmsArray)c.getSingleMeta("#single_array36"));

		// 値を設定していないメタ情報は初期値
		c = new BmsContent(spec());
		assertEquals((long)c.getSpec().getSingleMeta("#single_integer").getDefaultValue(), (long)c.getSingleMeta("#single_integer"));
		assertEquals((double)c.getSpec().getSingleMeta("#single_float").getDefaultValue(), (double)c.getSingleMeta("#single_float"), 0.0);
		assertEquals((String)c.getSpec().getSingleMeta("#single_string").getDefaultValue(), (String)c.getSingleMeta("#single_string"));
		assertEquals((long)c.getSpec().getSingleMeta("#single_base16").getDefaultValue(), (long)c.getSingleMeta("#single_base16"));
		assertEquals((long)c.getSpec().getSingleMeta("#single_base36").getDefaultValue(), (long)c.getSingleMeta("#single_base36"));
		assertEquals((BmsArray)c.getSpec().getSingleMeta("#single_array16").getDefaultValue(), (BmsArray)c.getSingleMeta("#single_array16"));
		assertEquals((BmsArray)c.getSpec().getSingleMeta("#single_array36").getDefaultValue(), (BmsArray)c.getSingleMeta("#single_array36"));
	}

	// getSingleMeta(String)
	// NullPointerException nameがnull
	@Test
	public void testGetSingleMeta_002() {
		var c = new BmsContent(spec());
		assertThrows(NullPointerException.class, () -> c.getSingleMeta(null));
	}

	// getSingleMeta(String)
	// IllegalArgumentException 名称に合致するメタ情報が存在しない
	@Test
	public void testGetSingleMeta_003() {
		var c = new BmsContent(spec());
		assertThrows(IllegalArgumentException.class, () -> c.getSingleMeta("#indexed_integer"));
	}

	// getMultipleMeta(String, int)
	// 正常
	@Test
	public void testGetMultipleMeta_001() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.setMultipleMeta("#multiple_integer", 0, 0);
		c.setMultipleMeta("#multiple_float", 0, 200.123);
		c.setMultipleMeta("#multiple_string", 0, "hoge");
		c.setMultipleMeta("#multiple_base16", 0, 0xaf);
		c.setMultipleMeta("#multiple_base36", 0, Integer.parseInt("gz", 36));
		c.setMultipleMeta("#multiple_array16", 0, "0088aaff");
		c.setMultipleMeta("#multiple_array36", 0, "0099ffkkppzz");
		c.endEdit();

		// 通常の取得処理
		assertEquals(0L, (long)c.getMultipleMeta("#multiple_integer", 0));
		assertEquals(200.123, (double)c.getMultipleMeta("#multiple_float", 0), 0.0001);
		assertEquals("hoge", (String)c.getMultipleMeta("#multiple_string", 0));
		assertEquals(0xafL, (long)c.getMultipleMeta("#multiple_base16", 0));
		assertEquals(Long.parseLong("gz", 36), (long)c.getMultipleMeta("#multiple_base36", 0));
		assertEquals(new BmsArray("0088aaff", 16), (BmsArray)c.getMultipleMeta("#multiple_array16", 0));
		assertEquals(new BmsArray("0099ffkkppzz", 36), (BmsArray)c.getMultipleMeta("#multiple_array36", 0));

		// 値を設定していないメタ情報は初期値
		c = new BmsContent(spec());
		assertEquals((long)c.getSpec().getMultipleMeta("#multiple_integer").getDefaultValue(), (long)c.getMultipleMeta("#multiple_integer", 0));
		assertEquals((double)c.getSpec().getMultipleMeta("#multiple_float").getDefaultValue(), (double)c.getMultipleMeta("#multiple_float", 0), 0.0);
		assertEquals((String)c.getSpec().getMultipleMeta("#multiple_string").getDefaultValue(), (String)c.getMultipleMeta("#multiple_string", 0));
		assertEquals((long)c.getSpec().getMultipleMeta("#multiple_base16").getDefaultValue(), (long)c.getMultipleMeta("#multiple_base16", 0));
		assertEquals((long)c.getSpec().getMultipleMeta("#multiple_base36").getDefaultValue(), (long)c.getMultipleMeta("#multiple_base36", 0));
		assertEquals((BmsArray)c.getSpec().getMultipleMeta("#multiple_array16").getDefaultValue(), (BmsArray)c.getMultipleMeta("#multiple_array16", 0));
		assertEquals((BmsArray)c.getSpec().getMultipleMeta("#multiple_array36").getDefaultValue(), (BmsArray)c.getMultipleMeta("#multiple_array36", 0));

		// 歯抜け部分と件数超過部分は初期値
		c = new BmsContent(spec());
		c.beginEdit();
		c.setMultipleMeta("#multiple_integer", 0, -100);
		c.setMultipleMeta("#multiple_integer", 3, 100);
		c.endEdit();
		assertEquals(-100L, (long)c.getMultipleMeta("#multiple_integer", 0));
		assertEquals((long)c.getSpec().getMultipleMeta("#multiple_integer").getDefaultValue(), (long)c.getMultipleMeta("#multiple_integer", 1));
		assertEquals((long)c.getSpec().getMultipleMeta("#multiple_integer").getDefaultValue(), (long)c.getMultipleMeta("#multiple_integer", 2));
		assertEquals(100L, (long)c.getMultipleMeta("#multiple_integer", 3));
		assertEquals((long)c.getSpec().getMultipleMeta("#multiple_integer").getDefaultValue(), (long)c.getMultipleMeta("#multiple_integer", 4));
	}

	// getMultipleMeta(String, int)
	// NullPointerException nameがnull
	@Test
	public void testGetMultipleMeta_002() {
		var c = new BmsContent(spec());
		assertThrows(NullPointerException.class, () -> c.getMultipleMeta(null, 0));
	}

	// getMultipleMeta(String, int)
	// IndexOutOfBoundsException indexが0～{@link BmsSpec#MULTIPLE_META_INDEX_MAX}の範囲外
	@Test
	public void testGetMultipleMeta_003() {
		var c = new BmsContent(spec());
		var ex = IndexOutOfBoundsException.class;
		assertThrows(ex, () -> c.getMultipleMeta("#multiple_integer", -1));
		assertThrows(ex, () -> c.getMultipleMeta("#multiple_integer", BmsSpec.MULTIPLE_META_INDEX_MAX + 1));
	}

	// getMultipleMeta(String, int)
	// IllegalArgumentException 名称に合致するメタ情報が存在しない
	@Test
	public void testGetMultipleMeta_004() {
		var c = new BmsContent(spec());
		assertThrows(IllegalArgumentException.class, () -> c.getMultipleMeta("#single_integer", 0));
	}

	// getIndexedMeta(String, int)
	// 正常
	@Test
	public void testGetIndexedMeta_001() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.setIndexedMeta("#indexed_integer", 0, 0);
		c.setIndexedMeta("#indexed_float", 0, 200.123);
		c.setIndexedMeta("#indexed_string", 0, "hoge");
		c.setIndexedMeta("#indexed_base16", 0, 0xaf);
		c.setIndexedMeta("#indexed_base36", 0, Integer.parseInt("gz", 36));
		c.setIndexedMeta("#indexed_array16", 0, "0088aaff");
		c.setIndexedMeta("#indexed_array36", 0, "0099ffkkppzz");
		c.endEdit();

		// 通常の取得処理
		assertEquals(0L, (long)c.getIndexedMeta("#indexed_integer", 0));
		assertEquals(200.123, (double)c.getIndexedMeta("#indexed_float", 0), 0.0001);
		assertEquals("hoge", (String)c.getIndexedMeta("#indexed_string", 0));
		assertEquals(0xafL, (long)c.getIndexedMeta("#indexed_base16", 0));
		assertEquals(Long.parseLong("gz", 36), (long)c.getIndexedMeta("#indexed_base36", 0));
		assertEquals(new BmsArray("0088aaff", 16), (BmsArray)c.getIndexedMeta("#indexed_array16", 0));
		assertEquals(new BmsArray("0099ffkkppzz", 36), (BmsArray)c.getIndexedMeta("#indexed_array36", 0));

		// 値を設定していないメタ情報は初期値
		c = new BmsContent(spec());
		assertEquals((long)c.getSpec().getIndexedMeta("#indexed_integer").getDefaultValue(), (long)c.getIndexedMeta("#indexed_integer", 0));
		assertEquals((double)c.getSpec().getIndexedMeta("#indexed_float").getDefaultValue(), (double)c.getIndexedMeta("#indexed_float", 0), 0.0);
		assertEquals((String)c.getSpec().getIndexedMeta("#indexed_string").getDefaultValue(), (String)c.getIndexedMeta("#indexed_string", 0));
		assertEquals((long)c.getSpec().getIndexedMeta("#indexed_base16").getDefaultValue(), (long)c.getIndexedMeta("#indexed_base16", 0));
		assertEquals((long)c.getSpec().getIndexedMeta("#indexed_base36").getDefaultValue(), (long)c.getIndexedMeta("#indexed_base36", 0));
		assertEquals((BmsArray)c.getSpec().getIndexedMeta("#indexed_array16").getDefaultValue(), (BmsArray)c.getIndexedMeta("#indexed_array16", 0));
		assertEquals((BmsArray)c.getSpec().getIndexedMeta("#indexed_array36").getDefaultValue(), (BmsArray)c.getIndexedMeta("#indexed_array36", 0));
	}

	// getIndexedMeta(String, int)
	// NullPointerException nameがnull
	@Test
	public void testGetIndexedMeta_002() {
		var c = new BmsContent(spec());
		assertThrows(NullPointerException.class, () -> c.getIndexedMeta(null, 0));
	}

	// getIndexedMeta(String, int)
	// IndexOutOfBoundsException indexが0～{@link BmsSpec#INDEXED_META_INDEX_MAX}の範囲外
	@Test
	public void testGetIndexedMeta_003() {
		var c = new BmsContent(spec());
		var ex = IndexOutOfBoundsException.class;
		assertThrows(ex, () -> c.getIndexedMeta("#indexed_integer", -1));
		assertThrows(ex, () -> c.getIndexedMeta("#indexed_integer", BmsSpec.INDEXED_META_INDEX_MAX + 1));
	}

	// getIndexedMeta(String, int)
	// IllegalArgumentException 名称に合致するメタ情報が存在しない
	@Test
	public void testGetIndexedMeta_004() {
		var c = new BmsContent(spec());
		assertThrows(IllegalArgumentException.class, () -> c.getIndexedMeta("#multiple_integer", 0));
	}

	// getMeta(BmsMetaKey)
	// 正常
	@Test
	public void testGetMetaBmsMetaKey_001() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.setMeta("#single_integer", BmsUnit.SINGLE, 0, 0);
		c.setMeta("#single_float", BmsUnit.SINGLE, 0, 200.123);
		c.setMeta("#single_string", BmsUnit.SINGLE, 0, "hoge");
		c.setMeta("#single_base16", BmsUnit.SINGLE, 0, 0xaf);
		c.setMeta("#single_base36", BmsUnit.SINGLE, 0, Integer.parseInt("gz", 36));
		c.setMeta("#single_array16", BmsUnit.SINGLE, 0, "0088aaff");
		c.setMeta("#single_array36", BmsUnit.SINGLE, 0, "0099ffkkppzz");

		c.setMeta("#multiple_integer", BmsUnit.MULTIPLE, 0, 0);
		c.setMeta("#multiple_float", BmsUnit.MULTIPLE, 0, 200.123);
		c.setMeta("#multiple_string", BmsUnit.MULTIPLE, 0, "hoge");
		c.setMeta("#multiple_base16", BmsUnit.MULTIPLE, 0, 0xaf);
		c.setMeta("#multiple_base36", BmsUnit.MULTIPLE, 0, Integer.parseInt("gz", 36));
		c.setMeta("#multiple_array16", BmsUnit.MULTIPLE, 0, "0088aaff");
		c.setMeta("#multiple_array36", BmsUnit.MULTIPLE, 0, "0099ffkkppzz");

		c.setMeta("#indexed_integer", BmsUnit.INDEXED, 0, 0);
		c.setMeta("#indexed_float", BmsUnit.INDEXED, 0, 200.123);
		c.setMeta("#indexed_string", BmsUnit.INDEXED, 0, "hoge");
		c.setMeta("#indexed_base16", BmsUnit.INDEXED, 0, 0xaf);
		c.setMeta("#indexed_base36", BmsUnit.INDEXED, 0, Integer.parseInt("gz", 36));
		c.setMeta("#indexed_array16", BmsUnit.INDEXED, 0, "0088aaff");
		c.setMeta("#indexed_array36", BmsUnit.INDEXED, 0, "0099ffkkppzz");
		c.endEdit();

		assertEquals(0L, (long)c.getMeta(new BmsMetaKey("#single_integer", BmsUnit.SINGLE)));
		assertEquals(200.123, (double)c.getMeta(new BmsMetaKey("#single_float", BmsUnit.SINGLE)), 0.0001);
		assertEquals("hoge", (String)c.getMeta(new BmsMetaKey("#single_string", BmsUnit.SINGLE)));
		assertEquals(0xafL, (long)c.getMeta(new BmsMetaKey("#single_base16", BmsUnit.SINGLE)));
		assertEquals(Long.parseLong("gz", 36), (long)c.getMeta(new BmsMetaKey("#single_base36", BmsUnit.SINGLE)));
		assertEquals(new BmsArray("0088aaff", 16), (BmsArray)c.getMeta(new BmsMetaKey("#single_array16", BmsUnit.SINGLE)));
		assertEquals(new BmsArray("0099ffkkppzz", 36), (BmsArray)c.getMeta(new BmsMetaKey("#single_array36", BmsUnit.SINGLE)));

		assertEquals(0L, (long)c.getMeta(new BmsMetaKey("#multiple_integer", BmsUnit.MULTIPLE)));
		assertEquals(200.123, (double)c.getMeta(new BmsMetaKey("#multiple_float", BmsUnit.MULTIPLE)), 0.0001);
		assertEquals("hoge", (String)c.getMeta(new BmsMetaKey("#multiple_string", BmsUnit.MULTIPLE)));
		assertEquals(0xafL, (long)c.getMeta(new BmsMetaKey("#multiple_base16", BmsUnit.MULTIPLE)));
		assertEquals(Long.parseLong("gz", 36), (long)c.getMeta(new BmsMetaKey("#multiple_base36", BmsUnit.MULTIPLE)));
		assertEquals(new BmsArray("0088aaff", 16), (BmsArray)c.getMeta(new BmsMetaKey("#multiple_array16", BmsUnit.MULTIPLE)));
		assertEquals(new BmsArray("0099ffkkppzz", 36), (BmsArray)c.getMeta(new BmsMetaKey("#multiple_array36", BmsUnit.MULTIPLE)));

		assertEquals(0L, (long)c.getMeta(new BmsMetaKey("#indexed_integer", BmsUnit.INDEXED)));
		assertEquals(200.123, (double)c.getMeta(new BmsMetaKey("#indexed_float", BmsUnit.INDEXED)), 0.0001);
		assertEquals("hoge", (String)c.getMeta(new BmsMetaKey("#indexed_string", BmsUnit.INDEXED)));
		assertEquals(0xafL, (long)c.getMeta(new BmsMetaKey("#indexed_base16", BmsUnit.INDEXED)));
		assertEquals(Long.parseLong("gz", 36), (long)c.getMeta(new BmsMetaKey("#indexed_base36", BmsUnit.INDEXED)));
		assertEquals(new BmsArray("0088aaff", 16), (BmsArray)c.getMeta(new BmsMetaKey("#indexed_array16", BmsUnit.INDEXED)));
		assertEquals(new BmsArray("0099ffkkppzz", 36), (BmsArray)c.getMeta(new BmsMetaKey("#indexed_array36", BmsUnit.INDEXED)));
	}

	// getMeta(BmsMetaKey)
	// NullPointerException keyがnull
	@Test
	public void testGetMetaBmsMetaKey_002() {
		var c = new BmsContent(spec());
		assertThrows(NullPointerException.class, () -> c.getMeta(null));
	}

	// getMeta(BmsMetaKey)
	// IllegalArgumentException メタ情報キーに合致するメタ情報が存在しない
	@Test
	public void testGetMetaBmsMetaKey_003() {
		var c = new BmsContent(spec());
		var ex = IllegalArgumentException.class;
		assertThrows(ex, () -> c.getMeta(new BmsMetaKey("#indexed_integer", BmsUnit.SINGLE)));
		assertThrows(ex, () -> c.getMeta(new BmsMetaKey("#single_integer", BmsUnit.MULTIPLE)));
		assertThrows(ex, () -> c.getMeta(new BmsMetaKey("#multiple_integer", BmsUnit.INDEXED)));
	}

	// getMeta(BmsMetaKey, int)
	// 正常
	@Test
	public void testGetMetaBmsMetaKeyInt_001() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.setMeta("#single_integer", BmsUnit.SINGLE, 0, 0);
		c.setMeta("#single_float", BmsUnit.SINGLE, 0, 200.123);
		c.setMeta("#single_string", BmsUnit.SINGLE, 0, "hoge");
		c.setMeta("#single_base16", BmsUnit.SINGLE, 0, 0xaf);
		c.setMeta("#single_base36", BmsUnit.SINGLE, 0, Integer.parseInt("gz", 36));
		c.setMeta("#single_array16", BmsUnit.SINGLE, 0, "0088aaff");
		c.setMeta("#single_array36", BmsUnit.SINGLE, 0, "0099ffkkppzz");

		c.setMeta("#multiple_integer", BmsUnit.MULTIPLE, 0, 0);
		c.setMeta("#multiple_float", BmsUnit.MULTIPLE, 0, 200.123);
		c.setMeta("#multiple_string", BmsUnit.MULTIPLE, 0, "hoge");
		c.setMeta("#multiple_base16", BmsUnit.MULTIPLE, 0, 0xaf);
		c.setMeta("#multiple_base36", BmsUnit.MULTIPLE, 0, Integer.parseInt("gz", 36));
		c.setMeta("#multiple_array16", BmsUnit.MULTIPLE, 0, "0088aaff");
		c.setMeta("#multiple_array36", BmsUnit.MULTIPLE, 0, "0099ffkkppzz");

		c.setMeta("#indexed_integer", BmsUnit.INDEXED, 0, 0);
		c.setMeta("#indexed_float", BmsUnit.INDEXED, 0, 200.123);
		c.setMeta("#indexed_string", BmsUnit.INDEXED, 0, "hoge");
		c.setMeta("#indexed_base16", BmsUnit.INDEXED, 0, 0xaf);
		c.setMeta("#indexed_base36", BmsUnit.INDEXED, 0, Integer.parseInt("gz", 36));
		c.setMeta("#indexed_array16", BmsUnit.INDEXED, 0, "0088aaff");
		c.setMeta("#indexed_array36", BmsUnit.INDEXED, 0, "0099ffkkppzz");
		c.endEdit();

		assertEquals(0L, (long)c.getMeta(new BmsMetaKey("#single_integer", BmsUnit.SINGLE), 0));
		assertEquals(200.123, (double)c.getMeta(new BmsMetaKey("#single_float", BmsUnit.SINGLE), 0), 0.0001);
		assertEquals("hoge", (String)c.getMeta(new BmsMetaKey("#single_string", BmsUnit.SINGLE), 0));
		assertEquals(0xafL, (long)c.getMeta(new BmsMetaKey("#single_base16", BmsUnit.SINGLE), 0));
		assertEquals(Long.parseLong("gz", 36), (long)c.getMeta(new BmsMetaKey("#single_base36", BmsUnit.SINGLE), 0));
		assertEquals(new BmsArray("0088aaff", 16), (BmsArray)c.getMeta(new BmsMetaKey("#single_array16", BmsUnit.SINGLE), 0));
		assertEquals(new BmsArray("0099ffkkppzz", 36), (BmsArray)c.getMeta(new BmsMetaKey("#single_array36", BmsUnit.SINGLE), 0));

		assertEquals(0L, (long)c.getMeta(new BmsMetaKey("#multiple_integer", BmsUnit.MULTIPLE), 0));
		assertEquals(200.123, (double)c.getMeta(new BmsMetaKey("#multiple_float", BmsUnit.MULTIPLE), 0), 0.0001);
		assertEquals("hoge", (String)c.getMeta(new BmsMetaKey("#multiple_string", BmsUnit.MULTIPLE), 0));
		assertEquals(0xafL, (long)c.getMeta(new BmsMetaKey("#multiple_base16", BmsUnit.MULTIPLE), 0));
		assertEquals(Long.parseLong("gz", 36), (long)c.getMeta(new BmsMetaKey("#multiple_base36", BmsUnit.MULTIPLE), 0));
		assertEquals(new BmsArray("0088aaff", 16), (BmsArray)c.getMeta(new BmsMetaKey("#multiple_array16", BmsUnit.MULTIPLE), 0));
		assertEquals(new BmsArray("0099ffkkppzz", 36), (BmsArray)c.getMeta(new BmsMetaKey("#multiple_array36", BmsUnit.MULTIPLE), 0));

		assertEquals(0L, (long)c.getMeta(new BmsMetaKey("#indexed_integer", BmsUnit.INDEXED), 0));
		assertEquals(200.123, (double)c.getMeta(new BmsMetaKey("#indexed_float", BmsUnit.INDEXED), 0), 0.0001);
		assertEquals("hoge", (String)c.getMeta(new BmsMetaKey("#indexed_string", BmsUnit.INDEXED), 0));
		assertEquals(0xafL, (long)c.getMeta(new BmsMetaKey("#indexed_base16", BmsUnit.INDEXED), 0));
		assertEquals(Long.parseLong("gz", 36), (long)c.getMeta(new BmsMetaKey("#indexed_base36", BmsUnit.INDEXED), 0));
		assertEquals(new BmsArray("0088aaff", 16), (BmsArray)c.getMeta(new BmsMetaKey("#indexed_array16", BmsUnit.INDEXED), 0));
		assertEquals(new BmsArray("0099ffkkppzz", 36), (BmsArray)c.getMeta(new BmsMetaKey("#indexed_array36", BmsUnit.INDEXED), 0));
	}

	// getMeta(BmsMetaKey, int)
	// NullPointerException keyがnull
	@Test
	public void testGetMetaBmsMetaKeyInt_002() {
		var c = new BmsContent(spec());
		assertThrows(NullPointerException.class, () -> c.getMeta(null, 0));
	}

	// getMeta(BmsMetaKey, int)
	// IllegalArgumentException メタ情報キーに合致するメタ情報が存在しない
	@Test
	public void testGetMetaBmsMetaKeyInt_003() {
		var c = new BmsContent(spec());
		var ex = IllegalArgumentException.class;
		assertThrows(ex, () -> c.getMeta(new BmsMetaKey("#indexed_integer", BmsUnit.SINGLE), 0));
		assertThrows(ex, () -> c.getMeta(new BmsMetaKey("#single_integer", BmsUnit.MULTIPLE), 0));
		assertThrows(ex, () -> c.getMeta(new BmsMetaKey("#multiple_integer", BmsUnit.INDEXED), 0));
	}

	// getMeta(BmsMetaKey, int)
	// IndexOutOfBoundsException 構成単位が単体の時、indexが0以外
	@Test
	public void testGetMetaBmsMetaKeyInt_004() {
		var c = new BmsContent(spec());
		var ex = IndexOutOfBoundsException.class;
		assertThrows(ex, () -> c.getMeta(new BmsMetaKey("#single_integer", BmsUnit.SINGLE), 1));
	}

	// getMeta(BmsMetaKey, int)
	// IndexOutOfBoundsException 構成単位が複数の時、indexが0～{@link BmsSpec#MULTIPLE_META_INDEX_MAX}の範囲外
	@Test
	public void testGetMetaBmsMetaKeyInt_005() {
		var c = new BmsContent(spec());
		var ex = IndexOutOfBoundsException.class;
		assertThrows(ex, () -> c.getMeta(new BmsMetaKey("#multiple_integer", BmsUnit.MULTIPLE), -1));
		assertThrows(ex, () -> c.getMeta(new BmsMetaKey("#multiple_integer", BmsUnit.MULTIPLE), BmsSpec.MULTIPLE_META_INDEX_MAX + 1));
	}

	// getMeta(BmsMetaKey, int)
	// IndexOutOfBoundsException 構成単位が索引付きの時、indexが0～{@link BmsSpec#INDEXED_META_INDEX_MAX}の範囲外
	@Test
	public void testGetMetaBmsMetaKeyInt_006() {
		var c = new BmsContent(spec());
		c.beginEdit();
		var ex = IndexOutOfBoundsException.class;
		assertThrows(ex, () -> c.getMeta(new BmsMetaKey("#indexed_integer", BmsUnit.INDEXED), -1));
		assertThrows(ex, () -> c.getMeta(new BmsMetaKey("#indexed_integer", BmsUnit.INDEXED), BmsSpec.INDEXED_META_INDEX_MAX + 1));
	}

	// getMeta(String, BmsUnit, int)
	// 正常
	@Test
	public void testGetMeta_001() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.setMeta("#single_integer", BmsUnit.SINGLE, 0, 0);
		c.setMeta("#single_float", BmsUnit.SINGLE, 0, 200.123);
		c.setMeta("#single_string", BmsUnit.SINGLE, 0, "hoge");
		c.setMeta("#single_base16", BmsUnit.SINGLE, 0, 0xaf);
		c.setMeta("#single_base36", BmsUnit.SINGLE, 0, Integer.parseInt("gz", 36));
		c.setMeta("#single_array16", BmsUnit.SINGLE, 0, "0088aaff");
		c.setMeta("#single_array36", BmsUnit.SINGLE, 0, "0099ffkkppzz");

		c.setMeta("#multiple_integer", BmsUnit.MULTIPLE, 0, 0);
		c.setMeta("#multiple_float", BmsUnit.MULTIPLE, 0, 200.123);
		c.setMeta("#multiple_string", BmsUnit.MULTIPLE, 0, "hoge");
		c.setMeta("#multiple_base16", BmsUnit.MULTIPLE, 0, 0xaf);
		c.setMeta("#multiple_base36", BmsUnit.MULTIPLE, 0, Integer.parseInt("gz", 36));
		c.setMeta("#multiple_array16", BmsUnit.MULTIPLE, 0, "0088aaff");
		c.setMeta("#multiple_array36", BmsUnit.MULTIPLE, 0, "0099ffkkppzz");

		c.setMeta("#indexed_integer", BmsUnit.INDEXED, 0, 0);
		c.setMeta("#indexed_float", BmsUnit.INDEXED, 0, 200.123);
		c.setMeta("#indexed_string", BmsUnit.INDEXED, 0, "hoge");
		c.setMeta("#indexed_base16", BmsUnit.INDEXED, 0, 0xaf);
		c.setMeta("#indexed_base36", BmsUnit.INDEXED, 0, Integer.parseInt("gz", 36));
		c.setMeta("#indexed_array16", BmsUnit.INDEXED, 0, "0088aaff");
		c.setMeta("#indexed_array36", BmsUnit.INDEXED, 0, "0099ffkkppzz");
		c.endEdit();

		assertEquals(0L, (long)c.getMeta("#single_integer", BmsUnit.SINGLE, 0));
		assertEquals(200.123, (double)c.getMeta("#single_float", BmsUnit.SINGLE, 0), 0.0001);
		assertEquals("hoge", (String)c.getMeta("#single_string", BmsUnit.SINGLE, 0));
		assertEquals(0xafL, (long)c.getMeta("#single_base16", BmsUnit.SINGLE, 0));
		assertEquals(Long.parseLong("gz", 36), (long)c.getMeta("#single_base36", BmsUnit.SINGLE, 0));
		assertEquals(new BmsArray("0088aaff", 16), (BmsArray)c.getMeta("#single_array16", BmsUnit.SINGLE, 0));
		assertEquals(new BmsArray("0099ffkkppzz", 36), (BmsArray)c.getMeta("#single_array36", BmsUnit.SINGLE, 0));

		assertEquals(0L, (long)c.getMeta("#multiple_integer", BmsUnit.MULTIPLE, 0));
		assertEquals(200.123, (double)c.getMeta("#multiple_float", BmsUnit.MULTIPLE, 0), 0.0001);
		assertEquals("hoge", (String)c.getMeta("#multiple_string", BmsUnit.MULTIPLE, 0));
		assertEquals(0xafL, (long)c.getMeta("#multiple_base16", BmsUnit.MULTIPLE, 0));
		assertEquals(Long.parseLong("gz", 36), (long)c.getMeta("#multiple_base36", BmsUnit.MULTIPLE, 0));
		assertEquals(new BmsArray("0088aaff", 16), (BmsArray)c.getMeta("#multiple_array16", BmsUnit.MULTIPLE, 0));
		assertEquals(new BmsArray("0099ffkkppzz", 36), (BmsArray)c.getMeta("#multiple_array36", BmsUnit.MULTIPLE, 0));

		assertEquals(0L, (long)c.getMeta("#indexed_integer", BmsUnit.INDEXED, 0));
		assertEquals(200.123, (double)c.getMeta("#indexed_float", BmsUnit.INDEXED, 0), 0.0001);
		assertEquals("hoge", (String)c.getMeta("#indexed_string", BmsUnit.INDEXED, 0));
		assertEquals(0xafL, (long)c.getMeta("#indexed_base16", BmsUnit.INDEXED, 0));
		assertEquals(Long.parseLong("gz", 36), (long)c.getMeta("#indexed_base36", BmsUnit.INDEXED, 0));
		assertEquals(new BmsArray("0088aaff", 16), (BmsArray)c.getMeta("#indexed_array16", BmsUnit.INDEXED, 0));
		assertEquals(new BmsArray("0099ffkkppzz", 36), (BmsArray)c.getMeta("#indexed_array36", BmsUnit.INDEXED, 0));
	}

	// getMeta(String, BmsUnit, int)
	// NullPointerException nameがnull
	@Test
	public void testGetMeta_002() {
		var c = new BmsContent(spec());
		assertThrows(NullPointerException.class, () -> c.getMeta(null, BmsUnit.SINGLE, 0));
	}

	// getMeta(String, BmsUnit, int)
	// NullPointerException unitがnull
	@Test
	public void testGetMeta_003() {
		var c = new BmsContent(spec());
		assertThrows(NullPointerException.class, () -> c.getMeta("#meta", null, 0));
	}

	// getMeta(String, BmsUnit, int)
	// IllegalArgumentException name, unitに合致するメタ情報が存在しない
	@Test
	public void testGetMeta_004() {
		var c = new BmsContent(spec());
		var ex = IllegalArgumentException.class;
		assertThrows(ex, () -> c.getMeta("#indexed_integer", BmsUnit.SINGLE, 0));
		assertThrows(ex, () -> c.getMeta("#single_integer", BmsUnit.MULTIPLE, 0));
		assertThrows(ex, () -> c.getMeta("#multiple_integer", BmsUnit.INDEXED, 0));
	}

	// getMeta(String, BmsUnit, int)
	// IndexOutOfBoundsException unitが{@link BmsUnit#SINGLE SINGLE}の時、indexが0以外
	@Test
	public void testGetMeta_005() {
		var c = new BmsContent(spec());
		var ex = IndexOutOfBoundsException.class;
		assertThrows(ex, () -> c.getMeta("#single_integer", BmsUnit.SINGLE, 1));
	}

	// getMeta(String, BmsUnit, int)
	// IndexOutOfBoundsException unitが{@link BmsUnit#MULTIPLE MULTIPLE}の時、indexが0～{@link BmsSpec#MULTIPLE_META_INDEX_MAX}の範囲外
	@Test
	public void testGetMeta_006() {
		var c = new BmsContent(spec());
		var ex = IndexOutOfBoundsException.class;
		assertThrows(ex, () -> c.getMeta("#multiple_integer", BmsUnit.MULTIPLE, -1));
		assertThrows(ex, () -> c.getMeta("#multiple_integer", BmsUnit.MULTIPLE, BmsSpec.MULTIPLE_META_INDEX_MAX + 1));
	}

	// getMeta(String, BmsUnit, int)
	// IndexOutOfBoundsException unitが{@link BmsUnit#INDEXED INDEXED}の時、indexが0～{@link BmsSpec#INDEXED_META_INDEX_MAX}の範囲外
	@Test
	public void testGetMeta_007() {
		var c = new BmsContent(spec());
		c.beginEdit();
		var ex = IndexOutOfBoundsException.class;
		assertThrows(ex, () -> c.getMeta("#indexed_integer", BmsUnit.INDEXED, -1));
		assertThrows(ex, () -> c.getMeta("#indexed_integer", BmsUnit.INDEXED, BmsSpec.INDEXED_META_INDEX_MAX + 1));
	}

	// getMultipleMetas(String)
	// 正常
	@Test
	public void testGetMultipleMetas_001() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.setMultipleMeta("#multiple_integer", 0, 100);
		c.setMultipleMeta("#multiple_integer", 3, 500);
		c.setMultipleMeta("#multiple_integer", 4, 800);
		c.setMultipleMeta("#multiple_integer", 5, 1200);
		c.endEdit();

		var ms = c.getMultipleMetas("#multiple_integer");
		var dv = (long)c.getSpec().getMultipleMeta("#multiple_integer").getDefaultValue();
		assertEquals(6, ms.size());
		assertEquals(100L, (long)ms.get(0));
		assertEquals(dv, (long)ms.get(1));
		assertEquals(dv, (long)ms.get(2));
		assertEquals(500L, (long)ms.get(3));
		assertEquals(800L, (long)ms.get(4));
		assertEquals(1200L, (long)ms.get(5));
	}

	// getMultipleMetas(String)
	// NullPointerException nameがnull
	@Test
	public void testGetMultipleMetas_002() {
		var c = new BmsContent(spec());
		assertThrows(NullPointerException.class, () -> c.getMultipleMetas(null));
	}

	// getMultipleMetas(String)
	// IllegalArgumentException 名称に合致するメタ情報が存在しない
	@Test
	public void testGetMultipleMetas_003() {
		var c = new BmsContent(spec());
		assertThrows(IllegalArgumentException.class, () -> c.getMultipleMetas("#unknown"));
	}

	// getIndexedMetas(String)
	// 正常
	@Test
	public void testGetIndexedMetas_001() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.setIndexedMeta("#indexed_integer", 0, 100);
		c.setIndexedMeta("#indexed_integer", 3, 500);
		c.setIndexedMeta("#indexed_integer", 4, 800);
		c.setIndexedMeta("#indexed_integer", 5, 1200);
		c.endEdit();

		var ms = c.getIndexedMetas("#indexed_integer");
		assertEquals(4, ms.size());
		assertEquals(100L, (long)ms.get(0));
		assertNull(ms.get(1));
		assertNull(ms.get(2));
		assertEquals(500L, (long)ms.get(3));
		assertEquals(800L, (long)ms.get(4));
		assertEquals(1200L, (long)ms.get(5));
	}

	// getIndexedMetas(String)
	// NullPointerException nameがnull
	@Test
	public void testGetIndexedMetas_002() {
		var c = new BmsContent(spec());
		assertThrows(NullPointerException.class, () -> c.getIndexedMetas(null));
	}

	// getIndexedMetas(String)
	// IllegalArgumentException 名称に合致するメタ情報が存在しない
	@Test
	public void testGetIndexedMetas_003() {
		var c = new BmsContent(spec());
		assertThrows(IllegalArgumentException.class, () -> c.getIndexedMetas("#unknown"));
	}

	// getSingleMetaCount(String)
	// 正常
	@Test
	public void testGetSingleMetaCount_001() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.setSingleMeta("#single_integer", 100);
		c.endEdit();
		assertEquals(1, c.getSingleMetaCount("#single_integer"));

		// nullで消すと0
		c.beginEdit();
		c.setSingleMeta("#single_integer", null);
		c.endEdit();
		assertEquals(0, c.getSingleMetaCount("#single_integer"));

		// 値が設定されていない時は0
		c = new BmsContent(spec());
		assertEquals(0, c.getSingleMetaCount("#single_integer"));
	}

	// getSingleMetaCount(String)
	// NullPointerException nameがnull
	@Test
	public void testGetSingleMetaCount_002() {
		var c = new BmsContent(spec());
		assertThrows(NullPointerException.class, () -> c.getSingleMetaCount(null));
	}

	// getSingleMetaCount(String)
	// IllegalArgumentException 名称に合致するメタ情報が存在しない
	@Test
	public void testGetSingleMetaCount_003() {
		var c = new BmsContent(spec());
		assertThrows(IllegalArgumentException.class, () -> c.getSingleMetaCount("#unknown"));
	}

	// getMultipleMetaCount(String)
	// 正常
	@Test
	public void testGetMultipleMetaCount_001() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.setMultipleMeta("#multiple_integer", 0, 100);
		c.setMultipleMeta("#multiple_integer", 3, 500);
		c.setMultipleMeta("#multiple_integer", 4, 800);
		c.setMultipleMeta("#multiple_integer", 5, 1200);
		c.endEdit();
		assertEquals(6, c.getMultipleMetaCount("#multiple_integer"));
		// nullで消せる
		c.beginEdit();
		c.setMultipleMeta("#multiple_integer", 0, null);
		c.setMultipleMeta("#multiple_integer", 0, null);
		c.endEdit();
		assertEquals(4, c.getMultipleMetaCount("#multiple_integer"));

		// 値が設定されていない時は0
		c = new BmsContent(spec());
		assertEquals(0, c.getMultipleMetaCount("#multiple_integer"));
	}

	// getMultipleMetaCount(String)
	// NullPointerException nameがnull
	@Test
	public void testGetMultipleMetaCount_002() {
		var c = new BmsContent(spec());
		assertThrows(NullPointerException.class, () -> c.getMultipleMetaCount(null));
	}

	// getMultipleMetaCount(String)
	// IllegalArgumentException 名称に合致するメタ情報が存在しない
	@Test
	public void testGetMultipleMetaCount_003() {
		var c = new BmsContent(spec());
		assertThrows(IllegalArgumentException.class, () -> c.getMultipleMetaCount("#unknown"));
	}

	// getIndexedMetaCount(String)
	// 正常
	@Test
	public void testGetIndexedMetaCount_001() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.setIndexedMeta("#indexed_integer", 0, 100);
		c.setIndexedMeta("#indexed_integer", 3, 500);
		c.setIndexedMeta("#indexed_integer", 4, 800);
		c.setIndexedMeta("#indexed_integer", 5, 1200);
		c.endEdit();
		assertEquals(4, c.getIndexedMetaCount("#indexed_integer"));
		// nullで消せる
		c.beginEdit();
		c.setIndexedMeta("#indexed_integer", 3, null);
		c.setIndexedMeta("#indexed_integer", 5, null);
		c.endEdit();
		assertEquals(2, c.getIndexedMetaCount("#indexed_integer"));

		// 値が設定されていない時は0
		c = new BmsContent(spec());
		assertEquals(0, c.getIndexedMetaCount("#indexed_integer"));
	}

	// getIndexedMetaCount(String)
	// NullPointerException nameがnull
	@Test
	public void testGetIndexedMetaCount_002() {
		var c = new BmsContent(spec());
		assertThrows(NullPointerException.class, () -> c.getIndexedMetaCount(null));
	}

	// getIndexedMetaCount(String)
	// IllegalArgumentException 名称に合致するメタ情報が存在しない
	@Test
	public void testGetIndexedMetaCount_003() {
		var c = new BmsContent(spec());
		assertThrows(IllegalArgumentException.class, () -> c.getIndexedMetaCount("#unknown"));
	}

	// getMetaCount(BmsMetaKey)
	// 正常
	@Test
	public void testGetMetaCountBmsMetaKey_001() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.setSingleMeta("#single_integer", 100);
		c.setSingleMeta("#single_string", null);
		c.setMultipleMeta("#multiple_integer", 0, 100);
		c.setMultipleMeta("#multiple_integer", 5, 100);
		c.setIndexedMeta("#indexed_integer", 0, 100);
		c.setIndexedMeta("#indexed_integer", 3, 500);
		c.endEdit();
		assertEquals(1, c.getMetaCount(new BmsMetaKey("#single_integer", BmsUnit.SINGLE)));
		assertEquals(0, c.getMetaCount(new BmsMetaKey("#single_string", BmsUnit.SINGLE)));
		assertEquals(6, c.getMetaCount(new BmsMetaKey("#multiple_integer", BmsUnit.MULTIPLE)));
		assertEquals(2, c.getMetaCount(new BmsMetaKey("#indexed_integer", BmsUnit.INDEXED)));
	}

	// getMetaCount(BmsMetaKey)
	// NullPointerException keyがnull
	@Test
	public void testGetMetaCountBmsMetaKey_002() {
		var c = new BmsContent(spec());
		assertThrows(NullPointerException.class, () -> c.getMetaCount(null));
	}

	// getMetaCount(BmsMetaKey)
	// IllegalArgumentException 名称に合致するメタ情報が存在しない
	@Test
	public void testGetMetaCountBmsMetaKey_003() {
		var c = new BmsContent(spec());
		var ex = IllegalArgumentException.class;
		assertThrows(ex, () -> c.getMetaCount(new BmsMetaKey("#indexed_integer", BmsUnit.SINGLE)));
		assertThrows(ex, () -> c.getMetaCount(new BmsMetaKey("#single_integer", BmsUnit.MULTIPLE)));
		assertThrows(ex, () -> c.getMetaCount(new BmsMetaKey("#multiple_integer", BmsUnit.INDEXED)));
	}

	// getMetaCount(String, BmsUnit)
	// 正常
	@Test
	public void testGetMetaCount_001() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.setSingleMeta("#single_integer", 100);
		c.setSingleMeta("#single_string", null);
		c.setMultipleMeta("#multiple_integer", 0, 100);
		c.setMultipleMeta("#multiple_integer", 5, 100);
		c.setIndexedMeta("#indexed_integer", 0, 100);
		c.setIndexedMeta("#indexed_integer", 3, 500);
		c.endEdit();
		assertEquals(1, c.getMetaCount("#single_integer", BmsUnit.SINGLE));
		assertEquals(0, c.getMetaCount("#single_string", BmsUnit.SINGLE));
		assertEquals(6, c.getMetaCount("#multiple_integer", BmsUnit.MULTIPLE));
		assertEquals(2, c.getMetaCount("#indexed_integer", BmsUnit.INDEXED));
	}

	// getMetaCount(String, BmsUnit)
	// NullPointerException nameがnull
	@Test
	public void testGetMetaCount_002() {
		var c = new BmsContent(spec());
		assertThrows(NullPointerException.class, () -> c.getMetaCount(null, BmsUnit.SINGLE));
	}

	// getMetaCount(String, BmsUnit)
	// NullPointerException unitがnull
	@Test
	public void testGetMetaCount_003() {
		var c = new BmsContent(spec());
		assertThrows(NullPointerException.class, () -> c.getMetaCount("#meta", null));
	}

	// getMetaCount(String, BmsUnit)
	// IllegalArgumentException 名称に合致するメタ情報が存在しない
	@Test
	public void testGetMetaCount_004() {
		var c = new BmsContent(spec());
		var ex = IllegalArgumentException.class;
		assertThrows(ex, () -> c.getMetaCount("#indexed_integer", BmsUnit.SINGLE));
		assertThrows(ex, () -> c.getMetaCount("#single_integer", BmsUnit.MULTIPLE));
		assertThrows(ex, () -> c.getMetaCount("#multiple_integer", BmsUnit.INDEXED));
	}

	// containsSingleMeta(String)
	// 正常
	@Test
	public void testContainsSingleMeta_001() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.setSingleMeta("#single_integer", 100);
		c.endEdit();
		assertTrue(c.containsSingleMeta("#single_integer"));

		// nullで消せる
		c.beginEdit();
		c.setSingleMeta("#single_integer", null);
		c.endEdit();
		assertFalse(c.containsSingleMeta("#single_integer"));

		// 未設定時はfalse
		c = new BmsContent(spec());
		assertFalse(c.containsSingleMeta("#single_integer"));
	}

	// containsSingleMeta(String)
	// NullPointerException nameがnull
	@Test
	public void testContainsSingleMeta_002() {
		var c = new BmsContent(spec());
		assertThrows(NullPointerException.class, () -> c.containsSingleMeta(null));
	}

	// containsSingleMeta(String)
	// IllegalArgumentException 名称に合致するメタ情報が存在しない
	@Test
	public void testContainsSingleMeta_003() {
		var c = new BmsContent(spec());
		assertThrows(IllegalArgumentException.class, () -> c.containsSingleMeta("#unknown"));
	}

	// containsMultipleMeta(String, int)
	// 正常
	@Test
	public void testContainsMultipleMeta_001() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.setMultipleMeta("#multiple_integer", 0, 100);
		c.setMultipleMeta("#multiple_integer", 3, 500);
		c.endEdit();
		assertTrue(c.containsMultipleMeta("#multiple_integer", 0));
		assertTrue(c.containsMultipleMeta("#multiple_integer", 1));
		assertTrue(c.containsMultipleMeta("#multiple_integer", 2));
		assertTrue(c.containsMultipleMeta("#multiple_integer", 3));
		assertFalse(c.containsMultipleMeta("#multiple_integer", 4));

		// nullで途中を消すと末尾が詰められてfalseになる
		c.beginEdit();
		c.setMultipleMeta("#multiple_integer", 1, null);
		c.endEdit();
		assertFalse(c.containsMultipleMeta("#multiple_integer", 3));
	}

	// containsMultipleMeta(String, int)
	// NullPointerException nameがnull
	@Test
	public void testContainsMultipleMeta_002() {
		var c = new BmsContent(spec());
		assertThrows(NullPointerException.class, () -> c.containsMultipleMeta(null, 0));
	}

	// containsMultipleMeta(String, int)
	// IllegalArgumentException 名称に合致するメタ情報が存在しない
	@Test
	public void testContainsMultipleMeta_003() {
		var c = new BmsContent(spec());
		assertThrows(IllegalArgumentException.class, () -> c.containsMultipleMeta("#unknown", 0));
	}

	// containsMultipleMeta(String, int)
	// IndexOutOfBoundsException indexが0～{@link BmsSpec#MULTIPLE_META_INDEX_MAX}の範囲外
	@Test
	public void testContainsMultipleMeta_004() {
		var c = new BmsContent(spec());
		var ex = IndexOutOfBoundsException.class;
		assertThrows(ex, () -> c.containsMultipleMeta("#multiple_integer", -1));
		assertThrows(ex, () -> c.containsMultipleMeta("#multiple_integer", BmsSpec.MULTIPLE_META_INDEX_MAX + 1));
	}

	// containsIndexedMeta(String, int)
	// 正常
	@Test
	public void testContainsIndexedMeta_001() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.setIndexedMeta("#indexed_integer", 0, 100);
		c.setIndexedMeta("#indexed_integer", 3, 500);
		c.endEdit();
		assertTrue(c.containsIndexedMeta("#indexed_integer", 0));
		assertFalse(c.containsIndexedMeta("#indexed_integer", 1));
		assertFalse(c.containsIndexedMeta("#indexed_integer", 2));
		assertTrue(c.containsIndexedMeta("#indexed_integer", 3));
		assertFalse(c.containsIndexedMeta("#indexed_integer", 4));

		// nullで途中を消しても末尾は詰められない
		c.beginEdit();
		c.setIndexedMeta("#indexed_integer", 0, null);
		c.endEdit();
		assertFalse(c.containsIndexedMeta("#indexed_integer", 0));
		assertTrue(c.containsIndexedMeta("#indexed_integer", 3));
	}

	// containsIndexedMeta(String, int)
	// NullPointerException nameがnull
	@Test
	public void testContainsIndexedMeta_002() {
		var c = new BmsContent(spec());
		assertThrows(NullPointerException.class, () -> c.containsIndexedMeta(null, 0));
	}

	// containsIndexedMeta(String, int)
	// IllegalArgumentException 名称に合致するメタ情報が存在しない
	@Test
	public void testContainsIndexedMeta_003() {
		var c = new BmsContent(spec());
		assertThrows(IllegalArgumentException.class, () -> c.containsIndexedMeta("#unknown", 0));
	}

	// containsIndexedMeta(String, int)
	// IndexOutOfBoundsException indexが0～{@link BmsSpec#INDEXED_META_INDEX_MAX}の範囲外
	@Test
	public void testContainsIndexedMeta_004() {
		var c = new BmsContent(spec());
		var ex = IndexOutOfBoundsException.class;
		assertThrows(ex, () -> c.containsIndexedMeta("#indexed_integer", -1));
		assertThrows(ex, () -> c.containsIndexedMeta("#indexed_integer", BmsSpec.INDEXED_META_INDEX_MAX + 1));
	}

	// containsMeta(BmsMetaKey)
	// 正常
	@Test
	public void testContainsMetaBmsMetaKey_001() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.setMeta("#single_integer", BmsUnit.SINGLE, 0, 0);
		c.setMeta("#multiple_integer", BmsUnit.MULTIPLE, 0, 0);
		c.setMeta("#indexed_integer", BmsUnit.INDEXED, 0, 0);
		c.endEdit();
		assertTrue(c.containsMeta(new BmsMetaKey("#single_integer", BmsUnit.SINGLE)));
		assertFalse(c.containsMeta(new BmsMetaKey("#single_float", BmsUnit.SINGLE)));
		assertTrue(c.containsMeta(new BmsMetaKey("#multiple_integer", BmsUnit.MULTIPLE)));
		assertFalse(c.containsMeta(new BmsMetaKey("#multiple_float", BmsUnit.MULTIPLE)));
		assertTrue(c.containsMeta(new BmsMetaKey("#indexed_integer", BmsUnit.INDEXED)));
		assertFalse(c.containsMeta(new BmsMetaKey("#indexed_float", BmsUnit.INDEXED)));
	}

	// containsMeta(BmsMetaKey)
	// NullPointerException keyがnull
	@Test
	public void testContainsMetaBmsMetaKey_002() {
		var c = new BmsContent(spec());
		assertThrows(NullPointerException.class, () -> c.containsMeta(null));
	}

	// containsMeta(BmsMetaKey)
	// IllegalArgumentException 名称・単位に合致するメタ情報が存在しない
	@Test
	public void testContainsMetaBmsMetaKey_003() {
		var c = new BmsContent(spec());
		var ex = IllegalArgumentException.class;
		assertThrows(ex, () -> c.containsMeta(new BmsMetaKey("#indexed_integer", BmsUnit.SINGLE)));
		assertThrows(ex, () -> c.containsMeta(new BmsMetaKey("#single_integer", BmsUnit.MULTIPLE)));
		assertThrows(ex, () -> c.containsMeta(new BmsMetaKey("#multiple_integer", BmsUnit.INDEXED)));
	}

	// containsMeta(BmsMetaKey, int)
	// 正常
	@Test
	public void testContainsMetaBmsMetaKeyInt_001() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.setMeta("#single_integer", BmsUnit.SINGLE, 0, 0);
		c.setMeta("#multiple_integer", BmsUnit.MULTIPLE, 0, 0);
		c.setMeta("#indexed_integer", BmsUnit.INDEXED, 0, 0);
		c.endEdit();
		assertTrue(c.containsMeta(new BmsMetaKey("#single_integer", BmsUnit.SINGLE), 0));
		assertFalse(c.containsMeta(new BmsMetaKey("#single_float", BmsUnit.SINGLE), 0));
		assertTrue(c.containsMeta(new BmsMetaKey("#multiple_integer", BmsUnit.MULTIPLE), 0));
		assertFalse(c.containsMeta(new BmsMetaKey("#multiple_integer", BmsUnit.MULTIPLE), 1));
		assertFalse(c.containsMeta(new BmsMetaKey("#multiple_float", BmsUnit.MULTIPLE), 0));
		assertTrue(c.containsMeta(new BmsMetaKey("#indexed_integer", BmsUnit.INDEXED), 0));
		assertFalse(c.containsMeta(new BmsMetaKey("#indexed_integer", BmsUnit.INDEXED), 1));
		assertFalse(c.containsMeta(new BmsMetaKey("#indexed_float", BmsUnit.INDEXED), 0));
	}

	// containsMeta(BmsMetaKey, int)
	// NullPointerException keyがnull
	@Test
	public void testContainsMetaBmsMetaKeyInt_002() {
		var c = new BmsContent(spec());
		assertThrows(NullPointerException.class, () -> c.containsMeta(null, 0));
	}

	// containsMeta(BmsMetaKey, int)
	// IllegalArgumentException 名称・単位に合致するメタ情報が存在しない
	@Test
	public void testContainsMetaBmsMetaKeyInt_003() {
		var c = new BmsContent(spec());
		var ex = IllegalArgumentException.class;
		assertThrows(ex, () -> c.containsMeta(new BmsMetaKey("#indexed_integer", BmsUnit.SINGLE), 0));
		assertThrows(ex, () -> c.containsMeta(new BmsMetaKey("#single_integer", BmsUnit.MULTIPLE), 0));
		assertThrows(ex, () -> c.containsMeta(new BmsMetaKey("#multiple_integer", BmsUnit.INDEXED), 0));
	}

	// containsMeta(BmsMetaKey, int)
	// IndexOutOfBoundsException 構成単位が単体の時、indexが0以外
	@Test
	public void testContainsMetaBmsMetaKeyInt_004() {
		var c = new BmsContent(spec());
		var ex = IndexOutOfBoundsException.class;
		assertThrows(ex, () -> c.containsMeta(new BmsMetaKey("#single_integer", BmsUnit.SINGLE), 1));
	}

	// containsMeta(BmsMetaKey, int)
	// IndexOutOfBoundsException 構成単位が複数の時、indexが0～{@link BmsSpec#MULTIPLE_META_INDEX_MAX}の範囲外
	@Test
	public void testContainsMetaBmsMetaKeyInt_005() {
		var c = new BmsContent(spec());
		var ex = IndexOutOfBoundsException.class;
		assertThrows(ex, () -> c.containsMeta(new BmsMetaKey("#multiple_integer", BmsUnit.MULTIPLE), -1));
		assertThrows(ex, () -> c.containsMeta(new BmsMetaKey("#multiple_integer", BmsUnit.MULTIPLE), BmsSpec.MULTIPLE_META_INDEX_MAX + 1));
	}

	// containsMeta(BmsMetaKey, int)
	// IndexOutOfBoundsException 構成単位が索引付きの時、indexが0～{@link BmsSpec#INDEXED_META_INDEX_MAX}の範囲外
	@Test
	public void testContainsMetaBmsMetaKeyInt_006() {
		var c = new BmsContent(spec());
		c.beginEdit();
		var ex = IndexOutOfBoundsException.class;
		assertThrows(ex, () -> c.containsMeta(new BmsMetaKey("#indexed_integer", BmsUnit.INDEXED), -1));
		assertThrows(ex, () -> c.containsMeta(new BmsMetaKey("#indexed_integer", BmsUnit.INDEXED), BmsSpec.INDEXED_META_INDEX_MAX + 1));
	}

	// containsMeta(String, BmsUnit, int)
	// 正常
	@Test
	public void testContainsMeta_001() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.setMeta("#single_integer", BmsUnit.SINGLE, 0, 0);
		c.setMeta("#multiple_integer", BmsUnit.MULTIPLE, 0, 0);
		c.setMeta("#indexed_integer", BmsUnit.INDEXED, 0, 0);
		c.endEdit();
		assertTrue(c.containsMeta("#single_integer", BmsUnit.SINGLE, 0));
		assertFalse(c.containsMeta("#single_float", BmsUnit.SINGLE, 0));
		assertTrue(c.containsMeta("#multiple_integer", BmsUnit.MULTIPLE, 0));
		assertFalse(c.containsMeta("#multiple_integer", BmsUnit.MULTIPLE, 1));
		assertFalse(c.containsMeta("#multiple_float", BmsUnit.MULTIPLE, 0));
		assertTrue(c.containsMeta("#indexed_integer", BmsUnit.INDEXED, 0));
		assertFalse(c.containsMeta("#indexed_integer", BmsUnit.INDEXED, 1));
		assertFalse(c.containsMeta("#indexed_float", BmsUnit.INDEXED, 0));
	}

	// containsMeta(String, BmsUnit, int)
	// NullPointerException nameがnull
	@Test
	public void testContainsMeta_002() {
		var c = new BmsContent(spec());
		assertThrows(NullPointerException.class, () -> c.containsMeta(null, BmsUnit.SINGLE, 0));
	}

	// containsMeta(String, BmsUnit, int)
	// NullPointerException unitがnull
	@Test
	public void testContainsMeta_003() {
		var c = new BmsContent(spec());
		assertThrows(NullPointerException.class, () -> c.containsMeta("#meta", null, 0));
	}

	// containsMeta(String, BmsUnit, int)
	// IllegalArgumentException 名称・単位に合致するメタ情報が存在しない
	@Test
	public void testContainsMeta_004() {
		var c = new BmsContent(spec());
		var ex = IllegalArgumentException.class;
		assertThrows(ex, () -> c.containsMeta("#indexed_integer", BmsUnit.SINGLE, 0));
		assertThrows(ex, () -> c.containsMeta("#single_integer", BmsUnit.MULTIPLE, 0));
		assertThrows(ex, () -> c.containsMeta("#multiple_integer", BmsUnit.INDEXED, 0));
	}

	// containsMeta(String, BmsUnit, int)
	// IndexOutOfBoundsException unitが{@link BmsUnit#SINGLE SINGLE}の時、indexが0以外
	@Test
	public void testContainsMeta_005() {
		var c = new BmsContent(spec());
		var ex = IndexOutOfBoundsException.class;
		assertThrows(ex, () -> c.containsMeta("#single_integer", BmsUnit.SINGLE, 1));
	}

	// containsMeta(String, BmsUnit, int)
	// IndexOutOfBoundsException unitが{@link BmsUnit#MULTIPLE MULTIPLE}の時、indexが0～{@link BmsSpec#MULTIPLE_META_INDEX_MAX}の範囲外
	@Test
	public void testContainsMeta_006() {
		var c = new BmsContent(spec());
		var ex = IndexOutOfBoundsException.class;
		assertThrows(ex, () -> c.containsMeta("#multiple_integer", BmsUnit.MULTIPLE, -1));
		assertThrows(ex, () -> c.containsMeta("#multiple_integer", BmsUnit.MULTIPLE, BmsSpec.MULTIPLE_META_INDEX_MAX + 1));
	}

	// containsMeta(String, BmsUnit, int)
	// IndexOutOfBoundsException unitが{@link BmsUnit#INDEXED INDEXED}の時、indexが0～{@link BmsSpec#INDEXED_META_INDEX_MAX}の範囲外
	@Test
	public void testContainsMeta_007() {
		var c = new BmsContent(spec());
		var ex = IndexOutOfBoundsException.class;
		c.beginEdit();
		assertThrows(ex, () -> c.containsMeta("#indexed_integer", BmsUnit.INDEXED, -1));
		assertThrows(ex, () -> c.containsMeta("#indexed_integer", BmsUnit.INDEXED, BmsSpec.INDEXED_META_INDEX_MAX + 1));
	}

	// containsMeta(String, BmsUnit)
	// 値を設定した単体メタ情報でtrueを返すこと
	@Test
	public void testContainsMeta2_001() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.setSingleMeta("#single_integer", 100L);
		c.endEdit();
		assertTrue(c.containsMeta("#single_integer", BmsUnit.SINGLE));
	}

	// containsMeta(String, BmsUnit)
	// 値が未設定の単体メタ情報でfalseを返すこと
	@Test
	public void testContainsMeta2_002() {
		var c = new BmsContent(spec());
		assertFalse(c.containsMeta("#single_integer", BmsUnit.SINGLE));
	}

	// containsMeta(String, BmsUnit)
	// 値が1件の複数メタ情報でtrueを返すこと
	@Test
	public void testContainsMeta2_003() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.setMultipleMeta("#multiple_integer", 0, 100L);
		c.endEdit();
		assertTrue(c.containsMeta("#multiple_integer", BmsUnit.MULTIPLE));
	}

	// containsMeta(String, BmsUnit)
	// 値を1件設定後、削除した複数メタ情報でfalseを返すこと
	@Test
	public void testContainsMeta2_004() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.setMultipleMeta("#multiple_integer", 0, 100L);
		c.setMultipleMeta("#multiple_integer", 0, null);
		c.endEdit();
		assertFalse(c.containsMeta("#multiple_integer", BmsUnit.MULTIPLE));
	}

	// containsMeta(String, BmsUnit)
	// 値が未設定の複数メタ情報でfalseを返すこと
	@Test
	public void testContainsMeta2_005() {
		var c = new BmsContent(spec());
		assertFalse(c.containsMeta("#multiple_integer", BmsUnit.MULTIPLE));
	}

	// containsMeta(String, BmsUnit)
	// 値が1件の索引付きメタ情報でtrueを返すこと(index==0)
	@Test
	public void testContainsMeta2_006() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.setIndexedMeta("#indexed_integer", 0, 100L);
		c.endEdit();
		assertTrue(c.containsMeta("#indexed_integer", BmsUnit.INDEXED));
	}

	// containsMeta(String, BmsUnit)
	// 値が1件の索引付きメタ情報でtrueを返すこと(index==256)
	@Test
	public void testContainsMeta2_007() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.setIndexedMeta("#indexed_integer", 256, 100L);
		c.endEdit();
		assertTrue(c.containsMeta("#indexed_integer", BmsUnit.INDEXED));
	}

	// containsMeta(String, BmsUnit)
	// 値を1件設定後、削除した索引付きメタ情報でfalseを返すこと
	@Test
	public void testContainsMeta2_008() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.setIndexedMeta("#indexed_integer", 256, 100L);
		c.setIndexedMeta("#indexed_integer", 256, null);
		c.endEdit();
		assertFalse(c.containsMeta("#indexed_integer", BmsUnit.INDEXED));
	}

	// containsMeta(String, BmsUnit)
	// 値が未設定の索引付きメタ情報でfalseを返すこと
	@Test
	public void testContainsMeta2_009() {
		var c = new BmsContent(spec());
		assertFalse(c.containsMeta("#indexed_integer", BmsUnit.INDEXED));
	}

	// containsMeta(String, BmsUnit)
	// NullPointerException nameがnull
	@Test
	public void testContainsMeta2_010() {
		var c = new BmsContent(spec());
		assertThrows(NullPointerException.class, () -> c.containsMeta(null, BmsUnit.SINGLE));
	}

	// containsMeta(String, BmsUnit)
	// NullPointerException unitがnull
	@Test
	public void testContainsMeta2_011() {
		var c = new BmsContent(spec());
		assertThrows(NullPointerException.class, () -> c.containsMeta("#single_integer", null));
	}

	// containsMeta(String, BmsUnit)
	// IllegalArgumentException 名称・単位に合致するメタ情報が存在しない
	@Test
	public void testContainsMeta2_012() {
		var c = new BmsContent(spec());
		var ex = IllegalArgumentException.class;
		assertThrows(ex, () -> c.containsMeta("#not_exist", BmsUnit.SINGLE));
		assertThrows(ex, () -> c.containsMeta("#not_exist", BmsUnit.MULTIPLE));
		assertThrows(ex, () -> c.containsMeta("#not_exist", BmsUnit.INDEXED));
	}

	// metas()
	// BMS仕様の全てのメタ情報、全てのインデックスのデータが走査されること
	@Test
	public void testMetas_AllData() {
		var s = specForMetaStream();
		var v = new ArrayList<>(List.of(
				new BmsMetaElement(s.getSingleMeta("#sint"), 0, 100L),
				new BmsMetaElement(s.getSingleMeta("#sflt"), 0, 200.345),
				new BmsMetaElement(s.getSingleMeta("#sstr"), 0, "STR"),
				new BmsMetaElement(s.getSingleMeta("#sb16"), 0, (long)BmsInt.to16i("AB")),
				new BmsMetaElement(s.getSingleMeta("#sb36"), 0, (long)BmsInt.to36i("XY")),
				new BmsMetaElement(s.getSingleMeta("#sa16"), 0, new BmsArray("AABB", 16)),
				new BmsMetaElement(s.getSingleMeta("#sa36"), 0, new BmsArray("XXYY", 36)),
				new BmsMetaElement(s.getSingleMeta("#sobj"), 0, "OBJ"),
				new BmsMetaElement(s.getMultipleMeta("#mint"), 0, 100L),
				new BmsMetaElement(s.getMultipleMeta("#mint"), 1, 200L),
				new BmsMetaElement(s.getMultipleMeta("#mflt"), 0, 200.345),
				new BmsMetaElement(s.getMultipleMeta("#mflt"), 1, 300.456),
				new BmsMetaElement(s.getMultipleMeta("#mstr"), 0, "STR1"),
				new BmsMetaElement(s.getMultipleMeta("#mstr"), 1, "STR2"),
				new BmsMetaElement(s.getMultipleMeta("#mb16"), 0, (long)BmsInt.to16i("AB")),
				new BmsMetaElement(s.getMultipleMeta("#mb16"), 1, (long)BmsInt.to16i("BC")),
				new BmsMetaElement(s.getMultipleMeta("#mb36"), 0, (long)BmsInt.to36i("XY")),
				new BmsMetaElement(s.getMultipleMeta("#mb36"), 1, (long)BmsInt.to36i("YZ")),
				new BmsMetaElement(s.getMultipleMeta("#ma16"), 0, new BmsArray("AABB", 16)),
				new BmsMetaElement(s.getMultipleMeta("#ma16"), 1, new BmsArray("BBCC", 16)),
				new BmsMetaElement(s.getMultipleMeta("#ma36"), 0, new BmsArray("XXYY", 36)),
				new BmsMetaElement(s.getMultipleMeta("#ma36"), 1, new BmsArray("YYZZ", 36)),
				new BmsMetaElement(s.getMultipleMeta("#mobj"), 0, "OBJ1"),
				new BmsMetaElement(s.getMultipleMeta("#mobj"), 1, "OBJ2"),
				new BmsMetaElement(s.getIndexedMeta("#iint"), 2, 100L),
				new BmsMetaElement(s.getIndexedMeta("#iint"), 4, 200L),
				new BmsMetaElement(s.getIndexedMeta("#iflt"), 2, 200.345),
				new BmsMetaElement(s.getIndexedMeta("#iflt"), 4, 300.456),
				new BmsMetaElement(s.getIndexedMeta("#istr"), 2, "STR1"),
				new BmsMetaElement(s.getIndexedMeta("#istr"), 4, "STR2"),
				new BmsMetaElement(s.getIndexedMeta("#ib16"), 2, (long)BmsInt.to16i("AB")),
				new BmsMetaElement(s.getIndexedMeta("#ib16"), 4, (long)BmsInt.to16i("BC")),
				new BmsMetaElement(s.getIndexedMeta("#ib36"), 2, (long)BmsInt.to36i("XY")),
				new BmsMetaElement(s.getIndexedMeta("#ib36"), 4, (long)BmsInt.to36i("YZ")),
				new BmsMetaElement(s.getIndexedMeta("#ia16"), 2, new BmsArray("AABB", 16)),
				new BmsMetaElement(s.getIndexedMeta("#ia16"), 4, new BmsArray("BBCC", 16)),
				new BmsMetaElement(s.getIndexedMeta("#ia36"), 2, new BmsArray("XXYY", 36)),
				new BmsMetaElement(s.getIndexedMeta("#ia36"), 4, new BmsArray("YYZZ", 36)),
				new BmsMetaElement(s.getIndexedMeta("#iobj"), 2, "OBJ1"),
				new BmsMetaElement(s.getIndexedMeta("#iobj"), 4, "OBJ2")));
		var c = new BmsContent(s);
		c.beginEdit();
		v.forEach(c::putMeta);
		c.endEdit();
		c.metas().forEach(e1 -> {
			//System.out.printf("m=%s, i=%d, v=%s\n", e1.getMeta(), e1.getIndex(), e1.getRawValue());
			assertTrue(v.removeIf(e2 -> equalsMetaElement(e1, e2)));
		});
		assertEquals(0, v.size());
	}

	// metas()
	// 値を未設定の単体メタ情報も全て走査されること
	@Test
	public void testMetas_NoValueSingles() {
		var s = specForMetaStream();
		var c = new BmsContent(s);
		var singles = s.metas().filter(BmsMeta::isSingleUnit).collect(Collectors.toSet());
		c.metas().filter(BmsMetaElement::isSingleUnit).forEach(e -> {
			//System.out.printf("m=%s, i=%d, v=%s\n", e1.getMeta(), e1.getIndex(), e1.getRawValue());
			assertEquals(0, e.getIndex());
			assertFalse(e.isContain());
			assertEquals(e.getMeta().getDefaultValue(), e.getValue());
			assertNull(e.getRawValue());
			assertTrue(singles.remove(e.getMeta()));
		});
		assertEquals(0, singles.size());
	}

	// metas()
	// 0件の複数メタ情報はインデックスがマイナス値の要素が走査されること
	@Test
	public void testMetas_NoValueMultiples() {
		var s = specForMetaStream();
		var c = new BmsContent(s);
		var multiples = new ArrayList<>(List.of(
				new BmsMetaElement(s.getMultipleMeta("#mint"), 0, null),
				new BmsMetaElement(s.getMultipleMeta("#mint"), 1, null),
				new BmsMetaElement(s.getMultipleMeta("#mint"), 2, 100L), // #mintは[2]のみデータあり
				new BmsMetaElement(s.getMultipleMeta("#mflt"), -1, null),
				new BmsMetaElement(s.getMultipleMeta("#mstr"), 0, null),
				new BmsMetaElement(s.getMultipleMeta("#mstr"), 1, "STR"), // #mstrは[1]のみデータあり
				new BmsMetaElement(s.getMultipleMeta("#mb16"), -1, null),
				new BmsMetaElement(s.getMultipleMeta("#mb36"), -1, null),
				new BmsMetaElement(s.getMultipleMeta("#ma16"), -1, null),
				new BmsMetaElement(s.getMultipleMeta("#ma36"), -1, null),
				new BmsMetaElement(s.getMultipleMeta("#mobj"), -1, null)));
		c.beginEdit();
		multiples.stream().filter(BmsMetaElement::isContain).forEach(c::putMeta);;
		c.endEdit();
		c.metas().filter(BmsMetaElement::isMultipleUnit).forEach(e1 -> {
			//System.out.printf("m=%s, i=%d, v=%s\n", e1.getMeta(), e1.getIndex(), e1.getRawValue());
			assertTrue(multiples.removeIf(e2 -> equalsMetaElement(e1, e2)));
		});
		assertEquals(0, multiples.size());
	}

	// metas()
	// 0件の索引付きメタ情報はインデックスがマイナス値の要素が走査されること
	@Test
	public void testMetas_NoValueIndices() {
		var s = specForMetaStream();
		var c = new BmsContent(s);
		var indices = new ArrayList<>(List.of(
				new BmsMetaElement(s.getIndexedMeta("#iint"), 1, 100L),
				new BmsMetaElement(s.getIndexedMeta("#iint"), 3, 300L),
				new BmsMetaElement(s.getIndexedMeta("#iflt"), -1, null),
				new BmsMetaElement(s.getIndexedMeta("#istr"), 2, "STR1"),
				new BmsMetaElement(s.getIndexedMeta("#istr"), 5, "STR2"),
				new BmsMetaElement(s.getIndexedMeta("#ib16"), -1, null),
				new BmsMetaElement(s.getIndexedMeta("#ib36"), -1, null),
				new BmsMetaElement(s.getIndexedMeta("#ia16"), -1, null),
				new BmsMetaElement(s.getIndexedMeta("#ia36"), -1, null),
				new BmsMetaElement(s.getIndexedMeta("#iobj"), -1, null)));
		c.beginEdit();
		indices.stream().filter(BmsMetaElement::isContain).forEach(c::putMeta);
		c.endEdit();
		c.metas().filter(BmsMetaElement::isIndexedUnit).forEach(e1 -> {
			//System.out.printf("m=%s, i=%d, v=%s\n", e1.getMeta(), e1.getIndex(), e1.getRawValue());
			assertTrue(indices.removeIf(e2 -> equalsMetaElement(e1, e2)));
		});
		assertEquals(0, indices.size());
	}

	// metas()
	// 単体→複数→索引付きの順で走査されること
	@Test
	public void testMetas_SortOrder() {
		var s = specForMetaStream();
		var c = new BmsContent(s);
		var l = new BmsUnit[] { BmsUnit.SINGLE };
		c.beginEdit();
		c.putMeta(new BmsMetaElement(s.getSingleMeta("#sint"), 0, 100L));
		c.putMeta(new BmsMetaElement(s.getSingleMeta("#sstr"), 0, "STR"));
		c.putMeta(new BmsMetaElement(s.getSingleMeta("#sa36"), 0, new BmsArray("XXYY", 36)));
		c.putMeta(new BmsMetaElement(s.getMultipleMeta("#mflt"), 0, 100.234));
		c.putMeta(new BmsMetaElement(s.getMultipleMeta("#mflt"), 1, 200.345));
		c.putMeta(new BmsMetaElement(s.getMultipleMeta("#mb16"), 0, (long)BmsInt.to16i("AB")));
		c.putMeta(new BmsMetaElement(s.getIndexedMeta("#istr"), 3, "STR1"));
		c.putMeta(new BmsMetaElement(s.getIndexedMeta("#istr"), 5, "STR2"));
		c.putMeta(new BmsMetaElement(s.getIndexedMeta("#iobj"), 9, "OBJ"));
		c.endEdit();
		c.metas().forEach(e -> {
			//System.out.printf("m=%s, i=%d, v=%s\n", e1.getMeta(), e1.getIndex(), e1.getRawValue());
			var ul = l[0];
			assertTrue((ul == BmsUnit.SINGLE && (e.isSingleUnit() || e.isMultipleUnit())) || (ul != BmsUnit.SINGLE));
			assertTrue((ul == BmsUnit.MULTIPLE && (e.isMultipleUnit() || e.isIndexedUnit())) || (ul != BmsUnit.MULTIPLE));
			assertTrue((ul == BmsUnit.INDEXED && e.isIndexedUnit()) || (ul != BmsUnit.INDEXED));
			l[0] = e.getUnit();
		});
	}

	// metas()
	// IllegalStateException 動作モードが編集モード
	@Test
	public void testMetas_IsEditMode() {
		var s = specForMetaStream();
		var c = new BmsContent(s);
		c.beginEdit();
		assertThrows(IllegalStateException.class, () -> c.metas());
	}

	// singleMetas()
	// 値の有無に関わらず全ての単体メタ情報のみが走査されること
	@Test
	public void testSingleMetas_AllData() {
		var s = specForMetaStream();
		var c = new BmsContent(s);
		var singles = new ArrayList<>(List.of(
				new BmsMetaElement(s.getSingleMeta("#sint"), 0, 100L),
				new BmsMetaElement(s.getSingleMeta("#sflt"), 0, null),
				new BmsMetaElement(s.getSingleMeta("#sstr"), 0, "STR"),
				new BmsMetaElement(s.getSingleMeta("#sb16"), 0, (long)BmsInt.to16i("AB")),
				new BmsMetaElement(s.getSingleMeta("#sb36"), 0, null),
				new BmsMetaElement(s.getSingleMeta("#sa16"), 0, null),
				new BmsMetaElement(s.getSingleMeta("#sa36"), 0, null),
				new BmsMetaElement(s.getSingleMeta("#sobj"), 0, null)));
		c.beginEdit();
		singles.stream().filter(BmsMetaElement::isContain).forEach(c::putMeta);
		c.endEdit();
		c.singleMetas().forEach(e1 -> {
			//System.out.printf("m=%s, i=%d, v=%s\n", e1.getMeta(), e1.getIndex(), e1.getRawValue());
			assertTrue(singles.removeIf(e2 -> equalsMetaElement(e1, e2)));
		});
		assertEquals(0, singles.size());
	}

	// singleMetas()
	// IllegalStateException 動作モードが編集モード
	@Test
	public void testSingleMetas_IsEditMode() {
		var s = specForMetaStream();
		var c = new BmsContent(s);
		c.beginEdit();
		assertThrows(IllegalStateException.class, () -> c.singleMetas());
	}

	// multipleMetas()
	// 値の有無に関わらず全ての複数メタ情報のみが走査されること
	@Test
	public void testMultipleMetas_AllData() {
		var s = specForMetaStream();
		var c = new BmsContent(s);
		var multiples = new ArrayList<>(List.of(
				new BmsMetaElement(s.getMultipleMeta("#mint"), 0, 100L),
				new BmsMetaElement(s.getMultipleMeta("#mflt"), -1, null),
				new BmsMetaElement(s.getMultipleMeta("#mstr"), 0, "STR1"),
				new BmsMetaElement(s.getMultipleMeta("#mstr"), 1, null),
				new BmsMetaElement(s.getMultipleMeta("#mstr"), 2, "STR2"),
				new BmsMetaElement(s.getMultipleMeta("#mb16"), 0, (long)BmsInt.to16i("AB")),
				new BmsMetaElement(s.getMultipleMeta("#mb16"), 1, (long)BmsInt.to16i("BC")),
				new BmsMetaElement(s.getMultipleMeta("#mb36"), -1, null),
				new BmsMetaElement(s.getMultipleMeta("#ma16"), -1, null),
				new BmsMetaElement(s.getMultipleMeta("#ma36"), -1, null),
				new BmsMetaElement(s.getMultipleMeta("#mobj"), -1, null)));
		c.beginEdit();
		multiples.stream().filter(BmsMetaElement::isContain).forEach(c::putMeta);
		c.endEdit();
		c.multipleMetas().forEach(e1 -> {
			//System.out.printf("m=%s, i=%d, v=%s\n", e1.getMeta(), e1.getIndex(), e1.getRawValue());
			assertTrue(multiples.removeIf(e2 -> equalsMetaElement(e1, e2)));
		});
		assertEquals(0, multiples.size());
	}

	// multipleMetas()
	// IllegalStateException 動作モードが編集モード
	@Test
	public void testMultipleMetas_IsEditMode() {
		var s = specForMetaStream();
		var c = new BmsContent(s);
		c.beginEdit();
		assertThrows(IllegalStateException.class, () -> c.multipleMetas());
	}

	// multipleMetas(String)
	// 指定された名前のデータあり複数メタ情報のみが走査されること
	@Test
	public void testMultipleMetasString_HasData() {
		var s = specForMetaStream();
		var c = new BmsContent(s);
		var multiples = new ArrayList<>(List.of(
				new BmsMetaElement(s.getMultipleMeta("#mint"), 0, 100L),
				new BmsMetaElement(s.getMultipleMeta("#mint"), 1, 200L),
				new BmsMetaElement(s.getMultipleMeta("#mint"), 2, null),
				new BmsMetaElement(s.getMultipleMeta("#mint"), 3, null),
				new BmsMetaElement(s.getMultipleMeta("#mint"), 4, 300L)));
		c.beginEdit();
		multiples.stream().filter(BmsMetaElement::isContain).forEach(c::putMeta);
		c.endEdit();
		c.multipleMetas("#mint").forEach(e1 -> {
			//System.out.printf("m=%s, i=%d, v=%s\n", e1.getMeta(), e1.getIndex(), e1.getRawValue());
			assertTrue(multiples.removeIf(e2 -> equalsMetaElement(e1, e2)));
		});
		assertEquals(0, multiples.size());
	}

	// multipleMetas(String)
	// 指定された名前のデータなし複数メタ情報のみが走査されること(インデックスが-1)
	@Test
	public void testMultipleMetasString_NoData() {
		var s = specForMetaStream();
		var c = new BmsContent(s);
		var l = c.multipleMetas("#mobj").collect(Collectors.toList());
		assertEquals(1, l.size());
		var e = l.get(0);
		assertEquals("#mobj", e.getName());
		assertEquals(-1, e.getIndex());
		assertFalse(e.isContain());
		assertNull(e.getRawValue());
	}

	// multipleMetas(String)
	// IllegalStateException 動作モードが編集モード
	@Test
	public void testMultipleMetasString_IsEditMode() {
		var s = specForMetaStream();
		var c = new BmsContent(s);
		c.beginEdit();
		assertThrows(IllegalStateException.class, () -> c.multipleMetas("#mint"));
	}

	// multipleMetas(String)
	// NullPointerException nameがnull
	@Test
	public void testMultipleMetasString_NullName() {
		var s = specForMetaStream();
		var c = new BmsContent(s);
		assertThrows(NullPointerException.class, () -> c.multipleMetas(null));
	}

	// multipleMetas(String)
	// IllegalArgumentException nameに該当する複数メタ情報がBMS仕様に存在しない
	@Test
	public void testMultipleMetasString_NoMeta() {
		var s = specForMetaStream();
		var c = new BmsContent(s);
		assertThrows(IllegalArgumentException.class, () -> c.multipleMetas("#notfound"));
	}

	// indexedMetas()
	// 値の有無に関わらず全ての複数メタ情報のみが走査されること
	@Test
	public void testIndexedMetas_AllData() {
		var s = specForMetaStream();
		var c = new BmsContent(s);
		var indices = new ArrayList<>(List.of(
				new BmsMetaElement(s.getIndexedMeta("#iint"), -1, null),
				new BmsMetaElement(s.getIndexedMeta("#iflt"), -1, null),
				new BmsMetaElement(s.getIndexedMeta("#istr"), -1, null),
				new BmsMetaElement(s.getIndexedMeta("#ib16"), -1, null),
				new BmsMetaElement(s.getIndexedMeta("#ib36"), -1, null),
				new BmsMetaElement(s.getIndexedMeta("#ia16"), 2, new BmsArray("AABB", 16)),
				new BmsMetaElement(s.getIndexedMeta("#ia16"), 5, new BmsArray("BBCC", 16)),
				new BmsMetaElement(s.getIndexedMeta("#ia16"), 6, new BmsArray("CCDD", 16)),
				new BmsMetaElement(s.getIndexedMeta("#ia36"), -1, null),
				new BmsMetaElement(s.getIndexedMeta("#iobj"), 1, "OBJ")));
		c.beginEdit();
		indices.stream().filter(BmsMetaElement::isContain).forEach(c::putMeta);
		c.endEdit();
		c.indexedMetas().forEach(e1 -> {
			//System.out.printf("m=%s, i=%d, v=%s\n", e1.getMeta(), e1.getIndex(), e1.getRawValue());
			assertTrue(indices.removeIf(e2 -> equalsMetaElement(e1, e2)));
		});
		assertEquals(0, indices.size());
	}

	// indexedMetas()
	// IllegalStateException 動作モードが編集モード
	@Test
	public void testIndexedMetas_IsEditMode() {
		var s = specForMetaStream();
		var c = new BmsContent(s);
		c.beginEdit();
		assertThrows(IllegalStateException.class, () -> c.indexedMetas());
	}

	// indexedMetas(String)
	// 指定された名前のデータあり索引付きメタ情報のみが走査されること
	@Test
	public void testIndexedMetasString_HasData() {
		var s = specForMetaStream();
		var c = new BmsContent(s);
		var indices = new ArrayList<>(List.of(
				new BmsMetaElement(s.getIndexedMeta("#iflt"), 3, 100.234),
				new BmsMetaElement(s.getIndexedMeta("#iflt"), 4, 200.345),
				new BmsMetaElement(s.getIndexedMeta("#iflt"), 8, 300.456)));
		c.beginEdit();
		indices.stream().filter(BmsMetaElement::isContain).forEach(c::putMeta);
		c.endEdit();
		c.indexedMetas("#iflt").forEach(e1 -> {
			//System.out.printf("m=%s, i=%d, v=%s\n", e1.getMeta(), e1.getIndex(), e1.getRawValue());
			assertTrue(indices.removeIf(e2 -> equalsMetaElement(e1, e2)));
		});
		assertEquals(0, indices.size());
	}

	// indexedMetas(String)
	// 指定された名前のデータなし索引付きメタ情報のみが走査されること(インデックスが-1)
	@Test
	public void testIndexedMetasString_NoData() {
		var s = specForMetaStream();
		var c = new BmsContent(s);
		var l = c.indexedMetas("#istr").collect(Collectors.toList());
		assertEquals(1, l.size());
		var e = l.get(0);
		assertEquals("#istr", e.getName());
		assertEquals(-1, e.getIndex());
		assertFalse(e.isContain());
		assertNull(e.getRawValue());
	}

	// indexedMetas(String)
	// IllegalStateException 動作モードが編集モード
	@Test
	public void testIndexedMetasString_IsEditMode() {
		var s = specForMetaStream();
		var c = new BmsContent(s);
		c.beginEdit();
		assertThrows(IllegalStateException.class, () -> c.indexedMetas("#iint"));
	}

	// indexedMetas(String)
	// NullPointerException nameがnull
	@Test
	public void testIndexedMetasString_NullName() {
		var s = specForMetaStream();
		var c = new BmsContent(s);
		assertThrows(NullPointerException.class, () -> c.indexedMetas(null));
	}

	// indexedMetas(String)
	// IllegalArgumentException nameに該当する複数メタ情報がBMS仕様に存在しない
	@Test
	public void testIndexedMetasString_NoMeta() {
		var s = specForMetaStream();
		var c = new BmsContent(s);
		assertThrows(IllegalArgumentException.class, () -> c.indexedMetas("#notfound"));
	}

	// setInitialBpm(double)
	// 正常
	@Test
	public void testSetInitialBpm_001() {
		var c = new BmsContent(spec());

		// 設定した値が入っていること
		var bpm = 250.875;
		c.beginEdit();
		c.setInitialBpm(bpm);
		c.endEdit();
		assertEquals(bpm, c.getInitialBpm(), 0.0);
	}

	// setInitialBpm(double)
	// IllegalStateException 動作モードが編集モードではない
	@Test
	public void testSetInitialBpm_002() {
		var c = new BmsContent(spec());
		assertThrows(IllegalStateException.class, () -> c.setInitialBpm(130.0));
	}

	// setInitialBpm(double)
	// IllegalArgumentException bpmが{@link BmsSpec#BPM_MIN}未満、または{@link BmsSpec#BPM_MAX}超過
	@Test
	public void testSetInitialBpm_003() {
		var c = new BmsContent(spec());
		var ex = IllegalArgumentException.class;
		c.beginEdit();
		assertThrows(ex, () -> c.setInitialBpm(BmsSpec.BPM_MIN - 0.1));
		assertThrows(ex, () -> c.setInitialBpm(BmsSpec.BPM_MAX + 0.1));
	}

	// getInitialBpm()
	// 正常
	@Test
	public void testGetInitialBpm_001() {
		var c = new BmsContent(spec());
		var bpm = 325.2297;
		c.beginEdit();
		c.setInitialBpm(bpm);
		c.endEdit();
		assertEquals(bpm, c.getInitialBpm(), 0.0);
		assertEquals(bpm, (double)c.getSingleMeta(c.getSpec().getInitialBpmMeta().getName()), 0.0);
	}

	private static boolean equalsMetaElement(BmsMetaElement e1, BmsMetaElement e2) {
		return e1.getMeta().equals(e2.getMeta()) &&
				e1.getIndex() == e2.getIndex() &&
				e1.isContain() == e2.isContain() && (
						(e1.isContain() && e1.getRawValue().equals(e2.getRawValue())) ||
						(!e1.isContain() && e1.getRawValue() == null && e2.getRawValue() == null)
				);
	}

	private static BmsSpec specForMetaStream() {
		var b = new BmsSpecBuilder();
		var l = List.of(
				BmsMeta.single("#sint", BmsType.INTEGER, "1", 0, true),
				BmsMeta.single("#sflt", BmsType.FLOAT, "2", 0, true),
				BmsMeta.single("#sstr", BmsType.STRING, "", 0, true),
				BmsMeta.single("#sb16", BmsType.BASE16, "03", 0, true),
				BmsMeta.single("#sb36", BmsType.BASE36, "04", 0, true),
				BmsMeta.single("#sa16", BmsType.ARRAY16, "05", 0, true),
				BmsMeta.single("#sa36", BmsType.ARRAY36, "06", 0, true),
				BmsMeta.single("#sobj", BmsType.OBJECT, null, 0, false),
				BmsMeta.multiple("#mint", BmsType.INTEGER, "1", 0, true),
				BmsMeta.multiple("#mflt", BmsType.FLOAT, "2", 0, true),
				BmsMeta.multiple("#mstr", BmsType.STRING, "", 0, true),
				BmsMeta.multiple("#mb16", BmsType.BASE16, "03", 0, true),
				BmsMeta.multiple("#mb36", BmsType.BASE36, "04", 0, true),
				BmsMeta.multiple("#ma16", BmsType.ARRAY16, "05", 0, true),
				BmsMeta.multiple("#ma36", BmsType.ARRAY36, "06", 0, true),
				BmsMeta.multiple("#mobj", BmsType.OBJECT, null, 0, false),
				BmsMeta.indexed("#iint", BmsType.INTEGER, "1", 0, true),
				BmsMeta.indexed("#iflt", BmsType.FLOAT, "2", 0, true),
				BmsMeta.indexed("#istr", BmsType.STRING, "", 0, true),
				BmsMeta.indexed("#ib16", BmsType.BASE16, "03", 0, true),
				BmsMeta.indexed("#ib36", BmsType.BASE36, "04", 0, true),
				BmsMeta.indexed("#ia16", BmsType.ARRAY16, "05", 0, true),
				BmsMeta.indexed("#ia36", BmsType.ARRAY36, "06", 0, true),
				BmsMeta.indexed("#iobj", BmsType.OBJECT, null, 0, false));
		l.stream().forEach(b::addMeta);
		b.addChannel(BmsChannel.spec(1, BmsType.ARRAY36, null, "", false, true));
		b.setInitialBpmMeta("#sflt");
		return b.create();
	}
}
