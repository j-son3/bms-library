package com.lmt.lib.bms.bemusic;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import org.junit.Test;

import com.lmt.lib.bms.BmsAt;
import com.lmt.lib.bms.BmsPoint;

public class BeMusicPointTest {
	private static class VisibleParam {
		BeMusicDevice device;
		BeMusicNoteType noteType;
		int value;
	}

	private static class InvisibleParam {
		BeMusicDevice device;
		int value;
	}

	// BeMusicPoint()
	// 正常：インスタンスが生成できること
	@Test
	public void testBeMusicPoint() {
		new BeMusicPoint();
	}

	// getMeasure()
	// 正常：設定した値が正常に取得できること
	@Test
	public void testGetMeasure() {
		var p = point(BmsPoint.of(100, 0.0), 0.0);
		assertEquals(100, p.getMeasure());
	}

	// getTick()
	// 正常：設定した値が正常に取得できること
	@Test
	public void testGetTick() {
		var p = point(BmsPoint.of(0, 96.0), 0.0);
		assertEquals(96, p.getTick(), 0.0);
	}

	// getTime()
	// 正常：設定した値が正常に取得できること
	@Test
	public void testGetTime() {
		var p = point(BmsPoint.BEGIN, 120.23);
		assertEquals(120.23, 0.0, p.getTime());
	}

	// getVisibleNoteType(BeMusicDevice)
	// 正常：設定した値が正常に取得できること
	@Test
	public void testGetVisibleNoteType_Normal() {
		var p = point(
				visible(BeMusicDevice.SWITCH11, BeMusicNoteType.NONE, 0),
				visible(BeMusicDevice.SWITCH12, BeMusicNoteType.BEAT, 100),
				visible(BeMusicDevice.SWITCH13, BeMusicNoteType.LONG, 200),
				visible(BeMusicDevice.SWITCH25, BeMusicNoteType.LONG_ON, 300),
				visible(BeMusicDevice.SWITCH26, BeMusicNoteType.LONG_OFF, 1295),
				visible(BeMusicDevice.SWITCH27, BeMusicNoteType.CHARGE_OFF, 1294),
				visible(BeMusicDevice.SCRATCH2, BeMusicNoteType.MINE, 400));
		assertEquals(BeMusicNoteType.NONE, p.getVisibleNoteType(BeMusicDevice.SWITCH11));
		assertEquals(BeMusicNoteType.BEAT, p.getVisibleNoteType(BeMusicDevice.SWITCH12));
		assertEquals(BeMusicNoteType.LONG, p.getVisibleNoteType(BeMusicDevice.SWITCH13));
		assertEquals(BeMusicNoteType.LONG_ON, p.getVisibleNoteType(BeMusicDevice.SWITCH25));
		assertEquals(BeMusicNoteType.LONG_OFF, p.getVisibleNoteType(BeMusicDevice.SWITCH26));
		assertEquals(BeMusicNoteType.CHARGE_OFF, p.getVisibleNoteType(BeMusicDevice.SWITCH27));
		assertEquals(BeMusicNoteType.MINE, p.getVisibleNoteType(BeMusicDevice.SCRATCH2));
	}

	// getVisibleNoteType(BeMusicDevice)
	// 正常：ノート設定を全く行わない場合NONEを返すこと
	@Test
	public void testGetVisibleNoteType_Normal_NoSetting() {
		var p = point();
		assertEquals(BeMusicNoteType.NONE, p.getVisibleNoteType(BeMusicDevice.SWITCH13));
	}

	// getVisibleValue(BeMusicDevice)
	// 正常：設定した値が正常に取得できること
	@Test
	public void testGetVisibleValue_Normal() {
		var p = point(
				visible(BeMusicDevice.SWITCH11, BeMusicNoteType.NONE, 0),
				visible(BeMusicDevice.SWITCH12, BeMusicNoteType.BEAT, 100),
				visible(BeMusicDevice.SWITCH13, BeMusicNoteType.LONG, 200),
				visible(BeMusicDevice.SWITCH25, BeMusicNoteType.LONG_ON, 300),
				visible(BeMusicDevice.SWITCH26, BeMusicNoteType.LONG_OFF, 1295),
				visible(BeMusicDevice.SWITCH27, BeMusicNoteType.CHARGE_OFF, 1294),
				visible(BeMusicDevice.SCRATCH2, BeMusicNoteType.MINE, 400));
		assertEquals(0, p.getVisibleValue(BeMusicDevice.SWITCH11));
		assertEquals(100, p.getVisibleValue(BeMusicDevice.SWITCH12));
		assertEquals(200, p.getVisibleValue(BeMusicDevice.SWITCH13));
		assertEquals(300, p.getVisibleValue(BeMusicDevice.SWITCH25));
		assertEquals(1295, p.getVisibleValue(BeMusicDevice.SWITCH26));
		assertEquals(1294, p.getVisibleValue(BeMusicDevice.SWITCH27));
		assertEquals(400, p.getVisibleValue(BeMusicDevice.SCRATCH2));
	}

	// getVisibleValue(BeMusicDevice)
	// 正常：ノート設定を全く行わない場合0を返すこと
	@Test
	public void testGetVisibleValue_Normal_NoSetting() {
		var p = point();
		assertEquals(0, p.getVisibleValue(BeMusicDevice.SWITCH22));
	}

	// getVisibleValue(BeMusicDevice)
	// 音声に関連するノートの情報が欠落することなく取得できること
	@Test
	public void testGetVisibleValue_SoundInfo() {
		var dev = BeMusicDevice.SWITCH11;
		var p = point(visible(dev, BeMusicNoteType.NONE, 0xffffffff));
		assertEquals(RawNotes.MASK_VALUE, p.getVisibleValue(dev) & RawNotes.MASK_VALUE);
	}

	// getNoteCount()
	// 正常：設定した値が正常に取得できること(短押しと長押し開始がカウントされること)
	@Test
	public void testGetNoteCount_Normal() {
		var p = point(
				visible(BeMusicDevice.SWITCH11, BeMusicNoteType.LONG_OFF, 1),
				visible(BeMusicDevice.SWITCH12, BeMusicNoteType.BEAT, 2),
				visible(BeMusicDevice.SWITCH13, BeMusicNoteType.NONE, 0),
				visible(BeMusicDevice.SWITCH14, BeMusicNoteType.LONG_ON, 3),
				visible(BeMusicDevice.SWITCH15, BeMusicNoteType.MINE, 4),
				visible(BeMusicDevice.SWITCH16, BeMusicNoteType.BEAT, 5),
				visible(BeMusicDevice.SWITCH17, BeMusicNoteType.NONE, 0),
				visible(BeMusicDevice.SCRATCH1, BeMusicNoteType.BEAT, 6),
				visible(BeMusicDevice.SWITCH21, BeMusicNoteType.NONE, 0),
				visible(BeMusicDevice.SWITCH22, BeMusicNoteType.LONG_OFF, 7),
				visible(BeMusicDevice.SWITCH23, BeMusicNoteType.LONG_ON, 8),
				visible(BeMusicDevice.SWITCH24, BeMusicNoteType.NONE, 0),
				visible(BeMusicDevice.SWITCH25, BeMusicNoteType.BEAT, 9),
				visible(BeMusicDevice.SWITCH26, BeMusicNoteType.MINE, 10),
				visible(BeMusicDevice.SWITCH27, BeMusicNoteType.NONE, 0),
				visible(BeMusicDevice.SCRATCH2, BeMusicNoteType.LONG_ON, 11));
		assertEquals(7, p.getNoteCount());
	}

	// getNoteCount()
	// 正常：ノート設定を全く行わない場合0を返すこと
	@Test
	public void testGetNoteCount_Normal_NoSetting() {
		var p = point();
		assertEquals(0, p.getNoteCount());
	}

	// getNoteCount()
	// ロングノート終端のノート数カウントが期待通りに行われること
	@Test
	public void testGetNoteCount_LnTail() {
		var p = point(
				visible(BeMusicDevice.SWITCH11, BeMusicNoteType.NONE, 0),
				visible(BeMusicDevice.SWITCH12, BeMusicNoteType.BEAT, 1),
				visible(BeMusicDevice.SWITCH13, BeMusicNoteType.LONG_OFF, 1295),
				visible(BeMusicDevice.SWITCH14, BeMusicNoteType.CHARGE_OFF, 1295),
				visible(BeMusicDevice.SWITCH15, BeMusicNoteType.MINE, 1),
				visible(BeMusicDevice.SWITCH16, BeMusicNoteType.NONE, 0),
				visible(BeMusicDevice.SWITCH17, BeMusicNoteType.NONE, 0),
				visible(BeMusicDevice.SCRATCH1, BeMusicNoteType.NONE, 0),
				visible(BeMusicDevice.SWITCH21, BeMusicNoteType.NONE, 0),
				visible(BeMusicDevice.SWITCH22, BeMusicNoteType.BEAT, 1),
				visible(BeMusicDevice.SWITCH23, BeMusicNoteType.LONG_ON, 1),
				visible(BeMusicDevice.SWITCH24, BeMusicNoteType.LONG_OFF, 1295),
				visible(BeMusicDevice.SWITCH25, BeMusicNoteType.CHARGE_OFF, 1295),
				visible(BeMusicDevice.SWITCH26, BeMusicNoteType.MINE, 1),
				visible(BeMusicDevice.SWITCH27, BeMusicNoteType.NONE, 0),
				visible(BeMusicDevice.SCRATCH2, BeMusicNoteType.NONE, 0));
		assertEquals(5, p.getNoteCount()); // beat * 2 + long_on * 1 + charge_off * 2
	}

	// getNoteCount(BeMusicLane)
	// 主レーンのノート数を正しく取得できること
	@Test
	public void testGetNoteCountBeMusicLane_Primary() {
		var p1 = point(
				visible(BeMusicDevice.SWITCH11, BeMusicNoteType.BEAT, 1),
				visible(BeMusicDevice.SWITCH12, BeMusicNoteType.LONG, 0),
				visible(BeMusicDevice.SWITCH13, BeMusicNoteType.LONG_OFF, 9),
				visible(BeMusicDevice.SWITCH14, BeMusicNoteType.LONG_ON, 2),
				visible(BeMusicDevice.SWITCH15, BeMusicNoteType.CHARGE_OFF, 3),
				visible(BeMusicDevice.SWITCH16, BeMusicNoteType.MINE, 9),
				visible(BeMusicDevice.SWITCH17, BeMusicNoteType.NONE, 0),
				visible(BeMusicDevice.SCRATCH1, BeMusicNoteType.BEAT, 4),
				visible(BeMusicDevice.SWITCH21, BeMusicNoteType.BEAT, 5));
		assertEquals(4, p1.getNoteCount(BeMusicLane.PRIMARY));
		var p2 = point(
				visible(BeMusicDevice.SWITCH21, BeMusicNoteType.BEAT, 1),
				visible(BeMusicDevice.SWITCH22, BeMusicNoteType.LONG, 0),
				visible(BeMusicDevice.SWITCH23, BeMusicNoteType.LONG_OFF, 9),
				visible(BeMusicDevice.SWITCH24, BeMusicNoteType.LONG_ON, 2),
				visible(BeMusicDevice.SWITCH25, BeMusicNoteType.CHARGE_OFF, 3),
				visible(BeMusicDevice.SWITCH26, BeMusicNoteType.MINE, 9),
				visible(BeMusicDevice.SWITCH27, BeMusicNoteType.NONE, 0),
				visible(BeMusicDevice.SCRATCH2, BeMusicNoteType.BEAT, 4),
				visible(BeMusicDevice.SWITCH11, BeMusicNoteType.BEAT, 5));
		assertEquals(1, p2.getNoteCount(BeMusicLane.PRIMARY));
	}

	// getNoteCount(BeMusicLane)
	// 副レーンのノート数を正しく取得できること
	@Test
	public void testGetNoteCountBeMusicLane_Secondary() {
		var p1 = point(
				visible(BeMusicDevice.SWITCH11, BeMusicNoteType.BEAT, 1),
				visible(BeMusicDevice.SWITCH12, BeMusicNoteType.LONG, 0),
				visible(BeMusicDevice.SWITCH13, BeMusicNoteType.LONG_OFF, 9),
				visible(BeMusicDevice.SWITCH14, BeMusicNoteType.LONG_ON, 2),
				visible(BeMusicDevice.SWITCH15, BeMusicNoteType.CHARGE_OFF, 3),
				visible(BeMusicDevice.SWITCH16, BeMusicNoteType.MINE, 9),
				visible(BeMusicDevice.SWITCH17, BeMusicNoteType.NONE, 0),
				visible(BeMusicDevice.SCRATCH1, BeMusicNoteType.BEAT, 4),
				visible(BeMusicDevice.SWITCH21, BeMusicNoteType.BEAT, 5));
		assertEquals(1, p1.getNoteCount(BeMusicLane.SECONDARY));
		var p2 = point(
				visible(BeMusicDevice.SWITCH21, BeMusicNoteType.BEAT, 1),
				visible(BeMusicDevice.SWITCH22, BeMusicNoteType.LONG, 0),
				visible(BeMusicDevice.SWITCH23, BeMusicNoteType.LONG_OFF, 9),
				visible(BeMusicDevice.SWITCH24, BeMusicNoteType.LONG_ON, 2),
				visible(BeMusicDevice.SWITCH25, BeMusicNoteType.CHARGE_OFF, 3),
				visible(BeMusicDevice.SWITCH26, BeMusicNoteType.MINE, 9),
				visible(BeMusicDevice.SWITCH27, BeMusicNoteType.NONE, 0),
				visible(BeMusicDevice.SCRATCH2, BeMusicNoteType.BEAT, 4),
				visible(BeMusicDevice.SWITCH11, BeMusicNoteType.BEAT, 5));
		assertEquals(4, p2.getNoteCount(BeMusicLane.SECONDARY));
	}

	// getNoteCount(BeMusicLane)
	// NullPointerException laneがnull
	@Test
	public void testGetNoteCountBeMusicLane_NullLane() {
		var p = point();
		assertThrows(NullPointerException.class, () -> p.getNoteCount(null));
	}

	// getLongNoteCount()
	// 正常：設定した値が正常に取得できること
	@Test
	public void testGetLongNoteCount_Normal() {
		var p = point(
				visible(BeMusicDevice.SWITCH11, BeMusicNoteType.LONG_OFF, 1),
				visible(BeMusicDevice.SWITCH12, BeMusicNoteType.BEAT, 2),
				visible(BeMusicDevice.SWITCH13, BeMusicNoteType.NONE, 0),
				visible(BeMusicDevice.SWITCH14, BeMusicNoteType.LONG_ON, 3),
				visible(BeMusicDevice.SWITCH15, BeMusicNoteType.MINE, 4),
				visible(BeMusicDevice.SWITCH16, BeMusicNoteType.BEAT, 5),
				visible(BeMusicDevice.SWITCH17, BeMusicNoteType.NONE, 0),
				visible(BeMusicDevice.SCRATCH1, BeMusicNoteType.BEAT, 6),
				visible(BeMusicDevice.SWITCH21, BeMusicNoteType.NONE, 0),
				visible(BeMusicDevice.SWITCH22, BeMusicNoteType.LONG_OFF, 7),
				visible(BeMusicDevice.SWITCH23, BeMusicNoteType.LONG_ON, 8),
				visible(BeMusicDevice.SWITCH24, BeMusicNoteType.NONE, 0),
				visible(BeMusicDevice.SWITCH25, BeMusicNoteType.BEAT, 9),
				visible(BeMusicDevice.SWITCH26, BeMusicNoteType.MINE, 10),
				visible(BeMusicDevice.SWITCH27, BeMusicNoteType.NONE, 0),
				visible(BeMusicDevice.SCRATCH2, BeMusicNoteType.LONG_ON, 11));
		assertEquals(3, p.getLongNoteCount());
	}

	// getLongNoteCount()
	// 正常：ノート設定を全く行わない場合0を返すこと
	@Test
	public void testGetLongNoteCount_Normal_NoSetting() {
		var p = point();
		assertEquals(0, p.getLongNoteCount());
	}

	// getLongNoteCount()
	// ロングノート終端のノート数カウントが期待通りに行われること
	@Test
	public void testGetLongNoteCount_LnTail() {
		var p = point(
				visible(BeMusicDevice.SWITCH11, BeMusicNoteType.NONE, 0),
				visible(BeMusicDevice.SWITCH12, BeMusicNoteType.BEAT, 1),
				visible(BeMusicDevice.SWITCH13, BeMusicNoteType.LONG_OFF, 1295),
				visible(BeMusicDevice.SWITCH14, BeMusicNoteType.CHARGE_OFF, 1295),
				visible(BeMusicDevice.SWITCH15, BeMusicNoteType.MINE, 1),
				visible(BeMusicDevice.SWITCH16, BeMusicNoteType.NONE, 0),
				visible(BeMusicDevice.SWITCH17, BeMusicNoteType.NONE, 0),
				visible(BeMusicDevice.SCRATCH1, BeMusicNoteType.NONE, 0),
				visible(BeMusicDevice.SWITCH21, BeMusicNoteType.NONE, 0),
				visible(BeMusicDevice.SWITCH22, BeMusicNoteType.BEAT, 1),
				visible(BeMusicDevice.SWITCH23, BeMusicNoteType.LONG_ON, 1),
				visible(BeMusicDevice.SWITCH24, BeMusicNoteType.LONG_OFF, 1295),
				visible(BeMusicDevice.SWITCH25, BeMusicNoteType.CHARGE_OFF, 1295),
				visible(BeMusicDevice.SWITCH26, BeMusicNoteType.MINE, 1),
				visible(BeMusicDevice.SWITCH27, BeMusicNoteType.NONE, 0),
				visible(BeMusicDevice.SCRATCH2, BeMusicNoteType.NONE, 0));
		assertEquals(3, p.getLongNoteCount()); // long_on * 1 + charge_off * 2
	}

	// getLongNoteCount(BeMusicLane)
	// 主レーンのロングノート数を正しく取得できること
	@Test
	public void testGetLongNoteCountBeMusicLane_Primary() {
		var p1 = point(
				visible(BeMusicDevice.SWITCH11, BeMusicNoteType.LONG_ON, 1),
				visible(BeMusicDevice.SWITCH12, BeMusicNoteType.LONG, 0),
				visible(BeMusicDevice.SWITCH13, BeMusicNoteType.LONG_OFF, 9),
				visible(BeMusicDevice.SWITCH14, BeMusicNoteType.BEAT, 9),
				visible(BeMusicDevice.SWITCH15, BeMusicNoteType.CHARGE_OFF, 2),
				visible(BeMusicDevice.SWITCH16, BeMusicNoteType.MINE, 9),
				visible(BeMusicDevice.SWITCH17, BeMusicNoteType.NONE, 0),
				visible(BeMusicDevice.SCRATCH1, BeMusicNoteType.LONG_ON, 3),
				visible(BeMusicDevice.SWITCH21, BeMusicNoteType.LONG_ON, 4));
		assertEquals(3, p1.getLongNoteCount(BeMusicLane.PRIMARY));
		var p2 = point(
				visible(BeMusicDevice.SWITCH21, BeMusicNoteType.LONG_ON, 1),
				visible(BeMusicDevice.SWITCH22, BeMusicNoteType.LONG, 0),
				visible(BeMusicDevice.SWITCH23, BeMusicNoteType.LONG_OFF, 9),
				visible(BeMusicDevice.SWITCH24, BeMusicNoteType.BEAT, 9),
				visible(BeMusicDevice.SWITCH25, BeMusicNoteType.CHARGE_OFF, 2),
				visible(BeMusicDevice.SWITCH26, BeMusicNoteType.MINE, 9),
				visible(BeMusicDevice.SWITCH27, BeMusicNoteType.NONE, 0),
				visible(BeMusicDevice.SCRATCH2, BeMusicNoteType.LONG_ON, 3),
				visible(BeMusicDevice.SWITCH11, BeMusicNoteType.LONG_ON, 4));
		assertEquals(1, p2.getLongNoteCount(BeMusicLane.PRIMARY));
	}

	// getLongNoteCount(BeMusicLane)
	// 副レーンのロングノート数を正しく取得できること
	@Test
	public void testGetLongNoteCountBeMusicLane_Secondary() {
		var p1 = point(
				visible(BeMusicDevice.SWITCH11, BeMusicNoteType.LONG_ON, 1),
				visible(BeMusicDevice.SWITCH12, BeMusicNoteType.LONG, 0),
				visible(BeMusicDevice.SWITCH13, BeMusicNoteType.LONG_OFF, 9),
				visible(BeMusicDevice.SWITCH14, BeMusicNoteType.BEAT, 9),
				visible(BeMusicDevice.SWITCH15, BeMusicNoteType.CHARGE_OFF, 2),
				visible(BeMusicDevice.SWITCH16, BeMusicNoteType.MINE, 9),
				visible(BeMusicDevice.SWITCH17, BeMusicNoteType.NONE, 0),
				visible(BeMusicDevice.SCRATCH1, BeMusicNoteType.LONG_ON, 3),
				visible(BeMusicDevice.SWITCH21, BeMusicNoteType.LONG_ON, 4));
		assertEquals(1, p1.getLongNoteCount(BeMusicLane.SECONDARY));
		var p2 = point(
				visible(BeMusicDevice.SWITCH21, BeMusicNoteType.LONG_ON, 1),
				visible(BeMusicDevice.SWITCH22, BeMusicNoteType.LONG, 0),
				visible(BeMusicDevice.SWITCH23, BeMusicNoteType.LONG_OFF, 9),
				visible(BeMusicDevice.SWITCH24, BeMusicNoteType.BEAT, 9),
				visible(BeMusicDevice.SWITCH25, BeMusicNoteType.CHARGE_OFF, 2),
				visible(BeMusicDevice.SWITCH26, BeMusicNoteType.MINE, 9),
				visible(BeMusicDevice.SWITCH27, BeMusicNoteType.NONE, 0),
				visible(BeMusicDevice.SCRATCH2, BeMusicNoteType.LONG_ON, 3),
				visible(BeMusicDevice.SWITCH11, BeMusicNoteType.LONG_ON, 4));
		assertEquals(3, p2.getLongNoteCount(BeMusicLane.SECONDARY));
	}

	// getLongNoteCount(BeMusicLane)
	// NullPointerException laneがnull
	@Test
	public void testGetLongNoteCountBeMusicLane_NullLane() {
		var p = point();
		assertThrows(NullPointerException.class, () -> p.getLongNoteCount(null));
	}

	// getMineCount()
	// 正常：設定した値が正常に取得できること
	@Test
	public void testGetMineCount_Normal() {
		var p = point(
				visible(BeMusicDevice.SWITCH11, BeMusicNoteType.LONG_OFF, 1),
				visible(BeMusicDevice.SWITCH12, BeMusicNoteType.BEAT, 2),
				visible(BeMusicDevice.SWITCH13, BeMusicNoteType.NONE, 0),
				visible(BeMusicDevice.SWITCH14, BeMusicNoteType.LONG_ON, 3),
				visible(BeMusicDevice.SWITCH15, BeMusicNoteType.MINE, 4),
				visible(BeMusicDevice.SWITCH16, BeMusicNoteType.BEAT, 5),
				visible(BeMusicDevice.SWITCH17, BeMusicNoteType.NONE, 0),
				visible(BeMusicDevice.SCRATCH1, BeMusicNoteType.BEAT, 6),
				visible(BeMusicDevice.SWITCH21, BeMusicNoteType.NONE, 0),
				visible(BeMusicDevice.SWITCH22, BeMusicNoteType.LONG_OFF, 7),
				visible(BeMusicDevice.SWITCH23, BeMusicNoteType.LONG_ON, 8),
				visible(BeMusicDevice.SWITCH24, BeMusicNoteType.NONE, 0),
				visible(BeMusicDevice.SWITCH25, BeMusicNoteType.BEAT, 9),
				visible(BeMusicDevice.SWITCH26, BeMusicNoteType.MINE, 10),
				visible(BeMusicDevice.SWITCH27, BeMusicNoteType.NONE, 0),
				visible(BeMusicDevice.SCRATCH2, BeMusicNoteType.LONG_ON, 11));
		assertEquals(2, p.getMineCount());
	}

	// getMineCount()
	// 正常：ノート設定を全く行わない場合0を返すこと
	@Test
	public void testGetMineCount_Normal_NoSetting() {
		var p = point();
		assertEquals(0, p.getMineCount());
	}

	// getMineCount(BeMusicLane)
	// 主レーンの地雷オブジェ数を正しく取得できること
	@Test
	public void testGetMineCountBeMusicLane_Primary() {
		var p1 = point(
				visible(BeMusicDevice.SWITCH11, BeMusicNoteType.MINE, 1),
				visible(BeMusicDevice.SWITCH12, BeMusicNoteType.LONG, 0),
				visible(BeMusicDevice.SWITCH13, BeMusicNoteType.LONG_OFF, 9),
				visible(BeMusicDevice.SWITCH14, BeMusicNoteType.BEAT, 9),
				visible(BeMusicDevice.SWITCH15, BeMusicNoteType.CHARGE_OFF, 9),
				visible(BeMusicDevice.SWITCH16, BeMusicNoteType.MINE, 2),
				visible(BeMusicDevice.SWITCH17, BeMusicNoteType.NONE, 0),
				visible(BeMusicDevice.SCRATCH1, BeMusicNoteType.MINE, 3),
				visible(BeMusicDevice.SWITCH21, BeMusicNoteType.MINE, 4));
		assertEquals(3, p1.getMineCount(BeMusicLane.PRIMARY));
		var p2 = point(
				visible(BeMusicDevice.SWITCH21, BeMusicNoteType.MINE, 1),
				visible(BeMusicDevice.SWITCH22, BeMusicNoteType.LONG, 0),
				visible(BeMusicDevice.SWITCH23, BeMusicNoteType.LONG_OFF, 9),
				visible(BeMusicDevice.SWITCH24, BeMusicNoteType.BEAT, 9),
				visible(BeMusicDevice.SWITCH25, BeMusicNoteType.CHARGE_OFF, 9),
				visible(BeMusicDevice.SWITCH26, BeMusicNoteType.MINE, 2),
				visible(BeMusicDevice.SWITCH27, BeMusicNoteType.NONE, 0),
				visible(BeMusicDevice.SCRATCH2, BeMusicNoteType.MINE, 3),
				visible(BeMusicDevice.SWITCH11, BeMusicNoteType.MINE, 4));
		assertEquals(1, p2.getMineCount(BeMusicLane.PRIMARY));
	}

	// getMineCount(BeMusicLane)
	// 副レーンの地雷オブジェ数を正しく取得できること
	@Test
	public void testGetMineCountBeMusicLane_Sedoncary() {
		var p1 = point(
				visible(BeMusicDevice.SWITCH11, BeMusicNoteType.MINE, 1),
				visible(BeMusicDevice.SWITCH12, BeMusicNoteType.LONG, 0),
				visible(BeMusicDevice.SWITCH13, BeMusicNoteType.LONG_OFF, 9),
				visible(BeMusicDevice.SWITCH14, BeMusicNoteType.BEAT, 9),
				visible(BeMusicDevice.SWITCH15, BeMusicNoteType.CHARGE_OFF, 9),
				visible(BeMusicDevice.SWITCH16, BeMusicNoteType.MINE, 2),
				visible(BeMusicDevice.SWITCH17, BeMusicNoteType.NONE, 0),
				visible(BeMusicDevice.SCRATCH1, BeMusicNoteType.MINE, 3),
				visible(BeMusicDevice.SWITCH21, BeMusicNoteType.MINE, 4));
		assertEquals(1, p1.getMineCount(BeMusicLane.SECONDARY));
		var p2 = point(
				visible(BeMusicDevice.SWITCH21, BeMusicNoteType.MINE, 1),
				visible(BeMusicDevice.SWITCH22, BeMusicNoteType.LONG, 0),
				visible(BeMusicDevice.SWITCH23, BeMusicNoteType.LONG_OFF, 9),
				visible(BeMusicDevice.SWITCH24, BeMusicNoteType.BEAT, 9),
				visible(BeMusicDevice.SWITCH25, BeMusicNoteType.CHARGE_OFF, 9),
				visible(BeMusicDevice.SWITCH26, BeMusicNoteType.MINE, 2),
				visible(BeMusicDevice.SWITCH27, BeMusicNoteType.NONE, 0),
				visible(BeMusicDevice.SCRATCH2, BeMusicNoteType.MINE, 3),
				visible(BeMusicDevice.SWITCH11, BeMusicNoteType.MINE, 4));
		assertEquals(3, p2.getMineCount(BeMusicLane.SECONDARY));
	}

	// getMineCount(BeMusicLane)
	// NullPointerException laneがnull
	@Test
	public void testGetMineCountBeMusicLane_NullLane() {
		var p = point();
		assertThrows(NullPointerException.class, () -> p.getMineCount(null));
	}

	// getVisualEffectCount()
	// 視覚効果を持つノートの総数が取得できること
	@Test
	public void testGetVisualEffectCount_Normal() {
		var p = point(
				visible(BeMusicDevice.SWITCH11, BeMusicNoteType.NONE, 0),
				visible(BeMusicDevice.SWITCH12, BeMusicNoteType.BEAT, 1),
				visible(BeMusicDevice.SWITCH13, BeMusicNoteType.LONG_ON, 1),
				visible(BeMusicDevice.SWITCH14, BeMusicNoteType.LONG, 1),
				visible(BeMusicDevice.SWITCH15, BeMusicNoteType.LONG_OFF, 1),
				visible(BeMusicDevice.SWITCH16, BeMusicNoteType.CHARGE_OFF, 1),
				visible(BeMusicDevice.SWITCH17, BeMusicNoteType.MINE, 1),
				visible(BeMusicDevice.SCRATCH1, BeMusicNoteType.NONE, 0),
				visible(BeMusicDevice.SWITCH21, BeMusicNoteType.NONE, 0),
				visible(BeMusicDevice.SWITCH22, BeMusicNoteType.BEAT, 1),
				visible(BeMusicDevice.SWITCH23, BeMusicNoteType.LONG_ON, 1),
				visible(BeMusicDevice.SWITCH24, BeMusicNoteType.LONG, 1),
				visible(BeMusicDevice.SWITCH25, BeMusicNoteType.LONG_OFF, 1),
				visible(BeMusicDevice.SWITCH26, BeMusicNoteType.CHARGE_OFF, 1),
				visible(BeMusicDevice.SWITCH27, BeMusicNoteType.MINE, 1),
				visible(BeMusicDevice.SCRATCH2, BeMusicNoteType.NONE, 0));
		assertEquals(12, p.getVisualEffectCount());
	}

	// getVisualEffectCount()
	// ノート設定を全く行わない場合0を返すこと
	@Test
	public void testGetVisualEffectCount_NoSetting() {
		var p = point();
		assertEquals(0, p.getVisualEffectCount());
	}

	// getVisualEffectCount(BeMusicLane)
	// 主レーンの視覚効果を持つノート数を正しく取得できること
	@Test
	public void testGetVisualEffectCountBeMusicLane_Primary() {
		var p1 = point(
				visible(BeMusicDevice.SWITCH11, BeMusicNoteType.BEAT, 1),
				visible(BeMusicDevice.SWITCH12, BeMusicNoteType.LONG, 2),
				visible(BeMusicDevice.SWITCH13, BeMusicNoteType.LONG_ON, 3),
				visible(BeMusicDevice.SWITCH14, BeMusicNoteType.LONG_OFF, 4),
				visible(BeMusicDevice.SWITCH15, BeMusicNoteType.CHARGE_OFF, 5),
				visible(BeMusicDevice.SWITCH16, BeMusicNoteType.MINE, 6),
				visible(BeMusicDevice.SWITCH17, BeMusicNoteType.NONE, 0),
				visible(BeMusicDevice.SCRATCH1, BeMusicNoteType.BEAT, 7),
				visible(BeMusicDevice.SWITCH21, BeMusicNoteType.BEAT, 8));
		assertEquals(7, p1.getVisualEffectCount(BeMusicLane.PRIMARY));
		var p2 = point(
				visible(BeMusicDevice.SWITCH21, BeMusicNoteType.BEAT, 1),
				visible(BeMusicDevice.SWITCH22, BeMusicNoteType.LONG, 2),
				visible(BeMusicDevice.SWITCH23, BeMusicNoteType.LONG_ON, 3),
				visible(BeMusicDevice.SWITCH24, BeMusicNoteType.LONG_OFF, 4),
				visible(BeMusicDevice.SWITCH25, BeMusicNoteType.CHARGE_OFF, 5),
				visible(BeMusicDevice.SWITCH26, BeMusicNoteType.MINE, 6),
				visible(BeMusicDevice.SWITCH27, BeMusicNoteType.NONE, 0),
				visible(BeMusicDevice.SCRATCH2, BeMusicNoteType.BEAT, 7),
				visible(BeMusicDevice.SWITCH11, BeMusicNoteType.BEAT, 8));
		assertEquals(1, p2.getVisualEffectCount(BeMusicLane.PRIMARY));
	}

	// getVisualEffectCount(BeMusicLane)
	// 副レーンの視覚効果を持つノート数を正しく取得できること
	@Test
	public void testGetVisualEffectCountBeMusicLane_Secondary() {
		var p1 = point(
				visible(BeMusicDevice.SWITCH11, BeMusicNoteType.BEAT, 1),
				visible(BeMusicDevice.SWITCH12, BeMusicNoteType.LONG, 2),
				visible(BeMusicDevice.SWITCH13, BeMusicNoteType.LONG_ON, 3),
				visible(BeMusicDevice.SWITCH14, BeMusicNoteType.LONG_OFF, 4),
				visible(BeMusicDevice.SWITCH15, BeMusicNoteType.CHARGE_OFF, 5),
				visible(BeMusicDevice.SWITCH16, BeMusicNoteType.MINE, 6),
				visible(BeMusicDevice.SWITCH17, BeMusicNoteType.NONE, 0),
				visible(BeMusicDevice.SCRATCH1, BeMusicNoteType.BEAT, 7),
				visible(BeMusicDevice.SWITCH21, BeMusicNoteType.BEAT, 8));
		assertEquals(1, p1.getVisualEffectCount(BeMusicLane.SECONDARY));
		var p2 = point(
				visible(BeMusicDevice.SWITCH21, BeMusicNoteType.BEAT, 1),
				visible(BeMusicDevice.SWITCH22, BeMusicNoteType.LONG, 2),
				visible(BeMusicDevice.SWITCH23, BeMusicNoteType.LONG_ON, 3),
				visible(BeMusicDevice.SWITCH24, BeMusicNoteType.LONG_OFF, 4),
				visible(BeMusicDevice.SWITCH25, BeMusicNoteType.CHARGE_OFF, 5),
				visible(BeMusicDevice.SWITCH26, BeMusicNoteType.MINE, 6),
				visible(BeMusicDevice.SWITCH27, BeMusicNoteType.NONE, 0),
				visible(BeMusicDevice.SCRATCH2, BeMusicNoteType.BEAT, 7),
				visible(BeMusicDevice.SWITCH11, BeMusicNoteType.BEAT, 8));
		assertEquals(7, p2.getVisualEffectCount(BeMusicLane.SECONDARY));
	}

	// getVisualEffectCount(BeMusicLane)
	// NullPointerException laneがnull
	@Test
	public void testGetVisualEffectCountBeMusicLane_NullLane() {
		var p = point();
		assertThrows(NullPointerException.class, () -> p.getVisualEffectCount(null));
	}

	// hasMovementNote()
	// ノートなしはfalseを返すこと
	@Test
	public void testHasMovementNote_None() {
		var p = point();
		assertFalse(p.hasMovementNote());
	}

	// hasMovementNote()
	// 短押しはtrueを返すこと
	@Test
	public void testHasMovementNote_HasBeat() {
		var p = point(visible(BeMusicDevice.SWITCH11, BeMusicNoteType.BEAT, 1));
		assertTrue(p.hasMovementNote());
	}

	// hasMovementNote()
	// 長押し開始はtrueを返すこと
	@Test
	public void testHasMovementNote_HasLongOn() {
		var p = point(visible(BeMusicDevice.SWITCH14, BeMusicNoteType.LONG_ON, 1));
		assertTrue(p.hasMovementNote());
	}

	// hasMovementNote()
	// 長押し継続はfalseを返すこと
	@Test
	public void testHasMovementNote_HasLong() {
		var p = point(visible(BeMusicDevice.SWITCH17, BeMusicNoteType.LONG, 1));
		assertFalse(p.hasMovementNote());
	}

	// hasMovementNote()
	// 長押し終了はtrueを返すこと
	@Test
	public void testHasMovementNote_HasLongOff() {
		var p = point(visible(BeMusicDevice.SWITCH21, BeMusicNoteType.LONG_OFF, 1));
		assertTrue(p.hasMovementNote());
	}

	// hasMovementNote()
	// 長押し終了(Charge)はtrueを返すこと
	@Test
	public void testHasMovementNote_HasChargeOff() {
		var p = point(visible(BeMusicDevice.SWITCH24, BeMusicNoteType.CHARGE_OFF, 1));
		assertTrue(p.hasMovementNote());
	}

	// hasMovementNote()
	// 地雷はfalseを返すこと
	@Test
	public void testHasMovementNote_HasMine() {
		var p = point(visible(BeMusicDevice.SWITCH27, BeMusicNoteType.MINE, 1));
		assertFalse(p.hasMovementNote());
	}

	// hasMovementNote(BeMusicLane)
	// 主レーンの何らかの操作を伴うノートの有無を正しく取得できること
	@Test
	public void testHasMovementNoteBeMusicLane_Primary() {
		var lane = BeMusicLane.PRIMARY;
		assertFalse(point(visible(BeMusicDevice.SWITCH11, BeMusicNoteType.NONE, 1)).hasMovementNote(lane));
		assertTrue(point(visible(BeMusicDevice.SWITCH12, BeMusicNoteType.BEAT, 1)).hasMovementNote(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH13, BeMusicNoteType.LONG, 1)).hasMovementNote(lane));
		assertTrue(point(visible(BeMusicDevice.SWITCH14, BeMusicNoteType.LONG_ON, 1)).hasMovementNote(lane));
		assertTrue(point(visible(BeMusicDevice.SWITCH15, BeMusicNoteType.LONG_OFF, 1)).hasMovementNote(lane));
		assertTrue(point(visible(BeMusicDevice.SWITCH16, BeMusicNoteType.CHARGE_OFF, 1)).hasMovementNote(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH17, BeMusicNoteType.MINE, 1)).hasMovementNote(lane));
		assertTrue(point(visible(BeMusicDevice.SCRATCH1, BeMusicNoteType.BEAT, 1)).hasMovementNote(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH21, BeMusicNoteType.NONE, 1)).hasMovementNote(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH22, BeMusicNoteType.BEAT, 1)).hasMovementNote(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH23, BeMusicNoteType.LONG, 1)).hasMovementNote(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH24, BeMusicNoteType.LONG_ON, 1)).hasMovementNote(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH25, BeMusicNoteType.LONG_OFF, 1)).hasMovementNote(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH26, BeMusicNoteType.CHARGE_OFF, 1)).hasMovementNote(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH27, BeMusicNoteType.MINE, 1)).hasMovementNote(lane));
		assertFalse(point(visible(BeMusicDevice.SCRATCH2, BeMusicNoteType.BEAT, 1)).hasMovementNote(lane));
	}

	// hasMovementNote(BeMusicLane)
	// 副レーンの何らかの操作を伴うノートの有無を正しく取得できること
	@Test
	public void testHasMovementNoteBeMusicLane_Secondary() {
		var lane = BeMusicLane.SECONDARY;
		assertFalse(point(visible(BeMusicDevice.SWITCH11, BeMusicNoteType.NONE, 1)).hasMovementNote(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH12, BeMusicNoteType.BEAT, 1)).hasMovementNote(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH13, BeMusicNoteType.LONG, 1)).hasMovementNote(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH14, BeMusicNoteType.LONG_ON, 1)).hasMovementNote(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH15, BeMusicNoteType.LONG_OFF, 1)).hasMovementNote(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH16, BeMusicNoteType.CHARGE_OFF, 1)).hasMovementNote(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH17, BeMusicNoteType.MINE, 1)).hasMovementNote(lane));
		assertFalse(point(visible(BeMusicDevice.SCRATCH1, BeMusicNoteType.BEAT, 1)).hasMovementNote(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH21, BeMusicNoteType.NONE, 1)).hasMovementNote(lane));
		assertTrue(point(visible(BeMusicDevice.SWITCH22, BeMusicNoteType.BEAT, 1)).hasMovementNote(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH23, BeMusicNoteType.LONG, 1)).hasMovementNote(lane));
		assertTrue(point(visible(BeMusicDevice.SWITCH24, BeMusicNoteType.LONG_ON, 1)).hasMovementNote(lane));
		assertTrue(point(visible(BeMusicDevice.SWITCH25, BeMusicNoteType.LONG_OFF, 1)).hasMovementNote(lane));
		assertTrue(point(visible(BeMusicDevice.SWITCH26, BeMusicNoteType.CHARGE_OFF, 1)).hasMovementNote(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH27, BeMusicNoteType.MINE, 1)).hasMovementNote(lane));
		assertTrue(point(visible(BeMusicDevice.SCRATCH2, BeMusicNoteType.BEAT, 1)).hasMovementNote(lane));
	}

	// hasMovementNote(BeMusicLane)
	// NullPointerException laneがnull
	@Test
	public void testHasMovementNoteBeMusicLane_NullLane() {
		var p = point();
		assertThrows(NullPointerException.class, () -> p.hasMovementNote(null));
	}

	// hasHolding()
	// 短押しはをfalse返すこと
	@Test
	public void testHasHolding_HasBeat() {
		var p = point(visible(BeMusicDevice.SWITCH11, BeMusicNoteType.BEAT, 1));
		assertFalse(p.hasHolding());
	}

	// hasHolding()
	// 長押し開始はfalseを返すこと
	@Test
	public void testHasHolding_HasLongOn() {
		var p = point(visible(BeMusicDevice.SWITCH14, BeMusicNoteType.LONG_ON, 1));
		assertFalse(p.hasHolding());
	}

	// hasHolding()
	// 長押し継続はtrueを返すこと
	@Test
	public void testHasHolding_HasLong() {
		var p = point(visible(BeMusicDevice.SWITCH17, BeMusicNoteType.LONG, 1));
		assertTrue(p.hasHolding());
	}

	// hasHolding()
	// 長押し終了はfalseを返すこと
	@Test
	public void testHasHolding_HasLongOff() {
		var p = point(visible(BeMusicDevice.SWITCH21, BeMusicNoteType.LONG_OFF, 1));
		assertFalse(p.hasHolding());
	}

	// hasHolding()
	// 長押し終了(Charge)はfalseを返すこと
	@Test
	public void testHasHolding_HasChargeOff() {
		var p = point(visible(BeMusicDevice.SWITCH24, BeMusicNoteType.CHARGE_OFF, 1));
		assertFalse(p.hasHolding());
	}

	// hasHolding()
	// 地雷はfalseを返すこと
	@Test
	public void testHasHolding_HasMine() {
		var p = point(visible(BeMusicDevice.SWITCH27, BeMusicNoteType.MINE, 1));
		assertFalse(p.hasHolding());
	}

	// hasHolding(BeMusicLane)
	// 主レーンの長押し継続ノートの有無を正しく取得できること
	@Test
	public void testHasHoldingBeMusicLane_Primary() {
		var lane = BeMusicLane.PRIMARY;
		assertFalse(point(visible(BeMusicDevice.SWITCH11, BeMusicNoteType.NONE, 1)).hasHolding(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH12, BeMusicNoteType.BEAT, 1)).hasHolding(lane));
		assertTrue(point(visible(BeMusicDevice.SWITCH13, BeMusicNoteType.LONG, 1)).hasHolding(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH14, BeMusicNoteType.LONG_ON, 1)).hasHolding(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH15, BeMusicNoteType.LONG_OFF, 1)).hasHolding(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH16, BeMusicNoteType.CHARGE_OFF, 1)).hasHolding(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH17, BeMusicNoteType.MINE, 1)).hasHolding(lane));
		assertFalse(point(visible(BeMusicDevice.SCRATCH1, BeMusicNoteType.BEAT, 1)).hasHolding(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH21, BeMusicNoteType.NONE, 1)).hasHolding(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH22, BeMusicNoteType.BEAT, 1)).hasHolding(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH23, BeMusicNoteType.LONG, 1)).hasHolding(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH24, BeMusicNoteType.LONG_ON, 1)).hasHolding(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH25, BeMusicNoteType.LONG_OFF, 1)).hasHolding(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH26, BeMusicNoteType.CHARGE_OFF, 1)).hasHolding(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH27, BeMusicNoteType.MINE, 1)).hasHolding(lane));
		assertFalse(point(visible(BeMusicDevice.SCRATCH2, BeMusicNoteType.BEAT, 1)).hasHolding(lane));
	}

	// hasHolding(BeMusicLane)
	// 副レーンの長押し継続ノートの有無を正しく取得できること
	@Test
	public void testHasHoldingBeMusicLane_Secondary() {
		var lane = BeMusicLane.SECONDARY;
		assertFalse(point(visible(BeMusicDevice.SWITCH11, BeMusicNoteType.NONE, 1)).hasHolding(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH12, BeMusicNoteType.BEAT, 1)).hasHolding(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH13, BeMusicNoteType.LONG, 1)).hasHolding(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH14, BeMusicNoteType.LONG_ON, 1)).hasHolding(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH15, BeMusicNoteType.LONG_OFF, 1)).hasHolding(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH16, BeMusicNoteType.CHARGE_OFF, 1)).hasHolding(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH17, BeMusicNoteType.MINE, 1)).hasHolding(lane));
		assertFalse(point(visible(BeMusicDevice.SCRATCH1, BeMusicNoteType.BEAT, 1)).hasHolding(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH21, BeMusicNoteType.NONE, 1)).hasHolding(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH22, BeMusicNoteType.BEAT, 1)).hasHolding(lane));
		assertTrue(point(visible(BeMusicDevice.SWITCH23, BeMusicNoteType.LONG, 1)).hasHolding(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH24, BeMusicNoteType.LONG_ON, 1)).hasHolding(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH25, BeMusicNoteType.LONG_OFF, 1)).hasHolding(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH26, BeMusicNoteType.CHARGE_OFF, 1)).hasHolding(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH27, BeMusicNoteType.MINE, 1)).hasHolding(lane));
		assertFalse(point(visible(BeMusicDevice.SCRATCH2, BeMusicNoteType.BEAT, 1)).hasHolding(lane));
	}

	// hasHolding(BeMusicLane)
	// NullPointerException laneがnull
	@Test
	public void testHasHoldingBeMusicLane_NullLane() {
		var p = point();
		assertThrows(NullPointerException.class, () -> p.hasHolding(null));
	}

	// hasLongNoteHead()
	// 短押しはfalseを返すこと
	@Test
	public void testHasLongNoteHead_HasBeat() {
		var p = point(visible(BeMusicDevice.SWITCH11, BeMusicNoteType.BEAT, 1));
		assertFalse(p.hasLongNoteHead());
	}

	// hasLongNoteHead()
	// 長押し開始はtrueを返すこと
	@Test
	public void testHasLongNoteHead_HasLongOn() {
		var p = point(visible(BeMusicDevice.SWITCH14, BeMusicNoteType.LONG_ON, 1));
		assertTrue(p.hasLongNoteHead());
	}

	// hasLongNoteHead()
	// 長押し継続はfalseを返すこと
	@Test
	public void testHasLongNoteHead_HasLong() {
		var p = point(visible(BeMusicDevice.SWITCH17, BeMusicNoteType.LONG, 1));
		assertFalse(p.hasLongNoteHead());
	}

	// hasLongNoteHead()
	// 長押し終了はfalseを返すこと
	@Test
	public void testHasLongNoteHead_HasLongOff() {
		var p = point(visible(BeMusicDevice.SWITCH21, BeMusicNoteType.LONG_OFF, 1));
		assertFalse(p.hasLongNoteHead());
	}

	// hasLongNoteHead()
	// 長押し終了(Charge)はfalseを返すこと
	@Test
	public void testHasLongNoteHead_HasChargeOff() {
		var p = point(visible(BeMusicDevice.SWITCH24, BeMusicNoteType.CHARGE_OFF, 1));
		assertFalse(p.hasLongNoteHead());
	}

	// hasLongNoteHead()
	// 地雷はfalseを返すこと
	@Test
	public void testHasLongNoteHead_HasMine() {
		var p = point(visible(BeMusicDevice.SWITCH27, BeMusicNoteType.MINE, 1));
		assertFalse(p.hasLongNoteHead());
	}

	// hasLongNoteHead(BeMusicLane)
	// 主レーンの長押し開始ノートの有無を正しく取得できること
	@Test
	public void testHasLongNoteHeadBeMusicLane_Primary() {
		var lane = BeMusicLane.PRIMARY;
		assertFalse(point(visible(BeMusicDevice.SWITCH11, BeMusicNoteType.NONE, 1)).hasLongNoteHead(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH12, BeMusicNoteType.BEAT, 1)).hasLongNoteHead(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH13, BeMusicNoteType.LONG, 1)).hasLongNoteHead(lane));
		assertTrue(point(visible(BeMusicDevice.SWITCH14, BeMusicNoteType.LONG_ON, 1)).hasLongNoteHead(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH15, BeMusicNoteType.LONG_OFF, 1)).hasLongNoteHead(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH16, BeMusicNoteType.CHARGE_OFF, 1)).hasLongNoteHead(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH17, BeMusicNoteType.MINE, 1)).hasLongNoteHead(lane));
		assertFalse(point(visible(BeMusicDevice.SCRATCH1, BeMusicNoteType.BEAT, 1)).hasLongNoteHead(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH21, BeMusicNoteType.NONE, 1)).hasLongNoteHead(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH22, BeMusicNoteType.BEAT, 1)).hasLongNoteHead(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH23, BeMusicNoteType.LONG, 1)).hasLongNoteHead(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH24, BeMusicNoteType.LONG_ON, 1)).hasLongNoteHead(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH25, BeMusicNoteType.LONG_OFF, 1)).hasLongNoteHead(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH26, BeMusicNoteType.CHARGE_OFF, 1)).hasLongNoteHead(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH27, BeMusicNoteType.MINE, 1)).hasLongNoteHead(lane));
		assertFalse(point(visible(BeMusicDevice.SCRATCH2, BeMusicNoteType.BEAT, 1)).hasLongNoteHead(lane));
	}

	// hasLongNoteHead(BeMusicLane)
	// 副レーンの長押し開始ノートの有無を正しく取得できること
	@Test
	public void testHasLongNoteHeadBeMusicLane_Secondary() {
		var lane = BeMusicLane.SECONDARY;
		assertFalse(point(visible(BeMusicDevice.SWITCH11, BeMusicNoteType.NONE, 1)).hasLongNoteHead(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH12, BeMusicNoteType.BEAT, 1)).hasLongNoteHead(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH13, BeMusicNoteType.LONG, 1)).hasLongNoteHead(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH14, BeMusicNoteType.LONG_ON, 1)).hasLongNoteHead(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH15, BeMusicNoteType.LONG_OFF, 1)).hasLongNoteHead(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH16, BeMusicNoteType.CHARGE_OFF, 1)).hasLongNoteHead(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH17, BeMusicNoteType.MINE, 1)).hasLongNoteHead(lane));
		assertFalse(point(visible(BeMusicDevice.SCRATCH1, BeMusicNoteType.BEAT, 1)).hasLongNoteHead(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH21, BeMusicNoteType.NONE, 1)).hasLongNoteHead(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH22, BeMusicNoteType.BEAT, 1)).hasLongNoteHead(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH23, BeMusicNoteType.LONG, 1)).hasLongNoteHead(lane));
		assertTrue(point(visible(BeMusicDevice.SWITCH24, BeMusicNoteType.LONG_ON, 1)).hasLongNoteHead(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH25, BeMusicNoteType.LONG_OFF, 1)).hasLongNoteHead(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH26, BeMusicNoteType.CHARGE_OFF, 1)).hasLongNoteHead(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH27, BeMusicNoteType.MINE, 1)).hasLongNoteHead(lane));
		assertFalse(point(visible(BeMusicDevice.SCRATCH2, BeMusicNoteType.BEAT, 1)).hasLongNoteHead(lane));
	}

	// hasLongNoteHead(BeMusicLane)
	// NullPointerException laneがnull
	@Test
	public void testHasLongNoteHeadBeMusicLane_NullLane() {
		var p = point();
		assertThrows(NullPointerException.class, () -> p.hasLongNoteHead(null));
	}

	// hasLongNoteTail()
	// 短押しはfalseを返すこと
	@Test
	public void testHasLongNoteTail_HasBeat() {
		var p = point(visible(BeMusicDevice.SWITCH11, BeMusicNoteType.BEAT, 1));
		assertFalse(p.hasLongNoteTail());
	}

	// hasLongNoteTail()
	// 長押し開始はfalseを返すこと
	@Test
	public void testHasLongNoteTail_HasLongOn() {
		var p = point(visible(BeMusicDevice.SWITCH14, BeMusicNoteType.LONG_ON, 1));
		assertFalse(p.hasLongNoteTail());
	}

	// hasLongNoteTail()
	// 長押し継続はfalseを返すこと
	@Test
	public void testHasLongNoteTail_HasLong() {
		var p = point(visible(BeMusicDevice.SWITCH17, BeMusicNoteType.LONG, 1));
		assertFalse(p.hasLongNoteTail());
	}

	// hasLongNoteTail()
	// 長押し終了はtrueを返すこと
	@Test
	public void testHasLongNoteTail_HasLongOff() {
		var p = point(visible(BeMusicDevice.SWITCH21, BeMusicNoteType.LONG_OFF, 1));
		assertTrue(p.hasLongNoteTail());
	}

	// hasLongNoteTail()
	// 長押し終了(Charge)はtrueを返すこと
	@Test
	public void testHasLongNoteTail_HasChargeOff() {
		var p = point(visible(BeMusicDevice.SWITCH24, BeMusicNoteType.CHARGE_OFF, 1));
		assertTrue(p.hasLongNoteTail());
	}

	// hasLongNoteTail()
	// 地雷はfalseを返すこと
	@Test
	public void testHasLongNoteTail_HasMine() {
		var p = point(visible(BeMusicDevice.SWITCH27, BeMusicNoteType.MINE, 1));
		assertFalse(p.hasLongNoteTail());
	}

	// hasLongNoteTail(BeMusicLane)
	// 主レーンの長押し終了ノートの有無を正しく取得できること
	@Test
	public void testHasLongNoteTailBeMusicLane_Primary() {
		var lane = BeMusicLane.PRIMARY;
		assertFalse(point(visible(BeMusicDevice.SWITCH11, BeMusicNoteType.NONE, 1)).hasLongNoteTail(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH12, BeMusicNoteType.BEAT, 1)).hasLongNoteTail(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH13, BeMusicNoteType.LONG, 1)).hasLongNoteTail(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH14, BeMusicNoteType.LONG_ON, 1)).hasLongNoteTail(lane));
		assertTrue(point(visible(BeMusicDevice.SWITCH15, BeMusicNoteType.LONG_OFF, 1)).hasLongNoteTail(lane));
		assertTrue(point(visible(BeMusicDevice.SWITCH16, BeMusicNoteType.CHARGE_OFF, 1)).hasLongNoteTail(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH17, BeMusicNoteType.MINE, 1)).hasLongNoteTail(lane));
		assertFalse(point(visible(BeMusicDevice.SCRATCH1, BeMusicNoteType.BEAT, 1)).hasLongNoteTail(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH21, BeMusicNoteType.NONE, 1)).hasLongNoteTail(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH22, BeMusicNoteType.BEAT, 1)).hasLongNoteTail(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH23, BeMusicNoteType.LONG, 1)).hasLongNoteTail(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH24, BeMusicNoteType.LONG_ON, 1)).hasLongNoteTail(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH25, BeMusicNoteType.LONG_OFF, 1)).hasLongNoteTail(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH26, BeMusicNoteType.CHARGE_OFF, 1)).hasLongNoteTail(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH27, BeMusicNoteType.MINE, 1)).hasLongNoteTail(lane));
		assertFalse(point(visible(BeMusicDevice.SCRATCH2, BeMusicNoteType.BEAT, 1)).hasLongNoteTail(lane));
	}

	// hasLongNoteTail(BeMusicLane)
	// 副レーンの長押し終了ノートの有無を正しく取得できること
	@Test
	public void testHasLongNoteTailBeMusicLane_Secondary() {
		var lane = BeMusicLane.SECONDARY;
		assertFalse(point(visible(BeMusicDevice.SWITCH11, BeMusicNoteType.NONE, 1)).hasLongNoteTail(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH12, BeMusicNoteType.BEAT, 1)).hasLongNoteTail(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH13, BeMusicNoteType.LONG, 1)).hasLongNoteTail(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH14, BeMusicNoteType.LONG_ON, 1)).hasLongNoteTail(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH15, BeMusicNoteType.LONG_OFF, 1)).hasLongNoteTail(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH16, BeMusicNoteType.CHARGE_OFF, 1)).hasLongNoteTail(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH17, BeMusicNoteType.MINE, 1)).hasLongNoteTail(lane));
		assertFalse(point(visible(BeMusicDevice.SCRATCH1, BeMusicNoteType.BEAT, 1)).hasLongNoteTail(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH21, BeMusicNoteType.NONE, 1)).hasLongNoteTail(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH22, BeMusicNoteType.BEAT, 1)).hasLongNoteTail(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH23, BeMusicNoteType.LONG, 1)).hasLongNoteTail(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH24, BeMusicNoteType.LONG_ON, 1)).hasLongNoteTail(lane));
		assertTrue(point(visible(BeMusicDevice.SWITCH25, BeMusicNoteType.LONG_OFF, 1)).hasLongNoteTail(lane));
		assertTrue(point(visible(BeMusicDevice.SWITCH26, BeMusicNoteType.CHARGE_OFF, 1)).hasLongNoteTail(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH27, BeMusicNoteType.MINE, 1)).hasLongNoteTail(lane));
		assertFalse(point(visible(BeMusicDevice.SCRATCH2, BeMusicNoteType.BEAT, 1)).hasLongNoteTail(lane));
	}

	// hasLongNoteTail(BeMusicLane)
	// NullPointerException laneがnull
	@Test
	public void testHasLongNoteTailBeMusicLane_NullLane() {
		var p = point();
		assertThrows(NullPointerException.class, () -> p.hasLongNoteTail(null));
	}

	// hasLongNoteType()
	// 短押しはfalseを返すこと
	@Test
	public void testHasLongNoteType_HasBeat() {
		var p = point(visible(BeMusicDevice.SWITCH11, BeMusicNoteType.BEAT, 1));
		assertFalse(p.hasLongNoteType());
	}

	// hasLongNoteType()
	// 長押し開始はtrueを返すこと
	@Test
	public void testHasLongNoteType_HasLongOn() {
		var p = point(visible(BeMusicDevice.SWITCH14, BeMusicNoteType.LONG_ON, 1));
		assertTrue(p.hasLongNoteType());
	}

	// hasLongNoteType()
	// 長押し継続はtrueを返すこと
	@Test
	public void testHasLongNoteType_HasLong() {
		var p = point(visible(BeMusicDevice.SWITCH17, BeMusicNoteType.LONG, 1));
		assertTrue(p.hasLongNoteType());
	}

	// hasLongNoteType()
	// 長押し終了はtrueを返すこと
	@Test
	public void testHasLongNoteType_HasLongOff() {
		var p = point(visible(BeMusicDevice.SWITCH21, BeMusicNoteType.LONG_OFF, 1));
		assertTrue(p.hasLongNoteType());
	}

	// hasLongNoteType()
	// 長押し終了(Charge)はtrueを返すこと
	@Test
	public void testHasLongNoteType_HasChargeOff() {
		var p = point(visible(BeMusicDevice.SWITCH24, BeMusicNoteType.CHARGE_OFF, 1));
		assertTrue(p.hasLongNoteType());
	}

	// hasLongNoteType()
	// 地雷はfalseを返すこと
	@Test
	public void testHasLongNoteType_HasMine() {
		var p = point(visible(BeMusicDevice.SWITCH27, BeMusicNoteType.MINE, 1));
		assertFalse(p.hasLongNoteType());
	}

	// hasLongNoteType(BeMusicLane)
	// 主レーンの長押し関連ノートの有無を正しく取得できること
	@Test
	public void testHasLongNoteTypeBeMusicLane_Primary() {
		var lane = BeMusicLane.PRIMARY;
		assertFalse(point(visible(BeMusicDevice.SWITCH11, BeMusicNoteType.NONE, 1)).hasLongNoteType(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH12, BeMusicNoteType.BEAT, 1)).hasLongNoteType(lane));
		assertTrue(point(visible(BeMusicDevice.SWITCH13, BeMusicNoteType.LONG, 1)).hasLongNoteType(lane));
		assertTrue(point(visible(BeMusicDevice.SWITCH14, BeMusicNoteType.LONG_ON, 1)).hasLongNoteType(lane));
		assertTrue(point(visible(BeMusicDevice.SWITCH15, BeMusicNoteType.LONG_OFF, 1)).hasLongNoteType(lane));
		assertTrue(point(visible(BeMusicDevice.SWITCH16, BeMusicNoteType.CHARGE_OFF, 1)).hasLongNoteType(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH17, BeMusicNoteType.MINE, 1)).hasLongNoteType(lane));
		assertFalse(point(visible(BeMusicDevice.SCRATCH1, BeMusicNoteType.BEAT, 1)).hasLongNoteType(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH21, BeMusicNoteType.NONE, 1)).hasLongNoteType(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH22, BeMusicNoteType.BEAT, 1)).hasLongNoteType(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH23, BeMusicNoteType.LONG, 1)).hasLongNoteType(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH24, BeMusicNoteType.LONG_ON, 1)).hasLongNoteType(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH25, BeMusicNoteType.LONG_OFF, 1)).hasLongNoteType(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH26, BeMusicNoteType.CHARGE_OFF, 1)).hasLongNoteType(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH27, BeMusicNoteType.MINE, 1)).hasLongNoteType(lane));
		assertFalse(point(visible(BeMusicDevice.SCRATCH2, BeMusicNoteType.BEAT, 1)).hasLongNoteType(lane));
	}

	// hasLongNoteType(BeMusicLane)
	// 副レーンの長押し関連ノートの有無を正しく取得できること
	@Test
	public void testHasLongNoteTypeBeMusicLane_Secondary() {
		var lane = BeMusicLane.SECONDARY;
		assertFalse(point(visible(BeMusicDevice.SWITCH11, BeMusicNoteType.NONE, 1)).hasLongNoteType(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH12, BeMusicNoteType.BEAT, 1)).hasLongNoteType(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH13, BeMusicNoteType.LONG, 1)).hasLongNoteType(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH14, BeMusicNoteType.LONG_ON, 1)).hasLongNoteType(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH15, BeMusicNoteType.LONG_OFF, 1)).hasLongNoteType(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH16, BeMusicNoteType.CHARGE_OFF, 1)).hasLongNoteType(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH17, BeMusicNoteType.MINE, 1)).hasLongNoteType(lane));
		assertFalse(point(visible(BeMusicDevice.SCRATCH1, BeMusicNoteType.BEAT, 1)).hasLongNoteType(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH21, BeMusicNoteType.NONE, 1)).hasLongNoteType(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH22, BeMusicNoteType.BEAT, 1)).hasLongNoteType(lane));
		assertTrue(point(visible(BeMusicDevice.SWITCH23, BeMusicNoteType.LONG, 1)).hasLongNoteType(lane));
		assertTrue(point(visible(BeMusicDevice.SWITCH24, BeMusicNoteType.LONG_ON, 1)).hasLongNoteType(lane));
		assertTrue(point(visible(BeMusicDevice.SWITCH25, BeMusicNoteType.LONG_OFF, 1)).hasLongNoteType(lane));
		assertTrue(point(visible(BeMusicDevice.SWITCH26, BeMusicNoteType.CHARGE_OFF, 1)).hasLongNoteType(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH27, BeMusicNoteType.MINE, 1)).hasLongNoteType(lane));
		assertFalse(point(visible(BeMusicDevice.SCRATCH2, BeMusicNoteType.BEAT, 1)).hasLongNoteType(lane));
	}

	// hasLongNoteType(BeMusicLane)
	// NullPointerException laneがnull
	@Test
	public void testHasLongNoteTypeBeMusicLane_NullLane() {
		var p = point();
		assertThrows(NullPointerException.class, () -> p.hasLongNoteType(null));
	}

	// hasChangeSpeed()
	// BPM変更がある時はtrueを返すこと
	@Test
	public void testHasChangeSpeed_HasBpm() {
		var p = point(pr -> pr.bpm = -180.0);
		assertTrue(p.hasChangeSpeed());
	}

	// hasChangeSpeed()
	// スクロール速度変更がある時はtrueを返すこと
	@Test
	public void testHasChangeSpeed_HasScroll() {
		var p = point(pr -> {
			pr.scroll = 1.2;
			pr.changeScroll = true;
		});
		assertTrue(p.hasChangeSpeed());
	}

	// hasChangeSpeed()
	// BPM変更・スクロール速度変更の両方がある時はtrueを返すこと
	@Test
	public void testHasChangeSpeed_HasBpmAndScroll() {
		var p = point(pr -> {
			pr.bpm = -150.0;
			pr.scroll = 1.3;
			pr.changeScroll = true;
		});
		assertTrue(p.hasChangeSpeed());
	}

	// hasChangeSpeed()
	// BPM変更・スクロール速度変更の両方がない時はfalseを返すこと
	@Test
	public void testHasChangeSpeed_NoChangeSpeed() {
		var p = point(pr -> {
			pr.bpm = 160.0;
			pr.scroll = 1.0;
			pr.changeScroll = false;
		});
		assertFalse(p.hasChangeSpeed());
	}

	// hasGimmick()
	// BPM変更がある時はtrueを返すこと
	@Test
	public void testHasGimmick_HasBpm() {
		var p = point(pr -> pr.bpm = -130.0);
		assertTrue(p.hasGimmick());
	}

	// hasGimmick()
	// スクロール速度変更がある時はtrueを返すこと
	@Test
	public void testHasGimmick_HasScroll() {
		var p = point(pr -> {
			pr.scroll = -1.1;
			pr.changeScroll = true;
		});
		assertTrue(p.hasGimmick());
	}

	// hasGimmick()
	// 譜面停止がある時はtrueを返すこと
	@Test
	public void testHasGimmick_HasStop() {
		var p = point(pr -> pr.stop = 0.5);
		assertTrue(p.hasGimmick());
	}

	// hasGimmick()
	// 地雷オブジェがある時はtrueを返すこと
	@Test
	public void testHasGimmick_HasMine() {
		var p = point(visible(BeMusicDevice.SCRATCH2, BeMusicNoteType.MINE, 1));
		assertTrue(p.hasGimmick());
	}

	// hasGimmick()
	// BPM変更・スクロール速度変更・譜面停止・地雷オブジェが全てない時はfalseを返すこと
	@Test
	public void testHasGimmick_NothingAll() {
		var p = point();
		// 何も設定しない
		assertFalse(p.hasGimmick());
	}

	// hasVisualEffect()
	// ノートなしはfalseを返すこと
	@Test
	public void testHasVisualEffect_None() {
		var p = point();
		assertFalse(p.hasVisualEffect());
	}

	// hasVisualEffect()
	// 短押しはtrueを返すこと
	@Test
	public void testHasVisualEffect_HasBeat() {
		var p = point(visible(BeMusicDevice.SCRATCH1, BeMusicNoteType.BEAT, 1));
		assertTrue(p.hasVisualEffect());
	}

	// hasVisualEffect()
	// 長押し開始はtrueを返すこと
	@Test
	public void testHasVisualEffect_HasLongOn() {
		var p = point(visible(BeMusicDevice.SWITCH13, BeMusicNoteType.LONG_ON, 1));
		assertTrue(p.hasVisualEffect());
	}

	// hasVisualEffect()
	// 長押し継続はtrueを返すこと
	@Test
	public void testHasVisualEffect_HasLong() {
		var p = point(visible(BeMusicDevice.SWITCH16, BeMusicNoteType.LONG, 1));
		assertTrue(p.hasVisualEffect());
	}

	// hasVisualEffect()
	// 長押し終了はtrueを返すこと
	@Test
	public void testHasVisualEffect_HasLongOff() {
		var p = point(visible(BeMusicDevice.SCRATCH2, BeMusicNoteType.LONG_OFF, 1));
		assertTrue(p.hasVisualEffect());
	}

	// hasVisualEffect()
	// 長押し終了(Charge)はtrueを返すこと
	@Test
	public void testHasVisualEffect_HasChargeOff() {
		var p = point(visible(BeMusicDevice.SWITCH23, BeMusicNoteType.CHARGE_OFF, 1));
		assertTrue(p.hasVisualEffect());
	}

	// hasVisualEffect()
	// 地雷はtrueを返すこと
	@Test
	public void testHasVisualEffect_HasMine() {
		var p = point(visible(BeMusicDevice.SWITCH26, BeMusicNoteType.MINE, 1));
		assertTrue(p.hasVisualEffect());
	}

	// hasVisualEffect(BeMusicLane)
	// 主レーンの視覚効果を持つノートの有無を正しく取得できること
	@Test
	public void testHasVisualEffectBeMusicLane_Primary() {
		var lane = BeMusicLane.PRIMARY;
		assertFalse(point(visible(BeMusicDevice.SWITCH11, BeMusicNoteType.NONE, 1)).hasVisualEffect(lane));
		assertTrue(point(visible(BeMusicDevice.SWITCH12, BeMusicNoteType.BEAT, 1)).hasVisualEffect(lane));
		assertTrue(point(visible(BeMusicDevice.SWITCH13, BeMusicNoteType.LONG, 1)).hasVisualEffect(lane));
		assertTrue(point(visible(BeMusicDevice.SWITCH14, BeMusicNoteType.LONG_ON, 1)).hasVisualEffect(lane));
		assertTrue(point(visible(BeMusicDevice.SWITCH15, BeMusicNoteType.LONG_OFF, 1)).hasVisualEffect(lane));
		assertTrue(point(visible(BeMusicDevice.SWITCH16, BeMusicNoteType.CHARGE_OFF, 1)).hasVisualEffect(lane));
		assertTrue(point(visible(BeMusicDevice.SWITCH17, BeMusicNoteType.MINE, 1)).hasVisualEffect(lane));
		assertTrue(point(visible(BeMusicDevice.SCRATCH1, BeMusicNoteType.BEAT, 1)).hasVisualEffect(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH21, BeMusicNoteType.NONE, 1)).hasVisualEffect(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH22, BeMusicNoteType.BEAT, 1)).hasVisualEffect(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH23, BeMusicNoteType.LONG, 1)).hasVisualEffect(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH24, BeMusicNoteType.LONG_ON, 1)).hasVisualEffect(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH25, BeMusicNoteType.LONG_OFF, 1)).hasVisualEffect(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH26, BeMusicNoteType.CHARGE_OFF, 1)).hasVisualEffect(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH27, BeMusicNoteType.MINE, 1)).hasVisualEffect(lane));
		assertFalse(point(visible(BeMusicDevice.SCRATCH2, BeMusicNoteType.BEAT, 1)).hasVisualEffect(lane));
	}

	// hasVisualEffect(BeMusicLane)
	// 副レーンの視覚効果を持つノートの有無を正しく取得できること
	@Test
	public void testHasVisualEffectBeMusicLane_Secondary() {
		var lane = BeMusicLane.SECONDARY;
		assertFalse(point(visible(BeMusicDevice.SWITCH11, BeMusicNoteType.NONE, 1)).hasVisualEffect(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH12, BeMusicNoteType.BEAT, 1)).hasVisualEffect(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH13, BeMusicNoteType.LONG, 1)).hasVisualEffect(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH14, BeMusicNoteType.LONG_ON, 1)).hasVisualEffect(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH15, BeMusicNoteType.LONG_OFF, 1)).hasVisualEffect(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH16, BeMusicNoteType.CHARGE_OFF, 1)).hasVisualEffect(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH17, BeMusicNoteType.MINE, 1)).hasVisualEffect(lane));
		assertFalse(point(visible(BeMusicDevice.SCRATCH1, BeMusicNoteType.BEAT, 1)).hasVisualEffect(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH21, BeMusicNoteType.NONE, 1)).hasVisualEffect(lane));
		assertTrue(point(visible(BeMusicDevice.SWITCH22, BeMusicNoteType.BEAT, 1)).hasVisualEffect(lane));
		assertTrue(point(visible(BeMusicDevice.SWITCH23, BeMusicNoteType.LONG, 1)).hasVisualEffect(lane));
		assertTrue(point(visible(BeMusicDevice.SWITCH24, BeMusicNoteType.LONG_ON, 1)).hasVisualEffect(lane));
		assertTrue(point(visible(BeMusicDevice.SWITCH25, BeMusicNoteType.LONG_OFF, 1)).hasVisualEffect(lane));
		assertTrue(point(visible(BeMusicDevice.SWITCH26, BeMusicNoteType.CHARGE_OFF, 1)).hasVisualEffect(lane));
		assertTrue(point(visible(BeMusicDevice.SWITCH27, BeMusicNoteType.MINE, 1)).hasVisualEffect(lane));
		assertTrue(point(visible(BeMusicDevice.SCRATCH2, BeMusicNoteType.BEAT, 1)).hasVisualEffect(lane));
	}

	// hasVisualEffect(BeMusicLane)
	// NullPointerException laneがnull
	@Test
	public void testHasVisualEffectBeMusicLane_NullLane() {
		var p = point();
		assertThrows(NullPointerException.class, () -> p.hasVisualEffect(null));
	}

	// getInvisibleValue(BeMusicDevice)
	// 正常：設定した値が正常に取得できること
	@Test
	public void testGetInvisibleValue_Normal() {
		var p = point(
				invisible(BeMusicDevice.SWITCH11, 16),
				invisible(BeMusicDevice.SWITCH12, 15),
				invisible(BeMusicDevice.SWITCH13, 14),
				invisible(BeMusicDevice.SWITCH14, 13),
				invisible(BeMusicDevice.SWITCH15, 12),
				invisible(BeMusicDevice.SWITCH16, 11),
				invisible(BeMusicDevice.SWITCH17, 10),
				invisible(BeMusicDevice.SCRATCH1, 9),
				invisible(BeMusicDevice.SWITCH21, 8),
				invisible(BeMusicDevice.SWITCH22, 7),
				invisible(BeMusicDevice.SWITCH23, 6),
				invisible(BeMusicDevice.SWITCH24, 5),
				invisible(BeMusicDevice.SWITCH25, 4),
				invisible(BeMusicDevice.SWITCH26, 3),
				invisible(BeMusicDevice.SWITCH27, 2),
				invisible(BeMusicDevice.SCRATCH2, 1));
		assertEquals(16, p.getInvisibleValue(BeMusicDevice.SWITCH11));
		assertEquals(15, p.getInvisibleValue(BeMusicDevice.SWITCH12));
		assertEquals(14, p.getInvisibleValue(BeMusicDevice.SWITCH13));
		assertEquals(13, p.getInvisibleValue(BeMusicDevice.SWITCH14));
		assertEquals(12, p.getInvisibleValue(BeMusicDevice.SWITCH15));
		assertEquals(11, p.getInvisibleValue(BeMusicDevice.SWITCH16));
		assertEquals(10, p.getInvisibleValue(BeMusicDevice.SWITCH17));
		assertEquals(9, p.getInvisibleValue(BeMusicDevice.SCRATCH1));
		assertEquals(8, p.getInvisibleValue(BeMusicDevice.SWITCH21));
		assertEquals(7, p.getInvisibleValue(BeMusicDevice.SWITCH22));
		assertEquals(6, p.getInvisibleValue(BeMusicDevice.SWITCH23));
		assertEquals(5, p.getInvisibleValue(BeMusicDevice.SWITCH24));
		assertEquals(4, p.getInvisibleValue(BeMusicDevice.SWITCH25));
		assertEquals(3, p.getInvisibleValue(BeMusicDevice.SWITCH26));
		assertEquals(2, p.getInvisibleValue(BeMusicDevice.SWITCH27));
		assertEquals(1, p.getInvisibleValue(BeMusicDevice.SCRATCH2));
	}

	// getInvisibleValue(BeMusicDevice)
	// 正常：ノート設定を全く行わない場合0を返すこと
	@Test
	public void testGetInvisibleValue_Normal_NoSetting() {
		var p = point();
		assertEquals(0, p.getInvisibleValue(BeMusicDevice.SWITCH15));
	}

	// getInvisibleValue(BeMusicDevice)
	// 音声に関連するノートの情報が欠落することなく取得できること
	@Test
	public void testGetInvisibleValue_SoundInfo() {
		var dev = BeMusicDevice.SWITCH11;
		var p = point(invisible(dev, 0xffffffff));
		assertEquals(RawNotes.MASK_VALUE, p.getInvisibleValue(dev) & RawNotes.MASK_VALUE);
	}

	// hasPlayableNote()
	// 短押しがある場合はtrueを返すこと
	@Test
	public void testHasPlayableNote_HasBeat() {
		var p = point(visible(BeMusicDevice.SWITCH13, BeMusicNoteType.BEAT, 1));
		assertTrue("Point has BEAT note, but it judged nothing playables.", p.hasPlayableNote());
	}

	// hasPlayableNote()
	// 長押し継続がある場合はfalseを返すこと
	@Test
	public void testHasPlayableNote_HasLong() {
		var p = point(visible(BeMusicDevice.SCRATCH1, BeMusicNoteType.LONG, 1));
		assertFalse("Point has LONG(Continue) note, but it judged has playables.", p.hasPlayableNote());
	}

	// hasPlayableNote()
	// 長押し開始がある場合はtrueを返すこと
	@Test
	public void testHasPlayableNote_HasLongOn() {
		var p = point(visible(BeMusicDevice.SWITCH25, BeMusicNoteType.LONG_ON, 1));
		assertTrue("Point has LONG_ON note, but it judged nothing playables.", p.hasPlayableNote());
	}

	// hasPlayableNote()
	// 長押し終了がある場合はtrueを返すこと
	@Test
	public void testHasPlayableNote_HasLongOff() {
		var p = point(visible(BeMusicDevice.SCRATCH2, BeMusicNoteType.LONG_OFF, 1));
		assertTrue("Point has LONG_OFF note, but it judged nothing playables.", p.hasPlayableNote());
	}

	// hasPlayableNote()
	// 長押し終了(CHARGE)がある場合はtrueを返すこと
	@Test
	public void testHasPlayableNote_HasChargeOff() {
		var p = point(visible(BeMusicDevice.SCRATCH2, BeMusicNoteType.CHARGE_OFF, 1));
		assertTrue("Point has CHARGE_OFF note, but it judged nothing playables.", p.hasPlayableNote());
	}

	// hasPlayableNote()
	// 地雷オブジェがある場合はtrueを返すこと
	@Test
	public void testHasPlayableNote_HasMine() {
		var p = point(visible(BeMusicDevice.SWITCH17, BeMusicNoteType.MINE, 1));
		assertTrue("Point has MINE note, but it judged nothing playables.", p.hasPlayableNote());
	}

	// hasPlayableNote()
	// 操作なしの場合はfalseを返すこと
	@Test
	public void testHasPlayableNote_AllNone() {
		var p = point();
		assertFalse("Point has not playable, but it judged has playables.", p.hasPlayableNote());
	}

	// hasPlayableNote()
	// trueを返す要素が複数あってもtrueを返すこと
	@Test
	public void testHasPlayableNote_MultiPlayables() {
		var p = point(
				visible(BeMusicDevice.SWITCH12, BeMusicNoteType.BEAT, 1),
				visible(BeMusicDevice.SCRATCH1, BeMusicNoteType.LONG_OFF, 1),
				visible(BeMusicDevice.SWITCH24, BeMusicNoteType.MINE, 1));
		assertTrue("Point has multi playables, but it judged nothing playables.", p.hasPlayableNote());
	}

	// hasPlayableNote(BeMusicLane)
	// 主レーンの操作可能ノートの有無を正しく取得できること
	@Test
	public void testHasPlayableNoteBeMusicLane_Primary() {
		var lane = BeMusicLane.PRIMARY;
		assertFalse(point(visible(BeMusicDevice.SWITCH11, BeMusicNoteType.NONE, 1)).hasPlayableNote(lane));
		assertTrue(point(visible(BeMusicDevice.SWITCH12, BeMusicNoteType.BEAT, 1)).hasPlayableNote(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH13, BeMusicNoteType.LONG, 1)).hasPlayableNote(lane));
		assertTrue(point(visible(BeMusicDevice.SWITCH14, BeMusicNoteType.LONG_ON, 1)).hasPlayableNote(lane));
		assertTrue(point(visible(BeMusicDevice.SWITCH15, BeMusicNoteType.LONG_OFF, 1)).hasPlayableNote(lane));
		assertTrue(point(visible(BeMusicDevice.SWITCH16, BeMusicNoteType.CHARGE_OFF, 1)).hasPlayableNote(lane));
		assertTrue(point(visible(BeMusicDevice.SWITCH17, BeMusicNoteType.MINE, 1)).hasPlayableNote(lane));
		assertTrue(point(visible(BeMusicDevice.SCRATCH1, BeMusicNoteType.BEAT, 1)).hasPlayableNote(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH21, BeMusicNoteType.NONE, 1)).hasPlayableNote(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH22, BeMusicNoteType.BEAT, 1)).hasPlayableNote(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH23, BeMusicNoteType.LONG, 1)).hasPlayableNote(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH24, BeMusicNoteType.LONG_ON, 1)).hasPlayableNote(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH25, BeMusicNoteType.LONG_OFF, 1)).hasPlayableNote(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH26, BeMusicNoteType.CHARGE_OFF, 1)).hasPlayableNote(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH27, BeMusicNoteType.MINE, 1)).hasPlayableNote(lane));
		assertFalse(point(visible(BeMusicDevice.SCRATCH2, BeMusicNoteType.BEAT, 1)).hasPlayableNote(lane));
	}

	// hasPlayableNote(BeMusicLane)
	// 副レーンの操作可能ノートの有無を正しく取得できること
	@Test
	public void testHasPlayableNoteBeMusicLane_Secondary() {
		var lane = BeMusicLane.SECONDARY;
		assertFalse(point(visible(BeMusicDevice.SWITCH11, BeMusicNoteType.NONE, 1)).hasPlayableNote(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH12, BeMusicNoteType.BEAT, 1)).hasPlayableNote(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH13, BeMusicNoteType.LONG, 1)).hasPlayableNote(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH14, BeMusicNoteType.LONG_ON, 1)).hasPlayableNote(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH15, BeMusicNoteType.LONG_OFF, 1)).hasPlayableNote(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH16, BeMusicNoteType.CHARGE_OFF, 1)).hasPlayableNote(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH17, BeMusicNoteType.MINE, 1)).hasPlayableNote(lane));
		assertFalse(point(visible(BeMusicDevice.SCRATCH1, BeMusicNoteType.BEAT, 1)).hasPlayableNote(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH21, BeMusicNoteType.NONE, 1)).hasPlayableNote(lane));
		assertTrue(point(visible(BeMusicDevice.SWITCH22, BeMusicNoteType.BEAT, 1)).hasPlayableNote(lane));
		assertFalse(point(visible(BeMusicDevice.SWITCH23, BeMusicNoteType.LONG, 1)).hasPlayableNote(lane));
		assertTrue(point(visible(BeMusicDevice.SWITCH24, BeMusicNoteType.LONG_ON, 1)).hasPlayableNote(lane));
		assertTrue(point(visible(BeMusicDevice.SWITCH25, BeMusicNoteType.LONG_OFF, 1)).hasPlayableNote(lane));
		assertTrue(point(visible(BeMusicDevice.SWITCH26, BeMusicNoteType.CHARGE_OFF, 1)).hasPlayableNote(lane));
		assertTrue(point(visible(BeMusicDevice.SWITCH27, BeMusicNoteType.MINE, 1)).hasPlayableNote(lane));
		assertTrue(point(visible(BeMusicDevice.SCRATCH2, BeMusicNoteType.BEAT, 1)).hasPlayableNote(lane));
	}

	// hasPlayableNote(BeMusicLane)
	// NullPointerException laneがnull
	@Test
	public void testHasPlayableNoteBeMusicLane_NullLane() {
		var p = point();
		assertThrows(NullPointerException.class, () -> p.hasPlayableNote(null));
	}

	// getMeasureLength()
	// 正常：マイナス値で指定するとプラス値が返ること(明示的指定)
	@Test
	public void testGetMeasureLength_Normal_Explicit() {
		var p = point(pr -> pr.length = -3.4);
		assertEquals(3.4, p.getMeasureLength(), 0.0001);
	}

	// getMeasureLength()
	// 正常：何も指定しないと1が返ること
	@Test
	public void testGetMeasureLength_Normal_Implicit() {
		var p = point();
		assertEquals(1.0, p.getMeasureLength(), 0.0);
	}

	// getCurrentScroll()
	// 正常：設定した値が返ること
	@Test
	public void testGetCurrentScroll_Normal() {
		var scroll = 5.0;
		var p = point(pr -> pr.scroll = scroll);
		assertEquals(scroll, p.getCurrentScroll(), 0.000001);
	}

	// getCurrentScroll()
	// 正常：マイナス値であっても、設定した値がそのまま返ること(スクロール速度はマイナス値もあり得る)
	@Test
	public void testGetCurrentScroll_MinusValue() {
		var scroll = 8.1;
		var p = point(pr -> pr.scroll = scroll);
		assertEquals(scroll, p.getCurrentScroll(), 0.000001);
	}

	// getCurrentBpm()
	// 正常：プラス値で指定するとその値がそのまま返ること(現在BPMの変更なしのケース)
	@Test
	public void testGetCurrentBpm_Normal_PlusValue() {
		var p = point(pr -> pr.bpm = 170.0);
		assertEquals(170.0, p.getCurrentBpm(), 0.0);
	}

	// 正常：マイナス値で指定するとプラス値が返ること(BPM変更ありのケース)
	@Test
	public void testGetCurrentBpm_Normal_MinusValue() {
		var p = point(pr -> pr.bpm = -190.0);
		assertEquals(190.0, p.getCurrentBpm(), 0.0);
	}

	// getCurrentSpeed()
	// BPMプラス指定、スクロール速度未指定の時はBPMの値が返ること
	@Test
	public void testGetCurrentSpeed_PlusBpm_DefaultScroll() {
		var p = point(pr -> pr.bpm = 150.0);
		assertEquals(150.0, p.getCurrentSpeed(), 0.0);
	}

	// getCurrentSpeed()
	// BPMマイナス指定、スクロール速度未指定の時はプラスのBPMの値が返ること
	@Test
	public void testGetCurrentSpeed_MinusBpm_DefaultScroll() {
		var p = point(pr -> pr.bpm = -180.0);
		assertEquals(180.0, p.getCurrentSpeed(), 0.0);
	}

	// getCurrentSpeed()
	// BPMプラス指定、スクロール速度プラス指定の時に期待する値が返ること
	@Test
	public void testGetCurrentSpeed_PlusBpm_PlusScroll() {
		var p = point(pr -> {
			pr.bpm = 140.0;
			pr.scroll = 2.0;
			pr.changeScroll = true;
		});
		assertEquals(280.0, p.getCurrentSpeed(), 0.0);
	}

	// getCurrentSpeed()
	// BPMマイナス指定、スクロール速度プラス指定の時に期待する値が返ること
	@Test
	public void testGetCurrentSpeed_MinusBpm_PlusScroll() {
		var p = point(pr -> {
			pr.bpm = -150.0;
			pr.scroll = 3.0;
			pr.changeScroll = true;
		});
		assertEquals(450.0, p.getCurrentSpeed(), 0.0);
	}

	// getCurrentSpeed()
	// BPMプラス指定、スクロール速度マイナス指定の時に期待する値が返ること
	@Test
	public void testGetCurrentSpeed_PlusBpm_MinusScroll() {
		var p = point(pr -> {
			pr.bpm = 160.0;
			pr.scroll = -1.0;
			pr.changeScroll = true;
		});
		assertEquals(-160.0, p.getCurrentSpeed(), 0.0);
	}

	// getCurrentSpeed()
	// BPMマイナス指定、スクロール速度マイナス指定の時に期待する値が返ること
	@Test
	public void testGetCurrentSpeed_MinusBpm_MinusScroll() {
		var p = point(pr -> {
			pr.bpm = 170.0;
			pr.scroll = -1.5;
			pr.changeScroll = true;
		});
		assertEquals(-255.0, p.getCurrentSpeed(), 0.0);
	}

	// getStop()
	// 正常：設定した値が正常に取得できること
	@Test
	public void testGetStop_Normal() {
		var p = point(pr -> pr.stop = 5.87);
		assertEquals(5.87, p.getStop(), 0.0001);
	}

	// getStop()
	// 正常：何も指定しないと0が返ること
	@Test
	public void testGetStop_Normal_NoSetting() {
		var p = point();
		assertEquals(0.0, p.getStop(), 0.0);
	}

	// getBgmCount()
	// 正常：設定した値が正常に取得できること
	@Test
	public void testGetBgmCount_Normal() {
		var p = point(List.of(10, 20, 30));
		assertEquals(3, p.getBgmCount());
	}

	// getBgmCount()
	// 正常：空リストを指定すると0が返ること
	@Test
	public void testGetBgmCount_Normal_EmptyList() {
		var p = point(Collections.emptyList());
		assertEquals(0, p.getBgmCount());
	}

	// getBgmCount()
	// 正常：何も指定しないと0が返ること
	@Test
	public void testGetBgmCount_Normal_NoSetting() {
		var p = point();
		assertEquals(0, p.getBgmCount());
	}

	// getBgmValue(int)
	// 正常：設定した値が正常に取得できること
	@Test
	public void testGetBgmValue_Normal() {
		var p = point(List.of(10, 20, 30, 40, 50));
		assertEquals(10, p.getBgmValue(0));
		assertEquals(20, p.getBgmValue(1));
		assertEquals(30, p.getBgmValue(2));
		assertEquals(40, p.getBgmValue(3));
		assertEquals(50, p.getBgmValue(4));
	}

	// getBgmValue(int)
	// 音声に関連するノートの情報が欠落することなく取得できること
	@Test
	public void testGetBgmValue_SoundInfo() {
		var p = point(List.of(0xffffffff));
		assertEquals(RawNotes.MASK_VALUE, p.getBgmValue(0) & RawNotes.MASK_VALUE);
	}

	// getBgmValue(int)
	// IllegalStateException この楽曲位置にBGMが存在しない
	@Test
	public void testGetBgmValue_Exception_NoBgm() {
		var p = point();
		assertThrows(IllegalStateException.class, () -> { p.getBgmValue(0); });
	}

	// getBgmValue(int)
	// IndexOutOfBoundsException indexがマイナス値
	@Test
	public void testGetBgmValue_Exception_MinusIndex() {
		var p = point(List.of(10, 20, 30, 40, 50));
		assertThrows(IndexOutOfBoundsException.class, () -> { p.getBgmValue(-1); });
	}

	// getBgmValue(int)
	// IndexOutOfBoundsException indexが指定可能範囲超過
	@Test
	public void testGetBgmValue_Exception_OverflowIndex() {
		var l = List.of(10, 20, 30, 40, 50);
		var p = point(l);
		assertThrows(IndexOutOfBoundsException.class, () -> { p.getBgmValue(l.size()); });
	}

	// getBgaValue()
	// 正常：設定した値が正常に取得できること
	@Test
	public void testGetBgaValue_Normal() {
		var p = point(1000, 0, 0);
		assertEquals(1000, p.getBgaValue());
	}

	// getBgaValue()
	// 正常：何も指定しないと0が返ること
	@Test
	public void testGetBgaValue_Normal_NoSetting() {
		var p = point();
		assertEquals(0, p.getBgaValue());
	}

	// getLayerValue()
	// 正常：設定した値が正常に取得できること
	@Test
	public void testGetLayerValue_Normal() {
		var p = point(0, 2000, 0);
		assertEquals(2000, p.getLayerValue());
	}

	// getLayerValue()
	// 正常：何も指定しないと0が返ること
	@Test
	public void testGetLayerValue_Normal_NoSetting() {
		var p = point();
		assertEquals(0, p.getLayerValue());
	}

	// getMissValue()
	// 正常：設定した値が正常に取得できること
	@Test
	public void testGetMissValue_Normal() {
		var p = point(0, 0, 3000);
		assertEquals(3000, p.getMissValue());
	}

	// getMissValue()
	// 正常：何も指定しないと0が返ること
	@Test
	public void testGetMissValue_Normal_NoSetting() {
		var p = point();
		assertEquals(0, p.getMissValue());
	}

	// getText()
	// 正常：設定した値が正常に取得できること
	@Test
	public void testGetText_Normal() {
		var p = point(pr -> pr.text = "text");
		assertEquals("text", p.getText());
	}

	// getText()
	// 正常：何も指定しないと空文字列が返ること
	@Test
	public void testGetText_Normal_NoSetting() {
		var p = point();
		assertEquals("", p.getText());
	}

	// hasMeasureLine()
	// 正常：刻み位置0でtrueになること
	@Test
	public void testHasMeasureLine_Normal_True() {
		var p = point(BmsPoint.of(30, 0.0), 0.0);
		assertTrue(p.hasMeasureLine());
	}

	// hasMeasureLine()
	// 正常：刻み位置が0以外でfalseになること
	@Test
	public void testHasMeasureLine_Normal_False() {
		var p = point(BmsPoint.of(0, 96.0), 0.0);
		assertFalse(p.hasMeasureLine());
	}

	// hasMeasureLength()
	// 正常：明示的に1を指定するとtrueになること
	@Test
	public void testHasMeasureLength_Normal_1Then() {
		var p = point(pr -> pr.length = -1.0);
		assertTrue(p.hasMeasureLength());
	}

	// hasMeasureLength()
	// 正常：明示的に1以外を指定するとtrueになること
	@Test
	public void testHasMeasureLength_Normal_1Else() {
		var p = point(pr -> pr.length = -1.25);
		assertTrue(p.hasMeasureLength());
	}

	// hasMeasureLength()
	// 正常：何も指定しないとfalseになること
	@Test
	public void testHasMeasureLength_Normal_NoSetting() {
		var p = point();
		assertFalse(p.hasMeasureLength());
	}

	// hasScroll()
	// 正常：スクロール速度変更ありにするとtrueになること
	@Test
	public void testHasScroll_True() {
		var p = point(pr -> pr.changeScroll = true);
		assertTrue(p.hasScroll());
	}

	// hasScroll()
	// 正常：スクロール速度変更なしにするとfalseになること
	@Test
	public void testHasScroll_False() {
		var p = point(pr -> pr.changeScroll = false);
		assertFalse(p.hasScroll());
	}

	// hasBpm()
	// 正常：プラス値(BPM変化なし)を指定するとfalseになること
	@Test
	public void testHasBpm_Normal_NoChange() {
		var p = point(pr -> pr.bpm = 120.0);
		assertFalse(p.hasBpm());
	}

	// hasBpm()
	// 正常：マイナス値(BPM変化あり)を指定するとtrueになること
	@Test
	public void testHasBpm_Normal_Change() {
		var p = point(pr -> pr.bpm = -180.0);
		assertTrue(p.hasBpm());
	}

	// hasStop()
	// 正常：0以外の値を指定するとtrueになること
	@Test
	public void testHasStop_Normal_0Else() {
		var p = point(pr -> pr.stop = 0.7);
		assertTrue(p.hasStop());
	}

	// hasStop()
	// 正常：0を指定するとfalseになること
	@Test
	public void testHasStop_Normal_0Then() {
		var p = point(pr -> pr.stop = 0.0);
		assertFalse(p.hasStop());
	}

	// hasStop()
	// 正常：何も指定しないとfalseになること
	@Test
	public void testHasStop_Normal_NoSetting() {
		var p = point();
		assertFalse(p.hasStop());
	}

	// hasMine()
	// 地雷オブジェが1個でもあるとtrueになること
	@Test
	public void testHasMine_HasMine() {
		var p = point(visible(BeMusicDevice.SWITCH21, BeMusicNoteType.MINE, 1));
		assertTrue(p.hasMine());
	}

	// hasMine()
	// 地雷オブジェが0個だとfalseになること
	@Test
	public void testHasMine_NoMine() {
		var p = point(
				visible(BeMusicDevice.SWITCH11, BeMusicNoteType.BEAT, 1),
				visible(BeMusicDevice.SWITCH12, BeMusicNoteType.LONG_ON, 1),
				visible(BeMusicDevice.SWITCH13, BeMusicNoteType.LONG, 1),
				visible(BeMusicDevice.SWITCH14, BeMusicNoteType.LONG_OFF, 1),
				visible(BeMusicDevice.SWITCH15, BeMusicNoteType.CHARGE_OFF, 1));
		assertFalse(p.hasMine());
	}

	// hasMine()
	// 何も指定しないとfalseになること
	@Test
	public void testHasMine_() {
		var p = point();
		// 何も設定しない
		assertFalse(p.hasMine());
	}

	// hasBgm()
	// 1件でもBGMがあるとtrueになること
	@Test
	public void testHasBgm_Exist() {
		var p = point(List.of(10, 20, 30, 40, 50));
		assertTrue(p.hasBgm());
	}

	// hasBgm()
	// 0件リストを指定するとfalseになること
	@Test
	public void testHasBgm_NotExist() {
		var p = point(Collections.emptyList());
		assertFalse(p.hasBgm());
	}

	// hasBgm()
	// 何も指定しないとfalseになること
	@Test
	public void testHasBgm_NoSetting() {
		var p = point();
		assertFalse(p.hasBgm());
	}

	// hasBga
	// 0を指定するとfalseになること
	@Test
	public void testHasBga_Normal_0Then() {
		var p = point();
		assertFalse(p.hasBga());
	}

	// hasBga
	// 0以外を指定するとtrueになること
	@Test
	public void testHasBga_Normal_0Else() {
		var p = point(10, 0, 0);
		assertTrue(p.hasBga());
	}

	// hasBga
	// 何も指定しないとfalseになること
	@Test
	public void testHasBga_Normal_NoSetting() {
		var p = point();
		assertFalse(p.hasBga());
	}

	// hasLayer()
	// 正常：0を指定するとfalseになること
	@Test
	public void testHasLayer_Normal_0Then() {
		var p = point();
		assertFalse(p.hasLayer());
	}

	// hasLayer()
	// 正常：0以外を指定するとtrueになること
	@Test
	public void testHasLayer_Normal_0Else() {
		var p = point(0, 20, 0);
		assertTrue(p.hasLayer());
	}

	// hasLayer()
	// 正常：何も指定しないとfalseになること
	@Test
	public void testHasLayer_Normal_NoSetting() {
		var p = point();
		assertFalse(p.hasLayer());
	}

	// hasMiss()
	// 正常：0を指定するとfalseになること
	@Test
	public void testHasMiss_Normal_0Then() {
		var p = point();
		assertFalse(p.hasMiss());
	}

	// hasMiss()
	// 正常：0以外を指定するとtrueになること
	@Test
	public void testHasMiss_Normal_0Else() {
		var p = point(0, 0, 30);
		assertTrue(p.hasMiss());
	}

	// hasMiss()
	// 正常：何も指定しないとfalseになること
	@Test
	public void testHasMiss_Normal_NoSetting() {
		var p = point();
		assertFalse(p.hasMiss());
	}

	// hasText()
	// 正常：長さ1以上のテキストを設定するとtrueになること
	@Test
	public void testHasText_Normal_True() {
		var p = point(pr -> pr.text = "X");
		assertTrue(p.hasText());
	}

	// hasText()
	// 正常：長さ0のテキストを設定するとfalseになること
	public void testHasText_Normal_False() {
		var p = point(pr -> pr.text = "");
		assertFalse(p.hasText());
	}

	// hasText()
	// 正常：何も設定しないとfalseになること
	public void testHasText_NoSetting() {
		var p = point();
		assertFalse(p.hasText());
	}

	// enumVisibles(IntConsumer)
	// 可視オブジェのみが列挙されること
	@Test
	public void testEnumVisibles_Enum() {
		var p = testEnumSounds_Point(true, true, true);
		var v = new ArrayList<Integer>();
		p.enumVisibles(v::add);
		assertEquals(BeMusicPoint.VC, v.size());
		testEnumSounds_AssertVisibles(v, 0);
	}

	// enumVisibles(IntConsumer)
	// 可視オブジェが未設定だと一度も関数が呼ばれないこと
	@Test
	public void testEnumVisibles_NoVisible() {
		var p = point(
				Collections.emptyList(),
				List.of(invisible(BeMusicDevice.SWITCH12, 11)),
				List.of(21));
		p.enumVisibles(n -> fail("Visible note is nothing, but called action"));
	}

	// enumVisibles(IntConsumer)
	// NullPointerException actionがnull
	@Test
	public void testEnumVisibles_NullAction() {
		var p = point();
		assertThrows(NullPointerException.class, () -> p.enumVisibles(null));
	}

	// enumInvisibles(IntConsumer)
	// 不可視オブジェのみが列挙されること
	@Test
	public void testEnumInvisibles_Enum() {
		var p = testEnumSounds_Point(true, true, true);
		var i = new ArrayList<Integer>();
		p.enumInvisibles(i::add);
		assertEquals(BeMusicPoint.IC, i.size());
		testEnumSounds_AssertInvisibles(i, 0);
	}

	// enumInvisibles(IntConsumer)
	// 不可視オブジェが未設定だと一度も関数が呼ばれないこと
	@Test
	public void testEnumInvisibles_NoInvisible() {
		var p = point(
				List.of(visible(BeMusicDevice.SWITCH11, BeMusicNoteType.BEAT, 1)),
				Collections.emptyList(),
				List.of(21));
		p.enumInvisibles(n -> fail("Invisible note is nothing, but called action"));
	}

	// enumInvisibles(IntConsumer)
	// NullPointerException actionがnull
	@Test
	public void testEnumInvisibles_NullAction() {
		var p = point();
		assertThrows(NullPointerException.class, () -> p.enumInvisibles(null));
	}

	// enumBgms(IntConsumer)
	// BGMのみが列挙されること
	@Test
	public void testEnumBgms_Enum() {
		var p = testEnumSounds_Point(true, true, true);
		var b = new ArrayList<Integer>();
		p.enumBgms(b::add);
		assertEquals(3, b.size());
		testEnumSounds_AssertBgms(b, 0);
	}

	// enumBgms(IntConsumer)
	// BGMが未設定だと一度も関数が呼ばれないこと
	@Test
	public void testEnumBgms_NoBgm() {
		var p = testEnumSounds_Point(true, true, false);
		p.enumBgms(n -> fail("BGM note is nothing, but called action"));
	}

	// enumBgms(IntConsumer)
	// NullPointerException actionがnull
	@Test
	public void testEnumBgms_NullAction() {
		var p = point();
		assertThrows(NullPointerException.class, () -> p.enumBgms(null));
	}

	// enumSounds(IntConsumer)
	// 全てのサウンド関連ノートが可視オブジェ、不可視オブジェ、BGMの順で列挙されること
	@Test
	public void testEnumSounds_Enum() {
		var p = testEnumSounds_Point(true, true, true);
		var s = new ArrayList<Integer>();
		p.enumSounds(s::add);
		testEnumSounds_Assertion(s);
	}

	// enumSounds(IntConsumer)
	// 全てのサウンド関連ノートが未設定だと一度も関数が呼ばれないこと
	@Test
	public void testEnumSounds_NoSound() {
		var p = point(Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
		p.enumSounds(n -> fail("No sound, but called action"));
	}

	// enumSounds(IntConsumer)
	// NullPointerException actionがnull
	@Test
	public void testEnumSounds_NullAction() {
		var p = point();
		assertThrows(NullPointerException.class, () -> p.enumSounds(null));
	}

	// enumSounds(boolean, boolean, boolean, IntConsumer)
	// 選択された種類のノートのみが列挙されること
	@Test
	public void testEnumSounds2_Enum() {
		var p = testEnumSounds_Point(true, true, true);
		var s = new ArrayList<Integer>();
		p.enumSounds(true, false, true, s::add);
		assertEquals(BeMusicPoint.VC + 3, s.size());
		testEnumSounds_AssertVisibles(s, 0);
		testEnumSounds_AssertBgms(s, BeMusicPoint.VC);
	}

	// enumSounds(boolean, boolean, boolean, IntConsumer)
	// 選択した種類のノートが未設定だと関数が呼ばれないこと
	@Test
	public void testEnumSounds2_NoSound() {
		var p = point(Collections.emptyList(), Collections.emptyList(), List.of(21, 22, 23));
		p.enumSounds(true, true, false, s -> fail("Target sound is nothing, but called action"));
	}

	// enumSounds(boolean, boolean, boolean, IntConsumer)
	// 可視オブジェ、不可視オブジェ、BGMの順で列挙されること
	@Test
	public void testEnumSounds2_Order() {
		var p = testEnumSounds_Point(true, true, true);
		var s = new ArrayList<Integer>();
		p.enumSounds(true, true, true, s::add);
		testEnumSounds_Assertion(s);
	}

	// enumSounds(boolean, boolean, boolean, IntConsumer)
	// NullPointerException actionがnull
	@Test
	public void testEnumSounds2_NullAction() {
		var p = point();
		assertThrows(NullPointerException.class, () -> p.enumSounds(true, true, true, null));
	}

	private BeMusicPoint testEnumSounds_Point(boolean visible, boolean invisible, boolean bgms) {
		var v = List.of(
				visible(BeMusicDevice.SWITCH11, BeMusicNoteType.BEAT, 1),
				visible(BeMusicDevice.SWITCH13, BeMusicNoteType.LONG_ON, 2),
				visible(BeMusicDevice.SWITCH15, BeMusicNoteType.MINE, 3));
		var i = List.of(
				invisible(BeMusicDevice.SWITCH12, 11),
				invisible(BeMusicDevice.SWITCH14, 12),
				invisible(BeMusicDevice.SWITCH16, 13));
		var b = List.of(
				21,
				22,
				23);
		var p = point(
				visible ? v : Collections.emptyList(),
				invisible ? i : Collections.emptyList(),
				bgms ? b : Collections.emptyList());
		return p;
	}

	private void testEnumSounds_Assertion(List<Integer> raws) {
		assertEquals(BeMusicPoint.VC + BeMusicPoint.IC + 3, raws.size());
		testEnumSounds_AssertVisibles(raws, 0);
		testEnumSounds_AssertInvisibles(raws, BeMusicPoint.VC);
		testEnumSounds_AssertBgms(raws, BeMusicPoint.VC + BeMusicPoint.IC);
	}

	private void testEnumSounds_AssertVisibles(List<Integer> raws, int offset) {
		assertVisible(raws.get(offset + 0), BeMusicDevice.SWITCH11, BeMusicNoteType.BEAT, 1);
		assertVisible(raws.get(offset + 1), BeMusicDevice.SWITCH12, BeMusicNoteType.NONE, 0);
		assertVisible(raws.get(offset + 2), BeMusicDevice.SWITCH13, BeMusicNoteType.LONG_ON, 2);
		assertVisible(raws.get(offset + 3), BeMusicDevice.SWITCH14, BeMusicNoteType.NONE, 0);
		assertVisible(raws.get(offset + 4), BeMusicDevice.SWITCH15, BeMusicNoteType.MINE, 3);
		assertVisible(raws.get(offset + 5), BeMusicDevice.SWITCH16, BeMusicNoteType.NONE, 0);
		assertVisible(raws.get(offset + 6), BeMusicDevice.SWITCH17, BeMusicNoteType.NONE, 0);
		assertVisible(raws.get(offset + 7), BeMusicDevice.SCRATCH1, BeMusicNoteType.NONE, 0);
		assertVisible(raws.get(offset + 8), BeMusicDevice.SWITCH21, BeMusicNoteType.NONE, 0);
		assertVisible(raws.get(offset + 9), BeMusicDevice.SWITCH22, BeMusicNoteType.NONE, 0);
		assertVisible(raws.get(offset + 10), BeMusicDevice.SWITCH23, BeMusicNoteType.NONE, 0);
		assertVisible(raws.get(offset + 11), BeMusicDevice.SWITCH24, BeMusicNoteType.NONE, 0);
		assertVisible(raws.get(offset + 12), BeMusicDevice.SWITCH25, BeMusicNoteType.NONE, 0);
		assertVisible(raws.get(offset + 13), BeMusicDevice.SWITCH26, BeMusicNoteType.NONE, 0);
		assertVisible(raws.get(offset + 14), BeMusicDevice.SWITCH27, BeMusicNoteType.NONE, 0);
		assertVisible(raws.get(offset + 15), BeMusicDevice.SCRATCH2, BeMusicNoteType.NONE, 0);
	}

	private void testEnumSounds_AssertInvisibles(List<Integer> raws, int offset) {
		assertInvisible(raws.get(offset + 0), BeMusicDevice.SWITCH11, 0);
		assertInvisible(raws.get(offset + 1), BeMusicDevice.SWITCH12, 11);
		assertInvisible(raws.get(offset + 2), BeMusicDevice.SWITCH13, 0);
		assertInvisible(raws.get(offset + 3), BeMusicDevice.SWITCH14, 12);
		assertInvisible(raws.get(offset + 4), BeMusicDevice.SWITCH15, 0);
		assertInvisible(raws.get(offset + 5), BeMusicDevice.SWITCH16, 13);
		assertInvisible(raws.get(offset + 6), BeMusicDevice.SWITCH17, 0);
		assertInvisible(raws.get(offset + 7), BeMusicDevice.SCRATCH1, 0);
		assertInvisible(raws.get(offset + 8), BeMusicDevice.SWITCH21, 0);
		assertInvisible(raws.get(offset + 9), BeMusicDevice.SWITCH22, 0);
		assertInvisible(raws.get(offset + 10), BeMusicDevice.SWITCH23, 0);
		assertInvisible(raws.get(offset + 11), BeMusicDevice.SWITCH24, 0);
		assertInvisible(raws.get(offset + 12), BeMusicDevice.SWITCH25, 0);
		assertInvisible(raws.get(offset + 13), BeMusicDevice.SWITCH26, 0);
		assertInvisible(raws.get(offset + 14), BeMusicDevice.SWITCH27, 0);
		assertInvisible(raws.get(offset + 15), BeMusicDevice.SCRATCH2, 0);
	}

	private void testEnumSounds_AssertBgms(List<Integer> raws, int offset) {
		assertBgm(raws.get(offset + 0), 21);
		assertBgm(raws.get(offset + 1), 22);
		assertBgm(raws.get(offset + 2), 23);
	}

	// visibles()
	// 可視：なし、不可視：なし、BGM：なし　の条件で何も走査されないこと
	@Test
	public void testVisibles_NoVisible_NoInvisible_NoBgm() {
		var p = testSounds_Point(false, false, false);
		var s = p.visibles();
		assertEquals(0, s.count());
	}

	// visibles()
	// 可視：なし、不可視：なし、BGM：あり　の条件で何も走査されないこと
	@Test
	public void testVisibles_NoVisible_NoInvisible_HasBgm() {
		var p = testSounds_Point(false, false, true);
		var s = p.visibles();
		assertEquals(0, s.count());
	}

	// visibles()
	// 可視：なし、不可視：あり、BGM：なし　の条件で何も走査されないこと
	@Test
	public void testVisibles_NoVisible_HasInvisible_NoBgm() {
		var p = testSounds_Point(false, true, false);
		var s = p.visibles();
		assertEquals(0, s.count());
	}

	// visibles()
	// 可視：なし、不可視：あり、BGM：あり　の条件で何も走査されないこと
	@Test
	public void testVisibles_NoVisible_HasInvisible_HasBgm() {
		var p = testSounds_Point(false, true, true);
		var s = p.visibles();
		assertEquals(0, s.count());
	}

	// visibles()
	// 可視：あり、不可視：なし、BGM：なし　の条件で可視オブジェのみが走査されること
	@Test
	public void testVisibles_HasVisible_NoInvisible_NoBgm() {
		var p = testSounds_Point(true, false, false);
		var a = p.visibles().toArray();
		assertEquals(BeMusicPoint.VC, a.length);
		testSounds_AssertVisibles(a, 0);
	}

	// visibles()
	// 可視：あり、不可視：なし、BGM：あり　の条件で可視オブジェのみが走査されること
	@Test
	public void testVisibles_HasVisible_NoInvisible_HasBgm() {
		var p = testSounds_Point(true, false, true);
		var a = p.visibles().toArray();
		assertEquals(BeMusicPoint.VC, a.length);
		testSounds_AssertVisibles(a, 0);
	}

	// visibles()
	// 可視：あり、不可視：あり、BGM：なし　の条件で可視オブジェのみが走査されること
	@Test
	public void testVisibles_HasVisible_HasInvisible_NoBgm() {
		var p = testSounds_Point(true, true, false);
		var a = p.visibles().toArray();
		assertEquals(BeMusicPoint.VC, a.length);
		testSounds_AssertVisibles(a, 0);
	}

	// visibles()
	// 可視：あり、不可視：あり、BGM：あり　の条件で可視オブジェのみが走査されること
	@Test
	public void testVisibles_HasVisible_HasInvisible_HasBgm() {
		var p = testSounds_Point(true, true, true);
		var a = p.visibles().toArray();
		assertEquals(BeMusicPoint.VC, a.length);
		testSounds_AssertVisibles(a, 0);
	}

	// invisibles()
	// 可視：なし、不可視：なし、BGM：なし　の条件で何も走査されないこと
	@Test
	public void testInvisibles_NoVisible_NoInvisible_NoBgm() {
		var p = testSounds_Point(false, false, false);
		var s = p.invisibles();
		assertEquals(0, s.count());
	}

	// invisibles()
	// 可視：なし、不可視：なし、BGM：あり　の条件で何も走査されないこと
	@Test
	public void testInvisibles_NoVisible_NoInvisible_HasBgm() {
		var p = testSounds_Point(false, false, true);
		var s = p.invisibles();
		assertEquals(0, s.count());
	}

	// invisibles()
	// 可視：なし、不可視：あり、BGM：なし　の条件で不可視オブジェのみが走査されること
	@Test
	public void testInvisibles_NoVisible_HasInvisible_NoBgm() {
		var p = testSounds_Point(false, true, false);
		var a = p.invisibles().toArray();
		assertEquals(BeMusicPoint.IC, a.length);
		testSounds_AssertInvisibles(a, 0);
	}

	// invisibles()
	// 可視：なし、不可視：あり、BGM：あり　の条件で不可視オブジェのみが走査されること
	@Test
	public void testInvisibles_NoVisible_HasInvisible_HasBgm() {
		var p = testSounds_Point(false, true, true);
		var a = p.invisibles().toArray();
		assertEquals(BeMusicPoint.IC, a.length);
		testSounds_AssertInvisibles(a, 0);
	}

	// invisibles()
	// 可視：あり、不可視：なし、BGM：なし　の条件で何も走査されないこと
	@Test
	public void testInvisibles_HasVisible_NoInvisible_NoBgm() {
		var p = testSounds_Point(true, false, false);
		var s = p.invisibles();
		assertEquals(0, s.count());
	}

	// invisibles()
	// 可視：あり、不可視：なし、BGM：あり　の条件で何も走査されないこと
	@Test
	public void testInvisibles_HasVisible_NoInvisible_HasBgm() {
		var p = testSounds_Point(true, false, true);
		var s = p.invisibles();
		assertEquals(0, s.count());
	}

	// invisibles()
	// 可視：あり、不可視：あり、BGM：なし　の条件で不可視オブジェのみが走査されること
	@Test
	public void testInvisibles_HasVisible_HasInvisible_NoBgm() {
		var p = testSounds_Point(true, true, false);
		var a = p.invisibles().toArray();
		assertEquals(BeMusicPoint.IC, a.length);
		testSounds_AssertInvisibles(a, 0);
	}

	// invisibles()
	// 可視：あり、不可視：あり、BGM：あり　の条件で不可視オブジェのみが走査されること
	@Test
	public void testInvisibles_HasVisible_HasInvisible_HasBgm() {
		var p = testSounds_Point(true, true, true);
		var a = p.invisibles().toArray();
		assertEquals(BeMusicPoint.IC, a.length);
		testSounds_AssertInvisibles(a, 0);
	}

	// bgms()
	// 可視：なし、不可視：なし、BGM：なし　の条件で何も走査されないこと
	@Test
	public void testBgms_NoVisible_NoInvisible_NoBgm() {
		var p = testSounds_Point(false, false, false);
		var s = p.bgms();
		assertEquals(0, s.count());
	}

	// bgms()
	// 可視：なし、不可視：なし、BGM：あり　の条件でBGMのみが走査されること
	@Test
	public void testBgms_NoVisible_NoInvisible_HasBgm() {
		var p = testSounds_Point(false, false, true);
		var a = p.bgms().toArray();
		assertEquals(2, a.length);
		testSounds_AssertBgms(a, 0);
	}

	// bgms()
	// 可視：なし、不可視：あり、BGM：なし　の条件で何も走査されないこと
	@Test
	public void testBgms_NoVisible_HasInvisible_NoBgm() {
		var p = testSounds_Point(false, true, false);
		var s = p.bgms();
		assertEquals(0, s.count());
	}

	// bgms()
	// 可視：なし、不可視：あり、BGM：あり　の条件でBGMのみが走査されること
	@Test
	public void testBgms_NoVisible_HasInvisible_HasBgm() {
		var p = testSounds_Point(false, true, true);
		var a = p.bgms().toArray();
		assertEquals(2, a.length);
		testSounds_AssertBgms(a, 0);
	}

	// bgms()
	// 可視：あり、不可視：なし、BGM：なし　の条件で何も走査されないこと
	@Test
	public void testBgms_HasVisible_NoInvisible_NoBgm() {
		var p = testSounds_Point(true, false, false);
		var s = p.bgms();
		assertEquals(0, s.count());
	}

	// bgms()
	// 可視：あり、不可視：なし、BGM：あり　の条件でBGMのみが走査されること
	@Test
	public void testBgms_HasVisible_NoInvisible_HasBgm() {
		var p = testSounds_Point(true, false, true);
		var a = p.bgms().toArray();
		assertEquals(2, a.length);
		testSounds_AssertBgms(a, 0);
	}

	// bgms()
	// 可視：あり、不可視：あり、BGM：なし　の条件で何も走査されないこと
	@Test
	public void testBgms_HasVisible_HasInvisible_NoBgm() {
		var p = testSounds_Point(true, true, false);
		var s = p.bgms();
		assertEquals(0, s.count());
	}

	// bgms()
	// 可視：あり、不可視：あり、BGM：あり　の条件でBGMのみが走査されること
	@Test
	public void testBgms_HasVisible_HasInvisible_HasBgm() {
		var p = testSounds_Point(true, true, true);
		var a = p.bgms().toArray();
		assertEquals(2, a.length);
		testSounds_AssertBgms(a, 0);
	}

	// sounds()
	// 可視：なし、不可視：なし、BGM：なし　の条件で何も走査されないこと
	@Test
	public void testSounds_NoVisible_NoInvisible_NoBgm() {
		var p = testSounds_Point(false, false, false);
		var s = p.sounds();
		assertEquals(0, s.count());
	}

	// sounds()
	// 可視：なし、不可視：なし、BGM：あり　の条件でBGMが走査されること
	@Test
	public void testSounds_NoVisible_NoInvisible_HasBgm() {
		var p = testSounds_Point(false, false, true);
		var a = p.sounds().toArray();
		assertEquals(2, a.length);
		testSounds_AssertBgms(a, 0);
	}

	// sounds()
	// 可視：なし、不可視：あり、BGM：なし　の条件で不可視オブジェが走査されること
	@Test
	public void testSounds_NoVisible_HasInvisible_NoBgm() {
		var p = testSounds_Point(false, true, false);
		var a = p.sounds().toArray();
		assertEquals(BeMusicPoint.IC, a.length);
		testSounds_AssertInvisibles(a, 0);
	}

	// sounds()
	// 可視：なし、不可視：あり、BGM：あり　の条件で不可視オブジェ・BGMの順で走査されること
	@Test
	public void testSounds_NoVisible_HasInvisible_HasBgm() {
		var p = testSounds_Point(false, true, true);
		var a = p.sounds().toArray();
		assertEquals(BeMusicPoint.IC + 2, a.length);
		testSounds_AssertInvisibles(a, 0);
		testSounds_AssertBgms(a, BeMusicPoint.IC);
	}

	// sounds()
	// 可視：あり、不可視：なし、BGM：なし　の条件で可視オブジェが走査されること
	@Test
	public void testSounds_HasVisible_NoInvisible_NoBgm() {
		var p = testSounds_Point(true, false, false);
		var a = p.sounds().toArray();
		assertEquals(BeMusicPoint.VC, a.length);
		testSounds_AssertVisibles(a, 0);
	}

	// sounds()
	// 可視：あり、不可視：なし、BGM：あり　の条件で可視オブジェ・BGMの順で走査されること
	@Test
	public void testSounds_HasVisible_NoInvisible_HasBgm() {
		var p = testSounds_Point(true, false, true);
		var a = p.sounds().toArray();
		assertEquals(BeMusicPoint.VC + 2, a.length);
		testSounds_AssertVisibles(a, 0);
		testSounds_AssertBgms(a, BeMusicPoint.VC);
	}

	// sounds()
	// 可視：あり、不可視：あり、BGM：なし　の条件で可視オブジェ・不可視オブジェの順で走査されること
	@Test
	public void testSounds_HasVisible_HasInvisible_NoBgm() {
		var p = testSounds_Point(true, true, false);
		var a = p.sounds().toArray();
		assertEquals(BeMusicPoint.VC + BeMusicPoint.IC, a.length);
		testSounds_AssertVisibles(a, 0);
		testSounds_AssertInvisibles(a, BeMusicPoint.VC);
	}

	// sounds()
	// 可視：あり、不可視：あり、BGM：あり　の条件で可視オブジェ・不可視オブジェ・BGMの順で走査されること
	@Test
	public void testSounds_HasVisible_HasInvisible_HasBgm() {
		var p = testSounds_Point(true, true, true);
		var a = p.sounds().toArray();
		assertEquals(BeMusicPoint.VC + BeMusicPoint.IC + 2, a.length);
		testSounds_AssertVisibles(a, 0);
		testSounds_AssertInvisibles(a, BeMusicPoint.VC);
		testSounds_AssertBgms(a, BeMusicPoint.VC + BeMusicPoint.IC);
	}

	// sounds(boolean, boolean, boolean)
	// 可視オブジェなし、visible=trueで何も走査されないこと
	@Test
	public void testSounds2_NoVisible_True() {
		var p = testSounds_Point(false, true, true);
		var s = p.sounds(true, false, false);
		assertEquals(0, s.count());
	}

	// sounds(boolean, boolean, boolean)
	// 可視オブジェあり、visible=falseで何も走査されないこと
	@Test
	public void testSounds2_HasVisible_False() {
		var p = testSounds_Point(true, false, false);
		var s = p.sounds(false, true, true);
		assertEquals(0, s.count());
	}

	// sounds(boolean, boolean, boolean)
	// 可視オブジェあり、visible=trueで可視オブジェが走査されること
	@Test
	public void testSounds2_HasVisible_True() {
		var p = testSounds_Point(true, false, false);
		var a = p.sounds(true, true, true).toArray();
		assertEquals(BeMusicPoint.VC, a.length);
		testSounds_AssertVisibles(a, 0);
	}

	// sounds(boolean, boolean, boolean)
	// 不可視オブジェなし、invisible=trueで何も走査されないこと
	@Test
	public void testSounds2_NoInvisible_True() {
		var p = testSounds_Point(true, false, true);
		var s = p.sounds(false, true, false);
		assertEquals(0, s.count());
	}

	// sounds(boolean, boolean, boolean)
	// 不可視オブジェあり、invisible=falseで何も走査されないこと
	@Test
	public void testSounds2_HasInvisible_False() {
		var p = testSounds_Point(false, true, false);
		var s = p.sounds(true, false, true);
		assertEquals(0, s.count());
	}

	// sounds(boolean, boolean, boolean)
	// 不可視オブジェあり、invisible=trueで不可視オブジェが走査されること
	@Test
	public void testSounds2_HasInvisible_True() {
		var p = testSounds_Point(false, true, false);
		var a = p.sounds(true, true, true).toArray();
		assertEquals(BeMusicPoint.IC, a.length);
		testSounds_AssertInvisibles(a, 0);
	}

	// sounds(boolean, boolean, boolean)
	// BGMなし、bgm=trueで何も走査されないこと
	@Test
	public void testSounds2_NoBgm_True() {
		var p = testSounds_Point(true, true, false);
		var s = p.sounds(false, false, true);
		assertEquals(0, s.count());
	}

	// sounds(boolean, boolean, boolean)
	// BGMあり、bgm=falseで何も走査されないこと
	@Test
	public void testSounds2_HasBgm_False() {
		var p = testSounds_Point(false, false, true);
		var s = p.sounds(true, true, false);
		assertEquals(0, s.count());
	}

	// sounds(boolean, boolean, boolean)
	// BGMあり、bgm=trueでBGMが走査されること
	@Test
	public void testSounds2_HasBgm_True() {
		var p = testSounds_Point(false, false, true);
		var a = p.sounds(true, true, true).toArray();
		assertEquals(2, a.length);
		testSounds_AssertBgms(a, 0);
	}

	// sounds(boolean, boolean, boolean)
	// 可視：あり、不可視：あり、BGM：あり、全て走査ありで可視オブジェ・不可視オブジェ・BGMの順で走査されること
	@Test
	public void testSounds2_Order() {
		var p = testSounds_Point(true, true, true);
		var a = p.sounds(true, true, true).toArray();
		assertEquals(BeMusicPoint.VC + BeMusicPoint.IC + 2, a.length);
		testSounds_AssertVisibles(a, 0);
		testSounds_AssertInvisibles(a, BeMusicPoint.VC);
		testSounds_AssertBgms(a, BeMusicPoint.VC + BeMusicPoint.IC);
	}

	private BeMusicPoint testSounds_Point(boolean visible, boolean invisible, boolean bgm) {
		var v = List.of(
				visible(BeMusicDevice.SWITCH11, BeMusicNoteType.BEAT, 1),
				visible(BeMusicDevice.SWITCH27, BeMusicNoteType.LONG_ON, 2));
		var i = List.of(invisible(BeMusicDevice.SWITCH12, 11), invisible(BeMusicDevice.SWITCH26, 12));
		var b = List.of(21, 22);
		var p = point(visible ? v : Collections.emptyList(), invisible ? i : Collections.emptyList(), bgm ? b : Collections.emptyList());
		return p;
	}

	private void testSounds_AssertVisibles(int[] raws, int offset) {
		assertVisible(raws[offset + 0], BeMusicDevice.SWITCH11, BeMusicNoteType.BEAT, 1);
		assertVisible(raws[offset + 1], BeMusicDevice.SWITCH12, BeMusicNoteType.NONE, 0);
		assertVisible(raws[offset + 2], BeMusicDevice.SWITCH13, BeMusicNoteType.NONE, 0);
		assertVisible(raws[offset + 3], BeMusicDevice.SWITCH14, BeMusicNoteType.NONE, 0);
		assertVisible(raws[offset + 4], BeMusicDevice.SWITCH15, BeMusicNoteType.NONE, 0);
		assertVisible(raws[offset + 5], BeMusicDevice.SWITCH16, BeMusicNoteType.NONE, 0);
		assertVisible(raws[offset + 6], BeMusicDevice.SWITCH17, BeMusicNoteType.NONE, 0);
		assertVisible(raws[offset + 7], BeMusicDevice.SCRATCH1, BeMusicNoteType.NONE, 0);
		assertVisible(raws[offset + 8], BeMusicDevice.SWITCH21, BeMusicNoteType.NONE, 0);
		assertVisible(raws[offset + 9], BeMusicDevice.SWITCH22, BeMusicNoteType.NONE, 0);
		assertVisible(raws[offset + 10], BeMusicDevice.SWITCH23, BeMusicNoteType.NONE, 0);
		assertVisible(raws[offset + 11], BeMusicDevice.SWITCH24, BeMusicNoteType.NONE, 0);
		assertVisible(raws[offset + 12], BeMusicDevice.SWITCH25, BeMusicNoteType.NONE, 0);
		assertVisible(raws[offset + 13], BeMusicDevice.SWITCH26, BeMusicNoteType.NONE, 0);
		assertVisible(raws[offset + 14], BeMusicDevice.SWITCH27, BeMusicNoteType.LONG_ON, 2);
		assertVisible(raws[offset + 15], BeMusicDevice.SCRATCH2, BeMusicNoteType.NONE, 0);
	}

	private void testSounds_AssertInvisibles(int[] raws, int offset) {
		assertInvisible(raws[offset + 0], BeMusicDevice.SWITCH11, 0);
		assertInvisible(raws[offset + 1], BeMusicDevice.SWITCH12, 11);
		assertInvisible(raws[offset + 2], BeMusicDevice.SWITCH13, 0);
		assertInvisible(raws[offset + 3], BeMusicDevice.SWITCH14, 0);
		assertInvisible(raws[offset + 4], BeMusicDevice.SWITCH15, 0);
		assertInvisible(raws[offset + 5], BeMusicDevice.SWITCH16, 0);
		assertInvisible(raws[offset + 6], BeMusicDevice.SWITCH17, 0);
		assertInvisible(raws[offset + 7], BeMusicDevice.SCRATCH1, 0);
		assertInvisible(raws[offset + 8], BeMusicDevice.SWITCH21, 0);
		assertInvisible(raws[offset + 9], BeMusicDevice.SWITCH22, 0);
		assertInvisible(raws[offset + 10], BeMusicDevice.SWITCH23, 0);
		assertInvisible(raws[offset + 11], BeMusicDevice.SWITCH24, 0);
		assertInvisible(raws[offset + 12], BeMusicDevice.SWITCH25, 0);
		assertInvisible(raws[offset + 13], BeMusicDevice.SWITCH26, 12);
		assertInvisible(raws[offset + 14], BeMusicDevice.SWITCH27, 0);
		assertInvisible(raws[offset + 15], BeMusicDevice.SCRATCH2, 0);
	}

	private void testSounds_AssertBgms(int[] raws, int offset) {
		assertBgm(raws[offset + 0], 21);
		assertBgm(raws[offset + 1], 22);
	}

	private static BeMusicPoint point() {
		return new BeMusicPoint();
	}

	private static BeMusicPoint point(BmsAt at, double time) {
		var p = new BeMusicPoint();
		p.setup(at.getMeasure(), at.getTick(), time, PointProperty.DEFAULT, null, null, null, null, Collections.emptyList());
		return p;
	}

	private static BeMusicPoint point(VisibleParam...visibles) {
		var p = new BeMusicPoint();
		var vt = new BeMusicNoteType[BeMusicPoint.VC];
		var vv = new int[BeMusicPoint.VC];
		Arrays.fill(vt, BeMusicNoteType.NONE);
		Arrays.fill(vv, 0);
		for (var v : visibles) {
			vt[v.device.getIndex()] = v.noteType;
			vv[v.device.getIndex()] = v.value;
		}
		p.setup(0, 0.0, 0.0, PointProperty.DEFAULT, vt, vv, null, null, Collections.emptyList());
		return p;
	}

	private static BeMusicPoint point(InvisibleParam...invisibles) {
		var p = new BeMusicPoint();
		var vi = new int[BeMusicPoint.IC];
		Arrays.fill(vi, 0);
		for (var i : invisibles) { vi[i.device.getIndex()] = i.value; }
		p.setup(0, 0.0, 0.0, PointProperty.DEFAULT, null, null, vi, null, Collections.emptyList());
		return p;
	}

	private static BeMusicPoint point(Consumer<PointProperty> editor) {
		var p = new BeMusicPoint();
		var pr = new PointProperty();
		editor.accept(pr);
		p.setup(0, 0.0, 0.0, pr, null, null, null, null, Collections.emptyList());
		return p;
	}

	private static BeMusicPoint point(List<Integer> bgms) {
		var p = new BeMusicPoint();
		p.setup(0, 0.0, 0.0, PointProperty.DEFAULT, null, null, null, null, bgms);
		return p;
	}

	private static BeMusicPoint point(int bga, int layer, int miss) {
		var p = new BeMusicPoint();
		var bg = new int[] { bga, layer, miss };
		p.setup(0, 0.0, 0.0, PointProperty.DEFAULT, null, null, null, bg, Collections.emptyList());
		return p;
	}

	private static BeMusicPoint point(List<VisibleParam> visibles, List<InvisibleParam> invisibles, List<Integer> bgms) {
		var p = new BeMusicPoint();
		var hasv = !visibles.isEmpty();
		var hasi = !invisibles.isEmpty();
		var vt = new BeMusicNoteType[BeMusicPoint.VC];
		var vv = new int[BeMusicPoint.VC];
		var vi = new int[BeMusicPoint.IC];
		Arrays.fill(vt, BeMusicNoteType.NONE);
		Arrays.fill(vv, 0);
		Arrays.fill(vi, 0);
		visibles.forEach(v -> { vt[v.device.getIndex()] = v.noteType; vv[v.device.getIndex()] = v.value; });
		invisibles.forEach(i -> vi[i.device.getIndex()] = i.value);
		p.setup(0, 0.0, 0.0, PointProperty.DEFAULT, hasv ? vt : null, hasv ? vv : null, hasi ? vi : null, null, bgms);
		return p;
	}

	private static VisibleParam visible(BeMusicDevice device, BeMusicNoteType noteType, int value) {
		var param = new VisibleParam();
		param.device = device;
		param.noteType = noteType;
		param.value = value;
		return param;
	}

	private static InvisibleParam invisible(BeMusicDevice device, int value) {
		var param = new InvisibleParam();
		param.device = device;
		param.value = value;
		return param;
	}

	private static void assertVisible(int raw, BeMusicDevice device, BeMusicNoteType noteType, int trackId) {
		assertTrue(BeMusicSound.isVisible(raw));
		assertEquals(device, BeMusicSound.getDevice(raw));
		assertEquals(noteType, BeMusicSound.getNoteType(raw));
		assertEquals(trackId, BeMusicSound.getTrackId(raw));
	}

	private static void assertInvisible(int raw, BeMusicDevice device, int trackId) {
		assertTrue(BeMusicSound.isInvisible(raw));
		assertEquals(device, BeMusicSound.getDevice(raw));
		assertEquals(BeMusicNoteType.NONE, BeMusicSound.getNoteType(raw));
		assertEquals(trackId, BeMusicSound.getTrackId(raw));
	}

	private static void assertBgm(int raw, int trackId) {
		assertTrue(BeMusicSound.isBgm(raw));
		assertNull(BeMusicSound.getDevice(raw));
		assertEquals(BeMusicNoteType.NONE, BeMusicSound.getNoteType(raw));
		assertEquals(trackId, BeMusicSound.getTrackId(raw));
	}
}
