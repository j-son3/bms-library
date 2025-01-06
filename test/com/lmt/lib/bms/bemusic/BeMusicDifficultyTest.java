package com.lmt.lib.bms.bemusic;

import static org.junit.Assert.*;

import org.junit.Test;

public class BeMusicDifficultyTest {
	// getNativeValue()
	// 期待する実際の値が取得できること
	@Test
	public void testGetNativeValue_001() {
		assertEquals(0L, BeMusicDifficulty.OTHER.getNativeValue());
		assertEquals(1L, BeMusicDifficulty.BEGINNER.getNativeValue());
		assertEquals(2L, BeMusicDifficulty.NORMAL.getNativeValue());
		assertEquals(3L, BeMusicDifficulty.HYPER.getNativeValue());
		assertEquals(4L, BeMusicDifficulty.ANOTHER.getNativeValue());
		assertEquals(5L, BeMusicDifficulty.INSANE.getNativeValue());
	}

	// getString()
	// 期待する文字列表記が取得できること
	@Test
	public void testGetString_001() {
		assertEquals("OTHER", BeMusicDifficulty.OTHER.getString());
		assertEquals("BEGINNER", BeMusicDifficulty.BEGINNER.getString());
		assertEquals("NORMAL", BeMusicDifficulty.NORMAL.getString());
		assertEquals("HYPER", BeMusicDifficulty.HYPER.getString());
		assertEquals("ANOTHER", BeMusicDifficulty.ANOTHER.getString());
		assertEquals("INSANE", BeMusicDifficulty.INSANE.getString());
	}

	// fromNativeValue(long)
	// 0xffffffffより大きい値を指定するとOTHERになること
	@Test
	public void testFromNativeValue_001() {
		assertEquals(BeMusicDifficulty.OTHER, BeMusicDifficulty.fromNativeValue(0xffffffffL + 1L));
	}

	// fromNativeValue(long)
	// 定義済みの値が正常に取得できること
	@Test
	public void testFromNativeValue_002() {
		assertEquals(BeMusicDifficulty.OTHER, BeMusicDifficulty.fromNativeValue(0L));
		assertEquals(BeMusicDifficulty.BEGINNER, BeMusicDifficulty.fromNativeValue(1L));
		assertEquals(BeMusicDifficulty.NORMAL, BeMusicDifficulty.fromNativeValue(2L));
		assertEquals(BeMusicDifficulty.HYPER, BeMusicDifficulty.fromNativeValue(3L));
		assertEquals(BeMusicDifficulty.ANOTHER, BeMusicDifficulty.fromNativeValue(4L));
		assertEquals(BeMusicDifficulty.INSANE, BeMusicDifficulty.fromNativeValue(5L));
	}

	// fromNativeValue(long)
	// 未知の値の場合はOTHERになること
	@Test
	public void testFromNativeValue_003() {
		assertEquals(BeMusicDifficulty.OTHER, BeMusicDifficulty.fromNativeValue(10L));
	}
}
