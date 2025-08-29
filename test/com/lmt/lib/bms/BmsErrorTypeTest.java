package com.lmt.lib.bms;

import static org.junit.Assert.*;

import org.junit.Test;

public class BmsErrorTypeTest {
	// isScriptError()
	// 正常：想定する値が返ること
	@Test
	public void testIsScriptError() {
		assertTrue(BmsErrorType.SYNTAX.isScriptError());
		assertTrue(BmsErrorType.TEST_DECLARATION.isScriptError());
		assertTrue(BmsErrorType.TEST_META.isScriptError());
		assertTrue(BmsErrorType.TEST_CHANNEL.isScriptError());
		assertTrue(BmsErrorType.TEST_CONTENT.isScriptError());
		assertTrue(BmsErrorType.UNKNOWN_META.isScriptError());
		assertTrue(BmsErrorType.UNKNOWN_CHANNEL.isScriptError());
		assertTrue(BmsErrorType.WRONG_DATA.isScriptError());
		assertTrue(BmsErrorType.REDEFINE.isScriptError());
		assertTrue(BmsErrorType.SPEC_VIOLATION.isScriptError());
		assertTrue(BmsErrorType.COMMENT_NOT_CLOSED.isScriptError());
		assertFalse(BmsErrorType.COMMON.isScriptError());
	}

	// isCommonError()
	// 正常：想定する値が返ること
	@Test
	public void testIsCommonError() {
		assertFalse(BmsErrorType.SYNTAX.isCommonError());
		assertFalse(BmsErrorType.TEST_DECLARATION.isCommonError());
		assertFalse(BmsErrorType.TEST_META.isCommonError());
		assertFalse(BmsErrorType.TEST_CHANNEL.isCommonError());
		assertFalse(BmsErrorType.TEST_CONTENT.isCommonError());
		assertFalse(BmsErrorType.UNKNOWN_META.isCommonError());
		assertFalse(BmsErrorType.UNKNOWN_CHANNEL.isCommonError());
		assertFalse(BmsErrorType.WRONG_DATA.isCommonError());
		assertFalse(BmsErrorType.REDEFINE.isCommonError());
		assertFalse(BmsErrorType.SPEC_VIOLATION.isCommonError());
		assertFalse(BmsErrorType.COMMENT_NOT_CLOSED.isCommonError());
		assertTrue(BmsErrorType.COMMON.isCommonError());
	}

}
