package com.lmt.lib.bms;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class BmsSaverTest {
	private static Path sTestDataDir;

	private static class NormalSaver extends BmsSaver {
		@Override protected void onWrite(BmsContent content, OutputStream dst) throws IOException, BmsException {
			dst.write('a');
		}
	}

	private static class IOExceptionSaver extends BmsSaver {
		@Override protected void onWrite(BmsContent content, OutputStream dst) throws IOException, BmsException {
			throw new IOException();
		}
	}

	private static class RuntimeExceptionSaver extends BmsSaver {
		@Override protected void onWrite(BmsContent content, OutputStream dst) throws IOException, BmsException {
			throw new RuntimeException();
		}
	}

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		sTestDataDir = Tests.mktmpdir(BmsSaverTest.class);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		Tests.rmtmpdir(BmsSaverTest.class);
	}

	// save(BmsContent Path)
	// BMSコンテンツが指定パスのファイルへ出力されること(出力内容の合否は実装先の書き込み処理テストで検証する)
	@Test
	public void testSaveBmsContentPath_Okay() throws Exception {
		var path = sTestDataDir.resolve("testSaveBmsContentPath_Okay");
		var content = new BmsContent(BmsTest.createTestSpec());
		new NormalSaver().save(content, path);
		assertTrue(Files.size(path) > 0);
	}

	// save(BmsContent Path)
	// NullPointerException contentがnull
	@Test
	public void testSaveBmsContentPath_NullContent() throws Exception {
		var saver = new NormalSaver();
		assertThrows(NullPointerException.class, () -> saver.save(null, sTestDataDir));
	}

	// save(BmsContent Path)
	// NullPointerException dstがnull
	@Test
	public void testSaveBmsContentPath_NullDst() throws Exception {
		var content = new BmsContent(BmsTest.createTestSpec());
		var saver = new NormalSaver();
		assertThrows(NullPointerException.class, () -> saver.save(content, (Path)null));
	}

	// save(BmsContent Path)
	// IllegalArgumentException contentが参照モードではない
	@Test
	public void testSaveBmsContentPath_NotReferenceMode() throws Exception {
		var content = new BmsContent(BmsTest.createTestSpec());
		var saver = new NormalSaver();
		content.beginEdit();
		assertThrows(IllegalArgumentException.class, () -> saver.save(content, sTestDataDir));
	}

	// save(BmsContent Path)
	// IOException 指定パスへのファイル作成失敗
	@Test
	public void testSaveBmsContentPath_FailedCreateFile() throws Exception {
		var content = new BmsContent(BmsTest.createTestSpec());
		var saver = new NormalSaver();
		assertThrows(IOException.class, () -> saver.save(content, sTestDataDir));  // 出力先が既存ディレクトリ
	}

	// save(BmsContent Path)
	// IOException BMSコンテンツの出力失敗
	@Test
	public void testSaveBmsContentPath_FailedOutput() throws Exception {
		var path = sTestDataDir.resolve("testSaveBmsContentPath_FailedOutput");
		var content = new BmsContent(BmsTest.createTestSpec());
		var saver = new IOExceptionSaver();
		assertThrows(IOException.class, () -> saver.save(content, path));
	}

	// save(BmsContent Path)
	// BmsException BMSコンテンツ出力時、BMSに関連する要因で出力処理がエラー終了した
	@Test
	public void testSaveBmsContentPath_Unexpected() throws Exception {
		var path = sTestDataDir.resolve("testSaveBmsContentPath_Unexpected");
		var content = new BmsContent(BmsTest.createTestSpec());
		var saver = new RuntimeExceptionSaver();
		try {
			saver.save(content, path);
			fail("Expects throw BmsException, but not thrown.");
		} catch (BmsException e) {
			assertEquals(RuntimeException.class, e.getCause().getClass());
		} catch (Exception e) {
			fail(String.format("Expects throw BmsException, but other. (%s)", e.getClass().getSimpleName()));
		}
	}

	// save(BmsContent, File)
	// BMSコンテンツが指定ファイルへ出力されること(出力内容の合否は実装先の書き込み処理テストで検証する)
	@Test
	public void testSaveBmsContentFile_Okay() throws Exception {
		var path = sTestDataDir.resolve("testSaveBmsContentFile_Okay");
		var content = new BmsContent(BmsTest.createTestSpec());
		new NormalSaver().save(content, path.toFile());
		assertTrue(Files.size(path) > 0);
	}

	// save(BmsContent, File)
	// NullPointerException contentがnull
	@Test
	public void testSaveBmsContentFile_NullContent() throws Exception {
		var saver = new NormalSaver();
		assertThrows(NullPointerException.class, () -> saver.save(null, sTestDataDir.toFile()));
	}

	// save(BmsContent, File)
	// NullPointerException dstがnull
	@Test
	public void testSaveBmsContentFile_NullDst() throws Exception {
		var content = new BmsContent(BmsTest.createTestSpec());
		var saver = new NormalSaver();
		assertThrows(NullPointerException.class, () -> saver.save(content, (File)null));
	}

	// save(BmsContent, File)
	// IllegalArgumentException contentが参照モードではない
	@Test
	public void testSaveBmsContentFile_NotReferenceMode() throws Exception {
		var content = new BmsContent(BmsTest.createTestSpec());
		var saver = new NormalSaver();
		content.beginEdit();
		assertThrows(IllegalArgumentException.class, () -> saver.save(content, sTestDataDir.toFile()));
	}

	// save(BmsContent, File)
	// IOException 指定ファイルへのファイル作成失敗
	@Test
	public void testSaveBmsContentFile_FailedCreateFile() throws Exception {
		var content = new BmsContent(BmsTest.createTestSpec());
		var saver = new NormalSaver();
		assertThrows(IOException.class, () -> saver.save(content, sTestDataDir.toFile()));  // 出力先が既存ディレクトリ
	}

	// save(BmsContent, File)
	// IOException BMSコンテンツの出力失敗
	@Test
	public void testSaveBmsContentFile_FailedOutput() throws Exception {
		var path = sTestDataDir.resolve("testSaveBmsContentFile_FailedOutput");
		var content = new BmsContent(BmsTest.createTestSpec());
		var saver = new IOExceptionSaver();
		assertThrows(IOException.class, () -> saver.save(content, path.toFile()));
	}

	// save(BmsContent, File)
	// BmsException BMSコンテンツ出力時、BMSに関連する要因で出力処理がエラー終了した
	@Test
	public void testSaveBmsContentFile_Unexpected() throws Exception {
		var path = sTestDataDir.resolve("testSaveBmsContentFile_Unexpected");
		var content = new BmsContent(BmsTest.createTestSpec());
		var saver = new RuntimeExceptionSaver();
		try {
			saver.save(content, path.toFile());
			fail("Expects throw BmsException, but not thrown.");
		} catch (BmsException e) {
			assertEquals(RuntimeException.class, e.getCause().getClass());
		} catch (Exception e) {
			fail(String.format("Expects throw BmsException, but other. (%s)", e.getClass().getSimpleName()));
		}
	}

	// save(BmsContent, OutputStream)
	// BMSコンテンツがストリームへ出力されること(出力内容の合否は実装先の書き込み処理テストで検証する)
	@Test
	public void testSaveBmsContentOutputStream_Okay() throws Exception {
		var content = new BmsContent(BmsTest.createTestSpec());
		var os = new ByteArrayOutputStream();
		new NormalSaver().save(content, os);
		assertTrue(os.toByteArray().length > 0);
	}

	// save(BmsContent, OutputStream)
	// NullPointerException contentがnull
	@Test
	public void testSaveBmsContentOutputStream_NullContent() throws Exception {
		var saver = new NormalSaver();
		assertThrows(NullPointerException.class, () -> saver.save(null, new ByteArrayOutputStream()));
	}

	// save(BmsContent, OutputStream)
	// NullPointerException dstがnull
	@Test
	public void testSaveBmsContentOutputStream_NullDst() throws Exception {
		var content = new BmsContent(BmsTest.createTestSpec());
		var saver = new NormalSaver();
		assertThrows(NullPointerException.class, () -> saver.save(content, (OutputStream)null));
	}

	// save(BmsContent, OutputStream)
	// IllegalArgumentException contentが参照モードではない
	@Test
	public void testSaveBmsContentOutputStream_NotReferenceMode() throws Exception {
		var content = new BmsContent(BmsTest.createTestSpec());
		var saver = new NormalSaver();
		content.beginEdit();
		assertThrows(IllegalArgumentException.class, () -> saver.save(content, new ByteArrayOutputStream()));
	}

	// save(BmsContent, OutputStream)
	// IOException BMSコンテンツ出力時、異常を検知した
	@Test
	public void testSaveBmsContentOutputStream_FailedOutput() throws Exception {
		var content = new BmsContent(BmsTest.createTestSpec());
		var saver = new IOExceptionSaver();
		assertThrows(IOException.class, () -> saver.save(content, new ByteArrayOutputStream()));
	}

	// save(BmsContent, OutputStream)
	// BmsException BMSコンテンツ出力時、BMSに関連する要因で出力処理がエラー終了した
	@Test
	public void testSaveBmsContentOutputStream_Unexpected() throws Exception {
		var content = new BmsContent(BmsTest.createTestSpec());
		var saver = new RuntimeExceptionSaver();
		try {
			saver.save(content, new ByteArrayOutputStream());
			fail("Expects throw BmsException, but not thrown.");
		} catch (BmsException e) {
			assertEquals(RuntimeException.class, e.getCause().getClass());
		} catch (Exception e) {
			fail(String.format("Expects throw BmsException, but other. (%s)", e.getClass().getSimpleName()));
		}
	}
}
