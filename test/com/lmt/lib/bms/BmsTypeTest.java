package com.lmt.lib.bms;

import static org.junit.Assert.*;

import java.util.List;
import java.util.regex.PatternSyntaxException;

import org.junit.Test;

public class BmsTypeTest {
	// BmsType(int, String, String, int, int, Predicate<Number>)
	// 正常
	@Test
	public void testBmsType_001() throws Exception {
		var ts = (int)Tests.getsf(BmsType.class, "TYPE_STRING");
		var t = new BmsType(ts, "type", "t", ".*", 10, BmsType.NTYPE_STRING, null);
		assertEquals("type", t.getName());
		assertEquals(".*", t.getPattern().pattern());
		assertEquals(10, t.getBase());
		assertEquals(BmsType.NTYPE_STRING, t.getNativeType());
	}

	// BmsType(int, String, String, int, int, Predicate<Number>)
	// NullPointerException patternがnull
	@Test
	public void testBmsType_002() {
		var ex = NullPointerException.class;
		assertThrows(ex, () -> new BmsType(0, "type", "t", null, 10, BmsType.NTYPE_LONG, null));
	}

	// BmsType(int, String, String, int, int, Predicate<Number>)
	// PatternSyntaxException 表現の構文が無効である場合
	@Test
	public void testBmsType_003() {
		var ex = PatternSyntaxException.class;
		assertThrows(ex, () -> new BmsType(0, "type", "t", "$$[[????]BAD PATTERN**^??^", 36, BmsType.NTYPE_ARRAY, null));
	}

	// equals(Object)
	@Test
	public void testEquals_001() throws Exception {
		var ts = (int)Tests.getsf(BmsType.class, "TYPE_STRING");
		var t = new BmsType(ts, "type", "t", "[a-z]*", 10, BmsType.NTYPE_STRING, null);
		assertEquals(false, t.equals(null));
		assertEquals(false, t.equals(100));
		assertEquals(false, t.equals(""));
		assertEquals(false, t.equals(new BmsType(ts, "type", "t", "DIFFERENT PATTERN", 10, BmsType.NTYPE_STRING, null)));
		assertEquals(false, t.equals(new BmsType(ts, "type", "t", "[a-z]*", 10, BmsType.NTYPE_ARRAY, null)));
		assertEquals(true, t.equals(new BmsType(ts, "type", "t", "[a-z]*", 10, BmsType.NTYPE_STRING, null)));
		assertEquals(true, t.equals(new BmsType(ts, "type", "t", "[a-z]*", 16, BmsType.NTYPE_STRING, null)));
	}

	// getPattern()
	@Test
	public void testGetPattern() {
		assertEquals("^[\\+\\-]?[0-9]+$", BmsType.INTEGER.getPattern().pattern());
		assertEquals("^[\\+\\-]?[0-9]+(\\.[0-9]+)?([eE][\\+\\-]?[0-9]+)?$", BmsType.FLOAT.getPattern().pattern());
		assertEquals(".*", BmsType.STRING.getPattern().pattern());
		assertEquals("^[a-fA-F0-9]{2}$", BmsType.BASE16.getPattern().pattern());
		assertEquals("^[a-zA-Z0-9]{2}$", BmsType.BASE36.getPattern().pattern());
		assertEquals("^[a-zA-Z0-9]{2}$", BmsType.BASE62.getPattern().pattern());
		assertEquals("^[a-zA-Z0-9]{2}$", BmsType.BASE.getPattern().pattern());
		assertEquals("^([a-fA-F0-9]{2})*$", BmsType.ARRAY16.getPattern().pattern());
		assertEquals("^([a-zA-Z0-9]{2})*$", BmsType.ARRAY36.getPattern().pattern());
		assertEquals("^([a-zA-Z0-9]{2})*$", BmsType.ARRAY62.getPattern().pattern());
		assertEquals("^([a-zA-Z0-9]{2})*$", BmsType.ARRAY.getPattern().pattern());
		assertEquals("^.*$", BmsType.OBJECT.getPattern().pattern());
	}

	// getBase()
	@Test
	public void testGetBase() {
		assertEquals(10, BmsType.INTEGER.getBase());
		assertEquals(10, BmsType.FLOAT.getBase());
		assertEquals(0, BmsType.STRING.getBase());
		assertEquals(16, BmsType.BASE16.getBase());
		assertEquals(36, BmsType.BASE36.getBase());
		assertEquals(62, BmsType.BASE62.getBase());
		assertEquals(62, BmsType.BASE.getBase());
		assertEquals(16, BmsType.ARRAY16.getBase());
		assertEquals(36, BmsType.ARRAY36.getBase());
		assertEquals(62, BmsType.ARRAY62.getBase());
		assertEquals(62, BmsType.ARRAY.getBase());
		assertEquals(0, BmsType.OBJECT.getBase());
	}

	// getNativeType()
	@Test
	public void testGetNativeType() {
		assertEquals(BmsType.NTYPE_LONG, BmsType.INTEGER.getNativeType());
		assertEquals(BmsType.NTYPE_DOUBLE, BmsType.FLOAT.getNativeType());
		assertEquals(BmsType.NTYPE_STRING, BmsType.STRING.getNativeType());
		assertEquals(BmsType.NTYPE_LONG, BmsType.BASE16.getNativeType());
		assertEquals(BmsType.NTYPE_LONG, BmsType.BASE36.getNativeType());
		assertEquals(BmsType.NTYPE_LONG, BmsType.BASE62.getNativeType());
		assertEquals(BmsType.NTYPE_LONG, BmsType.BASE.getNativeType());
		assertEquals(BmsType.NTYPE_ARRAY, BmsType.ARRAY16.getNativeType());
		assertEquals(BmsType.NTYPE_ARRAY, BmsType.ARRAY36.getNativeType());
		assertEquals(BmsType.NTYPE_ARRAY, BmsType.ARRAY62.getNativeType());
		assertEquals(BmsType.NTYPE_ARRAY, BmsType.ARRAY.getNativeType());
		assertEquals(BmsType.NTYPE_OBJECT, BmsType.OBJECT.getNativeType());
	}

	// isIntegerType()
	// 各BMSデータ型が期待通りの値を返すこと
	@Test
	public void testIsIntegerType() {
		assertTrue(BmsType.INTEGER.isIntegerType());
		assertFalse(BmsType.FLOAT.isIntegerType());
		assertFalse(BmsType.STRING.isIntegerType());
		assertFalse(BmsType.BASE16.isIntegerType());
		assertFalse(BmsType.BASE36.isIntegerType());
		assertFalse(BmsType.BASE62.isIntegerType());
		assertFalse(BmsType.BASE.isIntegerType());
		assertFalse(BmsType.ARRAY16.isIntegerType());
		assertFalse(BmsType.ARRAY36.isIntegerType());
		assertFalse(BmsType.ARRAY62.isIntegerType());
		assertFalse(BmsType.ARRAY.isIntegerType());
		assertFalse(BmsType.OBJECT.isIntegerType());
	}

	// isFloatType()
	// 各BMSデータ型が期待通りの値を返すこと
	@Test
	public void testIsFloatType() {
		assertFalse(BmsType.INTEGER.isFloatType());
		assertTrue(BmsType.FLOAT.isFloatType());
		assertFalse(BmsType.STRING.isFloatType());
		assertFalse(BmsType.BASE16.isFloatType());
		assertFalse(BmsType.BASE36.isFloatType());
		assertFalse(BmsType.BASE62.isFloatType());
		assertFalse(BmsType.BASE.isFloatType());
		assertFalse(BmsType.ARRAY16.isFloatType());
		assertFalse(BmsType.ARRAY36.isFloatType());
		assertFalse(BmsType.ARRAY62.isFloatType());
		assertFalse(BmsType.ARRAY.isFloatType());
		assertFalse(BmsType.OBJECT.isFloatType());
	}

	// isStringType()
	// 各BMSデータ型が期待通りの値を返すこと
	@Test
	public void testIsStringType() {
		assertFalse(BmsType.INTEGER.isStringType());
		assertFalse(BmsType.FLOAT.isStringType());
		assertTrue(BmsType.STRING.isStringType());
		assertFalse(BmsType.BASE16.isStringType());
		assertFalse(BmsType.BASE36.isStringType());
		assertFalse(BmsType.BASE62.isStringType());
		assertFalse(BmsType.BASE.isStringType());
		assertFalse(BmsType.ARRAY16.isStringType());
		assertFalse(BmsType.ARRAY36.isStringType());
		assertFalse(BmsType.ARRAY62.isStringType());
		assertFalse(BmsType.ARRAY.isStringType());
		assertFalse(BmsType.OBJECT.isStringType());
	}

	// isBase16Type()
	// 各BMSデータ型が期待通りの値を返すこと
	@Test
	public void testIsBase16Type() {
		assertFalse(BmsType.INTEGER.isBase16Type());
		assertFalse(BmsType.FLOAT.isBase16Type());
		assertFalse(BmsType.STRING.isBase16Type());
		assertTrue(BmsType.BASE16.isBase16Type());
		assertFalse(BmsType.BASE36.isBase16Type());
		assertFalse(BmsType.BASE62.isBase16Type());
		assertFalse(BmsType.BASE.isBase16Type());
		assertFalse(BmsType.ARRAY16.isBase16Type());
		assertFalse(BmsType.ARRAY36.isBase16Type());
		assertFalse(BmsType.ARRAY62.isBase16Type());
		assertFalse(BmsType.ARRAY.isBase16Type());
		assertFalse(BmsType.OBJECT.isBase16Type());
	}

	// isBase36Type()
	// 各BMSデータ型が期待通りの値を返すこと
	@Test
	public void testIsBase36Type() {
		assertFalse(BmsType.INTEGER.isBase36Type());
		assertFalse(BmsType.FLOAT.isBase36Type());
		assertFalse(BmsType.STRING.isBase36Type());
		assertFalse(BmsType.BASE16.isBase36Type());
		assertTrue(BmsType.BASE36.isBase36Type());
		assertFalse(BmsType.BASE62.isBase36Type());
		assertFalse(BmsType.BASE.isBase36Type());
		assertFalse(BmsType.ARRAY16.isBase36Type());
		assertFalse(BmsType.ARRAY36.isBase36Type());
		assertFalse(BmsType.ARRAY62.isBase36Type());
		assertFalse(BmsType.ARRAY.isBase36Type());
		assertFalse(BmsType.OBJECT.isBase36Type());
	}

	// isBase62Type()
	// 各BMSデータ型が期待通りの値を返すこと
	@Test
	public void testIsBase62Type() {
		assertFalse(BmsType.INTEGER.isBase62Type());
		assertFalse(BmsType.FLOAT.isBase62Type());
		assertFalse(BmsType.STRING.isBase62Type());
		assertFalse(BmsType.BASE16.isBase62Type());
		assertFalse(BmsType.BASE36.isBase62Type());
		assertTrue(BmsType.BASE62.isBase62Type());
		assertFalse(BmsType.BASE.isBase62Type());
		assertFalse(BmsType.ARRAY16.isBase62Type());
		assertFalse(BmsType.ARRAY36.isBase62Type());
		assertFalse(BmsType.ARRAY62.isBase62Type());
		assertFalse(BmsType.ARRAY.isBase62Type());
		assertFalse(BmsType.OBJECT.isBase62Type());
	}

	// isArray16Type()
	// 各BMSデータ型が期待通りの値を返すこと
	@Test
	public void testIsArray16Type() {
		assertFalse(BmsType.INTEGER.isArray16Type());
		assertFalse(BmsType.FLOAT.isArray16Type());
		assertFalse(BmsType.STRING.isArray16Type());
		assertFalse(BmsType.BASE16.isArray16Type());
		assertFalse(BmsType.BASE36.isArray16Type());
		assertFalse(BmsType.BASE62.isArray16Type());
		assertFalse(BmsType.BASE.isArray16Type());
		assertTrue(BmsType.ARRAY16.isArray16Type());
		assertFalse(BmsType.ARRAY36.isArray16Type());
		assertFalse(BmsType.ARRAY62.isArray16Type());
		assertFalse(BmsType.ARRAY.isArray16Type());
		assertFalse(BmsType.OBJECT.isArray16Type());
	}

	// isArray36Type()
	// 各BMSデータ型が期待通りの値を返すこと
	@Test
	public void testIsArray36Type() {
		assertFalse(BmsType.INTEGER.isArray36Type());
		assertFalse(BmsType.FLOAT.isArray36Type());
		assertFalse(BmsType.STRING.isArray36Type());
		assertFalse(BmsType.BASE16.isArray36Type());
		assertFalse(BmsType.BASE36.isArray36Type());
		assertFalse(BmsType.BASE62.isArray36Type());
		assertFalse(BmsType.BASE.isArray36Type());
		assertFalse(BmsType.ARRAY16.isArray36Type());
		assertTrue(BmsType.ARRAY36.isArray36Type());
		assertFalse(BmsType.ARRAY62.isArray36Type());
		assertFalse(BmsType.ARRAY.isArray36Type());
		assertFalse(BmsType.OBJECT.isArray36Type());
	}

	// isArray62Type()
	// 各BMSデータ型が期待通りの値を返すこと
	@Test
	public void testIsArray62Type() {
		assertFalse(BmsType.INTEGER.isArray62Type());
		assertFalse(BmsType.FLOAT.isArray62Type());
		assertFalse(BmsType.STRING.isArray62Type());
		assertFalse(BmsType.BASE16.isArray62Type());
		assertFalse(BmsType.BASE36.isArray62Type());
		assertFalse(BmsType.BASE62.isArray62Type());
		assertFalse(BmsType.BASE.isArray62Type());
		assertFalse(BmsType.ARRAY16.isArray62Type());
		assertFalse(BmsType.ARRAY36.isArray62Type());
		assertTrue(BmsType.ARRAY62.isArray62Type());
		assertFalse(BmsType.ARRAY.isArray62Type());
		assertFalse(BmsType.OBJECT.isArray62Type());
	}

	// isObjectType()
	// 各BMSデータ型が期待通りの値を返すこと
	@Test
	public void testIsObjectType() {
		assertFalse(BmsType.INTEGER.isObjectType());
		assertFalse(BmsType.FLOAT.isObjectType());
		assertFalse(BmsType.STRING.isObjectType());
		assertFalse(BmsType.BASE16.isObjectType());
		assertFalse(BmsType.BASE36.isObjectType());
		assertFalse(BmsType.BASE62.isObjectType());
		assertFalse(BmsType.BASE.isObjectType());
		assertFalse(BmsType.ARRAY16.isObjectType());
		assertFalse(BmsType.ARRAY36.isObjectType());
		assertFalse(BmsType.ARRAY62.isObjectType());
		assertFalse(BmsType.ARRAY.isObjectType());
		assertTrue(BmsType.OBJECT.isObjectType());
	}

	// isNumberType()
	@Test
	public void testIsNumberType() {
		assertEquals(true, BmsType.INTEGER.isNumberType());
		assertEquals(true, BmsType.FLOAT.isNumberType());
		assertEquals(false, BmsType.STRING.isNumberType());
		assertEquals(true, BmsType.BASE16.isNumberType());
		assertEquals(true, BmsType.BASE36.isNumberType());
		assertEquals(true, BmsType.BASE62.isNumberType());
		assertEquals(true, BmsType.BASE.isNumberType());
		assertEquals(false, BmsType.ARRAY16.isNumberType());
		assertEquals(false, BmsType.ARRAY36.isNumberType());
		assertEquals(false, BmsType.ARRAY62.isNumberType());
		assertEquals(false, BmsType.ARRAY.isNumberType());
		assertEquals(false, BmsType.OBJECT.isNumberType());
	}

	// isArrayType()
	@Test
	public void testIsArrayType() {
		assertEquals(false, BmsType.INTEGER.isArrayType());
		assertEquals(false, BmsType.FLOAT.isArrayType());
		assertEquals(false, BmsType.STRING.isArrayType());
		assertEquals(false, BmsType.BASE16.isArrayType());
		assertEquals(false, BmsType.BASE36.isArrayType());
		assertEquals(false, BmsType.BASE62.isArrayType());
		assertEquals(false, BmsType.BASE.isArrayType());
		assertEquals(true, BmsType.ARRAY16.isArrayType());
		assertEquals(true, BmsType.ARRAY36.isArrayType());
		assertEquals(true, BmsType.ARRAY62.isArrayType());
		assertEquals(true, BmsType.ARRAY.isArrayType());
		assertEquals(false, BmsType.OBJECT.isArrayType());
	}

	// isValueType()
	@Test
	public void testIsValueType() {
		assertEquals(true, BmsType.INTEGER.isValueType());
		assertEquals(true, BmsType.FLOAT.isValueType());
		assertEquals(true, BmsType.STRING.isValueType());
		assertEquals(true, BmsType.BASE16.isValueType());
		assertEquals(true, BmsType.BASE36.isValueType());
		assertEquals(true, BmsType.BASE62.isValueType());
		assertEquals(true, BmsType.BASE.isValueType());
		assertEquals(false, BmsType.ARRAY16.isValueType());
		assertEquals(false, BmsType.ARRAY36.isValueType());
		assertEquals(false, BmsType.ARRAY62.isValueType());
		assertEquals(false, BmsType.ARRAY.isValueType());
		assertEquals(false, BmsType.OBJECT.isValueType());
	}

	// isNormalType()
	// 各BMSデータ型が期待通りの値を返すこと
	@Test
	public void testIsNormalType() {
		assertTrue(BmsType.INTEGER.isNormalType());
		assertTrue(BmsType.FLOAT.isNormalType());
		assertTrue(BmsType.STRING.isNormalType());
		assertTrue(BmsType.BASE16.isNormalType());
		assertTrue(BmsType.BASE36.isNormalType());
		assertTrue(BmsType.BASE62.isNormalType());
		assertTrue(BmsType.BASE.isNormalType());
		assertTrue(BmsType.ARRAY16.isNormalType());
		assertTrue(BmsType.ARRAY36.isNormalType());
		assertTrue(BmsType.ARRAY62.isNormalType());
		assertTrue(BmsType.ARRAY.isNormalType());
		assertFalse(BmsType.OBJECT.isNormalType());
	}

	// isSelectableBaseType()
	// 各BMSデータ型が期待通りの値を返すこと
	@Test
	public void testIsSelectableBaseType() {
		assertFalse(BmsType.INTEGER.isSelectableBaseType());
		assertFalse(BmsType.FLOAT.isSelectableBaseType());
		assertFalse(BmsType.STRING.isSelectableBaseType());
		assertFalse(BmsType.BASE16.isSelectableBaseType());
		assertFalse(BmsType.BASE36.isSelectableBaseType());
		assertFalse(BmsType.BASE62.isSelectableBaseType());
		assertTrue(BmsType.BASE.isSelectableBaseType());
		assertFalse(BmsType.ARRAY16.isSelectableBaseType());
		assertFalse(BmsType.ARRAY36.isSelectableBaseType());
		assertFalse(BmsType.ARRAY62.isSelectableBaseType());
		assertFalse(BmsType.ARRAY.isSelectableBaseType());
		assertFalse(BmsType.OBJECT.isSelectableBaseType());
	}

	// isSelectableArrayType()
	// 各BMSデータ型が期待通りの値を返すこと
	@Test
	public void testIsSelectableArrayType() {
		assertFalse(BmsType.INTEGER.isSelectableArrayType());
		assertFalse(BmsType.FLOAT.isSelectableArrayType());
		assertFalse(BmsType.STRING.isSelectableArrayType());
		assertFalse(BmsType.BASE16.isSelectableArrayType());
		assertFalse(BmsType.BASE36.isSelectableArrayType());
		assertFalse(BmsType.BASE62.isSelectableArrayType());
		assertFalse(BmsType.BASE.isSelectableArrayType());
		assertFalse(BmsType.ARRAY16.isSelectableArrayType());
		assertFalse(BmsType.ARRAY36.isSelectableArrayType());
		assertFalse(BmsType.ARRAY62.isSelectableArrayType());
		assertTrue(BmsType.ARRAY.isSelectableArrayType());
		assertFalse(BmsType.OBJECT.isSelectableArrayType());
	}

	// isSelectable()
	// 各BMSデータ型が期待通りの値を返すこと
	@Test
	public void testIsSelectable() {
		assertFalse(BmsType.INTEGER.isSelectable());
		assertFalse(BmsType.FLOAT.isSelectable());
		assertFalse(BmsType.STRING.isSelectable());
		assertFalse(BmsType.BASE16.isSelectable());
		assertFalse(BmsType.BASE36.isSelectable());
		assertFalse(BmsType.BASE62.isSelectable());
		assertTrue(BmsType.BASE.isSelectable());
		assertFalse(BmsType.ARRAY16.isSelectable());
		assertFalse(BmsType.ARRAY36.isSelectable());
		assertFalse(BmsType.ARRAY62.isSelectable());
		assertTrue(BmsType.ARRAY.isSelectable());
		assertFalse(BmsType.OBJECT.isSelectable());
	}

	// test(String)
	// 正常
	@Test
	public void testTest() {
		BmsType t;

		t = BmsType.INTEGER;
		assertEquals(true, t.test("0"));
		assertEquals(true, t.test("12345"));
		assertEquals(true, t.test("+350"));
		assertEquals(true, t.test("-2000"));
		assertEquals(false, t.test("+-255"));
		assertEquals(false, t.test("-+9999"));
		assertEquals(false, t.test("3.14159"));
		assertEquals(false, t.test(" 150"));
		assertEquals(false, t.test("510 "));
		assertEquals(false, t.test("NAN"));

		t = BmsType.FLOAT;
		assertEquals(true, t.test("123.456"));
		assertEquals(true, t.test("+15.789"));
		assertEquals(true, t.test("-22877.115"));
		assertEquals(true, t.test("65535"));
		assertEquals(true, t.test("+55789"));
		assertEquals(true, t.test("-112543"));
		assertEquals(true, t.test("1.5e2"));
		assertEquals(true, t.test("100e-3"));
		assertEquals(true, t.test("123.456e+11"));
		assertEquals(true, t.test("0.112E5"));
		assertEquals(true, t.test("+789.0123E+003"));
		assertEquals(false, t.test("+-0.9876"));
		assertEquals(false, t.test("-+321.567"));
		assertEquals(false, t.test(" 6.789"));
		assertEquals(false, t.test("8.987 "));
		assertEquals(false, t.test("11.22.33"));
		assertEquals(false, t.test(".238"));
		assertEquals(false, t.test("NAN"));

		t = BmsType.STRING;
		assertEquals(true, t.test(""));
		assertEquals(true, t.test(" "));
		assertEquals(true, t.test("hoge"));
		assertEquals(true, t.test("			"));
		assertEquals(true, t.test("123"));
		assertEquals(true, t.test("4.567"));

		t = BmsType.BASE16;
		assertEquals(true, t.test("7a"));
		assertEquals(true, t.test("BC"));
		assertEquals(true, t.test("08"));
		assertEquals(false, t.test("3"));
		assertEquals(false, t.test("f"));
		assertEquals(false, t.test("1f0"));
		assertEquals(false, t.test("ECD"));
		assertEquals(false, t.test(" f1"));
		assertEquals(false, t.test("b7 "));
		assertEquals(false, t.test("NAN"));

		for (var bt : List.of(BmsType.BASE36, BmsType.BASE62, BmsType.BASE)) {
			t = bt;
			assertEquals(true, t.test("7a"));
			assertEquals(true, t.test("bz"));
			assertEquals(true, t.test("BC"));
			assertEquals(true, t.test("8Q"));
			assertEquals(true, t.test("08"));
			assertEquals(true, t.test("aP"));
			assertEquals(false, t.test("3"));
			assertEquals(false, t.test("f"));
			assertEquals(false, t.test("j"));
			assertEquals(false, t.test("1F0"));
			assertEquals(false, t.test("9UW"));
			assertEquals(false, t.test(" r1"));
			assertEquals(false, t.test("x7 "));
			assertEquals(false, t.test("NAN"));
		}

		t = BmsType.ARRAY16;
		assertEquals(true, t.test(""));
		assertEquals(true, t.test("10"));
		assertEquals(true, t.test("ab00"));
		assertEquals(true, t.test("A7"));
		assertEquals(true, t.test("01FE"));
		assertEquals(true, t.test("00FF00Db07ac"));
		assertEquals(false, t.test("0"));
		assertEquals(false, t.test("f"));
		assertEquals(false, t.test("D"));
		assertEquals(false, t.test("002"));
		assertEquals(false, t.test("ff9"));
		assertEquals(false, t.test("DE0"));
		assertEquals(false, t.test(" ff"));
		assertEquals(false, t.test("15 "));
		assertEquals(false, t.test("NAN"));

		for (var bt : List.of(BmsType.ARRAY36, BmsType.ARRAY62, BmsType.ARRAY)) {
			t = bt;
			assertEquals(true, t.test(""));
			assertEquals(true, t.test("10"));
			assertEquals(true, t.test("ak00"));
			assertEquals(true, t.test("dy77zz"));
			assertEquals(true, t.test("A7"));
			assertEquals(true, t.test("01FE"));
			assertEquals(true, t.test("01ZYWS"));
			assertEquals(true, t.test("00FZ00Ds07xc"));
			assertEquals(false, t.test("0"));
			assertEquals(false, t.test("f"));
			assertEquals(false, t.test("V"));
			assertEquals(false, t.test("002"));
			assertEquals(false, t.test("mf9"));
			assertEquals(false, t.test("DU0"));
			assertEquals(false, t.test(" zz"));
			assertEquals(false, t.test("15 "));
			assertEquals(false, t.test("NAN"));
		}
	}

	// test(String)
	// NullPointerException dataがnull
	@Test
	public void testTest_002() {
		assertThrows(NullPointerException.class, () -> BmsType.INTEGER.test(null));
	}

	// cast(Object)
	// 正常
	@Test
	public void testCast1_001() {
		BmsType t;

		t = BmsType.INTEGER;
		assertEquals(16L, (long)t.cast((byte)0x10));
		assertEquals(30000L, (long)t.cast((short)30000));
		assertEquals(1000000L, (long)t.cast((int)1000000));
		assertEquals(9999999999L, (long)t.cast((long)9999999999L));
		assertEquals(10L, (long)t.cast((float)10.567));
		assertEquals(-35L, (long)t.cast((float)-35.234));
		assertEquals(5000L, (long)t.cast((double)5000.12345));
		assertEquals(-13000L, (long)t.cast((double)-13000.8765));
		assertEquals(50L, (long)t.cast("50"));
		assertEquals(300L, (long)t.cast("+300"));
		assertEquals(-1200L, (long)t.cast("-1200"));

		t = BmsType.FLOAT;
		assertEquals(16.0, (double)t.cast((byte)0x10), 0.0);
		assertEquals(30000.0, (double)t.cast((short)30000), 0.0);
		assertEquals(1000000.0, (double)t.cast((int)1000000), 0.0);
		assertEquals(9999999999.0, (double)t.cast((long)9999999999L), 0.0);
		assertEquals(10.567000389099121, (double)t.cast((float)10.567), 0.0);
		assertEquals(-35.23400115966797, (double)t.cast((float)-35.234), 0.0);
		assertEquals(5000.12345, (double)t.cast((double)5000.12345), 0.0);
		assertEquals(-13000.8765, (double)t.cast((double)-13000.8765), 0.0);
		assertEquals(50.0, (double)t.cast("50"), 0.0);
		assertEquals(300.0, (double)t.cast("+300"), 0.0);
		assertEquals(-1200.0, (double)t.cast("-1200"), 0.0);
		assertEquals(3.14159, (double)t.cast("3.14159"), 0.0);
		assertEquals(-7.6543, (double)t.cast("-7.6543"), 0.0);

		t = BmsType.STRING;
		assertEquals("16", (String)t.cast((byte)0x10));
		assertEquals("30000", (String)t.cast((short)30000));
		assertEquals("1000000", (String)t.cast((int)1000000));
		assertEquals("9999999999", (String)t.cast((long)9999999999L));
		assertEquals("10.567", (String)t.cast((float)10.567));
		assertEquals("-35.234", (String)t.cast((float)-35.234));
		assertEquals("5000.12345", (String)t.cast((double)5000.12345));
		assertEquals("-13000.8765", (String)t.cast((double)-13000.8765));
		assertEquals("50", (String)t.cast("50"));
		assertEquals("+300", (String)t.cast("+300"));
		assertEquals("-1200", (String)t.cast("-1200"));
		assertEquals("This is a sentence.", (String)t.cast("This is a sentence."));
		assertEquals("0055AAFF", (String)t.cast(new BmsArray("0055aaFF", 16)));
		assertEquals("22008855CCAAXXYYZZ", (String)t.cast(new BmsArray("22008855ccAAxXYyZZ", 36)));

		t = BmsType.BASE16;
		assertEquals(16L, (long)t.cast((byte)0x10));
		assertEquals(30L, (long)t.cast((short)30));
		assertEquals(100L, (long)t.cast((int)100));
		assertEquals(99L, (long)t.cast((long)99L));
		assertEquals(10L, (long)t.cast((float)10.567));
		assertEquals(50L, (long)t.cast((double)50.12345));
		assertEquals(80L, (long)t.cast("50"));
		assertEquals(255L, (long)t.cast("+FF"));

		t = BmsType.BASE36;
		assertEquals(16L, (long)t.cast((byte)0x10));
		assertEquals(512L, (long)t.cast((short)512));
		assertEquals(1295L, (long)t.cast((int)1295));
		assertEquals(99L, (long)t.cast((long)99L));
		assertEquals(10L, (long)t.cast((float)10.567));
		assertEquals(1050L, (long)t.cast((double)1050.12345));
		assertEquals(0L, (long)t.cast("0"));
		assertEquals(1270L, (long)t.cast("ZA"));
		assertEquals(1295L, (long)t.cast("+ZZ"));

		var tbs = List.of(BmsType.BASE62, BmsType.BASE);
		for (var tb : tbs) {
			t = tb;
			assertEquals(16L, (long)t.cast((byte)0x10));
			assertEquals(512L, (long)t.cast((short)512));
			assertEquals(3483L, (long)t.cast((int)3483));
			assertEquals(99L, (long)t.cast((long)99L));
			assertEquals(10L, (long)t.cast((float)10.567));
			assertEquals(1050L, (long)t.cast((double)1050.12345));
			assertEquals(0L, (long)t.cast("0"));
			assertEquals(3818L, (long)t.cast("za"));
			assertEquals(3843L, (long)t.cast("+zz"));
		}

		t = BmsType.ARRAY16;
		assertEquals(new BmsArray("", 16), (BmsArray)t.cast(""));
		assertEquals(new BmsArray("00", 16), (BmsArray)t.cast("00"));
		assertEquals(new BmsArray("001020", 16), (BmsArray)t.cast("001020"));
		assertNotEquals(new BmsArray("001020", 36), (BmsArray)t.cast("001020"));
		assertNotEquals(new BmsArray("001020", 62), (BmsArray)t.cast("001020"));
		assertEquals(new BmsArray("AABBCCDDEEFF", 16), (BmsArray)t.cast("AABBCCDDEEFF"));
		assertNotEquals(new BmsArray("AABBCCDDEEFF", 36), (BmsArray)t.cast("AABBCCDDEEFF"));
		assertEquals(new BmsArray("ABCDEF", 16), (BmsArray)t.cast("abcdef"));

		t = BmsType.ARRAY36;
		assertEquals(new BmsArray("", 36), (BmsArray)t.cast(""));
		assertEquals(new BmsArray("00", 36), (BmsArray)t.cast("00"));
		assertEquals(new BmsArray("001020", 36), (BmsArray)t.cast("001020"));
		assertNotEquals(new BmsArray("001020", 16), (BmsArray)t.cast("001020"));
		assertNotEquals(new BmsArray("001020", 62), (BmsArray)t.cast("001020"));
		assertEquals(new BmsArray("GGPPVVZZ", 36), (BmsArray)t.cast("GGPPVVZZ"));
		assertEquals(new BmsArray("AABBCCXXYYZZ", 36), (BmsArray)t.cast("aabbccxxyyzz"));

		var tas = List.of(BmsType.ARRAY62, BmsType.ARRAY);
		for (var ta : tas) {
			t = ta;
			assertEquals(new BmsArray("", 62), (BmsArray)t.cast(""));
			assertEquals(new BmsArray("00", 62), (BmsArray)t.cast("00"));
			assertEquals(new BmsArray("001020", 62), (BmsArray)t.cast("001020"));
			assertNotEquals(new BmsArray("001020", 16), (BmsArray)t.cast("001020"));
			assertNotEquals(new BmsArray("001020", 36), (BmsArray)t.cast("001020"));
			assertEquals(new BmsArray("GGPPVVZZ", 62), (BmsArray)t.cast("GGPPVVZZ"));
			assertEquals(new BmsArray("aabbccxxyyzz", 62), (BmsArray)t.cast("aabbccxxyyzz"));
		}
	}

	// cast(Object)
	// 正常
	// 任意型への変換を行った場合、変換処理は行われず、戻り値はsrcと同じ参照を返します。
	@Test
	public void testCast1_001_Object() {
		var longValue = (Long)1L;
		var doubleValue = (Double)1.0;
		var strValue = "";
		var arrayValue = new BmsArray("AABB", 16);
		var objValue = new BmsTest.AnyType1(0, 0);

		var t = BmsType.OBJECT;
		assertSame(longValue, t.cast(longValue));
		assertSame(doubleValue, t.cast(doubleValue));
		assertSame(strValue, t.cast(strValue));
		assertSame(arrayValue, t.cast(arrayValue));
		assertSame(objValue, t.cast(objValue));
	}

	// cast(Object)
	// ClassCastException srcの変換に失敗、またはrequireの表現可能範囲を超えた
	@Test
	public void testCast1_002() {
		var ex = ClassCastException.class;
		var ti = BmsType.INTEGER;
//		assertThrows(ex, () ->ti.cast(" 100"));
//		assertThrows(ex, () ->ti.cast("100 "));
		assertThrows(ex, () -> ti.cast("3.14"));
		assertThrows(ex, () -> ti.cast("NAN"));
		assertThrows(ex, () -> ti.cast(new BmsArray(16)));
		assertThrows(ex, () -> ti.cast(new BmsArray(36)));
		assertThrows(ex, () -> ti.cast(new BmsArray(62)));
		assertThrows(ex, () -> ti.cast(new Object()));

		var tf = BmsType.FLOAT;
//		assertThrows(ex, () -> tf.cast(" 100"));
//		assertThrows(ex, () -> tf.cast("100 "));
//		assertThrows(ex, () -> tf.cast(" 1.56"));
//		assertThrows(ex, () -> tf.cast("90.34 "));
		assertThrows(ex, () -> tf.cast("1.45.78"));
		assertThrows(ex, () -> tf.cast(new BmsArray(16)));
		assertThrows(ex, () -> tf.cast(new BmsArray(36)));
		assertThrows(ex, () -> tf.cast(new BmsArray(62)));
		assertThrows(ex, () -> tf.cast(new Object()));

		var tb16 = BmsType.BASE16;
//		assertThrows(ex, () -> tb16.cast(" 100"));
//		assertThrows(ex, () -> tb16.cast("100 "));
		assertThrows(ex, () -> tb16.cast("3.14"));
		assertThrows(ex, () -> tb16.cast("NAN"));
		assertThrows(ex, () -> tb16.cast("-1"));
		assertThrows(ex, () -> tb16.cast("100"));
		assertThrows(ex, () -> tb16.cast(new BmsArray(16)));
		assertThrows(ex, () -> tb16.cast(new BmsArray(36)));
		assertThrows(ex, () -> tb16.cast(new BmsArray(62)));
		assertThrows(ex, () -> tb16.cast(new Object()));

		var tb36 = BmsType.BASE36;
//		assertThrows(ex, () -> tb36.cast(" 100"));
//		assertThrows(ex, () -> tb36.cast("100 "));
		assertThrows(ex, () -> tb36.cast("3.14"));
		assertThrows(ex, () -> tb36.cast("NAN"));
		assertThrows(ex, () -> tb36.cast("-1"));
		assertThrows(ex, () -> tb36.cast("100"));
		assertThrows(ex, () -> tb36.cast(new BmsArray(16)));
		assertThrows(ex, () -> tb36.cast(new BmsArray(36)));
		assertThrows(ex, () -> tb36.cast(new BmsArray(62)));
		assertThrows(ex, () -> tb36.cast(new Object()));

		var tbs = List.of(BmsType.BASE62, BmsType.BASE);
		for (var tb : tbs) {
//			assertThrows(ex, () -> tb.cast(" 100"));
//			assertThrows(ex, () -> tb.cast("100 "));
			assertThrows(ex, () -> tb.cast("3.14"));
			assertThrows(ex, () -> tb.cast("NAN"));
			assertThrows(ex, () -> tb.cast("-1"));
			assertThrows(ex, () -> tb.cast("100"));
			assertThrows(ex, () -> tb.cast(new BmsArray(16)));
			assertThrows(ex, () -> tb.cast(new BmsArray(36)));
			assertThrows(ex, () -> tb.cast(new BmsArray(62)));
			assertThrows(ex, () -> tb.cast(new Object()));
		}

		var ta16 = BmsType.ARRAY16;
		assertThrows(ex, () -> ta16.cast((byte)0));
		assertThrows(ex, () -> ta16.cast((short)257));
		assertThrows(ex, () -> ta16.cast((int)1299));
		assertThrows(ex, () -> ta16.cast((float)3.14f));
		assertThrows(ex, () -> ta16.cast((double)108.797));
		assertThrows(ex, () -> ta16.cast("0088AAFF "));
		assertThrows(ex, () -> ta16.cast(" FFBBAA4400"));
		assertThrows(ex, () -> ta16.cast("hoge"));
//		assertThrows(ex, () -> ta16.cast(new BmsArray(16)));
		assertThrows(ex, () -> ta16.cast(new BmsArray(36)));
		assertThrows(ex, () -> ta16.cast(new BmsArray(62)));
		assertThrows(ex, () -> ta16.cast(new Object()));

		var ta36 = BmsType.ARRAY36;
		assertThrows(ex, () -> ta36.cast((byte)0));
		assertThrows(ex, () -> ta36.cast((short)257));
		assertThrows(ex, () -> ta36.cast((int)1299));
		assertThrows(ex, () -> ta36.cast((float)3.14f));
		assertThrows(ex, () -> ta36.cast((double)108.797));
		assertThrows(ex, () -> ta36.cast("0088AAFF "));
		assertThrows(ex, () -> ta36.cast(" ZZGGAA4400"));
		assertThrows(ex, () -> ta36.cast(new BmsArray(16)));
//		assertThrows(ex, () -> ta36.cast(new BmsArray(36)));
		assertThrows(ex, () -> ta36.cast(new BmsArray(62)));
		assertThrows(ex, () -> ta36.cast(new Object()));

		var ta62 = BmsType.ARRAY62;
		assertThrows(ex, () -> ta62.cast((byte)0));
		assertThrows(ex, () -> ta62.cast((short)257));
		assertThrows(ex, () -> ta62.cast((int)1299));
		assertThrows(ex, () -> ta62.cast((float)3.14f));
		assertThrows(ex, () -> ta62.cast((double)108.797));
		assertThrows(ex, () -> ta62.cast("0088AAFF "));
		assertThrows(ex, () -> ta62.cast(" ZZGGAA4400"));
		assertThrows(ex, () -> ta62.cast(new BmsArray(16)));
		assertThrows(ex, () -> ta62.cast(new BmsArray(36)));
//		assertThrows(ex, () -> ta62.cast(new BmsArray(62)));
		assertThrows(ex, () -> ta62.cast(new Object()));

		var ta = BmsType.ARRAY;
		assertThrows(ex, () -> ta.cast((byte)0));
		assertThrows(ex, () -> ta.cast((short)257));
		assertThrows(ex, () -> ta.cast((int)1299));
		assertThrows(ex, () -> ta.cast((float)3.14f));
		assertThrows(ex, () -> ta.cast((double)108.797));
		assertThrows(ex, () -> ta.cast("0088AAFF "));
		assertThrows(ex, () -> ta.cast(" ZZGGAA4400"));
		// ※基数選択数値配列型では全ての基数の配列データをキャスト可能
//		assertThrows(ex, () -> ta.cast(new BmsArray(16)));
//		assertThrows(ex, () -> ta.cast(new BmsArray(36)));
//		assertThrows(ex, () -> ta.cast(new BmsArray(62)));
		assertThrows(ex, () -> ta.cast(new Object()));

		// BmsType.OBJECTのテストは testCast1_001_Object() でテスト済み
	}

	// cast(Object)
	// NullPointerException srcがnull
	@Test
	public void testCast1_003() {
		assertThrows(NullPointerException.class, () -> BmsType.INTEGER.cast(null));
	}

	// cast(Object, BmsType)
	@Test
	public void testCast2_001() {
		// cast(Object)をテストすることで網羅可能であるため、実施しない
	}

	// REGEX(String)
	// 正常
	@Test
	public void testREGEX_001() {
		var t = BmsType.REGEX("^[a-zA-Z0-9_]+$");
		assertEquals("^[a-zA-Z0-9_]+$", t.getPattern().pattern());
		assertEquals(0, t.getBase());
		assertEquals(BmsType.NTYPE_STRING, t.getNativeType());
		assertEquals(false, t.isNumberType());
		assertEquals(false, t.isArrayType());
		assertEquals(true, t.isValueType());
	}

	// REGEX(String)
	// NullPointerException patternがnull
	@Test
	public void testREGEX_002() {
		assertThrows(NullPointerException.class, () -> BmsType.REGEX(null));
	}

	// REGEX(String)
	// PatternSyntaxException 正規表現の構文が無効である場合
	@Test
	public void testREGEX_003() {
		assertThrows(PatternSyntaxException.class, () -> BmsType.REGEX("$[[[]**.???^$?"));
	}
}
