package com.lmt.lib.bms.internal.deltasystem;

import com.lmt.lib.bms.bemusic.BeMusicRatingType;

public class DeltaAnalyzer extends RatingAnalyzer {
	public DeltaAnalyzer() {
		super(BeMusicRatingType.DELTA);
	}

	@Override
	protected void compute(DsContext ctx) {
		// TODO 自動生成されたメソッド・スタブ
		ctx.stat.setRating(getRatingType(), -1);
	}
}
