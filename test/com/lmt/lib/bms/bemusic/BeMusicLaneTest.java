package com.lmt.lib.bms.bemusic;

import static org.junit.Assert.*;

import org.junit.Test;

public class BeMusicLaneTest {
	// getIndex()
	// 正常：SPレーンが0、DP右側レーンが1になること
	@Test
	public void testGetIndex_Normal() {
		assertEquals(0, BeMusicLane.PRIMARY.getIndex());
		assertEquals(1, BeMusicLane.SECONDARY.getIndex());
	}

	// fromIndex(int)
	// SPレーンが0、DP右側レーンが1で取得できること
	@Test
	public void testFromIndex_Normal() {
		assertEquals(BeMusicLane.PRIMARY, BeMusicLane.fromIndex(0));
		assertEquals(BeMusicLane.SECONDARY, BeMusicLane.fromIndex(1));
	}

	// fromIndex(int)
	// IndexOutOfBoundsException indexがマイナス値またはCOUNT以上
	@Test
	public void testFromIndex_IndexOutOfRange() {
		var ec = IndexOutOfBoundsException.class;
		assertThrows(ec, () -> BeMusicLane.fromIndex(-1));
		assertThrows(ec, () -> BeMusicLane.fromIndex(BeMusicLane.COUNT));
	}
}
