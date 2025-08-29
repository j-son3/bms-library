package com.lmt.lib.bms.internal.deltasystem;

import java.util.List;
import java.util.stream.Collectors;

import com.lmt.lib.bms.bemusic.BeMusicDevice;
import com.lmt.lib.bms.bemusic.BeMusicLane;
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

	/** コンテキスト */
	private DsContext mCtx;
	/** このレーティング要素に対応する楽曲位置情報 */
	private BeMusicPoint mPoint;
	/** 1つ前の楽曲位置との時間差分 */
	private double mTimeDelta = 0.0;
	/** ノート種別(ノートレイアウト適用済み) */
	private byte[] mNoteTypes = new byte[BeMusicDevice.COUNT];

	/**
	 * コンストラクタ
	 * @param ctx コンテキスト
	 * @param point 楽曲位置情報
	 */
	protected RatingElement(DsContext ctx, BeMusicPoint point) {
		mCtx = ctx;
		mPoint = point;
	}

	/**
	 * 楽曲位置情報取得
	 * @return 楽曲位置情報
	 */
	BeMusicPoint getPoint() {
		return mPoint;
	}

	/**
	 * 小節番号取得
	 * @return 小節番号
	 */
	int getMeasure() {
		return mPoint.getMeasure();
	}

	/**
	 * 楽曲位置の時間取得
	 * @return 楽曲位置の時間
	 */
	double getTime() {
		return mPoint.getTime();
	}

	/**
	 * 前の楽曲位置からの時間差分取得
	 * @return 前の楽曲位置からの時間差分
	 */
	double getTimeDelta() {
		return mTimeDelta;
	}

	/**
	 * 前の楽曲位置からの時間差分設定
	 * @param timeDelta 前の楽曲位置からの時間差分
	 */
	void setTimeDelta(double timeDelta) {
		mTimeDelta = (float)timeDelta;
	}

	/**
	 * ノート種別取得
	 * @param device 入力デバイス
	 * @return ノート種別
	 */
	BeMusicNoteType getNoteType(BeMusicDevice device) {
		return BeMusicNoteType.fromId(mNoteTypes[device.getIndex()]);
	}

	/**
	 * ノート種別設定
	 * @param device 入力デバイス
	 * @param ntype ノート種別
	 */
	void setNoteType(BeMusicDevice device, BeMusicNoteType ntype) {
		mNoteTypes[device.getIndex()] = (byte)ntype.getId();
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
		var elemTop = elements.get(0);
		elemTop.printHeader();

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
	 * ノート配置の内容を表した文字列を生成する
	 * <p>ノートレイアウトを適用したノート配置をテキストで表現した文字列を返す。</p>
	 * @param lane レーン
	 * @return ノート配置の内容を表した文字列
	 */
	protected String makeNotesString(BeMusicLane lane) {
		var devices = BeMusicDevice.orderedByDpList(lane);
		return devices.stream().map(this::getNoteTypeString).collect(Collectors.joining(" "));
	}

	/**
	 * ノート種別に応じたノートの文字列表現を返す
	 * @param device 入力デバイス
	 * @return 入力デバイスに対応したノートのノート種別文字列表現
	 */
	protected String getNoteTypeString(BeMusicDevice device) {
		switch (getNoteType(device)) {
		case BEAT:
			return mCtx.dpMode ? "===" : "====";
		case LONG_ON:
			return mCtx.dpMode ? "+=+" : "+==+";
		case LONG_OFF:
			return mCtx.dpMode ? "+ +" : "+  +";
		case CHARGE_OFF:
			return mCtx.dpMode ? "+-+" : "+--+";
		case LONG:
			return mCtx.dpMode ? "| |" : "|  |";
		case MINE:
			return mCtx.dpMode ? "***" : "****";
		default:
			return mCtx.dpMode ? "   " : "    ";
		}
	}

	/**
	 * コンテキスト取得
	 * @return コンテキスト
	 */
	protected DsContext getContext() {
		return mCtx;
	}

	/**
	 * レーティング要素データをデバッグ出力する(SP)
	 * @param pos このレーティング要素のリストにおけるインデックス値
	 */
	protected abstract void printSpData(int pos);

	/**
	 * レーティング要素データをデバッグ出力する(DP)
	 * @param pos このレーティング要素のリストにおけるインデックス値
	 */
	protected abstract void printDpData(int pos);

	/**
	 * 小節線をデバッグ出力する(SP)
	 * @param m 小節番号
	 */
	protected abstract void printSpMeasure(int m);

	/**
	 * 小節線をデバッグ出力する(DP)
	 * @param m 小節番号
	 */
	protected abstract void printDpMeasure(int m);

	/** 譜面情報のヘッダ部をデバッグ出力する(SP) */
	protected abstract void printSpHeader();

	/** 譜面情報のヘッダ部をデバッグ出力する(DP) */
	protected abstract void printDpHeader();

	/**
	 * レーティング要素データをデバッグ出力する
	 * @param pos このレーティング要素のリストにおけるインデックス値
	 */
	private void printData(int pos) {
		if (mCtx.dpMode) { printDpData(pos); } else { printSpData(pos); }
	}

	/** 小節線をデバッグ出力する */
	private void printMeasure() {
		var m = getMeasure();
		if (mCtx.dpMode) { printDpMeasure(m); } else { printSpMeasure(m); }
	}

	/** 譜面情報のヘッダ部をデバッグ出力する */
	private void printHeader() {
		if (mCtx.dpMode) { printDpHeader(); } else { printSpHeader(); }
	}
}