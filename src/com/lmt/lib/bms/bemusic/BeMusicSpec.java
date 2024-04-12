package com.lmt.lib.bms.bemusic;

import static com.lmt.lib.bms.bemusic.Assertion.*;

import java.nio.file.Path;

import com.lmt.lib.bms.BmsChannel;
import com.lmt.lib.bms.BmsContent;
import com.lmt.lib.bms.BmsException;
import com.lmt.lib.bms.BmsLoadError;
import com.lmt.lib.bms.BmsLoader;
import com.lmt.lib.bms.BmsMeta;
import com.lmt.lib.bms.BmsSpec;
import com.lmt.lib.bms.BmsSpecBuilder;

/**
 * Be-MusicのBMS仕様を表します。
 *
 * <p>当クラスは、Be-MusicのBMS仕様に関連する定義のプレースホルダの役割を果たします。
 * そのため、インスタンスを生成することを想定していません。</p>
 *
 * <p>当クラスの役割は、ライブラリの利用者が希望する形式でのBe-Music用BMS仕様({@link BmsSpec})を提供すること、
 * またはそのBMS仕様を用いて一般的なBMSファイル読み込み機能を提供することにあります。</p>
 */
public class BeMusicSpec {
	/** Be-MusicのBMS仕様バージョン(V1)を表します。 */
	public static final int V1 = 0;
	/** Be-Music用BMS仕様の最新バージョン */
	public static final int LATEST_VERSION = V1;

	/**
	 * 最新バージョンのBe-Music用BMS仕様です。
	 * <p>このBMS仕様は{@link #LATEST_VERSION}に対応したものです。任意型メタ情報、ユーザーチャンネルは何も指定されていません。</p>
	 */
	public static final BmsSpec LATEST = create(LATEST_VERSION, null, null);

	/**
	 * Be-Music用のBMS仕様を生成します。
	 * @param specVersion BMS仕様のバージョン
	 * @param objectMetas BMS仕様に含める任意型メタ情報のリスト
	 * @param userChannels BMS仕様に含めるユーザーチャンネルのリスト
	 * @return BeMusic用のBMS仕様
	 * @exception IllegalArgumentException specVersionに未知の値を指定した
	 * @exception IllegalArgumentException objectMetasのリスト内に任意型以外のメタ情報が含まれていた
	 * @exception IllegalArgumentException userChannelsのリスト内に仕様チャンネルが含まれていた
	 */
	public static BmsSpec create(int specVersion, BmsMeta[] objectMetas, BmsChannel[] userChannels) {
		switch (specVersion) {
		case V1: return createV1(objectMetas, userChannels);
		default: throw new IllegalArgumentException(String.format("Invalid specVersion. [%d]", specVersion));
		}
	}

	/**
	 * {@link #V1}のBeMusic用BMS仕様を生成します。
	 * @param objectMetas BMS仕様に含める任意型メタ情報のリスト
	 * @param userChannels BMS仕様に含めるユーザーチャンネルのリスト
	 * @return BeMusic用のBMS仕様
	 * @exception IllegalArgumentException objectMetasのリスト内に任意型以外のメタ情報が含まれていた
	 * @exception IllegalArgumentException userChannelsのリスト内に仕様チャンネルが含まれていた
	 */
	public static BmsSpec createV1(BmsMeta[] objectMetas, BmsChannel[] userChannels) {
		assertArgObjectMetas(objectMetas);
		assertArgUserChannels(userChannels);

		var builder = new BmsSpecBuilder();

		// メタ情報を登録する
		// BMS書き出し時は上から登録順に書き出されるので、記述順に注意すること
		builder
				.addMeta(BeMusicMeta.PLAYER)
				.addMeta(BeMusicMeta.GENRE)
				.addMeta(BeMusicMeta.TITLE)
				.addMeta(BeMusicMeta.SUBTITLE)
				.addMeta(BeMusicMeta.ARTIST)
				.addMeta(BeMusicMeta.SUBARTIST)
				.addMeta(BeMusicMeta.INITIAL_BPM)
				.addMeta(BeMusicMeta.BASEBPM)
				.addMeta(BeMusicMeta.CDDA)
				.addMeta(BeMusicMeta.MIDIFILE)
				.addMeta(BeMusicMeta.DIFFICULTY)
				.addMeta(BeMusicMeta.PLAYLEVEL)
				.addMeta(BeMusicMeta.RANK)
				.addMeta(BeMusicMeta.DEFEXRANK)
				.addMeta(BeMusicMeta.TOTAL)
				.addMeta(BeMusicMeta.VOLWAV)
				.addMeta(BeMusicMeta.CHARFILE)
				.addMeta(BeMusicMeta.COMMENT)
				.addMeta(BeMusicMeta.BANNER)
				.addMeta(BeMusicMeta.STAGEFILE)
				.addMeta(BeMusicMeta.BACKBMP)
				.addMeta(BeMusicMeta.POORBGA)
				.addMeta(BeMusicMeta.MOVIE)
				.addMeta(BeMusicMeta.VIDEOFILE)
				.addMeta(BeMusicMeta.VIDEOFPS)
				.addMeta(BeMusicMeta.VIDEOCOLORS)
				.addMeta(BeMusicMeta.VIDEODLY)
				.addMeta(BeMusicMeta.PATH_WAV)
				.addMeta(BeMusicMeta.MATERIALSWAV)
				.addMeta(BeMusicMeta.MATERIALSBMP)
				.addMeta(BeMusicMeta.DIVIDEPROP)
				.addMeta(BeMusicMeta.CHARSET)
				.addMeta(BeMusicMeta.OCT_FP)
				.addMeta(BeMusicMeta.LNTYPE)
				.addMeta(BeMusicMeta.LNOBJ)
				.addMeta(BeMusicMeta.MAKER)
				.addMeta(BeMusicMeta.URL)
				.addMeta(BeMusicMeta.EMAIL)
				.addMeta(BeMusicMeta.WAV)
				.addMeta(BeMusicMeta.EXWAV)
				.addMeta(BeMusicMeta.WAVCMD)
				.addMeta(BeMusicMeta.BMP)
				.addMeta(BeMusicMeta.EXBMP)
				.addMeta(BeMusicMeta.BGA)
				.addMeta(BeMusicMeta.SWBGA)
				.addMeta(BeMusicMeta.ARGB)
				.addMeta(BeMusicMeta.EXTCHR)
				.addMeta(BeMusicMeta.BPM)
				.addMeta(BeMusicMeta.EXBPM)
				.addMeta(BeMusicMeta.STOP)
				.addMeta(BeMusicMeta.STP)
				.addMeta(BeMusicMeta.EXRANK)
				.addMeta(BeMusicMeta.OPTION)
				.addMeta(BeMusicMeta.CHANGEOPTION)
				.addMeta(BeMusicMeta.SEEK)
				.addMeta(BeMusicMeta.TEXT)
				.addMeta(BeMusicMeta.SONG)
				.addMeta(BeMusicMeta.RANDOM)
				.addMeta(BeMusicMeta.IF)
				.addMeta(BeMusicMeta.ELSEIF)
				.addMeta(BeMusicMeta.ELSE)
				.addMeta(BeMusicMeta.ENDIF);

		// チャンネルを登録する
		builder
				.addChannel(BeMusicChannel.BGM)
				.addChannel(BeMusicChannel.LENGTH)
				.addChannel(BeMusicChannel.BPM_LEGACY)
				.addChannel(BeMusicChannel.BGA)
				.addChannel(BeMusicChannel.EXT_OBJ)
				.addChannel(BeMusicChannel.BGA_MISS)
				.addChannel(BeMusicChannel.BGA_LAYER)
				.addChannel(BeMusicChannel.BPM)
				.addChannel(BeMusicChannel.STOP)
				.addChannel(BeMusicChannel.BGA_LAYER2)
				.addChannel(BeMusicChannel.BGA_BASE_OPACITY)
				.addChannel(BeMusicChannel.BGA_LAYER_OPACITY)
				.addChannel(BeMusicChannel.BGA_LAYER2_OPACITY)
				.addChannel(BeMusicChannel.BGA_MISS_OPACITY)
				.addChannel(BeMusicChannel.BGM_VOLUME)
				.addChannel(BeMusicChannel.KEY_VOLUME)
				.addChannel(BeMusicChannel.TEXT)
				.addChannel(BeMusicChannel.JUDGE)
				.addChannel(BeMusicChannel.BGA_BASE_ARGB)
				.addChannel(BeMusicChannel.BGA_LAYER_ARGB)
				.addChannel(BeMusicChannel.BGA_LAYER2_ARGB)
				.addChannel(BeMusicChannel.BGA_MISS_ARGB)
				.addChannel(BeMusicChannel.BGA_KEYBOUND)
				.addChannel(BeMusicChannel.OPTION);
		for (var c : BeMusicChannel.VISIBLE_1P_CHANNELS) { builder.addChannel(c); }
		for (var c : BeMusicChannel.VISIBLE_2P_CHANNELS) { builder.addChannel(c); }
		for (var c : BeMusicChannel.INVISIBLE_CHANNELS) { builder.addChannel(c); }
		for (var c : BeMusicChannel.LONG_CHANNELS) { builder.addChannel(c); }
		for (var c : BeMusicChannel.LANDMINE_CHANNELS) { builder.addChannel(c); }

		// 任意型メタ情報とユーザーチャンネルを登録する
		if (objectMetas != null) {
			for (var m : objectMetas) { builder.addMeta(m); }
		}
		if (userChannels != null) {
			for (var c : userChannels) { builder.addChannel(c); }
		}

		// 初期BPMメタ情報、小節長変更、BPM変更、譜面停止チャンネルを設定する
		builder
				.setInitialBpmMeta(BeMusicMeta.BPM.getName())
				.setLengthChannel(BeMusicChannel.LENGTH.getNumber())
				.setBpmChannel(BeMusicChannel.BPM_LEGACY.getNumber(), BeMusicChannel.BPM.getNumber())
				.setStopChannel(BeMusicChannel.STOP.getNumber());

		return builder.create();
	}

	/**
	 * 最新バージョンのBe-Music用BMS仕様を用いて指定パスのファイルからBMSコンテンツを読み込みます。
	 * <p>当メソッドは最も一般的なBMSコンテンツの読み込み機能を提供します。通常、BeMusicサブセットが持つ柔軟かつ高機能な
	 * 処理を行うためには複数の煩雑な設定を行ったうえで読み込みを行う必要がありますが「ファイルからBMSコンテンツを読み込む」
	 * という一般的な処理を行いたい場合には当メソッドを用いるのが最適な選択肢となります。</p>
	 * <p>2番目の引数は、読み込みの際に厳格なフォーマットチェックを行うかどうかを選択します。
	 * falseを指定するとBMS定義の構文エラー、値の範囲・書式、チャンネルの定義ミスなど、様々な誤りに対して寛容的になり、
	 * 誤りを検出した際にはその誤りを無視して読み込みを続行します。この設定で読み込みを行うと当メソッドが例外をスローする
	 * ケースが減少しますが、代償としてBMS定義の誤りに気付く機会が失われ、読み込まれたBMSコンテンツが意図せずに期待とは異なる
	 * 内容になってしまう可能性があります。trueを指定すると以下のような場合にメソッドが例外をスローするようになります。</p>
	 * <ul>
	 * <li>BMSフォーマットとして不正な構文が使用されている時</li>
	 * <li>未知のメタ情報(ヘッダ)が定義されている時</li>
	 * <li>メタ情報の値が想定する形式の値になっていない時(例えば数値を設定する箇所に数値以外を記述する等)</li>
	 * <li>未知のチャンネル(番号)が定義されている時</li>
	 * <li>チャンネルに設定した値の形式が不正な時</li>
	 * <li>乱数(#RANDOM/#IF/#ELSE/#ENDIF等)の定義階層が不正な時</li>
	 * </ul>
	 * <p>上記以外にもファイルの読み込み中にエラーが発生する等、複数の要因で例外がスローされる可能性があります。
	 * 例外およびエラーの詳細については{@link BmsException}、{@link BmsLoadError}を参照してください。</p>
	 * <p>当メソッドでは乱数を使用したCONTROL FLOW読み込みを行います。そのため乱数を使用したBMSコンテンツでは
	 * 読み込み毎に異なる内容のBMSコンテンツになる場合があることに留意してください。</p>
	 * <p>詳細な読み込み設定を行ったうえでBMSコンテンツを様々なデータ型から読み込みたい場合には、
	 * {@link BmsLoader}と{@link BeMusicLoadHandler}を参照し、必要な手続きを行う処理を記述してください。
	 * 当メソッド内部では一般的な用途向けで手続きを行っています。</p>
	 * @param path 読み込み対象のBMSファイルのパス
	 * @param strictly 厳格なフォーマットチェックを行うかどうか
	 * @return 最新バージョンのBe-Music用BMS仕様で読み込まれたBMSコンテンツ
	 * @throws BmsException BMSファイルの読み込みに失敗した
	 * @exception NullPointerException pathがnull
	 * @see BmsLoader
	 * @see BmsLoadError
	 * @see BeMusicLoadHandler
	 */
	public static BmsContent loadContentFrom(Path path, boolean strictly) throws BmsException {
		assertArgNotNull(path, "path");
		var handler = new BeMusicLoadHandler()
				.setEnableControlFlow(true)
				.setForceRandomValue(null);
		var loader = new BmsLoader()
				.setSpec(LATEST)
				.setHandler(handler)
				.setSyntaxErrorEnable(strictly)
				.setFixSpecViolation(!strictly)
				.setIgnoreUnknownMeta(!strictly)
				.setIgnoreUnknownChannel(!strictly)
				.setIgnoreWrongData(!strictly);
		return loader.load(path);
	}

	/**
	 * 任意型メタ情報リストのアサーション。
	 * @param objectMetas 任意型メタ情報のリスト
	 * @exception IllegalArgumentException objectMetasのリスト内に任意型以外のメタ情報が含まれていた
	 */
	private static void assertArgObjectMetas(BmsMeta[] objectMetas) {
		if (objectMetas == null) {
			return;
		}
		for (var i = 0; i < objectMetas.length; i++) {
			if (!objectMetas[i].isObject()) {
				throw new IllegalArgumentException(String.format(
						"There is non-object meta in the list. [index=%d]", i));
			}
		}
	}

	/**
	 * ユーザーチャンネルリストのアサーション。
	 * @param userChannels ユーザーチャンネルのリスト
	 * @exception IllegalArgumentException userChannelsのリスト内に仕様チャンネルが含まれていた
	 */
	private static void assertArgUserChannels(BmsChannel[] userChannels) {
		if (userChannels == null) {
			return;
		}
		for (var i = 0; i < userChannels.length; i++) {
			if (!userChannels[i].isUser()) {
				throw new IllegalArgumentException(String.format(
						"There is non-user channel in the list. [index=%d]", i));
			}
		}
	}
}
