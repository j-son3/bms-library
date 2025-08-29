package com.lmt.lib.bms;

import static org.junit.Assert.*;

import org.junit.Test;

public class BmsPanicExceptionTest {
	// BmsPanicException()
	// インスタンスが正しく構築されること
	@Test
	public void testBmsPanicException1() {
		var e = new BmsPanicException();
		assertNull(e.getMessage());
		assertNull(e.getCause());
	}

	// BmsPanicException(String, Throwable)
	// インスタンスが正しく構築されること
	@Test
	public void testBmsPanicException2() {
		var cause = new RuntimeException();
		var e1 = new BmsPanicException("message", cause);
		assertEquals("message", e1.getMessage());
		assertSame(cause, e1.getCause());

		var e2 = new BmsPanicException(null, null);
		assertNull(e2.getMessage());
		assertNull(e2.getCause());
	}
}
