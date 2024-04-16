package com.lmt.lib.bms;

/**
 * タイムライン要素の一つである小節データの情報を表します。
 *
 * <p>当クラスはタイムライン要素の基本情報に加え、小節データの実際の値を保有しています。
 * 小節データは原則として非nullであり、値を取得する時にnullが返ることはありません。</p>
 *
 * @see BmsTimelineElement
 */
public class BmsMeasureValue extends BmsTimelineElement {
	/** 小節データの値 */
	private Object mValue;

	/**
	 * コンストラクタ
	 * @param measure 小節番号
	 * @param channel チャンネル番号
	 * @param index チャンネルインデックス
	 * @param value 値
	 */
	BmsMeasureValue(int measure, int channel, int index, Object value) {
		super(measure, BmsSpec.TICK_MIN, channel, index);
		mValue = value;
	}

	/** {@inheritDoc} */
	@Override
	public long getValueAsLong() {
		return ((Number)mValue).longValue();
	}

	/** {@inheritDoc} */
	@Override
	public double getValueAsDouble() {
		return ((Number)mValue).doubleValue();
	}

	/** {@inheritDoc} */
	@Override
	public String getValueAsString() {
		return mValue.toString();
	}

	/** {@inheritDoc} */
	@Override
	public BmsArray getValueAsArray() {
		return (BmsArray)mValue;
	}

	/** {@inheritDoc} */
	@Override
	public Object getValueAsObject() {
		return mValue;
	}

	/** {@inheritDoc} */
	@Override
	public boolean isMeasureValueElement() {
		return true;
	}
}
