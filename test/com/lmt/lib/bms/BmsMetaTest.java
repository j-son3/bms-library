package com.lmt.lib.bms;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

public class BmsMetaTest {
	// BmsMeta(String, BmsUnit, BmsType, String, int, boolean)
	// 正常
	@Test
	public void testBmsMeta_001() {
		BmsMeta m;

		m = new BmsMeta("#value", BmsUnit.SINGLE, BmsType.INTEGER, "250", 1, false);
		assertEquals("#value", m.getName());
		assertEquals(BmsUnit.SINGLE, m.getUnit());
		assertEquals(BmsType.INTEGER, m.getType());
		assertEquals(250L, (long)m.getDefaultValue());
		assertEquals(1, m.getOrder());
		assertEquals(false, m.isUniqueness());

		m = new BmsMeta("#value", BmsUnit.MULTIPLE, BmsType.FLOAT, "270.876", 2, true);
		assertEquals("#value", m.getName());
		assertEquals(BmsUnit.MULTIPLE, m.getUnit());
		assertEquals(BmsType.FLOAT, m.getType());
		assertEquals(270.876, (double)m.getDefaultValue(), 0.0);
		assertEquals(2, m.getOrder());
		assertEquals(true, m.isUniqueness());

		m = new BmsMeta("#value", BmsUnit.INDEXED, BmsType.STRING, "This is a pen", 3, false);
		assertEquals("#value", m.getName());
		assertEquals(BmsUnit.INDEXED, m.getUnit());
		assertEquals(BmsType.STRING, m.getType());
		assertEquals("This is a pen", (String)m.getDefaultValue());
		assertEquals(3, m.getOrder());
		assertEquals(false, m.isUniqueness());

		m = new BmsMeta("#value", BmsUnit.SINGLE, BmsType.BASE16, "AB", 4, true);
		assertEquals("#value", m.getName());
		assertEquals(BmsUnit.SINGLE, m.getUnit());
		assertEquals(BmsType.BASE16, m.getType());
		assertEquals(0xabL, (long)m.getDefaultValue());
		assertEquals(4, m.getOrder());
		assertEquals(true, m.isUniqueness());

		m = new BmsMeta("#value", BmsUnit.MULTIPLE, BmsType.BASE36, "XY", 5, false);
		assertEquals("#value", m.getName());
		assertEquals(BmsUnit.MULTIPLE, m.getUnit());
		assertEquals(BmsType.BASE36, m.getType());
		assertEquals(1222L, (long)m.getDefaultValue());
		assertEquals(5, m.getOrder());
		assertEquals(false, m.isUniqueness());

		m = new BmsMeta("#value", BmsUnit.INDEXED, BmsType.BASE62, "yz", 5, true);
		assertEquals("#value", m.getName());
		assertEquals(BmsUnit.INDEXED, m.getUnit());
		assertEquals(BmsType.BASE62, m.getType());
		assertEquals(3781L, (long)m.getDefaultValue());
		assertEquals(5, m.getOrder());
		assertEquals(true, m.isUniqueness());

		m = new BmsMeta("#value", BmsUnit.SINGLE, BmsType.BASE, "aA", 5, false);
		assertEquals("#value", m.getName());
		assertEquals(BmsUnit.SINGLE, m.getUnit());
		assertEquals(BmsType.BASE62, m.getType());
		assertEquals(2242L, (long)m.getDefaultValue());
		assertEquals(5, m.getOrder());
		assertEquals(false, m.isUniqueness());

		m = new BmsMeta("#value", BmsUnit.INDEXED, BmsType.ARRAY16, "0055AAFF", 6, true);
		assertEquals("#value", m.getName());
		assertEquals(BmsUnit.INDEXED, m.getUnit());
		assertEquals(BmsType.ARRAY16, m.getType());
		assertEquals(new BmsArray("0055aaff", 16), (BmsArray)m.getDefaultValue());
		assertEquals(6, m.getOrder());
		assertEquals(true, m.isUniqueness());

		m = new BmsMeta("#value", BmsUnit.SINGLE, BmsType.ARRAY36, "ZZGG8800", 7, false);
		assertEquals("#value", m.getName());
		assertEquals(BmsUnit.SINGLE, m.getUnit());
		assertEquals(BmsType.ARRAY36, m.getType());
		assertEquals(new BmsArray("zzgg8800", 36), (BmsArray)m.getDefaultValue());
		assertEquals(7, m.getOrder());
		assertEquals(false, m.isUniqueness());

		m = new BmsMeta("#value", BmsUnit.MULTIPLE, BmsType.ARRAY62, "xxFF1100", 7, true);
		assertEquals("#value", m.getName());
		assertEquals(BmsUnit.MULTIPLE, m.getUnit());
		assertEquals(BmsType.ARRAY62, m.getType());
		assertEquals(new BmsArray("xxFF1100", 62), (BmsArray)m.getDefaultValue());
		assertEquals(7, m.getOrder());
		assertEquals(true, m.isUniqueness());

		m = new BmsMeta("#value", BmsUnit.INDEXED, BmsType.ARRAY, "YYgg2200", 7, false);
		assertEquals("#value", m.getName());
		assertEquals(BmsUnit.INDEXED, m.getUnit());
		assertEquals(BmsType.ARRAY, m.getType());
		assertEquals(new BmsArray("YYgg2200", 62), (BmsArray)m.getDefaultValue());
		assertEquals(7, m.getOrder());
		assertEquals(false, m.isUniqueness());

		// 任意型
		m = new BmsMeta("#value", BmsUnit.MULTIPLE, BmsType.OBJECT, null, 8, false);
		assertEquals("#value", m.getName());
		assertEquals(BmsUnit.MULTIPLE, m.getUnit());
		assertEquals(BmsType.OBJECT, m.getType());
		assertNull(m.getDefaultValue());
		assertEquals(m.getOrder(), 8);
		assertEquals(false, m.isUniqueness());
	}

	// BmsMeta(String, BmsUnit, BmsType, String, int, boolean)
	// NullPointerException nameがnull
	@Test
	public void testBmsMeta_002() {
		var ex = NullPointerException.class;
		assertThrows(ex, () -> new BmsMeta(null, BmsUnit.SINGLE, BmsType.INTEGER, "", 0, false));
	}

	// BmsMeta(String, BmsUnit, BmsType, String, int, boolean)
	// NullPointerException unitがnull
	@Test
	public void testBmsMeta_003() {
		var ex = NullPointerException.class;
		assertThrows(ex, () -> new BmsMeta("#value", null, BmsType.INTEGER, "", 0, false));
	}

	// BmsMeta(String, BmsUnit, BmsType, String, int, boolean)
	// NullPointerException typeがnull
	@Test
	public void testBmsMeta_004() {
		var ex = NullPointerException.class;
		assertThrows(ex, () -> new BmsMeta("#value", BmsUnit.SINGLE, null, "", 0, false));
	}

	// BmsMeta(String, BmsUnit, BmsType, String, int, boolean)
	// NullPointerException typeがOBJECT以外の時、defaultValueがnull
	@Test
	public void testBmsMeta_005() {
		var ex = NullPointerException.class;
		assertThrows(ex, () -> new BmsMeta("#value", BmsUnit.SINGLE, BmsType.INTEGER, null, 0, false));
		assertThrows(ex, () -> new BmsMeta("#value", BmsUnit.SINGLE, BmsType.FLOAT, null, 0, false));
		assertThrows(ex, () -> new BmsMeta("#value", BmsUnit.SINGLE, BmsType.STRING, null, 0, false));
		assertThrows(ex, () -> new BmsMeta("#value", BmsUnit.SINGLE, BmsType.BASE16, null, 0, false));
		assertThrows(ex, () -> new BmsMeta("#value", BmsUnit.SINGLE, BmsType.BASE36, null, 0, false));
		assertThrows(ex, () -> new BmsMeta("#value", BmsUnit.SINGLE, BmsType.BASE62, null, 0, false));
		assertThrows(ex, () -> new BmsMeta("#value", BmsUnit.SINGLE, BmsType.BASE, null, 0, false));
		assertThrows(ex, () -> new BmsMeta("#value", BmsUnit.SINGLE, BmsType.ARRAY16, null, 0, false));
		assertThrows(ex, () -> new BmsMeta("#value", BmsUnit.SINGLE, BmsType.ARRAY36, null, 0, false));
		assertThrows(ex, () -> new BmsMeta("#value", BmsUnit.SINGLE, BmsType.ARRAY62, null, 0, false));
		assertThrows(ex, () -> new BmsMeta("#value", BmsUnit.SINGLE, BmsType.ARRAY, null, 0, false));
	}

	// BmsMeta(String, BmsUnit, BmsType, String, int, boolean)
	// IllegalArgumentException nameがNAME_PATTERNにマッチしない
	@Test
	public void testBmsMeta_006() {
		var ex = IllegalArgumentException.class;
		var u = BmsUnit.SINGLE;
		var t = BmsType.STRING;
		var f = false;
		assertThrows(ex, () -> new BmsMeta("", u, t, "", 0, f));
		assertThrows(ex, () -> new BmsMeta("#", u, t, "", 0, f));
		assertThrows(ex, () -> new BmsMeta("%", u, t, "", 0, f));
		assertThrows(ex, () -> new BmsMeta("noprefix", u, t, "", 0, f));
		assertThrows(ex, () -> new BmsMeta("#UPPERCASE", u, t, "", 0, f));
		assertThrows(ex, () -> new BmsMeta("#uppercasE", u, t, "", 0, f));
		assertThrows(ex, () -> new BmsMeta("#1stnumber", u, t, "", 0, f));
		assertThrows(ex, () -> new BmsMeta("#/1stslash", u, t, "", 0, f));
		assertThrows(ex, () -> new BmsMeta("$wrongsymbol", u, t, "", 0, f));
		assertThrows(ex, () -> new BmsMeta("#toolongname_____________________________________________________", u, t, "", 0, f));
	}

	// BmsMeta(String, BmsUnit, BmsType, String, int, boolean)
	// IllegalArgumentException defaultValueがtypeの書式に適合しない
	@Test
	public void testBmsMeta_007() {
		var ex = IllegalArgumentException.class;
		var n = "#value";
		var u = BmsUnit.SINGLE;
		var ti = BmsType.INTEGER;
		assertThrows(ex, () -> new BmsMeta(n, u, ti, "", 0, false));
		assertThrows(ex, () -> new BmsMeta(n, u, ti, "100 ", 0, false));
		assertThrows(ex, () -> new BmsMeta(n, u, ti, " 100", 0, false));
		assertThrows(ex, () -> new BmsMeta(n, u, ti, "3.14", 0, false));
		assertThrows(ex, () -> new BmsMeta(n, u, ti, "NAN", 0, false));
		assertThrows(ex, () -> new BmsMeta(n, u, ti, "FF", 0, false));
		assertThrows(ex, () -> new BmsMeta(n, u, ti, "ZZ", 0, false));
		assertThrows(ex, () -> new BmsMeta(n, u, ti, "0088EE", 0, false));
		assertThrows(ex, () -> new BmsMeta(n, u, ti, "ZZHH00", 0, false));

		var tf = BmsType.FLOAT;
		assertThrows(ex, () -> new BmsMeta(n, u, tf, "", 0, false));
		assertThrows(ex, () -> new BmsMeta(n, u, tf, "100 ", 0, false));
		assertThrows(ex, () -> new BmsMeta(n, u, tf, " 100", 0, false));
		assertThrows(ex, () -> new BmsMeta(n, u, tf, ".98", 0, false));
		assertThrows(ex, () -> new BmsMeta(n, u, tf, "1.2.3", 0, false));
		assertThrows(ex, () -> new BmsMeta(n, u, tf, "NAN", 0, false));
		assertThrows(ex, () -> new BmsMeta(n, u, tf, "FF", 0, false));
		assertThrows(ex, () -> new BmsMeta(n, u, tf, "ZZ", 0, false));
		assertThrows(ex, () -> new BmsMeta(n, u, tf, "0088EE", 0, false));
		assertThrows(ex, () -> new BmsMeta(n, u, tf, "ZZHH00", 0, false));

		var tbs = List.of(BmsType.BASE16, BmsType.BASE36, BmsType.BASE62, BmsType.BASE);
		for (var tb : tbs) {
			assertThrows(ex, () -> new BmsMeta(n, u, tb, "", 0, false));
			assertThrows(ex, () -> new BmsMeta(n, u, tb, "10 ", 0, false));
			assertThrows(ex, () -> new BmsMeta(n, u, tb, " 10", 0, false));
			assertThrows(ex, () -> new BmsMeta(n, u, tb, "1.2", 0, false));
			assertThrows(ex, () -> new BmsMeta(n, u, tb, "NAN", 0, false));
			assertThrows(ex, () -> new BmsMeta(n, u, tb, "A$", 0, false));
			assertThrows(ex, () -> new BmsMeta(n, u, tb, "0088EE", 0, false));
			assertThrows(ex, () -> new BmsMeta(n, u, tb, "ZZHH00", 0, false));
		}

		var tas = List.of(BmsType.ARRAY16, BmsType.ARRAY36, BmsType.ARRAY62, BmsType.ARRAY);
		for (var ta : tas) {
			assertThrows(ex, () -> new BmsMeta(n, u, ta, " 10", 0, false));
			assertThrows(ex, () -> new BmsMeta(n, u, ta, "10 ", 0, false));
			assertThrows(ex, () -> new BmsMeta(n, u, ta, "00Z", 0, false));
			assertThrows(ex, () -> new BmsMeta(n, u, ta, "AABBC$", 0, false));
		}
	}

	// BmsMeta(String, BmsUnit, BmsType, String, int, boolean)
	// IllegalArgumentException typeがOBJECTの時、defaultValueがnullではない
	@Test
	public void testBmsMeta_008() {
		var ex = IllegalArgumentException.class;
		assertThrows(ex, () -> new BmsMeta("#value", BmsUnit.SINGLE, BmsType.OBJECT, "NOT NULL", 0, false));
	}

	// BmsMeta(String, BmsUnit, BmsType, String, int, boolean)
	// IllegalArgumentException typeがOBJECTの時、uniquenessがtrue
	@Test
	public void testBmsMeta_009() {
		var ex = IllegalArgumentException.class;
		assertThrows(ex, () -> new BmsMeta("#value", BmsUnit.SINGLE, BmsType.OBJECT, null, 0, true));
	}

	// object(String, BmsUnit)
	// 正常
	// ・引数で指定した名称であること
	// ・引数で指定した構成単位であること
	// ・データ型がOBJECTであること
	// ・初期値がnullであること
	// ・同一性チェックがOFFであること
	// ・isObject()がtrueを返すこと
	@Test
	public void testObject_001() {
		var units = new BmsUnit[] { BmsUnit.SINGLE, BmsUnit.MULTIPLE, BmsUnit.INDEXED };
		for (var unit : units) {
			var m = BmsMeta.object("#objmeta", unit);
			assertEquals("#objmeta", m.getName());
			assertEquals(unit, m.getUnit());
			assertEquals(BmsType.OBJECT, m.getType());
			assertNull(m.getDefaultValue());
			assertFalse(m.isUniqueness());
			assertTrue(m.isObjectType());
		}
	}

	// object(String, BmsUnit)
	// NullPointerException nameがnull
	@Test
	public void testObject_002() {
		assertThrows(NullPointerException.class, () -> BmsMeta.object(null, BmsUnit.SINGLE));
	}

	// object(String, BmsUnit)
	// NullPointerException unitがnull
	@Test
	public void testObject_003() {
		assertThrows(NullPointerException.class, () -> BmsMeta.object("#objmeta", null));
	}

	// object(String, BmsUnit)
	// IllegalArgumentException nameが{@link #NAME_PATTERN}にマッチしない
	@Test
	public void testObject_004() {
		var ex = IllegalArgumentException.class;
		var u = BmsUnit.SINGLE;
		assertThrows(ex, () -> BmsMeta.object("", u));
		assertThrows(ex, () -> BmsMeta.object("#", u));
		assertThrows(ex, () -> BmsMeta.object("%", u));
		assertThrows(ex, () -> BmsMeta.object("noprefix", u));
		assertThrows(ex, () -> BmsMeta.object("#UPPERCASE", u));
		assertThrows(ex, () -> BmsMeta.object("#uppercasE", u));
		assertThrows(ex, () -> BmsMeta.object("#1stnumber", u));
		assertThrows(ex, () -> BmsMeta.object("#/1stslash", u));
		assertThrows(ex, () -> BmsMeta.object("$wrongsymbol", u));
		assertThrows(ex, () -> BmsMeta.object("#toolongname_____________________________________________________", u));
	}

	// isObjectType()
	// 正常
	// ・各データ型でメソッドが正確な値を返すこと（OBJECT型のみtrue、それ以外はfalse）
	@Test
	public void testIsObjectType_001() {
		assertFalse((new BmsMeta("#value", BmsUnit.SINGLE, BmsType.INTEGER, "0", 0, false)).isObjectType());
		assertFalse((new BmsMeta("#value", BmsUnit.SINGLE, BmsType.FLOAT, "0", 0, false)).isObjectType());
		assertFalse((new BmsMeta("#value", BmsUnit.SINGLE, BmsType.STRING, "", 0, false)).isObjectType());
		assertFalse((new BmsMeta("#value", BmsUnit.SINGLE, BmsType.BASE16, "00", 0, false)).isObjectType());
		assertFalse((new BmsMeta("#value", BmsUnit.SINGLE, BmsType.BASE36, "00", 0, false)).isObjectType());
		assertFalse((new BmsMeta("#value", BmsUnit.SINGLE, BmsType.BASE62, "00", 0, false)).isObjectType());
		assertFalse((new BmsMeta("#value", BmsUnit.SINGLE, BmsType.BASE, "00", 0, false)).isObjectType());
		assertFalse((new BmsMeta("#value", BmsUnit.SINGLE, BmsType.ARRAY16, "00", 0, false)).isObjectType());
		assertFalse((new BmsMeta("#value", BmsUnit.SINGLE, BmsType.ARRAY36, "00", 0, false)).isObjectType());
		assertFalse((new BmsMeta("#value", BmsUnit.SINGLE, BmsType.ARRAY62, "00", 0, false)).isObjectType());
		assertFalse((new BmsMeta("#value", BmsUnit.SINGLE, BmsType.ARRAY, "00", 0, false)).isObjectType());
		assertTrue((new BmsMeta("#value", BmsUnit.SINGLE, BmsType.OBJECT, null, 0, false)).isObjectType());
	}

	// isBaseChanger()
	// 基数選択メタ情報が設定された時trueになること
	@Test
	public void testIsBaseChanger_True() {
		var m = BmsMeta.single("#m", BmsType.INTEGER, "16", 0, true);
		m.setIsBaseChanger();
		assertTrue(m.isBaseChanger());
	}

	// isBaseChanger()
	// 基数選択メタ情報が設定されていない時falseになること
	@Test
	public void testIsBaseChanger_False() {
		var m = BmsMeta.single("#m", BmsType.INTEGER, "16", 0, true);
		assertFalse(m.isBaseChanger());
	}

	// isSingleUnit()
	// 構成単位が単体の場合trueになること
	@Test
	public void testIsSingleUnit_True() {
		assertTrue(BmsMeta.single("#meta", BmsType.INTEGER, "0", 0, false).isSingleUnit());
	}

	// isSingleUnit()
	// 構成単位が単体以外の場合falseになること
	@Test
	public void testIsSingleUnit_False() {
		assertFalse(BmsMeta.multiple("#meta", BmsType.INTEGER, "0", 0, false).isSingleUnit());
		assertFalse(BmsMeta.indexed("#meta", BmsType.INTEGER, "0", 0, false).isSingleUnit());
	}

	// isMultipleUnit()
	// 構成単位が複数の場合trueになること
	@Test
	public void testIsMultipleUnit_True() {
		assertTrue(BmsMeta.multiple("#meta", BmsType.INTEGER, "0", 0, false).isMultipleUnit());
	}

	// isMultipleUnit()
	// 構成単位が複数以外の場合falseになること
	@Test
	public void testIsMultipleUnit_False() {
		assertFalse(BmsMeta.single("#meta", BmsType.INTEGER, "0", 0, false).isMultipleUnit());
		assertFalse(BmsMeta.indexed("#meta", BmsType.INTEGER, "0", 0, false).isMultipleUnit());
	}

	// isIndexedUnit()
	// 構成単位が索引付きの場合trueになること
	@Test
	public void testIsIndexedUnit_True() {
		assertTrue(BmsMeta.indexed("#meta", BmsType.INTEGER, "0", 0, false).isIndexedUnit());
	}

	// isIndexedUnit()
	// 構成単位が索引付き以外の場合falseになること
	@Test
	public void testIsIndexedUnit_False() {
		assertFalse(BmsMeta.single("#meta", BmsType.INTEGER, "0", 0, false).isIndexedUnit());
		assertFalse(BmsMeta.multiple("#meta", BmsType.INTEGER, "0", 0, false).isIndexedUnit());
	}

	// isIntegerType()
	// 構成型が整数の場合trueになること
	@Test
	public void testIsIntegerType_True() {
		assertTrue(BmsMeta.single("#meta", BmsType.INTEGER, "0", 0, false).isIntegerType());
	}

	// isIntegerType()
	// 構成型が整数以外の場合falseになること
	@Test
	public void testIsIntegerType_False() {
		assertFalse(BmsMeta.single("#meta", BmsType.FLOAT, "0", 0, false).isIntegerType());
		assertFalse(BmsMeta.single("#meta", BmsType.STRING, "", 0, false).isIntegerType());
		assertFalse(BmsMeta.single("#meta", BmsType.BASE16, "00", 0, false).isIntegerType());
		assertFalse(BmsMeta.single("#meta", BmsType.BASE36, "00", 0, false).isIntegerType());
		assertFalse(BmsMeta.single("#meta", BmsType.BASE62, "00", 0, false).isIntegerType());
		assertFalse(BmsMeta.single("#meta", BmsType.BASE, "00", 0, false).isIntegerType());
		assertFalse(BmsMeta.single("#meta", BmsType.ARRAY16, "00", 0, false).isIntegerType());
		assertFalse(BmsMeta.single("#meta", BmsType.ARRAY36, "00", 0, false).isIntegerType());
		assertFalse(BmsMeta.single("#meta", BmsType.ARRAY62, "00", 0, false).isIntegerType());
		assertFalse(BmsMeta.single("#meta", BmsType.ARRAY, "00", 0, false).isIntegerType());
		assertFalse(BmsMeta.single("#meta", BmsType.OBJECT, null, 0, false).isIntegerType());
	}

	// isFloatType()
	// データ型が時数の場合trueになること
	@Test
	public void testIsFloatType_True() {
		assertTrue(BmsMeta.single("#meta", BmsType.FLOAT, "0", 0, false).isFloatType());
	}

	// isFloatType()
	// データ型が実数以外の場合falseになること
	@Test
	public void testIsFloatType_False() {
		assertFalse(BmsMeta.single("#meta", BmsType.INTEGER, "0", 0, false).isFloatType());
		assertFalse(BmsMeta.single("#meta", BmsType.STRING, "", 0, false).isFloatType());
		assertFalse(BmsMeta.single("#meta", BmsType.BASE16, "00", 0, false).isFloatType());
		assertFalse(BmsMeta.single("#meta", BmsType.BASE36, "00", 0, false).isFloatType());
		assertFalse(BmsMeta.single("#meta", BmsType.BASE62, "00", 0, false).isFloatType());
		assertFalse(BmsMeta.single("#meta", BmsType.BASE, "00", 0, false).isFloatType());
		assertFalse(BmsMeta.single("#meta", BmsType.ARRAY16, "00", 0, false).isFloatType());
		assertFalse(BmsMeta.single("#meta", BmsType.ARRAY36, "00", 0, false).isFloatType());
		assertFalse(BmsMeta.single("#meta", BmsType.ARRAY62, "00", 0, false).isFloatType());
		assertFalse(BmsMeta.single("#meta", BmsType.ARRAY, "00", 0, false).isFloatType());
		assertFalse(BmsMeta.single("#meta", BmsType.OBJECT, null, 0, false).isFloatType());
	}

	// isStringType()
	// データ型が文字列の場合trueになること
	@Test
	public void testIsStringType_True() {
		assertTrue(BmsMeta.single("#meta", BmsType.STRING, "", 0, false).isStringType());
	}

	// isStringType()
	// データ型が文字列以外の場合falseになること
	@Test
	public void testIsStringType_False() {
		assertFalse(BmsMeta.single("#meta", BmsType.INTEGER, "0", 0, false).isStringType());
		assertFalse(BmsMeta.single("#meta", BmsType.FLOAT, "0", 0, false).isStringType());
		assertFalse(BmsMeta.single("#meta", BmsType.BASE16, "00", 0, false).isStringType());
		assertFalse(BmsMeta.single("#meta", BmsType.BASE36, "00", 0, false).isStringType());
		assertFalse(BmsMeta.single("#meta", BmsType.BASE62, "00", 0, false).isStringType());
		assertFalse(BmsMeta.single("#meta", BmsType.BASE, "00", 0, false).isStringType());
		assertFalse(BmsMeta.single("#meta", BmsType.ARRAY16, "00", 0, false).isStringType());
		assertFalse(BmsMeta.single("#meta", BmsType.ARRAY36, "00", 0, false).isStringType());
		assertFalse(BmsMeta.single("#meta", BmsType.ARRAY62, "00", 0, false).isStringType());
		assertFalse(BmsMeta.single("#meta", BmsType.ARRAY, "00", 0, false).isStringType());
		assertFalse(BmsMeta.single("#meta", BmsType.OBJECT, null, 0, false).isStringType());
	}

	// isBase16Type()
	// データ型が16進数値の場合trueになること
	@Test
	public void testIsBase16Type_True() {
		assertTrue(BmsMeta.single("#meta", BmsType.BASE16, "00", 0, false).isBase16Type());
	}

	// isBase16Type()
	// データ型が16進数値以外の場合falseになること
	@Test
	public void testIsBase16Type_False() {
		assertFalse(BmsMeta.single("#meta", BmsType.INTEGER, "0", 0, false).isBase16Type());
		assertFalse(BmsMeta.single("#meta", BmsType.FLOAT, "0", 0, false).isBase16Type());
		assertFalse(BmsMeta.single("#meta", BmsType.STRING, "", 0, false).isBase16Type());
		assertFalse(BmsMeta.single("#meta", BmsType.BASE36, "00", 0, false).isBase16Type());
		assertFalse(BmsMeta.single("#meta", BmsType.BASE62, "00", 0, false).isBase16Type());
		assertFalse(BmsMeta.single("#meta", BmsType.BASE, "00", 0, false).isBase16Type());
		assertFalse(BmsMeta.single("#meta", BmsType.ARRAY16, "00", 0, false).isBase16Type());
		assertFalse(BmsMeta.single("#meta", BmsType.ARRAY36, "00", 0, false).isBase16Type());
		assertFalse(BmsMeta.single("#meta", BmsType.ARRAY62, "00", 0, false).isBase16Type());
		assertFalse(BmsMeta.single("#meta", BmsType.ARRAY, "00", 0, false).isBase16Type());
		assertFalse(BmsMeta.single("#meta", BmsType.OBJECT, null, 0, false).isBase16Type());
	}

	// isBase36Type()
	// データ型が36進数値の場合trueになること
	@Test
	public void testIsBase36Type_True() {
		assertTrue(BmsMeta.single("#meta", BmsType.BASE36, "00", 0, false).isBase36Type());
	}

	// isBase36Type()
	// データ型が36進数値以外の場合falseになること
	@Test
	public void testIsBase36Type_False() {
		assertFalse(BmsMeta.single("#meta", BmsType.INTEGER, "0", 0, false).isBase36Type());
		assertFalse(BmsMeta.single("#meta", BmsType.FLOAT, "0", 0, false).isBase36Type());
		assertFalse(BmsMeta.single("#meta", BmsType.STRING, "", 0, false).isBase36Type());
		assertFalse(BmsMeta.single("#meta", BmsType.BASE16, "00", 0, false).isBase36Type());
		assertFalse(BmsMeta.single("#meta", BmsType.BASE62, "00", 0, false).isBase36Type());
		assertFalse(BmsMeta.single("#meta", BmsType.BASE, "00", 0, false).isBase36Type());
		assertFalse(BmsMeta.single("#meta", BmsType.ARRAY16, "00", 0, false).isBase36Type());
		assertFalse(BmsMeta.single("#meta", BmsType.ARRAY36, "00", 0, false).isBase36Type());
		assertFalse(BmsMeta.single("#meta", BmsType.ARRAY62, "00", 0, false).isBase36Type());
		assertFalse(BmsMeta.single("#meta", BmsType.ARRAY, "00", 0, false).isBase36Type());
		assertFalse(BmsMeta.single("#meta", BmsType.OBJECT, null, 0, false).isBase36Type());
	}

	// isBase62Type()
	// データ型が62進数値の場合trueになること
	@Test
	public void testIsBase62Type_True() {
		assertTrue(BmsMeta.single("#meta", BmsType.BASE62, "00", 0, false).isBase62Type());
	}

	// isBase62Type()
	// データ型が62進数値以外の場合falseになること
	@Test
	public void testIsBase62Type_False() {
		assertFalse(BmsMeta.single("#meta", BmsType.INTEGER, "0", 0, false).isBase62Type());
		assertFalse(BmsMeta.single("#meta", BmsType.FLOAT, "0", 0, false).isBase62Type());
		assertFalse(BmsMeta.single("#meta", BmsType.STRING, "", 0, false).isBase62Type());
		assertFalse(BmsMeta.single("#meta", BmsType.BASE16, "00", 0, false).isBase62Type());
		assertFalse(BmsMeta.single("#meta", BmsType.BASE36, "00", 0, false).isBase62Type());
		assertFalse(BmsMeta.single("#meta", BmsType.BASE, "00", 0, false).isBase62Type());
		assertFalse(BmsMeta.single("#meta", BmsType.ARRAY16, "00", 0, false).isBase62Type());
		assertFalse(BmsMeta.single("#meta", BmsType.ARRAY36, "00", 0, false).isBase62Type());
		assertFalse(BmsMeta.single("#meta", BmsType.ARRAY62, "00", 0, false).isBase62Type());
		assertFalse(BmsMeta.single("#meta", BmsType.ARRAY, "00", 0, false).isBase62Type());
		assertFalse(BmsMeta.single("#meta", BmsType.OBJECT, null, 0, false).isBase62Type());
	}

	// isArray16Type()
	// データ型が16進数値配列の場合trueになること
	@Test
	public void testIsArray16Type_True() {
		assertTrue(BmsMeta.single("#meta", BmsType.ARRAY16, "00", 0, false).isArray16Type());
	}

	// isArray16Type()
	// データ型が16進数値配列以外の場合falseになること
	@Test
	public void testIsArray16Type_False() {
		assertFalse(BmsMeta.single("#meta", BmsType.INTEGER, "0", 0, false).isArray16Type());
		assertFalse(BmsMeta.single("#meta", BmsType.FLOAT, "0", 0, false).isArray16Type());
		assertFalse(BmsMeta.single("#meta", BmsType.STRING, "", 0, false).isArray16Type());
		assertFalse(BmsMeta.single("#meta", BmsType.BASE16, "00", 0, false).isArray16Type());
		assertFalse(BmsMeta.single("#meta", BmsType.BASE36, "00", 0, false).isArray16Type());
		assertFalse(BmsMeta.single("#meta", BmsType.BASE62, "00", 0, false).isArray16Type());
		assertFalse(BmsMeta.single("#meta", BmsType.BASE, "00", 0, false).isArray16Type());
		assertFalse(BmsMeta.single("#meta", BmsType.ARRAY36, "00", 0, false).isArray16Type());
		assertFalse(BmsMeta.single("#meta", BmsType.ARRAY62, "00", 0, false).isArray16Type());
		assertFalse(BmsMeta.single("#meta", BmsType.ARRAY, "00", 0, false).isArray16Type());
		assertFalse(BmsMeta.single("#meta", BmsType.OBJECT, null, 0, false).isArray16Type());
	}

	// isArray36Type()
	// データ型が36進数値配列の場合trueになること
	@Test
	public void testIsArray36Type_True() {
		assertTrue(BmsMeta.single("#meta", BmsType.ARRAY36, "00", 0, false).isArray36Type());
	}

	// isArray36Type()
	// データ型が36進数値配列以外の場合falseになること
	@Test
	public void testIsArray36Type_False() {
		assertFalse(BmsMeta.single("#meta", BmsType.INTEGER, "0", 0, false).isArray36Type());
		assertFalse(BmsMeta.single("#meta", BmsType.FLOAT, "0", 0, false).isArray36Type());
		assertFalse(BmsMeta.single("#meta", BmsType.STRING, "", 0, false).isArray36Type());
		assertFalse(BmsMeta.single("#meta", BmsType.BASE16, "00", 0, false).isArray36Type());
		assertFalse(BmsMeta.single("#meta", BmsType.BASE36, "00", 0, false).isArray36Type());
		assertFalse(BmsMeta.single("#meta", BmsType.BASE62, "00", 0, false).isArray36Type());
		assertFalse(BmsMeta.single("#meta", BmsType.BASE, "00", 0, false).isArray36Type());
		assertFalse(BmsMeta.single("#meta", BmsType.ARRAY16, "00", 0, false).isArray36Type());
		assertFalse(BmsMeta.single("#meta", BmsType.ARRAY62, "00", 0, false).isArray36Type());
		assertFalse(BmsMeta.single("#meta", BmsType.ARRAY, "00", 0, false).isArray36Type());
		assertFalse(BmsMeta.single("#meta", BmsType.OBJECT, null, 0, false).isArray36Type());
	}

	// isArray62Type()
	// データ型が62進数値配列の場合trueになること
	@Test
	public void testIsArray62Type_True() {
		assertTrue(BmsMeta.single("#meta", BmsType.ARRAY62, "00", 0, false).isArray62Type());
	}

	// isArray62Type()
	// データ型が62進数値配列以外の場合falseになること
	@Test
	public void testIsArray62Type_False() {
		assertFalse(BmsMeta.single("#meta", BmsType.INTEGER, "0", 0, false).isArray62Type());
		assertFalse(BmsMeta.single("#meta", BmsType.FLOAT, "0", 0, false).isArray62Type());
		assertFalse(BmsMeta.single("#meta", BmsType.STRING, "", 0, false).isArray62Type());
		assertFalse(BmsMeta.single("#meta", BmsType.BASE16, "00", 0, false).isArray62Type());
		assertFalse(BmsMeta.single("#meta", BmsType.BASE36, "00", 0, false).isArray62Type());
		assertFalse(BmsMeta.single("#meta", BmsType.BASE62, "00", 0, false).isArray62Type());
		assertFalse(BmsMeta.single("#meta", BmsType.BASE, "00", 0, false).isArray62Type());
		assertFalse(BmsMeta.single("#meta", BmsType.ARRAY16, "00", 0, false).isArray62Type());
		assertFalse(BmsMeta.single("#meta", BmsType.ARRAY36, "00", 0, false).isArray62Type());
		assertFalse(BmsMeta.single("#meta", BmsType.ARRAY, "00", 0, false).isArray62Type());
		assertFalse(BmsMeta.single("#meta", BmsType.OBJECT, null, 0, false).isArray62Type());
	}

	// isObjectType()
	// データ型が任意型の場合trueになること
	@Test
	public void testIsObjectType_True() {
		assertTrue(BmsMeta.single("#meta", BmsType.OBJECT, null, 0, false).isObjectType());
	}

	// isObjectType()
	// データ型が任意型以外の場合falseになること
	@Test
	public void testIsObjectType_False() {
		assertFalse(BmsMeta.single("#meta", BmsType.INTEGER, "0", 0, false).isObjectType());
		assertFalse(BmsMeta.single("#meta", BmsType.FLOAT, "0", 0, false).isObjectType());
		assertFalse(BmsMeta.single("#meta", BmsType.STRING, "", 0, false).isObjectType());
		assertFalse(BmsMeta.single("#meta", BmsType.BASE16, "00", 0, false).isObjectType());
		assertFalse(BmsMeta.single("#meta", BmsType.BASE36, "00", 0, false).isObjectType());
		assertFalse(BmsMeta.single("#meta", BmsType.BASE62, "00", 0, false).isObjectType());
		assertFalse(BmsMeta.single("#meta", BmsType.BASE, "00", 0, false).isObjectType());
		assertFalse(BmsMeta.single("#meta", BmsType.ARRAY16, "00", 0, false).isObjectType());
		assertFalse(BmsMeta.single("#meta", BmsType.ARRAY36, "00", 0, false).isObjectType());
		assertFalse(BmsMeta.single("#meta", BmsType.ARRAY62, "00", 0, false).isObjectType());
		assertFalse(BmsMeta.single("#meta", BmsType.ARRAY, "00", 0, false).isObjectType());
	}

	// isNumberType()
	// データ型が数値型の場合trueになること
	@Test
	public void testIsNumberType_True() {
		assertTrue(BmsMeta.single("#meta", BmsType.INTEGER, "0", 0, false).isNumberType());
		assertTrue(BmsMeta.single("#meta", BmsType.FLOAT, "0", 0, false).isNumberType());
		assertTrue(BmsMeta.single("#meta", BmsType.BASE16, "00", 0, false).isNumberType());
		assertTrue(BmsMeta.single("#meta", BmsType.BASE36, "00", 0, false).isNumberType());
		assertTrue(BmsMeta.single("#meta", BmsType.BASE62, "00", 0, false).isNumberType());
		assertTrue(BmsMeta.single("#meta", BmsType.BASE, "00", 0, false).isNumberType());
	}

	// isNumberType()
	// データ型が数値型以外の場合falseになること
	@Test
	public void testIsNumberType_False() {
		assertFalse(BmsMeta.single("#meta", BmsType.STRING, "", 0, false).isNumberType());
		assertFalse(BmsMeta.single("#meta", BmsType.ARRAY16, "00", 0, false).isNumberType());
		assertFalse(BmsMeta.single("#meta", BmsType.ARRAY36, "00", 0, false).isNumberType());
		assertFalse(BmsMeta.single("#meta", BmsType.ARRAY62, "00", 0, false).isNumberType());
		assertFalse(BmsMeta.single("#meta", BmsType.ARRAY, "00", 0, false).isNumberType());
		assertFalse(BmsMeta.single("#meta", BmsType.OBJECT, null, 0, false).isNumberType());
	}

	// isValueType()
	// データ型が値型の場合trueになること
	@Test
	public void testIsValueType_True() {
		assertTrue(BmsMeta.single("#meta", BmsType.INTEGER, "0", 0, false).isValueType());
		assertTrue(BmsMeta.single("#meta", BmsType.FLOAT, "0", 0, false).isValueType());
		assertTrue(BmsMeta.single("#meta", BmsType.BASE16, "00", 0, false).isValueType());
		assertTrue(BmsMeta.single("#meta", BmsType.BASE36, "00", 0, false).isValueType());
		assertTrue(BmsMeta.single("#meta", BmsType.BASE62, "00", 0, false).isValueType());
		assertTrue(BmsMeta.single("#meta", BmsType.BASE, "00", 0, false).isValueType());
		assertTrue(BmsMeta.single("#meta", BmsType.STRING, "", 0, false).isValueType());
	}

	// isValueType()
	// データ型が値型以外の場合falseになること
	@Test
	public void testIsValueType_False() {
		assertFalse(BmsMeta.single("#meta", BmsType.ARRAY16, "00", 0, false).isValueType());
		assertFalse(BmsMeta.single("#meta", BmsType.ARRAY36, "00", 0, false).isValueType());
		assertFalse(BmsMeta.single("#meta", BmsType.ARRAY62, "00", 0, false).isValueType());
		assertFalse(BmsMeta.single("#meta", BmsType.ARRAY, "00", 0, false).isValueType());
		assertFalse(BmsMeta.single("#meta", BmsType.OBJECT, null, 0, false).isValueType());
	}

	// isArrayType()
	// データ型が配列型の場合trueになること
	@Test
	public void testIsArrayType_True() {
		assertTrue(BmsMeta.single("#meta", BmsType.ARRAY16, "00", 0, false).isArrayType());
		assertTrue(BmsMeta.single("#meta", BmsType.ARRAY36, "00", 0, false).isArrayType());
		assertTrue(BmsMeta.single("#meta", BmsType.ARRAY62, "00", 0, false).isArrayType());
		assertTrue(BmsMeta.single("#meta", BmsType.ARRAY, "00", 0, false).isArrayType());
	}

	// isArrayType()
	// データ型が配列型以外の場合falseになること
	@Test
	public void testIsArrayType_False() {
		assertFalse(BmsMeta.single("#meta", BmsType.INTEGER, "0", 0, false).isArrayType());
		assertFalse(BmsMeta.single("#meta", BmsType.FLOAT, "0", 0, false).isArrayType());
		assertFalse(BmsMeta.single("#meta", BmsType.BASE16, "00", 0, false).isArrayType());
		assertFalse(BmsMeta.single("#meta", BmsType.BASE36, "00", 0, false).isArrayType());
		assertFalse(BmsMeta.single("#meta", BmsType.BASE62, "00", 0, false).isArrayType());
		assertFalse(BmsMeta.single("#meta", BmsType.BASE, "00", 0, false).isArrayType());
		assertFalse(BmsMeta.single("#meta", BmsType.STRING, "", 0, false).isArrayType());
		assertFalse(BmsMeta.single("#meta", BmsType.OBJECT, null, 0, false).isArrayType());
	}

	// isSelectableBaseType()
	// データ型が基数選択数値型の場合trueになること
	@Test
	public void testIsSelectableBaseType_True() {
		assertTrue(BmsMeta.single("#meta", BmsType.BASE, "00", 0, false).isSelectableBaseType());
	}

	// isSelectableBaseType()
	// データ型が基数選択数値型以外の場合falseになること
	@Test
	public void testIsSelectableBaseType_False() {
		assertFalse(BmsMeta.single("#meta", BmsType.INTEGER, "0", 0, false).isSelectableBaseType());
		assertFalse(BmsMeta.single("#meta", BmsType.FLOAT, "0", 0, false).isSelectableBaseType());
		assertFalse(BmsMeta.single("#meta", BmsType.STRING, "", 0, false).isSelectableBaseType());
		assertFalse(BmsMeta.single("#meta", BmsType.BASE16, "00", 0, false).isSelectableBaseType());
		assertFalse(BmsMeta.single("#meta", BmsType.BASE36, "00", 0, false).isSelectableBaseType());
		assertFalse(BmsMeta.single("#meta", BmsType.BASE62, "00", 0, false).isSelectableBaseType());
		assertFalse(BmsMeta.single("#meta", BmsType.ARRAY16, "00", 0, false).isSelectableBaseType());
		assertFalse(BmsMeta.single("#meta", BmsType.ARRAY36, "00", 0, false).isSelectableBaseType());
		assertFalse(BmsMeta.single("#meta", BmsType.ARRAY62, "00", 0, false).isSelectableBaseType());
		assertFalse(BmsMeta.single("#meta", BmsType.ARRAY, "00", 0, false).isSelectableBaseType());
		assertFalse(BmsMeta.single("#meta", BmsType.OBJECT, null, 0, false).isSelectableBaseType());
	}

	// isSelectableBaseType()
	// データ型が基数選択数値配列型の場合trueになること
	@Test
	public void testIsSelectableArrayType_True() {
		assertTrue(BmsMeta.single("#meta", BmsType.ARRAY, "00", 0, false).isSelectableArrayType());
	}

	// isSelectableBaseType()
	// データ型が基数選択数値配列型以外の場合falseになること
	@Test
	public void testIsSelectableArrayType_False() {
		assertFalse(BmsMeta.single("#meta", BmsType.INTEGER, "0", 0, false).isSelectableArrayType());
		assertFalse(BmsMeta.single("#meta", BmsType.FLOAT, "0", 0, false).isSelectableArrayType());
		assertFalse(BmsMeta.single("#meta", BmsType.STRING, "", 0, false).isSelectableArrayType());
		assertFalse(BmsMeta.single("#meta", BmsType.BASE16, "00", 0, false).isSelectableArrayType());
		assertFalse(BmsMeta.single("#meta", BmsType.BASE36, "00", 0, false).isSelectableArrayType());
		assertFalse(BmsMeta.single("#meta", BmsType.BASE62, "00", 0, false).isSelectableArrayType());
		assertFalse(BmsMeta.single("#meta", BmsType.BASE, "00", 0, false).isSelectableArrayType());
		assertFalse(BmsMeta.single("#meta", BmsType.ARRAY16, "00", 0, false).isSelectableArrayType());
		assertFalse(BmsMeta.single("#meta", BmsType.ARRAY36, "00", 0, false).isSelectableArrayType());
		assertFalse(BmsMeta.single("#meta", BmsType.ARRAY62, "00", 0, false).isSelectableArrayType());
		assertFalse(BmsMeta.single("#meta", BmsType.OBJECT, null, 0, false).isSelectableArrayType());
	}

	// isSelectableBaseType()
	// データ型が基数選択可能な場合trueになること
	@Test
	public void testIsSelectableType_True() {
		assertTrue(BmsMeta.single("#meta", BmsType.BASE, "00", 0, false).isSelectableType());
		assertTrue(BmsMeta.single("#meta", BmsType.ARRAY, "00", 0, false).isSelectableType());
	}

	// isSelectableBaseType()
	// データ型が基数選択不可能な場合falseになること
	@Test
	public void testIsSelectableType_False() {
		assertFalse(BmsMeta.single("#meta", BmsType.INTEGER, "0", 0, false).isSelectableType());
		assertFalse(BmsMeta.single("#meta", BmsType.FLOAT, "0", 0, false).isSelectableType());
		assertFalse(BmsMeta.single("#meta", BmsType.STRING, "", 0, false).isSelectableType());
		assertFalse(BmsMeta.single("#meta", BmsType.BASE16, "00", 0, false).isSelectableType());
		assertFalse(BmsMeta.single("#meta", BmsType.BASE36, "00", 0, false).isSelectableType());
		assertFalse(BmsMeta.single("#meta", BmsType.BASE62, "00", 0, false).isSelectableType());
		assertFalse(BmsMeta.single("#meta", BmsType.ARRAY16, "00", 0, false).isSelectableType());
		assertFalse(BmsMeta.single("#meta", BmsType.ARRAY36, "00", 0, false).isSelectableType());
		assertFalse(BmsMeta.single("#meta", BmsType.ARRAY62, "00", 0, false).isSelectableType());
		assertFalse(BmsMeta.single("#meta", BmsType.OBJECT, null, 0, false).isSelectableType());
	}

	// isNormalType()
	// データ型が通常型の場合trueになること
	@Test
	public void testIsNormalType_True() {
		assertTrue(BmsMeta.single("#meta", BmsType.ARRAY16, "00", 0, false).isNormalType());
		assertTrue(BmsMeta.single("#meta", BmsType.ARRAY36, "00", 0, false).isNormalType());
		assertTrue(BmsMeta.single("#meta", BmsType.ARRAY62, "00", 0, false).isNormalType());
		assertTrue(BmsMeta.single("#meta", BmsType.ARRAY, "00", 0, false).isNormalType());
		assertTrue(BmsMeta.single("#meta", BmsType.INTEGER, "0", 0, false).isNormalType());
		assertTrue(BmsMeta.single("#meta", BmsType.FLOAT, "0", 0, false).isNormalType());
		assertTrue(BmsMeta.single("#meta", BmsType.BASE16, "00", 0, false).isNormalType());
		assertTrue(BmsMeta.single("#meta", BmsType.BASE36, "00", 0, false).isNormalType());
		assertTrue(BmsMeta.single("#meta", BmsType.BASE62, "00", 0, false).isNormalType());
		assertTrue(BmsMeta.single("#meta", BmsType.BASE, "00", 0, false).isNormalType());
		assertTrue(BmsMeta.single("#meta", BmsType.STRING, "", 0, false).isNormalType());
	}

	// isNormalType()
	// データ型が通常型以外の場合falseになること
	@Test
	public void testIsNormalType_False() {
		assertFalse(BmsMeta.single("#meta", BmsType.OBJECT, null, 0, false).isNormalType());
	}
}
