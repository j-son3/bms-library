package com.lmt.lib.bms;

/**
 * タイムライン要素の一つであるノートの情報を表します。
 *
 * <p>情報には、アドレス、およびノートが示す値の2つが存在します。当クラスの情報は変更不可であり、
 * BMSライブラリの外部から情報の変更が加えられることを想定していません。</p>
 *
 * @see BmsElement
 */
public class BmsNote extends BmsElement {
	/**
	 * ノートオブジェクトを生成するI/Fを提供します。
	 * <p>当インターフェイスは主に{@link BmsContent}のメソッドにパラメータとして渡されます。1個のノートオブジェクトの
	 * インスタンスを生成し、戻り値として返却する役割を担います。</p>
	 */
	@FunctionalInterface
	public interface Creator {
		/**
		 * ノートオブジェクトを生成します。
		 * <p>配列型チャンネルのデータ要素は全てノートオブジェクトとしてBMSコンテンツ内で管理されます。
		 * 当メソッドで返されたノートオブジェクトはBMSコンテンツ内のタイムライン要素として格納されることになります。
		 * アプリケーションが個々のノートオブジェクトに何らかの情報を付加してBMSコンテンツ内で管理させたい場合、
		 * 当メソッドで{@link BmsNote}を継承したノートオブジェクトを返します。</p>
		 * <p>付加情報の更新は{@link #onCreate()}を使用して行ってください。
		 * これらのメソッドは{@link BmsContent}内部から呼び出されるよう設計されています。</p>
		 * @return ノートオブジェクト
		 */
		BmsNote createNote();
	}

	/**
	 * ノートオブジェクトを検査するI/Fを提供します。
	 * <p>当インターフェイスは主に{@link BmsContent}のメソッドにパラメータとして渡されます。1個のノートオブジェクトの
	 * 検査を実施し、戻り値として検査結果を返却する役割を担います。</p>
	 */
	@FunctionalInterface
	public interface Tester {
		/**
		 * ノートオブジェクトの検査を行います。
		 * <p>パラメータで渡されるノートオブジェクトが検査対象です。オブジェクトの内容を確認し、検査OKとなる場合には
		 * 戻り値でtrueを返し、検査失敗の場合はfalseを返します。検査後、どのような振る舞いになるかは当インターフェイスを
		 * 扱うメソッドに依存します。</p>
		 * @param note 検査対象のノートオブジェクト
		 * @return 検査合格の場合はtrue、不合格の場合はfalse
		 */
		boolean testNote(BmsNote note);
	}

	/** デフォルトのノートオブジェクト生成器。 */
	public static final Creator DEFAULT_CREATOR = () -> new BmsNote();
	/** 常に検査合格とするテスター。 */
	public static final Tester TEST_OK = n -> true;
	/** 常に検査不合格とするテスター。 */
	public static final Tester TEST_FAIL = n -> false;

	/** ノートが持つ値 */
	private short mValue = 0;

	/**
	 * ノートオブジェクトを新しく構築します。
	 * <p>標準のノートオブジェクトでは、ノートの値・チャンネル番号・チャンネルインデックス・小節番号・小節内の刻み位置を
	 * 保有します。これらの情報は{@link BmsContent}からのみ設定されることを想定しており、BMSライブラリの外部から値を
	 * 更新するためのインターフェイスは用意されていません。ただし、クラスを拡張してノートオブジェクトに付加的な情報
	 * を挿入することは可能です。</p>
	 * <p>ノートオブジェクトを構築しただけでは{@link #onCreate}は呼び出されません。</p>
	 */
	public BmsNote() {
		// Do nothing
	}

	/**
	 * チャンネル番号、インデックス、小節番号、刻み位置、値が分かる形式の文字列を返します。
	 * @return チャンネル番号、インデックス、小節番号、刻み位置、値が分かる形式の文字列
	 */
	@Override
	public String toString() {
		return String.format("{Adr=%s, Value(d/h/z)=%d/%02X/%s}",
				super.toString(), mValue, mValue, BmsInt.to36s(mValue));
	}

	/**
	 * ノートに割り当てられた値を取得します。
	 * <p>ノートの値とは、配列型({@link BmsArray})チャンネルデータの1つの配列要素を表します。つまり、
	 * 16進配列では0～255、36進配列では0～1295の範囲の値を示します。</p>
	 * @return ノートの値
	 */
	public final int getValue() {
		return mValue;
	}

	/**
	 * ノートに割り当てられた値を設定する。
	 * @param value ノートの値
	 */
	final void setValue(int value) {
		mValue = (short)value;
	}

	/**
	 * ノートに割り当てられた値をlong型にキャストして取得します。
	 * <p>ノートに割り当てられた値を参照する際は可能であれば{@link #getValue()}を使用することを推奨します。</p>
	 * @return ノートに割り当てられた値
	 */
	@Override
	public long getValueAsLong() {
		return mValue;
	}

	/**
	 * ノートに割り当てられた値をdouble型にキャストして取得します。
	 * <p>ノートに割り当てられた値を参照する際は可能であれば{@link #getValue()}を使用することを推奨します。</p>
	 * @return ノートに割り当てられた値
	 */
	@Override
	public double getValueAsDouble() {
		return mValue;
	}

	/**
	 * ノートに割り当てられた値の文字列表現を取得します。
	 * @return ノートに割り当てられた値の文字列表現
	 */
	@Override
	public String getValueAsString() {
		return String.valueOf(mValue);
	}

	/**
	 * ノートに割り当てられた値をObject型にキャストして取得します。
	 * <p>返されるオブジェクトの実体はInteger型のノートに割り当てられた値です。ノートに割り当てられた値を参照する際は
	 * 可能であれば{@link #getValue()}を使用することを推奨します。</p>
	 * @return ノートに割り当てられた値
	 */
	@Override
	public Object getValueAsObject() {
		return (int)mValue;
	}

	/** {@inheritDoc} */
	@Override
	public boolean isNoteElement() {
		return true;
	}

	/**
	 * このオブジェクトと同等の新しいノートオブジェクトのインスタンスを生成します。
	 * <p>当メソッドはノートの移動・コピーが発生した時に内部処理が呼び出します。当クラスを継承したノートオブジェクトは
	 * 当メソッドをオーバーライドし、継承先クラスのインスタンスを生成するように実装するべきです。デフォルトの実装では
	 * {@link BmsNote}のインスタンスが返されるようになっています。継承先クラスが当メソッドを実装しない場合、
	 * ノートの移動・コピー時に継承先クラスが持つ付加情報が失われることになります。</p>
	 * @return 新しいインスタンスのノートオブジェクト
	 */
	protected BmsNote onNewInstance() {
		return new BmsNote();
	}

	/**
	 * BMSコンテンツにノートが登録される時、またはノートの移動・コピーなどが発生した時に呼び出されます。
	 * <p>当メソッドは、{@link BmsNote}のデフォルト実装では何も行われません。</p>
	 * <p>当メソッドは、ノートオブジェクト登録時に拡張データを含んだカスタムノートオブジェクト(BmsNoteを継承したクラス)
	 * を生成する目的で使用することを想定しています。ノートオブジェクト生成時、拡張データ生成のために一度だけ呼び出したい
	 * 処理がある場合に当メソッドをオーバーライドし、処理を記述してください。</p>
	 */
	protected void onCreate() {
		// Do nothing
	}

	/**
	 * ノートの正式なセットアップ処理
	 * <p>ノートに対して刻み位置と値を設定するために用いる。ユーザーによる無秩序な刻み位置と値の設定を
	 * 防ぐため、外部からはそれらを設定出来ないようにしている。</p>
	 * @param channel チャンネル番号
	 * @param index チャンネル内インデックス
	 * @param measure 小節番号
	 * @param tick 小節内の刻み位置
	 * @param value ノートが持つ値
	 */
	void setup(int channel, int index, int measure, double tick, int value) {
		setMeasure(measure);
		setTick(tick);
		setChx(channel, index);
		mValue = (short)value;
		onCreate();
	}

	/**
	 * 指定量だけ小節番号をずらした新しいノートオブジェクト取得
	 * @param shift 小節番号のシフト量
	 * @return 新しいノートオブジェクト
	 */
	BmsNote shiftMeasure(int shift) {
		var cloned = onNewInstance();
		cloned.setup(getChannel(), getIndex(), getMeasure() + shift, getTick(), mValue);
		return cloned;
	}

	/**
	 * チャンネルを変更した新しいノートオブジェクト取得
	 * @param channel チャンネル番号
	 * @param index チャンネルインデックス
	 * @return 新しいノートオブジェクト
	 */
	BmsNote changeChannel(int channel, int index) {
		var cloned = onNewInstance();
		cloned.setup(channel, index, getMeasure(), getTick(), mValue);
		return cloned;
	}
}
