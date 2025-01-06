package com.lmt.lib.bms.bemusic;

import static org.junit.Assert.*;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class DeltaSystemTest {
	static final Path DATA_PATH = Path.of(
			System.getProperty("user.dir"), "test", "com", "lmt", "lib", "bms", "bemusic", "testdata", "deltasystem");
	static final String SP = "sp";
	static final String DP = "dp";

	static DeltaSystemTestPackage sPackageSp = null;
	static DeltaSystemTestPackage sPackageDp = null;

	@BeforeClass
	public static void setupClass() throws Exception {
		sPackageSp = loadPackage(SP);
		sPackageDp = loadPackage(DP);
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
		if (sPackageDp != null) { sPackageDp.close(); }
		if (sPackageSp != null) { sPackageSp.close(); }
	}

	// Delta Systemによるレーティング値算出の値が期待通りであることを専用テストデータで確認(SP)
	@Test
	public void testVerifySp() throws Exception {
		testVerify(SP, sPackageSp);
	}

	// Delta Systemによるレーティング値算出の値が期待通りであることを専用テストデータで確認(DP)
	@Test
	public void testVerifyDp() throws Exception {
		testVerify(DP, sPackageDp);
	}

	// Delta Systemによるレーティング値算出が期待通りの値であること
	// このテストは、全てのサンプルデータでレーティング値が正しく出力されることを検証する
	private void testVerify(String playMode, DeltaSystemTestPackage testPackage) throws Exception {
		// 全BMSファイルの全レーティング値を算出し、期待値と比較する
		var allTypes = BeMusicRatingType.values();
		var fails = new ArrayList<DeltaSystemTestExpectItem>();
		var ids = DeltaSystemTestExpect.ids(playMode);
		for (var id : ids) {
			var data = testPackage.load(id, true);
			var stat = new BeMusicStatisticsBuilder(data.header, data.chart)
					.setNoteLayout(BeMusicNoteLayout.DP_REGULAR)
					.addRating(allTypes)
					.statistics();
			var actual = new DeltaSystemTestExpectItem(data.header, stat);
			var expect = getExpected(playMode, actual.id);
			for (var rt : allTypes) {
				if (actual.ratings[rt.getIndex()] != expect.ratings[rt.getIndex()]) {
					// 1個でも異なるレーティング値があればテスト失敗とする
					fails.add(actual);
					break;
				}
			}
		}

		// 1件でもテスト失敗のBMSファイルがあれば差分内容を標準出力してテスト失敗とする
		if (!fails.isEmpty()) {
			// ヘッダ部を出力する
			var p = System.out;
			var h1 = Stream.of(allTypes).map(rt -> rt.name()).collect(Collectors.joining("\t\t\t"));
			var h2 = IntStream.range(0, 7).mapToObj(i -> "\tActual\tExpect\tDelta").collect(Collectors.joining(""));
			p.println("Report of Delta System test");
			p.printf("ID\tTitle\t%s\n", h1);
			p.printf("\t%s\n", h2);

			// レーティング値に差異のあったファイルの情報を全て出力する
			var expects = DeltaSystemTestExpect.get(playMode);
			for (var actual : fails) {
				var expect = expects.get(actual.id);
				p.printf("%s\t%s", actual.id, actual.title);
				for (var rt : allTypes) {
					var ratingActual = actual.ratings[rt.getIndex()];
					var ratingExpect = expect.ratings[rt.getIndex()];
					var ratingDelta = ratingActual - ratingExpect;
					p.printf("\t%.2f\t%.2f\t%.2f", ratingActual, ratingExpect, ratingDelta);
				}
				p.println();
			}

			// テスト失敗を報告する
			var msg = String.format("%s: %d/%d files rating value is different than expected.",
					playMode.toUpperCase(), fails.size(), ids.size());
			fail(msg);
		}
	}

	// 譜面の余分な情報(BGA/BGM/不可視/テキスト)の有無によってレーティング値が変動しないこと
	@Test
	public void testOutOfScope_OnOff() throws Exception {
		// 小節をまたぐロングノートがあり、かつ余分な情報を持つ譜面を選んで検証する
		var data = sPackageSp.load("ln02-02", false); // 余分な情報をカットする
		var types = BeMusicRatingType.values();
		var stat = new BeMusicStatisticsBuilder(data.header, data.chart).addRating(types).statistics();
		var actual = new DeltaSystemTestExpectItem(data.header, stat);
		var expect = getExpected(SP, actual.id);
		for (var rt : BeMusicRatingType.values()) {
			var ratingActual = actual.ratings[rt.getIndex()];
			var ratingExpect = expect.ratings[rt.getIndex()];
			if (ratingActual != ratingExpect) {
				var msg = String.format("%s - %s: %s expected %s, but %s",
						actual.id, actual.title, rt, ratingExpect, ratingActual);
				fail(msg);
			}
		}
	}

	static DeltaSystemTestPackage loadPackage(String playMode) throws Exception {
		var packagePath = DATA_PATH.resolve(playMode + ".bin");
		var testPackage = new DeltaSystemTestPackage(packagePath);
		return testPackage;
	}

	private static DeltaSystemTestExpectItem getExpected(String playMode, String id) {
		var expect = DeltaSystemTestExpect.get(playMode).get(id);
		if (expect == null) {
			// 対象BMSファイルのIDが存在しない場合はテスト失敗とする
			fail(String.format("ID:%s No such test data.", id));
		}
		return expect;
	}
}
