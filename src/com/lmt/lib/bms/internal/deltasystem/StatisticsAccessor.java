package com.lmt.lib.bms.internal.deltasystem;

import com.lmt.lib.bms.bemusic.BeMusicRatingType;

/**
 * 譜面統計情報へのアクセスを行うためのインターフェイス
 *
 * <p>譜面統計情報は、対外的にはImmutable(変更不可能)なオブジェクトであるがDelta Systemは譜面統計情報とは
 * 別パッケージで実装されているためそのままでは値を設定することができない。Delta Systemへはコンテキスト情報として
 * 当インターフェイスが渡されるため、このインターフェイスを通じて分析結果を譜面統計情報に適用することとなる。</p>
 */
public interface StatisticsAccessor {
	/**
	 * レーティング値を譜面統計情報へ設定する
	 * @param ratingType レーティング種別
	 * @param rating レーティング値
	 */
	void setRating(BeMusicRatingType ratingType, int rating);

	// TODO 期間統計情報に、その地点まででの各レーティング値を設定するI/Fを追加する
}
