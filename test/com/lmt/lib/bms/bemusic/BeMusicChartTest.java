package com.lmt.lib.bms.bemusic;

import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.function.IntFunction;
import java.util.stream.Collectors;

import org.junit.Test;

import com.lmt.lib.bms.BmsAt;
import com.lmt.lib.bms.BmsContent;
import com.lmt.lib.bms.BmsPoint;
import com.lmt.lib.bms.BmsStandardLoader;
import com.lmt.lib.bms.UseTestData;

public class BeMusicChartTest implements UseTestData {
	@FunctionalInterface
	private interface PointOfTester {
		int pointOf(BmsContent content, BeMusicChart chart, int measure, double tick);
	}

	private static class TestOnCreateChart extends BeMusicChart {
		boolean isCalledOnCreate = false;

		@Override
		protected void onCreate() {
			isCalledOnCreate = true;
			assertCommonChart(this);  // このメソッドが呼ばれた時点でデータ構築済みであること
		}
	}

	@Override
	public Path testDataDirectory() {
		return Path.of("test", "com", "lmt", "lib", "bms", "bemusic", "testdata", "bemusicchart");
	}

	@Override
	public String testDataExtension() {
		return "bms";
	}

	// BeMusicChart()
	// 正常：インスタンスが生成できること
	@Test
	public void testConstructor() {
		new BeMusicChart();
	}

	// create(List<BeMusicPoint)
	// 正常：正常なリストを指定してBeMusicChartオブジェクトを生成できること
	@Test
	public void testCreate1_Normal() {
		var l = builder(testCommonContent()).createList();
		var s = BeMusicChart.create(l);
		assertCommonChart(s);
	}

	// create(List<BeMusicPoint>)
	// 準正常：空リストを指定してもオブジェクトを生成できること
	@Test
	public void testCreate1_SemiNormal_EmptyList() {
		var l = builder(contentEmpty()).createList();
		var s = BeMusicChart.create(l);
		assertEquals(0, s.getPointCount());
		assertEquals(0, s.getNoteCount());
		assertEquals(0, s.getLongNoteCount());
		assertEquals(0, s.getMineCount());
		assertFalse(s.hasLongNote());
		assertFalse(s.hasMine());
		assertFalse(s.hasBgm());
		assertFalse(s.hasChangeBpm());
		assertFalse(s.hasStop());
		assertFalse(s.hasBga());
	}

	// create(List<BeMusicPoint>)
	// NullPointerException listがnull
	@Test
	public void testCreate1_Exception_NullList() {
		assertThrows(NullPointerException.class, () -> { BeMusicChart.create(null); });
	}

	// create(List<BeMusicPoint>)
	// IllegalArgumentException 楽曲位置情報リストで小節番号・刻み位置が後退した
	@Test
	public void testCreate1_Exception_BackPoint() {
		var l = builder(testCommonContent()).createList();
		var p = new BeMusicPoint();
		var t = l.get(0).getTime() + 0.00000001;
		p.setup(0, 0, t, PointProperty.DEFAULT, null, null, null, null, Collections.emptyList());
		l.add(1, p);
		assertThrows(IllegalArgumentException.class, () -> { BeMusicChart.create(l); });
	}

	// create(List<BeMusicPoint>)
	// IllegalArgumentException 楽曲位置情報リストで時間が後退した
	@Test
	public void testCreate1_Exception_BackTime() {
		var l = builder(testCommonContent()).createList();
		var p = new BeMusicPoint();
		var t = l.get(0).getTime() - 0.00000001;
		p.setup(0, 1.0, t, PointProperty.DEFAULT, null, null, null, null, Collections.emptyList());
		l.add(1, p);
		assertThrows(IllegalArgumentException.class, () -> { BeMusicChart.create(l); });
	}

	// create(List<BeMusicPoint>, Supplier<S>)
	// 正常：正常なリストを指定して拡張BeMusicChartオブジェクトを生成できること
	@Test
	public void testCreate2_Normal() {
		var l = builder(testCommonContent()).createList();
		var s = BeMusicChart.create(l, () -> new BeMusicChart());
		assertCommonChart(s);
	}

	// create(List<BeMusicPoint>, Supplier<S>)
	// 準正常：空リストを指定してもオブジェクトを生成できること
	@Test
	public void testCreate2_SemiNormal_EmptyList() {
		var l = builder(contentEmpty()).createList();
		var s = BeMusicChart.create(l, () -> new BeMusicChart());
		assertEquals(0, s.getPointCount());
		assertEquals(0, s.getNoteCount());
		assertEquals(0, s.getLongNoteCount());
		assertEquals(0, s.getMineCount());
		assertFalse(s.hasLongNote());
		assertFalse(s.hasMine());
		assertFalse(s.hasBgm());
		assertFalse(s.hasChangeBpm());
		assertFalse(s.hasStop());
		assertFalse(s.hasBga());
	}

	// create(List<BeMusicPoint>, Supplier<S>)
	// NullPointerException listがnull
	@Test
	public void testCreate2_Exception_NullList() {
		assertThrows(NullPointerException.class, () -> { BeMusicChart.create(null, () -> new BeMusicChart()); });
	}

	// create(List<BeMusicPoint>, Supplier<S>)
	// NullPointerException creatorがnull
	@Test
	public void testCreate2_Exception_NullCreator() {
		var l = builder(contentEmpty()).createList();
		assertThrows(NullPointerException.class, () -> { BeMusicChart.create(l, null); });
	}

	// create(List<BeMusicPoint>, Supplier<S>)
	// NullPointerException creatorがnullを返した
	@Test
	public void testCreate2_Exception_CreatorReturnedNull() {
		var l = builder(contentEmpty()).createList();
		assertThrows(NullPointerException.class, () -> { BeMusicChart.create(l, null); });
	}

	// create(List<BeMusicPoint>, Supplier<S>)
	// IllegalArgumentException 楽曲位置情報リストで小節番号・刻み位置が後退した
	@Test
	public void testCreate2_Exception_BackPoint() {
		var l = builder(testCommonContent()).createList();
		var p = new BeMusicPoint();
		var t = l.get(0).getTime() + 0.00000001;
		p.setup(0, 0, t, PointProperty.DEFAULT, null, null, null, null, Collections.emptyList());
		l.add(1, p);
		assertThrows(IllegalArgumentException.class, () -> { BeMusicChart.create(l, () -> new BeMusicChart()); });
	}

	// create(List<BeMusicPoint>, Supplier<S>)
	// IllegalArgumentException 楽曲位置情報リストで時間が後退した
	@Test
	public void testCreate2_Exception_BackTime() {
		var l = builder(testCommonContent()).createList();
		var p = new BeMusicPoint();
		var t = l.get(0).getTime() - 0.00000001;
		p.setup(0, 1.0, t, PointProperty.DEFAULT, null, null, null, null, Collections.emptyList());
		l.add(1, p);
		assertThrows(IllegalArgumentException.class, () -> { BeMusicChart.create(l, () -> new BeMusicChart()); });
	}

	// getPoint(int)
	// 正常：インデックスに該当する楽曲位置情報が取得できること
	@Test
	public void testGetPoint_Normal() {
		var s = testCommonChart();
		assertCommonPoints(s.getPointCount(), i -> s.getPoint(i));
	}

	// getPoint(int)
	// IndexOutOfBoundsException indexがマイナス値
	@Test
	public void testGetPoint_Exception_MinusIndex() {
		var s = testCommonChart();
		assertThrows(IndexOutOfBoundsException.class, () -> { s.getPoint(-1); });
	}

	// getPoint(int)
	// IndexOutOfBoundsException indexがgetPointCount()以上
	@Test
	public void testGetPoint_Exception_OverflowIndex() {
		var s = testCommonChart();
		assertThrows(IndexOutOfBoundsException.class, () -> { s.getPoint(s.getPointCount()); });
	}

	// getPoints()
	// 正常：楽曲位置情報リストのコピーが返ること
	@Test
	public void testGetPoints_Normal() {
		var l = builder(testCommonContent()).createList();
		var s = BeMusicChart.create(l);
		var pts = s.getPoints();
		var count = l.size();
		assertEquals(count, pts.size());
		for (var i = 0; i < count; i++) { assertNotSame(l.get(i), pts.get(i)); }
		assertCommonPoints(count, i -> pts.get(i));
	}

	// getPoints()
	// 正常：楽曲位置情報が0件の場合、空のリストが返ること
	@Test
	public void testGetPoints_Normal_Empty() {
		var s = builder(contentEmpty()).createChart();
		var pts = s.getPoints();
		assertNotNull(pts);
		assertTrue(pts.isEmpty());
	}

	// getPointCount()
	// 正常：楽曲位置情報の件数が返ること
	@Test
	public void testGetPointCount_Normal() {
		var s = builder(contentEmpty()).createChart();
		assertEquals(0, s.getPointCount());
		s = testCommonChart();
		assertEquals(58, s.getPointCount());
	}

	// getNoteCount()
	// 正常：全ての楽曲位置情報のノート数の合計値が返ること
	// 短押し、長押し、地雷を含めたDP譜面でノート数の合計値が一致することを確認する
	@Test
	public void testGetNoteCount_Normal() {
		var s = testCommonChart();
		assertEquals(98, s.getNoteCount());
	}

	// getNoteCount(BeMusicLane)
	// 指定レーンのノート数の合計値が返ること
	@Test
	public void testGetNoteCountBeMusicLane_Normal() {
		var c = testCommonChart();
		assertEquals(45, c.getNoteCount(BeMusicLane.PRIMARY));
		assertEquals(53, c.getNoteCount(BeMusicLane.SECONDARY));
	}

	// getNoteCount(BeMusicLane)
	// NullPointerException laneがnull
	@Test
	public void testGetNoteCountBeMusicLane_NullLane() {
		var c = builder(contentEmpty()).createChart();
		assertThrows(NullPointerException.class, () -> c.getNoteCount((BeMusicLane)null));
	}

	// getNoteCount(BeMusicDevice)
	// 正常：全デバイスでノート数合計値が正しい値で返ること
	@Test
	public void testGetNoteCountBeMusicDevice_Normal() {
		var s = testCommonChart();
		assertEquals(7, s.getNoteCount(BeMusicDevice.SWITCH11));
		assertEquals(6, s.getNoteCount(BeMusicDevice.SWITCH12));
		assertEquals(3, s.getNoteCount(BeMusicDevice.SWITCH13));
		assertEquals(7, s.getNoteCount(BeMusicDevice.SWITCH14));
		assertEquals(2, s.getNoteCount(BeMusicDevice.SWITCH15));
		assertEquals(6, s.getNoteCount(BeMusicDevice.SWITCH16));
		assertEquals(8, s.getNoteCount(BeMusicDevice.SWITCH17));
		assertEquals(6, s.getNoteCount(BeMusicDevice.SCRATCH1));
		assertEquals(8, s.getNoteCount(BeMusicDevice.SWITCH21));
		assertEquals(6, s.getNoteCount(BeMusicDevice.SWITCH22));
		assertEquals(7, s.getNoteCount(BeMusicDevice.SWITCH23));
		assertEquals(7, s.getNoteCount(BeMusicDevice.SWITCH24));
		assertEquals(6, s.getNoteCount(BeMusicDevice.SWITCH25));
		assertEquals(6, s.getNoteCount(BeMusicDevice.SWITCH26));
		assertEquals(7, s.getNoteCount(BeMusicDevice.SWITCH27));
		assertEquals(6, s.getNoteCount(BeMusicDevice.SCRATCH2));
	}

	// getNoteCount(BeMusicDevice)
	// NullPointerException deviceがnull
	@Test
	public void testGetNoteCountBeMusicDevice_Exception_NullDevice() {
		var s = builder(contentEmpty()).createChart();
		assertThrows(NullPointerException.class, () -> { s.getNoteCount((BeMusicDevice)null); });
	}

	// getLongNoteCount()
	// 正常：全ての楽曲位置情報のロングノート数の合計値が返ること
	// 短押し、長押し、地雷を含めたDP譜面でノート数の合計値が一致することを確認する
	@Test
	public void testGetLongNoteCount_Normal() {
		var s = testCommonChart();
		assertEquals(18, s.getLongNoteCount());
	}

	// getLongNoteCount(BeMusicLane)
	// 指定レーンのロングノート数の合計値が返ること
	@Test
	public void testGetLongNoteCountBeMusicLane_Normal() {
		var c = testCommonChart();
		assertEquals(10, c.getLongNoteCount(BeMusicLane.PRIMARY));
		assertEquals(8, c.getLongNoteCount(BeMusicLane.SECONDARY));
	}

	// getLongNoteCount(BeMusicLane)
	// NullPointerException laneがnull
	@Test
	public void testGetLongNoteCountBeMusicLane_NullLane() {
		var c = builder(contentEmpty()).createChart();
		assertThrows(NullPointerException.class, () -> c.getLongNoteCount((BeMusicLane)null));
	}

	// getLongNoteCount(BeMusicDevice)
	// 正常：全デバイスでロングノート数合計値が正しい値で返ること
	@Test
	public void testGetLongNoteCountBeMusicDevice_Normal() {
		var s = testCommonChart();
		assertEquals(2, s.getLongNoteCount(BeMusicDevice.SWITCH11));
		assertEquals(1, s.getLongNoteCount(BeMusicDevice.SWITCH12));
		assertEquals(1, s.getLongNoteCount(BeMusicDevice.SWITCH13));
		assertEquals(2, s.getLongNoteCount(BeMusicDevice.SWITCH14));
		assertEquals(1, s.getLongNoteCount(BeMusicDevice.SWITCH15));
		assertEquals(1, s.getLongNoteCount(BeMusicDevice.SWITCH16));
		assertEquals(1, s.getLongNoteCount(BeMusicDevice.SWITCH17));
		assertEquals(1, s.getLongNoteCount(BeMusicDevice.SCRATCH1));
		assertEquals(1, s.getLongNoteCount(BeMusicDevice.SWITCH21));
		assertEquals(1, s.getLongNoteCount(BeMusicDevice.SWITCH22));
		assertEquals(1, s.getLongNoteCount(BeMusicDevice.SWITCH23));
		assertEquals(2, s.getLongNoteCount(BeMusicDevice.SWITCH24));
		assertEquals(1, s.getLongNoteCount(BeMusicDevice.SWITCH25));
		assertEquals(1, s.getLongNoteCount(BeMusicDevice.SWITCH26));
		assertEquals(1, s.getLongNoteCount(BeMusicDevice.SWITCH27));
		assertEquals(0, s.getLongNoteCount(BeMusicDevice.SCRATCH2));
	}

	// getLongNoteCount(BeMusicDevice)
	// NullPointerException deviceがnull
	@Test
	public void testGetLongNoteCountBeMusicDevice_Exception_NullDevice() {
		var s = builder(contentEmpty()).createChart();
		assertThrows(NullPointerException.class, () -> { s.getLongNoteCount((BeMusicDevice)null); });
	}

	// getMineCount()
	// 正常：全ての楽曲位置情報の地雷オブジェ数の合計値が返ること
	// 短押し、長押し、地雷を含めたDP譜面で地雷オブジェ数の合計値が一致することを確認する
	@Test
	public void testGetMineCount_Normal() {
		var s = testCommonChart();
		assertEquals(24, s.getMineCount());
	}

	// getMineCount(BeMusicLane)
	// 指定レーンの地雷オブジェ数の合計値が返ること
	@Test
	public void testGetMineCountBeMusicLane_Normal() {
		var c = testCommonChart();
		assertEquals(12, c.getMineCount(BeMusicLane.PRIMARY));
		assertEquals(12, c.getMineCount(BeMusicLane.SECONDARY));
	}

	// getMineCount(BeMusicLane)
	// NullPointerException laneがnull
	@Test
	public void testGetMineCountBeMusicLane_NullLane() {
		var c = builder(contentEmpty()).createChart();
		assertThrows(NullPointerException.class, () -> c.getMineCount((BeMusicLane)null));
	}

	// getMineCount(BeMusicDevice)
	// 正常：全デバイスで地雷オブジェ数合計値が正しい値で返ること
	@Test
	public void testGetMineCountBeMusicDevice_Normal() {
		var s = testCommonChart();
		assertEquals(1, s.getMineCount(BeMusicDevice.SWITCH11));
		assertEquals(1, s.getMineCount(BeMusicDevice.SWITCH12));
		assertEquals(2, s.getMineCount(BeMusicDevice.SWITCH13));
		assertEquals(2, s.getMineCount(BeMusicDevice.SWITCH14));
		assertEquals(2, s.getMineCount(BeMusicDevice.SWITCH15));
		assertEquals(1, s.getMineCount(BeMusicDevice.SWITCH16));
		assertEquals(2, s.getMineCount(BeMusicDevice.SWITCH17));
		assertEquals(1, s.getMineCount(BeMusicDevice.SCRATCH1));
		assertEquals(2, s.getMineCount(BeMusicDevice.SWITCH21));
		assertEquals(1, s.getMineCount(BeMusicDevice.SWITCH22));
		assertEquals(2, s.getMineCount(BeMusicDevice.SWITCH23));
		assertEquals(2, s.getMineCount(BeMusicDevice.SWITCH24));
		assertEquals(2, s.getMineCount(BeMusicDevice.SWITCH25));
		assertEquals(1, s.getMineCount(BeMusicDevice.SWITCH26));
		assertEquals(1, s.getMineCount(BeMusicDevice.SWITCH27));
		assertEquals(1, s.getMineCount(BeMusicDevice.SCRATCH2));
	}

	// getMineCount(BeMusicDevice)
	// NullPointerException deviceがnull
	@Test
	public void testGetMineCountBeMusicDevice_NullDevice() {
		var s = builder(contentEmpty()).createChart();
		assertThrows(NullPointerException.class, () -> { s.getLongNoteCount((BeMusicDevice)null); });
	}

	// getPlayTime()
	// 操作可能ノートがない場合は0が返ること
	@Test
	public void testGetPlayTime_NoPlayable() {
		var s = builder(contentFromFile()).createChart();
		assertEquals(0.0, s.getPlayTime(), 0.0);
	}

	// getPlayTime()
	// 末尾の操作可能ノートがある楽曲位置の時間を返すこと
	@Test
	public void testGetPlayTime_TailPlayable() {
		var s = builder(contentFromFile()).createChart();
		assertEquals(5.0, s.getPlayTime(), 0.0);
	}

	// getPlayTime()
	// 末尾の操作可能ノートの後ろに操作可能ノートがないノートがあっても末尾の操作可能ノートがある楽曲位置の時間を返すこと
	@Test
	public void testGetPlayTime_TailPlayableAfter() {
		var s = builder(contentFromFile()).createChart();
		assertEquals(11.0, s.getPlayTime(), 0.0);
	}

	// getPlayTime()
	// 空譜面の場合は0が返ること
	@Test
	public void testGetPlayTime_EmptyChart() {
		var s = builder(contentFromFile()).createChart();
		assertEquals(0.0, s.getPlayTime(), 0.0);
	}

	// getChangeScrollCount()
	// 正常：正しいスクロール速度変化回数が取得できること
	@Test
	public void testGetChangeScrollCount_Normal() {
		var s = builder(contentFromFile()).createChart();
		assertEquals(4, s.getChangeScrollCount());
	}

	// getChangeScrollCount()
	// 正常：スクロール速度変化がない譜面で0が返ること
	@Test
	public void testGetChangeScrollCount_NoChangeScroll() {
		var s = builder(contentEmpty()).createChart();
		assertEquals(0, s.getChangeScrollCount());
	}

	// getChangeBpmCount()
	// 正常：正しいBPM変化回数が取得できること
	@Test
	public void testGetChangeBpmCount_Normal() {
		var s = testCommonChart();
		assertEquals(3, s.getChangeBpmCount());
	}

	// getChangeBpmCount()
	// 正常：BPM変化がない譜面で0が返ること
	@Test
	public void testGetChangeBpmCount_Normal_NoChangeBpm() {
		var s = builder(contentEmpty()).createChart();
		assertEquals(0, s.getChangeBpmCount());

	}

	// getStopCount()
	// 正常：正しい譜面停止回数が取得できること
	@Test
	public void testGetStopCount_Normal() {
		var s = testCommonChart();
		assertEquals(5, s.getStopCount());
	}

	// getStopCount()
	// 正常：譜面停止がない譜面で0が返ること
	@Test
	public void testGetStopCount_Normal_NoStop() {
		var s = builder(contentEmpty()).createChart();
		assertEquals(0, s.getStopCount());
	}

	// getRecommendTotal1()
	// 期待通りの値が取得できること
	@Test
	public void testGetRecommendTotal1_200Notes() {
		var s = builder(contentFromFile()).createChart();
		var n = (double)s.getNoteCount();
		var expect = 7.605 * n / (0.01 * n + 6.5);
		assertEquals(expect, s.getRecommendTotal1(), 0.0);
	}

	// getRecommendTotal1()
	// 期待通りの値が取得できること
	@Test
	public void testGetRecommendTotal1_400Notes() {
		var s = builder(contentFromFile()).createChart();
		var n = (double)s.getNoteCount();
		var expect = 7.605 * n / (0.01 * n + 6.5);
		assertEquals(expect, s.getRecommendTotal1(), 0.0);
	}

	// getRecommendTotal1()
	// 期待通りの値が取得できること
	@Test
	public void testGetRecommendTotal1_600Notes() {
		var s = builder(contentFromFile()).createChart();
		var n = (double)s.getNoteCount();
		var expect = 7.605 * n / (0.01 * n + 6.5);
		assertEquals(expect, s.getRecommendTotal1(), 0.0);
	}

	// getRecommendTotal2()
	// 期待通りの値が取得できること
	@Test
	public void testGetRecommendTotal2_300Notes() {
		var s = builder(contentFromFile()).createChart();
		var n = (double)s.getNoteCount();
		var expect = 200.0 + n / 5.0;
		assertEquals(expect, s.getRecommendTotal2(), 0.0);
	}

	// getRecommendTotal2()
	// 期待通りの値が取得できること
	@Test
	public void testGetRecommendTotal2_400Notes() {
		var s = builder(contentFromFile()).createChart();
		var n = (double)s.getNoteCount();
		var expect = 280.0 + (n - 400.0) / 2.5;
		assertEquals(expect, s.getRecommendTotal2(), 0.0);
	}

	// getRecommendTotal2()
	// 期待通りの値が取得できること
	@Test
	public void testGetRecommendTotal2_500Notes() {
		var s = builder(contentFromFile()).createChart();
		var n = (double)s.getNoteCount();
		var expect = 280.0 + (n - 400.0) / 2.5;
		assertEquals(expect, s.getRecommendTotal2(), 0.0);
	}

	// getRecommendTotal2()
	// 期待通りの値が取得できること
	@Test
	public void testGetRecommendTotal2_600Notes() {
		var s = builder(contentFromFile()).createChart();
		var n = (double)s.getNoteCount();
		var expect = 360.0 + (n - 600.0) / 5.0;
		assertEquals(expect, s.getRecommendTotal2(), 0.0);
	}

	// getRecommendTotal2()
	// 期待通りの値が取得できること
	@Test
	public void testGetRecommendTotal2_700Notes() {
		var s = builder(contentFromFile()).createChart();
		var n = (double)s.getNoteCount();
		var expect = 360.0 + (n - 600.0) / 5.0;
		assertEquals(expect, s.getRecommendTotal2(), 0.0);
	}

	// getScratchMode()
	// スクラッチのない譜面では「通常」になること
	@Test
	public void testGetScratchMode_NoScratch() {
		var s = builder(contentFromFile()).createChart();
		assertEquals(BeMusicScratchMode.NORMAL, s.getScratchMode());
	}

	// getScratchMode()
	// 通常スクラッチのみの譜面では「通常」になること(SP)
	@Test
	public void testGetScratchMode_BeatOnlySp() {
		var s = builder(contentFromFile()).createChart();
		assertEquals(BeMusicScratchMode.NORMAL, s.getScratchMode());
	}

	// getScratchMode()
	// 通常スクラッチのみの譜面では「通常」になること(DP)
	@Test
	public void testGetScratchMode_BeatOnlyDp() {
		var s = builder(contentFromFile()).createChart();
		assertEquals(BeMusicScratchMode.NORMAL, s.getScratchMode());
	}

	// getScratchMode()
	// LNモード時、長押し終了後規定時間以内に通常スクラッチがあれば「Back-Spin」になること(SP)
	@Test
	public void testGetScratchMode_PseudoBackSpinSP() {
		var s = builder(contentFromFile()).createChart();
		assertEquals(BeMusicScratchMode.BACKSPIN, s.getScratchMode());
	}

	// getScratchMode()
	// LNモード時、長押し終了後規定時間以内に通常スクラッチがあれば「Back-Spin」になること(DP)
	@Test
	public void testGetScratchMode_PseudoBackSpinDP() {
		var s = builder(contentFromFile()).createChart();
		assertEquals(BeMusicScratchMode.BACKSPIN, s.getScratchMode());
	}

	// getScratchMode()
	// LNモード時、長押し終了後規定時間後に通常スクラッチがあっても「通常」になること(SP)
	@Test
	public void testGetScratchMode_NoPseudoBackSpinSp() {
		var s = builder(contentFromFile()).createChart();
		assertEquals(BeMusicScratchMode.NORMAL, s.getScratchMode());
	}

	// getScratchMode()
	// LNモード時、長押し終了後規定時間後に通常スクラッチがあっても「通常」になること(DP)
	@Test
	public void testGetScratchMode_NoPseudoBackSpinDp() {
		var s = builder(contentFromFile()).createChart();
		assertEquals(BeMusicScratchMode.NORMAL, s.getScratchMode());
	}

	// getScratchMode()
	// LNモード時、長押し終了後規定時間以内に長押し開始があれば「Multi-Spin」になること(SP)
	@Test
	public void testGetScratchMode_PseudoMultiSpinSp() {
		var s = builder(contentFromFile()).createChart();
		assertEquals(BeMusicScratchMode.MULTISPIN, s.getScratchMode());
	}

	// getScratchMode()
	// LNモード時、長押し終了後規定時間以内に長押し開始があれば「Multi-Spin」になること(DP)
	@Test
	public void testGetScratchMode_PseudoMultiSpinDp() {
		var s = builder(contentFromFile()).createChart();
		assertEquals(BeMusicScratchMode.MULTISPIN, s.getScratchMode());
	}

	// getScratchMode()
	// LNモード時、長押し終了後規定時間後に長押し開始があっても「通常」になること(SP)
	@Test
	public void testGetScratchMode_NoPseudoMultiSpinSp() {
		var s = builder(contentFromFile()).createChart();
		assertEquals(BeMusicScratchMode.NORMAL, s.getScratchMode());
	}

	// getScratchMode()
	// LNモード時、長押し終了後規定時間後に長押し開始があっても「通常」になること(DP)
	@Test
	public void testGetScratchMode_NoPseudoMultiSpinDp() {
		var s = builder(contentFromFile()).createChart();
		assertEquals(BeMusicScratchMode.NORMAL, s.getScratchMode());
	}

	// getScratchMode()
	// CNモード時、長押しがあれば「Back-Spin」になること(SP)
	@Test
	public void testGetScratchMode_BackSpinInCnSp() {
		var s = builder(contentFromFile()).createChart();
		assertEquals(BeMusicScratchMode.BACKSPIN, s.getScratchMode());
	}

	// getScratchMode()
	// CNモード時、長押しがあれば「Back-Spin」になること(DP)
	@Test
	public void testGetScratchMode_BackSpinInCnDp() {
		var s = builder(contentFromFile()).createChart();
		assertEquals(BeMusicScratchMode.BACKSPIN, s.getScratchMode());
	}

	// getScratchMode()
	// CNモード時、長押し終了後規定時間以内に長押し開始があっても「Multi-Spin」にはならず「Back-Spin」になること(SP)
	@Test
	public void testGetScratchMode_NoMultiSpinInCnSp() {
		var s = builder(contentFromFile()).createChart();
		assertEquals(BeMusicScratchMode.BACKSPIN, s.getScratchMode());
	}

	// getScratchMode()
	// CNモード時、長押し終了後規定時間以内に長押し開始があっても「Multi-Spin」にはならず「Back-Spin」になること(DP)
	@Test
	public void testGetScratchMode_NoMultiSpinInCnDp() {
		var s = builder(contentFromFile()).createChart();
		assertEquals(BeMusicScratchMode.BACKSPIN, s.getScratchMode());
	}

	// getScratchMode()
	// HCNモード時、長押しがあれば「Back-Spin」になること(SP)
	@Test
	public void testGetScratchMode_BackSpinInHcnSp() {
		var s = builder(contentFromFile()).createChart();
		assertEquals(BeMusicScratchMode.BACKSPIN, s.getScratchMode());
	}

	// getScratchMode()
	// HCNモード時、長押しがあれば「Back-Spin」になること(DP)
	@Test
	public void testGetScratchMode_BackSpinInHcnDp() {
		var s = builder(contentFromFile()).createChart();
		assertEquals(BeMusicScratchMode.BACKSPIN, s.getScratchMode());
	}

	// getScratchMode()
	// HCNモード時、長押し終了後規定時間以内に長押し開始があっても「Multi-Spin」にはならず「Back-Spin」になること(SP)
	@Test
	public void testGetScratchMode_NoMultiSpinInHcnSp() {
		var s = builder(contentFromFile()).createChart();
		assertEquals(BeMusicScratchMode.BACKSPIN, s.getScratchMode());
	}

	// getScratchMode()
	// HCNモード時、長押し終了後規定時間以内に長押し開始があっても「Multi-Spin」にはならず「Back-Spin」になること(DP)
	@Test
	public void testGetScratchMode_NoMultiSpinInHcnDp() {
		var s = builder(contentFromFile()).createChart();
		assertEquals(BeMusicScratchMode.BACKSPIN, s.getScratchMode());
	}

	// getScratchMode()
	// LNモード時、「Multi-Spin」検出後に「Back-Spin」を検出しても「Multi-Spin」であること
	@Test
	public void testGetScratchMode_MultiSpinAfterBackSpin() {
		var s = builder(contentFromFile()).createChart();
		assertEquals(BeMusicScratchMode.MULTISPIN, s.getScratchMode());
	}

	// hasLongNote()
	// 正常：ロングノートが存在する譜面でtrueが返ること
	@Test
	public void testHasLongNote_Normal_True() {
		var s = testCommonChart();
		assertTrue(s.hasLongNote());
	}

	// hasLongNote()
	// 正常：ロングノートが存在しない譜面でfalseが返ること
	@Test
	public void testHasLongNote_Normal_False() {
		var s = builder(contentEmpty()).createChart();
		assertFalse(s.hasLongNote());
	}

	// hasLongNote(BeMusicLane)
	// ロングノートが存在するレーンでtrueが返ること
	@Test
	public void testHasLongNoteBeMusicLane_True() {
		var c = testCommonChart();
		assertTrue(c.hasLongNote(BeMusicLane.PRIMARY));
		assertTrue(c.hasLongNote(BeMusicLane.SECONDARY));
	}

	// hasLongNote(BeMusicLane)
	// ロングノートが存在しないレーンでfalseが返ること
	@Test
	public void testHasLongNoteBeMusicLane_False() {
		var c = builder(contentEmpty()).createChart();
		assertFalse(c.hasLongNote(BeMusicLane.PRIMARY));
		assertFalse(c.hasLongNote(BeMusicLane.SECONDARY));
	}

	// hasLongNote(BeMusicLane)
	// NullPointerException laneがnull
	@Test
	public void testHasLongNoteBeMusicLane_NullLane() {
		var c = builder(contentEmpty()).createChart();
		assertThrows(NullPointerException.class, () -> c.hasLongNote((BeMusicLane)null));
	}

	// hasLongNote(BeMusicDevice)
	// ロングノートが存在する入力デバイスでtrueが返ること
	@Test
	public void testHasLongNoteBeMusicDevice_True() {
		var c = testCommonChart();
		assertTrue(c.hasLongNote(BeMusicDevice.SWITCH11));
		assertTrue(c.hasLongNote(BeMusicDevice.SWITCH12));
		assertTrue(c.hasLongNote(BeMusicDevice.SWITCH13));
		assertTrue(c.hasLongNote(BeMusicDevice.SWITCH14));
		assertTrue(c.hasLongNote(BeMusicDevice.SWITCH15));
		assertTrue(c.hasLongNote(BeMusicDevice.SWITCH16));
		assertTrue(c.hasLongNote(BeMusicDevice.SWITCH17));
		assertTrue(c.hasLongNote(BeMusicDevice.SCRATCH1));
		assertTrue(c.hasLongNote(BeMusicDevice.SWITCH21));
		assertTrue(c.hasLongNote(BeMusicDevice.SWITCH22));
		assertTrue(c.hasLongNote(BeMusicDevice.SWITCH23));
		assertTrue(c.hasLongNote(BeMusicDevice.SWITCH24));
		assertTrue(c.hasLongNote(BeMusicDevice.SWITCH25));
		assertTrue(c.hasLongNote(BeMusicDevice.SWITCH26));
		assertTrue(c.hasLongNote(BeMusicDevice.SWITCH27));
		assertFalse(c.hasLongNote(BeMusicDevice.SCRATCH2));
	}

	// hasLongNote(BeMusicDevice)
	// ロングノートが存在しない入力デバイスでfalseが返ること
	@Test
	public void testHasLongNoteBeMusicDevice_False() {
		var c = builder(contentEmpty()).createChart();
		assertFalse(c.hasLongNote(BeMusicDevice.SWITCH11));
		assertFalse(c.hasLongNote(BeMusicDevice.SWITCH12));
		assertFalse(c.hasLongNote(BeMusicDevice.SWITCH13));
		assertFalse(c.hasLongNote(BeMusicDevice.SWITCH14));
		assertFalse(c.hasLongNote(BeMusicDevice.SWITCH15));
		assertFalse(c.hasLongNote(BeMusicDevice.SWITCH16));
		assertFalse(c.hasLongNote(BeMusicDevice.SWITCH17));
		assertFalse(c.hasLongNote(BeMusicDevice.SCRATCH1));
		assertFalse(c.hasLongNote(BeMusicDevice.SWITCH21));
		assertFalse(c.hasLongNote(BeMusicDevice.SWITCH22));
		assertFalse(c.hasLongNote(BeMusicDevice.SWITCH23));
		assertFalse(c.hasLongNote(BeMusicDevice.SWITCH24));
		assertFalse(c.hasLongNote(BeMusicDevice.SWITCH25));
		assertFalse(c.hasLongNote(BeMusicDevice.SWITCH26));
		assertFalse(c.hasLongNote(BeMusicDevice.SWITCH27));
		assertFalse(c.hasLongNote(BeMusicDevice.SCRATCH2));
	}

	// hasLongNote(BeMusicDevice)
	// NullPointerException deviceがnull
	@Test
	public void testHasLongNoteBeMusicLane_NullDevice() {
		var c = builder(contentEmpty()).createChart();
		assertThrows(NullPointerException.class, () -> c.hasLongNote((BeMusicDevice)null));
	}

	// hasMine()
	// 正常：地雷オブジェが存在する譜面でtrueが返ること
	@Test
	public void testHasMine_Normal_True() {
		var s = testCommonChart();
		assertTrue(s.hasMine());
	}

	// hasMine()
	// 正常：地雷オブジェが存在しない譜面でtrueが返ること
	@Test
	public void testHasMine_Normal_False() {
		var s = builder(contentEmpty()).createChart();
		assertFalse(s.hasMine());
	}

	// hasMine(BeMusicLane)
	// 地雷オブジェが存在するレーンでtrueが返ること
	@Test
	public void testHasMineBeMusicLane_True() {
		var c = testCommonChart();
		assertTrue(c.hasMine(BeMusicLane.PRIMARY));
		assertTrue(c.hasMine(BeMusicLane.SECONDARY));
	}

	// hasMine(BeMusicLane)
	// 地雷オブジェが存在しないレーンでfalseが返ること
	@Test
	public void testHasMineBeMusicLane_False() {
		var c = builder(contentEmpty()).createChart();
		assertFalse(c.hasMine(BeMusicLane.PRIMARY));
		assertFalse(c.hasMine(BeMusicLane.SECONDARY));
	}

	// hasMine(BeMusicLane)
	// NullPointerException laneがnull
	@Test
	public void testHasMineBeMusicLane_NullLane() {
		var c = builder(contentEmpty()).createChart();
		assertThrows(NullPointerException.class, () -> c.hasMine((BeMusicLane)null));
	}

	// hasMine(BeMusicDevice)
	// 地雷オブジェが存在する入力デバイスでtrueが返ること
	@Test
	public void testHasMineBeMusicDevice_True() {
		var c = testCommonChart();
		assertTrue(c.hasMine(BeMusicDevice.SWITCH11));
		assertTrue(c.hasMine(BeMusicDevice.SWITCH12));
		assertTrue(c.hasMine(BeMusicDevice.SWITCH13));
		assertTrue(c.hasMine(BeMusicDevice.SWITCH14));
		assertTrue(c.hasMine(BeMusicDevice.SWITCH15));
		assertTrue(c.hasMine(BeMusicDevice.SWITCH16));
		assertTrue(c.hasMine(BeMusicDevice.SWITCH17));
		assertTrue(c.hasMine(BeMusicDevice.SCRATCH1));
		assertTrue(c.hasMine(BeMusicDevice.SWITCH21));
		assertTrue(c.hasMine(BeMusicDevice.SWITCH22));
		assertTrue(c.hasMine(BeMusicDevice.SWITCH23));
		assertTrue(c.hasMine(BeMusicDevice.SWITCH24));
		assertTrue(c.hasMine(BeMusicDevice.SWITCH25));
		assertTrue(c.hasMine(BeMusicDevice.SWITCH26));
		assertTrue(c.hasMine(BeMusicDevice.SWITCH27));
		assertTrue(c.hasMine(BeMusicDevice.SCRATCH2));
	}

	// hasMine(BeMusicDevice)
	// 地雷オブジェが存在しない入力デバイスでfalseが返ること
	@Test
	public void testHasMineBeMusicDevice_False() {
		var c = builder(contentEmpty()).createChart();
		assertFalse(c.hasMine(BeMusicDevice.SWITCH11));
		assertFalse(c.hasMine(BeMusicDevice.SWITCH12));
		assertFalse(c.hasMine(BeMusicDevice.SWITCH13));
		assertFalse(c.hasMine(BeMusicDevice.SWITCH14));
		assertFalse(c.hasMine(BeMusicDevice.SWITCH15));
		assertFalse(c.hasMine(BeMusicDevice.SWITCH16));
		assertFalse(c.hasMine(BeMusicDevice.SWITCH17));
		assertFalse(c.hasMine(BeMusicDevice.SCRATCH1));
		assertFalse(c.hasMine(BeMusicDevice.SWITCH21));
		assertFalse(c.hasMine(BeMusicDevice.SWITCH22));
		assertFalse(c.hasMine(BeMusicDevice.SWITCH23));
		assertFalse(c.hasMine(BeMusicDevice.SWITCH24));
		assertFalse(c.hasMine(BeMusicDevice.SWITCH25));
		assertFalse(c.hasMine(BeMusicDevice.SWITCH26));
		assertFalse(c.hasMine(BeMusicDevice.SWITCH27));
		assertFalse(c.hasMine(BeMusicDevice.SCRATCH2));
	}

	// hasMine(BeMusicDevice)
	// NullPointerException deviceがnull
	@Test
	public void testHasMineBeMusicLane_NullDevice() {
		var c = builder(contentEmpty()).createChart();
		assertThrows(NullPointerException.class, () -> c.hasMine((BeMusicDevice)null));
	}

	// hasBgm()
	// 正常：BGMが存在する譜面でtrueが返ること
	@Test
	public void testHasBgm_Normal_True() {
		var s = testCommonChart();
		assertTrue(s.hasBgm());
	}

	// hasBgm()
	// 正常：BGMが存在しない譜面でfalseが返ること
	@Test
	public void testHasBgm_Normal_False() {
		var s = builder(contentEmpty()).createChart();
		assertFalse(s.hasBgm());
	}

	// hasBga()
	// 正常：BGAが存在する譜面でtrueが返ること(BGA/LAYER/MISSいずれかひとつでもあればtrue)
	@Test
	public void testHasBga_Normal_True() {
		var s = testCommonChart();
		assertTrue(s.hasBga());
	}

	// hasBga()
	// 正常：BGAが存在しない譜面でfalseが返ること
	@Test
	public void testHasBga_Normal_False() {
		var s = builder(contentEmpty()).createChart();
		assertFalse(s.hasBga());
	}

	// hasChangeScroll()
	// 正常：スクロール速度変化が存在する譜面でtrueが返ること
	@Test
	public void testHasChangeScroll_Normal_True() {
		var s = builder(contentFromFile()).createChart();
		assertTrue(s.hasChangeScroll());
	}

	// hasChangeScroll()
	// 正常：スクロール速度変化が存在しない譜面でfalseが返ること
	@Test
	public void testHasChangeScroll_Normal_False() {
		var s = builder(contentEmpty()).createChart();
		assertFalse(s.hasChangeScroll());
	}

	// hasChangeBpm()
	// 正常：BPM変更が存在する譜面でtrueが返ること
	@Test
	public void testHasChangeBpm_Normal_True() {
		var s = testCommonChart();
		assertTrue(s.hasChangeBpm());
	}

	// hasChangeBpm()
	// 正常：BPM変更が存在しない譜面でtrueが返ること
	@Test
	public void testHasChangeBpm_Normal_False() {
		var s = builder(contentEmpty()).createChart();
		assertFalse(s.hasChangeBpm());
	}

	// hasChangeSpeed()
	// BPM変更が存在する譜面でtrueが返ること
	@Test
	public void testHasChangeSpeed_HasBpm() {
		var s = builder(contentFromFile()).createChart();
		assertTrue(s.hasChangeSpeed());
	}

	// hasChangeSpeed()
	// スクロール速度変更が存在する譜面でtrueが返ること
	@Test
	public void testHasChangeSpeed_HasScroll() {
		var s = builder(contentFromFile()).createChart();
		assertTrue(s.hasChangeSpeed());
	}

	// hasChangeSpeed()
	// BPM変更・スクロール速度変更の両方が存在する譜面でtrueが返ること
	@Test
	public void testHasChangeSpeed_HasBpmAndScrool() {
		var s = builder(contentFromFile()).createChart();
		assertTrue(s.hasChangeSpeed());
	}

	// hasChangeSpeed()
	// BPM変更・スクロール速度変更の両方が存在しない譜面でfalseが返ること
	@Test
	public void testHasChangeSpeed_NoChangeSpeed() {
		var s = builder(contentEmpty()).createChart();
		assertFalse(s.hasChangeSpeed());
	}

	// hasStop()
	// 正常：譜面停止が存在する譜面でtrueが返ること
	@Test
	public void testHasStop_Normal_True() {
		var s = testCommonChart();
		assertTrue(s.hasStop());
	}

	// hasStop()
	// 正常：譜面停止が存在しない譜面でfalseが返ること
	@Test
	public void testHasStop_Normal_False() {
		var s = builder(contentEmpty()).createChart();
		assertFalse(s.hasStop());
	}

	// hasGimmick()
	// BPM変更が存在する譜面でtrueが返ること
	@Test
	public void testHasGimmick_HasBpm() {
		var s = builder(contentFromFile()).createChart();
		assertTrue(s.hasGimmick());
	}

	// hasGimmick()
	// スクロール速度変更が存在する譜面でtrueが返ること
	@Test
	public void testHasGimmick_HasScroll() {
		var s = builder(contentFromFile()).createChart();
		assertTrue(s.hasGimmick());
	}

	// hasGimmick()
	// 譜面停止が存在する譜面でtrueが返ること
	@Test
	public void testHasGimmick_HasStop() {
		var s = builder(contentFromFile()).createChart();
		assertTrue(s.hasGimmick());
	}

	// hasGimmick()
	// 地雷オブジェが存在する譜面でtrueが返ること
	@Test
	public void testHasGimmick_HasMine() {
		var s = builder(contentFromFile()).createChart();
		assertTrue(s.hasGimmick());
	}

	// hasGimmick()
	// BPM変更・スクロール速度変更・譜面停止・地雷オブジェが全く存在しない譜面でfalseが返ること
	@Test
	public void testHasGimmick_NothingAll() {
		var s = builder(contentEmpty()).createChart();
		assertFalse(s.hasGimmick());
	}

	// iterator()
	// 先頭から末尾までの楽曲位置情報が順番に走査されること
	@Test
	public void testIterator() {
		var c = testCommonChart();
		var pc = c.getPointCount();
		var pl = new ArrayList<BeMusicPoint>(pc);
		for (var p : c) { pl.add(p); }
		for (var i = 0; i < pc; i++) { assertEquals(0, BmsAt.compare(c.getPoint(i), pl.get(i))); }
	}

	// points()
	// 空譜面では何も走査されないこと
	@Test
	public void testPoints_Empty() {
		var c = builder(contentEmpty()).createChart();
		assertEquals(0, c.points().count());
	}

	// points()
	// 先頭から末尾までの楽曲位置情報が順番に走査されること
	@Test
	public void testPoints_Normal() {
		var c = testCommonChart();
		var pl = c.points().collect(Collectors.toList());
		var pc = c.getPointCount();
		assertEquals(pc, pl.size());
		for (var i = 0; i < pc; i++) { assertEquals(0, BmsAt.compare(c.getPoint(i), pl.get(i))); }
	}

	// points(int, int)
	// 指定範囲の楽曲位置情報のみが走査されること
	@Test
	public void testPoints2_Normal() {
		var c = testCommonChart();
		var pl = c.points(5, 12).collect(Collectors.toList());
		var pc = 7;
		assertEquals(pc, pl.size());
		for (var i = 0; i < pc; i++) { assertEquals(0, BmsAt.compare(c.getPoint(i + 5), pl.get(i))); }
	}

	// points(int, int)
	// 譜面全体を走査できること
	@Test
	public void testPoints2_AllRange() {
		var c = testCommonChart();
		var pc = c.getPointCount();
		var pl = c.points(0, pc).collect(Collectors.toList());
		assertEquals(pc, pl.size());
		for (var i = 0; i < pc; i++) { assertEquals(0, BmsAt.compare(c.getPoint(i), pl.get(i))); }
	}

	// points(int, int)
	// IndexOutOfBoundsException startが 0～getPointCount()-1 の範囲外
	@Test
	public void testPoints2_OutOfStart() {
		var c = testCommonChart();
		var pc = c.getPointCount();
		var ex = IndexOutOfBoundsException.class;
		assertThrows(ex, () -> c.points(-1, pc));
		assertThrows(ex, () -> c.points(pc, pc));
	}

	// points(int, int)
	// IndexOutOfBoundsException endが 0～getPointCount() の範囲外
	@Test
	public void testPoints2_OutOfEnd() {
		var c = testCommonChart();
		var pc = c.getPointCount();
		var ex = IndexOutOfBoundsException.class;
		assertThrows(ex, () -> c.points(0, -1));
		assertThrows(ex, () -> c.points(0, pc + 1));
	}

	// indexOf(Predicate)
	// 正常：テスターでtrueを返した項目のインデックスが返ること
	@Test
	public void testIndexOf1_Normal_Match() {
		var s = testCommonChart();
		assertEquals(0, s.indexOf(p -> p.hasBga()));
	}

	// indexOf(Predicate)
	// 正常：テスターで全てfalseを返すと-1が返ること
	@Test
	public void testIndexOf1_Normal_Unmatch() {
		var s = testCommonChart();
		assertEquals(-1, s.indexOf(p -> false));
	}

	// indexOf(Predicate)
	// NullPointerException testerがnull
	@Test
	public void testIndexOf1_Exception_NullTester() {
		var s = testCommonChart();
		assertThrows(NullPointerException.class, () -> { s.indexOf(null); });
	}

	// indexOf(int, int, Predicate)
	// 正常：テスターでtrueを返した項目のインデックスが返ること
	@Test
	public void testIndexOf2_Normal_Match() {
		var s = testCommonChart();
		assertEquals(1, s.indexOf(0, s.getPointCount(), p -> p.getBgmCount() > 1));
		assertEquals(37, s.indexOf(2, s.getPointCount(), p -> p.getBgmCount() > 1));
	}

	// indexOf(int, int, Predicate)
	// 正常：テスターで全てfalseを返すと-1が返ること
	@Test
	public void testIndexOf2_Normal_Unmatch() {
		var s = testCommonChart();
		assertEquals(-1, s.indexOf(0, s.getPointCount(), p -> false));
	}

	// indexOf(int, int, Predicate)
	// 正常：楽曲位置情報が0件の場合-1がテスターが実行されず返ること
	@Test
	public void testIndexOf2_Normal_Empty() {
		var s = builder(contentEmpty()).createChart();
		assertEquals(-1, s.indexOf(0, s.getPointCount(), p -> { fail(); return true; }));
	}

	// indexOf(int, int, Predicate)
	// IndexOutOfBoundsException beginIndexがマイナス値
	@Test
	public void testIndexOf2_Exception_MinusBeginIndex() {
		var s = testCommonChart();
		assertThrows(IndexOutOfBoundsException.class, () -> { s.indexOf(-1, s.getPointCount(), p -> false); });
	}

	// indexOf(int, int, Predicate)
	// IndexOutOfBoundsException beginIndexがgetPointCount()超過
	@Test
	public void testIndexOf2_Exception_OverflowBeginIndex() {
		var s = testCommonChart();
		assertThrows(IndexOutOfBoundsException.class, () -> { s.indexOf(s.getPointCount() + 1, s.getPointCount(), p -> false); });
	}

	// indexOf(int, int, Predicate)
	// IndexOutOfBoundsException endIndexがマイナス値
	@Test
	public void testIndexOf2_Exception_MinusEndIndex() {
		var s = testCommonChart();
		assertThrows(IndexOutOfBoundsException.class, () -> { s.indexOf(0, -1, p -> false); });
	}

	// indexOf(int, int, Predicate)
	// IndexOutOfBoundsException endIndexがgetPointCount()超過
	@Test
	public void testIndexOf2_Exception_OverflowEndIndex() {
		var s = testCommonChart();
		assertThrows(IndexOutOfBoundsException.class, () -> { s.indexOf(0, s.getPointCount() + 1, p -> false); });
	}

	// indexOf(int, int, Predicate)
	// NullPointerException testerがnull
	@Test
	public void testIndexOf2_Exception_NullTester() {
		var s = testCommonChart();
		assertThrows(NullPointerException.class, () -> { s.indexOf(0, s.getPointCount(), null); });
	}

	// lastIndexOf(Predicate)
	// 正常：テスターでtrueを返した項目のインデックスが返ること
	@Test
	public void testLastIndexOf1_Normal_Match() {
		var s = testCommonChart();
		assertEquals(51, s.lastIndexOf(p -> p.hasBga()));
	}

	// lastIndexOf(Predicate)
	// 正常：テスターで全てfalseを返すと-1が返ること
	@Test
	public void testLastIndexOf1_Normal_Unmatch() {
		var s = testCommonChart();
		assertEquals(-1, s.lastIndexOf(p -> false));
	}

	// lastIndexOf(Predicate)
	// NullPointerException testerがnull
	@Test
	public void testLastIndexOf1_Exception_NullTester() {
		var s = testCommonChart();
		assertThrows(NullPointerException.class, () -> { s.lastIndexOf(null); });
	}

	// lastIndexOf(int, int, Predicate)
	// 正常：テスターでtrueを返した項目のインデックスが返ること
	@Test
	public void testLastIndexOf2_Normal_Match() {
		var s = testCommonChart();
		assertEquals(37, s.lastIndexOf(0, s.getPointCount(), p -> p.getBgmCount() > 1));
		assertEquals(1, s.lastIndexOf(0, 37, p -> p.getBgmCount() > 1));
	}

	// lastIndexOf(int, int, Predicate)
	// 正常：テスターで全てfalseを返すと-1が返ること
	@Test
	public void testLastIndexOf2_Normal_Unmatch() {
		var s = testCommonChart();
		assertEquals(-1, s.lastIndexOf(0, s.getPointCount(), p -> false));
	}

	// lastIndexOf(int, int, Predicate)
	// 正常：楽曲位置情報が0件の場合-1がテスターが実行されず返ること
	@Test
	public void testLastIndexOf2_Normal_Empty() {
		var s = builder(contentEmpty()).createChart();
		assertEquals(-1, s.lastIndexOf(0, s.getPointCount(), p -> { fail(); return true; }));
	}

	// lastIndexOf(int, int, Predicate)
	// IndexOutOfBoundsException beginIndexがマイナス値
	@Test
	public void testLastIndexOf2_Exception_MinusBeginIndex() {
		var s = testCommonChart();
		assertThrows(IndexOutOfBoundsException.class, () -> { s.lastIndexOf(-1, s.getPointCount(), p -> false); });
	}

	// lastIndexOf(int, int, Predicate)
	// IndexOutOfBoundsException beginIndexがgetPointCount()超過
	@Test
	public void testLastIndexOf2_Exception_OverflowBeginIndex() {
		var s = testCommonChart();
		assertThrows(IndexOutOfBoundsException.class, () -> { s.lastIndexOf(s.getPointCount() + 1, s.getPointCount(), p -> false); });
	}

	// lastIndexOf(int, int, Predicate)
	// IndexOutOfBoundsException endIndexがマイナス値
	@Test
	public void testLastIndexOf2_Exception_MinusEndIndex() {
		var s = testCommonChart();
		assertThrows(IndexOutOfBoundsException.class, () -> { s.lastIndexOf(0, -1, p -> false); });
	}

	// lastIndexOf(int, int, Predicate)
	// IndexOutOfBoundsException endIndexがgetPointCount()超過
	@Test
	public void testLastIndexOf2_Exception_OverflowEndIndex() {
		var s = testCommonChart();
		assertThrows(IndexOutOfBoundsException.class, () -> { s.lastIndexOf(0, s.getPointCount() + 1, p -> false); });
	}

	// lastIndexOf(int, int, Predicate)
	// NullPointerException testerがnull
	@Test
	public void testLastIndexOf2_Exception_NullTester() {
		var s = testCommonChart();
		assertThrows(NullPointerException.class, () -> { s.lastIndexOf(0, s.getPointCount(), null); });
	}

	// floorPointOf(BmsAt)
	// 正常：期待通りのインデックスが取得できること
	@Test
	public void testFloorPointOf1_Normal() {
		testFloorPointOf_Normal((c, s, m, t) -> s.floorPointOf(new BmsPoint(m, t)));
	}

	// floorPointOf(BmsAt)
	// NullPointerException atがnull
	@Test
	public void testFloorPointOf1_Exception_NullAt() {
		var s = testCommonChart();
		assertThrows(NullPointerException.class, () -> { s.floorPointOf(null); });
	}

	// floorPointOf(int, int)
	// 正常：期待通りのインデックスが取得できること
	@Test
	public void testFloorPointOf3_Normal() {
		testFloorPointOf_Normal((c, s, m, t) -> s.floorPointOf(m, t));
	}

	// floorPointOf(double)
	// 正常：期待通りのインデックスが取得できること
	@Test
	public void testFloorPointOf4_Normal() {
		testFloorPointOf_Normal((c, s, m, t) -> s.floorPointOf(c.pointToTime(m, t)), false);
	}

	// floorPointOf(double)
	// IllegalArgumentException timeがマイナス値
	@Test
	public void testFloorPointOf4_Exception_MinusTime() {
		var s = testCommonChart();
		assertThrows(IllegalArgumentException.class, () -> { s.floorPointOf(-0.00000001); });
	}

	// ceilPointOf(BmsAt)
	// 正常：期待通りのインデックスが取得できること
	@Test
	public void testCeilPointOf1_Normal() {
		testCeilPointOf_Normal((c, s, m, t) -> s.ceilPointOf(new BmsPoint(m, t)));
	}

	// ceilPointOf(BmsAt)
	// NullPointerException atがnull
	@Test
	public void testCeilPointOf1_Exception_NullAt() {
		var s = testCommonChart();
		assertThrows(NullPointerException.class, () -> { s.ceilPointOf(null); });
	}

	// ceilPointOf(int, int)
	// 正常：期待通りのインデックスが取得できること
	@Test
	public void testCeilPointOf3_Normal() {
		testCeilPointOf_Normal((c, s, m, t) -> s.ceilPointOf(m, t));
	}

	// ceilPointOf(double)
	// 正常：期待通りのインデックスが取得できること
	@Test
	public void testCeilPointOf4_Normal() {
		testCeilPointOf_Normal((c, s, m, t) -> s.ceilPointOf(c.pointToTime(m, t)));
	}

	// ceilPointOf(double)
	// IllegalArgumentException timeがマイナス値
	@Test
	public void testCeilPointOf4_Exception_MinusTime() {
		var s = testCommonChart();
		assertThrows(IllegalArgumentException.class, () -> { s.ceilPointOf(-0.00000001); });
	}

	// collectSoundTracks(IntPredicate)
	// 譜面全体のトラックIDが収集されること
	@Test
	public void testCollectSoundTracks1_Normal() {
		var c = chartForCollectSoundTracks();
		var ids = c.collectSoundTracks(r -> true);
		assertEquals(List.of(4, 6, 7, 8, 9), List.copyOf(ids));
	}

	// collectSoundTracks(IntPredicate)
	// 楽曲位置情報が0件の時、空のセットが返されること
	@Test
	public void testCollectSoundTracks1_Empty() {
		var c = chartEmpty();
		var ids = c.collectSoundTracks(r -> true);
		assertTrue(ids.isEmpty());
	}

	// collectSoundTracks(IntPredicate)
	// NullPointerException isCollect が null
	@Test
	public void testCollectSoundTracks1_NullIsCollect() {
		var c = chartForCollectSoundTracks();
		var ex = NullPointerException.class;
		assertThrows(ex, () -> c.collectSoundTracks(null));
	}

	// collectSoundTracks(double, double, IntPredicate)
	// 終了を含む指定範囲のトラックIDが収集されること
	@Test
	public void testCollectSoundTracks2_Normal() {
		var c = chartForCollectSoundTracks();
		var ids = c.collectSoundTracks(3.0, 6.0, r -> true);
		assertEquals(List.of(6, 7, 8), List.copyOf(ids));
	}

	// collectSoundTracks(double, double, IntPredicate)
	// 楽曲位置情報が0件の時、空のセットが返されること
	@Test
	public void testCollectSoundTracks2_Empty() {
		var c = chartEmpty();
		var ids = c.collectSoundTracks(0.0, 10.0, r -> true);
		assertTrue(ids.isEmpty());
	}

	// collectSoundTracks(double, double, IntPredicate)
	// 開始・終了が逆転しても例外はスローされず、空のセットが返されること
	@Test
	public void testCollectSoundTracks2_ReverseRange() {
		var c = chartForCollectSoundTracks();
		var ids = c.collectSoundTracks(10.0, 0.0, r -> true);
		assertTrue(ids.isEmpty());
	}

	// collectSoundTracks(double, double, IntPredicate)
	// 開始・終了に同じ値を指定すると、その位置と完全に一致する楽曲位置情報のみが収集対象になること
	@Test
	public void testCollectSoundTracks2_Pinpoint() {
		var c = chartForCollectSoundTracks();
		var ids = c.collectSoundTracks(7.0, 7.0, r -> true);
		assertEquals(List.of(4, 8), List.copyOf(ids));
	}

	// collectSoundTracks(double, double, IntPredicate)
	// 最初の楽曲位置情報より前方の範囲を指定した場合、空のセットが返されること
	@Test
	public void testCollectSoundTracks2_BehindHead() {
		var c = chartForCollectSoundTracks();
		var ids = c.collectSoundTracks(0.0, 1.9, r -> true);
		assertTrue(ids.isEmpty());
	}

	// collectSoundTracks(double, double, IntPredicate)
	// 最後の楽曲位置情報より後方の範囲を指定した場合、空のセットが返されること
	@Test
	public void testCollectSoundTracks2_AheadTail() {
		var c = chartForCollectSoundTracks();
		var ids = c.collectSoundTracks(8.5, 10.0, r -> true);
		assertTrue(ids.isEmpty());
	}

	// collectSoundTracks(double, double, IntPredicate)
	// IllegalArgumentException timeBegin が負の値
	@Test
	public void testCollectSoundTracks2_NegativeTimeBegin() {
		var c = chartForCollectSoundTracks();
		var ex = IllegalArgumentException.class;
		assertThrows(ex, () -> c.collectSoundTracks(-1.0, 8.0, r -> true));
	}

	// collectSoundTracks(double, double, IntPredicate)
	// IllegalArgumentException timeEnd が負の値
	@Test
	public void testCollectSoundTracks2_NegativeTimeEnd() {
		var c = chartForCollectSoundTracks();
		var ex = IllegalArgumentException.class;
		assertThrows(ex, () -> c.collectSoundTracks(0.0, -1.0, r -> true));
	}

	// collectSoundTracks(double, double, IntPredicate)
	// NullPointerException isCollect が null
	@Test
	public void testCollectSoundTracks2_NullIsCollect() {
		var c = chartForCollectSoundTracks();
		var ex = NullPointerException.class;
		assertThrows(ex, () -> c.collectSoundTracks(0.0, 8.0, null));
	}

	// collectSoundTracks(BmsAt, BmsAt, IntPredicate)
	// 終了を含む指定範囲のトラックIDが収集されること
	@Test
	public void testCollectSoundTracks3_Normal() {
		var c = chartForCollectSoundTracks();
		var ids = c.collectSoundTracks(BmsPoint.of(1, 96), BmsPoint.of(3, 0), r -> true);
		assertEquals(List.of(6, 7, 8), List.copyOf(ids));
	}

	// collectSoundTracks(BmsAt, BmsAt, IntPredicate)
	// 楽曲位置情報が0件の時、空のセットが返されること
	@Test
	public void testCollectSoundTracks3_Empty() {
		var c = chartEmpty();
		var ids = c.collectSoundTracks(BmsPoint.of(0, 0), BmsPoint.of(10, 0), r -> true);
		assertTrue(ids.isEmpty());
	}

	// collectSoundTracks(BmsAt, BmsAt, IntPredicate)
	// 開始・終了が逆転しても例外はスローされず、空のセットが返されること
	@Test
	public void testCollectSoundTracks3_ReverseRange() {
		var c = chartForCollectSoundTracks();
		var ids = c.collectSoundTracks(BmsPoint.of(10, 0), BmsPoint.of(0, 0), r -> true);
		assertTrue(ids.isEmpty());
	}

	// collectSoundTracks(BmsAt, BmsAt, IntPredicate)
	// 開始・終了に同じ値を指定すると、その位置と完全に一致する楽曲位置情報のみが収集対象になること
	@Test
	public void testCollectSoundTracks3_Pinpoint() {
		var c = chartForCollectSoundTracks();
		var ids = c.collectSoundTracks(BmsPoint.of(3, 96), BmsPoint.of(3, 96), r -> true);
		assertEquals(List.of(4, 8), List.copyOf(ids));
	}

	// collectSoundTracks(BmsAt, BmsAt, IntPredicate)
	// 最初の楽曲位置情報より前方の範囲を指定した場合、空のセットが返されること
	@Test
	public void testCollectSoundTracks3_BehindHead() {
		var c = chartForCollectSoundTracks();
		var ids = c.collectSoundTracks(BmsPoint.of(0, 0), BmsPoint.of(0, 191), r -> true);
		assertTrue(ids.isEmpty());
	}

	// collectSoundTracks(BmsAt, BmsAt, IntPredicate)
	// 最後の楽曲位置情報より後方の範囲を指定した場合、空のセットが返されること
	@Test
	public void testCollectSoundTracks3_AheadTail() {
		var c = chartForCollectSoundTracks();
		var ids = c.collectSoundTracks(BmsPoint.of(4, 1), BmsPoint.of(10, 0), r -> true);
		assertTrue(ids.isEmpty());
	}

	// collectSoundTracks(BmsAt, BmsAt, IntPredicate)
	// NullPointerException atBegin が null
	@Test
	public void testCollectSoundTracks3_NullAtBegin() {
		var c = chartForCollectSoundTracks();
		var ex = NullPointerException.class;
		assertThrows(ex, () -> c.collectSoundTracks(null, BmsPoint.of(10, 0), r -> true));
	}

	// collectSoundTracks(BmsAt, BmsAt, IntPredicate)
	// NullPointerException atLast が null
	@Test
	public void testCollectSoundTracks3_NullAtLast() {
		var c = chartForCollectSoundTracks();
		var ex = NullPointerException.class;
		assertThrows(ex, () -> c.collectSoundTracks(BmsPoint.of(0, 0), null, r -> true));
	}

	// collectSoundTracks(BmsAt, BmsAt, IntPredicate)
	// NullPointerException isCollect が null
	@Test
	public void testCollectSoundTracks3_NullIsCollect() {
		var c = chartForCollectSoundTracks();
		var ex = NullPointerException.class;
		assertThrows(ex, () -> c.collectSoundTracks(BmsPoint.of(0, 0), BmsPoint.of(10, 0), null));
	}

	// collectSoundTracks(int, double, int, double, IntPredicate)
	// 終了を含む指定範囲のトラックIDが収集されること
	@Test
	public void testCollectSoundTracks4_Normal() {
		var c = chartForCollectSoundTracks();
		var ids = c.collectSoundTracks(1, 96, 3, 0, r -> true);
		assertEquals(List.of(6, 7, 8), List.copyOf(ids));
	}

	// collectSoundTracks(int, double, int, double, IntPredicate)
	// 楽曲位置情報が0件の時、空のセットが返されること
	@Test
	public void testCollectSoundTracks4_Empty() {
		var c = chartEmpty();
		var ids = c.collectSoundTracks(0, 0, 10, 0, r -> true);
		assertTrue(ids.isEmpty());
	}

	// collectSoundTracks(int, double, int, double, IntPredicate)
	// 開始・終了が逆転しても例外はスローされず、空のセットが返されること
	@Test
	public void testCollectSoundTracks4_ReverseRange() {
		var c = chartForCollectSoundTracks();
		var ids = c.collectSoundTracks(10, 0, 0, 0, r -> true);
		assertTrue(ids.isEmpty());
	}

	// collectSoundTracks(int, double, int, double, IntPredicate)
	// 開始・終了に同じ値を指定すると、その位置と完全に一致する楽曲位置情報のみが収集対象になること
	@Test
	public void testCollectSoundTracks4_Pinpoint() {
		var c = chartForCollectSoundTracks();
		var ids = c.collectSoundTracks(3, 96, 3, 96, r -> true);
		assertEquals(List.of(4, 8), List.copyOf(ids));
	}

	// collectSoundTracks(int, double, int, double, IntPredicate)
	// 最初の楽曲位置情報より前方の範囲を指定した場合、空のセットが返されること
	@Test
	public void testCollectSoundTracks4_BehindHead() {
		var c = chartForCollectSoundTracks();
		var ids = c.collectSoundTracks(0, 0, 0, 191, r -> true);
		assertTrue(ids.isEmpty());
	}

	// collectSoundTracks(int, double, int, double, IntPredicate)
	// 最後の楽曲位置情報より後方の範囲を指定した場合、空のセットが返されること
	@Test
	public void testCollectSoundTracks4_AheadTail() {
		var c = chartForCollectSoundTracks();
		var ids = c.collectSoundTracks(4, 1, 10, 0, r -> true);
		assertTrue(ids.isEmpty());
	}

	// collectSoundTracks(int, double, int, double, IntPredicate)
	// NullPointerException isCollect が null
	@Test
	public void testCollectSoundTracks4_NullIsCollect() {
		var c = chartForCollectSoundTracks();
		var ex = NullPointerException.class;
		assertThrows(ex, () -> c.collectSoundTracks(0, 0, 10, 0, null));
	}

	// collectSoundTracks(int, int, IntPredicate)
	// 終了を含む指定範囲のトラックIDが収集されること
	@Test
	public void testCollectSoundTracks5_Normal() {
		var c = chartForCollectSoundTracks();
		var ids = c.collectSoundTracks(2, 6, r -> true);
		assertEquals(List.of(6, 7, 8), List.copyOf(ids));
	}

	// collectSoundTracks(int, int, IntPredicate)
	// 楽曲位置情報が0件の時、空のセットが返されること
	@Test
	public void testCollectSoundTracks5_Empty() {
		var c = chartEmpty();
		var ids = c.collectSoundTracks(0, Integer.MAX_VALUE, r -> true);
		assertTrue(ids.isEmpty());
	}

	// collectSoundTracks(int, int, IntPredicate)
	// 開始・終了が逆転しても例外はスローされず、空のセットが返されること
	@Test
	public void testCollectSoundTracks5_ReverseRange() {
		var c = chartForCollectSoundTracks();
		var ids = c.collectSoundTracks(Integer.MAX_VALUE, 0, r -> true);
		assertTrue(ids.isEmpty());
	}

	// collectSoundTracks(int, int, IntPredicate)
	// 開始・終了に同じ値を指定すると、その位置と完全に一致する楽曲位置情報のみが収集対象になること
	@Test
	public void testCollectSoundTracks5_Pinpoint() {
		var c = chartForCollectSoundTracks();
		var ids = c.collectSoundTracks(7, 7, r -> true);
		assertEquals(List.of(4, 8), List.copyOf(ids));
	}

	// collectSoundTracks(int, int, IntPredicate)
	// 最後の楽曲位置情報より後方の範囲を指定した場合、空のセットが返されること
	@Test
	public void testCollectSoundTracks5_AheadTail() {
		var c = chartForCollectSoundTracks();
		var ids = c.collectSoundTracks(100, Integer.MAX_VALUE, r -> true);
		assertTrue(ids.isEmpty());
	}

	// collectSoundTracks(int, int, IntPredicate)
	// 可視オブジェのノート生値が正しくテスター関数に渡されること
	@Test
	public void testCollectSoundTracks5_VisibleRaw() {
		var c = chartForCollectSoundTracks();
		var ids1 = c.collectSoundTracks(1, 1, r -> {
			assertTrue(BeMusicSound.isVisible(r));
			assertEquals(BeMusicDevice.SWITCH11, BeMusicSound.getDevice(r));
			assertEquals(9, BeMusicSound.getTrackId(r));
			assertFalse(BeMusicSound.isRestartTrack(r));
			assertNull(BeMusicSound.getLongNoteMode(r, null));
			return true;
		});
		assertEquals(1, ids1.size());
		var ids2 = c.collectSoundTracks(2, 2, r -> {
			assertTrue(BeMusicSound.isVisible(r));
			assertEquals(BeMusicDevice.SWITCH12, BeMusicSound.getDevice(r));
			assertEquals(8, BeMusicSound.getTrackId(r));
			assertTrue(BeMusicSound.isRestartTrack(r));
			assertNull(BeMusicSound.getLongNoteMode(r, null));
			return true;
		});
		assertEquals(1, ids2.size());
		var ids3 = c.collectSoundTracks(3, 3, r -> {
			assertTrue(BeMusicSound.isVisible(r));
			assertEquals(BeMusicDevice.SWITCH13, BeMusicSound.getDevice(r));
			assertEquals(7, BeMusicSound.getTrackId(r));
			assertFalse(BeMusicSound.isRestartTrack(r));
			assertEquals(BeMusicLongNoteMode.CN, BeMusicSound.getLongNoteMode(r, null));
			return true;
		});
		assertEquals(1, ids3.size());
	}

	// collectSoundTracks(int, int, IntPredicate)
	// 不可視オブジェのノート生値が正しくテスター関数に渡されること
	@Test
	public void testCollectSoundTracks5_InvisibleRaw() {
		var c = chartForCollectSoundTracks();
		var ids = c.collectSoundTracks(5, 5, r -> {
			assertTrue(BeMusicSound.isInvisible(r));
			assertEquals(6, BeMusicSound.getTrackId(r));
			return true;
		});
		assertEquals(1, ids.size());
	}

	// collectSoundTracks(int, int, IntPredicate)
	// BGMのノート生値が正しくテスター関数に渡されること
	@Test
	public void testCollectSoundTracks5_BgmRaw() {
		var c = chartForCollectSoundTracks();
		var ids1 = c.collectSoundTracks(7, 8, r -> true);
		assertEquals(List.of(4, 8, 9), List.copyOf(ids1));
		var ids2 = c.collectSoundTracks(8, 8, r -> {
			assertTrue(BeMusicSound.isBgm(r));
			assertEquals(9, BeMusicSound.getTrackId(r));
			return true;
		});
		assertEquals(1, ids2.size());
	}

	// collectSoundTracks(int, int, IntPredicate)
	// テスター関数が false を返すと収集対象外になること
	@Test
	public void testCollectSoundTracks5_NotCollect() {
		var c = chartForCollectSoundTracks();
		var ids = c.collectSoundTracks(0, Integer.MAX_VALUE, r -> BeMusicSound.isInvisible(r));
		assertEquals(List.of(6), List.copyOf(ids));
	}

	// collectSoundTracks(int, int, IntPredicate)
	// IllegalArgumentException start が負の値
	@Test
	public void testCollectSoundTracks5_NegativeStart() {
		var c = chartForCollectSoundTracks();
		var ex = IllegalArgumentException.class;
		assertThrows(ex, () -> c.collectSoundTracks(-1, 0, r -> true));
	}

	// collectSoundTracks(int, int, IntPredicate)
	// IllegalArgumentException last が負の値
	@Test
	public void testCollectSoundTracks5_NegativeLast() {
		var c = chartForCollectSoundTracks();
		var ex = IllegalArgumentException.class;
		assertThrows(ex, () -> c.collectSoundTracks(0, -1, r -> true));
	}

	// collectSoundTracks(int, int, IntPredicate)
	// NullPointerException isCollect が null
	@Test
	public void testCollectSoundTracks5_NullIsCollect() {
		var c = chartForCollectSoundTracks();
		var ex = NullPointerException.class;
		assertThrows(ex, () -> c.collectSoundTracks(0, Integer.MAX_VALUE, null));
	}

	// collectImageTracks(BiPredicate<Integer, Integer>)
	// 譜面全体のトラックIDが収集されること
	@Test
	public void testCollectImageTracks1_Normal() {
		var c = chartForCollectImageTracks();
		var ids = c.collectImageTracks((t, v) -> true);
		assertEquals(List.of(3, 5, 7), List.copyOf(ids));
	}

	// collectImageTracks(BiPredicate<Integer, Integer>)
	// 楽曲位置情報が0件の時、空のセットが返されること
	@Test
	public void testCollectImageTracks1_Empty() {
		var c = chartEmpty();
		var ids = c.collectImageTracks((t, v) -> true);
		assertTrue(ids.isEmpty());
	}

	// collectImageTracks(BiPredicate<Integer, Integer>)
	// NullPointerException isCollect が null
	@Test
	public void testCollectImageTracks1_NullIsCollect() {
		var c = chartForCollectImageTracks();
		var ex = NullPointerException.class;
		assertThrows(ex, () -> c.collectImageTracks(null));
	}

	// collectImageTracks(double, double, BiPredicate<Integer, Integer>)
	// 終了を含む指定範囲のトラックIDが収集されること
	@Test
	public void testCollectImageTracks2_Normal() {
		var c = chartForCollectImageTracks();
		var ids = c.collectImageTracks(4.0, 6.0, (t, v) -> true);
		assertEquals(List.of(5, 7), List.copyOf(ids));
	}

	// collectImageTracks(double, double, BiPredicate<Integer, Integer>)
	// 楽曲位置情報が0件の時、空のセットが返されること
	@Test
	public void testCollectImageTracks2_Empty() {
		var c = chartEmpty();
		var ids = c.collectImageTracks(0.0, 10.0, (t, v) -> true);
		assertTrue(ids.isEmpty());
	}

	// collectImageTracks(double, double, BiPredicate<Integer, Integer>)
	// 開始・終了が逆転しても例外はスローされず、空のセットが返されること
	@Test
	public void testCollectImageTracks2_ReverseRange() {
		var c = chartForCollectImageTracks();
		var ids = c.collectImageTracks(10.0, 0.0, (t, v) -> true);
		assertTrue(ids.isEmpty());
	}

	// collectImageTracks(double, double, BiPredicate<Integer, Integer>)
	// 開始・終了に同じ値を指定すると、その位置と完全に一致する楽曲位置情報のみが収集対象になること
	@Test
	public void testCollectImageTracks2_Pinpoint() {
		var c = chartForCollectImageTracks();
		var ids = c.collectImageTracks(5.0, 5.0, (t, v) -> true);
		assertEquals(List.of(7), List.copyOf(ids));
	}

	// collectImageTracks(double, double, BiPredicate<Integer, Integer>)
	// 最初の楽曲位置情報より前方の範囲を指定した場合、空のセットが返されること
	@Test
	public void testCollectImageTracks2_BehindHead() {
		var c = chartForCollectImageTracks();
		var ids = c.collectImageTracks(0.0, 1.9, (t, v) -> true);
		assertTrue(ids.isEmpty());
	}

	// collectImageTracks(double, double, BiPredicate<Integer, Integer>)
	// 最後の楽曲位置情報より後方の範囲を指定した場合、空のセットが返されること
	@Test
	public void testCollectImageTracks2_AheadTail() {
		var c = chartForCollectImageTracks();
		var ids = c.collectImageTracks(7.1, 10.0, (t, v) -> true);
		assertTrue(ids.isEmpty());
	}

	// collectImageTracks(double, double, BiPredicate<Integer, Integer>)
	// IllegalArgumentException timeBegin が負の値
	@Test
	public void testCollectImageTracks2_NegativeTimeBegin() {
		var c = chartForCollectImageTracks();
		var ex = IllegalArgumentException.class;
		assertThrows(ex, () -> c.collectImageTracks(-1.0, 10.0, (t, v) -> true));
	}

	// collectImageTracks(double, double, BiPredicate<Integer, Integer>)
	// IllegalArgumentException timeEnd が負の値
	@Test
	public void testCollectImageTracks2_NegativeTimeEnd() {
		var c = chartForCollectImageTracks();
		var ex = IllegalArgumentException.class;
		assertThrows(ex, () -> c.collectImageTracks(0.0, -1.0, (t, v) -> true));
	}

	// collectImageTracks(double, double, BiPredicate<Integer, Integer>)
	// NullPointerException isCollect が null
	@Test
	public void testCollectImageTracks2_NullIsCollect() {
		var c = chartForCollectImageTracks();
		var ex = NullPointerException.class;
		assertThrows(ex, () -> c.collectImageTracks(0.0, 10.0, null));
	}

	// collectImageTracks(BmsAt, BmsAt, BiPredicate<Integer, Integer>)
	// 終了を含む指定範囲のトラックIDが収集されること
	@Test
	public void testCollectImageTracks3_Normal() {
		var c = chartForCollectImageTracks();
		var ids = c.collectImageTracks(BmsPoint.of(2, 0), BmsPoint.of(3, 0), (t, v) -> true);
		assertEquals(List.of(5, 7), List.copyOf(ids));
	}

	// collectImageTracks(BmsAt, BmsAt, BiPredicate<Integer, Integer>)
	// 楽曲位置情報が0件の時、空のセットが返されること
	@Test
	public void testCollectImageTracks3_Empty() {
		var c = chartEmpty();
		var ids = c.collectImageTracks(BmsPoint.of(0, 0), BmsPoint.of(10, 0), (t, v) -> true);
		assertTrue(ids.isEmpty());
	}

	// collectImageTracks(BmsAt, BmsAt, BiPredicate<Integer, Integer>)
	// 開始・終了が逆転しても例外はスローされず、空のセットが返されること
	@Test
	public void testCollectImageTracks3_ReverseRange() {
		var c = chartForCollectImageTracks();
		var ids = c.collectImageTracks(BmsPoint.of(10, 0), BmsPoint.of(0, 0), (t, v) -> true);
		assertTrue(ids.isEmpty());
	}

	// collectImageTracks(BmsAt, BmsAt, BiPredicate<Integer, Integer>)
	// 開始・終了に同じ値を指定すると、その位置と完全に一致する楽曲位置情報のみが収集対象になること
	@Test
	public void testCollectImageTracks3_Pinpoint() {
		var c = chartForCollectImageTracks();
		var ids = c.collectImageTracks(BmsPoint.of(2, 96), BmsPoint.of(2, 96), (t, v) -> true);
		assertEquals(List.of(7), List.copyOf(ids));
	}

	// collectImageTracks(BmsAt, BmsAt, BiPredicate<Integer, Integer>)
	// 最初の楽曲位置情報より前方の範囲を指定した場合、空のセットが返されること
	@Test
	public void testCollectImageTracks3_BehindHead() {
		var c = chartForCollectImageTracks();
		var ids = c.collectImageTracks(BmsPoint.of(0, 0), BmsPoint.of(0, 191), (t, v) -> true);
		assertTrue(ids.isEmpty());
	}

	// collectImageTracks(BmsAt, BmsAt, BiPredicate<Integer, Integer>)
	// 最後の楽曲位置情報より後方の範囲を指定した場合、空のセットが返されること
	@Test
	public void testCollectImageTracks3_AheadTail() {
		var c = chartForCollectImageTracks();
		var ids = c.collectImageTracks(BmsPoint.of(3, 97), BmsPoint.of(10, 0), (t, v) -> true);
		assertTrue(ids.isEmpty());
	}

	// collectImageTracks(BmsAt, BmsAt, BiPredicate<Integer, Integer>)
	// NullPointerException atBegin が null
	@Test
	public void testCollectImageTracks3_NullAtBegin() {
		var c = chartForCollectImageTracks();
		var ex = NullPointerException.class;
		assertThrows(ex, () -> c.collectImageTracks(null, BmsPoint.of(10, 0), (t, v) -> true));
	}

	// collectImageTracks(BmsAt, BmsAt, BiPredicate<Integer, Integer>)
	// NullPointerException atLast が null
	@Test
	public void testCollectImageTracks3_NullAtLast() {
		var c = chartForCollectImageTracks();
		var ex = NullPointerException.class;
		assertThrows(ex, () -> c.collectImageTracks(BmsPoint.of(0, 0), null, (t, v) -> true));
	}

	// collectImageTracks(BmsAt, BmsAt, BiPredicate<Integer, Integer>)
	// NullPointerException isCollect が null
	@Test
	public void testCollectImageTracks3_NullIsCollect() {
		var c = chartForCollectImageTracks();
		var ex = NullPointerException.class;
		assertThrows(ex, () -> c.collectImageTracks(BmsPoint.of(0, 0), BmsPoint.of(10, 0), null));
	}

	// collectImageTracks(int, double, int, double, BiPredicate<Integer, Integer>)
	// 終了を含む指定範囲のトラックIDが収集されること
	@Test
	public void testCollectImageTracks4_Normal() {
		var c = chartForCollectImageTracks();
		var ids = c.collectImageTracks(2, 0, 3, 0, (t, v) -> true);
		assertEquals(List.of(5, 7), List.copyOf(ids));
	}

	// collectImageTracks(int, double, int, double, BiPredicate<Integer, Integer>)
	// 楽曲位置情報が0件の時、空のセットが返されること
	@Test
	public void testCollectImageTracks4_Empty() {
		var c = chartEmpty();
		var ids = c.collectImageTracks(0, 0, 10, 0, (t, v) -> true);
		assertTrue(ids.isEmpty());
	}

	// collectImageTracks(int, double, int, double, BiPredicate<Integer, Integer>)
	// 開始・終了が逆転しても例外はスローされず、空のセットが返されること
	@Test
	public void testCollectImageTracks4_ReverseRange() {
		var c = chartForCollectImageTracks();
		var ids = c.collectImageTracks(10, 0, 0, 0, (t, v) -> true);
		assertTrue(ids.isEmpty());
	}

	// collectImageTracks(int, double, int, double, BiPredicate<Integer, Integer>)
	// 開始・終了に同じ値を指定すると、その位置と完全に一致する楽曲位置情報のみが収集対象になること
	@Test
	public void testCollectImageTracks4_Pinpoint() {
		var c = chartForCollectImageTracks();
		var ids = c.collectImageTracks(2, 96, 2, 96, (t, v) -> true);
		assertEquals(List.of(7), List.copyOf(ids));
	}

	// collectImageTracks(int, double, int, double, BiPredicate<Integer, Integer>)
	// 最初の楽曲位置情報より前方の範囲を指定した場合、空のセットが返されること
	@Test
	public void testCollectImageTracks4_BehindHead() {
		var c = chartForCollectImageTracks();
		var ids = c.collectImageTracks(0, 0, 0, 191, (t, v) -> true);
		assertTrue(ids.isEmpty());
	}

	// collectImageTracks(int, double, int, double, BiPredicate<Integer, Integer>)
	// 最後の楽曲位置情報より後方の範囲を指定した場合、空のセットが返されること
	@Test
	public void testCollectImageTracks4_AheadTail() {
		var c = chartForCollectImageTracks();
		var ids = c.collectImageTracks(3, 97, 10, 0, (t, v) -> true);
		assertTrue(ids.isEmpty());
	}

	// collectImageTracks(int, double, int, double, BiPredicate<Integer, Integer>)
	// NullPointerException isCollect が null
	@Test
	public void testCollectImageTracks4_NullIsCollect() {
		var c = chartForCollectImageTracks();
		var ex = NullPointerException.class;
		assertThrows(ex, () -> c.collectImageTracks(0, 0, 10, 0, null));
	}

	// collectImageTracks(int, int, BiPredicate<Integer, Integer>)
	// 終了を含む指定範囲のトラックIDが収集されること
	@Test
	public void testCollectImageTracks5_Normal() {
		var c = chartForCollectImageTracks();
		var ids = c.collectImageTracks(3, 5, (t, v) -> true);
		assertEquals(List.of(5, 7), List.copyOf(ids));
	}

	// collectImageTracks(int, int, BiPredicate<Integer, Integer>)
	// 楽曲位置情報が0件の時、空のセットが返されること
	@Test
	public void testCollectImageTracks5_Empty() {
		var c = chartEmpty();
		var ids = c.collectImageTracks(0, Integer.MAX_VALUE, (t, v) -> true);
		assertTrue(ids.isEmpty());
	}

	// collectImageTracks(int, int, BiPredicate<Integer, Integer>)
	// 開始・終了が逆転しても例外はスローされず、空のセットが返されること
	@Test
	public void testCollectImageTracks5_ReverseRange() {
		var c = chartForCollectImageTracks();
		var ids = c.collectImageTracks(Integer.MAX_VALUE, 0, (t, v) -> true);
		assertTrue(ids.isEmpty());
	}

	// collectImageTracks(int, int, BiPredicate<Integer, Integer>)
	// 開始・終了に同じ値を指定すると、その位置と完全に一致する楽曲位置情報のみが収集対象になること
	@Test
	public void testCollectImageTracks5_Pinpoint() {
		var c = chartForCollectImageTracks();
		var ids = c.collectImageTracks(4, 4, (t, v) -> true);
		assertEquals(List.of(7), List.copyOf(ids));
	}

	// collectImageTracks(int, int, BiPredicate<Integer, Integer>)
	// 最初の楽曲位置情報より前方の範囲を指定した場合、空のセットが返されること
	@Test
	public void testCollectImageTracks5_BehindHead() {
		var c = chartForCollectImageTracks();
		var ids = c.collectImageTracks(0, 0, (t, v) -> true);
		assertTrue(ids.isEmpty());
	}

	// collectImageTracks(int, int, BiPredicate<Integer, Integer>)
	// 最後の楽曲位置情報より後方の範囲を指定した場合、空のセットが返されること
	@Test
	public void testCollectImageTracks5_AheadTail() {
		var c = chartForCollectImageTracks();
		var ids = c.collectImageTracks(100, Integer.MAX_VALUE, (t, v) -> true);
		assertTrue(ids.isEmpty());
	}

	// collectImageTracks(int, int, BiPredicate<Integer, Integer>)
	// BGAが正しくテスター関数に渡されること
	@Test
	public void testCollectImageTracks5_BgaTest() {
		var c = chartForCollectImageTracks();
		var ids1 = c.collectImageTracks(1, 1, (t, v) -> {
			assertEquals((Integer)0, t);
			assertEquals((Integer)3, v);
			return true;
		});
		assertEquals(List.of(3), List.copyOf(ids1));
		var ids2 = c.collectImageTracks(4, 4, (t, v) -> {
			assertEquals((Integer)0, t);
			assertEquals((Integer)7, v);
			return true;
		});
		assertEquals(List.of(7), List.copyOf(ids2));
	}

	// collectImageTracks(int, int, BiPredicate<Integer, Integer>)
	// BGAレイヤーが正しくテスター関数に渡されること
	@Test
	public void testCollectImageTracks5_LayerTest() {
		var c = chartForCollectImageTracks();
		var ids1 = c.collectImageTracks(2, 2, (t, v) -> {
			assertEquals((Integer)1, t);
			assertEquals((Integer)5, v);
			return true;
		});
		assertEquals(List.of(5), List.copyOf(ids1));
		var ids2 = c.collectImageTracks(5, 5, (t, v) -> {
			assertEquals((Integer)1, t);
			assertEquals((Integer)5, v);
			return true;
		});
		assertEquals(List.of(5), List.copyOf(ids2));
	}

	// collectImageTracks(int, int, BiPredicate<Integer, Integer>)
	// プレーミス画像が正しくテスター関数に渡されること
	@Test
	public void testCollectImageTracks5_MissTest() {
		var c = chartForCollectImageTracks();
		var ids1 = c.collectImageTracks(3, 3, (t, v) -> {
			assertEquals((Integer)2, t);
			assertEquals((Integer)7, v);
			return true;
		});
		assertEquals(List.of(7), List.copyOf(ids1));
		var ids2 = c.collectImageTracks(6, 6, (t, v) -> {
			assertEquals((Integer)2, t);
			assertEquals((Integer)3, v);
			return true;
		});
		assertEquals(List.of(3), List.copyOf(ids2));
	}

	// collectImageTracks(int, int, BiPredicate<Integer, Integer>)
	// テスター関数が false を返すと収集対象外になること
	@Test
	public void testCollectImageTracks5_NotCollect() {
		var c = chartForCollectImageTracks();
		var ids = c.collectImageTracks(0, Integer.MAX_VALUE, (t, v) -> t != 1);
		assertEquals(List.of(3, 7), List.copyOf(ids));
	}

	// collectImageTracks(int, int, BiPredicate<Integer, Integer>)
	// IllegalArgumentException start が負の値
	@Test
	public void testCollectImageTracks5_NegativeStart() {
		var c = chartForCollectImageTracks();
		var ex = IllegalArgumentException.class;
		assertThrows(ex, () -> c.collectImageTracks(-1, Integer.MAX_VALUE, (t, v) -> true));
	}

	// collectImageTracks(int, int, BiPredicate<Integer, Integer>)
	// IllegalArgumentException last が負の値
	@Test
	public void testCollectImageTracks5_NegativeLast() {
		var c = chartForCollectImageTracks();
		var ex = IllegalArgumentException.class;
		assertThrows(ex, () -> c.collectImageTracks(0, -1, (t, v) -> true));
	}

	// collectImageTracks(int, int, BiPredicate<Integer, Integer>)
	// NullPointerException isCollect が null
	@Test
	public void testCollectImageTracks5_NullIsCollect() {
		var c = chartForCollectImageTracks();
		var ex = NullPointerException.class;
		assertThrows(ex, () -> c.collectImageTracks(0, Integer.MAX_VALUE, null));
	}

	// computeActualPlayTime(IntToDoubleFunction)
	// 楽曲位置＋サウンド再生時間が最も長い時間が返ること
	@Test
	public void testComputeActualPlayTime_Normal() {
		var c = chartForComputeActualPlayTime();
		var tm = 0.0;

		// BEAT
		tm = c.computeActualPlayTime(r -> BeMusicSound.getTrackId(r) == 1 ? 15.0 : 1.0);
		assertEquals(17.0, tm, 0.00000001);

		// LONG_ON(LN)
		tm = c.computeActualPlayTime(r -> BeMusicSound.getTrackId(r) == 2 ? 15.0 : 1.0);
		assertEquals(18.5, tm, 0.00000001);

		// LONG_ON(Switch-CN)
		tm = c.computeActualPlayTime(r -> BeMusicSound.getTrackId(r) == 3 ? 15.0 : 1.0);
		assertEquals(20.5, tm, 0.00000001);

		// LONG_ON(Scratch-CN)
		tm = c.computeActualPlayTime(r -> {
			var trackId = BeMusicSound.getTrackId(r);
			return trackId == 4 || trackId == 5 ? 15.0 : 1.0;
		});
		assertEquals(23.5, tm, 0.00000001); // CHARGE_OFF(Back-Spin)の音が考慮されること

		// Invisible
		tm = c.computeActualPlayTime(r -> BeMusicSound.getTrackId(r) == 7 ? 15.0 : 1.0);
		assertEquals(25.0, tm, 0.00000001);

		// Bgm
		tm = c.computeActualPlayTime(r -> BeMusicSound.getTrackId(r) == 9 ? 15.0 : 1.0);
		assertEquals(26.0, tm, 0.00000001);
	}

	// computeActualPlayTime(IntToDoubleFunction)
	// サウンド再生を伴わない可視オブジェはサウンド再生時間が加算されないこと(再生時間取得関数に入力されないこと)
	@Test
	public void testComputeActualPlayTime_Visible() {
		var unchecked = new HashSet<>(List.of(1, 2, 3, 4, 5));
		var c = chartForComputeActualPlayTime();
		c.computeActualPlayTime(r -> {
			if (BeMusicSound.isVisible(r)) {
				assertTrue(BeMusicSound.getNoteType(r).hasSound(BeMusicSound.getDevice(r)));
				assertTrue(unchecked.remove(BeMusicSound.getTrackId(r)));
			}
			return 0.0;
		});
		assertTrue(unchecked.isEmpty());
	}

	// computeActualPlayTime(IntToDoubleFunction)
	// 楽曲位置情報が存在しない譜面では0が返ること
	@Test
	public void testComputeActualPlayTime_Empty() {
		var c = chartEmpty();
		var tm = c.computeActualPlayTime(r -> { fail(); return 0.0; });
		assertEquals(0.0, tm, 0.0);
	}

	// computeActualPlayTime(IntToDoubleFunction)
	// NullPointerException getSoundTime が null
	@Test
	public void testComputeActualPlayTime_NullGetSoundTime() {
		var c = chartForComputeActualPlayTime();
		assertThrows(NullPointerException.class, () -> c.computeActualPlayTime(null));
	}

	// onCreate()
	// 正常：オブジェクト構築時に必ず呼ばれ、呼び出し時には各Getterでベースクラスが持つ値を正常に取得できること
	@Test
	public void testOnCreate_Normal() {
		var l = builder(testCommonContent()).createList();
		var s = BeMusicChart.create(l, () -> new TestOnCreateChart());
		assertTrue(s.isCalledOnCreate);
	}

	// #LNMODEのテスト
	// LNを指定するとロングノート終端が期待するノート種別になり、ノート数としてカウントされないこと
	@Test
	public void testCase_LnMode_Ln() {
		var c = contentFromFile();
		assertEquals(BeMusicLongNoteMode.LN, BeMusicMeta.getLnMode(c));
		var s = builder(c).createChart();
		assertEquals(12, s.getNoteCount());
		assertEquals(8, s.getLongNoteCount());
		var p = s.getPoint(1);
		assertEquals(BeMusicNoteType.LONG_OFF, p.getVisibleNoteType(BeMusicDevice.SWITCH11));
		assertEquals(BeMusicNoteType.LONG_OFF, p.getVisibleNoteType(BeMusicDevice.SWITCH12));
		assertEquals(BeMusicNoteType.LONG_OFF, p.getVisibleNoteType(BeMusicDevice.SWITCH13));
		assertEquals(BeMusicNoteType.LONG_OFF, p.getVisibleNoteType(BeMusicDevice.SWITCH14));
		assertEquals(BeMusicNoteType.LONG_OFF, p.getVisibleNoteType(BeMusicDevice.SWITCH15));
		assertEquals(BeMusicNoteType.LONG_OFF, p.getVisibleNoteType(BeMusicDevice.SWITCH16));
		assertEquals(BeMusicNoteType.LONG_OFF, p.getVisibleNoteType(BeMusicDevice.SWITCH17));
		assertEquals(BeMusicNoteType.LONG_OFF, p.getVisibleNoteType(BeMusicDevice.SCRATCH1));
	}

	// #LNMODEのテスト
	// CNを指定するとロングノート終端が期待するノート種別になり、ノート数としてカウントされること
	@Test
	public void testCase_LnMode_Cn() {
		var c = contentFromFile();
		assertEquals(BeMusicLongNoteMode.CN, BeMusicMeta.getLnMode(c));
		var s = builder(c).createChart();
		assertEquals(20, s.getNoteCount());
		assertEquals(16, s.getLongNoteCount());
		var p = s.getPoint(1);
		assertEquals(BeMusicNoteType.CHARGE_OFF, p.getVisibleNoteType(BeMusicDevice.SWITCH11));
		assertEquals(BeMusicNoteType.CHARGE_OFF, p.getVisibleNoteType(BeMusicDevice.SWITCH12));
		assertEquals(BeMusicNoteType.CHARGE_OFF, p.getVisibleNoteType(BeMusicDevice.SWITCH13));
		assertEquals(BeMusicNoteType.CHARGE_OFF, p.getVisibleNoteType(BeMusicDevice.SWITCH14));
		assertEquals(BeMusicNoteType.CHARGE_OFF, p.getVisibleNoteType(BeMusicDevice.SWITCH15));
		assertEquals(BeMusicNoteType.CHARGE_OFF, p.getVisibleNoteType(BeMusicDevice.SWITCH16));
		assertEquals(BeMusicNoteType.CHARGE_OFF, p.getVisibleNoteType(BeMusicDevice.SWITCH17));
		assertEquals(BeMusicNoteType.CHARGE_OFF, p.getVisibleNoteType(BeMusicDevice.SCRATCH1));
	}

	// #LNMODEのテスト
	// HCNを指定するとロングノート終端が期待するノート種別になり、ノート数としてカウントされること
	@Test
	public void testCase_LnMode_Hcn() {
		var c = contentFromFile();
		assertEquals(BeMusicLongNoteMode.HCN, BeMusicMeta.getLnMode(c));
		var s = builder(c).createChart();
		assertEquals(20, s.getNoteCount());
		assertEquals(16, s.getLongNoteCount());
		var p = s.getPoint(1);
		assertEquals(BeMusicNoteType.CHARGE_OFF, p.getVisibleNoteType(BeMusicDevice.SWITCH11));
		assertEquals(BeMusicNoteType.CHARGE_OFF, p.getVisibleNoteType(BeMusicDevice.SWITCH12));
		assertEquals(BeMusicNoteType.CHARGE_OFF, p.getVisibleNoteType(BeMusicDevice.SWITCH13));
		assertEquals(BeMusicNoteType.CHARGE_OFF, p.getVisibleNoteType(BeMusicDevice.SWITCH14));
		assertEquals(BeMusicNoteType.CHARGE_OFF, p.getVisibleNoteType(BeMusicDevice.SWITCH15));
		assertEquals(BeMusicNoteType.CHARGE_OFF, p.getVisibleNoteType(BeMusicDevice.SWITCH16));
		assertEquals(BeMusicNoteType.CHARGE_OFF, p.getVisibleNoteType(BeMusicDevice.SWITCH17));
		assertEquals(BeMusicNoteType.CHARGE_OFF, p.getVisibleNoteType(BeMusicDevice.SCRATCH1));
	}

	// #LNMODEのテスト
	// #LNMODE未指定だとLNと同じ状態になること
	@Test
	public void testCase_LnMode_Undefine() {
		var c = contentFromFile();
		assertEquals(BeMusicLongNoteMode.LN, BeMusicMeta.getLnMode(c));
		var s = builder(c).createChart();
		assertEquals(12, s.getNoteCount());
		assertEquals(8, s.getLongNoteCount());
		var p = s.getPoint(1);
		assertEquals(BeMusicNoteType.LONG_OFF, p.getVisibleNoteType(BeMusicDevice.SWITCH11));
		assertEquals(BeMusicNoteType.LONG_OFF, p.getVisibleNoteType(BeMusicDevice.SWITCH12));
		assertEquals(BeMusicNoteType.LONG_OFF, p.getVisibleNoteType(BeMusicDevice.SWITCH13));
		assertEquals(BeMusicNoteType.LONG_OFF, p.getVisibleNoteType(BeMusicDevice.SWITCH14));
		assertEquals(BeMusicNoteType.LONG_OFF, p.getVisibleNoteType(BeMusicDevice.SWITCH15));
		assertEquals(BeMusicNoteType.LONG_OFF, p.getVisibleNoteType(BeMusicDevice.SWITCH16));
		assertEquals(BeMusicNoteType.LONG_OFF, p.getVisibleNoteType(BeMusicDevice.SWITCH17));
		assertEquals(BeMusicNoteType.LONG_OFF, p.getVisibleNoteType(BeMusicDevice.SCRATCH1));
	}

	// ロングノートのテスト(RDM形式)
	// RDM形式の長押し開始～継続～終了のノート種別が正常に設定されていること
	@Test
	public void testCase_LongNote_Rdm() {
		var c = contentFromFile();
		var s = builder(c).createChart();
		assertTestCase_LongNote_Xxx(s);
	}

	// ロングノートのテスト(MGQ形式)
	// MGQ形式の長押し開始～継続～終了のノート種別が正常に設定されていること
	@Test
	public void testCase_LongNote_Mgq() {
		var c = contentFromFile();
		var s = builder(c).createChart();
		assertTestCase_LongNote_Xxx(s);
	}

	// ロングノートのテスト(RDM/MGQ形式混在)
	// #LNTYPEの定義に関わらずRDM/MGQ形式を正常に認識し、長押し開始～継続～終了のノート種別が正常に設定されていること
	@Test
	public void testCase_LongNote_MixedRdmMgq() {
		var c = contentFromFile();
		var s = builder(c).createChart();
		assertTestCase_LongNote_Xxx(s);
	}

	// スクロール速度のテスト
	// 楽曲位置情報に、スクロール速度の値が意図通りに設定されていること
	@Test
	public void testCase_Scroll() {
		var s = builder(contentFromFile()).createChart();
		assertEquals(10, s.getPointCount());
		var i = 0;
		var p = s.getPoint(i++);
		assertEquals(0, p.getMeasure());
		assertEquals(0.0, p.getTick(), 0.0);
		assertEquals(1.0, p.getCurrentScroll(), 0.0000001);
		p = s.getPoint(i++);
		assertEquals(0, p.getMeasure());
		assertEquals(96.0, p.getTick(), 0.0);
		assertEquals(1.0, p.getCurrentScroll(), 0.0000001);
		p = s.getPoint(i++);
		assertEquals(1, p.getMeasure());
		assertEquals(0.0, p.getTick(), 0.0);
		assertEquals(0.5, p.getCurrentScroll(), 0.0000001);
		p = s.getPoint(i++);
		assertEquals(1, p.getMeasure());
		assertEquals(48.0, p.getTick(), 0.0);
		assertEquals(0.5, p.getCurrentScroll(), 0.0000001);
		p = s.getPoint(i++);
		assertEquals(1, p.getMeasure());
		assertEquals(96.0, p.getTick(), 0.0);
		assertEquals(1.0, p.getCurrentScroll(), 0.0000001);
		p = s.getPoint(i++);
		assertEquals(2, p.getMeasure());
		assertEquals(0.0, p.getTick(), 0.0);
		assertEquals(2.0, p.getCurrentScroll(), 0.0000001);
		p = s.getPoint(i++);
		assertEquals(2, p.getMeasure());
		assertEquals(48.0, p.getTick(), 0.0);
		assertEquals(2.0, p.getCurrentScroll(), 0.0000001);
		p = s.getPoint(i++);
		assertEquals(2, p.getMeasure());
		assertEquals(96.0, p.getTick(), 0.0);
		assertEquals(0.7, p.getCurrentScroll(), 0.0000001);
		p = s.getPoint(i++);
		assertEquals(2, p.getMeasure());
		assertEquals(144.0, p.getTick(), 0.0);
		assertEquals(100000.0, p.getCurrentScroll(), 0.0000001);
		p = s.getPoint(i++);
		assertEquals(3, p.getMeasure());
		assertEquals(0.0, p.getTick(), 0.0);
		assertEquals(0.0, p.getCurrentScroll(), 0.0000001);
	}

	private static void assertTestCase_LongNote_Xxx(BeMusicChart s) {
		var p = s.getPoint(0);
		assertEquals(BeMusicNoteType.LONG_ON, p.getVisibleNoteType(BeMusicDevice.SWITCH11));
		p = s.getPoint(1);
		assertEquals(BeMusicNoteType.LONG, p.getVisibleNoteType(BeMusicDevice.SWITCH11));
		assertEquals(BeMusicNoteType.BEAT, p.getVisibleNoteType(BeMusicDevice.SWITCH16));
		p = s.getPoint(2);
		assertEquals(BeMusicNoteType.LONG, p.getVisibleNoteType(BeMusicDevice.SWITCH11));
		assertEquals(BeMusicNoteType.MINE, p.getVisibleNoteType(BeMusicDevice.SWITCH22));
		p = s.getPoint(3);
		assertEquals(BeMusicNoteType.LONG, p.getVisibleNoteType(BeMusicDevice.SWITCH11));
		assertEquals(BeMusicNoteType.LONG_ON, p.getVisibleNoteType(BeMusicDevice.SWITCH27));
		p = s.getPoint(4);
		assertEquals(BeMusicNoteType.LONG, p.getVisibleNoteType(BeMusicDevice.SWITCH11));
		assertEquals(BeMusicNoteType.LONG, p.getVisibleNoteType(BeMusicDevice.SWITCH27));
		assertTrue(p.hasBga());
		p = s.getPoint(5);
		assertEquals(BeMusicNoteType.LONG, p.getVisibleNoteType(BeMusicDevice.SWITCH11));
		assertEquals(BeMusicNoteType.LONG, p.getVisibleNoteType(BeMusicDevice.SWITCH27));
		assertTrue(p.hasLayer());
		p = s.getPoint(6);
		assertEquals(BeMusicNoteType.LONG, p.getVisibleNoteType(BeMusicDevice.SWITCH11));
		assertEquals(BeMusicNoteType.LONG, p.getVisibleNoteType(BeMusicDevice.SWITCH27));
		assertTrue(p.hasMiss());
		p = s.getPoint(7);
		assertEquals(BeMusicNoteType.LONG_OFF, p.getVisibleNoteType(BeMusicDevice.SWITCH11));
		assertEquals(BeMusicNoteType.LONG, p.getVisibleNoteType(BeMusicDevice.SWITCH27));
		p = s.getPoint(8);
		assertEquals(BeMusicNoteType.LONG, p.getVisibleNoteType(BeMusicDevice.SWITCH27));
		assertTrue(p.hasMeasureLength());
		p = s.getPoint(9);
		assertEquals(BeMusicNoteType.LONG, p.getVisibleNoteType(BeMusicDevice.SWITCH27));
		assertTrue(p.hasBgm());
		p = s.getPoint(10);
		assertEquals(BeMusicNoteType.LONG, p.getVisibleNoteType(BeMusicDevice.SWITCH27));
		assertTrue(p.hasBpm());
		p = s.getPoint(11);
		assertEquals(BeMusicNoteType.LONG, p.getVisibleNoteType(BeMusicDevice.SWITCH27));
		assertTrue(p.hasStop());
		p = s.getPoint(12);
		assertEquals(BeMusicNoteType.LONG_OFF, p.getVisibleNoteType(BeMusicDevice.SWITCH27));
	}

	private void testFloorPointOf_Normal(PointOfTester tester) {
		testFloorPointOf_Normal(tester, true);
	}

	private void testFloorPointOf_Normal(PointOfTester tester, boolean testOutOfRange) {
		var c = testCommonContent();
		var s = builder(c).createChart();

		// 譜面前半のインデックス(指定位置にデータなし)
		assertEquals(16, tester.pointOf(c, s, 1, 191));

		// 譜面前半のインデックス(指定位置にデータあり)
		assertEquals(21, tester.pointOf(c, s, 2, 48));

		// 譜面後半のインデックス(指定位置にデータなし)
		assertEquals(49, tester.pointOf(c, s, 6, 72));

		// 譜面後半のインデックス(指定位置にデータあり)
		assertEquals(51, tester.pointOf(c, s, 7, 0));

		// 譜面先頭のインデックス
		assertEquals(0, tester.pointOf(c, s, 0, 0));

		// 譜面末尾のインデックス
		assertEquals(57, tester.pointOf(c, s, 7, 180));

		// 該当なし
		if (testOutOfRange) {
			c = contentEmpty();
			s = builder(c).createChart();
			assertEquals(-1, tester.pointOf(c, s, 3, 0));
		}
	}

	private void testCeilPointOf_Normal(PointOfTester tester) {
		var c = testCommonContent();
		var s = builder(c).createChart();

		// 譜面前半のインデックス(指定位置にデータなし)
		assertEquals(30, tester.pointOf(c, s, 2, 150));

		// 譜面前半のインデックス(指定位置にデータあり)
		assertEquals(9, tester.pointOf(c, s, 1, 96));

		// 譜面後半のインデックス(指定位置にデータなし)
		assertEquals(51, tester.pointOf(c, s, 6, 143));

		// 譜面後半のインデックス(指定位置にデータあり)
		assertEquals(53, tester.pointOf(c, s, 7, 48));

		// 譜面先頭のインデックス
		assertEquals(0, tester.pointOf(c, s, 0, 0));

		// 譜面末尾のインデックス
		assertEquals(57, tester.pointOf(c, s, 7, 180));

		// 該当なし
		assertEquals(-1, tester.pointOf(c, s, 7, 181));
	}

	private static BmsContent contentEmpty() {
		return new BmsContent(BeMusicSpec.createV1(null, null));
	}

	private BmsContent contentFromFile() {
		var path = testDataPath(1);
		try {
			var handler = new BeMusicLoadHandler()
					.setEnableControlFlow(false);
			return new BmsStandardLoader()
					.setSpec(BeMusicSpec.createV1(null, null))
					.setHandler(handler)
					.setSyntaxErrorEnable(false)
					.setFixSpecViolation(false)
					.setIgnoreUnknownChannel(false)
					.setIgnoreUnknownMeta(false)
					.setIgnoreWrongData(false)
					.load(path);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static BeMusicChartBuilder builder(BmsContent content) {
		return new BeMusicChartBuilder(content, true);
	}

	private BmsContent testCommonContent() {
		return contentFromFile();
	}

	private BeMusicChart testCommonChart() {
		return builder(testCommonContent()).createChart();
	}

	private static BeMusicChart chartEmpty() {
		return builder(contentEmpty()).createChart();
	}

	private static BeMusicChart chartForCollectSoundTracks() {
		var c = new BmsContent(BeMusicSpec.LATEST);
		c.edit(() -> {
			c.setInitialBpm(120.0);
			c.putNote(BeMusicDevice.SWITCH11.getVisibleChannel().getNumber(), 1, 0,    // M=1, T=0: 2.0s
					BeMusicSound.makeValue(9, false, null));
			c.putNote(BeMusicDevice.SWITCH12.getVisibleChannel().getNumber(), 1, 96,   // M=1, T=96: 3.0s
					BeMusicSound.makeValue(8, true, null));
			c.putNote(BeMusicDevice.SWITCH13.getLongChannel().getNumber(), 2, 0,       // M=2, T=0: 4.0s
					BeMusicSound.makeValue(7, false, BeMusicLongNoteMode.CN));
			c.putNote(BeMusicDevice.SWITCH13.getLongChannel().getNumber(), 2, 48,      // M=2, T=48: 4.5s
					BeMusicSound.makeValue(7, false, BeMusicLongNoteMode.CN));
			c.putNote(BeMusicDevice.SWITCH14.getInvisibleChannel().getNumber(), 2, 96, // M=2, T=96: 5.0s
					BeMusicSound.makeValue(6, false, null));
			c.putNote(BeMusicDevice.SWITCH15.getMineChannel().getNumber(), 3, 0,       // M=3, T=0: 6.0s
					BeMusicSound.makeValue(5, false, null));
			c.putNote(BeMusicChannel.NUM_BGM, 0, 3, 96, 4);                            // M=3, T=96, CI=0: 7.0s
			c.putNote(BeMusicChannel.NUM_BGM, 1, 3, 96, 8);                            // M=3, T=96, CI=1: 7.0s
			c.putNote(BeMusicChannel.NUM_BGM, 0, 4, 0, 9);                             // M=4, T=0, CI=0: 8.0s
		});
		return builder(c).createChart();
	}

	private static BeMusicChart chartForCollectImageTracks() {
		var c = new BmsContent(BeMusicSpec.LATEST);
		c.edit(() -> {
			c.setInitialBpm(120.0);
			c.putNote(BeMusicChannel.NUM_BGA, 1, 0, 3);         // M=1, T=0: 2.0s
			c.putNote(BeMusicChannel.NUM_BGA_LAYER, 1, 96, 5);  // M=1, T=96: 3.0s
			c.putNote(BeMusicChannel.NUM_BGA_MISS, 2, 0, 7);    // M=2, T=0: 4.0s
			c.putNote(BeMusicChannel.NUM_BGA, 2, 96, 7);        // M=2, T=96: 5.0s
			c.putNote(BeMusicChannel.NUM_BGA_LAYER, 3, 0, 5);   // M=3, T=0: 6.0s
			c.putNote(BeMusicChannel.NUM_BGA_MISS, 3, 96, 3);   // M=3, T=96: 7.0s
		});
		return builder(c).createChart();
	}

	private static BeMusicChart chartForComputeActualPlayTime() {
		var c = new BmsContent(BeMusicSpec.LATEST);
		c.edit(() -> {
			c.setInitialBpm(120.0);
			c.putNote(BeMusicDevice.SWITCH11.getVisibleChannel().getNumber(), 1, 0,    // Visible: BEAT: 2.0s
					BeMusicSound.makeValue(1, false, null));
			c.putNote(BeMusicDevice.SWITCH12.getLongChannel().getNumber(), 1, 144,     // Visible: LONG_ON(LN): 3.5s
					BeMusicSound.makeValue(2, false, BeMusicLongNoteMode.LN));
			c.putNote(BeMusicDevice.SWITCH12.getLongChannel().getNumber(), 2, 48,      // Visible: LONG_OFF: 4.5s
					BeMusicSound.makeValue(2, false, BeMusicLongNoteMode.LN));
			c.putNote(BeMusicDevice.SWITCH13.getLongChannel().getNumber(), 2, 144,     // Visible: LONG_ON(CN): 5.5s
					BeMusicSound.makeValue(3, false, BeMusicLongNoteMode.CN));
			c.putNote(BeMusicDevice.SWITCH13.getLongChannel().getNumber(), 3, 48,      // Visible: CHARGE_OFF: 6.5s
					BeMusicSound.makeValue(3, false, BeMusicLongNoteMode.CN));
			c.putNote(BeMusicDevice.SCRATCH1.getLongChannel().getNumber(), 3, 144,     // Visible: LONG_ON(CN): 7.5s
					BeMusicSound.makeValue(4, false, BeMusicLongNoteMode.CN));
			c.putNote(BeMusicDevice.SCRATCH1.getLongChannel().getNumber(), 4, 48,      // Visible: CHARGE_OFF(Scr): 8.5s
					BeMusicSound.makeValue(5, false, BeMusicLongNoteMode.CN));
			c.putNote(BeMusicDevice.SWITCH15.getMineChannel().getNumber(), 4, 96,      // Visible: MINE: 9.0s
					BeMusicSound.makeValue(6, false, null));
			c.putNote(BeMusicDevice.SWITCH16.getInvisibleChannel().getNumber(), 5, 0,  // Invisible: 10.0s
					BeMusicSound.makeValue(7, false, null));
			c.putNote(BeMusicChannel.NUM_BGM, 0, 5, 96, 8);                            // Bgm: CI=0: 11.0s
			c.putNote(BeMusicChannel.NUM_BGM, 1, 5, 96, 9);                            // Bgm: CI=1: 11.0s
		});
		return builder(c).createChart();
	}

	private static void assertCommonChart(BeMusicChart s) {
		assertNotNull(s);
		assertEquals(58, s.getPointCount());
//		assertNoteCount(s, BeMusicDevice.SWITCH11, 7, 2, 4);
//		assertNoteCount(s, BeMusicDevice.SWITCH12, 8, 3, 1);
//		assertNoteCount(s, BeMusicDevice.SWITCH13, 3, 1, 6);
//		assertNoteCount(s, BeMusicDevice.SWITCH14, 7, 2, 2);
//		assertNoteCount(s, BeMusicDevice.SWITCH15, 2, 1, 2);
//		assertNoteCount(s, BeMusicDevice.SWITCH16, 9, 4, 2);
//		assertNoteCount(s, BeMusicDevice.SWITCH17, 8, 1, 1);
//		assertNoteCount(s, BeMusicDevice.SCRATCH1, 6, 1, 3);
//		assertNoteCount(s, BeMusicDevice.SWITCH21, 8, 1, 3);
//		assertNoteCount(s, BeMusicDevice.SWITCH22, 9, 4, 1);
//		assertNoteCount(s, BeMusicDevice.SWITCH23, 6, 2, 2);
//		assertNoteCount(s, BeMusicDevice.SWITCH24, 7, 2, 2);
//		assertNoteCount(s, BeMusicDevice.SWITCH25, 7, 1, 2);
//		assertNoteCount(s, BeMusicDevice.SWITCH26, 8, 3, 6);
//		assertNoteCount(s, BeMusicDevice.SWITCH27, 6, 1, 1);
//		assertNoteCount(s, BeMusicDevice.SCRATCH2, 6, 1, 4);
		assertEquals(3, s.getChangeBpmCount());
		assertEquals(5, s.getStopCount());
	}

	private static void assertCommonPoints(int count, IntFunction<BeMusicPoint> getter) {
		// 無作為抽出した3件をテストする
		var p = getter.apply(34);
		assertEquals(3, p.getMeasure());
		assertEquals(24, p.getTick(), 0.0);
		assertEquals(2, p.getNoteCount());
		assertEquals(1, p.getLongNoteCount());
		assertEquals(0, p.getMineCount());
		assertEquals(0, p.getBgmCount());
		assertEquals(150.0, p.getCurrentBpm(), 0.0);
		assertEquals(0.0, p.getStop(), 0.0);
		assertFalse(p.hasMeasureLine());
		assertFalse(p.hasBpm());
		assertFalse(p.hasStop());
		assertFalse(p.hasMeasureLength());
		assertTrue(p.hasBga());
		assertFalse(p.hasLayer());
		assertFalse(p.hasMiss());
		assertFalse(p.hasBgm());

		p = getter.apply(37);
		assertEquals(3, p.getMeasure());
		assertEquals(96, p.getTick(), 0.0);
		assertEquals(2, p.getNoteCount());
		assertEquals(2, p.getLongNoteCount());
		assertEquals(0, p.getMineCount());
		assertEquals(5, p.getBgmCount());
		assertEquals(150.0, p.getCurrentBpm(), 0.0);
		assertEquals(0.0, p.getStop(), 0.0);
		assertFalse(p.hasMeasureLine());
		assertFalse(p.hasBpm());
		assertFalse(p.hasStop());
		assertFalse(p.hasMeasureLength());
		assertFalse(p.hasBga());
		assertFalse(p.hasLayer());
		assertFalse(p.hasMiss());
		assertTrue(p.hasBgm());

		p = getter.apply(51);
		assertEquals(7, p.getMeasure());
		assertEquals(0, p.getTick(), 0.0);
		assertEquals(0, p.getNoteCount());
		assertEquals(0, p.getLongNoteCount());
		assertEquals(8, p.getMineCount());
		assertEquals(0, p.getBgmCount());
		assertEquals(120.0, p.getCurrentBpm(), 0.0);
		assertEquals(4.0, p.getStop(), 0.0);
		assertTrue(p.hasMeasureLine());
		assertFalse(p.hasBpm());
		assertTrue(p.hasStop());
		assertFalse(p.hasMeasureLength());
		assertTrue(p.hasBga());
		assertFalse(p.hasLayer());
		assertTrue(p.hasMiss());
		assertFalse(p.hasBgm());
	}
}
