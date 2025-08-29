package com.lmt.lib.bms.bemusic;

import static org.junit.Assert.*;

import java.util.Map;
import java.util.function.Predicate;

import org.junit.Test;

public class BeMusicNoteTypeTest {
	// getId()
	// 正常：ID値は将来に渡って拡張が行われたとしても値が変化しないこと
	// 注意！このテストコードは一度実装してOKなった後は変更してはならない(値が変化しないことが絶対的に求められるため)
	@Test
	public void testGetId_Normal() {
		assertEquals(0, BeMusicNoteType.NONE.getId());
		assertEquals(1, BeMusicNoteType.BEAT.getId());
		assertEquals(2, BeMusicNoteType.LONG.getId());
		assertEquals(3, BeMusicNoteType.LONG_ON.getId());
		assertEquals(4, BeMusicNoteType.LONG_OFF.getId());
		assertEquals(5, BeMusicNoteType.CHARGE_OFF.getId());
		assertEquals(6, BeMusicNoteType.MINE.getId());
	}

	// isCountNotes()
	// 正常：短押し、長押し開始、CN系長押し解放がカウントされること
	@Test
	public void testIsCountNotes_Normal_IsCount() {
		assertTrue(BeMusicNoteType.BEAT.isCountNotes());
		assertTrue(BeMusicNoteType.LONG_ON.isCountNotes());
		assertTrue(BeMusicNoteType.CHARGE_OFF.isCountNotes());
	}

	// isCountNotes()
	// 正常：短押し、長押し以外はカウントされないこと
	@Test
	public void testIsCountNotes_Normal_NotCount() {
		assertFalse(BeMusicNoteType.NONE.isCountNotes());
		assertFalse(BeMusicNoteType.LONG.isCountNotes());
		assertFalse(BeMusicNoteType.LONG_OFF.isCountNotes());
		assertFalse(BeMusicNoteType.MINE.isCountNotes());
	}

	// fromId(int)
	// 正常：IDに対応したノート種別が取得できること
	// 注意！このテストコードは一度実装してOKなった後は変更してはならない(値が変化しないことが絶対的に求められるため)
	@Test
	public void testFromId_Normal() {
		assertEquals(BeMusicNoteType.NONE, BeMusicNoteType.fromId(0));
		assertEquals(BeMusicNoteType.BEAT, BeMusicNoteType.fromId(1));
		assertEquals(BeMusicNoteType.LONG, BeMusicNoteType.fromId(2));
		assertEquals(BeMusicNoteType.LONG_ON, BeMusicNoteType.fromId(3));
		assertEquals(BeMusicNoteType.LONG_OFF, BeMusicNoteType.fromId(4));
		assertEquals(BeMusicNoteType.CHARGE_OFF, BeMusicNoteType.fromId(5));
		assertEquals(BeMusicNoteType.MINE, BeMusicNoteType.fromId(6));
	}

	// isHolding()
	// 長押し継続のみがtrueになること
	@Test
	public void testIsHolding() {
		assertFalse(BeMusicNoteType.NONE.isHolding());
		assertFalse(BeMusicNoteType.BEAT.isHolding());
		assertTrue(BeMusicNoteType.LONG.isHolding());
		assertFalse(BeMusicNoteType.LONG_ON.isHolding());
		assertFalse(BeMusicNoteType.LONG_OFF.isHolding());
		assertFalse(BeMusicNoteType.CHARGE_OFF.isHolding());
		assertFalse(BeMusicNoteType.MINE.isHolding());
	}

	// isLongNoteHead()
	// 長押し開始のみがtrueになること
	@Test
	public void testIsLongNoteHead() {
		assertFalse(BeMusicNoteType.NONE.isLongNoteHead());
		assertFalse(BeMusicNoteType.BEAT.isLongNoteHead());
		assertFalse(BeMusicNoteType.LONG.isLongNoteHead());
		assertTrue(BeMusicNoteType.LONG_ON.isLongNoteHead());
		assertFalse(BeMusicNoteType.LONG_OFF.isLongNoteHead());
		assertFalse(BeMusicNoteType.CHARGE_OFF.isLongNoteHead());
		assertFalse(BeMusicNoteType.MINE.isLongNoteHead());
	}

	// isLongNoteTail()
	// 長押し終了のみがtrueになること
	@Test
	public void testIsLongNoteTail() {
		assertFalse(BeMusicNoteType.NONE.isLongNoteTail());
		assertFalse(BeMusicNoteType.BEAT.isLongNoteTail());
		assertFalse(BeMusicNoteType.LONG.isLongNoteTail());
		assertFalse(BeMusicNoteType.LONG_ON.isLongNoteTail());
		assertTrue(BeMusicNoteType.LONG_OFF.isLongNoteTail());
		assertTrue(BeMusicNoteType.CHARGE_OFF.isLongNoteTail());
		assertFalse(BeMusicNoteType.MINE.isLongNoteTail());
	}

	// isLongNoteType()
	// ロングノートに関連する種別がtrueになること
	@Test
	public void testIsLongNoteType() {
		assertFalse(BeMusicNoteType.NONE.isLongNoteType());
		assertFalse(BeMusicNoteType.BEAT.isLongNoteType());
		assertTrue(BeMusicNoteType.LONG.isLongNoteType());
		assertTrue(BeMusicNoteType.LONG_ON.isLongNoteType());
		assertTrue(BeMusicNoteType.LONG_OFF.isLongNoteType());
		assertTrue(BeMusicNoteType.CHARGE_OFF.isLongNoteType());
		assertFalse(BeMusicNoteType.MINE.isLongNoteType());
	}

	// hasVisualEffect()
	// 正常：ノートなし以外は全て視覚効果を持つこと
	@Test
	public void testHasVisualEffect() {
		assertFalse(BeMusicNoteType.NONE.hasVisualEffect());
		assertTrue(BeMusicNoteType.BEAT.hasVisualEffect());
		assertTrue(BeMusicNoteType.LONG.hasVisualEffect());
		assertTrue(BeMusicNoteType.LONG_ON.hasVisualEffect());
		assertTrue(BeMusicNoteType.LONG_OFF.hasVisualEffect());
		assertTrue(BeMusicNoteType.CHARGE_OFF.hasVisualEffect());
		assertTrue(BeMusicNoteType.MINE.hasVisualEffect());
	}

	// hasUpAction()
	// 操作が終了するノート種別でtrueが返り、それ以外はfalseが返ること
	@Test
	public void testHasUpAction() {
		assertFalse(BeMusicNoteType.NONE.hasUpAction());
		assertTrue(BeMusicNoteType.BEAT.hasUpAction());
		assertFalse(BeMusicNoteType.LONG.hasUpAction());
		assertFalse(BeMusicNoteType.LONG_ON.hasUpAction());
		assertTrue(BeMusicNoteType.LONG_OFF.hasUpAction());
		assertTrue(BeMusicNoteType.CHARGE_OFF.hasUpAction());
		assertFalse(BeMusicNoteType.MINE.hasUpAction());
	}

	// hasDownAction()
	// 操作を開始するノート種別でtrueが返り、それ以外はfalseが返ること
	@Test
	public void testHasDownAction() {
		assertFalse(BeMusicNoteType.NONE.hasDownAction());
		assertTrue(BeMusicNoteType.BEAT.hasDownAction());
		assertFalse(BeMusicNoteType.LONG.hasDownAction());
		assertTrue(BeMusicNoteType.LONG_ON.hasDownAction());
		assertFalse(BeMusicNoteType.LONG_OFF.hasDownAction());
		assertFalse(BeMusicNoteType.CHARGE_OFF.hasDownAction());
		assertFalse(BeMusicNoteType.MINE.hasDownAction());
	}

	// hasMovement
	// 何らかの操作変化を伴うノート種別でtrueが返り、それ以外はfalseが返ること
	@Test
	public void testHasMovement() {
		assertFalse(BeMusicNoteType.NONE.hasMovement());
		assertTrue(BeMusicNoteType.BEAT.hasMovement());
		assertFalse(BeMusicNoteType.LONG.hasMovement());
		assertTrue(BeMusicNoteType.LONG_ON.hasMovement());
		assertTrue(BeMusicNoteType.LONG_OFF.hasMovement());
		assertTrue(BeMusicNoteType.CHARGE_OFF.hasMovement());
		assertFalse(BeMusicNoteType.MINE.hasMovement());
	}

	// hasSound(BeMusicDevice)
	// サウンド再生を伴う可能性がある場合trueが返り、それ以外はfalseが返ること
	@Test
	public void testHasSound_Normal() {
		Predicate<BeMusicDevice> allTrue = dev -> true;
		Predicate<BeMusicDevice> allFalse = dev -> false;
		Predicate<BeMusicDevice> scratchOnly = dev -> dev.isScratch();
		var expects = Map.of(
				BeMusicNoteType.NONE, allFalse,
				BeMusicNoteType.BEAT, allTrue,
				BeMusicNoteType.LONG, allFalse,
				BeMusicNoteType.LONG_ON, allTrue,
				BeMusicNoteType.LONG_OFF, allFalse,
				BeMusicNoteType.CHARGE_OFF, scratchOnly,
				BeMusicNoteType.MINE, allFalse);
		for (var noteType : BeMusicNoteType.values()) {
			var fnExpect = expects.get(noteType);
			for (var i = 0; i < BeMusicDevice.COUNT; i++) {
				var device = BeMusicDevice.fromIndex(i);
				assertEquals(fnExpect.test(device), noteType.hasSound(device));
			}
		}
	}

	// hasSound(BeMusicDevice)
	// 入力デバイスを参照するノート種別では、入力デバイスが null の時に NullPointerException がスローされること
	@Test
	public void testHasSound_NullDevice() {
		assertThrows(NullPointerException.class, () -> BeMusicNoteType.CHARGE_OFF.hasSound(null));
	}
}
