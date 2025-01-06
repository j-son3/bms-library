package com.lmt.lib.bms.bemusic;

import static org.junit.Assert.*;

import org.junit.Test;

public class BeMusicPlayerTest {
	// getNativeValue()
	// 期待する実際の値が取得できること
	@Test
	public void testGetNativeValue_001() {
		assertEquals(0L, BeMusicPlayer.OTHER.getNativeValue());
		assertEquals(1L, BeMusicPlayer.SINGLE.getNativeValue());
		assertEquals(2L, BeMusicPlayer.COUPLE.getNativeValue());
		assertEquals(3L, BeMusicPlayer.DOUBLE.getNativeValue());
		assertEquals(4L, BeMusicPlayer.BATTLE.getNativeValue());
	}

	// getString()
	// 期待する文字列表記が取得できること
	@Test
	public void testGetString_001() {
		assertEquals("OTHER", BeMusicPlayer.OTHER.getString());
		assertEquals("SINGLE", BeMusicPlayer.SINGLE.getString());
		assertEquals("COUPLE", BeMusicPlayer.COUPLE.getString());
		assertEquals("DOUBLE", BeMusicPlayer.DOUBLE.getString());
		assertEquals("BATTLE", BeMusicPlayer.BATTLE.getString());
	}

	// fromNativeValue(long)
	// 0xffffffffより大きい値を指定するとOTHERになること
	@Test
	public void testFromNativeValue_001() {
		assertEquals(BeMusicPlayer.OTHER, BeMusicPlayer.fromNativeValue(0xffffffffL + 1L));
	}

	// fromNativeValue(long)
	// 定義済みの値が正常に取得できること
	@Test
	public void testFromNativeValue_002() {
		assertEquals(BeMusicPlayer.OTHER, BeMusicPlayer.fromNativeValue(0L));
		assertEquals(BeMusicPlayer.SINGLE, BeMusicPlayer.fromNativeValue(1L));
		assertEquals(BeMusicPlayer.COUPLE, BeMusicPlayer.fromNativeValue(2L));
		assertEquals(BeMusicPlayer.DOUBLE, BeMusicPlayer.fromNativeValue(3L));
		assertEquals(BeMusicPlayer.BATTLE, BeMusicPlayer.fromNativeValue(4L));
	}

	// fromNativeValue(long)
	// 未知の値の場合はOTHERになること
	@Test
	public void testFromNativeValue_003() {
		assertEquals(BeMusicPlayer.OTHER, BeMusicPlayer.fromNativeValue(10L));
	}
}
