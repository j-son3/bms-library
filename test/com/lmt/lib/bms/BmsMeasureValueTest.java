package com.lmt.lib.bms;

import static org.junit.Assert.*;

import org.junit.Test;

public class BmsMeasureValueTest {
	// getValueAsLong()
	// 対応する型(long, double)をlong型に変換して返すこと
	@Test
	public void testGetValueAsLong_Ok() {
		var eInt = new BmsMeasureValue(0, 1, 0, 100L);
		var eFlt = new BmsMeasureValue(0, 1, 0, 200.123);
		assertEquals(100L, eInt.getValueAsLong());
		assertEquals(200L, eFlt.getValueAsLong());
	}

	// getValueAsLong()
	// ClassCastException 非対応型の要素で当メソッドを実行した
	@Test
	public void testGetValueAsLong_Fail() {
		var eStr = new BmsMeasureValue(0, 1, 0, "STR");
		var eAry = new BmsMeasureValue(0, 1, 0, new BmsArray("XXYY", 36));
		var ex = ClassCastException.class;
		assertThrows(ex, () -> eStr.getValueAsLong());
		assertThrows(ex, () -> eAry.getValueAsLong());
	}

	// getValueAsDouble()
	// 対応する型(long, double)をdouble型に変換して返すこと
	@Test
	public void testGetValueAsDouble_Ok() {
		var eInt = new BmsMeasureValue(0, 1, 0, 100L);
		var eFlt = new BmsMeasureValue(0, 1, 0, 200.123);
		assertEquals(100.0, eInt.getValueAsDouble(), 0.0);
		assertEquals(200.123, eFlt.getValueAsDouble(), 0.0);
	}

	// getValueAsDouble()
	// ClassCastException 非対応型の要素で当メソッドを実行した
	@Test
	public void testGetValueAsDouble_Fail() {
		var eStr = new BmsMeasureValue(0, 1, 0, "STR");
		var eAry = new BmsMeasureValue(0, 1, 0, new BmsArray("XXYY", 36));
		var ex = ClassCastException.class;
		assertThrows(ex, () -> eStr.getValueAsDouble());
		assertThrows(ex, () -> eAry.getValueAsDouble());
	}

	// getValueAsString()
	// 全ての型をString型に変換して返せること
	@Test
	public void testGetValueAsString() {
		var eInt = new BmsMeasureValue(0, 1, 0, 100L);
		var eFlt = new BmsMeasureValue(0, 1, 0, 200.123);
		var eStr = new BmsMeasureValue(0, 1, 0, "STR");
		var eAry = new BmsMeasureValue(0, 1, 0, new BmsArray("XXYY", 36));
		assertEquals("100", eInt.getValueAsString());
		assertEquals("200.123", eFlt.getValueAsString());
		assertEquals("STR", eStr.getValueAsString());
		assertEquals("XXYY", eAry.getValueAsString());
	}

	// getValueAsArray()
	// 対応する型(BMS配列)をBMS配列型に変換して返すこと
	@Test
	public void testGetValueAsArray_Ok() {
		var value = new BmsArray("XXYY", 36);
		var eAry = new BmsMeasureValue(0, 1, 0, value);
		assertEquals(value, eAry.getValueAsArray());
	}

	// getValueAsArray()
	// ClassCastException 非対応型の要素で当メソッドを実行した
	@Test
	public void testGetValueAsArray_Fail() {
		var eInt = new BmsMeasureValue(0, 1, 0, 100L);
		var eFlt = new BmsMeasureValue(0, 1, 0, 200.123);
		var eStr = new BmsMeasureValue(0, 1, 0, "STR");
		var ex = ClassCastException.class;
		assertThrows(ex, () -> eInt.getValueAsArray());
		assertThrows(ex, () -> eFlt.getValueAsArray());
		assertThrows(ex, () -> eStr.getValueAsArray());
	}

	// getValueAsObject()
	// 全ての型をObject型に変換して返せること
	@Test
	public void testGetValueAsObject() {
		var eInt = new BmsMeasureValue(0, 1, 0, 100L);
		var eFlt = new BmsMeasureValue(0, 1, 0, 200.123);
		var eStr = new BmsMeasureValue(0, 1, 0, "STR");
		var ary = new BmsArray("XXYY", 36);
		var eAry = new BmsMeasureValue(0, 1, 0, ary);
		assertEquals(100L, eInt.getValueAsObject());
		assertEquals(200.123, eFlt.getValueAsObject());
		assertEquals("STR", eStr.getValueAsObject());
		assertEquals(ary, eAry.getValueAsObject());
	}

	// isMeasureLineElement()
	// falseを返すこと
	@Test
	public void testIsMeasureLineElement() {
		var e = new BmsMeasureValue(0, 1, 0, "VALUE");
		assertFalse(e.isMeasureLineElement());
	}

	// isMeasureValueElement()
	// trueを返すこと
	@Test
	public void testIsMeasureValueElement() {
		var e = new BmsMeasureValue(0, 1, 0, "VALUE");
		assertTrue(e.isMeasureValueElement());
	}

	// isNoteElement()
	// falseを返すこと
	@Test
	public void testIsNoteElement() {
		var e = new BmsMeasureValue(0, 1, 0, "VALUE");
		assertFalse(e.isNoteElement());
	}
}
