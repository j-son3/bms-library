package com.lmt.lib.bms.bemusic;

import static com.lmt.lib.bms.internal.Assertion.*;

import java.util.Arrays;
import java.util.List;

import com.lmt.lib.bms.BmsAt;

/**
 * BMS譜面全体のうち、同一楽曲位置上における各種情報を表します。
 *
 * <p>BMS譜面は、譜面全体が楽曲位置情報のリストで表されます。同一楽曲位置には複数の情報が含まれることが
 * ほとんどであり、当クラスではそれらの情報を統合的に管理します。アプリケーションは必要に応じ、当クラスを通じて
 * 各種情報にアクセスし、アプリケーションの機能を実現します。</p>
 *
 * <p>各種情報の構成は以下のようになっています。</p>
 *
 * <ul>
 * <li>小節番号、小節の刻み位置からなる楽曲位置</li>
 * <li>楽曲位置に対応した譜面上の時間</li>
 * <li>楽曲位置に対応する情報を表示するべき表示位置の値</li>
 * <li>楽曲位置が含まれる小節の小節長</li>
 * <li>楽曲位置における現在のBPM</li>
 * <li>楽曲位置に到達した時に譜面を停止する時間</li>
 * <li>ノートの情報(可視・不可視・地雷)</li>
 * <li>楽曲位置に到達した時に再生されるべきBGM</li>
 * <li>視覚効果を構成する情報(BGA・レイヤー・ミス時BGA)</li>
 * <li>楽曲位置に到達した時に表示されるべきテキスト</li>
 * <li>その他、ノートの数に関する情報、および各種情報の有無を表すフラグ</li>
 * </ul>
 */
public class BeMusicPoint implements BmsAt {
	/** 小節番号 */
	private int mMeasure = 0;
	/** 小節の刻み位置 */
	private double mTick = 0;
	/** 表示位置 */
	private double mDispPos;
	/** 楽曲位置の時間 */
	private double mTime;
	/** この楽曲位置が含まれる小節の小節長 */
	private float mLength = 1.0f;
	/** この楽曲位置の現在BPM */
	private float mBpm;
	/** 譜面停止時間 */
	private float mStop;
	/** ノート情報 */
	private short[] mNotes;
	/** 不可視オブジェ情報 */
	private short[] mInvisibles;
	/** BGM */
	private short[] mBgms;
	/** BGA */
	private short mBga;
	/** レイヤー */
	private short mLayer;
	/** ミス時BGA */
	private short mMiss;
	/** 各種情報 */
	private int mInfo;  // 0-4b:NoteCount, 5-9b:LnCount, 10-14b:LmCount, 15-19b:VeCount 20b:isLastPlayable 21b:hasMovement
	/** 表示テキスト */
	private String mText = "";

	/**
	 * 譜面が空だった場合の唯一の楽曲位置情報。この楽曲位置情報は小節番号、刻み位置が0でその他の情報が
	 * 全て未設定の状態を表します。
	 */
	public static final BeMusicPoint EMPTY = new BeMusicPoint();

	/**
	 * 楽曲位置情報オブジェクトを構築します。
	 * <p>当クラスはアプリケーションからnew演算子で直接インスタンスを生成することを想定していません。
	 * 楽曲位置情報の構築については{@link BeMusicScoreBuilder}を参照してください。</p>
	 * @see BeMusicScoreBuilder
	 */
	public BeMusicPoint() {
		// Do nothing
	}

	/**
	 * コピーコンストラクタ
	 * @param src コピー元
	 */
	BeMusicPoint(BeMusicPoint src) {
		mMeasure = src.mMeasure;
		mTick = src.mTick;
		mDispPos = src.mDispPos;
		mTime = src.mTime;
		mLength = src.mLength;
		mBpm = src.mBpm;
		mStop = src.mStop;
		mNotes = (src.mNotes == null) ? null : Arrays.copyOf(src.mNotes, src.mNotes.length);
		mInvisibles = (src.mInvisibles == null) ? null : Arrays.copyOf(src.mInvisibles, src.mInvisibles.length);
		mBgms = (src.mBgms == null) ? null : Arrays.copyOf(src.mBgms, src.mBgms.length);
		mBga = src.mBga;
		mLayer = src.mLayer;
		mMiss = src.mMiss;
		mInfo = src.mInfo;
		mText = src.mText;
	}

	/** {@inheritDoc} */
	@Override
	public final int getMeasure() {
		return mMeasure;
	}

	/**
	 * 小節番号設定
	 * @param measure 小節番号
	 */
	final void setMeasure(int measure) {
		mMeasure = measure;
	}

	/** {@inheritDoc} */
	@Override
	public final double getTick() {
		return mTick;
	}

	/**
	 * 小節の刻み位置設定
	 * @param tick 小節の刻み位置
	 */
	final void setTick(double tick) {
		mTick = tick;
	}

	/**
	 * 楽曲位置情報の表示位置を示す値を取得します。
	 * <p>当メソッドが返す表示位置の値は、楽曲位置の時間とBPMを用いた値になっています。従って、BPMの値が大きいほど
	 * 楽曲位置情報同士の間隔を空けて表示することになります。</p>
	 * <p>この値はBPMの変化に比例して譜面のスクロール速度と表示間隔が変化するようなプレゼンテーションを想定しています。
	 * BPM変化の影響を受けないプレゼンテーションを行いたい場合には当メソッドの値を使用せず、{@link #getTime()}の
	 * 値を使用するようにしてください。</p>
	 * <p>TODO 表示位置の計算方法を記載する</p>
	 * @return 楽曲位置情報の表示位置を示す値
	 */
	public final double getDisplayPosition() {
		return mDispPos;
	}

	/**
	 * 表示位置設定
	 * @param dispPos 表示位置
	 */
	final void setDisplayPosition(double dispPos) {
		mDispPos = dispPos;
	}

	/**
	 * 楽曲位置の時間を取得します。
	 * <p>楽曲位置の時間は、小節0・刻み位置0からこの楽曲位置に到達するまでの時間を秒単位で表します。</p>
	 * @return 楽曲位置の時間
	 */
	public final double getTime() {
		return mTime;
	}

	/**
	 * 楽曲位置の時間設定
	 * @param time 楽曲位置の時間
	 */
	final void setTime(double time) {
		mTime = time;
	}

	/**
	 * 指定入力デバイスのノート種別を取得します。
	 * <p>ノート種別については{@link BeMusicNoteType}を参照してください。</p>
	 * @param device 入力デバイス
	 * @return ノート種別
	 * @exception NullPointerException deviceがnull
	 * @see BeMusicDevice
	 * @see BeMusicNoteType
	 */
	public final BeMusicNoteType getNoteType(BeMusicDevice device) {
		return (mNotes == null) ? BeMusicNoteType.NONE : BeMusicNoteType.fromId(mNotes[device.getIndex()] & 0x07);
	}

	/**
	 * 指定入力デバイスの値を取得します。
	 * <p>この値が示す意味はノート種別ごとに異なります。詳細は以下を参照してください。</p>
	 * <table><caption>&nbsp;</caption>
	 * <tr><td><b>ノート種別</b></td><td><b>値の意味</b></td></tr>
	 * <tr><td>{@link BeMusicNoteType#NONE}</td><td>原則として0を返します。このノート種別では値を参照するべきではありません。</td></tr>
	 * <tr><td>{@link BeMusicNoteType#BEAT}</td><td>値をメタ情報のインデックス値と見なします。入力デバイス操作時に{@link BeMusicMeta#WAV}に記述された音声が再生されるべきです。</td></tr>
	 * <tr><td>{@link BeMusicNoteType#LONG_ON}</td><td>同上</td></tr>
	 * <tr><td>{@link BeMusicNoteType#LONG_OFF}</td><td>{@link BeMusicMeta#LNOBJ}に記述された値が格納されています。</td></tr>
	 * <tr><td>{@link BeMusicNoteType#LONG}</td><td>原則として0を返します。このノート種別では値を参照するべきではありません。</td></tr>
	 * <tr><td>{@link BeMusicNoteType#LANDMINE}</td><td>入力デバイス操作時に、値が示す大きさのダメージを受けます。値が"ZZ"場合、プレーを即時終了させるべきです。</td></tr>
	 * </table>
	 * @param device 入力デバイス
	 * @return ノートの値
	 * @exception NullPointerException deviceがnull
	 * @see BeMusicDevice
	 * @see BeMusicNoteType
	 * @see BeMusicMeta#WAV
	 * @see BeMusicMeta#LNOBJ
	 * @see BeMusicChannel#VISIBLE_1P_01
	 * @see BeMusicChannel#LANDMINE_1P_01
	 */
	public final int getNoteValue(BeMusicDevice device) {
		return (mNotes == null) ? 0 : (mNotes[device.getIndex()] >> 3);
	}

	/**
	 * ノートの値設定
	 * @param device 入力デバイス
	 * @param noteType ノート種別
	 * @param noteValue ノートの値
	 */
	final void setNote(BeMusicDevice device, BeMusicNoteType noteType, int noteValue) {
		if (mNotes == null) { mNotes = new short[BeMusicDevice.COUNT]; }
		mNotes[device.getIndex()] = (short)((noteType.getId() & 0x07) | (noteValue << 3));
	}

	/**
	 * この楽曲位置におけるノート数を取得します。
	 * <p>ノート数は、この楽曲位置に到達した時に操作するべき入力デバイスの数を表します。
	 * どのノート種別でノート数をカウントするべきかは{@link BeMusicNoteType}を参照してください。</p>
	 * @return ノート数
	 */
	public final int getNoteCount() {
		return mInfo & 0x1f;
	}

	/**
	 * この楽曲位置においてロングノートが開始される数を取得します。
	 * <p>厳密には{@link BeMusicNoteType#LONG_ON}の数を表します。</p>
	 * @return ロングノート数
	 */
	public final int getLongNoteCount() {
		return (mInfo >> 5) & 0x1f;
	}

	/**
	 * この楽曲位置における地雷オブジェの数を取得します。
	 * @return 地雷オブジェの数
	 */
	public final int getLandmineCount() {
		return (mInfo >> 10) & 0x1f;
	}

	/**
	 * この楽曲位置において視覚効果を持つノートの数を取得します。
	 * @return 視覚効果を持つノートの数
	 */
	public final int getVisualEffectCount() {
		return (mInfo >> 15) & 0x1f;
	}

	/**
	 * 不可視オブジェの値を取得します。
	 * <p>この値が0以外となる場合、値をメタ情報のインデックス値と見なし、入力デバイス操作時に
	 * {@link BeMusicMeta#WAV}に記述された音声が再生されるべきです。但し同一楽曲位置上に可視オブジェが存在する場合には
	 * 可視オブジェ側の音声再生を優先的に行うべきです。</p>
	 * @param device 入力デバイス
	 * @return 不可視オブジェの値
	 * @exception NullPointerException deviceがnull
	 * @see BeMusicDevice
	 */
	public final int getInvisible(BeMusicDevice device) {
		return (mInvisibles == null) ? 0 : mInvisibles[device.getIndex()];
	}

	/**
	 * この楽曲位置の操作可能ノートの有無を取得します。
	 * <p>「操作可能ノート」とは、視覚表示されるノートで{@link BeMusicNoteType#NONE}以外のものを指します。
	 * この楽曲位置のいずれかの入力デバイスに1つでも操作可能ノートがあれば「有り」と見なされます。</p>
	 * @return この楽曲位置に操作可能ノートが1つでもある場合にtrue
	 */
	public final boolean hasPlayableNote() {
		return (mInfo & 0x100000) != 0;
	}

	/**
	 * この楽曲位置の何らかの操作を伴うノートの有無を取得します。
	 * <p>「何らかの操作を伴う」とは、{@link BeMusicNoteType#hasMovement()}がtrueを返すことを表します。
	 * この楽曲位置のいずれかの入力デバイスに1つでも何らかの操作を伴うノートがあれば「有り」と見なされます。</p>
	 * @return この楽曲位置何らかの操作を伴うノートが1つでもある場合にtrue
	 */
	public final boolean hasMovementNote() {
		return (mInfo & 0x200000) != 0;
	}

	/**
	 * この楽曲位置の視覚効果を持つノートの有無を取得します。
	 * <p>「視覚効果を持つ」とは、{@link BeMusicNoteType#hasVisualEffect()}がtrueを返すことを表します。
	 * この楽曲位置のいずれかの入力デバイスに1つでも視覚効果を持つノートがあれば「有り」と見なされます。</p>
	 * @return この楽曲位置に視覚効果を持つノートが1つでもある場合にtrue
	 */
	public final boolean hasVisualEffect() {
		return getVisualEffectCount() > 0;
	}

	/**
	 * 不可視オブジェの値設定
	 * @param device 入力デバイス
	 * @param value 不可視オブジェの値
	 */
	final void setInvisible(BeMusicDevice device, int value) {
		if (mInvisibles == null) { mInvisibles = new short[BeMusicDevice.COUNT]; }
		mInvisibles[device.getIndex()] = (short)value;
	}

	/**
	 * この楽曲位置が存在する小節の小節長を取得します。
	 * <p>小節に対して小節長の指定がない場合、この値は1を示し4/4拍子となります。
	 * 小節長の値は4/4拍子を1とした長さの比率になります。</p>
	 * <p>BMSにて小節長が明示的に指定されたかどうかを参照したい場合は{@link #hasMeasureLength()}を使用してください。</p>
	 * @return 小節長
	 */
	public final double getMeasureLength() {
		return Math.abs(mLength);
	}

	/**
	 * 小節長設定
	 * <p>小節長の明示指定がある場合はマイナス値を指定すること。</p>
	 * @param length 小節長
	 */
	final void setMeasureLength(double length) {
		mLength = (float)length;
	}

	/**
	 * この楽曲位置での現在のBPMを取得します。
	 * <p>BPM変化のない譜面では、この値は常に初期BPMと同じ値になります。楽曲位置にてBPM変更が行われた場合、
	 * 当メソッドは変更後のBPMを返します。</p>
	 * @return 現在のBPM
	 * @see BeMusicMeta#INITIAL_BPM
	 * @see BeMusicMeta#BPM
	 * @see BeMusicChannel#BPM
	 * @see BeMusicChannel#BPM_LEGACY
	 */
	public final double getCurrentBpm() {
		return Math.abs(mBpm);
	}

	/**
	 * 現在のBPM設定
	 * <p>BPM変更が行われた結果のBPMの場合、マイナス値を指定すること。</p>
	 * @param bpm 現在のBPM
	 */
	final void setCurrentBpm(double bpm) {
		mBpm = (float)bpm;
	}

	/**
	 * この楽曲位置到達時に譜面のスクロールを一時停止する時間を取得します。
	 * <p>譜面停止はBe Musicの仕様上、同一楽曲位置でBPM変更が行われた場合には変更後のBPMに基づいて
	 * 停止時間の計算が行われます。譜面停止は{@link BeMusicMeta#STOP}にて刻み数で指定されますが、
	 * 当メソッドでは刻み数とBPMから停止時間を計算し、その計算結果を返します。</p>
	 * @return 譜面停止時間
	 * @see BeMusicMeta#STOP
	 * @see BeMusicChannel#STOP
	 */
	public final double getStop() {
		return mStop;
	}

	/**
	 * 譜面停止時間設定
	 * @param stop 譜面停止時間
	 */
	final void setStop(double stop) {
		mStop = (float)stop;
	}

	/**
	 * この楽曲位置でのBGM数を取得します。
	 * @return BGM数
	 */
	public final int getBgmCount() {
		return (mBgms == null) ? 0 : mBgms.length;
	}

	/**
	 * この楽曲位置でのBGMの値を取得します。
	 * <p>BGMは楽曲位置が示す時間に到達した時に再生されるべき音声の値が格納されています。この値をメタ情報の
	 * インデックス値と見なし、{@link BeMusicMeta#WAV}の該当する音声参照し再生されるべきです。</p>
	 * @param index インデックス(0～{@link #getBgmCount()}-1)
	 * @return BGMの値
	 * @exception IllegalStateException この楽曲位置にBGMが存在しない
	 * @exception IndexOutOfBoundsException indexがマイナス値または指定可能範囲超過
	 * @see BeMusicMeta#WAV
	 * @see BeMusicChannel#BGM
	 */
	public final int getBgmValue(int index) {
		assertField(mBgms != null, "BGM is nothing.");
		assertArgIndexRange(index, mBgms.length, "index");
		return mBgms[index];
	}

	/**
	 * BGM設定
	 * @param bgms BGMのリスト
	 */
	final void setBgm(List<Short> bgms) {
		mBgms = null;
		if ((bgms != null) && (bgms.size() > 0)) {
			mBgms = new short[bgms.size()];
			for (var i = 0; i < mBgms.length; i++) { mBgms[i] = bgms.get(i); }
		}
	}

	/**
	 * この楽曲位置で表示するBGAの値を取得します。
	 * <p>この値が0以外の場合、値をメタ情報のインデックス値と見なし、{@link BeMusicMeta#BMP}の該当する
	 * 画像が表示されるべきです。</p>
	 * @return BGAの値
	 * @see BeMusicMeta#BMP
	 * @see BeMusicChannel#BGA
	 */
	public final int getBgaValue() {
		return mBga;
	}

	/**
	 * BGAの値設定
	 * @param bgaValue BGAの値
	 */
	final void setBgaValue(int bgaValue) {
		mBga = (short)bgaValue;
	}

	/**
	 * この楽曲位置で表示するBGAレイヤーの値を取得します。
	 * <p>この値が0以外の場合、値をメタ情報のインデックス値と見なし、{@link BeMusicMeta#BMP}の該当する
	 * 画像が{@link BeMusicChannel#BGA}の上に重ねて表示されるべきです。</p>
	 * @return BGAレイヤーの値
	 * @see BeMusicMeta#BMP
	 * @see BeMusicChannel#BGA_LAYER
	 */
	public final int getLayerValue() {
		return mLayer;
	}

	/**
	 * BGAレイヤーの値設定
	 * @param layerValue BGAレイヤーの値
	 */
	final void setLayerValue(int layerValue) {
		mLayer = (short)layerValue;
	}

	/**
	 * 楽曲のプレーミス時にこの楽曲位置で表示する画像の値を取得します。
	 * <p>この値が0以外の場合、値をメタ情報のインデックス値と見なし、{@link BeMusicMeta#BMP}の該当する
	 * 画像が表示されるべきです。(当該楽曲位置でプレーミスが発生している場合)</p>
	 * @return プレーミス時に表示する画像の値
	 * @see BeMusicMeta#BMP
	 * @see BeMusicChannel#BGA_MISS
	 */
	public final int getMissValue() {
		return mMiss;
	}

	/**
	 * ミス時画像の値設定
	 * @param missValue ミス時画像の値
	 */
	final void setMissValue(int missValue) {
		mMiss = (short)missValue;
	}

	/**
	 * この楽曲位置で表示するテキストを取得します。
	 * <p>当該楽曲位置で表示するべきテキストが存在しない場合は長さ0の空文字列を返します。
	 * 当メソッドではnullを返しませんのでその点には注意してください。</p>
	 * @return 表示するテキスト
	 * @see BeMusicMeta#TEXT
	 * @see BeMusicChannel#TEXT
	 */
	public final String getText() {
		return mText;
	}

	/**
	 * 表示テキスト設定
	 * @param text 表示テキスト
	 */
	final void setText(String text) {
		mText = text;
	}

	/**
	 * この楽曲位置が小節線を含んでいるかを判定します。
	 * <p>当メソッドがtrueを返す時は、小節の刻み位置が0であることを示します。</p>
	 * @return 楽曲位置に小節線を含んでいる場合true
	 * @see com.lmt.lib.bms.BmsSpec#CHANNEL_MEASURE
	 */
	public final boolean hasMeasureLine() {
		return (mTick == 0);
	}

	/**
	 * この楽曲位置の小節で明示的な小節長の指定が存在するかを判定します。
	 * <p>明示的な指定がない場合の小節長は1(4/4拍子)ですが、明示的に小節長1を指定した場合でも当メソッドはtrueを返します。</p>
	 * @return 明示的な小節長の指定がある場合true
	 * @see BeMusicChannel#LENGTH
	 */
	public final boolean hasMeasureLength() {
		return (mLength < 0.0f);
	}

	/**
	 * この楽曲位置でBPMを変更する指定が存在するかを判定します。
	 * @return BPM変更の指定がある場合true
	 * @see BeMusicMeta#BPM
	 * @see BeMusicChannel#BPM
	 * @see BeMusicChannel#BPM_LEGACY
	 */
	public final boolean hasBpm() {
		return (mBpm < 0.0f);
	}

	/**
	 * この楽曲位置で譜面停止の視覚効果を行う指定が存在するかを判定します。
	 * @return 譜面停止の指定がある場合true
	 * @see BeMusicMeta#STOP
	 * @see BeMusicChannel#STOP
	 */
	public final boolean hasStop() {
		return (mStop != 0.0f);
	}

	/**
	 * この楽曲位置で1つでもBGM再生の指定が存在するかを判定します。
	 * @return BGM再生の指定が存在する場合true
	 * @see BeMusicMeta#WAV
	 * @see BeMusicChannel#BGM
	 */
	public final boolean hasBgm() {
		return (mBgms != null);
	}

	/**
	 * この楽曲位置でBGAの表示指定が存在するかを判定します。
	 * @return BGAの表示指定が存在する場合true
	 * @see BeMusicMeta#BMP
	 * @see BeMusicChannel#BGA
	 */
	public final boolean hasBga() {
		return (mBga != 0);
	}

	/**
	 * この楽曲位置でBGAレイヤーの表示指定が存在するかを判定します。
	 * @return BGAレイヤーの表示指定が存在する場合true
	 * @see BeMusicMeta#BMP
	 * @see BeMusicChannel#BGA_LAYER
	 */
	public final boolean hasLayer() {
		return (mLayer != 0);
	}

	/**
	 * この楽曲位置でミス時に表示するBGAの指定が存在するかを判定します。
	 * @return ミス時に表示するBGAの指定が存在する場合true
	 * @see BeMusicMeta#BMP
	 * @see BeMusicChannel#BGA_MISS
	 */
	public final boolean hasMiss() {
		return (mMiss != 0);
	}

	/**
	 * この楽曲位置のサマリーを実行する
	 */
	final void computeSummary() {
		// ノート数をカウントする
		var noteCount = 0;
		var lnCount = 0;
		var lmCount = 0;
		var veCount = 0;
		var playable = 0;
		var movement = 0;
		for (var i = 0; i < BeMusicDevice.COUNT; i++) {
			var dev = BeMusicDevice.fromIndex(i);
			var ntype = getNoteType(dev);
			noteCount += (ntype.isCountNotes() ? 1 : 0);
			lnCount += (((ntype == BeMusicNoteType.LONG_ON) || (ntype.isCountNotes() && ntype.isLongNoteTail())) ? 1 : 0);
			lmCount += ((ntype == BeMusicNoteType.LANDMINE) ? 1 : 0);
			veCount += (ntype.hasVisualEffect() ? 1 : 0);
			playable |= ((ntype.isPlayable()) ? 1 : 0);
			movement |= ((ntype.hasMovement()) ? 1 : 0);
		}

		// 汎用データ領域に値を設定する
		mInfo = (noteCount | (lnCount << 5) | (lmCount << 10) | (veCount << 15) | (playable << 20) | (movement << 21));
	}
}
