package com.lmt.lib.bms;

import static org.junit.Assert.*;

import java.util.stream.Collectors;

import org.junit.Test;

public class BmsContentDeclarationTest extends BmsContentTest {
	// putDeclaration(String, String)
	// 正常
	@Test
	public void testPutDeclaration_001() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.putDeclaration("aaa", "value");
		c.putDeclaration("bbb", "secret word");
		c.putDeclaration("bbb", "overrided word");
		c.putDeclaration("BBB", "new word");
		c.putDeclaration("_hoge", "valuehoge");
		c.putDeclaration("zzz3_", "valuechar");
		c.putDeclaration("escape", "aaa\\\"bbb");
		assertEquals("value", c.getDeclaration("aaa"));
		assertEquals("overrided word", c.getDeclaration("bbb"));
		assertEquals("new word", c.getDeclaration("BBB"));
		assertEquals("valuehoge", c.getDeclaration("_hoge"));
		assertEquals("valuechar", c.getDeclaration("zzz3_"));
		assertEquals("aaa\\\"bbb", c.getDeclaration("escape"));
	}

	// putDeclaration(String, String)
	// IllegalStateException 動作モードが編集モードではない
	@Test
	public void testPutDeclaration_002() {
		var c = new BmsContent(spec());
		assertThrows(IllegalStateException.class, () -> c.putDeclaration("hoge", "any"));
	}

	// putDeclaration(String, String)
	// NullPointerException nameがnull
	@Test
	public void testPutDeclaration_003() {
		var c = new BmsContent(spec());
		c.beginEdit();
		assertThrows(NullPointerException.class, () -> c.putDeclaration(null, "any"));
	}

	// putDeclaration(String, String)
	// NullPointerException valueがnull
	@Test
	public void testPutDeclaration_004() {
		var c = new BmsContent(spec());
		c.beginEdit();
		assertThrows(NullPointerException.class, () -> c.putDeclaration("name", null));
	}

	// putDeclaration(String, String)
	// IllegalArgumentException nameの書式が不正
	@Test
	public void testPutDeclaration_005() {
		var c = new BmsContent(spec());
		var ex = IllegalArgumentException.class;
		c.beginEdit();
		assertThrows(ex, () -> c.putDeclaration("", ""));
		assertThrows(ex, () -> c.putDeclaration("#aa", ""));
		assertThrows(ex, () -> c.putDeclaration("1aa", ""));
		assertThrows(ex, () -> c.putDeclaration("aa?", ""));
	}

	// putDeclaration(String, String)
	// IllegalArgumentException valueの書式が不正
	@Test
	public void testPutDeclaration_006() {
		var c = new BmsContent(spec());
		var ex = IllegalArgumentException.class;
		c.beginEdit();
		assertThrows(ex, () -> c.putDeclaration("a", "\\a"));
		assertThrows(ex, () -> c.putDeclaration("a", "\""));
		assertThrows(ex, () -> c.putDeclaration("a", "hoge\\?"));
		assertThrows(ex, () -> c.putDeclaration("a", "hoge\""));
	}

	// putDeclaration(BmsDeclarationElement)
	// BMS宣言を正常に登録できること
	@Test
	public void testPutDeclarationBmsDeclarationElement_Normal() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.putDeclaration(new BmsDeclarationElement("aaa", "value"));
		c.putDeclaration(new BmsDeclarationElement("bbb", "secret word"));
		c.putDeclaration(new BmsDeclarationElement("bbb", "overrided word"));
		c.putDeclaration(new BmsDeclarationElement("BBB", "new word"));
		c.putDeclaration(new BmsDeclarationElement("_hoge", "valuehoge"));
		c.putDeclaration(new BmsDeclarationElement("zzz3_", "valuechar"));
		c.putDeclaration(new BmsDeclarationElement("escape", "aaa\\\"bbb"));
		assertEquals("value", c.getDeclaration("aaa"));
		assertEquals("overrided word", c.getDeclaration("bbb"));
		assertEquals("new word", c.getDeclaration("BBB"));
		assertEquals("valuehoge", c.getDeclaration("_hoge"));
		assertEquals("valuechar", c.getDeclaration("zzz3_"));
		assertEquals("aaa\\\"bbb", c.getDeclaration("escape"));
	}

	// putDeclaration(BmsDeclarationElement)
	// IllegalStateException 動作モードが編集モードではない
	@Test
	public void testPutDeclarationBmsDeclarationElement_NotEditMode() {
		var c = new BmsContent(spec());
		var ex = IllegalStateException.class;
		assertThrows(ex, () -> c.putDeclaration(new BmsDeclarationElement("name", "value")));
	}

	// putDeclaration(BmsDeclarationElement)
	// NullPointerException declarationがnull
	@Test
	public void testPutDeclarationBmsDeclarationElement_NullElement() {
		var c = new BmsContent(spec());
		var ex = NullPointerException.class;
		c.beginEdit();
		assertThrows(ex, () -> c.putDeclaration(null));
	}

	// removeDeclaration(String)
	// 正常
	@Test
	public void testRemoveDeclaration_001() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.putDeclaration("aaa", "hoge");
		c.putDeclaration("_", "hage");
		c.putDeclaration("a012_", "hige");
		assertTrue(c.removeDeclaration("aaa"));
		assertFalse(c.containsDeclaration("aaa"));
		assertFalse(c.removeDeclaration("unknown"));
		assertFalse(c.containsDeclaration("unknown"));
	}

	// removeDeclaration(String)
	// IllegalStateException 動作モードが編集モードではない
	@Test
	public void testRemoveDeclaration_002() {
		var c = new BmsContent(spec());
		assertThrows(IllegalStateException.class, () -> c.removeDeclaration("hoge"));
	}

	// removeDeclaration(String)
	// NullPointerException nameがnull
	@Test
	public void testRemoveDeclaration_003() {
		var c = new BmsContent(spec());
		c.beginEdit();
		assertThrows(NullPointerException.class, () -> c.removeDeclaration(null));
	}

	// containsDeclaration(String)
	// 正常
	@Test
	public void testContainsDeclaration_001() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.putDeclaration("aaa", "hoge");
		c.putDeclaration("bbb", "hage");
		c.putDeclaration("ccc", "hige");
		c.endEdit();
		assertFalse(c.containsDeclaration("aa"));
		assertTrue(c.containsDeclaration("aaa"));
		assertFalse(c.containsDeclaration("aaaa"));
		assertFalse(c.containsDeclaration("!#invalid_char_included#?"));
	}

	// containsDeclaration(String)
	// NullPointerException nameがnull
	@Test
	public void testContainsDeclaration_002() {
		var c = new BmsContent(spec());
		c.beginEdit();
		assertThrows(NullPointerException.class, () -> c.containsDeclaration(null));
	}

	// getDeclaration(String)
	// 正常
	@Test
	public void testGetDeclaration_001() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.putDeclaration("aaa", "hoge");
		c.endEdit();
		assertEquals("hoge", c.getDeclaration("aaa"));
		assertNull(c.getDeclaration("not_found"));
		assertNull(c.getDeclaration("!#invalid_char_included#?"));
	}

	// getDeclaration(String)
	// NullPointerException nameがnull
	@Test
	public void testGetDeclaration_002() {
		var c = new BmsContent(spec());
		c.beginEdit();
		assertThrows(NullPointerException.class, () -> c.getDeclaration(null));
	}

	// getDeclarations()
	@Test
	public void testGetDeclarations_001() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.putDeclaration("aaa", "hoge");
		c.putDeclaration("bbb", "hage");
		c.putDeclaration("ccc", "hige");
		c.endEdit();

		// 追加したデータ内容と一致していること
		var ds = c.getDeclarations();
		assertEquals(3, ds.size());
		assertEquals("hoge", ds.get("aaa"));
		assertEquals("hage", ds.get("bbb"));
		assertEquals("hige", ds.get("ccc"));

		// Mapを消しても元データが消えていないこと
		ds.clear();
		ds = c.getDeclarations();
		assertEquals(3, ds.size());
	}

	// declarations()
	// BMS宣言を追加した順で走査されること
	@Test
	public void testDeclarations_Order() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.putDeclaration("xxx", "hoge");
		c.putDeclaration("bbb", "hage");
		c.putDeclaration("jjj", "hige");
		c.endEdit();
		var decls = c.declarations().collect(Collectors.toList());
		assertEquals(3, decls.size());
		assertEquals("xxx", decls.get(0).getName());
		assertEquals("hoge", decls.get(0).getValue());
		assertEquals("bbb", decls.get(1).getName());
		assertEquals("hage", decls.get(1).getValue());
		assertEquals("jjj", decls.get(2).getName());
		assertEquals("hige", decls.get(2).getValue());
	}

	// declarations()
	// 削除されたBMS宣言は走査されないこと
	@Test
	public void testDeclarations_Remove() {
		var c = new BmsContent(spec());
		c.beginEdit();
		c.putDeclaration("xxx", "hoge");
		c.putDeclaration("bbb", "hage");
		c.putDeclaration("zzz", "hige");
		c.putDeclaration("sss", "hege");
		c.removeDeclaration("bbb");
		c.removeDeclaration("sss");
		c.endEdit();
		var decls = c.declarations().collect(Collectors.toList());
		assertEquals(2, decls.size());
		assertEquals("xxx", decls.get(0).getName());
		assertEquals("hoge", decls.get(0).getValue());
		assertEquals("zzz", decls.get(1).getName());
		assertEquals("hige", decls.get(1).getValue());
	}

	// declarations()
	// BMS宣言が空でも走査がエラーにならないこと
	@Test
	public void testDeclarations_Empty() {
		var c = new BmsContent(spec());
		assertEquals(0L, c.declarations().count());
	}
}
