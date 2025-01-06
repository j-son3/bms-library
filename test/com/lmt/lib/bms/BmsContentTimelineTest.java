package com.lmt.lib.bms;

import static org.junit.Assert.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.junit.Ignore;
import org.junit.Test;

import com.lmt.lib.bms.BmsTest.AnyType1;
import com.lmt.lib.bms.BmsTest.AnyType2;

public class BmsContentTimelineTest extends BmsContentTest {
	private static class TestBmsNote extends BmsNote {}
	private static final Supplier<BmsNote> TEST_NOTE_CREATOR = () -> new TestBmsNote();

	private static class NoteTestData {
		int channel, index, measure, value;
		double tick;
		NoteTestData(int c, int i, int m, double t, int v) {
			channel = c;
			index = i;
			measure = m;
			tick = t;
			value = v;
		}
	}

	// putNote(int, int, double, int)
	// 正常系のみテスト
	@Test
	public void testPutNoteIntIntIntDouble_001() {
		var c = new BmsContent(spec());
		c.beginEdit();
		getNormalNoteTestData(true).forEach(t -> {
			var n = c.putNote(t.channel, t.measure, t.tick, t.value);
			assertEquals(t.channel, n.getChannel());
			assertEquals(t.index, n.getIndex());
			assertEquals(t.measure, n.getMeasure());
			assertEquals(t.tick, n.getTick(), 0.0);
			assertEquals(t.value, n.getValue());
		});
	}

	// putNote(int, BmsAt, int)
	// 正常系のみテスト
	@Test
	public void testPutNoteIntBmsAtInt_001() {
		var c = new BmsContent(spec());
		c.beginEdit();
		getNormalNoteTestData(true).forEach(t -> {
			var n = c.putNote(t.channel, new BmsPoint(t.measure, t.tick), t.value);
			assertEquals(t.channel, n.getChannel());
			assertEquals(t.index, n.getIndex());
			assertEquals(t.measure, n.getMeasure());
			assertEquals(t.tick, n.getTick(), 0.0);
			assertEquals(t.value, n.getValue());
		});
	}

	// putNote(int, int, int, double, int)
	// 正常系のみテスト
	@Test
	public void testPutNoteIntIntIntIntInt_001() {
		var c = new BmsContent(spec());
		c.beginEdit();
		getNormalNoteTestData(false).forEach(t -> {
			var n = c.putNote(t.channel, t.index, t.measure, t.tick, t.value);
			assertEquals(t.channel, n.getChannel());
			assertEquals(t.index, n.getIndex());
			assertEquals(t.measure, n.getMeasure());
			assertEquals(t.tick, n.getTick(), 0.0);
			assertEquals(t.value, n.getValue());
		});
	}

	// putNote(int, int, BmsAt, int)
	// 正常系のみテスト
	@Test
	public void testPutNoteIntIntBmsAtInt_001() {
		var c = new BmsContent(spec());
		c.beginEdit();
		getNormalNoteTestData(false).forEach(t -> {
			var n = c.putNote(t.channel, t.index, new BmsPoint(t.measure, t.tick), t.value);
			assertEquals(t.channel, n.getChannel());
			assertEquals(t.index, n.getIndex());
			assertEquals(t.measure, n.getMeasure());
			assertEquals(t.tick, n.getTick(), 0.0);
			assertEquals(t.value, n.getValue());
		});
	}

	// putNote(int, int, double, int, Supplier<BmsNote>)
	// 正常系のみテスト
	@Test
	public void testPutNoteIntIntDoubleIntCreator_001() {
		var c = new BmsContent(spec());
		c.beginEdit();
		getNormalNoteTestData(true).forEach(t -> {
			var n = c.putNote(t.channel, t.measure, t.tick, t.value, TEST_NOTE_CREATOR);
			assertTrue(n instanceof TestBmsNote);
			assertEquals(t.channel, n.getChannel());
			assertEquals(t.index, n.getIndex());
			assertEquals(t.measure, n.getMeasure());
			assertEquals(t.tick, n.getTick(), 0.0);
			assertEquals(t.value, n.getValue());
		});
	}

	// putNote(int, BmsAt, int, Supplier<BmsNote>)
	// 正常系のみテスト
	@Test
	public void testPutNoteIntBmsAtIntCreator_001() {
		var c = new BmsContent(spec());
		c.beginEdit();
		getNormalNoteTestData(true).forEach(t -> {
			var n = c.putNote(t.channel, new BmsPoint(t.measure, t.tick), t.value, TEST_NOTE_CREATOR);
			assertTrue(n instanceof TestBmsNote);
			assertEquals(t.channel, n.getChannel());
			assertEquals(t.index, n.getIndex());
			assertEquals(t.measure, n.getMeasure());
			assertEquals(t.tick, n.getTick(), 0.0);
			assertEquals(t.value, n.getValue());
		});
	}

	// putNote(int, BmsAt, int, Supplier<BmsNote>)
	// 正常系のみテスト
	@Test
	public void testPutNoteIntIntBmsAtIntCreator_001() {
		var c = new BmsContent(spec());
		c.beginEdit();
		getNormalNoteTestData(true).forEach(t -> {
			var n = c.putNote(t.channel, new BmsPoint(t.measure, t.tick), t.value, TEST_NOTE_CREATOR);
			assertTrue(n instanceof TestBmsNote);
			assertEquals(t.channel, n.getChannel());
			assertEquals(t.index, n.getIndex());
			assertEquals(t.measure, n.getMeasure());
			assertEquals(t.tick, n.getTick(), 0.0);
			assertEquals(t.value, n.getValue());
		});
	}

	// putNote(int, int, int, double, int, Supplier<BmsNote>)
	// 正常
	@Test
	public void testPutNoteIntIntIntDoubleIntCreator_001() {
		var c = new BmsContent(spec());
		c.beginEdit();
		getNormalNoteTestData(false).forEach(t -> {
			var n = c.putNote(t.channel, t.index, t.measure, t.tick, t.value, TEST_NOTE_CREATOR);
			assertTrue(n instanceof TestBmsNote);
			assertEquals(t.channel, n.getChannel());
			assertEquals(t.index, n.getIndex());
			assertEquals(t.measure, n.getMeasure());
			assertEquals(t.tick, n.getTick(), 0.0);
			assertEquals(t.value, n.getValue());
		});
	}

	// putNote(int, int, int, double, int, Supplier<BmsNote>)
	// 既にノートのある場所に追加すると、新しいノートで上書きされること
	@Test
	public void testPutNoteIntIntIntDoubleIntCreator_Overwrite() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.putNote(CN_A36S, 0, 1, 96.0, 500, TEST_NOTE_CREATOR);
		c.putNote(CN_A36S, 0, 1, 96.0, 1000, TEST_NOTE_CREATOR);
		c.endEdit();
		var n = c.getNote(CN_A36S, 0, 1, 96.0);
		assertTrue(n instanceof TestBmsNote);
		assertEquals(1000, n.getValue());
	}

	// putNote(int, int, int, double, int, Supplier<BmsNote>)
	// ノートの値にVALUE_MIN, VALUE_MAXを設定できること
	@Test
	public void testPutNoteIntIntIntDoubleIntCreator_MinMaxValue() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.putNote(CN_A16S, 0, 1, 0.0, BmsSpec.VALUE_MIN, TEST_NOTE_CREATOR);
		c.putNote(CN_A16S, 0, 1, 48.0, BmsSpec.VALUE_MAX, TEST_NOTE_CREATOR);
		c.putNote(CN_A36S, 0, 1, 96.0, BmsSpec.VALUE_MIN, TEST_NOTE_CREATOR);
		c.putNote(CN_A36S, 0, 1, 144.0, BmsSpec.VALUE_MAX, TEST_NOTE_CREATOR);
		c.endEdit();
		assertEquals(BmsSpec.VALUE_MIN, c.getNote(CN_A16S, 0, 1, 0.0).getValue());
		assertEquals(BmsSpec.VALUE_MAX, c.getNote(CN_A16S, 0, 1, 48.0).getValue());
		assertEquals(BmsSpec.VALUE_MIN, c.getNote(CN_A36S, 0, 1, 96.0).getValue());
		assertEquals(BmsSpec.VALUE_MAX, c.getNote(CN_A36S, 0, 1, 144.0).getValue());
	}

	// putNote(int, int, int, double, int, Supplier<BmsNote>)
	// IllegalStateException 動作モードが編集モードではない
	@Test
	public void testPutNoteIntIntIntDoubleIntCreator_002() {
		var c = new BmsContent(spec());
		assertThrows(IllegalStateException.class, () -> c.putNote(1, 0, 0, 0, 0, TEST_NOTE_CREATOR));
	}

	// putNote(int, int, int, double, int, Supplier<BmsNote>)
	// IllegalArgumentException BMS仕様にないチャンネル番号を指定した
	@Test
	public void testPutNoteIntIntIntDoubleIntCreator_003() {
		var c = new BmsContent(spec());
		c.beginEdit();
		assertThrows(IllegalArgumentException.class, () -> c.putNote(333, 0, 0, 0, 0, TEST_NOTE_CREATOR));
	}

	// putNote(int, int, int, double, int, Supplier<BmsNote>)
	// IndexOutOfBoundsException チャンネルインデックスがマイナス値
	@Test
	public void testPutNoteIntIntIntDoubleIntCreator_004() {
		var c = new BmsContent(spec());
		c.beginEdit();
		assertThrows(IndexOutOfBoundsException.class, () -> c.putNote(1, -1, 0, 0, 0, TEST_NOTE_CREATOR));
	}

	// putNote(int, int, int, double, int, Supplier<BmsNote>)
	// IndexOutOfBoundsException 重複不許可のチャンネルで0以外のチャンネルインデックスを指定した
	@Test
	public void testPutNoteIntIntIntDoubleIntCreator_005() {
		var c = new BmsContent(spec());
		c.beginEdit();
		assertThrows(IndexOutOfBoundsException.class, () -> c.putNote(11, 1, 0, 0, 0, TEST_NOTE_CREATOR));
	}

	// putNote(int, int, int, double, int, Supplier<BmsNote>)
	// IllegalArgumentException 小節番号が{@link BmsSpec#MEASURE_MIN}未満または{@link BmsSpec#MEASURE_MAX}超過
	@Test
	public void testPutNoteIntIntIntDoubleIntCreator_006() {
		var c = new BmsContent(spec());
		var ex = IllegalArgumentException.class;
		c.beginEdit();
		assertThrows(ex, () -> c.putNote(1, 0, BmsSpec.MEASURE_MIN - 1, 0, 0, TEST_NOTE_CREATOR));
		assertThrows(ex, () -> c.putNote(1, 0, BmsSpec.MEASURE_MAX + 1, 0, 0, TEST_NOTE_CREATOR));
	}

	// putNote(int, int, int, double, int, Supplier<BmsNote>)
	// IllegalArgumentException 小節の刻み位置がマイナス値または当該小節の刻み数以上
	@Test
	public void testPutNoteIntIntIntDoubleIntCreator_007() {
		var ex = IllegalArgumentException.class;

		// 4/4拍子
		var c = new BmsContent(spec());
		c.beginEdit();
		assertThrows(ex, () -> c.putNote(1, 0, 0, -1, 0, TEST_NOTE_CREATOR));
		assertThrows(ex, () -> c.putNote(1, 0, 0, 192, 0, TEST_NOTE_CREATOR));

		// 小節長2倍
		var c2 = new BmsContent(spec());
		c2.beginEdit();
		c2.setMeasureValue(2, 0, 2.0);  // 0小節の長さを2倍に
		c2.endEdit();
		c2.beginEdit();
		assertThrows(ex, () -> c2.putNote(1, 0, 0, -1, 0, TEST_NOTE_CREATOR));
		assertThrows(ex, () -> c2.putNote(1, 0, 0, 384, 0, TEST_NOTE_CREATOR));
	}

	// putNote(int, int, int, double, int, Supplier<BmsNote>)
	// IllegalArgumentException 指定チャンネルのデータ型が配列型ではない
	@Test
	public void testPutNoteIntIntIntDoubleIntCreator_008() {
		var c = new BmsContent(spec());
		c.beginEdit();
		assertThrows(IllegalArgumentException.class, () -> c.putNote(2, 0, 0, 0, 0, TEST_NOTE_CREATOR));
	}

	// putNote(int, int, int, double, Supplier<BmsNote>)
	// IllegalArgumentException ノートの値に0を指定した
	@Test
	public void testPutNoteIntIntIntDoubleIntCreator_ValueIs0() {
		var ec = IllegalArgumentException.class;
		var c = new BmsContent(spec());
		c.beginEdit();
		assertThrows(ec, () -> c.putNote(CN_A16, 0, 0, 0, 0, TEST_NOTE_CREATOR));
		assertThrows(ec, () -> c.putNote(CN_A36, 0, 0, 0, 0, TEST_NOTE_CREATOR));
	}

	// putNote(int, int, int, double, int, Supplier<BmsNote>)
	// NullPointerException createNoteがnull
	@Test
	public void testPutNoteIntIntIntDoubleIntCreator_009() {
		var c = new BmsContent(spec());
		c.beginEdit();
		assertThrows(NullPointerException.class, () -> c.putNote(1, 0, 0, 0, 1, null));
	}

	// putNote(int, int, int, double, int, Supplier<BmsNote>)
	// IllegalArgumentException createNoteの結果がnull
	@Test
	public void testPutNoteIntIntIntDoubleIntCreator_010() {
		var c = new BmsContent(spec());
		c.beginEdit();
		assertThrows(IllegalArgumentException.class, () -> c.putNote(1, 0, 0, 0, 0, () -> null));
	}

	private static class BmsNote_PutNoteCallOnCreate extends BmsNote {
		static boolean isOnCreateCalled;

		@Override
		protected void onCreate() {
			isOnCreateCalled = true;
			assertEquals(11, getChannel());
			assertEquals(0, getIndex());
			assertEquals(1, getMeasure());
			assertEquals(96.0, getTick(), 0.0);
			assertEquals(1000, getValue());
		}
	}

	// putNote(int, int, int, double, int, Supplier<BmsNote>)
	// ノート追加時にはBmsNote#onCreateが呼び出される
	@Test
	public void testPutNote_CallOnCreateOnUpdate() {
		BmsNote_PutNoteCallOnCreate.isOnCreateCalled = false;
		var c = new BmsContent(spec());
		c.beginEdit();
		c.putNote(11, 0, 1, 96, 1000, () -> new BmsNote_PutNoteCallOnCreate());
		c.endEdit();

		// 通知が呼ばれていること
		assertTrue(BmsNote_PutNoteCallOnCreate.isOnCreateCalled);
	}

	// getMeasureValue(int, int)
	// 正常系のみ
	@Test
	public void testGetMeasureValueIntInt_001() {
		var c = new BmsContent(spec());
		var objValue = new BmsTest.AnyType1(10, 20);
		c.beginEdit();
		c.setMeasureValue(CN_INT, 1, 100L);
		c.setMeasureValue(CN_NUM, 1, 200.0);
		c.setMeasureValue(CN_STR, 1, "hoge");
		c.setMeasureValue(CN_B16, 1, 0xabL);
		c.setMeasureValue(CN_B36, 1, Long.parseLong("xy", 36));
		c.setMeasureValue(CN_OBJ, 1, objValue);
		c.endEdit();

		// 設定済みの値はその設定値が返る
		// 任意型の場合はsetMeasureValueで指定したオブジェクトの参照が返る
		assertEquals(100L, (long)c.getMeasureValue(CN_INT, 1));
		assertEquals(200.0, (double)c.getMeasureValue(CN_NUM, 1), 0.0);
		assertEquals("hoge", (String)c.getMeasureValue(CN_STR, 1));
		assertEquals(0xabL, (long)c.getMeasureValue(CN_B16, 1));
		assertEquals(Long.parseLong("xy", 36), (long)c.getMeasureValue(CN_B36, 1));
		assertEquals(objValue, (BmsTest.AnyType1)c.getMeasureValue(CN_OBJ, 1));
		assertSame(objValue, (BmsTest.AnyType1)c.getMeasureValue(CN_OBJ, 1));

		// 未設定の値はそのチャンネルの初期値が返る
		// 任意型の場合は必ずnullが返る
		BmsSpec s = c.getSpec();
		assertEquals((long)s.getChannel(CN_INT).getDefaultValue(), (long)c.getMeasureValue(CN_INT, 0));
		assertEquals((double)s.getChannel(CN_NUM).getDefaultValue(), (double)c.getMeasureValue(CN_NUM, 0), 0.0);
		assertEquals((String)s.getChannel(CN_STR).getDefaultValue(), (String)c.getMeasureValue(CN_STR, 0));
		assertEquals((long)s.getChannel(CN_B16).getDefaultValue(), (long)c.getMeasureValue(CN_B16, 0));
		assertEquals((long)s.getChannel(CN_B36).getDefaultValue(), (long)c.getMeasureValue(CN_B36, 0));
		assertNull(c.getMeasureValue(CN_OBJ, 0));
	}

	// getMeasureValue(int, int, int)
	// 正常
	@Test
	public void testGetMeasureValueIntIntInt_001() {
		var c = new BmsContent(spec());
		var objValue = new BmsTest.AnyType1(10, 20);
		c.beginEdit();
		c.setMeasureValue(CN_INT, 1, 0, 100L);
		c.setMeasureValue(CN_NUM, 1, 0, 200.0);
		c.setMeasureValue(CN_STR, 1, 0, "hoge");
		c.setMeasureValue(CN_B16, 1, 0, 0xabL);
		c.setMeasureValue(CN_B36, 1, 0, Long.parseLong("xy", 36));
		c.setMeasureValue(CN_OBJ, 1, 0, objValue);
		c.endEdit();

		// 設定済みの値はその設定値が返る
		// 任意型の場合はsetMeasureValueで指定したオブジェクトの参照が返る
		assertEquals(100L, (long)c.getMeasureValue(CN_INT, 1, 0));
		assertEquals(200.0, (double)c.getMeasureValue(CN_NUM, 1, 0), 0.0);
		assertEquals("hoge", (String)c.getMeasureValue(CN_STR, 1, 0));
		assertEquals(0xabL, (long)c.getMeasureValue(CN_B16, 1, 0));
		assertEquals(Long.parseLong("xy", 36), (long)c.getMeasureValue(CN_B36, 1, 0));
		assertEquals(objValue, (BmsTest.AnyType1)c.getMeasureValue(CN_OBJ, 1, 0));
		assertSame(objValue, (BmsTest.AnyType1)c.getMeasureValue(CN_OBJ, 1, 0));

		// 未設定の値はそのチャンネルの初期値が返る
		// 任意型の場合は必ずnullが返る
		var s = c.getSpec();
		assertEquals((long)s.getChannel(CN_INT).getDefaultValue(), (long)c.getMeasureValue(CN_INT, 0, 0));
		assertEquals((double)s.getChannel(CN_NUM).getDefaultValue(), (double)c.getMeasureValue(CN_NUM, 0, 0), 0.0);
		assertEquals((String)s.getChannel(CN_STR).getDefaultValue(), (String)c.getMeasureValue(CN_STR, 0, 0));
		assertEquals((long)s.getChannel(CN_B16).getDefaultValue(), (long)c.getMeasureValue(CN_B16, 0, 0));
		assertEquals((long)s.getChannel(CN_B36).getDefaultValue(), (long)c.getMeasureValue(CN_B36, 0, 0));
		assertNull(c.getMeasureValue(CN_OBJ, 0, 0));
	}

	// getMeasureValue(int, int, int)
	// IllegalArgumentException BMS仕様にないチャンネル番号を指定した
	@Test
	public void testGetMeasureValueIntIntInt_002() {
		var c = new BmsContent(spec());
		assertThrows(IllegalArgumentException.class, () -> c.getMeasureValue(999, 0, 0));
	}

	// getMeasureValue(int, int, int)
	// IndexOutOfBoundsException チャンネルインデックスがマイナス値
	@Test
	public void testGetMeasureValueIntIntInt_003() {
		var c = new BmsContent(spec());
		assertThrows(IndexOutOfBoundsException.class, () -> c.getMeasureValue(CN_INT, -1, 0));
	}

	// getMeasureValue(int, int, int)
	// IndexOutOfBoundsException 重複不許可のチャンネルで0以外のインデックスを指定した
	@Test
	public void testGetMeasureValueIntIntInt_004() {
		var c = new BmsContent(spec());
		assertThrows(IndexOutOfBoundsException.class, () -> c.getMeasureValue(CN_INTS, 1, 0));
	}

	// getMeasureValue(int, int, int)
	// IllegalArgumentException 小節番号が{@link BmsSpec#MEASURE_MIN}未満または{@link BmsSpec#MEASURE_MAX}超過
	@Test
	public void testGetMeasureValueIntIntInt_005() {
		var c = new BmsContent(spec());
		var ex = IllegalArgumentException.class;
		c.beginEdit();
		c.setMeasureValue(CN_INT, 0, 0, 100L);
		c.setMeasureValue(CN_INT, 0, 1, 200L);
		c.endEdit();
		assertThrows(ex, () -> c.getMeasureValue(CN_INT, 0, BmsSpec.MEASURE_MIN - 1));
		assertThrows(ex, () -> c.getMeasureValue(CN_INT, 0, BmsSpec.MEASURE_MAX + 1));
		assertEquals((Long)c.getSpec().getChannel(CN_INT).getDefaultValue(), c.getMeasureValue(CN_INT, 0, BmsSpec.MEASURE_MIN));
		assertEquals((Long)c.getSpec().getChannel(CN_INT).getDefaultValue(), c.getMeasureValue(CN_INT, 0, BmsSpec.MEASURE_MAX));
	}

	// getMeasureValue(int, int, int)
	// IllegalArgumentException 指定チャンネルのデータ型が値型ではない
	@Test
	public void testGetMeasureValueIntIntInt_006() {
		var c = new BmsContent(spec());
		assertThrows(IllegalArgumentException.class, () -> c.getMeasureValue(CN_A36, 0, 0));
	}

	// setMeasureValue(int, int, Object)
	// 正常系のみ
	@Test
	public void testSetMeasureValueIntIntObject_001() {
		// 通常ケース(setMeasureValueで設定した値がgetMeasureValueで取得でき、登録時の値と一致すること)
		// 任意型の値は、setで設定したObject参照が格納されるため、getで同じ参照を返すこと
		var c = new BmsContent(spec());
		var objValue = new BmsTest.AnyType1(1, 2);
		var objValue2 = new BmsTest.AnyType2(3, 4, 5);

		c.beginEdit();
		c.setMeasureValue(CN_INT, 0, 300L);
		c.setMeasureValue(CN_INT, 1, 0L);  // Dummy data
		c.setMeasureValue(CN_NUM, 0, 300.0);
		c.setMeasureValue(CN_NUM, 1, 0.0);  // Dummy data
		c.setMeasureValue(CN_STR, 0, "str");
		c.setMeasureValue(CN_STR, 1, "hoge");  // Dummy data
		c.setMeasureValue(CN_B16, 0, 0xABL);
		c.setMeasureValue(CN_B16, 1, 0xCDL);  // Dummy data
		c.setMeasureValue(CN_B36, 0, Long.parseLong("WX", 36));
		c.setMeasureValue(CN_B36, 1, Long.parseLong("YZ", 36));  // Dummy data
		c.setMeasureValue(CN_OBJ, 0, objValue);
		c.setMeasureValue(CN_OBJ, 1, objValue2);  // Dummy data
		c.endEdit();
		assertEquals(300L, (long)c.getMeasureValue(CN_INT, 0));
		assertEquals(300.0, (double)c.getMeasureValue(CN_NUM, 0), 0.00001);
		assertEquals("str", (String)c.getMeasureValue(CN_STR, 0));
		assertEquals(0xABL, (long)c.getMeasureValue(CN_B16, 0));
		assertEquals(Long.parseLong("WX", 36), (long)c.getMeasureValue(CN_B36, 0));
		assertEquals(objValue, (BmsTest.AnyType1)c.getMeasureValue(CN_OBJ, 0));
		assertSame(objValue, (BmsTest.AnyType1)c.getMeasureValue(CN_OBJ, 0));

		var dvInt = (long)c.getSpec().getChannel(CN_INT).getDefaultValue();
		var dvNum = (double)c.getSpec().getChannel(CN_NUM).getDefaultValue();
		var dvStr = (String)c.getSpec().getChannel(CN_STR).getDefaultValue();
		var dvB16 = (long)c.getSpec().getChannel(CN_B16).getDefaultValue();
		var dvB36 = (long)c.getSpec().getChannel(CN_B36).getDefaultValue();
		var dvObj = c.getSpec().getChannel(CN_OBJ).getDefaultValue();

		// null指定で値を消去し、当該小節データ値は初期値になること
		// 任意型の初期値は必ずnullであること
		c.beginEdit();
		c.setMeasureValue(CN_INT, 0, null);
		c.setMeasureValue(CN_NUM, 0, null);
		c.setMeasureValue(CN_STR, 0, null);
		c.setMeasureValue(CN_B16, 0, null);
		c.setMeasureValue(CN_B36, 0, null);
		c.setMeasureValue(CN_OBJ, 0, null);
		c.endEdit();
		assertEquals(dvInt, (long)c.getMeasureValue(CN_INT, 0));
		assertEquals(dvNum, (double)c.getMeasureValue(CN_NUM, 0), 0.00001);
		assertEquals(dvStr, (String)c.getMeasureValue(CN_STR, 0));
		assertEquals(dvB16, (long)c.getMeasureValue(CN_B16, 0));
		assertEquals(dvB36, (long)c.getMeasureValue(CN_B36, 0));
		assertEquals(dvObj, c.getMeasureValue(CN_OBJ, 0));
		assertNull(dvObj);

		// measureに先頭以外を指定して設定した値が正常に登録されること
		// measure中間の、小節データ値未指定の小節は初期値になっていること
		c = new BmsContent(spec());
		c.beginEdit();
		c.setMeasureValue(CN_INT, 0, 500L);
		c.setMeasureValue(CN_INT, 2, 800L);
		c.setMeasureValue(CN_NUM, 0, 100.0);
		c.setMeasureValue(CN_NUM, 2, 300.0);
		c.setMeasureValue(CN_STR, 0, "json");
		c.setMeasureValue(CN_STR, 2, "white");
		c.setMeasureValue(CN_B16, 0, 0xAAL);
		c.setMeasureValue(CN_B16, 2, 0xBBL);
		c.setMeasureValue(CN_B36, 0, Long.parseLong("XX", 36));
		c.setMeasureValue(CN_B36, 2, Long.parseLong("YY", 36));
		c.setMeasureValue(CN_OBJ, 0, objValue);
		c.setMeasureValue(CN_OBJ, 2, objValue2);
		c.endEdit();
		assertEquals(500L, (long)c.getMeasureValue(CN_INT, 0));
		assertEquals(dvInt, (long)c.getMeasureValue(CN_INT, 1));
		assertEquals(800L, (long)c.getMeasureValue(CN_INT, 2));
		assertEquals(100.0, (double)c.getMeasureValue(CN_NUM, 0), 0.00001);
		assertEquals(dvNum, (double)c.getMeasureValue(CN_NUM, 1), 0.00001);
		assertEquals(300.0, (double)c.getMeasureValue(CN_NUM, 2), 0.00001);
		assertEquals("json", (String)c.getMeasureValue(CN_STR, 0));
		assertEquals(dvStr, (String)c.getMeasureValue(CN_STR, 1));
		assertEquals("white", (String)c.getMeasureValue(CN_STR, 2));
		assertEquals(0xAAL, (long)c.getMeasureValue(CN_B16, 0));
		assertEquals(dvB16, (long)c.getMeasureValue(CN_B16, 1));
		assertEquals(0xBBL, (long)c.getMeasureValue(CN_B16, 2));
		assertEquals(Long.parseLong("XX", 36), (long)c.getMeasureValue(CN_B36, 0));
		assertEquals(dvB36, (long)c.getMeasureValue(CN_B36, 1));
		assertEquals(Long.parseLong("YY", 36), (long)c.getMeasureValue(CN_B36, 2));
		assertEquals(objValue, (AnyType1)c.getMeasureValue(CN_OBJ, 0));
		assertSame(objValue, (AnyType1)c.getMeasureValue(CN_OBJ, 0));
		assertNull(c.getMeasureValue(CN_OBJ, 1));
		assertEquals(objValue2, (AnyType2)c.getMeasureValue(CN_OBJ, 2));
		assertSame(objValue2, (AnyType2)c.getMeasureValue(CN_OBJ, 2));
	}

	// setMeasureValue(int, int, int, Object)
	// 正常
	@Test
	public void testSetMeasureValueIntIntIntObject_001() {
		// 通常ケース(setMeasureValueで設定した値がgetMeasureValueで取得でき、登録時の値と一致すること)
		// 任意型の値は、setで設定したObject参照が格納されるため、getで同じ参照を返すこと
		var c = new BmsContent(spec());
		var objValue = new AnyType1(1, 2);
		var objValue2 = new AnyType2(3, 4, 5);

		c.beginEdit();
		c.setMeasureValue(CN_INT, 0, 0, 300L);
		c.setMeasureValue(CN_INT, 1, 0, 0L);  // Dummy data
		c.setMeasureValue(CN_NUM, 0, 0, 300.0);
		c.setMeasureValue(CN_NUM, 1, 0, 0.0);  // Dummy data
		c.setMeasureValue(CN_STR, 0, 0, "str");
		c.setMeasureValue(CN_STR, 1, 0, "hoge");  // Dummy data
		c.setMeasureValue(CN_B16, 0, 0, 0xABL);
		c.setMeasureValue(CN_B16, 1, 0, 0xCDL);  // Dummy data
		c.setMeasureValue(CN_B36, 0, 0, Long.parseLong("WX", 36));
		c.setMeasureValue(CN_B36, 1, 0, Long.parseLong("YZ", 36));  // Dummy data
		c.setMeasureValue(CN_OBJ, 0, 0, objValue);
		c.setMeasureValue(CN_OBJ, 1, 0, objValue2);  // Dummy data
		c.endEdit();
		assertEquals(300L, (long)c.getMeasureValue(CN_INT, 0, 0));
		assertEquals(300.0, (double)c.getMeasureValue(CN_NUM, 0, 0), 0.00001);
		assertEquals("str", (String)c.getMeasureValue(CN_STR, 0, 0));
		assertEquals(0xABL, (long)c.getMeasureValue(CN_B16, 0, 0));
		assertEquals(Long.parseLong("WX", 36), (long)c.getMeasureValue(CN_B36, 0, 0));
		assertEquals(objValue, (AnyType1)c.getMeasureValue(CN_OBJ, 0, 0));
		assertSame(objValue, (AnyType1)c.getMeasureValue(CN_OBJ, 0, 0));

		var dvInt = (long)c.getSpec().getChannel(CN_INT).getDefaultValue();
		var dvNum = (double)c.getSpec().getChannel(CN_NUM).getDefaultValue();
		var dvStr = (String)c.getSpec().getChannel(CN_STR).getDefaultValue();
		var dvB16 = (long)c.getSpec().getChannel(CN_B16).getDefaultValue();
		var dvB36 = (long)c.getSpec().getChannel(CN_B36).getDefaultValue();
		var dvObj = c.getSpec().getChannel(CN_OBJ).getDefaultValue();

		// null指定で値を消去し、当該小節データ値は初期値になること
		// 任意型の初期値は必ずnullであること
		c.beginEdit();
		c.setMeasureValue(CN_INT, 0, 0, null);
		c.setMeasureValue(CN_NUM, 0, 0, null);
		c.setMeasureValue(CN_STR, 0, 0, null);
		c.setMeasureValue(CN_B16, 0, 0, null);
		c.setMeasureValue(CN_B36, 0, 0, null);
		c.setMeasureValue(CN_OBJ, 0, 0, null);
		c.endEdit();
		assertEquals(dvInt, (long)c.getMeasureValue(CN_INT, 0, 0));
		assertEquals(dvNum, (double)c.getMeasureValue(CN_NUM, 0, 0), 0.00001);
		assertEquals(dvStr, (String)c.getMeasureValue(CN_STR, 0, 0));
		assertEquals(dvB16, (long)c.getMeasureValue(CN_B16, 0, 0));
		assertEquals(dvB36, (long)c.getMeasureValue(CN_B36, 0, 0));
		assertEquals(dvObj, c.getMeasureValue(CN_OBJ, 0, 0));
		assertNull(dvObj);

		// measureに先頭以外を指定して設定した値が正常に登録されること
		// measure中間の、小節データ値未指定の小節は初期値になっていること
		c = new BmsContent(spec());
		c.beginEdit();
		c.setMeasureValue(CN_INT, 0, 0, 500L);
		c.setMeasureValue(CN_INT, 2, 0, 800L);
		c.setMeasureValue(CN_NUM, 0, 0, 100.0);
		c.setMeasureValue(CN_NUM, 2, 0, 300.0);
		c.setMeasureValue(CN_STR, 0, 0, "json");
		c.setMeasureValue(CN_STR, 2, 0, "white");
		c.setMeasureValue(CN_B16, 0, 0, 0xAAL);
		c.setMeasureValue(CN_B16, 2, 0, 0xBBL);
		c.setMeasureValue(CN_B36, 0, 0, Long.parseLong("XX", 36));
		c.setMeasureValue(CN_B36, 2, 0, Long.parseLong("YY", 36));
		c.setMeasureValue(CN_OBJ, 0, 0, objValue);
		c.setMeasureValue(CN_OBJ, 2, 0, objValue2);
		c.endEdit();
		assertEquals(500L, (long)c.getMeasureValue(CN_INT, 0, 0));
		assertEquals(dvInt, (long)c.getMeasureValue(CN_INT, 1, 0));
		assertEquals(800L, (long)c.getMeasureValue(CN_INT, 2, 0));
		assertEquals(100.0, (double)c.getMeasureValue(CN_NUM, 0, 0), 0.00001);
		assertEquals(dvNum, (double)c.getMeasureValue(CN_NUM, 1, 0), 0.00001);
		assertEquals(300.0, (double)c.getMeasureValue(CN_NUM, 2, 0), 0.00001);
		assertEquals("json", (String)c.getMeasureValue(CN_STR, 0, 0));
		assertEquals(dvStr, (String)c.getMeasureValue(CN_STR, 1, 0));
		assertEquals("white", (String)c.getMeasureValue(CN_STR, 2, 0));
		assertEquals(0xAAL, (long)c.getMeasureValue(CN_B16, 0, 0));
		assertEquals(dvB16, (long)c.getMeasureValue(CN_B16, 1, 0));
		assertEquals(0xBBL, (long)c.getMeasureValue(CN_B16, 2, 0));
		assertEquals(Long.parseLong("XX", 36), (long)c.getMeasureValue(CN_B36, 0, 0));
		assertEquals(dvB36, (long)c.getMeasureValue(CN_B36, 1, 0));
		assertEquals(Long.parseLong("YY", 36), (long)c.getMeasureValue(CN_B36, 2, 0));
		assertEquals(objValue, (BmsTest.AnyType1)c.getMeasureValue(CN_OBJ, 0, 0));
		assertSame(objValue, (BmsTest.AnyType1)c.getMeasureValue(CN_OBJ, 0, 0));
		assertNull(c.getMeasureValue(CN_OBJ, 1, 0));
		assertEquals(objValue2, (BmsTest.AnyType2)c.getMeasureValue(CN_OBJ, 2, 0));
		assertSame(objValue2, (BmsTest.AnyType2)c.getMeasureValue(CN_OBJ, 2, 0));
	}

	// setMeasureValue(int, int, int, Object)
	// IllegalStateException 動作モードが編集モードではない
	@Test
	public void testSetMeasureValueIntIntIntObject_002() {
		var c = new BmsContent(spec());
		assertThrows(IllegalStateException.class, () -> c.setMeasureValue(CN_STR, 0, 0, ""));
	}

	// setMeasureValue(int, int, int, Object)
	// IllegalArgumentException BMS仕様にないチャンネル番号を指定した
	@Test
	public void testSetMeasureValueIntIntIntObject_003() {
		var c = new BmsContent(spec());
		c.beginEdit();
		assertThrows(IllegalArgumentException.class, () -> c.setMeasureValue(333, 0, 0, ""));
	}

	// setMeasureValue(int, int, int, Object)
	// IndexOutOfBoundsException チャンネルインデックスがマイナス値
	@Test
	public void testSetMeasureValueIntIntIntObject_004() {
		var c = new BmsContent(spec());
		c.beginEdit();
		assertThrows(IndexOutOfBoundsException.class, () -> c.setMeasureValue(CN_STR, -1, 0, ""));
	}

	// setMeasureValue(int, int, int, Object)
	// IndexOutOfBoundsException データ重複不許可のチャンネルで0以外のインデックスを指定した
	@Test
	public void testSetMeasureValueIntIntIntObject_005() {
		var c = new BmsContent(spec());
		c.beginEdit();
		assertThrows(IndexOutOfBoundsException.class, () -> c.setMeasureValue(CN_STRS, 1, 0, ""));
	}

	// setMeasureValue(int, int, int, Object)
	// IllegalArgumentException 小節番号が{@link BmsSpec#MEASURE_MIN}未満または{@link BmsSpec#MEASURE_MAX}超過
	@Test
	public void testSetMeasureValueIntIntIntObject_006() {
		var c = new BmsContent(spec());
		var ex = IllegalArgumentException.class;
		c.beginEdit();
		assertThrows(ex, () -> c.setMeasureValue(CN_STR, 0, BmsSpec.MEASURE_MIN - 1, ""));
		assertThrows(ex, () -> c.setMeasureValue(CN_STR, 0, BmsSpec.MEASURE_MAX + 1, ""));
	}

	// setMeasureValue(int, int, int, Object)
	// IllegalArgumentException チャンネルのデータ型が値型、任意型ではない
	@Test
	public void testSetMeasureValueIntIntIntObject_007() {
		var c = new BmsContent(spec());
		c.beginEdit();
		assertThrows(IllegalArgumentException.class, () -> c.setMeasureValue(11, 0, 0, ""));  // Playable channel (ARRAY36)
	}

	// setMeasureValue(int, int, int, Object)
	// ClassCastException valueをチャンネルのデータ型に変換できない
	@Test
	public void testSetMeasureValueIntIntIntObject_008() {
		var c = new BmsContent(spec());
		c.beginEdit();
		assertThrows(ClassCastException.class, () -> c.setMeasureValue(CN_INT, 0, 0, "unknown"));
	}

	// setMeasureValue(int, int, int, Object)
	// 小節長変更チャンネルへ最小値未満を設定するとIllegalArgumentExceptionがスローされること
	@Test
	public void testSetMeasureValueIntIntIntObject_Length_Underflow() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.setMeasureValue(2, 0, 0, BmsSpec.LENGTH_MIN);
		assertThrows(IllegalArgumentException.class, () -> c.setMeasureValue(2, 0, 1, BmsSpec.LENGTH_MIN - 0.000000001));
		assertEquals(BmsSpec.LENGTH_MIN, ((Number)c.getMeasureValue(2, 0)).doubleValue(), 0.000000001);
	}

	// setMeasureValue(int, int, int, Object)
	// 小節長変更チャンネルへ最大値超過を設定するとIllegalArgumentExceptionがスローされること
	@Test
	public void testSetMeasureValueIntIntIntObject_Length_Overflow() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.setMeasureValue(2, 0, 0, BmsSpec.LENGTH_MAX);
		assertThrows(IllegalArgumentException.class, () -> c.setMeasureValue(2, 0, 1, BmsSpec.LENGTH_MAX + 0.00000001));
		assertEquals(BmsSpec.LENGTH_MAX, ((Number)c.getMeasureValue(2, 0)).doubleValue(), 0.000000001);
	}

	// setMeasureValue(int, int, int, Object)
	// ノート追加後に小節長を縮小すると、縮小した場所にあったノートが自動消去されること
	@Test
	public void testSetMeasureValue_Normal_RemoveOutOfTick() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.setInitialBpm(120.0);
		// M=0 2秒
		c.putNote(CN_A36S, 0, 0, 48, 100);
		c.putNote(CN_A36S, 0, 0, 144, 200);
		// M=1 2秒 (この小節の長さを後で ***** [0.5 : 2/4拍子] *****にする)
		c.setMeasureValue(2, 1, 1.0);
		c.putNote(CN_A36S, 0, 1, 0, 300);
		c.putNote(CN_A36S, 0, 1, 48, 400);
		c.putNote(CN_A36S, 0, 1, 95, 500);
		c.putNote(CN_A36S, 0, 1, 96, 500);  // 消去対象
		c.putNote(CN_A36S, 0, 1, 97, 500);  // 消去対象
		c.putNote(CN_A36S, 0, 1, 144, 600);  // 消去対象
		c.putNote(CN_A16S, 0, 1, 0, 10);
		c.putNote(CN_A16S, 0, 1, 96, 20);  // 消去対象
		c.putNote(CN_A36, 1, 1, 0, 91);
		c.putNote(CN_A36, 2, 1, 96, 92);  // 消去対象
		// M=2 1.5秒 (この小節の長さを後で ***** [0.75 : 3/4拍子] *****にする
		//c.setMeasureValue(2, 2, 1.0);  // 初期値でのテストのため設定しない
		c.putNote(CN_A36S, 0, 2, 0, 300);
		c.putNote(CN_A36S, 0, 2, 48, 400);
		c.putNote(CN_A36S, 0, 2, 96, 500);
		c.putNote(CN_A36S, 0, 2, 143, 600);
		c.putNote(CN_A36S, 0, 2, 144, 600);  // 消去対象
		c.putNote(CN_A36S, 0, 2, 145, 600);  // 消去対象
		// M=3 2秒
		c.putNote(CN_A36S, 0, 3, 0, 700);
		c.putNote(CN_A36S, 0, 3, 96, 800);
		c.putNote(CN_A36S, 0, 3, 191, 900);
		c.endEdit();

		// この時点でのチェックを行っておく
		assertEquals(21, c.countNotes(n -> true));
		assertEquals(4, c.getMeasureCount());
		assertEquals(BmsSpec.TICK_COUNT_DEFAULT, c.getMeasureTickCount(1), 0.0);
		assertEquals(BmsSpec.TICK_COUNT_DEFAULT, c.getMeasureTickCount(2), 0.0);

		// 小節長の縮小を行ったことによる挙動の確認
		c.beginEdit();
		c.setMeasureValue(2, 1, 0.5);  // 2→1秒 T=96
		assertEquals(16, c.countNotes(n -> true));  // 範囲外になったノートが自動消去されること
		assertEquals((int)(BmsSpec.TICK_COUNT_DEFAULT * 0.5), c.getMeasureTickCount(1), 0.0);
		c.setMeasureValue(2, 2, 0.75);  // 2→1.5秒 T=144
		assertEquals(14, c.countNotes(n -> true));  // 範囲外になったノートが自動消去されること
		assertEquals((int)(BmsSpec.TICK_COUNT_DEFAULT * 0.75), c.getMeasureTickCount(2), 0.0);
		c.endEdit();

		// 小節長を戻しても自動消去されたノートが復活しないこと
		c.beginEdit();
		c.setMeasureValue(2, 1, 1.0);
		c.setMeasureValue(2, 2, 1.0);
		c.endEdit();
		assertNull(c.getNote(CN_A36S, 0, 1, 96));
		assertNull(c.getNote(CN_A36S, 0, 1, 97));
		assertNull(c.getNote(CN_A36S, 0, 1, 144));
		assertNull(c.getNote(CN_A16S, 0, 1, 96));
		assertNull(c.getNote(CN_A36, 2, 1, 96));
		assertNull(c.getNote(CN_A36S, 0, 2, 144));
		assertNull(c.getNote(CN_A36S, 0, 2, 145));
	}

	// containsMeasureValue(int, int)
	// 正常系のみ
	@Test
	public void testContainsMeasureValueIntInt_001() {
		var c = new BmsContent(spec());
		c.beginEdit();
		// サポートする全ての型
		c.setMeasureValue(CN_INTS, 0, 0, 100L);
		c.setMeasureValue(CN_NUMS, 0, 1, 123.456);
		c.setMeasureValue(CN_STRS, 0, 2, "hoge");
		c.setMeasureValue(CN_B16S, 0, 3, 0xABL);
		c.setMeasureValue(CN_B36S, 0, 4, Long.parseLong("XY", 36));
		c.setMeasureValue(CN_OBJS, 0, 5, new AnyType1(0, 0));
		// 未設定の小節
		//c.setMeasureValue(CN_STRS, 0, 10, "NOT-SET");
		// 設定の後消去
		c.setMeasureValue(CN_NUMS, 0, 0, 456.789);
		c.setMeasureValue(CN_NUMS, 0, 0, (Long)null);
		c.endEdit();

		// アサーション
		assertTrue(c.containsMeasureValue(CN_INTS, 0));
		assertTrue(c.containsMeasureValue(CN_NUMS, 1));
		assertTrue(c.containsMeasureValue(CN_STRS, 2));
		assertTrue(c.containsMeasureValue(CN_B16S, 3));
		assertTrue(c.containsMeasureValue(CN_B36S, 4));
		assertTrue(c.containsMeasureValue(CN_OBJS, 5));

		assertFalse(c.containsMeasureValue(CN_STRS, 10));

		assertFalse(c.containsMeasureValue(CN_NUMS, 0));
	}

	// containsMeasureValue(int, int, int)
	// 正常
	@Test
	public void testContainsMeasureValueIntIntInt_001() {
		var c = new BmsContent(spec());
		c.beginEdit();
		// サポートする全ての型
		c.setMeasureValue(CN_INTS, 0, 0, 100L);
		c.setMeasureValue(CN_NUMS, 0, 1, 123.456);
		c.setMeasureValue(CN_STRS, 0, 2, "hoge");
		c.setMeasureValue(CN_B16S, 0, 3, 0xABL);
		c.setMeasureValue(CN_B36S, 0, 4, Long.parseLong("XY", 36));
		c.setMeasureValue(CN_OBJS, 0, 5, new AnyType1(1, 1));
		// チャンネルインデックス1以上
		c.setMeasureValue(CN_INT, 1, 0, 100L);
		c.setMeasureValue(CN_INT, 2, 0, 200L);
		c.setMeasureValue(CN_INT, 5, 0, 300L);
		// 未設定の小節
		//c.setMeasureValue(CN_STRS, 0, 10, "NOT-SET");
		// 設定の後消去
		c.setMeasureValue(CN_NUMS, 0, 0, 456.789);
		c.setMeasureValue(CN_NUMS, 0, 0, (Long)null);
		c.endEdit();

		// アサーション
		assertTrue(c.containsMeasureValue(CN_INTS, 0, 0));
		assertTrue(c.containsMeasureValue(CN_NUMS, 0, 1));
		assertTrue(c.containsMeasureValue(CN_STRS, 0, 2));
		assertTrue(c.containsMeasureValue(CN_B16S, 0, 3));
		assertTrue(c.containsMeasureValue(CN_B36S, 0, 4));
		assertTrue(c.containsMeasureValue(CN_OBJS, 0, 5));

		assertFalse(c.containsMeasureValue(CN_INT, 0, 0));
		assertTrue(c.containsMeasureValue(CN_INT, 1, 0));
		assertTrue(c.containsMeasureValue(CN_INT, 2, 0));
		assertFalse(c.containsMeasureValue(CN_INT, 3, 0));
		assertFalse(c.containsMeasureValue(CN_INT, 4, 0));
		assertTrue(c.containsMeasureValue(CN_INT, 5, 0));
		assertFalse(c.containsMeasureValue(CN_INT, 6, 0));  // Exceptionが発生しないこと

		assertFalse(c.containsMeasureValue(CN_STRS, 0, 10));

		assertFalse(c.containsMeasureValue(CN_NUMS, 0, 0));
	}

	// containsMeasureValue(int, int, int)
	// IllegalArgumentException BMS仕様にないチャンネル番号を指定した
	@Test
	public void testContainsMeasureValueIntIntInt_002() {
		var c = new BmsContent(spec());
		assertThrows(IllegalArgumentException.class, () -> c.containsMeasureValue(999, 0, 0));
	}

	// containsMeasureValue(int, int, int)
	// IndexOutOfBoundsException チャンネルインデックスがマイナス値
	@Test
	public void testContainsMeasureValueIntIntInt_003() {
		var c = new BmsContent(spec());
		assertThrows(IndexOutOfBoundsException.class, () -> c.containsMeasureValue(CN_INT, -1, 0));
	}

	// containsMeasureValue(int, int, int)
	// IndexOutOfBoundsException 重複不許可のチャンネルで0以外のインデックスを指定した
	@Test
	public void testContainsMeasureValueIntIntInt_004() {
		var c = new BmsContent(spec());
		assertThrows(IndexOutOfBoundsException.class, () -> c.containsMeasureValue(CN_INTS, 1, 0));
	}

	// containsMeasureValue(int, int, int)
	// IllegalArgumentException 指定チャンネルのデータ型が値型ではない
	@Test
	public void testContainsMeasureValueIntIntInt_005() {
		var c = new BmsContent(spec());
		assertThrows(IllegalArgumentException.class, () -> c.containsMeasureValue(CN_A36S, 0, 0));
	}

	// containsMeasureValue(int, int, int)
	// IllegalArgumentException 小節番号が{@link BmsSpec#MEASURE_MIN}未満または{@link BmsSpec#MEASURE_MAX}超過
	@Test
	public void testContainsMeasureValueIntIntInt_006() {
		var c = new BmsContent(spec());
		assertThrows(IllegalArgumentException.class, () -> c.containsMeasureValue(CN_STRS, 0, BmsSpec.MEASURE_MAX + 1));
	}

	// removeNote(int, int, double)
	// 正常系のみ
	@Test
	public void testRemoveNoteIntIntDouble_001() {
		var testData = getNormalNoteTestData(true);
		var c = new BmsContent(spec());
		c.beginEdit();
		testData.forEach(t -> c.putNote(t.channel, t.index, t.measure, t.tick, t.value));
		c.endEdit();
		c.beginEdit();
		testData.forEach(t -> {
			var rn = c.removeNote(t.channel, t.measure, t.tick);
			assertTrue(rn);
		});
		c.endEdit();
	}

	// removeNote(int, BmsAt)
	// 正常系のみ
	@Test
	public void testRemoveNoteIntBmsAt_001() {
		var testData = getNormalNoteTestData(true);
		var c = new BmsContent(spec());
		c.beginEdit();
		testData.forEach(t -> c.putNote(t.channel, t.index, t.measure, t.tick, t.value));
		c.endEdit();
		c.beginEdit();
		testData.forEach(t -> {
			var rn = c.removeNote(t.channel, new BmsPoint(t.measure, t.tick));
			assertTrue(rn);
		});
		c.endEdit();
	}

	// removeNote(int, int, BmsAt)
	// 正常系のみ
	@Test
	public void testRemoveNoteIntIntBmsAt_001() {
		var testData = getNormalNoteTestData(false);
		var c = new BmsContent(spec());
		c.beginEdit();
		testData.forEach(t -> c.putNote(t.channel, t.index, t.measure, t.tick, t.value));
		c.endEdit();
		c.beginEdit();
		testData.forEach(t -> {
			var rn = c.removeNote(t.channel, t.index, new BmsPoint(t.measure, t.tick));
			assertTrue(rn);
		});
		c.endEdit();
	}

	// removeNote(int, int, int, double)
	// 正常
	@Test
	public void testRemoveNoteIntIntIntDouble_001() {
		var testData = getNormalNoteTestData(false);
		var c = new BmsContent(spec());
		c.beginEdit();
		testData.forEach(t -> c.putNote(t.channel, t.index, t.measure, t.tick, t.value));
		c.endEdit();

		// 該当するノートがない場合はfalse
		c.beginEdit();
		assertFalse(c.removeNote(CN_A36, 0, 0, 191));
		c.endEdit();

		// 正常系のテスト
		c.beginEdit();
		testData.forEach(t -> {
			var rn = c.removeNote(t.channel, t.index, t.measure, t.tick);
			assertTrue(rn);
		});
		c.endEdit();

		// 全ノートを消去したので小節数は0
		assertEquals(0, c.getMeasureCount());
	}

	// removeNote(int, int, int, double)
	// IllegalStateException 動作モードが編集モードではない
	@Test
	public void testRemoveNoteIntIntIntDouble_002() {
		var c = new BmsContent(spec());
		assertThrows(IllegalStateException.class, () -> c.removeNote(1, 0, 0, 0));
	}

	// removeNote(int, int, int, double)
	// IllegalArgumentException BMS仕様にないチャンネル番号を指定した
	@Test
	public void testRemoveNoteIntIntIntDouble_003() {
		var c = new BmsContent(spec());
		c.beginEdit();
		assertThrows(IllegalArgumentException.class, () -> c.removeNote(333, 0, 0, 0));
	}

	// removeNote(int, int, int, double)
	// IndexOutOfBoundsException チャンネルインデックスがマイナス値
	@Test
	public void testRemoveNoteIntIntIntDouble_004() {
		var c = new BmsContent(spec());
		c.beginEdit();
		assertThrows(IndexOutOfBoundsException.class, () -> c.removeNote(1, -1, 0, 0));
	}

	// removeNote(int, int, int, double)
	// IndexOutOfBoundsException 重複不許可のチャンネルで0以外のインデックスを指定した
	@Test
	public void testRemoveNoteIntIntIntDouble_005() {
		var c = new BmsContent(spec());
		c.beginEdit();
		assertThrows(IndexOutOfBoundsException.class, () -> c.removeNote(11, 1, 0, 0));
	}

	// removeNote(int, int, int, double)
	// IllegalArgumentException 小節番号が{@link BmsSpec#MEASURE_MIN}未満または{@link BmsSpec#MEASURE_MAX}超過
	@Test
	public void testRemoveNoteIntIntIntDouble_006() {
		var c = new BmsContent(spec());
		var ex = IllegalArgumentException.class;
		c.beginEdit();
		assertThrows(ex, () -> c.removeNote(1, 0, BmsSpec.MEASURE_MIN - 1, 0));
		assertThrows(ex, () -> c.removeNote(1, 0, BmsSpec.MEASURE_MAX + 1, 0));
	}

	// removeNote(int, int, int, double)
	// IllegalArgumentException 小節の刻み位置がマイナス値または当該小節の刻み数以上
	@Test
	public void testRemoveNoteIntIntIntDouble_007() {
		var ex = IllegalArgumentException.class;

		// 4/4拍子
		var c = new BmsContent(spec());
		c.beginEdit();
		assertThrows(ex, () -> c.removeNote(1, 0, 0, -1));
		assertThrows(ex, () -> c.removeNote(1, 0, 0, 192));

		// 小節長2倍
		var c2 = new BmsContent(spec());
		c2.beginEdit();
		c2.setMeasureValue(2, 0, 2.0);  // 0小節の長さを2倍に
		c2.endEdit();
		c2.beginEdit();
		assertThrows(ex, () -> c2.removeNote(1, 0, 0, -1));
		assertThrows(ex, () -> c2.removeNote(1, 0, 0, 384));
	}

	// removeNote(int, int, int, double)
	// IllegalArgumentException 指定チャンネルのデータ型が配列型ではない
	@Test
	public void testRemoveNoteIntIntIntDouble_008() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.putNote(11, 0, 0, 0, 1000);
		assertThrows(IllegalArgumentException.class, () -> c.removeNote(2, 0, 0, 0));
	}

	// removeNote(Tester)
	// 正常
	@Test
	public void testRemoveNoteTester_001() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.putNote(1, 0, 0, 0, 100);
		c.putNote(1, 1, 0, 48, 300);
		c.putNote(11, 0, 3, 0, 10);
		c.putNote(11, 0, 3, 96, 30);
		c.putNote(11, 0, 10, 144, 50);
		c.putNote(21, 0, 10, 0, 1);
		c.putNote(21, 0, 10, 96, 3);
		c.putNote(21, 0, 7, 144, 5);
		c.putNote(31, 0, 7, 0, 1000);
		c.putNote(31, 0, 7, 96, 1100);
		c.putNote(31, 0, 7, 144, 1200);
		c.endEdit();

		c.beginEdit();
		var ret = c.removeNote(n -> (n.getIndex() == 0) && ((n.getChannel() == 1) || (n.getChannel() == 21)));
		c.endEdit();

		assertEquals(4, ret);
		assertNull(c.getNote(1, 0, 0, 0));
		assertNotNull(c.getNote(1, 1, 0, 48));
		assertNotNull(c.getNote(11, 0, 3, 0));
		assertNotNull(c.getNote(11, 0, 3, 96));
		assertNotNull(c.getNote(11, 0, 10, 144));
		assertNull(c.getNote(21, 0, 10, 0));
		assertNull(c.getNote(21, 0, 10, 96));
		assertNull(c.getNote(21, 0, 7, 144));
		assertNotNull(c.getNote(31, 0, 7, 0));
		assertNotNull(c.getNote(31, 0, 7, 96));
		assertNotNull(c.getNote(31, 0, 7, 144));
	}

	// removeNote(Tester)
	// IllegalStateException 動作モードが編集モードではない
	@Test
	public void testRemoveNoteTester_002() {
		var c = new BmsContent(spec());
		assertThrows(IllegalStateException.class, () -> c.removeNote(n -> true));
	}

	// removeNote(Tester)
	// NullPointerException isRemoveTargetがnull
	@Test
	public void testRemoveNoteTester_003() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.putNote(11, 0, 0, 100);
		assertThrows(NullPointerException.class, () -> c.removeNote(null));
	}

	// removeNote(int, int, Tester)
	// 正常系のみ
	@Test
	public void testRemoveNoteIntIntTester_001() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.putNote(1, 0, 0, 0, 100);
		c.putNote(1, 1, 0, 48, 300);
		c.putNote(11, 0, 3, 0, 10);
		c.putNote(11, 0, 3, 96, 30);
		c.putNote(11, 0, 10, 144, 50);
		c.putNote(21, 0, 10, 0, 1);
		c.putNote(21, 0, 10, 96, 3);
		c.putNote(21, 0, 7, 144, 5);
		c.putNote(31, 0, 7, 0, 1000);
		c.putNote(31, 0, 7, 96, 1100);
		c.putNote(31, 0, 7, 144, 1200);
		c.endEdit();

		c.beginEdit();
		var ret = c.removeNote(0, 10, n -> true);
		c.endEdit();

		assertEquals(8, ret);
		assertNull(c.getNote(1, 0, 0, 0));
		assertNull(c.getNote(1, 1, 0, 48));
		assertNull(c.getNote(11, 0, 3, 0));
		assertNull(c.getNote(11, 0, 3, 96));
		assertNotNull(c.getNote(11, 0, 10, 144));
		assertNotNull(c.getNote(21, 0, 10, 0));
		assertNotNull(c.getNote(21, 0, 10, 96));
		assertNull(c.getNote(21, 0, 7, 144));
		assertNull(c.getNote(31, 0, 7, 0));
		assertNull(c.getNote(31, 0, 7, 96));
		assertNull(c.getNote(31, 0, 7, 144));
	}

	// removeNote(int, int, int, int, Tester)
	// 正常
	@Test
	public void testRemoveNoteIntIntIntIntTester_001() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.putNote(1, 0, 0, 0, 100);
		c.putNote(1, 1, 0, 48, 300);
		c.putNote(11, 0, 3, 0, 10);
		c.putNote(11, 0, 3, 96, 30);
		c.putNote(11, 0, 10, 144, 50);
		c.putNote(21, 0, 10, 0, 1);
		c.putNote(21, 0, 10, 96, 3);
		c.putNote(21, 0, 7, 144, 5);
		c.putNote(31, 0, 7, 0, 1000);
		c.putNote(31, 0, 7, 96, 1100);
		c.putNote(31, 0, 7, 144, 1200);
		c.endEdit();

		c.beginEdit();
		var ret = c.removeNote(1, 21, 0, 10, n -> true);
		c.endEdit();

		assertEquals(4, ret);
		assertNull(c.getNote(1, 0, 0, 0));
		assertNull(c.getNote(1, 1, 0, 48));
		assertNull(c.getNote(11, 0, 3, 0));
		assertNull(c.getNote(11, 0, 3, 96));
		assertNotNull(c.getNote(11, 0, 10, 144));
		assertNotNull(c.getNote(21, 0, 10, 0));
		assertNotNull(c.getNote(21, 0, 10, 96));
		assertNotNull(c.getNote(21, 0, 7, 144));
		assertNotNull(c.getNote(31, 0, 7, 0));
		assertNotNull(c.getNote(31, 0, 7, 96));
		assertNotNull(c.getNote(31, 0, 7, 144));
	}

	// removeNote(int, int, int, int, Tester)
	// IllegalStateException 動作モードが編集モードではない
	@Test
	public void testRemoveNoteIntIntIntIntTester_002() {
		var c = new BmsContent(spec());
		assertThrows(IllegalStateException.class, () -> c.removeNote(1, 1, 0, 0, n -> true));
	}

	// removeNote(int, int, int, int, Tester)
	// IllegalArgumentException channelBeginに小節データ・ノートを登録できないチャンネル番号を指定した
	@Test
	public void testRemoveNoteIntIntIntIntTester_003() {
		var c = new BmsContent(spec());
		var ex = IllegalArgumentException.class;
		c.beginEdit();
		assertThrows(ex, () -> c.removeNote(BmsSpec.CHANNEL_MIN - 1, 1, 0, 0, n -> true));
		assertThrows(ex, () -> c.removeNote(BmsSpec.CHANNEL_MAX + 1, 1, 0, 0, n -> true));
	}

	// removeNote(int, int, int, int, Tester)
	// IllegalArgumentException channelEndに{@link BmsSpec#CHANNEL_MAX}より大きいチャンネル番号を指定した
	@Test
	public void testRemoveNoteIntIntIntIntTester_004() {
		var c = new BmsContent(spec());
		var ex = IllegalArgumentException.class;
		c.beginEdit();
		assertThrows(ex, () -> c.removeNote(1, BmsSpec.CHANNEL_MIN - 1, 0, 0, n -> true));
		assertThrows(ex, () -> c.removeNote(1, BmsSpec.CHANNEL_MAX + 2, 0, 0, n -> true));
	}

	// removeNote(int, int, int, int, Tester)
	// IllegalArgumentException measureBeginにノート・小節データの存在し得ない小節番号を指定した
	@Test
	public void testRemoveNoteIntIntIntIntTester_005() {
		var c = new BmsContent(spec());
		var ex = IllegalArgumentException.class;
		c.beginEdit();
		assertThrows(ex, () -> c.removeNote(1, 1, BmsSpec.MEASURE_MIN - 1, 0, n -> true));
		assertThrows(ex, () -> c.removeNote(1, 1, BmsSpec.MEASURE_MAX_COUNT + 1, 0, n -> true));
	}

	// removeNote(int, int, int, int, Tester)
	// IllegalArgumentException measureEndに現在の小節数より大きい小節番号を指定した
	@Test
	public void testRemoveNoteIntIntIntIntTester_006() {
		var c = new BmsContent(spec());
		var ex = IllegalArgumentException.class;
		c.beginEdit();
		assertThrows(ex, () -> c.removeNote(1, 1, 0, BmsSpec.MEASURE_MIN - 1, n -> true));
		assertThrows(ex, () -> c.removeNote(1, 1, 0, BmsSpec.MEASURE_MAX_COUNT + 1, n -> true));
	}

	// removeNote(int, int, int, int, Tester)
	// NullPointerException isRemoveTargetがnull
	@Test
	public void testRemoveNoteIntIntIntIntTester_007() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.putNote(11, 0, 0, 100);
		assertThrows(NullPointerException.class, () -> c.removeNote(1, 1, 0, 0, null));
	}

	// insertMeasure(int)
	// 正常系のみ
	@Test
	public void testInsertMeasureInt_001() {
		// 通常の挿入処理
		var c = new BmsContent(spec());
		c.beginEdit();
		c.putNote(1, 0, 0, 0, 199);
		c.insertMeasure(0);
		c.putNote(1, 0, 0, 0, 299);
		c.endEdit();
		assertNotNull(c.getNote(1, 0, 0, 0));
		assertEquals(299, c.getNote(1, 0, 0, 0).getValue());
		assertNotNull(c.getNote(1, 0, 1, 0));
		assertEquals(199, c.getNote(1, 0, 1, 0).getValue());

		// 空の譜面に挿入しても何もない
		c = new BmsContent(spec());
		c.beginEdit();
		c.insertMeasure(0);
		c.endEdit();
		assertEquals(0, c.getMeasureCount());

		// 挿入された小節にはノートが無く、必ず4/4拍子
		c = new BmsContent(spec());
		c.beginEdit();
		c.setMeasureValue(2, 0, 2.0);
		c.putNote(1, 0, 0, 0, 100);
		c.insertMeasure(0);
		c.endEdit();
		assertEquals(0, c.countNotes(n -> n.getMeasure() == 0));
		assertEquals(1.0, (double)c.getMeasureValue(2, 0), 0.0);
	}

	// insertMeasure(int, int)
	// 正常
	@Test
	public void testInsertMeasureIntInt_001() {
		// 通常の挿入処理
		var c = new BmsContent(spec());
		c.beginEdit();
		c.putNote(1, 0, 0, 0, 100);
		c.insertMeasure(0, 2);
		c.putNote(1, 0, 0, 0, 200);
		c.endEdit();
		assertNotNull(c.getNote(1, 0, 0, 0));
		assertEquals(200, c.getNote(1, 0, 0, 0).getValue());
		assertNotNull(c.getNote(1, 0, 2, 0));
		assertEquals(100, c.getNote(1, 0, 2, 0).getValue());

		// 空の譜面に挿入しても何もない
		c = new BmsContent(spec());
		c.beginEdit();
		c.insertMeasure(0, 1);
		c.endEdit();
		assertEquals(0, c.getMeasureCount());

		// 挿入された小節にはノートが無く、必ず4/4拍子
		c = new BmsContent(spec());
		c.beginEdit();
		c.setMeasureValue(2, 0, 2.0);
		c.putNote(1, 0, 0, 0, 100);
		c.insertMeasure(0, 1);
		c.endEdit();
		assertEquals(0, c.countNotes(n -> n.getMeasure() == 0));
		assertEquals(1.0, (double)c.getMeasureValue(2, 0), 0.0);
	}

	// insertMeasure(int, int)
	// IllegalStateException 動作モードが編集モードではない
	@Test
	public void testInsertMeasureIntInt_002() {
		var c = new BmsContent(spec());
		assertThrows(IllegalStateException.class, () -> c.insertMeasure(0, 1));
	}

	// insertMeasure(int, int)
	// IllegalArgumentException 挿入位置に現在の小節数より大きい小節番号を指定した
	@Test
	public void testInsertMeasureIntInt_003() {
		var c = new BmsContent(spec());
		c.beginEdit();
		assertThrows(IllegalArgumentException.class, () -> c.insertMeasure(1, 1));
	}

	// insertMeasure(int, int)
	// IllegalArgumentException 挿入する小節数にマイナス値を指定した
	@Test
	public void testInsertMeasureIntInt_004() {
		var c = new BmsContent(spec());
		c.beginEdit();
		assertThrows(IllegalArgumentException.class, () -> c.insertMeasure(1, -1));
	}

	// insertMeasure(int, int)
	// IllegalArgumentException 挿入により小節数が{@link BmsSpec#MEASURE_MAX_COUNT}を超える
	@Test
	public void testInsertMeasureIntInt_005() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.putNote(1, 0, 0.0, 1000);
		c.putNote(1, 1, 0.0, 1100);
		c.endEdit();
		c.beginEdit();
		assertThrows(IllegalArgumentException.class, () -> c.insertMeasure(0, BmsSpec.MEASURE_MAX_COUNT - 1));  // MEASURE_MAX_COUNT + 1 小節になる想定
	}

	// removeMeasure(int)
	// 正常系のみ
	@Test
	public void testRemoveMeasureInt_001() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.putNote(1, 0, 0, 0, 100);
		c.putNote(1, 0, 1, 0, 200);
		c.putNote(1, 0, 2, 0, 300);
		c.removeMeasure(1);
		c.endEdit();
		assertEquals(2, c.countNotes(n -> true));
		assertNotNull(c.getNote(1, 0, 0, 0));
		assertEquals(100, c.getNote(1, 0, 0, 0).getValue());
		assertNotNull(c.getNote(1, 0, 1, 0));
		assertEquals(300, c.getNote(1, 0, 1, 0).getValue());
	}

	// removeMeasure(int, int)
	// 正常
	@Test
	public void testRemoveMeasureIntInt_001() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.putNote(1, 0, 0, 0, 100);
		c.putNote(1, 0, 1, 0, 200);
		c.putNote(1, 0, 2, 0, 300);
		c.removeMeasure(0, 2);
		c.endEdit();
		assertEquals(1, c.countNotes(n -> true));
		assertNotNull(c.getNote(1, 0, 0, 0));
		assertEquals(300, c.getNote(1, 0, 0, 0).getValue());
	}

	// removeMeasure(int, int)
	// IllegalStateException 動作モードが編集モードではない
	@Test
	public void testRemoveMeasureIntInt_002() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.putNote(1, 0, 0, 0, 100);
		c.endEdit();
		assertThrows(IllegalStateException.class, () -> c.removeMeasure(0, 1));
	}

	// removeMeasure(int, int)
	// IllegalArgumentException 消去位置に現在の小節数より大きい小節番号を指定した
	@Test
	public void testRemoveMeasureIntInt_003() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.putNote(1, 0, 0, 0, 100);
		assertThrows(IllegalArgumentException.class, () -> c.removeMeasure(1, 1));
	}

	// removeMeasure(int, int)
	// IllegalArgumentException 消去する小節数にマイナス値を指定した
	@Test
	public void testRemoveMeasureIntInt_004() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.putNote(1, 0, 0, 0, 100);
		assertThrows(IllegalArgumentException.class, () -> c.removeMeasure(0, -1));
	}

	// removeMeasure(int, int)
	// IllegalArgumentException 存在しない小節を消去しようとした
	@Test
	public void testRemoveMeasureIntInt_005() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.putNote(1, 0, 0, 0, 100);
		assertThrows(IllegalArgumentException.class, () -> c.removeMeasure(0, 2));
	}

	private static class BmsNote_RemoveMeasureCallOnUpdate extends BmsNote {
		static boolean sIsEnableCheck = false;
		static boolean[] sIsCalledFlags = new boolean[8];

		@Override
		protected BmsNote onNewInstance() {
			return new BmsNote_RemoveMeasureCallOnUpdate();
		}

		@Override
		protected void onCreate() {
			if (!sIsEnableCheck) {
				return;
			}
			switch (getValue()) {
			case 1:
				sIsCalledFlags[0] = true;
				break;
			case 2:
				sIsCalledFlags[1] = true;
				break;
			case 3:
				sIsCalledFlags[2] = true;
				break;
			case 4:
				sIsCalledFlags[3] = true;
				break;
			case 5:
				assertEquals(15, getChannel());
				assertEquals(0, getIndex());
				assertEquals(6, getMeasure());
				assertEquals(80.0, getTick(), 0.0);
				sIsCalledFlags[4] = true;
				break;
			case 6:
				assertEquals(16, getChannel());
				assertEquals(0, getIndex());
				assertEquals(7, getMeasure());
				assertEquals(96.0, getTick(), 0.0);
				sIsCalledFlags[5] = true;
				break;
			case 7:
				assertEquals(CN_A36, getChannel());
				assertEquals(0, getIndex());
				assertEquals(11, getMeasure());
				assertEquals(112.0, getTick(), 0.0);
				sIsCalledFlags[6] = true;
				break;
			case 8:
				assertEquals(CN_A36, getChannel());
				assertEquals(1, getIndex());
				assertEquals(13, getMeasure());
				assertEquals(128.0, getTick(), 0.0);
				sIsCalledFlags[7] = true;
				break;
			default:
				fail("Detected unknown value. Is test data correctly?");
			}
		}
	}

	// removeMeasure(int, int)
	// 小節を除去すると、除去小節より後ろの小節にあるノート全てでonCreate()が呼ばれる
	@Test
	public void testRemoveMeasure_CallOnUpdate() {
		BmsNote_RemoveMeasureCallOnUpdate.sIsEnableCheck = false;
		Arrays.fill(BmsNote_RemoveMeasureCallOnUpdate.sIsCalledFlags, false);

		var c = new BmsContent(spec());
		c.beginEdit();
		c.putNote(11, 0, 1, 16, 1, () -> new BmsNote_RemoveMeasureCallOnUpdate());
		c.putNote(12, 0, 2, 32, 2, () -> new BmsNote_RemoveMeasureCallOnUpdate());
		c.putNote(13, 0, 4, 48, 3, () -> new BmsNote_RemoveMeasureCallOnUpdate());  // これを消去する
		c.putNote(14, 0, 5, 64, 4, () -> new BmsNote_RemoveMeasureCallOnUpdate());  // これを消去する
		c.putNote(15, 0, 8, 80, 5, () -> new BmsNote_RemoveMeasureCallOnUpdate());
		c.putNote(16, 0, 9, 96, 6, () -> new BmsNote_RemoveMeasureCallOnUpdate());
		c.putNote(CN_A36, 0, 13, 112, 7, () -> new BmsNote_RemoveMeasureCallOnUpdate());
		c.putNote(CN_A36, 1, 15, 128, 8, () -> new BmsNote_RemoveMeasureCallOnUpdate());

		BmsNote_RemoveMeasureCallOnUpdate.sIsEnableCheck = true;
		c.removeMeasure(4, 2);
		c.endEdit();

		// OnCreate()の呼び出しチェック
		assertFalse(BmsNote_RemoveMeasureCallOnUpdate.sIsCalledFlags[0]);
		assertFalse(BmsNote_RemoveMeasureCallOnUpdate.sIsCalledFlags[1]);
		assertFalse(BmsNote_RemoveMeasureCallOnUpdate.sIsCalledFlags[2]);
		assertFalse(BmsNote_RemoveMeasureCallOnUpdate.sIsCalledFlags[3]);
		assertTrue(BmsNote_RemoveMeasureCallOnUpdate.sIsCalledFlags[4]);
		assertTrue(BmsNote_RemoveMeasureCallOnUpdate.sIsCalledFlags[5]);
		assertTrue(BmsNote_RemoveMeasureCallOnUpdate.sIsCalledFlags[6]);
		assertTrue(BmsNote_RemoveMeasureCallOnUpdate.sIsCalledFlags[7]);
	}

	// swapChannel(int, int)
	// 正常系のみ
	@Test
	public void testSwapChannelIntInt_001() {
		var c = new BmsContent(spec());
		c.beginEdit();
		// 小節値の入れ替えテスト用
		c.setMeasureValue(CN_INT, 0, 100L);
		c.setMeasureValue(CN_INTS, 0, 500L);
		// 譜面の入れ替えテスト用
		c.putNote(11, 0, 0.0, 100);
		c.putNote(11, 1, 48.0, 200);
		c.putNote(11, 2, 96.0, 300);
		c.putNote(21, 0, 32.0, 800);
		c.putNote(21, 1, 16.0, 700);
		c.putNote(21, 2, 0.0, 600);

		// 小節値の入れ替え
		c.swapChannel(CN_INT, CN_INTS);
		assertEquals(500L, (long)c.getMeasureValue(CN_INT, 0));
		assertEquals(100L, (long)c.getMeasureValue(CN_INTS, 0));

		// 譜面の入れ替え(不可能チャンネル同士)
		c.swapChannel(11, 21);
		assertEquals(800, c.getNote(11, 0, 32.0).getValue());
		assertEquals(700, c.getNote(11, 1, 16.0).getValue());
		assertEquals(600, c.getNote(11, 2, 0.0).getValue());
		assertEquals(100, c.getNote(21, 0, 0.0).getValue());
		assertEquals(200, c.getNote(21, 1, 48.0).getValue());
		assertEquals(300, c.getNote(21, 2, 96.0).getValue());
	}

	// swapChannel(int, int, int, int)
	// 正常
	@Test
	public void testSwapChannelIntIntIntInt_001() {
		var c = new BmsContent(spec());
		c.beginEdit();
		// 小節値の入れ替えテスト用
		c.setMeasureValue(CN_INT, 1, 0, 100L);
		c.setMeasureValue(CN_INTS, 0, 0, 500L);
		// 譜面の入れ替えテスト用
		c.putNote(11, 0, 0, 0, 100);
		c.putNote(11, 0, 1, 48, 200);
		c.putNote(11, 0, 2, 96, 300);
		c.putNote(21, 0, 0, 32, 800);
		c.putNote(21, 0, 1, 16, 700);
		c.putNote(21, 0, 2, 0, 600);
		// 譜面で重複可能チャンネルの入れ替えテスト用
		c.putNote(CN_A36, 1, 0, 0, 100);
		c.putNote(CN_A36, 1, 1, 96, 200);
		c.putNote(CN_A36S, 0, 2, 0, 10);
		c.putNote(CN_A36S, 0, 3, 48, 20);

		// 小節値の入れ替え
		c.swapChannel(CN_INT, 1, CN_INTS, 0);
		assertEquals(500L, (long)c.getMeasureValue(CN_INT, 1, 0));
		assertEquals(100L, (long)c.getMeasureValue(CN_INTS, 0, 0));

		// 譜面の入れ替え(不可能チャンネル同士)
		c.swapChannel(11, 0, 21, 0);
		assertEquals(800, c.getNote(11, 0, 0, 32).getValue());
		assertEquals(700, c.getNote(11, 0, 1, 16).getValue());
		assertEquals(600, c.getNote(11, 0, 2, 0).getValue());
		assertEquals(100, c.getNote(21, 0, 0, 0).getValue());
		assertEquals(200, c.getNote(21, 0, 1, 48).getValue());
		assertEquals(300, c.getNote(21, 0, 2, 96).getValue());

		// 譜面の入れ替え(重複可能チャンネルあり)
		c.swapChannel(CN_A36, 1, CN_A36S, 0);
		assertEquals(10, c.getNote(CN_A36, 1, 2, 0).getValue());
		assertEquals(20, c.getNote(CN_A36, 1, 3, 48).getValue());
		assertEquals(100, c.getNote(CN_A36S, 0, 0, 0).getValue());
		assertEquals(200, c.getNote(CN_A36S, 0, 1, 96).getValue());
	}

	// swapChannel(int, int, int, int)
	// IllegalStateException 動作モードが編集モードではない
	@Test
	public void testSwapChannelIntIntIntInt_002() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.setMeasureValue(CN_INT, 1, 0, 100L);
		c.setMeasureValue(CN_INTS, 0, 0, 500L);
		c.endEdit();
		assertThrows(IllegalStateException.class, () -> c.swapChannel(CN_INT, 1, CN_INTS, 0));
	}

	// swapChannel(int, int, int, int)
	// IllegalArgumentException BMS仕様にないチャンネル番号を指定した
	@Test
	public void testSwapChannelIntIntIntInt_003() {
		var c = new BmsContent(spec());
		var ex = IllegalArgumentException.class;
		c.beginEdit();
		assertThrows(ex, () -> c.swapChannel(999, 0, CN_INTS, 0));
		assertThrows(ex, () -> c.swapChannel(CN_INT, 0, 999, 0));
	}

	// swapChannel(int, int, int, int)
	// IndexOutOfBoundsException チャンネルインデックスがマイナス値
	@Test
	public void testSwapChannelIntIntIntInt_004() {
		var c = new BmsContent(spec());
		var ex = IndexOutOfBoundsException.class;
		c.beginEdit();
		c.setMeasureValue(CN_INT, 1, 0, 100L);
		c.setMeasureValue(CN_INTS, 0, 0, 500L);
		assertThrows(ex, () -> c.swapChannel(CN_INT, -1, CN_INTS, 0));
		assertThrows(ex, () -> c.swapChannel(CN_INT, 1, CN_INTS, -1));
	}

	// swapChannel(int, int, int, int)
	// IndexOutOfBoundsException 重複不許可のチャンネルで0以外のインデックスを指定した
	@Test
	public void testSwapChannelIntIntIntInt_005() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.setMeasureValue(CN_INT, 1, 0, 100L);
		c.setMeasureValue(CN_INTS, 0, 0, 500L);
		assertThrows(IndexOutOfBoundsException.class, () -> c.swapChannel(CN_INT, 1, CN_INTS, 1));
	}

	// swapChannel(int, int, int, int)
	// IllegalArgumentException 指定チャンネルが小節長変更・BPM変更・譜面停止のいずれかだった
	@Test
	public void testSwapChannelIntIntIntInt_006() {
		var c = new BmsContent(spec());
		var ex = IllegalArgumentException.class;
		c.beginEdit();
		c.setMeasureValue(CN_INT, 0, 0, 100L);
		c.setMeasureValue(CN_NUM, 0, 0, 200.0);
		assertThrows(ex, () -> c.swapChannel(CN_NUM, 0, 2, 0));
		assertThrows(ex, () -> c.swapChannel(CN_NUM, 0, 8, 0));
		assertThrows(ex, () -> c.swapChannel(CN_INT, 0, 9, 0));
	}

	// swapChannel(int, int, int, int)
	// IllegalArgumentException チャンネル1,2のデータ型が一致しない
	@Test
	public void testSwapChannelIntIntIntInt_007() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.setMeasureValue(CN_INT, 0, 0, 100L);
		c.setMeasureValue(CN_NUM, 0, 0, 500.0);
		assertThrows(IllegalArgumentException.class, () -> c.swapChannel(CN_INT, 0, CN_NUM, 0));
	}

	// seekPoint(BmsAt, double, BmsPoint)
	// 正常
	@Test
	public void testSeekPointBmsAtDoubleBmsPoint_001() {
		var p = new BmsPoint();
		var c = new BmsContent(spec());
		c.beginEdit();
		c.putNote(1, 0, 9, 0, 1000);
		c.endEdit();

		// 前進
		c.seekPoint(new BmsPoint(4, 0), 400, p);
		assertEquals(6, p.getMeasure());
		assertEquals(16.0, p.getTick(), 0.0);

		// 後退
		c.seekPoint(new BmsPoint(5, 96), -390, p);
		assertEquals(3, p.getMeasure());
		assertEquals(90.0, p.getTick(), 0.0);

		// 進行方向の最大値は小節番号==小節数, 刻み位置==0
		c.seekPoint(new BmsPoint(0, 0), 20 * 192, p);
		assertEquals(10, p.getMeasure());
		assertEquals(0.0, p.getTick(), 0.0);

		// 後退方向の最小値は小節番号==0, 刻み位置==0
		c.seekPoint(new BmsPoint(9, 191), -(20 * 192), p);
		assertEquals(0, p.getMeasure());
		assertEquals(0.0, p.getTick(), 0.0);

		// 小節長の影響を受ける
		c = new BmsContent(spec());
		c.beginEdit();
		c.putNote(1, 0, 9, 0, 1000);
		c.setMeasureValue(2, 2, 2.0);
		c.endEdit();
		c.seekPoint(new BmsPoint(0, 0), (5 * 192) + 10, p);
		assertEquals(4, p.getMeasure());
		assertEquals(10.0, p.getTick(), 0.0);
		c.seekPoint(new BmsPoint(4, 0), -((4 * 192) + 10), p);
		assertEquals(0, p.getMeasure());
		assertEquals(182.0, p.getTick(), 0.0);

		// 譜面停止の影響を受けない
		c = new BmsContent(spec());
		c.beginEdit();
		c.putNote(1, 0, 9, 0, 1000);
		c.setIndexedMeta("#stop", 999, 192L);
		c.putNote(9, 0, 2, 96, 999);
		c.endEdit();
		c.seekPoint(new BmsPoint(0, 0), ((4 * 192) + 10), p);
		assertEquals(4, p.getMeasure());
		assertEquals(10.0, p.getTick(), 0.0);
		c.seekPoint(new BmsPoint(5, 0), -((4 * 192) + 10), p);
		assertEquals(0, p.getMeasure());
		assertEquals(182.0, p.getTick(), 0.0);
	}

	// seekPoint(BmsAt, double, BmsPoint)
	// NullPointerException atFromがnull
	@Test
	public void testSeekPointBmsAtDoubleBmsPoint_002() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.putNote(1, 0, 9, 0, 1000);
		c.endEdit();
		var p = new BmsPoint();
		assertThrows(NullPointerException.class, () -> c.seekPoint(null, 1, p));
	}

	// seekPoint(int, double, double, BmsPoint)
	// 正常
	@Test
	public void testSeekPointIntDoubleDoubleBmsPoint_001() {
		var p = new BmsPoint();
		var c = new BmsContent(spec());
		c.beginEdit();
		c.putNote(1, 0, 9, 0, 1000);
		c.endEdit();

		// 前進
		c.seekPoint(4, 0, 400, p);
		assertEquals(6, p.getMeasure());
		assertEquals(16.0, p.getTick(), 0.0);

		// 後退
		c.seekPoint(5, 96, -390, p);
		assertEquals(3, p.getMeasure());
		assertEquals(90.0, p.getTick(), 0.0);

		// 進行方向の最大値は小節番号==小節数, 刻み位置==0
		c.seekPoint(0, 0, 20 * 192, p);
		assertEquals(10, p.getMeasure());
		assertEquals(0.0, p.getTick(), 0.0);

		// 後退方向の最小値は小節番号==0, 刻み位置==0
		c.seekPoint(9, 191, -(20 * 192), p);
		assertEquals(0, p.getMeasure());
		assertEquals(0.0, p.getTick(), 0.0);

		// 小節長の影響を受ける
		c = new BmsContent(spec());
		c.beginEdit();
		c.putNote(1, 0, 9, 0, 1000);
		c.setMeasureValue(2, 2, 2.0);
		c.endEdit();
		c.seekPoint(0, 0, (5 * 192) + 10, p);
		assertEquals(4, p.getMeasure());
		assertEquals(10.0, p.getTick(), 0.0);
		c.seekPoint(4, 0, -((4 * 192) + 10), p);
		assertEquals(0, p.getMeasure());
		assertEquals(182.0, p.getTick(), 0.0);

		// 譜面停止の影響を受けない
		c = new BmsContent(spec());
		c.beginEdit();
		c.putNote(1, 0, 9, 0, 1000);
		c.setIndexedMeta("#stop", 999, 192L);
		c.putNote(9, 0, 2, 96, 999);
		c.endEdit();
		c.seekPoint(0, 0, ((4 * 192) + 10), p);
		assertEquals(4, p.getMeasure());
		assertEquals(10.0, p.getTick(), 0.0);
		c.seekPoint(5, 0, -((4 * 192) + 10), p);
		assertEquals(0, p.getMeasure());
		assertEquals(182.0, p.getTick(), 0.0);
	}

	// seekPoint(int, double, double, BmsPoint)
	// IllegalArgumentException 原点の小節番号にノート・小節データが存在し得ない値を指定した
	@Test
	public void testSeekPointIntDoubleDoubleBmsPoint_002() {
		var p = new BmsPoint();
		var c = new BmsContent(spec());
		var ex = IllegalArgumentException.class;
		c.beginEdit();
		c.putNote(1, 0, 9, 0, 1000);
		c.endEdit();
		assertThrows(ex, () -> c.seekPoint(-1, 0, 1, p));
		assertThrows(ex, () -> c.seekPoint(10, 1, 1, p));
		assertThrows(ex, () -> c.seekPoint(11, 0, 1, p));
	}

	// seekPoint(int, double, double, BmsPoint)
	// IllegalArgumentException 原点の刻み位置にマイナス値または当該小節の刻み数以上の値を指定した
	@Test
	public void testSeekPointIntDoubleDoubleBmsPoint_003() {
		var p = new BmsPoint();
		var c = new BmsContent(spec());
		var ex = IllegalArgumentException.class;
		c.beginEdit();
		c.putNote(1, 0, 9, 0, 1000);
		c.endEdit();
		assertThrows(ex, () -> c.seekPoint(0, -1, 1, p));
		assertThrows(ex, () -> c.seekPoint(0, 192, 1, p));
	}

	// seekPoint(int, double, double, BmsPoint)
	// NullPointerException outPointがnull
	@Test
	public void testSeekPointIntDoubleDoubleBmsPoint_004() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.putNote(1, 0, 9, 0, 1000);
		c.endEdit();
		assertThrows(NullPointerException.class, () -> c.seekPoint(0, 0, 1, null));
	}

	// seekNextPoint(BmsAt, boolean, BmsPoint)
	// 正常
	@Test
	public void testSeekNextPointBmsAtBmsPoint_001() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.setMeasureValue(CN_INTS, 1, 199L);
		c.putNote(CN_A36S, 3, 48.0, 599);
		c.putNote(CN_A36, 2, 4, 12.0, 911);
		c.setMeasureValue(CN_OBJS, 6, new File("file_single_1.txt"));
		c.endEdit();

		// 楽曲位置0から始めて最終位置まで正常にシークできること
		var p = new BmsPoint(0, 0);
		c.seekNextPoint(new BmsPoint(p.getMeasure(), p.getTick()), false, p);
		assertEquals(1, p.getMeasure());
		assertEquals(0, p.getTick(), 0.0);
		c.seekNextPoint(new BmsPoint(p.getMeasure(), p.getTick()), false, p);
		assertEquals(2, p.getMeasure());
		assertEquals(0, p.getTick(), 0.0);
		c.seekNextPoint(new BmsPoint(p.getMeasure(), p.getTick()), false, p);
		assertEquals(3, p.getMeasure());
		assertEquals(0, p.getTick(), 0.0);
		c.seekNextPoint(new BmsPoint(p.getMeasure(), p.getTick()), false, p);
		assertEquals(3, p.getMeasure());
		assertEquals(48, p.getTick(), 0.0);
		c.seekNextPoint(new BmsPoint(p.getMeasure(), p.getTick()), false, p);
		assertEquals(4, p.getMeasure());
		assertEquals(0, p.getTick(), 0.0);
		c.seekNextPoint(new BmsPoint(p.getMeasure(), p.getTick()), false, p);
		assertEquals(4, p.getMeasure());
		assertEquals(12, p.getTick(), 0.0);
		c.seekNextPoint(new BmsPoint(p.getMeasure(), p.getTick()), false, p);
		assertEquals(5, p.getMeasure());
		assertEquals(0, p.getTick(), 0.0);
		c.seekNextPoint(new BmsPoint(p.getMeasure(), p.getTick()), false, p);
		assertEquals(6, p.getMeasure());
		assertEquals(0, p.getTick(), 0.0);

		// 戻り値は出力引数と同一インスタンスを返すこと
		p = new BmsPoint(0, 0);
		var ret = c.seekNextPoint(new BmsPoint(p.getMeasure(), p.getTick()), false, p);
		assertSame(p, ret);
	}

	// seekNextPoint(BmsAt, boolean, BmsPoint)
	// NullPointerException atがnull
	@Test
	public void testSeekNextPointBmsAtBmsPoint_002() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.putNote(CN_A36S, 10, 0.0, 100);
		c.endEdit();
		assertThrows(NullPointerException.class, () -> c.seekNextPoint(null, true, new BmsPoint()));
	}

	// seekNextPoint(int, double, boolean, BmsPoint)
	// 正常系のみ
	@Test
	public void testSeekNextPointDoubleIntBmsPoint_001() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.setMeasureValue(CN_INTS, 1, 199L);
		c.putNote(CN_A36S, 3, 48.0, 599);
		c.putNote(CN_A36, 2, 4, 12.0, 911);
		c.setMeasureValue(CN_OBJS, 6, new File("file_single_1.txt"));
		c.endEdit();

		// 楽曲位置0から始めて最終位置まで正常にシークできること
		var p = new BmsPoint(0, 0);
		c.seekNextPoint(p.getMeasure(), p.getTick(), false, p);
		assertEquals(1, p.getMeasure());
		assertEquals(0, p.getTick(), 0.0);
		c.seekNextPoint(p.getMeasure(), p.getTick(), false, p);
		assertEquals(2, p.getMeasure());
		assertEquals(0, p.getTick(), 0.0);
		c.seekNextPoint(p.getMeasure(), p.getTick(), false, p);
		assertEquals(3, p.getMeasure());
		assertEquals(0, p.getTick(), 0.0);
		c.seekNextPoint(p.getMeasure(), p.getTick(), false, p);
		assertEquals(3, p.getMeasure());
		assertEquals(48, p.getTick(), 0.0);
		c.seekNextPoint(p.getMeasure(), p.getTick(), false, p);
		assertEquals(4, p.getMeasure());
		assertEquals(0, p.getTick(), 0.0);
		c.seekNextPoint(p.getMeasure(), p.getTick(), false, p);
		assertEquals(4, p.getMeasure());
		assertEquals(12, p.getTick(), 0.0);
		c.seekNextPoint(p.getMeasure(), p.getTick(), false, p);
		assertEquals(5, p.getMeasure());
		assertEquals(0, p.getTick(), 0.0);
		c.seekNextPoint(p.getMeasure(), p.getTick(), false, p);
		assertEquals(6, p.getMeasure());
		assertEquals(0, p.getTick(), 0.0);

		// 戻り値は出力引数と同一インスタンスを返すこと
		p = new BmsPoint(0, 0);
		var ret = c.seekNextPoint(p.getMeasure(), p.getTick(), false, p);
		assertSame(p, ret);
	}

	// seekNextPoint(BmsAt, boolean, Tester, BmsPoint)
	// 正常
	@Test
	public void testSeekNextPointBmsAtBmsChannelTesterBmsPoint_001() {
		var c = new BmsContent(spec());
		c.beginEdit();
		// 値型
		c.setMeasureValue(CN_INTS, 100, 199L);
		c.setMeasureValue(CN_INTS, 150, 299L);
		c.setMeasureValue(CN_INT, 3, 100, 399L);
		c.setMeasureValue(CN_INT, 5, 150, 499L);
		c.setMeasureValue(CN_NUMS, 180, 9999.99);
		// 配列型
		c.putNote(CN_A36S, 100, 0.0, 599);
		c.putNote(CN_A36S, 100, 96.0, 699);
		c.putNote(CN_A36S, 150, 0.0, 799);
		c.putNote(CN_A36S, 150, 96.0, 899);
		c.putNote(CN_A36, 2, 100, 0.0, 911);
		c.putNote(CN_A36, 4, 100, 96.0, 922);
		c.putNote(CN_A36, 2, 150, 0.0, 933);
		c.putNote(CN_A36, 4, 150, 96.0, 944);
		// OBJECT型
		c.setMeasureValue(CN_OBJS, 100, new File("file_single_1.txt"));
		c.setMeasureValue(CN_OBJS, 120, new File("file_single_2.txt"));
		c.setMeasureValue(CN_OBJ, 1, 100, new File("file_multiple_1.txt"));
		c.setMeasureValue(CN_OBJ, 3, 120, new File("file_multiple_2.txt"));
		c.endEdit();

		// 楽曲位置0から始めて最終位置まで正常にシークできること
		var p = new BmsPoint(0, 0);
		c.seekNextPoint(new BmsPoint(p.getMeasure(), p.getTick()), false, ch -> ch != BmsSpec.CHANNEL_MEASURE, p);
		assertEquals(100, p.getMeasure());
		assertEquals(0, p.getTick(), 0.0);
		c.seekNextPoint(new BmsPoint(p.getMeasure(), p.getTick()), false, ch -> ch != BmsSpec.CHANNEL_MEASURE, p);
		assertEquals(100, p.getMeasure());
		assertEquals(96, p.getTick(), 0.0);
		c.seekNextPoint(new BmsPoint(p.getMeasure(), p.getTick()), false, ch -> ch != BmsSpec.CHANNEL_MEASURE, p);
		assertEquals(120, p.getMeasure());
		assertEquals(0, p.getTick(), 0.0);
		c.seekNextPoint(new BmsPoint(p.getMeasure(), p.getTick()), false, ch -> ch != BmsSpec.CHANNEL_MEASURE, p);
		assertEquals(150, p.getMeasure());
		assertEquals(0, p.getTick(), 0.0);
		c.seekNextPoint(new BmsPoint(p.getMeasure(), p.getTick()), false, ch -> ch != BmsSpec.CHANNEL_MEASURE, p);
		assertEquals(150, p.getMeasure());
		assertEquals(96, p.getTick(), 0.0);
		c.seekNextPoint(new BmsPoint(p.getMeasure(), p.getTick()), false, ch -> ch != BmsSpec.CHANNEL_MEASURE, p);
		assertEquals(180, p.getMeasure());
		assertEquals(0, p.getTick(), 0.0);
		c.seekNextPoint(new BmsPoint(p.getMeasure(), p.getTick()), false, ch -> ch != BmsSpec.CHANNEL_MEASURE, p);
		assertEquals(c.getMeasureCount(), p.getMeasure());
		assertEquals(0, p.getTick(), 0.0);

		// テスターで判定不合格となったチャンネルはシーク対象にならないこと
		p = new BmsPoint(0, 0);
		c.seekNextPoint(new BmsPoint(p.getMeasure(), p.getTick()), false, ch -> ch == CN_A36, p);
		assertEquals(100, p.getMeasure());
		assertEquals(0, p.getTick(), 0.0);
		c.seekNextPoint(new BmsPoint(p.getMeasure(), p.getTick()), false, ch -> ch == CN_A36, p);
		assertEquals(100, p.getMeasure());
		assertEquals(96, p.getTick(), 0.0);
		c.seekNextPoint(new BmsPoint(p.getMeasure(), p.getTick()), false, ch -> ch == CN_A36, p);
		assertEquals(150, p.getMeasure());
		assertEquals(0, p.getTick(), 0.0);
		c.seekNextPoint(new BmsPoint(p.getMeasure(), p.getTick()), false, ch -> ch == CN_A36, p);
		assertEquals(150, p.getMeasure());
		assertEquals(96, p.getTick(), 0.0);
		c.seekNextPoint(new BmsPoint(p.getMeasure(), p.getTick()), false, ch -> ch == CN_A36, p);
		assertEquals(c.getMeasureCount(), p.getMeasure());
		assertEquals(0, p.getTick(), 0.0);

		// 全チャンネルが判定不合格となっても、処理結果は終端(小節数, 0)であること
		p = new BmsPoint(0, 0);
		c.seekNextPoint(new BmsPoint(p.getMeasure(), p.getTick()), false, ch -> false, p);
		assertEquals(c.getMeasureCount(), p.getMeasure());
		assertEquals(0, p.getTick(), 0.0);

		// 戻り値は出力引数と同一インスタンスを返すこと
		p = new BmsPoint(0, 0);
		var ret = c.seekNextPoint(new BmsPoint(p.getMeasure(), p.getTick()), false, ch -> true, p);
		assertSame(p, ret);
	}

	// seekNextPoint(BmsAt, boolean, Tester, BmsPoint)
	// NullPointerException atがnull
	@Test
	public void testSeekNextPointBmsAtBmsChannelTesterBmsPoint_002() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.putNote(CN_A36S, 10, 0.0, 100);
		c.endEdit();
		assertThrows(NullPointerException.class, () -> c.seekNextPoint(null, true, ch -> true, new BmsPoint()));
	}

	// seekNextPoint(int, double, boolean, Tester, BmsPoint)
	// 正常
	@Test
	public void testSeekNextPointIntDoubleBmsChannelTesterBmsPoint_001() {
		var c = new BmsContent(spec());
		c.beginEdit();
		// 値型
		c.setMeasureValue(CN_INTS, 100, 199L);
		c.setMeasureValue(CN_INTS, 150, 299L);
		c.setMeasureValue(CN_INT, 3, 100, 399L);
		c.setMeasureValue(CN_INT, 5, 150, 499L);
		c.setMeasureValue(CN_NUMS, 180, 9999.99);
		// 配列型
		c.putNote(CN_A36S, 100, 0.0, 599);
		c.putNote(CN_A36S, 100, 96.0, 699);
		c.putNote(CN_A36S, 150, 0.0, 799);
		c.putNote(CN_A36S, 150, 96.0, 899);
		c.putNote(CN_A36, 2, 100, 0.0, 911);
		c.putNote(CN_A36, 4, 100, 96.0, 922);
		c.putNote(CN_A36, 2, 150, 0.0, 933);
		c.putNote(CN_A36, 4, 150, 96.0, 944);
		// OBJECT型
		c.setMeasureValue(CN_OBJS, 100, new File("file_single_1.txt"));
		c.setMeasureValue(CN_OBJS, 120, new File("file_single_2.txt"));
		c.setMeasureValue(CN_OBJ, 1, 100, new File("file_multiple_1.txt"));
		c.setMeasureValue(CN_OBJ, 3, 120, new File("file_multiple_2.txt"));
		c.endEdit();

		// 楽曲位置0から始めて最終位置まで正常にシークできること
		var p = new BmsPoint(0, 0);
		c.seekNextPoint(p.getMeasure(), p.getTick(), false, ch -> ch != BmsSpec.CHANNEL_MEASURE, p);
		assertEquals(100, p.getMeasure());
		assertEquals(0, p.getTick(), 0.0);
		c.seekNextPoint(p.getMeasure(), p.getTick(), false, ch -> ch != BmsSpec.CHANNEL_MEASURE, p);
		assertEquals(100, p.getMeasure());
		assertEquals(96, p.getTick(), 0.0);
		c.seekNextPoint(p.getMeasure(), p.getTick(), false, ch -> ch != BmsSpec.CHANNEL_MEASURE, p);
		assertEquals(120, p.getMeasure());
		assertEquals(0, p.getTick(), 0.0);
		c.seekNextPoint(p.getMeasure(), p.getTick(), false, ch -> ch != BmsSpec.CHANNEL_MEASURE, p);
		assertEquals(150, p.getMeasure());
		assertEquals(0, p.getTick(), 0.0);
		c.seekNextPoint(p.getMeasure(), p.getTick(), false, ch -> ch != BmsSpec.CHANNEL_MEASURE, p);
		assertEquals(150, p.getMeasure());
		assertEquals(96, p.getTick(), 0.0);
		c.seekNextPoint(p.getMeasure(), p.getTick(), false, ch -> ch != BmsSpec.CHANNEL_MEASURE, p);
		assertEquals(180, p.getMeasure());
		assertEquals(0, p.getTick(), 0.0);
		c.seekNextPoint(p.getMeasure(), p.getTick(), false, ch -> ch != BmsSpec.CHANNEL_MEASURE, p);
		assertEquals(c.getMeasureCount(), p.getMeasure());
		assertEquals(0, p.getTick(), 0.0);

		// テスターで判定不合格となったチャンネルはシーク対象にならないこと
		p = new BmsPoint(0, 0);
		c.seekNextPoint(p.getMeasure(), p.getTick(), false, ch -> ch == CN_A36, p);
		assertEquals(100, p.getMeasure());
		assertEquals(0, p.getTick(), 0.0);
		c.seekNextPoint(p.getMeasure(), p.getTick(), false, ch -> ch == CN_A36, p);
		assertEquals(100, p.getMeasure());
		assertEquals(96, p.getTick(), 0.0);
		c.seekNextPoint(p.getMeasure(), p.getTick(), false, ch -> ch == CN_A36, p);
		assertEquals(150, p.getMeasure());
		assertEquals(0, p.getTick(), 0.0);
		c.seekNextPoint(p.getMeasure(), p.getTick(), false, ch -> ch == CN_A36, p);
		assertEquals(150, p.getMeasure());
		assertEquals(96, p.getTick(), 0.0);
		c.seekNextPoint(p.getMeasure(), p.getTick(), false, ch -> ch == CN_A36, p);
		assertEquals(c.getMeasureCount(), p.getMeasure());
		assertEquals(0, p.getTick(), 0.0);

		// 全チャンネルが判定不合格となっても、処理結果は終端(小節数, 0)であること
		p = new BmsPoint(0, 0);
		c.seekNextPoint(p.getMeasure(), p.getTick(), false, ch -> false, p);
		assertEquals(c.getMeasureCount(), p.getMeasure());
		assertEquals(0, p.getTick(), 0.0);

		// 戻り値は出力引数と同一インスタンスを返すこと
		p = new BmsPoint(0, 0);
		var ret = c.seekNextPoint(p.getMeasure(), p.getTick(), false, ch -> true, p);
		assertSame(p, ret);
	}

	// seekNextPoint(int, double, boolean, Tester, BmsPoint)
	// 正常(追加仕様のテスト)
	@Test
	public void testSeekNextPointIntDoubleBmsChannelTesterBmsPoint_001_2() {
		var s = spec();
		var c = (BmsContent)null;
		var p = new BmsPoint();

		// 小節0、刻み0指定で同位置を返せること
		c = new BmsContent(s);
		c.beginEdit();
		c.putNote(CN_A36S, 1, 0.0, 100);
		c.setMeasureValue(CN_INTS, 2, 200L);
		c.endEdit();
		c.seekNextPoint(0, 0, true, ch -> true, p);
		assertEquals(0, p.getMeasure());
		assertEquals(0, p.getTick(), 0.0);

		// 小節0、刻み0の小節線を返せること
		c = new BmsContent(s);
		c.beginEdit();
		c.putNote(CN_A36S, 1, 0.0, 100);
		c.setMeasureValue(CN_INTS, 2, 200L);
		c.endEdit();
		c.seekNextPoint(0, 0, true, ch -> ch == BmsSpec.CHANNEL_MEASURE, p);
		assertEquals(0, p.getMeasure());
		assertEquals(0, p.getTick(), 0.0);

		// 指定位置と同じ場所にノートがあった場合にその位置を返せること
		c = new BmsContent(s);
		c.beginEdit();
		c.putNote(CN_A36S, 1, 48.0, 100);
		c.putNote(CN_A36S, 1, 96.0, 200);
		c.putNote(CN_A36S, 2, 24.0, 300);
		c.endEdit();
		c.seekNextPoint(1, 48, true, ch -> ch == CN_A36S, p);
		assertEquals(1, p.getMeasure());
		assertEquals(48, p.getTick(), 0.0);

		// 指定位置と同じ場所に小節線があった場合にその位置を返せること
		c = new BmsContent(s);
		c.beginEdit();
		c.putNote(CN_A36S, 5, 0.0, 100);
		c.endEdit();
		c.seekNextPoint(3, 0, true, ch -> ch == BmsSpec.CHANNEL_MEASURE, p);
		assertEquals(3, p.getMeasure());
		assertEquals(0, p.getTick(), 0.0);

		// 指定位置と同じ場所に小節データがあった場合にその位置を返せること
		c = new BmsContent(s);
		c.beginEdit();
		c.setMeasureValue(CN_INTS, 2, 200L);
		c.setMeasureValue(CN_NUMS, 4, 400.0);
		c.setMeasureValue(CN_STRS, 5, "str");
		c.endEdit();
		c.seekNextPoint(4, 0, true, ch -> ch == CN_NUMS, p);
		assertEquals(4, p.getMeasure());
		assertEquals(0, p.getTick(), 0.0);

		// 指定位置から次小節にあるノートの間の小節線を返せること
		c = new BmsContent(s);
		c.beginEdit();
		c.putNote(CN_A36S, 5, 96.0, 100);
		c.endEdit();
		c.seekNextPoint(4, 48, true, ch -> true, p);
		assertEquals(5, p.getMeasure());
		assertEquals(0, p.getTick(), 0.0);

		// 指定位置から次小節にあるノートの間の小節データを返せること
		c = new BmsContent(s);
		c.beginEdit();
		c.setMeasureValue(CN_INTS, 5, 600L);
		c.putNote(CN_A36S, 5, 96.0, 100);
		c.endEdit();
		c.seekNextPoint(4, 96, true, ch -> ch == CN_INTS, p);
		assertEquals(5, p.getMeasure());
		assertEquals(0, p.getTick(), 0.0);

		// 指定位置から複数小節飛んだ先のノートの間の小節線を1小節ごとに返せること
		c = new BmsContent(s);
		c.beginEdit();
		c.putNote(CN_A36S, 4, 24.0, 400);
		c.endEdit();
		c.seekNextPoint(1, 96, false, ch -> true, p);
		assertEquals(2, p.getMeasure());
		assertEquals(0, p.getTick(), 0.0);
		c.seekNextPoint(p.getMeasure(), p.getTick(), false, ch -> true, p);
		assertEquals(3, p.getMeasure());
		assertEquals(0, p.getTick(), 0.0);
		c.seekNextPoint(p.getMeasure(), p.getTick(), false, ch -> true, p);
		assertEquals(4, p.getMeasure());
		assertEquals(0, p.getTick(), 0.0);

		// 指定位置から複数小節飛んだ先のノートの間の小節線を省略して当該ノートを返せること
		c = new BmsContent(s);
		c.beginEdit();
		c.putNote(CN_A36S, 4, 24.0, 400);
		c.endEdit();
		c.seekNextPoint(1, 96, true, ch -> ch != BmsSpec.CHANNEL_MEASURE, p);
		assertEquals(4, p.getMeasure());
		assertEquals(24, p.getTick(), 0.0);

		// 指定位置(小節線上)を含まない状態で指定位置にノートがあった時、指定位置より後の楽曲位置を返すこと
		c = new BmsContent(s);
		c.beginEdit();
		c.putNote(CN_A36S, 1, 0.0, 10);
		c.putNote(CN_A36S, 1, 48.0, 20);
		c.putNote(CN_A36S, 1, 96.0, 30);
		c.endEdit();
		c.seekNextPoint(1, 0, false, ch -> ch == CN_A36S, p);
		assertEquals(1, p.getMeasure());
		assertEquals(48, p.getTick(), 0.0);

		// 指定位置(小節線上)を含まない状態で指定位置に要素がない時、小節線は返されずに指定位置より後の楽曲位置を返すこと
		c = new BmsContent(s);
		c.beginEdit();
		c.putNote(CN_A36S, 1, 48.0, 10);
		c.endEdit();
		c.seekNextPoint(1, 0, false, ch -> true, p);
		assertEquals(1, p.getMeasure());
		assertEquals(48, p.getTick(), 0.0);

		// 指定位置(小節線上)を含まない状態で指定位置に小節データがあった時、指定位置より後の楽曲位置を返すこと
		c = new BmsContent(s);
		c.beginEdit();
		c.setMeasureValue(CN_INTS, 2, 200L);
		c.putNote(CN_A36S, 5, 120.0, 500);
		c.endEdit();
		c.seekNextPoint(2, 0, false, ch -> ch != BmsSpec.CHANNEL_MEASURE, p);
		assertEquals(5, p.getMeasure());
		assertEquals(120, p.getTick(), 0.0);

		// 指定位置(小節の中間)を含まない状態で指定位置にノートがあった時、指定位置より後の楽曲位置を返すこと
		c = new BmsContent(s);
		c.beginEdit();
		c.putNote(CN_A36S, 2, 24.0, 200);
		c.putNote(CN_A36, 2, 96.0, 900);
		c.putNote(CN_A36S, 2, 168.0, 200);
		c.endEdit();
		c.seekNextPoint(2, 24, false, ch -> ch == CN_A36S, p);
		assertEquals(2, p.getMeasure());
		assertEquals(168, p.getTick(), 0.0);

		// 同一楽曲位置に複数のチャンネルのノート・小節データが存在する時、全てのチャンネルがテスターの検査にかけられること
		c = new BmsContent(s);
		c.beginEdit();
		// m=3, t=0
		c.putNote(CN_A36S, 3, 0.0, 300);
		c.putNote(CN_A36, 3, 0.0, 400);
		c.setMeasureValue(CN_INTS, 3, 300L);
		c.setMeasureValue(CN_STRS, 3, "str");
		c.setMeasureValue(CN_OBJS, 3, new Object());
		// m=5, t=24
		c.putNote(CN_A36S_INT, 5, 24.0, 500);
		c.putNote(CN_A36S_NUM, 5, 24.0, 600);
		c.putNote(CN_A36S_STR, 5, 24.0, 700);
		c.endEdit();
		var m3t0 = new ArrayList<Integer>(List.of(BmsSpec.CHANNEL_MEASURE, CN_A36S, CN_A36, CN_INTS, CN_STRS, CN_OBJS));
		var m5t24 = new ArrayList<Integer>(List.of(CN_A36S_INT, CN_A36S_NUM, CN_A36S_STR));
		c.seekNextPoint(2, 96, false, ch -> { m3t0.remove((Object)ch); return m3t0.size() == 0; }, p);
		assertEquals(0, m3t0.size());
		assertEquals(3, p.getMeasure());
		assertEquals(0, p.getTick(), 0.0);
		c.seekNextPoint(p.getMeasure(), p.getTick(), false, ch -> true, p);
		assertEquals(4, p.getMeasure());
		assertEquals(0, p.getTick(), 0.0);
		c.seekNextPoint(p.getMeasure(), p.getTick(), false, ch -> true, p);
		assertEquals(5, p.getMeasure());
		assertEquals(0, p.getTick(), 0.0);
		c.seekNextPoint(p.getMeasure(), p.getTick(), false, ch -> { m5t24.remove((Object)ch); return m5t24.size() == 0; }, p);
		assertEquals(0, m5t24.size());
		assertEquals(5, p.getMeasure());
		assertEquals(24, p.getTick(), 0.0);

		// チャンネルテスターで不合格となったノートは省略されること
		c = new BmsContent(s);
		c.beginEdit();
		c.putNote(CN_A36S, 1, 24.0, 100);
		c.putNote(CN_A36, 3, 96.0, 200);
		c.putNote(CN_A36, 4, 0.0, 300);
		c.putNote(CN_A36S, 4, 12.0, 400);
		c.endEdit();
		c.seekNextPoint(0, 0, false, ch -> ch == CN_A36S, p);
		assertEquals(1, p.getMeasure());
		assertEquals(24, p.getTick(), 0.0);
		c.seekNextPoint(p.getMeasure(), p.getTick(), false, ch -> ch == CN_A36S, p);
		assertEquals(4, p.getMeasure());
		assertEquals(12, p.getTick(), 0.0);

		// チャンネルテスターで不合格となった小節線は省略されること
		c = new BmsContent(s);
		c.beginEdit();
		c.setMeasureValue(CN_STRS, 2, "hoge");
		c.setMeasureValue(CN_STRS, 4, "hage");
		c.endEdit();
		c.seekNextPoint(1, 96, false, ch -> ch != BmsSpec.CHANNEL_MEASURE, p);
		assertEquals(2, p.getMeasure());
		assertEquals(0, p.getTick(), 0.0);
		c.seekNextPoint(p.getMeasure(), p.getTick(), false, ch -> ch != BmsSpec.CHANNEL_MEASURE, p);
		assertEquals(4, p.getMeasure());
		assertEquals(0, p.getTick(), 0.0);

		// チャンネルテスターで不合格となった小節データは省略されること
		c = new BmsContent(s);
		c.beginEdit();
		c.setMeasureValue(CN_STRS, 2, "hoge");
		c.putNote(CN_A36S, 3, 12.0, 300);
		c.setMeasureValue(CN_STRS, 4, "hage");
		c.putNote(CN_A36S, 4, 48.0, 400);
		c.endEdit();
		c.seekNextPoint(1, 160, false, ch -> ch == CN_A36S, p);
		assertEquals(3, p.getMeasure());
		assertEquals(12, p.getTick(), 0.0);
		c.seekNextPoint(p.getMeasure(), p.getTick(), false, ch -> ch == CN_A36S, p);
		assertEquals(4, p.getMeasure());
		assertEquals(48, p.getTick(), 0.0);

		// ノート・小節データが空(小節数0)で小節0, 刻み0を指定すると、小節0, 刻み0の楽曲位置を返すこと
		c = new BmsContent(s);
		c.seekNextPoint(0, 0, true, ch -> true, p);
		assertEquals(0, p.getMeasure());
		assertEquals(0, p.getTick(), 0.0);

		// 小節の最後まで合格となる要素がない時に、小節数, 刻み0の楽曲位置を返すこと(小節数=10)
		c = new BmsContent(s);
		c.beginEdit();
		c.putNote(CN_A36S, 9, 120.0, 900);
		c.endEdit();
		c.seekNextPoint(0, 0, true, ch -> false, p);
		assertEquals(10, p.getMeasure());
		assertEquals(0, p.getTick(), 0.0);

		// 小節の最後まで合格となる要素がない時に、小節数, 刻み0の楽曲位置を返すこと(小節数=MAX)
		c = new BmsContent(s);
		c.beginEdit();
		c.putNote(CN_A36S, BmsSpec.MEASURE_MAX, 120.0, 1000);
		c.endEdit();
		c.seekNextPoint(0, 0, true, ch -> false, p);
		assertEquals(BmsSpec.MEASURE_MAX_COUNT, p.getMeasure());
		assertEquals(0, p.getTick(), 0.0);
	}

	// seekNextPoint(int, double, boolean, Tester, BmsPoint)
	// IllegalArgumentException 小節番号がマイナス値
	@Test
	public void testSeekNextPointIntDoubleBmsChannelTesterBmsPoint_002() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.putNote(CN_A36S, 10, 0.0, 100);
		c.endEdit();
		assertThrows(IllegalArgumentException.class, () -> c.seekNextPoint(-1, 0, false, ch -> true, new BmsPoint()));
	}

	// seekNextPoint(int, double, boolean, Tester, BmsPoint)
	// IllegalArgumentException 小節番号が小節数以上
	@Test
	public void testSeekNextPointIntDoubleBmsChannelTesterBmsPoint_003() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.putNote(CN_A36S, 10, 0.0, 100);
		c.endEdit();
		assertThrows(IllegalArgumentException.class, () -> c.seekNextPoint(-1, 0, false, ch -> true, new BmsPoint()));
	}

	// seekNextPoint(int, double, boolean, Tester, BmsPoint)
	// IllegalArgumentException 小節の刻み位置がマイナス値
	@Test
	public void testSeekNextPointIntDoubleBmsChannelTesterBmsPoint_004() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.putNote(CN_A36S, 10, 0.0, 100);
		c.endEdit();
		assertThrows(IllegalArgumentException.class, () -> c.seekNextPoint(0, -1, false, ch -> true, new BmsPoint()));
	}

	// seekNextPoint(int, double, boolean, Tester, BmsPoint)
	// IllegalArgumentException 小節の刻み位置が当該小節の刻み数以上
	@Test
	public void testSeekNextPointIntDoubleBmsChannelTesterBmsPoint_005() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.putNote(CN_A36S, 10, 0.0, 100);
		c.endEdit();
		assertThrows(IllegalArgumentException.class, () -> c.seekNextPoint(0, BmsSpec.TICK_COUNT_DEFAULT, false, ch -> true, new BmsPoint()));
	}

	// seekNextPoint(int, double, boolean, Tester, BmsPoint)
	// NullPointerException chTesterがnull
	@Test
	public void testSeekNextPointIntDoubleBmsChannelTesterBmsPoint_006() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.putNote(CN_A36S, 10, 0.0, 100);
		c.endEdit();
		assertThrows(NullPointerException.class, () -> c.seekNextPoint(10, 0, false, null, new BmsPoint()));
	}

	// seekNextPoint(int, double, boolean, Tester, BmsPoint)
	// NullPointerException outPointがnull
	@Test
	public void testSeekNextPointIntDoubleBmsChannelTesterBmsPoint_007() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.putNote(CN_A36S, 10, 0.0, 100);
		c.endEdit();
		assertThrows(NullPointerException.class, () -> c.seekNextPoint(10, 0, false, ch -> true, null));
	}

	// timeline(BmsAt)
	// 正常：小節線上の楽曲位置を指定すると、小節線→小節データ→ノートの順に走査されること
	@Test
	public void testTimelineBmsAt_OnMeasureLine() {
		var c = contentForTimeline();
		var e = c.timeline(BmsPoint.of(1, 0.0)).collect(Collectors.toList());
		assertEquals(8, e.size());
		assertMeasureLineElement(e.get(0), 1);
		assertMeasureValueElement(e.get(1), 1, CN_STR, 0, "STRING1");
		assertMeasureValueElement(e.get(2), 1, CN_STR, 1, "STRING2");
		assertMeasureValueElement(e.get(3), 1, CN_INTS, 0, 10L);
		assertMeasureValueElement(e.get(4), 1, CN_OBJS, 0, BmsPoint.of(1, 2.0));
		assertNoteElement(e.get(5), 1, 0.0, CN_A36, 0, 200);
		assertNoteElement(e.get(6), 1, 0.0, CN_A36, 1, 300);
		assertNoteElement(e.get(7), 1, 0.0, CN_A36S, 0, 400);
	}

	// timeline(BmsAt)
	// 正常：小節線上以外の楽曲位置を指定すると、当該楽曲位置ノートのみが全て走査されること
	@Test
	public void testTimelineBmsAt_NotMeasureLine() {
		var c = contentForTimeline();
		var e = c.timeline(BmsPoint.of(6, 96.0)).collect(Collectors.toList());
		assertEquals(4, e.size());
		assertNoteElement(e.get(0), 6, 96.0, CN_A36, 0, 1000);
		assertNoteElement(e.get(1), 6, 96.0, CN_A36, 1, 1010);
		assertNoteElement(e.get(2), 6, 96.0, CN_A36, 2, 1020);
		assertNoteElement(e.get(3), 6, 96.0, CN_A36S, 0, 1030);
	}

	// timeline(BmsAt)
	// 正常：指定楽曲位置にタイムライン要素がない場合、何も走査されないこと
	@Test
	public void testTimelineBmsAt_NoElementOnAt() {
		var c = contentForTimeline();
		var e = c.timeline(BmsPoint.of(1, 48.0)).collect(Collectors.toList());
		assertEquals(0, e.size());
	}

	// timeline(BmsAt)
	// 正常：タイムラインが空の場合、何も走査されないこと
	@Test
	public void testTimelineBmsAt_EmptyTimeline() {
		var c = contentForTimelineEmpty();
		var e = c.timeline(BmsPoint.of(0, 0.0)).collect(Collectors.toList());
		assertEquals(0, e.size());
	}

	// timeline(BmsAt)
	// 正常：編集モードでも走査可能であること
	@Test
	public void testTimelineBmsAt_EditMode() {
		var c = contentForTimeline();
		c.beginEdit();
		var e = c.timeline(BmsPoint.of(1, 0.0)).collect(Collectors.toList());
		assertEquals(8, e.size());
		// 同様のチェックを別テストで実施済みのため、アサーション省略
	}

	// timeline(BmsAt)
	// NullPointerException atがnull
	@Test
	public void testTimelineBmsAt_NullAt() {
		var e = NullPointerException.class;
		var c = contentForTimeline();
		assertThrows(e, () -> c.timeline(null));
	}

	// timeline(BmsAt)
	// IllegalArgumentException 小節番号がマイナス値
	@Test
	public void testTimelineBmsAt_NegativeMeasure() {
		var e = IllegalArgumentException.class;
		var c = contentForTimeline();
		assertThrows(e, () -> c.timeline(BmsPoint.of(-1, 0)));
	}

	// timeline(BmsAt)
	// IllegalArgumentException 小節の刻み位置が{@link BmsSpec#TICK_MIN}以外の時、小節番号が小節数以上
	@Test
	public void testTimelineBmsAt_OverflowTimeline() {
		var e = IllegalArgumentException.class;
		var c = contentForTimeline();
		assertThrows(e, () -> c.timeline(BmsPoint.of(c.getMeasureCount(), 96.0)));
	}

	// timeline(BmsAt)
	// IllegalArgumentException 小節の刻み位置が{@link BmsSpec#TICK_MIN}の時、小節番号が小節数超過
	@Test
	public void testTimelineBmsAt_OverTerminate() {
		var e = IllegalArgumentException.class;
		var c = contentForTimeline();
		assertThrows(e, () -> c.timeline(BmsPoint.of(c.getMeasureCount() + 1, BmsSpec.TICK_MIN)));
	}

	// timeline(BmsAt)
	// IllegalArgumentException 小節の刻み位置が{@link BmsSpec#TICK_MIN}未満、または{@link BmsSpec#TICK_MAX}超過
	@Test
	public void testTimelineBmsAt_OutOfRangeTick() {
		var e = IllegalArgumentException.class;
		var c = contentForTimeline();
		assertThrows(e, () -> c.timeline(BmsPoint.of(1, Math.nextDown(BmsSpec.TICK_MIN))));
		assertThrows(e, () -> c.timeline(BmsPoint.of(1, Math.nextUp(BmsSpec.TICK_MAX))));
	}

	// timeline(int, double)
	// 正常：小節線上の楽曲位置を指定すると、小節線→小節データ→ノートの順に走査されること
	@Test
	public void testTimelineIntDouble_OnMeasureLine() {
		var c = contentForTimeline();
		var e = c.timeline(1, 0.0).collect(Collectors.toList());
		assertEquals(8, e.size());
		assertMeasureLineElement(e.get(0), 1);
		assertMeasureValueElement(e.get(1), 1, CN_STR, 0, "STRING1");
		assertMeasureValueElement(e.get(2), 1, CN_STR, 1, "STRING2");
		assertMeasureValueElement(e.get(3), 1, CN_INTS, 0, 10L);
		assertMeasureValueElement(e.get(4), 1, CN_OBJS, 0, BmsPoint.of(1, 2.0));
		assertNoteElement(e.get(5), 1, 0.0, CN_A36, 0, 200);
		assertNoteElement(e.get(6), 1, 0.0, CN_A36, 1, 300);
		assertNoteElement(e.get(7), 1, 0.0, CN_A36S, 0, 400);
	}

	// timeline(int, double)
	// 正常：小節線上以外の楽曲位置を指定すると、当該楽曲位置ノートのみが全て走査されること
	@Test
	public void testTimelineIntDouble_NotMeasureLine() {
		var c = contentForTimeline();
		var e = c.timeline(6, 96.0).collect(Collectors.toList());
		assertEquals(4, e.size());
		assertNoteElement(e.get(0), 6, 96.0, CN_A36, 0, 1000);
		assertNoteElement(e.get(1), 6, 96.0, CN_A36, 1, 1010);
		assertNoteElement(e.get(2), 6, 96.0, CN_A36, 2, 1020);
		assertNoteElement(e.get(3), 6, 96.0, CN_A36S, 0, 1030);
	}

	// timeline(int, double)
	// 正常：指定楽曲位置にタイムライン要素がない場合、何も走査されないこと
	@Test
	public void testTimelineIntDouble_NotElementOnAt() {
		var c = contentForTimeline();
		var e = c.timeline(1, 48.0).collect(Collectors.toList());
		assertEquals(0, e.size());
	}

	// timeline(int, double)
	// 正常：タイムラインが空の場合、何も走査されないこと
	@Test
	public void testTimelineIntDouble_EmptyTimeline() {
		var c = contentForTimelineEmpty();
		var e = c.timeline(0, 0.0).collect(Collectors.toList());
		assertEquals(0, e.size());
	}

	// timeline(int, double)
	// 正常：編集モードでも走査可能であること
	@Test
	public void testTimelineIntDouble_EditMode() {
		var c = contentForTimeline();
		c.beginEdit();
		var e = c.timeline(1, 0.0).collect(Collectors.toList());
		assertEquals(8, e.size());
		// 同様のチェックを別テストで実施済みのため、アサーション省略
	}

	// timeline(int, double)
	// IllegalArgumentException 小節番号がマイナス値
	@Test
	public void testTimelineIntDouble_NegativeMeasure() {
		var e = IllegalArgumentException.class;
		var c = contentForTimeline();
		assertThrows(e, () -> c.timeline(-1, 0));
	}

	// timeline(int, double)
	// IllegalArgumentException 小節の刻み位置が{@link BmsSpec#TICK_MIN}以外の時、小節番号が小節数以上
	@Test
	public void testTimelineIntDouble_OverflowTimeline() {
		var e = IllegalArgumentException.class;
		var c = contentForTimeline();
		assertThrows(e, () -> c.timeline(c.getMeasureCount(), 96.0));
	}

	// timeline(int, double)
	// IllegalArgumentException 小節の刻み位置が{@link BmsSpec#TICK_MIN}の時、小節番号が小節数超過
	@Test
	public void testTimelineIntDouble_OverTerminate() {
		var e = IllegalArgumentException.class;
		var c = contentForTimeline();
		assertThrows(e, () -> c.timeline(c.getMeasureCount() + 1, BmsSpec.TICK_MIN));
	}

	// timeline(int, double)
	// IllegalArgumentException 小節の刻み位置が{@link BmsSpec#TICK_MIN}未満、または{@link BmsSpec#TICK_MAX}超過
	@Test
	public void testTimelineIntDouble_OutOfRangeTick() {
		var e = IllegalArgumentException.class;
		var c = contentForTimeline();
		assertThrows(e, () -> c.timeline(1, Math.nextDown(BmsSpec.TICK_MIN)));
		assertThrows(e, () -> c.timeline(1, Math.nextUp(BmsSpec.TICK_MAX)));
	}

	// timeline()
	// 正常：タイムライン全体で期待通りの走査が行われること(小節線上は小節線・小節データ・ノートの順、小節線上外はノートのみ、空小節でも小節線は列挙)
	@Test
	public void testTimeline_Normal() {
		var c = contentForTimeline();
		var e = c.timeline().collect(Collectors.toList());
		assertEquals(30, e.size());
		// 0小節目
		var i = 0;
		var m = 0;
		assertMeasureLineElement(e.get(i++), m);
		assertNoteElement(e.get(i++), m, 96.0, CN_A36S, 0, 100);
		// 1小節目
		m++;
		assertMeasureLineElement(e.get(i++), m);
		assertMeasureValueElement(e.get(i++), m, CN_STR, 0, "STRING1");
		assertMeasureValueElement(e.get(i++), m, CN_STR, 1, "STRING2");
		assertMeasureValueElement(e.get(i++), m, CN_INTS, 0, 10L);
		assertMeasureValueElement(e.get(i++), m, CN_OBJS, 0, BmsPoint.of(1, 2.0));
		assertNoteElement(e.get(i++), m, 0.0, CN_A36, 0, 200);
		assertNoteElement(e.get(i++), m, 0.0, CN_A36, 1, 300);
		assertNoteElement(e.get(i++), m, 0.0, CN_A36S, 0, 400);
		assertNoteElement(e.get(i++), m, Math.nextUp(0.0), CN_A36S, 0, 500);
		// 2小節目
		m++;
		assertMeasureLineElement(e.get(i++), m);
		assertNoteElement(e.get(i++), m, 0.0, CN_A36, 0, 600);
		assertNoteElement(e.get(i++), m, 0.0, CN_A36, 1, 700);
		assertNoteElement(e.get(i++), m, 0.0, CN_A36, 2, 800);
		// 3小節目
		m++;
		assertMeasureLineElement(e.get(i++), m);
		// 4小節目
		m++;
		assertMeasureLineElement(e.get(i++), m);
		// 5小節目
		m++;
		assertMeasureLineElement(e.get(i++), m);
		assertMeasureValueElement(e.get(i++), m, CN_INT, 0, 20L);
		assertMeasureValueElement(e.get(i++), m, CN_INT, 1, 30L);
		assertMeasureValueElement(e.get(i++), m, CN_INT, 2, 40L);
		assertMeasureValueElement(e.get(i++), m, CN_NUMS, 0, 50.0);
		assertNoteElement(e.get(i++), m, Math.nextDown(BmsSpec.TICK_COUNT_DEFAULT), CN_A36S, 0, 900);
		// 6小節目
		m++;
		assertMeasureLineElement(e.get(i++), m);
		assertNoteElement(e.get(i++), m, Math.nextDown(96.0), CN_A36S, 0, 990);
		assertNoteElement(e.get(i++), m, 96.0, CN_A36, 0, 1000);
		assertNoteElement(e.get(i++), m, 96.0, CN_A36, 1, 1010);
		assertNoteElement(e.get(i++), m, 96.0, CN_A36, 2, 1020);
		assertNoteElement(e.get(i++), m, 96.0, CN_A36S, 0, 1030);
		assertNoteElement(e.get(i++), m, Math.nextUp(96.0), CN_A36S, 0, 1040);
	}

	// timeline()
	// 正常：タイムラインが空の場合、何も走査されないこと
	@Test
	public void testTimeline_EmptyTimeline() {
		var c = contentForTimelineEmpty();
		var e = c.timeline().collect(Collectors.toList());
		assertEquals(0, e.size());
	}

	// timeline()
	// 正常：編集モードでも走査可能であること
	@Test
	public void testTimeline_EditMode() {
		var c = contentForTimeline();
		c.beginEdit();
		var e = c.timeline().collect(Collectors.toList());
		assertEquals(30, e.size());
		// 同様のチェックを別テストで実施済みのため、アサーション省略
	}

	// timeline(BmsAt, BmsAt)
	// 正常：走査開始楽曲位置の要素は走査され、走査終了楽曲位置の要素は走査されないこと。また、範囲外の要素は走査されないこと
	@Test
	public void testTimelineBmsAtBmsAt_IncludeBeginExcludeEnd() {
		var c = contentForTimeline();
		var from = BmsPoint.of(5, Math.nextDown(BmsSpec.TICK_COUNT_DEFAULT));
		var to = BmsPoint.of(6, Math.nextUp(96.0));
		var e = c.timeline(from, to).collect(Collectors.toList());
		assertEquals(7, e.size());
		// 5小節目
		var i = 0;
		var m = 5;
		assertNoteElement(e.get(i++), m, Math.nextDown(BmsSpec.TICK_COUNT_DEFAULT), CN_A36S, 0, 900);
		// 6小節目
		m++;
		assertMeasureLineElement(e.get(i++), m);
		assertNoteElement(e.get(i++), m, Math.nextDown(96.0), CN_A36S, 0, 990);
		assertNoteElement(e.get(i++), m, 96.0, CN_A36, 0, 1000);
		assertNoteElement(e.get(i++), m, 96.0, CN_A36, 1, 1010);
		assertNoteElement(e.get(i++), m, 96.0, CN_A36, 2, 1020);
		assertNoteElement(e.get(i++), m, 96.0, CN_A36S, 0, 1030);
		//assertNoteElement(e.get(i++), m, Math.nextUp(96.0), CN_A36S, 0, 1040);
	}

	// timeline(BmsAt, BmsAt)
	// 正常：小節線上は小節線・小節データ・ノートの順、小節線上外はノートのみ、空小節でも小節線は走査されること
	@Test
	public void testTimelineBmsAtBmsAt_WithinSequence() {
		var c = contentForTimeline();
		var from = BmsPoint.of(0, 96.0);
		var to = BmsPoint.of(2, Math.nextUp(0.0));
		var e = c.timeline(from, to).collect(Collectors.toList());
		assertEquals(14, e.size());
		// 0小節目
		var i = 0;
		var m = 0;
		assertNoteElement(e.get(i++), m, 96.0, CN_A36S, 0, 100);
		// 1小節目
		m++;
		assertMeasureLineElement(e.get(i++), m);
		assertMeasureValueElement(e.get(i++), m, CN_STR, 0, "STRING1");
		assertMeasureValueElement(e.get(i++), m, CN_STR, 1, "STRING2");
		assertMeasureValueElement(e.get(i++), m, CN_INTS, 0, 10L);
		assertMeasureValueElement(e.get(i++), m, CN_OBJS, 0, BmsPoint.of(1, 2.0));
		assertNoteElement(e.get(i++), m, 0.0, CN_A36, 0, 200);
		assertNoteElement(e.get(i++), m, 0.0, CN_A36, 1, 300);
		assertNoteElement(e.get(i++), m, 0.0, CN_A36S, 0, 400);
		assertNoteElement(e.get(i++), m, Math.nextUp(0.0), CN_A36S, 0, 500);
		// 2小節目
		m++;
		assertMeasureLineElement(e.get(i++), m);
		assertNoteElement(e.get(i++), m, 0.0, CN_A36, 0, 600);
		assertNoteElement(e.get(i++), m, 0.0, CN_A36, 1, 700);
		assertNoteElement(e.get(i++), m, 0.0, CN_A36, 2, 800);
	}

	// timeline(BmsAt, BmsAt)
	// 正常：指定範囲の楽曲位置に要素がない場合、何も走査されないこと
	@Test
	public void testTimelineBmsAtBmsAt_NoElementInRange() {
		var c = contentForTimeline();
		var from = BmsPoint.of(3, 1.0);
		var to = BmsPoint.of(3, 190.0);
		var e = c.timeline(from, to).collect(Collectors.toList());
		assertEquals(0, e.size());
	}

	// timeline(BmsAt, BmsAt)
	// 正常：タイムラインが空の場合、何も走査されないこと
	@Test
	public void testTimelineBmsAtBmsAt_EmptyTimeline() {
		var c = contentForTimelineEmpty();
		var from = BmsPoint.of(0, 0.0);
		var to = BmsPoint.of(0, 0.0);
		var e = c.timeline(from, to).collect(Collectors.toList());
		assertEquals(0, e.size());
	}

	// timeline(BmsAt, BmsAt)
	// 正常：編集モードでも走査可能であること
	@Test
	public void testTimelineBmsAtBmsAt_EditMode() {
		var c = contentForTimeline();
		c.beginEdit();
		var from = BmsPoint.of(5, Math.nextDown(BmsSpec.TICK_COUNT_DEFAULT));
		var to = BmsPoint.of(6, Math.nextUp(96.0));
		var e = c.timeline(from, to).collect(Collectors.toList());
		assertEquals(7, e.size());
		// 同様のチェックを別テストで実施済みのため、アサーション省略
	}

	// timeline(BmsAt, BmsAt)
	// NullPointerException atBeginがnull
	@Test
	public void testTimelineBmsAtBmsAt_NullAtBegin() {
		var e = NullPointerException.class;
		var c = contentForTimeline();
		assertThrows(e, () -> c.timeline(null, BmsPoint.of(1, 0.0)));
	}

	// timeline(BmsAt, BmsAt)
	// NullPointerException atEndがnull
	@Test
	public void testTimelineBmsAtBmsAt_NullAtEnd() {
		var e = NullPointerException.class;
		var c = contentForTimeline();
		assertThrows(e, () -> c.timeline(BmsPoint.of(0, 0.0), null));
	}

	// timeline(BmsAt, BmsAt)
	// IllegalArgumentException 走査開始/終了楽曲位置の小節番号がマイナス値
	@Test
	public void testTimelineBmsAtBmsAt_NegativeMeasure() {
		var e = IllegalArgumentException.class;
		var c = contentForTimeline();
		assertThrows(e, () -> c.timeline(BmsPoint.of(-1, 0.0), BmsPoint.of(1, 0.0)));
		assertThrows(e, () -> c.timeline(BmsPoint.of(0, 0.0), BmsPoint.of(-1, 0.0)));
	}

	// timeline(BmsAt, BmsAt)
	// IllegalArgumentException 終了楽曲位置の小節の刻み位置が{@link BmsSpec#TICK_MIN}以外の時、小節番号が小節数以上
	@Test
	public void testTimelineBmsAtBmsAt_OverflowTimeline() {
		var e = IllegalArgumentException.class;
		var c = contentForTimeline();
		var from = BmsPoint.of(0, 0.0);
		var to = BmsPoint.of(c.getMeasureCount(), 1.0);
		assertThrows(e, () -> c.timeline(from, to));
	}

	// timeline(BmsAt, BmsAt)
	// IllegalArgumentException 終了楽曲位置の小節の刻み位置が{@link BmsSpec#TICK_MIN}の時、小節番号が小節数超過
	@Test
	public void testTimelineBmsAtBmsAt_OverTerminate() {
		var e = IllegalArgumentException.class;
		var c = contentForTimeline();
		var from = BmsPoint.of(0, 0.0);
		var to = BmsPoint.of(c.getMeasureCount() + 1, BmsSpec.TICK_MIN);
		assertThrows(e, () -> c.timeline(from, to));
	}

	// timeline(BmsAt, BmsAt)
	// IllegalArgumentException 走査開始/終了楽曲位置の楽曲位置の小節の刻み位置が{@link BmsSpec#TICK_MIN}未満、または{@link BmsSpec#TICK_MAX}超過
	@Test
	public void testTimelineBmsAtBmsAt_OutOfRangeTick() {
		var e = IllegalArgumentException.class;
		var c = contentForTimeline();
		assertThrows(e, () -> c.timeline(BmsPoint.of(1, Math.nextDown(BmsSpec.TICK_MIN)), BmsPoint.of(2, BmsSpec.TICK_MIN)));
		assertThrows(e, () -> c.timeline(BmsPoint.of(1, BmsSpec.TICK_MIN), BmsPoint.of(2, Math.nextDown(BmsSpec.TICK_MIN))));
		assertThrows(e, () -> c.timeline(BmsPoint.of(1, Math.nextUp(BmsSpec.TICK_MAX)), BmsPoint.of(2, BmsSpec.TICK_MIN)));
		assertThrows(e, () -> c.timeline(BmsPoint.of(1, BmsSpec.TICK_MIN), BmsPoint.of(2, Math.nextUp(BmsSpec.TICK_MAX))));
	}

	// timeline(BmsAt, BmsAt)
	// IllegalArgumentException atEndがatBeginと同じまたは手前の楽曲位置を示している
	@Test
	public void testTimelineBmsAtBmsAt_IllegalRange() {
		var e = IllegalArgumentException.class;
		var c = contentForTimeline();
		assertThrows(e, () -> c.timeline(BmsPoint.of(1, 0.0), BmsPoint.of(1, 0.0)));
		assertThrows(e, () -> c.timeline(BmsPoint.of(1, 0.0), BmsPoint.of(0, 191.0)));
	}

	// timeline(int, double, int, double)
	// 正常：走査開始楽曲位置の要素は走査され、走査終了楽曲位置の要素は走査されないこと。また、範囲外の要素は走査されないこと
	@Test
	public void testTimelineIntDoubleIntDouble_IncludeBeginExcludeEnd() {
		var c = contentForTimeline();
		var e = c.timeline(
				5, Math.nextDown(BmsSpec.TICK_COUNT_DEFAULT),
				6, Math.nextUp(96.0))
				.collect(Collectors.toList());
		assertEquals(7, e.size());
		// 5小節目
		var i = 0;
		var m = 5;
		assertNoteElement(e.get(i++), m, Math.nextDown(BmsSpec.TICK_COUNT_DEFAULT), CN_A36S, 0, 900);
		// 6小節目
		m++;
		assertMeasureLineElement(e.get(i++), m);
		assertNoteElement(e.get(i++), m, Math.nextDown(96.0), CN_A36S, 0, 990);
		assertNoteElement(e.get(i++), m, 96.0, CN_A36, 0, 1000);
		assertNoteElement(e.get(i++), m, 96.0, CN_A36, 1, 1010);
		assertNoteElement(e.get(i++), m, 96.0, CN_A36, 2, 1020);
		assertNoteElement(e.get(i++), m, 96.0, CN_A36S, 0, 1030);
		//assertNoteElement(e.get(i++), m, Math.nextUp(96.0), CN_A36S, 0, 1040);
	}

	// timeline(int, double, int, double)
	// 正常：小節線上は小節線・小節データ・ノートの順、小節線上外はノートのみ、空小節でも小節線は走査されること
	@Test
	public void testTimelineIntDoubleIntDouble_WithinSequence() {
		var c = contentForTimeline();
		var e = c.timeline(0, 96.0, 2, Math.nextUp(0.0)).collect(Collectors.toList());
		assertEquals(14, e.size());
		// 0小節目
		var i = 0;
		var m = 0;
		assertNoteElement(e.get(i++), m, 96.0, CN_A36S, 0, 100);
		// 1小節目
		m++;
		assertMeasureLineElement(e.get(i++), m);
		assertMeasureValueElement(e.get(i++), m, CN_STR, 0, "STRING1");
		assertMeasureValueElement(e.get(i++), m, CN_STR, 1, "STRING2");
		assertMeasureValueElement(e.get(i++), m, CN_INTS, 0, 10L);
		assertMeasureValueElement(e.get(i++), m, CN_OBJS, 0, BmsPoint.of(1, 2.0));
		assertNoteElement(e.get(i++), m, 0.0, CN_A36, 0, 200);
		assertNoteElement(e.get(i++), m, 0.0, CN_A36, 1, 300);
		assertNoteElement(e.get(i++), m, 0.0, CN_A36S, 0, 400);
		assertNoteElement(e.get(i++), m, Math.nextUp(0.0), CN_A36S, 0, 500);
		// 2小節目
		m++;
		assertMeasureLineElement(e.get(i++), m);
		assertNoteElement(e.get(i++), m, 0.0, CN_A36, 0, 600);
		assertNoteElement(e.get(i++), m, 0.0, CN_A36, 1, 700);
		assertNoteElement(e.get(i++), m, 0.0, CN_A36, 2, 800);
	}

	// timeline(int, double, int, double)
	// 正常：指定範囲の楽曲位置に要素がない場合、何も走査されないこと
	@Test
	public void testTimelineIntDoubleIntDouble_NoElementInRange() {
		var c = contentForTimeline();
		var e = c.timeline(3, 1.0, 3, 190.0).collect(Collectors.toList());
		assertEquals(0, e.size());
	}

	// timeline(int, double, int, double)
	// 正常：タイムラインが空の場合、何も走査されないこと
	@Test
	public void testTimelineIntDoubleIntDouble_EmptyTimeline() {
		var c = contentForTimelineEmpty();
		var e = c.timeline(0, 0.0, 0, 0.0).collect(Collectors.toList());
		assertEquals(0, e.size());
	}

	// timeline(int, double, int, double)
	// 正常：編集モードでも走査可能であること
	@Test
	public void testTimelineIntDoubleIntDouble_EditMode() {
		var c = contentForTimeline();
		c.beginEdit();
		var e = c.timeline(
				5, Math.nextDown(BmsSpec.TICK_COUNT_DEFAULT),
				6, Math.nextUp(96.0))
				.collect(Collectors.toList());
		assertEquals(7, e.size());
		// 同様のチェックを別テストで実施済みのため、アサーション省略
	}

	// timeline(int, double, int, double)
	// IllegalArgumentException 走査開始/終了楽曲位置の小節番号がマイナス値
	@Test
	public void testTimelineIntDoubleIntDouble_NegativeMeasure() {
		var e = IllegalArgumentException.class;
		var c = contentForTimeline();
		assertThrows(e, () -> c.timeline(-1, 0.0, 1, 0.0));
		assertThrows(e, () -> c.timeline(0, 0.0, -1, 0.0));
	}

	// timeline(int, double, int, double)
	// IllegalArgumentException 走査終了楽曲位置の小節の刻み位置が{@link BmsSpec#TICK_MIN}以外の時、小節番号が小節数以上
	@Test
	public void testTimelineIntDoubleIntDouble_OverflowTimeline() {
		var e = IllegalArgumentException.class;
		var c = contentForTimeline();
		assertThrows(e, () -> c.timeline(0, 0.0, c.getMeasureCount(), 1.0));
	}

	// timeline(int, double, int, double)
	// IllegalArgumentException 走査終了楽曲位置の小節の刻み位置が{@link BmsSpec#TICK_MIN}の時、小節番号が小節数超過
	@Test
	public void testTimelineIntDoubleIntDouble_OverTerminate() {
		var e = IllegalArgumentException.class;
		var c = contentForTimeline();
		assertThrows(e, () -> c.timeline(0, 0.0, c.getMeasureCount() + 1, BmsSpec.TICK_MIN));
	}

	// timeline(int, double, int, double)
	// IllegalArgumentException 走査開始/終了楽曲位置の楽曲位置の小節の刻み位置が{@link BmsSpec#TICK_MIN}未満、または{@link BmsSpec#TICK_MAX}超過
	@Test
	public void testTimelineIntDoubleIntDouble_OutOfRangeTick() {
		var e = IllegalArgumentException.class;
		var c = contentForTimeline();
		assertThrows(e, () -> c.timeline(1, Math.nextDown(BmsSpec.TICK_MIN), 2, BmsSpec.TICK_MIN));
		assertThrows(e, () -> c.timeline(1, BmsSpec.TICK_MIN, 2, Math.nextDown(BmsSpec.TICK_MIN)));
		assertThrows(e, () -> c.timeline(1, Math.nextUp(BmsSpec.TICK_MAX), 2, BmsSpec.TICK_MIN));
		assertThrows(e, () -> c.timeline(1, BmsSpec.TICK_MIN, 2, Math.nextUp(BmsSpec.TICK_MAX)));
	}

	// timeline(int, double, int, double)
	// IllegalArgumentException atEndがatBeginと同じまたは手前の楽曲位置を示している
	@Test
	public void testTimelineIntDoubleIntDouble_IllegalRange() {
		var e = IllegalArgumentException.class;
		var c = contentForTimeline();
		assertThrows(e, () -> c.timeline(1, 0.0, 1, 0.0));
		assertThrows(e, () -> c.timeline(1, 0.0, 0, 191.0));
	}

	// putTimeline(BmsTimelineElement)
	// BMS仕様に存在する小節データを追加できること
	@Test
	public void testPutTimeline_MeasureValues() {
		var c = new BmsContent(spec());
		var tls = List.of(
				new BmsMeasureValue(0, CN_INTS, 0, 100L),
				new BmsMeasureValue(1, CN_NUMS, 0, 200.345),
				new BmsMeasureValue(2, CN_STRS, 0, "STR"),
				new BmsMeasureValue(3, CN_B16S, 0, (long)BmsInt.to16i("AB")),
				new BmsMeasureValue(4, CN_B36S, 0, (long)BmsInt.to36i("XY")),
				new BmsMeasureValue(7, CN_OBJS, 0, "OBJ"));
		c.beginEdit();
		tls.stream().forEach(c::putTimeline);
		c.endEdit();
		assertEquals(100L, (long)c.getMeasureValue(CN_INTS, 0));
		assertEquals(200.345, c.getMeasureValue(CN_NUMS, 1), 0.0);
		assertEquals("STR", c.getMeasureValue(CN_STRS, 2));
		assertEquals(BmsInt.to16i("AB"), (long)c.getMeasureValue(CN_B16S, 3));
		assertEquals(BmsInt.to36i("XY"), (long)c.getMeasureValue(CN_B36S, 4));
		assertEquals("OBJ", c.getMeasureValue(CN_OBJS, 7));
	}

	// putTimeline(BmsTimelineElement)
	// BMS仕様に存在するノートを追加できること
	@Test
	public void testPutTimeline_Notes() {
		var c = new BmsContent(spec());
		var tls = List.of(
				newBmsNote(CN_A16S, 0, 0, 0.0, BmsInt.to16i("AB")),
				newBmsNote(CN_A36S, 0, 0, 0.0, BmsInt.to36i("WX")),
				newBmsNote(CN_A36S, 0, 0, 96.0, BmsInt.to36i("XY")),
				newBmsNote(CN_A36, 0, 1, 48.0, BmsInt.to36i("PP")),
				newBmsNote(CN_A36, 1, 1, 48.0, BmsInt.to36i("QQ")),
				newBmsNote(CN_A36S, 0, 2, 48.0, BmsInt.to36i("YZ")));
		c.beginEdit();
		tls.stream().forEach(c::putTimeline);
		c.endEdit();
		assertEquals(BmsInt.to16i("AB"), c.getNote(CN_A16S, 0, 0, 0.0).getValue());
		assertEquals(BmsInt.to36i("WX"), c.getNote(CN_A36S, 0, 0, 0.0).getValue());
		assertEquals(BmsInt.to36i("XY"), c.getNote(CN_A36S, 0, 0, 96.0).getValue());
		assertEquals(BmsInt.to36i("PP"), c.getNote(CN_A36, 0, 1, 48.0).getValue());
		assertEquals(BmsInt.to36i("QQ"), c.getNote(CN_A36, 1, 1, 48.0).getValue());
		assertEquals(BmsInt.to36i("YZ"), c.getNote(CN_A36S, 0, 2, 48.0).getValue());
	}

	// putTimeline(BmsTimelineElement)
	// 小節線が指定されても何も行われないこと
	@Test
	public void testPutTimeline_MeasureLine() {
		var ml = new MeasureElement(3) {
			@Override protected MeasureTimeInfo onRecalculateTimeInfo(Object userParam) { return null; }
		};
		var c = new BmsContent(spec());
		c.beginEdit();
		c.putTimeline(ml);
		c.endEdit();
		assertEquals(0, c.getMeasureCount());  // 小節線がデータとして追加されていないことを確認
	}

	// putTimeline(BmsTimelineElement)
	// IllegalStateException 動作モードが編集モードではない
	@Test
	public void testPutTimeline_NotEditMode() {
		var c = new BmsContent(spec());
		var e = new BmsMeasureValue(0, 1, 0, 0L);
		assertThrows(IllegalStateException.class, () -> c.putTimeline(e));
	}

	// putTimeline(BmsTimelineElement)
	// NullPointerException timelineがnull
	@Test
	public void testPutTimeline_NullTimeline() {
		var c = new BmsContent(spec());
		c.beginEdit();
		assertThrows(NullPointerException.class, () -> c.putTimeline(null));
	}

	// putTimeline(BmsTimelineElement)
	// IllegalArgumentException 小節データ：BMS仕様にないチャンネル番号を指定した
	@Test
	public void testPutTimeline_MeasureValue_WrongChannel() {
		var c = new BmsContent(spec());
		var e = new BmsMeasureValue(0, BmsSpec.CHANNEL_MAX, 0, 0L);
		c.beginEdit();
		assertThrows(IllegalArgumentException.class, () -> c.putTimeline(e));
	}

	// putTimeline(BmsTimelineElement)
	// IndexOutOfBoundsException 小節データ：チャンネルインデックスがマイナス値
	@Test
	public void testPutTimeline_MeasureValue_MinusChIndex() {
		var c = new BmsContent(spec());
		var e = new BmsMeasureValue(0, CN_INTS, -1, 0L);
		c.beginEdit();
		assertThrows(IndexOutOfBoundsException.class, () -> c.putTimeline(e));
	}

	// putTimeline(BmsTimelineElement)
	// IndexOutOfBoundsException 小節データ：データ重複不許可のチャンネルで0以外のインデックスを指定した
	@Test
	public void testPutTimeline_MeasureValue_NotMultiple() {
		var c = new BmsContent(spec());
		var e = new BmsMeasureValue(0, CN_STRS, 1, "STR");
		c.beginEdit();
		assertThrows(IndexOutOfBoundsException.class, () -> c.putTimeline(e));
	}

	// putTimeline(BmsTimelineElement)
	// IllegalArgumentException 小節データ：小節番号が{@link BmsSpec#MEASURE_MIN}未満または{@link BmsSpec#MEASURE_MAX}超過
	@Test
	public void testPutTimeline_MeasureValue_MeasureOutOfRange() {
		var c = new BmsContent(spec());
		var e1 = new BmsMeasureValue(BmsSpec.MEASURE_MIN - 1, CN_NUMS, 0, 100.0);
		var e2 = new BmsMeasureValue(BmsSpec.MEASURE_MAX + 1, CN_NUMS, 0, 200.0);
		var ex = IllegalArgumentException.class;
		c.beginEdit();
		assertThrows(ex, () -> c.putTimeline(e1));
		assertThrows(ex, () -> c.putTimeline(e2));
	}

	// putTimeline(BmsTimelineElement)
	// IllegalArgumentException 小節データ：チャンネルのデータ型が値型、任意型ではない
	@Test
	public void testPutTimeline_MeasureValue_IsArrayType() {
		var c = new BmsContent(spec());
		var e1 = new BmsMeasureValue(0, CN_A16S, 0, new BmsArray("AABB", 16));
		var e2 = new BmsMeasureValue(0, CN_A36S, 0, new BmsArray("XXYY", 36));
		var ex = IllegalArgumentException.class;
		c.beginEdit();
		assertThrows(ex, () -> c.putTimeline(e1));
		assertThrows(ex, () -> c.putTimeline(e2));
	}

	// putTimeline(BmsTimelineElement)
	// ClassCastException 小節データ：値をチャンネルのデータ型に変換できない
	@Test
	public void testPutTimeline_MeasureValue_BadCast() {
		var c = new BmsContent(spec());
		var e = new BmsMeasureValue(0, CN_INTS, 0, "STR");
		c.beginEdit();
		assertThrows(ClassCastException.class, () -> c.putTimeline(e));
	}

	// putTimeline(BmsTimelineElement)
	// IllegalArgumentException 小節データ：小節長に{@link BmsSpec#LENGTH_MIN}未満または{@link BmsSpec#LENGTH_MAX}超過の値を設定しようとした
	@Test
	public void testPutTimeline_MeasureValue_MeasureLengthOutOfRange() {
		var c = new BmsContent(spec());
		var e1 = new BmsMeasureValue(0, 2, 0, Math.nextDown(BmsSpec.LENGTH_MIN));
		var e2 = new BmsMeasureValue(0, 2, 0, Math.nextUp(BmsSpec.LENGTH_MAX));
		var ex = IllegalArgumentException.class;
		c.beginEdit();
		assertThrows(ex, () -> c.putTimeline(e1));
		assertThrows(ex, () -> c.putTimeline(e2));
	}

	// putTimeline(BmsTimelineElement)
	// IllegalArgumentException ノート：BMS仕様にないチャンネル番号を指定した
	@Test
	public void testPutTimeline_Note_WrongChannel() {
		var c = new BmsContent(spec());
		var e = newBmsNote(BmsSpec.CHANNEL_MAX, 0, 0, 0.0, 0);
		c.beginEdit();
		assertThrows(IllegalArgumentException.class, () -> c.putTimeline(e));
	}

	// putTimeline(BmsTimelineElement)
	// IndexOutOfBoundsException ノート：チャンネルインデックスがマイナス値
	@Test
	public void testPutTimeline_Note_MinusChIndex() {
		var c = new BmsContent(spec());
		var e = newBmsNote(CN_A36, -1, 0, 0.0, 0);
		c.beginEdit();
		assertThrows(IndexOutOfBoundsException.class, () -> c.putTimeline(e));
	}

	// putTimeline(BmsTimelineElement)
	// IndexOutOfBoundsException ノート：重複不許可のチャンネルで0以外のチャンネルインデックスを指定した
	@Test
	public void testPutTimeline_Note_NotMultiple() {
		var c = new BmsContent(spec());
		var e = newBmsNote(CN_A36S, 1, 0, 0.0, 0);
		c.beginEdit();
		assertThrows(IndexOutOfBoundsException.class, () -> c.putTimeline(e));
	}

	// putTimeline(BmsTimelineElement)
	// IllegalArgumentException ノート：小節番号が{@link BmsSpec#MEASURE_MIN}未満または{@link BmsSpec#MEASURE_MAX}超過
	@Test
	public void testPutTimeline_Note_MeasureOutOfRange() {
		var c = new BmsContent(spec());
		var e1 = newBmsNote(CN_A36S, 0, BmsSpec.MEASURE_MIN - 1, 0.0, 0);
		var e2 = newBmsNote(CN_A36S, 0, BmsSpec.MEASURE_MAX + 1, 0.0, 0);
		var ex = IllegalArgumentException.class;
		c.beginEdit();
		assertThrows(ex, () -> c.putTimeline(e1));
		assertThrows(ex, () -> c.putTimeline(e2));
	}

	// putTimeline(BmsTimelineElement)
	// IllegalArgumentException ノート：小節の刻み位置がマイナス値または当該小節の刻み数以上
	@Test
	public void testPutTimeline_Note_TickOutOfRange() {
		var c = new BmsContent(spec());
		var e1 = newBmsNote(CN_A36S, 0, 0, Math.nextDown(0.0), 0);
		var e2 = newBmsNote(CN_A36S, 0, 0, BmsSpec.TICK_COUNT_DEFAULT, 0);
		var ex = IllegalArgumentException.class;
		c.beginEdit();
		assertThrows(ex, () -> c.putTimeline(e1));
		assertThrows(ex, () -> c.putTimeline(e2));
	}

	// putTimeline(BmsTimelineElement)
	// IllegalArgumentException ノート：指定チャンネルのデータ型が配列型ではない
	@Test
	public void testPutTimeline_Note_NotArrayType() {
		var c = new BmsContent(spec());
		var e1 = newBmsNote(CN_INTS, 0, 0, 0.0, 0);
		var e2 = newBmsNote(CN_NUMS, 0, 0, 0.0, 0);
		var e3 = newBmsNote(CN_STRS, 0, 0, 0.0, 0);
		var e4 = newBmsNote(CN_A16S, 0, 0, 0.0, 0);
		var e5 = newBmsNote(CN_A36S, 0, 0, 0.0, 0);
		var e6 = newBmsNote(CN_OBJS, 0, 0, 0.0, 0);
		var ex = IllegalArgumentException.class;
		c.beginEdit();
		assertThrows(ex, () -> c.putTimeline(e1));
		assertThrows(ex, () -> c.putTimeline(e2));
		assertThrows(ex, () -> c.putTimeline(e3));
		assertThrows(ex, () -> c.putTimeline(e4));
		assertThrows(ex, () -> c.putTimeline(e5));
		assertThrows(ex, () -> c.putTimeline(e6));
	}

	// pointOf(BmsAt, Tester)
	// 正常系のみ
	@Test
	public void testPointOfBmsAtTester_001() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.putNote(1, 0, 0, 0.0, 900);
		c.putNote(1, 1, 0, 0.0, 911);
		c.putNote(1, 0, 5, 96.0, 922);
		c.putNote(1, 1, 5, 96.0, 933);
		c.putNote(11, 0, 0, 0.0, 100);
		c.putNote(11, 0, 1, 16.0, 200);
		c.putNote(11, 0, 2, 32.0, 300);
		c.putNote(11, 0, 3, 48.0, 400);
		c.putNote(11, 0, 4, 64.0, 500);
		c.putNote(21, 0, 0, 24.0, 10);
		c.putNote(21, 0, 2, 48.0, 20);
		c.putNote(21, 0, 4, 72.0, 30);
		c.putNote(21, 0, 6, 96.0, 40);
		c.putNote(21, 0, 8, 120.0, 50);
		c.endEdit();

		// 最初に見つかったノートを返す
		BmsNote p;
		// チャンネルインデックスは0から大きいほうに向かって検索する
		p = c.pointOf(new BmsPoint(0, 0), n -> (n.getChannel() == 1) && (n.getMeasure() == 5));
		assertEquals(0, p.getIndex());
		assertEquals(96, p.getTick(), 0.0);
		assertEquals(922, p.getValue());
		// 小節番号・刻み位置は0から大きいほうに向かって検索する
		p = c.pointOf(new BmsPoint(0, 0), n -> n.getTick() == 48);
		assertEquals(21, p.getChannel());
		assertEquals(0, p.getIndex());
		assertEquals(2, p.getMeasure());
		assertEquals(20, p.getValue());

		// 検索開始位置より前のノートは検索対象にならない
		p = c.pointOf(new BmsPoint(1, 0), n -> n.getIndex() == 1);
		assertEquals(1, p.getChannel());
		assertEquals(5, p.getMeasure());
		assertEquals(96, p.getTick(), 0.0);
		assertEquals(933, p.getValue());
		p = c.pointOf(new BmsPoint(6, 97), n -> true);
		assertEquals(21, p.getChannel());
		assertEquals(0, p.getIndex());
		assertEquals(8, p.getMeasure());
		assertEquals(120, p.getTick(), 0.0);
		assertEquals(50, p.getValue());

		// 該当するノートが見つからない
		p = c.pointOf(new BmsPoint(0, 0), n -> false);
		assertNull(p);
	}

	// pointOf(int, double, Tester)
	// 正常
	@Test
	public void testPointOfIntDoubleTester_001() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.putNote(1, 0, 0, 0.0, 900);
		c.putNote(1, 1, 0, 0.0, 911);
		c.putNote(1, 0, 5, 96.0, 922);
		c.putNote(1, 1, 5, 96.0, 933);
		c.putNote(11, 0, 0, 0.0, 100);
		c.putNote(11, 0, 1, 16.0, 200);
		c.putNote(11, 0, 2, 32.0, 300);
		c.putNote(11, 0, 3, 48.0, 400);
		c.putNote(11, 0, 4, 64.0, 500);
		c.putNote(21, 0, 0, 24.0, 10);
		c.putNote(21, 0, 2, 48.0, 20);
		c.putNote(21, 0, 4, 72.0, 30);
		c.putNote(21, 0, 6, 96.0, 40);
		c.putNote(21, 0, 8, 120.0, 50);
		c.endEdit();

		// 最初に見つかったノートを返す
		BmsNote p;
		// チャンネルインデックスは0から大きいほうに向かって検索する
		p = c.pointOf(0, 0, n -> (n.getChannel() == 1) && (n.getMeasure() == 5));
		assertEquals(0, p.getIndex());
		assertEquals(96, p.getTick(), 0.0);
		assertEquals(922, p.getValue());
		// 小節番号・刻み位置は0から大きいほうに向かって検索する
		p = c.pointOf(0, 0, n -> n.getTick() == 48);
		assertEquals(21, p.getChannel());
		assertEquals(0, p.getIndex());
		assertEquals(2, p.getMeasure());
		assertEquals(20, p.getValue());

		// 検索開始位置より前のノートは検索対象にならない
		p = c.pointOf(1, 0, n -> n.getIndex() == 1);
		assertEquals(1, p.getChannel());
		assertEquals(5, p.getMeasure());
		assertEquals(96, p.getTick(), 0.0);
		assertEquals(933, p.getValue());
		p = c.pointOf(6, 97, n -> true);
		assertEquals(21, p.getChannel());
		assertEquals(0, p.getIndex());
		assertEquals(8, p.getMeasure());
		assertEquals(120, p.getTick(), 0.0);
		assertEquals(50, p.getValue());

		// 該当するノートが見つからない
		p = c.pointOf(0, 0, n -> false);
		assertNull(p);
	}

	// pointOf(int, double, Tester)
	// IllegalArgumentException 検索開始位置の小節番号にノート・小節データが存在し得ない値を指定した
	@Test
	public void testPointOfIntDoubleTester_002() {
		var c = new BmsContent(spec());
		var ex = IllegalArgumentException.class;
		c.beginEdit();
		c.putNote(11, 0, 9, 0, 1);
		c.endEdit();
		assertThrows(ex, () -> c.pointOf(BmsSpec.MEASURE_MIN - 1, 0, n -> false));
		assertThrows(ex, () -> c.pointOf(10, 1, n -> false));
		assertThrows(ex, () -> c.pointOf(11, 0, n -> false));
	}

	// pointOf(int, double, Tester)
	// IllegalArgumentException 検索開始位置の刻み位置にマイナス値または当該小節の刻み数以上の値を指定した
	@Test
	public void testPointOfIntDoubleTester_003() {
		var c = new BmsContent(spec());
		var ex = IllegalArgumentException.class;
		c.beginEdit();
		c.putNote(11, 0, 9, 0, 1);
		c.endEdit();
		assertThrows(ex, () -> c.pointOf(0, BmsSpec.TICK_MIN - 1, n -> false));
		assertThrows(ex, () -> c.pointOf(0, 192, n -> false));
	}

	// pointOf(int, double, Tester)
	// NullPointerException judgeがnull
	@Test
	public void testPointOfIntDoubleTester_004() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.putNote(11, 0, 9, 0, 1);
		c.endEdit();
		assertThrows(NullPointerException.class, () -> c.pointOf(0, 0, null));
	}

	// getResolvedNoteValue(BmsNote)
	// 正常
	@Test
	public void testGetResolvedNoteValueBmsNote_001() {
		var c = new BmsContent(spec());
		var td = getNormalGetResolvedNoteValueTestData(c, true);
		td.forEach(d -> {
			var n = new BmsNote();
			n.setChx(d.channel, d.index);
			n.setMeasure(d.measure);
			n.setTick(d.tick);
			n.setValue(d.noteValue);
			var v = c.getResolvedNoteValue(n);
			assertNotNull(v);
			assertEquals(d.metaValue, v);
			assertEquals(d.valueType, v.getClass());
		});
	}

	// getResolvedNoteValue(BmsNote)
	// NullPointerException noteがnull
	@Test
	public void testGetResolvedNoteValueBmsNote_002() {
		var c = new BmsContent(spec());
		assertThrows(NullPointerException.class, () -> c.getResolvedNoteValue(CN_INT, (BmsAt)null));
	}

	private static class GetResolvedNoteValueTestData {
		int channel, index, measure, noteValue;
		double tick;
		Object metaValue;
		Class<?> valueType;
		GetResolvedNoteValueTestData(int c, int i, int m, double t, int nv, Object mv, Class<?> vt) {
			channel = c; index = i; measure = m; tick = t; noteValue = nv; metaValue = mv; valueType = vt;
		}
	}

	// getResolvedNoteValue(int, BmsAt)
	// 正常
	@Test
	public void testGetResolvedNoteValueIntBmsAt_001() {
		var c = new BmsContent(spec());
		var td = getNormalGetResolvedNoteValueTestData(c, true);
		td.forEach(d -> {
			var v = c.getResolvedNoteValue(d.channel, new BmsPoint(d.measure, d.tick));
			assertNotNull(v);
			assertEquals(d.metaValue, v);
			assertEquals(d.valueType, v.getClass());
		});
	}

	// getResolvedNoteValue(int, BmsAt)
	// 正常：存在しないノートを指定すると参照先メタ情報の初期値を返すこと
	@Test
	public void testGetResolvedNoteValueIntBmsAt_001_2() {
		var s = spec();
		var c = new BmsContent(s);
		var r = c.getResolvedNoteValue(CN_A36S_STR, new BmsPoint(BmsSpec.MEASURE_MAX, 0));
		assertNotNull(r);
		assertTrue(r instanceof String);
		assertEquals(s.getIndexedMeta(s.getChannel(CN_A36S_STR).getRef()).getDefaultValue(), r);
	}

	// getResolvedNoteValue(int, BmsAt)
	// 正常：存在しないノートを指定し、且つ参照先メタ情報がない場合0を返すこと
	@Test
	public void testGetResolvedNoteValueIntBmsAt_001_3() {
		var s = spec();
		var c = new BmsContent(s);
		var r = c.getResolvedNoteValue(CN_A36S, new BmsPoint(BmsSpec.MEASURE_MAX, 0));
		assertNotNull(r);
		assertTrue(r instanceof Long);
		assertEquals(0L, ((Long)r).longValue());
	}

	// getResolvedNoteValue(int, BmsAt)
	// NullPointerException atがnull
	@Test
	public void testGetResolvedNoteValueIntBmsAt_002() {
		var c = new BmsContent(spec());
		assertThrows(NullPointerException.class, () -> c.getResolvedNoteValue(CN_INT, (BmsAt)null));
	}

	// getResolvedNoteValue(int, int, BmsAt)
	// 正常
	@Test
	public void testGetResolvedNoteValueIntIntBmsAt_001() {
		var c = new BmsContent(spec());
		var td = getNormalGetResolvedNoteValueTestData(c, false);
		td.forEach(d -> {
			var v = c.getResolvedNoteValue(d.channel, d.index, new BmsPoint(d.measure, d.tick));
			assertNotNull(v);
			assertEquals(d.metaValue, v);
			assertEquals(d.valueType, v.getClass());
		});
	}

	// getResolvedNoteValue(int, int, BmsAt)
	// 正常：存在しないノートを指定すると参照先メタ情報の初期値を返すこと
	@Test
	public void testGetResolvedNoteValueIntIntBmsAt_001_2() {
		var s = spec();
		var c = new BmsContent(s);
		var r = c.getResolvedNoteValue(CN_A36S_STR, 0, new BmsPoint(BmsSpec.MEASURE_MAX, 0));
		assertNotNull(r);
		assertTrue(r instanceof String);
		assertEquals(s.getIndexedMeta(s.getChannel(CN_A36S_STR).getRef()).getDefaultValue(), r);
	}

	// getResolvedNoteValue(int, int, BmsAt)
	// 正常：存在しないノートを指定し、且つ参照先メタ情報がない場合0を返すこと
	@Test
	public void testGetResolvedNoteValueIntIntBmsAt_001_3() {
		var s = spec();
		var c = new BmsContent(s);
		var r = c.getResolvedNoteValue(CN_A36S, 0, new BmsPoint(BmsSpec.MEASURE_MAX, 0));
		assertNotNull(r);
		assertTrue(r instanceof Long);
		assertEquals(0L, ((Long)r).longValue());
	}

	// getResolvedNoteValue(int, int, BmsAt)
	// NullPointerException atがnull
	@Test
	public void testGetResolvedNoteValueIntIntBmsAt_002() {
		var c = new BmsContent(spec());
		assertThrows(NullPointerException.class, () -> c.getResolvedNoteValue(CN_INT, 0, (BmsAt)null));
	}

	// getResolvedNoteValue(int, int, double)
	// 正常系のみ
	@Test
	public void testGetResolvedNoteValueIntIntDouble_001() {
		var c = new BmsContent(spec());
		var td = getNormalGetResolvedNoteValueTestData(c, true);
		td.forEach(d -> {
			var v = c.getResolvedNoteValue(d.channel, d.measure, d.tick);
			assertNotNull(v);
			assertEquals(d.metaValue, v);
			assertEquals(d.valueType, v.getClass());
		});
	}

	// getResolvedNoteValue(int, int, double)
	// 正常：存在しないノートを指定すると参照先メタ情報の初期値を返すこと
	@Test
	public void testGetResolvedNoteValueIntIntDouble_001_2() {
		var s = spec();
		var c = new BmsContent(s);
		var r = c.getResolvedNoteValue(CN_A36S_STR, BmsSpec.MEASURE_MAX, 0.0);
		assertNotNull(r);
		assertTrue(r instanceof String);
		assertEquals(s.getIndexedMeta(s.getChannel(CN_A36S_STR).getRef()).getDefaultValue(), r);
	}

	// getResolvedNoteValue(int, int, double)
	// 正常：存在しないノートを指定し、且つ参照先メタ情報がない場合0を返すこと
	@Test
	public void testGetResolvedNoteValueIntIntDouble_001_3() {
		var s = spec();
		var c = new BmsContent(s);
		var r = c.getResolvedNoteValue(CN_A36S, BmsSpec.MEASURE_MAX, 0.0);
		assertNotNull(r);
		assertTrue(r instanceof Long);
		assertEquals(0L, ((Long)r).longValue());
	}

	// getResolvedNoteValue(int, int, int, double)
	// 正常
	@Test
	public void testGetResolvedNoteValueIntIntIntDouble_001() {
		var c = new BmsContent(spec());
		var td = getNormalGetResolvedNoteValueTestData(c, false);
		td.forEach(d -> {
			var v = c.getResolvedNoteValue(d.channel, d.index, d.measure, d.tick);
			assertNotNull(v);
			assertEquals(d.metaValue, v);
			assertEquals(d.valueType, v.getClass());
		});
	}

	// getResolvedNoteValue(int, int, int, double)
	// 正常：存在しないノートを指定すると参照先メタ情報の初期値を返すこと
	@Test
	public void testGetResolvedNoteValueIntIntIntDouble_001_2() {
		var s = spec();
		var c = new BmsContent(s);
		var r = c.getResolvedNoteValue(CN_A36S_STR, 0, BmsSpec.MEASURE_MAX, 0);
		assertNotNull(r);
		assertTrue(r instanceof String);
		assertEquals(s.getIndexedMeta(s.getChannel(CN_A36S_STR).getRef()).getDefaultValue(), r);
	}

	// getResolvedNoteValue(int, int, int, double)
	// 正常：存在しないノートを指定し、且つ参照先メタ情報がない場合0を返すこと
	@Test
	public void testGetResolvedNoteValueIntIntIntDouble_001_3() {
		var s = spec();
		var c = new BmsContent(s);
		var r = c.getResolvedNoteValue(CN_A36S, 0, BmsSpec.MEASURE_MAX, 0);
		assertNotNull(r);
		assertTrue(r instanceof Long);
		assertEquals(0L, ((Long)r).longValue());
	}

	// getResolvedNoteValue(int, int, int, double)
	// IllegalArgumentException BMS仕様にないチャンネル番号を指定した
	@Test
	public void testGetResolvedNoteValueIntIntIntDouble_002() {
		var c = new BmsContent(spec());
		assertThrows(IllegalArgumentException.class, () -> c.getResolvedNoteValue(999, 0, 0, 0));
	}

	// getResolvedNoteValue(int, int, int, double)
	// IndexOutOfBoundsException チャンネルインデックスがマイナス値
	@Test
	public void testGetResolvedNoteValueIntIntIntDouble_003() {
		var c = new BmsContent(spec());
		assertThrows(IndexOutOfBoundsException.class, () -> c.getResolvedNoteValue(CN_A36, -1, 0, 0));
	}

	// getResolvedNoteValue(int, int, int, double)
	// IndexOutOfBoundsException 重複不許可のチャンネルで0以外のチャンネルインデックスを指定した
	@Test
	public void testGetResolvedNoteValueIntIntIntDouble_004() {
		var c = new BmsContent(spec());
		assertThrows(IndexOutOfBoundsException.class, () -> c.getResolvedNoteValue(CN_A36S, 1, 0, 0));
	}

	// getResolvedNoteValue(int, int, int, double)
	// IllegalArgumentException 小節番号が{@link BmsSpec#MEASURE_MIN}未満または{@link BmsSpec#MEASURE_MAX}超過
	@Test
	public void testGetResolvedNoteValueIntIntIntDouble_005() {
		var c = new BmsContent(spec());
		var ex = IllegalArgumentException.class;
		c.beginEdit();
		c.putNote(1, 0, 1, 0, 1000);
		c.endEdit();
		assertThrows(ex, () -> c.getResolvedNoteValue(CN_A36, 0, BmsSpec.MEASURE_MIN - 1, 0));
		assertThrows(ex, () -> c.getResolvedNoteValue(CN_A36, 0, BmsSpec.MEASURE_MAX + 1, 0));
	}

	// getResolvedNoteValue(int, int, int, double)
	// IllegalArgumentException マイナス値または当該小節の刻み数以上の刻み位置を指定した
	@Test
	public void testGetResolvedNoteValueIntIntIntDouble_006() {
		var c = new BmsContent(spec());
		var ex = IllegalArgumentException.class;
		c.beginEdit();
		c.putNote(1, 0, 1, 0, 1000);
		c.endEdit();
		assertThrows(ex, () -> c.getResolvedNoteValue(CN_A36, 0, 0, -1));
		assertThrows(ex, () -> c.getResolvedNoteValue(CN_A36, 0, 0, 192));
	}

	// getResolvedNoteValue(int, int, int, double)
	// IllegalArgumentException 指定チャンネルのデータ型が配列型ではない
	@Test
	public void testGetResolvedNoteValueIntIntIntDouble_007() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.setMeasureValue(CN_INT, 0, 100L);
		c.endEdit();
		assertThrows(IllegalArgumentException.class, () -> c.getResolvedNoteValue(CN_INT, 0, 0, 0));
	}

	// enumNotes(Tester)
	// 正常系のみ
	@Test
	public void testEnumNotesTester_001() {
		var c = new BmsContent(spec());
		getEnumNotesTestData(c);
		var all = c.countNotes(n -> true);

		// 全ノートが列挙されること
		int[] counter = { 0 };
		c.enumNotes(n -> counter[0]++);
		assertEquals(all, counter[0]);
	}

	// enumNotes(BmsAt, Tester)
	// 正常
	@Test
	public void testEnumNotesBmsAtBmsNoteTester_001() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.putNote(CN_A36S, 100, 0.0, 599);
		c.putNote(CN_A36S, 100, 96.0, 699);
		c.putNote(CN_A36S, 150, 0.0, 799);
		c.putNote(CN_A36S, 150, 96.0, 899);
		c.putNote(CN_A36, 2, 100, 0.0, 911);
		c.putNote(CN_A36, 4, 100, 96.0, 922);
		c.putNote(CN_A36, 2, 150, 0.0, 933);
		c.putNote(CN_A36, 4, 150, 96.0, 944);
		c.endEdit();

		// 小節番号、刻み位置が一致するノートが列挙されること
		// 小節番号が同一で刻み位置が異なるノートが列挙されないこと
		// 小節番号が異なり、刻み位置が同一のノートが列挙されないこと
		c.enumNotes(new BmsPoint(100, 0), n -> {
			assertEquals(100, n.getMeasure());
			assertEquals(0, n.getTick(), 0.0);
			var chNum = n.getChannel();
			if (chNum == CN_A36S) {
				assertEquals(599, n.getValue());
			} else if (chNum == CN_A36) {
				assertEquals(2, n.getIndex());
				assertEquals(911, n.getValue());
			} else {
				fail("Detected illegal channel!");
			}
		});
		c.enumNotes(new BmsPoint(150, 96), n -> {
			assertEquals(150, n.getMeasure());
			assertEquals(96, n.getTick(), 0.0);
			var chNum = n.getChannel();
			if (chNum == CN_A36S) {
				assertEquals(899, n.getValue());
			} else if (chNum == CN_A36) {
				assertEquals(4, n.getIndex());
				assertEquals(944, n.getValue());
			} else {
				fail("Detected illegal channel!");
			}
		});
	}

	// enumNotes(BmsAt, Tester)
	// NullPointerException atがnull
	@Test
	public void testEnumNotesBmsAtBmsNoteTester_002() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.putNote(CN_A36S, 10, 0.0, 100);
		c.endEdit();
		assertThrows(NullPointerException.class, () -> c.enumNotes((BmsAt)null, n -> {}));
	}

	// enumNotes(int, double, Tester)
	// 正常
	@Test
	public void testEnumNotesIntDoubleBmsNoteTester_001() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.putNote(CN_A36S, 100, 0.0, 599);
		c.putNote(CN_A36S, 100, 96.0, 699);
		c.putNote(CN_A36S, 150, 0.0, 799);
		c.putNote(CN_A36S, 150, 96.0, 899);
		c.putNote(CN_A36, 2, 100, 0.0, 911);
		c.putNote(CN_A36, 4, 100, 96.0, 922);
		c.putNote(CN_A36, 2, 150, 0.0, 933);
		c.putNote(CN_A36, 4, 150, 96.0, 944);
		c.endEdit();

		// 小節番号、刻み位置が一致するノートが列挙されること
		// 小節番号が同一で刻み位置が異なるノートが列挙されないこと
		// 小節番号が異なり、刻み位置が同一のノートが列挙されないこと
		c.enumNotes(100, 0.0, n -> {
			assertEquals(100, n.getMeasure());
			assertEquals(0, n.getTick(), 0.0);
			var chNum = n.getChannel();
			if (chNum == CN_A36S) {
				assertEquals(599, n.getValue());
			} else if (chNum == CN_A36) {
				assertEquals(2, n.getIndex());
				assertEquals(911, n.getValue());
			} else {
				fail("Detected illegal channel!");
			}
		});
		c.enumNotes(150, 96.0, n -> {
			assertEquals(150, n.getMeasure());
			assertEquals(96, n.getTick(), 0.0);
			var chNum = n.getChannel();
			if (chNum == CN_A36S) {
				assertEquals(899, n.getValue());
			} else if (chNum == CN_A36) {
				assertEquals(4, n.getIndex());
				assertEquals(944, n.getValue());
			} else {
				fail("Detected illegal channel!");
			}
		});
	}

	// enumNotes(int, double, Tester)
	// IllegalArgumentException measureがマイナス値
	@Test
	public void testEnumNotesIntDoubleBmsNoteTester_002() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.putNote(CN_A36S, 10, 0.0, 100);
		c.endEdit();
		assertThrows(IllegalArgumentException.class, () -> c.enumNotes(-1, 0.0, n -> {}));
	}

	// enumNotes(int, double, Tester)
	// IllegalArgumentException measureが小節数以上
	@Test
	public void testEnumNotesIntDoubleBmsNoteTester_003() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.putNote(CN_A36S, 10, 0.0, 100);
		c.endEdit();
		assertThrows(IllegalArgumentException.class, () -> c.enumNotes(11, 0.0, n -> {}));
	}

	// enumNotes(int, double, Tester)
	// IllegalArgumentException tickがマイナス値
	@Test
	public void testEnumNotesIntDoubleBmsNoteTester_004() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.putNote(CN_A36S, 10, 0.0, 100);
		c.endEdit();
		assertThrows(IllegalArgumentException.class, () -> c.enumNotes(0, -1.0, n -> {}));
	}

	// enumNotes(int, double, Tester)
	// IllegalArgumentException tickが当該小節の刻み数以上
	@Test
	public void testEnumNotesIntDoubleBmsNoteTester_005() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.putNote(CN_A36S, 10, 0.0, 100);
		c.endEdit();
		assertThrows(IllegalArgumentException.class, () -> c.enumNotes(0, BmsSpec.TICK_COUNT_DEFAULT, n -> {}));
	}

	// enumNotes(int, double, Tester)
	// NullPointerException enumNoteがnull
	@Test
	public void testEnumNotesIntDoubleBmsNoteTester_006() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.putNote(CN_A36S, 10, 0.0, 100);
		c.endEdit();
		assertThrows(NullPointerException.class, () -> c.enumNotes(10, 0.0, null));
	}

	// enumNotes(int, double, int, double, Tester)
	// 正常系のみ
	@Test
	public void testEnumNotesIntDoubleIntDoubleTester_001() {
		var c = new BmsContent(spec());
		getEnumNotesTestData(c);

		// 小節・刻み位置の範囲指定が正しく機能するかどうか
		c.enumNotes(2, 48.0, 8, 96.0, n -> {
			assertTrue(n.getMeasure() >= 2);
			assertTrue(n.getMeasure() <= 8);
			if (n.getMeasure() == 2) {
				assertTrue(n.getTick() >= 48);
			} else if (n.getMeasure() == 8) {
				assertTrue(n.getTick() < 96);
			}
		});
	}

	// enumNotes(BmsAt, BmsAt, Tester)
	// 正常
	@Test
	public void testEnumNotesBmsAtBmsAtTester_001() {
		var c = new BmsContent(spec());
		getEnumNotesTestData(c);

		// 小節・刻み位置の範囲指定が正しく機能するかどうか
		c.enumNotes(new BmsPoint(2, 48), new BmsPoint(8, 96), n -> {
			assertTrue(n.getMeasure() >= 2);
			assertTrue(n.getMeasure() <= 8);
			if (n.getMeasure() == 2) {
				assertTrue(n.getTick() >= 48);
			} else if (n.getMeasure() == 8) {
				assertTrue(n.getTick() < 96);
			}
		});
	}

	// enumNotes(BmsAt, BmsAt, Tester)
	// NullPointerException atBeginがnull
	@Test
	public void testEnumNotesBmsAtBmsAtTester_002() {
		var c = new BmsContent(spec());
		assertThrows(NullPointerException.class, () -> c.enumNotes(null, new BmsPoint(1, 0), n -> {}));
	}

	// enumNotes(BmsAt, BmsAt, Tester)
	// NullPointerException atEndがnull
	@Test
	public void testEnumNotesBmsAtBmsAtTester_003() {
		var c = new BmsContent(spec());
		assertThrows(NullPointerException.class, () -> c.enumNotes(new BmsPoint(0, 0), null, n -> {}));
	}

	// enumNotes(int, int, BmsAt, BmsAt, Tester)
	// 正常
	@Test
	public void testEnumNotesIntIntBmsAtBmsAtTester_001() {
		var c = new BmsContent(spec());
		getEnumNotesTestData(c);

		// チャンネルの範囲指定が正しく機能するかどうか
		c.enumNotes(11, 31, new BmsPoint(0, 0), new BmsPoint(c.getMeasureCount(), 0), n -> {
			assertTrue(n.getChannel() >= 11);
			assertTrue(n.getChannel() < 31);
		});

		// 小節・刻み位置の範囲指定が正しく機能するかどうか
		c.enumNotes(BmsSpec.CHANNEL_MIN, BmsSpec.CHANNEL_MAX + 1, new BmsPoint(2, 48), new BmsPoint(8, 96), n -> {
			assertTrue(n.getMeasure() >= 2);
			assertTrue(n.getMeasure() <= 8);
			if (n.getMeasure() == 2) {
				assertTrue(n.getTick() >= 48);
			} else if (n.getMeasure() == 8) {
				assertTrue(n.getTick() < 96);
			}
		});
	}

	// enumNotes(int, int, BmsAt, BmsAt, Tester)
	// NullPointerException atBeginがnull
	@Test
	public void testEnumNotesIntIntBmsAtBmsAtTester_002() {
		var c = new BmsContent(spec());
		assertThrows(NullPointerException.class, () -> c.enumNotes(0, 1296, null, new BmsPoint(1, 0), n -> {}));
	}

	// enumNotes(int, int, BmsAt, BmsAt, Tester)
	// NullPointerException atEndがnull
	@Test
	public void testEnumNotesIntIntBmsAtBmsAtTester_003() {
		var c = new BmsContent(spec());
		assertThrows(NullPointerException.class, () -> c.enumNotes(0, 1296, new BmsPoint(0, 0), null, n -> {}));
	}

	// enumNotes(int, int, int, double, int, double, Tester)
	// 正常
	@Test
	public void testEnumNotesIntIntIntDoubleIntDoubleTester_001() {
		var c = new BmsContent(spec());
		getEnumNotesTestData(c);

		// チャンネルの範囲指定が正しく機能するかどうか
		c.enumNotes(11, 31, 0, 0, c.getMeasureCount(), 0, n -> {
			assertTrue(n.getChannel() >= 11);
			assertTrue(n.getChannel() < 31);
		});

		// 小節・刻み位置の範囲指定が正しく機能するかどうか
		c.enumNotes(BmsSpec.CHANNEL_MIN, BmsSpec.CHANNEL_MAX + 1, 2, 48, 8, 96, n -> {
			assertTrue(n.getMeasure() >= 2);
			assertTrue(n.getMeasure() <= 8);
			if (n.getMeasure() == 2) {
				assertTrue(n.getTick() >= 48);
			} else if (n.getMeasure() == 8) {
				assertTrue(n.getTick() < 96);
			}
		});
	}

	// enumNotes(int, int, int, double, int, double, Tester)
	// IllegalArgumentException channelBeginに小節データ・ノートを登録できないチャンネル番号を指定した
	@Test
	public void testEnumNotesIntIntIntDoubleIntDoubleTester_002() {
		var c = new BmsContent(spec());
		var ex = IllegalArgumentException.class;
		c.beginEdit();
		c.putNote(1, 0, 3, 0, 1000);
		c.endEdit();
		assertThrows(ex, () -> c.enumNotes(BmsSpec.CHANNEL_MIN - 1, BmsSpec.CHANNEL_MAX + 1, 0, 0, 4, 0, n -> {}));
		assertThrows(ex, () -> c.enumNotes(BmsSpec.CHANNEL_MAX + 2, BmsSpec.CHANNEL_MAX + 1, 0, 0, 4, 0, n -> {}));
	}

	// enumNotes(int, int, int, double, int, double, Tester)
	// IllegalArgumentException channelEndに{@link BmsSpec#CHANNEL_MAX}を超える値を指定した
	@Test
	public void testEnumNotesIntIntIntDoubleIntDoubleTester_003() {
		var c = new BmsContent(spec());
		var ex = IllegalArgumentException.class;
		c.beginEdit();
		c.putNote(1, 0, 3, 0, 1000);
		c.endEdit();
		assertThrows(ex, () -> c.enumNotes(BmsSpec.CHANNEL_MIN + 0, BmsSpec.CHANNEL_MIN - 1, 0, 0, 4, 0, n -> {}));
		assertThrows(ex, () -> c.enumNotes(BmsSpec.CHANNEL_MIN + 0, BmsSpec.CHANNEL_MAX + 2, 0, 0, 4, 0, n -> {}));
	}

	// enumNotes(int, int, int, double, int, double, Tester)
	// IllegalArgumentException measureBeginにノート・小節データの存在し得ない小節番号を指定した
	@Test
	public void testEnumNotesIntIntIntDoubleIntDoubleTester_004() {
		var c = new BmsContent(spec());
		var ex = IllegalArgumentException.class;
		c.beginEdit();
		c.putNote(1, 0, 3, 0, 1000);
		c.endEdit();
		assertThrows(ex, () -> c.enumNotes(BmsSpec.CHANNEL_MIN, BmsSpec.CHANNEL_MAX + 1, -1, 0, 4, 0, n -> {}));
		assertThrows(ex, () -> c.enumNotes(BmsSpec.CHANNEL_MIN, BmsSpec.CHANNEL_MAX + 1, 4, 1, 4, 0, n -> {}));
		assertThrows(ex, () -> c.enumNotes(BmsSpec.CHANNEL_MIN, BmsSpec.CHANNEL_MAX + 1, 5, 0, 4, 0, n -> {}));
	}

	// enumNotes(int, int, int, double, int, double, Tester)
	// IllegalArgumentException tickBeginにマイナス値または当該小節の刻み数以上の値を指定した
	@Test
	public void testEnumNotesIntIntIntDoubleIntDoubleTester_005() {
		var c = new BmsContent(spec());
		var ex = IllegalArgumentException.class;
		c.beginEdit();
		c.putNote(1, 0, 3, 0, 1000);
		c.endEdit();
		assertThrows(ex, () -> c.enumNotes(BmsSpec.CHANNEL_MIN, BmsSpec.CHANNEL_MAX + 1, 0, -1, 4, 0, n -> {}));
		assertThrows(ex, () -> c.enumNotes(BmsSpec.CHANNEL_MIN, BmsSpec.CHANNEL_MAX + 1, 3, 192, 4, 0, n -> {}));
		assertThrows(ex, () -> c.enumNotes(BmsSpec.CHANNEL_MIN, BmsSpec.CHANNEL_MAX + 1, 4, 1, 4, 0, n -> {}));
	}

	// enumNotes(int, int, int, double, int, double, Tester)
	// IllegalArgumentException measureEndに{@link BmsSpec#MEASURE_MIN}未満または{@link BmsSpec#MEASURE_MAX}超過の値を指定した
	@Test
	public void testEnumNotesIntIntIntDoubleIntDoubleTester_006() {
		var c = new BmsContent(spec());
		var ex = IllegalArgumentException.class;
		c.beginEdit();
		c.putNote(1, 0, 3, 0, 1000);
		c.endEdit();
		assertThrows(ex, () -> c.enumNotes(BmsSpec.CHANNEL_MIN, BmsSpec.CHANNEL_MAX + 1, 0, 0, -1, 0, n -> {}));
		assertThrows(ex, () -> c.enumNotes(BmsSpec.CHANNEL_MIN, BmsSpec.CHANNEL_MAX + 1, 0, 0, 4, 1, n -> {}));
		assertThrows(ex, () -> c.enumNotes(BmsSpec.CHANNEL_MIN, BmsSpec.CHANNEL_MAX + 1, 0, 0, 5, 0, n -> {}));
	}

	// enumNotes(int, int, int, double, int, double, Tester)
	// IllegalArgumentException tickEndにマイナス値、当該小節の刻み数以上、または小節番号==小節数の時に0以外の値を指定した
	@Test
	public void testEnumNotesIntIntIntDoubleIntDoubleTester_007() {
		var c = new BmsContent(spec());
		var ex = IllegalArgumentException.class;
		c.beginEdit();
		c.putNote(1, 0, 3, 0, 1000);
		c.endEdit();
		assertThrows(ex, () -> c.enumNotes(BmsSpec.CHANNEL_MIN, BmsSpec.CHANNEL_MAX + 1, 0, -1, 4, 0, n -> {}));
		assertThrows(ex, () -> c.enumNotes(BmsSpec.CHANNEL_MIN, BmsSpec.CHANNEL_MAX + 1, 3, 192, 4, 0, n -> {}));
		assertThrows(ex, () -> c.enumNotes(BmsSpec.CHANNEL_MIN, BmsSpec.CHANNEL_MAX + 1, 4, 1, 4, 0, n -> {}));
	}

	// enumNotes(int, int, int, double, int, double, Tester)
	// NullPointerException enumNoteがnull
	@Test
	public void testEnumNotesIntIntIntDoubleIntDoubleTester_008() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.putNote(1, 0, 3, 0, 1000);
		c.endEdit();
		assertThrows(NullPointerException.class, () -> c.enumNotes(BmsSpec.CHANNEL_MIN, BmsSpec.CHANNEL_MAX + 1, 0, 0, 4, 0, null));
	}

	// getNote(int, BmsAt)
	// 正常
	@Test
	public void testGetNoteIntBmsAt_001() {
		var c = new BmsContent(spec());
		getGetNoteTestData(c, true).forEach(t -> {
			var n = c.getNote(t.channel, new BmsPoint(t.measure, t.tick));
			assertNotNull(n);
			assertEquals(t.channel, n.getChannel());
			assertEquals(t.index, n.getIndex());
			assertEquals(t.measure, n.getMeasure());
			assertEquals(t.tick, n.getTick(), 0.0);
			assertEquals(t.value, n.getValue());
		});
	}

	// getNote(int, BmsAt)
	// NullPointerException atがnull
	@Test
	public void testGetNoteIntBmsAt_002() {
		var c = new BmsContent(spec());
		assertThrows(NullPointerException.class, () -> c.getNote(1, null));
	}

	// getNote(int, int, BmsAt)
	// 正常
	@Test
	public void testGetNoteIntIntBmsAt_001() {
		var c = new BmsContent(spec());
		getGetNoteTestData(c, false).forEach(t -> {
			var n = c.getNote(t.channel, t.index, new BmsPoint(t.measure, t.tick));
			assertNotNull(n);
			assertEquals(t.channel, n.getChannel());
			assertEquals(t.index, n.getIndex());
			assertEquals(t.measure, n.getMeasure());
			assertEquals(t.tick, n.getTick(), 0.0);
			assertEquals(t.value, n.getValue());
		});
	}

	// getNote(int, int, BmsAt)
	// NullPointerException atがnull
	@Test
	public void testGetNoteIntIntBmsAt_002() {
		var c = new BmsContent(spec());
		assertThrows(NullPointerException.class, () -> c.getNote(1, 0, null));
	}

	// getNote(int, int, double)
	// 正常系のみ
	@Test
	public void testGetNoteIntIntDouble_001() {
		var c = new BmsContent(spec());
		getGetNoteTestData(c, true).forEach(t -> {
			var n = c.getNote(t.channel, t.measure, t.tick);
			assertNotNull(n);
			assertEquals(t.channel, n.getChannel());
			assertEquals(t.index, n.getIndex());
			assertEquals(t.measure, n.getMeasure());
			assertEquals(t.tick, n.getTick(), 0.0);
			assertEquals(t.value, n.getValue());
		});
	}

	// getNote(int, int, int, double)
	// 正常
	@Test
	public void testGetNoteIntIntIntDouble_001() {
		var c = new BmsContent(spec());

		// 正常系
		getGetNoteTestData(c, false).forEach(t -> {
			var n = c.getNote(t.channel, t.index, t.measure, t.tick);
			assertNotNull(n);
			assertEquals(t.channel, n.getChannel());
			assertEquals(t.index, n.getIndex());
			assertEquals(t.measure, n.getMeasure());
			assertEquals(t.tick, n.getTick(), 0.0);
			assertEquals(t.value, n.getValue());
		});

		// 存在しないノートを指定した場合はnull
		var n = c.getNote(1, 1, 0, 0);
		assertNull(n);
		n = c.getNote(1, 0, 0, 5);
		assertNull(n);
	}

	// getNote(int, int, int, double)
	// IllegalArgumentException BMS仕様にないチャンネル番号を指定した
	@Test
	public void testGetNoteIntIntIntDouble_002() {
		var c = new BmsContent(spec());
		assertThrows(IllegalArgumentException.class, () -> c.getNote(999, 0, 0, 0));
	}

	// getNote(int, int, int, double)
	// IndexOutOfBoundsException チャンネルインデックスにマイナス値を指定した
	@Test
	public void testGetNoteIntIntIntDouble_003() {
		var c = new BmsContent(spec());
		assertThrows(IndexOutOfBoundsException.class, () -> c.getNote(1, -1, 0, 0));
	}

	// getNote(int, int, int, double)
	// IndexOutOfBoundsException 重複不許可のチャンネルで0以外のチャンネルインデックスを指定した
	@Test
	public void testGetNoteIntIntIntDouble_004() {
		var c = new BmsContent(spec());
		assertThrows(IndexOutOfBoundsException.class, () -> c.getNote(CN_A36S, 1, 0, 0));
	}

	// getNote(int, int, int, double)
	// IllegalArgumentException 小節番号が{@link BmsSpec#MEASURE_MIN}未満または{@link BmsSpec#MEASURE_MAX}超過
	@Test
	public void testGetNoteIntIntIntDouble_005() {
		var c = new BmsContent(spec());
		var ex = IllegalArgumentException.class;
		c.beginEdit();
		c.putNote(1, 0, 3, 0, 1000);
		c.endEdit();
		assertThrows(ex, () -> c.getNote(1, 0, BmsSpec.MEASURE_MIN - 1, 0));
		assertThrows(ex, () -> c.getNote(1, 0, BmsSpec.MEASURE_MAX + 1, 0));
		assertNull(c.getNote(1, 0, BmsSpec.MEASURE_MIN, 0));
		assertNull(c.getNote(1, 0, BmsSpec.MEASURE_MAX, 0));
	}

	// getNote(int, int, int, double)
	// IllegalArgumentException 小節の刻み位置にマイナス値または当該小節の刻み数以上の値を指定した
	@Test
	public void testGetNoteIntIntIntDouble_006() {
		var c = new BmsContent(spec());
		var ex = IllegalArgumentException.class;
		c.beginEdit();
		c.putNote(1, 0, 3, 0, 1000);
		c.endEdit();
		assertThrows(ex, () -> c.getNote(1, 0, 0, -1));
		assertThrows(ex, () -> c.getNote(1, 0, 0, 192));
	}

	// getNote(int, int, int, double)
	// IllegalArgumentException 指定チャンネルのデータ型が配列型ではない
	@Test
	public void testGetNoteIntIntIntDouble_007() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.setMeasureValue(CN_INT, 0, 100L);
		c.endEdit();
		assertThrows(IllegalArgumentException.class, () -> c.getNote(CN_INT, 0, 0, 0));
	}

	// getPreviousNote(int, BmsAt, boolean)
	// 正常
	@Test
	public void testGetPreviousNoteIntBmsAt_001() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.putNote(1, 0, 0, 96, 100);
		c.putNote(1, 0, 1, 64, 300);
		c.putNote(1, 0, 1, 128, 400);
		c.putNote(1, 0, 3, 0, 500);
		c.putNote(1, 1, 3, 0, 1000);
		c.putNote(1, 1, 3, 96, 1100);
		c.putNote(1, 2, 4, 32, 1200);
		c.putNote(1, 2, 6, 64, 1250);
		c.putNote(11, 0, 0, 0, 10);
		c.putNote(11, 0, 3, 0, 20);
		c.putNote(11, 0, 5, 0, 30);
		c.endEdit();

		// 通常ケース
		var n = c.getPreviousNote(1, new BmsPoint(1, 32), true);
		assertNotNull(n);
		assertEquals(1, n.getChannel());
		assertEquals(0, n.getIndex());
		assertEquals(0, n.getMeasure());
		assertEquals(96, n.getTick(), 0.0);
		assertEquals(100, n.getValue());

		// ノートのない小節からの検索開始
		n = c.getPreviousNote(1, new BmsPoint(2, 96), true);
		assertNotNull(n);
		assertEquals(1, n.getChannel());
		assertEquals(0, n.getIndex());
		assertEquals(1, n.getMeasure());
		assertEquals(128, n.getTick(), 0.0);
		assertEquals(400, n.getValue());

		// 検索開始位置にノートがある場合、そのノートを返す
		n = c.getPreviousNote(1, new BmsPoint(1, 127), true);
		assertNotNull(n);
		assertEquals(1, n.getChannel());
		assertEquals(0, n.getIndex());
		assertEquals(1, n.getMeasure());
		assertEquals(64, n.getTick(), 0.0);
		assertEquals(300, n.getValue());

		// 検索開始位置にノートがあり、その位置を検索対象外にするケース
		n = c.getPreviousNote(1, new BmsPoint(1, 128), false);
		assertNotNull(n);
		assertEquals(1, n.getChannel());
		assertEquals(0, n.getIndex());
		assertEquals(1, n.getMeasure());
		assertEquals(64, n.getTick(), 0.0);
		assertEquals(300, n.getValue());

		// 該当ノートがない場合はnull
		n = c.getPreviousNote(1, new BmsPoint(0, 95), true);  // ノートのあるチャンネル
		assertNull(n);
		n = c.getPreviousNote(21, new BmsPoint(2, 0), true);  // ノートのないチャンネル
		assertNull(n);
	}

	// getPreviousNote(int, BmsAt, boolean)
	// NullPointerException atがnull
	@Test
	public void testGetPreviousNoteIntBmsAt_002() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.putNote(1, 0, 4, 0, 1000);
		c.endEdit();
		assertThrows(NullPointerException.class, () -> c.getPreviousNote(1, null, true));
	}

	// getPreviousNote(int, int, BmsAt, boolean)
	// 正常
	@Test
	public void testGetPreviousNoteIntIntBmsAtBoolean_001() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.putNote(1, 0, 0, 96, 100);
		c.putNote(1, 0, 1, 64, 300);
		c.putNote(1, 0, 1, 128, 400);
		c.putNote(1, 0, 3, 0, 500);
		c.putNote(1, 1, 3, 0, 1000);
		c.putNote(1, 1, 3, 96, 1100);
		c.putNote(1, 2, 4, 32, 1200);
		c.putNote(1, 2, 6, 64, 1250);
		c.putNote(11, 0, 0, 0, 10);
		c.putNote(11, 0, 3, 0, 20);
		c.putNote(11, 0, 5, 0, 30);
		c.endEdit();

		// 通常ケース
		var n = c.getPreviousNote(1, 1, new BmsPoint(3, 48), true);
		assertNotNull(n);
		assertEquals(1, n.getChannel());
		assertEquals(1, n.getIndex());
		assertEquals(3, n.getMeasure());
		assertEquals(0, n.getTick(), 0.0);
		assertEquals(1000, n.getValue());

		// ノートの無い小節からの検索開始
		n = c.getPreviousNote(1, 2, new BmsPoint(5, 0), true);
		assertNotNull(n);
		assertEquals(1, n.getChannel());
		assertEquals(2, n.getIndex());
		assertEquals(4, n.getMeasure());
		assertEquals(32, n.getTick(), 0.0);
		assertEquals(1200, n.getValue());

		// 検索開始位置にノートがある場合、そのノートを返す
		n = c.getPreviousNote(1, 2, new BmsPoint(6, 64), true);
		assertNotNull(n);
		assertEquals(1, n.getChannel());
		assertEquals(2, n.getIndex());
		assertEquals(6, n.getMeasure());
		assertEquals(64, n.getTick(), 0.0);
		assertEquals(1250, n.getValue());

		// 検索開始位置にノートがあり、その位置を検索対象外にするケース
		n = c.getPreviousNote(1, 2, new BmsPoint(6, 64), false);
		assertNotNull(n);
		assertEquals(1, n.getChannel());
		assertEquals(2, n.getIndex());
		assertEquals(4, n.getMeasure());
		assertEquals(32, n.getTick(), 0.0);
		assertEquals(1200, n.getValue());

		// 該当ノートがない場合はnull
		n = c.getPreviousNote(1, 1, new BmsPoint(2, 0), true);  // ノートのあるチャンネル
		assertNull(n);
		n = c.getPreviousNote(CN_A36, 2, new BmsPoint(2, 0), true);  // ノートのないチャンネル
		assertNull(n);
	}

	// getPreviousNote(int, int, BmsAt, boolean)
	// NullPointerException atがnull
	@Test
	public void testGetPreviousNoteIntIntBmsAtBoolean_002() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.putNote(1, 0, 4, 0, 1000);
		c.endEdit();
		assertThrows(NullPointerException.class, () -> c.getPreviousNote(1, 0, null, true));
	}

	// getPreviousNote(int, int, double, boolean)
	// 正常系のみ
	@Test
	public void testGetPreviousNoteIntIntDoubleBoolean_001() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.putNote(1, 0, 0, 96, 100);
		c.putNote(1, 0, 1, 64, 300);
		c.putNote(1, 0, 1, 128, 400);
		c.putNote(1, 0, 3, 0, 500);
		c.putNote(1, 1, 3, 0, 1000);
		c.putNote(1, 1, 3, 96, 1100);
		c.putNote(1, 2, 4, 32, 1200);
		c.putNote(1, 2, 6, 64, 1250);
		c.putNote(11, 0, 0, 0, 10);
		c.putNote(11, 0, 3, 0, 20);
		c.putNote(11, 0, 5, 0, 30);
		c.endEdit();

		// 通常ケース
		var n = c.getPreviousNote(1, 1, 32.0, true);
		assertNotNull(n);
		assertEquals(1, n.getChannel());
		assertEquals(0, n.getIndex());
		assertEquals(0, n.getMeasure());
		assertEquals(96, n.getTick(), 0.0);
		assertEquals(100, n.getValue());

		// ノートのない小節からの検索開始
		n = c.getPreviousNote(1, 2, 96.0, true);
		assertNotNull(n);
		assertEquals(1, n.getChannel());
		assertEquals(0, n.getIndex());
		assertEquals(1, n.getMeasure());
		assertEquals(128, n.getTick(), 0.0);
		assertEquals(400, n.getValue());

		// 検索開始位置にノートがある場合、そのノートを返す
		n = c.getPreviousNote(1, 1, 127.0, true);
		assertNotNull(n);
		assertEquals(1, n.getChannel());
		assertEquals(0, n.getIndex());
		assertEquals(1, n.getMeasure());
		assertEquals(64, n.getTick(), 0.0);
		assertEquals(300, n.getValue());

		// 検索開始位置にノートがあり、その位置を検索対象外にするケース
		n = c.getPreviousNote(1, 1, 128.0, false);
		assertNotNull(n);
		assertEquals(1, n.getChannel());
		assertEquals(0, n.getIndex());
		assertEquals(1, n.getMeasure());
		assertEquals(64, n.getTick(), 0.0);
		assertEquals(300, n.getValue());

		// 該当ノートがない場合はnull
		n = c.getPreviousNote(1, 0, 95.0, true);  // ノートのあるチャンネル
		assertNull(n);
		n = c.getPreviousNote(21, 2, 0.0, true);  // ノートのないチャンネル
		assertNull(n);
	}

	// getPreviousNote(int, int, int, double, boolean)
	// 正常
	@Test
	public void testGetPreviousNoteIntIntIntDoubleBoolean_001() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.putNote(1, 0, 0, 96, 100);
		c.putNote(1, 0, 1, 64, 300);
		c.putNote(1, 0, 1, 128, 400);
		c.putNote(1, 0, 3, 0, 500);
		c.putNote(1, 1, 3, 0, 1000);
		c.putNote(1, 1, 3, 96, 1100);
		c.putNote(1, 2, 4, 32, 1200);
		c.putNote(1, 2, 6, 64, 1250);
		c.putNote(11, 0, 0, 0, 10);
		c.putNote(11, 0, 3, 0, 20);
		c.putNote(11, 0, 5, 0, 30);
		c.endEdit();

		// 通常ケース
		var n = c.getPreviousNote(1, 1, 3, 48, true);
		assertNotNull(n);
		assertEquals(1, n.getChannel());
		assertEquals(1, n.getIndex());
		assertEquals(3, n.getMeasure());
		assertEquals(0, n.getTick(), 0.0);
		assertEquals(1000, n.getValue());

		// ノートのない小節からの検索開始
		n = c.getPreviousNote(1, 2, 5, 0, true);
		assertNotNull(n);
		assertEquals(1, n.getChannel());
		assertEquals(2, n.getIndex());
		assertEquals(4, n.getMeasure());
		assertEquals(32, n.getTick(), 0.0);
		assertEquals(1200, n.getValue());

		// 検索開始位置にノートがある場合、そのノートを返す
		n = c.getPreviousNote(1, 2, 6, 64, true);
		assertNotNull(n);
		assertEquals(1, n.getChannel());
		assertEquals(2, n.getIndex());
		assertEquals(6, n.getMeasure());
		assertEquals(64, n.getTick(), 0.0);
		assertEquals(1250, n.getValue());

		// 検索開始位置にノートがあり、その位置を検索対象外にするケース
		n = c.getPreviousNote(1, 2, 6, 64, false);
		assertNotNull(n);
		assertEquals(1, n.getChannel());
		assertEquals(2, n.getIndex());
		assertEquals(4, n.getMeasure());
		assertEquals(32, n.getTick(), 0.0);
		assertEquals(1200, n.getValue());

		// 該当ノートがない場合はnull
		n = c.getPreviousNote(1, 1, 2, 0, true);  // ノートのあるチャンネル
		assertNull(n);
		n = c.getPreviousNote(CN_A36, 2, 2, 0, true);  // ノートのないチャンネル
		assertNull(n);
	}

	// getPreviousNote(int, int, int, double, boolean)
	// IllegalArgumentException BMS仕様にないチャンネル番号を指定した
	@Test
	public void testGetPreviousNoteIntIntIntDoubleBoolean_002() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.putNote(1, 0, 4, 0, 1000);
		c.endEdit();
		assertThrows(IllegalArgumentException.class, () -> c.getPreviousNote(999, 0, 0, 0, true));
	}

	// getPreviousNote(int, int, int, double, boolean)
	// IndexOutOfBoundsException チャンネルインデックスにマイナス値を指定した
	@Test
	public void testGetPreviousNoteIntIntIntDoubleBoolean_003() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.putNote(1, 0, 4, 0, 1000);
		c.endEdit();
		assertThrows(IndexOutOfBoundsException.class, () -> c.getPreviousNote(1, -1, 0, 0, true));
	}

	// getPreviousNote(int, int, int, double, boolean)
	// IndexOutOfBoundsException 重複不許可のチャンネルで0以外のチャンネルインデックスを指定した
	@Test
	public void testGetPreviousNoteIntIntIntDoubleBoolean_004() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.putNote(1, 0, 4, 0, 1000);
		c.endEdit();
		assertThrows(IndexOutOfBoundsException.class, () -> c.getPreviousNote(11, 1, 0, 0, true));
	}

	// getPreviousNote(int, int, int, double, boolean)
	// IllegalArgumentException 小節番号が{@link BmsSpec#MEASURE_MIN}未満または小節数以上
	@Test
	public void testGetPreviousNoteIntIntIntDoubleBoolean_005() {
		var c = new BmsContent(spec());
		var ex = IllegalArgumentException.class;
		c.beginEdit();
		c.putNote(1, 0, 4, 0, 1000);
		c.endEdit();
		assertThrows(ex, () -> c.getPreviousNote(1, 0, BmsSpec.MEASURE_MIN - 1, 0, true));
		assertThrows(ex, () -> c.getPreviousNote(1, 0, 5, 1, true));
	}

	// getPreviousNote(int, int, int, double, boolean)
	// IllegalArgumentException 小節の刻み位置にマイナス値または当該小節の刻み数以上を指定した
	@Test
	public void testGetPreviousNoteIntIntIntDoubleBoolean_006() {
		var c = new BmsContent(spec());
		var ex = IllegalArgumentException.class;
		c.beginEdit();
		c.putNote(1, 0, 4, 0, 1000);
		c.endEdit();
		assertThrows(ex, () -> c.getPreviousNote(1, 0, 1, -1, true));
		assertThrows(ex, () -> c.getPreviousNote(1, 0, 1, 192, true));
	}

	// getPreviousNote(int, int, int, double, boolean)
	// IllegalArgumentException 指定チャンネルのデータ型が配列型ではない
	@Test
	public void testGetPreviousNoteIntIntIntDoubleBoolean_007() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.putNote(1, 0, 4, 0, 1000);
		c.endEdit();
		assertThrows(IllegalArgumentException.class, () -> c.getPreviousNote(CN_INT, 0, 0, 0, true));
	}

	// getNextNote(int, BmsAt, boolean)
	// 正常
	@Test
	public void testGetNextNoteIntBmsAtBoolean_001() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.putNote(1, 0, 0, 96, 100);
		c.putNote(1, 0, 1, 64, 300);
		c.putNote(1, 0, 1, 128, 400);
		c.putNote(1, 0, 3, 0, 500);
		c.putNote(1, 1, 3, 0, 1000);
		c.putNote(1, 1, 3, 96, 1100);
		c.putNote(1, 2, 4, 32, 1200);
		c.putNote(1, 2, 6, 64, 1250);
		c.putNote(11, 0, 0, 0, 10);
		c.putNote(11, 0, 3, 0, 20);
		c.putNote(11, 0, 5, 0, 30);
		c.endEdit();

		// 通常ケース
		var n = c.getNextNote(1, new BmsPoint(1, 96), true);
		assertNotNull(n);
		assertEquals(1, n.getChannel());
		assertEquals(0, n.getIndex());
		assertEquals(1, n.getMeasure());
		assertEquals(128, n.getTick(), 0.0);
		assertEquals(400, n.getValue());

		// ノートのない小節からの検索開始
		n = c.getNextNote(1, new BmsPoint(2, 96), true);
		assertNotNull(n);
		assertEquals(1, n.getChannel());
		assertEquals(0, n.getIndex());
		assertEquals(3, n.getMeasure());
		assertEquals(0, n.getTick(), 0.0);
		assertEquals(500, n.getValue());

		// 検索開始位置にノートがある場合、そのノートを返す
		n = c.getNextNote(1, new BmsPoint(1, 64), true);
		assertNotNull(n);
		assertEquals(1, n.getChannel());
		assertEquals(0, n.getIndex());
		assertEquals(1, n.getMeasure());
		assertEquals(64, n.getTick(), 0.0);
		assertEquals(300, n.getValue());

		// 検索開始位置にノートがあり、その位置を検索対象外にするケース
		n = c.getNextNote(1, new BmsPoint(1, 64), false);
		assertNotNull(n);
		assertEquals(1, n.getChannel());
		assertEquals(0, n.getIndex());
		assertEquals(1, n.getMeasure());
		assertEquals(128, n.getTick(), 0.0);
		assertEquals(400, n.getValue());

		// 該当ノートがない場合はnull
		n = c.getNextNote(1, new BmsPoint(3, 1), true);  // ノートのあるチャンネルで該当ノートなし
		assertNull(n);
		n = c.getNextNote(21, new BmsPoint(2, 0), true);  // ノートのないチャンネルで該当ノートなし
		assertNull(n);
	}

	// getNextNote(int, BmsAt, boolean)
	// NullPointerException atがnull
	@Test
	public void testGetNextNoteIntBmsAtBoolean_002() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.putNote(1, 0, 4, 0, 1000);
		c.endEdit();
		assertThrows(NullPointerException.class, () -> c.getNextNote(1, null, true));
	}

	// getNextNote(int, int, BmsAt, boolean)
	// 正常
	@Test
	public void testGetNextNoteIntIntBmsAtBoolean_001() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.putNote(1, 0, 0, 96, 100);
		c.putNote(1, 0, 1, 64, 300);
		c.putNote(1, 0, 1, 128, 400);
		c.putNote(1, 0, 3, 0, 500);
		c.putNote(1, 1, 3, 0, 1000);
		c.putNote(1, 1, 3, 96, 1100);
		c.putNote(1, 2, 4, 32, 1200);
		c.putNote(1, 2, 6, 64, 1250);
		c.putNote(11, 0, 0, 0, 10);
		c.putNote(11, 0, 3, 0, 20);
		c.putNote(11, 0, 5, 0, 30);
		c.endEdit();

		// 通常ケース
		var n = c.getNextNote(1, 1, new BmsPoint(3, 48), true);
		assertNotNull(n);
		assertEquals(1, n.getChannel());
		assertEquals(1, n.getIndex());
		assertEquals(3, n.getMeasure());
		assertEquals(96, n.getTick(), 0.0);
		assertEquals(1100, n.getValue());

		// ノートのない小節からの検索開始
		n = c.getNextNote(1, 2, new BmsPoint(5, 0), true);
		assertNotNull(n);
		assertEquals(1, n.getChannel());
		assertEquals(2, n.getIndex());
		assertEquals(6, n.getMeasure());
		assertEquals(64, n.getTick(), 0.0);
		assertEquals(1250, n.getValue());

		// 検索開始位置にノートがある場合、そのノートを返す
		n = c.getNextNote(1, 2, new BmsPoint(6, 64), true);
		assertNotNull(n);
		assertEquals(1, n.getChannel());
		assertEquals(2, n.getIndex());
		assertEquals(6, n.getMeasure());
		assertEquals(64, n.getTick(), 0.0);
		assertEquals(1250, n.getValue());

		// 検索開始位置にノートがあり、その位置を検索対象外にするケース
		n = c.getNextNote(1, 2, new BmsPoint(4, 32), false);
		assertNotNull(n);
		assertEquals(1, n.getChannel());
		assertEquals(2, n.getIndex());
		assertEquals(6, n.getMeasure());
		assertEquals(64, n.getTick(), 0.0);
		assertEquals(1250, n.getValue());

		// 該当ノートがない場合はnull
		n = c.getNextNote(1, 1, new BmsPoint(3, 97), true);  // ノートのあるチャンネルで該当ノートなし
		assertNull(n);
		n = c.getNextNote(CN_A36, 2, new BmsPoint(2, 0), true);  // ノートのないチャンネルで該当ノートなし
		assertNull(n);
	}

	// getNextNote(int, int, BmsAt, boolean)
	// NullPointerException atがnull
	@Test
	public void testGetNextNoteIntIntBmsAtBoolean_002() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.putNote(1, 0, 4, 0, 1000);
		c.endEdit();
		assertThrows(NullPointerException.class, () -> c.getNextNote(1, 0, null, true));
	}

	// getNextNote(int, int, double, boolean)
	// 正常系のみ
	@Test
	public void testGetNextNoteIntIntDoubleBoolean_001() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.putNote(1, 0, 0, 96.0, 100);
		c.putNote(1, 0, 1, 64.0, 300);
		c.putNote(1, 0, 1, 128.0, 400);
		c.putNote(1, 0, 3, 0.0, 500);
		c.putNote(1, 1, 3, 0.0, 1000);
		c.putNote(1, 1, 3, 96.0, 1100);
		c.putNote(1, 2, 4, 32.0, 1200);
		c.putNote(1, 2, 6, 64.0, 1250);
		c.putNote(11, 0, 0, 0.0, 10);
		c.putNote(11, 0, 3, 0.0, 20);
		c.putNote(11, 0, 5, 0.0, 30);
		c.endEdit();

		// 通常ケース
		var n = c.getNextNote(1, 1, 96.0, true);
		assertNotNull(n);
		assertEquals(1, n.getChannel());
		assertEquals(0, n.getIndex());
		assertEquals(1, n.getMeasure());
		assertEquals(128, n.getTick(), 0.0);
		assertEquals(400, n.getValue());

		// ノートのない小節からの検索開始
		n = c.getNextNote(1, 2, 96.0, true);
		assertNotNull(n);
		assertEquals(1, n.getChannel());
		assertEquals(0, n.getIndex());
		assertEquals(3, n.getMeasure());
		assertEquals(0, n.getTick(), 0.0);
		assertEquals(500, n.getValue());

		// 検索開始位置にノートがある場合、そのノートを返す
		n = c.getNextNote(1, 1, 64.0, true);
		assertNotNull(n);
		assertEquals(1, n.getChannel());
		assertEquals(0, n.getIndex());
		assertEquals(1, n.getMeasure());
		assertEquals(64, n.getTick(), 0.0);
		assertEquals(300, n.getValue());

		// 検索開始位置にノートがあり、その位置を検索対象外にするケース
		n = c.getNextNote(1, 1, 64.0, false);
		assertNotNull(n);
		assertEquals(1, n.getChannel());
		assertEquals(0, n.getIndex());
		assertEquals(1, n.getMeasure());
		assertEquals(128, n.getTick(), 0.0);
		assertEquals(400, n.getValue());

		// 該当ノートがない場合はnull
		n = c.getNextNote(1, 3, 1.0, true);  // ノートのあるチャンネルで該当ノートなし
		assertNull(n);
		n = c.getNextNote(21, 2, 0.0, true);  // ノートのないチャンネルで該当ノートなし
		assertNull(n);
	}

	// getNextNote(int, int, int, double, boolean)
	// 正常
	@Test
	public void testGetNextNoteIntIntIntDoubleBoolean_001() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.putNote(1, 0, 0, 96, 100);
		c.putNote(1, 0, 1, 64, 300);
		c.putNote(1, 0, 1, 128, 400);
		c.putNote(1, 0, 3, 0, 500);
		c.putNote(1, 1, 3, 0, 1000);
		c.putNote(1, 1, 3, 96, 1100);
		c.putNote(1, 2, 4, 32, 1200);
		c.putNote(1, 2, 6, 64, 1250);
		c.putNote(11, 0, 0, 0, 10);
		c.putNote(11, 0, 3, 0, 20);
		c.putNote(11, 0, 5, 0, 30);
		c.endEdit();

		// 通常ケース
		var n = c.getNextNote(1, 1, 3, 48, true);
		assertNotNull(n);
		assertEquals(1, n.getChannel());
		assertEquals(1, n.getIndex());
		assertEquals(3, n.getMeasure());
		assertEquals(96, n.getTick(), 0.0);
		assertEquals(1100, n.getValue());

		// ノートのない小節からの検索開始
		n = c.getNextNote(1, 2, 5, 0, true);
		assertNotNull(n);
		assertEquals(1, n.getChannel());
		assertEquals(2, n.getIndex());
		assertEquals(6, n.getMeasure());
		assertEquals(64, n.getTick(), 0.0);
		assertEquals(1250, n.getValue());

		// 検索開始位置にノートがある場合、そのノートを返す
		n = c.getNextNote(1, 2, 6, 64, true);
		assertNotNull(n);
		assertEquals(1, n.getChannel());
		assertEquals(2, n.getIndex());
		assertEquals(6, n.getMeasure());
		assertEquals(64, n.getTick(), 0.0);
		assertEquals(1250, n.getValue());

		// 検索開始位置にノートがあり、その位置を検索対象外にするケース
		n = c.getNextNote(1, 2, 4, 32, false);
		assertNotNull(n);
		assertEquals(1, n.getChannel());
		assertEquals(2, n.getIndex());
		assertEquals(6, n.getMeasure());
		assertEquals(64, n.getTick(), 0.0);
		assertEquals(1250, n.getValue());

		// 該当ノートがない場合はnull
		n = c.getNextNote(1, 1, 3, 97, true);  // ノートのあるチャンネルで該当ノートなし
		assertNull(n);
		n = c.getNextNote(CN_A36, 2, 2, 0, true);  // ノートのないチャンネルで該当ノートなし
		assertNull(n);
	}

	// getNextNote(int, int, int, double, boolean)
	// IllegalArgumentException BMS仕様にないチャンネル番号を指定した
	@Test
	public void testGetNextNoteIntIntIntDoubleBoolean_002() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.putNote(1, 0, 4, 0, 1000);
		c.endEdit();
		assertThrows(IllegalArgumentException.class, () -> c.getNextNote(999, 0, 0, 0, true));
	}

	// getNextNote(int, int, int, double, boolean)
	// IndexOutOfBoundsException チャンネルインデックスにマイナス値を指定した
	@Test
	public void testGetNextNoteIntIntIntDoubleBoolean_003() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.putNote(1, 0, 4, 0, 1000);
		c.endEdit();
		assertThrows(IndexOutOfBoundsException.class, () -> c.getNextNote(1, -1, 0, 0, true));
	}

	// getNextNote(int, int, int, double, boolean)
	// IndexOutOfBoundsException 重複不許可のチャンネルで0以外のチャンネルインデックスを指定した
	@Test
	public void testGetNextNoteIntIntIntDoubleBoolean_004() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.putNote(1, 0, 4, 0, 1000);
		c.endEdit();
		assertThrows(IndexOutOfBoundsException.class, () -> c.getNextNote(11, 1, 0, 0, true));
	}

	// getNextNote(int, int, int, double, boolean)
	// IllegalArgumentException 小節番号が{@link BmsSpec#MEASURE_MIN}未満または小節数以上
	@Test
	public void testGetNextNoteIntIntIntDoubleBoolean_005() {
		var c = new BmsContent(spec());
		var ex = IllegalArgumentException.class;
		c.beginEdit();
		c.putNote(1, 0, 4, 0, 1000);
		c.endEdit();
		assertThrows(ex, () -> c.getNextNote(1, 0, BmsSpec.MEASURE_MIN - 1, 0, true));
		assertThrows(ex, () -> c.getNextNote(1, 0, 5, 1, true));
	}

	// getNextNote(int, int, int, double, boolean)
	// IllegalArgumentException 小節の刻み位置にマイナス値または当該小節の刻み数以上を指定した
	@Test
	public void testGetNextNoteIntIntIntDoubleBoolean_006() {
		var c = new BmsContent(spec());
		var ex = IllegalArgumentException.class;
		c.beginEdit();
		c.putNote(1, 0, 4, 0, 1000);
		c.endEdit();
		assertThrows(ex, () -> c.getNextNote(1, 0, 1, -1, true));
		assertThrows(ex, () -> c.getNextNote(1, 0, 1, 192, true));
	}

	// getNextNote(int, int, int, double, boolean)
	// IllegalArgumentException 指定チャンネルのデータ型が配列型ではない
	@Test
	public void testGetNextNoteIntIntIntDoubleBoolean_007() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.putNote(1, 0, 4, 0, 1000);
		c.endEdit();
		assertThrows(IllegalArgumentException.class, () -> c.getNextNote(CN_INT, 0, 0, 0, true));
	}

	// listNotes(Tester)
	// 正常系のみ
	@Test
	public void testListNotesTester_001() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.putNote(1, 0, 0, 0, 10);
		c.putNote(1, 0, 1, 0, 20);
		c.putNote(1, 0, 1, 96, 30);
		c.putNote(1, 0, 2, 0, 40);
		c.putNote(1, 0, 4, 0, 50);
		c.putNote(1, 0, 5, 0, 60);
		c.putNote(11, 0, 1, 0, 100);
		c.putNote(11, 0, 1, 48, 110);
		c.putNote(11, 0, 1, 96, 120);
		c.putNote(12, 0, 3, 0, 200);
		c.putNote(12, 0, 3, 96, 210);
		c.putNote(12, 0, 3, 144, 220);
		c.putNote(13, 0, 5, 0, 300);
		c.putNote(13, 0, 5, 32, 310);
		c.putNote(13, 0, 5, 64, 300);
		c.putNote(1, 1, 3, 0, 400);
		c.putNote(1, 2, 3, 64, 410);
		c.putNote(1, 3, 3, 128, 420);
		c.putNote(1, 4, 4, 0, 430);
		c.endEdit();

		List<BmsNote> l;

		// trueを返したノートのみがリストアップされる
		l = c.listNotes(n -> n.getChannel() == 11);
		l.forEach(n -> assertTrue(n.getChannel() == 11));
		assertEquals(3, l.size());

		// 重複可能ノートも全件リストアップできること
		l = c.listNotes(n -> n.getIndex() > 0);
		l.forEach(n -> assertTrue(n.getChannel() == 1));
		assertEquals(4, l.size());
	}

	// listNotes(BmsNote.Tester)
	// 空タイムラインでは空リストが返ること
	@Test
	public void testListNotesTester_EmptyTimeline() {
		var c = new BmsContent(spec());
		var l = c.listNotes(n -> true);
		assertNotNull(l);
		assertEquals(true, l.isEmpty());
	}

	// listNotes(BmsAt, Tester)
	// 正常
	@Test
	public void testListNotesBmsAtBmsNoteTester_001() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.putNote(CN_A36S, 100, 0.0, 599);
		c.putNote(CN_A36S, 100, 96.0, 699);
		c.putNote(CN_A36S, 150, 0.0, 799);
		c.putNote(CN_A36S, 150, 96.0, 899);
		c.putNote(CN_A36, 2, 100, 0.0, 911);
		c.putNote(CN_A36, 4, 100, 96.0, 922);
		c.putNote(CN_A36, 2, 150, 0.0, 933);
		c.putNote(CN_A36, 4, 150, 96.0, 944);
		c.endEdit();

		// 小節番号、刻み位置が一致するノートがリストされること
		// 小節番号が同一で刻み位置が異なるノートがリストされないこと
		// 小節番号が異なり、刻み位置が同一のノートがリストされないこと
		var l1 = c.listNotes(new BmsPoint(100, 0), n -> true);
		assertNotEquals(0, l1.size());
		l1.forEach(n -> {
			assertEquals(100, n.getMeasure());
			assertEquals(0, n.getTick(), 0.0);
			var chNum = n.getChannel();
			if (chNum == CN_A36S) {
				assertEquals(599, n.getValue());
			} else if (chNum == CN_A36) {
				assertEquals(2, n.getIndex());
				assertEquals(911, n.getValue());
			} else {
				fail("Detected illegal channel!");
			}
		});
		var l2 = c.listNotes(new BmsPoint(150, 96), n -> true);
		assertNotEquals(0, l2.size());
		l2.forEach(n -> {
			assertEquals(150, n.getMeasure());
			assertEquals(96, n.getTick(), 0.0);
			var chNum = n.getChannel();
			if (chNum == CN_A36S) {
				assertEquals(899, n.getValue());
			} else if (chNum == CN_A36) {
				assertEquals(4, n.getIndex());
				assertEquals(944, n.getValue());
			} else {
				fail("Detected illegal channel!");
			}
		});
	}

	// listNotes(BmsAt, Tester)
	// NullPointerException atがnull
	@Test
	public void testListNotesBmsAtBmsNoteTester_002() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.putNote(CN_A36S, 10, 0.0, 100);
		c.endEdit();
		assertThrows(NullPointerException.class, () -> c.listNotes((BmsAt)null, n -> true));
	}

	// listNotes(int, double, Tester)
	// 正常
	@Test
	public void testListNotesIntDoubleBmsNoteTester_001() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.putNote(CN_A36S, 100, 0.0, 599);
		c.putNote(CN_A36S, 100, 96.0, 699);
		c.putNote(CN_A36S, 150, 0.0, 799);
		c.putNote(CN_A36S, 150, 96.0, 899);
		c.putNote(CN_A36, 2, 100, 0.0, 911);
		c.putNote(CN_A36, 4, 100, 96.0, 922);
		c.putNote(CN_A36, 2, 150, 0.0, 933);
		c.putNote(CN_A36, 4, 150, 96.0, 944);
		c.endEdit();

		// 小節番号、刻み位置が一致するノートがリストされること
		// 小節番号が同一で刻み位置が異なるノートがリストされないこと
		// 小節番号が異なり、刻み位置が同一のノートがリストされないこと
		var l1 = c.listNotes(100, 0.0, n -> true);
		assertNotEquals(0, l1.size());
		l1.forEach(n -> {
			assertEquals(100, n.getMeasure());
			assertEquals(0, n.getTick(), 0.0);
			var chNum = n.getChannel();
			if (chNum == CN_A36S) {
				assertEquals(599, n.getValue());
			} else if (chNum == CN_A36) {
				assertEquals(2, n.getIndex());
				assertEquals(911, n.getValue());
			} else {
				fail("Detected illegal channel!");
			}
		});
		var l2 = c.listNotes(150, 96.0, n -> true);
		assertNotEquals(0, l2.size());
		l2.forEach(n -> {
			assertEquals(150, n.getMeasure());
			assertEquals(96, n.getTick(), 0.0);
			var chNum = n.getChannel();
			if (chNum == CN_A36S) {
				assertEquals(899, n.getValue());
			} else if (chNum == CN_A36) {
				assertEquals(4, n.getIndex());
				assertEquals(944, n.getValue());
			} else {
				fail("Detected illegal channel!");
			}
		});
	}

	// listNotes(int, double, Tester)
	// IllegalArgumentException measureがマイナス値
	@Test
	public void testListNotesIntDoubleBmsNoteTester_002() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.putNote(CN_A36S, 10, 0.0, 100);
		c.endEdit();
		assertThrows(IllegalArgumentException.class, () -> c.listNotes(-1, 0.0, n -> true));
	}

	// listNotes(int, double, Tester)
	// IllegalArgumentException measureが小節数以上
	@Test
	public void testListNotesIntDoubleBmsNoteTester_003() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.putNote(CN_A36S, 10, 0.0, 100);
		c.endEdit();
		assertThrows(IllegalArgumentException.class, () -> c.listNotes(11, 0.0, n -> true));
	}

	// listNotes(int, double, Tester)
	// IllegalArgumentException tickがマイナス値
	@Test
	public void testListNotesIntDoubleBmsNoteTester_004() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.putNote(CN_A36S, 10, 0.0, 100);
		c.endEdit();
		assertThrows(IllegalArgumentException.class, () -> c.listNotes(0, -1.0, n -> true));
	}

	// listNotes(int, double, Tester)
	// IllegalArgumentException tickが当該小節の刻み数以上
	@Test
	public void testListNotesIntDoubleBmsNoteTester_005() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.putNote(CN_A36S, 10, 0.0, 100);
		c.endEdit();
		assertThrows(IllegalArgumentException.class, () -> c.listNotes(0, BmsSpec.TICK_COUNT_DEFAULT, n -> true));
	}

	// listNotes(int, double, Tester)
	// NullPointerException isCollectがnull
	@Test
	public void testListNotesIntDoubleBmsNoteTester_006() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.putNote(CN_A36S, 10, 0.0, 100);
		c.endEdit();
		assertThrows(NullPointerException.class, () -> c.listNotes(10, 0.0, null));
	}

	// listNotes(BmsAt, BmsAt, Tester)
	// 正常
	@Test
	public void testListNotesBmsAtBmsAtTester_001() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.putNote(1, 0, 0, 0, 10);
		c.putNote(1, 0, 1, 0, 20);
		c.putNote(1, 0, 1, 96, 30);
		c.putNote(1, 0, 2, 0, 40);
		c.putNote(1, 0, 4, 0, 50);
		c.putNote(1, 0, 5, 0, 60);
		c.putNote(11, 0, 1, 0, 100);
		c.putNote(11, 0, 1, 48, 110);
		c.putNote(11, 0, 1, 96, 120);
		c.putNote(12, 0, 3, 0, 200);
		c.putNote(12, 0, 3, 96, 210);
		c.putNote(12, 0, 3, 144, 220);
		c.putNote(13, 0, 5, 0, 300);
		c.putNote(13, 0, 5, 32, 310);
		c.putNote(13, 0, 5, 64, 300);
		c.putNote(1, 1, 3, 0, 400);
		c.putNote(1, 2, 3, 64, 410);
		c.putNote(1, 3, 3, 128, 420);
		c.putNote(1, 4, 4, 0, 430);
		c.endEdit();

		List<BmsNote> l;

		// 小節の範囲指定が正常に動作すること
		l = c.listNotes(new BmsPoint(2, 0), new BmsPoint(5, 32), n -> true);
		l.forEach(n -> assertTrue((n.getMeasure() >= 0 && n.getMeasure() <= 4) || (n.getMeasure() == 5 && n.getTick() < 32)));
		assertEquals(11, l.size());

		// trueを返したノートのみがリストアップされる
		l = c.listNotes(new BmsPoint(0, 0), new BmsPoint(6, 0), n -> n.getChannel() == 11);
		l.forEach(n -> assertTrue(n.getChannel() == 11));
		assertEquals(3, l.size());

		// 重複可能ノートも全件リストアップできること
		l = c.listNotes(new BmsPoint(0, 0), new BmsPoint(6, 0), n -> n.getIndex() > 0);
		l.forEach(n -> assertTrue(n.getChannel() == 1));
		assertEquals(4, l.size());
	}

	// listNotes(BmsAt, BmsAt, Tester)
	// NullPointerException atBeginがnull
	@Test
	public void testListNotesBmsAtBmsAtTester_002() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.putNote(1, 0, 4, 0, 1000);
		c.endEdit();
		assertThrows(NullPointerException.class, () -> c.listNotes(null, new BmsPoint(0, 0), n -> true));
	}

	// listNotes(BmsAt, BmsAt, Tester)
	// NullPointerException atEndがnull
	@Test
	public void testListNotesBmsAtBmsAtTester_003() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.putNote(1, 0, 4, 0, 1000);
		c.endEdit();
		assertThrows(NullPointerException.class, () -> c.listNotes(new BmsPoint(0, 0), null, n -> true));
	}

	// listNotes(int, int, BmsAt, BmsAt, Tester)
	// 正常
	@Test
	public void testListNotesIntIntBmsAtBmsAtTester_001() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.putNote(1, 0, 0, 0, 10);
		c.putNote(1, 0, 1, 0, 20);
		c.putNote(1, 0, 1, 96, 30);
		c.putNote(1, 0, 2, 0, 40);
		c.putNote(1, 0, 4, 0, 50);
		c.putNote(1, 0, 5, 0, 60);
		c.putNote(11, 0, 1, 0, 100);
		c.putNote(11, 0, 1, 48, 110);
		c.putNote(11, 0, 1, 96, 120);
		c.putNote(12, 0, 3, 0, 200);
		c.putNote(12, 0, 3, 96, 210);
		c.putNote(12, 0, 3, 144, 220);
		c.putNote(13, 0, 5, 0, 300);
		c.putNote(13, 0, 5, 32, 310);
		c.putNote(13, 0, 5, 64, 300);
		c.putNote(1, 1, 3, 0, 400);
		c.putNote(1, 2, 3, 64, 410);
		c.putNote(1, 3, 3, 128, 420);
		c.putNote(1, 4, 4, 0, 430);
		c.endEdit();

		List<BmsNote> l;

		// チャンネルの範囲指定が正常に動作すること
		l = c.listNotes(11, 13, new BmsPoint(0, 0), new BmsPoint(6, 0), n -> true);
		l.forEach(n -> assertTrue(n.getChannel() >= 11 && n.getChannel() < 13));
		assertEquals(6, l.size());

		// 小節の範囲指定が正常に動作すること
		l = c.listNotes(1, 99, new BmsPoint(2, 0), new BmsPoint(5, 32), n -> true);
		l.forEach(n -> assertTrue((n.getMeasure() >= 0 && n.getMeasure() <= 4) || (n.getMeasure() == 5 && n.getTick() < 32)));
		assertEquals(11, l.size());

		// trueを返したノートのみがリストアップされる
		l = c.listNotes(1, 99, new BmsPoint(0, 0), new BmsPoint(6, 0), n -> n.getChannel() == 11);
		l.forEach(n -> assertTrue(n.getChannel() == 11));
		assertEquals(3, l.size());

		// 重複可能ノートも全件リストアップできること
		l = c.listNotes(1, 99, new BmsPoint(0, 0), new BmsPoint(6, 0), n -> n.getIndex() > 0);
		l.forEach(n -> assertTrue(n.getChannel() == 1));
		assertEquals(4, l.size());
	}

	// listNotes(int, int, BmsAt, BmsAt, Tester)
	// NullPointerException atBeginがnull
	@Test
	public void testListNotesIntIntBmsAtBmsAtTester_002() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.putNote(1, 0, 4, 0, 1000);
		c.endEdit();
		assertThrows(NullPointerException.class, () -> c.listNotes(1, 10, null, new BmsPoint(0, 0), n -> true));
	}

	// listNotes(int, int, BmsAt, BmsAt, Tester)
	// NullPointerException atEndがnull
	@Test
	public void testListNotesIntIntBmsAtBmsAtTester_003() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.putNote(1, 0, 4, 0, 1000);
		c.endEdit();
		assertThrows(NullPointerException.class, () -> c.listNotes(1, 10, new BmsPoint(0, 0), null, n -> true));
	}

	// listNotes(int, double, int, double, Tester)
	// 正常系のみ
	@Test
	public void testListNotesIntDoubleIntDoubleTester_001() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.putNote(1, 0, 0, 0, 10);
		c.putNote(1, 0, 1, 0, 20);
		c.putNote(1, 0, 1, 96, 30);
		c.putNote(1, 0, 2, 0, 40);
		c.putNote(1, 0, 4, 0, 50);
		c.putNote(1, 0, 5, 0, 60);
		c.putNote(11, 0, 1, 0, 100);
		c.putNote(11, 0, 1, 48, 110);
		c.putNote(11, 0, 1, 96, 120);
		c.putNote(12, 0, 3, 0, 200);
		c.putNote(12, 0, 3, 96, 210);
		c.putNote(12, 0, 3, 144, 220);
		c.putNote(13, 0, 5, 0, 300);
		c.putNote(13, 0, 5, 32, 310);
		c.putNote(13, 0, 5, 64, 300);
		c.putNote(1, 1, 3, 0, 400);
		c.putNote(1, 2, 3, 64, 410);
		c.putNote(1, 3, 3, 128, 420);
		c.putNote(1, 4, 4, 0, 430);
		c.endEdit();

		List<BmsNote> l;

		// 小節の範囲指定が正常に動作すること
		l = c.listNotes(2, 0.0, 5, 32.0, n -> true);
		l.forEach(n -> assertTrue((n.getMeasure() >= 0 && n.getMeasure() <= 4) || (n.getMeasure() == 5 && n.getTick() < 32)));
		assertEquals(11, l.size());

		// trueを返したノートのみがリストアップされる
		l = c.listNotes(0, 0.0, 6, 0.0, n -> n.getChannel() == 11);
		l.forEach(n -> assertTrue(n.getChannel() == 11));
		assertEquals(3, l.size());

		// 重複可能ノートも全件リストアップできること
		l = c.listNotes(0, 0.0, 6, 0.0, n -> n.getIndex() > 0);
		l.forEach(n -> assertTrue(n.getChannel() == 1));
		assertEquals(4, l.size());
	}

	// listNotes(int, int, int, double, int, double, Tester)
	// 正常
	@Test
	public void testListNotesIntIntIntDoubleIntDoubleTester_001() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.putNote(1, 0, 0, 0, 10);
		c.putNote(1, 0, 1, 0, 20);
		c.putNote(1, 0, 1, 96, 30);
		c.putNote(1, 0, 2, 0, 40);
		c.putNote(1, 0, 4, 0, 50);
		c.putNote(1, 0, 5, 0, 60);
		c.putNote(11, 0, 1, 0, 100);
		c.putNote(11, 0, 1, 48, 110);
		c.putNote(11, 0, 1, 96, 120);
		c.putNote(12, 0, 3, 0, 200);
		c.putNote(12, 0, 3, 96, 210);
		c.putNote(12, 0, 3, 144, 220);
		c.putNote(13, 0, 5, 0, 300);
		c.putNote(13, 0, 5, 32, 310);
		c.putNote(13, 0, 5, 64, 300);
		c.putNote(1, 1, 3, 0, 400);
		c.putNote(1, 2, 3, 64, 410);
		c.putNote(1, 3, 3, 128, 420);
		c.putNote(1, 4, 4, 0, 430);
		c.endEdit();

		List<BmsNote> l;

		// チャンネルの範囲指定が正常に動作すること
		l = c.listNotes(11, 13, 0, 0, 6, 0, n -> true);
		l.forEach(n -> assertTrue(n.getChannel() >= 11 && n.getChannel() < 13));
		assertEquals(6, l.size());

		// 小節の範囲指定が正常に動作すること
		l = c.listNotes(1, 99, 2, 0, 5, 32, n -> true);
		l.forEach(n -> assertTrue((n.getMeasure() >= 0 && n.getMeasure() <= 4) || (n.getMeasure() == 5 && n.getTick() < 32)));
		assertEquals(11, l.size());

		// trueを返したノートのみがリストアップされる
		l = c.listNotes(1, 99, 0, 0, 6, 0, n -> n.getChannel() == 11);
		l.forEach(n -> assertTrue(n.getChannel() == 11));
		assertEquals(3, l.size());

		// 重複可能ノートも全件リストアップできること
		l = c.listNotes(1, 99, 0, 0, 6, 0, n -> n.getIndex() > 0);
		l.forEach(n -> assertTrue(n.getChannel() == 1));
		assertEquals(4, l.size());
	}

	// listNotes(int, int, int, double, int, double, Tester)
	// IllegalArgumentException channelBeginに{@link BmsSpec#CHANNEL_MIN}未満または{@link BmsSpec#CHANNEL_MAX}を超える値を指定した
	@Test
	public void testListNotesIntIntIntDoubleIntDoubleTester_002() {
		var c = new BmsContent(spec());
		var ex = IllegalArgumentException.class;
		c.beginEdit();
		c.putNote(1, 0, 4, 0, 1000);
		c.endEdit();
		assertThrows(ex, () -> c.listNotes(BmsSpec.CHANNEL_MIN - 1, 10, 0, 0, 4, 191, n -> true));
		assertThrows(ex, () -> c.listNotes(BmsSpec.CHANNEL_MAX + 1, 10, 0, 0, 4, 191, n -> true));
	}

	// listNotes(int, int, int, double, int, double, Tester)
	// IllegalArgumentException channelEndに{@link BmsSpec#CHANNEL_MIN}未満または{@link BmsSpec#CHANNEL_MAX}+1を超える値を指定した
	@Test
	public void testListNotesIntIntIntDoubleIntDoubleTester_003() {
		var c = new BmsContent(spec());
		var ex = IllegalArgumentException.class;
		c.beginEdit();
		c.putNote(1, 0, 4, 0, 1000);
		c.endEdit();
		assertThrows(ex, () -> c.listNotes(1, BmsSpec.CHANNEL_MIN - 1, 0, 0, 4, 191, n -> true));
		assertThrows(ex, () -> c.listNotes(1, BmsSpec.CHANNEL_MAX + 2, 0, 0, 4, 191, n -> true));
	}

	// listNotes(int, int, int, double, int, double, Tester)
	// IllegalArgumentException measureBeginにノート・小節データの存在し得ない小節番号を指定した
	@Test
	public void testListNotesIntIntIntDoubleIntDoubleTester_004() {
		var c = new BmsContent(spec());
		var ex = IllegalArgumentException.class;
		c.beginEdit();
		c.putNote(1, 0, 4, 0, 1000);
		c.endEdit();
		assertThrows(ex, () -> c.listNotes(1, 10, -1, 0, 4, 191, n -> true));
		assertThrows(ex, () -> c.listNotes(1, 10, 5, 1, 4, 191, n -> true));
	}

	// listNotes(int, int, int, double, int, double, Tester)
	// IllegalArgumentException tickBeginにマイナス値または当該小節の刻み数以上の値を指定した
	@Test
	public void testListNotesIntIntIntDoubleIntDoubleTester_005() {
		var c = new BmsContent(spec());
		var ex = IllegalArgumentException.class;
		c.beginEdit();
		c.putNote(1, 0, 4, 0, 1000);
		c.endEdit();
		assertThrows(ex, () -> c.listNotes(1, 10, 0, -1, 4, 191, n -> true));
		assertThrows(ex, () -> c.listNotes(1, 10, 0, 192, 4, 191, n -> true));
	}

	// listNotes(int, int, int, double, int, double, Tester)
	// IllegalArgumentException measureEndに{@link BmsSpec#MEASURE_MIN}未満または{@link BmsSpec#MEASURE_MAX}超過の値を指定した
	@Test
	public void testListNotesIntIntIntDoubleIntDoubleTester_006() {
		var c = new BmsContent(spec());
		var ex = IllegalArgumentException.class;
		c.beginEdit();
		c.putNote(1, 0, 4, 0, 1000);
		c.endEdit();
		assertThrows(ex, () -> c.listNotes(1, 10, 0, 0, -1, 0, n -> true));
		assertThrows(ex, () -> c.listNotes(1, 10, 0, 0, 5, 1, n -> true));
	}

	// listNotes(int, int, int, double, int, double, Tester)
	// IllegalArgumentException tickEndにマイナス値、当該小節の刻み数以上、または小節番号==小節数の時に0以外の値を指定した
	@Test
	public void testListNotesIntIntIntDoubleIntDoubleTester_007() {
		var c = new BmsContent(spec());
		var ex = IllegalArgumentException.class;
		c.beginEdit();
		c.putNote(1, 0, 4, 0, 1000);
		c.endEdit();
		assertThrows(ex, () -> c.listNotes(1, 10, 0, 0, 0, -1, n -> true));
		assertThrows(ex, () -> c.listNotes(1, 10, 0, 0, 0, 192, n -> true));
	}

	// listNotes(int, int, int, double, int, double, Tester)
	// NullPointerException isCollectがnull
	@Test
	public void testListNotesIntIntIntDoubleIntDoubleTester_008() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.putNote(1, 0, 4, 0, 1000);
		c.endEdit();
		assertThrows(NullPointerException.class, () -> c.listNotes(1, 10, 0, 0, 5, 0, null));
	}

	// countNotes(Tester)
	// 正常系のみ
	@Test
	public void testCountNotesTester_001() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.putNote(1, 0, 0, 0, 10);
		c.putNote(1, 0, 1, 0, 20);
		c.putNote(1, 0, 1, 96, 30);
		c.putNote(1, 0, 2, 0, 40);
		c.putNote(1, 0, 4, 0, 50);
		c.putNote(1, 0, 5, 0, 60);
		c.putNote(11, 0, 1, 0, 100);
		c.putNote(11, 0, 1, 48, 110);
		c.putNote(11, 0, 1, 96, 120);
		c.putNote(12, 0, 3, 0, 200);
		c.putNote(12, 0, 3, 96, 210);
		c.putNote(12, 0, 3, 144, 220);
		c.putNote(13, 0, 5, 0, 300);
		c.putNote(13, 0, 5, 32, 310);
		c.putNote(13, 0, 5, 64, 300);
		c.putNote(1, 1, 3, 0, 400);
		c.putNote(1, 2, 3, 64, 410);
		c.putNote(1, 3, 3, 128, 420);
		c.putNote(1, 4, 4, 0, 430);
		c.endEdit();

		int cnt;

		// trueを返したノートのみがリストアップされる
		cnt = c.countNotes(n -> n.getChannel() == 11);
		assertEquals(3, cnt);

		// 重複可能ノートも全件リストアップできること
		cnt = c.countNotes(n -> n.getIndex() > 0);
		assertEquals(4, cnt);
	}

	// countNotes(BmsNote.Tester)
	// 空タイムラインでは0が返ること
	@Test
	public void testCountNotesTester_EmptyTimeline() {
		var c = new BmsContent(spec());
		assertEquals(0, c.countNotes(n -> true));
	}

	// countNotes(BmsAt, Tester)
	// 正常
	@Test
	public void testCountNotesBmsAtBmsNoteTester_001() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.putNote(CN_A36S, 100, 0.0, 599);
		c.putNote(CN_A36S, 100, 96.0, 699);
		c.putNote(CN_A36S, 150, 0.0, 799);
		c.putNote(CN_A36S, 150, 96.0, 899);
		c.putNote(CN_A36, 2, 100, 0.0, 911);
		c.putNote(CN_A36, 4, 100, 96.0, 922);
		c.putNote(CN_A36, 2, 150, 0.0, 933);
		c.putNote(CN_A36, 4, 150, 96.0, 944);
		c.endEdit();

		// 小節番号、刻み位置が一致するノートがカウントされること
		// 小節番号が同一で刻み位置が異なるノートがカウントされないこと
		// 小節番号が異なり、刻み位置が同一のノートがカウントされないこと
		var c1 = c.countNotes(new BmsPoint(100, 0), n -> true);
		assertEquals(2, c1);
		var c2 = c.countNotes(new BmsPoint(150, 96), n -> true);
		assertEquals(2, c2);
	}

	// countNotes(BmsAt, Tester)
	// NullPointerException atがnull
	@Test
	public void testCountNotesBmsAtBmsNoteTester_002() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.putNote(CN_A36S, 10, 0.0, 100);
		c.endEdit();
		assertThrows(NullPointerException.class, () -> c.countNotes((BmsAt)null, n -> true));
	}

	// countNotes(int, double, Tester)
	// 正常
	@Test
	public void testCountNotesIntDoubleBmsNoteTester_001() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.putNote(CN_A36S, 100, 0.0, 599);
		c.putNote(CN_A36S, 100, 96.0, 699);
		c.putNote(CN_A36S, 150, 0.0, 799);
		c.putNote(CN_A36S, 150, 96.0, 899);
		c.putNote(CN_A36, 2, 100, 0.0, 911);
		c.putNote(CN_A36, 4, 100, 96.0, 922);
		c.putNote(CN_A36, 2, 150, 0.0, 933);
		c.putNote(CN_A36, 4, 150, 96.0, 944);
		c.endEdit();

		// 小節番号、刻み位置が一致するノートがカウントされること
		// 小節番号が同一で刻み位置が異なるノートがカウントされないこと
		// 小節番号が異なり、刻み位置が同一のノートがカウントされないこと
		var c1 = c.countNotes(100, 0.0, n -> true);
		assertEquals(2, c1);
		var c2 = c.countNotes(150, 96.0, n -> true);
		assertEquals(2, c2);
	}

	// countNotes(int, double, Tester)
	// IllegalArgumentException measureがマイナス値
	@Test
	public void testCountNotesIntDoubleBmsNoteTester_002() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.putNote(CN_A36S, 10, 0.0, 100);
		c.endEdit();
		assertThrows(IllegalArgumentException.class, () -> c.countNotes(-1, 0.0, n -> true));
	}

	// countNotes(int, double, Tester)
	// IllegalArgumentException measureが小節数以上
	@Test
	public void testCountNotesIntDoubleBmsNoteTester_003() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.putNote(CN_A36S, 10, 0.0, 100);
		c.endEdit();
		assertThrows(IllegalArgumentException.class, () -> c.countNotes(11, 0.0, n -> true));
	}

	// countNotes(int, double, Tester)
	// IllegalArgumentException tickがマイナス値
	@Test
	public void testCountNotesIntDoubleBmsNoteTester_004() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.putNote(CN_A36S, 10, 0.0, 100);
		c.endEdit();
		assertThrows(IllegalArgumentException.class, () -> c.countNotes(0, -1.0, n -> true));
	}

	// countNotes(int, double, Tester)
	// IllegalArgumentException tickが当該小節の刻み数以上
	@Test
	public void testCountNotesIntDoubleBmsNoteTester_005() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.putNote(CN_A36S, 10, 0.0, 100);
		c.endEdit();
		assertThrows(IllegalArgumentException.class, () -> c.countNotes(0, BmsSpec.TICK_COUNT_DEFAULT, n -> true));
	}

	// countNotes(int, double, Tester)
	// NullPointerException isCountingがnull
	@Test
	public void testCountNotesIntDoubleBmsNoteTester_006() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.putNote(CN_A36S, 10, 0.0, 100);
		c.endEdit();
		assertThrows(NullPointerException.class, () -> c.countNotes(10, 0.0, null));
	}

	// countNotes(BmsAt, BmsAt, Tester)
	// 正常
	@Test
	public void testCountNotesBmsAtBmsAtTester_001() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.putNote(1, 0, 0, 0, 10);
		c.putNote(1, 0, 1, 0, 20);
		c.putNote(1, 0, 1, 96, 30);
		c.putNote(1, 0, 2, 0, 40);
		c.putNote(1, 0, 4, 0, 50);
		c.putNote(1, 0, 5, 0, 60);
		c.putNote(11, 0, 1, 0, 100);
		c.putNote(11, 0, 1, 48, 110);
		c.putNote(11, 0, 1, 96, 120);
		c.putNote(12, 0, 3, 0, 200);
		c.putNote(12, 0, 3, 96, 210);
		c.putNote(12, 0, 3, 144, 220);
		c.putNote(13, 0, 5, 0, 300);
		c.putNote(13, 0, 5, 32, 310);
		c.putNote(13, 0, 5, 64, 300);
		c.putNote(1, 1, 3, 0, 400);
		c.putNote(1, 2, 3, 64, 410);
		c.putNote(1, 3, 3, 128, 420);
		c.putNote(1, 4, 4, 0, 430);
		c.endEdit();

		int cnt;

		// 小節の範囲指定が正常に動作すること
		cnt = c.countNotes(new BmsPoint(2, 0), new BmsPoint(5, 32), n -> true);
		assertEquals(11, cnt);

		// trueを返したノートのみがリストアップされる
		cnt = c.countNotes(new BmsPoint(0, 0), new BmsPoint(6, 0), n -> n.getChannel() == 11);
		assertEquals(3, cnt);

		// 重複可能ノートも全件リストアップできること
		cnt = c.countNotes(new BmsPoint(0, 0), new BmsPoint(6, 0), n -> n.getIndex() > 0);
		assertEquals(4, cnt);
	}

	// countNotes(BmsAt, BmsAt, Tester)
	// NullPointerException atBeginがnull
	@Test
	public void testCountNotesBmsAtBmsAtTester_002() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.putNote(1, 0, 4, 0, 1000);
		c.endEdit();
		assertThrows(NullPointerException.class, () -> c.countNotes(null, new BmsPoint(0, 0), n -> true));
	}

	// countNotes(BmsAt, BmsAt, Tester)
	// NullPointerException atEndがnull
	@Test
	public void testCountNotesBmsAtBmsAtTester_003() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.putNote(1, 0, 4, 0, 1000);
		c.endEdit();
		assertThrows(NullPointerException.class, () -> c.countNotes(new BmsPoint(0, 0), null, n -> true));
	}

	// countNotes(int, int, BmsAt, BmsAt, Tester)
	// 正常
	@Test
	public void testCountNotesIntIntBmsAtBmsAtTester_001() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.putNote(1, 0, 0, 0, 10);
		c.putNote(1, 0, 1, 0, 20);
		c.putNote(1, 0, 1, 96, 30);
		c.putNote(1, 0, 2, 0, 40);
		c.putNote(1, 0, 4, 0, 50);
		c.putNote(1, 0, 5, 0, 60);
		c.putNote(11, 0, 1, 0, 100);
		c.putNote(11, 0, 1, 48, 110);
		c.putNote(11, 0, 1, 96, 120);
		c.putNote(12, 0, 3, 0, 200);
		c.putNote(12, 0, 3, 96, 210);
		c.putNote(12, 0, 3, 144, 220);
		c.putNote(13, 0, 5, 0, 300);
		c.putNote(13, 0, 5, 32, 310);
		c.putNote(13, 0, 5, 64, 300);
		c.putNote(1, 1, 3, 0, 400);
		c.putNote(1, 2, 3, 64, 410);
		c.putNote(1, 3, 3, 128, 420);
		c.putNote(1, 4, 4, 0, 430);
		c.endEdit();

		int cnt;

		// チャンネルの範囲指定が正常に動作すること
		cnt = c.countNotes(11, 13, new BmsPoint(0, 0), new BmsPoint(6, 0), n -> true);
		assertEquals(6, cnt);

		// 小節の範囲指定が正常に動作すること
		cnt = c.countNotes(1, 99, new BmsPoint(2, 0), new BmsPoint(5, 32), n -> true);
		assertEquals(11, cnt);

		// trueを返したノートのみがリストアップされる
		cnt = c.countNotes(1, 99, new BmsPoint(0, 0), new BmsPoint(6, 0), n -> n.getChannel() == 11);
		assertEquals(3, cnt);

		// 重複可能ノートも全件リストアップできること
		cnt = c.countNotes(1, 99, new BmsPoint(0, 0), new BmsPoint(6, 0), n -> n.getIndex() > 0);
		assertEquals(4, cnt);
	}

	// countNotes(int, int, BmsAt, BmsAt, Tester)
	// NullPointerException atBeginがnull
	@Test
	public void testCountNotesIntIntBmsAtBmsAtTester_002() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.putNote(1, 0, 4, 0, 1000);
		c.endEdit();
		assertThrows(NullPointerException.class, () -> c.countNotes(1, 10, null, new BmsPoint(0, 0), n -> true));
	}

	// countNotes(int, int, BmsAt, BmsAt, Tester)
	// NullPointerException atEndがnull
	@Test
	public void testCountNotesIntIntBmsAtBmsAtTester_003() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.putNote(1, 0, 4, 0, 1000);
		c.endEdit();
		assertThrows(NullPointerException.class, () -> c.countNotes(1, 10, new BmsPoint(0, 0), null, n -> true));
	}

	// countNotes(int, double, int, double, Tester)
	// 正常系のみ
	@Test
	public void testCountNotesIntDoubleIntDoubleTester_001() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.putNote(1, 0, 0, 0, 10);
		c.putNote(1, 0, 1, 0, 20);
		c.putNote(1, 0, 1, 96, 30);
		c.putNote(1, 0, 2, 0, 40);
		c.putNote(1, 0, 4, 0, 50);
		c.putNote(1, 0, 5, 0, 60);
		c.putNote(11, 0, 1, 0, 100);
		c.putNote(11, 0, 1, 48, 110);
		c.putNote(11, 0, 1, 96, 120);
		c.putNote(12, 0, 3, 0, 200);
		c.putNote(12, 0, 3, 96, 210);
		c.putNote(12, 0, 3, 144, 220);
		c.putNote(13, 0, 5, 0, 300);
		c.putNote(13, 0, 5, 32, 310);
		c.putNote(13, 0, 5, 64, 300);
		c.putNote(1, 1, 3, 0, 400);
		c.putNote(1, 2, 3, 64, 410);
		c.putNote(1, 3, 3, 128, 420);
		c.putNote(1, 4, 4, 0, 430);
		c.endEdit();

		int cnt;

		// 小節の範囲指定が正常に動作すること
		cnt = c.countNotes(2, 0, 5, 32.0, n -> true);
		assertEquals(11, cnt);

		// trueを返したノートのみがリストアップされる
		cnt = c.countNotes(0, 0, 6, 0.0, n -> n.getChannel() == 11);
		assertEquals(3, cnt);

		// 重複可能ノートも全件リストアップできること
		cnt = c.countNotes(0, 0, 6, 0.0, n -> n.getIndex() > 0);
		assertEquals(4, cnt);
	}

	// countNotes(int, int, int, double, int, double, Tester)
	// 正常
	@Test
	public void testCountNotesIntIntIntDoubleIntDoubleTester_001() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.putNote(1, 0, 0, 0, 10);
		c.putNote(1, 0, 1, 0, 20);
		c.putNote(1, 0, 1, 96, 30);
		c.putNote(1, 0, 2, 0, 40);
		c.putNote(1, 0, 4, 0, 50);
		c.putNote(1, 0, 5, 0, 60);
		c.putNote(11, 0, 1, 0, 100);
		c.putNote(11, 0, 1, 48, 110);
		c.putNote(11, 0, 1, 96, 120);
		c.putNote(12, 0, 3, 0, 200);
		c.putNote(12, 0, 3, 96, 210);
		c.putNote(12, 0, 3, 144, 220);
		c.putNote(13, 0, 5, 0, 300);
		c.putNote(13, 0, 5, 32, 310);
		c.putNote(13, 0, 5, 64, 300);
		c.putNote(1, 1, 3, 0, 400);
		c.putNote(1, 2, 3, 64, 410);
		c.putNote(1, 3, 3, 128, 420);
		c.putNote(1, 4, 4, 0, 430);
		c.endEdit();

		int cnt;

		// チャンネルの範囲指定が正常に動作すること
		cnt = c.countNotes(11, 13, 0, 0, 6, 0, n -> true);
		assertEquals(6, cnt);

		// 小節の範囲指定が正常に動作すること
		cnt = c.countNotes(1, 99, 2, 0, 5, 32, n -> true);
		assertEquals(11, cnt);

		// trueを返したノートのみがリストアップされる
		cnt = c.countNotes(1, 99, 0, 0, 6, 0, n -> n.getChannel() == 11);
		assertEquals(3, cnt);

		// 重複可能ノートも全件リストアップできること
		cnt = c.countNotes(1, 99, 0, 0, 6, 0, n -> n.getIndex() > 0);
		assertEquals(4, cnt);
	}

	// countNotes(int, int, int, double, int, double, Tester)
	// IllegalArgumentException channelBeginに{@link BmsSpec#CHANNEL_MIN}未満または{@link BmsSpec#CHANNEL_MAX}を超える値を指定した
	@Test
	public void testCountNotesIntIntIntDoubleIntDoubleTester_002() {
		var c = new BmsContent(spec());
		var ex = IllegalArgumentException.class;
		c.beginEdit();
		c.putNote(1, 0, 4, 0, 1000);
		c.endEdit();
		assertThrows(ex, () -> c.countNotes(BmsSpec.CHANNEL_MIN - 1, 10, 0, 0, 4, 191, n -> true));
		assertThrows(ex, () -> c.countNotes(BmsSpec.CHANNEL_MAX + 1, 10, 0, 0, 4, 191, n -> true));
	}

	// countNotes(int, int, int, double, int, double, Tester)
	// IllegalArgumentException channelEndに{@link BmsSpec#CHANNEL_MIN}未満または{@link BmsSpec#CHANNEL_MAX}+1を超える値を指定した
	@Test
	public void testCountNotesIntIntIntDoubleIntDoubleTester_003() {
		var c = new BmsContent(spec());
		var ex = IllegalArgumentException.class;
		c.beginEdit();
		c.putNote(1, 0, 4, 0, 1000);
		c.endEdit();
		assertThrows(ex, () -> c.countNotes(1, BmsSpec.CHANNEL_MIN - 1, 0, 0, 4, 191, n -> true));
		assertThrows(ex, () -> c.countNotes(1, BmsSpec.CHANNEL_MAX + 2, 0, 0, 4, 191, n -> true));
	}

	// countNotes(int, int, int, double, int, double, Tester)
	// IllegalArgumentException measureBeginにノート・小節データの存在し得ない小節番号を指定した
	@Test
	public void testCountNotesIntIntIntDoubleIntDoubleTester_004() {
		var c = new BmsContent(spec());
		var ex = IllegalArgumentException.class;
		c.beginEdit();
		c.putNote(1, 0, 4, 0, 1000);
		c.endEdit();
		assertThrows(ex, () -> c.countNotes(1, 10, -1, 0, 4, 191, n -> true));
		assertThrows(ex, () -> c.countNotes(1, 10, 5, 1, 4, 191, n -> true));
	}

	// countNotes(int, int, int, double, int, double, Tester)
	// IllegalArgumentException tickBeginにマイナス値または当該小節の刻み数以上の値を指定した
	@Test
	public void testCountNotesIntIntIntDoubleIntDoubleTester_005() {
		var c = new BmsContent(spec());
		var ex = IllegalArgumentException.class;
		c.beginEdit();
		c.putNote(1, 0, 4, 0, 1000);
		c.endEdit();
		assertThrows(ex, () -> c.countNotes(1, 10, 0, -1, 4, 191, n -> true));
		assertThrows(ex, () -> c.countNotes(1, 10, 0, 192, 4, 191, n -> true));
	}

	// countNotes(int, int, int, double, int, double, Tester)
	// IllegalArgumentException measureEndに{@link BmsSpec#MEASURE_MIN}未満または{@link BmsSpec#MEASURE_MAX}超過の値を指定した
	@Test
	public void testCountNotesIntIntIntDoubleIntDoubleTester_006() {
		var c = new BmsContent(spec());
		var ex = IllegalArgumentException.class;
		c.beginEdit();
		c.putNote(1, 0, 4, 0, 1000);
		c.endEdit();
		assertThrows(ex, () -> c.countNotes(1, 10, 0, 0, -1, 0, n -> true));
		assertThrows(ex, () -> c.countNotes(1, 10, 0, 0, 5, 1, n -> true));
	}

	// countNotes(int, int, int, double, int, double, Tester)
	// IllegalArgumentException tickEndにマイナス値、当該小節の刻み数以上、または小節番号==小節数の時に0以外の値を指定した
	@Test
	public void testCountNotesIntIntIntDoubleIntDoubleTester_007() {
		var c = new BmsContent(spec());
		var ex = IllegalArgumentException.class;
		c.beginEdit();
		c.putNote(1, 0, 4, 0, 1000);
		c.endEdit();
		assertThrows(ex, () -> c.countNotes(1, 10, 0, 0, 0, -1, n -> true));
		assertThrows(ex, () -> c.countNotes(1, 10, 0, 0, 0, 192, n -> true));
	}

	// countNotes(int, int, int, double, int, double, Tester)
	// NullPointerException isCountingがnull
	@Test
	public void testCountNotesIntIntIntDoubleIntDoubleTester_008() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.putNote(1, 0, 4, 0, 1000);
		c.endEdit();
	}

	// timeToPoint(double, BmsPoint)
	// 正常
	@Test
	public void testTimeToPointDoubleBmsPoint_001() {
		var c = getTestTimeContent();
		var p = new BmsPoint();

		// 先頭位置
		c.timeToPoint(0.0, p);
		assertEquals(0, p.getMeasure());
		assertEquals(0.0, p.getTick(), 0.0);

		// 1小節目の中間
		c.timeToPoint(1.0, p);
		assertEquals(0, p.getMeasure());
		assertEquals(96.0, p.getTick(), 0.0);

		// 無段階刻み位置が効いていること
		c.timeToPoint(1.3, p);
		assertEquals(0, p.getMeasure());
		assertEquals(124.8, p.getTick(), 0.00001);

		// 2小節目の小節長変更に対応していること
		c.timeToPoint(3.0, p);
		assertEquals(2, p.getMeasure());
		assertEquals(0, p.getTick(), 0.0);

		// 譜面停止と同じ位置を指している場合は譜面停止時間を含めないこと
		c.timeToPoint(5.0, p);
		assertEquals(2, p.getMeasure());
		assertEquals(96.0, p.getTick(), 0.0);

		// 譜面停止時間が考慮されること
		c.timeToPoint(7.0, p);
		assertEquals(3, p.getMeasure());
		assertEquals(0.0, p.getTick(), 0.0);

		// 小節途中のBPM変更と同じ位置の計算が正しいこと
		c.timeToPoint(8.5, p);
		assertEquals(4, p.getMeasure());
		assertEquals(96.0, p.getTick(), 0.00001);

		// 小節途中のBPM変更が正確に反映されていること
		c.timeToPoint(9.5, p);
		assertEquals(5, p.getMeasure());
		assertEquals(0.0, p.getTick(), 0.0);

		// 小節途中のBPM変更の値を正確に反映した譜面停止時間になっていること
		c.timeToPoint(12.0, p);
		assertEquals(6, p.getMeasure());
		assertEquals(0.0, p.getTick(), 0.0);

		// 短い小節長と小数値の出るBPMの時間計算が正確に行われること
		c.timeToPoint(13.2, p);
		assertEquals(7, p.getMeasure());
		assertEquals(0.0, p.getTick(), 0.0);

		// 総時間を越えた場合小節数、刻み位置0が返されること
		c.timeToPoint(15.200001, p);
		assertEquals(8, p.getMeasure());
		assertEquals(0.0, p.getTick(), 0.0);
	}

	// timeToPoint(double, BmsPoint)
	// IllegalStateException 動作モードが参照モードではない
	@Test
	public void testTimeToPointDoubleBmsPoint_002() {
		var c = new BmsContent(spec());
		c.beginEdit();
		assertThrows(IllegalStateException.class, () -> c.timeToPoint(0.0, new BmsPoint()));
	}

	// timeToPoint(double, BmsPoint)
	// IllegalArgumentException 時間にマイナス値を指定した
	@Test
	public void testTimeToPointDoubleBmsPoint_003() {
		var c = new BmsContent(spec());
		assertThrows(IllegalArgumentException.class, () -> c.timeToPoint(-1.0, new BmsPoint()));
	}

	// timeToPoint(double, BmsPoint)
	// NullPointerException outPointがnull
	@Test
	public void testTimeToPointDoubleBmsPoint_004() {
		var c = new BmsContent(spec());
		assertThrows(NullPointerException.class, () -> c.timeToPoint(0.0, null));
	}

	// timeToPoint(double, BmsPoint)
	// 正常：計算上の刻み数が1未満の小節があっても正確な値を返すこと
	@Test
	public void testTimeToPointDoubleBmsPoint_Normal_HasMinimalLength() {
		var c = getTestTimeContentForMinimalLengthMeasure();
		var p = new BmsPoint();

		// 2小節目先頭
		c.timeToPoint(2.0, p);
		assertEquals(1, p.getMeasure());
		assertEquals(0.0, p.getTick(), 0.00001);

		// 2小節目中間
		c.timeToPoint(2.5, p);
		assertEquals(1, p.getMeasure());
		assertEquals(0.5, p.getTick(), 0.00001);

		// 3小節目先頭
		c.timeToPoint(3.0, p);
		assertEquals(2, p.getMeasure());
		assertEquals(0.0, p.getTick(), 0.00001);

		// 3小節目中間
		c.timeToPoint(4.0, p);
		assertEquals(2, p.getMeasure());
		assertEquals(0.5, p.getTick(), 0.00001);

		// 4小節目先頭
		c.timeToPoint(5.0, p);
		assertEquals(3, p.getMeasure());
		assertEquals(0.0, p.getTick(), 0.00001);

		// 4小節目中間
		c.timeToPoint(6.5, p);
		assertEquals(3, p.getMeasure());
		assertEquals(0.5, p.getTick(), 0.00001);

		// 5小節目先頭
		c.timeToPoint(8.0, p);
		assertEquals(4, p.getMeasure());
		assertEquals(0.0, p.getTick(), 0.00001);
	}

	// pointToTime(BmsAt)
	// 正常
	@Test
	public void testPointToTimeBmsAt_001() {
		var c = getTestTimeContent();
		double t;

		// 先頭位置
		t = c.pointToTime(new BmsPoint(0, 0));
		assertEquals(0.0, t, 0.00001);

		// 1小節目の中間
		t = c.pointToTime(new BmsPoint(0, 96));
		assertEquals(1.0, t, 0.00001);

		// 1未満の時間が正確に計算されること
		t = c.pointToTime(new BmsPoint(0, 124));
		assertEquals(1.291667, t, 0.00001);

		// 2小節目の小節長変更に対応していること
		t = c.pointToTime(new BmsPoint(2, 0));
		assertEquals(3.0, t, 0.00001);

		// 譜面停止と同じ位置を指している場合は譜面停止時間を含めないこと
		t = c.pointToTime(new BmsPoint(2, 96));
		assertEquals(4.0, t, 0.00001);

		// 譜面停止時間が考慮されること
		t = c.pointToTime(new BmsPoint(3, 0));
		assertEquals(7.0, t, 0.00001);

		// 小節途中のBPM変更と同じ位置の計算が正しいこと
		t = c.pointToTime(new BmsPoint(4, 96));
		assertEquals(8.5, t, 0.00001);

		// 小節途中のBPM変更が正確に反映されていること
		t = c.pointToTime(new BmsPoint(5, 0));
		assertEquals(9.5, t, 0.00001);

		// 小節途中のBPM変更の値を正確に反映した譜面停止時間になっていること
		t = c.pointToTime(new BmsPoint(6, 0));
		assertEquals(12.0, t, 0.00001);

		// 短い小節長と小数値の出るBPMの時間計算が正確に行われること
		t = c.pointToTime(new BmsPoint(7, 0));
		assertEquals(13.2, t, 0.00001);

		// 小節番号==小節数、刻み位置0を指定すると総時間が返されること
		t = c.pointToTime(new BmsPoint(8, 0));
		assertEquals(15.2, t, 0.00001);

		// 小節番号=0、刻み位置0を指定すると0が返されること
		t = c.pointToTime(new BmsPoint(0, 0));
		assertEquals(0.0, t, 0.00001);

		// 小節データが空の状態で小節番号=0、刻み位置0を指定すると0が返されること
		c = new BmsContent(spec());
		t = c.pointToTime(new BmsPoint(0, 0));
		assertEquals(0.0, t, 0.00001);
	}

	// pointToTime(BmsAt)
	// NullPointerException atがnull
	@Test
	public void testPointToTimeBmsAt_002() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.putNote(1,  0,  4, 0, 1000);
		c.endEdit();
		assertThrows(NullPointerException.class, () -> c.pointToTime(null));
	}

	// pointToTime(BmsAt)
	// 正常：計算上の刻み数が1未満の小節があっても正確な値を返すこと
	@Test
	public void testPointToTimeBmsAt_Normal_HasMinimalLength() {
		var c = getTestTimeContentForMinimalLengthMeasure();
		assertEquals(2.0, c.pointToTime(new BmsPoint(1, 0)), 0.00001);
		assertEquals(3.0, c.pointToTime(new BmsPoint(2, 0)), 0.00001);
		assertEquals(5.0, c.pointToTime(new BmsPoint(3, 0)), 0.00001);
		assertEquals(8.0, c.pointToTime(new BmsPoint(4, 0)), 0.00001);
	}

	// pointToTime(BmsAt)
	// 正常：同じ小節内にあるBPM変更・譜面停止位置よりも手前までの時間計測でも正確な値を返すこと
	@Test
	public void testPointToTimeBmsAt_Normal_PreviousBpmAndStop() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.setInitialBpm(120.0);
		c.setIndexedMeta("#bpm", 1, 60.0);
		c.setIndexedMeta("#stop", 1, 96L);

		// 1小節目：tick=96 でBPMを 60 に変更, 小節時間=3s
		c.putNote(8, 0, 96.0, 1);

		// 2小節目：tick=96 で 96tick 譜面停止, 小節時間=9s
		c.putNote(9, 1, 96.0, 1);

		c.endEdit();

		assertEquals(0.5, c.pointToTime(new BmsPoint(0, 48)), 0.00001);
		assertEquals(1.0, c.pointToTime(new BmsPoint(0, 96)), 0.00001);
		assertEquals(4.0, c.pointToTime(new BmsPoint(1, 48)), 0.00001);
		assertEquals(5.0, c.pointToTime(new BmsPoint(1, 96)), 0.00001);
	}

	// pointToTime(int, double)
	// 正常
	@Test
	public void testPointToTimeIntInt_001() {
		var c = getTestTimeContent();
		double t;

		// 先頭位置
		t = c.pointToTime(0, 0);
		assertEquals(0.0, t, 0.00001);

		// 1小節目の中間
		t = c.pointToTime(0, 96);
		assertEquals(1.0, t, 0.00001);

		// 1未満の時間が正確に計算されること
		t = c.pointToTime(0, 124);
		assertEquals(1.291667, t, 0.00001);

		// 2小節目の小節長変更に対応していること
		t = c.pointToTime(2, 0);
		assertEquals(3.0, t, 0.00001);

		// 譜面停止と同じ位置を指している場合は譜面停止時間を含めないこと
		t = c.pointToTime(2, 96);
		assertEquals(4.0, t, 0.00001);

		// 譜面停止時間が考慮されること
		t = c.pointToTime(3, 0);
		assertEquals(7.0, t, 0.00001);

		// 小節途中のBPM変更と同じ位置の計算が正しいこと
		t = c.pointToTime(4, 96);
		assertEquals(8.5, t, 0.00001);

		// 小節途中のBPM変更が正確に反映されていること
		t = c.pointToTime(5, 0);
		assertEquals(9.5, t, 0.00001);

		// 小節途中のBPM変更の値を正確に反映した譜面停止時間になっていること
		t = c.pointToTime(6, 0);
		assertEquals(12, t, 0.00001);

		// 短い小節長と小数値の出るBPMの時間計算が正確に行われること
		t = c.pointToTime(7, 0);
		assertEquals(13.2, t, 0.00001);

		// 小節番号==小節数、刻み位置0を指定すると総時間が返されること
		t = c.pointToTime(8, 0);
		assertEquals(15.2, t, 0.00001);

		// 小節番号==0、刻み位置0を指定すると0が返されること
		t = c.pointToTime(0, 0);
		assertEquals(0.0, t, 0.00001);

		// 空の小節データで小節番号==0、刻み位置0を指定すると0が返されること
		c = new BmsContent(spec());
		t = c.pointToTime(0, 0);
		assertEquals(0.0, t, 0.00001);
	}

	// pointToTime(int, double)
	// IllegalStateException 動作モードが参照モードではない
	@Test
	public void testPointToTimeIntInt_002() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.putNote(1,  0,  4, 0, 1000);
		assertThrows(IllegalStateException.class, () -> c.pointToTime(0, 0));
	}

	// pointToTime(int, double)
	// IllegalArgumentException 小節番号が{@link BmsSpec#MEASURE_MIN}未満または{@link BmsSpec#MEASURE_MAX}超過
	// IllegalArgumentException 小節番号が{@link BmsSpec#MEASURE_MAX_COUNT}の時、小節の刻み位置に0以外を指定した
	@Test
	public void testPointToTimeIntInt_003() {
		var c = new BmsContent(spec());
		var ex = IllegalArgumentException.class;
		c.beginEdit();
		c.putNote(1, 0, 4, 0, 1000);
		c.endEdit();
		assertThrows(ex, () -> c.pointToTime(-1, 0));
		assertThrows(ex, () -> c.pointToTime(5, 1));

	}

	// pointToTime(int, double)
	// IllegalArgumentException 小節の刻み位置にマイナス値または当該小節の刻み数以上の値を指定した
	@Test
	public void testPointToTimeIntInt_004() {
		var c = new BmsContent(spec());
		var ex = IllegalArgumentException.class;
		c.beginEdit();
		c.putNote(1, 0, 4, 0, 1000);
		c.endEdit();
		assertThrows(ex, () -> c.pointToTime(0, -1));
		assertThrows(ex, () -> c.pointToTime(0, 192));
	}

	// pointToTime(int, double)
	// 正常：計算上の刻み数が1未満の小節があっても正確な値を返すこと
	@Test
	public void testPointToTimeIntInt_Normal_HasMinimalLength() {
		var c = getTestTimeContentForMinimalLengthMeasure();
		assertEquals(2.0, c.pointToTime(1, 0), 0.00001);
		assertEquals(3.0, c.pointToTime(2, 0), 0.00001);
		assertEquals(5.0, c.pointToTime(3, 0), 0.00001);
		assertEquals(8.0, c.pointToTime(4, 0), 0.00001);
	}

	// pointToTime(int, double)
	// 正常：同じ小節内にあるBPM変更・譜面停止位置よりも手前までの時間計測でも正確な値を返すこと
	@Test
	public void testPointToTimeIntInt_Normal_PreviousBpmAndStop() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.setInitialBpm(120.0);
		c.setIndexedMeta("#bpm", 1, 60.0);
		c.setIndexedMeta("#stop", 1, 96L);

		// 1小節目：tick=96 でBPMを 60 に変更, 小節時間=3s
		c.putNote(8, 0, 96.0, 1);

		// 2小節目：tick=96 で 96tick 譜面停止, 小節時間=9s
		c.putNote(9, 1, 96.0, 1);

		c.endEdit();

		assertEquals(0.5, c.pointToTime(0, 48), 0.00001);
		assertEquals(1.0, c.pointToTime(0, 96), 0.00001);
		assertEquals(4.0, c.pointToTime(1, 48), 0.00001);
		assertEquals(5.0, c.pointToTime(1, 96), 0.00001);
	}

	// getMeasureCount()
	// 正常
	@Test
	public void testGetMeasureCount_001() {
		// 通常ケース
		var c = new BmsContent(spec());
		c.beginEdit();
		c.putNote(1, 0, 0.0, 100);
		c.putNote(1, 1, 0.0, 200);
		c.putNote(1, 2, 0.0, 300);
		c.endEdit();
		assertEquals(3, c.getMeasureCount());

		// ノートが空の場合は0
		c = new BmsContent(spec());
		assertEquals(0, c.getMeasureCount());

		// 0件からいきなり最大小節にノートを追加した場合は、最大小節数
		c = new BmsContent(spec());
		c.beginEdit();
		c.putNote(1, 0, BmsSpec.MEASURE_MAX, 0.0, 100);
		c.endEdit();
		assertEquals(BmsSpec.MEASURE_MAX_COUNT, c.getMeasureCount());

		// 小節数は、最も小節番号の大きいもの+1
		c = new BmsContent(spec());
		c.beginEdit();
		c.putNote(1, 3, 0.0, 100);
		c.putNote(1, 5, 0.0, 200);
		c.putNote(11, 4, 0.0, 300);
		c.putNote(21, 8, 0.0, 400);
		c.endEdit();
		assertEquals(9, c.getMeasureCount());

		// 最も小節番号の大きいノートを消去すると、その次に小節番号の大きいもの+1
		c = new BmsContent(spec());
		c.beginEdit();
		c.putNote(1, 3, 0.0, 100);
		c.putNote(1, 7, 0.0, 200);
		c.putNote(11, 15, 0.0, 300);
		c.putNote(21, 8, 0.0, 400);
		c.removeNote(11, 15, 0.0);
		c.endEdit();
		assertEquals(9, c.getMeasureCount());
	}

	// getMeasureTickCount(int)
	// 正常
	@Test
	public void testGetMeasureTickCount_001() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.putNote(1, 0, 0.0, 100);
		c.putNote(1, 1, 0.0, 200);
		c.putNote(1, 2, 0.0, 300);
		c.putNote(1, 3, 0.0, 400);
		c.setMeasureValue(2, 1, 1.0);
		c.setMeasureValue(2, 2, 2.0);
		c.setMeasureValue(2, 3, 3.0);
		c.setMeasureValue(2, 3, (Double)null);
		c.endEdit();

		// 通常ケース
		assertEquals(BmsSpec.TICK_COUNT_DEFAULT, c.getMeasureTickCount(0), 0.0);
		// 小節長は設定したが、その値が1
		assertEquals(BmsSpec.TICK_COUNT_DEFAULT, c.getMeasureTickCount(1), 0.0);
		// 小節長を変更した
		assertEquals(BmsSpec.TICK_COUNT_DEFAULT * 2.0, c.getMeasureTickCount(2), 0.0);
		// 小節長を変更したが、その後削除した
		assertEquals(BmsSpec.TICK_COUNT_DEFAULT, c.getMeasureTickCount(3), 0.0);
	}

	// getMeasureTickCount(int)
	// IllegalArgumentException 小節番号が{@link BmsSpec#MEASURE_MIN}未満または{@link BmsSpec#MEASURE_MAX}超過
	@Test
	public void testGetMeasureTickCount_002() {
		var c = new BmsContent(spec());
		var ex = IllegalArgumentException.class;
		c.beginEdit();
		c.putNote(1, 0, 4, 0, 1000);
		c.endEdit();
		assertThrows(ex, () -> c.getMeasureTickCount(BmsSpec.MEASURE_MIN - 1));
		assertThrows(ex, () -> c.getMeasureTickCount(BmsSpec.MEASURE_MAX + 1));
		assertEquals(BmsSpec.TICK_COUNT_DEFAULT, c.getMeasureTickCount(BmsSpec.MEASURE_MIN), 0.0);
		assertEquals(BmsSpec.TICK_COUNT_DEFAULT, c.getMeasureTickCount(BmsSpec.MEASURE_MAX), 0.0);
	}

	// getMeasureTickCount(int)
	// 正常：小節長に1/192未満の値を設定しても小節の刻み数が1になること
	// 仕様変更により刻み数が1未満になることがあるため本テストは廃止する
	@Ignore
	@Test
	public void testGetMeasureTickCount_Normal_MinimalLength_Return1() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.putNote(1, 10, 0, 100);
		c.setMeasureValue(2, 0, 1.0 / BmsSpec.TICK_COUNT_DEFAULT - 0.00000001);
		c.setMeasureValue(2, 1, BmsSpec.LENGTH_MIN);
		c.endEdit();
		assertEquals(1, c.getMeasureTickCount(0), 0.0);
		assertEquals(1, c.getMeasureTickCount(1), 0.0);
	}

	// getMeasureTickCount(int)
	// 小節長が極端に短くなると刻み数が1未満の値になること
	@Test
	public void testGetMeasureTickCount_LessThan1() {
		var c = new BmsContent(spec());
		c.beginEdit();
		var length1 = (1.0 / (BmsSpec.TICK_COUNT_DEFAULT + 0.00000001));
		c.setMeasureValue(2, 0, length1);
		var length2 = BmsSpec.LENGTH_MIN;
		c.setMeasureValue(2, 1, length2);
		c.endEdit();
		assertEquals(BmsSpec.TICK_COUNT_DEFAULT * length1, c.getMeasureTickCount(0), 0.00000001);
		assertEquals(BmsSpec.TICK_COUNT_DEFAULT * length2, c.getMeasureTickCount(1), 0.00000001);
	}

	// getChannelDataCount(int, int)
	// 正常
	@Test
	public void testGetChannelDataCount_001() {
		var c = new BmsContent(spec());
		c.beginEdit();
		// 重複可能チャンネルで1件だけ
		c.putNote(1, 0, 0, 0, 100);
		// 重複可能チャンネルで隙間無く複数詰める
		c.putNote(1, 0, 1, 0, 110);
		c.putNote(1, 1, 1, 0, 111);
		c.putNote(1, 2, 1, 0, 112);
		// 重複可能チャンネルで歯抜けで複数詰める
		c.putNote(1, 0, 2, 0, 120);
		c.putNote(1, 3, 2, 0, 121);
		c.putNote(1, 7, 2, 0, 122);
		c.putNote(1, 8, 2, 0, 123);
		// 重複可能チャンネルで歯抜け複数の末尾を消去する
		c.putNote(1, 0, 3, 0, 130);
		c.putNote(1, 1, 3, 0, 131);
		c.putNote(1, 6, 3, 0, 132);
		c.putNote(1, 9, 3, 0, 133);
		c.removeNote(1, 9, 3, 0);
		// 重複不可チャンネル
		c.putNote(11, 0, 4, 0, 200);
		c.endEdit();

		// 重複可能チャンネルで1件だけ
		assertEquals(1, c.getChannelDataCount(1, 0));
		// 重複可能チャンネルで隙間無く複数詰める
		assertEquals(3, c.getChannelDataCount(1, 1));
		// 重複可能チャンネルで歯抜けで複数詰める
		assertEquals(9, c.getChannelDataCount(1, 2));
		// 重複可能チャンネルで歯抜け複数の末尾を消去する
		assertEquals(7, c.getChannelDataCount(1, 3));
		// 重複不可チャンネル
		assertEquals(1, c.getChannelDataCount(11, 4));
	}

	// getChannelDataCount(int, int)
	// IllegalArgumentException 小節データ・ノートを登録できないチャンネル番号を指定した
	@Test
	public void testGetChannelDataCount_002() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.putNote(1, 0, 4, 0, 1000);
		c.endEdit();
		assertThrows(IllegalArgumentException.class, () -> c.getChannelDataCount(999, 0));
	}

	// getChannelDataCount(int, int)
	// IllegalArgumentException 小節番号が{@link BmsSpec#MEASURE_MIN}未満または{@link BmsSpec#MEASURE_MAX}超過
	@Test
	public void testGetChannelDataCount_003() {
		var c = new BmsContent(spec());
		var ex = IllegalArgumentException.class;
		c.beginEdit();
		c.putNote(1, 0, 4, 0, 1000);
		c.endEdit();
		assertThrows(ex, () -> c.getChannelDataCount(1, BmsSpec.MEASURE_MIN - 1));
		assertThrows(ex, () -> c.getChannelDataCount(1, BmsSpec.MEASURE_MAX + 1));
		assertEquals(0, c.getChannelDataCount(1, BmsSpec.MEASURE_MIN));
		assertEquals(0, c.getChannelDataCount(1, BmsSpec.MEASURE_MAX));
	}

	private static List<NoteTestData> getNormalNoteTestData(boolean isZeroIndex) {
		var l = new ArrayList<NoteTestData>();
		if (isZeroIndex) {
			// index指定なしのメソッド用テストデータ
			// 1P可視先頭
			l.add(new NoteTestData(11, 0, BmsSpec.MEASURE_MIN, 0, 1));
			// 1P可視末尾
			l.add(new NoteTestData(11, 0, BmsSpec.MEASURE_MAX, 191, 1295));
			// ARRAY16への登録（最小値）
			l.add(new NoteTestData(Integer.valueOf("Z5", 36), 0, 0, 0, 0x01));
			// ARRAY16への登録（最大値）
			l.add(new NoteTestData(Integer.valueOf("Z5", 36), 0, 0, 96, 0xff));
			// 重複可能チャンネルのindex0への登録
			l.add(new NoteTestData(1, 0, BmsSpec.MEASURE_MIN, 0, 1));
			l.add(new NoteTestData(1, 0, BmsSpec.MEASURE_MAX, 0, 1295));
		} else {
			// WAV先頭index0
			l.add(new NoteTestData(1, 0, BmsSpec.MEASURE_MIN, 0, 1));
			// WAV末尾index0
			l.add(new NoteTestData(1, 0, BmsSpec.MEASURE_MAX, 191, 1295));
			// WAV先頭index3
			l.add(new NoteTestData(1, 3, BmsSpec.MEASURE_MIN, 0, 1));
			// WAV末尾index3
			l.add(new NoteTestData(1, 3, BmsSpec.MEASURE_MAX, 191, 1295));
			// ARRAY16への登録（最小値）index0
			l.add(new NoteTestData(Integer.valueOf("Z5", 36), 0, 0, 0, 0x01));
			// ARRAY16への登録（最大値）index0
			l.add(new NoteTestData(Integer.valueOf("Z5", 36), 0, 0, 96, 0xff));
			// ARRAY16への登録（最小値）index2
			l.add(new NoteTestData(Integer.valueOf("Z5", 36), 2, 0, 0, 0x01));
			// ARRAY16への登録（最大値）index2
			l.add(new NoteTestData(Integer.valueOf("Z5", 36), 2, 0, 96, 0xff));
		}
		return l;
	}

	private static List<GetResolvedNoteValueTestData> getNormalGetResolvedNoteValueTestData(BmsContent c, boolean is0Index) {
		c.beginEdit();
		// 参照されるメタ情報
		c.setIndexedMeta("#indexed_integer", 1, 11111L);
		c.setIndexedMeta("#indexed_integer", 2, 22222L);
		c.setIndexedMeta("#indexed_integer", 3, 33333L);
		c.setIndexedMeta("#indexed_float", 1, 11111.111);
		c.setIndexedMeta("#indexed_float", 2, 22222.222);
		c.setIndexedMeta("#indexed_float", 3, 33333.333);
		c.setIndexedMeta("#indexed_string", 1, "hogehoge");
		c.setIndexedMeta("#indexed_string", 2, "hagehage");
		c.setIndexedMeta("#indexed_string", 3, "higehige");
		c.setIndexedMeta("#indexed_base16", 1, 0x11L);
		c.setIndexedMeta("#indexed_base16", 2, 0x22L);
		c.setIndexedMeta("#indexed_base16", 3, 0x33L);
		c.setIndexedMeta("#indexed_base36", 1, Long.parseLong("XX", 36));
		c.setIndexedMeta("#indexed_base36", 2, Long.parseLong("YY", 36));
		c.setIndexedMeta("#indexed_base36", 3, Long.parseLong("ZZ", 36));
		c.setIndexedMeta("#indexed_array16", 1, "0011223344");
		c.setIndexedMeta("#indexed_array16", 2, "5566778899");
		c.setIndexedMeta("#indexed_array16", 3, "AABBCCDDEE");
		c.setIndexedMeta("#indexed_array36", 1, "GGHHIIJJKK");
		c.setIndexedMeta("#indexed_array36", 2, "LLMMNNOOPP");
		c.setIndexedMeta("#indexed_array36", 3, "QQRRSSTTUU");

		var s = c.getSpec();
		var dvIntr = (Long)s.getIndexedMeta(s.getChannel(CN_A36_INT).getRef()).getDefaultValue();
		var dvIntsr = (Long)s.getIndexedMeta(s.getChannel(CN_A36S_INT).getRef()).getDefaultValue();
		var dvNumr = (Double)s.getIndexedMeta(s.getChannel(CN_A36_NUM).getRef()).getDefaultValue();
		var dvNumsr = (Double)s.getIndexedMeta(s.getChannel(CN_A36S_NUM).getRef()).getDefaultValue();
		var dvStrr = (String)s.getIndexedMeta(s.getChannel(CN_A36_STR).getRef()).getDefaultValue();
		var dvStrsr = (String)s.getIndexedMeta(s.getChannel(CN_A36S_STR).getRef()).getDefaultValue();
		var dvB16r = (Long)s.getIndexedMeta(s.getChannel(CN_A36_B16).getRef()).getDefaultValue();
		var dvB16sr = (Long)s.getIndexedMeta(s.getChannel(CN_A36S_B16).getRef()).getDefaultValue();
		var dvB36r = (Long)s.getIndexedMeta(s.getChannel(CN_A36_B36).getRef()).getDefaultValue();
		var dvB36sr = (Long)s.getIndexedMeta(s.getChannel(CN_A36S_B36).getRef()).getDefaultValue();
		var dvA16r = (BmsArray)s.getIndexedMeta(s.getChannel(CN_A36_A16).getRef()).getDefaultValue();
		var dvA16sr = (BmsArray)s.getIndexedMeta(s.getChannel(CN_A36S_A16).getRef()).getDefaultValue();
		var dvA36r = (BmsArray)s.getIndexedMeta(s.getChannel(CN_A36_A36).getRef()).getDefaultValue();
		var dvA36sr = (BmsArray)s.getIndexedMeta(s.getChannel(CN_A36S_A36).getRef()).getDefaultValue();

		// テスト用ノートの生成
		var td = new ArrayList<GetResolvedNoteValueTestData>();
		if (is0Index) {
			// 重複不可チャンネル使用
			// INTEGER
			td.add(new GetResolvedNoteValueTestData(CN_A36S_INT, 0, 0, 0, 1, 11111L, Long.class));
			td.add(new GetResolvedNoteValueTestData(CN_A36S_INT, 0, 1, 48, 2, 22222L, Long.class));
			td.add(new GetResolvedNoteValueTestData(CN_A36S_INT, 0, 2, 96, 3, 33333L, Long.class));
			td.add(new GetResolvedNoteValueTestData(CN_A36S_INT, 0, 3, 144, 4, dvIntsr, Long.class));
			// FLOAT
			td.add(new GetResolvedNoteValueTestData(CN_A36S_NUM, 0, 0, 0, 1, 11111.111, Double.class));
			td.add(new GetResolvedNoteValueTestData(CN_A36S_NUM, 0, 1, 48, 2, 22222.222, Double.class));
			td.add(new GetResolvedNoteValueTestData(CN_A36S_NUM, 0, 2, 96, 3, 33333.333, Double.class));
			td.add(new GetResolvedNoteValueTestData(CN_A36S_NUM, 0, 3, 144, 4, dvNumsr, Double.class));
			// STRING
			td.add(new GetResolvedNoteValueTestData(CN_A36S_STR, 0, 0, 0, 1, "hogehoge", String.class));
			td.add(new GetResolvedNoteValueTestData(CN_A36S_STR, 0, 1, 48, 2, "hagehage", String.class));
			td.add(new GetResolvedNoteValueTestData(CN_A36S_STR, 0, 2, 96, 3, "higehige", String.class));
			td.add(new GetResolvedNoteValueTestData(CN_A36S_STR, 0, 3, 144, 4, dvStrsr, String.class));
			// BASE16
			td.add(new GetResolvedNoteValueTestData(CN_A36S_B16, 0, 0, 0, 1, 0x11L, Long.class));
			td.add(new GetResolvedNoteValueTestData(CN_A36S_B16, 0, 1, 48, 2, 0x22L, Long.class));
			td.add(new GetResolvedNoteValueTestData(CN_A36S_B16, 0, 2, 96, 3, 0x33L, Long.class));
			td.add(new GetResolvedNoteValueTestData(CN_A36S_B16, 0, 3, 144, 4, dvB16sr, Long.class));
			// BASE36
			td.add(new GetResolvedNoteValueTestData(CN_A36S_B36, 0, 0, 0, 1, Long.parseLong("XX", 36), Long.class));
			td.add(new GetResolvedNoteValueTestData(CN_A36S_B36, 0, 1, 48, 2, Long.parseLong("YY", 36), Long.class));
			td.add(new GetResolvedNoteValueTestData(CN_A36S_B36, 0, 2, 96, 3, Long.parseLong("ZZ", 36), Long.class));
			td.add(new GetResolvedNoteValueTestData(CN_A36S_B36, 0, 3, 144, 4, dvB36sr, Long.class));
			// ARRAY16
			td.add(new GetResolvedNoteValueTestData(CN_A36S_A16, 0, 0, 0, 1, new BmsArray("0011223344", 16), BmsArray.class));
			td.add(new GetResolvedNoteValueTestData(CN_A36S_A16, 0, 1, 48, 2, new BmsArray("5566778899", 16), BmsArray.class));
			td.add(new GetResolvedNoteValueTestData(CN_A36S_A16, 0, 2, 96, 3, new BmsArray("AABBCCDDEE", 16), BmsArray.class));
			td.add(new GetResolvedNoteValueTestData(CN_A36S_A16, 0, 3, 144, 4, dvA16sr, BmsArray.class));
			// ARRAY36
			td.add(new GetResolvedNoteValueTestData(CN_A36S_A36, 0, 0, 0, 1, new BmsArray("GGHHIIJJKK", 36), BmsArray.class));
			td.add(new GetResolvedNoteValueTestData(CN_A36S_A36, 0, 1, 48, 2, new BmsArray("LLMMNNOOPP", 36), BmsArray.class));
			td.add(new GetResolvedNoteValueTestData(CN_A36S_A36, 0, 2, 96, 3, new BmsArray("QQRRSSTTUU", 36), BmsArray.class));
			td.add(new GetResolvedNoteValueTestData(CN_A36S_A36, 0, 3, 144, 4, dvA36sr, BmsArray.class));
			// 参照メタ情報がないチャンネル
			td.add(new GetResolvedNoteValueTestData(CN_A16S, 0, 0, 0, 111, 111L, Long.class));
			td.add(new GetResolvedNoteValueTestData(CN_A36S, 0, 0, 0, 999, 999L, Long.class));
		} else {
			// 重複可能チャンネル使用
			// INTEGER
			td.add(new GetResolvedNoteValueTestData(CN_A36_INT, 3, 0, 0, 1, 11111L, Long.class));
			td.add(new GetResolvedNoteValueTestData(CN_A36_INT, 2, 0, 48, 2, 22222L, Long.class));
			td.add(new GetResolvedNoteValueTestData(CN_A36_INT, 1, 0, 96, 3, 33333L, Long.class));
			td.add(new GetResolvedNoteValueTestData(CN_A36_INT, 0, 0, 144, 4, dvIntr, Long.class));
			// FLOAT
			td.add(new GetResolvedNoteValueTestData(CN_A36_NUM, 3, 0, 0, 1, 11111.111, Double.class));
			td.add(new GetResolvedNoteValueTestData(CN_A36_NUM, 2, 0, 48, 2, 22222.222, Double.class));
			td.add(new GetResolvedNoteValueTestData(CN_A36_NUM, 1, 0, 96, 3, 33333.333, Double.class));
			td.add(new GetResolvedNoteValueTestData(CN_A36_NUM, 0, 0, 144, 4, dvNumr, Double.class));
			// STRING
			td.add(new GetResolvedNoteValueTestData(CN_A36_STR, 3, 0, 0, 1, "hogehoge", String.class));
			td.add(new GetResolvedNoteValueTestData(CN_A36_STR, 2, 0, 48, 2, "hagehage", String.class));
			td.add(new GetResolvedNoteValueTestData(CN_A36_STR, 1, 0, 96, 3, "higehige", String.class));
			td.add(new GetResolvedNoteValueTestData(CN_A36_STR, 0, 0, 144, 4, dvStrr, String.class));
			// BASE16
			td.add(new GetResolvedNoteValueTestData(CN_A36_B16, 3, 0, 0, 1, 0x11L, Long.class));
			td.add(new GetResolvedNoteValueTestData(CN_A36_B16, 2, 0, 48, 2, 0x22L, Long.class));
			td.add(new GetResolvedNoteValueTestData(CN_A36_B16, 1, 0, 96, 3, 0x33L, Long.class));
			td.add(new GetResolvedNoteValueTestData(CN_A36_B16, 0, 0, 144, 4, dvB16r, Long.class));
			// BASE36
			td.add(new GetResolvedNoteValueTestData(CN_A36_B36, 3, 0, 0, 1, Long.parseLong("XX", 36), Long.class));
			td.add(new GetResolvedNoteValueTestData(CN_A36_B36, 2, 0, 48, 2, Long.parseLong("YY", 36), Long.class));
			td.add(new GetResolvedNoteValueTestData(CN_A36_B36, 1, 0, 96, 3, Long.parseLong("ZZ", 36), Long.class));
			td.add(new GetResolvedNoteValueTestData(CN_A36_B36, 0, 0, 144, 4, dvB36r, Long.class));
			// ARRAY16
			td.add(new GetResolvedNoteValueTestData(CN_A36_A16, 3, 0, 0, 1, new BmsArray("0011223344", 16), BmsArray.class));
			td.add(new GetResolvedNoteValueTestData(CN_A36_A16, 2, 0, 48, 2, new BmsArray("5566778899", 16), BmsArray.class));
			td.add(new GetResolvedNoteValueTestData(CN_A36_A16, 1, 0, 96, 3, new BmsArray("AABBCCDDEE", 16), BmsArray.class));
			td.add(new GetResolvedNoteValueTestData(CN_A36_A16, 0, 0, 144, 4, dvA16r, BmsArray.class));
			// ARRAY36
			td.add(new GetResolvedNoteValueTestData(CN_A36_A36, 3, 0, 0, 1, new BmsArray("GGHHIIJJKK", 36), BmsArray.class));
			td.add(new GetResolvedNoteValueTestData(CN_A36_A36, 2, 0, 48, 2, new BmsArray("LLMMNNOOPP", 36), BmsArray.class));
			td.add(new GetResolvedNoteValueTestData(CN_A36_A36, 1, 0, 96, 3, new BmsArray("QQRRSSTTUU", 36), BmsArray.class));
			td.add(new GetResolvedNoteValueTestData(CN_A36_A36, 0, 0, 144, 4, dvA36r, BmsArray.class));
			// 参照メタ情報がないチャンネル
			td.add(new GetResolvedNoteValueTestData(CN_A16, 1, 0, 0, 111, 111L, Long.class));
			td.add(new GetResolvedNoteValueTestData(CN_A36, 1, 0, 0, 999, 999L, Long.class));
		}

		// ノートを登録
		td.forEach(d -> c.putNote(d.channel, d.index, d.measure, d.tick, d.noteValue));
		c.endEdit();
		return td;
	}

	private static void getEnumNotesTestData(BmsContent c) {
		c.beginEdit();
		c.setInitialBpm(120.0);
		c.putNote(1, 0, 0, 0, 10);
		c.putNote(1, 1, 0, 48, 20);
		c.putNote(1, 2, 0, 96, 30);
		c.putNote(1, 0, 1, 0, 100);
		c.putNote(1, 0, 2, 48, 200);
		c.putNote(1, 0, 2, 96, 250);
		c.putNote(11, 0, 3, 0, 1000);
		c.putNote(11, 0, 3, 8, 1005);
		c.putNote(11, 0, 4, 16, 1010);
		c.putNote(11, 0, 4, 24, 1015);
		c.putNote(11, 0, 5, 32, 1020);
		c.putNote(11, 0, 5, 40, 1025);
		c.putNote(11, 0, 6, 48, 1000);
		c.putNote(11, 0, 6, 56, 1050);
		c.putNote(21, 0, 7, 0, 1100);
		c.putNote(21, 0, 7, 32, 1105);
		c.putNote(21, 0, 8, 64, 1110);
		c.putNote(21, 0, 8, 96, 1115);
		c.putNote(21, 0, 9, 128, 1120);
		c.putNote(31, 0, 10, 0, 1);
		c.putNote(31, 0, 10, 48, 2);
		c.putNote(31, 0, 10, 96, 3);
		c.endEdit();
	}

	private static List<NoteTestData> getGetNoteTestData(BmsContent c, boolean isZeroIndex) {
		var ret = new ArrayList<NoteTestData>();
		c.beginEdit();
		if (isZeroIndex) {
			// 重複不可チャンネル
			ret.add(new NoteTestData(11, 0, 0, 0, 10));
			ret.add(new NoteTestData(11, 0, 1, 0, 20));
			ret.add(new NoteTestData(11, 0, 2, 0, 30));
			ret.add(new NoteTestData(11, 0, BmsSpec.MEASURE_MAX, 0, 1295));
			ret.add(new NoteTestData(21, 0, 10, 0, 100));
			ret.add(new NoteTestData(21, 0, 10, 96, 200));
			ret.add(new NoteTestData(21, 0, 10, 191, 300));
		} else {
			// 重複可能チャンネル
			ret.add(new NoteTestData(1, BmsSpec.CHINDEX_MIN, 0, 0, 10));
			ret.add(new NoteTestData(1, BmsSpec.CHINDEX_MAX / 2, 1, 32, 20));
			ret.add(new NoteTestData(1, BmsSpec.CHINDEX_MAX, 2, 64, 30));
			ret.add(new NoteTestData(CN_A36, 0, 10, 0, 100));
			ret.add(new NoteTestData(CN_A36, 1, 11, 48, 200));
			ret.add(new NoteTestData(CN_A36, 2, 12, 96, 300));
			ret.add(new NoteTestData(CN_A36, 3, BmsSpec.MEASURE_MAX, 191, 1295));
		}
		ret.forEach(t -> c.putNote(t.channel, t.index, t.measure, t.tick, t.value));
		c.endEdit();
		return ret;
	}

	private static BmsContent getTestTimeContent() {
		// 計算式の解説
		// time = (tick / 48) * (60 / bpm)
		// time = (60 * tick) / (48 * bpm)
		// time = (5 * tick) / (4 * bpm)
		// (5 * tick) = time * (4 * bpm)
		// tick = (4 * time * bpm) / 5
		// tick = 0.8 * time * bpm ★

		var c = new BmsContent(spec());

		c.beginEdit();
		// 初期BPMは120
		c.setInitialBpm(120.0);

		// 1小節目：BPM120 4/4拍子 計2秒 開始位置：0
		c.setMeasureValue(2, 0, 1.0);

		// 2小節目：BPM120 2/4拍子 計1秒 開始位置：2
		c.setMeasureValue(2, 1, 0.5);

		// 3小節目：BPM120 4/4拍子 2拍目で4拍停止 計4秒 開始位置：3
		c.setIndexedMeta("#stop", 1, 192L);
		c.setMeasureValue(2, 2, 1.0);
		c.putNote(9, 0, 2, 96, 1);

		// 4小節目：BPM240 4/4拍子 0泊目でBPMを240に変更 計1秒 開始位置：7
		c.setIndexedMeta("#bpm", 1, 240.0);
		c.setMeasureValue(2, 3, 1.0);
		c.putNote(8, 0, 3, 0, 1);

		// 5小節目：BPM240→120 4/4拍子 2拍目でBPMを120に変更 計1.5秒 開始位置：8
		c.setIndexedMeta("#bpm", 2, 120.0);
		c.setMeasureValue(2, 4, 1.0);
		c.putNote(8, 0, 4, 96, 2);

		// 6小節目：BPM120→240 4/4拍子 2拍目でBPMを240に変更 3拍目で4拍停止 計2.5秒 開始位置：9.5
		c.setIndexedMeta("#bpm", 3, 240.0);
		c.setIndexedMeta("#stop", 2, 192L);
		c.setMeasureValue(2, 5, 1.0);
		c.putNote(8, 0, 5, 96, 3);
		c.putNote(9, 0, 5, 144, 2);

		// 7小節目：BPM240→150 3/4拍子 計1.2秒 開始位置：12
		c.setIndexedMeta("#bpm", 4, 150.0);
		c.setMeasureValue(2, 6, 0.75);
		c.putNote(8, 0, 6, 0, 4);

		// 8小節目：BPM150→120 4/4拍子 計2秒 開始位置：13.2
		c.setIndexedMeta("#bpm", 5, 120.0);
		c.setMeasureValue(2, 7, 1.0);
		c.putNote(8, 0, 7, 0, 5);

		// 総時間：15.2

		c.endEdit();

		return c;
	}

	private static BmsContent getTestTimeContentForMinimalLengthMeasure() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.setInitialBpm(120.0);

		// 1小節目：小節長=1, BPM=120, 小節時間=2.0s
		c.setMeasureValue(2, 0, 1.0);

		// 2小節目：小節長=0.0001, BPM=0.024, 小節時間=1.0s
		c.setIndexedMeta("#bpm", 1, 0.024);
		c.setMeasureValue(2, 1, 0.0001);
		c.putNote(8, 1, 0.0, 1);

		// 3小節目：小節長=0.0002, BPM=0.024(前小節の値を継承), 小節時間=2.0s
		c.setMeasureValue(2, 2, 0.0002);

		// 4小節目：小節長=0.0005, BPM=0.04, 小節時間=3.0s
		c.setIndexedMeta("#bpm", 2, 0.04);
		c.setMeasureValue(2, 3, 0.0005);
		c.putNote(8, 3, 0.0, 2);

		// 5小節目：小節長=0.5, BPM=120, 小節時間=1.0s
		c.setIndexedMeta("#bpm", 3, 120.0);
		c.setMeasureValue(2, 4, 0.5);
		c.putNote(8, 4, 0.0, 3);

		// 総時間 = 2.0 + 1.0 + 2.0 + 3.0 + 1.0 = 9s
		c.endEdit();

		return c;
	}

	private static BmsContent contentForTimeline() {
		var c = new BmsContent(spec());
		c.beginEdit();

		// ***** 0小節目 *****
		// --- M=0, T=0: 小節線のみ
		// Do nothing
		// --- M=0, T=96: ノートのみ
		c.putNote(CN_A36S, 0, 0, 96.0, 100);

		// ***** 1小節目 *****
		// --- M=1, T=0: 小節線、小節データ、ノート全てあり
		c.setMeasureValue(CN_STR, 0, 1, "STRING1");
		c.setMeasureValue(CN_STR, 1, 1, "STRING2");
		c.setMeasureValue(CN_INTS, 0, 1, 10L);
		c.setMeasureValue(CN_OBJS, 0, 1, BmsPoint.of(1, 2.0));
		c.putNote(CN_A36, 0, 1, 0.0, 200);
		c.putNote(CN_A36, 1, 1, 0.0, 300);
		c.putNote(CN_A36S, 0, 1, 0.0, 400);
		// --- M=1, T=0next: ノートのみ
		c.putNote(CN_A36S, 0, 1, Math.nextUp(0.0), 500);

		// ***** 2小節目 *****
		// --- M=2, T=0: 小節線、ノート (小節データなし)
		c.putNote(CN_A36, 0, 2, 0.0, 600);
		c.putNote(CN_A36, 1, 2, 0.0, 700);
		c.putNote(CN_A36, 2, 2, 0.0, 800);

		// ***** 3小節目 *****
		// ----- M=3, T=0: 小節線のみ
		// Do nothing

		// ***** 4小節目 *****
		// ----- M=4, T=0: 小節線のみ
		// Do nothing

		// ***** 5小節目 *****
		// ----- M=5, T=0: 小節線、小節データ (ノートなし)
		c.setMeasureValue(CN_INT, 0, 5, 20L);
		c.setMeasureValue(CN_INT, 1, 5, 30L);
		c.setMeasureValue(CN_INT, 2, 5, 40L);
		c.setMeasureValue(CN_NUMS, 0, 5, 50.0);
		// ----- M=5, T=192prev: ノートのみ
		c.putNote(CN_A36S, 0, 5, Math.nextDown(BmsSpec.TICK_COUNT_DEFAULT), 900);

		// ***** 6小節目 *****
		// ----- M=6, T=0: 小節線のみ
		// Do nothing
		// ----- M=6, T=96prev: ノートのみ
		c.putNote(CN_A36S, 0, 6, Math.nextDown(96.0), 990);
		// ----- M=6, T=96: ノートのみ
		c.putNote(CN_A36, 0, 6, 96.0, 1000);
		c.putNote(CN_A36, 1, 6, 96.0, 1010);
		c.putNote(CN_A36, 2, 6, 96.0, 1020);
		c.putNote(CN_A36S, 0, 6, 96.0, 1030);
		// ----- M=6, T=96next: ノートのみ
		c.putNote(CN_A36S, 0, 6, Math.nextUp(96.0), 1040);

		c.endEdit();
		return c;
	}

	private static BmsContent contentForTimelineEmpty() {
		return new BmsContent(spec());
	}

	private static BmsNote newBmsNote(int channel, int index, int measure, double tick, int value) {
		var n = new BmsNote();
		n.setup(channel, index, measure, tick, value);
		return n;
	}

	private static void assertMeasureLineElement(BmsTimelineElement e, int measure) {
		assertNotNull(e);
		assertTrue(e.isMeasureLineChannel());
		assertFalse(e.isSpecChannel());
		assertFalse(e.isUserChannel());
		assertTrue(e.isMeasureLineElement());
		assertFalse(e.isMeasureValueElement());
		assertFalse(e.isNoteElement());
		assertTrue(e.onMeasureLine());
		assertEquals(measure, e.getMeasure());
		assertEquals(BmsSpec.TICK_MIN, e.getTick(), 0.0);
		assertEquals(BmsSpec.CHANNEL_MEASURE, e.getChannel());
		assertEquals(BmsSpec.CHINDEX_MIN, e.getIndex());
		assertEquals(measure, e.getValueAsLong());
		assertEquals((double)measure, e.getValueAsDouble(), 0.0);
		assertEquals(String.valueOf(measure), e.getValueAsString());
		assertThrows(ClassCastException.class, () -> e.getValueAsArray());
		assertEquals(measure, e.getValueAsObject());
	}

	private static void assertMeasureValueElement(BmsTimelineElement e, int measure, int channel, int index, Object value) {
		assertNotNull(e);
		assertFalse(e.isMeasureLineChannel());
		assertFalse(e.isMeasureLineElement());
		assertTrue(e.isMeasureValueElement());
		assertFalse(e.isNoteElement());
		assertTrue(e.onMeasureLine());
		assertEquals(measure, e.getMeasure());
		assertEquals(BmsSpec.TICK_MIN, e.getTick(), 0.0);
		assertEquals(channel, e.getChannel());
		assertEquals(index, e.getIndex());
		assertEquals(value, e.getValueAsObject());
	}

	private static void assertNoteElement(BmsTimelineElement e, int measure, double tick, int channel, int index, int value) {
		assertNotNull(e);
		assertFalse(e.isMeasureLineChannel());
		assertFalse(e.isMeasureLineElement());
		assertFalse(e.isMeasureValueElement());
		assertTrue(e.isNoteElement());
		assertEquals(measure, e.getMeasure());
		assertEquals(tick, e.getTick(), 0.0);
		assertEquals(channel, e.getChannel());
		assertEquals(index, e.getIndex());
		assertEquals(value, e.getValueAsLong());
		assertEquals((double)value, e.getValueAsDouble(), 0.0);
		assertEquals(String.valueOf(value), e.getValueAsString());
		assertThrows(ClassCastException.class, () -> e.getValueAsArray());
		assertEquals(value, e.getValueAsObject());
	}
}
