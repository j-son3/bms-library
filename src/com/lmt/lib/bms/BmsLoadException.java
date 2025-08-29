package com.lmt.lib.bms;

/**
 * ユーザープログラムの判断によりBMSの読み込みがエラーにより中止されたことを表す例外です。
 *
 * <p>{@link BmsLoader}による外部データからのBMSの読み込み処理において、呼び出し側が設定したハンドラによって
 * 読み込みを中止すべきであると判定した時、または{@link BmsLoader}がエラーを検出した時にスローされます。</p>
 *
 * <p>本例外のスロー要因となったBMSの記述内容およびエラー要因を特定したい場合は{@link #getError()}を
 * 呼び出し、{@link BmsError}の内容を参照してください。</p>
 *
 * @see BmsError
 * @since 0.4.0
 */
public class BmsLoadException extends BmsException {
	/** エラーの原因になったBMS読み込みエラーの内容 */
	private BmsError mError;

	/**
	 * 指定したBMS関連エラーを持つ例外オブジェクトを生成します。
	 * @param error BMS関連エラー
	 */
	public BmsLoadException(BmsError error) {
		super((error == null) ? null : error.toString(), (error == null) ? null : error.getCause());
		mError = error;
	}

	/**
	 * BMS関連エラー情報を取得します。
	 * @return BMS関連エラー情報
	 */
	public BmsError getError() {
		return mError;
	}
}
