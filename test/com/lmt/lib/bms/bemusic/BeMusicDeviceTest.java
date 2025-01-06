package com.lmt.lib.bms.bemusic;

import static org.junit.Assert.*;

import java.util.stream.Collectors;

import org.junit.Test;

public class BeMusicDeviceTest {
	// getIndex()
	// 正常：SP/DP左側レーン向けのデバイスが0から開始、DP右側レーンのデバイスがその後に連番で続くこと
	@Test
	public void testGetIndex_Normal() {
		assertEquals(0, BeMusicDevice.SWITCH11.getIndex());
		assertEquals(1, BeMusicDevice.SWITCH12.getIndex());
		assertEquals(2, BeMusicDevice.SWITCH13.getIndex());
		assertEquals(3, BeMusicDevice.SWITCH14.getIndex());
		assertEquals(4, BeMusicDevice.SWITCH15.getIndex());
		assertEquals(5, BeMusicDevice.SWITCH16.getIndex());
		assertEquals(6, BeMusicDevice.SWITCH17.getIndex());
		assertEquals(7, BeMusicDevice.SCRATCH1.getIndex());
		assertEquals(8, BeMusicDevice.SWITCH21.getIndex());
		assertEquals(9, BeMusicDevice.SWITCH22.getIndex());
		assertEquals(10, BeMusicDevice.SWITCH23.getIndex());
		assertEquals(11, BeMusicDevice.SWITCH24.getIndex());
		assertEquals(12, BeMusicDevice.SWITCH25.getIndex());
		assertEquals(13, BeMusicDevice.SWITCH26.getIndex());
		assertEquals(14, BeMusicDevice.SWITCH27.getIndex());
		assertEquals(15, BeMusicDevice.SCRATCH2.getIndex());
	}

	// getLane()
	// 正常：SP/DP左側レーンが主レーン、DP右側レーンが副レーンとなること
	@Test
	public void testGetLane_Normal() {
		assertEquals(BeMusicLane.PRIMARY, BeMusicDevice.SWITCH11.getLane());
		assertEquals(BeMusicLane.PRIMARY, BeMusicDevice.SWITCH12.getLane());
		assertEquals(BeMusicLane.PRIMARY, BeMusicDevice.SWITCH13.getLane());
		assertEquals(BeMusicLane.PRIMARY, BeMusicDevice.SWITCH14.getLane());
		assertEquals(BeMusicLane.PRIMARY, BeMusicDevice.SWITCH15.getLane());
		assertEquals(BeMusicLane.PRIMARY, BeMusicDevice.SWITCH16.getLane());
		assertEquals(BeMusicLane.PRIMARY, BeMusicDevice.SWITCH17.getLane());
		assertEquals(BeMusicLane.PRIMARY, BeMusicDevice.SCRATCH1.getLane());
		assertEquals(BeMusicLane.SECONDARY, BeMusicDevice.SWITCH21.getLane());
		assertEquals(BeMusicLane.SECONDARY, BeMusicDevice.SWITCH22.getLane());
		assertEquals(BeMusicLane.SECONDARY, BeMusicDevice.SWITCH23.getLane());
		assertEquals(BeMusicLane.SECONDARY, BeMusicDevice.SWITCH24.getLane());
		assertEquals(BeMusicLane.SECONDARY, BeMusicDevice.SWITCH25.getLane());
		assertEquals(BeMusicLane.SECONDARY, BeMusicDevice.SWITCH26.getLane());
		assertEquals(BeMusicLane.SECONDARY, BeMusicDevice.SWITCH27.getLane());
		assertEquals(BeMusicLane.SECONDARY, BeMusicDevice.SCRATCH2.getLane());
	}

	// getVisibleChannel()
	// 正常：チャンネルが想定通りになっていること
	// 参考：https://hitkey.nekokan.dyndns.info/cmdsJP.htm#CHANNEL-MAPPING-TABLE
	@Test
	public void testGetVisibleChannel() {
		assertEquals(BeMusicChannel.VISIBLE_1P_01, BeMusicDevice.SWITCH11.getVisibleChannel());
		assertEquals(BeMusicChannel.VISIBLE_1P_02, BeMusicDevice.SWITCH12.getVisibleChannel());
		assertEquals(BeMusicChannel.VISIBLE_1P_03, BeMusicDevice.SWITCH13.getVisibleChannel());
		assertEquals(BeMusicChannel.VISIBLE_1P_04, BeMusicDevice.SWITCH14.getVisibleChannel());
		assertEquals(BeMusicChannel.VISIBLE_1P_05, BeMusicDevice.SWITCH15.getVisibleChannel());
		assertEquals(BeMusicChannel.VISIBLE_1P_08, BeMusicDevice.SWITCH16.getVisibleChannel());
		assertEquals(BeMusicChannel.VISIBLE_1P_09, BeMusicDevice.SWITCH17.getVisibleChannel());
		assertEquals(BeMusicChannel.VISIBLE_1P_06, BeMusicDevice.SCRATCH1.getVisibleChannel());
		assertEquals(BeMusicChannel.VISIBLE_2P_01, BeMusicDevice.SWITCH21.getVisibleChannel());
		assertEquals(BeMusicChannel.VISIBLE_2P_02, BeMusicDevice.SWITCH22.getVisibleChannel());
		assertEquals(BeMusicChannel.VISIBLE_2P_03, BeMusicDevice.SWITCH23.getVisibleChannel());
		assertEquals(BeMusicChannel.VISIBLE_2P_04, BeMusicDevice.SWITCH24.getVisibleChannel());
		assertEquals(BeMusicChannel.VISIBLE_2P_05, BeMusicDevice.SWITCH25.getVisibleChannel());
		assertEquals(BeMusicChannel.VISIBLE_2P_08, BeMusicDevice.SWITCH26.getVisibleChannel());
		assertEquals(BeMusicChannel.VISIBLE_2P_09, BeMusicDevice.SWITCH27.getVisibleChannel());
		assertEquals(BeMusicChannel.VISIBLE_2P_06, BeMusicDevice.SCRATCH2.getVisibleChannel());
	}

	// getInvisibleChannel()
	// 正常：チャンネルが想定通りになっていること
	// 参考：https://hitkey.nekokan.dyndns.info/cmdsJP.htm#CHANNEL-MAPPING-TABLE
	@Test
	public void testGetInvisibleChannel() {
		assertEquals(BeMusicChannel.INVISIBLE_1P_01, BeMusicDevice.SWITCH11.getInvisibleChannel());
		assertEquals(BeMusicChannel.INVISIBLE_1P_02, BeMusicDevice.SWITCH12.getInvisibleChannel());
		assertEquals(BeMusicChannel.INVISIBLE_1P_03, BeMusicDevice.SWITCH13.getInvisibleChannel());
		assertEquals(BeMusicChannel.INVISIBLE_1P_04, BeMusicDevice.SWITCH14.getInvisibleChannel());
		assertEquals(BeMusicChannel.INVISIBLE_1P_05, BeMusicDevice.SWITCH15.getInvisibleChannel());
		assertEquals(BeMusicChannel.INVISIBLE_1P_08, BeMusicDevice.SWITCH16.getInvisibleChannel());
		assertEquals(BeMusicChannel.INVISIBLE_1P_09, BeMusicDevice.SWITCH17.getInvisibleChannel());
		assertEquals(BeMusicChannel.INVISIBLE_1P_06, BeMusicDevice.SCRATCH1.getInvisibleChannel());
		assertEquals(BeMusicChannel.INVISIBLE_2P_01, BeMusicDevice.SWITCH21.getInvisibleChannel());
		assertEquals(BeMusicChannel.INVISIBLE_2P_02, BeMusicDevice.SWITCH22.getInvisibleChannel());
		assertEquals(BeMusicChannel.INVISIBLE_2P_03, BeMusicDevice.SWITCH23.getInvisibleChannel());
		assertEquals(BeMusicChannel.INVISIBLE_2P_04, BeMusicDevice.SWITCH24.getInvisibleChannel());
		assertEquals(BeMusicChannel.INVISIBLE_2P_05, BeMusicDevice.SWITCH25.getInvisibleChannel());
		assertEquals(BeMusicChannel.INVISIBLE_2P_08, BeMusicDevice.SWITCH26.getInvisibleChannel());
		assertEquals(BeMusicChannel.INVISIBLE_2P_09, BeMusicDevice.SWITCH27.getInvisibleChannel());
		assertEquals(BeMusicChannel.INVISIBLE_2P_06, BeMusicDevice.SCRATCH2.getInvisibleChannel());
	}

	// getMineChannel()
	// 正常：チャンネルが想定通りになっていること
	// 参考：https://hitkey.nekokan.dyndns.info/cmdsJP.htm#CHANNEL-MAPPING-TABLE
	@Test
	public void testGetMineChannel() {
		assertEquals(BeMusicChannel.MINE_1P_01, BeMusicDevice.SWITCH11.getMineChannel());
		assertEquals(BeMusicChannel.MINE_1P_02, BeMusicDevice.SWITCH12.getMineChannel());
		assertEquals(BeMusicChannel.MINE_1P_03, BeMusicDevice.SWITCH13.getMineChannel());
		assertEquals(BeMusicChannel.MINE_1P_04, BeMusicDevice.SWITCH14.getMineChannel());
		assertEquals(BeMusicChannel.MINE_1P_05, BeMusicDevice.SWITCH15.getMineChannel());
		assertEquals(BeMusicChannel.MINE_1P_08, BeMusicDevice.SWITCH16.getMineChannel());
		assertEquals(BeMusicChannel.MINE_1P_09, BeMusicDevice.SWITCH17.getMineChannel());
		assertEquals(BeMusicChannel.MINE_1P_06, BeMusicDevice.SCRATCH1.getMineChannel());
		assertEquals(BeMusicChannel.MINE_2P_01, BeMusicDevice.SWITCH21.getMineChannel());
		assertEquals(BeMusicChannel.MINE_2P_02, BeMusicDevice.SWITCH22.getMineChannel());
		assertEquals(BeMusicChannel.MINE_2P_03, BeMusicDevice.SWITCH23.getMineChannel());
		assertEquals(BeMusicChannel.MINE_2P_04, BeMusicDevice.SWITCH24.getMineChannel());
		assertEquals(BeMusicChannel.MINE_2P_05, BeMusicDevice.SWITCH25.getMineChannel());
		assertEquals(BeMusicChannel.MINE_2P_08, BeMusicDevice.SWITCH26.getMineChannel());
		assertEquals(BeMusicChannel.MINE_2P_09, BeMusicDevice.SWITCH27.getMineChannel());
		assertEquals(BeMusicChannel.MINE_2P_06, BeMusicDevice.SCRATCH2.getMineChannel());
	}

	// getSwitchNumber()
	// スクラッチはスイッチ番号が0になること
	@Test
	public void testGetSwitchNumber_Scratch() {
		assertEquals(0, BeMusicDevice.SCRATCH1.getSwitchNumber());
		assertEquals(0, BeMusicDevice.SCRATCH2.getSwitchNumber());
	}

	// getSwitchNumber()
	// スイッチはスイッチ番号が1～7の連番になっており、自身にスイッチと番号が対応していること
	@Test
	public void testGetSwitchNumber_Switch() {
		assertEquals(1, BeMusicDevice.SWITCH11.getSwitchNumber());
		assertEquals(2, BeMusicDevice.SWITCH12.getSwitchNumber());
		assertEquals(3, BeMusicDevice.SWITCH13.getSwitchNumber());
		assertEquals(4, BeMusicDevice.SWITCH14.getSwitchNumber());
		assertEquals(5, BeMusicDevice.SWITCH15.getSwitchNumber());
		assertEquals(6, BeMusicDevice.SWITCH16.getSwitchNumber());
		assertEquals(7, BeMusicDevice.SWITCH17.getSwitchNumber());
		assertEquals(1, BeMusicDevice.SWITCH21.getSwitchNumber());
		assertEquals(2, BeMusicDevice.SWITCH22.getSwitchNumber());
		assertEquals(3, BeMusicDevice.SWITCH23.getSwitchNumber());
		assertEquals(4, BeMusicDevice.SWITCH24.getSwitchNumber());
		assertEquals(5, BeMusicDevice.SWITCH25.getSwitchNumber());
		assertEquals(6, BeMusicDevice.SWITCH26.getSwitchNumber());
		assertEquals(7, BeMusicDevice.SWITCH27.getSwitchNumber());
	}

	// isPrimary()
	// 主レーンの入力デバイスがtrueになること
	@Test
	public void testIsPrimary() {
		assertTrue(BeMusicDevice.SWITCH11.isPrimary());
		assertTrue(BeMusicDevice.SWITCH12.isPrimary());
		assertTrue(BeMusicDevice.SWITCH13.isPrimary());
		assertTrue(BeMusicDevice.SWITCH14.isPrimary());
		assertTrue(BeMusicDevice.SWITCH15.isPrimary());
		assertTrue(BeMusicDevice.SWITCH16.isPrimary());
		assertTrue(BeMusicDevice.SWITCH17.isPrimary());
		assertTrue(BeMusicDevice.SCRATCH1.isPrimary());
		assertFalse(BeMusicDevice.SWITCH21.isPrimary());
		assertFalse(BeMusicDevice.SWITCH22.isPrimary());
		assertFalse(BeMusicDevice.SWITCH23.isPrimary());
		assertFalse(BeMusicDevice.SWITCH24.isPrimary());
		assertFalse(BeMusicDevice.SWITCH25.isPrimary());
		assertFalse(BeMusicDevice.SWITCH26.isPrimary());
		assertFalse(BeMusicDevice.SWITCH27.isPrimary());
		assertFalse(BeMusicDevice.SCRATCH2.isPrimary());
	}

	// isSecondary()
	// 副レーンの入力デバイスがtrueになること
	@Test
	public void testIsSecondary() {
		assertFalse(BeMusicDevice.SWITCH11.isSecondary());
		assertFalse(BeMusicDevice.SWITCH12.isSecondary());
		assertFalse(BeMusicDevice.SWITCH13.isSecondary());
		assertFalse(BeMusicDevice.SWITCH14.isSecondary());
		assertFalse(BeMusicDevice.SWITCH15.isSecondary());
		assertFalse(BeMusicDevice.SWITCH16.isSecondary());
		assertFalse(BeMusicDevice.SWITCH17.isSecondary());
		assertFalse(BeMusicDevice.SCRATCH1.isSecondary());
		assertTrue(BeMusicDevice.SWITCH21.isSecondary());
		assertTrue(BeMusicDevice.SWITCH22.isSecondary());
		assertTrue(BeMusicDevice.SWITCH23.isSecondary());
		assertTrue(BeMusicDevice.SWITCH24.isSecondary());
		assertTrue(BeMusicDevice.SWITCH25.isSecondary());
		assertTrue(BeMusicDevice.SWITCH26.isSecondary());
		assertTrue(BeMusicDevice.SWITCH27.isSecondary());
		assertTrue(BeMusicDevice.SCRATCH2.isSecondary());
	}

	// isSwitch()
	// スイッチではtrueになること
	@Test
	public void testIsSwitch_Switch() {
		assertTrue(BeMusicDevice.SWITCH11.isSwitch());
		assertTrue(BeMusicDevice.SWITCH12.isSwitch());
		assertTrue(BeMusicDevice.SWITCH13.isSwitch());
		assertTrue(BeMusicDevice.SWITCH14.isSwitch());
		assertTrue(BeMusicDevice.SWITCH15.isSwitch());
		assertTrue(BeMusicDevice.SWITCH16.isSwitch());
		assertTrue(BeMusicDevice.SWITCH17.isSwitch());
		assertTrue(BeMusicDevice.SWITCH21.isSwitch());
		assertTrue(BeMusicDevice.SWITCH22.isSwitch());
		assertTrue(BeMusicDevice.SWITCH23.isSwitch());
		assertTrue(BeMusicDevice.SWITCH24.isSwitch());
		assertTrue(BeMusicDevice.SWITCH25.isSwitch());
		assertTrue(BeMusicDevice.SWITCH26.isSwitch());
		assertTrue(BeMusicDevice.SWITCH27.isSwitch());
	}

	// isSwitch()
	// スクラッチではfalseになること
	@Test
	public void testIsSwitch_Scratch() {
		assertFalse(BeMusicDevice.SCRATCH1.isSwitch());
		assertFalse(BeMusicDevice.SCRATCH2.isSwitch());
	}

	// isScratch()
	// スイッチではfalseになること
	@Test
	public void testIsScratch_Switch() {
		assertFalse(BeMusicDevice.SWITCH11.isScratch());
		assertFalse(BeMusicDevice.SWITCH12.isScratch());
		assertFalse(BeMusicDevice.SWITCH13.isScratch());
		assertFalse(BeMusicDevice.SWITCH14.isScratch());
		assertFalse(BeMusicDevice.SWITCH15.isScratch());
		assertFalse(BeMusicDevice.SWITCH16.isScratch());
		assertFalse(BeMusicDevice.SWITCH17.isScratch());
		assertFalse(BeMusicDevice.SWITCH21.isScratch());
		assertFalse(BeMusicDevice.SWITCH22.isScratch());
		assertFalse(BeMusicDevice.SWITCH23.isScratch());
		assertFalse(BeMusicDevice.SWITCH24.isScratch());
		assertFalse(BeMusicDevice.SWITCH25.isScratch());
		assertFalse(BeMusicDevice.SWITCH26.isScratch());
		assertFalse(BeMusicDevice.SWITCH27.isScratch());
	}

	// isScratch()
	// スクラッチではtrueになること
	@Test
	public void testIsScratch_Scratch() {
		assertTrue(BeMusicDevice.SCRATCH1.isScratch());
		assertTrue(BeMusicDevice.SCRATCH2.isScratch());
	}

	// isSwitch(int)
	// 1～7を指定するとtrueになること
	@Test
	public void testIsSwitchInt_Switch() {
		assertTrue(BeMusicDevice.isSwitch(1));
		assertTrue(BeMusicDevice.isSwitch(2));
		assertTrue(BeMusicDevice.isSwitch(3));
		assertTrue(BeMusicDevice.isSwitch(4));
		assertTrue(BeMusicDevice.isSwitch(5));
		assertTrue(BeMusicDevice.isSwitch(6));
		assertTrue(BeMusicDevice.isSwitch(7));
	}

	// isSwitch(int)
	// 0を指定するとfalseになること
	@Test
	public void testIsSwitchInt_Scratch() {
		assertFalse(BeMusicDevice.isSwitch(0));
	}

	// isSwitch(int)
	// 負の値、7より大きい値を指定するとfalseになること
	@Test
	public void testIsSwitchInt_InvalidNumber() {
		assertFalse(BeMusicDevice.isSwitch(-1));
		assertFalse(BeMusicDevice.isSwitch(8));
	}

	// isScratch(int)
	// 1～7を指定するとfalseになること
	@Test
	public void testIsScratchInt_Switch() {
		assertFalse(BeMusicDevice.isScratch(1));
		assertFalse(BeMusicDevice.isScratch(2));
		assertFalse(BeMusicDevice.isScratch(3));
		assertFalse(BeMusicDevice.isScratch(4));
		assertFalse(BeMusicDevice.isScratch(5));
		assertFalse(BeMusicDevice.isScratch(6));
		assertFalse(BeMusicDevice.isScratch(7));
	}

	// isScratch(int)
	// 0を指定するとtrueになること
	@Test
	public void testIsScratchInt_Scratch() {
		assertTrue(BeMusicDevice.isScratch(0));
	}

	// isScratch(int)
	// 負の値、7より大きい値を指定するとfalseになること
	@Test
	public void testIsScratchInt_InvalidNumber() {
		assertFalse(BeMusicDevice.isScratch(-1));
		assertFalse(BeMusicDevice.isScratch(8));
	}

	// fromIndex(int)
	// 正常：インデックスに対応した入力デバイスが取得できること
	@Test
	public void testFromIndex() {
		assertEquals(BeMusicDevice.SWITCH11, BeMusicDevice.fromIndex(0));
		assertEquals(BeMusicDevice.SWITCH12, BeMusicDevice.fromIndex(1));
		assertEquals(BeMusicDevice.SWITCH13, BeMusicDevice.fromIndex(2));
		assertEquals(BeMusicDevice.SWITCH14, BeMusicDevice.fromIndex(3));
		assertEquals(BeMusicDevice.SWITCH15, BeMusicDevice.fromIndex(4));
		assertEquals(BeMusicDevice.SWITCH16, BeMusicDevice.fromIndex(5));
		assertEquals(BeMusicDevice.SWITCH17, BeMusicDevice.fromIndex(6));
		assertEquals(BeMusicDevice.SCRATCH1, BeMusicDevice.fromIndex(7));
		assertEquals(BeMusicDevice.SWITCH21, BeMusicDevice.fromIndex(8));
		assertEquals(BeMusicDevice.SWITCH22, BeMusicDevice.fromIndex(9));
		assertEquals(BeMusicDevice.SWITCH23, BeMusicDevice.fromIndex(10));
		assertEquals(BeMusicDevice.SWITCH24, BeMusicDevice.fromIndex(11));
		assertEquals(BeMusicDevice.SWITCH25, BeMusicDevice.fromIndex(12));
		assertEquals(BeMusicDevice.SWITCH26, BeMusicDevice.fromIndex(13));
		assertEquals(BeMusicDevice.SWITCH27, BeMusicDevice.fromIndex(14));
		assertEquals(BeMusicDevice.SCRATCH2, BeMusicDevice.fromIndex(15));
	}

	// fromChannel(int)
	// 全ての入力デバイスのチャンネルが期待通りにマッピングされていること
	@Test
	public void testFromChannel_Normal() {
		BeMusicDevice.all().forEach(d -> {
			assertSame(d, BeMusicDevice.fromChannel(d.getVisibleChannel().getNumber()));
			assertSame(d, BeMusicDevice.fromChannel(d.getInvisibleChannel().getNumber()));
			assertSame(d, BeMusicDevice.fromChannel(d.getLongChannel().getNumber()));
			assertSame(d, BeMusicDevice.fromChannel(d.getMineChannel().getNumber()));
		});
	}

	// fromChannel(int)
	// 非対応チャンネルを指定するとnullを返すこと
	@Test
	public void testFromChannel_Unknown() {
		assertNull(BeMusicDevice.fromChannel(0));
		assertNull(BeMusicDevice.fromChannel(-1));
		assertNull(BeMusicDevice.fromChannel(BeMusicChannel.VISIBLE_1P_10.getNumber()));
		assertNull(BeMusicDevice.fromChannel(BeMusicChannel.VISIBLE_2P_10.getNumber()));
		assertNull(BeMusicDevice.fromChannel(BeMusicChannel.INVISIBLE_1P_10.getNumber()));
		assertNull(BeMusicDevice.fromChannel(BeMusicChannel.INVISIBLE_2P_10.getNumber()));
		assertNull(BeMusicDevice.fromChannel(BeMusicChannel.LONG_1P_10.getNumber()));
		assertNull(BeMusicDevice.fromChannel(BeMusicChannel.LONG_2P_10.getNumber()));
		assertNull(BeMusicDevice.fromChannel(BeMusicChannel.MINE_1P_07.getNumber()));
		assertNull(BeMusicDevice.fromChannel(BeMusicChannel.MINE_2P_07.getNumber()));
	}

	// all()
	// 全ての入力デバイスが定義順(期待通り)に走査されること
	@Test
	public void testAll() {
		var allDevs = BeMusicDevice.all().collect(Collectors.toList());
		assertEquals(BeMusicDevice.SWITCH11, allDevs.get(0));
		assertEquals(BeMusicDevice.SWITCH12, allDevs.get(1));
		assertEquals(BeMusicDevice.SWITCH13, allDevs.get(2));
		assertEquals(BeMusicDevice.SWITCH14, allDevs.get(3));
		assertEquals(BeMusicDevice.SWITCH15, allDevs.get(4));
		assertEquals(BeMusicDevice.SWITCH16, allDevs.get(5));
		assertEquals(BeMusicDevice.SWITCH17, allDevs.get(6));
		assertEquals(BeMusicDevice.SCRATCH1, allDevs.get(7));
		assertEquals(BeMusicDevice.SWITCH21, allDevs.get(8));
		assertEquals(BeMusicDevice.SWITCH22, allDevs.get(9));
		assertEquals(BeMusicDevice.SWITCH23, allDevs.get(10));
		assertEquals(BeMusicDevice.SWITCH24, allDevs.get(11));
		assertEquals(BeMusicDevice.SWITCH25, allDevs.get(12));
		assertEquals(BeMusicDevice.SWITCH26, allDevs.get(13));
		assertEquals(BeMusicDevice.SWITCH27, allDevs.get(14));
		assertEquals(BeMusicDevice.SCRATCH2, allDevs.get(15));
	}

	// orderedBySpLeftList()
	// 正常：スクラッチがスイッチの左側にある場合のシングルプレー用入力デバイスリストが取得できること
	@Test
	public void testOrderedBySpLeftList() {
		var l = BeMusicDevice.orderedBySpLeftList();
		assertNotNull(l);
		assertEquals(BeMusicDevice.COUNT_PER_LANE, l.size());
		assertEquals(BeMusicDevice.SCRATCH1, l.get(0));
		assertEquals(BeMusicDevice.SWITCH11, l.get(1));
		assertEquals(BeMusicDevice.SWITCH12, l.get(2));
		assertEquals(BeMusicDevice.SWITCH13, l.get(3));
		assertEquals(BeMusicDevice.SWITCH14, l.get(4));
		assertEquals(BeMusicDevice.SWITCH15, l.get(5));
		assertEquals(BeMusicDevice.SWITCH16, l.get(6));
		assertEquals(BeMusicDevice.SWITCH17, l.get(7));
	}

	// orderedBySpRightList()
	// 正常：スクラッチがスイッチの右側にある場合のシングルプレー用入力デバイスリストが取得できること
	@Test
	public void testOrderedBySpRightList() {
		var l = BeMusicDevice.orderedBySpRightList();
		assertNotNull(l);
		assertEquals(BeMusicDevice.COUNT_PER_LANE, l.size());
		assertEquals(BeMusicDevice.SWITCH11, l.get(0));
		assertEquals(BeMusicDevice.SWITCH12, l.get(1));
		assertEquals(BeMusicDevice.SWITCH13, l.get(2));
		assertEquals(BeMusicDevice.SWITCH14, l.get(3));
		assertEquals(BeMusicDevice.SWITCH15, l.get(4));
		assertEquals(BeMusicDevice.SWITCH16, l.get(5));
		assertEquals(BeMusicDevice.SWITCH17, l.get(6));
		assertEquals(BeMusicDevice.SCRATCH1, l.get(7));
	}

	// orderedByDpList()
	// 正常：ダブルプレー用入力デバイスリストが取得できること
	@Test
	public void testOrderedByDpList() {
		var l = BeMusicDevice.orderedByDpList();
		assertNotNull(l);
		assertEquals(16, l.size());
		assertEquals(BeMusicDevice.SCRATCH1, l.get(0));
		assertEquals(BeMusicDevice.SWITCH11, l.get(1));
		assertEquals(BeMusicDevice.SWITCH12, l.get(2));
		assertEquals(BeMusicDevice.SWITCH13, l.get(3));
		assertEquals(BeMusicDevice.SWITCH14, l.get(4));
		assertEquals(BeMusicDevice.SWITCH15, l.get(5));
		assertEquals(BeMusicDevice.SWITCH16, l.get(6));
		assertEquals(BeMusicDevice.SWITCH17, l.get(7));
		assertEquals(BeMusicDevice.SWITCH21, l.get(8));
		assertEquals(BeMusicDevice.SWITCH22, l.get(9));
		assertEquals(BeMusicDevice.SWITCH23, l.get(10));
		assertEquals(BeMusicDevice.SWITCH24, l.get(11));
		assertEquals(BeMusicDevice.SWITCH25, l.get(12));
		assertEquals(BeMusicDevice.SWITCH26, l.get(13));
		assertEquals(BeMusicDevice.SWITCH27, l.get(14));
		assertEquals(BeMusicDevice.SCRATCH2, l.get(15));
	}

	// orderedByDpList(BeMusicLane)
	// 期待する並び順で入力デバイスリストが取得できること
	@Test
	public void testOrderedByDpListBeMusicLane_Normal() {
		var p = BeMusicDevice.orderedByDpList(BeMusicLane.PRIMARY);
		assertNotNull(p);
		assertEquals(8, p.size());
		assertEquals(BeMusicDevice.SCRATCH1, p.get(0));
		assertEquals(BeMusicDevice.SWITCH11, p.get(1));
		assertEquals(BeMusicDevice.SWITCH12, p.get(2));
		assertEquals(BeMusicDevice.SWITCH13, p.get(3));
		assertEquals(BeMusicDevice.SWITCH14, p.get(4));
		assertEquals(BeMusicDevice.SWITCH15, p.get(5));
		assertEquals(BeMusicDevice.SWITCH16, p.get(6));
		assertEquals(BeMusicDevice.SWITCH17, p.get(7));
		var s = BeMusicDevice.orderedByDpList(BeMusicLane.SECONDARY);
		assertNotNull(s);
		assertEquals(8, s.size());
		assertEquals(BeMusicDevice.SWITCH21, s.get(0));
		assertEquals(BeMusicDevice.SWITCH22, s.get(1));
		assertEquals(BeMusicDevice.SWITCH23, s.get(2));
		assertEquals(BeMusicDevice.SWITCH24, s.get(3));
		assertEquals(BeMusicDevice.SWITCH25, s.get(4));
		assertEquals(BeMusicDevice.SWITCH26, s.get(5));
		assertEquals(BeMusicDevice.SWITCH27, s.get(6));
		assertEquals(BeMusicDevice.SCRATCH2, s.get(7));
	}

	// orderedByDpList(BeMusicLane)
	// NullPointerException laneがnull
	@Test
	public void testOrderedByDpListBeMusicLane_NullLane() {
		assertThrows(NullPointerException.class, () -> BeMusicDevice.orderedByDpList(null));
	}

	// getBaseIndex(BeMusicLane)
	// 主レーンのインデックスベース値が期待通りであること
	@Test
	public void testGetBaseIndex_Primary() {
		assertEquals(BeMusicDevice.PRIMARY_BASE, BeMusicDevice.getBaseIndex(BeMusicLane.PRIMARY));
	}

	// getBaseIndex(BeMusicLane)
	// 副レーンのインデックスベース値が期待通りであること
	@Test
	public void testGetBaseIndex_Secondary() {
		assertEquals(BeMusicDevice.SECONDARY_BASE, BeMusicDevice.getBaseIndex(BeMusicLane.SECONDARY));
	}

	// getBaseIndex(BeMusicLane)
	// NullPointerException laneがnull
	@Test
	public void testGetBaseIndex_NullLane() {
		assertThrows(NullPointerException.class, () -> BeMusicDevice.getBaseIndex(null));
	}

	// getDevices(BeMusicLane)
	// 主レーンの入力デバイスリストが期待通りであること
	@Test
	public void testGetDevices_Primary() {
		var l = BeMusicDevice.getDevices(BeMusicLane.PRIMARY);
		assertEquals(BeMusicDevice.COUNT_PER_LANE, l.size());
		assertTrue(l.contains(BeMusicDevice.SCRATCH1));
		assertTrue(l.contains(BeMusicDevice.SWITCH11));
		assertTrue(l.contains(BeMusicDevice.SWITCH12));
		assertTrue(l.contains(BeMusicDevice.SWITCH13));
		assertTrue(l.contains(BeMusicDevice.SWITCH14));
		assertTrue(l.contains(BeMusicDevice.SWITCH15));
		assertTrue(l.contains(BeMusicDevice.SWITCH16));
		assertTrue(l.contains(BeMusicDevice.SWITCH17));
	}

	// getDevices(BeMusicLane)
	// 副レーンの入力デバイスリストが期待通りであること
	@Test
	public void testGetDevices_Secondary() {
		var l = BeMusicDevice.getDevices(BeMusicLane.SECONDARY);
		assertEquals(BeMusicDevice.COUNT_PER_LANE, l.size());
		assertTrue(l.contains(BeMusicDevice.SWITCH21));
		assertTrue(l.contains(BeMusicDevice.SWITCH22));
		assertTrue(l.contains(BeMusicDevice.SWITCH23));
		assertTrue(l.contains(BeMusicDevice.SWITCH24));
		assertTrue(l.contains(BeMusicDevice.SWITCH25));
		assertTrue(l.contains(BeMusicDevice.SWITCH26));
		assertTrue(l.contains(BeMusicDevice.SWITCH27));
		assertTrue(l.contains(BeMusicDevice.SCRATCH2));
	}

	// getDevices(BeMusicLane)
	// 取得したリストが読み取り専用であること
	@Test
	public void testGetDevices_ListIsReadOnly() {
		var l1 = BeMusicDevice.getDevices(BeMusicLane.PRIMARY);
		assertThrows(Exception.class, () -> l1.add(BeMusicDevice.SWITCH11));
		assertThrows(Exception.class, () -> l1.remove(BeMusicDevice.SWITCH11));
		assertThrows(Exception.class, () -> l1.set(0, BeMusicDevice.SWITCH11));
		assertThrows(Exception.class, () -> l1.clear());
		var l2 = BeMusicDevice.getDevices(BeMusicLane.SECONDARY);
		assertThrows(Exception.class, () -> l2.add(BeMusicDevice.SWITCH11));
		assertThrows(Exception.class, () -> l2.remove(BeMusicDevice.SWITCH11));
		assertThrows(Exception.class, () -> l2.set(0, BeMusicDevice.SWITCH11));
		assertThrows(Exception.class, () -> l2.clear());
	}

	// getDevices(BeMusicLane)
	// NullPointerException laneがnull
	@Test
	public void testGetDevices_NullLane() {
		assertThrows(NullPointerException.class, () -> BeMusicDevice.getDevices(null));
	}

	// getSwitches(BeMusicLane)
	// 主レーンの入力デバイスリストが期待通りであること
	@Test
	public void testGetSwitches_Primary() {
		var l = BeMusicDevice.getSwitches(BeMusicLane.PRIMARY);
		assertEquals(7, l.size());
		assertTrue(l.contains(BeMusicDevice.SWITCH11));
		assertTrue(l.contains(BeMusicDevice.SWITCH12));
		assertTrue(l.contains(BeMusicDevice.SWITCH13));
		assertTrue(l.contains(BeMusicDevice.SWITCH14));
		assertTrue(l.contains(BeMusicDevice.SWITCH15));
		assertTrue(l.contains(BeMusicDevice.SWITCH16));
		assertTrue(l.contains(BeMusicDevice.SWITCH17));
	}

	// getSwitches(BeMusicLane)
	// 副レーンの入力デバイスリストが期待通りであること
	@Test
	public void testGetSwitches_Secondary() {
		var l = BeMusicDevice.getSwitches(BeMusicLane.SECONDARY);
		assertEquals(7, l.size());
		assertTrue(l.contains(BeMusicDevice.SWITCH21));
		assertTrue(l.contains(BeMusicDevice.SWITCH22));
		assertTrue(l.contains(BeMusicDevice.SWITCH23));
		assertTrue(l.contains(BeMusicDevice.SWITCH24));
		assertTrue(l.contains(BeMusicDevice.SWITCH25));
		assertTrue(l.contains(BeMusicDevice.SWITCH26));
		assertTrue(l.contains(BeMusicDevice.SWITCH27));
	}

	// getSwitches(BeMusicLane)
	// 取得したリストが読み取り専用であること
	@Test
	public void testGetSwitches_ListIsReadOnly() {
		var l1 = BeMusicDevice.getSwitches(BeMusicLane.PRIMARY);
		assertThrows(Exception.class, () -> l1.add(BeMusicDevice.SWITCH11));
		assertThrows(Exception.class, () -> l1.remove(BeMusicDevice.SWITCH11));
		assertThrows(Exception.class, () -> l1.set(0, BeMusicDevice.SWITCH11));
		assertThrows(Exception.class, () -> l1.clear());
		var l2 = BeMusicDevice.getSwitches(BeMusicLane.SECONDARY);
		assertThrows(Exception.class, () -> l2.add(BeMusicDevice.SWITCH11));
		assertThrows(Exception.class, () -> l2.remove(BeMusicDevice.SWITCH11));
		assertThrows(Exception.class, () -> l2.set(0, BeMusicDevice.SWITCH11));
		assertThrows(Exception.class, () -> l2.clear());
	}

	// getSwitches(BeMusicLane)
	// NullPointerException laneがnull
	@Test
	public void testGetSwitches_NullLane() {
		assertThrows(NullPointerException.class, () -> BeMusicDevice.getSwitches(null));
	}

	// getScratch(BeMusicLane)
	// 主レーンのスクラッチが期待通りであること
	@Test
	public void testGetScratch_Primary() {
		var dev = BeMusicDevice.getScratch(BeMusicLane.PRIMARY);
		assertEquals(BeMusicDevice.SCRATCH1, dev);
	}

	// getScratch(BeMusicLane)
	// 副レーンのスクラッチが期待通りであること
	@Test
	public void testGetScratch_Secondary() {
		var dev = BeMusicDevice.getScratch(BeMusicLane.SECONDARY);
		assertEquals(BeMusicDevice.SCRATCH2, dev);
	}

	// getScratch(BeMusicLane)
	// NullPointerException laneがnull
	@Test
	public void testGetScratch_NullLane() {
		assertThrows(NullPointerException.class, () -> BeMusicDevice.getScratch(null));
	}
}
