package com.lmt.lib.bms;

/**
 * BMSライブラリの内部処理異常による処理の強制停止が発生したことを表す例外です。
 *
 * <p>当例外がスローされるのは、BMSライブラリの動作仕様において想定外の事象が発生し期待外の例外がスローされた時です。
 * この例外をキャッチした際は {@link Throwable#getCause()} の内容を確認し、例外の根本原因を調査する必要があります。
 * また、これはライブラリの不具合である可能性が非常に高いため、アプリケーション側では当例外を回避する暫定的な回避策が
 * 必要になります。</p>
 *
 * @since 0.10.0
 */
public class BmsPanicException extends BmsException {
	/**
	 * 新しい例外オブジェクトを構築します。
	 */
	public BmsPanicException() {
		super();
	}

	/**
	 * 指定したメッセージと原因を持つ新しい例外オブジェクトを構築します。
	 * @param message メッセージ
	 * @param cause 原因
	 */
	public BmsPanicException(String message, Throwable cause) {
		super(message, cause);
	}
}
