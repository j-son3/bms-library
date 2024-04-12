package com.lmt.lib.bms;

import java.util.Comparator;

/**
 * BMSコンテンツの時間軸に配置されたノートの情報を表します。
 *
 * <p>情報には、時間軸上の位置・チャンネル・ノートが示す値が存在します。当クラスの情報は参照専用であり、
 * BMSコンテンツの外部から情報の変更が加えられることを想定していません。</p>
 *
 * @see BmsAt [BmsAt] 当クラスが実装する、時間軸を参照するインターフェイス
 */
public class BmsNote implements BmsAt {
	/** ノート比較用コンパレータ */
	static Comparator<BmsNote> COMPARATOR = new Comparator<>() {
		@Override
		public int compare(BmsNote o1, BmsNote o2) {
			var c1 = Integer.compare(o1.getMeasure(), o2.getMeasure());
			if (c1 != 0) { return c1; }
			var c2 = Double.compare(o1.getTick(), o2.getTick());
			if (c2 != 0) { return c2; }
			var c3 = Integer.compare(o1.getChannel(), o2.getChannel());
			if (c3 != 0) { return c3; }
			return Integer.compare(o1.getIndex(), o2.getIndex());
		}
	};

	/**
	 * ノートオブジェクトを生成するI/Fを提供します。
	 * <p>当インターフェイスは主に{@link BmsContent}のメソッドにパラメータとして渡されます。1個のノートオブジェクトの
	 * インスタンスを生成し、戻り値として返却する役割を担います。</p>
	 */
	@FunctionalInterface
	public interface Creator {
		/**
		 * ノートオブジェクトを生成します。
		 * <p>配列型チャンネルのデータ要素は全てノートオブジェクトとしてBMSコンテンツ内で管理されます。当メソッドで返されたノートオブジェクトは
		 * BMSコンテンツ内に格納されることになります。アプリケーションが個々のノートオブジェクトに何らかの情報を付加してBMSコンテンツ内で
		 * 管理させたい場合、当メソッドで{@link BmsNote}を継承したノートオブジェクトを返します。</p>
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
	/** チャンネル番号 */
	private short mChannel = 0;
	/** チャンネル内インデックス */
	private short mIndex = 0;
	/** 小節番号 */
	private short mMeasure = 0;
	/** 小節内の刻み位置 */
	private double mTick = 0;

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
		var chZ = Integer.toString(mChannel, 36);
		var valZ = Integer.toString(mValue, 36);
		return String.format("{Ch=%s(%d)[%d], M=%d, T=%d, V(d/h/z)=%d/%02X/%s}",
				chZ, mChannel, mIndex, mMeasure, mTick, mValue, mValue, valZ);
	}

	/**
	 * チャンネル番号を取得します。
	 * @return チャンネル番号
	 */
	public final int getChannel() {
		return mChannel;
	}

	/**
	 * チャンネル番号を設定する。
	 * @param channel チャンネル番号
	 */
	final void setChannel(int channel) {
		mChannel = (short)channel;
	}

	/**
	 * チャンネルインデックスを取得します。
	 * @return チャンネルインデックス
	 */
	public final int getIndex() {
		return mIndex;
	}

	/**
	 * チャンネルインデックスを設定する。
	 * @param index チャンネルインデックス
	 */
	final void setIndex(int index) {
		mIndex = (short)index;
	}

	/**
	 * @see BmsAt#getMeasure()
	 */
	@Override
	public final int getMeasure() {
		return mMeasure;
	}

	/**
	 * 小節番号を設定する。
	 * @param measure 小節番号
	 */
	final void setMeasure(int measure) {
		mMeasure = (short)measure;
	}

	/**
	 * @see BmsAt#getTick()
	 */
	@Override
	public final double getTick() {
		return mTick;
	}

	/**
	 * 小節の刻み位置を設定する。
	 * @param tick 小節の刻み位置
	 */
	final void setTick(double tick) {
		mTick = tick;
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
		mChannel = (short)channel;
		mIndex = (short)index;
		mMeasure = (short)measure;
		mTick = tick;
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
		cloned.setup(mChannel, mIndex, mMeasure + shift, mTick, mValue);
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
		cloned.setup(channel, index, mMeasure, mTick, mValue);
		return cloned;
	}
}
