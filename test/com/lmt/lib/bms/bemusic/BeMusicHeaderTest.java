package com.lmt.lib.bms.bemusic;

import static org.junit.Assert.*;

import org.junit.Test;

import com.lmt.lib.bms.BmsContent;
import com.lmt.lib.bms.BmsInt;
import com.lmt.lib.bms.BmsMetaKey;
import com.lmt.lib.bms.BmsSpec;

public class BeMusicHeaderTest {
	private static class HeaderForOfTest extends BeMusicHeader {
		boolean calledOnCreate = false;
		@Override protected void onCreate(BmsContent content, int flags) { calledOnCreate = true; }
	}

	// BeMusicHeader()
	// 値が空のヘッダ定義が構築されること
	@Test
	public void testBeMusicHeader1() {
		var h = new BeMusicHeader();
		assertEquals(BeMusicPlayer.OTHER, h.getPlayer());
		assertEquals("", h.getGenre());
		assertEquals("", h.getTitle());
		assertEquals("", h.getSubTitle());
		assertEquals("", h.getArtist());
		assertEquals("", h.getSubArtist(""));
		assertEquals(0.0, h.getInitialBpm(), 0.0);
		assertEquals(BeMusicDifficulty.OTHER, h.getDifficulty());
		assertEquals("", h.getChartName());
		assertEquals("", h.getPlayLevelRaw());
		assertEquals(0.0, h.getPlayLevel(), 0.0);
		assertEquals(BeMusicRank.OTHER, h.getRank());
		assertNull(h.getDefExRank());
		assertEquals(0.0, h.getTotal(), 0.0);
		assertEquals("", h.getComment());
		assertEquals("", h.getBanner());
		assertEquals("", h.getStageFile());
		assertEquals("", h.getBackBmp());
		assertEquals("", h.getEyecatch());
		assertEquals("", h.getPreview());
		assertTrue(h.getLnObjs().isEmpty());
		assertEquals(BeMusicLongNoteMode.LN, h.getLnMode());
		assertEquals("", h.getUrl());
		assertEquals("", h.getEmail());
		assertTrue(h.getWavs().isEmpty());
		assertTrue(h.getBmps().isEmpty());
		assertTrue(h.getBpms().isEmpty());
		assertTrue(h.getStops().isEmpty());
		assertTrue(h.getScrolls().isEmpty());
		assertTrue(h.getTexts().isEmpty());
	}

	// BeMusicHeader(BmsContent)
	// 正常：サポートする全てのヘッダ定義が収集されること
	@Test
	public void testBeMusicHeader2_Normal() {
		var c = contentForConstructor();
		var h = new BeMusicHeader(c);
		assertHeaderForConstructorAll(h);
	}

	// BeMusicHeader(BmsContent)
	// NullPointerException contentがnull
	@Test
	public void testBeMusicHeader2_Exception_NullContent() {
		assertThrows(NullPointerException.class, () -> { new BeMusicHeader(null); });
	}

	// BeMusicHeader(BmsContent, int)
	// 正常：全ての収集フラグをOFFにすると対象のヘッダ定義が全く収集されないこと
	@Test
	public void testBeMusicHeader3_Normal_AllFlagsOff() {
		var c = contentForConstructor();
		var h = new BeMusicHeader(c, BeMusicHeader.NONE);
		assertHeaderForConstructorNone(h);
	}

	// BeMusicHeader(BmsContent, int)
	// 正常：全ての収集フラグをONにすると全てのヘッダ定義が収集されること
	@Test
	public void testBeMusicHeader3_Normal_AllFlagsOn() {
		var c = contentForConstructor();
		var h = new BeMusicHeader(c, BeMusicHeader.ALL);
		assertHeaderForConstructorAll(h);
	}

	// BeMusicHeader(BmsContent, int)
	// NullPointerException contentがnull
	@Test
	public void testBeMusicHeader3_Exception_NullContent() {
		assertThrows(NullPointerException.class, () -> { new BeMusicHeader(null, BeMusicHeader.ALL); });
	}

	// getBase()
	// 正常：期待する値が正常に取得できること
	@Test
	public void testGetBase() {
		var c = content();
		var h = (BeMusicHeader)null;
		c.edit(() -> c.setMeta(BeMusicMeta.BASE, 16));
		h = BeMusicHeader.of(c);
		assertEquals(16, h.getBase());
		c.edit(() -> c.setMeta(BeMusicMeta.BASE, 36));
		h = BeMusicHeader.of(c);
		assertEquals(36, h.getBase());
		c.edit(() -> c.setMeta(BeMusicMeta.BASE, 62));
		h = BeMusicHeader.of(c);
		assertEquals(62, h.getBase());
		c.edit(() -> c.setMeta(BeMusicMeta.BASE, null));
		h = BeMusicHeader.of(c);
		assertEquals(36, h.getBase());
	}

	// getPlayer()
	// 正常：期待する値が正常に取得できること
	@Test
	public void testGetPlayer() {
		var c = content();
		setSingle(c, BeMusicMeta.PLAYER, BeMusicPlayer.DOUBLE.getNativeValue());
		var h = new BeMusicHeader(c);
		assertEquals(BeMusicPlayer.DOUBLE, h.getPlayer());
	}

	// getGenre()
	// 正常：期待する値が正常に取得できること
	@Test
	public void testGetGenre() {
		var c = content();
		setSingle(c, BeMusicMeta.GENRE, "MyGenre");
		var h = new BeMusicHeader(c);
		assertEquals("MyGenre", h.getGenre());
	}

	// getTitle()
	// 正常：期待する値が正常に取得できること
	@Test
	public void testGetTitle() {
		var c = content();
		setSingle(c, BeMusicMeta.TITLE, "MyTitle");
		var h = new BeMusicHeader(c);
		assertEquals("MyTitle", h.getTitle());
	}

	// getSubTitle()
	// 正常：期待する値が正常に取得できること
	@Test
	public void testGetSubTitle() {
		var c = content();
		setSingle(c, BeMusicMeta.SUBTITLE, "MySubTitle");
		var h = new BeMusicHeader(c);
		assertEquals("MySubTitle", h.getSubTitle());
	}

	// getArtist()
	// 正常：期待する値が正常に取得できること
	@Test
	public void testGetArtist() {
		var c = content();
		setSingle(c, BeMusicMeta.ARTIST, "MyArtist");
		var h = new BeMusicHeader(c);
		assertEquals("MyArtist", h.getArtist());
	}

	// getSubArtist(String)
	// 正常：複数のサブアーティストをセパレータで結合した文字列が返されること
	@Test
	public void testGetSubArtist_Normal_MultiSubArtists() {
		var c = content();
		setMultiple(c, BeMusicMeta.SUBARTIST, "Michael", "Alex", "John");
		var h = new BeMusicHeader(c);
		assertEquals("Michael / Alex / John", h.getSubArtist(" / "));
	}

	// getSubArtist(String)
	// 正常：セパレータが空文字の場合複数のサブアーティストが単純に結合されて返されること
	@Test
	public void testGetSubArtist_Normal_EmptySeparator() {
		var c = content();
		setMultiple(c, BeMusicMeta.SUBARTIST, "Michael", "Alex", "John");
		var h = new BeMusicHeader(c);
		assertEquals("MichaelAlexJohn", h.getSubArtist(""));
	}

	// getSubArtist(String)
	// 正常：サブアーティストが0件の場合空文字列が返されること
	@Test
	public void testGetSubArtist_Normal_NoSubArtist() {
		var c = content();
		var h = new BeMusicHeader(c);
		assertEquals("", h.getSubArtist(","));
	}

	// getSubArtist(String)
	// 正常：サブアーティストが1件の場合そのサブアーティストが返されること
	@Test
	public void testGetSubArtist_Normal_OneSubArtist() {
		var c = content();
		setMultiple(c, BeMusicMeta.SUBARTIST, "Michael");
		var h = new BeMusicHeader(c);
		assertEquals("Michael", h.getSubArtist(","));
	}

	// getSubArtist(String)
	// NullPointerException separatorがnull
	@Test
	public void testGetSubArtist_Exception_NullSeparator() {
		var h = new BeMusicHeader(content());
		assertThrows(NullPointerException.class, () -> { h.getSubArtist(null); });
	}

	// getSubArtists()
	// 正常：複数のサブアーティストが定義順にリストで返されること
	@Test
	public void testGetSubArtists_Normal_MultiSubArtists() {
		var c = content();
		setMultiple(c, BeMusicMeta.SUBARTIST, "Michael", "Alex", "John");
		var h = new BeMusicHeader(c);
		var l = h.getSubArtists();
		assertEquals(3, l.size());
		assertEquals("Michael", l.get(0));
		assertEquals("Alex", l.get(1));
		assertEquals("John", l.get(2));
	}

	// getSubArtists()
	// 正常：サブアーティストが1件の場合そのサブアーティストだけがリストに格納され返されること
	@Test
	public void testGetSubArtists_Normal_OneSubArtist() {
		var c = content();
		setMultiple(c, BeMusicMeta.SUBARTIST, "Michael");
		var h = new BeMusicHeader(c);
		var l = h.getSubArtists();
		assertEquals(1, l.size());
		assertEquals("Michael", l.get(0));
	}

	// getSubArtists()
	// 正常：サブアーティストが0件の場合空のリストが返されること
	@Test
	public void testGetSubArtists_Normal_NoSubArtist() {
		var c = content();
		setMultiple(c, BeMusicMeta.SUBARTIST);
		var h = new BeMusicHeader(c);
		var l = h.getSubArtists();
		assertEquals(0, l.size());
	}

	// getInitialBpm()
	// 正常：期待する値が正常に取得できること
	@Test
	public void testGetInitialBpm() {
		var c = content();
		setSingle(c, BeMusicMeta.INITIAL_BPM, 170.0);
		var h = new BeMusicHeader(c);
		assertEquals(170.0, 0.0, h.getInitialBpm());
	}

	// getDifficulty()
	// 正常：期待する値が正常に取得できること
	@Test
	public void testGetDifficulty() {
		var c = content();
		setSingle(c, BeMusicMeta.DIFFICULTY, BeMusicDifficulty.ANOTHER.getNativeValue());
		var h = new BeMusicHeader(c);
		assertEquals(BeMusicDifficulty.ANOTHER, h.getDifficulty());
	}

	// getChartName()
	// 期待する値が正常に取得できること
	@Test
	public void testGetChartName_Normal() {
		var c = content();
		setSingle(c, BeMusicMeta.CHARTNAME, "7key another");
		var h = new BeMusicHeader(c);
		assertEquals("7key another", h.getChartName());
	}

	// getChartName()
	// 値が未設定だと初期値が取得できること
	@Test
	public void testGetChartName_Default() {
		var c = content();
		var h = new BeMusicHeader(c);
		assertEquals(BeMusicMeta.CHARTNAME.getDefaultValue(), h.getChartName());
	}

	// getPlayLevelRaw()
	// 正常：期待する値が正常に取得できること
	@Test
	public void testGetPlayLevelRaw() {
		var c = content();
		setSingle(c, BeMusicMeta.PLAYLEVEL, "123.567");
		var h = new BeMusicHeader(c);
		assertEquals("123.567", h.getPlayLevelRaw());
	}

	// getPlayLevelRaw()
	// 正常：数値以外の値を設定し取得できること
	@Test
	public void testGetPlayLevelRaw_Nan() {
		var c = content();
		setSingle(c, BeMusicMeta.PLAYLEVEL, "NotNumberString");
		var h = new BeMusicHeader(c);
		assertEquals("NotNumberString", h.getPlayLevelRaw());
	}

	// getPlayLevelRaw()
	// 正常：空文字を指定すると空文字が返ること
	@Test
	public void testGetPlayLevelRaw_Empty() {
		var c = content();
		setSingle(c, BeMusicMeta.PLAYLEVEL, "");
		var h = new BeMusicHeader(c);
		assertEquals("", h.getPlayLevelRaw());
	}

	// getPlayLevel()
	// 正常：期待する値が正常に取得できること
	@Test
	public void testGetPlayLevel() {
		var c = content();
		setSingle(c, BeMusicMeta.PLAYLEVEL, "11.5");
		var h = new BeMusicHeader(c);
		assertEquals(11.5, 0.0, h.getPlayLevel());
	}

	// getPlayLevel()
	// 正常：数値書式以外の文字列の場合は0を返すこと
	@Test
	public void testGetPlayLevel_Nan() {
		var c = content();
		setSingle(c, BeMusicMeta.PLAYLEVEL, "NaN12.0");
		var h = new BeMusicHeader(c);
		assertEquals(0.0, 0.0, h.getPlayLevel());
	}

	// getRank()
	// 正常：期待する値が正常に取得できること
	@Test
	public void testGetRank() {
		var c = content();
		setSingle(c, BeMusicMeta.RANK, BeMusicRank.HARD.getNativeValue());
		var h = new BeMusicHeader(c);
		assertEquals(BeMusicRank.HARD, h.getRank());
	}

	// getDefExRank()
	// 期待する値が正常に取得できること
	@Test
	public void testGetDefExRank_Normal() {
		var c = content();
		setSingle(c, BeMusicMeta.DEFEXRANK, 123.456);
		var h = new BeMusicHeader(c);
		assertEquals(123.456, h.getDefExRank(), 0.0);
	}

	// getDefExRank()
	// 値が未設定だとnullが返ること
	@Test
	public void testGetDefExRank_NotDefine() {
		var c = content();
		var h = new BeMusicHeader(c);
		assertNull(h.getDefExRank());
	}

	// getTotal()
	// 正常：期待する値が正常に取得できること
	@Test
	public void testGetTotal() {
		var c = content();
		setSingle(c, BeMusicMeta.TOTAL, 1510.0);
		var h = new BeMusicHeader(c);
		assertEquals(1510.0, 0.0, h.getTotal());
	}

	// getComment()
	// 正常：期待する値が正常に取得できること
	@Test
	public void testGetComment() {
		var c = content();
		var v = "This is a BMS";
		setSingle(c, BeMusicMeta.COMMENT, v);
		var h = new BeMusicHeader(c);
		assertEquals(v, h.getComment());
	}

	// getBanner()
	// 正常：期待する値が正常に取得できること
	@Test
	public void testGetBanner() {
		var c = content();
		setSingle(c, BeMusicMeta.BANNER, "banner.jpg");
		var h = new BeMusicHeader(c);
		assertEquals("banner.jpg", h.getBanner());
	}

	// getStageFile()
	// 正常：期待する値が正常に取得できること
	@Test
	public void testGetStageFile() {
		var c = content();
		setSingle(c, BeMusicMeta.STAGEFILE, "stagefile.png");
		var h = new BeMusicHeader(c);
		assertEquals("stagefile.png", h.getStageFile());
	}

	// getBackBmp()
	// 正常：期待する値が正常に取得できること
	@Test
	public void testGetBackBmp() {
		var c = content();
		setSingle(c, BeMusicMeta.BACKBMP, "backbmp.bmp");
		var h = new BeMusicHeader(c);
		assertEquals("backbmp.bmp", h.getBackBmp());
	}

	// getEyecatch()
	// 期待する値が正常に取得できること
	@Test
	public void testGetEyecatch_Normal() {
		var c = content();
		setSingle(c, BeMusicMeta.EYECATCH, "eyecatch.png");
		var h = new BeMusicHeader(c);
		assertEquals("eyecatch.png", h.getEyecatch());
	}

	// getEyecatch()
	// 値が未設定だと初期値が取得できること
	@Test
	public void testGetEyecatch_Default() {
		var c = content();
		var h = new BeMusicHeader(c);
		assertEquals(BeMusicMeta.EYECATCH.getDefaultValue(), h.getEyecatch());
	}

	// getPreview()
	// 正常：期待する値が正常に取得できること
	@Test
	public void testGetPreview() {
		var c = content();
		setSingle(c, BeMusicMeta.PREVIEW, "preview.mp3");
		var h = new BeMusicHeader(c);
		assertEquals("preview.mp3", h.getPreview());
	}

	// getLnObjs()
	// 正常：複数のLNOBJが定義順にリストで返されること
	@Test
	public void testGetLnObjs_Normal_MultiLnObjs() {
		var c = content();
		setMultiple(c, BeMusicMeta.LNOBJ, "XX", "YY", "ZZ");
		var h = new BeMusicHeader(c);
		var l = h.getLnObjs();
		assertEquals(3, l.size());
		assertEquals(Long.valueOf("XX", 36), l.get(0));
		assertEquals(Long.valueOf("YY", 36), l.get(1));
		assertEquals(Long.valueOf("ZZ", 36), l.get(2));
	}

	// getLnObjs()
	// 正常：LNOBJが1件の場合そのLNOBJだけがリストに格納され返されること
	@Test
	public void testGetLnObjs_Normal_OneLnObj() {
		var c = content();
		setMultiple(c, BeMusicMeta.LNOBJ, "XX");
		var h = new BeMusicHeader(c);
		var l = h.getLnObjs();
		assertEquals(1, l.size());
		assertEquals(Long.valueOf("XX", 36), l.get(0));
	}

	// getLnObjs()
	// 正常：LNOBJが0件の場合空のリストが返されること
	@Test
	public void testGetLnObjs_Normal_NoLnObj() {
		var c = content();
		var h = new BeMusicHeader(c);
		var l = h.getLnObjs();
		assertEquals(0, l.size());
	}

	// getLnMode()
	// #LNMODE未定義の場合はLNが返ること
	@Test
	public void testGetLnMode_Undefine() {
		var c = content();
		var h = new BeMusicHeader(c);
		assertEquals(BeMusicLongNoteMode.LN, h.getLnMode());
	}

	// getLnMode()
	// #LNMODEに既知の値を指定すると期待するロングノートモードが返ること
	@Test
	public void testGetLnMode_Normal() {
		var c = content();

		var lnMode = BeMusicLongNoteMode.LN;
		setSingle(c, BeMusicMeta.LNMODE, lnMode.getNativeValue());
		assertEquals(lnMode, new BeMusicHeader(c).getLnMode());

		lnMode = BeMusicLongNoteMode.CN;
		setSingle(c, BeMusicMeta.LNMODE, lnMode.getNativeValue());
		assertEquals(lnMode, new BeMusicHeader(c).getLnMode());

		lnMode = BeMusicLongNoteMode.HCN;
		setSingle(c, BeMusicMeta.LNMODE, lnMode.getNativeValue());
		assertEquals(lnMode, new BeMusicHeader(c).getLnMode());
	}

	// getLnMode()
	// #LNMODEに未知の値を指定するとLNが返ること
	@Test
	public void testGetLnMode_Unknown() {
		var c = content();

		setSingle(c, BeMusicMeta.LNMODE, Long.MIN_VALUE);
		assertEquals(BeMusicLongNoteMode.LN, new BeMusicHeader(c).getLnMode());

		setSingle(c, BeMusicMeta.LNMODE, Long.MAX_VALUE);
		assertEquals(BeMusicLongNoteMode.LN, new BeMusicHeader(c).getLnMode());
	}

	// getUrl()
	// 正常：期待する値が正常に取得できること
	@Test
	public void testGetUrl() {
		var c = content();
		setSingle(c, BeMusicMeta.URL, "http://www.lm-t.com/");
		var h = new BeMusicHeader(c);
		assertEquals("http://www.lm-t.com/", h.getUrl());
	}

	// getEmail()
	// 正常：期待する値が正常に取得できること
	@Test
	public void testGetEmail() {
		var c = content();
		setSingle(c, BeMusicMeta.EMAIL, "j-son@lm-t.com");
		var h = new BeMusicHeader(c);
		assertEquals("j-son@lm-t.com", h.getEmail());
	}

	// getWav(int)
	// 正常：存在する場合その値が、存在しない場合空文字が返されること
	@Test
	public void testGetWav() {
		var c = content();
		setIndexed(c, BeMusicMeta.WAV, imeta("01", "a.wav"), imeta("03", "b.ogg"), imeta("05", "c.mp3"));
		var h = new BeMusicHeader(c);
		assertEquals("a.wav", h.getWav(Integer.valueOf("01", 36)));
		assertEquals("", h.getWav(Integer.valueOf("02", 36)));
		assertEquals("b.ogg", h.getWav(Integer.valueOf("03", 36)));
		assertEquals("", h.getWav(Integer.valueOf("04", 36)));
		assertEquals("c.mp3", h.getWav(Integer.valueOf("05", 36)));
	}

	// getWavs()
	// 正常：複数の値が正常に格納されること
	@Test
	public void testGetWavs_Normal_Multi() {
		var c = content();
		setIndexed(c, BeMusicMeta.WAV, imeta("01", "a.wav"), imeta("03", "b.ogg"), imeta("05", "c.mp3"));
		var h = new BeMusicHeader(c);
		var m = h.getWavs();
		assertEquals(3, m.size());
		assertEquals("a.wav", m.get(Integer.valueOf("01", 36)));
		assertEquals("b.ogg", m.get(Integer.valueOf("03", 36)));
		assertEquals("c.mp3", m.get(Integer.valueOf("05", 36)));
	}

	// getWavs()
	// 正常：1件の値が正常に格納されること
	@Test
	public void testGetWavs_Normal_One() {
		var c = content();
		setIndexed(c, BeMusicMeta.WAV, imeta("01", "a.wav"));
		var h = new BeMusicHeader(c);
		var m = h.getWavs();
		assertEquals(1, m.size());
		assertEquals("a.wav", m.get(Integer.valueOf("01", 36)));
	}

	// getWavs()
	// 正常：0件の場合空のマップが返されること
	@Test
	public void testGetWavs_Normal_Empty() {
		var c = content();
		var h = new BeMusicHeader(c);
		var m = h.getWavs();
		assertEquals(0, m.size());
	}

	// getWavs()
	// 正常：返されたマップは読み取り専用であること
	@Test
	public void testGetWavs_Readonly() {
		var c = content();
		setIndexed(c, BeMusicMeta.WAV, imeta("01", "a.wav"));
		var h1 = new BeMusicHeader(c);
		var m1 = h1.getWavs();
		assertThrows(UnsupportedOperationException.class, () -> m1.put(999, "value"));
		var h2 = new BeMusicHeader(c, BeMusicHeader.NONE);
		var m2 = h2.getWavs();
		assertThrows(UnsupportedOperationException.class, () -> m2.put(999, "value"));
	}

	// getBmp(int)
	// 正常：存在する場合その値が、存在しない場合空文字が返されること
	@Test
	public void testGetBmp() {
		var c = content();
		setIndexed(c, BeMusicMeta.BMP, imeta("01", "a.bmp"), imeta("03", "b.jpg"), imeta("05", "c.png"));
		var h = new BeMusicHeader(c);
		assertEquals("a.bmp", h.getBmp(Integer.valueOf("01", 36)));
		assertEquals("", h.getBmp(Integer.valueOf("02", 36)));
		assertEquals("b.jpg", h.getBmp(Integer.valueOf("03", 36)));
		assertEquals("", h.getBmp(Integer.valueOf("04", 36)));
		assertEquals("c.png", h.getBmp(Integer.valueOf("05", 36)));
	}

	// getBmps()
	// 正常：複数の値が正常に格納されること
	@Test
	public void testGetBmps_Normal_Multi() {
		var c = content();
		setIndexed(c, BeMusicMeta.BMP, imeta("01", "a.bmp"), imeta("03", "b.jpg"), imeta("05", "c.png"));
		var h = new BeMusicHeader(c);
		var m = h.getBmps();
		assertEquals(3, m.size());
		assertEquals("a.bmp", m.get(Integer.valueOf("01", 36)));
		assertEquals("b.jpg", m.get(Integer.valueOf("03", 36)));
		assertEquals("c.png", m.get(Integer.valueOf("05", 36)));
	}

	// getBmps()
	// 正常：1件の値が正常に格納されること
	@Test
	public void testGetBmps_Normal_One() {
		var c = content();
		setIndexed(c, BeMusicMeta.BMP, imeta("01", "a.bmp"));
		var h = new BeMusicHeader(c);
		var m = h.getBmps();
		assertEquals(1, m.size());
		assertEquals("a.bmp", m.get(Integer.valueOf("01", 36)));
	}

	// getBmps()
	// 正常：0件の場合空のマップが返されること
	@Test
	public void testGetBmps_Normal_Empty() {
		var c = content();
		var h = new BeMusicHeader(c);
		var m = h.getBmps();
		assertEquals(0, m.size());
	}

	// getBmps()
	// 正常：返されたマップは読み取り専用であること
	@Test
	public void testGetBmps_Readonly() {
		var c = content();
		setIndexed(c, BeMusicMeta.BMP, imeta("01", "a.bmp"));
		var h1 = new BeMusicHeader(c);
		var m1 = h1.getBmps();
		assertThrows(UnsupportedOperationException.class, () -> m1.put(999, "value"));
		var h2 = new BeMusicHeader(c, BeMusicHeader.NONE);
		var m2 = h2.getBmps();
		assertThrows(UnsupportedOperationException.class, () -> m2.put(999, "value"));
	}

	// getBpm(int)
	// 正常：存在する場合その値が、存在しない場合BmsSpec#BPM_DEFAULTが返されること
	@Test
	public void testGetBpm() {
		var c = content();
		setIndexed(c, BeMusicMeta.BPM, imeta("01", 140.0), imeta("03", 180.0), imeta("05", 220.0));
		var h = new BeMusicHeader(c);
		assertEquals(140.0, 0.0, h.getBpm(Integer.valueOf("01", 36)));
		assertEquals(BmsSpec.BPM_DEFAULT, 0.0, h.getBpm(Integer.valueOf("02", 36)));
		assertEquals(180.0, 0.0, h.getBpm(Integer.valueOf("03", 36)));
		assertEquals(BmsSpec.BPM_DEFAULT, 0.0, h.getBpm(Integer.valueOf("04", 36)));
		assertEquals(220.0, 0.0, h.getBpm(Integer.valueOf("05", 36)));
	}

	// getBpms()
	// 正常：複数の値が正常に格納されること
	@Test
	public void testGetBpms_Normal_Multi() {
		var c = content();
		setIndexed(c, BeMusicMeta.BPM, imeta("01", 140.0), imeta("03", 180.0), imeta("05", 220.0));
		var h = new BeMusicHeader(c);
		var m = h.getBpms();
		assertEquals(3, m.size());
		assertEquals(140.0, 0.0, m.get(Integer.valueOf("01", 36)));
		assertEquals(180.0, 0.0, m.get(Integer.valueOf("03", 36)));
		assertEquals(220.0, 0.0, m.get(Integer.valueOf("05", 36)));
	}

	// getBpms()
	// 正常：1件の値が正常に格納されること
	@Test
	public void testGetBpms_Normal_One() {
		var c = content();
		setIndexed(c, BeMusicMeta.BPM, imeta("01", 140.0));
		var h = new BeMusicHeader(c);
		var m = h.getBpms();
		assertEquals(1, m.size());
		assertEquals(140.0, 0.0, m.get(Integer.valueOf("01", 36)));
	}

	// getBpms()
	// 正常：0件の場合空のマップが返されること
	@Test
	public void testGetBpms_Normal_Empty() {
		var c = content();
		var h = new BeMusicHeader(c);
		var m = h.getBpms();
		assertEquals(0, m.size());
	}

	// getBpms()
	// 正常：返されたマップは読み取り専用であること
	@Test
	public void testGetBpms_Readonly() {
		var c = content();
		setIndexed(c, BeMusicMeta.BPM, imeta("01", 130.0));
		var h1 = new BeMusicHeader(c);
		var m1 = h1.getBpms();
		assertThrows(UnsupportedOperationException.class, () -> m1.put(999, 99.0));
		var h2 = new BeMusicHeader(c, BeMusicHeader.NONE);
		var m2 = h2.getBpms();
		assertThrows(UnsupportedOperationException.class, () -> m2.put(999, 99.0));
	}

	// getStop(int)
	// 正常：存在する場合その値が、存在しない場合0が返されること
	@Test
	public void testGetStop() {
		var c = content();
		setIndexed(c, BeMusicMeta.STOP, imeta("01", 1000.0), imeta("03", 2000.0), imeta("05", 3000.0), imeta("07", 10.567));
		var h = new BeMusicHeader(c);
		assertEquals(1000.0, h.getStop(Integer.valueOf("01", 36)), 0.0);
		assertEquals(0.0, h.getStop(Integer.valueOf("02", 36)), 0.0);
		assertEquals(2000.0, h.getStop(Integer.valueOf("03", 36)), 0.0);
		assertEquals(0.0, h.getStop(Integer.valueOf("04", 36)), 0.0);
		assertEquals(3000.0, h.getStop(Integer.valueOf("05", 36)), 0.0);
		assertEquals(0.0, h.getStop(Integer.valueOf("06", 36)), 0.0);
		assertEquals(10.567, h.getStop(Integer.valueOf("07", 36)), 0.0);
	}

	// getStops()
	// 正常：複数の値が正常に格納されること
	@Test
	public void testGetStops_Normal_Multi() {
		var c = content();
		setIndexed(c, BeMusicMeta.STOP, imeta("01", 1000.0), imeta("03", 2000.0), imeta("05", 3000.0));
		var h = new BeMusicHeader(c);
		var m = h.getStops();
		assertEquals(3, m.size());
		assertEquals(1000.0, m.get(Integer.valueOf("01", 36)).doubleValue(), 0.0);
		assertEquals(2000.0, m.get(Integer.valueOf("03", 36)).doubleValue(), 0.0);
		assertEquals(3000.0, m.get(Integer.valueOf("05", 36)).doubleValue(), 0.0);
	}

	// getStops()
	// 正常：1件の値が正常に格納されること
	@Test
	public void testGetStops_Normal_One() {
		var c = content();
		setIndexed(c, BeMusicMeta.STOP, imeta("01", 1000.0));
		var h = new BeMusicHeader(c);
		var m = h.getStops();
		assertEquals(1, m.size());
		assertEquals(1000.0, m.get(Integer.valueOf("01", 36)).doubleValue(), 0.0);
	}

	// getStops()
	// 正常：0件の場合空のマップが返されること
	@Test
	public void testGetStops_Normal_Empty() {
		var c = content();
		var h = new BeMusicHeader(c);
		var m = h.getStops();
		assertEquals(0, m.size());
	}

	// getStops()
	// 正常：返されたマップは読み取り専用であること
	@Test
	public void testGetStops_Readonly() {
		var c = content();
		setIndexed(c, BeMusicMeta.STOP, imeta("01", 1000.0));
		var h1 = new BeMusicHeader(c);
		var m1 = h1.getStops();
		assertThrows(UnsupportedOperationException.class, () -> m1.put(999, 99.0));
		var h2 = new BeMusicHeader(c, BeMusicHeader.NONE);
		var m2 = h2.getStops();
		assertThrows(UnsupportedOperationException.class, () -> m2.put(999, 99.0));
	}

	// getScroll(int)
	// 正常：存在する場合その値が、存在しない場合0が返されること
	@Test
	public void testGetScroll() {
		var c = content();
		setIndexed(c, BeMusicMeta.SCROLL, imeta("01", 1.23), imeta("03", 4.56), imeta("05", 7.89), imeta("07", 10.12));
		var h = new BeMusicHeader(c);
		assertEquals(1.23, h.getScroll(Integer.valueOf("01", 36)), 0.0);
		assertEquals(0.0, h.getScroll(Integer.valueOf("02", 36)), 0.0);
		assertEquals(4.56, h.getScroll(Integer.valueOf("03", 36)), 0.0);
		assertEquals(0.0, h.getScroll(Integer.valueOf("04", 36)), 0.0);
		assertEquals(7.89, h.getScroll(Integer.valueOf("05", 36)), 0.0);
		assertEquals(0.0, h.getScroll(Integer.valueOf("06", 36)), 0.0);
		assertEquals(10.12, h.getScroll(Integer.valueOf("07", 36)), 0.0);
	}

	// getScrolls()
	// 正常：複数の値が正常に格納されること
	@Test
	public void testGetScrolls_Normal_Multi() {
		var c = content();
		setIndexed(c, BeMusicMeta.SCROLL, imeta("01", 0.48), imeta("03", 0.96), imeta("05", 1.44));
		var h = new BeMusicHeader(c);
		var m = h.getScrolls();
		assertEquals(3, m.size());
		assertEquals(0.48, m.get(Integer.valueOf("01", 36)).doubleValue(), 0.0);
		assertEquals(0.96, m.get(Integer.valueOf("03", 36)).doubleValue(), 0.0);
		assertEquals(1.44, m.get(Integer.valueOf("05", 36)).doubleValue(), 0.0);
	}

	// getScrolls()
	// 正常：1件の値が正常に格納されること
	@Test
	public void testGetScrolls_Normal_One() {
		var c = content();
		setIndexed(c, BeMusicMeta.SCROLL, imeta("01", 1.23));
		var h = new BeMusicHeader(c);
		var m = h.getScrolls();
		assertEquals(1, m.size());
		assertEquals(1.23, m.get(Integer.valueOf("01", 36)).doubleValue(), 0.0);
	}

	// getScrolls()
	// 正常：0件の場合空のマップが返されること
	@Test
	public void testGetScrolls_Normal_Empty() {
		var c = content();
		var h = new BeMusicHeader(c);
		var m = h.getScrolls();
		assertEquals(0, m.size());
	}

	// getScrolls()
	// 正常：返されたマップは読み取り専用であること
	@Test
	public void testGetScrolls_Readonly() {
		var c = content();
		setIndexed(c, BeMusicMeta.SCROLL, imeta("01", 1.23));
		var h1 = new BeMusicHeader(c);
		var m1 = h1.getScrolls();
		assertThrows(UnsupportedOperationException.class, () -> m1.put(999, 0.99));
		var h2 = new BeMusicHeader(c, BeMusicHeader.NONE);
		var m2 = h2.getScrolls();
		assertThrows(UnsupportedOperationException.class, () -> m2.put(999, 0.99));
	}

	// getText(int)
	// 正常：存在する場合その値が、存在しない場合空文字が返されること
	@Test
	public void testGetText() {
		var c = content();
		setIndexed(c, BeMusicMeta.TEXT, imeta("01", "text1"), imeta("03", "text2"), imeta("05", "text3"));
		var h = new BeMusicHeader(c);
		assertEquals("text1", h.getText(Integer.valueOf("01", 36)));
		assertEquals("", h.getText(Integer.valueOf("02", 36)));
		assertEquals("text2", h.getText(Integer.valueOf("03", 36)));
		assertEquals("", h.getText(Integer.valueOf("04", 36)));
		assertEquals("text3", h.getText(Integer.valueOf("05", 36)));
	}

	// getTexts()
	// 正常：複数の値が正常に格納されること
	@Test
	public void testGetTexts_Normal_Multi() {
		var c = content();
		setIndexed(c, BeMusicMeta.TEXT, imeta("01", "text1"), imeta("03", "text2"), imeta("05", "text3"));
		var h = new BeMusicHeader(c);
		var m = h.getTexts();
		assertEquals(3, m.size());
		assertEquals("text1", m.get(Integer.valueOf("01", 36)));
		assertEquals("text2", m.get(Integer.valueOf("03", 36)));
		assertEquals("text3", m.get(Integer.valueOf("05", 36)));
	}

	// getTexts()
	// 正常：1件の値が正常に格納されること
	@Test
	public void testGetTexts_Normal_One() {
		var c = content();
		setIndexed(c, BeMusicMeta.TEXT, imeta("01", "text1"));
		var h = new BeMusicHeader(c);
		var m = h.getTexts();
		assertEquals(1, m.size());
		assertEquals("text1", m.get(Integer.valueOf("01", 36)));
	}

	// getTexts()
	// 正常：0件の場合空のマップが返されること
	@Test
	public void testGetTexts_Normal_Empty() {
		var c = content();
		var h = new BeMusicHeader(c);
		var m = h.getTexts();
		assertEquals(0, m.size());
	}

	// getTexts()
	// 正常：返されたマップは読み取り専用であること
	@Test
	public void testGetTexts_Readonly() {
		var c = content();
		setIndexed(c, BeMusicMeta.TEXT, imeta("01", "text"));
		var h1 = new BeMusicHeader(c);
		var m1 = h1.getTexts();
		assertThrows(UnsupportedOperationException.class, () -> m1.put(999, "value"));
		var h2 = new BeMusicHeader(c, BeMusicHeader.NONE);
		var m2 = h2.getTexts();
		assertThrows(UnsupportedOperationException.class, () -> m2.put(999, "value"));
	}

	// of(BmsContent)
	// サポートする全てのヘッダ定義が収集されること
	@Test
	public void testOfBmsContent_Normal() {
		var c = contentForConstructor();
		var h = BeMusicHeader.of(c);
		assertHeaderForConstructorAll(h);
	}

	// of(BmsContent)
	// NullPointerException contentがnull
	@Test
	public void testOfBmsContent_NullContent() {
		assertThrows(NullPointerException.class, () -> { BeMusicHeader.of(null); });
	}

	// of(BmsContent, int)
	// 全ての収集フラグをOFFにすると対象のヘッダ定義が全く収集されないこと
	@Test
	public void testOfBmsContentInt_AllFlagsOff() {
		var c = contentForConstructor();
		var h = BeMusicHeader.of(c, BeMusicHeader.NONE);
		assertHeaderForConstructorNone(h);
	}

	// of(BmsContent, int)
	// 全ての収集フラグをONにすると全てのヘッダ定義が収集されること
	@Test
	public void testOfBmsContentInt_AllFlagsOn() {
		var c = contentForConstructor();
		var h = BeMusicHeader.of(c, BeMusicHeader.ALL);
		assertHeaderForConstructorAll(h);
	}

	// of(BmsContent, int)
	// NullPointerException contentがnull
	@Test
	public void testOfBmsContentInt_NullContent() {
		assertThrows(NullPointerException.class, () -> { BeMusicHeader.of(null, 0); });
	}

	// of(BmsContent, Supplier<H>)
	// サポートする全てのヘッダ定義が収集され、拡張定義のセットアップが行われること
	@Test
	public void testOfBmsContentSupplier_Normal() {
		var c = contentForConstructor();
		var h = BeMusicHeader.of(c, HeaderForOfTest::new);
		assertHeaderForConstructorAll(h);
		assertTrue(h.calledOnCreate);
	}

	// of(BmsContent, Supplier<H>)
	// NullPointerException contentがnull
	@Test
	public void testOfBmsContentSupplier_NullContent() {
		assertThrows(NullPointerException.class, () -> { BeMusicHeader.of(null, HeaderForOfTest::new); });
	}

	// of(BmsContent, Supplier<H>)
	// NullPointerException creatorがnull
	@Test
	public void testOfBmsContentSupplier_NullCreator() {
		var c = content();
		assertThrows(NullPointerException.class, () -> { BeMusicHeader.of(c, null); });
	}

	// of(BmsContent, Supplier<H>)
	// NullPointerException creatorがnullを返した
	@Test
	public void testOfBmsContentSupplier_CreatorReturnedNull() {
		var c = content();
		assertThrows(NullPointerException.class, () -> { BeMusicHeader.of(c, () -> null); });
	}

	// of(BmsContent, int, Supplier<H>)
	// 全ての収集フラグをOFFにすると対象のヘッダ定義が全く収集されず、拡張定義のセットアップが行われること
	@Test
	public void testOfBmsContentIntSupplier_AllFlagsOff() {
		var c = contentForConstructor();
		var h = BeMusicHeader.of(c, BeMusicHeader.NONE, HeaderForOfTest::new);
		assertHeaderForConstructorNone(h);
		assertTrue(h.calledOnCreate);
	}

	// of(BmsContent, int, Supplier<H>)
	// 全ての収集フラグをONにすると全てのヘッダ定義が収集され、拡張定義のセットアップが行われること
	@Test
	public void testOfBmsContentIntSupplier_AllFlagsOn() {
		var c = contentForConstructor();
		var h = BeMusicHeader.of(c, BeMusicHeader.ALL, HeaderForOfTest::new);
		assertHeaderForConstructorAll(h);
		assertTrue(h.calledOnCreate);
	}

	// of(BmsContent, int, Supplier<H>)
	// NullPointerException contentがnull
	@Test
	public void testOfBmsContentIntSupplier_NullContent() {
		assertThrows(NullPointerException.class, () -> { BeMusicHeader.of(null, 0, HeaderForOfTest::new); });
	}

	// of(BmsContent, int, Supplier<H>)
	// NullPointerException creatorがnull
	@Test
	public void testOfBmsContentIntSupplier_NullCreator() {
		var c = content();
		assertThrows(NullPointerException.class, () -> { BeMusicHeader.of(c, 0, null); });
	}

	// of(BmsContent, int, Supplier<H>)
	// NullPointerException creatorがnullを返した
	@Test
	public void testOfBmsContentIntSupplier_CreatorReturnedNull() {
		var c = content();
		assertThrows(NullPointerException.class, () -> { BeMusicHeader.of(c, 0, () -> null); });
	}

	private static class TestOnCreateHeader extends BeMusicHeader {
		private static final int FLAGS = WAV | BMP | BPM | STOP | TEXT;
		static BmsContent sContent = null;
		static boolean sSuccess = false;

		TestOnCreateHeader(BmsContent content) {
			super(content, FLAGS);
		}

		@Override
		protected void onCreate(BmsContent content, int flags) {
			assertSame(sContent, content);
			assertEquals(FLAGS, flags);
			assertEquals("MyTitle", this.getTitle());
			assertEquals("Taro,Hanako", this.getSubArtist(","));
			assertEquals("sound.wav", this.getWav(Integer.valueOf("8Z", 36)));
			sSuccess = true;
		}
	}

	// onCreate()
	// 正常：全ての項目生成が完了した後でonCreate()が呼ばれること
	@Test
	public void testOnCreate() {
		var c = content();
		setSingle(c, BeMusicMeta.TITLE, "MyTitle");
		setMultiple(c, BeMusicMeta.SUBARTIST, "Taro", "Hanako");
		setIndexed(c, BeMusicMeta.WAV, imeta("8Z", "sound.wav"));
		TestOnCreateHeader.sContent = c;
		TestOnCreateHeader.sSuccess = false;
		new TestOnCreateHeader(c);
		assertTrue(TestOnCreateHeader.sSuccess);
	}

	private static BmsContent content() {
		var spec = BeMusicSpec.createV1(null, null);
		return new BmsContent(spec);
	}

	private static BmsContent contentForConstructor() {
		var c = content();
		setSingle(c, BeMusicMeta.GENRE, "MyGenre");
		setMultiple(c, BeMusicMeta.SUBARTIST, "SubArtist1", "SubArtist2");
		setIndexed(c, BeMusicMeta.WAV, imeta("01", "a.wav"), imeta("ZZ", "b.wav"));
		setIndexed(c, BeMusicMeta.BMP, imeta("02", "c.bmp"), imeta("ZY", "d.bmp"));
		setIndexed(c, BeMusicMeta.BPM, imeta("03", 150.0), imeta("ZX", 180.0));
		setIndexed(c, BeMusicMeta.STOP, imeta("04", 192L), imeta("ZW", 384L));
		setIndexed(c, BeMusicMeta.TEXT, imeta("05", "text1"), imeta("ZV", "text2"));
		setIndexed(c, BeMusicMeta.SCROLL, imeta("06", 1.25), imeta("ZU", 0.125));
		return c;
	}

	private static void setSingle(BmsContent content, BmsMetaKey meta, Object value) {
		content.beginEdit();
		content.setMeta(meta, value);
		content.endEdit();
	}

	private static void setMultiple(BmsContent content, BmsMetaKey meta, Object...values) {
		content.beginEdit();
		for (var i = 0; i < values.length; i++) { content.setMeta(meta, i, values[i]); }
		content.endEdit();
	}

	private static class IndexedMeta {
		String index;
		Object value;
	}

	private static IndexedMeta imeta(String index, Object value) {
		var meta = new IndexedMeta();
		meta.index = index;
		meta.value = value;
		return meta;
	}

	private static void setIndexed(BmsContent content, BmsMetaKey meta, IndexedMeta...values) {
		content.beginEdit();
		for (var i = 0; i < values.length; i++) { content.setMeta(meta, Integer.valueOf(values[i].index, 36), values[i].value); }
		content.endEdit();
	}

	private static void assertHeaderForConstructorNone(BeMusicHeader h) {
		assertEquals("MyGenre", h.getGenre());
		assertEquals("SubArtist1,SubArtist2", h.getSubArtist(","));
		assertEquals("", h.getWav(Integer.valueOf("01", 36)));
		assertEquals("", h.getWav(Integer.valueOf("ZZ", 36)));
		assertEquals("", h.getBmp(Integer.valueOf("02", 36)));
		assertEquals("", h.getBmp(Integer.valueOf("ZY", 36)));
		assertEquals(BmsSpec.BPM_DEFAULT, 0.0, h.getBpm(Integer.valueOf("03", 36)));
		assertEquals(BmsSpec.BPM_DEFAULT, 0.0, h.getBpm(Integer.valueOf("ZX", 36)));
		assertEquals(0.0, h.getStop(Integer.valueOf("04", 36)), 0.0);
		assertEquals(0.0, h.getStop(Integer.valueOf("ZW", 36)), 0.0);
		assertEquals("", h.getText(Integer.valueOf("05", 36)));
		assertEquals("", h.getText(Integer.valueOf("ZV", 36)));
		assertEquals(0.0, h.getScroll(BmsInt.to36i("06")), 0.0);
		assertEquals(0.0, h.getScroll(BmsInt.to36i("ZU")), 0.0);
	}

	private static void assertHeaderForConstructorAll(BeMusicHeader h) {
		assertEquals("MyGenre", h.getGenre());
		assertEquals("SubArtist1,SubArtist2", h.getSubArtist(","));
		assertEquals("a.wav", h.getWav(Integer.valueOf("01", 36)));
		assertEquals("b.wav", h.getWav(Integer.valueOf("ZZ", 36)));
		assertEquals("c.bmp", h.getBmp(Integer.valueOf("02", 36)));
		assertEquals("d.bmp", h.getBmp(Integer.valueOf("ZY", 36)));
		assertEquals(150.0, 0.0, h.getBpm(Integer.valueOf("03", 36)));
		assertEquals(180.0, 0.0, h.getBpm(Integer.valueOf("ZX", 36)));
		assertEquals(192.0, h.getStop(Integer.valueOf("04", 36)), 0.0);
		assertEquals(384.0, h.getStop(Integer.valueOf("ZW", 36)), 0.0);
		assertEquals("text1", h.getText(Integer.valueOf("05", 36)));
		assertEquals("text2", h.getText(Integer.valueOf("ZV", 36)));
		assertEquals(1.25, h.getScroll(BmsInt.to36i("06")), 0.0);
		assertEquals(0.125, h.getScroll(BmsInt.to36i("ZU")), 0.0);
	}
}
