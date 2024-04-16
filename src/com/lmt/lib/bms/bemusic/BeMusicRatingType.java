package com.lmt.lib.bms.bemusic;

import static com.lmt.lib.bms.internal.Assertion.*;

import java.util.Collections;
import java.util.List;
import java.util.function.IntFunction;
import java.util.function.IntToDoubleFunction;
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
	DELTA(
			0,
			BeMusicRatings.DELTA_MAX,
			r -> BeMusicRatings.deltaAsString(r, true),
			BeMusicRatings::deltaAsDouble,
			DeltaAnalyzer::new),
	/**
	 * 譜面の複雑さを表します。
	 * この要素は通称「横認識力」と呼ばれることがあります。
	 */
	COMPLEX(
			1,
			BeMusicRatings.TENDENCY_MAX,
			r -> BeMusicRatings.tendencyAsString(r, true),
			BeMusicRatings::tendencyAsDouble,
			ComplexAnalyzer::new),
	/**
	 * 譜面を正確に演奏するために求められる操作の速さ、および手指にかかる負荷を表します。
	 * この要素は通称「地力」と呼ばれることがあります。
	 */
	POWER(
			2,
			BeMusicRatings.TENDENCY_MAX,
			r -> BeMusicRatings.tendencyAsString(r, true),
			BeMusicRatings::tendencyAsDouble,
			PowerAnalyzer::new),
	/**
	 * 譜面のリズムの難しさを表します。
	 * この要素が高い譜面は通称「縦認識力」と呼ばれる能力を多く求められる傾向にあります。
	 */
	RHYTHM(
			3,
			BeMusicRatings.TENDENCY_MAX,
			r -> BeMusicRatings.tendencyAsString(r, true),
			BeMusicRatings::tendencyAsDouble,
			RhythmAnalyzer::new),
	/**
	 * 入力デバイスのうち「スクラッチ」を正確に操作することの難しさを表します。
	 */
	SCRATCH(
			4,
			BeMusicRatings.TENDENCY_MAX,
			r -> BeMusicRatings.tendencyAsString(r, true),
			BeMusicRatings::tendencyAsDouble,
			ScratchAnalyzer::new),
	/**
	 * 長押しノートを正確に操作することの難しさを表します。
	 */
	HOLDING(
			5,
			BeMusicRatings.TENDENCY_MAX,
			r -> BeMusicRatings.tendencyAsString(r, true),
			BeMusicRatings::tendencyAsDouble,
			HoldingAnalyzer::new),
	/**
	 * 譜面の特殊な視覚効果に正確に対応することの難しさを表します。
	 * この要素に含まれる具体的な視覚効果には「BPM変更」「スクロール速度変更」「譜面停止」「地雷」があります。
	 */
	GIMMICK(
			6,
			BeMusicRatings.TENDENCY_MAX,
			r -> BeMusicRatings.tendencyAsString(r, true),
			BeMusicRatings::tendencyAsDouble,
			GimmickAnalyzer::new);

	/** レーティング種別の数 */
	public static final int COUNT = 7;

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
	/** 文字列変換用関数 */
	private IntFunction<String> mToString;
	/** 数値変換用関数 */
	private IntToDoubleFunction mToValue;
	/** レーティング値分析処理のインスタンス生成関数 */
	private Supplier<? extends RatingAnalyzer> mCreator;

	/**
	 * コンストラクタ
	 * @param index インデックス
	 * @param max 最大レーティング値
	 * @param fnToString 文字列変換用関数
	 * @param fnToValue 数値変換用関数
	 * @param creator レーティング値分析処理のインスタンス生成関数
	 */
	private BeMusicRatingType(int index, int max, IntFunction<String> fnToString, IntToDoubleFunction fnToValue,
			Supplier<? extends RatingAnalyzer> creator) {
		mIndex = index;
		mMax = max;
		mToString = fnToString;
		mToValue = fnToValue;
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
	 * @param rating 変換対象のレーティング値
	 * @return double型に変換されたレーティング値
	 * @see BeMusicRatings#tendencyAsDouble(int)
	 * @see BeMusicRatings#deltaAsDouble(int)
	 */
	public final double toValue(int rating) {
		return mToValue.applyAsDouble(rating);
	}

	/**
	 * レーティング値をユーザープレゼンテーションするにあたり推奨する形式の文字列に変換します。
	 * @param rating レーティング値
	 * @return ユーザープレゼンテーションに推奨される形式の文字列
	 * @see BeMusicRatings#tendencyAsString(int, boolean)
	 * @see BeMusicRatings#deltaAsString(int, boolean)
	 */
	public final String toString(int rating) {
		return mToString.apply(rating);
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
}
