package com.lmt.lib.bms.parse;

import static org.junit.Assert.*;

import org.junit.Test;

public class BmsSourceTest {
	// BmsSource(byte[])
	// NullPointerException binaryがnull
	@Test
	public void testBmsSource1_NullBinary() {
		assertThrows(NullPointerException.class, () -> new BmsSource((byte[])null));
	}

	// BmsSource(String)
	// NullPointerException scriptがnull
	@Test
	public void testBmsSource2_NullScript() {
		assertThrows(NullPointerException.class, () -> new BmsSource((String)null));
	}

	// getAsBinary()
	// バイナリ指定されて構築されたオブジェクトの場合、指定したバイナリと同じ参照を返すこと
	@Test
	public void testGetAsBinary_FromBinary() {
		var b = new byte[16];
		var s = new BmsSource(b);
		assertSame(b, s.getAsBinary());
	}

	// getAsBinary()
	// テキスト指定されて構築されたオブジェクトの場合、nullを返すこと
	@Test
	public void testGetAsBinary_FromText() {
		var t = "script";
		var s = new BmsSource(t);
		assertNull(s.getAsBinary());
	}

	// getAsScript()
	// バイナリ指定されて構築されたオブジェクトの場合、nullを返すこと
	@Test
	public void testGetAsScript_FromBinary() {
		var b = new byte[16];
		var s = new BmsSource(b);
		assertNull(s.getAsScript());
	}

	// getAsScript()
	// テキスト指定されて構築されたオブジェクトの場合、指定したテキストと同じ参照を返すこと
	@Test
	public void testGetAsScript_FromScript() {
		var t = "script";
		var s = new BmsSource(t);
		assertSame(t, s.getAsScript());
	}
}
