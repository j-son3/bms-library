package com.lmt.lib.bms;

import static com.lmt.lib.bms.BmsAssertion.*;

/**
 * タイムラインの時間軸の位置を表す「楽曲位置({@link BmsAt})」の単純実装です。
 *
 * <p>楽曲位置のみのデータが欲しい時、MapやSet等のキーとして利用したい時などの用途で利用することを想定しています。
 * 当クラスで保持する小節番号、小節の刻み位置は{@link BmsSpec}で規定されている範囲であることを強制しません。
 * 従って、各値にはマイナス値などのタイムライン上の有効範囲外の値を格納することができます。そのような値が格納された
 * 楽曲位置を各APIの入力値として使用すると、APIによっては例外をスローすることがありますので取り扱いには注意してください。</p>
 *
 * @see BmsAt
 */
public class BmsPoint implements BmsAt, Comparable<BmsAt> {
	/** タイムライン上で有効な最も小さい楽曲位置 */
	public static final BmsPoint BEGIN = new BmsPoint();
	/** タイムライン上で有効な最も大きい楽曲位置 */
	public static final BmsPoint END = new BmsPoint(BmsSpec.MEASURE_MAX_COUNT, BmsSpec.TICK_MIN);

	/** 小節番号 */
	private int mMeasure;
	/** 刻み位置 */
	private double mTick;

	/**
	 * 新しい楽曲位置オブジェクトを構築します。
	 */
	public BmsPoint() {
		mMeasure = BmsSpec.MEASURE_MIN;
		mTick = BmsSpec.TICK_MIN;
	}

	/**
	 * 指定した楽曲位置と同じ新しい楽曲位置オブジェクトを構築します。
	 * @param at 楽曲位置
	 * @exception NullPointerException atがnull
	 */
	public BmsPoint(BmsAt at) {
		assertArgNotNull(at, "at");
		mMeasure = at.getMeasure();
		mTick = at.getTick();
	}

	/**
	 * 指定した小節番号、刻み位置で新しい楽曲位置オブジェクトを構築します。
	 * @param measure 小節番号
	 * @param tick 刻み位置
	 */
	public BmsPoint(int measure, double tick) {
		mMeasure = measure;
		mTick = tick;
	}

	/**
	 * 楽曲位置の比較を行います。
	 * <p>比較対象オブジェクトを{@link BmsAt}にキャストできない場合、当メソッドはfalseを返します。</p>
	 * @param obj 比較対象オブジェクト
	 * @return このオブジェクトと比較対象オブジェクトの楽曲位置が同一の場合true
	 */
	@Override
	public boolean equals(Object obj) {
		return (obj instanceof BmsAt) ? BmsAt.equals(this, (BmsAt)obj) : false;
	}

	/**
	 * 楽曲位置のハッシュコード値を返します。
	 * @return 楽曲位置のハッシュコード値
	 */
	@Override
	public int hashCode() {
		return BmsAt.hashCode(this);
	}

	/**
	 * 楽曲位置の文字列表現を返します。
	 * @return 楽曲位置の文字列表現
	 */
	@Override
	public String toString() {
		return String.format("{M=%d, T=%.16g}", mMeasure, mTick);
	}

	/**
	 * この楽曲位置オブジェクトと指定された楽曲位置を比較します。
	 * <p>比較処理の詳細については{@link BmsAt#compare(BmsAt, BmsAt)}を参照してください。</p>
	 * @param at 比較対象楽曲位置
	 * @return 楽曲位置の比較結果
	 * @exception NullPointerException atがnull
	 */
	@Override
	public int compareTo(BmsAt at) {
		return BmsAt.compare(this, at);
	}

	/** {@inheritDoc} */
	@Override
	public int getMeasure() {
		return mMeasure;
	}

	/**
	 * 小節番号を設定します。
	 * @param measure 小節番号
	 */
	final void setMeasure(int measure) {
		mMeasure = measure;
	}

	/** {@inheritDoc} */
	@Override
	public double getTick() {
		return mTick;
	}

	/**
	 * 小節の刻み位置を設定します。
	 * @param tick 小節の刻み位置
	 */
	final void setTick(double tick) {
		mTick = tick;
	}

	/**
	 * 小節番号、小節の刻み位置設定
	 * @param measure 小節番号
	 * @param tick 小節の刻み位置
	 */
	final void set(int measure, double tick) {
		mMeasure = measure;
		mTick = tick;
	}

	/**
	 * 指定された楽曲位置と同じ新しい楽曲位置オブジェクトを構築します。
	 * @param at 楽曲位置
	 * @exception NullPointerException atがnull
	 * @return 楽曲位置オブジェクト
	 */
	public static BmsPoint of(BmsAt at) {
		return new BmsPoint(at);
	}

	/**
	 * 指定された小節番号、小節の刻み位置で新しい楽曲位置オブジェクトを構築します。
	 * @param measure 小節番号
	 * @param tick 小節の刻み位置
	 * @return 楽曲位置オブジェクト
	 */
	public static BmsPoint of(int measure, double tick) {
		return new BmsPoint(measure, tick);
	}
}
