package com.lmt.lib.bms;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class BmsStandardSaverTest {
	private List<Charset> mCharsets;

	private static class IOExceptionOutputStream extends OutputStream {
		@Override public void write(int b) throws IOException { throw new IOException(); }
	}

	private static class RuntimeExceptionOutputStream extends OutputStream {
		@Override public void write(int b) throws IOException { throw new RuntimeException(); }
	}

	@Before
	public void setUp() throws Exception {
		mCharsets = BmsLibrary.getDefaultCharsets();
	}

	@After
	public void tearDown() throws Exception {
		BmsLibrary.setDefaultCharsets(mCharsets.toArray(Charset[]::new));
	}

	// getLastProcessedCharset()
	// 初期状態ではnullを返すこと
	@Test
	public void testGetLastProcessedCharset_Initial() throws Exception {
		var s = new BmsStandardSaver();
		assertNull(s.getLastProcessedCharset());
	}

	// getLastProcessedCharset()
	// 優先文字セットが使用された場合は優先文字セットを返すこと
	@Test
	public void testGetLastProcessedCharset_PrimaryCharset() throws Exception {
		var content = content(c -> c.setSingleMeta("#title", "曲名"));
		var cs1 = Charset.forName("MS932");
		var cs2 = StandardCharsets.UTF_8;
		var saver = new BmsStandardSaver().setCharsets(cs1, cs2);
		var textBin = write(content, saver);
		assertOnWrite(textBin, 0, cs1, "#TITLE 曲名");
		assertEquals(cs1, saver.getLastProcessedCharset());
	}

	// getLastProcessedCharset()
	// 2番目の文字セットが使用された場合は2番目の文字セットを返すこと
	@Test
	public void testGetLastProcessedCharset_SecondaryCharset() throws Exception {
		var content = content(c -> c.setSingleMeta("#title", "곡명"));
		var cs1 = Charset.forName("MS932");
		var cs2 = StandardCharsets.UTF_8;
		var saver = new BmsStandardSaver().setCharsets(cs1, cs2);
		var textBin = write(content, saver);
		assertOnWrite(textBin, 0, cs2, "#TITLE 곡명");
		assertEquals(cs2, saver.getLastProcessedCharset());
	}

	// getLastProcessedCharset()
	// デフォルト優先文字セットが使用された場合はデフォルト優先文字セットを返すこと
	@Test
	public void testGetLastProcessedCharset_DefaultPrimaryCharset() throws Exception {
		var content = content(c -> c.setSingleMeta("#title", "曲名"));
		var cs1 = Charset.forName("MS932");
		var cs2 = StandardCharsets.UTF_8;
		var saver = new BmsStandardSaver();
		BmsLibrary.setDefaultCharsets(cs1, cs2);
		var textBin = write(content, saver);
		assertOnWrite(textBin, 0, cs1, "#TITLE 曲名");
		assertEquals(cs1, saver.getLastProcessedCharset());
	}

	// getLastProcessedCharset()
	// デフォルトの2番目の文字セットが使用された場合はデフォルトの2番目の文字セットを返すこと
	@Test
	public void testGetLastProcessedCharset_DefaultSecondaryCharset() throws Exception {
		var content = content(c -> c.setSingleMeta("#title", "곡명"));
		var cs1 = Charset.forName("MS932");
		var cs2 = StandardCharsets.UTF_8;
		var saver = new BmsStandardSaver();
		BmsLibrary.setDefaultCharsets(cs1, cs2);
		var textBin = write(content, saver);
		assertOnWrite(textBin, 0, cs2, "#TITLE 곡명");
		assertEquals(cs2, saver.getLastProcessedCharset());
	}

	// getLastProcessedCharset()
	// 例外がスローされた場合は文字セットが更新されないこと
	@Test
	public void testGetLastProcessedCharset_ThrownException() throws Exception {
		var content = content(c -> c.setSingleMeta("#title", "곡명"));
		var csOrg = StandardCharsets.UTF_8;
		var cs1 = Charset.forName("MS932");
		var cs2 = Charset.forName("GB2312");
		var saver = new BmsStandardSaver().setCharsets(cs1, cs2);
		Tests.setf(saver, "mLastProcessedCharset", csOrg);
		assertThrows(BmsCompatException.class, () -> write(content, saver));
		assertEquals(csOrg, saver.getLastProcessedCharset());
	}

	// setMaxPrecision(int)
	// 指定した値が正しく設定されること
	@Test
	public void testSetMaxPrecision_Normal() throws Exception {
		var s1 = new BmsStandardSaver();
		var s2 = s1.setMaxPrecision(BmsStandardSaver.PRECISION_MIN);
		assertSame(s1, s2);
		assertEquals(BmsStandardSaver.PRECISION_MIN, (int)Tests.getf(s1, "mMaxPrecision"));
		s2 = s1.setMaxPrecision(BmsStandardSaver.PRECISION_MAX);
		assertSame(s1, s2);
		assertEquals(BmsStandardSaver.PRECISION_MAX, (int)Tests.getf(s1, "mMaxPrecision"));
	}

	// setMaxPrecision(int)
	// IllegalArgumentException maxPrecisionがPRECISION_MIN未満
	@Test
	public void testSetMaxPrecision_Underflow() {
		var s = new BmsStandardSaver();
		assertThrows(IllegalArgumentException.class, () -> s.setMaxPrecision(BmsStandardSaver.PRECISION_MIN - 1));
	}

	// setMaxPrecision(int)
	// IllegalArgumentException maxPrecisionがPRECISION_MAX超過
	@Test
	public void testSetMaxPrecision_Overflow() {
		var s = new BmsStandardSaver();
		assertThrows(IllegalArgumentException.class, () -> s.setMaxPrecision(BmsStandardSaver.PRECISION_MAX + 1));
	}

	// setMetaComments(Collection<String>)
	// 指定したコメントリストが正しく設定されること
	@Test
	@SuppressWarnings("unchecked")
	public void testSetMetaComments_Normal() throws Exception {
		var comments = List.of("Michael", "Ray", "Cate", "Kyle");
		var s1 = new BmsStandardSaver();
		var s2 = s1.setMetaComments(comments);
		assertSame(s1, s2);
		assertEquals(comments, (List<String>)Tests.getf(s1, "mMetaComments"));
	}

	// setMetaComments(Collection<String>)
	// コメント0件も設定できること
	@Test
	@SuppressWarnings("unchecked")
	public void testSetMetaComments_NoComment() throws Exception {
		var s1 = new BmsStandardSaver();
		var s2 = s1.setMetaComments(Collections.emptyList());
		assertSame(s1, s2);
		assertTrue(((List<String>)Tests.getf(s1, "mMetaComments")).isEmpty());
	}

	// setMetaComments(Collection<String>)
	// コメント内のCR/LFは空白に変換されること
	@Test
	@SuppressWarnings("unchecked")
	public void testSetMetaComments_ReplaceNewLine() throws Exception {
		var s1 = new BmsStandardSaver();
		var s2 = s1.setMetaComments(List.of("Hoge\rHage", "Foo\nBar", "Line1\r\nLine2"));
		assertSame(s1, s2);
		assertEquals(List.of("Hoge Hage", "Foo Bar", "Line1  Line2"), (List<String>)Tests.getf(s1, "mMetaComments"));
	}

	// setMetaComments(Collection<String>)
	// NullPointerException metaCommentsがnull
	@Test
	public void testSetMetaComments_NullComments() {
		var s = new BmsStandardSaver();
		assertThrows(NullPointerException.class, () -> s.setMetaComments(null));
	}

	// setChannelComments(Collection<String>)
	// NullPointerException metaCommentsの中にnullが含まれる
	@Test
	public void testSetMetaComments_HasNull() {
		var s = new BmsStandardSaver();
		assertThrows(NullPointerException.class, () -> s.setMetaComments(List.of("Hoge", null)));
		assertThrows(NullPointerException.class, () -> s.setMetaComments(List.of(null, "Hoge")));
	}

	// setMetaComments(Collection<String>)
	// 指定したコメントリストが正しく設定されること
	@Test
	@SuppressWarnings("unchecked")
	public void testSetChannelComments_Normal() throws Exception {
		var comments = List.of("Michael", "Ray", "Cate", "Kyle");
		var s1 = new BmsStandardSaver();
		var s2 = s1.setChannelComments(comments);
		assertSame(s1, s2);
		assertEquals(comments, (List<String>)Tests.getf(s1, "mChannelComments"));
	}

	// setChannelComments(Collection<String>)
	// コメント0件も設定できること
	@Test
	@SuppressWarnings("unchecked")
	public void testSetChannelComments_NoComment() throws Exception {
		var s1 = new BmsStandardSaver();
		var s2 = s1.setChannelComments(Collections.emptyList());
		assertSame(s1, s2);
		assertTrue(((List<String>)Tests.getf(s1, "mChannelComments")).isEmpty());
	}

	// setChannelComments(Collection<String>)
	// コメント内のCR/LFは空白に変換されること
	@Test
	@SuppressWarnings("unchecked")
	public void testSetChannelComments_ReplaceNewLine() throws Exception {
		var s1 = new BmsStandardSaver();
		var s2 = s1.setChannelComments(List.of("Hoge\rHage", "Foo\nBar", "Line1\r\nLine2"));
		assertSame(s1, s2);
		assertEquals(List.of("Hoge Hage", "Foo Bar", "Line1  Line2"), (List<String>)Tests.getf(s1, "mChannelComments"));
	}

	// setMetaComments(Collection<String>)
	// NullPointerException channelCommentsがnull
	@Test
	public void testSetChannelComments_NullComments() {
		var s = new BmsStandardSaver();
		assertThrows(NullPointerException.class, () -> s.setChannelComments(null));
	}

	// setChannelComments(Collection<String>)
	// NullPointerException channelCommentsの中にnullが含まれる
	@Test
	public void testSetChannelComments_HasNull() {
		var s = new BmsStandardSaver();
		assertThrows(NullPointerException.class, () -> s.setChannelComments(List.of("Hoge", null)));
		assertThrows(NullPointerException.class, () -> s.setChannelComments(List.of(null, "Hoge")));
	}

	// setFooterComments(Collection<String>)
	// 指定したコメントリストが正しく設定されること
	@Test
	@SuppressWarnings("unchecked")
	public void testSetFooterComments_Normal() throws Exception {
		var comments = List.of("Michael", "Ray", "Cate", "Kyle");
		var s1 = new BmsStandardSaver();
		var s2 = s1.setFooterComments(comments);
		assertSame(s1, s2);
		assertEquals(comments, (List<String>)Tests.getf(s1, "mFooterComments"));
	}

	// setFooterComments(Collection<String>)
	// コメント0件も設定できること
	@Test
	@SuppressWarnings("unchecked")
	public void testSetFooterComments_NoComment() throws Exception {
		var s1 = new BmsStandardSaver();
		var s2 = s1.setFooterComments(Collections.emptyList());
		assertSame(s1, s2);
		assertTrue(((List<String>)Tests.getf(s1, "mFooterComments")).isEmpty());
	}

	// setFooterComments(Collection<String>)
	// コメント内のCR/LFは空白に変換されること
	@Test
	@SuppressWarnings("unchecked")
	public void testSetFooterComments_ReplaceNewLine() throws Exception {
		var s1 = new BmsStandardSaver();
		var s2 = s1.setFooterComments(List.of("Hoge\rHage", "Foo\nBar", "Line1\r\nLine2"));
		assertSame(s1, s2);
		assertEquals(List.of("Hoge Hage", "Foo Bar", "Line1  Line2"), (List<String>)Tests.getf(s1, "mFooterComments"));
	}

	// setMetaComments(Collection<String>)
	// NullPointerException footerCommentsがnull
	@Test
	public void testSetFooterComments_NullComments() {
		var s = new BmsStandardSaver();
		assertThrows(NullPointerException.class, () -> s.setFooterComments(null));
	}

	// setChannelComments(Collection<String>)
	// NullPointerException footerCommentsの中にnullが含まれる
	@Test
	public void testSetFooterComments_HasNull() {
		var s = new BmsStandardSaver();
		assertThrows(NullPointerException.class, () -> s.setFooterComments(List.of("Hoge", null)));
		assertThrows(NullPointerException.class, () -> s.setFooterComments(List.of(null, "Hoge")));
	}

	// setCharsets(Charset...)
	// 複数の文字セットが指定順に登録されること
	@Test
	@SuppressWarnings("unchecked")
	public void testSetCharsets_AddableMultiple() throws Exception {
		var s1 = new BmsStandardSaver();
		var s2 = s1.setCharsets(
				StandardCharsets.US_ASCII,
				StandardCharsets.UTF_16LE,
				StandardCharsets.UTF_8);
		var cs = (List<Charset>)Tests.getf(s1, "mCharsets");
		assertSame(s1, s2);
		assertEquals(3, cs.size());
		assertEquals(StandardCharsets.US_ASCII, cs.get(0));
		assertEquals(StandardCharsets.UTF_16LE, cs.get(1));
		assertEquals(StandardCharsets.UTF_8, cs.get(2));
	}

	// setCharsets(Charset...)
	// 同一の文字セットが含まれると、後方の同一文字セットが無視されて登録されること
	@Test
	@SuppressWarnings("unchecked")
	public void testSetCharsets_RemoveSame() throws Exception {
		var s1 = new BmsStandardSaver();
		var s2 = s1.setCharsets(
				StandardCharsets.US_ASCII,
				StandardCharsets.UTF_16LE,
				StandardCharsets.US_ASCII,
				StandardCharsets.UTF_16LE,
				StandardCharsets.UTF_8,
				StandardCharsets.UTF_8,
				StandardCharsets.UTF_16LE);
		var cs = (List<Charset>)Tests.getf(s1, "mCharsets");
		assertSame(s1, s2);
		assertEquals(3, cs.size());
		assertEquals(StandardCharsets.US_ASCII, cs.get(0));
		assertEquals(StandardCharsets.UTF_16LE, cs.get(1));
		assertEquals(StandardCharsets.UTF_8, cs.get(2));
	}

	// setCharsets(Charset...)
	// 文字セット0件で登録できること
	@Test
	@SuppressWarnings("unchecked")
	public void testSetCharsets_NoCharset() throws Exception {
		var s1 = new BmsStandardSaver();
		var s2 = s1.setCharsets();
		var cs = (List<Charset>)Tests.getf(s1, "mCharsets");
		assertSame(s1, s2);
		assertEquals(0, cs.size());
	}

	// setCharsets(Charset...)
	// NullPointerException charsetsにnullが含まれている
	@Test
	public void testSetCharsets_HasNull() throws Exception {
		var s = new BmsStandardSaver();
		assertThrows(NullPointerException.class, () -> s.setCharsets(StandardCharsets.UTF_16BE, null));
		assertThrows(NullPointerException.class, () -> s.setCharsets(null, StandardCharsets.UTF_16BE));
	}

	// setAddBom(boolean)
	// 設定した値が記録されていること
	@Test
	public void testSetAddBom_Normal() throws Exception {
		var s = new BmsStandardSaver();
		assertFalse((boolean)Tests.getf(s, "mAddBom"));
	}

	// setAddBom(boolean)
	// デフォルトでは無効(false)になっていること
	@Test
	public void testSetAddBom_Default() throws Exception {
		var s = new BmsStandardSaver();
		assertSame(s.setAddBom(true), s);
		assertTrue((boolean)Tests.getf(s, "mAddBom"));
		assertSame(s.setAddBom(false), s);
		assertFalse((boolean)Tests.getf(s, "mAddBom"));
	}

	// onWrite(BmsContent, OutputStream)
	// 単純正常系：BMS宣言が正しく出力されること
	@Test
	public void testOnWrite_Normal_Declaration() throws Exception {
		var content = content(c -> {
			c.putDeclaration("rule", "BM");
			c.putDeclaration("name", "J-SON3");
			c.putDeclaration("value", "\\\"150\\\"");
		});
		var textBin = write(content, new BmsStandardSaver());
		assertOnWrite(textBin,
				";?bms rule=\"BM\" name=\"J-SON3\" value=\"\\\"150\\\"\"");
	}

	// onWrite(BmsContent, OutputStream)
	// 単純正常系：メタ情報が正しく出力されること(コメントなし)
	@Test
	public void testOnWrite_Normal_Meta() throws Exception {
		var content = content(c -> {
			c.setSingleMeta("#title", "MySong");
			c.setSingleMeta("#genre", "EUROBEAT");
			c.setSingleMeta("#bpm", 155.0);
			c.setSingleMeta("%url", "http://www.lm-t.com/");
		});
		var textBin = write(content, new BmsStandardSaver());
		assertOnWrite(textBin,
				"#GENRE EUROBEAT",
				"#TITLE MySong",
				"#BPM 155",
				"%URL http://www.lm-t.com/");
	}

	// onWrite(BmsContent, OutputStream)
	// 単純正常系：メタ情報が正しく出力されること(コメントあり)
	@Test
	public void testOnWrite_Normal_MetaWithComment() throws Exception {
		var content = content(c -> {
			c.setSingleMeta("#title", "みんなのうた");
			c.setSingleMeta("#artist", "えねーちけー");
			c.setSingleMeta("#bpm", 130.123);
		});
		var textBin = write(content, new BmsStandardSaver()
				.setMetaComments(List.of(
						"This sentence is a comment.",
						"Verify output comments.")));
		assertOnWrite(textBin,
				"; This sentence is a comment.",
				"; Verify output comments.",
				"#TITLE みんなのうた",
				"#ARTIST えねーちけー",
				"#BPM 130.123");
	}

	// onWrite(BmsContent, OutputStream)
	// 単純正常系：タイムライン要素が正しく出力されること(コメントなし)
	@Test
	public void testOnWrite_Normal_Channel() throws Exception {
		var content = content(c -> {
			// 1小節目
			c.setMeasureValue(BmsInt.to36i("02"), 1, 0.5);
			c.setMeasureValue(BmsInt.to36i("03"), 1, "Music Start!");
			c.putNote(BmsInt.to36i("07"), 1, 0.0, BmsInt.to36i("XX"));
			c.putNote(BmsInt.to36i("07"), 1, 48.0, BmsInt.to36i("YY"));
			// 2小節目
			c.setMeasureValue(BmsInt.to36i("01"), 2, 500);
			c.putNote(BmsInt.to36i("07"), 2, 48.0, BmsInt.to36i("WW"));
			c.putNote(BmsInt.to36i("07"), 2, 96.0, BmsInt.to36i("ZZ"));
		});
		var textBin = write(content, new BmsStandardSaver());
		assertOnWrite(textBin,
				"#00102:0.5",
				"#00103:Music Start!",
				"#00107:XXYY",
				"",
				"#00201:500",
				"#00207:00WWZZ00");
	}

	// onWrite(BmsContent, OutputStream)
	// 単純正常系：タイムライン要素が正しく出力されること(コメントあり)
	@Test
	public void testOnWrite_Normal_ChannelWithComment() throws Exception {
		var content = content(c -> {
			// 1小節目
			c.setMeasureValue(BmsInt.to36i("04"), 1, BmsInt.to16i("F1"));
			c.setMeasureValue(BmsInt.to36i("05"), 1, BmsInt.to36i("LM"));
			c.putNote(BmsInt.to36i("07"), 1, 0.0, BmsInt.to36i("AA"));
			c.putNote(BmsInt.to36i("07"), 1, 16.0, BmsInt.to36i("BB"));
			c.putNote(BmsInt.to36i("07"), 1, 32.0, BmsInt.to36i("CC"));
			c.putNote(BmsInt.to36i("07"), 1, 48.0, BmsInt.to36i("XX"));
			c.putNote(BmsInt.to36i("07"), 1, 96.0, BmsInt.to36i("YY"));
			c.putNote(BmsInt.to36i("07"), 1, 144.0, BmsInt.to36i("ZZ"));
			// 2小節目
			c.putNote(BmsInt.to36i("07"), 3, 0.0, BmsInt.to36i("QQ"));
			// 999小節目
			c.setMeasureValue(BmsInt.to36i("03"), 999, "The last measure");
		});
		var textBin = write(content, new BmsStandardSaver()
				.setChannelComments(List.of(
						"Channel test data.",
						"This BMS is original format.")));
		assertOnWrite(textBin,
				"; Channel test data.",
				"; This BMS is original format.",
				"#00104:F1",
				"#00105:LM",
				"#00107:AABBCCXX0000YY0000ZZ0000",
				"",
				"#00307:QQ",
				"",
				"#99903:The last measure");
	}

	// onWrite(BmsContent, OutputStream)
	// 単純正常系：フッターコメントが正しく出力されること
	@Test
	public void testOnWrite_Normal_FooterComment() throws Exception {
		var content = content(c -> {
			c.putDeclaration("key", "value");
		});
		var textBin = write(content, new BmsStandardSaver()
				.setFooterComments(List.of(
						"Copyright(C) 2023 J-SON3",
						"All rights reserved")));
		assertOnWrite(textBin,
				";?bms key=\"value\"",
				"",
				"; Copyright(C) 2023 J-SON3",
				"; All rights reserved");
	}

	// onWrite(BmsContent, OutputStream)
	// 準正常系：メタ情報コメントを設定しても、メタ情報がなければコメントも出力されないこと
	@Test
	public void testOnWrite_SamiNormal_NoMetaNoComment() throws Exception {
		var content = content(c -> {
			c.putDeclaration("key", "value");
			c.putNote(BmsInt.to36i("07"), 0, 0.0, BmsInt.to36i("AA"));
		});
		var textBin = write(content, new BmsStandardSaver()
				.setMetaComments(List.of(
						"Expects this comment is not output")));
		assertOnWrite(textBin,
				";?bms key=\"value\"",
				"",
				"#00007:AA");
	}

	// onWrite(BmsContent, OutputStream)
	// 準正常系：タイムライン要素コメントを設定しても、タイムライン要素がなければコメントも出力されないこと
	@Test
	public void testOnWrite_SemiNormal_NoChannelNoComment() throws Exception {
		var content = content(c -> {
			c.putDeclaration("key", "value");
			c.setSingleMeta("#title", "MySong");
		});
		var textBin = write(content, new BmsStandardSaver()
				.setChannelComments(List.of(
						"Expects this comment is not output")));
		assertOnWrite(textBin,
				";?bms key=\"value\"",
				"",
				"#TITLE MySong");
	}

	// onWrite(BmsContent, OutputStream)
	// エンコード関連：最優先文字セットでエンコードされて出力されること
	@Test
	public void testOnWrite_Encode_Primary() throws Exception {
		var cs1 = StandardCharsets.UTF_8;
		var cs2 = StandardCharsets.UTF_16LE;
		var content = content(c -> {
			c.putDeclaration("key", "テスト");
			c.setSingleMeta("#title", "俺の作ったヤバい曲");
			c.setMeasureValue(BmsInt.to36i("03"), 0, "日本語");
		});
		var textBin = write(content, new BmsStandardSaver()
				.setCharsets(cs1, cs2));
		assertOnWrite(textBin, cs1,
				";?bms key=\"テスト\"",
				"",
				"#TITLE 俺の作ったヤバい曲",
				"",
				"#00003:日本語");
	}

	// onWrite(BmsContent, OutputStream)
	// エンコード関連：最優先文字セットでエンコードに失敗した場合、2番目の文字セットでエンコードされて出力されること
	@Test
	public void testOnWrite_Encode_Secondary() throws Exception {
		var cs1 = Charset.forName("MS932");
		var cs2 = StandardCharsets.UTF_8;
		var content = content(c -> {
			c.putDeclaration("key", "テスト");
			c.setSingleMeta("#title", "俺の作ったヤバい曲");
			c.setMeasureValue(BmsInt.to36i("03"), 0, "한국어 - Korean");
		});
		var textBin = write(content, new BmsStandardSaver()
				.setCharsets(cs1, cs2));
		assertOnWrite(textBin, cs2,
				";?bms key=\"テスト\"",
				"",
				"#TITLE 俺の作ったヤバい曲",
				"",
				"#00003:한국어 - Korean");
	}

	// onWrite(BmsContent, OutputStream)
	// エンコード関連：BmsCompatException 指定された全ての文字セットでテキストのエンコードが失敗した(明示指定時)
	@Test
	public void testOnWrite_Encode_FailCharsets() throws Exception {
		var cs1 = Charset.forName("MS932");
		var cs2 = Charset.forName("EUC-KR");
		var content = content(c -> {
			c.putDeclaration("key", "テスト");
			c.setSingleMeta("#title", "俺の作ったヤバい曲");
			c.setMeasureValue(BmsInt.to36i("03"), 0, "العربية - Arabian");
		});
		assertThrows(BmsCompatException.class, () -> write(content, new BmsStandardSaver().setCharsets(cs1, cs2)));
	}

	// onWrite(BmsContent, OutputStream)
	// エンコード関連：文字セット未指定時、ライブラリのデフォルト最優先文字セットでエンコードされて出力されること
	@Test
	public void testOnWrite_Encode_DefaultPrimary() throws Exception {
		var cs1 = StandardCharsets.UTF_8;
		var cs2 = StandardCharsets.UTF_16LE;
		var content = content(c -> {
			c.putDeclaration("key", "テスト");
			c.setSingleMeta("#title", "俺の作ったヤバい曲");
			c.setMeasureValue(BmsInt.to36i("03"), 0, "日本語");
		});
		BmsLibrary.setDefaultCharsets(cs1, cs2);
		var textBin = write(content, new BmsStandardSaver());
		assertOnWrite(textBin, cs1,
				";?bms key=\"テスト\"",
				"",
				"#TITLE 俺の作ったヤバい曲",
				"",
				"#00003:日本語");
	}

	// onWrite(BmsContent, OutputStream)
	// エンコード関連：文字セット未指定時、ライブラリのデフォルト最優先文字セットでエンコードに失敗した場合、2番目の文字セットでエンコードされて出力されること
	@Test
	public void testOnWrite_Encode_DefaultSecondary() throws Exception {
		var cs1 = Charset.forName("MS932");
		var cs2 = StandardCharsets.UTF_8;
		var content = content(c -> {
			c.putDeclaration("key", "テスト");
			c.setSingleMeta("#title", "俺の作ったヤバい曲");
			c.setMeasureValue(BmsInt.to36i("03"), 0, "한국어 - Korean");
		});
		BmsLibrary.setDefaultCharsets(cs1, cs2);
		var textBin = write(content, new BmsStandardSaver());
		assertOnWrite(textBin, cs2,
				";?bms key=\"テスト\"",
				"",
				"#TITLE 俺の作ったヤバい曲",
				"",
				"#00003:한국어 - Korean");
	}

	// onWrite(BmsContent, OutputStream)
	// エンコード関連：BmsCompatException 指定された全ての文字セットでテキストのエンコードが失敗した(ライブラリのデフォルト使用時)
	@Test
	public void testOnWrite_Encode_FailDefault() throws Exception {
		var cs1 = Charset.forName("MS932");
		var cs2 = Charset.forName("EUC-KR");
		var content = content(c -> {
			c.putDeclaration("key", "テスト");
			c.setSingleMeta("#title", "俺の作ったヤバい曲");
			c.setMeasureValue(BmsInt.to36i("03"), 0, "العربية - Arabian");
		});
		BmsLibrary.setDefaultCharsets(cs1, cs2);
		assertThrows(BmsCompatException.class, () -> write(content, new BmsStandardSaver()));
	}

	// onWrite(BmsContent, OutputStream)
	// メタ情報関連：全てのデータ型の出力内容が期待通りであること
	@Test
	public void testOnWrite_Meta_AllTypes() throws Exception {
		var content = content(c -> {
			c.setSingleMeta("#sinteger", 300);
			c.setSingleMeta("#sinteger2", Long.MIN_VALUE);
			c.setSingleMeta("#sinteger3", Long.MAX_VALUE);
			c.setSingleMeta("#sfloat", 123.4567);
			c.setSingleMeta("#sfloat2", -12345678901234.0);
			c.setSingleMeta("#sfloat3", 98765432109876.0);
			c.setSingleMeta("#sstring", "Text");
			c.setSingleMeta("#sstring2", "");
			c.setSingleMeta("#sstring3", "<--------><--------><--------><--------><--------><--------><--------><--------><--------><-------->");
			c.setSingleMeta("#sbase16", BmsInt.to16i("0C"));
			c.setSingleMeta("#sbase162", BmsInt.to16i("00"));
			c.setSingleMeta("#sbase163", BmsInt.to16i("EF"));
			c.setSingleMeta("#sbase36", BmsInt.to36i("V0"));
			c.setSingleMeta("#sbase362", BmsInt.to36i("00"));
			c.setSingleMeta("#sbase363", BmsInt.to36i("ZY"));
			c.setSingleMeta("#sarray16", new BmsArray("00A100B200C300D400E500F6", 16));
			c.setSingleMeta("#sarray162", new BmsArray("", 16));
			c.setSingleMeta("#sarray163", new BmsArray("FF11EE22DD33", 16));
			c.setSingleMeta("#sarray36", new BmsArray("ZZ00YY00XX00WW", 36));
			c.setSingleMeta("#sarray362", new BmsArray("", 36));
			c.setSingleMeta("#sarray363", new BmsArray("0011009900AA00KK", 36));
		});
		var textBin = write(content, new BmsStandardSaver());
		assertOnWrite(textBin,
				"#SINTEGER 300",
				"#SINTEGER2 -9223372036854775808",
				"#SINTEGER3 9223372036854775807",
				"#SFLOAT 123.4567",
				"#SFLOAT2 -12345678901234",
				"#SFLOAT3 98765432109876",
				"#SSTRING Text",
				"#SSTRING2 ",
				"#SSTRING3 <--------><--------><--------><--------><--------><--------><--------><--------><--------><-------->",
				"#SBASE16 0C",
				"#SBASE162 00",
				"#SBASE163 EF",
				"#SBASE36 V0",
				"#SBASE362 00",
				"#SBASE363 ZY",
				"#SARRAY16 00A100B200C300D400E500F6",
				"#SARRAY162 ",
				"#SARRAY163 FF11EE22DD33",
				"#SARRAY36 ZZ00YY00XX00WW",
				"#SARRAY362 ",
				"#SARRAY363 0011009900AA00KK");
	}

	// onWrite(BmsContent, OutputStream)
	// メタ情報関連：保有する全ての複数メタ情報が期待通りに出力されること
	@Test
	public void testOnWrite_Meta_Multiple() throws Exception {
		var content = content(c -> {
			c.putMultipleMeta("#minteger", 50);
			c.putMultipleMeta("#minteger", 30);
			c.putMultipleMeta("#minteger", 10);
			c.putMultipleMeta("#mfloat", 1.23);
			c.putMultipleMeta("#mfloat", 4.0);
			c.putMultipleMeta("#mfloat", 5.67);
			c.putMultipleMeta("#mfloat", 8.9);
			c.setMultipleMeta("#mfloat", 2, null); // 5.67を削除
		});
		var textBin = write(content, new BmsStandardSaver());
		assertOnWrite(textBin,
				"#MINTEGER 50",
				"#MINTEGER 30",
				"#MINTEGER 10",
				"#MFLOAT 1.23",
				"#MFLOAT 4",
				"#MFLOAT 8.9");
	}

	// onWrite(BmsContent, OutputStream)
	// メタ情報関連：保有する全ての索引付きメタ情報が期待通りに出力されること(索引の値に関するテスト)
	@Test
	public void testOnWrite_Meta_Indexed() throws Exception {
		var content = content(c -> {
			c.setIndexedMeta("#istring", BmsInt.to36i("01"), "Michael");
			c.setIndexedMeta("#istring", BmsInt.to36i("A5"), "Ray");
			c.setIndexedMeta("#istring", BmsInt.to36i("RK"), "Cate");
			c.setIndexedMeta("#istring", BmsInt.to36i("XY"), "Kyle");
			c.setIndexedMeta("#ibase16", BmsInt.to36i("01"), BmsInt.to16i("35"));
			c.setIndexedMeta("#ibase16", BmsInt.to36i("02"), BmsInt.to16i("7D"));
			c.setIndexedMeta("#ibase16", BmsInt.to36i("03"), BmsInt.to16i("A6"));
			c.setIndexedMeta("#ibase16", BmsInt.to36i("04"), BmsInt.to16i("E8"));
			c.setIndexedMeta("#ibase16", BmsInt.to36i("03"), null); // A6を削除
		});
		var textBin = write(content, new BmsStandardSaver());
		assertOnWrite(textBin,
				"#ISTRING01 Michael",
				"#ISTRINGA5 Ray",
				"#ISTRINGRK Cate",
				"#ISTRINGXY Kyle",
				"",
				"#IBASE1601 35",
				"#IBASE1602 7D",
				"#IBASE1604 E8");
	}

	// onWrite(BmsContent, OutputStream)
	// メタ情報関連：任意型のメタ情報は出力されないこと
	@Test
	public void testOnWrite_Meta_ObjectType() throws Exception {
		var content = content(c -> {
			c.setSingleMeta("#genre", "TECHNO");
			c.setSingleMeta("#title", "MySong");
			c.setSingleMeta("#sobject", LocalDateTime.now());
			c.putMultipleMeta("#mobject", Collections.emptyList());
			c.putMultipleMeta("#mobject", Collections.emptyMap());
			c.setIndexedMeta("#iobject", 5, this.getClass());
			c.setIndexedMeta("#iobject", 9, null);
		});
		var textBin = write(content, new BmsStandardSaver());
		assertOnWrite(textBin,
				"#GENRE TECHNO",
				"#TITLE MySong");
	}

	// onWrite(BmsContent, OutputStream)
	// メタ情報関連：単体・複数→索引付きの順で出力され、索引付きはメタ情報ごとに改行が入ること
	@Test
	public void testOnWrite_Meta_Order() throws Exception {
		var content = content(c -> {
			c.setSingleMeta("#genre", "HOUSE");
			c.setSingleMeta("#title", "MySong");
			c.setSingleMeta("#artist", "Michael");
			c.putMultipleMeta("#subartist", "Ray");
			c.putMultipleMeta("#subartist", "Cate");
			c.putMultipleMeta("#subartist", "Kyle");
			c.setSingleMeta("#bpm", 150.0);
			c.setSingleMeta("#sinteger", 893);
			c.putMultipleMeta("#mstring", "Tokyo");
			c.putMultipleMeta("#mstring", "Osaka");
			c.putMultipleMeta("#mstring", "Fukuoka");
			c.setIndexedMeta("#bpm", BmsInt.to36i("02"), 180.0);
			c.setIndexedMeta("#bpm", BmsInt.to36i("05"), 195.5);
			c.setIndexedMeta("#bpm", BmsInt.to36i("A8"), 200.0);
			c.setIndexedMeta("#stop", BmsInt.to36i("01"), 48.0);
			c.setIndexedMeta("#stop", BmsInt.to36i("02"), 64.0);
			c.setIndexedMeta("#stop", BmsInt.to36i("X0"), 192.0);
			c.setIndexedMeta("#stop", BmsInt.to36i("Y1"), 384.0);
		});
		var textBin = write(content, new BmsStandardSaver());
		assertOnWrite(textBin,
				"#GENRE HOUSE",
				"#TITLE MySong",
				"#ARTIST Michael",
				"#SUBARTIST Ray",
				"#SUBARTIST Cate",
				"#SUBARTIST Kyle",
				"#BPM 150",
				"#SINTEGER 893",
				"#MSTRING Tokyo",
				"#MSTRING Osaka",
				"#MSTRING Fukuoka",
				"",
				"#BPM02 180",
				"#BPM05 195.5",
				"#BPMA8 200",
				"",
				"#STOP01 48",
				"#STOP02 64",
				"#STOPX0 192",
				"#STOPY1 384");
	}

	// onWrite(BmsContent, OutputStream)
	// チャンネル関連：全てのデータ型の出力内容が期待通りであること
	@Test
	public void testOnWrite_Channel_AllTypes() throws Exception {
		var content = content(c -> {
			c.setMeasureValue(BmsInt.to36i("01"), 1, 300);
			c.setMeasureValue(BmsInt.to36i("01"), 2, Long.MIN_VALUE);
			c.setMeasureValue(BmsInt.to36i("01"), 3, Long.MAX_VALUE);
			c.setMeasureValue(BmsInt.to36i("03"), 1, "Text");
			c.setMeasureValue(BmsInt.to36i("03"), 2, "");
			c.setMeasureValue(BmsInt.to36i("03"), 3, "<--------><--------><--------><--------><--------><--------><--------><--------><--------><-------->");
			c.setMeasureValue(BmsInt.to36i("04"), 1, BmsInt.to16i("0C"));
			c.setMeasureValue(BmsInt.to36i("04"), 2, BmsInt.to16i("00"));
			c.setMeasureValue(BmsInt.to36i("04"), 3, BmsInt.to16i("EF"));
			c.setMeasureValue(BmsInt.to36i("05"), 1, BmsInt.to36i("V0"));
			c.setMeasureValue(BmsInt.to36i("05"), 2, BmsInt.to36i("00"));
			c.setMeasureValue(BmsInt.to36i("05"), 3, BmsInt.to36i("XY"));
			c.putNote(BmsInt.to36i("06"), 1, 0.0, BmsInt.to16i("A1"));
			c.putNote(BmsInt.to36i("06"), 1, 24.0, BmsInt.to16i("B2"));
			c.putNote(BmsInt.to36i("06"), 1, 48.0, BmsInt.to16i("C3"));
			c.putNote(BmsInt.to36i("06"), 1, 72.0, BmsInt.to16i("D4"));
			c.putNote(BmsInt.to36i("06"), 1, 96.0, BmsInt.to16i("E5"));
			c.putNote(BmsInt.to36i("06"), 1, 120.0, BmsInt.to16i("F6"));
			c.putNote(BmsInt.to36i("06"), 3, 0.0, BmsInt.to16i("FF"));
			c.putNote(BmsInt.to36i("06"), 3, 16.0, BmsInt.to16i("11"));
			c.putNote(BmsInt.to36i("06"), 3, 32.0, BmsInt.to16i("EE"));
			c.putNote(BmsInt.to36i("06"), 3, 96.0, BmsInt.to16i("22"));
			c.putNote(BmsInt.to36i("06"), 3, 112.0, BmsInt.to16i("DD"));
			c.putNote(BmsInt.to36i("06"), 3, 128.0, BmsInt.to16i("33"));
			c.putNote(BmsInt.to36i("07"), 1, 0.0, BmsInt.to36i("ZZ"));
			c.putNote(BmsInt.to36i("07"), 1, 48.0, BmsInt.to36i("YY"));
			c.putNote(BmsInt.to36i("07"), 1, 96.0, BmsInt.to36i("XX"));
			c.putNote(BmsInt.to36i("07"), 1, 144.0, BmsInt.to36i("WW"));
			c.putNote(BmsInt.to36i("07"), 3, 24.0, BmsInt.to36i("11"));
			c.putNote(BmsInt.to36i("07"), 3, 72.0, BmsInt.to36i("99"));
			c.putNote(BmsInt.to36i("07"), 3, 120.0, BmsInt.to36i("AA"));
			c.putNote(BmsInt.to36i("07"), 3, 168.0, BmsInt.to36i("KK"));
			c.setMeasureValue(BmsInt.to36i("0A"), 1, 123.4567);
			c.setMeasureValue(BmsInt.to36i("0A"), 2, -12345678901234.0);
			c.setMeasureValue(BmsInt.to36i("0A"), 3, 98765432109876.0);
		});
		var textBin = write(content, new BmsStandardSaver());
		assertOnWrite(textBin,
				"#00101:300",
				"#00103:Text",
				"#00104:0C",
				"#00105:V0",
				"#00106:A1B2C3D4E5F60000",
				"#00107:ZZYYXXWW",
				"#0010A:123.4567",
				"",
				"#00201:-9223372036854775808",
				"#00203:",
				"#00204:00",
				"#00205:00",
				"#0020A:-12345678901234",
				"",
				"#00301:9223372036854775807",
				"#00303:<--------><--------><--------><--------><--------><--------><--------><--------><--------><-------->",
				"#00304:EF",
				"#00305:XY",
				"#00306:FF11EE00000022DD33000000",
				"#00307:0011009900AA00KK",
				"#0030A:98765432109876");
	}

	// onWrite(BmsContent, OutputStream)
	// チャンネル関連：保有する全ての重複可能チャンネルが期待通りに出力されること
	@Test
	public void testOnWrite_Channel_Multiple() throws Exception {
		var content = content(c -> {
			// 1小節目：値型
			c.putNote(BmsInt.to36i("AA"), 1, 0.0, BmsInt.to36i("XX"));
			c.setMeasureValue(BmsInt.to36i("M1"), 0, 1, 500);
			c.setMeasureValue(BmsInt.to36i("M1"), 1, 1, 300);
			c.setMeasureValue(BmsInt.to36i("M1"), 2, 1, 100);
			c.setMeasureValue(BmsInt.to36i("M2"), 0, 1, 1.23);
			c.setMeasureValue(BmsInt.to36i("M2"), 1, 1, 4.0);
			c.setMeasureValue(BmsInt.to36i("M2"), 2, 1, 5.67);
			c.setMeasureValue(BmsInt.to36i("M2"), 3, 1, 8.9);
			c.setMeasureValue(BmsInt.to36i("M2"), 2, 1, null);  // 5.67を削除 ※小節データ削除ではデータ詰めは行われない
			c.putNote(BmsInt.to36i("ZZ"), 1, 0.0, BmsInt.to36i("YY"));
			// 2小節目：配列型
			c.putNote(BmsInt.to36i("M7"), 0, 2, 0.0, BmsInt.to36i("A1"));
			c.putNote(BmsInt.to36i("M7"), 0, 2, 96.0, BmsInt.to36i("A2"));
			c.putNote(BmsInt.to36i("M7"), 1, 2, 0.0, BmsInt.to36i("B1"));
			c.putNote(BmsInt.to36i("M7"), 2, 2, 0.0, BmsInt.to36i("C1"));
			c.putNote(BmsInt.to36i("M7"), 2, 2, 48.0, BmsInt.to36i("C2"));
			c.putNote(BmsInt.to36i("M7"), 2, 2, 96.0, BmsInt.to36i("C3"));
			c.putNote(BmsInt.to36i("M7"), 2, 2, 144.0, BmsInt.to36i("C4"));
		});
		var textBin = write(content, new BmsStandardSaver());
		assertOnWrite(textBin,
				"#001AA:XX",
				"#001M1:500",
				"#001M1:300",
				"#001M1:100",
				"#001M2:1.23",
				"#001M2:4",
				"#001M2:0",
				"#001M2:8.9",
				"#001ZZ:YY",
				"",
				"#002M7:A1A2",
				"#002M7:B1",
				"#002M7:C1C2C3C4");
	}

	// onWrite(BmsContent, OutputStream)
	// チャンネル関連：空配列が存在し、小節を跨いだ重複可能チャンネルのデータが期待通り出力されること
	@Test
	public void testOnWrite_Channel_Multiple2() throws Exception {
		var content = content(c -> {
			// 1小節目：ChIndex=0が空、ChIndex=1にデータあり
			c.putNote(BmsInt.to36i("M7"), 1, 1, 144.0, BmsInt.to36i("AG"));
			// 2小節目：ChIndex=0,2にデータあり、ChIndex=0が空
			c.putNote(BmsInt.to36i("M7"), 0, 2, 0.0, BmsInt.to36i("BH"));
			c.putNote(BmsInt.to36i("M7"), 0, 2, 48.0, BmsInt.to36i("CI"));
			c.putNote(BmsInt.to36i("M7"), 0, 2, 96.0, BmsInt.to36i("DJ"));
			c.putNote(BmsInt.to36i("M7"), 0, 2, 144.0, BmsInt.to36i("EK"));
			c.putNote(BmsInt.to36i("M7"), 2, 2, 48.0, BmsInt.to36i("FL"));
			c.putNote(BmsInt.to36i("M7"), 2, 2, 144.0, BmsInt.to36i("GM"));
		});
		var textBin = write(content, new BmsStandardSaver());
		assertOnWrite(textBin,
				"#001M7:00",
				"#001M7:000000AG",
				"",
				"#002M7:BHCIDJEK",
				"#002M7:00",
				"#002M7:00FL00GM");
	}

	// onWrite(BmsContent, OutputStream)
	// チャンネル関連：任意型のチャンネルは出力されないこと
	@Test
	public void testOnWrite_Channel_ObjectType() throws Exception {
		var content = content(c -> {
			c.setMeasureValue(BmsInt.to36i("01"), 1, 100);
			c.setMeasureValue(BmsInt.to36i("03"), 1, "Text");
			c.setMeasureValue(BmsSpec.USER_CHANNEL_MIN + 0, 1, LocalDateTime.now());
		});
		var textBin = write(content, new BmsStandardSaver());
		assertOnWrite(textBin,
				"#00101:100",
				"#00103:Text");
	}

	// onWrite(BmsContent, OutputStream)
	// チャンネル関連：小数部のない刻み位置のみのチャンネルでの配列データ出力内容が期待通りであること
	@Test
	public void testOnWrite_Channel_AllIntegerTick() throws Exception {
		var content = content(c -> {
			// 1小節目：1件、Tick = 0 → AA
			c.putNote(BmsInt.to36i("07"), 1, 0.0, BmsInt.to36i("AA"));
			// 2小節目：1件、Tick > 0 → 00BB0000
			c.putNote(BmsInt.to36i("07"), 2, 48.0, BmsInt.to36i("BB"));
			// 3小節目：複数件、最大公約数割り切れる → C1C20000C300000000000000C4000000
			c.putNote(BmsInt.to36i("07"), 3, 0.0, BmsInt.to36i("C1"));
			c.putNote(BmsInt.to36i("07"), 3, 12.0, BmsInt.to36i("C2"));
			c.putNote(BmsInt.to36i("07"), 3, 48.0, BmsInt.to36i("C3"));
			c.putNote(BmsInt.to36i("07"), 3, 144.0, BmsInt.to36i("C4"));
			// 4小節目：複数件、最大公約数割り切れない → 00000000D1000000D2...(以降全て00で合計384文字)
			c.putNote(BmsInt.to36i("07"), 4, 5.0, BmsInt.to36i("D1"));
			c.putNote(BmsInt.to36i("07"), 4, 9.0, BmsInt.to36i("D2"));
			// 5小節目：複数件、小節長変更 → E1E2E3
			c.setMeasureValue(BmsInt.to36i("02"), 5, 0.75);
			c.putNote(BmsInt.to36i("07"), 5, 0.0, BmsInt.to36i("E1"));
			c.putNote(BmsInt.to36i("07"), 5, 48.0, BmsInt.to36i("E2"));
			c.putNote(BmsInt.to36i("07"), 5, 96.0, BmsInt.to36i("E3"));
		});
		var textBin = write(content, new BmsStandardSaver());
		assertOnWrite(textBin,
				"#00107:AA",
				"",
				"#00207:00BB0000",
				"",
				"#00307:C1C20000C300000000000000C4000000",
				"",
				"#00407:0000000000D1000000D20000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000",
				"",
				"#00502:0.75",
				"#00507:E1E2E3");
	}

	// onWrite(BmsContent, OutputStream)
	// チャンネル関連：小数部のある刻み位置を持つチャンネルでの配列データ出力内容が期待通りであること
	@Test
	public void testOnWrite_Channel_HasDecimalTick() throws Exception {
		var content = content(c -> {
			// 1小節目：全ての小数部持ちTickを10倍で整数になり、最大分解能の範囲内
			c.putNote(BmsInt.to36i("07"), 1, 0.0, BmsInt.to36i("WW"));
			c.putNote(BmsInt.to36i("07"), 1, 48.5, BmsInt.to36i("XX"));
			c.putNote(BmsInt.to36i("07"), 1, 96.0, BmsInt.to36i("YY"));
			c.putNote(BmsInt.to36i("07"), 1, 96.5, BmsInt.to36i("ZZ"));
			// 2小節目：全ての小数部持ちTickを10倍で整数になるが、最大公約数が小さくて最大分解能を超過する
			c.putNote(BmsInt.to36i("07"), 2, 0.0, BmsInt.to36i("WW"));
			c.putNote(BmsInt.to36i("07"), 2, 12.6, BmsInt.to36i("XX"));
			c.putNote(BmsInt.to36i("07"), 2, 24.7, BmsInt.to36i("YY"));
			c.putNote(BmsInt.to36i("07"), 2, 36.4, BmsInt.to36i("ZZ"));
			// 3小節目：全ての小数部持ちTickを最大倍数でも整数にならない(仕方なく最大分解能で出力する動作になる)
			c.putNote(BmsInt.to36i("07"), 3, 0.0, BmsInt.to36i("WW"));
			c.putNote(BmsInt.to36i("07"), 3, 96.123456789012, BmsInt.to36i("XX"));
			// 4小節目：配列データの出力精度試験 - 01
			c.setMeasureValue(BmsInt.to36i("02"), 4, 0.0188);
			c.putNote(BmsInt.to36i("07"), 4, 0.0, BmsInt.to36i("XX"));
			c.putNote(BmsInt.to36i("07"), 4, 1.2032, BmsInt.to36i("YY"));
			c.putNote(BmsInt.to36i("07"), 4, 2.4064, BmsInt.to36i("ZZ"));
			// 5小節目：配列データの出力精度試験 - 02
			c.setMeasureValue(BmsInt.to36i("02"), 5, 0.002);
			c.putNote(BmsInt.to36i("07"), 5, 0.096, BmsInt.to36i("XX"));
			c.putNote(BmsInt.to36i("07"), 5, 0.224, BmsInt.to36i("YY"));
			c.putNote(BmsInt.to36i("07"), 5, 0.36, BmsInt.to36i("ZZ"));
			// 6小節目：配列データの出力精度試験 - 03
			c.setMeasureValue(BmsInt.to36i("02"), 6, 0.0703125);
			c.putNote(BmsInt.to36i("07"), 6, 3.0, BmsInt.to36i("XX"));
			c.putNote(BmsInt.to36i("07"), 6, 7.5, BmsInt.to36i("YY"));
			// 7小節目：配列データの出力精度試験 - 04
			c.setMeasureValue(BmsInt.to36i("02"), 7, 2.53125);
			c.putNote(BmsInt.to36i("07"), 7, 0.0, BmsInt.to36i("XX"));
			c.putNote(BmsInt.to36i("07"), 7, 47.03225806451613, BmsInt.to36i("YY"));
			// 8小節目：配列データの出力精度試験 - 05
			c.setMeasureValue(BmsInt.to36i("02"), 8, 1.3984375);
			c.putNote(BmsInt.to36i("07"), 8, 72.0, BmsInt.to36i("XX"));
			c.putNote(BmsInt.to36i("07"), 8, 264.0, BmsInt.to36i("YY"));
		});
		var textBin = write(content, new BmsStandardSaver().setMaxPrecision(768));
		assertOnWrite(textBin,
				"#00107:WW000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000XX00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000YYZZ00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000",
				"",
				"#00207:WW00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000XX000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000YY00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000ZZ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000",
				"",
				"#00307:WW0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000XX0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000",
				"",
				"#00402:0.0188",
				"#00407:XXYYZZ",
				"",
				"#00502:0.002",
				"#00507:000000000000000000000000XX000000000000000000000000000000YY00000000000000000000000000000000ZZ0000",
				"",
				"#00602:0.0703125",
				"#00607:0000XX0000YY000000",
				"",
				"#00702:2.53125",
				"#00707:XX0000YY000000000000000000000000000000000000000000000000000000",
				"",
				"#00802:1.3984375",
				"#00807:000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000XX00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000YY0000");
	}

	// onWrite(BmsContent, OutputStream)
	// 互換性チェック関連：最大小節数のBMSコンテンツが期待通りの出力内容であること
	@Test
	public void testOnWrite_Compat_MaxMeasureCount() throws Exception {
		var content = content(c -> {
			c.putNote(7, BmsStandardSaver.MEASURE_COUNT_MAX - 1, 0.0, 3);
		});
		var textBin = write(content, new BmsStandardSaver());
		assertOnWrite(textBin, "#99907:03");
	}

	// onWrite(BmsContent, OutputStream)
	// 互換性チェック関連：最大小節数を超過したBMSコンテンツではBmsCompatExceptionがスローされること
	@Test
	public void testOnWrite_Compat_OverMaxMeasureCount() throws Exception {
		var content = content(c -> {
			c.putNote(7, BmsStandardSaver.MEASURE_COUNT_MAX, 0.0, 3);
		});
		var saver = new BmsStandardSaver();
		assertThrows(BmsCompatException.class, () -> write(content, saver));
	}

	// onWrite(BmsContent, OutputStream)
	// 互換性チェック関連：最大インデックス値の索引付きメタ情報があるBMSコンテンツが期待通りの出力内容であること
	@Test
	public void testOnWrite_Compat_MaxMetaIndex() throws Exception {
		var content = content(c -> {
			c.setIndexedMeta("#iinteger", BmsStandardSaver.META_INDEX_MAX, 10L);
		});
		var textBin = write(content, new BmsStandardSaver());
		assertOnWrite(textBin, "#IINTEGERZZ 10");
	}

	// onWrite(BmsContent, OutputStream)
	// 互換性チェック関連：最大インデックス値を超過した索引付きメタ情報を持つBMSコンテンツではBmsCompatExceptionがスローされること
	@Test
	public void testOnWrite_Compat_OverMaxMetaIndex() throws Exception {
		var content = content(c -> {
			c.setIndexedMeta("#iinteger", BmsStandardSaver.META_INDEX_MAX + 1, 10L);
		});
		var saver = new BmsStandardSaver();
		assertThrows(BmsCompatException.class, () -> write(content, saver));
	}

	// onWrite(BmsContent, OutputStream)
	// 互換性チェック関連：任意型の索引付きメタ情報でインデックス値が超過しても正常に出力されること(任意型メタ情報は出力されない)
	@Test
	public void testOnWrite_Compat_OverMaxObjectMetaIndex() throws Exception {
		var content = content(c -> {
			c.setIndexedMeta("#iinteger", BmsInt.to36i("XX"), 10L);
			c.setIndexedMeta("#iobject", BmsStandardSaver.META_INDEX_MAX + 1, LocalDateTime.now());
		});
		var textBin = write(content, new BmsStandardSaver());
		assertOnWrite(textBin, "#IINTEGERXX 10");
	}

	// onWrite(BmsContent, OutputStream)
	// 互換性チェック関連：16進配列の要素で最小値のノートの値があっても期待通りの出力内容であること
	@Test
	public void testOnWrite_Compat_MinArray16Value() throws Exception {
		var content = content(c -> {
			c.putNote(6, 1, 0.0, BmsSpec.VALUE_16_MIN);
		});
		var textBin = write(content, new BmsStandardSaver());
		assertOnWrite(textBin, "#00106:01");
	}

	// onWrite(BmsContent, OutputStream)
	// 互換性チェック関連：16進配列の要素で最大値のノートの値があっても期待通りの出力内容であること
	@Test
	public void testOnWrite_Compat_MaxArray16Value() throws Exception {
		var content = content(c -> {
			c.putNote(6, 1, 0.0, BmsSpec.VALUE_16_MAX);
		});
		var textBin = write(content, new BmsStandardSaver());
		assertOnWrite(textBin, "#00106:FF");
	}

	// onWrite(BmsContent, OutputStream)
	// 互換性チェック関連：16進配列の要素で最小値を超過した場合BmsCompatExceptionがスローされること
	@Test
	public void testOnWrite_Compat_OverMinArray16Value() throws Exception {
		var content = content(c -> {
			c.putNote(6, 1, 0.0, -1);
		});
		var saver = new BmsStandardSaver();
		assertThrows(BmsCompatException.class, () -> write(content, saver));
	}

	// onWrite(BmsContent, OutputStream)
	// 互換性チェック関連：16進配列の要素で最大値を超過した場合BmsCompatExceptionがスローされること
	@Test
	public void testOnWrite_Compat_OverMaxArray16Value() throws Exception {
		var content = content(c -> {
			c.putNote(6, 1, 0.0, BmsSpec.VALUE_16_MAX + 1);
		});
		var saver = new BmsStandardSaver();
		assertThrows(BmsCompatException.class, () -> write(content, saver));
	}

	// onWrite(BmsContent, OutputStream)
	// 互換性チェック関連：16進配列(ユーザーチャンネル)の要素が最小・最大値を超過しても正常に出力されること(ユーザーチャンネルは出力されない)
	@Test
	public void testOnWrite_Compat_OverUserArray16Value() throws Exception {
		var content = content(c -> {
			c.putNote(6, 1, 0.0, BmsInt.to16i("AA"));
			c.putNote(BmsSpec.USER_CHANNEL_MIN + 10, 1, 0.0, BmsSpec.VALUE_16_MAX + 1);
		});
		var textBin = write(content, new BmsStandardSaver());
		assertOnWrite(textBin, "#00106:AA");
	}

	// onWrite(BmsContent, OutputStream)
	// 互換性チェック関連：36進配列の要素で最小値のノートの値があっても期待通りの出力内容であること
	@Test
	public void testOnWrite_Compat_MinArray36Value() throws Exception {
		var content = content(c -> {
			c.putNote(7, 1, 0.0, BmsSpec.VALUE_36_MIN);
		});
		var textBin = write(content, new BmsStandardSaver());
		assertOnWrite(textBin, "#00107:01");
	}

	// onWrite(BmsContent, OutputStream)
	// 互換性チェック関連：36進配列の要素で最大値のノートの値があっても期待通りの出力内容であること
	@Test
	public void testOnWrite_Compat_MaxArray36Value() throws Exception {
		var content = content(c -> {
			c.putNote(7, 1, 0.0, BmsSpec.VALUE_36_MAX);
		});
		var textBin = write(content, new BmsStandardSaver());
		assertOnWrite(textBin, "#00107:ZZ");
	}

	// onWrite(BmsContent, OutputStream)
	// 互換性チェック関連：36進配列の要素で最小値を超過した場合BmsCompatExceptionがスローされること
	@Test
	public void testOnWrite_Compat_OverMinArray36Value() throws Exception {
		var content = content(c -> {
			c.putNote(7, 1, 0.0, -1);
		});
		var saver = new BmsStandardSaver();
		assertThrows(BmsCompatException.class, () -> write(content, saver));
	}

	// onWrite(BmsContent, OutputStream)
	// 互換性チェック関連：36進配列の要素で最大値を超過した場合BmsCompatExceptionがスローされること
	@Test
	public void testOnWrite_Compat_OverMaxArray36Value() throws Exception {
		var content = content(c -> {
			c.putNote(7, 1, 0.0, BmsSpec.VALUE_36_MAX + 1);
		});
		var saver = new BmsStandardSaver();
		assertThrows(BmsCompatException.class, () -> write(content, saver));
	}

	// onWrite(BmsContent, OutputStream)
	// 互換性チェック関連：36進配列(ユーザーチャンネル)の要素が最小・最大値を超過しても正常に出力されること(ユーザーチャンネルは出力されない)
	@Test
	public void testOnWrite_Compat_OverUserArray36Value() throws Exception {
		var content = content(c -> {
			c.putNote(7, 1, 0.0, BmsInt.to36i("XX"));
			c.putNote(BmsSpec.USER_CHANNEL_MIN + 11, 1, 0.0, BmsSpec.VALUE_36_MAX + 1);
		});
		var textBin = write(content, new BmsStandardSaver());
		assertOnWrite(textBin, "#00107:XX");
	}

	// onWrite(BmsContent, OutputStream)
	// チャンネル関連：BmsCompatException 配列データの分解能不足によりノートの欠落が発生した(小数部を持つ刻み位置のテスト)
	@Test
	public void testOnWrite_Channel_OverwriteData() throws Exception {
		var content = content(c -> {
			// 1小節目：全ての小数部持ちTickが分解能倍率「2」で整数になる
			c.putNote(BmsInt.to36i("07"), 1, 0.0, BmsInt.to36i("XX"));
			c.putNote(BmsInt.to36i("07"), 1, 96.0, BmsInt.to36i("YY"));
			c.putNote(BmsInt.to36i("07"), 1, 96.000001, BmsInt.to36i("ZZ"));  // Tickを詰まらせて重複させる
		});
		assertThrows(BmsCompatException.class, () -> {
			write(content, new BmsStandardSaver().setMaxPrecision(BmsStandardSaver.PRECISION_MIN));
		});
	}

	// onWrite(BmsContent, OutputStream)
	// コメント：コメント途中の改行コード"\r", "\n"が消去され" "に変換されて出力されること
	@Test
	public void testOnWrite_Comment_RemoveBreakLineCode() throws Exception {
		var content = content(c -> {
			c.setSingleMeta("#title", "MySong");
			c.setMeasureValue(BmsInt.to36i("01"), 0, 3L);
			c.putNote(BmsInt.to36i("07"), 0, 0.0, BmsInt.to36i("XX"));
		});
		var textBin = write(content, new BmsStandardSaver()
				.setMetaComments(List.of("meta\r\nmeta\rmeta\nmeta\r\r\n\nmeta"))
				.setChannelComments(List.of("channel\r\nchannel\rchannel\nchannel\r\r\n\nchannel"))
				.setFooterComments(List.of("footer\r\nfooter\rfooter\nfooter\r\r\n\nfooter")));
		assertOnWrite(textBin,
				"; meta  meta meta meta    meta",
				"#TITLE MySong",
				"",
				"; channel  channel channel channel    channel",
				"#00001:3",
				"#00007:XX",
				"",
				"; footer  footer footer footer    footer");
	}

	// onWrite(BmsContent, OutputStream)
	// コメント：コメント途中の複数行コメント開始文字"/*"が消去され" "に変換されて出力されること
	@Test
	public void testOnWrite_Comment_RemoveBeginMultiLineComment() throws Exception {
		var content = content(c -> {
			c.setSingleMeta("#title", "MySong");
			c.setMeasureValue(BmsInt.to36i("01"), 0, 3L);
			c.putNote(BmsInt.to36i("07"), 0, 0.0, BmsInt.to36i("XX"));
		});
		var textBin = write(content, new BmsStandardSaver()
				.setMetaComments(List.of("meta/*meta*/meta/*meta"))
				.setChannelComments(List.of("channel/*channel*/channel/*channel"))
				.setFooterComments(List.of("footer/*footer*/footer/*footer")));
		assertOnWrite(textBin,
				"; meta meta*/meta meta",
				"#TITLE MySong",
				"",
				"; channel channel*/channel channel",
				"#00001:3",
				"#00007:XX",
				"",
				"; footer footer*/footer footer");
	}

	// onWrite(BmsContent, OutputStream)
	// コメント：コメント末尾の空白文字が完全に消去されて出力されること
	@Test
	public void testOnWrite_Comment_StripTailWhiteSpaces() throws Exception {
		var content = content(c -> {
			c.setSingleMeta("#title", "MySong");
			c.setMeasureValue(BmsInt.to36i("01"), 0, 3L);
			c.putNote(BmsInt.to36i("07"), 0, 0.0, BmsInt.to36i("XX"));
		});
		var textBin = write(content, new BmsStandardSaver()
				.setMetaComments(List.of("meta \tmeta \t \t"))
				.setChannelComments(List.of("channel \tchannel \t \t"))
				.setFooterComments(List.of("footer \tfooter \t \t")));
		assertOnWrite(textBin,
				"; meta \tmeta",
				"#TITLE MySong",
				"",
				"; channel \tchannel",
				"#00001:3",
				"#00007:XX",
				"",
				"; footer \tfooter");
	}

	// onWrite(BmsContent, OutputStream)
	// コメント：先頭文字が";"の時、コメント文字が付加されずに出力されること
	@Test
	public void testOnWrite_Comment_NotAddCommentCharIfSemicolon() throws Exception {
		var content = content(c -> {
			c.setSingleMeta("#title", "MySong");
			c.setMeasureValue(BmsInt.to36i("01"), 0, 3L);
			c.putNote(BmsInt.to36i("07"), 0, 0.0, BmsInt.to36i("XX"));
		});
		var textBin = write(content, new BmsStandardSaver()
				.setMetaComments(List.of(";meta1", " \t;meta2"))
				.setChannelComments(List.of(";channel1", " \t;channel2"))
				.setFooterComments(List.of(";footer1", " \t;footer2")));
		assertOnWrite(textBin,
				";meta1",
				" \t;meta2",
				"#TITLE MySong",
				"",
				";channel1",
				" \t;channel2",
				"#00001:3",
				"#00007:XX",
				"",
				";footer1",
				" \t;footer2");
	}

	// onWrite(BmsContent, OutputStream)
	// コメント：先頭文字が"*"の時、コメント文字が付加されずに出力されること
	@Test
	public void testOnWrite_Comment_NotAddCommentCharIfAsterisk() throws Exception {
		var content = content(c -> {
			c.setSingleMeta("#title", "MySong");
			c.setMeasureValue(BmsInt.to36i("01"), 0, 3L);
			c.putNote(BmsInt.to36i("07"), 0, 0.0, BmsInt.to36i("XX"));
		});
		var textBin = write(content, new BmsStandardSaver()
				.setMetaComments(List.of("*meta1", " \t*meta2"))
				.setChannelComments(List.of("*channel1", " \t*channel2"))
				.setFooterComments(List.of("*footer1", " \t*footer2")));
		assertOnWrite(textBin,
				"*meta1",
				" \t*meta2",
				"#TITLE MySong",
				"",
				"*channel1",
				" \t*channel2",
				"#00001:3",
				"#00007:XX",
				"",
				"*footer1",
				" \t*footer2");
	}

	// onWrite(BmsContent, OutputStream)
	// コメント：先頭文字が"//"の時、コメント文字が付加されずに出力されること
	@Test
	public void testOnWrite_Comment_NotAddCommentCharIfClangComment() throws Exception {
		var content = content(c -> {
			c.setSingleMeta("#title", "MySong");
			c.setMeasureValue(BmsInt.to36i("01"), 0, 3L);
			c.putNote(BmsInt.to36i("07"), 0, 0.0, BmsInt.to36i("XX"));
		});
		var textBin = write(content, new BmsStandardSaver()
				.setMetaComments(List.of("//meta1", " \t//meta2"))
				.setChannelComments(List.of("//channel1", " \t//channel2"))
				.setFooterComments(List.of("//footer1", " \t//footer2")));
		assertOnWrite(textBin,
				"//meta1",
				" \t//meta2",
				"#TITLE MySong",
				"",
				"//channel1",
				" \t//channel2",
				"#00001:3",
				"#00007:XX",
				"",
				"//footer1",
				" \t//footer2");
	}

	// onWrite(BmsContent, OutputStream)
	// コメント：空白文字のみで構成される場合、空行が出力されること
	@Test
	public void testOnWrite_Comment_EmptyLineIfOnlyWhiteSpaces() throws Exception {
		var content = content(c -> {
			c.setSingleMeta("#title", "MySong");
			c.setMeasureValue(BmsInt.to36i("01"), 0, 3L);
			c.putNote(BmsInt.to36i("07"), 0, 0.0, BmsInt.to36i("XX"));
		});
		var textBin = write(content, new BmsStandardSaver()
				.setMetaComments(List.of(" \t  \t\t"))
				.setChannelComments(List.of(" \t  \t\t"))
				.setFooterComments(List.of(" \t  \t\t")));
		assertOnWrite(textBin,
				"",
				"#TITLE MySong",
				"",
				"",
				"#00001:3",
				"#00007:XX",
				"",
				"");
	}

	// onWrite(BmsContent, OutputStream)
	// コメント：空文字列の場合、空行が出力されること
	@Test
	public void testOnWrite_Comment_EmptyLineIfEmptyString() throws Exception {
		var content = content(c -> {
			c.setSingleMeta("#title", "MySong");
			c.setMeasureValue(BmsInt.to36i("01"), 0, 3L);
			c.putNote(BmsInt.to36i("07"), 0, 0.0, BmsInt.to36i("XX"));
		});
		var textBin = write(content, new BmsStandardSaver()
				.setMetaComments(List.of(""))
				.setChannelComments(List.of(""))
				.setFooterComments(List.of("")));
		assertOnWrite(textBin,
				"",
				"#TITLE MySong",
				"",
				"",
				"#00001:3",
				"#00007:XX",
				"",
				"");
	}

	// onWrite(BmsContent, OutputStream)
	// BOM：有効かつ非対応文字セットではBOM付加されないこと
	@Test
	public void testOnWrite_Bom_EnableAndUnsupportCharset() throws Exception {
		var content = content(c -> c.setSingleMeta("#title", "曲名"));
		var charset = Charset.forName("MS932");
		var textBin = write(content, new BmsStandardSaver().setCharsets(charset).setAddBom(true));
		assertOnWrite(textBin, 0, charset, "#TITLE 曲名");
	}

	// onWrite(BmsContent, OutputStream)
	// BOM：有効かつUTF-8でBOM付加されること
	@Test
	public void testOnWrite_Bom_EnableAndUtf8() throws Exception {
		var content = content(c -> c.setSingleMeta("#title", "曲名"));
		var charset = StandardCharsets.UTF_8;
		var bom = new byte[] { (byte)0xef, (byte)0xbb, (byte)0xbf };
		var textBin = write(content, new BmsStandardSaver().setCharsets(charset).setAddBom(true));
		assertTrue(Arrays.equals(textBin, 0, 3, bom, 0, 3));
		assertOnWrite(textBin, 3, charset, "#TITLE 曲名");
	}

	// onWrite(BmsContent, OutputStream)
	// BOM：有効かつUTF-16LEでBOM付加されること
	@Test
	public void testOnWrite_Bom_EnableAndUtf16le() throws Exception {
		var content = content(c -> c.setSingleMeta("#title", "曲名"));
		var charset = StandardCharsets.UTF_16LE;
		var bom = new byte[] { (byte)0xff, (byte)0xfe };
		var textBin = write(content, new BmsStandardSaver().setCharsets(charset).setAddBom(true));
		assertTrue(Arrays.equals(textBin, 0, 2, bom, 0, 2));
		assertOnWrite(textBin, 2, charset, "#TITLE 曲名");
	}

	// onWrite(BmsContent, OutputStream)
	// BOM：有効かつUTF-16BEでBOM付加されること
	@Test
	public void testOnWrite_Bom_EnableAndUtf16be() throws Exception {
		var content = content(c -> c.setSingleMeta("#title", "曲名"));
		var charset = StandardCharsets.UTF_16BE;
		var bom = new byte[] { (byte)0xfe, (byte)0xff };
		var textBin = write(content, new BmsStandardSaver().setCharsets(charset).setAddBom(true));
		assertTrue(Arrays.equals(textBin, 0, 2, bom, 0, 2));
		assertOnWrite(textBin, 2, charset, "#TITLE 曲名");
	}

	// onWrite(BmsContent, OutputStream)
	// BOM：無効かつUTF-8でBOM付加されないこと
	@Test
	public void testOnWrite_Bom_DisableAndUtf8() throws Exception {
		var content = content(c -> c.setSingleMeta("#title", "曲名"));
		var charset = StandardCharsets.UTF_8;
		var textBin = write(content, new BmsStandardSaver().setCharsets(charset).setAddBom(false));
		assertOnWrite(textBin, 0, charset, "#TITLE 曲名");
	}

	// onWrite(BmsContent, OutputStream)
	// BOM：無効かつUTF-16LEでBOM付加されないこと
	@Test
	public void testOnWrite_Bom_DisableAndUtf16le() throws Exception {
		var content = content(c -> c.setSingleMeta("#title", "曲名"));
		var charset = StandardCharsets.UTF_16LE;
		var textBin = write(content, new BmsStandardSaver().setCharsets(charset).setAddBom(false));
		assertOnWrite(textBin, 0, charset, "#TITLE 曲名");
	}

	// onWrite(BmsContent, OutputStream)
	// BOM：無効かつUTF-16BEでBOM付加されないこと
	@Test
	public void testOnWrite_Bom_DisableAndUtf16be() throws Exception {
		var content = content(c -> c.setSingleMeta("#title", "曲名"));
		var charset = StandardCharsets.UTF_16BE;
		var textBin = write(content, new BmsStandardSaver().setCharsets(charset).setAddBom(false));
		assertOnWrite(textBin, 0, charset, "#TITLE 曲名");
	}

	// onWrite(BmsContent, OutputStream)
	// IOException dstへのBMSコンテンツ出力時に入出力エラーが発生した
	@Test
	public void testOnWrite_IoError() throws Exception {
		var content = content(c -> {
			c.putDeclaration("key", "value");
		});
		assertThrows(IOException.class, () -> write(content, new IOExceptionOutputStream()));
	}

	// onWrite(BmsContent, OutputStream)
	// @throws BmsHandleException ユーザープログラムの処理異常を検出した
	@Test
	public void testOnWrite_Unexpected() throws Exception {
		var content = content(c -> {
			c.putDeclaration("key", "value");
		});
		var ex = assertThrows(BmsHandleException.class, () -> write(content, new RuntimeExceptionOutputStream()));
		assertEquals(RuntimeException.class, ex.getCause().getClass());
	}

	// isCompatible(BmsContent)
	// 制約のあるデータ型・インデックス値の全てが範囲内であればtrueを返すこと(選択基数16)
	@Test
	public void testIsCompatible_Ok_16() throws Exception {
		var c = BmsTest.loadContent(
				"#BASE 16",
				"#SBASE FF",
				"#SARRAY 00FF",
				"#IINTEGERFF 100",
				"#00017:FF",
				"#00006:00FF",
				"#00007:00ZZ",
				"#00018:00zz",
				"#00019:00FF",
				"#99901:100");
		assertTrue(BmsStandardSaver.isCompatible(c));
	}

	// isCompatible(BmsContent)
	// 制約のあるデータ型・インデックス値の全てが範囲内であればtrueを返すこと(選択基数36)
	@Test
	public void testIsCompatible_Ok_36() throws Exception {
		var c = BmsTest.loadContent(
				"#BASE 36",
				"#SBASE ZZ",
				"#SARRAY 00ZZ",
				"#IINTEGERZZ 100",
				"#00017:ZZ",
				"#00006:00FF",
				"#00007:00ZZ",
				"#00018:00zz",
				"#00019:00ZZ",
				"#99901:100");
		assertTrue(BmsStandardSaver.isCompatible(c));
	}

	// isCompatible(BmsContent)
	// 制約のあるデータ型・インデックス値の全てが範囲内であればtrueを返すこと(選択基数62)
	@Test
	public void testIsCompatible_Ok_62() throws Exception {
		var c = BmsTest.loadContent(
				"#BASE 62",
				"#SBASE zz",
				"#SARRAY 00zz",
				"#IINTEGERzz 100",
				"#00017:zz",
				"#00006:00FF",
				"#00007:00ZZ",
				"#00018:00zz",
				"#00019:00zz",
				"#99901:100");
		assertTrue(BmsStandardSaver.isCompatible(c));
	}

	// isCompatible(BmsContent)
	// 制約のあるデータ型・インデックス値の全てが範囲内であればtrueを返すこと(デフォルト基数)
	@Test
	public void testIsCompatible_Ok_Default() throws Exception {
		var c = BmsTest.loadContent(
				"#SBASE ZZ",
				"#SARRAY 00ZZ",
				"#IINTEGERZZ 100",
				"#00017:ZZ",
				"#00006:00FF",
				"#00007:00ZZ",
				"#00018:00zz",
				"#00019:00ZZ",
				"#99901:100");
		assertTrue(BmsStandardSaver.isCompatible(c));
	}

	// isCompatible(BmsContent)
	// 全ユーザーチャンネルの制約のあるデータ型で範囲外を示していてもtrueを返すこと(ユーザーチャンネルは検査対象外)
	@Test
	public void testIsCompatible_Ok_UserChannels() throws Exception {
		var c = new BmsContent(BmsTest.createTestSpec());
		c.beginEdit();
		//c.setMeasureValue(BmsSpec.USER_CHANNEL_MIN + 6, 0, BmsInt.to16i("FF") + 1); // 小節データのBASE16は超過不可
		//c.setMeasureValue(BmsSpec.USER_CHANNEL_MIN + 7, 0, BmsInt.to36i("ZZ") + 1); // 小節データのBASE36は超過不可
		//c.setMeasureValue(BmsSpec.USER_CHANNEL_MIN + 8, 0, BmsInt.to62i("zz") + 1); // 小節データのBASE62は超過不可
		c.setMeasureValue(BmsSpec.USER_CHANNEL_MIN + 9, 0, BmsInt.to36i("ZZ") + 1);
		c.putNote(BmsSpec.USER_CHANNEL_MIN + 10, 0, 0.0, BmsInt.to16i("FF") + 1);
		c.putNote(BmsSpec.USER_CHANNEL_MIN + 11, 0, 0.0, BmsInt.to36i("ZZ") + 1);
		c.putNote(BmsSpec.USER_CHANNEL_MIN + 12, 0, 0.0, BmsInt.to62i("zz") + 1);
		c.putNote(BmsSpec.USER_CHANNEL_MIN + 13, 0, 0.0, BmsInt.to36i("ZZ") + 1);
		c.endEdit();
		assertTrue(BmsStandardSaver.isCompatible(c));
	}

	// isCompatible(BmsContent)
	// BASEのメタ情報が最大値を超過していたらfalseを返すこと(選択基数16)
	@Test
	public void testIsCompatible_Meta_Base_Overflow_16() throws Exception {
		var c = BmsTest.loadContent("#BASE 16");
		c.edit(() -> c.setSingleMeta("#sbase", BmsInt.to16i("FF") + 0));
		assertTrue(BmsStandardSaver.isCompatible(c));
		c.edit(() -> c.setSingleMeta("#sbase", BmsInt.to16i("FF") + 1));
		assertFalse(BmsStandardSaver.isCompatible(c));
	}

	// isCompatible(BmsContent)
	// BASEのメタ情報が最大値を超過していたらfalseを返すこと(選択基数36)
	@Test
	public void testIsCompatible_Meta_Base_Overflow_36() throws Exception {
		var c = BmsTest.loadContent("#BASE 36");
		c.edit(() -> c.setSingleMeta("#sbase", BmsInt.to36i("ZZ") + 0));
		assertTrue(BmsStandardSaver.isCompatible(c));
		c.edit(() -> c.setSingleMeta("#sbase", BmsInt.to36i("ZZ") + 1));
		assertFalse(BmsStandardSaver.isCompatible(c));
	}

	// isCompatible(BmsContent)
	// BASEのメタ情報が最大値を超過していたらfalseを返すこと(選択基数62)
	@Test
	public void testIsCompatible_Meta_Base_Overflow_62() throws Exception {
		var c = BmsTest.loadContent("#BASE 62");
		c.edit(() -> c.setSingleMeta("#sbase", BmsInt.to62i("zz")));
		assertTrue(BmsStandardSaver.isCompatible(c));
		// ※BASE型は62進数の最大値を超過できない
	}

	// isCompatible(BmsContent)
	// BASEのメタ情報が最大値を超過していたらfalseを返すこと(デフォルト基数)
	@Test
	public void testIsCompatible_Meta_Base_Overflow_Default() throws Exception {
		var c = new BmsContent(BmsTest.createTestSpec());
		c.edit(() -> c.setSingleMeta("#sbase", BmsInt.to36i("ZZ") + 0));
		assertTrue(BmsStandardSaver.isCompatible(c));
		c.edit(() -> c.setSingleMeta("#sbase", BmsInt.to36i("ZZ") + 1));
		assertFalse(BmsStandardSaver.isCompatible(c));
	}

	// isCompatible(BmsContent)
	// ARRAYのメタ情報の配列基数が選択基数を超過していたらfalseを返すこと(選択基数16)
	@Test
	public void testIsCompatible_Meta_Array_OverBase_16() throws Exception {
		var c = BmsTest.loadContent("#BASE 16");
		c.edit(() -> c.setSingleMeta("#sarray", new BmsArray("FF", 16)));
		assertTrue(BmsStandardSaver.isCompatible(c));
		c.edit(() -> c.setSingleMeta("#sarray", new BmsArray("ZZ", 36)));
		assertFalse(BmsStandardSaver.isCompatible(c));
		c.edit(() -> c.setSingleMeta("#sarray", new BmsArray("zz", 62)));
		assertFalse(BmsStandardSaver.isCompatible(c));
	}

	// isCompatible(BmsContent)
	// ARRAYのメタ情報の配列基数が選択基数を超過していたらfalseを返すこと(選択基数36)
	@Test
	public void testIsCompatible_Meta_Array_OverBase_36() throws Exception {
		var c = BmsTest.loadContent("#BASE 36");
		c.edit(() -> c.setSingleMeta("#sarray", new BmsArray("FF", 16)));
		assertTrue(BmsStandardSaver.isCompatible(c));
		c.edit(() -> c.setSingleMeta("#sarray", new BmsArray("ZZ", 36)));
		assertTrue(BmsStandardSaver.isCompatible(c));
		c.edit(() -> c.setSingleMeta("#sarray", new BmsArray("zz", 62)));
		assertFalse(BmsStandardSaver.isCompatible(c));
	}

	// isCompatible(BmsContent)
	// ARRAYのメタ情報の配列基数が選択基数を超過していたらfalseを返すが、選択基数62では常にtrueになること
	@Test
	public void testIsCompatible_Meta_Array_OverBase_62() throws Exception {
		var c = BmsTest.loadContent("#BASE 62");
		c.edit(() -> c.setSingleMeta("#sarray", new BmsArray("FF", 16)));
		assertTrue(BmsStandardSaver.isCompatible(c));
		c.edit(() -> c.setSingleMeta("#sarray", new BmsArray("ZZ", 36)));
		assertTrue(BmsStandardSaver.isCompatible(c));
		c.edit(() -> c.setSingleMeta("#sarray", new BmsArray("zz", 62)));
		assertTrue(BmsStandardSaver.isCompatible(c));
		// ※基数62ではfalseにはならない
	}

	// isCompatible(BmsContent)
	// ARRAYのメタ情報の配列基数が選択基数を超過していたらfalseを返すこと(デフォルト基数)
	@Test
	public void testIsCompatible_Meta_Array_OverBase_Default() throws Exception {
		var c = new BmsContent(BmsTest.createTestSpec());
		c.edit(() -> c.setSingleMeta("#sarray", new BmsArray("FF", 16)));
		assertTrue(BmsStandardSaver.isCompatible(c));
		c.edit(() -> c.setSingleMeta("#sarray", new BmsArray("ZZ", 36)));
		assertTrue(BmsStandardSaver.isCompatible(c));
		c.edit(() -> c.setSingleMeta("#sarray", new BmsArray("zz", 62)));
		assertFalse(BmsStandardSaver.isCompatible(c));
	}

	// isCompatible(BmsContent)
	// 索引付きメタ情報のインデックス値が最大値を超過していたらfalseを返すこと(選択基数16)
	@Test
	public void testIsCompatible_Meta_Index_Overflow_16() throws Exception {
		var c = BmsTest.loadContent("#BASE 16");
		c.edit(() -> c.setIndexedMeta("#iinteger", BmsInt.to16i("FF") + 0, 0));
		assertTrue(BmsStandardSaver.isCompatible(c));
		c.edit(() -> c.setIndexedMeta("#iinteger", BmsInt.to16i("FF") + 1, 0));
		assertFalse(BmsStandardSaver.isCompatible(c));
	}

	// isCompatible(BmsContent)
	// 索引付きメタ情報のインデックス値が最大値を超過していたらfalseを返すこと(選択基数36)
	@Test
	public void testIsCompatible_Meta_Index_Overflow_36() throws Exception {
		var c = BmsTest.loadContent("#BASE 36");
		c.edit(() -> c.setIndexedMeta("#iinteger", BmsInt.to36i("ZZ") + 0, 0));
		assertTrue(BmsStandardSaver.isCompatible(c));
		c.edit(() -> c.setIndexedMeta("#iinteger", BmsInt.to36i("ZZ") + 1, 0));
		assertFalse(BmsStandardSaver.isCompatible(c));
	}

	// isCompatible(BmsContent)
	// 索引付きメタ情報のインデックス値が最大値を超過していたらfalseを返すこと(選択基数62)
	@Test
	public void testIsCompatible_Meta_Index_Overflow_62() throws Exception {
		var c = BmsTest.loadContent("#BASE 62");
		c.edit(() -> c.setIndexedMeta("#iinteger", BmsInt.to62i("zz") + 0, 0));
		assertTrue(BmsStandardSaver.isCompatible(c));
		c.edit(() -> c.setIndexedMeta("#iinteger", BmsInt.to62i("zz") + 1, 0));
		assertFalse(BmsStandardSaver.isCompatible(c));
	}

	// isCompatible(BmsContent)
	// 索引付きメタ情報のインデックス値が最大値を超過していたらfalseを返すこと(デフォルト基数)
	@Test
	public void testIsCompatible_Meta_Index_Overflow_Default() throws Exception {
		var c = new BmsContent(BmsTest.createTestSpec());
		c.edit(() -> c.setIndexedMeta("#iinteger", BmsInt.to36i("ZZ") + 0, 0));
		assertTrue(BmsStandardSaver.isCompatible(c));
		c.edit(() -> c.setIndexedMeta("#iinteger", BmsInt.to36i("ZZ") + 1, 0));
		assertFalse(BmsStandardSaver.isCompatible(c));
	}

	// isCompatible(BmsContent)
	// 任意型の索引付きメタ情報のインデックス値が最大値を超過していてもtrueを返すこと(任意型は出力されないため検査対象外)
	@Test
	public void testIsCompatible_Meta_Index_Overflow_Object() throws Exception {
		var c = BmsTest.loadContent("#BASE 62");
		c.edit(() -> c.setIndexedMeta("#iobject", BmsInt.to62i("zz") + 0, ""));
		assertTrue(BmsStandardSaver.isCompatible(c));
		c.edit(() -> c.setIndexedMeta("#iobject", BmsInt.to62i("zz") + 1, ""));
		assertTrue(BmsStandardSaver.isCompatible(c));
	}

	// isCompatible(BmsContent)
	// 小節数が上限を超えていたらfalseを返すこと
	@Test
	public void testIsCompatible_MeasureCount_Overflow() throws Exception {
		var c = new BmsContent(BmsTest.createTestSpec());
		c.edit(() -> c.putNote(7, BmsStandardSaver.MEASURE_COUNT_MAX - 1, 0.0, 1));
		assertTrue(BmsStandardSaver.isCompatible(c));
		c.edit(() -> c.putNote(7, BmsStandardSaver.MEASURE_COUNT_MAX - 0, 0.0, 1));
		assertFalse(BmsStandardSaver.isCompatible(c));
	}

	// isCompatible(BmsContent)
	// BASEの小節データが最大値を超過していたらfalseを返すこと(選択基数16)
	@Test
	public void testIsCompatible_MeasureValue_Base_Overflow_16() throws Exception {
		var c = BmsTest.loadContent("#BASE 16");
		c.edit(() -> c.setMeasureValue(BmsInt.to36i("17"), 0, BmsInt.to16i("FF") + 0));
		assertTrue(BmsStandardSaver.isCompatible(c));
		c.edit(() -> c.setMeasureValue(BmsInt.to36i("17"), 0, BmsInt.to16i("FF") + 1));
		assertFalse(BmsStandardSaver.isCompatible(c));
	}

	// isCompatible(BmsContent)
	// BASEの小節データが最大値を超過していたらfalseを返すこと(選択基数36)
	@Test
	public void testIsCompatible_MeasureValue_Base_Overflow_36() throws Exception {
		var c = BmsTest.loadContent("#BASE 36");
		c.edit(() -> c.setMeasureValue(BmsInt.to36i("17"), 0, BmsInt.to36i("ZZ") + 0));
		assertTrue(BmsStandardSaver.isCompatible(c));
		c.edit(() -> c.setMeasureValue(BmsInt.to36i("17"), 0, BmsInt.to36i("ZZ") + 1));
		assertFalse(BmsStandardSaver.isCompatible(c));
	}

	// isCompatible(BmsContent)
	// BASEの小節データが最大値を超過していたらfalseを返すこと(選択基数62)
	@Test
	public void testIsCompatible_MeasureValue_Base_Overflow_62() throws Exception {
		var c = BmsTest.loadContent("#BASE 62");
		c.edit(() -> c.setMeasureValue(BmsInt.to36i("17"), 0, BmsInt.to62i("zz") + 0));
		assertTrue(BmsStandardSaver.isCompatible(c));
		// ※BASE型は62進数の最大値を超過できない
	}

	// isCompatible(BmsContent)
	// BASEの小節データが最大値を超過していたらfalseを返すこと(デフォルト基数)
	@Test
	public void testIsCompatible_MeasureValue_Base_Overflow_Default() throws Exception {
		var c = new BmsContent(BmsTest.createTestSpec());
		c.edit(() -> c.setMeasureValue(BmsInt.to36i("17"), 0, BmsInt.to36i("ZZ") + 0));
		assertTrue(BmsStandardSaver.isCompatible(c));
		c.edit(() -> c.setMeasureValue(BmsInt.to36i("17"), 0, BmsInt.to36i("ZZ") + 1));
		assertFalse(BmsStandardSaver.isCompatible(c));
	}

	// isCompatible(BmsContent)
	// ARRAY16のノートが範囲外の値であればfalseを返すこと
	@Test
	public void testIsCompatible_Note_Array16_OutRange() throws Exception {
		var c = new BmsContent(BmsTest.createTestSpec());
		c.edit(() -> c.putNote(6, 0, 0.0, -1));
		assertFalse(BmsStandardSaver.isCompatible(c));
		c.edit(() -> c.putNote(6, 0, 0.0, BmsInt.to16i("FF") + 0));
		assertTrue(BmsStandardSaver.isCompatible(c));
		c.edit(() -> c.putNote(6, 0, 0.0, BmsInt.to16i("FF") + 1));
		assertFalse(BmsStandardSaver.isCompatible(c));
	}

	// isCompatible(BmsContent)
	// ARRAY36のノートが範囲外の値であればfalseを返すこと
	@Test
	public void testIsCompatible_Note_Array36_OutRange() throws Exception {
		var c = new BmsContent(BmsTest.createTestSpec());
		c.edit(() -> c.putNote(7, 0, 0.0, -1));
		assertFalse(BmsStandardSaver.isCompatible(c));
		c.edit(() -> c.putNote(7, 0, 0.0, BmsInt.to36i("ZZ") + 0));
		assertTrue(BmsStandardSaver.isCompatible(c));
		c.edit(() -> c.putNote(7, 0, 0.0, BmsInt.to36i("ZZ") + 1));
		assertFalse(BmsStandardSaver.isCompatible(c));
	}

	// isCompatible(BmsContent)
	// ARRAY62のノートが範囲外の値であればfalseを返すこと
	@Test
	public void testIsCompatible_Note_Array62_OutRange() throws Exception {
		var c = new BmsContent(BmsTest.createTestSpec());
		c.edit(() -> c.putNote(BmsInt.to36i("18"), 0, 0.0, -1));
		assertFalse(BmsStandardSaver.isCompatible(c));
		c.edit(() -> c.putNote(BmsInt.to36i("18"), 0, 0.0, BmsInt.to62i("zz") + 0));
		assertTrue(BmsStandardSaver.isCompatible(c));
		c.edit(() -> c.putNote(BmsInt.to36i("18"), 0, 0.0, BmsInt.to62i("zz") + 1));
		assertFalse(BmsStandardSaver.isCompatible(c));
	}

	// isCompatible(BmsContent)
	// ARRAYのノートが範囲外の値であればfalseを返すこと(選択基数16)
	@Test
	public void testIsCompatible_Note_Array_OutRange_16() throws Exception {
		var c = BmsTest.loadContent("#BASE 16");
		c.edit(() -> c.putNote(BmsInt.to36i("19"), 0, 0.0, -1));
		assertFalse(BmsStandardSaver.isCompatible(c));
		c.edit(() -> c.putNote(BmsInt.to36i("19"), 0, 0.0, BmsInt.to16i("FF") + 0));
		assertTrue(BmsStandardSaver.isCompatible(c));
		c.edit(() -> c.putNote(BmsInt.to36i("19"), 0, 0.0, BmsInt.to16i("FF") + 1));
		assertFalse(BmsStandardSaver.isCompatible(c));
	}

	// isCompatible(BmsContent)
	// ARRAYのノートが範囲外の値であればfalseを返すこと(選択基数36)
	@Test
	public void testIsCompatible_Note_Array_OutRange_36() throws Exception {
		var c = BmsTest.loadContent("#BASE 36");
		c.edit(() -> c.putNote(BmsInt.to36i("19"), 0, 0.0, -1));
		assertFalse(BmsStandardSaver.isCompatible(c));
		c.edit(() -> c.putNote(BmsInt.to36i("19"), 0, 0.0, BmsInt.to36i("ZZ") + 0));
		assertTrue(BmsStandardSaver.isCompatible(c));
		c.edit(() -> c.putNote(BmsInt.to36i("19"), 0, 0.0, BmsInt.to36i("ZZ") + 1));
		assertFalse(BmsStandardSaver.isCompatible(c));
	}

	// isCompatible(BmsContent)
	// ARRAYのノートが範囲外の値であればfalseを返すこと(選択基数62)
	@Test
	public void testIsCompatible_Note_Array_OutRange_62() throws Exception {
		var c = BmsTest.loadContent("#BASE 62");
		c.edit(() -> c.putNote(BmsInt.to36i("19"), 0, 0.0, -1));
		assertFalse(BmsStandardSaver.isCompatible(c));
		c.edit(() -> c.putNote(BmsInt.to36i("19"), 0, 0.0, BmsInt.to62i("zz") + 0));
		assertTrue(BmsStandardSaver.isCompatible(c));
		c.edit(() -> c.putNote(BmsInt.to36i("19"), 0, 0.0, BmsInt.to62i("zz") + 1));
		assertFalse(BmsStandardSaver.isCompatible(c));
	}

	// isCompatible(BmsContent)
	// ARRAYのノートが範囲外の値であればfalseを返すこと(デフォルト基数)
	@Test
	public void testIsCompatible_Note_Array_OutRange_Default() throws Exception {
		var c = new BmsContent(BmsTest.createTestSpec());
		c.edit(() -> c.putNote(BmsInt.to36i("19"), 0, 0.0, -1));
		assertFalse(BmsStandardSaver.isCompatible(c));
		c.edit(() -> c.putNote(BmsInt.to36i("19"), 0, 0.0, BmsInt.to36i("ZZ") + 0));
		assertTrue(BmsStandardSaver.isCompatible(c));
		c.edit(() -> c.putNote(BmsInt.to36i("19"), 0, 0.0, BmsInt.to36i("ZZ") + 1));
		assertFalse(BmsStandardSaver.isCompatible(c));
	}

	// isCompatible(BmsContent)
	// NullPointerException contentがnull
	@Test
	public void testIsCompatible_NullContent() throws Exception {
		assertThrows(NullPointerException.class, () -> BmsStandardSaver.isCompatible(null));
	}

	// isCompatible(BmsContent)
	// IllegalArgumentException contentが編集モード
	@Test
	public void testIsCompatible_EditMode() throws Exception {
		var c = new BmsContent(BmsTest.createTestSpec());
		c.beginEdit();
		assertThrows(IllegalArgumentException.class, () -> BmsStandardSaver.isCompatible(c));
	}

	private static BmsContent content(Consumer<BmsContent> editor) {
		var s = BmsTest.createTestSpec();
		var c = new BmsContent(s);
		c.beginEdit();
		editor.accept(c);
		c.endEdit();
		return c;
	}

	private static byte[] write(BmsContent content, BmsStandardSaver saver) throws Exception {
		var os = new ByteArrayOutputStream();
		saver.save(content, os);
		return os.toByteArray();
	}

	private static byte[] write(BmsContent content, OutputStream os) throws Exception {
		new BmsStandardSaver().save(content, os);
		return null;
	}

	private static void assertOnWrite(byte[] bin, String...expectLines) throws Exception {
		assertOnWriteMain(bin, 0, bin.length, Charset.forName("MS932"), expectLines);
	}

	private static void assertOnWrite(byte[] bin, Charset cs, String...expectLines) throws Exception {
		assertOnWriteMain(bin, 0, bin.length, cs, expectLines);
	}

	private static void assertOnWrite(byte[] bin, int offset, Charset cs, String...expectLines) throws Exception {
		assertOnWriteMain(bin, offset, bin.length - offset, cs, expectLines);
	}

	private static void assertOnWriteMain(byte[] bin, int offset, int length, Charset cs, String[] expectLines) throws Exception {
		var stackTrace = Thread.currentThread().getStackTrace()[3];
		var testMethod = stackTrace.getClassName() + "#" + stackTrace.getMethodName();
		var is = new ByteArrayInputStream(bin, offset, length);
		var isr = new InputStreamReader(is, cs);
		var reader = new BufferedReader(isr);
		var actuals = reader.lines().collect(Collectors.toList());
		var expects = Arrays.asList(expectLines);
		var actualCount = actuals.size();
		var expectCount = expects.size();

		if (actualCount != expectCount) {
			dumpBmsIo(testMethod, expects, actuals, Collections.emptySet());
			fail(String.format("Expects %d lines, but %d.", expectCount, actualCount));
		}

		var diffs = new HashSet<Integer>();
		for (var i = 0; i < actualCount; i++) {
			if (!actuals.get(i).equals(expects.get(i))) { diffs.add(i); }
		}
		if (!diffs.isEmpty()) {
			dumpBmsIo(testMethod, expects, actuals, diffs);
			fail(String.format("Found %d differences.", diffs.size()));
		}
	}

	private static void dumpBmsIo(String testMethod, List<String> e, List<String> a, Set<Integer> diffs) {
		System.out.println(testMethod + "()");
		System.out.println("[EXPECT]");
		IntStream.range(0, e.size()).forEach(i -> System.out.printf("  L%03d %s\n", (i + 1), e.get(i)));
		System.out.println("[ACTUAL]");
		IntStream.range(0, a.size()).forEach(i -> System.out.printf("%c L%03d %s\n", (diffs.contains(i) ? '*' : ' '), (i + 1), a.get(i)));
		System.out.println();
	}
}
