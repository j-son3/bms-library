package com.lmt.lib.bms;

import static org.junit.Assert.*;

import org.junit.Test;

public class BmsHandleExceptionTest {
	// BmsHandleException()
	// インスタンスが正しく構築されること
	@Test
	public void testBmsHandleException1() {
		var e = new BmsHandleException();
		assertNull(e.getMessage());
		assertNull(e.getCause());
	}

	// BmsHandleException(String, Throwable)
	// インスタンスが正しく構築されること
	@Test
	public void testBmsHandleException2() {
		var cause = new RuntimeException();
		var e1 = new BmsHandleException("message", cause);
		assertEquals("message", e1.getMessage());
		assertSame(cause, e1.getCause());

		var e2 = new BmsHandleException(null, null);
		assertNull(e2.getMessage());
		assertNull(e2.getCause());
	}
}
