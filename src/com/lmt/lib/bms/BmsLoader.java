package com.lmt.lib.bms;

import static com.lmt.lib.bms.internal.Assertion.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 外部データからBMSコンテンツを生成するローダの基底クラスです。
 *
 * <p>BMSライブラリは、「外部データ」からBMSコンテンツを読み込む機能を提供します。ここで言う「外部データ」とは、
 * BMSで記述されたテキストデータのことを指します。外部データはファイル等の何らかの形式で記録されていることを
 * 想定しており、Java言語の{@link java.io.InputStream}で読み取ることができるもの全てを指します。</p>
 *
 * <p><strong>BMSローダの基本的な動作について</strong><br>
 * BMS読み込み処理は「BMSコンテンツ生成処理部」と「パーサ部」に分かれており、このうちパーサ部は抽象化されています。
 * パーサ部は当クラスを継承したクラスで実装され、そのクラスがそのまま対応するフォーマットになります。
 * BMSライブラリでは{@link BmsStandardLoader}で標準フォーマットのBMSに対応しています。
 * また、当クラスを継承し独自のパーサ部を実装することで別のフォーマットに対応したBMSローダを作成できます。</p>
 *
 * <p><strong>エラーハンドリングについて</strong><br>
 * 通常、BMSの読み込みにおいてエラー行を検出した場合はその行を無視し、読み込みを続行することがほとんどです。
 * アプリケーションによっては、特定のエラーを検出した場合例外的に読み込みを中止し、読み込みエラーとして扱いたい
 * ケースがあります。そのような場合は{@link #setHandler(BmsLoadHandler)}でハンドラを登録し、発生したエラーに応じて
 * 読み込みの続行・中止を選択することができます。発生したエラーを蓄積し、エラーをユーザーに報告するようなケースも
 * エラーハンドリングを行うことで解決できます。詳細は{@link BmsLoadHandler}を参照してください。</p>
 *
 * <p><strong>拡張したBMSコンテンツオブジェクトを読み込みたい場合</strong><br>
 * アプリケーションによっては拡張したBMSコンテンツオブジェクト({@link BmsContent})を読み込みたいケースが存在します。
 * {@link BmsLoader}では読み込み時に、生成するBMSコンテンツオブジェクトを決定することができるようになっています。
 * {@link #setHandler(BmsLoadHandler)}で設定したハンドラの{@link BmsLoadHandler#createContent(BmsSpec)}を
 * オーバーライドすることによりアプリケーションで独自拡張したBMSコンテンツオブジェクトを返すことができます。</p>
 *
 * @see BmsLoadHandler
 * @see BmsScriptError
 */
public abstract class BmsLoader {
	/** デコード用バッファサイズ */
	private static final int OUT_BUFFER_SIZE = 8 * 1024;

	/** BMSローダのデフォルトハンドラです。具体的な振る舞いは{@link BmsLoadHandler}を参照してください。 */
	public static final BmsLoadHandler DEFAULT_HANDLER = new BmsLoadHandler() {};

	/** ローダーの設定を参照するクラス */
	private class Settings implements BmsLoaderSettings {
		/** {@inheritDoc} */
		@Override
		public BmsSpec getSpec() {
			return mSpec;
		}

		/** {@inheritDoc} */
		@Override
		public boolean isSyntaxErrorEnable() {
			return mIsEnableSyntaxError;
		}

		/** {@inheritDoc} */
		@Override
		public boolean isFixSpecViolation() {
			return mIsFixSpecViolation;
		}

		/** {@inheritDoc} */
		@Override
		public boolean isAllowRedefine() {
			return mIsAllowRedefine;
		}

		/** {@inheritDoc} */
		@Override
		public boolean isIgnoreUnknownMeta() {
			return mIsIgnoreUnknownMeta;
		}

		/** {@inheritDoc} */
		@Override
		public boolean isIgnoreUnknownChannel() {
			return mIsIgnoreUnknownChannel;
		}

		/** {@inheritDoc} */
		@Override
		public boolean isIgnoreWrongData() {
			return mIsIgnoreWrongData;
		}

		/** {@inheritDoc} */
		@Override
		public boolean isSkipReadTimeline() {
			return mIsSkipReadTimeline;
		}
	}

	/** チャンネルデータのうち、配列データを一時的にプールするための1データ要素 */
	private static class ChannelArrayData {
		/** この配列データが登場した行番号 */
		int lineNumber;
		/** 解析した行テキスト */
		Object line;
		/** 対象のチャンネル番号 */
		BmsChannel channel;
		/** 対象の小節番号 */
		int measure;
		/** int配列に変換された配列データ */
		List<Integer> array;
	}

	/** フィールドにセットしたノートオブジェクトをノート生成器で返す橋渡し用クラス */
	private static class NoteBridge implements BmsNote.Creator {
		/** createNoteで橋渡しするノートオブジェクト */
		BmsNote mNote = null;

		@Override
		public BmsNote createNote() {
			var note = mNote;
			mNote = null;
			return note;
		}
	}

	/** 重複可能チャンネルのインデックス値採番用キー */
	private static class MeasureChNumberKey {
		/** 小節番号 */
		private int mMeasure;
		/** チャンネル番号 */
		private int mChNumber;

		/**
		 * コンストラクタ
		 * @param measure 小節番号
		 * @param chNumber チャンネル番号
		 */
		MeasureChNumberKey(int measure, int chNumber) {
			mMeasure = measure;
			mChNumber = chNumber;
		}

		/** {@inheritDoc} */
		@Override
		public boolean equals(Object obj) {
			if (obj instanceof MeasureChNumberKey) {
				var mc = (MeasureChNumberKey)obj;
				return (mMeasure == mc.mMeasure) && (mChNumber == mc.mChNumber);
			} else {
				return false;
			}
		}

		/** {@inheritDoc} */
		@Override
		public int hashCode() {
			return Objects.hash(mMeasure, mChNumber);
		}
	}

	/** ローダー設定I/F */
	private Settings mSettings = new Settings();
	/** BMS仕様 */
	private BmsSpec mSpec = null;
	/** BMS読み込みハンドラ */
	private BmsLoadHandler mHandler = DEFAULT_HANDLER;
	/** 構文エラー有効フラグ */
	private boolean mIsEnableSyntaxError = false;
	/** BMS仕様違反の値修正フラグ */
	private boolean mIsFixSpecViolation = true;
	/** 単体メタ情報・重複不可チャンネルの再定義を許可するかどうか */
	private boolean mIsAllowRedefine = true;
	/** 不明メタ情報を無視するかどうか */
	private boolean mIsIgnoreUnknownMeta = true;
	/** 不明チャンネルを無視するかどうか */
	private boolean mIsIgnoreUnknownChannel = true;
	/** 不正データを無視するかどうか */
	private boolean mIsIgnoreWrongData = true;
	/** タイムライン読み込みスキップするかどうか */
	private boolean mIsSkipReadTimeline = false;
	/** デコード時の文字セットリスト(リストの先頭から順にデコードが試みられる) */
	private List<Charset> mCharsets = new ArrayList<>();
	/** ノートオブジェクト橋渡し用クラス */
	private NoteBridge mNoteBridge = new NoteBridge();
	/** エラー発生有無マップ */
	private Map<BmsErrorType, BooleanSupplier> mOccurErrorMap;
	/** BMS読み込み時のデコード用出力バッファ */
	private CharBuffer mOutBuffer = CharBuffer.wrap(new char[OUT_BUFFER_SIZE]);

	/**
	 * 解析済み要素の種別を表す列挙型です。
	 * <p><strong>※当クラスはBMSライブラリの一般利用者が参照する必要はありません。</strong></p>
	 */
	protected static enum ParsedElementType {
		/** BMS宣言 */
		DECLARATION,
		/** メタ情報 */
		META,
		/** 値型チャンネル */
		VALUE_CHANNEL,
		/** 配列型チャンネル */
		ARRAY_CHANNEL,
		/** エラー */
		ERROR,
	}

	/**
	 * BMSの入力元から解析された1つの要素を表すオブジェクトの抽象クラスです。
	 * <p><strong>※当クラスはBMSライブラリの一般利用者が参照する必要はありません。</strong></p>
	 * <p>当クラスはBMSコンテンツを構成する複数種類の要素の基底クラスであり、それぞれの要素が持つ共通の情報を管理します。
	 * このオブジェクトはBMS読み込み時にBMSローダのパーサ部から返される一時的なオブジェクトで、
	 * BMSローダのBMSコンテンツの読み込み処理部とパーサ部とのデータの橋渡しに利用されます。
	 * つまり、当クラスから派生するクラスは全て内部処理用のクラスであるためBMSライブラリの一般利用者はこれらを無視して構いません。</p>
	 */
	protected static abstract class ParsedElement {
		/** 要素の種別 */
		private ParsedElementType mElementType;
		/** この要素が存在した入力元の行番号、または要素の登場した順番 */
		public int lineNumber;
		/** この要素の元になった行の記述内容、定義内容などのデータ */
		public Object line;

		/**
		 * コンストラクタ
		 * @param elementType 要素の種別
		 */
		private ParsedElement(ParsedElementType elementType) {
			mElementType = elementType;
		}

		/**
		 * この要素がエラー要素かどうかを返します。
		 * @return エラー要素であればtrue、そうでなければfalse
		 */
		public final boolean error() {
			return mElementType == ParsedElementType.ERROR;
		}
	}

	/**
	 * BMSの入力元から解析されたBMS宣言を表す要素データクラスです。
	 * <p><strong>※当クラスはBMSライブラリの一般利用者が参照する必要はありません。</strong></p>
	 * @see ParsedElement
	 */
	protected static class DeclarationParsedElement extends ParsedElement {
		/** BMS宣言のキー名 */
		public String key;
		/** BMS宣言の値 */
		public String value;

		/** BMS宣言要素のオブジェクトを構築します。 */
		public DeclarationParsedElement() {
			this(0, null, null, null);
		}

		/**
		 * BMS宣言要素のオブジェクトを構築します。
		 * @param lineNumber この要素が存在した入力元の行番号、または要素の登場した順番
		 * @param line この要素の元になった行の記述内容、定義内容などのデータ
		 * @param key BMS宣言のキー名
		 * @param value BMS宣言の値
		 */
		public DeclarationParsedElement(int lineNumber, Object line, String key, String value) {
			super(ParsedElementType.DECLARATION);
			set(lineNumber, line, key, value);
		}

		/**
		 * BMS宣言要素のオブジェクトの内容を設定します。
		 * @param lineNumber この要素が存在した入力元の行番号、または要素の登場した順番
		 * @param line この要素の元になった行の記述内容、定義内容などのデータ
		 * @param key BMS宣言のキー名
		 * @param value BMS宣言の値
		 * @return このオブジェクトのインスタンス
		 */
		public final DeclarationParsedElement set(int lineNumber, Object line, String key, String value) {
			this.lineNumber = lineNumber;
			this.line = line;
			this.key = key;
			this.value = value;
			return this;
		}
	}

	/**
	 * BMSの入力元から解析されたメタ情報を表す要素データクラスです。
	 * <p><strong>※当クラスはBMSライブラリの一般利用者が参照する必要はありません。</strong></p>
	 * @see ParsedElement
	 */
	protected static class MetaParsedElement extends ParsedElement {
		/** メタ情報 */
		public BmsMeta meta;
		/** 索引付きメタ情報のインデックス(索引付き以外では0であること) */
		public int index;
		/** メタ情報の値の文字列表現 */
		public String value;

		/** メタ情報要素のオブジェクトを構築します。 */
		public MetaParsedElement() {
			this(0, null, null, 0, null);
		}

		/**
		 * メタ情報要素のオブジェクトを構築します。
		 * @param lineNumber この要素が存在した入力元の行番号、または要素の登場した順番
		 * @param line この要素の元になった行の記述内容、定義内容などのデータ
		 * @param meta メタ情報
		 * @param index 索引付きメタ情報のインデックス
		 * @param value メタ情報の値
		 */
		public MetaParsedElement(int lineNumber, Object line, BmsMeta meta, int index, String value) {
			super(ParsedElementType.META);
			set(lineNumber, line, meta, index, value);
		}

		/**
		 * メタ情報要素のオブジェクトの内容を設定します。
		 * @param lineNumber この要素が存在した入力元の行番号、または要素の登場した順番
		 * @param line この要素の元になった行の記述内容、定義内容などのデータ
		 * @param meta メタ情報
		 * @param index 索引付きメタ情報のインデックス
		 * @param value メタ情報の値
		 * @return このオブジェクトのインスタンス
		 */
		public final MetaParsedElement set(int lineNumber, Object line, BmsMeta meta, int index, String value) {
			this.lineNumber = lineNumber;
			this.line = line;
			this.meta = meta;
			this.index = index;
			this.value = value;
			return this;
		}
	}

	/**
	 * BMSの入力元から解析されたチャンネルを表す要素データクラスです。
	 * <p><strong>※当クラスはBMSライブラリの一般利用者が参照する必要はありません。</strong></p>
	 * @see ParsedElement
	 */
	protected static abstract class ChannelParsedElement extends ParsedElement {
		/** 小節番号 */
		public int measure;
		/** チャンネル番号 */
		public int number;

		/**
		 * チャンネル要素のオブジェクトを構築します。
		 * @param elementType 要素の種別
		 */
		protected ChannelParsedElement(ParsedElementType elementType) {
			super(elementType);
		}
	}

	/**
	 * BMSの入力元から解析された値型チャンネルを表す要素データクラスです。
	 * <p><strong>※当クラスはBMSライブラリの一般利用者が参照する必要はありません。</strong></p>
	 * @see ChannelParsedElement
	 */
	protected static class ValueChannelParsedElement extends ChannelParsedElement {
		/** チャンネルの値の文字列表現 */
		public String value;

		/** 値型チャンネル要素のオブジェクトを構築します。 */
		public ValueChannelParsedElement() {
			this(0, null, 0, 0, null);
		}

		/**
		 * 値型チャンネル要素のオブジェクトを構築します。
		 * @param lineNumber この要素が存在した入力元の行番号、または要素の登場した順番
		 * @param line この要素の元になった行の記述内容、定義内容などのデータ
		 * @param measure 小節番号
		 * @param number チャンネル番号
		 * @param value チャンネルの値の文字列表現
		 */
		public ValueChannelParsedElement(int lineNumber, Object line, int measure, int number, String value) {
			super(ParsedElementType.VALUE_CHANNEL);
			set(lineNumber, line, measure, number, value);
		}

		/**
		 * 値型チャンネル要素のオブジェクトの内容を設定します。
		 * @param lineNumber この要素が存在した入力元の行番号、または要素の登場した順番
		 * @param line この要素の元になった行の記述内容、定義内容などのデータ
		 * @param measure 小節番号
		 * @param number チャンネル番号
		 * @param value チャンネルの値の文字列表現
		 * @return このオブジェクトのインスタンス
		 */
		public final ChannelParsedElement set(int lineNumber, Object line, int measure, int number, String value) {
			this.lineNumber = lineNumber;
			this.line = line;
			this.measure = measure;
			this.number = number;
			this.value = value;
			return this;
		}
	}

	/**
	 * BMSの入力元から解析された配列型チャンネルを表す要素データクラスです。
	 * <p><strong>※当クラスはBMSライブラリの一般利用者が参照する必要はありません。</strong></p>
	 * @see ChannelParsedElement
	 */
	protected static class ArrayChannelParsedElement extends ChannelParsedElement {
		/** 配列データ */
		public List<Integer> array;

		/** 配列型チャンネル要素のオブジェクトを構築します。 */
		public ArrayChannelParsedElement() {
			this(0, null, 0, 0, null);
		}

		/**
		 * 配列型チャンネル要素のオブジェクトを構築します。
		 * @param lineNumber この要素が存在した入力元の行番号、または要素の登場した順番
		 * @param line この要素の元になった行の記述内容、定義内容などのデータ
		 * @param measure 小節番号
		 * @param number チャンネル番号
		 * @param array 配列データ
		 */
		public ArrayChannelParsedElement(int lineNumber, Object line, int measure, int number, List<Integer> array) {
			super(ParsedElementType.ARRAY_CHANNEL);
			set(lineNumber, line, measure, number, array);
		}

		/**
		 * 配列型チャンネル要素のオブジェクトの内容を設定します。
		 * @param lineNumber この要素が存在した入力元の行番号、または要素の登場した順番
		 * @param line この要素の元になった行の記述内容、定義内容などのデータ
		 * @param measure 小節番号
		 * @param number チャンネル番号
		 * @param array 配列データ
		 * @return このオブジェクトのインスタンス
		 */
		public final ArrayChannelParsedElement set(int lineNumber, Object line, int measure, int number,
				List<Integer> array) {
			this.lineNumber = lineNumber;
			this.line = line;
			this.measure = measure;
			this.number = number;
			this.array = array;
			return this;
		}
	}

	/**
	 * BMSローダのパーサ部で発生したエラーを表す要素データクラスです。
	 * <p><strong>※当クラスはBMSライブラリの一般利用者が参照する必要はありません。</strong></p>
	 * @see ParsedElement
	 */
	protected static class ErrorParsedElement extends ParsedElement {
		/** エラーがないことを表すエラー要素オブジェクト */
		public static final ErrorParsedElement PASS = new ErrorParsedElement();

		/** エラー発出要因となった解析対象要素の種別 */
		public ParsedElementType causeType;
		/** エラー情報 */
		public BmsScriptError error;

		/** エラー要素のオブジェクトを構築します。 */
		public ErrorParsedElement() {
			this(ParsedElementType.ERROR, null);
		}

		/**
		 * エラー要素のオブジェクトを構築します。
		 * @param error エラー情報
		 */
		public ErrorParsedElement(BmsScriptError error) {
			super(ParsedElementType.ERROR);
			set(ParsedElementType.ERROR, error);
		}

		/**
		 * エラー要素のオブジェクトを構築します。
		 * @param causeType エラー発出要因となった解析対象要素の種別
		 * @param error エラー情報
		 */
		public ErrorParsedElement(ParsedElementType causeType, BmsScriptError error) {
			super(ParsedElementType.ERROR);
			set(causeType, error);
		}

		/**
		 * エラー要素のオブジェクトの内容を設定します。
		 * @param causeType エラー発出要因となった解析対象要素の種別
		 * @param error エラー情報
		 * @return このオブジェクトのインスタンス
		 */
		public final ErrorParsedElement set(ParsedElementType causeType, BmsScriptError error) {
			this.causeType = causeType;
			this.error = error;
			return this;
		}

		/**
		 * このオブジェクトが「エラーなし」を表すかどうかを判定します。
		 * @return エラーなしであればtrue、そうでなければfalse
		 */
		public final boolean pass() {
			return error == null;
		}

		/**
		 * このオブジェクトが「エラーあり」を表すかどうかを判定します。
		 * @return エラーありであればtrue、そうでなければfalse
		 */
		public final boolean fail() {
			return error != null;
		}
	}

	/**
	 * BmsLoaderオブジェクトを構築します。
	 */
	public BmsLoader() {
		mOccurErrorMap = Map.ofEntries(
				Map.entry(BmsErrorType.SYNTAX, () -> mIsEnableSyntaxError),
				Map.entry(BmsErrorType.SPEC_VIOLATION, () -> !mIsFixSpecViolation),
				Map.entry(BmsErrorType.UNKNOWN_META, () -> !mIsIgnoreUnknownMeta),
				Map.entry(BmsErrorType.UNKNOWN_CHANNEL, () -> !mIsIgnoreUnknownChannel),
				Map.entry(BmsErrorType.WRONG_DATA, () -> !mIsIgnoreWrongData),
				Map.entry(BmsErrorType.REDEFINE, () -> !mIsAllowRedefine),
				Map.entry(BmsErrorType.COMMENT_NOT_CLOSED, () -> mIsEnableSyntaxError),
				Map.entry(BmsErrorType.TEST_CONTENT, () -> mIsEnableSyntaxError));
	}

	/**
	 * 読み込み対象BMSのBMS仕様を設定します。
	 * <p>ローダーは設定されたBMS仕様に従ってBMSの構文を解析します。</p>
	 * @param spec BMS仕様
	 * @return このオブジェクトのインスタンス
	 */
	public final BmsLoader setSpec(BmsSpec spec) {
		mSpec = spec;
		return this;
	}

	/**
	 * BMS読み込みハンドラを設定します。
	 * <p>デフォルトでは{@link #DEFAULT_HANDLER}が設定されています。BMS読み込み時の振る舞いを変えたい場合は
	 * 実装をカスタマイズした{@link BmsLoadHandler}オブジェクトを設定してください。</p>
	 * <p>nullを設定することは出来ません。nullにした状態で{@link #load}メソッドを呼び出すと例外がスローされます。</p>
	 * @param handler BMS読み込みハンドラ
	 * @return このオブジェクトのインスタンス
	 */
	public final BmsLoader setHandler(BmsLoadHandler handler) {
		mHandler = handler;
		return this;
	}

	/**
	 * 厳格なフォーマットチェックの有無を設定します。
	 * <p>この設定を有効にすると、BMSの記述内容のチェックが厳格になりエラーが出力されやすくなります。
	 * 記述内容の不備に関する要因で読み込み処理が中止されやすくなりますが、
	 * それによる意図しないBMSコンテンツの構築を防止できるというメリットがあります。</p>
	 * <p>具体的には、この設定を有効にすると下記の設定になり、無効にすると下記とは逆の設定になります。</p>
	 * <ul>
	 * <li>構文エラーを無視せずエラーとして報告します。</li>
	 * <li>BMSライブラリの仕様に違反する範囲の値を設定した時、値を仕様範囲内に収めずエラーとして報告します。</li>
	 * <li>メタ情報・値型の重複可能チャンネルの再定義を検出した時、上書きをせずにエラーとして報告します。</li>
	 * <li>BMS仕様にないメタ情報を検出した時、無視をせずエラーとして報告します。</li>
	 * <li>BMS仕様にないチャンネルを検出した時、無視をせずエラーとして報告します。</li>
	 * <li>BMS仕様・チャンネルの値が規定されたデータ型に適合しない時、無視をせずエラーとして報告します。</li>
	 * </ul>
	 * <p>当メソッドで設定を行うと、複数の設定が一度に書き換えられることに留意してください。</p>
	 * @param strictly 厳格なフォーマットチェックの有無
	 * @return このオブジェクトのインスタンス
	 * @see #setSyntaxErrorEnable(boolean)
	 * @see #setFixSpecViolation(boolean)
	 * @see #setAllowRedefine(boolean)
	 * @see #setIgnoreUnknownMeta(boolean)
	 * @see #setIgnoreUnknownChannel(boolean)
	 * @see #setIgnoreWrongData(boolean)
	 */
	public final BmsLoader setStrictly(boolean strictly) {
		return this
				.setSyntaxErrorEnable(strictly)
				.setFixSpecViolation(!strictly)
				.setAllowRedefine(!strictly)
				.setIgnoreUnknownMeta(!strictly)
				.setIgnoreUnknownChannel(!strictly)
				.setIgnoreWrongData(!strictly);
	}

	/**
	 * 構文エラーの有効状態を設定します。
	 * <p>BMSでは、メタ情報・チャンネル定義以外の記述は全て無視される仕様になっています。BMS内での不要・不正な記述を
	 * 防ぐために、メタ情報・チャンネル定義と認識されない全ての記述を構文エラーとしたい場合に当メソッドを使用します。</p>
	 * <p>構文エラーを有効にした状態でメタ情報・チャンネル定義以外の記述を検出すると、BMS読み込みハンドラにて解析エラーを
	 * 通知するようになります。通知メソッドから解析中止が返されるとBMS解析はエラーとなります。</p>
	 * <p>この設定は、複数行コメントが終了しない状態でBMSの読み込みが終了した場合({@link BmsErrorType#COMMENT_NOT_CLOSED})、
	 * および{@link #setHandler(BmsLoadHandler)}で設定したハンドラの{@link BmsLoadHandler#testContent(BmsContent)}
	 * が検査失敗を返した場合({@link BmsErrorType#TEST_CONTENT})にも適用されます。</p>
	 * <p>デフォルトでは構文エラーは「無効」になっています。</p>
	 * @param isEnable 構文エラーの有効状態
	 * @return このオブジェクトのインスタンス
	 */
	public final BmsLoader setSyntaxErrorEnable(boolean isEnable) {
		mIsEnableSyntaxError = isEnable;
		return this;
	}

	/**
	 * BMSライブラリの定める仕様に違反した値を自動的に仕様範囲内に丸め込むかどうかを設定します。
	 * <p>この設定を有効にすることで訂正される値とは、「初期BPM」「小節長」「BPM変更用のBPM」「譜面停止時間」を指します。
	 * それぞれの値の許容範囲は{@link BmsSpec}を参照してください。</p>
	 * <p>この設定のデフォルト値は false です。BMSライブラリ仕様の違反を検出するとローダーに設定された{@link BmsLoadHandler}
	 * に対してエラー通知を行います。その際のエラー種別は{@link BmsErrorType#SPEC_VIOLATION}となります。</p>
	 * <p>通常、BMSライブラリの仕様違反となる値は非常に極端な値となっており、値を訂正したとしても当該楽曲の再生には
	 * 大きな影響がない場合がほとんどですが、値の使い方次第では楽曲の構成を大きく崩してしまう可能性があります。そのような
	 * ケースが許容されない場合には仕様違反の訂正は行わず、当該楽曲の読み込みはエラーとして扱うべきです。</p>
	 * <p>この設定を有効にして読み込まれたBMSは定義上とは異なるデータとして読み込まれますので、{@link BmsContent#generateHash}
	 * が生成する値にも影響を及ぼします。</p>
	 * <p>デフォルトではこの設定は「有効」になっています。</p>
	 * @param isFix 仕様違反訂正の有効状態
	 * @return このオブジェクトのインスタンス
	 */
	public final BmsLoader setFixSpecViolation(boolean isFix) {
		mIsFixSpecViolation = isFix;
		return this;
	}

	/**
	 * メタ情報・値型の重複不可チャンネルの再定義を検出した場合のデータ上書きを許可するかどうかを設定します。
	 * <p>データ上書きを許可すると、先に定義されたメタ情報、または同じ小節の値型重複不可チャンネルデータを上書きするようになります。
	 * メタ情報は単体メタ情報({@link BmsUnit#SINGLE})と定義済みの索引付きメタ情報({@link BmsUnit#INDEXED})が対象です。</p>
	 * <p>再定義が不許可の状態でメタ情報・値型重複不可チャンネルの再定義が検出された場合、再定義された行はエラーとして処理され、
	 * BMSコンテンツ読み込みハンドラの{@link BmsLoadHandler#parseError(BmsScriptError)}が呼び出されます。
	 * エラーは{@link BmsErrorType#REDEFINE}として通知されます。</p>
	 * <p>この設定は配列型の重複不可チャンネルには適用されません。配列型の重複不可チャンネルで重複定義があった場合、
	 * 重複した配列の定義内容を合成します。これはBMSの一般的な仕様です。</p>
	 * <p>デフォルトではこの設定は「許可」になっています。</p>
	 * @param isAllow 再定義の許可有無
	 * @return このオブジェクトのインスタンス
	 */
	public final BmsLoader setAllowRedefine(boolean isAllow) {
		mIsAllowRedefine = isAllow;
		return this;
	}

	/**
	 * 不明なメタ情報を無視するかどうかを設定します。
	 * <p>無視すると、BMS解析はエラーにならず不明メタ情報を読み飛ばして解析を続行するようになります。</p>
	 * <p>具体的には、{@link #setHandler(BmsLoadHandler)}で設定したハンドラの{@link BmsLoadHandler#parseError(BmsScriptError)}
	 * に{@link BmsErrorType#UNKNOWN_META}のエラーが一切通知されなくなります。</p>
	 * <p>デフォルトではこの設定は「有効」になっています。</p>
	 * @param isIgnore 不明メタ情報を無視するかどうか
	 * @return このオブジェクトのインスタンス
	 */
	public final BmsLoader setIgnoreUnknownMeta(boolean isIgnore) {
		mIsIgnoreUnknownMeta = isIgnore;
		return this;
	}

	/**
	 * 不明なチャンネルを無視するかどうかを設定します。
	 * <p>無視すると、BMS解析はエラーにならず不明チャンネルを読み飛ばして解析を続行するようになります。</p>
	 * <p>具体的には、{@link #setHandler(BmsLoadHandler)}で設定したハンドラの{@link BmsLoadHandler#parseError(BmsScriptError)}
	 * に{@link BmsErrorType#UNKNOWN_CHANNEL}のエラーが一切通知されなくなります。</p>
	 * <p>デフォルトではこの設定は「有効」になっています。</p>
	 * @param isIgnore 不明チャンネルを無視するかどうか
	 * @return このオブジェクトのインスタンス
	 */
	public final BmsLoader setIgnoreUnknownChannel(boolean isIgnore) {
		mIsIgnoreUnknownChannel = isIgnore;
		return this;
	}

	/**
	 * 不正なデータを無視するかどうかを設定します。
	 * <p>無視すると、BMS解析はエラーにならず不正データ定義のあったメタ情報・チャンネルを読み飛ばして解析を
	 * 続行するようになります。</p>
	 * <p>具体的には、{@link #setHandler(BmsLoadHandler)}で設定したハンドラの{@link BmsLoadHandler#parseError(BmsScriptError)}
	 * に{@link BmsErrorType#WRONG_DATA}のエラーが一切通知されなくなります。</p>
	 * <p>デフォルトではこの設定は「有効」になっています。</p>
	 * @param isIgnore 不正データを無視するかどうか
	 * @return このオブジェクトのインスタンス
	 */
	public final BmsLoader setIgnoreWrongData(boolean isIgnore) {
		mIsIgnoreWrongData = isIgnore;
		return this;
	}

	/**
	 * タイムラインの読み込みをスキップするかどうかを設定します。
	 * <p>この設定を有効にするとタイムラインの定義を読み飛ばすようになり、BMSコンテンツに小節データとノートが
	 * 含まれなくなります。つまり、BMSコンテンツに取り込まれるのはBMS宣言とメタ情報のみなることを意味します。</p>
	 * <p>読み込み対象のBMSに含まれるメタ情報のみを参照したい場合は、この設定を有効にすることで
	 * 読み込み処理のパフォーマンス向上が期待できます。</p>
	 * <p>タイムラインの読み込みをスキップする場合、タイムライン(チャンネル定義)の行の小節番号とチャンネル番号定義の
	 * 書式が正しければ、誤った値({@link BmsErrorType#WRONG_DATA}になる状態)を定義していたとしてもスキップされます。
	 * ただし、小節番号、チャンネル番号のいずれかが誤った書式で定義されていた場合、その行はスキップされず
	 * 構文エラー({@link BmsErrorType#SYNTAX})として扱われます。</p>
	 * <p>デフォルトではこの設定は「無効」になっています。</p>
	 * @param isSkip タイムラインの読み込みをスキップするかどうか
	 * @return このオブジェクトのインスタンス
	 */
	public final BmsLoader setSkipReadTimeline(boolean isSkip) {
		mIsSkipReadTimeline = isSkip;
		return this;
	}

	/**
	 * BMS読み込み時、入力のテキストデータのデコードに使用する文字セットを設定します。
	 * <p>BMS読み込みに{@link #load(Reader)}以外を使用する場合、テキストのデコード処理が必要になります。
	 * 当メソッドを使用し、優先順位の高い文字セットから順に文字セットを登録してください。</p>
	 * <p>同じ文字セットを複数追加しても意味はありません。後方で指定したほうの同一の文字セットが無視されます。</p>
	 * <p>文字セットの登録は省略可能です。省略した場合{@link BmsLibrary#getDefaultCharsets()}を呼び出し、
	 * BMSライブラリのデフォルト文字セットリストを使用してデコード処理が行われます。
	 * これは、当メソッドで文字セットを1個も指定しなかった場合も同様です。</p>
	 * @param charsets テキストのデコード処理時に使用する文字セットリスト
	 * @return このオブジェクトのインスタンス
	 * @exception NullPointerException charsetsにnullが含まれている
	 * @see BmsLibrary#setDefaultCharsets(Charset...)
	 */
	public final BmsLoader setCharsets(Charset...charsets) {
		mCharsets = Stream.of(charsets)
				.peek(cs -> assertArgNotNull(cs, "charsets[?]"))
				.distinct()
				.collect(Collectors.toList());
		return this;
	}

	/**
	 * BMSコンテンツを読み込みます。
	 * <p>BMSコンテンツは指定されたファイルから読み込みます。</p>
	 * <p>読み込み処理の詳細は{@link #load(String)}を参照してください。</p>
	 * @param bms BMSファイル
	 * @return BMSコンテンツ
	 * @exception NullPointerException bmsがnull
	 * @exception IllegalStateException BMS仕様が設定されていない
	 * @exception IllegalStateException BMS読み込みハンドラが設定されていない
	 * @exception IOException 指定されたファイルが見つからない、読み取り権限がない、または読み取り中に異常を検出した
	 * @exception BmsLoadException ハンドラ({@link BmsLoadHandler#parseError})がfalseを返した
	 * @exception BmsException 読み込み処理中に想定外の例外がスローされた
	 */
	public final BmsContent load(File bms) throws BmsException, IOException {
		assertArgNotNull(bms, "bms");
		return load(bms.toPath());
	}

	/**
	 * BMSコンテンツを読み込みます。
	 * <p>BMSコンテンツは指定されたパスが示すファイルから読み込みます。</p>
	 * <p>読み込み処理の詳細は{@link #load(String)}を参照してください。</p>
	 * @param bms BMSファイルのパス
	 * @return BMSコンテンツ
	 * @exception NullPointerException bmsがnull
	 * @exception IllegalStateException BMS仕様が設定されていない
	 * @exception IllegalStateException BMS読み込みハンドラが設定されていない
	 * @exception IOException 指定されたファイルが見つからない、読み取り権限がない、または読み取り中に異常を検出した
	 * @exception BmsLoadException ハンドラ({@link BmsLoadHandler#parseError})がfalseを返した
	 * @exception BmsException 読み込み処理中に想定外の例外がスローされた
	 */
	public final BmsContent load(Path bms) throws BmsException, IOException {
		assertArgNotNull(bms, "bms");
		assertLoaderState();
		return load(Files.readAllBytes(bms));
	}

	/**
	 * BMSコンテンツを読み込みます。
	 * <p>BMSコンテンツは指定された入力ストリームから読み込みます。</p>
	 * <p>読み込み処理の詳細は{@link #load(String)}を参照してください。</p>
	 * @param bms BMSの入力ストリーム
	 * @return BMSコンテンツ
	 * @exception NullPointerException bmsがnull
	 * @exception IllegalStateException BMS仕様が設定されていない
	 * @exception IllegalStateException BMS読み込みハンドラが設定されていない
	 * @exception IOException 入力ストリームからのデータ読み取り中に異常を検出した
	 * @exception BmsLoadException ハンドラ({@link BmsLoadHandler#parseError})がfalseを返した
	 * @exception BmsException 読み込み処理中に想定外の例外がスローされた
	 */
	public final BmsContent load(InputStream bms) throws BmsException, IOException {
		assertArgNotNull(bms, "bms");
		assertLoaderState();
		return load(bms.readAllBytes());
	}

	/**
	 * BMSコンテンツを読み込みます。
	 * <p>BMSコンテンツは指定されたバイト配列から読み込みます。</p>
	 * <p>当メソッドではバイト配列は「文字コードが不明なテキスト」として扱います。
	 * より少ない工程で文字コードが特定できるように、最初にテキストにBOM(Byte Order Mark)が付与されているかを調べます。
	 * この工程で判明する文字コードは「UTF-8」「UTF-16LE」「UTF-16BE」のいずれかです。</p>
	 * <p>BOMによる文字コードの特定ができなかった場合、{@link #setCharsets(Charset...)}で指定された文字セット
	 * (未指定の場合は{@link BmsLibrary#getDefaultCharsets()}で取得できる文字セットリスト)
	 * で優先順にテキストのデコードを試行し、デコードがエラーなく完了するまで繰り返します。
	 * 全ての文字セットでデコードエラーが発生した場合、最優先文字セット(リストの先頭で指定された文字セット)
	 * でテキストの最後まで強制的にデコードします。当該文字セットで変換できなかった文字は代替文字で置き換えられますが、
	 * 代替文字の内容は未定義の値となります。つまり、文字化けと同義の状態となることに注意してください。</p>
	 * <p>より高速に読み込むには、読み込む想定のBMSコンテンツがどの文字セットでエンコードされていることが多いかを、
	 * アプリケーションごとに検討のうえ最適な優先順で文字セットリストを指定することが重要になります。</p>
	 * <p><strong>※注意</strong><br>
	 * 入力バイト配列に格納されたテキストの実際の文字コードとデコードする文字セットが異なっていれば、
	 * 必ずデコードが失敗するというわけではありません。例えばShift-JISのテキストをUTF-16LEでデコードした場合、
	 * デコード失敗が期待動作ですがテキストの内容次第ではデコードが成功することがあります。
	 * そのようなケースでは文字化けした文字列でBMS読み込みが行われてしまい、期待する結果が得られません。
	 * デコードの誤判定が発生しやすい文字セットは優先順位を下げるか、デコード対象に含まないようにしてください。</p>
	 * <p>テキストのデコード後の読み込み処理詳細は{@link #load(String)}を参照してください。</p>
	 * @param bms BMSのバイト配列
	 * @return BMSコンテンツ
	 * @exception NullPointerException bmsがnull
	 * @exception IllegalStateException BMS仕様が設定されていない
	 * @exception IllegalStateException BMS読み込みハンドラが設定されていない
	 * @exception BmsLoadException ハンドラ({@link BmsLoadHandler#parseError})がfalseを返した
	 * @exception BmsException 読み込み処理中に想定外の例外がスローされた
	 */
	public final BmsContent load(byte[] bms) throws BmsException {
		assertArgNotNull(bms, "bms");
		assertLoaderState();

		// BOMチェックを行い、文字セットを特定しようとする
		var charset = (Charset)null;
		var skipCount = 0;
		if ((bms.length >= 3) && (bms[0] == (byte)0xef) && (bms[1] == (byte)0xbb) && (bms[2] == (byte)0xbf)) {
			// UTF-8
			charset = StandardCharsets.UTF_8;
			skipCount = 3;
		} else if ((bms.length >= 2) && (bms[0] == (byte)0xff) && (bms[1] == (byte)0xfe)) {
			// UTF-16LE
			charset = StandardCharsets.UTF_16LE;
			skipCount = 2;
		} else if ((bms.length >= 2) && (bms[0] == (byte)0xfe) && (bms[1] == (byte)0xff)) {
			// UTF-16BE
			charset = StandardCharsets.UTF_16BE;
			skipCount = 2;
		} else {
			// BOMによる文字セットの特定は不可
			// Do nothing
		}

		// テキストのデコードを行う
		var bmsStr = (String)null;
		if (charset != null) {
			// BOMによる文字セット確定済み
			bmsStr = decodeText(bms, charset, skipCount, false);
		} else {
			// 文字セット未決の場合は優先文字セット順でのデコードを試みる
			// ローダに使用文字セットが指定されていなければBMSライブラリのデフォルト文字セットリストを使用する
			var charsets = !mCharsets.isEmpty() ? mCharsets : BmsLibrary.getDefaultCharsets();
			var numCharsets = charsets.size();
			var isErrorStop = (numCharsets > 1);  // 文字セット1件の場合はエラー停止なし
			for (var i = 0; (i < numCharsets) && (bmsStr == null); i++) {
				bmsStr = decodeText(bms, charsets.get(i), 0, isErrorStop);
			}

			// 全ての文字セットでデコードに失敗した場合は最優先文字セットで再デコードする
			// その際、デコードできない文字は代替文字で置換する
			if (bmsStr == null) {
				bmsStr = decodeText(bms, charsets.get(0), 0, false);
			}
		}

		// デコードされたテキストでBMSの解析を行う
		var content = load(bmsStr);

		return content;
	}

	/**
	 * BMSコンテンツを読み込みます。
	 * <p>当メソッドを使用してBMSを読み込む場合、入力のテキストデータはデコード処理が行われません。</p>
	 * @param bms BMSのReader
	 * @return BMSコンテンツ
	 * @exception NullPointerException bmsがnull
	 * @exception IllegalStateException BMS仕様が設定されていない
	 * @exception IllegalStateException BMS読み込みハンドラが設定されていない
	 * @exception IOException テキストの読み取り中に異常を検出した
	 * @exception BmsLoadException ハンドラ({@link BmsLoadHandler#parseError})がfalseを返した
	 * @exception BmsException 読み込み処理中に想定外の例外がスローされた
	 */
	public final BmsContent load(Reader bms) throws BmsException, IOException {
		assertArgNotNull(bms, "bms");
		assertLoaderState();
		var sb = new StringBuilder();
		var reader = new BufferedReader(bms);
		for (var line = reader.readLine(); line != null; line = reader.readLine()) {
			sb.append(line);
			sb.append("\n");
		}
		return loadMain(sb.toString());
	}

	/**
	 * BMSコンテンツを読み込みます。
	 * <p>当メソッドには入力元のBMSテキストを直接指定します。</p>
	 * <p>メタ情報解析について</p>
	 * <ul>
	 * <li>BMS仕様に規定されていない名称を検出した場合、エラーハンドラにて{@link BmsErrorType#UNKNOWN_META}が通知されます。</li>
	 * <li>メタ情報の値がBMS仕様に規定されたデータ型の記述書式に適合しない場合、エラーハンドラにて{@link BmsErrorType#WRONG_DATA}が通知されます。</li>
	 * </ul>
	 * <p>チャンネル解析について</p>
	 * <ul>
	 * <li>チャンネル番号がBMS仕様に未定義の場合、エラーハンドラにて{@link BmsErrorType#UNKNOWN_CHANNEL}が通知されます。</li>
	 * <li>重複可能チャンネルで空定義("00"のみの定義)の場合、空配列データとして読み込まれます。</li>
	 * <li>重複可能チャンネルの末尾側で空配列データが連続する場合、空でない定義以降から末尾までの連続する空配列データは読み込まれません。</li>
	 * <li>以下のケースを検出した場合、エラーハンドラにて{@link BmsErrorType#WRONG_DATA}が通知されます。
	 * <ul>
	 * <li>チャンネルに定義されたデータの記述書式がBMS仕様に違反している場合。</li>
	 * <li>データ重複許可チャンネルの同小節番号内にて{@link BmsSpec#CHINDEX_MAX}+1個を超えるデータ定義を検出した場合。</li>
	 * </ul></li>
	 * </ul>
	 * @param bms BMSの文字列
	 * @return BMSコンテンツ
	 * @exception NullPointerException bmsがnull
	 * @exception IllegalStateException BMS仕様が設定されていない
	 * @exception IllegalStateException BMS読み込みハンドラが設定されていない
	 * @exception BmsLoadException ハンドラ({@link BmsLoadHandler#parseError})がfalseを返した
	 * @exception BmsException 読み込み処理中に想定外の例外がスローされた
	 */
	public final BmsContent load(String bms) throws BmsException {
		assertArgNotNull(bms, "bms");
		assertLoaderState();
		return loadMain(bms);
	}

	/**
	 * 引数のBMSスクリプトを解析し、BMS仕様に従ってBMSコンテンツを生成する。
	 * @param bms Unicode文字列に変換されたBMSスクリプト
	 * @return 生成されたBMSコンテンツ
	 * @throws BmsException {@link #loadCore}参照
	 */
	private BmsContent loadMain(String bms) throws BmsException {
		var content = (BmsContent)null;
		try {
			// BMSコンテンツ読み込み開始
			mHandler.startLoad(mSettings);
			// BMSコンテンツ読み込み処理
			content = loadCore(bms);
		} catch (BmsException e) {
			// スローされた例外はそのまま読み出し元へ流す
			throw e;
		} catch (Exception e) {
			// スローされた例外はBmsExceptionに内包する
			throw new BmsException(e);
		}

		// BMSコンテンツ読み込み終了
		try {
			var result = mHandler.testContent(content);
			if (result == null) {
				var msg = "Content test result was returned null by handler";
				error(BmsErrorType.PANIC, 0, "", msg, null);
			} else switch (result.getResult()) {
			case BmsLoadHandler.TestResult.RESULT_OK:
			case BmsLoadHandler.TestResult.RESULT_THROUGH:
				// 検査失敗でなければ合格とする
				if (content.isEditMode()) {
					// BMSコンテンツが編集モードの場合はエラーとする
					throw new BmsException("Loaded content is edit mode");
				}
				break;
			case BmsLoadHandler.TestResult.RESULT_FAIL: {
				// BMSコンテンツ検査失敗
				error(BmsErrorType.TEST_CONTENT, 0, "", result.getMessage(), null);
				break;
			}
			default:
				// 想定外
				var msg = String.format("Content test result was returned '%d' by handler", result.getResult());
				var err = new BmsScriptError(BmsErrorType.PANIC, 0, "", msg, null);
				throw new BmsLoadException(err);
			}
		} catch (BmsException e) {
			throw e;
		} catch (Exception e) {
			throw new BmsException (e);
		}

		return content;
	}

	/**
	 * 引数のBMSスクリプトを解析し、BMS仕様に従ってBMSコンテンツを生成する。
	 * @param bms Unicode文字列に変換されたBMSスクリプト
	 * @return 生成されたBMSコンテンツ
	 * @exception IllegalStateException BMS仕様が設定されていない時
	 * @exception IllegalStateException ハンドラが設定されていない時
	 * @exception IllegalStateException BmsContentクリエータが設定されていない時
	 * @exception IllegalStateException BmsNoteクリエータが設定されていない時
	 * @exception NullPointerException 引数bmsがnullの時
	 * @exception BmsParseException エラーハンドラがfalseを返した時
	 * @exception BmsException 入出力エラー等、解析処理中にエラーが発生した時。causeが設定される場合がある。
	 */
	private BmsContent loadCore(String bms) throws BmsException {
		// 出力対象となるBmsContentを生成する
		var createdContent = (BmsContent)null;
		try {
			createdContent = mHandler.createContent(mSpec);
		} catch (Exception e) {
			var msg = String.format("Failed to create BmsContent by '%s'.", mHandler.getClass().getSimpleName());
			throw new BmsException(msg, e);
		}

		// 生成されたBMSコンテンツのチェック
		if (createdContent == null) {
			// BMSコンテンツが生成されなかった場合は続行不能
			var msg = String.format("null BmsContent is returned by '%s'.", mHandler.getClass().getSimpleName());
			throw new BmsException(msg);
		} else if (createdContent.getSpec() != mSpec) {
			// 生成されたBMSコンテンツのBMS仕様が指定したBMS仕様と異なる
			var msg = String.format("Created BmsContent has illegal BMS specification. It's created by '%s'.",
					mHandler.getClass().getSimpleName());
			throw new BmsException(msg);
		}

		final var content = createdContent;
		var activeParse = Optional.of(Void.TYPE);
		try {
			// BMS解析を開始する
			var beginError = beginParse(mSettings, bms);
			bms = null;
			if (beginError == null) {
				// BMS解析開始結果のエラー情報が未設定の場合は処理を続行しない
				throw new BmsException("Result of start parse is not returned.");
			} else if (beginError.fail()) {
				// BMS解析開始でエラーが検出された場合も処理を続行しない
				throw new BmsLoadException(beginError.error);
			} else {
				// Do nothing
			}

			// 入力データから全ての要素を取り出す
			content.beginEdit();
			var element = (ParsedElement)null;
			var dataList = new ArrayList<ChannelArrayData>(BmsSpec.SPEC_CHANNEL_MAX);
			while ((element = nextElement()) != null) {
				switch (element.mElementType) {
				case DECLARATION:  // BMS宣言
					parseDeclaration((DeclarationParsedElement)element, content);
					break;
				case META:  // メタ情報
					parseMeta((MetaParsedElement)element, content);
					break;
				case VALUE_CHANNEL:  // 値型チャンネル
					parseValueChannel((ValueChannelParsedElement)element, content);
					break;
				case ARRAY_CHANNEL:  // 配列型チャンネル
					parseArrayChannel((ArrayChannelParsedElement)element, dataList, content);
					break;
				case ERROR:  // エラー
					parseError((ErrorParsedElement)element);
					break;
				default:  // Don't care
					break;
				}
			}
			content.endEdit();

			// BMS解析を終了する
			activeParse = Optional.empty();
			var endError = endParse();
			if (endError == null) {
				// BMS解析終了結果のエラー情報が未設定の場合は処理を続行しない
				throw new BmsException("Result of start parse is not returned.");
			} else if (endError.fail()) {
				// BMS解析終了でエラーが検出された場合も処理を続行しない
				throw new BmsLoadException(endError.error);
			} else {
				// Do nothing
			}

			// 配列型のチャンネルデータをコンテンツに登録する
			// (配列型の要素登録先(tick)が小節の長さの影響を受けるため、一旦BMS全体を解析し切った後で登録する必要あり)
			var multiChCounts = new HashMap<MeasureChNumberKey, Integer>();
			content.beginEdit();
			for (ChannelArrayData data : dataList) {
				var chNumber = data.channel.getNumber();
				var chIndex = 0;
				var tickCount = content.getMeasureTickCount(data.measure);
				var count = data.array.size();

				// チャンネルデータの検査を行う
				var result = mHandler.testChannel(data.channel, chIndex, data.measure, data.array);
				if (result == null) {
					var msg = "Channel test result was returned null by handler";
					error(BmsErrorType.PANIC, data.lineNumber, data.line, msg, null);
				} else switch (result.getResult()) {
				case BmsLoadHandler.TestResult.RESULT_OK: {
					// 重複可能チャンネルの場合は次のインデックスへの登録を行う
					// 登録先インデックスの配列データ数が0個の場合そのインデックスは空データとなるが、それは意図した動作
					if (data.channel.isMultiple()) {
						var key = new MeasureChNumberKey(data.measure, chNumber);
						chIndex = multiChCounts.getOrDefault(key, 0);
						multiChCounts.put(key, BmsInt.box(chIndex + 1));
					}

					// データ配列をNoteとしてコンテンツに登録する
					// Tickの値は配列要素数, 配列インデックス値, 当該小節の刻み数から計算
					var occurError = false;
					var lastTick = -1.0;
					var tickRatio = tickCount / (double)count;
					var processedPos = 0;
					for (processedPos = 0; processedPos < count; processedPos++) {
						var i = processedPos;
						var value = data.array.get(i).intValue();
						if (value == 0) {
							// 値00はノート未存在として扱うため、ノート追記処理はスキップする
							continue;
						}

						// 同一刻み位置への上書きが発生しないかを確認する
						var tick = (double)i * tickRatio;
						try {
							if (tick == lastTick) {
								var msg = String.format("Occurred overwrite channel data. tick=%.16g", tick);
								throw new Exception(msg);
							}
						} catch (Exception e) {
							error(BmsErrorType.WRONG_DATA, data.lineNumber, data.line, null, e);
							occurError = true;
							break;
						}

						// ノートオブジェクトを生成する
						try {
							mNoteBridge.mNote = mHandler.createNote();
							if (mNoteBridge.mNote == null) {
								// ノートオブジェクト生成でnullを返した場合は続行不可
								var msg = String.format("New note object is returned null by '%s'.",
										mHandler.getClass().getSimpleName());
								throw new BmsException(msg);
							}
						} catch (BmsException e) {
							throw e;
						} catch (Exception e) {
							var msg = String.format("Failed to create note object: %s", e);
							throw new BmsException(msg, e);
						}

						// ノートを追記する
						try {
							content.putNote(chNumber, chIndex, data.measure, tick, value, mNoteBridge);
						} catch (Exception e) {
							// データ不備により例外発生時はデータ不正とする
							error(BmsErrorType.WRONG_DATA, data.lineNumber, data.line, null, e);
							occurError = true;
							break;
						}
					}

					// エラー発生時は登録途中のデータ配列を消去する
					if (occurError) {
						for (var i = 0; i < processedPos; i++) {
							var tick = (double)i * tickRatio;
							var value = data.array.get(i).intValue();
							if (value != 0) {
								content.removeNote(chNumber, chIndex, data.measure, tick);
							}
						}
					}

					break;
				}
				case BmsLoadHandler.TestResult.RESULT_FAIL:
					// 検査に失敗した場合はエラーとし、ノートの登録処理は省略しようとする
					error(BmsErrorType.TEST_CHANNEL, data.lineNumber, data.line, result.getMessage(), null);
					break;
				case BmsLoadHandler.TestResult.RESULT_THROUGH:
					// 解析したチャンネルデータを破棄する
					break;
				default:
					// 不明なエラー
					var msg = String.format("Channel test result was returned '%d' by handler", result.getResult());
					error(BmsErrorType.PANIC, data.lineNumber, data.line, msg, null);
					break;
				}
			}
			content.endEdit();
		} catch (BmsException e) {
			throw e;
		} catch (Exception e) {
			var msg = String.format("Caught un-expected exception: %s", e);
			throw new BmsException(msg, e);
		} finally {
			activeParse.ifPresent(x -> endParse());
		}

		// 正常終了
		return content;
	}

	/**
	 * BMS宣言を解析する
	 * @param element BMS宣言要素
	 * @param content BMSコンテンツ
	 * @exception BmsException エラーハンドラがfalseを返した時
	 */
	private void parseDeclaration(DeclarationParsedElement element, BmsContent content) throws BmsException {
		var lineNumber = element.lineNumber;
		var line = element.line;
		var key = element.key;
		var value = element.value;
		var result = mHandler.testDeclaration(key, value);
		if (result == null) {
			var msg = "BMS declaration test result was returned null by handler";
			error(BmsErrorType.PANIC, lineNumber, line, msg, null);
		} else switch (result.getResult()) {
		case BmsLoadHandler.TestResult.RESULT_OK:
			// BMS宣言の検査合格時はコンテンツに登録する
			content.addDeclaration(key, value);
			break;
		case BmsLoadHandler.TestResult.RESULT_FAIL:
			// BMS宣言の検査失敗時はコンテンツに登録せずにエラー処理に回す
			error(BmsErrorType.TEST_DECLARATION, 1, line, result.getMessage(), null);
			break;
		case BmsLoadHandler.TestResult.RESULT_THROUGH:
			// BMS宣言を破棄する
			break;
		default:
			// 想定外
			var msg = String.format("BMS declaration test result was returned '%d' by handler",
					result.getResult());
			error(BmsErrorType.PANIC, 1, line, msg, null);
			break;
		}
	}

	/**
	 * メタ情報を解析する
	 * @param element メタ情報要素
	 * @param content BMSコンテンツ
	 * @exception BmsException エラーハンドラがfalseを返した時
	 */
	private void parseMeta(MetaParsedElement element, BmsContent content) throws BmsException {
		// 値を取り出す
		var lineNumber = element.lineNumber;
		var line = element.line;
		var meta = element.meta;
		var index = element.index;
		var value = Objects.requireNonNullElse(element.value, "");

		// パーサ部から返されたメタ情報を検証する
		if (meta == null) {
			// メタ情報が未設定の場合はエラーとして扱う
			var msg = String.format("Parser '%s' is returned null meta.", getClass().getSimpleName());
			error(BmsErrorType.PANIC, lineNumber, line, msg, null);
			return;
		}
		if (meta.getUnit() == BmsUnit.INDEXED) {
			// 索引付きメタ情報の場合はインデックス値の範囲チェックを行う
			if ((index < 0) || (index > BmsSpec.INDEXED_META_INDEX_MAX)) {
				var msg = String.format("Index out of range. [index=%d]", index);
				error(BmsErrorType.WRONG_DATA, lineNumber, line, msg, null);
				return;
			}
		} else {
			// 索引付き以外ではインデックス値は0でなければならない
			if (index != 0) {
				var msg = String.format("At %s unit, index must be 0. [index=%d]", meta.getUnit(), index);
				error(BmsErrorType.WRONG_DATA, lineNumber, line, msg, null);
				return;
			}
		}

		// 定義値が当該メタ情報の規定データ型に適合するか
		var type = meta.getType();
		if (!type.test(value)) {
			// データの記述内容が適合しない
			var msg = "Type mismatch meta value";
			error(BmsErrorType.WRONG_DATA, lineNumber, line, msg, null);
			return;
		}

		// 定義値の仕様違反を検査する
		var obj = type.cast(value);
		if (meta.isInitialBpm() || meta.isReferenceBpm()) {
			// 初期BPMまたはBPM変更メタ情報
			var bpm = ((Number)obj).doubleValue();
			if (!BmsSpec.isBpmWithinRange(bpm)) {
				if (mIsFixSpecViolation) {
					obj = Math.min(BmsSpec.BPM_MAX, Math.max(BmsSpec.BPM_MIN, bpm));
				} else {
					var msg = "This BPM is spec violation of BMS library";
					error(BmsErrorType.SPEC_VIOLATION, lineNumber, line, msg, null);
					return;
				}
			}
		}
		if (meta.isReferenceStop()) {
			// 譜面停止時間
			var stop = ((Number)obj).doubleValue();
			if (!BmsSpec.isStopWithinRange(stop)) {
				if (mIsFixSpecViolation) {
					obj = Math.min(BmsSpec.STOP_MAX, Math.max(BmsSpec.STOP_MIN, stop));
				} else {
					var msg = "This stop time is spec violation of BMS library";
					error(BmsErrorType.SPEC_VIOLATION, lineNumber, line, msg, null);
					return;
				}
			}
		}

		// メタ情報を検査する
		var name = meta.getName();
		var unit = meta.getUnit();
		var result = BmsLoadHandler.TestResult.FAIL;
		switch (unit) {
		case SINGLE:
		case INDEXED: {
			// ユーザーによる検査処理
			result = mHandler.testMeta(meta, index, obj);

			// 再定義不許可の状態で再定義を検出した場合はエラーとする
			if ((result != null) && (result.getResult() == BmsLoadHandler.TestResult.RESULT_OK) &&
					!mIsAllowRedefine && content.containsMeta(meta, index)) {
				error(BmsErrorType.REDEFINE, lineNumber, line, "Re-defined meta", null);
				return;
			}

			break;
		}
		case MULTIPLE: {
			// ユーザーによる検査処理
			result = mHandler.testMeta(meta, content.getMultipleMetaCount(name), obj);
			break;
		}
		default:
			break;
		}
		if (result == null) {
			var msg = "Meta test result was returned null by handler";
			error(BmsErrorType.PANIC, lineNumber, line, msg, null);
		} else switch (result.getResult()) {
		case BmsLoadHandler.TestResult.RESULT_OK:
			// メタ情報をBMSコンテンツに登録する
			switch (unit) {
			case SINGLE: content.setSingleMeta(name, obj); break;
			case MULTIPLE: content.putMultipleMeta(name, obj); break;
			case INDEXED: content.setIndexedMeta(name, index, obj); break;
			default: break;
			}
			break;
		case BmsLoadHandler.TestResult.RESULT_FAIL:
			// 検査不合格
			error(BmsErrorType.TEST_META, lineNumber, line, result.getMessage(), null);
			break;
		case BmsLoadHandler.TestResult.RESULT_THROUGH:
			// メタ情報破棄
			break;
		default:
			// 想定外
			var msg = String.format("Meta test result was returned '%d' by handler", result.getResult());
			error(BmsErrorType.PANIC, lineNumber, line, msg, null);
			break;
		}
	}

	/**
	 * 値型チャンネルを解析する
	 * @param element 値型チャンネル要素
	 * @param content BMSコンテンツ
	 * @exception BmsException エラーハンドラがfalseを返した時
	 */
	private void parseValueChannel(ValueChannelParsedElement element, BmsContent content) throws BmsException {
		// タイムライン読み込みをスキップする場合は何もしない
		if (mIsSkipReadTimeline) {
			return;
		}

		// 小節番号、チャンネル等の値を取り出す
		var lineNumber = element.lineNumber;
		var line = element.line;
		var measure = element.measure;
		var channelNum = element.number;
		var value = element.value;

		// 解析結果の適合チェック
		var channel = mSpec.getChannel(channelNum);
		if (channel == null) {
			// 該当するチャンネルが仕様として規定されていない
			var msg = String.format("'%s' No such channel in spec", BmsInt.to36s(channelNum));
			error(BmsErrorType.UNKNOWN_CHANNEL, lineNumber, line, msg, null);
			return;
		}
		if (!BmsSpec.isMeasureWithinRange(measure)) {
			// 小節番号が不正
			var msg = String.format("Measure is out of range", measure);
			error(BmsErrorType.WRONG_DATA, lineNumber, line, msg, null);
			return;
		}

		// 解析したデータの登録処理
		var channelType = channel.getType();
		if (channelType.isValueType()) {
			// 小節データの型変換を行う
			var object = (Object)null;
			try {
				object = channelType.cast(value);
			} catch (Exception e) {
				error(BmsErrorType.WRONG_DATA, lineNumber, line, null, e);
				return;
			}

			// 定義値の仕様違反を検査する
			if (channel.isLength()) {
				// 小節長の仕様違反を検査する
				var length = ((Number)object).doubleValue();
				if (!BmsSpec.isLengthWithinRange(length)) {
					if (mIsFixSpecViolation) {
						object = Math.min(BmsSpec.LENGTH_MAX, Math.max(BmsSpec.LENGTH_MIN, length));
					} else {
						var msg = "This length is spec violation of BMS library";
						error(BmsErrorType.SPEC_VIOLATION, lineNumber, line, msg, null);
						return;
					}
				}
			}

			// チャンネルデータの検査を行う
			var chIndex = content.getChannelDataCount(channelNum, measure);
			var result = mHandler.testChannel(channel, chIndex, measure, object);
			if (result == null) {
				var msg = "Channel test result was returned null by handler";
				error(BmsErrorType.PANIC, lineNumber, line, msg, null);
				return;
			}

			// 重複不可チャンネルの重複チェックを行う
			if ((result == BmsLoadHandler.TestResult.OK) && (chIndex > 0) && !channel.isMultiple()) {
				if (mIsAllowRedefine) {
					// 再定義が許可されている場合は上書きするようにする
					chIndex = 0;
				} else {
					// 上書き不許可・重複不可・再定義検出の条件が揃った場合はエラーとする
					error(BmsErrorType.REDEFINE, lineNumber, line, "Re-defined value type channel", null);
					return;
				}
			}

			// チャンネルデータの検査結果を判定する
			switch (result.getResult()) {
			case BmsLoadHandler.TestResult.RESULT_OK:
				try {
					// 小節データの空き領域にチャンネルデータを登録する
					content.setMeasureValue(channelNum, chIndex, measure, object);
				} catch (Exception e) {
					// 何らかのエラーが発生した場合はデータの不備
					error(BmsErrorType.WRONG_DATA, lineNumber, line, null, e);
				}
				break;
			case BmsLoadHandler.TestResult.RESULT_FAIL:
				// 検査不合格
				error(BmsErrorType.TEST_CHANNEL, lineNumber, line, result.getMessage(), null);
				break;
			case BmsLoadHandler.TestResult.RESULT_THROUGH:
				// チャンネルデータを破棄する
				break;
			default:
				// 想定外
				var msg = String.format("Channel test result was returned '%d' by handler", result.getResult());
				error(BmsErrorType.PANIC, lineNumber, line, msg, null);
				break;
			}
		} else {
			// 値型チャンネル要素で配列型チャンネルを返されても処理しない
			var msg = String.format("Number %d this channel is array type", channelNum);
			error(BmsErrorType.PANIC, lineNumber, line, msg, null);
			return;
		}
	}

	/**
	 * 配列型チャンネルを解析する
	 * <p>配列型のデータは一旦リストに退避する。配列型のデータの譜面上の配置は小節の長さの影響を受けるが、
	 * 小節の長さを確定できるのは単一型のデータを完全に解析した後であるため、
	 * 当メソッドでは配列型のデータはBMSコンテンツには直接登録しない。</p>
	 * @param element 配列型チャンネル要素
	 * @param dataList 解析済みチャンネル定義データ
	 * @param content BMSコンテンツ
	 * @exception BmsException エラーハンドラがfalseを返した時
	 */
	private void parseArrayChannel(ArrayChannelParsedElement element, List<ChannelArrayData> dataList,
			BmsContent content) throws BmsException {
		// タイムライン読み込みをスキップする場合は何もしない
		if (mIsSkipReadTimeline) {
			return;
		}

		// 小節番号、チャンネル、等の値を取り出す
		var lineNumber = element.lineNumber;
		var line = element.line;
		var measure = element.measure;
		var channelNum = element.number;
		var array = element.array;

		// チャンネルの取得とデータ型の適合チェック
		var channel = mSpec.getChannel(channelNum);
		if (channel == null) {
			// 該当するチャンネルが仕様として規定されていない
			var msg = String.format("'%s' No such channel in spec", BmsInt.to36s(channelNum));
			error(BmsErrorType.UNKNOWN_CHANNEL, lineNumber, line, msg, null);
			return;
		}
		if (!BmsSpec.isMeasureWithinRange(measure)) {
			// 小節番号が不正
			var msg = String.format("Measure is out of range", measure);
			error(BmsErrorType.WRONG_DATA, lineNumber, line, msg, null);
			return;
		}

		// 解析したデータの登録処理
		var channelType = channel.getType();
		if (channelType.isArrayType()) {
			// 配列型の場合
			// チャンネルデータの検査を行う
			var chIndex = content.getChannelDataCount(channelNum, measure);
			var result = mHandler.testChannel(channel, chIndex, measure, array);
			if (result == null) {
				var msg = "Channel test result was returned null by handler";
				error(BmsErrorType.PANIC, lineNumber, line, msg, null);
				return;
			}

			// チャンネルデータの検査結果を判定する
			switch (result.getResult()) {
			case BmsLoadHandler.TestResult.RESULT_OK: {
				// 解析済みデータを生成し、小節番号・チャンネルとそのデータの場所(行番号)を覚えておく
				ChannelArrayData data = new ChannelArrayData();
				data.lineNumber = lineNumber;
				data.line = line;
				data.channel = channel;
				data.measure = measure;
				data.array = array;
				dataList.add(data);
				break;
			}
			case BmsLoadHandler.TestResult.RESULT_FAIL:
				// 検査不合格
				error(BmsErrorType.TEST_CHANNEL, lineNumber, line, result.getMessage(), null);
				break;
			case BmsLoadHandler.TestResult.RESULT_THROUGH:
				// チャンネルデータを破棄する
				break;
			default:
				// 想定外
				var msg = String.format("Channel test result was returned '%d' by handler", result.getResult());
				error(BmsErrorType.PANIC, lineNumber, line, msg, null);
				break;
			}
		} else {
			// 配列型チャンネル要素で値型チャンネルを返されても処理しない
			var msg = String.format("Number %d this channel is value type", channelNum);
			error(BmsErrorType.PANIC, lineNumber, line, msg, null);
		}
	}

	/**
	 * エラーを解析する
	 * <p>ローダの解析部から報告されたエラーをハンドラに通知する。</p>
	 * @param element エラー要素
	 * @exception BmsException エラーハンドラがfalseを返した時
	 */
	private void parseError(ErrorParsedElement element) throws BmsException {
		var err = element.error;
		error(err.getType(), err.getLineNumber(), err.getLine(), err.getMessage(), err.getCause());
	}

	/**
	 * エラー発生時の処理
	 * @param errType エラー種別
	 * @param lineNumber 行番号
	 * @param line 行文字列
	 * @param message エラーメッセージ
	 * @param throwable 発生した例外
	 * @exception BmsLoadException エラーハンドラがfalseを返した時
	 */
	private void error(BmsErrorType errType, int lineNumber, Object line, String message, Throwable throwable)
			throws BmsLoadException {
		// エラーが無効にされている場合は例外をスローしない
		var fnIsOccur = mOccurErrorMap.get(errType);
		if ((fnIsOccur != null) && !fnIsOccur.getAsBoolean()) {
			return;
		}

		// エラーハンドラにエラー内容を通知する
		var lineStr = (line == null) ? "" : line.toString();
		var error = new BmsScriptError(errType, lineNumber, lineStr, message, throwable);
		if (!mHandler.parseError(error)) {
			// BMS解析を中断する場合は例外を投げる
			throw new BmsLoadException(error);
		}
	}

	/**
	 * テキストのデコード処理
	 * @param inRaw デコード対象テキスト
	 * @param cs デコード文字セット
	 * @param top デコード対象テキストの先頭位置
	 * @param isErrorStop エラー発生時、処理を停止するかどうか
	 * @return デコード後テキスト。エラー停止ONでエラー発生時はnull。
	 */
	private String decodeText(byte[] inRaw, Charset cs, int top, boolean isErrorStop) {
		// 指定文字セットのデコーダと入力データをセットアップする
		var in = ByteBuffer.wrap(inRaw, top, inRaw.length - top);
		var action = isErrorStop ? CodingErrorAction.REPORT : CodingErrorAction.REPLACE;
		var decoder = cs.newDecoder()
				.onMalformedInput(action)
				.onUnmappableCharacter(action);

		// デコード処理
		// デコード後のテキストは、バッファの再割り当てが起こりにくいような現実的なサイズを指定する。
		// 基本的にASCII文字以外が用いられるのはメタ情報のみであることがほとんどのため入力バッファの10～20%だけ
		// 大きめのサイズを初期キャパシティとして割り当てておく。ただし、際限なくデコード後バッファが肥大化しても困るので
		// キャパシティの上限は定めておき、OutOfMemoryErrorが発生しないようにしておく。
		final var MAX_INITIAL_CAPACITY = 1 * 1024 * 1024;  // 1M文字
		var capacity = (int)Math.min((inRaw.length * 1.2), MAX_INITIAL_CAPACITY);
		var decodedText = new StringBuilder(capacity);
		var processing = true;
		while (processing) {
			mOutBuffer.position(0);
			var result = decoder.decode(in, mOutBuffer, true);
			if (result.isError()) {
				// デコード結果にエラーがある場合は処理を中断してnullを返す
				// エラー停止指示がONの場合のみ、このケースに入ることを想定している
				return null;
			} else {
				// デコードした分のテキストをデコード済みバッファへ積み上げる
				decodedText.append(mOutBuffer.array(), 0, mOutBuffer.position());
				processing = !result.isUnderflow();
			}
		}

		// 全テキストのデコード完了
		return decodedText.toString();
	}

	/**
	 * ローダの状態アサーション
	 * @exception IllegalStateException BMS仕様が設定されていない
	 * @exception IllegalStateException BMS読み込みハンドラが設定されていない
	 */
	private void assertLoaderState() {
		assertField(mSpec != null, "BmsSpec is NOT specified.");
		assertField(mHandler != null, "BMS load handler is NOT specified.");
	}

	protected final BmsLoaderSettings getSettings() {
		return mSettings;
	}

	/**
	 * BMSの解析処理開始を通知します。
	 * <p><strong>※当メソッドはBMSライブラリの一般利用者が参照する必要はありません。</strong></p>
	 * <p>当メソッドが呼ばれた時点でBMSローダが持つパーサ部を初期化することを求めます。
	 * 入力引数でローダの設定と解析対象のテキストが通知されるので、パーサ部の動作に必要な初期化処理を行ってください。</p>
	 * <p>パーサ部の初期化完了後に{@link #nextElement()}が呼び出され、BMSコンテンツの各構成要素を返すモードに遷移します。
	 * しかし、当メソッドの実行で以下の条件のいずれかを満たすと、パーサ部の初期化エラーと見なしBMS読み込みは中止され
	 * {@link BmsException}がスローされます。</p>
	 * <ul>
	 * <li>戻り値でnullを返した</li>
	 * <li>戻り値でエラー({@link ErrorParsedElement#fail()}がtrueになるオブジェクト)を返した({@link BmsLoadException})</li>
	 * <li>当メソッドから実行時例外がスローされた</li>
	 * <li>当メソッドから意図的に{@link BmsException}をスローした</li>
	 * </ul>
	 * <p>当メソッドが呼ばれると、上記の実行結果に関わらず{@link #endParse()}が必ず呼び出されます。</p>
	 * @param settings ローダの設定
	 * @param source 解析対象のテキスト
	 * @return 初期化結果を表すエラー情報要素
	 * @exception BmsException 解析処理開始時に続行不可能なエラーが発生した
	 */
	protected abstract ErrorParsedElement beginParse(BmsLoaderSettings settings, String source) throws BmsException;

	/**
	 * BMSの解析処理終了を通知します。
	 * <p><strong>※当メソッドはBMSライブラリの一般利用者が参照する必要はありません。</strong></p>
	 * <p>当メソッドは{@link #beginParse(BmsLoaderSettings, String)}が呼ばれると、その実行の成否に関わらず必ず呼ばれます。
	 * BMSローダのパーサ部が使用したリソースを確実に解放する契機を確保するためです。</p>
	 * <p>パーサ部の初期化途中で実行時例外がスローされ、初期化が中途半端な状態で当メソッドが実行される可能性がありますので、
	 * それを踏まえたうえで当メソッドの処理を実装するようにしてください。</p>
	 * <p>当メソッドの実行で以下の条件のいずれかを満たすと読み込まれたBMSコンテンツは破棄され{@link BmsException}
	 * がスローされます。</p>
	 * <ul>
	 * <li>戻り値でnullを返した</li>
	 * <li>戻り値でエラー({@link ErrorParsedElement#fail()}がtrueになるオブジェクト)を返した({@link BmsLoadException})</li>
	 * <li>当メソッドから実行時例外がスローされた</li>
	 * </ul>
	 * <p>パーサ部の初期化エラー後に当メソッドが呼ばれ実行時例外がスローされた場合、BMSローダは未定義の動作となります。
	 * 当メソッドは極力、実行時例外がスローされる契機がないよう実装してください。</p>
	 * @return 終了処理結果を表すエラー情報要素
	 */
	protected abstract ErrorParsedElement endParse();

	/**
	 * BMSの解析によって得られたBMSコンテンツの要素を1件返します。
	 * <p><strong>※当メソッドはBMSライブラリの一般利用者が参照する必要はありません。</strong></p>
	 * <p>当メソッドは{@link #beginParse(BmsLoaderSettings, String)}によるパーサ部の初期化処理が正常に終了した後、
	 * BMSローダによって連続で呼び出されます。パーサ部は当メソッドが呼び出される度に、
	 * 解析によって得られた要素を順次返すように実装してください。
	 * 全ての要素を返した後、nullを返すことで要素の抽出処理を終了することができます。</p>
	 * <p>要素を返す順番は問いません。BMSローダによって適切にBMSコンテンツへの登録が行われます。
	 * また、要素の正当性はBMSローダによってチェックされますのでパーサ部でチェックを行う必要はありません。
	 * 要素の内容に問題があれば自動的にBMS読み込みハンドラへ然るべき通知が行われます。
	 * ただし、構文エラーなどのような要素の正当性に関連しないエラーはパーサ部でチェックするようにしてください。</p>
	 * <p>解析の過程でエラーが検出された場合、BMSコンテンツへ登録する要素ではなくエラー要素({@link ErrorParsedElement})
	 * を返してください。エラー要素はBMSローダによってBMS読み込みハンドラの{@link BmsLoadHandler#parseError(BmsScriptError)}
	 * へ通知されます。また、1つの要素で複数のエラーが発生した場合、当メソッドの呼び出し毎に発生したエラーの要素を全て返してください。</p>
	 * <p>当メソッドで実行時例外がスローされた場合BMSローダによってキャッチされ、{@link BmsException}がスローされます。
	 * 意図的に{@link BmsException}をスローした場合、その例外がそのまま呼び出し元へスローされます。</p>
	 * @return BMSコンテンツの要素、またはエラー要素。これ以上要素がない場合はnull。
	 * @exception BmsException 処理中に続行不可能なエラーが発生した
	 * @see DeclarationParsedElement
	 * @see MetaParsedElement
	 * @see ChannelParsedElement
	 * @see ErrorParsedElement
	 */
	protected abstract ParsedElement nextElement() throws BmsException;
}
