package com.lmt.lib.bms.parse;

import com.lmt.lib.bms.BmsChannel;
import com.lmt.lib.bms.BmsContent;
import com.lmt.lib.bms.BmsLoadHandler;
import com.lmt.lib.bms.BmsMeta;
import com.lmt.lib.bms.BmsScriptError;

/**
 * BMS宣言、メタ情報、タイムライン要素、およびBMSコンテンツの検査結果を示すクラスです。<br>
 * 当クラスは以下のメソッドの戻り値として使用します。
 * <ul>
 * <li>BMS宣言の検査：{@link BmsLoadHandler#testDeclaration(String, String)}</li>
 * <li>メタ情報の検査：{@link BmsLoadHandler#testMeta(BmsMeta, int, Object)}</li>
 * <li>タイムライン要素の検査：{@link BmsLoadHandler#testChannel(BmsChannel, int, int, Object)}</li>
 * <li>BMSコンテンツの検査：{@link BmsLoadHandler#testContent(BmsContent)}</li>
 * </ul>
 */
public class BmsTestResult {
	/** 検査に合格したことを示す値です。 */
	public static final int RESULT_OK = 0;
	/** 検査に失敗したことを示す値です。 */
	public static final int RESULT_FAIL = 1;
	/** 検査した要素を読み飛ばすことを示す値です。 */
	public static final int RESULT_THROUGH = 2;

	/**
	 * 検査に合格したことを示します。<br>
	 * この値を返すと、当該要素はBMSコンテンツの一部として取り込まれます。
	 */
	public static final BmsTestResult OK = new BmsTestResult(RESULT_OK, null);

	/**
	 * 検査に失敗したことを示します。<br>
	 * この値を返すと当該要素は破棄され、{@link BmsLoadHandler#parseError(BmsScriptError)}が呼ばれます。
	 */
	public static final BmsTestResult FAIL = new BmsTestResult(RESULT_FAIL, null);

	/**
	 * 検査した要素を読み飛ばすことを示します。<br>
	 * この値を返すと当該要素は破棄され、BMSコンテンツには取り込まれません。
	 */
	public static final BmsTestResult THROUGH = new BmsTestResult(RESULT_THROUGH, null);

	/** 検査結果 */
	private int mResult;
	/** メッセージ */
	private String mMessage;

	/**
	 * コンストラクタ
	 * @param result 検査結果
	 * @param message メッセージ
	 */
	private BmsTestResult(int result, String message) {
		mResult = result;
		mMessage = message;
	}

	/**
	 * 検査結果を取得します。
	 * <p>この値が示すのは {@link #RESULT_OK}, {@link #RESULT_FAIL}, {@link #RESULT_THROUGH} のいずれかです。</p>
	 * @return 検査結果の値
	 */
	public final int getResult() {
		return mResult;
	}

	/**
	 * メッセージを取得します。
	 * <p>検査結果が {@link #RESULT_FAIL} になった場合に読み込みハンドラが検査失敗理由等を通知したい場合に
	 * 用いることを想定しています。検査失敗以外の結果ではメッセージを参照する機会がないためnullとなります。</p>
	 * @return メッセージ
	 */
	public final String getMessage() {
		return mMessage;
	}

	/**
	 * メッセージ付きの検査失敗を生成します。
	 * <p>読み込みハンドラが検査失敗理由等を通知したい場合に当メソッドを用いてメッセージ付きの検査失敗を
	 * 生成することができます。標準の検査失敗オブジェクト({@link #FAIL})はメッセージを保有していません。</p>
	 * @param message 検査失敗に付加したいメッセージ
	 * @return 引数で指定したメッセージを持つ検査失敗オブジェクト
	 */
	public static BmsTestResult fail(String message) {
		return new BmsTestResult(RESULT_FAIL, message);
	}
}