package com.lmt.lib.bms.bemusic;

import static org.junit.Assert.*;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

public class BeMusicStatisticsTest {
	// getSpanCount()
	// 正常：期間統計情報リストの件数が返ること
	@Test
	public void testGetSpanCount() {
		var stat = newInstance();
		var list = List.of(new BeMusicTimeSpan(), new BeMusicTimeSpan(), new BeMusicTimeSpan());
		stat.setTimeSpanList(list);
		assertEquals(3, stat.getSpanCount());
	}

	// getSpan(int)
	// 正常：インデックスに該当する期間統計情報リストの要素が返ること
	@Test
	public void testGetSpan() {
		var stat = newInstance();
		var span1 = new BeMusicTimeSpan();
		var span2 = new BeMusicTimeSpan();
		var span3 = new BeMusicTimeSpan();
		var span4 = new BeMusicTimeSpan();
		var list = List.of(span1, span2, span3, span4);
		stat.setTimeSpanList(list);
		assertSame(span1, stat.getSpan(0));
		assertSame(span2, stat.getSpan(1));
		assertSame(span3, stat.getSpan(2));
		assertSame(span4, stat.getSpan(3));
	}

	// spans()
	// 正しい順番で期間統計情報を走査するストリームが返ること
	@Test
	public void testSpans() {
		var stat = newInstance();
		var span1 = new BeMusicTimeSpan();
		var span2 = new BeMusicTimeSpan();
		var span3 = new BeMusicTimeSpan();
		var inList = List.of(span1, span2, span3);
		stat.setTimeSpanList(inList);
		assertNotNull(stat.spans());
		assertEquals(3L, stat.spans().count());
		var outList = stat.spans().collect(Collectors.toList());
		assertSame(span1, outList.get(0));
		assertSame(span2, outList.get(1));
		assertSame(span3, outList.get(2));
	}

	// getSpanLength()
	// 正常：設定した期間統計情報の長さが返ること
	@Test
	public void testGetSpanLength() {
		var stat = newInstance();
		var length = 1.34;
		stat.setSpanLength(length);
		assertEquals(length, stat.getSpanLength(), 0.0);
	}

	// getNoteLayout()
	// 正常：設定したノートレイアウトが返ること
	@Test
	public void testGetNoteLayout() {
		var stat = newInstance();
		var layout = new BeMusicNoteLayout("1234567");
		stat.setNoteLayout(layout);
		assertSame(layout, stat.getNoteLayout());
	}

	// getAverageDensity()
	// 正常：設定した平均ノート密度が返ること
	@Test
	public void testGetAverageDensity() {
		var stat = newInstance();
		var density = 12.345;
		stat.setAverageDensity(density);
		assertEquals(density, stat.getAverageDensity(), 0.0);
	}

	// getMaxDensity()
	// 正常：設定した最大ノート密度が返ること
	@Test
	public void testGetMaxDensity() {
		var stat = newInstance();
		var density = 34.567;
		stat.setMaxDensity(density);
		assertEquals(density, stat.getMaxDensity(), 0.0);
	}

	// getNoPlayingRatio()
	// 正常：設定した無操作期間比率が返ること
	@Test
	public void testGetNoPlayingRatio() {
		var stat = newInstance();
		var ratio = 0.2;
		stat.setNoPlayingRatio(ratio);
		assertEquals(ratio, stat.getNoPlayingRatio(), 0.0);
	}

	// getNoVisualEffectRatio()
	// 正常：設定した視覚効果のない期間比率が返ること
	@Test
	public void testGetNoVisualEffectRatio() {
		var stat = newInstance();
		var ratio = 0.3;
		stat.setNoVisualEffectRatio(ratio);
		assertEquals(ratio, stat.getNoVisualEffectRatio(), 0.0);
	}

	// getAverageGazePoint()
	// 正常：設定した平均注視点が返ること
	@Test
	public void testGetAverageGazePoint() {
		var stat = newInstance();
		stat.setAverageGazePoint(0.1, 0.4);
		assertEquals(0.1, stat.getAverageGazePoint(), 0.0);
	}

	// getAverageGazePointR()
	// 正常：設定した平均注視点が返ること
	@Test
	public void testGetAverageGazePointR() {
		var stat = newInstance();
		stat.setAverageGazePoint(0.1, 0.4);
		assertEquals(0.4, stat.getAverageGazePointR(), 0.0);
	}

	// getAverageViewWidth()
	// 正常：設定した平均視野幅が返ること
	@Test
	public void testGetAverageViewWidth() {
		var stat = newInstance();
		stat.setAverageViewWidth(0.2, 0.5);
		assertEquals(0.2, stat.getAverageViewWidth(), 0.0);
	}

	// getAverageViewWidthR()
	// 正常：設定した平均視野幅が返ること
	@Test
	public void testGetAverageViewWidthR() {
		var stat = newInstance();
		stat.setAverageViewWidth(0.2, 0.5);
		assertEquals(0.5, stat.getAverageViewWidthR(), 0.0);
	}

	// getGazeSwingley()
	// 正常：設定した注視点変動係数が返ること
	@Test
	public void testGetGazeSwingley() {
		var stat = newInstance();
		stat.setAverageGazeSwingley(0.3, 0.6);
		assertEquals(0.3, stat.getAverageGazeSwingley(), 0.0);
	}

	// getGazeSwingleyR()
	// 正常：設定した注視点変動係数が返ること
	@Test
	public void testGetGazeSwingleyR() {
		var stat = newInstance();
		stat.setAverageGazeSwingley(0.3, 0.6);
		assertEquals(0.6, stat.getAverageGazeSwingleyR(), 0.0);
	}

	// getViewSwingley()
	// 正常：設定した視野幅変動係数が返ること
	@Test
	public void testGetViewSwingley() {
		var stat = newInstance();
		stat.setAverageViewSwingley(0.4, 0.7);
		assertEquals(0.4, stat.getAverageViewSwingley(), 0.0);
	}

	// getViewSwingleyR()
	// 正常：設定した視野幅変動係数が返ること
	@Test
	public void testGetViewSwingleyR() {
		var stat = newInstance();
		stat.setAverageViewSwingley(0.4, 0.7);
		assertEquals(0.7, stat.getAverageViewSwingleyR(), 0.0);
	}

	// getRatingAlgorithmVersion()
	// 正常：設定したレーティングアルゴリズムバージョンが返ること
	@Test
	public void testGetRatingAlgorithmVersion() {
		var stat = newInstance();
		var version = "1.10-RC";
		stat.setRatingAlgorithmVersion(version);
		assertEquals(version, stat.getRatingAlgorithmVersion());
	}

	// getRating(BeMusicRatingType)
	// 正常：設定したレーティング値が返ること
	@Test
	public void testGetRating() {
		var stat = newInstance();
		stat.setRating(BeMusicRatingType.HOLDING, 10000);
		assertEquals(10000, stat.getRating(BeMusicRatingType.HOLDING));
		assertTrue(stat.getRating(BeMusicRatingType.DELTA) < 0);
		assertTrue(stat.getRating(BeMusicRatingType.COMPLEX) < 0);
		assertTrue(stat.getRating(BeMusicRatingType.POWER) < 0);
		assertTrue(stat.getRating(BeMusicRatingType.RHYTHM) < 0);
		assertTrue(stat.getRating(BeMusicRatingType.SCRATCH) < 0);
		assertTrue(stat.getRating(BeMusicRatingType.GIMMICK) < 0);
	}

	// getPrimaryTendency()
	// 正常：設定した譜面主傾向が返ること
	@Test
	public void testGetPrimaryTendency() {
		var stat = newInstance();
		stat.setPrimaryTendency(BeMusicRatingType.POWER);
		assertEquals(BeMusicRatingType.POWER, stat.getPrimaryTendency());
	}

	// getSecondaryTendency()
	// 正常：設定した譜面副次傾向が返ること
	@Test
	public void testGetSecondaryTendency() {
		var stat = newInstance();
		stat.setSecondaryTendency(BeMusicRatingType.GIMMICK);
		assertEquals(BeMusicRatingType.GIMMICK, stat.getSecondaryTendency());
	}

	// getDeltaSystemVersion()
	// 想定する形式のバージョン文字列が返ること
	@Test
	public void testGetDeltaSystemVersion() {
		var ver = BeMusicStatistics.getDeltaSystemVersion();
		assertNotNull(ver);
		assertTrue(ver.matches("^[0-9]+\\.[0-9]+-[DRF]$"));
	}

	private static BeMusicStatistics newInstance() {
		return new BeMusicStatistics();
	}
}
