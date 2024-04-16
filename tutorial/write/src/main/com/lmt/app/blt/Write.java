package com.lmt.app.blt;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import com.lmt.lib.bms.BmsContent;
import com.lmt.lib.bms.BmsLoader;
import com.lmt.lib.bms.BmsStandardSaver;
import com.lmt.lib.bms.bemusic.BeMusic;
import com.lmt.lib.bms.bemusic.BeMusicChannel;
import com.lmt.lib.bms.bemusic.BeMusicChart;
import com.lmt.lib.bms.bemusic.BeMusicChartBuilder;
import com.lmt.lib.bms.bemusic.BeMusicLoadHandler;
import com.lmt.lib.bms.bemusic.BeMusicMeta;
import com.lmt.lib.bms.bemusic.BeMusicSpec;

public class Write {
	public static void main(String[] args) throws Exception {
		// このチュートリアルでは入力のBMSファイルパス、出力先ファイルパスの2つのパラメータが必要になります。
		// 入力パラメータが不足している場合は正しく動作させられないので直ちにプログラムを終了します。
		if (args.length < 2) {
			System.out.println(Write.class.getName() + ": Need the following args.");
			System.out.println("arg1: Input BMS file path");
			System.out.println("arg2: Output file path");
			return;
		}

		Path inputBmsPath = Path.of(args[0]);
		Path outputBmsPath = Path.of(args[1]);

		// このチュートリアルでのBMSファイルの読み込みではBMSローダの状態を確認したり、動作設定を変更したりします。
		// そのためBMSコンテンツの簡易読み込みであるBeMusic#loadContentFrom()は使用しません。
		BmsLoader loader = BeMusic.createLoaderFor(inputBmsPath);
		if (!loader.isStandard()) {
			// BMS標準フォーマットではない(つまりbmson形式)場合、出力処理に対応していないので処理を終了します。
			// ※bmsonの出力処理は将来的に対応する可能性があります
			System.out.println("bmson output is not supported.");
			return;
		}

		// BMSローダに以下の動作設定を行ったうえで入力のBMSファイルパスからBMSコンテンツを読み込みます。
		// 1.Be-Musicの最新仕様をBMSローダに設定します。
		// 2.Be-Music用読み込みハンドラで#RANDOMによる乱数生成を有効化し、それをBMSローダに設定します。
		// 3.BMSファイル読み込み時の優先文字コードを UTF-8→MS932(Shift-JIS) に設定します。
		// BMSコンテンツ読み込み後、ファイルの文字コードとBOM(Byte Order Mark)の有無を控えておきます。
		// 上記は、後の処理で編集したBMSファイルを出力する際に使用します。
		// また、譜面情報は後続のタイムライン編集で使用するので予め生成しておきます。
		BmsContent content = loader
				.setSpec(BeMusicSpec.LATEST)
				.setHandler(new BeMusicLoadHandler().setEnableControlFlow(true))
				.setCharsets(StandardCharsets.UTF_8, Charset.forName("MS932"))
				.load(inputBmsPath);
		Charset charset = loader.getLastProcessedCharset();
		boolean hasBom = loader.getLastProcessedHasBom();
		BeMusicChart chart = BeMusicChartBuilder.createChart(content);

		// BMSコンテンツの更新が完了するまでBMSコンテンツを編集モードに変更します。
		content.beginEdit();

		// BMSコンテンツにBMS宣言を追加します。
		// BMS宣言は特定のアプリケーションに動作指示を与えたい場合などに使用することを想定しています。
		// これはBMS Library特有の仕様のため、Be-Music関連のアプリケーションで使用されることはありませんが、
		// このチュートリアルではデータ編集を学習することを目的としているので書き出しを行うものとします。
		content.putDeclaration("brand", "edited-by-bms-library");

		// #COMMENTにデータ編集を行った日時を書き出します。
		// ただし、#COMMENTには既に何らかの値が設定されている可能性があるので、値が未定義であれば書き出すものとします。
		// #COMMENTが未定義だと空文字列(デフォルト値)が返されるので、空文字であるかどうかで定義有無を判定します。
		String orgComment = BeMusicMeta.getComment(content);
		if (orgComment.isEmpty()) {
			BeMusicMeta.setComment(content, LocalDateTime.now().toString());
		}

		// タイムラインを編集します。
		// このチュートリアルでは小節ごとのノート数を#TEXTxxに書き出し、それを各小節の先頭で表示する設定を追加します。
		// ノート数の計算はBeMusicChartから得られる情報を使用すると簡単なので、予め生成しておいたものを利用します。
		int measureCount = content.getMeasureCount();
		for (int measure = 0; measure < measureCount; measure++) {
			// この処理では小節の先頭から次の小節の先頭までを走査し、処理対象小節のノート数合計値を算出しています。
			// BeMusicPointはその楽曲位置でのノート数を保持していますので、それをノート数の計算に利用しています。
			int m = measure;
			int start = chart.ceilPointOf(m + 0, 0.0);
			int end = chart.floorPointOf(m + 1, 0.0) + 1;
			int notes = 0;
			if ((start >= 0) && (end >= 0)) {
				notes = chart.points(start, end).filter(p -> p.getMeasure() == m).mapToInt(p -> p.getNoteCount()).sum();
			}

			// 小節のノート数からテキストを生成し、#TEXTxxへ設定し、TEXTチャンネルにxxの値を書き出します。
			// Be-Musicサブセットにはタイムラインへの書き出しを行うAPIがないので、BmsContentの機能を利用します。
			int textIndex = measure + 1;
			String textValue = "Notes=" + notes;
			BeMusicMeta.setText(content, textIndex, textValue);
			content.putNote(BeMusicChannel.TEXT.getNumber(), measure, 0.0, textIndex);
		}

		// 全ての編集が終了したので編集モードを終了します。
		content.endEdit();

		// 編集したBMSコンテンツをBMS標準形式で書き出します。
		// 読み込み時の文字コードとBOMの有無を設定し、元の文字コードから変化しない形でテキストをエンコードします。
		// また、コメントは一般的なBMS編集ソフトと同等になるよう調整して出力します。
		// どのような編集が行われたか、WinMergeなどの比較ツールで確認してみてください。
		new BmsStandardSaver()
				.setCharsets(charset)
				.setAddBom(hasBom)
				.setMetaComments(List.of("", "*---------------------- HEADER FIELD", ""))
				.setChannelComments(List.of("", "*---------------------- MAIN DATA FIELD", ""))
				.save(content, outputBmsPath);
		System.out.println("Completed.");
	}
}
