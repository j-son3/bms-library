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
 * <p>BMSでは一般的な10進数の数値に加えて、2文字1セットの16進整数値(00～FF)、36進整数値(00～ZZ)、62進整数値(00～zz)、
 * およびそれらを連結した数値の羅列(配列)を頻繁に用います。
 * 当クラスでは、そのような表現の整数値および配列をプログラムで制御しやすい形式との相互変換を行う機能を提供します。</p>
 *
 * <p>提供する機能は以下の通りです。</p>
 *
 * <ul>
 * <li>1個の整数値を16進、36進、62進の文字列に変換する</li>
 * <li>1個の16進、36進、62進の文字列を整数値に変換する</li>
 * <li>整数値の配列を16進、36進、62進の文字列に変換する</li>
 * <li>複数の16進、36進、62進の文字列を整数値の配列に変換する</li>
 * </ul>
 */
public abstract class BmsInt {
	/** 16進数値の変換処理クラス */
	private static final BmsInt BASE16_INT = new Base16Int();
	/** 36進数値の変換処理クラス */
	private static final BmsInt BASE36_INT = new Base36Int();
	/** 62進数値の変換処理クラス */
	private static final BmsInt BASE62_INT = new Base62Int();

	/** 16進用文字→整数変換テーブル */
	private static final int[] TABLE_C2N16;
	/** 36進用文字→整数変換テーブル */
	private static final int[] TABLE_C2N36;
	/** 62進用文字→整数変換テーブル */
	private static final int[] TABLE_C2N62;
	/** 16進用数値→文字列変換テーブル */
	private static final String[] TABLE_N2S16;
	/** 36進用数値→文字列変換テーブル */
	private static final String[] TABLE_N2S36;
	/** 62進用数値→文字列変換テーブル */
	private static final String[] TABLE_N2S62;

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

		var c2n62 = new int[128];
		Arrays.fill(c2n62, -1);
		fillInt(c2n62, 0, '0', '9');
		fillInt(c2n62, 10, 'A', 'Z');
		fillInt(c2n62, 36, 'a', 'z');

		var n2s16 = new String[256];
		fillString(n2s16, "0123456789ABCDEF");

		var n2s36 = new String[1296];
		fillString(n2s36, "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ");

		var n2s62 = new String[3844];
		fillString(n2s62, "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz");

		TABLE_C2N16 = c2n16;
		TABLE_C2N36 = c2n36;
		TABLE_C2N62 = c2n62;
		TABLE_N2S16 = n2s16;
		TABLE_N2S36 = n2s36;
		TABLE_N2S62 = n2s62;
	}

	/** 16進数値の変換処理クラス */
	private static class Base16Int extends BmsInt {
		/** {@inheritDoc} */
		@Override public int base() { return 16; }
		/** {@inheritDoc} */
		@Override public int max() { return BmsSpec.VALUE_16_MAX; }
		/** {@inheritDoc} */
		@Override public String tos(int n) { return to16s(n); }
		/** {@inheritDoc} */
		@Override public int toi(String s) { return to16i(s); }
		/** {@inheritDoc} */
		@Override public String tosa(int[] a, int offset, int count) { return to16sa(a, offset, count); }
		/** {@inheritDoc} */
		@Override public int[] toia(String s, int offset, int count) { return to16ia(s, offset, count); }
		/** {@inheritDoc} */
		@Override int digit(char c) { return digit16(c); }
		/** {@inheritDoc} */
		@Override boolean within(int n) { return (n >= 0) && (n <= BmsSpec.VALUE_16_MAX); }
	}

	/** 36進数値の変換処理クラス */
	private static class Base36Int extends BmsInt {
		/** {@inheritDoc} */
		@Override public int base() { return 36; }
		/** {@inheritDoc} */
		@Override public int max() { return BmsSpec.VALUE_36_MAX; }
		/** {@inheritDoc} */
		@Override public String tos(int n) { return to36s(n); }
		/** {@inheritDoc} */
		@Override public int toi(String s) { return to36i(s); }
		/** {@inheritDoc} */
		@Override public String tosa(int[] a, int offset, int count) { return to36sa(a, offset, count); }
		/** {@inheritDoc} */
		@Override public int[] toia(String s, int offset, int count) { return to36ia(s, offset, count); }
		/** {@inheritDoc} */
		@Override int digit(char c) { return digit36(c); }
		/** {@inheritDoc} */
		@Override boolean within(int n) { return (n >= 0) && (n <= BmsSpec.VALUE_36_MAX); }
	}

	/** 62進数値の変換処理クラス */
	private static class Base62Int extends BmsInt {
		/** {@inheritDoc} */
		@Override public int base() { return 62; }
		/** {@inheritDoc} */
		@Override public int max() { return BmsSpec.VALUE_62_MAX; }
		/** {@inheritDoc} */
		@Override public String tos(int n) { return to62s(n); }
		/** {@inheritDoc} */
		@Override public int toi(String s) { return to62i(s); }
		/** {@inheritDoc} */
		@Override public String tosa(int[] a, int offset, int count) { return to62sa(a, offset, count); }
		/** {@inheritDoc} */
		@Override public int[] toia(String s, int offset, int count) { return to62ia(s, offset, count); }
		/** {@inheritDoc} */
		@Override int digit(char c) { return digit62(c); }
		/** {@inheritDoc} */
		@Override boolean within(int n) { return (n >= 0) && (n <= BmsSpec.VALUE_62_MAX); }
	}

	/** 整数値キャッシュ */
	private static class Cache {
		/** インデックス用キャッシュの最大値 */
		private static final int MX = 3844;
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
	 * この整数オブジェクトの基数を取得します。
	 * @return 整数オブジェクトの基数(16, 36, 62 のいずれか)
	 */
	public abstract int base();

	/**
	 * この整数オブジェクトが扱う1個の整数の最大値を取得します。
	 * <p>最大値は基数によって異なります。最小値はどの基数でも常に0です。</p>
	 * @return 整数オブジェクトが扱う1個の整数の最大値
	 */
	public abstract int max();

	/**
	 * 整数値を基数に対応した2文字の数値文字列に変換します。
	 * <p>当メソッドでは基数に応じて {@link #to16s(int)}, {@link #to36s(int)}, {@link #to62s(int)} を呼び分けます。</p>
	 * @param n 変換対象の整数値
	 * @return 基数に対応した2文字の数値文字列
	 * @exception IllegalArgumentException nが0未満または{@link #max()}超過
	 * @see #to16s(int)
	 * @see #to36s(int)
	 * @see #to62s(int)
	 */
	public abstract String tos(int n);

	/**
	 * 基数に対応した2文字の数値文字列を整数値に変換します。
	 * <p>当メソッドでは基数に応じて {@link #to16i(String)}, {@link #to36i(String)}, {@link #to62i(String)}
	 * を呼び分けます。</p>
	 * @param s 変換対象の文字列
	 * @return 整数値
	 * @exception NullPointerException sがnull
	 * @exception IllegalArgumentException sの長さが2以外
	 * @exception IllegalArgumentException sに変換不可能な文字がある
	 * @see #to16i(String)
	 * @see #to36i(String)
	 * @see #to62i(String)
	 */
	public abstract int toi(String s);

	/**
	 * 整数値配列を、基数に対応した数値文字列の羅列に変換します。
	 * <p>当メソッドは基数に応じて {@link #to16sa(int[], int, int)}, {@link #to36sa(int[], int, int)},
	 * {@link #to62sa(int[], int, int)} を呼び分けます。</p>
	 * @param a 整数値配列
	 * @param offset 変換開始位置
	 * @param count 変換数
	 * @return 基数に対応した数値文字列を羅列した文字列
	 * @exception NullPointerException aがnull
	 * @exception IllegalArgumentException offsetが0未満
	 * @exception IllegalArgumentException countが0未満
	 * @exception IndexOutOfBoundsException aの範囲外にアクセスした
	 * @exception IllegalArgumentException a内の変換対象に0未満または{@link #max()}超過の値がある
	 * @see #to16sa(int[], int, int)
	 * @see #to36sa(int[], int, int)
	 * @see #to62sa(int[], int, int)
	 */
	public abstract String tosa(int[] a, int offset, int count);

	/**
	 * 基数に対応した数値文字列の羅列を整数値配列に変換します。
	 * <p>当メソッドは基数に応じて {@link #to16ia(String, int, int)}, {@link #to36ia(String, int, int)},
	 * {@link #to62ia(String, int, int)} を呼び分けます。</p>
	 * @param s 基数に対応した数値文字列の羅列
	 * @param offset 変換開始位置
	 * @param count 変換個数(2文字1個セットの個数)
	 * @return 整数値配列
	 * @exception NullPointerException sがnull
	 * @exception IllegalArgumentException offsetが0未満
	 * @exception IllegalArgumentException countが0未満
	 * @exception IndexOutOfBoundsException sの範囲外にアクセスした
	 * @exception IllegalArgumentException sに変換不可能な文字がある
	 * @see #to16ia(String, int, int)
	 * @see #to36ia(String, int, int)
	 * @see #to62ia(String, int, int)
	 */
	public abstract int[] toia(String s, int offset, int count);

	/**
	 * 文字を数値に変換
	 * @param c 文字
	 * @return 変換後の数値、cが変換不可文字の場合-1。
	 */
	abstract int digit(char c);

	/**
	 * 指定数値がこの整数オブジェクトで取り扱い可能な範囲かチェック
	 * @param n 数値
	 * @return 範囲内であればtrue
	 */
	abstract boolean within(int n);

	/**
	 * 16進数の整数オブジェクトを取得します。
	 * @return 16進数の整数オブジェクト
	 */
	public static BmsInt base16() {
		return BASE16_INT;
	}

	/**
	 * 36進数の整数オブジェクトを取得します。
	 * @return 36進数の整数オブジェクト
	 */
	public static BmsInt base36() {
		return BASE36_INT;
	}

	/**
	 * 62進数の整数オブジェクトを取得します。
	 * @return 62進数の整数オブジェクト
	 */
	public static BmsInt base62() {
		return BASE62_INT;
	}

	/**
	 * 指定された基数の整数オブジェクトを取得します。
	 * <p>サポートしている基数は16, 36, 62のいずれかです。</p>
	 * @param base 基数
	 * @return 基数に該当する整数オブジェクト
	 * @exception IllegalArgumentException baseが16, 36, 62以外
	 */
	public static BmsInt of(int base) {
		switch (base) {
		case 16: return BASE16_INT;
		case 36: return BASE36_INT;
		case 62: return BASE62_INT;
		default: throw new IllegalArgumentException("Invalid base " + base);
		}
	}

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
	 * 整数値を2文字の62進文字列に変換します。
	 * @param n 整数値
	 * @return 2文字の62進文字列
	 * @exception IllegalArgumentException nが0未満または3843超過
	 */
	public static String to62s(int n) {
		assertArgRange(n, 0, 3843, "n");
		return TABLE_N2S62[n];
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
	 * 62進文字列を整数値に変換します。
	 * @param s 62進文字列
	 * @return 整数値
	 * @exception NullPointerException sがnull
	 * @exception IllegalArgumentException sの長さが2以外
	 * @exception IllegalArgumentException sに変換不可能な文字がある
	 */
	public static int to62i(String s) {
		assertArgNotNull(s, "s");
		assertArg(s.length() == 2, "Argument 's' invalid length. s=%s", s);
		return fetchN(TABLE_C2N62, s, 0, 62);
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
	 * @exception IllegalArgumentException a内の変換対象に0未満または1295超過の値がある
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
	 * 整数値配列を62進文字を羅列した文字列に変換します。
	 * @param a 整数値配列
	 * @param offset 変換開始位置
	 * @param count 変換数
	 * @return 62進文字を羅列した文字列
	 * @exception NullPointerException aがnull
	 * @exception IllegalArgumentException offsetが0未満
	 * @exception IllegalArgumentException countが0未満
	 * @exception IndexOutOfBoundsException aの範囲外にアクセスした
	 * @exception IllegalArgumentException a内の変換対象に0未満または3843超過の値がある
	 */
	public static String to62sa(int[] a, int offset, int count) {
		assertArgNotNull(a, "a");
		assertOffsetCount(offset, count);
		var sb = new StringBuilder(a.length * 2);
		var end = offset + count;
		for (var i = offset; i < end; i++) { sb.append(fetchS(TABLE_N2S62, a, i)); }
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
	 * 62進文字の羅列文字列を整数値配列に変換します。
	 * @param s 62進文字の羅列文字列
	 * @param offset 変換開始位置
	 * @param count 変換個数(2文字1個セットの個数)
	 * @return 整数値配列
	 * @exception NullPointerException sがnull
	 * @exception IllegalArgumentException offsetが0未満
	 * @exception IllegalArgumentException countが0未満
	 * @exception IndexOutOfBoundsException sの範囲外にアクセスした
	 * @exception IllegalArgumentException sに変換不可能な文字がある
	 */
	public static int[] to62ia(String s, int offset, int count) {
		assertArgNotNull(s, "s");
		assertOffsetCount(offset, count);
		var ary = new int[count];
		for (var i = 0; i < count; i++) { ary[i] = fetchN(TABLE_C2N62, s, offset + (i * 2), 62); }
		return ary;
	}

	/**
	 * 指定基数の対応有無判定
	 * @param base 基数
	 * @return 指定基数が対応していればtrue
	 */
	static boolean isSupportBase(int base) {
		return (base == 16) || (base == 36) || (base == 62);
	}

	/**
	 * 16進文字を数値に変換
	 * @param c 文字
	 * @return 変換後の数値、cが変換不可文字の場合-1。
	 */
	static int digit16(char c) {
		return fetchN(TABLE_C2N16, c);
	}

	/**
	 * 36進文字を数値に変換
	 * @param c 文字
	 * @return 変換後の数値、cが変換不可文字の場合-1。
	 */
	static int digit36(char c) {
		return fetchN(TABLE_C2N36, c);
	}

	/**
	 * 62進文字を数値に変換
	 * @param c 文字
	 * @return 変換後の数値、cが変換不可文字の場合-1。
	 */
	static int digit62(char c) {
		return fetchN(TABLE_C2N62, c);
	}

	/**
	 * 文字列をlong型数値へ変換
	 * <p>当メソッドは Long.parseLong(String, int) の62進数対応版として実装。</p>
	 * @param s 変換対象文字列
	 * @param base 基数(10, 16, 36, 62のいずれか)
	 * @return 変換後のlong型数値
	 * @throws NumberFormatException 変換対象文字列が不正
	 * @throws NumberFormatException baseが 10, 16, 36, 62 以外
	 */
    static long parseLong(String s, int base) throws NumberFormatException {
    	if (base == 10) {
    		return Long.parseLong(s);
    	}

    	var intc = (BmsInt)null;
    	try {
			assertArgNotNull(s, "s");
			assertArg(!s.isEmpty(), "Empty number string.");
			intc = of(base);
    	} catch (IllegalArgumentException e) {
    		throw new NumberFormatException(e.getMessage());
    	}

		final var FMT = "Invalid number format '%s'.";
		var negative = false;
		var i = 0;
		var len = s.length();
		var limit = -Long.MAX_VALUE;
		var firstChar = s.charAt(0);
		if (firstChar < '0') { // Possible leading "+" or "-"
			if (firstChar == '-') {
				negative = true;
				limit = Long.MIN_VALUE;
			} else if (firstChar != '+') {
				throw new NumberFormatException(String.format(FMT, s));
			}
			if (len == 1) { // Cannot have lone "+" or "-"
				throw new NumberFormatException(String.format(FMT, s));
			}
			i++;
		}

		var multmin = limit / base;
		var result = 0L;
		while (i < len) {
			// Accumulating negatively avoids surprises near MAX_VALUE
			int digit = intc.digit(s.charAt(i++));
			if (digit < 0 || result < multmin) {
				throw new NumberFormatException(String.format(FMT, s));
			}
			result *= base;
			if (result < limit + digit) {
				throw new NumberFormatException(String.format(FMT, s));
			}
			result -= digit;
		}
		return negative ? result : -result;
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
	 * 1文字→数値変換テーブルからの数値取り出し
	 * @param a 文字→数値変換テーブル
	 * @param c 変換対象文字
	 * @return 変換後数値、cが変換不可文字の場合-1。
	 */
	private static int fetchN(int[] a, char c) {
		return ((c & 0xffffff80) == 0) ? a[c] : -1;
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
	 * @param chars 基数に対応した文字列
	 */
	private static void fillString(String[] a, String chars) {
		var valChr = new char[2];
		var radix = chars.length();
		for (var i = 0; i < a.length; i++) {
			valChr[0] = chars.charAt(i / radix);
			valChr[1] = chars.charAt(i % radix);
			a[i] = String.valueOf(valChr);
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
