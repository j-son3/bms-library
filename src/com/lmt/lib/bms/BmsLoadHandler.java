package com.lmt.lib.bms;

/**
 * BMSコンテンツ読み込み処理を制御するためのハンドラです。
 *
 * <p>{@link BmsLoader}によるBMSコンテンツの読み込み処理をアプリケーションが制御するためのインターフェイスを提供します。
 * 通常、BMSコンテンツの読み込み処理は指定されたBMS仕様({@link BmsSpec})に基づいて行われますが、アプリケーション固有の事情により
 * 更に細かくローダーの処理を制御したい場合などに本インターフェイスを介して振る舞いを変更することができます。</p>
 *
 * <p>BMSコンテンツの読み込み処理はデフォルトの動作が定義されており、本インターフェイスにはその動作が実装されています。
 * 個々のアプリケーションは自身の実現したい仕様に基づき、必要な振る舞いのみを変更してください。
 * 変更可能な振る舞いについては各メソッドのドキュメントを参照してください。</p>
 *
 * <p><strong>例外のポリシーについて</strong><br>
 * 当インターフェイスが提供するメソッド内で発生した例外は{@link BmsLoader#load}内でキャッチされ、{@link BmsException}に
 * 内包されて呼び出し元へスローします。本来、当インターフェイスが提供するメソッドでは例外がスローされることは想定していません。</p>
 */
public interface BmsLoadHandler extends BmsContent.Creator, BmsNote.Creator {
	/**
	 * BMS宣言、メタ情報、チャンネルデータ、およびBMSコンテンツの検査結果を示す列挙です。<br>
	 * 当列挙型は以下のメソッドの戻り値として使用します。
	 * <ul>
	 * <li>BMS宣言の検査：{@link BmsLoadHandler#testDeclaration(String, String)}</li>
	 * <li>メタ情報の検査：{@link BmsLoadHandler#testMeta(BmsMeta, int, Object)}</li>
	 * <li>チャンネルデータの検査：{@link BmsLoadHandler#testChannel(BmsChannel, int, int, Object)}</li>
	 * <li>BMSコンテンツの検査：{@link BmsLoadHandler#testContent(BmsContent)}</li>
	 * </ul>
	 */
	public static class TestResult {
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
		public static final TestResult OK = new TestResult(RESULT_OK, null);

		/**
		 * 検査に失敗したことを示します。<br>
		 * この値を返すと当該要素は破棄され、{@link #parseError}が呼ばれます。
		 */
		public static final TestResult FAIL = new TestResult(RESULT_FAIL, null);

		/**
		 * 検査した要素を読み飛ばすことを示します。<br>
		 * この値を返すと当該要素は破棄され、BMSコンテンツには取り込まれません。
		 */
		public static final TestResult THROUGH = new TestResult(RESULT_THROUGH, null);

		/** 検査結果 */
		private int mResult;
		/** メッセージ */
		private String mMessage;

		/**
		 * コンストラクタ
		 * @param result 検査結果
		 * @param message メッセージ
		 */
		private TestResult(int result, String message) {
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
		public static TestResult fail(String message) {
			return new TestResult(RESULT_FAIL, message);
		}
	}

	/**
	 * BMSコンテンツオブジェクトを生成します。
	 *
	 * <p>{@link BmsLoader}を通して指定されたソースから解析された各種情報は当メソッドが返したBMSコンテンツオブジェクトに格納されます。
	 * BMSの読み込みが正常に完了した場合、{@link BmsLoader#load}からは当メソッドから返されたオブジェクトの参照を返却します。</p>
	 *
	 * <p>BMSコンテンツに指定するBMS仕様は、パラメータで渡されるオブジェクトを渡してください。例えBMS仕様が全く同じであったとしても、
	 * パラメータで渡されたオブジェクト以外を渡したBMSコンテンツを返した場合、{@link BmsLoader#load}の以後の処理でエラーとなり
	 * 読み込み失敗となります。</p>
	 *
	 * @param spec BMS仕様
	 * @return BMSコンテンツオブジェクト
	 */
	@Override
	default BmsContent createContent(BmsSpec spec) {
		return new BmsContent(spec);
	}

	/**
	 * ノートオブジェクトを生成します。
	 *
	 * <p>配列型チャンネルのデータ要素は全てノートオブジェクトとしてBMSコンテンツ内で管理されます。当メソッドで返されたノートオブジェクトは
	 * BMSコンテンツ内に格納されることになります。アプリケーションが個々のノートオブジェクトに何らかの情報を付加してBMSコンテンツ内で管理させたい場合、
	 * 当メソッドで{@link BmsNote}を継承したノートオブジェクトを返します。</p>
	 *
	 * <p>付加情報の更新は{@link BmsNote#onCreate()}を使用して行ってください。
	 * これらのメソッドは{@link BmsContent}内部から呼び出されるよう設計されています。</p>
	 *
	 * <p>原則として、当メソッドが呼び出される度に新しいBmsNoteオブジェクトインスタンスを返却しなければなりません。
	 * 生成済みのBmsNoteオブジェクトの参照を返却するとオブジェクト内部のデータが上書きされてしまい、当該オブジェクトを持つBMSコンテンツの
	 * データ不整合が発生してしまいます。</p>
	 *
	 * <p>当メソッドで例外をスローした場合、当該例外を内包した{@link BmsException}がスローされます。</p>
	 *
	 * @return ノートオブジェクト
	 */
	@Override
	default BmsNote createNote() {
		return new BmsNote();
	}

	/**
	 * {@link BmsLoader}の読み込み処理にて読み込みが開始された時に一度だけ呼び出されます。
	 *
	 * <p>当メソッドは1個のBMSの読み込みが開始される度に、アプリケーション固有の何らかの初期化処理を行いたい場合を想定して
	 * 用意されています。そのような事情が無い場合は何も行う必要はありません。</p>
	 *
	 * <p>当メソッドで例外をスローすると、後の処理で呼ばれる検査メソッドは一切呼ばれず、スローされた例外を内包した
	 * {@link BmsException}がスローされます。</p>
	 *
	 * @param settings BMSローダーの設定
	 */
	default void startLoad(BmsLoaderSettings settings) {
		return;
	}

	/**
	 * BMS読み込み中にソース解析エラーが発生した場合に呼び出されます。
	 *
	 * <p>アプリケーションは当メソッドのパラメータを参照することでエラー情報にアクセスすることができます。また、エラーを無視して解析を
	 * 続行するかどうかをメソッドの戻り値で制御することができます。但し、解析エラーはBMSライブラリ、および(もしかしたら)アプリケーションが
	 * BMSコンテンツをBMS仕様に従って適正に読み込めないと判断した時に発生させるものであるため、基本的にはエラーを無視することは
	 * あまり推奨されません。</p>
	 *
	 * <p>発生するエラーの種類については{@link BmsLoadError.Kind}を参照してください。</p>
	 *
	 * <p>当メソッドでfalseを返し、エラーを無視しない場合は{@link BmsLoader#load}は{@link BmsAbortException}例外を
	 * スローし、生成中のBMSコンテンツは破棄します。デフォルトの動作では解析エラーは無視しません。</p>
	 *
	 * <p>当メソッドで例外をスローした場合、当該例外を内包した{@link BmsException}がスローされます。</p>
	 *
	 * @param error エラー情報
	 * @return falseを返すと、読み込み続行不可としてBMS読み込みを停止します。
	 * @see BmsLoadError.Kind
	 */
	default boolean parseError(BmsLoadError error) {
		return false;
	}

	/**
	 * 1個のBMS宣言を解析した時に呼び出されます。
	 *
	 * <p>通常、BMS宣言はBMS仕様に関わらずどのようなキーと値でも受け入れますが、アプリケーションとして個々のBMS宣言の内容を
	 * 検査したい場合は当メソッドを用います。検査結果は戻り値でBMSローダに報告します。</p>
	 *
	 * <p>デフォルトの動作では、全てのBMS宣言を受け入れます。</p>
	 *
	 * <p>当メソッドで例外をスローした場合、当該例外を内包した{@link BmsException}がスローされます。</p>
	 *
	 * @param key キー(NOT null保証)
	 * @param value 値(NOT null保証)
	 * @return {@link TestResult}参照
	 * @see TestResult
	 */
	default TestResult testDeclaration(String key, String value) {
		return TestResult.OK;
	}

	/**
	 * 1個のメタ情報を解析した時に呼び出されます。
	 *
	 * <p>当メソッドが呼び出される契機は、BMS仕様に定義のあるメタ情報の解析が正常に行われた後です。デフォルトの動作では、
	 * BMS仕様に準拠したメタ情報は全てBMSコンテンツ内に記録されます。アプリケーションが個々のメタ情報に対して固有の検査を行う必要が
	 * ある場合は当メソッドで検査処理を実装してください。</p>
	 *
	 * <p>解析されたメタ情報の検査結果は戻り値でBMSローダに報告します。</p>
	 *
	 * <p>BMS仕様に無いメタ情報を検出しても当メソッドは呼び出されず、直接{@link #parseError}が呼び出されます。</p>
	 *
	 * <p>当メソッドで例外をスローした場合、当該例外を内包した{@link BmsException}がスローされます。</p>
	 *
	 * @param meta メタ情報
	 * @param index インデックス
	 * @param value 解析後の値。データ型は当該メタ情報のデータ型に依存します。
	 * @return {@link TestResult}参照
	 * @see BmsMeta
	 * @see BmsType
	 * @see TestResult
	 */
	default TestResult testMeta(BmsMeta meta, int index, Object value) {
		return TestResult.OK;
	}

	/**
	 * 1個のチャンネルデータを解析した時に呼び出されます。
	 *
	 * <p>当メソッドが呼び出される契機は、BMS仕様に定義のあるチャンネルが正常に解析された後です。デフォルトの動作では
	 * BMS仕様に準拠したチャンネルデータは全てBMSコンテンツ内に記録されます。アプリケーションが個々のチャンネルデータに対して
	 * 固有の検査を行う必要がある場合は当メソッドで検査処理を実装してください。</p>
	 *
	 * <p>解析されたチャンネルデータの検査結果は戻り値でBMSローダに報告します。</p>
	 *
	 * <p>BMS仕様に無いチャンネルを検出しても当メソッドは呼び出されず、直接{@link #parseError}が呼び出されます。</p>
	 *
	 * <p>当メソッドで例外をスローした場合、当該例外を内包した{@link BmsException}がスローされます。</p>
	 *
	 * @param channel チャンネル
	 * @param index チャンネルインデックス
	 * @param measure 小節番号
	 * @param value チャンネルの値。データ型は当該チャンネルのデータ型に依存します。
	 * @return {@link TestResult}参照
	 * @see BmsChannel
	 * @see BmsType
	 * @see TestResult
	 */
	default TestResult testChannel(BmsChannel channel, int index, int measure, Object value) {
		return TestResult.OK;
	}

	/**
	 * BMSコンテンツの読み込みが完了した時に呼び出されます。
	 * <p>{@link BmsLoader}のBMSコンテンツ読み込みメソッドで指定した入力ソースからのBMSコンテンツ読み込みの全工程が完了した後、
	 * 最後に当メソッドが呼び出されます。当メソッドの目的は読み込まれたBMSコンテンツの最終チェックを行うことにあります。
	 * 例えば、BMSコンテンツ内に特定のメタ情報やチャンネルが定義されているかをチェックしたり、読み込み工程における状態の最終値が
	 * 期待する値であるかをチェックしたりなどです。</p>
	 * <p>読み込みの全工程が完了する前に検査失敗や何らかの読み込みエラーが発生し、読み込み処理が停止した場合には
	 * 当メソッドは実行されません。1回の読み込み処理毎に必ず当メソッドが実行されるわけではないことに注意してください。</p>
	 * <p>当メソッドの引数で渡されるBMSコンテンツは編集しても構いません。ただし、当メソッドを抜ける前に必ず
	 * {@link BmsContent#endEdit()}を実行して編集モードを完了させてください。編集モードのまま処理を終了すると
	 * BMSコンテンツの読み込み処理失敗と判定されてしまいます。</p>
	 * <p>当メソッドから返す検査結果に{@link TestResult#RESULT_FAIL}を設定するとBMSコンテンツ読み込みは失敗します。
	 * それ以外の検査結果は全て読み込み成功と判定されます。</p>
	 * <p>当メソッドで例外をスローした場合、当該例外を内包した{@link BmsException}がスローされます。</p>
	 * <p>当メソッドの既定の動作では{@link TestResult#OK}を返します。</p>
	 * @param content 読み込みの完了したBMSコンテンツ
	 * @return 検査結果
	 */
	default TestResult testContent(BmsContent content) {
		return TestResult.OK;
	}
}
