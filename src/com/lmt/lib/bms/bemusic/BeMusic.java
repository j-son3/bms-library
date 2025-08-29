package com.lmt.lib.bms.bemusic;

import static com.lmt.lib.bms.internal.Assertion.*;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

import com.lmt.lib.bms.BmsContent;
import com.lmt.lib.bms.BmsError;
import com.lmt.lib.bms.BmsException;
import com.lmt.lib.bms.BmsLoadException;
import com.lmt.lib.bms.BmsLoader;
import com.lmt.lib.bms.BmsPanicException;
import com.lmt.lib.bms.BmsStandardLoader;

/**
 * Be-Musicに関連する一般的な機能・情報を提供するプレースホルダクラスです。
 *
 * <p>当クラスはBe-Musicに関する最上位レベルのAPI、および定数値などの情報を集約する目的で存在します。
 * 提供される機能はBe-Music関連アプリケーションを素早く作成するために必要なものになっており、
 * 同時にBe-Musicサブセットの機能を理解するために最適な情報が揃っています。</p>
 *
 * @since 0.7.0
 */
public class BeMusic {
	/**
	 * 指定されたファイルパスから適切なBMSローダを選択し生成します。
	 * <p>ファイルの拡張子が「bmson」の場合{@link BeMusicBmsonLoader}、それ以外は{@link BmsStandardLoader}を生成します。
	 * 拡張子の大文字・小文字は区別されません。生成されたローダの各設定内容はデフォルトの状態で返されます。</p>
	 * @param path ファイルパス
	 * @return ファイルパスに対応する適切なBMSローダ
	 * @throws NullPointerException pathがnull
	 */
	public static BmsLoader createLoaderFor(Path path) {
		assertArgNotNull(path, "path");
		var fileName = path.getFileName();
		var fileNameStr = (fileName == null) ? "" : fileName.toString();
		var extPos = fileNameStr.lastIndexOf('.');
		if (extPos == -1) {
			// ファイルの拡張子が見つからない場合は標準フォーマット
			return new BmsStandardLoader();
		} else if (fileNameStr.substring(extPos + 1).equalsIgnoreCase("bmson")) {
			// ファイルの拡張子がbmsonの場合はbmson形式
			return new BeMusicBmsonLoader();
		} else {
			// ファイルの拡張子がbmson以外の場合は標準フォーマット
			return new BmsStandardLoader();
		}
	}

	/**
	 * 最新バージョンのBe-Music用BMS仕様を用いて指定パスのファイルからBMSコンテンツを読み込みます。
	 * <p>当メソッドは最も一般的なBMSコンテンツの読み込み機能を提供します。通常、BeMusicサブセットが持つ柔軟かつ高機能な
	 * 処理を行うためには複数の煩雑な設定を行ったうえで読み込みを行う必要がありますが「ファイルからBMSコンテンツを読み込む」
	 * という一般的な処理を行いたい場合には当メソッドを用いるのが最適な選択肢となります。</p>
	 * <p>BMSローダは{@link #createLoaderFor(Path)}に1番目の引数のファイルパスを指定して生成されたものを使用します。</p>
	 * <p>2番目の引数は、乱数が使用されている場合に固定化された特定の値を使用するか、乱数を都度生成するかを指定します。
	 * 1以上の値を指定すると全ての乱数は指定された値で固定化され、0以下の値を指定すると#IFブロックは常に「偽」
	 * を示すようになります。nullを指定すると#RANDOMが登場する度に乱数が生成されます。
	 * つまり、乱数が使用されたBMSでは読み込みを行う度に異なるBMSコンテンツが生成される可能性が生じます。
	 * <p>3番目の引数は、読み込みの際に厳格なフォーマットチェックを行うかどうかを選択します。
	 * falseを指定するとBMS定義の構文エラー、値の範囲・書式、チャンネルの定義ミスなど、様々な誤りに対して寛容的になり、
	 * 誤りを検出した際にはその誤りを無視して読み込みを続行します。この設定で読み込みを行うと当メソッドが例外をスローする
	 * ケースが減少しますが、代償としてBMS定義の誤りに気付く機会が失われ、読み込まれたBMSコンテンツが意図せずに期待とは異なる
	 * 内容になってしまう可能性があります。trueを指定すると以下のような場合にメソッドが例外をスローするようになります。</p>
	 * <ul>
	 * <li>BMSフォーマットとして不正な構文が使用されている時</li>
	 * <li>未知のメタ情報(ヘッダ)が定義されている時</li>
	 * <li>メタ情報の値が想定する形式の値になっていない時(例えば数値を設定する箇所に数値以外を記述する等)</li>
	 * <li>同じ単体・索引付きメタ情報が再定義された時</li>
	 * <li>未知のチャンネル(番号)が定義されている時</li>
	 * <li>チャンネルに設定した値の形式が不正な時</li>
	 * <li>乱数(#RANDOM/#IF/#ELSE/#ENDIF等)の定義階層が不正な時</li>
	 * </ul>
	 * <p>上記以外にもファイルの読み込み中にエラーが発生する等、複数の要因で例外がスローされる可能性があります。
	 * 例外およびエラーの詳細については{@link BmsException}、{@link BmsError}を参照してください。</p>
	 * <p>詳細な読み込み設定を行ったうえでBMSコンテンツを様々なデータ型から読み込みたい場合には、
	 * {@link BmsLoader}と{@link BeMusicLoadHandler}を参照し、必要な手続きを行う処理を記述してください。
	 * 当メソッド内部では一般的な用途向けで手続きを行っています。</p>
	 * @param path 読み込み対象のBMSファイルのパス
	 * @param randomValue 乱数を固定化する場合の値、またはnull
	 * @param strictly 厳格なフォーマットチェックを行うかどうか
	 * @return 最新バージョンのBe-Music用BMS仕様で読み込まれたBMSコンテンツ
	 * @throws NullPointerException pathがnull
	 * @throws NoSuchFileException 指定されたパスのファイルが存在しない
	 * @throws IOException 何らかの理由によりデータ読み取り異常が発生した
	 * @throws BmsLoadException BMSコンテンツの読み込みが中止された
	 * @throws BmsPanicException 内部処理異常による処理の強制停止が発生した
	 * @see BmsStandardLoader
	 * @see BeMusicBmsonLoader
	 * @see BmsError
	 * @see BeMusicLoadHandler
	 */
	public static BmsContent loadContentFrom(Path path, Long randomValue, boolean strictly) throws IOException {
		assertArgNotNull(path, "path");
		var handler = new BeMusicLoadHandler()
				.setEnableControlFlow(true)
				.setForceRandomValue(randomValue);
		var loader = createLoaderFor(path)
				.setSpec(BeMusicSpec.LATEST)
				.setHandler(handler)
				.setStrictly(strictly);
		return loader.load(path);
	}
}
