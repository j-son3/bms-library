package com.lmt.lib.bms;

import static com.lmt.lib.bms.BmsAssertion.*;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.function.Function;

/**
 * BMSで取り扱う0(00)～255(FF)の16進整数値、または0(00)～1295(ZZ)の36進整数値の配列を表現するクラスです。
 *
 * <p>BmsArrayでは、オブジェクト生成時に配列を表現した文字列、およびその文字列が表す基数を入力し、文字列の解析結果を
 * 整数の配列データに変換します。その後は配列に対して変更を加えることは出来ません。</p>
 *
 * <p>また、配列データを外部データ出力されたBMS上での文字列に変換する機能も有しています。以下に変換例を示します。<br>
 * 16進数の場合：&quot;004A007800FFC9&quot;<br>
 * 36進数の場合：&quot;GH00ZAZ60000AFPIZZ&quot;</p>
 */
public final class BmsArray extends AbstractList<Integer> {
	/** 16進数用文字列配列から整数配列への変換関数 */
	private static final Function<String, int[]> FN_S16TOA = s -> BmsInt.to16ia(s, 0, s.length() / 2);
	/** 36進数用文字列配列から整数配列への変換関数 */
	private static final Function<String, int[]> FN_S36TOA = s -> BmsInt.to36ia(s, 0, s.length() / 2);
	/** 16進数用整数配列から文字列配列への変換関数 */
	private static final Function<int[], String> FN_ATOS16 = a -> BmsInt.to16sa(a, 0, a.length);
	/** 36進数用整数配列から文字列配列への変換関数 */
	private static final Function<int[], String> FN_ATOS36 = a -> BmsInt.to36sa(a, 0, a.length);

	/** 基数 */
	private int mRadix;
	/** 文字列配列から整数配列への変換関数 */
	private Function<String, int[]> mFnStoA;
	/** 整数配列から文字列配列への変換関数 */
	private Function<int[], String> mFnAtoS;
	/** 整数配列 */
	private int[] mArray;

	/**
	 * 指定された基数の空の配列を生成します。
	 * @param radix 入力元データの基数(16,36のいずれかのみサポートしています)
	 * @exception IllegalArgumentException radixが16,36以外
	 */
	public BmsArray(int radix) {
		setup(radix, "");
	}

	/**
	 * 指定された基数で入力元データを解析し、配列を生成します。
	 * <p>基数に16を指定した場合、認識可能な文字は0～9,a～f,A～F、36の場合は0～9,a～z,A～Zとなり、
	 * いずれの場合も2文字1セットの整数値となります。従って、1つの要素で表現可能な値の範囲は
	 * 基数16で0～255、基数36で0～1295です。</p>
	 * <p>入力元データの空白文字など、認識不可能な文字をトリミングする機能はありません。
	 * それらの文字は予め除去したうえでこのコンストラクタを呼び出してください。</p>
	 * @param src 入力元データ
	 * @param radix 入力元データの基数(16,36のみサポートしています)
	 * @exception NullPointerException srcがnull
	 * @exception IllegalArgumentException srcの文字数が2で割り切れない
	 * @exception IllegalArgumentException srcに解析不可能な文字が含まれる
	 * @exception IllegalArgumentException radixが16,36以外
	 */
	public BmsArray(String src, int radix) {
		setup(radix, src);
	}

	/**
	 * 指定された配列と同等の配列を生成します。
	 * <p>新しく生成された配列オブジェクトは、入力元配列と同じ基数、要素数、データ内容を返すようになります。</p>
	 * @param array 入力元配列
	 * @exception NullPointerException arrayがnull
	 */
	public BmsArray(BmsArray array) {
		assertArgNotNull(array, "array");
		mRadix = array.mRadix;
		mFnStoA = array.mFnStoA;
		mFnAtoS = array.mFnAtoS;
		mArray = array.mArray;  // 一度生成されると変更されないので参照のみ渡す
	}

	/**
	 * BmsArrayオブジェクトをセットアップする
	 * @param radix 基数
	 * @param src 変換元文字列
	 * @exception NullPointerException srcがnullの場合
	 * @exception IllegalArgumentException radixが16,36以外、srcの文字数が2で割り切れない、またはsrcに解析不可能な文字が含まれる場合
	 */
	private void setup(int radix, String src) {
		assertArgNotNull(src, "src");
		assertArg((src.length() % 2) == 0, "Argument 'src' length is wrong. src='%s'", src);
		if (radix == 16) {
			mFnStoA = FN_S16TOA;
			mFnAtoS = FN_ATOS16;
		} else if (radix == 36) {
			mFnStoA = FN_S36TOA;
			mFnAtoS = FN_ATOS36;
		} else {
			var msg = String.format("Unsupported radix. radix=%d", radix);
			throw new IllegalArgumentException(msg);
		}
		mRadix = radix;
		mArray = mFnStoA.apply(src);
	}

	/**
	 * 配列の内容を文字列に変換します。
	 * <p>文字列の内容は、配列生成時に指定された入力元データ、および基数に従って変換されますが、
	 * 入力元データの内容に関わらずアルファベットは全て大文字で表現されます。</p>
	 * @return 文字列に変換された配列
	 */
	@Override
	public String toString() {
		return mFnAtoS.apply(mArray);
	}

	/**
	 * 配列の内容が一致しているかを判定します。
	 * <p>配列の基数、および内容が完全に一致している場合のみ「一致」と判定します。ただし、入力元文字列の
	 * アルファベット文字の大小は判定に影響しません。</p>
	 * @param obj 比較対象配列
	 * @return 配列の内容が一致している場合はtrue
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof BmsArray) {
			var o = (BmsArray)obj;
			return (mRadix == o.mRadix) && Arrays.equals(mArray, o.mArray);
		} else {
			return false;
		}
	}

	/** @see AbstractList#get */
	@Override
	public Integer get(int index) {
		return mArray[index];
	}

	/**
	 * 整数値配列から指定された位置の数値を取得します。
	 * <p>{@link #get(int)}とは異なり、プリミティブ型として値を取得します。</p>
	 * @param index インデックス
	 * @return 指定された位置の数値
	 * @exception IndexOutOfBoundsException インデックスが0未満または配列の要素数以上
	 */
	public final int getValue(int index) {
		return mArray[index];
	}

	/** @see AbstractList#size */
	@Override
	public int size() {
		return mArray.length;
	}

	/**
	 * 入力元データの基数を返します。
	 * @return 基数
	 */
	public int getRadix() {
		return mRadix;
	}
}
