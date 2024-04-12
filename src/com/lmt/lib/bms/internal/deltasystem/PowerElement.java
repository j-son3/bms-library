package com.lmt.lib.bms.internal.deltasystem;

import java.util.Arrays;
import java.util.List;

import com.lmt.lib.bms.bemusic.BeMusicDevice;
import com.lmt.lib.bms.bemusic.BeMusicPoint;

/**
 * 譜面傾向「POWER」の分析データクラス
 */
class PowerElement extends RatingElement {
	/** 手前の楽曲位置が存在しないことを示す値 */
	static final int NO_PREVIOUS = -1;

	/** インデックス：当該ノートにおいて、同一入力デバイス上で一つ前のノートからの時間 */
	private static final int I_NOTE_TIME = 0;
	/** インデックス：担当指にかかる抵抗値 */
	private static final int I_RESIST = I_NOTE_TIME + BeMusicDevice.COUNT;
	/** インデックス：ノート評価点 */
	private static final int I_NOTE_SCORE = I_RESIST + BeMusicDevice.COUNT;
	/** インデックス：連続操作評価点 */
	private static final int I_RAPID_BEAT_SCORE = I_NOTE_SCORE + BeMusicDevice.COUNT;
	/** インデックス：評価対象となった連続操作の数 */
	private static final int I_RAPID_BEAT_COUNT = I_RAPID_BEAT_SCORE + BeMusicDevice.COUNT;
	/** インデックス：譜面密度評価点 */
	private static final int I_DENSITY_SCORE = I_RAPID_BEAT_COUNT + BeMusicDevice.COUNT;
	/** 1楽曲位置におけるデータの総数 */
	private static final int DATA_COUNT = I_DENSITY_SCORE + 1;

	/** 分析データ格納領域 */
	private float[] mData = new float[DATA_COUNT];
	/** 入力デバイスごとの、ノートが存在する1つ前の楽曲位置インデックス */
	private int[] mPrevious = new int[BeMusicDevice.COUNT];
	/** この楽曲位置で採用する運指 */
	private Fingering mFingering = Fingering.SP_DEFAULT;

	/** 楽曲位置評価点 */
	private double mPointScore = 0.0;

	/**
	 * コンストラクタ
	 * @param point 楽曲位置情報
	 */
	PowerElement(BeMusicPoint point) {
		super(point);
		Arrays.fill(mPrevious, NO_PREVIOUS);
	}

	/**
	 * 前ノートからの時間取得
	 * @param device 入力デバイス
	 * @return 前ノートからの時間
	 */
	final double getNoteTime(BeMusicDevice device) {
		return mData[I_NOTE_TIME + device.getIndex()];
	}

	/**
	 * 前ノートからの時間設定
	 * @param device 入力デバイス
	 * @param time 前ノートからの時間
	 */
	final void setNoteTime(BeMusicDevice device, double time) {
		mData[I_NOTE_TIME + device.getIndex()] = (float)time;
	}

	/**
	 * 連動指の抵抗値取得
	 * @param device 入力デバイス
	 * @return 連動指の抵抗値
	 */
	final double getResist(BeMusicDevice device) {
		return mData[I_RESIST + device.getIndex()];
	}

	/**
	 * 連動指の抵抗値設定
	 * @param device 入力デバイス
	 * @param resist 連動指の抵抗値
	 */
	final void setResist(BeMusicDevice device, double resist) {
		mData[I_RESIST + device.getIndex()] = (float)resist;
	}

	/**
	 * ノート評価点取得
	 * @param device 入力デバイス
	 * @return ノート評価点
	 */
	final double getNoteScore(BeMusicDevice device) {
		return mData[I_NOTE_SCORE + device.getIndex()];
	}

	/**
	 * ノート評価点設定
	 * @param device 入力デバイス
	 * @param score ノート評価点
	 */
	final void setNoteScore(BeMusicDevice device, double score) {
		mData[I_NOTE_SCORE + device.getIndex()] += (float)score;
	}

	/**
	 * 連続操作評価点取得
	 * @param device 入力デバイス
	 * @return 連続操作評価点
	 */
	final double getRapidBeatScore(BeMusicDevice device) {
		return mData[I_RAPID_BEAT_SCORE + device.getIndex()];
	}

	/**
	 * 評価対象となった連続操作の数取得
	 * @param device 入力デバイス
	 * @return 評価対象となった連続操作の数
	 */
	final int getRapidBeatCount(BeMusicDevice device) {
		return (int)mData[I_RAPID_BEAT_COUNT + device.getIndex()];
	}

	/**
	 * 連続操作評価点追加
	 * @param device 入力デバイス
	 * @param score 連続操作評価点
	 */
	final void putRapidBeatScore(BeMusicDevice device, double score) {
		var i = device.getIndex();
		mData[I_RAPID_BEAT_SCORE + i] += (float)score;
		mData[I_RAPID_BEAT_COUNT + i]++;
	}

	/**
	 * 譜面密度評価点取得
	 * @return 譜面密度評価点
	 */
	final double getDensityScore() {
		return mData[I_DENSITY_SCORE];
	}

	/**
	 * 譜面密度評価点設定
	 * @param score 譜面密度評価点
	 */
	final void setDensityScore(double score) {
		mData[I_DENSITY_SCORE] = (float)score;
	}

	/**
	 * 指定入力デバイスでこの楽曲位置の1つ手前で操作のある楽曲位置取得
	 * @param device 入力デバイス
	 * @param elements 分析データリスト
	 * @return 該当する楽曲位置。なければnull
	 */
	final PowerElement getPrevious(BeMusicDevice device, List<PowerElement> elements) {
		var previous = mPrevious[device.getIndex()];
		return (previous < 0) ? null : elements.get(previous);
	}

	/**
	 * 指定入力デバイスでこの楽曲位置の1つ手前で操作のある楽曲位置設定
	 * @param device 入力デバイス
	 * @param previous 手前の楽曲位置を示すインデックス値
	 */
	final void setPrevious(BeMusicDevice device, int previous) {
		mPrevious[device.getIndex()] = previous;
	}

	/**
	 * この楽曲位置で採用する運指取得
	 * @return この楽曲位置で採用する運指
	 */
	final Fingering getFingering() {
		return mFingering;
	}

	/**
	 * この楽曲位置で採用する運指設定
	 * @param fingering この楽曲位置で採用する運指
	 */
	final void setFingering(Fingering fingering) {
		mFingering = fingering;
	}

	/**
	 * 楽曲位置評価点取得
	 * @return 楽曲位置評価点
	 */
	final double getPointScore() {
		return mPointScore;
	}

	/**
	 * 楽曲位置評価点設定
	 * @param pointScore 楽曲位置評価点
	 */
	final void setPointScore(double pointScore) {
		mPointScore = pointScore;
	}

	/**
	 * 担当手評価点計算
	 * @param hand 手
	 * @return 担当手評価点
	 */
	final double computeHandScore(Hand hand) {
		// 指最大のスコアを軸にして、他指の合計の数%を最大スコアに加算する(同時押しの影響度を緩和する)
		var scoreMax = 0.0;
		var scoreSum = 0.0;
		for (var dev : mFingering.getDevices(hand)) {
			var scoreDev = getNoteScore(dev) + getRapidBeatScore(dev);
			scoreMax = Math.max(scoreMax, scoreDev);
			scoreSum += scoreDev;
		}
		return scoreMax + ((scoreSum - scoreMax) * PowerConfig.getInstance().influenceOtherNoteScore);
	}

	/** {@inheritDoc} */
	@Override
	protected void printHeader() {
		Ds.debug("---+-+-----+----+----+----+----+----+----+----+----+-----+--------------+--------------+--------------+--------------+--------------+--------------+--------------+--------------+----------------------------");
		Ds.debug("M  |F|DELTA|SCR1|SW1 |SW2 |SW3 |SW4 |SW5 |SW6 |SW7 |DENS |SCR           |SW1           |SW2           |SW3           |SW4           |SW5           |SW6           |SW7           |Score");
		Ds.debug("---+-+-----+----+----+----+----+----+----+----+----+-----+--------------+--------------+--------------+--------------+--------------+--------------+--------------+--------------+----------------------------");
	}

	/** {@inheritDoc} */
	@Override
	protected void printMeasure() {
		var m = getPoint().getMeasure();
		Ds.debug("%3d|-|-----|---------------------------------------|-----|--------------|--------------|--------------|--------------|--------------|--------------|--------------|--------------|----------------------------", m);
	}

	/** {@inheritDoc} */
	@Override
	protected void printData() {
		Ds.debug("   |%c|%.3f|%s|%.3f|%s|%s|%s|%s|%s|%s|%s|%s|L=%.4f, R=%.4f, S=%.4f",
				getFingering().getChar(),
				getTimeDelta(),
				makeNotesString(),
				getDensityScore(),
				makeDeviceValueString(BeMusicDevice.SCRATCH1), makeDeviceValueString(BeMusicDevice.SWITCH11),
				makeDeviceValueString(BeMusicDevice.SWITCH12), makeDeviceValueString(BeMusicDevice.SWITCH13),
				makeDeviceValueString(BeMusicDevice.SWITCH14), makeDeviceValueString(BeMusicDevice.SWITCH15),
				makeDeviceValueString(BeMusicDevice.SWITCH16), makeDeviceValueString(BeMusicDevice.SWITCH17),
				computeHandScore(Hand.LEFT), computeHandScore(Hand.RIGHT), getPointScore());
	}

	/**
	 * 入力デバイスごとのデバッグ出力内容生成
	 * @param device 入力デバイス
	 * @return デバッグ出力内容
	 */
	private String makeDeviceValueString(BeMusicDevice device) {
		var scoreNote = getNoteScore(device);
		var scoreRapidBeat = getRapidBeatScore(device);
		var countRapidBeat = getRapidBeatCount(device);
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