package com.lmt.lib.bms.bemusic;

import static com.lmt.lib.bms.internal.Assertion.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.lmt.lib.bms.BmsContent;
import com.lmt.lib.bms.BmsInt;
import com.lmt.lib.bms.BmsNote;
import com.lmt.lib.bms.BmsPoint;
import com.lmt.lib.bms.BmsSpec;
import com.lmt.lib.bms.internal.Utility;

/**
 * BMSコンテンツからBe Music仕様のBMS譜面オブジェクトを構築するためのビルダーです。
 *
 * <p>当ビルダーでは、BMS譜面オブジェクトにどのような情報を含めるかを選択し、楽曲の先頭から末尾までをスキャンします。
 * 譜面を画面に表示し、音声・アニメーションの再生までをカバーするようなBMS譜面オブジェクトを構築する場合には
 * 当ビルダーがサポートする全ての情報を含めなければなりません。視聴覚の演出を必要としないBMS譜面オブジェクトで
 * 良いのであれば音声・アニメーションの情報を除外した状態でBMS譜面オブジェクトを構築しても構いません。</p>
 *
 * <p>但し、どのような用途のBMS譜面オブジェクトであったとしても以下の情報だけは必ず含まれることになります。
 * これらは総じて「時間」に関連する情報となっています。</p>
 *
 * <ul>
 * <li>小節長の明示的な指定</li>
 * <li>スクロール速度変化に関する情報</li>
 * <li>BPM変化に関する情報</li>
 * <li>譜面停止に関する情報</li>
 * </ul>
 *
 * <p>以下に、当ビルダーの代表的な使い方を示す簡単なサンプルコードを示します。</p>
 *
 * <pre>
 * private BeMusicChart buildMyChart(BmsContent targetContent) {
 *     // 以下はBMS譜面の分析処理に必要な最低限の情報を含めたい場合の設定です。
 *     BeMusicChartBuilder builder = new BeMusicChartBuilder(targetContent)
 *         .setSeekMeasureLine(true)  // 小節線を含む
 *         .setSeekVisible(true)      // 可視オブジェを含む
 *         .setSeekInvisible(false)   // 不可視オブジェは無視する
 *         .setSeekMine(true)         // 地雷オブジェを含む
 *         .setSeekBgm(false)         // BGMは無視する
 *         .setSeekBga(false)         // BGA(レイヤー、ミス画像含む)は無視する
 *         .setSeekText(false);       // テキストは無視する
 *
 *     // 上記ビルダーで設定した条件でBMS譜面オブジェクトを構築します。
 *     BeMusicChart myChart = builder.createChart();
 *
 *     // 構築したBMS譜面オブジェクトを返します。
 *     return myChart;
 * }
 * </pre>
 *
 * @since 0.0.1
 */
public class BeMusicChartBuilder {
	/** 小節線をシークするかどうか */
	private boolean mSeekMeasureLine = false;
	/** 可視オブジェをシークするかどうか */
	private boolean mSeekVisible = false;
	/** 不可視オブジェをシークするかどうか */
	private boolean mSeekInvisible = false;
	/** 地雷オブジェをシークするかどうか */
	private boolean mSeekMine = false;
	/** BGMをシークするかどうか */
	private boolean mSeekBgm = false;
	/** BGAをシークするかどうか */
	private boolean mSeekBga = false;
	/** テキストをシークするかどうか */
	private boolean mSeekText = false;
	/** 楽曲位置情報生成関数 */
	private Supplier<BeMusicPoint> mPointCreator = BeMusicPoint::new;
	/** BMS譜面生成関数 */
	private Supplier<BeMusicChart> mChartCreator = BeMusicChart::new;

	/** 処理対象BMSコンテンツ */
	private BmsContent mContent;

	/** 現在のシーク位置 */
	private BmsPoint mCurAt;
	/** 現在のスクロール速度 */
	private double mCurScroll;
	/** 現在のBPM */
	private double mCurBpm;
	/** #LNOBJリスト */
	private List<Long> mLnObjs;
	/** ロングノート終端の種別 */
	private BeMusicNoteType mLnTail = null;
	/** 前回シークした楽曲位置情報 */
	private BeMusicPoint mPrevPoint;

	/** 現在の楽曲位置情報プロパティ */
	private PointProperty mPtProperty;
	/** 楽曲位置情報プロパティの新インスタンス生成を行ったかどうか */
	private boolean mPtPropertyNew;

	/** 可視オブジェのノート種別 */
	private BeMusicNoteType[] mVisibleTypes = new BeMusicNoteType[BeMusicDevice.COUNT];
	/** 可視オブジェの値 */
	private int[] mVisibleValues = new int[BeMusicPoint.VC];
	/** 不可視オブジェの値 */
	private int[] mInvisibles = new int[BeMusicPoint.IC];
	/** BGAの値 */
	private int[] mBgas = new int[BeMusicPoint.BC];
	/** BGMの値 */
	private List<Integer> mBgms = new ArrayList<>(64);
	/** 可視オブジェが存在するかどうか */
	private boolean mHasVisible;
	/** 不可視オブジェが存在するかどうか */
	private boolean mHasInvisible;
	/** BGAが存在するかどうか */
	private boolean mHasBga;

	/** 長押しノート継続中かを示すフラグのリスト(MGQ形式用) */
	private int[] mHoldingsMgq = new int[BeMusicDevice.COUNT];
	/** 長押しノート継続中かを示すフラグのリスト(RDM形式用) */
	private boolean[] mHoldingsRdm = new boolean[BeMusicDevice.COUNT];
	/** シーク中かどうか */
	private boolean mSeeking = false;
	/** シーク対象のチャンネル一式 */
	private Set<Integer> mTargetChannels = new HashSet<>();

	/**
	 * BMS譜面ビルダーオブジェクトを構築します。
	 * <p>BMS譜面ビルダーは必ず対象となるBMSコンテンツを指定する必要があります。また、当該BMSコンテンツは
	 * {@link BeMusicSpec}で生成されたBMS仕様に基づいたコンテンツでなければなりません。そうでない場合には
	 * 当ビルダーでの動作保証外になります。</p>
	 * <p>当コンストラクタで構築したBMS譜面ビルダーはデフォルトで全ての情報をシークする設定になっています。</p>
	 * @param content BMSコンテンツ
	 * @throws NullPointerException contentがnull
	 */
	public BeMusicChartBuilder(BmsContent content) {
		this(content, true);
	}

	/**
	 * BMS譜面ビルダーオブジェクトを構築します。
	 * <p>BMS譜面ビルダーは必ず対象となるBMSコンテンツを指定する必要があります。また、当該BMSコンテンツは
	 * {@link BeMusicSpec}で生成されたBMS仕様に基づいたコンテンツでなければなりません。そうでない場合には
	 * 当ビルダーでの動作保証外になります。</p>
	 * <p>当コンストラクタはオブジェクト構築時に全ての情報をシークするか、または最小限の情報のみシークするかを決定できます。
	 * isSeekAll引数をtrueにすると全ての情報をシークし、falseにすると最小限の情報のみシークします。</p>
	 * @param content BMSコンテンツ
	 * @param isSeekAll 全情報シーク有無
	 * @throws NullPointerException contentがnull
	 * @since 0.8.0
	 */
	public BeMusicChartBuilder(BmsContent content, boolean isSeekAll) {
		assertArgNotNull(content, "content");
		mContent = content;
		setSeekMeasureLine(isSeekAll);
		setSeekVisible(isSeekAll);
		setSeekInvisible(isSeekAll);
		setSeekMine(isSeekAll);
		setSeekBgm(isSeekAll);
		setSeekBga(isSeekAll);
		setSeekText(isSeekAll);
	}

	/**
	 * 小節線を楽曲位置情報に含めるかどうかを設定します。
	 * <p>小節線は、ある小節の刻み位置0であることを示します。通常、どの楽曲位置でも小節データ、ノートが全く存在しない
	 * 場合には楽曲位置情報は生成対象になりませんが、小節線を含めることで小節毎に必ず楽曲位置情報が含まれることを
	 * 保証することができます。アプリケーションの実装都合に合わせて小節線有無を決定してください。</p>
	 * @param seek 小節線有無
	 * @return このBMS譜面ビルダーオブジェクトのインスタンス
	 * @throws IllegalStateException BMS譜面生成中
	 */
	public BeMusicChartBuilder setSeekMeasureLine(boolean seek) {
		assertNotSeeking();
		mSeekMeasureLine = seek;
		return this;
	}

	/**
	 * 可視オブジェを楽曲位置情報に含めるかどうかを設定します。
	 * @param seek 可視オブジェ有無
	 * @return このBMS譜面ビルダーオブジェクトのインスタンス
	 * @throws IllegalStateException BMS譜面生成中
	 */
	public BeMusicChartBuilder setSeekVisible(boolean seek) {
		assertNotSeeking();
		mSeekVisible = seek;
		return this;
	}

	/**
	 * 不可視オブジェを楽曲位置情報に含めるかどうかを設定します。
	 * @param seek 不可視オブジェ有無
	 * @return このBMS譜面ビルダーオブジェクトのインスタンス
	 * @throws IllegalStateException BMS譜面生成中
	 */
	public BeMusicChartBuilder setSeekInvisible(boolean seek) {
		assertNotSeeking();
		mSeekInvisible = seek;
		return this;
	}

	/**
	 * 地雷オブジェを楽曲位置情報に含めるかどうかを設定します。
	 * @param seek 地雷オブジェ有無
	 * @return このBMS譜面ビルダーオブジェクトのインスタンス
	 * @throws IllegalStateException BMS譜面生成中
	 */
	public BeMusicChartBuilder setSeekMine(boolean seek) {
		assertNotSeeking();
		mSeekMine = seek;
		return this;
	}

	/**
	 * BGMを楽曲位置情報に含めるかどうかを設定します。
	 * @param seek BGM有無
	 * @return このBMS譜面ビルダーオブジェクトのインスタンス
	 * @throws IllegalStateException BMS譜面生成中
	 */
	public BeMusicChartBuilder setSeekBgm(boolean seek) {
		assertNotSeeking();
		mSeekBgm = seek;
		return this;
	}

	/**
	 * BGAを楽曲位置情報に含めるかどうかを設定します。
	 * @param seek BGA有無
	 * @return このBMS譜面ビルダーオブジェクトのインスタンス
	 * @throws IllegalStateException BMS譜面生成中
	 */
	public BeMusicChartBuilder setSeekBga(boolean seek) {
		assertNotSeeking();
		mSeekBga = seek;
		return this;
	}

	/**
	 * テキストを楽曲位置情報に含めるかどうかを設定します。
	 * @param seek テキスト有無
	 * @return このBMS譜面ビルダーオブジェクトのインスタンス
	 * @throws IllegalStateException BMS譜面生成中
	 */
	public BeMusicChartBuilder setSeekText(boolean seek) {
		assertNotSeeking();
		mSeekText = seek;
		return this;
	}

	/**
	 * 楽曲位置情報生成関数を設定します。
	 * <p>当メソッドで設定された関数は、BMS譜面を生成する過程で1個の楽曲位置情報オブジェクトを構築しようとする度に
	 * 1回実行されます。関数は、実行される度に新しい楽曲位置情報オブジェクトを生成して返してください。
	 * 生成済みの楽曲位置情報オブジェクトを返してはいけません。それを行った場合BMS譜面は正しく構築されず、
	 * 一切の動作保証外となります。また、nullを返すとBMS譜面構築時に例外がスローされます。</p>
	 * <p>デフォルトでは{@link BeMusicPoint}を生成する関数が設定されています。</p>
	 * @param creator 楽曲位置情報生成関数
	 * @return このBMS譜面ビルダーオブジェクトのインスタンス
	 * @throws IllegalStateException BMS譜面生成中
	 * @throws NullPointerException creatorがnull
	 * @since 0.8.0
	 */
	public BeMusicChartBuilder setPointCreator(Supplier<BeMusicPoint> creator) {
		assertNotSeeking();
		assertArgNotNull(creator, "creator");
		mPointCreator = creator;
		return this;
	}

	/**
	 * BMS譜面生成関数を設定します。
	 * <p>当メソッドで設定された関数は、{@link #createChart()}でBMS譜面を生成しようとする際に
	 * {@link BeMusicChart#create(List, Supplier)}に渡されます。その時に指定されたBMS譜面生成関数を1度だけ実行し、
	 * BMS譜面オブジェクトを構築します。指定した関数がnullを返す動作にならないよう注意してください。</p>
	 * <p>{@link #createChart()}を使用しない場合、当メソッドで指定した関数が参照されることはありません。</p>
	 * <p>デフォルトでは{@link BeMusicChart}を生成する関数が設定されています。</p>
	 * @param creator BMS譜面生成関数
	 * @return このBMS譜面ビルダーオブジェクトのインスタンス
	 * @throws IllegalStateException BMS譜面生成中
	 * @throws NullPointerException creatorがnull
	 * @since 0.8.0
	 */
	public BeMusicChartBuilder setChartCreator(Supplier<BeMusicChart> creator) {
		assertNotSeeking();
		assertArgNotNull(creator, "creator");
		mChartCreator = creator;
		return this;
	}

	/**
	 * 処理対象のBMSコンテンツを取得します。
	 * @return 処理対象のBMSコンテンツ
	 */
	public BmsContent getContent() {
		return mContent;
	}

	/**
	 * このBMS譜面ビルダーオブジェクトに指定された条件でBMS譜面構築を開始し、最初に楽曲位置情報を返します。
	 * <p>BMS譜面構築を開始すると、楽曲位置情報の生成が完了するまでビルダーの条件を変更できなくなります。</p>
	 * <p>当メソッドはビルダーの状態に関わらずいつでも実行できます。</p>
	 * <p>指定の条件に該当する楽曲位置が全く存在しない場合、当メソッドはnullを返します。</p>
	 * <p>当メソッドと{@link #next()}メソッドは低レベルAPIです。これらのメソッドを用いてBMS譜面を構築することは
	 * 推奨されません。代わりに{@link #createList()}または{@link #createChart()}を使用してください。</p>
	 * @return 楽曲位置情報
	 * @throws IllegalStateException 処理対象BMSコンテンツが参照モードではない
	 */
	public BeMusicPoint first() {
		setup();
		var first = createPoint(true);
		mPrevPoint = first;
		return first;
	}

	/**
	 * このBMS譜面ビルダーオブジェクトに指定された条件で次の楽曲位置を検索し、該当する楽曲位置の楽曲位置情報を返します。
	 * <p>当メソッドを呼び出す前には{@link #first()}が実行されている必要があります。その後、当メソッドが最後の楽曲位置まで
	 * 検索し終え、nullを返すまで繰り返し当メソッドを実行してください。nullを返すと当ビルダーのBMS譜面生成中状態が解除され、
	 * 再びSetterを実行可能になります。</p>
	 * <p>当メソッドと{@link #first()}メソッドは低レベルAPIです。これらのメソッドを用いてBMS譜面を構築することは
	 * 推奨されません。代わりに{@link #createList()}または{@link #createChart()}を使用してください。</p>
	 * @return 楽曲位置情報
	 * @throws IllegalStateException BMS譜面生成中ではない({@link #first()}が実行されていない)
	 * @throws IllegalStateException 処理対象BMSコンテンツが参照モードではない
	 */
	public BeMusicPoint next() {
		assertSeeking();
		var next = createPoint(false);
		mPrevPoint = next;
		return next;
	}

	/**
	 * このBMS譜面ビルダーオブジェクトに指定された条件で譜面を最初から最後まで検索し、楽曲位置情報リストを構築します。
	 * <p>当メソッドは{@link #first()}と{@link #next()}を使用して以下のサンプルと同様の処理を行います。</p>
	 * <pre>
	 * List&lt;BeMusicPoint&gt; list = new ArrayList&lt;&gt;();
	 * for (BeMusicPoint point = first(); point != null; point = next()) {
	 *     list.add(point);
	 * }
	 * </pre>
	 * @return 楽曲位置情報リスト
	 */
	public List<BeMusicPoint> createList() {
		var result = new ArrayList<BeMusicPoint>();
		for (var data = first(); data != null; data = next()) { result.add(data); }
		return result;
	}

	/**
	 * このBMS譜面ビルダーオブジェクトに指定された条件でBMS譜面オブジェクトを構築します。
	 * <p>当メソッドは{@link #createList()}で生成した楽曲位置情報リストを{@link BeMusicChart#create(List, Supplier)}に渡し、
	 * {@link BeMusicChart}オブジェクトを構築する手順を簡略化するヘルパーメソッドです。</p>
	 * @return BMS譜面オブジェクト
	 */
	public BeMusicChart createChart() {
		return BeMusicChart.create(createList(), mChartCreator);
	}

	/**
	 * 指定したBMSコンテンツからBMS譜面を生成します。
	 * <p>当メソッドを使用すると以下の記述を簡略化できます。</p>
	 * <pre>new BeMusicChartBuilder(content).createChart();</pre>
	 * @param content BMSコンテンツ
	 * @return BMS譜面オブジェクト
	 * @throws NullPointerException contentがnull
	 * @since 0.8.0
	 */
	public static BeMusicChart createChart(BmsContent content) {
		return (new BeMusicChartBuilder(content)).createChart();
	}

	/**
	 * セットアップ
	 */
	private void setup() {
		// コンテンツの状態をチェックする
		assertIsReferenceMode();

		// シーク対象となるチャンネルを収集する
		mTargetChannels.clear();

		// 必須チャンネル
		mTargetChannels.add(BmsInt.box(BeMusicChannel.LENGTH.getNumber()));
		mTargetChannels.add(BmsInt.box(BeMusicChannel.SCROLL.getNumber()));
		mTargetChannels.add(BmsInt.box(BeMusicChannel.BPM_LEGACY.getNumber()));
		mTargetChannels.add(BmsInt.box(BeMusicChannel.BPM.getNumber()));
		mTargetChannels.add(BmsInt.box(BeMusicChannel.STOP.getNumber()));

		// 小節線
		if (mSeekMeasureLine) {
			mTargetChannels.add(BmsInt.box(BmsSpec.CHANNEL_MEASURE));
		}

		// 可視・不可視・地雷オブジェ
		for (var l : BeMusicDevice.values()) {
			// 可視オブジェ
			if (mSeekVisible) {
				mTargetChannels.add(BmsInt.box(l.getVisibleChannel().getNumber()));
				mTargetChannels.add(BmsInt.box(l.getLongChannel().getNumber()));
			}
			// 不可視オブジェ
			if (mSeekInvisible) {
				mTargetChannels.add(BmsInt.box(l.getInvisibleChannel().getNumber()));
			}
			// 地雷オブジェ
			if (mSeekMine) {
				mTargetChannels.add(BmsInt.box(l.getMineChannel().getNumber()));
			}
		}

		// BGM
		if (mSeekBgm) {
			mTargetChannels.add(BmsInt.box(BeMusicChannel.BGM.getNumber()));
		}

		// BGA関連
		if (mSeekBga) {
			mTargetChannels.add(BmsInt.box(BeMusicChannel.BGA.getNumber()));
			mTargetChannels.add(BmsInt.box(BeMusicChannel.BGA_LAYER.getNumber()));
			mTargetChannels.add(BmsInt.box(BeMusicChannel.BGA_MISS.getNumber()));
		}

		// テキスト
		if (mSeekText) {
			mTargetChannels.add(BmsInt.box(BeMusicChannel.TEXT.getNumber()));
		}

		// 楽曲位置情報プロパティ
		mPtProperty = new PointProperty();
		mPtProperty.bpm = mContent.getInitialBpm();
		mPtPropertyNew = false;

		// その他のセットアップ
		mCurAt = new BmsPoint(0, 0);
		mCurScroll = 1.0;
		mCurBpm = mContent.getInitialBpm();
		mLnObjs = mContent.getMultipleMetas(BeMusicMeta.LNOBJ.getName());
		mLnTail = BeMusicMeta.getLnMode(mContent).getTailType();
		mPrevPoint = null;
		Arrays.fill(mHoldingsMgq, 0);
		Arrays.fill(mHoldingsRdm, false);
		mSeeking = true;
	}

	/**
	 * 楽曲位置情報生成
	 * @param inclusiveFrom 現在位置を含めた検索を行うかどうか
	 * @return 楽曲位置情報
	 * @throws IllegalStateException 対象BMSコンテンツが参照モードではない
	 * @throws NullPointerException 楽曲位置情報生成関数がnullを返した
	 */
	private BeMusicPoint createPoint(boolean inclusiveFrom) {
		// アサーション
		assertIsReferenceMode();

		// 次の楽曲位置を検索する
		mContent.seekNextPoint(
				mCurAt.getMeasure(),
				mCurAt.getTick(),
				inclusiveFrom,
				ch -> mTargetChannels.contains(BmsInt.box(ch)),
				mCurAt);
		if (mCurAt.getMeasure() == mContent.getMeasureCount()) {
			// 楽曲の最後まで検索し終わった場合はシークを終了してnullを返す
			mSeeking = false;
			return null;
		}

		// 楽曲位置情報の編集開始
		propInit();
		noteInit();

		// 楽曲位置情報を生成する
		var curMeasure = mCurAt.getMeasure();
		var curTick = mCurAt.getTick();
		var note = (BmsNote)null;
		var note2 = (BmsNote)null;
		var pt = mPointCreator.get();
		if (pt == null) {
			throw new NullPointerException("Point creator returned null");
		}

		// 時間
		var curTime = mContent.pointToTime(mCurAt);
		var prevTime = (mPrevPoint == null) ? 0.0 : mPrevPoint.getTime();
		if ((curTime <= prevTime) && (mPrevPoint != null)) {
			curTime = Math.nextUp(prevTime);
		}

		// 小節長
		if (mContent.containsMeasureValue(BeMusicChannel.LENGTH.getNumber(), curMeasure)) {
			// 小節長変更データが存在する
			var length = (double)mContent.getMeasureValue(BeMusicChannel.LENGTH.getNumber(), curMeasure) * -1.0;
			propSet(p -> p.length != length, p -> p.length = length);
		} else {
			// 小節長変更データが存在しないので等倍とする
			var length = 1.0;
			propSet(p -> p.length != length, p -> p.length = length);
		}

		// スクロール速度変更
		if ((note = mContent.getNote(BeMusicChannel.SCROLL.getNumber(), mCurAt)) != null) {
			// スクロール速度変更あり
			mCurScroll = (Double)mContent.getResolvedNoteValue(BeMusicChannel.SCROLL.getNumber(), mCurAt);
			propSet(p -> p.changeScroll != true, p -> p.changeScroll = true);
			propSet(p -> p.scroll != mCurScroll, p -> p.scroll = mCurScroll);
		} else {
			// スクロール速度変更が存在しない場合は現在のスクロール速度を設定する
			propSet(p -> p.changeScroll != false, p -> p.changeScroll = false);
			propSet(p -> p.scroll != mCurScroll, p -> p.scroll = mCurScroll);
		}

		// BPM変更
		if ((note = mContent.getNote(BeMusicChannel.BPM.getNumber(), mCurAt)) != null) {
			// BPM変更データあり
			mCurBpm = (Double)mContent.getResolvedNoteValue(BeMusicChannel.BPM.getNumber(), mCurAt);
			propSet(p -> p.bpm != -mCurBpm, p -> p.bpm = -mCurBpm);
		} else if ((note = mContent.getNote(BeMusicChannel.BPM_LEGACY.getNumber(), mCurAt)) != null) {
			// BPM変更データ(旧形式)あり
			mCurBpm = (double)note.getValue();
			propSet(p -> p.bpm != -mCurBpm, p -> p.bpm = -mCurBpm);
		} else {
			// BPM変更データが存在しない場合は現在のBPMを設定する
			propSet(p -> p.bpm != mCurBpm, p -> p.bpm = mCurBpm);
		}

		// 譜面停止
		if ((note = mContent.getNote(BeMusicChannel.STOP.getNumber(), mCurAt)) != null) {
			// 譜面停止データあり
			var stopValue = (double)mContent.getResolvedNoteValue(BeMusicChannel.STOP.getNumber(), mCurAt);
			var stopTime = Utility.computeTime(stopValue, mCurBpm);
			propSet(p -> p.stop != stopTime, p -> p.stop = stopTime);
		} else {
			// 譜面停止データなし
			var stopTime = 0.0;
			propSet(p -> p.stop != stopTime, p -> p.stop = stopTime);
		}

		// テキスト
		if (mSeekText) {
			var text = (String)mContent.getResolvedNoteValue(BeMusicChannel.TEXT.getNumber(), mCurAt);
			propSet(p -> !p.text.equals(text), p -> p.text = text);
		}

		// 全ラインの可視・不可視・地雷オブジェ
		for (var i = 0; i < BeMusicDevice.COUNT; i++) {
			var dev = BeMusicDevice.fromIndex(i);
			var chNum = 0;

			// 可視オブジェ
			if (mSeekVisible) {
				chNum = dev.getLongChannel().getNumber();
				if ((note = mContent.getNote(chNum, mCurAt)) != null) {
					// MGQ形式のロングノートチャンネルを解析する
					var lnValue = note.getValue();
					var curHolding = mHoldingsMgq[i];
					if (curHolding == 0) {
						// 長押しが開始されていなかった場合は長押しを開始する
						var lnRdm = mHoldingsRdm[i];
						setVisible(dev, lnRdm ? BeMusicNoteType.LONG : BeMusicNoteType.LONG_ON, lnValue);
						mHoldingsMgq[i] = lnValue;
						mHoldingsRdm[i] = false;  // RDM形式のLNは強制OFF
					} else {
						// 長押し継続中の場合は長押しを終了とする
						// 開始と終了の値が異なる場合でも強制的に終了とする。
						// (世に出回っているBMSには、稀に開始と終了の値が異なるものがある)
						setVisible(dev, getLnTailType(lnValue), lnValue);
						mHoldingsMgq[i] = 0;
					}
				} else if (mHoldingsMgq[i] != 0) {
					// 当該入力デバイスが長押しノート継続中の場合は「長押しノート継続中」を設定する
					// MGQ形式のLNがONの場合は通常オブジェとRDM形式のLNは解析しない(MGQ形式LNを上に被せるイメージ)
					setVisible(dev, BeMusicNoteType.LONG, 0);
				} else {
					// 通常オブジェ、RDM形式のLNを解析する
					chNum = dev.getVisibleChannel().getNumber();
					if ((note = mContent.getNote(chNum, mCurAt)) != null) {
						// ノートの種別を判定する
						if (mLnObjs.contains((long)note.getValue())) {
							// ロングノート終了オブジェ
							note2 = mContent.getPreviousNote(chNum, mCurAt, false);
							if ((note2 != null) && !mLnObjs.contains((long)note2.getValue())) {
								// 前のノートが通常オブジェの場合はロングノート終了として扱う
								var noteValue = note.getValue();
								setVisible(dev, getLnTailType(noteValue), noteValue);
								mHoldingsRdm[i] = false;
							} else {
								// 前のノートなし、または続けてロングノート終了オブジェ検出時は通常オブジェとして扱う
								setVisible(dev, BeMusicNoteType.BEAT, note.getValue());
							}
						} else {
							// 通常オブジェ
							note2 = mContent.getNextNote(chNum, mCurAt, false);
							if ((note2 != null) && mLnObjs.contains((long)note2.getValue())) {
								// 次のノートがロングノート終了オブジェの場合はロングノート開始として扱う
								setVisible(dev, BeMusicNoteType.LONG_ON, note.getValue());
								mHoldingsRdm[i] = true;
							} else {
								// 次のノートがロングノート終了オブジェではない場合は通常オブジェとして扱う
								setVisible(dev, BeMusicNoteType.BEAT, note.getValue());
							}
						}
					} else if (mHoldingsRdm[i]) {
						// 当該入力デバイスが長押しノート継続中の場合は「長押しノート継続中」を設定する
						setVisible(dev, BeMusicNoteType.LONG, 0);
					}
				}
			}

			// 不可視オブジェ
			if (mSeekInvisible) {
				chNum = dev.getInvisibleChannel().getNumber();
				if ((note = mContent.getNote(chNum, mCurAt)) != null) {
					// 不可視オブジェを検出した場合は無条件に設定する
					setInvisible(dev, note.getValue());
				}
			}

			// 地雷オブジェ
			if (mSeekMine) {
				chNum = dev.getMineChannel().getNumber();
				if (!getVisibleNoteType(dev).hasVisualEffect() && ((note = mContent.getNote(chNum, mCurAt)) != null)) {
					// 当該ラインに可視オブジェがない場合に限り、地雷オブジェを設定する
					setVisible(dev, BeMusicNoteType.MINE, note.getValue());
				}
			}
		}

		// BGM
		if (mSeekBgm) {
			mContent.timeline(mCurAt)
					.filter(e -> e.getChannel() == BeMusicChannel.BGM.getNumber())
					.forEach(e -> setBgm((int)e.getValueAsLong()));
		}

		// BGA関連
		if (mSeekBga) {
			note = mContent.getNote(BeMusicChannel.BGA.getNumber(), mCurAt);
			var bga = (note == null) ? 0 : note.getValue();
			note = mContent.getNote(BeMusicChannel.BGA_LAYER.getNumber(), mCurAt);
			var layer = (note == null) ? 0 : note.getValue();
			note = mContent.getNote(BeMusicChannel.BGA_MISS.getNumber(), mCurAt);
			var miss = (note == null) ? 0 : note.getValue();
			setBga(bga, layer, miss);
		}

		// 楽曲位置情報のセットアップ
		var vt = mHasVisible ? mVisibleTypes : null;
		var vv = mHasVisible ? mVisibleValues : null;
		var iv = mHasInvisible ? mInvisibles : null;
		var bg = mHasBga ? mBgas : null;
		pt.setup(curMeasure, curTick, curTime, mPtProperty, vt, vv, iv, bg, mBgms);

		return pt;
	}

	/**
	 * ロングノート終端の種別取得
	 * <p>bmson(beatoraja固有)の、ノート単位でのロングノート種別指定機能に対応する。
	 * "t"の値により種別指定がある場合、#LNMODEの内容に限らず"t"の値を使用し、種別指定がない場合は#LNMODEの値を返す。</p>
	 * @param noteValue ノートの値
	 * @return ロングノート終端の種別
	 */
	private BeMusicNoteType getLnTailType(int noteValue) {
		var lnMode = BeMusicSound.getLongNoteMode(noteValue, null);
		return (lnMode == null) ? mLnTail : lnMode.getTailType();
	}

	/**
	 * 楽曲位置情報プロパティ編集初期化
	 */
	private void propInit() {
		mPtPropertyNew = false;
	}

	/**
	 * 楽曲位置情報プロパティ項目設定
	 * <p>前回楽曲位置のプロパティとの差分有無を判定関数でチェックし、差分ありと判定されればプロパティの更新を行う。
	 * 同一楽曲位置内で初の差分検出時はプロパティのインスタンスを新しいものに入れ替える。
	 * 全項目同一である限り、同じインスタンスのプロパティが参照され続ける。</p>
	 * @param hasDelta 前回プロパティからの差分有無判定関数
	 * @param updater 項目アップデータ
	 */
	private void propSet(Predicate<PointProperty> hasDelta, Consumer<PointProperty> updater) {
		if (hasDelta.test(mPtProperty)) {
			if (!mPtPropertyNew) {
				mPtPropertyNew = true;
				mPtProperty = new PointProperty(mPtProperty);
			}
			updater.accept(mPtProperty);
		}
	}

	/**
	 * ノートの初期化
	 */
	private void noteInit() {
		Arrays.fill(mVisibleTypes, BeMusicNoteType.NONE);
		Arrays.fill(mVisibleValues, 0);
		Arrays.fill(mBgas, 0);
		mBgms.clear();
		mHasVisible = false;
		mHasInvisible = false;
		mHasBga = false;
	}

	/**
	 * 可視オブジェのノート種別取得
	 * @param device 入力デバイス
	 * @return 可視オブジェのノート種別
	 */
	private BeMusicNoteType getVisibleNoteType(BeMusicDevice device) {
		return mVisibleTypes[device.getIndex()];
	}

	/**
	 * 可視オブジェ設定
	 * @param device 入力デバイス
	 * @param noteType ノート種別
	 * @param value ノートの値(トラックID・音声データの再開要否・ロングノートモードの組み合わせ)
	 */
	private void setVisible(BeMusicDevice device, BeMusicNoteType noteType, int value) {
		var index = device.getIndex();
		mVisibleTypes[index] = noteType;
		mVisibleValues[index] = value;
		mHasVisible = true;
	}

	/**
	 * 不可視オブジェ設定
	 * @param device 入力デバイス
	 * @param trackId トラックID
	 */
	private void setInvisible(BeMusicDevice device, int trackId) {
		mInvisibles[device.getIndex()] = trackId;
		mHasInvisible = true;
	}

	/**
	 * BGA設定
	 * @param bga BGAのトラックID
	 * @param layer LayerのトラックID
	 * @param miss MissのトラックID
	 */
	private void setBga(int bga, int layer, int miss) {
		mBgas[0] = bga;
		mBgas[1] = layer;
		mBgas[2] = miss;
		mHasBga = mHasBga || (bga != 0) || (layer != 0) || (miss != 0);
	}

	/**
	 * BGM設定(追加)
	 * @param trackId トラックID
	 */
	private void setBgm(int trackId) {
		mBgms.add(BmsInt.box(trackId));
	}

	/**
	 * シーク中であることを確認するアサーション
	 * @throws IllegalStateException シーク中ではない
	 */
	private void assertSeeking() {
		assertField(mSeeking, "This operation can't be performed while NOT seeking.");
	}

	/**
	 * シーク中でないことを確認するアサーション
	 * @throws IllegalStateException シーク中
	 */
	private void assertNotSeeking() {
		assertField(!mSeeking, "This operation can't be performed while seeking.");
	}

	/**
	 * 対象コンテンツが参照モードであることを確認するアサーション
	 * @throws IllegalStateException 対象コンテンツが参照モードではない
	 */
	private void assertIsReferenceMode() {
		assertField(mContent.isReferenceMode(), "Content is NOT reference mode.");
	}
}
