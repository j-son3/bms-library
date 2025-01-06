package com.lmt.lib.bms.bemusic;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.BeforeClass;
import org.junit.Test;

import com.lmt.lib.bms.BmsStandardLoader;
import com.lmt.lib.bms.internal.deltasystem.Ds;
import com.lmt.lib.bms.internal.deltasystem.DsDebugMode;

/**
 * Delta System デバッグダンプ用テストコード
 *
 * <p>当クラスで実装されたテストコードは公式の単機能試験で有用なテストを行わない。
 * 当クラスはライブラリ開発者向けのデバッグコードであり、ライブラリ利用者が参照する必要はない。</p>
 *
 * <p>デバッグダンプを有効にするには「dump.properties.org」ファイルを「dump.properties」にコピーし、
 * ダンプパラメータを指定した後で、実行したい1個のテストメソッドを選択すること。
 * 尚、「enable」パラメータ以外は全て「testDumpSpecifiedFile()」用のパラメータとなっている。</p>
 */
public class DeltaSystemDumpTest {
	private static boolean sEnable = false;
	private static Path sPath = Path.of("");
	private static BeMusicRatingType[] sRatings = {};
	private static DsDebugMode sFormat = DsDebugMode.NONE;
	private static BeMusicNoteLayout sLayout = BeMusicNoteLayout.SP_REGULAR;

	@BeforeClass
	public static void setupClass() {
		// テスト用パラメータファイルが存在しない場合はデフォルトの動作とする(当クラスのテストケースは全てPass)
		var paramsPath = DeltaSystemTest.DATA_PATH.resolve("dump.properties");
		if (!Files.isRegularFile(paramsPath)) {
			return;
		}

		// テスト用パラメータを読み込む
		var params = new Properties();
		try (var reader = Files.newBufferedReader(paramsPath, StandardCharsets.UTF_8)) {
			params.load(reader);
		} catch (IOException e) {
			// Do nothing
		}

		// テスト用パラメータを設定する
		// ※ここの読み込みでエラーになった場合はテストを続行しない
		sEnable = Boolean.valueOf(params.getProperty("enable", "false"));
		sPath = Path.of(params.getProperty("path", ""));
		sRatings = Stream.of(params.getProperty("ratings", "").split(","))
				.map(BeMusicRatingType::valueOf)
				.collect(Collectors.toList())
				.toArray(BeMusicRatingType[]::new);
		sFormat = DsDebugMode.valueOf(params.getProperty("format", "NONE"));
		var layouts = params.getProperty("layout", "1234567").split(",", 2);
		if (layouts.length == 1) {
			sLayout = new BeMusicNoteLayout(layouts[0]);
		} else {
			sLayout = new BeMusicNoteLayout(layouts[0], layouts[1]);
		}
	}

	// ダンプパラメータで指定した内容で分析を実施、データダンプを行う
	@Test
	public void testDumpSpecifiedFile() throws Exception {
		if (sEnable) {
			var content = new BmsStandardLoader()
					.setSpec(BeMusicSpec.LATEST)
					.setHandler(BeMusicLoadHandler.withControlFlow(1L))
					.setSyntaxErrorEnable(false)
					.setFixSpecViolation(true)
					.setAllowRedefine(true)
					.setIgnoreUnknownMeta(true)
					.setIgnoreUnknownChannel(true)
					.setIgnoreWrongData(true)
					.setCharsets(StandardCharsets.UTF_8, Charset.forName("MS932"))
					.load(sPath);
			var header = BeMusicHeader.of(content);
			var chart = new BeMusicChartBuilder(content)
					.setSeekBga(false)
					.setSeekBgm(false)
					.setSeekInvisible(false)
					.setSeekMine(true)
					.setSeekMeasureLine(true)
					.setSeekText(false)
					.setSeekVisible(true)
					.createChart();
			var ratings = Stream.of(sRatings).map(r -> r.toString()).collect(Collectors.joining(","));
			Ds.setDebugHandler(System.out::println);
			Ds.setDebugMode(sFormat);
			System.out.println(sPath);
			System.out.printf("TITLE: %s %s\n", header.getTitle(), header.getSubTitle());
			System.out.printf("LAYOUT: %s\n", sLayout);
			System.out.printf("RATINGS: %s\n", ratings);
			new BeMusicStatisticsBuilder(header, chart)
					.setNoteLayout(sLayout)
					.addRating(sRatings)
					.statistics();
		}
	}

	// DELTAのサマリ情報を出力する(シングルプレー)
	@Test
	public void testDumpSpDeltaSummary() throws Exception {
		dumpSummary(DeltaSystemTest.SP, BeMusicRatingType.DELTA);
	}

	// COMPLEXのサマリ情報を出力する(シングルプレー)
	@Test
	public void testDumpSpComplexSummary() throws Exception {
		dumpSummary(DeltaSystemTest.SP, BeMusicRatingType.COMPLEX);
	}

	// POWERのサマリ情報を出力する(シングルプレー)
	@Test
	public void testDumpSpPowerSummary() throws Exception {
		dumpSummary(DeltaSystemTest.SP, BeMusicRatingType.POWER);
	}

	// RHYTHMのサマリ情報を出力する(シングルプレー)
	@Test
	public void testDumpSpRhythmSummary() throws Exception {
		dumpSummary(DeltaSystemTest.SP, BeMusicRatingType.RHYTHM);
	}

	// SCRATCHのサマリ情報を出力する(シングルプレー)
	@Test
	public void testDumpSpScratchSummary() throws Exception {
		dumpSummary(DeltaSystemTest.SP, BeMusicRatingType.SCRATCH);
	}

	// HOLDINGのサマリ情報を出力する(シングルプレー)
	@Test
	public void testDumpSpHoldingSummary() throws Exception {
		dumpSummary(DeltaSystemTest.SP, BeMusicRatingType.HOLDING);
	}

	// GIMMICKのサマリ情報を出力する(シングルプレー)
	@Test
	public void testDumpSpGimmickSummary() throws Exception {
		dumpSummary(DeltaSystemTest.SP, BeMusicRatingType.GIMMICK);
	}

	// DELTAのサマリ情報を出力する(ダブルプレー)
	@Test
	public void testDumpDpDeltaSummary() throws Exception {
		dumpSummary(DeltaSystemTest.DP, BeMusicRatingType.DELTA);
	}

	// COMPLEXのサマリ情報を出力する(ダブルプレー)
	@Test
	public void testDumpDpComplexSummary() throws Exception {
		dumpSummary(DeltaSystemTest.DP, BeMusicRatingType.COMPLEX);
	}

	// POWERのサマリ情報を出力する(ダブルプレー)
	@Test
	public void testDumpDpPowerSummary() throws Exception {
		dumpSummary(DeltaSystemTest.DP, BeMusicRatingType.POWER);
	}

	// RHYTHMのサマリ情報を出力する(ダブルプレー)
	@Test
	public void testDumpDpRhythmSummary() throws Exception {
		dumpSummary(DeltaSystemTest.DP, BeMusicRatingType.RHYTHM);
	}

	// SCRATCHのサマリ情報を出力する(ダブルプレー)
	@Test
	public void testDumpDpScratchSummary() throws Exception {
		dumpSummary(DeltaSystemTest.DP, BeMusicRatingType.SCRATCH);
	}

	// HOLDINGのサマリ情報を出力する(ダブルプレー)
	@Test
	public void testDumpDpHoldingSummary() throws Exception {
		dumpSummary(DeltaSystemTest.DP, BeMusicRatingType.HOLDING);
	}

	// GIMMICKのサマリ情報を出力する(ダブルプレー)
	@Test
	public void testDumpDpGimmickSummary() throws Exception {
		dumpSummary(DeltaSystemTest.DP, BeMusicRatingType.GIMMICK);
	}

	private static void dumpSummary(String playMode, BeMusicRatingType ratingType) throws Exception {
		if (sEnable) {
			Ds.setDebugHandler(System.out::println);
			Ds.setDebugMode(DsDebugMode.SUMMARY);
			var dpMode = playMode.equals(DeltaSystemTest.DP);
			var layout = dpMode ? BeMusicNoteLayout.DP_REGULAR : BeMusicNoteLayout.SP_REGULAR;
			try (var testPackage = DeltaSystemTest.loadPackage(playMode)) {
				var ids = DeltaSystemTestExpect.ids(playMode);
				for (var id : ids) {
					var data = testPackage.load(id);
					new BeMusicStatisticsBuilder(data.header, data.chart)
							.setNoteLayout(layout)
							.addRating(ratingType)
							.statistics();
				}
			} finally {
				Ds.setDebugHandler(null);
				Ds.setDebugMode(DsDebugMode.NONE);
			}
		}
	}
}
