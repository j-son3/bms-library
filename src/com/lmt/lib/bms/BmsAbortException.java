package com.lmt.lib.bms;

/**
 * BMSコンテンツの読み取りが中止された時にスローされる例外です。
 *
 * <p>{@link BmsLoader}による外部データからのBMSコンテンツの読み取り処理において、呼び出し側が
 * 読み取りを中止すべきであると判定した時に、{@link BmsLoader}からスローされる例外です。
 * この例外は{@link BmsException}を継承した例外であるため、BMSコンテンツの読み込み処理時に発生する
 * 本例外以外と同じように扱うことができます。本例外と他の例外処理を別々にしたい場合はcatchブロックを
 * 分けて記述してください。</p>
 *
 * <p>中止例外のスロー要因となったBMSの記述内容およびエラー要因を特定したい場合は{@link #getError}を
 * 呼び出し、{@link BmsLoadError}の内容を参照してください。</p>
 */
public class BmsAbortException extends BmsException {
	/** 中止の原因になったBMS読み込みエラーの内容 */
	private BmsLoadError mError;

	/**
	 * 指定したBMS読み込みエラーを持つ中止例外を生成します。
	 * @param error BMS読み込みエラー
	 */
	public BmsAbortException(BmsLoadError error) {
		super((error == null) ? null : error.toString(), (error == null) ? null : error.getThrowable());
		mError = error;
	}

	/**
	 * BMS読み込みエラーを取得します。
	 * @return BMS読み込みエラー
	 */
	public BmsLoadError getError() {
		return mError;
	}
}
