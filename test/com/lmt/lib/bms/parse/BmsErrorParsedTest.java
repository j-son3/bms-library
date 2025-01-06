package com.lmt.lib.bms.parse;

import static org.junit.Assert.*;

import org.junit.Test;

import com.lmt.lib.bms.BmsErrorType;
import com.lmt.lib.bms.BmsScriptError;

public class BmsErrorParsedTest {
	// BmsErrorParsed()
	// 想定通りの構成でオブジェクトが生成されること
	@Test
	public void testBmsErrorParsed1_Normal() {
		var p = new BmsErrorParsed();
		assertEquals(BmsParsedType.ERROR, p.causeType);
		assertNull(p.error);
	}

	// BmsErrorParsed(BmsScriptError)
	// 指定したエラー情報でオブジェクトが生成されること
	@Test
	public void testBmsErrorParsed2_Normal() {
		var err = new BmsScriptError(BmsErrorType.PANIC, 0, "", "msg", null);
		var p = new BmsErrorParsed(err);
		assertEquals(BmsParsedType.ERROR, p.causeType);
		assertSame(err, p.error);
	}

	// BmsErrorParsed(BmsParsedType, BmsScriptError)
	// 指定した内容でオブジェクトが生成されること
	@Test
	public void testBmsErrorParsed3_Normal() {
		var type = BmsParsedType.NOTE;
		var err = new BmsScriptError(BmsErrorType.PANIC, 0, "", "msg", null);
		var p = new BmsErrorParsed(type, err);
		assertEquals(type, p.causeType);
		assertSame(err, p.error);
	}

	// set(BmsParsedType, BmsScriptError)
	// おbジェクトの内容が指定した内容に設定されること
	@Test
	public void testSet_Normal() {
		var type = BmsParsedType.META;
		var err = new BmsScriptError(BmsErrorType.PANIC, 0, "", "msg", null);
		var p = new BmsErrorParsed();
		p.set(type, err);
		assertEquals(type, p.causeType);
		assertSame(err, p.error);
		p.set(null, null);
		assertNull(p.causeType);
		assertNull(p.error);
	}

	// isOk()
	// エラー情報が設定されていればfalseが返ること
	@Test
	public void testIsOk_HasError() {
		var err = new BmsScriptError(BmsErrorType.PANIC, 0, "", "msg", null);
		var p = new BmsErrorParsed(err);
		assertFalse(p.isOk());
	}

	// isOk()
	// エラー情報が未設定(null)ならばtrueが返ること
	@Test
	public void testIsOk_NoError() {
		var p = new BmsErrorParsed(null);
		assertTrue(p.isOk());
	}

	// isFail()
	// エラー情報が設定されていればtrueが返ること
	@Test
	public void testIsFail_HasError() {
		var err = new BmsScriptError(BmsErrorType.PANIC, 0, "", "msg", null);
		var p = new BmsErrorParsed(err);
		assertTrue(p.isFail());
	}

	// isFail()
	// エラー情報が未設定(null)ならばfalseが返ること
	@Test
	public void testIsFail_NoError() {
		var p = new BmsErrorParsed(null);
		assertFalse(p.isFail());
	}
}
