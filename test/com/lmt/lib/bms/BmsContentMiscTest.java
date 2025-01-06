package com.lmt.lib.bms;

import static org.junit.Assert.*;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.junit.Test;

public class BmsContentMiscTest extends BmsContentTest {
	private static class NoteForConstructorTest extends BmsNote {
		static final Supplier<BmsNote> CREATOR = () -> new NoteForConstructorTest();
		@Override protected BmsNote onNewInstance() { return CREATOR.get(); }
	}

	private static class ContentForEditTest extends BmsContent {
		boolean calledOnBeginEdit = false;
		boolean calledOnEndEdit = false;
		public ContentForEditTest(BmsSpec spec) { super(spec); }
		@Override protected void onBeginEdit() { calledOnBeginEdit = true; }
		@Override protected void onEndEdit(boolean isRecalculateTime) { calledOnEndEdit = true; }
	}

	// BmsContent(BmsSpec)
	// 正常
	@Test
	public void testBmsContentBmsSpec_001() {
		new BmsContent(spec());
	}

	// BmsContent(BmsSpec)
	// NullPointerException specがnull
	@Test
	public void testBmsContentBmsSpec_002() {
		assertThrows(NullPointerException.class, () -> new BmsContent((BmsSpec)null));
	}

	// BmsContent(BmsContent)
	// BMS宣言：同じ値が同じ順でコピーされること
	@Test
	public void testBmsContentBmsContent_HasDeclaration() {
		var src = new BmsContent(specForConstructorTest());
		src.beginEdit();
		src.putDeclaration("key1", "hoge");
		src.putDeclaration("key2", "hage");
		src.putDeclaration("key3", "hige");
		src.putDeclaration("key4", "hege");
		src.removeDeclaration("key3");
		src.endEdit();
		var dst = new BmsContent(src);
		var decls = dst.declarations().collect(Collectors.toList());
		assertEquals(3, decls.size());
		assertEquals("key1", decls.get(0).getName());
		assertEquals("hoge", decls.get(0).getValue());
		assertEquals("key2", decls.get(1).getName());
		assertEquals("hage", decls.get(1).getValue());
		assertEquals("key4", decls.get(2).getName());
		assertEquals("hege", decls.get(2).getValue());
	}

	// BmsContent(BmsContent)
	// BMS宣言：0件の場合はコピー先も0件になること
	@Test
	public void testBmsContentBmsContent_NoDeclaration() {
		var src = new BmsContent(specForConstructorTest());
		var dst = new BmsContent(src);
		var decls = dst.declarations().collect(Collectors.toList());
		assertEquals(0, decls.size());
	}

	// BmsContent(BmsContent)
	// メタ情報：同じメタ情報、インデックス値の場所にメタ情報の値がコピーされること
	@Test
	public void testBmsContentBmsContent_HasMeta() {
		var s = specForConstructorTest();
		var src = new BmsContent(s);
		src.beginEdit();
		src.setSingleMeta("#sint", 100L);
		src.setMultipleMeta("#mstr", 0, "STR1");
		src.setMultipleMeta("#mstr", 2, "STR2");
		src.setIndexedMeta("#iobj", 1, BmsPoint.of(1, 2));
		src.setIndexedMeta("#iobj", 3, BmsPoint.of(3, 4));
		src.setIndexedMeta("#iobj", 4, BmsPoint.of(5, 6));
		src.endEdit();
		var dst = new BmsContent(src);
		assertEquals(100L, (long)dst.getSingleMeta("#sint"));
		assertFalse(dst.containsSingleMeta("#sstr"));
		assertFalse(dst.containsSingleMeta("#sobj"));
		assertEquals("STR1", dst.getMultipleMeta("#mstr", 0));
		assertEquals(s.getMultipleMeta("#mstr").getDefaultValue(), dst.getMultipleMeta("#mstr", 1));
		assertEquals("STR2", dst.getMultipleMeta("#mstr", 2));
		assertEquals(BmsPoint.of(1, 2), dst.getIndexedMeta("#iobj", 1));
		assertFalse(dst.containsIndexedMeta("#iobj", 2));
		assertEquals(BmsPoint.of(3, 4), dst.getIndexedMeta("#iobj", 3));
		assertEquals(BmsPoint.of(5, 6), dst.getIndexedMeta("#iobj", 4));
	}

	// BmsContent(BmsContent)
	// メタ情報：全て空の場合はコピー先も全て空になること
	@Test
	public void testBmsContentBmsContent_NoMeta() {
		var src = new BmsContent(specForConstructorTest());
		var dst = new BmsContent(src);
		assertEquals(0L, dst.metas().filter(m -> m.isContain()).count());
	}

	// BmsContent(BmsContent)
	// タイムライン要素：同じアドレスに同じ値がコピーされること
	@Test
	public void testBmsContentBmsContent_HasTimeline() {
		var creator = NoteForConstructorTest.CREATOR;
		var src = new BmsContent(specForConstructorTest());
		src.beginEdit();
		src.setMeasureValue(4, 0, 0, 1L);
		src.setMeasureValue(4, 0, 2, 2L);
		src.setMeasureValue(4, 0, 4, 3L);
		src.setMeasureValue(5, 0, 1, 10L);
		src.setMeasureValue(5, 1, 1, 20L);
		src.setMeasureValue(5, 3, 1, 30L);
		src.putNote(6, 0, 0, 48.0, 100, creator);
		src.putNote(7, 1, 0, 24.0, 200, creator);
		src.putNote(7, 2, 0, 64.0, 201, creator);
		src.putNote(6, 0, 1, 96.0, 101, creator);
		src.putNote(6, 0, 2, 144.0, 102, creator);
		src.putNote(7, 4, 3, 168.0, 202, creator);
		src.endEdit();
		var dst = new BmsContent(src);
		assertEquals(1L, (long)dst.getMeasureValue(4, 0, 0));
		assertEquals(2L, (long)dst.getMeasureValue(4, 0, 2));
		assertEquals(3L, (long)dst.getMeasureValue(4, 0, 4));
		assertEquals(10L, (long)dst.getMeasureValue(5, 0, 1));
		assertEquals(20L, (long)dst.getMeasureValue(5, 1, 1));
		assertEquals(30L, (long)dst.getMeasureValue(5, 3, 1));
		assertEquals(100, dst.getNote(6, 0, 0, 48.0).getValue());
		assertEquals(200, dst.getNote(7, 1, 0, 24.0).getValue());
		assertEquals(201, dst.getNote(7, 2, 0, 64.0).getValue());
		assertEquals(101, dst.getNote(6, 0, 1, 96.0).getValue());
		assertEquals(102, dst.getNote(6, 0, 2, 144.0).getValue());
		assertEquals(202, dst.getNote(7, 4, 3, 168.0).getValue());
		var notes = dst.timeline().filter(e -> e.isNoteElement()).collect(Collectors.toList());
		assertEquals(0, notes.stream().filter(n -> !(n instanceof NoteForConstructorTest)).count());
	}

	// BmsContent(BmsContent)
	// タイムライン要素：0件の場合はコピー先も0件になること
	@Test
	public void testBmsContentBmsContent_NoTimeline() {
		var src = new BmsContent(specForConstructorTest());
		var dst = new BmsContent(src);
		assertEquals(0L, dst.timeline().count());
	}

	// BmsContent(BmsContent)
	// NullPointerException srcがnull
	@Test
	public void testBmsContentBmsContent_NullSrc() {
		assertThrows(NullPointerException.class, () -> new BmsContent((BmsContent)null));
	}

	// BmsContent(BmsContent)
	// IllegalArgumentException srcが編集モード
	@Test
	public void testBmsContentBmsContent_EdittingSrc() {
		var src = new BmsContent(specForConstructorTest());
		src.beginEdit();
		assertThrows(IllegalArgumentException.class, () -> new BmsContent(src));
	}

	// BmsContent(BmsContent, Function, Function, Function, UnaryOperator)
	// 無変換：BMS宣言：同じ値が同じ順でコピーされること
	@Test
	public void testBmsContent_WithConverter_NoConvertDeclaration() {
		var src = new BmsContent(specForConstructorTest());
		src.beginEdit();
		src.putDeclaration("key1", "hoge");
		src.putDeclaration("key2", "hage");
		src.putDeclaration("key3", "hige");
		src.putDeclaration("key4", "hege");
		src.removeDeclaration("key3");
		src.endEdit();
		var dst = new BmsContent(src, null, null, null, null);
		var decls = dst.declarations().collect(Collectors.toList());
		assertEquals(3, decls.size());
		assertEquals("key1", decls.get(0).getName());
		assertEquals("hoge", decls.get(0).getValue());
		assertEquals("key2", decls.get(1).getName());
		assertEquals("hage", decls.get(1).getValue());
		assertEquals("key4", decls.get(2).getName());
		assertEquals("hege", decls.get(2).getValue());
	}

	// BmsContent(BmsContent, Function, Function, Function, UnaryOperator)
	// 無変換：メタ情報：同じメタ情報、インデックス値の場所にメタ情報の値がコピーされること
	@Test
	public void testBmsContent_WithConverter_NoConvertMeta() {
		var s = specForConstructorTest();
		var src = new BmsContent(s);
		src.beginEdit();
		src.setSingleMeta("#sint", 100L);
		src.setMultipleMeta("#mstr", 0, "STR1");
		src.setMultipleMeta("#mstr", 2, "STR2");
		src.setIndexedMeta("#iobj", 1, BmsPoint.of(1, 2));
		src.setIndexedMeta("#iobj", 3, BmsPoint.of(3, 4));
		src.setIndexedMeta("#iobj", 4, BmsPoint.of(5, 6));
		src.endEdit();
		var dst = new BmsContent(src, null, null, null, null);
		assertEquals(100L, (long)dst.getSingleMeta("#sint"));
		assertFalse(dst.containsSingleMeta("#sstr"));
		assertFalse(dst.containsSingleMeta("#sobj"));
		assertEquals("STR1", dst.getMultipleMeta("#mstr", 0));
		assertEquals(s.getMultipleMeta("#mstr").getDefaultValue(), dst.getMultipleMeta("#mstr", 1));
		assertEquals("STR2", dst.getMultipleMeta("#mstr", 2));
		assertEquals(BmsPoint.of(1, 2), dst.getIndexedMeta("#iobj", 1));
		assertFalse(dst.containsIndexedMeta("#iobj", 2));
		assertEquals(BmsPoint.of(3, 4), dst.getIndexedMeta("#iobj", 3));
		assertEquals(BmsPoint.of(5, 6), dst.getIndexedMeta("#iobj", 4));
	}

	// BmsContent(BmsContent, Function, Function, Function, UnaryOperator)
	// 無変換：タイムライン要素：同じアドレスに同じ値がコピーされること
	@Test
	public void testBmsContent_WithConverter_NoConvertTimeline() {
		var creator = NoteForConstructorTest.CREATOR;
		var src = new BmsContent(specForConstructorTest());
		src.beginEdit();
		src.setMeasureValue(4, 0, 0, 1L);
		src.setMeasureValue(4, 0, 2, 2L);
		src.setMeasureValue(4, 0, 4, 3L);
		src.setMeasureValue(5, 0, 1, 10L);
		src.setMeasureValue(5, 1, 1, 20L);
		src.setMeasureValue(5, 3, 1, 30L);
		src.putNote(6, 0, 0, 48.0, 100, creator);
		src.putNote(7, 1, 0, 24.0, 200, creator);
		src.putNote(7, 2, 0, 64.0, 201, creator);
		src.putNote(6, 0, 1, 96.0, 101, creator);
		src.putNote(6, 0, 2, 144.0, 102, creator);
		src.putNote(7, 4, 3, 168.0, 202, creator);
		src.endEdit();
		var dst = new BmsContent(src, null, null, null, null);
		assertEquals(1L, (long)dst.getMeasureValue(4, 0, 0));
		assertEquals(2L, (long)dst.getMeasureValue(4, 0, 2));
		assertEquals(3L, (long)dst.getMeasureValue(4, 0, 4));
		assertEquals(10L, (long)dst.getMeasureValue(5, 0, 1));
		assertEquals(20L, (long)dst.getMeasureValue(5, 1, 1));
		assertEquals(30L, (long)dst.getMeasureValue(5, 3, 1));
		assertEquals(100, dst.getNote(6, 0, 0, 48.0).getValue());
		assertEquals(200, dst.getNote(7, 1, 0, 24.0).getValue());
		assertEquals(201, dst.getNote(7, 2, 0, 64.0).getValue());
		assertEquals(101, dst.getNote(6, 0, 1, 96.0).getValue());
		assertEquals(102, dst.getNote(6, 0, 2, 144.0).getValue());
		assertEquals(202, dst.getNote(7, 4, 3, 168.0).getValue());
		var notes = dst.timeline().filter(e -> e.isNoteElement()).collect(Collectors.toList());
		assertEquals(0, notes.stream().filter(n -> !(n instanceof NoteForConstructorTest)).count());
	}

	// BmsContent(BmsContent, Function, Function, Function, UnaryOperator)
	// BMS宣言：コンバータ関数で返した値が当該キーの値として設定されること
	@Test
	public void testBmsContent_WithConverter_Declaration_Convert() {
		var src = new BmsContent(specForConstructorTest());
		src.beginEdit();
		src.putDeclaration("key1", "hoge");
		src.putDeclaration("key2", "hage");
		src.endEdit();
		var dst = new BmsContent(src, d -> d.getName().equals("key2") ? "xxx" : d.getValue(), null, null, null);
		var decls = dst.declarations().collect(Collectors.toList());
		assertEquals(2, decls.size());
		assertEquals("key1", decls.get(0).getName());
		assertEquals("hoge", decls.get(0).getValue());
		assertEquals("key2", decls.get(1).getName());
		assertEquals("xxx", decls.get(1).getValue());
	}

	// BmsContent(BmsContent, Function, Function, Function, UnaryOperator)
	// BMS宣言：コンバータ関数でnullを返すと当該キーは設定されないこと
	@Test
	public void testBmsContent_WithConverter_Declaration_Null() {
		var src = new BmsContent(specForConstructorTest());
		src.beginEdit();
		src.putDeclaration("key1", "hoge");
		src.putDeclaration("key2", "hage");
		src.endEdit();
		var dst = new BmsContent(src, d -> d.getName().equals("key2") ? null : d.getValue(), null, null, null);
		var decls = dst.declarations().collect(Collectors.toList());
		assertEquals(1, decls.size());
		assertEquals("key1", decls.get(0).getName());
		assertEquals("hoge", decls.get(0).getValue());
	}

	// BmsContent(BmsContent, Function, Function, Function, UnaryOperator)
	// メタ情報：コンバータ関数で返した値が当該メタ情報の値として設定されること
	@Test
	public void testBmsContent_WithConverter_Meta_Convert() {
		var s = specForConstructorTest();
		var src = new BmsContent(s);
		src.beginEdit();
		src.setSingleMeta("#sint", 100L);
		src.setMultipleMeta("#mstr", 1, "STR1");
		src.setIndexedMeta("#iobj", 1, BmsPoint.of(1, 2));
		src.endEdit();
		var dst = new BmsContent(src, null, m -> m.isMultipleUnit() && m.getValue().equals("STR1") ? "xxx" : m.getRawValue(), null, null);
		assertEquals(100L, (long)dst.getSingleMeta("#sint"));
		assertFalse(dst.containsSingleMeta("#sstr"));
		assertFalse(dst.containsSingleMeta("#sobj"));
		assertEquals(s.getMultipleMeta("#mstr").getDefaultValue(), dst.getMultipleMeta("#mstr", 0));
		assertEquals("xxx", dst.getMultipleMeta("#mstr", 1));
		assertFalse(dst.containsIndexedMeta("#iobj", 0));
		assertEquals(BmsPoint.of(1, 2), dst.getIndexedMeta("#iobj", 1));
	}

	// BmsContent(BmsContent, Function, Function, Function, UnaryOperator)
	// メタ情報：コンバータ関数でnullを返すと当該メタ情報は設定されないこと
	@Test
	public void testBmsContent_WithConverter_Meta_Null() {
		var s = specForConstructorTest();
		var src = new BmsContent(s);
		src.beginEdit();
		src.setSingleMeta("#sint", 100L);
		src.setMultipleMeta("#mstr", 0, "STR1");
		src.setIndexedMeta("#iobj", 1, BmsPoint.of(1, 2));
		src.setIndexedMeta("#iobj", 3, BmsPoint.of(3, 4));
		src.setIndexedMeta("#iobj", 4, BmsPoint.of(5, 6));
		src.endEdit();
		var dst = new BmsContent(src, null, m -> m.getName().equals("#iobj") ? null : m.getRawValue(), null, null);
		assertEquals(100L, (long)dst.getSingleMeta("#sint"));
		assertEquals("STR1", dst.getMultipleMeta("#mstr", 0));
		assertFalse(dst.containsIndexedMeta("#iobj", 1));
		assertFalse(dst.containsIndexedMeta("#iobj", 3));
		assertFalse(dst.containsIndexedMeta("#iobj", 4));
	}

	// BmsContent(BmsContent, Function, Function, Function, UnaryOperator)
	// 小節データ：コンバータ関数で返した値が当該小節データの値として設定されること
	@Test
	public void testBmsContent_WithConverter_MeasureValue_Convert() {
		var creator = NoteForConstructorTest.CREATOR;
		var src = new BmsContent(specForConstructorTest());
		src.beginEdit();
		src.setMeasureValue(4, 0, 0, 1L);
		src.setMeasureValue(5, 0, 1, 10L);
		src.setMeasureValue(5, 1, 1, 20L);
		src.setMeasureValue(5, 3, 1, 30L);
		src.putNote(6, 0, 0, 48.0, 100, creator);
		src.endEdit();
		var dst = new BmsContent(src, null, null, v -> v.getChannel() == 5 ? v.getValueAsLong() + 100L : v.getValueAsObject(), null);
		assertEquals(1L, (long)dst.getMeasureValue(4, 0, 0));
		assertEquals(110L, (long)dst.getMeasureValue(5, 0, 1));
		assertEquals(120L, (long)dst.getMeasureValue(5, 1, 1));
		assertEquals(130L, (long)dst.getMeasureValue(5, 3, 1));
		assertEquals(100, dst.getNote(6, 0, 0, 48.0).getValue());
	}

	// BmsContent(BmsContent, Function, Function, Function, UnaryOperator)
	// 小節データ：コンバータ関数でnullを返すと当該小節データは設定されないこと
	@Test
	public void testBmsContent_WithConverter_MeasureValue_Null() {
		var creator = NoteForConstructorTest.CREATOR;
		var src = new BmsContent(specForConstructorTest());
		src.beginEdit();
		src.setMeasureValue(4, 0, 0, 1L);
		src.setMeasureValue(4, 0, 2, 2L);
		src.setMeasureValue(4, 0, 4, 3L);
		src.setMeasureValue(5, 0, 1, 10L);
		src.putNote(6, 0, 0, 48.0, 100, creator);
		src.endEdit();
		var dst = new BmsContent(src, null, null, v -> v.getChannel() == 4 ? null : v.getValueAsObject(), null);
		assertFalse(dst.containsMeasureValue(4, 0, 0));
		assertFalse(dst.containsMeasureValue(4, 0, 2));
		assertFalse(dst.containsMeasureValue(4, 0, 4));
		assertEquals(10L, (long)dst.getMeasureValue(5, 0, 1));
		assertEquals(100, dst.getNote(6, 0, 0, 48.0).getValue());
	}

	// BmsContent(BmsContent, Function, Function, Function, UnaryOperator)
	// ノート：コンバータ関数で返したノートが設定されること
	@Test
	public void testBmsContent_WithConverter_Note_Convert() {
		var creator = NoteForConstructorTest.CREATOR;
		var src = new BmsContent(specForConstructorTest());
		src.beginEdit();
		src.setMeasureValue(4, 0, 0, 1L);
		src.putNote(6, 0, 0, 48.0, 100, creator);
		src.putNote(7, 1, 0, 24.0, 200, creator);
		src.putNote(7, 2, 0, 64.0, 201, creator);
		src.putNote(6, 0, 1, 96.0, 101, creator);
		src.putNote(6, 0, 2, 144.0, 102, creator);
		src.putNote(7, 4, 3, 168.0, 202, creator);
		src.endEdit();
		var dst = new BmsContent(src, null, null, null, n ->
				n.getChannel() == 6 ? n.newNote(7, 10, n.getMeasure(), n.getTick(), n.getValue() + 1000) : n);
		assertEquals(1L, (long)dst.getMeasureValue(4, 0, 0));
		assertNull(dst.getNote(6, 0, 0, 48.0));
		assertEquals(1100, dst.getNote(7, 10, 0, 48.0).getValue());
		assertEquals(200, dst.getNote(7, 1, 0, 24.0).getValue());
		assertEquals(201, dst.getNote(7, 2, 0, 64.0).getValue());
		assertNull(dst.getNote(6, 0, 1, 96.0));
		assertEquals(1101, dst.getNote(7, 10, 1, 96.0).getValue());
		assertNull(dst.getNote(6, 0, 2, 144.0));
		assertEquals(1102, dst.getNote(7, 10, 2, 144.0).getValue());
		assertEquals(202, dst.getNote(7, 4, 3, 168.0).getValue());
		var notes = dst.timeline().filter(e -> e.isNoteElement()).collect(Collectors.toList());
		assertEquals(0, notes.stream().filter(n -> !(n instanceof NoteForConstructorTest)).count());
	}

	// BmsContent(BmsContent, Function, Function, Function, UnaryOperator)
	// ノート：コンバータ関数でnullを返すと当該ノートは設定されないこと
	@Test
	public void testBmsContent_WithConverter_Note_Null() {
		var creator = NoteForConstructorTest.CREATOR;
		var src = new BmsContent(specForConstructorTest());
		src.beginEdit();
		src.setMeasureValue(4, 0, 0, 1L);
		src.putNote(6, 0, 0, 48.0, 100, creator);
		src.putNote(7, 1, 0, 24.0, 200, creator);
		src.putNote(7, 2, 0, 64.0, 201, creator);
		src.putNote(6, 0, 1, 96.0, 101, creator);
		src.putNote(6, 0, 2, 144.0, 102, creator);
		src.putNote(7, 4, 3, 168.0, 202, creator);
		src.endEdit();
		var dst = new BmsContent(src, null, null, null, n -> n.getTick() >= 96.0 ? null : n);
		assertEquals(1L, (long)dst.getMeasureValue(4, 0, 0));
		assertEquals(100, dst.getNote(6, 0, 0, 48.0).getValue());
		assertEquals(200, dst.getNote(7, 1, 0, 24.0).getValue());
		assertEquals(201, dst.getNote(7, 2, 0, 64.0).getValue());
		assertNull(dst.getNote(6, 0, 1, 96.0));
		assertNull(dst.getNote(6, 0, 2, 144.0));
		assertNull(dst.getNote(7, 4, 3, 168.0));
		var notes = dst.timeline().filter(e -> e.isNoteElement()).collect(Collectors.toList());
		assertEquals(0, notes.stream().filter(n -> !(n instanceof NoteForConstructorTest)).count());
	}

	// BmsContent(BmsContent, Function, Function, Function, UnaryOperator)
	// NullPointerException srcがnull
	@Test
	public void testBmsContent_WithConverter_NullSrc() {
		assertThrows(NullPointerException.class, () -> new BmsContent((BmsContent)null, null, null, null, null));
	}

	// BmsContent(BmsContent, Function, Function, Function, UnaryOperator)
	// IllegalArgumentException srcが編集モード
	@Test
	public void testBmsContent_WithConverter_EdittingSrc() {
		var src = new BmsContent(specForConstructorTest());
		src.beginEdit();
		assertThrows(IllegalArgumentException.class, () -> new BmsContent(src, null, null, null, null));
	}

	// BmsContent(BmsContent, Function, Function, Function, UnaryOperator)
	// ClassCastException メタ情報・小節データの変換で、当該要素のデータ型に適合しないデータを返した
	@Test
	public void testBmsContent_WithConverter_BadCast_MetaAndMeasureValue() {
		var src = new BmsContent(specForConstructorTest());
		var ex = ClassCastException.class;
		src.beginEdit();
		src.setSingleMeta("#sint", 100L);
		src.setMeasureValue(1, 0, 0, 100.0);
		src.endEdit();
		assertThrows(ex, () -> new BmsContent(src, null, m -> m.isContain() ? "" : m.getRawValue(), null, null));
		assertThrows(ex, () -> new BmsContent(src, null, null, v -> "", null));
	}

	// BmsContent(BmsContent, Function, Function, Function, UnaryOperator)
	// IllegalArgumentException メタ情報の変換で、初期BPMメタ情報に{@link BmsSpec#BPM_MIN}未満または{@link BmsSpec#BPM_MAX}超過の値を設定しようとした
	@Test
	public void testBmsContent_WithConverter_WrongInitialBpm() {
		var src = new BmsContent(specForConstructorTest());
		var ex = IllegalArgumentException.class;
		src.beginEdit();
		src.setSingleMeta("#sbpm", 130.0);
		src.endEdit();
		assertThrows(ex, () -> new BmsContent(src, null, m -> m.isContain() ? Math.nextDown(BmsSpec.BPM_MIN) : m.getRawValue(), null, null));
		assertThrows(ex, () -> new BmsContent(src, null, m -> m.isContain() ? Math.nextUp(BmsSpec.BPM_MAX) : m.getRawValue(), null, null));
	}

	// BmsContent(BmsContent, Function, Function, Function, UnaryOperator)
	// IllegalArgumentException メタ情報の変換で、BPM変更メタ情報に{@link BmsSpec#BPM_MIN}未満または{@link BmsSpec#BPM_MAX}超過の値を設定しようとした
	@Test
	public void testBmsContent_WithConverter_WrongChangeBpm() {
		var src = new BmsContent(specForConstructorTest());
		var ex = IllegalArgumentException.class;
		src.beginEdit();
		src.setIndexedMeta("#ibpm", 100, 130.0);
		src.endEdit();
		assertThrows(ex, () -> new BmsContent(src, null, m -> m.isContain() ? Math.nextDown(BmsSpec.BPM_MIN) : m.getRawValue(), null, null));
		assertThrows(ex, () -> new BmsContent(src, null, m -> m.isContain() ? Math.nextUp(BmsSpec.BPM_MAX) : m.getRawValue(), null, null));
	}

	// BmsContent(BmsContent, Function, Function, Function, UnaryOperator)
	// IllegalArgumentException メタ情報の変換で、譜面停止時間メタ情報に{@link BmsSpec#STOP_MIN}未満または{@link BmsSpec#STOP_MAX}超過の値を設定しようとした
	@Test
	public void testBmsContent_WithConverter_WrongStop() {
		var src = new BmsContent(specForConstructorTest());
		var ex = IllegalArgumentException.class;
		src.beginEdit();
		src.setIndexedMeta("#istop", 100, 48.0);
		src.endEdit();
		assertThrows(ex, () -> new BmsContent(src, null, m -> m.isContain() ? Math.nextDown(BmsSpec.STOP_MIN) : m.getRawValue(), null, null));
		assertThrows(ex, () -> new BmsContent(src, null, m -> m.isContain() ? Math.nextUp(BmsSpec.STOP_MAX) : m.getRawValue(), null, null));
	}

	// BmsContent(BmsContent, Function, Function, Function, UnaryOperator)
	// IllegalArgumentException 小節データの変換で、小節長に{@link BmsSpec#LENGTH_MIN}未満または{@link BmsSpec#LENGTH_MAX}超過の値を設定しようとした
	@Test
	public void testBmsContent_WithConverter_WrongMeasureLength() {
		var src = new BmsContent(specForConstructorTest());
		var ex = IllegalArgumentException.class;
		src.beginEdit();
		src.setMeasureValue(1, 0, 3, 1.5);
		src.endEdit();
		assertThrows(ex, () -> new BmsContent(src, null, null, v -> Math.nextDown(BmsSpec.LENGTH_MIN), null));
		assertThrows(ex, () -> new BmsContent(src, null, null, v -> Math.nextUp(BmsSpec.LENGTH_MAX), null));
	}

	// BmsContent(BmsContent, Function, Function, Function, UnaryOperator)
	// IllegalArgumentException ノートの変換で、{@link BmsSpec#CHANNEL_MIN}未満または{@link BmsSpec#CHANNEL_MAX}超過のチャンネル番号を返した
	@Test
	public void testBmsContent_WithConverter_WrongNoteChannel() {
		var src = new BmsContent(specForConstructorTest());
		var ex = IllegalArgumentException.class;
		src.beginEdit();
		src.putNote(6, 0, 0, 0.0, 100);
		src.endEdit();
		assertThrows(ex, () -> new BmsContent(src, null, null, null, n -> n.newNote(BmsSpec.CHANNEL_MIN - 1, 0, 0, 0.0, 100)));
		assertThrows(ex, () -> new BmsContent(src, null, null, null, n -> n.newNote(BmsSpec.CHANNEL_MAX + 1, 0, 0, 0.0, 100)));
	}

	// BmsContent(BmsContent, Function, Function, Function, UnaryOperator)
	// IllegalArgumentException ノートの変換で、BMS仕様に存在しないチャンネル番号を返した
	@Test
	public void testBmsContent_WithConverter_UnknownNoteChannel() {
		var src = new BmsContent(specForConstructorTest());
		var ex = IllegalArgumentException.class;
		src.beginEdit();
		src.putNote(6, 0, 0, 0.0, 100);
		src.endEdit();
		assertThrows(ex, () -> new BmsContent(src, null, null, null, n -> n.newNote(999, 0, 0, 0.0, 100)));
	}

	// BmsContent(BmsContent, Function, Function, Function, UnaryOperator)
	// IllegalArgumentException ノートの変換で、配列型以外のチャンネル番号を返した
	@Test
	public void testBmsContent_WithConverter_NoArrayTypeNoteChannel() {
		var src = new BmsContent(specForConstructorTest());
		var ex = IllegalArgumentException.class;
		src.beginEdit();
		src.putNote(6, 0, 0, 0.0, 100);
		src.endEdit();
		assertThrows(ex, () -> new BmsContent(src, null, null, null, n -> n.newNote(4, 0, 0, 0.0, 100)));
	}

	// BmsContent(BmsContent, Function, Function, Function, UnaryOperator)
	// IndexOutOfBoundsException ノートの変換で、コピー先の重複不可チャンネルへチャンネルインデックスが1以上のノートをコピーしようとした
	@Test
	public void testBmsContent_WithConverter_WrongChIndexNoteChannel() {
		var src = new BmsContent(specForConstructorTest());
		var ex = IndexOutOfBoundsException.class;
		src.beginEdit();
		src.putNote(7, 5, 0, 0.0, 100);
		src.endEdit();
		assertThrows(ex, () -> new BmsContent(src, null, null, null, n ->
				n.newNote(6, n.getChannel(), n.getMeasure(), n.getTick(), n.getValue())));
	}

	// BmsContent(BmsContent, Function, Function, Function, UnaryOperator)
	// IllegalArgumentException ノートの変換で、小節番号が{@link BmsSpec#MEASURE_MIN}未満または{@link BmsSpec#MEASURE_MAX}超過
	@Test
	public void testBmsContent_WithConverter_WrongNoteMeasure() {
		var src = new BmsContent(specForConstructorTest());
		var ex = IllegalArgumentException.class;
		src.beginEdit();
		src.putNote(6, 0, 0, 0.0, 100);
		src.endEdit();
		assertThrows(ex, () -> new BmsContent(src, null, null, null, n -> n.newNote(6, 0, BmsSpec.MEASURE_MIN - 1, 0.0, 100)));
		assertThrows(ex, () -> new BmsContent(src, null, null, null, n -> n.newNote(6, 0, BmsSpec.MEASURE_MAX + 1, 0.0, 100)));
	}

	// BmsContent(BmsContent, Function, Function, Function, UnaryOperator)
	// IllegalArgumentException ノートの変換で、小節の刻み位置がマイナス値または当該小節の刻み数以上
	@Test
	public void testBmsContent_WithConverter_WrongNoteTick() {
		var src = new BmsContent(specForConstructorTest());
		var ex = IllegalArgumentException.class;
		src.beginEdit();
		src.putNote(6, 0, 0, 0.0, 100);
		src.endEdit();
		assertThrows(ex, () -> new BmsContent(src, null, null, null, n -> n.newNote(6, 0, 0, Math.nextDown(0.0), 100)));
		assertThrows(ex, () -> new BmsContent(src, null, null, null, n -> n.newNote(6, 0, 0, Math.nextUp(BmsSpec.TICK_COUNT_DEFAULT), 100)));
	}

	// BmsContent(BmsContent, Function, Function, Function, UnaryOperator)
	// IllegalArgumentException ノートの変換で、ノートの値に0を指定した
	@Test
	public void testBmsContent_WithConverter_ZeroNoteValue() {
		var src = new BmsContent(specForConstructorTest());
		var ex = IllegalArgumentException.class;
		src.beginEdit();
		src.putNote(6, 0, 0, 0.0, 100);
		src.endEdit();
		assertThrows(ex, () -> new BmsContent(src, null, null, null, n -> n.newNote(6, 0, 0, 0.0, 0)));
	}

	// BmsContent(BmsContent, Function, Function, Function, UnaryOperator)
	// IllegalArgumentException ノートの変換で、ノートの値に{@link BmsSpec#VALUE_MIN}未満または{@link BmsSpec#VALUE_MAX}超過の値を指定した
	@Test
	public void testBmsContent_WithConverter_WrongNoteValue() {
		var src = new BmsContent(specForConstructorTest());
		src.beginEdit();
		src.putNote(6, 0, 0, 0.0, 100);
		src.endEdit();
		// ノートの値の最小値・最大値がそれぞれint型の最小値・最大値を示しているためテスト不可
		//var ex = IllegalArgumentException.class;
		//assertThrows(ex, () -> new BmsContent(src, null, null, null, n -> n.newInstance(6, 0, 0, 0.0, BmsSpec.VALUE_MIN - 1)));
		//assertThrows(ex, () -> new BmsContent(src, null, null, null, n -> n.newInstance(6, 0, 0, 0.0, BmsSpec.VALUE_MAX + 1)));
	}

	// getSpec()
	@Test
	public void testGetSpec() {
		var s = spec();
		var c = new BmsContent(s);
		assertEquals(s, c.getSpec());
	}

	// getMinBpm()
	// BmsContent生成直後は#BPMの初期値と同じ値を返すこと
	@Test
	public void testGetMinBpm_001() {
		var c = createTestBpmContent(true, true);
		assertEquals((double)c.getSpec().getInitialBpmMeta().getDefaultValue(), c.getMinBpm(), 0.0);
	}

	// getMinBpm()
	// BPM変更チャンネルがない場合は初期BPMと同じ値を返すこと
	@Test
	public void testGetMinBpm_002() {
		var c = createTestBpmContent(false, false);
		assertEquals((double)c.getSpec().getInitialBpmMeta().getDefaultValue(), c.getMinBpm(), 0.0);
	}

	// getMinBpm()
	// BPM変更チャンネルにノートがない場合は初期BPMと同じ値を返すこと
	@Test
	public void testGetMinBpm_003() {
		var c = createTestBpmContent(true, true);
		c.beginEdit();
		// 何の編集も行わない
		c.endEdit();
		assertEquals((double)c.getSpec().getInitialBpmMeta().getDefaultValue(), c.getMinBpm(), 0.0);
	}

	// getMinBpm()
	// BPM変更チャンネルのノートが示す#BPMxxがあり、その値が初期BPMより大きい場合は初期BPMと同じ値を返すこと（ノート1個でテスト）
	@Test
	public void testGetMinBpm_004() {
		var c = createTestBpmContent(true, true);
		c.beginEdit();
		c.setInitialBpm(150.0);
		c.setIndexedMeta("#bpm", 1, 200.0);
		c.putNote(1, 2, 0.0, 1);  // 2小節目でBPM200に変更
		c.endEdit();
		assertEquals(150.0, c.getMinBpm(), 0.0);
	}

	// getMinBpm()
	// BPM変更チャンネルのノートが示す#BPMxxがあり、その値が初期BPMより小さい場合は#BPMxxの値を返すこと（ノート1個でテスト）
	@Test
	public void testGetMinBpm_005() {
		var c = createTestBpmContent(true, true);
		c.beginEdit();
		c.setInitialBpm(150.0);
		c.setIndexedMeta("#bpm", 1, 100.0);
		c.putNote(1, 2, 0.0, 1);  // 2小節目でBPM100に変更
		c.endEdit();
		assertEquals(100.0, c.getMinBpm(), 0.0);
	}

	// getMinBpm()
	// BPM変更チャンネルのノートが示す#BPMxxがない場合は初期BPMと同じ値を返すこと（ノート1個でテスト）
	@Test
	public void testGetMinBpm_006() {
		var c = createTestBpmContent(true, true);
		c.beginEdit();
		c.setInitialBpm(150.0);
		c.setIndexedMeta("#bpm", 1, 200.0);
		c.putNote(1, 2, 0.0, 999);  // 2小節目で存在しないメタ情報を指定
		c.endEdit();
		assertEquals(150.0, c.getMinBpm(), 0.0);
	}

	// getMinBpm()
	// BPM変更チャンネルに複数のノートがある場合、その中で最小のBPMを返すこと（初期BPMより小さい値を複数セットしてテスト）
	@Test
	public void testGetMinBpm_007() {
		var c = createTestBpmContent(true, true);
		c.beginEdit();
		c.setInitialBpm(150.0);
		c.setIndexedMeta("#bpm", 10, 120.0);
		c.setIndexedMeta("#bpm", 20, 140.0);
		c.setIndexedMeta("#bpm", 30, 160.0);
		c.setIndexedMeta("#bpm", 40, 180.0);
		c.putNote(1, 2, 0.0, 40);  // 2小節目でBPM180に変更
		c.putNote(1, 4, 0.0, 30);  // 4小節目でBPM160に変更
		c.putNote(1, 6, 0.0, 20);  // 6小節目でBPM140に変更
		c.putNote(1, 8, 0.0, 10);  // 8小節目でBPM120に変更
		c.endEdit();
		assertEquals(120.0, c.getMinBpm(), 0.0);
	}

	// getMinBpm()
	// IllegalStateException 動作モードが参照モードではない
	@Test
	public void testGetMinBpm_008() {
		var c = createTestBpmContent(true, true);
		c.beginEdit();
		assertThrows(IllegalStateException.class, () -> c.getMinBpm());
	}

	// getMaxBpm()
	// BmsContent生成直後は#BPMの初期値と同じ値を返すこと
	@Test
	public void testGetMaxBpm_001() {
		var c = createTestBpmContent(true, true);
		assertEquals((double)c.getSpec().getInitialBpmMeta().getDefaultValue(), c.getMaxBpm(), 0.0);
	}

	// getMaxBpm()
	// BPM変更チャンネルがない場合は初期BPMと同じ値を返すこと
	@Test
	public void testGetMaxBpm_002() {
		var c = createTestBpmContent(false, false);
		assertEquals((double)c.getSpec().getInitialBpmMeta().getDefaultValue(), c.getMaxBpm(), 0.0);
	}

	// getMaxBpm()
	// BPM変更チャンネルにノートがない場合は初期BPMと同じ値を返すこと
	@Test
	public void testGetMaxBpm_003() {
		var c = createTestBpmContent(true, true);
		c.beginEdit();
		// 何の編集も行わない
		c.endEdit();
		assertEquals((double)c.getSpec().getInitialBpmMeta().getDefaultValue(), c.getMaxBpm(), 0.0);
	}

	// getMaxBpm()
	// BPM変更チャンネルのノートが示す#BPMxxがあり、その値が初期BPMより小さい場合は初期BPMと同じ値を返すこと（ノート1個でテスト）
	@Test
	public void testGetMaxBpm_004() {
		var c = createTestBpmContent(true, true);
		c.beginEdit();
		c.setInitialBpm(150.0);
		c.setIndexedMeta("#bpm", 1, 100.0);
		c.putNote(1, 2, 0.0, 1);  // 2小節目でBPM100に変更
		c.endEdit();
		assertEquals(150.0, c.getMaxBpm(), 0.0);
	}

	// getMaxBpm()
	// BPM変更チャンネルのノートが示す#BPMxxがあり、その値が初期BPMより大きい場合は#BPMxxの値を返すこと（ノート1個でテスト）
	@Test
	public void testGetMaxBpm_005() {
		var c = createTestBpmContent(true, true);
		c.beginEdit();
		c.setInitialBpm(150.0);
		c.setIndexedMeta("#bpm", 1, 200.0);
		c.putNote(1, 2, 0.0, 1);  // 2小節目でBPM200に変更
		c.endEdit();
		assertEquals(200.0, c.getMaxBpm(), 0.0);
	}

	// getMaxBpm()
	// BPM変更チャンネルのノートが示す#BPMxxがない場合は初期BPMと同じ値を返すこと（ノート1個でテスト）
	@Test
	public void testGetMaxBpm_006() {
		var c = createTestBpmContent(true, true);
		c.beginEdit();
		c.setInitialBpm(150.0);
		c.setIndexedMeta("#bpm", 1, 200.0);
		c.putNote(1, 2, 0.0, 999);  // 2小節目で存在しないメタ情報を指定
		c.endEdit();
		assertEquals(150.0, c.getMaxBpm(), 0.0);
	}

	// getMaxBpm()
	// BPM変更チャンネルに複数のノートがある場合、その中で最大のBPMを返すこと（初期BPMより大きい値を複数セットしてテスト）
	@Test
	public void testGetMaxBpm_007() {
		var c = createTestBpmContent(true, true);
		c.beginEdit();
		c.setInitialBpm(150.0);
		c.setIndexedMeta("#bpm", 10, 120.0);
		c.setIndexedMeta("#bpm", 20, 140.0);
		c.setIndexedMeta("#bpm", 30, 160.0);
		c.setIndexedMeta("#bpm", 40, 180.0);
		c.putNote(1, 2, 0.0, 40);  // 2小節目でBPM180に変更
		c.putNote(1, 4, 0.0, 30);  // 4小節目でBPM160に変更
		c.putNote(1, 6, 0.0, 20);  // 6小節目でBPM140に変更
		c.putNote(1, 8, 0.0, 10);  // 8小節目でBPM120に変更
		c.endEdit();
		assertEquals(180.0, c.getMaxBpm(), 0.0);
	}

	// getMaxBpm()
	// IllegalStateException 動作モードが参照モードではない
	@Test
	public void testGetMaxBpm_008() {
		var c = createTestBpmContent(true, true);
		c.beginEdit();
		assertThrows(IllegalStateException.class, () -> c.getMaxBpm());
	}

	// getChangeBpmCount()
	// BmsContent生成直後は0を返すこと
	@Test
	public void testGetChangeBpmCount_001() {
		var c = createTestBpmContent(true, true);
		assertEquals(0L, c.getChangeBpmCount());
	}

	// getChangeBpmCount()
	// BPM変更チャンネルがない場合は0を返すこと
	@Test
	public void testGetChangeBpmCount_002() {
		var c = createTestBpmContent(false, false);
		assertEquals(0L, c.getChangeBpmCount());
	}

	// getChangeBpmCount()
	// BPM変更チャンネルに参照先メタ情報がない場合、変更後BPMが変更前BPMの値と同じでも、それらが変更回数としてカウントされること
	@Test
	public void testGetChangeBpmCount_003() {
		var c = createTestBpmContent(true, false);
		c.beginEdit();
		c.putNote(1, 1, 0.0, 120);  // 1小節1拍目でBPMを120に変更
		c.putNote(1, 1, 48.0, 160);  // 1小節2拍目でBPMを160に変更
		c.putNote(1, 1, 96.0, 140);  // 1小節3拍目でBPMを140に変更
		c.putNote(1, 1, 144.0, 140);  // 1小節4拍目でBPMを140に変更（実際には変化はないが、カウントされる）
		c.putNote(1, 2, 0.0, 180);  // 2小節1拍目でBPMを180に変更
		c.putNote(1, 2, 48.0, 180);  // 2小節2拍目でBPMを180に変更（実際には変化はないが、カウントされる）
		c.endEdit();
		assertEquals(6L, c.getChangeBpmCount());
	}

	// getChangeBpmCount()
	// BPM変更チャンネルに参照先メタ情報がある場合で、参照先メタ情報未存在の値を示すノートはカウントされないこと
	@Test
	public void testGetChangeBpmCount_004() {
		var c = createTestBpmContent(true, true);
		c.beginEdit();
		c.setIndexedMeta("#bpm", 10, 80.0);
		c.setIndexedMeta("#bpm", 20, 120.0);
		c.setIndexedMeta("#bpm", 30, 160.0);
		c.setIndexedMeta("#bpm", 40, 200.0);
		c.putNote(1, 1, 0.0, 10);  // 1小節目でBPMを80に変更
		c.putNote(1, 2, 0.0, 20);  // 2小節目でBPMを120に変更
		c.putNote(1, 3, 0.0, 99);  // 3小節目でBPMを変更しようとする（参照先なしのためカウントされない）
		c.putNote(1, 4, 0.0, 30);  // 4小節目でBPMを160に変更
		c.putNote(1, 5, 0.0, 40);  // 5小節目でBPMを200に変更
		c.endEdit();
		assertEquals(4L, c.getChangeBpmCount());
	}

	// getChangeBpmCount()
	// BPM変更チャンネルに参照先メタ情報がある場合で、変更後BPMが変更前BPMの値と同じでも、それらが変更回数としてカウントされること
	@Test
	public void testGetChangeBpmCount_005() {
		var c = createTestBpmContent(true, true);
		c.beginEdit();
		c.setIndexedMeta("#bpm", 10, 80.0);
		c.setIndexedMeta("#bpm", 20, 120.0);
		c.setIndexedMeta("#bpm", 30, 160.0);
		c.setIndexedMeta("#bpm", 40, 200.0);
		c.putNote(1, 1, 0.0, 10);  // 1小節目でBPMを80に変更
		c.putNote(1, 2, 0.0, 20);  // 2小節目でBPMを120に変更
		c.putNote(1, 3, 0.0, 20);  // 3小節目でBPMを120に変更（実際には変化はないが、カウントされる）
		c.putNote(1, 4, 0.0, 30);  // 4小節目でBPMを160に変更
		c.putNote(1, 5, 0.0, 40);  // 5小節目でBPMを200に変更
		c.putNote(1, 6, 0.0, 40);  // 5小節目でBPMを200に変更（実際には変化はないが、カウントされる）
		c.endEdit();
		assertEquals(6L, c.getChangeBpmCount());
	}

	// getChangeBpmCount()
	// IllegalStateException 動作モードが参照モードではない
	@Test
	public void testGetChangeBpmCount_006() {
		var c = createTestBpmContent(true, true);
		c.beginEdit();
		assertThrows(IllegalStateException.class, () -> c.getChangeBpmCount());
	}

	// getStopCount()
	// BmsContent生成直後は0を返すこと
	@Test
	public void testGetStopCount_001() {
		var c = createTestStopContent(true, true);
		assertEquals(0L, c.getStopCount());
	}

	// getStopCount()
	// 譜面停止チャンネルがない場合は0を返すこと
	@Test
	public void testGetStopCount_002() {
		var c = createTestStopContent(false, false);
		assertEquals(0L, c.getStopCount());
	}

	// getStopCount()
	// 譜面停止チャンネルに参照先メタ情報がある場合で、参照先メタ情報の示す値が0のノートはカウントされないこと
	@Test
	public void testGetStopCount_003() {
		var c = createTestStopContent(true, true);
		c.beginEdit();
		c.setIndexedMeta("#stop", 10, 0L);
		c.setIndexedMeta("#stop", 20, 48L);
		c.setIndexedMeta("#stop", 30, 96L);
		c.putNote(1, 1, 0.0, 20);  // 1小節目で1拍停止
		c.putNote(1, 2, 0.0, 20);  // 2小節目で1拍停止
		c.putNote(1, 3, 0.0, 10);  // 3小節目で0拍停止（カウント対象外）
		c.putNote(1, 4, 0.0, 30);  // 4小節目で2拍停止
		c.endEdit();
		assertEquals(3L, c.getStopCount());
	}

	// getStopCount()
	// 譜面停止チャンネルに参照先メタ情報がある場合で、参照先メタ情報未存在の値を示すノートはカウントされないこと
	@Test
	public void testGetStopCount_004() {
		var c = createTestStopContent(true, true);
		c.beginEdit();
		c.setIndexedMeta("#stop", 10, 48L);
		c.setIndexedMeta("#stop", 20, 96L);
		c.setIndexedMeta("#stop", 30, 192L);
		c.putNote(1, 1, 0.0, 10);  // 1小節目で1拍停止
		c.putNote(1, 2, 0.0, 20);  // 2小節目で21拍停止
		c.putNote(1, 3, 0.0, 30);  // 3小節目で4拍停止
		c.putNote(1, 4, 0.0, 99);  // 4小節目で停止しようとする（参照先なしのためカウントされない）
		c.putNote(1, 5, 0.0, 30);  // 5小節目で4拍停止
		c.endEdit();
		assertEquals(4L, c.getStopCount());
	}

	// getStopCount()
	// IllegalStateException 動作モードが参照モードではない
	@Test
	public void testGetStopCount_005() {
		var c = createTestBpmContent(true, true);
		c.beginEdit();
		assertThrows(IllegalStateException.class, () -> c.getStopCount());
	}

	// beginEdit()
	// 正常
	@Test
	public void testBeginEdit_001() throws Exception {
		var c = new BmsContent(spec());
		c.beginEdit();
		assertTrue((boolean)Tests.getf(c, "mIsEditMode"));
	}

	// beginEdit()
	// IllegalStateException 動作モードが参照モードではない
	@Test
	public void testBeginEdit_002() {
		var c = new BmsContent(spec());
		c.beginEdit();
		assertThrows(IllegalStateException.class, () -> c.beginEdit());
	}

	private static class BeginEdit003Content extends BmsContent {
		boolean mCalled = false;
		BeginEdit003Content(BmsSpec spec) { super(spec); }
		@Override protected void onBeginEdit() { mCalled = true; }
	}

	// beginEdit()
	// beginEdit()を呼ぶとonBeginEdit()が呼ばれること
	@Test
	public void testBeginEdit_003() {
		var c = new BeginEdit003Content(spec());
		c.beginEdit();
		assertTrue(c.mCalled);
	}

	private static class BeginEdit004Content extends BmsContent {
		BeginEdit004Content(BmsSpec spec) { super(spec); }
		@Override protected void onBeginEdit() {
			beginEdit();
			endEdit();
		}
	}

	// beginEdit()
	// onBeginEdit()実行中はbeginEdit(), endEdit()を呼んでも何も起きないこと
	@Test
	public void testBeginEdit_004() {
		var c = new BeginEdit004Content(spec());
		c.beginEdit();
	}

	private static class BeginEdit005Content extends BmsContent {
		BeginEdit005Content(BmsSpec spec) { super(spec); }
		@Override protected void onBeginEdit() { assertTrue(isEditMode()); }
	}

	// beginEdit()
	// onBeginEdit()呼び出し時点では編集モードになっていること
	@Test
	public void testBeginEdit_005() {
		var c = new BeginEdit005Content(spec());
		c.beginEdit();
	}

	private static class BeginEdit006Content extends BmsContent {
		BeginEdit006Content(BmsSpec spec) { super(spec); }
		@Override protected void onBeginEdit() { throw new RuntimeException("EXCEPTION"); }
	}

	// beginEdit()
	// onBeginEdit()で例外をスローしても編集モードになっていること
	@Test
	public void testBeginEdit_006() {
		var c = new BeginEdit006Content(spec());
		assertThrows(RuntimeException.class, () -> c.beginEdit());
		assertTrue(c.isEditMode());
	}

	// endEdit()
	// 正常
	@Test
	public void testEndEdit_001() throws Exception {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.endEdit();
		assertFalse((boolean)Tests.getf(c, "mIsEditMode"));
	}

	// endEdit()
	// IllegalStateException 動作モードが編集モードではない
	@Test
	public void testEndEdit_002() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.endEdit();
		assertThrows(IllegalStateException.class, () -> c.endEdit());
	}

	private static class EndEdit003Content extends BmsContent {
		boolean mCalled = false;
		EndEdit003Content(BmsSpec spec) { super(spec); }
		@Override protected void onEndEdit(boolean isRecalculateTime) { mCalled = true; }
	}

	// endEdit()
	// endEdit()を呼ぶとonEndEdit()が呼ばれること
	@Test
	public void testEndEdit_003() {
		var c = new EndEdit003Content(spec());
		c.beginEdit();
		c.endEdit();
		assertTrue(c.mCalled);
	}

	private static class EndEdit004Content extends BmsContent {
		EndEdit004Content(BmsSpec spec) { super(spec); }
		@Override protected void onEndEdit(boolean isRecalculateTime) {
			beginEdit();
			endEdit();
		}
	}

	// endEdit()
	// onEndEdit()実行中はbeginEdit(), endEdit()を呼んでも何も起きないこと
	@Test
	public void testEndEdit_004() {
		var c = new EndEdit004Content(spec());
		c.beginEdit();
		c.endEdit();
	}

	private static class EndEdit005Content extends BmsContent {
		EndEdit005Content(BmsSpec spec) { super(spec); }
		@Override protected void onEndEdit(boolean isRecalculateTime) { assertTrue(isReferenceMode()); }
	}

	// endEdit()
	// onEndEdit()呼び出し時点では参照モードになっていること
	@Test
	public void testEndEdit_005() {
		var c = new EndEdit005Content(spec());
		c.beginEdit();
		c.endEdit();
	}

	private static class EndEdit006Content extends BmsContent {
		EndEdit006Content(BmsSpec spec) { super(spec); }
		@Override protected void onEndEdit(boolean isRecalculateTime) { assertFalse(isRecalculateTime); }
	}

	// endEdit()
	// 時間・BPMに関する情報を変更しない場合isRecalculateTimeがfalseになること
	@Test
	public void testEndEdit_006() {
		var c = new EndEdit006Content(spec());
		c.beginEdit();
		c.endEdit();
	}

	private static class EndEdit007Content extends BmsContent {
		boolean recalculateTime;
		EndEdit007Content(BmsSpec spec) { super(spec); }
		@Override protected void onEndEdit(boolean isRecalculateTime) { assertTrue(isRecalculateTime == recalculateTime); }
	}

	// endEdit()
	// 時間・BPMに関する情報を変更するとisRecalculateTimeがtrueになること
	@Test
	public void testEndEdit_007() {
		var c = new EndEdit007Content(spec());
		// 小節数が拡張されるため再計算対象になる
		c.beginEdit();
		c.putNote(11, 0, 999, 0, Integer.valueOf("ZZ", 36));
		c.recalculateTime = true;
		c.endEdit();
		// 小節数が拡張されず時間・BPMに関連しないチャンネルの更新のため再計算対象外
		c.beginEdit();
		c.putNote(11, 0, 1, 0, Integer.valueOf("AA", 36));
		c.recalculateTime = false;
		c.endEdit();
		// 小節長
		c.beginEdit();
		c.setMeasureValue(2, 10, 2.0);
		c.recalculateTime = true;
		c.endEdit();
		// BPM変更
		c.beginEdit();
		c.putNote(8, 20, 0.0, Integer.valueOf("CC", 36));
		c.recalculateTime = true;
		c.endEdit();
		// 譜面停止
		c.beginEdit();
		c.putNote(9, 30, 0.0, Integer.valueOf("DD", 36));
		c.recalculateTime = true;
		c.endEdit();
	}

	private static class EndEdit008Content extends BmsContent {
		EndEdit008Content(BmsSpec spec) { super(spec); }
		@Override protected void onEndEdit(boolean isRecalculateTime) { throw new RuntimeException("EXCEPTION"); }
	}

	// endEdit()
	// onEndEdit()で例外をスローしても参照モードになっていること
	@Test
	public void testEndEdit_008() {
		var c = new EndEdit008Content(spec());
		c.beginEdit();
		assertThrows(RuntimeException.class, () -> c.endEdit());
		assertTrue(c.isReferenceMode());
	}

	// edit(Runnable)
	// 編集モードになってエディタ関数が実行され、その後参照モードに戻ること
	@Test
	public void testEdit_Normal() {
		var c = new ContentForEditTest(spec());
		assertTrue(c.isReferenceMode());
		c.edit(() -> {
			assertTrue(c.isEditMode());
			assertTrue(c.calledOnBeginEdit);
			assertFalse(c.calledOnEndEdit);
			c.setSingleMeta("#title", "MySong");
		});
		assertTrue(c.isReferenceMode());
		assertTrue(c.calledOnEndEdit);
		assertEquals("MySong", c.getSingleMeta("#title"));
	}

	// edit(Runnable)
	// エディタ関数内で例外がスローされると当該例外がスローされる＆参照モードに戻っていること
	@Test
	public void testEdit_ExceptionInEditor() {
		var c = new ContentForEditTest(spec());
		var ex = RuntimeException.class;
		var e = assertThrows(ex, () -> c.edit(() -> { throw new RuntimeException("IN_EDIT"); }));
		assertEquals("IN_EDIT", e.getMessage());
		assertTrue(c.isReferenceMode());
		assertTrue(c.calledOnBeginEdit);
		assertTrue(c.calledOnEndEdit);
	}

	// edit(Runnable)
	// エディタ関数内でError系がスローされると編集モードのままスローされること
	@Test
	public void testEdit_ErrorInEditor() {
		var c = new ContentForEditTest(spec());
		var ex = OutOfMemoryError.class;
		var e = assertThrows(ex, () -> c.edit(() -> { throw new OutOfMemoryError("IN_EDIT"); }));
		assertEquals("IN_EDIT", e.getMessage());
		assertTrue(c.isEditMode());
		assertTrue(c.calledOnBeginEdit);
		assertFalse(c.calledOnEndEdit);
	}

	// edit(Runnable)
	// エディタ関数内で編集を終了しても正常にメソッドが終了し参照モードに戻っていること
	@Test
	public void testEdit_EndEditInEditor() {
		var c = new ContentForEditTest(spec());
		c.edit(() -> c.endEdit());
		assertTrue(c.isReferenceMode());
		assertTrue(c.calledOnBeginEdit);
		assertTrue(c.calledOnEndEdit);
	}

	// edit(Runnable)
	// IllegalStateException 動作モードが参照モードではない
	@Test
	public void testEdit_NotReferenceMode() {
		var c = new BmsContent(spec());
		c.beginEdit();
		assertThrows(IllegalStateException.class, () -> c.edit(() -> {}));
	}

	// edit(Runnable)
	// NullPointerException editorがnull
	@Test
	public void testEdit_NullEditor() {
		var c = new BmsContent(spec());
		assertThrows(NullPointerException.class, () -> c.edit(null));
	}

	// isEditMode()
	// 初期状態ではfalseであること
	@Test
	public void testIsEditMode_001() {
		var c = new BmsContent(spec());
		assertFalse(c.isEditMode());
	}

	// isEditMode()
	// beginEdit()を呼び出すとtrueになること
	@Test
	public void testIsEditMode_002() {
		var c = new BmsContent(spec());
		c.beginEdit();
		assertTrue(c.isEditMode());
	}

	// isEditMode()
	// endEdit()を呼び出すとfalseになること
	@Test
	public void testIsEditMode_003() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.endEdit();
		assertFalse(c.isEditMode());
	}

	// isReferenceMode()
	// 初期状態ではtrueであること
	@Test
	public void testIsReferenceMode_001() {
		var c = new BmsContent(spec());
		assertTrue(c.isReferenceMode());
	}

	// isReferenceMode()
	// beginEdit()を呼び出すとfalseになること
	@Test
	public void testIsReferenceMode_002() {
		var c = new BmsContent(spec());
		c.beginEdit();
		assertFalse(c.isReferenceMode());
	}

	// isReferenceMode()
	// endEdit()を呼び出すとtrueになること
	@Test
	public void testIsReferenceMode_003() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.endEdit();
		assertTrue(c.isReferenceMode());
	}

	private static BmsSpec specForConstructorTest() {
		var builder = new BmsSpecBuilder();
		List.of(
				BmsMeta.single("#sint", BmsType.INTEGER, "1", 0, true),
				BmsMeta.single("#sstr", BmsType.STRING, "", 0, true),
				BmsMeta.single("#sobj", BmsType.OBJECT, null, 0, false),
				BmsMeta.multiple("#mint", BmsType.INTEGER, "1", 0, true),
				BmsMeta.multiple("#mstr", BmsType.STRING, "", 0, true),
				BmsMeta.multiple("#mobj", BmsType.OBJECT, null, 0, false),
				BmsMeta.indexed("#iint", BmsType.INTEGER, "1", 0, true),
				BmsMeta.indexed("#istr", BmsType.STRING, "", 0, true),
				BmsMeta.indexed("#iobj", BmsType.OBJECT, null, 0, false),
				BmsMeta.single("#sbpm", BmsType.FLOAT, "1", 0, true),
				BmsMeta.indexed("#ibpm", BmsType.FLOAT, "1", 0, true),
				BmsMeta.indexed("#istop", BmsType.FLOAT, "1", 0, true)
		).stream().forEach(builder::addMeta);
		List.of(
				BmsChannel.spec(1, BmsType.FLOAT, null, "1", false, true),
				BmsChannel.spec(2, BmsType.ARRAY36, "#ibpm", "", false, true),
				BmsChannel.spec(3, BmsType.ARRAY36, "#istop", "", false, true),
				BmsChannel.spec(4, BmsType.INTEGER, null, "0", false, false),
				BmsChannel.spec(5, BmsType.INTEGER, null, "0", true, false),
				BmsChannel.spec(6, BmsType.ARRAY36, null, "", false, true),
				BmsChannel.spec(7, BmsType.ARRAY36, null, "", true, false),
				BmsChannel.user(BmsSpec.USER_CHANNEL_MIN, BmsType.OBJECT, null, null, false)
		).stream().forEach(builder::addChannel);
		return builder
				.setInitialBpmMeta("#sbpm")
				.setLengthChannel(1)
				.setBpmChannel(2)
				.setStopChannel(3)
				.create();
	}

	private static BmsContent createTestBpmContent(boolean hasChangeBpm, boolean useRef) {
		var spec = new BmsSpecBuilder()
				.addMeta(BmsMeta.single("#bpm", BmsType.FLOAT, "130", 0, true))
				.addMeta(BmsMeta.indexed("#bpm", BmsType.FLOAT, "1.0", 1, true))
				.addChannel(new BmsChannel(1, BmsType.ARRAY36, (useRef ? "#bpm" : null), "", false, true))
				.addChannel(new BmsChannel(2, BmsType.ARRAY36, null, "", false, true))
				.setInitialBpmMeta("#bpm")
				.setBpmChannel(hasChangeBpm ? 1 : null)
				.create();
		return new BmsContent(spec);
	}

	private static BmsContent createTestStopContent(boolean hasStop, boolean useRef) {
		var spec = new BmsSpecBuilder()
				.addMeta(BmsMeta.single("#bpm", BmsType.FLOAT, "130", 0, true))
				.addMeta(BmsMeta.indexed("#stop", BmsType.INTEGER, "0", 1, true))
				.addChannel(new BmsChannel(1, BmsType.ARRAY36, (useRef ? "#stop" : null), "", false, true))
				.addChannel(new BmsChannel(2, BmsType.ARRAY36, null, "", false, true))
				.setInitialBpmMeta("#bpm")
				.setStopChannel(hasStop ? 1 : null)
				.create();
		return new BmsContent(spec);
	}
}
