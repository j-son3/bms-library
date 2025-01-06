package com.lmt.lib.bms.bemusic;

import static com.lmt.lib.bms.internal.Assertion.*;

import java.util.List;
import java.util.Spliterator;
import java.util.function.IntConsumer;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

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
 * <li>楽曲位置が含まれる小節の小節長</li>
 * <li>楽曲位置における現在のスクロール速度</li>
 * <li>楽曲位置における現在のBPM</li>
 * <li>楽曲位置に到達した時に譜面を停止する時間</li>
 * <li>ノートの情報(可視・不可視・地雷)</li>
 * <li>楽曲位置に到達した時に再生されるべきBGM</li>
 * <li>視覚効果を構成する情報(BGA・レイヤー・ミス時BGA)</li>
 * <li>楽曲位置に到達した時に表示されるべきテキスト</li>
 * <li>その他、ノートの数に関する情報、および各種情報の有無を表すフラグ</li>
 * </ul>
 *
 * @since 0.0.1
 */
public class BeMusicPoint implements BmsAt {
	/** 小節番号 */
	private int mMeasure;
	/** 小節の刻み位置 */
	private double mTick;
	/** 楽曲位置の時間 */
	private double mTime;
	/** 楽曲位置情報のプロパティ */
	private PointProperty mProperty = PointProperty.DEFAULT;
	/** ノート配列 */
	private int[] mNotes;
	/** 個数情報 */
	private int mCount;
	/** 各種情報 */
	private int mInfo;

	/** 可視オブジェの数 */
	static final int VC = BeMusicDevice.COUNT;
	/** 不可視オブジェの数 */
	static final int IC = BeMusicDevice.COUNT;
	/** BGAノートの数(BGA + Layer + Miss) */
	static final int BC = 3;
	/** 当該種別のノートは存在しない */
	private static final int NA = -1;
	/** ノート配列の先頭 */
	private static final int HD = 0;
	/** 可視オブジェと不可視オブジェの合計数 */
	private static final int VI = VC + IC;
	/** 可視オブジェとBGAノートの合計数 */
	private static final int VB = VC + BC;
	/** 不可視オブジェとBGAノートの合計数 */
	private static final int IB = IC + BC;
	/** 可視オブジェ＋不可視オブジェ＋BGAノートの合計数 */
	private static final int AL = VC + IC + BC;
	/** 種別ごとのノート情報開始位置 */
	private static final int[][] NOTE_POSITIONS = new int[][] {
			{ NA, NA, NA, NA },  // 0x00: なし
			{ HD, NA, NA, NA },  // 0x01: 可視
			{ NA, HD, NA, NA },  // 0x02: 不可視
			{ HD, VC, NA, NA },  // 0x03: 可視、不可視
			{ NA, NA, HD, NA },  // 0x04: BGA
			{ HD, NA, VC, NA },  // 0x05: 可視、BGA
			{ NA, HD, IC, NA },  // 0x06: 不可視、BGA
			{ HD, VC, VI, NA },  // 0x07: 可視、不可視、BGA
			{ NA, NA, NA, HD },  // 0x08: BGM
			{ HD, NA, NA, VC },  // 0x09: 可視、BGM
			{ NA, HD, NA, IC },  // 0x0a: 不可視、BGM
			{ HD, VC, NA, VI },  // 0x0b: 可視、不可視、BGM
			{ NA, NA, HD, BC },  // 0x0c: BGA、BGM
			{ HD, NA, VC, VB },  // 0x0d: 可視、BGA、BGM
			{ NA, HD, IC, IB },  // 0x0e: 不可視、BGA、BGM
			{ HD, VC, VI, AL },  // 0x0f: 可視、不可視、BGA、BGM
	};

	// [個数情報]メモリレイアウト
	// ------+-----+------------+---------------------------------------------
	// Range | Bit | Mask       | Value
	// ------+-----+------------+---------------------------------------------
	// 00-03 |   4 | 0x0000000f | この楽曲位置の総ノート数(主レーン)
	// 04-07 |   4 | 0x000000f0 | この楽曲位置の総ノート数(副レーン)
	// 08-11 |   4 | 0x00000f00 | この楽曲位置のロングノート数(主レーン)
	// 12-15 |   4 | 0x0000f000 | この楽曲位置のロングノート数(副レーン)
	// 16-19 |   4 | 0x000f0000 | この楽曲位置の地雷オブジェ数(主レーン)
	// 20-23 |   4 | 0x00f00000 | この楽曲位置の地雷オブジェ数(副レーン)
	// 24-27 |   4 | 0x0f000000 | この楽曲位置の視覚効果を持つ可視オブジェの数(主レーン)
	// 28-31 |   4 | 0xf0000000 | この楽曲位置の視覚効果を持つ可視オブジェの数(副レーン)
	//
	// [各種情報]メモリレイアウト
	// ------+-----+------------+---------------------------------------------
	// Range | Bit | Mask       | Value
	// ------+-----+------------+---------------------------------------------
	//    00 |   1 | 0x00000001 | 可視オブジェ有無(0:なし, 1:あり)
	//    01 |   1 | 0x00000002 | 不可視オブジェ有無(0:なし, 1:あり)
	//    02 |   1 | 0x00000004 | BGA有無(0:なし, 1:あり)
	//    03 |   1 | 0x00000008 | BGM有無(0:なし, 1:あり)
	//    04 |   1 | 0x00000010 | 速度変化有無(0:なし, 1:あり)
	//    05 |   1 | 0x00000020 | ギミック有無(0:なし, 1:あり)
	//    06 |   1 | 0x00000040 | プレー可能ノート有無(主レーン)(0:なし, 1:あり)
	//    07 |   1 | 0x00000080 | プレー可能ノート有無(副レーン)(0:なし, 1:あり)
	//    08 |   1 | 0x00000100 | 操作を伴うノート有無(主レーン)(0:なし, 1:あり)
	//    09 |   1 | 0x00000200 | 操作を伴うノート有無(副レーン)(0:なし, 1:あり)
	//    10 |   1 | 0x00000400 | 長押し継続ノート有無(主レーン)(0:なし, 1:あり)
	//    11 |   1 | 0x00000800 | 長押し継続ノート有無(副レーン)(0:なし, 1:あり)
	//    12 |   1 | 0x00001000 | 長押し開始ノート有無(主レーン)(0:なし, 1:あり)
	//    13 |   1 | 0x00002000 | 長押し開始ノート有無(副レーン)(0:なし, 1:あり)
	//    14 |   1 | 0x00004000 | 長押し終了ノート有無(主レーン)(0:なし, 1:あり)
	//    15 |   1 | 0x00008000 | 長押し終了ノート有無(副レーン)(0:なし, 1:あり)
	//    16 |   1 | 0x00010000 | 長押し関連ノート有無(主レーン)(0:なし, 1:あり)
	//    17 |   1 | 0x00020000 | 長押し関連ノート有無(副レーン)(0:なし, 1:あり)
	// 18-31 |  14 | 0xfffc0000 | 未使用
	//
	// [ノート配列]レイアウト
	// ※左側が配列の先頭要素
	// -------------------------------+-------------------------------+----+------------+
	//             VISIBLE            |           INVISIBLE           |BGA |    BGM     |
	// -------------------------------+-------------------------------+----+------------+
	// ----------+---------------------------------------
	// Name      | Description
	// ----------+---------------------------------------
	// VISIBLE   | 可視オブジェを入力デバイス順に格納
	// INVISIBLE | 不可視オブジェを入力デバイス順に格納
	// BGA       | BGA/Layer/Missの順に3個格納
	// BGM       | BGM数分だけ格納(数は特に規定しない)

	/**
	 * 譜面が空だった場合の唯一の楽曲位置情報。この楽曲位置情報は小節番号、刻み位置が0でその他の情報が
	 * 全て未設定の状態を表します。
	 */
	public static final BeMusicPoint EMPTY = new BeMusicPoint();

	/** {@link BeMusicPoint#sounds(boolean, boolean, boolean)}向けのSpliterator */
	private class SoundSpliterator implements Spliterator.OfInt {
		/** フェーズ番号 */
		private int mPhase = 0;
		/** 要素インデックス */
		private int mIndex = -1;
		/** ノートの種類ごとの先頭位置 */
		private int mVPos, mIPos, mBPos;
		/** サイズ */
		private int mBSize, mSize;

		/**
		 * コンストラクタ
		 * @param visible 可視オブジェ走査フラグ
		 * @param invisible 不可視オブジェ走査フラグ
		 * @param bgm BGM走査フラグ
		 */
		SoundSpliterator(boolean visible, boolean invisible, boolean bgm) {
			mVPos = visible ? getNotePos(RawNotes.VISIBLE) : NA;
			mIPos = invisible ? getNotePos(RawNotes.INVISIBLE) : NA;
			mBPos = bgm ? getNotePos(RawNotes.BGM) : NA;
			mBSize = bgm ? getBgmCount() : 0;
			mSize = ((mVPos == NA) ? 0 : VC) + ((mIPos == NA) ? 0 : IC) + ((mBPos == NA) ? 0 : mBSize);
		}

		/** {@inheritDoc} */
		@Override
		public long estimateSize() {
			return mSize;
		}

		/** {@inheritDoc} */
		@Override
		public int characteristics() {
			return IMMUTABLE | NONNULL | SIZED;
		}

		/** {@inheritDoc} */
		@Override
		public OfInt trySplit() {
			return null;
		}

		/** {@inheritDoc} */
		@Override
		public boolean tryAdvance(IntConsumer action) {
			switch (mPhase) {
			case 0: return advanceVisible(action);
			case 1: return advanceInvisible(action);
			case 2: return advanceBgm(action);
			default: return false;
			}
		}

		/**
		 * 可視オブジェの走査
		 * @param action アクション
		 * @return 残要素があればtrue
		 */
		private boolean advanceVisible(IntConsumer action) {
			if ((mVPos == NA) || ((++mIndex) >= VC)) {
				setPhase(1);
				return advanceInvisible(action);
			} else {
				action.accept(mNotes[mVPos + mIndex]);
				return true;
			}
		}

		/**
		 * 不可視オブジェの走査
		 * @param action アクション
		 * @return 残要素があればtrue
		 */
		private boolean advanceInvisible(IntConsumer action) {
			if ((mIPos == NA) || ((++mIndex) >= IC)) {
				setPhase(2);
				return advanceBgm(action);
			} else {
				action.accept(mNotes[mIPos + mIndex]);
				return true;
			}
		}

		/**
		 * BGMの走査
		 * @param action アクション
		 * @return 残要素があればtrue
		 */
		private boolean advanceBgm(IntConsumer action) {
			if ((mBPos == NA) || ((++mIndex) >= mBSize)) {
				setPhase(3);
				return false;
			} else {
				action.accept(mNotes[mBPos + mIndex]);
				return true;
			}
		}

		/**
		 * フェーズ設定
		 * @param phase フェーズ番号
		 */
		private void setPhase(int phase) {
			mPhase = phase;
			mIndex = -1;
		}
	}

	/**
	 * 楽曲位置情報オブジェクトを構築します。
	 * <p>当クラスはアプリケーションからnew演算子で直接インスタンスを生成することを想定していません。
	 * 楽曲位置情報の構築については{@link BeMusicChartBuilder}を参照してください。</p>
	 * @see BeMusicChartBuilder
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
		mTime = src.mTime;
		mProperty = src.mProperty;
		mNotes = src.mNotes;
		mCount = src.mCount;
		mInfo = src.mInfo;
	}

	/** {@inheritDoc} */
	@Override
	public final int getMeasure() {
		return mMeasure;
	}

	/** {@inheritDoc} */
	@Override
	public final double getTick() {
		return mTick;
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
	 * 指定入力デバイスに対応する可視オブジェのノート種別を取得します。
	 * <p>ノート種別については{@link BeMusicNoteType}を参照してください。</p>
	 * @param device 入力デバイス
	 * @return ノート種別
	 * @exception NullPointerException deviceがnull
	 * @see BeMusicDevice
	 * @see BeMusicNoteType
	 */
	public final BeMusicNoteType getVisibleNoteType(BeMusicDevice device) {
		return RawNotes.getNoteType(getRawNote(RawNotes.VISIBLE, device.getIndex()));
	}

	/**
	 * 指定入力デバイスの値を取得します。
	 * <p>この値が示す意味はノート種別ごとに異なります。詳細は以下を参照してください。</p>
	 * <table><caption>&nbsp;</caption>
	 * <tr><td><strong>ノート種別</strong></td><td><strong>値の意味</strong></td></tr>
	 * <tr><td>{@link BeMusicNoteType#NONE}</td>
	 * <td>原則として0を返します。このノート種別では値を参照するべきではありません。</td></tr>
	 * <tr><td>{@link BeMusicNoteType#BEAT}</td>
	 * <td>音声データのトラックID、音声の再開フラグ、ノートごとのロングノートモードが格納されています。
	 * 音声データはトラックIDをキーにして{@link BeMusicMeta#WAV}にアクセスすることで参照できます。
	 * 各情報へのアクセス方法については{@link BeMusicSound}を参照してください。</td></tr>
	 * <tr><td>{@link BeMusicNoteType#LONG_ON}</td>
	 * <td>同上</td></tr>
	 * <tr><td>{@link BeMusicNoteType#LONG_OFF}</td>
	 * <td>同上、または{@link BeMusicMeta#LNOBJ}に記述された値が格納されています。</td></tr>
	 * <tr><td>{@link BeMusicNoteType#CHARGE_OFF}</td>
	 * <td>同上</td></tr>
	 * <tr><td>{@link BeMusicNoteType#LONG}</td>
	 * <td>原則として0を返します。このノート種別では値を参照するべきではありません。</td></tr>
	 * <tr><td>{@link BeMusicNoteType#MINE}</td>
	 * <td>入力デバイス操作時に、値が示す大きさのダメージを受けます。</td></tr>
	 * </table>
	 * @param device 入力デバイス
	 * @return ノートの値
	 * @exception NullPointerException deviceがnull
	 * @see BeMusicDevice
	 * @see BeMusicNoteType
	 * @see BeMusicSound
	 * @see BeMusicMeta#WAV
	 * @see BeMusicMeta#LNOBJ
	 * @see BeMusicChannel#VISIBLE_1P_01
	 * @see BeMusicChannel#MINE_1P_01
	 */
	public final int getVisibleValue(BeMusicDevice device) {
		return RawNotes.getValue(getRawNote(RawNotes.VISIBLE, device.getIndex()));
	}

	/**
	 * この楽曲位置におけるノート数を取得します。
	 * <p>ノート数は、この楽曲位置に到達した時に操作するべき入力デバイスの数を表します。
	 * どのノート種別でノート数をカウントするべきかは{@link BeMusicNoteType}を参照してください。</p>
	 * @return ノート数
	 */
	public final int getNoteCount() {
		return (mCount & 0x0000000f) + ((mCount & 0x000000f0) >> 4);
	}

	/**
	 * この楽曲位置において指定したレーンのノート数を取得します。
	 * <p>ノート数は、この楽曲位置に到達した時に操作するべき入力デバイスの数を表します。
	 * どのノート種別でノート数をカウントするべきかは{@link BeMusicNoteType}を参照してください。</p>
	 * @param lane レーン
	 * @return ノート数
	 * @exception NullPointerException laneがnull
	 * @since 0.9.0
	 */
	public final int getNoteCount(BeMusicLane lane) {
		assertArgNotNull(lane, "lane");
		return (mCount >> (lane.getIndex() * 4)) & 0x0000000f;
	}

	/**
	 * この楽曲位置においてロングノートが開始される数を取得します。
	 * <p>厳密には{@link BeMusicNoteType#LONG_ON}の数を表します。</p>
	 * @return ロングノート数
	 */
	public final int getLongNoteCount() {
		return ((mCount & 0x00000f00) >> 8) + ((mCount & 0x0000f000) >> 12);
	}

	/**
	 * この楽曲位置において指定したレーンでロングノートが開始される数を取得します。
	 * <p>厳密には{@link BeMusicNoteType#LONG_ON}の数を表します。</p>
	 * @param lane レーン
	 * @return ロングノート数
	 * @exception NullPointerException laneがnull
	 * @since 0.9.0
	 */
	public final int getLongNoteCount(BeMusicLane lane) {
		assertArgNotNull(lane, "lane");
		return (mCount >> (8 + (lane.getIndex() * 4))) & 0x0000000f;
	}

	/**
	 * この楽曲位置における地雷オブジェの数を取得します。
	 * @return 地雷オブジェの数
	 */
	public final int getMineCount() {
		return ((mCount & 0x000f0000) >> 16) + ((mCount & 0x00f00000) >> 20);
	}

	/**
	 * この楽曲位置において指定レーンの地雷オブジェの数を取得します。
	 * @param lane レーン
	 * @return 地雷オブジェの数
	 * @exception NullPointerException laneがnull
	 * @since 0.9.0
	 */
	public final int getMineCount(BeMusicLane lane) {
		assertArgNotNull(lane, "lane");
		return (mCount >> (16 + (lane.getIndex() * 4))) & 0x0000000f;
	}

	/**
	 * この楽曲位置において視覚効果を持つノートの数を取得します。
	 * @return 視覚効果を持つノートの数
	 * @since 0.5.0
	 */
	public final int getVisualEffectCount() {
		return ((mCount & 0x0f000000) >> 24) + ((mCount & 0xf0000000) >> 28);
	}

	/**
	 * この楽曲位置において指定レーンで視覚効果を持つノートの数を取得します。
	 * @param lane レーン
	 * @return 視覚効果を持つノートの数
	 * @exception NullPointerException laneがnull
	 * @since 0.9.0
	 */
	public final int getVisualEffectCount(BeMusicLane lane) {
		assertArgNotNull(lane, "lane");
		return (mCount >> (24 + (lane.getIndex() * 4))) & 0x0000000f;
	}

	/**
	 * 不可視オブジェの値を取得します。
	 * <p>この値が0以外となる場合、値からトラックIDを取り出してメタ情報のインデックス値と見なし、入力デバイス操作時に
	 * {@link BeMusicMeta#WAV}に記述された音声が再生されるべきです。但し同一楽曲位置上に可視オブジェが存在する場合には
	 * 可視オブジェ側の音声再生を優先的に行うべきです。</p>
	 * @param device 入力デバイス
	 * @return 不可視オブジェの値
	 * @exception NullPointerException deviceがnull
	 * @see BeMusicDevice
	 * @see BeMusicSound#getTrackId(int)
	 */
	public final int getInvisibleValue(BeMusicDevice device) {
		return RawNotes.getValue(getRawNote(RawNotes.INVISIBLE, device.getIndex()));
	}

	/**
	 * この楽曲位置の操作可能ノートの有無を取得します。
	 * <p>「操作可能ノート」とは、視覚表示されるノートで{@link BeMusicNoteType#NONE}以外のものを指します。
	 * この楽曲位置のいずれかの入力デバイスに1つでも操作可能ノートがあれば「あり」と見なされます。</p>
	 * @return この楽曲位置に操作可能ノートが1つでもある場合にtrue
	 */
	public final boolean hasPlayableNote() {
		return (mInfo & 0x000000c0) != 0;
	}

	/**
	 * この楽曲位置において指定レーンでの操作可能ノートの有無を取得します。
	 * <p>「操作可能ノート」とは、視覚表示されるノートで{@link BeMusicNoteType#NONE}以外のものを指します。
	 * この楽曲位置のいずれかの入力デバイスに1つでも操作可能ノートがあれば「あり」と見なされます。</p>
	 * @param lane レーン
	 * @return 操作可能ノートが1つでもある場合true
	 * @exception NullPointerException laneがnull
	 * @since 0.9.0
	 */
	public final boolean hasPlayableNote(BeMusicLane lane) {
		assertArgNotNull(lane, "lane");
		return (mInfo & (0x00000040 << lane.getIndex())) != 0;
	}

	/**
	 * この楽曲位置の何らかの操作を伴うノートの有無を取得します。
	 * <p>「何らかの操作を伴う」とは、{@link BeMusicNoteType#hasMovement()}がtrueを返すことを表します。
	 * この楽曲位置のいずれかの入力デバイスに1つでも何らかの操作を伴うノートがあれば「あり」と見なされます。</p>
	 * @return この楽曲位置何らかの操作を伴うノートが1つでもある場合にtrue
	 * @see BeMusicNoteType#hasMovement()
	 * @since 0.5.0
	 */
	public final boolean hasMovementNote() {
		return (mInfo & 0x00000300) != 0;
	}

	/**
	 * この楽曲位置において指定レーンで何らかの操作を伴うノートの有無を取得します。
	 * <p>「何らかの操作を伴う」とは、{@link BeMusicNoteType#hasMovement()}がtrueを返すことを表します。
	 * この楽曲位置のいずれかの入力デバイスに1つでも何らかの操作を伴うノートがあれば「あり」と見なされます。</p>
	 * @param lane レーン
	 * @return 何らかの操作を伴うノートが1つでもある場合true
	 * @exception NullPointerException laneがnull
	 * @see BeMusicNoteType#hasMovement()
	 * @since 0.9.0
	 */
	public final boolean hasMovementNote(BeMusicLane lane) {
		assertArgNotNull(lane, "lane");
		return (mInfo & (0x00000100 << lane.getIndex())) != 0;
	}

	/**
	 * この楽曲位置での長押し継続ノートの有無を取得します。
	 * <p>具体的には、{@link BeMusicNoteType#isHolding()}がtrueを返すノートが1個以上存在する場合にtrueを返します。</p>
	 * @return この楽曲位置に長押し継続ノートが1つでもある場合にtrue
	 * @see BeMusicNoteType#isHolding()
	 * @since 0.6.0
	 */
	public final boolean hasHolding() {
		return (mInfo & 0x00000c00) != 0;
	}

	/**
	 * この楽曲位置において指定レーンでの長押し継続ノートの有無を取得します。
	 * <p>具体的には、{@link BeMusicNoteType#isHolding()}がtrueを返すノートが1個以上存在する場合にtrueを返します。</p>
	 * @param lane レーン
	 * @return 長押し継続ノートが1つでもある場合true
	 * @exception NullPointerException laneがnull
	 * @see BeMusicNoteType#isHolding()
	 * @since 0.9.0
	 */
	public final boolean hasHolding(BeMusicLane lane) {
		assertArgNotNull(lane, "lane");
		return (mInfo & (0x00000400 << lane.getIndex())) != 0;
	}

	/**
	 * この楽曲位置での長押し開始ノートの有無を取得します。
	 * <p>具体的には、{@link BeMusicNoteType#isLongNoteHead()}がtrueを返すノートが1個以上存在する場合にtrueを返します。</p>
	 * @return この楽曲位置に長押し開始ノートが1つでもある場合にtrue
	 * @see BeMusicNoteType#isLongNoteHead()
	 * @since 0.6.0
	 */
	public final boolean hasLongNoteHead() {
		return (mInfo & 0x00003000) != 0;
	}

	/**
	 * この楽曲位置において指定レーンでの長押し開始ノートの有無を取得します。
	 * <p>具体的には、{@link BeMusicNoteType#isLongNoteHead()}がtrueを返すノートが1個以上存在する場合にtrueを返します。</p>
	 * @param lane レーン
	 * @return 長押し開始ノートが1つでもある場合true
	 * @exception NullPointerException laneがnull
	 * @see BeMusicNoteType#isLongNoteHead()
	 * @since 0.9.0
	 */
	public final boolean hasLongNoteHead(BeMusicLane lane) {
		assertArgNotNull(lane, "lane");
		return (mInfo & (0x00001000 << lane.getIndex())) != 0;
	}

	/**
	 * この楽曲位置において長押し終了ノートの有無を取得します。
	 * <p>具体的には、{@link BeMusicNoteType#isLongNoteTail()}がtrueを返すノートが1個以上存在する場合にtrueを返します。</p>
	 * @return この楽曲位置に長押し終了ノートが1つでもある場合にtrue
	 * @see BeMusicNoteType#isLongNoteTail()
	 * @since 0.6.0
	 */
	public final boolean hasLongNoteTail() {
		return (mInfo & 0x0000c000) != 0;
	}

	/**
	 * この楽曲位置において指定レーンでの長押し終了ノートの有無を取得します。
	 * <p>具体的には、{@link BeMusicNoteType#isLongNoteTail()}がtrueを返すノートが1個以上存在する場合にtrueを返します。</p>
	 * @param lane レーン
	 * @return 長押し終了ノートが1つでもある場合true
	 * @exception NullPointerException laneがnull
	 * @see BeMusicNoteType#isLongNoteTail()
	 * @since 0.9.0
	 */
	public final boolean hasLongNoteTail(BeMusicLane lane) {
		assertArgNotNull(lane, "lane");
		return (mInfo & (0x00004000 << lane.getIndex())) != 0;
	}

	/**
	 * この楽曲位置において長押し関連ノートの有無を取得します。
	 * <p>具体的には、{@link BeMusicNoteType#isLongNoteType()}がtrueを返すノートが1個以上存在する場合にtrueを返します。</p>
	 * @return この楽曲位置に長押し関連ノートが1つでもある場合にtrue
	 * @see BeMusicNoteType#isLongNoteType()
	 * @since 0.6.0
	 */
	public final boolean hasLongNoteType() {
		return (mInfo & 0x00030000) != 0;
	}

	/**
	 * この楽曲位置において指定レーンでの長押関連ノートの有無を取得します。
	 * <p>具体的には、{@link BeMusicNoteType#isLongNoteType()}がtrueを返すノートが1個以上存在する場合にtrueを返します。</p>
	 * @param lane レーン
	 * @return 長押し関連ノートが1つでもある場合true
	 * @exception NullPointerException laneがnull
	 * @see BeMusicNoteType#isLongNoteType()
	 * @since 0.9.0
	 */
	public final boolean hasLongNoteType(BeMusicLane lane) {
		assertArgNotNull(lane, "lane");
		return (mInfo & (0x00010000 << lane.getIndex())) != 0;
	}

	/**
	 * この楽曲位置の視覚効果を持つノートの有無を取得します。
	 * <p>「視覚効果を持つ」とは、{@link BeMusicNoteType#hasVisualEffect()}がtrueを返すことを表します。
	 * この楽曲位置のいずれかの入力デバイスに1つでも視覚効果を持つノートがあれば「あり」と見なされます。</p>
	 * @return この楽曲位置に視覚効果を持つノートが1つでもある場合にtrue
	 * @see BeMusicNoteType#hasVisualEffect()
	 * @since 0.5.0
	 */
	public final boolean hasVisualEffect() {
		return getVisualEffectCount() > 0;
	}

	/**
	 * この楽曲位置において指定レーンで視覚効果を持つノートの有無を取得します。
	 * <p>「視覚効果を持つ」とは、{@link BeMusicNoteType#hasVisualEffect()}がtrueを返すことを表します。
	 * この楽曲位置のいずれかの入力デバイスに1つでも視覚効果を持つノートがあれば「あり」と見なされます。</p>
	 * @param lane レーン
	 * @return 視覚効果を持つノートが1つでもある場合true
	 * @exception NullPointerException laneがnull
	 * @see BeMusicNoteType#hasVisualEffect()
	 * @since 0.9.0
	 */
	public final boolean hasVisualEffect(BeMusicLane lane) {
		return getVisualEffectCount(lane) > 0;
	}

	/**
	 * この楽曲位置での速度変更の有無を取得します。
	 * <p>具体的には、{@link #hasBpm()}, {@link #hasScroll()}のいずれかがtrueを返す場合にtrueを返します。</p>
	 * @return この楽曲位置に速度変更がある場合にtrue
	 * @see #hasBpm()
	 * @see #hasScroll()
	 * @since 0.6.0
	 */
	public final boolean hasChangeSpeed() {
		return (mInfo & 0x00000010) != 0;
	}

	/**
	 * この楽曲位置でのギミック要素の有無を取得します。
	 * <p>「ギミック要素」とは、BPM変更、スクロール速度変更、譜面停止、地雷を指します。
	 * これらの要素がいずれか1つ以上存在する場合にtrueを返します。</p>
	 * @return この楽曲位置にギミック要素がある場合にtrue
	 * @see #hasChangeSpeed()
	 * @see #hasBpm()
	 * @see #hasScroll()
	 * @see #hasStop()
	 * @see #hasMine()
	 * @since 0.6.0
	 */
	public final boolean hasGimmick() {
		return (mInfo & 0x00000020) != 0;
	}

	/**
	 * この楽曲位置が存在する小節の小節長を取得します。
	 * <p>小節に対して小節長の指定がない場合、この値は1を示し4/4拍子となります。
	 * 小節長の値は4/4拍子を1とした長さの比率になります。</p>
	 * <p>BMSにて小節長が明示的に指定されたかどうかを参照したい場合は{@link #hasMeasureLength()}を使用してください。</p>
	 * @return 小節長
	 */
	public final double getMeasureLength() {
		return Math.abs(mProperty.length);
	}

	/**
	 * この楽曲位置での現在のスクロール速度を取得します。
	 * <p>スクロール速度変化のない譜面では、この値は常に1.0になります。楽曲位置にてスクロール速度の変更が行われた場合、
	 * 当メソッドは変更後のスクロール速度を返します。</p>
	 * @return 現在のスクロール速度
	 * @see BeMusicMeta#SCROLL
	 * @see BeMusicChannel#SCROLL
	 * @since 0.6.0
	 */
	public final double getCurrentScroll() {
		return mProperty.scroll;
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
		return Math.abs(mProperty.bpm);
	}

	/**
	 * この楽曲位置での現在の譜面速度を取得します。
	 * <p>返される値は、現在のBPMに現在のスクロール速度を積算した値となります。</p>
	 * @return 現在の譜面速度
	 * @see #getCurrentBpm()
	 * @see #getCurrentScroll()
	 * @since 0.6.0
	 */
	public final double getCurrentSpeed() {
		return getCurrentBpm() * getCurrentScroll();
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
		return mProperty.stop;
	}

	/**
	 * この楽曲位置でのBGM数を取得します。
	 * @return BGM数
	 */
	public final int getBgmCount() {
		var bgmPos = getNotePos(RawNotes.BGM);
		return (bgmPos == NA) ? 0 : (mNotes.length - bgmPos);
	}

	/**
	 * この楽曲位置でのBGMの値を取得します。
	 * <p>BGMは楽曲位置が示す時間に到達した時に再生されるべき音声の値が格納されています。
	 * この値からトラックIDを取り出してメタ情報のインデックス値と見なし、{@link BeMusicMeta#WAV}
	 * の該当する音声参照し再生されるべきです。</p>
	 * @param index インデックス(0～{@link #getBgmCount()}-1)
	 * @return BGMの値
	 * @exception IllegalStateException この楽曲位置にBGMが存在しない
	 * @exception IndexOutOfBoundsException indexがマイナス値または指定可能範囲超過
	 * @see BeMusicMeta#WAV
	 * @see BeMusicChannel#BGM
	 * @see BeMusicSound#getTrackId(int)
	 */
	public final int getBgmValue(int index) {
		var bgmPos = getNotePos(RawNotes.BGM);
		assertField(bgmPos != NA, "BGM is nothing.");
		assertArgIndexRange(index, mNotes.length - bgmPos, "index");
		return RawNotes.getValue(mNotes[bgmPos + index]);
	}

	/**
	 * この楽曲位置で表示するBGAの値(トラックID)を取得します。
	 * <p>この値が0以外の場合、値をメタ情報のインデックス値と見なし、{@link BeMusicMeta#BMP}の該当する
	 * 画像が表示されるべきです。</p>
	 * @return BGAの値
	 * @see BeMusicMeta#BMP
	 * @see BeMusicChannel#BGA
	 */
	public final int getBgaValue() {
		return RawNotes.getTrackId(getRawNote(RawNotes.BGA, 0));
	}

	/**
	 * この楽曲位置で表示するBGAレイヤーの値(トラックID)を取得します。
	 * <p>この値が0以外の場合、値をメタ情報のインデックス値と見なし、{@link BeMusicMeta#BMP}の該当する
	 * 画像が{@link BeMusicChannel#BGA}の上に重ねて表示されるべきです。</p>
	 * @return BGAレイヤーの値
	 * @see BeMusicMeta#BMP
	 * @see BeMusicChannel#BGA_LAYER
	 */
	public final int getLayerValue() {
		return RawNotes.getTrackId(getRawNote(RawNotes.BGA, 1));
	}

	/**
	 * 楽曲のプレーミス時にこの楽曲位置で表示する画像の値(トラックID)を取得します。
	 * <p>この値が0以外の場合、値をメタ情報のインデックス値と見なし、{@link BeMusicMeta#BMP}の該当する
	 * 画像が表示されるべきです。(当該楽曲位置でプレーミスが発生している場合)</p>
	 * @return プレーミス時に表示する画像の値
	 * @see BeMusicMeta#BMP
	 * @see BeMusicChannel#BGA_MISS
	 */
	public final int getMissValue() {
		return RawNotes.getTrackId(getRawNote(RawNotes.BGA, 2));
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
		return mProperty.text;
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
		return (mProperty.length < 0.0);
	}

	/**
	 * この楽曲位置でスクロール速度を変更する指定が存在するかを判定します。
	 * @return スクロール速度変更の指定がある場合true
	 * @see BeMusicMeta#SCROLL
	 * @see BeMusicChannel#SCROLL
	 * @since 0.6.0
	 */
	public final boolean hasScroll() {
		return mProperty.changeScroll;
	}

	/**
	 * この楽曲位置でBPMを変更する指定が存在するかを判定します。
	 * @return BPM変更の指定がある場合true
	 * @see BeMusicMeta#BPM
	 * @see BeMusicChannel#BPM
	 * @see BeMusicChannel#BPM_LEGACY
	 */
	public final boolean hasBpm() {
		return (mProperty.bpm < 0.0);
	}

	/**
	 * この楽曲位置で譜面停止の視覚効果を行う指定が存在するかを判定します。
	 * @return 譜面停止の指定がある場合true
	 * @see BeMusicMeta#STOP
	 * @see BeMusicChannel#STOP
	 */
	public final boolean hasStop() {
		return (mProperty.stop != 0.0);
	}

	/**
	 * この楽曲位置に地雷オブジェが存在するかを判定します。
	 * @return 地雷オブジェが存在する場合true
	 * @see #getMineCount()
	 * @since 0.6.0
	 */
	public final boolean hasMine() {
		return (getMineCount() != 0);
	}

	/**
	 * この楽曲位置で1つでもBGM再生の指定が存在するかを判定します。
	 * @return BGM再生の指定が存在する場合true
	 * @see BeMusicMeta#WAV
	 * @see BeMusicChannel#BGM
	 */
	public final boolean hasBgm() {
		return (mInfo & 0x00000008) != 0;
	}

	/**
	 * この楽曲位置でBGAの表示指定が存在するかを判定します。
	 * @return BGAの表示指定が存在する場合true
	 * @see BeMusicMeta#BMP
	 * @see BeMusicChannel#BGA
	 */
	public final boolean hasBga() {
		return getBgaValue() != 0;
	}

	/**
	 * この楽曲位置でBGAレイヤーの表示指定が存在するかを判定します。
	 * @return BGAレイヤーの表示指定が存在する場合true
	 * @see BeMusicMeta#BMP
	 * @see BeMusicChannel#BGA_LAYER
	 */
	public final boolean hasLayer() {
		return getLayerValue() != 0;
	}

	/**
	 * この楽曲位置でミス時に表示するBGAの指定が存在するかを判定します。
	 * @return ミス時に表示するBGAの指定が存在する場合true
	 * @see BeMusicMeta#BMP
	 * @see BeMusicChannel#BGA_MISS
	 */
	public final boolean hasMiss() {
		return getMissValue() != 0;
	}

	/**
	 * この楽曲位置で表示するテキストが存在するかを判定します。
	 * @return 表示するテキストが存在する場合true
	 * @see BeMusicMeta#TEXT
	 * @see BeMusicChannel#TEXT
	 * @since 0.9.0
	 */
	public final boolean hasText() {
		return !getText().isEmpty();
	}

	/**
	 * 全ての可視オブジェのノートを列挙します。
	 * <p>当メソッドは{@link #enumSounds(boolean, boolean, boolean, IntConsumer)}を以下のパラメータで呼び出します。</p>
	 * <pre>enumSounds(true, false, false, action);</pre>
	 * @param action ノート1個に対して何らかの処理を行う関数
	 * @exception NullPointerException actionがnull
	 * @since 0.8.0
	 */
	public final void enumVisibles(IntConsumer action) {
		enumSounds(true, false, false, action);
	}

	/**
	 * 全ての不可視オブジェのノートを列挙します。
	 * <p>当メソッドは{@link #enumSounds(boolean, boolean, boolean, IntConsumer)}を以下のパラメータで呼び出します。</p>
	 * <pre>enumSounds(false, true, false, action);</pre>
	 * @param action ノート1個に対して何らかの処理を行う関数
	 * @exception NullPointerException actionがnull
	 * @since 0.8.0
	 */
	public final void enumInvisibles(IntConsumer action) {
		enumSounds(false, true, false, action);
	}

	/**
	 * 全てのBGMのノートを列挙します。
	 * <p>当メソッドは{@link #enumSounds(boolean, boolean, boolean, IntConsumer)}を以下のパラメータで呼び出します。</p>
	 * <pre>enumSounds(false, false, true, action);</pre>
	 * @param action ノート1個に対して何らかの処理を行う関数
	 * @exception NullPointerException actionがnull
	 * @since 0.8.0
	 */
	public final void enumBgms(IntConsumer action) {
		enumSounds(false, false, true, action);
	}

	/**
	 * サウンドに関連するノートを全て列挙します。
	 * <p>当メソッドは{@link #enumSounds(boolean, boolean, boolean, IntConsumer)}を以下のパラメータで呼び出します。</p>
	 * <pre>enumSounds(true, true, true, action);</pre>
	 * @param action ノート1個に対して何らかの処理を行う関数
	 * @exception NullPointerException actionがnull
	 * @since 0.8.0
	 */
	public final void enumSounds(IntConsumer action) {
		enumSounds(true, true, true, action);
	}

	/**
	 * サウンドに関連するノートを列挙します。
	 * <p>楽曲位置情報が持つ可視オブジェ・不可視オブジェ・BGMから列挙する種類のノートを選択し、
	 * 指定した関数へノートを通知します。関数へ通知される値はノートの生値であり、この値には複数の情報が含まれます。
	 * そのままの状態では音声データを検索するインデックス値としては使用できないことに注意してください。</p>
	 * <p>列挙されたノートの生値に含まれる情報を取り出すには{@link BeMusicSound}を使用します。
	 * 例えば音声データを検索するインデックス値を取り出したい時は{@link BeMusicSound#getTrackId(int)}を呼び出します。</p>
	 * <p>列挙順は常に可視オブジェ、不可視オブジェ、BGMの順になり、引数で選択された種類のノートが列挙されます。
	 * ただし、選択した種類のノートが楽曲位置情報に存在しない場合は関数へは通知されません。</p>
	 * @param visible 可視オブジェを列挙するかどうか
	 * @param invisible 不可視オブジェを列挙するかどうか
	 * @param bgm BGMを列挙するかどうか
	 * @param action ノート1個に対して何らかの処理を行う関数
	 * @exception NullPointerException actionがnull
	 * @see BeMusicSound
	 * @since 0.8.0
	 */
	public final void enumSounds(boolean visible, boolean invisible, boolean bgm, IntConsumer action) {
		assertArgNotNull(action, "action");
		var pos = 0;
		if (visible && ((pos = getNotePos(RawNotes.VISIBLE)) != NA)) {
			for (var i = 0; i < VC; i++) { action.accept(mNotes[pos + i]); }
		}
		if (invisible && ((pos = getNotePos(RawNotes.INVISIBLE)) != NA)) {
			for (var i = 0; i < IC; i++) { action.accept(mNotes[pos + i]); }
		}
		if (bgm && ((pos = getNotePos(RawNotes.BGM)) != NA)) {
			var bc = getBgmCount();
			for (var i = 0; i < bc; i++) { action.accept(mNotes[pos + i]); }
		}
	}

	/**
	 * 可視オブジェのノートを走査します。
	 * <p>当メソッドは{@link #sounds(boolean, boolean, boolean)}を以下のパラメータで呼び出します。</p>
	 * <pre>sounds(true, false, false);</pre>
	 * @return 可視オブジェのノートを走査するストリーム
	 * @since 0.8.0
	 */
	public final IntStream visibles() {
		return sounds(true, false, false);
	}

	/**
	 * 不可視オブジェのノートを走査します。
	 * <p>当メソッドは{@link #sounds(boolean, boolean, boolean)}を以下のパラメータで呼び出します。</p>
	 * <pre>sounds(false, true, false);</pre>
	 * @return 不可視オブジェのノートを走査するストリーム
	 * @since 0.8.0
	 */
	public final IntStream invisibles() {
		return sounds(false, true, false);
	}

	/**
	 * BGMのノートを走査します。
	 * <p>当メソッドは{@link #sounds(boolean, boolean, boolean)}を以下のパラメータで呼び出します。</p>
	 * <pre>sounds(false, false, true);</pre>
	 * @return BGMのノートを走査するストリーム
	 * @since 0.8.0
	 */
	public final IntStream bgms() {
		return sounds(false, false, true);
	}

	/**
	 * 全てのサウンドに関連するノートを走査します。
	 * <p>当メソッドは{@link #sounds(boolean, boolean, boolean)}を以下のパラメータで呼び出します。</p>
	 * <pre>sounds(true, true, true);</pre>
	 * @return 全てのサウンドに関連するノートを走査するストリーム
	 * @since 0.8.0
	 */
	public final IntStream sounds() {
		return sounds(true, true, true);
	}

	/**
	 * サウンドに関連するノートを走査します。
	 * <p>楽曲位置情報が持つ可視オブジェ・不可視オブジェ・BGMから、走査する種類のノートを選択し走査します。
	 * 通知される値はノートの生値であり、この値には複数の情報が含まれます。
	 * そのままの状態では音声データを検索するインデックス値としては使用できないことに注意してください。</p>
	 * <p>走査されたノートの生値に含まれる情報を取り出すには{@link BeMusicSound}を使用します。
	 * 例えば音声データを検索するインデックス値を取り出したい時は{@link BeMusicSound#getTrackId(int)}を呼び出します。</p>
	 * <p>走査順は常に可視オブジェ、不可視オブジェ、BGMの順になり、引数で選択された種類のノートが走査されます。
	 * ただし、選択した種類のノートが楽曲位置情報に存在しない場合は走査されません。</p>
	 * @param visible 可視オブジェを列挙するかどうか
	 * @param invisible 不可視オブジェを列挙するかどうか
	 * @param bgm BGMを列挙するかどうか
	 * @return 選択された種類のノートを走査するストリーム
	 * @since 0.8.0
	 */
	public final IntStream sounds(boolean visible, boolean invisible, boolean bgm) {
		return StreamSupport.intStream(new SoundSpliterator(visible, invisible, bgm), false);
	}

	/**
	 * 楽曲位置情報が構築された時に実行されます。
	 * <p>当メソッドが実行されるのはオブジェクトのベースクラスである{@link BeMusicPoint}の構築処理が完了した後です。
	 * 従って、クラスのGetterを使用することで構築済みの情報にアクセス可能な状態となっています。</p>
	 * <p>当メソッドの意図は、ベースクラスを拡張したクラスにおいて自身が必要とする情報を構築する機会を提供する
	 * ことにあります。メソッドは全ての情報が設定された後で実行され、当メソッドの実行が完了する時には全ての情報構築が
	 * 完了していることが推奨されています。</p>
	 */
	protected void onCreate() {
		// Do nothing
	}

	/**
	 * 楽曲位置情報のセットアップ
	 * @param measure 小節番号
	 * @param tick 小節の刻み位置
	 * @param time 楽曲位置の時間
	 * @param property 楽曲位置のプロパティ
	 * @param vt 可視オブジェのノート種別リスト (nullの場合可視オブジェなし)
	 * @param vv 可視オブジェの値リスト (nullの場合可視オブジェなし)
	 * @param iv 不可視オブジェの値リスト (nullの場合不可視オブジェなし)
	 * @param bg BGAの値(BGA/Layer/Miss) (nullの場合BGAなし)
	 * @param bgms BGMの値リスト (null不可)
	 */
	final void setup(int measure, double tick, double time, PointProperty property,
			BeMusicNoteType[] vt, int[] vv, int[] iv, int[] bg, List<Integer> bgms) {
		// 楽曲位置情報の基本情報を初期化する
		mMeasure = measure;
		mTick = tick;
		mTime = time;
		mProperty = property;

		// ノート配列を初期化する
		var hasVisible = (vt == null) ? 0 : 1;
		var hasInvisible = (iv == null) ? 0 : 1;
		var hasBga = (bg == null) ? 0 : 1;
		var hasBgm = bgms.isEmpty() ? 0 : 1;
		var noteBits = hasVisible | (hasInvisible << 1) | (hasBga << 2) | (hasBgm << 3);
		if (noteBits == 0x00) {
			// 全ノートなしの場合はノート配列をnullとする
			mNotes = null;
		} else {
			// 各種ノートを連続した配列に敷き詰めて配置する
			// 順番は、可視オブジェ→不可視オブジェ→BGA(BGA,Layere,Miss)→BGM
			var lenNotes = (hasVisible * VC) + (hasInvisible * IC) + (hasBga * BC) + bgms.size();
			var notes = new int[lenNotes];
			var pos = 0;
			if (hasVisible != 0) {
				for (var i = 0; i < VC; i++) { notes[pos + i] = RawNotes.visible(i, vt[i].getId(), vv[i]); }
				pos += VC;
			}
			if (hasInvisible != 0) {
				for (var i = 0; i < IC; i++) { notes[pos + i] = RawNotes.invisible(i, iv[i]); }
				pos += IC;
			}
			if (hasBga != 0) {
				for (var i = 0; i < BC; i++) { notes[pos + i] = RawNotes.bga(bg[i]); }
				pos += BC;
			}
			if (hasBgm != 0) {
				var mc = bgms.size();
				for (var i = 0; i < mc; i++) { notes[pos + i] = RawNotes.bgm(bgms.get(i)); }
			}
			mNotes = notes;
		}

		// ノート数をカウントして関連情報を生成する
		// これらの情報は可視オブジェが存在しなければ常に全て0を示す
		mCount = 0;
		var playable = 0;
		var movement = 0;
		var holding = 0;
		var longNoteHead = 0;
		var longNoteTail = 0;
		var longNoteType = 0;
		var hasMine = false;
		if (hasVisible != 0) {
			for (var iLane = 0; iLane < BeMusicLane.COUNT; iLane++) {
				var lane = BeMusicLane.fromIndex(iLane);
				var countShift = iLane * 4;  // 副レーンの個数情報が上位ビットに位置して隣り合う
				var hasBit = 1 << iLane;  // 主レーンが下位ビット、副レーンが上位ビットに位置して隣り合う
				var noteCount = 0;
				var lnCount = 0;
				var mineCount = 0;
				var veCount = 0;
				for (var dev : BeMusicDevice.getDevices(lane)) {
					var ntype = vt[dev.getIndex()];
					noteCount += (ntype.isCountNotes() ? 1 : 0);
					lnCount += (((ntype.isLongNoteHead()) || (ntype.isCountNotes() && ntype.isLongNoteTail())) ? 1 : 0);
					mineCount += ((ntype == BeMusicNoteType.MINE) ? 1 : 0);
					veCount += (ntype.hasVisualEffect() ? 1 : 0);
					playable |= ((ntype.isPlayable()) ? hasBit : 0);
					movement |= ((ntype.hasMovement()) ? hasBit : 0);
					holding |= ((ntype.isHolding()) ? hasBit : 0);
					longNoteHead |= (ntype.isLongNoteHead() ? hasBit : 0);
					longNoteTail |= (ntype.isLongNoteTail() ? hasBit : 0);
					longNoteType |= (ntype.isLongNoteType() ? hasBit : 0);
				}
				hasMine = hasMine || (mineCount > 0);
				mCount |= ((noteCount | (lnCount << 8) | (mineCount << 16) | (veCount << 24)) << countShift);
			}
		}

		// 各種要素の有無を集計する
		var hasBpm = (property.bpm < 0.0);
		var hasStop = (property.stop != 0.0);
		var chgSpeed = (hasBpm || property.changeScroll) ? 1 : 0;
		var gimmick = (hasBpm || property.changeScroll || hasStop || hasMine) ? 1 : 0;

		// 各種情報の値を設定する
		mInfo = noteBits | (chgSpeed << 4) | (gimmick << 5) | (playable << 6) | (movement << 8) |
				(holding << 10) | (longNoteHead << 12) | (longNoteTail << 14) | (longNoteType << 16);

		// 拡張情報初期化用のイベントを呼び出す
		onCreate();
	}

	/**
	 * ノート配列上のベースインデックス値取得
	 * @param kind ノートの種類を表す値
	 * @return ベースインデックス値。当該種類のノートがない場合は{@link #NA}
	 */
	private int getNotePos(int kind) {
		return NOTE_POSITIONS[mInfo & 0x0000000f][kind];
	}

	/**
	 * 指定種類、インデックスのノートの生値取得
	 * <p>この値は、トラックID・音声データの再開要否・ロングノートモード・ノート種別・入力デバイス・ノートの種類を表す値
	 * が規定のフォーマットで格納されている。</p>
	 * @param kind ノートの種類を表す値
	 * @param index インデックス値
	 * @return ノートの生値
	 */
	private int getRawNote(int kind, int index) {
		// 全ノートなしの場合、nullになっている
		if (mNotes == null) {
			return 0;
		}

		// 各種情報の下位4ビットに各種ノート有無のフラグが入っているので、そのフラグの組み合わせからベース位置情報を解決
		var basePos = getNotePos(kind);
		if (basePos == NA) {
			// ベース位置がN/Aの場合、当該情報なし
			return 0;
		} else {
			// ベース位置＋指定インデックス値の位置にあるノートの値を取り出す
			return mNotes[basePos + index];
		}
	}
}
