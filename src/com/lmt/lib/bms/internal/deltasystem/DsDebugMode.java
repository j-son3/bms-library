package com.lmt.lib.bms.internal.deltasystem;

/**
 * Delta Systemデバッグモード
 *
 * <p>デバッグモードを指定すると、モードに応じて処理内容が開発者向けになったりデバッグ出力の内容が
 * 変化したりする。BMSライブラリの規定値は「デバッグではない状態」であり、一般ユーザー向けの処理内容になっている。
 * 開発者向けにDelta Systemを稼働させたい場合にはデバッグモードを変更すること。</p>
 *
 * <p>デバッグモードの設定については{@link Ds#setDebugMode(DsDebugMode)}、{@link Ds#setDebugHandler(java.util.function.Consumer)}
 * も併せて参照すること。</p>
 */
public enum DsDebugMode {
	/** デバッグなし */
	NONE(false, false),
	/** シンプルデバッグモード */
	SIMPLE(true, true),
	/** 分析結果のサマリを出力する用のモード */
	SUMMARY(false, false),
	/** 譜面1つあたりの詳細な動作情報、および分析結果を出力する用のモード */
	DETAIL(true, true);

	/** デバッグ出力の開始を示す出力が必要かどうか */
	private boolean mNeedStarter;
	/** デバッグ出力の終了を示す出力が必要かどうか */
	private boolean mNeedFinisher;

	/**
	 * コンストラクタ
	 * @param needStarter デバッグ出力の開始を示す出力が必要かどうか
	 * @param needFinisher デバッグ出力の終了を示す出力が必要かどうか
	 */
	private DsDebugMode(boolean needStarter, boolean needFinisher) {
		mNeedStarter = needStarter;
		mNeedFinisher = needFinisher;
	}

	/**
	 * デバッグ出力の開始を示す出力が必要かどうか
	 * @return 必要であればtrue
	 */
	boolean isStarterNecessary() {
		return mNeedStarter;
	}

	/**
	 * デバッグ出力の終了を示す出力が必要かどうか
	 * @return 必要であればtrue
	 */
	boolean isFinisherNecessary() {
		return mNeedFinisher;
	}
}
