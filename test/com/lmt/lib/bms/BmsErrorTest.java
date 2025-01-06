package com.lmt.lib.bms;

import static org.junit.Assert.*;

import org.junit.Test;

public class BmsErrorTest {
	// toString()
	// 正常：エラーメッセージと同じ値が返ること
	@Test
	public void testToString_Normal() {
		var msg = "msg";
		var err = new BmsError(msg);
		assertEquals(msg, err.toString());
	}

	// toString()
	// 正常：エラーメッセージがnullの場合、空文字列が返ること
	@Test
	public void testToString_NullMessage() {
		var err = new BmsError(null);
		assertEquals("", err.toString());
	}

	// getType()
	// 正常：このクラスのコンストラクタで生成されたエラー情報は汎用エラーであること
	@Test
	public void testGetType() {
		var err1 = new BmsError("msg");
		assertTrue(err1.getType().isCommonError());
		var err2 = new BmsError("msg", new Exception());
		assertTrue(err2.getType().isCommonError());
	}

	// getMessage()
	// 正常：コンストラクタで指定したメッセージが返ること
	@Test
	public void testGetMessage_Normal() {
		var msg = "msg";
		var err1 = new BmsError(msg);
		assertEquals(msg, err1.getMessage());
		var err2 = new BmsError(msg, new Exception());
		assertEquals(msg, err2.getMessage());
	}

	// getMessage()
	// 正常：nullを指定した場合nullが返ること
	@Test
	public void testGetMessage_NullMessage() {
		var err1 = new BmsError(null);
		assertNull(err1.getMessage());
		var err2 = new BmsError(null, new Exception());
		assertNull(err2.getMessage());
	}

	// getCause()
	// 正常：コンストラクタで指定した例外が返ること
	@Test
	public void testGetCause_Normal() {
		var e = new RuntimeException();
		var err = new BmsError(null, e);
		assertSame(e, err.getCause());
	}

	// getCause()
	// 正常：nullを指定した場合nullが返ること
	@Test
	public void testGetCause_NullCause() {
		var err = new BmsError("", null);
		assertNull(err.getCause());
	}
}
