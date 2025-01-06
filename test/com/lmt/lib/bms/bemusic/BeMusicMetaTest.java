package com.lmt.lib.bms.bemusic;

import static org.junit.Assert.*;

import java.util.function.Consumer;

import org.junit.Test;

import com.lmt.lib.bms.BmsContent;
import com.lmt.lib.bms.BmsInt;
import com.lmt.lib.bms.BmsSpec;

public class BeMusicMetaTest {
	// setBase(BmsContent, Integer), getBase(BmsContent)
	// 設定した値を正しく取得できること
	@Test
	public void testXetBase() {
		var c = content();
		c.edit(() -> BeMusicMeta.setBase(c, 16));
		assertEquals(16, BeMusicMeta.getBase(c));
		c.edit(() -> BeMusicMeta.setBase(c, 36));
		assertEquals(36, BeMusicMeta.getBase(c));
		c.edit(() -> BeMusicMeta.setBase(c, 62));
		assertEquals(62, BeMusicMeta.getBase(c));
		c.edit(() -> BeMusicMeta.setBase(c, null));
		assertEquals(36, BeMusicMeta.getBase(c));
	}

	// getBase(BmsContent)
	// NullPointerException contentがnull
	@Test
	public void testGetBase_NullContent() {
		assertThrows(NullPointerException.class, () -> BeMusicMeta.getBase(null));
	}

	// setBase(BmsContent, Integer)
	// NullPointerException contentがnull
	@Test
	public void testSetBase_NullContent() {
		assertThrows(NullPointerException.class, () -> BeMusicMeta.setBase(null, 16));
	}

	// setBase(BmsContent, Integer)
	// IllegalStateException BMSコンテンツが編集モードではない
	@Test
	public void testSetBase_NotEditMode() {
		var c = content();
		assertThrows(IllegalStateException.class, () -> BeMusicMeta.setBase(c, 36));
	}

	// setBase(BmsContent, Integer)
	// IllegalArgumentException baseがnull, 16, 36, 62以外
	@Test
	public void testSetBase_WrongBase() {
		var c = content();
		c.beginEdit();
		assertThrows(IllegalArgumentException.class, () -> BeMusicMeta.setBase(c, 10));
	}

	// setGenre(BmsContent, String), getGenre(BmsContent)
	// 設定した値を正しく取得できること
	@Test
	public void testXetGenre() {
		var c = content(e -> BeMusicMeta.setGenre(e, "EUROBEAT"));
		assertEquals("EUROBEAT", BeMusicMeta.getGenre(c));
	}

	// getGenre(BmsContent)
	// NullPointerException contentがnull
	@Test
	public void testGetGenre_NullContent() {
		assertThrows(NullPointerException.class, () -> BeMusicMeta.getGenre(null));
	}

	// setGenre(BmsContent, String)
	// NullPointerException contentがnull
	@Test
	public void testSetGenre_NullContent() {
		assertThrows(NullPointerException.class, () -> BeMusicMeta.setGenre(null, "#GENRE"));
	}

	// setGenre(BmsContent, String)
	// IllegalStateException BMSコンテンツが編集モードではない
	@Test
	public void testSetGenre_NotEditMode() {
		var c = content();
		assertThrows(IllegalStateException.class, () -> BeMusicMeta.setGenre(c, "#GENRE"));
	}

	// setTitle(BmsContent, String), getTitle(BmsContent)
	// 設定した値を正しく取得できること
	@Test
	public void testXetTitle() {
		var c = content(e -> BeMusicMeta.setTitle(e, "My Song"));
		assertEquals("My Song", BeMusicMeta.getTitle(c));
	}

	// getTitle(BmsContent)
	// NullPointerException contentがnull
	@Test
	public void testGetTitle_NullContent() {
		assertThrows(NullPointerException.class, () -> BeMusicMeta.getTitle(null));
	}

	// setTitle(BmsContent, String)
	// NullPointerException contentがnull
	@Test
	public void testSetTitle_NullContent() {
		assertThrows(NullPointerException.class, () -> BeMusicMeta.setTitle(null, "#TITLE"));
	}

	// setTitle(BmsContent, String)
	// IllegalStateException BMSコンテンツが編集モードではない
	@Test
	public void testSetTitle_NotEditMode() {
		var c = content();
		assertThrows(IllegalStateException.class, () -> BeMusicMeta.setTitle(c, "#TITLE"));
	}

	// setSubTitle(BmsContent, String), getSubTitle(BmsContent)
	// 設定した値を正しく取得できること
	@Test
	public void testXetSubTitle() {
		var c = content(e -> BeMusicMeta.setSubTitle(e, "SP ANOTHER"));
		assertEquals("SP ANOTHER", BeMusicMeta.getSubTitle(c));
	}

	// getSubTitle(BmsContent)
	// NullPointerException contentがnull
	@Test
	public void testGetSubTitle_NullContent() {
		assertThrows(NullPointerException.class, () -> BeMusicMeta.getSubTitle(null));
	}

	// setSubTitle(BmsContent, String)
	// NullPointerException contentがnull
	@Test
	public void testSetSubTitle_NullContent() {
		assertThrows(NullPointerException.class, () -> BeMusicMeta.setSubTitle(null, "#SUBTITLE"));
	}

	// setSubTitle(BmsContent, String)
	// IllegalStateException BMSコンテンツが編集モードではない
	@Test
	public void testSetSubTitle_NotEditMode() {
		var c = content();
		assertThrows(IllegalStateException.class, () -> BeMusicMeta.setSubTitle(c, "#SUBTITLE"));
	}

	// setArtist(BmsContent, String), getArtist(BmsContent)
	// 設定した値を正しく取得できること
	@Test
	public void testXetArtist() {
		var c = content(e -> BeMusicMeta.setArtist(e, "J-SON3"));
		assertEquals("J-SON3", BeMusicMeta.getArtist(c));
	}

	// getArtist(BmsContent)
	// NullPointerException contentがnull
	@Test
	public void testGetArtist_NullContent() {
		assertThrows(NullPointerException.class, () -> BeMusicMeta.getArtist(null));
	}

	// setArtist(BmsContent, String)
	// NullPointerException contentがnull
	@Test
	public void testSetArtist_NullContent() {
		assertThrows(NullPointerException.class, () -> BeMusicMeta.setArtist(null, "#ARTIST"));
	}

	// setArtist(BmsContent, String)
	// IllegalStateException BMSコンテンツが編集モードではない
	@Test
	public void testSetArtist_NotEditMode() {
		var c = content();
		assertThrows(IllegalStateException.class, () -> BeMusicMeta.setArtist(c, "#ARTIST"));
	}

	// setSubArtist(BmsContent, int, String), getSubArtists(BmsContent), getSubArtist(BmsContent, int)
	// 設定した値を正しく取得できること
	@Test
	public void testXetSubArtist() {
		var c = content(e -> {
			BeMusicMeta.setSubArtist(e, 0, "Michael");
			BeMusicMeta.setSubArtist(e, 1, "Ray");
			BeMusicMeta.setSubArtist(e, 2, "Cate");
		});
		assertEquals("Michael", BeMusicMeta.getSubArtist(c, 0));
		assertEquals("Ray", BeMusicMeta.getSubArtist(c, 1));
		assertEquals("Cate", BeMusicMeta.getSubArtist(c, 2));
		var l = BeMusicMeta.getSubArtists(c);
		assertEquals(3, l.size());
		assertEquals("Michael", l.get(0));
		assertEquals("Ray", l.get(1));
		assertEquals("Cate", l.get(2));
	}

	// getSubArtists(BmsContent)
	// NullPointerException contentがnull
	@Test
	public void testGetSubArtists_NullContent() {
		assertThrows(NullPointerException.class, () -> BeMusicMeta.getSubArtists(null));
	}

	// getSubArtist(BmsContent, int)
	// NullPointerException contentがnull
	@Test
	public void testGetSubArtist_NullContent() {
		assertThrows(NullPointerException.class, () -> BeMusicMeta.getSubArtist(null, 0));
	}

	// getSubArtist(BmsContent, int)
	// IndexOutOfBoundsException - indexが0～BmsSpec.MULTIPLE_META_INDEX_MAXの範囲外
	@Test
	public void testGetSubArtist_IndexOutOfRange() {
		var c = content();
		var ec = IndexOutOfBoundsException.class;
		assertThrows(ec, () -> BeMusicMeta.getSubArtist(c, -1));
		assertThrows(ec, () -> BeMusicMeta.getSubArtist(c, BmsSpec.MULTIPLE_META_INDEX_MAX + 1));
	}

	// setSubArtist(BmsContent, int, String)
	// NullPointerException contentがnull
	@Test
	public void testSetSubArtist_NullContent() {
		assertThrows(NullPointerException.class, () -> BeMusicMeta.setSubArtist(null, 0, "#SUBARTIST"));
	}

	// setSubArtist(BmsContent, int, String)
	// IndexOutOfBoundsException - indexが0～BmsSpec.MULTIPLE_META_INDEX_MAXの範囲外
	@Test
	public void testSetSubArtist_IndexOutOfRange() {
		var c = content();
		var ec = IndexOutOfBoundsException.class;
		c.beginEdit();
		assertThrows(ec, () -> BeMusicMeta.setSubArtist(c, -1, "#SUBARTIST"));
		assertThrows(ec, () -> BeMusicMeta.setSubArtist(c, BmsSpec.MULTIPLE_META_INDEX_MAX + 1, "#SUBARTIST"));
	}

	// setSubArtist(BmsContent, int, String)
	// IllegalStateException BMSコンテンツが編集モードではない
	@Test
	public void testSetSubArtist_NotEditMode() {
		var c = content();
		assertThrows(IllegalStateException.class, () -> BeMusicMeta.setSubArtist(c, 0, "#SUBARTIST"));
	}

	// setPlayer(BmsContent, BeMusicPlayer), getPlayer(BmsContent)
	// 設定した値を正しく取得できること
	@Test
	public void testXetPlayer() {
		var c = content(e -> BeMusicMeta.setPlayer(e, BeMusicPlayer.DOUBLE));
		assertEquals(BeMusicPlayer.DOUBLE, BeMusicMeta.getPlayer(c));
	}

	// getPlayer(BmsContent)
	// NullPointerException contentがnull
	@Test
	public void testGetPlayer_NullContent() {
		assertThrows(NullPointerException.class, () -> BeMusicMeta.getPlayer(null));
	}

	// setPlayer(BmsContent, BeMusicPlayer)
	// NullPointerException contentがnull
	@Test
	public void testSetPlayer_NullContent() {
		assertThrows(NullPointerException.class, () -> BeMusicMeta.setPlayer(null, BeMusicPlayer.SINGLE));
	}

	// setPlayer(BmsContent, BeMusicPlayer)
	// IllegalStateException BMSコンテンツが編集モードではない
	@Test
	public void testSetPlayer_NotEditMode() {
		var c = content();
		assertThrows(IllegalStateException.class, () -> BeMusicMeta.setPlayer(c, BeMusicPlayer.SINGLE));
	}

	// setRank(BmsContent, BeMusicRank), getRank(BmsContent)
	// 設定した値を正しく取得できること
	@Test
	public void testXetRank() {
		var c = content(e -> BeMusicMeta.setRank(e, BeMusicRank.NORMAL));
		assertEquals(BeMusicRank.NORMAL, BeMusicMeta.getRank(c));
	}

	// getRank(BmsContent)
	// NullPointerException contentがnull
	@Test
	public void testGetRank_NullContent() {
		assertThrows(NullPointerException.class, () -> BeMusicMeta.getRank(null));
	}

	// setRank(BmsContent, BeMusicRank)
	// NullPointerException contentがnull
	@Test
	public void testSetRank_NullContent() {
		assertThrows(NullPointerException.class, () -> BeMusicMeta.setRank(null, BeMusicRank.EASY));
	}

	// setRank(BmsContent, BeMusicRank)
	// IllegalStateException BMSコンテンツが編集モードではない
	@Test
	public void testSetRank_NotEditMode() {
		var c = content();
		assertThrows(IllegalStateException.class, () -> BeMusicMeta.setRank(c, BeMusicRank.EASY));
	}

	// setDefExRank(BmsContent, Double), getDefExRank(BmsContent)
	// 設定した値を正しく取得できること
	@Test
	public void testXetDefExRank() {
		var c = content(e -> BeMusicMeta.setDefExRank(e, 150.0));
		assertEquals(150.0, BeMusicMeta.getDefExRank(c), 0.0);
	}

	// getDefExRank(BmsContent)
	// NullPointerException contentがnull
	@Test
	public void testGetDefExRank_NullContent() {
		assertThrows(NullPointerException.class, () -> BeMusicMeta.getDefExRank(null));
	}

	// setDefExRank(BmsContent, Double)
	// NullPointerException contentがnull
	@Test
	public void testSetDefExRank_NullContent() {
		assertThrows(NullPointerException.class, () -> BeMusicMeta.setDefExRank(null, 200.0));
	}

	// setDefExRank(BmsContent, Double)
	// IllegalStateException BMSコンテンツが編集モードではない
	@Test
	public void testSetDefExRank_NotEditMode() {
		var c = content();
		assertThrows(IllegalStateException.class, () -> BeMusicMeta.setDefExRank(c, 200.0));
	}

	// setTotal(BmsContent, Double), getTotal(BmsContent)
	// 設定した値を正しく取得できること
	@Test
	public void testXetTotal() {
		var c = content(e -> BeMusicMeta.setTotal(e, 350.0));
		assertEquals(350.0, BeMusicMeta.getTotal(c), 0.0);
	}

	// getTotal(BmsContent)
	// NullPointerException contentがnull
	@Test
	public void testGetTotal_NullContent() {
		assertThrows(NullPointerException.class, () -> BeMusicMeta.getTotal(null));
	}

	// setTotal(BmsContent, Double)
	// NullPointerException contentがnull
	@Test
	public void testSetTotal_NullContent() {
		assertThrows(NullPointerException.class, () -> BeMusicMeta.setTotal(null, 600.0));
	}

	// setTotal(BmsContent, Double)
	// IllegalStateException BMSコンテンツが編集モードではない
	@Test
	public void testSetTotal_NotEditMode() {
		var c = content();
		assertThrows(IllegalStateException.class, () -> BeMusicMeta.setTotal(c, 600.0));
	}

	// setComment(BmsContent, String), getComment(BmsContent)
	// 設定した値を正しく取得できること
	@Test
	public void testXetComment() {
		var c = content(e -> BeMusicMeta.setComment(e, "Great music!"));
		assertEquals("Great music!", BeMusicMeta.getComment(c));
	}

	// getComment(BmsContent)
	// NullPointerException contentがnull
	@Test
	public void testGetComment_NullContent() {
		assertThrows(NullPointerException.class, () -> BeMusicMeta.getComment(null));
	}

	// setComment(BmsContent, String)
	// NullPointerException contentがnull
	@Test
	public void testSetComment_NullContent() {
		assertThrows(NullPointerException.class, () -> BeMusicMeta.setComment(null, "#COMMENT"));
	}

	// setComment(BmsContent, String)
	// IllegalStateException BMSコンテンツが編集モードではない
	@Test
	public void testSetComment_NotEditMode() {
		var c = content();
		assertThrows(IllegalStateException.class, () -> BeMusicMeta.setComment(c, "#COMMENT"));
	}

	// setStageFile(BmsContent, String), getStageFile(BmsContent)
	// 設定した値を正しく取得できること
	@Test
	public void testXetStageFile() {
		var c = content(e -> BeMusicMeta.setStageFile(e, "jacket.png"));
		assertEquals("jacket.png", BeMusicMeta.getStageFile(c));
	}

	// getStageFile(BmsContent)
	// NullPointerException contentがnull
	@Test
	public void testGetStageFile_NullContent() {
		assertThrows(NullPointerException.class, () -> BeMusicMeta.getStageFile(null));
	}

	// setStageFile(BmsContent, String)
	// NullPointerException contentがnull
	@Test
	public void testSetStageFile_NullContent() {
		assertThrows(NullPointerException.class, () -> BeMusicMeta.setStageFile(null, "#STAGEFILE"));
	}

	// setStageFile(BmsContent, String)
	// IllegalStateException BMSコンテンツが編集モードではない
	@Test
	public void testSetStageFile_NotEditMode() {
		var c = content();
		assertThrows(IllegalStateException.class, () -> BeMusicMeta.setStageFile(c, "#STAGEFILE"));
	}

	// setBanner(BmsContent, String), getBanner(BmsContent)
	// 設定した値を正しく取得できること
	@Test
	public void testXetBanner() {
		var c = content(e -> BeMusicMeta.setBanner(e, "banner.jpg"));
		assertEquals("banner.jpg", BeMusicMeta.getBanner(c));
	}

	// getBanner(BmsContent)
	// NullPointerException contentがnull
	@Test
	public void testGetBanner_NullContent() {
		assertThrows(NullPointerException.class, () -> BeMusicMeta.getBanner(null));
	}

	// setBanner(BmsContent, String)
	// NullPointerException contentがnull
	@Test
	public void testSetBanner_NullContent() {
		assertThrows(NullPointerException.class, () -> BeMusicMeta.setBanner(null, "#BANNER"));
	}

	// setBanner(BmsContent, String)
	// IllegalStateException BMSコンテンツが編集モードではない
	@Test
	public void testSetBanner_NotEditMode() {
		var c = content();
		assertThrows(IllegalStateException.class, () -> BeMusicMeta.setBanner(c, "#BANNER"));
	}

	// setBackBmp(BmsContent, String), getBackBmp(BmsContent)
	// 設定した値を正しく取得できること
	@Test
	public void testXetBackBmp() {
		var c = content(e -> BeMusicMeta.setBackBmp(e, "background.bmp"));
		assertEquals("background.bmp", BeMusicMeta.getBackBmp(c));
	}

	// getBackBmp(BmsContent)
	// NullPointerException contentがnull
	@Test
	public void testGetBackBmp_NullContent() {
		assertThrows(NullPointerException.class, () -> BeMusicMeta.getBackBmp(null));
	}

	// setBackBmp(BmsContent, String)
	// NullPointerException contentがnull
	@Test
	public void testSetBackBmp_NullContent() {
		assertThrows(NullPointerException.class, () -> BeMusicMeta.setBackBmp(null, "#BACKBMP"));
	}

	// setBackBmp(BmsContent, String)
	// IllegalStateException BMSコンテンツが編集モードではない
	@Test
	public void testSetBackBmp_NotEditMode() {
		var c = content();
		assertThrows(IllegalStateException.class, () -> BeMusicMeta.setBackBmp(c, "#BACKBMP"));
	}

	// setEyecatch(BmsContent, String), getEyecatch(BmsContent)
	// 設定した値を正しく取得できること
	@Test
	public void testXetEyecatch() {
		var c = content(e -> BeMusicMeta.setEyecatch(e, "eyecatch.png"));
		assertEquals("eyecatch.png", BeMusicMeta.getEyecatch(c));
	}

	// getEyecatch(BmsContent)
	// NullPointerException contentがnull
	@Test
	public void testGetEyecatch_NullContent() {
		assertThrows(NullPointerException.class, () -> BeMusicMeta.getEyecatch(null));
	}

	// setEyecatch(BmsContent, String)
	// NullPointerException contentがnull
	@Test
	public void testSetEyecatch_NullContent() {
		assertThrows(NullPointerException.class, () -> BeMusicMeta.setEyecatch(null, "#EYECATCH"));
	}

	// setEyecatch(BmsContent, String)
	// IllegalStateException BMSコンテンツが編集モードではない
	@Test
	public void testSetEyecatch_NotEditMode() {
		var c = content();
		assertThrows(IllegalStateException.class, () -> BeMusicMeta.setEyecatch(c, "#EYECATCH"));
	}

	// setPreview(BmsContent, String), getPreview(BmsContent)
	// 設定した値を正しく取得できること
	@Test
	public void testXetPreview() {
		var c = content(e -> BeMusicMeta.setPreview(e, "preview.ogg"));
		assertEquals("preview.ogg", BeMusicMeta.getPreview(c));
	}

	// getPreview(BmsContent)
	// NullPointerException contentがnull
	@Test
	public void testGetPreview_NullContent() {
		assertThrows(NullPointerException.class, () -> BeMusicMeta.getPreview(null));
	}

	// setPreview(BmsContent, String)
	// NullPointerException contentがnull
	@Test
	public void testSetPreview_NullContent() {
		assertThrows(NullPointerException.class, () -> BeMusicMeta.setPreview(null, "#PREVIEW"));
	}

	// setPreview(BmsContent, String)
	// IllegalStateException BMSコンテンツが編集モードではない
	@Test
	public void testSetPreview_NotEditMode() {
		var c = content();
		assertThrows(IllegalStateException.class, () -> BeMusicMeta.setPreview(c, "#PREVIEW"));
	}

	// setPlayLevel(BmsContent, Double), getPlayLevelRaw(BmsContent), getPlayLevel(BmsContent)
	// 設定した値を正しく取得できること
	@Test
	public void testXetPlayLevel() {
		var c = content(e -> BeMusicMeta.setPlayLevel(e, 12.7));
		assertEquals(12.7, BeMusicMeta.getPlayLevel(c), 0.0);
		assertEquals("12.7", BeMusicMeta.getPlayLevelRaw(c));
	}

	// getPlayLevelRaw(BmsContent)
	// NullPointerException contentがnull
	@Test
	public void testGetPlayLevelRaw_NullContent() {
		assertThrows(NullPointerException.class, () -> BeMusicMeta.getPlayLevelRaw(null));
	}

	// getPlayLevel(BmsContent)
	// NullPointerException contentがnull
	@Test
	public void testGetPlayLevel_NullContent() {
		assertThrows(NullPointerException.class, () -> BeMusicMeta.getPlayLevel(null));
	}

	// getPlayLevel(BmsContent, Double)
	// NullPointerException contentがnull
	@Test
	public void testSetPlayLevel_NullContent() {
		assertThrows(NullPointerException.class, () -> BeMusicMeta.setPlayLevel(null, 11.0));
	}

	// getPlayLevel(BmsContent, Double)
	// IllegalStateException BMSコンテンツが編集モードではない
	@Test
	public void testSetPlayLevel_NotEditMode() {
		var c = content();
		assertThrows(IllegalStateException.class, () -> BeMusicMeta.setPlayLevel(c, 10.0));
	}

	// setDifficulty(BmsContent, BeMusicDifficulty), getDifficulty(BmsContent)
	// 設定した値を正しく取得できること
	@Test
	public void testXetDifficulty() {
		var c = content(e -> BeMusicMeta.setDifficulty(e, BeMusicDifficulty.INSANE));
		assertEquals(BeMusicDifficulty.INSANE, BeMusicMeta.getDifficulty(c));
	}

	// getDifficulty(BmsContent)
	// NullPointerException contentがnull
	@Test
	public void testGetDifficulty_NullContent() {
		assertThrows(NullPointerException.class, () -> BeMusicMeta.getDifficulty(null));
	}

	// setDifficulty(BmsContent, BeMusicDifficulty)
	// NullPointerException contentがnull
	@Test
	public void testSetDifficulty_NullContent() {
		assertThrows(NullPointerException.class, () -> BeMusicMeta.setDifficulty(null, BeMusicDifficulty.HYPER));
	}

	// setDifficulty(BmsContent, BeMusicDifficulty)
	// IllegalStateException BMSコンテンツが編集モードではない
	@Test
	public void testSetDifficulty_NotEditMode() {
		var c = content();
		assertThrows(IllegalStateException.class, () -> BeMusicMeta.setDifficulty(c, BeMusicDifficulty.HYPER));
	}

	// setChartName(BmsContent, String), getChartName(BmsContent)
	// 設定した値を正しく取得できること
	@Test
	public void testXetChartName() {
		var c = content(e -> BeMusicMeta.setChartName(e, "[FOUR DIMENSION]"));
		assertEquals("[FOUR DIMENSION]", BeMusicMeta.getChartName(c));
	}

	// getChartName(BmsContent)
	// NullPointerException contentがnull
	@Test
	public void testGetChartName_NullContent() {
		assertThrows(NullPointerException.class, () -> BeMusicMeta.getChartName(null));
	}

	// setChartName(BmsContent, String)
	// NullPointerException contentがnull
	@Test
	public void testSetChartName_NullContent() {
		assertThrows(NullPointerException.class, () -> BeMusicMeta.setChartName(null, "#CHARTNAME"));
	}

	// setChartName(BmsContent, String)
	// IllegalStateException BMSコンテンツが編集モードではない
	@Test
	public void testSetChartName_NotEditMode() {
		var c = content();
		assertThrows(IllegalStateException.class, () -> BeMusicMeta.setChartName(c, "#CHARTNAME"));
	}

	// setLnObj(BmsContent, int, Long), getLnObjs(BmsContent), getLnObj(BmsContent, int)
	// 設定した値を正しく取得できること
	@Test
	public void testXetLnObj() {
		var c = content(e -> {
			BeMusicMeta.setLnObj(e, 0, (long)BmsInt.to36i("AA"));
			BeMusicMeta.setLnObj(e, 1, (long)BmsInt.to36i("BB"));
			BeMusicMeta.setLnObj(e, 2, (long)BmsInt.to36i("CC"));
		});
		assertEquals((long)BmsInt.to36i("AA"), BeMusicMeta.getLnObj(c, 0));
		assertEquals((long)BmsInt.to36i("BB"), BeMusicMeta.getLnObj(c, 1));
		assertEquals((long)BmsInt.to36i("CC"), BeMusicMeta.getLnObj(c, 2));
		var l = BeMusicMeta.getLnObjs(c);
		assertEquals(3, l.size());
		assertEquals((long)BmsInt.to36i("AA"), (long)l.get(0));
		assertEquals((long)BmsInt.to36i("BB"), (long)l.get(1));
		assertEquals((long)BmsInt.to36i("CC"), (long)l.get(2));
	}

	// getLnObjs(BmsContent)
	// NullPointerException contentがnull
	@Test
	public void testGetLnObjs_NullContent() {
		assertThrows(NullPointerException.class, () -> BeMusicMeta.getLnObjs(null));
	}

	// getLnObj(BmsContent, int)
	// NullPointerException contentがnull
	@Test
	public void testGetLnObj_NullContent() {
		assertThrows(NullPointerException.class, () -> BeMusicMeta.getLnObj(null, 0));
	}

	// getLnObj(BmsContent, int)
	// IndexOutOfBoundsException indexが0～BmsSpec.MULTIPLE_META_INDEX_MAXの範囲外
	@Test
	public void testGetLnObj_IndexOutOfRange() {
		var c = content();
		var ec = IndexOutOfBoundsException.class;
		assertThrows(ec, () -> BeMusicMeta.getLnObj(c, -1));
		assertThrows(ec, () -> BeMusicMeta.getLnObj(c, BmsSpec.MULTIPLE_META_INDEX_MAX + 1));
	}

	// setLnObj(BmsContent, int, Long)
	// NullPointerException contentがnull
	@Test
	public void testSetLnObj_NullContent() {
		assertThrows(NullPointerException.class, () -> BeMusicMeta.setLnObj(null, 0, 1L));
	}

	// setLnObj(BmsContent, int, Long)
	// IndexOutOfBoundsException indexが0～BmsSpec.MULTIPLE_META_INDEX_MAXの範囲外
	@Test
	public void testSetLnObj_IndexOutOfRange() {
		var c = content();
		var ec = IndexOutOfBoundsException.class;
		c.beginEdit();
		assertThrows(ec, () -> BeMusicMeta.setLnObj(c, -1, 1L));
		assertThrows(ec, () -> BeMusicMeta.setLnObj(c, BmsSpec.MULTIPLE_META_INDEX_MAX + 1, 1L));
	}

	// setLnObj(BmsContent, int, Long)
	// IllegalStateException BMSコンテンツが編集モードではない
	@Test
	public void testSetLnObj_NotEditMode() {
		var c = content();
		assertThrows(IllegalStateException.class, () -> BeMusicMeta.setLnObj(c, 0, 1L));
	}

	// setLnMode(BmsContent, BeMusicLongNoteMode), getLnMode(BmsContent)
	// 設定した値を正しく取得できること
	@Test
	public void testXetLnMode() {
		var c = content(e -> BeMusicMeta.setLnMode(e, BeMusicLongNoteMode.HCN));
		assertEquals(BeMusicLongNoteMode.HCN, BeMusicMeta.getLnMode(c));
	}

	// getLnMode(BmsContent)
	// NullPointerException contentがnull
	@Test
	public void testGetLnMode_NullContent() {
		assertThrows(NullPointerException.class, () -> BeMusicMeta.getLnMode(null));
	}

	// setLnMode(BmsContent, BeMusicLongNoteMode)
	// NullPointerException contentがnull
	@Test
	public void testSetLnMode_NullContent() {
		assertThrows(NullPointerException.class, () -> BeMusicMeta.setLnMode(null, BeMusicLongNoteMode.CN));
	}

	// setLnMode(BmsContent, BeMusicLongNoteMode)
	// IllegalStateException BMSコンテンツが編集モードではない
	@Test
	public void testSetLnMode_NotEditMode() {
		var c = content();
		assertThrows(IllegalStateException.class, () -> BeMusicMeta.setLnMode(c, BeMusicLongNoteMode.CN));
	}

	// setUrl(BmsContent, String), getUrl(BmsContent)
	// 設定した値を正しく取得できること
	@Test
	public void testXetUrl() {
		var c = content(e -> BeMusicMeta.setUrl(e, "http://www.lm-t.com/"));
		assertEquals("http://www.lm-t.com/", BeMusicMeta.getUrl(c));
	}

	// getUrl(BmsContent)
	// NullPointerException contentがnull
	@Test
	public void testGetUrl_NullContent() {
		assertThrows(NullPointerException.class, () -> BeMusicMeta.getUrl(null));
	}

	// setUrl(BmsContent, String)
	// NullPointerException contentがnull
	@Test
	public void testSetUrl_NullContent() {
		assertThrows(NullPointerException.class, () -> BeMusicMeta.setUrl(null, "http://abc.com/"));
	}

	// setUrl(BmsContent, String)
	// IllegalStateException BMSコンテンツが編集モードではない
	@Test
	public void testSetUrl_NotEditMode() {
		var c = content();
		assertThrows(IllegalStateException.class, () -> BeMusicMeta.setUrl(c, "http://abc.com/"));
	}

	// setEmail(BmsContent, String), getEmail(BmsContent)
	// 設定した値を正しく取得できること
	@Test
	public void testXetEmail() {
		var c = content(e -> BeMusicMeta.setEmail(e, "hogehoge@lm-t.com"));
		assertEquals("hogehoge@lm-t.com", BeMusicMeta.getEmail(c));
	}

	// getEmail(BmsContent)
	// NullPointerException contentがnull
	@Test
	public void testGetEmail_NullContent() {
		assertThrows(NullPointerException.class, () -> BeMusicMeta.getEmail(null));
	}

	// setEmail(BmsContent, String)
	// NullPointerException contentがnull
	@Test
	public void testSetEmail_NullContent() {
		assertThrows(NullPointerException.class, () -> BeMusicMeta.setEmail(null, "a@b.com"));
	}

	// setEmail(BmsContent, String)
	// IllegalStateException BMSコンテンツが編集モードではない
	@Test
	public void testSetEmail_NotEditMode() {
		var c = content();
		assertThrows(IllegalStateException.class, () -> BeMusicMeta.setEmail(c, "a@b.com"));
	}

	// setBpm(BmsContent, int, Double), getBpm(BmsContent, int), getBpms(BmsContent)
	// 設定した値を正しく取得できること
	@Test
	public void testXetBpm() {
		var c = content(e -> {
			BeMusicMeta.setBpm(e, 1, 150.0);
			BeMusicMeta.setBpm(e, 3, 180.0);
			BeMusicMeta.setBpm(e, 5, 210.0);
		});
		assertEquals(150.0, BeMusicMeta.getBpm(c, 1), 0.0);
		assertEquals(180.0, BeMusicMeta.getBpm(c, 3), 0.0);
		assertEquals(210.0, BeMusicMeta.getBpm(c, 5), 0.0);
		var l = BeMusicMeta.getBpms(c);
		assertEquals(3, l.size());
		assertEquals(150.0, l.get(1), 0.0);
		assertEquals(180.0, l.get(3), 0.0);
		assertEquals(210.0, l.get(5), 0.0);
	}

	// getBpms(BmsContent)
	// NullPointerException contentがnull
	@Test
	public void testGetBpms_NullContent() {
		assertThrows(NullPointerException.class, () -> BeMusicMeta.getBpms(null));
	}

	// getBpm(BmsContent, int)
	// NullPointerException contentがnull
	@Test
	public void testGetBpm_NullContent() {
		assertThrows(NullPointerException.class, () -> BeMusicMeta.getBpm(null, 0));
	}

	// getBpm(BmsContent, int)
	// IndexOutOfBoundsException indexが0～BmsSpec#INDEXED_META_INDEX_MAXの範囲外
	@Test
	public void testGetBpm_IndexOutOfRange() {
		var c = content();
		var ec = IndexOutOfBoundsException.class;
		assertThrows(ec, () -> BeMusicMeta.getBpm(c, -1));
		assertThrows(ec, () -> BeMusicMeta.getBpm(c, BmsSpec.INDEXED_META_INDEX_MAX + 1));
	}

	// setBpm(BmsContent, int, Double)
	// NullPointerException contentがnull
	@Test
	public void testSetBpm_NullContent() {
		assertThrows(NullPointerException.class, () -> BeMusicMeta.setBpm(null, 0, 150.0));
	}

	// setBpm(BmsContent, int, Double)
	// IndexOutOfBoundsException indexが0～BmsSpec#INDEXED_META_INDEX_MAXの範囲外
	@Test
	public void testSetBpm_IndexOutOfRange() {
		var c = content();
		var ec = IndexOutOfBoundsException.class;
		c.beginEdit();
		assertThrows(ec, () -> BeMusicMeta.setBpm(c, -1, 150.0));
		assertThrows(ec, () -> BeMusicMeta.setBpm(c, BmsSpec.INDEXED_META_INDEX_MAX + 1, 180.0));
	}

	// setBpm(BmsContent, int, Double)
	// IllegalStateException BMSコンテンツが編集モードではない
	@Test
	public void testSetBpm_NotEditMode() {
		var c = content();
		assertThrows(IllegalStateException.class, () -> BeMusicMeta.setBpm(c, 0, 150.0));
	}

	// setStop(BmsContent, int, Double), getStop(BmsContent, int), getStops(BmsContent)
	// 設定した値を正しく取得できること
	@Test
	public void testXetStop() {
		var c = content(e -> {
			BeMusicMeta.setStop(e, 1, 192.0);
			BeMusicMeta.setStop(e, 3, 384.0);
			BeMusicMeta.setStop(e, 5, 576.0);
		});
		assertEquals(192.0, BeMusicMeta.getStop(c, 1), 0.0);
		assertEquals(384.0, BeMusicMeta.getStop(c, 3), 0.0);
		assertEquals(576.0, BeMusicMeta.getStop(c, 5), 0.0);
		var l = BeMusicMeta.getStops(c);
		assertEquals(3, l.size());
		assertEquals(192.0, l.get(1), 0.0);
		assertEquals(384.0, l.get(3), 0.0);
		assertEquals(576.0, l.get(5), 0.0);
	}

	// getStops(BmsContent)
	// NullPointerException contentがnull
	@Test
	public void testGetStops_NullContent() {
		assertThrows(NullPointerException.class, () -> BeMusicMeta.getStops(null));
	}

	// getStop(BmsContent, int)
	// NullPointerException contentがnull
	@Test
	public void testGetStop_NullContent() {
		assertThrows(NullPointerException.class, () -> BeMusicMeta.getStop(null, 0));
	}

	// getStop(BmsContent, int)
	// IndexOutOfBoundsException indexが0～BmsSpec#INDEXED_META_INDEX_MAXの範囲外
	@Test
	public void testGetStop_IndexOutOfRange() {
		var c = content();
		var ec = IndexOutOfBoundsException.class;
		assertThrows(ec, () -> BeMusicMeta.getStop(c, -1));
		assertThrows(ec, () -> BeMusicMeta.getStop(c, BmsSpec.INDEXED_META_INDEX_MAX + 1));
	}

	// setStop(BmsContent, int, Double)
	// NullPointerException contentがnull
	@Test
	public void testSetStop_NullContent() {
		assertThrows(NullPointerException.class, () -> BeMusicMeta.setStop(null, 0, 192.0));
	}

	// setStop(BmsContent, int, Double)
	// IndexOutOfBoundsException indexが0～BmsSpec#INDEXED_META_INDEX_MAXの範囲外
	@Test
	public void testSetStop_IndexOutOfRange() {
		var c = content();
		var ec = IndexOutOfBoundsException.class;
		c.beginEdit();
		assertThrows(ec, () -> BeMusicMeta.setStop(c, -1, 192.0));
		assertThrows(ec, () -> BeMusicMeta.setStop(c, BmsSpec.INDEXED_META_INDEX_MAX + 1, 384.0));
	}

	// setStop(BmsContent, int, Double)
	// IllegalStateException BMSコンテンツが編集モードではない
	@Test
	public void testSetStop_NoEditMode() {
		var c = content();
		assertThrows(IllegalStateException.class, () -> BeMusicMeta.setStop(c, 0, 192.0));
	}

	// setScroll(BmsContent, int, Double), getScroll(BmsContent, int), getScrolls(BmsContent)
	// 設定した値を正しく取得できること
	@Test
	public void testXetScroll() {
		var c = content(e -> {
			BeMusicMeta.setScroll(e, 1, 1.5);
			BeMusicMeta.setScroll(e, 3, 3.0);
			BeMusicMeta.setScroll(e, 5, 4.5);
		});
		assertEquals(1.5, BeMusicMeta.getScroll(c, 1), 0.0);
		assertEquals(3.0, BeMusicMeta.getScroll(c, 3), 0.0);
		assertEquals(4.5, BeMusicMeta.getScroll(c, 5), 0.0);
		var l = BeMusicMeta.getScrolls(c);
		assertEquals(3, l.size());
		assertEquals(1.5, l.get(1), 0.0);
		assertEquals(3.0, l.get(3), 0.0);
		assertEquals(4.5, l.get(5), 0.0);
	}

	// getScrolls(BmsContent)
	// NullPointerException contentがnull
	@Test
	public void testGetGetScrolls_NullContent() {
		assertThrows(NullPointerException.class, () -> BeMusicMeta.getScrolls(null));
	}

	// getScroll(BmsContent, int)
	// NullPointerException contentがnull
	@Test
	public void testGetScroll_NullContent() {
		assertThrows(NullPointerException.class, () -> BeMusicMeta.getScroll(null, 0));
	}

	// getScroll(BmsContent, int)
	// IndexOutOfBoundsException indexが0～BmsSpec#INDEXED_META_INDEX_MAXの範囲外
	@Test
	public void testGetScroll_IndexOutOfRange() {
		var c = content();
		var ec = IndexOutOfBoundsException.class;
		assertThrows(ec, () -> BeMusicMeta.getScroll(c, -1));
		assertThrows(ec, () -> BeMusicMeta.getScroll(c, BmsSpec.INDEXED_META_INDEX_MAX + 1));
	}

	// setScroll(BmsContent, int, Double)
	// NullPointerException contentがnull
	@Test
	public void testSetScroll_NullContent() {
		assertThrows(NullPointerException.class, () -> BeMusicMeta.setScroll(null, 0, 1.5));
	}

	// setScroll(BmsContent, int, Double)
	// IndexOutOfBoundsException indexが0～BmsSpec#INDEXED_META_INDEX_MAXの範囲外
	@Test
	public void testSetScroll_IndexOutOfRange() {
		var c = content();
		var ec = IndexOutOfBoundsException.class;
		c.beginEdit();
		assertThrows(ec, () -> BeMusicMeta.setScroll(c, -1, 1.5));
		assertThrows(ec, () -> BeMusicMeta.setScroll(c, BmsSpec.INDEXED_META_INDEX_MAX + 1, 3.0));
	}

	// setScroll(BmsContent, int, Double)
	// IllegalStateException BMSコンテンツが編集モードではない
	@Test
	public void testSetScroll_NotEditMode() {
		var c = content();
		assertThrows(IllegalStateException.class, () -> BeMusicMeta.setScroll(c, 0, 1.5));
	}

	// setWav(BmsContent, int, String), getWav(BmsContent, int), getWavs(BmsContent)
	// 設定した値を正しく取得できること
	@Test
	public void testXetWav() {
		var c = content(e -> {
			BeMusicMeta.setWav(e, 1, "a.wav");
			BeMusicMeta.setWav(e, 3, "b.ogg");
			BeMusicMeta.setWav(e, 5, "c.mp3");
		});
		assertEquals("a.wav", BeMusicMeta.getWav(c, 1));
		assertEquals("b.ogg", BeMusicMeta.getWav(c, 3));
		assertEquals("c.mp3", BeMusicMeta.getWav(c, 5));
		var l = BeMusicMeta.getWavs(c);
		assertEquals(3, l.size());
		assertEquals("a.wav", l.get(1));
		assertEquals("b.ogg", l.get(3));
		assertEquals("c.mp3", l.get(5));
	}

	// getWavs(BmsContent)
	// NullPointerException contentがnull
	@Test
	public void testGetWavs_NullContent() {
		assertThrows(NullPointerException.class, () -> BeMusicMeta.getWavs(null));
	}

	// getWav(BmsContent, int)
	// NullPointerException contentがnull
	@Test
	public void testGetWav_NullContent() {
		assertThrows(NullPointerException.class, () -> BeMusicMeta.getWav(null, 0));
	}

	// getWav(BmsContent, int)
	// IndexOutOfBoundsException indexが0～BmsSpec#INDEXED_META_INDEX_MAXの範囲外
	@Test
	public void testGetWav_IndexOutOfRange() {
		var c = content();
		var ec = IndexOutOfBoundsException.class;
		assertThrows(ec, () -> BeMusicMeta.getWav(c, -1));
		assertThrows(ec, () -> BeMusicMeta.getWav(c, BmsSpec.INDEXED_META_INDEX_MAX + 1));
	}

	// setWav(BmsContent, int, String)
	// NullPointerException contentがnull
	@Test
	public void testSetWav_NullContent() {
		assertThrows(NullPointerException.class, () -> BeMusicMeta.setWav(null, 0, "a.wav"));
	}

	// setWav(BmsContent, int, String)
	// IndexOutOfBoundsException indexが0～BmsSpec#INDEXED_META_INDEX_MAXの範囲外
	@Test
	public void testSetWav_IndexOutOfRange() {
		var c = content();
		var ec = IndexOutOfBoundsException.class;
		c.beginEdit();
		assertThrows(ec, () -> BeMusicMeta.setWav(c, -1, "a.wav"));
		assertThrows(ec, () -> BeMusicMeta.setWav(c, BmsSpec.INDEXED_META_INDEX_MAX + 1, "b.ogg"));
	}

	// setWav(BmsContent, int, String)
	// IllegalStateException BMSコンテンツが編集モードではない
	@Test
	public void testSetWav_NotEditMode() {
		var c = content();
		assertThrows(IllegalStateException.class, () -> BeMusicMeta.setWav(c, 0, "a.wav"));
	}

	// setBmp(BmsContent, int, String), getBmp(BmsContent, int), getBmps(BmsContent)
	// 設定した値を正しく取得できること
	@Test
	public void testXetBmp() {
		var c = content(e -> {
			BeMusicMeta.setBmp(e, 1, "a.bmp");
			BeMusicMeta.setBmp(e, 3, "b.jpg");
			BeMusicMeta.setBmp(e, 5, "c.png");
		});
		assertEquals("a.bmp", BeMusicMeta.getBmp(c, 1));
		assertEquals("b.jpg", BeMusicMeta.getBmp(c, 3));
		assertEquals("c.png", BeMusicMeta.getBmp(c, 5));
		var l = BeMusicMeta.getBmps(c);
		assertEquals(3, l.size());
		assertEquals("a.bmp", l.get(1));
		assertEquals("b.jpg", l.get(3));
		assertEquals("c.png", l.get(5));
	}

	// getBmps(BmsContent)
	// NullPointerException contentがnull
	@Test
	public void testGetBmps_NullContent() {
		assertThrows(NullPointerException.class, () -> BeMusicMeta.getBmps(null));
	}

	// getBmp(BmsContent, int)
	// NullPointerException contentがnull
	@Test
	public void testGetBmp_NullContent() {
		assertThrows(NullPointerException.class, () -> BeMusicMeta.getBmp(null, 0));
	}

	// getBmp(BmsContent, int)
	// IndexOutOfBoundsException indexが0～BmsSpec#INDEXED_META_INDEX_MAXの範囲外
	@Test
	public void testGetBmp_IndexOutOfRange() {
		var c = content();
		var ec = IndexOutOfBoundsException.class;
		assertThrows(ec, () -> BeMusicMeta.getBmp(c, -1));
		assertThrows(ec, () -> BeMusicMeta.getBmp(c, BmsSpec.INDEXED_META_INDEX_MAX + 1));
	}

	// setBmp(BmsContent, int, String)
	// NullPointerException contentがnull
	@Test
	public void testSetBmp_NullContent() {
		assertThrows(NullPointerException.class, () -> BeMusicMeta.setBmp(null, 0, "a.bmp"));
	}

	// setBmp(BmsContent, int, String)
	// IndexOutOfBoundsException indexが0～BmsSpec#INDEXED_META_INDEX_MAXの範囲外
	@Test
	public void testSetBmp_IndexOutOfRange() {
		var c = content();
		var ec = IndexOutOfBoundsException.class;
		c.beginEdit();
		assertThrows(ec, () -> BeMusicMeta.setBmp(c, -1, "a.bmp"));
		assertThrows(ec, () -> BeMusicMeta.setBmp(c, BmsSpec.INDEXED_META_INDEX_MAX + 1, "b.jpg"));
	}

	// setBmp(BmsContent, int, String)
	// IllegalStateException BMSコンテンツが編集モードではない
	@Test
	public void testSetBmp_NotEditMode() {
		var c = content();
		assertThrows(IllegalStateException.class, () -> BeMusicMeta.setBmp(c, 0, "a.bmp"));
	}

	// setText(BmsContent, int, String), getText(BmsContent, int), getTexts(BmsContent)
	// 設定した値を正しく取得できること
	@Test
	public void testXetText() {
		var c = content(e -> {
			BeMusicMeta.setText(e, 1, "hoge");
			BeMusicMeta.setText(e, 3, "hage");
			BeMusicMeta.setText(e, 5, "hige");
		});
		assertEquals("hoge", BeMusicMeta.getText(c, 1));
		assertEquals("hage", BeMusicMeta.getText(c, 3));
		assertEquals("hige", BeMusicMeta.getText(c, 5));
		var l = BeMusicMeta.getTexts(c);
		assertEquals(3, l.size());
		assertEquals("hoge", l.get(1));
		assertEquals("hage", l.get(3));
		assertEquals("hige", l.get(5));
	}

	// getTexts(BmsContent)
	// NullPointerException contentがnull
	@Test
	public void testGetTexts_NullContent() {
		assertThrows(NullPointerException.class, () -> BeMusicMeta.getTexts(null));
	}

	// getText(BmsContent, int)
	// NullPointerException contentがnull
	@Test
	public void testGetText_NullContent() {
		assertThrows(NullPointerException.class, () -> BeMusicMeta.getText(null, 0));
	}

	// getText(BmsContent, int)
	// IndexOutOfBoundsException indexが0～BmsSpec#INDEXED_META_INDEX_MAXの範囲外
	@Test
	public void testGetText_IndexOutOfRange() {
		var c = content();
		var ec = IndexOutOfBoundsException.class;
		assertThrows(ec, () -> BeMusicMeta.getText(c, -1));
		assertThrows(ec, () -> BeMusicMeta.getText(c, BmsSpec.INDEXED_META_INDEX_MAX + 1));
	}

	// setText(BmsContent, int, String)
	// NullPointerException contentがnull
	@Test
	public void testSetText_NullContent() {
		assertThrows(NullPointerException.class, () -> BeMusicMeta.setText(null, 0, "hoge"));
	}

	// setText(BmsContent, int, String)
	// IndexOutOfBoundsException indexが0～BmsSpec#INDEXED_META_INDEX_MAXの範囲外
	@Test
	public void testSetText_IndexOutOfRange() {
		var c = content();
		var ec = IndexOutOfBoundsException.class;
		c.beginEdit();
		assertThrows(ec, () -> BeMusicMeta.setText(c, -1, "hoge"));
		assertThrows(ec, () -> BeMusicMeta.setText(c, BmsSpec.INDEXED_META_INDEX_MAX + 1, "hage"));
	}

	// setText(BmsContent, int, String)
	// IllegalStateException BMSコンテンツが編集モードではない
	@Test
	public void testSetText_NotEditMode() {
		var c = content();
		assertThrows(IllegalStateException.class, () -> BeMusicMeta.setText(c, 0, "hoge"));
	}

	private static BmsContent content() {
		return content(c -> {});
	}

	private static BmsContent content(Consumer<BmsContent> editor) {
		var c = new BmsContent(BeMusicSpec.LATEST);
		c.beginEdit();
		editor.accept(c);
		c.endEdit();
		return c;
	}
}
