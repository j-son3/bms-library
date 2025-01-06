package com.lmt.lib.bms.parse;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

public class BmsNoteParsedTest {
	// BmsNoteParsed()
	// 全ての値が初期値のオブジェクトが構築されること
	@Test
	public void testBmsNoteParsed1() {
		var p = new BmsNoteParsed();
		assertEquals(0, p.lineNumber);
		assertNull(p.line);
		assertEquals(0, p.measure);
		assertEquals(0, p.number);
		assertNull(p.array);
		assertNull(p.encodedArray);
	}

	// BmsNoteParsed(int, Object, int, int, List<Integer>)
	// 指定された値が初期値のオブジェクトが構築されること
	@Test
	public void testBmsNoteParsed2() {
		var a = List.of(1, 2, 3);
		var p = new BmsNoteParsed(100, "line", 1, 2, a);
		assertEquals(100, p.lineNumber);
		assertEquals("line", p.line);
		assertEquals(1, p.measure);
		assertEquals(2, p.number);
		assertSame(a, p.array);
		assertNull(p.encodedArray);
	}

	// BmsNoteParsed(int, Object, int, int, String)
	// 指定された値が初期値のオブジェクトが構築されること
	@Test
	public void testBmsNoteParsed3() {
		var a = "AAbb00yyZZ";
		var p = new BmsNoteParsed(100, "line", 1, 2, a);
		assertEquals(100, p.lineNumber);
		assertEquals("line", p.line);
		assertEquals(1, p.measure);
		assertEquals(2, p.number);
		assertNull(p.array);
		assertEquals(a, p.encodedArray);
	}

	// set(int, Object, int, int, List<Integer>)
	// 指定された値が設定されること
	@Test
	public void testSet1() {
		var a = List.of(1, 2, 3);
		var p = new BmsNoteParsed();
		assertSame(p, p.set(100, "line", 1, 2, a));
		assertEquals(100, p.lineNumber);
		assertEquals("line", p.line);
		assertEquals(1, p.measure);
		assertEquals(2, p.number);
		assertSame(a, p.array);
		assertNull(p.encodedArray);
	}

	// set(int, Object, int, int, String)
	// 指定された値が設定されること
	@Test
	public void testSet2() {
		var a = "AAbb00yyZZ";
		var p = new BmsNoteParsed();
		assertSame(p, p.set(100, "line", 1, 2, a));
		assertEquals(100, p.lineNumber);
		assertEquals("line", p.line);
		assertEquals(1, p.measure);
		assertEquals(2, p.number);
		assertNull(p.array);
		assertEquals(a, p.encodedArray);
	}
}
