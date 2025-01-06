package com.lmt.lib.bms;

import static org.junit.Assert.*;

import org.junit.Test;

public class BmsChannelTest {
	@FunctionalInterface
	private interface ChannelCreator {
		BmsChannel create(int num, BmsType type, String ref, String def, boolean multi, boolean unique);
	}

	private static final ChannelCreator FN_USER = (a1, a2, a3, a4, a5, a6) -> BmsChannel.user(a1, a2, a3, a4, a5);

	// BmsChannel(int, BmsType, String, String, boolean, boolean)
	// 正常
	@Test
	public void testBmsChannel_Normal() {
		testNormal(1, 100, BmsSpec.USER_CHANNEL_MIN, true, BmsChannel::new);
	}

	// BmsChannel(int, BmsType, String, String, boolean, boolean)
	// NullPointerException typeがnull
	@Test
	public void testBmsChannel_NullType() {
		var ec = NullPointerException.class;
		assertThrows(ec, () -> new BmsChannel(BmsSpec.SPEC_CHANNEL_MIN, null, null, "0", false, false));
	}

	// BmsChannel(int, BmsType, String, String, boolean, boolean)
	// NullPointerException typeがOBJECT以外の時、defaultValueがnull
	@Test
	public void testBmsChannel_NonObjectTypeAndNullDefaultValue() {
		testNullDefaultValue(BmsSpec.SPEC_CHANNEL_MIN, BmsChannel::new);
	}

	// BmsChannel(int, BmsType, String, String, boolean, boolean)
	// IllegalArgumentException numberに登録できないチャンネル番号を指定した
	@Test
	public void testBmsChannel_NumberOutOfRange() {
		var ec = IllegalArgumentException.class;
		assertThrows(ec, () -> new BmsChannel(BmsSpec.CHANNEL_MIN - 1, BmsType.INTEGER, "#meta", "0", false, false));
		assertThrows(ec, () -> new BmsChannel(BmsSpec.CHANNEL_MAX + 1, BmsType.INTEGER, "#meta", "0", false, false));
	}

	// BmsChannel(int, BmsType, String, String, boolean, boolean)
	// IllegalArgumentException データ型が値型で参照先メタ情報を指定した
	@Test
	public void testBmsChannel_ValueTypeAndNonNullRef() {
		testValueTypeAndNonNullRef(BmsSpec.SPEC_CHANNEL_MIN, BmsChannel::new);
	}

	// BmsChannel(int, BmsType, String, String, boolean, boolean)
	// IllegalArgumentException typeがOBJECT以外の時、defaultValueがtypeのテストに失敗した
	@Test
	public void testBmsChannel_NonObjectTypeAndInvalidDefaultValue() {
		testInvalidDefaultValue(BmsSpec.SPEC_CHANNEL_MIN, BmsChannel::new);
	}

	// BmsChannel(int, BmsType, String, String, boolean, boolean)
	// IllegalArgumentException typeがOBJECTの時、defaultValueがnullではない
	@Test
	public void testBmsChannel_ObjectTypeAndNonNullDefaultValue() {
		var ec = IllegalArgumentException.class;
		assertThrows(ec, () -> new BmsChannel(BmsSpec.USER_CHANNEL_MIN, BmsType.OBJECT, null, "", false, false));
	}

	// BmsChannel(int, BmsType, String, String, boolean, boolean)
	// IllegalArgumentException typeがOBJECTの時、refがnullではない
	@Test
	public void testBmsChannel_ObjectTypeAndNonNullRef() {
		var ec = IllegalArgumentException.class;
		assertThrows(ec, () -> new BmsChannel(BmsSpec.USER_CHANNEL_MIN, BmsType.OBJECT, "#meta", null, false, false));
	}

	// BmsChannel(int, BmsType, String, String, boolean, boolean)
	// IllegalArgumentException typeがOBJECTの時、仕様チャンネルを指定した
	@Test
	public void testBmsChannel_ObjectTypeAndSpecChannel() {
		var ec = IllegalArgumentException.class;
		assertThrows(ec, () -> new BmsChannel(BmsSpec.SPEC_CHANNEL_MIN, BmsType.OBJECT, null, null, false, false));
	}

	// BmsChannel(int, BmsType, String, String, boolean, boolean)
	// IllegalArgumentException ユーザーチャンネルでuniquenessをtrueにしようとした
	@Test
	public void testBmsChannel_UserChannelAndUniqueness() {
		var ec = IllegalArgumentException.class;
		assertThrows(ec, () -> new BmsChannel(BmsSpec.USER_CHANNEL_MIN, BmsType.OBJECT, null, null, false, true));
	}

	// spec(int, BmsType, String, String, boolean, boolean)
	// 正しいパラメータを指定したチャンネルオブジェクトが構築できること(正常ケース全ての確認)
	@Test
	public void testSpec_Normal() {
		testNormal(BmsSpec.SPEC_CHANNEL_MIN, BmsSpec.SPEC_CHANNEL_MAX, null, true, BmsChannel::spec);
	}

	// spec(int, BmsType, String, String, boolean, boolean)
	// NullPointerException typeがnull
	@Test
	public void testSpec_NullType() {
		var ec = NullPointerException.class;
		assertThrows(ec, () -> BmsChannel.spec(BmsSpec.SPEC_CHANNEL_MIN, null, null, "0", false, false));
	}

	// spec(int, BmsType, String, String, boolean, boolean)
	// NullPointerException defaultValueがnull
	@Test
	public void testSpec_NullDefaultValue() {
		testNullDefaultValue(BmsSpec.SPEC_CHANNEL_MIN, BmsChannel::spec);
	}

	// spec(int, BmsType, String, String, boolean, boolean)
	// IllegalArgumentException numberがBmsSpec#SPEC_CHANNEL_MIN未満またはBmsSpec#SPEC_CHANNEL_MAX超過
	@Test
	public void testSpec_NumberOutOfRange() {
		var ec = IllegalArgumentException.class;
		assertThrows(ec, () -> BmsChannel.spec(BmsSpec.SPEC_CHANNEL_MIN - 1, BmsType.INTEGER, null, "0", false, false));
		assertThrows(ec, () -> BmsChannel.spec(BmsSpec.SPEC_CHANNEL_MAX + 1, BmsType.INTEGER, null, "0", false, false));
	}

	// spec(int, BmsType, String, String, boolean, boolean)
	// IllegalArgumentException typeがBmsType#OBJECT
	@Test
	public void testSpec_ObjectType() {
		var ec = IllegalArgumentException.class;
		assertThrows(ec, () -> BmsChannel.spec(BmsSpec.USER_CHANNEL_MIN, BmsType.OBJECT, null, null, false, false));
	}

	// spec(int, BmsType, String, String, boolean, boolean)
	// IllegalArgumentException データ型が値型の時、refがnullではない
	@Test
	public void testSpec_ValueTypeAndNonNullRef() {
		testValueTypeAndNonNullRef(BmsSpec.SPEC_CHANNEL_MIN, BmsChannel::spec);
	}

	// spec(int, BmsType, String, String, boolean, boolean)
	// IllegalArgumentException defaultValueがtypeのテストに失敗した
	@Test
	public void testSpec_InvalidDefaultValue() {
		testInvalidDefaultValue(BmsSpec.SPEC_CHANNEL_MIN, BmsChannel::spec);
	}

	// user(int, BmsType, String, String, boolean)
	// 正しいパラメータを指定したチャンネルオブジェクトが構築できること(正常ケース全ての確認)
	@Test
	public void testUser_Normal() {
		testNormal(BmsSpec.USER_CHANNEL_MIN, BmsSpec.USER_CHANNEL_MAX, BmsSpec.USER_CHANNEL_MIN + 1, false, FN_USER);
	}

	// user(int, BmsType, String, String, boolean)
	// NullPointerException typeがnull
	@Test
	public void testUser_NullType() {
		var ec = NullPointerException.class;
		assertThrows(ec, () -> BmsChannel.user(BmsSpec.USER_CHANNEL_MIN, null, null, "0", false));
	}

	// user(int, BmsType, String, String, boolean)
	// NullPointerException typeがBmsType#OBJECT以外の時、defaultValueがnull
	@Test
	public void testUser_NonObjectTypeAndNullDefaultValue() {
		testNullDefaultValue(BmsSpec.USER_CHANNEL_MIN, FN_USER);
	}

	// user(int, BmsType, String, String, boolean)
	// IllegalArgumentException typeがBmsType#OBJECTの時、defaultValueがnullではない
	@Test
	public void testUser_ObjectTypeAndNonNullDefaultValue() {
		var ec = IllegalArgumentException.class;
		assertThrows(ec, () -> BmsChannel.user(BmsSpec.USER_CHANNEL_MIN, BmsType.OBJECT, null, "0", false));
	}

	// user(int, BmsType, String, String, boolean)
	// IllegalArgumentException numberがBmsSpec#USER_CHANNEL_MIN未満またはBmsSpec#USER_CHANNEL_MAX超過
	@Test
	public void testUser_NumberOutOfRange() {
		var ec = IllegalArgumentException.class;
		assertThrows(ec, () -> BmsChannel.user(BmsSpec.USER_CHANNEL_MIN - 1, BmsType.INTEGER, null, "0", false));
		assertThrows(ec, () -> BmsChannel.user(BmsSpec.USER_CHANNEL_MAX + 1, BmsType.INTEGER, null, "0", false));
	}

	// user(int, BmsType, String, String, boolean)
	// IllegalArgumentException typeがBmsType#OBJECTの時、refがnullではない
	@Test
	public void testUser_ObjectTypeAndNonNullRef() {
		var ec = IllegalArgumentException.class;
		assertThrows(ec, () -> BmsChannel.user(BmsSpec.USER_CHANNEL_MIN, BmsType.OBJECT, "#meta", null, false));
	}

	// user(int, BmsType, String, String, boolean)
	// IllegalArgumentException データ型が値型で参照先メタ情報を指定した
	@Test
	public void testUser_ValueTypeAndNonNullRef() {
		testValueTypeAndNonNullRef(BmsSpec.USER_CHANNEL_MIN, FN_USER);
	}

	// user(int, BmsType, String, String, boolean)
	// IllegalArgumentException typeがBmsType#OBJECT以外の時、defaultValueがtypeのテストに失敗した
	@Test
	public void testUser_NonObjectTypeAndInvalidDefaultValue() {
		testInvalidDefaultValue(BmsSpec.USER_CHANNEL_MIN, FN_USER);
	}

	// object(int)
	// 正しいパラメータを指定したチャンネルオブジェクトが構築できること(正常ケース全ての確認)
	@Test
	public void testObject_Normal() {
		var c = BmsChannel.object(BmsSpec.USER_CHANNEL_MIN);
		assertEquals(BmsType.OBJECT, c.getType());
		assertEquals(BmsSpec.USER_CHANNEL_MIN, c.getNumber());
		assertNull(c.getRef());
		assertNull(c.getDefaultValue());
		assertFalse(c.isValueType());
		assertFalse(c.isArrayType());

		c = BmsChannel.object(BmsSpec.USER_CHANNEL_MAX);
		assertEquals(BmsType.OBJECT, c.getType());
		assertEquals(BmsSpec.USER_CHANNEL_MAX, c.getNumber());
		assertNull(c.getRef());
		assertNull(c.getDefaultValue());
		assertFalse(c.isValueType());
		assertFalse(c.isArrayType());
	}

	// object(int)
	// IllegalArgumentException numberがBmsSpec#USER_CHANNEL_MIN未満またはBmsSpec#USER_CHANNEL_MAX超過
	@Test
	public void testObject_NumberOutOfRange() {
		var ec = IllegalArgumentException.class;
		assertThrows(ec, () -> BmsChannel.object(BmsSpec.USER_CHANNEL_MIN - 1));
		assertThrows(ec, () -> BmsChannel.object(BmsSpec.USER_CHANNEL_MAX + 1));
	}

	// isSpec()
	// 正常
	// ・BmsSpec#SPEC_CHANNEL_MIN, MAXでtrue
	// ・BmsSpec#USER_CHANNEL_MIN, MAXでfalse
	@Test
	public void testIsSpec_001() {
		assertTrue((new BmsChannel(BmsSpec.SPEC_CHANNEL_MIN, BmsType.INTEGER, null, "0", false, false)).isSpec());
		assertTrue((new BmsChannel(BmsSpec.SPEC_CHANNEL_MAX, BmsType.INTEGER, null, "0", false, false)).isSpec());
		assertFalse((new BmsChannel(BmsSpec.USER_CHANNEL_MIN, BmsType.INTEGER, null, "0", false, false)).isSpec());
		assertFalse((new BmsChannel(BmsSpec.USER_CHANNEL_MAX, BmsType.INTEGER, null, "0", false, false)).isSpec());
	}

	// isUser()
	// 正常
	// ・BmsSpec#SPEC_CHANNEL_MIN, MAXでfalse
	// ・BmsSpec#USER_CHANNEL_MIN, MAXでtrue
	@Test
	public void testIsUser_001() {
		assertFalse((new BmsChannel(BmsSpec.SPEC_CHANNEL_MIN, BmsType.STRING, null, "", false, false)).isUser());
		assertFalse((new BmsChannel(BmsSpec.SPEC_CHANNEL_MAX, BmsType.STRING, null, "", false, false)).isUser());
		assertTrue((new BmsChannel(BmsSpec.USER_CHANNEL_MIN, BmsType.STRING, null, "", false, false)).isUser());
		assertTrue((new BmsChannel(BmsSpec.USER_CHANNEL_MAX, BmsType.STRING, null, "", false, false)).isUser());
	}

	private void testNormal(int num, int numOther, Integer numObjectType, boolean testUniqueness,
			ChannelCreator creator) {
		BmsChannel c;
		BmsType t;

		// チャンネル番号
		c = creator.create(num, BmsType.INTEGER, null, "0", false, false);
		assertEquals(num, c.getNumber());
		c = creator.create(numOther, BmsType.INTEGER, null, "0", false, false);
		assertEquals(numOther, c.getNumber());

		// 参照先メタ情報名称
		c = creator.create(num, BmsType.INTEGER, null, "0", false, false);
		assertEquals(null, c.getRef());
		c = creator.create(num, BmsType.ARRAY16, "hoge", "00", false, false);
		assertEquals("hoge", c.getRef());

		// 複数データフラグ
		c = creator.create(num, BmsType.INTEGER, null, "0", false, false);
		assertEquals(false, c.isMultiple());
		c = creator.create(num, BmsType.INTEGER, null, "0", true, false);
		assertEquals(true, c.isMultiple());

		// 同一性フラグ
		if (testUniqueness) {
			c = creator.create(num, BmsType.INTEGER, null, "0", false, false);
			assertEquals(false, c.isUniqueness());
			c = creator.create(num, BmsType.INTEGER, null, "0", false, true);
			assertEquals(true, c.isUniqueness());
		}

		// 時間関連フラグ
		c = creator.create(num, BmsType.INTEGER, null, "0", false, false);
		c.setRelatedFlag(0);
		assertEquals(false, c.isRelatedToTime());
		assertEquals(false, c.isLength());
		assertEquals(false, c.isBpm());
		assertEquals(false, c.isStop());

		c.setRelatedFlag(1);
		assertEquals(true, c.isRelatedToTime());
		assertEquals(true, c.isLength());
		assertEquals(false, c.isBpm());
		assertEquals(false, c.isStop());

		c.setRelatedFlag(2);
		assertEquals(true, c.isRelatedToTime());
		assertEquals(false, c.isLength());
		assertEquals(true, c.isBpm());
		assertEquals(false, c.isStop());

		c.setRelatedFlag(3);
		assertEquals(true, c.isRelatedToTime());
		assertEquals(false, c.isLength());
		assertEquals(false, c.isBpm());
		assertEquals(true, c.isStop());

		// データ型と初期値
		t = BmsType.INTEGER;
		c = creator.create(num, t, null, "10", false, false);
		assertEquals(BmsType.INTEGER, c.getType());
		assertEquals(10L, (long)c.getDefaultValue());
		assertEquals(true, c.isValueType());
		assertEquals(false, c.isArrayType());

		c = creator.create(num, t, null, "+3805", false, false);
		assertEquals(BmsType.INTEGER, c.getType());
		assertEquals(3805L, (long)c.getDefaultValue());
		assertEquals(true, c.isValueType());
		assertEquals(false, c.isArrayType());

		c = creator.create(num, t, null, "-99999", false, false);
		assertEquals(BmsType.INTEGER, c.getType());
		assertEquals(-99999L, (long)c.getDefaultValue());
		assertEquals(true, c.isValueType());
		assertEquals(false, c.isArrayType());

		t = BmsType.FLOAT;
		c = creator.create(num, t, null, "1", false, false);
		assertEquals(BmsType.FLOAT, c.getType());
		assertEquals(1.0, (double)c.getDefaultValue(), 0.0);
		assertEquals(true, c.isValueType());
		assertEquals(false, c.isArrayType());

		c = creator.create(num, t, null, "3.14159", false, false);
		assertEquals(BmsType.FLOAT, c.getType());
		assertEquals(3.14159, (double)c.getDefaultValue(), 0.0);
		assertEquals(true, c.isValueType());
		assertEquals(false, c.isArrayType());

		c = creator.create(num, t, null, "+100.123", false, false);
		assertEquals(BmsType.FLOAT, c.getType());
		assertEquals(100.123, (double)c.getDefaultValue(), 0.0);
		assertEquals(true, c.isValueType());
		assertEquals(false, c.isArrayType());

		c = creator.create(num, t, null, "-2800.45", false, false);
		assertEquals(BmsType.FLOAT, c.getType());
		assertEquals(-2800.45, (double)c.getDefaultValue(), 0.0);
		assertEquals(true, c.isValueType());
		assertEquals(false, c.isArrayType());

		t = BmsType.STRING;
		c = creator.create(num, t, null, "", false, false);
		assertEquals(BmsType.STRING, c.getType());
		assertEquals("", (String)c.getDefaultValue());
		assertEquals(true, c.isValueType());
		assertEquals(false, c.isArrayType());

		c = creator.create(num, t, null, "hoge", false, false);
		assertEquals(BmsType.STRING, c.getType());
		assertEquals("hoge", (String)c.getDefaultValue());
		assertEquals(true, c.isValueType());
		assertEquals(false, c.isArrayType());

		t = BmsType.BASE16;
		c = creator.create(num, t, null, "00", false, false);
		assertEquals(BmsType.BASE16, c.getType());
		assertEquals(0L, (long)c.getDefaultValue());
		assertEquals(true, c.isValueType());
		assertEquals(false, c.isArrayType());

		c = creator.create(num, t, null, "50", false, false);
		assertEquals(BmsType.BASE16, c.getType());
		assertEquals(0x50L, (long)c.getDefaultValue());
		assertEquals(true, c.isValueType());
		assertEquals(false, c.isArrayType());

		c = creator.create(num, t, null, "aa", false, false);
		assertEquals(BmsType.BASE16, c.getType());
		assertEquals(0xaaL, (long)c.getDefaultValue());
		assertEquals(true, c.isValueType());
		assertEquals(false, c.isArrayType());

		c = creator.create(num, t, null, "EF", false, false);
		assertEquals(BmsType.BASE16, c.getType());
		assertEquals(0xefL, (long)c.getDefaultValue());
		assertEquals(true, c.isValueType());
		assertEquals(false, c.isArrayType());

		t = BmsType.BASE36;
		c = creator.create(num, t, null, "00", false, false);
		assertEquals(BmsType.BASE36, c.getType());
		assertEquals(0L, (long)c.getDefaultValue());
		assertEquals(true, c.isValueType());
		assertEquals(false, c.isArrayType());

		c = creator.create(num, t, null, "30", false, false);
		assertEquals(BmsType.BASE36, c.getType());
		assertEquals(108L, (long)c.getDefaultValue());
		assertEquals(true, c.isValueType());
		assertEquals(false, c.isArrayType());

		c = creator.create(num, t, null, "ap", false, false);
		assertEquals(BmsType.BASE36, c.getType());
		assertEquals(385L, (long)c.getDefaultValue());
		assertEquals(true, c.isValueType());
		assertEquals(false, c.isArrayType());

		c = creator.create(num, t, null, "ZX", false, false);
		assertEquals(BmsType.BASE36, c.getType());
		assertEquals(1293L, (long)c.getDefaultValue());
		assertEquals(true, c.isValueType());
		assertEquals(false, c.isArrayType());

		t = BmsType.ARRAY16;
		c = creator.create(num, t, null, "", false, false);
		assertEquals(BmsType.ARRAY16, c.getType());
		assertEquals(new BmsArray("", 16), (BmsArray)c.getDefaultValue());
		assertEquals(false, c.isValueType());
		assertEquals(true, c.isArrayType());

		c = creator.create(num, t, null, "", false, false);
		assertEquals(BmsType.ARRAY16, c.getType());
		assertNotEquals(new BmsArray("", 36), (BmsArray)c.getDefaultValue());
		assertEquals(false, c.isValueType());
		assertEquals(true, c.isArrayType());

		c = creator.create(num, t, null, "0088CC", false, false);
		assertEquals(BmsType.ARRAY16, c.getType());
		assertEquals(new BmsArray("0088CC", 16), (BmsArray)c.getDefaultValue());
		assertEquals(false, c.isValueType());
		assertEquals(true, c.isArrayType());

		c = creator.create(num, t, null, "aaddff", false, false);
		assertEquals(BmsType.ARRAY16, c.getType());
		assertEquals(new BmsArray("AADDFF", 16), (BmsArray)c.getDefaultValue());
		assertEquals(false, c.isValueType());
		assertEquals(true, c.isArrayType());

		if (numObjectType != null) {
			t = BmsType.OBJECT;
			c = creator.create(numObjectType, t, null, null, false, false);
			assertEquals(BmsType.OBJECT, c.getType());
			assertNull(c.getDefaultValue());
			assertEquals(false, c.isValueType());
			assertEquals(false, c.isArrayType());
		}
	}

	private void testNullDefaultValue(int num, ChannelCreator creator) {
		var ec = NullPointerException.class;
		assertThrows(ec, () ->creator.create(num, BmsType.INTEGER, null, null, false, false));
		assertThrows(ec, () ->creator.create(num, BmsType.FLOAT, null, null, false, false));
		assertThrows(ec, () ->creator.create(num, BmsType.STRING, null, null, false, false));
		assertThrows(ec, () ->creator.create(num, BmsType.BASE16, null, null, false, false));
		assertThrows(ec, () ->creator.create(num, BmsType.BASE36, null, null, false, false));
		assertThrows(ec, () ->creator.create(num, BmsType.ARRAY16, null, null, false, false));
		assertThrows(ec, () ->creator.create(num, BmsType.ARRAY36, null, null, false, false));
	}

	private void testInvalidDefaultValue(int num, ChannelCreator creator) {
		var ec = IllegalArgumentException.class;
		var t1 = BmsType.INTEGER;
		assertThrows(ec, () -> creator.create(num, t1, null, "", false, false));
		assertThrows(ec, () -> creator.create(num, t1, null, "100 ", false, false));
		assertThrows(ec, () -> creator.create(num, t1, null, " 100", false, false));
		assertThrows(ec, () -> creator.create(num, t1, null, "+-30", false, false));
		assertThrows(ec, () -> creator.create(num, t1, null, "1.2", false, false));
		assertThrows(ec, () -> creator.create(num, t1, null, "NAN", false, false));
		assertThrows(ec, () -> creator.create(num, t1, null, "ff", false, false));
		assertThrows(ec, () -> creator.create(num, t1, null, "zz", false, false));
		var t2 = BmsType.FLOAT;
		assertThrows(ec, () -> creator.create(num, t2, null, "", false, false));
		assertThrows(ec, () -> creator.create(num, t2, null, "100 ", false, false));
		assertThrows(ec, () -> creator.create(num, t2, null, " 100", false, false));
		assertThrows(ec, () -> creator.create(num, t2, null, "+-1.5", false, false));
		assertThrows(ec, () -> creator.create(num, t2, null, "10.20.30", false, false));
		assertThrows(ec, () -> creator.create(num, t2, null, ".58", false, false));
		assertThrows(ec, () -> creator.create(num, t2, null, "NAN", false, false));
		assertThrows(ec, () -> creator.create(num, t2, null, "ff", false, false));
		assertThrows(ec, () -> creator.create(num, t2, null, "zz", false, false));
		var t3 = BmsType.BASE16;
		assertThrows(ec, () -> creator.create(num, t3, null, "", false, false));
		assertThrows(ec, () -> creator.create(num, t3, null, " 11", false, false));
		assertThrows(ec, () -> creator.create(num, t3, null, "22 ", false, false));
		assertThrows(ec, () -> creator.create(num, t3, null, "0", false, false));
		assertThrows(ec, () -> creator.create(num, t3, null, "aaa", false, false));
		assertThrows(ec, () -> creator.create(num, t3, null, "FFF", false, false));
		assertThrows(ec, () -> creator.create(num, t3, null, "GH", false, false));
		assertThrows(ec, () -> creator.create(num, t3, null, "YZ", false, false));
		assertThrows(ec, () -> creator.create(num, t3, null, "NAN", false, false));
		assertThrows(ec, () -> creator.create(num, t3, null, "+00", false, false));
		assertThrows(ec, () -> creator.create(num, t3, null, "-FF", false, false));
		var t4 = BmsType.BASE36;
		assertThrows(ec, () -> creator.create(num, t4, null, "", false, false));
		assertThrows(ec, () -> creator.create(num, t4, null, " 11", false, false));
		assertThrows(ec, () -> creator.create(num, t4, null, "22 ", false, false));
		assertThrows(ec, () -> creator.create(num, t4, null, "0", false, false));
		assertThrows(ec, () -> creator.create(num, t4, null, "aaa", false, false));
		assertThrows(ec, () -> creator.create(num, t4, null, "ZZZ", false, false));
		assertThrows(ec, () -> creator.create(num, t4, null, "NAN", false, false));
		assertThrows(ec, () -> creator.create(num, t4, null, "+00", false, false));
		assertThrows(ec, () -> creator.create(num, t4, null, "-ZZ", false, false));
		var t5 = BmsType.ARRAY16;
		assertThrows(ec, () -> creator.create(num, t5, null, " 001122", false, false));
		assertThrows(ec, () -> creator.create(num, t5, null, "ccddee ", false, false));
		assertThrows(ec, () -> creator.create(num, t5, null, "0", false, false));
		assertThrows(ec, () -> creator.create(num, t5, null, "112", false, false));
		assertThrows(ec, () -> creator.create(num, t5, null, "AABBGG", false, false));
		assertThrows(ec, () -> creator.create(num, t5, null, "ZZ", false, false));
		assertThrows(ec, () -> creator.create(num, t5, null, "+CC", false, false));
		assertThrows(ec, () -> creator.create(num, t5, null, "-DD", false, false));
		assertThrows(ec, () -> creator.create(num, t5, null, "NAN", false, false));
		var t6 = BmsType.ARRAY36;
		assertThrows(ec, () -> creator.create(num, t6, null, " 001122", false, false));
		assertThrows(ec, () -> creator.create(num, t6, null, "xxyyzz ", false, false));
		assertThrows(ec, () -> creator.create(num, t6, null, "0", false, false));
		assertThrows(ec, () -> creator.create(num, t6, null, "112", false, false));
		assertThrows(ec, () -> creator.create(num, t6, null, "AABB??", false, false));
		assertThrows(ec, () -> creator.create(num, t6, null, "**", false, false));
		assertThrows(ec, () -> creator.create(num, t6, null, "+VV", false, false));
		assertThrows(ec, () -> creator.create(num, t6, null, "-WW", false, false));
		assertThrows(ec, () -> creator.create(num, t6, null, "NAN", false, false));
	}

	private void testValueTypeAndNonNullRef(int num, ChannelCreator creator) {
		var ec = IllegalArgumentException.class;
		assertThrows(ec, () -> BmsChannel.spec(num, BmsType.INTEGER, "#meta", "0", false, false));
		assertThrows(ec, () -> BmsChannel.spec(num, BmsType.FLOAT, "#meta", "0", false, false));
		assertThrows(ec, () -> BmsChannel.spec(num, BmsType.STRING, "#meta", "", false, false));
		assertThrows(ec, () -> BmsChannel.spec(num, BmsType.BASE16, "#meta", "00", false, false));
		assertThrows(ec, () -> BmsChannel.spec(num, BmsType.BASE36, "#meta", "00", false, false));
	}
}
