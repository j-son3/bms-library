package com.lmt.lib.bms.bemusic;

import static org.junit.Assert.*;

import org.junit.Test;

public class BeMusicSoundTest {
	private static final int MASK_TRACKID = 0x0000ffff;
	private static final int RMASK_TRACKID = ~MASK_TRACKID;
	private static final int MASK_RESTART = 0x00010000;
	private static final int RMASK_RESTART = ~MASK_RESTART;
	private static final int MASK_LNMODE = 0x00060000;
	private static final int RMASK_LNMODE = ~MASK_LNMODE;
	private static final int SHIFT_RESTART = 16;
	private static final int SHIFT_LNMODE = 17;

	// getTrackId(int)
	// トラックIDの最小値～最大値まで取得できること
	@Test
	public void testGetTrackId() {
		var min = BeMusicSound.TRACK_ID_MIN;
		var mid = BeMusicSound.TRACK_ID_MAX / 2;
		var max = BeMusicSound.TRACK_ID_MAX;
		assertEquals(min, BeMusicSound.getTrackId(RMASK_TRACKID | min));
		assertEquals(mid, BeMusicSound.getTrackId(RMASK_TRACKID | mid));
		assertEquals(max, BeMusicSound.getTrackId(RMASK_TRACKID | max));
	}

	// isRestartTrack(int)
	// 対象ビットが0の場合、trueが返ること
	@Test
	public void testIsRestartTrack_True() {
		assertEquals(true, BeMusicSound.isRestartTrack(RMASK_RESTART | (0x00 << SHIFT_RESTART)));
	}

	// isRestartTrack(int)
	// 対象ビットが1の場合、falseが返ること
	@Test
	public void testIsRestartTrack_False() {
		assertEquals(false, BeMusicSound.isRestartTrack(RMASK_RESTART | (0x01 << SHIFT_RESTART)));
	}

	// getLongNoteMode(int)
	// 対象ビットの値が0の場合、LNが返ること
	@Test
	public void testGetLongNoteModeInt_Undefined() {
		assertEquals(BeMusicLongNoteMode.LN, BeMusicSound.getLongNoteMode(RMASK_LNMODE | (0x00 << SHIFT_LNMODE)));
	}

	// getLongNoteMode(int)
	// 対象ビットの値が1の場合、LNが返ること
	@Test
	public void testGetLongNoteModeInt_Ln() {
		assertEquals(BeMusicLongNoteMode.LN, BeMusicSound.getLongNoteMode(RMASK_LNMODE | (0x01 << SHIFT_LNMODE)));
	}

	// getLongNoteMode(int)
	// 対象ビットの値が2の場合、CNが返ること
	@Test
	public void testGetLongNoteModeInt_Cn() {
		assertEquals(BeMusicLongNoteMode.CN, BeMusicSound.getLongNoteMode(RMASK_LNMODE | (0x02 << SHIFT_LNMODE)));
	}

	// getLongNoteMode(int)
	// 対象ビットの値が3の場合、HCNが返ること
	@Test
	public void testGetLongNoteModeInt_Hcn() {
		assertEquals(BeMusicLongNoteMode.HCN, BeMusicSound.getLongNoteMode(RMASK_LNMODE | (0x03 << SHIFT_LNMODE)));
	}

	// getLongNoteMode(int, BeMusicLongNoteMode)
	// 対象ビットの値が1の場合、LNが返ること
	@Test
	public void testGetLongNoteModeIntBeMusicLongNoteMode_Ln() {
		var dm = BeMusicLongNoteMode.HCN;
		assertEquals(BeMusicLongNoteMode.LN, BeMusicSound.getLongNoteMode(RMASK_LNMODE | (0x01 << SHIFT_LNMODE), dm));
	}

	// getLongNoteMode(int, BeMusicLongNoteMode)
	// 対象ビットの値が2の場合、CNが返ること
	@Test
	public void testGetLongNoteModeIntBeMusicLongNoteMode_Cn() {
		var dm = BeMusicLongNoteMode.HCN;
		assertEquals(BeMusicLongNoteMode.CN, BeMusicSound.getLongNoteMode(RMASK_LNMODE | (0x02 << SHIFT_LNMODE), dm));
	}

	// getLongNoteMode(int, BeMusicLongNoteMode)
	// 対象ビットの値が3の場合、HCNが返ること
	@Test
	public void testGetLongNoteModeIntBeMusicLongNoteMode_Hcn() {
		var dm = BeMusicLongNoteMode.CN;
		assertEquals(BeMusicLongNoteMode.HCN, BeMusicSound.getLongNoteMode(RMASK_LNMODE | (0x03 << SHIFT_LNMODE), dm));
	}

	// getLongNoteMode(int, BeMusicLongNoteMode)
	// 対象ビットの値が0でデフォルトがnullの場合、nullが返ること
	@Test
	public void testGetLongNoteModeIntBeMusicLongNoteMode_Undefined_Null() {
		assertNull(BeMusicSound.getLongNoteMode(RMASK_LNMODE | (0x00 << SHIFT_LNMODE), null));
	}

	// getLongNoteMode(int, BeMusicLongNoteMode)
	// 対象ビットの値が0でデフォルトがnull以外の場合、デフォルトで指定した値が返ること
	@Test
	public void testGetLongNoteModeIntBeMusicLongNoteMode_Undefined_Specify() {
		var dm = BeMusicLongNoteMode.CN;
		assertEquals(dm, BeMusicSound.getLongNoteMode(RMASK_LNMODE | (0x00 << SHIFT_LNMODE), dm));
	}

	// getNoteType(int)
	// 可視オブジェの生値で正しい値を返すこと
	@Test
	public void testGetNoteType_Visible() {
		var nts = BeMusicNoteType.values();
		for (var nt : nts) {
			assertEquals(nt, BeMusicSound.getNoteType(RawNotes.visible(0, nt.getId(), 1)));
		}
	}

	// getNoteType(int)
	// 可視オブジェ以外の生値では常にNONEを返すこと
	@Test
	public void testGetNoteType_NotVisible() {
		var none = BeMusicNoteType.NONE;
		assertEquals(none, BeMusicSound.getNoteType(RawNotes.invisible(0, 1)));
		assertEquals(none, BeMusicSound.getNoteType(RawNotes.bga(2)));
		assertEquals(none, BeMusicSound.getNoteType(RawNotes.bgm(3)));
	}

	// getDevice(int)
	// 可視オブジェの生値で正しい値を返すこと
	@Test
	public void testGetDevice_Visible() {
		var devs = BeMusicDevice.values();
		for (var dev : devs) {
			assertEquals(dev, BeMusicSound.getDevice(RawNotes.visible(dev.getIndex(), 1, 1)));
		}
	}

	// getDevice(int)
	// 不可視オブジェの生値で正しい値を返すこと
	@Test
	public void testGetDevice_Invisible() {
		var devs = BeMusicDevice.values();
		for (var dev : devs) {
			assertEquals(dev, BeMusicSound.getDevice(RawNotes.invisible(dev.getIndex(), 1)));
		}
	}

	// getDevice(int)
	// 可視・不可視オブジェ以外の生値ではnullを返すこと
	@Test
	public void testGetDevice_Other() {
		assertNull(BeMusicSound.getDevice(RawNotes.bga(1)));
		assertNull(BeMusicSound.getDevice(RawNotes.bgm(2)));
	}

	// hasDevice(int)
	// 可視・不可視オブジェの生値でtrueを返すこと
	@Test
	public void testHasDevice_True() {
		var devs = BeMusicDevice.values();
		for (var dev : devs) {
			assertTrue(BeMusicSound.hasDevice(RawNotes.visible(dev.getIndex(), 1, 1)));
			assertTrue(BeMusicSound.hasDevice(RawNotes.invisible(dev.getIndex(), 2)));
		}
	}

	// hasDevice(int)
	// 可視・不可視オブジェ以外の生値でfalseを返すこと
	@Test
	public void testHasDevice_False() {
		assertFalse(BeMusicSound.hasDevice(RawNotes.bga(1)));
		assertFalse(BeMusicSound.hasDevice(RawNotes.bgm(2)));
	}

	// hasTrack(int)
	// トラックIDに0以外指定するとtrueを返すこと
	@Test
	public void testHasTrack_True() {
		assertTrue(BeMusicSound.hasTrack(RawNotes.visible(0, 1, BeMusicSound.TRACK_ID_MIN)));
		assertTrue(BeMusicSound.hasTrack(RawNotes.visible(0, 1, BeMusicSound.TRACK_ID_MAX)));
		assertTrue(BeMusicSound.hasTrack(RawNotes.invisible(0, BeMusicSound.TRACK_ID_MIN)));
		assertTrue(BeMusicSound.hasTrack(RawNotes.invisible(0, BeMusicSound.TRACK_ID_MAX)));
		assertTrue(BeMusicSound.hasTrack(RawNotes.bga(BeMusicSound.TRACK_ID_MIN)));
		assertTrue(BeMusicSound.hasTrack(RawNotes.bga(BeMusicSound.TRACK_ID_MAX)));
		assertTrue(BeMusicSound.hasTrack(RawNotes.bgm(BeMusicSound.TRACK_ID_MIN)));
		assertTrue(BeMusicSound.hasTrack(RawNotes.bgm(BeMusicSound.TRACK_ID_MAX)));
	}

	// hasTrack(int)
	// トラックIDに0を指定するとfalseを返すこと
	@Test
	public void testHasTrack_False() {
		assertFalse(BeMusicSound.hasTrack(RawNotes.visible(0, 1, 0)));
		assertFalse(BeMusicSound.hasTrack(RawNotes.invisible(0, 0)));
		assertFalse(BeMusicSound.hasTrack(RawNotes.bga(0)));
		assertFalse(BeMusicSound.hasTrack(RawNotes.bgm(0)));
	}

	// hasExtended(int)
	// 再開=0x00(YES)、LN=0x00(未定義=LN)でfalseを返すこと ※全てのビットが0のため
	@Test
	public void testHasExtended_Restart_Undefined() {
		var v = BeMusicSound.makeValue(BeMusicSound.TRACK_ID_MIN, true, null);
		assertFalse(BeMusicSound.hasExtended(RawNotes.visible(0, 1, v)));
		assertFalse(BeMusicSound.hasExtended(RawNotes.invisible(0, v)));
		assertFalse(BeMusicSound.hasExtended(RawNotes.bgm(v)));
	}

	// hasExtended(int)
	// 再開=0x00(YES)、LN=0x01(LN)でtrueを返すこと
	@Test
	public void testHasExtended_Restart_Ln() {
		var v = BeMusicSound.makeValue(BeMusicSound.TRACK_ID_MIN, true, BeMusicLongNoteMode.LN);
		assertTrue(BeMusicSound.hasExtended(RawNotes.visible(0, 1, v)));
		assertTrue(BeMusicSound.hasExtended(RawNotes.invisible(0, v)));
		assertTrue(BeMusicSound.hasExtended(RawNotes.bgm(v)));
	}

	// hasExtended(int)
	// 再開=0x00(YES)、LN=0x02(CN)でtrueを返すこと
	@Test
	public void testHasExtended_Restart_Cn() {
		var v = BeMusicSound.makeValue(BeMusicSound.TRACK_ID_MIN, true, BeMusicLongNoteMode.CN);
		assertTrue(BeMusicSound.hasExtended(RawNotes.visible(0, 1, v)));
		assertTrue(BeMusicSound.hasExtended(RawNotes.invisible(0, v)));
		assertTrue(BeMusicSound.hasExtended(RawNotes.bgm(v)));
	}

	// hasExtended(int)
	// 再開=0x00(YES)、LN=0x03(HCN)でtrueを返すこと
	@Test
	public void testHasExtended_Restart_Hcn() {
		var v = BeMusicSound.makeValue(BeMusicSound.TRACK_ID_MIN, true, BeMusicLongNoteMode.HCN);
		assertTrue(BeMusicSound.hasExtended(RawNotes.visible(0, 1, v)));
		assertTrue(BeMusicSound.hasExtended(RawNotes.invisible(0, v)));
		assertTrue(BeMusicSound.hasExtended(RawNotes.bgm(v)));
	}

	// hasExtended(int)
	// 再開=0x01(NO)、LN=0x00(未定義=LN)でtrueを返すこと
	@Test
	public void testHasExtended_NoRestart_Undefined() {
		var v = BeMusicSound.makeValue(BeMusicSound.TRACK_ID_MIN, false, null);
		assertTrue(BeMusicSound.hasExtended(RawNotes.visible(0, 1, v)));
		assertTrue(BeMusicSound.hasExtended(RawNotes.invisible(0, v)));
		assertTrue(BeMusicSound.hasExtended(RawNotes.bgm(v)));
	}

	// hasExtended(int)
	// 再開=0x01(NO)、LN=0x01(LN)でtrueを返すこと
	@Test
	public void testHasExtended_NoRestart_Ln() {
		var v = BeMusicSound.makeValue(BeMusicSound.TRACK_ID_MIN, false, BeMusicLongNoteMode.LN);
		assertTrue(BeMusicSound.hasExtended(RawNotes.visible(0, 1, v)));
		assertTrue(BeMusicSound.hasExtended(RawNotes.invisible(0, v)));
		assertTrue(BeMusicSound.hasExtended(RawNotes.bgm(v)));
	}

	// hasExtended(int)
	// 再開=0x01(NO)、LN=0x02(CN)でtrueを返すこと
	@Test
	public void testHasExtended_NoRestart_Cn() {
		var v = BeMusicSound.makeValue(BeMusicSound.TRACK_ID_MIN, false, BeMusicLongNoteMode.CN);
		assertTrue(BeMusicSound.hasExtended(RawNotes.visible(0, 1, v)));
		assertTrue(BeMusicSound.hasExtended(RawNotes.invisible(0, v)));
		assertTrue(BeMusicSound.hasExtended(RawNotes.bgm(v)));
	}

	// hasExtended(int)
	// 再開=0x01(NO)、LN=0x03(HCN)でtrueを返すこと
	@Test
	public void testHasExtended_NoRestart_Hcn() {
		var v = BeMusicSound.makeValue(BeMusicSound.TRACK_ID_MIN, false, BeMusicLongNoteMode.HCN);
		assertTrue(BeMusicSound.hasExtended(RawNotes.visible(0, 1, v)));
		assertTrue(BeMusicSound.hasExtended(RawNotes.invisible(0, v)));
		assertTrue(BeMusicSound.hasExtended(RawNotes.bgm(v)));
	}

	// isVisible(int)
	// 可視オブジェの生値でtrueを返すこと
	@Test
	public void testIsVisible_Visible() {
		assertTrue(BeMusicSound.isVisible(RawNotes.visible(0, 1, 2)));
	}

	// isVisible(int)
	// 可視オブジェ以外の生値でfalseを返すこと
	@Test
	public void testIsVisible_NotVisible() {
		assertFalse(BeMusicSound.isVisible(RawNotes.invisible(0, 1)));
		assertFalse(BeMusicSound.isVisible(RawNotes.bga(2)));
		assertFalse(BeMusicSound.isVisible(RawNotes.bgm(3)));
	}

	// isInvisible(int)
	// 不可視オブジェの生値でtrueを返すこと
	@Test
	public void testIsInvisible_Invisible() {
		assertTrue(BeMusicSound.isInvisible(RawNotes.invisible(0, 1)));
	}

	// isInvisible(int)
	// 不可視オブジェ以外の生値でfalseを返すこと
	@Test
	public void testIsInvisible_NotInvisible() {
		assertFalse(BeMusicSound.isInvisible(RawNotes.visible(0, 1, 2)));
		assertFalse(BeMusicSound.isInvisible(RawNotes.bga(3)));
		assertFalse(BeMusicSound.isInvisible(RawNotes.bgm(4)));
	}

	// isBgm(int)
	// BGMの生値でtrueを返すこと
	@Test
	public void testIsBgm_Bgm() {
		assertTrue(BeMusicSound.isBgm(RawNotes.bgm(1)));
	}

	// isBgm(int)
	// BGM以外の生値でfalseを返すこと
	@Test
	public void testIsBgm_NotBgm() {
		assertFalse(BeMusicSound.isBgm(RawNotes.visible(0, 1, 2)));
		assertFalse(BeMusicSound.isBgm(RawNotes.invisible(0, 3)));
		assertFalse(BeMusicSound.isBgm(RawNotes.bga(4)));
	}

	// makeValue(int, boolean, BeMusicLongNoteMode)
	// トラックIDの最小値～最大値が正しく設定されること
	@Test
	public void testMakeValue_TrackId() {
		var min = BeMusicSound.TRACK_ID_MIN;
		var mid = BeMusicSound.TRACK_ID_MAX / 2;
		var max = BeMusicSound.TRACK_ID_MAX;
		assertEquals(min, BeMusicSound.makeValue(min, false, BeMusicLongNoteMode.HCN) & MASK_TRACKID);
		assertEquals(mid, BeMusicSound.makeValue(mid, false, BeMusicLongNoteMode.HCN) & MASK_TRACKID);
		assertEquals(max, BeMusicSound.makeValue(max, false, BeMusicLongNoteMode.HCN) & MASK_TRACKID);
	}

	// makeValue(int, boolean, BeMusicLongNoteMode)
	// 音声データの再開フラグが正しく設定されること
	@Test
	public void testMakeValue_Restart() {
		assertEquals(1, (BeMusicSound.makeValue(1, false, BeMusicLongNoteMode.HCN) & MASK_RESTART) >> SHIFT_RESTART);
		assertEquals(0, (BeMusicSound.makeValue(1, true, BeMusicLongNoteMode.HCN) & MASK_RESTART) >> SHIFT_RESTART);
	}

	// makeValue(int, boolean, BeMusicLongNoteMode)
	// ロングノートモードが正しく設定されること
	@Test
	public void testMakeValue_LnMode() {
		assertEquals(1, (BeMusicSound.makeValue(1, false, BeMusicLongNoteMode.LN) & MASK_LNMODE) >> SHIFT_LNMODE);
		assertEquals(2, (BeMusicSound.makeValue(1, false, BeMusicLongNoteMode.CN) & MASK_LNMODE) >> SHIFT_LNMODE);
		assertEquals(3, (BeMusicSound.makeValue(1, false, BeMusicLongNoteMode.HCN) & MASK_LNMODE) >> SHIFT_LNMODE);
	}

	// makeValue(int, boolean, BeMusicLongNoteMode)
	// ロングノートモードにnullを指定すると対象ビットの値が0に設定されること
	@Test
	public void testMakeValue_LnMode_Null() {
		assertEquals(0, (BeMusicSound.makeValue(1, false, null) & MASK_LNMODE) >> SHIFT_LNMODE);
	}

	// makeValue(int, boolean, BeMusicLongNoteMode)
	// IllegalArgumentException trackIdがTRACK_ID_MIN未満、または TRACK_ID_MAX超過
	@Test
	public void testMakeValue_TrackId_OutOfRange() {
		var ec = IllegalArgumentException.class;
		assertThrows(ec, () -> BeMusicSound.makeValue(BeMusicSound.TRACK_ID_MIN - 1, false, null));
		assertThrows(ec, () -> BeMusicSound.makeValue(BeMusicSound.TRACK_ID_MAX + 1, false, null));
	}
}
