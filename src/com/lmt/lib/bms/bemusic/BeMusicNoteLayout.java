package com.lmt.lib.bms.bemusic;

import static com.lmt.lib.bms.internal.Assertion.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

/**
 * ノート配置のレイアウトを表現するクラスです。
 *
 * <p>当クラスでは、レーン内の各入力デバイスのアサインを仮想的に入れ替え、入れ替え後の入力デバイスにノートが
 * 割り当たっているように見せる役割を果たします。この機能を用いることで譜面のノートをデータ的に変更することなく
 * ノートの入れ替えを実現することが可能になります。</p>
 *
 * @since 0.1.0
 */
public class BeMusicNoteLayout implements Comparable<BeMusicNoteLayout> {
	/** 主レーンにおける正規配置パターン */
	public static final String PATTERN_PRIMARY_REGULAR = "1234567S";
	/** 主レーンにおけるミラー配置パターン */
	public static final String PATTERN_PRIMARY_MIRROR = "7654321S";
	/** 副レーンにおける正規配置パターン */
	public static final String PATTERN_SECONDARY_REGULAR = "ABCDEFGS";
	/** 副レーンにおけるミラー配置パターン */
	public static final String PATTERN_SECONDARY_MIRROR = "GFEDCBAS";

	/** 主レーンに指定可能な文字リスト(スクラッチなし) */
	private static final List<Character> PRIMARY_LIST = List.of('1', '2', '3', '4', '5', '6', '7');
	/** 主レーンに指定可能な文字リスト(スクラッチあり) */
	private static final List<Character> PRIMARY_FULL_LIST = List.of('1', '2', '3', '4', '5', '6', '7', 'S');
	/** 副レーンに指定可能な文字リスト(スクラッチなし) */
	private static final List<Character> SECONDARY_LIST = List.of('A', 'B', 'C', 'D', 'E', 'F', 'G');
	/** 副レーンに指定可能な文字リスト(スクラッチあり) */
	private static final List<Character> SECONDARY_FULL_LIST = List.of('A', 'B', 'C', 'D', 'E', 'F', 'G', 'S');
	/** レーンレイアウトに指定可能な文字リスト(スクラッチなし)全て */
	private static final List<List<Character>> SWITCH_LIST = List.of(PRIMARY_LIST, SECONDARY_LIST);

	/** 任意のレイアウトパターン生成用乱数オブジェクト */
	private static final Random RANDOM_OBJECT = new Random();

	/** 主レーン用デバイスマップ */
	private static final Map<Character, BeMusicDevice> PRIMARY_DEVICE_MAP = Map.ofEntries(
			Map.entry('1', BeMusicDevice.SWITCH11), Map.entry('2', BeMusicDevice.SWITCH12),
			Map.entry('3', BeMusicDevice.SWITCH13), Map.entry('4', BeMusicDevice.SWITCH14),
			Map.entry('5', BeMusicDevice.SWITCH15), Map.entry('6', BeMusicDevice.SWITCH16),
			Map.entry('7', BeMusicDevice.SWITCH17), Map.entry('S', BeMusicDevice.SCRATCH1));
	/** 副レーン用デバイスマップ */
	private static final Map<Character, BeMusicDevice> SECONDARY_DEVICE_MAP = Map.ofEntries(
			Map.entry('A', BeMusicDevice.SWITCH21), Map.entry('B', BeMusicDevice.SWITCH22),
			Map.entry('C', BeMusicDevice.SWITCH23), Map.entry('D', BeMusicDevice.SWITCH24),
			Map.entry('E', BeMusicDevice.SWITCH25), Map.entry('F', BeMusicDevice.SWITCH26),
			Map.entry('G', BeMusicDevice.SWITCH27), Map.entry('S', BeMusicDevice.SCRATCH2));

	/** シングルプレーの正規レイアウト */
	public static final BeMusicNoteLayout SP_REGULAR =
			new BeMusicNoteLayout(PATTERN_PRIMARY_REGULAR);
	/** シングルプレーのミラーレイアウト */
	public static final BeMusicNoteLayout SP_MIRROR =
			new BeMusicNoteLayout(PATTERN_PRIMARY_MIRROR);
	/** ダブルプレーの正規レイアウト */
	public static final BeMusicNoteLayout DP_REGULAR =
			new BeMusicNoteLayout(PATTERN_PRIMARY_REGULAR, PATTERN_SECONDARY_REGULAR);

	/** 主レーンのレイアウト */
	private String mPrimary;
	/** 副レーンのレイアウト */
	private String mSecondary;
	/** 主・副レーンが入れ替わっているかどうか */
	private boolean mIsFlip;
	/** デバイスレイアウトテーブル */
	private BeMusicDevice[] mLayout;

	/**
	 * シングルプレー用のノートレイアウトオブジェクトを構築します。
	 * <p>レイアウトは主レーンのスイッチ1～7を表す'1'～'7'の文字とスクラッチを表す'S'の文字で指定します。
	 * レイアウトは7または8文字で指定し、スクラッチのみ省略が可能です。
	 * スクラッチを指定する場合、スイッチとスクラッチを含めたレイアウトが行えます。スクラッチにアサインするノートは
	 * 8文字目に記述してください。</p>
	 * <p>1つの入力デバイスを複数にアサインすることはできません。そのような指定があった場合は例外をスローします。</p>
	 * @param layout 主レーンのレイアウト文字列
	 * @exception NullPointerException layoutがnull
	 * @exception IllegalArgumentException レイアウト記述ルール違反
	 */
	public BeMusicNoteLayout(String layout) {
		this(layout, null);
	}

	/**
	 * ノートレイアウトオブジェクトを構築します。
	 * <p>主レーンのレイアウトはスイッチ1～7を表す'1'～'7'の文字とスクラッチを表す'S'の文字で指定します。
	 * 副レーンのレイアウトはスイッチ1～7を表す'A'～'G'の文字とスクラッチを表す'S'の文字で指定します。</p>
	 * <p>レイアウトは7または8文字で指定し、スクラッチのみ省略が可能です。
	 * スクラッチを指定する場合、スイッチとスクラッチを含めたレイアウトが行えます。スクラッチにアサインするノートは
	 * 8文字目に記述してください。</p>
	 * <p>1つのノートを複数にアサインすることはできません。そのような指定があった場合は例外をスローします。</p>
	 * <p>副レーンのレイアウトを省略(null指定)するとシングルプレー用、指定するとダブルプレー用の
	 * ノートレイアウトオブジェクトとなります。主レーンに副レーンのレイアウトを指定すると両者を
	 * 入れ替えるレイアウト(FLIP)を構築することができます。ただし、シングルプレー用レイアウトで副レーンの
	 * レイアウトを指定することはできません。指定すると例外がスローされます。</p>
	 * @param primary 主レーンのレイアウト文字列
	 * @param secondary 副レーンのレイアウト文字列
	 * @exception NullPointerException primaryがnull
	 * @exception IllegalArgumentException 主レーンのレイアウト記述ルール違反
	 * @exception IllegalArgumentException 副レーンのレイアウト記述ルール違反
	 * @exception IllegalArgumentException 主レーンに副レーンのレイアウト指定時、副レーンのレイアウトが主レーンのレイアウトではない
	 */
	public BeMusicNoteLayout(String primary, String secondary) {
		// PRIMARYのセットアップ
		mPrimary = primary;
		assertArgNotNull(primary, "primary");
		var isSp = (secondary == null);
		var pOkP = testPattern(primary, PRIMARY_FULL_LIST);
		var pOkS = isSp ? false : testPattern(primary, SECONDARY_FULL_LIST);
		assertArg(pOkP || pOkS, "Argument 'primary' syntax error. primary=%s, secondary=%s", primary, secondary);

		// SECONDARYのセットアップ
		mSecondary = secondary;
		mIsFlip = pOkS;
		if (!isSp) {
			// ダブルプレー用の場合のみ副レーンを検査する
			var sList = pOkP ? SECONDARY_FULL_LIST : PRIMARY_FULL_LIST;
			var sOk = testPattern(secondary, sList);
			assertArg(sOk, "Argument 'secondary' syntax error. primary=%s, secondary=%s", primary, secondary);
		}

		// デバイスレイアウトを構築する
		var map = pOkP ? PRIMARY_DEVICE_MAP : SECONDARY_DEVICE_MAP;
		mLayout = new BeMusicDevice[BeMusicDevice.COUNT];
		primary = (primary.length() == 7) ? (primary + "S") : primary;
		for (var i = 0; i < BeMusicDevice.COUNT_PER_LANE; i++) {
			mLayout[i] = map.get(primary.charAt(i));
		}
		if (isSp) {
			// シングルプレー用の場合副レーンはレイアウト変更なしとする
			for (var i = 0; i < BeMusicDevice.COUNT_PER_LANE; i++) {
				var index = BeMusicDevice.SECONDARY_BASE + i;
				mLayout[index] = BeMusicDevice.fromIndex(index);
			}
		} else {
			// ダブルプレー用の場合副レーンのレイアウト替えを行う
			map = pOkP ? SECONDARY_DEVICE_MAP : PRIMARY_DEVICE_MAP;
			secondary = (secondary.length() == 7) ? (secondary + "S") : secondary;
			for (var i = 0; i < BeMusicDevice.COUNT_PER_LANE; i++) {
				mLayout[BeMusicDevice.SECONDARY_BASE + i] = map.get(secondary.charAt(i));
			}
		}
	}

	/**
	 * 指定レーン用のいずれかのレイアウトパターンを生成します。
	 * <p>当メソッドはスイッチのみの全レイアウトパターン5040種類の中から任意のレイアウトをランダムで1つ生成します。
	 * スクラッチのレイアウト変更は行われないので注意してください。</p>
	 * @param lane レイアウトパターン生成対象レーン
	 * @return 指定用レイアウトパターン
	 * @exception NullPointerException laneがnull
	 */
	public static String generateAnyPatterns(BeMusicLane lane) {
		assertArgNotNull(lane, "lane");
		synchronized (RANDOM_OBJECT) {
			var list = SWITCH_LIST.get(lane.getIndex());
			return generateRandom(new StringBuilder(), new ArrayList<>(list));
		}
	}

	/**
	 * 指定レーン用の全てのレイアウトパターンを生成します。
	 * <p>当メソッドはスイッチのみの全レイアウトパターン5040種類全てを生成します。
	 * スクラッチのレイアウト変更は行われないので注意してください。</p>
	 * @param lane レイアウトパターン生成対象レーン
	 * @return 指定レーン用の全レイアウトパターンリスト
	 * @exception NullPointerException laneがnull
	 */
	public static List<String> generateAllPatterns(BeMusicLane lane) {
		assertArgNotNull(lane, "lane");
		var list = SWITCH_LIST.get(lane.getIndex());
		var outList = new ArrayList<String>(5040);
		generatePattern(outList, new StringBuilder(), list);
		return outList;
	}

	/**
	 * 指定レーン用のローテートレイアウトパターンを生成します。
	 * <p>ローテートレイアウトパターンとは、正規、またはミラーパターンを右方向にずらした合計12個のレイアウトパターン
	 * のことを指します。ただし、このパターンリストに正規・ミラーパターンは含まれません。
	 * また、スクラッチのレイアウト変更は行われないので注意してください。</p>
	 * @param lane レイアウトパターン生成対象レーン
	 * @return 指定レーン用のローテートレイアウトパターンリスト
	 * @exception NullPointerException laneがnull
	 */
	public static List<String> generateAllRotatePatterns(BeMusicLane lane) {
		assertArgNotNull(lane, "lane");
		var list = SWITCH_LIST.get(lane.getIndex());
		return generateRotatePatterns(list);
	}

	/**
	 * レイアウトパターンの比較を行います。
	 * <p>当メソッドでは同じレイアウトパターンを指定したかどうかを比較します。
	 * 従って、スクラッチを指定したかどうかも比較対象となります。例えば「1234567」「1234567S」はレイアウト変更結果は
	 * 全く同じになりますが、レイアウトパターンとしては異なるものとして認識されます。</p>
	 * @param obj 比較対象レイアウト
	 * @return このオブジェクトと指定レイアウトのレイアウトパターンが同じであればtrue
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof BeMusicNoteLayout) {
			var layout = (BeMusicNoteLayout)obj;
			if (!mPrimary.equals(layout.mPrimary)) {
				return false;
			} else if (mSecondary == null) {
				return layout.mSecondary == null;
			} else {
				return mSecondary.equals(layout.mSecondary);
			}
		} else {
			return false;
		}
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		return Objects.hash(mPrimary, (mSecondary == null) ? "" : mSecondary);
	}

	/**
	 * レイアウトの文字列表現を返します。
	 * <p>具体的にはレイアウトパターン文字列が返されます。
	 * ダブルプレー用レイアウトでは主レーン用と副レーン用のレイアウトパターン文字列を結合した文字列が
	 * 返されます。</p>
	 * @return レイアウトの文字列表現
	 */
	@Override
	public String toString() {
		return isSinglePlayLayout() ? mPrimary : String.format("%s/%s", mPrimary, mSecondary);
	}

	/**
	 * レイアウトパターンの比較を行います。
	 * <p>比較のソースとしてレイアウトオブジェクト構築時に指定されたレイアウトパターンを用います。
	 * 主レーンのレイアウトパターンが同じ場合、副レーンのレイアウトパターンで比較を行います。
	 * このようなケースではダブルプレー用のレイアウトのほうが大きいと判定されます。</p>
	 * @param o 比較対象レイアウト
	 * @return 比較対象レイアウトと等価の場合0、小さければ負の値、大きければ正の値
	 * @exception NullPointerException oがnull
	 */
	@Override
	public int compareTo(BeMusicNoteLayout o) {
		var comp = mPrimary.compareTo(o.mPrimary);
		if (comp != 0) {
			return comp;
		} else if (mSecondary == null) {
			return (o.mSecondary == null) ? 0 : -1;
		} else {
			return (o.mSecondary == null) ? 1 : mSecondary.compareTo(o.mSecondary);
		}
	}

	/**
	 * レイアウトパターンに従ってマッピングされた入力デバイスを取得します。
	 * <p>引数の入力デバイスはレイアウト変更前の正規の入力デバイスを指定します。戻り値ではレイアウトパターンに従って
	 * 変更された入力デバイスを返します。</p>
	 * <p>シングルプレー用レイアウトで副レーンの入力デバイスを指定しても効果はありません。</p>
	 * @param before レイアウト変更前のノートを示す入力デバイス
	 * @return レイアウト変更後のノートを示す入力デバイス
	 * @exception NullPointerException beforeがnull
	 */
	public final BeMusicDevice get(BeMusicDevice before) {
		assertArgNotNull(before, "before");
		return mLayout[before.getIndex()];
	}

	/**
	 * 主レーン用レイアウトパターンを取得します。
	 * <p>当メソッドは、オブジェクト構築時に指定したレイアウトパターンを返します。</p>
	 * @return 主レーン用レイアウトパターン
	 */
	public final String getPrimaryPattern() {
		return mPrimary;
	}

	/**
	 * 副レーン用レイアウトパターンを取得します。
	 * <p>当メソッドは、レイアウトオブジェクト構築時に指定したレイアウトパターンを返します。シングルプレー用レイアウトでは
	 * 副レーンのレイアウトパターンがnullで返ることに注意してください。</p>
	 * @return 副レーン用レイアウトパターン
	 */
	public final String getSecondaryPattern() {
		return mSecondary;
	}

	/**
	 * このレイアウトがシングルプレー用かどうかを判定します。
	 * @return このレイアウトがシングルプレー用ならtrue
	 */
	public final boolean isSinglePlayLayout() {
		return (mSecondary == null);
	}

	/**
	 * このレイアウトがダブルプレー用かどうかを判定します。
	 * @return このレイアウトがダブルプレー用ならtrue
	 */
	public final boolean isDoublePlayLayout() {
		return (mSecondary != null);
	}

	/**
	 * 主レーンと副レーンが入れ替わった状態(FLIP)であるかを判定します。
	 * <p>当メソッドはシングルプレー用レイアウトでは必ずfalseを返します。</p>
	 * @return このレイアウトがFLIPの場合true
	 */
	public final boolean isFlip() {
		return mIsFlip;
	}

	/**
	 * レイアウトパターン検査
	 * @param pattern 検査対象レイアウトパターン
	 * @param chars 使用可能文字一覧
	 * @return 検査合格時にtrue、不合格時にfalse
	 */
	private static boolean testPattern(String pattern, List<Character> chars) {
		var unused = new ArrayList<>(chars);
		var matched = pattern.chars().allMatch(c -> unused.remove(Character.valueOf((char)c)));
		return matched && (unused.isEmpty() || ((unused.size() == 1) && (unused.get(0) == 'S')));
	}

	/**
	 * ランダムなレイアウトパターン生成
	 * @param sb 作業用StringBuilder(初回呼び出し時は空のオブジェクトを指定すること)
	 * @param remain 作業用残り使用文字(初回呼び出し時は全文字の入った編集可能リストを指定すること)
	 * @return ランダムなレイアウトパターン文字列
	 */
	private static String generateRandom(StringBuilder sb, List<Character> remain) {
		if (remain.size() == 0) {
			return sb.toString();
		} else {
			sb.append(remain.remove(RANDOM_OBJECT.nextInt(remain.size())));
			return generateRandom(sb, remain);
		}
	}

	/**
	 * 全レイアウトパターン生成
	 * @param outList レイアウトパターン格納先リスト
	 * @param sb 作業用StringBuilder
	 * @param remain 作業用残り使用文字
	 */
	private static void generatePattern(List<String> outList, StringBuilder sb, List<Character> remain) {
		var count = remain.size();
		for (var i = 0; i < count; i++) {
			var newRemain = new ArrayList<>(remain);
			var newSb = new StringBuilder(sb.toString()).append(newRemain.remove(i));
			if (newRemain.isEmpty()) {
				outList.add(newSb.toString());
			} else {
				generatePattern(outList, newSb, newRemain);
			}
		}
	}

	/**
	 * ローテートレイアウトパターン生成
	 * @param orgPattern ソースレイアウトパターン(この配置と、この配置のミラー配置は出力対象外)
	 * @return ローテートレイアウトパターン
	 */
	private static List<String> generateRotatePatterns(List<Character> orgPattern) {
		var mirrorPattern = new ArrayList<>(orgPattern);
		Collections.reverse(mirrorPattern);
		var patterns = List.of(orgPattern, mirrorPattern);
		var outList = new ArrayList<String>(12);
		for (var pattern : patterns) {
			var rotate = new ArrayList<>(pattern);
			for (var i = 0; i < 6; i++) {
				var sb = new StringBuilder();
				Collections.rotate(rotate, 1);
				rotate.forEach(c -> sb.append(c));
				outList.add(sb.toString());
			}
		}
		return outList;
	}
}
