package com.lmt.lib.bms.internal.deltasystem;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.lmt.lib.bms.bemusic.BeMusicDevice;
import com.lmt.lib.bms.bemusic.BeMusicNoteType;
import com.lmt.lib.bms.bemusic.BeMusicPoint;

/**
 * レーティング要素データクラス
 */
abstract class RatingElement {
	/** 楽曲位置間の最大時間差分 */
	protected static final double MAX_TIME_DELTA = 1.5;

	// TODO Elementで入力デバイスリストを外部から入力できるようにリファクタリングする
	protected static final List<BeMusicDevice> DEVS = List.of(
			BeMusicDevice.SCRATCH1, BeMusicDevice.SWITCH11, BeMusicDevice.SWITCH12, BeMusicDevice.SWITCH13,
			BeMusicDevice.SWITCH14, BeMusicDevice.SWITCH15, BeMusicDevice.SWITCH16, BeMusicDevice.SWITCH17);

	/** このレーティング要素に対応する楽曲位置情報 */
	private BeMusicPoint mPoint;
	/** 1つ前の楽曲位置との時間差分 */
	private double mTimeDelta = 0.0;
	/** ノート種別(ノートレイアウト適用済み) */
	private byte[] mNoteTypes = new byte[BeMusicDevice.COUNT];

	/**
	 * コンストラクタ
	 * @param point 楽曲位置情報
	 */
	protected RatingElement(BeMusicPoint point) {
		mPoint = point;
	}

	/**
	 * 楽曲位置情報取得
	 * @return 楽曲位置情報
	 */
	final BeMusicPoint getPoint() {
		return mPoint;
	}

	/**
	 * 小節番号取得
	 * @return 小節番号
	 */
	final int getMeasure() {
		return mPoint.getMeasure();
	}

	/**
	 * 楽曲位置の時間取得
	 * @return 楽曲位置の時間
	 */
	final double getTime() {
		return mPoint.getTime();
	}

	/**
	 * 前の楽曲位置からの時間差分取得
	 * @return 前の楽曲位置からの時間差分
	 */
	final double getTimeDelta() {
		return mTimeDelta;
	}

	/**
	 * 前の楽曲位置からの時間差分設定
	 * @param timeDelta 前の楽曲位置からの時間差分
	 */
	final void setTimeDelta(double timeDelta) {
		mTimeDelta = (float)timeDelta;
	}

	/**
	 * ノート種別取得
	 * @param device 入力デバイス
	 * @return ノート種別
	 */
	final BeMusicNoteType getNoteType(BeMusicDevice device) {
		return BeMusicNoteType.fromId(mNoteTypes[device.getIndex()]);
	}

	/**
	 * ノート種別設定
	 * @param device 入力デバイス
	 * @param ntype ノート種別
	 */
	final void setNoteType(BeMusicDevice device, BeMusicNoteType ntype) {
		mNoteTypes[device.getIndex()] = (byte)ntype.getId();
	}

	/**
	 * レーティング値分析に必要な楽曲位置を抽出し、その楽曲位置情報を包括したレーティング要素データのリストを生成する
	 * <p>当メソッドでは、フィルタリング関数によって抽出された必要な楽曲位置情報のみに絞り込み、その楽曲位置情報を
	 * 包括するレーティング要素データを、指定のコンストラクタによって生成する。</p>
	 * <p>生成されたレーティング要素データには、1つ前の楽曲位置との時間差分とノートレイアウトを適用した状態のノート種別を
	 * 設定して返される。返したレーティング要素リストはレーティング値分析に使用されることを想定している。</p>
	 * @param <T> レーティング要素データの型
	 * @param cxt Delta System用のコンテキスト
	 * @param constructor レーティング要素データを生成するコンストラクタ
	 * @param filter 楽曲位置情報の絞り込みを行うフィルタリング関数
	 * @return レーティング要素リスト
	 */
	static <T extends RatingElement> List<T> listElements(DsContext cxt, Function<BeMusicPoint, T> constructor,
			Predicate<BeMusicPoint> filter) {
		// 条件に合致する楽曲位置のみでレーティング要素のリストを構築する
		var elems = cxt.chart.points().filter(filter).map(constructor).collect(Collectors.toList());
		var countElem = elems.size();

		// レーティング要素に必要情報を設定する
		var layout = cxt.layout;
		var countDev = BeMusicDevice.COUNT;
		for (var i = 0; i < countElem; i++) {
			// 1つ前の楽曲位置との時間差分を設定する
			var elem = elems.get(i);
			var timeDelta = (i == 0) ? 0.0 : Math.min(MAX_TIME_DELTA, elem.getTime() - elems.get(i - 1).getTime());
			elem.setTimeDelta(timeDelta);

			// ノート配置レイアウトを適用したノート種別を設定する
			var point = elem.getPoint();
			for (var j = 0; j < countDev; j++) {
				var dev = BeMusicDevice.fromIndex(j);
				var ntype = point.getVisibleNoteType(layout.get(dev));
				elem.setNoteType(dev, ntype);
			}
		}

		return elems;
	}

	/**
	 * 演奏時間取得
	 * <p>当メソッドで言うところの「演奏時間」とは、最後の楽曲位置と最初の楽曲位置の時間差分を指す。</p>
	 * @param elems レーティング要素リスト
	 * @return 演奏時間
	 */
	static double computeTimeOfPointRange(List<? extends RatingElement> elems) {
		return elems.isEmpty() ? 0.0 : (elems.get(elems.size() - 1).getTime() - elems.get(0).getTime());
	}

	static double timeDelta(List<? extends RatingElement> elems, int from, int to) {
		return elems.get(to).getTime() - elems.get(from).getTime();
	}

	static <T extends RatingElement> double timeDelta(T from, T to) {
		return to.getTime() - from.getTime();
	}

	/**
	 * レーティング要素リストのデバッグ出力
	 * <p>当メソッドは、デバッグモードが{@link DsDebugMode#DETAIL}の時に出力されるべき譜面の詳細情報を
	 * 出力するために用いることを想定している。</p>
	 * @param elements レーティング要素リスト
	 */
	static void print(List<? extends RatingElement> elements) {
		if (elements.size() == 0) {
			// 要素がない場合は出力しない(できない)
			return;
		}

		// ヘッダ部を出力する
		elements.get(0).printHeader();

		// データ部を出力する
		// データは末尾から先頭に向かって出力(実際の譜面表示と同じような出力イメージにするため)
		var last = elements.size() - 1;
		var lastMeasure = elements.get(last).getPoint().getMeasure();
		for (var i = last; i >= 0; i--) {
			// 小節番号が変化した場合は小節線を出力する
			var elem = elements.get(i);
			var curMeasure = elem.getPoint().getMeasure();
			if (curMeasure != lastMeasure) {
				elem.printMeasure();
				lastMeasure = curMeasure;
			}

			// データを出力する
			elem.printData(i);
		}
	}

	/**
	 * レーティング要素データをデバッグ出力する
	 * <p>このレーティング要素データが持つ固有のデータを図式化、数値化し、整形して出力する。
	 * 出力内容は{@link #printHeader()}, {@link #printMeasure()}と整合性の取れた形式の出力を行う。</p>
	 * @param pos このレーティング要素のリストにおけるインデックス値
	 */
	protected abstract void printData(int pos);

	/**
	 * 小節線をデバッグ出力する
	 * <p>小節線は、小節番号の切り替わり毎に出力されるように調整されて呼び出される。
	 * {@link #printHeader()}と整合性の取れた形式での出力を行う。尚、出力する内容は小節番号と
	 * 小節線を表すテキストの記号とする。</p>
	 */
	protected abstract void printMeasure();

	/**
	 * 譜面情報のヘッダ部をデバッグ出力する
	 * <p>当メソッドは譜面情報のデバッグ出力時、最初に1回だけ呼び出される。
	 * {@link #printMeasure()}, {@link #printData()}で出力予定のレーティング要素データに合わせたヘッダを
	 * テキストで表現し、デバッグ出力する。</p>
	 */
	protected abstract void printHeader();

	/**
	 * ノート配置の内容を表した文字列を生成する
	 * <p>ノートレイアウトを適用したノート配置をテキストで表現した文字列を返す。</p>
	 * @return ノート配置の内容を表した文字列
	 */
	protected String makeNotesString() {
		return new StringBuilder()
				.append(getNoteTypeString(BeMusicDevice.SCRATCH1)).append(" ")
				.append(getNoteTypeString(BeMusicDevice.SWITCH11)).append(" ")
				.append(getNoteTypeString(BeMusicDevice.SWITCH12)).append(" ")
				.append(getNoteTypeString(BeMusicDevice.SWITCH13)).append(" ")
				.append(getNoteTypeString(BeMusicDevice.SWITCH14)).append(" ")
				.append(getNoteTypeString(BeMusicDevice.SWITCH15)).append(" ")
				.append(getNoteTypeString(BeMusicDevice.SWITCH16)).append(" ")
				.append(getNoteTypeString(BeMusicDevice.SWITCH17)).toString();
	}

	/**
	 * ノート種別に応じたノートの文字列表現を返す
	 * @param device 入力デバイス
	 * @return 入力デバイスに対応したノートのノート種別文字列表現
	 */
	protected String getNoteTypeString(BeMusicDevice device) {
		switch (getNoteType(device)) {
		case BEAT:
			return "====";
		case LONG_ON:
			return "+==+";
		case LONG_OFF:
			return "+  +";
		case CHARGE_OFF:
			return "+--+";
		case LONG:
			return "|  |";
		case MINE:
			return "****";
		default:
			return "    ";
		}
	}
}