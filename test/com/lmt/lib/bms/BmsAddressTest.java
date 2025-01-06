package com.lmt.lib.bms;

import static org.junit.Assert.*;

import org.junit.Test;

public class BmsAddressTest {
	// BmsAddress()
	// 正常：各値が最小値のインスタンスが生成されること
	@Test
	public void testBmsAddress_Normal() {
		var a = new BmsAddress();
		assertEquals(BmsSpec.MEASURE_MIN, a.getMeasure());
		assertEquals(BmsSpec.TICK_MIN, a.getTick(), 0.0);
		assertEquals(BmsSpec.CHANNEL_MIN, a.getChannel());
		assertEquals(BmsSpec.CHINDEX_MIN, a.getIndex());
	}

	// BmsAddress(BmsAddress)
	// 正常：指定されたアドレスと同じ値のインスタンスが生成されること
	@Test
	public void testBmsAddressBmsAddress_Normal() {
		var org = BmsAddress.of(1, 2.0, 3, 4);
		var a = new BmsAddress(org);
		assertEquals(1, a.getMeasure());
		assertEquals(2.0, a.getTick(), 0.0);
		assertEquals(3, a.getChannel());
		assertEquals(4, a.getIndex());
	}

	// BmsAddress(BmsAddress)
	// NullPointerException adrがnull
	@Test
	public void testBmsAddressBmsAddress_NullAdr() {
		assertThrows(NullPointerException.class, () -> new BmsAddress(null));
	}

	// BmsAddress(BmsAt, BmsChx)
	// 正常：指定された楽曲位置・CHXと同じ値のインスタンスが生成されること
	@Test
	public void testBmsAddressBmsAtBmsChx_Normal() {
		var orgAt = BmsPoint.of(1, 2.0);
		var orgChx = BmsAddress.of(0, 0, 3, 4);
		var a = new BmsAddress(orgAt, orgChx);
		assertEquals(1, a.getMeasure());
		assertEquals(2.0, a.getTick(), 0.0);
		assertEquals(3, a.getChannel());
		assertEquals(4, a.getIndex());
	}

	// BmsAddress(BmsAt, BmsChx)
	// NullPointerException atがnull
	@Test
	public void testBmsAddressBmsAtBmsChx_NullAt() {
		assertThrows(NullPointerException.class, () -> new BmsAddress(null, BmsAddress.of(0, 0, 0, 0)));
	}

	// BmsAddress(BmsAt, BmsChx)
	// NullPointerException chxがnull
	@Test
	public void testBmsAddressBmsAtBmsChx_NullChx() {
		assertThrows(NullPointerException.class, () -> new BmsAddress(BmsPoint.of(0, 0), null));
	}

	// BmsAddress(int, double, int, int)
	// 正常：指定された楽曲位置・CHXと同じ値のインスタンスが生成されること
	@Test
	public void testBmsAddressIntDoubleIntInt_Normal() {
		var a = new BmsAddress(1, 2.0, 3, 4);
		assertEquals(1, a.getMeasure());
		assertEquals(2.0, a.getTick(), 0.0);
		assertEquals(3, a.getChannel());
		assertEquals(4, a.getIndex());
	}

	// getMeasure()
	// テスト省略

	// getTick()
	// テスト省略

	// getChannel()
	// 正常：0x8000以上のチャンネル番号を正常に処理できること
	@Test
	public void testGetChannel_Larger0x8000() {
		var a1 = BmsAddress.of(0, 0, 32768, 0);
		assertEquals(32768, a1.getChannel());
		var a2 = BmsAddress.of(0, 0, 65535, 0);
		assertEquals(65535, a2.getChannel());
	}

	// getIndex()
	// 正常：0x8000以上のチャンネルインデックスを正常に処理できること
	@Test
	public void testGetIndex_Larger0x8000() {
		var a1 = BmsAddress.of(0, 0, 0, 32768);
		assertEquals(32768, a1.getIndex());
		var a2 = BmsAddress.of(0, 0, 0, 65535);
		assertEquals(65535, a2.getIndex());
	}

	// equals(Object)
	// 正常：同じアドレスを指定するとtrueを返すこと
	@Test
	public void testEqualsObject_Equal() {
		var a1 = BmsAddress.of(3, 4.567, 8, 9);
		var a2 = BmsAddress.of(3, 4.567, 8, 9);
		assertTrue(a1.equals(a2));
	}

	// equals(Object)
	// 正常：楽曲位置の小節番号のみが異なるアドレスを指定するとfalseを返すこと
	@Test
	public void testEqualsObject_DifferentMeasure() {
		var a1 = BmsAddress.of(2, 4.567, 8, 9);
		var a2 = BmsAddress.of(3, 4.567, 8, 9);
		assertFalse(a1.equals(a2));
	}

	// equals(Object)
	// 正常：楽曲位置の小節の刻み位置のみがこアドレスを指定するとfalseを返すこと
	@Test
	public void testEqualsObject_DifferentTick() {
		var a1 = BmsAddress.of(3, 4.566, 8, 9);
		var a2 = BmsAddress.of(3, 4.567, 8, 9);
		assertFalse(a1.equals(a2));
	}

	// equals(Object)
	// 正常：CHXのチャンネル番号のみが異なるアドレスを指定するとfalseを返すこと
	@Test
	public void testEqualsObject_DifferentChannel() {
		var a1 = BmsAddress.of(3, 4.567, 7, 9);
		var a2 = BmsAddress.of(3, 4.567, 8, 9);
		assertFalse(a1.equals(a2));
	}

	// equals(Object)
	// 正常：CHXのチャンネルインデックスのみが異なるアドレスを指定するとfalseを返すこと
	@Test
	public void testEqualsObject_DifferentIndex() {
		var a1 = BmsAddress.of(3, 4.567, 8, 8);
		var a2 = BmsAddress.of(3, 4.567, 8, 9);
		assertFalse(a1.equals(a2));
	}

	// equals(Object)
	// 準正常：BmsAddressにキャストできないオブジェクトを指定するとfalseを返すこと
	@SuppressWarnings("unlikely-arg-type")
	@Test
	public void testEqualsObject_OtherObj() {
		var a1 = BmsAddress.of(3, 4.567, 8, 9);
		var a2 = "String";
		assertFalse(a1.equals(a2));
	}

	// equals(Object)
	// 準正常：nullを指定するとfalseを返すこと
	@Test
	public void testEqualsObject_NullObj() {
		var a1 = BmsAddress.of(3, 4.567, 8, 9);
		assertFalse(a1.equals(null));
	}

	// hashCode()
	// 正常：ハッシュコード値を返すこと
	@Test
	public void testHashCode_Normal() {
		new BmsAddress().hashCode();
	}

	// toString()
	// 楽曲位置・CHXのいずれかの要素が異なると違う文字列になること
	@Test
	public void testToString_Normal() {
		var a = BmsAddress.of(1, 2.345, 6, 7);
		var s = a.toString();
		assertNotEquals(s, BmsAddress.of(2, 2.345, 6, 7));
		assertNotEquals(s, BmsAddress.of(1, 2.346, 6, 7));
		assertNotEquals(s, BmsAddress.of(1, 2.345, 7, 7));
		assertNotEquals(s, BmsAddress.of(1, 2.345, 6, 8));
	}

	// compareTo(BmsAddress)
	// 正常：同じアドレスを指定すると0を返すこと
	@Test
	public void testCompareToBmsAddress_Equal() {
		var a1 = BmsAddress.of(3, 4.567, 8, 9);
		var a2 = BmsAddress.of(3, 4.567, 8, 9);
		assertEquals(0, a1.compareTo(a2));
	}

	// compareTo(BmsAddress)
	// 正常：このアドレスの楽曲位置の小節番号のみが小さいと負の値を返すこと
	@Test
	public void testCompareToBmsAddress_ThisSmallerMeasure() {
		var a1 = BmsAddress.of(2, 4.567, 8, 9);
		var a2 = BmsAddress.of(3, 4.567, 8, 9);
		assertTrue("Expected return negative value, but it was NOT.", a1.compareTo(a2) < 0);
	}

	// compareTo(BmsAddress)
	// 正常：このアドレスの楽曲位置の小節番号のみが大きいと正の値を返すこと
	@Test
	public void testCompareToBmsAddress_ThisLargerMeasure() {
		var a1 = BmsAddress.of(4, 4.567, 8, 9);
		var a2 = BmsAddress.of(3, 4.567, 8, 9);
		assertTrue("Expected return positive value, but it was NOT.", a1.compareTo(a2) > 0);
	}

	// compareTo(BmsAddress)
	// 正常：このアドレスの楽曲位置の小節の刻み位置のみが小さいと負の値を返すこと
	@Test
	public void testCompareToBmsAddress_ThisSmallerTick() {
		var a1 = BmsAddress.of(3, 4.566, 8, 9);
		var a2 = BmsAddress.of(3, 4.567, 8, 9);
		assertTrue("Expected return negative value, but it was NOT.", a1.compareTo(a2) < 0);
	}

	// compareTo(BmsAddress)
	// 正常：このアドレスの楽曲位置の小節の刻み位置のみが大きいと正の値を返すこと
	@Test
	public void testCompareToBmsAddress_ThisLargerTick() {
		var a1 = BmsAddress.of(4, 4.568, 8, 9);
		var a2 = BmsAddress.of(3, 4.567, 8, 9);
		assertTrue("Expected return positive value, but it was NOT.", a1.compareTo(a2) > 0);
	}

	// compareTo(BmsAddress)
	// 正常：このアドレスのCHXのチャンネル番号のみが小さいと負の値を返すこと
	@Test
	public void testCompareToBmsAddress_ThisSmallerChannel() {
		var a1 = BmsAddress.of(3, 4.567, 7, 9);
		var a2 = BmsAddress.of(3, 4.567, 8, 9);
		assertTrue("Expected return negative value, but it was NOT.", a1.compareTo(a2) < 0);
	}

	// compareTo(BmsAddress)
	// 正常：このアドレスのCHXのチャンネル番号のみが大きいと正の値を返すこと
	@Test
	public void testCompareToBmsAddress_ThisLargerChannel() {
		var a1 = BmsAddress.of(4, 4.568, 9, 9);
		var a2 = BmsAddress.of(3, 4.567, 8, 9);
		assertTrue("Expected return positive value, but it was NOT.", a1.compareTo(a2) > 0);
	}

	// compareTo(BmsAddress)
	// 正常：このアドレスのCHXのチャンネルインデックスのみが小さいと負の値を返すこと
	@Test
	public void testCompareToBmsAddress_ThisSmallerIndex() {
		var a1 = BmsAddress.of(3, 4.567, 8, 8);
		var a2 = BmsAddress.of(3, 4.567, 8, 9);
		assertTrue("Expected return negative value, but it was NOT.", a1.compareTo(a2) < 0);
	}

	// compareTo(BmsAddress)
	// 正常：このアドレスのCHXのチャンネルインデックスのみが大きいと正の値を返すこと
	@Test
	public void testCompareToBmsAddress_ThisLargerIndex() {
		var a1 = BmsAddress.of(4, 4.568, 9, 10);
		var a2 = BmsAddress.of(3, 4.567, 8, 9);
		assertTrue("Expected return positive value, but it was NOT.", a1.compareTo(a2) > 0);
	}

	// compareTo(BmsAddress)
	// 正常：このアドレスの楽曲位置が大きく、指定アドレスのCHXが大きいと正の値を返すこと
	@Test
	public void testCompareToBmsAddress_ThisLargerAtAdrLargerChx() {
		var a1 = BmsAddress.of(4, 4.567, 7, 8);
		var a2 = BmsAddress.of(3, 4.567, 8, 9);
		assertTrue("Expected return positive value, but it was NOT.", a1.compareTo(a2) > 0);
	}

	// compareTo(BmsAddress)
	// 正常：このアドレスのCHXが大きく、指定アドレスの楽曲位置が大きいと負の値を返すこと
	@Test
	public void testCompareToBmsAddress_ThisLargerChxAdrLargerAt() {
		var a1 = BmsAddress.of(2, 4.567, 9, 9);
		var a2 = BmsAddress.of(3, 4.567, 8, 9);
		assertTrue("Expected return negative value, but it was NOT.", a1.compareTo(a2) < 0);
	}

	// compareTo(BmsAddress)
	// 正常：0x8000以上のチャンネル番号比較を正常に行えること
	@Test
	public void testCompareToBmsAddress_ChannelLarger0x8000() {
		var a1 = BmsAddress.of(0, 0, 10000, 2);
		var a2 = BmsAddress.of(0, 0, 32768, 1);
		assertTrue("Expected return negative value, but it was NOT.", a1.compareTo(a2) < 0);
		var a3 = BmsAddress.of(0, 0, 65535, 1);
		var a4 = BmsAddress.of(0, 0, 10000, 2);
		assertTrue("Expected return positive value, but it was NOT.", a3.compareTo(a4) > 0);
	}

	// compareTo(BmsAddress)
	// 正常：0x8000以上のチャンネルインデックス比較を正常に行えること
	@Test
	public void testCompareToBmsAddress_IndexLarger0x8000() {
		var a1 = BmsAddress.of(0, 0, 0, 10000);
		var a2 = BmsAddress.of(0, 0, 0, 32768);
		assertTrue("Expected return negative value, but it was NOT.", a1.compareTo(a2) < 0);
		var a3 = BmsAddress.of(0, 0, 0, 65535);
		var a4 = BmsAddress.of(0, 0, 0, 10000);
		assertTrue("Expected return positive value, but it was NOT.", a3.compareTo(a4) > 0);
	}

	// compareTo(BmsAddress)
	// NullPointerException adrがnull
	@Test
	public void testCompareToBmsAddress_NullAdr() {
		var a1 = BmsAddress.of(3, 4.567, 8, 8);
		assertThrows(NullPointerException.class, () -> a1.compareTo(null));
	}

	// equals(BmsAddress, BmsAddress)
	// 正常：同じアドレスを指定するとtrueを返すこと
	@Test
	public void testEqualsBmsAddressBmsAddress_Equal() {
		var a1 = BmsAddress.of(3, 4.567, 8, 9);
		var a2 = BmsAddress.of(3, 4.567, 8, 9);
		assertTrue(BmsAddress.equals(a1, a2));
	}

	// equals(BmsAddress, BmsAddress)
	// 正常：adr1の楽曲位置の小節番号のみが異なるアドレスを指定するとfalseを返すこと
	@Test
	public void testEqualsBmsAddressBmsAddress_DifferentMeasure() {
		var a1 = BmsAddress.of(2, 4.567, 8, 9);
		var a2 = BmsAddress.of(3, 4.567, 8, 9);
		assertFalse(BmsAddress.equals(a1, a2));
	}

	// equals(BmsAddress, BmsAddress)
	// 正常：adr1の楽曲位置の小節の刻み位置のみがこアドレスを指定するとfalseを返すこと
	@Test
	public void testEqualsBmsAddressBmsAddress_DifferentTick() {
		var a1 = BmsAddress.of(3, 4.566, 8, 9);
		var a2 = BmsAddress.of(3, 4.567, 8, 9);
		assertFalse(BmsAddress.equals(a1, a2));
	}

	// equals(BmsAddress, BmsAddress)
	// 正常：adr1のCHXのチャンネル番号のみが異なるアドレスを指定するとfalseを返すこと
	@Test
	public void testEqualsBmsAddressBmsAddress_DifferentChannel() {
		var a1 = BmsAddress.of(3, 4.567, 7, 9);
		var a2 = BmsAddress.of(3, 4.567, 8, 9);
		assertFalse(BmsAddress.equals(a1, a2));
	}

	// equals(BmsAddress, BmsAddress)
	// 正常：adr1のCHXのチャンネルインデックスのみが異なるアドレスを指定するとfalseを返すこと
	@Test
	public void testEqualsBmsAddressBmsAddress_DifferentIndex() {
		var a1 = BmsAddress.of(3, 4.567, 8, 8);
		var a2 = BmsAddress.of(3, 4.567, 8, 9);
		assertFalse(BmsAddress.equals(a1, a2));
	}

	// equals(BmsAddress, BmsAddress)
	// NullPointerException adr1がnull
	@Test
	public void testEqualsBmsAddressBmsAddress_NullAdr1() {
		var a1 = BmsAddress.of(3, 4.567, 8, 9);
		assertThrows(NullPointerException.class, () -> BmsAddress.equals(null, a1));
	}

	// equals(BmsAddress, BmsAddress)
	// NullPointerException adr2がnull
	@Test
	public void testEqualsBmsAddressBmsAddress_NullAdr2() {
		var a1 = BmsAddress.of(3, 4.567, 8, 9);
		assertThrows(NullPointerException.class, () -> BmsAddress.equals(a1, null));
	}

	// compare(BmsAddress, BmsAddress)
	// 正常：同じアドレスを指定すると0を返すこと
	@Test
	public void testCompareBmsAddressBmsAddress_Equal() {
		var a1 = BmsAddress.of(3, 4.567, 8, 9);
		var a2 = BmsAddress.of(3, 4.567, 8, 9);
		assertEquals(0, BmsAddress.compare(a1, a2));
	}

	// compare(BmsAddress, BmsAddress)
	// 正常：adr1の楽曲位置の小節番号のみが小さいと負の値を返すこと
	@Test
	public void testCompareBmsAddressBmsAddress_Adr1SmallerMeasure() {
		var a1 = BmsAddress.of(2, 4.567, 8, 9);
		var a2 = BmsAddress.of(3, 4.567, 8, 9);
		assertTrue("Expected return negative value, but it was NOT.", BmsAddress.compare(a1, a2) < 0);
	}

	// compare(BmsAddress, BmsAddress)
	// 正常：adr1の楽曲位置の小節番号のみが大きいと正の値を返すこと
	@Test
	public void testCompareBmsAddressBmsAddress_Adr1LargerMeasure() {
		var a1 = BmsAddress.of(4, 4.567, 8, 9);
		var a2 = BmsAddress.of(3, 4.567, 8, 9);
		assertTrue("Expected return positive value, but it was NOT.", BmsAddress.compare(a1, a2) > 0);
	}

	// compare(BmsAddress, BmsAddress)
	// 正常：adr1の楽曲位置の小節の刻み位置のみが小さいと負の値を返すこと
	@Test
	public void testCompareBmsAddressBmsAddress_Adr1SmallerTick() {
		var a1 = BmsAddress.of(3, 4.566, 8, 9);
		var a2 = BmsAddress.of(3, 4.567, 8, 9);
		assertTrue("Expected return negative value, but it was NOT.", BmsAddress.compare(a1, a2) < 0);
	}

	// compare(BmsAddress, BmsAddress)
	// 正常：adr1の楽曲位置の小節の刻み位置のみが大きいと正の値を返すこと
	@Test
	public void testCompareBmsAddressBmsAddress_Adr1LargerTick() {
		var a1 = BmsAddress.of(3, 4.568, 8, 9);
		var a2 = BmsAddress.of(3, 4.567, 8, 9);
		assertTrue("Expected return positive value, but it was NOT.", BmsAddress.compare(a1, a2) > 0);
	}

	// compare(BmsAddress, BmsAddress)
	// 正常：adr1のCHXのチャンネル番号のみが小さいと負の値を返すこと
	@Test
	public void testCompareBmsAddressBmsAddress_Adr1SmallerChannel() {
		var a1 = BmsAddress.of(3, 4.567, 7, 9);
		var a2 = BmsAddress.of(3, 4.567, 8, 9);
		assertTrue("Expected return negative value, but it was NOT.", BmsAddress.compare(a1, a2) < 0);
	}

	// compare(BmsAddress, BmsAddress)
	// 正常：adr1のCHXのチャンネル番号のみが大きいと正の値を返すこと
	@Test
	public void testCompareBmsAddressBmsAddress_Adr1LargerChannel() {
		var a1 = BmsAddress.of(3, 4.567, 9, 9);
		var a2 = BmsAddress.of(3, 4.567, 8, 9);
		assertTrue("Expected return positive value, but it was NOT.", BmsAddress.compare(a1, a2) > 0);
	}

	// compare(BmsAddress, BmsAddress)
	// 正常：adr1のCHXのチャンネルインデックスのみが小さいと負の値を返すこと
	@Test
	public void testCompareBmsAddressBmsAddress_Adr1SmallerIndex() {
		var a1 = BmsAddress.of(3, 4.567, 8, 8);
		var a2 = BmsAddress.of(3, 4.567, 8, 9);
		assertTrue("Expected return negative value, but it was NOT.", BmsAddress.compare(a1, a2) < 0);
	}

	// compare(BmsAddress, BmsAddress)
	// 正常：adr1のCHXのチャンネルインデックスのみが大きいと正の値を返すこと
	@Test
	public void testCompareBmsAddressBmsAddress_Adr1LargerIndex() {
		var a1 = BmsAddress.of(3, 4.567, 8, 10);
		var a2 = BmsAddress.of(3, 4.567, 8, 9);
		assertTrue("Expected return positive value, but it was NOT.", BmsAddress.compare(a1, a2) > 0);
	}

	// compare(BmsAddress, BmsAddress)
	// 正常：adr1の楽曲位置が大きく、指定アドレスのCHXが大きいと正の値を返すこと
	@Test
	public void testCompareBmsAddressBmsAddress_Adr1LargerAtAdr2LargerChx() {
		var a1 = BmsAddress.of(4, 4.567, 7, 9);
		var a2 = BmsAddress.of(3, 4.567, 8, 9);
		assertTrue("Expected return positive value, but it was NOT.", BmsAddress.compare(a1, a2) > 0);
	}

	// compare(BmsAddress, BmsAddress)
	// 正常：adr1のCHXが大きく、指定アドレスの楽曲位置が大きいと負の値を返すこと
	@Test
	public void testCompareBmsAddressBmsAddress_Adr1LargerChxAdr2LargerAt() {
		var a1 = BmsAddress.of(2, 4.567, 9, 9);
		var a2 = BmsAddress.of(3, 4.567, 8, 9);
		assertTrue("Expected return negative value, but it was NOT.", BmsAddress.compare(a1, a2) < 0);
	}

	// compare(BmsAddress, BmsAddress)
	// 正常：0x8000以上のチャンネル番号比較を正常に行えること
	@Test
	public void testCompareBmsAddressBmsAddress_ChannelLarger0x8000() {
		var a1 = BmsAddress.of(0, 0, 10000, 2);
		var a2 = BmsAddress.of(0, 0, 32768, 1);
		assertTrue("Expected return negative value, but it was NOT.", BmsAddress.compare(a1, a2) < 0);
		var a3 = BmsAddress.of(0, 0, 65535, 1);
		var a4 = BmsAddress.of(0, 0, 10000, 2);
		assertTrue("Expected return positive value, but it was NOT.", BmsAddress.compare(a3, a4) > 0);
	}

	// compare(BmsAddress, BmsAddress)
	// 正常：0x8000以上のチャンネルインデックス比較を正常に行えること
	@Test
	public void testCompareBmsAddressBmsAddress_IndexLarger0x8000() {
		var a1 = BmsAddress.of(0, 0, 0, 10000);
		var a2 = BmsAddress.of(0, 0, 0, 32768);
		assertTrue("Expected return negative value, but it was NOT.", BmsAddress.compare(a1, a2) < 0);
		var a3 = BmsAddress.of(0, 0, 0, 65535);
		var a4 = BmsAddress.of(0, 0, 0, 10000);
		assertTrue("Expected return positive value, but it was NOT.", BmsAddress.compare(a3, a4) > 0);
	}

	// compare(BmsAddress, BmsAddress)
	// NullPointerException adr1がnull
	@Test
	public void testCompareBmsAddressBmsAddress_NullAdr1() {
		var a1 = BmsAddress.of(3, 4.567, 8, 9);
		assertThrows(NullPointerException.class, () -> BmsAddress.compare(null, a1));
	}

	// compare(BmsAddress, BmsAddress)
	// NullPointerException adr2がnull
	@Test
	public void testCompareBmsAddressBmsAddress_NullAdr2() {
		var a1 = BmsAddress.of(3, 4.567, 8, 9);
		assertThrows(NullPointerException.class, () -> BmsAddress.compare(a1, null));
	}

	// hashCode(BmsAddress)
	// ハッシュコード値を返すこと
	@Test
	public void testHashCodeBmsAddress_Normal() {
		BmsAddress.hashCode(new BmsAddress());
	}

	// hashCode(BmsAddress)
	// NullPointerException adrがnull
	@Test
	public void testHashCodeBmsAddress_NullAdr() {
		assertThrows(NullPointerException.class, () -> BmsAddress.hashCode(null));
	}

	// of(BmsAddress)
	// 正常：指定されたアドレスと同じ値のインスタンスが生成されること
	@Test
	public void testOfBmsAddress_Normal() {
		var org = BmsAddress.of(3, 4.567, 8, 9);
		var a = BmsAddress.of(org);
		assertNotSame(a, org);
		assertEquals(3, a.getMeasure());
		assertEquals(4.567, a.getTick(), 0.0);
		assertEquals(8, a.getChannel());
		assertEquals(9, a.getIndex());
	}

	// of(BmsAddress)
	// NullPointerException adrがnull
	@Test
	public void testOfBmsAddress_NullAdr() {
		assertThrows(NullPointerException.class, () -> BmsAddress.of(null));
	}

	// of(BmsAt, BmsChx)
	// 正常：指定された楽曲位置・CHXと同じ値のインスタンスが生成されること
	@Test
	public void testOfBmsAtBmsChx_Normal() {
		var orgAt = BmsPoint.of(1, 2.345);
		var orgChx = BmsAddress.of(0, 0, 6, 7);
		var a = BmsAddress.of(orgAt, orgChx);
		assertEquals(1, a.getMeasure());
		assertEquals(2.345, a.getTick(), 0.0);
		assertEquals(6, a.getChannel());
		assertEquals(7, a.getIndex());
	}

	// of(BmsAt, BmsChx)
	// NullPointerException atがnull
	@Test
	public void testOfBmsAtBmsChx_NullAt() {
		assertThrows(NullPointerException.class, () -> BmsAddress.of(null, new BmsAddress()));
	}

	// of(BmsAt, BmsChx)
	// NullPointerException chxがnull
	@Test
	public void testOfBmsAtBmsChx_NullChx() {
		assertThrows(NullPointerException.class, () -> BmsAddress.of(new BmsPoint(), null));
	}

	// of(int, double, int, int)
	// 正常：指定された楽曲位置・CHXと同じ値のインスタンスが生成されること
	@Test
	public void testOfIntDoubleIntInt_Normal() {
		var a = BmsAddress.of(3, 4.567, 8, 9);
		assertEquals(3, a.getMeasure());
		assertEquals(4.567, a.getTick(), 0.0);
		assertEquals(8, a.getChannel());
		assertEquals(9, a.getIndex());
	}
}
