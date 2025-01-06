package com.lmt.lib.bms.parse;

import static org.junit.Assert.*;

import org.junit.Test;

public class BmsMeasureValueTest {
	// BmsMeasureValueParsed()
	// 全ての値が初期値のオブジェクトが構築されること
	@Test
	public void testBmsMeasureValue1() {
		var p = new BmsMeasureValueParsed();
		assertEquals(0, p.lineNumber);
		assertNull(p.line);
		assertEquals(0, p.measure);
		assertEquals(0, p.number);
		assertNull(p.value);
	}

	// BmsMeasureValueParsed(int, Object, int, int, String)
	// 指定された値が初期値のオブジェクトが構築されること
	@Test
	public void testBmsMeasureValue2() {
		var p = new BmsMeasureValueParsed(100, "line", 1, 2, "value");
		assertEquals(100, p.lineNumber);
		assertEquals("line", p.line);
		assertEquals(1, p.measure);
		assertEquals(2, p.number);
		assertEquals("value", p.value);
	}

	// set(int, Object, int, int, String)
	// 指定された値が設定されること
	@Test
	public void testSet() {
		var p = new BmsMeasureValueParsed();
		assertSame(p, p.set(100, "line", 1, 2, "value"));
		assertEquals(100, p.lineNumber);
		assertEquals("line", p.line);
		assertEquals(1, p.measure);
		assertEquals(2, p.number);
		assertEquals("value", p.value);
	}
}
