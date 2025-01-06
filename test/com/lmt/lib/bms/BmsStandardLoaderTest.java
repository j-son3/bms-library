package com.lmt.lib.bms;

import static org.junit.Assert.*;

import java.nio.file.Path;
import java.util.ArrayList;

import org.junit.Test;

public class BmsStandardLoaderTest extends BmsLoaderTestBase {
	@Override
	public Path testDataDirectory() {
		return Path.of("test", "com", "lmt", "lib", "bms", "testdata", "bmsstandardloader");
	}

	@Override
	public String testDataExtension() {
		return "bms";
	}

	// isStandard()
	// trueを返すこと
	@Test
	public void testIsStandard() {
		var l = new BmsStandardLoader();
		assertTrue(l.isStandard());
	}

	// isBinaryFormat()
	// falseを返すこと
	@Test
	public void testIsBinaryFormat() {
		var l = new BmsStandardLoader();
		assertFalse(l.isBinaryFormat());
	}

	// load(Any)
	// BMS宣言：先頭行にBMS宣言存在、BMS宣言は左から順に解析し、その順番でBMSコンテンツにセットされる
	@Test
	public void testParser_BmsDecl_001() {
		var c = load();
		var decls = c.getDeclarations();
		var keys = new ArrayList<String>();
		var values = new ArrayList<String>();
		decls.forEach((k, v) -> {
			keys.add(k);
			values.add(v);
		});
		assertEquals(5, decls.size());
		assertEquals("hoge", keys.get(0)); assertEquals("10", values.get(0));
		assertEquals("hage", keys.get(1)); assertEquals("20", values.get(1));
		assertEquals("hige", keys.get(2)); assertEquals("30", values.get(2));
		assertEquals("mykey", keys.get(3)); assertEquals("foo", values.get(3));
		assertEquals("secret", keys.get(4)); assertEquals("bar", values.get(4));
	}

	// load(Any)
	// BMS宣言：先頭行にBMS宣言存在、BMS宣言の要素0件
	@Test
	public void testParser_BmsDecl_002() {
		var c = load();
		assertEquals(0, c.getDeclarations().size());
	}

	// load(Any)
	// BMS宣言：先頭行のBMS宣言の文言が間違っている
	@Test
	public void testParser_BmsDecl_003() {
		var c = load();
		assertEquals(0, c.getDeclarations().size());
	}

	// load(Any)
	// BMS宣言：2行目にBMS宣言がある（先頭行ではないのでただのコメントとして認識される）
	@Test
	public void testParser_BmsDecl_004() {
		var c = load();
		assertEquals(0, c.getDeclarations().size());
	}

	// load(Any)
	// BMS解析の動作概要：行頭の半角スペースとタブは無視される
	@Test
	public void testParser_ParseOverview_000() {
		var c = load();
		assertEquals("value1", c.getDeclaration("mykey1"));
		assertEquals("value2", c.getDeclaration("mykey2"));
		assertEquals("Love Song", c.getSingleMeta("#title"));
		assertEquals(Integer.parseInt("AA", 36), c.getNote(7, 0, 0.0).getValue());
		assertEquals(Integer.parseInt("BB", 36), c.getNote(7, 0, 48.0).getValue());
		assertEquals(Integer.parseInt("CC", 36), c.getNote(7, 0, 96.0).getValue());
		assertEquals(Integer.parseInt("DD", 36), c.getNote(7, 0, 144.0).getValue());
	}

	// load(Any)
	// BMS解析の動作概要：半角スペースおよびタブのみの行は空行として読み飛ばす
	@Test
	public void testParser_ParseOverview_010() {
		load();
	}

	// load(Any)
	// BMS解析の動作概要：改行コードCR/LF/CRLFが混在したBMSを正常に解析できる
	@Test
	public void testParser_ParseOverview_020() {
		var c = load();
		assertEquals("value1", c.getDeclaration("mykey1"));
		assertEquals("value2", c.getDeclaration("mykey2"));
		assertEquals("Love Song", c.getSingleMeta("#title"));
		assertEquals("NoName", c.getSingleMeta("#artist"));
		assertEquals(123L, (long)c.getMeasureValue(1, 0));
		assertEquals(Integer.parseInt("11", 36), c.getNote(7, 0, 0.0).getValue());
		assertEquals(Integer.parseInt("22", 36), c.getNote(7, 0, 96.0).getValue());
	}

	// load(Any)
	// BMS解析の動作概要：「;」で始まる行はコメント
	@Test
	public void testParser_ParseOverview_030() {
		var c = load();
		assertNotEquals(true, c.containsDeclaration("mykey"));
		assertNotEquals("My Song", c.getSingleMeta("#title"));
		assertNotEquals("NoName", c.getSingleMeta("#artist"));
		assertNotEquals(123L, (long)c.getMeasureValue(1, 0));
		assertNull(c.getNote(7, 0, 0));
		assertNull(c.getNote(7, 0, 96));
	}

	// load(Any)
	// BMS解析の動作概要：「//」で始まる行はコメント
	@Test
	public void testParser_ParseOverview_031() {
		var c = load();
		assertNotEquals(true, c.containsDeclaration("mykey"));
		assertNotEquals("My Song", c.getSingleMeta("#title"));
		assertNotEquals("NoName", c.getSingleMeta("#artist"));
		assertNotEquals(123L, (long)c.getMeasureValue(1, 0));
		assertNull(c.getNote(7, 0, 0));
		assertNull(c.getNote(7, 0, 96));
	}

	// load(Any)
	// BMS解析の動作概要：行コメントの後で/*を書いても複数行コメントにはならない
	@Test
	public void testParser_ParseOverview_032() {
		load();
	}

	// load(Any)
	// BMS解析の動作概要：複数行コメントの間に書かれた定義は全て無視される
	@Test
	public void testParser_ParseOverview_040() {
		var c = load();
		assertNotEquals(true, c.containsDeclaration("mykey"));
		assertNotEquals("My Song", c.getSingleMeta("#title"));
		assertNotEquals("NoName", c.getSingleMeta("#artist"));
		assertNotEquals(123L, (long)c.getMeasureValue(1, 0));
		assertNull(c.getNote(7, 0, 0));
		assertNull(c.getNote(7, 0, 96));
	}

	// load(Any)
	// BMS解析の動作概要：複数行コメントを閉じた後、もう一度複数行コメントを書く
	@Test
	public void testParser_ParseOverview_041() {
		var c = load();
		assertNotEquals(true, c.containsDeclaration("mykey"));
		assertNotEquals("My Song", c.getSingleMeta("#title"));
		assertNotEquals("NoName", c.getSingleMeta("#artist"));
		assertNotEquals(123L, (long)c.getMeasureValue(1, 0));
		assertNull(c.getNote(7, 0, 0));
		assertNull(c.getNote(7, 0, 96));
	}

	// load(Any)
	// BMS解析の動作概要：複数行コメント内で/*を書いても無効である
	@Test
	public void testParser_ParseOverview_042() {
		load();
	}

	// load(Any)
	// BMS解析の動作概要：行が「*/」で終わっていないのは複数行コメント終了と見なさない（右側空白を削った状態でチェック）
	@Test
	public void testParser_ParseOverview_043() {
		load(err -> {
			assertEquals(BmsErrorType.COMMENT_NOT_CLOSED, err.getType());
			assertEquals(0, err.getLineNumber());
			assertEquals("", err.getLine());
			return true;
		});
	}

	// load(Any)
	// BMS解析の動作概要：複数行コメントが閉じられずにBMSが終了したらCOMMENT_NOT_CLOSED
	@Test
	public void testParser_ParseOverview_044() {
		load(err -> {
			assertEquals(BmsErrorType.COMMENT_NOT_CLOSED, err.getType());
			assertEquals(6, err.getLineNumber());
			assertEquals("", err.getLine());
			return true;
		});
	}

	// load(Any)
	// BMS解析の動作概要：#で開始した行をメタ情報とする
	@Test
	public void testParser_ParseOverview_050() {
		var c = load();
		assertEquals("My Song", c.getSingleMeta("#title"));
		assertEquals("NoName", c.getSingleMeta("#artist"));
		assertEquals("Eurobeat", c.getSingleMeta("#genre"));
		assertEquals(180.0, (double)c.getSingleMeta("#bpm"), 0.0);
	}

	// load(Any)
	// BMS解析の動作概要：#で開始したメタ情報名称1文字目がアルファベットではない（UNKNOWN_META）
	@Test
	public void testParser_ParseOverview_051() {
		int[] errCount = {0};
		load(err -> {
			errCount[0]++;
			assertEquals(BmsErrorType.UNKNOWN_META, err.getType());
			assertNotEquals(0, err.getLineNumber());
			assertNotEquals("", err.getLine());
			return true;
		});
		assertEquals(3, errCount[0]);
	}

	// load(Any)
	// BMS解析の動作概要：#で開始したメタ情報の名称が0文字（UNKNOWN_META）
	@Test
	public void testParser_ParseOverview_052() {
		load(err -> {
			assertEquals(BmsErrorType.UNKNOWN_META, err.getType());
			assertEquals(1, err.getLineNumber());
			assertEquals("# My Song", err.getLine());
			return true;
		});
	}

	// load(Any)
	// BMS解析の動作概要：#で開始したメタ情報名称2文字目以降に、半角英数字、_、/以外の文字（UNKNOWN_META）
	@Test
	public void testParser_ParseOverview_053() {
		int[] errCount = {0};
		load(err -> {
			errCount[0]++;
			assertEquals(BmsErrorType.UNKNOWN_META, err.getType());
			assertNotEquals(0, err.getLineNumber());
			assertNotEquals("", err.getLine());
			return true;
		});
		assertEquals(4, errCount[0]);
	}

	// load(Any)
	// BMS解析の動作概要：%で開始した行をメタ情報とする
	@Test
	public void testParser_ParseOverview_054() {
		var c = load();
		assertEquals("http://www.lm-t.com/", c.getSingleMeta("%url"));
		assertEquals("j-son@lm-t.com", c.getSingleMeta("%email"));
		assertEquals("my_twitter_account", c.getSingleMeta("%sns"));
		assertEquals(45L, (long)c.getSingleMeta("%age"));
	}

	// load(Any)
	// BMS解析の動作概要：%で開始したメタ情報名称1文字目がアルファベットではない（UNKNOWN_META）
	@Test
	public void testParser_ParseOverview_055() {
		int[] errCount = {0};
		load(err -> {
			errCount[0]++;
			assertEquals(BmsErrorType.UNKNOWN_META, err.getType());
			assertNotEquals(0, err.getLineNumber());
			assertNotEquals("", err.getLine());
			return true;
		});
		assertEquals(4, errCount[0]);
	}

	// load(Any)
	// BMS解析の動作概要：%で開始したメタ情報の名称が0文字（UNKNOWN_META）
	@Test
	public void testParser_ParseOverview_056() {
		load(err -> {
			assertEquals(BmsErrorType.UNKNOWN_META, err.getType());
			assertEquals(1, err.getLineNumber());
			assertEquals("% My Song", err.getLine());
			return true;
		});
	}

	// load(Any)
	// BMS解析の動作概要：%で開始したメタ情報名称2文字目以降に、半角英数字、_、/以外の文字（UNKNOWN_META）
	@Test
	public void testParser_ParseOverview_057() {
		int[] errCount = {0};
		load(err -> {
			errCount[0]++;
			assertEquals(BmsErrorType.UNKNOWN_META, err.getType());
			assertNotEquals(0, err.getLineNumber());
			assertNotEquals("", err.getLine());
			return true;
		});
		assertEquals(4, errCount[0]);
	}

	// load(Any)
	// BMS解析の動作概要：3文字の半角数字で始まり、次に2文字の半角英数字、更にその次が":"で始まる行をタイムライン要素の定義と見なす
	@Test
	public void testParser_ParseOverview_060() {
		var c = load();

		assertEquals(123L, (long)c.getMeasureValue(1, 0));

		assertEquals(Integer.parseInt("01", 36), c.getNote(Integer.parseInt("AA", 36), 0, 0.0).getValue());
		assertEquals(Integer.parseInt("02", 36), c.getNote(Integer.parseInt("AA", 36), 0, 96.0).getValue());
		assertEquals(Integer.parseInt("03", 36), c.getNote(Integer.parseInt("AA", 36), 1, 48.0).getValue());
		assertEquals(Integer.parseInt("04", 36), c.getNote(Integer.parseInt("AA", 36), 1, 144.0).getValue());
		assertEquals(Integer.parseInt("05", 36), c.getNote(Integer.parseInt("AA", 36), 3, 0.0).getValue());
		assertEquals(Integer.parseInt("06", 36), c.getNote(Integer.parseInt("AA", 36), 3, 48.0).getValue());
		assertEquals(Integer.parseInt("07", 36), c.getNote(Integer.parseInt("AA", 36), 6, 96.0).getValue());
		assertEquals(Integer.parseInt("08", 36), c.getNote(Integer.parseInt("AA", 36), 6, 144.0).getValue());

		assertEquals(Integer.parseInt("XX", 36), c.getNote(Integer.parseInt("ZZ", 36), 2, 0.0).getValue());
		assertEquals(Integer.parseInt("YY", 36), c.getNote(Integer.parseInt("ZZ", 36), 100, 0.0).getValue());
		assertEquals(Integer.parseInt("ZZ", 36), c.getNote(Integer.parseInt("ZZ", 36), 999, 0.0).getValue());
	}

	// load(Any)
	// BMS解析の動作概要：チャンネル定義行に":"がない（SYNTAX）
	@Test
	public void testParser_ParseOverview_061() {
		load(err -> {
			assertEquals(BmsErrorType.SYNTAX, err.getType());
			assertEquals(1, err.getLineNumber());
			assertEquals("#00001123", err.getLine());
			return true;
		});
	}

	// load(Any)
	// BMS解析の動作概要：タイムライン要素のチャンネル番号が1文字足りない（SYNTAX）
	@Test
	public void testParser_ParseOverview_062() {
		load(err -> {
			assertEquals(BmsErrorType.SYNTAX, err.getType());
			assertEquals(1, err.getLineNumber());
			assertEquals("#0007:AA00BB00", err.getLine());
			return true;
		});
	}

	// load(Any)
	// BMS解析の動作概要：タイムライン要素のチャンネル番号が1文字多い（SYNTAX）
	@Test
	public void testParser_ParseOverview_063() {
		load(err -> {
			assertEquals(BmsErrorType.SYNTAX, err.getType());
			assertEquals(1, err.getLineNumber());
			assertEquals("#0000ZZ:00CC00DD00EE00FF", err.getLine());
			return true;
		});
	}

	// load(Any)
	// BMS解析の動作概要：タイムライン要素のチャンネル番号で英小文字を使用（正常）
	@Test
	public void testParser_ParseOverview_064() {
		var c = load();
		assertEquals(Integer.parseInt("11", 36), c.getNote(Integer.parseInt("AA", 36), 3, 0.0).getValue());
		assertEquals(Integer.parseInt("33", 36), c.getNote(Integer.parseInt("AA", 36), 3, 96.0).getValue());
	}

	// load(Any)
	// メタ情報解析：値の文字列右側の半角空白文字は消去されて解析される（全型テストする）
	@Test
	public void testParser_ParseMeta_001() {
		var c = load();
		assertEquals(3456L, (long)c.getSingleMeta("#sinteger"));
		assertEquals(234.987, (double)c.getSingleMeta("#sfloat"), 0.00001);
		assertEquals("Text", c.getSingleMeta("#sstring"));
		assertEquals((long)BmsInt.to16i("AB"), (long)c.getSingleMeta("#sbase16"));
		assertEquals((long)BmsInt.to36i("XY"), (long)c.getSingleMeta("#sbase36"));
		assertEquals((long)BmsInt.to62i("aZ"), (long)c.getSingleMeta("#sbase62"));
		assertEquals((long)BmsInt.to36i("GG"), (long)c.getSingleMeta("#sbase")); // デフォルトの基数
		assertEquals("11009900BB00FF00", c.getSingleMeta("#sarray16").toString());
		assertEquals("ZZGG000088220000", c.getSingleMeta("#sarray36").toString());
		assertEquals("AAbb0000yyZZ0000", c.getSingleMeta("#sarray62").toString());
		assertEquals("CCDD0000WWXX0000", c.getSingleMeta("#sarray").toString()); // デフォルトの基数
	}

	// load(Any)
	// メタ情報解析：索引付きメタ情報のインデックス解析（正常）
	@Test
	public void testParser_ParseMeta_010() {
		var c = load();
		assertEquals(100L, (long)c.getIndexedMeta("#iinteger", Integer.parseInt("00", 36)));
		assertEquals(200L, (long)c.getIndexedMeta("#iinteger", Integer.parseInt("AA", 36)));
		assertEquals(300L, (long)c.getIndexedMeta("#iinteger", Integer.parseInt("XX", 36)));
		assertEquals(400L, (long)c.getIndexedMeta("#iinteger", Integer.parseInt("ZZ", 36)));
	}

	// load(Any)
	// メタ情報解析：索引付きメタ情報のインデックス値が英小文字記載（解析OK）
	@Test
	public void testParser_ParseMeta_011() {
		var c = load();
		assertEquals(333L, (long)c.getIndexedMeta("#iinteger", Integer.parseInt("0Z", 36)));
		assertEquals(666L, (long)c.getIndexedMeta("#iinteger", Integer.parseInt("Z0", 36)));
		assertEquals(999L, (long)c.getIndexedMeta("#iinteger", Integer.parseInt("AB", 36)));
	}

	// load(Any)
	// メタ情報解析：索引付きメタ情報のインデックス値の文字数が1文字または3文字（UNKNOWN_META）
	@Test
	public void testParser_ParseMeta_012() {
		load(err -> {
			switch (err.getLineNumber()) {
			case 1:
				assertEquals(BmsErrorType.UNKNOWN_META, err.getType());
				assertEquals("#IINTEGERa 1", err.getLine());
				break;
			case 2:
				assertEquals(BmsErrorType.UNKNOWN_META, err.getType());
				assertEquals("#IINTEGER0xx 2", err.getLine());
				break;
			default:
				fail(String.format("Line number is unexpected value: %d", err.getLineNumber()));
			}
			return true;
		});
	}

	// load(Any)
	// メタ情報解析：索引付きメタ情報のインデックス値に不正な文字を使用（WRONG_DATA）
	@Test
	public void testParser_ParseMeta_013() {
		load(err -> {
			assertEquals(BmsErrorType.WRONG_DATA, err.getType());
			assertEquals(1, err.getLineNumber());
			assertEquals("#IINTEGER0@ 1000", err.getLine());
			return true;
		});
	}

	// load(Any)
	// メタ情報解析：BMS仕様に規定のない名称（UNKNOWN_META）
	@Test
	public void testParser_ParseMeta_020() {
		load(err -> {
			assertEquals(BmsErrorType.UNKNOWN_META, err.getType());
			assertEquals(2, err.getLineNumber());
			assertEquals("#BADMETANAME Text", err.getLine());
			return true;
		});
	}

	// load(Any)
	// メタ情報解析：BMS仕様にある名称＋余分な文字（UNKNOWN_META）
	@Test
	public void testParser_ParseMeta_021() {
		load(err -> {
			assertEquals(BmsErrorType.UNKNOWN_META, err.getType());
			assertEquals(3, err.getLineNumber());
			assertEquals("#SSTRINGXX Text", err.getLine());
			return true;
		});
	}

	// load(Any)
	// メタ情報解析：BMS仕様にある名称（ただし英大小文字混在）（解析OK）
	@Test
	public void testParser_ParseMeta_022() {
		var c = load();
		assertEquals("Text", c.getSingleMeta("#sstring"));
		assertEquals(2850L, (long)c.getMultipleMeta("#minteger", 0));
		assertEquals(111.222, (double)c.getIndexedMeta("#ifloat", Integer.parseInt("ZZ", 36)), 0.00001);
	}

	// load(Any)
	// メタ情報解析：メタ情報の値がBMS仕様の記述書式に適合しない（WRONG_DATA）（全型チェックする）
	@Test
	public void testParser_ParseMeta_030() {
		var occurs = new boolean[7];
		load(err -> {
			assertEquals(BmsErrorType.WRONG_DATA, err.getType());
			switch (err.getLineNumber()) {
			case 1: assertEquals("#SINTEGER nan", err.getLine()); break;
			case 2: assertEquals("#SFLOAT 1.2.3", err.getLine()); break;
			case 4: assertEquals("#SBASE16 xx", err.getLine()); break;
			case 5: assertEquals("#SBASE36 ??", err.getLine()); break;
			case 6: assertEquals("#SARRAY16 0011yy2233", err.getLine()); break;
			case 7: assertEquals("#SARRAY36 AAZZ**YYBB", err.getLine()); break;
			default: fail(String.format("Line number is unexpected value: %d", err.getLineNumber())); break;
			}
			occurs[err.getLineNumber() - 1] = true;
			return true;
		});
		assertArrayEquals(new boolean[]{ true, true, false, true, true, true, true }, occurs);
	}

	// load(Any)
	// チャンネル解析：BMS仕様に規定のないチャンネル番号（UNKNOWN_CHANNEL）
	@Test
	public void testParser_ParseChannel_000() {
		load(err -> {
			assertEquals(BmsErrorType.UNKNOWN_CHANNEL, err.getType());
			assertEquals(1, err.getLineNumber());
			assertEquals("#000JS:11223344", err.getLine());
			return true;
		});
	}

	// load(Any)
	// チャンネル解析：タイムライン要素の記述書式がBMS仕様の規定に適合しない（WRONG_DATA）（全型チェックする）
	@Test
	public void testParser_ParseChannel_010() {
		var occurs = new boolean[7];
		load(err -> {
			assertEquals(BmsErrorType.WRONG_DATA, err.getType());
			switch (err.getLineNumber()) {
			case 1: assertEquals("#00001:nan", err.getLine()); break;
			case 2: assertEquals("#00002:1.2.3", err.getLine()); break;
			case 4: assertEquals("#00004:xx", err.getLine()); break;
			case 5: assertEquals("#00005:??", err.getLine()); break;
			case 6: assertEquals("#00006:0011yy2233", err.getLine()); break;
			case 7: assertEquals("#00007:AAZZ**YYBB", err.getLine()); break;
			default: fail(String.format("Line number is unexpected value: %d", err.getLineNumber())); break;
			}
			occurs[err.getLineNumber() - 1] = true;
			return true;
		});
		assertArrayEquals(new boolean[]{ true, true, false, true, true, true, true }, occurs);
	}

	// load(Any)
	// チャンネル解析：重複不許可チャンネルで、同小節番号で重複定義（2件目のタイムライン要素がREDEFINE）
	@Test
	public void testParser_ParseChannel_020() {
		var c = load(err -> {
			assertEquals(BmsErrorType.REDEFINE, err.getType());
			assertEquals(2, err.getLineNumber());
			assertEquals("#00001:25100", err.getLine());
			return true;
		});
		assertEquals(19800L, (long)c.getMeasureValue(1, 0));
	}

	// load(Any)
	// チャンネル解析：重複許可チャンネルで、同小節番号で最大重複個数を超過（WRONG_DATA）
	@Test
	public void testParser_ParseChannel_030() {
		var c = load(err -> {
			assertEquals(BmsErrorType.WRONG_DATA, err.getType());
			assertEquals(32769, err.getLineNumber());
			assertEquals("#000DD:789", err.getLine());
			return true;
		});
		assertEquals(32768, c.getChannelDataCount(Integer.parseInt("DD", 36), 0));
	}

	// load(Any)
	// チャンネル解析：ノートの要素数がその小節の総刻み数を超過（WRONG_DATA）
	@Test
	public void testParser_ParseChannel_040() {
		load(err -> {
			assertEquals(BmsErrorType.WRONG_DATA, err.getType());
			assertEquals(1, err.getLineNumber());
			assertEquals("#00007:00112233445566778899AABBCCDDEEFF00112233445566778899AABBCCDDEEFF00112233445566778899AABBCCDDEEFF00112233445566778899AABBCCDDEEFF00112233445566778899AABBCCDDEEFF00112233445566778899AABBCCDDEEFF00112233445566778899AABBCCDDEEFF00112233445566778899AABBCCDDEEFF00112233445566778899AABBCCDDEEFF00112233445566778899AABBCCDDEEFF00112233445566778899AABBCCDDEEFF00112233445566778899AABBCCDDEEFF00", err.getLine());
			return true;
		});
	}

	// load(Any)
	// チャンネル解析：ノートの要素数が0（WRONG_DATA）
	@Test
	public void testParser_ParseChannel_041() {
		load(err -> {
			assertEquals(BmsErrorType.WRONG_DATA, err.getType());
			assertEquals(1, err.getLineNumber());
			assertEquals("#00007:", err.getLine());
			return true;
		});
	}

	// load(Any)
	// ノート00はNoteデータとしては取り込まれない
	@Test
	public void testParser_ParseChannel_042() {
		var c = load();

		// 一定間隔で00が登場。それらがBmsNoteとしてBmsContentに取り込まれないこと
		var l = c.listNotes(n -> n.getChannel() == 7 && n.getMeasure() == 0);
		assertEquals(4, l.size());
		assertEquals(0, l.get(0).getTick(), 0.0);
		assertEquals(1, l.get(0).getValue());
		assertEquals(48, l.get(1).getTick(), 0.0);
		assertEquals(2, l.get(1).getValue());
		assertEquals(96, l.get(2).getTick(), 0.0);
		assertEquals(3, l.get(2).getValue());
		assertEquals(144, l.get(3).getTick(), 0.0);
		assertEquals(4, l.get(3).getValue());

		// 中間の00が取り込まれないこと
		l = c.listNotes(n -> n.getChannel() == 7 && n.getMeasure() == 1);
		assertEquals(4, l.size());
		assertEquals(0, l.get(0).getTick(), 0.0);
		assertEquals(1, l.get(0).getValue());
		assertEquals(24, l.get(1).getTick(), 0.0);
		assertEquals(2, l.get(1).getValue());
		assertEquals(144, l.get(2).getTick(), 0.0);
		assertEquals(7, l.get(2).getValue());
		assertEquals(168, l.get(3).getTick(), 0.0);
		assertEquals(8, l.get(3).getValue());

		// 両端の00が取り込まれないこと
		l = c.listNotes(n -> n.getChannel() == 7 && n.getMeasure() == 2);
		assertEquals(4, l.size());
		assertEquals(48, l.get(0).getTick(), 0.0);
		assertEquals(3, l.get(0).getValue());
		assertEquals(72, l.get(1).getTick(), 0.0);
		assertEquals(4, l.get(1).getValue());
		assertEquals(96, l.get(2).getTick(), 0.0);
		assertEquals(5, l.get(2).getValue());
		assertEquals(120, l.get(3).getTick(), 0.0);
		assertEquals(6, l.get(3).getValue());
	}
}
