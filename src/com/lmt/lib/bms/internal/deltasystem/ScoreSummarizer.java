package com.lmt.lib.bms.internal.deltasystem;

import static com.lmt.lib.bms.internal.Assertion.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.TreeSet;
import java.util.function.ToDoubleFunction;
import java.util.stream.IntStream;

/**
 * 評価点のサマリクラス
 *
 * <p>評価点の最小値、最大値を基に評価点をグループ化し、グループごとの重み係数とグループに投入された評価点の数から
 * 最終的なサマリされた評価点を出力するプログラム。</p>
 */
class ScoreSummarizer {
	/** グループの総数 */
	private static final int GROUP_COUNT = 20;

	/** 評価点の飽和値 */
	private double mSatulate;

	/** 投入された評価点の数 */
	private int mScoreCount = 0;
	/** 投入された評価点の最小値 */
	private double mScoreMin = Double.MAX_VALUE;
	/** 投入された評価点の最大値 */
	private double mScoreMax = Double.MIN_VALUE;
	/** 評価点リスト */
	private TreeSet<Item> mScores = new TreeSet<>();

	/** 評価点データ */
	private static class Item implements Comparable<Item> {
		/** 時間 */
		double time;
		/** 評価点 */
		double score;
		/** この時間時点でのサマリ値 */
		double summary;

		/** {@inheritDoc} */
		@Override
		public int compareTo(Item o) {
			return Double.compare(time, o.time);
		}

		/**
		 * グループインデックス取得
		 * @param satulate 評価点の飽和値
		 * @param groupCount グループ数
		 * @return グループインデックス
		 */
		int groupIndex(double satulate, int groupCount) {
			var index = (int)((score / satulate) * groupCount);
			return Math.max(0, Math.min((groupCount - 1), index));
		}
	}

	/**
	 * コンストラクタ
	 * @param scoreMax 評価点の最大値
	 */
	ScoreSummarizer(double satulate) {
		mSatulate = satulate;
	}

	/**
	 * コンストラクタ
	 * @param <T> 要素データの型
	 * @param satulate 評価点の最大値
	 * @param elems 要素リスト
	 * @param scoreGetter 評価点取得関数
	 */
	<T extends RatingElement> ScoreSummarizer(double satulate, Collection<T> elems, ToDoubleFunction<T> scoreGetter) {
		this(satulate);
		put(elems, scoreGetter);
	}

	/**
	 * 評価点投入
	 * @param time この評価点の時間(前回投入した時間より大きい値にすること)
	 * @param score 評価点
	 * @exception IllegalArgumentException 時間が前回投入した時間の値以下
	 */
	final void put(double time, double score) {
		// サマリ値を計算する
		var thisScore = Math.max(0.0, Math.min(mSatulate, score));
		var thisSum = thisScore / mSatulate;
		if (!mScores.isEmpty()) {
			// 2件目以降の投入ではサマリ値の合計値を前回投入時の値から引き継いで加算する
			var last = mScores.last();
			assertArg(time > last.time, "Specified smaller time than previous. time=%f", time);
			thisSum += last.summary;
		}

		// 評価点データを生成する
		var item = new Item();
		item.time = time;
		item.score = thisScore;
		item.summary = thisSum;
		mScores.add(item);
		mScoreMin = Math.min(mScoreMin, thisScore);
		mScoreMax = Math.max(mScoreMax, thisScore);
		mScoreCount++;
	}

	/**
	 * 指定要素リストの全評価点を投入
	 * @param <T> 要素データの型
	 * @param elems 要素リスト
	 * @param scoreGetter 評価点取得関数
	 */
	final <T extends RatingElement> void put(Collection<T> elems, ToDoubleFunction<T> scoreGetter) {
		elems.stream().forEach(e -> put(e.getTime(), scoreGetter.applyAsDouble(e)));
	}

	/**
	 * サマリ実行
	 * @return サマリ結果
	 */
	final double summary() {
		return mScores.isEmpty() ? 0.0 : (mScores.last().summary / mScoreCount);
	}

	/**
	 * 現在の状況を文字列化
	 * <p>評価点の最小値＞各グループの評価点の数＞評価点の最大値　をタブ区切りで返す</p>
	 */
	@Override
	public String toString() {
		var groupCounts = new int[GROUP_COUNT];
		Arrays.fill(groupCounts, 0);
		mScores.forEach(i -> groupCounts[i.groupIndex(mSatulate, GROUP_COUNT)]++);

		var sb = new StringBuilder();
		sb.append(String.format("%.4f", mScoreMin));
		IntStream.of(groupCounts).forEach(n -> sb.append('\t').append(n));
		sb.append(String.format("\t%.4f", mScoreMax));
		return sb.toString();
	}
}
