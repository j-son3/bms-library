package com.lmt.lib.bms.bemusic;

import static org.junit.Assert.*;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.lmt.lib.bms.BmsContent;
import com.lmt.lib.bms.BmsStandardLoader;
import com.lmt.lib.bms.Tests;
import com.lmt.lib.bms.UseTestData;
import com.lmt.lib.bms.internal.deltasystem.Ds;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class BeMusicStatisticsBuilderTest implements UseTestData {
	private static DeltaSystemTestPackage.Data sTestData;

	@Override
	public Path testDataDirectory() {
		return Path.of("test", "com", "lmt", "lib", "bms", "bemusic", "testdata", "bemusicstatisticsbuilder");
	}

	@Override
	public String testDataExtension() {
		return "bms";
	}

	@BeforeClass
	public static void setupClass() throws Exception {
		try (var testPackage = DeltaSystemTest.loadPackage(DeltaSystemTest.SP)) {
			sTestData = testPackage.load("bm01-01");
		}
	}

	// BeMusicStatisticsBuilder(BeMusicHeader, BeMusicChart)
	// 正常：ヘッダと譜面データが記憶されること
	@Test
	public void testBeMusicStatisticsBuilder_Normal() throws Exception {
		var c = new BmsContent(BeMusicSpec.createV1(null, null));
		var h = new BeMusicHeader(c);
		var s = new BeMusicChart();
		var b = new BeMusicStatisticsBuilder(h, s);
		assertSame(h, Tests.getf(b, "mHeader"));
		assertSame(s, Tests.getf(b, "mChart"));
	}

	// BeMusicStatisticsBuilder(BeMusicHeader, BeMusicChart)
	// NullPointerException headerがnull
	@Test
	public void testBeMusicStatisticsBuilder_NullHeader() {
		var h = new BeMusicChart();
		assertThrows(NullPointerException.class, () -> new BeMusicStatisticsBuilder(null, h));
	}

	// BeMusicStatisticsBuilder(BeMusicHeader, BeMusicChart)
	// NullPointerException chartがnull
	@Test
	public void testBeMusicStatisticsBuilder_NullChart() {
		var c = new BmsContent(BeMusicSpec.createV1(null, null));
		var h = new BeMusicHeader(c);
		assertThrows(NullPointerException.class, () -> new BeMusicStatisticsBuilder(h, null));
	}

	// BeMusicStatisticsBuilder(BmsContent)
	// 指定したBMSコンテンツから生成されたヘッダ情報・譜面データが入力データとして使用されること
	@Test
	public void testBeMusicStatisticsBuilderBmsContent_Normal() throws Exception {
		var c = BeMusic.loadContentFrom(testDataPath(), null, true);
		var b = new BeMusicStatisticsBuilder(c);
		var h = (BeMusicHeader)Tests.getf(b, "mHeader");
		var s = (BeMusicChart)Tests.getf(b, "mChart");
		assertNotNull(h);
		assertEquals("Builder by content", h.getTitle());
		assertNotNull(s);
		assertEquals(1, s.getPoint(0).getVisibleValue(BeMusicDevice.SWITCH11));
		assertEquals(2, s.getPoint(0).getVisibleValue(BeMusicDevice.SWITCH14));
		assertEquals(3, s.getPoint(0).getVisibleValue(BeMusicDevice.SWITCH17));
	}

	// BeMusicStatisticsBuilder(BmsContent)
	// NullPointerException contentがnull
	@Test
	public void testBeMusicStatisticsBuilderBmsContent_NullContent() {
		assertThrows(NullPointerException.class, () -> new BeMusicStatisticsBuilder(null));
	}

	// setSpanLength(double)
	// 正常：期間統計情報の長さが記憶されること
	@Test
	public void testSetSpanLength() throws Exception {
		var length = 2.345;
		var c = new BmsContent(BeMusicSpec.createV1(null, null));
		var b = new BeMusicStatisticsBuilder(new BeMusicHeader(c), new BeMusicChart());
		assertSame(b, b.setSpanLength(length));
		assertEquals(length, (double)Tests.getf(b, "mLength"), 0.0);
	}

	// setNoteLayout(BeMusicNoteLayout)
	// 正常：ノートレイアウトが記憶されること
	@Test
	public void testSetNoteLayout() throws Exception {
		var layout = new BeMusicNoteLayout("1234567");
		var c = new BmsContent(BeMusicSpec.createV1(null, null));
		var b = new BeMusicStatisticsBuilder(new BeMusicHeader(c), new BeMusicChart());
		assertSame(b, b.setNoteLayout(layout));
		assertSame(layout, Tests.getf(b, "mLayout"));
	}

	// addRating(BeMusicRatingType...)
	// 指定したレーティング種別が追加されること
	@Test
	public void testAddRating_Normal() throws Exception {
		var c = new BmsContent(BeMusicSpec.createV1(null, null));
		var b = new BeMusicStatisticsBuilder(new BeMusicHeader(c), new BeMusicChart());
		assertSame(b, b.addRating(BeMusicRatingType.POWER));
		List<BeMusicRatingType> r = Tests.getf(b, "mRatings");
		assertEquals(1, r.size());
		assertEquals(BeMusicRatingType.POWER, r.get(0));
	}

	// addRating(BeMusicRatingType...)
	// 複数のレーティング種別が1回の呼び出しで追加できること
	@Test
	public void testAddRating_MultiRatings() throws Exception {
		var c = new BmsContent(BeMusicSpec.createV1(null, null));
		var b = new BeMusicStatisticsBuilder(new BeMusicHeader(c), new BeMusicChart());
		assertSame(b, b.addRating(BeMusicRatingType.COMPLEX, BeMusicRatingType.SCRATCH));
		List<BeMusicRatingType> r = Tests.getf(b, "mRatings");
		assertEquals(2, r.size());
		assertEquals(BeMusicRatingType.COMPLEX, r.get(0));
		assertEquals(BeMusicRatingType.SCRATCH, r.get(1));
	}

	// addRating(BeMusicRatingType...)
	// 既に追加済みのレーティング種別を追加しても破棄されること
	@Test
	public void testAddRating_Conflict() throws Exception {
		var c = new BmsContent(BeMusicSpec.createV1(null, null));
		var b = new BeMusicStatisticsBuilder(new BeMusicHeader(c), new BeMusicChart());
		assertSame(b, b.addRating(BeMusicRatingType.RHYTHM, BeMusicRatingType.RHYTHM));
		List<BeMusicRatingType> r = Tests.getf(b, "mRatings");
		assertEquals(1, r.size());
		assertEquals(BeMusicRatingType.RHYTHM, r.get(0));
	}

	// addRating(BeMusicRatingType...)
	// nullを指定しても何も追加されないこと
	@Test
	public void testAddRating_Null() throws Exception {
		var c = new BmsContent(BeMusicSpec.createV1(null, null));
		var b = new BeMusicStatisticsBuilder(new BeMusicHeader(c), new BeMusicChart());
		assertSame(b, b.addRating(BeMusicRatingType.POWER, null));
		List<BeMusicRatingType> r = Tests.getf(b, "mRatings");
		assertEquals(1, r.size());
		assertEquals(BeMusicRatingType.POWER, r.get(0));
	}

	// addRating(C)
	// 指定したレーティング種別が追加されること
	@Test
	public void testAddRatingCollection_Normal() throws Exception {
		var b = new BeMusicStatisticsBuilder(new BmsContent(BeMusicSpec.LATEST));
		var l = List.of(BeMusicRatingType.POWER, BeMusicRatingType.SCRATCH);
		assertSame(b, b.addRating(l));
		List<BeMusicRatingType> r = Tests.getf(b, "mRatings");
		assertEquals(r, l);
	}

	// addRating(C)
	// 既に追加済みのレーティング種別を追加しても破棄されること
	@Test
	public void testAddRatingCollection_Conflict() throws Exception {
		var b = new BeMusicStatisticsBuilder(new BmsContent(BeMusicSpec.LATEST));
		var l1 = List.of(BeMusicRatingType.POWER, BeMusicRatingType.SCRATCH);
		var l2 = List.of(BeMusicRatingType.POWER, BeMusicRatingType.GIMMICK);
		b.addRating(l1);
		b.addRating(l2);
		List<BeMusicRatingType> r = Tests.getf(b, "mRatings");
		assertEquals(3, r.size());
		assertEquals(BeMusicRatingType.POWER, r.get(0));
		assertEquals(BeMusicRatingType.SCRATCH, r.get(1));
		assertEquals(BeMusicRatingType.GIMMICK, r.get(2));
	}

	// addRating(C)
	// nullを指定しても何も追加されないこと
	@Test
	public void testAddRatingCollection_Null() throws Exception {
		var b = new BeMusicStatisticsBuilder(new BmsContent(BeMusicSpec.LATEST));
		var l = new ArrayList<BeMusicRatingType>();
		l.add(BeMusicRatingType.COMPLEX);
		l.add(null);
		l.add(BeMusicRatingType.SCRATCH);
		b.addRating(l);
		List<BeMusicRatingType> r = Tests.getf(b, "mRatings");
		assertEquals(2, r.size());
		assertEquals(BeMusicRatingType.COMPLEX, r.get(0));
		assertEquals(BeMusicRatingType.SCRATCH, r.get(1));
	}

	// addRating(C)
	// NullPointerException ratingTypesがnull
	@Test
	public void testAddRatingCollection_NullRatingTypes() throws Exception {
		var b = new BeMusicStatisticsBuilder(new BmsContent(BeMusicSpec.LATEST));
		assertThrows(NullPointerException.class, () -> b.addRating((List<BeMusicRatingType>)null));
	}

	// statistics()
	// IllegalStateException 集計を行ったビルダーで再度集計を行おうとした
	@Test
	public void testStatistics_ReStatistics() {
		var c = new BmsContent(BeMusicSpec.createV1(null, null));
		var s = new BeMusicChart();
		var p = new BeMusicPoint();
		s.setup(List.of(p));
		var b = new BeMusicStatisticsBuilder(new BeMusicHeader(c), s);
		b.statistics();
		assertThrows(IllegalStateException.class, () -> b.statistics());
	}

	// statistics()
	// IllegalStateException 期間統計情報の長さが{@link BeMusicTimeSpan#MIN_SPAN}未満
	@Test
	public void testStatistics_SpanLengthUnderflow() {
		var c = new BmsContent(BeMusicSpec.createV1(null, null));
		var b = new BeMusicStatisticsBuilder(new BeMusicHeader(c), new BeMusicChart());
		b.setSpanLength(Math.nextDown(BeMusicTimeSpan.MIN_SPAN));
		assertThrows(IllegalStateException.class, () -> b.statistics());
	}

	// statistics()
	// IllegalStateException 期間統計情報の長さが{@link BeMusicTimeSpan#MAX_SPAN}超過
	@Test
	public void testStatistics_SpanLengthOverflow() {
		var c = new BmsContent(BeMusicSpec.createV1(null, null));
		var b = new BeMusicStatisticsBuilder(new BeMusicHeader(c), new BeMusicChart());
		b.setSpanLength(Math.nextUp(BeMusicTimeSpan.MAX_SPAN));
		assertThrows(IllegalStateException.class, () -> b.statistics());
	}

	// statistics()
	// IllegalStateException ノートレイアウトがnull
	@Test
	public void testStatistics_NullNoteLayout() {
		var c = new BmsContent(BeMusicSpec.createV1(null, null));
		var b = new BeMusicStatisticsBuilder(new BeMusicHeader(c), new BeMusicChart());
		b.setNoteLayout(null);
		assertThrows(IllegalStateException.class, () -> b.statistics());
	}

	// statistics()
	// 期間統計情報のリストと期間のリンク(Previous-Next)が想定通りになっていること
	@Test
	public void testStatistics_LinkageSpan() throws Exception {
		var stat = loadTestStatistics();
		assertEquals(4, stat.getSpanCount());
		var s1 = stat.getSpan(0);
		var s2 = stat.getSpan(1);
		var s3 = stat.getSpan(2);
		var s4 = stat.getSpan(3);
		// s1 -----
		assertEquals(0, s1.getIndex());
		assertSame(s1, s1.getPrevious());
		assertSame(s2, s1.getNext());
		assertTrue(s1.isFirstSpan());
		assertFalse(s1.isLastSpan());
		// s2 -----
		assertEquals(1, s2.getIndex());
		assertSame(s1, s2.getPrevious());
		assertSame(s3, s2.getNext());
		assertFalse(s2.isFirstSpan());
		assertFalse(s2.isLastSpan());
		// s3 -----
		assertEquals(2, s3.getIndex());
		assertSame(s2, s3.getPrevious());
		assertSame(s4, s3.getNext());
		assertFalse(s3.isFirstSpan());
		assertFalse(s3.isLastSpan());
		// s4 -----
		assertEquals(3, s4.getIndex());
		assertSame(s3, s4.getPrevious());
		assertSame(s4, s4.getNext());
		assertFalse(s4.isFirstSpan());
		assertTrue(s4.isLastSpan());
	}

	// statistics()
	// 期間統計情報の長さと時間が想定通りの値になっていること
	@Test
	public void testStatistics_SpanLengthAndTime() throws Exception {
		var stat = loadTestStatistics(0.8, BeMusicNoteLayout.SP_REGULAR);
		assertEquals(5, stat.getSpanCount());
		var sa = IntStream.range(0, 5).mapToObj(n -> stat.getSpan(n)).collect(Collectors.toList());
		var delta = 0.00000001;
		var i = 0;
		// s1 -----
		var s = sa.get(i++);
		assertEquals(0.0, s.getBeginTime(), delta);
		assertEquals(0.8, s.getEndTime(), delta);
		// s2 -----
		s = sa.get(i++);
		assertEquals(0.4, s.getBeginTime(), delta);
		assertEquals(1.2, s.getEndTime(), delta);
		// s3 -----
		s = sa.get(i++);
		assertEquals(0.8, s.getBeginTime(), delta);
		assertEquals(1.6, s.getEndTime(), delta);
		// s4 -----
		s = sa.get(i++);
		assertEquals(1.2, s.getBeginTime(), delta);
		assertEquals(2.0, s.getEndTime(), delta);
		// s5 -----
		s = sa.get(i++);
		assertEquals(1.6, s.getBeginTime(), delta);
		assertEquals(2.4, s.getEndTime(), delta);
	}

	// statistics()
	// 期間内の楽曲位置情報が想定通りに格納されていること
	@Test
	public void testStatistics_PointIndices() throws Exception {
		var stat = loadTestStatistics();
		assertEquals(4, stat.getSpanCount());
		var sa = IntStream.range(0, 4).mapToObj(n -> stat.getSpan(n)).collect(Collectors.toList());
		var i = 0;
		// s1 -----
		var s = sa.get(i++);
		assertEquals(0, s.getFirstPointIndex());
		assertEquals(5, s.getLastPointIndex());
		// s2 -----
		s = sa.get(i++);
		assertEquals(4, s.getFirstPointIndex());
		assertEquals(9, s.getLastPointIndex());
		// s3 -----
		s = sa.get(i++);
		assertEquals(6, s.getFirstPointIndex());
		assertEquals(13, s.getLastPointIndex());
		// s4 -----
		s = sa.get(i++);
		assertEquals(10, s.getFirstPointIndex());
		assertEquals(14, s.getLastPointIndex());
	}

	// statistics()
	// 注視点・注視点変動係数の値は指定レイアウトが反映された想定通りの値であること(シングルプレー)
	@Test
	public void testStatistics_GazePointSp() throws Exception {
		var stat = loadTestStatistics(1.0, BeMusicNoteLayout.SP_MIRROR);
		assertEquals(4, stat.getSpanCount());
		var sa = IntStream.range(0, 4).mapToObj(n -> stat.getSpan(n)).collect(Collectors.toList());
		var delta = 0.0001;
		var i = 0;
		// s1 -----
		var s = sa.get(i++);
		assertEquals(0.0714, s.getGazePoint(), delta);
		assertEquals(0.3571, s.getGazePointR(), delta);
		assertEquals(0.4642, s.getGazeSwingley(), delta);
		assertEquals(0.1785, s.getGazeSwingleyR(), delta);
		// s2 -----
		s = sa.get(i++);
		assertEquals(1.0000, s.getGazePoint(), delta);
		assertEquals(0.7142, s.getGazePointR(), delta);
		assertEquals(0.9642, s.getGazeSwingley(), delta);
		assertEquals(0.5357, s.getGazeSwingleyR(), delta);
		// s3 -----
		s = sa.get(i++);
		assertEquals(0.0000, s.getGazePoint(), delta);
		assertEquals(0.0000, s.getGazePointR(), delta);
		assertEquals(0.0000, s.getGazeSwingley(), delta);
		assertEquals(0.0000, s.getGazeSwingleyR(), delta);
		// s4 -----
		s = sa.get(i++);
		assertEquals(-0.1428, s.getGazePoint(), delta);
		assertEquals(0.7142, s.getGazePointR(), delta);
		assertEquals(0.0714, s.getGazeSwingley(), delta);
		assertEquals(0.3571, s.getGazeSwingleyR(), delta);
		// stat -----
		assertEquals(0.3095, stat.getAverageGazePoint(), delta);
		assertEquals(0.5952, stat.getAverageGazePointR(), delta);
		assertEquals(0.5000, stat.getAverageGazeSwingley(), delta);
		assertEquals(0.3571, stat.getAverageGazeSwingleyR(), delta);
	}

	// statistics()
	// 注視点・注視点変動係数の値は指定レイアウトが反映された想定通りの値であること(ダブルプレー)
	@Test
	public void testStatistics_GazePointDp() throws Exception {
		var stat = loadTestStatistics(1.0, new BeMusicNoteLayout("GFEDCBA", "7654321"));
		assertEquals(4, stat.getSpanCount());
		var sa = IntStream.range(0, 4).mapToObj(n -> stat.getSpan(n)).collect(Collectors.toList());
		var delta = 0.0001;
		var i = 0;
		// s1 -----
		var s = sa.get(i++);
		assertEquals(0.3333, s.getGazePoint(), delta);
		assertEquals(0.0000, s.getGazePointR(), delta);
		assertEquals(0.0666, s.getGazeSwingley(), delta);
		assertEquals(0.0000, s.getGazeSwingleyR(), delta);
		// s2 -----
		s = sa.get(i++);
		assertEquals(0.2000, s.getGazePoint(), delta);
		assertEquals(0.0000, s.getGazePointR(), delta);
		assertEquals(0.1666, s.getGazeSwingley(), delta);
		assertEquals(0.0000, s.getGazeSwingleyR(), delta);
		// s3 -----
		s = sa.get(i++);
		assertEquals(0.0000, s.getGazePoint(), delta);
		assertEquals(0.0000, s.getGazePointR(), delta);
		assertEquals(0.0000, s.getGazeSwingley(), delta);
		assertEquals(0.0000, s.getGazeSwingleyR(), delta);
		// s4 -----
		s = sa.get(i++);
		assertEquals(-0.1333, s.getGazePoint(), delta);
		assertEquals(0.0000, s.getGazePointR(), delta);
		assertEquals(0.0666, s.getGazeSwingley(), delta);
		assertEquals(0.0000, s.getGazeSwingleyR(), delta);
		// stat -----
		assertEquals(0.1333, stat.getAverageGazePoint(), delta);
		assertEquals(0.0000, stat.getAverageGazePointR(), delta);
		assertEquals(0.1000, stat.getAverageGazeSwingley(), delta);
		assertEquals(0.0000, stat.getAverageGazeSwingleyR(), delta);
	}

	// statistics()
	// 視野幅・視野幅変動係数の値は指定レイアウトが反映された想定通りの値であること(シングルプレー)
	@Test
	public void testStatistics_ViewWidthSp() throws Exception {
		var stat = loadTestStatistics(1.0, BeMusicNoteLayout.SP_MIRROR);
		assertEquals(4, stat.getSpanCount());
		var sa = IntStream.range(0, 4).mapToObj(n -> stat.getSpan(n)).collect(Collectors.toList());
		var delta = 0.0001;
		var i = 0;
		// s1 -----
		var s = sa.get(i++);
		assertEquals(0.2492, s.getViewWidth(), delta);
		assertEquals(0.2492, s.getViewWidthR(), delta);
		assertEquals(0.0896, s.getViewSwingley(), delta);
		assertEquals(0.0896, s.getViewSwingleyR(), delta);
		// s2 -----
		s = sa.get(i++);
		assertEquals(0.0700, s.getViewWidth(), delta);
		assertEquals(0.0700, s.getViewWidthR(), delta);
		assertEquals(0.1246, s.getViewSwingley(), delta);
		assertEquals(0.1246, s.getViewSwingleyR(), delta);
		// s3 -----
		s = sa.get(i++);
		assertEquals(0.0000, s.getViewWidth(), delta);
		assertEquals(0.0000, s.getViewWidthR(), delta);
		assertEquals(0.0000, s.getViewSwingley(), delta);
		assertEquals(0.0000, s.getViewSwingleyR(), delta);
		// s4 -----
		s = sa.get(i++);
		assertEquals(0.8571, s.getViewWidth(), delta);
		assertEquals(0.2857, s.getViewWidthR(), delta);
		assertEquals(0.4285, s.getViewSwingley(), delta);
		assertEquals(0.1428, s.getViewSwingleyR(), delta);
		// stat -----
		assertEquals(0.3921, stat.getAverageViewWidth(), delta);
		assertEquals(0.2016, stat.getAverageViewWidthR(), delta);
		assertEquals(0.2142, stat.getAverageViewSwingley(), delta);
		assertEquals(0.1190, stat.getAverageViewSwingleyR(), delta);
	}

	// statistics()
	// 視野幅・視野幅変動係数の値は指定レイアウトが反映された想定通りの値であること(ダブルプレー)
	@Test
	public void testStatistics_ViewWidthDp() throws Exception {
		var stat = loadTestStatistics(1.0, new BeMusicNoteLayout("GFEDCBA", "7654321"));
		assertEquals(4, stat.getSpanCount());
		var sa = IntStream.range(0, 4).mapToObj(n -> stat.getSpan(n)).collect(Collectors.toList());
		var delta = 0.0001;
		var i = 0;
		// s1 -----
		var s = sa.get(i++);
		assertEquals(0.4150, s.getViewWidth(), delta);
		assertEquals(0.0000, s.getViewWidthR(), delta);
		assertEquals(0.1925, s.getViewSwingley(), delta);
		assertEquals(0.0000, s.getViewSwingleyR(), delta);
		// s2 -----
		s = sa.get(i++);
		assertEquals(0.0300, s.getViewWidth(), delta);
		assertEquals(0.0000, s.getViewWidthR(), delta);
		assertEquals(0.2075, s.getViewSwingley(), delta);
		assertEquals(0.0000, s.getViewSwingleyR(), delta);
		// s3 -----
		s = sa.get(i++);
		assertEquals(0.0000, s.getViewWidth(), delta);
		assertEquals(0.0000, s.getViewWidthR(), delta);
		assertEquals(0.0000, s.getViewSwingley(), delta);
		assertEquals(0.0000, s.getViewSwingleyR(), delta);
		// s4 -----
		s = sa.get(i++);
		assertEquals(0.0666, s.getViewWidth(), delta);
		assertEquals(0.0000, s.getViewWidthR(), delta);
		assertEquals(0.0333, s.getViewSwingley(), delta);
		assertEquals(0.0000, s.getViewSwingleyR(), delta);
		// stat -----
		assertEquals(0.1705, stat.getAverageViewWidth(), delta);
		assertEquals(0.0000, stat.getAverageViewWidthR(), delta);
		assertEquals(0.1444, stat.getAverageViewSwingley(), delta);
		assertEquals(0.0000, stat.getAverageViewSwingleyR(), delta);
	}

	// statistics()
	// 総ノート数・LN数・地雷オブジェ数が想定通りであること(シングルプレー)
	@Test
	public void testStatistics_NoteCountingSp() throws Exception {
		var stat = loadTestStatistics(0.5, BeMusicNoteLayout.SP_REGULAR);
		assertEquals(8, stat.getSpanCount());
		var sa = IntStream.range(0, 8).mapToObj(n -> stat.getSpan(n)).collect(Collectors.toList());
		var i = 0;
		// s1 -----
		var s = sa.get(i++);
		assertEquals(5, s.getNoteCount());
		assertEquals(2, s.getLongNoteCount());
		assertEquals(0, s.getMineCount());
		assertTrue(s.hasCountNote());
		assertTrue(s.hasVisualEffect());
		// s2 -----
		s = sa.get(i++);
		assertEquals(3, s.getNoteCount());
		assertEquals(2, s.getLongNoteCount());
		assertEquals(0, s.getMineCount());
		assertTrue(s.hasCountNote());
		assertTrue(s.hasVisualEffect());
		// s3 -----
		s = sa.get(i++);
		assertEquals(2, s.getNoteCount());
		assertEquals(1, s.getLongNoteCount());
		assertEquals(3, s.getMineCount());
		assertTrue(s.hasCountNote());
		assertTrue(s.hasVisualEffect());
		// s4 -----
		s = sa.get(i++);
		assertEquals(0, s.getNoteCount());
		assertEquals(0, s.getLongNoteCount());
		assertEquals(3, s.getMineCount());
		assertFalse(s.hasCountNote());
		assertTrue(s.hasVisualEffect());
		// s5 -----
		s = sa.get(i++);
		assertEquals(2, s.getNoteCount());
		assertEquals(0, s.getLongNoteCount());
		assertEquals(2, s.getMineCount());
		assertTrue(s.hasCountNote());
		assertTrue(s.hasVisualEffect());
		// s6 -----
		s = sa.get(i++);
		assertEquals(2, s.getNoteCount());
		assertEquals(0, s.getLongNoteCount());
		assertEquals(2, s.getMineCount());
		assertTrue(s.hasCountNote());
		assertTrue(s.hasVisualEffect());
		// s7 -----
		s = sa.get(i++);
		assertEquals(0, s.getNoteCount());
		assertEquals(0, s.getLongNoteCount());
		assertEquals(0, s.getMineCount());
		assertFalse(s.hasCountNote());
		assertFalse(s.hasVisualEffect());
		// s8 -----
		s = sa.get(i++);
		assertEquals(3, s.getNoteCount());
		assertEquals(0, s.getLongNoteCount());
		assertEquals(0, s.getMineCount());
		assertTrue(s.hasCountNote());
		assertTrue(s.hasVisualEffect());
		// stat -----
		var delta = 0.0001;
		assertEquals(5.6666, stat.getAverageDensity(), delta);
		assertEquals(10.0000, stat.getMaxDensity(), delta);
	}

	// statistics()
	// 総ノート数・LN数・地雷オブジェ数が想定通りであること(ダブルプレー)
	@Test
	public void testStatistics_NoteCountingDp() throws Exception {
		var stat = loadTestStatistics(0.5, BeMusicNoteLayout.SP_REGULAR);
		assertEquals(8, stat.getSpanCount());
		var sa = IntStream.range(0, 8).mapToObj(n -> stat.getSpan(n)).collect(Collectors.toList());
		var i = 0;
		// s1 -----
		var s = sa.get(i++);
		assertEquals(7, s.getNoteCount());
		assertEquals(2, s.getLongNoteCount());
		assertEquals(0, s.getMineCount());
		assertTrue(s.hasCountNote());
		assertTrue(s.hasVisualEffect());
		// s2 -----
		s = sa.get(i++);
		assertEquals(7, s.getNoteCount());
		assertEquals(3, s.getLongNoteCount());
		assertEquals(0, s.getMineCount());
		assertTrue(s.hasCountNote());
		assertTrue(s.hasVisualEffect());
		// s3 -----
		s = sa.get(i++);
		assertEquals(3, s.getNoteCount());
		assertEquals(1, s.getLongNoteCount());
		assertEquals(0, s.getMineCount());
		assertTrue(s.hasCountNote());
		assertTrue(s.hasVisualEffect());
		// s4 -----
		s = sa.get(i++);
		assertEquals(0, s.getNoteCount());
		assertEquals(0, s.getLongNoteCount());
		assertEquals(8, s.getMineCount());
		assertFalse(s.hasCountNote());
		assertTrue(s.hasVisualEffect());
		// s5 -----
		s = sa.get(i++);
		assertEquals(0, s.getNoteCount());
		assertEquals(0, s.getLongNoteCount());
		assertEquals(8, s.getMineCount());
		assertFalse(s.hasCountNote());
		assertTrue(s.hasVisualEffect());
		// s6 -----
		s = sa.get(i++);
		assertEquals(0, s.getNoteCount());
		assertEquals(0, s.getLongNoteCount());
		assertEquals(0, s.getMineCount());
		assertFalse(s.hasCountNote());
		assertFalse(s.hasVisualEffect());
		// s7 -----
		s = sa.get(i++);
		assertEquals(4, s.getNoteCount());
		assertEquals(0, s.getLongNoteCount());
		assertEquals(2, s.getMineCount());
		assertTrue(s.hasCountNote());
		assertTrue(s.hasVisualEffect());
		// s8 -----
		s = sa.get(i++);
		assertEquals(4, s.getNoteCount());
		assertEquals(0, s.getLongNoteCount());
		assertEquals(2, s.getMineCount());
		assertTrue(s.hasCountNote());
		assertTrue(s.hasVisualEffect());
		// stat -----
		var delta = 0.0001;
		assertEquals(10.0000, stat.getAverageDensity(), delta);
		assertEquals(14.0000, stat.getMaxDensity(), delta);
	}

	// statistics()
	// 無操作期間比率が想定通りであること
	@Test
	public void testStatistics_NoPlayingRatio() throws Exception {
		var stat = loadTestStatistics();
		assertEquals(8, stat.getSpanCount());
		var sa = IntStream.range(0, 8).mapToObj(n -> stat.getSpan(n)).collect(Collectors.toList());
		var i = 0;
		// s1 -----
		var s = sa.get(i++);
		assertTrue(s.hasCountNote());
		// s2 -----
		s = sa.get(i++);
		assertFalse(s.hasCountNote());
		// s3 -----
		s = sa.get(i++);
		assertFalse(s.hasCountNote());
		// s4 -----
		s = sa.get(i++);
		assertTrue(s.hasCountNote());
		// s5 -----
		s = sa.get(i++);
		assertTrue(s.hasCountNote());
		// s6 -----
		s = sa.get(i++);
		assertTrue(s.hasCountNote());
		// s7 -----
		s = sa.get(i++);
		assertFalse(s.hasCountNote());
		// s8 -----
		s = sa.get(i++);
		assertTrue(s.hasCountNote());
		// stat
		assertEquals(0.3750, stat.getNoPlayingRatio(), 0.0001);
	}

	// statistics()
	// 視覚効果なし期間比率が想定通りであること
	@Test
	public void testStatistics_NoVisualEffect() throws Exception {
		var stat = loadTestStatistics();
		assertEquals(8, stat.getSpanCount());
		var sa = IntStream.range(0, 8).mapToObj(n -> stat.getSpan(n)).collect(Collectors.toList());
		var i = 0;
		// s1 -----
		var s = sa.get(i++);
		assertTrue(s.hasVisualEffect());
		// s2 -----
		s = sa.get(i++);
		assertFalse(s.hasVisualEffect());
		// s3 -----
		s = sa.get(i++);
		assertTrue(s.hasVisualEffect());
		// s4 -----
		s = sa.get(i++);
		assertTrue(s.hasVisualEffect());
		// s5 -----
		s = sa.get(i++);
		assertTrue(s.hasVisualEffect());
		// s6 -----
		s = sa.get(i++);
		assertTrue(s.hasVisualEffect());
		// s7 -----
		s = sa.get(i++);
		assertFalse(s.hasVisualEffect());
		// s8 -----
		s = sa.get(i++);
		assertTrue(s.hasVisualEffect());
		// stat
		assertEquals(0.2500, stat.getNoVisualEffectRatio(), 0.0001);
	}

	// statistics()
	// Delta System未使用時は関連各種データがnull/無効値等の規定値になること
	@Test
	public void testStatistics_NoDeltaSystem() {
		var stat = new BeMusicStatisticsBuilder(sTestData.header, sTestData.chart).statistics();
		assertNull(stat.getRatingAlgorithmVersion());
		assertNull(stat.getPrimaryTendency());
		assertNull(stat.getSecondaryTendency());
		for (var ratingType : BeMusicRatingType.values()) {
			assertTrue(ratingType.isUnknown(stat.getRating(ratingType)));
		}
	}

	// statistics()
	// 算出するレーティング値が1つでもあるとアルゴリズムバージョンが取得でき、想定する値になること
	@Test
	public void testStatistics_RatingAlgorithmVersion1_Normal() {
		var stat = new BeMusicStatisticsBuilder(sTestData.header, sTestData.chart)
				.addRating(BeMusicRatingType.POWER)
				.statistics();
		var ver = String.format("%d.%d-%c",
				Ds.ALGORITHM_MAJOR_VERSION,
				Ds.ALGORITHM_REVISION_NUMBER,
				Ds.ALGORITHM_STATUS_CHAR);
		assertEquals(ver, stat.getRatingAlgorithmVersion());
	}

	// statistics()
	// Delta Systemの設定を変更するとアルゴリズムバージョンの末尾に'C'が付与されること
	@Test
	public void testStatistics_RatingAlgorithmVersion2_Customized() throws Exception {
		try {
			Tests.setsf(Ds.class, "sConfigChanged", true);
			var stat = new BeMusicStatisticsBuilder(sTestData.header, sTestData.chart)
					.addRating(BeMusicRatingType.POWER)
					.statistics();
			var ver = String.format("%d.%d-%c%c",
					Ds.ALGORITHM_MAJOR_VERSION,
					Ds.ALGORITHM_REVISION_NUMBER,
					Ds.ALGORITHM_STATUS_CHAR,
					Ds.ALGORITHM_CONFIG_CHANGED);
			assertEquals(ver, stat.getRatingAlgorithmVersion());
		} finally {
			Tests.setsf(Ds.class, "sConfigChanged", false);
		}
	}

	// statistics()
	// 算出するレーティング値が1つの時は主傾向と副次傾向が同一になること
	@Test
	public void testStatistics_SamePrimaryAndSecondaryTendency() {
		var type = BeMusicRatingType.RHYTHM;
		var stat = new BeMusicStatisticsBuilder(sTestData.header, sTestData.chart).addRating(type).statistics();
		assertEquals(type, stat.getPrimaryTendency());
		assertEquals(type, stat.getSecondaryTendency());
	}

	// statistics()
	// 算出するレーティング値が2つ以上の時は主傾向が最も高い種別、副次傾向が2番目に高い種別になること
	@Test
	public void testStatistics_PrimaryAndSecondaryTendency() {
		var types = BeMusicRatingType.values();
		var stat = new BeMusicStatisticsBuilder(sTestData.header, sTestData.chart).addRating(types).statistics();
		assertEquals(BeMusicRatingType.POWER, stat.getPrimaryTendency());
		assertEquals(BeMusicRatingType.GIMMICK, stat.getSecondaryTendency());
	}

	// statistics()
	// 空譜面での統計情報が想定通りの値になっていること
	@Test
	public void testStatistics_EmptyChart() throws Exception {
		var length = 0.75;
		var layout = new BeMusicNoteLayout("1234567");
		var stat = loadTestStatistics(length, layout);
		assertEquals(0.0, stat.getAverageDensity(), 0.0);
		assertEquals(0.0, stat.getAverageGazePoint(), 0.0);
		assertEquals(0.0, stat.getAverageGazePointR(), 0.0);
		assertEquals(0.0, stat.getAverageGazeSwingley(), 0.0);
		assertEquals(0.0, stat.getAverageGazeSwingleyR(), 0.0);
		assertEquals(0.0, stat.getAverageViewWidth(), 0.0);
		assertEquals(0.0, stat.getAverageViewWidthR(), 0.0);
		assertEquals(0.0, stat.getAverageViewSwingley(), 0.0);
		assertEquals(0.0, stat.getAverageViewSwingleyR(), 0.0);
		assertEquals(0.0, stat.getMaxDensity(), 0.0);
		assertEquals(1.0, stat.getNoPlayingRatio(), 0.0);
		assertEquals(1.0, stat.getNoVisualEffectRatio(), 0.0);
		assertEquals(layout, stat.getNoteLayout());
		assertEquals(0, stat.getSpanCount());
		assertEquals(length, stat.getSpanLength(), 0.0);
	}

	private BeMusicStatistics loadTestStatistics() throws Exception {
		return builder(1.0, BeMusicNoteLayout.SP_REGULAR);
	}

	private BeMusicStatistics loadTestStatistics(double spanLength, BeMusicNoteLayout layout) throws Exception {
		return builder(spanLength, layout);
	}

	private BeMusicStatistics builder(double spanLength, BeMusicNoteLayout layout) throws Exception {
		var dataPath = testDataPath(2);
		var handler = new BeMusicLoadHandler()
				.setEnableControlFlow(false)
				.setForceRandomValue(1L);
		var loader = new BmsStandardLoader()
				.setSpec(BeMusicSpec.createV1(null, null))
				.setFixSpecViolation(false)
				.setHandler(handler)
				.setSyntaxErrorEnable(true)
				.setIgnoreUnknownChannel(false)
				.setIgnoreUnknownMeta(false)
				.setIgnoreWrongData(false);
		var content = loader.load(dataPath);
		var header = new BeMusicHeader(content);
		var chart = new BeMusicChartBuilder(content)
				.setSeekBga(false)
				.setSeekBgm(false)
				.setSeekInvisible(false)
				.setSeekMine(true)
				.setSeekMeasureLine(true)
				.setSeekText(false)
				.setSeekVisible(true)
				.createChart();
		var builder = new BeMusicStatisticsBuilder(header, chart)
				.setSpanLength(spanLength)
				.setNoteLayout(layout);
		var stat = builder.statistics();
		return stat;
	}
}
