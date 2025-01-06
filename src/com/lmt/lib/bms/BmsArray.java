package com.lmt.lib.bms;

import static com.lmt.lib.bms.internal.Assertion.*;

import java.util.AbstractList;
import java.util.Arrays;

/**
 * 0(00)～255(FF)の16進整数値、0(00)～1295(ZZ)の36進整数値、0(00)～3843(zz)の62進整数値の配列を表現するクラスです。
 *
 * <p>当クラスでは、オブジェクト生成時に配列を表現した文字列、およびその文字列が表す基数を入力し、
 * 文字列の解析結果を整数の配列データに変換します。その後は配列に対して変更を加えることはできません。</p>
 *
 * <p>また、配列データを外部データ出力されたBMS上での文字列に変換する機能も有しています。以下に変換例を示します。<br>
 * 16進数の場合：&quot;004A007800FFC9&quot;<br>
 * 36進数の場合：&quot;GH00ZAZ60000AFPIZZ&quot;<br>
 * 62進数の場合：&quot;A8006x00zC00QPyt&quot;</p>
 *
 * @since 0.0.1
 */
public final class BmsArray extends AbstractList<Integer> {
	/** 基数に応じた整数オブジェクト */
	private BmsInt mInt;
	/** 整数配列 */
	private int[] mArray;

	/**
	 * 指定された基数の空の配列を生成します。
	 * @param base 入力元データの基数(16, 36, 62のいずれかのみサポートしています)
	 * @exception IllegalArgumentException radixが16, 36, 62以外
	 */
	public BmsArray(int base) {
		setup(base, "");
	}

	/**
	 * 指定された基数で入力元データを解析し、配列を生成します。
	 * <p>基数に16を指定した場合、認識可能な文字は0～9,a～f,A～F、36, 62の場合は0～9,a～z,A～Zとなり、
	 * いずれの場合も2文字1セットの整数値となります。従って、1つの要素で表現可能な値の範囲は
	 * 基数16で0～255、基数36で0～1295、基数62で0～3843です。</p>
	 * <p>入力元データの空白文字など、認識不可能な文字をトリミングする機能はありません。
	 * それらの文字は予め除去したうえでこのコンストラクタを呼び出してください。</p>
	 * <p>入力元データの文字数が奇数の場合、最後の文字が"0"である場合に限り、最後の要素を"00"と見なして解析します。
	 * このケースは構文エラーに対する特別な救済措置であり、本来であればエラーとして処理されるべき内容です。
	 * 救済措置は通常では不要なデータ編集などの余分な処理が行われパフォーマンスが低下しますので、
	 * 構文誤りのデータを入力しないよう注意してください。</p>
	 * @param src 入力元データ
	 * @param base 入力元データの基数(16, 36, 62のみサポートしています)
	 * @exception NullPointerException srcがnull
	 * @exception IllegalArgumentException srcの文字数が2で割り切れず、最後の文字が"0"ではない
	 * @exception IllegalArgumentException srcに解析不可能な文字が含まれる
	 * @exception IllegalArgumentException radixが16, 36, 62以外
	 */
	public BmsArray(String src, int base) {
		setup(base, src);
	}

	/**
	 * 指定された配列と同等の配列を生成します。
	 * <p>新しく生成された配列オブジェクトは、入力元配列と同じ基数、要素数、データ内容を返すようになります。</p>
	 * @param array 入力元配列
	 * @exception NullPointerException arrayがnull
	 */
	public BmsArray(BmsArray array) {
		assertArgNotNull(array, "array");
		mInt = array.mInt;
		mArray = array.mArray;  // 一度生成されると変更されないので参照のみ渡す
	}

	/**
	 * BmsArrayオブジェクトをセットアップする
	 * @param base 基数
	 * @param src 変換元文字列
	 * @exception NullPointerException srcがnullの場合
	 * @exception IllegalArgumentException radixが16, 36, 62以外
	 * @exception IllegalArgumentException srcの文字数が2で割り切れず、最後の文字が"0"ではない
	 * @exception IllegalArgumentException srcに解析不可能な文字が含まれている
	 */
	private void setup(int base, String src) {
		assertArgNotNull(src, "src");

		// 入力配列が奇数文字数の場合、最後の文字が"0"である場合に限り最後の要素を"00"と見なす
		// 手打ち編集しているBMSに奇数文字数のミスが散見されるため、仕様違反ではあるがこれを救済措置とする
		var length = src.length();
		if ((length & 0x01) != 0) {
			assertArg(src.endsWith("0"), "Argument 'src' length is wrong. src='%s'", src);
			src = new StringBuilder(length + 1).append(src).append('0').toString();
		}

		mInt = BmsInt.of(base);
		mArray = mInt.toia(src, 0, src.length() / 2);
	}

	/**
	 * 配列の内容を文字列に変換します。
	 * <p>文字列の内容は、配列生成時に指定された入力元データ、および基数に従って変換されますが、
	 * 入力元データの内容に関わらずアルファベットは全て大文字で表現されます。</p>
	 * @return 文字列に変換された配列
	 */
	@Override
	public String toString() {
		return mInt.tosa(mArray, 0, mArray.length);
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
			return (mInt == o.mInt) && Arrays.equals(mArray, o.mArray);
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
	 * @since 0.8.0
	 */
	public int getBase() {
		return mInt.base();
	}
}
