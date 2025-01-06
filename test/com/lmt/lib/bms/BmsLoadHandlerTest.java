package com.lmt.lib.bms;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;

import com.lmt.lib.bms.parse.BmsTestResult;

public class BmsLoadHandlerTest {
	// createContent(BmsSpec)
	// 返却したBmsContentと同じオブジェクト参照がload()の戻り値として返却されること
	@Test
	public void testCreateContent_001() throws Exception {
		var content = new BmsContent[] { null };
		var loadedContent = testHandler(new BmsLoadHandler() {
			@Override public BmsContent createContent(BmsSpec spec) {
				content[0] = new BmsContent(spec);
				return content[0];
			}
		}, "");
		assertSame(loadedContent, content[0]);
	}

	// createContent(BmsSpec)
	// I/F内で例外をスローするとBmsExceptionがスローされ、その中にI/Fでスローした例外が内包されていること
	@Test
	public void testCreateContent_002() throws Exception {
		var e = assertThrows(BmsException.class, () -> {
			testHandler(new BmsLoadHandler() {
				@Override public BmsContent createContent(BmsSpec spec) {
					throw new RuntimeException("EXCEPTION");
				}
			}, "");
		});
		assertEquals(RuntimeException.class, e.getCause().getClass());
	}

	// createContent(BmsSpec)
	// パラメータで渡されたBmsSpec以外を指定したBmsContentを返すと、BmsExceptionがスローされること
	@Test
	public void testCreateContent_003() throws Exception {
		assertThrows(BmsException.class, () -> {
			testHandler(new BmsLoadHandler() {
				@Override public BmsContent createContent(BmsSpec spec) {
					return new BmsContent(BmsTest.createTestSpec());
				}
			}, "");
		});
	}

	// createNote()
	// 返却したBmsNoteオブジェクトがBMSコンテンツ内に格納されていること
	@Test
	public void testCreateNote_001() throws Exception {
		var tempNote = new BmsNote[] { null };
		var content = testHandler(new BmsLoadHandler() {
			@Override public BmsNote createNote() {
				assertNull(tempNote[0]);  // 二度呼ばれたらテスト失敗
				tempNote[0] = new BmsNote();
				return tempNote[0];
			}
		}, "#00007:AB");
		var note = content.getNote(7, 0, 0);
		assertNotNull(note);
		assertSame(tempNote[0], note);
	}

	private static class BmsNote4TestCreateNote002 extends BmsNote {
		boolean called = false;
		@Override protected void onCreate() {
			assertFalse(called);
			called = true;
		}
	}

	// createNote()
	// 返却したBmsNoteオブジェクトでonCreateが呼び出されること
	@Test
	public void testCreateNote_002() throws Exception {
		var content = testHandler(new BmsLoadHandler() {
			@Override public BmsNote createNote() {
				return new BmsNote4TestCreateNote002();
			}
		}, "#00007:01");

		var note = (BmsNote4TestCreateNote002)content.getNote(7, 0, 0);
		assertNotNull(note);
		assertTrue(note.called);
	}

	// createNote()
	// I/F内で例外をスローするとBmsExceptionがスローされ、その中にI/Fでスローした例外が内包されていること
	@Test
	public void testCreateNote_003() throws Exception {
		var e = assertThrows(BmsException.class, () -> {
			testHandler(new BmsLoadHandler() {
				@Override public BmsNote createNote() {
					throw new RuntimeException("EXCEPTION");
				}
			}, "#00007:01");
		});
		assertEquals(RuntimeException.class, e.getCause().getClass());
	}

	// createNote()
	// nullを返すとBmsExceptionがスローされること
	@Test
	public void testCreateNote_004() throws Exception {
		assertThrows(BmsException.class, () -> {
			var bms =
					"#00001:AB";
			testHandler(new BmsLoadHandler() {
				@Override public BmsNote createNote() {
					return null;
				}
			}, bms);
		});
	}

	// startLoad(BmsLoaderSettings)
	// BmsLoaderにBmsSpecが未指定だとI/Fは呼び出されないこと
	@Test
	public void testStartLoad_001() throws Exception {
		var called = new boolean[] { false };
		assertThrows(IllegalStateException.class, () -> {
			new BmsStandardLoader().setHandler(new BmsLoadHandler() {
				@Override public void startLoad(BmsLoaderSettings spec) {
					called[0] = true;
				}
			}).load("");
		});
		assertFalse(called[0]);
	}

	// startLoad(BmsLoaderSettings)
	// BmsLoaderに指定したBmsSpecと同じ参照がパラメータで渡されていること
	@Test
	public void testStartLoad_002() throws Exception {
		var called = new boolean[] { false };
		var specifiedSpec = BmsTest.createTestSpec();
		new BmsStandardLoader().setSpec(specifiedSpec).setHandler(new BmsLoadHandler() {
			@Override public void startLoad(BmsLoaderSettings settings) {
				assertNotNull(settings);
				assertSame(specifiedSpec, settings.getSpec());
				called[0] = true;
			}
		}).load("");
		assertTrue(called[0]);
	}

	// startLoad(BmsLoaderSettings)
	// 全てのI/Fの中で、最初に呼ばれること
	@Test
	public void testStartLoad_003() throws Exception {
		var called = new boolean[] { false };
		var bms =
				";?bms key=\"value\"\n" +
				"#SINTEGER 100\n" +
				"#00007:01";
		testHandler(new BmsLoadHandler() {
			@Override public BmsContent createContent(BmsSpec spec) {
				assertTrue(called[0]);
				return new BmsContent(spec);
			}
			@Override public BmsNote createNote() {
				assertTrue(called[0]);
				return new BmsNote();
			}
			@Override public void startLoad(BmsLoaderSettings spec) {
				assertFalse(called[0]);  // 二度呼ばれたらテスト失敗
				called[0] = true;
			}
			@Override public boolean parseError(BmsScriptError error) {
				assertTrue(called[0]);
				return false;
			}
			@Override public BmsTestResult testDeclaration(String key, String value) {
				assertTrue(called[0]);
				return BmsTestResult.OK;
			}
			@Override public BmsTestResult testMeta(BmsMeta meta, int index, Object value) {
				assertTrue(called[0]);
				return BmsTestResult.OK;
			}
			@Override public BmsTestResult testChannel(BmsChannel channel, int index, int measure, Object value) {
				assertTrue(called[0]);
				return BmsTestResult.OK;
			}
			@Override public BmsTestResult testContent(BmsContent content) {
				assertTrue(called[0]);
				return BmsTestResult.OK;
			}
		}, bms);
	}

	// startLoad(BmsLoaderSettings)
	// I/F内で例外をスローすると、スローした例外を内包したBmsExceptionがスローされること
	@Test
	public void testStartLoad_004() throws Exception {
		var e = assertThrows(BmsException.class, () -> {
			testHandler(new BmsLoadHandler() {
				@Override public void startLoad(BmsLoaderSettings spec) {
					throw new RuntimeException("EXCEPTION");
				}
			}, "");
		});
		assertEquals(RuntimeException.class, e.getCause().getClass());
	}

	// testContent(BmsContent)
	// startLoad()が呼ばれた後で呼び出されること
	@Test
	public void testTestContent_001() throws Exception {
		var ok = new boolean[] { false };
		testHandler(new BmsLoadHandler() {
			private boolean calledStart = false;
			@Override public void startLoad(BmsLoaderSettings spec) {
				calledStart = true;
			}
			@Override public BmsTestResult testContent(BmsContent content) {
				assertTrue(calledStart);
				ok[0] = true;
				return BmsTestResult.OK;
			}
		}, "");
		assertTrue(ok[0]);
	}

	// testContent(BmsContent)
	// createContent()で生成したBmsContentオブジェクト参照がパラメータで渡されていること
	@Test
	public void testTestContent_002() throws Exception {
		var ok = new boolean[] { false };
		testHandler(new BmsLoadHandler() {
			private BmsContent createdContent = null;
			@Override public BmsContent createContent(BmsSpec spec) {
				assertNull(createdContent);  // 二度呼ばれたらテスト失敗
				createdContent = new BmsContent(spec);
				return createdContent;
			}
			@Override public BmsTestResult testContent(BmsContent content) {
				assertNotNull(createdContent);
				assertSame(createdContent, content);
				ok[0] = true;
				return BmsTestResult.OK;
			}
		}, "");
		assertTrue(ok[0]);
	}

	// testContent(BmsContent)
	// I/F内(createContent)で例外をスローすると当メソッドは呼ばれず、スローした例外を内包したBmsExceptionがスローされること
	@Test
	public void testTestContent_003() throws Exception {
		var called = new boolean[] { false };
		var e = assertThrows(BmsException.class, () -> {
			var bms =
					";?bms key=\"value\"\n" +
					"#SINTEGER 100\n" +
					"#00007:01";
			testHandler(new BmsLoadHandler() {
				@Override public BmsContent createContent(BmsSpec spec) {
					throw new RuntimeException("EXCEPTION");
				}
				@Override public BmsTestResult testContent(BmsContent content) {
					called[0] = true;
					return BmsTestResult.OK;
				}
			}, bms);
		});
		assertFalse(called[0]);
		assertEquals(RuntimeException.class, e.getCause().getClass());
	}

	// testContent(BmsContent)
	// I/F内(createNote)で例外をスローすると当メソッドは呼ばれず、スローした例外を内包したBmsExceptionがスローされること
	@Test
	public void testTestContent_004() throws Exception {
		var called = new boolean[] { false };
		var e = assertThrows(BmsException.class, () -> {
			var bms =
					";?bms key=\"value\"\n" +
					"#SINTEGER 100\n" +
					"#00007:01";
			testHandler(new BmsLoadHandler() {
				@Override public BmsNote createNote() {
					throw new RuntimeException("EXCEPTION");
				}
				@Override public BmsTestResult testContent(BmsContent content) {
					called[0] = true;
					return BmsTestResult.OK;
				}
			}, bms);
		});
		assertFalse(called[0]);
		assertEquals(RuntimeException.class, e.getCause().getClass());
	}

	// testContent(BmsContent)
	// I/F内(startLoad)で例外をスローすると当メソッドは呼ばれず、スローした例外を内包したBmsExceptionがスローされること
	@Test
	public void testTestContent_005() throws Exception {
		var called = new boolean[] { false };
		var e = assertThrows(BmsException.class, () -> {
			var bms =
					";?bms key=\"value\"\n" +
					"#SINTEGER 100\n" +
					"#00007:01";
			testHandler(new BmsLoadHandler() {
				@Override public void startLoad(BmsLoaderSettings spec) {
					throw new RuntimeException("EXCEPTION");
				}
				@Override public BmsTestResult testContent(BmsContent content) {
					called[0] = true;
					return BmsTestResult.OK;
				}
			}, bms);
		});
		assertFalse(called[0]);
		assertEquals(RuntimeException.class, e.getCause().getClass());
	}

	// testContent(BmsContent)
	// I/F内(parseError)で例外をスローすると当メソッドは呼ばれず、スローした例外を内包したBmsExceptionがスローされること
	@Test
	public void testTestContent_006() throws Exception {
		var called = new boolean[] { false };
		var e = assertThrows(BmsException.class, () -> {
			var bms =
					";?bms key=\"value\"\n" +
					"#SINTEGER 100\n" +
					"#00007:01";
			testHandler(new BmsLoadHandler() {
				@Override public boolean parseError(BmsScriptError error) {
					throw new RuntimeException("EXCEPTION");
				}
				@Override public BmsTestResult testDeclaration(String key, String value) {
					return BmsTestResult.FAIL;
				}
				@Override public BmsTestResult testContent(BmsContent content) {
					called[0] = true;
					return BmsTestResult.OK;
				}
			}, bms);
		});
		assertFalse(called[0]);
		assertEquals(RuntimeException.class, e.getCause().getClass());
	}

	// testContent(BmsContent)
	// I/F内(testDeclaration)で例外をスローすると当メソッドは呼ばれず、スローした例外を内包したBmsExceptionがスローされること
	@Test
	public void testTestContent_007() throws Exception {
		var called = new boolean[] { false };
		var e = assertThrows(BmsException.class, () -> {
			var bms =
					";?bms key=\"value\"\n" +
					"#SINTEGER 100\n" +
					"#00007:01";
			testHandler(new BmsLoadHandler() {
				@Override public BmsTestResult testDeclaration(String key, String value) {
					throw new RuntimeException("EXCEPTION");
				}
				@Override public BmsTestResult testContent(BmsContent content) {
					called[0] = true;
					return BmsTestResult.OK;
				}
			}, bms);
		});
		assertFalse(called[0]);
		assertEquals(RuntimeException.class, e.getCause().getClass());
	}

	// testContent(BmsContent)
	// I/F内(testMeta)で例外をスローすると当メソッドは呼ばれず、スローした例外を内包したBmsExceptionがスローされること
	@Test
	public void testTestContent_008() throws Exception {
		var called = new boolean[] { false };
		var e = assertThrows(BmsException.class, () -> {
			var bms =
					";?bms key=\"value\"\n" +
					"#SINTEGER 100\n" +
					"#00007:01";
			testHandler(new BmsLoadHandler() {
				@Override public BmsTestResult testMeta(BmsMeta meta, int index, Object value) {
					throw new RuntimeException("EXCEPTION");
				}
				@Override public BmsTestResult testContent(BmsContent content) {
					called[0] = true;
					return BmsTestResult.OK;
				}
			}, bms);
		});
		assertFalse(called[0]);
		assertEquals(RuntimeException.class, e.getCause().getClass());
	}

	// testContent(BmsContent)
	// I/F内(testChannel)で例外をスローすると当メソッドは呼ばれず、スローした例外を内包したBmsExceptionがスローされること(値型チャンネル)
	@Test
	public void testTestContent_009_ValueType() throws Exception {
		var called = new boolean[] { false };
		var e = assertThrows(BmsException.class, () -> {
			var bms =
					";?bms key=\"value\"\n" +
					"#SINTEGER 100\n" +
					"#00001:255";
			testHandler(new BmsLoadHandler() {
				@Override public BmsTestResult testChannel(BmsChannel channel, int index, int measure, Object value) {
					throw new RuntimeException("EXCEPTION");
				}
				@Override public BmsTestResult testContent(BmsContent content) {
					called[0] = true;
					return BmsTestResult.OK;
				}
			}, bms);
		});
		assertFalse(called[0]);
		assertEquals(RuntimeException.class, e.getCause().getClass());
	}

	// testContent(BmsContent)
	// I/F内(testChannel)で例外をスローすると当メソッドは呼ばれず、スローした例外を内包したBmsExceptionがスローされること(配列型チャンネル)
	@Test
	public void testTestContent_009_ArrayType() throws Exception {
		var called = new boolean[] { false };
		var e = assertThrows(BmsException.class, () -> {
			var bms =
					";?bms key=\"value\"\n" +
					"#SINTEGER 100\n" +
					"#00007:01";
			testHandler(new BmsLoadHandler() {
				@Override public BmsTestResult testChannel(BmsChannel channel, int index, int measure, Object value) {
					throw new RuntimeException("EXCEPTION");
				}
				@Override public BmsTestResult testContent(BmsContent content) {
					called[0] = true;
					return BmsTestResult.OK;
				}
			}, bms);
		});
		assertFalse(called[0]);
		assertEquals(RuntimeException.class, e.getCause().getClass());
	}

	// testContent(BmsContent)
	// 戻り値をtrueで返却時はパラメータと同じオブジェクト参照がload()の戻り値で返却されること
	@Test
	public void testTestContent_010() throws Exception {
		var finishedContent = new BmsContent[] { null };
		var returnedContent = testHandler(new BmsLoadHandler() {
			@Override public BmsTestResult testContent(BmsContent content) {
				finishedContent[0] = content;
				return BmsTestResult.OK;
			}
		}, "");
		assertNotNull(returnedContent);
		assertNotNull(finishedContent[0]);
		assertSame(finishedContent[0], returnedContent);
	}

	// testContent(BmsContent)
	// 戻り値をFAILで返却時はparseError()は呼ばれ、BmsAbortExceptionがスローされ、エラー種別がTEST_CONTENTになること
	@Test
	public void testTestContent_011() throws Exception {
		var called = new boolean[] { false, false };
		var e = assertThrows(BmsLoadException.class, () -> {
			testHandler(new BmsLoadHandler() {
				@Override public boolean parseError(BmsScriptError error) {
					called[1] = true;
					return false;
				}
				@Override public BmsTestResult testContent(BmsContent content) {
					called[0] = true;
					return BmsTestResult.FAIL;
				}
			}, "");
		});
		assertTrue(called[0]);
		assertTrue(called[1]);
		assertEquals(BmsErrorType.TEST_CONTENT, e.getError().getType());
	}

	// testContent(BmsContent)
	// I/F内で例外をスローすると、当該例外を内包したBmsExceptionがスローされること
	@Test
	public void testTestContent_012() throws Exception {
		var called = new boolean[] { false };
		var e = assertThrows(BmsException.class, () -> {
			testHandler(new BmsLoadHandler() {
				@Override public BmsTestResult testContent(BmsContent content) {
					called[0] = true;
					throw new RuntimeException("EXCEPTION");
				}
			}, "");
		});
		assertEquals(RuntimeException.class, e.getCause().getClass());
	}

	// testContent(BmsContent)
	// 戻り値がFAIL以外は全て検査成功になること
	@Test
	public void testTestContent_OtherFail() throws Exception {
		// OK
		var called = new boolean[] { false };
		testHandler(new BmsLoadHandler() {
			@Override public BmsTestResult testContent(BmsContent content) {
				called[0] = true;
				return BmsTestResult.OK;
			}
		}, "");
		assertTrue(called[0]);

		// THROUGH
		Arrays.fill(called, false);
		testHandler(new BmsLoadHandler() {
			@Override public BmsTestResult testContent(BmsContent content) {
				called[0] = true;
				return BmsTestResult.THROUGH;
			}
		}, "");
		assertTrue(called[0]);
	}

	// testContent(BmsContent)
	// BMSコンテンツを編集モードで返すとBmsExceptionがスローされること
	@Test
	public void testTestContent_EditMode() throws Exception {
		var called = new boolean[] { false };
		assertThrows(BmsException.class, () -> testHandler(new BmsLoadHandler() {
			@Override public BmsTestResult testContent(BmsContent content) {
				called[0] = true;
				content.beginEdit();
				return BmsTestResult.OK;
			}
		}, ""));
		assertTrue(called[0]);
	}

	// parseError(BmsScriptError)
	// SYNTAX, false
	@Test
	public void testParseError_001() throws Exception {
		var e = assertThrows(BmsException.class, () -> {
			var bms =
					"This_is_syntax_error_line";
			testHandler(new BmsLoadHandler() {
				@Override public boolean parseError(BmsScriptError error) {
					assertEquals(BmsErrorType.SYNTAX, error.getType());
					return false;
				}
			}, bms);
		});
		assertEquals(BmsLoadException.class, e.getClass());
	}

	// parseError(BmsScriptError)
	// SYNTAX, true
	@Test
	public void testParseError_002() throws Exception {
		var callCount = new int[] { 0 };
		var bms =
				"#GENRE Good Genre Definition\n" +
				"X#TITLE Bad Title Definition\n" +
				"#00003?Bad Channel Definition\n";
		var content = testHandler(new BmsLoadHandler() {
			@Override public boolean parseError(BmsScriptError error) {
				assertEquals(BmsErrorType.SYNTAX, error.getType());
				callCount[0]++;
				return true;
			}
		}, bms);
		assertEquals(2, callCount[0]);
		assertEquals("Good Genre Definition", (String)content.getSingleMeta("#genre"));
		assertNotEquals("Bad Title Definition", (String)content.getSingleMeta("#title"));
		assertNotEquals("Bad Channel Definition", content.getMeasureValue(3, 0));
	}

	// parseError(BmsScriptError)
	// TEST_DECLARATION, false
	@Test
	public void testParseError_005() throws Exception {
		var e = assertThrows(BmsException.class, () -> {
			var bms =
					";?bms key=\"value\"";
			testHandler(new BmsLoadHandler() {
				@Override public boolean parseError(BmsScriptError error) {
					assertEquals(BmsErrorType.TEST_DECLARATION, error.getType());
					return false;
				}
				@Override public BmsTestResult testDeclaration(String key, String value) {
					return BmsTestResult.FAIL;
				}
			}, bms);
		});
		assertEquals(BmsLoadException.class, e.getClass());
	}

	// parseError(BmsScriptError)
	// TEST_DECLARATION, true
	@Test
	public void testParseError_006() throws Exception {
		var callCount = new int[] { 0 };
		var bms =
				";?bms key1=\"value\" key2=\"hoge\"";
		var content = testHandler(new BmsLoadHandler() {
			@Override public boolean parseError(BmsScriptError error) {
				assertEquals(BmsErrorType.TEST_DECLARATION, error.getType());
				callCount[0]++;
				return true;
			}
			@Override public BmsTestResult testDeclaration(String key, String value) {
				return BmsTestResult.FAIL;
			}
		}, bms);
		assertEquals(2, callCount[0]);
		assertFalse(content.containsDeclaration("key1"));
		assertFalse(content.containsDeclaration("key2"));
	}

	// parseError(BmsScriptError)
	// TEST_META, false
	@Test
	public void testParseError_007() throws Exception {
		var e = assertThrows(BmsException.class, () -> {
			var bms =
					"#TITLE hogehoge";
			testHandler(new BmsLoadHandler() {
				@Override public boolean parseError(BmsScriptError error) {
					assertEquals(BmsErrorType.TEST_META, error.getType());
					return false;
				}
				@Override public BmsTestResult testMeta(BmsMeta meta, int index, Object value) {
					return BmsTestResult.FAIL;
				}
			}, bms);
		});
		assertEquals(BmsLoadException.class, e.getClass());
	}

	// parseError(BmsScriptError)
	// TEST_META, true
	@Test
	public void testParseError_008() throws Exception {
		var callCount = new int[] { 0 };
		var bms =
				"#GENRE Eurobeat\n" +
				"#TITLE LoveSong";
		var content = testHandler(new BmsLoadHandler() {
			@Override public boolean parseError(BmsScriptError error) {
				assertEquals(BmsErrorType.TEST_META, error.getType());
				callCount[0]++;
				return true;
			}
			@Override public BmsTestResult testMeta(BmsMeta meta, int index, Object value) {
				return BmsTestResult.FAIL;
			}
		}, bms);
		assertEquals(2, callCount[0]);
		assertFalse(content.containsSingleMeta("#genre"));
		assertFalse(content.containsSingleMeta("#title"));
	}

	// parseError(BmsScriptError)
	// TEST_CHANNEL, false
	@Test
	public void testParseError_009() throws Exception {
		var e = assertThrows(BmsException.class, () -> {
			var bms =
					"#00007:01020304";
			testHandler(new BmsLoadHandler() {
				@Override public boolean parseError(BmsScriptError error) {
					assertEquals(BmsErrorType.TEST_CHANNEL, error.getType());
					return false;
				}
				@Override public BmsTestResult testChannel(BmsChannel channel, int index, int measure, Object value) {
					return BmsTestResult.FAIL;
				}
			}, bms);
		});
		assertEquals(BmsLoadException.class, e.getClass());
	}

	// parseError(BmsScriptError)
	// TEST_CHANNEL, true
	@Test
	public void testParseError_010() throws Exception {
		var callCount = new int[] { 0 };
		var bms =
				"#00001:2500\n" +
				"#00007:AA";
		var content = testHandler(new BmsLoadHandler() {
			@Override public boolean parseError(BmsScriptError error) {
				assertEquals(BmsErrorType.TEST_CHANNEL, error.getType());
				callCount[0]++;
				return true;
			}
			@Override public BmsTestResult testChannel(BmsChannel channel, int index, int measure, Object value) {
				return BmsTestResult.FAIL;
			}
		}, bms);
		assertEquals(2, callCount[0]);
		assertFalse(content.containsMeasureValue(1, 0));
		assertNull(content.getNote(7, 0, 0));
	}

	// parseError(BmsScriptError)
	// UNKNOWN_META, false
	@Test
	public void testParseError_011() throws Exception {
		var e = assertThrows(BmsException.class, () -> {
			var bms =
					"#UNKNOWN_META_NAME hogehoge";
			testHandler(new BmsLoadHandler() {
				@Override public boolean parseError(BmsScriptError error) {
					assertEquals(BmsErrorType.UNKNOWN_META, error.getType());
					return false;
				}
			}, bms);
		});
		assertEquals(BmsLoadException.class, e.getClass());
	}

	// parseError(BmsScriptError)
	// UNKNOWN_META, true
	@Test
	public void testParseError_012() throws Exception {
		var callCount = new int[] { 0 };
		var bms =
				"#UNKNOWN_META1 hogehoge\n" +
				"#UNKNOWN_META2 hagehage\n" +
				"#TITLE MySong";
		var content = testHandler(new BmsLoadHandler() {
			@Override public boolean parseError(BmsScriptError error) {
				assertEquals(BmsErrorType.UNKNOWN_META, error.getType());
				callCount[0]++;
				return true;
			}
		}, bms);
		assertEquals(2, callCount[0]);
		assertEquals("MySong", content.getSingleMeta("#title"));
	}

	// parseError(BmsScriptError)
	// UNKNOWN_CHANNEL, false
	@Test
	public void testParseError_013() throws Exception {
		var e = assertThrows(BmsException.class, () -> {
			var bms =
					"#000XY:AA";
			testHandler(new BmsLoadHandler() {
				@Override public boolean parseError(BmsScriptError error) {
					assertEquals(BmsErrorType.UNKNOWN_CHANNEL, error.getType());
					return false;
				}
			}, bms);
		});
		assertEquals(BmsLoadException.class, e.getClass());
	}

	// parseError(BmsScriptError)
	// UNKNOWN_CHANNEL, true
	@Test
	public void testParseError_014() throws Exception {
		var callCount = new int[] { 0 };
		var bms =
				"#000YZ:AABBCCDD\n" +
				"#00007:XXYY\n" +
				"#00001:280";
		var content = testHandler(new BmsLoadHandler() {
			@Override public boolean parseError(BmsScriptError error) {
				assertEquals(BmsErrorType.UNKNOWN_CHANNEL, error.getType());
				callCount[0]++;
				return true;
			}
		}, bms);
		assertEquals(1, callCount[0]);
		assertNotNull(content.getNote(7, 0, 0.0));
		assertNotNull(content.getNote(7, 0, 96.0));
		assertEquals(280L, (long)content.getMeasureValue(1, 0));
	}

	// parseError(BmsScriptError)
	// WRONG_DATA, false
	@Test
	public void testParseError_015() throws Exception {
		var e = assertThrows(BmsException.class, () -> {
			var bms =
					"#BPM TypeMismatch";
			testHandler(new BmsLoadHandler() {
				@Override public boolean parseError(BmsScriptError error) {
					assertEquals(BmsErrorType.WRONG_DATA, error.getType());
					return false;
				}
			}, bms);
		});
		assertEquals(BmsLoadException.class, e.getClass());
	}

	// parseError(BmsScriptError)
	// WRONG_DATA, true
	@Test
	public void testParseError_016() throws Exception {
		var callCount = new int[] { 0 };
		var bms =
				"#BPM StringType\n" +
				"#00001:NotInteger\n" +
				"#00007:!!?NotArray?!!";
		var content = testHandler(new BmsLoadHandler() {
			@Override public boolean parseError(BmsScriptError error) {
				assertEquals(BmsErrorType.WRONG_DATA, error.getType());
				callCount[0]++;
				return true;
			}
		}, bms);
		assertEquals(3, callCount[0]);
		assertEquals((double)content.getSpec().getInitialBpmMeta().getDefaultValue(), content.getInitialBpm(), 0.00001);
		assertFalse(content.containsMeasureValue(1, 0));
		assertNull(content.getNote(7, 0, 0));
	}

	// parseError(BmsScriptError)
	// COMMENT_NOT_CLOSED, false
	@Test
	public void testParseError_017() throws Exception {
		var e = assertThrows(BmsException.class, () -> {
			var bms =
					"/* \n" +
					" * This is a multi line comment.\n" +
					" * I don't close this comment.";
			testHandler(new BmsLoadHandler() {
				@Override public boolean parseError(BmsScriptError error) {
					assertEquals(BmsErrorType.COMMENT_NOT_CLOSED, error.getType());
					return false;
				}
			}, bms);
		});
		assertEquals(BmsLoadException.class, e.getClass());
	}

	// parseError(BmsScriptError)
	// COMMENT_NOT_CLOSED, true
	@Test
	public void testParseError_018() throws Exception {
		var callCount = new int[] { 0 };
		var bms =
				";?bms key=\"value\"\n" +
				"#TITLE MySong\n" +
				"#00007:AA\n" +
				"/* \n" +
				" * This is a multi line comment.\n" +
				" * I don't close this comment.";
		var content = testHandler(new BmsLoadHandler() {
			@Override public boolean parseError(BmsScriptError error) {
				assertEquals(BmsErrorType.COMMENT_NOT_CLOSED, error.getType());
				callCount[0]++;
				return true;
			}
		}, bms);
		assertEquals(1, callCount[0]);
		assertEquals("value", content.getDeclaration("key"));
		assertEquals("MySong", content.getSingleMeta("#title"));
		assertNotNull(content.getNote(7, 0, 0));
	}

	// parseError(BmsScriptError)
	// PANIC, false
	@Test
	public void testParseError_019() throws Exception {
		assertThrows(BmsLoadException.class, () -> {
			var bms =
					"#TITLE my song";
			testHandler(new BmsLoadHandler() {
				@Override public boolean parseError(BmsScriptError error) {
					assertEquals(BmsErrorType.PANIC, error.getType());
					return false;
				}
				@Override public BmsTestResult testMeta(BmsMeta meta, int index, Object value) {
					return null;
				}
			}, bms);
		});
	}

	// parseError(BmsScriptError)
	// PANIC, true
	@Test
	public void testParseError_020() throws Exception {
		var callCount = new int[] { 0 };
		var bms =
				";?bms key=\"value\"\n" +
				"#TITLE MySong\n" +
				"#00001:999\n" +
				"#00007:ZZ";
		var content = testHandler(new BmsLoadHandler() {
			@Override public boolean parseError(BmsScriptError error) {
				assertEquals(BmsErrorType.PANIC, error.getType());
				callCount[0]++;
				return true;
			}
			@Override public BmsTestResult testDeclaration(String key, String value) {
				return null;
			}
			@Override public BmsTestResult testMeta(BmsMeta meta, int index, Object value) {
				return null;
			}
			@Override public BmsTestResult testChannel(BmsChannel channel, int index, int measure, Object value) {
				return null;
			}
		}, bms);
		assertEquals(4, callCount[0]);
		assertFalse(content.containsDeclaration("key"));
		assertFalse(content.containsSingleMeta("#title"));
		assertFalse(content.containsMeasureValue(1, 0));
		assertNull(content.getNote(7, 0, 0));
	}

	// testDeclaration(String, String)
	// OKを返すと当該データがBMSコンテンツに格納されること
	@Test
	public void testTestDeclaration_001() throws Exception {
		var bms =
				";?bms key1=\"value1\" key2=\"value2\"";
		var content = testHandler(new BmsLoadHandler() {
			@Override public BmsTestResult testDeclaration(String key, String value) {
				return BmsTestResult.OK;
			}
		}, bms);
		var decls = content.getDeclarations();
		assertEquals(2, decls.size());
		assertEquals("value1", decls.get("key1"));
		assertEquals("value2", decls.get("key2"));
	}

	// testDeclaration(String, String)
	// FAILを返すとparseErrorが呼ばれ、そこでtrueを返すとBMS読み込みが続行されること
	@Test
	public void testTestDeclaration_002() throws Exception {
		var callCount = new int[] { 0 };
		var bms =
				";?bms key1=\"value1\" key2=\"value2\"";
		var content = testHandler(new BmsLoadHandler() {
			@Override public boolean parseError(BmsScriptError error) {
				callCount[0]++;
				return true;
			}
			@Override public BmsTestResult testDeclaration(String key, String value) {
				return BmsTestResult.FAIL;
			}
		}, bms);

		assertEquals(2, callCount[0]);
		assertEquals(0, content.getDeclarations().size());
	}

	// testDeclaration(String, String)
	// FAILを返すとparseErrorが呼ばれ、そこでfalseを返すとload()がBmsAbortExceptionをスローすること
	@Test
	public void testTestDeclaration_003() throws Exception {
		var called = new boolean[] { false };
		assertThrows(BmsLoadException.class, () -> {
			var bms =
					";?bms key=\"value\"";
			testHandler(new BmsLoadHandler() {
				@Override public boolean parseError(BmsScriptError error) {
					called[0] = true;
					return false;
				}
				@Override public BmsTestResult testDeclaration(String key, String value) {
					return BmsTestResult.FAIL;
				}
			}, bms);
		});
		assertTrue(called[0]);
	}

	// testDeclaration(String, String)
	// THROUGHを返すとparseErrorは呼ばれずにBMS読み込みが続行され、当該データがBMSコンテンツに格納されないこと
	@Test
	public void testTestDeclaration_004() throws Exception {
		var called = new boolean[] { false };
		var bms =
				";?bms key1=\"value1\" key2=\"value2\"";
		var content = testHandler(new BmsLoadHandler() {
			@Override public boolean parseError(BmsScriptError error) {
				called[0] = true;
				return false;
			}
			@Override public BmsTestResult testDeclaration(String key, String value) {
				return BmsTestResult.THROUGH;
			}
		}, bms);

		assertFalse(called[0]);
		assertEquals(0, content.getDeclarations().size());
	}

	// testDeclaration(String, String)
	// nullを返すとparseErrorが呼ばれ、エラー種別がPANICになること
	@Test
	public void testTestDeclaration_005() throws Exception {
		var called = new boolean[] { false };
		var bms =
				";?bms key=\"value\"";
		testHandler(new BmsLoadHandler() {
			@Override public boolean parseError(BmsScriptError error) {
				called[0] = true;
				assertEquals(error.getType(), BmsErrorType.PANIC);
				return true;
			}
			@Override public BmsTestResult testDeclaration(String key, String value) {
				return null;
			}
		}, bms);
		assertTrue(called[0]);
	}

	// testDeclaration(String, String)
	// OKを返すと当該データがBMSコンテンツに格納されること(Single)
	@Test
	public void testTestMeta_001_Single() throws Exception {
		var bms =
				"#GENRE MyGenre\n" +
				"#TITLE MySong";
		var content = testHandler(new BmsLoadHandler() {
			@Override public BmsTestResult testMeta(BmsMeta meta, int index, Object value) {
				return BmsTestResult.OK;
			}
		}, bms);
		assertEquals("MyGenre", content.getSingleMeta("#genre"));
		assertEquals("MySong", content.getSingleMeta("#title"));
	}

	// testMeta(BmsMeta, int, Object)
	// OKを返すと当該データがBMSコンテンツに格納されること(Multiple)
	@Test
	public void testTestMeta_001_Multiple() throws Exception {
		var bms =
				"#SUBARTIST Sato\n" +
				"#SUBARTIST Suzuki\n" +
				"#SUBARTIST Tanaka";
		var content = testHandler(new BmsLoadHandler() {
			@Override public BmsTestResult testMeta(BmsMeta meta, int index, Object value) {
				return BmsTestResult.OK;
			}
		}, bms);
		assertEquals(3, content.getMultipleMetaCount("#subartist"));
		assertEquals("Sato", content.getMultipleMeta("#subartist", 0));
		assertEquals("Suzuki", content.getMultipleMeta("#subartist", 1));
		assertEquals("Tanaka", content.getMultipleMeta("#subartist", 2));
	}

	// testMeta(BmsMeta, int, Object)
	// OKを返すと当該データがBMSコンテンツに格納されること(Indexed)
	@Test
	public void testTestMeta_001_Indexed() throws Exception {
		var bms =
				"#ISTRING01 hoge\n" +
				"#ISTRINGAB hage\n" +
				"#ISTRINGZZ hige";
		var content = testHandler(new BmsLoadHandler() {
			@Override public BmsTestResult testMeta(BmsMeta meta, int index, Object value) {
				return BmsTestResult.OK;
			}
		}, bms);
		assertEquals(3, content.getIndexedMetaCount("#istring"));
		assertEquals("hoge", content.getIndexedMeta("#istring", Integer.parseInt("01", 36)));
		assertEquals("hage", content.getIndexedMeta("#istring", Integer.parseInt("AB", 36)));
		assertEquals("hige", content.getIndexedMeta("#istring", Integer.parseInt("ZZ", 36)));
	}

	// testMeta(BmsMeta, int, Object)
	// FAILを返すとparseErrorが呼ばれ、そこでtrueを返すとBMS読み込みが続行されること
	@Test
	public void testTestMeta_002() throws Exception {
		var callCount = new int[] { 0 };
		var bms =
				"#GENRE MyGenre\n" +
				"#TITLE MySong";
		var content = testHandler(new BmsLoadHandler() {
			@Override public boolean parseError(BmsScriptError error) {
				callCount[0]++;
				return true;
			}
			@Override public BmsTestResult testMeta(BmsMeta meta, int index, Object value) {
				return BmsTestResult.FAIL;
			}
		}, bms);
		assertEquals(2, callCount[0]);
		assertFalse(content.containsSingleMeta("#genre"));
		assertFalse(content.containsSingleMeta("#title"));
	}

	// testMeta(BmsMeta, int, Object)
	// FAILを返すとparseErrorが呼ばれ、そこでfalseを返すとload()がBmsAbortExceptionをスローすること
	@Test
	public void testTestMeta_003() throws Exception {
		var called = new boolean[] { false };
		assertThrows(BmsLoadException.class, () -> {
			var bms =
					"#TITLE MySong";
			testHandler(new BmsLoadHandler() {
				@Override public boolean parseError(BmsScriptError error) {
					called[0] = true;
					return false;
				}
				@Override public BmsTestResult testMeta(BmsMeta meta, int index, Object value) {
					return BmsTestResult.FAIL;
				}
			}, bms);
		});
		assertTrue(called[0]);
	}

	// testMeta(BmsMeta, int, Object)
	// THROUGHを返すとparseErrorは呼ばれずにBMS読み込みが続行され、当該データがBMSコンテンツに格納されないこと
	@Test
	public void testTestMeta_004() throws Exception {
		var called = new boolean[] { false };
		var bms =
				"#GENRE MyGenre\n" +
				"#TITLE MySong";
		var content = testHandler(new BmsLoadHandler() {
			@Override public boolean parseError(BmsScriptError error) {
				called[0] = true;
				return false;
			}
			@Override public BmsTestResult testMeta(BmsMeta meta, int index, Object value) {
				return BmsTestResult.THROUGH;
			}
		}, bms);
		assertFalse(called[0]);
		assertFalse(content.containsSingleMeta("#genre"));
		assertFalse(content.containsSingleMeta("#title"));
	}

	// testMeta(BmsMeta, int, Object)
	// nullを返すとparseErrorが呼ばれ、エラー種別がPANICになること
	@Test
	public void testTestMeta_005() throws Exception {
		var called = new boolean[] { false };
		var bms =
				"#TITLE MySong";
		testHandler(new BmsLoadHandler() {
			@Override public boolean parseError(BmsScriptError error) {
				called[0] = true;
				assertEquals(error.getType(), BmsErrorType.PANIC);
				return true;
			}
			@Override public BmsTestResult testMeta(BmsMeta meta, int index, Object value) {
				return null;
			}
		}, bms);
		assertTrue(called[0]);
	}

	// testChannel(BmsChannel, int, int, Object)
	// OKを返すと当該データがBMSコンテンツに格納されること
	@Test
	public void testTestChannel_ValueType_001() throws Exception {
		var bms =
				"#00001:180\n" +
				"#00003:String";
		var content = testHandler(new BmsLoadHandler() {
			@Override public BmsTestResult testChannel(BmsChannel channel, int index, int measure, Object value) {
				return BmsTestResult.OK;
			}
		}, bms);
		assertEquals(180L, (long)content.getMeasureValue(1, 0));
		assertEquals("String", content.getMeasureValue(3, 0));
	}

	// testChannel(BmsChannel, int, int, Object)
	// FAILを返すとparseErrorが呼ばれ、そこでtrueを返すとBMS読み込みが続行されること
	@Test
	public void testTestChannel_ValueType_002() throws Exception {
		var callCount = new int[] { 0 };
		var bms =
				"#00001:180\n" +
				"#00003:String";
		var content = testHandler(new BmsLoadHandler() {
			@Override public boolean parseError(BmsScriptError error) {
				callCount[0]++;
				return true;
			}
			@Override public BmsTestResult testChannel(BmsChannel channel, int index, int measure, Object value) {
				return BmsTestResult.FAIL;
			}
		}, bms);
		assertEquals(2, callCount[0]);
		assertFalse(content.containsMeasureValue(1, 0));
		assertFalse(content.containsMeasureValue(3, 0));
	}

	// testChannel(BmsChannel, int, int, Object)
	// FAILを返すとparseErrorが呼ばれ、そこでfalseを返すとload()がBmsAbortExceptionをスローすること
	@Test
	public void testTestChannel_ValueType_003() throws Exception {
		var called = new boolean[] { false };
		assertThrows(BmsLoadException.class, () -> {
			var bms =
					"#00001:180";
			testHandler(new BmsLoadHandler() {
				@Override public boolean parseError(BmsScriptError error) {
					called[0] = true;
					return false;
				}
				@Override public BmsTestResult testChannel(BmsChannel channel, int index, int measure, Object value) {
					return BmsTestResult.FAIL;
				}
			}, bms);
		});
		assertTrue(called[0]);
	}

	// testChannel(BmsChannel, int, int, Object)
	// THROUGHを返すとparseErrorは呼ばれずにBMS読み込みが続行され、当該データがBMSコンテンツに格納されないこと
	@Test
	public void testTestChannel_ValueType_004() throws Exception {
		var called = new boolean[] { false };
		var bms =
				"#00001:180\n" +
				"#00003:String";
		var content = testHandler(new BmsLoadHandler() {
			@Override public boolean parseError(BmsScriptError error) {
				called[0] = true;
				return false;
			}
			@Override public BmsTestResult testChannel(BmsChannel channel, int index, int measure, Object value) {
				return BmsTestResult.THROUGH;
			}
		}, bms);
		assertFalse(called[0]);
		assertFalse(content.containsMeasureValue(1, 0));
		assertFalse(content.containsMeasureValue(3, 0));
	}

	// testChannel(BmsChannel, int, int, Object)
	// nullを返すとparseErrorが呼ばれ、エラー種別がPANICになること
	@Test
	public void testTestChannel_ValueType_005() throws Exception {
		var called = new boolean[] { false };
		var bms =
				"#00001:180";
		testHandler(new BmsLoadHandler() {
			@Override public boolean parseError(BmsScriptError error) {
				called[0] = true;
				assertEquals(error.getType(), BmsErrorType.PANIC);
				return true;
			}
			@Override public BmsTestResult testChannel(BmsChannel channel, int index, int measure, Object value) {
				return null;
			}
		}, bms);
		assertTrue(called[0]);
	}

	// testChannel(BmsChannel, int, int, Object)
	// OKを返すと当該データがBMSコンテンツに格納されること
	@Test
	public void testTestChannel_ArrayType_001() throws Exception {
		var bms =
				"#00007:AA\n" +
				"#00107:BB";
		var content = testHandler(new BmsLoadHandler() {
			@Override public BmsTestResult testChannel(BmsChannel channel, int index, int measure, Object value) {
				return BmsTestResult.OK;
			}
		}, bms);
		assertEquals(Long.parseLong("AA", 36), content.getNote(7, 0, 0.0).getValue());
		assertEquals(Long.parseLong("BB", 36), content.getNote(7, 1, 0.0).getValue());
	}

	// testChannel(BmsChannel, int, int, Object)
	// FAILを返すとparseErrorが呼ばれ、そこでtrueを返すとBMS読み込みが続行されること
	@Test
	public void testTestChannel_ArrayType_002() throws Exception {
		var callCount = new int[] { 0 };
		var bms =
				"#00007:AA\n" +
				"#00107:BB";
		var content = testHandler(new BmsLoadHandler() {
			@Override public boolean parseError(BmsScriptError error) {
				callCount[0]++;
				return true;
			}
			@Override public BmsTestResult testChannel(BmsChannel channel, int index, int measure, Object value) {
				return BmsTestResult.FAIL;
			}
		}, bms);
		assertEquals(2, callCount[0]);
		assertNull(content.getNote(7, 0, 0.0));
		assertNull(content.getNote(7, 1, 0.0));
	}

	// testChannel(BmsChannel, int, int, Object)
	// FAILを返すとparseErrorが呼ばれ、そこでfalseを返すとload()がBmsAbortExceptionをスローすること
	@Test
	public void testTestChannel_ArrayType_003() throws Exception {
		var called = new boolean[] { false };
		assertThrows(BmsLoadException.class, () -> {
			var bms =
					"#00007:AA";
			testHandler(new BmsLoadHandler() {
				@Override public boolean parseError(BmsScriptError error) {
					called[0] = true;
					return false;
				}
				@Override public BmsTestResult testChannel(BmsChannel channel, int index, int measure, Object value) {
					return BmsTestResult.FAIL;
				}
			}, bms);
		});
		assertTrue(called[0]);
	}

	// testChannel(BmsChannel, int, int, Object)
	// THROUGHを返すとparseErrorは呼ばれずにBMS読み込みが続行され、当該データがBMSコンテンツに格納されないこと
	@Test
	public void testTestChannel_ArrayType_004() throws Exception {
		var called = new boolean[] { false };
		var bms =
				"#00007:AA\n" +
				"#00107:BB";
		var content = testHandler(new BmsLoadHandler() {
			@Override public boolean parseError(BmsScriptError error) {
				called[0] = true;
				return false;
			}
			@Override public BmsTestResult testChannel(BmsChannel channel, int index, int measure, Object value) {
				return BmsTestResult.THROUGH;
			}
		}, bms);
		assertFalse(called[0]);
		assertNull(content.getNote(7, 0, 0.0));
		assertNull(content.getNote(7, 1, 0.0));
	}

	// testChannel(BmsChannel, int, int, Object)
	// nullを返すとparseErrorが呼ばれ、エラー種別がPANICになること
	@Test
	public void testTestChannel_ArrayType_005() throws Exception {
		var called = new boolean[] { false };
		var bms =
				"#00007:AA";
		testHandler(new BmsLoadHandler() {
			@Override public boolean parseError(BmsScriptError error) {
				called[0] = true;
				assertEquals(error.getType(), BmsErrorType.PANIC);
				return true;
			}
			@Override public BmsTestResult testChannel(BmsChannel channel, int index, int measure, Object value) {
				return null;
			}
		}, bms);

		assertTrue(called[0]);
	}

	private static BmsContent testHandler(BmsLoadHandler handler, String bms) throws BmsException {
		var loader = new BmsStandardLoader()
				.setSpec(BmsTest.createTestSpec())
				.setHandler(handler)
				.setSyntaxErrorEnable(true)
				.setFixSpecViolation(false)
				.setIgnoreUnknownMeta(false)
				.setIgnoreUnknownChannel(false)
				.setIgnoreWrongData(false);
		return loader.load(bms);
	}
}
