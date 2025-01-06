package com.lmt.lib.bms.internal.deltasystem;

import java.util.Collections;
import java.util.List;

import com.lmt.lib.bms.bemusic.BeMusicChart;
import com.lmt.lib.bms.bemusic.BeMusicDevice;
import com.lmt.lib.bms.bemusic.BeMusicHeader;
import com.lmt.lib.bms.bemusic.BeMusicLane;
import com.lmt.lib.bms.bemusic.BeMusicNoteLayout;

/**
 * Delta System用コンテキストクラス
 */
public class DsContext {
	/** 入力デバイステーブル(SP/DP左) */
	private static final List<BeMusicDevice> DEVICES_PRIMARY = Collections.unmodifiableList(List.of(
			BeMusicDevice.SCRATCH1, BeMusicDevice.SWITCH11, BeMusicDevice.SWITCH12, BeMusicDevice.SWITCH13,
			BeMusicDevice.SWITCH14, BeMusicDevice.SWITCH15, BeMusicDevice.SWITCH16, BeMusicDevice.SWITCH17
	));
	/** 入力デバイステーブル(DP右) */
	private static final List<BeMusicDevice> DEVICES_SECONDARY = Collections.unmodifiableList(List.of(
			BeMusicDevice.SCRATCH2, BeMusicDevice.SWITCH27, BeMusicDevice.SWITCH26, BeMusicDevice.SWITCH25,
			BeMusicDevice.SWITCH24, BeMusicDevice.SWITCH23, BeMusicDevice.SWITCH22, BeMusicDevice.SWITCH21
	));
	/** レーンごとの入力デバイステーブル */
	private static final List<List<BeMusicDevice>> DEVICES = Collections.unmodifiableList(List.of(
			DEVICES_PRIMARY,
			DEVICES_SECONDARY
	));

	/** 分析対象楽曲のヘッダ情報 */
	BeMusicHeader header;
	/** 分析対象楽曲の譜面データ */
	BeMusicChart chart;
	/** ノートレイアウト */
	BeMusicNoteLayout layout;
	/** 統計情報へのアクセッサ */
	StatisticsAccessor stat;
	/** ダブルプレーの分析モードかどうか */
	boolean dpMode;
	/** プレーモードに応じた入力デバイス一覧(実際の並び順) */
	BeMusicDevice[] devices;

	/**
	 * コンストラクタ
	 * @param header 分析対象楽曲のヘッダ情報
	 * @param chart 分析対象楽曲の譜面データ
	 * @param layout ノートレイアウト
	 * @param stat 統計情報へのアクセッサ
	 */
	public DsContext(BeMusicHeader header, BeMusicChart chart, BeMusicNoteLayout layout, StatisticsAccessor stat) {
		this.header = header;
		this.chart = chart;
		this.layout = layout;
		this.stat = stat;
		this.dpMode = header.getPlayer().isDoublePlay() && layout.isDoublePlayLayout();
		this.devices = (this.dpMode ? BeMusicDevice.orderedByDpList() : BeMusicDevice.orderedBySpLeftList()).stream()
				.toArray(BeMusicDevice[]::new);
	}

	/**
	 * Delta System評価用の並び替え済み入力デバイスリスト取得
	 * <p>当メソッドはDelta Systemの各レーティング値評価で必要になる並び順の入力デバイスリストを返す。</p>
	 * @param lane レーン
	 * @return 入力デバイスリスト
	 */
	static List<BeMusicDevice> orderedDevices(BeMusicLane lane) {
		return DEVICES.get(lane.getIndex());
	}
}
