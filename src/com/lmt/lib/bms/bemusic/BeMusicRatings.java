package com.lmt.lib.bms.bemusic;

/**
 * Delta Systemのレーティング値に関連する定数値と静的メソッドをまとめたクラスです。
 */
public class BeMusicRatings {
	/** {@link BeMusicRatingType#DELTA}のレーティング値の最小値 */
	public static final int DELTA_ZERO = 0;
	/** {@link BeMusicRatingType#DELTA}のレーティング値の最大値 */
	public static final int DELTA_MAX = 1600;
	/** 譜面傾向のレーティング値の最小値 */
	public static final int TENDENCY_MIN = 0;
	/** 譜面傾向のレーティング値の最大値 */
	public static final int TENDENCY_MAX = 20000;

	/** レーティング値が無効時の文字列表現 */
	private static final String STR_UNKNOWN = "UNKNOWN";
	/** {@link BeMusicRatingType#DELTA}が最小時の文字列表現 */
	private static final String STR_DELTA_ZERO = "ZERO";
	/** {@link BeMusicRatingType#DELTA}が最大時の文字列表現 */
	private static final String STR_DELTA_MAX = "MAX";

	/**
	 * 譜面傾向のレーティング値をdouble型に変換します。
	 * <p>Delta Systemが出力するレーティング値はユーザーにプレゼンテーションする想定値の100倍の値になります。
	 * 実際にプレゼンテーションする際は小数点第2位までを示すことを想定しているため、プログラム上で扱う値も
	 * 正確には浮動小数点型を使用する必要があります。</p>
	 * <p>当メソッドに有効範囲外の値({@link #TENDENCY_MIN}～{@link #TENDENCY_MAX})を指定すると、
	 * 有効範囲内に丸められた値のdouble型の値を返します。</p>
	 * @param rating 譜面傾向のレーティング値
	 * @return double型に変換された譜面傾向レーティング値
	 */
	public static double tendencyAsDouble(int rating) {
		rating = Math.max(TENDENCY_MIN, Math.min(TENDENCY_MAX, rating));
		return rating / 100.0;
	}

	/**
	 * 譜面傾向のレーティング値をユーザープレゼンテーションするにあたり推奨する形式の文字列に変換します。
	 * <p>moreStringifyにtrueを指定し、レーティング値が有効範囲外の場合は「UNKNOWN」を返します。
	 * それ以外の場合はレーティング値を有効範囲内に丸め、小数点第2位までを示した数値文字列を返します。</p>
	 * @param rating 譜面傾向のレーティング値
	 * @param moreStringify レーティング値が特定の状態を示す場合の特殊な文字列変換を行うかどうか
	 * @return ユーザープレゼンテーションに推奨される形式の譜面傾向の文字列
	 */
	public static String tendencyAsString(int rating, boolean moreStringify) {
		if (moreStringify && ((rating < TENDENCY_MIN) || (rating > TENDENCY_MAX))) {
			return STR_UNKNOWN;
		} else {
			return String.format("%.2f", tendencyAsDouble(rating));
		}
	}

	/**
	 * クリア難易度のレーティング値をdouble型に変換します。
	 * <p>Delta Systemが出力するレーティング値はユーザーにプレゼンテーションする想定値の100倍の値になります。
	 * 実際にプレゼンテーションする際は小数点第2位までを示すことを想定しているため、プログラム上で扱う値も
	 * 正確には浮動小数点型を使用する必要があります。</p>
	 * <p>当メソッドに有効範囲外の値({@link #DELTA_ZERO}～{@link #DELTA_MAX})を指定すると、
	 * 有効範囲内に丸められた値のdouble型の値を返します。</p>
	 * @param rating クリア難易度のレーティング値
	 * @return double型に変換されたクリア難易度レーティング値
	 */
	public static double deltaAsDouble(int rating) {
		rating = Math.max(DELTA_ZERO, Math.min(DELTA_MAX, rating));
		return rating / 100.0;
	}

	/**
	 * クリア難易度のレーティング値をユーザープレゼンテーションするにあたり推奨する形式の文字列に変換します。
	 * <p>moreStringifyにtrueを指定しレーティング値が特定の条件を満たす場合、以下の文字列を返します。</p>
	 * <ul>
	 * <li>レーティング値が{@link #DELTA_ZERO}未満の場合「UNKNOWN」を返します。</li>
	 * <li>レーティング値が{@link #DELTA_ZERO}と等しい場合「ZERO」を返します。</li>
	 * <li>レーティング値が{@link #DELTA_MAX}と等しい場合「MAX」を返します。</li>
	 * <li>レーティング値が{@link #DELTA_MAX}超過の場合「UNKNOWN」を返します。</li>
	 * </ul>
	 * <p>それ以外の場合はレーティング値を有効範囲内に丸め、小数点第2位までを示した数値文字列を返します。</p>
	 * @param rating クリア難易度のレーティング値
	 * @param moreStringify レーティング値が特定の状態を示す場合の特殊な文字列変換を行うかどうか
	 * @return ユーザープレゼンテーションに推奨される形式のクリア難易度の文字列
	 */
	public static String deltaAsString(int rating, boolean moreStringify) {
		if (moreStringify) {
			if ((rating < DELTA_ZERO) || (rating > DELTA_MAX)) {
				return STR_UNKNOWN;
			} else if (rating == DELTA_ZERO) {
				return STR_DELTA_ZERO;
			} else if (rating == DELTA_MAX) {
				return STR_DELTA_MAX;
			}
		}
		return String.format("%.2f", deltaAsDouble(rating));
	}
}
