package com.lmt.lib.bms.bemusic;

import static org.junit.Assert.*;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.junit.Test;

import com.lmt.lib.bms.BmsCompatException;
import com.lmt.lib.bms.BmsContent;
import com.lmt.lib.bms.BmsErrorType;
import com.lmt.lib.bms.BmsScriptError;
import com.lmt.lib.bms.BmsSpec;
import com.lmt.lib.bms.UseTestData;

public class BeMusicBmsonLoaderTest implements UseTestData {
	@Override
	public Path testDataDirectory() {
		return Path.of("test", "com", "lmt", "lib", "bms", "bemusic", "testdata", "bemusicbmsonloader");
	}

	@Override
	public String testDataExtension() {
		return "bmson";
	}

	// isStandard()
	// falseを返すこと
	@Test
	public void testIsStandard() {
		var l = new BeMusicBmsonLoader();
		assertFalse(l.isStandard());
	}

	// isBinaryFormat()
	// falseを返すこと
	@Test
	public void testIsBinaryFormat() {
		var l = new BeMusicBmsonLoader();
		assertFalse(l.isBinaryFormat());
	}

	// 全般：JSON構文エラーの場合BmsCompatExceptionがスローされること
	@Test
	public void test_Common_WrongJsonSyntax() throws Exception {
		var path = testDataPath();
		assertThrows(BmsCompatException.class, () -> content(path));
	}

	// 全般：複数言語の定義が入っていても正常に文字列がデコードされ読み込めること
	@Test
	public void test_Common_MultiLanguage() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals("日本語のタイトル", BeMusicMeta.getTitle(r.c));
		assertEquals("한국어의 부제", BeMusicMeta.getSubTitle(r.c));
	}

	// version：未定義の場合BmsCompatExceptionがスローされること
	@Test
	public void test_Version_Undefined() throws Exception {
		var path = testDataPath();
		assertThrows(BmsCompatException.class, () -> content(path));
	}

	// version：定義を文字列に変換できない場合BmsCompatExceptionがスローされること
	@Test
	public void test_Version_NotString() throws Exception {
		var path = testDataPath();
		assertThrows(BmsCompatException.class, () -> content(path));
	}

	// version：バージョンが"1.0.0"でない場合BmsCompatExceptionがスローされること
	@Test
	public void test_Version_IncompatibleVersion() throws Exception {
		var path = testDataPath();
		assertThrows(BmsCompatException.class, () -> content(path));
	}

	// version：バージョンが"1.0.0"であれば正常に解析処理が続行されること
	@Test
	public void test_Version_CompatibleVersion() throws Exception {
		content();  // 読み込みのみを確認する
	}

	// info：infoが未定義の場合BmsCompatExceptionがスローされること
	@Test
	public void test_Info_InfoUndefined() throws Exception {
		var path = testDataPath();
		assertThrows(BmsCompatException.class, () -> content(path));
	}

	// info：infoがJSONオブジェクトでない場合BmsCompatExceptionがスローされること
	@Test
	public void test_Info_InfoNotObject() throws Exception {
		var path = testDataPath();
		assertThrows(BmsCompatException.class, () -> content(path));
	}

	// info：mode_hint："beat-5k"の時、#PLAYERがSINGLEになること
	@Test
	public void test_Info_ModeHint_Beat5k() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals(BeMusicPlayer.SINGLE, BeMusicMeta.getPlayer(r.c));
	}

	// info：mode_hint："beat-7k"の時、#PLAYERがSINGLEになること
	@Test
	public void test_Info_ModeHint_Beat7k() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals(BeMusicPlayer.SINGLE, BeMusicMeta.getPlayer(r.c));
	}

	// info：mode_hint："beat-10k"の時、#PLAYERがDOUBLEになること
	@Test
	public void test_Info_ModeHint_Beat10k() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals(BeMusicPlayer.DOUBLE, BeMusicMeta.getPlayer(r.c));
	}

	// info：mode_hint："beat-14k"の時、#PLAYERがDOUBLEになること
	@Test
	public void test_Info_ModeHint_Beat14k() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals(BeMusicPlayer.DOUBLE, BeMusicMeta.getPlayer(r.c));
	}

	// info：mode_hint：未定義の時、#PLAYERがSINGLEになること
	@Test
	public void test_Info_ModeHint_Undefined() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals(BeMusicPlayer.SINGLE, BeMusicMeta.getPlayer(r.c));
	}

	// info：mode_hint：非対応モードの時、BmsCompatExceptionがスローされること
	@Test
	public void test_Info_ModeHint_WrongValue() throws Exception {
		var path = testDataPath();
		assertThrows(BmsCompatException.class, () -> content(path));
	}

	// info：mode_hint：文字列に変換できない時、BmsCompatExceptionがスローされること
	@Test
	public void test_Info_ModeHint_WrongDefinition() throws Exception {
		var path = testDataPath();
		assertThrows(BmsCompatException.class, () -> content(path));
	}

	// info：resolution：1以上の正の整数の時、その値が解像度になること
	@Test
	public void test_Info_Resolution_PlusValue() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals(1, r.c.getNote(chInvisible(BeMusicDevice.SWITCH11), 0, 96.0).getValue());
	}

	// info：resolution：-1以下の負の整数の時、その値の絶対値が解像度になること
	@Test
	public void test_Info_Resolution_MinusValue() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals(1, r.c.getNote(chInvisible(BeMusicDevice.SWITCH12), 0, 72.0).getValue());
	}

	// info：resolution：実数の時、その値の整数が解像度になること
	@Test
	public void test_Info_Resolution_FloatValue() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals(1, r.c.getNote(chInvisible(BeMusicDevice.SWITCH13), 0, 144.0).getValue());
	}

	// info：resolution：未定義の時、解像度が240になること
	@Test
	public void test_Info_Resolution_Undefined() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals(1, r.c.getNote(chInvisible(BeMusicDevice.SWITCH14), 0, 48.0).getValue());
	}

	// info：resolution：0の時、BmsCompatExceptionがスローされること
	@Test
	public void test_Info_Resolution_Zero() throws Exception {
		var path = testDataPath();
		assertThrows(BmsCompatException.class, () -> content(path));
	}

	// info：resolution：数値でない時、BmsCompatExceptionがスローされること
	@Test
	public void test_Info_Resolution_NotNumber() throws Exception {
		var path = testDataPath();
		assertThrows(BmsCompatException.class, () -> content(path));
	}

	// info：genre：指定された文字列が#GENREに設定されること
	@Test
	public void test_Info_Genre_Normal() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals("EUROBEAT", BeMusicMeta.getGenre(r.c));
	}

	// info：genre：空文字列の時、#GENREが未定義になること
	@Test
	public void test_Info_Genre_Empty() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals(false, r.c.containsSingleMeta(BeMusicMeta.GENRE.getName()));
	}

	// info：genre：未定義の時、UNKNOWN_METAが報告され、#GENREが未定義になること
	@Test
	public void test_Info_Genre_Undefined() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.UNKNOWN_META, r.e.get(0).getType());
		assertEquals(false, r.c.containsSingleMeta(BeMusicMeta.GENRE.getName()));
	}

	// info：genre：文字列に変換できない時、WRONG_DATAが報告され、#GENREが未定義になること
	@Test
	public void test_Info_Genre_WrongDefinition() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.WRONG_DATA, r.e.get(0).getType());
		assertEquals(false, r.c.containsSingleMeta(BeMusicMeta.GENRE.getName()));
	}

	// info：title：指定された文字列が#TITLEに設定されること
	@Test
	public void test_Info_Title_Normal() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals("My Song", BeMusicMeta.getTitle(r.c));
	}

	// info：title：空文字列の時、#TITLEが空文字列で設定されること
	@Test
	public void test_Info_Title_Empty() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals(true, r.c.containsSingleMeta(BeMusicMeta.TITLE.getName()));
		assertEquals("", BeMusicMeta.getTitle(r.c));
	}

	// info：title：未定義の時、UNKNOWN_METAが報告され、#TITLEが空文字列で設定されること
	@Test
	public void test_Info_Title_Undefined() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.UNKNOWN_META, r.e.get(0).getType());
		assertEquals(true, r.c.containsSingleMeta(BeMusicMeta.TITLE.getName()));
		assertEquals("", BeMusicMeta.getTitle(r.c));
	}

	// info：title：文字列に変換できない時、WRONG_DATAが報告され、#TITLEが空文字列で設定されること
	@Test
	public void test_Info_Title_WrongDefinition() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.WRONG_DATA, r.e.get(0).getType());
		assertEquals(true, r.c.containsSingleMeta(BeMusicMeta.TITLE.getName()));
		assertEquals("", BeMusicMeta.getTitle(r.c));
	}

	// info：subtitle：指定された文字列が#SUBTITLEに設定されること
	@Test
	public void test_Info_SubTitle_Normal() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals("- for my lover -", BeMusicMeta.getSubTitle(r.c));
	}

	// info：subtitle：空文字列の時、#SUBTITLEが未定義になること
	@Test
	public void test_Info_SubTitle_Empty() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals(false, r.c.containsSingleMeta(BeMusicMeta.SUBTITLE.getName()));
	}

	// info：subtitle：未定義の時、UNKNOWN_METAが報告され、#SUBTITLEが未定義になること
	@Test
	public void test_Info_SubTitle_Undefined() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.UNKNOWN_META, r.e.get(0).getType());
		assertEquals(false, r.c.containsSingleMeta(BeMusicMeta.SUBTITLE.getName()));
	}

	// info：subtitle：文字列に変換できない時、WRONG_DATAが報告され、#SUBTITLEが未定義になること
	@Test
	public void test_Info_SubTitle_WrongDefinition() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.WRONG_DATA, r.e.get(0).getType());
		assertEquals(false, r.c.containsSingleMeta(BeMusicMeta.SUBTITLE.getName()));
	}

	// info：artist：指定された文字列が#ARTISTに設定されること
	@Test
	public void test_Info_Artist_Normal() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals("J-SON3", BeMusicMeta.getArtist(r.c));
	}

	// info：artist：空文字列の時、#ARTISTが空文字列で設定されること
	@Test
	public void test_Info_Artist_Empty() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals(true, r.c.containsSingleMeta(BeMusicMeta.ARTIST.getName()));
		assertEquals("", BeMusicMeta.getArtist(r.c));
	}

	// info：artist：未定義の時、UNKNOWN_METAが報告され、#ARTISTが空文字列で設定されること
	@Test
	public void test_Info_Artist_Undefined() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.UNKNOWN_META, r.e.get(0).getType());
		assertEquals(true, r.c.containsSingleMeta(BeMusicMeta.ARTIST.getName()));
		assertEquals("", BeMusicMeta.getArtist(r.c));
	}

	// info：artist：文字列に変換できない時、WRONG_DATAが報告され、#ARTISTが空文字列で設定されること
	@Test
	public void test_Info_Artist_WrongDefinition() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.WRONG_DATA, r.e.get(0).getType());
		assertEquals(true, r.c.containsSingleMeta(BeMusicMeta.ARTIST.getName()));
		assertEquals("", BeMusicMeta.getArtist(r.c));
	}

	// info：subartists：配列の1件の文字列が#SUBARTISTに設定されること
	@Test
	public void test_Info_SubArtist_Single() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals(1, r.c.getMultipleMetaCount(BeMusicMeta.SUBARTIST.getName()));
		assertEquals("Michael", BeMusicMeta.getSubArtist(r.c, 0));
	}

	// info：subartists：配列の複数件の文字列が定義順に#SUBARTISTに設定されること
	@Test
	public void test_Info_SubArtist_Multiple() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals(3, r.c.getMultipleMetaCount(BeMusicMeta.SUBARTIST.getName()));
		assertEquals("Cate", BeMusicMeta.getSubArtist(r.c, 0));
		assertEquals("Kyle", BeMusicMeta.getSubArtist(r.c, 1));
		assertEquals("100", BeMusicMeta.getSubArtist(r.c, 2));
	}

	// info：subartists：配列の要素で文字列に変換できない要素があるとWRONG_DATAが報告され、当該要素が#SUBARTISTに設定されないこと
	@Test
	public void test_Info_SubArtist_HasWrongDefinition() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.WRONG_DATA, r.e.get(0).getType());
		assertEquals(2, r.c.getMultipleMetaCount(BeMusicMeta.SUBARTIST.getName()));
		assertEquals("Michael", BeMusicMeta.getSubArtist(r.c, 0));
		assertEquals("Ray", BeMusicMeta.getSubArtist(r.c, 1));
	}

	// info：subartists：配列が0件の時、#SUBARTISTが未定義になること
	@Test
	public void test_Info_SubArtist_Empty() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals(false, r.c.containsMultipleMeta(BeMusicMeta.SUBARTIST.getName(), 0));
	}

	// info：subartists：未定義の時、#SUBARTISTが未定義になること
	@Test
	public void test_Info_SubArtist_Undefined() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals(false, r.c.containsMultipleMeta(BeMusicMeta.SUBARTIST.getName(), 0));
	}

	// info：subartists：配列以外の時、WRONG_DATAが報告され、#SUBARTISTが未定義になること
	@Test
	public void test_Info_SubArtist_WrongDefinition() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.WRONG_DATA, r.e.get(0).getType());
		assertEquals(false, r.c.containsMultipleMeta(BeMusicMeta.SUBARTIST.getName(), 0));
	}

	// info：chart_name：指定された文字列が#CHARTNAMEに設定され、#DIFFICULTYが未定義になること
	@Test
	public void test_Info_ChartName_Normal() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals("FOUR DIMENSION", BeMusicMeta.getChartName(r.c));
		assertEquals(false, r.c.containsSingleMeta(BeMusicMeta.DIFFICULTY.getName()));
	}

	// info：chart_name："beginner"の時、#CHARTNAMEが未定義になり、#DIFFICULTYがBEGINNERに設定されること
	@Test
	public void test_Info_ChartName_DifficultyBeginner() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals(false, r.c.containsSingleMeta(BeMusicMeta.CHARTNAME.getName()));
		assertEquals(BeMusicDifficulty.BEGINNER, BeMusicMeta.getDifficulty(r.c));
	}

	// info：chart_name："normal"の時、#CHARTNAMEが未定義になり、#DIFFICULTYがNORMALに設定されること
	@Test
	public void test_Info_ChartName_DifficultyNormal() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals(false, r.c.containsSingleMeta(BeMusicMeta.CHARTNAME.getName()));
		assertEquals(BeMusicDifficulty.NORMAL, BeMusicMeta.getDifficulty(r.c));
	}

	// info：chart_name："hyper"の時、#CHARTNAMEが未定義になり、#DIFFICULTYがHYPERに設定されること
	@Test
	public void test_Info_ChartName_DifficultyHyper() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals(false, r.c.containsSingleMeta(BeMusicMeta.CHARTNAME.getName()));
		assertEquals(BeMusicDifficulty.HYPER, BeMusicMeta.getDifficulty(r.c));
	}

	// info：chart_name："another"の時、#CHARTNAMEが未定義になり、#DIFFICULTYがANOTHERに設定されること
	@Test
	public void test_Info_ChartName_DifficultyAnother() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals(false, r.c.containsSingleMeta(BeMusicMeta.CHARTNAME.getName()));
		assertEquals(BeMusicDifficulty.ANOTHER, BeMusicMeta.getDifficulty(r.c));
	}

	// info：chart_name："insane"の時、#CHARTNAMEが未定義になり、#DIFFICULTYがINSANEに設定されること
	@Test
	public void test_Info_ChartName_DifficultyInsane() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals(false, r.c.containsSingleMeta(BeMusicMeta.CHARTNAME.getName()));
		assertEquals(BeMusicDifficulty.INSANE, BeMusicMeta.getDifficulty(r.c));
	}

	// info：chart_name：空文字列の時、#CHARTNAMEと#DIFFICULTYが未定義になること
	@Test
	public void test_Info_ChartName_Empty() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals(false, r.c.containsSingleMeta(BeMusicMeta.CHARTNAME.getName()));
		assertEquals(false, r.c.containsSingleMeta(BeMusicMeta.DIFFICULTY.getName()));
	}

	// info：chart_name：未定義の時、UNKNOWN_METAが報告され、#CHARTNAMEと#DIFFICULTYが未定義になること
	@Test
	public void test_Info_ChartName_Undefined() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.UNKNOWN_META, r.e.get(0).getType());
		assertEquals(false, r.c.containsSingleMeta(BeMusicMeta.CHARTNAME.getName()));
		assertEquals(false, r.c.containsSingleMeta(BeMusicMeta.DIFFICULTY.getName()));
	}

	// info：chart_name：文字列に変換できない時、WRONG_DATAが報告され、#CHARTNAMEと#DIFFICULTYが未定義になること
	@Test
	public void test_Info_ChartName_WrongDefinition() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.WRONG_DATA, r.e.get(0).getType());
		assertEquals(false, r.c.containsSingleMeta(BeMusicMeta.CHARTNAME.getName()));
		assertEquals(false, r.c.containsSingleMeta(BeMusicMeta.DIFFICULTY.getName()));
	}

	// info：level：指定された数値が文字列に変換され、#PLAYLEVELに設定されること
	@Test
	public void test_Info_Level_Normal() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals("12", BeMusicMeta.getPlayLevelRaw(r.c));
		assertEquals(12.0, BeMusicMeta.getPlayLevel(r.c), 0.0);
	}

	// info：level：負の値がそのまま文字列に変換され、#PLAYLEVELに設定されること
	@Test
	public void test_Info_Level_MinusValue() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals("-3", BeMusicMeta.getPlayLevelRaw(r.c));
		assertEquals(-3.0, BeMusicMeta.getPlayLevel(r.c), 0.0);
	}

	// info：level：未定義の時、UNKNOWN_METAが報告され、#PLAYLEVELが0に設定されること
	@Test
	public void test_Info_Level_Undefined() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.UNKNOWN_META, r.e.get(0).getType());
		assertEquals(true, r.c.containsSingleMeta(BeMusicMeta.PLAYLEVEL.getName()));
		assertEquals("0", BeMusicMeta.getPlayLevelRaw(r.c));
	}

	// info：level：文字列に変換できない時、WRONG_DATAが報告され、#PLAYLEVELが0に設定されること
	@Test
	public void test_Info_Level_WrongDefinition() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.WRONG_DATA, r.e.get(0).getType());
		assertEquals(true, r.c.containsSingleMeta(BeMusicMeta.PLAYLEVEL.getName()));
		assertEquals("0", BeMusicMeta.getPlayLevelRaw(r.c));
	}

	// info：init_bpm：指定された数値が#BPMに設定されること
	@Test
	public void test_Info_InitBpm_Normal() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals(150.0, r.c.getInitialBpm(), 0.0);
	}

	// info：init_bpm：小数部のある数値が#BPMに設定されること
	@Test
	public void test_Info_InitBpm_FloatValue() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals(180.123, r.c.getInitialBpm(), 0.00000001);
	}

	// info：init_bpm：ライブラリ下限値を下回る数値の時、SPEC_VIOLATIONが報告され、#BPMが未定義になること
	@Test
	public void test_Info_InitBpm_Underflow() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.SPEC_VIOLATION, r.e.get(0).getType());
		assertEquals(false, r.c.containsSingleMeta(BeMusicMeta.INITIAL_BPM.getName()));
	}

	// info：init_bpm：ライブラリ上限値を下回る数値の時、SPEC_VIOLATIONが報告され、#BPMが未定義になること
	@Test
	public void test_Info_InitBpm_Overflow() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.SPEC_VIOLATION, r.e.get(0).getType());
		assertEquals(false, r.c.containsSingleMeta(BeMusicMeta.INITIAL_BPM.getName()));
	}

	// info：init_bpm：仕様違反修正ONでライブラリ下限値を下回る数値の時、#BPMが下限値に設定されること
	@Test
	public void test_Info_InitBpm_FixUnderflow() throws Exception {
		var r = content(l -> l.setFixSpecViolation(true));
		assertEquals(true, r.e.isEmpty());
		assertEquals(BmsSpec.BPM_MIN, r.c.getInitialBpm(), 0.0);
	}

	// info：init_bpm：仕様違反修正ONでライブラリ上限値を下回る数値の時、#BPMが上限値に設定されること
	@Test
	public void test_Info_InitBpm_FixOverflow() throws Exception {
		var r = content(l -> l.setFixSpecViolation(true));
		assertEquals(true, r.e.isEmpty());
		assertEquals(BmsSpec.BPM_MAX, r.c.getInitialBpm(), 0.0);
	}

	// info：init_bpm：未定義の時、UNKNOWN_METAが報告され、#BPMが130に設定されること
	@Test
	public void test_Info_InitBpm_Undefined() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.UNKNOWN_META, r.e.get(0).getType());
		assertEquals(true, r.c.containsSingleMeta(BeMusicMeta.INITIAL_BPM.getName()));
		assertEquals(130.0, r.c.getInitialBpm(), 0.0);
	}

	// info：init_bpm：数値に変換できない時、WRONG_DATAが報告され、#BPMが130に設定されること
	@Test
	public void test_Info_InitBpm_WrongDefinition() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.WRONG_DATA, r.e.get(0).getType());
		assertEquals(true, r.c.containsSingleMeta(BeMusicMeta.INITIAL_BPM.getName()));
		assertEquals(130.0, r.c.getInitialBpm(), 0.0);
	}

	// info：judge_rank：指定された数値が#DEFEXRANKに設定されること
	@Test
	public void test_Info_JudgeRank_Normal() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals(120.0, BeMusicMeta.getDefExRank(r.c), 0.0);
		assertEquals(false, r.c.containsSingleMeta(BeMusicMeta.RANK.getName()));
	}

	// info：judge_rank：小数部のある数値が#DEFEXRANKに設定されること
	@Test
	public void test_Info_JudgeRank_FloatValue() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals(56.789, BeMusicMeta.getDefExRank(r.c), 0.00000001);
		assertEquals(false, r.c.containsSingleMeta(BeMusicMeta.RANK.getName()));
	}

	// info：judge_rank：負の値の時、WRONG_DATAが報告され、#DEFEXRANKが100に設定されること
	@Test
	public void test_Info_JudgeRank_MinusValue() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.WRONG_DATA, r.e.get(0).getType());
		assertEquals(true, r.c.containsSingleMeta(BeMusicMeta.DEFEXRANK.getName()));
		assertEquals(100.0, BeMusicMeta.getDefExRank(r.c), 0.0);
		assertEquals(false, r.c.containsSingleMeta(BeMusicMeta.RANK.getName()));
	}

	// info：judge_rank：未定義の時、UNKNOWN_METAが報告され、#DEFEXRANKが100に設定されること
	@Test
	public void test_Info_JudgeRank_Undefined() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.UNKNOWN_META, r.e.get(0).getType());
		assertEquals(true, r.c.containsSingleMeta(BeMusicMeta.DEFEXRANK.getName()));
		assertEquals(100.0, BeMusicMeta.getDefExRank(r.c), 0.0);
		assertEquals(false, r.c.containsSingleMeta(BeMusicMeta.RANK.getName()));
	}

	// info：judge_rank：数値に変換できない時、WRONG_DATAが報告され、#DEFEXRANKが100に設定されること
	@Test
	public void test_Info_JudgeRank_WrongDefinition() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.WRONG_DATA, r.e.get(0).getType());
		assertEquals(true, r.c.containsSingleMeta(BeMusicMeta.DEFEXRANK.getName()));
		assertEquals(100.0, BeMusicMeta.getDefExRank(r.c), 0.0);
		assertEquals(false, r.c.containsSingleMeta(BeMusicMeta.RANK.getName()));
	}

	// info：total：指定された数値が#TOTALに設定されること
	@Test
	public void test_Info_Total_Normal() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals(350.0, BeMusicMeta.getTotal(r.c), 0.0);
	}

	// info：total：小数部のある数値が#TOTALに設定されること
	@Test
	public void test_Info_Total_FloatValue() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals(642.123, BeMusicMeta.getTotal(r.c), 0.00000001);
	}

	// info：total：負の値の時、WRONG_DATAが報告され、#TOTALが100に設定されること
	@Test
	public void test_Info_Total_MinusValue() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.WRONG_DATA, r.e.get(0).getType());
		assertEquals(true, r.c.containsSingleMeta(BeMusicMeta.TOTAL.getName()));
		assertEquals(100.0, BeMusicMeta.getTotal(r.c), 0.0);
	}

	// info：total：未定義の時、UNKNOWN_METAが報告され、#TOTALが100に設定されること
	@Test
	public void test_Info_Total_Undefined() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.UNKNOWN_META, r.e.get(0).getType());
		assertEquals(true, r.c.containsSingleMeta(BeMusicMeta.TOTAL.getName()));
		assertEquals(100.0, BeMusicMeta.getTotal(r.c), 0.0);
	}

	// info：total：数値に変換できない時、WRONG_DATAが報告され、#TOTALが100に設定されること
	@Test
	public void test_Info_Total_WrongDefinition() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.WRONG_DATA, r.e.get(0).getType());
		assertEquals(true, r.c.containsSingleMeta(BeMusicMeta.TOTAL.getName()));
		assertEquals(100.0, BeMusicMeta.getTotal(r.c), 0.0);
	}

	// info：title_image：指定された文字列が#STAGEFILEに設定されること
	@Test
	public void test_Info_TitleImage_Normal() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals("jacket.png", BeMusicMeta.getStageFile(r.c));
	}

	// info：title_image：空文字列の時、#STAGEFILEが未定義になること
	@Test
	public void test_Info_TitleImage_Empty() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals(false, r.c.containsSingleMeta(BeMusicMeta.STAGEFILE.getName()));
	}

	// info：title_image：未定義の時、#STAGEFILEが未定義になること
	@Test
	public void test_Info_TitleImage_Undefined() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals(false, r.c.containsSingleMeta(BeMusicMeta.STAGEFILE.getName()));
	}

	// info：title_image：文字列に変換できない時、WRONG_DATAが報告され、#STAGEFILEが未定義になること
	@Test
	public void test_Info_TitleImage_WrongDefinition() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.WRONG_DATA, r.e.get(0).getType());
		assertEquals(false, r.c.containsSingleMeta(BeMusicMeta.STAGEFILE.getName()));
	}

	// info：back_image：指定された文字列が#BACKBMPに設定されること
	@Test
	public void test_Info_BackImage_Normal() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals("background.bmp", BeMusicMeta.getBackBmp(r.c));
	}

	// info：back_image：空文字列の時、#BACKBMPが未定義になること
	@Test
	public void test_Info_BackImage_Empty() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals(false, r.c.containsSingleMeta(BeMusicMeta.BACKBMP.getName()));
	}

	// info：back_image：未定義の時、#BACKBMPが未定義になること
	@Test
	public void test_Info_BackImage_Undefined() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals(false, r.c.containsSingleMeta(BeMusicMeta.BACKBMP.getName()));
	}

	// info：back_image：文字列に変換できない時、WRONG_DATAが報告され、#BACKBMPが未定義になること
	@Test
	public void test_Info_BackImage_WrongDefinition() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.WRONG_DATA, r.e.get(0).getType());
		assertEquals(false, r.c.containsSingleMeta(BeMusicMeta.BACKBMP.getName()));
	}

	// info：eyecatch_image：指定された文字列が#EYECATCHに設定されること
	@Test
	public void test_Info_EyecatchImage_Normal() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals("eyecatch.jpg", BeMusicMeta.getEyecatch(r.c));
	}

	// info：eyecatch_image：空文字列の時、#EYECATCHが未定義になること
	@Test
	public void test_Info_EyecatchImage_Empty() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals(false, r.c.containsSingleMeta(BeMusicMeta.EYECATCH.getName()));
	}

	// info：eyecatch_image：未定義の時、#EYECATCHが未定義になること
	@Test
	public void test_Info_EyecatchImage_Undefined() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals(false, r.c.containsSingleMeta(BeMusicMeta.EYECATCH.getName()));
	}

	// info：eyecatch_image：文字列に変換できない時、#EYECATCHが未定義になること
	@Test
	public void test_Info_EyecatchImage_WrongDefinition() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.WRONG_DATA, r.e.get(0).getType());
		assertEquals(false, r.c.containsSingleMeta(BeMusicMeta.EYECATCH.getName()));
	}

	// info：banner_image：指定された文字列が#BANNERに設定されること
	@Test
	public void test_Info_BannerImage_Normal() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals("banner.gif", BeMusicMeta.getBanner(r.c));
	}

	// info：banner_image：空文字列の時、#BANNERが未定義になること
	@Test
	public void test_Info_BannerImage_Empty() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals(false, r.c.containsSingleMeta(BeMusicMeta.BANNER.getName()));
	}

	// info：banner_image：未定義の時、#BANNERが未定義になること
	@Test
	public void test_Info_BannerImage_Undefined() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals(false, r.c.containsSingleMeta(BeMusicMeta.BANNER.getName()));
	}

	// info：banner_image：文字列に変換できない時、#BANNERが未定義になること
	@Test
	public void test_Info_BannerImage_WrongDefinition() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.WRONG_DATA, r.e.get(0).getType());
		assertEquals(false, r.c.containsSingleMeta(BeMusicMeta.BANNER.getName()));
	}

	// info：preview_music：指定された文字列が#PREVIEWに設定されること
	@Test
	public void test_Info_PreviewMusic_Normal() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals("preview.ogg", BeMusicMeta.getPreview(r.c));
	}

	// info：preview_music：空文字列の時、#PREVIEWが未定義になること
	@Test
	public void test_Info_PreviewMusic_Empty() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals(false, r.c.containsSingleMeta(BeMusicMeta.PREVIEW.getName()));
	}

	// info：preview_music：未定義の時、#PREVIEWが未定義になること
	@Test
	public void test_Info_PreviewMusic_Undefined() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals(false, r.c.containsSingleMeta(BeMusicMeta.PREVIEW.getName()));
	}

	// info：preview_music：文字列に変換できない時、#PREVIEWが未定義になること
	@Test
	public void test_Info_PreviewMusic_WrongDefinition() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.WRONG_DATA, r.e.get(0).getType());
		assertEquals(false, r.c.containsSingleMeta(BeMusicMeta.PREVIEW.getName()));
	}

	// info：ln_type：1の時、#LNMODEが1に設定されること
	@Test
	public void test_Info_LnType_Ln() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals(BeMusicLongNoteMode.LN, BeMusicMeta.getLnMode(r.c));
	}

	// info：ln_type：2の時、#LNMODEが2に設定されること
	@Test
	public void test_Info_LnType_Cn() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals(BeMusicLongNoteMode.CN, BeMusicMeta.getLnMode(r.c));
	}

	// info：ln_type：3の時、#LNMODEが3に設定されること
	@Test
	public void test_Info_LnType_Hcn() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals(BeMusicLongNoteMode.HCN, BeMusicMeta.getLnMode(r.c));
	}

	// info：ln_type：0の時、#LNMODEが未定義になること
	@Test
	public void test_Info_LnType_0() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals(false, r.c.containsSingleMeta(BeMusicMeta.LNMODE.getName()));
	}

	// info：ln_type：未定義の時、#LNMODEが未定義になること
	@Test
	public void test_Info_LnType_Undefined() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals(false, r.c.containsSingleMeta(BeMusicMeta.LNMODE.getName()));
	}

	// info：ln_type：不明な数値の時、WRONG_DATAが報告され、#LNMODEが未定義になること
	@Test
	public void test_Info_LnType_UnknownValue() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.WRONG_DATA, r.e.get(0).getType());
		assertEquals(false, r.c.containsSingleMeta(BeMusicMeta.LNMODE.getName()));
	}

	// info：ln_type：数値に変換できない時、WRONG_DATAが報告され、#LNMODEが未定義になること
	@Test
	public void test_Info_LnType_WrongDefinition() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.WRONG_DATA, r.e.get(0).getType());
		assertEquals(false, r.c.containsSingleMeta(BeMusicMeta.LNMODE.getName()));
	}

	// info：非対応項目が見つかっても無視されること
	@Test
	public void test_Info_OtherItems() throws Exception {
		// 最低限、エラーが発出されないことは確認しておく
		var r = content();
		assertEquals(true, r.e.isEmpty());
	}

	// lines：未定義：最大絶対パルス番号がbpm_events：最大絶対パルス番号を含む小節まで全て4/4拍子であること
	@Test
	public void test_Lines_Undefined_MaxIsBpmEvents() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals(4, r.c.getMeasureCount());
		assertEquals(180.0, (double)r.c.getIndexedMeta(BeMusicMeta.BPM.getName(), 1), 0.0);
		assertEquals(1, r.c.getNote(BeMusicChannel.BPM.getNumber(), 3, 0.0).getValue());
		assertEquals(false, r.c.containsMeasureValue(BeMusicChannel.LENGTH.getNumber(), 0));
		assertEquals(false, r.c.containsMeasureValue(BeMusicChannel.LENGTH.getNumber(), 1));
		assertEquals(false, r.c.containsMeasureValue(BeMusicChannel.LENGTH.getNumber(), 2));
	}

	// lines：未定義：最大絶対パルス番号がscroll_events：最大絶対パルス番号を含む小節まで全て4/4拍子であること
	@Test
	public void test_Lines_Undefined_MaxIsScrollEvents() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals(3, r.c.getMeasureCount());
		assertEquals(1.5, (double)r.c.getIndexedMeta(BeMusicMeta.SCROLL.getName(), 1), 0.0);
		assertEquals(1, r.c.getNote(BeMusicChannel.SCROLL.getNumber(), 2, 144.0).getValue());
		assertEquals(false, r.c.containsMeasureValue(BeMusicChannel.LENGTH.getNumber(), 0));
		assertEquals(false, r.c.containsMeasureValue(BeMusicChannel.LENGTH.getNumber(), 1));
		assertEquals(false, r.c.containsMeasureValue(BeMusicChannel.LENGTH.getNumber(), 2));
	}

	// lines：未定義：最大絶対パルス番号がstop_events：最大絶対パルス番号を含む小節まで全て4/4拍子であること
	@Test
	public void test_Lines_Undefined_MaxIsStopEvents() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals(3, r.c.getMeasureCount());
		assertEquals(480.0, (double)r.c.getIndexedMeta(BeMusicMeta.STOP.getName(), 1), 0.0);
		assertEquals(1, r.c.getNote(BeMusicChannel.STOP.getNumber(), 2, 96.0).getValue());
		assertEquals(false, r.c.containsMeasureValue(BeMusicChannel.LENGTH.getNumber(), 0));
		assertEquals(false, r.c.containsMeasureValue(BeMusicChannel.LENGTH.getNumber(), 1));
		assertEquals(false, r.c.containsMeasureValue(BeMusicChannel.LENGTH.getNumber(), 2));
	}

	// lines：未定義：最大絶対パルス番号がsound_channels：最大絶対パルス番号を含む小節まで全て4/4拍子であること
	@Test
	public void test_Lines_Undefined_MaxIsSoundChannels() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals(3, r.c.getMeasureCount());
		assertEquals("a.wav", r.c.getIndexedMeta(BeMusicMeta.WAV.getName(), 1));
		assertEquals(1, r.c.getNote(chVisible(BeMusicDevice.SWITCH11), 2, 48.0).getValue());
		assertEquals(false, r.c.containsMeasureValue(BeMusicChannel.LENGTH.getNumber(), 0));
		assertEquals(false, r.c.containsMeasureValue(BeMusicChannel.LENGTH.getNumber(), 1));
		assertEquals(false, r.c.containsMeasureValue(BeMusicChannel.LENGTH.getNumber(), 2));
	}

	// lines：未定義：最大絶対パルス番号がmine_channels：最大絶対パルス番号を含む小節まで全て4/4拍子であること
	@Test
	public void test_Lines_Undefined_MaxIsMineChannels() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals(3, r.c.getMeasureCount());
		assertEquals(3, r.c.getNote(chMine(BeMusicDevice.SWITCH12), 2, 0.0).getValue());
		assertEquals(false, r.c.containsMeasureValue(BeMusicChannel.LENGTH.getNumber(), 0));
		assertEquals(false, r.c.containsMeasureValue(BeMusicChannel.LENGTH.getNumber(), 1));
		assertEquals(false, r.c.containsMeasureValue(BeMusicChannel.LENGTH.getNumber(), 2));
	}

	// lines：未定義：最大絶対パルス番号がkey_channels：最大絶対パルス番号を含む小節まで全て4/4拍子であること
	@Test
	public void test_Lines_Undefined_MaxIsKeyChannels() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals(3, r.c.getMeasureCount());
		assertEquals("a.wav", r.c.getIndexedMeta(BeMusicMeta.WAV.getName(), 1));
		assertEquals(1, r.c.getNote(chInvisible(BeMusicDevice.SWITCH13), 2, 24.0).getValue());
		assertEquals(false, r.c.containsMeasureValue(BeMusicChannel.LENGTH.getNumber(), 0));
		assertEquals(false, r.c.containsMeasureValue(BeMusicChannel.LENGTH.getNumber(), 1));
		assertEquals(false, r.c.containsMeasureValue(BeMusicChannel.LENGTH.getNumber(), 2));
	}

	// lines：未定義：最大絶対パルス番号がbga_events：最大絶対パルス番号を含む小節まで全て4/4拍子であること
	@Test
	public void test_Lines_Undefined_MaxIsBgaEvents() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals(3, r.c.getMeasureCount());
		assertEquals(5, r.c.getNote(BeMusicChannel.BGA.getNumber(), 2, 72.0).getValue());
		assertEquals(false, r.c.containsMeasureValue(BeMusicChannel.LENGTH.getNumber(), 0));
		assertEquals(false, r.c.containsMeasureValue(BeMusicChannel.LENGTH.getNumber(), 1));
		assertEquals(false, r.c.containsMeasureValue(BeMusicChannel.LENGTH.getNumber(), 2));
	}

	// lines：未定義：最大絶対パルス番号がlayer_events：最大絶対パルス番号を含む小節まで全て4/4拍子であること
	@Test
	public void test_Lines_Undefined_MaxIsLayerEvents() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals(3, r.c.getMeasureCount());
		assertEquals(8, r.c.getNote(BeMusicChannel.BGA_LAYER.getNumber(), 2, 120.0).getValue());
		assertEquals(false, r.c.containsMeasureValue(BeMusicChannel.LENGTH.getNumber(), 0));
		assertEquals(false, r.c.containsMeasureValue(BeMusicChannel.LENGTH.getNumber(), 1));
		assertEquals(false, r.c.containsMeasureValue(BeMusicChannel.LENGTH.getNumber(), 2));
	}

	// lines：未定義：最大絶対パルス番号がpoor_events：最大絶対パルス番号を含む小節まで全て4/4拍子であること
	@Test
	public void test_Lines_Undefined_MaxIsPoorEvents() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals(3, r.c.getMeasureCount());
		assertEquals(10, r.c.getNote(BeMusicChannel.BGA_MISS.getNumber(), 2, 168.0).getValue());
		assertEquals(false, r.c.containsMeasureValue(BeMusicChannel.LENGTH.getNumber(), 0));
		assertEquals(false, r.c.containsMeasureValue(BeMusicChannel.LENGTH.getNumber(), 1));
		assertEquals(false, r.c.containsMeasureValue(BeMusicChannel.LENGTH.getNumber(), 2));
	}

	// lines：空配列：最大絶対パルス番号がbpm_events：最大絶対パルス番号を含む小節までを1小節とする定義になること
	@Test
	public void test_Lines_Empty_MaxIsBpmEvents() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals(1, r.c.getMeasureCount());
		assertEquals(180.0, (double)r.c.getIndexedMeta(BeMusicMeta.BPM.getName(), 1), 0.0);
		assertEquals(1, r.c.getNote(BeMusicChannel.BPM.getNumber(), 0, 576.0).getValue());
		assertEquals(4.0, (double)r.c.getMeasureValue(BeMusicChannel.LENGTH.getNumber(), 0), 0.0);
	}

	// lines：空配列：最大絶対パルス番号がscroll_events：最大絶対パルス番号を含む小節までを1小節とする定義になること
	@Test
	public void test_Lines_Empty_MaxIsScrollEvents() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals(1, r.c.getMeasureCount());
		assertEquals(1.5, (double)r.c.getIndexedMeta(BeMusicMeta.SCROLL.getName(), 1), 0.0);
		assertEquals(1, r.c.getNote(BeMusicChannel.SCROLL.getNumber(), 0, 528.0).getValue());
		assertEquals(3.0, (double)r.c.getMeasureValue(BeMusicChannel.LENGTH.getNumber(), 0), 0.0);
	}

	// lines：空配列：最大絶対パルス番号がstop_events：最大絶対パルス番号を含む小節までを1小節とする定義になること
	@Test
	public void test_Lines_Empty_MaxIsStopEvents() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals(1, r.c.getMeasureCount());
		assertEquals(480.0, (double)r.c.getIndexedMeta(BeMusicMeta.STOP.getName(), 1), 0.0);
		assertEquals(1, r.c.getNote(BeMusicChannel.STOP.getNumber(), 0, 480.0).getValue());
		assertEquals(3.0, (double)r.c.getMeasureValue(BeMusicChannel.LENGTH.getNumber(), 0), 0.0);
	}

	// lines：空配列：最大絶対パルス番号がsound_channels：最大絶対パルス番号を含む小節までを1小節とする定義になること
	@Test
	public void test_Lines_Empty_MaxIsSoundChannels() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals(1, r.c.getMeasureCount());
		assertEquals("a.wav", r.c.getIndexedMeta(BeMusicMeta.WAV.getName(), 1));
		assertEquals(1, r.c.getNote(chVisible(BeMusicDevice.SWITCH11), 0, 432.0).getValue());
		assertEquals(3.0, (double)r.c.getMeasureValue(BeMusicChannel.LENGTH.getNumber(), 0), 0.0);
	}

	// lines：空配列：最大絶対パルス番号がmine_channels：最大絶対パルス番号を含む小節までを1小節とする定義になること
	@Test
	public void test_Lines_Empty_MaxIsMineChannels() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals(1, r.c.getMeasureCount());
		assertEquals(3, r.c.getNote(chMine(BeMusicDevice.SWITCH12), 0, 384.0).getValue());
		assertEquals(3.0, (double)r.c.getMeasureValue(BeMusicChannel.LENGTH.getNumber(), 0), 0.0);
	}

	// lines：空配列：最大絶対パルス番号がkey_channels：最大絶対パルス番号を含む小節までを1小節とする定義になること
	@Test
	public void test_Lines_Empty_MaxIsKeyChannels() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals(1, r.c.getMeasureCount());
		assertEquals("a.wav", r.c.getIndexedMeta(BeMusicMeta.WAV.getName(), 1));
		assertEquals(1, r.c.getNote(chInvisible(BeMusicDevice.SWITCH13), 0, 408.0).getValue());
		assertEquals(3.0, (double)r.c.getMeasureValue(BeMusicChannel.LENGTH.getNumber(), 0), 0.0);
	}

	// lines：空配列：最大絶対パルス番号がbga_events：最大絶対パルス番号を含む小節までを1小節とする定義になること
	@Test
	public void test_Lines_Empty_MaxIsBgaEvents() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals(1, r.c.getMeasureCount());
		assertEquals(5, r.c.getNote(BeMusicChannel.BGA.getNumber(), 0, 456.0).getValue());
		assertEquals(3.0, (double)r.c.getMeasureValue(BeMusicChannel.LENGTH.getNumber(), 0), 0.0);
	}

	// lines：空配列：最大絶対パルス番号がlayer_events：最大絶対パルス番号を含む小節までを1小節とする定義になること
	@Test
	public void test_Lines_Empty_MaxIsLayerEvents() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals(1, r.c.getMeasureCount());
		assertEquals(8, r.c.getNote(BeMusicChannel.BGA_LAYER.getNumber(), 0, 504.0).getValue());
		assertEquals(3.0, (double)r.c.getMeasureValue(BeMusicChannel.LENGTH.getNumber(), 0), 0.0);
	}

	// lines：空配列：最大絶対パルス番号がpoor_events：最大絶対パルス番号を含む小節までを1小節とする定義になること
	@Test
	public void test_Lines_Empty_MaxIsPoorEvents() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals(1, r.c.getMeasureCount());
		assertEquals(10, r.c.getNote(BeMusicChannel.BGA_MISS.getNumber(), 0, 552.0).getValue());
		assertEquals(3.0, (double)r.c.getMeasureValue(BeMusicChannel.LENGTH.getNumber(), 0), 0.0);
	}

	// lines：配列でない時、SYNTAXが報告され、最大絶対パルス番号を含む小節まで全て4/4拍子であること
	@Test
	public void test_Lines_WrongDefinition() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.SYNTAX, r.e.get(0).getType());
		assertEquals(3, r.c.getMeasureCount());
		assertEquals(false, r.c.containsMeasureValue(BeMusicChannel.LENGTH.getNumber(), 0));
		assertEquals(false, r.c.containsMeasureValue(BeMusicChannel.LENGTH.getNumber(), 1));
		assertEquals(false, r.c.containsMeasureValue(BeMusicChannel.LENGTH.getNumber(), 2));
	}

	// lines：y=0のみの時、4/4拍子1小節分となること
	@Test
	public void test_Lines_OneMeasure_Y0() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals(1, r.c.getMeasureCount());
		assertEquals(false, r.c.containsMeasureValue(BeMusicChannel.LENGTH.getNumber(), 0));
		assertEquals(1, r.c.getNote(BeMusicChannel.BGA.getNumber(), 0, 0.0).getValue());
	}

	// lines：y=960のみの時、4/4拍子1小節分となること
	@Test
	public void test_Lines_OneMeasure_Y960() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals(1, r.c.getMeasureCount());
		assertEquals(false, r.c.containsMeasureValue(BeMusicChannel.LENGTH.getNumber(), 0));
		assertEquals(1, r.c.getNote(BeMusicChannel.BGA.getNumber(), 0, 0.0).getValue());
	}

	// lines：y=1440のみの時、1小節のみで小節長1.5になり、小節長変更が出力されること
	@Test
	public void test_Lines_OneMeasure_OutLength() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals(1, r.c.getMeasureCount());
		assertEquals(1.5, (double)r.c.getMeasureValue(BeMusicChannel.LENGTH.getNumber(), 0), 0.0);
		assertEquals(1, r.c.getNote(BeMusicChannel.BGA.getNumber(), 0, 0.0).getValue());
	}

	// lines：y=960,1680,2640の時、2小節目の小節長が0.75になり、小節長変更が出力されること
	@Test
	public void test_Lines_HasOutLength() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals(3, r.c.getMeasureCount());
		assertEquals(false, r.c.containsMeasureValue(BeMusicChannel.LENGTH.getNumber(), 0));
		assertEquals(0.75, (double)r.c.getMeasureValue(BeMusicChannel.LENGTH.getNumber(), 1), 0.0);
		assertEquals(false, r.c.containsMeasureValue(BeMusicChannel.LENGTH.getNumber(), 2));
		assertEquals(2, r.c.getNote(BeMusicChannel.BGA.getNumber(), 2, 96.0).getValue());
	}

	// lines：resolution=1, y=1,2,3の時、4/4拍子3小節分となること
	@Test
	public void test_Lines_Resolution_Min() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals(4, r.c.getMeasureCount());
		assertEquals(false, r.c.containsMeasureValue(BeMusicChannel.LENGTH.getNumber(), 0));
		assertEquals(false, r.c.containsMeasureValue(BeMusicChannel.LENGTH.getNumber(), 1));
		assertEquals(false, r.c.containsMeasureValue(BeMusicChannel.LENGTH.getNumber(), 2));
		assertEquals(false, r.c.containsMeasureValue(BeMusicChannel.LENGTH.getNumber(), 3));
		assertEquals(4, r.c.getNote(BeMusicChannel.BGA.getNumber(), 2, 0.0).getValue());
		assertEquals(5, r.c.getNote(BeMusicChannel.BGA.getNumber(), 3, 0.0).getValue());
	}

	// lines：yが数値に変換できない時、WRONG_DATAが報告され、当該小節の定義は無視されること
	@Test
	public void test_Lines_Y_WrongDefinition() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.WRONG_DATA, r.e.get(0).getType());
		assertEquals(2, r.c.getMeasureCount());
		assertEquals(false, r.c.containsMeasureValue(BeMusicChannel.LENGTH.getNumber(), 0));
		assertEquals(false, r.c.containsMeasureValue(BeMusicChannel.LENGTH.getNumber(), 1));
		assertEquals(false, r.c.containsMeasureValue(BeMusicChannel.LENGTH.getNumber(), 2));
		assertEquals(3, r.c.getNote(BeMusicChannel.BGA.getNumber(), 1, 96.0).getValue());
	}

	// lines：yの値が進行しなかった時(前回の絶対パルス番号と同じ値)、WRONG_DATAが報告され、後発の小節は無視されること
	@Test
	public void test_Lines_Y_NoStep() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.WRONG_DATA, r.e.get(0).getType());
		assertEquals(3, r.c.getMeasureCount());
		assertEquals(false, r.c.containsMeasureValue(BeMusicChannel.LENGTH.getNumber(), 0));
		assertEquals(false, r.c.containsMeasureValue(BeMusicChannel.LENGTH.getNumber(), 1));
		assertEquals(false, r.c.containsMeasureValue(BeMusicChannel.LENGTH.getNumber(), 2));
		assertEquals(2, r.c.getNote(BeMusicChannel.BGA.getNumber(), 2, 0.0).getValue());
	}

	// lines：yの値が後退した時(前回の絶対パルス番号より小さい値)、WRONG_DATAが報告され、後発の小節は無視されること
	@Test
	public void test_Lines_Y_StepBack() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.WRONG_DATA, r.e.get(0).getType());
		assertEquals(3, r.c.getMeasureCount());
		assertEquals(false, r.c.containsMeasureValue(BeMusicChannel.LENGTH.getNumber(), 0));
		assertEquals(false, r.c.containsMeasureValue(BeMusicChannel.LENGTH.getNumber(), 1));
		assertEquals(false, r.c.containsMeasureValue(BeMusicChannel.LENGTH.getNumber(), 2));
		assertEquals(1, r.c.getNote(BeMusicChannel.BGA.getNumber(), 2, 0.0).getValue());
	}

	// lines：yがない時、SYNTAXが報告され、当該小節の定義は無視されること
	@Test
	public void test_Lines_Y_Undefined() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.SYNTAX, r.e.get(0).getType());
		assertEquals(3, r.c.getMeasureCount());
		assertEquals(false, r.c.containsMeasureValue(BeMusicChannel.LENGTH.getNumber(), 0));
		assertEquals(false, r.c.containsMeasureValue(BeMusicChannel.LENGTH.getNumber(), 1));
		assertEquals(false, r.c.containsMeasureValue(BeMusicChannel.LENGTH.getNumber(), 2));
		assertEquals(10, r.c.getNote(BeMusicChannel.BGA.getNumber(), 2, 0.0).getValue());
	}

	// lines：yが負の値の時、WRONG_DATAが報告され、当該小節の定義は無視されること
	@Test
	public void test_Lines_Y_MinusValue() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.WRONG_DATA, r.e.get(0).getType());
		assertEquals(3, r.c.getMeasureCount());
		assertEquals(false, r.c.containsMeasureValue(BeMusicChannel.LENGTH.getNumber(), 0));
		assertEquals(false, r.c.containsMeasureValue(BeMusicChannel.LENGTH.getNumber(), 1));
		assertEquals(false, r.c.containsMeasureValue(BeMusicChannel.LENGTH.getNumber(), 2));
		assertEquals(9, r.c.getNote(BeMusicChannel.BGA.getNumber(), 2, 0.0).getValue());
	}

	// lines：小節長がライブラリ下限値を下回る値の時、SPEC_VIOLATIONが報告され、当該小節の定義は無視されること
	@Test
	public void test_Lines_Length_Underflow() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.SPEC_VIOLATION, r.e.get(0).getType());
		assertEquals(4, r.c.getMeasureCount());
		assertEquals(false, r.c.containsMeasureValue(BeMusicChannel.LENGTH.getNumber(), 0));  // 仕様違反で無効になるため未定義となる
		assertEquals(true, r.c.containsMeasureValue(BeMusicChannel.LENGTH.getNumber(), 1));
		assertEquals(false, r.c.containsMeasureValue(BeMusicChannel.LENGTH.getNumber(), 2));
		assertEquals(false, r.c.containsMeasureValue(BeMusicChannel.LENGTH.getNumber(), 3));
		assertEquals(8, r.c.getNote(BeMusicChannel.BGA.getNumber(), 3, 0.0).getValue());
	}

	// lines：小節長がライブラリ上限値を上回る値の時、SPEC_VIOLATIONが報告され、当該小節の定義は無視されること
	@Test
	public void test_Lines_Length_Overflow() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.SPEC_VIOLATION, r.e.get(0).getType());
		assertEquals(4, r.c.getMeasureCount());
		assertEquals(false, r.c.containsMeasureValue(BeMusicChannel.LENGTH.getNumber(), 0));  // 仕様違反で無効になるため未定義となる
		assertEquals(false, r.c.containsMeasureValue(BeMusicChannel.LENGTH.getNumber(), 1));
		assertEquals(false, r.c.containsMeasureValue(BeMusicChannel.LENGTH.getNumber(), 2));
		assertEquals(false, r.c.containsMeasureValue(BeMusicChannel.LENGTH.getNumber(), 3));
		assertEquals(7, r.c.getNote(BeMusicChannel.BGA.getNumber(), 3, 0.0).getValue());
	}

	// lines：仕様違反修正ONで小節長がライブラリ下限値を下回る値の時、当該小節の小節長は下限値に設定されること
	@Test
	public void test_Lines_Length_FixUnderflow() throws Exception {
		var r = content(ld -> ld.setFixSpecViolation(true));
		assertEquals(true, r.e.isEmpty());
		assertEquals(4, r.c.getMeasureCount());
		assertEquals(BmsSpec.LENGTH_MIN, (double)r.c.getMeasureValue(BeMusicChannel.LENGTH.getNumber(), 0), 0.0);
		assertEquals(true, r.c.containsMeasureValue(BeMusicChannel.LENGTH.getNumber(), 1));
		assertEquals(false, r.c.containsMeasureValue(BeMusicChannel.LENGTH.getNumber(), 2));
		assertEquals(false, r.c.containsMeasureValue(BeMusicChannel.LENGTH.getNumber(), 3));
		assertEquals(8, r.c.getNote(BeMusicChannel.BGA.getNumber(), 3, 0.0).getValue());
	}

	// lines：仕様違反修正ONで小節長がライブラリ上限値を上回る値の時、当該小節の小節長は上限値に設定されること
	@Test
	public void test_Lines_Length_FixOverflow() throws Exception {
		var r = content(ld -> ld.setFixSpecViolation(true));
		assertEquals(true, r.e.isEmpty());
		assertEquals(4, r.c.getMeasureCount());
		assertEquals(BmsSpec.LENGTH_MAX, (double)r.c.getMeasureValue(BeMusicChannel.LENGTH.getNumber(), 0), 0.0);
		assertEquals(false, r.c.containsMeasureValue(BeMusicChannel.LENGTH.getNumber(), 1));
		assertEquals(false, r.c.containsMeasureValue(BeMusicChannel.LENGTH.getNumber(), 2));
		assertEquals(false, r.c.containsMeasureValue(BeMusicChannel.LENGTH.getNumber(), 3));
		assertEquals(7, r.c.getNote(BeMusicChannel.BGA.getNumber(), 3, 0.0).getValue());
	}

	// bpm_events：未定義の時、何も行われないこと
	@Test
	public void test_BpmEvents_Undefined() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals(0, r.c.getIndexedMetaCount(BeMusicMeta.BPM.getName()));
	}

	// bpm_events：配列でない時、SYNTAXが報告されること
	@Test
	public void test_BpmEvents_WrongDefinition() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.SYNTAX, r.e.get(0).getType());
		assertEquals(0, r.c.getIndexedMetaCount(BeMusicMeta.BPM.getName()));
	}

	// bpm_events：空配列の時、何も行われないこと
	@Test
	public void test_BpmEvents_Empty() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals(0, r.c.getIndexedMetaCount(BeMusicMeta.BPM.getName()));
	}

	// bpm_events：タイムライン解析省略時、メタ情報が出力され、チャンネルは出力されないこと
	@Test
	public void test_BpmEvents_SkipTimeline() throws Exception {
		var r = content(ld -> ld.setSkipReadTimeline(true));
		assertEquals(true, r.e.isEmpty());
		assertEquals(150.0, (double)r.c.getIndexedMeta(BeMusicMeta.BPM.getName(), 1), 0.0);
		assertNull(r.c.getNote(BeMusicChannel.BPM.getNumber(), 0, 0.0));
	}

	// bpm_events：yの位置にbpmの値でBPM変更が出力されること。#BPMxxと#xxx08:が正しく出力されること
	@Test
	public void test_BpmEvents_NormalCases() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals(5, r.c.getIndexedMetaCount(BeMusicMeta.BPM.getName()));
		assertEquals(150.0, (double)r.c.getIndexedMeta(BeMusicMeta.BPM.getName(), 1), 0.0);
		assertEquals(180.0, (double)r.c.getIndexedMeta(BeMusicMeta.BPM.getName(), 2), 0.0);
		assertEquals(150.1, (double)r.c.getIndexedMeta(BeMusicMeta.BPM.getName(), 3), 0.0);
		assertEquals(300.0, (double)r.c.getIndexedMeta(BeMusicMeta.BPM.getName(), 4), 0.0);
		assertEquals(100.0, (double)r.c.getIndexedMeta(BeMusicMeta.BPM.getName(), 5), 0.0);
		assertEquals(1, r.c.getNote(BeMusicChannel.BPM.getNumber(), 0, 0.0).getValue());
		assertEquals(2, r.c.getNote(BeMusicChannel.BPM.getNumber(), 0, 96.0).getValue());
		assertEquals(1, r.c.getNote(BeMusicChannel.BPM.getNumber(), 2, 96.0).getValue());
		assertEquals(3, r.c.getNote(BeMusicChannel.BPM.getNumber(), 3, 0.0).getValue());
		assertEquals(4, r.c.getNote(BeMusicChannel.BPM.getNumber(), 3, 144.0).getValue());
		assertEquals(5, r.c.getNote(BeMusicChannel.BPM.getNumber(), 5, 0.0).getValue());
	}

	// bpm_events：yが同じ位置の定義がある時、後発の定義が優先されること
	@Test
	public void test_BpmEvents_Y_HasSame() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals(2, r.c.getIndexedMetaCount(BeMusicMeta.BPM.getName()));
		assertEquals(150.0, (double)r.c.getIndexedMeta(BeMusicMeta.BPM.getName(), 1), 0.0);
		assertEquals(180.0, (double)r.c.getIndexedMeta(BeMusicMeta.BPM.getName(), 2), 0.0);
		assertEquals(2, r.c.getNote(BeMusicChannel.BPM.getNumber(), 0, 96.0).getValue());
	}

	// bpm_events：yがない時、SYNTAXが報告され、当該定義は無視されること
	@Test
	public void test_BpmEvents_Y_Undefined() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.SYNTAX, r.e.get(0).getType());
		assertEquals(0, r.c.getIndexedMetaCount(BeMusicMeta.BPM.getName()));
	}

	// bpm_events：yが負の値の時、WRONG_DATAが報告され、当該定義は無視されること
	@Test
	public void test_BpmEvents_Y_MinusValue() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.WRONG_DATA, r.e.get(0).getType());
		assertEquals(0, r.c.getIndexedMetaCount(BeMusicMeta.BPM.getName()));
	}

	// bpm_events：yが数値に変換できない時、WRONG_DATAが報告され、当該定義は無視されること
	@Test
	public void test_BpmEvents_Y_WrongDefinition() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.WRONG_DATA, r.e.get(0).getType());
		assertEquals(0, r.c.getIndexedMetaCount(BeMusicMeta.BPM.getName()));
	}

	// bpm_events：bpmがない時、SYNTAXが報告され、当該定義は無視されること
	@Test
	public void test_BpmEvents_Bpm_Undefined() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.SYNTAX, r.e.get(0).getType());
		assertEquals(0, r.c.getIndexedMetaCount(BeMusicMeta.BPM.getName()));
		assertNull(r.c.getNote(BeMusicChannel.BPM.getNumber(), 0, 96.0));
	}

	// bpm_events：bpmが負の値の時、WRONG_DATAが報告され、当該定義は無視されること
	@Test
	public void test_BpmEvents_Bpm_MinusValue() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.WRONG_DATA, r.e.get(0).getType());
		assertEquals(0, r.c.getIndexedMetaCount(BeMusicMeta.BPM.getName()));
		assertNull(r.c.getNote(BeMusicChannel.BPM.getNumber(), 0, 96.0));
	}

	// bpm_events：bpmが数値に変換できない時、WRONG_DATAが報告され、当該定義は無視されること
	@Test
	public void test_BpmEvents_Bpm_WrongDefinition() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.WRONG_DATA, r.e.get(0).getType());
		assertEquals(0, r.c.getIndexedMetaCount(BeMusicMeta.BPM.getName()));
		assertNull(r.c.getNote(BeMusicChannel.BPM.getNumber(), 0, 96.0));
	}

	// bpm_events：bpmがライブラリ下限値を下回る値の時、SPEC_VIOLATIONが報告され、当該定義は無視されること
	// ※標準フォーマットの挙動と合わせるため、メタ情報は未定義、ノートは出力される動作を正とする
	@Test
	public void test_BpmEvents_Bpm_Underflow() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.SPEC_VIOLATION, r.e.get(0).getType());
		assertEquals(0, r.c.getIndexedMetaCount(BeMusicMeta.BPM.getName()));
		assertEquals(1, r.c.getNote(BeMusicChannel.BPM.getNumber(), 0, 96.0).getValue());  // ノートは出力される
	}

	// bpm_events：bpmがライブラリ上限値を上回る値の時、SPEC_VIOLATIONが報告され、当該定義は無視されること
	// ※標準フォーマットの挙動と合わせるため、メタ情報は未定義、ノートは出力される動作を正とする
	@Test
	public void test_BpmEvents_Bpm_Overflow() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.SPEC_VIOLATION, r.e.get(0).getType());
		assertEquals(0, r.c.getIndexedMetaCount(BeMusicMeta.BPM.getName()));
		assertEquals(1, r.c.getNote(BeMusicChannel.BPM.getNumber(), 0, 96.0).getValue());  // ノートは出力される
	}

	// bpm_events：仕様違反修正ONでbpmがライブラリ下限値を下回る値の時、当該定義は下限値に設定されること
	@Test
	public void test_BpmEvents_Bpm_FixUnderflow() throws Exception {
		var r = content(ld -> ld.setFixSpecViolation(true));
		assertEquals(true, r.e.isEmpty());
		assertEquals(1, r.c.getIndexedMetaCount(BeMusicMeta.BPM.getName()));
		assertEquals((double)BmsSpec.BPM_MIN, (double)r.c.getIndexedMeta(BeMusicMeta.BPM.getName(), 1), 0.0);
		assertEquals(1, r.c.getNote(BeMusicChannel.BPM.getNumber(), 0, 96.0).getValue());
	}

	// bpm_events：仕様違反修正ONでbpmがライブラリ上限値を上回る値の時、当該定義は上限値に設定されること
	@Test
	public void test_BpmEvents_Bpm_FixOverflow() throws Exception {
		var r = content(ld -> ld.setFixSpecViolation(true));
		assertEquals(true, r.e.isEmpty());
		assertEquals(1, r.c.getIndexedMetaCount(BeMusicMeta.BPM.getName()));
		assertEquals((double)BmsSpec.BPM_MAX, (double)r.c.getIndexedMeta(BeMusicMeta.BPM.getName(), 1), 0.0);
		assertEquals(1, r.c.getNote(BeMusicChannel.BPM.getNumber(), 0, 96.0).getValue());
	}

	// bpm_events：bpmの種類数がメタ情報インデックス値上限を超える時、WRONG_DATAが報告され、当該bpmとそれを使用する定義は無視されること
	@Test
	public void test_BpmEvents_Bpm_TooMany() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.WRONG_DATA, r.e.get(0).getType());
		assertEquals(BmsSpec.INDEXED_META_INDEX_MAX, r.c.getIndexedMetaCount(BeMusicMeta.BPM.getName()));
		assertNull(r.c.getNote(BeMusicChannel.BPM.getNumber(), 1, 0.0));  // 左記ノートで上限超過するデータになっている
	}

	// bpm_events：ノート定義がJSONオブジェクト以外の時、SYNTAXが報告されること
	@Test
	public void test_BpmEvents_Note_WrongDefinition() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.SYNTAX, r.e.get(0).getType());
		assertEquals(0, r.c.getIndexedMetaCount(BeMusicMeta.BPM.getName()));
	}

	// scroll_events：未定義の時、何も行われないこと
	@Test
	public void test_ScrollEvents_Undefined() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals(0, r.c.getIndexedMetaCount(BeMusicMeta.SCROLL.getName()));
	}

	// scroll_events：配列でない時、SYNTAXが報告されること
	@Test
	public void test_ScrollEvents_WrongDefinition() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.SYNTAX, r.e.get(0).getType());
		assertEquals(0, r.c.getIndexedMetaCount(BeMusicMeta.SCROLL.getName()));
	}

	// scroll_events：空配列の時、何も行われないこと
	@Test
	public void test_ScrollEvents_Empty() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals(0, r.c.getIndexedMetaCount(BeMusicMeta.SCROLL.getName()));
	}

	// scroll_events：タイムライン解析省略時、メタ情報が出力され、チャンネルは出力されないこと
	@Test
	public void test_ScrollEvents_SkipTimeline() throws Exception {
		var r = content(ld -> ld.setSkipReadTimeline(true));
		assertEquals(true, r.e.isEmpty());
		assertEquals(1.5, (double)r.c.getIndexedMeta(BeMusicMeta.SCROLL.getName(), 1), 0.0);
		assertNull(r.c.getNote(BeMusicChannel.SCROLL.getNumber(), 0, 0.0));
	}

	// scroll_events：yの位置にrateの値でスクロール速度変更が出力されること。#SCROLLxxと#xxxSC:が正しく出力されること
	@Test
	public void test_ScrollEvents_NormalCases() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals(5, r.c.getIndexedMetaCount(BeMusicMeta.SCROLL.getName()));
		assertEquals(1.5, (double)r.c.getIndexedMeta(BeMusicMeta.SCROLL.getName(), 1), 0.0);
		assertEquals(1.8, (double)r.c.getIndexedMeta(BeMusicMeta.SCROLL.getName(), 2), 0.0);
		assertEquals(1.501, (double)r.c.getIndexedMeta(BeMusicMeta.SCROLL.getName(), 3), 0.0);
		assertEquals(3.0, (double)r.c.getIndexedMeta(BeMusicMeta.SCROLL.getName(), 4), 0.0);
		assertEquals(1.0, (double)r.c.getIndexedMeta(BeMusicMeta.SCROLL.getName(), 5), 0.0);
		assertEquals(1, r.c.getNote(BeMusicChannel.SCROLL.getNumber(), 0, 0.0).getValue());
		assertEquals(2, r.c.getNote(BeMusicChannel.SCROLL.getNumber(), 0, 96.0).getValue());
		assertEquals(1, r.c.getNote(BeMusicChannel.SCROLL.getNumber(), 2, 96.0).getValue());
		assertEquals(3, r.c.getNote(BeMusicChannel.SCROLL.getNumber(), 3, 0.0).getValue());
		assertEquals(4, r.c.getNote(BeMusicChannel.SCROLL.getNumber(), 3, 144.0).getValue());
		assertEquals(5, r.c.getNote(BeMusicChannel.SCROLL.getNumber(), 5, 0.0).getValue());
	}

	// scroll_events：yが同じ位置の定義がある時、後発の定義が優先されること
	@Test
	public void test_ScrollEvents_Y_HasSame() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals(2, r.c.getIndexedMetaCount(BeMusicMeta.SCROLL.getName()));
		assertEquals(1.5, (double)r.c.getIndexedMeta(BeMusicMeta.SCROLL.getName(), 1), 0.0);
		assertEquals(1.8, (double)r.c.getIndexedMeta(BeMusicMeta.SCROLL.getName(), 2), 0.0);
		assertEquals(2, r.c.getNote(BeMusicChannel.SCROLL.getNumber(), 0, 96.0).getValue());
	}

	// scroll_events：yがない時、SYNTAXが報告され、当該定義は無視されること
	@Test
	public void test_ScrollEvents_Y_Undefined() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.SYNTAX, r.e.get(0).getType());
		assertEquals(0, r.c.getIndexedMetaCount(BeMusicMeta.SCROLL.getName()));
	}

	// scroll_events：yが負の値の時、WRONG_DATAが報告され、当該定義は無視されること
	@Test
	public void test_ScrollEvents_Y_MinusValue() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.WRONG_DATA, r.e.get(0).getType());
		assertEquals(0, r.c.getIndexedMetaCount(BeMusicMeta.SCROLL.getName()));
	}

	// scroll_events：yが数値に変換できない時、WRONG_DATAが報告され、当該定義は無視されること
	@Test
	public void test_ScrollEvents_Y_WrongDefinition() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.WRONG_DATA, r.e.get(0).getType());
		assertEquals(0, r.c.getIndexedMetaCount(BeMusicMeta.SCROLL.getName()));
	}

	// scroll_events：rateがない時、SYNTAXが報告され、当該定義は無視されること
	@Test
	public void test_ScrollEvents_Rate_Undefined() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.SYNTAX, r.e.get(0).getType());
		assertEquals(0, r.c.getIndexedMetaCount(BeMusicMeta.SCROLL.getName()));
		assertNull(r.c.getNote(BeMusicChannel.SCROLL.getNumber(), 0, 96.0));
	}

	// scroll_events：rateが数値に変換できない時、WRONG_DATAが報告され、当該定義は無視されること
	@Test
	public void test_ScrollEvents_Rate_WrongDefinition() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.WRONG_DATA, r.e.get(0).getType());
		assertEquals(0, r.c.getIndexedMetaCount(BeMusicMeta.SCROLL.getName()));
		assertNull(r.c.getNote(BeMusicChannel.SCROLL.getNumber(), 0, 96.0));
	}

	// scroll_events：rateの種類数がメタ情報インデックス値上限を超える時、WRONG_DATAが報告され、当該rateとそれを使用する定義は無視されること
	@Test
	public void test_ScrollEvents_Rate_TooMany() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.WRONG_DATA, r.e.get(0).getType());
		assertEquals(BmsSpec.INDEXED_META_INDEX_MAX, r.c.getIndexedMetaCount(BeMusicMeta.SCROLL.getName()));
		assertNull(r.c.getNote(BeMusicChannel.SCROLL.getNumber(), 1, 0.0));  // 左記ノートで上限超過するデータになっている
	}

	// scroll_events：ノート定義がJSONオブジェクト以外の時、SYNTAXが報告されること
	@Test
	public void test_ScrollEvents_Note_WrongDefinition() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.SYNTAX, r.e.get(0).getType());
		assertEquals(0, r.c.getIndexedMetaCount(BeMusicMeta.SCROLL.getName()));
	}

	// stop_events：未定義の時、何も行われないこと
	@Test
	public void test_StopEvents_Undefined() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals(0, r.c.getIndexedMetaCount(BeMusicMeta.STOP.getName()));
	}

	// stop_events：配列でない時、SYNTAXが報告されること
	@Test
	public void test_StopEvents_WrongDefinition() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.SYNTAX, r.e.get(0).getType());
		assertEquals(0, r.c.getIndexedMetaCount(BeMusicMeta.STOP.getName()));
	}

	// stop_events：空配列の時、何も行われないこと
	@Test
	public void test_StopEvents_Empty() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals(0, r.c.getIndexedMetaCount(BeMusicMeta.STOP.getName()));
	}

	// stop_events：タイムライン解析省略時、メタ情報が出力され、チャンネルは出力されないこと
	@Test
	public void test_StopEvents_SkipTimeline() throws Exception {
		var r = content(ld -> ld.setSkipReadTimeline(true));
		assertEquals(true, r.e.isEmpty());
		assertEquals(240.0, (double)r.c.getIndexedMeta(BeMusicMeta.STOP.getName(), 1), 0.0);
		assertNull(r.c.getNote(BeMusicChannel.STOP.getNumber(), 0, 0.0));
	}

	// stop_events：yの位置にdurationの値で譜面停止が出力されること。#STOPxxと#xxx09:が正しく出力されること
	@Test
	public void test_StopEvents_NormalCases() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals(5, r.c.getIndexedMetaCount(BeMusicMeta.STOP.getName()));
		assertEquals(240.0, (double)r.c.getIndexedMeta(BeMusicMeta.STOP.getName(), 1), 0.0);
		assertEquals(360.0, (double)r.c.getIndexedMeta(BeMusicMeta.STOP.getName(), 2), 0.0);
		assertEquals(241.0, (double)r.c.getIndexedMeta(BeMusicMeta.STOP.getName(), 3), 0.0);
		assertEquals(720.0, (double)r.c.getIndexedMeta(BeMusicMeta.STOP.getName(), 4), 0.0);
		assertEquals(480.0, (double)r.c.getIndexedMeta(BeMusicMeta.STOP.getName(), 5), 0.0);
		assertEquals(1, r.c.getNote(BeMusicChannel.STOP.getNumber(), 0, 0.0).getValue());
		assertEquals(2, r.c.getNote(BeMusicChannel.STOP.getNumber(), 0, 96.0).getValue());
		assertEquals(1, r.c.getNote(BeMusicChannel.STOP.getNumber(), 2, 96.0).getValue());
		assertEquals(3, r.c.getNote(BeMusicChannel.STOP.getNumber(), 3, 0.0).getValue());
		assertEquals(4, r.c.getNote(BeMusicChannel.STOP.getNumber(), 3, 144.0).getValue());
		assertEquals(5, r.c.getNote(BeMusicChannel.STOP.getNumber(), 5, 0.0).getValue());
	}

	// stop_events：yが同じ位置の定義がある時、後発の定義が優先されること　※bmson標準仕様では停止時間の加算だが、準拠しないこととする
	@Test
	public void test_StopEvents_Y_HasSame() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals(2, r.c.getIndexedMetaCount(BeMusicMeta.STOP.getName()));
		assertEquals(240.0, (double)r.c.getIndexedMeta(BeMusicMeta.STOP.getName(), 1), 0.0);
		assertEquals(360.0, (double)r.c.getIndexedMeta(BeMusicMeta.STOP.getName(), 2), 0.0);
		assertEquals(2, r.c.getNote(BeMusicChannel.STOP.getNumber(), 0, 96.0).getValue());
	}

	// stop_events：yがない時、SYNTAXが報告され、当該定義は無視されること
	@Test
	public void test_StopEvents_Y_Undefined() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.SYNTAX, r.e.get(0).getType());
		assertEquals(0, r.c.getIndexedMetaCount(BeMusicMeta.STOP.getName()));
	}

	// stop_events：yが負の値の時、WRONG_DATAが報告され、当該定義は無視されること
	@Test
	public void test_StopEvents_Y_MinusValue() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.WRONG_DATA, r.e.get(0).getType());
		assertEquals(0, r.c.getIndexedMetaCount(BeMusicMeta.STOP.getName()));
	}

	// stop_events：yが数値に変換できない時、WRONG_DATAが報告され、当該定義は無視されること
	@Test
	public void test_StopEvents_Y_WrongDefinition() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.WRONG_DATA, r.e.get(0).getType());
		assertEquals(0, r.c.getIndexedMetaCount(BeMusicMeta.STOP.getName()));
	}

	// stop_events：durationがない時、SYNTAXが報告され、当該定義は無視されること
	@Test
	public void test_StopEvents_Duration_Undefined() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.SYNTAX, r.e.get(0).getType());
		assertEquals(0, r.c.getIndexedMetaCount(BeMusicMeta.STOP.getName()));
		assertNull(r.c.getNote(BeMusicChannel.STOP.getNumber(), 0, 96.0));
	}

	// stop_events：durationが負の値の時、WRONG_DATAが報告され、当該定義は無視されること
	@Test
	public void test_StopEvents_Duration_MinusValue() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.WRONG_DATA, r.e.get(0).getType());
		assertEquals(0, r.c.getIndexedMetaCount(BeMusicMeta.STOP.getName()));
		assertNull(r.c.getNote(BeMusicChannel.STOP.getNumber(), 0, 96.0));
	}

	// stop_events：durationが数値に変換できない時、WRONG_DATAが報告され、当該定義は無視されること
	@Test
	public void test_StopEvents_Duration_WrongDefinition() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.WRONG_DATA, r.e.get(0).getType());
		assertEquals(0, r.c.getIndexedMetaCount(BeMusicMeta.STOP.getName()));
		assertNull(r.c.getNote(BeMusicChannel.STOP.getNumber(), 0, 96.0));
	}

	// stop_events：durationがライブラリ下限値を下回る値の時、SPEC_VIOLATIONが報告され、当該定義は無視されること　※テスト不可
	@Test
	public void test_StopEvents_Duration_Underflow() throws Exception {
		// 停止時間の下限値が0で、且つ負の値はWRONG_DATAになるためテスト不可
	}

	// stop_events：durationがライブラリ上限値を上回る値の時、SPEC_VIOLATIONが報告され、当該定義は無視されること
	@Test
	public void test_StopEvents_Duration_Overflow() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.SPEC_VIOLATION, r.e.get(0).getType());
		assertEquals(0, r.c.getIndexedMetaCount(BeMusicMeta.STOP.getName()));
		assertEquals(1, r.c.getNote(BeMusicChannel.STOP.getNumber(), 0, 96.0).getValue());  // ノートは出力される
	}

	// stop_events：仕様違反修正ONでdurationがライブラリ下限値を下回る値の時、当該定義は下限値に設定されること　※テスト不可
	@Test
	public void test_StopEvents_Duration_FixUnderflow() throws Exception {
		// 停止時間の下限値が0で、且つ負の値はWRONG_DATAになるためテスト不可
	}

	// stop_events：仕様違反修正ONでdurationがライブラリ上限値を上回る値の時、当該定義は上限値に設定されること
	@Test
	public void test_StopEvents_Duration_FixOverflow() throws Exception {
		var r = content(ld -> ld.setFixSpecViolation(true));
		assertEquals(true, r.e.isEmpty());
		assertEquals(1, r.c.getIndexedMetaCount(BeMusicMeta.STOP.getName()));
		assertEquals((double)BmsSpec.STOP_MAX, (double)r.c.getIndexedMeta(BeMusicMeta.STOP.getName(), 1), 0.0);
		assertEquals(1, r.c.getNote(BeMusicChannel.STOP.getNumber(), 0, 96.0).getValue());
	}

	// stop_events：durationの種類数がメタ情報インデックス値上限を超える時、WRONG_DATAが報告され、当該durationとそれを使用する定義は無視されること
	@Test
	public void test_StopEvents_Duration_TooMany() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.WRONG_DATA, r.e.get(0).getType());
		assertEquals(BmsSpec.INDEXED_META_INDEX_MAX, r.c.getIndexedMetaCount(BeMusicMeta.STOP.getName()));
		assertNull(r.c.getNote(BeMusicChannel.STOP.getNumber(), 1, 0.0));  // 左記ノートで上限超過するデータになっている
	}

	// stop_events：ノート定義がJSONオブジェクト以外の時、SYNTAXが報告されること
	@Test
	public void test_StopEvents_Note_WrongDefinition() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.SYNTAX, r.e.get(0).getType());
		assertEquals(0, r.c.getIndexedMetaCount(BeMusicMeta.STOP.getName()));
	}

	// sound_channels：未定義の時、SYNTAXが報告されること
	@Test
	public void test_SoundChannels_Undefined() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.SYNTAX, r.e.get(0).getType());
		assertEquals(0, r.c.getIndexedMetaCount(BeMusicMeta.WAV.getName()));
	}

	// sound_channels：配列でない時、SYNTAXが報告されること
	@Test
	public void test_SoundChannels_WrongDefinition() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.SYNTAX, r.e.get(0).getType());
		assertEquals(0, r.c.getIndexedMetaCount(BeMusicMeta.WAV.getName()));
	}

	// sound_channels：空配列の時、何も行われないこと
	@Test
	public void test_SoundChannels_Empty() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals(0, r.c.getIndexedMetaCount(BeMusicMeta.WAV.getName()));
	}

	// sound_channels：タイムライン解析省略時、メタ情報が出力され、チャンネルは出力されないこと
	@Test
	public void test_SoundChannels_SkipTimeline() throws Exception {
		var r = content(ld -> ld.setSkipReadTimeline(true));
		assertEquals(true, r.e.isEmpty());
		assertEquals(1, r.c.getIndexedMetaCount(BeMusicMeta.WAV.getName()));
		assertEquals("a.wav", r.c.getIndexedMeta(BeMusicMeta.WAV.getName(), 1));
		assertNull(r.c.getNote(chVisible(BeMusicDevice.SWITCH11), 0, 96.0));
	}

	// sound_channels：配列要素インデックス+1がトラックIDとなること。#WAVxxと#xxx1y-1z,2y-2z,5y-5z,6y-6zが正しく出力されること（正常系）
	@Test
	public void test_SoundChannels_NormalCases() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals(6, r.c.getIndexedMetaCount(BeMusicMeta.WAV.getName()));
		assertEquals("1.wav", r.c.getIndexedMeta(BeMusicMeta.WAV.getName(), 1));
		assertEquals("2.wav", r.c.getIndexedMeta(BeMusicMeta.WAV.getName(), 2));
		assertEquals("3.wav", r.c.getIndexedMeta(BeMusicMeta.WAV.getName(), 3));
		assertEquals("4.wav", r.c.getIndexedMeta(BeMusicMeta.WAV.getName(), 4));
		assertEquals("5.wav", r.c.getIndexedMeta(BeMusicMeta.WAV.getName(), 5));
		assertEquals("6.wav", r.c.getIndexedMeta(BeMusicMeta.WAV.getName(), 6));
		assertEquals(1, r.c.getNote(chVisible(BeMusicDevice.SWITCH11), 0, 0.0).getValue());
		assertEquals(4, r.c.getNote(chVisible(BeMusicDevice.SCRATCH2), 0, 0.0).getValue());
		assertEquals(5, r.c.getNote(BeMusicChannel.BGM.getNumber(), 0, 0, 0.0).getValue());
		assertEquals(1, r.c.getNote(chVisible(BeMusicDevice.SWITCH11), 0, 48.0).getValue());
		assertEquals(2, r.c.getNote(chVisible(BeMusicDevice.SWITCH16), 0, 48.0).getValue());
		assertEquals(3, r.c.getNote(chVisible(BeMusicDevice.SWITCH21), 0, 48.0).getValue());
		assertEquals(6, r.c.getNote(BeMusicChannel.BGM.getNumber(), 0, 0, 48.0).getValue());
		assertEquals(2, r.c.getNote(chVisible(BeMusicDevice.SWITCH17), 0, 60.0).getValue());
		assertEquals(2, r.c.getNote(chVisible(BeMusicDevice.SWITCH16), 0, 72.0).getValue());
		assertEquals(1, r.c.getNote(chVisible(BeMusicDevice.SWITCH11), 0, 96.0).getValue());
		assertEquals(2, r.c.getNote(chVisible(BeMusicDevice.SWITCH14), 0, 96.0).getValue());
		assertEquals(3, r.c.getNote(chVisible(BeMusicDevice.SWITCH23), 0, 96.0).getValue());
		assertEquals(5, r.c.getNote(BeMusicChannel.BGM.getNumber(), 0, 0, 96.0).getValue());
		assertEquals(1, r.c.getNote(chVisible(BeMusicDevice.SWITCH11), 0, 144.0).getValue());
		assertEquals(2, r.c.getNote(chVisible(BeMusicDevice.SWITCH12), 0, 144.0).getValue());
		assertEquals(3, r.c.getNote(chVisible(BeMusicDevice.SWITCH25), 0, 144.0).getValue());
		assertEquals(4, r.c.getNote(chVisible(BeMusicDevice.SCRATCH1), 1, 0.0).getValue());
		assertEquals(3, r.c.getNote(chVisible(BeMusicDevice.SWITCH11), 1, 0.0).getValue());
		assertEquals(5, r.c.getNote(BeMusicChannel.BGM.getNumber(), 0, 1, 0.0).getValue());
		assertEquals(6, r.c.getNote(BeMusicChannel.BGM.getNumber(), 1, 1, 0.0).getValue());
		assertEquals(2, r.c.getNote(chLong(BeMusicDevice.SWITCH24), 1, 0.0).getValue());
		assertEquals(1, r.c.getNote(chVisible(BeMusicDevice.SWITCH27), 1, 0.0).getValue());
		assertEquals(3, r.c.getNote(chVisible(BeMusicDevice.SWITCH15), 1, 24.0).getValue());
		assertEquals(3, r.c.getNote(chVisible(BeMusicDevice.SWITCH12), 1, 48.0).getValue());
		assertEquals(2, r.c.getNote(chLong(BeMusicDevice.SWITCH22), 1, 48.0).getValue());
		assertEquals(2, r.c.getNote(chLong(BeMusicDevice.SWITCH24), 1, 48.0).getValue());
		assertEquals(1, r.c.getNote(chVisible(BeMusicDevice.SWITCH27), 1, 48.0).getValue());
		assertEquals(3, r.c.getNote(chVisible(BeMusicDevice.SWITCH16), 1, 72.0).getValue());
		assertEquals(3, r.c.getNote(chLong(BeMusicDevice.SWITCH13), 1, 96.0).getValue());
		assertEquals(2, r.c.getNote(chLong(BeMusicDevice.SWITCH22), 1, 96.0).getValue());
		assertEquals(2, r.c.getNote(chLong(BeMusicDevice.SWITCH24), 1, 96.0).getValue());
		assertEquals(1, r.c.getNote(chVisible(BeMusicDevice.SWITCH27), 1, 96.0).getValue());
		assertEquals(2, r.c.getNote(chLong(BeMusicDevice.SWITCH22), 1, 144.0).getValue());
		assertEquals(2, r.c.getNote(chLong(BeMusicDevice.SWITCH24), 1, 144.0).getValue());
		assertEquals(1, r.c.getNote(chVisible(BeMusicDevice.SWITCH27), 1, 144.0).getValue());
		assertEquals(4, r.c.getNote(chVisible(BeMusicDevice.SCRATCH1), 2, 0.0).getValue());
		assertEquals(3, r.c.getNote(chLong(BeMusicDevice.SWITCH13), 2, 0.0).getValue());
		assertEquals(2, r.c.getNote(chLong(BeMusicDevice.SWITCH22), 2, 0.0).getValue());
		assertEquals(4, r.c.getNote(chVisible(BeMusicDevice.SCRATCH2), 2, 0.0).getValue());
	}

	// sound_channels：トラックの数がメタ情報インデックス値上限を超える時、WRONG_DATAが報告され、当該トラックは無視されること
	@Test
	public void test_SoundChannels_Track_TooMany() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.WRONG_DATA, r.e.get(0).getType());
		assertEquals(BmsSpec.INDEXED_META_INDEX_MAX, r.c.getIndexedMetaCount(BeMusicMeta.WAV.getName()));
		assertNull(r.c.getNote(chVisible(BeMusicDevice.SWITCH11), 0, 0.0));  // 上限超過したトラックの定義が無効であることを期待
	}

	// sound_channels：nameの値が同じトラックでは、トラックIDが同一で出力されること
	@Test
	public void test_SoundChannels_Name_Same() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals(2, r.c.getIndexedMetaCount(BeMusicMeta.WAV.getName()));
		assertEquals("a.wav", r.c.getIndexedMeta(BeMusicMeta.WAV.getName(), 1));
		assertEquals("b.wav", r.c.getIndexedMeta(BeMusicMeta.WAV.getName(), 2));
		assertEquals(1, r.c.getNote(chVisible(BeMusicDevice.SWITCH11), 0, 0.0).getValue());
		assertEquals(2, r.c.getNote(chVisible(BeMusicDevice.SWITCH11), 0, 96.0).getValue());
		assertEquals(1, r.c.getNote(chVisible(BeMusicDevice.SWITCH11), 1, 0.0).getValue());
	}

	// sound_channels：nameが未定義の時、SYNTAXが報告され、当該トラックは無視されること
	@Test
	public void test_SoundChannels_Name_Undefined() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.SYNTAX, r.e.get(0).getType());
		assertEquals(0, r.c.getIndexedMetaCount(BeMusicMeta.WAV.getName()));
		assertNull(r.c.getNote(chVisible(BeMusicDevice.SWITCH11), 0, 0.0));
	}

	// sound_channels：nameが文字列に変換できない時、WRONG_DATAが報告され、当該トラックは無視されること
	@Test
	public void test_SoundChannels_Name_WrongDefinition() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.WRONG_DATA, r.e.get(0).getType());
		assertEquals(0, r.c.getIndexedMetaCount(BeMusicMeta.WAV.getName()));
		assertNull(r.c.getNote(chVisible(BeMusicDevice.SWITCH11), 0, 0.0));
	}

	// sound_channels：notesが未定義の時、SYNTAXが報告され、当該トラックは無視されること。ただしnameに該当する#WAVxxは出力されること
	@Test
	public void test_SoundChannels_Notes_Undefined() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.SYNTAX, r.e.get(0).getType());
		assertEquals(1, r.c.getIndexedMetaCount(BeMusicMeta.WAV.getName()));
		assertEquals("a.wav", r.c.getIndexedMeta(BeMusicMeta.WAV.getName(), 1));
	}

	// sound_channels：notesが空配列の時、当該トラックは#WAVxxのみが出力されること
	@Test
	public void test_SoundChannels_Notes_Empty() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals(1, r.c.getIndexedMetaCount(BeMusicMeta.WAV.getName()));
		assertEquals("a.wav", r.c.getIndexedMeta(BeMusicMeta.WAV.getName(), 1));
	}

	// sound_channels：notesが配列でない時、WRONG_DATAが報告され、当該トラックは無視されること。ただしnameに該当する#WAVxxは出力されること
	@Test
	public void test_SoundChannels_Notes_WrongDefinition() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.WRONG_DATA, r.e.get(0).getType());
		assertEquals(1, r.c.getIndexedMetaCount(BeMusicMeta.WAV.getName()));
		assertEquals("a.wav", r.c.getIndexedMeta(BeMusicMeta.WAV.getName(), 1));
	}

	// sound_channels：xが0の時、BGMチャンネルへ出力されること
	@Test
	public void test_SoundChannels_X_0() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals(1, r.c.getIndexedMetaCount(BeMusicMeta.WAV.getName()));
		assertEquals("a.wav", r.c.getIndexedMeta(BeMusicMeta.WAV.getName(), 1));
		assertEquals(1, r.c.getNote(BeMusicChannel.BGM.getNumber(), 0, 0, 96.0).getValue());
	}

	// sound_channels：xがnullの時、BGMチャンネルへ出力されること
	@Test
	public void test_SoundChannels_X_Null() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals(1, r.c.getIndexedMetaCount(BeMusicMeta.WAV.getName()));
		assertEquals("a.wav", r.c.getIndexedMeta(BeMusicMeta.WAV.getName(), 1));
		assertEquals(1, r.c.getNote(BeMusicChannel.BGM.getNumber(), 0, 0, 96.0).getValue());
	}

	// sound_channels：xが1～8の時、1～7がSWITCH1x, 8がSCRATCH1へ出力されること
	@Test
	public void test_SoundChannels_X_Primary() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals(1, r.c.getIndexedMetaCount(BeMusicMeta.WAV.getName()));
		assertEquals("a.wav", r.c.getIndexedMeta(BeMusicMeta.WAV.getName(), 1));
		assertEquals(1, r.c.getNote(chVisible(BeMusicDevice.SWITCH11), 0, 0.0).getValue());
		assertEquals(1, r.c.getNote(chVisible(BeMusicDevice.SWITCH12), 0, 24.0).getValue());
		assertEquals(1, r.c.getNote(chVisible(BeMusicDevice.SWITCH13), 0, 48.0).getValue());
		assertEquals(1, r.c.getNote(chVisible(BeMusicDevice.SWITCH14), 0, 72.0).getValue());
		assertEquals(1, r.c.getNote(chVisible(BeMusicDevice.SWITCH15), 0, 96.0).getValue());
		assertEquals(1, r.c.getNote(chVisible(BeMusicDevice.SWITCH16), 0, 120.0).getValue());
		assertEquals(1, r.c.getNote(chVisible(BeMusicDevice.SWITCH17), 0, 144.0).getValue());
		assertEquals(1, r.c.getNote(chVisible(BeMusicDevice.SCRATCH1), 0, 168.0).getValue());
	}

	// sound_channels：xが9～16の時、9～15がSWITCH2x, 16がSCRATCH2へ出力されること
	@Test
	public void test_SoundChannels_X_Secondary() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals(1, r.c.getIndexedMetaCount(BeMusicMeta.WAV.getName()));
		assertEquals("a.wav", r.c.getIndexedMeta(BeMusicMeta.WAV.getName(), 1));
		assertEquals(1, r.c.getNote(chVisible(BeMusicDevice.SWITCH21), 0, 0.0).getValue());
		assertEquals(1, r.c.getNote(chVisible(BeMusicDevice.SWITCH22), 0, 24.0).getValue());
		assertEquals(1, r.c.getNote(chVisible(BeMusicDevice.SWITCH23), 0, 48.0).getValue());
		assertEquals(1, r.c.getNote(chVisible(BeMusicDevice.SWITCH24), 0, 72.0).getValue());
		assertEquals(1, r.c.getNote(chVisible(BeMusicDevice.SWITCH25), 0, 96.0).getValue());
		assertEquals(1, r.c.getNote(chVisible(BeMusicDevice.SWITCH26), 0, 120.0).getValue());
		assertEquals(1, r.c.getNote(chVisible(BeMusicDevice.SWITCH27), 0, 144.0).getValue());
		assertEquals(1, r.c.getNote(chVisible(BeMusicDevice.SCRATCH2), 0, 168.0).getValue());
	}

	// sound_channels：xが負の値の時、WRONG_DATAが報告され、当該ノートは無視されること
	@Test
	public void test_SoundChannels_X_MinusValue() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.WRONG_DATA, r.e.get(0).getType());
		assertEquals(1, r.c.getIndexedMetaCount(BeMusicMeta.WAV.getName()));
		assertEquals("a.wav", r.c.getIndexedMeta(BeMusicMeta.WAV.getName(), 1));
		assertEquals(1, r.c.getNote(chVisible(BeMusicDevice.SWITCH11), 0, 0.0).getValue());
		assertEquals(0, r.c.countNotes(0, 48.0, n -> true));
	}

	// sound_channels：xが17以上の時、UNKNOWN_CHANNELが報告され、当該ノートは無視されること
	@Test
	public void test_SoundChannels_X_WrongValue() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.UNKNOWN_CHANNEL, r.e.get(0).getType());
		assertEquals(1, r.c.getIndexedMetaCount(BeMusicMeta.WAV.getName()));
		assertEquals("a.wav", r.c.getIndexedMeta(BeMusicMeta.WAV.getName(), 1));
		assertEquals(1, r.c.getNote(chVisible(BeMusicDevice.SWITCH11), 0, 0.0).getValue());
		assertEquals(0, r.c.countNotes(0, 48.0, n -> true));
	}

	// sound_channels：xが数値に変換できない時、WRONG_DATAが報告され、当該ノートは無視されること
	@Test
	public void test_SoundChannels_X_WrongDefinition() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.WRONG_DATA, r.e.get(0).getType());
		assertEquals(1, r.c.getIndexedMetaCount(BeMusicMeta.WAV.getName()));
		assertEquals("a.wav", r.c.getIndexedMeta(BeMusicMeta.WAV.getName(), 1));
		assertEquals(1, r.c.getNote(chVisible(BeMusicDevice.SWITCH11), 0, 0.0).getValue());
		assertEquals(0, r.c.countNotes(0, 48.0, n -> true));
	}

	// sound_channels：x,yが同じ位置の定義がある時、同一トラック・別トラックに関わらず後発の定義が優先されること
	@Test
	public void test_SoundChannels_XY_Same() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals(2, r.c.getIndexedMetaCount(BeMusicMeta.WAV.getName()));
		assertEquals("a.wav", r.c.getIndexedMeta(BeMusicMeta.WAV.getName(), 1));
		assertEquals("b.wav", r.c.getIndexedMeta(BeMusicMeta.WAV.getName(), 2));
		assertEquals(1, r.c.getNote(chVisible(BeMusicDevice.SWITCH13), 0, 0.0).getValue());
		assertEquals(2, r.c.getNote(chVisible(BeMusicDevice.SWITCH15), 0, 48.0).getValue());
	}

	// sound_channels：yがない時、SYNTAXが報告され、当該ノートは無視されること
	@Test
	public void test_SoundChannels_Y_Undefined() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.SYNTAX, r.e.get(0).getType());
		assertEquals(1, r.c.getIndexedMetaCount(BeMusicMeta.WAV.getName()));
		assertEquals("a.wav", r.c.getIndexedMeta(BeMusicMeta.WAV.getName(), 1));
		assertNull(r.c.getNote(chVisible(BeMusicDevice.SWITCH11), 0, 0.0));
	}

	// sound_channels：yが負の値の時、WRONG_DATAが報告され、当該ノートは無視されること
	@Test
	public void test_SoundChannels_Y_MinusValue() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.WRONG_DATA, r.e.get(0).getType());
		assertEquals(1, r.c.getIndexedMetaCount(BeMusicMeta.WAV.getName()));
		assertEquals("a.wav", r.c.getIndexedMeta(BeMusicMeta.WAV.getName(), 1));
		assertEquals(0, r.c.countNotes(n -> n.getChannel() == chVisible(BeMusicDevice.SWITCH11)));
	}

	// sound_channels：yが数値に変換できない時、WRONG_DATAが報告され、当該ノートは無視されること
	@Test
	public void test_SoundChannels_Y_WrongDefinition() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.WRONG_DATA, r.e.get(0).getType());
		assertEquals(1, r.c.getIndexedMetaCount(BeMusicMeta.WAV.getName()));
		assertEquals("a.wav", r.c.getIndexedMeta(BeMusicMeta.WAV.getName(), 1));
		assertEquals(0, r.c.countNotes(n -> n.getChannel() == chVisible(BeMusicDevice.SWITCH11)));
	}

	// sound_channels：lが0の時、当該ノートは#xxx1y-1z,2y-2zに出力されること
	@Test
	public void test_SoundChannels_L_Beat() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals(1, r.c.getIndexedMetaCount(BeMusicMeta.WAV.getName()));
		assertEquals("a.wav", r.c.getIndexedMeta(BeMusicMeta.WAV.getName(), 1));
		assertEquals(1, r.c.getNote(chVisible(BeMusicDevice.SWITCH11), 0, 0.0).getValue());
		assertEquals(1, r.c.getNote(chVisible(BeMusicDevice.SWITCH12), 0, 12.0).getValue());
		assertEquals(1, r.c.getNote(chVisible(BeMusicDevice.SWITCH13), 0, 24.0).getValue());
		assertEquals(1, r.c.getNote(chVisible(BeMusicDevice.SWITCH14), 0, 36.0).getValue());
		assertEquals(1, r.c.getNote(chVisible(BeMusicDevice.SWITCH15), 0, 48.0).getValue());
		assertEquals(1, r.c.getNote(chVisible(BeMusicDevice.SWITCH16), 0, 60.0).getValue());
		assertEquals(1, r.c.getNote(chVisible(BeMusicDevice.SWITCH17), 0, 72.0).getValue());
		assertEquals(1, r.c.getNote(chVisible(BeMusicDevice.SCRATCH1), 0, 84.0).getValue());
		assertEquals(1, r.c.getNote(chVisible(BeMusicDevice.SWITCH21), 0, 96.0).getValue());
		assertEquals(1, r.c.getNote(chVisible(BeMusicDevice.SWITCH22), 0, 108.0).getValue());
		assertEquals(1, r.c.getNote(chVisible(BeMusicDevice.SWITCH23), 0, 120.0).getValue());
		assertEquals(1, r.c.getNote(chVisible(BeMusicDevice.SWITCH24), 0, 132.0).getValue());
		assertEquals(1, r.c.getNote(chVisible(BeMusicDevice.SWITCH25), 0, 144.0).getValue());
		assertEquals(1, r.c.getNote(chVisible(BeMusicDevice.SWITCH26), 0, 156.0).getValue());
		assertEquals(1, r.c.getNote(chVisible(BeMusicDevice.SWITCH27), 0, 168.0).getValue());
		assertEquals(1, r.c.getNote(chVisible(BeMusicDevice.SCRATCH2), 0, 180.0).getValue());
	}

	// sound_channels：lが1以上の時、当該ノートは#xxx5y-5z,6y-6zに出力され、y+lの位置にロングノート終端が出力されること
	@Test
	public void test_SoundChannels_L_Long() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals(1, r.c.getIndexedMetaCount(BeMusicMeta.WAV.getName()));
		assertEquals("a.wav", r.c.getIndexedMeta(BeMusicMeta.WAV.getName(), 1));
		assertEquals(1, r.c.getNote(chLong(BeMusicDevice.SWITCH11), 0, 0.0).getValue());
		assertEquals(1, r.c.getNote(chLong(BeMusicDevice.SWITCH11), 0, 96.0).getValue());
		assertEquals(1, r.c.getNote(chLong(BeMusicDevice.SWITCH12), 0, 12.0).getValue());
		assertEquals(1, r.c.getNote(chLong(BeMusicDevice.SWITCH12), 0, 108.0).getValue());
		assertEquals(1, r.c.getNote(chLong(BeMusicDevice.SWITCH13), 0, 24.0).getValue());
		assertEquals(1, r.c.getNote(chLong(BeMusicDevice.SWITCH13), 0, 120.0).getValue());
		assertEquals(1, r.c.getNote(chLong(BeMusicDevice.SWITCH14), 0, 36.0).getValue());
		assertEquals(1, r.c.getNote(chLong(BeMusicDevice.SWITCH14), 0, 132.0).getValue());
		assertEquals(1, r.c.getNote(chLong(BeMusicDevice.SWITCH15), 0, 48.0).getValue());
		assertEquals(1, r.c.getNote(chLong(BeMusicDevice.SWITCH15), 0, 144.0).getValue());
		assertEquals(1, r.c.getNote(chLong(BeMusicDevice.SWITCH16), 0, 60.0).getValue());
		assertEquals(1, r.c.getNote(chLong(BeMusicDevice.SWITCH16), 0, 156.0).getValue());
		assertEquals(1, r.c.getNote(chLong(BeMusicDevice.SWITCH17), 0, 72.0).getValue());
		assertEquals(1, r.c.getNote(chLong(BeMusicDevice.SWITCH17), 0, 168.0).getValue());
		assertEquals(1, r.c.getNote(chLong(BeMusicDevice.SCRATCH1), 0, 84.0).getValue());
		assertEquals(1, r.c.getNote(chLong(BeMusicDevice.SCRATCH1), 0, 180.0).getValue());
		assertEquals(1, r.c.getNote(chLong(BeMusicDevice.SWITCH21), 0, 96.0).getValue());
		assertEquals(1, r.c.getNote(chLong(BeMusicDevice.SWITCH21), 2, 0.0).getValue());
		assertEquals(1, r.c.getNote(chLong(BeMusicDevice.SWITCH22), 0, 108.0).getValue());
		assertEquals(1, r.c.getNote(chLong(BeMusicDevice.SWITCH22), 2, 12.0).getValue());
		assertEquals(1, r.c.getNote(chLong(BeMusicDevice.SWITCH23), 0, 120.0).getValue());
		assertEquals(1, r.c.getNote(chLong(BeMusicDevice.SWITCH23), 2, 24.0).getValue());
		assertEquals(1, r.c.getNote(chLong(BeMusicDevice.SWITCH24), 0, 132.0).getValue());
		assertEquals(1, r.c.getNote(chLong(BeMusicDevice.SWITCH24), 2, 36.0).getValue());
		assertEquals(1, r.c.getNote(chLong(BeMusicDevice.SWITCH25), 0, 144.0).getValue());
		assertEquals(1, r.c.getNote(chLong(BeMusicDevice.SWITCH25), 2, 48.0).getValue());
		assertEquals(1, r.c.getNote(chLong(BeMusicDevice.SWITCH26), 0, 156.0).getValue());
		assertEquals(1, r.c.getNote(chLong(BeMusicDevice.SWITCH26), 2, 60.0).getValue());
		assertEquals(1, r.c.getNote(chLong(BeMusicDevice.SWITCH27), 0, 168.0).getValue());
		assertEquals(1, r.c.getNote(chLong(BeMusicDevice.SWITCH27), 2, 72.0).getValue());
		assertEquals(1, r.c.getNote(chLong(BeMusicDevice.SCRATCH2), 0, 180.0).getValue());
		assertEquals(1, r.c.getNote(chLong(BeMusicDevice.SCRATCH2), 2, 84.0).getValue());
	}

	// sound_channels：lがない時、SYNTAXが報告され、当該ノートは無視されること
	@Test
	public void test_SoundChannels_L_Undefined() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.SYNTAX, r.e.get(0).getType());
		assertEquals(1, r.c.getIndexedMetaCount(BeMusicMeta.WAV.getName()));
		assertEquals("a.wav", r.c.getIndexedMeta(BeMusicMeta.WAV.getName(), 1));
		assertNull(r.c.getNote(chLong(BeMusicDevice.SWITCH11), 0, 0.0));
	}

	// sound_channels：lが負の値の時、WRONG_DATAが報告され、当該ノートは無視されること
	@Test
	public void test_SoundChannels_L_MinusValue() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.WRONG_DATA, r.e.get(0).getType());
		assertEquals(1, r.c.getIndexedMetaCount(BeMusicMeta.WAV.getName()));
		assertEquals("a.wav", r.c.getIndexedMeta(BeMusicMeta.WAV.getName(), 1));
		assertNull(r.c.getNote(chLong(BeMusicDevice.SWITCH11), 0, 0.0));
	}

	// sound_channels：lが数値に変換できない時、WRONG_DATAが報告され、当該ノートは無視されること
	@Test
	public void test_SoundChannels_L_WrongDefinition() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.WRONG_DATA, r.e.get(0).getType());
		assertEquals(1, r.c.getIndexedMetaCount(BeMusicMeta.WAV.getName()));
		assertEquals("a.wav", r.c.getIndexedMeta(BeMusicMeta.WAV.getName(), 1));
		assertNull(r.c.getNote(chLong(BeMusicDevice.SWITCH11), 0, 0.0));
	}

	// sound_channels：cがfalseの時、当該ノートが音声データ再開必要として出力されること
	@Test
	public void test_SoundChannels_C_Restart() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals(1, r.c.getIndexedMetaCount(BeMusicMeta.WAV.getName()));
		assertEquals("a.wav", r.c.getIndexedMeta(BeMusicMeta.WAV.getName(), 1));
		var v = r.c.getNote(chVisible(BeMusicDevice.SWITCH11), 0, 0.0).getValue();
		assertEquals(true, BeMusicSound.isRestartTrack(v));
	}

	// sound_channels：cがtrueの時、当該ノートが音声データ再開不要として出力されること
	@Test
	public void test_SoundChannels_C_NotRestart() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals(1, r.c.getIndexedMetaCount(BeMusicMeta.WAV.getName()));
		assertEquals("a.wav", r.c.getIndexedMeta(BeMusicMeta.WAV.getName(), 1));
		var v = r.c.getNote(chVisible(BeMusicDevice.SWITCH11), 0, 0.0).getValue();
		assertEquals(false, BeMusicSound.isRestartTrack(v));
	}

	// sound_channels：cがない時、SYNTAXが報告され、当該ノートは無視されること
	@Test
	public void test_SoundChannels_C_Undefined() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.SYNTAX, r.e.get(0).getType());
		assertEquals(1, r.c.getIndexedMetaCount(BeMusicMeta.WAV.getName()));
		assertEquals("a.wav", r.c.getIndexedMeta(BeMusicMeta.WAV.getName(), 1));
		assertNull(r.c.getNote(chVisible(BeMusicDevice.SWITCH11), 0, 0.0));
	}

	// sound_channels：cがbooleanに変換できない時、WRONG_DATAが報告され、当該ノートは無視されること
	@Test
	public void test_SoundChannels_C_WrongDefinition() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.WRONG_DATA, r.e.get(0).getType());
		assertEquals(1, r.c.getIndexedMetaCount(BeMusicMeta.WAV.getName()));
		assertEquals("a.wav", r.c.getIndexedMeta(BeMusicMeta.WAV.getName(), 1));
		assertNull(r.c.getNote(chVisible(BeMusicDevice.SWITCH11), 0, 0.0));
	}

	// sound_channels：tが1の時、当該ノートのLN種別がLNになること
	@Test
	public void test_SoundChannels_T_Ln() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals(1, r.c.getIndexedMetaCount(BeMusicMeta.WAV.getName()));
		assertEquals("a.wav", r.c.getIndexedMeta(BeMusicMeta.WAV.getName(), 1));
		var v1 = r.c.getNote(chLong(BeMusicDevice.SWITCH11), 0, 0.0).getValue();
		assertEquals(BeMusicLongNoteMode.LN, BeMusicSound.getLongNoteMode(v1, null));
		var v2 = r.c.getNote(chLong(BeMusicDevice.SWITCH11), 0, 96.0).getValue();
		assertEquals(BeMusicLongNoteMode.LN, BeMusicSound.getLongNoteMode(v2, null));
		var p = new BeMusicChartBuilder(r.c).createList();
		assertEquals(2, p.size());
		assertEquals(BeMusicNoteType.LONG_ON, p.get(0).getVisibleNoteType(BeMusicDevice.SWITCH11));
		assertEquals(BeMusicNoteType.LONG_OFF, p.get(1).getVisibleNoteType(BeMusicDevice.SWITCH11));
	}

	// sound_channels：tが2の時、当該ノートのLN種別がCNになること
	@Test
	public void test_SoundChannels_T_Cn() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals(1, r.c.getIndexedMetaCount(BeMusicMeta.WAV.getName()));
		assertEquals("a.wav", r.c.getIndexedMeta(BeMusicMeta.WAV.getName(), 1));
		var v1 = r.c.getNote(chLong(BeMusicDevice.SWITCH11), 0, 0.0).getValue();
		assertEquals(BeMusicLongNoteMode.CN, BeMusicSound.getLongNoteMode(v1, null));
		var v2 = r.c.getNote(chLong(BeMusicDevice.SWITCH11), 0, 96.0).getValue();
		assertEquals(BeMusicLongNoteMode.CN, BeMusicSound.getLongNoteMode(v2, null));
		var p = new BeMusicChartBuilder(r.c).createList();
		assertEquals(2, p.size());
		assertEquals(BeMusicNoteType.LONG_ON, p.get(0).getVisibleNoteType(BeMusicDevice.SWITCH11));
		assertEquals(BeMusicNoteType.CHARGE_OFF, p.get(1).getVisibleNoteType(BeMusicDevice.SWITCH11));
	}

	// sound_channels：tが3の時、当該ノートのLN種別がHCNになること
	@Test
	public void test_SoundChannels_T_Hcn() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals(1, r.c.getIndexedMetaCount(BeMusicMeta.WAV.getName()));
		assertEquals("a.wav", r.c.getIndexedMeta(BeMusicMeta.WAV.getName(), 1));
		var v1 = r.c.getNote(chLong(BeMusicDevice.SWITCH11), 0, 0.0).getValue();
		assertEquals(BeMusicLongNoteMode.HCN, BeMusicSound.getLongNoteMode(v1, null));
		var v2 = r.c.getNote(chLong(BeMusicDevice.SWITCH11), 0, 96.0).getValue();
		assertEquals(BeMusicLongNoteMode.HCN, BeMusicSound.getLongNoteMode(v2, null));
		var p = new BeMusicChartBuilder(r.c).createList();
		assertEquals(2, p.size());
		assertEquals(BeMusicNoteType.LONG_ON, p.get(0).getVisibleNoteType(BeMusicDevice.SWITCH11));
		assertEquals(BeMusicNoteType.CHARGE_OFF, p.get(1).getVisibleNoteType(BeMusicDevice.SWITCH11));
	}

	// sound_channels：tが0の時、当該ノートのLN種別がLNになり、ノートの値のLnTypeビットは0になること
	@Test
	public void test_SoundChannels_T_Neutral() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals(1, r.c.getIndexedMetaCount(BeMusicMeta.WAV.getName()));
		assertEquals("a.wav", r.c.getIndexedMeta(BeMusicMeta.WAV.getName(), 1));
		var v1 = r.c.getNote(chLong(BeMusicDevice.SWITCH11), 0, 0.0).getValue();
		assertNull(BeMusicSound.getLongNoteMode(v1, null));
		var v2 = r.c.getNote(chLong(BeMusicDevice.SWITCH11), 0, 96.0).getValue();
		assertNull(BeMusicSound.getLongNoteMode(v2, null));
		var p = new BeMusicChartBuilder(r.c).createList();
		assertEquals(2, p.size());
		assertEquals(BeMusicNoteType.LONG_ON, p.get(0).getVisibleNoteType(BeMusicDevice.SWITCH11));
		assertEquals(BeMusicNoteType.LONG_OFF, p.get(1).getVisibleNoteType(BeMusicDevice.SWITCH11));
	}

	// sound_channels：tがない時、当該ノートのLN種別がLNになり、ノートの値のLnTypeビットは0になること（oraja独自仕様のため省略可）
	@Test
	public void test_SoundChannels_T_Undefined() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals(1, r.c.getIndexedMetaCount(BeMusicMeta.WAV.getName()));
		assertEquals("a.wav", r.c.getIndexedMeta(BeMusicMeta.WAV.getName(), 1));
		var v1 = r.c.getNote(chLong(BeMusicDevice.SWITCH11), 0, 0.0).getValue();
		assertNull(BeMusicSound.getLongNoteMode(v1, null));
		var v2 = r.c.getNote(chLong(BeMusicDevice.SWITCH11), 0, 96.0).getValue();
		assertNull(BeMusicSound.getLongNoteMode(v2, null));
		var p = new BeMusicChartBuilder(r.c).createList();
		assertEquals(2, p.size());
		assertEquals(BeMusicNoteType.LONG_ON, p.get(0).getVisibleNoteType(BeMusicDevice.SWITCH11));
		assertEquals(BeMusicNoteType.LONG_OFF, p.get(1).getVisibleNoteType(BeMusicDevice.SWITCH11));
	}

	// sound_channels：tが0～3以外の時、WRONG_DATAが報告され、当該ノートは無視されること
	@Test
	public void test_SoundChannels_T_WrongValue() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.WRONG_DATA, r.e.get(0).getType());
		assertEquals(1, r.c.getIndexedMetaCount(BeMusicMeta.WAV.getName()));
		assertEquals("a.wav", r.c.getIndexedMeta(BeMusicMeta.WAV.getName(), 1));
		assertNull(r.c.getNote(chLong(BeMusicDevice.SWITCH11), 0, 0.0));
		assertNull(r.c.getNote(chLong(BeMusicDevice.SWITCH11), 0, 96.0));
	}

	// sound_channels：tが数値に変換できない時、WRONG_DATAが報告され、当該ノートは無視されること
	@Test
	public void test_SoundChannels_T_WrongDefinition() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.WRONG_DATA, r.e.get(0).getType());
		assertEquals(1, r.c.getIndexedMetaCount(BeMusicMeta.WAV.getName()));
		assertEquals("a.wav", r.c.getIndexedMeta(BeMusicMeta.WAV.getName(), 1));
		assertNull(r.c.getNote(chLong(BeMusicDevice.SWITCH11), 0, 0.0));
		assertNull(r.c.getNote(chLong(BeMusicDevice.SWITCH11), 0, 96.0));
	}

	// sound_channels：upがfalseでx,y同一位置にロングノート終端がある時、当該ノートは通常のノート定義として扱われること
	@Test
	public void test_SoundChannels_Up_FalseAndHasLongOffAtSameXY() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals(2, r.c.getIndexedMetaCount(BeMusicMeta.WAV.getName()));
		assertEquals("a.wav", r.c.getIndexedMeta(BeMusicMeta.WAV.getName(), 1));
		assertEquals("b.wav", r.c.getIndexedMeta(BeMusicMeta.WAV.getName(), 2));
		assertEquals(1, r.c.getNote(chLong(BeMusicDevice.SWITCH11), 0, 0.0).getValue());
		assertEquals(1, r.c.getNote(chLong(BeMusicDevice.SWITCH11), 0, 96.0).getValue());
		assertEquals(2, r.c.getNote(chVisible(BeMusicDevice.SWITCH11), 0, 96.0).getValue());
	}

	// sound_channels：upがtrueでx,y同一位置にロングノート終端がない時、当該ノートは通常のノート定義として扱われること
	@Test
	public void test_SoundChannels_Up_TrueAndNoLongOffAtSameXY() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals(2, r.c.getIndexedMetaCount(BeMusicMeta.WAV.getName()));
		assertEquals("a.wav", r.c.getIndexedMeta(BeMusicMeta.WAV.getName(), 1));
		assertEquals("b.wav", r.c.getIndexedMeta(BeMusicMeta.WAV.getName(), 2));
		assertEquals(1, r.c.getNote(chLong(BeMusicDevice.SWITCH11), 0, 0.0).getValue());
		assertEquals(1, r.c.getNote(chLong(BeMusicDevice.SWITCH11), 0, 96.0).getValue());
		assertEquals(2, r.c.getNote(chVisible(BeMusicDevice.SWITCH11), 0, 48.0).getValue());
	}

	// sound_channels：upがtrueでx,y同一位置にロングノート終端がある時、当該ロングノート終端のトラックIDのみが当該ノートのトラックIDで上書きされること
	@Test
	public void test_SoundChannels_Up_TrueAndHasLongOffAtSameXY() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals(2, r.c.getIndexedMetaCount(BeMusicMeta.WAV.getName()));
		assertEquals("a.wav", r.c.getIndexedMeta(BeMusicMeta.WAV.getName(), 1));
		assertEquals("b.wav", r.c.getIndexedMeta(BeMusicMeta.WAV.getName(), 2));
		assertEquals(1, r.c.getNote(chLong(BeMusicDevice.SWITCH11), 0, 0.0).getValue());
		assertEquals(2, r.c.getNote(chLong(BeMusicDevice.SWITCH11), 0, 96.0).getValue());
		assertNull(r.c.getNote(chVisible(BeMusicDevice.SWITCH11), 0, 96.0));
	}

	// sound_channels：upがない時、通常のノート定義として扱われること
	@Test
	public void test_SoundChannels_Up_Undefined() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals(1, r.c.getIndexedMetaCount(BeMusicMeta.WAV.getName()));
		assertEquals("a.wav", r.c.getIndexedMeta(BeMusicMeta.WAV.getName(), 1));
		assertEquals(1, r.c.getNote(chVisible(BeMusicDevice.SWITCH11), 0, 0.0).getValue());
	}

	// sound_channels：upがbooleanに変換できない時、WRONG_DATAが報告され、当該ノートは無視されること
	@Test
	public void test_SoundChannels_Up_WrongDefinition() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.WRONG_DATA, r.e.get(0).getType());
		assertEquals(1, r.c.getIndexedMetaCount(BeMusicMeta.WAV.getName()));
		assertEquals("a.wav", r.c.getIndexedMeta(BeMusicMeta.WAV.getName(), 1));
		assertNull(r.c.getNote(chVisible(BeMusicDevice.SWITCH11), 0, 0.0));
	}

	// mine_channels：未定義の時、何も行われないこと
	@Test
	public void test_MineChannels_Undefined() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
	}

	// mine_channels：配列でない時、SYNTAXが報告されること
	@Test
	public void test_MineChannels_WrongDefinition() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.SYNTAX, r.e.get(0).getType());
	}

	// mine_channels：空配列の時、何も行われないこと
	@Test
	public void test_MineChannels_Empty() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
	}

	// mine_channels：タイムライン解析省略時、#WAV00が出力され、チャンネルは出力されないこと
	@Test
	public void test_MineChannels_SkipTimeline() throws Exception {
		var r = content(ld -> ld.setSkipReadTimeline(true));
		assertEquals(true, r.e.isEmpty());
		assertEquals(1, r.c.getIndexedMetaCount(BeMusicMeta.WAV.getName()));
		assertEquals("mine.wav", r.c.getIndexedMeta(BeMusicMeta.WAV.getName(), 0));
		assertNull(r.c.getNote(chMine(BeMusicDevice.SWITCH11), 0, 0.0));
	}

	// mine_channels：nameの値は#WAV00に出力されること
	@Test
	public void test_MineChannels_Name_Normal() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals(1, r.c.getIndexedMetaCount(BeMusicMeta.WAV.getName()));
		assertEquals("mine.wav", r.c.getIndexedMeta(BeMusicMeta.WAV.getName(), 0));
	}

	// mine_channels：nameが空文字列の時、#WAV00には出力されず、エラーも報告されないこと
	@Test
	public void test_MineChannels_Name_Empty() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals(0, r.c.getIndexedMetaCount(BeMusicMeta.WAV.getName()));
	}

	// mine_channels：nameが未定義の時、SYNTAXが報告されること。ただしnotesは正常に解析されること
	@Test
	public void test_MineChannels_Name_Undefined() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.SYNTAX, r.e.get(0).getType());
		assertEquals(0, r.c.getIndexedMetaCount(BeMusicMeta.WAV.getName()));
		assertEquals(1, r.c.getNote(chMine(BeMusicDevice.SWITCH11), 0, 0.0).getValue());
	}

	// mine_channels：nameが文字列に変換できない時、WRONG_DATAが報告されること。ただしnotesは正常に解析されること
	@Test
	public void test_MineChannels_Name_WrongDefinition() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.WRONG_DATA, r.e.get(0).getType());
		assertEquals(0, r.c.getIndexedMetaCount(BeMusicMeta.WAV.getName()));
		assertEquals(1, r.c.getNote(chMine(BeMusicDevice.SWITCH11), 0, 0.0).getValue());
	}

	// mine_channels：notesが未定義の時、SYNTAXが報告されること。ただしnameに該当する#WAV00は出力されること
	@Test
	public void test_MineChannels_Notes_Undefined() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.SYNTAX, r.e.get(0).getType());
		assertEquals(1, r.c.getIndexedMetaCount(BeMusicMeta.WAV.getName()));
		assertEquals("mine.wav", r.c.getIndexedMeta(BeMusicMeta.WAV.getName(), 0));
	}

	// mine_channels：notesが空配列の時、当該トラックは#WAV00のみが出力されること
	@Test
	public void test_MineChannels_Notes_Empty() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals(1, r.c.getIndexedMetaCount(BeMusicMeta.WAV.getName()));
		assertEquals("mine.wav", r.c.getIndexedMeta(BeMusicMeta.WAV.getName(), 0));
	}

	// mine_channels：notesが配列でない時、WRONG_DATAが報告されること。ただしnameに該当する#WAV00は出力されること
	@Test
	public void test_MineChannels_Notes_WrongDefinition() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.WRONG_DATA, r.e.get(0).getType());
		assertEquals(1, r.c.getIndexedMetaCount(BeMusicMeta.WAV.getName()));
		assertEquals("mine.wav", r.c.getIndexedMeta(BeMusicMeta.WAV.getName(), 0));
	}

	// mine_channels：xが0の時、UNKNOWN_CHANNELが報告され、当該ノートは無視されること
	@Test
	public void test_MineChannels_X_0() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.UNKNOWN_CHANNEL, r.e.get(0).getType());
		assertEquals(0, r.c.getIndexedMetaCount(BeMusicMeta.WAV.getName()));
		assertEquals(0, r.c.countNotes(n -> true));
	}

	// mine_channels：xがnullの時、WRONG_DATAが報告され、当該ノートは無視されること
	@Test
	public void test_MineChannels_X_Null() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.WRONG_DATA, r.e.get(0).getType());
		assertEquals(0, r.c.getIndexedMetaCount(BeMusicMeta.WAV.getName()));
		assertEquals(0, r.c.countNotes(n -> true));
	}

	// mine_channels：xが1～8の時、1～7がSWITCH1x, 8がSCRATCH1へ出力されること
	@Test
	public void test_MineChannels_X_Primary() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals(0, r.c.getIndexedMetaCount(BeMusicMeta.WAV.getName()));
		assertEquals(10, r.c.getNote(chMine(BeMusicDevice.SWITCH11), 0, 0.0).getValue());
		assertEquals(20, r.c.getNote(chMine(BeMusicDevice.SWITCH12), 0, 24.0).getValue());
		assertEquals(30, r.c.getNote(chMine(BeMusicDevice.SWITCH13), 0, 48.0).getValue());
		assertEquals(40, r.c.getNote(chMine(BeMusicDevice.SWITCH14), 0, 72.0).getValue());
		assertEquals(50, r.c.getNote(chMine(BeMusicDevice.SWITCH15), 0, 96.0).getValue());
		assertEquals(60, r.c.getNote(chMine(BeMusicDevice.SWITCH16), 0, 120.0).getValue());
		assertEquals(70, r.c.getNote(chMine(BeMusicDevice.SWITCH17), 0, 144.0).getValue());
		assertEquals(80, r.c.getNote(chMine(BeMusicDevice.SCRATCH1), 0, 168.0).getValue());
	}

	// mine_channels：xが9～16の時、9～15がSWITCH2x, 16がSCRATCH2へ出力されること
	@Test
	public void test_MineChannels_X_Secondary() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals(0, r.c.getIndexedMetaCount(BeMusicMeta.WAV.getName()));
		assertEquals(10, r.c.getNote(chMine(BeMusicDevice.SWITCH21), 0, 0.0).getValue());
		assertEquals(20, r.c.getNote(chMine(BeMusicDevice.SWITCH22), 0, 24.0).getValue());
		assertEquals(30, r.c.getNote(chMine(BeMusicDevice.SWITCH23), 0, 48.0).getValue());
		assertEquals(40, r.c.getNote(chMine(BeMusicDevice.SWITCH24), 0, 72.0).getValue());
		assertEquals(50, r.c.getNote(chMine(BeMusicDevice.SWITCH25), 0, 96.0).getValue());
		assertEquals(60, r.c.getNote(chMine(BeMusicDevice.SWITCH26), 0, 120.0).getValue());
		assertEquals(70, r.c.getNote(chMine(BeMusicDevice.SWITCH27), 0, 144.0).getValue());
		assertEquals(80, r.c.getNote(chMine(BeMusicDevice.SCRATCH2), 0, 168.0).getValue());
	}

	// mine_channels：xが負の値の時、WRONG_DATAが報告され、当該ノートは無視されること
	@Test
	public void test_MineChannels_X_MinusValue() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.WRONG_DATA, r.e.get(0).getType());
		assertEquals(0, r.c.getIndexedMetaCount(BeMusicMeta.WAV.getName()));
		assertEquals(0, r.c.countNotes(n -> true));
	}

	// mine_channels：xが17以上の時、UNKNOWN_CHANNELが報告され、当該ノートは無視されること
	@Test
	public void test_MineChannels_X_WrongValue() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.UNKNOWN_CHANNEL, r.e.get(0).getType());
		assertEquals(0, r.c.getIndexedMetaCount(BeMusicMeta.WAV.getName()));
		assertEquals(0, r.c.countNotes(n -> true));
	}

	// mine_channels：xが数値に変換できない時、WRONG_DATAが報告され、当該ノートは無視されること
	@Test
	public void test_MineChannels_X_WrongDefinition() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.WRONG_DATA, r.e.get(0).getType());
		assertEquals(0, r.c.getIndexedMetaCount(BeMusicMeta.WAV.getName()));
		assertEquals(0, r.c.countNotes(n -> true));
	}

	// mine_channels：x,yが同じ位置の定義がある時、同一トラック・別トラックに関わらず後発の定義が優先されること
	@Test
	public void test_MineChannels_XY_Same() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals(0, r.c.getIndexedMetaCount(BeMusicMeta.WAV.getName()));
		assertEquals(50, r.c.getNote(chMine(BeMusicDevice.SWITCH11), 0, 0.0).getValue());
		assertEquals(60, r.c.getNote(chMine(BeMusicDevice.SWITCH12), 0, 96.0).getValue());
	}

	// mine_channels：yがない時、SYNTAXが報告され、当該ノートは無視されること
	@Test
	public void test_MineChannels_Y_Undefined() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.SYNTAX, r.e.get(0).getType());
		assertEquals(0, r.c.getIndexedMetaCount(BeMusicMeta.WAV.getName()));
		assertEquals(0, r.c.countNotes(n -> true));
	}

	// mine_channels：yが負の値の時、WRONG_DATAが報告され、当該ノートは無視されること
	@Test
	public void test_MineChannels_Y_MinusValue() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.WRONG_DATA, r.e.get(0).getType());
		assertEquals(0, r.c.getIndexedMetaCount(BeMusicMeta.WAV.getName()));
		assertEquals(0, r.c.countNotes(n -> true));
	}

	// mine_channels：yが数値に変換できない時、WRONG_DATAが報告され、当該ノートは無視されること
	@Test
	public void test_MineChannels_Y_WrongDefinition() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.WRONG_DATA, r.e.get(0).getType());
		assertEquals(0, r.c.getIndexedMetaCount(BeMusicMeta.WAV.getName()));
		assertEquals(0, r.c.countNotes(n -> true));
	}

	// mine_channels：damageの値がノートの値として出力されること（範囲は1～0x7fffffff）
	@Test
	public void test_MineChannels_Damage_Normal() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals(0, r.c.getIndexedMetaCount(BeMusicMeta.WAV.getName()));
		assertEquals(1, r.c.getNote(chMine(BeMusicDevice.SWITCH11), 0, 0.0).getValue());
		assertEquals(0x7fffffff, r.c.getNote(chMine(BeMusicDevice.SWITCH11), 0, 96.0).getValue());
	}

	// mine_channels：damageの値が0超過～1未満の時、ノートの値は1として出力されること　※ライブラリ仕様として小数値は許容しない
	@Test
	public void test_MineChannels_Damage_LessThan1() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals(0, r.c.getIndexedMetaCount(BeMusicMeta.WAV.getName()));
		assertEquals(1, r.c.getNote(chMine(BeMusicDevice.SWITCH11), 0, 0.0).getValue());
		assertEquals(1, r.c.getNote(chMine(BeMusicDevice.SWITCH11), 0, 96.0).getValue());
	}

	// mine_channels：damageがない時、SYNTAXが報告され、当該ノートは無視されること
	@Test
	public void test_MineChannels_Damage_Undefined() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.SYNTAX, r.e.get(0).getType());
		assertEquals(0, r.c.getIndexedMetaCount(BeMusicMeta.WAV.getName()));
		assertEquals(0, r.c.countNotes(n -> true));
	}

	// mine_channels：damageが0の時、WRONG_DATAが報告され、当該ノートは無視されること
	@Test
	public void test_MineChannels_Damage_0() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.WRONG_DATA, r.e.get(0).getType());
		assertEquals(0, r.c.getIndexedMetaCount(BeMusicMeta.WAV.getName()));
		assertEquals(0, r.c.countNotes(n -> true));
	}

	// mine_channels：damageが負の値の時、WRONG_DATAが報告され、当該ノートは無視されること
	@Test
	public void test_MineChannels_Damage_MinusValue() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.WRONG_DATA, r.e.get(0).getType());
		assertEquals(0, r.c.getIndexedMetaCount(BeMusicMeta.WAV.getName()));
		assertEquals(0, r.c.countNotes(n -> true));
	}

	// mine_channels：damageが数値に変換できない時、WRONG_DATAが報告され、当該ノートは無視されること
	@Test
	public void test_MineChannels_Damage_WrongDefinition() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.WRONG_DATA, r.e.get(0).getType());
		assertEquals(0, r.c.getIndexedMetaCount(BeMusicMeta.WAV.getName()));
		assertEquals(0, r.c.countNotes(n -> true));
	}

	// key_channels：未定義の時、何も行われないこと
	@Test
	public void test_KeyChannels_Undefined() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
	}

	// key_channels：配列でない時、SYNTAXが報告されること
	@Test
	public void test_KeyChannels_WrongDefinition() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.SYNTAX, r.e.get(0).getType());
	}

	// key_channels：空配列の時、何も行われないこと
	@Test
	public void test_KeyChannels_Empty() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
	}

	// key_channels：タイムライン解析省略時、メタ情報が出力され、チャンネルは出力されないこと
	@Test
	public void test_KeyChannels_SkipTimeline() throws Exception {
		var r = content(ld -> ld.setSkipReadTimeline(true));
		assertEquals(true, r.e.isEmpty());
		assertEquals(1, r.c.getIndexedMetaCount(BeMusicMeta.WAV.getName()));
		assertEquals("a.wav", r.c.getIndexedMeta(BeMusicMeta.WAV.getName(), 1));
		assertNull(r.c.getNote(chInvisible(BeMusicDevice.SWITCH11), 0, 0.0));
	}

	// key_channels：配列要素インデックス+1がトラックIDとなること。#WAVxxと#xxx3y-3z,4y-4zが正しく出力されること（正常系）
	@Test
	public void test_KeyChannels_NormalCases() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals(3, r.c.getIndexedMetaCount(BeMusicMeta.WAV.getName()));
		assertEquals("a.wav", r.c.getIndexedMeta(BeMusicMeta.WAV.getName(), 1));
		assertEquals("b.wav", r.c.getIndexedMeta(BeMusicMeta.WAV.getName(), 2));
		assertEquals("c.wav", r.c.getIndexedMeta(BeMusicMeta.WAV.getName(), 3));
		assertEquals(1, r.c.getNote(chInvisible(BeMusicDevice.SWITCH11), 0, 0.0).getValue());
		assertEquals(1, r.c.getNote(chInvisible(BeMusicDevice.SWITCH12), 0, 24.0).getValue());
		assertEquals(1, r.c.getNote(chInvisible(BeMusicDevice.SWITCH13), 0, 48.0).getValue());
		assertEquals(1, r.c.getNote(chInvisible(BeMusicDevice.SWITCH14), 0, 72.0).getValue());
		assertEquals(1, r.c.getNote(chInvisible(BeMusicDevice.SWITCH15), 0, 96.0).getValue());
		assertEquals(1, r.c.getNote(chInvisible(BeMusicDevice.SWITCH16), 0, 120.0).getValue());
		assertEquals(1, r.c.getNote(chInvisible(BeMusicDevice.SWITCH17), 0, 144.0).getValue());
		assertEquals(1, r.c.getNote(chInvisible(BeMusicDevice.SCRATCH1), 0, 168.0).getValue());
		assertEquals(2, r.c.getNote(chInvisible(BeMusicDevice.SWITCH21), 0, 96.0).getValue());
		assertEquals(2, r.c.getNote(chInvisible(BeMusicDevice.SWITCH22), 0, 120.0).getValue());
		assertEquals(2, r.c.getNote(chInvisible(BeMusicDevice.SWITCH23), 0, 144.0).getValue());
		assertEquals(2, r.c.getNote(chInvisible(BeMusicDevice.SWITCH24), 0, 168.0).getValue());
		assertEquals(2, r.c.getNote(chInvisible(BeMusicDevice.SWITCH25), 1, 0.0).getValue());
		assertEquals(2, r.c.getNote(chInvisible(BeMusicDevice.SWITCH26), 1, 24.0).getValue());
		assertEquals(2, r.c.getNote(chInvisible(BeMusicDevice.SWITCH27), 1, 48.0).getValue());
		assertEquals(2, r.c.getNote(chInvisible(BeMusicDevice.SCRATCH2), 1, 72.0).getValue());
		assertEquals(3, r.c.getNote(chInvisible(BeMusicDevice.SCRATCH1), 1, 0.0).getValue());
		assertEquals(3, r.c.getNote(chInvisible(BeMusicDevice.SCRATCH2), 2, 0.0).getValue());
	}

	// key_channels：トラックの数がメタ情報インデックス値上限を超え無視されたトラックは無視されること
	@Test
	public void test_KeyChannels_Track_TooMany() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.WRONG_DATA, r.e.get(0).getType());
		assertEquals(BmsSpec.INDEXED_META_INDEX_MAX, r.c.getIndexedMetaCount(BeMusicMeta.WAV.getName()));
		assertNull(r.c.getNote(chInvisible(BeMusicDevice.SWITCH11), 0, 0.0));  // 上限超過したトラックの定義が無効であることを期待
	}

	// key_channels：nameの値が同じトラックでは、トラックIDが同一で出力されること
	@Test
	public void test_KeyChannels_Name_Same() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals(1, r.c.getIndexedMetaCount(BeMusicMeta.WAV.getName()));
		assertEquals("a.wav", r.c.getIndexedMeta(BeMusicMeta.WAV.getName(), 1));
		assertEquals(1, r.c.getNote(chInvisible(BeMusicDevice.SWITCH11), 0, 0.0).getValue());
		assertEquals(1, r.c.getNote(chInvisible(BeMusicDevice.SWITCH12), 0, 96.0).getValue());
	}

	// key_channels：nameの値がsound_channelsで使用しているものと競合した時、トラックIDはそれと同じ値になること
	@Test
	public void test_KeyChannels_Name_Share() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals(3, r.c.getIndexedMetaCount(BeMusicMeta.WAV.getName()));
		assertEquals("share.wav", r.c.getIndexedMeta(BeMusicMeta.WAV.getName(), 1));
		assertEquals("a.wav", r.c.getIndexedMeta(BeMusicMeta.WAV.getName(), 2));
		assertEquals("b.wav", r.c.getIndexedMeta(BeMusicMeta.WAV.getName(), 3));
		assertEquals(1, r.c.getNote(chVisible(BeMusicDevice.SWITCH11), 0, 0.0).getValue());
		assertEquals(2, r.c.getNote(chInvisible(BeMusicDevice.SWITCH11), 0, 0.0).getValue());
		assertEquals(1, r.c.getNote(chInvisible(BeMusicDevice.SWITCH12), 0, 48.0).getValue());
		assertEquals(3, r.c.getNote(chInvisible(BeMusicDevice.SWITCH13), 0, 96.0).getValue());
	}

	// key_channels：nameが未定義の時、SYNTAXが報告され、当該トラックは無視されること
	@Test
	public void test_KeyChannels_Name_Undefined() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.SYNTAX, r.e.get(0).getType());
		assertEquals(0, r.c.getIndexedMetaCount(BeMusicMeta.WAV.getName()));
		assertEquals(0, r.c.countNotes(n -> true));
	}

	// key_channels：nameが文字列に変換できない時、WRONG_DATAが報告され、当該トラックは無視されること
	@Test
	public void test_KeyChannels_Name_WrongDefinition() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.WRONG_DATA, r.e.get(0).getType());
		assertEquals(0, r.c.getIndexedMetaCount(BeMusicMeta.WAV.getName()));
		assertEquals(0, r.c.countNotes(n -> true));
	}

	// key_channels：notesが未定義の時、SYNTAXが報告され、当該トラックは無視されること。ただしnameに該当する#WAVxxは出力されること
	@Test
	public void test_KeyChannels_Notes_Undefined() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.SYNTAX, r.e.get(0).getType());
		assertEquals(1, r.c.getIndexedMetaCount(BeMusicMeta.WAV.getName()));
		assertEquals("a.wav", r.c.getIndexedMeta(BeMusicMeta.WAV.getName(), 1));
		assertEquals(0, r.c.countNotes(n -> true));
	}

	// key_channels：notesが空配列の時、当該トラックは#WAVxxのみが出力されること
	@Test
	public void test_KeyChannels_Notes_Empty() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals(1, r.c.getIndexedMetaCount(BeMusicMeta.WAV.getName()));
		assertEquals("a.wav", r.c.getIndexedMeta(BeMusicMeta.WAV.getName(), 1));
		assertEquals(0, r.c.countNotes(n -> true));
	}

	// key_channels：notesが配列でない時、WRONG_DATAが報告され、当該トラックは無視されること。ただしnameに該当する#WAVxxは出力されること
	@Test
	public void test_KeyChannels_Notes_WrongDefinition() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.WRONG_DATA, r.e.get(0).getType());
		assertEquals(1, r.c.getIndexedMetaCount(BeMusicMeta.WAV.getName()));
		assertEquals("a.wav", r.c.getIndexedMeta(BeMusicMeta.WAV.getName(), 1));
		assertEquals(0, r.c.countNotes(n -> true));
	}

	// key_channels：xが0の時、UNKNOWN_CHANNELが報告され、当該ノートは無視されること
	@Test
	public void test_KeyChannels_X_0() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.UNKNOWN_CHANNEL, r.e.get(0).getType());
		assertEquals(1, r.c.getIndexedMetaCount(BeMusicMeta.WAV.getName()));
		assertEquals("a.wav", r.c.getIndexedMeta(BeMusicMeta.WAV.getName(), 1));
		assertEquals(0, r.c.countNotes(n -> true));
	}

	// key_channels：xがnullの時、WRONG_DATAが報告され、当該ノートは無視されること
	@Test
	public void test_KeyChannels_X_Null() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.WRONG_DATA, r.e.get(0).getType());
		assertEquals(1, r.c.getIndexedMetaCount(BeMusicMeta.WAV.getName()));
		assertEquals("a.wav", r.c.getIndexedMeta(BeMusicMeta.WAV.getName(), 1));
		assertEquals(0, r.c.countNotes(n -> true));
	}

	// key_channels：xが1～8の時、1～7がSWITCH1x, 8がSCRATCH1へ出力されること
	@Test
	public void test_KeyChannels_X_Primary() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals(1, r.c.getIndexedMetaCount(BeMusicMeta.WAV.getName()));
		assertEquals("a.wav", r.c.getIndexedMeta(BeMusicMeta.WAV.getName(), 1));
		assertEquals(1, r.c.getNote(chInvisible(BeMusicDevice.SWITCH11), 0, 0.0).getValue());
		assertEquals(1, r.c.getNote(chInvisible(BeMusicDevice.SWITCH12), 0, 24.0).getValue());
		assertEquals(1, r.c.getNote(chInvisible(BeMusicDevice.SWITCH13), 0, 48.0).getValue());
		assertEquals(1, r.c.getNote(chInvisible(BeMusicDevice.SWITCH14), 0, 72.0).getValue());
		assertEquals(1, r.c.getNote(chInvisible(BeMusicDevice.SWITCH15), 0, 96.0).getValue());
		assertEquals(1, r.c.getNote(chInvisible(BeMusicDevice.SWITCH16), 0, 120.0).getValue());
		assertEquals(1, r.c.getNote(chInvisible(BeMusicDevice.SWITCH17), 0, 144.0).getValue());
		assertEquals(1, r.c.getNote(chInvisible(BeMusicDevice.SCRATCH1), 0, 168.0).getValue());
	}

	// key_channels：xが9～16の時、9～15がSWITCH2x, 16がSCRATCH2へ出力されること
	@Test
	public void test_KeyChannels_X_Secondary() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals(1, r.c.getIndexedMetaCount(BeMusicMeta.WAV.getName()));
		assertEquals("a.wav", r.c.getIndexedMeta(BeMusicMeta.WAV.getName(), 1));
		assertEquals(1, r.c.getNote(chInvisible(BeMusicDevice.SWITCH21), 0, 0.0).getValue());
		assertEquals(1, r.c.getNote(chInvisible(BeMusicDevice.SWITCH22), 0, 24.0).getValue());
		assertEquals(1, r.c.getNote(chInvisible(BeMusicDevice.SWITCH23), 0, 48.0).getValue());
		assertEquals(1, r.c.getNote(chInvisible(BeMusicDevice.SWITCH24), 0, 72.0).getValue());
		assertEquals(1, r.c.getNote(chInvisible(BeMusicDevice.SWITCH25), 0, 96.0).getValue());
		assertEquals(1, r.c.getNote(chInvisible(BeMusicDevice.SWITCH26), 0, 120.0).getValue());
		assertEquals(1, r.c.getNote(chInvisible(BeMusicDevice.SWITCH27), 0, 144.0).getValue());
		assertEquals(1, r.c.getNote(chInvisible(BeMusicDevice.SCRATCH2), 0, 168.0).getValue());
	}

	// key_channels：xが負の値の時、WRONG_DATAが報告され、当該ノートは無視されること
	@Test
	public void test_KeyChannels_X_MinusValue() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.WRONG_DATA, r.e.get(0).getType());
		assertEquals(1, r.c.getIndexedMetaCount(BeMusicMeta.WAV.getName()));
		assertEquals("a.wav", r.c.getIndexedMeta(BeMusicMeta.WAV.getName(), 1));
		assertEquals(0, r.c.countNotes(n -> true));
	}

	// key_channels：xが17以上の時、UNKNOWN_CHANNELが報告され、当該ノートは無視されること
	@Test
	public void test_KeyChannels_X_WrongValue() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.UNKNOWN_CHANNEL, r.e.get(0).getType());
		assertEquals(1, r.c.getIndexedMetaCount(BeMusicMeta.WAV.getName()));
		assertEquals("a.wav", r.c.getIndexedMeta(BeMusicMeta.WAV.getName(), 1));
		assertEquals(0, r.c.countNotes(n -> true));
	}

	// key_channels：xが数値に変換できない時、WRONG_DATAが報告され、当該ノートは無視されること
	@Test
	public void test_KeyChannels_X_WrongDefinition() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.WRONG_DATA, r.e.get(0).getType());
		assertEquals(1, r.c.getIndexedMetaCount(BeMusicMeta.WAV.getName()));
		assertEquals("a.wav", r.c.getIndexedMeta(BeMusicMeta.WAV.getName(), 1));
		assertEquals(0, r.c.countNotes(n -> true));
	}

	// key_channels：x,yが同じ位置の定義がある時、同一トラック・別トラックに関わらず後発の定義が優先されること
	@Test
	public void test_KeyChannels_XY_Same() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals(2, r.c.getIndexedMetaCount(BeMusicMeta.WAV.getName()));
		assertEquals("a.wav", r.c.getIndexedMeta(BeMusicMeta.WAV.getName(), 1));
		assertEquals("b.wav", r.c.getIndexedMeta(BeMusicMeta.WAV.getName(), 2));
		assertEquals(1, r.c.getNote(chInvisible(BeMusicDevice.SWITCH11), 0, 0.0).getValue());
		assertEquals(2, r.c.getNote(chInvisible(BeMusicDevice.SWITCH12), 0, 96.0).getValue());
	}

	// key_channels：yがない時、SYNTAXが報告され、当該ノートは無視されること
	@Test
	public void test_KeyChannels_Y_Undefined() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.SYNTAX, r.e.get(0).getType());
		assertEquals(1, r.c.getIndexedMetaCount(BeMusicMeta.WAV.getName()));
		assertEquals("a.wav", r.c.getIndexedMeta(BeMusicMeta.WAV.getName(), 1));
		assertEquals(0, r.c.countNotes(n -> true));
	}

	// key_channels：yが負の値の時、WRONG_DATAが報告され、当該ノートは無視されること
	@Test
	public void test_KeyChannels_Y_MinusValue() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.WRONG_DATA, r.e.get(0).getType());
		assertEquals(1, r.c.getIndexedMetaCount(BeMusicMeta.WAV.getName()));
		assertEquals("a.wav", r.c.getIndexedMeta(BeMusicMeta.WAV.getName(), 1));
		assertEquals(0, r.c.countNotes(n -> true));
	}

	// key_channels：yが数値に変換できない時、WRONG_DATAが報告され、当該ノートは無視されること
	@Test
	public void test_KeyChannels_Y_WrongDefinition() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.WRONG_DATA, r.e.get(0).getType());
		assertEquals(1, r.c.getIndexedMetaCount(BeMusicMeta.WAV.getName()));
		assertEquals("a.wav", r.c.getIndexedMeta(BeMusicMeta.WAV.getName(), 1));
		assertEquals(0, r.c.countNotes(n -> true));
	}

	// bga：未定義の時、何も行われないこと。また、bga_header, bga_events, layer_events, poor_eventsに対するエラーも報告されないこと
	@Test
	public void test_Bga_Undefined() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
	}

	// bga：JSONオブジェクト以外の時、SYNTAXが報告されること
	@Test
	public void test_Bga_WrongDefinition() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.SYNTAX, r.e.get(0).getType());
	}

	// bga_header：未定義の時、SYNTAXが報告されること
	@Test
	public void test_BgaHeader_Undefined() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.SYNTAX, r.e.get(0).getType());
	}

	// bga_header：配列でない時、SYNTAXが報告されること
	@Test
	public void test_BgaHeader_WrongDefinition() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.SYNTAX, r.e.get(0).getType());
	}

	// bga_header：空配列の時、何も行われないこと
	@Test
	public void test_BgaHeader_Empty() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals(0, r.c.getIndexedMetaCount(BeMusicMeta.BMP.getName()));
	}

	// bga_header：配列要素がJSONオブジェクト以外の時、SYNTAXが報告され、当該定義は無視されること
	@Test
	public void test_BgaHeader_HasWrongValue() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.SYNTAX, r.e.get(0).getType());
		assertEquals(1, r.c.getIndexedMetaCount(BeMusicMeta.BMP.getName()));
		assertEquals("a.bmp", r.c.getIndexedMeta(BeMusicMeta.BMP.getName(), 1));
	}

	// bga_header：配列要素のidの値がインデックス値、nameの値がファイル名となり、#BMPxxが出力されること（正常系）
	@Test
	public void test_BgaHeader_NormalCases() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals(3, r.c.getIndexedMetaCount(BeMusicMeta.BMP.getName()));
		assertEquals("a.bmp", r.c.getIndexedMeta(BeMusicMeta.BMP.getName(), 1));
		assertEquals("b.jpg", r.c.getIndexedMeta(BeMusicMeta.BMP.getName(), 5));
		assertEquals("bga.png", r.c.getIndexedMeta(BeMusicMeta.BMP.getName(), 20));
	}

	// bga_header：配列要素のidが同一の定義がある時、後発の定義が優先されること
	@Test
	public void test_BgaHeader_Id_HasSame() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals(2, r.c.getIndexedMetaCount(BeMusicMeta.BMP.getName()));
		assertEquals("c.png", r.c.getIndexedMeta(BeMusicMeta.BMP.getName(), 1));
		assertEquals("b.jpg", r.c.getIndexedMeta(BeMusicMeta.BMP.getName(), 2));
	}

	// bga_header：idがメタ情報インデックス値上限を超える時、WRONG_DATAが報告され、当該定義は無視されること
	@Test
	public void test_BgaHeader_Id_TooMany() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.WRONG_DATA, r.e.get(0).getType());
		assertEquals(BmsSpec.INDEXED_META_INDEX_MAX, r.c.getIndexedMetaCount(BeMusicMeta.BMP.getName()));
	}

	// bga_header：idが0の時、当該定義は#BMP00に設定されること
	@Test
	public void test_BgaHeader_Id_0() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals(2, r.c.getIndexedMetaCount(BeMusicMeta.BMP.getName()));
		assertEquals("b.bmp", r.c.getIndexedMeta(BeMusicMeta.BMP.getName(), 0));
		assertEquals("a.bmp", r.c.getIndexedMeta(BeMusicMeta.BMP.getName(), 1));
	}

	// bga_header：idが負の値の時、WRONG_DATAが報告され、当該定義は無視されること
	@Test
	public void test_BgaHeader_Id_MinusValue() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.WRONG_DATA, r.e.get(0).getType());
		assertEquals(1, r.c.getIndexedMetaCount(BeMusicMeta.BMP.getName()));
		assertEquals("a.bmp", r.c.getIndexedMeta(BeMusicMeta.BMP.getName(), 1));
	}

	// bga_header：idが数値に変換できない時、WRONG_DATAが報告され、当該定義は無視されること
	@Test
	public void test_BgaHeader_Id_WrongDefinition() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.WRONG_DATA, r.e.get(0).getType());
		assertEquals(1, r.c.getIndexedMetaCount(BeMusicMeta.BMP.getName()));
		assertEquals("a.bmp", r.c.getIndexedMeta(BeMusicMeta.BMP.getName(), 1));
	}

	// bga_header：nameが空文字列の時、WRONG_DATAが報告され、当該定義は無視されること
	@Test
	public void test_BgaHeader_Name_Empty() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.WRONG_DATA, r.e.get(0).getType());
		assertEquals(1, r.c.getIndexedMetaCount(BeMusicMeta.BMP.getName()));
		assertEquals("a.bmp", r.c.getIndexedMeta(BeMusicMeta.BMP.getName(), 1));
	}

	// bga_header：nameが文字列に変換できない時、WRONG_DATAが報告され、当該定義は無視されること
	@Test
	public void test_BgaHeader_Name_WrongDefinition() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.WRONG_DATA, r.e.get(0).getType());
		assertEquals(1, r.c.getIndexedMetaCount(BeMusicMeta.BMP.getName()));
		assertEquals("a.bmp", r.c.getIndexedMeta(BeMusicMeta.BMP.getName(), 1));
	}

	// bga_events：未定義の時、SYNTAXが報告されること
	@Test
	public void test_BgaEvents_Undefined() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.SYNTAX, r.e.get(0).getType());
	}

	// bga_events：配列でない時、SYNTAXが報告されること
	@Test
	public void test_BgaEvents_WrongDefinition() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.SYNTAX, r.e.get(0).getType());
	}

	// bga_events：空配列の時、何も行われないこと
	@Test
	public void test_BgaEvents_Empty() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
	}

	// bga_events：配列要素がJSONオブジェクト以外の時、SYNTAXが報告され、当該定義は無視されること
	@Test
	public void test_BgaEvents_HasWrongValue() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.SYNTAX, r.e.get(0).getType());
		assertEquals(1, r.c.getIndexedMetaCount(BeMusicMeta.BMP.getName()));
		assertEquals("a.bmp", r.c.getIndexedMeta(BeMusicMeta.BMP.getName(), 1));
		assertEquals(1, r.c.getNote(BeMusicChannel.BGA.getNumber(), 0, 0.0).getValue());
	}

	// bga_events：配列要素のyの位置に、idがノートの値となり、#xxx04が出力されること（正常系）
	@Test
	public void test_BgaEvents_NormalCases() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals(3, r.c.getIndexedMetaCount(BeMusicMeta.BMP.getName()));
		assertEquals("a.bmp", r.c.getIndexedMeta(BeMusicMeta.BMP.getName(), 1));
		assertEquals("b.bmp", r.c.getIndexedMeta(BeMusicMeta.BMP.getName(), 3));
		assertEquals("c.bmp", r.c.getIndexedMeta(BeMusicMeta.BMP.getName(), 5));
		assertEquals(1, r.c.getNote(BeMusicChannel.BGA.getNumber(), 0, 0.0).getValue());
		assertEquals(3, r.c.getNote(BeMusicChannel.BGA.getNumber(), 0, 96.0).getValue());
		assertEquals(5, r.c.getNote(BeMusicChannel.BGA.getNumber(), 2, 0.0).getValue());
		assertEquals(1, r.c.getNote(BeMusicChannel.BGA.getNumber(), 3, 0.0).getValue());
	}

	// bga_events：yが同じ位置の定義がある時、後発の定義が優先されること
	@Test
	public void test_BgaEvents_Y_HasSame() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals(2, r.c.getIndexedMetaCount(BeMusicMeta.BMP.getName()));
		assertEquals("a.bmp", r.c.getIndexedMeta(BeMusicMeta.BMP.getName(), 1));
		assertEquals("b.bmp", r.c.getIndexedMeta(BeMusicMeta.BMP.getName(), 2));
		assertEquals(2, r.c.getNote(BeMusicChannel.BGA.getNumber(), 0, 0.0).getValue());
	}

	// bga_events：yがない時、SYNTAXが報告され、当該ノートは無視されること
	@Test
	public void test_BgaEvents_Y_Undefined() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.SYNTAX, r.e.get(0).getType());
		assertEquals(1, r.c.getIndexedMetaCount(BeMusicMeta.BMP.getName()));
		assertEquals("a.bmp", r.c.getIndexedMeta(BeMusicMeta.BMP.getName(), 1));
		assertEquals(1, r.c.getNote(BeMusicChannel.BGA.getNumber(), 0, 0.0).getValue());
	}

	// bga_events：yが負の値の時、WRONG_DATAが報告され、当該ノートは無視されること
	@Test
	public void test_BgaEvents_Y_MinusValue() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.WRONG_DATA, r.e.get(0).getType());
		assertEquals(1, r.c.getIndexedMetaCount(BeMusicMeta.BMP.getName()));
		assertEquals("a.bmp", r.c.getIndexedMeta(BeMusicMeta.BMP.getName(), 1));
		assertEquals(1, r.c.getNote(BeMusicChannel.BGA.getNumber(), 0, 0.0).getValue());
	}

	// bga_events：yが数値に変換できない時、WRONG_DATAが報告され、当該ノートは無視されること
	@Test
	public void test_BgaEvents_Y_WrongDefinition() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.WRONG_DATA, r.e.get(0).getType());
		assertEquals(1, r.c.getIndexedMetaCount(BeMusicMeta.BMP.getName()));
		assertEquals("a.bmp", r.c.getIndexedMeta(BeMusicMeta.BMP.getName(), 1));
		assertEquals(1, r.c.getNote(BeMusicChannel.BGA.getNumber(), 0, 0.0).getValue());
	}

	// bga_events：idがない時、SYNTAXが報告され、当該定義は無視されること
	@Test
	public void test_BgaEvents_Id_Undefined() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.SYNTAX, r.e.get(0).getType());
		assertEquals(1, r.c.getIndexedMetaCount(BeMusicMeta.BMP.getName()));
		assertEquals("a.bmp", r.c.getIndexedMeta(BeMusicMeta.BMP.getName(), 1));
		assertEquals(1, r.c.getNote(BeMusicChannel.BGA.getNumber(), 0, 0.0).getValue());
		assertNull(r.c.getNote(BeMusicChannel.BGA.getNumber(), 0, 96.0));
	}

	// bga_events：idが0の時、WRONG_DATAが報告され、当該定義は無視されること
	@Test
	public void test_BgaEvents_Id_0() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.WRONG_DATA, r.e.get(0).getType());
		assertEquals(1, r.c.getIndexedMetaCount(BeMusicMeta.BMP.getName()));
		assertEquals("a.bmp", r.c.getIndexedMeta(BeMusicMeta.BMP.getName(), 1));
		assertEquals(1, r.c.getNote(BeMusicChannel.BGA.getNumber(), 0, 0.0).getValue());
		assertNull(r.c.getNote(BeMusicChannel.BGA.getNumber(), 0, 96.0));
	}

	// bga_events：idが負の値の時、WRONG_DATAが報告され、当該定義は無視されること
	@Test
	public void test_BgaEvents_Id_MinusValue() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.WRONG_DATA, r.e.get(0).getType());
		assertEquals(1, r.c.getIndexedMetaCount(BeMusicMeta.BMP.getName()));
		assertEquals("a.bmp", r.c.getIndexedMeta(BeMusicMeta.BMP.getName(), 1));
		assertEquals(1, r.c.getNote(BeMusicChannel.BGA.getNumber(), 0, 0.0).getValue());
		assertNull(r.c.getNote(BeMusicChannel.BGA.getNumber(), 0, 96.0));
	}

	// bga_events：idが数値に変換できない時、WRONG_DATAが報告され、当該定義は無視されること
	@Test
	public void test_BgaEvents_Id_WrongDefinition() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.WRONG_DATA, r.e.get(0).getType());
		assertEquals(1, r.c.getIndexedMetaCount(BeMusicMeta.BMP.getName()));
		assertEquals("a.bmp", r.c.getIndexedMeta(BeMusicMeta.BMP.getName(), 1));
		assertEquals(1, r.c.getNote(BeMusicChannel.BGA.getNumber(), 0, 0.0).getValue());
		assertNull(r.c.getNote(BeMusicChannel.BGA.getNumber(), 0, 96.0));
	}

	// layer_events：未定義の時、SYNTAXが報告されること
	@Test
	public void test_LayerEvents_Undefined() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.SYNTAX, r.e.get(0).getType());
	}

	// layer_events：配列でない時、SYNTAXが報告されること
	@Test
	public void test_LayerEvents_WrongDefinition() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.SYNTAX, r.e.get(0).getType());
	}

	// layer_events：空配列の時、何も行われないこと
	@Test
	public void test_LayerEvents_Empty() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
	}

	// layer_events：配列要素がJSONオブジェクト以外の時、SYNTAXが報告され、当該定義は無視されること
	@Test
	public void test_LayerEvents_HasWrongValue() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.SYNTAX, r.e.get(0).getType());
		assertEquals(1, r.c.getIndexedMetaCount(BeMusicMeta.BMP.getName()));
		assertEquals("a.bmp", r.c.getIndexedMeta(BeMusicMeta.BMP.getName(), 1));
		assertEquals(1, r.c.getNote(BeMusicChannel.BGA_LAYER.getNumber(), 0, 0.0).getValue());
	}

	// layer_events：配列要素のyの位置に、idがノートの値となり、#xxx07が出力されること（正常系）
	@Test
	public void test_LayerEvents_NormalCases() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals(3, r.c.getIndexedMetaCount(BeMusicMeta.BMP.getName()));
		assertEquals("a.bmp", r.c.getIndexedMeta(BeMusicMeta.BMP.getName(), 1));
		assertEquals("b.bmp", r.c.getIndexedMeta(BeMusicMeta.BMP.getName(), 3));
		assertEquals("c.bmp", r.c.getIndexedMeta(BeMusicMeta.BMP.getName(), 5));
		assertEquals(1, r.c.getNote(BeMusicChannel.BGA_LAYER.getNumber(), 0, 0.0).getValue());
		assertEquals(3, r.c.getNote(BeMusicChannel.BGA_LAYER.getNumber(), 0, 96.0).getValue());
		assertEquals(5, r.c.getNote(BeMusicChannel.BGA_LAYER.getNumber(), 2, 0.0).getValue());
		assertEquals(1, r.c.getNote(BeMusicChannel.BGA_LAYER.getNumber(), 3, 0.0).getValue());
	}

	// layer_events：yが同じ位置の定義がある時、後発の定義が優先されること
	@Test
	public void test_LayerEvents_Y_HasSame() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals(1, r.c.getIndexedMetaCount(BeMusicMeta.BMP.getName()));
		assertEquals("a.bmp", r.c.getIndexedMeta(BeMusicMeta.BMP.getName(), 1));
		assertEquals(2, r.c.getNote(BeMusicChannel.BGA_LAYER.getNumber(), 0, 0.0).getValue());
	}

	// layer_events：yがない時、SYNTAXが報告され、当該ノートは無視されること
	@Test
	public void test_LayerEvents_Y_Undefined() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.SYNTAX, r.e.get(0).getType());
		assertEquals(1, r.c.getIndexedMetaCount(BeMusicMeta.BMP.getName()));
		assertEquals("a.bmp", r.c.getIndexedMeta(BeMusicMeta.BMP.getName(), 1));
		assertEquals(1, r.c.getNote(BeMusicChannel.BGA_LAYER.getNumber(), 0, 0.0).getValue());
	}

	// layer_events：yが負の値の時、WRONG_DATAが報告され、当該ノートは無視されること
	@Test
	public void test_LayerEvents_Y_MinusValue() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.WRONG_DATA, r.e.get(0).getType());
		assertEquals(1, r.c.getIndexedMetaCount(BeMusicMeta.BMP.getName()));
		assertEquals("a.bmp", r.c.getIndexedMeta(BeMusicMeta.BMP.getName(), 1));
		assertEquals(1, r.c.getNote(BeMusicChannel.BGA_LAYER.getNumber(), 0, 0.0).getValue());
	}

	// layer_events：yが数値に変換できない時、WRONG_DATAが報告され、当該ノートは無視されること
	@Test
	public void test_LayerEvents_Y_WrongDefinition() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.WRONG_DATA, r.e.get(0).getType());
		assertEquals(1, r.c.getIndexedMetaCount(BeMusicMeta.BMP.getName()));
		assertEquals("a.bmp", r.c.getIndexedMeta(BeMusicMeta.BMP.getName(), 1));
		assertEquals(1, r.c.getNote(BeMusicChannel.BGA_LAYER.getNumber(), 0, 0.0).getValue());
	}

	// layer_events：idがない時、SYNTAXが報告され、当該定義は無視されること
	@Test
	public void test_LayerEvents_Id_Undefined() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.SYNTAX, r.e.get(0).getType());
		assertEquals(1, r.c.getIndexedMetaCount(BeMusicMeta.BMP.getName()));
		assertEquals("a.bmp", r.c.getIndexedMeta(BeMusicMeta.BMP.getName(), 1));
		assertEquals(1, r.c.getNote(BeMusicChannel.BGA_LAYER.getNumber(), 0, 0.0).getValue());
		assertNull(r.c.getNote(BeMusicChannel.BGA_LAYER.getNumber(), 0, 96.0));
	}

	// layer_events：idが0の時、WRONG_DATAが報告され、当該定義は無視されること
	@Test
	public void test_LayerEvents_Id_0() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.WRONG_DATA, r.e.get(0).getType());
		assertEquals(1, r.c.getIndexedMetaCount(BeMusicMeta.BMP.getName()));
		assertEquals("a.bmp", r.c.getIndexedMeta(BeMusicMeta.BMP.getName(), 1));
		assertEquals(1, r.c.getNote(BeMusicChannel.BGA_LAYER.getNumber(), 0, 0.0).getValue());
		assertNull(r.c.getNote(BeMusicChannel.BGA_LAYER.getNumber(), 0, 96.0));
	}

	// layer_events：idが負の値の時、WRONG_DATAが報告され、当該定義は無視されること
	@Test
	public void test_LayerEvents_Id_MinusValue() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.WRONG_DATA, r.e.get(0).getType());
		assertEquals(1, r.c.getIndexedMetaCount(BeMusicMeta.BMP.getName()));
		assertEquals("a.bmp", r.c.getIndexedMeta(BeMusicMeta.BMP.getName(), 1));
		assertEquals(1, r.c.getNote(BeMusicChannel.BGA_LAYER.getNumber(), 0, 0.0).getValue());
		assertNull(r.c.getNote(BeMusicChannel.BGA_LAYER.getNumber(), 0, 96.0));
	}

	// layer_events：idが数値に変換できない時、WRONG_DATAが報告され、当該定義は無視されること
	@Test
	public void test_LayerEvents_Id_WrongDefinition() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.WRONG_DATA, r.e.get(0).getType());
		assertEquals(1, r.c.getIndexedMetaCount(BeMusicMeta.BMP.getName()));
		assertEquals("a.bmp", r.c.getIndexedMeta(BeMusicMeta.BMP.getName(), 1));
		assertEquals(1, r.c.getNote(BeMusicChannel.BGA_LAYER.getNumber(), 0, 0.0).getValue());
		assertNull(r.c.getNote(BeMusicChannel.BGA_LAYER.getNumber(), 0, 96.0));
	}

	// poor_events：未定義の時、SYNTAXが報告されること
	@Test
	public void test_PoorEvents_Undefined() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.SYNTAX, r.e.get(0).getType());
	}

	// poor_events：配列でない時、SYNTAXが報告されること
	@Test
	public void test_PoorEvents_WrongDefinition() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.SYNTAX, r.e.get(0).getType());
	}

	// poor_events：空配列の時、何も行われないこと
	@Test
	public void test_PoorEvents_Empty() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
	}

	// poor_events：配列要素がJSONオブジェクト以外の時、SYNTAXが報告され、当該定義は無視されること
	@Test
	public void test_PoorEvents_HasWrongValue() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.SYNTAX, r.e.get(0).getType());
		assertEquals(1, r.c.getIndexedMetaCount(BeMusicMeta.BMP.getName()));
		assertEquals("a.bmp", r.c.getIndexedMeta(BeMusicMeta.BMP.getName(), 1));
		assertEquals(1, r.c.getNote(BeMusicChannel.BGA_MISS.getNumber(), 0, 0.0).getValue());
	}

	// poor_events：配列要素のyの位置に、idがノートの値となり、#xxx06が出力されること（正常系）
	@Test
	public void test_PoorEvents_NormalCases() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals(3, r.c.getIndexedMetaCount(BeMusicMeta.BMP.getName()));
		assertEquals("a.bmp", r.c.getIndexedMeta(BeMusicMeta.BMP.getName(), 1));
		assertEquals("b.bmp", r.c.getIndexedMeta(BeMusicMeta.BMP.getName(), 3));
		assertEquals("c.bmp", r.c.getIndexedMeta(BeMusicMeta.BMP.getName(), 5));
		assertEquals(1, r.c.getNote(BeMusicChannel.BGA_MISS.getNumber(), 0, 0.0).getValue());
		assertEquals(3, r.c.getNote(BeMusicChannel.BGA_MISS.getNumber(), 0, 96.0).getValue());
		assertEquals(5, r.c.getNote(BeMusicChannel.BGA_MISS.getNumber(), 2, 0.0).getValue());
		assertEquals(1, r.c.getNote(BeMusicChannel.BGA_MISS.getNumber(), 3, 0.0).getValue());
	}

	// poor_events：yが同じ位置の定義がある時、後発の定義が優先されること
	@Test
	public void test_PoorEvents_Y_HasSame() throws Exception {
		var r = content();
		assertEquals(true, r.e.isEmpty());
		assertEquals(1, r.c.getIndexedMetaCount(BeMusicMeta.BMP.getName()));
		assertEquals("a.bmp", r.c.getIndexedMeta(BeMusicMeta.BMP.getName(), 1));
		assertEquals(2, r.c.getNote(BeMusicChannel.BGA_MISS.getNumber(), 0, 0.0).getValue());
	}

	// poor_events：yがない時、SYNTAXが報告され、当該ノートは無視されること
	@Test
	public void test_PoorEvents_Y_Undefined() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.SYNTAX, r.e.get(0).getType());
		assertEquals(1, r.c.getIndexedMetaCount(BeMusicMeta.BMP.getName()));
		assertEquals("a.bmp", r.c.getIndexedMeta(BeMusicMeta.BMP.getName(), 1));
		assertEquals(1, r.c.getNote(BeMusicChannel.BGA_MISS.getNumber(), 0, 0.0).getValue());
	}

	// poor_events：yが負の値の時、WRONG_DATAが報告され、当該ノートは無視されること
	@Test
	public void test_PoorEvents_Y_MinusValue() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.WRONG_DATA, r.e.get(0).getType());
		assertEquals(1, r.c.getIndexedMetaCount(BeMusicMeta.BMP.getName()));
		assertEquals("a.bmp", r.c.getIndexedMeta(BeMusicMeta.BMP.getName(), 1));
		assertEquals(1, r.c.getNote(BeMusicChannel.BGA_MISS.getNumber(), 0, 0.0).getValue());
	}

	// poor_events：yが数値に変換できない時、WRONG_DATAが報告され、当該ノートは無視されること
	@Test
	public void test_PoorEvents_Y_WrongDefinition() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.WRONG_DATA, r.e.get(0).getType());
		assertEquals(1, r.c.getIndexedMetaCount(BeMusicMeta.BMP.getName()));
		assertEquals("a.bmp", r.c.getIndexedMeta(BeMusicMeta.BMP.getName(), 1));
		assertEquals(1, r.c.getNote(BeMusicChannel.BGA_MISS.getNumber(), 0, 0.0).getValue());
	}

	// poor_events：idがない時、SYNTAXが報告され、当該定義は無視されること
	@Test
	public void test_PoorEvents_Id_Undefined() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.SYNTAX, r.e.get(0).getType());
		assertEquals(1, r.c.getIndexedMetaCount(BeMusicMeta.BMP.getName()));
		assertEquals("a.bmp", r.c.getIndexedMeta(BeMusicMeta.BMP.getName(), 1));
		assertEquals(1, r.c.getNote(BeMusicChannel.BGA_MISS.getNumber(), 0, 0.0).getValue());
		assertNull(r.c.getNote(BeMusicChannel.BGA_MISS.getNumber(), 0, 96.0));
	}

	// poor_events：idが0の時、WRONG_DATAが報告され、当該定義は無視されること
	@Test
	public void test_PoorEvents_Id_0() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.WRONG_DATA, r.e.get(0).getType());
		assertEquals(1, r.c.getIndexedMetaCount(BeMusicMeta.BMP.getName()));
		assertEquals("a.bmp", r.c.getIndexedMeta(BeMusicMeta.BMP.getName(), 1));
		assertEquals(1, r.c.getNote(BeMusicChannel.BGA_MISS.getNumber(), 0, 0.0).getValue());
		assertNull(r.c.getNote(BeMusicChannel.BGA_MISS.getNumber(), 0, 96.0));
	}

	// poor_events：idが負の値の時、WRONG_DATAが報告され、当該定義は無視されること
	@Test
	public void test_PoorEvents_Id_MinusValue() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.WRONG_DATA, r.e.get(0).getType());
		assertEquals(1, r.c.getIndexedMetaCount(BeMusicMeta.BMP.getName()));
		assertEquals("a.bmp", r.c.getIndexedMeta(BeMusicMeta.BMP.getName(), 1));
		assertEquals(1, r.c.getNote(BeMusicChannel.BGA_MISS.getNumber(), 0, 0.0).getValue());
		assertNull(r.c.getNote(BeMusicChannel.BGA_MISS.getNumber(), 0, 96.0));
	}

	// poor_events：idが数値に変換できない時、WRONG_DATAが報告され、当該定義は無視されること
	@Test
	public void test_PoorEvents_Id_WrongDefinition() throws Exception {
		var r = content();
		assertEquals(1, r.e.size());
		assertEquals(BmsErrorType.WRONG_DATA, r.e.get(0).getType());
		assertEquals(1, r.c.getIndexedMetaCount(BeMusicMeta.BMP.getName()));
		assertEquals("a.bmp", r.c.getIndexedMeta(BeMusicMeta.BMP.getName(), 1));
		assertEquals(1, r.c.getNote(BeMusicChannel.BGA_MISS.getNumber(), 0, 0.0).getValue());
		assertNull(r.c.getNote(BeMusicChannel.BGA_MISS.getNumber(), 0, 96.0));
	}

	private static int chVisible(BeMusicDevice dev) {
		return dev.getVisibleChannel().getNumber();
	}

	private static int chInvisible(BeMusicDevice dev) {
		return dev.getInvisibleChannel().getNumber();
	}

	private static int chLong(BeMusicDevice dev) {
		return dev.getLongChannel().getNumber();
	}

	private static int chMine(BeMusicDevice dev) {
		return dev.getMineChannel().getNumber();
	}
	private LoadResult content() throws Exception {
		return content(testDataPath(1));
	}

	private LoadResult content(Path path) throws Exception {
		return content(path, l -> {});
	}

	private LoadResult content(Consumer<BeMusicBmsonLoader> ld) throws Exception {
		return content(testDataPath(1), ld);
	}

	private LoadResult content(Path path, Consumer<BeMusicBmsonLoader> ld) throws Exception {
		var handler = new LoadHandler();
		var loader = loader(handler, true);
		ld.accept(loader);
		var content = loader.load(path);
		var result = new LoadResult();
		result.e = handler.errors;
		result.c = content;
		return result;
	}

	private static BeMusicBmsonLoader loader(LoadHandler handler, boolean strictly) {
		var loader = (BeMusicBmsonLoader)new BeMusicBmsonLoader()
				.setSpec(BeMusicSpec.LATEST)
				.setHandler(handler)
				.setStrictly(strictly);
		return loader;
	}

	private static class LoadHandler extends BeMusicLoadHandler {
		List<BmsScriptError> errors = new ArrayList<>();

		@Override
		public boolean parseError(BmsScriptError error) {
			errors.add(error);
			return true;
		}
	}

	private static class LoadResult {
		List<BmsScriptError> e = new ArrayList<>();
		BmsContent c;
	}
}
