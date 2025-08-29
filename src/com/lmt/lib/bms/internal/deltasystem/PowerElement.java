package com.lmt.lib.bms.internal.deltasystem;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.lmt.lib.bms.bemusic.BeMusicDevice;
import com.lmt.lib.bms.bemusic.BeMusicLane;
import com.lmt.lib.bms.bemusic.BeMusicPoint;

/**
 * 譜面傾向「POWER」の分析データクラス
 */
class PowerElement extends RatingElement {
	/** 手前の楽曲位置が存在しないことを示す値 */
	static final int NO_PREVIOUS = -1;

	/** インデックス：当該ノートにおいて、同一入力デバイス上で一つ前のノートからの時間 */
	static final int I_NOTE_TIME = 0;
	/** インデックス：担当指にかかる抵抗値 */
	static final int I_RESIST = I_NOTE_TIME + BeMusicDevice.COUNT_PER_LANE;
	/** インデックス：ノート評価点 */
	static final int I_NOTE_SCORE = I_RESIST + BeMusicDevice.COUNT_PER_LANE;
	/** インデックス：連続操作評価点 */
	static final int I_RAPID_BEAT_SCORE = I_NOTE_SCORE + BeMusicDevice.COUNT_PER_LANE;
	/** インデックス：評価対象となった連続操作の数 */
	static final int I_RAPID_BEAT_COUNT = I_RAPID_BEAT_SCORE + BeMusicDevice.COUNT_PER_LANE;
	/** インデックス：譜面密度評価点 */
	static final int I_DENSITY_SCORE = I_RAPID_BEAT_COUNT + BeMusicDevice.COUNT_PER_LANE;
	/** 1楽曲位置におけるデータの総数 */
	static final int DATA_COUNT = I_DENSITY_SCORE + 1;

	/** レーンごとの要素データ */
	private Data[] mData = new Data[2];
	/** 操作対象ノートが存在するかどうか(主レーン) */
	private boolean mHasMovementPrimary;
	/** 操作対象ノートが存在するかどうか(副レーン) */
	private boolean mHasMovementSecondary;

	/** 要素データクラス */
	static class Data {
		/** 対象レーン */
		private BeMusicLane mLane;
		/** 分析データ格納領域 */
		float[] data = new float[DATA_COUNT];
		/** 入力デバイスごとの、ノートが存在する1つ前の楽曲位置インデックス */
		int[] previous = new int[BeMusicDevice.COUNT_PER_LANE];
		/** この楽曲位置で採用する運指 */
		Fingering fingering = Fingering.SP_DEFAULT;
		/** 楽曲位置評価点 */
		double pointScore = 0.0;

		/**
		 * コンストラクタ
		 * @param lane レーン
		 */
		Data(BeMusicLane lane) {
			mLane = lane;
			Arrays.fill(this.previous, NO_PREVIOUS);
		}

		/**
		 * 前ノートからの時間取得
		 * @param device 入力デバイス
		 * @return 前ノートからの時間
		 */
		final double getNoteTime(BeMusicDevice device) {
			return this.data[deviceDataIndex(I_NOTE_TIME, device)];
		}

		/**
		 * 前ノートからの時間設定
		 * @param device 入力デバイス
		 * @param time 前ノートからの時間
		 */
		final void setNoteTime(BeMusicDevice device, double time) {
			this.data[deviceDataIndex(I_NOTE_TIME, device)] = (float)time;
		}

		/**
		 * 連動指の抵抗値取得
		 * @param device 入力デバイス
		 * @return 連動指の抵抗値
		 */
		final double getResist(BeMusicDevice device) {
			return this.data[deviceDataIndex(I_RESIST, device)];
		}

		/**
		 * 連動指の抵抗値設定
		 * @param device 入力デバイス
		 * @param resist 連動指の抵抗値
		 */
		final void setResist(BeMusicDevice device, double resist) {
			this.data[deviceDataIndex(I_RESIST, device)] = (float)resist;
		}

		/**
		 * ノート評価点取得
		 * @param device 入力デバイス
		 * @return ノート評価点
		 */
		final double getNoteScore(BeMusicDevice device) {
			return this.data[deviceDataIndex(I_NOTE_SCORE, device)];
		}

		/**
		 * ノート評価点設定
		 * @param device 入力デバイス
		 * @param score ノート評価点
		 */
		final void setNoteScore(BeMusicDevice device, double score) {
			this.data[deviceDataIndex(I_NOTE_SCORE, device)] += (float)score;
		}

		/**
		 * 連続操作評価点取得
		 * @param device 入力デバイス
		 * @return 連続操作評価点
		 */
		final double getRapidBeatScore(BeMusicDevice device) {
			return this.data[deviceDataIndex(I_RAPID_BEAT_SCORE, device)];
		}

		/**
		 * 評価対象となった連続操作の数取得
		 * @param device 入力デバイス
		 * @return 評価対象となった連続操作の数
		 */
		final int getRapidBeatCount(BeMusicDevice device) {
			return (int)this.data[deviceDataIndex(I_RAPID_BEAT_COUNT, device)];
		}

		/**
		 * 連続操作評価点追加
		 * @param device 入力デバイス
		 * @param score 連続操作評価点
		 */
		final void putRapidBeatScore(BeMusicDevice device, double score) {
			this.data[deviceDataIndex(I_RAPID_BEAT_SCORE, device)] += (float)score;
			this.data[deviceDataIndex(I_RAPID_BEAT_COUNT, device)]++;
		}

		/**
		 * 譜面密度評価点取得
		 * @return 譜面密度評価点
		 */
		final double getDensityScore() {
			return this.data[I_DENSITY_SCORE];
		}

		/**
		 * 譜面密度評価点設定
		 * @param score 譜面密度評価点
		 */
		final void setDensityScore(double score) {
			this.data[I_DENSITY_SCORE] = (float)score;
		}

		/**
		 * 指定入力デバイスでこの楽曲位置の1つ手前で操作のある楽曲位置取得
		 * @param device 入力デバイス
		 * @param elements 分析データリスト
		 * @return 該当する楽曲位置。なければnull
		 */
		final PowerElement getPrevious(BeMusicDevice device, List<PowerElement> elements) {
			var previous = this.previous[device.getSwitchNumber()];
			return (previous < 0) ? null : elements.get(previous);
		}

		/**
		 * 指定入力デバイスでこの楽曲位置の1つ手前で操作のある楽曲位置設定
		 * @param device 入力デバイス
		 * @param previous 手前の楽曲位置を示すインデックス値
		 */
		final void setPrevious(BeMusicDevice device, int previous) {
			this.previous[device.getSwitchNumber()] = previous;
		}

		/**
		 * この楽曲位置で採用する運指取得
		 * @return この楽曲位置で採用する運指
		 */
		final Fingering getFingering() {
			return this.fingering;
		}

		/**
		 * この楽曲位置で採用する運指設定
		 * @param fingering この楽曲位置で採用する運指
		 */
		final void setFingering(Fingering fingering) {
			this.fingering = fingering;
		}

		/**
		 * 担当手評価点計算
		 * @param hand 手
		 * @return 担当手評価点
		 */
		final double computeHandScore(Hand hand) {
			// 指最大のスコアを軸にして、他指の合計の数%を最大スコアに加算する(同時押しの影響度を緩和する)
			// ※対象レーンの異なる入力デバイスは評価対象外とする
			var scoreMax = 0.0;
			var scoreSum = 0.0;
			var devs = this.fingering.getDevices(hand);
			var count = devs.size();
			for (var i = 0; i < count; i++) {
				var dev = devs.get(i);
				if (dev.getLane() == mLane) {
					var scoreDev = getNoteScore(dev) + getRapidBeatScore(dev);
					scoreMax = Math.max(scoreMax, scoreDev);
					scoreSum += scoreDev;
				}
			}
			return scoreMax + ((scoreSum - scoreMax) * PowerConfig.getInstance().influenceOtherNoteScore);
		}

		/**
		 * 入力デバイスごとのデータのインデックス値取得
		 * @param base ベースインデックス
		 * @param device 入力デバイス
		 * @return 入力デバイスのデータインデックス値
		 */
		private int deviceDataIndex(int base, BeMusicDevice device) {
			return base + device.getSwitchNumber();
		}
	}

	/**
	 * コンストラクタ
	 * @param ctx コンテキスト
	 * @param point 楽曲位置情報
	 */
	PowerElement(DsContext ctx, BeMusicPoint point) {
		super(ctx, point);
		if (ctx.layout.isFlip()) {
			mHasMovementPrimary = point.hasMovementNote(BeMusicLane.SECONDARY);
			mHasMovementSecondary = point.hasMovementNote(BeMusicLane.PRIMARY);
		} else {
			mHasMovementPrimary = point.hasMovementNote(BeMusicLane.PRIMARY);
			mHasMovementSecondary = point.hasMovementNote(BeMusicLane.SECONDARY);
		}
	}

	/**
	 * 要素データ取得
	 * @param lane レーン
	 * @return 要素データ
	 */
	Data getData(BeMusicLane lane) {
		return mData[lane.getIndex()];
	}

	/**
	 * 要素データ設定
	 * @param lane レーン
	 * @param data 要素データ
	 */
	void setData(BeMusicLane lane, Data data) {
		mData[lane.getIndex()] = data;
	}

	/**
	 * 操作対象ノート有無取得
	 * @param lane レーン
	 * @return 操作対象ノートがあればtrue
	 */
	boolean hasMovementNote(BeMusicLane lane) {
		return (lane == BeMusicLane.PRIMARY) ? mHasMovementPrimary : mHasMovementSecondary;
	}

	/** {@inheritDoc} */
	@Override
	protected void printSpData(int pos) {
		var data = getData(BeMusicLane.PRIMARY);
		Ds.debug("   |%c|%.3f|%s|%.3f|%s|L=%.4f, R=%.4f, S=%.4f",
				data.getFingering().getChar(),
				getTimeDelta(),
				makeNotesString(BeMusicLane.PRIMARY),
				data.getDensityScore(),
				makeLaneScoreString(BeMusicLane.PRIMARY),
				data.computeHandScore(Hand.LEFT),
				data.computeHandScore(Hand.RIGHT),
				data.pointScore);
	}

	/** {@inheritDoc} */
	@Override
	protected void printDpData(int pos) {
		var dp = getData(BeMusicLane.PRIMARY);
		var ds = getData(BeMusicLane.SECONDARY);
		Ds.debug("   |%.3f|%s| |%s|%c|%.3f|%s|L=%.4f, R=%.4f, S=%.4f|%c|%.3f|%s|L=%.4f, R=%.4f, S=%.4f|",
				getTimeDelta(),
				makeNotesString(BeMusicLane.PRIMARY),
				makeNotesString(BeMusicLane.SECONDARY),
				dp.getFingering().getChar(),
				dp.getDensityScore(),
				makeLaneScoreString(BeMusicLane.PRIMARY),
				dp.computeHandScore(Hand.LEFT),
				dp.computeHandScore(Hand.RIGHT),
				dp.pointScore,
				ds.getFingering().getChar(),
				ds.getDensityScore(),
				makeLaneScoreString(BeMusicLane.SECONDARY),
				ds.computeHandScore(Hand.LEFT),
				ds.computeHandScore(Hand.RIGHT),
				ds.pointScore);
	}

	/** {@inheritDoc} */
	@Override
	protected void printSpHeader() {
		Ds.debug("---+-+-----+----+----+----+----+----+----+----+----+-----+--------------+--------------+--------------+--------------+--------------+--------------+--------------+--------------+----------------------------");
		Ds.debug("M  |F|DELTA|SCR1|SW1 |SW2 |SW3 |SW4 |SW5 |SW6 |SW7 |DENS |SCR           |SW1           |SW2           |SW3           |SW4           |SW5           |SW6           |SW7           |Score");
		Ds.debug("---+-+-----+----+----+----+----+----+----+----+----+-----+--------------+--------------+--------------+--------------+--------------+--------------+--------------+--------------+----------------------------");
	}

	/** {@inheritDoc} */
	@Override
	protected void printDpHeader() {
		Ds.debug("---+-----+-------------------------------+-+-------------------------------+------------------------------------------------------------------------------------------------------------------------------------------------------------+------------------------------------------------------------------------------------------------------------------------------------------------------------+");
		Ds.debug("   |     |             LEFT              | |              RIGHT            |                                                                                                                                                            |                                                                                                                                                            |");
		Ds.debug("   |     +---+---+---+---+---+---+---+---+ +---+---+---+---+---+---+---+---+-+-----+--------------+--------------+--------------+--------------+--------------+--------------+--------------+--------------+----------------------------+-+-----+--------------+--------------+--------------+--------------+--------------+--------------+--------------+--------------+----------------------------+");
		Ds.debug("M  |DELTA|SC1|SW1|SW2|SW3|SW4|SW5|SW6|SW7| |SW1|SW2|SW3|SW4|SW5|SW6|SW7|SC2|F|DENS |SCR1          |SW1           |SW2           |SW3           |SW4           |SW5           |SW6           |SW7           |Score                       |F|DENS |SW1           |SW2           |SW3           |SW4           |SW5           |SW6           |SW7           |SCR2          |Score                       |");
		Ds.debug("---+-----+---+---+---+---+---+---+---+---+-+---+---+---+---+---+---+---+---+-+-----+--------------+--------------+--------------+--------------+--------------+--------------+--------------+--------------+----------------------------+-+-----+--------------+--------------+--------------+--------------+--------------+--------------+--------------+--------------+----------------------------+");
	}

	/** {@inheritDoc} */
	@Override
	protected void printSpMeasure(int m) {
		Ds.debug("%3d|-|-----|---------------------------------------|-----|--------------|--------------|--------------|--------------|--------------|--------------|--------------|--------------|----------------------------", m);
	}

	/** {@inheritDoc} */
	@Override
	protected void printDpMeasure(int m) {
		Ds.debug("%3d|-----|-------------------------------| |-------------------------------|-+-----+--------------+--------------+--------------+--------------+--------------+--------------+--------------+--------------+----------------------------+-+-----+--------------+--------------+--------------+--------------+--------------+--------------+--------------+--------------+----------------------------|", m);
	}

	/**
	 * レーンごとのデバッグ出力内容生成
	 * @param lane レーン
	 * @return デバッグ出力内容
	 */
	private String makeLaneScoreString(BeMusicLane lane) {
		return BeMusicDevice.orderedByDpList(lane).stream()
				.map(d -> makeDeviceValueString(lane, d))
				.collect(Collectors.joining("|"));
	}

	/**
	 * 入力デバイスごとのデバッグ出力内容生成
	 * @param lane レーン
	 * @param device 入力デバイス
	 * @return デバッグ出力内容
	 */
	private String makeDeviceValueString(BeMusicLane lane, BeMusicDevice device) {
		var data = getData(lane);
		var scoreNote = data.getNoteScore(device);
		var scoreRapidBeat = data.getRapidBeatScore(device);
		var countRapidBeat = data.getRapidBeatCount(device);
		if ((scoreNote + scoreRapidBeat) <= 0.0) {
			// ノート評価点・連続操作評価点がいずれも存在しない場合は空欄とする
			return "              ";
		} else if (scoreRapidBeat <= 0.0) {
			// 連続操作評価点が存在しない場合は連続操作部分を仮文字とする
			return String.format("%.3f:********", scoreNote);
		} else {
			// 上記以外の場合は全情報を出力する
			return String.format("%.3f:%.3f-%02d", scoreNote, scoreRapidBeat, countRapidBeat);
		}
	}
}