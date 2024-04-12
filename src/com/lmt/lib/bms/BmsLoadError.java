package com.lmt.lib.bms;

import static com.lmt.lib.bms.BmsAssertion.*;

/**
 * BMSコンテンツの読み込み中に発生したエラーの情報を表します。
 *
 * <p>{@link BmsLoader}でBMSコンテンツを外部データから読み込む工程において、エラーを検出した場合に通知される
 * 一連の情報を表します。アプリケーションは{@link BmsLoader#setHandler}で指定したハンドラの
 * {@link BmsLoadHandler#parseError}によりこの情報の通知を受け、BMSコンテンツの読み込み続行有無を決定することができます。</p>
 *
 * <p><b>エラー情報について</b><br>
 * 当クラスで通知するエラー情報には次のようなものがあります。</p>
 *
 * <ul>
 * <li>エラー種別<br>
 * {@link Kind}で列挙するエラーの種類を示す値です。この値は、外部データ解析において散見される解析エラーの
 * 種類を大まかに分類したものに過ぎません。より厳密な動作を行いたいアプリケーションでは、解析エラーになった行の内容を
 * 分析し、読み込み中止有無を判断すべきです。</li>
 * <li>行番号<br>
 * 解析エラーが発生した外部データ上の行の番号を示します。この値はアプリケーションのユーザーに対して、外部データの
 * どこでエラーが発生したかを示す目的で使用することが出来ます。</li>
 * <li>行の内容<br>
 * 解析エラーとなった外部データ上の行の記述内容そのものを示します。行番号と同様、ユーザーに対してどのような内容の記述が
 * エラーになったかを示す目的で使用出来ます。</li>
 * </ul>
 */
public class BmsLoadError {
	/**
	 * BMSフォーマット読み込み時に発生するエラーの種別を表します。
	 */
	public enum Kind {
		/**
		 * BMSフォーマットで規定されている、どの構文にも該当しない行を表します。
		 * <p>メタ情報、チャンネル、単一行コメント、複数行コメントのいずれにも該当しない行に対して適用されます。</p>
		 */
		SYNTAX,

		/**
		 * BMS宣言が定義されている場合で、「encoding」に記述されている文字セットが未知の内容だった時。
		 * <p>具体的に、encodingに指定する値はJavaで言うところのjava.nio.charset.Charsetを生成する際に
		 * 用いられます。これを生成するのに使用できない内容だった場合を示します。</p>
		 */
		ENCODING,

		/**
		 * BMS宣言の検査に失敗した場合を表します。
		 * <p>このエラーは{@link BmsLoadHandler#testDeclaration}が{@link BmsLoadHandler.TestResult#FAIL}
		 * を返した場合に発生します。検査に失敗したBMS宣言はBMSコンテンツには登録されません。</p>
		 */
		FAILED_TEST_DECLARATION,

		/**
		 * メタ情報の検査に失敗した場合を表します。
		 * <p>このエラーは{@link BmsLoadHandler#testMeta}が{@link BmsLoadHandler.TestResult#FAIL}を返した場合に発生します。
		 * 検査に失敗したメタ情報はBMSコンテンツには登録されません。</p>
		 */
		FAILED_TEST_META,

		/**
		 * チャンネルデータの検査に失敗した場合を表します。
		 * <p>このエラーは{@link BmsLoadHandler#testChannel}が{@link BmsLoadHandler.TestResult#FAIL}を返した場合に発生します。
		 * 検査に失敗したチャンネルデータはBMSコンテンツには登録されません。</p>
		 */
		FAILED_TEST_CHANNEL,

		/**
		 * BMSコンテンツの検査に失敗した場合を表します。
		 * <p>{@link BmsLoadHandler#finishLoad(BmsContent)}がfalseを返した場合に発生します。
		 * 検査に失敗した場合、当該BMSコンテンツは破棄されます。</p>
		 * <p>このエラーは{@link BmsLoadHandler#parseError}では通知されません。エラー発生時にスローされる
		 * {@link BmsAbortException}の{@link BmsAbortException#getError getError}メソッドで取得出来る
		 * エラー情報を参照することで確認出来ます。 </p>
		 */
		FAILED_TEST_CONTENT,

		/**
		 * メタ情報の定義と思われる行で、そのメタ情報がBMS仕様に存在しない場合を表します。
		 * <p>いくつかのBMSフォーマットでは、複数のBMSプレーヤーに対応するためにマイナーなメタ情報を記述している
		 * ケースがあるため、このエラーを検出したとしてもBMSフォーマットの解析をエラー終了することは、
		 * 一般的なBMSプレーヤー、およびBMSエディタにおいては推奨されません。</p>
		 */
		UNKNOWN_META,

		/**
		 * チャンネル定義の行で、チャンネル番号がBMS仕様に存在しない場合を表します。
		 * <p>メタ情報のケースと同様に、複数のBMSプレーヤーに対応するためのマイナーなチャンネルを記述している
		 * ことがあるため、このエラーによるBMSフォーマットの解析終了は推奨されません。</p>
		 */
		UNKNOWN_CHANNEL,

		/**
		 * メタ情報・チャンネルにおいて、データの記述がBMS仕様に規定の当該定義のデータ型・重複可否に適合しない場合を表します。
		 * <p>このエラーを検出した行のデータはBMSコンテンツには取り込まれずに捨てられます。そのような挙動が
		 * 望ましくないアプリケーションでは、BMSフォーマットの読み込みをエラーとして扱うべきです。</p>
		 */
		WRONG_DATA,

		/**
		 * メタ情報・チャンネルにおいて、データの記述がBMSライブラリの定める範囲外であることを検出した場合を表します。
		 * <p>{@link #WRONG_DATA}と似ていますが、こちらはBMSライブラリとしての仕様違反を示しています。
		 * {@link BmsLoader#setFixSpecViolation}を用いることで当エラーを検出した際にエラー発出するか、自動的に
		 * 値の補正を行うかを選択することができます。</p>
		 */
		LIBRARY_SPEC_VIOLATION,

		/**
		 * 複数行コメントが閉じられずにBMSフォーマットの読み込みが終了した場合を表します。
		 * <p>このエラーでは行番号は0、行の記述内容は空文字として報告されます。</p>
		 */
		COMMENT_NOT_CLOSED,

		/**
		 * 想定外の状況が発生したことを表します。<br>
		 * このエラーを検出した場合はBMSコンテンツ読み込みを続行するべきではありません。
		 */
		PANIC,
	}

	/** エラー種別 */
	private Kind mKind;
	/** 行番号 */
	private int mLineNumber;
	/** エラーになった行の定義 */
	private String mLine;
	/** 発生した例外(無い場合はnull) */
	private Throwable mThrowable;

	/**
	 * BMSフォーマット読み込みエラーを構築します。
	 * <p>このクラスのインスタンスは{@link BmsLoader}が生成することを想定しており、アプリケーションが独自に
	 * インスタンスを生成することは推奨されません。</p>
	 * @param kind エラー種別
	 * @param lineNumber 行番号
	 * @param line 行の記述内容
	 * @exception NullPointerException kindがnull
	 * @exception NullPointerException lineがnull
	 * @exception IllegalArgumentException lineNumberがマイナス値
	 */
	public BmsLoadError(Kind kind, int lineNumber, String line) {
		assertArgNotNull(kind, "kind");
		assertArg(lineNumber >= 0, "Wrong lineNumber: %d", lineNumber);
		assertArgNotNull(line, "line");
		mKind = kind;
		mLineNumber = lineNumber;
		mLine = line;
		mThrowable = null;
	}

	/**
	 * BMSフォーマット読み込みエラーを構築します。
	 * <p>このクラスのインスタンスは{@link BmsLoader}が生成することを想定しており、アプリケーションが独自に
	 * インスタンスを生成することは推奨されません。</p>
	 * @param kind エラー種別
	 * @param lineNumber 行番号
	 * @param line 行の記述内容
	 * @param throwable 発生した例外
	 * @exception NullPointerException kindがnull
	 * @exception NullPointerException lineがnull
	 * @exception IllegalArgumentException lineNumberがマイナス値
	 */
	BmsLoadError(Kind kind, int lineNumber, String line, Throwable throwable) {
		assertArgNotNull(kind, "kind");
		assertArg(lineNumber >= 0, "Wrong lineNumber: %d", lineNumber);
		assertArgNotNull(line, "line");
		mKind = kind;
		mLineNumber = lineNumber;
		mLine = line;
		mThrowable = throwable;
	}

	/**
	 * このBMS読み込みエラーの一般的なエラーメッセージを返します。
	 * @return エラーメッセージ
	 */
	@Override
	public String toString() {
		return String.format("L%d: %s: %s", mLineNumber, mKind.toString(), mLine);
	}

	/**
	 * エラー種別を取得します。
	 * @return エラー種別
	 */
	public Kind getKind() {
		return mKind;
	}

	/**
	 * エラー発生行の行番号を取得します。
	 * @return 行番号
	 */
	public int getLineNumber() {
		return mLineNumber;
	}

	/**
	 * エラー発生行の記述を取得します。
	 * @return 行の記述内容
	 */
	public String getLine() {
		return mLine;
	}

	/**
	 * 発生した例外を取得する。
	 * @return 発生した例外
	 */
	Throwable getThrowable() {
		return mThrowable;
	}
}
