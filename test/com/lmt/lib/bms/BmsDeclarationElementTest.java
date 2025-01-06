package com.lmt.lib.bms;

import static org.junit.Assert.*;

import org.junit.Test;

public class BmsDeclarationElementTest {
	// getName()
	// 設定したBMS宣言の名前が取得できること
	@Test
	public void testGetName() {
		var n = "anyName";
		var e = new BmsDeclarationElement(n, "");
		assertEquals(n, e.getName());
	}

	// getValue()
	// 設定したBMS宣言の値が取得できること
	@Test
	public void testGetValue() {
		var v = "anyValue";
		var e = new BmsDeclarationElement("", v);
		assertEquals(v, e.getValue());
	}
}
