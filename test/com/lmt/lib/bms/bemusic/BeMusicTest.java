package com.lmt.lib.bms.bemusic;

import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.Path;

import org.junit.Test;

import com.lmt.lib.bms.BmsErrorType;
import com.lmt.lib.bms.BmsLoadException;
import com.lmt.lib.bms.BmsStandardLoader;
import com.lmt.lib.bms.UseTestData;

public class BeMusicTest implements UseTestData {
	@Override
	public Path testDataDirectory() {
		return Path.of("test", "com", "lmt", "lib", "bms", "bemusic", "testdata", "bemusic");
	}

	@Override
	public String testDataExtension() {
		return "bms";
	}

	// createLoaderFor(Path)
	// 拡張子が「bmson」のパスでbmson用ローダが生成されること
	@Test
	public void testCreateLoaderFor_Bmson() {
		var l = BeMusic.createLoaderFor(Path.of("music.bmson"));
		assertEquals(BeMusicBmsonLoader.class, l.getClass());
	}

	// createLoaderFor(Path)
	// 拡張子が「bmson」(英字大小がバラバラ)のパスでbmson用ローダが生成されること
	@Test
	public void testCreateLoaderFor_BmsonIgnoreCase() {
		var l = BeMusic.createLoaderFor(Path.of("music.BmSoN"));
		assertEquals(BeMusicBmsonLoader.class, l.getClass());
	}

	// createLoaderFor(Path)
	// 拡張子が「bms」のパスで標準ローダが生成されること
	@Test
	public void testCreateLoaderFor_Bms() {
		var l = BeMusic.createLoaderFor(Path.of("music.bms"));
		assertEquals(BmsStandardLoader.class, l.getClass());
	}

	// createLoaderFor(Path)
	// 拡張子が「bme」のパスで標準ローダが生成されること
	@Test
	public void testCreateLoaderFor_Bme() {
		var l = BeMusic.createLoaderFor(Path.of("music.bme"));
		assertEquals(BmsStandardLoader.class, l.getClass());
	}

	// createLoaderFor(Path)
	// 拡張子が「bml」のパスで標準ローダが生成されること
	@Test
	public void testCreateLoaderFor_Bml() {
		var l = BeMusic.createLoaderFor(Path.of("music.bml"));
		assertEquals(BmsStandardLoader.class, l.getClass());
	}

	// createLoaderFor(Path)
	// 拡張子がないパスで標準ローダが生成されること
	@Test
	public void testCreateLoaderFor_NoExtension() {
		var l = BeMusic.createLoaderFor(Path.of("music"));
		assertEquals(BmsStandardLoader.class, l.getClass());
		var l2 = BeMusic.createLoaderFor(Path.of("music."));
		assertEquals(BmsStandardLoader.class, l2.getClass());
	}

	// createLoaderFor(Path)
	// NullPointerException pathがnull
	@Test
	public void testCreateLoaderFor_NullPath() {
		assertThrows(NullPointerException.class, () -> BeMusic.createLoaderFor(null));
	}

	// loadContentFrom(Path, Long, boolean)
	// エラーのない一般的なBMSファイルが読み込めること(シングルプレー)
	@Test
	public void testLoadContentFrom_CorrectlySp() throws Exception {
		var c = BeMusic.loadContentFrom(testDataPath(), null, true);
		assertNotNull(c);
	}

	// loadContentFrom(Path, Long, boolean)
	// エラーのない一般的なBMSファイルが読み込めること(ダブルプレー)
	@Test
	public void testLoadContentFrom_CorrectlyDp() throws Exception {
		var c = BeMusic.loadContentFrom(testDataPath(), null, true);
		assertNotNull(c);
	}

	// loadContentFrom(Path, Long, boolean)
	// 寛容読み込みで、構文エラーのあるBMSファイルが読み込めること
	@Test
	public void testLoadContentFrom_TolerantlySyntaxError() throws Exception {
		var c = BeMusic.loadContentFrom(testDataPath(), null, false);
		assertNotNull(c);
	}

	// loadContentFrom(Path, Long, boolean)
	// 寛容読み込みで、仕様違反の値があるBMSファイルが読み込めること
	@Test
	public void testLoadContentFrom_TolerantlySpecViolation() throws Exception {
		var c = BeMusic.loadContentFrom(testDataPath(), null, false);
		assertNotNull(c);
	}

	// loadContentFrom(Path, Long, boolean)
	// 寛容読み込みで、未知のメタ情報が定義されたBMSファイルが読み込めること
	@Test
	public void testLoadContentFrom_TolerantlyUnknownMeta() throws Exception {
		var c = BeMusic.loadContentFrom(testDataPath(), null, false);
		assertNotNull(c);
	}

	// loadContentFrom(Path, Long, boolean)
	// 寛容読み込みで、未知のチャンネルが定義されたBMSファイルが読み込めること
	@Test
	public void testLoadContentFrom_TolerantlyUnknownChannel() throws Exception {
		var c = BeMusic.loadContentFrom(testDataPath(), null, false);
		assertNotNull(c);
	}

	// loadContentFrom(Path, Long, boolean)
	// 寛容読み込みで、メタ情報に不正な値を設定したBMSファイルが読み込めること
	@Test
	public void testLoadContentFrom_TolerantlyWrongMeta() throws Exception {
		var c = BeMusic.loadContentFrom(testDataPath(), null, false);
		assertNotNull(c);
	}

	// loadContentFrom(Path, Long, boolean)
	// 寛容読み込みで、チャンネルに不正な値を設定したBMSファイルが読み込めること
	@Test
	public void testLoadContentFrom_TolerantlyWrongChannel() throws Exception {
		var c = BeMusic.loadContentFrom(testDataPath(), null, false);
		assertNotNull(c);
	}

	// loadContentFrom(Path, Long, boolean)
	// 寛容読み込みで、CONTROL FLOWの制御が不正なBMSファイルが読み込めること
	@Test
	public void testLoadContentFrom_TolerantlyControlFlow() throws Exception {
		var c = BeMusic.loadContentFrom(testDataPath(), null, false);
		assertNotNull(c);
	}

	// loadContentFrom(Path, Long, boolean)
	// 厳格読み込みで、構文エラーのあるBMSファイルが読み込めないこと
	@Test
	public void testLoadContentFrom_StrictlySyntaxError() throws Exception {
		var f = testDataPath();
		var e = assertThrows(BmsLoadException.class, () -> BeMusic.loadContentFrom(f, null, true));
		assertEquals(e.getError().toString(), BmsErrorType.SYNTAX, e.getError().getType());
	}

	// loadContentFrom(Path, Long, boolean)
	// 厳格読み込みで、仕様違反の値があるBMSファイルが読み込めないこと
	@Test
	public void testLoadContentFrom_StrictlySpecViolation() throws Exception {
		var f = testDataPath();
		var e = assertThrows(BmsLoadException.class, () -> BeMusic.loadContentFrom(f, null, true));
		assertEquals(e.getError().toString(), BmsErrorType.SPEC_VIOLATION, e.getError().getType());
	}

	// loadContentFrom(Path, Long, boolean)
	// 厳格読み込みで、未知のメタ情報が定義されたBMSファイルが読み込めないこと
	@Test
	public void testLoadContentFrom_StrictlyUnknownMeta() throws Exception {
		var f = testDataPath();
		var e = assertThrows(BmsLoadException.class, () -> BeMusic.loadContentFrom(f, null, true));
		assertEquals(e.getError().toString(), BmsErrorType.UNKNOWN_META, e.getError().getType());
	}

	// loadContentFrom(Path, Long, boolean)
	// 厳格読み込みで、未知のチャンネルが定義されたBMSファイルが読み込めないこと
	@Test
	public void testLoadContentFrom_StrictlyUnknownChannel() throws Exception {
		var f = testDataPath();
		var e = assertThrows(BmsLoadException.class, () -> BeMusic.loadContentFrom(f, null, true));
		assertEquals(e.getError().toString(), BmsErrorType.UNKNOWN_CHANNEL, e.getError().getType());
	}

	// loadContentFrom(Path, Long, boolean)
	// 厳格読み込みで、メタ情報に不正な値を設定したBMSファイルが読み込めないこと
	@Test
	public void testLoadContentFrom_StrictlyWrongMeta() throws Exception {
		var f = testDataPath();
		var e = assertThrows(BmsLoadException.class, () -> BeMusic.loadContentFrom(f, null, true));
		assertEquals(e.getError().toString(), BmsErrorType.WRONG_DATA, e.getError().getType());
	}

	// loadContentFrom(Path, Long, boolean)
	// 厳格読み込みで、チャンネルに不正な値を設定したBMSファイルが読み込めないこと
	@Test
	public void testLoadContentFrom_StrictlyWrongChannel() throws Exception {
		var f = testDataPath();
		var e = assertThrows(BmsLoadException.class, () -> BeMusic.loadContentFrom(f, null, true));
		assertEquals(e.getError().toString(), BmsErrorType.WRONG_DATA, e.getError().getType());
	}

	// loadContentFrom(Path, Long, boolean)
	// 厳格読み込みで、CONTROL FLOWの制御が不正なBMSファイルが読み込めないこと
	@Test
	public void testLoadContentFrom_StrictlyControlFlow() throws Exception {
		var f = testDataPath();
		var e = assertThrows(BmsLoadException.class, () -> BeMusic.loadContentFrom(f, null, true));
		assertEquals(e.getError().toString(), BmsErrorType.TEST_CONTENT, e.getError().getType());
	}

	// loadContentFrom(Path, Long, boolean)
	// 固定化した乱数の値(1以上)でBMSファイルを読み込んだ時、期待する読み込み結果になること
	@Test
	public void testLoadContentFrom_RandomFixed() throws Exception {
		for (var i = 1; i <= 6; i++) {
			var r = (long)i;
			var s = Integer.toString(i);
			var c = BeMusic.loadContentFrom(testDataPath(), r, true);
			assertEquals(s, BeMusicMeta.getArtist(c));
			assertEquals(s, BeMusicMeta.getStageFile(c));
			assertEquals(s, BeMusicMeta.getComment(c));
		}
	}

	// loadContentFrom(Path, Long, boolean)
	// 固定化した乱数の値(0以下)でBMSファイルを読み込んだ時、期待する読み込み結果(全て偽)になること
	@Test
	public void testLoadContentFrom_RandomFixedFalse() throws Exception {
		for (var i = 0; i >= -2; i--) {
			var c = BeMusic.loadContentFrom(testDataPath(), (long)i, true);
			assertEquals("", BeMusicMeta.getArtist(c));
			assertEquals("", BeMusicMeta.getStageFile(c));
			assertEquals("", BeMusicMeta.getComment(c));
		}
	}

	// loadContentFrom(Path, Long, boolean)
	// 乱数の値を固定化しない(null指定)でBMSファイルを読み込んだ時、期待する読み込み結果になること
	@Test
	public void testLoadContentFrom_RandomNull() throws Exception {
		for (var i = 0; i < 5; i++) {
			// 乱数を使用するので、最大で複数回チェックする
			var c = BeMusic.loadContentFrom(testDataPath(), null, true);
			var s1 = BeMusicMeta.getArtist(c);
			var s2 = BeMusicMeta.getStageFile(c);
			var s3 = BeMusicMeta.getComment(c);
			var same = (s1.equals(s2) && s1.equals(s3));
			if (!same) { return; } // 異なる値になっていれば、乱数が効いていると判断する
		}
		fail("Always all metas loaded as same value. Are random function really valid ??");
	}

	// loadContentFrom(Path, Long, boolean)
	// NullPointerException pathがnull
	@Test
	public void testLoadContentFrom_NullPath() throws Exception {
		assertThrows(NullPointerException.class, () -> BeMusic.loadContentFrom(null, null, true));
	}

	// loadContentFrom(Path, Long, boolean)
	// IOException pathに指定されたファイルなし
	@Test
	public void testLoadContentFrom_FileNotFound() throws Exception {
		assertThrows(IOException.class, () -> BeMusic.loadContentFrom(testDataPath(), null, true));
	}
}
