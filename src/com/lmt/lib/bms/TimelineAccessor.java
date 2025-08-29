package com.lmt.lib.bms;

import static com.lmt.lib.bms.internal.Assertion.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntPredicate;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.lmt.lib.bms.internal.Utility;

/**
 * BMSコンテンツを構成するタイムラインのアクセッサ。
 * <p>外部に公開するAPIのメソッドから直接アクセスするのではなく、
 * このクラスを介してタイムラインにアクセスするように設計されている。</p>
 * <p>上記の事情により、このクラスではメソッド単位で引数やデータ整合性のアサーションを行っている。
 * このクラスよりも下層のメソッドには原則としてアサーションが存在しない。全てのタイムラインの操作は
 * このクラスを介して行うこと。</p>
 */
class TimelineAccessor {
	/** BMS仕様 */
	private BmsSpec mSpec;
	/** メタ情報コンテナ */
	private MetaContainer mMetas = null;
	/** 編集モードかどうかを取得する関数 */
	private BooleanSupplier mFnIsEditMode;
	/** タイムライン */
	private Timeline<MeasureElement> mTl = null;

	/**
	 * 小節全体データの実装
	 */
	private static class BmsMeasureElement extends MeasureElement {
		/**
		 * コンストラクタ
		 * @param measure 小節番号
		 */
		BmsMeasureElement(int measure) {
			super(measure);
		}

		/** {@inheritDoc} */
		@Override
		protected MeasureTimeInfo onRecalculateTimeInfo(Object userParam) {
			// 小節に対して時間・BPMの情報を反映する
			var tla = (TimelineAccessor)userParam;
			return tla.calculateMeasureTimeInfo(getMeasure(), getTickCount());
		}
	}

	/**
	 * コンストラクタ
	 * @param spec BMS仕様
	 * @param metas メタ情報コンテナ
	 * @param fnIsEditMode 編集モードかどうかを取得する関数
	 */
	TimelineAccessor(BmsSpec spec, MetaContainer metas, BooleanSupplier fnIsEditMode) {
		mSpec = spec;
		mMetas = metas;
		mFnIsEditMode = fnIsEditMode;
		mTl = new Timeline<>(spec, BmsMeasureElement::new);
	}

	/**
	 * 指定チャンネル番号・小節番号に存在するデータの件数を取得する。
	 * @param channel チャンネル番号
	 * @param measure 小節番号
	 * @return
	 * @throws IllegalArgumentException 小節データ・ノートを登録できないチャンネル番号
	 * @throws IllegalArgumentException 小節番号が{@link BmsSpec#MEASURE_MIN}未満または{@link BmsSpec#MEASURE_MAX}超過
	 */
	int getChannelDataCount(int channel, int measure) {
		// アサーション
		assertChannelIndex(channel, 0);
		assertArgMeasureWithinRange(measure);

		// チャンネルに登録されているデータの件数を返す
		var channelSpec = mSpec.getChannel(channel);
		if (measure >= mTl.getMeasureCount()) {
			// データのない小節アクセス時は0とする
			return 0;
		} else if (channelSpec.isArrayType()) {
			// ノートの件数を返す
			return mTl.getNoteChannelDataCount(channel, measure);
		} else if (channelSpec.isValueType()) {
			// 小節データの件数を返す
			return mTl.getValueChannelDataCount(channel, measure);
		} else {
			// Don't care
			return 0;
		}
	}

	/**
	 * 指定の小節番号の位置に空の小節データを挿入する。
	 * <p>このメソッドによる挿入操作が完了すると、挿入位置(も含む)以降の小節データが挿入件数分だけ
	 * 後ろにずれることになる。</p>
	 * @param measureWhere 挿入位置の小節番号
	 * @param count 挿入小節数
	 * @throws IllegalStateException 動作モードが編集モードではない
	 * @throws IllegalArgumentException ノート・小節データが存在し得ない小節番号
	 * @throws IllegalArgumentException countがマイナス値
	 * @throws IllegalArgumentException 挿入処理により小節数が{@link BmsSpec#MEASURE_MAX_COUNT}を超過する
	 */
	void insert(int measureWhere, int count) {
		// アサーション
		assertIsEditMode();
		assertArgMeasureWithinRange(measureWhere, mTl.getMeasureCount());
		assertArg(count >= 0, "Argument 'count' is minus value. count=%d", count);
		var beforeMeasureCount = mTl.getMeasureCount();
		assertArgRange(beforeMeasureCount + count, 0, BmsSpec.MEASURE_MAX_COUNT, "beforeMeasureCount + count");

		// 挿入件数が0の場合は何もしない(ていうか呼ぶなや)
		if (count == 0) {
			return;
		}

		// 末尾小節よりも後ろを挿入位置にした場合は何もしない(なんで効果があると思ったの？？)
		if (measureWhere >= beforeMeasureCount) {
			return;
		}

		// 小節を挿入する
		mTl.insertMeasure(measureWhere, count);
	}

	/**
	 * 指定の小節番号の小節から指定個数の小節データを消去し、それ以降の小節データを詰める。
	 * @param measureWhere 消去開始位置の小節番号
	 * @param count 消去する小節データの件数
	 * @throws IllegalStateException 動作モードが編集モードではない
	 * @throws IllegalArgumentException ノート・小節データが存在し得ない小節番号
	 * @throws IllegalArgumentException countがマイナス値
	 * @throws IllegalArgumentException 存在する小節データを超えて小節データを消去しようとした
	 */
	void remove(int measureWhere, int count) {
		// アサーション
		assertIsEditMode();
		assertArgMeasureWithinRange(measureWhere, mTl.getMeasureCount() - 1);
		assertArg(count >= 0, "Argument 'count' is minus value. count=%d", count);
		assertArgRange(measureWhere + count, 0, mTl.getMeasureCount() - 1, "measureWhere + count");

		// 削除件数が0の場合は何もしない(ていうか呼ぶなや)
		if (count == 0) {
			return;
		}

		// 小節を消去する
		mTl.removeMeasure(measureWhere, count);
	}

	/**
	 * 指定位置にノートを登録する。同じ場所にノートが存在する場合は新しいノートで上書きする。
	 * @param channel チャンネル番号
	 * @param index チャンネルインデックス
	 * @param measure 小節番号
	 * @param tick 小節の刻み位置
	 * @param value ノートが持つ値
	 * @param createNote ノートオブジェクトを生成する関数
	 * @return 登録されたノートオブジェクト
	 * @throws IllegalStateException 動作モードが編集モードではない
	 * @throws IllegalArgumentException BMS仕様にないチャンネル番号
	 * @throws IndexOutOfBoundsException チャンネルインデックスがマイナス値
	 * @throws IndexOutOfBoundsException 重複不許可のチャンネルで0以外のインデックスを指定
	 * @throws IllegalArgumentException ノート・小節データが存在し得ない小節番号
	 * @throws IllegalArgumentException 小節番号がBMS仕様の許容範囲外
	 * @throws IllegalArgumentException 小節の刻み位置がマイナス値または当該小節の刻み数以上
	 * @throws IllegalArgumentException 指定チャンネルのデータ型が配列型ではない
	 * @throws IllegalArgumentException ノートの値に0を指定した
	 * @throws NullPointerException createNoteがnull
	 * @throws IllegalArgumentException createNoteの結果がnull
	 */
	@SuppressWarnings("unchecked")
	<T extends BmsNote> T putNote(int channel, int index, int measure, double tick, int value,
			Supplier<BmsNote> createNote) {
		// アサーション
		assertIsEditMode();
		assertChannelIndex(channel, index);
		assertPointAllowOverMeasureCount(measure, tick);
		assertChannelArrayType(channel);
		assertValue(value);
		assertArgNotNull(createNote, "createNote");

		// Creatorを使用して登録対象のNoteオブジェクトを生成する
		var note = createNote.get();
		assertArg(note != null, "Failed to create note.");

		// ノートを追加する
		note.setup(channel, index, measure, tick, value);
		mTl.putNote(note);

		return (T)note;
	}

	/**
	 * 指定位置のノートを消去する。
	 * @param channel チャンネル番号
	 * @param index チャンネルインデックス
	 * @param measure 小節番号
	 * @param tick 小節の刻み位置
	 * @return 消去した場合true、消去しなかった場合false
	 * @throws IllegalStateException 動作モードが編集モードではない
	 * @throws IllegalArgumentException BMS仕様にないチャンネル番号
	 * @throws IndexOutOfBoundsException チャンネルインデックスがマイナス値
	 * @throws IndexOutOfBoundsException 重複不許可のチャンネルで0以外のインデックスを指定
	 * @throws IllegalArgumentException 小節番号が{@link BmsSpec#MEASURE_MIN}未満または{@link BmsSpec#MEASURE_MAX}超過
	 * @throws IllegalArgumentException 小節の刻み位置がマイナス値または当該小節の刻み数以上
	 * @throws IllegalArgumentException 指定チャンネルのデータ型が配列型ではない
	 */
	boolean removeNote(int channel, int index, int measure, double tick) {
		// アサーション
		assertIsEditMode();
		assertChannelIndex(channel, index);
		assertPointAllowOverMeasureCount(measure, tick);
		assertChannelArrayType(channel);

		return mTl.removeNote(channel, index, measure, tick);
	}

	/**
	 * 指定範囲のノートを消去する。
	 * @param channelBegin 消去開始チャンネル番号
	 * @param channelEnd 消去終了チャンネル番号(このチャンネルを含まない)
	 * @param measureBegin 消去開始小節番号
	 * @param measureEnd 消去終了小節番号(この小節を含まない)
	 * @param isRemoveTarget ノート消去の是非を判定するテスター
	 * @return 消去されたノートの個数
	 * @throws IllegalStateException 動作モードが編集モードではない
	 * @throws IllegalArgumentException {@link TimelineAccessor#enumNotes}
	 * @throws NullPointerException isRemoveTargetがnull
	 */
	int removeNote(int channelBegin, int channelEnd, int measureBegin, int measureEnd,
			Predicate<BmsNote> isRemoveTarget) {
		// アサーション
		assertIsEditMode();
		assertChannelRange(channelBegin);
		assertEndChannelRange(channelEnd);
		assertPoint(measureBegin, 0);
		assertPointAllowTerm(measureEnd, 0);
		assertArgNotNull(isRemoveTarget, "isRemoveTarget");

		// 削除対象Noteをリストアップする
		final var removeTargets = new ArrayList<BmsNote>();
		enumNotes(channelBegin, channelEnd, measureBegin, 0, measureEnd, 0, note -> {
			if (isRemoveTarget.test(note)) { removeTargets.add(note); }
		});

		// リストアップしたNoteを削除する
		removeTargets.forEach(n -> removeNote(n.getChannel(), n.getIndex(), n.getMeasure(), n.getTick()));

		return removeTargets.size();
	}

	/**
	 * 指定小節のチャンネルに値型データを設定または破棄する。
	 * @param channel チャンネル番号
	 * @param index チャンネルインデックス
	 * @param measure 小節番号
	 * @param value 設定値。nullを指定した場合は破棄。
	 * @throws IllegalStateException 動作モードが編集モードではない
	 * @throws IllegalArgumentException BMS仕様にないチャンネル番号を指定した
	 * @throws IndexOutOfBoundsException チャンネルインデックスがマイナス値
	 * @throws IndexOutOfBoundsException 重複不許可のチャンネルで0以外のインデックスを指定した
	 * @throws IllegalArgumentException チャンネルのデータ型が値型ではない
	 * @throws ClassCastException valueをチャンネルのデータ型に変換できない
	 * @throws IllegalArgumentException 小節長に{@link BmsSpec#LENGTH_MIN}未満または{@link BmsSpec#LENGTH_MAX}超過の値を設定しようとした
	 */
	void setValue(int channel, int index, int measure, Object value) {
		// アサーション
		assertIsEditMode();
		assertChannelIndex(channel, index);
		assertArgMeasureWithinRange(measure);
		assertChannelNotArrayType(channel);

		// 値の設定か破棄かで処理を分岐
		if (value == null) {
			// 破棄の場合
			mTl.removeMeasureValue(channel, index, measure);
		} else {
			// 設定の場合
			var ch = mSpec.getChannel(channel);
			var convertedValue = ch.getType().cast(value);
			assertMeasureLength(ch, measure, value);
			mTl.putMeasureValue(channel, index, measure, convertedValue);
		}
	}

	/**
	 * 指定小節の小節データを取得する。
	 * @param channel チャンネル番号
	 * @param index チャンネルインデックス
	 * @param measure 小節番号
	 * @param requiredType 要求するデータ型。型変換不要の場合はnull。
	 * @param nullIfNotExist データ未登録の場合に初期値ではなくnullを返す場合はtrue
	 * @return 要求データ型に変換された小節データ。当該小節に小節データ未登録の場合はそのチャンネルの初期値を返す。
	 * @throws IllegalArgumentException BMS仕様にないチャンネル番号
	 * @throws IndexOutOfBoundsException チャンネルインデックスがマイナス値
	 * @throws IndexOutOfBoundsException 重複不許可のチャンネルで0以外のインデックスを指定
	 * @throws IllegalArgumentException 指定チャンネルのデータ型が値型ではない
	 * @throws IllegalArgumentException 小節番号が{@link BmsSpec#MEASURE_MIN}未満または{@link BmsSpec#MEASURE_MAX}超過
	 * @throws ClassCastException データを要求型に変換できない
	 */
	Object getValue(int channel, int index, int measure, BmsType requiredType, boolean nullIfNotExist) {
		// アサーション
		assertChannelIndex(channel, index);
		assertChannelNotArrayType(channel);
		assertArgMeasureWithinRange(measure);

		// 取得元の小節データを参照する
		var elem = (measure >= mTl.getMeasureCount()) ? null : mTl.getMeasureValue(channel, index, measure);
		if (elem == null) {
			// 小節データなし、または値未設定の場合は当該チャンネルの初期値を返す
			return nullIfNotExist ? null : mSpec.getChannel(channel).getDefaultValue();
		} else if (requiredType == null) {
			// 小節データありで型変換なし
			return elem.getValueAsObject();
		} else {
			// 小節データありで型変換ありの場合は要求型への変換を行い返す
			return requiredType.cast(elem.getValueAsObject());
		}
	}

	/**
	 * 指定範囲の小節データを時間・BPM再計算対象に設定する。
	 * @param first 対象の最小小節番号
	 * @param last 対象の最大小節番号
	 */
	void updateRecalcRange(int first, int last) {
		mTl.updateRecalcRange(first, last);
	}

	/**
	 * 時間・BPM再計算対象の小節データの再計算処理。
	 * @return 小節データの再計算を行った場合true
	 */
	boolean recalculateTime() {
		return mTl.recalculateTime(this);
	}

	/**
	 * 指定された2つのチャンネルのデータを入れ替える。
	 * @param channel1 入れ替え対象1のチャンネル番号
	 * @param index1 入れ替え対象1のチャンネルインデックス
	 * @param channel2 入れ替え対象2のチャンネル番号
	 * @param index2 入れ替え対象2のチャンネルインデックス
	 * @throws IllegalStateException 動作モードが編集モードではない
	 * @throws IllegalArgumentException BMS仕様にないチャンネル番号
	 * @throws IndexOutOfBoundsException チャンネルインデックスがマイナス値
	 * @throws IndexOutOfBoundsException 重複不許可のチャンネルで0以外のインデックスを指定
	 * @throws IllegalArgumentException 指定チャンネルが小節長変更・BPM変更・譜面停止のいずれか
	 * @throws IllegalArgumentException チャンネル1,2のデータ型が一致しない
	 */
	void swapChannel(int channel1, int index1, int channel2, int index2) {
		// アサーション
		assertIsEditMode();
		assertChannelIndex(channel1, index1);
		assertChannelIndex(channel2, index2);
		var ch1 = mSpec.getChannel(channel1);
		var ch2 = mSpec.getChannel(channel2);
		assertArg(!ch1.isRelatedToTime(), "%d: This channel cannot swap.", channel1);
		assertArg(!ch2.isRelatedToTime(), "%d: This channel cannot swap.", channel2);
		assertArg(ch1.getType() == ch2.getType(), "Type mismatch chnnels. ch1=%s, ch2=%s", channel1, channel2);

		// 同じチャンネル・インデックスを示している場合は何もしない(ていうか呼ぶなや)
		if ((channel1 == channel2) && (index1 == index2)) {
			return;
		}

		// 全小節のチャンネルを入れ替える
		if (ch1.isArrayType()) {
			// 配列型チャンネル同士の入れ替え
			mTl.swapNoteChannel(channel1, index1, channel2, index2);
		} else if (ch2.isValueType()) {
			// 値型チャンネル同士の入れ替え
			mTl.swapValueChannel(channel1, index1, channel2, index2);
		} else {
			// Don't care
		}
	}

	/**
	 * 指定位置のノートを取得する。
	 * @param channel チャンネル番号
	 * @param index チャンネルインデックス
	 * @param measure 小節番号
	 * @param tick 小節の刻み位置
	 * @return 指定位置のノートオブジェクト。ノートが存在しない場合はnull。
	 * @throws IllegalArgumentException BMS仕様にないチャンネル番号
	 * @throws IndexOutOfBoundsException チャンネルインデックスがマイナス値
	 * @throws IndexOutOfBoundsException 重複不許可のチャンネルで0以外のインデックスを指定
	 * @throws IllegalArgumentException 小節番号が{@link BmsSpec#MEASURE_MIN}未満または{@link BmsSpec#MEASURE_MAX}超過
	 * @throws IllegalArgumentException 小節の刻み位置がマイナス値または当該小節の刻み数以上
	 * @throws IllegalArgumentException 指定チャンネルのデータ型が配列型ではない
	 */
	BmsNote getNote(int channel, int index, int measure, double tick) {
		// アサーション
		assertChannelIndex(channel, index);
		assertPointAllowOverMeasureCount(measure, tick);
		assertChannelArrayType(channel);

		// ノートを取得する
		return mTl.getNote(channel, index, measure, tick);
	}

	/**
	 * 指定範囲のノートを取得する。
	 * @param channelBegin 取得開始チャンネル番号
	 * @param channelEnd 取得終了チャンネル番号(このチャンネルを含まない)
	 * @param measureBegin 取得開始小節番号
	 * @param tickBegin 取得開始刻み位置
	 * @param measureEnd 取得終了小節番号
	 * @param tickEnd 取得終了刻み位置(この位置を含まない)
	 * @param isCollect 当該ノートの取得是非を判定するテスター
	 * @return 取得したノートオブジェクトのリスト。取得結果が0件でもnullにはならない。
	 * @throws IllegalArgumentException {@link TimelineAccessor#enumNotes}
	 * @throws NullPointerException isCollectがnull
	 */
	List<BmsNote> listNotes(int channelBegin, int channelEnd,
			int measureBegin, double tickBegin, int measureEnd, double tickEnd, Predicate<BmsNote> isCollect) {
		// アサーション
		assertChannelRange(channelBegin);
		assertEndChannelRange(channelEnd);
		assertPoint(measureBegin, tickBegin);
		assertPointAllowTerm(measureEnd, tickEnd);
		assertArgNotNull(isCollect, "isCollect");

		// 指定範囲のノートリストを取得する
		return mTl.listNotes(channelBegin, channelEnd, measureBegin, tickBegin, measureEnd, tickEnd, isCollect);
	}

	/**
	 * 指定位置のノートを取得する。
	 * @param measure 取得対象の小節番号
	 * @param tick 取得対象の刻み位置
	 * @param isCollect ノートの取得是非を判定するテスター
	 * @return 取得したノートオブジェクトのリスト。取得結果が0件でもnullにはならない。
	 * @throws NullPointerException isCollectがnull
	 * @throws * {@link #enumNotes(int, int, BmsNote.Tester)}を参照
	 */
	List<BmsNote> listNotes(int measure, double tick, Predicate<BmsNote> isCollect) {
		assertPoint(measure, tick);
		assertArgNotNull(isCollect, "isCollect");

		// 終点を計算する
		var point = seekPoint(measure, tick, 1, new BmsPoint());
		var mEnd = point.getMeasure();
		var tEnd = point.getTick();

		// 指定位置のノートリストを取得する
		return mTl.listNotes(BmsSpec.CHANNEL_MIN, BmsSpec.CHANNEL_MAX + 1, measure, tick, mEnd, tEnd, isCollect);
	}

	/**
	 * ノートをリスト化
	 * @param channel チャンネル番号
	 * @param index チャンネルインデックス
	 * @param measure 小節番号
	 * @param outList リスト化したノートを格納するリストインスタンス
	 * @return パラメータで指定したリストインスタンス
	 */
	List<BmsNote> listNotes(int channel, int index, int measure, List<BmsNote> outList) {
		return mTl.listNotes(channel, index, measure, outList);
	}

	/**
	 * 指定位置から最も近い位置にあるノートを検索する。検索開始位置も検索対象になる。
	 * @param channel チャンネル番号
	 * @param index チャンネルインデックス
	 * @param measure 小節番号
	 * @param tick 小節の刻み位置
	 * @param direction 検索方向。1以上で前方検索、0以下で後方検索。
	 * @param inclusive 指定位置を検索対象に含めるかどうか
	 * @return 見つかったノートオブジェクト。見つからなかった場合はnull。
	 * @throws IllegalArgumentException BMS仕様にないチャンネル番号
	 * @throws IndexOutOfBoundsException チャンネルインデックスがマイナス値
	 * @throws IndexOutOfBoundsException 重複不許可のチャンネルで0以外のインデックスを指定
	 * @throws IllegalArgumentException 小節番号がマイナス値または小節数以上
	 * @throws IllegalArgumentException 小節の刻み位置がマイナス値または当該小節の刻み数以上
	 * @throws IllegalArgumentException 指定チャンネルのデータ型が配列型ではない
	 */
	BmsNote getNearerNote(int channel, int index, int measure, double tick, int direction, boolean inclusive) {
		// アサーション
		assertChannelIndex(channel, index);
		assertPoint(measure, tick);
		assertChannelArrayType(channel);

		// 近隣を検索する処理
		if (direction > 0) {
			return mTl.getNextNote(channel, index, measure, tick, inclusive);
		} else {
			return mTl.getPreviousNote(channel, index, measure, tick, inclusive);
		}
	}

	/**
	 * テスターの検査を通過するノートの数を取得する。
	 * @param channelBegin カウント対象の開始チャンネル番号
	 * @param channelEnd カウント対象の終了チャンネル番号(このチャンネルを含まない)
	 * @param measureBegin カウント対象の開始小節番号
	 * @param tickBegin カウント対象の開始刻み位置
	 * @param measureEnd カウント対象の終了小節番号
	 * @param tickEnd カウント対象の終了刻み位置(この位置を含まない)
	 * @param isCounting ノートを検査するテスター
	 * @return テストを通過したノートの数
	 * @throws IllegalArgumentException {@link TimelineAccessor#enumNotes}
	 */
	int countNotes(int channelBegin, int channelEnd, int measureBegin,
			double tickBegin, int measureEnd, double tickEnd, Predicate<BmsNote> isCounting) {
		// アサーション
		assertChannelRange(channelBegin);
		assertEndChannelRange(channelEnd);
		assertPoint(measureBegin, tickBegin);
		assertPointAllowTerm(measureEnd, tickEnd);
		assertArgNotNull(isCounting, "isCounting");

		return mTl.countNotes(channelBegin, channelEnd, measureBegin, tickBegin, measureEnd, tickEnd, isCounting);
	}

	/**
	 * ノートの数を取得する。
	 * @param measure カウント対象の小節番号
	 * @param tick カウント対象の刻み位置
	 * @param isCounting カウント対象のノートを判定するテスター
	 * @return 判定に合格したノートの数
	 * @throws NullPointerException isCountingがnull
	 * @throws * {@link #enumNotes(int, int, BmsNote.Tester)}を参照
	 */
	int countNotes(int measure, double tick, Predicate<BmsNote> isCounting) {
		assertPoint(measure, tick);
		assertArgNotNull(isCounting, "isCounting");

		// 終点を計算する
		var point = seekPoint(measure, tick, 1, new BmsPoint());
		var mEnd = point.getMeasure();
		var tEnd = point.getTick();

		// ノートの数を取得する
		return mTl.countNotes(BmsSpec.CHANNEL_MIN, BmsSpec.CHANNEL_MAX + 1, measure, tick, mEnd, tEnd, isCounting);
	}

	/**
	 * 指定範囲のノートを列挙する。
	 * @param channelBegin 列挙開始チャンネル番号
	 * @param channelEnd 列挙終了チャンネル番号(このチャンネルを含まない)
	 * @param measureBegin 列挙開始小節番号
	 * @param tickBegin 列挙開始刻み位置
	 * @param measureEnd 列挙終了小節番号
	 * @param tickEnd 列挙終了刻み位置(この位置を含まない)
	 * @param enumNote 列挙されたノートの通知を受ける関数
	 * @throws IllegalArgumentException channelBeginが小節データ・ノートを登録できないチャンネル番号
	 * @throws IllegalArgumentException channelEnd - 1が小節データ・ノートを登録できないチャンネル番号
	 * @throws IllegalArgumentException measureBeginがノート・小節データの存在し得ない小節番号
	 * @throws IllegalArgumentException tickBeginがマイナス値または当該小節の刻み数以上
	 * @throws IllegalArgumentException measureEndがノート・小節データが存在し得ない小節番号
	 * @throws IllegalArgumentException tickEndがマイナス値、当該小節の刻み数以上、または最終小節+1の時に0以外
	 * @throws NullPointerException enumNoteがnull
	 */
	void enumNotes(int channelBegin, int channelEnd,
			int measureBegin, double tickBegin, int measureEnd, double tickEnd, Consumer<BmsNote> enumNote) {
		// アサーション
		assertChannelRange(channelBegin);
		assertEndChannelRange(channelEnd);
		if (mTl.getMeasureCount() > 0) {
			assertPoint(measureBegin, tickBegin);
			assertPointAllowTerm(measureEnd, tickEnd);
		}
		assertArgNotNull(enumNote, "enumNote");

		// 列挙範囲を決定する
		mTl.enumNotes(channelBegin, channelEnd, measureBegin, tickBegin, measureEnd, tickEnd, enumNote);
	}

	/**
	 * 指定楽曲位置のノートを全て列挙する。
	 * @param measure 小節番号
	 * @param tick 刻み位置
	 * @param enumNote 列挙されたノートの通知を受ける関数
	 * @throws IllegalArgumentException measureがマイナス値または小節数以上
	 * @throws IllegalArgumentException tickがマイナス値または当該小節の刻み数以上
	 * @throws NullPointerException enumNoteがnull
	 */
	void enumNotes(int measure, double tick, Consumer<BmsNote> enumNote) {
		assertPoint(measure, tick);
		assertArgNotNull(enumNote, "enumNote");

		// 終点を計算する
		var point = seekPoint(measure, tick, 1, new BmsPoint());
		var mEnd = point.getMeasure();
		var tEnd = point.getTick();

		// 引数で指定位置のノートのみを列挙する
		enumNotes(BmsSpec.CHANNEL_MIN, BmsSpec.CHANNEL_MAX + 1, measure, tick, mEnd, tEnd, enumNote);
	}

	/**
	 * 楽曲位置をオフセットする。
	 * <p>前方移動ではMeasure=小節データ数,Tick=0、後方移動ではMeasure=0,Tick=0を超える位置には移動しない。</p>
	 * @param measure 基点の小節番号
	 * @param tick 基点の刻み位置
	 * @param offsetTick オフセットする刻み数。プラス値で前方、マイナス値で後方へオフセットする。
	 * @param outPoint オフセット後の楽曲位置を格納する楽曲位置オブジェクト
	 * @return 引数で指定した楽曲位置オブジェクト
	 * @throws IllegalArgumentException 小節番号がマイナス値または小節数以上
	 * @throws IllegalArgumentException 小節の刻み位置がマイナス値または当該小節の刻み数以上
	 */
	BmsPoint seekPoint(int measure, double tick, double offsetTick, BmsPoint outPoint) {
		// アサーション
		assertPoint(measure, tick);

		// 進行方向により処理を振り分ける
		if (offsetTick >= 0) {
			// 前進
			return forwardPoint(measure, tick, offsetTick, outPoint);
		} else {
			// 後退
			return backwardPoint(measure, tick, -offsetTick, outPoint);
		}
	}

	/**
	 * 指定楽曲位置より後でノートの存在する楽曲位置を検索する。
	 * @param measure 検索を開始する小節番号
	 * @param tick 検索を開始する刻み位置
	 * @param inclusiveFrom 指定楽曲位置を検索対象に含めるかどうか
	 * @param chTester 検索対象のチャンネルを判定するテスター
	 * @param outPoint 検索した楽曲位置を格納する{@link BmsPoint}の参照
	 * @return outPointと同じ参照
	 * @throws IllegalArgumentException 小節番号がマイナス値または小節数以上
	 * @throws IllegalArgumentException 小節の刻み位置がマイナス値または当該小節の刻み数以上
	 * @throws NullPointerException chTesterがnull
	 * @throws NullPointerException outPointがnull
	 */
	BmsPoint seekNextPoint(int measure, double tick, boolean inclusiveFrom, IntPredicate chTester,
			BmsPoint outPoint) {
		assertArgNotNull(chTester, "chTester");
		assertArgNotNull(outPoint, "outPoint");
		if (mTl.getMeasureCount() > 0) {
			assertPoint(measure, tick);
		} else {
			outPoint.setMeasure(0);
			outPoint.setTick(0);
			return outPoint;
		}

		// 指定小節から進行方向へ全ての小節を走査する
		return mTl.seekNextPoint(measure, tick, inclusiveFrom, chTester, outPoint);
	}

	/**
	 * タイムラインの指定楽曲位置を走査するストリームを返す
	 * @param measure 走査楽曲位置の小節番号
	 * @param tick 走査楽曲位置の小節の刻み位置
	 * @return タイムラインの指定楽曲位置を走査するストリーム
	 * @throws IllegalArgumentException 楽曲位置の小節番号がマイナス値
	 * @throws IllegalArgumentException 楽曲位置の小節の刻み位置が{@link BmsSpec#TICK_MIN}以外の時、小節番号が小節数以上
	 * @throws IllegalArgumentException 楽曲位置の小節の刻み位置が{@link BmsSpec#TICK_MIN}未満、または{@link BmsSpec#TICK_MAX}超過
	 */
	Stream<BmsTimelineElement> timeline(int measure, double tick) {
		if ((mTl.getMeasureCount() == 0) && (measure == BmsSpec.MEASURE_MIN) && (tick == BmsSpec.TICK_MIN)) {
			// 空タイムラインでタイムライン先頭指定時は空ストリームを返す
			return Stream.empty();
		} else {
			// 楽曲位置のピンポイント指定でストリームを返す
			assertPoint(measure, tick);
			return mTl.timeline(measure, tick, measure, Math.nextUp(tick));
		}
	}

	/**
	 * タイムラインの指定楽曲位置の範囲を走査するストリームを返す
	 * @param measureBegin 走査開始楽曲位置の小節番号
	 * @param tickBegin 走査開始楽曲位置の小節の刻み位置
	 * @param measureEnd 走査終了楽曲位置の小節番号
	 * @param tickEnd 走査終了楽曲位置の小節の刻み位置(この位置を含まない)
	 * @return タイムラインの指定楽曲位置の範囲を走査するストリーム
	 * @throws IllegalArgumentException 走査開始/終了楽曲位置の小節番号がマイナス値
	 * @throws IllegalArgumentException 走査楽曲位置の小節の刻み位置が{@link BmsSpec#TICK_MIN}以外の時、小節番号が小節数以上
	 * @throws IllegalArgumentException 走査楽曲位置の小節の刻み位置が{@link BmsSpec#TICK_MIN}の時、小節番号が小節数超過
	 * @throws IllegalArgumentException 走査開始/終了楽曲位置の小節の刻み位置が{@link BmsSpec#TICK_MIN}未満、または{@link BmsSpec#TICK_MAX}超過
	 * @throws IllegalArgumentException 走査終了楽曲位置が走査開始楽曲位置と同じまたは手前の楽曲位置を示している
	 */
	Stream<BmsTimelineElement> timeline(int measureBegin, double tickBegin, int measureEnd, double tickEnd) {
		// 空タイムラインで走査開始/終了楽曲位置がタイムライン先頭を指している場合は空ストリームを返す
		if (mTl.getMeasureCount() == 0) {
			if ((measureBegin == BmsSpec.MEASURE_MIN) && (tickBegin == BmsSpec.TICK_MIN) &&
					(measureEnd == BmsSpec.MEASURE_MIN) && (tickEnd == BmsSpec.TICK_MIN)) {
				return Stream.empty();
			}
		}

		// アサーション
		assertPoint(measureBegin, tickBegin);
		assertPointAllowTerm(measureEnd, tickEnd);
		if (BmsAt.compare2(measureBegin, tickBegin, measureEnd, tickEnd) >= 0) {
			var msg = String.format(
					"Wrong point range. beginAt=%s, endAt=%s",
					BmsPoint.of(measureBegin, tickBegin), BmsPoint.of(measureEnd, tickEnd));
			throw new IllegalArgumentException(msg);
		}

		// タイムライン走査ストリームを返す
		return mTl.timeline(measureBegin, tickBegin, measureEnd, tickEnd);
	}

	/**
	 * 指定したメタ情報要素をBMSコンテンツに追加
	 * @param timeline タイムライン要素
	 * @throws IllegalStateException 動作モードが編集モードではない
	 * @throws NullPointerException timelineがnull
	 */
	void putTimeline(BmsTimelineElement timeline) {
		assertIsEditMode();
		assertArgNotNull(timeline, "timeline");
		var channel = timeline.getChannel();
		var index = timeline.getIndex();
		var measure = timeline.getMeasure();
		var tick = timeline.getTick();
		if (timeline.isNoteElement()) {
			// タイムライン要素がノートの場合、指定された要素をノートオブジェクトとしてそのまま追加する
			// ノートは一度生成されると変更されない想定のため新しいインスタンスは生成しないものとする
			assertChannelIndex(channel, index);
			assertPointAllowOverMeasureCount(measure, tick);
			assertChannelArrayType(channel);
			assertValue((int)timeline.getValueAsLong());
			mTl.putNote((BmsNote)timeline);
		} else if (timeline.isMeasureValueElement()) {
			// タイムライン要素が小節データの場合、データをそのまま追加する
			// 要素の中に格納されているデータは全て変更不可(Immutable)の前提で取り扱う
			assertChannelIndex(channel, index);
			assertArgMeasureWithinRange(measure);
			assertChannelNotArrayType(channel);
			var ch = mSpec.getChannel(channel);
			var value = ch.getType().cast(timeline.getValueAsObject());
			assertMeasureLength(ch, measure, value);
			mTl.putMeasureValue(channel, index, measure, value);
		} else {
			// 上記以外の小節線などのタイムライン要素は設定処理を行わない
			// Do nothing
		}
	}

	/**
	 * 楽曲位置を前方へオフセットする。
	 * @param measure 基点の小節番号
	 * @param tick 基点の刻み位置
	 * @param offset オフセットする刻み数
	 * @param outPoint オフセット後の楽曲位置を格納する楽曲位置オブジェクト
	 * @return 引数で指定した楽曲位置オブジェクト
	 */
	private BmsPoint forwardPoint(int measure, double tick, double offset, BmsPoint outPoint) {
		var curMeasure = measure;
		var curTick = tick;
		var remain = offset;
		while ((remain > 0) && (curMeasure < mTl.getMeasureCount())) {
			// 刻み位置の前進処理
			var measureData = mTl.getMeasureData(curMeasure);
			var tickCount = measureData.getTickCount();
			var forward = Math.min(tickCount - curTick, remain);
			remain -= forward;
			curTick += forward;

			// 当該小節の終端に到達した場合は次の小節へ移動する
			if (curTick >= tickCount) {
				curMeasure++;
				curTick = 0;
			}
		}

		outPoint.set(curMeasure, curTick);
		return outPoint;
	}

	/**
	 * 楽曲位置を後方へオフセットする。
	 * @param measure 基点の小節番号
	 * @param tick 基点の刻み位置
	 * @param offset オフセットする刻み数
	 * @param outPoint オフセット後の楽曲位置を格納する楽曲位置オブジェクト
	 * @return 引数で指定した楽曲位置オブジェクト
	 */
	private BmsPoint backwardPoint(int measure, double tick, double offset, BmsPoint outPoint) {
		var curMeasure = measure;
		var curTick = tick;
		var remain = offset;
		while (remain > 0) {
			// 当該小節の終端に到達している場合は次の小節へ移動する
			if (curTick <= 0) {
				if (curMeasure <= 0) {
					// 小節の先端に到達済み
					break;
				} else {
					// 前の小節へ移動する
					curMeasure--;
					curTick = mTl.getMeasureTickCount(curMeasure);
				}
			}

			// 刻み位置の後退処理
			var backward = Math.min(curTick, remain);
			remain -= backward;
			curTick -= backward;
		}

		outPoint.set(curMeasure, curTick);
		return outPoint;
	}

	/**
	 * テスターの検査が通過する最初のノートを検索する。
	 * @param measureFrom 検索開始小節番号
	 * @param tickFrom 検索開始刻み位置
	 * @param judge ノートを検査するテスター
	 * @return 検索で見つかった最初のノート。見つからなかった場合はnull。
	 * @throws IllegalArgumentException ノート・小節データが存在し得ない小節番号
	 * @throws IllegalArgumentException 小節の刻み位置がマイナス値または当該小節の刻み数以上
	 * @throws NullPointerException judgeがnull
	 */
	BmsNote pointOf(int measureFrom, double tickFrom, Predicate<BmsNote> judge) {
		assertPoint(measureFrom, tickFrom);
		assertArgNotNull(judge, "judge");

		// 判定処理がOKを返すNoteを探す処理
		return mTl.pointOf(measureFrom, tickFrom, judge);
	}

	/**
	 * 小節データ数を取得する。
	 * @return 小節データ数
	 */
	int getCount() {
		return mTl.getMeasureCount();
	}

	/**
	 * 指定小節番号の小節の刻み数を取得する。
	 * @param measure 小節番号
	 * @return 当該小節の刻み数
	 * @throws IllegalArgumentException 小節番号が{@link BmsSpec#MEASURE_MIN}未満または{@link BmsSpec#MEASURE_MAX}超過
	 */
	double getTickCount(int measure) {
		assertArgMeasureWithinRange(measure);
		return mTl.getMeasureTickCount(measure);
	}

	/**
	 * 時間(sec)を楽曲位置に変換する。
	 * <p>時間(sec)が総時間を超える場合はMeasure=小節数,Tick=0を返す。</p>
	 * @param timeSec 時間(sec)
	 * @param outPoint 変換後の楽曲位置を格納する楽曲位置オブジェクト
	 * @return 引数で指定した楽曲位置オブジェクト
	 * @throws IllegalStateException 動作モードが参照モードではない
	 * @throws IllegalArgumentException 指定時間がマイナス値
	 */
	BmsPoint timeToPoint(double timeSec, BmsPoint outPoint) {
		// アサーション
		assertIsReferenceMode();
		assertArg(timeSec >= 0.0, "Argument 'timeSec' is minus value. actual=%f", timeSec);

		// 当該時間が何小節目の範囲にあるかを検出する
		var measureCount = mTl.getMeasureCount();
		MeasureElement measureElem = null;
		for (int i = 0; i < measureCount; i++) {
			var m = mTl.getMeasureData(i);
			var baseTime = m.getBaseTime();
			if ((timeSec >= baseTime) && (timeSec < (baseTime + m.getLength()))) {
				// 当該時間の収まる小節を発見した
				measureElem = m;
				break;
			}
		}
		if (measureElem == null) {
			// 範囲外の時間を入力した場合は最終小節+1, tikc=0の位置を返す
			outPoint.set(measureCount, 0);
			return outPoint;
		}

		// データなし小節の場合の処理
		var tickCount = measureElem.getTickCount();
		var pointTime = timeSec - measureElem.getBaseTime();
		var measure = measureElem.getMeasure();
		if (mTl.isMeasureEmptyNotes(measure)) {
			if (tickCount >= 1.0) {
				// 通常の長さの小節の場合
				var tick = Utility.computeTick(pointTime, measureElem.getBeginBpm());
				outPoint.set(measure, tick);
				return outPoint;
			} else {
				// 計算上の刻み数が1を下回る極小小節の場合
				var tick = pointTime / measureElem.getLength();
				outPoint.set(measure, tick);
				return outPoint;
			}
		}

		// データあり小節の場合の処理
		var tickFindStart = 0.0;
		var currentTick = 0.0;
		var tickCur2Pt = 0.0;
		var currentBpm = measureElem.getBeginBpm();
		var currentTime = 0.0;
		var areaTime = 0.0;

		while (currentTick < tickCount) {
			// 現在位置を基点にして次のBPM変更Noteを取得する
			var nextBpmNote = getNextBpmNoteInMeasure(measure, tickFindStart);
			var nextBpmTick = (nextBpmNote == null) ? tickCount : nextBpmNote.getTick();

			// 現在位置を基点にして次の譜面停止Noteを取得する
			var nextStopNote = getNextStopNoteInMeasure(measure, tickFindStart);
			var nextStopTick = (nextStopNote == null) ? tickCount : nextStopNote.getTick();

			// 時間計算処理
			if ((nextBpmTick <= nextStopTick) && (nextBpmNote != null)) {
				// BPM変更を検出した場合は、現在位置からBPM変更位置までを現在BPMで時間計算する
				areaTime = Utility.computeTime(nextBpmTick - currentTick, currentBpm);
				if ((pointTime >= currentTime) && (pointTime < (currentTime + areaTime))) {
					// 指定位置までの刻み数を計算する
					tickCur2Pt = Utility.computeTick(pointTime - currentTime, currentBpm);
					break;
				} else {
					// BPMの現在値を更新する
					currentBpm = mMetas.getIndexedMetaBpm(nextBpmNote, currentBpm);
					currentTime += areaTime;
				}

				// 同じ位置に譜面停止が存在する場合は、更新後のBPMで譜面停止分の時間を計算する
				if ((nextBpmTick == nextStopTick) && (nextStopNote != null)) {
					var stopTick = mMetas.getIndexedMetaStop(nextStopNote, 0);
					areaTime = Utility.computeTime(stopTick, currentBpm);
					if ((pointTime >= currentTime) && (pointTime < (currentTime + areaTime))) {
						// 指定位置までの刻み数を計算する
						tickCur2Pt = Utility.computeTick(pointTime - currentTime, currentBpm);
						break;
					} else {
						currentTime += areaTime;
					}
				}

				// 現在位置を更新する
				currentTick = nextBpmTick;
			} else if (nextStopNote != null) {
				// 譜面停止を検出した場合は、現在位置から譜面停止位置までを現在BPMで時間計算する
				areaTime = Utility.computeTime(nextStopTick - currentTick, currentBpm);
				if ((pointTime >= currentTime) && (pointTime < (currentTime + areaTime))) {
					// 指定位置までの刻み数を計算する
					tickCur2Pt = Utility.computeTick(pointTime - currentTime, currentBpm);
					break;
				} else {
					currentTime += areaTime;
				}

				// 譜面停止分の時間を現在BPMで計算する
				var stopTick = mMetas.getIndexedMetaStop(nextStopNote, 0);
				areaTime = Utility.computeTime(stopTick, currentBpm);
				if ((pointTime >= currentTime) && (pointTime < (currentTime + areaTime))) {
					// 指定位置までの刻み数を計算する
					tickCur2Pt = Utility.computeTick(pointTime - currentTime, currentBpm);
					break;
				} else {
					currentTime += areaTime;
				}

				// 現在位置を更新する
				currentTick = nextStopTick;
			} else {
				// 現在位置からBPM変更・譜面停止のいずれも見つからなかった場合は、現在位置から小節の最後まで時間計算する
				areaTime = Utility.computeTime(tickCount - currentTick, currentBpm);
				if ((pointTime >= currentTime) && (pointTime < (currentTime + areaTime))) {
					// 指定位置までの刻み数を計算する
					if (tickCount >= 1.0) {
						// 通常の長さの小節の場合
						tickCur2Pt = Utility.computeTick(pointTime - currentTime, currentBpm);
					} else {
						// 計算上の刻み数が1を下回る極小小節の場合
						tickCur2Pt = pointTime / measureElem.getLength();
					}
					break;
				} else {
					currentTime += areaTime;
				}

				// 現在位置を更新する
				currentTick = tickCount;
			}

			// 次の検索開始位置を設定する
			tickFindStart = Math.nextUp(currentTick);
		}

		outPoint.set(measure, (double)currentTick + tickCur2Pt);
		return outPoint;
	}

	/**
	 * 楽曲位置を時間(sec)に変換する。
	 * @param measure 小節番号
	 * @param tick 小節の刻み位置
	 * @return 時間(sec)
	 * @throws IllegalStateException 動作モードが参照モードではない
	 * @throws IllegalArgumentException 小節番号がマイナス値、小節数以上、または小節数と同値で刻み位置が0以外
	 * @throws IllegalArgumentException 小節の刻み位置がマイナス値または当該小節の刻み数以上
	 */
	double pointToTime(int measure, double tick) {
		// アサーション
		assertIsReferenceMode();
		assertPointAllowTerm(measure, tick);

		// 小節の末端の場合は総時間を返す
		var measureCount = mTl.getMeasureCount();
		if (measure == measureCount) {
			if (measureCount == 0) {
				// データなし
				return 0.0;
			} else {
				// 最終小節の開始時間＋長さを返す
				var lastMeasure = mTl.getMeasureData(measureCount - 1);
				return lastMeasure.getBaseTime() + lastMeasure.getLength();
			}
		}

		// 小節・刻み位置から時間への変換を行う
		return mTl.getMeasureData(measure).computeTime(tick);
	}

	/**
	 * 指定ノートに該当するメタ情報の値を取得する。
	 * @param channel チャンネル番号
	 * @param index チャンネルインデックス
	 * @param measure 小節番号
	 * @param tick 小節の刻み位置
	 * @return ノートに該当するメタ情報の値。チャンネルに参照先メタ情報のない場合はノートの値をINTEGERで返す。
	 * @throws IllegalArgumentException BMS仕様にないチャンネル番号
	 * @throws IndexOutOfBoundsException チャンネルインデックスがマイナス値
	 * @throws IndexOutOfBoundsException 重複不許可のチャンネルで0以外のインデックスを指定
	 * @throws IllegalArgumentException 小節番号が{@link BmsSpec#MEASURE_MIN}未満または{@link BmsSpec#MEASURE_MAX}超過
	 * @throws IllegalArgumentException 小節の刻み位置がマイナス値または当該小節の刻み数以上
	 * @throws IllegalArgumentException 指定チャンネルのデータ型が配列型ではない
	 */
	Object getResolvedNoteValue(int channel, int index, int measure, double tick) {
		var note = getNote(channel, index, measure, tick);
		if (note != null) {
			// ノートを取得できた場合はそのノートの値を使用してメタ情報の値を抽出する
			return getResolvedNoteValueCore(note.getChannel(), note.getValue());
		} else {
			// ノートを取得できなかった場合は指定チャンネルの初期値を返す
			var info = mMetas.getReferenceMeta(channel);
			return (info == null) ? (Long)0L : info.meta.getDefaultValue();
		}
	}

	/**
	 * 指定ノートに該当するメタ情報の値を取得する。
	 * @param note ノート
	 * @return ノートに該当するメタ情報の値。チャンネルに参照先メタ情報のない場合はノートの値をINTEGERで返す。
	 * @throws NullPointerException noteがnull
	 */
	Object getResolvedNoteValue(BmsNote note) {
		assertArgNotNull(note, "note");
		return getResolvedNoteValueCore(note.getChannel(), note.getValue());
	}

	/**
	 * 指定小節の基準時間(sec)、小節時間(sec)、開始時BPM、終了時BPM、楽曲位置ごとの時間関連情報を算出する。
	 * <p>このメソッドでは当該小節の諸々の情報を計算するにあたり、指定小節の1つ前の小節の情報を参照するため、
	 * 前の小節の情報は予め計算しておくこと。</p>
	 * @param measure 小節番号
	 * @param tickCalcTo どの刻み位置までの時間を計算するか(この位置は含まない)
	 * @return 小節が保有する時間関連情報
	 */
	MeasureTimeInfo calculateMeasureTimeInfo(int measure, double tickCalcTo) {
		// 指定小節における基準時間、初期BPMを決定する
		var baseTimeSec = 0.0;
		var currentBpm = 0.0;
		var prevMeasureData = (measure == 0) ? null : mTl.getMeasureData(measure - 1);
		if (prevMeasureData == null) {
			// 先頭小節の場合
			baseTimeSec = 0.0;
			currentBpm = mMetas.getInitialBpm();
		} else {
			// 先頭小節以降の場合
			baseTimeSec = prevMeasureData.getBaseTime() + prevMeasureData.getLength();
			currentBpm = prevMeasureData.getEndBpm();
		}

		// 小節長の倍率を計算する
		var lengthRatio = 1.0;
		var lengthChannel = mSpec.getLengthChannel();
		if (lengthChannel != null) {
			lengthRatio = (double)getValue(lengthChannel.getNumber(), 0, measure, BmsType.FLOAT, false);
		}

		// データのない小節では単純に小節の長さと小節開始時のBPMから時間を算出する
		var timeInfo = new MeasureTimeInfo();
		var tickCount = BmsSpec.computeTickCount(lengthRatio, true);
		var actualTickCount = BmsSpec.computeTickCount(lengthRatio, false);
		if (mTl.isMeasureEmptyNotes(measure)) {
			timeInfo.baseTime = baseTimeSec;
			timeInfo.length = Utility.computeTime(Math.min(tickCalcTo, actualTickCount), currentBpm);
			timeInfo.beginBpm = currentBpm;
			timeInfo.endBpm = currentBpm;
			return timeInfo;
		}

		// 譜面データから具体的な時間を計算する
		var currentTick = 0.0;
		var beginBpm = currentBpm;
		var totalTime = 0.0;

		var tickFindStart = 0.0;
		var tickEnd = Math.min(tickCalcTo, tickCount);
		var actualTickEnd = Math.min(tickCalcTo, actualTickCount);
		while (currentTick < tickEnd) {
			// 現在位置を基点にして次のBPM変更Noteを取得する
			var nextBpmNote = getNextBpmNoteInMeasure(measure, tickFindStart);
			var nextBpmTick = (nextBpmNote == null) ? tickEnd : nextBpmNote.getTick();

			// 現在位置を基点にして次の譜面停止Noteを取得する
			var nextStopNote = getNextStopNoteInMeasure(measure, tickFindStart);
			var nextStopTick = (nextStopNote == null) ? tickEnd : nextStopNote.getTick();

			// 時間計算処理
			if ((nextBpmTick <= nextStopTick) && (nextBpmNote != null) && (nextBpmTick < tickEnd)) {
				// BPM変更を検出した場合は、現在位置からBPM変更位置までを現在BPMで時間計算する
				totalTime += Utility.computeTime(nextBpmTick - currentTick, currentBpm);
				currentBpm = mMetas.getIndexedMetaBpm(nextBpmNote, currentBpm);
				var nodeActualTime = baseTimeSec + totalTime;
				var nodeCurrentBpm = currentBpm;

				// 同じ位置に譜面停止が存在する場合は、更新後のBPMで譜面停止分の時間を計算する
				var nodeStopTime = 0.0;
				if ((nextBpmTick == nextStopTick) && (nextStopNote != null)) {
					var stopTick = mMetas.getIndexedMetaStop(nextStopNote, 0);
					nodeStopTime = Utility.computeTime(stopTick, currentBpm);
					totalTime += nodeStopTime;
				}

				// BPM変更位置の時間情報を登録する
				timeInfo.addTimeNode(nextBpmTick, nodeActualTime, nodeCurrentBpm, nodeStopTime);

				// 現在位置を更新する
				currentTick = nextBpmTick;
			} else if ((nextStopNote != null) && (nextStopTick < tickEnd)) {
				// 譜面停止を検出した場合は、現在位置から譜面停止位置までを現在BPMで時間計算する
				// 但し、刻み位置が走査停止位置と同じ以上の場合はこの限りではない
				totalTime += Utility.computeTime(nextStopTick - currentTick, currentBpm);
				var nodeActualTime = baseTimeSec + totalTime;
				var nodeCurrentBpm = currentBpm;

				// 譜面停止分の時間を現在BPMで計算する
				var stopTick = mMetas.getIndexedMetaStop(nextStopNote, 0);
				var nodeStopTime = Utility.computeTime(stopTick, currentBpm);
				totalTime += nodeStopTime;

				// 譜面停止位置の時間情報を登録する
				timeInfo.addTimeNode(nextStopTick, nodeActualTime, nodeCurrentBpm, nodeStopTime);

				// 現在位置を更新する
				currentTick = nextStopTick;
			} else {
				// 現在位置からBPM変更・譜面停止のいずれも見つからなかった場合は、現在位置から計算位置まで時間計算する
				totalTime += Utility.computeTime(actualTickEnd - currentTick, currentBpm);
				currentTick = tickEnd;
			}

			// 次の検索開始位置を設定する
			tickFindStart = Math.nextUp(currentTick);
		}

		// 計算結果を出力する
		timeInfo.baseTime = baseTimeSec;
		timeInfo.length = totalTime;
		timeInfo.beginBpm = beginBpm;
		timeInfo.endBpm = currentBpm;
		return timeInfo;
	}

	/**
	 * 指定小節内で刻み位置より後ろにあるBPM変更ノート取得
	 * @param measure 小節番号
	 * @param tickFrom 検索開始小節の刻み位置
	 * @return 次のBPM変更ノート、なければnull
	 */
	private BmsNote getNextBpmNoteInMeasure(int measure, double tickFrom) {
		// 指定楽曲位置より後ろで同一小節内にあるBPM変更ノートを返す
		var bpmNote = (BmsNote)null;
		var channelCount = mSpec.getBpmChannelCount();
		for (var i = 0; i < channelCount; i++) {
			var bpmChannel = mSpec.getBpmChannel(i, true);
			var nextNote = mTl.getNextNote(bpmChannel.getNumber(), 0, measure, tickFrom, true);
			if ((nextNote != null) && (nextNote.getMeasure() == measure)) {
				// 同一小節内のBPM変更ノートを発見した
				if (bpmNote == null) {
					// 最初に見つかったノート
					bpmNote = nextNote;
				} else {
					// 2個目以降に見つかった場合、より手前で見つかったノートを採用する
					// 同一刻み位置だった場合は後で追加されたチャンネルを優先して採用する
					bpmNote = (nextNote.getTick() <= bpmNote.getTick()) ? nextNote : bpmNote;
				}
			}
		}
		return bpmNote;
	}

	/**
	 * 指定小節内で刻み位置より後ろにある譜面停止ノート取得
	 * @param measure 小節番号
	 * @param tickFrom 検索開始小節の刻み位置
	 * @return 次の譜面停止ノート、なければnull
	 */
	private BmsNote getNextStopNoteInMeasure(int measure, double tickFrom) {
		// 指定楽曲位置より後ろで同一小節内にある譜面停止ノートを返す
		var stopNote = (BmsNote)null;
		var channelCount = mSpec.getStopChannelCount();
		for (var i = 0; i < channelCount; i++) {
			var stopChannel = mSpec.getStopChannel(i, true);
			var nextNote = mTl.getNextNote(stopChannel.getNumber(), 0, measure, tickFrom, true);
			if ((nextNote != null) && (nextNote.getMeasure() == measure)) {
				// 同一小節内の譜面停止ノートを発見した
				if (stopNote == null) {
					// 最初に見つかったノート
					stopNote = nextNote;
				} else {
					// 2個目以降に見つかった場合、より手前で見つかったノートを採用する
					// 同一刻み位置だった場合は後で追加されたチャンネルを優先して採用する
					stopNote = (nextNote.getTick() <= stopNote.getTick()) ? nextNote : stopNote;
				}
			}
		}
		return stopNote;
	}

	/**
	 * 指定ノートに該当するメタ情報の値を取得する。
	 * @param channel チャンネル番号
	 * @param value ノートの値
	 * @return 指定ノートに該当するメタ情報の値。参照先メタ情報の該当インデックスに値がない場合、
	 *          そのメタ情報の初期値を返し、参照先メタ情報のないチャンネルの場合Long型のノートの値。
	 */
	private Object getResolvedNoteValueCore(int channel, int value) {
		// 参照先メタ情報が存在しないチャンネルの場合は当該ノートの値をINTEGER形式で返す
		var valObj = BmsInt.box(value);
		var info = mMetas.getReferenceMeta(channel);
		if (info == null) {
			return BmsType.INTEGER.cast(valObj);
		}

		// 参照先メタ情報に当該データが存在する場合はその値を、ない場合はメタ情報の初期値を返す
		var element = info.valueList.get(valObj);
		var resolvedValue = (element == null) ? info.meta.getDefaultValue() : element.getValue();

		return resolvedValue;
	}

	/**
	 * チャンネル番号とチャンネルインデックスの整合性をテストするアサーション
	 * @param channel チャンネル番号
	 * @param index チャンネルインデックス
	 * @throws IllegalArgumentException BMS仕様にないチャンネル番号
	 * @throws IndexOutOfBoundsException チャンネルインデックスがマイナス値
	 * @throws IndexOutOfBoundsException 重複不許可のチャンネルで0以外のインデックスを指定
	 */
	private void assertChannelIndex(int channel, int index) {
		var channelSpec = mSpec.getChannel(channel);
		if (channelSpec == null) {
			throw new IllegalArgumentException(String.format("Channel number %d no such channel.", channel));
		}
		var isMultiple = channelSpec.isMultiple();
		if (!BmsSpec.isChIndexWithinRange(index, isMultiple)) {
			var msg = String.format(
					"Channel index is out of range. channel=%d, index=%d, multiple=%s",
					channel, index, isMultiple);
			throw new IndexOutOfBoundsException(msg);
		}
	}

	/**
	 * チャンネルが配列型であることをテストするアサーション
	 * @param channel チャンネル番号
	 * @throws IllegalArgumentException チャンネルが配列型ではない
	 */
	private void assertChannelArrayType(int channel) {
		var ch = mSpec.getChannel(channel);
		if (!ch.isArrayType()) {
			var msg = String.format("Channel is NOT array type. channel=%s", ch);
			throw new IllegalArgumentException(msg);
		}
	}

	/**
	 * チャンネルが配列型ではないことをテストするアサーション
	 * @param channel チャンネル番号
	 * @throws IllegalArgumentException チャンネルが配列型
	 */
	private void assertChannelNotArrayType(int channel) {
		var ch = mSpec.getChannel(channel);
		if (ch.isArrayType()) { // Value/ObjectであればOK
			var msg = String.format("Channel is NOT value type. channel=%s", ch);
			throw new IllegalArgumentException(msg);
		}
	}

	/**
	 * 小節番号と小節の刻み位置の整合性をテストするアサーション。
	 * <p>当メソッドでのアサーションは、小節番号が小節数未満であることを要求する。</p>
	 * @param measure 小節番号
	 * @param tick 小節の刻み位置
	 * @throws IllegalArgumentException 小節番号がマイナス値または小節数以上
	 * @throws IllegalArgumentException 小節の刻み位置がマイナス値または当該小節の刻み数以上
	 */
	private void assertPoint(int measure, double tick) {
		assertArgMeasureWithinRange(measure, mTl.getMeasureCount() - 1, tick);
		assertArgTickWithinRange(tick, mTl.getMeasureTickMax(measure), measure);
	}

	/**
	 * 小節番号と小節の刻み位置の整合性をテストするアサーション。
	 * <p>小節番号が小節数と同じ場合、刻み位置が0であることを要求する。</p>
	 * <p>当メソッドでのアサーションは、小節番号が小節数の範囲内であることを要求する。</p>
	 * @param measure 小節番号
	 * @param tick 小節の刻み位置
	 * @throws IllegalArgumentException 小節番号がマイナス値、小節数以上、または小節数と同値で刻み位置が0以外
	 * @throws IllegalArgumentException 小節の刻み位置がマイナス値または当該小節の刻み数以上
	 */
	private void assertPointAllowTerm(int measure, double tick) {
		var measureMax = mTl.getMeasureCount();
		assertArgMeasureWithinRange(measure, measureMax, tick);
		var tickMax = (measure == measureMax) ? BmsSpec.TICK_MIN : mTl.getMeasureTickMax(measure);
		assertArgTickWithinRange(tick, tickMax, measure);
	}

	/**
	 * 小節番号と小節の刻み位置の整合性をテストするアサーション。
	 * @param measure 小節番号
	 * @param tick 小節の刻み位置
	 * @throws IllegalArgumentException 小節番号が{@link BmsSpec#MEASURE_MIN}未満または{@link BmsSpec#MEASURE_MAX}超過
	 * @throws IllegalArgumentException 小節の刻み位置がマイナス値または当該小節の刻み数以上
	 */
	private void assertPointAllowOverMeasureCount(int measure, double tick) {
		assertArgMeasureWithinRange(measure, BmsSpec.MEASURE_MAX, tick);
		assertArgTickWithinRange(tick, mTl.getMeasureTickMax(measure), measure);
	}

	/**
	 * 小節長の値をテストするアサーション
	 * <p>チャンネルが小節長変更チャンネル以外の場合、このテストはスキップされる。</p>
	 * @param channel チャンネル
	 * @param measure 小節番号
	 * @param value 小節長
	 * @throws IllegalArgumentException 小節長に{@link BmsSpec#LENGTH_MIN}未満または{@link BmsSpec#LENGTH_MAX}超過の値を設定しようとした
	 */
	private void assertMeasureLength(BmsChannel channel, int measure, Object value) {
		if (channel.isLength()) {
			// 小節長変更チャンネルの範囲外チェック
			var length = ((Number)value).doubleValue();
			if (!BmsSpec.isLengthWithinRange(length)) {
				var msg = String.format(
						"Measure length is out of range. channel=%s, measure=%d, expect-length=(%.16g-%.16g), actual-length=%.16g",
						channel, measure, BmsSpec.LENGTH_MIN, BmsSpec.LENGTH_MAX, length);
				throw new IllegalArgumentException(msg);
			}
		}
	}

	/**
	 * 動作モードが編集モードかどうかをテストするアサーション
	 * @throws IllegalStateException 動作モードが編集モードではない
	 */
	private void assertIsEditMode() {
		if (!mFnIsEditMode.getAsBoolean()) {
			throw new IllegalStateException("Now is NOT edit mode.");
		}
	}

	/**
	 * 動作モードが参照モードかどうかをテストするアサーション
	 * @throws IllegalStateException 動作モードが参照モードではない
	 */
	private void assertIsReferenceMode() {
		if (mFnIsEditMode.getAsBoolean()) {
			throw new IllegalStateException("Now is NOT reference mode.");
		}
	}
}