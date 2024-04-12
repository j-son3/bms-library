package com.lmt.lib.bms;

import static com.lmt.lib.bms.BmsAssertion.*;

/**
 * BMSチャンネルのキーを表します。
 *
 * <p>BMSチャンネルにおけるキーとは、チャンネル番号を示します。
 * 当クラスではチャンネル番号のみを持ち、プログラム内でチャンネル番号の比較を行うための処理を実装します。</p>
 *
 * <p>当クラス単体ではMapやSetなどのキーとして用いるのが一般的な想定です。それ以外での用途は想定していません。</p>
 *
 * <p>また、当クラスは{@link BmsChannel}が継承します。</p>
 *
 * @see BmsChannel
 */
public class BmsChannelKey implements Comparable<BmsChannelKey> {
	/** チャンネル番号 */
	private int mNumber;

	/**
	 * チャンネルキーオブジェクトを構築します。
	 * @param number チャンネル番号
	 * @exception IllegalArgumentException numberに登録できないチャンネル番号を指定した
	 */
	public BmsChannelKey(int number) {
		initialize(number);
	}

	/**
	 * チャンネルキーオブジェクトを構築します。
	 * @param number チャンネル番号(36進数で指定)
	 * @exception NullPointerException numberがnull
	 * @exception NumberFormatException numberの内容が36進数ではない
	 * @exception IllegalArgumentException numberに登録できないチャンネル番号を指定した
	 */
	public BmsChannelKey(String number) {
		assertArgNotNull(number, "number");
		initialize(Integer.parseInt(number, 36));
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		return Integer.hashCode(mNumber);
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		} else if (obj instanceof BmsChannelKey) {
			return mNumber == ((BmsChannelKey)obj).mNumber;
		} else if (obj instanceof Number) {
			return mNumber == ((Number)obj).intValue();
		} else {
			return false;
		}
	}

	/**
	 * チャンネル番号が分かる形式の文字列を返します。
	 * @return チャンネル番号が分かる形式の文字列
	 */
	@Override
	public String toString() {
		return String.format("{Ch:%s(%d)}", Integer.toString(mNumber, 36), mNumber);
	}

	/** {@inheritDoc} */
	@Override
	public int compareTo(BmsChannelKey o) {
		return Integer.compare(mNumber, o.mNumber);
	}

	/**
	 * チャンネル番号を取得します。
	 * @return チャンネル番号
	 */
	public final int getNumber() {
		return mNumber;
	}

	/**
	 * チャンネル番号を設定する。
	 * @param number チャンネル番号
	 * @exception IllegalArgumentException numberに登録できないチャンネル番号を指定した
	 */
	protected void setNumber(int number) {
		initialize(number);
	}

	/**
	 * 初期化処理。
	 * @param number チャンネル番号
	 * @exception IllegalArgumentException numberに登録できないチャンネル番号を指定した
	 */
	private void initialize(int number) {
		assertChannelRange(number);
		mNumber = number;
	}
}
