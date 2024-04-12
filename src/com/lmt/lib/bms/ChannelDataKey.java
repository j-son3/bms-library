package com.lmt.lib.bms;

/**
 * チャンネルデータキークラス。
 *
 * <p>チャンネルデータキーとは、チャンネル番号・チャンネルインデックスをキーとする値のこと。
 * これら2つの数値データを1つの32ビット整数値として表す。</p>
 */
class ChannelDataKey {
	/**
	 * チャンネルデータキー生成
	 * @param note ノート
	 * @return チャンネルデータキー
	 */
	static int make(BmsNote note) {
		return make(note.getChannel(), note.getIndex());
	}

	/**
	 * チャンネルデータキー生成
	 * @param channel チャンネル番号
	 * @param index チャンネルインデックス
	 * @return チャンネルデータキー
	 */
	static int make(int channel, int index) {
		return ((channel & 0xffff) << 16) | (index & 0xffff);
	}

	/**
	 * チャンネルデータキー生成
	 * @param out 出力先整数値
	 * @param note ノート
	 * @return 出力先整数値の参照
	 */
	static MutableInt make(MutableInt out, BmsNote note) {
		out.set(make(note));
		return out;
	}

	/**
	 * チャンネルデータキー生成
	 * @param out 出力先整数値
	 * @param channel チャンネル番号
	 * @param index チャンネルインデックス
	 * @return 出力先整数値の参照
	 */
	static MutableInt make(MutableInt out, int channel, int index) {
		out.set(make(channel, index));
		return out;
	}

	/**
	 * チャンネル番号取得
	 * @param value チャンネルデータキー
	 * @return チャンネル番号
	 */
	static int getNumber(int value) {
		return ((value >> 16) & 0xffff);
	}

	/**
	 * チャンネルインデックス取得
	 * @param value チャンネルデータキー
	 * @return チャンネルインデックス
	 */
	static int getIndex(int value) {
		return (value & 0xffff);
	}
}
