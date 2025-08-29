package com.lmt.lib.bms;

/**
 * ユーザープログラムの処理異常を検出したことを表す例外です。
 *
 * <p>BMSライブラリではBMSコンテンツのローダ・セーバのようにハンドラ追加・クラス拡張により機能拡張が可能です。
 * 拡張方法には規定のルールがあり、それに違反するとライブラリは本来のサービスを提供できなくなります。
 * 当例外はそのような事象を検出した場合にスローされ、処理が停止します。</p>
 *
 * <p>当例外がスローされることは、ユーザープログラムが不具合を内包していることを表します。
 * 例外のメッセージ、および原因となる例外の内容を参照し不具合の原因を解決してください。</p>
 *
 * @since 0.10.0
 */
public class BmsHandleException extends BmsException {
	/**
	 * 新しい例外オブジェクトを構築します。
	 */
	public BmsHandleException() {
		super();
	}

	/**
	 * 指定したメッセージと原因を持つ新しい例外オブジェクトを構築します。
	 * @param message メッセージ
	 * @param cause 原因
	 */
	public BmsHandleException(String message, Throwable cause) {
		super(message, cause);
	}
}
