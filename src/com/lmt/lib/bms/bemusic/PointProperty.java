package com.lmt.lib.bms.bemusic;

import com.lmt.lib.bms.BmsSpec;

/**
 * 楽曲位置情報のプロパティクラス。
 *
 * <p>このクラスには、楽曲位置情報のうち一般的には頻繁に変更されることが少ない情報が含まれる。
 * {@link BeMusicChart}を構成する複数の{@link BeMusicPoint}内で同じ値を持つプロパティを共有させることで、
 * メモリ使用量を緩和させることを目的としている。</p>
 */
class PointProperty {
	/** デフォルト値(初期値の参照用として使用する) */
	static final PointProperty DEFAULT = new PointProperty();

	/** この楽曲位置が含まれる小節の小節長 */
	double length;
	/** 明示的なスクロール速度変更の有無 */
	boolean changeScroll;
	/** この楽曲位置の現在のスクロール速度 */
	double scroll;
	/** この楽曲位置の現在BPM */
	double bpm;
	/** 譜面停止時間 */
	double stop;
	/** 表示テキスト */
	String text;

	/**
	 * コンストラクタ
	 */
	PointProperty() {
		this.length = 1.0;
		this.changeScroll = false;
		this.scroll = 1.0;
		this.bpm = BmsSpec.BPM_DEFAULT;
		this.stop = 0.0;
		this.text = "";
	}

	/**
	 * コンストラクタ
	 * @param src コピー元プロパティ
	 */
	PointProperty(PointProperty src) {
		this.length = src.length;
		this.changeScroll = src.changeScroll;
		this.scroll = src.scroll;
		this.bpm = src.bpm;
		this.stop = src.stop;
		this.text = src.text;
	}
}
