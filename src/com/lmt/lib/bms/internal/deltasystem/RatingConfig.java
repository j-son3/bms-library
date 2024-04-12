package com.lmt.lib.bms.internal.deltasystem;

import java.util.Properties;

/**
 * Delta Systemの設定情報ベースクラス
 */
abstract class RatingConfig {
	/**
	 * 設定情報の読み込み
	 * <p>指定されたプロパティ情報から当該レーティング種別に対応する設定情報を読み込み、設定内容を反映させる。</p>
	 * @param config プロパティ情報
	 */
	abstract void load(Properties config);

	/**
	 * 設定情報のデバッグ出力
	 */
	abstract void print();
}
