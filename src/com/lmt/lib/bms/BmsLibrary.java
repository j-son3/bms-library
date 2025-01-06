package com.lmt.lib.bms;

import static com.lmt.lib.bms.internal.Assertion.*;

import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * BMSライブラリに関する基本的な情報の設定・取得を行うためのクラスです。
 *
 * @since 0.7.0
 */
public class BmsLibrary {
	/** ライブラリ本体バージョン */
	private static final String LIBRARY_VERSION = "0.9.0";

	/**
	 * デフォルトの文字セットリスト
	 * ※デフォルトでは MS932 のみ("～"が文字化けするため"Shift-JIS"は使用しない)
	 */
	private static List<Charset> sDefaultCharsets = List.of(Charset.forName("MS932"));

	/**
	 * BMSライブラリ本体のバージョンを取得します。
	 * @return BMSライブラリ本体のバージョン
	 */
	public static String getVersion() {
		return LIBRARY_VERSION;
	}

	/**
	 * BMSライブラリが扱う最優先文字セットを取得します。
	 * <p>{@link BmsLoader}によるBMS読み込み時、スクリプトをデコードする際に最初に使用する文字セットとなります。
	 * また、{@link BmsSaver}によるBMS書き込み時、出力文字セット未指定時の規定文字セットとしても使用されます。</p>
	 * <p>当メソッドは {@link #getDefaultCharsets()}.get(0) と等価です。</p>
	 * @return BMSライブラリが扱う最優先文字セット
	 * @see #getDefaultCharsets()
	 * @since 0.7.0
	 */
	public static Charset getPrimaryCharset() {
		return sDefaultCharsets.get(0);
	}

	/**
	 * BMSライブラリが扱うデフォルトの文字セットのリストを取得します。
	 * <p>最優先文字セットはリストの先頭に格納されています。その値は{@link #getPrimaryCharset()}と等価になります。
	 * 文字セットが複数指定されている場合はリストに2個以上の文字セットが格納されています。その場合、リストの中に
	 * 同じ文字セットが格納されることはありません。</p>
	 * <p>返されるリストは変更できません。</p>
	 * @return 文字セットのリスト
	 * @see #setDefaultCharsets(Charset...)
	 * @since 0.7.0
	 */
	public static List<Charset> getDefaultCharsets() {
		return sDefaultCharsets;
	}

	/**
	 * BMSライブラリが扱うデフォルトの文字セットを優先順に設定します。
	 * <p>パラメータの先頭で指定した文字セットが最優先文字セットになります(指定必須)。
	 * 文字セットは2個以上指定することができますが、必須ではありません。しかし、複数指定することで{@link BmsLoader}
	 * によるBMS読み込み時、文字化けの発生しない文字セットを自動的に探し出してくれるようになります。</p>
	 * <p>指定した文字セットが重複している場合、後方で指定した同一の文字セットは無視されます。</p>
	 * <p>指定する文字セットの中にnullを含めてはなりません。nullが含まれていると例外がスローされます。</p>
	 * @param charsets デフォルトの文字セット(複数指定可)
	 * @exception IllegalArgumentException 文字セットが0件
	 * @exception NullPointerException 文字セットにnullが含まれている
	 * @since 0.7.0
	 */
	public static void setDefaultCharsets(Charset...charsets) {
		// エラーチェック
		assertArg(charsets.length > 0, "Default charsets is not specified.");
		for (var cs : charsets) {
			assertArgNotNull(cs, "charsets[?]");
		}

		// ユニークな文字セットのリストを構築する
		var uniqueChasets = Stream.of(charsets).distinct().collect(Collectors.toList());
		sDefaultCharsets = Collections.unmodifiableList(uniqueChasets);
	}

	/**
	 * BMSライブラリのロゴを標準出力に出力します。
	 * @since 0.8.0
	 */
	public static void printLogo() {
		printLogo(System.out);
	}

	/**
	 * BMSライブラリのロゴを指定した出力ストリームに出力します。
	 * @param ps ロゴの出力先ストリーム
	 * @exception NullPointerException psがnull
	 * @since 0.8.0
	 */
	public static void printLogo(PrintStream ps) {
		assertArgNotNull(ps, "ps");
		ps.println("   _____  ___ __  _____");
		ps.println("  / __  \\/ _ '_ \\/ ____)");
		ps.println(" / __  </ // // /\\___ \\");
		ps.println("/______/_//_//_/(_____/ Library v" + LIBRARY_VERSION);
		ps.println("======================================");
	}
}
