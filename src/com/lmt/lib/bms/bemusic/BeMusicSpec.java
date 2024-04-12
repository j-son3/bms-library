package com.lmt.lib.bms.bemusic;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;

import com.lmt.lib.bms.BmsChannel;
import com.lmt.lib.bms.BmsContent;
import com.lmt.lib.bms.BmsException;
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
				.addMeta(BeMusicMeta.CHARTNAME)
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
				.addMeta(BeMusicMeta.EYECATCH)
				.addMeta(BeMusicMeta.PREVIEW)
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
				.addMeta(BeMusicMeta.LNMODE)
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
				.addMeta(BeMusicMeta.SCROLL)
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
				.addMeta(BeMusicMeta.ENDIF)
				.addMeta(BeMusicMeta.ENDRANDOM);

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
				.addChannel(BeMusicChannel.OPTION)
				.addChannel(BeMusicChannel.SCROLL);
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
	 * 最新バージョンのBe-Music用BMS仕様を生成します。
	 * <p>当メソッドは、最新バージョンのBe-Music用BMS仕様に任意型メタ情報かユーザーチャンネル、
	 * またはその両方を付加して生成したい時のヘルパーメソッドです。</p>
	 * <p>任意型メタ情報、ユーザーチャンネルのコレクションにnullを指定すると何も付加されずにBMS仕様が生成されます。</p>
	 * @param objectMetas 任意型メタ情報のコレクション
	 * @param userChannels ユーザーチャンネルのコレクション
	 * @return 最新バージョンのBe-Music用BMS仕様
	 * @exception NullPointerException objectMetasのコレクション内にnullが含まれていた
	 * @exception NullPointerException userChannelsのコレクション内にnullが含まれていた
	 * @exception IllegalArgumentException objectMetasのコレクション内に任意型以外のメタ情報が含まれていた
	 * @exception IllegalArgumentException userChannelsのコレクション内に仕様チャンネルが含まれていた
	 */
	public static BmsSpec createLatest(Collection<BmsMeta> objectMetas, Collection<BmsChannel> userChannels) {
		return create(
				LATEST_VERSION,
				(objectMetas == null) || (objectMetas.isEmpty()) ? null : objectMetas.toArray(BmsMeta[]::new),
				(userChannels == null) || (userChannels.isEmpty()) ? null : userChannels.toArray(BmsChannel[]::new));
	}

	/**
	 * 最新バージョンのBe-Music用BMS仕様を用いて指定パスのファイルからBMSコンテンツを読み込みます。
	 * <p>当メソッドは{@link BeMusic#loadContentFrom(Path, Long, boolean)}を乱数の固定化なしで呼び出します。</p>
	 * @param path 読み込み対象のBMSファイルのパス
	 * @param strictly 厳格なフォーマットチェックを行うかどうか
	 * @return 最新バージョンのBe-Music用BMS仕様で読み込まれたBMSコンテンツ
	 * @exception NullPointerException pathがnull
	 * @exception IOException 指定されたファイルが見つからない、読み取り権限がない、または読み取り中に異常を検出した
	 * @exception BmsException 読み込み処理中に想定外の例外がスローされた
	 * @see BeMusic#loadContentFrom(Path, Long, boolean)
	 * @deprecated 当メソッドはBMS Library ver.0.7.0以降、非推奨になりました。
	 */
	@Deprecated(since = "0.7.0")
	public static BmsContent loadContentFrom(Path path, boolean strictly) throws BmsException, IOException {
		return BeMusic.loadContentFrom(path, null, strictly);
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
