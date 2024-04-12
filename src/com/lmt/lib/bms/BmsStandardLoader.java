package com.lmt.lib.bms;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * 標準フォーマットのBMSからBMSコンテンツを生成するBMSローダクラスです。
 *
 * <p><strong>標準フォーマットの記述形式について</strong><br>
 * 標準フォーマットは、一般に知られるBMSの仕様に基づいて記述されたテキストです。一般的には「BMSファイル」のことを指します。
 * BMSライブラリで読み取り可能な記述形式は一般的なBMSの仕様を取り込んでいますが、一部独自の機能を盛り込んでいます。
 * 詳細については以下を参照してください。</p>
 *
 * <p><strong>BMS宣言</strong><br>
 * BMSの1行目において、";?bms"で始まるコメントを検出した場合、1行目をBMS宣言として解析しようとします。
 * BMS宣言の構文に誤りがある場合、BMS宣言は通常のコメントとして認識されるようになり、BMS宣言のないBMSコンテンツとして
 * 読み込まれます。BMS宣言の記述例は以下の通りです。</p>
 *
 * <pre>
 * ;?bms rule="BM"</pre>
 *
 * <p><strong>メタ情報</strong><br>
 * BMSの中で「ヘッダ」と呼ばれる宣言をメタ情報として解析します。メタ情報は"#"または"%"で始まる行が該当します。
 * メタ情報はBMSのどこで宣言されていても問題なく読み込むことができます(チャンネルを記述した後でもOK)。
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
 * "#"＋数字3文字＋36進数2文字＋":"で始まる行は「チャンネル」として解析します。
 * 最初の数字は小節番号、続く36進数はチャンネル番号を表し、":"の後の記述は当該小節・チャンネルのデータを表します。
 * チャンネルデータの記述形式はBMS仕様によって定められているため、仕様に違反する記述をした場合、
 * 当該チャンネルは構文エラーとして報告されます。一般的にチャンネルは小節番号の若い順で記述されますが、
 * 小節番号は前後しても構いません。ただし、BMSの可読性が著しく低下するので小節番号順に記述することが推奨されます。<br>
 * チャンネルの記述例は以下の通りです。</p>
 *
 * <pre>
 * #01002:1.5
 * #0880A:00AB00CD00EF00GH
 * #123ZZ:String data channel</pre>
 */
public class BmsStandardLoader extends BmsLoader {
	/** 解析フェーズ：BMS宣言読み取り中 */
	private static final int PHASE_DECLARATION = 0;
	/** 解析フェーズ：BMS宣言取り出し中 */
	private static final int PHASE_DECLARATION_FETCH = 1;
	/** 解析フェーズ：通常解析モード */
	private static final int PHASE_NORMAL = 2;
	/** 解析フェーズ：複数行コメントモード */
	private static final int PHASE_MULTILINE_COMMENT = 3;
	/** 解析フェーズ：終了 */
	private static final int PHASE_DONE = 4;

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
			"^[ \\t]*#(([0-9]{3})([a-zA-Z0-9]{2})):(.*)$");

	/** ローダの設定内容 */
	private BmsLoaderSettings mLoaderSettings;
	/** BMSテキストの行読み取り用リーダ */
	private BufferedReader mReader;
	/** 現在読み取り中の行番号 */
	private int mLineNumber;
	/** 解析フェーズ */
	private int mParsePhase;
	/** 解析済みBMS宣言リスト */
	private Deque<DeclarationParsedElement> mDeclarations;

	/** メタ情報要素コンテナ */
	private MetaParsedElement mMetaElement = new MetaParsedElement();
	/** 値型チャンネル要素コンテナ */
	private ValueChannelParsedElement mValueChElement = new ValueChannelParsedElement();
	/** 配列型チャンネル要素コンテナ */
	private ArrayChannelParsedElement mArrayChElement = new ArrayChannelParsedElement();

	/** {@inheritDoc} */
	@Override
	protected ErrorParsedElement beginParse(BmsLoaderSettings settings, String source) {
		mLoaderSettings = settings;
		mReader = new BufferedReader(new StringReader(source));
		mLineNumber = 0;
		mParsePhase = PHASE_DECLARATION;
		mDeclarations = null;
		return ErrorParsedElement.PASS;
	}

	/** {@inheritDoc} */
	@Override
	protected ErrorParsedElement endParse() {
		try { mReader.close(); } catch (IOException e) {}
		mLoaderSettings = null;
		mReader = null;
		mLineNumber = 0;
		mParsePhase = PHASE_DONE;
		mDeclarations = null;
		return ErrorParsedElement.PASS;
	}

	/**
	 * BMSの解析によって得られたBMSコンテンツの要素を1件返します。
	 * <p><strong>※当メソッドはBMSライブラリの一般利用者が参照する必要はありません。</strong></p>
	 * <p>標準フォーマットにおけるBMS解析の動作仕様は概ね以下の通りです。</p>
	 * <ul>
	 * <li>行頭の半角スペースおよびタブは無視されます。</li>
	 * <li>半角スペースおよびタブのみの行は空行として読み飛ばします。</li>
	 * <li>改行コードは&lt;CR&gt;, &lt;LF&gt;, &lt;CR&gt;&lt;LF&gt;を自動認識します。</li>
	 * <li>先頭行において、";bms? "で始まる場合はBMS宣言として認識されます。</li>
	 * <li>BMS宣言は左から順に解析し、その順番で返します。</li>
	 * <li>";"、"*"、または"//"で始まる行はコメント行として認識されます。(単一行コメント)</li>
	 * <li>"/*"で始まる行は複数行コメントの開始行として認識されます。以降、行末に"* /"が出現するまでの
	 * 行は全てコメントとして認識されます。</li>
	 * <li>複数行コメントが閉じられずにBMS解析が終了した場合、エラーハンドラにて{@link BmsErrorType#COMMENT_NOT_CLOSED}
	 * が通知されます。</li>
	 * <li>"#"または"%"で始まり、1文字目がアルファベットで始まる行をメタ情報の定義と見なします。</li>
	 * <li>"#"に続き3文字の半角数字で始まり、次に2文字の半角英数字、更にその次が":"で始まる行をチャンネルデータの定義と見なします。</li>
	 * <li>以上のパターンに該当しない行は構文エラーとしてエラー要素を返します。</li>
	 * </ul>
	 * @return BMSコンテンツの要素、またはエラー要素。これ以上要素がない場合はnull。
	 */
	@Override
	protected ParsedElement nextElement() {
		try {
			// 読み取りが既に完了している場合は何もしない
			if (mParsePhase == PHASE_DONE) {
				return null;
			}

			// BMS宣言読み取り中の場合は次のBMS宣言を返す
			if (mParsePhase == PHASE_DECLARATION_FETCH) {
				return fetchDeclaration();
			}

			// 行単位の解析処理
			var element = (ParsedElement)null;
			var line = "";
			while ((element == null) && ((line = mReader.readLine()) != null)) {
				mLineNumber++;
				element = parseElement(line);
			}

			// 全行読み取り完了チェック
			if (line == null) {
				if ((mParsePhase == PHASE_MULTILINE_COMMENT) && getSettings().isSyntaxErrorEnable()) {
					// 複数行コメントモードまま終了した場合はエラー(構文エラー有効時のみ)
					var msg = "Multi-line comment is not closed.";
					var err = new BmsScriptError(BmsErrorType.COMMENT_NOT_CLOSED, mLineNumber, "", msg, null);
					element = new ErrorParsedElement(err);
				}
				mParsePhase = PHASE_DONE;
			}

			return element;
		} catch (IOException e) {
			// 行読み込みのリーダ用の例外ハンドラだが、元がStringReaderのためスローされない
			return null;  // Don't care
		}
	}

	/**
	 * BMSコンテンツ要素解析
	 * @param line 行テキスト
	 * @return 解析されたBMSコンテンツ要素、またはエラー要素
	 */
	private ParsedElement parseElement(String line) {
		switch (mParsePhase) {
		case PHASE_DECLARATION:  // BMS宣言モード
			return parseDeclaration(line);
		case PHASE_NORMAL:  // 通常解析モード
			return parseNormal(line);
		case PHASE_MULTILINE_COMMENT:  // 複数行コメントモード
			return parseMultilineComment(line);
		default:  // Don't care
			return null;
		}
	}

	/**
	 * BMS宣言要素解析
	 * @param line 行テキスト
	 * @return BMS宣言要素
	 */
	private ParsedElement parseDeclaration(String line) {
		// 行がBMS宣言の構文に適合するかチェックする
		var matcherAll = SYNTAX_BMS_DECLARATION_ALL.matcher(line);
		if (!matcherAll.matches()) {
			// 構文が不適当なためBMS宣言と見なさず、通常解析モードで解析を行う
			mParsePhase = PHASE_NORMAL;
			return parseNormal(line);
		}

		// BMS宣言のキーと値を取り出す
		var decls = new ArrayDeque<DeclarationParsedElement>();
		var elements = matcherAll.group(1);
		var matcherElem = SYNTAX_BMS_DECLARATION_ELEMENT.matcher(elements);
		while (matcherElem.find()) {
			var key = matcherElem.group(1);
			var value = matcherElem.group(2);
			decls.push(new DeclarationParsedElement(1, line, key, value));
		}

		// BMS宣言が1件も存在しない場合はこの行を単なるコメントと見なし、通常解析モードで解析を続行する
		if (decls.size() == 0) {
			mParsePhase = PHASE_NORMAL;
			return null;
		}

		// BMS宣言取り出しモードで1件ずつBMS宣言を返すようにする
		mParsePhase = PHASE_DECLARATION_FETCH;
		mDeclarations = decls;
		return fetchDeclaration();
	}

	/**
	 * 通常解析モードにおけるBMSコンテンツ要素解析処理
	 * @param line 行テキスト
	 * @return BMSコンテンツ要素、またはエラー要素
	 */
	private ParsedElement parseNormal(String line) {
		var element = (ParsedElement)null;
		if ((element = parseChannel(line)) != null) {
			// チャンネルを検出
		} else if ((element = parseMeta(line)) != null) {
			// ヘッダ(メタ情報)を検出
		} else if (SYNTAX_BLANK.matcher(line).matches()) {
			// 空行
		} else if (SYNTAX_SINGLELINE_COMMENT.matcher(line).matches()) {
			// 単一行コメント
		} else if (SYNTAX_MULTILINE_COMMENT_BEGIN.matcher(line).matches()) {
			// 複数行コメント開始(同一行でコメントが終了する場合は通常解析モードを維持する)
			var isNormalMode = SYNTAX_MULTILINE_COMMENT_END.matcher(line).matches();
			mParsePhase = isNormalMode ? PHASE_NORMAL : PHASE_MULTILINE_COMMENT;
		} else if (!getSettings().isSyntaxErrorEnable()) {
			// 構文エラー無効
		} else {
			// 構文エラー
			element = new ErrorParsedElement(new BmsScriptError(BmsErrorType.SYNTAX, mLineNumber, line, null, null));
		}
		return element;
	}

	/**
	 * 複数行コメントモードにおけるBMSコンテンツ要素解析処理
	 * @param line 行テキスト
	 * @return BMSコンテンツ要素
	 */
	private ParsedElement parseMultilineComment(String line) {
		var element = (ParsedElement)null;
		var matcher = SYNTAX_MULTILINE_COMMENT_END.matcher(line);
		if (matcher.matches()) {
			// 複数行コメント終了
			mParsePhase = PHASE_NORMAL;
			element = parseElement(matcher.group(2));
		}
		return element;
	}

	/**
	 * メタ情報要素解析
	 * @param line 行テキスト
	 * @return メタ情報要素
	 */
	private ParsedElement parseMeta(String line) {
		// メタ情報定義かどうか確認する
		var matcher = SYNTAX_DEFINE_META.matcher(line);
		if (!matcher.matches()) {
			// マッチしない場合はメタ情報定義ではない
			return null;
		}

		// メタ名称を取り出す
		var name = matcher.group(1);
		if ((name.length() >= 2) && (name.charAt(0) == '#') && Character.isDigit(name.charAt(1))) {
			// 1文字目が"#"で2文字目が数字の場合、チャンネルを指定しようとした形跡がある
			// そもそも2文字目はアルファベットから始まらないとメタ情報として認定されないのでメタ情報定義ではない
			return null;
		}

		// BMS仕様から対応するメタ情報を検索する
		var spec = mLoaderSettings.getSpec();
		var index = 0;
		var meta = spec.getMeta(name, BmsUnit.SINGLE);
		if (meta == null) {
			// 重複可能メタ情報で試行する
			meta = spec.getMeta(name, BmsUnit.MULTIPLE);
		}
		if (meta == null) {
			// 索引付きメタ情報で試行する
			var nameLength = name.length();
			if (nameLength <= 2) {
				// 2文字以下の場合、名称が無くなるのでエラー
				var msg = "Wrong indexed meta name";
				var err = new BmsScriptError(BmsErrorType.UNKNOWN_META, mLineNumber, line, msg, null);
				return new ErrorParsedElement(ParsedElementType.META, err);
			}

			// 末尾2文字をインデックス値と見なす
			var indexStr = name.substring(nameLength - 2, nameLength);
			if (!BmsType.BASE36.test(indexStr)) {
				// インデックス値の記述が不正のためエラー
				var msg = "Wrong indexed meta's index";
				var err = new BmsScriptError(BmsErrorType.UNKNOWN_META, mLineNumber, line, msg, null);
				return new ErrorParsedElement(ParsedElementType.META, err);
			}

			// 索引付きメタ情報取得を試行する
			var nameIdx = name.substring(0, nameLength - 2);
			meta = spec.getMeta(nameIdx, BmsUnit.INDEXED);
			index = BmsInt.to36i(indexStr);
			if (meta == null) {
				// メタ情報不明
				var msg = String.format("'%s' No such meta in spec", name);
				var err = new BmsScriptError(BmsErrorType.UNKNOWN_META, mLineNumber, line, msg, null);
				return new ErrorParsedElement(ParsedElementType.META, err);
			} else {
				// 索引付きメタ情報で見つかった場合は索引を抜いた名前を控えておく
				name = nameIdx;
			}
		}

		// メタ情報を検出
		var value = Objects.requireNonNullElse(matcher.group(5), "");
		return mMetaElement.set(mLineNumber, line, meta, index, value);
	}

	/**
	 * チャンネル要素解析
	 * @param line 行テキスト
	 * @return チャンネル要素
	 */
	private ParsedElement parseChannel(String line) {
		// チャンネル定義かどうか確認する
		var matcher = SYNTAX_DEFINE_CHANNEL.matcher(line);
		if (!matcher.matches()) {
			// マッチしない場合はチャンネル定義ではない
			return null;
		}

		// タイムライン読み込みをスキップする場合は何もしない
		if (mLoaderSettings.isSkipReadTimeline()) {
			return null;
		}

		// チャンネルを検出
		var measure = Integer.parseInt(matcher.group(2));
		var channelNum = BmsInt.to36i(matcher.group(3));
		var value = matcher.group(4).strip();

		// チャンネルがBMSに存在するかチェックする
		var channel = mLoaderSettings.getSpec().getChannel(channelNum);
		if (channel == null) {
			// 該当するチャンネルが仕様として規定されていない
			var msg = String.format("'%s' No such channel in spec", BmsInt.to36s(channelNum));
			var err = new BmsScriptError(BmsErrorType.UNKNOWN_CHANNEL, mLineNumber, line, msg, null);
			return new ErrorParsedElement(ParsedElementType.VALUE_CHANNEL, err);
		}

		// データ型ごとの処理
		if (channel.isValueType()) {
			// 値型の場合
			return mValueChElement.set(mLineNumber, line, measure, channelNum, value);
		} else {
			// 配列型の場合
			try {
				// チャンネルの定義値から配列を生成する
				var array = new BmsArray(value, channel.getType().getRadix());
				return mArrayChElement.set(mLineNumber, line, measure, channelNum, array);
			} catch (IllegalArgumentException e) {
				// 配列の書式が不正
				var msg = "Wrong array value";
				var err = new BmsScriptError(BmsErrorType.WRONG_DATA, mLineNumber, line, msg, e);
				return new ErrorParsedElement(ParsedElementType.ARRAY_CHANNEL, err);
			}
		}
	}

	/**
	 * BMS宣言要素取り出し
	 * @return BMS宣言要素
	 */
	private ParsedElement fetchDeclaration() {
		var element = mDeclarations.removeLast();
		mParsePhase = mDeclarations.isEmpty() ? PHASE_NORMAL : PHASE_DECLARATION_FETCH;
		return element;
	}
}
