package com.lmt.lib.bms.bemusic;

import static org.junit.Assert.*;

import org.junit.Test;

import com.lmt.lib.bms.BmsChannel;
import com.lmt.lib.bms.BmsSpec;

public class BeMusicChannelTest {
	// visibles(BeMusicLane)
	// 主レーンの可視オブジェリストが取得できること
	@Test
	public void testVisibles_Primary() {
		var chs = BeMusicChannel.visibles(BeMusicLane.PRIMARY);
		assertSame(BeMusicChannel.VISIBLES_PRIMARY, chs);
		assertTrue(chs.contains(BeMusicChannel.VISIBLE_1P_01));
		assertTrue(chs.contains(BeMusicChannel.VISIBLE_1P_02));
		assertTrue(chs.contains(BeMusicChannel.VISIBLE_1P_03));
		assertTrue(chs.contains(BeMusicChannel.VISIBLE_1P_04));
		assertTrue(chs.contains(BeMusicChannel.VISIBLE_1P_05));
		assertTrue(chs.contains(BeMusicChannel.VISIBLE_1P_06));
		assertTrue(chs.contains(BeMusicChannel.VISIBLE_1P_07));
		assertTrue(chs.contains(BeMusicChannel.VISIBLE_1P_08));
		assertTrue(chs.contains(BeMusicChannel.VISIBLE_1P_09));
		assertTrue(chs.contains(BeMusicChannel.VISIBLE_1P_10));
		assertTrue(chs.contains(BeMusicChannel.VISIBLE_1P_11));
		assertTrue(chs.contains(BeMusicChannel.VISIBLE_1P_12));
		assertTrue(chs.contains(BeMusicChannel.VISIBLE_1P_13));
		assertTrue(chs.contains(BeMusicChannel.VISIBLE_1P_14));
		assertTrue(chs.contains(BeMusicChannel.VISIBLE_1P_15));
		assertTrue(chs.contains(BeMusicChannel.VISIBLE_1P_16));
		assertTrue(chs.contains(BeMusicChannel.VISIBLE_1P_17));
		assertTrue(chs.contains(BeMusicChannel.VISIBLE_1P_18));
		assertTrue(chs.contains(BeMusicChannel.VISIBLE_1P_19));
		assertTrue(chs.contains(BeMusicChannel.VISIBLE_1P_20));
		assertTrue(chs.contains(BeMusicChannel.VISIBLE_1P_21));
		assertTrue(chs.contains(BeMusicChannel.VISIBLE_1P_22));
		assertTrue(chs.contains(BeMusicChannel.VISIBLE_1P_23));
		assertTrue(chs.contains(BeMusicChannel.VISIBLE_1P_24));
		assertTrue(chs.contains(BeMusicChannel.VISIBLE_1P_25));
		assertTrue(chs.contains(BeMusicChannel.VISIBLE_1P_26));
		assertTrue(chs.contains(BeMusicChannel.VISIBLE_1P_27));
		assertTrue(chs.contains(BeMusicChannel.VISIBLE_1P_28));
		assertTrue(chs.contains(BeMusicChannel.VISIBLE_1P_29));
		assertTrue(chs.contains(BeMusicChannel.VISIBLE_1P_30));
		assertTrue(chs.contains(BeMusicChannel.VISIBLE_1P_31));
		assertTrue(chs.contains(BeMusicChannel.VISIBLE_1P_32));
		assertTrue(chs.contains(BeMusicChannel.VISIBLE_1P_33));
		assertTrue(chs.contains(BeMusicChannel.VISIBLE_1P_34));
		assertTrue(chs.contains(BeMusicChannel.VISIBLE_1P_35));
	}

	// visibles(BeMusicLane)
	// 副レーンの可視オブジェリストが取得できること
	@Test
	public void testVisibles_Secondary() {
		var chs = BeMusicChannel.visibles(BeMusicLane.SECONDARY);
		assertSame(BeMusicChannel.VISIBLES_SECONDARY, chs);
		assertTrue(chs.contains(BeMusicChannel.VISIBLE_2P_01));
		assertTrue(chs.contains(BeMusicChannel.VISIBLE_2P_02));
		assertTrue(chs.contains(BeMusicChannel.VISIBLE_2P_03));
		assertTrue(chs.contains(BeMusicChannel.VISIBLE_2P_04));
		assertTrue(chs.contains(BeMusicChannel.VISIBLE_2P_05));
		assertTrue(chs.contains(BeMusicChannel.VISIBLE_2P_06));
		assertTrue(chs.contains(BeMusicChannel.VISIBLE_2P_07));
		assertTrue(chs.contains(BeMusicChannel.VISIBLE_2P_08));
		assertTrue(chs.contains(BeMusicChannel.VISIBLE_2P_09));
		assertTrue(chs.contains(BeMusicChannel.VISIBLE_2P_10));
		assertTrue(chs.contains(BeMusicChannel.VISIBLE_2P_11));
		assertTrue(chs.contains(BeMusicChannel.VISIBLE_2P_12));
		assertTrue(chs.contains(BeMusicChannel.VISIBLE_2P_13));
		assertTrue(chs.contains(BeMusicChannel.VISIBLE_2P_14));
		assertTrue(chs.contains(BeMusicChannel.VISIBLE_2P_15));
		assertTrue(chs.contains(BeMusicChannel.VISIBLE_2P_16));
		assertTrue(chs.contains(BeMusicChannel.VISIBLE_2P_17));
		assertTrue(chs.contains(BeMusicChannel.VISIBLE_2P_18));
		assertTrue(chs.contains(BeMusicChannel.VISIBLE_2P_19));
		assertTrue(chs.contains(BeMusicChannel.VISIBLE_2P_20));
		assertTrue(chs.contains(BeMusicChannel.VISIBLE_2P_21));
		assertTrue(chs.contains(BeMusicChannel.VISIBLE_2P_22));
		assertTrue(chs.contains(BeMusicChannel.VISIBLE_2P_23));
		assertTrue(chs.contains(BeMusicChannel.VISIBLE_2P_24));
		assertTrue(chs.contains(BeMusicChannel.VISIBLE_2P_25));
		assertTrue(chs.contains(BeMusicChannel.VISIBLE_2P_26));
		assertTrue(chs.contains(BeMusicChannel.VISIBLE_2P_27));
		assertTrue(chs.contains(BeMusicChannel.VISIBLE_2P_28));
		assertTrue(chs.contains(BeMusicChannel.VISIBLE_2P_29));
		assertTrue(chs.contains(BeMusicChannel.VISIBLE_2P_30));
		assertTrue(chs.contains(BeMusicChannel.VISIBLE_2P_31));
		assertTrue(chs.contains(BeMusicChannel.VISIBLE_2P_32));
		assertTrue(chs.contains(BeMusicChannel.VISIBLE_2P_33));
		assertTrue(chs.contains(BeMusicChannel.VISIBLE_2P_34));
		assertTrue(chs.contains(BeMusicChannel.VISIBLE_2P_35));
	}

	// visibles(BeMusicLane)
	// 取得したリストは読み取り専用であること
	@Test
	public void testVisibles_ReadOnly() {
		var ex = UnsupportedOperationException.class;
		var primary = BeMusicChannel.visibles(BeMusicLane.PRIMARY);
		assertThrows(ex, () -> primary.add(BmsChannel.object(BmsSpec.USER_CHANNEL_MIN)));
		var secondary = BeMusicChannel.visibles(BeMusicLane.SECONDARY);
		assertThrows(ex, () -> secondary.add(BmsChannel.object(BmsSpec.USER_CHANNEL_MIN)));
	}

	// visibles(BeMusicLane)
	// NullPointerException laneがnull
	@Test
	public void testVisibles_NullLane() {
		assertThrows(NullPointerException.class, () -> BeMusicChannel.visibles(null));
	}

	// invisibles(BeMusicLane)
	// 主レーンの不可視オブジェリストが取得できること
	@Test
	public void testInvisibles_Primary() {
		var chs = BeMusicChannel.invisibles(BeMusicLane.PRIMARY);
		assertSame(BeMusicChannel.INVISIBLES_PRIMARY, chs);
		assertTrue(chs.contains(BeMusicChannel.INVISIBLE_1P_01));
		assertTrue(chs.contains(BeMusicChannel.INVISIBLE_1P_02));
		assertTrue(chs.contains(BeMusicChannel.INVISIBLE_1P_03));
		assertTrue(chs.contains(BeMusicChannel.INVISIBLE_1P_04));
		assertTrue(chs.contains(BeMusicChannel.INVISIBLE_1P_05));
		assertTrue(chs.contains(BeMusicChannel.INVISIBLE_1P_06));
		assertTrue(chs.contains(BeMusicChannel.INVISIBLE_1P_07));
		assertTrue(chs.contains(BeMusicChannel.INVISIBLE_1P_08));
		assertTrue(chs.contains(BeMusicChannel.INVISIBLE_1P_09));
		assertTrue(chs.contains(BeMusicChannel.INVISIBLE_1P_10));
		assertTrue(chs.contains(BeMusicChannel.INVISIBLE_1P_11));
		assertTrue(chs.contains(BeMusicChannel.INVISIBLE_1P_12));
		assertTrue(chs.contains(BeMusicChannel.INVISIBLE_1P_13));
		assertTrue(chs.contains(BeMusicChannel.INVISIBLE_1P_14));
		assertTrue(chs.contains(BeMusicChannel.INVISIBLE_1P_15));
		assertTrue(chs.contains(BeMusicChannel.INVISIBLE_1P_16));
		assertTrue(chs.contains(BeMusicChannel.INVISIBLE_1P_17));
		assertTrue(chs.contains(BeMusicChannel.INVISIBLE_1P_18));
		assertTrue(chs.contains(BeMusicChannel.INVISIBLE_1P_19));
		assertTrue(chs.contains(BeMusicChannel.INVISIBLE_1P_20));
		assertTrue(chs.contains(BeMusicChannel.INVISIBLE_1P_21));
		assertTrue(chs.contains(BeMusicChannel.INVISIBLE_1P_22));
		assertTrue(chs.contains(BeMusicChannel.INVISIBLE_1P_23));
		assertTrue(chs.contains(BeMusicChannel.INVISIBLE_1P_24));
		assertTrue(chs.contains(BeMusicChannel.INVISIBLE_1P_25));
		assertTrue(chs.contains(BeMusicChannel.INVISIBLE_1P_26));
		assertTrue(chs.contains(BeMusicChannel.INVISIBLE_1P_27));
		assertTrue(chs.contains(BeMusicChannel.INVISIBLE_1P_28));
		assertTrue(chs.contains(BeMusicChannel.INVISIBLE_1P_29));
		assertTrue(chs.contains(BeMusicChannel.INVISIBLE_1P_30));
		assertTrue(chs.contains(BeMusicChannel.INVISIBLE_1P_31));
		assertTrue(chs.contains(BeMusicChannel.INVISIBLE_1P_32));
		assertTrue(chs.contains(BeMusicChannel.INVISIBLE_1P_33));
		assertTrue(chs.contains(BeMusicChannel.INVISIBLE_1P_34));
		assertTrue(chs.contains(BeMusicChannel.INVISIBLE_1P_35));
	}

	// invisibles(BeMusicLane)
	// 副レーンの不可視オブジェリストが取得できること
	@Test
	public void testInvisibles_Secondary() {
		var chs = BeMusicChannel.invisibles(BeMusicLane.SECONDARY);
		assertSame(BeMusicChannel.INVISIBLES_SECONDARY, chs);
		assertTrue(chs.contains(BeMusicChannel.INVISIBLE_2P_01));
		assertTrue(chs.contains(BeMusicChannel.INVISIBLE_2P_02));
		assertTrue(chs.contains(BeMusicChannel.INVISIBLE_2P_03));
		assertTrue(chs.contains(BeMusicChannel.INVISIBLE_2P_04));
		assertTrue(chs.contains(BeMusicChannel.INVISIBLE_2P_05));
		assertTrue(chs.contains(BeMusicChannel.INVISIBLE_2P_06));
		assertTrue(chs.contains(BeMusicChannel.INVISIBLE_2P_07));
		assertTrue(chs.contains(BeMusicChannel.INVISIBLE_2P_08));
		assertTrue(chs.contains(BeMusicChannel.INVISIBLE_2P_09));
		assertTrue(chs.contains(BeMusicChannel.INVISIBLE_2P_10));
		assertTrue(chs.contains(BeMusicChannel.INVISIBLE_2P_11));
		assertTrue(chs.contains(BeMusicChannel.INVISIBLE_2P_12));
		assertTrue(chs.contains(BeMusicChannel.INVISIBLE_2P_13));
		assertTrue(chs.contains(BeMusicChannel.INVISIBLE_2P_14));
		assertTrue(chs.contains(BeMusicChannel.INVISIBLE_2P_15));
		assertTrue(chs.contains(BeMusicChannel.INVISIBLE_2P_16));
		assertTrue(chs.contains(BeMusicChannel.INVISIBLE_2P_17));
		assertTrue(chs.contains(BeMusicChannel.INVISIBLE_2P_18));
		assertTrue(chs.contains(BeMusicChannel.INVISIBLE_2P_19));
		assertTrue(chs.contains(BeMusicChannel.INVISIBLE_2P_20));
		assertTrue(chs.contains(BeMusicChannel.INVISIBLE_2P_21));
		assertTrue(chs.contains(BeMusicChannel.INVISIBLE_2P_22));
		assertTrue(chs.contains(BeMusicChannel.INVISIBLE_2P_23));
		assertTrue(chs.contains(BeMusicChannel.INVISIBLE_2P_24));
		assertTrue(chs.contains(BeMusicChannel.INVISIBLE_2P_25));
		assertTrue(chs.contains(BeMusicChannel.INVISIBLE_2P_26));
		assertTrue(chs.contains(BeMusicChannel.INVISIBLE_2P_27));
		assertTrue(chs.contains(BeMusicChannel.INVISIBLE_2P_28));
		assertTrue(chs.contains(BeMusicChannel.INVISIBLE_2P_29));
		assertTrue(chs.contains(BeMusicChannel.INVISIBLE_2P_30));
		assertTrue(chs.contains(BeMusicChannel.INVISIBLE_2P_31));
		assertTrue(chs.contains(BeMusicChannel.INVISIBLE_2P_32));
		assertTrue(chs.contains(BeMusicChannel.INVISIBLE_2P_33));
		assertTrue(chs.contains(BeMusicChannel.INVISIBLE_2P_34));
		assertTrue(chs.contains(BeMusicChannel.INVISIBLE_2P_35));
	}

	// invisibles(BeMusicLane)
	// 取得したリストは読み取り専用であること
	@Test
	public void testInvisibles_ReadOnly() {
		var ex = UnsupportedOperationException.class;
		var primary = BeMusicChannel.invisibles(BeMusicLane.PRIMARY);
		assertThrows(ex, () -> primary.add(BmsChannel.object(BmsSpec.USER_CHANNEL_MIN)));
		var secondary = BeMusicChannel.invisibles(BeMusicLane.SECONDARY);
		assertThrows(ex, () -> secondary.add(BmsChannel.object(BmsSpec.USER_CHANNEL_MIN)));
	}

	// invisibles(BeMusicLane)
	// NullPointerException laneがnull
	@Test
	public void testInvisibles_NullLane() {
		assertThrows(NullPointerException.class, () -> BeMusicChannel.invisibles(null));
	}

	// longs(BeMusicLane)
	// 主レーンのロングノートオブジェリストが取得できること
	@Test
	public void testLongs_Primary() {
		var chs = BeMusicChannel.longs(BeMusicLane.PRIMARY);
		assertSame(BeMusicChannel.LONGS_PRIMARY, chs);
		assertTrue(chs.contains(BeMusicChannel.LONG_1P_01));
		assertTrue(chs.contains(BeMusicChannel.LONG_1P_02));
		assertTrue(chs.contains(BeMusicChannel.LONG_1P_03));
		assertTrue(chs.contains(BeMusicChannel.LONG_1P_04));
		assertTrue(chs.contains(BeMusicChannel.LONG_1P_05));
		assertTrue(chs.contains(BeMusicChannel.LONG_1P_06));
		assertTrue(chs.contains(BeMusicChannel.LONG_1P_07));
		assertTrue(chs.contains(BeMusicChannel.LONG_1P_08));
		assertTrue(chs.contains(BeMusicChannel.LONG_1P_09));
		assertTrue(chs.contains(BeMusicChannel.LONG_1P_10));
		assertTrue(chs.contains(BeMusicChannel.LONG_1P_11));
		assertTrue(chs.contains(BeMusicChannel.LONG_1P_12));
		assertTrue(chs.contains(BeMusicChannel.LONG_1P_13));
		assertTrue(chs.contains(BeMusicChannel.LONG_1P_14));
		assertTrue(chs.contains(BeMusicChannel.LONG_1P_15));
		assertTrue(chs.contains(BeMusicChannel.LONG_1P_16));
		assertTrue(chs.contains(BeMusicChannel.LONG_1P_17));
		assertTrue(chs.contains(BeMusicChannel.LONG_1P_18));
		assertTrue(chs.contains(BeMusicChannel.LONG_1P_19));
		assertTrue(chs.contains(BeMusicChannel.LONG_1P_20));
		assertTrue(chs.contains(BeMusicChannel.LONG_1P_21));
		assertTrue(chs.contains(BeMusicChannel.LONG_1P_22));
		assertTrue(chs.contains(BeMusicChannel.LONG_1P_23));
		assertTrue(chs.contains(BeMusicChannel.LONG_1P_24));
		assertTrue(chs.contains(BeMusicChannel.LONG_1P_25));
		assertTrue(chs.contains(BeMusicChannel.LONG_1P_26));
		assertTrue(chs.contains(BeMusicChannel.LONG_1P_27));
		assertTrue(chs.contains(BeMusicChannel.LONG_1P_28));
		assertTrue(chs.contains(BeMusicChannel.LONG_1P_29));
		assertTrue(chs.contains(BeMusicChannel.LONG_1P_30));
		assertTrue(chs.contains(BeMusicChannel.LONG_1P_31));
		assertTrue(chs.contains(BeMusicChannel.LONG_1P_32));
		assertTrue(chs.contains(BeMusicChannel.LONG_1P_33));
		assertTrue(chs.contains(BeMusicChannel.LONG_1P_34));
		assertTrue(chs.contains(BeMusicChannel.LONG_1P_35));
	}

	// longs(BeMusicLane)
	// 副レーンのロングノートオブジェリストが取得できること
	@Test
	public void testLongs_Secondary() {
		var chs = BeMusicChannel.longs(BeMusicLane.SECONDARY);
		assertSame(BeMusicChannel.LONGS_SECONDARY, chs);
		assertTrue(chs.contains(BeMusicChannel.LONG_2P_01));
		assertTrue(chs.contains(BeMusicChannel.LONG_2P_02));
		assertTrue(chs.contains(BeMusicChannel.LONG_2P_03));
		assertTrue(chs.contains(BeMusicChannel.LONG_2P_04));
		assertTrue(chs.contains(BeMusicChannel.LONG_2P_05));
		assertTrue(chs.contains(BeMusicChannel.LONG_2P_06));
		assertTrue(chs.contains(BeMusicChannel.LONG_2P_07));
		assertTrue(chs.contains(BeMusicChannel.LONG_2P_08));
		assertTrue(chs.contains(BeMusicChannel.LONG_2P_09));
		assertTrue(chs.contains(BeMusicChannel.LONG_2P_10));
		assertTrue(chs.contains(BeMusicChannel.LONG_2P_11));
		assertTrue(chs.contains(BeMusicChannel.LONG_2P_12));
		assertTrue(chs.contains(BeMusicChannel.LONG_2P_13));
		assertTrue(chs.contains(BeMusicChannel.LONG_2P_14));
		assertTrue(chs.contains(BeMusicChannel.LONG_2P_15));
		assertTrue(chs.contains(BeMusicChannel.LONG_2P_16));
		assertTrue(chs.contains(BeMusicChannel.LONG_2P_17));
		assertTrue(chs.contains(BeMusicChannel.LONG_2P_18));
		assertTrue(chs.contains(BeMusicChannel.LONG_2P_19));
		assertTrue(chs.contains(BeMusicChannel.LONG_2P_20));
		assertTrue(chs.contains(BeMusicChannel.LONG_2P_21));
		assertTrue(chs.contains(BeMusicChannel.LONG_2P_22));
		assertTrue(chs.contains(BeMusicChannel.LONG_2P_23));
		assertTrue(chs.contains(BeMusicChannel.LONG_2P_24));
		assertTrue(chs.contains(BeMusicChannel.LONG_2P_25));
		assertTrue(chs.contains(BeMusicChannel.LONG_2P_26));
		assertTrue(chs.contains(BeMusicChannel.LONG_2P_27));
		assertTrue(chs.contains(BeMusicChannel.LONG_2P_28));
		assertTrue(chs.contains(BeMusicChannel.LONG_2P_29));
		assertTrue(chs.contains(BeMusicChannel.LONG_2P_30));
		assertTrue(chs.contains(BeMusicChannel.LONG_2P_31));
		assertTrue(chs.contains(BeMusicChannel.LONG_2P_32));
		assertTrue(chs.contains(BeMusicChannel.LONG_2P_33));
		assertTrue(chs.contains(BeMusicChannel.LONG_2P_34));
		assertTrue(chs.contains(BeMusicChannel.LONG_2P_35));
	}

	// longs(BeMusicLane)
	// 取得したリストは読み取り専用であること
	@Test
	public void testLongs_ReadOnly() {
		var ex = UnsupportedOperationException.class;
		var primary = BeMusicChannel.longs(BeMusicLane.PRIMARY);
		assertThrows(ex, () -> primary.add(BmsChannel.object(BmsSpec.USER_CHANNEL_MIN)));
		var secondary = BeMusicChannel.longs(BeMusicLane.SECONDARY);
		assertThrows(ex, () -> secondary.add(BmsChannel.object(BmsSpec.USER_CHANNEL_MIN)));
	}

	// longs(BeMusicLane)
	// NullPointerException laneがnull
	@Test
	public void testLongs_NullLane() {
		assertThrows(NullPointerException.class, () -> BeMusicChannel.longs(null));
	}

	// mines(BeMusicLane)
	// 主レーンの地雷オブジェリストが取得できること
	@Test
	public void testMines_Primary() {
		var chs = BeMusicChannel.mines(BeMusicLane.PRIMARY);
		assertSame(BeMusicChannel.MINES_PRIMARY, chs);
		assertTrue(chs.contains(BeMusicChannel.MINE_1P_01));
		assertTrue(chs.contains(BeMusicChannel.MINE_1P_02));
		assertTrue(chs.contains(BeMusicChannel.MINE_1P_03));
		assertTrue(chs.contains(BeMusicChannel.MINE_1P_04));
		assertTrue(chs.contains(BeMusicChannel.MINE_1P_05));
		assertTrue(chs.contains(BeMusicChannel.MINE_1P_06));
		assertTrue(chs.contains(BeMusicChannel.MINE_1P_07));
		assertTrue(chs.contains(BeMusicChannel.MINE_1P_08));
		assertTrue(chs.contains(BeMusicChannel.MINE_1P_09));
	}

	// mines(BeMusicLane)
	// 副レーンの地雷オブジェリストが取得できること
	@Test
	public void testMines_Secondary() {
		var chs = BeMusicChannel.mines(BeMusicLane.SECONDARY);
		assertSame(BeMusicChannel.MINES_SECONDARY, chs);
		assertTrue(chs.contains(BeMusicChannel.MINE_2P_01));
		assertTrue(chs.contains(BeMusicChannel.MINE_2P_02));
		assertTrue(chs.contains(BeMusicChannel.MINE_2P_03));
		assertTrue(chs.contains(BeMusicChannel.MINE_2P_04));
		assertTrue(chs.contains(BeMusicChannel.MINE_2P_05));
		assertTrue(chs.contains(BeMusicChannel.MINE_2P_06));
		assertTrue(chs.contains(BeMusicChannel.MINE_2P_07));
		assertTrue(chs.contains(BeMusicChannel.MINE_2P_08));
		assertTrue(chs.contains(BeMusicChannel.MINE_2P_09));
	}

	// mines(BeMusicLane)
	// 取得したリストは読み取り専用であること
	@Test
	public void testMines_ReadOnly() {
		var ex = UnsupportedOperationException.class;
		var primary = BeMusicChannel.mines(BeMusicLane.PRIMARY);
		assertThrows(ex, () -> primary.add(BmsChannel.object(BmsSpec.USER_CHANNEL_MIN)));
		var secondary = BeMusicChannel.mines(BeMusicLane.SECONDARY);
		assertThrows(ex, () -> secondary.add(BmsChannel.object(BmsSpec.USER_CHANNEL_MIN)));
	}

	// mines(BeMusicLane)
	// NullPointerException laneがnull
	@Test
	public void testMines_NullLane() {
		assertThrows(NullPointerException.class, () -> BeMusicChannel.mines(null));
	}
}
