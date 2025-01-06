package com.lmt.lib.bms.bemusic;

import static com.lmt.lib.bms.internal.Assertion.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.function.Function;
import java.util.function.Supplier;

import com.lmt.lib.bms.BmsContent;
import com.lmt.lib.bms.BmsInt;
import com.lmt.lib.bms.BmsSpec;

/**
 * Be Musicデータフォーマットのヘッダ定義を表します。
 *
 * <p>当クラスでは{@link BmsContent}からBe Musicの仕様に基づいてメタ情報を収集します。
 * 収集するメタ情報は{@link BeMusicMeta}に定義されているメタ情報ですが、一部の特定アプリケーションのみが
 * 対応しているマイナーなメタ情報や、時代遅れとなり使用されなくなったメタ情報は収集の対象外となります。
 * 収集対象のメタ情報については当クラスが持つgetterのメソッドを参照してください。</p>
 *
 * <p>データ数の多い以下の索引付きメタ情報も収集することができます。</p>
 *
 * <ul>
 * <li>{@link BeMusicMeta#WAV}</li>
 * <li>{@link BeMusicMeta#BMP}</li>
 * <li>{@link BeMusicMeta#BPM}</li>
 * <li>{@link BeMusicMeta#STOP}</li>
 * <li>{@link BeMusicMeta#SCROLL}</li>
 * <li>{@link BeMusicMeta#TEXT}</li>
 * </ul>
 *
 * <p>アプリケーションによっては上記の索引付きメタ情報は冗長で必要ない場合が想定されます。
 * そのようなケースでは当クラスのインスタンス生成時に索引付きメタ情報の収集を除外することができます。</p>
 *
 * <p>アプリケーション側で収集するヘッダ情報を拡張したい場合は当クラスを継承し、{@link #onCreate(BmsContent, int)}を
 * オーバーライドすることでそれが可能になります。先述のメソッド内で{@link BmsContent}から希望するメタ情報を
 * 抽出してください。</p>
 *
 * @since 0.0.1
 */
public class BeMusicHeader {
	/**
	 * ヘッダ定義構築時、#WAVの情報を収集するフラグです。
	 * @see BeMusicMeta#WAV
	 */
	public static final int WAV = 0x0001;
	/**
	 * ヘッダ定義構築時、#BMPの情報を収集するフラグです。
	 * @see BeMusicMeta#BMP
	 */
	public static final int BMP = 0x0002;
	/**
	 * ヘッダ定義構築時、#BPMの情報を収集するフラグです。
	 * @see BeMusicMeta#BPM
	 */
	public static final int BPM = 0x0004;
	/**
	 * ヘッダ定義構築時、#STOPの情報を収集するフラグです。
	 * @see BeMusicMeta#STOP
	 */
	public static final int STOP = 0x0008;
	/**
	 * ヘッダ定義構築時、#TEXTの情報を収集するフラグです。
	 * @see BeMusicMeta#TEXT
	 */
	public static final int TEXT = 0x0010;
	/**
	 * ヘッダ定義構築時、#SCROLLの情報を収集するフラグです。
	 * @see BeMusicMeta#SCROLL
	 */
	public static final int SCROLL = 0x0020;
	/**
	 * ヘッダ定義構築時、フラグで情報収集有無を選択可能なメタ情報を全て収集しないことを示す値です。
	 */
	public static final int NONE = 0;
	/**
	 * ヘッダ定義構築時、当クラスがサポートする全てのヘッダ定義を収集することを示す値です。
	 * この値は、現時点では関連項目に該当するフラグを全て含むことを表します。
	 * @see #WAV
	 * @see #BMP
	 * @see #BPM
	 * @see #STOP
	 * @see #SCROLL
	 * @see #TEXT
	 */
	public static final int ALL = -1;
	/**
	 * ヘッダ定義収集フラグのユーザ定義領域を示すマスク値です。
	 * <p>ヘッダ定義収集フラグのライブラリ定義値はフラグの下位16ビット分を使用するのに対し、ユーザ定義領域は
	 * 上位16ビットを使用して表現できます。将来のライブラリ拡張が行われても、ユーザ定義領域の上位16ビット分が
	 * 別の用途で使用されないことはライブラリとして保証します。</p>
	 */
	public static final int FLAG_USER_AREA = 0xffff0000;

	/** #BASE */
	private int mBase = 0;
	/** #PLAYER */
	private BeMusicPlayer mPlayer = BeMusicPlayer.OTHER;
	/** #GENRE */
	private String mGenre = "";
	/** #TITLE */
	private String mTitle = "";
	/** #SUBTITLE */
	private String mSubTitle = "";
	/** #ARTIST */
	private String mArtist = "";
	/** #SUBARTIST */
	private List<String> mSubArtists = Collections.emptyList();
	/** #BPM(初期BPM) */
	private double mInitialBpm = 0.0;
	/** #DIFFICULTY */
	private BeMusicDifficulty mDifficulty = BeMusicDifficulty.OTHER;
	/** #CHARTNAME */
	private String mChartName = "";
	/** #PLAYLEVEL */
	private String mPlayLevelRaw = "";
	/** #PLAYLEVEL(数値) */
	private double mPlayLevel = 0.0;
	/** #RANK */
	private BeMusicRank mRank = BeMusicRank.OTHER;
	/** #DEFEXRANK */
	private Double mDefExRank = null;
	/** #TOTAL */
	private double mTotal = 0.0;
	/** #COMMENT */
	private String mComment = "";
	/** #BANNER */
	private String mBanner = "";
	/** #STAGEFILE */
	private String mStageFile = "";
	/** #BACKBMP */
	private String mBackBmp = "";
	/** #EYECATCH */
	private String mEyecatch = "";
	/** #PREVIEW */
	private String mPreview = "";
	/** #LNOBJ */
	private List<Long> mLnObjs = Collections.emptyList();
	/** #LNMODE */
	private BeMusicLongNoteMode mLnMode = BeMusicLongNoteMode.LN;
	/** %URL */
	private String mUrl = "";
	/** %EMAIL */
	private String mEmail = "";

	/** #WAV */
	private Map<Integer, String> mWavs = Collections.emptyMap();
	/** #BMP */
	private Map<Integer, String> mBmps = Collections.emptyMap();
	/** #BPM */
	private Map<Integer, Double> mBpms = Collections.emptyMap();
	/** #STOP */
	private Map<Integer, Double> mStops = Collections.emptyMap();
	/** #SCROLL */
	private Map<Integer, Double> mScrolls = Collections.emptyMap();
	/** #TEXT */
	private Map<Integer, String> mTexts = Collections.emptyMap();

	/**
	 * 空のヘッダ定義オブジェクトを構築します。
	 * <p>このコンストラクタは当クラスを継承したヘッダクラスが{@link #of(BmsContent, Supplier)} または
	 * {@link #of(BmsContent, int, Supplier)} を使用してオブジェクトを構築する際に使用することを想定しています。</p>
	 */
	public BeMusicHeader() {
		// Do nothing
	}

	/**
	 * ヘッダ定義オブジェクトを構築します。
	 * <p>このコンストラクタを使用した場合、当クラスがサポートする全てのヘッダ定義を抽出しようとします。
	 * アプリケーションによっては不要な情報も多量に抽出し余分なオーバーヘッドがかかる場合がありますので、
	 * 抽出する情報を厳密に指定したい場合には{@link #BeMusicHeader(BmsContent, int)}を使用してください。</p>
	 * <p>BMSコンテンツは、{@link BeMusicSpec}を用いて生成されたBMS仕様に基づくものを指定してください。
	 * それ以外のBMS仕様に基づいて生成されたBMSコンテンツを指定した場合の動作保証はありません。</p>
	 * @param content BMSコンテンツ
	 * @exception NullPointerException contentがnull
	 */
	public BeMusicHeader(BmsContent content) {
		this(content, ALL);
	}

	/**
	 * ヘッダ定義オブジェクトを構築します。
	 * <p>このコンストラクタではパラメータのフラグを設定することで抽出するヘッダ定義の種類を指定することができます。
	 * 不要な情報を抽出したくない場合に使用してください。</p>
	 * <p>BMSコンテンツは、{@link BeMusicSpec}を用いて生成されたBMS仕様に基づくものを指定してください。
	 * それ以外のBMS仕様に基づいて生成されたBMSコンテンツを指定した場合の動作保証はありません。</p>
	 * @param content BMSコンテンツ
	 * @param flags ヘッダ定義収集フラグ
	 * @exception NullPointerException contentがnull
	 * @see #WAV
	 * @see #BMP
	 * @see #BPM
	 * @see #STOP
	 * @see #SCROLL
	 * @see #TEXT
	 * @see #NONE
	 * @see #ALL
	 */
	public BeMusicHeader(BmsContent content, int flags) {
		assertArgNotNull(content, "content");
		setup(content, flags);
	}

	/**
	 * #BASEを取得します。
	 * @return #BASEの値
	 * @since 0.8.0
	 */
	public final int getBase() {
		return mBase;
	}

	/**
	 * #PLAYERを取得します。
	 * @return #PLAYERの値
	 */
	public final BeMusicPlayer getPlayer() {
		return mPlayer;
	}

	/**
	 * #GENREを取得します。
	 * @return #GENREの値
	 */
	public final String getGenre() {
		return mGenre;
	}

	/**
	 * #TITLEを取得します。
	 * @return #TITLEの値
	 */
	public final String getTitle() {
		return mTitle;
	}

	/**
	 * #SUBTITLEを取得します。
	 * @return #SUBTITLEの値
	 */
	public final String getSubTitle() {
		return mSubTitle;
	}

	/**
	 * #ARTISTを取得します。
	 * @return #ARTISTの値
	 */
	public final String getArtist() {
		return mArtist;
	}

	/**
	 * #SUBARTISTを取得します。
	 * @param separator サブアーティストが複数定義されている場合の区切り文字列
	 * @return #SUBARTISTの値
	 * @exception NullPointerException separatorがnull
	 */
	public final String getSubArtist(String separator) {
		assertArgNotNull(separator, "separator");
		var joiner = new StringJoiner(separator);
		mSubArtists.forEach(sa -> { joiner.add(sa); });
		return joiner.toString();
	}

	/**
	 * #SUBARTISTを全て取得します。
	 * @return #SUBARTISTのリスト
	 */
	public final List<String> getSubArtists() {
		return new ArrayList<>(mSubArtists);
	}

	/**
	 * #BPM(初期BPM)を取得します。
	 * @return #BPMの値
	 */
	public final double getInitialBpm() {
		return mInitialBpm;
	}

	/**
	 * #DIFFICULTYを取得します。
	 * @return #DIFFICULTYの値
	 */
	public final BeMusicDifficulty getDifficulty() {
		return mDifficulty;
	}

	/**
	 * #CHARTNAMEを取得します。
	 * @return #CHARTNAMEの値
	 * @since 0.7.0
	 */
	public final String getChartName() {
		return mChartName;
	}

	/**
	 * #PLAYLEVELを取得します。
	 * @return #PLAYLEVELの値
	 */
	public final String getPlayLevelRaw() {
		return mPlayLevelRaw;
	}

	/**
	 * #PLAYLEVELの数値を取得します。
	 * <p>元の値が数値書式でない場合、取得される値は0になります。</p>
	 * @return #PLAYLEVELを数値に変換した値
	 */
	public final double getPlayLevel() {
		return mPlayLevel;
	}

	/**
	 * #RANKを取得します。
	 * @return #RANKの値
	 */
	public final BeMusicRank getRank() {
		return mRank;
	}

	/**
	 * #DEFEXRANKを取得します。
	 * <p>ヘッダに定義されていなかった場合、nullを返します。</p>
	 * @return #DEFEXRANKの値、またはnull
	 * @since 0.7.0
	 */
	public final Double getDefExRank() {
		return mDefExRank;
	}

	/**
	 * #TOTALを取得します。
	 * @return #TOTALの値
	 */
	public final double getTotal() {
		return mTotal;
	}

	/**
	 * #COMMENTを取得します。
	 * @return #COMMENTの値
	 * @since 0.5.0
	 */
	public final String getComment() {
		return mComment;
	}

	/**
	 * #BANNERを取得します。
	 * @return #BANNERの値
	 */
	public final String getBanner() {
		return mBanner;
	}

	/**
	 * #STAGEFILEを取得します。
	 * @return #STAGEFILEの値
	 */
	public final String getStageFile() {
		return mStageFile;
	}

	/**
	 * #BACKBMPを取得します。
	 * @return #BACKBMPの値
	 */
	public final String getBackBmp() {
		return mBackBmp;
	}

	/**
	 * #EYECATCHを取得します。
	 * @return #EYECATCHの値
	 * @since 0.7.0
	 */
	public final String getEyecatch() {
		return mEyecatch;
	}

	/**
	 * #PREVIEWを取得します。
	 * @return #PREVIEWの値
	 * @since 0.2.0
	 */
	public final String getPreview() {
		return mPreview;
	}

	/**
	 * #LNOBJを全て取得します。
	 * @return #LNOBJのリスト
	 */
	public final List<Long> getLnObjs() {
		return new ArrayList<>(mLnObjs);
	}

	/**
	 * #LNMODEを取得します。
	 * @return #LNMODEの値
	 * @since 0.3.0
	 */
	public final BeMusicLongNoteMode getLnMode() {
		return mLnMode;
	}

	/**
	 * %URLを取得します。
	 * @return %URLの値
	 */
	public final String getUrl() {
		return mUrl;
	}

	/**
	 * %EMAILを取得します。
	 * @return %EMAILの値
	 */
	public final String getEmail() {
		return mEmail;
	}

	/**
	 * #WAVを取得します。
	 * @param metaIndex メタ情報インデックス
	 * @return #WAVの値。インデックスに該当する値がない場合空文字。
	 */
	public final String getWav(int metaIndex) {
		return Objects.requireNonNullElse(mWavs.get(BmsInt.box(metaIndex)), "");
	}

	/**
	 * #WAVを全て取得します。
	 * <p>返されるマップは読み取り専用です。</p>
	 * @return メタ情報インデックスでマップされた#WAVの値
	 */
	public final Map<Integer, String> getWavs() {
		return mWavs;
	}

	/**
	 * #BMPを取得します。
	 * @param metaIndex メタ情報インデックス
	 * @return #BMPの値。インデックスに該当する値がない場合空文字。
	 */
	public final String getBmp(int metaIndex) {
		return Objects.requireNonNullElse(mBmps.get(BmsInt.box(metaIndex)), "");
	}

	/**
	 * #BMPを全て取得します。
	 * <p>返されるマップは読み取り専用です。</p>
	 * @return メタ情報インデックスでマップされた#BMPの値
	 */
	public final Map<Integer, String> getBmps() {
		return mBmps;
	}

	/**
	 * #BPMを取得します。
	 * @param metaIndex メタ情報インデックス
	 * @return #BPMの値。インデックスに該当する値がない場合{@link BmsSpec#BPM_DEFAULT}。
	 */
	public final double getBpm(int metaIndex) {
		var bpm = (mBpms == null) ? null : mBpms.get(BmsInt.box(metaIndex));
		return (bpm == null) ? BmsSpec.BPM_DEFAULT : bpm;
	}

	/**
	 * #BPMを全て取得します。
	 * <p>返されるマップは読み取り専用です。</p>
	 * @return メタ情報インデックスでマップされた#BPMの値
	 */
	public final Map<Integer, Double> getBpms() {
		return mBpms;
	}

	/**
	 * #STOPを取得します。
	 * @param metaIndex メタ情報インデックス
	 * @return #STOPの値。インデックスに該当する値がない場合0。
	 */
	public final double getStop(int metaIndex) {
		var stop = (mStops == null) ? null : mStops.get(BmsInt.box(metaIndex));
		return (stop == null) ? 0 : stop.doubleValue();
	}

	/**
	 * #STOPを全て取得します。
	 * <p>返されるマップは読み取り専用です。</p>
	 * @return メタ情報インデックスでマップされた#STOPの値
	 */
	public final Map<Integer, Double> getStops() {
		return mStops;
	}

	/**
	 * #SCROLLを取得します。
	 * @param metaIndex メタ情報インデックス
	 * @return #SCROLLの値。インデックスに該当する値がない場合0。
	 */
	public final double getScroll(int metaIndex) {
		var scroll = (mScrolls == null) ? null : mScrolls.get(BmsInt.box(metaIndex));
		return (scroll == null) ? 0 : scroll.doubleValue();
	}

	/**
	 * #SCROLLを全て取得します。
	 * <p>返されるマップは読み取り専用です。</p>
	 * @return メタ情報インデックスでマップされた#SCROLLの値
	 */
	public final Map<Integer, Double> getScrolls() {
		return mScrolls;
	}

	/**
	 * #TEXTを取得します。
	 * @param metaIndex メタ情報インデックス
	 * @return #TEXTの値。インデックスに該当する値がない場合空文字。
	 */
	public final String getText(int metaIndex) {
		return Objects.requireNonNullElse(mTexts.get(BmsInt.box(metaIndex)), "");
	}

	/**
	 * #TEXTを全て取得します。
	 * <p>返されるマップは読み取り専用です。</p>
	 * @return メタ情報インデックスでマップされた#TEXTの値
	 */
	public final Map<Integer, String> getTexts() {
		return mTexts;
	}

	/**
	 * ヘッダ定義オブジェクトを構築します。
	 * <p>当メソッドは{@link #BeMusicHeader(BmsContent)}と同じ機能を提供します。</p>
	 * @param content BMSコンテンツ
	 * @return ヘッダ定義オブジェクト
	 * @exception NullPointerException contentがnull
	 * @since 0.8.0
	 */
	public static BeMusicHeader of(BmsContent content) {
		return new BeMusicHeader(content);
	}

	/**
	 * ヘッダ定義オブジェクトを構築します。
	 * <p>当メソッドは{@link #BeMusicHeader(BmsContent, int)}と同じ機能を提供します。</p>
	 * @param content BMSコンテンツ
	 * @param flags ヘッダ定義収集フラグ
	 * @return ヘッダ定義オブジェクト
	 * @exception NullPointerException contentがnull
	 * @since 0.8.0
	 */
	public static BeMusicHeader of(BmsContent content, int flags) {
		return new BeMusicHeader(content, flags);
	}

	/**
	 * ヘッダ定義オブジェクトを構築します。
	 * <p>当クラスはヘッダ定義収集を全て行うことを除き、{@link #of(BmsContent, int, Supplier)}と同じです。</p>
	 * @param <H> 拡張ヘッダクラス
	 * @param content BMSコンテンツ
	 * @param creator 拡張ヘッダ定義オブジェクト生成関数
	 * @return 拡張ヘッダ定義オブジェクト
	 * @exception NullPointerException contentがnull
	 * @exception NullPointerException creatorがnull
	 * @exception NullPointerException creatorがnullを返した
	 * @see #of(BmsContent, int, Supplier)
	 * @since 0.8.0
	 */
	public static <H extends BeMusicHeader> H of(BmsContent content, Supplier<H> creator) {
		return of(content, ALL, creator);
	}

	/**
	 * ヘッダ定義オブジェクトを構築します。
	 * <p>当メソッドは{@link BeMusicHeader}を継承した拡張ヘッダクラスを構築しセットアップする仕組みを提供します。
	 * creatorによって生成された拡張ヘッダ定義オブジェクトを指定BMSコンテンツでセットアップし、
	 * {@link #onCreate(BmsContent, int)}によって拡張情報を初期化します。</p>
	 * @param <H> 拡張ヘッダクラス
	 * @param content BMSコンテンツ
	 * @param flags ヘッダ定義収集フラグ
	 * @param creator 拡張ヘッダ定義オブジェクト生成関数
	 * @return 拡張ヘッダ定義オブジェクト
	 * @exception NullPointerException contentがnull
	 * @exception NullPointerException creatorがnull
	 * @exception NullPointerException creatorがnullを返した
	 * @see #onCreate(BmsContent, int)
	 * @since 0.8.0
	 */
	public static <H extends BeMusicHeader> H of(BmsContent content, int flags, Supplier<H> creator) {
		assertArgNotNull(content, "content");
		assertArgNotNull(creator, "creator");
		var header = creator.get();
		if (header == null) {
			throw new NullPointerException("Creator returned null header");
		}
		header.setup(content, flags);
		return header;
	}

	/**
	 * オブジェクトのセットアップ。
	 * @param content BMSコンテンツ
	 * @param flags ヘッダ定義収集フラグ
	 */
	final void setup(BmsContent content, int flags) {
		// 取得フラグに関わらず必ず取得するメタ情報
		mBase = BeMusicMeta.getBase(content);
		mPlayer = BeMusicMeta.getPlayer(content);
		mGenre = BeMusicMeta.getGenre(content);
		mTitle = BeMusicMeta.getTitle(content);
		mSubTitle = BeMusicMeta.getSubTitle(content);
		mArtist = BeMusicMeta.getArtist(content);
		mSubArtists = BeMusicMeta.getSubArtists(content);
		mInitialBpm = content.getInitialBpm();
		mDifficulty = BeMusicMeta.getDifficulty(content);
		mChartName = BeMusicMeta.getChartName(content);
		mPlayLevelRaw = BeMusicMeta.getPlayLevelRaw(content);
		mPlayLevel = BeMusicMeta.getPlayLevel(content);
		mRank = BeMusicMeta.getRank(content);
		mDefExRank = content.containsSingleMeta(BeMusicMeta.DEFEXRANK.getName()) ? BeMusicMeta.getDefExRank(content) : null;
		mComment = BeMusicMeta.getComment(content);
		mTotal = BeMusicMeta.getTotal(content);
		mBanner = BeMusicMeta.getBanner(content);
		mStageFile = BeMusicMeta.getStageFile(content);
		mBackBmp = BeMusicMeta.getBackBmp(content);
		mEyecatch = BeMusicMeta.getEyecatch(content);
		mPreview = BeMusicMeta.getPreview(content);
		mLnObjs = BeMusicMeta.getLnObjs(content);
		mLnMode = BeMusicMeta.getLnMode(content);
		mUrl = BeMusicMeta.getUrl(content);
		mEmail = BeMusicMeta.getEmail(content);

		// 取得フラグによって取得有無を決定するメタ情報
		mWavs = collectMetas(flags, WAV, content, BeMusicMeta::getWavs);
		mBmps = collectMetas(flags, BMP, content, BeMusicMeta::getBmps);
		mBpms = collectMetas(flags, BPM, content, BeMusicMeta::getBpms);
		mStops = collectMetas(flags, STOP, content, BeMusicMeta::getStops);
		mScrolls = collectMetas(flags, SCROLL, content, BeMusicMeta::getScrolls);
		mTexts = collectMetas(flags, TEXT, content, BeMusicMeta::getTexts);

		// 拡張情報取得用処理を実行する
		onCreate(content, flags);
	}

	/**
	 * 索引付きメタ情報収集
	 * <p>マップは外部へ直接参照させるため、読み取り専用マップとして生成する。</p>
	 * @param <V> マップの値
	 * @param flags 取得フラグ
	 * @param mask フラグのマスク値
	 * @param content BMSコンテンツ
	 * @param collector メタ情報収集メソッド
	 * @return 読み取り専用のメタ情報マップ。取得フラグのビットが立っていない場合は空マップ。
	 */
	private static <V> Map<Integer, V> collectMetas(int flags, int mask, BmsContent content,
			Function<BmsContent, Map<Integer, V>> collector) {
		return ((flags & mask) == 0) ? Collections.emptyMap() : Collections.unmodifiableMap(collector.apply(content));
	}

	/**
	 * ヘッダ定義オブジェクトが構築された時に実行されます。
	 * <p>当メソッドが実行されるのはオブジェクトのベースクラスである{@link BeMusicHeader}の構築処理が完了した後です。
	 * 従って、クラスのGetterを使用することで抽出済みの情報にアクセス可能な状態となっています。</p>
	 * <p>当メソッドの意図は、ベースクラスを拡張したクラスにおいて自身が必要とする情報を構築する機会を提供する
	 * ことにあります。メソッドはコンストラクタの最後で実行され、当メソッドの実行が完了する時には全ての情報構築が
	 * 完了していることが推奨されています。</p>
	 * @param content BMSコンテンツ
	 * @param flags ヘッダ定義収集フラグ
	 */
	protected void onCreate(BmsContent content, int flags) {
		// Do nothing
	}
}
