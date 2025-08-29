package com.lmt.lib.bms;

import java.util.Objects;

/**
 * タイムラインにおけるチャンネル番号およびチャンネルインデックスを表すインターフェイスです。
 *
 * <p>「CHX」とは、「チャンネル」と「インデックス」の単語をくっつけて略称化したBMSライブラリ専用の造語です。</p>
 *
 * <p>タイムラインのCHXの有効範囲は、チャンネル番号が{@link BmsSpec#CHANNEL_MIN}～{@link BmsSpec#CHANNEL_MAX}、
 * チャンネルインデックスが{@link BmsSpec#CHINDEX_MIN}～{@link BmsSpec#CHINDEX_MAX}となります。</p>
 *
 * @since 0.1.0
 */
public interface BmsChx {
	/**
	 * チャンネル番号を取得します。
	 * @return チャンネル番号
	 */
	int getChannel();

	/**
	 * チャンネルインデックスを取得します。
	 * @return チャンネルインデックス
	 */
	int getIndex();

	/**
	 * チャンネル番号が小節線チャンネルかどうかを判定します。
	 * <p>当メソッドの結果がtrueの場合、{@link #getChannel()}が返す値が{@link BmsSpec#CHANNEL_MEASURE}であることを示します。</p>
	 * @return チャンネル番号が小節線チャンネルの場合true
	 */
	default boolean isMeasureLineChannel() {
		return getChannel() == BmsSpec.CHANNEL_MEASURE;
	}

	/**
	 * チャンネル番号が仕様チャンネルかどうかを判定します。
	 * <p>当メソッドの結果がtrueの場合、{@link #getChannel()}が返す値が
	 * {@link BmsSpec#SPEC_CHANNEL_MIN}～{@link BmsSpec#SPEC_CHANNEL_MAX}の範囲であることを示します。</p>
	 * @return チャンネル番号が仕様チャンネルの場合true
	 */
	default boolean isSpecChannel() {
		var channel = getChannel();
		return (channel >= BmsSpec.SPEC_CHANNEL_MIN) && (channel <= BmsSpec.SPEC_CHANNEL_MAX);
	}

	/**
	 * チャンネル番号がユーザーチャンネルかどうかを判定します。
	 * <p>当メソッドの結果がtrueの場合、{@link #getChannel()}が返す値が
	 * {@link BmsSpec#USER_CHANNEL_MIN}～{@link BmsSpec#USER_CHANNEL_MAX}の範囲であることを示します。</p>
	 * @return チャンネル番号がユーザーチャンネルの場合true
	 */
	default boolean isUserChannel() {
		var channel = getChannel();
		return (channel >= BmsSpec.USER_CHANNEL_MIN) && (channel <= BmsSpec.USER_CHANNEL_MAX);
	}

	/**
	 * 2つのCHXを比較し、同じ場所を指しているかどうかを判定します。
	 * @param chx1 CHX1
	 * @param chx2 CHX2
	 * @return 2つのCHXが同じ場所を指していればtrue
	 * @throws NullPointerException chx1またはchx2がnull
	 */
	public static boolean equals(BmsChx chx1, BmsChx chx2) {
		return (chx1.getChannel() == chx2.getChannel()) && (chx1.getIndex() == chx2.getIndex());
	}

	/**
	 * 2つのCHXを比較します。
	 * @param chx1 CHX1
	 * @param chx2 CHX2
	 * @return CHX1 == CHX2は0、CHX1 &gt; CHX2は正の値、CHX1 &lt; CHX2は負の値
	 * @throws NullPointerException chx1またはchx2がnull
	 */
	public static int compare(BmsChx chx1, BmsChx chx2) {
		return Integer.compareUnsigned(toInt(chx1), toInt(chx2));
	}

	/**
	 * CHXからハッシュコード値を生成します。
	 * @param chx CHX
	 * @return ハッシュコード値
	 * @throws NullPointerException chxがnull
	 */
	public static int hashCode(BmsChx chx) {
		return Objects.hashCode(toInt(chx));
	}

	/**
	 * CHXの文字列表現を返します。
	 * @param chx CHX
	 * @return CHXの文字列表現
	 * @throws NullPointerException chxがnull
	 */
	public static String toString(BmsChx chx) {
		return toString(chx.getChannel(), chx.getIndex());
	}

	/**
	 * CHX値の文字列表現を返します。
	 * @param chx CHX値
	 * @return CHX値の文字列表現
	 */
	public static String toString(int chx) {
		return toString(toChannel(chx), toIndex(chx));
	}

	/**
	 * チャンネル番号とチャンネルインデックスの文字列表現生成
	 * @param channel チャンネル番号
	 * @param index チャンネルインデックス
	 * @return チャンネル番号とチャンネルインデックスの文字列表現
	 */
	private static String toString(int channel, int index) {
		return String.format("{C=%d(%s), I=%d}", channel, BmsInt.to36s(channel), index);
	}

	/**
	 * チャンネル番号とチャンネルインデックスからCHX値に変換します。
	 * <p>CHX値は、チャンネル番号とチャンネルインデックスを1個の32ビット整数値として表現した値のことを指します。
	 * 指定したチャンネル番号、チャンネルインデックスは下位16ビットのみを使用し0～65535までの値になります。
	 * 上位16ビットがどのような状態であってもCHX値の計算結果には影響を及ぼしません。また、{@link BmsSpec}で定義された
	 * 範囲外の値を指定しても範囲内チェックは行われず、例外もスローされません。</p>
	 * @param channel チャンネル番号
	 * @param index チャンネルインデックス
	 * @return CHX値
	 */
	public static int toInt(int channel, int index) {
		return ((channel & 0xffff) << 16) | (index & 0xffff);
	}

	/**
	 * チャンネル番号からCHX値に変換します。
	 * <p>CHX値は、チャンネル番号とチャンネルインデックスを1個の32ビット整数値として表現した値のことを指します。
	 * 当メソッドではチャンネルインデックスは0が指定されたものとしてCHX値への変換を行います。
	 * 指定したチャンネル番号は下位16ビットのみを使用し0～65535までの値になります。
	 * 上位16ビットがどのような状態であってもCHX値の計算結果には影響を及ぼしません。また、{@link BmsSpec}で定義された
	 * 範囲外の値を指定しても範囲内チェックは行われず、例外もスローされません。
	 * @param channel チャンネル番号
	 * @return CHX値
	 */
	public static int toInt(int channel) {
		return (channel & 0xffff) << 16;
	}

	/**
	 * CHXからCHX値に変換します。
	 * <p>CHX値は、チャンネル番号とチャンネルインデックスを1個の32ビット整数値として表現した値のことを指します。
	 * 指定したチャンネル番号、チャンネルインデックスは下位16ビットのみを使用し0～65535までの値になります。
	 * 上位16ビットがどのような状態であってもCHX値の計算結果には影響を及ぼしません。また、{@link BmsSpec}で定義された
	 * 範囲外の値を指定しても範囲内チェックは行われず、例外もスローされません。</p>
	 * @param chx CHX
	 * @return CHX値
	 * @throws NullPointerException chxがnull
	 */
	public static int toInt(BmsChx chx) {
		return ((chx.getChannel() & 0xffff) << 16) | (chx.getIndex() & 0xffff);
	}

	/**
	 * CHX値からチャンネル番号を取得します。
	 * <p>CHX値は生成の際に範囲外チェックを行わないため、{@link BmsSpec#CHANNEL_MIN}～{@link BmsSpec#CHANNEL_MAX}
	 * の範囲外になることがあることに注意してください。</p>
	 * @param chx CHX値
	 * @return チャンネル番号
	 */
	public static int toChannel(int chx) {
		return (chx >> 16) & 0xffff;
	}

	/**
	 * CHX値からチャンネルインデックスを取得します。
	 * <p>CHX値は生成の際に範囲外チェックを行わないため、{@link BmsSpec#CHINDEX_MIN}～{@link BmsSpec#CHINDEX_MAX}
	 * の範囲外になることがあることに注意してください。</p>
	 * @param chx CHX値
	 * @return チャンネルインデックス
	 */
	public static int toIndex(int chx) {
		return chx & 0xffff;
	}
}
