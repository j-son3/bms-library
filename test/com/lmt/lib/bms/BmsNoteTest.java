package com.lmt.lib.bms;

import static org.junit.Assert.*;

import org.junit.Test;

public class BmsNoteTest {
	private static class NoteForNewNoteTest extends BmsNote {
		@Override protected BmsNote onNewInstance() { return new NoteForNewNoteTest(); }
	}

	// BmsNote()
	// 正常にインスタンスが生成できること
	@Test
	public void testBmsNote() {
		var n = new BmsNote();
		n.setup(100, 5, 20, 96, 255);
		assertEquals(100, n.getChannel());
		assertEquals(5, n.getIndex());
		assertEquals(20, n.getMeasure());
		assertEquals(96.0, n.getTick(), 0.0);
		assertEquals(255, n.getValue());
	}

	// newNote(BmsAddress, int)
	// 指定されたアドレス・値が設定されたコピー元オブジェクトと同じ型の新しいインスタンスを返すこと
	@Test
	public void testNoewNoteBmsAddressInt_Normal() {
		var org = new NoteForNewNoteTest();
		var copied = org.newNote(BmsAddress.of(3, 4.0, 1, 2), 5);
		assertNotSame(copied, org);
		assertEquals(1, copied.getChannel());
		assertEquals(2, copied.getIndex());
		assertEquals(3, copied.getMeasure());
		assertEquals(4.0, copied.getTick(), 0.0);
		assertEquals(5, copied.getValue());
		assertTrue(copied instanceof NoteForNewNoteTest);
	}

	// newNote(BmsAddress, int)
	// アドレス・値の検証は行われず、指定されたデータがそのまま格納されていること
	@Test
	public void testNoewNoteBmsAddressInt_NoVerify() {
		var org = new NoteForNewNoteTest();
		var adr = BmsAddress.of(
				BmsSpec.MEASURE_MAX + 1,
				BmsSpec.TICK_MAX + 1,
				BmsSpec.CHANNEL_MAX + 1,
				BmsSpec.CHINDEX_MAX + 1);
		var copied = org.newNote(adr, 0);
		assertNotSame(copied, org);
		assertEquals(BmsSpec.CHANNEL_MAX + 1, copied.getChannel());
		assertEquals(BmsSpec.CHINDEX_MAX + 1, copied.getIndex());
		assertEquals(BmsSpec.MEASURE_MAX + 1, copied.getMeasure());
		assertEquals(BmsSpec.TICK_MAX + 1, copied.getTick(), 0.0);
		assertEquals(0, copied.getValue());
		assertTrue(copied instanceof NoteForNewNoteTest);
	}

	// newNote(BmsAddress, int)
	// NullPointerException addressがnull
	@Test
	public void testNoewNoteBmsAddressInt_NullAddress() {
		var org = new NoteForNewNoteTest();
		assertThrows(NullPointerException.class, () -> org.newNote(null, 1));
	}

	// newNote(int, int, int, double, int)
	// 指定されたアドレス・値が設定されたコピー元オブジェクトと同じ型の新しいインスタンスを返すこと
	@Test
	public void testNewNoteIntIntIntDoubleInt_Normal() {
		var org = new NoteForNewNoteTest();
		var copied = org.newNote(1, 2, 3, 4.0, 5);
		assertNotSame(copied, org);
		assertEquals(1, copied.getChannel());
		assertEquals(2, copied.getIndex());
		assertEquals(3, copied.getMeasure());
		assertEquals(4.0, copied.getTick(), 0.0);
		assertEquals(5, copied.getValue());
		assertTrue(copied instanceof NoteForNewNoteTest);
	}

	// newNote(int, int, int, double, int)
	// アドレス・値の検証は行われず、指定されたデータがそのまま格納されていること
	@Test
	public void testNewNoteIntIntIntDoubleInt_NoVerify() {
		var org = new NoteForNewNoteTest();
		var copied = org.newNote(
				BmsSpec.CHANNEL_MAX + 1,
				BmsSpec.CHINDEX_MAX + 1,
				BmsSpec.MEASURE_MAX + 1,
				BmsSpec.TICK_MAX + 1,
				0);
		assertNotSame(copied, org);
		assertEquals(BmsSpec.CHANNEL_MAX + 1, copied.getChannel());
		assertEquals(BmsSpec.CHINDEX_MAX + 1, copied.getIndex());
		assertEquals(BmsSpec.MEASURE_MAX + 1, copied.getMeasure());
		assertEquals(BmsSpec.TICK_MAX + 1, copied.getTick(), 0.0);
		assertEquals(0, copied.getValue());
		assertTrue(copied instanceof NoteForNewNoteTest);
	}
}
