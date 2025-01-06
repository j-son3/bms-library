package com.lmt.lib.bms;

import static org.junit.Assert.*;

import org.junit.Test;

public class BmsPointTest {
	// BmsPoint()
	// 正常：タイムライン上の有効な楽曲位置の最初を示すこと
	@Test
	public void testBmsPoint() {
		var p = new BmsPoint();
		assertEquals(BmsSpec.MEASURE_MIN, p.getMeasure());
		assertEquals(BmsSpec.TICK_MIN, p.getTick(), 0.0);
	}

	// BmsPoint(BmsAt)
	// 正常：指定した楽曲位置と同じ位置が設定されること
	@Test
	public void testBmsPointBmsAt_Normal() {
		var p = new BmsPoint(new BmsPoint(10, 12.3456));
		assertEquals(10, p.getMeasure());
		assertEquals(12.3456, p.getTick(), 0.0);
	}

	// BmsPoint(BmsAt)
	// NullPointerException atがnull
	@Test
	public void testBmsPointBmsAt_NullAt() {
		assertThrows(NullPointerException.class, () -> new BmsPoint(null));
	}

	// BmsPoint(int, double)
	// 正常：指定した楽曲位置と同じ位置が設定されること
	@Test
	public void testBmsPointIntDouble_Normal() {
		var p = new BmsPoint(15, 100.12345678);
		assertEquals(15, p.getMeasure());
		assertEquals(100.12345678, p.getTick(), 0.00000004);
	}

	// equals(Object)
	// 正常：同じ楽曲位置を指定するとtrueを返すこと
	@Test
	public void testEqualsObject_Equal() {
		var p1 = new BmsPoint(1, 2.3456);
		var p2 = new BmsPoint(1, 2.3456);
		assertTrue("Expected return true, but false.", p1.equals(p2));
	}

	// equals(Object)
	// 正常：異なる楽曲位置を指定するとfalseを返すこと
	@Test
	public void testEqualsObject_DifferentAt() {
		var p1 = new BmsPoint(1, 2.3456);
		var p2 = new BmsPoint(3, 4.5678);
		assertFalse("Expected return false, but true.", p1.equals(p2));
	}

	// equals(Object)
	// 準正常：BmsAtにキャストできないオブジェクトを指定するとfalseを返すこと
	@SuppressWarnings("unlikely-arg-type")
	@Test
	public void testEqualsObject_TypeMismatch() {
		var p = new BmsPoint(10, 20);
		assertFalse("Expected return false, but true.", p.equals(new String("abc")));
	}

	// equals(Object)
	// 準正常：nullを指定するとfalseを返すこと
	@Test
	public void testEqualsObject_NullAt() {
		var p = new BmsPoint(30, 40);
		assertFalse("Expected return false, but true.", p.equals(null));
	}

	// hashCode()
	// 正常：ハッシュコード値を返すこと
	@Test
	public void testHashCode() {
		new BmsPoint().hashCode();  // 例外がスローされないこと
	}

	// toString()
	// 正常：nullにならないこと
	@Test
	public void testToString() {
		assertNotNull(new BmsPoint().toString());
	}

	// compareTo(BmsAt)
	// 正常：同じ楽曲位置を指定すると0を返すこと
	@Test
	public void testCompareToBmsAt_Equal() {
		var p1 = new BmsPoint(10, 20.3456);
		var p2 = new BmsPoint(10, 20.3456);
		assertEquals(0, p1.compareTo(p2));
	}

	// compareTo(BmsAt)
	// 正常：同じ小節番号でthisの刻み位置が小さいと負の値を返すこと
	@Test
	public void testCompareToBmsAt_ThisSmallerTick() {
		var p1 = new BmsPoint(10, 20.3455);
		var p2 = new BmsPoint(10, 20.3456);
		assertTrue("Expected return negative value, but it was NOT.", p1.compareTo(p2) < 0);
	}

	// compareTo(BmsAt)
	// 正常：同じ小節番号でthisの刻み位置が大きいと正の値を返すこと
	@Test
	public void testCompareToBmsAt_ThisLargerTick() {
		var p1 = new BmsPoint(10, 20.3456);
		var p2 = new BmsPoint(10, 20.3455);
		assertTrue("Expected return positive value, but it was NOT.", p1.compareTo(p2) > 0);
	}

	// compareTo(BmsAt)
	// 正常：同じ刻み位置でthisの小節番号が小さいと負の値を返すこと
	@Test
	public void testCompareToBmsAt_ThisSmallerMeasure() {
		var p1 = new BmsPoint(10, 20.3456);
		var p2 = new BmsPoint(11, 20.3456);
		assertTrue("Expected return negative value, but it was NOT.", p1.compareTo(p2) < 0);
	}

	// compareTo(BmsAt)
	// 正常：同じ刻み位置でthisの小節番号が大きいと正の値を返すこと
	@Test
	public void testCompareToBmsAt_ThisLargerMeasure() {
		var p1 = new BmsPoint(11, 20.3456);
		var p2 = new BmsPoint(10, 20.3456);
		assertTrue("Expected return positive value, but it was NOT.", p1.compareTo(p2) > 0);
	}

	// compareTo(BmsAt)
	// NullPointerException atがnull
	@Test
	public void testCompareToBmsAt_NullAt() {
		assertThrows(NullPointerException.class, () -> new BmsPoint().compareTo(null));
	}

	// getMeasure()
	// テスト省略

	// getTick()
	// テスト省略

	// of(BmsAt)
	// 正常：指定した楽曲位置と同じ値の楽曲位置が生成されること
	@Test
	public void testOfBmsAt_Normal() {
		var org = new BmsPoint(9, 10.203040);
		var p = BmsPoint.of(org);
		assertNotNull(p);
		assertEquals(9, p.getMeasure());
		assertEquals(10.203040, p.getTick(), 0.0);
	}

	// of(BmsAt)
	// NullPointerException atがnull
	@Test
	public void testOfBmsAt_NullAt() {
		assertThrows(NullPointerException.class, () -> BmsPoint.of(null));
	}

	// of(int, double)
	// 正常：指定した楽曲位置と同じ値の楽曲位置が生成されること
	@Test
	public void testOfIntDouble() {
		var p = BmsPoint.of(100, 200.345);
		assertNotNull(p);
		assertEquals(100, p.getMeasure());
		assertEquals(200.345, p.getTick(), 0.0);
	}
}
