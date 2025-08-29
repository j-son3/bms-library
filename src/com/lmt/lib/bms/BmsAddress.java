package com.lmt.lib.bms;

import static com.lmt.lib.bms.internal.Assertion.*;

import java.util.Objects;

/**
 * タイムラインにおける完全な位置を表すクラスです。
 *
 * <p>楽曲位置({@link BmsAt})、およびCHX({@link BmsChx})の両方を解決すると、タイムライン上での完全な位置を表します。
 * BMSライブラリでは、この位置のことをタイムラインの「アドレス」と呼称します。</p>
 *
 * <p>当クラスはアドレスを表現するために必要な最低限の情報を保有し、必要な機能を提供します。</p>
 *
 * <p>このクラスの情報は一度インスタンスを生成すると以後の情報の変更はできません。異なるアドレスを表したい場合には
 * 新しく別のインスタンスを生成する必要があります。</p>
 *
 * @see BmsAt
 * @see BmsChx
 * @since 0.1.0
 */
public class BmsAddress implements BmsAt, BmsChx, Comparable<BmsAddress> {
	/** 小節番号 */
	private int mMeasure;
	/** 小節の刻み位置 */
	private double mTick;
	/** CHX値 */
	private int mChx;

	/**
	 * 新しいアドレスオブジェクトを構築します。
	 */
	public BmsAddress() {
		mMeasure = BmsSpec.MEASURE_MIN;
		mTick = BmsSpec.TICK_MIN;
		mChx = BmsChx.toInt(BmsSpec.CHANNEL_MIN, BmsSpec.CHINDEX_MIN);
	}

	/**
	 * 指定されたアドレスと同じ新しいアドレスオブジェクトを構築します。
	 * @param adr アドレス
	 * @throws NullPointerException adrがnull
	 */
	public BmsAddress(BmsAddress adr) {
		assertArgNotNull(adr, "adr");
		mMeasure = adr.mMeasure;
		mTick = adr.mTick;
		mChx = adr.mChx;
	}

	/**
	 * 指定された楽曲位置、CHXで新しいアドレスオブジェクトを構築します。
	 * @param at 楽曲位置
	 * @param chx CHX
	 * @throws NullPointerException atまたはchxがnull
	 */
	public BmsAddress(BmsAt at, BmsChx chx) {
		assertArgNotNull(at, "at");
		assertArgNotNull(chx, "chx");
		mMeasure = at.getMeasure();
		mTick = at.getTick();
		mChx = BmsChx.toInt(chx);
	}

	/**
	 * 指定された楽曲位置、CHXで新しいアドレスオブジェクトを構築します。
	 * @param measure 小節番号
	 * @param tick 小節の刻み位置
	 * @param channel チャンネル番号
	 * @param index チャンネルインデックス
	 */
	public BmsAddress(int measure, double tick, int channel, int index) {
		mMeasure = measure;
		mTick = tick;
		mChx = BmsChx.toInt(channel, index);
	}

	/** {@inheritDoc} */
	@Override
	public int getMeasure() {
		return mMeasure;
	}

	/**
	 * 小節番号設定
	 * @param measure 小節番号
	 */
	void setMeasure(int measure) {
		mMeasure = measure;
	}

	/** {@inheritDoc} */
	@Override
	public double getTick() {
		return mTick;
	}

	/**
	 * 小節の刻み位置設定
	 * @param tick 小節の刻み位置
	 */
	void setTick(double tick) {
		mTick = tick;
	}

	/** {@inheritDoc} */
	@Override
	public int getChannel() {
		return BmsChx.toChannel(mChx);
	}

	/** {@inheritDoc} */
	@Override
	public int getIndex() {
		return BmsChx.toIndex(mChx);
	}

	/**
	 * CHX設定
	 * @param channel チャンネル番号
	 * @param index チャンネルインデックス
	 */
	void setChx(int channel, int index) {
		mChx = BmsChx.toInt(channel, index);
	}

	/**
	 * CHX設定
	 * @param chx CHX値
	 */
	void setChx(int chx) {
		mChx = chx;
	}

	/**
	 * アドレスの比較を行います。
	 * <p>比較対象オブジェクトを{@link BmsAddress}にキャストできない場合、当メソッドはfalseを返します。</p>
	 * @param obj 比較対象オブジェクト
	 * @return このオブジェクトと比較対象オブジェクトのアドレスが同一の場合true
	 */
	@Override
	public boolean equals(Object obj) {
		return (obj instanceof BmsAddress) ? equals(this, (BmsAddress)obj) : false;
	}

	/**
	 * ハッシュコード値を生成します。
	 * @return ハッシュコード値
	 */
	@Override
	public int hashCode() {
		return hashCode(this);
	}

	/**
	 * アドレスの文字列表現を返します。
	 * @return アドレスの文字列表現
	 */
	@Override
	public String toString() {
		int ch = BmsChx.toChannel(mChx);
		int idx = BmsChx.toIndex(mChx);
		return String.format("{At={M=%d, T=%.16g}, Chx={C=%d(%s), I=%d}}", mMeasure, mTick, ch, BmsInt.to36s(ch), idx);
	}

	/**
	 * このアドレスオブジェクトと指定されたアドレスを比較します。
	 * <p>比較処理の詳細については{@link #compare(BmsAddress, BmsAddress)}を参照してください。</p>
	 * @param adr 比較対象アドレス
	 * @return アドレスの比較結果
	 * @throws NullPointerException adrがnull
	 */
	@Override
	public int compareTo(BmsAddress adr) {
		return compare(this, adr);
	}

	/**
	 * 2つのアドレスを比較し、同一アドレスかどうかを判定します。
	 * @param adr1 アドレス1
	 * @param adr2 アドレス2
	 * @return 2つのアドレスが同一の場合true
	 * @throws NullPointerException adr1またはadr2がnull
	 */
	public static boolean equals(BmsAddress adr1, BmsAddress adr2) {
		return BmsAt.equals(adr1, adr2) && BmsChx.equals(adr1, adr2);
	}

	/**
	 * 2つのアドレスを比較します。
	 * <p>アドレスの比較では、最初に楽曲位置の比較を行います。楽曲位置が進行するほど大きい値として評価します。
	 * 楽曲位置が同一の場合に、CHXの比較を行います。楽曲位置、CHXが全て同じ場合は同一アドレスと評価します。</p>
	 * @param adr1 アドレス1
	 * @param adr2 アドレス2
	 * @return アドレス1 == アドレス2は0、アドレス1 &gt; アドレス2は正の値、アドレス1 &lt; アドレス2は負の値
	 * @throws NullPointerException adr1またはadr2がnull
	 */
	public static int compare(BmsAddress adr1, BmsAddress adr2) {
		var comp = BmsAt.compare(adr1, adr2);
		comp = (comp == 0) ? BmsChx.compare(adr1, adr2) : comp;
		return comp;
	}

	/**
	 * 指定されたアドレスのハッシュコード値を生成します。
	 * @param adr アドレス
	 * @return ハッシュコード値
	 * @throws NullPointerException adrがnull
	 */
	public static int hashCode(BmsAddress adr) {
		return Objects.hash(adr.getMeasure(), adr.getTick(), BmsChx.toInt(adr));
	}

	/**
	 * 指定されたアドレスと同じ新しいアドレスオブジェクトを構築します。
	 * @param adr アドレス
	 * @throws NullPointerException adrがnull
	 * @return アドレスオブジェクト
	 */
	public static BmsAddress of(BmsAddress adr) {
		return new BmsAddress(adr);
	}

	/**
	 * 指定された楽曲位置、CHXの新しいアドレスオブジェクトを構築します。
	 * @param at 楽曲位置
	 * @param chx CHX
	 * @throws NullPointerException atまたはchxがnull
	 * @return アドレスオブジェクト
	 */
	public static BmsAddress of(BmsAt at, BmsChx chx) {
		return new BmsAddress(at, chx);
	}

	/**
	 * 指定された楽曲位置、CHXの新しいアドレスオブジェクトを構築します。
	 * @param measure 小節番号
	 * @param tick 小節の刻み位置
	 * @param channel チャンネル番号
	 * @param index チャンネルインデックス
	 * @return アドレスオブジェクト
	 */
	public static BmsAddress of(int measure, double tick, int channel, int index) {
		return new BmsAddress(measure, tick, channel, index);
	}
}
