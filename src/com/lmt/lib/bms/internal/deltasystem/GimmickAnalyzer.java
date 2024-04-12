package com.lmt.lib.bms.internal.deltasystem;

import com.lmt.lib.bms.bemusic.BeMusicRatingType;

public class GimmickAnalyzer extends RatingAnalyzer {
	public GimmickAnalyzer() {
		super(BeMusicRatingType.GIMMICK);
	}

	@Override
	protected void compute(DsContext cxt) {
		// TODO 自動生成されたメソッド・スタブ
		cxt.stat.setRating(getRatingType(), -1);
	}
}
