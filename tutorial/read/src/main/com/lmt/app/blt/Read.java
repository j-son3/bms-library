package com.lmt.app.blt;

import java.nio.file.Path;

import com.lmt.lib.bms.BmsContent;
import com.lmt.lib.bms.bemusic.BeMusic;
import com.lmt.lib.bms.bemusic.BeMusicChart;
import com.lmt.lib.bms.bemusic.BeMusicChartBuilder;
import com.lmt.lib.bms.bemusic.BeMusicHeader;

public class Read {
	public static void main(String[] args) throws Exception {
		// 読み込むBMSファイルのパスは起動時に指定された最初のパラメータから取り出します。
		// パラメータが未指定だったり、不正なパスを指定するとこの時点で例外がスローされプログラムが終了します。
		Path bmsPath = Path.of(args[0]);

		// 指定されたBMSファイルを読み込みます。
		// 読み込みは最も一般的な設定で行います。(#RANDOM有効、各エラー無視)
		// 読み込み可能な形式は .bms, .bme, .bml, .bmson のいずれかです。
		// bmsonの読み込みでは、JSONの構文にエラーがあったり非対応のモードが指定されていると例外がスローされます。
		BmsContent content = BeMusic.loadContentFrom(bmsPath, null, false);

		// 読み込まれたBMSコンテンツから、Be-Musicのヘッダ情報を読み込んで出力します。
		// BMSライブラリが標準で対応するヘッダ情報についてはBeMusicHeaderのリファレンスを参照してください。
		// ※標準で対応していない情報はBmsContentから直接取り出せますがプログラムは冗長になります
		// ※bmsonから読み込まれた情報は標準BMS形式のヘッダ情報に変換されて格納されています
		BeMusicHeader header = BeMusicHeader.of(content);
		String playMode = header.getPlayer().isSinglePlay() ? "SP" : "DP";
		System.out.println("----- HEADER INFORMATION -----");
		System.out.println("Genre       : " + header.getGenre());
		System.out.println("Title       : " + header.getTitle());
		System.out.println("Sub title   : " + header.getSubTitle());
		System.out.println("Artist      : " + header.getArtist());
		System.out.println("Sub artist  : " + header.getSubArtist(" / "));
		System.out.println("Chart status: " + playMode + " " + header.getDifficulty() + " " + header.getPlayLevelRaw());
		System.out.println("BPM         : " + header.getInitialBpm());
		System.out.println("Total       : " + header.getTotal());
		System.out.println("Rank        : " + header.getRank());
		System.out.println("Stage file  : " + header.getStageFile());
		System.out.println("Banner      : " + header.getBanner());
		System.out.println("Backbmp     : " + header.getBackBmp());
		System.out.println("Preview     : " + header.getPreview());
		System.out.println("Comment     : " + header.getComment());

		// 読み込まれたBMSコンテンツから譜面情報を生成し、一部の情報を出力します。
		// 譜面情報には、譜面の簡易統計情報とタイムラインの情報が含まれますがこのチュートリアルでは前者のみ出力します。
		// タイムラインの情報の扱い方については他のチュートリアルを参照してください。
		// ※一部の情報は便宜上ヘッダ情報を表示します
		BeMusicChart chart = BeMusicChartBuilder.createChart(content);
		System.out.println("----- CHART INFORMATION -----");
		System.out.println("Total time   : " + chart.getPlayTime() + "secs");
		System.out.println("Total notes  : " + chart.getNoteCount());
		System.out.println("Long notes   : " + chart.getLongNoteCount());
		System.out.println("Mine count   : " + chart.getMineCount());
		System.out.println("Change BPM   : " + chart.getChangeBpmCount());
		System.out.println("Change scroll: " + chart.getChangeScrollCount());
		System.out.println("Stop count   : " + chart.getStopCount());
		System.out.println("LN mode      : " + header.getLnMode());
		System.out.println("Scratch mode : " + chart.getScratchMode());
	}
}
