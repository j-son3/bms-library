package com.lmt.lib.bms.bemusic;

import java.util.stream.Stream;

// DELTAはまだ未実装のため、DELTAに関する当クラスはパッケージスコープとして隠ぺいしておく
/* public */ enum BeMusicDeltaCategory {
	BEGINNER(1, BeMusicRatings.DELTA_ZERO, 499),
	INTERMEDIATE(2, 500, 999),
	ADVANCED(3, 1000, 1399),
	EXPERT(4, 1400, 1549),
	PROFESSIONAL(5, 1550, BeMusicRatings.DELTA_MAX);

	private int mNumber;
	private int mMinDelta;
	private int mMaxDelta;

	private BeMusicDeltaCategory(int number, int minDelta, int maxDelta) {
		mNumber = number;
		mMinDelta = minDelta;
		mMaxDelta = maxDelta;
	}

	public final int getNumber() {
		return mNumber;
	}

	public final int getMinDelta() {
		return mMinDelta;
	}

	public final int getMaxDelta() {
		return mMaxDelta;
	}

	public final boolean within(int delta) {
		return (delta >= mMinDelta) && (delta <= mMaxDelta);
	}

	public static BeMusicDeltaCategory fromDelta(int delta) {
		var normalized = Math.min(BeMusicRatings.DELTA_MAX, Math.max(BeMusicRatings.DELTA_ZERO, delta));
		return Stream.of(values()).filter(c -> c.within(normalized)).findFirst().get();
	}
}
