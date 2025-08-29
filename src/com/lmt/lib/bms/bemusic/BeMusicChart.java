package com.lmt.lib.bms.bemusic;

import static com.lmt.lib.bms.internal.Assertion.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.BiPredicate;
import java.util.function.IntPredicate;
import java.util.function.IntToDoubleFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.lmt.lib.bms.BmsAt;
import com.lmt.lib.bms.BmsInt;
import com.lmt.lib.bms.internal.Utility;

/**
 * BMS譜面全体とその統計情報を表します。
 *
 * <p>BMS譜面全体とは、楽曲位置情報である{@link BeMusicPoint}の集合(リスト)を示します。
 * 当クラスが保有する楽曲位置情報は時間軸で昇順ソートされています。アプリケーションは当クラスが持つイテレータや
 * 楽曲位置情報を取得するためのGetter、およびストリームを用いて楽曲位置情報を参照することができます。</p>
 *
 * <p>楽曲位置情報の集合からは様々な分析を行うことができ、代表的な統計情報の収集は当クラスの構築時に行われます。
 * 統計情報にはGetterからアクセスすることが可能で、アプリケーションの要求に応じてそれらの情報を活用することが
 * 可能になっています。より高度な統計情報を集計したい場合は{@link BeMusicStatisticsBuilder}を参照してください。</p>
 *
 * <p>また、小節番号・刻み位置や時間を用いて時間軸への高速なアクセスを行ったり、条件を指定して楽曲位置情報を
 * 検索する等の機能を提供し、Be-Musicに関するアプリケーション開発のアシストを行います。</p>
 *
 * <p>当クラスが持つ情報や機能では足りない場合には、当クラスを拡張し、処理や機能を追加してください。そのための
 * 処理実装は{@link #onCreate()}で行うことを想定しています。</p>
 *
 * @since 0.0.1
 */
public class BeMusicChart implements Iterable<BeMusicPoint> {
	/** LNモードで、疑似的なBack-Spin, Multi-Spinと判定する最大間隔(秒単位) */
	private static final double MAX_PSEUDO_TIME = 0.21;

	/** 楽曲位置情報のイテレータ */
	private class PointIterator implements Iterator<BeMusicPoint> {
		/** インデックス */
		private int mIndex = 0;
		/** 楽曲位置情報の数 */
		private int mCount = mPoints.size();

		/** {@inheritDoc} */
		@Override
		public boolean hasNext() {
			return (mIndex < mCount);
		}

		/** {@inheritDoc} */
		@Override
		public BeMusicPoint next() {
			if (!hasNext()) { throw new NoSuchElementException(); }
			return mPoints.get(mIndex++);
		}
	}

	/** 楽曲位置情報リスト */
	private List<BeMusicPoint> mPoints;
	/** 総ノート数 */
	private int mNoteCount;
	/** ロングノート数 */
	private int mLnCount;
	/** 地雷オブジェ数 */
	private int mLmCount;
	/** レーンごとの総ノート数 */
	private int[] mNoteCountsLane = new int[BeMusicLane.COUNT];
	/** 入力デバイスごとの総ノート数 */
	private int[] mNoteCountsDevice = new int[BeMusicDevice.COUNT];
	/** レーンごとのロングノート数 */
	private int[] mLnCountsLane = new int[BeMusicLane.COUNT];
	/** 入力デバイスごとのロングノート数 */
	private int[] mLnCountsDevice = new int[BeMusicDevice.COUNT];
	/** レーンごとの地雷オブジェ数 */
	private int[] mLmCountsLane = new int[BeMusicLane.COUNT];
	/** 入力デバイスごとの地雷オブジェ数 */
	private int[] mLmCountsDevice = new int[BeMusicDevice.COUNT];
	/** スクロール速度変化回数 */
	private int mChgScrollCount;
	/** BPM変化回数 */
	private int mChgBpmCount;
	/** 譜面停止回数 */
	private int mStopCount;
	/** 速度変更有無 */
	private boolean mChgSpeed;
	/** ギミック有無 */
	private boolean mGimmick;
	/** 操作可能ノートのある楽曲位置情報の最終インデックス */
	private int mLastPlayableIndex;
	/** BGM有無 */
	private boolean mHasBgm;
	/** BGA有無 */
	private boolean mHasBga;
	/** 推奨TOTAL値1 */
	private double mRecommendTotal1;
	/** 推奨TOTAL値2 */
	private double mRecommendTotal2;
	/** スクラッチモード */
	private BeMusicScratchMode mScratchMode;

	/**
	 * BMS譜面オブジェクトを構築します。
	 * <p>当クラスはアプリケーションからこのコンストラクタ単体でオブジェクトを生成し、独自に使用することを
	 * 想定していません。オブジェクトの生成は{@link #create(List, Supplier)}のオブジェクトクリエータで
	 * インスタンス生成されることを意図しています。</p>
	 */
	public BeMusicChart() {
		// Do nothing
	}

	/**
	 * 指定した楽曲位置情報リストを用いてBMS譜面オブジェクトを構築します。
	 * <p>当メソッドでは{@link BeMusicChart}クラスのインスタンスを生成してオブジェクトを構築します。
	 * それ以外の動作仕様については{@link #create(List, Supplier)}を参照してください。</p>
	 * @param list 楽曲位置情報リスト
	 * @return BMS譜面オブジェクト
	 * @see #create(List, Supplier)
	 */
	public static BeMusicChart create(List<BeMusicPoint> list) {
		return create(list, () -> new BeMusicChart());
	}

	/**
	 * 指定した楽曲位置情報リストを用いてBMS譜面オブジェクトを構築します。
	 * <p>BMSコンテンツから楽曲位置情報を抽出したリストから、BMS譜面オブジェクトクリエータで生成したオブジェクトを
	 * 生成しデータを構築します。楽曲位置情報の抽出については{@link BeMusicChartBuilder}を参照してください。</p>
	 * <p>通常、当メソッドは当クラスの拡張を行わない限り使用されることはありません。アプリケーションによって
	 * 当クラスの拡張を行い、情報・機能を追加する場合にのみ参照することを推奨します。ライブラリが提供する
	 * BMS譜面オブジェクトを用いる場合は{@link #create(List)}または{@link BeMusicChartBuilder#createChart()}を
	 * 使用してBMS譜面オブジェクトを生成してください。</p>
	 * <p>楽曲位置情報リストは小節番号・刻み位置と時間が昇順でソートされていなければなりません。
	 * リストが前述のような状態になっていない場合はBMS譜面オブジェクトは生成されず例外がスローされます。</p>
	 * @param <S> BMS譜面オブジェクト(拡張したものを含む)
	 * @param list 楽曲位置情報リスト
	 * @param creator BMS譜面オブジェクトクリエータ
	 * @return BMS譜面オブジェクト
	 * @throws NullPointerException listがnull
	 * @throws NullPointerException creatorがnull
	 * @throws NullPointerException creatorがnullを返した
	 * @throws IllegalArgumentException 楽曲位置情報リストで小節番号・刻み位置が後退した
	 * @throws IllegalArgumentException 楽曲位置情報リストで時間が後退した
	 * @see BeMusicChartBuilder
	 * @see #create(List)
	 */
	public static <S extends BeMusicChart> S create(List<BeMusicPoint> list, Supplier<S> creator) {
		assertArgNotNull(list, "list");
		assertArgNotNull(creator, "creator");
		var instance = creator.get();
		instance.setup(list);
		return instance;
	}

	/**
	 * 楽曲位置情報を取得します。
	 * @param index インデックス
	 * @return 楽曲位置情報
	 * @throws IndexOutOfBoundsException indexがマイナス値または{@link #getPointCount()}以上
	 */
	public BeMusicPoint getPoint(int index) {
		assertArgIndexRange(index, mPoints.size(), "index");
		return mPoints.get(index);
	}

	/**
	 * 楽曲位置情報リストのコピーを取得します。
	 * <p>当メソッドはBMS譜面オブジェクトが持つ楽曲位置情報リストの完全なコピーを返します。従って、
	 * 頻繁に実行するとアプリケーションのパフォーマンスが大幅に低下する可能性がありますので注意が必要です。</p>
	 * @return 楽曲位置情報リスト
	 */
	public List<BeMusicPoint> getPoints() {
		var points = new ArrayList<BeMusicPoint>(mPoints.size());
		mPoints.forEach(p -> points.add(new BeMusicPoint(p)));
		return points;
	}

	/**
	 * 楽曲位置情報の数を取得します。
	 * @return 楽曲位置情報の数
	 */
	public int getPointCount() {
		return mPoints.size();
	}

	/**
	 * 総ノート数を取得します。
	 * @return 総ノート数
	 */
	public int getNoteCount() {
		return mNoteCount;
	}

	/**
	 * 指定レーンの総ノート数を取得します。
	 * @param lane レーン
	 * @return 指定レーンの総ノート数
	 * @throws NullPointerException laneがnull
	 * @since 0.9.0
	 */
	public int getNoteCount(BeMusicLane lane) {
		assertArgNotNull(lane, "lane");
		return mNoteCountsLane[lane.getIndex()];
	}

	/**
	 * 指定入力デバイスの総ノート数を取得します。
	 * @param device 入力デバイス
	 * @return 指定入力デバイスの総ノート数
	 * @throws NullPointerException deviceがnull
	 */
	public int getNoteCount(BeMusicDevice device) {
		assertArgNotNull(device, "device");
		return mNoteCountsDevice[device.getIndex()];
	}

	/**
	 * ロングノート数を取得します。
	 * @return ロングノート数
	 */
	public int getLongNoteCount() {
		return mLnCount;
	}

	/**
	 * 指定レーンのロングノート数を取得します。
	 * @param lane レーン
	 * @return 指定レーンのロングノート数
	 * @throws NullPointerException laneがnull
	 * @since 0.9.0
	 */
	public int getLongNoteCount(BeMusicLane lane) {
		assertArgNotNull(lane, "lane");
		return mLnCountsLane[lane.getIndex()];
	}

	/**
	 * 指定入力デバイスのロングノート数を取得します。
	 * @param device 入力デバイス
	 * @return 指定入力デバイスのロングノート数
	 * @throws NullPointerException deviceがnull
	 */
	public int getLongNoteCount(BeMusicDevice device) {
		assertArgNotNull(device, "device");
		return mLnCountsDevice[device.getIndex()];
	}

	/**
	 * 地雷オブジェの数を取得します。
	 * @return 地雷オブジェの数
	 */
	public int getMineCount() {
		return mLmCount;
	}

	/**
	 * 指定レーンの地雷オブジェ数を取得します。
	 * @param lane レーン
	 * @return 指定レーンの地雷オブジェ数
	 * @throws NullPointerException laneがnull
	 * @since 0.9.0
	 */
	public int getMineCount(BeMusicLane lane) {
		assertArgNotNull(lane, "lane");
		return mLmCountsLane[lane.getIndex()];
	}

	/**
	 * 指定入力デバイスの地雷オブジェ数を取得します。
	 * @param device 入力デバイス
	 * @return 指定入力デバイスの地雷オブジェ数
	 * @throws NullPointerException deviceがnull
	 */
	public int getMineCount(BeMusicDevice device) {
		assertArgNotNull(device, "device");
		return mLmCountsDevice[device.getIndex()];
	}

	/**
	 * この譜面の演奏時間を秒単位で取得します。
	 * <p>当メソッドが返す演奏時間は譜面の先頭から最後の操作可能ノートに到達するまでの時間を表します。
	 * それ以降のBGM/BGA等の有無や音声の再生状態は演奏時間には含まれません。</p>
	 * <p>操作可能ノートのない譜面、または譜面が空の場合は演奏時間は0になります。</p>
	 * <p>使用するサウンドの再生時間を考慮した演奏時間を計算したい場合は
	 * {@link #computeActualPlayTime(IntToDoubleFunction)} を使用してください。</p>
	 * @return この譜面の演奏時間
	 * @see #computeActualPlayTime(IntToDoubleFunction)
	 */
	public double getPlayTime() {
		return  mPoints.isEmpty() ? 0.0 : mPoints.get(mLastPlayableIndex).getTime();
	}

	/**
	 * スクロール速度の変化回数を取得します。
	 * @return スクロール速度の変化回数
	 * @since 0.6.0
	 */
	public int getChangeScrollCount() {
		return mChgScrollCount;
	}

	/**
	 * BPM変化回数を取得します。
	 * @return BPM変化回数
	 */
	public int getChangeBpmCount() {
		return mChgBpmCount;
	}

	/**
	 * 譜面停止回数を取得します。
	 * @return 譜面停止回数
	 */
	public int getStopCount() {
		return mStopCount;
	}

	/**
	 * 推奨TOTAL値を取得します。
	 * <p>この値は総ノート数をもとに、以下の計算式で算出されたものです。</p>
	 * <pre>
	 * #TOTAL = 7.605 * N / (0.01 * N + 6.5)
	 * ※N = 総ノート数</pre>
	 * @return 推奨TOTAL値
	 * @since 0.3.0
	 */
	public double getRecommendTotal1() {
		return mRecommendTotal1;
	}

	/**
	 * 推奨TOTAL値を取得します。
	 * <p>この値は総ノート数をもとに、以下の計算式で算出されたものです。</p>
	 * <pre>
	 * N &lt; 400 : #TOTAL = 200 + N / 5
	 * N &lt; 600 : #TOTAL = 280 + (N - 400) / 2.5
	 * N &gt;= 600: #TOTAL = 360 + (N - 600) / 5
	 * ※N = 総ノート数</pre>
	 * @return 推奨TOTAL値
	 * @since 0.3.0
	 */
	public double getRecommendTotal2() {
		return mRecommendTotal2;
	}

	/**
	 * スクラッチモードを取得します。
	 * @return スクラッチモード
	 * @since 0.7.0
	 */
	public BeMusicScratchMode getScratchMode() {
		return mScratchMode;
	}

	/**
	 * ロングノート有無を取得します。
	 * @return ロングノート有無
	 */
	public boolean hasLongNote() {
		return (mLnCount > 0);
	}

	/**
	 * 指定レーンのロングノート有無を取得します。
	 * @param lane レーン
	 * @return 指定レーンにロングノートが含まれていればtrue
	 * @throws NullPointerException laneがnull
	 * @since 0.9.0
	 */
	public boolean hasLongNote(BeMusicLane lane) {
		return (getLongNoteCount(lane) > 0);
	}

	/**
	 * 指定入力デバイスのロングノート有無を取得します。
	 * @param device 入力デバイス
	 * @return 指定入力デバイスにロングノートが含まれていればtrue
	 * @throws NullPointerException deviceがnull
	 * @since 0.9.0
	 */
	public boolean hasLongNote(BeMusicDevice device) {
		return (getLongNoteCount(device) > 0);
	}

	/**
	 * 地雷オブジェ有無を取得します。
	 * @return 地雷オブジェ有無
	 */
	public boolean hasMine() {
		return (mLmCount > 0);
	}

	/**
	 * 指定レーンの地雷オブジェ有無を取得します。
	 * @param lane レーン
	 * @return 指定レーンに地雷オブジェが含まれていればtrue
	 * @throws NullPointerException laneがnull
	 * @since 0.9.0
	 */
	public boolean hasMine(BeMusicLane lane) {
		return (getMineCount(lane) > 0);
	}

	/**
	 * 指定入力デバイスの地雷オブジェ有無を取得します。
	 * @param device 入力デバイス
	 * @return 指定入力デバイスに地雷オブジェが含まれていればtrue
	 * @throws NullPointerException deviceがnull
	 * @since 0.9.0
	 */
	public boolean hasMine(BeMusicDevice device) {
		return (getMineCount(device) > 0);
	}

	/**
	 * BGM有無を取得します。
	 * @return BGM有無
	 */
	public boolean hasBgm() {
		return mHasBgm;
	}

	/**
	 * BGA有無を取得します。
	 * @return BGA有無
	 */
	public boolean hasBga() {
		return mHasBga;
	}

	/**
	 * スクロール速度の変化有無を取得します。
	 * @return スクロール速度の変化有無
	 * @since 0.6.0
	 */
	public boolean hasChangeScroll() {
		return (mChgScrollCount > 0);
	}

	/**
	 * BPM変化有無を取得します。
	 * @return BPM変化有無
	 */
	public boolean hasChangeBpm() {
		return (mChgBpmCount > 0);
	}

	/**
	 * 速度変更有無を取得します。
	 * <p>当メソッドは、譜面内にBPMの途中変更、またはスクロール速度変更があった場合にtrueを返します。</p>
	 * @return 速度変更有無
	 * @see #hasChangeBpm()
	 * @see #hasChangeScroll()
	 * @since 0.6.0
	 */
	public boolean hasChangeSpeed() {
		return mChgSpeed;
	}

	/**
	 * 譜面停止有無を取得します。
	 * @return 譜面停止有無
	 */
	public boolean hasStop() {
		return (mStopCount > 0);
	}

	/**
	 * ギミック有無を取得します。
	 * <p>当メソッドは、譜面内にBPMの途中変更、スクロール速度変更、譜面停止、地雷オブジェのいずれかが
	 * 存在した場合にtrueを返します。</p>
	 * @return ギミック有無
	 * @see #hasChangeSpeed()
	 * @see #hasChangeBpm()
	 * @see #hasChangeScroll()
	 * @see #hasStop()
	 * @see #hasMine()
	 * @since 0.6.0
	 */
	public boolean hasGimmick() {
		return mGimmick;
	}

	/** {@inheritDoc} */
	@Override
	public Iterator<BeMusicPoint> iterator() {
		return new PointIterator();
	}

	/**
	 * 楽曲位置情報リストを走査するストリームを返します。
	 * <p>楽曲位置情報リストは、楽曲位置の時間で昇順ソートされていることを保証します。</p>
	 * @return 楽曲位置情報リストを走査するストリーム
	 * @since 0.1.0
	 */
	public Stream<BeMusicPoint> points() {
		return mPoints.stream();
	}

	/**
	 * 指定範囲の楽曲位置情報リストを走査するストリームを返します。
	 * <p>引数に指定可能な値は 0～{@link #getPointCount()}-1 の範囲です。</p>
	 * @param start 走査を開始する楽曲位置情報リストのインデックス(この値を含む)
	 * @param end 走査を終了する楽曲位置情報リストのインデックス(この値を含まない)
	 * @return 楽曲位置情報リストを走査するストリーム
	 * @throws IndexOutOfBoundsException startが 0～{@link #getPointCount()}-1 の範囲外
	 * @throws IndexOutOfBoundsException endが 0～{@link #getPointCount()} の範囲外
	 * @since 0.8.0
	 */
	public Stream<BeMusicPoint> points(int start, int end) {
		var count = mPoints.size();
		assertArgIndexRange(start, count, "start");
		assertArgIndexRange(end, count + 1, "end");
		return IntStream.range(start, end).mapToObj(this::getPoint);
	}

	/**
	 * 楽曲位置情報リスト全体から指定条件に該当する最初の楽曲位置情報のインデックスを返します。
	 * <p>当メソッドはindexOf(0, getPointCount(), tester)を実行します。</p>
	 * @param tester 条件のテスター
	 * @return 指定条件に最初に該当した楽曲位置情報のインデックス。条件に該当する楽曲位置情報がない場合-1。
	 * @see #indexOf(int, int, Predicate)
	 */
	public int indexOf(Predicate<BeMusicPoint> tester) {
		return indexOf(0, getPointCount(), tester);
	}

	/**
	 * 楽曲位置情報リストの指定範囲から指定条件に該当する最初の楽曲位置情報のインデックスを返します。
	 * @param beginIndex テスト範囲FROM(このインデックスを含む)
	 * @param endIndex テスト範囲TO(このインデックスを含まない)
	 * @param tester 条件のテスター
	 * @return 指定条件に最初に該当した楽曲位置情報のインデックス。条件に該当する楽曲位置情報がない場合-1。
	 * @throws IndexOutOfBoundsException beginIndexがマイナス値または{@link #getPointCount()}超過
	 * @throws IndexOutOfBoundsException endIndexがマイナス値または{@link #getPointCount()}超過
	 * @throws NullPointerException testerがnull
	 */
	public int indexOf(int beginIndex, int endIndex, Predicate<BeMusicPoint> tester) {
		assertArgIndexRange(beginIndex, mPoints.size() + 1, "beginIndex");
		assertArgIndexRange(endIndex, mPoints.size() + 1, "endIndex");
		assertArgNotNull(tester, "tester");
		for (var i = beginIndex; i < endIndex; i++) { if (tester.test(mPoints.get(i))) { return i; } }
		return -1;
	}

	/**
	 * 楽曲位置情報リスト全体から指定条件に該当する最後の楽曲位置情報のインデックスを返します。
	 * <p>当メソッドはlastIndexOf(0, getPointCount(), tester)を実行します。</p>
	 * @param tester 条件のテスター
	 * @return 指定条件に最後に該当した楽曲位置情報のインデックス。条件に該当する楽曲位置情報がない場合-1。
	 * @see #lastIndexOf(int, int, Predicate)
	 */
	public int lastIndexOf(Predicate<BeMusicPoint> tester) {
		return lastIndexOf(0, getPointCount(), tester);
	}

	/**
	 * 楽曲位置情報リストの指定範囲から指定条件に該当する最後の楽曲位置情報のインデックスを返します。
	 * @param beginIndex テスト範囲FROM(このインデックスを含む)
	 * @param endIndex テスト範囲TO(このインデックスを含まない)
	 * @param tester 条件のテスター
	 * @return 指定条件に最後に該当した楽曲位置情報のインデックス。条件に該当する楽曲位置情報がない場合-1。
	 * @throws IndexOutOfBoundsException beginIndexがマイナス値または{@link #getPointCount()}超過
	 * @throws IndexOutOfBoundsException endIndexがマイナス値または{@link #getPointCount()}超過
	 * @throws NullPointerException testerがnull
	 */
	public int lastIndexOf(int beginIndex, int endIndex, Predicate<BeMusicPoint> tester) {
		assertArgIndexRange(beginIndex, mPoints.size() + 1, "beginIndex");
		assertArgIndexRange(endIndex, mPoints.size() + 1, "endIndex");
		assertArgNotNull(tester, "tester");
		for (var i = endIndex - 1; i >= beginIndex; i--) { if (tester.test(mPoints.get(i))) { return i; } }
		return -1;
	}

	/**
	 * 指定楽曲位置以前(この位置を含む)で最大の楽曲位置を持つ楽曲位置情報のインデックスを返します。
	 * @param at 楽曲位置
	 * @return 条件に該当するインデックス。そのような楽曲位置情報がない場合-1。
	 * @throws NullPointerException atがnull
	 */
	public int floorPointOf(BmsAt at) {
		assertArgNotNull(at, "at");
		return floorPointOf(at.getMeasure(), at.getTick());
	}

	/**
	 * 指定楽曲位置以前(この位置を含む)で最大の楽曲位置を持つ楽曲位置情報のインデックスを返します。
	 * @param measure 小節番号
	 * @param tick 小節の刻み位置
	 * @return 条件に該当するインデックス。そのような楽曲位置情報がない場合-1。
	 */
	public int floorPointOf(int measure, double tick) {
		return Utility.bsearchFloor(mPoints, p -> BmsAt.compare2(p.getMeasure(), p.getTick(), measure, tick));
	}

	/**
	 * 指定時間以前(この時間を含む)で最大の時間を持つ楽曲位置情報のインデックスを返します。
	 * @param time 時間
	 * @return 条件に該当するインデックス。そのような楽曲位置情報がない場合-1。
	 * @throws IllegalArgumentException timeがマイナス値
	 */
	public int floorPointOf(double time) {
		assertArg(time >= 0.0, "Argument:time is minus value. [%f]", time);
		return Utility.bsearchFloor(mPoints, p -> Double.compare(p.getTime(), time));
	}

	/**
	 * 指定楽曲位置以降(この位置を含む)で最小の楽曲位置を持つ楽曲位置情報のインデックスを返します。
	 * @param at 楽曲位置
	 * @return 条件に該当するインデックス。そのような楽曲位置情報がない場合-1。
	 * @throws NullPointerException atがnull
	 */
	public int ceilPointOf(BmsAt at) {
		assertArgNotNull(at, "at");
		return ceilPointOf(at.getMeasure(), at.getTick());
	}

	/**
	 * 指定楽曲位置以降(この位置を含む)で最小の楽曲位置を持つ楽曲位置情報のインデックスを返します。
	 * @param measure 小節番号
	 * @param tick 小節の刻み位置
	 * @return 条件に該当するインデックス。そのような楽曲位置情報がない場合-1。
	 */
	public int ceilPointOf(int measure, double tick) {
		return Utility.bsearchCeil(mPoints, p -> BmsAt.compare2(p.getMeasure(), p.getTick(), measure, tick));
	}

	/**
	 * 指定時間以降(この時間を含む)で最小の時間を持つ楽曲位置情報のインデックスを返します。
	 * @param time 時間
	 * @return 条件に該当するインデックス。そのような楽曲位置情報がない場合-1。
	 * @throws IllegalArgumentException timeがマイナス値
	 */
	public int ceilPointOf(double time) {
		assertArg(time >= 0.0, "Argument:time is minus value. [%f]", time);
		return Utility.bsearchCeil(mPoints, p -> Double.compare(p.getTime(), time));
	}

	/**
	 * サウンドのトラックIDを収集します。
	 * <p>当メソッドは以下のコードと等価です。</p>
	 * <pre>chart.collectSoundTracks(0, Math.max(0, chart.getPointCount() - 1), isCollect);</pre>
	 * @param isCollect ノートのトラックIDを収集するかを決定するテスト関数
	 * @return 昇順ソートされたトラックIDセット
	 * @throws NullPointerException isCollect が null
	 * @see #collectSoundTracks(int, int, IntPredicate)
	 * @since 0.10.0
	 */
	public Set<Integer> collectSoundTracks(IntPredicate isCollect) {
		return collectSoundTracks(0, Math.max(0, mPoints.size() - 1), isCollect);
	}

	/**
	 * 指定した範囲の楽曲位置情報に記録されているサウンドのトラックIDを収集します。
	 * <p>当メソッドは以下のコードと等価です。</p>
	 * <pre>
	 * int start = chart.ceilPointOf(timeBegin);
	 * int last = chart.floorPointOf(timeLast);
	 * chart.collectSoundTracks(
	 *     (start == -1) ? Integer.MAX_VALUE : start,
	 *     (last == -1) ? 0 : last,
	 *     isCollect
	 * );</pre>
	 * @param timeBegin 収集開始時間(この時間を含む)
	 * @param timeLast 収集終了時間(この時間を含む)
	 * @param isCollect ノートのトラックIDを収集するかを決定するテスト関数
	 * @return 昇順ソートされたトラックIDセット
	 * @throws IllegalArgumentException timeBegin, timeEnd が負の値
	 * @throws NullPointerException isCollect が null
	 * @see #collectSoundTracks(int, int, IntPredicate)
	 * @since 0.10.0
	 */
	public Set<Integer> collectSoundTracks(double timeBegin, double timeLast, IntPredicate isCollect) {
		var start = ceilPointOf(timeBegin);
		var last = floorPointOf(timeLast);
		return collectSoundTracks((start == -1) ? Integer.MAX_VALUE : start, (last == -1) ? 0 : last, isCollect);
	}

	/**
	 * 指定した範囲の楽曲位置情報に記録されているサウンドのトラックIDを収集します。
	 * <p>当メソッドは以下のコードと等価です。</p>
	 * <pre>
	 * int start = chart.ceilPointOf(atBegin);
	 * int last = chart.floorPointOf(atLast);
	 * chart.collectSoundTracks(
	 *     (start == -1) ? Integer.MAX_VALUE : start,
	 *     (last == -1) ? 0 : last,
	 *     isCollect
	 * );</pre>
	 * @param atBegin 収集開始楽曲位置(この位置を含む)
	 * @param atLast 収集終了楽曲位置(この位置を含む)
	 * @param isCollect ノートのトラックIDを収集するかを決定するテスト関数
	 * @return 昇順ソートされたトラックIDセット
	 * @throws NullPointerException atBegin, atLast, isCollect が null
	 * @see #collectSoundTracks(int, int, IntPredicate)
	 * @since 0.10.0
	 */
	public Set<Integer> collectSoundTracks(BmsAt atBegin, BmsAt atLast, IntPredicate isCollect) {
		assertArgNotNull(atBegin, "atBegin");
		assertArgNotNull(atLast, "atEnd");
		var start = ceilPointOf(atBegin);
		var last = floorPointOf(atLast);
		return collectSoundTracks((start == -1) ? Integer.MAX_VALUE : start, (last == -1) ? 0 : last, isCollect);
	}

	/**
	 * 指定した範囲の楽曲位置情報に記録されているサウンドのトラックIDを収集します。
	 * <p>当メソッドは以下のコードと等価です。</p>
	 * <pre>
	 * int start = chart.ceilPointOf(measureBegin, tickBegin);
	 * int last = chart.floorPointOf(measureLast, tickLast);
	 * chart.collectSoundTracks(
	 *     (start == -1) ? Integer.MAX_VALUE : start,
	 *     (last == -1) ? 0 : last,
	 *     isCollect
	 * );</pre>
	 * @param measureBegin 収集開始小節番号(この小節を含む)
	 * @param tickBegin 収集開始刻み位置(この刻み位置を含む)
	 * @param measureLast 収集終了小節番号(この小節を含む)
	 * @param tickLast 収集終了刻み位置(この刻み位置を含む)
	 * @param isCollect ノートのトラックIDを収集するかを決定するテスト関数
	 * @return 昇順ソートされたトラックIDセット
	 * @throws NullPointerException isCollect が null
	 * @see #collectSoundTracks(int, int, IntPredicate)
	 * @since 0.10.0
	 */
	public Set<Integer> collectSoundTracks(int measureBegin, double tickBegin, int measureLast, double tickLast,
			IntPredicate isCollect) {
		var start = ceilPointOf(measureBegin, tickBegin);
		var last = floorPointOf(measureLast, tickLast);
		return collectSoundTracks((start == -1) ? Integer.MAX_VALUE : start, (last == -1) ? 0 : last, isCollect);
	}

	/**
	 * 指定した範囲の楽曲位置情報に記録されているサウンドのトラックIDを収集します。
	 * <p>具体的には「可視オブジェ」、「不可視オブジェ」、「BGM」のトラックIDが収集対象となります。</p>
	 * <p>可視オブジェでは操作を伴うノート({@link BeMusicNoteType#hasMovement()} が true を返すノート)
	 * が収集対象となり、それ以外のノートは収集されません。</p>
	 * <p>トラックIDセットに含めるかどうかは isCollect 関数のテスト結果によって決定されます。
	 * この関数の入力値はノートの生値になっているため、このノートから各種属性値を得るには
	 * {@link BeMusicSound} を使用してください。関数が false を返すと収集の対象外になります。</p>
	 * <p>返されたトラックIDセットは、走査すると昇順ソートされています。また、このセットの変更可否については
	 * 未定義となりますのでセットの変更を行う場合は別インスタンスへコピーしてください。</p>
	 * @param start 収集開始インデックス(この値を含む)
	 * @param last 収集終了インデックス(この値を含む)
	 * @param isCollect ノートのトラックIDを収集するかを決定するテスト関数
	 * @return 昇順ソートされたトラックIDセット
	 * @throws IllegalArgumentException start, last が負の値
	 * @throws NullPointerException isCollect が null
	 * @since 0.10.0
	 */
	public Set<Integer> collectSoundTracks(int start, int last, IntPredicate isCollect) {
		assertArg(start >= 0, "Argument 'start' is negative value");
		assertArg(last >= 0, "Argument 'last' is negative value");
		assertArgNotNull(isCollect, "isCollect");

		// 指定範囲のトラックIDを収集する
		var iLast = Math.min(last, mPoints.size() - 1);
		var trackIds = new TreeSet<Integer>();
		for (var i = start; i <= iLast; i++) {
			// 可視オブジェのトラックIDを収集する
			var pt = mPoints.get(i);
			if (pt.getNotePos(RawNotes.VISIBLE) != BeMusicPoint.NA) {
				for (var j = 0; j < BeMusicDevice.COUNT; j++) {
					var raw = pt.getRawNote(RawNotes.VISIBLE, j);
					var id = BeMusicSound.getTrackId(raw);
					if ((id != 0) && BeMusicSound.getNoteType(raw).hasMovement() && isCollect.test(raw)) {
						trackIds.add(BmsInt.box(id));
					}
				}
			}
			// 不可視オブジェのトラックIDを収集する
			if (pt.getNotePos(RawNotes.INVISIBLE) != BeMusicPoint.NA) {
				for (var j = 0; j < BeMusicDevice.COUNT; j++) {
					var raw = pt.getRawNote(RawNotes.INVISIBLE, j);
					var id = BeMusicSound.getTrackId(raw);
					if ((id != 0) && isCollect.test(raw)) {
						trackIds.add(BmsInt.box(id));
					}
				}
			}
			// BGMのトラックIDを収集する
			if (pt.getNotePos(RawNotes.BGM) != BeMusicPoint.NA) {
				var numBgm = pt.getBgmCount();
				for (var j = 0; j < numBgm; j++) {
					var raw = pt.getRawNote(RawNotes.BGM, j);
					var id = BeMusicSound.getTrackId(raw);
					if ((id != 0) && isCollect.test(raw)) {
						trackIds.add(BmsInt.box(id));
					}
				}
			}
		}

		return trackIds;
	}

	/**
	 * イメージのトラックIDを収集します。
	 * <p>当メソッドは以下のコードと等価です。</p>
	 * <pre>chart.collectImageTracks(0, Math.max(0, chart.getPointCount() - 1), isCollect);</pre>
	 * @param isCollect イメージのトラックIDを収集するかを決定するテスト関数
	 * @return 昇順ソートされたトラックIDセット
	 * @throws NullPointerException isCollect が null
	 * @see #collectImageTracks(int, int, BiPredicate)
	 * @since 0.10.0
	 */
	public Set<Integer> collectImageTracks(BiPredicate<Integer, Integer> isCollect) {
		return collectImageTracks(0, Math.max(0, mPoints.size() - 1), isCollect);
	}

	/**
	 * 指定した範囲の楽曲位置情報に記録されているイメージのトラックIDを収集します。
	 * <p>当メソッドは以下のコードと等価です。</p>
	 * <pre>
	 * int start = chart.ceilPointOf(timeBegin);
	 * int last = chart.floorPointOf(timeLast);
	 * chart.collectImageTracks(
	 *     (start == -1) ? Integer.MAX_VALUE : start,
	 *     (last == -1) ? 0 : last,
	 *     isCollect
	 * );</pre>
	 * @param timeBegin 収集開始時間(この時間を含む)
	 * @param timeLast 収集終了時間(この時間を含む)
	 * @param isCollect イメージのトラックIDを収集するかを決定するテスト関数
	 * @return 昇順ソートされたトラックIDセット
	 * @throws IllegalArgumentException timeBegin, timeEnd が負の値
	 * @throws NullPointerException isCollect が null
	 * @see #collectImageTracks(int, int, BiPredicate)
	 * @since 0.10.0
	 */
	public Set<Integer> collectImageTracks(double timeBegin, double timeLast, BiPredicate<Integer, Integer> isCollect) {
		var start = ceilPointOf(timeBegin);
		var last = floorPointOf(timeLast);
		return collectImageTracks((start == -1) ? Integer.MAX_VALUE : start, (last == -1) ? 0 : last, isCollect);
	}

	/**
	 * 指定した範囲の楽曲位置情報に記録されているイメージのトラックIDを収集します。
	 * <p>当メソッドは以下のコードと等価です。</p>
	 * <pre>
	 * int start = chart.ceilPointOf(atBegin);
	 * int last = chart.floorPointOf(atLast);
	 * chart.collectImageTracks(
	 *     (start == -1) ? Integer.MAX_VALUE : start,
	 *     (last == -1) ? 0 : last,
	 *     isCollect
	 * );</pre>
	 * @param atBegin 収集開始楽曲位置(この位置を含む)
	 * @param atLast 収集終了楽曲位置(この位置を含む)
	 * @param isCollect イメージのトラックIDを収集するかを決定するテスト関数
	 * @return 昇順ソートされたトラックIDセット
	 * @throws NullPointerException atBegin, atLast, isCollect が null
	 * @see #collectImageTracks(int, int, BiPredicate)
	 * @since 0.10.0
	 */
	public Set<Integer> collectImageTracks(BmsAt atBegin, BmsAt atLast, BiPredicate<Integer, Integer> isCollect) {
		assertArgNotNull(atBegin, "atBegin");
		assertArgNotNull(atLast, "atEnd");
		var start = ceilPointOf(atBegin);
		var last = floorPointOf(atLast);
		return collectImageTracks((start == -1) ? Integer.MAX_VALUE : start, (last == -1) ? 0 : last, isCollect);
	}

	/**
	 * 指定した範囲の楽曲位置情報に記録されているイメージのトラックIDを収集します。
	 * <p>当メソッドは以下のコードと等価です。</p>
	 * <pre>
	 * int start = chart.ceilPointOf(measureBegin, tickBegin);
	 * int last = chart.floorPointOf(measureLast, tickLast);
	 * chart.collectImageTracks(
	 *     (start == -1) ? Integer.MAX_VALUE : start,
	 *     (last == -1) ? 0 : last,
	 *     isCollect
	 * );</pre>
	 * @param measureBegin 収集開始小節番号(この小節を含む)
	 * @param tickBegin 収集開始刻み位置(この刻み位置を含む)
	 * @param measureLast 収集終了小節番号(この小節を含む)
	 * @param tickLast 収集終了刻み位置(この刻み位置を含む)
	 * @param isCollect イメージのトラックIDを収集するかを決定するテスト関数
	 * @return 昇順ソートされたトラックIDセット
	 * @throws NullPointerException isCollect が null
	 * @see #collectImageTracks(int, int, BiPredicate)
	 * @since 0.10.0
	 */
	public Set<Integer> collectImageTracks(int measureBegin, double tickBegin, int measureLast, double tickLast,
			BiPredicate<Integer, Integer> isCollect) {
		var start = ceilPointOf(measureBegin, tickBegin);
		var last = floorPointOf(measureLast, tickLast);
		return collectImageTracks((start == -1) ? Integer.MAX_VALUE : start, (last == -1) ? 0 : last, isCollect);
	}

	/**
	 * 指定した範囲の楽曲位置情報に記録されているイメージのトラックIDを収集します。
	 * <p>具体的には「BGA」、「BGAレイヤー」、「プレーミス画像」のトラックIDが収集対象となります。</p>
	 * <p>トラックIDセットに含めるかどうかは isCollect 関数のテスト結果によって決定されます。
	 * この関数の入力値は第1引数がイメージ種別を表す整数値、第2引数がそのイメージのトラックIDとなっています。
	 * イメージ種別は 0=BGA, 1=BGAレイヤー, 2=プレーミス画像 となっています。種別によって収集有無を決定したい場合は
	 * この値を参照してください。関数が false を返すと収集の対象外になります。</p>
	 * <p>返されたトラックIDセットは、走査すると昇順ソートされています。また、このセットの変更可否については
	 * 未定義となりますのでセットの変更を行う場合は別インスタンスへコピーしてください。</p>
	 * @param start 収集開始インデックス(この値を含む)
	 * @param last 収集終了インデックス(この値を含む)
	 * @param isCollect イメージのトラックIDを収集するかを決定するテスト関数
	 * @return 昇順ソートされたトラックIDセット
	 * @throws IllegalArgumentException start, last が負の値
	 * @throws NullPointerException isCollect が null
	 * @since 0.10.0
	 */
	public Set<Integer> collectImageTracks(int start, int last, BiPredicate<Integer, Integer> isCollect) {
		assertArg(start >= 0, "Argument 'start' is negative value");
		assertArg(last >= 0, "Argument 'last' is negative value");
		assertArgNotNull(isCollect, "isCollect");

		// 指定範囲のトラックIDを収集する
		var iLast = Math.min(last, mPoints.size() - 1);
		var trackIds = new TreeSet<Integer>();
		for (var i = start; i <= iLast; i++) {
			// BGAのトラックIDを収集する
			var pt = mPoints.get(i);
			var bgaId = BmsInt.box(pt.getBgaValue());
			if ((bgaId != 0) && isCollect.test(0, bgaId)) {
				trackIds.add(bgaId);
			}
			// BGAレイヤーのトラックIDを収集する
			var layerId = BmsInt.box(pt.getLayerValue());
			if ((layerId != 0) && isCollect.test(1, layerId)) {
				trackIds.add(layerId);
			}
			// プレーミス画像のトラックIDを収集する
			var missId = BmsInt.box(pt.getMissValue());
			if ((missId != 0) && isCollect.test(2, missId)) {
				trackIds.add(missId);
			}
		}

		return trackIds;
	}

	/**
	 * サウンド再生時間を考慮した実際の演奏時間を計算します。
	 * <p>当メソッドは {@link #getPlayTime()} とは異なり、実際のサウンドの再生時間を参照して最後のサウンドが
	 * 再生し終えるまでの時間を計算します。利用者はノートの生値から対応するサウンドの再生時間を秒単位で返す
	 * 関数を引数で渡してください。対応するサウンドを再生しない、または何らかの理由により再生時間が決定できない場合は
	 * 0 を返すようにし、負の値を返さないようにしてください。負の値を返した場合の計算結果は未定義となり、
	 * 期待する演奏時間を得られなくなる可能性があります。</p>
	 * <p>ノートの生値からサウンドのトラックIDを取得するには {@link BeMusicSound#getTrackId(int)} を使用します。</p>
	 * @param getSoundTime 指定されたノートの生値に対応するサウンドの再生時間を返す関数
	 * @return 最後のサウンドが再生し終えるまでの時間(秒単位)
	 * @throws NullPointerException getSoundTime が null
	 * @see #getPlayTime()
	 * @see BeMusicSound
	 * @since 0.10.0
	 */
	public double computeActualPlayTime(IntToDoubleFunction getSoundTime) {
		assertArgNotNull(getSoundTime, "getSoundTime");
		var time = 0.0;
		var numPoint = mPoints.size();
		for (var i = 0; i < numPoint; i++) {
			// 可視オブジェのサウンド再生時間を計算する
			var pt = mPoints.get(i);
			if (pt.getNotePos(RawNotes.VISIBLE) != BeMusicPoint.NA) {
				for (var j = 0; j < BeMusicDevice.COUNT; j++) {
					var dev = BeMusicDevice.fromIndex(j);
					var raw = pt.getRawNote(RawNotes.VISIBLE, j);
					var id = BeMusicSound.getTrackId(raw);
					if ((id != 0) && BeMusicSound.getNoteType(raw).hasSound(dev)) {
						time = Math.max(time, pt.getTime() + getSoundTime.applyAsDouble(raw));
					}
				}
			}
			// 不可視オブジェのサウンド再生時間を計算する
			if (pt.getNotePos(RawNotes.INVISIBLE) != BeMusicPoint.NA) {
				for (var j = 0; j < BeMusicDevice.COUNT; j++) {
					var raw = pt.getRawNote(RawNotes.INVISIBLE, j);
					if (BeMusicSound.getTrackId(raw) != 0) {
						time = Math.max(time, pt.getTime() + getSoundTime.applyAsDouble(raw));
					}
				}
			}
			// BGMのサウンド再生時間を計算する
			if (pt.getNotePos(RawNotes.BGM) != BeMusicPoint.NA) {
				var numBgm = pt.getBgmCount();
				for (var j = 0; j < numBgm; j++) {
					var raw = pt.getRawNote(RawNotes.BGM, j);
					if (BeMusicSound.getTrackId(raw) != 0) {
						time = Math.max(time, pt.getTime() + getSoundTime.applyAsDouble(raw));
					}
				}
			}
		}
		return time;
	}

	/**
	 * BMS譜面オブジェクトのセットアップ。
	 * @param list 楽曲位置情報リスト
	 */
	void setup(List<BeMusicPoint> list) {
		// 初期設定
		mPoints = list;
		mNoteCount = 0;
		mLnCount = 0;
		mLmCount = 0;
		mChgScrollCount = 0;
		mChgBpmCount = 0;
		mStopCount = 0;
		mLastPlayableIndex = 0;
		mHasBgm = false;
		mHasBga = false;
		mScratchMode = BeMusicScratchMode.NORMAL;
		Arrays.fill(mNoteCountsDevice, 0);
		Arrays.fill(mLnCountsDevice, 0);
		Arrays.fill(mLmCountsDevice, 0);

		// 楽曲位置情報を分析する
		var listCount = list.size();
		var lastMeasure = -1;
		var lastTick = -1.0;
		var lastTime = -1.0;
		var lastScrPt = new BeMusicPoint[BeMusicLane.COUNT];
		for (var ptIndex = 0; ptIndex < listCount; ptIndex++) {
			var pt = list.get(ptIndex);
			var measure = pt.getMeasure();
			var tick = pt.getTick();
			var time = pt.getTime();

			// 小節が増加した場合は最終刻み位置を更新する
			if (measure > lastMeasure) { lastTick = -1.0; }

			// アサーション
			if (measure < lastMeasure) {
				var msg = String.format(
						"Detected incorrectly measure. expect-measure>=%d, actual-measure=%d, tick=%.16g",
						lastMeasure, measure, tick);
				throw new IllegalArgumentException(msg);
			}
			if (tick <= lastTick) {
				var msg = String.format(
						"Detected incorrectly tick. measure=%d, expect-tick>%.16g, actual-tick=%.16g",
						measure, lastTick, tick);
				throw new IllegalArgumentException(msg);
			}
			if (time <= lastTime) {
				var msg = String.format(
						"Detected incorrectly time. measure=%d, tick=%.16g, expect-time>%.16g, actual-time=%.16g",
						measure, tick, lastTime, time);
				throw new IllegalArgumentException(msg);
			}

			// 各ノート数を更新する
			for (var i = 0; i < BeMusicDevice.COUNT; i++) {
				var ntype = pt.getVisibleNoteType(BeMusicDevice.fromIndex(i));
				mNoteCountsDevice[i] += (ntype.isCountNotes() ? 1 : 0);
				mLnCountsDevice[i] += ((ntype.isCountNotes() && ntype.isLongNoteType()) ? 1 : 0);
				mLmCountsDevice[i] += ((ntype == BeMusicNoteType.MINE) ? 1 : 0);
			}

			// スクラッチモードを更新する
			for (var i = 0; i < BeMusicLane.COUNT; i++) {
				var scr = BeMusicDevice.getScratch(BeMusicLane.fromIndex(i));
				var curNt = pt.getVisibleNoteType(scr);
				if (curNt.hasMovement()) {
					var prevPt = lastScrPt[i];
					var prevNt = (prevPt == null) ? BeMusicNoteType.NONE : prevPt.getVisibleNoteType(scr);
					switch (curNt) {
					case BEAT:
						if ((prevNt == BeMusicNoteType.LONG_OFF) && ((time - prevPt.getTime()) <= MAX_PSEUDO_TIME)) {
							// LNモードで、長押し終端から次の通常ノートまでの時間が規定時間以内(疑似BSS)
							var canTransition = (mScratchMode == BeMusicScratchMode.NORMAL);
							mScratchMode = canTransition ? BeMusicScratchMode.BACKSPIN : mScratchMode;
						}
						break;
					case LONG_ON:
						if ((prevNt == BeMusicNoteType.LONG_OFF) && ((time - prevPt.getTime()) <= MAX_PSEUDO_TIME)) {
							// LNモードで、長押し終端から次の長押し開始までの時間が規定時間以内(疑似MSS)
							var canTransition = (mScratchMode != BeMusicScratchMode.MULTISPIN);
							mScratchMode = canTransition ? BeMusicScratchMode.MULTISPIN : mScratchMode;
						}
						break;
					case CHARGE_OFF: {
						// CN/HCNモードで、長押し終了を検出(正式BSS)
						var canTransition = (mScratchMode == BeMusicScratchMode.NORMAL);
						mScratchMode = canTransition ? BeMusicScratchMode.BACKSPIN : mScratchMode;
						break;
					}
					default:
						// それ以外の種別ではスクラッチモードの更新は起こり得ない
						break;
					}
					lastScrPt[i] = pt;
				}
			}

			// 最終操作可能ノート位置を更新する
			if (pt.hasPlayableNote()) {
				mLastPlayableIndex = ptIndex;
			}

			// その他の統計情報を更新する
			mNoteCount += pt.getNoteCount();
			mLnCount += pt.getLongNoteCount();
			mLmCount += pt.getMineCount();
			mChgScrollCount += (pt.hasScroll() ? 1 : 0);
			mChgBpmCount += (pt.hasBpm() ? 1 : 0);
			mStopCount += (pt.hasStop() ? 1 : 0);
			if (pt.hasBgm()) { mHasBgm = true; }
			if (pt.hasBga()) { mHasBga = true; }

			// アサーション対象データを記憶しておく
			lastMeasure = measure;
			lastTick = tick;
			lastTime = time;
		}

		// レーンごとの各種個数を計算する
		Arrays.fill(mNoteCountsLane, 0);
		Arrays.fill(mLnCountsLane, 0);
		Arrays.fill(mLmCountsLane, 0);
		for (var i = 0; i < BeMusicDevice.COUNT; i++) {
			var iLane = BeMusicDevice.fromIndex(i).getLane().getIndex();
			mNoteCountsLane[iLane] += mNoteCountsDevice[i];
			mLnCountsLane[iLane] += mLnCountsDevice[i];
			mLmCountsLane[iLane] += mLmCountsDevice[i];
		}

		// 推奨TOTAL値1を計算する
		var notes = (double)mNoteCount;
		mRecommendTotal1 = 7.605 * notes / (0.01 * notes + 6.5);

		// 推奨TOTAL値2を計算する
		mRecommendTotal2 =
				(notes < 400) ? (200.0 + notes / 5.0) :
				(notes < 600) ? (280.0 + (notes - 400.0) / 2.5) :
				                (360.0 + (notes - 600.0) / 5.0);

		// 各種要素の有無情報を設定する
		mChgSpeed = (mChgBpmCount > 0) || (mChgScrollCount > 0);
		mGimmick = mChgSpeed || (mStopCount > 0) || (mLmCount > 0);

		// 拡張情報取得用処理を実行する
		onCreate();
	}

	/**
	 * BMS譜面オブジェクトが構築された時に実行されます。
	 * <p>当メソッドが実行されるのはオブジェクトのベースクラスである{@link BeMusicChart}の構築処理が完了した後です。
	 * 従って、クラスのGetterを使用することで構築済みの情報にアクセス可能な状態となっています。</p>
	 * <p>当メソッドの意図は、ベースクラスを拡張したクラスにおいて自身が必要とする情報を構築する機会を提供する
	 * ことにあります。メソッドはコンストラクタの最後で実行され、当メソッドの実行が完了する時には全ての情報構築が
	 * 完了していることが推奨されています。</p>
	 */
	protected void onCreate() {
		// Do nothing
	}
}
