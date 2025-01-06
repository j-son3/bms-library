package com.lmt.lib.bms.parse;

import static org.junit.Assert.*;

import org.junit.Test;

import com.lmt.lib.bms.BmsMeta;
import com.lmt.lib.bms.BmsType;

public class BmsMetaParsedTest {
	// BmsMetaParsed()
	// 全ての値が初期値のオブジェクトが構築されること
	@Test
	public void testBmsMetaParsed1() {
		var p = new BmsMetaParsed();
		assertEquals(0, p.lineNumber);
		assertNull(p.line);
		assertNull(p.meta);
		assertEquals(0, p.index);
		assertNull(p.encodedIndex);
		assertNull(p.value);
	}

	// BmsMetaParsed(int, Object, BmsMeta, int, String)
	// 指定された値が初期値のオブジェクトが構築されること
	@Test
	public void testBmsMetaParsed2() {
		var m = BmsMeta.single("#m", BmsType.INTEGER, "0", 0, false);
		var p = new BmsMetaParsed(100, "line", m, 1, "value");
		assertEquals(100, p.lineNumber);
		assertEquals("line", p.line);
		assertSame(m, p.meta);
		assertEquals(1, p.index);
		assertNull(p.encodedIndex);
		assertEquals("value", p.value);
	}

	// BmsMetaParsed(int, Object, BmsMeta, String, String)
	// 指定された値が初期値のオブジェクトが構築されること
	@Test
	public void testBmsMetaParsed3() {
		var m = BmsMeta.single("#m", BmsType.STRING, "0", 0, false);
		var p = new BmsMetaParsed(100, "line", m, "AB", "value");
		assertEquals(100, p.lineNumber);
		assertEquals("line", p.line);
		assertSame(m, p.meta);
		assertEquals(0, p.index);
		assertEquals("AB", p.encodedIndex);
		assertEquals("value", p.value);
	}

	// set(int, Object, BmsMeta, int, String)
	// 指定された値が設定されること
	@Test
	public void testSet1() {
		var m = BmsMeta.single("#m", BmsType.INTEGER, "0", 0, false);
		var p = new BmsMetaParsed();
		assertSame(p, p.set(100, "line", m, 1, "value"));
		assertEquals(100, p.lineNumber);
		assertEquals("line", p.line);
		assertSame(m, p.meta);
		assertEquals(1, p.index);
		assertNull(p.encodedIndex);
		assertEquals("value", p.value);
	}

	// set(int, Object, BmsMeta, String, String)
	// 指定された値が設定されること
	@Test
	public void testSet2() {
		var m = BmsMeta.single("#m", BmsType.STRING, "0", 0, false);
		var p = new BmsMetaParsed();
		assertSame(p, p.set(100, "line", m, "AB", "value"));
		assertEquals(100, p.lineNumber);
		assertEquals("line", p.line);
		assertSame(m, p.meta);
		assertEquals(0, p.index);
		assertEquals("AB", p.encodedIndex);
		assertEquals("value", p.value);
	}
}
