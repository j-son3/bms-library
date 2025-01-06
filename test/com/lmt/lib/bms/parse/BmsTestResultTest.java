package com.lmt.lib.bms.parse;

import static org.junit.Assert.*;

import org.junit.Test;

public class BmsTestResultTest {
	// 定義した内容が想定通りであること
	@Test
	public void testPredefined() {
		var ok = BmsTestResult.OK;
		assertEquals(BmsTestResult.RESULT_OK, ok.getResult());
		assertNull(ok.getMessage());
		var fail = BmsTestResult.FAIL;
		assertEquals(BmsTestResult.RESULT_FAIL, fail.getResult());
		assertNull(fail.getMessage());
		var through = BmsTestResult.THROUGH;
		assertEquals(BmsTestResult.RESULT_THROUGH, through.getResult());
		assertNull(through.getMessage());
	}

	// fail(String)
	// 期待するオブジェクトが構築されること
	@Test
	public void testFail() {
		var f1 = BmsTestResult.fail(null);
		assertEquals(BmsTestResult.RESULT_FAIL, f1.getResult());
		assertNull(f1.getMessage());
		var f2 = BmsTestResult.fail("message");
		assertEquals(BmsTestResult.RESULT_FAIL, f2.getResult());
		assertEquals("message", f2.getMessage());
	}
}
