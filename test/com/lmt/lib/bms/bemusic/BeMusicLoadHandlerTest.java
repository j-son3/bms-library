package com.lmt.lib.bms.bemusic;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

import com.lmt.lib.bms.BmsChannel;
import com.lmt.lib.bms.BmsContent;
import com.lmt.lib.bms.BmsErrorType;
import com.lmt.lib.bms.BmsInt;
import com.lmt.lib.bms.BmsLoadException;
import com.lmt.lib.bms.BmsMeta;
import com.lmt.lib.bms.BmsScriptError;
import com.lmt.lib.bms.BmsStandardLoader;
import com.lmt.lib.bms.Tests;
import com.lmt.lib.bms.parse.BmsTestResult;

public class BeMusicLoadHandlerTest {
	private static class TestHandler extends BeMusicLoadHandler {
		boolean occurredError = false;
		List<BmsScriptError> errors = new ArrayList<>();

		TestHandler() {
			setEnableControlFlow(true);
			setForceRandomValue(1L);
		}

		TestHandler(boolean enableCtrlFlow, Long forceRandomValue) {
			setEnableControlFlow(enableCtrlFlow);
			setForceRandomValue(forceRandomValue);
		}

		@Override
		public boolean parseError(BmsScriptError error) {
			occurredError = true;
			errors.add(error);
			return super.parseError(error);
		}

		@Override
		public BmsTestResult testMeta(BmsMeta meta, int index, Object value) {
			return super.testMeta(meta, index, value);
		}

		@Override
		public BmsTestResult testChannel(BmsChannel channel, int index, int measure, Object value) {
			return super.testChannel(channel, index, measure, value);
		}

		@Override
		public BmsTestResult testContent(BmsContent content) {
			return super.testContent(content);
		}
	}

	// setEnableControlFlow(boolean)
	// falseの時、フロー制御のメタ情報が全て無視されること
	@Test
	public void testSetEnableControlFlow_Disable() throws Exception {
		var b = bms(
				"#RANDOM 2",
				"#IF 1",
				"#GENRE Love Song",
				"#ELSEIF 2",
				"#TITLE Can't stop my heart",
				"#ELSE",
				"#ARTIST Mr.X",
				"#ENDIF",
				"#ENDRANDOM");
		var h = new TestHandler(false, null);
		var c = load(h, b);
		assertEquals(false, h.occurredError);
		// フロー制御が効かないので全ての定義が有効になる
		assertEquals("Love Song", BeMusicMeta.getGenre(c));
		assertEquals("Can't stop my heart", BeMusicMeta.getTitle(c));
		assertEquals("Mr.X", BeMusicMeta.getArtist(c));
	}

	// setEnableControlFlow(boolean)
	// trueの時、フロー制御のメタ情報が全て有効になること
	@Test
	public void testSetEnableControlFlow_Enable() throws Exception {
		var b = bms(
				"#RANDOM 3",
				"#IF 1",
				"#GENRE Love Song",
				"#ELSEIF 2",
				"#TITLE Can't stop my heart",
				"#ELSEIF 3",
				"#ARTIST Mr.X",
				"#ELSE",
				"#COMMENT Hoge",
				"#ENDIF",
				"#ENDRANDOM");
		var h = new TestHandler(true, 3L);
		var c = load(h, b);
		assertEquals(false, h.occurredError);
		assertEquals(BeMusicMeta.GENRE.getDefaultValue(), BeMusicMeta.getGenre(c));
		assertEquals(BeMusicMeta.TITLE.getDefaultValue(), BeMusicMeta.getTitle(c));
		assertEquals("Mr.X", BeMusicMeta.getArtist(c));
		assertEquals(BeMusicMeta.COMMENT.getDefaultValue(), BeMusicMeta.getComment(c));
	}

	// setEnableControlFlow(boolean)
	// 未指定時、フロー制御は無効になっていること
	@Test
	public void testSetEnableControlFlow_Default() throws Exception {
		var h = new BeMusicLoadHandler();
		assertEquals(false, Tests.getf(h, "mRandomEnable"));
	}

	// setForceRandomValue(Long)
	// このメソッドのテストは BeMusic#loadContentFrom にて実施する。

	// withControlFlow()
	// CONTROL FLOWを有効にしたBe-Music用BMSコンテンツ読み込みハンドラが生成されて返ること
	@Test
	public void testWithControlFlow() throws Exception {
		var h = BeMusicLoadHandler.withControlFlow();
		var h2 = BeMusicLoadHandler.withControlFlow();
		assertNotSame(h, h2);
		assertEquals(true, Tests.getf(h, "mRandomEnable"));
		assertNull(Tests.getf(h, "mRandomValueForce"));
	}

	// withControlFlow(long)
	// CONTROL FLOWを有効にしたBe-Music用BMSコンテンツ読み込みハンドラが生成されて返り、指定値で#RANDOMが強制されること
	@Test
	public void testWithControlFlowLong() throws Exception {
		var h = BeMusicLoadHandler.withControlFlow(5);
		var h2 = BeMusicLoadHandler.withControlFlow(5);
		assertNotSame(h, h2);
		assertEquals(true, Tests.getf(h, "mRandomEnable"));
		assertEquals(5L, (long)Tests.getf(h, "mRandomValueForce"));
	}

	// withoutControlFlow()
	// CONTROL FLOWを無効にしたBe-Music用BMSコンテンツ読み込みハンドラが生成されて返ること
	@Test
	public void testWithoutControlFlow() throws Exception {
		var h = BeMusicLoadHandler.withoutControlFlow();
		var h2 = BeMusicLoadHandler.withoutControlFlow();
		assertNotSame(h, h2);
		assertEquals(false, Tests.getf(h, "mRandomEnable"));
		assertNull(Tests.getf(h, "mRandomValueForce"));
	}

	// #ENDRANDOM
	// #ENDIFの終わりに記述してもエラーにならず、新しい#RANDOMを記述できること
	@Test
	public void testEndRandom_NormalTerminate() throws Exception {
		var h = new TestHandler();
		var b = "#RANDOM 3\n" +
				"#IF 1\n" +
				"#TITLE then\n" +
				"#00002:1.0\n" +
				"#00008:01\n" +
				"#ELSE\n" +
				"#TITLE else\n" +
				"#00002:2.0\n" +
				"#00008:02\n" +
				"#ENDIF\n" +
				"#ENDRANDOM\n" +
				"#RANDOM 5\n" +
				"#IF 2\n" +
				"#ELSE\n" +
				"#ARTIST artist\n" +
				"#ENDIF";
		var c = load(h, b);
		assertFalse(h.occurredError);
		assertEquals("then", BeMusicMeta.getTitle(c));
		assertEquals(1.0, c.getMeasureValue(BmsInt.to36i("02"), 0), 0.0);
		assertEquals(1, c.getNote(BmsInt.to36i("08"), 0, 0.0).getValue());
		assertEquals("artist", BeMusicMeta.getArtist(c));
	}

	// #ENDRANDOM
	// #RANDOMなしで記述でき、記述しても作用がないこと
	@Test
	public void testEndRandom_NoRandom() throws Exception {
		var h = new TestHandler();
		var b = "#ENDRANDOM\n" +
				"#TITLE title";
		var c = load(h, b);
		assertFalse(h.occurredError);
		assertEquals("title", BeMusicMeta.getTitle(c));
	}

	// #ENDRANDOM
	// #IFブロックの中で記述するとエラーになること
	@Test
	public void testEndRandom_InIf() throws Exception {
		var h = new TestHandler();
		var b = "#RANDOM 3\n" +
				"#IF 1\n" +
				"#ENDRANDOM\n" +
				"#ELSE\n" +
				"#ENDIF";
		var e = assertThrows(BmsLoadException.class, () -> load(h, b));
		assertEquals(BmsErrorType.TEST_META, e.getError().getType());
		assertTrue(h.occurredError);
		assertEquals(1, h.errors.size());
		assertEquals(BmsErrorType.TEST_META, h.errors.get(0).getType());
		assertEquals(3, h.errors.get(0).getLineNumber());
	}

	// #ENDRANDOM
	// #ELSEIFブロックの中で記述するとエラーになること
	@Test
	public void testEndRandom_InElseIf() throws Exception {
		var h = new TestHandler();
		var b = "#RANDOM 3\n" +
				"#IF 2\n" +
				"#ELSEIF 1\n" +
				"#ENDRANDOM\n" +
				"#ELSE\n" +
				"#ENDIF";
		var e = assertThrows(BmsLoadException.class, () -> load(h, b));
		assertEquals(BmsErrorType.TEST_META, e.getError().getType());
		assertTrue(h.occurredError);
		assertEquals(1, h.errors.size());
		assertEquals(BmsErrorType.TEST_META, h.errors.get(0).getType());
		assertEquals(4, h.errors.get(0).getLineNumber());
	}

	// #ENDRANDOM
	// #ELSEブロックの中で記述するとエラーになること
	@Test
	public void testEndRandom_InElse() throws Exception {
		var h = new TestHandler();
		var b = "#RANDOM 3\n" +
				"#IF 2\n" +
				"#ELSE\n" +
				"#ENDRANDOM\n" +
				"#ENDIF";
		var e = assertThrows(BmsLoadException.class, () -> load(h, b));
		assertEquals(BmsErrorType.TEST_META, e.getError().getType());
		assertTrue(h.occurredError);
		assertEquals(1, h.errors.size());
		assertEquals(BmsErrorType.TEST_META, h.errors.get(0).getType());
		assertEquals(4, h.errors.get(0).getLineNumber());
	}

	// #ENDRANDOM
	// #RANDOM直後に記述すると当該乱数が打ち消され、その後の#IFは成立せず必ず#ELSEに入ること
	@Test
	public void testEndRandom_RightAfterRandom() throws Exception {
		var h = new TestHandler();
		var b = "#RANDOM 3\n" +
				"#ENDRANDOM\n" +
				"#IF 1\n" +
				"#TITLE fail\n" +
				"#ELSE\n" +
				"#TITLE ok\n" +
				"#ENDIF";
		var c = load(h, b);
		assertFalse(h.occurredError);
		assertEquals("ok", BeMusicMeta.getTitle(c));
	}

	private static String bms(String...lines) {
		return Stream.of(lines).collect(Collectors.joining("\n"));
	}

	private static BmsContent load(BeMusicLoadHandler handler, String bms) throws Exception {
		return new BmsStandardLoader()
				.setSpec(BeMusicSpec.LATEST)
				.setHandler(handler)
				.setStrictly(true)
				.load(bms);
	}
}
