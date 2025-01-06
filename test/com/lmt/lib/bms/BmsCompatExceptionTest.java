package com.lmt.lib.bms;

import static org.junit.Assert.*;

import org.junit.Test;

public class BmsCompatExceptionTest {
	// BmsCompatException()
	// 例外オブジェクトのインスタンスを生成できること
	@Test
	public void testBmsCompatException() {
		var e = new BmsCompatException();
		assertTrue(BmsException.class.isInstance(e));
		assertNull(e.getMessage());
	}

	// BmsCompatException(String)
	// 指定したメッセージを持つ例外オブジェクトのインスタンスを生成できること
	@Test
	public void testBmsCompatExceptionString_Normal() {
		var msg = "Exception Message!";
		var e = new BmsCompatException(msg);
		assertEquals(msg, e.getMessage());
	}

	// BmsCompatException(String)
	// メッセージにはnullを指定できること
	@Test
	public void testBmsCompatExceptionString_NullMessage() {
		var e = new BmsCompatException(null);
		assertNull(e.getMessage());
	}

	// BmsCompatException(String, Throwable)
	// 指定したメッセージ、原因を持つ例外オブジェクトのインスタンスを生成できること
	@Test
	public void testBmsCompatExceptionStringThrowable_Normal() {
		var msg = "Exception Message!";
		var cause = new NullPointerException();
		var e = new BmsCompatException(msg, cause);
		assertEquals(msg, e.getMessage());
		assertEquals(cause.getClass(), e.getCause().getClass());
	}

	// BmsCompatException(String, Throwable)
	// メッセージにはnullを指定できること
	@Test
	public void testBmsCompatExceptionStringThrowable_NullMessage() {
		var e = new BmsCompatException(null, new RuntimeException());
		assertNull(e.getMessage());
	}

	// BmsCompatException(String, Throwable)
	// 原因にはnullを指定できること
	@Test
	public void testBmsCompatExceptionStringThrowable_NullCause() {
		var e = new BmsCompatException("msg", null);
		assertNull(e.getCause());
	}
}
