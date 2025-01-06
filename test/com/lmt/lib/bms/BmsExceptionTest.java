package com.lmt.lib.bms;

import static org.junit.Assert.*;

import org.junit.Test;

public class BmsExceptionTest {
	// BmsException()
	// 正常
	@Test
	public void testBmsException1_001() {
		var e = new BmsException();
		assertEquals(null, e.getMessage());
		assertEquals(null, e.getCause());
	}

	// BmsException(String)
	// 正常
	@Test
	public void testBmsException2_001() {
		BmsException e;

		e = new BmsException((String)null);
		assertEquals(null, e.getMessage());
		assertEquals(null, e.getCause());

		e = new BmsException("hoge");
		assertEquals("hoge", e.getMessage());
		assertEquals(null, e.getCause());
	}

	// BmsException(String, Throwable)
	// 正常
	@Test
	public void testBmsException3_001() {
		BmsException e;

		e = new BmsException(null, null);
		assertEquals(null, e.getMessage());
		assertEquals(null, e.getCause());

		e = new BmsException("hoge", null);
		assertEquals("hoge", e.getMessage());
		assertEquals(null, e.getCause());

		Exception ec = new Exception();
		e = new BmsException(null, ec);
		assertEquals(null, e.getMessage());
		assertEquals(ec, e.getCause());
	}

	// BmsException(Throwable)
	// 正常
	@Test
	public void testBmsException4_001() {
		BmsException e;

		var ec = new Exception();
		e = new BmsException(ec);
		assertEquals(ec.toString(), e.getMessage());
		assertEquals(ec, e.getCause());
	}
}
