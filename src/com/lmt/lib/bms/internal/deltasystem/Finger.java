package com.lmt.lib.bms.internal.deltasystem;

import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 指の特性を表す列挙型
 *
 * <p>Delta Systemにおいて、手指に関する物理的な特性をシミュレーションするに際し、当該指に対応する要素の数値に
 * 容易にアクセスするために用いる。例えば、指の力の強さを列挙型から受け取り、その値を評価点の計算に組み込むことで
 * 指の強さを考慮した評価を行うことができる。</p>
 */
enum Finger {
	/** 左親指 */
	LTHUMB(0, 0.9, Hand.LEFT),

	/** 左人差し指 */
	LINDEX(1, 1.1, Hand.LEFT),

	/** 左中指 */
	LMIDDLE(2, 1.0, Hand.LEFT),

	/** 左薬指 */
	LRING(3, 1.0, Hand.LEFT),

	/** 左小指 */
	LLITTLE(4, 0.7, Hand.LEFT),

	/** 右親指 */
	RTHUMB(5, 0.9, Hand.RIGHT),

	/** 右人差し指 */
	RINDEX(6, 1.1, Hand.RIGHT),

	/** 右中指 */
	RMIDDLE(7, 1.0, Hand.RIGHT),

	/** 右薬指 */
	RRING(8, 1.0, Hand.RIGHT),

	/** 右小指 */
	RLITTLE(9, 0.7, Hand.RIGHT);

	/** 指の総数 */
	public static final int COUNT = 10;

	/** インデックス値による指のテーブル */
	private static final Finger[] TABLE_INDEX = {
			LTHUMB, LINDEX, LMIDDLE, LRING, LLITTLE,
			RTHUMB, RINDEX, RMIDDLE, RRING, RLITTLE,
	};

	/** インデックス */
	private int mIndex;
	/** 指の強さ */
	private double mStrength;
	/** 指の付いた手 */
	private Hand mHand;

	/**
	 * コンストラクタ
	 * @param index インデックス
	 * @param strength 指の強さ
	 * @param hand 指の付いた手
	 */
	private Finger(int index, double strength, Hand hand) {
		mIndex = index;
		mStrength = strength;
		mHand = hand;
	}

	/**
	 * インデックス取得
	 * @return インデックス
	 */
	final int getIndex() {
		return mIndex;
	}

	/**
	 * 指の強さ取得
	 * @return 指の強さ
	 */
	final double getStrength() {
		return mStrength;
	}

	/**
	 * 指の付いた手取得
	 * @return 指の付いた手
	 */
	final Hand getHand() {
		return mHand;
	}

	/**
	 * インデックスから指を取得
	 * @param index インデックス
	 * @return インデックスに対応した指
	 */
	static Finger fromIndex(int index) {
		return TABLE_INDEX[index];
	}

	/**
	 * 指の文字列表現取得
	 * @return 指の文字列表現
	 */
	@Override
	public String toString() {
		return toString(0);
	}

	/**
	 * 指の文字列表現取得
	 * @param indent インデント数
	 * @return 指の文字列表現
	 */
	String toString(int indent) {
		var ind1 = String.join("", Stream.generate(() -> "  ").limit(indent).collect(Collectors.toList()));
		return String.format("%s%s: { index:%d, strength:%s }", ind1, name(), mIndex, mStrength);
	}

	/**
	 * 全指の情報をデバッグ出力
	 */
	static void print() {
		Ds.debug("fingers: {");
		Stream.of(TABLE_INDEX).forEach(f -> Ds.debug("%s", f.toString(1)));
		Ds.debug("}");
	}
}
