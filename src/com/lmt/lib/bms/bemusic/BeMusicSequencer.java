package com.lmt.lib.bms.bemusic;

import static com.lmt.lib.bms.internal.Assertion.*;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.function.Consumer;

/**
 * BMS譜面の楽曲位置情報に含まれる時間情報に従って譜面の再生動作を行うシーケンサです。
 *
 * <p>シーケンサは時間経過を監視し、然るべき時間に到達すると再生動作を行うべき楽曲位置情報の通知を順次行います。
 * シーケンサを利用することでアプリケーションはBMS譜面の処理タイミングの考慮から解放され、
 * 再生動作のロジック実装に集中することができるようになります。</p>
 *
 * <p>Be-Musicサブセットが提供するシーケンサでは、一般的な音楽プレーヤーのような再生・停止・一時停止・再開機能に加え、
 * 再生位置の設定・取得にも対応しているので、あらゆる種類のアプリケーションに応用することが可能です。
 * アプリケーションはシーケンサが提供する各種イベント通知を必要な分だけ実装することで、
 * 目的とする機能を少ないコーディング量で実現できます。イベント通知の詳細は「on」で始まるメソッドを参照してください。</p>
 *
 * <p><strong>状態遷移</strong></p>
 *
 * <p>シーケンサは一般の音楽プレーヤーと似た状態を持ち、以下の図のような遷移を行います。</p>
 *
 * <pre>
 *                                                   +-----------+
 *          +------------------stop------------------| COMPLETED |
 *          |                                        +-----------+
 *          |                                              1
 *          |                      +-------complete--------+
 *          v                      |
 *       +------+             +---------+              +---------+
 * (*)--&gt;| IDLE |----play----&gt;| PLAYING |----pause----&gt;| PAUSING |
 *       +------+             +---------+              +---------+
 *         1  1                  |   1                    |   |
 *         |  +-------stop-------+   +-------resume-------+   |
 *         |                                                  |
 *         +-----------------------stop-----------------------+</pre>
 *
 * <p><strong>再生制御と状態</strong></p>
 *
 * <p>各再生制御機能は前述の状態により使用可否が異なります。どの状態の時にどの再生制御が有効かは以下の表を参照してください。
 * 「n/a」と記載されている箇所は、その機能はその状態の時には使用できず、実行しても何も行われないことを示します。</p>
 *
 * <table><caption>&nbsp;</caption>
 * <tr><th>Func/Status</th><th>IDLE</th><th>PLAYING</th><th>PAUSING</th><th>COMPLETED</th></tr>
 * <tr><td>play</td><td>OK</td><td>n/a</td><td>n/a</td><td>n/a</td></tr>
 * <tr><td>stop</td><td>n/a</td><td>OK</td><td>OK</td><td>OK</td></tr>
 * <tr><td>pause</td><td>n/a</td><td>OK</td><td>n/a</td><td>n/a</td></tr>
 * <tr><td>resume</td><td>n/a</td><td>n/a</td><td>OK</td><td>n/a</td></tr>
 * <tr><td>setPosition</td><td>OK</td><td>OK</td><td>OK</td><td>n/a</td></tr>
 * </table>
 *
 * <p><strong>イベント通知について</strong></p>
 *
 * <p>当クラスではシーケンサの各種動作状況を検知するためのイベントが用意されています。
 * 「on」で始まる名前のメソッドがイベント通知が行われるメソッドです。これらのメソッドは必ず {@link #loop(boolean)},
 * {@link #update()} を呼び出したスレッドから呼び出されることに留意してください。</p>
 */
public abstract class BeMusicSequencer {
	/** 当クラスが提唱するループ待機時間の規定値(秒) */
	public static final double SLEEP_TIME_DEFAULT = 0.001;
	/** ループ待機時間の最大値(秒) */
	public static final double SLEEP_TIME_MAX = 1.0;
	/** 再生制御要求キューのサイズ */
	public static final int QUEUE_SIZE = 64;

	/** 再生対象のBMS譜面 */
	private BeMusicChart mChart;
	/** 再生時間(秒) */
	private double mDuration;
	/** 現在位置(秒) */
	private double mPosition;

	/** ループ・更新処理の排他制御用セマフォ */
	private Semaphore mUpdateSemaphore = new Semaphore(1);
	/** 時間計算の基準値(ナノ秒) */
	private long mBaseTimeNs = 0L;
	/** 現在位置の基準値(秒) */
	private double mBasePosition = 0.0;

	/** 現在のシーケンサの状態 */
	private Status mStatus = Status.IDLE;
	/** 状態ごとの更新処理呼び出し先マップ */
	private Map<Status, Runnable> mStatusHandlers = Map.of(
			Status.IDLE, this::onUpdate,
			Status.PLAYING, this::processPlaying,
			Status.PAUSING, this::onUpdate,
			Status.COMPLETED, this::onUpdate);

	/** 再生制御要求キュー */
	private Queue<RequestControl> mRequests = new LinkedBlockingQueue<>(QUEUE_SIZE);
	/** 再生制御要求種別ごとの再生制御処理呼び出し先マップ */
	private Map<Request, Consumer<RequestControl>> mRequestHandlers = Map.of(
			Request.PLAY, this::processPlayRequest,
			Request.STOP, this::processStopRequest,
			Request.PAUSE, this::processPauseRequest,
			Request.RESUME, this::processResumeRequest,
			Request.SEEK, this::processSeekRequest);

	/** シーケンサの状態 */
	private static enum Status {
		/** 初期状態(停止中) */
		IDLE,
		/** 再生中 */
		PLAYING,
		/** 一時停止中 */
		PAUSING,
		/** 再生完了 */
		COMPLETED;
	}

	/** 要求種別 */
	private static enum Request {
		/** 再生要求 */
		PLAY,
		/** 停止要求 */
		STOP,
		/** 一時停止要求 */
		PAUSE,
		/** 再開要求 */
		RESUME,
		/** 再生位置設定要求 */
		SEEK;
	}

	/** 再生制御要求データ */
	private static class RequestControl {
		/** 要求種別 */
		Request request;
		/** パラメータ(double) */
		double fParam;
	}

	/** シンプルなシーケンサ */
	private static class SimpleSequencer extends BeMusicSequencer {
		/** ループ待機時間(秒) */
		private double mSleepTime;
		/** 楽曲位置情報処理関数 */
		private Consumer<BeMusicPoint> mProcessor;

		/**
		 * コンストラクタ
		 * @param chart 再生対象のBMS譜面
		 * @param duration 再生時間
		 * @param sleepTime ループ待機時間(秒)
		 * @param processor 楽曲位置情報処理関数
		 * @exception NullPointerException chartがnull
		 * @exception IllegalArgumentException durationがマイナス値
		 * @exception IllegalArgumentException sleepTimeが{@link #SLEEP_TIME_MAX}超過
		 * @exception NullPointerException processorがnull
		 */
		SimpleSequencer(BeMusicChart chart, double duration, double sleepTime, Consumer<BeMusicPoint> processor) {
			super(chart, duration);
			assertArg(sleepTime <= SLEEP_TIME_MAX, "Invalid sleepTime: %s", sleepTime);
			assertArgNotNull(processor, "processor");
			mSleepTime = sleepTime;
			mProcessor = processor;
		}

		/** {@inheritDoc} */
		@Override
		protected void onPoint(BeMusicPoint p) {
			mProcessor.accept(p);
		}

		/** {@inheritDoc} */
		@Override
		protected double onGetSleepTime() {
			return mSleepTime;
		}
	}

	/**
	 * 新しいシーケンサオブジェクトを構築します。
	 * <p>当コンストラクタを使用すると、再生時間は指定されたBMS譜面の {@link BeMusicChart#getPlayTime()} から取得します。</p>
	 * @param chart 再生対象のBMS譜面
	 * @exception NullPointerException chartがnull
	 * @see #BeMusicSequencer(BeMusicChart, double)
	 */
	protected BeMusicSequencer(BeMusicChart chart) {
		this(chart, chart.getPlayTime());
	}

	/**
	 * 新しいシーケンサオブジェクトを構築します。
	 * <p>シーケンサは再生対象のBMS譜面と再生時間が必須入力情報となります。
	 * アプリケーションでシーケンサを実装する際はオブジェクト構築時にこれらを指定できるように設計してください。</p>
	 * @param chart 再生対象のBMS譜面
	 * @param duration 再生時間
	 * @exception NullPointerException chartがnull
	 * @exception IllegalArgumentException durationがマイナス値
	 */
	protected BeMusicSequencer(BeMusicChart chart, double duration) {
		setup(chart, duration);
	}

	/**
	 * このシーケンサが再生対象としているBMS譜面を取得します。
	 * @return 再生対象のBMS譜面
	 */
	public BeMusicChart getChart() {
		return mChart;
	}

	/**
	 * このシーケンサの再生時間(秒)を取得します。
	 * @return 再生時間(秒)
	 */
	public double getDuration() {
		return mDuration;
	}

	/**
	 * 現在の再生位置(秒)を取得します。
	 * <p>再生位置はループ処理または更新処理によって時間経過処理を行うことで更新されます。
	 * 時間経過処理を行わない場合、常に同じ値を返すことに注意してください。</p>
	 * @return 現在の再生位置(秒)
	 */
	public double getPosition() {
		return mPosition;
	}

	/**
	 * 現在の再生位置を指定した位置に設定します。
	 * <p>当メソッドを呼び出すと再生位置を指定した位置に更新する要求が再生制御要求キューに投入されます。
	 * 要求が処理されるのはループ処理または更新処理によって時間経過処理が行われる時です。
	 * 要求が受理されると現在の再生位置を更新し、{@link #onSeek(double)} が呼び出されます。</p>
	 * <p>再生位置の設定を再生が完了している状態で実行しても再生位置は更新されません。
	 * 例外はスローされないので再生位置を更新可能な状態かどうかは {@link #isCompleted()} で調べてください。</p>
	 * @param newPosition 新しい再生位置(秒)
	 * @exception IllegalArgumentException newPositionがマイナス値または再生時間超過
	 * @exception IllegalStateException 再生制御要求キューに空きがなく要求が受理できなかった
	 */
	public void setPosition(double newPosition) {
		assertArgRange(newPosition, 0.0, mDuration, "newPosition");
		requestControl(Request.SEEK, newPosition);
	}

	/**
	 * シーケンサの状態が「再生中」かどうかを取得します。
	 * @return シーケンサの状態が「再生中」であればtrue
	 */
	public boolean isPlaying() {
		return mStatus == Status.PLAYING;
	}

	/**
	 * シーケンサの状態が「一時停止中」かどうかを取得します。
	 * @return シーケンサの状態が「一時停止中」であればtrue
	 */
	public boolean isPausing() {
		return mStatus == Status.PAUSING;
	}

	/**
	 * シーケンサの状態が「初期状態」かどうかを取得します。
	 * @return シーケンサの状態が「初期状態」であればtrue
	 */
	public boolean isStopped() {
		return mStatus == Status.IDLE;
	}

	/**
	 * シーケンサの状態が「再生完了」かどうかを取得します。
	 * @return シーケンサの状態が「再生完了」であればtrue
	 */
	public boolean isCompleted() {
		return mStatus == Status.COMPLETED;
	}

	/**
	 * シーケンサの「時間経過処理」をループします。
	 * <p>当メソッドは、呼び出し元スレッドによってシーケンサの時間経過処理をループします。
	 * 実際にループが開始される前に {@link #onStartLoop()} が呼び出され、その後再生が終了するまで時間経過処理が繰り返されます。
	 * このループ処理は呼び出し元スレッドが割り込まれるか、exitWhenCompleteがtrueの場合はシーケンサの状態が「再生終了」
	 * になるまで繰り返されます。</p>
	 * <p>時間経過処理が1回処理される度にループの待機処理が入り、CPU負荷の軽減措置が行われます。
	 * 待機時間は毎回 {@link #onGetSleepTime()} が返す値によって決定されます。詳細はイベントの説明を参照してください。
	 * 待機時間が長くなると、その分次の時間経過処理で一度に処理される楽曲位置情報の量が多くなります。
	 * サウンドの再生を伴うシーケンサの場合、それにより正確なサウンドの再生タイミングが確保できなくなりますので、
	 * 待機時間を長くしすぎないように設計してください。</p>
	 * <p>ループ処理が終了すると {@link #onFinishLoop(Throwable)} が呼び出され、当メソッドから制御が戻ります。
	 * ループ処理中に例外がスローされるとループ処理から抜け、その例外が前述のイベントで通知されます。
	 * 通知された例外はイベント処理終了後に呼び出し元へスローされます。</p>
	 * <p>当メソッドと {@link #update()} は相互排他の関係にあり、どちらかが実行されている間はもう片方を実行できません。
	 * また、複数スレッドによる当メソッドの多重実行もできません。</p>
	 * @param exitWhenComplete シーケンサの状態が「再生終了」になった時ループ処理を終了するかどうか
	 * @exception IllegalStateException 別スレッドがループ処理を実行中
	 * @exception IllegalStateException {@link #update()} を実行中
	 * @see #update()
	 */
	public void loop(boolean exitWhenComplete) {
		lockUpdate();
		var uncaught = (Throwable)null;
		try {
			onStartLoop();
			for (var isIntr = false; !isIntr && (!exitWhenComplete || !isCompleted()); isIntr = processSleep()) {
				processUpdate();
			}
		} catch (Throwable e) {
			uncaught = e;
			throw e;
		} finally {
			try {
				onFinishLoop(uncaught);
			} catch (Exception e) {
				throw e;
			} finally {
				unlockUpdate();
			}
		}
	}

	/**
	 * シーケンサの「時間経過処理」を1回実行します。
	 * <p>シーケンサの状態が再生中の時に当メソッドを呼び出すと、前回の時間経過処理からの時間分だけ時間経過を行います。
	 * その時間経過で通過した楽曲位置が {@link #onPoint(BeMusicPoint)} イベントに通知され、その後 {@link #onUpdate()}
	 * イベントが呼び出されます。</p>
	 * <p>当メソッドによる時間経過処理では待機処理は実行されません。よって {@link #onGetSleepTime()} も呼び出されません。</p>
	 * <p>当メソッドはアプリケーションの他の箇所で実装されたループ処理・待機処理の中に、
	 * シーケンサの処理を組み込みたい場合に役立ちます。ただし、シーケンサがサウンドの再生を行うケースでは、
	 * 当メソッドの呼び出し間隔が空きすぎないように注意してください。
	 * 間隔が空きすぎると正確なサウンドの再生タイミングが確保できなくなります。</p>
	 * <p>当メソッドと {@link #loop(boolean)} は相互排他の関係にあり、どちらかが実行されている間はもう片方を実行できません。
	 * また、複数スレッドによる当メソッドの多重実行もできません。</p>
	 * @exception IllegalStateException 別スレッドが時間経過処理を実行中
	 * @exception IllegalStateException {@link #loop(boolean)} を実行中
	 */
	public void update() {
		lockUpdate();
		try {
			processUpdate();
		} finally {
			unlockUpdate();
		}
	}

	/**
	 * BMS譜面の再生を開始します。
	 * <p>当メソッドを呼び出すと再生要求が再生制御要求キューに投入されます。
	 * 要求が処理されるのはループ処理または更新処理によって時間経過処理が行われる時です。
	 * 要求が受理されると現在の再生位置から時間経過のカウントを開始し、{@link #onPlay()} が呼び出されます。</p>
	 * <p>当メソッドが作用するのはシーケンサの状態が「初期状態」の時のみです。それ以外の状態では何も行われません。
	 * 例外はスローされないので再生可能な状態かどうかは {@link #isStopped()} で調べてください。</p>
	 * @exception IllegalStateException 再生制御要求キューに空きがなく要求が受理できなかった
	 */
	public void play() {
		requestControl(Request.PLAY);
	}

	/**
	 * BMS譜面の再生を停止します。
	 * <p>当メソッドを呼び出すと停止要求が再生制御要求キューに投入されます。
	 * 要求が処理されるのはループ処理または更新処理によって時間経過処理が行われる時です。
	 * 要求が受理されると再生位置が0にリセットされ、{@link #onStop()} が呼び出されます。</p>
	 * <p>既に停止されている状態で当メソッドを呼び出しても何も行われません。
	 * 例外はスローされないので停止可能な状態かどうかは {@link #isStopped()} で調べてください。</p>
	 * @exception IllegalStateException 再生制御要求キューに空きがなく要求が受理できなかった
	 */
	public void stop() {
		requestControl(Request.STOP);
	}

	/**
	 * BMS譜面の再生を一時停止します。
	 * <p>当メソッドを呼び出すと一時停止要求が再生制御要求キューに投入されます。
	 * 要求が処理されるのはループ処理または更新処理によって時間経過処理が行われる時です。
	 * 要求が受理されると時間経過処理による再生位置のカウントが停止され、{@link #onPause()} が呼び出されます。</p>
	 * <p>当メソッドが作用するのはシーケンサの状態が「再生中」の時のみです。それ以外の状態では何も行われません。
	 * 例外はスローされないので一時停止可能な状態かどうかは {@link #isPlaying()} で調べてください。</p>
	 * @exception IllegalStateException 再生制御要求キューに空きがなく要求が受理できなかった
	 */
	public void pause() {
		requestControl(Request.PAUSE);
	}

	/**
	 * BMS譜面の再生を再開します。
	 * <p>当メソッドを呼び出すと再開要求が再生制御要求キューに投入されます。
	 * 要求が処理されるのはループ処理または更新処理によって時間経過処理が行われる時です。
	 * 要求が受理されると時間経過処理による再生位置のカウントが再開され、{@link #onResume()} が呼び出されます。</p>
	 * <p>当メソッドが作用するのはシーケンサの状態が「一時停止中」の時のみです。それ以外の状態では何も行われません。
	 * 例外はスローされないので再開可能な状態かどうかは {@link #isPausing()} で調べてください。</p>
	 * @exception IllegalStateException 再生制御要求キューに空きがなく要求が受理できなかった
	 */
	public void resume() {
		requestControl(Request.RESUME);
	}

	/**
	 * 指定したBMS譜面を再生します。
	 * <p>当メソッドは再生位置に到達した楽曲位置情報の処理のみを行う簡易シーケンサによってBMS譜面を再生します。
	 * 再生時間は指定したBMS譜面の {@link BeMusicChart#getPlayTime()}、ループ処理の待機時間は
	 * {@link #SLEEP_TIME_DEFAULT} を使用し、楽曲位置情報は関数processorに通知します。
	 * 指定したBMS譜面を時間まで再生後、当メソッドが終了します。</p>
	 * @param chart 再生対象のBMS譜面
	 * @param processor 再生位置に到達した楽曲位置情報を処理する関数
	 * @exception NullPointerException chartがnull
	 * @exception NullPointerException processorがnull
	 */
	public static void play(BeMusicChart chart, Consumer<BeMusicPoint> processor) {
		play(chart, chart.getPlayTime(), SLEEP_TIME_DEFAULT, processor);
	}

	/**
	 * 指定したBMS譜面を再生します。
	 * <p>当メソッドは再生位置に到達した楽曲位置情報の処理のみを行う簡易シーケンサによってBMS譜面を再生します。
	 * 再生時間、ループ処理の待機時間は指定された値を使用し、楽曲位置情報は関数processorに通知します。
	 * 指定したBMS譜面を時間まで再生後、当メソッドが終了します。</p>
	 * @param chart 再生対象のBMS譜面
	 * @param duration 再生時間
	 * @param sleepTime ループ処理の待機時間
	 * @param processor 再生位置に到達した楽曲位置情報を処理する関数
	 * @exception NullPointerException chartがnull
	 * @exception IllegalArgumentException durationがマイナス値
	 * @exception IllegalArgumentException sleepTimeが{@link #SLEEP_TIME_MAX}超過
	 * @exception NullPointerException processorがnull
	 */
	public static void play(BeMusicChart chart, double duration, double sleepTime, Consumer<BeMusicPoint> processor) {
		var sequencer = new SimpleSequencer(chart, duration, sleepTime, processor);
		sequencer.play();
		sequencer.loop(true);
	}

	/**
	 * ループ処理による時間経過処理が開始された時に呼び出されます。
	 * <p>当イベントは {@link #loop(boolean)} によるループ処理が開始された直後に呼び出されます。
	 * 実際の時間経過処理は当イベントの処理終了後に開始されます。アプリケーションは必要に応じて、
	 * 自身が実装するシーケンサの初期化処理を当イベントに記述することができます。</p>
	 * <p>当イベントで実行時例外がスローされると時間経過処理は一度も実行されることなく
	 * {@link #onFinishLoop(Throwable)} が呼び出され、その後ループ処理が終了し呼び出し元へ例外がスローされます。</p>
	 */
	protected void onStartLoop() {
		// Do nothing
	}

	/**
	 * ループ処理による時間経過処理が終了した時に呼び出されます。
	 * <p>当イベントは {@link #loop(boolean)} によるループ処理が終了した直後に呼び出されます。
	 * ループ処理の終了契機については{@link #loop(boolean)} のドキュメントを参照してください。</p>
	 * <p>当イベントでは、シーケンサの処理終了時にアプリケーション独自のメモリ解放などの終了処理を記述できます。
	 * また、ループ処理中に未キャッチの例外がスローされたかどうかをイベントの引数で検知することができます。</p>
	 * @param uncaught ループ処理中にスローされた未キャッチの例外、またはnull
	 */
	protected void onFinishLoop(Throwable uncaught) {
		// Do nothing
	}

	/**
	 * BMS譜面の再生が開始された時に呼び出されます。
	 * <p>当イベントは {@link #play()} による再生要求が処理された直後に呼び出されます。
	 * 当イベントが呼び出されるのは、停止していたBMS譜面が再生され始めることを意味します。
	 * また、その時の再生位置は必ずしもBMS譜面の先頭(0)であるとは限りません。
	 * アプリケーションは再生開始時に必要な処理を当イベント内で記述してください。</p>
	 */
	protected void onPlay() {
		// Do nothing
	}

	/**
	 * BMS譜面の再生が停止された時に呼び出されます。
	 * <p>当イベントは {@link #stop()} による停止要求が処理された直後に呼び出されます。
	 * BMS譜面の再生位置は常に先頭(0)にリセットされますので、再生位置を任意の場所に設定したい場合は別途
	 * {@link #setPosition(double)} を呼び出して明示的に再生位置を更新してください。</p>
	 */
	protected void onStop() {
		// Do nothing
	}

	/**
	 * BMS譜面の再生が一時停止された時に呼び出されます。
	 * <p>当イベントは {@link #pause()} による一時停止要求が処理された直後に呼び出されます。
	 * BMS譜面の再生位置は、再生が再開されるか明示的に再生位置を更新するまで変化しません。</p>
	 * <p>シーケンサが一時停止・再開をサポートし、且つサウンドの再生を伴う場合、
	 * アプリケーションは現在のサウンドを全て停止し、次に再生が再開されるまでその状態を保持することが求められます。</p>
	 */
	protected void onPause() {
		// Do nothing
	}

	/**
	 * BMS譜面の再生が再開された時に呼び出されます。
	 * <p>当イベントは {@link #resume()} による再開要求が処理された直後に呼び出されます。
	 * BMS譜面の再生位置のカウントが再開され、時間経過に伴い再生位置が変化するようになります。</p>
	 * <p>シーケンサが一時停止・再開をサポートし、且つサウンドの再生を伴う場合、
	 * アプリケーションは現在の再生位置からのサウンド再生を再開することが求められます。</p>
	 */
	protected void onResume() {
		// Do nothing
	}

	/**
	 * BMS譜面の再生が完了(再生によって再生位置が再生時間に到達)した時に呼び出されます。
	 * <p>当イベントが呼ばれる時、再生位置({@link #getPosition()})は再生時間({@link #getDuration()})と同じになります。
	 * また、{@link #isCompleted()} はtrueを返します。この状態になると再生・一時停止・再開は全て受け付けなくなり、
	 * 停止のみが有効な操作となります。</p>
	 * <p>当イベントは {@link #onPoint(BeMusicPoint)}, {@link #onUpdate()} よりも後で呼び出されます。</p>
	 */
	protected void onComplete() {
		// Do nothing
	}

	/**
	 * 時間経過処理が1回実行される度に1回呼び出されます。
	 * <p>当イベントは {@link #update()} では呼び出し毎に1回、{@link #loop(boolean)} ではループ毎に1回呼ばれます。
	 * 1回の時間経過処理で処理する楽曲位置情報が存在するかどうかに限らず、必ず呼び出されます。
	 * 当イベントの目的は、楽曲位置情報の有無に限らず何らかの定期的な処理が必要な時にその処理を行うことにあります。
	 * 例えば、譜面の描画処理などは楽曲位置情報の到達有無に限らず一定周期で行わなければならないため、
	 * そのような処理を行う契機として当イベントを活用することを想定しています。</p>
	 * <p>当イベントから再生位置を取得すると、時間経過処理後の再生位置が返ります。
	 * アプリケーションはその再生位置を基準としてBMS譜面から各種情報を抽出し、希望する処理を実装できます。</p>
	 * <p>当イベントは最も頻繁に呼び出されるイベントです。イベント内で記述する処理はできるだけ負荷を軽く、
	 * 使用メモリ容量を小さく抑えるべきです。当イベントの負荷が高すぎると後続の時間経過処理に遅れが生じ、
	 * 再生動作の品質が低下する可能性が発生してしまいます。</p>
	 */
	protected void onUpdate() {
		// Do nothing
	}

	/**
	 * 再生位置が楽曲位置情報の時間に到達した時に呼び出されます。
	 * <p>イベントの入力引数には再生位置に到達した楽曲位置情報が格納されています。当イベントで行う主な処理は、
	 * 例えばBGMの再生、BGAの表示、テキストの表示など、再生位置に応じたプレゼンテーションが挙げられます。
	 * これらのように処理タイミングに忠実に従う必要のある処理は当イベントで行うことが推奨されます。</p>
	 * <p>このイベントは {@link #onUpdate()} よりも先に呼び出されます。
	 * 前述の通り、当イベントで推奨される処理にはサウンド再生に関連するものがあり、
	 * 少しでも早く処理を完了させることが求められるためです。</p>
	 * <p>当イベント呼び出しの時間的な精度は {@link #onGetSleepTime()} が返す待機時間の長さに大きく依存します。
	 * アプリケーションごとに必要な精度は待機時間の長さで調整してください。(ループ処理で時間経過処理を行う場合)</p>
	 * @param p 再生位置に到達した楽曲位置情報
	 * @see BeMusicPoint#getTime()
	 */
	protected void onPoint(BeMusicPoint p) {
		// Do nothing
	}

	/**
	 * 再生位置が変更された時に呼び出されます。
	 * <p>当イベントが呼び出された時に {@link #getPosition()} を呼び出すと、変更後の再生位置が返ります。
	 * 変更前の再生位置を参照したい場合は入力引数の再生位置を参照してください。</p>
	 * @param oldPosition 変更前の再生位置(秒)
	 */
	protected void onSeek(double oldPosition) {
		// Do nothing
	}

	/**
	 * ループ処理による待機の直前で待機時間を決定する時に呼び出されます。
	 * <p>当イベントは待機毎に呼び出されます。これはシーケンサの状態により待機時間を調整する場合などに対応するためです。
	 * そのような必要がなければ常に同じ値を返すように実装してください。デフォルトでは固定で {@link #SLEEP_TIME_DEFAULT}
	 * を返すように実装されています。</p>
	 * <p>返す待機時間は {@link #SLEEP_TIME_MAX} を超えないようにしてください。超えた値を返すと例外がスローされます。
	 * また、0以下の値を返すと待機処理自体を行わず、ループ処理が最高速度で動作するようになります。
	 * 再生の精度は最大化されますが、CPU負荷は非常に高くなりますので注意してください。</p>
	 * @return 待機時間(秒)
	 */
	protected double onGetSleepTime() {
		return SLEEP_TIME_DEFAULT;
	}

	/**
	 * 初期化処理
	 * @param chart 再生対象のBMS譜面
	 * @param duration 再生時間
	 * @exception NullPointerException chartがnull
	 * @exception IllegalArgumentException durationがマイナス値
	 */
	private void setup(BeMusicChart chart, double duration) {
		assertArgNotNull(chart, "chart");
		assertArg(duration >= 0.0, "Invalid duration: %s", duration);
		mChart = chart;
		mDuration = duration;
		mPosition = 0.0;
	}

	/**
	 * 時間経過処理の排他開始
	 * @exception IllegalStateException 既に時間経過処理が排他状態になっている
	 */
	private void lockUpdate() {
		if (!mUpdateSemaphore.tryAcquire()) {
			throw new IllegalStateException("Update/Loop is already running.");
		}
	}

	/**
	 * 時間経過処理の排他終了
	 */
	private void unlockUpdate() {
		mUpdateSemaphore.release();
	}

	/**
	 * 再生制御要求の受理
	 * @param request 再生制御種別
	 * @exception IllegalStateException 再生制御要求キューに空きがなく要求が受理できなかった
	 */
	private void requestControl(Request request) {
		requestControl(request, 0.0);
	}

	/**
	 * 再生制御要求の受理
	 * @param request 再生制御種別
	 * @param fParam パラメータ(double)
	 * @exception IllegalStateException 再生制御要求キューに空きがなく要求が受理できなかった
	 */
	private void requestControl(Request request, double fParam) {
		var rc = new RequestControl();
		rc.request = request;
		rc.fParam = fParam;
		if (!mRequests.offer(rc)) {
			throw new IllegalStateException("Couldn't accept a request because queue is full.");
		}
	}

	/**
	 * 時間経過処理
	 */
	private void processUpdate() {
		for (var rc = mRequests.poll(); rc != null; rc = mRequests.poll()) {
			mRequestHandlers.get(rc.request).accept(rc);
		}
		mStatusHandlers.get(mStatus).run();
	}

	/**
	 * 再生要求の実行
	 * @param rc 再生制御データ
	 */
	private void processPlayRequest(RequestControl rc) {
		if (isStopped()) {
			mStatus = Status.PLAYING;
			mBaseTimeNs = System.nanoTime();
			mBasePosition = mPosition;
			onPlay();
		}
	}

	/**
	 * 停止要求の実行
	 * @param rc 再生制御データ
	 */
	private void processStopRequest(RequestControl rc) {
		if (!isStopped()) {
			mStatus = Status.IDLE;
			mBaseTimeNs = 0L;
			mBasePosition = 0.0;
			mPosition = 0.0;
			onStop();
		}
	}

	/**
	 * 一時停止要求の実行
	 * @param rc 再生制御データ
	 */
	private void processPauseRequest(RequestControl rc) {
		if (isPlaying()) {
			mStatus = Status.PAUSING;
			onPause();
		}
	}

	/**
	 * 再開要求の実行
	 * @param rc 再生制御データ
	 */
	private void processResumeRequest(RequestControl rc) {
		if (isPausing()) {
			mStatus = Status.PLAYING;
			mBaseTimeNs = System.nanoTime();
			mBasePosition = mPosition;
			onResume();
		}
	}

	/**
	 * 再生位置設定要求の実行
	 * @param rc 再生制御データ
	 */
	private void processSeekRequest(RequestControl rc) {
		if (!isCompleted()) {
			var oldPosition = mPosition;
			mBaseTimeNs = System.nanoTime();
			mBasePosition = rc.fParam;
			mPosition = rc.fParam;
			onSeek(oldPosition);
		}
	}

	/**
	 * シーケンサの状態が再生中の時の時間経過処理
	 * <p>基本的に、時間経過処理はシーケンサの状態が再生中の時のみ行う。
	 * それ以外の状態では {@link #onUpdate()} を呼び出すのみで再生時間の進行は発生しない。</p>
	 */
	private void processPlaying() {
		// 前回更新からの経過分の時間を進行させる
		var currentTime = System.nanoTime();
		var deltaTimeSec = (double)(currentTime - mBaseTimeNs) * 0.000000001;
		var newPosition = Math.min(mBasePosition + deltaTimeSec, mDuration);

		// 進行した分の楽曲位置情報を抽出しイベント通知する
		// 現在位置が再生時間に到達した場合は再生時間も含めて範囲に含める(そうしないと最後の楽曲位置情報が通知されない)
		var indexFrom = mChart.ceilPointOf(mPosition);
		var indexTo = mChart.floorPointOf((newPosition == mDuration) ? newPosition : Math.nextDown(newPosition));
		mPosition = newPosition;
		if ((indexFrom >= 0) && (indexTo >= 0)) {
			for (var i = indexFrom; i <= indexTo; i++) {
				onPoint(mChart.getPoint(i));
			}
		}

		// 楽曲位置情報の通知の後で更新処理を行う
		// 楽曲位置情報の通知では音声再生を行う可能性があり、更新処理よりも先に行うことで少しでも遅延を緩和する
		onUpdate();

		// 再生時間に到達した場合は再生終了状態へ遷移する
		if (mPosition == mDuration) {
			mStatus = Status.COMPLETED;
			onComplete();
		}
	}

	/**
	 * 時間経過処理ループの待機処理
	 * @return 待機中にスレッド割り込みが発生した場合true
	 */
	private boolean processSleep() {
		var timeSec = onGetSleepTime();
		if (timeSec <= 0.0) {
			// 待機時間に0以下を指定時はウェイトなしとする ※スレッドの割り込みのみをチェックする
			return Thread.interrupted();
		} else if (timeSec > SLEEP_TIME_MAX) {
			// 待機時間に最大値を超過する値を指定時はエラーとして例外をスローする
			throw new IllegalArgumentException(String.format("Invalid sleep time: %s", timeSec));
		} else {
			// 待機時間分のスリープを行う
			// スレッドの割り込みを検出した時点でループが終了するようにする
			var timeMs = (long)timeSec * 1000L;
			var timeNs = (int)((timeSec - (long)timeSec) * 1000000.0);
			try {
				Thread.sleep(timeMs, timeNs);
				return false;
			}  catch (InterruptedException e) {
				return true;
			}
		}
	}
}
