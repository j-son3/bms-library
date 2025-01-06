package com.lmt.lib.bms;

import static org.junit.Assert.*;

import org.junit.Test;

public class BmsLoadExceptionTest {
	// BmsLoadException(BmsError)
	// 正常
	@Test
	public void testBmsLoadException_001() {
		BmsLoadException e;
		var err = new BmsScriptError(BmsErrorType.SYNTAX, 10, "XX #META This line is wrong syntax", "", null);

		e = new BmsLoadException(null);
		assertEquals(null, e.getMessage());
		assertEquals(null, e.getCause());
		assertEquals(null, e.getError());

		e = new BmsLoadException(err);
		assertEquals(err.toString(), e.getMessage());
		assertEquals(null, e.getCause());
		assertEquals(err, e.getError());
	}

	// getError()
	// 正常
	@Test
	public void testGetError() {
		var err = new BmsScriptError(BmsErrorType.SYNTAX, 10, "XX #META This line is wrong syntax", "", null);
		assertEquals(err, new BmsLoadException(err).getError());
	}
}
