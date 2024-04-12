package com.lmt.lib.bms.bemusic;

import static com.lmt.lib.bms.BmsAssertion.*;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.nio.file.Path;

import com.lmt.lib.bms.BmsAbortException;
import com.lmt.lib.bms.BmsChannel;
import com.lmt.lib.bms.BmsException;
import com.lmt.lib.bms.BmsLoader;
import com.lmt.lib.bms.BmsMeta;
import com.lmt.lib.bms.BmsSpec;

/**
 * BeMusicクラスは、ライブラリ上で使用するAPIや定数値などを定義するためのプレースホルダの役割を果たします。
 * <p>当クラスはインスタンスを生成することを想定していません。全てのメンバは静的メンバとして宣言されています。
 * 宣言されたメンバの詳細については、それらのメンバの説明を参照してください。</p>
 */
public class BeMusic {
	/** {@link BeMusicContent#getVisibleNoteCount}の引数。1PのKEY1を示します。 */
	public static final int KEY1 = 0;
	/** {@link BeMusicContent#getVisibleNoteCount}の引数。1PのKEY2を示します。 */
	public static final int KEY2 = 1;
	/** {@link BeMusicContent#getVisibleNoteCount}の引数。1PのKEY3を示します。 */
	public static final int KEY3 = 2;
	/** {@link BeMusicContent#getVisibleNoteCount}の引数。1PのKEY4を示します。 */
	public static final int KEY4 = 3;
	/** {@link BeMusicContent#getVisibleNoteCount}の引数。1PのKEY5を示します。 */
	public static final int KEY5 = 4;
	/** {@link BeMusicContent#getVisibleNoteCount}の引数。1PのKEY6を示します。 */
	public static final int KEY6 = 5;
	/** {@link BeMusicContent#getVisibleNoteCount}の引数。1PのKEY7を示します。 */
	public static final int KEY7 = 6;
	/** {@link BeMusicContent#getVisibleNoteCount}の引数。2PのKEY1を示します。 */
	public static final int KEY8 = 7;
	/** {@link BeMusicContent#getVisibleNoteCount}の引数。2PのKEY2を示します。 */
	public static final int KEY9 = 8;
	/** {@link BeMusicContent#getVisibleNoteCount}の引数。2PのKEY3を示します。 */
	public static final int KEY10 = 9;
	/** {@link BeMusicContent#getVisibleNoteCount}の引数。2PのKEY4を示します。 */
	public static final int KEY11 = 10;
	/** {@link BeMusicContent#getVisibleNoteCount}の引数。2PのKEY5を示します。 */
	public static final int KEY12 = 11;
	/** {@link BeMusicContent#getVisibleNoteCount}の引数。2PのKEY6を示します。 */
	public static final int KEY13 = 12;
	/** {@link BeMusicContent#getVisibleNoteCount}の引数。2PのKEY7を示します。 */
	public static final int KEY14 = 13;
	/** {@link BeMusicContent#getVisibleNoteCount}の引数。1PのSCRATCHを示します。 */
	public static final int SCRATCH1 = 14;
	/** {@link BeMusicContent#getVisibleNoteCount}の引数。2PのSCRATCHを示します。 */
	public static final int SCRATCH2 = 15;
	/** {@link BeMusicContent#getVisibleNoteCount}の引数。1PのKEY1におけるロングノートを示します。 */
	public static final int KEY1_LN = 16;
	/** {@link BeMusicContent#getVisibleNoteCount}の引数。1PのKEY2におけるロングノートを示します。 */
	public static final int KEY2_LN = 17;
	/** {@link BeMusicContent#getVisibleNoteCount}の引数。1PのKEY3におけるロングノートを示します。 */
	public static final int KEY3_LN = 18;
	/** {@link BeMusicContent#getVisibleNoteCount}の引数。1PのKEY4におけるロングノートを示します。 */
	public static final int KEY4_LN = 19;
	/** {@link BeMusicContent#getVisibleNoteCount}の引数。1PのKEY5におけるロングノートを示します。 */
	public static final int KEY5_LN = 20;
	/** {@link BeMusicContent#getVisibleNoteCount}の引数。1PのKEY6におけるロングノートを示します。 */
	public static final int KEY6_LN = 21;
	/** {@link BeMusicContent#getVisibleNoteCount}の引数。1PのKEY7におけるロングノートを示します。 */
	public static final int KEY7_LN = 22;
	/** {@link BeMusicContent#getVisibleNoteCount}の引数。2PのKEY1におけるロングノートを示します。 */
	public static final int KEY8_LN = 23;
	/** {@link BeMusicContent#getVisibleNoteCount}の引数。2PのKEY2におけるロングノートを示します。 */
	public static final int KEY9_LN = 24;
	/** {@link BeMusicContent#getVisibleNoteCount}の引数。2PのKEY3におけるロングノートを示します。 */
	public static final int KEY10_LN = 25;
	/** {@link BeMusicContent#getVisibleNoteCount}の引数。2PのKEY4におけるロングノートを示します。 */
	public static final int KEY11_LN = 26;
	/** {@link BeMusicContent#getVisibleNoteCount}の引数。2PのKEY5におけるロングノートを示します。 */
	public static final int KEY12_LN = 27;
	/** {@link BeMusicContent#getVisibleNoteCount}の引数。2PのKEY6におけるロングノートを示します。 */
	public static final int KEY13_LN = 28;
	/** {@link BeMusicContent#getVisibleNoteCount}の引数。2PのKEY7におけるロングノートを示します。 */
	public static final int KEY14_LN = 29;
	/** {@link BeMusicContent#getVisibleNoteCount}の引数。1PのSCRATCHにおけるロングノートを示します。 */
	public static final int SCRATCH1_LN = 30;
	/** {@link BeMusicContent#getVisibleNoteCount}の引数。2PのSCRATCHにおけるロングノートを示します。 */
	public static final int SCRATCH2_LN = 31;
	/** {@link BeMusicContent#getVisibleNoteCount}の引数。総ノート数を示します。 */
	public static final int TOTAL_NOTES = 32;

	/** どのフラグも立っていない値 */
	static final int FLAG_NONE = 0x00000000;
	/** 当該BeMusicコンテンツがDPであると判定するチャンネルに定義があることを示すフラグ */
	static final int FLAG_DOUBLE = 0x00000001;
	/** 当該BeMusicコンテンツにロングノートが含まれていることを示すフラグ */
	static final int FLAG_LN = 0x00000002;
	/** 当該BeMusicコンテンツにBGAが含まれていることを示すフラグ */
	static final int FLAG_BGA = 0x00000004;
	/** 当該BeMusicコンテンツにてマイナーなBMSプレーヤーしか対応していない定義が使用されていることを示すフラグ */
	static final int FLAG_MINOR = 0x00000008;
	/** 当該BeMusicコンテンツで旧式の定義が使用されていることを示すフラグ */
	static final int FLAG_LEGACY = 0x00000010;
	/** 当該BeMusicコンテンツで非推奨の定義が使用されていることを示すフラグ */
	static final int FLAG_UNRECOMMEND = 0x00000020;

	/** BeMusicライブラリ初期化時に指定されたバージョンのBMS仕様 */
	private static BmsSpec sSpec = null;

	/**
	 * BeMusicライブラリを初期化します。
	 * <p>アプリケーションがBeMusicライブラリを使用する際は、必ず当メソッドを1回呼ばなければなりません。BeMusicライブラリは当メソッドで指定された
	 * パラメータでライブラリの初期化を行います。</p>
	 * <p>初期化処理では、アプリケーションが指定したパラメータを用い、BeMusicライブラリで使用するBMS仕様を初期化します。以後、BeMusicコンテンツを
	 * 読み込む際はこのBMS仕様に従います。</p>
	 * <p>任意型メタ情報、およびユーザーチャンネルは、アプリケーションの都合に応じて定義してください。これらの定義が不要な場合はnullを指定しても
	 * 構いません。メタ情報に任意型以外のメタ情報を指定したり、ユーザーチャンネルに仕様チャンネルを含めたりすると初期化が失敗するので注意してください。
	 * また、同じ構成単位で同じ名前のメタ情報が存在したり、同じチャンネル番号を持つユーザーチャンネルが存在したりなど、BMS仕様のルールに違反した場合も
	 * 例外がスローされます。詳しくはBmsSpecBuilder#createを参照してください。</p>
	 * @param specVersion BeMusicのBMS仕様バージョン。{@link BeMusicSpec}にバージョンの定数値の定義があります。
	 * @param objectMetas BMS仕様に含める任意型メタ情報のリスト
	 * @param userChannels BMS仕様に含めるユーザーチャンネルのリスト
	 * @exception IllegalStateException 初期化済みで当メソッドを呼び出した
	 * @exception IllegalArgumentException specVersionに未知の値を指定した
	 * @exception IllegalArgumentException objectMetasのリスト内に任意型以外のメタ情報が含まれていた
	 * @exception IllegalArgumentException userChannelsのリスト内に仕様チャンネルが含まれていた
	 */
	public static void initialize(int specVersion, BmsMeta[] objectMetas, BmsChannel[] userChannels) {
		assertNotInitialized();
		sSpec = BeMusicSpec.create(specVersion, objectMetas, userChannels);
	}

	/**
	 * BeMusicライブラリが初期化されているかどうかを返します。
	 * <p>BeMusicライブラリの初期化は{@link #initialize}で行います。</p>
	 * @return BeMusicライブラリが初期化済みの場合true
	 */
	public static boolean isInitialized() {
		return sSpec != null;
	}

	/**
	 * BeMusicのBMS仕様を取得します。
	 * @return BeMusicのBMS仕様
	 * @exception IllegalStateException BeMusicライブラリが初期化されていない
	 */
	public static BmsSpec getSpec() {
		assertInitialized();
		return sSpec;
	}

	/**
	 * 新しいBeMusicコンテンツオブジェクトを生成します。
	 * <p>BMS仕様は{@link #getSpec}で取得可能なオブジェクトを使用します。</p>
	 * @return BeMusicコンテンツオブジェクト
	 * @exception IllegalStateException BeMusicライブラリが初期化されていない
	 */
	public static BeMusicContent createContent() {
		assertInitialized();
		return new BeMusicContent(sSpec);
	}

	/**
	 * 指定されたファイルからBeMusicコンテンツを読み込みます。
	 * @param file BMSファイル
	 * @return BeMusicコンテンツオブジェクト
	 * @exception IllegalStateException BeMusicライブラリが初期化されていない
	 * @exception BmsException fileがnull: Cause NullPointerException
	 * @exception BmsException BMS宣言にてencoding指定時、指定の文字セットが未知: Cause Exception
	 * @exception BmsException その他、IOエラーなどでファイルアクセスに失敗した場合
	 * @exception BmsAbortException BMS解析エラーが発生した
	 */
	public static BeMusicContent loadContent(File file) throws BmsException {
		assertInitialized();
		return (BeMusicContent)createLoader().load(file);
	}

	/**
	 * 指定されたパスからBeMusicコンテンツを読み込みます。
	 * @param path BMSファイルのパス
	 * @return BeMusicコンテンツオブジェクト
	 * @exception IllegalStateException BeMusicライブラリが初期化されていない
	 * @exception BmsException pathがnull: Cause NullPointerException
	 * @exception BmsException その他、IOエラーなどでパスが示すコンテンツへのアクセスに失敗した場合
	 * @exception BmsAbortException BMS解析エラーが発生した
	 */
	public static BeMusicContent loadContent(Path path) throws BmsException {
		assertInitialized();
		return (BeMusicContent)createLoader().load(path);
	}

	/**
	 * 指定された入力ストリームからBeMusicコンテンツを読み込みます。
	 * @param stream BMSの入力ストリーム
	 * @return BeMusicコンテンツオブジェクト
	 * @exception IllegalStateException BeMusicライブラリが初期化されていない
	 * @exception BmsException streamがnull: Cause NullPointerException
	 * @exception BmsException その他、IOエラーなどでストリームが示すコンテンツへのアクセスに失敗した場合
	 * @exception BmsAbortException BMS解析エラーが発生した
	 */
	public static BeMusicContent loadContent(InputStream stream) throws BmsException {
		assertInitialized();
		return (BeMusicContent)createLoader().load(stream);
	}

	/**
	 * 指定されたReaderからBeMusicコンテンツを読み込みます。
	 * @param reader BMSのReader
	 * @return BeMusicコンテンツオブジェクト
	 * @exception IllegalStateException BeMusicライブラリが初期化されていない
	 * @exception BmsException readerがnull: Cause NullPointerException
	 * @exception BmsAbortException BMS解析エラーが発生した
	 */
	public static BeMusicContent loadContent(Reader reader) throws BmsException {
		assertInitialized();
		return (BeMusicContent)createLoader().load(reader);
	}

	/**
	 * 指定されたバイト配列からBeMusicコンテンツを読み込みます。
	 * @param bms BMSのバイト配列
	 * @return BeMusicコンテンツオブジェクト
	 * @exception IllegalStateException BeMusicライブラリが初期化されていない
	 * @exception BmsException bmsがnull: Cause NullPointerException
	 * @exception BmsAbortException BMS解析エラーが発生した
	 */
	public static BeMusicContent loadContent(byte[] bms) throws BmsException {
		assertInitialized();
		return (BeMusicContent)createLoader().load(bms);
	}

	/**
	 * 指定されたBMS文字列からBeMusicコンテンツを読み込みます。
	 * @param bms BMS文字列
	 * @return BeMusicコンテンツオブジェクト
	 * @exception IllegalStateException BeMusicライブラリが初期化されていない
	 * @exception BmsException bmsがnull: Cause NullPointerException
	 * @exception BmsAbortException BMS解析エラーが発生した
	 */
	public static BeMusicContent loadContent(String bms) throws BmsException {
		assertInitialized();
		return (BeMusicContent)createLoader().load(bms);
	}

	/**
	 * BMS読み込み用のローダーを生成する。
	 * @return BMS読み込み用のローダー
	 */
	private static BmsLoader createLoader() {
		return new BmsLoader()
				.setSpec(sSpec)
				.setHandler(new BeMusicLoadHandler());
	}

	/**
	 * BeMusicライブラリ未初期化をチェックするアサーション。
	 * @exception IllegalStateException BeMusicライブラリ初期化済み
	 */
	private static void assertNotInitialized() {
		assertField((sSpec == null), "BeMusic library is already initialized.");
	}

	/**
	 * BeMusicライブラリ初期化済みをチェックするアサーション。
	 * @exception IllegalStateException BeMusicライブラリが初期化されていない
	 */
	private static void assertInitialized() {
		assertField((sSpec != null), "BeMusic library is not initialized.");
	}
}
