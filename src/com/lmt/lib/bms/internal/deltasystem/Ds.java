package com.lmt.lib.bms.internal.deltasystem;

import static com.lmt.lib.bms.internal.Assertion.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.function.Consumer;

/**
 * Delta System制御用クラス
 *
 * <p>BMSライブラリの一般ユーザーがDelta Systemにアクセスできるのは、Be Musicサブセットを通してのみとなる。
 * このクラスは開発者がデバッグを行う際に必要となる機能をまとめ、非公式にDelta Systemの動作を制御することを
 * 目的としている。そのためこのクラスはドキュメント化の対象外となり、一般ユーザーが使用した結果不都合な動作を
 * したとしてもBMSライブラリ開発者はそのことについて関知しない。</p>
 */
public class Ds {
	/** アルゴリズムのメジャーバージョン */
	public static final int ALGORITHM_MAJOR_VERSION = 0;
	/** アルゴリズムのリビジョン番号 */
	public static final int ALGORITHM_REVISION_NUMBER = 0;
	/** アルゴリズムのステータス D=Draft / R=Reviewing / F=Fixed */
	public static final char ALGORITHM_STATUS_CHAR = 'D';
	/** アルゴリズムパラメータ変更ステータス ''=変更なし / C=変更あり */
	public static final char ALGORITHM_CONFIG_CHANGED = 'C';

	/** 設定情報一式 */
	private static final List<? extends RatingConfig> CONFIGS = List.of(
			CommonConfig.getInstance(),
			ComplexConfig.getInstance(),
			PowerConfig.getInstance(),
			RhythmConfig.getInstance());
	/** 空デバッグハンドラ */
	private static final Consumer<String> EMPTY_DEBUG_HANDLER = o -> {};

	/** デバッグモード */
	private static DsDebugMode sDebugMode = DsDebugMode.NONE;
	/** デバッグハンドラ */
	private static Consumer<String> sDebugHandler = EMPTY_DEBUG_HANDLER;
	/** パラメータ変更状態 */
	private static boolean sConfigChanged = false;

	/**
	 * デバッグモード設定
	 * @param debugMode デバッグモード
	 */
	public static void setDebugMode(DsDebugMode debugMode) {
		sDebugMode = debugMode;
	}

	/**
	 * デバッグモード取得
	 * @return デバッグモード
	 */
	static DsDebugMode getDebugMode() {
		return sDebugMode;
	}

	/**
	 * デバッグハンドラ設定
	 * <p>デバッグ出力の際、ハンドラにデバッグ出力するべき情報が渡されてくる。
	 * ハンドラではprintln()等の出力機能を用いてデバッグ情報を出力すること。</p>
	 * @param handler デバッグハンドラ
	 */
	public static void setDebugHandler(Consumer<String> handler) {
		sDebugHandler = Objects.requireNonNullElse(handler, EMPTY_DEBUG_HANDLER);
	}

	/**
	 * アルゴリズムパラメータ変更状態取得
	 * <p>この状態は{@link #loadConfig(Path)}によってパラメータ変更を行った場合にtrueとなる。
	 * 実際にパラメータが変更されたかどうかではなく、前述のメソッドが実行されたかどうかを表す。</p>
	 * @return パラメータ変更が行われていた場合true
	 */
	public static boolean isConfigChanged() {
		return sConfigChanged;
	}

	/**
	 * デバッグ機能の有効状態判定
	 * @return デバッグモードが{@link DsDebugMode#NONE}以外であればtrue
	 */
	static boolean isDebugEnable() {
		return sDebugMode != DsDebugMode.NONE;
	}

	/**
	 * デバッグ出力
	 * @param msg メッセージ
	 */
	static void debug(Object msg) {
		if (isDebugEnable()) {
			sDebugHandler.accept(msg.toString());
		}
	}

	/**
	 * デバッグ出力
	 * <p>String.format()を利用してメッセージを生成して出力する。</p>
	 * @param format 出力形式
	 * @param args 出力引数
	 */
	static void debug(String format, Object...args) {
		if (isDebugEnable()) {
			sDebugHandler.accept(String.format(format, args));
		}
	}

	/**
	 * デバッグ出力(long型引数)
	 * @param format 出力形式
	 * @param a1 出力引数1
	 */
	static void debug(String format, long a1) {
		debug(format, a1, 0, 0);
	}

	/**
	 * デバッグ出力(long型引数)
	 * @param format 出力形式
	 * @param a1 出力引数1
	 * @param a2 出力引数2
	 */
	static void debug(String format, long a1, long a2) {
		debug(format, a1, a2, 0);
	}

	/**
	 * デバッグ出力(long型引数)
	 * @param format 出力形式
	 * @param a1 出力引数1
	 * @param a2 出力引数2
	 * @param a3 出力引数3
	 */
	static void debug(String format, long a1, long a2, long a3) {
		if (isDebugEnable()) {
			sDebugHandler.accept(String.format(format, a1, a2, a3));
		}
	}

	/**
	 * デバッグ出力(double型引数)
	 * @param format 出力形式
	 * @param a1 出力引数1
	 */
	static void debug(String format, double a1) {
		debug(format, a1, 0, 0, 0, 0);
	}

	/**
	 * デバッグ出力(double型引数)
	 * @param format 出力形式
	 * @param a1 出力引数1
	 * @param a2 出力引数2
	 */
	static void debug(String format, double a1, double a2) {
		debug(format, a1, a2, 0, 0, 0);
	}

	/**
	 * デバッグ出力(double型引数)
	 * @param format 出力形式
	 * @param a1 出力引数1
	 * @param a2 出力引数2
	 * @param a3 出力引数3
	 */
	static void debug(String format, double a1, double a2, double a3) {
		if (isDebugEnable()) {
			sDebugHandler.accept(String.format(format, a1, a2, a3));
		}
	}

	/**
	 * Delta Systemの設定を設定ファイルから読み込む
	 * @param configFilePath 設定ファイルパス(Java標準の.properties形式)
	 * @throws IOException 設定ファイル読み込み失敗時
	 */
	public static void loadConfig(Path configFilePath) throws IOException {
		assertArgNotNull(configFilePath, "configFilePath");
		try (var reader = Files.newBufferedReader(configFilePath, StandardCharsets.UTF_8)) {
			// 設定ファイルを読み込む
			var config = new Properties();
			config.load(reader);

			// 全てのレーティング種別の設定を展開する
			CONFIGS.stream().forEach(c -> c.load(config));
			sConfigChanged = true;
		}
	}

	/**
	 * Delta Systemの全設定をデバッグ出力
	 * <p>ただし、出力するのはデバッグモードが{@link DsDebugMode#DETAIL}時のみ。</p>
	 */
	public static void printAllSettings() {
		if (getDebugMode() == DsDebugMode.DETAIL) {
			Ds.debug("Delta System all settings ----------");
			Finger.print();
			Fingering.print();
			CONFIGS.stream().forEach(c -> c.print());
		}
	}
}
