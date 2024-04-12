package com.lmt.lib.bms.bemusic;

import com.lmt.lib.bms.BmsChannel;
import com.lmt.lib.bms.BmsMeta;
import com.lmt.lib.bms.BmsSpec;
import com.lmt.lib.bms.BmsSpecBuilder;

/**
 * BeMusicのBMS仕様を表します。
 * <p>当クラスは、BeMusicのBMS仕様に関連する定義のプレースホルダの役割を果たします。そのため、インスタンスを生成することを想定していません。</p>
 * <p>BeMusicライブラリの通常の利用想定としては、当クラスの提供する定義をアプリケーションから直接使用することは推奨していません。BeMusicライブラリの
 * 初期化時に生成したBMS仕様にアクセスしたい場合は当クラスを用いて新しいインスタンスを生成するのではなく{@link BeMusic#getSpec}を使用してください。</p>
 */
public class BeMusicSpec {
	/**
	 * BeMusicライブラリのBMS仕様バージョン(V1)を表します。
	 * @see BeMusic#initialize
	 */
	public static final int V1 = 0;

	/**
	 * BeMusic用のBMS仕様を生成します。
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
