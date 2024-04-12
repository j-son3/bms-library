package com.lmt.lib.bms;

/**
 * BMSライブラリ特有の例外です。
 *
 * <p>BMSライブラリで発生し得る例外のうち、呼び出し側で発生時の振る舞いを明確に決定するべき事象について、
 * BMSライブラリはBmsExceptionをスローします。当例外には、一般的なJava言語の例外の情報を超える情報はありません。
 * 必要があれば{@link Throwable#getCause}を用いてBmsExceptionの発生要因となった例外を知ることができます。
 * 但し、BMSライブラリ独自の処理によってBmsExceptionがスローされた場合はnullを返します。</p>
 *
 * <p>BmsExceptionをスローする要因の代表的な例を以下に列挙します。(全てではありません)<br>
 * - BMSコンテンツの外部データからの読み込み時に続行不可能なエラーを検出した場合<br>
 * - BMSコンテンツの外部データへの書き込み時に続行不可能なエラーを検出した場合</p>
 *
 * <p>BmsExceptionを継承する例外以外は、全てRuntimeExceptionを継承する例外であるため、例外のハンドリングを
 * 省略することができます。但し、BMSライブラリでは基本的にどのメソッドでも例外をスローする可能性があるため、
 * 特にライブラリへの入力パラメータに不整合が発生し得るような可能性が生じるケースにおいては、できるだけ
 * 例外のハンドリングを考慮に入れてください。</p>
 */
public class BmsException extends Exception {
	/**
	 * BMS例外を生成します。
	 */
	public BmsException() {
		super();
	}

	/**
	 * 指定したメッセージを持つBMS例外を生成します。
	 * @param message メッセージ
	 */
	public BmsException(String message) {
		super(message);
	}

	/**
	 * 指定したメッセージと原因を持つBMS例外を生成します。
	 * @param message メッセージ
	 * @param cause 原因
	 */
	public BmsException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * 指定した原因を持つBMS例外を生成します。
	 * @param cause 原因
	 */
	public BmsException(Throwable cause) {
		super(cause);
	}
}
