package com.lmt.lib.bms.bemusic;

import static org.junit.Assert.*;

import org.junit.Test;

public class BeMusicLongNoteModeTest {
	// getNativeValue()
	// 期待するネイティブ値が取得できること
	@Test
	public void testGetNativeValue() {
		assertEquals(1L, BeMusicLongNoteMode.LN.getNativeValue());
		assertEquals(2L, BeMusicLongNoteMode.CN.getNativeValue());
		assertEquals(3L, BeMusicLongNoteMode.HCN.getNativeValue());
	}

	// getTailType()
	// 期待するロングノート終端のノート種別が取得できること
	@Test
	public void testGetTailType() {
		assertEquals(BeMusicNoteType.LONG_OFF, BeMusicLongNoteMode.LN.getTailType());
		assertEquals(BeMusicNoteType.CHARGE_OFF, BeMusicLongNoteMode.CN.getTailType());
		assertEquals(BeMusicNoteType.CHARGE_OFF, BeMusicLongNoteMode.HCN.getTailType());
	}

	// isCountTail()
	// LN以外はロングノート終端がカウントされること
	@Test
	public void testIsCountTail() {
		assertFalse(BeMusicLongNoteMode.LN.isCountTail());
		assertTrue(BeMusicLongNoteMode.CN.isCountTail());
		assertTrue(BeMusicLongNoteMode.HCN.isCountTail());
	}

	// isJudgeRegularly()
	// HCNのみが定期的な押下判定を持つこと
	@Test
	public void testIsJudgeRegularly() {
		assertFalse(BeMusicLongNoteMode.LN.isJudgeRegularly());
		assertFalse(BeMusicLongNoteMode.CN.isJudgeRegularly());
		assertTrue(BeMusicLongNoteMode.HCN.isJudgeRegularly());
	}

	// fromNative(long)
	// ネイティブ値に対応したロングノートモードが取得できること
	@Test
	public void testFromNative_Normal() {
		assertEquals(BeMusicLongNoteMode.LN, BeMusicLongNoteMode.fromNative(1L));
		assertEquals(BeMusicLongNoteMode.CN, BeMusicLongNoteMode.fromNative(2L));
		assertEquals(BeMusicLongNoteMode.HCN, BeMusicLongNoteMode.fromNative(3L));
	}

	// fromNative(long)
	// 不明なネイティブ値を指定した場合、LNが返ること
	@Test
	public void testFromNative_UnknownNative() {
		assertEquals(BeMusicLongNoteMode.LN, BeMusicLongNoteMode.fromNative(-1L));
		assertEquals(BeMusicLongNoteMode.LN, BeMusicLongNoteMode.fromNative(Long.MAX_VALUE));
	}
}
