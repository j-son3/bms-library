package com.lmt.lib.bms;

import static org.junit.Assert.*;

import org.junit.Test;

public class BmsSpecBuilderTest {
	// BmsSpecBuilder()
	@Test
	public void testBmsSpecBuilder() {
		// Do nothing
	}

	// addMeta(BmsMeta)
	// 正常
	@Test
	public void testAddMeta_001() {
		var b = new BmsSpecBuilder()
				.addMeta(new BmsMeta("#single", BmsUnit.SINGLE, BmsType.STRING, "", 0, false))
				.addMeta(new BmsMeta("#multiple", BmsUnit.MULTIPLE, BmsType.STRING, "", 0, false))
				.addMeta(new BmsMeta("#indexed", BmsUnit.INDEXED, BmsType.STRING, "", 0, false));

		assertTrue(b.containsMeta("#single", BmsUnit.SINGLE));
		assertFalse(b.containsMeta("#single", BmsUnit.MULTIPLE));
		assertFalse(b.containsMeta("#single", BmsUnit.INDEXED));

		assertTrue(b.containsMeta("#multiple", BmsUnit.MULTIPLE));
		assertFalse(b.containsMeta("#multiple", BmsUnit.SINGLE));
		assertFalse(b.containsMeta("#multiple", BmsUnit.INDEXED));

		assertTrue(b.containsMeta("#indexed", BmsUnit.INDEXED));
		assertFalse(b.containsMeta("#indexed", BmsUnit.SINGLE));
		assertFalse(b.containsMeta("#indexed", BmsUnit.MULTIPLE));
	}

	// addMeta(BmsMeta)
	// IllegalStateException BMS仕様生成後にこのメソッドを呼び出した
	@Test
	public void testAddMeta_002() {
		var b = createUsedBuilder();
		var ex = IllegalStateException.class;
		assertThrows(ex, () -> b.addMeta(new BmsMeta("#meta", BmsUnit.SINGLE, BmsType.STRING, "", 0, false)));
	}

	// addMeta(BmsMeta)
	// NullPointerException metaがnull
	@Test
	public void testAddMeta_003() {
		assertThrows(NullPointerException.class, () -> new BmsSpecBuilder().addMeta(null));
	}

	// addMeta(BmsMeta)
	// IllegalArgumentException メタ情報単位がSINGLEまたはMULTIPLEの時、同じ名前のメタ情報がSINGLE/MULTIPLEのいずれかに登録済み
	@Test
	public void testAddMeta_004() {
		var b = new BmsSpecBuilder()
				.addMeta(new BmsMeta("#single", BmsUnit.SINGLE, BmsType.STRING, "", 0, false))
				.addMeta(new BmsMeta("#multiple", BmsUnit.MULTIPLE, BmsType.STRING, "", 0, false));
		var ex = IllegalArgumentException.class;
		// SINGLEと同じ名前のMULTIPLE
		assertThrows(ex, () -> b.addMeta(new BmsMeta("#single", BmsUnit.MULTIPLE, BmsType.STRING, "", 0, false)));
		// MULTIPLEと同じ名前のSINGLE
		assertThrows(ex, () -> b.addMeta(new BmsMeta("#multiple", BmsUnit.SINGLE, BmsType.STRING, "", 0, false)));
	}

	// addMeta(BmsMeta)
	// IllegalArgumentException メタ情報単位がINDEXEDの時、同じ名前のメタ情報がINDEXEDに登録済み
	@Test
	public void testAddMeta_005() {
		var b = new BmsSpecBuilder().addMeta(new BmsMeta("#indexed", BmsUnit.INDEXED, BmsType.STRING, "", 0, false));
		var ex = IllegalArgumentException.class;
		assertThrows(ex, () -> b.addMeta(new BmsMeta("#indexed", BmsUnit.INDEXED, BmsType.INTEGER, "0", 0, false)));
	}

	// addChannel(BmsChannel)
	// 正常
	@Test
	public void testAddChannel_001() {
		var b = new BmsSpecBuilder()
				.addChannel(new BmsChannel(BmsSpec.SPEC_CHANNEL_MIN, BmsType.STRING, null, "", false, false))
				.addChannel(new BmsChannel(BmsSpec.SPEC_CHANNEL_MAX, BmsType.STRING, null, "", false, false))
				.addChannel(new BmsChannel(BmsSpec.USER_CHANNEL_MIN, BmsType.STRING, null, "", false, false))
				.addChannel(new BmsChannel(BmsSpec.USER_CHANNEL_MAX, BmsType.STRING, null, "", false, false));
		assertTrue(b.containsChannel(BmsSpec.SPEC_CHANNEL_MIN));
		assertTrue(b.containsChannel(BmsSpec.SPEC_CHANNEL_MAX));
		assertTrue(b.containsChannel(BmsSpec.USER_CHANNEL_MIN));
		assertTrue(b.containsChannel(BmsSpec.USER_CHANNEL_MAX));
	}

	// addChannel(BmsChannel)
	// IllegalStateException BMS仕様生成後にこのメソッドを呼び出した
	@Test
	public void testAddChannel_002() {
		var b = createUsedBuilder();
		var ex = IllegalStateException.class;
		assertThrows(ex, () -> b.addChannel(new BmsChannel(1, BmsType.STRING, null, "", false, false)));
	}

	// addChannel(BmsChannel)
	// NullPointerException channelがnull
	@Test
	public void testAddChannel_003() {
		assertThrows(NullPointerException.class, () -> new BmsSpecBuilder().addChannel(null));
	}

	// addChannel(BmsChannel)
	// IllegalArgumentException 同じチャンネル番号のチャンネルが登録済み
	@Test
	public void testAddChannel_004() {
		var b = new BmsSpecBuilder().addChannel(new BmsChannel(1, BmsType.STRING, null, "", false, false));
		var ex = IllegalArgumentException.class;
		assertThrows(ex, () -> b.addChannel(new BmsChannel(1, BmsType.STRING, null, "", false, false)));
	}

	// getMeta(String, BmsUnit)
	// 正常
	@Test
	public void testGetMeta_001() {
		var m = new BmsMeta("#meta", BmsUnit.SINGLE, BmsType.STRING, "", 0, false);
		var b = new BmsSpecBuilder().addMeta(m);

		var got = b.getMeta("#meta", BmsUnit.SINGLE);
		assertNotNull(got);
		assertEquals("#meta", got.getName());
		assertEquals(BmsUnit.SINGLE, got.getUnit());

		var notFound = b.getMeta("#notfound", BmsUnit.MULTIPLE);
		assertNull(notFound);
	}

	// getMeta(String, BmsUnit)
	// IllegalStateException BMS仕様生成後にこのメソッドを呼び出した
	@Test
	public void testGetMeta_002() {
		var b = createUsedBuilder();
		assertThrows(IllegalStateException.class, () -> b.getMeta("#hoge", BmsUnit.SINGLE));
	}

	// getMeta(String, BmsUnit)
	// NullPointerException nameがnull
	@Test
	public void testGetMeta_003() {
		assertThrows(NullPointerException.class, () -> new BmsSpecBuilder().getMeta(null, BmsUnit.SINGLE));
	}

	// getMeta(String, BmsUnit)
	// NullPointerException unitがnull
	@Test
	public void testGetMeta_004() {
		assertThrows(NullPointerException.class, () -> new BmsSpecBuilder().getMeta("#hoge", null));
	}

	// getChannel(int)
	// 正常
	@Test
	public void testGetChannel_001() {
		var b = new BmsSpecBuilder()
				.addChannel(new BmsChannel(1, BmsType.STRING, null, "", false, false));

		var got = b.getChannel(1);
		assertNotNull(got);
		assertEquals(1, got.getNumber());

		var notFound = b.getChannel(999);
		assertNull(notFound);
	}

	// getChannel(int)
	// IllegalStateException BMS仕様生成後にこのメソッドを呼び出した
	@Test
	public void testGetChannel_002() {
		var b = createUsedBuilder();
		assertThrows(IllegalStateException.class, () -> b.getChannel(1));
	}

	// containsMeta(String, BmsUnit)
	// 正常
	@Test
	public void testContainsMeta_001() {
		var b = new BmsSpecBuilder()
				.addMeta(new BmsMeta("#single", BmsUnit.SINGLE, BmsType.STRING, "", 0, false))
				.addMeta(new BmsMeta("#multiple", BmsUnit.MULTIPLE, BmsType.STRING, "", 0, false))
				.addMeta(new BmsMeta("#indexed", BmsUnit.INDEXED, BmsType.STRING, "", 0, false));

		assertTrue(b.containsMeta("#single", BmsUnit.SINGLE));
		assertFalse(b.containsMeta("#single", BmsUnit.MULTIPLE));
		assertFalse(b.containsMeta("#single", BmsUnit.INDEXED));

		assertFalse(b.containsMeta("#multiple", BmsUnit.SINGLE));
		assertTrue(b.containsMeta("#multiple", BmsUnit.MULTIPLE));
		assertFalse(b.containsMeta("#multiple", BmsUnit.INDEXED));

		assertFalse(b.containsMeta("#indexed", BmsUnit.SINGLE));
		assertFalse(b.containsMeta("#indexed", BmsUnit.MULTIPLE));
		assertTrue(b.containsMeta("#indexed", BmsUnit.INDEXED));
	}

	// containsMeta(String, BmsUnit)
	// IllegalStateException BMS仕様生成後にこのメソッドを呼び出した
	@Test
	public void testContainsMeta_002() {
		var b = createUsedBuilder();
		assertThrows(IllegalStateException.class, () -> b.containsMeta("#meta", BmsUnit.SINGLE));
	}

	// containsMeta(String, BmsUnit)
	// NullPointerException nameがnull
	@Test
	public void testContainsMeta_003() {
		assertThrows(NullPointerException.class, () -> new BmsSpecBuilder().containsMeta(null, BmsUnit.SINGLE));
	}

	// containsMeta(String, BmsUnit)
	// NullPointerException unitがnull
	@Test
	public void testContainsMeta_004() {
		assertThrows(NullPointerException.class, () -> new BmsSpecBuilder().containsMeta("#meta", null));
	}

	// containsChannel(int)
	// 正常
	@Test
	public void testContainsChannel_001() {
		var b = new BmsSpecBuilder()
				.addChannel(new BmsChannel(1, BmsType.STRING, null, "", false, false));

		assertTrue(b.containsChannel(1));
		assertFalse(b.containsChannel(2));

		// 変な値を入れても例外が発生せずにfalseを返す
		assertFalse(b.containsChannel(BmsSpec.CHANNEL_MIN - 1));
	}

	// containsChannel(int)
	// IllegalStateException BMS仕様生成後にこのメソッドを呼び出した
	@Test
	public void testContainsChannel_002() {
		var b = createUsedBuilder();
		assertThrows(IllegalStateException.class, () -> b.containsChannel(1));
	}

	// setBaseChangerMeta(String)
	// 指定した名前のメタ情報が設定されること
	@Test
	public void testSetBaseChangerMeta_Normal() throws Exception {
		var b = new BmsSpecBuilder();
		assertSame(b, b.setBaseChangerMeta("#m"));
		assertEquals("#m", Tests.getf(b, "mBaseChangerMetaName"));
	}

	// setBaseChangerMeta(String)
	// IllegalStateException BMS仕様生成後にこのメソッドを呼び出した
	@Test
	public void testSetBaseChangerMeta_AfterCreate() {
		var b = createUsedBuilder();
		assertThrows(IllegalStateException.class, () -> b.setBaseChangerMeta("#m"));
	}

	// setLengthChannel(Integer)
	// 正常
	@Test
	public void testSetLengthChannel_001() {
		new BmsSpecBuilder()
				.setLengthChannel(null)  // null OK
				.setLengthChannel(1)  // 実際にCh無くてもOK
				.setLengthChannel(BmsSpec.CHANNEL_MIN - 1);  // 不正なCh番号でもOK
	}

	// setLengthChannel(Integer)
	// IllegalStateException BMS仕様生成後にこのメソッドを呼び出した
	@Test
	public void testSetLengthChannel_002() {
		var b = createUsedBuilder();
		assertThrows(IllegalStateException.class, () -> b.setLengthChannel(1));
	}

	// setBpmChannel(Integer...)
	// 正常
	@Test
	public void testSetBpmChannel_001() {
		new BmsSpecBuilder()
			.setBpmChannel((Integer)null)  // null OK
			.setBpmChannel(1)  // 実際にCh無くてもOK
			.setBpmChannel(BmsSpec.CHANNEL_MIN - 1)  // 不正なCh番号でもOK
			.setBpmChannel();  // 引数なしでもOK
	}

	// setBpmChannel(Integer...)
	// IllegalStateException BMS仕様生成後にこのメソッドを呼び出した
	@Test
	public void testSetBpmChannel_002() {
		var b = createUsedBuilder();
		assertThrows(IllegalStateException.class, () -> b.setBpmChannel(1));
	}

	// setStopChannel(Integer...)
	// 正常
	@Test
	public void testSetStopChannel_001() {
		new BmsSpecBuilder()
			.setStopChannel((Integer)null)  // null OK
			.setStopChannel(1)  // 実際にCh無くてもOK
			.setStopChannel(BmsSpec.CHANNEL_MIN - 1)  // 不正なCh番号でもOK
			.setStopChannel();  // 引数なしでもOK
	}

	// setStopChannel(Integer...)
	// IllegalStateException BMS仕様生成後にこのメソッドを呼び出した
	@Test
	public void testSetStopChannel_002() {
		var b = createUsedBuilder();
		assertThrows(IllegalStateException.class, () -> b.setStopChannel(1));
	}

	// create()
	// 正常
	@Test
	public void testCreate_001() {
		BmsSpec s;
		BmsChannel c;
		BmsMeta m;

		// チャンネルが1件以上存在すること
		// 配列型で一意性チェック対象チャンネルが1件以上存在すること
		s = new BmsSpecBuilder()
				.addMeta(BmsMeta.single("#bpm", BmsType.FLOAT, "130", 0, true))
				.addChannel(new BmsChannel(1, BmsType.ARRAY36, null, "00", false, true))
				.addChannel(new BmsChannel(2, BmsType.INTEGER, null, "0", false, false))
				.setInitialBpmMeta("#bpm")
				.create();
		assertNotNull(s);
		assertEquals(2, s.getChannels().size());

		// 小節長変更のチャンネルに関するエラーチェック
		// 小節長変更チャンネルが未設定でも生成できること
		s = new BmsSpecBuilder()
				.addMeta(BmsMeta.single("#bpm", BmsType.FLOAT, "130", 0, true))
				.addChannel(new BmsChannel(1, BmsType.ARRAY36, null, "00", false, true))
				.setInitialBpmMeta("#bpm")
				.setLengthChannel(null)
				.create();
		assertNotNull(s);
		assertNull(s.getLengthChannel());
		// 正しい小節長変更チャンネルを設定できること
		s = new BmsSpecBuilder()
				.addMeta(BmsMeta.single("#bpm", BmsType.FLOAT, "130", 0, true))
				.addChannel(new BmsChannel(1, BmsType.ARRAY36, null, "00", false, true))
				.addChannel(new BmsChannel(2, BmsType.FLOAT, null, "1.0", false, true))
				.setInitialBpmMeta("#bpm")
				.setLengthChannel(2)
				.create();
		assertNotNull(s);
		assertNotNull(s.getLengthChannel());
		assertEquals(2, s.getLengthChannel().getNumber());
		assertEquals(BmsType.FLOAT, s.getLengthChannel().getType());
		assertEquals(true, s.getLengthChannel().isUniqueness());

		// BPM変更チャンネルに関するエラーチェック
		// BPM変更チャンネルが未設定でも生成できること
		s = new BmsSpecBuilder()
				.addMeta(BmsMeta.single("#bpm", BmsType.FLOAT, "130", 0, true))
				.addChannel(new BmsChannel(1, BmsType.ARRAY36, null, "00", false, true))
				.setInitialBpmMeta("#bpm")
				.setBpmChannel()
				.create();
		assertNotNull(s);
		assertEquals(0, s.getBpmChannelCount());
		// BPM変更チャンネルに1件nullを指定するとBPM変更チャンネル0件のBMS仕様になること
		s = new BmsSpecBuilder()
				.addMeta(BmsMeta.single("#bpm", BmsType.FLOAT, "130", 0, true))
				.addChannel(new BmsChannel(1, BmsType.ARRAY36, null, "00", false, true))
				.setInitialBpmMeta("#bpm")
				.setBpmChannel((Integer)null)
				.create();
		assertNotNull(s);
		assertEquals(0, s.getBpmChannelCount());
		// 参照先メタ情報のないBPM変更チャンネル
		s = new BmsSpecBuilder()
				.addMeta(BmsMeta.single("#bpm", BmsType.FLOAT, "130", 0, true))
				.addChannel(new BmsChannel(1, BmsType.ARRAY36, null, "00", false, true))
				.addChannel(new BmsChannel(2, BmsType.ARRAY36, null, "00", false, true))
				.setInitialBpmMeta("#bpm")
				.setBpmChannel(2)
				.create();
		assertNotNull(s);
		assertEquals(1, s.getBpmChannelCount());
		c = s.getBpmChannel(0, true);
		assertNotNull(c);
		assertEquals(2, c.getNumber());
		assertEquals(BmsType.ARRAY36, c.getType());
		assertEquals(true, c.isUniqueness());
		assertNull(c.getRef());
		// 参照先メタ情報のあるBPM変更チャンネル
		s = new BmsSpecBuilder()
				.addMeta(BmsMeta.single("#bpm", BmsType.FLOAT, "130", 0, true))
				.addMeta(new BmsMeta("#bpm", BmsUnit.INDEXED, BmsType.FLOAT, "130.0", 0, true))
				.addChannel(new BmsChannel(1, BmsType.ARRAY36, null, "00", false, true))
				.addChannel(new BmsChannel(2, BmsType.ARRAY36, "#bpm", "00", false, true))
				.setInitialBpmMeta("#bpm")
				.setBpmChannel(2)
				.create();
		assertNotNull(s);
		assertEquals(1, s.getBpmChannelCount());
		c = s.getBpmChannel(0, true);
		assertNotNull(c);
		assertEquals(2, c.getNumber());
		assertEquals(BmsType.ARRAY36, c.getType());
		assertEquals(true, c.isUniqueness());
		assertNotNull(c.getRef());
		m = s.getIndexedMeta(c.getRef());
		assertNotNull(m);
		assertEquals(BmsType.FLOAT, m.getType());
		assertEquals(true, m.isUniqueness());
		// 複数のBPM変更チャンネルを設定できること
		s = new BmsSpecBuilder()
				.addMeta(BmsMeta.single("#bpm", BmsType.FLOAT, "130", 0, true))
				.addMeta(new BmsMeta("#bpm", BmsUnit.INDEXED, BmsType.FLOAT, "130.0", 0, true))
				.addChannel(new BmsChannel(1, BmsType.ARRAY36, null, "00", false, true))
				.addChannel(new BmsChannel(2, BmsType.ARRAY36, "#bpm", "00", false, true))
				.addChannel(new BmsChannel(3, BmsType.ARRAY16, null, "00", false, true))
				.setInitialBpmMeta("#bpm")
				.setBpmChannel(2, 3)
				.create();
		assertNotNull(s);
		assertEquals(2, s.getBpmChannelCount());
		c = s.getBpmChannel(0, true);
		assertNotNull(c);
		assertEquals(2, c.getNumber());
		assertEquals(BmsType.ARRAY36, c.getType());
		assertEquals(true, c.isUniqueness());
		assertNotNull(c.getRef());
		c = s.getBpmChannel(1, true);
		assertNotNull(c);
		assertEquals(3, c.getNumber());
		assertEquals(BmsType.ARRAY16, c.getType());
		assertEquals(true, c.isUniqueness());
		assertNull(c.getRef());

		// 譜面停止チャンネルに関するエラーチェック
		// 譜面停止チャンネルが未設定でも生成できること
		s = new BmsSpecBuilder()
				.addMeta(BmsMeta.single("#bpm", BmsType.FLOAT, "130", 0, true))
				.addChannel(new BmsChannel(1, BmsType.ARRAY36, null, "00", false, true))
				.setStopChannel()
				.setInitialBpmMeta("#bpm")
				.create();
		assertNotNull(s);
		assertEquals(0, s.getStopChannelCount());
		// 譜面停止チャンネルに1件nullを指定すると譜面停止チャンネル0件のBMS仕様になること
		s = new BmsSpecBuilder()
				.addMeta(BmsMeta.single("#bpm", BmsType.FLOAT, "130", 0, true))
				.addChannel(new BmsChannel(1, BmsType.ARRAY36, null, "00", false, true))
				.setStopChannel((Integer)null)
				.setInitialBpmMeta("#bpm")
				.create();
		assertNotNull(s);
		assertEquals(0, s.getStopChannelCount());
		// 参照先メタ情報のない譜面停止チャンネル
		s = new BmsSpecBuilder()
				.addMeta(BmsMeta.single("#bpm", BmsType.FLOAT, "130", 0, true))
				.addChannel(new BmsChannel(1, BmsType.ARRAY36, null, "00", false, true))
				.addChannel(new BmsChannel(2, BmsType.ARRAY36, null, "00", false, true))
				.setInitialBpmMeta("#bpm")
				.setStopChannel(2)
				.create();
		assertNotNull(s);
		assertEquals(1, s.getStopChannelCount());
		c = s.getStopChannel(0, true);
		assertNotNull(c);
		assertEquals(2, c.getNumber());
		assertEquals(BmsType.ARRAY36, c.getType());
		assertEquals(true, c.isUniqueness());
		assertNull(c.getRef());
		// 参照先メタ情報のある譜面停止チャンネル
		s = new BmsSpecBuilder()
				.addMeta(BmsMeta.single("#bpm", BmsType.FLOAT, "130", 0, true))
				.addMeta(new BmsMeta("#stop", BmsUnit.INDEXED, BmsType.INTEGER, "0", 0, true))
				.addChannel(new BmsChannel(1, BmsType.ARRAY36, null, "00", false, true))
				.addChannel(new BmsChannel(2, BmsType.ARRAY36, "#stop", "00", false, true))
				.setInitialBpmMeta("#bpm")
				.setStopChannel(2)
				.create();
		assertNotNull(s);
		assertEquals(1, s.getStopChannelCount());
		c = s.getStopChannel(0, true);
		assertNotNull(c);
		assertEquals(2, c.getNumber());
		assertEquals(BmsType.ARRAY36, c.getType());
		assertEquals(true, c.isUniqueness());
		assertNotNull(c.getRef());
		m = s.getIndexedMeta(c.getRef());
		assertNotNull(m);
		assertEquals(BmsType.INTEGER, m.getType());
		assertEquals(true, m.isUniqueness());
		// 複数の譜面停止を設定できること
		s = new BmsSpecBuilder()
				.addMeta(BmsMeta.single("#bpm", BmsType.FLOAT, "130", 0, true))
				.addMeta(new BmsMeta("#stop", BmsUnit.INDEXED, BmsType.INTEGER, "0", 0, true))
				.addChannel(new BmsChannel(1, BmsType.ARRAY36, null, "00", false, true))
				.addChannel(new BmsChannel(2, BmsType.ARRAY36, "#stop", "00", false, true))
				.addChannel(new BmsChannel(3, BmsType.ARRAY16, null, "00", false, true))
				.setInitialBpmMeta("#bpm")
				.setStopChannel(2, 3)
				.create();
		assertNotNull(s);
		assertEquals(2, s.getStopChannelCount());
		c = s.getStopChannel(0, true);
		assertNotNull(c);
		assertEquals(2, c.getNumber());
		assertEquals(BmsType.ARRAY36, c.getType());
		assertEquals(true, c.isUniqueness());
		assertNotNull(c.getRef());
		c = s.getStopChannel(1, true);
		assertNotNull(c);
		assertEquals(3, c.getNumber());
		assertEquals(BmsType.ARRAY16, c.getType());
		assertEquals(true, c.isUniqueness());
		assertNull(c.getRef());

		// 初期BPM
		s = new BmsSpecBuilder()
			.addMeta(BmsMeta.single("#bpm", BmsType.FLOAT, "100", 0, true))
			.addChannel(new BmsChannel(1, BmsType.ARRAY36, null, "00", false, true))
			.setInitialBpmMeta("#bpm")
			.create();
		m = s.getInitialBpmMeta();
		assertNotNull(m);
		assertEquals("#bpm", m.getName());
		assertEquals(BmsUnit.SINGLE, m.getUnit());
		assertEquals(BmsType.FLOAT, m.getType());
		assertEquals(100.0, (double)m.getDefaultValue(), 0.00001);
		assertEquals(0, m.getOrder());
		assertTrue(m.isInitialBpm());
		assertTrue(m.isUniqueness());
	}

	// create()
	// 基数選択メタ情報(初期値：16)を設定すると、当該メタ情報が基数選択メタ情報として登録されること
	@Test
	public void testCreate_Normal_BaseChanger_Default16() {
		var s = new BmsSpecBuilder()
				.addMeta(BmsMeta.single("#bpm", BmsType.FLOAT, "130", 0, true))
				.addMeta(BmsMeta.single("#base", BmsType.INTEGER, "16", 0, true))
				.addChannel(new BmsChannel(1, BmsType.ARRAY36, null, "00", false, true))
				.setInitialBpmMeta("#bpm")
				.setBaseChangerMeta("#base")
				.create();
		var m = s.getBaseChangerMeta();
		assertNotNull(m);
		assertTrue(m.isBaseChanger());
		assertEquals("#base", m.getName());
		assertTrue(m.isSingleUnit());
		assertEquals(16L, (long)m.getDefaultValue());
	}

	// create()
	// 基数選択メタ情報(初期値：36)を設定すると、当該メタ情報が基数選択メタ情報として登録されること
	@Test
	public void testCreate_Normal_BaseChanger_Default36() {
		var s = new BmsSpecBuilder()
				.addMeta(BmsMeta.single("#bpm", BmsType.FLOAT, "130", 0, true))
				.addMeta(BmsMeta.single("#base", BmsType.INTEGER, "36", 0, true))
				.addChannel(new BmsChannel(1, BmsType.ARRAY36, null, "00", false, true))
				.setInitialBpmMeta("#bpm")
				.setBaseChangerMeta("#base")
				.create();
		var m = s.getBaseChangerMeta();
		assertNotNull(m);
		assertTrue(m.isBaseChanger());
		assertEquals("#base", m.getName());
		assertTrue(m.isSingleUnit());
		assertEquals(36L, (long)m.getDefaultValue());
	}

	// create()
	// 基数選択メタ情報(初期値：62)を設定すると、当該メタ情報が基数選択メタ情報として登録されること
	@Test
	public void testCreate_Normal_BaseChanger_Default62() {
		var s = new BmsSpecBuilder()
				.addMeta(BmsMeta.single("#bpm", BmsType.FLOAT, "130", 0, true))
				.addMeta(BmsMeta.single("#base", BmsType.INTEGER, "62", 0, true))
				.addChannel(new BmsChannel(1, BmsType.ARRAY36, null, "00", false, true))
				.setInitialBpmMeta("#bpm")
				.setBaseChangerMeta("#base")
				.create();
		var m = s.getBaseChangerMeta();
		assertNotNull(m);
		assertTrue(m.isBaseChanger());
		assertEquals("#base", m.getName());
		assertTrue(m.isSingleUnit());
		assertEquals(62L, (long)m.getDefaultValue());
	}

	// create()
	// IllegalStateException BMS仕様生成後にこのメソッドを呼び出した
	@Test
	public void testCreate_002() {
		var b = createUsedBuilder();
		assertThrows(IllegalStateException.class, () -> b.create());
	}

	// create()
	// IllegalStateException チャンネルが1件も存在しない
	@Test
	public void testCreate_003() {
		assertThrows(IllegalStateException.class, () -> new BmsSpecBuilder().create());
	}

	// create()
	// IllegalStateException 同一性チェック対象の配列型仕様チャンネルが1件も存在しない
	@Test
	public void testCreate_004() {
		var b = new BmsSpecBuilder()
				.addChannel(new BmsChannel(1, BmsType.STRING, null, "", false, true))
				.addChannel(new BmsChannel(2, BmsType.ARRAY36, null, "00", false, false));
		assertThrows(IllegalStateException.class, () -> b.create());
	}

	// create()
	// IllegalStateException 配列型チャンネルで参照先メタ情報の指定がある場合に、索引付きメタ情報に該当する名前のメタ情報がない
	@Test
	public void testCreate_005() {
		var b = new BmsSpecBuilder()
				.addChannel(new BmsChannel(1, BmsType.ARRAY36, "#notfound", "00", false, true));
		assertThrows(IllegalStateException.class, () -> b.create());
	}

	// create()
	// IllegalStateException 小節長変更チャンネルの番号が指定されているが、当該番号のチャンネルが存在しない
	@Test
	public void testCreate_006() {
		var b = new BmsSpecBuilder()
				.addChannel(new BmsChannel(1, BmsType.ARRAY36, null, "00", false, true))
				.addChannel(new BmsChannel(2, BmsType.FLOAT, null, "1.0", false, true))
				.setLengthChannel(999);  // そんな番号のチャンネルはない
		assertThrows(IllegalStateException.class, () -> b.create());
	}

	// create()
	// IllegalStateException 指定された小節長変更チャンネルのデータ型がFLOATではない
	@Test
	public void testCreate_007() {
		var b = new BmsSpecBuilder()
				.addChannel(new BmsChannel(1, BmsType.ARRAY36, null, "00", false, true))
				.addChannel(new BmsChannel(2, BmsType.STRING, null, "", false, true))
				.setLengthChannel(2);
		assertThrows(IllegalStateException.class, () -> b.create());
	}

	// create()
	// IllegalStateException 指定された小節長変更チャンネルが同一性チェック対象になっていない
	@Test
	public void testCreate_008() {
		var b = new BmsSpecBuilder()
				.addChannel(new BmsChannel(1, BmsType.ARRAY36, null, "00", false, true))
				.addChannel(new BmsChannel(2, BmsType.FLOAT, null, "1.0", false, false))
				.setLengthChannel(2);
		assertThrows(IllegalStateException.class, () -> b.create());
	}

	// create()
	// IllegalStateException BPM変更チャンネルの番号が指定されているが、当該番号のチャンネルが存在しない
	@Test
	public void testCreate_009() {
		var b = new BmsSpecBuilder()
				.addChannel(new BmsChannel(1, BmsType.ARRAY36, null, "00", false, true))
				.addChannel(new BmsChannel(2, BmsType.ARRAY36, null, "00", false, true))
				.setBpmChannel(999);  // そんな番号のチャンネルはない
		assertThrows(IllegalStateException.class, () -> b.create());
	}

	// create()
	// IllegalStateException 指定されたBPM変更チャンネルのデータ型が配列型ではない
	@Test
	public void testCreate_010() {
		var b = new BmsSpecBuilder()
				.addChannel(new BmsChannel(1, BmsType.ARRAY36, null, "00", false, true))
				.addChannel(new BmsChannel(2, BmsType.INTEGER, null, "0", false, true))
				.setBpmChannel(2);
		assertThrows(IllegalStateException.class, () -> b.create());
	}

	// create()
	// IllegalStateException 指定されたBPM変更チャンネルが同一性チェック対象になっていない
	@Test
	public void testCreate_011() {
		var b = new BmsSpecBuilder()
				.addChannel(new BmsChannel(1, BmsType.ARRAY36, null, "00", false, true))
				.addChannel(new BmsChannel(2, BmsType.ARRAY36, null, "00", false, false))
				.setBpmChannel(2);
		assertThrows(IllegalStateException.class, () -> b.create());
	}

	// create()
	// IllegalStateException 指定されたBPM変更チャンネルに参照先メタ情報がある場合で、索引付きメタ情報に該当する名前のメタ情報がない
	@Test
	public void testCreate_012() {
		var b = new BmsSpecBuilder()
				.addChannel(new BmsChannel(1, BmsType.ARRAY36, null, "00", false, true))
				.addChannel(new BmsChannel(2, BmsType.ARRAY36, "#notfound", "00", false, true))
				.setBpmChannel(2);
		assertThrows(IllegalStateException.class, () -> b.create());
	}

	// create()
	// IllegalStateException 指定されたBPM変更チャンネルの参照先メタ情報のデータ型が数値型ではない
	@Test
	public void testCreate_013() {
		var b = new BmsSpecBuilder()
				.addMeta(new BmsMeta("#bpm", BmsUnit.INDEXED, BmsType.STRING, "", 0, true))
				.addChannel(new BmsChannel(1, BmsType.ARRAY36, null, "00", false, true))
				.addChannel(new BmsChannel(2, BmsType.ARRAY36, "#bpm", "00", false, true))
				.setBpmChannel(2);
		assertThrows(IllegalStateException.class, () -> b.create());
	}

	// create()
	// IllegalStateException 指定されたBPM変更チャンネルの参照先メタ情報が同一性チェック対象になっていない
	@Test
	public void testCreate_014() {
		var b = new BmsSpecBuilder()
				.addMeta(new BmsMeta("#bpm", BmsUnit.INDEXED, BmsType.FLOAT, "130.0", 0, false))
				.addChannel(new BmsChannel(1, BmsType.ARRAY36, null, "00", false, true))
				.addChannel(new BmsChannel(2, BmsType.ARRAY36, "#bpm", "00", false, true))
				.setBpmChannel(2);
		assertThrows(IllegalStateException.class, () -> b.create());
	}

	// create()
	// IllegalStateException 譜面停止チャンネルの番号が指定されているが、当該番号のチャンネルが存在しない
	@Test
	public void testCreate_015() {
		var b = new BmsSpecBuilder()
				.addChannel(new BmsChannel(1, BmsType.ARRAY36, null, "00", false, true))
				.addChannel(new BmsChannel(2, BmsType.ARRAY36, null, "00", false, true))
				.setStopChannel(999);  // そんな番号のチャンネルはない
		assertThrows(IllegalStateException.class, () -> b.create());
	}

	// create()
	// IllegalStateException 指定された譜面停止チャンネルのデータ型が配列型ではない
	@Test
	public void testCreate_016() {
		var b = new BmsSpecBuilder()
				.addChannel(new BmsChannel(1, BmsType.ARRAY36, null, "00", false, true))
				.addChannel(new BmsChannel(2, BmsType.INTEGER, null, "0", false, true))
				.setStopChannel(2);
		assertThrows(IllegalStateException.class, () -> b.create());
	}

	// create()
	// IllegalStateException 指定された譜面停止チャンネルが同一性チェック対象になっていない
	@Test
	public void testCreate_017() {
		var b = new BmsSpecBuilder()
				.addChannel(new BmsChannel(1, BmsType.ARRAY36, null, "00", false, true))
				.addChannel(new BmsChannel(2, BmsType.ARRAY36, null, "00", false, false))
				.setBpmChannel(2);
		assertThrows(IllegalStateException.class, () -> b.create());
	}

	// create()
	// IllegalStateException 指定された譜面停止チャンネルに参照先メタ情報がある場合で、索引付きメタ情報に該当する名前のメタ情報がない
	@Test
	public void testCreate_018() {
		var b = new BmsSpecBuilder()
				.addChannel(new BmsChannel(1, BmsType.ARRAY36, null, "00", false, true))
				.addChannel(new BmsChannel(2, BmsType.ARRAY36, "#notfound", "00", false, true))
				.setStopChannel(2);
		assertThrows(IllegalStateException.class, () -> b.create());
	}

	// create()
	// IllegalStateException 指定された譜面停止チャンネルの参照先メタ情報のデータ型が数値型ではない
	@Test
	public void testCreate_019() {
		var b = new BmsSpecBuilder()
				.addMeta(new BmsMeta("#stop", BmsUnit.INDEXED, BmsType.STRING, "", 0, true))
				.addChannel(new BmsChannel(1, BmsType.ARRAY36, null, "00", false, true))
				.addChannel(new BmsChannel(2, BmsType.ARRAY36, "#stop", "00", false, true))
				.setStopChannel(2);
		assertThrows(IllegalStateException.class, () -> b.create());
	}

	// create()
	// IllegalStateException 指定された譜面停止チャンネルの参照先メタ情報が同一性チェック対象になっていない
	@Test
	public void testCreate_020() {
		var b = new BmsSpecBuilder()
				.addMeta(new BmsMeta("#stop", BmsUnit.INDEXED, BmsType.INTEGER, "0", 0, false))
				.addChannel(new BmsChannel(1, BmsType.ARRAY36, null, "00", false, true))
				.addChannel(new BmsChannel(2, BmsType.ARRAY36, "#stop", "00", false, true))
				.setStopChannel(2);
		assertThrows(IllegalStateException.class, () -> b.create());
	}

	// create()
	// IllegalStateException ユーザーチャンネルで同一性チェックをONにした
	@Test
	public void testCreate_021() {
		var b = new BmsSpecBuilder()
				.addChannel(new BmsChannel(BmsSpec.USER_CHANNEL_MIN, BmsType.INTEGER, null, "0", false, true))
				.addChannel(new BmsChannel(2, BmsType.ARRAY36, null, "00", false, true));
		assertThrows(IllegalStateException.class, () -> b.create());
	}

	// create()
	// IllegalStateException 指定された小節長変更チャンネルの番号が仕様チャンネルではない
	@Test
	public void testCreate_022() {
		var lengthChannel = BmsSpec.USER_CHANNEL_MIN;
		var b = new BmsSpecBuilder()
				.addChannel(new BmsChannel(lengthChannel, BmsType.FLOAT, null, "1.0", false, false))
				.addChannel(new BmsChannel(2, BmsType.ARRAY36, null, "00", false, true))
				.setLengthChannel(lengthChannel);
		assertThrows(IllegalStateException.class, () -> b.create());
	}

	// create()
	// IllegalStateException 指定されたBPM変更チャンネルの番号が仕様チャンネルではない
	@Test
	public void testCreate_023() {
		var chgBpmChannel = BmsSpec.USER_CHANNEL_MIN;
		var b = new BmsSpecBuilder()
				.addMeta(BmsMeta.indexed("#bpm", BmsType.FLOAT, "0.0", 0, true))
				.addChannel(new BmsChannel(chgBpmChannel, BmsType.ARRAY36, "#bpm", "00", false, false))
				.addChannel(new BmsChannel(2, BmsType.ARRAY36, null, "00", false, true))
				.setBpmChannel(chgBpmChannel);
		assertThrows(IllegalStateException.class, () -> b.create());
	}

	// create()
	// IllegalStateException 指定された譜面停止チャンネルの番号が仕様チャンネルではない
	@Test
	public void testCreate_024() {
		var stopChannel = BmsSpec.USER_CHANNEL_MIN;
		var b = new BmsSpecBuilder()
				.addMeta(BmsMeta.indexed("#stop", BmsType.FLOAT, "0.0", 0, true))
				.addChannel(new BmsChannel(stopChannel, BmsType.ARRAY36, "#stop", "00", false, false))
				.addChannel(new BmsChannel(2, BmsType.ARRAY36, null, "00", false, true))
				.setStopChannel(stopChannel);
		assertThrows(IllegalStateException.class, () -> b.create());
	}

	// create()
	// IllegalStateException BPM変更チャンネルの番号が重複している
	@Test
	public void testCreate_025_DuplicateBpmChannel() {
		var b = new BmsSpecBuilder()
				.addMeta(BmsMeta.indexed("#bpm", BmsType.FLOAT, "130.0", 0, true))
				.addChannel(new BmsChannel(1, BmsType.ARRAY36, null, "00", false, true))
				.addChannel(new BmsChannel(2, BmsType.ARRAY36, "#bpm", "00", false, true))
				.setBpmChannel(2, 2);
		assertThrows(IllegalStateException.class, () -> b.create());
	}

	// create()
	// IllegalStateException 指定されたBPM変更チャンネルの番号がnull
	@Test
	public void testCreate_026_HasNullBpmChannel() {
		var b = new BmsSpecBuilder()
				.addMeta(BmsMeta.indexed("#bpm", BmsType.FLOAT, "130.0", 0, true))
				.addChannel(new BmsChannel(1, BmsType.ARRAY36, null, "00", false, true))
				.addChannel(new BmsChannel(2, BmsType.ARRAY36, "#bpm", "00", false, true))
				.setBpmChannel(2, null);
		assertThrows(IllegalStateException.class, () -> b.create());
	}

	// create()
	// IllegalStateException 譜面停止チャンネルの番号が重複している
	@Test
	public void testCreate_027_DuplicateStopChannel() {
		var b = new BmsSpecBuilder()
				.addMeta(BmsMeta.indexed("#stop", BmsType.INTEGER, "0", 0, true))
				.addChannel(new BmsChannel(1, BmsType.ARRAY36, null, "00", false, true))
				.addChannel(new BmsChannel(2, BmsType.ARRAY36, "#stop", "00", false, true))
				.setStopChannel(2, 2);
		assertThrows(IllegalStateException.class, () -> b.create());
	}

	// create()
	// IllegalStateException 指定された譜面停止チャンネルの番号がnull
	@Test
	public void testCreate_028_HasNullStopChannel() {
		var b = new BmsSpecBuilder()
				.addMeta(BmsMeta.indexed("#stop", BmsType.INTEGER, "0", 0, true))
				.addChannel(new BmsChannel(1, BmsType.ARRAY36, null, "00", false, true))
				.addChannel(new BmsChannel(2, BmsType.ARRAY36, "#stop", "00", false, true))
				.setStopChannel(2, null);
		assertThrows(IllegalStateException.class, () -> b.create());
	}

	// create()
	// IllegalStateException 初期BPMメタ情報の名称がnull
	@Test
	public void testCreate_InitialBpm_001() {
		var b = new BmsSpecBuilder()
				.addMeta(BmsMeta.single("#bpm", BmsType.FLOAT, "130", 0, true))
				.addChannel(new BmsChannel(1, BmsType.ARRAY36, null, "00", false, true))
				.setInitialBpmMeta(null);
		assertThrows(IllegalStateException.class, () -> b.create());
	}

	// create()
	// IllegalStateException 初期BPMメタ情報の名称に該当するメタ情報が単体メタ情報に存在しない
	@Test
	public void testCreate_InitialBpm_002() {
		var b = new BmsSpecBuilder()
				.addMeta(BmsMeta.single("#bpm", BmsType.FLOAT, "130", 0, true))
				.addChannel(new BmsChannel(1, BmsType.ARRAY36, null, "00", false, true))
				.setInitialBpmMeta("#unknown_meta_name");
		assertThrows(IllegalStateException.class, () -> b.create());
	}

	// create()
	// IllegalStateException 初期BPMメタ情報の名称に該当するメタ情報のデータ型が数値型ではない
	@Test
	public void testCreate_InitialBpm_003() {
		var b = new BmsSpecBuilder()
				.addMeta(BmsMeta.single("#bpm", BmsType.STRING, "130", 0, true))
				.addChannel(new BmsChannel(1, BmsType.ARRAY36, null, "00", false, true))
				.setInitialBpmMeta("#bpm");
		assertThrows(IllegalStateException.class, () -> b.create());
	}

	// create()
	// IllegalStateException 初期BPMメタ情報の名称に該当するメタ情報の初期値がBmsSpec#BPM_MIN未満
	@Test
	public void testCreate_InitialBpm_004() {
		var b = new BmsSpecBuilder()
				.addMeta(BmsMeta.single("#bpm", BmsType.FLOAT, String.valueOf(BmsSpec.BPM_MIN - 0.1), 0, true))
				.addChannel(new BmsChannel(1, BmsType.ARRAY36, null, "00", false, true))
				.setInitialBpmMeta("#bpm");
		assertThrows(IllegalStateException.class, () -> b.create());
	}

	// create()
	// IllegalStateException 初期BPMメタ情報の名称に該当するメタ情報の初期値がBmsSpec#BPM_MAX超過
	@Test
	public void testCreate_InitialBpm_005() {
		var b = new BmsSpecBuilder()
				.addMeta(BmsMeta.single("#bpm", BmsType.FLOAT, String.valueOf(BmsSpec.BPM_MAX + 1), 0, true))
				.addChannel(new BmsChannel(1, BmsType.ARRAY36, null, "00", false, true))
				.setInitialBpmMeta("#bpm");
		assertThrows(IllegalStateException.class, () -> b.create());
	}

	// create()
	// IllegalStateException 初期BPMメタ情報の名称に該当するメタ情報の同一性チェックがOFF
	@Test
	public void testCreate_InitialBpm_006() {
		var b = new BmsSpecBuilder()
				.addMeta(BmsMeta.single("#bpm", BmsType.FLOAT, "130", 0, false))
				.addChannel(new BmsChannel(1, BmsType.ARRAY36, null, "00", false, true))
				.setInitialBpmMeta("#bpm");
		assertThrows(IllegalStateException.class, () -> b.create());
	}

	// create()
	// 基数選択メタ情報の名称に該当するメタ情報が単体メタ情報に存在しない
	@Test
	public void testCreate_BaseChanger_SingleMetaNotFound() {
		var b = new BmsSpecBuilder()
				.addMeta(BmsMeta.single("#bpm", BmsType.FLOAT, "130", 0, true))
				.addChannel(new BmsChannel(1, BmsType.ARRAY36, null, "00", false, true))
				.setInitialBpmMeta("#bpm")
				.setBaseChangerMeta("#base");
		assertThrows(IllegalStateException.class, () -> b.create());
	}

	// create()
	// 基数選択メタ情報の名称に該当するメタ情報のデータ型がBmsType#INTEGERではない
	@Test
	public void testCreate_BaseChanger_NotIntegerType() {
		var b = new BmsSpecBuilder()
				.addMeta(BmsMeta.single("#bpm", BmsType.FLOAT, "130", 0, true))
				.addMeta(BmsMeta.single("#base", BmsType.STRING, "16", 0, true))
				.addChannel(new BmsChannel(1, BmsType.ARRAY36, null, "00", false, true))
				.setInitialBpmMeta("#bpm")
				.setBaseChangerMeta("#base");
		assertThrows(IllegalStateException.class, () -> b.create());
	}

	// create()
	// 基数選択メタ情報の名称に該当するメタ情報の初期値が16, 36, 62のいずれでもない
	@Test
	public void testCreate_BaseChanger_WrongInitialValue() {
		var b = new BmsSpecBuilder()
				.addMeta(BmsMeta.single("#bpm", BmsType.FLOAT, "130", 0, true))
				.addMeta(BmsMeta.single("#base", BmsType.INTEGER, "10", 0, true))
				.addChannel(new BmsChannel(1, BmsType.ARRAY36, null, "00", false, true))
				.setInitialBpmMeta("#bpm")
				.setBaseChangerMeta("#base");
		assertThrows(IllegalStateException.class, () -> b.create());
	}

	// create()
	// 基数選択メタ情報の名称に該当するメタ情報の同一性チェックがOFF
	@Test
	public void testCreate_BaseChanger_NotUniqueness() {
		var b = new BmsSpecBuilder()
				.addMeta(BmsMeta.single("#bpm", BmsType.FLOAT, "130", 0, true))
				.addMeta(BmsMeta.single("#base", BmsType.INTEGER, "36", 0, false))
				.addChannel(new BmsChannel(1, BmsType.ARRAY36, null, "00", false, true))
				.setInitialBpmMeta("#bpm")
				.setBaseChangerMeta("#base");
		assertThrows(IllegalStateException.class, () -> b.create());
	}

	// create()
	// IllegalStateException 指定された小節長変更チャンネルの初期値が{@link BmsSpec#LENGTH_MIN}未満
	@Test
	public void testCreate_LengthChannel_UnderflowDefaultValue() {
		var b = new BmsSpecBuilder()
				.addMeta(BmsMeta.single("#bpm", BmsType.FLOAT, "130", 0, true))
				.addChannel(new BmsChannel(1, BmsType.ARRAY36, null, "00", false, true))
				.addChannel(new BmsChannel(2, BmsType.INTEGER, null, "0", false, false))
				.addChannel(new BmsChannel(3, BmsType.FLOAT, null, String.valueOf(BmsSpec.LENGTH_MIN - 0.000000000001), false, true))
				.setInitialBpmMeta("#bpm")
				.setLengthChannel(3);
		assertThrows(IllegalStateException.class, () -> b.create());
	}

	// create()
	// IllegalStateException 指定された小節長変更チャンネルの初期値が{@link BmsSpec#LENGTH_MAX}超過
	@Test
	public void testCreate_LengthChannel_OverflowDefaultValue() {
		var b = new BmsSpecBuilder()
				.addMeta(BmsMeta.single("#bpm", BmsType.FLOAT, "130", 0, true))
				.addChannel(new BmsChannel(1, BmsType.ARRAY36, null, "00", false, true))
				.addChannel(new BmsChannel(2, BmsType.INTEGER, null, "0", false, false))
				.addChannel(new BmsChannel(3, BmsType.FLOAT, null, String.valueOf(BmsSpec.LENGTH_MAX + 0.000000000001), false, true))
				.setInitialBpmMeta("#bpm")
				.setLengthChannel(3);
		assertThrows(IllegalStateException.class, () -> b.create());
	}

	// create()
	// IllegalStateException 指定されたBPM変更チャンネルの参照先メタ情報の初期値が{@link BmsSpec#BPM_MIN}未満
	@Test
	public void testCreate_BpmChannel_UnderflowDefaultValue() {
		var b = new BmsSpecBuilder()
				.addMeta(BmsMeta.single("#bpm", BmsType.FLOAT, "130", 0, true))
				.addMeta(BmsMeta.indexed("#bpm", BmsType.FLOAT, String.valueOf(BmsSpec.BPM_MIN - 0.000000000001), 0, true))
				.addChannel(new BmsChannel(1, BmsType.ARRAY36, null, "00", false, true))
				.addChannel(new BmsChannel(2, BmsType.INTEGER, null, "0", false, false))
				.addChannel(new BmsChannel(3, BmsType.ARRAY36, "#bpm", "00", false, true))
				.setInitialBpmMeta("#bpm")
				.setBpmChannel(3);
		assertThrows(IllegalStateException.class, () -> b.create());
	}

	// create()
	// IllegalStateException 指定されたBPM変更チャンネルの参照先メタ情報の初期値が{@link BmsSpec#BPM_MAX}超過
	@Test
	public void testCreate_BpmChannel_OverflowDefaultValue() {
		var b = new BmsSpecBuilder()
				.addMeta(BmsMeta.single("#bpm", BmsType.FLOAT, "130", 0, true))
				.addMeta(BmsMeta.indexed("#bpm", BmsType.FLOAT, String.valueOf(BmsSpec.BPM_MAX + 0.1), 0, true))
				.addChannel(new BmsChannel(1, BmsType.ARRAY36, null, "00", false, true))
				.addChannel(new BmsChannel(2, BmsType.INTEGER, null, "0", false, false))
				.addChannel(new BmsChannel(3, BmsType.ARRAY36, "#bpm", "00", false, true))
				.setInitialBpmMeta("#bpm")
				.setBpmChannel(3);
		assertThrows(IllegalStateException.class, () -> b.create());
	}

	// create()
	// IllegalStateException 指定された譜面停止チャンネルの参照先メタ情報の初期値が{@link BmsSpec#STOP_MIN}未満
	@Test
	public void testCreate_StopChannel_UnderflowDefaultValue() {
		var b = new BmsSpecBuilder()
				.addMeta(BmsMeta.single("#bpm", BmsType.FLOAT, "130", 0, true))
				.addMeta(BmsMeta.indexed("#stop", BmsType.FLOAT, String.valueOf(BmsSpec.STOP_MIN - 1), 0, true))
				.addChannel(new BmsChannel(1, BmsType.ARRAY36, null, "00", false, true))
				.addChannel(new BmsChannel(2, BmsType.INTEGER, null, "0", false, false))
				.addChannel(new BmsChannel(3, BmsType.ARRAY36, "#stop", "00", false, true))
				.setInitialBpmMeta("#bpm")
				.setStopChannel(3);
		assertThrows(IllegalStateException.class, () -> b.create());
	}

	// create()
	// IllegalStateException 指定された譜面停止チャンネルの参照先メタ情報の初期値が{@link BmsSpec#STOP_MAX}超過
	@Test
	public void testCreate_StopChannel_OverflowDefaultValue() {
		var b = new BmsSpecBuilder()
				.addMeta(BmsMeta.single("#bpm", BmsType.FLOAT, "130", 0, true))
				.addMeta(BmsMeta.indexed("#stop", BmsType.FLOAT, String.valueOf((long)BmsSpec.STOP_MAX + 1L), 0, true))
				.addChannel(new BmsChannel(1, BmsType.ARRAY36, null, "00", false, true))
				.addChannel(new BmsChannel(2, BmsType.INTEGER, null, "0", false, false))
				.addChannel(new BmsChannel(3, BmsType.ARRAY36, "#stop", "00", false, true))
				.setInitialBpmMeta("#bpm")
				.setStopChannel(3);
		assertThrows(IllegalStateException.class, () -> b.create());
	}

	private BmsSpecBuilder createUsedBuilder() {
		var b = new BmsSpecBuilder();
		b.addMeta(BmsMeta.single("#bpm", BmsType.FLOAT, "130.0", 0, true));
		b.addChannel(new BmsChannel(1, BmsType.ARRAY36, null, "00", false, true));
		b.setInitialBpmMeta("#bpm");
		b.create();
		return b;
	}
}
