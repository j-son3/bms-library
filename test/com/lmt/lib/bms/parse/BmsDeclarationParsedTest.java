package com.lmt.lib.bms.parse;

import static org.junit.Assert.*;

import org.junit.Test;

public class BmsDeclarationParsedTest {
	// BmsDeclarationParsed()
	// 全ての値が初期値のオブジェクトが構築されること
	@Test
	public void testBmsDeclarationParsed1() {
		var p = new BmsDeclarationParsed();
		assertEquals(0, p.lineNumber);
		assertNull(p.line);
		assertNull(p.name);
		assertNull(p.value);
	}

	// BmsDeclarationParsed(int, Object, String, String)
	// 指定された値が初期値のオブジェクトが構築されること
	@Test
	public void testBmsDeclarationParsed2() {
		var p = new BmsDeclarationParsed(100, "line", "myValue", "hoge");
		assertEquals(100, p.lineNumber);
		assertEquals("line", p.line);
		assertEquals("myValue", p.name);
		assertEquals("hoge", p.value);
	}

	// set(int, Object, String, String)
	// 指定された値が設定されること
	@Test
	public void testSet() {
		var p = new BmsDeclarationParsed();
		assertSame(p, p.set(100, "line", "myValue", "hoge"));
		assertEquals(100, p.lineNumber);
		assertEquals("line", p.line);
		assertEquals("myValue", p.name);
		assertEquals("hoge", p.value);
	}
}
