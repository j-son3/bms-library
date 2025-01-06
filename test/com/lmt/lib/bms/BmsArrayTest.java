package com.lmt.lib.bms;

import static org.junit.Assert.*;

import org.junit.Test;

public class BmsArrayTest {
	// BmsArray(int)
	// 基数が16, 36, 62
	@Test
	public void testBmsArray1_001() {
		BmsArray a;
		a = new BmsArray(16);
		assertEquals(16, a.getBase());
		assertEquals(0, a.size());
		a = new BmsArray(36);
		assertEquals(36, a.getBase());
		assertEquals(0, a.size());
		a = new BmsArray(62);
		assertEquals(62, a.getBase());
		assertEquals(0, a.size());
	}

	// BmsArray(int)
	// IllegalArgumentException radixが16, 36, 62以外
	@Test
	public void testBmsArray1_002() {
		var ex = IllegalArgumentException.class;
		assertThrows(ex, () -> new BmsArray(-1));
		assertThrows(ex, () -> new BmsArray(10));
	}

	// BmsArray(String, int)
	// 正常
	@Test
	public void testBmsArray2_001() {
		BmsArray a;
		a = new BmsArray("", 16);
		assertEquals(16, a.getBase());
		assertEquals(0, a.size());
		a = new BmsArray("00112233445566778899aabbccddeeffAABBCCDDEEFF", 16);
		assertEquals(16, a.getBase());
		assertEquals(22, a.size());
		assertEquals(0x00, (int)a.get(0));
		assertEquals(0xaa, (int)a.get(10));
		assertEquals(0xff, (int)a.get(21));

		a = new BmsArray("", 36);
		assertEquals(36, a.getBase());
		assertEquals(0, a.size());
		a = new BmsArray("00aazz99AAZZ", 36);
		assertEquals(36, a.getBase());
		assertEquals(6, a.size());
		assertEquals(0, (int)a.get(0));
		assertEquals(370, (int)a.get(1));
		assertEquals(370, (int)a.get(4));
		assertEquals(1295, (int)a.get(5));

		a = new BmsArray("", 62);
		assertEquals(62, a.getBase());
		assertEquals(0, a.size());
		a = new BmsArray("010cA0xyZj", 62);
		assertEquals(62, a.getBase());
		assertEquals(5, a.size());
		assertEquals(1, (int)a.get(0));
		assertEquals(38, (int)a.get(1));
		assertEquals(620, (int)a.get(2));
		assertEquals(3718, (int)a.get(3));
		assertEquals(2215, (int)a.get(4));
	}

	// BmsArray(String, int)
	// 文字数が奇数であっても、最後の文字が"0"であれば最後の要素が"00"と見なされ解析が正常に完了すること
	@Test
	public void testBmsArrayStringInt_OddLength() {
		BmsArray a;

		a = new BmsArray("0", 16);
		assertEquals(16, a.getBase());
		assertEquals(1, a.size());
		assertEquals(0, (int)a.get(0));
		a = new BmsArray("0", 36);
		assertEquals(36, a.getBase());
		assertEquals(1, a.size());
		assertEquals(0, (int)a.get(0));
		a = new BmsArray("0", 62);
		assertEquals(62, a.getBase());
		assertEquals(1, a.size());
		assertEquals(0, (int)a.get(0));

		a = new BmsArray("5500BB0", 16);
		assertEquals(16, a.getBase());
		assertEquals(4, a.size());
		assertEquals(0x55, (int)a.get(0));
		assertEquals(0xbb, (int)a.get(2));
		assertEquals(0x00, (int)a.get(3));
		a = new BmsArray("9900AAXX0", 36);
		assertEquals(36, a.getBase());
		assertEquals(5, a.size());
		assertEquals(BmsInt.to36i("99"), (int)a.get(0));
		assertEquals(BmsInt.to36i("AA"), (int)a.get(2));
		assertEquals(BmsInt.to36i("XX"), (int)a.get(3));
		assertEquals(BmsInt.to36i("00"), (int)a.get(4));
		a = new BmsArray("11zz0", 62);
		assertEquals(62, a.getBase());
		assertEquals(3, a.size());
		assertEquals(63, (int)a.get(0));
		assertEquals(3843, (int)a.get(1));
		assertEquals(0, (int)a.get(2));
	}

	// BmsArray(String, int)
	// NullPointerException srcがnull
	@Test
	public void testBmsArray2_002() {
		assertThrows(NullPointerException.class, () -> new BmsArray(null, 16));
	}

	// BmsArray(String, int)
	// IllegalArgumentException srcの文字数が2で割り切れず、最後の文字が"0"ではない
	@Test
	public void testBmsArray2_003() {
		assertThrows(IllegalArgumentException.class, () -> new BmsArray("1", 16));
		assertThrows(IllegalArgumentException.class, () -> new BmsArray("H", 36));
		assertThrows(IllegalArgumentException.class, () -> new BmsArray("v", 62));
		assertThrows(IllegalArgumentException.class, () -> new BmsArray("aabbccD", 16));
		assertThrows(IllegalArgumentException.class, () -> new BmsArray("QQ6600Z", 36));
		assertThrows(IllegalArgumentException.class, () -> new BmsArray("33ff00w", 62));
	}

	// BmsArray(String, int)
	// IllegalArgumentException srcに解析不可能な文字が含まれる
	@Test
	public void testBmsArray2_004() {
		var ex = IllegalArgumentException.class;
		assertThrows(ex, () -> new BmsArray("  007fabff  ", 16));  // 空白あり
		assertThrows(ex, () -> new BmsArray("0011AAFFZZ", 16));  // 基数16で36の文字あり
		assertThrows(ex, () -> new BmsArray("9abbccff##", 16));  // 純粋に非対応文字
		assertThrows(ex, () -> new BmsArray("  00axbycz  ", 36));  // 空白あり
		assertThrows(ex, () -> new BmsArray("zzqqJJ##", 36));  // 純粋に非対応文字
		assertThrows(ex, () -> new BmsArray("  00JKbba0  ", 62));  // 空白あり
		assertThrows(ex, () -> new BmsArray("PPllrR##", 62));  // 純粋に非対応文字
	}

	// BmsArray(String, int)
	// IllegalArgumentException radixが16, 36, 62以外
	@Test
	public void testBmsArray2_005() {
		assertThrows(IllegalArgumentException.class, () -> new BmsArray("", 38));
	}

	// BmsArray(BmsArray)
	// 正常
	@Test
	public void testBmsArray3_001() {
		BmsArray a, src;
		src = new BmsArray("0055aaff", 16);
		a = new BmsArray(src);
		assertEquals(16, a.getBase());
		assertEquals(4, a.size());
		assertEquals(0x00, (int)a.get(0));
		assertEquals(0x55, (int)a.get(1));
		assertEquals(0xff, (int)a.get(3));

		src = new BmsArray("zzppffaa5500", 36);
		a = new BmsArray(src);
		assertEquals(36, a.getBase());
		assertEquals(6, a.size());
		assertEquals(1295, (int)a.get(0));
		assertEquals(555, (int)a.get(2));
		assertEquals(185, (int)a.get(4));
		assertEquals(0, (int)a.get(5));

		src = new BmsArray("zz2200", 62);
		a = new BmsArray(src);
		assertEquals(62, a.getBase());
		assertEquals(3, a.size());
		assertEquals(3843, (int)a.get(0));
		assertEquals(126, (int)a.get(1));
		assertEquals(0, (int)a.get(2));
	}

	// BmsArray(BmsArray)
	// NullPointerException arrayがnull
	@Test
	public void testBmsArray3_002() {
		assertThrows(NullPointerException.class, () -> new BmsArray(null));
	}

	// toString()
	@Test
	public void testToString_001() {
		BmsArray a;
		a = new BmsArray(16);
		assertEquals("", a.toString());
		a = new BmsArray(36);
		assertEquals("", a.toString());
		a = new BmsArray(62);
		assertEquals("", a.toString());

		a = new BmsArray("0034ABEF", 16);
		assertEquals("0034ABEF", a.toString());
		a = new BmsArray("ffaaCC774400", 16);
		assertEquals("FFAACC774400", a.toString());
		a = new BmsArray("1133557799AAGGWWZZ", 36);
		assertEquals("1133557799AAGGWWZZ", a.toString());
		a = new BmsArray("yzstfg9a2301", 36);
		assertEquals("YZSTFG9A2301", a.toString());
		a = new BmsArray("00abXYmQZx", 62);
		assertEquals("00abXYmQZx", a.toString());
	}

	// get(int)
	// 正常
	@Test
	public void testGet_001() {
		BmsArray a;

		a = new BmsArray("EFAB98652100", 16);
		assertEquals(0xef, (int)a.get(0));
		assertEquals(0xab, (int)a.get(1));
		assertEquals(0x98, (int)a.get(2));
		assertEquals(0x65, (int)a.get(3));
		assertEquals(0x21, (int)a.get(4));
		assertEquals(0x00, (int)a.get(5));

		a = new BmsArray("003388AAZZ", 36);
		assertEquals(0, (int)a.get(0));
		assertEquals(111, (int)a.get(1));
		assertEquals(296, (int)a.get(2));
		assertEquals(370, (int)a.get(3));
		assertEquals(1295, (int)a.get(4));

		a = new BmsArray("01aaXX", 62);
		assertEquals(1, (int)a.get(0));
		assertEquals(2268, (int)a.get(1));
		assertEquals(2079, (int)a.get(2));
	}

	// get(int)
	// IndexOutOfBoundsException - インデックスが範囲外の場合(index < 0||index>= size())
	@Test
	public void testGet_002() {
		var a = new BmsArray("0099ZZ", 36);
		var ex = IndexOutOfBoundsException.class;
		assertThrows(ex, () -> a.get(-1));
		assertThrows(ex, () -> a.get(3));
	}

	// size()
	@Test
	public void testSize() {
		assertEquals(0, (int)new BmsArray(16).size());
		assertEquals(0, (int)new BmsArray(36).size());
		assertEquals(0, (int)new BmsArray(62).size());
		assertEquals(4, (int)new BmsArray("0088AAFF", 16).size());
		assertEquals(7, (int)new BmsArray("ZZQQHHCCAA3300", 36).size());
		assertEquals(5, (int)new BmsArray("0011AAxxzz", 62).size());
	}

	// getBase()
	@Test
	public void testGetBase() {
		assertEquals(16, (int)new BmsArray(16).getBase());
		assertEquals(36, (int)new BmsArray(36).getBase());
		assertEquals(62, (int)new BmsArray(62).getBase());
		assertEquals(16, (int)new BmsArray("0088AAFF", 16).getBase());
		assertEquals(36, (int)new BmsArray("ZZQQHHCCAA3300", 36).getBase());
		assertEquals(62, (int)new BmsArray("77hhDD0055", 62).getBase());
		assertEquals(16, (int)new BmsArray(new BmsArray(16)).getBase());
		assertEquals(36, (int)new BmsArray(new BmsArray(36)).getBase());
		assertEquals(62, (int)new BmsArray(new BmsArray(62)).getBase());
	}

}
