package com.lmt.lib.bms;

import static com.lmt.lib.bms.internal.Assertion.*;

import java.util.Objects;

/**
 * BMSに関連するエラー情報を表します。
 *
 * <p>保有できる情報はJava標準の例外({@link java.lang.Exception})とほぼ同等ですが、当クラスの目的は例外とは異なります。
 * BMSに関連する処理では、読み込み等の1操作について一度に複数のエラーが発生することがあり、発生したエラーをリスト化したい
 * ケースが度々あります。そのようなケースでは、例外オブジェクトとして情報を持つとスタックトレース等の冗長な情報を
 * 大量に保有することになり、メモリ使用量の観点で好ましくない状況となります。</p>
 *
 * <p>当クラスでは保有する情報は以下の通りです。</p>
 *
 * <ul>
 * <li><strong>エラー種別</strong><br>
 * {@link BmsErrorType}で列挙するエラーの種類を示す値です。この値は、主にBMS読み込みにおいて散見されるエラーの種類を
 * 大まかに分類したものにになっています。より厳密なエラー処理を行いたいアプリケーションでは、エラーになった箇所を分析し、
 * エラー発生後の振る舞いを決定するべきです。<br><br></li>
 * <li><strong>エラーメッセージ(オプション)</strong><br>
 * 発生したエラーの詳細情報が含まれるメッセージです。エラーの内容によっては具体的なエラー原因を示す文字列が
 * 設定されることがありますが未設定(null)の場合もあります。<br><br></li>
 * <li><strong>エラー原因となった例外(オプション)</strong><br>
 * 発生したエラーの直接的な原因となった例外を示します。アプリケーションはこの例外を用いて処理を分岐したり、
 * 例外の詳細な内容をログ出力したりすることができます。例外に起因しないエラーも多数あるため、多くのケースでこの値は
 * 未設定(null)を示します。</li>
 * </ul>
 *
 * @since 0.4.0
 */
public class BmsError {
	/** エラー種別 */
	private BmsErrorType mErrType;
	/** エラーメッセージ */
	private String mMessage;
	/** エラー原因となった例外 */
	private Throwable mCause;

	/**
	 * 新しいBMSエラー情報オブジェクトを汎用BMSエラー情報として構築します。
	 * @param message エラーメッセージ
	 */
	public BmsError(String message) {
		this(BmsErrorType.COMMON, message, null);
	}

	/**
	 * 新しいBMSエラー情報オブジェクトを汎用BMSエラー情報として構築します。
	 * <p>何らかの例外がスローされたことによるエラーの場合、スローされた例外を入力情報とすることを想定しています。</p>
	 * @param message エラーメッセージ
	 * @param cause エラー原因となった例外
	 */
	public BmsError(String message, Throwable cause) {
		this(BmsErrorType.COMMON, message, cause);
	}

	/**
	 * コンストラクタ
	 * @param errType エラー種別
	 * @param message エラーメッセージ
	 * @param cause エラー原因となった例外
	 * @exception NullPointerException errTypeがnull
	 */
	BmsError(BmsErrorType errType, String message, Throwable cause) {
		assertArgNotNull(errType, "errType");
		mErrType = errType;
		mMessage = message;
		mCause = cause;
	}

	/**
	 * このBMSエラー情報の文字列表現を取得します。
	 * <p>汎用BMSエラー情報の場合、当メソッドが返す内容はエラーメッセージと等価になります。
	 * ただし、エラーメッセージにnullが設定された場合は空文字を返します。</p>
	 * @return BMSエラー情報の文字列表現
	 */
	@Override
	public String toString() {
		return Objects.requireNonNullElse(mMessage, "");
	}

	/**
	 * エラー種別を取得します。
	 * @return エラー種別
	 */
	public final BmsErrorType getType() {
		return mErrType;
	}

	/**
	 * エラーメッセージを取得します。
	 * <p>この情報は任意情報です。未設定の場合、nullが返ります。</p>
	 * @return エラーメッセージ
	 */
	public String getMessage() {
		return mMessage;
	}

	/**
	 * エラー原因となった例外を取得します。
	 * <p>この情報は任意情報です。未設定の場合、nullが返ります。</p>
	 * @return エラー原因となった例外
	 */
	public Throwable getCause() {
		return mCause;
	}
}
