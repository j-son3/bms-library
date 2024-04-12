package com.lmt.lib.bms;

import static com.lmt.lib.bms.BmsAssertion.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * BMS仕様を構築するための機能を提供します。
 *
 * <p>BMS仕様を表現する{@link BmsSpec}オブジェクトを生成するためのビルダークラスです。{@link BmsSpec}オブジェクトは
 * 当クラスを通してのみ生成することが出来ます。それ以外の方法で生成する手段はありません。</p>
 *
 * <p>ビルダークラスに設定する情報は以下を参照してください。</p>
 *
 * <ul>
 * <li>メタ情報を登録する：{@link #addMeta}</li>
 * <li>チャンネルを登録する：{@link #addChannel}</li>
 * <li>小節長変更チャンネルを設定する：{@link #setLengthChannel}</li>
 * <li>BPM変更チャンネルを設定する：{@link #setBpmChannel}</li>
 * <li>譜面停止チャンネルを設定する：{@link #setStopChannel}</li>
 * </ul>
 *
 * <p>ビルダーに全ての設定情報を投入した後、{@link #create}を呼び出すことでBMS仕様を生成することができます。
 * この時、設定内容の整合性チェックが行われ、問題が無ければ生成された{@link BmsSpec}オブジェクトが返されます。
 * 整合性チェックの内容については{@link #create}を参照してください。</p>
 *
 * <p>一度BMS仕様の生成に使用したビルダーオブジェクトは使用出来なくなります。複数のBMS仕様を生成したい場合は
 * ビルダーオブジェクトのインスタンスを生成し直し、そのインスタンスを使用してください。</p>
 *
 * @see BmsMeta
 * @see BmsChannel
 * @see BmsSpec
 */
public final class BmsSpecBuilder {
	/** 単体メタ情報一式 */
	private Map<String, BmsMeta> mSingleMetas = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
	/** 重複定義可能メタ情報一式 */
	private Map<String, BmsMeta> mMultipleMetas = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
	/** 索引付きメタ情報一式 */
	private Map<String, BmsMeta> mIndexedMetas = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
	/** メタ情報の総数 */
	private int mMetaCount = 0;
	/** チャンネル一式 */
	private Map<Integer, BmsChannel> mChannels = new HashMap<>();
	/** 初期BPMメタ情報の名称 */
	private String mBpmMetaName = null;
	/** 小節長変更チャンネルのチャンネル番号(無い場合はnull) */
	private Integer mLengthChannelNumber = null;
	/** BPM変更チャンネルのチャンネル番号一覧 */
	private Integer[] mBpmChannelNumbers = new Integer[0];
	/** 譜面停止チャンネルのチャンネル番号一覧 */
	private Integer[] mStopChannelNumbers = new Integer[0];
	/** このインスタンスによる仕様生成が完了したかどうか */
	private boolean mIsCreated = false;

	/**
	 * BMS仕様ビルダーオブジェクトを構築します。
	 */
	public BmsSpecBuilder() {
		// Do nothing
	}

	/**
	 * BMS仕様にメタ情報を登録します。
	 * @param meta 登録するメタ情報
	 * @return BMS仕様ビルダーオブジェクトのインスタンス
	 * @exception IllegalStateException BMS仕様生成後にこのメソッドを呼び出した
	 * @exception NullPointerException metaがnull
	 * @exception IllegalArgumentException メタ情報単位が{@link BmsUnit#SINGLE SINGLE}または{@link BmsUnit#MULTIPLE MULTIPLE}の時、
	 *                                       同じ名前のメタ情報がSINGLE/MULTIPLEのいずれかに登録済み
	 * @exception IllegalArgumentException メタ情報単位が{@link BmsUnit#INDEXED INDEXED}の時、同じ名前のメタ情報が
	 *                                       INDEXEDに登録済み
	 */
	public final BmsSpecBuilder addMeta(BmsMeta meta) {
		assertIsNotCreated();
		assertArgNotNull(meta, "meta");

		// メタ情報の名称の競合チェック
		if (meta.getUnit() == BmsUnit.INDEXED) {
			// 索引付きメタ情報
			assertArg(!mIndexedMetas.containsKey(meta.getName()), "Meta is already exists. meta=%s", meta);
		} else {
			// 単体・複数メタ情報
			assertArg(!mSingleMetas.containsKey(meta.getName()) && !mMultipleMetas.containsKey(meta.getName()),
					"Meta is already exists.", meta);
		}

		// メタ情報を追加する
		var metas = getMetaMap(meta.getUnit());
		meta.setOrder2(mMetaCount++);  // 並び替え用の内部ソートキー
		metas.put(meta.getName(), meta);

		return this;
	}

	/**
	 * BMS仕様にチャンネルを登録します。
	 * <p>チャンネルは「仕様チャンネル」「ユーザー定義チャンネル」の両方を登録できます。仕様チャンネルは
	 * チャンネル番号が{@link BmsSpec#SPEC_CHANNEL_MIN}～{@link BmsSpec#SPEC_CHANNEL_MAX}のチャンネルを指し、
	 * ユーザー定義チャンネルはチャンネル番号が{@link BmsSpec#USER_CHANNEL_MIN}～{@link BmsSpec#USER_CHANNEL_MAX}
	 * のチャンネルを指します。</p>
	 * <p>ユーザー定義チャンネルは、アプリケーションが独自の処理を行う際の一時的な作業領域として使用することが
	 * できるチャンネルです。このチャンネルを使用することでアプリケーションはBMSライブラリが提唱するデータ構造を
	 * 再利用することができ、独自のデータ構造を定義せずに目的の処理を実装できる可能性があります。</p>
	 * @param channel 登録するチャンネル
	 * @return BMS仕様ビルダーオブジェクトのインスタンス
	 * @exception IllegalStateException BMS仕様生成後にこのメソッドを呼び出した
	 * @exception NullPointerException channelがnull
	 * @exception IllegalArgumentException 同じチャンネル番号のチャンネルが登録済み
	 */
	public final BmsSpecBuilder addChannel(BmsChannel channel) {
		// エラーチェック
		assertIsNotCreated();
		assertArgNotNull(channel, "channel");
		assertArg(!containsChannel(channel.getNumber()), "Channel is already exists. channel=", channel);

		// チャンネルを追加する
		mChannels.put(BmsInt.box(channel.getNumber()), channel);

		return this;
	}

	/**
	 * 登録済みのメタ情報を取得します。
	 * @param name メタ情報の名称
	 * @param unit メタ情報の単位
	 * @return メタ情報。該当するメタ情報が存在しない場合はnull。
	 * @exception IllegalStateException BMS仕様生成後にこのメソッドを呼び出した
	 * @exception NullPointerException nameがnull
	 * @exception NullPointerException unitがnull
	 */
	public final BmsMeta getMeta(String name, BmsUnit unit) {
		assertIsNotCreated();
		assertArgNotNull(name, "name");
		assertArgNotNull(unit, "unit");
		return getMetaMap(unit).get(name);
	}

	/**
	 * 登録済みのチャンネルを取得します。
	 * @param number チャンネル番号
	 * @return チャンネル。該当するチャンネルが存在しない場合はnull。
	 * @exception IllegalStateException BMS仕様生成後にこのメソッドを呼び出した
	 */
	public final BmsChannel getChannel(int number) {
		assertIsNotCreated();
		return mChannels.get(BmsInt.box(number));
	}

	/**
	 * 指定されたメタ情報が登録されているかどうかを返します。
	 * @param name メタ情報の名称
	 * @param unit メタ情報の単位
	 * @return 該当するメタ情報が登録済みの場合はtrue
	 * @exception IllegalStateException BMS仕様生成後にこのメソッドを呼び出した
	 * @exception NullPointerException nameがnull
	 * @exception NullPointerException unitがnull
	 */
	public final boolean containsMeta(String name, BmsUnit unit) {
		assertIsNotCreated();
		assertArgNotNull(name, "name");
		assertArgNotNull(unit, "unit");
		return getMetaMap(unit).get(name) != null;
	}

	/**
	 * 指定されたチャンネルが登録されているかどうかを返します。
	 * @param number チャンネル番号
	 * @return 該当するチャンネルが登録済みの場合はtrue
	 * @exception IllegalStateException BMS仕様生成後にこのメソッドを呼び出した
	 */
	public final boolean containsChannel(int number) {
		assertIsNotCreated();
		return getChannel(number) != null;
	}

	/**
	 * 初期BPMのメタ情報を設定します。
	 * <p>初期BPMは楽曲の演奏開始時のBPMを表します。BMS仕様は、メタ情報として必ず初期BPMを設定しなければなりません。</p>
	 * <p>初期BPMのメタ情報は、以下の要件を満たしている必要があります。</p>
	 * <table><caption>&nbsp;</caption>
	 * <tr><td><b>要素</b></td><td><b>値</b></td></tr>
	 * <tr><td>構成単位</td><td>単体</td></tr>
	 * <tr><td>データ型</td><td>NUMERIC</td></tr>
	 * <tr><td>初期値</td><td>{@link BmsSpec#BPM_MIN}～{@link BmsSpec#BPM_MAX}</td></tr>
	 * <tr><td>同一性チェック</td><td>ON</td></tr>
	 * </table>
	 * @param name 初期BPMのメタ情報名称
	 * @return BMS仕様ビルダーオブジェクトのインスタンス
	 * @exception IllegalStateException BMS仕様生成後にこのメソッドを呼び出した
	 */
	public final BmsSpecBuilder setInitialBpmMeta(String name) {
		assertIsNotCreated();
		mBpmMetaName = name;
		return this;
	}

	/**
	 * 小節長変更チャンネルのチャンネル番号を設定します。
	 * @param number チャンネル番号。nullを指定すると小節長変更チャンネルの無いBMS仕様になります。
	 * @return BMS仕様ビルダーオブジェクトのインスタンス
	 * @exception IllegalStateException BMS仕様生成後にこのメソッドを呼び出した
	 */
	public final BmsSpecBuilder setLengthChannel(Integer number) {
		assertIsNotCreated();
		mLengthChannelNumber = number;
		return this;
	}

	/**
	 * BPM変更チャンネルのチャンネル番号を設定します。
	 * <p>チャンネル番号は複数指定できます。チャンネル番号が0個またはnullを1個指定した場合、
	 * BPM変更チャンネルのないBMS仕様になります。複数個指定する場合にはnullを含めないでください。
	 * nullがあるとBMS仕様生成時に例外がスローされます。</p>
	 * <p>デフォルトではBPM変更のないBMS仕様になります。そのようなBMS仕様を希望する場合は
	 * 当メソッドを使用しないことをお勧めします。</p>
	 * @param numbers チャンネル番号
	 * @return BMS仕様ビルダーオブジェクトのインスタンス
	 * @exception IllegalStateException BMS仕様生成後にこのメソッドを呼び出した
	 */
	public final BmsSpecBuilder setBpmChannel(Integer...numbers) {
		assertIsNotCreated();
		if ((numbers.length == 0) || ((numbers.length == 1) && (numbers[0] == null))) {
			// チャンネル番号0個、または1個でnull指定の場合はBPM変更なし
			mBpmChannelNumbers = new Integer[0];
		} else {
			// チャンネル番号1個以上指定の場合はBPM変更あり
			mBpmChannelNumbers = numbers;
		}
		return this;
	}

	/**
	 * 譜面停止チャンネルのチャンネル番号を設定します。
	 * <p>チャンネル番号は複数指定できます。チャンネル番号が0個またはnullを1個指定した場合、
	 * 譜面停止チャンネルのないBMS仕様になります。複数個指定する場合にはnullを含めないでください。
	 * nullがあるとBMS仕様生成時に例外がスローされます。</p>
	 * <p>デフォルトではBPM変更のないBMS仕様になります。そのようなBMS仕様を希望する場合は
	 * 当メソッドを使用しないことをお勧めします。</p>
	 * @param numbers チャンネル番号
	 * @return BMS仕様ビルダーオブジェクトのインスタンス
	 * @exception IllegalStateException BMS仕様生成後にこのメソッドを呼び出した
	 */
	public final BmsSpecBuilder setStopChannel(Integer...numbers) {
		assertIsNotCreated();
		if ((numbers.length == 0) || ((numbers.length == 1) && (numbers[0] == null))) {
			// チャンネル番号0個、または1個でnull指定の場合は譜面停止なし
			mStopChannelNumbers = new Integer[0];
		} else {
			// チャンネル番号1個以上指定の場合は譜面停止あり
			mStopChannelNumbers = numbers;
		}
		return this;
	}

	/**
	 * 現在のビルダーへの登録内容でBMS仕様オブジェクトを生成します。
	 * <p>このメソッドで一度BMS仕様オブジェクトを生成したビルダーオブジェクトはオブジェクト生成済み状態となり、
	 * 新しいBMS仕様を生成することが出来なくなります。新しく別のBMS仕様オブジェクトを生成したい場合は再度ビルダーを
	 * 生成し、新しいインスタンスでBMS仕様を構築してください。</p>
	 * <p>ビルダーではBMS仕様を生成するにあたり、各種登録内容の一連の仕様不整合チェックを行います。不整合があると
	 * 判定された場合はIllegalStateExceptionをスローします。どのような不整合チェックがあるかはスローする例外の
	 * 説明を参照してください。</p>
	 * <p>BMS仕様には、少なくとも1個の「配列型で同一性チェック対象のチャンネル」が存在しなければなりません。
	 * BMSは元々音楽ゲーム向けのファイルフォーマットであり、プレイヤーがプレー可能なチャンネルを定義するには
	 * 先述の条件を満たすチャンネルが不可欠だからです。BMSライブラリでは、そのチャンネルが1個も無いBMS仕様は
	 * 不適切であると位置付けているため、生成を許可していません。</p>
	 * @return {@link BmsSpec}オブジェクト
	 * @exception IllegalStateException BMS仕様生成後にこのメソッドを呼び出した
	 * @exception IllegalStateException ビルダーへの登録内容に以下の整合性違反があった場合
	 * <ul>
	 * <li>チャンネルが1件も存在しない</li>
	 * <li>同一性チェック対象の配列型仕様チャンネルが1件も存在しない</li>
	 * <li>ユーザーチャンネルで同一性チェックをONにした</li>
	 * <li>配列型チャンネルで参照先メタ情報の指定がある場合に、索引付きメタ情報に該当する名前のメタ情報が無い</li>
	 * <li>　(未使用)　配列型で参照先メタ情報指定があるチャンネルで、該当する参照先メタ情報が数値型ではない</li>
	 * <li>初期BPMメタ情報の名称がnull</li>
	 * <li>初期BPMメタ情報の名称に該当するメタ情報が単体メタ情報に存在しない</li>
	 * <li>初期BPMメタ情報の名称に該当するメタ情報のデータ型が数値型ではない</li>
	 * <li>初期BPMメタ情報の名称に該当するメタ情報の初期値が{@link BmsSpec#BPM_MIN}～{@link BmsSpec#BPM_MAX}の範囲外</li>
	 * <li>初期BPMメタ情報の名称に該当するメタ情報の同一性チェックがOFF</li>
	 * <li>小節長変更チャンネルの番号が指定されているが、当該番号のチャンネルが存在しない</li>
	 * <li>指定された小節長変更チャンネルの番号が仕様チャンネルではない</li>
	 * <li>指定された小節長変更チャンネルのデータ型が{@link BmsType#NUMERIC NUMERIC}ではない</li>
	 * <li>指定された小節長変更チャンネルが同一性チェック対象になっていない</li>
	 * <li>指定された小節長変更チャンネルの初期値が{@link BmsSpec#LENGTH_MIN}未満または{@link BmsSpec#LENGTH_MAX}超過</li>
	 * <li>BPM変更チャンネルの番号が指定されているが、当該番号のチャンネルが存在しない</li>
	 * <li>BPM変更チャンネルの番号が重複している</li>
	 * <li>指定されたBPM変更チャンネルの番号がnull</li>
	 * <li>指定されたBPM変更チャンネルの番号が仕様チャンネルではない</li>
	 * <li>指定されたBPM変更チャンネルのデータ型が配列型ではない</li>
	 * <li>指定されたBPM変更チャンネルが同一性チェック対象になっていない</li>
	 * <li>指定されたBPM変更チャンネルに参照先メタ情報がある場合で、索引付きメタ情報に該当する名前のメタ情報が無い</li>
	 * <li>指定されたBPM変更チャンネルの参照先メタ情報のデータ型が数値型ではない</li>
	 * <li>指定されたBPM変更チャンネルの参照先メタ情報が同一性チェック対象になっていない</li>
	 * <li>指定されたBPM変更チャンネルの参照先メタ情報の初期値が{@link BmsSpec#BPM_MIN}～{@link BmsSpec#BPM_MAX}の範囲外</li>
	 * <li>譜面停止チャンネルの番号が指定されているが、当該番号のチャンネルが存在しない</li>
	 * <li>譜面停止チャンネルの番号が重複している</li>
	 * <li>指定された譜面停止チャンネルの番号がnull</li>
	 * <li>指定された譜面停止チャンネルのデータ型が配列型ではない</li>
	 * <li>指定された譜面停止チャンネルの番号が仕様チャンネルではない</li>
	 * <li>指定された譜面停止チャンネルが同一性チェック対象になっていない</li>
	 * <li>指定された譜面停止チャンネルに参照先メタ情報がある場合で、索引付きメタ情報に該当する名前のメタ情報が無い</li>
	 * <li>指定された譜面停止チャンネルの参照先メタ情報のデータ型が数値型ではない</li>
	 * <li>指定された譜面停止チャンネルの参照先メタ情報が同一性チェック対象になっていない</li>
	 * <li>指定された譜面停止チャンネルの参照先メタ情報の初期値が{@link BmsSpec#STOP_MIN}～{@link BmsSpec#STOP_MAX}の範囲外</li>
	 * </ul>
	 */
	public final BmsSpec create() {
		assertIsNotCreated();

		// チャンネルが1件以上存在すること
		assertField(mChannels.size() != 0, "No channel is NOT allowed.");

		// チャンネルのアサーション
		var hasUniquenessArrayCh = false;
		for (var channel : mChannels.values()) {
			// アサーションに使用するデータ
			var chNum = channel.getNumber();
			var ref = channel.getRef();
			var uniqueness = channel.isUniqueness();
			var isArray = channel.isArrayType();
			var isSpec = channel.isSpec();
			var isUser = channel.isUser();

			// ユーザーチャンネルでは同一性チェックをONに出来ない
			if (isUser) {
				assertField(!uniqueness, "Channel %d: User channel should be OFF uniqueness.", chNum);
			}

			// 配列型チャンネルのアサーション
			if (isArray) {
				// 仕様チャンネルの配列型で一意性チェック対象チャンネルが1件以上存在すること
				if (isSpec && uniqueness) {
					hasUniquenessArrayCh = true;
				}

				// 配列型でref指定がある場合に、該当するINDEXEDのメタ情報が存在すること
				if (ref != null) {
					var refMeta = getMeta(ref, BmsUnit.INDEXED);
					assertField(refMeta != null, "Channel %d ref %s: No such indexed meta.", chNum, ref);
				}
			}
		}
		assertField(hasUniquenessArrayCh, "No uniqueness array type in channels.");

		// 初期BPMに関するエラーチェック
		assertField(mBpmMetaName != null, "Initial BPM meta is NOT specified. (null)");
		var initialBpmMeta = getMeta(mBpmMetaName, BmsUnit.SINGLE);
		assertField(initialBpmMeta != null, "\'%s\' No such single meta to use to initial BPM.", mBpmMetaName);
		assertField(initialBpmMeta.getType().equals(BmsType.NUMERIC), "Initial BPM meta type is NOT NUMERIC.");
		var initialBpmDefault = ((Double)initialBpmMeta.getDefaultValue()).doubleValue();
		assertField(BmsSpec.isBpmWithinRange(initialBpmDefault), "Initial BPM is out of range. bpm=%f", initialBpmDefault);
		assertField(initialBpmMeta.isUniqueness(), "Initial BPM is NOT uniqueness.");

		// 小節長変更のチャンネルに関するエラーチェック
		var lengthChannel = (BmsChannel)null;
		if (mLengthChannelNumber != null) {
			var ch = getChannel(mLengthChannelNumber);
			assertField(ch != null, "Length channel is NOT found. channel=%s", ch);
			assertField(ch.isSpec(), "Length channel must be a spec channel. channel=%s", ch);
			assertField(ch.getType().equals(BmsType.NUMERIC), "Length channel type is NOT NUMERIC. channel=%s", ch);
			assertField(ch.isUniqueness(), "Length channel is NOT uniqueness. channel=%s", ch);
			var length = (Double)(ch.getDefaultValue());
			assertField(BmsSpec.isLengthWithinRange(length),
					"Length channel default value is out of range. channel=%s, expect=(%f-%f), actual=%f",
					ch, BmsSpec.LENGTH_MIN, BmsSpec.LENGTH_MAX, length);
			ch.setRelatedFlag(1);
			lengthChannel = ch;
		}

		// BPM変更チャンネルに関するエラーチェック
		var bpmChannels = new ArrayList<BmsChannel>(mBpmChannelNumbers.length);
		for (var bpmChNum : mBpmChannelNumbers) {
			assertField(bpmChNum != null, "Null BPM channel is NOT allowed.");
			assertField(!bpmChannels.contains(new BmsChannelKey(bpmChNum)), "Detected duplicate BPM channel.");
			var ch = getChannel(bpmChNum);
			assertField(ch != null, "BPM channel is NOT found. channel=%s", ch);
			assertField(ch.isSpec(), "BPM channel must be a spec channel. channel=%s", ch);
			assertField(ch.getType().isArrayType(), "BPM channel type is NOT array type. channel=%s", ch);
			assertField(ch.isUniqueness(), "BPM channel is NOT uniqueness. channel=%s", ch);
			var refName = ch.getRef();
			if (refName != null) {
				// メタ情報参照先がある場合のチェック
				var ref = getMeta(refName, BmsUnit.INDEXED);
				assertField(ref != null, "BPM channel reference '%s' is NOT found. channel=%s", refName, ch);
				assertField(ref.getType().isNumberType(), "BPM channel reference '%s' is NOT number type. channel=%s", refName, ch);
				assertField(ref.isUniqueness(), "BPM channel reference '%s' is NOT uniqueness. channel=%s", refName, ch);
				var bpm = (Number)ref.getDefaultValue();
				assertField(BmsSpec.isBpmWithinRange(bpm.doubleValue()),
						"BPM channel reference '%s' default value is out of range. channel=%s, expect=(%f-%f), actual=%f",
						refName, ch, BmsSpec.BPM_MIN, BmsSpec.BPM_MAX, bpm);
				ref.setIsReferenceBpm();
			}
			ch.setRelatedFlag(2);
			bpmChannels.add(ch);
		}

		// 譜面停止チャンネルに関するエラーチェック
		var stopChannels = new ArrayList<BmsChannel>(mStopChannelNumbers.length);
		for (var stopChNum : mStopChannelNumbers) {
			assertField(stopChNum != null, "Null stop channel is NOT allowed.");
			assertField(!stopChannels.contains(new BmsChannelKey(stopChNum)), "Detected duplicate stop channel");
			var ch = getChannel(stopChNum);
			assertField(ch != null, "Stop channel is NOT found. channel=%s", ch);
			assertField(ch.isSpec(), "Stop channel must be a spec channel. channel=%s", ch);
			assertField(ch.getType().isArrayType(), "Stop channel type is NOT array type. channel=%s", ch);
			assertField(ch.isUniqueness(), "Stop channel is NOT uniqueness. channel=%s", ch);
			var refName = ch.getRef();
			if (refName != null) {
				// メタ情報参照先がある場合のチェック
				var ref = getMeta(ch.getRef(), BmsUnit.INDEXED);
				assertField(ref != null, "Stop channel reference '%s' is NOT found. channel=%s", refName, ch);
				assertField(ref.getType().isNumberType(), "Stop channel reference '%s' is NOT number type. channel=%s", refName, ch);
				assertField(ref.isUniqueness(), "Stop channel reference '%s' is NOT uniqueness. channel=%s", refName, ch);
				var stop = (Number)ref.getDefaultValue();
				assertField(BmsSpec.isStopWithinRange(stop.doubleValue()),
						"Stop channel reference '%s' default value is out of range. channel=%s, expect=(%.16g-%.16g), actual=%.16g",
						refName, ch, BmsSpec.STOP_MIN, BmsSpec.STOP_MAX, stop);
				ref.setIsReferenceStop();
			}
			ch.setRelatedFlag(3);
			stopChannels.add(ch);
		}

		// 初期BPMメタ情報を設定する
		initialBpmMeta.setIsInitialBpm();

		// BMS仕様オブジェクトを構築する
		var spec = new BmsSpec(
				mSingleMetas,
				mMultipleMetas,
				mIndexedMetas,
				mChannels,
				initialBpmMeta,
				lengthChannel,
				bpmChannels.toArray(BmsChannel[]::new),
				stopChannels.toArray(BmsChannel[]::new));

		// 当ビルダーを生成済み状態にする
		mSingleMetas = null;
		mMultipleMetas = null;
		mIndexedMetas = null;
		mChannels = null;
		mBpmMetaName = null;
		mLengthChannelNumber = null;
		mBpmChannelNumbers = null;
		mStopChannelNumbers = null;
		mIsCreated = true;

		return spec;
	}

	/**
	 * メタ情報単位に該当するメタ情報マップを返す。
	 * @param unit メタ情報単位
	 * @return メタ情報マップ
	 */
	private Map<String, BmsMeta> getMetaMap(BmsUnit unit) {
		switch (unit) {
		case SINGLE:
			return mSingleMetas;
		case MULTIPLE:
			return mMultipleMetas;
		case INDEXED:
			return mIndexedMetas;
		default:
			return null;
		}
	}

	/**
	 * BMS仕様未生成であることをチェックするアサーション。
	 * @exception IllegalStateException BMS仕様生成済み
	 */
	private void assertIsNotCreated() {
		if (mIsCreated) {
			throw new IllegalStateException("At this builder, the spec is already created.");
		}
	}
}
