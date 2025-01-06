package com.lmt.lib.bms;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.lmt.lib.bms.parse.BmsErrorParsed;
import com.lmt.lib.bms.parse.BmsParsed;
import com.lmt.lib.bms.parse.BmsSource;
import com.lmt.lib.bms.parse.BmsTestResult;

public class BmsLoaderTest extends BmsLoaderTestBase {
	private static class ErrorStopLoadHandler implements BmsLoadHandler {
		// デフォルト実装ではエラー発生時は常時読み込み停止なので何も実装しない
	}
	private static class NullContentLoadHandler implements BmsLoadHandler {
		@Override public BmsContent createContent(BmsSpec spec) { return null; }
	}
	private static class EmptyInputStream extends InputStream {
		@Override public int read() throws IOException { return -1; }
	}
	private static class IOExceptionInputStream extends InputStream {
		@Override public int read() throws IOException { throw new IOException(); }
	}
	private static class IOExceptionReader extends Reader {
		@Override public int read(char[] cbuf, int off, int len) throws IOException { throw new IOException(); }
		@Override public void close() throws IOException {}
	}
	private static class TestLoaderBody extends BmsLoader {
		BmsSource source = null;
		TestLoaderBody(boolean isStandard, boolean isBinaryFormat) { super(isStandard, isBinaryFormat); }
		@Override protected BmsErrorParsed beginParse(BmsLoaderSettings settings, BmsSource source) {
			this.source = source; return BmsErrorParsed.PASS;
		}
		@Override protected BmsErrorParsed endParse() { return BmsErrorParsed.PASS; }
		@Override protected BmsParsed nextElement() { return null; }
	}
	private static class TestParserLoader extends BmsLoader {
		@Override protected BmsErrorParsed beginParse(BmsLoaderSettings settings, BmsSource source) { return BmsErrorParsed.PASS; }
		@Override protected BmsErrorParsed endParse() { return BmsErrorParsed.PASS; }
		@Override protected BmsParsed nextElement() { return null; }
	}
	@FunctionalInterface
	private interface GetLastProcessedXRunner {
		void run(BmsLoader l, Path p) throws Exception;
	}

	private static final String TEST_FILE_EMPTY = "testLoad_Empty.bms";
	private static final String TEST_FILE_SYNTAX_ERROR = "testLoad_SyntaxError.bms";
	private static final BmsLoadHandler HANDLER_ERR_STOP = new ErrorStopLoadHandler();
	private static final BmsLoadHandler HANDLER_NULL_CONTENT = new NullContentLoadHandler();
	private static final String TITLE_FOR_TEST_DECODE = "MySong～デコードテストの為の～";
	private static final String TITLE_FOR_TEST_DECODE_START = "MySong";

	private static List<Charset> sOrgCharsets;

	@Override
	public Path testDataDirectory() {
		return Path.of("test", "com", "lmt", "lib", "bms", "testdata", "bmsloader");
	}

	@Override
	public String testDataExtension() {
		return "bms";
	}

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		// テスト実行前の文字セットリストを退避する
		sOrgCharsets = BmsLibrary.getDefaultCharsets();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		// テスト実行前の文字セットリストを復元する
		BmsLibrary.setDefaultCharsets(sOrgCharsets.toArray(Charset[]::new));
	}

	// BmsLoader()
	// デフォルト設定ではエラーに対して寛容な設定になっていること
	@Test
	public void testBmsLoader_TorelantSettings() throws Exception {
		var l = new BmsStandardLoader();
		var s = (BmsLoaderSettings)Tests.getf(l, "mSettings");
		assertFalse(s.isSyntaxErrorEnable());
		assertTrue(s.isFixSpecViolation());
		assertTrue(s.isIgnoreUnknownMeta());
		assertTrue(s.isIgnoreUnknownChannel());
		assertTrue(s.isIgnoreWrongData());
	}

	// isStandard()
	// コンストラクタで指定した値が戻り値になること
	@Test
	public void testIsStandard() {
		var l1 = new TestLoaderBody(false, true);
		assertFalse(l1.isStandard());
		var l2 = new TestLoaderBody(true, false);
		assertTrue(l2.isStandard());
	}

	// isBinaryFormat()
	// コンストラクタで指定した値が戻り値になること
	@Test
	public void testIsBinaryFormat() {
		var l1 = new TestLoaderBody(true, false);
		assertFalse(l1.isBinaryFormat());
		var l2 = new TestLoaderBody(false, true);
		assertTrue(l2.isBinaryFormat());
	}

	// getLastProcessedCharset() / getLastProcessedHasBom()
	// ローダの初期状態では charset=null, hasBom=false になっていること
	@Test
	public void testGetLastProcessedX_Initial() {
		var l = new BmsStandardLoader();
		assertNull(l.getLastProcessedCharset());
		assertFalse(l.getLastProcessedHasBom());
	}

	// getLastProcessedCharset() / getLastProcessedHasBom()
	// BOM付きUTF-8を読み込むと charset=UTF-8, hasBom=true になること
	@Test
	public void testGetLastProcessedX_Utf8Bom() throws Exception {
		var path = testDataPath();
		var cs = StandardCharsets.UTF_8;
		var bom = true;
		testGetLastProcessedX_Run(path, cs, bom, (l, p) -> l.load(p.toFile()));
		testGetLastProcessedX_Run(path, cs, bom, (l, p) -> l.load(p));
		testGetLastProcessedX_Run(path, cs, bom, (l, p) -> l.load(Files.newInputStream(p)));
		testGetLastProcessedX_Run(path, cs, bom, (l, p) -> l.load(Files.readAllBytes(p)));
	}

	// getLastProcessedCharset() / getLastProcessedHasBom()
	// BOM付きUTF-16LEを読み込むと charset=UTF-16LE, hasBom=true になること
	@Test
	public void testGetLastProcessedX_Utf16leBom() throws Exception {
		var path = testDataPath();
		var cs = StandardCharsets.UTF_16LE;
		var bom = true;
		testGetLastProcessedX_Run(path, cs, bom, (l, p) -> l.load(p.toFile()));
		testGetLastProcessedX_Run(path, cs, bom, (l, p) -> l.load(p));
		testGetLastProcessedX_Run(path, cs, bom, (l, p) -> l.load(Files.newInputStream(p)));
		testGetLastProcessedX_Run(path, cs, bom, (l, p) -> l.load(Files.readAllBytes(p)));
	}

	// getLastProcessedCharset() / getLastProcessedHasBom()
	// BOM付きUTF-16BEを読み込むと charset=UTF-16BE, hasBom=true になること
	@Test
	public void testGetLastProcessedX_Utf16beBom() throws Exception {
		var path = testDataPath();
		var cs = StandardCharsets.UTF_16BE;
		var bom = true;
		testGetLastProcessedX_Run(path, cs, bom, (l, p) -> l.load(p.toFile()));
		testGetLastProcessedX_Run(path, cs, bom, (l, p) -> l.load(p));
		testGetLastProcessedX_Run(path, cs, bom, (l, p) -> l.load(Files.newInputStream(p)));
		testGetLastProcessedX_Run(path, cs, bom, (l, p) -> l.load(Files.readAllBytes(p)));
	}

	// getLastProcessedCharset() / getLastProcessedHasBom()
	// ローダに指定した優先文字セットで読み込むと charset=優先文字セット, hasBom=false になること
	@Test
	public void testGetLastProcessedX_PrimaryCharset() throws Exception {
		var path = testDataPath();
		var css = new Charset[] { StandardCharsets.UTF_8, Charset.forName("MS932") };
		var cs = StandardCharsets.UTF_8;
		var bom = false;
		testGetLastProcessedX_Run(path, cs, bom, (l, p) -> { l.setCharsets(css); l.load(p.toFile()); });
		testGetLastProcessedX_Run(path, cs, bom, (l, p) -> { l.setCharsets(css); l.load(p); });
		testGetLastProcessedX_Run(path, cs, bom, (l, p) -> { l.setCharsets(css); l.load(Files.newInputStream(p)); });
		testGetLastProcessedX_Run(path, cs, bom, (l, p) -> { l.setCharsets(css); l.load(Files.readAllBytes(p)); });
	}

	// getLastProcessedCharset() / getLastProcessedHasBom()
	// ローダに指定した2番目の文字セットで読み込むと charset=2番目の文字セット, hasBom=false になること
	@Test
	public void testGetLastProcessedX_SecondaryCharset() throws Exception {
		var path = testDataPath();
		var css = new Charset[] { StandardCharsets.UTF_8, Charset.forName("MS932") };
		var cs = Charset.forName("MS932");
		var bom = false;
		testGetLastProcessedX_Run(path, cs, bom, (l, p) -> { l.setCharsets(css); l.load(p.toFile()); });
		testGetLastProcessedX_Run(path, cs, bom, (l, p) -> { l.setCharsets(css); l.load(p); });
		testGetLastProcessedX_Run(path, cs, bom, (l, p) -> { l.setCharsets(css); l.load(Files.newInputStream(p)); });
		testGetLastProcessedX_Run(path, cs, bom, (l, p) -> { l.setCharsets(css); l.load(Files.readAllBytes(p)); });
	}

	// getLastProcessedCharset() / getLastProcessedHasBom()
	// 文字セット未指定、デフォルト優先文字セットで読み込むと charset=デフォルト優先文字セット, hasBom=false になること
	@Test
	public void testGetLastProcessedX_DefaultPrimaryCharset() throws Exception {
		var path = testDataPath();
		var orgCss = BmsLibrary.getDefaultCharsets().toArray(Charset[]::new);
		var css = new Charset[] { StandardCharsets.UTF_8, Charset.forName("MS932") };
		var cs = StandardCharsets.UTF_8;
		var bom = false;
		try {
			BmsLibrary.setDefaultCharsets(css);
			testGetLastProcessedX_Run(path, cs, bom, (l, p) -> l.load(p.toFile()));
			testGetLastProcessedX_Run(path, cs, bom, (l, p) -> l.load(p));
			testGetLastProcessedX_Run(path, cs, bom, (l, p) -> l.load(Files.newInputStream(p)));
			testGetLastProcessedX_Run(path, cs, bom, (l, p) -> l.load(Files.readAllBytes(p)));
		} finally {
			BmsLibrary.setDefaultCharsets(orgCss);
		}
	}

	// getLastProcessedCharset() / getLastProcessedHasBom()
	// 文字セット未指定、デフォルト2番目文字セットで読み込むと charset=デフォルト2番目文字セット, hasBom=false になること
	@Test
	public void testGetLastProcessedX_DefaultSecondaryCharset() throws Exception {
		var path = testDataPath();
		var orgCss = BmsLibrary.getDefaultCharsets().toArray(Charset[]::new);
		var css = new Charset[] { StandardCharsets.UTF_8, Charset.forName("MS932") };
		var cs = Charset.forName("MS932");
		var bom = false;
		try {
			BmsLibrary.setDefaultCharsets(css);
			testGetLastProcessedX_Run(path, cs, bom, (l, p) -> l.load(p.toFile()));
			testGetLastProcessedX_Run(path, cs, bom, (l, p) -> l.load(p));
			testGetLastProcessedX_Run(path, cs, bom, (l, p) -> l.load(Files.newInputStream(p)));
			testGetLastProcessedX_Run(path, cs, bom, (l, p) -> l.load(Files.readAllBytes(p)));
		} finally {
			BmsLibrary.setDefaultCharsets(orgCss);
		}
	}

	// getLastProcessedCharset() / getLastProcessedHasBom()
	// バイナリフォーマットのローダで読み込むと charset=null, hasBom=false になること
	@Test
	public void testGetLastProcessedX_BinaryFormat() throws Exception {
		var b = new byte[] { 'a' };
		var l = new TestLoaderBody(false, true).setSpec(spec()).setCharsets(StandardCharsets.UTF_8);
		l.load(b);
		assertNull(l.getLastProcessedCharset());
		assertFalse(l.getLastProcessedHasBom());
	}

	// getLastProcessedCharset() / getLastProcessedHasBom()
	// load(Reader)で読み込むと charset=null, hasBom=false になること
	@Test
	public void testGetLastProcessedX_LoadReader() throws Exception {
		var l = loader();
		Tests.setf(l, "mLastProcessedCharset", Charset.forName("MS932"));
		Tests.setf(l, "mLastProcessedHasBom", true);
		l.load(Files.newBufferedReader(testDataPath(), StandardCharsets.UTF_8));
		assertNull(l.getLastProcessedCharset());
		assertFalse(l.getLastProcessedHasBom());
	}

	// getLastProcessedCharset() / getLastProcessedHasBom()
	// load(String)で読み込むと charset=null, hasBom=false になること
	@Test
	public void testGetLastProcessedX_LoadString() throws Exception {
		var l = loader();
		Tests.setf(l, "mLastProcessedCharset", Charset.forName("MS932"));
		Tests.setf(l, "mLastProcessedHasBom", true);
		l.load(Files.lines(testDataPath()).collect(Collectors.joining("\n")));
		assertNull(l.getLastProcessedCharset());
		assertFalse(l.getLastProcessedHasBom());
	}

	// getLastProcessedCharset() / getLastProcessedHasBom()
	// 読み込み処理で例外がスローされると前回の値から更新されないこと
	@Test
	public void testGetLastProcessedX_ThrownException() throws Exception {
		var cs = Charset.forName("MS932");
		var bom = true;
		var runners = new GetLastProcessedXRunner[] {
				(l, p) -> l.load((File)null),
				(l, p) -> l.load((Path)null),
				(l, p) -> l.load((InputStream)null),
				(l, p) -> l.load((byte[])null) };
		for (var runner : runners) {
			var l = loader();
			Tests.setf(l, "mLastProcessedCharset", cs);
			Tests.setf(l, "mLastProcessedHasBom", bom);
			assertThrows(NullPointerException.class, () -> runner.run(l, Path.of("not_found")));
			assertEquals(cs, l.getLastProcessedCharset());
			assertEquals(bom, l.getLastProcessedHasBom());
		}
	}

	private void testGetLastProcessedX_Run(
			Path path, Charset csExpect, boolean bomExpect, GetLastProcessedXRunner runner) throws Exception {
		var l = loader();
		runner.run(l, path);
		assertEquals(csExpect, l.getLastProcessedCharset());
		assertEquals(bomExpect, l.getLastProcessedHasBom());
	}

	// setSpec(BmsSpec)
	// 正常
	@Test
	public void testSetSpec_001() throws Exception {
		var s = spec();
		var l = new BmsStandardLoader();
		l.setSpec(s);
		assertTrue(Tests.getf(l, "mSpec") == s);
	}

	// setSpec(BmsSpec)
	// nullをセットすると素直にnullが入る
	@Test
	public void testSetSpec_002() throws Exception {
		var l = new BmsStandardLoader();
		l.setSpec(null);
		assertNull(Tests.getf(l, "mSpec"));
	}

	// setStrictly(boolean)
	// 有効にすると厳格なフォーマットチェックありの設定になること
	@Test
	public void testSetStrictly_Enable() throws Exception {
		var l = new BmsStandardLoader().setStrictly(true);
		var s = (BmsLoaderSettings)Tests.getf(l, "mSettings");
		assertTrue(s.isSyntaxErrorEnable());
		assertFalse(s.isFixSpecViolation());
		assertFalse(s.isAllowRedefine());
		assertFalse(s.isIgnoreUnknownMeta());
		assertFalse(s.isIgnoreUnknownChannel());
		assertFalse(s.isIgnoreWrongData());
	}

	// setStrictly(boolean)
	// 無効にすると厳格なフォーマットチェックなしの設定になること
	@Test
	public void testSetStrictly_Disable() throws Exception {
		var l = new BmsStandardLoader().setStrictly(false);
		var s = (BmsLoaderSettings)Tests.getf(l, "mSettings");
		assertFalse(s.isSyntaxErrorEnable());
		assertTrue(s.isFixSpecViolation());
		assertTrue(s.isAllowRedefine());
		assertTrue(s.isIgnoreUnknownMeta());
		assertTrue(s.isIgnoreUnknownChannel());
		assertTrue(s.isIgnoreWrongData());
	}

	private static class TestSettingsHandler implements BmsLoadHandler {
		BmsScriptError loadError = null;
		boolean calledParseError = false;
		boolean ignoreParseError = false;
		BmsTestResult testContentResult = BmsTestResult.OK;
		TestSettingsHandler(boolean parseErrorResult) {
			ignoreParseError = parseErrorResult;
		}
		@Override
		public boolean parseError(BmsScriptError error) {
			loadError = error;
			calledParseError = true;
			return ignoreParseError;
		}
		@Override
		public BmsTestResult testContent(BmsContent content) {
			return testContentResult;
		}
	}

	// setSyntaxErrorEnable(boolean)
	// 有効時、構文エラーで解析エラーが通知され、falseを返すと読み込みが停止すること
	@Test
	public void testSetSyntaxErrorEnableBoolean_SyntaxErrorAccept() throws Exception {
		var ec = BmsLoadException.class;
		var h = new TestSettingsHandler(false);
		var bms = "This_is_syntax_error";
		var e = assertThrows(ec, () -> new BmsStandardLoader().setSpec(spec()).setHandler(h).setSyntaxErrorEnable(true).load(bms));
		assertEquals(BmsErrorType.SYNTAX, e.getError().getType());
		assertEquals(BmsErrorType.SYNTAX, h.loadError.getType());
		assertTrue(h.calledParseError);
	}

	// setSyntaxErrorEnable(boolean)
	// 有効時、構文エラーで解析エラーが通知され、trueを返すと読み込みが続行されること
	@Test
	public void testSetSyntaxErrorEnableBoolean_SyntaxErrorThrough() throws Exception {
		var h = new TestSettingsHandler(true);
		var bms = "This_is_syntax_error";
		var c = new BmsStandardLoader().setSpec(spec()).setHandler(h).setSyntaxErrorEnable(true).load(bms);
		assertNotNull(c);
		assertEquals(BmsErrorType.SYNTAX, h.loadError.getType());
		assertTrue(h.calledParseError);
	}

	// setSyntaxErrorEnable(boolean)
	// 有効時、複数行コメント未終了で解析エラーが通知され、falseを返すと読み込みが停止すること
	@Test
	public void testSetSyntaxErrorEnableBoolean_CommentNotClosedAccept() throws Exception {
		var ec = BmsLoadException.class;
		var h = new TestSettingsHandler(false);
		var bms = "/*\n" +
				"This line in multi-line comment.\n" +
				"But this comment is not closed.";
		var e = assertThrows(ec, () -> new BmsStandardLoader().setSpec(spec()).setHandler(h).setSyntaxErrorEnable(true).load(bms));
		assertEquals(BmsErrorType.COMMENT_NOT_CLOSED, e.getError().getType());
		assertEquals(BmsErrorType.COMMENT_NOT_CLOSED, h.loadError.getType());
		assertTrue(h.calledParseError);
	}

	// setSyntaxErrorEnable(boolean)
	// 有効時、複数行コメント未終了で解析エラーが通知され、trueを返すと読み込みが続行されること
	@Test
	public void testSetSyntaxErrorEnableBoolean_CommentNotClosedThrough() throws Exception {
		var h = new TestSettingsHandler(true);
		var bms = "/*\n" +
				"This line in multi-line comment.\n" +
				"But this comment is not closed.";
		var c = new BmsStandardLoader().setSpec(spec()).setHandler(h).setSyntaxErrorEnable(true).load(bms);
		assertNotNull(c);
		assertEquals(BmsErrorType.COMMENT_NOT_CLOSED, h.loadError.getType());
		assertTrue(h.calledParseError);
	}

	// setSyntaxErrorEnable(boolean)
	// 有効時、BMSコンテンツ検査失敗で解析エラーが通知され、falseを返すと読み込みが停止すること
	@Test
	public void testSetSyntaxErrorEnableBoolean_FailedTestContentAccept() throws Exception {
		var ec = BmsLoadException.class;
		var h = new TestSettingsHandler(false);
		h.testContentResult = BmsTestResult.FAIL;
		var bms = "#BPM 120";
		var e = assertThrows(ec, () -> new BmsStandardLoader().setSpec(spec()).setHandler(h).setSyntaxErrorEnable(true).load(bms));
		assertEquals(BmsErrorType.TEST_CONTENT, e.getError().getType());
		assertEquals(BmsErrorType.TEST_CONTENT, h.loadError.getType());
		assertTrue(h.calledParseError);
	}

	// setSyntaxErrorEnable(boolean)
	// 有効時、BMSコンテンツ検査失敗で解析エラーが通知され、trueを返すと読み込みが続行されること
	@Test
	public void testSetSyntaxErrorEnableBoolean_FailedTestContentThrough() throws Exception {
		var h = new TestSettingsHandler(true);
		h.testContentResult = BmsTestResult.FAIL;
		var bms = "#BPM 120";
		var c = new BmsStandardLoader().setSpec(spec()).setHandler(h).setSyntaxErrorEnable(true).load(bms);
		assertNotNull(c);
		assertEquals(BmsErrorType.TEST_CONTENT, h.loadError.getType());
		assertTrue(h.calledParseError);
	}

	// setSyntaxErrorEnable(boolean)
	// 無効時、構文エラーを検出しても解析エラーが通知されずに読み込みが正常終了すること
	@Test
	public void testSetSyntaxErrorEnableBoolean_SyntaxErrorDisable() throws Exception {
		var h = new TestSettingsHandler(false);
		var bms = "This_is_syntax_error";
		var c = new BmsStandardLoader().setSpec(spec()).setHandler(h).setSyntaxErrorEnable(false).load(bms);
		assertNotNull(c);
		assertNull(h.loadError);
		assertFalse(h.calledParseError);
	}

	// setSyntaxErrorEnable(boolean)
	// 無効時、複数行コメント未終了でも解析エラーが通知されずに読み込みが正常終了すること
	@Test
	public void testSetSyntaxErrorEnableBoolean_CommentNotClosedDisable() throws Exception {
		var h = new TestSettingsHandler(false);
		var bms = "/*\n" +
				"This line in multi-line comment.\n" +
				"But this comment is not closed.";
		var c = new BmsStandardLoader().setSpec(spec()).setHandler(h).setSyntaxErrorEnable(false).load(bms);
		assertNotNull(c);
		assertNull(h.loadError);
		assertFalse(h.calledParseError);
	}

	// setSyntaxErrorEnable(boolean)
	// 無効時、BMSコンテンツ検査失敗でも解析エラーが通知されずに読み込みが正常終了すること
	@Test
	public void testSetSyntaxErrorEnableBoolean_FailedTestContentDisable() throws Exception {
		var h = new TestSettingsHandler(false);
		h.testContentResult = BmsTestResult.FAIL;
		var bms = "#BPM 120";
		var c = new BmsStandardLoader().setSpec(spec()).setHandler(h).setSyntaxErrorEnable(false).load(bms);
		assertNotNull(c);
		assertNull(h.loadError);
		assertFalse(h.calledParseError);
	}

	private static class SetFixSpecViolationHandler implements BmsLoadHandler {
		BmsScriptError error = null;
		@Override
		public boolean parseError(BmsScriptError error) {
			this.error = error;
			return true;
		}
	}

	// setFixSpecViolation(boolean)
	// 仕様違反訂正無効、初期BPM、範囲内：正常に読み込めること
	@Test
	public void testSetFixSpecViolation_Off_InitialBpm_Within() throws Exception {
		var h = new SetFixSpecViolationHandler();
		var c = (BmsContent)null;

		// 最小BPM
		var bms1 = "#BPM " + String.valueOf(BmsSpec.BPM_MIN);
		c = new BmsStandardLoader().setSpec(spec()).setHandler(h).setFixSpecViolation(false).load(bms1);
		assertNull("Parse error must be null, but the error was detected.", h.error);
		assertEquals(BmsSpec.BPM_MIN, c.getInitialBpm(), 0.0);

		// 最大BPM
		var bms2 = "#BPM " + String.valueOf(BmsSpec.BPM_MAX);
		c = new BmsStandardLoader().setSpec(spec()).setHandler(h).setFixSpecViolation(false).load(bms2);
		assertNull("Parse error must be null, but the error was detected.", h.error);
		assertEquals(BmsSpec.BPM_MAX, c.getInitialBpm(), 0.0);
	}

	// setFixSpecViolation(boolean)
	// 仕様違反訂正無効、初期BPM、範囲外：解析エラーとなること
	@Test
	public void testSetFixSpecViolation_Off_InitialBpm_Without() throws Exception {
		var h = new SetFixSpecViolationHandler();
		var c = (BmsContent)null;

		// 最小BPM
		var bms1 = "#BPM " + String.valueOf(Math.nextDown(BmsSpec.BPM_MIN));
		c = new BmsStandardLoader().setSpec(spec()).setHandler(h).setFixSpecViolation(false).load(bms1);
		assertNotNull("Parse error must be detect, but an error was NOT detected.", h.error);
		assertEquals(BmsErrorType.SPEC_VIOLATION, h.error.getType());
		assertEquals(((Number)c.getSpec().getInitialBpmMeta().getDefaultValue()).doubleValue(), c.getInitialBpm(), 0.0);

		// 最大BPM
		var bms2 = "#BPM " + String.valueOf(Math.nextUp(BmsSpec.BPM_MAX));
		c = new BmsStandardLoader().setSpec(spec()).setHandler(h).setFixSpecViolation(false).load(bms2);
		assertNotNull("Parse error must be detect, but an error was NOT detected.", h.error);
		assertEquals(BmsErrorType.SPEC_VIOLATION, h.error.getType());
		assertEquals(((Number)c.getSpec().getInitialBpmMeta().getDefaultValue()).doubleValue(), c.getInitialBpm(), 0.0);
	}

	// setFixSpecViolation(boolean)
	// 仕様違反訂正無効、小節長、範囲内：正常に読み込めること
	@Test
	public void testSetFixSpecViolation_Off_Length_Within() throws Exception {
		var h = new SetFixSpecViolationHandler();
		var c = (BmsContent)null;

		// 最小小節長
		var bms1 = "#00002:" + String.valueOf(BmsSpec.LENGTH_MIN);
		c = new BmsStandardLoader().setSpec(spec()).setHandler(h).setFixSpecViolation(false).load(bms1);
		assertNull("Parse error must be null, but the error was detected.", h.error);
		assertEquals(BmsSpec.LENGTH_MIN, ((Number)c.getMeasureValue(2, 0)).doubleValue(), 0.0);

		// 最大小節長
		var bms2 = "#00002:" + String.valueOf(BmsSpec.LENGTH_MAX);
		c = new BmsStandardLoader().setSpec(spec()).setHandler(h).setFixSpecViolation(false).load(bms2);
		assertNull("Parse error must be null, but the error was detected.", h.error);
		assertEquals(BmsSpec.LENGTH_MAX, ((Number)c.getMeasureValue(2, 0)).doubleValue(), 0.0);
	}

	// setFixSpecViolation(boolean)
	// 仕様違反訂正無効、小節長、範囲外：解析エラーとなること
	@Test
	public void testSetFixSpecViolation_Off_Length_Without() throws Exception {
		var h = new SetFixSpecViolationHandler();

		// 最小BPM
		var bms1 = "#00002:" + String.valueOf(Math.nextDown(BmsSpec.LENGTH_MIN));
		new BmsStandardLoader().setSpec(spec()).setHandler(h).setFixSpecViolation(false).load(bms1);
		assertNotNull("Parse error must be detect, but an error was NOT detected.", h.error);
		assertEquals(BmsErrorType.SPEC_VIOLATION, h.error.getType());

		// 最大BPM
		var bms2 = "#00002:" + String.valueOf(Math.nextUp(BmsSpec.LENGTH_MAX));
		new BmsStandardLoader().setSpec(spec()).setHandler(h).setFixSpecViolation(false).load(bms2);
		assertNotNull("Parse error must be detect, but an error was NOT detected.", h.error);
		assertEquals(BmsErrorType.SPEC_VIOLATION, h.error.getType());
	}

	// setFixSpecViolation(boolean)
	// 仕様違反訂正無効、BPM変更、範囲内：正常に読み込めること
	@Test
	public void testSetFixSpecViolation_Off_Bpm_Within() throws Exception {
		var h = new SetFixSpecViolationHandler();
		var c = (BmsContent)null;

		// 最小BPM
		var bms1 = "#BPM01 " + String.valueOf(BmsSpec.BPM_MIN);
		c = new BmsStandardLoader().setSpec(spec()).setHandler(h).setFixSpecViolation(false).load(bms1);
		assertNull("Parse error must be null, but the error was detected.", h.error);
		assertEquals(BmsSpec.BPM_MIN, ((Number)c.getIndexedMeta("#bpm", 1)).doubleValue(), 0.0);

		// 最大BPM
		var bms2 = "#BPM01 " + String.valueOf(BmsSpec.BPM_MAX);
		c = new BmsStandardLoader().setSpec(spec()).setHandler(h).setFixSpecViolation(false).load(bms2);
		assertNull("Parse error must be null, but the error was detected.", h.error);
		assertEquals(BmsSpec.BPM_MAX, ((Number)c.getIndexedMeta("#bpm", 1)).doubleValue(), 0.0);
	}

	// setFixSpecViolation(boolean)
	// 仕様違反訂正無効、BPM変更、範囲外：解析エラーとなること
	@Test
	public void testSetFixSpecViolation_Off_Bpm_Without() throws Exception {
		var h = new SetFixSpecViolationHandler();

		// 最小BPM
		var bms1 = "#BPM01 " + String.valueOf(Math.nextDown(BmsSpec.BPM_MIN));
		new BmsStandardLoader().setSpec(spec()).setHandler(h).setFixSpecViolation(false).load(bms1);
		assertNotNull("Parse error must be detect, but an error was NOT detected.", h.error);
		assertEquals(BmsErrorType.SPEC_VIOLATION, h.error.getType());

		// 最大BPM
		var bms2 = "#BPM01 " + String.valueOf(Math.nextUp(BmsSpec.BPM_MAX));
		new BmsStandardLoader().setSpec(spec()).setHandler(h).setFixSpecViolation(false).load(bms2);
		assertNotNull("Parse error must be detect, but an error was NOT detected.", h.error);
		assertEquals(BmsErrorType.SPEC_VIOLATION, h.error.getType());
	}

	// setFixSpecViolation(boolean)
	// 仕様違反訂正無効、譜面停止時間、範囲内：正常に読み込めること
	@Test
	public void testSetFixSpecViolation_Off_Stop_Within() throws Exception {
		var h = new SetFixSpecViolationHandler();
		var c = (BmsContent)null;

		// 最小BPM
		var bms1 = "#STOP01 " + String.valueOf(BmsSpec.STOP_MIN);
		c = new BmsStandardLoader().setSpec(spec()).setHandler(h).setFixSpecViolation(false).load(bms1);
		assertNull("Parse error must be null, but the error was detected.", h.error);
		assertEquals(BmsSpec.STOP_MIN, ((Number)c.getIndexedMeta("#stop", 1)).doubleValue(), 0.0);

		// 最大BPM
		var bms2 = "#STOP01 " + String.valueOf(BmsSpec.STOP_MAX);
		c = new BmsStandardLoader().setSpec(spec()).setHandler(h).setFixSpecViolation(false).load(bms2);
		assertNull("Parse error must be null, but the error was detected.", h.error);
		assertEquals(BmsSpec.STOP_MAX, ((Number)c.getIndexedMeta("#stop", 1)).doubleValue(), 0.0);
	}

	// setFixSpecViolation(boolean)
	// 仕様違反訂正無効、譜面停止時間、範囲外：解析エラーとなること
	@Test
	public void testSetFixSpecViolation_Off_Stop_Without() throws Exception {
		var h = new SetFixSpecViolationHandler();

		// 最小BPM
		var bms1 = "#STOP01 " + String.valueOf(Math.nextDown(BmsSpec.STOP_MIN));
		new BmsStandardLoader().setSpec(spec()).setHandler(h).setFixSpecViolation(false).load(bms1);
		assertNotNull("Parse error must be detect, but an error was NOT detected.", h.error);
		assertEquals(BmsErrorType.SPEC_VIOLATION, h.error.getType());

		// 最大BPM
		var bms2 = "#STOP01 " + String.valueOf(Math.nextUp(BmsSpec.STOP_MAX));
		new BmsStandardLoader().setSpec(spec()).setHandler(h).setFixSpecViolation(false).load(bms2);
		assertNotNull("Parse error must be detect, but an error was NOT detected.", h.error);
		assertEquals(BmsErrorType.SPEC_VIOLATION, h.error.getType());
	}

	// setFixSpecViolation(boolean)
	// 仕様違反訂正有効、初期BPM、範囲内：正常に読み込めること
	@Test
	public void testSetFixSpecViolation_On_InitialBpm_Within() throws Exception {
		var h = new SetFixSpecViolationHandler();
		var c = (BmsContent)null;

		// 最小BPM
		var bms1 = "#BPM " + String.valueOf(BmsSpec.BPM_MIN);
		c = new BmsStandardLoader().setSpec(spec()).setHandler(h).setFixSpecViolation(true).load(bms1);
		assertNull("Parse error must be null, but the error was detected.", h.error);
		assertEquals(BmsSpec.BPM_MIN, c.getInitialBpm(), 0.0);

		// 最大BPM
		var bms2 = "#BPM " + String.valueOf(BmsSpec.BPM_MAX);
		c = new BmsStandardLoader().setSpec(spec()).setHandler(h).setFixSpecViolation(true).load(bms2);
		assertNull("Parse error must be null, but the error was detected.", h.error);
		assertEquals(BmsSpec.BPM_MAX, c.getInitialBpm(), 0.0);
	}

	// setFixSpecViolation(boolean)
	// 仕様違反訂正有効、初期BPM、範囲外：解析エラーにならず、範囲内に訂正されること
	@Test
	public void testSetFixSpecViolation_On_InitialBpm_Without() throws Exception {
		var h = new SetFixSpecViolationHandler();
		var c = (BmsContent)null;

		// 最小BPM
		var bms1 = "#BPM " + String.valueOf(Math.nextDown(BmsSpec.BPM_MIN));
		c = new BmsStandardLoader().setSpec(spec()).setHandler(h).setFixSpecViolation(true).load(bms1);
		assertNull("Parse error must be null, but the error was detected.", h.error);
		assertEquals(BmsSpec.BPM_MIN, c.getInitialBpm(), 0.0);

		// 最大BPM
		var bms2 = "#BPM " + String.valueOf(Math.nextUp(BmsSpec.BPM_MAX));
		c = new BmsStandardLoader().setSpec(spec()).setHandler(h).setFixSpecViolation(true).load(bms2);
		assertNull("Parse error must be null, but the error was detected.", h.error);
		assertEquals(BmsSpec.BPM_MAX, c.getInitialBpm(), 0.0);
	}

	// setFixSpecViolation(boolean)
	// 仕様違反訂正有効、小節長、範囲内：正常に読み込めること
	@Test
	public void testSetFixSpecViolation_On_Length_Within() throws Exception {
		var h = new SetFixSpecViolationHandler();
		var c = (BmsContent)null;

		// 最小小節長
		var bms1 = "#00002:" + String.valueOf(BmsSpec.LENGTH_MIN);
		c = new BmsStandardLoader().setSpec(spec()).setHandler(h).setFixSpecViolation(true).load(bms1);
		assertNull("Parse error must be null, but the error was detected.", h.error);
		assertEquals(BmsSpec.LENGTH_MIN, ((Number)c.getMeasureValue(2, 0)).doubleValue(), 0.0);

		// 最大小節長
		var bms2 = "#00002:" + String.valueOf(BmsSpec.LENGTH_MAX);
		c = new BmsStandardLoader().setSpec(spec()).setHandler(h).setFixSpecViolation(true).load(bms2);
		assertNull("Parse error must be null, but the error was detected.", h.error);
		assertEquals(BmsSpec.LENGTH_MAX, ((Number)c.getMeasureValue(2, 0)).doubleValue(), 0.0);
	}

	// setFixSpecViolation(boolean)
	// 仕様違反訂正有効、小節長、範囲外：解析エラーにならず、範囲内に訂正されること
	@Test
	public void testSetFixSpecViolation_On_Length_Without() throws Exception {
		var h = new SetFixSpecViolationHandler();
		var c = (BmsContent)null;

		// 最小小節長
		var bms1 = "#00002:" + String.valueOf(Math.nextDown(BmsSpec.LENGTH_MIN));
		c = new BmsStandardLoader().setSpec(spec()).setHandler(h).setFixSpecViolation(true).load(bms1);
		assertNull("Parse error must be null, but the error was detected.", h.error);
		assertEquals(BmsSpec.LENGTH_MIN, ((Number)c.getMeasureValue(2, 0)).doubleValue(), 0.0);

		// 最大小節長
		var bms2 = "#00002:" + String.valueOf(Math.nextUp(BmsSpec.LENGTH_MAX));
		c = new BmsStandardLoader().setSpec(spec()).setHandler(h).setFixSpecViolation(true).load(bms2);
		assertNull("Parse error must be null, but the error was detected.", h.error);
		assertEquals(BmsSpec.LENGTH_MAX, ((Number)c.getMeasureValue(2, 0)).doubleValue(), 0.0);
	}

	// setFixSpecViolation(boolean)
	// 仕様違反訂正有効、BPM変更、範囲内：正常に読み込めること
	@Test
	public void testSetFixSpecViolation_On_Bpm_Within() throws Exception {
		var h = new SetFixSpecViolationHandler();
		var c = (BmsContent)null;

		// 最小BPM
		var bms1 = "#BPM01 " + String.valueOf(BmsSpec.BPM_MIN);
		c = new BmsStandardLoader().setSpec(spec()).setHandler(h).setFixSpecViolation(true).load(bms1);
		assertNull("Parse error must be null, but the error was detected.", h.error);
		assertEquals(BmsSpec.BPM_MIN, ((Number)c.getIndexedMeta("#bpm", 1)).doubleValue(), 0.0);

		// 最大BPM
		var bms2 = "#BPM01 " + String.valueOf(BmsSpec.BPM_MAX);
		c = new BmsStandardLoader().setSpec(spec()).setHandler(h).setFixSpecViolation(true).load(bms2);
		assertNull("Parse error must be null, but the error was detected.", h.error);
		assertEquals(BmsSpec.BPM_MAX, ((Number)c.getIndexedMeta("#bpm", 1)).doubleValue(), 0.0);
	}

	// setFixSpecViolation(boolean)
	// 仕様違反訂正有効、BPM変更、範囲外：解析エラーにならず、範囲内に訂正されること
	@Test
	public void testSetFixSpecViolation_On_Bpm_Without() throws Exception {
		var h = new SetFixSpecViolationHandler();
		var c = (BmsContent)null;

		// 最小BPM
		var bms1 = "#BPM01 " + String.valueOf(Math.nextDown(BmsSpec.BPM_MIN));
		c = new BmsStandardLoader().setSpec(spec()).setHandler(h).setFixSpecViolation(true).load(bms1);
		assertNull("Parse error must be null, but the error was detected.", h.error);
		assertEquals(BmsSpec.BPM_MIN, ((Number)c.getIndexedMeta("#bpm", 1)).doubleValue(), 0.0);

		// 最大BPM
		var bms2 = "#BPM01 " + String.valueOf(Math.nextUp(BmsSpec.BPM_MAX));
		c = new BmsStandardLoader().setSpec(spec()).setHandler(h).setFixSpecViolation(true).load(bms2);
		assertNull("Parse error must be null, but the error was detected.", h.error);
		assertEquals(BmsSpec.BPM_MAX, ((Number)c.getIndexedMeta("#bpm", 1)).doubleValue(), 0.0);
	}

	// setFixSpecViolation(boolean)
	// 仕様違反訂正有効、譜面停止時間、範囲内：正常に読み込めること
	@Test
	public void testSetFixSpecViolation_On_Stop_Within() throws Exception {
		var h = new SetFixSpecViolationHandler();
		var c = (BmsContent)null;

		// 最小BPM
		var bms1 = "#STOP01 " + String.valueOf(BmsSpec.STOP_MIN);
		c = new BmsStandardLoader().setSpec(spec()).setHandler(h).setFixSpecViolation(true).load(bms1);
		assertNull("Parse error must be null, but the error was detected.", h.error);
		assertEquals(BmsSpec.STOP_MIN, ((Number)c.getIndexedMeta("#stop", 1)).doubleValue(), 0.0);

		// 最大BPM
		var bms2 = "#STOP01 " + String.valueOf(BmsSpec.STOP_MAX);
		c = new BmsStandardLoader().setSpec(spec()).setHandler(h).setFixSpecViolation(true).load(bms2);
		assertNull("Parse error must be null, but the error was detected.", h.error);
		assertEquals(BmsSpec.STOP_MAX, ((Number)c.getIndexedMeta("#stop", 1)).doubleValue(), 0.0);
	}

	// setFixSpecViolation(boolean)
	// 仕様違反訂正有効、譜面停止時間、範囲外：解析エラーにならず、範囲内に訂正されること
	@Test
	public void testSetFixSpecViolation_On_Stop_Without() throws Exception {
		var h = new SetFixSpecViolationHandler();
		var c = (BmsContent)null;

		// 最小BPM
		var bms1 = "#STOP01 " + String.valueOf(Math.nextDown(BmsSpec.STOP_MIN));
		c = new BmsStandardLoader().setSpec(spec()).setHandler(h).setFixSpecViolation(true).load(bms1);
		assertNull("Parse error must be null, but the error was detected.", h.error);
		assertEquals(BmsSpec.STOP_MIN, ((Number)c.getIndexedMeta("#stop", 1)).doubleValue(), 0.0);

		// 最大BPM
		var bms2 = "#STOP01 " + String.valueOf(Math.nextUp(BmsSpec.STOP_MAX));
		c = new BmsStandardLoader().setSpec(spec()).setHandler(h).setFixSpecViolation(true).load(bms2);
		assertNull("Parse error must be null, but the error was detected.", h.error);
		assertEquals(BmsSpec.STOP_MAX, ((Number)c.getIndexedMeta("#stop", 1)).doubleValue(), 0.0);
	}

	// setAllowRedefine(boolean)
	// 再定義許可：単体メタ情報は最後に定義されたデータが有効であること
	@Test
	public void testSetAllowRedefineBoolean_Allow_SingleMeta() throws Exception {
		var h = new TestSettingsHandler(false);
		var b = "#TITLE Ballad 4 U\n" +
				"#TITLE National Anthem\n" +
				"#TITLE My Love Song";
		var c = new BmsStandardLoader().setSpec(spec()).setHandler(h).setAllowRedefine(true).load(b);
		assertFalse(h.calledParseError);
		assertNull(h.loadError);
		assertEquals("My Love Song", (String)c.getSingleMeta("#title"));
	}

	// setAllowRedefine(boolean)
	// 再定義許可：複数メタ情報は定義されたデータが全て有効であること
	@Test
	public void testSetAllowRedefineBoolean_Allow_MultipleMeta() throws Exception {
		var h = new TestSettingsHandler(false);
		var b = "#SUBARTIST J-SON3\n" +
				"#SUBARTIST ZEISON\n" +
				"#SUBARTIST 13-FRI";
		var c = new BmsStandardLoader().setSpec(spec()).setHandler(h).setAllowRedefine(true).load(b);
		assertFalse(h.calledParseError);
		assertNull(h.loadError);
		assertEquals(3, c.getMultipleMetaCount("#subartist"));
		assertEquals("J-SON3", (String)c.getMultipleMeta("#subartist", 0));
		assertEquals("ZEISON", (String)c.getMultipleMeta("#subartist", 1));
		assertEquals("13-FRI", (String)c.getMultipleMeta("#subartist", 2));
	}

	// setAllowRedefine(boolean)
	// 再定義許可：索引付きメタ情報の同一インデックスは最後に定義されたデータが有効であること
	@Test
	public void testSetAllowRedefineBoolean_Allow_IndexedMeta() throws Exception {
		var h = new TestSettingsHandler(false);
		var b = "#IINTEGERXX 300\n" +
				"#IINTEGERXX 1000\n" +
				"#IINTEGERZZ 255\n" +
				"#IINTEGERXX 2700";
		var c = new BmsStandardLoader().setSpec(spec()).setHandler(h).setAllowRedefine(true).load(b);
		assertFalse(h.calledParseError);
		assertNull(h.loadError);
		assertEquals(2, c.getIndexedMetaCount("#iinteger"));
		assertEquals(2700L, ((Long)c.getIndexedMeta("#iinteger", BmsInt.to36i("XX"))).longValue());
		assertEquals(255L, ((Long)c.getIndexedMeta("#iinteger", BmsInt.to36i("ZZ"))).longValue());
	}

	// setAllowRedefine(boolean)
	// 再定義許可：同じ小節番号の重複可能チャンネル(値型)は定義されたデータが全て有効であること
	@Test
	public void testSetAllowRedefineBoolean_Allow_MultipleValueChannel() throws Exception {
		var h = new TestSettingsHandler(false);
		var b = "#003DD:160\n" +
				"#003DD:270\n" +
				"#004DD:990\n" +
				"#003DD:380";
		var c = new BmsStandardLoader().setSpec(spec()).setHandler(h).setAllowRedefine(true).load(b);
		assertFalse(h.calledParseError);
		assertNull(h.loadError);
		assertEquals(3, c.getChannelDataCount(BmsInt.to36i("DD"), 3));
		assertEquals(160L, ((Long)c.getMeasureValue(BmsInt.to36i("DD"), 0, 3)).longValue());
		assertEquals(270L, ((Long)c.getMeasureValue(BmsInt.to36i("DD"), 1, 3)).longValue());
		assertEquals(380L, ((Long)c.getMeasureValue(BmsInt.to36i("DD"), 2, 3)).longValue());
		assertEquals(1, c.getChannelDataCount(BmsInt.to36i("DD"), 4));
		assertEquals(990L, ((Long)c.getMeasureValue(BmsInt.to36i("DD"), 4)).longValue());
	}

	// setAllowRedefine(boolean)
	// 再定義許可：同じ小節番号の重複可能チャンネル(配列型)は定義されたデータが全て有効であること
	@Test
	public void testSetAllowRedefineBoolean_Allow_MultipleArrayChannel() throws Exception {
		var h = new TestSettingsHandler(false);
		var b = "#004EE:01000200\n" +
				"#004EE:05060708\n" +
				"#006EE:1122\n" +
				"#004EE:000A0B0C";
		var c = new BmsStandardLoader().setSpec(spec()).setHandler(h).setAllowRedefine(true).load(b);
		assertFalse(h.calledParseError);
		assertNull(h.loadError);
		assertEquals(BmsInt.to36i("01"), c.getNote(BmsInt.to36i("EE"), 0, 4, 0.0).getValue());
		assertEquals(BmsInt.to36i("02"), c.getNote(BmsInt.to36i("EE"), 0, 4, 96.0).getValue());
		assertEquals(BmsInt.to36i("05"), c.getNote(BmsInt.to36i("EE"), 1, 4, 0.0).getValue());
		assertEquals(BmsInt.to36i("06"), c.getNote(BmsInt.to36i("EE"), 1, 4, 48.0).getValue());
		assertEquals(BmsInt.to36i("07"), c.getNote(BmsInt.to36i("EE"), 1, 4, 96.0).getValue());
		assertEquals(BmsInt.to36i("08"), c.getNote(BmsInt.to36i("EE"), 1, 4, 144.0).getValue());
		assertEquals(BmsInt.to36i("0A"), c.getNote(BmsInt.to36i("EE"), 2, 4, 48.0).getValue());
		assertEquals(BmsInt.to36i("0B"), c.getNote(BmsInt.to36i("EE"), 2, 4, 96.0).getValue());
		assertEquals(BmsInt.to36i("0C"), c.getNote(BmsInt.to36i("EE"), 2, 4, 144.0).getValue());
		assertEquals(BmsInt.to36i("11"), c.getNote(BmsInt.to36i("EE"), 0, 6, 0.0).getValue());
		assertEquals(BmsInt.to36i("22"), c.getNote(BmsInt.to36i("EE"), 0, 6, 96.0).getValue());
	}

	// setAllowRedefine(boolean)
	// 再定義許可：同じ小節番号の重複不可チャンネル(値型)は最後に定義されたデータが有効であること
	@Test
	public void testSetAllowRedefineBoolean_Allow_SingleValueChannel() throws Exception {
		var h = new TestSettingsHandler(false);
		var b = "#003DC:160\n" +
				"#003DC:270\n" +
				"#004DC:990\n" +
				"#003DC:380";
		var c = new BmsStandardLoader().setSpec(spec()).setHandler(h).setAllowRedefine(true).load(b);
		assertFalse(h.calledParseError);
		assertNull(h.loadError);
		assertEquals(1, c.getChannelDataCount(BmsInt.to36i("DC"), 3));
		assertEquals(380L, ((Long)c.getMeasureValue(BmsInt.to36i("DC"), 0, 3)).longValue());
		assertEquals(1, c.getChannelDataCount(BmsInt.to36i("DC"), 4));
		assertEquals(990L, ((Long)c.getMeasureValue(BmsInt.to36i("DC"), 0, 4)).longValue());
	}

	// setAllowRedefine(boolean)
	// 再定義許可：同じ小節番号の重複不可チャンネル(配列型)は再定義されると、重複データは合成されること
	@Test
	public void testSetAllowRedefineBoolean_Allow_SingleArrayChannel_Merge() throws Exception {
		var h = new TestSettingsHandler(false);
		var b = "#004ED:0A000CZZ\n" +
				"#004ED:000B0000\n" +
				"#006ED:1122\n" +
				"#004ED:0E00000D";
		var c = new BmsStandardLoader().setSpec(spec()).setHandler(h).setAllowRedefine(true).load(b);
		assertFalse(h.calledParseError);
		assertNull(h.loadError);
		assertEquals(BmsInt.to36i("0E"), c.getNote(BmsInt.to36i("ED"), 0, 4, 0.0).getValue());
		assertEquals(BmsInt.to36i("0B"), c.getNote(BmsInt.to36i("ED"), 0, 4, 48.0).getValue());
		assertEquals(BmsInt.to36i("0C"), c.getNote(BmsInt.to36i("ED"), 0, 4, 96.0).getValue());
		assertEquals(BmsInt.to36i("0D"), c.getNote(BmsInt.to36i("ED"), 0, 4, 144.0).getValue());
		assertEquals(BmsInt.to36i("11"), c.getNote(BmsInt.to36i("ED"), 0, 6, 0.0).getValue());
		assertEquals(BmsInt.to36i("22"), c.getNote(BmsInt.to36i("ED"), 0, 6, 96.0).getValue());
	}

	// setAllowRedefine(boolean)
	// 再定義不許可：単体メタ情報が再定義されエラー通知でエラー無視すると、読み込みが続行され最初に定義されたデータが有効であること
	@Test
	public void testSetAllowRedefineBoolean_Disallow_SingleMeta_IgnoreError() throws Exception {
		var h = new TestSettingsHandler(true);
		var b = "#TITLE Ballad 4 U\n" +
				"#TITLE National Anthem\n" +
				"#TITLE My Love Song";
		var c = new BmsStandardLoader().setSpec(spec()).setHandler(h).setAllowRedefine(false).load(b);
		assertTrue(h.calledParseError);
		assertEquals(BmsErrorType.REDEFINE, h.loadError.getType());
		assertEquals("Ballad 4 U", (String)c.getSingleMeta("#title"));
	}

	// setAllowRedefine(boolean)
	// 再定義不許可：単体メタ情報が再定義されエラー通知でエラー承諾すると、REDEFINEで読み込みエラーになること
	@Test
	public void testSetAllowRedefineBoolean_Disallow_SingleMeta_AcceptError() throws Exception {
		var h = new TestSettingsHandler(false);
		var b = "#TITLE Ballad 4 U\n" +
				"#TITLE National Anthem\n" +
				"#TITLE My Love Song";
		var ex = BmsLoadException.class;
		var e = assertThrows(ex, () -> new BmsStandardLoader().setSpec(spec()).setHandler(h).setAllowRedefine(false).load(b));
		assertEquals(BmsErrorType.REDEFINE, e.getError().getType());
		assertTrue(h.calledParseError);
		assertEquals(BmsErrorType.REDEFINE, h.loadError.getType());
	}

	// setAllowRedefine(boolean)
	// 再定義不許可：複数メタ情報は定義されたデータが全て有効であること
	@Test
	public void testSetAllowRedefineBoolean_Disallow_MultipleMeta() throws Exception {
		var h = new TestSettingsHandler(false);
		var b = "#SUBARTIST J-SON3\n" +
				"#SUBARTIST ZEISON\n" +
				"#SUBARTIST 13-FRI";
		var c = new BmsStandardLoader().setSpec(spec()).setHandler(h).setAllowRedefine(false).load(b);
		assertFalse(h.calledParseError);
		assertNull(h.loadError);
		assertEquals(3, c.getMultipleMetaCount("#subartist"));
		assertEquals("J-SON3", (String)c.getMultipleMeta("#subartist", 0));
		assertEquals("ZEISON", (String)c.getMultipleMeta("#subartist", 1));
		assertEquals("13-FRI", (String)c.getMultipleMeta("#subartist", 2));
	}

	// setAllowRedefine(boolean)
	// 再定義不許可：索引付きメタ情報の同一インデックスが再定義されエラー通知でエラー無視すると、読み込みが続行され最初に定義されたデータが有効であること
	@Test
	public void testSetAllowRedefineBoolean_Disallow_IndexedMeta_IgnoreError() throws Exception {
		var h = new TestSettingsHandler(true);
		var b = "#IINTEGERXX 300\n" +
				"#IINTEGERXX 1000\n" +
				"#IINTEGERZZ 255\n" +
				"#IINTEGERXX 2700";
		var c = new BmsStandardLoader().setSpec(spec()).setHandler(h).setAllowRedefine(false).load(b);
		assertTrue(h.calledParseError);
		assertEquals(BmsErrorType.REDEFINE, h.loadError.getType());
		assertEquals(2, c.getIndexedMetaCount("#iinteger"));
		assertEquals(300L, ((Long)c.getIndexedMeta("#iinteger", BmsInt.to36i("XX"))).longValue());
		assertEquals(255L, ((Long)c.getIndexedMeta("#iinteger", BmsInt.to36i("ZZ"))).longValue());
	}

	// setAllowRedefine(boolean)
	// 再定義不許可：索引付きメタ情報の同一インデックスが再定義されエラー通知でエラー承諾すると、REDEFINEで読み込みエラーになること
	@Test
	public void testSetAllowRedefineBoolean_Disallow_IndexedMeta_AcceptError() throws Exception {
		var h = new TestSettingsHandler(false);
		var b = "#IINTEGERXX 300\n" +
				"#IINTEGERXX 1000\n" +
				"#IINTEGERZZ 255\n" +
				"#IINTEGERXX 2700";
		var ex = BmsLoadException.class;
		var e = assertThrows(ex, () -> new BmsStandardLoader().setSpec(spec()).setHandler(h).setAllowRedefine(false).load(b));
		assertEquals(BmsErrorType.REDEFINE, e.getError().getType());
		assertTrue(h.calledParseError);
		assertEquals(BmsErrorType.REDEFINE, h.loadError.getType());
	}

	// setAllowRedefine(boolean)
	// 再定義不許可：同じ小節番号の重複可能チャンネル(値型)は定義されたデータが全て有効であること
	@Test
	public void testSetAllowRedefineBoolean_Disallow_MultipleValueChannel() throws Exception {
		var h = new TestSettingsHandler(false);
		var b = "#003DD:160\n" +
				"#003DD:270\n" +
				"#004DD:990\n" +
				"#003DD:380";
		var c = new BmsStandardLoader().setSpec(spec()).setHandler(h).setAllowRedefine(false).load(b);
		assertFalse(h.calledParseError);
		assertNull(h.loadError);
		assertEquals(3, c.getChannelDataCount(BmsInt.to36i("DD"), 3));
		assertEquals(160L, ((Long)c.getMeasureValue(BmsInt.to36i("DD"), 0, 3)).longValue());
		assertEquals(270L, ((Long)c.getMeasureValue(BmsInt.to36i("DD"), 1, 3)).longValue());
		assertEquals(380L, ((Long)c.getMeasureValue(BmsInt.to36i("DD"), 2, 3)).longValue());
		assertEquals(1, c.getChannelDataCount(BmsInt.to36i("DD"), 4));
		assertEquals(990L, ((Long)c.getMeasureValue(BmsInt.to36i("DD"), 4)).longValue());
	}

	// setAllowRedefine(boolean)
	// 再定義不許可：同じ小節番号の重複可能チャンネル(配列型)は定義されたデータが全て有効であること
	@Test
	public void testSetAllowRedefineBoolean_Disallow_MultipleArrayChannel() throws Exception {
		var h = new TestSettingsHandler(false);
		var b = "#004EE:01000200\n" +
				"#004EE:05060708\n" +
				"#006EE:1122\n" +
				"#004EE:000A0B0C";
		var c = new BmsStandardLoader().setSpec(spec()).setHandler(h).setAllowRedefine(false).load(b);
		assertFalse(h.calledParseError);
		assertNull(h.loadError);
		assertEquals(BmsInt.to36i("01"), c.getNote(BmsInt.to36i("EE"), 0, 4, 0.0).getValue());
		assertEquals(BmsInt.to36i("02"), c.getNote(BmsInt.to36i("EE"), 0, 4, 96.0).getValue());
		assertEquals(BmsInt.to36i("05"), c.getNote(BmsInt.to36i("EE"), 1, 4, 0.0).getValue());
		assertEquals(BmsInt.to36i("06"), c.getNote(BmsInt.to36i("EE"), 1, 4, 48.0).getValue());
		assertEquals(BmsInt.to36i("07"), c.getNote(BmsInt.to36i("EE"), 1, 4, 96.0).getValue());
		assertEquals(BmsInt.to36i("08"), c.getNote(BmsInt.to36i("EE"), 1, 4, 144.0).getValue());
		assertEquals(BmsInt.to36i("0A"), c.getNote(BmsInt.to36i("EE"), 2, 4, 48.0).getValue());
		assertEquals(BmsInt.to36i("0B"), c.getNote(BmsInt.to36i("EE"), 2, 4, 96.0).getValue());
		assertEquals(BmsInt.to36i("0C"), c.getNote(BmsInt.to36i("EE"), 2, 4, 144.0).getValue());
		assertEquals(BmsInt.to36i("11"), c.getNote(BmsInt.to36i("EE"), 0, 6, 0.0).getValue());
		assertEquals(BmsInt.to36i("22"), c.getNote(BmsInt.to36i("EE"), 0, 6, 96.0).getValue());
	}

	// setAllowRedefine(boolean)
	// 再定義不許可：同じ小節番号の重複不可チャンネル(値型)が再定義されエラー通知でエラー無視すると、読み込みが続行され最初に定義されたデータが有効であること
	@Test
	public void testSetAllowRedefineBoolean_Disallow_SingleValueChannel_IgnoreError() throws Exception {
		var h = new TestSettingsHandler(true);
		var b = "#003DC:160\n" +
				"#003DC:270\n" +
				"#004DC:990\n" +
				"#003DC:380";
		var c = new BmsStandardLoader().setSpec(spec()).setHandler(h).setAllowRedefine(false).load(b);
		assertTrue(h.calledParseError);
		assertEquals(BmsErrorType.REDEFINE, h.loadError.getType());
		assertEquals(1, c.getChannelDataCount(BmsInt.to36i("DC"), 3));
		assertEquals(160L, ((Long)c.getMeasureValue(BmsInt.to36i("DC"), 0, 3)).longValue());
		assertEquals(1, c.getChannelDataCount(BmsInt.to36i("DC"), 4));
		assertEquals(990L, ((Long)c.getMeasureValue(BmsInt.to36i("DC"), 0, 4)).longValue());
	}

	// setAllowRedefine(boolean)
	// 再定義不許可：同じ小節番号の重複不可チャンネル(値型)が再定義されエラー通知でエラー承諾すると、REDEFINEで読み込みエラーになること
	@Test
	public void testSetAllowRedefineBoolean_Disallow_SingleValueChannel_AcceptError() throws Exception {
		var h = new TestSettingsHandler(false);
		var b = "#003DC:160\n" +
				"#003DC:270\n" +
				"#004DC:990\n" +
				"#003DC:380";
		var ex = BmsLoadException.class;
		var e = assertThrows(ex, () -> new BmsStandardLoader().setSpec(spec()).setHandler(h).setAllowRedefine(false).load(b));
		assertEquals(BmsErrorType.REDEFINE, e.getError().getType());
		assertTrue(h.calledParseError);
		assertEquals(BmsErrorType.REDEFINE, h.loadError.getType());
	}

	// setAllowRedefine(boolean)
	// 再定義不許可：同じ小節番号の重複不可チャンネル(配列型)が再定義されると、重複データは合成されること
	@Test
	public void testSetAllowRedefineBoolean_Disallow_SingleArrayChannel_Mearge() throws Exception {
		var h = new TestSettingsHandler(false);
		var b = "#004ED:01000300\n" +
				"#004ED:00020000\n" +
				"#006ED:1122\n" +
				"#004ED:05000004";
		var c = new BmsStandardLoader().setSpec(spec()).setHandler(h).setAllowRedefine(false).load(b);
		assertFalse(h.calledParseError);
		assertNull(h.loadError);
		assertEquals(BmsInt.to36i("05"), c.getNote(BmsInt.to36i("ED"), 4, 0.0).getValue());
		assertEquals(BmsInt.to36i("02"), c.getNote(BmsInt.to36i("ED"), 4, 48.0).getValue());
		assertEquals(BmsInt.to36i("03"), c.getNote(BmsInt.to36i("ED"), 4, 96.0).getValue());
		assertEquals(BmsInt.to36i("04"), c.getNote(BmsInt.to36i("ED"), 4, 144.0).getValue());
		assertEquals(BmsInt.to36i("11"), c.getNote(BmsInt.to36i("ED"), 0, 6, 0.0).getValue());
		assertEquals(BmsInt.to36i("22"), c.getNote(BmsInt.to36i("ED"), 0, 6, 96.0).getValue());
	}

	// setIgnoreUnknownMeta(boolean)
	// 無効設定で不明メタ情報検出時、解析エラーが通知されfalseを返すと読み込みが停止すること
	@Test
	public void testSetIgnoreUnknownMetaBoolean_Accept() throws Exception {
		var ec = BmsLoadException.class;
		var h = new TestSettingsHandler(false);
		var bms = "#UNKNOWN_META xxxxx";
		var e = assertThrows(ec, () -> new BmsStandardLoader().setSpec(spec()).setHandler(h).setIgnoreUnknownMeta(false).load(bms));
		assertEquals(BmsErrorType.UNKNOWN_META, e.getError().getType());
		assertEquals(BmsErrorType.UNKNOWN_META, h.loadError.getType());
		assertTrue(h.calledParseError);
	}

	// setIgnoreUnknownMeta(boolean)
	// 無効設定で不明メタ情報検出時、解析エラーが通知されtrueを返すと読み込みを続行すること
	@Test
	public void testSetIgnoreUnknownMetaBoolean_Through() throws Exception {
		var h = new TestSettingsHandler(true);
		var bms = "#UNKNOWN_META xxxxx";
		var c = new BmsStandardLoader().setSpec(spec()).setHandler(h).setIgnoreUnknownMeta(false).load(bms);
		assertNotNull(c);
		assertEquals(BmsErrorType.UNKNOWN_META, h.loadError.getType());
		assertTrue(h.calledParseError);
	}

	// setIgnoreUnknownMeta(boolean)
	// 有効設定で不明メタ情報検出時、解析エラーが通知されず読み込みを続行すること
	@Test
	public void testSetIgnoreUnknownMetaBoolean_Ignore() throws Exception {
		var h = new TestSettingsHandler(false);
		var bms = "#UNKNOWN_META xxxxx";
		var c = new BmsStandardLoader().setSpec(spec()).setHandler(h).setIgnoreUnknownMeta(true).load(bms);
		assertNotNull(c);
		assertNull(h.loadError);
		assertFalse(h.calledParseError);
	}

	// setIgnoreUnknownChannel(boolean)
	// 無効設定で不明チャンネル検出時、解析エラーが通知されfalseを返すと読み込みが停止すること
	@Test
	public void testSetIgnoreUnknownChannelBoolean_Accept() throws Exception {
		var ec = BmsLoadException.class;
		var h = new TestSettingsHandler(false);
		var bms = "#000XX:112233";
		var e = assertThrows(ec, () -> new BmsStandardLoader().setSpec(spec()).setHandler(h).setIgnoreUnknownChannel(false).load(bms));
		assertEquals(BmsErrorType.UNKNOWN_CHANNEL, e.getError().getType());
		assertEquals(BmsErrorType.UNKNOWN_CHANNEL, h.loadError.getType());
		assertTrue(h.calledParseError);
	}

	// setIgnoreUnknownChannel(boolean)
	// 無効設定で不明チャンネル検出時、解析エラーが通知されtrueを返すと読み込みを続行すること
	@Test
	public void testSetIgnoreUnknownChannelBoolean_Through() throws Exception {
		var h = new TestSettingsHandler(true);
		var bms = "#000XX:112233";
		var c = new BmsStandardLoader().setSpec(spec()).setHandler(h).setIgnoreUnknownChannel(false).load(bms);
		assertNotNull(c);
		assertEquals(BmsErrorType.UNKNOWN_CHANNEL, h.loadError.getType());
		assertTrue(h.calledParseError);
	}

	// setIgnoreUnknownChannel(boolean)
	// 有効設定で不明チャンネル検出時、解析エラーが通知されず読み込みを続行すること
	@Test
	public void testSetIgnoreUnknownChannelBoolean_Ignore() throws Exception {
		var h = new TestSettingsHandler(false);
		var bms = "#000XX:112233";
		var c = new BmsStandardLoader().setSpec(spec()).setHandler(h).setIgnoreUnknownChannel(true).load(bms);
		assertNotNull(c);
		assertNull(h.loadError);
		assertFalse(h.calledParseError);
	}

	// setIgnoreWrongData(boolean)
	// 無効設定で不正データ(メタ情報)検出時、解析エラーが通知されfalseを返すと読み込みが停止すること
	@Test
	public void testSetIgnoreWrongDataBoolean_MetaAccept() throws Exception {
		var ec = BmsLoadException.class;
		var h = new TestSettingsHandler(false);
		var bms = "#BPM hoge";
		var e = assertThrows(ec, () -> new BmsStandardLoader().setSpec(spec()).setHandler(h).setIgnoreWrongData(false).load(bms));
		assertEquals(BmsErrorType.WRONG_DATA, e.getError().getType());
		assertEquals(BmsErrorType.WRONG_DATA, h.loadError.getType());
		assertTrue(h.calledParseError);
	}

	// setIgnoreWrongData(boolean)
	// 無効設定で不正データ(チャンネル)検出時、解析エラーが通知されfalseを返すと読み込みが停止すること
	@Test
	public void testSetIgnoreWrongDataBoolean_ChannelAccept() throws Exception {
		var ec = BmsLoadException.class;
		var h = new TestSettingsHandler(false);
		var bms = "#00007:NOT-ARRAY36";
		var e = assertThrows(ec, () -> new BmsStandardLoader().setSpec(spec()).setHandler(h).setIgnoreWrongData(false).load(bms));
		assertEquals(BmsErrorType.WRONG_DATA, e.getError().getType());
		assertEquals(BmsErrorType.WRONG_DATA, h.loadError.getType());
		assertTrue(h.calledParseError);
	}

	// setIgnoreWrongData(boolean)
	// 無効設定で不正データ(メタ情報)検出時、解析エラーが通知されtrueを返すと読み込みを続行すること
	@Test
	public void testSetIgnoreWrongDataBoolean_MetaThrough() throws Exception {
		var h = new TestSettingsHandler(true);
		var bms = "#BPM hoge";
		var c = new BmsStandardLoader().setSpec(spec()).setHandler(h).setIgnoreWrongData(false).load(bms);
		assertNotNull(c);
		assertEquals(BmsErrorType.WRONG_DATA, h.loadError.getType());
		assertTrue(h.calledParseError);
	}

	// setIgnoreWrongData(boolean)
	// 無効設定で不正データ(チャンネル)検出時、解析エラーが通知されtrueを返すと読み込みを続行すること
	@Test
	public void testSetIgnoreWrongDataBoolean_ChannelThrough() throws Exception {
		var h = new TestSettingsHandler(true);
		var bms = "#00007:NOT-ARRAY36";
		var c = new BmsStandardLoader().setSpec(spec()).setHandler(h).setIgnoreWrongData(false).load(bms);
		assertNotNull(c);
		assertEquals(BmsErrorType.WRONG_DATA, h.loadError.getType());
		assertTrue(h.calledParseError);
	}

	// setIgnoreWrongData(boolean)
	// 有効設定で不正データ(メタ情報)検出時、解析エラーが通知されず読み込みを続行すること
	@Test
	public void testSetIgnoreWrongDataBoolean_MetaIgnore() throws Exception {
		var h = new TestSettingsHandler(false);
		var bms = "#BPM hoge";
		var c = new BmsStandardLoader().setSpec(spec()).setHandler(h).setIgnoreWrongData(true).load(bms);
		assertNotNull(c);
		assertNull(h.loadError);
		assertFalse(h.calledParseError);
	}

	// setIgnoreWrongData(boolean)
	// 有効設定で不正データ(チャンネル)検出時、解析エラーが通知されず読み込みを続行すること
	@Test
	public void testSetIgnoreWrongDataBoolean_ChannelIgnore() throws Exception {
		var h = new TestSettingsHandler(false);
		var bms = "#00007:NOT-ARRAY36";
		var c = new BmsStandardLoader().setSpec(spec()).setHandler(h).setIgnoreWrongData(true).load(bms);
		assertNotNull(c);
		assertNull(h.loadError);
		assertFalse(h.calledParseError);
	}

	// setSkipReadTimeline()
	// 無効にするとタイムラインが読み込まれること
	@Test
	public void testSetSkipReadTimeline_Disable() throws Exception {
		var h = new TestSettingsHandler(false);
		var bms =
				";?bms key=\"value\"\n" +
				"#TITLE My Song\n" +
				"#00101:500\n" +
				"#00207:0102";
		var c = new BmsStandardLoader().setSpec(spec()).setHandler(h).setSkipReadTimeline(false).load(bms);
		assertNotNull(c);
		assertNull(h.loadError);
		assertEquals("value", c.getDeclaration("key"));
		assertEquals("My Song", c.getSingleMeta("#title"));
		assertEquals(500L, (long)c.getMeasureValue(BmsInt.to36i("01"), 1));
		assertEquals(BmsInt.to36i("01"), c.getNote(BmsInt.to36i("07"), 2, 0.0).getValue());
		assertEquals(BmsInt.to36i("02"), c.getNote(BmsInt.to36i("07"), 2, 96.0).getValue());
	}

	// setSkipReadTimeline()
	// デフォルトの値は「無効」になっていること
	@Test
	public void testSetSkipReadTimeline_DefaultDisable() throws Exception {
		var h = new TestSettingsHandler(false);
		var bms =
				";?bms key=\"value\"\n" +
				"#TITLE My Song\n" +
				"#00101:500\n" +
				"#00207:0102";
		var c = new BmsStandardLoader().setSpec(spec()).setHandler(h).load(bms);  // setSkipReadTimeline()を呼ばない
		assertNotNull(c);
		assertNull(h.loadError);
		assertEquals("value", c.getDeclaration("key"));
		assertEquals("My Song", c.getSingleMeta("#title"));
		assertEquals(500L, (long)c.getMeasureValue(BmsInt.to36i("01"), 1));
		assertEquals(BmsInt.to36i("01"), c.getNote(BmsInt.to36i("07"), 2, 0.0).getValue());
		assertEquals(BmsInt.to36i("02"), c.getNote(BmsInt.to36i("07"), 2, 96.0).getValue());
	}

	// setSkipReadTimeline()
	// 有効にするとタイムライン読み込みがスキップされること
	@Test
	public void testSetSkipReadTimeline_Enable_Normal() throws Exception {
		var h = new TestSettingsHandler(false);
		var bms =
				";?bms key=\"value\"\n" +
				"#TITLE My Song\n" +
				"#00101:500\n" +
				"#00207:0102";
		var c = new BmsStandardLoader().setSpec(spec()).setHandler(h).setSkipReadTimeline(true).load(bms);
		assertNotNull(c);
		assertNull(h.loadError);
		assertEquals("value", c.getDeclaration("key"));
		assertEquals("My Song", c.getSingleMeta("#title"));
		assertEquals(0, c.getMeasureCount());
		assertFalse(c.containsMeasureValue(BmsInt.to36i("01"), 1));
		assertNull(c.getNote(BmsInt.to36i("07"), 2, 0.0));
		assertNull(c.getNote(BmsInt.to36i("07"), 2, 96.0));
	}

	// setSkipReadTimeline()
	// 有効の状態でタイムラインのデータ定義にエラーがあっても無視してスキップされること
	@Test
	public void testSetSkipReadTimeline_Enable_DetectWrongData() throws Exception {
		var h = new TestSettingsHandler(false);
		var bms = "#00101:THIS_IS_NOT_INTEGER";
		var c = new BmsStandardLoader().setSpec(spec()).setHandler(h).setIgnoreWrongData(false).setSkipReadTimeline(true).load(bms);
		assertNotNull(c);
		assertNull(h.loadError);
		assertEquals(0, c.getMeasureCount());
		assertFalse(c.containsMeasureValue(BmsInt.to36i("01"), 1));
	}

	// setSkipReadTimeline()
	// 有効の状態でチャンネル未存在があっても無視してスキップされること(チャンネル定義の書式が正しいため)
	@Test
	public void testSetSkipReadTimeline_Enable_DetectUnknownChannel() throws Exception {
		var h = new TestSettingsHandler(true);
		var bms = "#001ZN:99";
		var c = new BmsStandardLoader().setSpec(spec()).setHandler(h).setIgnoreUnknownChannel(false).setSkipReadTimeline(true).load(bms);
		assertNotNull(c);
		assertNull(h.loadError);
		assertEquals(0, c.getMeasureCount());
		assertFalse(c.containsMeasureValue(BmsInt.to36i("01"), 1));
	}

	// setSkipReadTimeline()
	// 有効の状態で小節番号の書式不正があると構文エラーとなること
	@Test
	public void testSetSkipReadTimeline_Enable_DetectBadMeasure() throws Exception {
		var h = new TestSettingsHandler(true);
		var bms = "#00?01:500";
		var c = new BmsStandardLoader().setSpec(spec()).setHandler(h).setSyntaxErrorEnable(true).setSkipReadTimeline(true).load(bms);
		assertNotNull(c);
		assertNotNull(h.loadError);
		assertEquals(BmsErrorType.SYNTAX, h.loadError.getType());
	}

	// setSkipReadTimeline()
	// 有効の状態でチャンネル書式不正があると構文エラーとなること
	@Test
	public void testSetSkipReadTimeline_Enable_DetectBadChannel() throws Exception {
		var h = new TestSettingsHandler(true);
		var bms = "#001??:500";
		var c = new BmsStandardLoader().setSpec(spec()).setHandler(h).setSyntaxErrorEnable(true).setSkipReadTimeline(true).load(bms);
		assertNotNull(c);
		assertNotNull(h.loadError);
		assertEquals(BmsErrorType.SYNTAX, h.loadError.getType());
	}

	// setCharsets(Charset...)
	// 複数の文字セットが指定順に登録されること
	@Test
	@SuppressWarnings("unchecked")
	public void testSetCharsets_AddableMultiple() throws Exception {
		var l = new BmsStandardLoader().setSpec(spec()).setCharsets(
				StandardCharsets.US_ASCII,
				StandardCharsets.UTF_16LE,
				StandardCharsets.UTF_8);
		var cs = (List<Charset>)Tests.getf(l, "mCharsets");
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
		var l = new BmsStandardLoader().setSpec(spec()).setCharsets(
				StandardCharsets.US_ASCII,
				StandardCharsets.UTF_16LE,
				StandardCharsets.US_ASCII,
				StandardCharsets.UTF_16LE,
				StandardCharsets.UTF_8,
				StandardCharsets.UTF_8,
				StandardCharsets.UTF_16LE);
		var cs = (List<Charset>)Tests.getf(l, "mCharsets");
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
		var l = new BmsStandardLoader().setSpec(spec()).setCharsets();
		var cs = (List<Charset>)Tests.getf(l, "mCharsets");
		assertEquals(0, cs.size());
	}

	// setCharsets(Charset...)
	// NullPointerException charsetsにnullが含まれている
	@Test
	public void testSetCharsets_HasNull() throws Exception {
		var l = new BmsStandardLoader().setSpec(spec());
		assertThrows(NullPointerException.class, () -> l.setCharsets(StandardCharsets.UTF_16BE, null));
		assertThrows(NullPointerException.class, () -> l.setCharsets(null, StandardCharsets.UTF_16BE));
	}

	// load(File)
	// 正常
	@Test
	public void testLoadFile() {
		BmsContent c = null;
		var l = new BmsStandardLoader();
		l.setSpec(spec());
		l.setHandler(HANDLER_ERR_STOP);
		try {
			c = l.load(new File(getTestLoadNormalyCasePathString()));
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.toString());
		}
		assertTestLoadNormalyCaseContent(c);
	}

	// load(File)
	// NullPointerException bmsがnull
	@Test
	public void testLoadFile_NullBms() {
		var l = loader();
		assertThrows(NullPointerException.class, () -> l.load((File)null));
	}

	// load(File)
	// IllegalStateException BMS仕様が設定されていない
	@Test
	public void testLoadFile_NoSpec() {
		var l = loader().setSpec(null);
		assertThrows(IllegalStateException.class, () -> l.load(new File("temp")));
	}

	// load(File)
	// IllegalStateException BMS読み込みハンドラが設定されていない
	@Test
	public void testLoadFile_NoHandler() {
		var l = loader().setHandler(null);
		assertThrows(IllegalStateException.class, () -> l.load(new File("temp")));
	}

	// load(File)
	// IOException 指定されたファイルが見つからない
	@Test
	public void testLoadFile_FileNotFound() {
		var l = loader();
		assertThrows(IOException.class, () -> l.load(new File("temp")));
	}

	// load(File)
	// IOException 指定されたファイルの読み取り権限がない
	@Test
	public void testLoadFile_Deny() {
		// Do nothing: 試験不可
	}

	// load(File)
	// IOException 指定されたファイルの読み取り中に異常を検出した
	@Test
	public void testLoadFile_IoError() {
		// Do nothing: 試験不可
	}

	// load(File)
	// BmsLoadException ハンドラ(BmsLoadHandler#parseError)がfalseを返した
	@Test
	public void testLoadFile_ParseError() {
		var l = loader();
		var f = testDataPath(TEST_FILE_SYNTAX_ERROR).toFile();
		assertThrows(BmsLoadException.class, () -> l.load(f));
	}

	// load(File)
	// BmsException 読み込み処理中に想定外の例外がスローされた
	@Test
	public void testLoadFile_Unexpected() {
		var l = loader().setHandler(HANDLER_NULL_CONTENT);
		var f = testDataPath(TEST_FILE_EMPTY).toFile();
		assertThrows(BmsException.class, () -> l.load(f));
	}

	// load(Path)
	// 正常
	@Test
	public void testLoadPath() {
		BmsContent c = null;
		var l = new BmsStandardLoader();
		l.setSpec(spec());
		l.setHandler(HANDLER_ERR_STOP);
		try {
			c = l.load(Paths.get(getTestLoadNormalyCasePathString()));
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.toString());
		}
		assertTestLoadNormalyCaseContent(c);
	}

	// load(Path)
	// NullPointerException bmsがnull
	@Test
	public void testLoadPath_NullBms() {
		var l = loader();
		assertThrows(NullPointerException.class, () -> l.load((Path)null));
	}

	// load(Path)
	// IllegalStateException BMS仕様が設定されていない
	@Test
	public void testLoadPath_NoSpec() {
		var l = loader().setSpec(null);
		assertThrows(IllegalStateException.class, () -> l.load(Path.of("temp")));
	}

	// load(Path)
	// IllegalStateException BMS読み込みハンドラが設定されていない
	@Test
	public void testLoadPath_NoHandler() {
		var l = loader().setHandler(null);
		assertThrows(IllegalStateException.class, () -> l.load(Path.of("temp")));
	}

	// load(Path)
	// IOException 指定されたファイルが見つからない
	@Test
	public void testLoadPath_FileNotFound() {
		var l = loader();
		assertThrows(IOException.class, () -> l.load(Path.of("temp")));
	}

	// load(Path)
	// IOException 指定されたファイルの読み取り権限がない
	@Test
	public void testLoadPath_Deny() {
		// Do nothing: 試験不可
	}

	// load(Path)
	// IOException 指定されたファイルの読み取り中に異常を検出した
	@Test
	public void testLoadPath_IoError() {
		// Do nothing: 試験不可
	}

	// load(Path)
	// BmsLoadException ハンドラ(BmsLoadHandler#parseError)がfalseを返した
	@Test
	public void testLoadPath_ParseError() {
		var l = loader();
		var p = testDataPath(TEST_FILE_SYNTAX_ERROR);
		assertThrows(BmsLoadException.class, () -> l.load(p));
	}

	// load(Path)
	// BmsException 読み込み処理中に想定外の例外がスローされた
	@Test
	public void testLoadPath_Unexpected() {
		var l = loader().setHandler(HANDLER_NULL_CONTENT);
		var p = testDataPath(TEST_FILE_EMPTY);
		assertThrows(BmsException.class, () -> l.load(p));
	}

	// 正常
	@Test
	public void testLoadInputStream_001() {
		BmsContent c = null;
		var l = new BmsStandardLoader();
		l.setSpec(spec());
		l.setHandler(HANDLER_ERR_STOP);
		try {
			c = l.load(new FileInputStream(getTestLoadNormalyCasePathString()));
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.toString());
		}
		assertTestLoadNormalyCaseContent(c);
	}

	// load(InputStream)
	// NullPointerException bmsがnull
	@Test
	public void testLoadInputStream_NullBms() {
		var l = loader();
		assertThrows(NullPointerException.class, () -> l.load((InputStream)null));
	}

	// load(InputStream)
	// IllegalStateException BMS仕様が設定されていない
	@Test
	public void testLoadInputStream_NoSpec() {
		var l = loader().setSpec(null);
		assertThrows(IllegalStateException.class, () -> l.load(new EmptyInputStream()));
	}

	// load(InputStream)
	// IllegalStateException BMS読み込みハンドラが設定されていない
	@Test
	public void testLoadInputStream_NoHandler() {
		var l = loader().setHandler(null);
		assertThrows(IllegalStateException.class, () -> l.load(new EmptyInputStream()));
	}

	// load(InputStream)
	// IOException 指定されたファイルの読み取り中に異常を検出した
	@Test
	public void testLoadInputStream_IoError() {
		var l = loader();
		assertThrows(IOException.class, () -> l.load(new IOExceptionInputStream()));
	}

	// load(InputStream)
	// BmsLoadException ハンドラ(BmsLoadHandler#parseError)がfalseを返した
	@Test
	public void testLoadInputStream_ParseError() throws Exception {
		var l = loader();
		var f = testDataPath(TEST_FILE_SYNTAX_ERROR).toFile();
		try (var s = new FileInputStream(f)) {
			assertThrows(BmsLoadException.class, () -> l.load(s));
		}
	}

	// load(InputStream)
	// BmsException 読み込み処理中に想定外の例外がスローされた
	@Test
	public void testLoadInputStream_Unexpected() throws Exception {
		var l = loader().setHandler(HANDLER_NULL_CONTENT);
		var f = testDataPath(TEST_FILE_EMPTY).toFile();
		try (var s = new FileInputStream(f)) {
			assertThrows(BmsException.class, () -> l.load(s));
		}
	}

	// load(byte[])
	// 正常
	@Test
	public void testLoadByteArray() {
		BmsContent c = null;
		var l = new BmsStandardLoader();
		l.setSpec(spec());
		l.setHandler(HANDLER_ERR_STOP);
		try {
			byte[] b = Files.readAllBytes(Paths.get(getTestLoadNormalyCasePathString()));
			c = l.load(b);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.toString());
		}
		assertTestLoadNormalyCaseContent(c);
	}

	// load(byte[])
	// BOM付きUTF-8のBMSが正常に読み込めること
	@Test
	public void testLoadByteArray_Utf8WithBom() throws BmsException {
		var c = load();
		assertEquals(TITLE_FOR_TEST_DECODE, (String)c.getSingleMeta("#title"));
	}

	// load(byte[])
	// BOM付きUTF-16LEのBMSが正常に読み込めること
	@Test
	public void testLoadByteArray_Utf16leWithBom() throws BmsException {
		var c = load();
		assertEquals(TITLE_FOR_TEST_DECODE, (String)c.getSingleMeta("#title"));
	}

	// load(byte[])
	// BOM付きUTF-16BEのBMSが正常に読み込めること
	@Test
	public void testLoadByteArray_Utf16beWithBom() throws BmsException {
		var c = load();
		assertEquals(TITLE_FOR_TEST_DECODE, (String)c.getSingleMeta("#title"));
	}

	// load(byte[])
	// デフォルト文字セット使用：最優先文字セットのBMSが正常に読み込めること
	@Test
	public void testLoadByteArray_DefaultCharsets_Primary() throws BmsException {
		BmsLibrary.setDefaultCharsets(Charset.forName("MS932"), StandardCharsets.UTF_8);
		var c = load();
		assertEquals(TITLE_FOR_TEST_DECODE, (String)c.getSingleMeta("#title"));
	}

	// load(byte[])
	// デフォルト文字セット使用：2番目の文字セットのBMSが正常に読み込めること
	@Test
	public void testLoadByteArray_DefaultCharsets_Secondary() throws BmsException {
		BmsLibrary.setDefaultCharsets(Charset.forName("MS932"), StandardCharsets.UTF_8);
		var c = load();
		assertEquals(TITLE_FOR_TEST_DECODE, (String)c.getSingleMeta("#title"));
	}

	// load(byte[])
	// デフォルト文字セット使用：全文字セットでのデコード失敗で、最優先文字セットでの強制デコードが行われること
	@Test
	public void testLoadByteArray_DefaultCharsets_FailedDecode() throws BmsException {
		BmsLibrary.setDefaultCharsets(Charset.forName("MS932"), StandardCharsets.UTF_16LE);
		var c = load();
		var m = (String)c.getSingleMeta("#title");
		assertNotEquals(TITLE_FOR_TEST_DECODE, m);
		assertTrue(m.startsWith(TITLE_FOR_TEST_DECODE_START)); // ASCII部分は正常の想定
	}

	// load(byte[])
	// ローダに文字セット指定：最優先文字セットのBMSが正常に読み込めること
	@Test
	public void testLoadByteArray_AddCharsets_Primary() throws BmsException {
		var c = load(Charset.forName("MS932"), StandardCharsets.UTF_8);
		assertEquals(TITLE_FOR_TEST_DECODE, (String)c.getSingleMeta("#title"));
	}

	// load(byte[])
	// ローダに文字セット指定：2番目の文字セットのBMSが正常に読み込めること
	@Test
	public void testLoadByteArray_AddCharsets_Secondary() throws BmsException {
		var c = load(Charset.forName("MS932"), StandardCharsets.UTF_8);
		assertEquals(TITLE_FOR_TEST_DECODE, (String)c.getSingleMeta("#title"));
	}

	// load(byte[])
	// ローダに文字セット指定：全文字セットでのデコード失敗で、最優先文字セットでの強制デコードが行われること
	@Test
	public void testLoadByteArray_AddCharsets_FailedDecode() throws BmsException {
		var c = load(Charset.forName("MS932"), StandardCharsets.UTF_16LE);
		var m = (String)c.getSingleMeta("#title");
		assertNotEquals(TITLE_FOR_TEST_DECODE, m);
		assertTrue(m.startsWith(TITLE_FOR_TEST_DECODE_START)); // ASCII部分は正常の想定
	}

	// load(byte[])
	// デコードバッファよりも大きいサイズのファイルを正しくデコードできること
	@Test
	public void testLoadByteArray_LargerThanDecodeBuffer() throws BmsException {
		var c = load(Charset.forName("MS932"), StandardCharsets.UTF_8);
		assertEquals(TITLE_FOR_TEST_DECODE, (String)c.getSingleMeta("#title"));
	}

	// load(byte[])
	// NullPointerException bmsがnull
	@Test
	public void testLoadByteArray_NullBms() {
		var l = loader();
		assertThrows(NullPointerException.class, () -> l.load((byte[])null));
	}

	// load(byte[])
	// IllegalStateException BMS仕様が設定されていない
	@Test
	public void testLoadByteArray_NoSpec() {
		var l = loader().setSpec(null);
		assertThrows(IllegalStateException.class, () -> l.load(new byte[] {}));
	}

	// load(byte[])
	// IllegalStateException BMS読み込みハンドラが設定されていない
	@Test
	public void testLoadByteArray_NoHandler() {
		var l = loader().setHandler(null);
		assertThrows(IllegalStateException.class, () -> l.load(new byte[] {}));
	}

	// load(byte[])
	// BmsLoadException ハンドラ(BmsLoadHandler#parseError)がfalseを返した
	@Test
	public void testLoadByteArray_ParseError() throws Exception {
		var l = loader();
		var p = testDataPath(TEST_FILE_SYNTAX_ERROR);
		var a = Files.readAllBytes(p);
		assertThrows(BmsLoadException.class, () -> l.load(a));
	}

	// load(byte[])
	// BmsException 読み込み処理中に想定外の例外がスローされた
	@Test
	public void testLoadByteArray_Unexpected() throws Exception {
		var l = loader().setHandler(HANDLER_NULL_CONTENT);
		var p = testDataPath(TEST_FILE_EMPTY);
		var a = Files.readAllBytes(p);
		assertThrows(BmsException.class, () -> l.load(a));
	}

	// load(String)
	// 正常
	@Test
	public void testLoadString() {
		BmsContent c = null;
		var l = new BmsStandardLoader();
		l.setSpec(spec());
		l.setHandler(HANDLER_ERR_STOP);
		try {
			Path path = Paths.get(getTestLoadNormalyCasePathString());
			List<String> lines = Files.readAllLines(path, Charset.forName("UTF-8"));
			StringBuilder sb = new StringBuilder();
			lines.forEach(str -> { sb.append(str); sb.append("\n"); });
			c = l.load(sb.toString());
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.toString());
		}
		assertTestLoadNormalyCaseContent(c);
	}

	// load(String)
	// UnsupportedOperationException 入力データがバイナリフォーマットのローダで当メソッドを呼び出した
	@Test
	public void testLoadString_BinaryFormat() {
		var l = new TestLoaderBody(false, true).setSpec(spec());
		assertThrows(UnsupportedOperationException.class, () -> l.load("script"));
	}

	// load(String)
	// NullPointerException bmsがnull
	@Test
	public void testLoadString_NullBms() {
		var l = loader();
		assertThrows(NullPointerException.class, () -> l.load((String)null));
	}

	// load(String)
	// IllegalStateException BMS仕様が設定されていない
	@Test
	public void testLoadString_NoSpec() {
		var l = loader().setSpec(null);
		assertThrows(IllegalStateException.class, () -> l.load(""));
	}

	// load(String)
	// IllegalStateException BMS読み込みハンドラが設定されていない
	@Test
	public void testLoadString_NoHandler() {
		var l = loader().setHandler(null);
		assertThrows(IllegalStateException.class, () -> l.load(""));
	}

	// load(String)
	// BmsLoadException ハンドラ(BmsLoadHandler#parseError)がfalseを返した
	@Test
	public void testLoadString_ParseError() {
		var l = loader();
		assertThrows(BmsLoadException.class, () -> l.load("_SYNTAX_ERROR_"));
	}

	// load(String)
	// BmsException 読み込み処理中に想定外の例外がスローされた
	@Test
	public void testLoadString_Unexpected() {
		var l = loader().setHandler(HANDLER_NULL_CONTENT);
		assertThrows(BmsException.class, () -> l.load("#TITLE MySong"));
	}

	// load(Reader)
	// UnsupportedOperationException 入力データがバイナリフォーマットのローダで当メソッドを呼び出した
	@Test
	public void testLoadReader_BinaryFormat() {
		var r = new StringReader("script");
		var l = new TestLoaderBody(false, true).setSpec(spec());
		assertThrows(UnsupportedOperationException.class, () -> l.load(r));
	}

	// load(Reader)
	// NullPointerException bmsがnull
	@Test
	public void testLoadReader_NullBms() {
		var l = loader();
		assertThrows(NullPointerException.class, () -> l.load((Reader)null));
	}

	// load(Reader)
	// IllegalStateException BMS仕様が設定されていない
	@Test
	public void testLoadReader_NoSpec() {
		var l = loader().setSpec(null);
		assertThrows(IllegalStateException.class, () -> l.load(new StringReader("")));
	}

	// load(Reader)
	// IllegalStateException BMS読み込みハンドラが設定されていない
	@Test
	public void testLoadReader_NoHandler() {
		var l = loader().setHandler(null);
		assertThrows(IllegalStateException.class, () -> l.load(new StringReader("")));
	}

	// load(Reader)
	// IOException 指定されたファイルの読み取り中に異常を検出した
	@Test
	public void testLoadReader_IoError() {
		var l = loader();
		assertThrows(IOException.class, () -> l.load(new IOExceptionReader()));
	}

	// load(Reader)
	// BmsLoadException ハンドラ(BmsLoadHandler#parseError)がfalseを返した
	@Test
	public void testLoadReader_ParseError() throws Exception {
		var l = loader();
		assertThrows(BmsLoadException.class, () -> l.load(new StringReader("_SYNTAX_ERROR_")));
	}

	// load(Reader)
	// BmsException 読み込み処理中に想定外の例外がスローされた
	@Test
	public void testLoadReader_Unexpected() throws Exception {
		var l = loader().setHandler(HANDLER_NULL_CONTENT);
		assertThrows(BmsException.class, () -> l.load(new StringReader("")));
	}

	// load(Any)
	// 重複可能チャンネルは空配列データ(#MMMCC:00)も含め、解析順に読み込まれること
	@Test
	public void testLoadAny_Multiple_EmptyArray() {
		var c = load();
		var ch = BmsInt.to36i("M7");
		// 1小節目
		assertEquals(6, c.getChannelDataCount(ch, 1));
		assertEquals(1, c.countNotes(BmsPoint.of(1, 0.0), BmsPoint.of(2, 0.0), n -> n.getIndex() == 0));
		assertEquals(BmsInt.to36i("AA"), c.getNote(ch, 0, 1, 0.0).getValue());
		assertEquals(0, c.countNotes(BmsPoint.of(1, 0.0), BmsPoint.of(2, 0.0), n -> n.getIndex() == 1));
		assertEquals(1, c.countNotes(BmsPoint.of(1, 0.0), BmsPoint.of(2, 0.0), n -> n.getIndex() == 2));
		assertEquals(BmsInt.to36i("BB"), c.getNote(ch, 2, 1, 0.0).getValue());
		assertEquals(0, c.countNotes(BmsPoint.of(1, 0.0), BmsPoint.of(2, 0.0), n -> n.getIndex() == 3));
		assertEquals(0, c.countNotes(BmsPoint.of(1, 0.0), BmsPoint.of(2, 0.0), n -> n.getIndex() == 4));
		assertEquals(1, c.countNotes(BmsPoint.of(1, 0.0), BmsPoint.of(2, 0.0), n -> n.getIndex() == 5));
		assertEquals(BmsInt.to36i("CC"), c.getNote(ch, 5, 1, 0.0).getValue());
		// 2小節目
		assertEquals(3, c.getChannelDataCount(ch, 2));
		assertEquals(1, c.countNotes(BmsPoint.of(2, 0.0), BmsPoint.of(3, 0.0), n -> n.getIndex() == 0));
		assertEquals(BmsInt.to36i("XX"), c.getNote(ch, 0, 2, 0.0).getValue());
		assertEquals(0, c.countNotes(BmsPoint.of(2, 0.0), BmsPoint.of(3, 0.0), n -> n.getIndex() == 1));
		assertEquals(1, c.countNotes(BmsPoint.of(2, 0.0), BmsPoint.of(3, 0.0), n -> n.getIndex() == 2));
		assertEquals(BmsInt.to36i("YY"), c.getNote(ch, 2, 2, 0.0).getValue());
	}

	// load(Any)
	// 重複可能チャンネルは空配列データ(#MMMCC:00)以降に空でない配列データがない場合、末尾の空配列は読み込まないこと
	@Test
	public void testLoadAny_Multiple_StripTailEmptyArray() {
		var c = load();
		var ch = BmsInt.to36i("M7");
		// 1小節目
		assertEquals(3, c.getChannelDataCount(ch, 1));
		assertEquals(0, c.countNotes(BmsPoint.of(1, 0.0), BmsPoint.of(2, 0.0), n -> n.getIndex() == 0));
		assertEquals(0, c.countNotes(BmsPoint.of(1, 0.0), BmsPoint.of(2, 0.0), n -> n.getIndex() == 1));
		assertEquals(1, c.countNotes(BmsPoint.of(1, 0.0), BmsPoint.of(2, 0.0), n -> n.getIndex() == 2));
		assertEquals(BmsInt.to36i("XX"), c.getNote(ch, 2, 1, 0.0).getValue());
		// 2小節目
		assertEquals(2, c.getChannelDataCount(ch, 2));
		assertEquals(1, c.countNotes(BmsPoint.of(2, 0.0), BmsPoint.of(3, 0.0), n -> n.getIndex() == 0));
		assertEquals(BmsInt.to36i("YY"), c.getNote(ch, 0, 2, 0.0).getValue());
		assertEquals(1, c.countNotes(BmsPoint.of(2, 0.0), BmsPoint.of(3, 0.0), n -> n.getIndex() == 1));
		assertEquals(BmsInt.to36i("ZZ"), c.getNote(ch, 1, 2, 0.0).getValue());
	}

	// load(Any)
	// 重複可能チャンネルは、解析順に見て小節番号が前後しても、解析順に読み込まれること
	@Test
	public void testLoadAny_Multiple_ShuffledMeasure() {
		var c = load();
		var ch = BmsInt.to36i("M7");
		// 1小節目
		assertEquals(3, c.getChannelDataCount(ch, 1));
		assertEquals(1, c.countNotes(BmsPoint.of(1, 0.0), BmsPoint.of(2, 0.0), n -> n.getIndex() == 0));
		assertEquals(BmsInt.to36i("XX"), c.getNote(ch, 0, 1, 0.0).getValue());
		assertEquals(0, c.countNotes(BmsPoint.of(1, 0.0), BmsPoint.of(2, 0.0), n -> n.getIndex() == 1));
		assertEquals(1, c.countNotes(BmsPoint.of(1, 0.0), BmsPoint.of(2, 0.0), n -> n.getIndex() == 2));
		assertEquals(BmsInt.to36i("ZZ"), c.getNote(ch, 2, 1, 0.0).getValue());
		// 2小節目
		assertEquals(2, c.getChannelDataCount(ch, 2));
		assertEquals(0, c.countNotes(BmsPoint.of(2, 0.0), BmsPoint.of(3, 0.0), n -> n.getIndex() == 0));
		assertEquals(1, c.countNotes(BmsPoint.of(2, 0.0), BmsPoint.of(3, 0.0), n -> n.getIndex() == 1));
		assertEquals(BmsInt.to36i("YY"), c.getNote(ch, 1, 2, 0.0).getValue());
	}

	// load(Any)
	// 解析開始処理の結果でnullが返るとBmsExceptionがスローされること
	@Test
	public void testLoadAny_StartParse_ReturnNull() {
		var l = testParserLoader(new TestParserLoader() {
			@Override protected BmsErrorParsed beginParse(BmsLoaderSettings settings, BmsSource source) { return null; }
		});
		assertThrows(BmsException.class, () -> l.load(""));
	}

	// load(Any)
	// 解析開始処理の結果がエラーありで返るとBmsLoadExceptionがスローされ、返されたエラーが設定されること
	@Test
	public void testLoadAny_StartParse_ReturnError() throws Exception {
		var l = testParserLoader(new TestParserLoader() {
			@Override protected BmsErrorParsed beginParse(BmsLoaderSettings settings, BmsSource source) {
				var err = new BmsScriptError(BmsErrorType.SYNTAX, 10, "Line", "Msg", new RuntimeException());
				return new BmsErrorParsed(err);
			}
		});
		var e = assertThrows(BmsLoadException.class, () -> l.load(""));
		var err = (BmsScriptError)e.getError();
		assertNotNull(err);
		assertEquals(BmsErrorType.SYNTAX, err.getType());
		assertEquals(10, err.getLineNumber());
		assertEquals("Line", err.getLine());
		assertEquals("Msg", err.getMessage());
		assertEquals(RuntimeException.class, err.getCause().getClass());
	}

	// load(Any)
	// 解析開始処理内で実行時例外がスローされるとBmsExceptionがスローされ、発生した実行時例外が内包されていること
	@Test
	public void testLoadAny_StartParse_Exception() throws Exception {
		var l = testParserLoader(new TestParserLoader() {
			@Override protected BmsErrorParsed beginParse(BmsLoaderSettings settings, BmsSource source) {
				throw new IllegalArgumentException();
			}
		});
		var e = assertThrows(BmsException.class, () -> l.load(""));
		assertEquals(IllegalArgumentException.class, e.getCause().getClass());
	}

	// load(Any)
	// バイナリフォーマットのローダでは入力データがバイナリになること
	@Test
	public void testLoadAny_StartParse_BinaryFormat() throws Exception {
		var b = new byte[16];
		var l = (TestLoaderBody)(new TestLoaderBody(false, true).setSpec(spec()));
		var c = l.load(b);
		assertNotNull(c);
		assertNotNull(l.source);
		assertSame(b, l.source.getAsBinary());
	}

	// load(Any)
	// テキストフォーマットのローダでは入力データがテキストになること
	@Test
	public void testLoadAny_StartParse_TextFormat() throws Exception {
		var t = "script";
		var l = (TestLoaderBody)(new TestLoaderBody(false, false).setSpec(spec()));
		var c = l.load(t);
		assertNotNull(c);
		assertNotNull(l.source);
		assertSame(t, l.source.getAsScript());
	}

	// load(Any)
	// 解析終了処理の結果でnullが返るとBmsExceptionがスローされること
	@Test
	public void testLoadAny_EndParse_ReturnNull() {
		var l = testParserLoader(new TestParserLoader() {
			@Override protected BmsErrorParsed endParse() { return null; }
		});
		assertThrows(BmsException.class, () -> l.load(""));
	}

	// load(Any)
	// 解析終了処理の結果がエラーありで返るとBmsLoadExceptionがスローされ、返されたエラーが設定されること
	@Test
	public void testLoadAny_EndParse_ReturnError() throws Exception {
		var l = testParserLoader(new TestParserLoader() {
			@Override protected BmsErrorParsed endParse() {
				var err = new BmsScriptError(BmsErrorType.SYNTAX, 10, "Line", "Msg", new RuntimeException());
				return new BmsErrorParsed(err);
			}
		});
		var e = assertThrows(BmsLoadException.class, () -> l.load(""));
		var err = (BmsScriptError)e.getError();
		assertNotNull(err);
		assertEquals(BmsErrorType.SYNTAX, err.getType());
		assertEquals(10, err.getLineNumber());
		assertEquals("Line", err.getLine());
		assertEquals("Msg", err.getMessage());
		assertEquals(RuntimeException.class, err.getCause().getClass());
	}

	// load(Any)
	// 解析終了処理の結果がエラーありで返っても、解析開始処理の結果がエラーありの場合は解析開始処理のエラーが優先されること
	@Test
	public void testLoadAny_EndParse_ReturnError_BeginToo() throws Exception {
		var l = testParserLoader(new TestParserLoader() {
			@Override protected BmsErrorParsed beginParse(BmsLoaderSettings settings, BmsSource source) {
				var err = new BmsScriptError(BmsErrorType.SPEC_VIOLATION, 50, "#META", "Error", new NullPointerException());
				return new BmsErrorParsed(err);
			}
			@Override protected BmsErrorParsed endParse() {
				var err = new BmsScriptError(BmsErrorType.SYNTAX, 10, "Line", "Msg", new RuntimeException());
				return new BmsErrorParsed(err);
			}
		});
		var e = assertThrows(BmsLoadException.class, () -> l.load(""));
		var err = (BmsScriptError)e.getError();
		assertNotNull(err);
		assertEquals(BmsErrorType.SPEC_VIOLATION, err.getType());
		assertEquals(50, err.getLineNumber());
		assertEquals("#META", err.getLine());
		assertEquals("Error", err.getMessage());
		assertEquals(NullPointerException.class, err.getCause().getClass());
	}

	// load(Any)
	// 解析終了処理内で実行時例外がスローされるとBmsExceptionがスローされ、発生した実行時例外が内包されていること
	@Test
	public void testLoadAny_EndParse_Exception() throws Exception {
		var l = testParserLoader(new TestParserLoader() {
			@Override protected BmsErrorParsed endParse() { throw new IllegalArgumentException(); }
		});
		var e = assertThrows(BmsException.class, () -> l.load(""));
		assertEquals(IllegalArgumentException.class, e.getCause().getClass());
	}

	// load(Any)
	// BMSコンテンツ要素の解析中に実行時例外がスローされるとBmsExceptionがスローされ、発生した実行時例外が内包されていること
	@Test
	public void testLoadAny_NextElement_Exception() throws Exception {
		var l = testParserLoader(new TestParserLoader() {
			@Override protected BmsParsed nextElement() { throw new ClassCastException(); }
		});
		var e = assertThrows(BmsException.class, () -> l.load(""));
		assertEquals(ClassCastException.class, e.getCause().getClass());
	}

	// load(Any)
	// 基数選択：索引付きメタ情報のインデックス、基数選択数値型、基数選択数値配列型が選択された基数で解析されること(16)
	@Test
	public void testLoadAny_BaseChanger_Base16() throws Exception {
		var i = BmsInt.base16();
		var c = load(
				"#BASE 16",
				"#SBASE 00",
				"#SBASE2 7f",
				"#SBASE3 FF",
				"#SARRAY FF7fa000",
				"#IINTEGER00 100",
				"#IINTEGER3F 200",
				"#IINTEGERff 300",
				"#00017:00",
				"#00117:cF",
				"#00217:Ff",
				"#00019:6c00FF00");
		assertEquals(16L, (long)c.getSingleMeta("#base"));
		assertEquals((long)i.toi("00"), (long)c.getSingleMeta("#sbase"));
		assertEquals((long)i.toi("7F"), (long)c.getSingleMeta("#sbase2"));
		assertEquals((long)i.toi("FF"), (long)c.getSingleMeta("#sbase3"));
		assertEquals("FF7FA000", c.getSingleMeta("#sarray").toString());
		assertEquals(100L, (long)c.getIndexedMeta("#iinteger", i.toi("00")));
		assertEquals(200L, (long)c.getIndexedMeta("#iinteger", i.toi("3F")));
		assertEquals(300L, (long)c.getIndexedMeta("#iinteger", i.toi("FF")));
		assertEquals((long)i.toi("00"), (long)c.getMeasureValue(BmsInt.to36i("17"), 0));
		assertEquals((long)i.toi("CF"), (long)c.getMeasureValue(BmsInt.to36i("17"), 1));
		assertEquals((long)i.toi("FF"), (long)c.getMeasureValue(BmsInt.to36i("17"), 2));
		assertEquals(i.toi("6C"), c.getNote(BmsInt.to36i("19"), 0, 0.0).getValue());
		assertEquals(i.toi("FF"), c.getNote(BmsInt.to36i("19"), 0, 96.0).getValue());
	}

	// load(Any)
	// 基数選択：索引付きメタ情報のインデックス、基数選択数値型、基数選択数値配列型が選択された基数で解析されること(36)
	@Test
	public void testLoadAny_BaseChanger_Base36() throws Exception {
		var i = BmsInt.base36();
		var c = load(
				"#BASE 36",
				"#SBASE 00",
				"#SBASE2 7g",
				"#SBASE3 ZZ",
				"#SARRAY ZZAgx000",
				"#IINTEGER00 100",
				"#IINTEGER3g 200",
				"#IINTEGERzz 300",
				"#00017:00",
				"#00117:kZ",
				"#00217:Zz",
				"#00019:8p00ZZ00");
		assertEquals(36L, (long)c.getSingleMeta("#base"));
		assertEquals((long)i.toi("00"), (long)c.getSingleMeta("#sbase"));
		assertEquals((long)i.toi("7G"), (long)c.getSingleMeta("#sbase2"));
		assertEquals((long)i.toi("ZZ"), (long)c.getSingleMeta("#sbase3"));
		assertEquals("ZZAGX000", c.getSingleMeta("#sarray").toString());
		assertEquals(100L, (long)c.getIndexedMeta("#iinteger", i.toi("00")));
		assertEquals(200L, (long)c.getIndexedMeta("#iinteger", i.toi("3G")));
		assertEquals(300L, (long)c.getIndexedMeta("#iinteger", i.toi("ZZ")));
		assertEquals((long)i.toi("00"), (long)c.getMeasureValue(BmsInt.to36i("17"), 0));
		assertEquals((long)i.toi("KZ"), (long)c.getMeasureValue(BmsInt.to36i("17"), 1));
		assertEquals((long)i.toi("ZZ"), (long)c.getMeasureValue(BmsInt.to36i("17"), 2));
		assertEquals(i.toi("8P"), c.getNote(BmsInt.to36i("19"), 0, 0.0).getValue());
		assertEquals(i.toi("ZZ"), c.getNote(BmsInt.to36i("19"), 0, 96.0).getValue());
	}

	// load(Any)
	// 基数選択：索引付きメタ情報のインデックス、基数選択数値型、基数選択数値配列型が選択された基数で解析されること(62)
	@Test
	public void testLoadAny_BaseChanger_Base62() throws Exception {
		var i = BmsInt.base62();
		var c = load(
				"#BASE 62",
				"#SBASE 00",
				"#SBASE2 7g",
				"#SBASE3 ZZ",
				"#SARRAY ZZAgx000",
				"#IINTEGER00 100",
				"#IINTEGER3g 200",
				"#IINTEGERzz 300",
				"#00017:00",
				"#00117:kZ",
				"#00217:zz",
				"#00019:8p00zz00");
		assertEquals(62L, (long)c.getSingleMeta("#base"));
		assertEquals((long)i.toi("00"), (long)c.getSingleMeta("#sbase"));
		assertEquals((long)i.toi("7g"), (long)c.getSingleMeta("#sbase2"));
		assertEquals((long)i.toi("ZZ"), (long)c.getSingleMeta("#sbase3"));
		assertEquals("ZZAgx000", c.getSingleMeta("#sarray").toString());
		assertEquals(100L, (long)c.getIndexedMeta("#iinteger", i.toi("00")));
		assertEquals(200L, (long)c.getIndexedMeta("#iinteger", i.toi("3g")));
		assertEquals(300L, (long)c.getIndexedMeta("#iinteger", i.toi("zz")));
		assertEquals((long)i.toi("00"), (long)c.getMeasureValue(BmsInt.to36i("17"), 0));
		assertEquals((long)i.toi("kZ"), (long)c.getMeasureValue(BmsInt.to36i("17"), 1));
		assertEquals((long)i.toi("zz"), (long)c.getMeasureValue(BmsInt.to36i("17"), 2));
		assertEquals(i.toi("8p"), c.getNote(BmsInt.to36i("19"), 0, 0.0).getValue());
		assertEquals(i.toi("zz"), c.getNote(BmsInt.to36i("19"), 0, 96.0).getValue());
	}

	// load(Any)
	// 基数選択：選択した基数の値の範囲を超える、または書式を誤るとWRONG_DATAエラーが発生すること(16)
	@Test
	public void testLoadAny_BaseChanger_Base16_WrongData() throws Exception {
		var e = new ArrayList<BmsScriptError>();
		var c = load(l -> l, e::add,
				"#BASE 16",
				"#SBASE G0",
				"#SBASE2 0?",
				"#SARRAY 1122G0",
				"#IINTEGERg0 100",
				"#IINTEGER0! 200",
				"#00017:G0",
				"#00117:0$",
				"#00019:11g0");
		assertEquals(16L, (long)c.getSingleMeta("#base"));
		assertEquals(8, e.size());
		assertEquals(8L, e.stream().filter(se -> se.getType() == BmsErrorType.WRONG_DATA).count());
	}

	// load(Any)
	// 基数選択：選択した基数の値の範囲を超える、または書式を誤るとWRONG_DATAエラーが発生すること(36)
	@Test
	public void testLoadAny_BaseChanger_Base36_WrongData() throws Exception {
		var e = new ArrayList<BmsScriptError>();
		var c = load(l -> l, e::add,
				"#BASE 36",
				"#SBASE Z!",
				"#SBASE2 123",
				"#SARRAY ZZgg0#",
				"#SARRAY2 0011223",
				"#IINTEGER$9 100",
				"#00017:c?",
				"#00117:123",
				"#00019:11%X",
				"#00119:0011223");
		assertEquals(36L, (long)c.getSingleMeta("#base"));
		assertEquals(9, e.size());
		assertEquals(9L, e.stream().filter(se -> se.getType() == BmsErrorType.WRONG_DATA).count());
	}

	// load(Any)
	// 基数選択：選択した基数の値の範囲を超える、または書式を誤るとWRONG_DATAエラーが発生すること(62)
	@Test
	public void testLoadAny_BaseChanger_Base62_WrongData() throws Exception {
		var e = new ArrayList<BmsScriptError>();
		var c = load(l -> l, e::add,
				"#BASE 62",
				"#SBASE z!",
				"#SBASE2 123",
				"#SARRAY zzGG0#",
				"#SARRAY2 0011223",
				"#IINTEGER$9 100",
				"#00017:c?",
				"#00117:123",
				"#00019:11%x",
				"#00119:0011223");
		assertEquals(62L, (long)c.getSingleMeta("#base"));
		assertEquals(9, e.size());
		assertEquals(9L, e.stream().filter(se -> se.getType() == BmsErrorType.WRONG_DATA).count());
	}

	// load(Any)
	// 基数選択：基数が未選択の場合、36として処理されること
	@Test
	public void testLoadAny_BaseChanger_DefaultBase() throws Exception {
		var i = BmsInt.base36();
		var c = load(
				"#SBASE 7g",
				"#SARRAY ZZAgx000",
				"#IINTEGER3g 100",
				"#00017:kZ",
				"#00019:8p00ZZ00");
		assertEquals(36L, (long)c.getSingleMeta("#base"));
		assertEquals((long)i.toi("7G"), (long)c.getSingleMeta("#sbase"));
		assertEquals("ZZAGX000", c.getSingleMeta("#sarray").toString());
		assertEquals(100L, (long)c.getIndexedMeta("#iinteger", i.toi("3G")));
		assertEquals((long)i.toi("KZ"), (long)c.getMeasureValue(BmsInt.to36i("17"), 0));
		assertEquals(i.toi("8P"), c.getNote(BmsInt.to36i("19"), 0, 0.0).getValue());
		assertEquals(i.toi("ZZ"), c.getNote(BmsInt.to36i("19"), 0, 96.0).getValue());
	}

	// load(Any)
	// 基数選択：基数の定義が各データの後方でも、記述された基数が選択されること
	@Test
	public void testLoadAny_BaseChanger_DefineAtLater() throws Exception {
		var i = BmsInt.base62();
		var c = load(
				"#SBASE 7g",
				"#SARRAY ZZAgx000",
				"#IINTEGER3g 100",
				"#00017:kZ",
				"#00019:8p00zz00",
				"#BASE 62");
		assertEquals(62L, (long)c.getSingleMeta("#base"));
		assertEquals((long)i.toi("7g"), (long)c.getSingleMeta("#sbase"));
		assertEquals("ZZAgx000", c.getSingleMeta("#sarray").toString());
		assertEquals(100L, (long)c.getIndexedMeta("#iinteger", i.toi("3g")));
		assertEquals((long)i.toi("kZ"), (long)c.getMeasureValue(BmsInt.to36i("17"), 0));
		assertEquals(i.toi("8p"), c.getNote(BmsInt.to36i("19"), 0, 0.0).getValue());
		assertEquals(i.toi("zz"), c.getNote(BmsInt.to36i("19"), 0, 96.0).getValue());
	}

	// load(Any)
	// 基数選択：再定義許可で基数が複数定義された場合、最後の基数が選択されること
	@Test
	public void testLoadAny_BaseChanger_MultipleDefine_Allow() throws Exception {
		var i = BmsInt.base16();
		var c = load(l -> l.setAllowRedefine(true), e -> {},
				"#BASE 62",
				"#BASE 16",
				"#SBASE 7f",
				"#SARRAY FF7fa000",
				"#IINTEGER3F 100",
				"#00017:cF",
				"#00019:6c00FF00");
		assertEquals(16L, (long)c.getSingleMeta("#base"));
		assertEquals((long)i.toi("7F"), (long)c.getSingleMeta("#sbase"));
		assertEquals("FF7FA000", c.getSingleMeta("#sarray").toString());
		assertEquals(100L, (long)c.getIndexedMeta("#iinteger", i.toi("3F")));
		assertEquals((long)i.toi("CF"), (long)c.getMeasureValue(BmsInt.to36i("17"), 0));
		assertEquals(i.toi("6C"), c.getNote(BmsInt.to36i("19"), 0, 0.0).getValue());
		assertEquals(i.toi("FF"), c.getNote(BmsInt.to36i("19"), 0, 96.0).getValue());
	}

	// load(Any)
	// 基数選択：再定義不許可で基数が複数定義された場合、最初の基数が選択され、2回目以降はREDEFINEエラーになること
	@Test
	public void testLoadAny_BaseChanger_MultipleDefine_Deny() throws Exception {
		var i = BmsInt.base16();
		var e = new ArrayList<BmsScriptError>();
		var c = load(l -> l, e::add,
				"#BASE 16",
				"#BASE 36",
				"#BASE 62",
				"#SBASE 7f",
				"#SARRAY FF7fa000",
				"#IINTEGER3F 100",
				"#00017:cF",
				"#00019:6c00FF00");
		assertEquals(16L, (long)c.getSingleMeta("#base"));
		assertEquals((long)i.toi("7F"), (long)c.getSingleMeta("#sbase"));
		assertEquals("FF7FA000", c.getSingleMeta("#sarray").toString());
		assertEquals(100L, (long)c.getIndexedMeta("#iinteger", i.toi("3F")));
		assertEquals((long)i.toi("CF"), (long)c.getMeasureValue(BmsInt.to36i("17"), 0));
		assertEquals(i.toi("6C"), c.getNote(BmsInt.to36i("19"), 0, 0.0).getValue());
		assertEquals(i.toi("FF"), c.getNote(BmsInt.to36i("19"), 0, 96.0).getValue());
		assertEquals(2, e.size());
		assertEquals(2L, e.stream().filter(er -> er.getType() == BmsErrorType.REDEFINE).count());
	}

	// load(Any)
	// 基数選択：基数が16, 36, 62以外だとWRONG_DATAエラーが発生すること
	@Test
	public void testLoadAny_BaseChanger_UnsupportBase() throws Exception {
		var e = new ArrayList<BmsScriptError>();
		var c = load(l -> l, e::add, "#BASE 10");
		assertEquals(36L, (long)c.getSingleMeta("#base"));
		assertEquals(1, e.size());
		assertEquals(BmsErrorType.WRONG_DATA, e.get(0).getType());
	}

	// load(Any)
	// 基数選択：基数に数値以外が入っているとWRONG_DATAエラーが発生すること
	@Test
	public void testLoadAny_BaseChanger_WrongBase() throws Exception {
		var e = new ArrayList<BmsScriptError>();
		var c = load(l -> l, e::add, "#BASE Q2");
		assertEquals(36L, (long)c.getSingleMeta("#base"));
		assertEquals(1, e.size());
		assertEquals(BmsErrorType.WRONG_DATA, e.get(0).getType());
	}

	private BmsSpec spec() {
		return BmsTest.createTestSpec();
	}

	private String getTestLoadNormalyCasePathString() {
		return testDataPath("testLoad_NormalyCase.bms").toString();
	}

	private void assertTestLoadNormalyCaseContent(BmsContent c) {
		assertNotNull(c);
		//c.dump(System.out);

		var decls = c.getDeclarations();
		assertEquals(3, decls.size());
		assertEquals("UTF-8", decls.get("encoding"));
		assertEquals("value1", decls.get("mykey1"));
		assertEquals("value2", decls.get("mykey2"));

		assertEquals("Test", c.getSingleMeta("#genre"));
		assertEquals("My Song", c.getSingleMeta("#title"));
		assertEquals("NoName", c.getSingleMeta("#artist"));
		assertEquals(160.0, (double)c.getSingleMeta("#bpm"), 0.00001);
		assertEquals(500L, (long)c.getSingleMeta("#sinteger"));
		assertEquals("Text 1", c.getMultipleMeta("#mstring", 0));
		assertEquals("Text 2", c.getMultipleMeta("#mstring", 1));
		assertEquals(10.25, (double)c.getIndexedMeta("#ifloat", Integer.parseInt("0A", 36)), 0.00001);
		assertEquals(7.89, (double)c.getIndexedMeta("#ifloat", Integer.parseInt("M0", 36)), 0.00001);
		assertEquals(370.65, (double)c.getIndexedMeta("#ifloat", Integer.parseInt("ZZ", 36)), 0.00001);

		assertEquals(1200L, (long)c.getMeasureValue(Integer.parseInt("01", 36), 0));
		assertEquals("This is the last measure", c.getMeasureValue(Integer.parseInt("03", 36), 999));

		var ch = Integer.parseInt("07", 36);
		assertEquals(Integer.parseInt("11", 36), c.getNote(ch, 0, 0.0).getValue());
		assertEquals(Integer.parseInt("22", 36), c.getNote(ch, 1, 48.0).getValue());
		assertEquals(Integer.parseInt("22", 36), c.getNote(ch, 1, 144.0).getValue());
		assertEquals(Integer.parseInt("33", 36), c.getNote(ch, 3, 0.0).getValue());
		assertEquals(Integer.parseInt("33", 36), c.getNote(ch, 3, 24.0).getValue());
		assertEquals(Integer.parseInt("44", 36), c.getNote(ch, 3, 96.0).getValue());
		assertEquals(Integer.parseInt("44", 36), c.getNote(ch, 3, 120.0).getValue());
	}

	private TestParserLoader testParserLoader(TestParserLoader base) {
		base.setSpec(spec()).setHandler(HANDLER_ERR_STOP);
		return base;
	}
}
