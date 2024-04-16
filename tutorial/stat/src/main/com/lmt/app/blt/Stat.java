package com.lmt.app.blt;

import java.nio.file.Path;
import java.text.DecimalFormat;

import com.lmt.lib.bms.BmsContent;
import com.lmt.lib.bms.bemusic.BeMusic;
import com.lmt.lib.bms.bemusic.BeMusicChart;
import com.lmt.lib.bms.bemusic.BeMusicChartBuilder;
import com.lmt.lib.bms.bemusic.BeMusicHeader;
import com.lmt.lib.bms.bemusic.BeMusicNoteLayout;
import com.lmt.lib.bms.bemusic.BeMusicRatingType;
import com.lmt.lib.bms.bemusic.BeMusicRatings;
import com.lmt.lib.bms.bemusic.BeMusicStatistics;
import com.lmt.lib.bms.bemusic.BeMusicStatisticsBuilder;

public class Stat {
	public static void main(String[] args) throws Exception {
		// 配置(譜面レイアウト)が指定されていれば、その配置での分析を行うようにします。
		// 2番目のパラメータに配置内容を指定します。配置の指定方法はBeMusicNoteLayoutのリファレンスを参照してください。
		// ダブルプレー譜面の場合、3番目のパラメータに右レーンの配置を記述してください。
		BeMusicNoteLayout layout = BeMusicNoteLayout.SP_REGULAR;
		if (args.length >= 3) {
			layout  = new BeMusicNoteLayout(args[1], args[2]);
		} else if (args.length >= 2) {
			layout = new BeMusicNoteLayout(args[1]);
		}

		// 指定されたBMSファイルを読み込みます。
		Path inputBmsPath = Path.of(args[0]);
		BmsContent content = BeMusic.loadContentFrom(inputBmsPath, null, false);

		// 読み込んだBMSコンテンツの分析を行います。
		// 分析にはヘッダ情報と譜面情報が必要になるのでそれぞれ生成し、BeMusicStatisticsBuilderの入力データとします。
		// このチュートリアルではDelta Systemによるレーティングも行いますので譜面傾向のレーティング種別を追加します。
		BeMusicHeader header = BeMusicHeader.of(content);
		BeMusicChart chart = BeMusicChartBuilder.createChart(content);
		BeMusicStatistics stat = new BeMusicStatisticsBuilder(header, chart)
				.setNoteLayout(layout)
				.addRating(BeMusicRatingType.tendencies())
				.statistics();

		// 一般的な譜面統計情報を出力します。
		// BeMusicStatisticsには譜面の総合的な統計情報が格納されているので、それらを出力します。
		// 期間統計情報を出力すると冗長になるので本チュートリアルでは取り扱いません。
		DecimalFormat fmtDensity = new DecimalFormat("#.00");
		DecimalFormat fmtPercent = new DecimalFormat("#.00");
		System.out.println("Title: " + header.getTitle() + " " + header.getSubTitle());
		System.out.println("----- Statistics (Generic) -----");
		System.out.println("Layout     : " + stat.getNoteLayout());
		System.out.println("Avg density: " + fmtDensity.format(stat.getAverageDensity()) + " Notes/s");
		System.out.println("Max density: " + fmtDensity.format(stat.getMaxDensity()) + " Notes/s");
		System.out.println("No playing : " + fmtPercent.format(stat.getNoPlayingRatio() * 100.0) + "%");
		System.out.println("No visual  : " + fmtPercent.format(stat.getNoVisualEffectRatio() * 100.0) + "%");
		System.out.println("Gaze point : L=" + stat.getAverageGazePoint() + ", R=" + stat.getAverageGazePointR());
		System.out.println("Gaze swing : L=" + stat.getAverageGazeSwingley() + ", R=" + stat.getAverageGazeSwingleyR());
		System.out.println("View width : L=" + stat.getAverageViewWidth() + ", R=" + stat.getAverageViewWidthR());
		System.out.println("View swing : L=" + stat.getAverageViewSwingley() + ", R=" + stat.getAverageViewSwingleyR());

		// Delta Systemのレーティング値を出力します。
		// BeMusicStatisticsBuilderに追加したレーティング種別の分析結果が格納されているのでそれぞれゲージ表示します。
		// ※レーダーチャートで表示することを推奨しますが、テキストベースで表現困難なためゲージ表示とします
		System.out.println("----- Statistics (Delta System) -----");
		System.out.println("Version: " + BeMusicStatistics.getDeltaSystemVersion());
		BeMusicRatingType.tendencies().forEach(t -> printTendency(stat, t));
	}

	private static void printTendency(BeMusicStatistics stat, BeMusicRatingType rt) {
		int rating = stat.getRating(rt);
		boolean primary = (rt == stat.getPrimaryTendency());
		boolean secondary = (rt == stat.getSecondaryTendency());
		String name = rt.name();
		String spc1 = " ".repeat(9 - name.length());
		String mark = (primary ? "**" : (secondary ? "* " : "  "));
		String gauge = "=".repeat((int)((rating / (double)rt.getMax()) * 50.0));
		String spc2 = " ".repeat(50 - gauge.length());
		String value = BeMusicRatings.tendencyAsString(rating, true);
		System.out.printf("%s%s%s [%s%s] %s\n", name, spc1, mark, gauge, spc2, value);
	}
}
