package com.lmt.lib.bms;

import static org.junit.Assert.*;

import org.junit.Test;

public class BmsChxTest {
	// equals(BmsChx, BmsChx)
	// 正常：同じCHXを指定するとtrueを返すこと
	@Test
	public void testEqualsBmsChxBmsChx_Equal() {
		var x1 = BmsAddress.of(0, 0, 3, 4);
		var x2 = BmsAddress.of(0, 0, 3, 4);
		assertTrue("Expected return true, but false.", BmsChx.equals(x1, x2));
	}

	// equals(BmsChx, BmsChx)
	// 正常：チャンネル番号のみが異なるCHXを指定するとfalseを返すこと
	@Test
	public void testEqualsBmsChxBmsChx_DifferentNumber() {
		var x1 = BmsAddress.of(0, 0, 2, 4);
		var x2 = BmsAddress.of(0, 0, 3, 4);
		assertFalse("Expected return false, but true.", BmsChx.equals(x1, x2));
	}

	// equals(BmsChx, BmsChx)
	// 正常：チャンネルインデックスのみが異なるCHXを指定するとfalseを返すこと
	@Test
	public void testEqualsBmsChxBmsChx_DifferentIndex() {
		var x1 = BmsAddress.of(0, 0, 3, 2);
		var x2 = BmsAddress.of(0, 0, 3, 4);
		assertFalse("Expected return false, but true.", BmsChx.equals(x1, x2));
	}

	// equals(BmsChx, BmsChx)
	// 正常：番号、インデックスの両方が異なるCHXを指定するとfalseを返すこと
	@Test
	public void testEqualsBmsChxBmsChx_DifferentAll() {
		var x1 = BmsAddress.of(0, 0, 2, 1);
		var x2 = BmsAddress.of(0, 0, 3, 4);
		assertFalse("Expected return false, but true.", BmsChx.equals(x1, x2));
	}

	// equals(BmsChx, BmsChx)
	// NullPointerException chx1がnull
	@Test
	public void testEqualsBmsChxBmsChx_NullChx1() {
		assertThrows(NullPointerException.class, () -> BmsChx.equals(null, BmsAddress.of(0, 0, 0, 0)));
	}

	// equals(BmsChx, BmsChx)
	// NullPointerException chx2がnull
	@Test
	public void testEqualsBmsChxBmsChx_NullChx2() {
		assertThrows(NullPointerException.class, () -> BmsChx.equals(BmsAddress.of(0, 0, 0, 0), null));
	}

	// compare(BmsChx, BmsChx)
	// 正常：同じCHXを指定すると0を返すこと
	@Test
	public void testCompareBmsChxBmsChx_Equal() {
		var x1 = BmsAddress.of(0, 0, 3, 4);
		var x2 = BmsAddress.of(0, 0, 3, 4);
		assertEquals(0, BmsChx.compare(x1, x2));
	}

	// compare(BmsChx, BmsChx)
	// 正常：CHX1のチャンネル番号のみが小さいと負の値を返すこと
	@Test
	public void testCompareBmsChxBmsChx_Chx1SmallerNumber() {
		var x1 = BmsAddress.of(0, 0, 2, 4);
		var x2 = BmsAddress.of(0, 0, 3, 4);
		assertTrue("Expected return negative value, but is was NOT.", BmsChx.compare(x1, x2) < 0);
	}

	// compare(BmsChx, BmsChx)
	// 正常：CHX1のチャンネル番号のみが大きいと正の値を返すこと
	@Test
	public void testCompareBmsChxBmsChx_Chx1LargerNumber() {
		var x1 = BmsAddress.of(0, 0, 5, 4);
		var x2 = BmsAddress.of(0, 0, 3, 4);
		assertTrue("Expected return positive value, but is was NOT.", BmsChx.compare(x1, x2) > 0);
	}

	// compare(BmsChx, BmsChx)
	// 正常：CHX1のチャンネルインデックスのみが小さいと負の値を返すこと
	@Test
	public void testCompareBmsChxBmsChx_Chx1SmallerIndex() {
		var x1 = BmsAddress.of(0, 0, 3, 2);
		var x2 = BmsAddress.of(0, 0, 3, 4);
		assertTrue("Expected return negative value, but is was NOT.", BmsChx.compare(x1, x2) < 0);
	}

	// compare(BmsChx, BmsChx)
	// 正常：CHX1のチャンネルインデックスのみが大きいと正の値を返すこと
	@Test
	public void testCompareBmsChxBmsChx_Chx2LargerIndex() {
		var x1 = BmsAddress.of(0, 0, 3, 5);
		var x2 = BmsAddress.of(0, 0, 3, 4);
		assertTrue("Expected return positive value, but is was NOT.", BmsChx.compare(x1, x2) > 0);
	}

	// compare(BmsChx, BmsChx)
	// NullPointerException chx1がnull
	@Test
	public void testCompareBmsChxBmsChx_NullChx1() {
		assertThrows(NullPointerException.class, () -> BmsChx.compare(null, BmsAddress.of(0, 0, 0, 0)));
	}

	// compare(BmsChx, BmsChx)
	// NullPointerException chx2がnull
	@Test
	public void testCompareBmsChxBmsChx_NullChx2() {
		assertThrows(NullPointerException.class, () -> BmsChx.compare(BmsAddress.of(0, 0, 0, 0), null));
	}

	// hashCode(BmsChx)
	// 正常：ハッシュコード値を返すこと
	@Test
	public void testHashCodeBmsChx_Normal() {
		var x1 = BmsAddress.of(0, 0, 3, 5);
		var h1 = BmsChx.hashCode(x1);
		var x2 = BmsAddress.of(1, 1, 3, 5);
		var h2 = BmsChx.hashCode(x2);
		assertEquals(h1, h2);
	}

	// hashCode(BmsChx)
	// NullPointerException chxがnull
	@Test
	public void testHashCodeBmsChx_NullChx() {
		assertThrows(NullPointerException.class, () -> BmsChx.hashCode(null));
	}

	// toString(BmsChx)
	// 正常：nullにならないこと
	@Test
	public void testToStringBmsChx_Normal() {
		var x1 = BmsAddress.of(0, 0, 3, 5);
		var s1 = BmsChx.toString(x1);
		var x2 = BmsAddress.of(1, 1, 3, 5);
		var s2 = BmsChx.toString(x2);
		assertNotNull(s1);
		assertNotNull(s2);
		assertEquals(s1, s2);
	}

	// toString(BmsChx)
	// NullPointerException chxがnull
	@Test
	public void testToStringBmsChx_NullChx() {
		assertThrows(NullPointerException.class, () -> BmsChx.toString(null));
	}

	// toString(int)
	// 正常：nullにならないこと
	@Test
	public void testToStringInt_Normal() {
		var x = BmsChx.toInt(3, 4);
		var s = BmsChx.toString(x);
		assertNotNull(s);
	}

	// toInt(int, int)
	// 正常：正しいCHX値を返すこと
	@Test
	public void testToIntIntInt_Normal() {
		assertEquals(0x11224433, BmsChx.toInt(0x1122, 0x4433));
	}

	// toInt(int, int)
	// 正常：チャンネル番号を0xffffにしても正しいCHX値を返すこと
	@Test
	public void testToIntIntInt_MaxNumber() {
		assertEquals(0xffff0000, BmsChx.toInt(0xffff, 0));
	}

	// toInt(int, int)
	// 正常：チャンネルインデックスを0xffffにしても正しいCHX値を返すこと
	@Test
	public void testToIntIntInt_MaxIndex() {
		assertEquals(0x0000ffff, BmsChx.toInt(0, 0xffff));
	}

	// toInt(int, int)
	// 準正常：番号、インデックスの上位16ビットが0以外でも上位16ビットは無視されること
	@Test
	public void testToIntIntInt_IgnoreHighBits() {
		assertEquals(0x12344321, BmsChx.toInt(0xaaaa1234, 0xbbbb4321));
	}

	// toInt(int)
	// 正常：正しいCHX値を返すこと
	@Test
	public void testToIntInt_Normal() {
		assertEquals(0x12340000, BmsChx.toInt(0x1234));
	}

	// toInt(int)
	// 正常：チャンネル番号を0xffffにしても正しいCHX値を返すこと
	@Test
	public void testToIntInt_MaxNumber() {
		assertEquals(0xffff0000, BmsChx.toInt(0xffff));
	}

	// toInt(int)
	// 準正常：チャンネル番号の上位16ビットが0以外でも上位16ビットは無視されること
	@Test
	public void testToIntInt_IgnoreHighBits() {
		assertEquals(0x12340000, BmsChx.toInt(0xaaaa1234));
	}

	// toInt(BmsChx)
	// 正常：正しいCHX値を返すこと
	@Test
	public void testToIntBmsChx_Normal() {
		assertEquals(0x00030009, BmsChx.toInt(BmsAddress.of(0, 0, 3, 9)));
	}

	// toInt(BmsChx)
	// 正常：チャンネル番号を0xffffにしても正しいCHX値を返すこと
	@Test
	public void testToIntBmsChx_MaxNumber() {
		assertEquals(0xffff0000, BmsChx.toInt(BmsAddress.of(0, 0, 0xffff, 0)));
	}

	// toInt(BmsChx)
	// 正常：チャンネルインデックスを0xffffにしても正しいCHX値を返すこと
	@Test
	public void testToIntBmsChx_MaxIndex() {
		assertEquals(0x0000ffff, BmsChx.toInt(BmsAddress.of(0, 0, 0, 0xffff)));
	}

	// toInt(BmsChx)
	// 準正常：番号、インデックスの上位16ビットが0以外でも上位16ビットは無視されること
	@Test
	public void testToIntBmsChx_IgnoreHighBits() {
		assertEquals(0x12344321, BmsChx.toInt(BmsAddress.of(0, 0, 0xaaaa1234, 0xbbbb4321)));
	}

	// toInt(BmsChx)
	// NullPointerException chxがnull
	@Test
	public void testToIntBmsChx_NullChx() {
		assertThrows(NullPointerException.class, () -> BmsChx.toInt(null));
	}

	// toChannel(int)
	// 正常：正しいチャンネル番号を返すこと
	@Test
	public void testToChannelInt_Normal() {
		assertEquals(0x1234, BmsChx.toChannel(0x12340000));
	}

	// toChannel(int)
	// 正常：チャンネル番号0xffffのCHX値から65535を返すこと
	@Test
	public void testToChannelInt_Max() {
		assertEquals(65535, BmsChx.toChannel(0xffff0000));
	}

	// toIndex(int)
	// 正常：正しいチャンネルインデックスを返すこと
	@Test
	public void testToIndexInt_Normal() {
		assertEquals(0x1234, BmsChx.toIndex(0xaaaa1234));
	}

	// toIndex(int)
	// 正常：チャンネルインデックス0xffffのCHX値から65535を返すこと
	@Test
	public void testToIndexInt_Max() {
		assertEquals(65535, BmsChx.toIndex(0x1234ffff));
	}
}
