package com.lmt.lib.bms.bemusic;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

public class BeMusicNoteLayoutTest {
	// BeMusicNoteLayout(String)
	// 正常：スイッチのみのレイアウト変更が行えること
	@Test
	public void testBeMusicNoteLayoutString_SwitchOnly() {
		var pattern = "3712654";
		var layout = new BeMusicNoteLayout(pattern);
		assertEquals(pattern, layout.getPrimaryPattern());
	}

	// BeMusicNoteLayout(String)
	// 正常：スクラッチを含めたレイアウト変更が行えること
	@Test
	public void testBeMusicNoteLayoutString_WithScratch() {
		var pattern = "2S647513";
		var layout = new BeMusicNoteLayout(pattern);
		assertEquals(pattern, layout.getPrimaryPattern());
	}

	// BeMusicNoteLayout(String)
	// NullPointerException layoutがnull
	@Test
	public void testBeMusicNoteLayoutString_NullLayout() {
		assertThrows(NullPointerException.class, () -> new BeMusicNoteLayout(null));
	}

	// BeMusicNoteLayout(String)
	// IllegalArgumentException スイッチの数が不足している
	@Test
	public void testBeMusicNoteLayoutString_MissingSwitch() {
		assertThrows(IllegalArgumentException.class, () -> new BeMusicNoteLayout("7165S23"));
	}

	// BeMusicNoteLayout(String)
	// IllegalArgumentException 同じ入力デバイスを2回記述している
	@Test
	public void testBeMusicNoteLayoutString_UsedSameDevice() {
		assertThrows(IllegalArgumentException.class, () -> new BeMusicNoteLayout("1123456"));
	}

	// BeMusicNoteLayout(String)
	// IllegalArgumentException 副レーンのレイアウトを指定している
	@Test
	public void testBeMusicNoteLayoutString_SecondaryLane() {
		assertThrows(IllegalArgumentException.class, () -> new BeMusicNoteLayout("ABCDEFG"));
	}

	// BeMusicNoteLayout(String)
	// IllegalArgumentException 不正な文字を使用している
	@Test
	public void testBeMusicNoteLayoutString_InvalidChar() {
		assertThrows(IllegalArgumentException.class, () -> new BeMusicNoteLayout("a123456"));
	}

	// BeMusicNoteLayout(String, String)
	// 正常：スイッチのみを指定したシングルプレー用レイアウトが正しく生成できること
	@Test
	public void testBeMusicNoteLayoutStringString_SpSwitchOnly() {
		var pattern = "4627315";
		var layout = new BeMusicNoteLayout(pattern, null);
		assertEquals(pattern, layout.getPrimaryPattern());
		assertNull(layout.getSecondaryPattern());
	}

	// BeMusicNoteLayout(String, String)
	// 正常：スクラッチを指定したシングルプレー用レイアウトが正しく生成できること
	@Test
	public void testBeMusicNoteLayoutStringString_SpWithScratch() {
		var pattern = "3746S125";
		var layout = new BeMusicNoteLayout(pattern, null);
		assertEquals(pattern, layout.getPrimaryPattern());
		assertNull(layout.getSecondaryPattern());
	}

	// BeMusicNoteLayout(String, String)
	// 正常：スイッチのみを指定したダブルプレー用レイアウトが正しく生成できること
	@Test
	public void testBeMusicNoteLayoutStringString_DpSwitchOnly() {
		var pattern1 = "7654321";
		var pattern2 = "GFEDCBA";
		var layout = new BeMusicNoteLayout(pattern1, pattern2);
		assertEquals(pattern1, layout.getPrimaryPattern());
		assertEquals(pattern2, layout.getSecondaryPattern());
	}

	// BeMusicNoteLayout(String, String)
	// 正常：スクラッチを指定したダブルプレー用レイアウトが正しく生成できること
	@Test
	public void testBeMusicNoteLayoutStringString_DpWithScratch() {
		var pattern1 = "12S34765";
		var pattern2 = "CBASDEFG";
		var layout = new BeMusicNoteLayout(pattern1, pattern2);
		assertEquals(pattern1, layout.getPrimaryPattern());
		assertEquals(pattern2, layout.getSecondaryPattern());
	}

	// BeMusicNoteLayout(String, String)
	// 正常：主がスイッチのみ、副がスクラッチ指定のダブルプレー用レイアウトが正しく生成できること
	@Test
	public void testBeMusicNoteLayoutStringString_DpSwitchOnlyWithScratch() {
		var pattern1 = "1234567";
		var pattern2 = "ABCDEFGS";
		var layout = new BeMusicNoteLayout(pattern1, pattern2);
		assertEquals(pattern1, layout.getPrimaryPattern());
		assertEquals(pattern2, layout.getSecondaryPattern());
	}

	// BeMusicNoteLayout(String, String)
	// 正常：主がスクラッチ指定、副がスイッチのみのダブルプレー用レイアウトが正しく生成できること
	@Test
	public void testBeMusicNoteLayoutStringString_DpWithScratchSwitchOnly() {
		var pattern1 = "1234567S";
		var pattern2 = "ABCDEFG";
		var layout = new BeMusicNoteLayout(pattern1, pattern2);
		assertEquals(pattern1, layout.getPrimaryPattern());
		assertEquals(pattern2, layout.getSecondaryPattern());
	}

	// BeMusicNoteLayout(String, String)
	// 正常：主に副、副に主のレイアウトを指定したFLIPレイアウトが正しく生成できること
	@Test
	public void testBeMusicNoteLayoutStringString_DpFlipSwitchOnly() {
		var pattern1 = "DACGBFE";
		var pattern2 = "7162534";
		var layout = new BeMusicNoteLayout(pattern1, pattern2);
		assertEquals(pattern1, layout.getPrimaryPattern());
		assertEquals(pattern2, layout.getSecondaryPattern());
	}

	// BeMusicNoteLayout(String, String)
	// 正常：FLIPレイアウトでスクラッチ指定が可能であること
	@Test
	public void testBeMusicNoteLayoutStringString_DpFlipWithScratch() {
		var pattern1 = "DACGBFE";
		var pattern2 = "7162534";
		var layout = new BeMusicNoteLayout(pattern1, pattern2);
		assertEquals(pattern1, layout.getPrimaryPattern());
		assertEquals(pattern2, layout.getSecondaryPattern());
	}

	// BeMusicNoteLayout(String, String)
	// NullPointerException primaryがnull
	@Test
	public void testBeMusicNoteLayoutStringString_NullPrimary() {
		assertThrows(NullPointerException.class, () -> new BeMusicNoteLayout(null, "ABCDEFG"));
	}

	// BeMusicNoteLayout(String, String)
	// IllegalArgumentException 主でスイッチ数が不足している
	@Test
	public void testBeMusicNoteLayoutStringString_PrimaryMissiongSwitch() {
		assertThrows(IllegalArgumentException.class, () -> new BeMusicNoteLayout("123456", "ABCDEFG"));
	}

	// BeMusicNoteLayout(String, String)
	// IllegalArgumentException 主で同じ入力デバイスを2回記述している
	@Test
	public void testBeMusicNoteLayoutStringString_PrimaryUsedSameDevice() {
		assertThrows(IllegalArgumentException.class, () -> new BeMusicNoteLayout("1165432", "ABCDEFG"));
	}

	// BeMusicNoteLayout(String, String)
	// IllegalArgumentException 主で不正な文字を使用している
	@Test
	public void testBeMusicNoteLayoutStringString_PrimaryInvalidChar() {
		assertThrows(IllegalArgumentException.class, () -> new BeMusicNoteLayout("s123456", "ABCDEFG"));
	}

	// BeMusicNoteLayout(String, String)
	// IllegalArgumentException 主で主レーンのレイアウトを指定し、副にも主レーンのレイアウト指定
	@Test
	public void testBeMusicNoteLayoutStringString_DpBothPrimary() {
		assertThrows(IllegalArgumentException.class, () -> new BeMusicNoteLayout("1234567", "7654321"));
	}

	// BeMusicNoteLayout(String, String)
	// IllegalArgumentException 主で副レーンのレイアウトを指定し、副未指定
	@Test
	public void testBeMusicNoteLayoutStringString_DpFlipAndNullSecondary() {
		assertThrows(IllegalArgumentException.class, () -> new BeMusicNoteLayout("ABCDEFG", null));
	}

	// BeMusicNoteLayout(String, String)
	// IllegalArgumentException 主で副レーンのレイアウトを指定し、副にも副レーンのレイアウト指定
	@Test
	public void testBeMusicNoteLayoutStringString_DpBothSecondary() {
		assertThrows(IllegalArgumentException.class, () -> new BeMusicNoteLayout("ABCDEFG", "GFEDCBA"));
	}

	// BeMusicNoteLayout(String, String)
	// IllegalArgumentException 副でスイッチ数が不足している
	@Test
	public void testBeMusicNoteLayoutStringString_SecondaryMissingSwitch() {
		assertThrows(IllegalArgumentException.class, () -> new BeMusicNoteLayout("1234567", "ABCDEF"));
	}

	// BeMusicNoteLayout(String, String)
	// IllegalArgumentException 副で同じ入力デバイスを2回記述している
	@Test
	public void testBeMusicNoteLayoutStringString_SecondaryUsedSameDevice() {
		assertThrows(IllegalArgumentException.class, () -> new BeMusicNoteLayout("1234567", "AABCDEF"));
	}

	// BeMusicNoteLayout(String, String)
	// IllegalArgumentException 副で不正な文字を使用している
	@Test
	public void testBeMusicNoteLayoutStringString_SecondaryInvalidChar() {
		assertThrows(IllegalArgumentException.class, () -> new BeMusicNoteLayout("1234567", "sABCDEFG"));
	}

	// generateAnyPatterns(BeMusicLane)
	// 正常：主レーンのスクラッチ未指定レイアウトが生成され、3回連続で同じパターンが出現しないこと
	@Test
	public void testGenerateAnyPatternsBeMusicLane_Primary() {
		var same = 0;
		var prev = "";
		for (var i = 0; i < 100; i++) {
			var chars = new ArrayList<>(List.of('1', '2', '3', '4', '5', '6', '7'));
			var cur = BeMusicNoteLayout.generateAnyPatterns(BeMusicLane.PRIMARY);
			same = cur.equals(prev) ? (same + 1) : 0;
			assertTrue("Primary random quality is too low.", same < 3);
			assertEquals(7, cur.length());
			assertTrue(cur.chars().allMatch(c -> chars.remove(Character.valueOf((char)c))));
			prev = cur;
		}
	}

	// generateAnyPatterns(BeMusicLane)
	// 正常：副レーンのスクラッチ未指定レイアウトが生成され、3回連続で同じパターンが出現しないこと
	@Test
	public void testGenerateAnyPatternsBeMusicLane_Secondary() {
		var same = 0;
		var prev = "";
		for (var i = 0; i < 100; i++) {
			var chars = new ArrayList<>(List.of('A', 'B', 'C', 'D', 'E', 'F', 'G'));
			var cur = BeMusicNoteLayout.generateAnyPatterns(BeMusicLane.SECONDARY);
			same = cur.equals(prev) ? (same + 1) : 0;
			assertTrue("Secondary random quality is too low.", same < 3);
			assertEquals(7, cur.length());
			assertTrue(cur.chars().allMatch(c -> chars.remove(Character.valueOf((char)c))));
			prev = cur;
		}
	}

	// generateAnyPatterns(BeMusicLane)
	// NullPointerException laneがnull
	@Test
	public void testGenerateAnyPatternsBeMusicLane_NullLane() {
		assertThrows(NullPointerException.class, () -> BeMusicNoteLayout.generateAnyPatterns(null));
	}

	// generateAllPatterns(BeMusicLane)
	// 正常：5040種類のスクラッチ未指定の主レーンレイアウトが生成されること
	@Test
	public void testGenerateAllPatternsBeMusicLane_Primary() {
		var checked = new HashSet<String>();
		var list = BeMusicNoteLayout.generateAllPatterns(BeMusicLane.PRIMARY);
		assertEquals(5040, list.size());
		for (var pattern : list) {
			var chars = new ArrayList<>(List.of('1', '2', '3', '4', '5', '6', '7'));
			assertEquals(7, pattern.length());
			assertTrue(checked.add(pattern));
			assertTrue(pattern.chars().allMatch(c -> chars.remove(Character.valueOf((char)c))));
		}
	}

	// generateAllPatterns(BeMusicLane)
	// 正常：5040種類のスクラッチ未指定の副レーンレイアウトが生成されること
	@Test
	public void testGenerateAllPatternsBeMusicLane_Secondary() {
		var checked = new HashSet<String>();
		var list = BeMusicNoteLayout.generateAllPatterns(BeMusicLane.SECONDARY);
		assertEquals(5040, list.size());
		for (var pattern : list) {
			var chars = new ArrayList<>(List.of('A', 'B', 'C', 'D', 'E', 'F', 'G'));
			assertEquals(7, pattern.length());
			assertTrue(checked.add(pattern));
			assertTrue(pattern.chars().allMatch(c -> chars.remove(Character.valueOf((char)c))));
		}
	}

	// generateAllPatterns(BeMusicLane)
	// NullPointerException laneがnull
	@Test
	public void testGenerateAllPatternsBeMusicLane_NullLane() {
		assertThrows(NullPointerException.class, () -> BeMusicNoteLayout.generateAllPatterns(null));
	}

	// generateAllRotatePatterns(BeMusicLane)
	// 正常：正規・ミラーを除く12種類のスクラッチ未指定主レーンレイアウトが生成されること
	@Test
	public void testGenerateAllRotatePatternsBeMusicLane_Primary() {
		var expected = new HashSet<>(Set.of(
				"7123456", "6712345", "5671234", "4567123", "3456712", "2345671",
				"1765432", "2176543", "3217654", "4321765", "5432176", "6543217"));
		var list = BeMusicNoteLayout.generateAllRotatePatterns(BeMusicLane.PRIMARY);
		list.forEach(p -> expected.remove(p));
		assertEquals(0, expected.size());
	}

	// generateAllRotatePatterns(BeMusicLane)
	// 正常：正規・ミラーを除く12種類のスクラッチ未指定副レーンレイアウトが生成されること
	@Test
	public void testGenerateAllRotatePatternsBeMusicLane_Secondary() {
		var expected = new HashSet<>(Set.of(
				"GABCDEF", "FGABCDE", "EFGABCD", "DEFGABC", "CDEFGAB", "BCDEFGA",
				"AGFEDCB", "BAGFEDC", "CBAGFED", "DCBAGFE", "EDCBAGF", "FEDCBAG"));
		var list = BeMusicNoteLayout.generateAllRotatePatterns(BeMusicLane.SECONDARY);
		list.forEach(p -> expected.remove(p));
		assertEquals(0, expected.size());
	}

	// generateAllRotatePatterns(BeMusicLane)
	// NullPointerException laneがnull
	@Test
	public void testGenerateAllRotatePatternsBeMusicLane_NullLane() {
		assertThrows(NullPointerException.class, () -> BeMusicNoteLayout.generateAllRotatePatterns(null));
	}

	// equals(Object)
	// 正常：シングルプレー同士でレイアウトが同一時はtrueが返ること
	@Test
	public void testEqualsObject_SingleEqual() {
		var layout1 = new BeMusicNoteLayout("3152746");
		var layout2 = new BeMusicNoteLayout("3152746");
		assertTrue(layout1.equals(layout2));
		layout1 = new BeMusicNoteLayout("712S6354");
		layout2 = new BeMusicNoteLayout("712S6354");
		assertTrue(layout1.equals(layout2));
	}

	// equals(Object)
	// 正常：シングルプレー同士でレイアウトが異なる時はfalseが返ること
	@Test
	public void testEqualsObject_SingleNotEqual() {
		var layout1 = new BeMusicNoteLayout("7654123");
		var layout2 = new BeMusicNoteLayout("1234567");
		assertFalse(layout1.equals(layout2));
	}

	// equals(Object)
	// 正常：シングルプレー同士でスイッチのレイアウトが同一で片方がスクラッチ指定、もう片方が未指定だとfalseが返ること
	@Test
	public void testEqualsObject_SingleSwitchOnlyAndWithScratch() {
		var layout1 = new BeMusicNoteLayout("7654321");
		var layout2 = new BeMusicNoteLayout("7654321S");
		assertFalse(layout1.equals(layout2));
	}

	// equals(Object)
	// 正常：シングルプレーとダブルプレーのレイアウトでは、主レーンのレイアウトが同一でもfalseが返ること
	@Test
	public void testEqualsObject_SingleAndDouble() {
		var layout1 = new BeMusicNoteLayout("1234567");
		var layout2 = new BeMusicNoteLayout("1234567", "ABCDEFG");
		assertFalse(layout1.equals(layout2));
	}

	// equals(Object)
	// 正常：ダブルプレー同士でレイアウトが同一時はtrueが返ること
	@Test
	public void testEqualsObject_DoubleEqual() {
		var layout1 = new BeMusicNoteLayout("6712453", "BADCFEG");
		var layout2 = new BeMusicNoteLayout("6712453", "BADCFEG");
		assertTrue(layout1.equals(layout2));
	}

	// equals(Object)
	// 正常：ダブルプレー同士でレイアウトが異なる時はfalseが返ること
	@Test
	public void testEqualsObject_DoubleNotEqual() {
		var layout1 = new BeMusicNoteLayout("6712453", "BADCFEG");
		var layout2 = new BeMusicNoteLayout("6712453", "ABCDEFG");
		assertFalse(layout1.equals(layout2));
		layout1 = new BeMusicNoteLayout("6712453", "BADCFEG");
		layout2 = new BeMusicNoteLayout("1234567", "BADCFEG");
		assertFalse(layout1.equals(layout2));
	}

	// equals(Object)
	// 正常：ダブルプレー同士でスイッチのレイアウトが同一で片方がスクラッチ指定、もう片方が未指定だとfalseが返ること
	@Test
	public void testEqualsObject_DoubleSwitchOnlyAndWithScratch() {
		var layout1 = new BeMusicNoteLayout("6712453S", "BADCFEG");
		var layout2 = new BeMusicNoteLayout("6712453", "BADCFEG");
		assertFalse(layout1.equals(layout2));
		layout1 = new BeMusicNoteLayout("6712453", "BADCFEGS");
		layout2 = new BeMusicNoteLayout("6712453", "BADCFEG");
		assertFalse(layout1.equals(layout2));
	}

	// equals(Object)
	// 正常：ダブルプレー同士で片方は正規、もう片方がFLIPだとfalseが返ること
	@Test
	public void testEqualsObject_DoubleRegularAndFlip() {
		var layout1 = new BeMusicNoteLayout("6712453", "BADCFEG");
		var layout2 = new BeMusicNoteLayout("BADCFEG", "6712453");
		assertFalse(layout1.equals(layout2));
	}

	// equals(Object)
	// 正常：引数の型が無関係の型だとfalseが返ること
	@SuppressWarnings("unlikely-arg-type")
	@Test
	public void testEqualsObject_OtherType() {
		var layout1 = new BeMusicNoteLayout("1234567");
		assertFalse(layout1.equals("hoge"));
	}

	// equals(Object)
	// 正常：引数がnullだとfalseが返ること
	@Test
	public void testEqualsObject_Null() {
		var layout1 = new BeMusicNoteLayout("1234567");
		assertFalse(layout1.equals(null));
	}

	// toString()
	// 正常：シングルプレースクラッチ指定なしの文字列が返ること
	@Test
	public void testToString_SingleSwitchOnly() {
		var pattern = "1726354";
		var layout = new BeMusicNoteLayout(pattern);
		assertEquals(pattern, layout.toString());
	}

	// toString()
	// 正常：シングルプレースクラッチ指定ありの文字列が返ること
	@Test
	public void testToString_SingleWithScratch() {
		var pattern = "1726S354";
		var layout = new BeMusicNoteLayout(pattern);
		assertEquals(pattern, layout.toString());
	}

	// toString()
	// 正常：ダブルプレースクラッチ指定なしの文字列が返ること
	@Test
	public void testToString_DoubleSwitchOnly() {
		var pattern1 = "1726354";
		var pattern2 = "AGBFCED";
		var string = pattern1 + "/" + pattern2;
		var layout = new BeMusicNoteLayout(pattern1, pattern2);
		assertEquals(string, layout.toString());
	}

	// toString()
	// 正常：ダブルプレースクラッチ指定ありの文字列が返ること
	@Test
	public void testToString_DoubleWithScratch() {
		var pattern1 = "172S6354";
		var pattern2 = "AGBFSCED";
		var string = pattern1 + "/" + pattern2;
		var layout = new BeMusicNoteLayout(pattern1, pattern2);
		assertEquals(string, layout.toString());
	}

	// toString()
	// 正常：ダブルプレーFLIPスクラッチ指定なしの文字列が返ること
	@Test
	public void testToString_DoubleFlipSwitchOnly() {
		var pattern1 = "AGBFCED";
		var pattern2 = "1726354";
		var string = pattern1 + "/" + pattern2;
		var layout = new BeMusicNoteLayout(pattern1, pattern2);
		assertEquals(string, layout.toString());
	}

	// toString()
	// 正常：ダブルプレーFLIPスクラッチ指定ありの文字列が返ること
	@Test
	public void testToString_DoubleFlipWithScratch() {
		var pattern1 = "AGBFCEDS";
		var pattern2 = "S1726354";
		var string = pattern1 + "/" + pattern2;
		var layout = new BeMusicNoteLayout(pattern1, pattern2);
		assertEquals(string, layout.toString());
	}

	// toString()
	// 正常：ダブルプレー片方スクラッチ指定、片方スクラッチ指定ありの文字列が返ること
	@Test
	public void testToString_DoubleSwitchOnlyAndWithScratch() {
		var pattern1 = "AGBFCEDS";
		var pattern2 = "1726354";
		var string = pattern1 + "/" + pattern2;
		var layout = new BeMusicNoteLayout(pattern1, pattern2);
		assertEquals(string, layout.toString());
	}

	// compareTo(BeMusicNoteLayout)
	// 正常：シングルプレー同士でレイアウトが同一時は0が返ること
	@Test
	public void testCompareToBeMusicNoteLayout_SingleEqual() {
		var layout1 = new BeMusicNoteLayout("3217654");
		var layout2 = new BeMusicNoteLayout("3217654");
		assertTrue(layout1.compareTo(layout2) == 0);
		layout1 = new BeMusicNoteLayout("321S7654");
		layout2 = new BeMusicNoteLayout("321S7654");
		assertTrue(layout1.compareTo(layout2) == 0);
	}

	// compareTo(BeMusicNoteLayout)
	// 正常：シングルプレー同士でレイアウトが異なる時は0以外が返ること
	@Test
	public void testCompareToBeMusicNoteLayout_SingleNotEqual() {
		var layout1 = new BeMusicNoteLayout("1234567");
		var layout2 = new BeMusicNoteLayout("7654321");
		assertTrue(layout1.compareTo(layout2) < 0);
		layout1 = new BeMusicNoteLayout("7654321");
		layout2 = new BeMusicNoteLayout("1234567");
		assertTrue(layout1.compareTo(layout2) > 0);
	}

	// compareTo(BeMusicNoteLayout)
	// 正常：シングルプレー同士でスイッチが同一で片方スクラッチ指定だと、スクラッチ指定側が大きくなること
	@Test
	public void testCompareToBeMusicNoteLayout_SwitchSwitchOnlyAndWithScratch() {
		var layout1 = new BeMusicNoteLayout("3217654S");
		var layout2 = new BeMusicNoteLayout("3217654");
		assertTrue(layout1.compareTo(layout2) > 0);
		layout1 = new BeMusicNoteLayout("3217654");
		layout2 = new BeMusicNoteLayout("3217654S");
		assertTrue(layout1.compareTo(layout2) < 0);
	}

	// compareTo(BeMusicNoteLayout)
	// 正常：シングルプレーとダブルプレー比較で主が同一時、シングルプレーが小さくなること
	@Test
	public void testCompareToBeMusicNoteLayout_SingleAndDoubleEqualPrimary() {
		var layout1 = new BeMusicNoteLayout("3217654");
		var layout2 = new BeMusicNoteLayout("3217654", "ABCDEFG");
		assertTrue(layout1.compareTo(layout2) < 0);
		layout1 = new BeMusicNoteLayout("3217654", "ABCDEFG");
		layout2 = new BeMusicNoteLayout("3217654");
		assertTrue(layout1.compareTo(layout2) > 0);
	}

	// compareTo(BeMusicNoteLayout)
	// 正常：シングルプレーとダブルプレー比較で主が異なる時、シングルプレーが大きいほうが大きくなること
	@Test
	public void testCompareToBeMusicNoteLayout_SingleAndDoubleNotEqualPrimary() {
		var layout1 = new BeMusicNoteLayout("3217645");
		var layout2 = new BeMusicNoteLayout("3217654", "ABCDEFG");
		assertTrue(layout1.compareTo(layout2) < 0);
		layout1 = new BeMusicNoteLayout("3217654");
		layout2 = new BeMusicNoteLayout("3217645", "ABCDEFG");
		assertTrue(layout1.compareTo(layout2) > 0);
	}

	// compareTo(BeMusicNoteLayout)
	// 正常：ダブルプレー同士でレイアウトが同一時は0が返ること
	@Test
	public void testCompareToBeMusicNoteLayout_DoubleEqual() {
		var layout1 = new BeMusicNoteLayout("3217645", "EFGDABC");
		var layout2 = new BeMusicNoteLayout("3217645", "EFGDABC");
		assertTrue(layout1.compareTo(layout2) == 0);
	}

	// compareTo(BeMusicNoteLayout)
	// 正常：ダブルプレー同士で主が異なり、副同一時は主の文字列比較結果が返ること
	@Test
	public void testCompareToBeMusicNoteLayout_DoubleDifferentPrimary() {
		var layout1 = new BeMusicNoteLayout("3217645", "EFGDABC");
		var layout2 = new BeMusicNoteLayout("3217654", "EFGDABC");
		assertTrue(layout1.compareTo(layout2) < 0);
		layout1 = new BeMusicNoteLayout("3217654", "EFGDABC");
		layout2 = new BeMusicNoteLayout("3217645", "EFGDABC");
		assertTrue(layout1.compareTo(layout2) > 0);
	}

	// compareTo(BeMusicNoteLayout)
	// 正常：ダブルプレー同士で主同一、副が異なる時は副の文字列比較結果が返ること
	@Test
	public void testCompareToBeMusicNoteLayout_DoubleDifferentSecondary() {
		var layout1 = new BeMusicNoteLayout("3217645", "EFGDACB");
		var layout2 = new BeMusicNoteLayout("3217645", "EFGDABC");
		assertTrue(layout1.compareTo(layout2) > 0);
		layout1 = new BeMusicNoteLayout("3217645", "EFGDABC");
		layout2 = new BeMusicNoteLayout("3217645", "EFGDACB");
		assertTrue(layout1.compareTo(layout2) < 0);
	}

	// compareTo(BeMusicNoteLayout)
	// 正常：ダブルプレー同士でスイッチが同一で片方スクラッチ指定だと、スクラッチ指定側が大きくなること
	@Test
	public void testCompareToBeMusicNoteLayout_DoubleSwitchOnlyAndWithScratch() {
		var layout1 = new BeMusicNoteLayout("3217645", "EFGDABCS");
		var layout2 = new BeMusicNoteLayout("3217645", "EFGDABC");
		assertTrue(layout1.compareTo(layout2) > 0);
		layout1 = new BeMusicNoteLayout("3217645", "EFGDABC");
		layout2 = new BeMusicNoteLayout("3217645", "EFGDABCS");
		assertTrue(layout1.compareTo(layout2) < 0);
		layout1 = new BeMusicNoteLayout("3217645S", "EFGDABC");
		layout2 = new BeMusicNoteLayout("3217645", "EFGDABC");
		assertTrue(layout1.compareTo(layout2) > 0);
		layout1 = new BeMusicNoteLayout("3217645", "EFGDABC");
		layout2 = new BeMusicNoteLayout("3217645S", "EFGDABC");
		assertTrue(layout1.compareTo(layout2) < 0);
	}

	// compareTo(BeMusicNoteLayout)
	// 正常：ダブルプレー同士で片方正規、もう片方正規FLIPだと、FLIPが大きくなること
	@Test
	public void testCompareToBeMusicNoteLayout_DoubleRegularAndFlip() {
		var layout1 = new BeMusicNoteLayout("1234567", "ABCDEFG");
		var layout2 = new BeMusicNoteLayout("ABCDEFG", "1234567");
		assertTrue(layout1.compareTo(layout2) < 0);
		layout1 = new BeMusicNoteLayout("ABCDEFG", "1234567");
		layout2 = new BeMusicNoteLayout("1234567", "ABCDEFG");
		assertTrue(layout1.compareTo(layout2) > 0);
	}

	// compareTo(BeMusicNoteLayout)
	// NullPointerException oがnull
	@Test
	public void testCompareToBeMusicNoteLayout_NullTarget() {
		var layout = new BeMusicNoteLayout("1234567");
		assertThrows(NullPointerException.class, () -> layout.compareTo(null));
	}

	// get(BeMusicDevice)
	// 正常：シングルプレー、スイッチのみ変更レイアウトで期待通りの変更結果になること(副はレイアウト変更なし)
	@Test
	public void testGetBeMusicDevice_SingleSwitchOnly() {
		var layout = new BeMusicNoteLayout("6371524");
		assertEquals(BeMusicDevice.SWITCH16, layout.get(BeMusicDevice.SWITCH11));
		assertEquals(BeMusicDevice.SWITCH13, layout.get(BeMusicDevice.SWITCH12));
		assertEquals(BeMusicDevice.SWITCH17, layout.get(BeMusicDevice.SWITCH13));
		assertEquals(BeMusicDevice.SWITCH11, layout.get(BeMusicDevice.SWITCH14));
		assertEquals(BeMusicDevice.SWITCH15, layout.get(BeMusicDevice.SWITCH15));
		assertEquals(BeMusicDevice.SWITCH12, layout.get(BeMusicDevice.SWITCH16));
		assertEquals(BeMusicDevice.SWITCH14, layout.get(BeMusicDevice.SWITCH17));
		assertEquals(BeMusicDevice.SCRATCH1, layout.get(BeMusicDevice.SCRATCH1));
		assertEquals(BeMusicDevice.SWITCH21, layout.get(BeMusicDevice.SWITCH21));
		assertEquals(BeMusicDevice.SWITCH22, layout.get(BeMusicDevice.SWITCH22));
		assertEquals(BeMusicDevice.SWITCH23, layout.get(BeMusicDevice.SWITCH23));
		assertEquals(BeMusicDevice.SWITCH24, layout.get(BeMusicDevice.SWITCH24));
		assertEquals(BeMusicDevice.SWITCH25, layout.get(BeMusicDevice.SWITCH25));
		assertEquals(BeMusicDevice.SWITCH26, layout.get(BeMusicDevice.SWITCH26));
		assertEquals(BeMusicDevice.SWITCH27, layout.get(BeMusicDevice.SWITCH27));
		assertEquals(BeMusicDevice.SCRATCH2, layout.get(BeMusicDevice.SCRATCH2));
	}

	// get(BeMusicDevice)
	// 正常：シングルプレー、スクラッチ指定ありレイアウトで期待通りの変更結果になること(副はレイアウト変更なし)
	@Test
	public void testGetBeMusicDevice_SingleWithScratch() {
		var layout = new BeMusicNoteLayout("312S4657");
		assertEquals(BeMusicDevice.SWITCH13, layout.get(BeMusicDevice.SWITCH11));
		assertEquals(BeMusicDevice.SWITCH11, layout.get(BeMusicDevice.SWITCH12));
		assertEquals(BeMusicDevice.SWITCH12, layout.get(BeMusicDevice.SWITCH13));
		assertEquals(BeMusicDevice.SCRATCH1, layout.get(BeMusicDevice.SWITCH14));
		assertEquals(BeMusicDevice.SWITCH14, layout.get(BeMusicDevice.SWITCH15));
		assertEquals(BeMusicDevice.SWITCH16, layout.get(BeMusicDevice.SWITCH16));
		assertEquals(BeMusicDevice.SWITCH15, layout.get(BeMusicDevice.SWITCH17));
		assertEquals(BeMusicDevice.SWITCH17, layout.get(BeMusicDevice.SCRATCH1));
		assertEquals(BeMusicDevice.SWITCH21, layout.get(BeMusicDevice.SWITCH21));
		assertEquals(BeMusicDevice.SWITCH22, layout.get(BeMusicDevice.SWITCH22));
		assertEquals(BeMusicDevice.SWITCH23, layout.get(BeMusicDevice.SWITCH23));
		assertEquals(BeMusicDevice.SWITCH24, layout.get(BeMusicDevice.SWITCH24));
		assertEquals(BeMusicDevice.SWITCH25, layout.get(BeMusicDevice.SWITCH25));
		assertEquals(BeMusicDevice.SWITCH26, layout.get(BeMusicDevice.SWITCH26));
		assertEquals(BeMusicDevice.SWITCH27, layout.get(BeMusicDevice.SWITCH27));
		assertEquals(BeMusicDevice.SCRATCH2, layout.get(BeMusicDevice.SCRATCH2));
	}

	// get(BeMusicDevice)
	// 正常：ダブルプレー、スイッチのみ変更レイアウトで期待通りの変更結果になること
	@Test
	public void testGetBeMusicDevice_DoubleSwitchOnly() {
		var layout = new BeMusicNoteLayout("6371524", "DGEFACB");
		assertEquals(BeMusicDevice.SWITCH16, layout.get(BeMusicDevice.SWITCH11));
		assertEquals(BeMusicDevice.SWITCH13, layout.get(BeMusicDevice.SWITCH12));
		assertEquals(BeMusicDevice.SWITCH17, layout.get(BeMusicDevice.SWITCH13));
		assertEquals(BeMusicDevice.SWITCH11, layout.get(BeMusicDevice.SWITCH14));
		assertEquals(BeMusicDevice.SWITCH15, layout.get(BeMusicDevice.SWITCH15));
		assertEquals(BeMusicDevice.SWITCH12, layout.get(BeMusicDevice.SWITCH16));
		assertEquals(BeMusicDevice.SWITCH14, layout.get(BeMusicDevice.SWITCH17));
		assertEquals(BeMusicDevice.SCRATCH1, layout.get(BeMusicDevice.SCRATCH1));
		assertEquals(BeMusicDevice.SWITCH24, layout.get(BeMusicDevice.SWITCH21));
		assertEquals(BeMusicDevice.SWITCH27, layout.get(BeMusicDevice.SWITCH22));
		assertEquals(BeMusicDevice.SWITCH25, layout.get(BeMusicDevice.SWITCH23));
		assertEquals(BeMusicDevice.SWITCH26, layout.get(BeMusicDevice.SWITCH24));
		assertEquals(BeMusicDevice.SWITCH21, layout.get(BeMusicDevice.SWITCH25));
		assertEquals(BeMusicDevice.SWITCH23, layout.get(BeMusicDevice.SWITCH26));
		assertEquals(BeMusicDevice.SWITCH22, layout.get(BeMusicDevice.SWITCH27));
		assertEquals(BeMusicDevice.SCRATCH2, layout.get(BeMusicDevice.SCRATCH2));
	}

	// get(BeMusicDevice)
	// 正常：ダブルプレー、スクラッチ指定ありレイアウトで期待通りの変更結果になること
	@Test
	public void testGetBeMusicDevice_DoubleWithScratch() {
		var layout = new BeMusicNoteLayout("312S4657", "SGDFAECB");
		assertEquals(BeMusicDevice.SWITCH13, layout.get(BeMusicDevice.SWITCH11));
		assertEquals(BeMusicDevice.SWITCH11, layout.get(BeMusicDevice.SWITCH12));
		assertEquals(BeMusicDevice.SWITCH12, layout.get(BeMusicDevice.SWITCH13));
		assertEquals(BeMusicDevice.SCRATCH1, layout.get(BeMusicDevice.SWITCH14));
		assertEquals(BeMusicDevice.SWITCH14, layout.get(BeMusicDevice.SWITCH15));
		assertEquals(BeMusicDevice.SWITCH16, layout.get(BeMusicDevice.SWITCH16));
		assertEquals(BeMusicDevice.SWITCH15, layout.get(BeMusicDevice.SWITCH17));
		assertEquals(BeMusicDevice.SWITCH17, layout.get(BeMusicDevice.SCRATCH1));
		assertEquals(BeMusicDevice.SCRATCH2, layout.get(BeMusicDevice.SWITCH21));
		assertEquals(BeMusicDevice.SWITCH27, layout.get(BeMusicDevice.SWITCH22));
		assertEquals(BeMusicDevice.SWITCH24, layout.get(BeMusicDevice.SWITCH23));
		assertEquals(BeMusicDevice.SWITCH26, layout.get(BeMusicDevice.SWITCH24));
		assertEquals(BeMusicDevice.SWITCH21, layout.get(BeMusicDevice.SWITCH25));
		assertEquals(BeMusicDevice.SWITCH25, layout.get(BeMusicDevice.SWITCH26));
		assertEquals(BeMusicDevice.SWITCH23, layout.get(BeMusicDevice.SWITCH27));
		assertEquals(BeMusicDevice.SWITCH22, layout.get(BeMusicDevice.SCRATCH2));
	}

	// get(BeMusicDevice)
	// 正常：ダブルプレー、FLIPレイアウトで期待通りの変更結果になること
	@Test
	public void testGetBeMusicDevice_DoubleFlip() {
		var layout = new BeMusicNoteLayout("SGDFAECB", "312S4657");
		assertEquals(BeMusicDevice.SCRATCH2, layout.get(BeMusicDevice.SWITCH11));
		assertEquals(BeMusicDevice.SWITCH27, layout.get(BeMusicDevice.SWITCH12));
		assertEquals(BeMusicDevice.SWITCH24, layout.get(BeMusicDevice.SWITCH13));
		assertEquals(BeMusicDevice.SWITCH26, layout.get(BeMusicDevice.SWITCH14));
		assertEquals(BeMusicDevice.SWITCH21, layout.get(BeMusicDevice.SWITCH15));
		assertEquals(BeMusicDevice.SWITCH25, layout.get(BeMusicDevice.SWITCH16));
		assertEquals(BeMusicDevice.SWITCH23, layout.get(BeMusicDevice.SWITCH17));
		assertEquals(BeMusicDevice.SWITCH22, layout.get(BeMusicDevice.SCRATCH1));
		assertEquals(BeMusicDevice.SWITCH13, layout.get(BeMusicDevice.SWITCH21));
		assertEquals(BeMusicDevice.SWITCH11, layout.get(BeMusicDevice.SWITCH22));
		assertEquals(BeMusicDevice.SWITCH12, layout.get(BeMusicDevice.SWITCH23));
		assertEquals(BeMusicDevice.SCRATCH1, layout.get(BeMusicDevice.SWITCH24));
		assertEquals(BeMusicDevice.SWITCH14, layout.get(BeMusicDevice.SWITCH25));
		assertEquals(BeMusicDevice.SWITCH16, layout.get(BeMusicDevice.SWITCH26));
		assertEquals(BeMusicDevice.SWITCH15, layout.get(BeMusicDevice.SWITCH27));
		assertEquals(BeMusicDevice.SWITCH17, layout.get(BeMusicDevice.SCRATCH2));
	}

	// get(BeMusicDevice)
	// NullPointerException beforeがnull
	@Test
	public void testGetBeMusicDevice_NullBefore() {
		var layout = BeMusicNoteLayout.SP_REGULAR;
		assertThrows(NullPointerException.class, () -> layout.get(null));
	}

	// getPrimaryPattern()
	// 正常：オブジェクト構築時に指定したパターン文字列が返ること
	@Test
	public void testGetPrimaryPattern() {
		var pattern = "4137265";
		var layout = new BeMusicNoteLayout(pattern);
		assertEquals(pattern, layout.getPrimaryPattern());
		pattern = "4137S265";
		layout = new BeMusicNoteLayout(pattern);
		assertEquals(pattern, layout.getPrimaryPattern());
		pattern = "7162534";
		layout = new BeMusicNoteLayout(pattern, "ABCDEFG");
		assertEquals(pattern, layout.getPrimaryPattern());
		pattern = "CGBFEAD";
		layout = new BeMusicNoteLayout(pattern, "1234567");
		assertEquals(pattern, layout.getPrimaryPattern());
	}

	// getPrimaryPattern()
	// 正常：シングルプレーレイアウトではnullが返ること
	@Test
	public void testGetSecondaryPattern_Single() {
		var layout = new BeMusicNoteLayout("1234567");
		assertNull(layout.getSecondaryPattern());
		layout = new BeMusicNoteLayout("1234567", null);
		assertNull(layout.getSecondaryPattern());
	}

	// getSecondaryPattern()
	// 正常：オブジェクト構築時に指定したパターンが返ること
	@Test
	public void testGetSecondaryPattern_Double() {
		var pattern = "GAFBECD";
		var layout = new BeMusicNoteLayout("1234567", pattern);
		assertEquals(pattern, layout.getSecondaryPattern());
		pattern = "GAFBSECD";
		layout = new BeMusicNoteLayout("1234567", pattern);
		assertEquals(pattern, layout.getSecondaryPattern());
		pattern = "7162534";
		layout = new BeMusicNoteLayout("ABCDEFG", pattern);
		assertEquals(pattern, layout.getSecondaryPattern());
		pattern = "71S62534";
		layout = new BeMusicNoteLayout("ABCDEFG", pattern);
		assertEquals(pattern, layout.getSecondaryPattern());
	}

	// isSinglePlayLayout()
	// 正常：シングルプレーレイアウトではtrueが返ること
	@Test
	public void testIsSinglePlayLayout_Single() {
		var layout = new BeMusicNoteLayout("1234567");
		assertTrue(layout.isSinglePlayLayout());
		layout = new BeMusicNoteLayout("1234567", null);
		assertTrue(layout.isSinglePlayLayout());
	}

	// isSinglePlayLayout()
	// 正常：ダブルプレーレイアウトではfalseが返ること
	@Test
	public void testIsSinglePlayLayout_Double() {
		var layout = new BeMusicNoteLayout("1234567", "ABCDEFG");
		assertFalse(layout.isSinglePlayLayout());
		layout = new BeMusicNoteLayout("1234567S", "ABCDEFG");
		assertFalse(layout.isSinglePlayLayout());
		layout = new BeMusicNoteLayout("1234567", "ABCDEFGS");
		assertFalse(layout.isSinglePlayLayout());
	}

	// isDoublePlayLayout()
	// 正常：シングルプレーレイアウトではfalseが返ること
	@Test
	public void testIsDoublePlayLayout_Single() {
		var layout = new BeMusicNoteLayout("1234567");
		assertFalse(layout.isDoublePlayLayout());
		layout = new BeMusicNoteLayout("1234567", null);
		assertFalse(layout.isDoublePlayLayout());
		layout = new BeMusicNoteLayout("1234567S", null);
		assertFalse(layout.isDoublePlayLayout());
	}

	// isDoublePlayLayout()
	// 正常：ダブルプレーレイアウトではtrueが返ること
	@Test
	public void testIsDoublePlayLayout_Double() {
		var layout = new BeMusicNoteLayout("1234567", "ABCDEFG");
		assertTrue(layout.isDoublePlayLayout());
		layout = new BeMusicNoteLayout("1234567S", "ABCDEFG");
		assertTrue(layout.isDoublePlayLayout());
		layout = new BeMusicNoteLayout("1234567", "ABCDEFGS");
		assertTrue(layout.isDoublePlayLayout());
	}

	// isFlip()
	// 正常：シングルプレーレイアウトではfalseが返ること
	@Test
	public void testIsFlip_Single() {
		var layout = new BeMusicNoteLayout("1234567");
		assertFalse(layout.isFlip());
		layout = new BeMusicNoteLayout("1234567S");
		assertFalse(layout.isFlip());
	}

	// isFlip()
	// 正常：ダブルプレーレイアウトでもFLIPなしではfalseが返ること
	@Test
	public void testIsFlip_Double() {
		var layout = new BeMusicNoteLayout("1234567", "ABCDEFG");
		assertFalse(layout.isFlip());
		layout = new BeMusicNoteLayout("1234567", "ABCDEFGS");
		assertFalse(layout.isFlip());
	}

	// isFlip()
	// 正常：ダブルプレーレイアウトでFLIPありでtrueが返ること
	@Test
	public void testIsFlip_DoubleFlip() {
		var layout = new BeMusicNoteLayout("ABCDEFG", "1234567");
		assertTrue(layout.isFlip());
		layout = new BeMusicNoteLayout("ABCDEFGS", "1234567");
		assertTrue(layout.isFlip());
	}
}
