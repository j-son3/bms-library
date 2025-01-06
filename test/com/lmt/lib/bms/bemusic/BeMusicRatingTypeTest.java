package com.lmt.lib.bms.bemusic;

import static org.junit.Assert.*;

import org.junit.Test;

public class BeMusicRatingTypeTest {
	// getIndex()
	// 期待するインデックスが取得できること
	@Test
	public void testGetIndex() {
		assertEquals(0, BeMusicRatingType.DELTA.getIndex());
		assertEquals(1, BeMusicRatingType.COMPLEX.getIndex());
		assertEquals(2, BeMusicRatingType.POWER.getIndex());
		assertEquals(3, BeMusicRatingType.RHYTHM.getIndex());
		assertEquals(4, BeMusicRatingType.SCRATCH.getIndex());
		assertEquals(5, BeMusicRatingType.HOLDING.getIndex());
		assertEquals(6, BeMusicRatingType.GIMMICK.getIndex());
	}

	// getMax()
	// 期待するレーティング値の最大値が取得できること
	@Test
	public void testGetMax() {
		assertEquals(BeMusicRatings.DELTA_MAX, BeMusicRatingType.DELTA.getMax());
		assertEquals(BeMusicRatings.TENDENCY_MAX, BeMusicRatingType.COMPLEX.getMax());
		assertEquals(BeMusicRatings.TENDENCY_MAX, BeMusicRatingType.POWER.getMax());
		assertEquals(BeMusicRatings.TENDENCY_MAX, BeMusicRatingType.RHYTHM.getMax());
		assertEquals(BeMusicRatings.TENDENCY_MAX, BeMusicRatingType.SCRATCH.getMax());
		assertEquals(BeMusicRatings.TENDENCY_MAX, BeMusicRatingType.HOLDING.getMax());
		assertEquals(BeMusicRatings.TENDENCY_MAX, BeMusicRatingType.GIMMICK.getMax());
	}

	// isValid()
	// DELTA:有効範囲内のレーティング値を指定するとtrueを返すこと
	@Test
	public void testIsValid_DeltaValid() {
		assertTrue(BeMusicRatingType.DELTA.isValid(BeMusicRatings.DELTA_ZERO));
		assertTrue(BeMusicRatingType.DELTA.isValid(BeMusicRatings.DELTA_MAX / 2));
		assertTrue(BeMusicRatingType.DELTA.isValid(BeMusicRatings.DELTA_MAX));
	}

	// isValid()
	// DELTA:有効範囲外のレーティング値を指定するとfalseを返すこと
	@Test
	public void testIsValid_DeltaInvalid() {
		assertFalse(BeMusicRatingType.DELTA.isValid(BeMusicRatings.DELTA_ZERO - 1));
		assertFalse(BeMusicRatingType.DELTA.isValid(BeMusicRatings.DELTA_MAX + 1));
	}

	// isValid()
	// 譜面傾向:有効範囲内のレーティング値を指定するとtrueを返すこと
	@Test
	public void testIsValid_TendencyValid() {
		for (var tendency : BeMusicRatingType.tendencies()) {
			assertTrue(tendency.isValid(0));
			assertTrue(tendency.isValid(BeMusicRatings.TENDENCY_MAX / 2));
			assertTrue(tendency.isValid(BeMusicRatings.TENDENCY_MAX));
		}
	}

	// isValid()
	// 譜面傾向:有効範囲外のレーティング値を指定するとfalseを返すこと
	@Test
	public void testIsValid_TendencyInvalid() {
		for (var tendency : BeMusicRatingType.tendencies()) {
			assertFalse(tendency.isValid(-1));
			assertFalse(tendency.isValid(BeMusicRatings.TENDENCY_MAX + 1));
		}
	}

	// isUnknown()
	// DELTA:有効範囲内のレーティング値を指定するとfalseを返すこと
	@Test
	public void testIsUnknown_DeltaValid() {
		assertFalse(BeMusicRatingType.DELTA.isUnknown(BeMusicRatings.DELTA_ZERO));
		assertFalse(BeMusicRatingType.DELTA.isUnknown(BeMusicRatings.DELTA_MAX / 2));
		assertFalse(BeMusicRatingType.DELTA.isUnknown(BeMusicRatings.DELTA_MAX));
	}

	// isUnknown()
	// DELTA:有効範囲外のレーティング値を指定するとtrueを返すこと
	@Test
	public void testIsUnknown_DeltaInvalid() {
		assertTrue(BeMusicRatingType.DELTA.isUnknown(BeMusicRatings.DELTA_ZERO - 1));
		assertTrue(BeMusicRatingType.DELTA.isUnknown(BeMusicRatings.DELTA_MAX + 1));
	}

	// isUnknown()
	// 譜面傾向:有効範囲内のレーティング値を指定するとfalseを返すこと
	@Test
	public void testIsUnknown_TendencyValid() {
		for (var tendency : BeMusicRatingType.tendencies()) {
			assertFalse(tendency.isUnknown(0));
			assertFalse(tendency.isUnknown(BeMusicRatings.TENDENCY_MAX / 2));
			assertFalse(tendency.isUnknown(BeMusicRatings.TENDENCY_MAX));
		}
	}

	// isUnknown()
	// 譜面傾向:有効範囲外のレーティング値を指定するとtrueを返すこと
	@Test
	public void testIsUnknown_TendencyInvalid() {
		for (var tendency : BeMusicRatingType.tendencies()) {
			assertTrue(tendency.isUnknown(-1));
			assertTrue(tendency.isUnknown(BeMusicRatings.TENDENCY_MAX + 1));
		}
	}

	// isTendency()
	// 譜面傾向のレーティング種別はtrue、それ以外はfalseを返すこと
	@Test
	public void testIsTendency() {
		assertFalse(BeMusicRatingType.DELTA.isTendency());
		assertTrue(BeMusicRatingType.COMPLEX.isTendency());
		assertTrue(BeMusicRatingType.POWER.isTendency());
		assertTrue(BeMusicRatingType.RHYTHM.isTendency());
		assertTrue(BeMusicRatingType.SCRATCH.isTendency());
		assertTrue(BeMusicRatingType.HOLDING.isTendency());
		assertTrue(BeMusicRatingType.GIMMICK.isTendency());
	}

	// toValue(int)
	// DELTA:範囲内のレーティング値が期待する値に変換されること
	@Test
	public void testToValue_DeltaValid() {
		assertEquals(BeMusicRatings.DELTA_ZERO / 100.0, BeMusicRatingType.DELTA.toValue(BeMusicRatings.DELTA_ZERO), 0.00000001);
		assertEquals(8.00, BeMusicRatingType.DELTA.toValue(800), 0.00000001);
		assertEquals(12.34, BeMusicRatingType.DELTA.toValue(1234), 0.0000001);
		assertEquals(BeMusicRatings.DELTA_MAX / 100.0, BeMusicRatingType.DELTA.toValue(BeMusicRatings.DELTA_MAX), 0.00000001);
	}

	// toValue(int)
	// DELTA:範囲外のレーティング値が範囲内に丸め込まれること
	@Test
	public void testToValue_DeltaInvalid() {
		assertEquals(BeMusicRatings.DELTA_ZERO / 100.0, BeMusicRatingType.DELTA.toValue(BeMusicRatings.DELTA_ZERO - 1), 0.00000001);
		assertEquals(BeMusicRatings.DELTA_MAX / 100.0, BeMusicRatingType.DELTA.toValue(BeMusicRatings.DELTA_MAX + 1), 0.00000001);
	}

	// toValue(int)
	// 譜面傾向:範囲内のレーティング値が期待する値に変換されること
	@Test
	public void testToValue_TendencyValid() {
		for (var tendency : BeMusicRatingType.tendencies()) {
			assertEquals(0.00, tendency.toValue(0), 0.00000001);
			assertEquals(99.00, tendency.toValue(9900), 0.00000001);
			assertEquals(135.79, tendency.toValue(13579), 0.00000001);
			assertEquals(BeMusicRatings.TENDENCY_MAX / 100.0, tendency.toValue(BeMusicRatings.TENDENCY_MAX), 0.00000001);
		}
	}

	// toValue(int)
	// 譜面傾向:範囲外のレーティング値が範囲内に丸め込まれること
	@Test
	public void testToValue_TendencyInvalid() {
		for (var tendency : BeMusicRatingType.tendencies()) {
			assertEquals(0.00, tendency.toValue(-1), 0.00000001);
			assertEquals(BeMusicRatings.TENDENCY_MAX / 100.0, tendency.toValue(BeMusicRatings.TENDENCY_MAX + 1), 0.00000001);
		}
	}

	// toString(int)
	// DELTA:最小値が"ZERO"になること
	@Test
	public void tesstToString_DeltaMin() {
		assertEquals("ZERO", BeMusicRatingType.DELTA.toString(BeMusicRatings.DELTA_ZERO));
	}

	// toString(int)
	// DELTA:最大値が"MAX"になること
	@Test
	public void tesstToString_DeltaMax() {
		assertEquals("MAX", BeMusicRatingType.DELTA.toString(BeMusicRatings.DELTA_MAX));
	}

	// toString(int)
	// DELTA:範囲内のレーティング値が期待する形式に変換されること
	@Test
	public void tesstToString_DeltaValid() {
		assertEquals("0.01", BeMusicRatingType.DELTA.toString(BeMusicRatings.DELTA_ZERO + 1));
		assertEquals("5.00", BeMusicRatingType.DELTA.toString(500));
		assertEquals("14.79", BeMusicRatingType.DELTA.toString(1479));
		assertEquals("15.99", BeMusicRatingType.DELTA.toString(BeMusicRatings.DELTA_MAX - 1));
	}

	// toString(int)
	// DELTA:範囲外のレーティング値"UNKNOWN"になること
	@Test
	public void tesstToString_DeltaInvalid() {
		assertEquals("UNKNOWN", BeMusicRatingType.DELTA.toString(BeMusicRatings.DELTA_ZERO - 1));
		assertEquals("UNKNOWN", BeMusicRatingType.DELTA.toString(BeMusicRatings.DELTA_MAX + 1));
	}

	// toString(int)
	// 譜面傾向:範囲内のレーティング値が期待する形式に変換されること
	@Test
	public void tesstToString_TendencyValid() {
		for (var tendency : BeMusicRatingType.tendencies()) {
			assertEquals("0.00", tendency.toString(0));
			assertEquals("0.01", tendency.toString(1));
			assertEquals("55.00", tendency.toString(5500));
			assertEquals("123.45", tendency.toString(12345));
			assertEquals("199.99", tendency.toString(BeMusicRatings.TENDENCY_MAX - 1));
			assertEquals("200.00", tendency.toString(BeMusicRatings.TENDENCY_MAX));
		}
	}

	// toString(int)
	// 譜面傾向:範囲外のレーティング値"UNKNOWN"になること
	@Test
	public void tesstToString_TendencyInvalid() {
		for (var tendency : BeMusicRatingType.tendencies()) {
			assertEquals("UNKNOWN", tendency.toString(-1));
			assertEquals("UNKNOWN", tendency.toString(BeMusicRatings.TENDENCY_MAX + 1));
		}
	}

	// fromIndex(int)
	// 期待する種別を返すこと
	@Test
	public void testFromIndex() {
		assertEquals(BeMusicRatingType.DELTA, BeMusicRatingType.fromIndex(0));
		assertEquals(BeMusicRatingType.COMPLEX, BeMusicRatingType.fromIndex(1));
		assertEquals(BeMusicRatingType.POWER, BeMusicRatingType.fromIndex(2));
		assertEquals(BeMusicRatingType.RHYTHM, BeMusicRatingType.fromIndex(3));
		assertEquals(BeMusicRatingType.SCRATCH, BeMusicRatingType.fromIndex(4));
		assertEquals(BeMusicRatingType.HOLDING, BeMusicRatingType.fromIndex(5));
		assertEquals(BeMusicRatingType.GIMMICK, BeMusicRatingType.fromIndex(6));
	}

	// tendencies()
	// 期待する種別が全て格納されていること
	@Test
	public void testTendencies_Normal() {
		var tendencies = BeMusicRatingType.tendencies();
		assertEquals(6, tendencies.size());
		assertTrue(tendencies.contains(BeMusicRatingType.COMPLEX));
		assertTrue(tendencies.contains(BeMusicRatingType.POWER));
		assertTrue(tendencies.contains(BeMusicRatingType.RHYTHM));
		assertTrue(tendencies.contains(BeMusicRatingType.SCRATCH));
		assertTrue(tendencies.contains(BeMusicRatingType.HOLDING));
		assertTrue(tendencies.contains(BeMusicRatingType.GIMMICK));
	}

	// tendencies()
	// 返されたリストが読み取り専用で変更不可であること
	@Test
	public void testTendencies_ReadOnly() {
		var tendencies = BeMusicRatingType.tendencies();
		assertThrows(UnsupportedOperationException.class, () -> tendencies.set(0, BeMusicRatingType.COMPLEX));
		assertThrows(UnsupportedOperationException.class, () -> tendencies.add(BeMusicRatingType.COMPLEX));
		assertThrows(UnsupportedOperationException.class, () -> tendencies.clear());
	}
}
