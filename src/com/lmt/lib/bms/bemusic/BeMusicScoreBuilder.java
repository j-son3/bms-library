package com.lmt.lib.bms.bemusic;

import static com.lmt.lib.bms.internal.Assertion.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.lmt.lib.bms.BmsContent;
import com.lmt.lib.bms.BmsInt;
import com.lmt.lib.bms.BmsNote;
import com.lmt.lib.bms.BmsPoint;
import com.lmt.lib.bms.BmsSpec;

/**
 * BMSコンテンツからBe Music仕様のBMS譜面オブジェクトを構築するためのビルダーです。
 *
 * <p>当ビルダーでは、BMS譜面オブジェクトにどのような情報を含めるかを選択し、楽曲の先頭から末尾までをスキャンします。
 * 譜面を画面に表示し、音声・アニメーションの再生までをカバーするようなBMS譜面オブジェクトを構築する場合には
 * 当ビルダーがサポートする全ての情報を含めなければなりません。視聴覚の演出を必要としないBMS譜面オブジェクトで
 * 良いのであれば音声・アニメーションの情報を除外した状態でBMS譜面オブジェクトを構築しても構いません。</p>
 *
 * <p>但し、どのような用途のBMS譜面オブジェクトであったとしても以下の情報だけは必ず含まれることになります。
 * これらは総じて「時間」「表示位置」に関連する情報となっています。</p>
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
 * private BeMusicScore buildMyScore(BmsContent targetContent) {
 *     // 以下はBMS譜面の分析処理に必要な最低限の情報を含めたい場合の設定です。
 *     BeMusicScoreBuilder builder = new BeMusicScoreBuilder(targetContent)
 *         .setSeekMeasureLine(true)  // 小節線を含む
 *         .setSeekVisible(true)      // 可視オブジェを含む
 *         .setSeekInvisible(false)   // 不可視オブジェは無視する
 *         .setSeekLandmine(true)     // 地雷オブジェを含む
 *         .setSeekBgm(false)         // BGMは無視する
 *         .setSeekBga(false)         // BGA(レイヤー、ミス画像含む)は無視する
 *         .setSeekText(false);       // テキストは無視する
 *
 *     // 上記ビルダーで設定した条件でBMS譜面オブジェクトを構築します。
 *     BeMusicScore myScore = builder.createScore();
 *
 *     // 構築したBMS譜面オブジェクトを返します。
 *     return myScore;
 * }
 * </pre>
 */
public class BeMusicScoreBuilder {
	/** 小節線をシークするかどうか */
	private boolean mSeekMeasureLine = true;
	/** 可視オブジェをシークするかどうか */
	private boolean mSeekVisible = true;
	/** 不可視オブジェをシークするかどうか */
	private boolean mSeekInvisible = false;
	/** 地雷オブジェをシークするかどうか */
	private boolean mSeekLandmine = true;
	/** BGMをシークするかどうか */
	private boolean mSeekBgm = false;
	/** BGAをシークするかどうか */
	private boolean mSeekBga = false;
	/** テキストをシークするかどうか */
	private boolean mSeekText = false;

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
	 * @param content BMSコンテンツ
	 * @exception NullPointerException contentがnull
	 */
	public BeMusicScoreBuilder(BmsContent content) {
		assertArgNotNull(content, "content");
		mContent = content;
	}

	/**
	 * 小節線を楽曲位置情報に含めるかどうかを設定します。
	 * <p>小節線は、ある小節の刻み位置0であることを示します。通常、どの楽曲位置でも小節データ、ノートが全く存在しない
	 * 場合には楽曲位置情報は生成対象になりませんが、小節線を含めることで小節毎に必ず楽曲位置情報が含まれることを
	 * 保証することができます。アプリケーションの実装都合に合わせて小節線有無を決定してください。</p>
	 * @param seek 小節線有無
	 * @return このBMS譜面ビルダーオブジェクトのインスタンス
	 * @exception IllegalStateException BMS譜面生成中
	 */
	public final BeMusicScoreBuilder setSeekMeasureLine(boolean seek) {
		assertNotSeeking();
		mSeekMeasureLine = seek;
		return this;
	}

	/**
	 * 可視オブジェを楽曲位置情報に含めるかどうかを設定します。
	 * @param seek 可視オブジェ有無
	 * @return このBMS譜面ビルダーオブジェクトのインスタンス
	 * @exception IllegalStateException BMS譜面生成中
	 */
	public final BeMusicScoreBuilder setSeekVisible(boolean seek) {
		assertNotSeeking();
		mSeekVisible = seek;
		return this;
	}

	/**
	 * 不可視オブジェを楽曲位置情報に含めるかどうかを設定します。
	 * @param seek 不可視オブジェ有無
	 * @return このBMS譜面ビルダーオブジェクトのインスタンス
	 * @exception IllegalStateException BMS譜面生成中
	 */
	public final BeMusicScoreBuilder setSeekInvisible(boolean seek) {
		assertNotSeeking();
		mSeekInvisible = seek;
		return this;
	}

	/**
	 * 地雷オブジェを楽曲位置情報に含めるかどうかを設定します。
	 * @param seek 地雷オブジェ有無
	 * @return このBMS譜面ビルダーオブジェクトのインスタンス
	 * @exception IllegalStateException BMS譜面生成中
	 */
	public final BeMusicScoreBuilder setSeekLandmine(boolean seek) {
		assertNotSeeking();
		mSeekLandmine = seek;
		return this;
	}

	/**
	 * BGMを楽曲位置情報に含めるかどうかを設定します。
	 * @param seek BGM有無
	 * @return このBMS譜面ビルダーオブジェクトのインスタンス
	 * @exception IllegalStateException BMS譜面生成中
	 */
	public final BeMusicScoreBuilder setSeekBgm(boolean seek) {
		assertNotSeeking();
		mSeekBgm = seek;
		return this;
	}

	/**
	 * BGAを楽曲位置情報に含めるかどうかを設定します。
	 * @param seek BGA有無
	 * @return このBMS譜面ビルダーオブジェクトのインスタンス
	 * @exception IllegalStateException BMS譜面生成中
	 */
	public final BeMusicScoreBuilder setSeekBga(boolean seek) {
		assertNotSeeking();
		mSeekBga = seek;
		return this;
	}

	/**
	 * テキストを楽曲位置情報に含めるかどうかを設定します。
	 * @param seek テキスト有無
	 * @return このBMS譜面ビルダーオブジェクトのインスタンス
	 * @exception IllegalStateException BMS譜面生成中
	 */
	public final BeMusicScoreBuilder setSeekText(boolean seek) {
		assertNotSeeking();
		mSeekText = seek;
		return this;
	}

	/**
	 * 処理対象のBMSコンテンツを取得します。
	 * @return 処理対象のBMSコンテンツ
	 */
	public final BmsContent getContent() {
		return mContent;
	}

	/**
	 * このBMS譜面ビルダーオブジェクトに指定された条件でBMS譜面構築を開始し、最初に楽曲位置情報を返します。
	 * <p>BMS譜面構築を開始すると、楽曲位置情報の生成が完了するまでビルダーの条件を変更できなくなります。</p>
	 * <p>当メソッドはビルダーの状態に関わらずいつでも実行できます。</p>
	 * <p>指定の条件に該当する楽曲位置が全く存在しない場合、当メソッドはnullを返します。</p>
	 * <p>当メソッドと{@link #next()}メソッドは低レベルAPIです。これらのメソッドを用いてBMS譜面を構築することは
	 * 推奨されません。代わりに{@link #createList()}または{@link #createScore()}を使用してください。</p>
	 * @return 楽曲位置情報
	 * @exception IllegalStateException 処理対象BMSコンテンツが参照モードではない
	 */
	public final BeMusicPoint first() {
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
	 * 推奨されません。代わりに{@link #createList()}または{@link #createScore()}を使用してください。</p>
	 * @return 楽曲位置情報
	 * @exception IllegalStateException BMS譜面生成中ではない({@link #first()}が実行されていない)
	 * @exception IllegalStateException 処理対象BMSコンテンツが参照モードではない
	 */
	public final BeMusicPoint next() {
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
	public final List<BeMusicPoint> createList() {
		var result = new ArrayList<BeMusicPoint>();
		for (var data = first(); data != null; data = next()) { result.add(data); }
		return result;
	}

	/**
	 * このBMS譜面ビルダーオブジェクトに指定された条件でBMS譜面オブジェクトを構築します。
	 * <p>当メソッドは{@link #createList()}で生成した楽曲位置情報リストを{@link BeMusicScore#create(List)}に渡し、
	 * {@link BeMusicScore}オブジェクトを構築する手順を簡略化するヘルパーメソッドです。</p>
	 * @return BMS譜面オブジェクト
	 */
	public final BeMusicScore createScore() {
		return BeMusicScore.create(createList());
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
				mTargetChannels.add(BmsInt.box(l.getLegacyLongChannel().getNumber()));
			}
			// 不可視オブジェ
			if (mSeekInvisible) {
				mTargetChannels.add(BmsInt.box(l.getInvisibleChannel().getNumber()));
			}
			// 地雷オブジェ
			if (mSeekLandmine) {
				mTargetChannels.add(BmsInt.box(l.getLandmineChannel().getNumber()));
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
	 * @exception IllegalStateException 対象BMSコンテンツが参照モードではない
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

		// 楽曲位置情報を生成する
		var pt = new BeMusicPoint();
		var curMeasure = mCurAt.getMeasure();
		var curTick = mCurAt.getTick();
		var note = (BmsNote)null;
		var note2 = (BmsNote)null;

		// 楽曲位置
		pt.setMeasure(curMeasure);
		pt.setTick(curTick);

		// 時間
		var curTime = mContent.pointToTime(mCurAt);
		var prevTime = (mPrevPoint == null) ? 0.0 : mPrevPoint.getTime();
		if ((curTime <= prevTime) && (mPrevPoint != null)) {
			curTime = Math.nextUp(prevTime);
		}
		pt.setTime(curTime);

		// 表示位置
		var timeDiff = curTime - prevTime;
		var dispPosBase = (mPrevPoint == null) ? 0.0 : mPrevPoint.getDisplayPosition();
		var dispPosAdd = timeDiff * (mCurBpm / BmsSpec.BPM_MAX);  // TODO スクロール速度を適用する
		var dispPos = dispPosBase + dispPosAdd;
		if ((dispPos == dispPosBase) && (mPrevPoint != null)) {
			dispPos = Math.nextUp(dispPos);
		}
		pt.setDisplayPosition(dispPos);

		// 小節長
		if (mContent.containsMeasureValue(BeMusicChannel.LENGTH.getNumber(), curMeasure)) {
			// 小節長変更データが存在する
			pt.setMeasureLength((double)mContent.getMeasureValue(BeMusicChannel.LENGTH.getNumber(), curMeasure) * -1.0);
		} else {
			// 小節長変更データが存在しないので等倍とする
			pt.setMeasureLength(1.0);
		}

		// スクロール速度変更
		if ((note = mContent.getNote(BeMusicChannel.SCROLL.getNumber(), mCurAt)) != null) {
			// スクロール速度変更あり
			mCurScroll = (Double)mContent.getResolvedNoteValue(BeMusicChannel.SCROLL.getNumber(), mCurAt);
			pt.setChangeScroll(true);
			pt.setCurrentScroll(mCurScroll);
		} else {
			// スクロール速度変更が存在しない場合は現在のスクロール速度を設定する
			pt.setChangeScroll(false);
			pt.setCurrentScroll(mCurScroll);
		}

		// BPM変更
		if ((note = mContent.getNote(BeMusicChannel.BPM.getNumber(), mCurAt)) != null) {
			// BPM変更データあり
			mCurBpm = (Double)mContent.getResolvedNoteValue(BeMusicChannel.BPM.getNumber(), mCurAt);
			pt.setCurrentBpm(-mCurBpm);
		} else if ((note = mContent.getNote(BeMusicChannel.BPM_LEGACY.getNumber(), mCurAt)) != null) {
			// BPM変更データ(旧形式)あり
			mCurBpm = (double)note.getValue();
			pt.setCurrentBpm(-mCurBpm);
		} else {
			// BPM変更データが存在しない場合は現在のBPMを設定する
			pt.setCurrentBpm(mCurBpm);
		}

		// 譜面停止
		if ((note = mContent.getNote(BeMusicChannel.STOP.getNumber(), mCurAt)) != null) {
			// 譜面停止データあり
			var stopValue = (double)mContent.getResolvedNoteValue(BeMusicChannel.STOP.getNumber(), mCurAt);
			var stopTime = 1.25 * stopValue / mCurBpm;  // TODO 刻み数から時間への変換を共通化
			pt.setStop(stopTime);
		} else {
			// 譜面停止データなし
			pt.setStop(0.0);
		}

		// 全ラインの可視・不可視・地雷オブジェ
		for (var i = 0; i < BeMusicDevice.COUNT; i++) {
			var dev = BeMusicDevice.fromIndex(i);
			var chNum = 0;

			// 可視オブジェ
			if (mSeekVisible) {
				chNum = dev.getLegacyLongChannel().getNumber();
				if ((note = mContent.getNote(chNum, mCurAt)) != null) {
					// MGQ形式のロングノートチャンネルを解析する
					var lnValue = note.getValue();
					var curHolding = mHoldingsMgq[i];
					if (curHolding == 0) {
						// 長押しが開始されていなかった場合は長押しを開始する
						var lnRdm = mHoldingsRdm[i];
						pt.setNote(dev, lnRdm ? BeMusicNoteType.LONG : BeMusicNoteType.LONG_ON, lnValue);
						mHoldingsMgq[i] = lnValue;
						mHoldingsRdm[i] = false;  // RDM形式のLNは強制OFF
					} else {
						// 長押し継続中の場合は長押しを終了とする
						// 開始と終了の値が異なる場合でも強制的に終了とする。
						// (世に出回っているBMSには、稀に開始と終了の値が異なるものがある)
						pt.setNote(dev, mLnTail, lnValue);
						mHoldingsMgq[i] = 0;
					}
				} else if (mHoldingsMgq[i] != 0) {
					// 当該入力デバイスが長押しノート継続中の場合は「長押しノート継続中」を設定する
					// MGQ形式のLNがONの場合は通常オブジェとRDM形式のLNは解析しない(MGQ形式LNを上に被せるイメージ)
					pt.setNote(dev, BeMusicNoteType.LONG, 0);
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
								pt.setNote(dev, mLnTail, note.getValue());
								mHoldingsRdm[i] = false;
							} else {
								// 前のノートなし、または続けてロングノート終了オブジェ検出時は通常オブジェとして扱う
								pt.setNote(dev, BeMusicNoteType.BEAT, note.getValue());
							}
						} else {
							// 通常オブジェ
							note2 = mContent.getNextNote(chNum, mCurAt, false);
							if ((note2 != null) && mLnObjs.contains((long)note2.getValue())) {
								// 次のノートがロングノート終了オブジェの場合はロングノート開始として扱う
								pt.setNote(dev, BeMusicNoteType.LONG_ON, note.getValue());
								mHoldingsRdm[i] = true;
							} else {
								// 次のノートがロングノート終了オブジェではない場合は通常オブジェとして扱う
								pt.setNote(dev, BeMusicNoteType.BEAT, note.getValue());
							}
						}
					} else if (mHoldingsRdm[i]) {
						// 当該入力デバイスが長押しノート継続中の場合は「長押しノート継続中」を設定する
						pt.setNote(dev, BeMusicNoteType.LONG, 0);
					}
				}
			}

			// 不可視オブジェ
			if (mSeekInvisible) {
				chNum = dev.getInvisibleChannel().getNumber();
				if ((note = mContent.getNote(chNum, mCurAt)) != null) {
					// 不可視オブジェを検出した場合は無条件に設定する
					pt.setInvisible(dev, note.getValue());
				}
			}

			// 地雷オブジェ
			if (mSeekLandmine) {
				chNum = dev.getLandmineChannel().getNumber();
				if ((pt.getNoteType(dev) == BeMusicNoteType.NONE) && ((note = mContent.getNote(chNum, mCurAt)) != null)) {
					// 当該ラインに可視オブジェがない場合に限り、地雷オブジェを設定する
					pt.setNote(dev, BeMusicNoteType.LANDMINE, note.getValue());
				}
			}
		}

		// BGM
		if (mSeekBgm) {
			var bgmNotes = mContent.listNotes(mCurAt, n -> n.getChannel() == BeMusicChannel.BGM.getNumber());
			var bgmList = new ArrayList<Short>(bgmNotes.size());
			bgmNotes.forEach(n -> { bgmList.add((short)n.getValue()); });
			pt.setBgm(bgmList);
		}

		// BGA関連
		if (mSeekBga) {
			note = mContent.getNote(BeMusicChannel.BGA.getNumber(), mCurAt);
			pt.setBgaValue((note == null) ? 0 : note.getValue());
			note = mContent.getNote(BeMusicChannel.BGA_LAYER.getNumber(), mCurAt);
			pt.setLayerValue((note == null) ? 0 : note.getValue());
			note = mContent.getNote(BeMusicChannel.BGA_MISS.getNumber(), mCurAt);
			pt.setMissValue((note == null) ? 0 : note.getValue());
		}

		// テキスト
		if (mSeekText) {
			pt.setText((String)mContent.getResolvedNoteValue(BeMusicChannel.TEXT.getNumber(), mCurAt));
		}

		// サマリー実行
		pt.computeSummary();

		return pt;
	}

	/**
	 * シーク中であることを確認するアサーション
	 * @exception IllegalStateException シーク中ではない
	 */
	private void assertSeeking() {
		assertField(mSeeking, "This operation can't be performed while NOT seeking.");
	}

	/**
	 * シーク中でないことを確認するアサーション
	 * @exception IllegalStateException シーク中
	 */
	private void assertNotSeeking() {
		assertField(!mSeeking, "This operation can't be performed while seeking.");
	}

	/**
	 * 対象コンテンツが参照モードであることを確認するアサーション
	 * @exception IllegalStateException 対象コンテンツが参照モードではない
	 */
	private void assertIsReferenceMode() {
		assertField(mContent.isReferenceMode(), "Content is NOT reference mode.");
	}
}
