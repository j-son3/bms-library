package com.lmt.lib.bms.bemusic;

import static com.lmt.lib.bms.BmsAssertion.*;
import static com.lmt.lib.bms.bemusic.BeMusic.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.lmt.lib.bms.BmsContent;
import com.lmt.lib.bms.BmsNote;
import com.lmt.lib.bms.BmsSpec;

/**
 * BeMusicライブラリにおいて、1個のBMS定義を表すメインクラスです。
 *
 * <p>当クラスは、押下によりON/OFFの切り替えが可能な7個のスイッチと、スクラッチにより2方向の検出が可能な入力機器を1セットまたは2セット使用する
 * ゲームルール(BeMusic)に基づいた楽曲のBMS定義を表します。BMSライブラリで定義したBMSコンテンツを拡張することでそれらの定義をサポートします。
 * 具体的には、以下のような拡張が行われています。</p>
 *
 * <ul>
 * <li>BeMusicで定義されるメタ情報へアクセスするsetter/getter</li>
 * <li>ノート数の統計情報収集とその情報の照会</li>
 * <li>ノートの定義状況からプレイスタイル等の詳細情報の照会</li>
 * </ul>
 *
 * <p>当クラスは、BMSの実際の利用シーンからデファクトスタンダードであると判断した定義の実装を行っています。しかし、編集系の機能は当クラスでは
 * 拡張していません。編集を行う際は、BMSライブラリが提供する低レベルAPIを経由して行ってください。</p>
 *
 * <p>当クラスにアプリケーション固有の情報を持たせたい場合は、BMS仕様に任意型メタ情報、およびユーザーチャンネルを定義し、それらに対して
 * アプリケーションが希望する情報を挿入してください。BMS仕様の定義は{@link BeMusic#initialize}で行います。</p>
 */
public class BeMusicContent extends BmsContent {
	/** {@link #getVisibleNoteCount}の引数whichの最小値 */
	private static final int WHICH_MIN = 0;
	/** {@link #getVisibleNoteCount}の引数whichの最大値*/
	private static final int WHICH_MAX = 32;
	/** 可視ノート統計情報の数 */
	private static final int VISIBLE_NOTE_STAT_COUNT = WHICH_MAX + 1;
	/** 通常可視ノート統計情報の数 */
	private static final int VISIBLE_NOTE_COUNT = 16;
	/** 可視ノート統計情報のうち、総ノート数の格納先インデックス */
	private static final int TOTAL_NOTES_INDEX = WHICH_MAX;

	/** チャンネル番号を可視ノート統計情報配列のインデックスに変換するマップ */
	private static final HashMap<Integer, Integer> sVisibleNoteStatIndexByChNum = new HashMap<>();

	/** 可視ノート統計情報配列 */
	private long[] mVisibleNoteStats = new long[VISIBLE_NOTE_STAT_COUNT];
	/** フラグ */
	private int mFlags = 0;

	/** 静的処理 */
	static {
		// 1P側
		sVisibleNoteStatIndexByChNum.put(BeMusicChannel.VISIBLE_1P_01.getNumber(), KEY1);
		sVisibleNoteStatIndexByChNum.put(BeMusicChannel.VISIBLE_1P_02.getNumber(), KEY2);
		sVisibleNoteStatIndexByChNum.put(BeMusicChannel.VISIBLE_1P_03.getNumber(), KEY3);
		sVisibleNoteStatIndexByChNum.put(BeMusicChannel.VISIBLE_1P_04.getNumber(), KEY4);
		sVisibleNoteStatIndexByChNum.put(BeMusicChannel.VISIBLE_1P_05.getNumber(), KEY5);
		sVisibleNoteStatIndexByChNum.put(BeMusicChannel.VISIBLE_1P_08.getNumber(), KEY6);
		sVisibleNoteStatIndexByChNum.put(BeMusicChannel.VISIBLE_1P_09.getNumber(), KEY7);
		sVisibleNoteStatIndexByChNum.put(BeMusicChannel.VISIBLE_1P_06.getNumber(), SCRATCH1);
		// 2P側
		sVisibleNoteStatIndexByChNum.put(BeMusicChannel.VISIBLE_2P_01.getNumber(), KEY8);
		sVisibleNoteStatIndexByChNum.put(BeMusicChannel.VISIBLE_2P_02.getNumber(), KEY9);
		sVisibleNoteStatIndexByChNum.put(BeMusicChannel.VISIBLE_2P_03.getNumber(), KEY10);
		sVisibleNoteStatIndexByChNum.put(BeMusicChannel.VISIBLE_2P_04.getNumber(), KEY11);
		sVisibleNoteStatIndexByChNum.put(BeMusicChannel.VISIBLE_2P_05.getNumber(), KEY12);
		sVisibleNoteStatIndexByChNum.put(BeMusicChannel.VISIBLE_2P_08.getNumber(), KEY13);
		sVisibleNoteStatIndexByChNum.put(BeMusicChannel.VISIBLE_2P_09.getNumber(), KEY14);
		sVisibleNoteStatIndexByChNum.put(BeMusicChannel.VISIBLE_2P_06.getNumber(), SCRATCH2);
	}

	/**
	 * コンストラクタ
	 * @param spec BMS仕様
	 */
	BeMusicContent(BmsSpec spec) {
		super(spec);
	}

	/**
	 * #GENREを設定する。
	 * @param genre #GENREの値
	 * @see BeMusicMeta#GENRE
	 */
	public final void setGenre(String genre) {
		setSingleMeta(BeMusicMeta.GENRE.getName(), genre);
	}

	/**
	 * #GENREを取得する。
	 * @return #GENREの値
	 * @see BeMusicMeta#GENRE
	 */
	public final String getGenre() {
		return (String)getSingleMeta(BeMusicMeta.GENRE.getName());
	}

	/**
	 * #TITLEを設定する。
	 * @param title #TITLEの値
	 * @see BeMusicMeta#TITLE
	 */
	public final void setTitle(String title) {
		setSingleMeta(BeMusicMeta.TITLE.getName(), title);
	}

	/**
	 * #TITLEを取得する。
	 * @return #TITLEの値
	 * @see BeMusicMeta#TITLE
	 */
	public final String getTitle() {
		return (String)getSingleMeta(BeMusicMeta.TITLE.getName());
	}

	/**
	 * #SUBTITLEを設定する。
	 * @param subTitle #SUBTITLEの値
	 * @see BeMusicMeta#SUBTITLE
	 */
	public final void setSubTitle(String subTitle) {
		setSingleMeta(BeMusicMeta.SUBTITLE.getName(), subTitle);
	}

	/**
	 * #SUBTITLEを取得する。
	 * @return #SUBTITLEの値
	 * @see BeMusicMeta#SUBTITLE
	 */
	public final String getSubTitle() {
		return (String)getSingleMeta(BeMusicMeta.SUBTITLE.getName());
	}

	/**
	 * #ARTISTを設定する。
	 * @param artist #ARTISTの値
	 * @see BeMusicMeta#ARTIST
	 */
	public final void setArtist(String artist) {
		setSingleMeta(BeMusicMeta.ARTIST.getName(), artist);
	}

	/**
	 * #ARTISTを取得する。
	 * @return #ARTISTの値
	 * @see BeMusicMeta#ARTIST
	 */
	public final String getArtist() {
		return (String)getSingleMeta(BeMusicMeta.ARTIST.getName());
	}

	/**
	 * #SUBARTISTを設定する。
	 * @param index インデックス
	 * @param subArtist #SUBARTISTの値
	 * @see BeMusicMeta#SUBARTIST
	 */
	public final void setSubArtist(int index, String subArtist) {
		setMultipleMeta(BeMusicMeta.SUBARTIST.getName(), index, subArtist);
	}

	/**
	 * #SUBARTISTのリストを取得する。
	 * @return #SUBARTISTのリスト
	 * @see BeMusicMeta#SUBARTIST
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public final List<String> getSubArtists() {
		var subArtists = getMultipleMetas(BeMusicMeta.SUBARTIST.getName());
		return new ArrayList(subArtists);
	}

	/**
	 * #SUBARTISTを取得する。
	 * @param index インデックス
	 * @return #SUBARTISTの値
	 * @see BeMusicMeta#SUBARTIST
	 */
	public final String getSubArtist(int index) {
		return (String)getMultipleMeta(BeMusicMeta.SUBARTIST.getName(), index);
	}

	/**
	 * #PLAYERを設定する。
	 * @param player #PLAYERの値
	 * @see BeMusicPlayer
	 * @see BeMusicMeta#PLAYER
	 */
	public final void setPlayer(BeMusicPlayer player) {
		setSingleMeta(BeMusicMeta.PLAYER.getName(), (player == null) ? null : player.getNativeValue());
	}

	/**
	 * #PLAYERを取得する。
	 * @return #PLAYERの値
	 * @see BeMusicPlayer
	 * @see BeMusicMeta#PLAYER
	 */
	public final BeMusicPlayer getPlayer() {
		return BeMusicPlayer.fromNativeValue(getSingleMeta(BeMusicMeta.PLAYER.getName()));
	}

	/**
	 * #RANKを設定する。
	 * @param rank #RANKの値
	 * @see BeMusicRank
	 * @see BeMusicMeta#RANK
	 */
	public final void setRank(BeMusicRank rank) {
		setSingleMeta(BeMusicMeta.RANK.getName(), (rank == null) ? null : rank.getNativeValue());
	}

	/**
	 * #RANKを取得する。
	 * @return #RANKの値
	 * @see BeMusicRank
	 * @see BeMusicMeta#RANK
	 */
	public final BeMusicRank getRank() {
		return BeMusicRank.fromNativeValue(getSingleMeta(BeMusicMeta.RANK.getName()));
	}

	/**
	 * #TOTALを設定する。
	 * @param total #TOTALの値
	 * @see BeMusicMeta#TOTAL
	 */
	public final void setTotal(Long total) {
		setSingleMeta(BeMusicMeta.TOTAL.getName(), total);
	}

	/**
	 * #TOTALを取得する。
	 * @return #TOTALの値
	 * @see BeMusicMeta#TOTAL
	 */
	public final long getTotal() {
		return (long)getSingleMeta(BeMusicMeta.TOTAL.getName());
	}

	/**
	 * #STAGEFILEを設定する。
	 * @param stageFile #STAGEFILEの値
	 * @see BeMusicMeta#STAGEFILE
	 */
	public final void setStageFile(String stageFile) {
		setSingleMeta(BeMusicMeta.STAGEFILE.getName(), stageFile);
	}

	/**
	 * #STAGEFILEを取得する。
	 * @return #STAGEFILEの値
	 * @see BeMusicMeta#STAGEFILE
	 */
	public final String getStageFile() {
		return (String)getSingleMeta(BeMusicMeta.STAGEFILE.getName());
	}

	/**
	 * #BANNERを設定する。
	 * @param banner #BANNERの値
	 * @see BeMusicMeta#BANNER
	 */
	public final void setBanner(String banner) {
		setSingleMeta(BeMusicMeta.BANNER.getName(), banner);
	}

	/**
	 * #BANNERを取得する。
	 * @return #BANNERの値
	 * @see BeMusicMeta#BANNER
	 */
	public final String getBanner() {
		return (String)getSingleMeta(BeMusicMeta.BANNER.getName());
	}

	/**
	 * #BACKBMPを設定する。
	 * @param backBmp #BACKBMPの値
	 * @see BeMusicMeta#BACKBMP
	 */
	public final void setBackBmp(String backBmp) {
		setSingleMeta(BeMusicMeta.BACKBMP.getName(), backBmp);
	}

	/**
	 * #BACKBMPを取得する。
	 * @return #BACKBMPの値
	 * @see BeMusicMeta#BACKBMP
	 */
	public final String getBackBmp() {
		return (String)getSingleMeta(BeMusicMeta.BACKBMP.getName());
	}

	/**
	 * #PLAYLEVELを設定する。
	 * @param playLevel #PLAYLEVELの値
	 * @see BeMusicMeta#PLAYLEVEL
	 */
	public final void setPlayLevel(Double playLevel) {
		setSingleMeta(BeMusicMeta.PLAYLEVEL.getName(), playLevel);
	}

	/**
	 * #PLAYLEVELを取得する。
	 * @return #PLAYLEVELの値
	 * @see BeMusicMeta#PLAYLEVEL
	 */
	public final double getPlayLevel() {
		return (double)getSingleMeta(BeMusicMeta.PLAYLEVEL.getName());
	}

	/**
	 * #DIFFICULTYを設定する。
	 * @param difficulty #DIFFICULTYの値
	 * @see BeMusicDifficulty
	 * @see BeMusicMeta#DIFFICULTY
	 */
	public final void setDifficulty(BeMusicDifficulty difficulty) {
		setSingleMeta(BeMusicMeta.DIFFICULTY.getName(), (difficulty == null) ? null : difficulty.getNativeValue());
	}

	/**
	 * #DIFFICULTYを取得する。
	 * @return #DIFFICULTYの値
	 * @see BeMusicMeta#DIFFICULTY
	 */
	public final BeMusicDifficulty getDifficulty() {
		return BeMusicDifficulty.fromNativeValue(getSingleMeta(BeMusicMeta.DIFFICULTY.getName()));
	}

	/**
	 * #LNOBJを設定する。
	 * @param index インデックス
	 * @param lnObj #LNOBJの値
	 * @exception IndexOutOfBoundsException indexが0～BmsSpec.MULTIPLE_META_INDEX_MAXの範囲外
	 * @see BeMusicMeta#LNOBJ
	 */
	public final void setLnObj(int index, Long lnObj) {
		setMultipleMeta(BeMusicMeta.LNOBJ.getName(), index, lnObj);
	}

	/**
	 * #LNOBJのリストを取得する。
	 * @return #LNOBJのリスト
	 * @see BeMusicMeta#LNOBJ
	 */
	public final List<Long> getLnObjs() {
		return getMultipleMetas(BeMusicMeta.LNOBJ.getName());
	}

	/**
	 * #LNOBJを取得する。
	 * @param index インデックス
	 * @return #LNOBJの値
	 * @exception IndexOutOfBoundsException indexが0～BmsSpec.MULTIPLE_META_INDEX_MAXの範囲外
	 * @see BeMusicMeta#LNOBJ
	 */
	public final long getLnObj(int index) {
		return (long)getMultipleMeta(BeMusicMeta.LNOBJ.getName(), index);
	}

	/**
	 * %URLを設定する。
	 * @param url %URLの値
	 * @see BeMusicMeta#URL
	 */
	public final void setUrl(String url) {
		setSingleMeta(BeMusicMeta.URL.getName(), url);
	}

	/**
	 * %URLを取得する。
	 * @return %URLの値
	 * @see BeMusicMeta#URL
	 */
	public final String getUrl() {
		return (String)getSingleMeta(BeMusicMeta.URL.getName());
	}

	/**
	 * %EMAILを設定する。
	 * @param email %EMAILの値
	 * @see BeMusicMeta#EMAIL
	 */
	public final void setEmail(String email) {
		setSingleMeta(BeMusicMeta.EMAIL.getName(), email);
	}

	/**
	 * %EMAILを取得する。
	 * @return %EMAILの値
	 * @see BeMusicMeta#EMAIL
	 */
	public final String getEmail() {
		return (String)getSingleMeta(BeMusicMeta.EMAIL.getName());
	}

	/**
	 * #BPMxxを設定する。
	 * @param index インデックス
	 * @param bpm #BPMxxの値
	 * @exception IndexOutOfBoundsException indexが0～BmsSpec.INDEXED_META_INDEX_MAXの範囲外
	 * @see BeMusicMeta#BPM
	 */
	public final void setBpm(int index, Double bpm) {
		setIndexedMeta(BeMusicMeta.BPM.getName(), index, bpm);
	}

	/**
	 * #BPMxxを取得する。
	 * @param index インデックス
	 * @return #BPMxxの値
	 * @exception IndexOutOfBoundsException indexが0～BmsSpec.INDEXED_META_INDEX_MAXの範囲外
	 * @see BeMusicMeta#BPM
	 */
	public final double getBpm(int index) {
		return getIndexedMeta(BeMusicMeta.BPM.getName(), index);
	}

	/**
	 * #BPMxxのリストを取得する。
	 * @return #BPMxxのリスト
	 * @see BeMusicMeta#BPM
	 */
	public final Map<Integer, Double> getBpms() {
		return getIndexedMetas(BeMusicMeta.BPM.getName());
	}

	/**
	 * #STOPxxを設定する。
	 * @param index インデックス
	 * @param stop #STOPの値
	 * @exception IndexOutOfBoundsException indexが0～BmsSpec.INDEXED_META_INDEX_MAXの範囲外
	 * @see BeMusicMeta#STOP
	 */
	public final void setStop(int index, Long stop) {
		setIndexedMeta(BeMusicMeta.STOP.getName(), index, stop);
	}

	/**
	 * #STOPxxを取得する。
	 * @param index インデックス
	 * @return #STOPの値
	 * @exception IndexOutOfBoundsException indexが0～BmsSpec.INDEXED_META_INDEX_MAXの範囲外
	 * @see BeMusicMeta#STOP
	 */
	public final long getStop(int index) {
		return getIndexedMeta(BeMusicMeta.STOP.getName(), index);
	}

	/**
	 * #STOPxxのリストを取得する。
	 * @return #STOPxxのリスト
	 * @see BeMusicMeta#STOP
	 */
	public final Map<Integer, Long> getStops() {
		return getIndexedMetas(BeMusicMeta.STOP.getName());
	}

	/**
	 * #WAVxxを設定する。
	 * @param index インデックス
	 * @param wav #WAVの値
	 * @exception IndexOutOfBoundsException indexが0～BmsSpec.INDEXED_META_INDEX_MAXの範囲外
	 * @see BeMusicMeta#WAV
	 */
	public final void setWav(int index, String wav) {
		setIndexedMeta(BeMusicMeta.WAV.getName(), index, wav);
	}

	/**
	 * #WAVxxを取得する。
	 * @param index インデックス
	 * @return #WAVの値
	 * @exception IndexOutOfBoundsException indexが0～BmsSpec.INDEXED_META_INDEX_MAXの範囲外
	 * @see BeMusicMeta#WAV
	 */
	public final String getWav(int index) {
		return getIndexedMeta(BeMusicMeta.WAV.getName(), index);
	}

	/**
	 * #WAVxxのリストを取得する。
	 * @return #WAVxxのリスト
	 * @see BeMusicMeta#WAV
	 */
	public final Map<Integer, String> getWavs() {
		return getIndexedMetas(BeMusicMeta.WAV.getName());
	}

	/**
	 * #BMPxxを設定する。
	 * @param index インデックス
	 * @param bmp #BMPxxの値
	 * @exception IndexOutOfBoundsException indexが0～BmsSpec.INDEXED_META_INDEX_MAXの範囲外
	 * @see BeMusicMeta#BMP
	 */
	public final void setBmp(int index, String bmp) {
		setIndexedMeta(BeMusicMeta.BMP.getName(), index, bmp);
	}

	/**
	 * #BMPxxを取得する。
	 * @param index インデックス
	 * @return #BMPxxの値
	 * @see BeMusicMeta#BMP
	 */
	public final String getBmp(int index) {
		return getIndexedMeta(BeMusicMeta.BMP.getName(), index);
	}

	/**
	 * #BMPxxのリストを取得する。
	 * @return #BMPxxのリスト
	 * @see BeMusicMeta#BMP
	 */
	public final Map<Integer, String> getBmps() {
		return getIndexedMetas(BeMusicMeta.BMP.getName());
	}

	/**
	 * 可視オブジェのノート数を取得する。
	 * @param which 可視オブジェの種類
	 * @return 可視オブジェのノート数
	 * @see BeMusic#KEY1
	 * @see BeMusic#KEY2
	 * @see BeMusic#KEY3
	 * @see BeMusic#KEY4
	 * @see BeMusic#KEY5
	 * @see BeMusic#KEY6
	 * @see BeMusic#KEY7
	 * @see BeMusic#SCRATCH1
	 * @see BeMusic#KEY8
	 * @see BeMusic#KEY9
	 * @see BeMusic#KEY10
	 * @see BeMusic#KEY11
	 * @see BeMusic#KEY12
	 * @see BeMusic#KEY13
	 * @see BeMusic#KEY14
	 * @see BeMusic#SCRATCH2
	 * @see BeMusic#KEY1_LN
	 * @see BeMusic#KEY2_LN
	 * @see BeMusic#KEY3_LN
	 * @see BeMusic#KEY4_LN
	 * @see BeMusic#KEY5_LN
	 * @see BeMusic#KEY6_LN
	 * @see BeMusic#KEY7_LN
	 * @see BeMusic#SCRATCH1_LN
	 * @see BeMusic#KEY8_LN
	 * @see BeMusic#KEY9_LN
	 * @see BeMusic#KEY10_LN
	 * @see BeMusic#KEY11_LN
	 * @see BeMusic#KEY12_LN
	 * @see BeMusic#KEY13_LN
	 * @see BeMusic#KEY14_LN
	 * @see BeMusic#SCRATCH2_LN
	 * @see BeMusic#TOTAL_NOTES
	 */
	public final long getVisibleNoteCount(int which) {
		assertIsReferenceMode();
		assertArgRange(which, WHICH_MIN, WHICH_MAX, "which");
		return mVisibleNoteStats[which];
	}

	/**
	 * シングルプレイの譜面かどうかを取得します。
	 * @return シングルプレイの場合true
	 */
	public final boolean isSinglePlay() {
		assertIsReferenceMode();
		return ((mFlags & FLAG_DOUBLE) == 0);
	}

	/**
	 * ダブルプレイの譜面かどうかを取得します。
	 * @return ダブルプレイの場合true
	 */
	public final boolean isDoublePlay() {
		assertIsReferenceMode();
		return ((mFlags & FLAG_DOUBLE) != 0);
	}

	/**
	 * 可視オブジェにロングノートが含まれるかどうかを取得します。
	 * @return 可視オブジェにロングノートが含まれる場合true
	 */
	public final boolean isLongNoteUsed() {
		assertIsReferenceMode();
		return ((mFlags & FLAG_LN) != 0);
	}

	/**
	 * BGAが使用されているかどうかを取得します。
	 * @return BGAが使用されている場合true
	 */
	public final boolean isBgaUsed() {
		assertIsReferenceMode();
		return ((mFlags & FLAG_BGA) != 0);
	}

	/**
	 * マイナーな定義を使用しているかどうかを取得します。
	 * @return マイナーな定義を使用している場合true
	 */
	public final boolean isMinorUsed() {
		assertIsReferenceMode();
		return ((mFlags & FLAG_MINOR) != 0);
	}

	/**
	 * 旧式の定義を使用しているかどうかを取得します。
	 * @return 旧式の定義を使用している場合true
	 */
	public final boolean isLegacyUsed() {
		assertIsReferenceMode();
		return ((mFlags & FLAG_LEGACY) != 0);
	}

	/**
	 * 非推奨の定義を使用しているかどうかを取得します。
	 * @return 非推奨の定義を使用している場合true
	 */
	public final boolean isUnrecommendUsed() {
		assertIsReferenceMode();
		return ((mFlags & FLAG_UNRECOMMEND) != 0);
	}

	/** {@inheritDoc} */
	protected void onEndEdit(boolean isRecalculateTime) {
		super.onEndEdit(isRecalculateTime);

		// 編集モード終了後のメタ情報とチャンネルから統計情報を収集する
		var metaFlags = collectMetaFlags();
		var channelStat = collectChannelStatistics();

		// 統計情報を反映する
		mFlags = (metaFlags | channelStat.flags);
		mVisibleNoteStats = channelStat.visibleNoteStats;
	}

	/**
	 * メタ情報のフラグを取得する。
	 * @return メタ情報のフラグ
	 */
	private int collectMetaFlags() {
		var flags = FLAG_NONE;
		var metas = getSpec().getMetas();
		for (var meta : metas) {
			if (containsMeta(meta.getName(), meta.getUnit())) {
				flags |= BeMusicMeta.getFlag(meta);
			}
		}
		return flags;
	}

	/** チャンネル統計情報収集クラス。 */
	private class ChannelStatisticsCollector implements BmsNote.Tester {
		/** フラグ */
		int flags = FLAG_NONE;
		/** 可視オブジェ統計情報配列 */
		long[] visibleNoteStats = new long[VISIBLE_NOTE_STAT_COUNT];
		/** 各チャンネルのキー押下状態 */
		private boolean[] mKeyPresses = new boolean[VISIBLE_NOTE_COUNT];
		/** #LNOBJのリスト */
		private ArrayList<Integer> mLnObjs = new ArrayList<>();

		/** コンストラクタ */
		ChannelStatisticsCollector() {
			var lnObjName = BeMusicMeta.LNOBJ.getName();
			var lnObjCount = getMultipleMetaCount(lnObjName);
			for (var i = 0; i < lnObjCount; i++) {
				mLnObjs.add(((Long)getMultipleMeta(lnObjName, i)).intValue());
			}
		}

		/** {@inheritDoc} */
		@Override
		public boolean testNote(BmsNote note) {
			// フラグを更新する
			flags |= BeMusicChannel.getFlag(note.getChannel());

			// 可視ノートの統計を更新する
			updateVisibleNoteStatistics(note);

			return true;
		}

		/**
		 * 可視オブジェ統計情報を更新する。
		 * @param note チェック対象ノート
		 */
		private void updateVisibleNoteStatistics(BmsNote note) {
			// チャンネルに該当する可視ノートの統計インデックスを取得する
			var index = sVisibleNoteStatIndexByChNum.get(note.getChannel());
			if (index == null) {
				// 7Keys SP/DPを構成するチャンネル以外の場合は何もしない
				return;
			}

			// LNOBJ指定かどうか判定する
			var i = index.intValue();
			if (mLnObjs.indexOf(note.getValue()) == -1) {
				// 通常ノートを示す場合の処理
				mKeyPresses[i] = true;
				visibleNoteStats[i]++;
				visibleNoteStats[TOTAL_NOTES_INDEX]++;
			} else if (mKeyPresses[i]) {
				// キー押下状態でロングノート終端を検出した場合はキー押下解除し、ロングノートを検出したものとする
				mKeyPresses[i] = false;
				flags |= FLAG_LN;
				visibleNoteStats[i + VISIBLE_NOTE_COUNT]++;
			} else {
				// キー解放状態でLNOBJを検出した場合はノート数としてカウントしない
				// Do nothing
			}
		}
	}

	/**
	 * チャンネル統計情報収集を行う。
	 * @return チャンネル統計情報収集結果
	 */
	private ChannelStatisticsCollector collectChannelStatistics() {
		var collector = new ChannelStatisticsCollector();
		if (getMeasureCount() > 0) {
			enumNotes(collector);
		}
		return collector;
	}
}
