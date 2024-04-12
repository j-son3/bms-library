package com.lmt.lib.bms.bemusic;

import static com.lmt.lib.bms.internal.Assertion.*;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.lmt.lib.bms.internal.deltasystem.ComplexAnalyzer;
import com.lmt.lib.bms.internal.deltasystem.DeltaAnalyzer;
import com.lmt.lib.bms.internal.deltasystem.GimmickAnalyzer;
import com.lmt.lib.bms.internal.deltasystem.HoldingAnalyzer;
import com.lmt.lib.bms.internal.deltasystem.PowerAnalyzer;
import com.lmt.lib.bms.internal.deltasystem.RatingAnalyzer;
import com.lmt.lib.bms.internal.deltasystem.RhythmAnalyzer;
import com.lmt.lib.bms.internal.deltasystem.ScratchAnalyzer;

/**
 * Delta Systemによる譜面のレーティング種別を表す列挙型です。
 *
 * <p>Delta Systemについては以下のリンクを参照してください。<br>
 * <a href="http://www.lm-t.com/content/bmslibrary/doc/deltasystem/">http://www.lm-t.com/content/bmslibrary/doc/deltasystem/</a></p>
 */
public enum BeMusicRatingType {
	/**
	 * 譜面を「クリア」する難易度を表します。
	 */
	DELTA(0, BeMusicRatings.DELTA_MAX, DeltaAnalyzer::new),
	/**
	 * 譜面の複雑さを表します。
	 * この要素は通称「横認識力」と呼ばれることがあります。
	 */
	COMPLEX(1, BeMusicRatings.TENDENCY_MAX, ComplexAnalyzer::new),
	/**
	 * 譜面を正確に演奏するために求められる操作の速さ、および手指にかかる負荷を表します。
	 * この要素は通称「地力」と呼ばれることがあります。
	 */
	POWER(2, BeMusicRatings.TENDENCY_MAX, PowerAnalyzer::new),
	/**
	 * 譜面のリズムの難しさを表します。
	 * この要素が高い譜面は通称「縦認識力」と呼ばれる能力を多く求められる傾向にあります。
	 */
	RHYTHM(3, BeMusicRatings.TENDENCY_MAX, RhythmAnalyzer::new),
	/**
	 * 入力デバイスのうち「スクラッチ」を正確に操作することの難しさを表します。
	 */
	SCRATCH(4, BeMusicRatings.TENDENCY_MAX, ScratchAnalyzer::new),
	/**
	 * 長押しノートを正確に操作することの難しさを表します。
	 */
	HOLDING(5, BeMusicRatings.TENDENCY_MAX, HoldingAnalyzer::new),
	/**
	 * 譜面の特殊な視覚効果に正確に対応することの難しさを表します。
	 * この要素に含まれる具体的な視覚効果には「BPM変更」「スクロール速度変更」「譜面停止」「地雷」があります。
	 */
	GIMMICK(6, BeMusicRatings.TENDENCY_MAX, GimmickAnalyzer::new);

	/** レーティング種別の数 */
	public static final int COUNT = 7;

	/** レーティング値が無効時の文字列表現 */
	private static final String STR_UNKNOWN = "UNKNOWN";
	/** {@link #DELTA}が最小時の文字列表現 */
	private static final String STR_DELTA_ZERO = "ZERO";
	/** {@link #DELTA}が最大時の文字列表現 */
	private static final String STR_DELTA_MAX = "MAX";

	/** 譜面傾向のレーティング種別リスト */
	private static final List<BeMusicRatingType> TENDENCIES = Collections.unmodifiableList(
			Stream.of(BeMusicRatingType.values())
			.filter(BeMusicRatingType::isTendency)
			.sorted((a, b) -> Integer.compare(a.getIndex(), b.getIndex()))
			.collect(Collectors.toList()));

	/** インデックス */
	private int mIndex;
	/** このレーティング種別の最大レーティング値 */
	private int mMax;
	/** レーティング値分析処理のインスタンス生成関数 */
	private Supplier<? extends RatingAnalyzer> mCreator;

	/**
	 * コンストラクタ
	 * @param index インデックス
	 * @param max 最大レーティング値
	 * @param creator レーティング値分析処理のインスタンス生成関数
	 */
	private BeMusicRatingType(int index, int max, Supplier<? extends RatingAnalyzer> creator) {
		mIndex = index;
		mMax = max;
		mCreator = creator;
	}

	/**
	 * このレーティング種別のインデックスを取得します。
	 * @return インデックス
	 */
	public final int getIndex() {
		return mIndex;
	}

	/**
	 * このレーティング種別の最大レーティング値取得
	 * @return 最大レーティング値
	 */
	public final int getMax() {
		return mMax;
	}

	/**
	 * このレーティング種別で指定レーティング値が有効範囲内かどうか判定します。
	 * <p>有効なレーティング値は0以上で、最大値は{@link #DELTA}とそれ以外で異なります。
	 * {@link #DELTA}の最大値は{@link BeMusicRatings#DELTA_MAX}、それ以外は{@link BeMusicRatings#TENDENCY_MAX}です。</p>
	 * @param rating レーティング値
	 * @return レーティング値が有効である場合true
	 */
	public final boolean isValid(int rating) {
		return (rating >= 0) && (rating <= getMax());
	}

	/**
	 * このレーティング種別で指定レーティング値が有効範囲外かどうか判定します。
	 * <p>{@link #isValid(int)}と逆の結果を返します。</p>
	 * @param rating レーティング値
	 * @return レーティング値が無効である場合true
	 * @see #isValid(int)
	 */
	public final boolean isUnknown(int rating) {
		return !isValid(rating);
	}

	/**
	 * 譜面傾向を表すレーティング種別であるかを判定します。
	 * <p>具体的には{@link #DELTA}以外であればtrueとなります。</p>
	 * @return 譜面傾向を表すレーティング種別であればtrue
	 */
	public final boolean isTendency() {
		return this != DELTA;
	}

	/**
	 * レーティング値をdouble型に変換します。
	 * <p>Delta Systemが出力するレーティング値はユーザーにプレゼンテーションする想定値の100倍の値になります。
	 * 実際にプレゼンテーションする際は小数点第2位までを示すことを想定しているため、プログラム上で扱う値も
	 * 正確には浮動小数点型を使用する必要があります。</p>
	 * <p>当メソッドに無効な値({@link #isUnknown(int)}がtrueを返す値)を指定すると有効範囲内に丸められた値で
	 * double型の値を返します。</p>
	 * @param rating 変換対象のレーティング値
	 * @return double型に変換されたレーティング値
	 */
	public final double toValue(int rating) {
		return (double)normalizeRating(rating) / 100.0;
	}

	/**
	 * レーティング値をユーザープレゼンテーションするにあたり推奨する形式の文字列に変換します。
	 * <p>プレゼンテーションする形式はレーティング種別により異なります。{@link #DELTA}では最小値を「ZERO」、
	 * 最大値を「MAX」と表記し、それ以外を小数点第2位までの数値で表現します。譜面傾向のレーティング種別では
	 * 一律小数点第2位までの数値で表現します。</p>
	 * <p>いずれの場合も無効な値を指定すると「UNKNOWN」が返されます。</p>
	 * @param rating レーティング値
	 * @return ユーザープレゼンテーションに推奨される形式の文字列
	 */
	public final String toString(int rating) {
		return isTendency() ? toTendencyString(rating) : toDeltaString(rating);
	}

	/**
	 * レーティング分析処理オブジェクト生成
	 * @return レーティング分析処理オブジェクト
	 */
	final RatingAnalyzer createAnalyzer() {
		return mCreator.get();
	}

	/**
	 * インデックス値に対応するレーティング種別を取得します。
	 * @param index インデックス
	 * @return レーティング種別
	 * @exception IndexOutOfBoundsException インデックスが有効範囲外
	 * @see #getIndex()
	 */
	public static BeMusicRatingType fromIndex(int index) {
		assertArgIndexRange(index, BeMusicRatingType.COUNT, "index");
		return values()[index];
	}

	/**
	 * 譜面傾向のレーティング種別のリストを取得します。
	 * <p>返されたリストのレーティング種別は全て{@link #isTendency()}がtrueになることを保証します。</p>
	 * <p>リストは読み取り専用です。リストを変更したい場合は返されたリストをコピーし、そちらを変更してください。</p>
	 * @return 譜面傾向のレーティング種別のリスト
	 */
	public static List<BeMusicRatingType> tendencies() {
		return TENDENCIES;
	}

	/**
	 * レーティング値を有効範囲内に丸め込む
	 * @param rating レーティング値
	 * @return 丸め込まれたレーティング値
	 */
	private int normalizeRating(int rating) {
		return Math.min(mMax, Math.max(0, rating));
	}

	/**
	 * {@link #DELTA}向け文字列化処理
	 * @param rating レーティング値
	 * @return 文字列化されたレーティング値
	 */
	private String toDeltaString(int rating) {
		if (isUnknown(rating)) {
			return STR_UNKNOWN;
		} else if (rating == BeMusicRatings.DELTA_ZERO) {
			return STR_DELTA_ZERO;
		} else if (rating == BeMusicRatings.DELTA_MAX) {
			return STR_DELTA_MAX;
		} else {
			return String.format("%.2f", (double)normalizeRating(rating) / 100.0);
		}
	}

	/**
	 * 譜面傾向のレーティング種別向け文字列化処理
	 * @param rating レーティング値
	 * @return 文字列化されたレーティング値
	 */
	private String toTendencyString(int rating) {
		if (isUnknown(rating)) {
			return STR_UNKNOWN;
		} else {
			return String.format("%.2f", (double)normalizeRating(rating) / 100.0);
		}
	}
}
