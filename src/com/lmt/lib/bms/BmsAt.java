package com.lmt.lib.bms;

/**
 * BMSにおける時間軸を表すインターフェイスです。
 *
 * <p>BMSにおける時間軸は「小節番号(Measure)」と「小節の刻み位置(Tick)」で成り立っています。
 * BmsAtは、BMSライブラリにおけるオブジェクトが示す時間軸を参照する機能を提供します。</p>
 *
 * <p>小節番号は{@link BmsSpec#MEASURE_MIN}～{@link BmsSpec#MEASURE_MAX}の範囲、
 * 小節の刻み位置は{@link BmsSpec#TICK_MIN}～{@link BmsSpec#TICK_MAX}の範囲の値を返します。但し、小節の刻み位置の範囲は
 * 小節番号が示す小節の長さに依存しますので、現実的な最大値は{@link BmsSpec#TICK_MAX}よりも遥かに小さい値になります。
 * 多くのBMSコンテンツでは、小節の刻み位置の最大値は{@link BmsSpec#TICK_COUNT_DEFAULT} - 1を示すことが多いです。</p>
 */
public interface BmsAt {
	/**
	 * 小節番号を取得します。
	 * <p>このメソッドで取得できる小節番号は、BMS仕様が定める範囲の小節番号であることを保証します。
	 * その範囲は{@link BmsSpec#MEASURE_MIN}～{@link BmsSpec#MEASURE_MAX}として定義されています。
	 * @return 小節番号
	 */
	int getMeasure();

	/**
	 * 小節の刻み位置を取得します。
	 * <p>このメソッドで取得できる刻み位置は、BMSライブラリで取り扱い可能な範囲の刻み位置であることを保証します。
	 * その範囲は{@link BmsSpec#TICK_MIN}～{@link BmsSpec#TICK_MAX}として定義されています。</p>
	 * <p>小節単位での刻み位置の最大値は、小節長の定義内容によって大幅に増減します。通常4/4拍子では刻み数は
	 * 192となり、取り扱い可能範囲を大幅に下回る値になります。</p>
	 * @return 小節の刻み位置
	 */
	double getTick();
}
