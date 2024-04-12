package com.lmt.lib.bms;

/**
 * {@link BmsLoader}の読み込みオプションを参照するインターフェイスです。
 *
 * <p>当インターフェイスは読み込みハンドラ({@link BmsLoadHandler}を実装したオブジェクト)からローダーの設定内容を参照し、
 * ハンドラの動作を決定する目的で用います。読み込みオプションは{@link BmsLoadHandler#startLoad(BmsLoaderSettings)}
 * によって読み込みを開始する直前にローダーから通知されます。</p>
 *
 * @see BmsLoader
 * @see BmsLoadHandler
 */
public interface BmsLoaderSettings {
	/**
	 * BMSコンテンツローダーに設定されたBMS仕様を取得します。
	 * @return BMS仕様
	 */
	BmsSpec getSpec();

	/**
	 * 構文エラーの有効状態を取得します。
	 * @return 構文エラーが有効であればtrue、無効であればfalse
	 */
	boolean isSyntaxErrorEnable();

	/**
	 * BMSライブラリの定める仕様に違反した値を自動的に仕様範囲内に丸め込むかどうかを取得します。
	 * @return 仕様範囲内への丸め込みが有効であればtrue、無効であればfalse
	 */
	boolean isFixSpecViolation();

	/**
	 * BMS仕様に存在しない未知のメタ情報検出エラーを無視するかどうかを取得します。
	 * @return 未知のメタ情報検出エラーを無視する場合はtrue、無視しない場合はfalse
	 */
	boolean isIgnoreUnknownMeta();

	/**
	 * BMS仕様に存在しない未知のチャンネル検出エラーを無視するかどうかを取得します。
	 * @return 未知のチャンネル検出エラーを無視する場合はtrue、無視しない場合はfalse
	 */
	boolean isIgnoreUnknownChannel();

	/**
	 * メタ情報/チャンネルの値不正検出エラーを無視するかどうかを取得します。
	 * @return メタ情報/チャンネルの値不正検出エラーを無視する場合はtrue、無視しない場合はfalse
	 */
	boolean isIgnoreWrongData();
}
