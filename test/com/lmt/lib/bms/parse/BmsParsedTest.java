package com.lmt.lib.bms.parse;

import static org.junit.Assert.*;

import org.junit.Test;

public class BmsParsedTest {
	// getType()
	// 各解析済み要素オブジェクトで期待する値が返ること
	@Test
	public void testBmsParsed_GetType() {
		assertEquals(BmsParsedType.DECLARATION, new BmsDeclarationParsed().getType());
		assertEquals(BmsParsedType.META, new BmsMetaParsed().getType());
		assertEquals(BmsParsedType.MEASURE_VALUE, new BmsMeasureValueParsed().getType());
		assertEquals(BmsParsedType.NOTE, new BmsNoteParsed().getType());
		assertEquals(BmsParsedType.ERROR, new BmsErrorParsed().getType());
	}

	// isErrorType()
	// 各解析済み要素オブジェクトで期待する値が返ること
	@Test
	public void testBmsParsed_IsErrorType() {
		assertFalse(new BmsDeclarationParsed().isErrorType());
		assertFalse(new BmsMetaParsed().isErrorType());
		assertFalse(new BmsMeasureValueParsed().isErrorType());
		assertFalse(new BmsNoteParsed().isErrorType());
		assertTrue(new BmsErrorParsed().isErrorType());
	}
}
