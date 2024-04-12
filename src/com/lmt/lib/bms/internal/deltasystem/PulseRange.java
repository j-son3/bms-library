package com.lmt.lib.bms.internal.deltasystem;

/**
 * 一定リズムの区間(リズム範囲)を表すクラス。
 */
class PulseRange {
	/** リズム範囲番号 */
	int number;
	/** このリズムの前のリズム(先頭リズムの場合null) */
	PulseRange previousRange;
	/** このリズムの次のリズム(末尾リズムの場合null) */
	PulseRange nextRange;
	/** このリズムが属するリピート */
	PulseRepeat repeat;
	/** このリズムの最初のリズム要素 */
	RhythmElement firstElement;
	/** このリズムの最後のリズム要素 */
	RhythmElement lastElement;
	/** このリズム範囲の時間(楽曲位置が1点しかない場合0になる) */
	double rangeTime;
	/** このリズム範囲の時間が演奏時間を占める比率 */
	double rangeRate;
	/** リズムの刻み回数 */
	int pulseCount;
	/** 1刻みあたりの時間(秒単位) */
	double pulseTimeSec;
	/** 前のリズムとの間隔(秒単位) */
	double intervalSec;
	/** このリズム範囲におけるノート密度(Notes/秒) */
	double density;
	/** このリズム範囲単体の評価点 */
	double score;

	/**
	 * コンストラクタ
	 * @param number リズム範囲番号
	 */
	PulseRange(int number) {
		this.number = number;
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof PulseRange) {
			var config = RhythmConfig.getInstance();
			var value = (PulseRange)obj;
			return (pulseCount == value.pulseCount) &&
					(Math.abs(pulseTimeSec - value.pulseTimeSec) <= config.acceptableTimeDelta) &&
					(Math.abs(intervalSec - value.intervalSec) <= config.acceptableTimeDelta);
		} else {
			return false;
		}
	}

	/**
	 * 指定数分先のリズム範囲取得
	 * @param stepCount 進行数
	 * @return 指定数分先のリズム範囲。末尾リズム範囲以降を示す場合null
	 */
	PulseRange nextRangeStep(int stepCount) {
		var nextRange = this;
		for (var i = 0; (i < stepCount) && (nextRange != null); i++) {
			nextRange = nextRange.nextRange;
		}
		return nextRange;
	}

	/**
	 * 指定パターン数のリピート検出処理
	 * @param patternCount リズム範囲パターン数
	 * @return 検出したリピート範囲の最終リズム範囲。リピート未検出時はこのオブジェクトのインスタンス。
	 */
	PulseRange detectPatternRepeat(int patternCount) {
		var lastRange = this;
		var lastIndex = patternCount - 1;
		var compare = nextRangeStep(patternCount);
		while (compare != null) {
			var base = this;
			for (var i = 0; (i <= lastIndex) && (base != null) && (compare != null); i++) {
				// 同一でないリズム範囲を検出した時点で走査を停止する
				if (!base.equals(compare)) {
					compare = null;
					break;
				}
				// 指定パターン数分のリズム範囲が同一だった場合、リピートを確定させる
				if (i == lastIndex) {
					lastRange = compare;
				}
				// 次のパターンを検出する準備
				base = base.nextRange;
				compare = compare.nextRange;
			}
		}
		return lastRange;
	}
}
