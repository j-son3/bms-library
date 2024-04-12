package com.lmt.lib.bms;

import static com.lmt.lib.bms.BmsAssertion.*;

import java.util.Arrays;

/**
 * BMS向けの整数値処理を定義したクラスです。
 *
 * <p>BMSでは一般的な10進数の数値に加えて、2文字1セットの16進整数値(00～FF)、36進整数値(00～ZZ)、およびそれらを連結した
 * 数値の羅列(配列)を頻繁に用います。当クラスでは、そのような表現の整数値および配列をプログラムで制御しやすい形式との
 * 相互変換を行う機能を提供します。</p>
 *
 * <p>提供する機能は以下の通りです。</p>
 *
 * <ul>
 * <li>1個の整数値を16進または36進の文字列に変換する</li>
 * <li>1個の16進または36進の文字列を整数値に変換する</li>
 * <li>整数値の配列を16進または36進の文字列に変換する</li>
 * <li>複数の16進または36進の文字列を整数値の配列に変換する</li>
 * </ul>
 */
public class BmsInt {
	/** 16進用文字→整数変換テーブル */
	private static final int[] TABLE_C2N16;
	/** 36進用文字→整数変換テーブル */
	private static final int[] TABLE_C2N36;
	/** 16進用数値→文字列変換テーブル */
	private static final String[] TABLE_N2S16;
	/** 36進用数値→文字列変換テーブル */
	private static final String[] TABLE_N2S36;

	/** 静的初期化 */
	static {
		var c2n16 = new int[128];
		Arrays.fill(c2n16, -1);
		fillInt(c2n16, 0, '0', '9');
		fillInt(c2n16, 10, 'A', 'F');
		fillInt(c2n16, 10, 'a', 'f');

		var c2n36 = new int[128];
		Arrays.fill(c2n36, -1);
		fillInt(c2n36, 0, '0', '9');
		fillInt(c2n36, 10, 'A', 'Z');
		fillInt(c2n36, 10, 'a', 'z');

		var n2s16 = new String[256];
		fillString(n2s16, 16);

		var n2s36 = new String[1296];
		fillString(n2s36, 36);

		TABLE_C2N16 = c2n16;
		TABLE_C2N36 = c2n36;
		TABLE_N2S16 = n2s16;
		TABLE_N2S36 = n2s36;
	}

	/**
	 * 整数値を2文字の16進文字列に変換します。
	 * @param n 整数値
	 * @return 2文字の16進文字列
	 * @exception IllegalArgumentException nが0未満または255超過
	 */
	public static String to16s(int n) {
		assertArgRange(n, 0, 255, "n");
		return TABLE_N2S16[n];
	}

	/**
	 * 整数値を2文字の36進文字列に変換します。
	 * @param n 整数値
	 * @return 2文字の36進文字列
	 * @exception IllegalArgumentException nが0未満または1295超過
	 */
	public static String to36s(int n) {
		assertArgRange(n, 0, 1295, "n");
		return TABLE_N2S36[n];
	}

	/**
	 * 16進文字列を整数値に変換します。
	 * @param s 16進文字列
	 * @return 整数値
	 * @exception NullPointerException sがnull
	 * @exception IllegalArgumentException sの長さが2以外
	 * @exception IllegalArgumentException sに変換不可能な文字がある
	 */
	public static int to16i(String s) {
		assertArgNotNull(s, "s");
		assertArg(s.length() == 2, "Argument 's' invalid length. s=%s", s);
		return fetchN(TABLE_C2N16, s, 0, 16);
	}

	/**
	 * 36進文字列を整数値に変換します。
	 * @param s 36進文字列
	 * @return 整数値
	 * @exception NullPointerException sがnull
	 * @exception IllegalArgumentException sの長さが2以外
	 * @exception IllegalArgumentException sに変換不可能な文字がある
	 */
	public static int to36i(String s) {
		assertArgNotNull(s, "s");
		assertArg(s.length() == 2, "Argument 's' invalid length. s=%s", s);
		return fetchN(TABLE_C2N36, s, 0, 36);
	}

	/**
	 * 整数値配列を16進文字を羅列した文字列に変換します。
	 * @param a 整数値配列
	 * @param offset 変換開始位置
	 * @param count 変換数
	 * @return 16進文字を羅列した文字列
	 * @exception NullPointerException aがnull
	 * @exception IllegalArgumentException offsetが0未満
	 * @exception IllegalArgumentException countが0未満
	 * @exception IndexOutOfBoundsException aの範囲外にアクセスした
	 * @exception IllegalArgumentException a内の変換対象に0未満または255超過の値がある
	 */
	public static String to16sa(int[] a, int offset, int count) {
		assertArgNotNull(a, "a");
		assertOffsetCount(offset, count);
		var sb = new StringBuilder(a.length * 2);
		var end = offset + count;
		for (var i = offset; i < end; i++) { sb.append(fetchS(TABLE_N2S16, a, i)); }
		return sb.toString();
	}

	/**
	 * 整数値配列を36進文字を羅列した文字列に変換します。
	 * @param a 整数値配列
	 * @param offset 変換開始位置
	 * @param count 変換数
	 * @return 36進文字を羅列した文字列
	 * @exception NullPointerException aがnull
	 * @exception IllegalArgumentException offsetが0未満
	 * @exception IllegalArgumentException countが0未満
	 * @exception IndexOutOfBoundsException aの範囲外にアクセスした
	 * @exception IllegalArgumentException a内の変換対象に0未満または255超過の値がある
	 */
	public static String to36sa(int[] a, int offset, int count) {
		assertArgNotNull(a, "a");
		assertOffsetCount(offset, count);
		var sb = new StringBuilder(a.length * 2);
		var end = offset + count;
		for (var i = offset; i < end; i++) { sb.append(fetchS(TABLE_N2S36, a, i)); }
		return sb.toString();
	}

	/**
	 * 16進文字の羅列文字列を整数値配列に変換します。
	 * @param s 16進文字の羅列文字列
	 * @param offset 変換開始位置
	 * @param count 変換個数(2文字1個セットの個数)
	 * @return 整数値配列
	 * @exception NullPointerException sがnull
	 * @exception IllegalArgumentException offsetが0未満
	 * @exception IllegalArgumentException countが0未満
	 * @exception IndexOutOfBoundsException sの範囲外にアクセスした
	 * @exception IllegalArgumentException sに変換不可能な文字がある
	 */
	public static int[] to16ia(String s, int offset, int count) {
		assertArgNotNull(s, "s");
		assertOffsetCount(offset, count);
		var ary = new int[count];
		for (var i = 0; i < count; i++) { ary[i] = fetchN(TABLE_C2N16, s, offset + (i * 2), 16); }
		return ary;
	}

	/**
	 * 36進文字の羅列文字列を整数値配列に変換します。
	 * @param s 36進文字の羅列文字列
	 * @param offset 変換開始位置
	 * @param count 変換個数(2文字1個セットの個数)
	 * @return 整数値配列
	 * @exception NullPointerException sがnull
	 * @exception IllegalArgumentException offsetが0未満
	 * @exception IllegalArgumentException countが0未満
	 * @exception IndexOutOfBoundsException sの範囲外にアクセスした
	 * @exception IllegalArgumentException sに変換不可能な文字がある
	 */
	public static int[] to36ia(String s, int offset, int count) {
		assertArgNotNull(s, "s");
		assertOffsetCount(offset, count);
		var ary = new int[count];
		for (var i = 0; i < count; i++) { ary[i] = fetchN(TABLE_C2N36, s, offset + (i * 2), 36); }
		return ary;
	}

	/**
	 * 文字→数値変換テーブルからの数値取り出し
	 * @param a 文字→数値変換テーブル
	 * @param s 変換対象文字列
	 * @param pos 変換開始位置
	 * @param radix 基数
	 * @return 変換後数値
	 */
	private static int fetchN(int[] a, String s, int pos, int radix) {
		var n = 0;
		for (var i = 0; i < 2; i++) {
			var si = pos + i;
			var c = (int)s.charAt(si);
			var v = ((c & 0xffffff80) == 0) ? a[c] : -1;
			if (v < 0) {
				var msg = String.format("Source '%s' has invalid char. pos=%d, char=%c(0x%04X)", s, i, c, c);
				throw new IllegalArgumentException(msg);
			}
			n = (n * radix) + v;
		}
		return n;
	}

	/**
	 * 数値→文字列変換テーブルからの文字列取り出し
	 * @param sa 数値→文字列変換テーブル
	 * @param na 変換対象数値配列
	 * @param pos 変換位置
	 * @return 変換後文字列
	 */
	private static String fetchS(String[] sa, int[] na, int pos) {
		var n = na[pos];
		if ((n < 0) || (n >= sa.length)) {
			var msg = String.format("Source array has invalid number. source-length=%d, pos=%d, number=%d",
					na.length, pos, n);
			throw new IllegalArgumentException(msg);
		}
		return sa[n];
	}

	/**
	 * 数値埋め
	 * @param a 編集対象数値配列
	 * @param base ベース数値
	 * @param first 編集開始位置
	 * @param last 編集終了位置
	 */
	private static void fillInt(int[] a, int base, char first, char last) {
		for (var i = first; i <= last; i++) {
			a[i] = base++;
		}
	}

	/**
	 * 文字列埋め
	 * @param a 編集対象文字列配列
	 * @param radix 基数
	 */
	private static void fillString(String[] a, int radix) {
		for (var i = 0; i < a.length; i++) {
			var org = Integer.toString(i, radix);
			org = (org.length() >= 2) ? org : "0".concat(org);
			a[i] = org.toUpperCase();
		}
	}

	/**
	 * オフセット値と個数のアサーション
	 * @param offset オフセット
	 * @param count 個数
	 */
	private static void assertOffsetCount(int offset, int count) {
		assertArg(offset >= 0, "Argument 'offset' must be >=0. offset=%d", offset);
		assertArg(count >= 0, "Argument 'count' must be >0. count=%d", count);
	}
}
