package com.lmt.lib.bms.internal.deltasystem;

import com.lmt.lib.bms.bemusic.BeMusicHeader;
import com.lmt.lib.bms.bemusic.BeMusicNoteLayout;
import com.lmt.lib.bms.bemusic.BeMusicScore;

/**
 * Delta System用コンテキストクラス
 */
public class DsContext {
	/** 分析対象楽曲のヘッダ情報 */
	BeMusicHeader header;
	/** 分析対象楽曲の譜面データ */
	BeMusicScore score;
	/** ノートレイアウト */
	BeMusicNoteLayout layout;
	/** 統計情報へのアクセッサ */
	StatisticsAccessor stat;
	/** ダブルプレーの分析モードかどうか */
	public boolean dpMode; // TODO ダブルプレーに対応後、publicを削除する

	/**
	 * コンストラクタ
	 * @param header 分析対象楽曲のヘッダ情報
	 * @param score 分析対象楽曲の譜面データ
	 * @param layout ノートレイアウト
	 * @param stat 統計情報へのアクセッサ
	 */
	public DsContext(BeMusicHeader header, BeMusicScore score, BeMusicNoteLayout layout, StatisticsAccessor stat) {
		this.header = header;
		this.score = score;
		this.layout = layout;
		this.stat = stat;
		this.dpMode = header.getPlayer().isDoublePlay() && layout.isDoublePlayLayout();
	}
}
