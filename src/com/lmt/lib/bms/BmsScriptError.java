package com.lmt.lib.bms;

import static com.lmt.lib.bms.internal.Assertion.*;

/**
 * BMSの読み込み中に発生したエラーの情報を表します。
 *
 * <p>{@link BmsLoader}でBMSコンテンツを外部データから読み込む工程において、エラーを検出した場合に通知される
 * 一連の情報を表します。アプリケーションは{@link BmsLoader#setHandler(BmsLoadHandler)}で指定したハンドラの
 * {@link BmsLoadHandler#parseError(BmsScriptError)}によりこの情報の通知を受け、BMSの読み込み続行有無を
 * 決定することができます。</p>
 *
 * <p>当クラスでは、親クラスである{@link BmsError}が持つエラー情報に加え以下の情報を保有します。</p>
 *
 * <ul>
 * <li><strong>行番号</strong><br>
 * 解析エラーが発生した外部データ上の行の番号を示します。この値はアプリケーションのユーザーに対して、
 * 外部データのどこでエラーが発生したかを示す目的で使用することができます。<br><br></li>
 * <li><strong>行の内容</strong><br>
 * 解析エラーとなった外部データ上の行の記述内容そのものを示します。行番号と同様、アプリケーションに対してどのような内容の記述が
 * エラーになったかを示す目的で使用できます。</li>
 * </ul>
 */
public class BmsScriptError extends BmsError {
	/** 行番号 */
	private int mLineNumber;
	/** エラーになった行の定義 */
	private String mLine;

	/**
	 * BMSフォーマット読み込みエラーを構築します。
	 * <p>このクラスのインスタンスは{@link BmsLoader}が生成することを想定しており、アプリケーションが独自に
	 * インスタンスを生成することは推奨されません。</p>
	 * @param errType エラー種別
	 * @param lineNumber 行番号
	 * @param line 行の記述内容
	 * @param message エラーメッセージ
	 * @param throwable 発生した例外
	 * @exception NullPointerException errTypeがnull
	 * @exception NullPointerException lineがnull
	 * @exception IllegalArgumentException lineNumberがマイナス値
	 */
	BmsScriptError(BmsErrorType errType, int lineNumber, String line, String message, Throwable throwable) {
		super(errType, message, throwable);
		assertArg(lineNumber >= 0, "Wrong lineNumber: %d", lineNumber);
		assertArgNotNull(line, "line");
		mLineNumber = lineNumber;
		mLine = line;
	}

	/**
	 * このBMS読み込みエラーの一般的なエラーメッセージを返します。
	 * @return エラーメッセージ
	 */
	@Override
	public String toString() {
		var sb = new StringBuilder(1024);
		sb.append("L");
		sb.append(mLineNumber);
		sb.append(": ");
		sb.append(getType().name());
		if (getMessage() != null) {
			sb.append("(");
			sb.append(getMessage());
			sb.append(")");
		}
		sb.append(": ");
		sb.append(mLine);
		return sb.toString();
	}

	/**
	 * エラー発生行の行番号を取得します。
	 * @return 行番号
	 */
	public final int getLineNumber() {
		return mLineNumber;
	}

	/**
	 * エラー発生行の記述を取得します。
	 * @return 行の記述内容
	 */
	public final String getLine() {
		return mLine;
	}
}
