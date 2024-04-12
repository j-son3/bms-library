package com.lmt.lib.bms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.IntFunction;

/**
 * チャンネルデータのデータを管理するコレクション。
 * <p>当クラスで管理するのは、配列型チャンネルのノート、および値型チャンネルの小節データである。</p>
 * <p>このクラスは{@link com.lmt.lib.bms.BmsContent BmsContent}の内部データとして扱われることを想定しており、
 * クラスへのアクセスは全てのパラメータのアサーションが完了したあとであることを前提とした作りになっている。</p>
 *
 * @param <E> 拡張小節データを表す型
 */
class ChannelDataCollection<E extends MeasureData> {
	/** ノートリスト生成クラス */
	private static class NoteLister implements BmsNote.Tester {
		/** ノート格納先リスト */
		private List<BmsNote> mList = null;
		/** 格納対象ノートを検査するテスター */
		private BmsNote.Tester mTester = null;

		/**
		 * セットアップ
		 * @param list ノート格納先リスト
		 * @param tester 格納対象ノートを検査するテスター
		 */
		final void setup(List<BmsNote> list, BmsNote.Tester tester) {
			mList = list;
			mTester = tester;
		}

		/**
		 * 処理を終了し、ノート格納先リスト取得
		 * @return ノート格納先リスト
		 */
		final List<BmsNote> finish() {
			var result = mList;
			mList = null;
			mTester = null;
			return result;
		}

		/**
		 * ノート格納先リストに追加するノートを検査するテスター
		 * @param note 検査対象ノート
		 * @return 常にtrueを返し、列挙続行
		 */
		@Override
		public boolean testNote(BmsNote note) {
			if (mTester.testNote(note)) { mList.add(note); }
			return true;
		}
	}

	/** ノートカウントクラス */
	private static class NoteCounter implements BmsNote.Tester {
		/** カウンタ */
		private int mCount = 0;
		/** カウント有無を検査するテスター */
		private BmsNote.Tester mTester = null;

		/**
		 * セットアップ
		 * @param tester カウント有無を検査するテスター
		 */
		final void setup(BmsNote.Tester tester) {
			mCount = 0;
			mTester = tester;
		}

		/**
		 * 処理を終了し、カウンタを取得する
		 * @return カウンタ
		 */
		final int finish() {
			var result = mCount;
			mCount = 0;
			mTester = null;
			return result;
		}

		/**
		 * カウント有無を検査するテスター
		 * @param note 検査対象ノート
		 * @return 常にtrueを返し、列挙続行
		 */
		@Override
		public boolean testNote(BmsNote note) {
			if (mTester.testNote(note)) { mCount++; }
			return true;
		}
	}

	/** BMS仕様 */
	private BmsSpec mSpec;
	/** 拡張小節データ生成関数 */
	private IntFunction<E> mMeasureDataCreator;
	/** 全ノート */
	private TreeSet<BmsNote> mNotes = new TreeSet<>(BmsNote.COMPARATOR);
	/** チャンネルデータキーごとのノートリスト */
	private TreeMap<MutableInt, TreeSet<BmsNote>> mNotesChMap = new TreeMap<>();
	/** 小節ごとの小節データマップ */
	private ArrayList<E> mMeasureDataMap = new ArrayList<>();
	/** 検索キー用ノート1 */
	private BmsNote mNoteTemp1 = new BmsNote();
	/** 検索キー用ノート2 */
	private BmsNote mNoteTemp2 = new BmsNote();
	/** 検索キー用チャンネルキー */
	private MutableInt mChKey = new MutableInt(0);
	/** ノートリスト生成クラス */
	private NoteLister mNoteLister = new NoteLister();
	/** ノートカウンタクラス */
	private NoteCounter mNoteCounter = new NoteCounter();
	/** 時間・BPM情報の再計算が必要な最初の小節データ */
	private int mRecalcFirst = Integer.MAX_VALUE;
	/** 時間・BPM情報の再計算が必要な最後の小節データ */
	private int mRecalcLast = Integer.MIN_VALUE;

	/**
	 * コンストラクタ
	 * @param measureDataCreator 小節データ生成関数
	 */
	ChannelDataCollection(BmsSpec spec, IntFunction<E> measureDataCreator) {
		mSpec = spec;
		mMeasureDataCreator = measureDataCreator;
	}

	/**
	 * ノート取得
	 * @param channel チャンネル番号
	 * @param index チャンネルインデックス
	 * @param measure 小節番号
	 * @param tick 小節の刻み位置
	 * @return ノート
	 */
	final BmsNote getNote(int channel, int index, int measure, double tick) {
		// 全体ノートリストから該当ノートを取得する
		var target = note1(channel, index, measure, tick);
		var got = mNotes.ceiling(target);

		// チャンネル、楽曲位置が同一の場合のみ取得結果を返す
		var equal = (got == null) ? false : (BmsNote.COMPARATOR.compare(target, got) == 0);
		return equal ? got : null;
	}

	/**
	 * 指定チャンネルキーに該当する次のノート取得
	 * @param channel チャンネル番号
	 * @param index チャンネルキー
	 * @param measure 小節番号
	 * @param tick 小節の刻み位置
	 * @param inclusive 指定楽曲位置のノートも検索対象とするかどうか
	 * @return 次のノート。そのようなノートが存在しない場合null
	 */
	final BmsNote getNextNote(int channel, int index, int measure, double tick, boolean inclusive) {
		var notesCh = mNotesChMap.get(ChannelDataKey.make(mChKey, channel, index));
		if (notesCh == null) {
			// 当該チャンネルにノートがない場合はnull
			return null;
		} else {
			// 指定位置以降のノートを取得する
			var point = note1(channel, index, measure, tick);
			return inclusive ? notesCh.ceiling(point) : notesCh.higher(point);
		}
	}

	/**
	 * 指定チャンネルキーに該当する前のノート取得
	 * @param channel チャンネル番号
	 * @param index チャンネルインデックス
	 * @param measure 小節番号
	 * @param tick 小節の刻み位置
	 * @param inclusive 指定楽曲位置のノートも検索対象とするかどうか
	 * @return 前のノート。そのようなノートが存在しない場合null
	 */
	final BmsNote getPreviousNote(int channel, int index, int measure, double tick, boolean inclusive) {
		// チャンネルごとのノートリストから指定位置より前にある最初のノートを取得する
		var notesCh = mNotesChMap.get(ChannelDataKey.make(mChKey, channel, index));
		if (notesCh == null) {
			// 当該チャンネルにノートがない場合はnull
			return null;
		} else {
			// 指定位置以前のノートを取得する
			var point = note1(channel, index, measure, tick);
			return inclusive ? notesCh.floor(point) : notesCh.lower(point);
		}
	}

	/**
	 * 小節データ取得
	 * @param channel チャンネル番号
	 * @param index チャンネルインデックス
	 * @param measure 小節番号
	 * @return 小節データ
	 */
	final Object getMeasureValue(int channel, int index, int measure) {
		return mMeasureDataMap.get(measure).getValue(channel, index);
	}

	/**
	 * 小節数取得
	 * @return 小節数
	 */
	final int getMeasureCount() {
		return mMeasureDataMap.size();
	}

	/**
	 * 配列型チャンネルのデータ数取得
	 * @param channel チャンネル番号
	 * @param measure 小節番号
	 * @return 配列型チャンネルのデータ数
	 */
	final int getNoteChannelDataCount(int channel, int measure) {
		return mMeasureDataMap.get(measure).getNoteChannelDataCount(channel);
	}

	/**
	 * 値型チャンネルのデータ数取得
	 * @param channel チャンネル番号
	 * @param measure 小節番号
	 * @return 値型チャンネルのデータ数
	 */
	final int getValueChannelDataCount(int channel, int measure) {
		return mMeasureDataMap.get(measure).getValueChannelDataCount(channel);
	}

	/**
	 * 指定小節の刻み数取得。範囲外の小節を指定した場合は{@link BmsSpec#TICK_COUNT_DEFAULT}を返す。
	 * @param measure 小節番号
	 * @return 指定小節の刻み数
	 */
	final int getMeasureTickCount(int measure) {
		if ((measure < BmsSpec.MEASURE_MIN) || (measure >= getMeasureCount())) {
			return BmsSpec.TICK_COUNT_DEFAULT;
		} else {
			return mMeasureDataMap.get(measure).getTickCount();
		}
	}

	/**
	 * 指定小節の刻み位置最大値取得。範囲外の小節を指定した場合は{@link BmsSpec#TICK_COUNT_DEFAULT}未満の最大値を返す。
	 * @param measure 小節番号
	 * @return 指定小節の刻み位置最大値
	 */
	final double getMeasureTickMax(int measure) {
		if ((measure < BmsSpec.MEASURE_MIN) || (measure >= getMeasureCount())) {
			return Math.nextDown(BmsSpec.TICK_COUNT_DEFAULT);
		} else {
			return mMeasureDataMap.get(measure).getTickMax();
		}
	}

	/**
	 * 拡張小節データ取得。小節データが存在しない場合は小節データを生成した後で拡張小節データを取得する。
	 * @param measure 小節番号
	 * @return 拡張小節データ
	 */
	final E getMeasureData(int measure) {
		var measureData = mMeasureDataMap.get(measure);
		return measureData;
	}

	/**
	 * 指定小節のノートが空かどうかを判定
	 * @param measure 小節番号
	 * @return 指定小節のノートが存在しなければtrue、そうでなければfalse
	 */
	final boolean isMeasureEmptyNotes(int measure) {
		return mMeasureDataMap.get(measure).isEmptyNotes();
	}

	/**
	 * 指定位置から進行方向にノート検索
	 * @param measureFrom 検索開始小節番号
	 * @param tickFrom 検索開始小節の刻み位置
	 * @param tester ノートを検査するテスター
	 * @return 見つかった次のノート。そのようなノートが存在しない場合null
	 */
	final BmsNote pointOf(int measureFrom, double tickFrom, BmsNote.Tester tester) {
		// 指定位置から進行方向にノートを検索し、テスターの検査に合格した最初のノートを返す
		for (var note : mNotes.tailSet(note1(BmsSpec.CHANNEL_MIN, BmsSpec.CHINDEX_MIN, measureFrom, tickFrom))) {
			if (tester.testNote(note)) { return note; }
		}
		return null;
	}

	/**
	 * 指定楽曲位置以降の次の楽曲位置を検索
	 * @param measure 検索開始小節番号
	 * @param tick 検索開始小節の刻み位置
	 * @param inclusiveFrom 指定楽曲位置を検索対象に含めるかどうか
	 * @param chTester チャンネルのテスター
	 * @param outPoint 見つかった楽曲位置を格納する楽曲位置のインスタンス
	 * @return パラメータで指定した楽曲位置のインスタンス
	 */
	final BmsPoint seekNextPoint(int measure, double tick, boolean inclusiveFrom, BmsChannel.Tester chTester, BmsPoint outPoint) {
		// 検索開始位置を決定する
		var noteFrom = (BmsNote)null;
		var inclusive = false;
		if (inclusiveFrom) {
			// 指定楽曲位置を含む場合、指定楽曲位置の最小チャンネル以降(これを含む)を始点とする
			noteFrom = note1(BmsSpec.CHANNEL_MIN, BmsSpec.CHINDEX_MIN, measure, tick);
			inclusive = true;
		} else {
			// 指定楽曲位置を含まない場合、指定楽曲位置の最大チャンネルより後(これを含まない)を始点とする
			noteFrom = note1(BmsSpec.CHANNEL_MAX, BmsSpec.CHINDEX_MAX, measure, tick);
			inclusive = false;
		}

		// 指定楽曲位置よりも後の楽曲位置を検索し、チャンネルのテスターの検査に合格した最初のノートの楽曲位置を探す
		var measurePrev = ((tick == 0) && inclusiveFrom) ? (measure - 1) : measure;
		var notePrev = note2(0, 0, measurePrev, tick);
		for (var note : mNotes.tailSet(noteFrom, inclusive)) {
			// 小節線と小節データをテストする
			var measureResult = testMeasureChannels(notePrev.getMeasure(), note.getMeasure(), chTester);
			if (measureResult >= 0) {
				outPoint.setMeasure(measureResult);
				outPoint.setTick(0);
				return outPoint;
			}

			// ノートをテストする
			if (chTester.testChannel(note.getChannel())) {
				outPoint.setMeasure(note.getMeasure());
				outPoint.setTick(note.getTick());
				return outPoint;
			}

			// 今回テストしたノートを前回ノートとして記憶しておく
			notePrev = note;
		}

		// 最後に検査したノートから小節終端までの小節線と小節データをテストする
		var measureCount = getMeasureCount();
		var measureResult = testMeasureChannels(notePrev.getMeasure(), (measureCount - 1), chTester);
		if (measureResult >= 0) {
			outPoint.setMeasure(measureResult);
			outPoint.setTick(0);
			return outPoint;
		}

		// 楽曲位置が取得できなかった場合は末端小節を返す
		outPoint.setMeasure(measureCount);
		outPoint.setTick(0);
		return outPoint;
	}

	/**
	 * 指定チャンネルの全ノートを入れ替える
	 * @param channel1 チャンネル番号1
	 * @param index1 チャンネルインデックス1
	 * @param channel2 チャンネル番号2
	 * @param index2 チャンネルインデックス2
	 */
	final void swapNoteChannel(int channel1, int index1, int channel2, int index2) {
		// 入れ替え対象チャンネルのデータを全件取り出す
		var set1 = mNotesChMap.get(ChannelDataKey.make(mChKey, channel1, index1));
		var set2 = mNotesChMap.get(ChannelDataKey.make(mChKey, channel2, index2));

		// 両ノートリストを一旦退避する
		var notes1 = new ArrayList<>((set1 == null) ? Collections.emptyList() : set1);
		var notes2 = new ArrayList<>((set2 == null) ? Collections.emptyList() : set2);

		// 両ノートリストを消去する
		notes1.forEach(n -> { removeNote(n.getChannel(), n.getIndex(), n.getMeasure(), n.getTick()); });
		notes2.forEach(n -> { removeNote(n.getChannel(), n.getIndex(), n.getMeasure(), n.getTick()); });

		// 両ノートリストをチャンネルを入れ替えて追加する
		notes1.forEach(n -> { putNote(n.changeChannel(channel2, index2)); });
		notes2.forEach(n -> { putNote(n.changeChannel(channel1, index1)); });
	}

	/**
	 * 指定チャンネルの全小節データを入れ替える
	 * @param channel1 チャンネル番号1
	 * @param index1 チャンネルインデックス1
	 * @param channel2 チャンネル番号2
	 * @param index2 チャンネルインデックス2
	 */
	final void swapValueChannel(int channel1, int index1, int channel2, int index2) {
		// 入れ替え対象チャンネルの全小節データを抜き出す
		var values1 = pullChannelAllValues(channel1, index1);
		var values2 = pullChannelAllValues(channel2, index2);

		// 両小節データをチャンネルを入れ替えて追加する
		values1.forEach((m, v) -> { putMeasureValue(channel2, index2, m.get(), v); });
		values2.forEach((m, v) -> { putMeasureValue(channel1, index1, m.get(), v); });
	}

	/**
	 * ノート列挙
	 * @param chBeg 列挙開始チャンネル番号
	 * @param chEnd 列挙終了チャンネル番号(このチャンネルを含まない)
	 * @param mBeg 列挙開始小節番号
	 * @param tBeg 列挙開始小節の刻み位置
	 * @param mEnd 列挙終了小節番号
	 * @param tEnd 列挙終了小節の刻み位置(この刻み位置を含まない)
	 * @param tester 列挙終了を判定するテスター
	 */
	final void enumNotes(int chBeg, int chEnd, int mBeg, double tBeg, int mEnd, double tEnd, BmsNote.Tester tester) {
		var begin = note1(BmsSpec.CHANNEL_MIN, BmsSpec.CHINDEX_MIN, mBeg, tBeg);
		var end = note2(BmsSpec.CHANNEL_MAX + 1, BmsSpec.CHINDEX_MAX + 1, mEnd, tEnd);
		for (var note : mNotes.subSet(begin, true, end, false)) {
			var ch = note.getChannel();
			if ((ch >= chBeg) && (ch < chEnd)) { if (!tester.testNote(note)) { return; } }
		}
	}

	/**
	 * ノートをリスト化
	 * @param chBeg リスト化開始チャンネル番号
	 * @param chEnd リスト化終了チャンネル番号(このチャンネルを含まない)
	 * @param mBeg リスト化開始小節番号
	 * @param tBeg リスト化開始小節の刻み位置
	 * @param mEnd リスト化終了小節番号
	 * @param tEnd リスト化終了小節の刻み位置(この刻み位置を含まない)
	 * @param tester リスト追加有無を判定するテスター
	 * @return ノートリスト
	 */
	final List<BmsNote> listNotes(int chBeg, int chEnd, int mBeg, double tBeg, int mEnd, double tEnd, BmsNote.Tester tester) {
		// ノート列挙を使用して列挙されたノートをリスト化する
		mNoteLister.setup(new ArrayList<>(), tester);
		enumNotes(chBeg, chEnd, mBeg, tBeg, mEnd, tEnd, mNoteLister);
		return mNoteLister.finish();
	}

	/**
	 * ノートをリスト化
	 * @param channel チャンネル番号
	 * @param index チャンネルインデックス
	 * @param measure 小節番号
	 * @param outList リスト化したノートを格納するリストインスタンス
	 * @return パラメータで指定したリストインスタンス
	 */
	final List<BmsNote> listNotes(int channel, int index, int measure, List<BmsNote> outList) {
		mMeasureDataMap.get(measure).listNotes(channel, index, outList);
		return outList;
	}

	/**
	 * ノート数をカウント
	 * @param chBeg カウント開始チャンネル番号
	 * @param chEnd カウント終了チャンネル番号(このチャンネルを含まない)
	 * @param mBeg カウント開始小節番号
	 * @param tBeg カウント開始小節の刻み位置
	 * @param mEnd カウント終了小節番号
	 * @param tEnd カウント終了小節の刻み位置(この刻み位置を含まない)
	 * @param tester カウント有無を判定するテスター
	 * @return
	 */
	final int countNotes(int chBeg, int chEnd, int mBeg, double tBeg, int mEnd, double tEnd, BmsNote.Tester tester) {
		// ノート列挙を使用して列挙されたノートをカウントする
		mNoteCounter.setup(tester);
		enumNotes(chBeg, chEnd, mBeg, tBeg, mEnd, tEnd, mNoteCounter);
		return mNoteCounter.finish();
	}

	/**
	 * ノート追加
	 * @param note 追加対象ノート
	 */
	final void putNote(BmsNote note) {
		var oldMeasureCount = getMeasureCount();

		// 全体ノートリストへ追加する(既存ノートであれば上書き)
		mNotes.add(note);

		// チャンネルごとのノートリストへ追加する
		var notesCh = mNotesChMap.get(ChannelDataKey.make(mChKey, note));
		if (notesCh == null) {
			notesCh = new TreeSet<>(BmsNote.COMPARATOR);
			mNotesChMap.put(new MutableInt(ChannelDataKey.make(note)), notesCh);
		}
		notesCh.add(note);

		// 小節データリストへ追加する
		var measureData = getOrCreateMeasureData(note.getMeasure());
		measureData.putNote(note);

		// 時間・BPMの再計算範囲を更新する
		updateRecalcRangeByAdd(oldMeasureCount);
		updateRecalcRangeByChannel(note.getChannel(), note.getMeasure());
	}

	/**
	 * ノート消去
	 * @param channel チャンネル番号
	 * @param index チャンネルインデックス
	 * @param measure 小節番号
	 * @param tick 小節の刻み位置
	 * @return ノートを消去した場合true、そうでなければfalse
	 */
	final boolean removeNote(int channel, int index, int measure, double tick) {
		// 全体ノートリストから消去する
		var result = mNotes.remove(note1(channel, index, measure, tick));

		// チャンネルごとのノートリストから消去する
		if (result) {
			// ノートリストからの消去
			var notesCh = mNotesChMap.get(ChannelDataKey.make(mChKey, channel, index));
			notesCh.remove(note1(channel, index, measure, tick));

			// 消去した結果リストが空になった場合はリスト自体を消去する
			if (notesCh.size() == 0) {
				mNotesChMap.remove(ChannelDataKey.make(mChKey, channel, index));
			}

			// 小節データリストから消去する
			mMeasureDataMap.get(measure).removeNote(channel, index, tick);

			// 小節データをクリーンアップする
			cleanupMeasureDataIfNeeded();
		}

		return result;
	}

	/**
	 * 小節挿入
	 * @param where 追加位置
	 * @param count 追加数
	 */
	final void insertMeasure(int where, int count) {
		// 挿入位置以降の全ノートと全小節データを取得する
		var oldMeasureCount = getMeasureCount();
		var noteList = listNotes(BmsSpec.CHANNEL_MIN, BmsSpec.CHANNEL_MAX + 1, where, 0, oldMeasureCount, 0, n -> true);
		var valuesList = pullMeasureValues(where, oldMeasureCount - where);

		// 挿入位置以降の全ノートと全小節データを消去する
		for (var n : noteList) { removeNote(n.getChannel(), n.getIndex(), n.getMeasure(), n.getTick()); }
		mMeasureDataMap.removeIf(m -> m.getMeasure() >= where);

		// 移動対象の全ノートと小節データを復元する
		restoreMeasureValues(valuesList, where + count);
		for (var n : noteList) { putNote(n.shiftMeasure(count)); }
	}

	/**
	 * 小節消去
	 * @param where 消去位置
	 * @param count 消去数
	 */
	final void removeMeasure(int where, int count) {
		// 消去対象以降の、移動対象全ノートと小節データを取得する
		var measureCount = getMeasureCount();
		var moveMeasureBase = where + count;
		var moveMeasureCount = measureCount - moveMeasureBase;
		var moveNoteList = listNotes(BmsSpec.CHANNEL_MIN, BmsSpec.CHANNEL_MAX + 1, moveMeasureBase, 0, measureCount, 0, n -> true);
		var moveValuesList = pullMeasureValues(moveMeasureBase, moveMeasureCount);

		// 消去対象小節のノートを消去する
		var removeNoteList = listNotes(BmsSpec.CHANNEL_MIN, BmsSpec.CHANNEL_MAX + 1, where, 0, where + count, 0, n -> true);
		for (var n : removeNoteList) { removeNote(n.getChannel(), n.getIndex(), n.getMeasure(), n.getTick()); }

		// 消去対象小節以降の小節データを消去する
		mMeasureDataMap.removeIf(m -> m.getMeasure() >= where);

		// 移動対象全ノートと小節データを再追加する
		restoreMeasureValues(moveValuesList, where);
		for (var n : moveNoteList) { putNote(n.shiftMeasure(-count)); }
	}

	/**
	 * 小節データ設定
	 * @param channel チャンネル番号
	 * @param index チャンネルインデックス
	 * @param measure 小節番号
	 * @param value 小節データ
	 */
	final void putMeasureValue(int channel, int index, int measure, Object value) {
		var oldMeasureCount = getMeasureCount();
		var measureData = getOrCreateMeasureData(measure);

		// 小節の範囲外に飛び出すノートを全て消去する
		removeNotesOnShrinkedMeasureIfNeeded(channel, index, measure, value);

		// 小節データを追記する
		measureData.putValue(channel, index, value);

		// 時間・BPMの再計算範囲を更新する
		updateRecalcRangeByAdd(oldMeasureCount);
		updateRecalcRangeByChannel(channel, measure);
	}

	/**
	 * 小節データ消去
	 * @param channel チャンネル番号
	 * @param index チャンネルインデックス
	 * @param measure 小節番号
	 */
	final void removeMeasureValue(int channel, int index, int measure) {
		// 小節の範囲外に飛び出すノートを全て消去する
		removeNotesOnShrinkedMeasureIfNeeded(channel, index, measure, null);

		// 小節データを消去する
		mMeasureDataMap.get(measure).removeValue(channel, index);

		// 小節データをクリーンアップする
		cleanupMeasureDataIfNeeded();

		// 時間・BPMの再計算範囲を更新する
		updateRecalcRangeByChannel(channel, measure);
	}

	/**
	 * 指定範囲の小節データを時間・BPM再計算対象に設定する。
	 * @param first 対象の最小小節番号
	 * @param last 対象の最大小節番号
	 */
	final void updateRecalcRange(int first, int last) {
		mRecalcFirst = Math.min(mRecalcFirst, first);
		mRecalcLast = Math.max(mRecalcLast, last);
	}

	/**
	 * 時間・BPM再計算対象の小節データの再計算処理。
	 * @return 小節データの再計算を行った場合true
	 */
	final boolean recalculateTime(Object userParam) {
		// 再計算範囲が存在しない場合は再計算不要
		if (mRecalcFirst > mRecalcLast) {
			return false;
		}

		// 再計算対象範囲の小節の時間、および初期・最終BPMを更新する
		var begin = mRecalcFirst;
		var last = Math.min(mRecalcLast, getMeasureCount() - 1);
		for (var i = begin; i <= last; i++) {
			// 小節に対して時間・BPMの情報を反映する
			var measureData = getMeasureData(i);
			measureData.recalculateTimeInfo(userParam);
		}

		// 再計算対象範囲をクリアする
		mRecalcFirst = Integer.MAX_VALUE;
		mRecalcLast = Integer.MIN_VALUE;

		return true;
	}

	/**
	 * 小節データ取得、なければ生成して取得
	 * @param measure 小節番号
	 * @return 小節データ
	 */
	private MeasureData getOrCreateMeasureData(int measure) {
		// 指定小節番号の小節データがまだ生成されていない場合は指定小節番号までの小節データを生成する
		var measureCount = mMeasureDataMap.size();
		if (measure >= measureCount) {
			var createCount = measure - measureCount + 1;
			for (var i = 0; i < createCount; i++) {
				var measureData = mMeasureDataCreator.apply(measureCount + i);
				measureData.setSpec(mSpec);
				mMeasureDataMap.add(measureData);
			}
		}

		// 指定小節番号の小節データを取得する(前処理により範囲内保証される)
		return mMeasureDataMap.get(measure);
	}

	/**
	 * 全小節データから指定チャンネルに該当する小節データを全て抜き出す
	 * @param channel チャンネル番号
	 * @param index チャンネルインデックス
	 * @return チャンネルに該当する全小節データマップ
	 */
	private Map<MutableInt, Object> pullChannelAllValues(int channel, int index) {
		var allValues = new TreeMap<MutableInt, Object>();
		var measureCount = getMeasureCount();
		for (var m = BmsSpec.MEASURE_MIN; m < measureCount; m++) {
			var measureData = mMeasureDataMap.get(m);
			var value = (measureData == null) ? null : measureData.getValue(channel, index);
			if (value != null) {
				allValues.put(new MutableInt(m), value);
				measureData.removeValue(channel, index);
			}
		}
		return allValues;
	}

	/**
	 * 指定小節から全小節データを抜き出す
	 * @param measureFrom 抜き出し開始小節番号
	 * @param count 抜き出す小節数
	 * @return 全小節データリスト
	 */
	private ArrayList<Map<MutableInt, Object>> pullMeasureValues(int measureFrom, int count) {
		var valuesList = new ArrayList<Map<MutableInt, Object>>(count);
		for (var i = measureFrom; i < (measureFrom + count); i++) {
			var measureData = mMeasureDataMap.get(i);
			var values = (measureData == null) ? Collections.<MutableInt, Object>emptyMap() : measureData.mapValues();
			for (var key : values.keySet()) {
				int channel = ChannelDataKey.getNumber(key.get());
				int index = ChannelDataKey.getIndex(key.get());
				measureData.removeValue(channel, index);
			}
			valuesList.add(values);
		}
		return valuesList;
	}

	/**
	 * 指定小節へ全小節データを復元
	 * @param valuesList 全小節データリスト
	 * @param measureFrom 復元開始位置の小節番号
	 */
	private void restoreMeasureValues(ArrayList<Map<MutableInt, Object>> valuesList, int measureFrom) {
		for (var i = 0; i < valuesList.size(); i++) {
			var values = valuesList.get(i);
			for (var entry : values.entrySet()) {
				var key = entry.getKey();
				var channel = ChannelDataKey.getNumber(key.get());
				var index = ChannelDataKey.getIndex(key.get());
				putMeasureValue(channel, index, measureFrom + i, entry.getValue());
			}
		}
	}

	/**
	 * 小節データのクリーンアップを必要に応じて実行
	 */
	private void cleanupMeasureDataIfNeeded() {
		// 小節データの消去範囲を特定する
		var measureCount = getMeasureCount();
		var emptyFrom = BmsSpec.MEASURE_MIN;
		for (var m = measureCount - 1; m >= BmsSpec.MEASURE_MIN; m--) {
			var measureData = mMeasureDataMap.get(m);
			if (!measureData.isEmpty()) {
				emptyFrom = m + 1;
				break;
			}
		}

		// 消去対象小節データが存在する場合は消去を行う
		if (emptyFrom < measureCount) {
			var removeFrom = emptyFrom;
			mMeasureDataMap.removeIf(m -> m.getMeasure() >= removeFrom);
		}
	}

	/**
	 * 小節線、小節データのチャンネルを検査する
	 * @param measureFirst 検査開始小節番号(実際は次の小節から検査を開始する)
	 * @param measureLast 検査終了小節番号(個の小節を含まない)
	 * @param chTester チャンネルを検査するテスター
	 * @return チャンネルの検査に最初に合格した小節番号。そのような小節がない場合-1
	 */
	private int testMeasureChannels(int measureFirst, int measureLast, BmsChannel.Tester chTester) {
		// 小節が変化しない場合は何もしない
		if (measureLast <= measureFirst) {
			return -1;
		}

		// 飛んだ分の小節の小節線と小節データをテストする
		for (var m = (measureFirst + 1); m < (measureLast + 1); m++) {
			// 小節線をテストする
			if (chTester.testChannel(BmsSpec.CHANNEL_MEASURE)) {
				return m;
			}

			// 小節データをテストする
			if (mMeasureDataMap.get(m).testValueChannels(chTester)) {
				return m;
			}
		}

		// 全テスト不合格
		return -1;
	}

	/**
	 * 小節長縮小時の小節範囲外ノートの一括消去(必要な場合のみ行う)
	 * @param channel 更新チャンネル番号
	 * @param index 更新チャンネルインデックス
	 * @param measure 更新小節番号
	 * @param newValue 更新値
	 */
	private void removeNotesOnShrinkedMeasureIfNeeded(int channel, int index, int measure, Object newValue) {
		// 小節長変更チャンネルなし、対象チャンネルが小節長変更チャンネルでない場合は処理不要
		var lengthChannel = mSpec.getLengthChannel();
		if ((lengthChannel == null) || (lengthChannel.getNumber() != channel)) {
			return;
		}

		// 小節長が拡張する場合は処理不要
		var newLength = (double)((newValue == null) ? lengthChannel.getDefaultValue() : newValue);
		var oldLength = mMeasureDataMap.get(measure).getLengthRatio();
		if (newLength >= oldLength) {
			return;
		}

		// 小節長縮小後の末尾以降のノートを消去する
		var removeBeginTick = BmsSpec.computeTickCount(newLength, true);
		var removeNotes = listNotes(BmsSpec.CHANNEL_MIN, BmsSpec.CHANNEL_MAX + 1, measure, removeBeginTick, (measure + 1), 0, n -> true);
		for (var n : removeNotes) { removeNote(n.getChannel(), n.getIndex(), n.getMeasure(), n.getTick()); }
	}

	/**
	 * 小節データの時間・BPM再計算対象範囲更新(データ追加による更新範囲)
	 * @param oldMeasureCount データ追加を行う前の小節数
	 */
	private void updateRecalcRangeByAdd(int oldMeasureCount) {
		// 小節データが増加した場合、増加分の小節データを再計算対象にする
		int nowMeasureCount = getMeasureCount();
		if (nowMeasureCount > oldMeasureCount) {
			updateRecalcRange(oldMeasureCount, BmsSpec.MEASURE_MAX);
		}
	}

	/**
	 * 小節データの時間・BPM再計算対象範囲更新(更新データのチャンネルによる更新範囲)
	 * @param channel 更新したデータのチャンネル番号
	 * @param measure データを更新した小節番号
	 */
	private void updateRecalcRangeByChannel(int channel, int measure) {
		var ch = mSpec.getChannel(channel);
		if (ch.isRelatedToTime()) {
			updateRecalcRange(measure, BmsSpec.MEASURE_MAX);
		}
	}

	/**
	 * 検索キー用ノート1構築
	 * @param channel チャンネル番号
	 * @param index チャンネルインデックス
	 * @param measure 小節番号
	 * @param tick 小節の刻み位置
	 * @return 検索キー用ノート1
	 */
	private BmsNote note1(int channel, int index, int measure, double tick) {
		return note(mNoteTemp1, channel, index, measure, tick);
	}

	/**
	 * 検索キー用ノート2構築
	 * @param channel チャンネル番号
	 * @param index チャンネルインデックス
	 * @param measure 小節番号
	 * @param tick 小節の刻み位置
	 * @return 検索キー用ノート2
	 */
	private BmsNote note2(int channel, int index, int measure, double tick) {
		return note(mNoteTemp2, channel, index, measure, tick);
	}

	/**
	 * 指定ノートに値設定
	 * @param target 対象ノート
	 * @param channel チャンネル番号
	 * @param index チャンネルインデックス
	 * @param measure 小節番号
	 * @param tick 小節の刻み位置
	 * @return 対象ノートのインスタンス
	 */
	private BmsNote note(BmsNote target, int channel, int index, int measure, double tick) {
		target.setChannel(channel);
		target.setIndex(index);
		target.setMeasure(measure);
		target.setTick(tick);
		return target;
	}
}
