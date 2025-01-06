package com.lmt.lib.bms.bemusic;

import static org.junit.Assert.*;

import org.junit.Test;

public class BeMusicRatingsTest {
	// tendencyAsDouble(int)
	// レーティング値を100で割ったdouble型の値が返されること
	@Test
	public void testTendencyAsDouble_Normal() {
		assertEquals(50.23, BeMusicRatings.tendencyAsDouble(5023), 0.00);
		assertEquals(134.78, BeMusicRatings.tendencyAsDouble(13478), 0.00);
		assertEquals(BeMusicRatings.TENDENCY_MIN / 100.0, BeMusicRatings.tendencyAsDouble(BeMusicRatings.TENDENCY_MIN), 0.00);
		assertEquals(BeMusicRatings.TENDENCY_MAX / 100.0, BeMusicRatings.tendencyAsDouble(BeMusicRatings.TENDENCY_MAX), 0.00);
	}

	// tendencyAsDouble(int)
	// 最小値を下回る場合、最小値が返されること
	@Test
	public void testTendencyAsDouble_Underflow() {
		assertEquals(BeMusicRatings.TENDENCY_MIN / 100.0, BeMusicRatings.tendencyAsDouble(BeMusicRatings.TENDENCY_MIN - 1), 0.00);
	}

	// tendencyAsDouble(int)
	// 最大値を上回る場合、最大値が返されること
	@Test
	public void testTendencyAsDouble_Overflow() {
		assertEquals(BeMusicRatings.TENDENCY_MAX / 100.0, BeMusicRatings.tendencyAsDouble(BeMusicRatings.TENDENCY_MAX + 1), 0.00);
	}

	// tendencyAsString(int, boolean)
	// 有効範囲内のレーティング値を小数点第2位までの数値文字列で返すこと
	@Test
	public void testTendencyAsString_Normal() {
		assertEquals("50.23", BeMusicRatings.tendencyAsString(5023, false));
		assertEquals("134.78", BeMusicRatings.tendencyAsString(13478, false));
		assertEquals("8.31", BeMusicRatings.tendencyAsString(831, true));
		assertEquals("159.33", BeMusicRatings.tendencyAsString(15933, true));
		assertEquals("0.00", BeMusicRatings.tendencyAsString(BeMusicRatings.TENDENCY_MIN, false));
		assertEquals("0.00", BeMusicRatings.tendencyAsString(BeMusicRatings.TENDENCY_MIN, true));
		assertEquals("200.00", BeMusicRatings.tendencyAsString(BeMusicRatings.TENDENCY_MAX, false));
		assertEquals("200.00", BeMusicRatings.tendencyAsString(BeMusicRatings.TENDENCY_MAX, true));
	}

	// tendencyAsString(int, boolean)
	// moreStringify=falseで最小値未満を指定すると最小値の小数点第2位までの数値文字列で返すこと
	@Test
	public void testTendencyAsString_Underflow() {
		assertEquals("0.00", BeMusicRatings.tendencyAsString(BeMusicRatings.TENDENCY_MIN - 1, false));
	}

	// tendencyAsString(int, boolean)
	// moreStringify=falseで最大値超過を指定すると最大値の小数点第2位までの数値文字列で返すこと
	@Test
	public void testTendencyAsString_Overflow() {
		assertEquals("200.00", BeMusicRatings.tendencyAsString(BeMusicRatings.TENDENCY_MAX + 1, false));
	}

	// tendencyAsString(int, boolean)
	// moreStringify=trueで最小値未満を指定すると「UNKNOWN」を返すこと
	@Test
	public void testTendencyAsString_Underflow_Unknown() {
		assertEquals("UNKNOWN", BeMusicRatings.tendencyAsString(BeMusicRatings.TENDENCY_MIN - 1, true));
	}

	// tendencyAsString(int, boolean)
	// moreStringify=trueで最大値超過を指定すると「UNKNOWN」を返すこと
	@Test
	public void testTendencyAsString_Overflow_Unknown() {
		assertEquals("UNKNOWN", BeMusicRatings.tendencyAsString(BeMusicRatings.TENDENCY_MAX + 1, true));
	}

	// deltaAsDouble(int)
	// レーティング値を100で割ったdouble型の値が返されること
	@Test
	public void testDeltaAsDouble_Normal() {
		assertEquals(4.27, BeMusicRatings.deltaAsDouble(427), 0.00);
		assertEquals(13.28, BeMusicRatings.deltaAsDouble(1328), 0.00);
		assertEquals(BeMusicRatings.DELTA_ZERO / 100.0, BeMusicRatings.deltaAsDouble(BeMusicRatings.DELTA_ZERO), 0.00);
		assertEquals(BeMusicRatings.DELTA_MAX / 100.0, BeMusicRatings.deltaAsDouble(BeMusicRatings.DELTA_MAX), 0.00);
	}

	// deltaAsDouble(int)
	// 最小値を下回る場合、最小値が返されること
	@Test
	public void testDeltaAsDouble_Underflow() {
		assertEquals(BeMusicRatings.DELTA_ZERO / 100.0, BeMusicRatings.deltaAsDouble(BeMusicRatings.DELTA_ZERO - 1), 0.00);
	}

	// deltaAsDouble(int)
	// 最大値を上回る場合、最大値が返されること
	@Test
	public void testDeltaAsDouble_Overflow() {
		assertEquals(BeMusicRatings.DELTA_MAX / 100.0, BeMusicRatings.deltaAsDouble(BeMusicRatings.DELTA_MAX + 1), 0.00);
	}

	// deltaAsString(int, boolean)
	// 有効範囲内のレーティング値を小数点第2位までの数値文字列で返すこと
	@Test
	public void testDeltaAsString_Normal() {
		assertEquals("4.27", BeMusicRatings.deltaAsString(427, false));
		assertEquals("13.28", BeMusicRatings.deltaAsString(1328, false));
		assertEquals("2.93", BeMusicRatings.deltaAsString(293, true));
		assertEquals("15.42", BeMusicRatings.deltaAsString(1542, true));
		assertEquals("0.00", BeMusicRatings.deltaAsString(BeMusicRatings.DELTA_ZERO, false));
		assertEquals("16.00", BeMusicRatings.deltaAsString(BeMusicRatings.DELTA_MAX, false));
	}

	// deltaAsString(int, boolean)
	// moreStringify=falseで最小値未満を指定すると最小値の小数点第2位までの数値文字列で返すこと
	@Test
	public void testDeltaAsString_Underflow() {
		assertEquals("0.00", BeMusicRatings.deltaAsString(BeMusicRatings.DELTA_ZERO - 1, false));
	}

	// deltaAsString(int, boolean)
	// moreStringify=falseで最大値超過を指定すると最大値の小数点第2位までの数値文字列で返すこと
	@Test
	public void testDeltaAsString_Overflow() {
		assertEquals("16.00", BeMusicRatings.deltaAsString(BeMusicRatings.DELTA_MAX + 1, false));
	}

	// deltaAsString(int, boolean)
	// moreStringify=trueで最小値未満を指定すると「UNKNOWN」を返すこと
	@Test
	public void testDeltaAsString_Underflow_Unknown() {
		assertEquals("UNKNOWN", BeMusicRatings.deltaAsString(BeMusicRatings.DELTA_ZERO - 1, true));
	}

	// deltaAsString(int, boolean)
	// moreStringify=trueで最小値を指定すると「ZERO」を返すこと
	@Test
	public void testDeltaAsString_Zero() {
		assertEquals("ZERO", BeMusicRatings.deltaAsString(BeMusicRatings.DELTA_ZERO, true));
	}

	// deltaAsString(int, boolean)
	// moreStringify=trueで最大値を指定すると「MAX」を返すこと
	@Test
	public void testDeltaAsString_Max() {
		assertEquals("MAX", BeMusicRatings.deltaAsString(BeMusicRatings.DELTA_MAX, true));
	}

	// deltaAsString(int, boolean)
	// moreStringify=trueで最大値超過を指定すると「UNKNOWN」を返すこと
	@Test
	public void testDeltaAsString_Overflow_Unknown() {
		assertEquals("UNKNOWN", BeMusicRatings.deltaAsString(BeMusicRatings.DELTA_MAX + 1, true));
	}
}
