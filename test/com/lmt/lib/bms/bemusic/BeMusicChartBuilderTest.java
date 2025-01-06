package com.lmt.lib.bms.bemusic;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.List;
import java.util.function.Predicate;

import org.junit.Test;

import com.lmt.lib.bms.BmsContent;
import com.lmt.lib.bms.Tests;

public class BeMusicChartBuilderTest {
	private static class PointForPointCreatorTest extends BeMusicPoint {
		boolean calledOnCreate = false;
		@Override protected void onCreate() { calledOnCreate = true; }
	}

	private static class ChartForChartCreatorTest extends BeMusicChart {
		boolean calledOnCreate = false;
		@Override protected void onCreate() { calledOnCreate = true; }
	}

	// BeMusicChartBuilder(BmsContent)
	// 正常：インスタンスを生成できること
	@Test
	public void testConstructor_Normal() {
		new BeMusicChartBuilder(content());
	}

	// BeMusicChartBuilder(BmsContent)
	// デフォルト設定では全ての情報がシーク対象になっていること
	@Test
	public void testConstructor_DefaultSeekTarget() throws Exception {
		var b = new BeMusicChartBuilder(content());
		assertSeek(b, true);
	}

	// BeMusicChartBuilder(BmsContent)
	// NullPointerException contentがnull
	@Test
	public void testConstructor_Exception_NullContent() {
		assertThrows(NullPointerException.class, () -> { new BeMusicChartBuilder(null); });
	}

	// BeMusicChartBuilder(BmsContent, boolean)
	// 全情報シークをtrueにすると全情報がシーク対象になっていること
	@Test
	public void testConstructor2_SeekAllOn() throws Exception {
		var b = new BeMusicChartBuilder(content(), true);
		assertSeek(b, true);
	}

	// BeMusicChartBuilder(BmsContent, boolean)
	// 全情報シークをfalseにすると全情報がシーク対象外になっていること
	@Test
	public void testConstructor2_SeekAllOff() throws Exception {
		var b = new BeMusicChartBuilder(content(), false);
		assertSeek(b, false);
	}

	// BeMusicChartBuilder(BmsContent, boolean)
	// NullPointerException contentがnull
	@Test
	public void testConstructor2_NullContent() {
		assertThrows(NullPointerException.class, () -> new BeMusicChartBuilder(null, true));
	}

	// setSeekMeasureLine(boolean)
	// 正常：trueを指定して譜面を構築するとT=0で空の楽曲位置が含まれること
	@Test
	public void testSetSeekMeasureLine_Normal_True() {
		var l = builder(content(), false).setSeekMeasureLine(true).createList();
		assertEquals(10, l.size());
		assertTrue(l.get(0).hasMeasureLine());
		assertTrue(l.get(1).hasMeasureLine());
		assertTrue(l.get(2).hasMeasureLine());
		assertTrue(l.get(3).hasMeasureLine());
		assertTrue(l.get(5).hasMeasureLine());
		assertTrue(l.get(6).hasMeasureLine());
		assertTrue(l.get(7).hasMeasureLine());
	}

	// 正常：falseを指定して譜面を構築するとT=0で空の楽曲位置が含まれないこと
	@Test
	public void testSetSeekMeasureLine_Normal_False() {
		var l = builder(content(), true).setSeekMeasureLine(false).createList();
		assertNotFound(l, p -> p.getMeasure() == 0 && p.getTick() == 0);
		assertNotFound(l, p -> p.getMeasure() == 1 && p.getTick() == 0);
		assertNotFound(l, p -> p.getMeasure() == 5 && p.getTick() == 0);
		assertNotFound(l, p -> p.getMeasure() == 6 && p.getTick() == 0);
	}

	// IllegalStateException BMS譜面生成中
	@Test
	public void testSetSeekMeasureLine_Exception_Seeking() {
		var b = builder(content(), false);
		b.first();
		assertThrows(IllegalStateException.class, () -> { b.setSeekMeasureLine(true); });
	}

	// setSeekVisible(boolean)
	// 正常：trueを指定して譜面を構築すると可視オブジェがある楽曲位置が含まれること
	@Test
	public void testSetSeekVisible_Normal_True() {
		var l = builder(content(), false).setSeekVisible(true).createList();
		assertEquals(8, l.size());

		var p = l.get(0);
		assertEquals(0, p.getMeasure());
		assertEquals(24, p.getTick(), 0.0);
		assertEquals(1, p.getNoteCount());
		assertEquals(BeMusicNoteType.BEAT, p.getVisibleNoteType(BeMusicDevice.SWITCH11));
		assertEquals(100, p.getVisibleValue(BeMusicDevice.SWITCH11));

		p = l.get(4);
		assertEquals(4, p.getMeasure());
		assertEquals(0, p.getTick(), 0.0);
		assertEquals(2, p.getNoteCount());
		assertEquals(BeMusicNoteType.BEAT, p.getVisibleNoteType(BeMusicDevice.SWITCH11));
		assertEquals(1000, p.getVisibleValue(BeMusicDevice.SWITCH11));
		assertEquals(BeMusicNoteType.LONG_ON, p.getVisibleNoteType(BeMusicDevice.SWITCH14));
		assertEquals(1001, p.getVisibleValue(BeMusicDevice.SWITCH14));

		p = l.get(5);
		assertEquals(4, p.getMeasure());
		assertEquals(48, p.getTick(), 0.0);
		assertEquals(1, p.getNoteCount());
		assertEquals(BeMusicNoteType.BEAT, p.getVisibleNoteType(BeMusicDevice.SWITCH12));
		assertEquals(1003, p.getVisibleValue(BeMusicDevice.SWITCH12));
		assertEquals(BeMusicNoteType.LONG_OFF, p.getVisibleNoteType(BeMusicDevice.SWITCH14));
		assertEquals(1295, p.getVisibleValue(BeMusicDevice.SWITCH14));
	}

	// setSeekVisible(boolean)
	// 正常：falseを指定して譜面を構築すると可視オブジェがある楽曲位置が含まれないこと
	@Test
	public void testSetSeekVisible_Normal_False() {
		var l = builder(content(), true).setSeekVisible(false).createList();
		assertNotFound(l, p -> p.getMeasure() == 0 && p.getTick() == 24);
		assertNotFound(l, p -> p.getMeasure() == 4 && p.getTick() == 0 && p.getVisibleNoteType(BeMusicDevice.SWITCH11) == BeMusicNoteType.BEAT);
		assertNotFound(l, p -> p.getMeasure() == 4 && p.getTick() == 0 && p.getVisibleNoteType(BeMusicDevice.SWITCH14) == BeMusicNoteType.LONG_ON);
		assertNotFound(l, p -> p.getMeasure() == 4 && p.getTick() == 48 && p.getVisibleNoteType(BeMusicDevice.SWITCH12) == BeMusicNoteType.BEAT);
		assertNotFound(l, p -> p.getMeasure() == 4 && p.getTick() == 48 && p.getVisibleNoteType(BeMusicDevice.SWITCH14) == BeMusicNoteType.LONG_OFF);
	}

	// setSeekVisible(boolean)
	// IllegalStateException BMS譜面生成中
	@Test
	public void testSetSeekVisible_Exception_Seeking() {
		var b = builder(content(), false);
		b.first();
		assertThrows(IllegalStateException.class, () -> { b.setSeekVisible(true); });
	}

	// setSeekInvisible(boolean)
	// 正常：trueを指定して譜面を構築すると不可視オブジェがある楽曲位置が含まれること
	@Test
	public void testSetSeekInvisible_Normal_True() {
		var l = builder(content(), false).setSeekInvisible(true).createList();
		assertEquals(6, l.size());

		var p = l.get(0);
		assertEquals(0, p.getMeasure());
		assertEquals(48, p.getTick(), 0.0);
		assertEquals(200, p.getInvisibleValue(BeMusicDevice.SWITCH15));
	}

	// setSeekInvisible(boolean)
	// 正常：falseを指定して譜面を構築すると不可視オブジェがある楽曲位置が含まれないこと
	@Test
	public void testSetSeekInvisible_Normal_False() {
		var l = builder(content(), true).setSeekInvisible(false).createList();
		assertNotFound(l, p -> p.getMeasure() == 0 && p.getTick() == 48);
	}

	// setSeekInvisible(boolean)
	// IllegalStateException BMS譜面生成中
	@Test
	public void testSetSeekInvisible_Exception_Seeking() {
		var b = builder(content(), false);
		b.first();
		assertThrows(IllegalStateException.class, () -> { b.setSeekInvisible(true); });
	}

	// setSeekMine(boolean)
	// 正常：trueを指定して譜面を構築すると地雷オブジェがある楽曲位置が含まれること
	@Test
	public void testSetSeekMine_Normal_True() {
		var l = builder(content(), false).setSeekMine(true).createList();
		assertEquals(8, l.size());

		var p = l.get(0);
		assertEquals(0, p.getMeasure());
		assertEquals(72, p.getTick(), 0.0);
		assertEquals(BeMusicNoteType.MINE, p.getVisibleNoteType(BeMusicDevice.SWITCH16));
		assertEquals(300, p.getVisibleValue(BeMusicDevice.SWITCH16));

		p = l.get(4);
		assertEquals(4, p.getMeasure());
		assertEquals(0, p.getTick(), 0.0);
		assertEquals(BeMusicNoteType.MINE, p.getVisibleNoteType(BeMusicDevice.SWITCH17));
		assertEquals(1002, p.getVisibleValue(BeMusicDevice.SWITCH17));

		p = l.get(5);
		assertEquals(4, p.getMeasure());
		assertEquals(48, p.getTick(), 0.0);
		assertEquals(BeMusicNoteType.MINE, p.getVisibleNoteType(BeMusicDevice.SWITCH16));
		assertEquals(1004, p.getVisibleValue(BeMusicDevice.SWITCH16));
	}

	// setSeekMine(boolean)
	// 正常：falseを指定して譜面を構築すると地雷オブジェがある楽曲位置が含まれないこと
	@Test
	public void testSetSeekMine_Normal_False() {
		var l = builder(content(), true).setSeekMine(false).createList();
		assertNotFound(l, p -> p.getMeasure() == 0 && p.getTick() == 72);
		assertNotFound(l, p -> p.getMeasure() == 4 && p.getTick() == 0 && p.getVisibleNoteType(BeMusicDevice.SWITCH17) == BeMusicNoteType.MINE);
		assertNotFound(l, p -> p.getMeasure() == 4 && p.getTick() == 48 && p.getVisibleNoteType(BeMusicDevice.SWITCH16) == BeMusicNoteType.MINE);
	}

	// setSeekMine(boolean)
	// IllegalStateException BMS譜面生成中
	@Test
	public void testSetSeekMine_Exception_Seeking() {
		var b = builder(content(), false);
		b.first();
		assertThrows(IllegalStateException.class, () -> { b.setSeekMine(true); });
	}

	// setSeekBgm(boolean)
	// 正常：trueを指定して譜面を構築するとBGMがある楽曲位置が含まれること
	@Test
	public void testSetSeekBgm_Normal_True() {
		var l = builder(content(), false).setSeekBgm(true).createList();
		assertEquals(7, l.size());

		var p = l.get(0);
		assertEquals(0, p.getMeasure());
		assertEquals(96, p.getTick(), 0.0);
		assertEquals(2, p.getBgmCount());
		assertEquals(400, p.getBgmValue(0));
		assertEquals(410, p.getBgmValue(1));

		p = l.get(4);
		assertEquals(6, p.getMeasure());
		assertEquals(96, p.getTick(), 0.0);
		assertEquals(1, p.getBgmCount());
		assertEquals(1010, p.getBgmValue(0));
	}

	// setSeekBgm(boolean)
	// 正常：falseを指定して譜面を構築するとBGMがある楽曲位置が含まれないこと
	@Test
	public void testSetSeekBgm_Normal_False() {
		var l = builder(content(), true).setSeekBgm(false).createList();
		assertNotFound(l, p -> p.getMeasure() == 0 && p.getTick() == 96);
		assertNotFound(l, p -> p.getMeasure() == 6 && p.getTick() == 96 && p.getBgmCount() == 1);
	}

	// setSeekBgm(boolean)
	// IllegalStateException BMS譜面生成中
	@Test
	public void testSetSeekBgm_Exception_Seeking() {
		var b = builder(content(), false);
		b.first();
		assertThrows(IllegalStateException.class, () -> { b.setSeekBgm(true); });
	}

	// setSeekBga(boolean)
	// 正常：trueを指定して譜面を構築するとBGAがある楽曲位置が含まれること
	@Test
	public void testSetSeekBga_Normal_True() {
		var l = builder(content(), false).setSeekBga(true).createList();
		assertEquals(9, l.size());

		var p = l.get(0);
		assertEquals(0, p.getMeasure());
		assertEquals(120, p.getTick(), 0.0);
		assertEquals(500, p.getBgaValue());

		p = l.get(1);
		assertEquals(0, p.getMeasure());
		assertEquals(144, p.getTick(), 0.0);
		assertEquals(600, p.getLayerValue());

		p = l.get(2);
		assertEquals(0, p.getMeasure());
		assertEquals(168, p.getTick(), 0.0);
		assertEquals(700, p.getMissValue());

		p = l.get(6);
		assertEquals(6, p.getMeasure());
		assertEquals(96, p.getTick(), 0.0);
		assertEquals(1020, p.getBgaValue());
		assertEquals(1021, p.getLayerValue());
		assertEquals(1022, p.getMissValue());
	}

	// setSeekBga(boolean)
	// 正常：falseを指定して譜面を構築するとBGAがある楽曲位置が含まれないこと
	@Test
	public void testSetSeekBga_Normal_False() {
		var l = builder(content(), true).setSeekBga(false).createList();
		assertNotFound(l, p -> p.getMeasure() == 0 && p.getTick() == 120);
		assertNotFound(l, p -> p.getMeasure() == 0 && p.getTick() == 144);
		assertNotFound(l, p -> p.getMeasure() == 0 && p.getTick() == 168);
		assertNotFound(l, p -> p.getMeasure() == 6 && p.getTick() == 96 && (p.getBgaValue() == 1020 || p.getLayerValue() == 1021 || p.getMissValue() == 1022));
	}

	// setSeekBga(boolean)
	// IllegalStateException BMS譜面生成中
	@Test
	public void testSetSeekBga_Exception_Seeking() {
		var b = builder(content(), false);
		b.first();
		assertThrows(IllegalStateException.class, () -> { b.setSeekBga(true); });
	}

	// setSeekText(boolean)
	// 正常：trueを指定して譜面を構築するとテキストがある楽曲位置が含まれること
	@Test
	public void testSetSeekText_Normal_True() {
		var l = builder(content(), false).setSeekText(true).createList();
		assertEquals(7, l.size());

		var p = l.get(0);
		assertEquals(1, p.getMeasure());
		assertEquals(24, p.getTick(), 0.0);
		assertEquals("TEXT1", p.getText());

		p = l.get(4);
		assertEquals(6, p.getMeasure());
		assertEquals(96, p.getTick(), 0.0);
		assertEquals("TEXT2", p.getText());
	}

	// setSeekText(boolean)
	// 正常：falseを指定して譜面を構築するとテキストがある楽曲位置が含まれないこと
	@Test
	public void testSetSeekText_Normal_False() {
		var l = builder(content(), true).setSeekText(false).createList();
		assertNotFound(l, p -> p.getMeasure() == 1 && p.getTick() == 24);
		assertNotFound(l, p -> p.getMeasure() == 6 && p.getTick() == 96 && !p.getText().isEmpty());
	}

	// setSeekText(boolean)
	// IllegalStateException BMS譜面生成中
	@Test
	public void testSetSeekText_Exception_Seeking() {
		var b = builder(content(), false);
		b.first();
		assertThrows(IllegalStateException.class, () -> { b.setSeekText(true); });
	}

	// setPointCreator(Supplier<BeMusicPoint>)
	// デフォルトではBeMusicPointが生成されること
	@Test
	public void testSetPointCreator_Default() {
		builder(content(), true).createList().forEach(p -> {
			assertEquals(BeMusicPoint.class, p.getClass());
		});
	}

	// setPointCreator(Supplier<BeMusicPoint>)
	// 関数が生成した楽曲位置情報がBMS譜面に組み込まれ、拡張情報が初期化済みであること
	@Test
	public void testSetPointCreator_Normal() {
		builder(content(), true).setPointCreator(PointForPointCreatorTest::new).createList().forEach(p -> {
			assertEquals(PointForPointCreatorTest.class, p.getClass());
			assertTrue(((PointForPointCreatorTest)p).calledOnCreate);
		});
	}

	// setPointCreator(Supplier<BeMusicPoint>)
	// IllegalStateException BMS譜面生成中
	@Test
	public void testSetPointCreator_Seeking() {
		var b = builder(content(), false);
		b.first();
		assertThrows(IllegalStateException.class, () -> b.setPointCreator(PointForPointCreatorTest::new));
	}

	// setPointCreator(Supplier<BeMusicPoint>)
	// NullPointerException creatorがnull
	@Test
	public void testSetPointCreator_NullCreator() {
		var b = builder(content(), false);
		assertThrows(NullPointerException.class, () -> b.setPointCreator(null));
	}

	// setPointCreator(Supplier<BeMusicPoint>)
	// NullPointerException 関数がnullを返した
	@Test
	public void testSetPointCreator_CreatorReturnedNull() {
		var b1 = builder(content(), true).setPointCreator(() -> null);
		assertThrows(NullPointerException.class, () -> b1.createList());
		var b2 = builder(content(), true).setPointCreator(() -> null);
		assertThrows(NullPointerException.class, () -> b2.first());
	}

	// setChartCreator(Supplier<BeMusicChart>)
	// デフォルトではBeMusicChartが生成されること
	@Test
	public void testSetChartCreator_Default() {
		var s = builder(content(), true).createChart();
		assertEquals(BeMusicChart.class, s.getClass());
	}

	// setPointCreator(Supplier<BeMusicPoint>)
	// 関数が生成したBMS譜面が返され、拡張情報が初期化済みであること
	@Test
	public void testSetChartCreator_Normal() {
		var s = builder(content(), true).setChartCreator(ChartForChartCreatorTest::new).createChart();
		assertEquals(ChartForChartCreatorTest.class, s.getClass());
		assertTrue(((ChartForChartCreatorTest)s).calledOnCreate);
	}

	// setPointCreator(Supplier<BeMusicPoint>)
	// IllegalStateException BMS譜面生成中
	@Test
	public void testSetChartCreator_Seeking() {
		var b = builder(content(), false);
		b.first();
		assertThrows(IllegalStateException.class, () -> b.setChartCreator(ChartForChartCreatorTest::new));
	}

	// setPointCreator(Supplier<BeMusicPoint>)
	// NullPointerException creatorがnull
	@Test
	public void testSetChartCreator_NullCreator() {
		var b = builder(content(), false);
		assertThrows(NullPointerException.class, () -> b.setChartCreator(null));
	}

	// setPointCreator(Supplier<BeMusicPoint>)
	// NullPointerException 関数がnullを返した
	@Test
	public void testSetChartCreator_CreatorReturnedNull() {
		var b = builder(content(), true).setChartCreator(() -> null);
		assertThrows(NullPointerException.class, () -> b.createChart());
	}

	// getContent()
	// 正常：コンストラクタで渡したBMSコンテンツが返されること
	@Test
	public void testGetContent() {
		var c = content();
		var b = builder(c, false);
		assertSame(c, b.getContent());
	}

	// first()
	// 正常：該当する楽曲位置の最初のオブジェクトが返されること(最初の位置が異なる複数のケースをテストする)
	@Test
	public void testFirst_Normal() {
		var c = content();
		var p = (BeMusicPoint)null;

		// 小節線のみ
		p = builder(c, false).setSeekMeasureLine(true).first();
		assertEquals(0, p.getMeasure());
		assertEquals(0, p.getTick(), 0.0);

		// 可視オブジェのみ
		p = builder(c, false).setSeekVisible(true).first();
		assertEquals(0, p.getMeasure());
		assertEquals(24, p.getTick(), 0.0);

		// 不可視オブジェのみ
		p = builder(c, false).setSeekInvisible(true).first();
		assertEquals(0, p.getMeasure());
		assertEquals(48, p.getTick(), 0.0);

		// 地雷オブジェのみ
		p = builder(c, false).setSeekMine(true).first();
		assertEquals(0, p.getMeasure());
		assertEquals(72, p.getTick(), 0.0);

		// BGMのみ
		p = builder(c, false).setSeekBgm(true).first();
		assertEquals(0, p.getMeasure());
		assertEquals(96, p.getTick(), 0.0);

		// BGAのみ
		p = builder(c, false).setSeekBga(true).first();
		assertEquals(0, p.getMeasure());
		assertEquals(120, p.getTick(), 0.0);

		// テキストのみ
		p = builder(c, false).setSeekText(true).first();
		assertEquals(1, p.getMeasure());
		assertEquals(24, p.getTick(), 0.0);
	}

	// first()
	// 正常：シーク中でも実行可能であること
	@Test
	public void testFirst_Normal_ExecutableWhileSeeking() {
		var b = builder(content(), true);
		b.first();
		b.first();
	}

	// first()
	// 準正常：条件に該当する楽曲位置が1件もない場合はnullを返すこと
	@Test
	public void testFirst_SemiNormal_NoPoint() {
		var p = builder(emptyContent(), true).setSeekMeasureLine(false).first();
		assertNull(p);
	}

	// first()
	// IllegalStateException 処理対象BMSコンテンツが参照モードではない
	@Test
	public void testFirst_Exception_ContentIsNotReferenceMode() {
		var c = content();
		var b = builder(c, true);
		c.beginEdit();
		assertThrows(IllegalStateException.class, () -> { b.first(); });
	}

	// next()
	// 正常：first()の次の楽曲位置が取得でき、次の楽曲位置がない時にnullを返すこと
	@Test
	public void testNext_Normal() {
		var b = builder(content(), false).setSeekMeasureLine(true).setSeekVisible(true);
		var p = (BeMusicPoint)b.first();
		assertNotNull(p);
		assertEquals(0, p.getMeasure());
		assertEquals(0, p.getTick(), 0.0);

		p = b.next();
		assertNotNull(p);
		assertEquals(0, p.getMeasure());
		assertEquals(24, p.getTick(), 0.0);

		p = b.next();
		assertNotNull(p);
		assertEquals(1, p.getMeasure());
		assertEquals(0, p.getTick(), 0.0);

		p = b.next();
		assertNotNull(p);
		assertEquals(2, p.getMeasure());
		assertEquals(0, p.getTick(), 0.0);

		p = b.next();
		assertNotNull(p);
		assertEquals(3, p.getMeasure());
		assertEquals(0, p.getTick(), 0.0);

		p = b.next();
		assertNotNull(p);
		assertEquals(3, p.getMeasure());
		assertEquals(96, p.getTick(), 0.0);

		p = b.next();
		assertNotNull(p);
		assertEquals(4, p.getMeasure());
		assertEquals(0, p.getTick(), 0.0);

		p = b.next();
		assertNotNull(p);
		assertEquals(4, p.getMeasure());
		assertEquals(48, p.getTick(), 0.0);

		p = b.next();
		assertNotNull(p);
		assertEquals(5, p.getMeasure());
		assertEquals(0, p.getTick(), 0.0);

		p = b.next();
		assertNotNull(p);
		assertEquals(6, p.getMeasure());
		assertEquals(0, p.getTick(), 0.0);

		p = b.next();
		assertNotNull(p);
		assertEquals(7, p.getMeasure());
		assertEquals(0, p.getTick(), 0.0);

		p = b.next();
		assertNotNull(p);
		assertEquals(7, p.getMeasure());
		assertEquals(96, p.getTick(), 0.0);

		p = b.next();
		assertNull(p);
	}

	// next()
	// 正常：次に該当する楽曲位置がない時にnullを返した後、Setterおよびfirst()が実行できるようになること
	@Test
	public void testNext_Normal_NullTerminate() {
		var b = builder(content(), false);
		b.first();
		while (b.next() != null) {}

		b.setSeekMeasureLine(true)
				.setSeekVisible(true)
				.setSeekInvisible(true)
				.setSeekMine(true)
				.setSeekBgm(true)
				.setSeekBga(true)
				.setSeekText(true);
		b.first();
	}

	// next()
	// IllegalStateException BMS譜面生成中ではない({@link #first()}が実行されていない)
	@Test
	public void testNext_Exception_NotSeeking() {
		var b = builder(content(), false);
		assertThrows(IllegalStateException.class, () -> { b.next(); });
	}

	// next()
	// IllegalStateException 処理対象BMSコンテンツが参照モードではない
	@Test
	public void testNext_Exception_ContentIsNotReferenceMode() {
		var c = content();
		var b = builder(c, false);
		b.first();
		c.beginEdit();
		assertThrows(IllegalStateException.class, () -> { b.next(); });
	}

	// createList()
	// 正常：楽曲位置の若い順に楽曲位置情報が格納されたリストが返ること
	@Test
	public void testCreateList_Normal() {
		var l = builder(content(), true).createList();
		assertTrue(l.size() > 0);
		var prevMeasure = -1;
		var prevTick = 0.0;
		var prevTime = -1.0;
		for (var i = 0; i < l.size(); i++) {
			var p = l.get(i);
			assertNotNull(p);
			assertTrue(p.getMeasure() >= prevMeasure);
			assertTrue((p.getMeasure() == prevMeasure && p.getTick() > prevTick) || (p.getMeasure() > prevMeasure));
			assertTrue(p.getTime() > prevTime);
			prevMeasure = p.getMeasure();
			prevTick = p.getTick();
			prevTime = p.getTime();
		}
	}

	// createList()
	// 正常：該当する楽曲位置情報が1件もない場合は空のリストが返ること
	@Test
	public void testCreateList_Normal_EmptyList() {
		var l = builder(emptyContent(), true).setSeekMeasureLine(false).createList();
		assertNotNull(l);
		assertEquals(0, l.size());
	}

	// createChart()
	// 正常：BeMusicChartのインスタンスが返ること
	// ※データ内容の検証はBeMusicChartの単体テストで厳密に行う
	@Test
	public void testCreateChart() {
		var s = builder(content(), true).createChart();
		assertNotNull(s);
		assertTrue(s instanceof BeMusicChart);
	}

	// createChart(BmsContent)
	// 全ての情報がシーク対象になったBMS譜面が構築されること
	@Test
	public void testCreateChartStatic_Normal() throws Exception {
		var el = new HashSet<>(List.of("line", "visible", "invisible", "mine", "bgm", "bga", "text"));
		var c = BeMusicChartBuilder.createChart(content());
		c.points().forEach(p -> {
			el.removeIf(e -> e.equals("line") && p.hasMeasureLine());
			el.removeIf(e -> e.equals("visible") && p.hasMovementNote());
			el.removeIf(e -> e.equals("invisible") && p.invisibles().anyMatch(n -> BeMusicSound.getTrackId(n) != 0));
			el.removeIf(e -> e.equals("mine") && p.hasMine());
			el.removeIf(e -> e.equals("bgm") && p.hasBgm());
			el.removeIf(e -> e.equals("bga") && p.hasBga());
			el.removeIf(e -> e.equals("text") && !p.getText().isEmpty());
		});
		assertTrue(el.isEmpty());
	}

	// createChart(BmsContent)
	// NullPointerException contentがnull
	@Test
	public void testCreateChartStatic_NullContent() {
		assertThrows(NullPointerException.class, () -> BeMusicChartBuilder.createChart(null));
	}

	// テストコードで使用するビルダーは、全てのシークオプションを指定パラメータの値にしておく
	private static BeMusicChartBuilder builder(BmsContent content, boolean seekOption) {
		return new BeMusicChartBuilder(content, seekOption);
	}

	// このクラスのテストコードは一部の例外を除いて全てこのメソッドで生成されるコンテンツを使用する
	private static BmsContent content() {
		var c = new BmsContent(BeMusicSpec.createV1(null, null));
		c.beginEdit();

		// ----- ヘッダ定義 -----
		BeMusicMeta.setGenre(c, "TEST");
		BeMusicMeta.setTitle(c, "BeMusicChartBuilder");
		BeMusicMeta.setSubTitle(c, "for JUnit");
		BeMusicMeta.setArtist(c, "J-SON3");
		BeMusicMeta.setSubArtist(c, 0, "13TREE");
		BeMusicMeta.setSubArtist(c, 1, "MA$TER");
		BeMusicMeta.setDifficulty(c, BeMusicDifficulty.NORMAL);
		BeMusicMeta.setPlayLevel(c, 5.0);
		BeMusicMeta.setRank(c, BeMusicRank.NORMAL);
		BeMusicMeta.setTotal(c, 500.0);

		c.setInitialBpm(120.0);
		BeMusicMeta.setBpm(c, 1, 180.0);
		BeMusicMeta.setBpm(c, 2, 240.0);
		BeMusicMeta.setStop(c, 1, 192.0);
		BeMusicMeta.setStop(c, 2, 384.0);
		c.setIndexedMeta(BeMusicMeta.TEXT.getName(), 1, "TEXT1");
		c.setIndexedMeta(BeMusicMeta.TEXT.getName(), 2, "TEXT2");
		BeMusicMeta.setLnObj(c, 0, 1295L);

		// ----- 譜面定義 -----
		/* M=07, T=96 */ // 譜面停止のみ
		c.putNote(BeMusicChannel.STOP.getNumber(), 7, 96.0, 2);  // 384ticks

		/* M=07, T=000 */ // BPM変更、譜面停止 /* --------------------- 7小節目 */
		c.putNote(BeMusicChannel.BPM.getNumber(), 7, 0.0, 2);  // 240BPM
		c.putNote(BeMusicChannel.STOP.getNumber(), 7, 0.0, 1);  // 192ticks

		/* M=06, T=096 */ // BGM1件、BGA/LAYER/MISS、テキスト
		c.putNote(BeMusicChannel.BGM.getNumber(), 6, 96.0, 1010);
		c.putNote(BeMusicChannel.BGA.getNumber(), 6, 96.0, 1020);
		c.putNote(BeMusicChannel.BGA_LAYER.getNumber(), 6, 96.0, 1021);
		c.putNote(BeMusicChannel.BGA_MISS.getNumber(), 6, 96.0, 1022);
		c.putNote(BeMusicChannel.TEXT.getNumber(), 6, 96.0, 2);  // TEXT2

		/* M=06, T=000 */ // データなし /* --------------------- 6小節目 */

		/* M=05, T=000 */ // データなし /* --------------------- 5小節目 */

		/* M=04, T=048 */ // 短押し、長押し終了、地雷、不可視
		c.putNote(BeMusicChannel.VISIBLE_1P_02.getNumber(), 4, 48.0, 1003);   // 短押し
		c.putNote(BeMusicChannel.VISIBLE_1P_04.getNumber(), 4, 48.0, 1295);   // 長押し終了
		c.putNote(BeMusicChannel.MINE_1P_08.getNumber(), 4, 48.0, 1004);      // 地雷

		/* M=04, T=000 */ // 短押し、長押し開始、地雷 /* --------------------- 4小節目 */
		c.putNote(BeMusicChannel.VISIBLE_1P_01.getNumber(), 4, 0.0, 1000);   // 短押し
		c.putNote(BeMusicChannel.VISIBLE_1P_04.getNumber(), 4, 0.0, 1001);   // 長押し開始
		c.putNote(BeMusicChannel.MINE_1P_09.getNumber(), 4, 0.0, 1002);      // 地雷

		/* M=03, T=096 */ // BPM変更のみ(旧式)
		c.putNote(BeMusicChannel.BPM_LEGACY.getNumber(), 3, 96.0, 120);  // 120BPM

		/* M=03, T=000 */ // BPM変更のみ(新式：小節線上) /* --------------------- 3小節目 */
		c.putNote(BeMusicChannel.BPM.getNumber(), 3, 0.0, 1);  // 180BPM

		/* M=02, T=000 */ // 小節長変更のみ /* --------------------- 2小節目 */
		c.setMeasureValue(BeMusicChannel.LENGTH.getNumber(), 2, 0.5);  // 2/4拍子

		/* M=01, T=024 */ // テキストのみ
		c.putNote(BeMusicChannel.TEXT.getNumber(), 1, 24.0, 1);  // "TEXT1"

		/* M=01, T=000 */ // データなし /* --------------------- 1小節目 */

		/* M=00, T=168 */ // MISSのみ
		c.putNote(BeMusicChannel.BGA_MISS.getNumber(), 0, 168.0, 700);

		/* M=00, T=144 */ // LAYERのみ
		c.putNote(BeMusicChannel.BGA_LAYER.getNumber(), 0, 144.0, 600);

		/* M=00, T=120 */ // BGAのみ
		c.putNote(BeMusicChannel.BGA.getNumber(), 0, 120.0, 500);

		/* M=00, T=096 */ // BGMのみ(2件)
		c.putNote(BeMusicChannel.BGM.getNumber(), 0, 0, 96.0, 400);
		c.putNote(BeMusicChannel.BGM.getNumber(), 1, 0, 96.0, 410);

		/* M=00, T=072 */ // 地雷オブジェのみ
		c.putNote(BeMusicChannel.MINE_1P_08.getNumber(), 0, 72.0, 300);

		/* M=00, T=048 */ // 不可視オブジェのみ
		c.putNote(BeMusicChannel.INVISIBLE_1P_05.getNumber(), 0, 48.0, 200);

		/* M=00, T=024 */ // 可視オブジェのみ
		c.putNote(BeMusicChannel.VISIBLE_1P_01.getNumber(), 0, 24.0, 100);

		/* M=00, T=000 */ // データなし /* --------------------- 0小節目 */

		c.endEdit();

		return c;
	}

	// 譜面オブジェクトの対象外ノートしかないBMSコンテンツ(空リストテスト用)
	private static BmsContent emptyContent() {
		var c = new BmsContent(BeMusicSpec.createV1(null, null));

		c.beginEdit();
		c.putNote(BeMusicChannel.NUM_EXT_OBJ, 0, 48.0, 1);
		c.putNote(BeMusicChannel.NUM_EXT_OBJ, 1, 96.0, 2);
		c.putNote(BeMusicChannel.NUM_EXT_OBJ, 3, 144.0, 3);
		c.endEdit();

		return c;
	}

	private static void assertSeek(BeMusicChartBuilder b, boolean seekable) throws Exception {
		assertEquals(seekable, Tests.getf(b, "mSeekMeasureLine"));
		assertEquals(seekable, Tests.getf(b, "mSeekVisible"));
		assertEquals(seekable, Tests.getf(b, "mSeekInvisible"));
		assertEquals(seekable, Tests.getf(b, "mSeekMine"));
		assertEquals(seekable, Tests.getf(b, "mSeekBgm"));
		assertEquals(seekable, Tests.getf(b, "mSeekBga"));
		assertEquals(seekable, Tests.getf(b, "mSeekText"));
	}

	private static <T> void assertNotFound(List<T> list, Predicate<T> tester) {
		for (var i = 0; i < list.size(); i++) {
			if (tester.test(list.get(i))) {
				var msg = String.format("list[%d]: Found an unexpected element.", i);
				throw new AssertionError(msg);
			}
		}
	}
}
