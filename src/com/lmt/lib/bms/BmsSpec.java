package com.lmt.lib.bms;

import static com.lmt.lib.bms.BmsAssertion.*;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * BMSの仕様を表します。
 *
 * <p>BMSライブラリで扱うことの出来るBMSの仕様は、利用者がBMSデータの仕様を決定することができるようになっています。
 * このクラスは利用者が決定したBMS仕様を表現し、必要に応じて利用者が参照できます。また、本クラスの最大の目的は
 * {@link BmsContent}の挙動を制御することにあります。</p>
 *
 * <p>BMS仕様が提供する情報には以下のものが含まれています。<br>
 * - メタ情報。BMSの記述のうち&quot;#&quot;または&quot;%&quot;で始まる情報のことであり、{@link BmsMeta}クラスで表現されます。<br>
 * - チャンネル。BMSの記述のうち小節番号＋チャンネル番号＋&quot;:&quot;で始まる情報のことであり、{@link BmsChannel}クラスで表現されます。<br>
 * - BMSデータにおける最小値・最大値・初期値等の情報。これらの情報はBMSライブラリの規定値であり変更できません。<br>
 * - BMSライブラリで使用する標準の文字セット。{@link #setStandardCharset}で変更できます。
 * </p>
 *
 * <p>BMS仕様を生成するには{@link BmsSpecBuilder}を使用します。ビルダーに対してメタ情報、チャンネル等の情報を
 * 構成し、最終的に{@link BmsSpec}オブジェクトを生成して返します。生成されたオブジェクトはBMSコンテンツの制御や
 * BMSデータの読み込み、書き込みなど、BMSデータを扱う様々な状況で必要になります。</p>
 *
 * <p>メタ情報({@link BmsMeta})について<br>
 * メタ情報はBMSコンテンツに関連付けられる付加的な情報のことであり、代表的な情報には楽曲のタイトル、アーティスト、
 * BPM等が挙げられます。メタ情報は「構成単位」と「名称」で分割して管理されます。メタ情報を参照する場合、
 * 構成単位は呼び出すメソッドの種類「{@link #getSingleMeta}」「{@link #getMultipleMeta}」「{@link #getIndexedMeta}」で
 * 決定し、名称はそれぞれのメソッドの引数として指定します。構成単位が異なれば、名称が同じでも構いません。
 * 但し、メタ情報の定義次第では同じ名前で構成単位ごとにデータの型が異なるという状況になる可能性があります。
 * (そのようなBMS仕様にするかは利用者側に委ねられます)</p>
 *
 * <p>チャンネル({@link BmsChannel})について<br>
 * チャンネルはBMSコンテンツにおいて、時間軸を必要とする要素を表す情報のことです。チャンネルで表すことのできる情報は
 * 音の発音タイミング、画像の切り替えタイミング等が存在します。これらの情報は1小節につき最大で{@link #CHINDEX_MAX}+1個
 * まで保有することが出来るようになっています。BMSでは各チャンネルに保有される「チャンネルデータ」を用いて
 * BMSコンテンツを表現していきます。</p>
 *
 * <p>ユーザーチャンネルについて<br>
 * ユーザーチャンネルとは、BMS仕様に規定されたチャンネルのうちチャンネル番号が{@link #USER_CHANNEL_MIN}以上の
 * チャンネルのことを指します。これらのチャンネルはBMSコンテンツを表すチャンネルとしてではなく、アプリケーションの
 * 制御上必要になる一時的な情報を格納するためのチャンネルとして利用することを目的としています。
 * そのため、ユーザーチャンネルは{@link BmsLoader}による外部データからの読み込み対象外、{@link BmsSaver}による
 * 外部データへの書き込み対象外となります。</p>
 *
 * <p>非サポート要素について<br>
 * BMSライブラリでは、一般的に「CONTROL FLOW」と呼ばれる要素をサポートしません。CONTROL FLOWは#RANDOM、#SWITCH
 * およびそれに付随する要素を指します。これらの要素は使用頻度が著しく低く、且つ仕様が複雑で実装が困難であることから
 * サポートを見送っています。</p>
 *
 * @see BmsSpecBuilder [BmsSpecBuilder] BMS仕様を構築する方法についての説明
 * @see BmsMeta [BmsMeta] メタ情報の詳細についての説明
 * @see BmsChannel [BmsChannel] チャンネルの詳細についての説明
 * @see BmsContent [BmsContent] BMS仕様に基づいて生成されたBMSコンテンツの詳細についての説明
 * @see BmsLoader [BmsLoader] BMS仕様に基づいて外部データからBMSコンテンツを読み込む方法についての説明
 * @see BmsSaver [BmsSaver] BMSコンテンツを外部データに書き込む方法についての説明
 */
public final class BmsSpec {
	/** BMS定義で初期BPMが未定義の場合に使用されるデフォルトのBPM */
	public static final double BPM_DEFAULT = 130.0;
	/** BMSライブラリで取り扱い可能な最小のBPMを表します。 */
	public static final double BPM_MIN = 0.00001;
	/** BMSライブラリで取り扱い可能な最大のBPMを表します。 */
	public static final double BPM_MAX = (double)Integer.MAX_VALUE;

	/** 小節長変更チャンネルに設定可能な最小の値を表します。 */
	public static final double LENGTH_MIN = 0.0000000001;
	/** 小節長変更チャンネルに設定可能な最大の値を表します。 */
	public static final double LENGTH_MAX = 170.66145833333333;  // tickがShort#MAX_VALUEを超えない値

	/** 重複可能メタ情報へのアクセスで使用可能な最大のインデックス値を表します。 */
	public static final int MULTIPLE_META_INDEX_MAX = Short.MAX_VALUE;
	/** 索引付きメタ情報へのアクセスで使用可能な最大のインデックス値を表します。 */
	public static final int INDEXED_META_INDEX_MAX = 1295;

	/** BMSライブラリで取り扱い可能な最小の譜面停止時間を表します。 */
	public static final double STOP_MIN = 0.0;
	/** BMSライブラリで取り扱い可能な最大の譜面停止時間を表します。 */
	public static final double STOP_MAX = (double)Integer.MAX_VALUE;

	/**
	 * BMSライブラリにおいて、小節線のチャンネル番号を表します。
	 * <p>このチャンネル番号は、BMS仕様には存在しないものです。BMSライブラリを使用するうえでの処理でのみ
	 * 用いられるチャンネル番号で、唯一最小～最大チャンネル番号の範囲外を示す特別な値です。</p>
	 * <p>楽曲位置の走査を行う場合に、走査対象のチャンネルを検査するテスターに渡される場合があります。
	 * 詳しくは{@link BmsContent#seekNextPoint(int, double, boolean, BmsChannel.Tester, BmsPoint)}を参照してください。</p>
	 */
	public static final int CHANNEL_MEASURE = 0;

	/** BMSライブラリで取り扱い可能な最小のチャンネル番号を表します。 */
	public static final int CHANNEL_MIN = 1;
	/** BMSライブラリで取り扱い可能な最大のチャンネル番号を表します。 */
	public static final int CHANNEL_MAX = Short.MAX_VALUE;

	/** BMS仕様として定義可能な最小のチャンネル番号を表します。 */
	public static final int SPEC_CHANNEL_MIN = CHANNEL_MIN;
	/** BMS仕様として定義可能な最大のチャンネル番号を表します。 */
	public static final int SPEC_CHANNEL_MAX = 1295;

	/** ユーザーチャンネルの最小のチャンネル番号を表します。 */
	public static final int USER_CHANNEL_MIN = SPEC_CHANNEL_MAX + 1;
	/** ユーザーチャンネルの最大のチャンネル番号を表します。 */
	public static final int USER_CHANNEL_MAX = CHANNEL_MAX;

	/** チャンネルインデックスの最小値。 */
	public static final int CHINDEX_MIN = 0;
	/** チャンネルインデックスの最大値。 */
	public static final int CHINDEX_MAX = Short.MAX_VALUE;

	/** 最小の小節番号。 */
	public static final int MEASURE_MIN = 0;
	/** 最大の小節番号。 */
	public static final int MEASURE_MAX = 999;
	/** BMSライブラリでサポートする小節数の上限を表します。 */
	public static final int MEASURE_MAX_COUNT = MEASURE_MAX + 1;

	/** 小節の刻み位置を表す最小の値。 */
	public static final double TICK_MIN = 0.0;
	/** 小節の刻み位置を表す最大の値。 */
	public static final double TICK_MAX = (double)Short.MAX_VALUE;
	/** 小節の刻み数のデフォルト値。 */
	public static final int TICK_COUNT_DEFAULT = 192;

	/** ノートに設定可能な最小の値(16進配列型の場合)を表します。 */
	public static final int VALUE_16_MIN = 1;
	/** ノートに設定可能な最大の値(16進配列型の場合)を表します。 */
	public static final int VALUE_16_MAX = 255;

	/** ノートに設定可能な最小の値(36進配列型の場合)を表します。 */
	public static final int VALUE_MIN = 1;
	/** ノートに設定可能な最大の値(36進配列型の場合)を表します。 */
	public static final int VALUE_MAX = 1295;

	/**
	 * 標準文字セット(初期値はShift-JISで、状況に応じて変更可能)。
	 * Shift-JISにしたいところだが、"～"が化けてしまうためMS932とする。
	 */
	private static Charset sStandardCharset = Charset.forName("MS932");

	/** 単体メタ情報一式 */
	private Map<String, BmsMeta> mSingleMetas;
	/** 重複定義可能メタ情報一式 */
	private Map<String, BmsMeta> mMultipleMetas;
	/** 索引付きメタ情報一式 */
	private Map<String, BmsMeta> mIndexedMetas;
	/** チャンネル一式 */
	private Map<Integer, BmsChannel> mChannels;
	/** 初期BPMメタ情報 */
	private BmsMeta mInitialBpmMeta;
	/** 小節長変更チャンネル(無い場合はnull) */
	private BmsChannel mLengthChannel;
	/** BPM変更チャンネル一覧 */
	private BmsChannel[] mBpmChannels;
	/** 譜面停止チャンネル */
	private BmsChannel[] mStopChannels;

	/**
	 * コンストラクタ
	 * <p>BMS仕様オブジェクトはビルダークラスを通して生成する仕様のため、外部から直接このクラスの
	 * インスタンスを生成することは出来ない。</p>
	 * @param nonIndexedMetas 索引の無いメタ情報一式
	 * @param indexedMetas 索引付きメタ情報一式
	 * @param channels チャンネル一式
	 * @param initialBpmMeta 初期BPMメタ情報
	 * @param lengthChannel 小節長変更チャンネル(無い場合はnull)
	 * @param bpmChannels BPM変更チャンネル一覧
	 * @param stopChannels 譜面停止チャンネル一覧
	 * @see BmsSpecBuilder
	 */
	BmsSpec(Map<String, BmsMeta> singleMetas, Map<String, BmsMeta> multipleMetas,
			Map<String, BmsMeta> indexedMetas, Map<Integer, BmsChannel> channels,
			BmsMeta initialBpmMeta, BmsChannel lengthChannel, BmsChannel[] bpmChannels, BmsChannel[] stopChannels) {
		mSingleMetas = singleMetas;
		mMultipleMetas = multipleMetas;
		mIndexedMetas = indexedMetas;
		mChannels = channels;
		mInitialBpmMeta = initialBpmMeta;
		mLengthChannel = lengthChannel;
		mBpmChannels = bpmChannels;
		mStopChannels = stopChannels;
	}

	/**
	 * 情報単位が{@link BmsUnit#SINGLE SINGLE}のメタ情報を取得します。
	 * @param name メタ情報の名称
	 * @return 名称に該当するメタ情報。存在しない場合はnull。
	 * @exception NullPointerException nameがnull
	 */
	public final BmsMeta getSingleMeta(String name) {
		assertArgNotNull(name, "name");
		return mSingleMetas.get(name);
	}

	/**
	 * 情報単位が{@link BmsUnit#MULTIPLE MULTIPLE}のメタ情報を取得します。
	 * @param name メタ情報の名称
	 * @return 名称に該当するメタ情報。存在しない場合はnull。
	 * @exception NullPointerException nameがnull
	 */
	public final BmsMeta getMultipleMeta(String name) {
		assertArgNotNull(name, "name");
		return mMultipleMetas.get(name);
	}

	/**
	 * 情報単位が{@link BmsUnit#INDEXED INDEXED}のメタ情報を取得します。
	 * @param name メタ情報の名称
	 * @return 名称に該当するメタ情報。存在しない場合はnull。
	 * @exception NullPointerException nameがnull
	 */
	public final BmsMeta getIndexedMeta(String name) {
		assertArgNotNull(name, "name");
		return mIndexedMetas.get(name);
	}

	/**
	 * 指定されたメタ情報キーのメタ情報を取得します。
	 * <p>メタ情報キーの名称・構成単位を使用してメタ情報を検索します。</p>
	 * @param key メタ情報キー
	 * @return メタ情報キーに該当するメタ情報。存在しない場合はnull。
	 * @exception NullPointerException keyがnull
	 */
	public final BmsMeta getMeta(BmsMetaKey key) {
		assertArgNotNull(key, "key");
		return getMeta(key.getName(), key.getUnit());
	}

	/**
	 * 指定された名称・単位のメタ情報を取得します。
	 * @param name メタ情報の名称
	 * @param unit 情報単位
	 * @return 名称・単位に該当するメタ情報。存在しない場合はnull。
	 * @exception NullPointerException nameがnull
	 * @exception NullPointerException unitがnull
	 */
	public final BmsMeta getMeta(String name, BmsUnit unit) {
		assertArgNotNull(name, "name");
		assertArgNotNull(unit, "unit");
		switch (unit) {
		case SINGLE:
			return mSingleMetas.get(name);
		case MULTIPLE:
			return mMultipleMetas.get(name);
		case INDEXED:
			return mIndexedMetas.get(name);
		default:
			throw new IllegalArgumentException("Wrong unit.");
		}
	}

	/**
	 * 指定されたメタ情報キーのメタ情報が存在するかどうかを返します。
	 * <p>メタ情報キーの名称・構成単位を使用してメタ情報の存在チェックを行います。</p>
	 * @param key メタ情報キー
	 * @return メタ情報キーに該当するメタ情報が存在する場合はtrue
	 * @exception NullPointerException keyがnull
	 */
	public final boolean containsMeta(BmsMetaKey key) {
		assertArgNotNull(key, "key");
		return containsMeta(key.getName(), key.getUnit());
	}

	/**
	 * 指定された名称・単位のメタ情報が存在するかどうかを返します。
	 * @param name メタ情報の名称
	 * @param unit 情報単位
	 * @return 名称・単位に該当するメタ情報が存在する場合はtrue
	 * @exception NullPointerException nameがnull
	 * @exception NullPointerException unitがnull
	 */
	public final boolean containsMeta(String name, BmsUnit unit) {
		return getMeta(name, unit) != null;
	}

	/**
	 * BMS仕様として定義されたメタ情報のリストを取得します。
	 * <p>リストに格納されたメタ情報の順番は設定されたソートキーで決定されます。ソートキーの小さいメタ情報が
	 * リストの先頭に格納されます。ソートキーが同一のメタ情報は、BMS仕様に先に登録したものが先に格納されます。</p>
	 * <p>任意型メタ情報はリストには含まれません。</p>
	 * @return メタ情報のリスト
	 */
	public final List<BmsMeta> getMetas() {
		// 任意型以外の全メタ情報を収集する
		return getMetas(m -> !m.isObject(), BmsMeta.COMPARATOR_BY_ORDER);
	}

	/**
	 * メタ情報のリストを取得する。
	 * @param tester メタ情報のテスター
	 * @param sortOrder リストのソート条件
	 * @return メタ情報のリスト
	 */
	final List<BmsMeta> getMetas(Predicate<BmsMeta> tester, Comparator<BmsMeta> sortOrder) {
		var metaCount = mSingleMetas.size() + mMultipleMetas.size() + mIndexedMetas.size();
		var metas = new ArrayList<BmsMeta>(metaCount);
		for (var meta : mSingleMetas.values()) { if (tester.test(meta)) { metas.add(meta); } }
		for (var meta : mMultipleMetas.values()) { if (tester.test(meta)) { metas.add(meta); } }
		for (var meta : mIndexedMetas.values()) { if (tester.test(meta)) { metas.add(meta); } }
		metas.sort(sortOrder);
		return metas;
	}

	/**
	 * 指定されたチャンネル番号のチャンネルを取得します。
	 * @param number チャンネル番号
	 * @return チャンネル番号に該当するチャンネル。存在しない場合はnull。
	 */
	public final BmsChannel getChannel(int number) {
		return mChannels.get(BmsInt.box(number));
	}

	/**
	 * 指定されたチャンネル番号のチャンネルが存在するかを返します。
	 * @param number チャンネル番号
	 * @return チャンネル番号に該当するチャンネルが存在する場合はtrue
	 */
	public final boolean containsChannel(int number) {
		return mChannels.get(BmsInt.box(number)) != null;
	}

	/**
	 * BMS仕様として定義されたチャンネルのリストを取得します。
	 * <p>リストへの格納順は、チャンネル番号の若いチャンネルが先になります。</p>
	 * <p>ユーザーチャンネルはリストには含まれません。</p>
	 * @return チャンネルのリスト
	 */
	public final List<BmsChannel> getChannels() {
		return getChannels(c -> c.isSpec(), (c1, c2) -> Integer.compare(c1.getNumber(), c2.getNumber()));
	}

	/**
	 * 指定条件にマッチするチャンネルのリストを取得する。
	 * @param tester 取得するチャンネルの条件を確認するテスター
	 * @param sortOrder リストのソート条件
	 * @return チャンネルのリスト
	 */
	final List<BmsChannel> getChannels(Predicate<BmsChannel> tester, Comparator<BmsChannel> sortOrder) {
		var channels = new ArrayList<BmsChannel>(mChannels.size());
		mChannels.values().forEach(channel -> { if (tester.test(channel)) { channels.add(channel); } });
		channels.sort(sortOrder);
		return channels;
	}

	/**
	 * 初期BPMメタ情報を取得します。
	 * @return 初期BPMメタ情報
	 */
	public final BmsMeta getInitialBpmMeta() {
		return mInitialBpmMeta;
	}

	/**
	 * 小節長変更チャンネルを取得します。
	 * <p>BMS仕様として小節長変更が未定義の場合、このメソッドはnullを返します。</p>
	 * @return 小節長変更チャンネル
	 */
	public final BmsChannel getLengthChannel() {
		return mLengthChannel;
	}

	/**
	 * チャンネルが小節長変更チャンネルか判定
	 * @param channel チャンネル
	 * @return チャンネルが小節長変更チャンネルならtrue
	 */
	final boolean isLengthChannel(BmsChannel channel) {
		return (mLengthChannel != null) && (mLengthChannel == channel);
	}

	/**
	 * BPM変更チャンネルを取得します。
	 * <p>BMS仕様としてBPM変更チャンネルが未定義の場合、このメソッドはnullを返します。</p>
	 * @param number チャンネル番号
	 * @return BPM変更チャンネル。指定したチャンネル番号に該当するBPM変更チャンネルが存在しない場合null
	 */
	public final BmsChannel getBpmChannel(int number) {
		return getBpmChannel(number, false);
	}

	/**
	 * BPM変更チャンネルを取得します。
	 * <p>当メソッドでは、チャンネル番号またはインデックス値を使用してBPM変更チャンネルを取得できます。
	 * インデックス値に使用可能な最大値は{@link #getBpmChannelCount()}-1です。</p>
	 * <p>インデックス値の指定可能範囲を超過した値を指定しても、例外をスローせずに戻り値でnullを返します。</p>
	 * @param number チャンネル番号またはインデックス値
	 * @param byIndex falseの場合チャンネル番号で、trueの場合インデックス値でチャンネルを取得します。
	 * @return BPM変更チャンネル。指定した値に該当するBPM変更チャンネルが存在しない場合null
	 */
	public final BmsChannel getBpmChannel(int number, boolean byIndex) {
		if (byIndex) {
			// インデックスによる検索
			return ((number < 0) || (number >= mBpmChannels.length)) ? null : mBpmChannels[number];
		} else {
			// チャンネル番号による検索
			for (var i = 0; i < mBpmChannels.length; i++) {
				if (mBpmChannels[i].getNumber() == number) { return mBpmChannels[i]; }
			}
			return null;
		}
	}

	/**
	 * BPM変更チャンネルの数を取得します。
	 * @return BPM変更チャンネルの数
	 */
	public final int getBpmChannelCount() {
		return mBpmChannels.length;
	}

	/**
	 * BPM変更チャンネルが存在するかを取得します。
	 * @return BPM変更チャンネルが存在する場合true
	 */
	public final boolean hasBpmChannel() {
		return (mBpmChannels.length > 0);
	}

	/**
	 * 譜面停止チャンネルを取得します。
	 * <p>BMS仕様として譜面停止チャンネルが未定義の場合、このメソッドはnullを返します。</p>
	 * @param number チャンネル番号
	 * @return 譜面停止チャンネル。指定したチャンネル番号に該当する譜面停止チャンネルが存在しない場合null
	 */
	public final BmsChannel getStopChannel(int number) {
		return getStopChannel(number, false);
	}

	/**
	 * 譜面停止チャンネルを取得します。
	 * <p>当メソッドでは、チャンネル番号またはインデックス値を使用して譜面停止チャンネルを取得できます。
	 * インデックス値に使用可能な最大値は{@link #getBpmChannelCount()}-1です。</p>
	 * <p>インデックス値の指定可能範囲を超過した値を指定しても、例外をスローせずに戻り値でnullを返します。</p>
	 * @param number チャンネル番号またはインデックス値
	 * @param byIndex falseの場合チャンネル番号で、trueの場合インデックス値でチャンネルを取得します。
	 * @return 譜面停止チャンネル。指定した値に該当する譜面停止チャンネルが存在しない場合null
	 */
	public final BmsChannel getStopChannel(int number, boolean byIndex) {
		if (byIndex) {
			// インデックスによる検索
			return ((number < 0) || (number >= mStopChannels.length)) ? null : mStopChannels[number];
		} else {
			// チャンネル番号による検索
			for (var i = 0; i < mStopChannels.length; i++) {
				if (mStopChannels[i].getNumber() == number) { return mStopChannels[i]; }
			}
			return null;
		}
	}

	/**
	 * 譜面停止チャンネルの数を取得します。
	 * @return 譜面停止チャンネルの数
	 */
	public final int getStopChannelCount() {
		return mStopChannels.length;
	}

	/**
	 * 譜面停止チャンネルが存在するかを取得します。
	 * @return 譜面停止チャンネルが存在する場合true
	 */
	public final boolean hasStopChannel() {
		return (mStopChannels.length > 0);
	}

	/**
	 * BMS仕様の内容からハッシュ値を計算し、結果を返します。
	 * <p>ハッシュ値の計算に使用する情報ソースは、全ての任意型({@link BmsType#OBJECT})ではないメタ情報、および
	 * 全ての仕様チャンネルです。小節長変更・BPM変更・譜面停止チャンネルの設定や、メタ情報の登録順もハッシュ値を
	 * 計算する要素となります。</p>
	 * <p>上記のように、BMS仕様の作成手順、作成内容の全ての要素がハッシュ値を決定する要素となることから、BMS仕様を生成する
	 * データ内容が少しでも変更された場合、別のハッシュ値が返却されることになります。そのため、当メソッドで返却されるハッシュ値は
	 * データフォーマットを解析する処理が変更されていないことを確認するためのセキュリティ対策として用いることを想定しています。</p>
	 * <p>唯一、任意型メタ情報・ユーザーチャンネルについてはどのような内容をどのような順番で登録したとしてもハッシュ値には
	 * 影響しません。これは、BMS仕様が変更されなくてもアプリケーションが更新される場合(※)があることを考慮するためです。
	 * (例えばアプリケーションのバグフィックスなどが挙げられます)</p>
	 * <p>※任意型メタ情報、ユーザーチャンネルはアプリケーション向けに割り当てられる情報のため、アプリケーション独自の都合で
	 * 追加・変更・削除される可能性が高い。</p>
	 * @return BMS仕様から算出されたハッシュ値
	 */
	public final byte[] generateHash() {
		// ハッシュ値計算用データ
		var data = new StringBuilder(2048);

		// メタ情報を計算する
		data.append("META");
		var metas = getMetas();
		var metaCount = metas.size();
		for (int i = 0; i < metaCount; i++) {
			appendMetaToHashData(i, metas.get(i), data);
		}

		// チャンネルを計算する
		data.append("CHANNEL");
		var channels = getChannels();
		var channelCount = channels.size();
		for (int i = 0; i < channelCount; i++) {
			appendChannelToHashData(i, channels.get(i), data);
		}

		// 小節長変更チャンネルを計算する
		data.append("LENGTH");
		appendChannelToHashData(0, getLengthChannel(), data);

		// BPM変更チャンネルを計算する
		data.append("BPM");
		channelCount = mBpmChannels.length;
		for (var i = 0; i < channelCount; i++) {
			appendChannelToHashData(i, mBpmChannels[i], data);
		}

		// 譜面停止チャンネルを計算する
		data.append("STOP");
		channelCount = mStopChannels.length;
		for (var i = 0; i < channelCount; i++) {
			appendChannelToHashData(i, mStopChannels[i], data);
		}

		// 生成したハッシュ値計算用データはUTF-16からUTF-8へ変換し、それをハッシュ生成用のインプットとする
		try {
			// ハッシュ値を生成する
			var hashInput = data.toString().getBytes(StandardCharsets.UTF_8);
			var msgDigest = MessageDigest.getInstance("SHA-1");
			return msgDigest.digest(hashInput);
		} catch (NoSuchAlgorithmException e) {
			// 想定しない
			return new byte[0];
		}
	}

	/**
	 * BMSライブラリが扱う標準文字セットを設定します。
	 * <p>BMSライブラリでは、デフォルトの文字セットをShift-JISとして設定しています。これはBMSライブラリが主に
	 * 日本で使用されることを想定しているためであり、日本国外向けのアプリケーションを開発する場合はこのメソッドを
	 * 呼び出して文字セットを変更してください。</p>
	 * <p>この設定が使用されるのはBMSを読み込む時です。BMSライブラリがサポートする「BMS宣言」に文字セットの
	 * 明示的な指定がない場合には、このメソッドで設定した文字セットを使用してBMS定義のデコードを試みます。
	 * 読み込もうとするBMSをエンコードした文字セットと標準文字セットが一致しない場合、文字化けが発生します。</p>
	 * @param cs 標準に設定する文字セット
	 * @exception NullPointerException csがnull
	 */
	public static void setStandardCharset(Charset cs) {
		assertArgNotNull(cs, "cs");
		sStandardCharset = cs;
	}

	/**
	 * BMSライブラリが扱う標準文字セットを取得します。
	 * @return 標準文字セット
	 */
	public static Charset getStandardCharset() {
		return sStandardCharset;
	}

	/**
	 * 小節長の下限値超過判定
	 * @param length 小節長
	 * @return 下限値を超過していた場合true
	 */
	static boolean isLengthUnderflow(double length) {
		return length < LENGTH_MIN;
	}

	/**
	 * 小節長上限値超過判定
	 * @param length 小節長
	 * @return 上限値を超過していた場合true
	 */
	static boolean isLengthOverflow(double length) {
		return length > LENGTH_MAX;
	}

	/**
	 * 小節長の範囲内判定
	 * @param length 小節長
	 * @return 範囲内の場合true
	 */
	static boolean isLengthWithinRange(double length) {
		return !isLengthUnderflow(length) && !isLengthOverflow(length);
	}

	/**
	 * BPMの下限値超過判定
	 * @param bpm BPM
	 * @return 下限値を超過していた場合true
	 */
	static boolean isBpmUnderflow(double bpm) {
		return bpm < BPM_MIN;
	}

	/**
	 * BPMの上限値超過判定
	 * @param bpm BPM
	 * @return 上限値を超過していた場合true
	 */
	static boolean isBpmOverflow(double bpm) {
		return bpm > BPM_MAX;
	}

	/**
	 * BPMの範囲内判定
	 * @param bpm BPM
	 * @return 範囲内の場合true
	 */
	static boolean isBpmWithinRange(double bpm) {
		return !isBpmUnderflow(bpm) && !isBpmOverflow(bpm);
	}

	/**
	 * 譜面停止刻み数の下限値超過判定
	 * @param stop 譜面停止刻み数
	 * @return 下限値を超過していた場合true
	 */
	static boolean isStopUnderflow(double stop) {
		return stop < STOP_MIN;
	}

	/**
	 * 譜面停止刻み数の上限値超過判定
	 * @param stop 譜面停止刻み数
	 * @return 上限値を超過していた場合true
	 */
	static boolean isStopOverflow(double stop) {
		return stop > STOP_MAX;
	}

	/**
	 * 譜面停止刻み数の範囲内判定
	 * @param stop 譜面停止刻み数
	 * @return 範囲内の場合true
	 */
	static boolean isStopWithinRange(double stop) {
		return !isStopUnderflow(stop) && !isStopOverflow(stop);
	}

	/**
	 * チャンネル番号の下限値超過判定
	 * @param channel チャンネル番号
	 * @return 下限値を超過していた場合true
	 */
	static boolean isChannelUnderflow(int channel) {
		return channel < CHANNEL_MIN;
	}

	/**
	 * チャンネル番号の上限値判定
	 * @param channel チャンネル番号
	 * @return 上限値を超過していた場合true
	 */
	static boolean isChannelOverflow(int channel) {
		return channel > CHANNEL_MAX;
	}

	/**
	 * チャンネル番号の範囲内判定
	 * @param channel チャンネル番号
	 * @return 範囲内の場合true
	 */
	static boolean isChannelWithinRange(int channel) {
		return !isChannelUnderflow(channel) && !isChannelOverflow(channel);
	}

	/**
	 * 仕様チャンネル番号の下限値判定
	 * @param channel チャンネル番号
	 * @return 下限値を超過していた場合true
	 */
	static boolean isSpecChannelUnderflow(int channel) {
		return channel < SPEC_CHANNEL_MIN;
	}

	/**
	 * 仕様チャンネル番号の上限値判定
	 * @param channel チャンネル番号
	 * @return 上限値を超過していた場合true
	 */
	static boolean isSpecChannelOverflow(int channel) {
		return channel > SPEC_CHANNEL_MAX;
	}

	/**
	 * 仕様チャンネル番号の範囲内判定
	 * @param channel チャンネル番号
	 * @return 範囲内の場合true
	 */
	static boolean isSpecChannelWithinRange(int channel) {
		return !isSpecChannelUnderflow(channel) && !isSpecChannelOverflow(channel);
	}

	/**
	 * ユーザーチャンネル番号の下限値判定
	 * @param channel チャンネル番号
	 * @return 下限値を超過していた場合true
	 */
	static boolean isUserChannelUnderflow(int channel) {
		return channel < USER_CHANNEL_MIN;
	}

	/**
	 * ユーザーチャンネル番号の上限値判定
	 * @param channel チャンネル番号
	 * @return 上限値を超過していた場合true
	 */
	static boolean isUserChannelOverflow(int channel) {
		return channel > USER_CHANNEL_MAX;
	}

	/**
	 * ユーザーチャンネル番号の範囲内判定
	 * @param channel チャンネル番号
	 * @return 範囲内の場合true
	 */
	static boolean isUserChannelWithinRange(int channel) {
		return !isUserChannelUnderflow(channel) && !isUserChannelOverflow(channel);
	}

	/**
	 * チャンネルインデックスの下限値超過判定
	 * @param index チャンネルインデックス
	 * @return 下限値を超過していた場合true
	 */
	static boolean isChIndexUnderflow(int index) {
		return index < CHINDEX_MIN;
	}

	/**
	 * チャンネルインデックスの上限値超過判定
	 * @param index チャンネルインデックス
	 * @return 上限値を超過していた場合true
	 */
	static boolean isChIndexOverflow(int index) {
		return index > CHINDEX_MAX;
	}

	/**
	 * チャンネルインデックスの範囲内判定
	 * @param index チャンネルインデックス
	 * @return 範囲内の場合true
	 */
	static boolean isChIndexWithinRange(int index) {
		return !isChIndexUnderflow(index) && !isChIndexOverflow(index);
	}

	/**
	 * チャンネルインデックスの範囲内判定
	 * @param index チャンネルインデックス
	 * @param isMultiple 複数データ許可
	 * @return 範囲内の場合true
	 */
	static boolean isChIndexWithinRange(int index, boolean isMultiple) {
		return isMultiple ? (!isChIndexUnderflow(index) && !isChIndexOverflow(index)) : (index == BmsSpec.CHINDEX_MIN);
	}

	/**
	 * 小節番号の下限値超過判定
	 * @param measure 小節番号
	 * @return 下限値を超過していた場合true
	 */
	static boolean isMeasureUnderflow(int measure) {
		return measure < MEASURE_MIN;
	}

	/**
	 * 小節番号の上限値超過判定
	 * @param measure 小節番号
	 * @return 上限値を超過していた場合true
	 */
	static boolean isMeasureOverflow(int measure) {
		return measure > MEASURE_MAX;
	}

	/**
	 * 小節番号の範囲内判定
	 * @param measure 小節番号
	 * @return 範囲内の場合true
	 */
	static boolean isMeasureWithinRange(int measure) {
		return !isMeasureUnderflow(measure) && !isMeasureOverflow(measure);
	}

	/**
	 * 小節番号の範囲内判定
	 * @param measure 小節番号
	 * @param measureMax 小節番号最大値
	 * @return 範囲内の場合true
	 */
	static boolean isMeasureWithinRange(int measure, int measureMax) {
		return !isMeasureUnderflow(measure) && (measure <= measureMax);
	}

	/**
	 * 刻み位置の下限値超過判定
	 * @param tick 刻み位置
	 * @return 下限値を超過していた場合true
	 */
	static boolean isTickUnderflow(double tick) {
		return tick < TICK_MIN;
	}

	/**
	 * 刻み位置の上限値超過判定
	 * @param tick 刻み位置
	 * @return 上限値を超過していた場合true
	 */
	static boolean isTickOverflow(double tick) {
		return tick > TICK_MAX;
	}

	/**
	 * 刻み位置の範囲内判定
	 * @param tick 刻み位置
	 * @return 範囲内の場合true
	 */
	static boolean isTickWithinRange(double tick) {
		return !isTickUnderflow(tick) && !isTickOverflow(tick);
	}

	/**
	 * 刻み位置の範囲内判定
	 * @param tick 刻み位置
	 * @param tickMax 刻み位置最大値
	 * @return 範囲内の場合true
	 */
	static boolean isTickWithinRange(double tick, double tickMax) {
		return !isTickUnderflow(tick) && (tick <= tickMax);
	}

	/**
	 * 小節の刻み数計算
	 * <p>小節における小節の刻み数は小節長に依存する。小節長は0以下または{@link #LENGTH_MAX}を超える値を
	 * 指定してはならない。この範囲外の値を指定した場合の計算結果は未定義となる。</p>
	 * <p>正規化とは、正式な刻み数の値に丸め込みを行うことを示す。正規化すると小数点以下は切り捨てられ、
	 * 1未満の刻み数は1になる。その結果、返される刻み数は1～{@link #TICK_MAX}の整数値となる。</p>
	 * <p>正規化を行わない場合、前述の丸め込みが行われない無段階の刻み数が返される。</p>
	 * @param length 小節長
	 * @param normalize 計算結果の正規化有無
	 * @return 小節の刻み数
	 */
	static double computeTickCount(double length, boolean normalize) {
		var tickCount = (double)TICK_COUNT_DEFAULT * length;
		return normalize ? Math.floor(Math.max(1.0, tickCount)) : tickCount;
	}

	/**
	 * ハッシュ値計算用データにメタ情報を追記する。
	 * @param index 0から開始のインデックス値
	 * @param meta 追記するメタ情報
	 * @param out 追記先StringBuilder
	 */
	private static void appendMetaToHashData(int index, BmsMeta meta, StringBuilder out) {
		String tmp;

		// メタ情報の名称：文字列をそのまま用いる
		tmp = meta.getName();
		out.append(tmp);

		// 構成単位：列挙型の名称文字列を用いる
		tmp = meta.getUnit().name();
		out.append(tmp);

		// データ型：データ型の名称文字列を用いる
		tmp = meta.getType().getName();
		out.append(tmp);

		// 初期値：メタ情報生成時に指定された文字列表現を用いる
		tmp = meta.getDefaultValueString();
		out.append(tmp);

		// 並び順：インデックス値を文字列に変換した値を用いる
		tmp = Integer.toString(index);
		out.append(tmp);

		// 同一性チェック有無："0"または"1"
		tmp = meta.isUniqueness() ? "1" : "0";
		out.append(tmp);
	}

	/**
	 * ハッシュ値計算用データにチャンネルを追記する。
	 * @param index 0から開始のインデックス値
	 * @param channel 追記するチャンネル
	 * @param out 追記先StringBuilder
	 */
	private static void appendChannelToHashData(int index, BmsChannel channel, StringBuilder out) {
		if (channel == null) {
			return;
		}

		String tmp;

		// インデックス値：整数値をそのまま文字列に変換したものを使用する
		tmp = Integer.toString(index);
		out.append(tmp);

		// チャンネル番号：整数値をそのまま文字列に変換したものを使用する
		tmp = Integer.toString(channel.getNumber());
		out.append(tmp);

		// データ型：データ型の名称文字列を用いる
		tmp = channel.getType().getName();
		out.append(tmp);

		// 参照先メタ情報名称："REF" + 名称文字列 を使用する
		tmp = "REF";
		out.append(tmp);
		tmp = channel.getRef();
		if (tmp != null) {
			out.append(tmp);
		}

		// 初期値：チャンネル生成時に指定された文字列表現を使用する
		tmp = channel.getDefaultValueString();
		out.append(tmp);

		// 複数データ保有可否："0"または"1"
		tmp = channel.isMultiple() ? "1" : "0";
		out.append(tmp);

		// 同一性チェック有無："0"または"1"
		tmp = channel.isUniqueness() ? "1" : "0";
		out.append(tmp);
	}
}
