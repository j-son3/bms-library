package com.lmt.lib.bms;

import static org.junit.Assert.*;

import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BmsTest {
	public static BmsSpec createTestSpec() {
		var b = new BmsSpecBuilder();

		b.addMeta(BmsMeta.single("#base", BmsType.INTEGER, "36", 0, true));
		b.addMeta(BmsMeta.single("#genre", BmsType.STRING, "", 0, true));
		b.addMeta(BmsMeta.single("#title", BmsType.STRING, "", 0, true));
		b.addMeta(BmsMeta.single("#subtitle", BmsType.STRING, "", 0, true));
		b.addMeta(BmsMeta.single("#artist", BmsType.STRING, "", 0, true));
		b.addMeta(BmsMeta.multiple("#subartist", BmsType.STRING, "", 0, true));
		b.addMeta(BmsMeta.single("#bpm", BmsType.FLOAT, "130.0", 0, true));
		b.addMeta(BmsMeta.indexed("#bpm", BmsType.FLOAT, "130.0", 0, true));
		b.addMeta(BmsMeta.indexed("#stop", BmsType.FLOAT, "0", 0, true));

		// Single Meta
		b.addMeta(BmsMeta.single("#sinteger", BmsType.INTEGER, "1", 1, false));
		b.addMeta(BmsMeta.single("#sinteger2", BmsType.INTEGER, "2", 2, false));
		b.addMeta(BmsMeta.single("#sinteger3", BmsType.INTEGER, "3", 3, false));
		b.addMeta(BmsMeta.single("#sfloat", BmsType.FLOAT, "1.23", 4, false));
		b.addMeta(BmsMeta.single("#sfloat2", BmsType.FLOAT, "4.56", 5, false));
		b.addMeta(BmsMeta.single("#sfloat3", BmsType.FLOAT, "7.89", 6, false));
		b.addMeta(BmsMeta.single("#sstring", BmsType.STRING, "hogehoge", 7, false));
		b.addMeta(BmsMeta.single("#sstring2", BmsType.STRING, "hagehage", 8, false));
		b.addMeta(BmsMeta.single("#sstring3", BmsType.STRING, "higehige", 9, false));
		b.addMeta(BmsMeta.single("#sbase16", BmsType.BASE16, "AB", 10, false));
		b.addMeta(BmsMeta.single("#sbase162", BmsType.BASE16, "CD", 11, false));
		b.addMeta(BmsMeta.single("#sbase163", BmsType.BASE16, "EF", 12, false));
		b.addMeta(BmsMeta.single("#sbase36", BmsType.BASE36, "XX", 13, false));
		b.addMeta(BmsMeta.single("#sbase362", BmsType.BASE36, "YY", 14, false));
		b.addMeta(BmsMeta.single("#sbase363", BmsType.BASE36, "ZZ", 15, false));
		b.addMeta(BmsMeta.single("#sbase62", BmsType.BASE62, "xx", 16, false));
		b.addMeta(BmsMeta.single("#sbase622", BmsType.BASE62, "yy", 17, false));
		b.addMeta(BmsMeta.single("#sbase623", BmsType.BASE62, "zz", 18, false));
		b.addMeta(BmsMeta.single("#sbase", BmsType.BASE, "xx", 19, false));
		b.addMeta(BmsMeta.single("#sbase2", BmsType.BASE, "yy", 20, false));
		b.addMeta(BmsMeta.single("#sbase3", BmsType.BASE, "zz", 21, false));
		b.addMeta(BmsMeta.single("#sarray16", BmsType.ARRAY16, "1122334455", 22, false));
		b.addMeta(BmsMeta.single("#sarray162", BmsType.ARRAY16, "66778899AA", 23, false));
		b.addMeta(BmsMeta.single("#sarray163", BmsType.ARRAY16, "BBCCDDEEFF", 24, false));
		b.addMeta(BmsMeta.single("#sarray36", BmsType.ARRAY36, "AABBCCDDEE", 25, false));
		b.addMeta(BmsMeta.single("#sarray362", BmsType.ARRAY36, "FFGGHHIIJJ", 26, false));
		b.addMeta(BmsMeta.single("#sarray363", BmsType.ARRAY36, "KKLLMMNNOO", 27, false));
		b.addMeta(BmsMeta.single("#sarray62", BmsType.ARRAY62, "aabbccddee", 28, false));
		b.addMeta(BmsMeta.single("#sarray622", BmsType.ARRAY62, "ffgghhiijj", 29, false));
		b.addMeta(BmsMeta.single("#sarray623", BmsType.ARRAY62, "kkllmmnnoo", 30, false));
		b.addMeta(BmsMeta.single("#sarray", BmsType.ARRAY, "aabbccddee", 31, false));
		b.addMeta(BmsMeta.single("#sarray2", BmsType.ARRAY, "ffgghhiijj", 32, false));
		b.addMeta(BmsMeta.single("#sarray3", BmsType.ARRAY, "kkllmmnnoo", 33, false));

		// Multiple Meta
		b.addMeta(BmsMeta.multiple("#minteger", BmsType.INTEGER, "1", 100, false));
		b.addMeta(BmsMeta.multiple("#minteger2", BmsType.INTEGER, "2", 101, false));
		b.addMeta(BmsMeta.multiple("#minteger3", BmsType.INTEGER, "3", 102, false));
		b.addMeta(BmsMeta.multiple("#mfloat", BmsType.FLOAT, "1.23", 103, false));
		b.addMeta(BmsMeta.multiple("#mfloat2", BmsType.FLOAT, "4.56", 104, false));
		b.addMeta(BmsMeta.multiple("#mfloat3", BmsType.FLOAT, "7.89", 105, false));
		b.addMeta(BmsMeta.multiple("#mstring", BmsType.STRING, "hogehoge", 106, false));
		b.addMeta(BmsMeta.multiple("#mstring2", BmsType.STRING, "hagehage", 107, false));
		b.addMeta(BmsMeta.multiple("#mstring3", BmsType.STRING, "higehige", 108, false));
		b.addMeta(BmsMeta.multiple("#mbase16", BmsType.BASE16, "AB", 109, false));
		b.addMeta(BmsMeta.multiple("#mbase162", BmsType.BASE16, "CD", 110, false));
		b.addMeta(BmsMeta.multiple("#mbase163", BmsType.BASE16, "EF", 111, false));
		b.addMeta(BmsMeta.multiple("#mbase36", BmsType.BASE36, "XX", 112, false));
		b.addMeta(BmsMeta.multiple("#mbase362", BmsType.BASE36, "YY", 113, false));
		b.addMeta(BmsMeta.multiple("#mbase363", BmsType.BASE36, "ZZ", 114, false));
		b.addMeta(BmsMeta.multiple("#mbase62", BmsType.BASE62, "xx", 115, false));
		b.addMeta(BmsMeta.multiple("#mbase622", BmsType.BASE62, "yy", 116, false));
		b.addMeta(BmsMeta.multiple("#mbase623", BmsType.BASE62, "zz", 117, false));
		b.addMeta(BmsMeta.multiple("#mbase", BmsType.BASE, "xx", 118, false));
		b.addMeta(BmsMeta.multiple("#mbase2", BmsType.BASE, "yy", 119, false));
		b.addMeta(BmsMeta.multiple("#mbase3", BmsType.BASE, "zz", 120, false));
		b.addMeta(BmsMeta.multiple("#marray16", BmsType.ARRAY16, "1122334455", 121, false));
		b.addMeta(BmsMeta.multiple("#marray162", BmsType.ARRAY16, "66778899AA", 122, false));
		b.addMeta(BmsMeta.multiple("#marray163", BmsType.ARRAY16, "BBCCDDEEFF", 123, false));
		b.addMeta(BmsMeta.multiple("#marray36", BmsType.ARRAY36, "AABBCCDDEE", 124, false));
		b.addMeta(BmsMeta.multiple("#marray362", BmsType.ARRAY36, "FFGGHHIIJJ", 125, false));
		b.addMeta(BmsMeta.multiple("#marray363", BmsType.ARRAY36, "KKLLMMNNOO", 126, false));
		b.addMeta(BmsMeta.multiple("#marray62", BmsType.ARRAY62, "aabbccddee", 127, false));
		b.addMeta(BmsMeta.multiple("#marray622", BmsType.ARRAY62, "ffgghhiijj", 128, false));
		b.addMeta(BmsMeta.multiple("#marray623", BmsType.ARRAY62, "kkllmmnnoo", 129, false));
		b.addMeta(BmsMeta.multiple("#marray", BmsType.ARRAY, "aabbccddee", 130, false));
		b.addMeta(BmsMeta.multiple("#marray2", BmsType.ARRAY, "ffgghhiijj", 131, false));
		b.addMeta(BmsMeta.multiple("#marray3", BmsType.ARRAY, "kkllmmnnoo", 132, false));

		// Indexed Meta
		b.addMeta(BmsMeta.indexed("#iinteger", BmsType.INTEGER, "1", 200, false));
		b.addMeta(BmsMeta.indexed("#iinteger2", BmsType.INTEGER, "2", 201, false));
		b.addMeta(BmsMeta.indexed("#iinteger3", BmsType.INTEGER, "3", 202, false));
		b.addMeta(BmsMeta.indexed("#ifloat", BmsType.FLOAT, "1.23", 203, false));
		b.addMeta(BmsMeta.indexed("#ifloat2", BmsType.FLOAT, "4.56", 204, false));
		b.addMeta(BmsMeta.indexed("#ifloat3", BmsType.FLOAT, "7.89", 205, false));
		b.addMeta(BmsMeta.indexed("#istring", BmsType.STRING, "hogehoge", 206, false));
		b.addMeta(BmsMeta.indexed("#istring2", BmsType.STRING, "hagehage", 207, false));
		b.addMeta(BmsMeta.indexed("#istring3", BmsType.STRING, "higehige", 208, false));
		b.addMeta(BmsMeta.indexed("#ibase16", BmsType.BASE16, "AB", 209, false));
		b.addMeta(BmsMeta.indexed("#ibase162", BmsType.BASE16, "CD", 210, false));
		b.addMeta(BmsMeta.indexed("#ibase163", BmsType.BASE16, "EF", 211, false));
		b.addMeta(BmsMeta.indexed("#ibase36", BmsType.BASE36, "XX", 212, false));
		b.addMeta(BmsMeta.indexed("#ibase362", BmsType.BASE36, "YY", 213, false));
		b.addMeta(BmsMeta.indexed("#ibase363", BmsType.BASE36, "ZZ", 214, false));
		b.addMeta(BmsMeta.indexed("#ibase62", BmsType.BASE62, "xx", 215, false));
		b.addMeta(BmsMeta.indexed("#ibase622", BmsType.BASE62, "yy", 216, false));
		b.addMeta(BmsMeta.indexed("#ibase623", BmsType.BASE62, "zz", 217, false));
		b.addMeta(BmsMeta.indexed("#ibase", BmsType.BASE, "xx", 218, false));
		b.addMeta(BmsMeta.indexed("#ibase2", BmsType.BASE, "yy", 219, false));
		b.addMeta(BmsMeta.indexed("#ibase3", BmsType.BASE, "zz", 220, false));
		b.addMeta(BmsMeta.indexed("#iarray16", BmsType.ARRAY16, "1122334455", 221, false));
		b.addMeta(BmsMeta.indexed("#iarray162", BmsType.ARRAY16, "66778899AA", 222, false));
		b.addMeta(BmsMeta.indexed("#iarray163", BmsType.ARRAY16, "BBCCDDEEFF", 223, false));
		b.addMeta(BmsMeta.indexed("#iarray36", BmsType.ARRAY36, "AABBCCDDEE", 224, false));
		b.addMeta(BmsMeta.indexed("#iarray362", BmsType.ARRAY36, "FFGGHHIIJJ", 225, false));
		b.addMeta(BmsMeta.indexed("#iarray363", BmsType.ARRAY36, "KKLLMMNNOO", 226, false));
		b.addMeta(BmsMeta.indexed("#iarray62", BmsType.ARRAY62, "aabbccddee", 227, false));
		b.addMeta(BmsMeta.indexed("#iarray622", BmsType.ARRAY62, "ffgghhiijj", 228, false));
		b.addMeta(BmsMeta.indexed("#iarray623", BmsType.ARRAY62, "kkllmmnnoo", 229, false));
		b.addMeta(BmsMeta.indexed("#iarray", BmsType.ARRAY, "aabbccddee", 230, false));
		b.addMeta(BmsMeta.indexed("#iarray2", BmsType.ARRAY, "ffgghhiijj", 231, false));
		b.addMeta(BmsMeta.indexed("#iarray3", BmsType.ARRAY, "kkllmmnnoo", 232, false));

		// 任意型
		b.addMeta(BmsMeta.object("#sobject", BmsUnit.SINGLE));
		b.addMeta(BmsMeta.object("#mobject", BmsUnit.MULTIPLE));
		b.addMeta(BmsMeta.object("#iobject", BmsUnit.INDEXED));

		// %で始まるメタ情報
		b.addMeta(BmsMeta.single("%url", BmsType.STRING, "", 300, false));
		b.addMeta(BmsMeta.single("%email", BmsType.STRING, "", 301, false));
		b.addMeta(BmsMeta.single("%sns", BmsType.STRING, "", 302, false));
		b.addMeta(BmsMeta.single("%age", BmsType.INTEGER, "0", 303, false));

		// Channel
		b.addChannel(new BmsChannel(1, BmsType.INTEGER, null, "100", false, false));
		b.addChannel(new BmsChannel(2, BmsType.FLOAT, null, "1.0", false, true));
		b.addChannel(new BmsChannel(3, BmsType.STRING, null, "foo", false, false));
		b.addChannel(new BmsChannel(4, BmsType.BASE16, null, "FF", false, false));
		b.addChannel(new BmsChannel(5, BmsType.BASE36, null, "ZZ", false, false));
		b.addChannel(new BmsChannel(6, BmsType.ARRAY16, null, "DDEEFF", false, false));
		b.addChannel(new BmsChannel(7, BmsType.ARRAY36, null, "XXYYZZ", false, true));
		b.addChannel(new BmsChannel(BmsInt.to36i("16"), BmsType.BASE62, null, "zz", false, false));
		b.addChannel(new BmsChannel(BmsInt.to36i("17"), BmsType.BASE, null, "zz", false, false));
		b.addChannel(new BmsChannel(BmsInt.to36i("18"), BmsType.ARRAY62, null, "xxyyzz", false, false));
		b.addChannel(new BmsChannel(BmsInt.to36i("19"), BmsType.ARRAY, null, "xxyyzz", false, false));

		b.addChannel(new BmsChannel(8, BmsType.ARRAY36, "#bpm", "00", false, true));
		b.addChannel(new BmsChannel(9, BmsType.ARRAY36, "#stop", "00", false, true));

		b.addChannel(new BmsChannel(BmsInt.to36i("0A"), BmsType.FLOAT, null, "1.0", false, false));

		b.addChannel(new BmsChannel(BmsInt.to36i("AA"), BmsType.ARRAY36, null, "00", false, true));
		b.addChannel(new BmsChannel(BmsInt.to36i("M1"), BmsType.INTEGER, null, "0", true, false));  // 重複可
		b.addChannel(new BmsChannel(BmsInt.to36i("M2"), BmsType.FLOAT, null, "0.0", true, false));  // 重複可
		b.addChannel(new BmsChannel(BmsInt.to36i("M3"), BmsType.STRING, null, "", true, false));  // 重複可
		b.addChannel(new BmsChannel(BmsInt.to36i("M4"), BmsType.BASE16, null, "00", true, false));  // 重複可
		b.addChannel(new BmsChannel(BmsInt.to36i("M5"), BmsType.BASE36, null, "00", true, false));  // 重複可
		b.addChannel(new BmsChannel(BmsInt.to36i("M6"), BmsType.ARRAY16, null, "", true, false));  // 重複可
		b.addChannel(new BmsChannel(BmsInt.to36i("M7"), BmsType.ARRAY36, null, "", true, false));  // 重複可
		b.addChannel(new BmsChannel(BmsInt.to36i("M8"), BmsType.BASE62, null, "00", true, false));  // 重複可
		b.addChannel(new BmsChannel(BmsInt.to36i("M9"), BmsType.BASE, null, "00", true, false));  // 重複可
		b.addChannel(new BmsChannel(BmsInt.to36i("MA"), BmsType.ARRAY62, null, "", true, false));  // 重複可
		b.addChannel(new BmsChannel(BmsInt.to36i("MB"), BmsType.ARRAY, null, "", true, false));  // 重複可
		b.addChannel(new BmsChannel(BmsInt.to36i("ZZ"), BmsType.ARRAY36, null, "00", false, true));

		b.addChannel(new BmsChannel(BmsInt.to36i("DC"), BmsType.INTEGER, null, "0", false, false));  // 重複テスト用
		b.addChannel(new BmsChannel(BmsInt.to36i("DD"), BmsType.INTEGER, null, "0", true, false));  // 重複テスト用
		b.addChannel(new BmsChannel(BmsInt.to36i("ED"), BmsType.ARRAY36, null, "00", false, false));  // 重複テスト用
		b.addChannel(new BmsChannel(BmsInt.to36i("EE"), BmsType.ARRAY36, null, "00", true, false));  // 重複テスト用

		b.addChannel(new BmsChannel(BmsSpec.USER_CHANNEL_MIN + 0, BmsType.OBJECT, null, null, false, false));  // 任意型

		b.addChannel(BmsChannel.user(BmsSpec.USER_CHANNEL_MIN + 6, BmsType.BASE16, null, "00", false));  // ユーザー16進数値
		b.addChannel(BmsChannel.user(BmsSpec.USER_CHANNEL_MIN + 7, BmsType.BASE36, null, "00", false));  // ユーザー36進数値
		b.addChannel(BmsChannel.user(BmsSpec.USER_CHANNEL_MIN + 8, BmsType.BASE62, null, "00", false));  // ユーザー62進数値
		b.addChannel(BmsChannel.user(BmsSpec.USER_CHANNEL_MIN + 9, BmsType.BASE, null, "00", false));  // ユーザー基数選択数値型
		b.addChannel(BmsChannel.user(BmsSpec.USER_CHANNEL_MIN + 10, BmsType.ARRAY16, null, "00", false));  // ユーザー16進配列
		b.addChannel(BmsChannel.user(BmsSpec.USER_CHANNEL_MIN + 11, BmsType.ARRAY36, null, "00", false));  // ユーザー36進配列
		b.addChannel(BmsChannel.user(BmsSpec.USER_CHANNEL_MIN + 12, BmsType.ARRAY62, null, "00", false));  // ユーザー62進配列
		b.addChannel(BmsChannel.user(BmsSpec.USER_CHANNEL_MIN + 13, BmsType.ARRAY, null, "00", false));  // ユーザー基数選択数値配列型

		b.setInitialBpmMeta("#bpm");
		b.setBaseChangerMeta("#base");
		b.setLengthChannel(2);
		b.setBpmChannel(8);
		b.setStopChannel(9);

		return b.create();
	}

	public static BmsContent loadContent(String...bms) throws Exception {
		return loadContent(l -> l, e -> fail(e.toString()), bms);
	}

	public static BmsContent loadContent(UnaryOperator<BmsLoader> loaderConfig, Consumer<BmsScriptError> errAction,
			String...bms) throws Exception {
		var handler = new BmsLoadHandler() {
			@Override public boolean parseError(BmsScriptError error) { errAction.accept(error); return true; }
		};
		var loader = loaderConfig.apply(new BmsStandardLoader()
				.setSpec(createTestSpec())
				.setHandler(handler)
				.setStrictly(true));
		return loader.load(Stream.of(bms).collect(Collectors.joining("\n")));
	}

	public static class AnyType1 {
		int a, b;
		AnyType1(int _a, int _b) { a = _a; b = _b; }
		@Override public boolean equals(Object o) {
			return (o instanceof AnyType1) && (((AnyType1)o).a == a) && (((AnyType1)o).b == b);
		}
	}

	public static class AnyType2 {
		long a, b, c;
		AnyType2(long _a, long _b, long _c) { a = _a; b = _b; c = _c; }
		@Override public boolean equals(Object o) {
			return (o instanceof AnyType2) && (((AnyType2)o).a == a) && (((AnyType2)o).b == b) && (((AnyType2)o).c == c);
		}
	}
}
