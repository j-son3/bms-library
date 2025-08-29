package com.lmt.app.blt;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.badlogic.gdx.backends.lwjgl3.audio.OpenALLwjgl3Audio;
import com.badlogic.gdx.backends.lwjgl3.audio.OpenALSound;
import com.badlogic.gdx.files.FileHandle;
import com.lmt.lib.bms.BmsContent;
import com.lmt.lib.bms.BmsInt;
import com.lmt.lib.bms.BmsLibrary;
import com.lmt.lib.bms.BmsLoader;
import com.lmt.lib.bms.bemusic.BeMusic;
import com.lmt.lib.bms.bemusic.BeMusicChart;
import com.lmt.lib.bms.bemusic.BeMusicChartBuilder;
import com.lmt.lib.bms.bemusic.BeMusicDevice;
import com.lmt.lib.bms.bemusic.BeMusicHeader;
import com.lmt.lib.bms.bemusic.BeMusicLane;
import com.lmt.lib.bms.bemusic.BeMusicLoadHandler;
import com.lmt.lib.bms.bemusic.BeMusicNoteType;
import com.lmt.lib.bms.bemusic.BeMusicPoint;
import com.lmt.lib.bms.bemusic.BeMusicSequencer;
import com.lmt.lib.bms.bemusic.BeMusicSound;
import com.lmt.lib.bms.bemusic.BeMusicSpec;

public class View {
	// サウンドのマスターボリューム(0～1)。
	// 最大音量にすると割と爆音になるので注意してください。
	private static final float MASTER_VOLUME = 0.25f;

	// サウンドの同時発音数。
	// この値を増加させると音の多い楽曲も正しく再生できる半面、音声再生時のCPU使用率が上昇します。
	// 減少しすぎると音が鳴らない場合があるので注意してください。
	private static final int POLYPHONY = 160;

	// 譜面・情報のヘッダ部分の文字列。
	private static final List<String> SP_HEADER = List.of(
			"---+---------------------------------+------------------------------------------",
			" M |              CHART              | INFORMATION",
			"---+---------------------------------+------------------------------------------");
	private static final List<String> DP_HEADER = List.of(
			"---+-------------------------+-------------------------+------------------------",
			" M |          LEFT           |          RIGHT          | INFORMATION",
			"---+-------------------------+-------------------------+------------------------");

	// 譜面のテキスト表現を表すマップです。
	// ノートの種別ごとにテキストの内容を定義しており、リストは左から順に以下の用途で利用されます。
	// [0] DP譜面のスイッチ
	// [1] SP譜面のスイッチ、DP譜面のスクラッチ
	// [2] SP譜面のスクラッチ
	private static final Map<BeMusicNoteType, List<String>> NOTES = Map.of(
			BeMusicNoteType.NONE, List.of("   ", "    ", "     "),
			BeMusicNoteType.BEAT, List.of("===", "====", "====="),
			BeMusicNoteType.LONG, List.of("| |", "|  |", "|   |"),
			BeMusicNoteType.LONG_ON, List.of("+=+", "+==+", "+===+"),
			BeMusicNoteType.LONG_OFF, List.of("+ +", "+  +", "+   +"),
			BeMusicNoteType.CHARGE_OFF, List.of("+-+", "+--+", "+---+"),
			BeMusicNoteType.MINE, List.of("***", "****", "*****"));

	// サウンドファイル形式ごとの拡張子検索順を表すマップです。
	// Be-Musicでは、#WAVxxに記載のファイル名とは異なる形式でサウンドファイルが格納されていることがしばしばあります。
	// リストの左から順にファイルの拡張子を変えながらサウンドファイルを検索するのに利用されます。
	private static final Map<String, List<String>> EXT_ORDERS = Map.of(
			".wav", List.of(".wav", ".ogg", ".mp3"),
			".ogg", List.of(".ogg", ".wav", ".mp3"),
			".mp3", List.of(".mp3", ".wav", ".ogg"));

	// #BASEに記述された基数に対応した整数変換オブジェクト
	private static BmsInt base;
	// 再生対象BMSファイルのヘッダ情報
	private static BeMusicHeader header;
	// 再生対象BMSファイルの譜面情報
	private static BeMusicChart chart;
	// 楽曲再生用のAudioオブジェクト。
	// このチュートリアルでは楽曲再生にOpenALを使用します。
	private static OpenALLwjgl3Audio audio;
	// 再生対象BMSファイルに記述された#WAVxxから読み込んだサウンドファイルのマップ。
	// 楽曲再生時、可視オブジェ・BGMの値から再生すべきサウンドを検索する際に利用されます。
	private static Map<Integer, OpenALSound> sounds = new HashMap<>();

	public static void main(String[] args) throws Exception {
		// BMS Libraryのロゴを表示します。(特に深い意味はありません)
		BmsLibrary.printLogo();

		// 再生対象BMSファイルから、適切なBMSローダを選択し生成します。
		// このチュートリアルではbmsonの楽曲再生に必要な複雑な実装を行っていないのでbmson形式はエラーとして処理します。
		Path bmsPath = Path.of(args[0]);
		BmsLoader loader = BeMusic.createLoaderFor(bmsPath);
		if (!loader.isStandard()) {
			System.out.println("In this tutorial, does not support bmson playback.");
			return;
		}

		// 再生対象BMSファイルを読み込み、ヘッダ情報と譜面情報を生成します。
		// BeMusic#createLoaderFor()で生成されたBMSローダには何も設定されていないので、以下の設定を行います。
		// 1.最新のBe-Music仕様を使用する
		// 2.#RANDOMを有効にする
		// 3.文字コードの優先順位をUTF-8→MS932(Shift-JIS)にする
		// 4.BMSファイルの構文チェック等のエラーに寛容的な設定にする(デフォルトでそうなっているので、なくても構いません)
		BmsContent content = loader
				.setSpec(BeMusicSpec.LATEST)
				.setHandler(new BeMusicLoadHandler().setEnableControlFlow(true))
				.setCharsets(StandardCharsets.UTF_8, Charset.forName("MS932"))
				.setStrictly(false)
				.load(bmsPath);
		header = BeMusicHeader.of(content);
		base = BmsInt.of(header.getBase());
		chart = BeMusicChartBuilder.createChart(content);

		// 楽曲の基本的な情報を表示しておきます。
		// ※表示しておかないと、何の曲を再生するのか分からなくなります
		System.out.println("Title : " + header.getTitle() + " " + header.getSubTitle());
		System.out.println("Artist: " + header.getArtist());
		System.out.println("BPM   : " + header.getInitialBpm());

		try {
			// 最初にAudioオブジェクトを構築します。
			// 同時発音数以外の第2,3引数の値はデフォルト値ですが、このチュートリアルでは使用しない機能の引数なので、
			// どのような値を指定してもチュートリアルへの作用はありません。
			audio = new OpenALLwjgl3Audio(POLYPHONY, 9, 512);

			// #WAVxxで定義されているサウンドファイルを全件読み込みます。
			// 譜面情報を解析し、使用されているサウンドファイルのみを読み込むようにすると高速化できますが、
			// ロジックが複雑になるので本チュートリアルでは一律全件読み込むものとします。
			int lastPer = -1;
			int errNum = 0;
			for (Map.Entry<Integer, String> wav : header.getWavs().entrySet()) {
				// 規定のルールに従ってサウンドファイルを読み込みます。(loadSound()参照)
				// 読み込みエラーの場合はエラー件数をカウントします。
				OpenALSound sound = loadSound(bmsPath.getParent().resolve(wav.getValue()).toString());
				if (sound != null) {
					sounds.put(wav.getKey(), sound);
				} else {
					errNum++;
				}

				// ローディングの現在の進捗状況をゲージ形式で表示します。
				double progress = (sounds.size() + errNum) / (double)header.getWavs().size();
				int bar = (int)(progress * 30.0);
				int progPer = (int)(progress * 100);
				if (progPer != lastPer) {
					// パーセンテージが更新された場合のみ画面表示を更新します。
					lastPer = progPer;
					System.out.printf("Loading [%s%s] %3d%%\r", "=".repeat(bar), " ".repeat(30 - bar), progPer);
				}
			}
			System.out.println();
			System.out.println("Done" + ((errNum == 0) ? "" : ", but %d files are failed to load.".formatted(errNum)));

			// 譜面・情報のヘッダ部分を先頭に表示します。
			// 以降、楽曲の進行に従って譜面と各種情報が下部に追記されていきます。
			List<String> chartHdr = header.getPlayer().isSinglePlay() ? SP_HEADER : DP_HEADER;
			chartHdr.forEach(System.out::println);

			// 再生を開始する前に再生時間を計算します。
			// BeMusicChart#getPlayTime()は、先頭から最後の操作可能ノートに到達するための時間です。
			// その時間ではまだ何らかの音が鳴っている場合があるので、最後の音が鳴り止むまでの時間を計算し、
			// その時間までを再生時間とします。
			double duration = chart.computeActualPlayTime(rawNote -> {
				OpenALSound sound = getShouldPlaySound(rawNote);
				return Objects.nonNull(sound) ? sound.duration() : 0.0;
			});

			// 簡易シーケンサによって楽曲の再生を行います。
			// 楽曲の先頭から順に、楽曲位置情報の時間に到達する度にprocessPoint()を呼び出すことを繰り返します。
			// 最後の楽曲位置情報まで到達し、全ての音が鳴り止んだ後、処理が返ります。
			BeMusicSequencer.play(chart, duration, 0.0005, View::processPoint);
		} finally {
			// プログラムの終了前に全てのサウンドとAudioオブジェクトを解放します。
			sounds.values().forEach(OpenALSound::dispose);
			audio.dispose();
		}
	}

	// このメソッドは楽曲位置情報に記録された時間に到達する度に、当該楽曲位置情報の処理を行うために呼び出されます。
	// このチュートリアルでは可視オブジェとBGMを理想的なタイミングで再生したいので、楽曲位置情報に記録されている
	// 可視オブジェ・BGMのサウンドを直ちに再生します。また、再生後は譜面と各種情報のテキスト表示を行います。
	private static void processPoint(BeMusicPoint p) {
		// できるだけ再生遅延を発生させないために楽曲位置情報通知後直ちにサウンド再生を行います。
		p.sounds(true, false, true)
				.mapToObj(View::getShouldPlaySound)
				.filter(Objects::nonNull)
				.forEach(s -> s.play(MASTER_VOLUME));

		// 小節線のある楽曲位置情報では、小節番号を表示します。
		if (p.hasMeasureLine()) {
			System.out.printf("%03d|", p.getMeasure());
		} else {
			System.out.print("   |");
		}

		// SP/DPごとに、この楽曲位置での譜面を表示します。
		// 表示では、実際のレーンの順に左側から表示を行っていきます。
		if (header.getPlayer().isSinglePlay()) {
			// シングルプレー譜面
			printLane(BeMusicDevice.orderedBySpLeftList(), p, true);
		} else {
			// ダブルプレー譜面
			printLane(BeMusicDevice.orderedByDpList(BeMusicLane.PRIMARY), p, false);
			printLane(BeMusicDevice.orderedByDpList(BeMusicLane.SECONDARY), p, false);
		}

		// その他情報を表示します。
		// 楽曲位置に記録されている全ての情報をINFORMATION枠に表示します。
		List<String> info = new ArrayList<String>(10);
		info.add(p.hasBpm() ? "BPM=" + p.getCurrentBpm() : "");
		info.add(p.hasScroll() ? "SCR=" + p.getCurrentScroll() : "");
		info.add(p.hasStop() ? "STOP=" + p.getStop() : "");
		info.add(p.hasBga() ? "BGA=" + base.tos(p.getBgaValue()) : "");
		info.add(p.hasLayer() ? "LAYER=" + base.tos(p.getLayerValue()) : "");
		info.add(p.hasMiss() ? "MISS=" + base.tos(p.getMissValue()) : "");
		info.add(!p.getText().isEmpty() ? "TEXT=" + p.getText() : "");
		info.add(p.hasBgm() ? "BGM=" + toStringBgms(p) : "");
		System.out.println(info.stream().filter(s -> !s.isEmpty()).collect(Collectors.joining(", ")));
	}

	// 譜面の表示処理を行うメソッドです。
	// devsのリストに格納された順でスイッチ/スクラッチに割り当てられたノートを楽曲位置から取り出し、表示します。
	private static void printLane(List<BeMusicDevice> devs, BeMusicPoint p, boolean large) {
		int offset = large ? 1 : 0;
		for (BeMusicDevice dev : devs) {
			BeMusicNoteType noteType = p.getVisibleNoteType(dev);
			String noteStr = NOTES.get(noteType).get(offset + (dev.isScratch() ? 1 : 0));
			System.out.print(noteStr);
		}
		System.out.print("|");
	}

	// INFORMATION枠に表示するBGMの一覧を文字列化するメソッドです。
	// 全てのBGMのトラックID(#WAVxxのインデックス値)をカンマ区切りで表現する文字列を生成して返します。
	private static String toStringBgms(BeMusicPoint p) {
		return p.bgms()
				.map(BeMusicSound::getTrackId)
				.mapToObj(base::tos)
				.collect(Collectors.joining(",", "(", ")"));
	}

	// サウンドファイルをロードするメソッドです。
	private static OpenALSound loadSound(String path) {
		// 指定サウンドファイルの形式(拡張子)を判定し、サウンドファイルを検索する順を決定します。
		// 必ずしも#WAVxxに記述された形式のファイルが同梱されているわけではないので、優先順にファイルを検索します。
		int pos = path.lastIndexOf('.');
		String ext = (pos == -1) ? "" : path.substring(pos);
		List<String> extOrders = Optional.ofNullable(EXT_ORDERS.get(ext)).orElse(Collections.emptyList());

		// サウンドファイルを検索し、最初に見つかったファイルでロードを試みます。
		for (String extOrder : extOrders) {
			String actualPath = path.replaceFirst(ext + "$", extOrder);
			if (Files.isRegularFile(Path.of(actualPath))) {
				try {
					// サウンドファイルの読み込みとデコード処理を実行します。
					return audio.newSound(new FileHandle(actualPath));
				} catch (Exception e) {
					// 読み込み失敗(形式・データ不正またはガチのIOエラー)の時は読み込み失敗とします。
					return null;
				}
			}
		}
		return null;
	}

	// 再生するべきサウンドをロード済みのサウンドから検索して返すメソッドです。
	// 引数sにはノートの生値が指定されます。※この値からサウンドの種類、ノート種別、トラックIDが取り出せます
	// ファイルなし、読み込みエラー等で該当するトラックIDのサウンドが存在しない場合はnullを返します。
	private static OpenALSound getShouldPlaySound(int s) {
		if (BeMusicSound.isVisible(s)) {
			// 可視オブジェの場合、サウンド再生を伴う可能性があるオブジェのみ再生を行います。
			// それ以外のノートでは再生を行いません。(そんなのを再生したら曲がえらいことになりますw)
			BeMusicNoteType type = BeMusicSound.getNoteType(s);
			BeMusicDevice device = BeMusicSound.getDevice(s);
			return type.hasSound(device) ? sounds.get(BeMusicSound.getTrackId(s)) : null;
		} else if (BeMusicSound.isBgm(s)) {
			// BGMの場合、無条件に当該サウンドを再生します。
			return sounds.get(BeMusicSound.getTrackId(s));
		} else {
			// それ以外(不可視オブジェ)は再生しません。
			// ※ユーザー操作のない状況では不可視オブジェが再生される契機はありません
			return null;
		}
	}
}
