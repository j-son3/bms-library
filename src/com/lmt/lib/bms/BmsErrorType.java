package com.lmt.lib.bms;

/**
 * BMS読み込み時に発生するエラーの種別を表します。
 */
public enum BmsErrorType {
	/**
	 * BMSフォーマットで規定されているどの構文にも該当しない行をBMSの構文エラーを表します。
	 * <p>メタ情報、チャンネル、単一行コメント、複数行コメントのいずれにも該当しない行に対して適用されます。
	 * 半角スペース、タブのみの行は構文エラーとはなりませんが、それ以外の改行コードを除く制御文字や全角スペースが
	 * 含まれていると構文エラーとして認識されます。</p>
	 * <p>BMS読み込み時にこのエラーの発生を制御したい場合は{@link BmsLoader#setSyntaxErrorEnable(boolean)}
	 * を使用してください。</p>
	 */
	SYNTAX,

	/**
	 * BMS宣言の検査に失敗した場合を表します。
	 * <p>このエラーは{@link BmsLoadHandler#testDeclaration}が{@link BmsLoadHandler.TestResult#FAIL}
	 * を返し、直後に呼び出される{@link BmsLoadHandler#parseError(BmsScriptError)}がfalseを返した場合に発生します。
	 * 検査に失敗したBMS宣言はBMSコンテンツには登録されません。</p>
	 */
	TEST_DECLARATION,

	/**
	 * メタ情報の検査に失敗した場合を表します。
	 * <p>このエラーは{@link BmsLoadHandler#testMeta}が{@link BmsLoadHandler.TestResult#FAIL}を返し、
	 * 直後に呼び出される{@link BmsLoadHandler#parseError(BmsScriptError)}がfalseを返した場合に発生します。
	 * 検査に失敗したメタ情報はBMSコンテンツには登録されません。</p>
	 */
	TEST_META,

	/**
	 * チャンネルデータの検査に失敗した場合を表します。
	 * <p>このエラーは{@link BmsLoadHandler#testChannel}が{@link BmsLoadHandler.TestResult#FAIL}を返し、
	 * 直後に呼び出される{@link BmsLoadHandler#parseError(BmsScriptError)}がfalseを返した場合に発生します。
	 * 検査に失敗したチャンネルデータはBMSコンテンツには登録されません。</p>
	 */
	TEST_CHANNEL,

	/**
	 * BMS読み込みによって生成されたBMSコンテンツの検査に失敗した場合を表します。
	 * <p>このエラーは{@link BmsLoadHandler#testContent(BmsContent)}が{@link BmsLoadHandler.TestResult#FAIL}
	 * を返した場合に発生します。他の検査失敗とは異なり、{@link BmsLoadHandler#parseError(BmsScriptError)}
	 * の呼び出しは行われません。検査に失敗した場合、当該BMSコンテンツは破棄されます。</p>
	 */
	TEST_CONTENT,

	/**
	 * メタ情報の定義と思われる行で、そのメタ情報がBMS仕様({@link BmsSpec})に定義されていない場合を表します。
	 * <p>アプリケーションによっては、未知のメタ情報をエラーとせず無視したい場合と、BMSの記述を厳密に扱い
	 * エラーとしたい場合が考えられます。BMS読み込み時にこのエラーの発生を制御したい場合は
	 * {@link BmsLoader#setIgnoreUnknownMeta(boolean)}を使用してください。</p>
	 */
	UNKNOWN_META,

	/**
	 * チャンネル定義の行で、チャンネル番号がBMS仕様({@link BmsSpec})に定義されていない場合を表します。
	 * <p>{@link #UNKNOWN_META}と同様に、BMS読み込み時にこのエラーの発生を制御したい場合は
	 * {@link BmsLoader#setIgnoreUnknownChannel(boolean)}を使用してください。</p>
	 */
	UNKNOWN_CHANNEL,

	/**
	 * メタ情報・チャンネルにおいて、データの記述がBMS仕様({@link BmsSpec})に規定のデータ型に適合しない場合を表します。
	 * <p>このエラーを検出した行のデータはBMSコンテンツには取り込まれずに破棄されます。
	 * BMS読み込み時にこのエラーの発生を制御したい場合は{@link BmsLoader#setIgnoreWrongData(boolean)}を使用してください。</p>
	 */
	WRONG_DATA,

	/**
	 * 単体メタ情報、または値型の重複不可チャンネルの再定義を検出したことを表します。
	 * <p>BMS仕様({@link BmsSpec})で重複が許可されていない単体メタ情報・値型の重複不可チャンネルは同一のBMS内に複数の値を
	 * 保有することができず、値を重複定義すると後発の値で上書きされます。この動作はBMS本来の仕様ですが、
	 * {@link BmsLoader#setAllowRedefine(boolean)}を用いることでローダを重複定義を許可しない厳密な動作に変更できます。
	 * 厳密な動作を有効にした状態で重複定義を検出した時にこのエラーを報告します。</p>
	 * <p>なお、厳密な動作が有効であったとしても配列型の重複不可チャンネルではこのエラーは報告されません。
	 * 配列型の重複不可チャンネルは、重複定義があった場合にはそれぞれの定義内容を合成する仕様となっているためです。</p>
	 */
	REDEFINE,

	/**
	 * メタ情報・チャンネルにおいて、データの値の有効範囲がBMSライブラリの定める範囲外であることを表します。
	 * <p>{@link #WRONG_DATA}と似ていますが、このエラーの場合データの記述ルールは誤っておらず、定義された値が
	 * BMSライブラリで取り扱い可能な範囲ではないことを示します。
	 * {@link BmsLoader#setFixSpecViolation(boolean)}を用いることで当エラーを検出した際にエラー発出するか、
	 * 自動的に定義値を有効範囲内に補正するかを選択することができます。</p>
	 */
	SPEC_VIOLATION,

	/**
	 * 複数行コメントが閉じられずにBMSの読み込みが終了した場合を表します。
	 * <p>このエラーでは行番号は0、行の記述内容は空文字として報告されます。</p>
	 */
	COMMENT_NOT_CLOSED,

	/**
	 * 想定外のエラーが発生したことを表します。
	 * <p>このエラーを検出した場合はBMSの読み込みを続行するべきではありません。</p>
	 */
	PANIC,

	/**
	 * 汎用エラー情報であることを表します。
	 * <p>このエラーはBMS読み込み時のエラーのように行番号・行文字列等の付加的なエラー情報を持ちません。
	 * BMS読み込み時以外で発生したBMS関連のエラーをBMSエラー情報として扱いたい場合に用いることを想定しています。</p>
	 */
	COMMON;

	/**
	 * このエラー種別がBMSの記述内容に起因するエラーかどうかを判定します。
	 * @return BMSの記述内容に起因するエラーであればtrue、そうでなければfalse
	 */
	public final boolean isScriptError() {
		return (this != COMMON);
	}

	/**
	 * このエラー種別が汎用エラーであるかどうかを判定します。
	 * @return 汎用エラーであればtrue、そうでなければfalse
	 */
	public final boolean isCommonError() {
		return (this == COMMON);
	}
}