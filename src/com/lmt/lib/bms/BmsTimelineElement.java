package com.lmt.lib.bms;

/**
 * タイムラインを構成する要素を表す抽象クラスです。
 *
 * <p>当クラスは以下に記載した種類の要素の共通的な機能を提供します。</p>
 *
 * <p><strong>小節線</strong><br>
 * 小節の先頭に必ず存在するラインです。この要素自体はタイムラインに重要な変化をもたらすものではありませんが、
 * タイムラインの要素を列挙した際にタイムライン上に存在する要素として通知されます。アプリケーションはこの情報を用いて
 * 要素の分類を行ったり、エンドユーザーへの小節線のプレゼンテーションを行ったりすることができます。</p>
 *
 * <p><strong>小節データ</strong><br>
 * 小節に対して付加された情報です。小節データは必ず小節の先頭(小節の刻み位置が最小の位置)に配置されます。
 * 値型({@link BmsChannel#isArrayType()}がfalseを返す全てのチャンネルのデータ型)のデータは全て小節データとして扱われます。</p>
 *
 * <p><strong>ノート</strong><br>
 * 任意の楽曲位置に配置される情報です。配列型({@link BmsChannel#isArrayType()}がtrueを返す全てのチャンネルのデータ型)の
 * データが対象となります。</p>
 *
 * @since 0.8.0
 */
public abstract class BmsTimelineElement extends BmsAddress {
	/**
	 * 新しいタイムライン要素オブジェクトを構築します。
	 */
	protected BmsTimelineElement() {
		super();
	}

	/**
	 * 指定されたアドレスと同じ新しいタイムライン要素オブジェクトを構築します。
	 * @param adr アドレス
	 * @throws NullPointerException adrがnull
	 */
	protected BmsTimelineElement(BmsAddress adr) {
		super(adr);
	}

	/**
	 * 指定された楽曲位置、CHXの新しいタイムライン要素オブジェクトを構築します。
	 * @param at 楽曲位置
	 * @param chx CHX
	 * @throws NullPointerException atまたはchxがnull
	 */
	protected BmsTimelineElement(BmsAt at, BmsChx chx) {
		super(at, chx);
	}

	/**
	 * 指定された楽曲位置、CHXの新しいアドレスオブジェクトを構築します。
	 * @param measure 小節番号
	 * @param tick 小節の刻み位置
	 * @param channel チャンネル番号
	 * @param index チャンネルインデックス
	 */
	protected BmsTimelineElement(int measure, double tick, int channel, int index) {
		super(measure, tick, channel, index);
	}

	/**
	 * タイムライン要素の値をlong型に変換した値を取得します。
	 * @return タイムライン要素の値をlong型に変換した値
	 * @throws ClassCastException long型への変換が非対応のタイムライン要素で当メソッドを実行した
	 */
	public long getValueAsLong() {
		throw unsupport("long");
	}

	/**
	 * タイムライン要素の値をdouble型に変換した値を取得します。
	 * @return タイムライン要素の値をdouble型に変換した値
	 * @throws ClassCastException double型への変換が非対応のタイムライン要素で当メソッドを実行した
	 */
	public double getValueAsDouble() {
		throw unsupport("double");
	}

	/**
	 * タイムライン要素の値をString型に変換した値を取得します。
	 * @return タイムライン要素の値をString型に変換した値
	 * @throws ClassCastException String型への変換が非対応のタイムライン要素で当メソッドを実行した
	 */
	public String getValueAsString() {
		throw unsupport("string");
	}

	/**
	 * タイムライン要素の値をBMS配列型に変換した値を取得します。
	 * @return タイムライン要素の値をBMS配列型に変換した値
	 * @throws ClassCastException BMS配列型への変換が非対応のタイムライン要素で当メソッドを実行した
	 */
	public BmsArray getValueAsArray() {
		throw unsupport("array");
	}

	/**
	 * タイムライン要素の値をObject型に変換した値を取得します。
	 * @return タイムライン要素の値をObject型に変換した値
	 * @throws ClassCastException Object型への変換が非対応のタイムライン要素で当メソッドを実行した
	 */
	public Object getValueAsObject() {
		throw unsupport("object");
	}

	/**
	 * タイムライン要素が小節線であるかどうかを判定します。
	 * <p>当メソッドがtrueを返す時、チャンネル番号は{@link BmsSpec#CHANNEL_MEASURE}を示します。</p>
	 * @return タイムライン要素が小節線の場合true
	 */
	public boolean isMeasureLineElement() {
		return false;
	}

	/**
	 * タイムライン要素が小節データであるかどうかを判定します。
	 * @return タイムライン要素が小節データの場合true
	 */
	public boolean isMeasureValueElement() {
		return false;
	}

	/**
	 *タイムライン要素がノートであるかどうかを判定します。
	 * @return タイムライン要素がノートの場合true
	 */
	public boolean isNoteElement() {
		return false;
	}

	/**
	 * 指定型へのキャストに対応していない時にスローする例外生成
	 * @param typeName 型名
	 * @return ClassCastExceptionオブジェクト
	 */
	private ClassCastException unsupport(String typeName) {
		var msg = String.format("In '%s' class, '%s' type cast is NOT support.", getClass().getSimpleName(), typeName);
		return new ClassCastException(msg);
	}
}
