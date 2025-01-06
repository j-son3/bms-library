package com.lmt.lib.bms;

import static org.junit.Assert.*;

import org.junit.Test;

public class BmsAtTest {
	// equals(BmsAt, BmsAt)
	// 正常：同じ楽曲位置を指定するとtrueを返すこと
	@Test
	public void testEqualsBmsAtBmsAt_Equals() {
		var p1 = BmsPoint.of(5, 6.789);
		var p2 = BmsPoint.of(5, 6.789);
		assertTrue("Expected return true, but false.", BmsAt.equals(p1, p2));
	}

	// equals(BmsAt, BmsAt)
	// 正常：小節番号のみが異なる楽曲位置を指定するとfalseを返すこと
	@Test
	public void testEqualsBmsAtBmsAt_DifferentMeasure() {
		var p1 = BmsPoint.of(4, 6.789);
		var p2 = BmsPoint.of(5, 6.789);
		assertFalse("Expected return false, but true.", BmsAt.equals(p1, p2));
	}

	// equals(BmsAt, BmsAt)
	// 正常：刻み位置のみが異なる楽曲位置を指定するとfalseを返すこと
	@Test
	public void testEqualsBmsAtBmsAt_DifferentTick() {
		var p1 = BmsPoint.of(5, 6.790);
		var p2 = BmsPoint.of(5, 6.789);
		assertFalse("Expected return false, but true.", BmsAt.equals(p1, p2));
	}

	// equals(BmsAt, BmsAt)
	// 正常：小節番号・刻み位置の両方が異なる楽曲位置を指定するとfalseを返すこと
	@Test
	public void testEqualsBmsAtBmsAt_DifferentAll() {
		var p1 = BmsPoint.of(4, 6.790);
		var p2 = BmsPoint.of(5, 6.789);
		assertFalse("Expected return false, but true.", BmsAt.equals(p1, p2));
	}

	// equals(BmsAt, BmsAt)
	// NullPointerException at1がnull
	@Test
	public void testEqualsBmsAtBmsAt_NullAt1() {
		assertThrows(NullPointerException.class, () -> BmsAt.equals(null, BmsPoint.of(0, 0)));
	}

	// equals(BmsAt, BmsAt)
	// NullPointerException at2がnull
	@Test
	public void testEqualsBmsAtBmsAt_NullAt2() {
		assertThrows(NullPointerException.class, () -> BmsAt.equals(BmsPoint.of(0, 0), null));
	}

	// compare(BmsAt, BmsAt)
	// 正常：同じ楽曲位置を指定すると0を返すこと
	@Test
	public void testCompareBmsAtBmsAt_Equal() {
		var p1 = BmsPoint.of(2, 3.4567);
		var p2 = BmsPoint.of(2, 3.4567);
		assertEquals(0, BmsAt.compare(p1, p2));
	}

	// compare(BmsAt, BmsAt)
	// 正常：at1の刻み位置のみが小さいと負の値を返すこと
	@Test
	public void testCompareBmsAtBmsAt_At1SmallerTick() {
		var p1 = BmsPoint.of(2, 3.4566);
		var p2 = BmsPoint.of(2, 3.4567);
		assertTrue("Expected return negative value, but it was NOT.", BmsAt.compare(p1, p2) < 0);
	}

	// compare(BmsAt, BmsAt)
	// 正常：at1の刻み位置のみが大きいと正の値を返すこと
	@Test
	public void testCompareBmsAtBmsAt_At1LargerTick() {
		var p1 = BmsPoint.of(2, 3.4568);
		var p2 = BmsPoint.of(2, 3.4567);
		assertTrue("Expected return positive value, but it was NOT.", BmsAt.compare(p1, p2) > 0);
	}

	// compare(BmsAt, BmsAt)
	// 正常：at1の小節番号のみが小さいと負の値を返すこと
	@Test
	public void testCompareBmsAtBmsAt_At1SmallerMeasure() {
		var p1 = BmsPoint.of(1, 3.4567);
		var p2 = BmsPoint.of(2, 3.4567);
		assertTrue("Expected return negative value, but it was NOT.", BmsAt.compare(p1, p2) < 0);
	}

	// compare(BmsAt, BmsAt)
	// 正常：at1の小節番号のみが大きいと正の値を返すこと
	@Test
	public void testCompareBmsAtBmsAt_At1LargerMeasure() {
		var p1 = BmsPoint.of(3, 3.4567);
		var p2 = BmsPoint.of(2, 3.4567);
		assertTrue("Expected return positive value, but it was NOT.", BmsAt.compare(p1, p2) > 0);
	}

	// compare(BmsAt, BmsAt)
	// NullPointerException at1がnull
	@Test
	public void testCompareBmsAtBmsAt_NullAt1() {
		assertThrows(NullPointerException.class, () -> BmsAt.compare(null, BmsPoint.of(0, 0)));
	}

	// compare(BmsAt, BmsAt)
	// NullPointerException at2がnull
	@Test
	public void testCompareBmsAtBmsAt_NullAt2() {
		assertThrows(NullPointerException.class, () -> BmsAt.compare(BmsPoint.of(0, 0), null));
	}

	// compare2(int, double, int, double)
	// 正常：同じ楽曲位置を指定すると0を返すこと
	@Test
	public void testCompare2IntDoubleIntDouble_Equal() {
		assertEquals(0, BmsAt.compare2(5, 6.789, 5, 6.789));
	}

	// compare2(int, double, int, double)
	// 正常：楽曲位置1の刻み位置のみが小さいと負の値を返すこと
	@Test
	public void testCompare2IntDoubleIntDouble_At1SmallerTick() {
		assertTrue("Expected return negative value, but it was NOT.", BmsAt.compare2(5, 6.788, 5, 6.789) < 0);
	}

	// compare2(int, double, int, double)
	// 正常：楽曲位置1の刻み位置のみが大きいと正の値を返すこと
	@Test
	public void testCompare2IntDoubleIntDouble_At1LargerTick() {
		assertTrue("Expected return positive value, but it was NOT.", BmsAt.compare2(5, 6.790, 5, 6.789) > 0);
	}

	// compare2(int, double, int, double)
	// 正常：楽曲位置1の小節番号のみが小さいと負の値を返すこと
	@Test
	public void testCompare2IntDoubleIntDouble_At1SmallerMeasure() {
		assertTrue("Expected return negative value, but it was NOT.", BmsAt.compare2(4, 6.789, 5, 6.789) < 0);
	}

	// compare2(int, double, int, double)
	// 正常：楽曲位置1の小節番号のみが大きいと正の値を返すこと
	@Test
	public void testCompare2IntDoubleIntDouble_At1LargerMeasure() {
		assertTrue("Expected return positive value, but it was NOT.", BmsAt.compare2(6, 6.789, 5, 6.789) > 0);
	}

	// hashCode(BmsAt)
	// 正常：ハッシュコード値を返すこと
	@Test
	public void testHashCodeBmsAt_Normal() {
		BmsAt.hashCode(BmsPoint.of(0, 0));
	}

	// hashCode(BmsAt)
	// NullPointerException atがnull
	@Test
	public void testHashCodeBmsAt_NullAt() {
		assertThrows(NullPointerException.class, () ->BmsAt.hashCode(null));
	}

	// toString(BmsAt)
	// 正常：nullにならないこと
	@Test
	public void testToStringBmsAt_Normal() {
		assertNotNull(BmsAt.toString(BmsPoint.of(0, 0)));
	}

	// toString(BmsAt)
	// NullPointerException atがnull
	@Test
	public void testToStringBmsAt_NullAt() {
		assertThrows(NullPointerException.class, () -> BmsAt.toString(null));
	}
}
