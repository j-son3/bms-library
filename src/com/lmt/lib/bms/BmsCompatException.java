package com.lmt.lib.bms;

/**
 * BMSコンテンツの入出力処理においてデータ互換性に関する問題が発生したことを表す例外です。
 */
public class BmsCompatException extends BmsException {
	/**
	 * 互換性例外を生成します。
	 */
	public BmsCompatException() {
		// Do nothing
	}

	/**
	 * 指定したメッセージを持つ互換性例外を生成します。
	 * @param message メッセージ
	 */
	public BmsCompatException(String message) {
		super(message);
	}

	/**
	 * 指定したメッセージ・を持つ互換性例外を生成します。
	 * @param message メッセージ
	 * @param cause 原因
	 */
	public BmsCompatException(String message, Throwable cause) {
		super(message, cause);
	}
}
