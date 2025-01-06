package com.lmt.lib.bms;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(OrderedRunner.class)
public class BmsLibraryTest {
	private static List<Charset> sOrgCharsets;

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

	// getVersion()
	// 正しい書式のバージョン文字列が取得できること
	@Test
	@Order(order = 99)
	public void testGetVersion() {
		var ver = BmsLibrary.getVersion();
		assertNotNull(ver);
		assertTrue(ver.matches("^[0-9]+\\.[0-9]+\\.[0-9]+$"));
	}

	// getPrimaryCharset()
	// 何も指定しなければデフォルトの最優先文字セットは MS932 であること
	@Test
	@Order(order = 0)
	public void testGetPrimaryCharset_Default() {
		assertEquals(Charset.forName("MS932"), BmsLibrary.getPrimaryCharset());
	}

	// getPrimaryCharset()
	// 先頭で指定された文字セットが最優先文字セットとなること
	@Test
	@Order(order = 1)
	public void testGetPrimaryCharset_TopIsPrimary() {
		BmsLibrary.setDefaultCharsets(
				StandardCharsets.UTF_8,
				StandardCharsets.US_ASCII,
				StandardCharsets.UTF_16);
		assertEquals(StandardCharsets.UTF_8, BmsLibrary.getPrimaryCharset());
	}

	// getDefaultCharsets()
	// 何も指定しなければ MS932 が1件だけ返り、リストは変更不可であること
	@Test
	@Order(order = 0)
	public void testGetDefaultCharsets_Default() {
		var list = BmsLibrary.getDefaultCharsets();
		assertEquals(1, list.size());
		assertEquals(Charset.forName("MS932"), list.get(0));
		assertThrows(UnsupportedOperationException.class, () -> list.add(StandardCharsets.UTF_8));
	}

	// getDefaultCharsets()
	// 設定された文字セットリストの順番と同じ順のリストが返ること
	@Test
	@Order(order = 1)
	public void testGetDefaultCharsets_Order() {
		BmsLibrary.setDefaultCharsets(
				StandardCharsets.UTF_8,
				StandardCharsets.US_ASCII,
				StandardCharsets.UTF_16);
		var list = BmsLibrary.getDefaultCharsets();
		assertEquals(3, list.size());
		assertEquals(StandardCharsets.UTF_8, list.get(0));
		assertEquals(StandardCharsets.US_ASCII, list.get(1));
		assertEquals(StandardCharsets.UTF_16, list.get(2));
	}

	// getDefaultCharsets()
	// 指定時の重複した文字セットは、後発の文字セットが除外されユニークなリストが返ること
	@Test
	@Order(order = 1)
	public void testGetDefaultCharsets_Unique() {
		BmsLibrary.setDefaultCharsets(
				StandardCharsets.US_ASCII,
				StandardCharsets.UTF_8,
				StandardCharsets.UTF_8,
				StandardCharsets.UTF_16,
				StandardCharsets.US_ASCII,
				StandardCharsets.UTF_8,
				StandardCharsets.ISO_8859_1,
				StandardCharsets.UTF_16);
		var list = BmsLibrary.getDefaultCharsets();
		assertEquals(4, list.size());
		assertEquals(StandardCharsets.US_ASCII, list.get(0));
		assertEquals(StandardCharsets.UTF_8, list.get(1));
		assertEquals(StandardCharsets.UTF_16, list.get(2));
		assertEquals(StandardCharsets.ISO_8859_1, list.get(3));
	}

	// getDefaultCharsets()
	// 設定された文字セットリストは変更不可であること
	@Test
	@Order(order = 1)
	public void testGetDefaultCharsets_Unmodifiable() {
		BmsLibrary.setDefaultCharsets(StandardCharsets.UTF_8);
		var list = BmsLibrary.getDefaultCharsets();
		assertThrows(UnsupportedOperationException.class, () -> list.add(StandardCharsets.US_ASCII));
	}

	// setDefaultCharsets(Charset...)
	// IllegalArgumentException 文字セットが0件
	@Test
	@Order(order = 1)
	public void testSetDefaultCharsets_NoCharset() {
		assertThrows(IllegalArgumentException.class, () -> BmsLibrary.setDefaultCharsets());
	}

	// setDefaultCharsets(Charset...)
	// NullPointerException 文字セットにnullが含まれている
	@Test
	@Order(order = 1)
	public void testSetDefaultCharsets_HasNull() {
		assertThrows(NullPointerException.class, () -> BmsLibrary.setDefaultCharsets(null, StandardCharsets.UTF_8));
		assertThrows(NullPointerException.class, () -> BmsLibrary.setDefaultCharsets(StandardCharsets.UTF_8, null));
	}

	// printLogo()
	// ロゴが標準出力に出力されること
	@Test
	@Order(order = 99)
	public void testPrintLogo() {
		var outOrg = System.out;
		try {
			var baos = new ByteArrayOutputStream();
			var ps = new PrintStream(baos);
			System.setOut(ps);
			BmsLibrary.printLogo();
			var bais = new ByteArrayInputStream(baos.toByteArray());
			var br = new BufferedReader(new InputStreamReader(bais));
			var lines = br.lines().collect(Collectors.toList());
			testPrintLogoAssertion(lines);
		} finally {
			System.setOut(outOrg);
		}
	}

	// printLogo(PrintStream)
	// ロゴが指定された出力ストリームに出力されること
	@Test
	@Order(order = 99)
	public void testPrintLogoPrintStream_Normal() {
		var baos = new ByteArrayOutputStream();
		var ps = new PrintStream(baos);
		BmsLibrary.printLogo(ps);
		var bais = new ByteArrayInputStream(baos.toByteArray());
		var br = new BufferedReader(new InputStreamReader(bais));
		var lines = br.lines().collect(Collectors.toList());
		testPrintLogoAssertion(lines);
	}

	// printLogo()のアサーション
	private void testPrintLogoAssertion(List<String> lines) {
		assertEquals(5, lines.size());
		assertEquals("   _____  ___ __  _____", lines.get(0));
		assertEquals("  / __  \\/ _ '_ \\/ ____)", lines.get(1));
		assertEquals(" / __  </ // // /\\___ \\", lines.get(2));
		assertEquals("/______/_//_//_/(_____/ Library v" + BmsLibrary.getVersion(), lines.get(3));
		assertEquals("======================================", lines.get(4));
	}

	// printLogo(PrintStream)
	// NullPointerException psがnull
	@Test
	@Order(order = 99)
	public void testPrintLogoPrintStream_NullStream() {
		assertThrows(NullPointerException.class, () -> BmsLibrary.printLogo(null));
	}
}
