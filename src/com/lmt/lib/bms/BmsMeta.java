package com.lmt.lib.bms;

import static com.lmt.lib.bms.BmsAssertion.*;

import java.util.Comparator;
import java.util.regex.Pattern;

/**
 * BMSコンテンツの持つメタ情報の属性を表します。
 *
 * <p>BMSライブラリにおけるメタ情報とは、BMSコンテンツのうち当該コンテンツの内容・属性・状態等を表す様々な情報を指します。
 * BMSにおけるメタ情報の一般的な呼称は「HEADER」ですが、BMSライブラリではこれを「メタ情報」と呼称します。</p>
 *
 * <p>メタ情報は「構成単位」「名称」の2つの情報を用いてデータを管理します。以下にそれぞれの情報について記載します。</p>
 *
 * <p><b>構成単位</b>({@link BmsUnit})<br>
 * BMSコンテンツ内で複数のメタ情報を管理するにあたり、管理構造は以下に分類されます。<br>
 * - 単体メタ情報<br>
 * 1つのメタ情報に対して1個のデータを割り当てる構造です。多くの情報はこの構成単位に分類されます。<br>
 * - 複数メタ情報<br>
 * 1つのメタ情報に対して複数のデータを割り当てることが出来る構造です。当該メタ情報として登録したい情報が
 * 複数あるような情報に対して適用することを想定しています。<br>
 * - 索引付きメタ情報<br>
 * 1つのメタ情報に対して複数のデータを割り当て、そのデータに00～ZZの索引を割り当てて管理する構造です。
 * チャンネルからメタ情報を参照するようなケースを想定しています。BMSデータとして索引付きメタ情報を記述する際は
 * メタ情報の名称の語尾に00～ZZの索引を2文字で記述し、その後ろに情報内容を記述するようにします。<br>
 * 例：#WAV8J aaa.wav</p>
 *
 * <p><b>名称</b><br>
 * 全てのメタ情報に付与される名前です。名前は&quot;#&quot;または&quot;%&quot;で始まり、1文字目が半角英字、
 * 2文字目以降が半角英数字または&quot;_&quot;, &quot;/&quot;のいずれかで指定しなければなりません。また、
 * メタ情報を定義する際の英字は必ず小文字とし、名称全体が64文字を超えてはなりません。BMSデータにメタ情報を
 * 記述する際は大文字の英字を使用しても構いません。<br>
 * 名称は同じ構成単位内で同じものを指定してはなりません。構成単位が異なれば同じ名称を使用できますが、
 * BMS仕様の分かりやすさの観点では推奨されません。</p>
 *
 * <p>メタ情報を構成する情報には「情報のデータ型({@link BmsType})」「データが定義されなかった場合の初期値」
 * 「BMSコンテンツを外部データへ出力する際の並び順を決定するソートキー」「同一性チェック対象かどうか」が
 * 存在します。それぞれの情報の詳細については以下の記述を参照してください。</p>
 *
 * <p><b>データ型</b>({@link BmsType})<br>
 * メタ情報のデータを記述する際の型を決定します。<br>
 * 詳細は{@link BmsType}を参照してください。</p>
 *
 * <p><b>初期値</b><br>
 * BMSの仕様として、全てのメタ情報は指定必須とすることが出来ません。そのため、メタ情報には必ず初期値を
 * 定義しなければなりません。初期値の記述は文字列のみですが記述の書式はデータ型の指定内容に依存します。</p>
 *
 * <p><b>ソートキー</b><br>
 * BMSコンテンツを外部データに出力する際、複数あるメタ情報をどういった順番で出力するかを決定するための数値です。
 * この数値がより小さいメタ情報が先に出力されるようになります。同じ数値を指定したメタ情報同士では、
 * {@link BmsSpec}に対して先に定義したほうが先に出力されます。</p>
 *
 * <p><b>同一性チェック</b><br>
 * BMSライブラリにおいて「同一性」とは、「プレイヤーがプレーするBMSコンテンツの譜面が同一の内容を示している
 * 状態のこと」を指します。同一性チェックは、譜面が同一の内容を示しているかどうかを確認するために使用される
 * データであるかどうかを決定します。<br>
 * 同一性チェックをONにしたメタ情報が、必ずしも譜面に影響を与えるかどうかはBMSライブラリでは関知しません。
 * 例えば楽曲名などはプレーする譜面には影響を与えませんが、BMSコンテンツのタイトルが変更されてしまう
 * ことを許容しない仕様とし、同一性チェックONを推奨しています。<br>
 * このように、どのような情報に同一性チェックを入れるかはBMS仕様の提唱者が厳密に検討し決定するべきです。
 * メタ情報が変更されることでプレーする譜面が変わってしまうものは原則として同一性チェックをONにするべきです。
 * そうしないと、譜面を比較した際、実際には譜面に変更があっても同一の譜面であると判定されてしまうことになります。</p>
 *
 * <p><b>任意型メタ情報</b><br>
 * 任意型メタ情報は、データ型を任意型({@link BmsType#OBJECT})にすることで作成することが可能なメタ情報です。
 * 任意型メタ情報はアプリケーションの動作上必要な情報を格納する用途に利用することが出来ます。通常のメタ情報とは
 * 下記の点が異なります。</p>
 *
 * <ul>
 * <li>初期値はnullでなければいけません。</li>
 * <li>同一性チェックをONにすることは出来ません。</li>
 * <li>{@link BmsSpec#getMetas}において、リストの格納対象外になります。</li>
 * <li>{@link BmsLoader}, {@link BmsSaver}の入出力対象外になります。</li>
 * </ul>
 *
 * @see BmsUnit
 * @see BmsType
 */
public final class BmsMeta extends BmsMetaKey {
	/** メタ情報の名称に使用可能な文字列の正規表現パターンです。 */
	public static final Pattern NAME_PATTERN = Pattern.compile("^(#|%)[a-z][a-z0-9_/]{0,62}$");

	/** 並び替え順序を示すコンパレータ */
	static final Comparator<BmsMeta> COMPARATOR_BY_ORDER = (m1, m2) -> {
		var o1 = Integer.compare(m1.getOrder(), m2.getOrder());
		return (o1 != 0) ? o1 : Integer.compare(m1.getOrder2(), m2.getOrder2());
	};

	/** メタ情報のデータ型 */
	private BmsType mType;
	/** 初期値(文字列表現) */
	private String mDefaultValueStr;
	/** 初期値(nullは許容しない) */
	private Object mDefaultValue;
	/** ソートキー1(外部から制御可能なキー) */
	private int mOrder;
	/** ソートキー2(キー1が同じ場合に、定義順の並びを実現するためのキー) */
	private int mOrder2;
	/** データ同一性をチェックする際に使用すべきメタ情報であるかどうか */
	private boolean mUniqueness;
	/** 初期BPMメタ情報かどうか */
	private boolean mIsInitialBpm;
	/** 任意型メタ情報かどうか */
	private boolean mIsObject;
	/** BPM変更チャンネルの参照先になっているかどうか */
	private boolean mIsReferenceBpm = false;
	/** 譜面停止チャンネルの参照先になっているかどうか */
	private boolean mIsReferenceStop = false;

	/**
	 * 単一メタ情報を生成します。
	 * @param name メタ情報の名称
	 * @param type データ型
	 * @param defaultValue 初期値の文字列表現
	 * @param order ソートキー
	 * @param uniqueness 同一性チェック時に使用されるべきメタ情報かどうか
	 * @return 単一メタ情報オブジェクト
	 * @exception NullPointerException nameがnull
	 * @exception NullPointerException typeがnull
	 * @exception NullPointerException defaultValueがnull
	 * @exception IllegalArgumentException nameが{@link #NAME_PATTERN}にマッチしない
	 * @exception IllegalArgumentException defaultValueがtypeの書式に適合しない
	 */
	public static BmsMeta single(String name, BmsType type, String defaultValue, int order, boolean uniqueness) {
		return new BmsMeta(name, BmsUnit.SINGLE, type, defaultValue, order, uniqueness);
	}

	/**
	 * 複数メタ情報を生成します。
	 * @param name メタ情報の名称
	 * @param type データ型
	 * @param defaultValue 初期値の文字列表現
	 * @param order ソートキー
	 * @param uniqueness 同一性チェック時に使用されるべきメタ情報かどうか
	 * @return 複数メタ情報オブジェクト
	 * @exception NullPointerException nameがnull
	 * @exception NullPointerException typeがnull
	 * @exception NullPointerException defaultValueがnull
	 * @exception IllegalArgumentException nameが{@link #NAME_PATTERN}にマッチしない
	 * @exception IllegalArgumentException defaultValueがtypeの書式に適合しない
	 */
	public static BmsMeta multiple(String name, BmsType type, String defaultValue, int order, boolean uniqueness) {
		return new BmsMeta(name, BmsUnit.MULTIPLE, type, defaultValue, order, uniqueness);
	}

	/**
	 * 索引付きメタ情報を生成します。
	 * @param name メタ情報の名称
	 * @param type データ型
	 * @param defaultValue 初期値の文字列表現
	 * @param order ソートキー
	 * @param uniqueness 同一性チェック時に使用されるべきメタ情報かどうか
	 * @return 索引付きメタ情報オブジェクト
	 * @exception NullPointerException nameがnull
	 * @exception NullPointerException typeがnull
	 * @exception NullPointerException defaultValueがnull
	 * @exception IllegalArgumentException nameが{@link #NAME_PATTERN}にマッチしない
	 * @exception IllegalArgumentException defaultValueがtypeの書式に適合しない
	 */
	public static BmsMeta indexed(String name, BmsType type, String defaultValue, int order, boolean uniqueness) {
		return new BmsMeta(name, BmsUnit.INDEXED, type, defaultValue, order, uniqueness);
	}

	/**
	 * 任意型メタ情報を生成します。
	 * @param name メタ情報の名称
	 * @param unit データ単位
	 * @return 任意型メタ情報オブジェクト
	 * @exception NullPointerException nameがnull
	 * @exception NullPointerException unitがnull
	 * @exception IllegalArgumentException nameが{@link #NAME_PATTERN}にマッチしない
	 */
	public static BmsMeta object(String name, BmsUnit unit) {
		return new BmsMeta(name, unit, BmsType.OBJECT, null, 0, false);
	}

	/**
	 * 新しいメタ情報オブジェクトを生成します。
	 * @param name メタ情報の名称
	 * @param unit データ単位
	 * @param type データ型
	 * @param defaultValue 初期値の文字列表現
	 * @param order ソートキー
	 * @param uniqueness 同一性チェック時に使用されるべきメタ情報かどうか
	 * @exception NullPointerException nameがnull
	 * @exception NullPointerException unitがnull
	 * @exception NullPointerException typeがnull
	 * @exception NullPointerException typeがOBJECT以外の時、defaultValueがnull
	 * @exception IllegalArgumentException typeがOBJECTの時、defaultValueがnullではない
	 * @exception IllegalArgumentException nameが{@link #NAME_PATTERN}にマッチしない
	 * @exception IllegalArgumentException defaultValueがtypeの書式に適合しない
	 * @exception IllegalArgumentException typeがOBJECTの時、uniquenessがtrue
	 */
	public BmsMeta(String name, BmsUnit unit, BmsType type, String defaultValue, int order, boolean uniqueness) {
		super(name, unit);
		// アサーション
		assertArgNotNull(type, "type");
		assertArg(NAME_PATTERN.matcher(name).matches(), "Wrong name: '%s'", name);
		var isObject = BmsType.OBJECT.equals(type);
		if (isObject) {
			assertArg(defaultValue == null, "When type is OBJECT, 'defaultValue' is allowed null only.");
			assertArg(uniqueness == false, "When type is OBJECT, 'uniqueness' is allowed false only.");
		} else {
			assertArgNotNull(defaultValue, "defaultValue");
			assertArg(type.test(defaultValue), "'defaultValue' does NOT passed %s test. value=%s", type, defaultValue);
		}

		// データを初期化する
		mType = type;
		mDefaultValueStr = defaultValue;
		mDefaultValue = isObject ? null : BmsType.cast(defaultValue, type);
		mOrder = order;
		mOrder2 = 0;
		mUniqueness = uniqueness;
		mIsObject = isObject;
	}

	/**
	 * メタ情報の名称、構成単位、データ型が分かる形式の文字列を返します。
	 * @return メタ情報の名称、構成単位、データ型が分かる形式の文字列
	 */
	@Override
	public String toString() {
		return String.format("{%s:%s as %s}", getName(), getUnit(), mType);
	}

	/**
	 * メタ情報のデータ型を取得します。
	 * @return メタ情報のデータ型
	 */
	public BmsType getType() {
		return mType;
	}

	/**
	 * 初期値の文字列表現を取得する。
	 * @return 初期値の文字列表現
	 */
	String getDefaultValueString() {
		return mDefaultValueStr;
	}

	/**
	 * BMSにメタ情報が定義されていない場合の初期値を取得します。
	 * <p>取得した値のデータ型は{@link #getType}で取得できるデータ型のネイティブデータ型と一致します。
	 * また、初期値の定義は必須のため、この値がnullになることはありません。</p>
	 * @return メタ情報の初期値
	 */
	public Object getDefaultValue() {
		return mDefaultValue;
	}

	/**
	 * ソートキーを取得します。
	 * <p>ソートキーは、BMSライブラリからBMS出力を行った際にメタ情報を出力する順番を決定するのに使用されます。
	 * 設定する値は任意で良く、同一BMS仕様の複数のメタ情報で同じソートキーが存在する場合は先に定義された
	 * メタ情報が先に出力されるようになります。</p>
	 * @return ソートキー
	 */
	public int getOrder() {
		return mOrder;
	}

	/**
	 * ソートキー2を設定する
	 * @param order ソートキー2
	 */
	void setOrder2(int order) {
		mOrder2 = order;
	}

	/**
	 * ソートキー2を取得する
	 * @return ソートキー2
	 */
	int getOrder2() {
		return mOrder2;
	}

	/**
	 * 同一性チェック時に参照されるべきメタ情報かどうかを取得します。
	 * @return 同一性チェック時に参照されるべき場合はtrue
	 */
	public boolean isUniqueness() {
		return mUniqueness;
	}

	/**
	 * 任意型メタ情報であるかどうかを取得します。
	 * @return 任意型メタ情報である場合はtrue
	 */
	public boolean isObject() {
		return mIsObject;
	}

	/**
	 * メタ情報を初期BPMメタ情報とする
	 */
	final void setIsInitialBpm() {
		mIsInitialBpm = true;
	}

	/**
	 * 初期BPMメタ情報かどうか判定する
	 * @return 初期BPMメタ情報ならtrue
	 */
	final boolean isInitialBpm() {
		return mIsInitialBpm;
	}

	/**
	 * BPM変更チャンネルの参照先設定
	 */
	final void setIsReferenceBpm() {
		mIsReferenceBpm = true;
	}

	/**
	 * BPM変更チャンネルの参照先になっているかどうか判定
	 * @return BPM変更チャンネルの参照先であればtrue
	 */
	final boolean isReferenceBpm() {
		return mIsReferenceBpm;
	}

	/**
	 * 譜面停止チャンネルの参照先設定
	 */
	final void setIsReferenceStop() {
		mIsReferenceStop = true;
	}

	/**
	 * 譜面停止チャンネルの参照先になっているかどうか判定
	 * @return 譜面停止チャンネルの参照先であればtrue
	 */
	final boolean isReferenceStop() {
		return mIsReferenceStop;
	}
}
