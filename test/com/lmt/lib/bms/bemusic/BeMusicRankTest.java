package com.lmt.lib.bms.bemusic;

import static org.junit.Assert.*;

import org.junit.Test;

public class BeMusicRankTest {
	// getNativeValue()
	// 期待する実際の値が取得できること
	@Test
	public void testGetNativeValue_001() {
		assertEquals(-1L, BeMusicRank.OTHER.getNativeValue());
		assertEquals(0L, BeMusicRank.VERY_HARD.getNativeValue());
		assertEquals(1L, BeMusicRank.HARD.getNativeValue());
		assertEquals(2L, BeMusicRank.NORMAL.getNativeValue());
		assertEquals(3L, BeMusicRank.EASY.getNativeValue());
		assertEquals(4L, BeMusicRank.VERY_EASY.getNativeValue());
	}

	// getString()
	// 期待する文字列表記が取得できること
	@Test
	public void testGetString_001() {
		assertEquals("OTHER", BeMusicRank.OTHER.getString());
		assertEquals("VERY HARD", BeMusicRank.VERY_HARD.getString());
		assertEquals("HARD", BeMusicRank.HARD.getString());
		assertEquals("NORMAL", BeMusicRank.NORMAL.getString());
		assertEquals("EASY", BeMusicRank.EASY.getString());
		assertEquals("VERY EASY", BeMusicRank.VERY_EASY.getString());
	}

	// fromNativeValue(long)
	// 0xffffffffより大きい値を指定するとOTHERになること
	@Test
	public void testFromNativeValue_001() {
		assertEquals(BeMusicRank.OTHER, BeMusicRank.fromNativeValue(0xffffffffL + 1L));
	}

	// fromNativeValue(long)
	// 定義済みの値が正常に取得できること
	@Test
	public void testFromNativeValue_002() {
		assertEquals(BeMusicRank.OTHER, BeMusicRank.fromNativeValue(-1L));
		assertEquals(BeMusicRank.VERY_HARD, BeMusicRank.fromNativeValue(0L));
		assertEquals(BeMusicRank.HARD, BeMusicRank.fromNativeValue(1L));
		assertEquals(BeMusicRank.NORMAL, BeMusicRank.fromNativeValue(2L));
		assertEquals(BeMusicRank.EASY, BeMusicRank.fromNativeValue(3L));
		assertEquals(BeMusicRank.VERY_EASY, BeMusicRank.fromNativeValue(4L));
	}

	// fromNativeValue(long)
	// 未知の値の場合はOTHERになること
	@Test
	public void testFromNativeValue_003() {
		assertEquals(BeMusicRank.OTHER, BeMusicRank.fromNativeValue(10L));
	}
}
