package com.lmt.lib.bms;

import static com.lmt.lib.bms.internal.Assertion.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.IntConsumer;
import java.util.stream.IntStream;

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

	/** 整数値キャッシュ */
	private static class Cache {
		/** インデックス用キャッシュの最大値 */
		private static final int MX = 1296;
		/** CHX用キャッシュのチャンネル番号最大値 */
		private static final int CMX = 1296;
		/** CHX用キャッシュのチャンネルインデックス最大値 */
		private static final int IMX = 4;

		/** 統計フラグ：キャッシュ未使用 */
		private static final int UNUSE = 0x00;
		/** 統計フラグ：インデックス用キャッシュヒット */
		private static final int HIT_IDX = 0x01;
		/** 統計フラグ：CHX用キャッシュヒット */
		private static final int HIT_CHX = 0x02;
		/** 統計フラグ：いずれかのキャッシュヒット */
		private static final int HIT_ANY = HIT_IDX | HIT_CHX;
		/** 統計フラグ：インデックス用キャッシュ要求 */
		private static final int REQ_IDX = 0x10;
		/** 統計フラグ：CHX用キャッシュ要求 */
		private static final int REQ_CHX = 0x20;

		/** 整数値要求回数 */
		private long mReqTotal = 0L;
		/** 整数値キャッシュヒット回数 */
		private long mHitTotal = 0L;
		/** インデックス用キャッシュ要求回数 */
		private long mReqIndex = 0L;
		/** インデックス用キャッシュヒット回数 */
		private long mHitIndex = 0L;
		/** CHX用キャッシュ要求回数 */
		private long mReqChx = 0L;
		/** CHX用キャッシュヒット回数 */
		private long mHitChx = 0L;

		/** 統計処理用関数 */
		private IntConsumer mStat;
		/** インデックス用キャッシュ */
		private Integer[] mCache4Index;
		/** CHX用キャッシュ */
		private List<Integer[]> mCache4Chx;
		/** 統計処理排他制御用オブジェクト */
		private Object mLockForDiag = new Object();

		/**
		 * コンストラクタ
		 */
		Cache() {
			// 整数値のキャッシュを生成する
			mCache4Index = IntStream.range(0, MX).mapToObj(n -> n).toArray(Integer[]::new);
			mCache4Chx = new ArrayList<>(IMX);
			IntStream.range(0, IMX).forEach(i -> {
				mCache4Chx.add(IntStream.range(0, CMX).mapToObj(n -> (n << 16) | i).toArray(Integer[]::new));
			});

			// デフォルトでは統計は無効
			statistics(false);
		}

		/**
		 * 統計処理設定
		 * @param diag 統計実行有無
		 */
		void statistics(boolean diag) {
			// 統計データを初期化する
			synchronized (mLockForDiag) {
				mReqTotal = 0L;
				mHitTotal = 0L;
				mReqIndex = 0L;
				mHitIndex = 0L;
				mReqChx = 0L;
				mHitChx = 0L;
				mStat = diag ? this::diagExecute : this::diagNothing;
			}
		}

		/**
		 * 統計情報集計結果スナップショット
		 * @return 統計情報集計結果スナップショット
		 */
		long[] snapshot() {
			synchronized (mLockForDiag) {
				return new long[] { mReqTotal, mHitTotal, mReqIndex, mHitIndex, mReqChx, mHitChx };
			}
		}

		/**
		 * 整数値取得
		 * @param n 値型整数値
		 * @return 整数値オブジェクト
		 */
		Integer get(int n) {
			var stat = mStat;
			var low = (n & 0xffff);
			var high = (n >> 16) & 0xffff;
			if (high == 0) {
				// 下位16ビット(0～65535)の場合は通常インデックスのキャッシュ
				if (low < MX) {
					// キャッシュあり
					stat.accept(REQ_IDX | HIT_IDX);
					return mCache4Index[low];
				} else {
					// キャッシュなし
					stat.accept(REQ_IDX);
					return Integer.valueOf(n);
				}
			} else if (high < CMX) {
				// 上位16ビットありの場合はCHXのキャッシュ
				if (low < IMX) {
					// キャッシュあり
					stat.accept(REQ_CHX | HIT_CHX);
					return mCache4Chx.get(low)[high];
				} else {
					// キャッシュなし
					stat.accept(REQ_CHX);
					return Integer.valueOf(n);
				}
			} else {
				// キャッシュを使用しない
				stat.accept(UNUSE);
				return Integer.valueOf(n);
			}
		}

		/**
		 * 統計処理なし
		 * @param flags 統計フラグ
		 */
		private void diagNothing(int flags) {
			// Do nothing
		}

		/**
		 * 統計処理あり
		 * @param flags 統計フラグ
		 */
		private void diagExecute(int flags) {
			synchronized (mLockForDiag) {
				mReqTotal++;
				mHitTotal += ((flags & HIT_ANY) != 0) ? 1L : 0L;
				mReqIndex += ((flags & REQ_IDX) != 0) ? 1L : 0L;
				mHitIndex += ((flags & HIT_IDX) != 0) ? 1L : 0L;
				mReqChx += ((flags & REQ_CHX) != 0) ? 1L : 0L;
				mHitChx += ((flags & HIT_CHX) != 0) ? 1L : 0L;
			}
		}
	}

	/** 整数値キャッシュ */
	private static Cache sCache = new Cache();

	/**
	 * 整数値キャッシュヒットの統計有無を設定します。
	 * <p>当メソッドを呼び出して統計有無を設定すると、呼び出し前の統計有無設定の内容に関わらず統計情報はリセットされ、
	 * 全てのカウンタが0になります。</p>
	 * <p>引数にtrueを指定すると統計が有効になり、{@link #box(int)}が実行される度にキャッシュヒットの統計情報が
	 * 更新されるようになります。この更新処理はスレッドセーフになっている関係上、統計を有効にすると整数値キャッシュの
	 * 取得処理({@link #box(int)})のパフォーマンスが低下します。そのため統計機能は基本的にはデバッグ用であると
	 * 認識してください。</p>
	 * <p>引数にfalseを指定すると統計が無効になり、統計情報がリセットされた後は統計情報の更新は行われません。</p>
	 * @param diag 統計有無
	 */
	public static void cacheStatistics(boolean diag) {
		sCache.statistics(diag);
	}

	/**
	 * 整数値キャッシュヒットの統計情報集計結果スナップショットを取得します。
	 * <p>当メソッドは{@link #box(int)}による整数値キャッシュの要求内容から生成した統計情報集計結果のスナップショットを取り、
	 * それらの値を配列にして返します。返された配列は以下のような構成になっています。</p>
	 * <ul>
	 * <li>[0] 整数値要求回数({@link #box(int)}が呼ばれた回数)</li>
	 * <li>[1] 整数値キャッシュヒット回数</li>
	 * <li>[2] 通常インデックス用キャッシュ(上位16ビットが全て0の整数値)要求回数</li>
	 * <li>[3] 通常インデックス用キャッシュヒット回数</li>
	 * <li>[4] CHX用キャッシュ(上位16ビットのいずれかが1の整数値)要求回数</li>
	 * <li>[5] CHX用キャッシュヒット回数</li>
	 * </ul>
	 * <p>整数値キャッシュの統計が無効になっている場合、上記配列の各要素は全て0を示します。</p>
	 * @return 整数値キャッシュヒットの統計情報集計結果スナップショット
	 */
	public static long[] cacheSnapshotResult() {
		return sCache.snapshot();
	}

	/**
	 * 指定された値型の整数値をBOX化した整数値オブジェクトをキャッシュから取得します。
	 * <p>Javaでは標準で-127～127のIntegerがキャッシュされていますが、BMSではそれよりも大きい値を扱うことがほとんどであり
	 * 標準のキャッシュが利用できない場合が大半を占めます。そのままではJavaヒープの確保・解放が頻繁に実行されることとなり
	 * パフォーマンスに影響が出ることが懸念されるため、BMSライブラリでは独自の整数値キャッシュを保持しています。</p>
	 * <p>索引付きメタ情報やBMS仕様のチャンネルはそれぞれ0～1295までの値を扱うことが多いことから、それらをキャッシュし
	 * 保持しています。また、CHX値は上位16ビットがチャンネル番号、下位16ビットがチャンネルインデックスとなっていますが、
	 * 頻出するCHX値もキャッシュし保持しています。</p>
	 * <p>当メソッドで整数値をBOX化しようとする時、上記に示したキャッシュを最初に参照しその整数値オブジェクトを
	 * 返そうとします。キャッシュにない整数値は{@link Integer#valueOf(int)}に処理を委譲します。</p>
	 * <p>当メソッドはどのような値を指定してもnullを返したり例外をスローすることはありません。</p>
	 * @param n BOX化対象の値型整数値
	 * @return BOX化された整数値オブジェクト
	 */
	public static Integer box(int n) {
		return sCache.get(n);
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
