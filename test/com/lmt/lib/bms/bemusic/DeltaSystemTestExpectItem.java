package com.lmt.lib.bms.bemusic;

public class DeltaSystemTestExpectItem {
	String id;
	String title;
	double[] ratings = new double[BeMusicRatingType.COUNT];

	DeltaSystemTestExpectItem(BeMusicHeader header, BeMusicStatistics stat) {
		this.id = header.getComment();
		this.title = String.format("%s %s", header.getTitle(), header.getSubTitle()).strip();
		for (var ratingType : BeMusicRatingType.values()) {
			var ratingRaw = stat.getRating(ratingType);
			var ratingActual = ratingType.toValue(ratingRaw);
			this.ratings[ratingType.getIndex()] = ratingActual;
		}
	}

	DeltaSystemTestExpectItem(String id, double delta, double complex, double power, double rhythm, double scratch,
			double holding, double gimmick) {
		this.id = id;
		this.title = "";
		this.ratings[BeMusicRatingType.DELTA.getIndex()] = delta;
		this.ratings[BeMusicRatingType.COMPLEX.getIndex()] = complex;
		this.ratings[BeMusicRatingType.POWER.getIndex()] = power;
		this.ratings[BeMusicRatingType.RHYTHM.getIndex()] = rhythm;
		this.ratings[BeMusicRatingType.SCRATCH.getIndex()] = scratch;
		this.ratings[BeMusicRatingType.HOLDING.getIndex()] = holding;
		this.ratings[BeMusicRatingType.GIMMICK.getIndex()] = gimmick;
	}
}
