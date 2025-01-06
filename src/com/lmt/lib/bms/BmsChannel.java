package com.lmt.lib.bms;

import static com.lmt.lib.bms.internal.Assertion.*;

/**
 * BMSコンテンツにおいて時間軸を必要とする要素を表す情報です。
 *
 * <p>通常、楽曲を構成するには同じ時間軸上に複数の異なる情報が混在している必要があります。チャンネルはそれらの
 * 情報1個分に番号を付け、データ形式などの属性を決定するために用いられます。</p>
 *
 * <p>チャンネルを構成する情報には「番号」「データ型」「チャンネルと関連付いたメタ情報」
 * 「データが定義されなかった場合の初期値」「データの重複可否」「同一性チェック対象かどうか」が
 * 存在します。それぞれの情報の詳細については以下の記述を参照してください。</p>
 *
 * <p><strong>番号</strong><br>
 * 当該チャンネルの番号を示します。チャンネル番号には「仕様チャンネル」「ユーザーチャンネル」の2種類が存在します。<br>
 * 仕様チャンネルは{@link BmsSpec#SPEC_CHANNEL_MIN}～{@link BmsSpec#SPEC_CHANNEL_MAX}の範囲で指定し、<br>
 * ユーザーチャンネルは{@link BmsSpec#USER_CHANNEL_MIN}～{@link BmsSpec#USER_CHANNEL_MAX}の範囲で指定します。<br>
 * 仕様チャンネルはBMS仕様として定義するチャンネルを示し、このチャンネルのデータは外部データからの入出力の
 * 対象として扱われます。一方、ユーザーチャンネルはアプリケーションがBMSコンテンツを制御する目的で使用できる
 * 一時的なデータとしての扱いになり、外部データからの入出力対象にはなりません。</p>
 *
 * <p><strong>データ型</strong>({@link BmsType})<br>
 * チャンネルのデータを記述する際の型を決定します。<br>
 * 詳細は{@link BmsType}を参照してください。</p>
 *
 * <p><strong>チャンネルと関連付いたメタ情報</strong><br>
 * チャンネルのデータは、しばしば索引付きメタ情報のインデックス値を示すことがあります。例えば、チャンネルのデータが
 * 時間軸上で鳴らす音の種類を示し、関連付いたメタ情報がその音のファイル名を示す場合などです。このような表現を行う
 * ためにはチャンネルのデータ型が「配列型」であり、且つ関連付けようとするメタ情報の構成要素が「索引付き」である
 * 必要があります。</p>
 *
 * <p><strong>初期値</strong><br>
 * BMSの仕様として、全てのチャンネルは指定必須とすることができません。そのため、チャンネルには必ず初期値を
 * 定義しなければなりません。初期値の記述は文字列のみですが記述の書式はデータ型の指定内容に依存します。</p>
 *
 * <p><strong>データの重複可否</strong><br>
 * チャンネルに登録するデータの中には、同じ種類のデータを複数重複して登録したい場合があります。例えば、
 * 任意のタイミングで発音したい音(BGM)等がそれに該当します。そのようなデータはチャンネルを複数作成して重ねるのではなく
 * チャンネルをデータ重複可能にすることで実現できます。BMSライブラリでは、このような機能をサポートします。</p>
 *
 * <p><strong>同一性チェック</strong><br>
 * 同一性チェックの詳細な説明に関しては{@link BmsMeta}の説明を参照してください。<br>
 * 同一性についてはチャンネルに対しても適用することができ、データの内容が同一であるかどうかをチェックする必要が
 * あるかどうかについての情報を付与することができるようになっています。</p>
 *
 * <p><strong>任意型チャンネル</strong><br>
 * データ型を{@link BmsType#OBJECT}に指定したチャンネルは「任意型チャンネル」という扱いになります。
 * 任意型チャンネルは、その他のチャンネルとは下記の点が異なります。</p>
 *
 * <ul>
 * <li>仕様チャンネルの番号は指定できません。</li>
 * <li>初期値はnullでなければいけません。</li>
 * <li>同一性チェックをONにすることはできません。</li>
 * </ul>
 *
 * @since 0.0.1
 */
public final class BmsChannel extends BmsChannelKey {
	/** チャンネルのデータ型 */
	private BmsType mType;
	/** チャンネルに関連するメタ情報の参照先の名前(索引付きであること) */
	private String mRef;
	/** チャンネルのデータ定義がない場合の初期値(文字列表現) */
	private String mDefaultValueStr;
	/** チャンネルのデータ定義がない場合の初期値 */
	private Object mDefaultValue;
	/** チャンネルのデータ用途を示す値 1:小節長 2:BPM変更 3:譜面停止 */
	private int mRelatedFlag;
	/** 同一チャンネルで複数データを保有可能かを示すフラグ */
	private boolean mMultiple;
	/** データの同一性チェックを行う際に参照されるチャンネルかどうかを示すフラグ */
	private boolean mUniqueness;

	/**
	 * 新しいチャンネルオブジェクトを生成します。
	 * @param number チャンネル番号
	 * @param type チャンネルデータ型
	 * @param ref 参照先メタ情報
	 * @param defaultValue チャンネルデータの初期値
	 * @param multiple チャンネルデータの重複許可フラグ
	 * @param uniqueness 同一性チェックを行う際に参照されるチャンネルかどうかを示すフラグ
	 * @exception NullPointerException typeがnull
	 * @exception NullPointerException typeが{@link BmsType#OBJECT}以外の時、defaultValueがnull
	 * @exception IllegalArgumentException typeが{@link BmsType#OBJECT}の時、defaultValueがnullではない
	 * @exception IllegalArgumentException numberが{@link BmsSpec#CHANNEL_MIN}未満または{@link BmsSpec#CHANNEL_MAX}超過
	 * @exception IllegalArgumentException typeが{@link BmsType#OBJECT}の時、仕様チャンネルを指定した
	 * @exception IllegalArgumentException typeが{@link BmsType#OBJECT}の時、refがnullではない
	 * @exception IllegalArgumentException データ型が値型で参照先メタ情報を指定した
	 * @exception IllegalArgumentException typeが{@link BmsType#OBJECT}以外の時、defaultValueがtypeのテストに失敗した
	 * @exception IllegalArgumentException ユーザーチャンネルでuniquenessをtrueにしようとした
	 */
	public BmsChannel(int number, BmsType type, String ref, String defaultValue, boolean multiple, boolean uniqueness) {
		super(number);
		initialize(type, ref, defaultValue, multiple, uniqueness);
	}

	/**
	 * 新しいチャンネルオブジェクトを生成します。
	 * @param number チャンネル番号(36進数で指定)
	 * @param type チャンネルデータ型
	 * @param ref 参照先メタ情報
	 * @param defaultValue チャンネルデータの初期値
	 * @param multiple チャンネルデータの重複許可フラグ
	 * @param uniqueness 同一性チェックを行う際に参照されるチャンネルかどうかを示すフラグ
	 * @exception NullPointerException numberがnull
	 * @exception NumberFormatException numberの内容が36進数ではない
	 * @exception NullPointerException typeがnull
	 * @exception NullPointerException typeが{@link BmsType#OBJECT}以外の時、defaultValueがnull
	 * @exception IllegalArgumentException typeが{@link BmsType#OBJECT}の時、defaultValueがnullではない
	 * @exception IllegalArgumentException numberが{@link BmsSpec#CHANNEL_MIN}未満または{@link BmsSpec#CHANNEL_MAX}超過
	 * @exception IllegalArgumentException typeが{@link BmsType#OBJECT}の時、仕様チャンネルを指定した
	 * @exception IllegalArgumentException typeが{@link BmsType#OBJECT}の時、refがnullではない
	 * @exception IllegalArgumentException データ型が値型で参照先メタ情報を指定した
	 * @exception IllegalArgumentException typeが{@link BmsType#OBJECT}以外の時、defaultValueがtypeのテストに失敗した
	 * @exception IllegalArgumentException ユーザーチャンネルでuniquenessをtrueにしようとした
	 */
	public BmsChannel(String number, BmsType type, String ref, String defaultValue, boolean multiple, boolean uniqueness) {
		super(number);
		initialize(type, ref, defaultValue, multiple, uniqueness);
	}

	/**
	 * 新しい仕様チャンネルオブジェクトを生成します。
	 * <p>当メソッドでは仕様チャンネルの範囲外のチャンネル番号は指定できません。</p>
	 * @param number チャンネル番号
	 * @param type チャンネルデータ型
	 * @param ref 参照先メタ情報
	 * @param defaultValue チャンネルデータの初期値
	 * @param multiple チャンネルデータの重複許可フラグ
	 * @param uniqueness 同一性チェックを行う際に参照されるチャンネルかどうかを示すフラグ
	 * @return 仕様チャンネルオブジェクト
	 * @exception NullPointerException typeがnull
	 * @exception NullPointerException defaultValueがnull
	 * @exception IllegalArgumentException numberが{@link BmsSpec#SPEC_CHANNEL_MIN}未満または{@link BmsSpec#SPEC_CHANNEL_MAX}超過
	 * @exception IllegalArgumentException typeが{@link BmsType#OBJECT}
	 * @exception IllegalArgumentException データ型が値型の時、refがnullではない
	 * @exception IllegalArgumentException defaultValueがtypeのテストに失敗した
	 * @since 0.7.0
	 */
	public static BmsChannel spec(int number, BmsType type, String ref, String defaultValue, boolean multiple,
			boolean uniqueness) {
		assertSpecChannelRange(number);
		return new BmsChannel(number, type, ref, defaultValue, multiple, uniqueness);
	}

	/**
	 * 新しいユーザーチャンネルオブジェクトを生成します。
	 * <p>当メソッドではユーザーチャンネルの範囲外のチャンネル番号は指定できません。</p>
	 * @param number チャンネル番号
	 * @param type チャンネルデータ型
	 * @param ref 参照先メタ情報
	 * @param defaultValue チャンネルデータの初期値
	 * @param multiple チャンネルデータの重複許可フラグ
	 * @return ユーザーチャンネルオブジェクト
	 * @exception NullPointerException typeがnull
	 * @exception NullPointerException typeが{@link BmsType#OBJECT}以外の時、defaultValueがnull
	 * @exception IllegalArgumentException typeが{@link BmsType#OBJECT}の時、defaultValueがnullではない
	 * @exception IllegalArgumentException numberが{@link BmsSpec#USER_CHANNEL_MIN}未満または{@link BmsSpec#USER_CHANNEL_MAX}超過
	 * @exception IllegalArgumentException typeが{@link BmsType#OBJECT}の時、refがnullではない
	 * @exception IllegalArgumentException データ型が値型で参照先メタ情報を指定した
	 * @exception IllegalArgumentException typeが{@link BmsType#OBJECT}以外の時、defaultValueがtypeのテストに失敗した
	 */
	public static BmsChannel user(int number, BmsType type, String ref, String defaultValue, boolean multiple) {
		assertUserChannelRange(number);
		return new BmsChannel(number, type, ref, defaultValue, multiple, false);
	}

	/**
	 * 新しい任意型ユーザーチャンネルオブジェクトを生成します。
	 * <p>当メソッドではユーザーチャンネルの範囲外のチャンネル番号は指定できません。</p>
	 * @param number チャンネル番号
	 * @return 任意型ユーザーチャンネルオブジェクト
	 * @exception IllegalArgumentException numberが{@link BmsSpec#USER_CHANNEL_MIN}未満または{@link BmsSpec#USER_CHANNEL_MAX}超過
	 */
	public static BmsChannel object(int number) {
		assertUserChannelRange(number);
		return new BmsChannel(number, BmsType.OBJECT, null, null, false, false);
	}

	/**
	 * チャンネル番号とデータ型が分かる形式の文字列を返します。
	 * @return チャンネル番号とデータ型が分かる形式の文字列
	 */
	@Override
	public String toString() {
		return String.format("{Ch:%s(%d) as %s}", Integer.toString(getNumber(), 36), getNumber(), mType);
	}

	/**
	 * チャンネルのデータ用途を設定する
	 * @param flag データ用途 1:小節長 2:BPM変更 3:譜面停止
	 */
	final void setRelatedFlag(int flag) {
		mRelatedFlag = flag;
	}

	/**
	 * チャンネルのデータ型を取得します。
	 * @return チャンネルのデータ型
	 */
	public final BmsType getType() {
		return mType;
	}

	/**
	 * チャンネルの参照先メタ情報の名称を取得します。
	 * <p>参照先メタ情報の単位({@link BmsUnit})は{@link BmsUnit#INDEXED INDEXED}である必要があります。
	 * また、参照先メタ情報を持つチャンネルのデータ型は配列型であることを想定しています。配列の要素の値が
	 * メタ情報へアクセスするためのインデックス値になります。</p>
	 * <p>参照先メタ情報を持たないチャンネルに対してこのメソッドを呼び出すとnullを返します。</p>
	 * @return 参照先メタ情報の名称。参照先メタ情報を持たない場合はnull。
	 */
	public final String getRef() {
		return mRef;
	}

	/**
	 * 初期値の文字列表現を取得する。
	 * @return 初期値の文字列表現
	 */
	String getDefaultValueString() {
		return mDefaultValueStr;
	}

	/**
	 * このチャンネルにデータが設定されていない場合の初期値を取得します。
	 * <p>このメソッドが返す初期値のデータ型は{@link BmsType}のネイティブデータ型によって決定されます。
	 * ネイティブデータ型は{@link BmsType#getNativeType}で参照できます。型を変換してデータにアクセスする場合は
	 * {@link BmsType}の仕様に従ってください。</p>
	 * <p>BMSの定義では、ある小節のあるチャンネルのデータが必ず定義されているという保証はありません。
	 * 従って、各チャンネルには明確に初期値が規定されるべきであることから、BMSライブラリの仕様としても
	 * このメソッドがnullを返さないことを保証します。</p>
	 * @return このチャンネルのデータの初期値
	 */
	public final Object getDefaultValue() {
		return mDefaultValue;
	}

	/**
	 * チャンネルが1小節内に複数のデータを持つことができるかどうかを取得します。
	 * <p>この値がtrueになるチャンネルでは、1小節内に2つ以上のデータを持つことができます。</p>
	 * @return 複数データを持つことができる場合はtrue
	 */
	public final boolean isMultiple() {
		return mMultiple;
	}

	/**
	 * チャンネルのデータがBMS定義の同一性を判定するために使用されるべきかどうかを取得します。
	 * @return 同一性判定に使用されるべきである場合はtrue
	 */
	public final boolean isUniqueness() {
		return mUniqueness;
	}

	/**
	 * チャンネルのデータ型が配列型であるかどうかを取得します。
	 * @see BmsType#isArrayType
	 * @return データ型が配列型の場合はtrue
	 */
	public final boolean isArrayType() {
		return mType.isArrayType();
	}

	/**
	 * チャンネルのデータ型が値型であるかどうかを取得します。
	 * @see BmsType#isValueType
	 * @return データ型が値型の場合はtrue
	 */
	public final boolean isValueType() {
		return mType.isValueType();
	}

	/**
	 * チャンネルがBMSの演奏時間に関連するデータを持つかどうかを取得します。
	 * <p>「演奏時間に関連するデータ」とは、「小節長変更」「BPM変更」「譜面停止」のいずれかであることを示します。
	 * これらのデータが変更されると、影響の大小に関わらず演奏時間に変化が生じます。演奏時間に変化が生じることは
	 * BMSの同一性を損なうことと同義であり、先に示したチャンネルは{@link BmsChannel#isUniqueness isUniqueness}が
	 * trueを返すことを表します。</p>
	 * <p>このメソッドがtrueを返すチャンネルはBMSライブラリ側で仕様として規定されています。それらのチャンネルの
	 * 定義方法については{@link BmsSpecBuilder}を参照してください。</p>
	 * @return 演奏時間に関連するチャンネルである場合はtrue
	 */
	public final boolean isRelatedToTime() {
		return mRelatedFlag != 0;
	}

	/**
	 * 小節長変更チャンネルであるかどうかを取得します。
	 * @return 小節長変更チャンネルである場合はtrue
	 */
	public final boolean isLength() {
		return mRelatedFlag == 1;
	}

	/**
	 * BPM変更チャンネルであるかどうかを習得します。
	 * @return BPM変更チャンネルである場合はtrue
	 */
	public final boolean isBpm() {
		return mRelatedFlag == 2;
	}

	/**
	 * 譜面停止チャンネルであるかどうかを取得します。
	 * @return 譜面停止チャンネルである場合はtrue
	 */
	public final boolean isStop() {
		return mRelatedFlag == 3;
	}

	/**
	 * 仕様チャンネルかどうかを取得します。
	 * @return 仕様チャンネルである場合はtrue
	 */
	public final boolean isSpec() {
		var number = getNumber();
		return (number >= BmsSpec.SPEC_CHANNEL_MIN) && (number <= BmsSpec.SPEC_CHANNEL_MAX);
	}

	/**
	 * ユーザーチャンネルかどうかを取得します。
	 * @return ユーザーチャンネルである場合はtrue
	 */
	public final boolean isUser() {
		var number = getNumber();
		return (number >= BmsSpec.USER_CHANNEL_MIN) && (number <= BmsSpec.USER_CHANNEL_MAX);
	}

	/**
	 * チャンネルオブジェクト生成時の初期化を行う。
	 * @param type チャンネルデータ型
	 * @param ref 参照先メタ情報
	 * @param defaultValue チャンネルデータの初期値
	 * @param multiple チャンネルデータの重複許可フラグ
	 * @param uniqueness 同一性チェックを行う際に参照されるチャンネルかどうかを示すフラグ
	 * @exception NullPointerException typeがnull
	 * @exception NullPointerException typeがOBJECT以外の時、defaultValueがnull
	 * @exception IllegalArgumentException typeがOBJECTの時、defaultValueがnullではない
	 * @exception IllegalArgumentException typeがOBJECTの時、仕様チャンネルを指定した
	 * @exception IllegalArgumentException typeがOBJECTの時、refがnullではない
	 * @exception IllegalArgumentException データ型が値型で参照先メタ情報を指定した
	 * @exception IllegalArgumentException typeがOBJECT以外の時、defaultValueがtypeのテストに失敗した
	 * @exception IllegalArgumentException ユーザーチャンネルでuniquenessをtrueにしようとした
	 */
	private void initialize(BmsType type, String ref, String defaultValue, boolean multiple,
			boolean uniqueness) {
		// 各種チェックを実施する
		assertArgNotNull(type, "type");
		if (type.isObjectType()) {
			assertUserChannelRange(getNumber());
			assertArg(ref == null, "When type is OBJECT, 'ref' can NOT set.");
			assertArg(defaultValue == null, "When type is OBJECT, 'defaultValue' is allowed null only.");
			assertArg(uniqueness == false, "When type is OBJECT, 'uniqueness' is allowed false only.");
		} else {
			assertChannelRange(getNumber());
			assertArg(type.isArrayType() || (!type.isArrayType() && (ref == null)),
					"'ref' can only be specified for array type.");
			assertArgNotNull(defaultValue, "defaultValue");
			assertArg(type.test(defaultValue), "'defaultValue' does NOT passed %s test. value=%s",
					type.getName(), defaultValue);
		}

		// データを初期化する
		mType = type;
		mRef = ref;
		mDefaultValueStr = defaultValue;
		mDefaultValue = (defaultValue == null) ? null : BmsType.cast(defaultValue, type);
		mRelatedFlag = 0;
		mMultiple = multiple;
		mUniqueness = uniqueness;
	}
}
