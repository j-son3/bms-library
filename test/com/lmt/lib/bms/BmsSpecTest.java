package com.lmt.lib.bms;

import static org.junit.Assert.*;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.junit.Test;

public class BmsSpecTest {
	// getSingleMeta(String)
	// 正常
	@Test
	public void testGetSingleMeta_001() {
		var s = createSpec();
		BmsMeta m;

		m = s.getSingleMeta("#genre");
		assertNotNull(m);
		assertEquals("#genre", m.getName());
		assertEquals(BmsUnit.SINGLE, m.getUnit());

		m = s.getSingleMeta("#not_exist_meta");
		assertNull(m);

		m = s.getSingleMeta("#subartist");  // 単位が異なる
		assertNull(m);

		m = s.getSingleMeta("#wav");  // 単位が異なる
		assertNull(m);
	}

	// getSingleMeta(String)
	// NullPointerException nameがnull
	@Test
	public void testGetSingleMeta_002() {
		var s = createSpec();
		assertThrows(NullPointerException.class, () -> s.getSingleMeta(null));
	}

	// getMultipleMeta(String)
	// 正常
	@Test
	public void testGetMultipleMeta_001() {
		var s = createSpec();
		BmsMeta m;

		m = s.getMultipleMeta("#comment");
		assertNotNull(m);
		assertEquals("#comment", m.getName());
		assertEquals(BmsUnit.MULTIPLE, m.getUnit());

		m = s.getMultipleMeta("#not_exist_meta");
		assertNull(m);

		m = s.getMultipleMeta("#genre");  // 単位が異なる
		assertNull(m);

		m = s.getMultipleMeta("#wav");  // 単位が異なる
		assertNull(m);
	}

	// getMultipleMeta(String)
	// NullPointerException nameがnull
	@Test
	public void testGetMultipleMeta_002() {
		var s = createSpec();
		assertThrows(NullPointerException.class, () -> s.getMultipleMeta(null));
	}

	// getIndexedMeta(String)
	// 正常
	@Test
	public void testGetIndexedMeta_001() {
		var s = createSpec();
		BmsMeta m;

		m = s.getIndexedMeta("#wav");
		assertNotNull(m);
		assertEquals("#wav", m.getName());
		assertEquals(BmsUnit.INDEXED, m.getUnit());

		m = s.getIndexedMeta("#not_exist_meta");
		assertNull(m);

		m = s.getIndexedMeta("#genre");  // 単位が異なる
		assertNull(m);

		m = s.getIndexedMeta("#comment");  // 単位が異なる
		assertNull(m);
	}

	// getIndexedMeta(String)
	// NullPointerException nameがnull
	@Test
	public void testGetIndexedMeta_002() {
		var s = createSpec();
		assertThrows(NullPointerException.class, () -> s.getIndexedMeta(null));
	}

	// getMeta(BmsMetaKey)
	// 正常
	@Test
	public void testGetMetaBmsMetaKey_001() {
		var s = createSpec();
		BmsMeta m;

		m = s.getMeta(new BmsMetaKey("#genre", BmsUnit.SINGLE));
		assertNotNull(m);
		assertEquals("#genre", m.getName());
		assertEquals(BmsUnit.SINGLE, m.getUnit());
		m = s.getMeta(new BmsMetaKey("#genre", BmsUnit.MULTIPLE));
		assertNull(m);
		m = s.getMeta(new BmsMetaKey("#genre", BmsUnit.INDEXED));
		assertNull(m);

		m = s.getMeta(new BmsMetaKey("#comment", BmsUnit.MULTIPLE));
		assertNotNull(m);
		assertEquals("#comment", m.getName());
		assertEquals(BmsUnit.MULTIPLE, m.getUnit());
		m = s.getMeta(new BmsMetaKey("#comment", BmsUnit.SINGLE));
		assertNull(m);
		m = s.getMeta(new BmsMetaKey("#comment", BmsUnit.INDEXED));
		assertNull(m);

		m = s.getMeta(new BmsMetaKey("#wav", BmsUnit.INDEXED));
		assertNotNull(m);
		assertEquals("#wav", m.getName());
		assertEquals(BmsUnit.INDEXED, m.getUnit());
		m = s.getMeta(new BmsMetaKey("#wav", BmsUnit.SINGLE));
		assertNull(m);
		m = s.getMeta(new BmsMetaKey("#wav", BmsUnit.MULTIPLE));
		assertNull(m);

		m = s.getMeta(new BmsMetaKey("#not_exist_meta", BmsUnit.SINGLE));
		assertNull(m);
		m = s.getMeta(new BmsMetaKey("#not_exist_meta", BmsUnit.MULTIPLE));
		assertNull(m);
		m = s.getMeta(new BmsMetaKey("#not_exist_meta", BmsUnit.INDEXED));
		assertNull(m);
	}

	// getMeta(BmsMetaKey)
	// NullPointerException keyがnull
	@Test
	public void testGetMetaBmsMetaKey_002() {
		var s = createSpec();
		assertThrows(NullPointerException.class, () -> s.getMeta(null));
	}

	// getMeta(String, BmsUnit)
	// 正常
	@Test
	public void testGetMetaStringBmsUnit_001() {
		var s = createSpec();
		BmsMeta m;

		m = s.getMeta("#genre", BmsUnit.SINGLE);
		assertNotNull(m);
		assertEquals("#genre", m.getName());
		assertEquals(BmsUnit.SINGLE, m.getUnit());
		m = s.getMeta("#genre", BmsUnit.MULTIPLE);
		assertNull(m);
		m = s.getMeta("#genre", BmsUnit.INDEXED);
		assertNull(m);

		m = s.getMeta("#comment", BmsUnit.MULTIPLE);
		assertNotNull(m);
		assertEquals("#comment", m.getName());
		assertEquals(BmsUnit.MULTIPLE, m.getUnit());
		m = s.getMeta("#comment", BmsUnit.SINGLE);
		assertNull(m);
		m = s.getMeta("#comment", BmsUnit.INDEXED);
		assertNull(m);

		m = s.getMeta("#wav", BmsUnit.INDEXED);
		assertNotNull(m);
		assertEquals("#wav", m.getName());
		assertEquals(BmsUnit.INDEXED, m.getUnit());
		m = s.getMeta("#wav", BmsUnit.SINGLE);
		assertNull(m);
		m = s.getMeta("#wav", BmsUnit.MULTIPLE);
		assertNull(m);

		m = s.getMeta("#not_exist_meta", BmsUnit.SINGLE);
		assertNull(m);
		m = s.getMeta("#not_exist_meta", BmsUnit.MULTIPLE);
		assertNull(m);
		m = s.getMeta("#not_exist_meta", BmsUnit.INDEXED);
		assertNull(m);
	}

	// getMeta(String, BmsUnit)
	// NullPointerException nameがnull
	@Test
	public void testGetMetaStringBmsUnit_002() {
		var s = createSpec();
		assertThrows(NullPointerException.class, () -> s.getMeta(null, BmsUnit.SINGLE));
	}

	// getMeta(String, BmsUnit)
	// NullPointerException unitがnull
	@Test
	public void testGetMetaStringBmsUnit_003() {
		var s = createSpec();
		assertThrows(NullPointerException.class, () -> s.getMeta("#wav", null));
	}

	// containsMeta(BmsMetaKey)
	// 正常
	@Test
	public void testContainsMetaBmsMetaKey_001() {
		var s = createSpec();

		assertEquals(true, s.containsMeta(new BmsMetaKey("#genre", BmsUnit.SINGLE)));
		assertEquals(false, s.containsMeta(new BmsMetaKey("#genre", BmsUnit.MULTIPLE)));
		assertEquals(false, s.containsMeta(new BmsMetaKey("#genre", BmsUnit.INDEXED)));

		assertEquals(false, s.containsMeta(new BmsMetaKey("#comment", BmsUnit.SINGLE)));
		assertEquals(true, s.containsMeta(new BmsMetaKey("#comment", BmsUnit.MULTIPLE)));
		assertEquals(false, s.containsMeta(new BmsMetaKey("#comment", BmsUnit.INDEXED)));

		assertEquals(false, s.containsMeta(new BmsMetaKey("#wav", BmsUnit.SINGLE)));
		assertEquals(false, s.containsMeta(new BmsMetaKey("#wav", BmsUnit.MULTIPLE)));
		assertEquals(true, s.containsMeta(new BmsMetaKey("#wav", BmsUnit.INDEXED)));

		// 同じ名前のメタ情報がSINGLE/MULTIPLEとINDEXEDの間で存在する
		assertEquals(true, s.containsMeta(new BmsMetaKey("#bpm", BmsUnit.SINGLE)));
		assertEquals(false, s.containsMeta(new BmsMetaKey("#bpm", BmsUnit.MULTIPLE)));
		assertEquals(true, s.containsMeta(new BmsMetaKey("#bpm", BmsUnit.INDEXED)));
	}

	// containsMeta(BmsMetaKey)
	// NullPointerException keyがnull
	@Test
	public void testContainsMetaBmsMetaKey_002() {
		var s = createSpec();
		assertThrows(NullPointerException.class, () -> s.containsMeta(null));
	}

	// containsMeta(String, BmsUnit)
	// 正常
	@Test
	public void testContainsMeta_001() {
		var s = createSpec();

		assertEquals(true, s.containsMeta("#genre", BmsUnit.SINGLE));
		assertEquals(false, s.containsMeta("#genre", BmsUnit.MULTIPLE));
		assertEquals(false, s.containsMeta("#genre", BmsUnit.INDEXED));

		assertEquals(false, s.containsMeta("#comment", BmsUnit.SINGLE));
		assertEquals(true, s.containsMeta("#comment", BmsUnit.MULTIPLE));
		assertEquals(false, s.containsMeta("#comment", BmsUnit.INDEXED));

		assertEquals(false, s.containsMeta("#wav", BmsUnit.SINGLE));
		assertEquals(false, s.containsMeta("#wav", BmsUnit.MULTIPLE));
		assertEquals(true, s.containsMeta("#wav", BmsUnit.INDEXED));

		// 同じ名前のメタ情報がSINGLE/MULTIPLEとINDEXEDの間で存在する
		assertEquals(true, s.containsMeta("#bpm", BmsUnit.SINGLE));
		assertEquals(false, s.containsMeta("#bpm", BmsUnit.MULTIPLE));
		assertEquals(true, s.containsMeta("#bpm", BmsUnit.INDEXED));
	}

	// containsMeta(String, BmsUnit)
	// NullPointerException nameがnull
	@Test
	public void testContainsMeta_002() {
		var s = createSpec();
		assertThrows(NullPointerException.class, () -> s.containsMeta(null, BmsUnit.SINGLE));
	}

	// containsMeta(String, BmsUnit)
	// NullPointerException unitがnull
	@Test
	public void testContainsMeta_003() {
		var s = createSpec();
		assertThrows(NullPointerException.class, () -> s.containsMeta("#wav", null));
	}

	// getMetas()
	@Test
	public void testGetMetas() {
		var s = createSpec();
		var metas = s.getMetas();

		// 取得したメタ情報は仕様内に全て存在すること
		metas.forEach(m -> {
			assertNotNull(m);
			assertEquals(true, s.containsMeta(m.getName(), m.getUnit()));
		});

		// メタ情報の格納順が、Order1->Order2で昇順になっていること
		// 任意型メタ情報は含まれていないこと
		var lastOrder1 = Integer.MIN_VALUE;
		var lastOrder2 = Integer.MIN_VALUE;
		for (var m : metas) {
			var order1 = m.getOrder();
			var order2 = m.getOrder2();
			lastOrder2 = (order1 != lastOrder1) ? Integer.MIN_VALUE : lastOrder2;
			assertTrue(order1 >= lastOrder1);
			assertTrue(order2 >= lastOrder2);
			lastOrder1 = order1;
			lastOrder2 = order2;
			assertFalse(m.isObjectType());
		}
	}

	// getMetas(boolean)
	// 任意型メタ情報を含まない、ソートキーによる並び替え済みのメタ情報リストが取得できること
	@Test
	public void testGetMetasBoolean_WithoutObject() {
		var s = createSpec();
		var metas = s.getMetas(false);
		var lastOrder1 = Integer.MIN_VALUE;
		var lastOrder2 = Integer.MIN_VALUE;
		for (var m : metas) {
			assertTrue(s.containsMeta(m));
			var order1 = m.getOrder();
			var order2 = m.getOrder2();
			lastOrder2 = (order1 != lastOrder1) ? Integer.MIN_VALUE : lastOrder2;
			assertTrue(order1 >= lastOrder1);
			assertTrue(order2 >= lastOrder2);
			lastOrder1 = order1;
			lastOrder2 = order2;
			assertFalse(m.isObjectType());
		}
	}

	// getMetas(boolean)
	// 任意型メタ情報を含む、ソートキーによる並び替え済みのメタ情報リストが取得できること
	@Test
	public void testGetMetasBoolean_WithinObject() {
		var s = createSpec();
		var metas = s.getMetas(true);
		var lastOrder1 = Integer.MIN_VALUE;
		var lastOrder2 = Integer.MIN_VALUE;
		var foundObject1 = false;
		var foundObject2 = false;
		for (var m : metas) {
			assertTrue(s.containsMeta(m));
			var order1 = m.getOrder();
			var order2 = m.getOrder2();
			lastOrder2 = (order1 != lastOrder1) ? Integer.MIN_VALUE : lastOrder2;
			assertTrue(order1 >= lastOrder1);
			assertTrue(order2 >= lastOrder2);
			lastOrder1 = order1;
			lastOrder2 = order2;
			foundObject1 = foundObject1 || (m.isObjectType() && m.getName().equals("#objectmeta1_1"));
			foundObject2 = foundObject2 || (m.isObjectType() && m.getName().equals("#objectmeta1_2"));
		}
		assertTrue(foundObject1);
		assertTrue(foundObject2);
	}

	// metas()
	// ソートキー順に全てのメタ情報が走査されること
	@Test
	public void testMetas() throws Exception {
		var s = createSpec();
		Map<String, BmsMeta> singles = Tests.getf(s, "mSingleMetas");
		Map<String, BmsMeta> multiples = Tests.getf(s, "mMultipleMetas");
		Map<String, BmsMeta> indices = Tests.getf(s, "mIndexedMetas");
		var lastOrder = new int[] { Integer.MIN_VALUE, Integer.MIN_VALUE };
		var remaining = new HashSet<BmsMeta>();
		remaining.addAll(singles.values());
		remaining.addAll(multiples.values());
		remaining.addAll(indices.values());
		s.metas().forEach(m -> {
			assertTrue(s.containsMeta(m));
			var order1 = m.getOrder();
			var order2 = m.getOrder2();
			lastOrder[1] = (order1 != lastOrder[0]) ? Integer.MIN_VALUE : lastOrder[1];
			assertTrue(order1 >= lastOrder[0]);
			assertTrue(order2 >= lastOrder[1]);
			lastOrder[0] = order1;
			lastOrder[1] = order2;
			assertTrue(remaining.remove(m));
		});
		assertEquals(0, remaining.size());
	}

	// getChannel(int)
	@Test
	public void testGetChannel() {
		var s = createSpec();
		BmsChannel c;

		c = s.getChannel(1);
		assertNotNull(c);
		assertEquals(1, c.getNumber());

		c = s.getChannel(999999);
		assertNull(c);

		// マイナス値を指定しても別にOK
		c = s.getChannel(-1);
		assertNull(c);
	}

	// containsChannel(int)
	@Test
	public void testContainsChannel() {
		var s = createSpec();
		assertTrue(s.containsChannel(1));
		assertTrue(s.containsChannel(2));
		assertFalse(s.containsChannel(999999));
		assertFalse(s.containsChannel(-1));
	}

	// getChannels()
	@Test
	public void testGetChannels() {
		var s = createSpec();
		var chs = s.getChannels();

		// 取得したチャンネルの内容をチェックする
		chs.forEach(m -> {
			// nullではないこと
			assertNotNull(m);
			// BMS仕様に含まれるチャンネルであること
			assertTrue(s.containsChannel(m.getNumber()));
			// ユーザーチャンネルは含まれないこと
			assertTrue(m.isSpec());
			assertFalse(m.isUser());
		});

		// 取得したチャンネルは番号昇順で格納されていること
		var lastNumber = BmsSpec.CHANNEL_MIN - 1;
		for (var ch : chs) {
			var number = ch.getNumber();
			assertTrue(number > lastNumber);
			lastNumber = number;
		}
	}

	// getChannels(boolean)
	// BMS仕様として定義されたチャンネルのリストをチャンネル番号昇順で取得できること(ユーザーチャンネルを含まない)
	@Test
	public void testGetChannelsBoolean_WithoutUser() {
		var s = createSpec();
		var chs = s.getChannels(false);
		var lastNumber = BmsSpec.CHANNEL_MIN - 1;
		for (var c : chs) {
			var chNum = c.getNumber();
			assertNotNull(c);
			assertTrue(s.containsChannel(chNum));
			assertTrue(c.isSpec());
			assertFalse(c.isUser());
			assertTrue(chNum > lastNumber);
			lastNumber = chNum;
		}
	}

	// getChannels(boolean)
	// BMS仕様として定義されたチャンネルのリストをチャンネル番号昇順で取得できること(ユーザーチャンネルを含む)
	@Test
	public void testGetChannelsBoolean_WithinUser() {
		var s = createSpec();
		var chs = s.getChannels(true);
		var lastNumber = BmsSpec.CHANNEL_MIN - 1;
		var foundUser1 = false;
		var foundUser2 = false;
		for (var c : chs) {
			var chNum = c.getNumber();
			assertNotNull(c);
			assertTrue(s.containsChannel(chNum));
			assertTrue(chNum > lastNumber);
			lastNumber = chNum;
			foundUser1 = foundUser1 || (c.isUser() && c.getNumber() == 2001);
			foundUser2 = foundUser2 || (c.isUser() && c.getNumber() == 2002);
		}
		assertTrue(foundUser1);
		assertTrue(foundUser2);
	}

	// channels()
	// 全てのチャンネルがチャンネル番号の若い順に走査されること
	@Test
	public void testChannels() throws Exception {
		var s = createSpec();
		Map<Integer, BmsChannel> allChannels = Tests.getf(s, "mChannels");
		var remaining = new HashSet<>(allChannels.values());
		var lastNumber = new int[] { BmsSpec.CHANNEL_MIN - 1 };
		s.channels().forEach(c -> {
			var chNum = c.getNumber();
			assertNotNull(c);
			assertTrue(remaining.remove(c));
			assertTrue(chNum > lastNumber[0]);
			lastNumber[0] = chNum;
		});
		assertEquals(0, remaining.size());
	}

	// getBaseChangerMeta()
	// 基数選択メタ情報が設定されていると、当該メタ情報を返すこと
	@Test
	public void testGetBaseChangerMeta_Set() {
		var s = createSpec(true, false, false, false);
		var m = s.getBaseChangerMeta();
		assertNotNull(m);
		assertEquals("#base", m.getName());
	}

	// getBaseChangerMeta()
	// 基数選択メタ情報が設定されていないと、nullを返すこと
	@Test
	public void testGetBaseChangerMeta_NotSet() {
		var s = createSpec(false, false, false, false);
		var m = s.getBaseChangerMeta();
		assertNull(m);
	}

	// hasBaseChanger()
	// 基数選択メタ情報が設定されていると、trueを返すこと
	@Test
	public void testHasBaseChanger_True() {
		var s = createSpec(true, false, false, false);
		assertTrue(s.hasBaseChanger());
	}

	// hasBaseChanger()
	// 基数選択メタ情報が設定されていないと、falseを返すこと
	@Test
	public void testHasBaseChanger_False() {
		var s = createSpec(false, false, false, false);
		assertFalse(s.hasBaseChanger());
	}

	// getLengthChannel()
	@Test
	public void testGetLengthChannel() {
		BmsSpec s;
		s = createSpec(false, true, false, false);
		assertNotNull(s.getLengthChannel());
		s = createSpec(false, false, true, true);
		assertNull(s.getLengthChannel());
	}

	// getBpmChannel(int)
	@Test
	public void testGetBpmChannelInt() {
		BmsSpec s;
		s = createSpec(false, false, true, false);
		assertNotNull(s.getBpmChannel(8));
		assertNotNull(s.getBpmChannel(1008));
		s = createSpec(false, false, false, false);
		assertNull(s.getBpmChannel(8));
		assertNull(s.getBpmChannel(1008));
	}

	// getBpmChannel(int, boolean)
	// 正常
	@Test
	public void testGetBpmChannelIntBoolean_Normal() {
		var s = createSpec(false, false, true, false);
		assertNotNull(s.getBpmChannel(8, false));
		assertNotNull(s.getBpmChannel(1008, false));
		assertNull(s.getBpmChannel(1295));
		assertNotNull(s.getBpmChannel(0, true));
		assertNotNull(s.getBpmChannel(1, true));
		assertNull(s.getBpmChannel(-1, true));
		assertNull(s.getBpmChannel(2, true));
	}

	// getBpmChannelCount()
	// 正常
	@Test
	public void testGetBpmChannelCount_Normal() {
		BmsSpec s;
		s = createSpec(false, false, true, false);
		assertEquals(2, s.getBpmChannelCount());
		s = createSpec(false, false, false, false);
		assertEquals(0, s.getBpmChannelCount());
	}

	// hasBpmChannel()
	// 正常
	@Test
	public void testHasBpmChannel_Normal() {
		BmsSpec s;
		s = createSpec(false, false, true, false);
		assertTrue(s.hasBpmChannel());
		s = createSpec(false, false, false, false);
		assertFalse(s.hasBpmChannel());
	}

	// getStopChannel(int)
	@Test
	public void testGetStopChannelInt() {
		BmsSpec s;
		s = createSpec(false, false, false, true);
		assertNotNull(s.getStopChannel(9));
		assertNotNull(s.getStopChannel(1009));
		s = createSpec(false, false, false, false);
		assertNull(s.getStopChannel(9));
		assertNull(s.getStopChannel(1009));
	}

	// getStopChannel(int, boolean)
	// 正常
	@Test
	public void testGetStopChannelIntBoolean_Normal() {
		var s = createSpec(false, false, false, true);
		assertNotNull(s.getStopChannel(9, false));
		assertNotNull(s.getStopChannel(1009, false));
		assertNull(s.getStopChannel(1295));
		assertNotNull(s.getStopChannel(0, true));
		assertNotNull(s.getStopChannel(1, true));
		assertNull(s.getStopChannel(-1, true));
		assertNull(s.getStopChannel(2, true));
	}

	// getStopChannelCount()
	// 正常
	@Test
	public void testGetStopChannelCount_Normal() {
		BmsSpec s;
		s = createSpec(false, false, false, true);
		assertEquals(2, s.getStopChannelCount());
		s = createSpec(false, false, false, false);
		assertEquals(0, s.getStopChannelCount());
	}

	// hasStopChannel()
	// 正常
	@Test
	public void testHasStopChannel_Normal() {
		BmsSpec s;
		s = createSpec(false, false, false, true);
		assertTrue(s.hasStopChannel());
		s = createSpec(false, false, false, false);
		assertFalse(s.hasStopChannel());
	}

	// setStandardCharset(Charset)
	@SuppressWarnings("deprecation")
	@Test
	public void testSetStandardCharset() {
		var orgCs = BmsSpec.getStandardCharset();
		var newCs = Charset.forName("utf-8");

		BmsSpec.setStandardCharset(newCs);
		assertEquals(newCs, BmsSpec.getStandardCharset());

		BmsSpec.setStandardCharset(orgCs);
	}

	private static BmsSpec createSpec() {
		return createSpec(true, true, true, true);
	}

	private static BmsSpec createSpec(boolean hasBase, boolean hasLen, boolean hasBpm, boolean hasStop) {
		var smetas = new HashMap<String, BmsMeta>();
		smetas.put("#base", BmsMeta.single("#base", BmsType.INTEGER, "36", 0, true));
		smetas.put("#genre", BmsMeta.single("#genre", BmsType.STRING, "", 0, true));
		smetas.put("#title", BmsMeta.single("#title", BmsType.STRING, "", 0, true));
		smetas.put("#subtitle", BmsMeta.single("#subtitle", BmsType.STRING, "", 0, true));
		smetas.put("#artist", BmsMeta.single("#artist", BmsType.STRING, "", 0, true));
		smetas.put("#bpm", BmsMeta.single("#bpm", BmsType.FLOAT, "130.0", 0, true));

		smetas.put("#sorttest1_1", new BmsMeta("#sorttest1_1", BmsUnit.SINGLE, BmsType.STRING, "", 1, false));
		smetas.put("#sorttest1_2", new BmsMeta("#sorttest1_2", BmsUnit.SINGLE, BmsType.STRING, "", 1, false));
		smetas.put("#sorttest1_3", new BmsMeta("#sorttest1_3", BmsUnit.SINGLE, BmsType.STRING, "", 1, false));

		smetas.put("#objectmeta1_1", BmsMeta.object("#objectmeta1_1", BmsUnit.SINGLE));
		smetas.put("#objectmeta1_2", BmsMeta.object("#objectmeta1_2", BmsUnit.SINGLE));

		smetas.put("#sorttest2_1", new BmsMeta("#sorttest2_1", BmsUnit.SINGLE, BmsType.STRING, "", 2, false));
		smetas.put("#sorttest2_2", new BmsMeta("#sorttest2_2", BmsUnit.SINGLE, BmsType.STRING, "", 2, false));

		var mmetas = new HashMap<String, BmsMeta>();
		mmetas.put("#subartist", BmsMeta.multiple("#subartist", BmsType.STRING, "", 0, true));
		mmetas.put("#comment", new BmsMeta("#comment", BmsUnit.MULTIPLE, BmsType.STRING, "", 0, false));

		var imetas = new HashMap<String, BmsMeta>();
		imetas.put("#wav", new BmsMeta("#wav", BmsUnit.INDEXED, BmsType.BASE36, "00", 0, false));
		imetas.put("#bmp", new BmsMeta("#bmp", BmsUnit.INDEXED, BmsType.BASE36, "00", 0, false));
		imetas.put("#bpm", new BmsMeta("#bpm", BmsUnit.INDEXED, BmsType.FLOAT, "130.0", 0, false));
		imetas.put("#stop", new BmsMeta("#stop", BmsUnit.INDEXED, BmsType.INTEGER, "0", 0, false));

		var chs = new HashMap<Integer, BmsChannel>();
		chs.put(1, new BmsChannel(1, BmsType.ARRAY36, "#wav", "", true, false));   // BGM
		chs.put(2, new BmsChannel(2, BmsType.FLOAT, null, "1.0", false, true)); // 小節長変更
		chs.put(8, new BmsChannel(8, BmsType.ARRAY36, "#bpm", "", false, true));   // BPM変更
		chs.put(1008, new BmsChannel(1008, BmsType.ARRAY36, "#bpm", "", false, true));  // BPM変更2
		chs.put(9, new BmsChannel(9, BmsType.ARRAY36, "#stop", "", false, true));  // 譜面停止
		chs.put(1009, new BmsChannel(1009, BmsType.ARRAY36, "#stop", "", false, true));  // 譜面停止
		for (var i = 0; i < 9; i++) {
			chs.put(11 + i, new BmsChannel(11 + i, BmsType.ARRAY36, "#wav", "00", false, true));  // 1P可視
			chs.put(21 + i, new BmsChannel(21 + i, BmsType.ARRAY36, "#wav", "00", false, true));  // 1P不可視
			chs.put(31 + i, new BmsChannel(31 + i, BmsType.ARRAY36, "#wav", "00", false, true));  // 2P可視
			chs.put(41 + i, new BmsChannel(41 + i, BmsType.ARRAY36, "#wav", "00", false, true));  // 2P不可視
		}
		chs.put(2001, BmsChannel.user(2001, BmsType.OBJECT, null, null, false));
		chs.put(2002, BmsChannel.user(2002, BmsType.ARRAY36, null, "00", false));

		BmsMeta base;
		BmsChannel len;
		BmsChannel[] bpm, stop;
		base = hasBase ? smetas.get("#base") : null;
		len = hasLen ? chs.get(2) : null;
		bpm = hasBpm ? new BmsChannel[] { chs.get(8), chs.get(1008) } : new BmsChannel[0];
		stop = hasStop ? new BmsChannel[] { chs.get(9), chs.get(1009) } : new BmsChannel[0];

		return new BmsSpec(smetas, mmetas, imetas, chs, smetas.get("#bpm"), base, len, bpm, stop);
	}
}
