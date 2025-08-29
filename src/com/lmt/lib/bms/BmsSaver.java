package com.lmt.lib.bms;

import static com.lmt.lib.bms.internal.Assertion.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;

/**
 * BMSコンテンツを外部データへ出力するセーバーの基底クラスです。
 *
 * <p>当クラスではBMSコンテンツの内容を外部データとして出力する機能を提供します。
 * 外部データはファイル等の何らかの形式で記録されていることを想定しています。
 * {@link java.io.OutputStream}による出力が可能であれば、どのような媒体でもBMSコンテンツを書き出すことができます。</p>
 *
 * @since 0.0.1
 */
public abstract class BmsSaver {
	/**
	 * 指定したパスのファイルにBMSコンテンツを出力します。
	 * <p>当メソッドは指定パスのファイルを書き込みモードでオープンし{@link #save(BmsContent, OutputStream)}を実行します。</p>
	 * @param content 出力対象のBMSコンテンツ
	 * @param dst 出力先パス
	 * @throws NullPointerException contentがnull
	 * @throws NullPointerException dstがnull
	 * @throws IllegalArgumentException contentが参照モードではない
	 * @throws IOException 指定パスへのファイル作成失敗、またはBMSコンテンツの出力失敗
	 * @throws BmsHandleException ユーザープログラムの処理異常を検出した
	 */
	public void save(BmsContent content, Path dst) throws IOException {
		assertArgNotNull(content, "content");
		assertArgNotNull(dst, "dst");
		assertArg(content.isReferenceMode(), "'content' is NOT reference mode.");
		try (var os = new FileOutputStream(dst.toFile())) {
			saveMain(content, os);
		}
	}

	/**
	 * 指定したファイルにBMSコンテンツを出力します。
	 * <p>当メソッドは指定ファイルを書き込みモードでオープンし{@link #save(BmsContent, OutputStream)}を実行します。</p>
	 * @param content 出力対象のBMSコンテンツ
	 * @param dst 出力先パス
	 * @throws NullPointerException contentがnull
	 * @throws NullPointerException dstがnull
	 * @throws IllegalArgumentException contentが参照モードではない
	 * @throws IOException 指定ファイルへのファイル作成失敗、またはBMSコンテンツの出力失敗
	 * @throws BmsHandleException ユーザープログラムの処理異常を検出した
	 */
	public void save(BmsContent content, File dst) throws IOException {
		assertArgNotNull(content, "content");
		assertArgNotNull(dst, "dst");
		assertArg(content.isReferenceMode(), "'content' is NOT reference mode.");
		try (var os = new FileOutputStream(dst)) {
			saveMain(content, os);
		}
	}

	/**
	 * 指定した出力ストリームにBMSコンテンツを出力します。
	 * <p>当メソッドは指定されたBMSコンテンツのエラーチェックを行った後、処理を{@link #onWrite(BmsContent, OutputStream)}
	 * に委譲します。</p>
	 * @param content 出力対象のBMSコンテンツ
	 * @param dst 出力先ストリーム
	 * @throws NullPointerException contentがnull
	 * @throws NullPointerException dstがnull
	 * @throws IllegalArgumentException contentが参照モードではない
	 * @throws IOException BMSコンテンツ出力時、異常を検知した
	 * @throws BmsHandleException ユーザープログラムの処理異常を検出した
	 */
	public void save(BmsContent content, OutputStream dst) throws IOException {
		assertArgNotNull(content, "content");
		assertArgNotNull(dst, "dst");
		assertArg(content.isReferenceMode(), "'content' is NOT reference mode.");
		saveMain(content, dst);
	}

	/**
	 * BMSコンテンツ出力処理メイン
	 * @param content 出力対象のBMSコンテンツ
	 * @param dst 出力先ストリーム
	 * @throws IOException BMSコンテンツ出力時、異常を検知した
	 * @throws BmsHandleException ユーザープログラムの処理異常を検出した
	 */
	private void saveMain(BmsContent content, OutputStream dst) throws IOException {
		try {
			onWrite(content, dst);
		} catch (IOException e) {
			throw e;
		} catch (Exception e) {
			throw new BmsHandleException("Thrown un-expected exception by onWrite()", e);
		}
	}

	/**
	 * BMSコンテンツの出力処理を実行します。
	 * <p>当メソッドに指定されるBMSコンテンツ、出力ストリームは呼び出し前にNot Null保証されています。
	 * また、BMSコンテンツは参照モードであることが保証されているため、全てのデータ読み出しが行える状態です。</p>
	 * <p>BMSコンテンツは、実装先セーバーが規定するフォーマットで出力ストリームに書き出されます。
	 * 出力フォーマットの具体的な調整は実装先セーバーで行ってください。</p>
	 * <p>出力処理で何らかの異常により例外({@link java.io.IOException}以外)がスローされた場合、{@link BmsException}
	 * がスローされます。具体的な要因は{@link BmsException#getCause()}を参照してください。
	 * ただし、原因によってはnullが返る場合があります。</p>
	 * @param content 出力対象のBMSコンテンツ
	 * @param dst 出力先ストリーム
	 * @throws IOException dstへのBMSコンテンツ出力時に入出力エラーが発生した時
	 */
	protected abstract void onWrite(BmsContent content, OutputStream dst) throws IOException;
}
