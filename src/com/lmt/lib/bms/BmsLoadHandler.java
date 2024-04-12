package com.lmt.lib.bms;

/**
 * BMSローダーの読み込み処理を制御するハンドラです。
 *
 * <p>{@link BmsLoader}によるBMSの読み込み処理をアプリケーションが制御するためのインターフェイスを提供します。
 * 通常、BMSの読み込み処理は指定されたBMS仕様({@link BmsSpec})に基づいて行われますが、アプリケーション固有の事情により
 * 更に細かくローダーの処理を制御したい場合などに本インターフェイスを介して振る舞いを変更することができます。</p>
 *
 * <p>BMSの読み込み処理はデフォルトの動作が定義されており、本インターフェイスにはその動作が実装されています。個々のアプリケーションは
 * 自身の実現したい仕様に基づき、必要な振る舞いのみを変更してください。変更可能な振る舞いについては各メソッドのドキュメントを
 * 変更してください。</p>
 *
 * <p><b>例外のポリシーについて</b><br>
 * 当インターフェイスが提供するメソッド内で発生した例外は{@link BmsLoader#load}内でキャッチされ、{@link BmsException}に
 * 内包されて呼び出し元へスローします。本来、当インターフェイスが提供するメソッドでは例外がスローされることは想定していません。</p>
 */
public interface BmsLoadHandler extends BmsContent.Creator, BmsNote.Creator {
	/**
	 * BMS宣言、メタ情報、チャンネルデータの検査結果を示す列挙です。<br>
	 * 当列挙型は以下のメソッドの戻り値として使用します。
	 * <ul>
	 * <li>BMS宣言の検査：{@link #testDeclaration}</li>
	 * <li>メタ情報の検査：{@link #testMeta}</li>
	 * <li>チャンネルデータの検査：{@link #testChannel}</li>
	 * </ul>
	 */
	public enum TestResult {
		/**
		 * 検査に合格したことを示します。<br>
		 * この値を返すと、当該要素はBMSコンテンツの一部としてオブジェクトに取り込まれます。
		 */
		OK,

		/**
		 * 検査に失敗したことを示します。<br>
		 * この値を返すと当該要素は破棄され、{@link #parseError}が呼ばれます。
		 */
		FAIL,

		/**
		 * 検査した要素を読み飛ばすことを示します。<br>
		 * この値を返すと当該要素は破棄され、BMSコンテンツには取り込まれません。
		 */
		THROUGH,
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
	 * {@link BmsLoader#load}にてBMSの読み込み処理が開始された時に一度だけ呼び出されます。
	 *
	 * <p>当メソッドは1個のBMSの読み込みが開始される度に、アプリケーション固有の何らかの初期化処理を行いたい場合を想定して
	 * 用意されています。そのような事情が無い場合は何も行う必要はありません。</p>
	 *
	 * <p>当メソッドで例外がスローすると、後の処理で呼ばれる{@link #finishLoad}は呼ばれず、スローされた例外を内包した
	 * {@link BmsException}がスローされます。</p>
	 *
	 * @param spec BMSの読み込みに参照するBMS仕様(NOT null保証)
	 */
	default void startLoad(BmsSpec spec) {
		return;
	}

	/**
	 * {@link BmsLoader#load}にてBMSの読み込み処理が完了した時に一度だけ呼び出されます。
	 *
	 * <p>アプリケーションは、当メソッドのパラメータで渡されるBMSコンテンツを参照することで、独自の観点でBMSコンテンツの読み込み結果
	 * についての検査を行うことができます。読み込まれたBMSコンテンツが当該アプリケーションの仕様にそぐわない内容であると判定し、
	 * エラーとして結果を破棄したい場合は当メソッドの戻り値をfalseで返してください。そうすると{@link BmsLoader#load}は
	 * {@link BmsAbortException}をスローします。エラー種別は{@link BmsLoadError.Kind#FAILED_TEST_CONTENT}
	 * として報告されます。</p>
	 *
	 * <p>BmsLoadHandlerのメソッド内で例外をスローした場合、当メソッドは呼び出されず、スローされた例外を内包した
	 * {@link BmsException}がスローされます。</p>
	 *
	 * <p>当メソッドで例外をスローした場合、当該例外を内包した{@link BmsException}がスローされます。</p>
	 *
	 * @param content BMS読み込みが完了し、完成されたBMSコンテンツオブジェクト
	 * @return trueを返すとBMS読み込み処理を正常終了、falseを返すと異常終了と判定します。
	 */
	default boolean finishLoad(BmsContent content) {
		return true;
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
}
