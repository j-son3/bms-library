package com.lmt.lib.bms;

import static com.lmt.lib.bms.BmsAssertion.*;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.regex.Pattern;

/**
 * 外部データからBMSコンテンツを生成するローダーです。
 *
 * <p>BMSライブラリは、「外部データ」からBMSコンテンツを読み込む機能を提供します。ここで言う「外部データ」とは、
 * BMSで記述されたテキストデータのことを指します。外部データはファイル等の何らかの形式で記録されていることを
 * 想定しており、Java言語の{@link java.io.InputStream}で読み取ることができるもの全てを指します。</p>
 *
 * <p><strong>外部データの記述形式について</strong><br>
 * 外部データは上述の通り、BMSで記述されたテキストデータです。一般的には「BMSファイル」のことを指します。
 * BMSライブラリで読み取り可能な記述形式は一般的なBMSの仕様を取り込んでいますが、一部独自の機能を盛り込んでいます。
 * 詳細については以下を参照してください。</p>
 *
 * <p><strong>BMS宣言</strong><br>
 * BMSの1行目において、&quot;;?bms&nbsp;&quot;で始まるコメントを検出した場合、1行目をBMS宣言として解析しようとします。
 * BMS宣言の構文に誤りがある場合、BMS宣言は通常のコメントとして認識されるようになり、BMS宣言の無いBMSコンテンツとして
 * 読み込まれます。BMS宣言の記述例は以下の通りです。</p>
 *
 * <pre>
 * ;?bms encoding=&quot;UTF-8&quot; rule=&quot;BM&quot;</pre>
 *
 * <p><strong>メタ情報</strong><br>
 * BMSの中で「ヘッダ」と呼ばれる宣言をメタ情報として解析します。メタ情報は&quot;#&quot;または&quot;%&quot;で始まる
 * 行が該当します。ただし、BMS仕様で規定されていない名称のメタ情報はBMSコンテンツの情報としては読み込まれません。
 * それらの行は無視されるか、解析エラーとして報告されます。<br>
 * メタ情報はBMSのどこで宣言されていても問題無く読み込むことができます(チャンネルを記述した後でもOK)。
 * ただし、一般的にはメタ情報を全て宣言した後でチャンネルを記述することがほとんどのようです。<br>
 * メタ情報の記述例は以下の通りです。</p>
 *
 * <pre>
 * #GENRE J-POP
 * #TITLE My Love Song
 * #BPM 120
 * %URL http://www.lm-t.com/
 * %MYMETA 00AA00GG00ZZ</pre>
 *
 * <p><strong>チャンネル</strong><br>
 * 数字3文字＋36進数2文字＋&quot;:&quot;で始まる行は「チャンネル」として解析します。最初の数字は小節番号、
 * 続く36進数はチャンネル番号を表し、&quot;:&quot;の後の記述は当該小節・チャンネルのデータを表します。
 * チャンネルデータの記述形式はBMS仕様によって定められているため、仕様に違反する記述をした場合、当該チャンネルは
 * 解析エラーとして報告されます。<br>
 * 一般的にチャンネルは小節番号の若い順で記述されますが、小節番号は前後しても構いません。ただし、BMSの可読性が
 * 著しく低下するので小節番号順に記述することが推奨されます。<br>
 * チャンネルの記述例は以下の通りです。</p>
 *
 * <pre>
 * 01002:1.5
 * 0880A:00AB00CD00EF00GH
 * 123ZZ:String data channel</pre>
 *
 * <p><strong>エラーハンドリングについて</strong><br>
 * 通常、BMSの読み込みにおいてエラー行を検出した場合はその行を無視し、読み込みを続行することがほとんどです。
 * アプリケーションによっては、特定のエラーを検出した場合例外的に読み込みを中止し、読み込みエラーとして扱いたい
 * ケースがあります。そのような場合は{@link #setHandler(BmsLoadHandler)}でハンドラを登録し、発生したエラーに応じて
 * 読み込みの続行・中止を選択することが	できます。発生したエラーを蓄積し、エラーをユーザーに報告するようなケースも
 * エラーハンドリングを行うことで解決できます。詳細は{@link BmsLoadHandler}を参照してください。</p>
 *
 * <p><strong>拡張したBMSコンテンツオブジェクトを読み込みたい場合</strong><br>
 * アプリケーションによっては拡張したBMSコンテンツオブジェクト({@link BmsContent})を読み込みたいケースが存在します。
 * {@link BmsLoader}では読み込み時に、生成するBMSコンテンツオブジェクトを決定することができるようになっています。
 * {@link #setHandler(BmsLoadHandler)}で設定したハンドラの{@link BmsLoadHandler#createContent(BmsSpec)}を
 * オーバーライドすることによりアプリケーションで独自拡張したBMSコンテンツオブジェクトを返すことができます。</p>
 *
 * <p><strong>BOM付きUTF-8で記録されたBMSの読み込みについて</strong><br>
 * 外部データにBOM(Byte Order Mark)が付与された場合でも正常に読み込むことはできますが、現バージョンのBMSライブラリで
 * 読み込めるのはリトルエンディアンの場合のみです。ビッグエンディアンで記録された外部データはサポートしていません。</p>
 *
 * @see BmsLoadHandler
 * @see BmsLoadError
 */
public final class BmsLoader {
	/** BMS宣言1行分 */
	private static final Pattern SYNTAX_BMS_DECLARATION_ALL = Pattern.compile(
			"^[ \\t]*;\\?bms(([ \\t]+([a-zA-Z_][a-zA-Z0-9_]*)=\\\"([^\\\"]*)\\\")*)[ \\t]*$");
	/** BMS宣言の項目抽出用 */
	private static final Pattern SYNTAX_BMS_DECLARATION_ELEMENT = Pattern.compile(
			"[ \\t]+([a-zA-Z_][a-zA-Z0-9_]*)=\\\"([^\\\"]*)\\\"");
	/** 空白の正規表現パターン */
	private static final Pattern SYNTAX_BLANK = Pattern.compile(
			"^[ \\t]*$");
	/** 単一行コメントの正規表現パターン */
	private static final Pattern SYNTAX_SINGLELINE_COMMENT = Pattern.compile(
			"^[ \\t]*(\\*|;|//).*$");
	/** 複数行コメント開始の正規表現パターン */
	private static final Pattern SYNTAX_MULTILINE_COMMENT_BEGIN = Pattern.compile(
			"^[ \\t]*/\\*.*$");
	/** 複数行コメント終了の正規表現パターン */
	private static final Pattern SYNTAX_MULTILINE_COMMENT_END = Pattern.compile(
			"^(.*?)\\*/(.*)$");
	/** メタ情報定義の正規表現パターン */
	private static final Pattern SYNTAX_DEFINE_META = Pattern.compile(
			"^[ \\t]*((#|%)[^ \\t]*)(([ \\t]+)(.*?))?([ \\t]*)$");
	/** チャンネル定義の正規表現パターン */
	private static final Pattern SYNTAX_DEFINE_CHANNEL = Pattern.compile(
			"^[ \\t]*#([0-9]{3})([a-zA-Z0-9]{2}):(.*)$");

	/** BMSローダのデフォルトハンドラです。具体的な振る舞いは{@link BmsLoadHandler}を参照してください。 */
	public static final BmsLoadHandler DEFAULT_HANDLER = new BmsLoadHandler() {};

	/**
	 * ローダーの設定を参照するクラス。
	 */
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
	}

	/**
	 * チャンネルデータのうち、配列データを一時的にプールするための1データ要素。
	 */
	private static class ChannelArrayData {
		/** この配列データが登場した行番号 */
		int lineNumber;
		/** 解析した行テキスト */
		String line;
		/** 対象のチャンネル番号 */
		BmsChannel channel;
		/** 対象の小節番号 */
		int measure;
		/** int配列に変換された配列データ */
		BmsArray array;
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
	/** 不明メタ情報を無視するかどうか */
	private boolean mIsIgnoreUnknownMeta = true;
	/** 不明チャンネルを無視するかどうか */
	private boolean mIsIgnoreUnknownChannel = true;
	/** 不正データを無視するかどうか */
	private boolean mIsIgnoreWrongData = true;
	/** ノートオブジェクト橋渡し用クラス */
	private NoteBridge mNoteBridge = new NoteBridge();
	/** エラー発生有無マップ */
	private Map<BmsLoadError.Kind, BooleanSupplier> mOccurErrorMap;

	/**
	 * BmsLoaderオブジェクトを構築します。
	 */
	public BmsLoader() {
		mOccurErrorMap = Map.ofEntries(
				Map.entry(BmsLoadError.Kind.SYNTAX, () -> mIsEnableSyntaxError),
				Map.entry(BmsLoadError.Kind.LIBRARY_SPEC_VIOLATION, () -> !mIsFixSpecViolation),
				Map.entry(BmsLoadError.Kind.UNKNOWN_META, () -> !mIsIgnoreUnknownMeta),
				Map.entry(BmsLoadError.Kind.UNKNOWN_CHANNEL, () -> !mIsIgnoreUnknownChannel),
				Map.entry(BmsLoadError.Kind.WRONG_DATA, () -> !mIsIgnoreWrongData),
				Map.entry(BmsLoadError.Kind.COMMENT_NOT_CLOSED, () -> mIsEnableSyntaxError),
				Map.entry(BmsLoadError.Kind.FAILED_TEST_CONTENT, () -> mIsEnableSyntaxError));
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
	 * 構文エラーの有効状態を設定します。
	 * <p>BMSでは、メタ情報・チャンネル定義以外の記述は全て無視される仕様になっています。BMS内での不要・不正な記述を
	 * 防ぐために、メタ情報・チャンネル定義と認識されない全ての記述を構文エラーとしたい場合に当メソッドを使用します。</p>
	 * <p>構文エラーを有効にした状態でメタ情報・チャンネル定義以外の記述を検出すると、BMS読み込みハンドラにて解析エラーを
	 * 通知するようになります。通知メソッドから解析中止が返されるとBMS解析はエラーとなります。</p>
	 * <p>この設定は、複数行コメントが終了しない状態でBMSの読み込みが終了した場合({@link BmsLoadError.Kind#COMMENT_NOT_CLOSED})、
	 * および{@link #setHandler(BmsLoadHandler)}で設定したハンドラの{@link BmsLoadHandler#testContent(BmsContent)}
	 * が検査失敗を返した場合({@link BmsLoadError.Kind#FAILED_TEST_CONTENT})にも適用されます。</p>
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
	 * に対してエラー通知を行います。その際のエラー種別は{@link BmsLoadError.Kind#LIBRARY_SPEC_VIOLATION}となります。</p>
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
	 * 不明なメタ情報を無視するかどうかを設定します。
	 * <p>無視すると、BMS解析はエラーにならず不明メタ情報を読み飛ばして解析を続行するようになります。</p>
	 * <p>具体的には、{@link #setHandler(BmsLoadHandler)}で設定したハンドラの{@link BmsLoadHandler#parseError(BmsLoadError)}
	 * に{@link BmsLoadError.Kind#UNKNOWN_META}のエラーが一切通知されなくなります。</p>
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
	 * <p>具体的には、{@link #setHandler(BmsLoadHandler)}で設定したハンドラの{@link BmsLoadHandler#parseError(BmsLoadError)}
	 * に{@link BmsLoadError.Kind#UNKNOWN_CHANNEL}のエラーが一切通知されなくなります。</p>
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
	 * <p>具体的には、{@link #setHandler(BmsLoadHandler)}で設定したハンドラの{@link BmsLoadHandler#parseError(BmsLoadError)}
	 * に{@link BmsLoadError.Kind#WRONG_DATA}のエラーが一切通知されなくなります。</p>
	 * <p>デフォルトではこの設定は「有効」になっています。</p>
	 * @param isIgnore 不正データを無視するかどうか
	 * @return このオブジェクトのインスタンス
	 */
	public final BmsLoader setIgnoreWrongData(boolean isIgnore) {
		mIsIgnoreWrongData = isIgnore;
		return this;
	}

	/**
	 * BMSコンテンツを読み込みます。
	 * <p>BMSコンテンツは指定されたファイルから読み込みます。</p>
	 * <p>読み込み処理の詳細は{@link #load(String)}を参照してください。</p>
	 * @param bms BMSファイル
	 * @return BMSコンテンツ
	 * @throws BmsException bmsがnull: Cause NullPointerException
	 * @throws BmsException {@link #load(String)}を参照
	 */
	public final BmsContent load(File bms) throws BmsException {
		try {
			assertArgNotNull(bms, "bms");
		} catch (Exception e) {
			throw new BmsException(e);
		}
		try {
			return load(bms.toPath());
		} catch (BmsException e) {
			throw e;
		} catch (Exception e) {
			throw new BmsException(e);
		}
	}

	/**
	 * BMSコンテンツを読み込みます。
	 * <p>BMSコンテンツは指定されたパスが示すファイルから読み込みます。</p>
	 * <p>読み込み処理の詳細は{@link #load(String)}を参照してください。</p>
	 * @param bms BMSファイルのパス
	 * @return BMSコンテンツ
	 * @throws BmsException bmsがnull: Cause NullPointerException
	 * @throws BmsException {@link #load(String)}を参照
	 */
	public final BmsContent load(Path bms) throws BmsException {
		try {
			assertArgNotNull(bms, "bms");
		} catch (Exception e) {
			throw new BmsException(e);
		}
		try {
			return load(Files.readAllBytes(bms));
		} catch (BmsException e) {
			throw e;
		} catch (Exception e) {
			throw new BmsException(e);
		}
	}

	/**
	 * BMSコンテンツを読み込みます。
	 * <p>BMSコンテンツは指定された入力ストリームから読み込みます。</p>
	 * <p>読み込み処理の詳細は{@link #load(String)}を参照してください。</p>
	 * @param bms BMSの入力ストリーム
	 * @return BMSコンテンツ
	 * @throws BmsException bmsがnull: Cause NullPointerException
	 * @throws BmsException {@link #load(String)}を参照
	 */
	public final BmsContent load(InputStream bms) throws BmsException {
		try {
			assertArgNotNull(bms, "bms");
		} catch (Exception e) {
			throw new BmsException(e);
		}
		try {
			// 入力ストリームの現在位置から終端まで一括で読み取り、バイト配列を生成したものを解析する
			// 入力ストリームのサイズはBMSデータとして一般的にあり得るサイズであることを想定している。
			// 最も重いBMSファイルでも約1MB程度のサイズになることを想定。
			var baos = new ByteArrayOutputStream(1024 * 8);
			var tempBuffer = new byte[1024];
			for (var length = bms.read(tempBuffer); length >= 0; length = bms.read(tempBuffer)) {
				baos.write(tempBuffer, 0, length);
			}
			return load(baos.toByteArray());
		} catch (BmsException e) {
			throw e;
		} catch (Exception e) {
			throw new BmsException(e);
		}
	}

	/**
	 * BMSコンテンツを読み込みます。
	 * <p>BMSコンテンツは指定されたバイト配列から読み込みます。</p>
	 * <p>読み込み処理の詳細は{@link #load(String)}を参照してください。</p>
	 * @param bms BMSのバイト配列
	 * @return BMSコンテンツ
	 * @throws BmsException bmsがnull: Cause NullPointerException
	 * @throws BmsException {@link #load(String)}を参照
	 */
	public final BmsContent load(byte[] bms) throws BmsException {
		try {
			assertArgNotNull(bms, "bms");
		} catch (Exception e) {
			throw new BmsException(e);
		}

		// BOMチェックを行う(UTF-8かどうかに関わらずBOMは無視する)
		var skipCount = 0L;
		if ((bms.length >= 3) && (bms[0] == 0xef) && (bms[1] == 0xbb) && (bms[2] == 0xbf)) {
			// データ先頭にUTF-8におけるBOMを検出した
			skipCount = 3;
		} else {
			// TODO: UTF-16のBOMチェックは不要？
		}

		// BMS解析に使用する文字セットを、BMS宣言のencodingから決定しようとする
		Charset charset = null;
		try {
			// 1行目のみをASCIIコード限定で読み取るReaderを生成する
			var bais = new ByteArrayInputStream(bms);
			bais.skip(skipCount);
			var isr = new InputStreamReader(bais, StandardCharsets.US_ASCII);
			var reader = new BufferedReader(isr);

			// バイト配列をASCII文字列として1行目を読み取る
			var declaration = reader.readLine();
			if (declaration != null) {
				// BMS宣言を解析してencodingの値から文字セットを決定しようとする
				var decls = parseDeclaration(declaration);
				if ((decls != null) && (decls.containsKey("encoding"))) {
					try {
						// encodingに記述されている文字セットを使用しようとする
						charset = Charset.forName(decls.get("encoding"));
					} catch (Exception e) {
						// 指定文字セットが使えない場合はエラー
						error(BmsLoadError.Kind.ENCODING, 1, declaration, null, e);
						charset = BmsSpec.getStandardCharset();
					}
				} else {
					// BMS宣言無し、BMS宣言有りでencoding指定無しの場合はBMS標準文字セットを使用する
					charset = BmsSpec.getStandardCharset();
				}
			} else {
				// 文字列が読み取れなかった場合はBMS標準文字セットを使用する
				// このケースではおそらく空のBMSファイルが投げられている
				charset = BmsSpec.getStandardCharset();
			}
		} catch (BmsException e) {
			throw e;
		} catch (Exception e) {
			// 例外発生時はBmsExceptionにラップして投げる
			throw new BmsException(e);
		}

		// BMS読み込み用のReaderを生成し、そのReaderで読み込みを実行する
		var bais = new ByteArrayInputStream(bms);
		bais.skip(skipCount);
		var reader = new InputStreamReader(bais, charset);

		return load(reader);
	}

	/**
	 * BMSコンテンツを読み込みます。
	 *
	 * <p>{@link java.io.File} / {@link java.nio.file.Path} / {@link java.io.InputStream} / byte[]が入力となる場合、
	 * BmsLoaderでは最初に先頭行のBMS宣言を解析します。BMS宣言が存在し、encoding要素が存在する場合はその設定値から
	 * 使用する文字セットを解決します。以後の読み込みは当該文字セットを用いて文字列をデコードし、BMS解析を行います。</p>
	 *
	 * <p>BMS宣言が無い、またはencoding要素が指定されていない場合は{@link BmsSpec#getStandardCharset}で取得できる
	 * 文字セットを用いてBMS解析を行います。</p>
	 *
	 * <p>BMS宣言は左から順に解析し、その順番でBMSコンテンツにセットされます。</p>
	 *
	 * <p>BMS解析の動作仕様は概ね以下の通りです。<br>
	 * - 行頭の半角スペースおよびタブは無視されます。<br>
	 * - 半角スペースおよびタブのみの行は空行として読み飛ばします。<br>
	 * - 改行コードは&lt;CR&gt;, &lt;LF&gt;, &lt;CR&gt;&lt;LF&gt;を自動認識します。<br>
	 * - 先頭行において、&quot;;bms? &quot;で始まる場合はBMS宣言として認識されます。<br>
	 * - &quot;;&quot;または&quot;//&quot;で始まる行はコメント行として認識されます。(単一行コメント)<br>
	 * - &quot;/*&quot;で始まる行は複数行コメントの開始行として認識されます。以降、行末に&quot;* /&quot;が出現するまでの
	 * 行は全てコメントとして認識されます。<br>
	 * - 複数行コメントが閉じられずにBMS解析が終了した場合、エラーハンドラにて{@link BmsLoadError.Kind#COMMENT_NOT_CLOSED}
	 * が通知されます。<br>
	 * - &quot;#&quot;または&quot;%&quot;で始まり、1文字目がアルファベットで始まる行をメタ情報の定義と見なします。<br>
	 * - 3文字の半角数字で始まり、次に2文字の半角英数字、更にその次が&quot;:&quot;で始まる行をチャンネルデータの定義と見なします。<br>
	 * - 以上のパターンに該当しない行は構文エラーとし、エラーハンドラにて{@link BmsLoadError.Kind#SYNTAX}が通知されます。
	 * </p>
	 *
	 * <p>
	 * メタ情報解析について<br>
	 * - メタ情報の値は、文字列の右側の半角空白文字は消去されます。<br>
	 * - 索引付きメタ情報は、名称の末端文字2文字を36進数値と見なしてその値をインデックス値とし、残りの文字を名称として扱います。<br>
	 * - 索引付きメタ情報のインデックス値が36進数値でない場合、エラーハンドラにて{@link BmsLoadError.Kind#UNKNOWN_META}が通知されます。<br>
	 * - BMS仕様に規定されていない名称を検出した場合、エラーハンドラにて{@link BmsLoadError.Kind#UNKNOWN_META}が通知されます。<br>
	 * - メタ情報の値がBMS仕様に規定されたデータ型の記述書式に適合しない場合、エラーハンドラにて{@link BmsLoadError.Kind#WRONG_DATA}が通知されます。<br>
	 * </p>
	 *
	 * <p>
	 * チャンネル解析について<br>
	 * - チャンネル番号がBMS仕様に未定義の場合、エラーハンドラにて{@link BmsLoadError.Kind#UNKNOWN_CHANNEL}が通知されます。<br>
	 * - 以下のケースを検出した場合、エラーハンドラにて{@link BmsLoadError.Kind#WRONG_DATA}が通知されます。<br>
	 * &nbsp;&nbsp;- チャンネルに定義されたデータの記述書式がBMS仕様に違反している場合。<br>
	 * &nbsp;&nbsp;- データ重複不許可チャンネルの同小節番号にて複数のデータ定義を検出した場合。<br>
	 * &nbsp;&nbsp;- データ重複許可チャンネルの同小節番号内にて{@link BmsSpec#CHINDEX_MAX}+1個を超えるデータ定義を検出した場合。<br>
	 * &nbsp;&nbsp;- 配列型チャンネルデータの配列要素数が当該小節の刻み総数より多い場合。<br>
	 * &nbsp;&nbsp;- 配列型チャンネルデータの配列要素数が0。<br>
	 * </p>
	 *
	 * @param bms BMSのReader
	 * @return BMSコンテンツ
	 * @exception BmsException bmsがnull: Cause NullPointerException
	 * @exception BmsException BMS仕様が設定されていない: Cause IllegalStateException
	 * @exception BmsException ハンドラが設定されていない: Cause IllegalStateException
	 * @exception BmsAbortException ハンドラ({@link BmsLoadHandler#parseError})がfalseを返した
	 */
	public final BmsContent load(Reader bms) throws BmsException {
		try {
			// 指定Readerからデータを読み取り、生成したBMSデータの文字列を解析にかける
			var sb = new StringBuffer();
			var reader = new BufferedReader(bms);
			for (var line = reader.readLine(); line != null; line = reader.readLine()) {
				sb.append(line);
				sb.append("\n");
			}
			return loadMain(sb.toString());
		} catch (BmsException e) {
			throw e;
		} catch (Exception e) {
			throw new BmsException(e);
		}
	}

	/**
	 * BMSコンテンツを読み込みます。
	 * <p>BmsLoaderでは、最初に先頭行のBMS宣言を解析します。BMS宣言が存在し、encoding要素が存在する場合はその設定値から
	 * 使用する文字セットを解決します。</p>
	 *
	 * @param bms BMSの文字列
	 * @return BMSコンテンツ
	 * @exception BmsException bmsがnull: Cause NullPointerException
	 * @exception BmsException BMS仕様が設定されていない: Cause IllegalStateException
	 * @exception BmsException BMS読み込みハンドラが設定されていない: Cause IllegalStateException
	 * @exception BmsException ハンドラが設定されていない: Cause IllegalStateException
	 * @exception BmsException BMS宣言にてencoding指定時、指定の文字セットが未知: Cause Exception
	 * @exception BmsAbortException ハンドラ({@link BmsLoadHandler#parseError})がfalseを返した
	 */
	public final BmsContent load(String bms) throws BmsException {
		return loadMain(bms);
	}

	/**
	 * 引数のBMSスクリプトを解析し、BMS仕様に従ってBMSコンテンツを生成する。
	 * @param bms Unicode文字列に変換されたBMSスクリプト
	 * @return 生成されたBMSコンテンツ
	 * @throws BmsException {@link #loadCore}参照
	 */
	private BmsContent loadMain(String bms) throws BmsException {
		// アサーション
		// 最初のエラーチェックで失敗する場合は、ハンドラのメソッドは何も呼ばれない
		try {
			assertField(mSpec != null, "BmsSpec is NOT specified.");
			assertField(mHandler != null, "BMS load handler is NOT specified.");
			assertArgNotNull(bms, "bms");
		} catch (Exception e) {
			throw new BmsException(e);
		}

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
				error(BmsLoadError.Kind.PANIC, 0, "", msg, null);
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
				error(BmsLoadError.Kind.FAILED_TEST_CONTENT, 0, "", result.getMessage(), null);
				break;
			}
			default:
				// 想定外
				var msg = String.format("Content test result was returned '%d' by handler", result.getResult());
				var err = new BmsLoadError(BmsLoadError.Kind.PANIC, 0, "", msg, null);
				throw new BmsAbortException(err);
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
		try {
			// 読み取り用のReaderを生成する
			var sr = new StringReader(bms);
			var reader = new BufferedReader(sr);

			content.beginEdit();

			// 1行目の記述を読み取る
			var line = reader.readLine();
			if (line == null) {
				// 1行も読み取れないBMSの場合は生成したての空コンテンツを返す
				content.endEdit();
				return content;
			}

			// BMS宣言の解析を試行する
			var lineNumber = 1;
			var decls = parseDeclaration(line);
			if (decls != null) {
				// BMS宣言を正常に解析できた場合は宣言内容をコンテンツに登録する
				for (var entry : decls.entrySet()) {
					var k = entry.getKey();
					var v = entry.getValue();
					var result = mHandler.testDeclaration(k, v);
					if (result == null) {
						var msg = "BMS declaration test result was returned null by handler";
						error(BmsLoadError.Kind.PANIC, lineNumber, line, msg, null);
					} else switch (result.getResult()) {
					case BmsLoadHandler.TestResult.RESULT_OK:
						// BMS宣言の検査合格時はコンテンツに登録する
						content.addDeclaration(k, v);
						break;
					case BmsLoadHandler.TestResult.RESULT_FAIL:
						// BMS宣言の検査失敗時はコンテンツに登録せずにエラー処理に回す
						error(BmsLoadError.Kind.FAILED_TEST_DECLARATION, 1, line, result.getMessage(), null);
						break;
					case BmsLoadHandler.TestResult.RESULT_THROUGH:
						// BMS宣言を破棄する
						break;
					default:
						// 想定外
						var msg = String.format("BMS declaration test result was returned '%d' by handler",
								result.getResult());
						error(BmsLoadError.Kind.PANIC, 1, line, msg, null);
						break;
					}
				}
				line = reader.readLine();
				lineNumber = 2;
			}

			// 全行を読み取り、BMSを解析する
			var chArrays = new ArrayList<ChannelArrayData>();
			var isNormalMode = true;
			while (line != null) {
				// 読み取りモードによる処理の分岐
				String nextLine = null;
				if (isNormalMode) {
					// 通常解析モード
					if (parseChannel(lineNumber, line, chArrays, content)) {
						// チャンネル
					} else if (parseMeta(lineNumber, line, content)) {
						// ヘッダ(メタ情報)
					} else if (SYNTAX_BLANK.matcher(line).matches()) {
						// 空行
					} else if (SYNTAX_SINGLELINE_COMMENT.matcher(line).matches()) {
						// 単一行コメント
					} else if (SYNTAX_MULTILINE_COMMENT_BEGIN.matcher(line).matches()) {
						// 複数行コメント開始(同一行でコメントが終了する場合は通常解析モードを維持する)
						isNormalMode = SYNTAX_MULTILINE_COMMENT_END.matcher(line).matches();
					} else if (!mIsEnableSyntaxError) {
						// 構文エラー無効
					} else {
						// 構文エラー
						error(BmsLoadError.Kind.SYNTAX, lineNumber, line, null, null);
					}
				} else {
					// 複数行コメントモード
					var matcher = SYNTAX_MULTILINE_COMMENT_END.matcher(line);
					if (matcher.matches()) {
						// 複数行コメント終了
						isNormalMode = true;
						nextLine = matcher.group(2);
					}
				}

				// 次行の読み取り
				if (nextLine != null) {
					// 複数行コメント終了後の行末残骸を次の解析とする
					line = nextLine;
				} else {
					// 次行を読み取る
					line = reader.readLine();
					lineNumber++;
				}
			} // while
			content.endEdit();

			// 複数行コメントモードまま終了した場合はエラー
			if (!isNormalMode && mIsEnableSyntaxError) {
				var msg = "Multi-line comment is NOT finished";
				error(BmsLoadError.Kind.COMMENT_NOT_CLOSED, lineNumber, "", msg, null);
			}

			// 配列型のチャンネルデータをコンテンツに登録する
			// (配列型の要素登録先(tick)が小節の長さの影響を受けるため、一旦BMS全体を解析し切った後で登録する必要あり)
			content.beginEdit();
			for (ChannelArrayData data : chArrays) {
				var chNumber = data.channel.getNumber();
				var chIndex = content.getChannelDataCount(chNumber, data.measure);
				var tickCount = content.getMeasureTickCount(data.measure);
				var count = data.array.size();

				// チャンネルデータの検査を行う
				var result = mHandler.testChannel(data.channel, chIndex, data.measure, data.array);
				if (result == null) {
					var msg = "Channel test result was returned null by handler";
					error(BmsLoadError.Kind.PANIC, data.lineNumber, data.line, msg, null);
				} else switch (result.getResult()) {
				case BmsLoadHandler.TestResult.RESULT_OK: {
					// データ配列をNoteとしてコンテンツに登録する
					// Tickの値は配列要素数, 配列インデックス値, 当該小節の刻み数から計算
					var occurError = false;
					var lastTick = -1.0;
					var tickRatio = (double)tickCount / (double)count;
					var processedPos = 0;
					for (processedPos = 0; processedPos < count; processedPos++) {
						var i = processedPos;
						var value = data.array.getValue(i);
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
							error(BmsLoadError.Kind.WRONG_DATA, data.lineNumber, data.line, null, e);
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
							error(BmsLoadError.Kind.WRONG_DATA, data.lineNumber, data.line, null, e);
							occurError = true;
							break;
						}
					}

					// エラー発生時は登録途中のデータ配列を消去する
					if (occurError) {
						for (var i = 0; i < processedPos; i++) {
							var tick = (double)i * tickRatio;
							var value = data.array.getValue(i);
							if (value != 0) {
								content.removeNote(chNumber, chIndex, data.measure, tick);
							}
						}
					}

					break;
				}
				case BmsLoadHandler.TestResult.RESULT_FAIL:
					// 検査に失敗した場合はエラーとし、ノートの登録処理は省略しようとする
					error(BmsLoadError.Kind.FAILED_TEST_CHANNEL, data.lineNumber, data.line, result.getMessage(), null);
					break;
				case BmsLoadHandler.TestResult.RESULT_THROUGH:
					// 解析したチャンネルデータを破棄する
					break;
				default:
					// 不明なエラー
					var msg = String.format("Channel test result was returned '%d' by handler", result.getResult());
					error(BmsLoadError.Kind.PANIC, data.lineNumber, data.line, msg, null);
					break;
				}
			}
			content.endEdit();
		} catch (BmsException e) {
			throw e;
		} catch (Exception e) {
			var msg = String.format("Caught un-expected exception: %s", e);
			throw new BmsException(msg, e);
		}

		// 正常終了
		return content;
	}

	/**
	 * BMS宣言を解析する
	 * @param declaration BMS宣言の記述(BMS宣言ではない可能性がある)
	 * @return BMS宣言の一式が格納されたハッシュマップ。BMS宣言の構文を成していない時はnull。
	 */
	private Map<String, String> parseDeclaration(String declaration) {
		// 行がBMS宣言の構文に適合するかチェックする
		var matcherAll = SYNTAX_BMS_DECLARATION_ALL.matcher(declaration);
		if (!matcherAll.matches()) {
			// 構文が不適当なためBMS宣言と見なさない
			return null;
		}

		// BMS宣言のキーと値を取り出す
		var decls = new LinkedHashMap<String, String>();
		var elements = matcherAll.group(1);
		var matcherElem = SYNTAX_BMS_DECLARATION_ELEMENT.matcher(elements);
		while (matcherElem.find()) {
			var key = matcherElem.group(1);
			var value = matcherElem.group(2);
			decls.put(key, value);
		}

		return (decls.size() == 0) ? null : decls;
	}

	/**
	 * メタ情報を解析する
	 * @param lineNumber 解析対象行の行番号
	 * @param line 解析対象行の文字列
	 * @param content メタ情報を格納するBMSコンテンツ
	 * @return 解析対象行をメタ情報として認識した場合はtrue、そうでなければfalse
	 * @exception BmsParseException エラーハンドラがfalseを返した時
	 */
	private boolean parseMeta(int lineNumber, String line, BmsContent content) throws BmsException {
		// メタ情報定義かどうか確認する
		var matcher = SYNTAX_DEFINE_META.matcher(line);
		if (!matcher.matches()) {
			// マッチしない場合はメタ情報定義ではない
			return false;
		}

		// メタ名称を取り出す
		var name = matcher.group(1);
		if ((name.length() >= 2) && (name.charAt(0) == '#') && Character.isDigit(name.charAt(1))) {
			// 1文字目が"#"で2文字目が数字の場合、チャンネルを指定しようとした形跡がある
			// そもそも2文字目はアルファベットから始まらないとメタ情報として認定されないのでメタ情報定義ではない
			return false;
		}

		// 値を取り出す
		var value = Objects.requireNonNullElse(matcher.group(5), "");

		// BMS仕様から対応するメタ情報を検索する
		var index = 0;
		var meta = mSpec.getMeta(name, BmsUnit.SINGLE);
		if (meta == null) {
			// 重複可能メタ情報で試行する
			meta = mSpec.getMeta(name, BmsUnit.MULTIPLE);
		}
		if (meta == null) {
			// 索引付きメタ情報で試行する
			var nameLength = name.length();
			if (nameLength <= 2) {
				// 2文字以下の場合、名称が無くなるのでエラー
				var msg = "Wrong indexed meta name";
				error(BmsLoadError.Kind.UNKNOWN_META, lineNumber, line, msg, null);
				return true;  // メタ情報として解析済みとする
			}

			// 末尾2文字をインデックス値と見なす
			var indexStr = name.substring(nameLength - 2, nameLength);
			if (!BmsType.BASE36.test(indexStr)) {
				// インデックス値の記述が不正のためエラー
				var msg = "Wrong indexed meta's index";
				error(BmsLoadError.Kind.UNKNOWN_META, lineNumber, line, msg, null);
				return true;  // メタ情報として解析済みとする
			}

			// 索引付きメタ情報取得を試行する
			name = name.substring(0, nameLength - 2);
			meta = mSpec.getMeta(name, BmsUnit.INDEXED);
			index = Integer.parseInt(indexStr, 36);
		}
		if (meta == null) {
			// メタ情報不明
			var msg = String.format("'%s' No such meta in spec", name);
			error(BmsLoadError.Kind.UNKNOWN_META, lineNumber, line, msg, null);
			return true;  // メタ情報として解析済みとする
		}

		// 定義値が当該メタ情報の規定データ型に適合するか
		var type = meta.getType();
		if (!type.test(value)) {
			// データの記述内容が適合しない
			var msg = "Type mismatch meta value";
			error(BmsLoadError.Kind.WRONG_DATA, lineNumber, line, msg, null);
			return true;  // メタ情報として解析済みとする
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
					error(BmsLoadError.Kind.LIBRARY_SPEC_VIOLATION, lineNumber, line, msg, null);
					return true;
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
					error(BmsLoadError.Kind.LIBRARY_SPEC_VIOLATION, lineNumber, line, msg, null);
					return true;
				}
			}
		}

		// メタ情報を検査する
		var unit = meta.getUnit();
		var result = BmsLoadHandler.TestResult.FAIL;
		switch (unit) {
		case SINGLE: result = mHandler.testMeta(meta, 0, obj); break;
		case MULTIPLE: result = mHandler.testMeta(meta, content.getMultipleMetaCount(name), obj); break;
		case INDEXED: result = mHandler.testMeta(meta, index, obj); break;
		default: break;
		}
		if (result == null) {
			var msg = "Meta test result was returned null by handler";
			error(BmsLoadError.Kind.PANIC, lineNumber, line, msg, null);
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
			error(BmsLoadError.Kind.FAILED_TEST_META, lineNumber, line, result.getMessage(), null);
			break;
		case BmsLoadHandler.TestResult.RESULT_THROUGH:
			// メタ情報破棄
			break;
		default:
			// 想定外
			var msg = String.format("Meta test result was returned '%d' by handler", result.getResult());
			error(BmsLoadError.Kind.PANIC, lineNumber, line, msg, null);
			break;
		}

		return true;
	}

	/**
	 * チャンネルを解析する
	 *
	 * <p>BMSコンテンツに直接登録するチャンネルデータは単一型のデータのみで、配列型のデータは一旦リストに退避する。
	 * 配列型のデータの譜面上の配置は小節の長さの影響を受けるが、小節の長さを確定できるのは単一型のデータを完全に解析した
	 * 後であるため、当メソッドでは配列型のデータはBMSコンテンツには直接登録しない。</p>
	 *
	 * @param lineNumber 解析対象行の行番号
	 * @param line 解析対象行
	 * @param channelArray 配列型のチャンネルデータを格納するリスト
	 * @param content チャンネルデータを格納するBMSコンテンツ
	 * @return 解析対象行をチャンネルとして認識した場合はtrue、そうでなければfalse
	 * @exception BmsParseException エラーハンドラがfalseを返した時
	 */
	private boolean parseChannel(int lineNumber, String line, List<ChannelArrayData> chArrays,
			BmsContent content) throws BmsException {
		// チャンネル定義かどうか確認する
		var matcher = SYNTAX_DEFINE_CHANNEL.matcher(line);
		if (!matcher.matches()) {
			// マッチしない場合はチャンネル定義ではない
			return false;
		}

		// 小節番号、チャンネル、値を取り出す
		var measure = Integer.parseInt(matcher.group(1));
		var channelNum = BmsInt.to36i(matcher.group(2));
		var value = matcher.group(3).trim();

		// チャンネルの取得とデータ型の適合チェック
		var channel = mSpec.getChannel(channelNum);
		if (channel == null) {
			// 該当するチャンネルが仕様として規定されていない
			var msg = String.format("'%s' No such channel in spec", BmsInt.to36s(channelNum));
			error(BmsLoadError.Kind.UNKNOWN_CHANNEL, lineNumber, line, msg, null);
			return true;
		}

		// 解析したデータの登録処理
		var channelType = channel.getType();
		if (channelType.isValueType()) {
			// 値型の場合
			// 小節データの型変換を行う
			var object = (Object)null;
			try {
				object = channelType.cast(value);
			} catch (Exception e) {
				error(BmsLoadError.Kind.WRONG_DATA, lineNumber, line, null, e);
				return true;
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
						error(BmsLoadError.Kind.LIBRARY_SPEC_VIOLATION, lineNumber, line, msg, null);
						return true;
					}
				}
			}

			// チャンネルデータの検査を行う
			var chIndex = content.getChannelDataCount(channelNum, measure);
			var result = mHandler.testChannel(channel, chIndex, measure, object);
			if (result == null) {
				var msg = "Channel test result was returned null by handler";
				error(BmsLoadError.Kind.PANIC, lineNumber, line, msg, null);
			} else switch (result.getResult()) {
			case BmsLoadHandler.TestResult.RESULT_OK:
				try {
					// 小節データの空き領域にチャンネルデータを登録する
					content.setMeasureValue(channelNum, chIndex, measure, object);
				} catch (Exception e) {
					// 何らかのエラーが発生した場合はデータの不備
					error(BmsLoadError.Kind.WRONG_DATA, lineNumber, line, null, e);
				}
				break;
			case BmsLoadHandler.TestResult.RESULT_FAIL:
				// 検査不合格
				error(BmsLoadError.Kind.FAILED_TEST_CHANNEL, lineNumber, line, result.getMessage(), null);
				break;
			case BmsLoadHandler.TestResult.RESULT_THROUGH:
				// チャンネルデータを破棄する
				break;
			default:
				// 想定外
				var msg = String.format("Channel test result was returned '%d' by handler", result.getResult());
				error(BmsLoadError.Kind.PANIC, lineNumber, line, msg, null);
				break;
			}
		} else if (channelType.isArrayType()) {
			// 配列型の場合
			var array = (BmsArray)null;
			try {
				// 配列データを解析する
				array = new BmsArray(value, channelType.getRadix());
			} catch (IllegalArgumentException e) {
				// 配列の書式が不正
				var msg = "Wrong array value";
				error(BmsLoadError.Kind.WRONG_DATA, lineNumber, line, msg, e);
				return true;
			}

			// チャンネルデータの検査を行う
			var chIndex = content.getChannelDataCount(channelNum, measure);
			var result = mHandler.testChannel(channel, chIndex, measure, array);
			if (result == null) {
				var msg = "Channel test result was returned null by handler";
				error(BmsLoadError.Kind.PANIC, lineNumber, line, msg, null);
			} else switch (result.getResult()) {
			case BmsLoadHandler.TestResult.RESULT_OK: {
				// 解析済みデータをストックしておく
				ChannelArrayData data = new ChannelArrayData();
				data.lineNumber = lineNumber;
				data.line = line;
				data.channel = channel;
				data.measure = measure;
				data.array = array;
				chArrays.add(data);
				break;
			}
			case BmsLoadHandler.TestResult.RESULT_FAIL:
				// 検査不合格
				error(BmsLoadError.Kind.FAILED_TEST_CHANNEL, lineNumber, line, result.getMessage(), null);
				break;
			case BmsLoadHandler.TestResult.RESULT_THROUGH:
				// チャンネルデータを破棄する
				break;
			default:
				// 想定外
				var msg = String.format("Channel test result was returned '%d' by handler", result.getResult());
				error(BmsLoadError.Kind.PANIC, lineNumber, line, msg, null);
				break;
			}
		} else {
			// Do nothing
			// どないやねん...
		}

		return true;
	}

	/**
	 * エラー発生時の処理
	 * @param kind エラー種別
	 * @param lineNumber 行番号
	 * @param line 行文字列
	 * @param message エラーメッセージ
	 * @param throwable 発生した例外
	 * @exception BmsAbortException エラーハンドラがfalseを返した時
	 */
	private void error(BmsLoadError.Kind kind, int lineNumber, String line, String message, Throwable throwable)
			throws BmsAbortException {
		// エラーが無効にされている場合は例外をスローしない
		var fnIsOccur = mOccurErrorMap.get(kind);
		if ((fnIsOccur != null) && !fnIsOccur.getAsBoolean()) {
			return;
		}

		// エラーハンドラにエラー内容を通知する
		var error = new BmsLoadError(kind, lineNumber, line, message, throwable);
		if (!mHandler.parseError(error)) {
			// BMS解析を中断する場合は例外を投げる
			throw new BmsAbortException(error);
		}
	}
}
