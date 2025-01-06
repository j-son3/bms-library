package com.lmt.lib.bms.bemusic;

import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.lmt.lib.bms.BmsChannel;
import com.lmt.lib.bms.BmsErrorType;
import com.lmt.lib.bms.BmsLoadException;
import com.lmt.lib.bms.BmsMeta;
import com.lmt.lib.bms.BmsSpec;
import com.lmt.lib.bms.BmsType;
import com.lmt.lib.bms.BmsUnit;
import com.lmt.lib.bms.UseTestData;
import com.lmt.lib.bms.internal.Utility;

public class BeMusicSpecTest implements UseTestData {
	private static final String HASH_V1 = "567abbeec2357f65cfa5ef7a61489d2d2f8767cd3c17f13c6d205cdc143a1878";

	@Override
	public Path testDataDirectory() {
		return Path.of("test", "com", "lmt", "lib", "bms", "bemusic", "testdata", "bemusicspec");
	}

	@Override
	public String testDataExtension() {
		return "bms";
	}

	// create(int, BmsMeta[], BmsChannel[])
	// V1指定でBMS仕様V1が生成されること
	@Test
	public void testCreate_001() throws Exception {
		var s = BeMusicSpec.create(BeMusicSpec.V1, null, null);
		//System.out.println(Utility.byteArrayToString(s.generateHash()));
		assertEquals(HASH_V1, Utility.byteArrayToString(s.generateHash()));
	}

	// create(int, BmsMeta[], BmsChannel[])
	// objectMetasにnullを指定しても生成可能であること
	@Test
	public void testCreate_002() {
		var uch = new BmsChannel[] { new BmsChannel(BmsSpec.USER_CHANNEL_MIN, BmsType.OBJECT, null, null, false, false) };
		BeMusicSpec.create(BeMusicSpec.V1, null, uch);
	}

	// create(int, BmsMeta[], BmsChannel[])
	// userChannelsにnullを指定しても生成可能であること
	@Test
	public void testCreate_003() {
		var om = new BmsMeta[] { BmsMeta.object("#object", BmsUnit.SINGLE) };
		BeMusicSpec.create(BeMusicSpec.V1, om, null);
	}

	// create(int, BmsMeta[], BmsChannel[])
	// IllegalArgumentException specVersionに未知の値を指定した
	@Test
	public void testCreate_004() {
		assertThrows(IllegalArgumentException.class, () -> BeMusicSpec.create(-9999, null, null));
	}

	// create(int, BmsMeta[], BmsChannel[])
	// IllegalArgumentException objectMetasのリスト内に任意型以外のメタ情報が含まれていた
	@Test
	public void testCreate_006() {
		var om = new BmsMeta[] { BmsMeta.single("#not_object", BmsType.INTEGER, "0", 0, false) };
		assertThrows(IllegalArgumentException.class, () -> BeMusicSpec.create(BeMusicSpec.V1, om, null));
	}

	// create(int, BmsMeta[], BmsChannel[])
	// IllegalArgumentException userChannelsのリスト内に仕様チャンネルが含まれていた
	@Test
	public void testCreate_007() {
		var uch = new BmsChannel[] { new BmsChannel("ZZ", BmsType.ARRAY36, null, "", false, false) };
		assertThrows(IllegalArgumentException.class, () -> BeMusicSpec.create(BeMusicSpec.V1, null, uch));
	}

	// createV1(BmsMeta[], BmsChannel[])
	// BMS仕様V1の内容が正しいこと
	@Test
	public void testCreateV1_Normal() {
		var s = BeMusicSpec.createV1(null, null);
		assertEquals(HASH_V1, Utility.byteArrayToString(s.generateHash()));
	}

	// createV1(BmsMeta[], BmsChannel[])
	// 基数選択メタ情報が設定されていること
	@Test
	public void testCreateV1_BaseChanger() {
		var s = BeMusicSpec.createV1(null, null);
		assertTrue(s.hasBaseChanger());
		assertEquals(BeMusicMeta.BASE, s.getBaseChangerMeta());
	}

	// createLatest(Collection<BmsMeta>, Collection<BmsChannel>)
	// 任意型メタ情報、ユーザーチャンネルの両方を指定してBMS仕様を生成できること
	@Test
	public void testCreateLatest_Normal() {
		var m = List.of(
				BmsMeta.object("#object1", BmsUnit.SINGLE),
				BmsMeta.object("#object2", BmsUnit.MULTIPLE),
				BmsMeta.object("#object3", BmsUnit.INDEXED));
		var c = List.of(
				BmsChannel.object(BmsSpec.USER_CHANNEL_MIN + 1),
				BmsChannel.object(BmsSpec.USER_CHANNEL_MIN + 2));
		var s = BeMusicSpec.createLatest(m, c);
		assertNotNull(s.getSingleMeta("#object1"));
		assertNotNull(s.getMultipleMeta("#object2"));
		assertNotNull(s.getIndexedMeta("#object3"));
		assertNotNull(s.getChannel(BmsSpec.USER_CHANNEL_MIN + 1));
		assertNotNull(s.getChannel(BmsSpec.USER_CHANNEL_MIN + 2));
	}

	// createLatest(Collection<BmsMeta>, Collection<BmsChannel>)
	// 任意型メタ情報のみを指定してBMS仕様を生成できること
	@Test
	public void testCreateLatest_ObjectMetasOnly() {
		var m = List.of(
				BmsMeta.object("#object1", BmsUnit.SINGLE),
				BmsMeta.object("#object2", BmsUnit.MULTIPLE),
				BmsMeta.object("#object3", BmsUnit.INDEXED));
		var s = BeMusicSpec.createLatest(m, null);
		assertNotNull(s.getSingleMeta("#object1"));
		assertNotNull(s.getMultipleMeta("#object2"));
		assertNotNull(s.getIndexedMeta("#object3"));
		assertNull(s.getChannel(BmsSpec.USER_CHANNEL_MIN + 1));
		assertNull(s.getChannel(BmsSpec.USER_CHANNEL_MIN + 2));
	}

	// createLatest(Collection<BmsMeta>, Collection<BmsChannel>)
	// ユーザーチャンネルのみを指定してBMS仕様を生成できること
	@Test
	public void testCreateLatest_UserChannelsOnly() {
		var c = List.of(
				BmsChannel.object(BmsSpec.USER_CHANNEL_MIN + 1),
				BmsChannel.object(BmsSpec.USER_CHANNEL_MIN + 2));
		var s = BeMusicSpec.createLatest(null, c);
		assertNull(s.getSingleMeta("#object1"));
		assertNull(s.getMultipleMeta("#object2"));
		assertNull(s.getIndexedMeta("#object3"));
		assertNotNull(s.getChannel(BmsSpec.USER_CHANNEL_MIN + 1));
		assertNotNull(s.getChannel(BmsSpec.USER_CHANNEL_MIN + 2));
	}

	// createLatest(Collection<BmsMeta>, Collection<BmsChannel>)
	// 任意型メタ情報、ユーザーチャンネル両方を未指定でもBMS仕様を生成できること
	@Test
	public void testCreateLatest_AllNull() {
		var s = BeMusicSpec.createLatest(null, null);
		assertNull(s.getSingleMeta("#object1"));
		assertNull(s.getMultipleMeta("#object2"));
		assertNull(s.getIndexedMeta("#object3"));
		assertNull(s.getChannel(BmsSpec.USER_CHANNEL_MIN + 1));
		assertNull(s.getChannel(BmsSpec.USER_CHANNEL_MIN + 2));
	}

	// createLatest(Collection<BmsMeta>, Collection<BmsChannel>)
	// NullPointerException objectMetasのコレクション内にnullが含まれていた
	@Test
	public void testCreateLatest_HasNullInObjectMetas() {
		var m = new BmsMeta[] {
				BmsMeta.object("#object1", BmsUnit.SINGLE),
				BmsMeta.object("#object2", BmsUnit.MULTIPLE),
				null };
		assertThrows(NullPointerException.class, () -> BeMusicSpec.createLatest(Arrays.asList(m), null));
	}

	// createLatest(Collection<BmsMeta>, Collection<BmsChannel>)
	// NullPointerException userChannelsのコレクション内にnullが含まれていた
	@Test
	public void testCreateLatest_HasNullInUserChannels() {
		var c = new BmsChannel[] {
				BmsChannel.object(BmsSpec.USER_CHANNEL_MIN + 1),
				null };
		assertThrows(NullPointerException.class, () -> BeMusicSpec.createLatest(null, Arrays.asList(c)));
	}

	// createLatest(Collection<BmsMeta>, Collection<BmsChannel>)
	// IllegalArgumentException objectMetasのコレクション内に任意型以外のメタ情報が含まれていた
	@Test
	public void testCreateLatest_HasNonObjectMeta() {
		var m = List.of(
				BmsMeta.object("#object1", BmsUnit.SINGLE),
				BmsMeta.object("#object2", BmsUnit.MULTIPLE),
				BmsMeta.single("#single", BmsType.INTEGER, "0", 0, false));
		assertThrows(IllegalArgumentException.class, () -> BeMusicSpec.createLatest(m, null));
	}

	// createLatest(Collection<BmsMeta>, Collection<BmsChannel>)
	// IllegalArgumentException userChannelsのコレクション内に仕様チャンネルが含まれていた
	@Test
	public void testCreateLatest_HasNonUserChannel() {
		var c = List.of(
				BmsChannel.object(BmsSpec.USER_CHANNEL_MIN + 1),
				BmsChannel.spec(BmsSpec.SPEC_CHANNEL_MIN, BmsType.INTEGER, null, "0", false, false));
		assertThrows(IllegalArgumentException.class, () -> BeMusicSpec.createLatest(null, c));
	}

	// loadContentFrom(Path, boolean)
	// エラーのない一般的なBMSファイルが読み込めること(シングルプレー)
	@Test
	@SuppressWarnings("deprecation")
	public void testLoadContentFromPathBoolean_CorrectlySp() throws Exception {
		var c = BeMusicSpec.loadContentFrom(testDataPath(), true);
		assertNotNull(c);
	}

	// loadContentFrom(Path, boolean)
	// エラーのない一般的なBMSファイルが読み込めること(ダブルプレー)
	@Test
	@SuppressWarnings("deprecation")
	public void testLoadContentFromPathBoolean_CorrectlyDp() throws Exception {
		var c = BeMusicSpec.loadContentFrom(testDataPath(), true);
		assertNotNull(c);
	}

	// loadContentFrom(Path, boolean)
	// 寛容読み込みで、構文エラーのあるBMSファイルが読み込めること
	@Test
	@SuppressWarnings("deprecation")
	public void testLoadContentFromPathBoolean_TolerantlySyntaxError() throws Exception {
		var c = BeMusicSpec.loadContentFrom(testDataPath(), false);
		assertNotNull(c);
	}

	// loadContentFrom(Path, boolean)
	// 寛容読み込みで、仕様違反の値があるBMSファイルが読み込めること
	@Test
	@SuppressWarnings("deprecation")
	public void testLoadContentFromPathBoolean_TolerantlySpecViolation() throws Exception {
		var c = BeMusicSpec.loadContentFrom(testDataPath(), false);
		assertNotNull(c);
	}

	// loadContentFrom(Path, boolean)
	// 寛容読み込みで、未知のメタ情報が定義されたBMSファイルが読み込めること
	@Test
	@SuppressWarnings("deprecation")
	public void testLoadContentFromPathBoolean_TolerantlyUnknownMeta() throws Exception {
		var c = BeMusicSpec.loadContentFrom(testDataPath(), false);
		assertNotNull(c);
	}

	// loadContentFrom(Path, boolean)
	// 寛容読み込みで、未知のチャンネルが定義されたBMSファイルが読み込めること
	@Test
	@SuppressWarnings("deprecation")
	public void testLoadContentFromPathBoolean_TolerantlyUnknownChannel() throws Exception {
		var c = BeMusicSpec.loadContentFrom(testDataPath(), false);
		assertNotNull(c);
	}

	// loadContentFrom(Path, boolean)
	// 寛容読み込みで、メタ情報に不正な値を設定したBMSファイルが読み込めること
	@Test
	@SuppressWarnings("deprecation")
	public void testLoadContentFromPathBoolean_TolerantlyWrongMeta() throws Exception {
		var c = BeMusicSpec.loadContentFrom(testDataPath(), false);
		assertNotNull(c);
	}

	// loadContentFrom(Path, boolean)
	// 寛容読み込みで、チャンネルに不正な値を設定したBMSファイルが読み込めること
	@Test
	@SuppressWarnings("deprecation")
	public void testLoadContentFromPathBoolean_TolerantlyWrongChannel() throws Exception {
		var c = BeMusicSpec.loadContentFrom(testDataPath(), false);
		assertNotNull(c);
	}

	// loadContentFrom(Path, boolean)
	// 寛容読み込みで、CONTROL FLOWの制御が不正なBMSファイルが読み込めること
	@Test
	@SuppressWarnings("deprecation")
	public void testLoadContentFromPathBoolean_TolerantlyControlFlow() throws Exception {
		var c = BeMusicSpec.loadContentFrom(testDataPath(), false);
		assertNotNull(c);
	}

	// loadContentFrom(Path, boolean)
	// 厳格読み込みで、構文エラーのあるBMSファイルが読み込めないこと
	@Test
	@SuppressWarnings("deprecation")
	public void testLoadContentFromPathBoolean_StrictlySyntaxError() throws Exception {
		var f = testDataPath();
		var e = assertThrows(BmsLoadException.class, () -> BeMusicSpec.loadContentFrom(f, true));
		assertEquals(e.getError().toString(), BmsErrorType.SYNTAX, e.getError().getType());
	}

	// loadContentFrom(Path, boolean)
	// 厳格読み込みで、仕様違反の値があるBMSファイルが読み込めないこと
	@Test
	@SuppressWarnings("deprecation")
	public void testLoadContentFromPathBoolean_StrictlySpecViolation() throws Exception {
		var f = testDataPath();
		var e = assertThrows(BmsLoadException.class, () -> BeMusicSpec.loadContentFrom(f, true));
		assertEquals(e.getError().toString(), BmsErrorType.SPEC_VIOLATION, e.getError().getType());
	}

	// loadContentFrom(Path, boolean)
	// 厳格読み込みで、未知のメタ情報が定義されたBMSファイルが読み込めないこと
	@Test
	@SuppressWarnings("deprecation")
	public void testLoadContentFromPathBoolean_StrictlyUnknownMeta() throws Exception {
		var f = testDataPath();
		var e = assertThrows(BmsLoadException.class, () -> BeMusicSpec.loadContentFrom(f, true));
		assertEquals(e.getError().toString(), BmsErrorType.UNKNOWN_META, e.getError().getType());
	}

	// loadContentFrom(Path, boolean)
	// 厳格読み込みで、未知のチャンネルが定義されたBMSファイルが読み込めないこと
	@Test
	@SuppressWarnings("deprecation")
	public void testLoadContentFromPathBoolean_StrictlyUnknownChannel() throws Exception {
		var f = testDataPath();
		var e = assertThrows(BmsLoadException.class, () -> BeMusicSpec.loadContentFrom(f, true));
		assertEquals(e.getError().toString(), BmsErrorType.UNKNOWN_CHANNEL, e.getError().getType());
	}

	// loadContentFrom(Path, boolean)
	// 厳格読み込みで、メタ情報に不正な値を設定したBMSファイルが読み込めないこと
	@Test
	@SuppressWarnings("deprecation")
	public void testLoadContentFromPathBoolean_StrictlyWrongMeta() throws Exception {
		var f = testDataPath();
		var e = assertThrows(BmsLoadException.class, () -> BeMusicSpec.loadContentFrom(f, true));
		assertEquals(e.getError().toString(), BmsErrorType.WRONG_DATA, e.getError().getType());
	}

	// loadContentFrom(Path, boolean)
	// 厳格読み込みで、チャンネルに不正な値を設定したBMSファイルが読み込めないこと
	@Test
	@SuppressWarnings("deprecation")
	public void testLoadContentFromPathBoolean_StrictlyWrongChannel() throws Exception {
		var f = testDataPath();
		var e = assertThrows(BmsLoadException.class, () -> BeMusicSpec.loadContentFrom(f, true));
		assertEquals(e.getError().toString(), BmsErrorType.WRONG_DATA, e.getError().getType());
	}

	// loadContentFrom(Path, boolean)
	// 厳格読み込みで、CONTROL FLOWの制御が不正なBMSファイルが読み込めないこと
	@Test
	@SuppressWarnings("deprecation")
	public void testLoadContentFromPathBoolean_StrictlyControlFlow() throws Exception {
		var f = testDataPath();
		var e = assertThrows(BmsLoadException.class, () -> BeMusicSpec.loadContentFrom(f, true));
		assertEquals(e.getError().toString(), BmsErrorType.TEST_CONTENT, e.getError().getType());
	}

	// loadContentFrom(Path, boolean)
	// NullPointerException pathがnull
	@Test
	@SuppressWarnings("deprecation")
	public void testLoadContentFromPathBoolean_NullPath() throws Exception {
		assertThrows(NullPointerException.class, () -> BeMusicSpec.loadContentFrom(null, true));
	}

	// loadContentFrom(Path, boolean)
	// IOException pathに指定されたファイルなし
	@Test
	@SuppressWarnings("deprecation")
	public void testLoadContentFromPathBoolean_FileNotFound() throws Exception {
		assertThrows(IOException.class, () -> BeMusicSpec.loadContentFrom(testDataPath(), true));
	}
}
