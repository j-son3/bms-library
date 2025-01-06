package com.lmt.lib.bms.bemusic;

import static org.junit.Assert.*;

import org.junit.Test;

public class BeMusicTimeSpanTest {
	// getPrevious()
	// 正常：設定した期間統計情報が返ること
	@Test
	public void testGetPrevious() {
		var ts = new BeMusicTimeSpan();
		var prev = new BeMusicTimeSpan();
		ts.setPrevious(prev);
		assertSame(prev, ts.getPrevious());
	}

	// getNext()
	// 正常：設定した期間統計情報が返ること
	@Test
	public void testGetNext() {
		var ts = new BeMusicTimeSpan();
		var next = new BeMusicTimeSpan();
		ts.setNext(next);
		assertSame(next, ts.getNext());
	}

	// getIndex()
	// 正常：設定したインデックス値が返ること
	@Test
	public void testGetIndex() {
		var ts = new BeMusicTimeSpan();
		ts.setIndex(10);
		assertEquals(10, ts.getIndex());
	}

	// getBeginTime()
	// 正常：設定した開始時間が返ること
	@Test
	public void testGetBeginTime() {
		var ts = new BeMusicTimeSpan();
		var time = 100.567;
		ts.setBeginTime(time);
		assertEquals(time, ts.getBeginTime(), 0.0);
	}

	// getEndTime()
	// 正常：設定した終了時間が返ること
	@Test
	public void testGetEndTime() {
		var ts = new BeMusicTimeSpan();
		var time = 200.234;
		ts.setEndTime(time);
		assertEquals(time, ts.getEndTime(), 0.0);
	}

	// getFirstPointIndex()
	// 正常：設定した楽曲位置インデックスが返ること
	@Test
	public void testGetFirstPointIndex() {
		var ts = new BeMusicTimeSpan();
		ts.setFirstPointIndex(20);
		assertEquals(20, ts.getFirstPointIndex());
	}

	// getLastPointIndex()
	// 正常：設定した楽曲位置インデックスが返ること
	@Test
	public void testGetLastPointIndex() {
		var ts = new BeMusicTimeSpan();
		ts.setLastPointIndex(30);
		assertEquals(30, ts.getLastPointIndex());
	}

	// getGazePoint()
	// 正常：設定した注視点が返ること
	@Test
	public void testGetGazePoint() {
		var ts = new BeMusicTimeSpan();
		ts.setGazePoint(0.5, 0.7);
		assertEquals(0.5, ts.getGazePoint(), 0.0);
	}

	// getGazePointR()
	// 正常：設定した注視点が返ること
	@Test
	public void testGetGazePointR() {
		var ts = new BeMusicTimeSpan();
		ts.setGazePoint(0.5, 0.7);
		assertEquals(0.7, ts.getGazePointR(), 0.0);
	}

	// getGazeSwingley()
	// 正常：設定した注視点変動係数が返ること
	@Test
	public void testGetGazeSwingley() {
		var ts = new BeMusicTimeSpan();
		ts.setGazeSwingley(0.3, 0.6);
		assertEquals(0.3, ts.getGazeSwingley(), 0.0);
	}

	// getGazeSwingleyR()
	// 正常：設定した注視点変動係数が返ること
	@Test
	public void testGetGazeSwingleyR() {
		var ts = new BeMusicTimeSpan();
		ts.setGazeSwingley(0.3, 0.6);
		assertEquals(0.6, ts.getGazeSwingleyR(), 0.0);
	}

	// getViewWidth()
	// 正常：設定した視野幅が返ること
	@Test
	public void testGetViewWidth() {
		var ts = new BeMusicTimeSpan();
		ts.setViewWidth(-0.2, 0.8);
		assertEquals(-0.2, ts.getViewWidth(), 0.0);
	}

	// getViewWidthR()
	// 正常：設定した視野幅が返ること
	@Test
	public void testGetViewWidthR() {
		var ts = new BeMusicTimeSpan();
		ts.setViewWidth(-0.2, 0.8);
		assertEquals(0.8, ts.getViewWidthR(), 0.0);
	}

	// getViewSwingley()
	// 正常：設定した視野幅変動係数が返ること
	@Test
	public void testGetViewSwingley() {
		var ts = new BeMusicTimeSpan();
		ts.setViewSwingley(0.7, 0.9);
		assertEquals(0.7, ts.getViewSwingley(), 0.0);
	}

	// getViewSwingleyR()
	// 正常：設定した視野幅変動係数が返ること
	@Test
	public void testGetViewSwingleyR() {
		var ts = new BeMusicTimeSpan();
		ts.setViewSwingley(0.7, 0.9);
		assertEquals(0.9, ts.getViewSwingleyR(), 0.0);
	}

	// getNoteCount()
	// 正常：設定した総ノート数が返ること
	@Test
	public void testGetNoteCount() {
		var ts = new BeMusicTimeSpan();
		ts.setNoteCount(50);
		assertEquals(50, ts.getNoteCount());
	}

	// getLongNoteCount()
	// 正常：設定したロングノート数が返ること
	@Test
	public void testGetLongNoteCount() {
		var ts = new BeMusicTimeSpan();
		ts.setLongNoteCount(60);
		assertEquals(60, ts.getLongNoteCount());
	}

	// getMineCount()
	// 正常：設定した地雷オブジェ数が返ること
	@Test
	public void testGetMineCount() {
		var ts = new BeMusicTimeSpan();
		ts.setMineCount(70);
		assertEquals(70, ts.getMineCount());
	}

	// isFIrstSpan()
	// 正常：最初の期間統計情報の場合trueが返ること
	@Test
	public void testIsFirstSpan_Yes() {
		var ts = new BeMusicTimeSpan();
		ts.setPrevious(ts);
		assertTrue(ts.isFirstSpan());
	}

	// isFIrstSpan()
	// 正常：最初の期間統計情報ではない場合falseが返ること
	@Test
	public void testIsFirstSpan_No() {
		var ts = new BeMusicTimeSpan();
		ts.setPrevious(new BeMusicTimeSpan());
		assertFalse(ts.isFirstSpan());
	}

	// isLastSpan()
	// 正常：最後の期間統計情報の場合trueが返ること
	@Test
	public void testIsLastSpan_Yes() {
		var ts = new BeMusicTimeSpan();
		ts.setNext(ts);
		assertTrue(ts.isLastSpan());
	}

	// isLastSpan()
	// 正常：最後の期間統計情報ではない場合falseが返ること
	@Test
	public void testIsLastSpan_No() {
		var ts = new BeMusicTimeSpan();
		ts.setNext(new BeMusicTimeSpan());
		assertFalse(ts.isLastSpan());
	}

	// hasPoint()
	// 正常：楽曲位置情報が存在する場合trueが返ること
	@Test
	public void testHasPoint_Yes() {
		var ts = new BeMusicTimeSpan();
		ts.setFirstPointIndex(0);
		ts.setLastPointIndex(1);
		assertTrue(ts.hasPoint());
	}

	// hasPoint()
	// 正常：楽曲位置情報が存在しない場合falseが返ること
	@Test
	public void testHasPoint_No() {
		var ts = new BeMusicTimeSpan();
		ts.setFirstPointIndex(-1);
		ts.setLastPointIndex(-1);
		assertFalse(ts.hasPoint());
	}

	// hasCountNote()
	// 正常：ノート数としてカウントされるノートがある場合trueが返ること
	@Test
	public void testHasCountNote_Yes() {
		var ts = new BeMusicTimeSpan();
		ts.setNoteCount(1);
		assertTrue(ts.hasCountNote());
	}

	// hasCountNote()
	// 正常：ノート数としてカウントされるノートがない場合falseが返ること
	@Test
	public void testHasCountNote_No() {
		var ts = new BeMusicTimeSpan();
		ts.setNoteCount(0);
		assertFalse(ts.hasCountNote());
	}

	// hasVisualEffect()
	// 正常：視覚効果を持つノートがある場合trueが返ること
	@Test
	public void testHasVisualEffect_Yes() {
		var ts = new BeMusicTimeSpan();
		ts.setViewWidth(0.1, 0.0);
		assertTrue(ts.hasVisualEffect());
		ts.setViewWidth(0.0, 0.1);
		assertTrue(ts.hasVisualEffect());
	}

	// hasVisualEffect()
	// 正常：視覚効果を持つノートがない場合falseが返ること
	@Test
	public void testHasVisualEffect_No() {
		var ts = new BeMusicTimeSpan();
		ts.setViewWidth(0.0, 0.0);
		assertFalse(ts.hasVisualEffect());
	}
}
