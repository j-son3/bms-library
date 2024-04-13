package com.lmt.lib.bms;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import com.lmt.lib.bms.internal.Utility;

/**
 * 小節データクラス。
 */
abstract class MeasureElement extends BmsElement {
	/**
	 * タイムライン要素の小節データ
	 */
	private static class ValueElement extends BmsElement {
		/** 値 */
		private Object mValue;

		/**
		 * コンストラクタ
		 * @param measure 小節番号
		 * @param channel チャンネル番号
		 * @param index チャンネルインデックス
		 * @param value 値
		 */
		ValueElement(int measure, int channel, int index, Object value) {
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

	/** BMS仕様 */
	private BmsSpec mSpec;
	/** 小節の刻み数 */
	private double mTickCount;
	/** 小節の刻み位置最大値 */
	private double mTickMax;
	/** 4/4拍子を1.0とした場合の小節の長さ比率 */
	private double mLengthRatio;
	/** 小節の開始時間(秒単位) */
	private double mBaseTime = 0.0;
	/** 小節長(秒単位) */
	private double mLength = 0.0;
	/** 小節開始時のBPM */
	private double mBeginBpm = 0.0;
	/** 小節終了時のBPM */
	private double mEndBpm = 0.0;
	/** 楽曲位置(刻み位置)ごとの時間関連情報 */
	private TreeMap<Double, PointTimeNode> mTimeNodeMap = null;
	/** CHXごとのノートリスト */
	private TreeMap<Integer, TreeSet<BmsNote>> mNotesChMap = null;
	/** CHXごとの小節データ */
	private TreeMap<Integer, BmsElement> mValuesChMap = null;

	/**
	 * コンストラクタ
	 * @param measure 小節番号
	 */
	MeasureElement(int measure) {
		super(measure, BmsSpec.TICK_MIN, BmsSpec.CHANNEL_MEASURE, BmsSpec.CHINDEX_MIN);
		setLengthRatio(1.0);
	}

	/** {@inheritDoc} */
	@Override
	public long getValueAsLong() {
		return getMeasure();
	}

	/** {@inheritDoc} */
	@Override
	public double getValueAsDouble() {
		return getMeasure();
	}

	/** {@inheritDoc} */
	@Override
	public String getValueAsString() {
		return String.valueOf(getMeasure());
	}

	/** {@inheritDoc} */
	@Override
	public Object getValueAsObject() {
		return getMeasure();
	}

	/** {@inheritDoc} */
	@Override
	public final boolean isMeasureLineElement() {
		return true;
	}

	/**
	 * BMS仕様取得
	 * @return BMS仕様
	 */
	final BmsSpec getSpec() {
		return mSpec;
	}

	/**
	 * BMS仕様設定
	 * @param spec BMS仕様
	 */
	final void setSpec(BmsSpec spec) {
		mSpec = spec;
	}

	/**
	 * 4/4拍子を1倍とした当該小節の長さ倍率を返す。
	 * <p>小節長変更チャンネルを用いて小節長を変更した場合に、1以外の値となる。この倍率は小節の刻み数を
	 * 決定するための値として用いられる。そのため、値が0になることは無い。最小値は刻み数が最小となる1/192である。</p>
	 * <p>BMSの仕様により、小節長を変更しない場合の初期値は必ず1となる。また、{@link BmsSpec}によって
	 * 小節長変更チャンネルを定義しなかった場合には、このメソッドは必ず1を返す結果となる。</p>
	 * @return 4/4拍子を1.0とした当該小節の長さ倍率
	 */
	final double getLengthRatio() {
		return mLengthRatio;
	}

	/**
	 * 当該小節の演奏が開始されるべき基準の時間を秒単位で返す。
	 * <p>この値には、初期BPM・小節の長さ・BPM変更・譜面停止時間を考慮して当該小節の演奏が開始されるべき
	 * 時間が格納される。終端の小節データに対してこのメソッドを呼び出した場合、返される値は不定である。</p>
	 * @return 小節の演奏が開始されるべき基準の時間(秒単位)
	 */
	final double getBaseTime() {
		return mBaseTime;
	}

	/**
	 * 当該小節の長さを秒単位で返す。
	 * <p>この値は、小節の長さ・BPM・譜面停止時間を考慮して計算された小節の長さを秒単位で返す。
	 * このメソッドが返す値の用途は主に時間を指定しての譜面データの取得である。</p>
	 * @return 当該小節の長さ(秒単位)
	 */
	final double getLength() {
		return mLength;
	}

	/**
	 * 当該小節の刻み数を返す。
	 * <p>この値は小節の長さ倍率が1.0の時に{@link BmsSpec#TICK_COUNT_DEFAULT}を返し、
	 * 長さ倍率に応じてその値に倍率を乗じた値を返す。</p>
	 * @return 当該小節の刻み数
	 */
	final double getTickCount() {
		return mTickCount;
	}

	/**
	 * 当該小節の刻み位置最大値を返す。
	 * @return 当該小節の刻み位置最大値
	 */
	final double getTickMax() {
		return mTickMax;
	}

	/**
	 * 当該小節開始時のBPMを返す。
	 * @return 当該小節開始時のBPM
	 */
	final double getBeginBpm() {
		return mBeginBpm;
	}

	/**
	 * 当該小節終了時のBPMを返す。
	 * @return 当該小節終了時のBPM
	 */
	final double getEndBpm() {
		return mEndBpm;
	}

	/**
	 * 指定楽曲位置の実際の時間計算
	 * @param tick 刻み位置
	 * @return この小節の小節番号と指定刻み位置の実際の時間
	 */
	final double computeTime(double tick) {
		var entry = (mTimeNodeMap != null) ? mTimeNodeMap.floorEntry(tick) : null;
		if (entry == null) {
			// この小節にBPM変更・譜面停止が存在しない場合、小節内の指定刻み位置までにBPM変更・譜面停止が登場しない場合
			// 上記の場合は小節開始時の実際の時間＋指定刻み位置までの時間を返す
			return mBaseTime + Utility.computeTime(tick, mBeginBpm);
		} else {
			// 指定刻み位置以下の最も大きい刻み位置にBPM変更・譜面停止が存在する場合
			// 上記楽曲位置の実際の時間＋指定刻み位置までの時間＋譜面停止時間を返す
			// ※ただし譜面停止時間は、指定刻み位置と取得した時間関連情報の刻み位置が同じ場合は加算しない。
			//   BMSの基本仕様として、譜面停止は楽曲位置到達後にカウント開始されるため。
			var nodeTick = entry.getKey().doubleValue();
			var timeNode = entry.getValue();
			var actualTime = timeNode.actualTime;
			actualTime += Utility.computeTime((tick - nodeTick), timeNode.currentBpm);
			actualTime += (tick == nodeTick) ? 0.0 : timeNode.stopTime;
			return actualTime;
		}
	}

	/**
	 * ノート追加
	 * @param note ノート
	 */
	final void putNote(BmsNote note) {
		// CHXごとのノートリストマップを生成する
		if (mNotesChMap == null) {
			mNotesChMap = new TreeMap<>();
		}

		// CHXに対応するノートリストを取得・生成する
		var chx = BmsInt.box(BmsChx.toInt(note));
		var notesCh = mNotesChMap.get(chx);
		if (notesCh == null) {
			notesCh = new TreeSet<>(BmsAddress::compare);
			mNotesChMap.put(chx, notesCh);
		}

		// ノートリストにノートを追加する
		notesCh.add(note);
	}

	/**
	 * ノート消去
	 * @param channel チャンネル番号
	 * @param index チャンネルインデックス
	 * @param tick 小節の刻み位置
	 */
	final void removeNote(int channel, int index, double tick) {
		// CHXごとのノートリストが存在しない場合は何もしない
		if (mNotesChMap == null) {
			return;
		}

		// CHXごとのノートリストを取得、存在しない場合は何もしない
		var chx = BmsInt.box(BmsChx.toInt(channel, index));
		var notesCh = mNotesChMap.get(chx);
		if (notesCh == null) {
			return;
		}

		// ノートリストから該当するノートを消去、ノートリストが0件ならリストごと消去する
		BmsNote note = new BmsNote();
		note.setChx(channel, index);
		note.setMeasure(getMeasure());
		note.setTick(tick);
		notesCh.remove(note);
		if (notesCh.size() == 0) {
			mNotesChMap.remove(chx);
		}

		// ノートリストマップが空になった場合はマップを解放する
		if (mNotesChMap.size() == 0) {
			mNotesChMap = null;
		}
	}

	/**
	 * 小節データ設定
	 * @param channel チャンネル番号
	 * @param index チャンネルインデックス
	 * @param value 小節データ
	 */
	final void putValue(int channel, int index, Object value) {
		// CHXごとの値マップを生成する
		if (mValuesChMap == null) {
			mValuesChMap = new TreeMap<>();
		}

		// マップに値を追加する
		mValuesChMap.put(
				BmsInt.box(BmsChx.toInt(channel, index)),
				new ValueElement(getMeasure(), channel, index, value));

		// 小節長を更新する
		var lengthChannel = mSpec.getLengthChannel();
		if ((lengthChannel != null) && (lengthChannel.getNumber() == channel)) {
			var lengthRatio = ((Number)((value == null) ? lengthChannel.getDefaultValue() : value)).doubleValue();
			setLengthRatio(lengthRatio);
		}
	}

	/**
	 * 小節データ消去
	 * @param channel チャンネル番号
	 * @param index チャンネルインデックス
	 */
	final void removeValue(int channel, int index) {
		// CHXごとの値マップが存在しない場合は何もしない
		if (mValuesChMap == null) {
			return;
		}

		// マップから値を削除、マップが空になったら解放する
		mValuesChMap.remove(BmsInt.box(BmsChx.toInt(channel, index)));
		if (mValuesChMap.size() == 0) {
			mValuesChMap = null;
		}

		// 小節長を更新する
		var lengthChannel = mSpec.getLengthChannel();
		if ((lengthChannel != null) && (lengthChannel.getNumber() == channel)) {
			var lengthRatio = ((Number)lengthChannel.getDefaultValue()).doubleValue();
			setLengthRatio(lengthRatio);
		}
	}

	/**
	 * ノートリスト取得
	 * @param channel チャンネル番号
	 * @param index チャンネルインデックス
	 * @param outList ノートリスト格納先リスト
	 * @return パラメータで指定したノートリスト格納先リスト
	 */
	final List<BmsNote> listNotes(int channel, int index, List<BmsNote> outList) {
		// CHXごとのノートリストが存在しない場合は何もしない
		outList.clear();
		if (mNotesChMap == null) {
			return outList;
		}

		// CHXごとのノートリストを取得、存在しない場合は何もしない
		var notesCh = mNotesChMap.get(BmsInt.box(BmsChx.toInt(channel, index)));
		if (notesCh == null) {
			return outList;
		}

		// 小節内の指定チャンネル全ノートを取得して返す
		outList.addAll(notesCh);
		return outList;
	}

	/**
	 * この小節が持つ全ての小節データ取得
	 * @return CHXごとの小節データマップ
	 */
	final Map<Integer, BmsElement> mapValues() {
		// CHXごとの小節の値が存在しない場合は何もしない
		var result = new TreeMap<Integer, BmsElement>();
		if (mValuesChMap == null) {
			return result;
		}

		// 小節の値を全て抽出する
		result.putAll(mValuesChMap);
		return result;
	}

	/**
	 * この小節が持つ小節データのチャンネルをテストする
	 * @param tester チャンネルを検査するテスター
	 * @return 検査に合格するチャンネルが存在した場合true、そうでなければfalse
	 */
	final boolean testValueChannels(BmsChannel.Tester tester) {
		// CHXごとの小節の値が存在しない場合はテスト不合格とする
		if (mValuesChMap == null) {
			return false;
		}

		// 全小節データのチャンネルを検査する
		for (var key : mValuesChMap.keySet()) {
			if (tester.testChannel(BmsChx.toChannel(key))) {
				return true;
			}
		}

		// 合格チャンネルなし
		return false;
	}

	/**
	 * 小節データ取得
	 * @param channel チャンネル番号
	 * @param index チャンネルインデックス
	 * @return 小節データ
	 */
	final BmsElement getValue(int channel, int index) {
		return (mValuesChMap == null) ? null : mValuesChMap.get(BmsInt.box(BmsChx.toInt(channel, index)));
	}

	/**
	 * 小節データのイテレータ取得
	 * @return 小節データのイテレータ
	 */
	final Iterator<? extends BmsElement> valueIterator() {
		return (mValuesChMap == null) ? Collections.emptyIterator() : mValuesChMap.values().iterator();
	}

	/**
	 * 配列型チャンネルのデータ数取得
	 * @param channel チャンネル番号
	 * @return 配列型チャンネルのデータ数
	 */
	final int getNoteChannelDataCount(int channel) {
		var chx = BmsInt.box(BmsChx.toInt(channel, BmsSpec.CHINDEX_MAX));
		var key = (mNotesChMap == null) ? null : mNotesChMap.floorKey(chx);
		return getChannelDataCount(channel, key);
	}

	/**
	 * 値型チャンネルのデータ数取得
	 * @param channel チャンネル番号
	 * @return 値型チャンネルのデータ数
	 */
	final int getValueChannelDataCount(int channel) {
		var chx = BmsInt.box(BmsChx.toInt(channel, BmsSpec.CHINDEX_MAX));
		var key = (mValuesChMap == null) ? null : mValuesChMap.floorKey(chx);
		return getChannelDataCount(channel, key);
	}

	/**
	 * この小節のデータが空かどうかを判定
	 * @return この小節のデータが空ならtrue、そうでないならfalse
	 */
	final boolean isEmpty() {
		return ((mNotesChMap == null) && (mValuesChMap == null));
	}

	/**
	 * この小節のノートが空かどうかを判定
	 * @return この小節のノートが空ならtrue、そうでないならfalse
	 */
	final boolean isEmptyNotes() {
		return (mNotesChMap == null);
	}

	/**
	 * 時間・BPMに関連する情報を再計算する。
	 * @param userParam ユーザー定義のパラメータ
	 */
	final void recalculateTimeInfo(Object userParam) {
		var timeInfo = onRecalculateTimeInfo(userParam);
		mBaseTime = timeInfo.baseTime;
		mLength = timeInfo.length;
		mBeginBpm = timeInfo.beginBpm;
		mEndBpm = timeInfo.endBpm;
		mTimeNodeMap = timeInfo.timeNodeMap;
	}

	/**
	 * 小節の長さ比率
	 * @param lengthRatio 小節の長さ比率
	 */
	private void setLengthRatio(double lengthRatio) {
		mLengthRatio = lengthRatio;
		mTickCount = BmsSpec.computeTickCount(mLengthRatio, false);
		mTickMax = Math.nextDown(mTickCount);
	}

	/**
	 * チャンネルデータ数取得
	 * @param channel チャンネル番号
	 * @param chx CHX値
	 * @return チャンネルデータ数
	 */
	private static int getChannelDataCount(int channel, Integer chx) {
		if (chx == null) {
			return 0;
		} else {
			var value = chx.intValue();
			var number = BmsChx.toChannel(value);
			var index = BmsChx.toIndex(value);
			return (number != channel) ? 0 : (index + 1);
		}
	}

	/**
	 * 時間・BPMに関連する情報を再計算する。
	 * @param userParam ユーザー定義のパラメータ
	 * @return 小節が保有する時間関連情報
	 */
	protected abstract MeasureTimeInfo onRecalculateTimeInfo(Object userParam);
}
