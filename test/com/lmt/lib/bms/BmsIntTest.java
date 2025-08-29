package com.lmt.lib.bms;

import static org.junit.Assert.*;

import java.util.regex.Pattern;

import org.junit.Test;

public class BmsIntTest {
	// base()
	@Test
	public void testBase() {
		// No test: 他のテストケースでカバーする
	}

	// max()
	// 基数ごとの正しい最大値を返すこと
	@Test
	public void testMax() {
		assertEquals(255, BmsInt.base16().max());
		assertEquals(1295, BmsInt.base36().max());
		assertEquals(3843, BmsInt.base62().max());
	}

	// tos(int)
	// 数値を正常に変換できること
	@Test
	public void testTos_Ok() {
		var b16 = BmsInt.base16();
		assertEquals("01", b16.tos(1));
		assertEquals("0A", b16.tos(10));
		assertEquals("80", b16.tos(128));
		var b36 = BmsInt.base36();
		assertEquals("01", b36.tos(1));
		assertEquals("0Z", b36.tos(35));
		assertEquals("Z0", b36.tos(35 * 36));
		var b62 = BmsInt.base62();
		assertEquals("01", b62.tos(1));
		assertEquals("0Z", b62.tos(35));
		assertEquals("0z", b62.tos(61));
		assertEquals("Z0", b62.tos(35 * 62));
		assertEquals("z0", b62.tos(61 * 62));
	}

	// tos(int)
	// IllegalArgumentException nが0未満
	@Test
	public void testTos_Underflow() {
		var ex = IllegalArgumentException.class;
		assertThrows(ex, () -> BmsInt.base16().tos(-1));
		assertThrows(ex, () -> BmsInt.base36().tos(-1));
		assertThrows(ex, () -> BmsInt.base62().tos(-1));
	}

	// tos(int)
	// IllegalArgumentException nがmax()超過
	@Test
	public void testTos_Overflow() {
		var ex = IllegalArgumentException.class;
		assertThrows(ex, () -> BmsInt.base16().tos(BmsInt.base16().max() + 1));
		assertThrows(ex, () -> BmsInt.base36().tos(BmsInt.base36().max() + 1));
		assertThrows(ex, () -> BmsInt.base62().tos(BmsInt.base62().max() + 1));
	}

	// toi(String)
	// 文字列を正常に変換できること
	@Test
	public void testToi_Ok() {
		var b16 = BmsInt.base16();
		assertEquals(1, b16.toi("01"));
		assertEquals(255, b16.toi("ff"));
		assertEquals(255, b16.toi("FF"));
		assertEquals(138, b16.toi("8A"));
		var b36 = BmsInt.base36();
		assertEquals(1, b36.toi("01"));
		assertEquals(16 * 36 + 5, b36.toi("g5"));
		assertEquals(3 * 36 + 35, b36.toi("3Z"));
		assertEquals(35 * 36 + 35, b36.toi("ZZ"));
		var b62 = BmsInt.base62();
		assertEquals(1, b62.toi("01"));
		assertEquals(3 * 62 + 10, b62.toi("3A"));
		assertEquals(38 * 62 + 2, b62.toi("c2"));
		assertEquals(61 * 62 + 35, b62.toi("zZ"));
	}

	// toi(String)
	// NullPointerException sがnull
	@Test
	public void testToi_NullS() {
		var ex = NullPointerException.class;
		assertThrows(ex, () -> BmsInt.base16().toi(null));
		assertThrows(ex, () -> BmsInt.base36().toi(null));
		assertThrows(ex, () -> BmsInt.base62().toi(null));
	}

	// toi(String)
	// IllegalArgumentException sの長さが2以外
	@Test
	public void testToi_LengthNot2() {
		var ex = IllegalArgumentException.class;
		assertThrows(ex, () -> BmsInt.base16().toi(""));
		assertThrows(ex, () -> BmsInt.base16().toi("1"));
		assertThrows(ex, () -> BmsInt.base16().toi("123"));
		assertThrows(ex, () -> BmsInt.base36().toi(""));
		assertThrows(ex, () -> BmsInt.base36().toi("1"));
		assertThrows(ex, () -> BmsInt.base36().toi("123"));
		assertThrows(ex, () -> BmsInt.base62().toi(""));
		assertThrows(ex, () -> BmsInt.base62().toi("1"));
		assertThrows(ex, () -> BmsInt.base62().toi("123"));
	}

	// toi(String)
	// IllegalArgumentException sに変換不可能な文字がある
	@Test
	public void testToi_IllegalS() {
		var ex = IllegalArgumentException.class;
		assertThrows(ex, () -> BmsInt.base16().toi("1*"));
		assertThrows(ex, () -> BmsInt.base16().toi("?A"));
		assertThrows(ex, () -> BmsInt.base36().toi("1*"));
		assertThrows(ex, () -> BmsInt.base36().toi("?A"));
		assertThrows(ex, () -> BmsInt.base62().toi("1*"));
		assertThrows(ex, () -> BmsInt.base62().toi("?A"));
	}

	// tosa(int[], int, int)
	// 整数値配列を正常に変換できること
	@Test
	public void testTosa_Ok() {
		var b16 = BmsInt.base16();
		assertEquals("", b16.tosa(new int[] {}, 0, 0));
		assertEquals("010FF0FF", b16.tosa(new int[] { 1, 15, 240, 255 }, 0, 4));
		assertEquals("0203", b16.tosa(new int[] { 1, 2, 3, 4 }, 1, 2));
		var b36 = BmsInt.base36();
		assertEquals("", b36.tosa(new int[] {}, 0, 0));
		assertEquals("010ZZ0ZZ", b36.tosa(new int[] { 1, 35, 1260, 1295 }, 0, 4));
		assertEquals("0203", b36.tosa(new int[] { 1, 2, 3, 4 }, 1, 2));
		var b62 = BmsInt.base62();
		assertEquals("", b62.tosa(new int[] {}, 0, 0));
		assertEquals("010zz0zz", b62.tosa(new int[] { 1, 61, 3782, 3843 }, 0, 4));
		assertEquals("0203", b62.tosa(new int[] { 1, 2, 3, 4 }, 1, 2));
	}

	// tosa(int[], int, int)
	// NullPointerException aがnull
	@Test
	public void testTosa_NullA() {
		var ex = NullPointerException.class;
		assertThrows(ex, () -> BmsInt.base16().tosa(null, 0, 0));
		assertThrows(ex, () -> BmsInt.base36().tosa(null, 0, 0));
		assertThrows(ex, () -> BmsInt.base62().tosa(null, 0, 0));
	}

	// tosa(int[], int, int)
	// IllegalArgumentException offsetが0未満
	@Test
	public void testTosa_MinusOffset() {
		var ex = IllegalArgumentException.class;
		assertThrows(ex, () -> BmsInt.base16().tosa(new int[] { 0 }, -1, 1));
		assertThrows(ex, () -> BmsInt.base36().tosa(new int[] { 0 }, -1, 1));
		assertThrows(ex, () -> BmsInt.base62().tosa(new int[] { 0 }, -1, 1));
	}

	// tosa(int[], int, int)
	// IllegalArgumentException countが0未満
	@Test
	public void testTosa_UnderflowCount() {
		var ex = IllegalArgumentException.class;
		assertThrows(ex, () -> BmsInt.base16().tosa(new int[] { 0 }, 0, -1));
		assertThrows(ex, () -> BmsInt.base36().tosa(new int[] { 0 }, 0, -1));
		assertThrows(ex, () -> BmsInt.base62().tosa(new int[] { 0 }, 0, -1));
	}

	// tosa(int[], int, int)
	// IndexOutOfBoundsException aの範囲外にアクセスした
	@Test
	public void testTosa_WithoutA() {
		var ex = IndexOutOfBoundsException.class;
		assertThrows(ex, () -> BmsInt.base16().tosa(new int[] { 0 }, 0, 2));
		assertThrows(ex, () -> BmsInt.base16().tosa(new int[] { 0 }, 1, 1));
		assertThrows(ex, () -> BmsInt.base36().tosa(new int[] { 0 }, 0, 2));
		assertThrows(ex, () -> BmsInt.base36().tosa(new int[] { 0 }, 1, 1));
		assertThrows(ex, () -> BmsInt.base62().tosa(new int[] { 0 }, 0, 2));
		assertThrows(ex, () -> BmsInt.base62().tosa(new int[] { 0 }, 1, 1));
	}

	// tosa(int[], int, int)
	// IllegalArgumentException a内の変換対象に0未満の値がある
	@Test
	public void testTosa_AHasUnderflow() {
		var ex = IllegalArgumentException.class;
		assertThrows(ex, () -> BmsInt.base16().tosa(new int[] { 0, 1, -1 }, 0, 3));
		assertThrows(ex, () -> BmsInt.base36().tosa(new int[] { 0, 1, -1 }, 0, 3));
		assertThrows(ex, () -> BmsInt.base62().tosa(new int[] { 0, 1, -1 }, 0, 3));
	}

	// tosa(int[], int, int)
	// IllegalArgumentException a内の変換対象にmax()超過の値がある
	@Test
	public void testTosa_AHasOverflow() {
		var ex = IllegalArgumentException.class;
		assertThrows(ex, () -> BmsInt.base16().tosa(new int[] { 0, 1, 256 }, 0, 3));
		assertThrows(ex, () -> BmsInt.base36().tosa(new int[] { 0, 1, 1296 }, 0, 3));
		assertThrows(ex, () -> BmsInt.base62().tosa(new int[] { 0, 1, 3844 }, 0, 3));
	}

	// toia(String, int, int)
	// 文字列を正常に整数値配列へ変換できること
	@Test
	public void testToia_Ok() {
		var b16 = BmsInt.base16();
		assertArrayEquals(new int[] {}, b16.toia("", 0, 0));
		assertArrayEquals(new int[] { 1, 15, 240, 255 }, b16.toia("010FF0FF", 0, 4));
		assertArrayEquals(new int[] { 1, 15, 240, 255 }, b16.toia("010ff0ff", 0, 4));
		assertArrayEquals(new int[] { 2, 3 }, b16.toia("01020304", 2, 2));
		var b36 = BmsInt.base36();
		assertArrayEquals(new int[] {}, b36.toia("", 0, 0));
		assertArrayEquals(new int[] { 1, 35, 1260, 1295 }, b36.toia("010ZZ0ZZ", 0, 4));
		assertArrayEquals(new int[] { 1, 35, 1260, 1295 }, b36.toia("010zz0zz", 0, 4));
		assertArrayEquals(new int[] { 2, 3 }, b36.toia("01020304", 2, 2));
		var b62 = BmsInt.base62();
		assertArrayEquals(new int[] {}, b62.toia("", 0, 0));
		assertArrayEquals(new int[] { 1, 35, 2170, 2205 }, b62.toia("010ZZ0ZZ", 0, 4));
		assertArrayEquals(new int[] { 1, 61, 3782, 3843 }, b62.toia("010zz0zz", 0, 4));
		assertArrayEquals(new int[] { 2, 3 }, b62.toia("01020304", 2, 2));
	}

	// toia(String, int, int)
	// NullPointerException aがnull
	@Test
	public void testToia_NullA() {
		var ex = NullPointerException.class;
		assertThrows(ex, () -> BmsInt.base16().toia(null, 0, 0));
		assertThrows(ex, () -> BmsInt.base36().toia(null, 0, 0));
		assertThrows(ex, () -> BmsInt.base62().toia(null, 0, 0));
	}

	// toia(String, int, int)
	// IllegalArgumentException offsetが0未満
	@Test
	public void testToia_MinusOffset() {
		var ex = IllegalArgumentException.class;
		assertThrows(ex, () -> BmsInt.base16().toia("00", -1, 1));
		assertThrows(ex, () -> BmsInt.base36().toia("00", -1, 1));
		assertThrows(ex, () -> BmsInt.base62().toia("00", -1, 1));
	}

	// toia(String, int, int)
	// IllegalArgumentException countが0未満
	@Test
	public void testToia_UnderflowCount() {
		var ex = IllegalArgumentException.class;
		assertThrows(ex, () -> BmsInt.base16().toia("00", 0, -1));
		assertThrows(ex, () -> BmsInt.base36().toia("00", 0, -1));
		assertThrows(ex, () -> BmsInt.base62().toia("00", 0, -1));
	}

	// toia(String, int, int)
	// IndexOutOfBoundsException sの範囲外にアクセスした
	@Test
	public void testToia_WithoutS() {
		var ex = IndexOutOfBoundsException.class;
		assertThrows(ex, () -> BmsInt.base16().toia("00", 1, 1));
		assertThrows(ex, () -> BmsInt.base36().toia("00", 1, 1));
		assertThrows(ex, () -> BmsInt.base62().toia("00", 1, 1));
	}

	// toia(String, int, int)
	// IllegalArgumentException sに変換不可能な文字がある
	@Test
	public void testToia_SHasInvalidChar() {
		var ex = IllegalArgumentException.class;
		assertThrows(ex, () -> BmsInt.base16().toia("01020*", 0, 3));
		assertThrows(ex, () -> BmsInt.base36().toia("01020*", 0, 3));
		assertThrows(ex, () -> BmsInt.base62().toia("01020*", 0, 3));
	}

	// base16()
	// 16進数用の整数オブジェクトが取得できること
	@Test
	public void testBase16() {
		assertEquals(16, BmsInt.base16().base());
	}

	// base36()
	// 36進数用の整数オブジェクトが取得できること
	@Test
	public void testBase36() {
		assertEquals(36, BmsInt.base36().base());
	}

	// base62()
	// 62進数用の整数オブジェクトが取得できること
	@Test
	public void testBase62() {
		assertEquals(62, BmsInt.base62().base());
	}

	// of(int)
	// 指定した基数に応じた整数オブジェクトが取得できること
	@Test
	public void testOf_Ok() {
		assertEquals(16, BmsInt.of(16).base());
		assertEquals(36, BmsInt.of(36).base());
		assertEquals(62, BmsInt.of(62).base());
	}

	// of(int)
	// IllegalArgumentException baseが16, 36, 62以外
	@Test
	public void testOf_BadBase() {
		var ex = IllegalArgumentException.class;
		assertThrows(ex, () -> BmsInt.of(-1));
		assertThrows(ex, () -> BmsInt.of(10));
	}

	// cacheStatistics(boolean)
	// falseを設定すると統計情報が無効になること
	@Test
	public void testCacheStatisticsBoolean_Disable() {
		BmsInt.cacheStatistics(false);
		BmsInt.box(0);
		BmsInt.box(BmsChx.toInt(0, 0));
		assertArrayEquals(new long[] { 0, 0, 0, 0, 0, 0 }, BmsInt.cacheSnapshotResult());
	}

	// cacheStatistics(boolean)
	// trueを設定すると統計情報が有効になること
	@Test
	public void testCacheStatisticsBoolean_Enable() {
		BmsInt.cacheStatistics(true);
		BmsInt.box(0);
		BmsInt.box(BmsChx.toInt(0, 0));
		assertEquals(2L, BmsInt.cacheSnapshotResult()[0]);
	}

	// cacheStatistics(boolean)
	// 統計情報の有効状態を設定すると統計情報のカウンタがリセットされること
	@Test
	public void testCacheStatisticsBoolean_ResetCounters() {
		BmsInt.cacheStatistics(true);
		BmsInt.box(0);
		BmsInt.box(1296);
		BmsInt.box(BmsChx.toInt(0, 0));
		BmsInt.box(BmsChx.toInt(1296, 0));
		BmsInt.cacheStatistics(true);
		assertArrayEquals(new long[] { 0, 0, 0, 0, 0, 0 }, BmsInt.cacheSnapshotResult());
	}

	// cacheSnapshotResult()
	// 統計情報が無効の時は6つの値が全て0であること
	@Test
	public void testCacheSnapshotResult_Disable() {
		BmsInt.cacheStatistics(false);
		BmsInt.box(0);
		BmsInt.box(1296);
		BmsInt.box(BmsChx.toInt(0, 0));
		BmsInt.box(BmsChx.toInt(1296, 0));
		assertArrayEquals(new long[] { 0, 0, 0, 0, 0, 0 }, BmsInt.cacheSnapshotResult());
	}

	// cacheSnapshotResult()
	// 統計情報が有効の時、6つの値が取得でき、それぞれの値が正しくカウントされていること
	@Test
	public void testCacheSnapshotResult_Enable() {
		BmsInt.cacheStatistics(true);
		BmsInt.box(0);
		BmsInt.box(1);
		BmsInt.box(2);
		BmsInt.box(3844);
		BmsInt.box(BmsChx.toInt(1, 0));
		BmsInt.box(BmsChx.toInt(1, 1));
		BmsInt.box(BmsChx.toInt(1295, 16));
		BmsInt.box(BmsChx.toInt(1295, 17));
		BmsInt.box(BmsChx.toInt(1295, 18));
		BmsInt.box(BmsChx.toInt(1295, 19));
		BmsInt.box(-1);
		BmsInt.box(BmsChx.toInt(1296, 0));
		assertArrayEquals(new long[] { 12, 5, 4, 3, 6, 2 }, BmsInt.cacheSnapshotResult());
	}

	// box(int)
	// 0～1295を指定するとキャッシュされた値が返ること
	@Test
	public void testBoxInt_IndexWithinRange() {
		for (var i = 0; i <= 1295; i++) {
			var o = BmsInt.box(i);
			assertSame(o, BmsInt.box(i));
		}
	}

	// box(int)
	// 3844以上を指定すると新しいインスタンスの整数値オブジェクトが返ること
	@Test
	public void testBoxInt_IndexOutOfRange() {
		var n = 3844;
		var o = BmsInt.box(n);
		assertNotSame(o, BmsInt.box(n));
		n = 65535;
		o = BmsInt.box(n);
		assertNotSame(o, BmsInt.box(n));
	}

	// box(int)
	// 負の値を指定すると新しいインスタンスの整数値オブジェクトが返ること
	@Test
	public void testBoxInt_Negative() {
		var n = -129;  // -128まではJava標準でキャッシュしている
		var o = BmsInt.box(n);
		assertNotSame(o, BmsInt.box(n));
		n = Integer.MIN_VALUE;
		o = BmsInt.box(n);
		assertNotSame(o, BmsInt.box(n));
	}

	// box(int)
	// CHX(0～1295, 0～3)を指定するとキャッシュされた値が返ること
	@Test
	public void testBoxInt_ChxWithinRange() {
		for (var c = 0; c <= 1295; c++) {
			for (var i = 0; i <= 3; i++) {
				var o = BmsInt.box(BmsChx.toInt(c, i));
				assertSame(o, BmsInt.box(BmsChx.toInt(c, i)));
			}
		}
	}

	// box(int)
	// チャンネル番号1296以上を指定すると新しいインスタンスの整数値オブジェクトが返ること
	@Test
	public void testBoxInt_ChannelNumberOutOfRange() {
		var n = BmsChx.toInt(1296, 0);
		var o = BmsInt.box(n);
		assertNotSame(o, BmsInt.box(n));
		n = BmsChx.toInt(65535, 0);
		o = BmsInt.box(n);
		assertNotSame(o, BmsInt.box(n));
	}

	// box(int)
	// チャンネルインデックス16以上を指定すると新しいインスタンスの整数値オブジェクトが返ること
	@Test
	public void testBoxInt_ChannelIndexOutOfRange() {
		var n = BmsChx.toInt(1295, 15);
		var o = BmsInt.box(n);
		assertSame(o, BmsInt.box(n));
		n = BmsChx.toInt(1295, 16);
		o = BmsInt.box(n);
		assertNotSame(o, BmsInt.box(n));
		n = BmsChx.toInt(1295, 65535);
		o = BmsInt.box(n);
		assertNotSame(o, BmsInt.box(n));
	}

	// to16s(int)
	// 範囲内の数値を正常に変換できること
	@Test
	public void testTo16s_Ok() {
		var okPattern = Pattern.compile("^[0-9A-F]{2}$");
		for (var i = 0; i < 256; i++) {
			var s = BmsInt.to16s(i);
			assertEquals(i, Integer.parseInt(s, 16));
			assertTrue(okPattern.matcher(s).matches());
		}
	}

	// to16s(int)
	// IllegalArgumentException nが0未満
	@Test
	public void testTo16s_Underflow() {
		assertThrows(IllegalArgumentException.class, () -> BmsInt.to16s(-1));
	}

	// to16s(int)
	// IllegalArgumentException nが255超過
	@Test
	public void testTo16s_Overflow() {
		assertThrows(IllegalArgumentException.class, () -> BmsInt.to16s(256));
	}


	// to36s(int)
	// 範囲内の数値を正常に変換できること
	@Test
	public void testTo36s_Ok() {
		var okPattern = Pattern.compile("^[0-9A-Z]{2}$");
		for (var i = 0; i < 1296; i++) {
			var s = BmsInt.to36s(i);
			assertEquals(i, Integer.parseInt(s, 36));
			assertTrue(okPattern.matcher(s).matches());
		}
	}

	// to36s(int)
	// IllegalArgumentException nが0未満
	@Test
	public void testTo36s_Underflow() {
		assertThrows(IllegalArgumentException.class, () -> BmsInt.to36s(-1));
	}

	// to36s(int)
	// IllegalArgumentException nが1295超過
	@Test
	public void testTo36s_Overflow() {
		assertThrows(IllegalArgumentException.class, () -> BmsInt.to36s(1296));
	}

	// to62s(int)
	// 文字列を正常に変換できること
	@Test
	public void testTo62s_Ok() {
		var chs = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
		for (var i = 0; i < 3844; i++) {
			var c0 = i / 62;
			var c1 = i % 62;
			var s = BmsInt.to62s(i);
			assertEquals(2, s.length());
			assertEquals(chs.charAt(c0), s.charAt(0));
			assertEquals(chs.charAt(c1), s.charAt(1));
		}
	}

	// to62s(int)
	// IllegalArgumentException nが0未満
	@Test
	public void testTo62s_Underflow() {
		assertThrows(IllegalArgumentException.class, () -> BmsInt.to62s(-1));
	}

	// to62s(int)
	// IllegalArgumentException nが3843超過
	@Test
	public void testTo62s_Overflow() {
		assertThrows(IllegalArgumentException.class, () -> BmsInt.to62s(3844));
	}


	// to16i(String)
	// 文字列を正常に変換できること
	@Test
	public void testTo16i_Ok() {
		for (var i = 0; i < 256; i++) {
			var s1 = String.format("%02x", i);
			var n1 = BmsInt.to16i(s1);
			assertEquals(i, n1);
			var s2 = String.format("%02X", i);
			var n2 = BmsInt.to16i(s2);
			assertEquals(i, n2);
		}
	}

	// to16i(String)
	// NullPointerException sがnull
	@Test
	public void testTo16i_NullS() {
		assertThrows(NullPointerException.class, () -> BmsInt.to16i(null));
	}

	// to16i(String)
	// IllegalArgumentException sの長さが2以外
	@Test
	public void testTo16i_LengthNot2() {
		var e = IllegalArgumentException.class;
		assertThrows(e, () -> BmsInt.to16i(""));
		assertThrows(e, () -> BmsInt.to16i("1"));
		assertThrows(e, () -> BmsInt.to16i("123"));
	}

	// to16i(String)
	// IllegalArgumentException sに変換不可能な文字がある
	@Test
	public void testTo16i_IllegalS() {
		var e = IllegalArgumentException.class;
		assertThrows(e, () -> BmsInt.to16i("0G"));
		assertThrows(e, () -> BmsInt.to16i("*F"));
		assertThrows(e, () -> BmsInt.to16i("あい"));
	}

	// to36i(String)
	// 文字列を正常に変換できること
	@Test
	public void testTo36i_Ok() {
		for (var i = 0; i < 36; i++) {
			for (var j = 0; j < 36; j++) {
				var expect = (i * 36) + j;
				var s1 = Integer.toString(i, 36);
				var s2 = Integer.toString(j, 36);
				var actual1 = BmsInt.to36i((s1 + s2).toUpperCase());
				assertEquals(expect, actual1);
				var actual2 = BmsInt.to36i((s1 + s2).toLowerCase());
				assertEquals(expect, actual2);
			}
		}
	}

	// to36i(String)
	// NullPointerException sがnull
	@Test
	public void testTo36i_NullS() {
		assertThrows(NullPointerException.class, () -> BmsInt.to36i(null));
	}

	// to36i(String)
	// IllegalArgumentException sの長さが2以外
	@Test
	public void testTo36i_LengthNot2() {
		var e = IllegalArgumentException.class;
		assertThrows(e, () -> BmsInt.to36i(""));
		assertThrows(e, () -> BmsInt.to36i("X"));
		assertThrows(e, () -> BmsInt.to36i("XYZ"));
	}

	// to36i(String)
	// IllegalArgumentException sに変換不可能な文字がある
	@Test
	public void testTo36i_IllegalS() {
		var e = IllegalArgumentException.class;
		assertThrows(e, () -> BmsInt.to36i("0*"));
		assertThrows(e, () -> BmsInt.to36i("?Z"));
		assertThrows(e, () -> BmsInt.to36i("うえ"));
	}

	// to62i(String)
	// 文字列を正常に変換できること
	@Test
	public void testTo62i_Ok() {
		var chs = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
		var ca = new char[2];
		for (var i = 0; i < 62; i++) {
			for (var j = 0; j < 62; j++) {
				var expect = (i * 62) + j;
				ca[0] = chs.charAt(i);
				ca[1] = chs.charAt(j);
				assertEquals(expect, BmsInt.to62i(String.valueOf(ca)));
			}
		}
	}

	// to62i(String)
	// NullPointerException sがnull
	@Test
	public void testTo62i_NullS() {
		assertThrows(NullPointerException.class, () -> BmsInt.to62i(null));
	}

	// to62i(String)
	// IllegalArgumentException sの長さが2以外
	@Test
	public void testTo62i_LengthNot2() {
		var e = IllegalArgumentException.class;
		assertThrows(e, () -> BmsInt.to62i(""));
		assertThrows(e, () -> BmsInt.to62i("X"));
		assertThrows(e, () -> BmsInt.to62i("XYZ"));
	}

	// to62i(String)
	// IllegalArgumentException sに変換不可能な文字がある
	@Test
	public void testTo62i_IllegalS() {
		var e = IllegalArgumentException.class;
		assertThrows(e, () -> BmsInt.to62i("0?"));
		assertThrows(e, () -> BmsInt.to62i("#z"));
		assertThrows(e, () -> BmsInt.to62i("おか"));
	}

	// to16sa(int[], int, int)
	// 整数値配列を正常に変換できること
	@Test
	public void testTo16sa_Ok() {
		var expect = new StringBuilder(256 * 2);
		var source = new int[256];
		for (var i = 0; i < 256; i++) {
			expect.append(String.format("%02X", i));
			source[i] = i;
		}
		var actual = BmsInt.to16sa(source, 0, source.length);
		assertEquals(actual, expect.toString());
		assertEquals("020304", BmsInt.to16sa(source, 2, 3));
	}

	// to16sa(int[], int, int)
	// NullPointerException aがnull
	@Test
	public void testTo16sa_NullA() {
		assertThrows(NullPointerException.class, () -> BmsInt.to16sa(null, 0, 1));
	}

	// to16sa(int[], int, int)
	// IllegalArgumentException offsetが0未満
	@Test
	public void testTo16sa_MinusOffset() {
		var sa = new int[] { 0 };
		assertThrows(IllegalArgumentException.class, () -> BmsInt.to16sa(sa, -1, 1));
	}

	// to16sa(int[], int, int)
	// IllegalArgumentException countが0未満
	@Test
	public void testTo16sa_UnderflowCount() {
		var sa = new int[] { 0 };
		assertThrows(IllegalArgumentException.class, () -> BmsInt.to16sa(sa, 0, -1));
	}

	// to16sa(int[], int, int)
	// IndexOutOfBoundsException aの範囲外にアクセスした
	@Test
	public void testTo16sa_WithoutA() {
		var sa = new int[] { 0 };
		assertThrows(IndexOutOfBoundsException.class, () -> BmsInt.to16sa(sa, 0, 2));
	}

	// to16sa(int[], int, int)
	// IllegalArgumentException a内の変換対象に0未満の値がある
	@Test
	public void testTo16sa_AHasUnderflow() {
		var sa = new int[] { 0, 1, -1 };
		assertThrows(IllegalArgumentException.class, () -> BmsInt.to16sa(sa, 0, 3));
	}

	// to16sa(int[], int, int)
	// IllegalArgumentException a内の変換対象に255超過の値がある
	@Test
	public void testTo16sa_AHasOverflow() {
		var sa = new int[] { 0, 1, 256 };
		assertThrows(IllegalArgumentException.class, () -> BmsInt.to16sa(sa, 0, 3));
	}

	// to36sa(int[], int, int)
	// 整数値配列を正常に文字列へ変換できること
	@Test
	public void testTo36sa_Ok() {
		var expect = new StringBuilder(1296 * 2);
		var source = new int[1296];
		for (var i = 0; i < 36; i++) {
			for (var j = 0; j < 36; j++) {
				expect.append((Integer.toString(i, 36) + Integer.toString(j, 36)).toUpperCase());
				source[(i * 36) + j] = (i * 36) + j;
			}
		}
		var actual = BmsInt.to36sa(source, 0, source.length);
		assertEquals(actual, expect.toString());
		assertEquals("020304", BmsInt.to36sa(source, 2, 3));
	}

	// to36sa(int[], int, int)
	// NullPointerException aがnull
	@Test
	public void testTo36sa_NullA() {
		assertThrows(NullPointerException.class, () -> BmsInt.to36sa(null, 0, 1));
	}

	// to36sa(int[], int, int)
	// IllegalArgumentException offsetが0未満
	@Test
	public void testTo36sa_MinusOffset() {
		var sa = new int[] { 0 };
		assertThrows(IllegalArgumentException.class, () -> BmsInt.to36sa(sa, -1, 1));
	}

	// to36sa(int[], int, int)
	// IllegalArgumentException countが0未満
	@Test
	public void testTo36sa_UnderflowCount() {
		var sa = new int[] { 0 };
		assertThrows(IllegalArgumentException.class, () -> BmsInt.to36sa(sa, 0, -1));
	}

	// to36sa(int[], int, int)
	// IndexOutOfBoundsException aの範囲外にアクセスした
	@Test
	public void testTo36sa_WithoutA() {
		var sa = new int[] { 0 };
		assertThrows(IndexOutOfBoundsException.class, () -> BmsInt.to36sa(sa, 0, 2));
	}

	// to36sa(int[], int, int)
	// IllegalArgumentException a内の変換対象に0未満の値がある
	@Test
	public void testTo36sa_AHasUnderflow() {
		var sa = new int[] { 0, 1, -1 };
		assertThrows(IllegalArgumentException.class, () -> BmsInt.to36sa(sa, 0, 3));
	}

	// to36sa(int[], int, int)
	// IllegalArgumentException a内の変換対象に1295超過の値がある
	@Test
	public void testTo36sa_AHasOverflow() {
		var sa = new int[] { 0, 1, 1296 };
		assertThrows(IllegalArgumentException.class, () -> BmsInt.to36sa(sa, 0, 3));
	}

	// to62sa(int[], int, int)
	// 整数値配列を正常に文字列へ変換できること
	@Test
	public void testTo62sa_Ok() {
		var chs = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
		var ca = new char[2];
		var expect = new StringBuilder(3844 * 2);
		var source = new int[3844];
		for (var i = 0; i < 62; i++) {
			for (var j = 0; j < 62; j++) {
				ca[0] = chs.charAt(i);
				ca[1] = chs.charAt(j);
				expect.append(String.valueOf(ca));
				source[(i * 62) + j] = (i * 62) + j;
			}
		}
		var actual = BmsInt.to62sa(source, 0, source.length);
		assertEquals(actual, expect.toString());
		assertEquals("0a0b0c", BmsInt.to62sa(source, 36, 3));
	}

	// to62sa(int[], int, int)
	// NullPointerException aがnull
	@Test
	public void testTo62sa_NullA() {
		assertThrows(NullPointerException.class, () -> BmsInt.to62sa(null, 0, 1));
	}

	// to62sa(int[], int, int)
	// IllegalArgumentException offsetが0未満
	@Test
	public void testTo62sa_MinusOffset() {
		var sa = new int[] { 0 };
		assertThrows(IllegalArgumentException.class, () -> BmsInt.to62sa(sa, -1, 1));
	}

	// to62sa(int[], int, int)
	// IllegalArgumentException countが0未満
	@Test
	public void testTo62sa_UnderflowCount() {
		var sa = new int[] { 0 };
		assertThrows(IllegalArgumentException.class, () -> BmsInt.to62sa(sa, 0, -1));
	}

	// to62sa(int[], int, int)
	// IndexOutOfBoundsException aの範囲外にアクセスした
	@Test
	public void testTo62sa_WithoutA() {
		var sa = new int[] { 0 };
		assertThrows(IndexOutOfBoundsException.class, () -> BmsInt.to62sa(sa, 0, 2));
	}

	// to62sa(int[], int, int)
	// IllegalArgumentException a内の変換対象に0未満の値がある
	@Test
	public void testTo62sa_AHasUnderflow() {
		var sa = new int[] { 0, 1, -1 };
		assertThrows(IllegalArgumentException.class, () -> BmsInt.to62sa(sa, 0, 3));
	}

	// to62sa(int[], int, int)
	// IllegalArgumentException a内の変換対象に3843超過の値がある
	@Test
	public void testTo62sa_AHasOverflow() {
		var sa = new int[] { 0, 1, 3844 };
		assertThrows(IllegalArgumentException.class, () -> BmsInt.to62sa(sa, 0, 3));
	}

	// to16ia(String, int, int)
	// 文字列を正常に整数値配列へ変換できること
	@Test
	public void testTo16ia_Ok() {
		var expect = new int[256];
		var source1 = new StringBuilder(256 * 2);
		var source2 = new StringBuilder(256 * 2);
		for (var i = 0; i < 256; i++) {
			expect[i] = i;
			source1.append(String.format("%02x", i));
			source2.append(String.format("%02X", i));
		}
		var actual1 = BmsInt.to16ia(source1.toString(), 0, expect.length);
		assertArrayEquals(expect, actual1);
		var actual2 = BmsInt.to16ia(source2.toString(), 0, expect.length);
		assertArrayEquals(expect, actual2);
		var part = BmsInt.to16ia(source1.toString(), 6, 3);
		assertEquals(3, part.length);
		assertEquals(3, part[0]);
		assertEquals(4, part[1]);
		assertEquals(5, part[2]);
	}

	// to16ia(String, int, int)
	// NullPointerException aがnull
	@Test
	public void testTo16ia_NullA() {
		assertThrows(NullPointerException.class, () -> BmsInt.to16ia(null, 0, 1));
	}

	// to16ia(String, int, int)
	// IllegalArgumentException offsetが0未満
	@Test
	public void testTo16ia_MinusOffset() {
		assertThrows(IllegalArgumentException.class, () -> BmsInt.to16ia("000102", -1, 1));
	}

	// to16ia(String, int, int)
	// IllegalArgumentException countが0未満
	@Test
	public void testTo16ia_UnderflowCount() {
		assertThrows(IllegalArgumentException.class, () -> BmsInt.to16ia("000102", 0, -1));
	}

	// to16ia(String, int, int)
	// IndexOutOfBoundsException sの範囲外にアクセスした
	@Test
	public void testTo16ia_WithoutS() {
		assertThrows(IndexOutOfBoundsException.class, () -> BmsInt.to16ia("000102", 1, 3));
	}

	// to16ia(String, int, int)
	// IllegalArgumentException sに変換不可能な文字がある
	@Test
	public void testTo16ia_SHasInvalidChar() {
		var e = IllegalArgumentException.class;
		assertThrows(e, () -> BmsInt.to16ia("00010G", 0, 3));
		assertThrows(e, () -> BmsInt.to16ia("0001*0", 0, 3));
		assertThrows(e, () -> BmsInt.to16ia("0001  ", 0, 3));
	}

	// to36ia(String, int, int)
	// 文字列を正常に整数値配列へ変換できること
	@Test
	public void testTo36ia_Ok() {
		var expect = new int[1296];
		var source1 = new StringBuilder(1296 * 2);
		var source2 = new StringBuilder(1296 * 2);
		for (var i = 0; i < 36; i++) {
			for (var j = 0; j < 36; j++) {
				expect[(i * 36) + j] = (i * 36) + j;
				source1.append((Integer.toString(i, 36) + Integer.toString(j, 36)).toLowerCase());
				source2.append((Integer.toString(i, 36) + Integer.toString(j, 36)).toUpperCase());
			}
		}
		var actual1 = BmsInt.to36ia(source1.toString(), 0, expect.length);
		assertArrayEquals(expect, actual1);
		var actual2 = BmsInt.to36ia(source2.toString(), 0, expect.length);
		assertArrayEquals(expect, actual2);
		var part = BmsInt.to36ia(source1.toString(), 6, 3);
		assertEquals(3, part.length);
		assertEquals(3, part[0]);
		assertEquals(4, part[1]);
		assertEquals(5, part[2]);
	}

	// to36ia(String, int, int)
	// NullPointerException aがnull
	@Test
	public void testTo36ia_NullA() {
		assertThrows(NullPointerException.class, () -> BmsInt.to36ia(null, 0, 1));
	}

	// to36ia(String, int, int)
	// IllegalArgumentException offsetが0未満
	@Test
	public void testTo36ia_MinusOffset() {
		assertThrows(IllegalArgumentException.class, () -> BmsInt.to36ia("000102", -1, 1));
	}

	// to36ia(String, int, int)
	// IllegalArgumentException countが0未満
	@Test
	public void testTo36ia_UnderflowCount() {
		assertThrows(IllegalArgumentException.class, () -> BmsInt.to36ia("000102", 0, -1));
	}

	// to36ia(String, int, int)
	// IndexOutOfBoundsException sの範囲外にアクセスした
	@Test
	public void testTo36ia_WithoutS() {
		assertThrows(IndexOutOfBoundsException.class, () -> BmsInt.to36ia("000102", 1, 3));
	}

	// to36ia(String, int, int)
	// IllegalArgumentException sに変換不可能な文字がある
	@Test
	public void testTo36ia_SHasInvalidChar() {
		var e = IllegalArgumentException.class;
		assertThrows(e, () -> BmsInt.to36ia("00010!", 0, 3));
		assertThrows(e, () -> BmsInt.to36ia("0001*0", 0, 3));
		assertThrows(e, () -> BmsInt.to36ia("0001  ", 0, 3));
	}

	// to62ia(String, int, int)
	// 文字列を正常に整数値配列へ変換できること
	@Test
	public void testTo62ia_Ok() {
		var chs = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
		var expect = new int[3844];
		var source = new StringBuilder(3844 * 2);
		for (var i = 0; i < 62; i++) {
			for (var j = 0; j < 62; j++) {
				expect[(i * 62) + j] = (i * 62) + j;
				source.append(chs.charAt(i)).append(chs.charAt(j));
			}
		}
		var actual = BmsInt.to62ia(source.toString(), 0, expect.length);
		assertArrayEquals(expect, actual);
		var part = BmsInt.to62ia(source.toString(), 72, 3);
		assertEquals(3, part.length);
		assertEquals(36, part[0]);
		assertEquals(37, part[1]);
		assertEquals(38, part[2]);
	}

	// to62ia(String, int, int)
	// NullPointerException aがnull
	@Test
	public void testTo62ia_NullA() {
		assertThrows(NullPointerException.class, () -> BmsInt.to62ia(null, 0, 1));
	}

	// to62ia(String, int, int)
	// IllegalArgumentException offsetが0未満
	@Test
	public void testTo62ia_MinusOffset() {
		assertThrows(IllegalArgumentException.class, () -> BmsInt.to62ia("000102", -1, 1));
	}

	// to62ia(String, int, int)
	// IllegalArgumentException countが0未満
	@Test
	public void testTo62ia_UnderflowCount() {
		assertThrows(IllegalArgumentException.class, () -> BmsInt.to62ia("000102", 0, -1));
	}

	// to62ia(String, int, int)
	// IndexOutOfBoundsException sの範囲外にアクセスした
	@Test
	public void testTo62ia_WithoutS() {
		assertThrows(IndexOutOfBoundsException.class, () -> BmsInt.to62ia("000102", 1, 3));
	}

	// to62ia(String, int, int)
	// IllegalArgumentException sに変換不可能な文字がある
	@Test
	public void testTo62ia_SHasInvalidChar() {
		var e = IllegalArgumentException.class;
		assertThrows(e, () -> BmsInt.to62ia("00010!", 0, 3));
		assertThrows(e, () -> BmsInt.to62ia("0001*0", 0, 3));
		assertThrows(e, () -> BmsInt.to62ia("0001  ", 0, 3));
	}
}
