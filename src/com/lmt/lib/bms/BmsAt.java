package com.lmt.lib.bms;

import java.util.Objects;

/**
 * タイムラインにおける時間軸の位置を表すインターフェイスです。
 *
 * <p>タイムラインにおける時間軸の位置は「小節番号(Measure)」と「小節の刻み位置(Tick)」で成り立っています。
 * この位置のことを「楽曲位置」と呼称します。{@link BmsAt}は、楽曲位置の要素を参照する機能を提供します。</p>
 *
 * <p>タイムラインの楽曲位置の有効範囲は、小節番号の場合{@link BmsSpec#MEASURE_MIN}～{@link BmsSpec#MEASURE_MAX}、
 * 刻み位置の場合{@link BmsSpec#TICK_MIN}～{@link BmsSpec#TICK_MAX}となります。ただし、刻み位置の範囲は小節番号が示す
 * 小節の長さに依存しますので、現実的な最大値は{@link BmsSpec#TICK_MAX}よりも遥かに小さい値になります。
 * 多くのBMSコンテンツでは、小節における刻み位置の最大値は概ね{@link BmsSpec#TICK_COUNT_DEFAULT}前後となります。</p>
 *
 * @since 0.0.1
 */
public interface BmsAt {
	/**
	 * 小節番号を取得します。
	 * @return 小節番号
	 */
	int getMeasure();

	/**
	 * 小節の刻み位置を取得します。
	 * @return 小節の刻み位置
	 */
	double getTick();

	/**
	 * 楽曲位置が小節線上にあるかどうかを判定します。
	 * <p>具体的に、小節線上とは{@link #getTick()}が返した刻み位置が{@link BmsSpec#TICK_MIN}と等しいことを表します。
	 * その位置から少しでもずれていた場合、小節線上とは見なされません。</p>
	 * @return 楽曲位置が小節線上にある場合true
	 */
	default boolean onMeasureLine() {
		return getTick() == BmsSpec.TICK_MIN;
	}

	/**
	 * 2つの楽曲位置を比較し、位置が同じかどうかを判定します。
	 * @param at1 楽曲位置1
	 * @param at2 楽曲位置2
	 * @return 2つの楽曲位置が同じであればtrue
	 * @exception NullPointerException at1またはat2がnull
	 */
	public static boolean equals(BmsAt at1, BmsAt at2) {
		return (at1.getMeasure() == at2.getMeasure()) && (at1.getTick() == at2.getTick());
	}

	/**
	 * 2つの楽曲位置を比較します。
	 * @param at1 楽曲位置1
	 * @param at2 楽曲位置2
	 * @return 楽曲位置1 == 楽曲位置2は0、楽曲位置1 &gt; 楽曲位置2は正の値、楽曲位置1 &lt; 楽曲位置2は負の値
	 * @exception NullPointerException at1またはat2がnull
	 */
	public static int compare(BmsAt at1, BmsAt at2) {
		var comp = Integer.compare(at1.getMeasure(), at2.getMeasure());
		comp = (comp == 0) ? Double.compare(at1.getTick(), at2.getTick()) : comp;
		return comp;
	}

	/**
	 * 2つの楽曲位置を比較します。
	 * @param measure1 楽曲位置1の小節番号
	 * @param tick1 楽曲位置1の小節の刻み位置
	 * @param measure2 楽曲位置2の小節番号
	 * @param tick2 楽曲位置2の小節の刻み位置
	 * @return 楽曲位置1 == 楽曲位置2は0、楽曲位置1 &gt; 楽曲位置2は正の値、楽曲位置1 &lt; 楽曲位置2は負の値
	 */
	public static int compare2(int measure1, double tick1, int measure2, double tick2) {
		var comp = Integer.compare(measure1, measure2);
		comp = (comp == 0) ? Double.compare(tick1, tick2) : comp;
		return comp;
	}

	/**
	 * 楽曲位置からハッシュコード値を生成します。
	 * @param at 楽曲位置
	 * @return ハッシュコード値
	 * @exception NullPointerException atがnull
	 */
	public static int hashCode(BmsAt at) {
		return Objects.hash(at.getMeasure(), at.getTick());
	}

	/**
	 * 楽曲位置の文字列表現を返します。
	 * @param at 楽曲位置
	 * @return 楽曲位置の文字列表現
	 * @exception NullPointerException atがnull
	 */
	public static String toString(BmsAt at) {
		return String.format("{M=%d, T=%.16g}", at.getMeasure(), at.getTick());
	}
}
