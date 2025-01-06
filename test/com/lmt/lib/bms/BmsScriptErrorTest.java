package com.lmt.lib.bms;

import static org.junit.Assert.*;

import org.junit.Test;

public class BmsScriptErrorTest {
	// BmsScriptError(BmsErrorType, int, String, String, Throwable)
	// 正常
	@Test
	public void testBmsScriptError_001() {
		// コンストラクタで設定した値が正常に取得できること
		// テスト1
		var err = new BmsScriptError(BmsErrorType.SYNTAX, 100, "content", "", null);
		assertEquals(BmsErrorType.SYNTAX, err.getType());
		assertEquals(100, err.getLineNumber());
		assertEquals("content", err.getLine());
		// テスト2
		var ex = new Exception("Test exception");
		var err2 = new BmsScriptError(BmsErrorType.PANIC, 9999, "has_exception", "message", ex);
		assertEquals(BmsErrorType.PANIC, err2.getType());
		assertEquals(9999, err2.getLineNumber());
		assertEquals("has_exception", err2.getLine());
		assertEquals("message", err2.getMessage());
		assertSame(ex, err2.getCause());
	}

	// BmsScriptError(BmsErrorType, int, String, String, Throwable)
	// NullPointerException errTypeがnull
	@Test
	public void testBmsScriptError_002() {
		var ex = NullPointerException.class;
		assertThrows(ex, () -> new BmsScriptError(null, 1, "error", "", null));
	}

	// BmsScriptError(BmsErrorType, int, String, String, Throwable)
	// NullPointerException lineがnull
	@Test
	public void testBmsScriptError_003() {
		var ex = NullPointerException.class;
		assertThrows(ex, () -> new BmsScriptError(BmsErrorType.SYNTAX, 1, null, "", null));
	}

	// BmsScriptError(BmsErrorType, int, String, String, Throwable)
	// IllegalArgumentException lineNumberがマイナス値
	@Test
	public void testBmsScriptError_004() {
		var ex = IllegalArgumentException.class;
		assertThrows(ex, () -> new BmsScriptError(BmsErrorType.SYNTAX, -1, "error", "", null));
	}
}
