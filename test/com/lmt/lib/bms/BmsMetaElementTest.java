package com.lmt.lib.bms;

import static org.junit.Assert.*;

import org.junit.Test;

public class BmsMetaElementTest {
	// getMeta()
	// 設定したメタ情報を返すこと
	@Test
	public void testGetMeta() {
		var m = BmsMeta.single("#m", BmsType.STRING, "", 0, false);
		var e = new BmsMetaElement(m, 0, null);
		assertSame(m, e.getMeta());
	}

	// getName()
	// 設定したメタ情報の名前を返すこと
	@Test
	public void testGetName() {
		var name = "#test";
		var e = new BmsMetaElement(BmsMeta.single(name, BmsType.STRING, "", 0, false), 0, null);
		assertEquals(name, e.getName());
	}

	// getUnit()
	// 設定したメタ情報の構成単位を返すこと
	@Test
	public void testGetUnit() {
		var single = new BmsMetaElement(BmsMeta.single("#m", BmsType.STRING, "", 0, false), 0, null);
		var multiple = new BmsMetaElement(BmsMeta.multiple("#m", BmsType.STRING, "", 0, false), 0, null);
		var indexed = new BmsMetaElement(BmsMeta.indexed("#m", BmsType.STRING, "", 0, false), 0, null);
		assertEquals(BmsUnit.SINGLE, single.getUnit());
		assertEquals(BmsUnit.MULTIPLE, multiple.getUnit());
		assertEquals(BmsUnit.INDEXED, indexed.getUnit());
	}

	// getType()
	// 設定したメタ情報のデータ型を返すこと
	@Test
	public void testGetType() {
		var intT = new BmsMetaElement(BmsMeta.single("#m", BmsType.INTEGER, "0", 0, false), 0, null);
		var fltT = new BmsMetaElement(BmsMeta.single("#m", BmsType.FLOAT, "0", 0, false), 0, null);
		var strT = new BmsMetaElement(BmsMeta.single("#m", BmsType.STRING, "", 0, false), 0, null);
		var b16T = new BmsMetaElement(BmsMeta.single("#m", BmsType.BASE16, "00", 0, false), 0, null);
		var b36T = new BmsMetaElement(BmsMeta.single("#m", BmsType.BASE36, "00", 0, false), 0, null);
		var a16T = new BmsMetaElement(BmsMeta.single("#m", BmsType.ARRAY16, "00", 0, false), 0, null);
		var a36T = new BmsMetaElement(BmsMeta.single("#m", BmsType.ARRAY36, "00", 0, false), 0, null);
		var objT = new BmsMetaElement(BmsMeta.object("#m", BmsUnit.SINGLE), 0, null);
		assertEquals(BmsType.INTEGER, intT.getType());
		assertEquals(BmsType.FLOAT, fltT.getType());
		assertEquals(BmsType.STRING, strT.getType());
		assertEquals(BmsType.BASE16, b16T.getType());
		assertEquals(BmsType.BASE36, b36T.getType());
		assertEquals(BmsType.ARRAY16, a16T.getType());
		assertEquals(BmsType.ARRAY36, a36T.getType());
		assertEquals(BmsType.OBJECT, objT.getType());
	}

	// getIndex()
	// 設定したインデックス値を返すこと
	@Test
	public void testGetIndex() {
		var idxMinus = new BmsMetaElement(BmsMeta.multiple("#m", BmsType.STRING, "", 0, false), -1, null);
		var idxNormal = new BmsMetaElement(BmsMeta.multiple("#m", BmsType.STRING, "", 0, false), 10, null);
		var idxMax = new BmsMetaElement(BmsMeta.multiple("#m", BmsType.STRING, "", 0, false), Integer.MAX_VALUE, null);
		assertEquals(-1, idxMinus.getIndex());
		assertEquals(10, idxNormal.getIndex());
		assertEquals(Integer.MAX_VALUE, idxMax.getIndex());
	}

	// getDefaultAsLong()
	// long型に変換したデフォルト値を返すこと
	@Test
	public void testGetDefaultAsLong_Success() {
		var intT = new BmsMetaElement(BmsMeta.single("#m", BmsType.INTEGER, "100", 0, false), 0, null);
		var fltT = new BmsMetaElement(BmsMeta.single("#m", BmsType.FLOAT, "200.345", 0, false), 0, null);
		var b16T = new BmsMetaElement(BmsMeta.single("#m", BmsType.BASE16, "AB", 0, false), 0, null);
		var b36T = new BmsMetaElement(BmsMeta.single("#m", BmsType.BASE36, "XY", 0, false), 0, null);
		assertEquals(100L, intT.getDefaultAsLong());
		assertEquals(200L, fltT.getDefaultAsLong());
		assertEquals(BmsInt.to16i("AB"), b16T.getDefaultAsLong());
		assertEquals(BmsInt.to36i("XY"), b36T.getDefaultAsLong());
	}

	// getDefaultAsLong()
	// ClassCastException メタ情報のデフォルト値をlong型に変換できない
	@Test
	public void testGetDefaultAsLong_Uncastable() {
		var strT = new BmsMetaElement(BmsMeta.single("#m", BmsType.STRING, "", 0, false), 0, null);
		var a16T = new BmsMetaElement(BmsMeta.single("#m", BmsType.ARRAY16, "00", 0, false), 0, null);
		var a36T = new BmsMetaElement(BmsMeta.single("#m", BmsType.ARRAY36, "00", 0, false), 0, null);
		var ex = ClassCastException.class;
		assertThrows(ex, () -> strT.getDefaultAsLong());
		assertThrows(ex, () -> a16T.getDefaultAsLong());
		assertThrows(ex, () -> a36T.getDefaultAsLong());
	}

	// getDefaultAsLong()
	// NullPointerException 任意型メタ情報で当メソッドを実行した
	@Test
	public void testGetDefaultAsLong_ObjectType() {
		var objT = new BmsMetaElement(BmsMeta.object("#m", BmsUnit.SINGLE), 0, null);
		assertThrows(NullPointerException.class, () -> objT.getDefaultAsLong());
	}

	// getDefaultAsDouble()
	// double型に変換したデフォルト値を返すこと
	@Test
	public void testGetDefaultAsDouble_Success() {
		var intT = new BmsMetaElement(BmsMeta.single("#m", BmsType.INTEGER, "100", 0, false), 0, null);
		var fltT = new BmsMetaElement(BmsMeta.single("#m", BmsType.FLOAT, "200.345", 0, false), 0, null);
		var b16T = new BmsMetaElement(BmsMeta.single("#m", BmsType.BASE16, "AB", 0, false), 0, null);
		var b36T = new BmsMetaElement(BmsMeta.single("#m", BmsType.BASE36, "XY", 0, false), 0, null);
		assertEquals(100.0, intT.getDefaultAsDouble(), 0.0);
		assertEquals(200.345, fltT.getDefaultAsDouble(), 0.0);
		assertEquals(BmsInt.to16i("AB"), b16T.getDefaultAsDouble(), 0.0);
		assertEquals(BmsInt.to36i("XY"), b36T.getDefaultAsDouble(), 0.0);
	}

	// getDefaultAsDouble()
	// ClassCastException メタ情報のデフォルト値をlong型に変換できない
	@Test
	public void testGetDefaultAsDouble_Uncastable() {
		var strT = new BmsMetaElement(BmsMeta.single("#m", BmsType.STRING, "", 0, false), 0, null);
		var a16T = new BmsMetaElement(BmsMeta.single("#m", BmsType.ARRAY16, "00", 0, false), 0, null);
		var a36T = new BmsMetaElement(BmsMeta.single("#m", BmsType.ARRAY36, "00", 0, false), 0, null);
		var ex = ClassCastException.class;
		assertThrows(ex, () -> strT.getDefaultAsDouble());
		assertThrows(ex, () -> a16T.getDefaultAsDouble());
		assertThrows(ex, () -> a36T.getDefaultAsDouble());
	}

	// getDefaultAsDouble()
	// NullPointerException 任意型メタ情報で当メソッドを実行した
	@Test
	public void testGetDefaultAsDouble_ObjectType() {
		var objT = new BmsMetaElement(BmsMeta.object("#m", BmsUnit.SINGLE), 0, null);
		assertThrows(NullPointerException.class, () -> objT.getDefaultAsDouble());
	}

	// getDefaultAsString()
	// String型に変換したデフォルト値を返すこと
	@Test
	public void testGetDefaultAsString_Success() {
		var intT = new BmsMetaElement(BmsMeta.single("#m", BmsType.INTEGER, "100", 0, false), 0, null);
		var fltT = new BmsMetaElement(BmsMeta.single("#m", BmsType.FLOAT, "200.345", 0, false), 0, null);
		var strT = new BmsMetaElement(BmsMeta.single("#m", BmsType.STRING, "STR", 0, false), 0, null);
		var b16T = new BmsMetaElement(BmsMeta.single("#m", BmsType.BASE16, "AB", 0, false), 0, null);
		var b36T = new BmsMetaElement(BmsMeta.single("#m", BmsType.BASE36, "XY", 0, false), 0, null);
		var a16T = new BmsMetaElement(BmsMeta.single("#m", BmsType.ARRAY16, "AABB", 0, false), 0, null);
		var a36T = new BmsMetaElement(BmsMeta.single("#m", BmsType.ARRAY36, "YYZZ", 0, false), 0, null);
		var objT = new BmsMetaElement(BmsMeta.object("#m", BmsUnit.SINGLE), 0, null);
		assertEquals("100", intT.getDefaultAsString());
		assertEquals("200.345", fltT.getDefaultAsString());
		assertEquals("STR", strT.getDefaultAsString());
		assertEquals(String.valueOf(BmsInt.to16i("AB")), b16T.getDefaultAsString());
		assertEquals(String.valueOf(BmsInt.to36i("XY")), b36T.getDefaultAsString());
		assertEquals("AABB", a16T.getDefaultAsString());
		assertEquals("YYZZ", a36T.getDefaultAsString());
		assertNull(objT.getDefaultAsString());  // 任意型のデフォルト値はnullのため
	}

	// getDefaultAsArray()
	// BmsArray型に変換したデフォルト値を返すこと
	@Test
	public void testGetDefaultAsArray_Success() {
		var a16T = new BmsMetaElement(BmsMeta.single("#m", BmsType.ARRAY16, "AABB", 0, false), 0, null);
		var a36T = new BmsMetaElement(BmsMeta.single("#m", BmsType.ARRAY36, "XXYY", 0, false), 0, null);
		var objT = new BmsMetaElement(BmsMeta.object("#m", BmsUnit.SINGLE), 0, null);
		assertEquals("AABB", a16T.getDefaultAsArray().toString());
		assertEquals("XXYY", a36T.getDefaultAsArray().toString());
		assertNull(objT.getDefaultAsArray());  // 任意型のデフォルト値はnullのため
	}

	// getDefaultAsArray()
	// ClassCastException メタ情報のデフォルト値をBmsArray型に変換できない
	@Test
	public void testGetDefaultAsArray_Uncastable() {
		var intT = new BmsMetaElement(BmsMeta.single("#m", BmsType.INTEGER, "100", 0, false), 0, null);
		var fltT = new BmsMetaElement(BmsMeta.single("#m", BmsType.FLOAT, "200.345", 0, false), 0, null);
		var strT = new BmsMetaElement(BmsMeta.single("#m", BmsType.STRING, "STR", 0, false), 0, null);
		var b16T = new BmsMetaElement(BmsMeta.single("#m", BmsType.BASE16, "AB", 0, false), 0, null);
		var b36T = new BmsMetaElement(BmsMeta.single("#m", BmsType.BASE36, "XY", 0, false), 0, null);
		var ex = ClassCastException.class;
		assertThrows(ex, () -> intT.getDefaultAsArray());
		assertThrows(ex, () -> fltT.getDefaultAsArray());
		assertThrows(ex, () -> strT.getDefaultAsArray());
		assertThrows(ex, () -> b16T.getDefaultAsArray());
		assertThrows(ex, () -> b36T.getDefaultAsArray());
	}

	// getDefault()
	// 型ごとに規定されたデータ型でデフォルト値を返すこと
	@Test
	public void testGetDefault() {
		var intT = new BmsMetaElement(BmsMeta.single("#m", BmsType.INTEGER, "100", 0, false), 0, null);
		var fltT = new BmsMetaElement(BmsMeta.single("#m", BmsType.FLOAT, "200.345", 0, false), 0, null);
		var strT = new BmsMetaElement(BmsMeta.single("#m", BmsType.STRING, "STR", 0, false), 0, null);
		var b16T = new BmsMetaElement(BmsMeta.single("#m", BmsType.BASE16, "AB", 0, false), 0, null);
		var b36T = new BmsMetaElement(BmsMeta.single("#m", BmsType.BASE36, "XY", 0, false), 0, null);
		var a16T = new BmsMetaElement(BmsMeta.single("#m", BmsType.ARRAY16, "AABB", 0, false), 0, null);
		var a36T = new BmsMetaElement(BmsMeta.single("#m", BmsType.ARRAY36, "XXYY", 0, false), 0, null);
		var objT = new BmsMetaElement(BmsMeta.object("#m", BmsUnit.SINGLE), 0, null);
		assertEquals(Long.class, intT.getDefault().getClass());
		assertEquals(100L, ((Number)intT.getDefault()).longValue());
		assertEquals(Double.class, fltT.getDefault().getClass());
		assertEquals(200.345, ((Number)fltT.getDefault()).doubleValue(), 0.0);
		assertEquals(String.class, strT.getDefault().getClass());
		assertEquals("STR", strT.getDefault());
		assertEquals(Long.class, b16T.getDefault().getClass());
		assertEquals(BmsInt.to16i("AB"), ((Number)b16T.getDefault()).longValue());
		assertEquals(Long.class, b36T.getDefault().getClass());
		assertEquals(BmsInt.to36i("XY"), ((Number)b36T.getDefault()).longValue());
		assertEquals(BmsArray.class, a16T.getDefault().getClass());
		assertEquals("AABB", ((BmsArray)a16T.getDefault()).toString());
		assertEquals(BmsArray.class, a36T.getDefault().getClass());
		assertEquals("XXYY", ((BmsArray)a36T.getDefault()).toString());
		assertNull(objT.getDefault());  // 任意型のデフォルト値はnullのため
	}

	// getValueAsLong()
	// long型に変換した値を返すこと
	@Test
	public void testGetValueAsLong_Success() {
		var intT = new BmsMetaElement(BmsMeta.single("#m", BmsType.INTEGER, "100", 0, false), 0, 200L);
		var fltT = new BmsMetaElement(BmsMeta.single("#m", BmsType.FLOAT, "200.345", 0, false), 0, 300.456);
		var b16T = new BmsMetaElement(BmsMeta.single("#m", BmsType.BASE16, "AB", 0, false), 0, (long)BmsInt.to16i("BC"));
		var b36T = new BmsMetaElement(BmsMeta.single("#m", BmsType.BASE36, "XY", 0, false), 0, (long)BmsInt.to36i("YZ"));
		var objT = new BmsMetaElement(BmsMeta.object("#m", BmsUnit.SINGLE), 0, 400L);
		assertEquals(200L, intT.getValueAsLong());
		assertEquals(300L, fltT.getValueAsLong());
		assertEquals(BmsInt.to16i("BC"), b16T.getValueAsLong());
		assertEquals(BmsInt.to36i("YZ"), b36T.getValueAsLong());
		assertEquals(400L, objT.getValueAsLong());
	}

	// getValueAsLong()
	// ClassCastException メタ情報の値をlong型に変換できない
	@Test
	public void testGetValueAsLong_Uncastable() {
		var strT = new BmsMetaElement(BmsMeta.single("#m", BmsType.STRING, "", 0, false), 0, "STR");
		var a16T = new BmsMetaElement(BmsMeta.single("#m", BmsType.ARRAY16, "AABB", 0, false), 0, new BmsArray("BBCC", 16));
		var a36T = new BmsMetaElement(BmsMeta.single("#m", BmsType.ARRAY36, "XXYY", 0, false), 0, new BmsArray("YYZZ", 36));
		var objT = new BmsMetaElement(BmsMeta.object("#m", BmsUnit.SINGLE), 0, "OBJ");
		var ex = ClassCastException.class;
		assertThrows(ex, () -> strT.getValueAsLong());
		assertThrows(ex, () -> a16T.getValueAsLong());
		assertThrows(ex, () -> a36T.getValueAsLong());
		assertThrows(ex, () -> objT.getValueAsLong());
	}

	// getValueAsDouble()
	// double型に変換した値を返すこと
	@Test
	public void testGetValueAsDouble_Success() {
		var intT = new BmsMetaElement(BmsMeta.single("#m", BmsType.INTEGER, "100", 0, false), 0, 200L);
		var fltT = new BmsMetaElement(BmsMeta.single("#m", BmsType.FLOAT, "200.345", 0, false), 0, 300.456);
		var b16T = new BmsMetaElement(BmsMeta.single("#m", BmsType.BASE16, "AB", 0, false), 0, (long)BmsInt.to16i("BC"));
		var b36T = new BmsMetaElement(BmsMeta.single("#m", BmsType.BASE36, "XY", 0, false), 0, (long)BmsInt.to36i("YZ"));
		var objT = new BmsMetaElement(BmsMeta.object("#m", BmsUnit.SINGLE), 0, 400.567);
		assertEquals(200.0, intT.getValueAsDouble(), 0.0);
		assertEquals(300.456, fltT.getValueAsDouble(), 0.0);
		assertEquals(BmsInt.to16i("BC"), b16T.getValueAsDouble(), 0.0);
		assertEquals(BmsInt.to36i("YZ"), b36T.getValueAsDouble(), 0.0);
		assertEquals(400.567, objT.getValueAsDouble(), 0.0);
	}

	// getValueAsDouble()
	// ClassCastException メタ情報の値をdouble型に変換できない
	@Test
	public void testGetValueAsDouble_Uncastable() {
		var strT = new BmsMetaElement(BmsMeta.single("#m", BmsType.STRING, "", 0, false), 0, "STR");
		var a16T = new BmsMetaElement(BmsMeta.single("#m", BmsType.ARRAY16, "AABB", 0, false), 0, new BmsArray("BBCC", 16));
		var a36T = new BmsMetaElement(BmsMeta.single("#m", BmsType.ARRAY36, "XXYY", 0, false), 0, new BmsArray("YYZZ", 36));
		var objT = new BmsMetaElement(BmsMeta.object("#m", BmsUnit.SINGLE), 0, "OBJ");
		var ex = ClassCastException.class;
		assertThrows(ex, () -> strT.getDefaultAsDouble());
		assertThrows(ex, () -> a16T.getDefaultAsDouble());
		assertThrows(ex, () -> a36T.getDefaultAsDouble());
		assertThrows(ex, () -> objT.getValueAsLong());
	}

	// getValueAsString()
	// String型に変換した値を返すこと
	@Test
	public void testGetValueAsString_Success() {
		var intT = new BmsMetaElement(BmsMeta.single("#m", BmsType.INTEGER, "100", 0, false), 0, 200L);
		var fltT = new BmsMetaElement(BmsMeta.single("#m", BmsType.FLOAT, "200.345", 0, false), 0, 300.456);
		var strT = new BmsMetaElement(BmsMeta.single("#m", BmsType.STRING, "", 0, false), 0, "STR");
		var b16T = new BmsMetaElement(BmsMeta.single("#m", BmsType.BASE16, "AB", 0, false), 0, (long)BmsInt.to16i("BC"));
		var b36T = new BmsMetaElement(BmsMeta.single("#m", BmsType.BASE36, "XY", 0, false), 0, (long)BmsInt.to36i("YZ"));
		var a16T = new BmsMetaElement(BmsMeta.single("#m", BmsType.ARRAY16, "AABB", 0, false), 0, new BmsArray("BBCC", 16));
		var a36T = new BmsMetaElement(BmsMeta.single("#m", BmsType.ARRAY36, "XXYY", 0, false), 0, new BmsArray("YYZZ", 36));
		var objT = new BmsMetaElement(BmsMeta.object("#m", BmsUnit.SINGLE), 0, 400L);
		assertEquals("200", intT.getValueAsString());
		assertEquals("300.456", fltT.getValueAsString());
		assertEquals("STR", strT.getValueAsString());
		assertEquals(String.valueOf(BmsInt.to16i("BC")), b16T.getValueAsString());
		assertEquals(String.valueOf(BmsInt.to36i("YZ")), b36T.getValueAsString());
		assertEquals("BBCC", a16T.getValueAsString());
		assertEquals("YYZZ", a36T.getValueAsString());
		assertEquals("400", objT.getValueAsString());
	}

	// getValueAsArray()
	// BmsArray型に変換した値を返すこと
	@Test
	public void testGetValueAsArray_Success() {
		var a16T = new BmsMetaElement(BmsMeta.single("#m", BmsType.ARRAY16, "AABB", 0, false), 0, new BmsArray("BBCC", 16));
		var a36T = new BmsMetaElement(BmsMeta.single("#m", BmsType.ARRAY36, "XXYY", 0, false), 0, new BmsArray("YYZZ", 36));
		var objT = new BmsMetaElement(BmsMeta.object("#m", BmsUnit.SINGLE), 0, new BmsArray("BCYZ", 36));
		assertEquals("BBCC", a16T.getValueAsArray().toString());
		assertEquals("YYZZ", a36T.getValueAsArray().toString());
		assertEquals("BCYZ", objT.getValueAsArray().toString());
	}

	// getValueAsArray()
	// ClassCastException メタ情報の値をBmsArray型に変換できない
	@Test
	public void testGetValueAsArray_Uncastable() {
		var intT = new BmsMetaElement(BmsMeta.single("#m", BmsType.INTEGER, "100", 0, false), 0, 200L);
		var fltT = new BmsMetaElement(BmsMeta.single("#m", BmsType.FLOAT, "200.345", 0, false), 0, 300.456);
		var strT = new BmsMetaElement(BmsMeta.single("#m", BmsType.STRING, "", 0, false), 0, "STR");
		var b16T = new BmsMetaElement(BmsMeta.single("#m", BmsType.BASE16, "AB", 0, false), 0, (long)BmsInt.to16i("BC"));
		var b36T = new BmsMetaElement(BmsMeta.single("#m", BmsType.BASE36, "XY", 0, false), 0, (long)BmsInt.to36i("YZ"));
		var objT = new BmsMetaElement(BmsMeta.object("#m", BmsUnit.SINGLE), 0, "OBJ");
		var ex = ClassCastException.class;
		assertThrows(ex, () -> intT.getValueAsArray());
		assertThrows(ex, () -> fltT.getValueAsArray());
		assertThrows(ex, () -> strT.getValueAsArray());
		assertThrows(ex, () -> b16T.getValueAsArray());
		assertThrows(ex, () -> b36T.getValueAsArray());
		assertThrows(ex, () -> objT.getValueAsArray());
	}

	// getValue()
	// 型ごとに規定されたデータ型で値を返すこと
	@Test
	public void testGetValue_Normal() {
		var intT = new BmsMetaElement(BmsMeta.single("#m", BmsType.INTEGER, "100", 0, false), 0, 200L);
		var fltT = new BmsMetaElement(BmsMeta.single("#m", BmsType.FLOAT, "200.345", 0, false), 0, 300.456);
		var strT = new BmsMetaElement(BmsMeta.single("#m", BmsType.STRING, "STR", 0, false), 0, "ING");
		var b16T = new BmsMetaElement(BmsMeta.single("#m", BmsType.BASE16, "AB", 0, false), 0, (long)BmsInt.to16i("BC"));
		var b36T = new BmsMetaElement(BmsMeta.single("#m", BmsType.BASE36, "XY", 0, false), 0, (long)BmsInt.to36i("YZ"));
		var a16T = new BmsMetaElement(BmsMeta.single("#m", BmsType.ARRAY16, "AABB", 0, false), 0, new BmsArray("BBCC", 16));
		var a36T = new BmsMetaElement(BmsMeta.single("#m", BmsType.ARRAY36, "XXYY", 0, false), 0, new BmsArray("YYZZ", 36));
		var objT = new BmsMetaElement(BmsMeta.object("#m", BmsUnit.SINGLE), 0, "OBJ");
		assertEquals(Long.class, intT.getValue().getClass());
		assertEquals(200L, ((Number)intT.getValue()).longValue());
		assertEquals(Double.class, fltT.getValue().getClass());
		assertEquals(300.456, ((Number)fltT.getValue()).doubleValue(), 0.0);
		assertEquals(String.class, strT.getValue().getClass());
		assertEquals("ING", strT.getValue());
		assertEquals(Long.class, b16T.getValue().getClass());
		assertEquals(BmsInt.to16i("BC"), ((Number)b16T.getValue()).longValue());
		assertEquals(Long.class, b36T.getValue().getClass());
		assertEquals(BmsInt.to36i("YZ"), ((Number)b36T.getValue()).longValue());
		assertEquals(BmsArray.class, a16T.getValue().getClass());
		assertEquals("BBCC", ((BmsArray)a16T.getValue()).toString());
		assertEquals(BmsArray.class, a36T.getValue().getClass());
		assertEquals("YYZZ", ((BmsArray)a36T.getValue()).toString());
		assertEquals(String.class, objT.getValue().getClass());
		assertEquals("OBJ", objT.getValue());
	}

	// getValue()
	// 値の設定されていないメタ情報はデフォルト値を返すこと
	public void testGetValue_Default() {
		var intT = new BmsMetaElement(BmsMeta.single("#m", BmsType.INTEGER, "100", 0, false), 0, null);
		var fltT = new BmsMetaElement(BmsMeta.single("#m", BmsType.FLOAT, "200.345", 0, false), 0, null);
		var strT = new BmsMetaElement(BmsMeta.single("#m", BmsType.STRING, "STR", 0, false), 0, null);
		var b16T = new BmsMetaElement(BmsMeta.single("#m", BmsType.BASE16, "AB", 0, false), 0, null);
		var b36T = new BmsMetaElement(BmsMeta.single("#m", BmsType.BASE36, "XY", 0, false), 0, null);
		var a16T = new BmsMetaElement(BmsMeta.single("#m", BmsType.ARRAY16, "AABB", 0, false), 0, null);
		var a36T = new BmsMetaElement(BmsMeta.single("#m", BmsType.ARRAY36, "XXYY", 0, false), 0, null);
		var objT = new BmsMetaElement(BmsMeta.object("#m", BmsUnit.SINGLE), 0, null);
		assertEquals(Long.class, intT.getValue().getClass());
		assertEquals(100L, ((Number)intT.getValue()).longValue());
		assertEquals(Double.class, fltT.getValue().getClass());
		assertEquals(200.345, ((Number)fltT.getValue()).doubleValue(), 0.0);
		assertEquals(String.class, strT.getValue().getClass());
		assertEquals("STR", strT.getValue());
		assertEquals(Long.class, b16T.getValue().getClass());
		assertEquals(BmsInt.to16i("AB"), ((Number)b16T.getValue()).longValue());
		assertEquals(Long.class, b36T.getValue().getClass());
		assertEquals(BmsInt.to36i("XY"), ((Number)b36T.getValue()).longValue());
		assertEquals(BmsArray.class, a16T.getValue().getClass());
		assertEquals("AABB", ((BmsArray)a16T.getValue()).toString());
		assertEquals(BmsArray.class, a36T.getValue().getClass());
		assertEquals("XXYY", ((BmsArray)a36T.getValue()).toString());
		assertNull(objT.getValue());  // 任意型のデフォルト値はnullのため
	}

	// isContain()
	// 値が設定されている(nullでない)時trueを返し、設定されていない時falseを返すこと
	@Test
	public void testIsContain() {
		assertTrue((new BmsMetaElement(BmsMeta.single("#m", BmsType.INTEGER, "100", 0, false), 0, 200L)).isContain());
		assertFalse((new BmsMetaElement(BmsMeta.single("#m", BmsType.INTEGER, "100", 0, false), 0, null)).isContain());
	}

	// isSingleUnit()
	// 単体メタ情報の時trueを返し、それ以外の時falseを返すこと
	@Test
	public void testIsSingleUnit() {
		assertTrue((new BmsMetaElement(BmsMeta.single("#m", BmsType.STRING, "", 0, false), 0, null)).isSingleUnit());
		assertFalse((new BmsMetaElement(BmsMeta.multiple("#m", BmsType.STRING, "", 0, false), 0, null)).isSingleUnit());
		assertFalse((new BmsMetaElement(BmsMeta.indexed("#m", BmsType.STRING, "", 0, false), 0, null)).isSingleUnit());
	}

	// isMultipleUnit()
	// 複数メタ情報の時trueを返し、それ以外の時falseを返すこと
	@Test
	public void testIsMultipleUnit() {
		assertFalse((new BmsMetaElement(BmsMeta.single("#m", BmsType.STRING, "", 0, false), 0, null)).isMultipleUnit());
		assertTrue((new BmsMetaElement(BmsMeta.multiple("#m", BmsType.STRING, "", 0, false), 0, null)).isMultipleUnit());
		assertFalse((new BmsMetaElement(BmsMeta.indexed("#m", BmsType.STRING, "", 0, false), 0, null)).isMultipleUnit());
	}

	// isIndexedUnit()
	// 索引付きメタ情報の時trueを返し、それ以外の時falseを返すこと
	@Test
	public void testIsIndexedUnit() {
		assertFalse((new BmsMetaElement(BmsMeta.single("#m", BmsType.STRING, "", 0, false), 0, null)).isIndexedUnit());
		assertFalse((new BmsMetaElement(BmsMeta.multiple("#m", BmsType.STRING, "", 0, false), 0, null)).isIndexedUnit());
		assertTrue((new BmsMetaElement(BmsMeta.indexed("#m", BmsType.STRING, "", 0, false), 0, null)).isIndexedUnit());
	}

	// isIntegerType()
	// 整数の時trueを返し、それ以外の時falseを返すこと
	@Test
	public void testIsIntegerType() {
		assertTrue((new BmsMetaElement(BmsMeta.single("#m", BmsType.INTEGER, "0", 0, false), 0, null)).isIntegerType());
		assertFalse((new BmsMetaElement(BmsMeta.single("#m", BmsType.FLOAT, "0", 0, false), 0, null)).isIntegerType());
		assertFalse((new BmsMetaElement(BmsMeta.single("#m", BmsType.STRING, "", 0, false), 0, null)).isIntegerType());
		assertFalse((new BmsMetaElement(BmsMeta.single("#m", BmsType.BASE16, "00", 0, false), 0, null)).isIntegerType());
		assertFalse((new BmsMetaElement(BmsMeta.single("#m", BmsType.BASE36, "00", 0, false), 0, null)).isIntegerType());
		assertFalse((new BmsMetaElement(BmsMeta.single("#m", BmsType.ARRAY16, "00", 0, false), 0, null)).isIntegerType());
		assertFalse((new BmsMetaElement(BmsMeta.single("#m", BmsType.ARRAY36, "00", 0, false), 0, null)).isIntegerType());
		assertFalse((new BmsMetaElement(BmsMeta.object("#m", BmsUnit.SINGLE), 0, null)).isIntegerType());
	}

	// isFloatType()
	// 実数の時trueを返し、それ以外の時falseを返すこと
	@Test
	public void testIsFloatType() {
		assertFalse((new BmsMetaElement(BmsMeta.single("#m", BmsType.INTEGER, "0", 0, false), 0, null)).isFloatType());
		assertTrue((new BmsMetaElement(BmsMeta.single("#m", BmsType.FLOAT, "0", 0, false), 0, null)).isFloatType());
		assertFalse((new BmsMetaElement(BmsMeta.single("#m", BmsType.STRING, "", 0, false), 0, null)).isFloatType());
		assertFalse((new BmsMetaElement(BmsMeta.single("#m", BmsType.BASE16, "00", 0, false), 0, null)).isFloatType());
		assertFalse((new BmsMetaElement(BmsMeta.single("#m", BmsType.BASE36, "00", 0, false), 0, null)).isFloatType());
		assertFalse((new BmsMetaElement(BmsMeta.single("#m", BmsType.ARRAY16, "00", 0, false), 0, null)).isFloatType());
		assertFalse((new BmsMetaElement(BmsMeta.single("#m", BmsType.ARRAY36, "00", 0, false), 0, null)).isFloatType());
		assertFalse((new BmsMetaElement(BmsMeta.object("#m", BmsUnit.SINGLE), 0, null)).isFloatType());
	}

	// isStringType()
	// 文字列の時trueを返し、それ以外の時falseを返すこと
	@Test
	public void testIsStringType() {
		assertFalse((new BmsMetaElement(BmsMeta.single("#m", BmsType.INTEGER, "0", 0, false), 0, null)).isStringType());
		assertFalse((new BmsMetaElement(BmsMeta.single("#m", BmsType.FLOAT, "0", 0, false), 0, null)).isStringType());
		assertTrue((new BmsMetaElement(BmsMeta.single("#m", BmsType.STRING, "", 0, false), 0, null)).isStringType());
		assertFalse((new BmsMetaElement(BmsMeta.single("#m", BmsType.BASE16, "00", 0, false), 0, null)).isStringType());
		assertFalse((new BmsMetaElement(BmsMeta.single("#m", BmsType.BASE36, "00", 0, false), 0, null)).isStringType());
		assertFalse((new BmsMetaElement(BmsMeta.single("#m", BmsType.ARRAY16, "00", 0, false), 0, null)).isStringType());
		assertFalse((new BmsMetaElement(BmsMeta.single("#m", BmsType.ARRAY36, "00", 0, false), 0, null)).isStringType());
		assertFalse((new BmsMetaElement(BmsMeta.object("#m", BmsUnit.SINGLE), 0, null)).isStringType());
	}

	// isBase16Type()
	// 16進数値の時trueを返し、それ以外の時falseを返すこと
	@Test
	public void testIsBase16Type() {
		assertFalse((new BmsMetaElement(BmsMeta.single("#m", BmsType.INTEGER, "0", 0, false), 0, null)).isBase16Type());
		assertFalse((new BmsMetaElement(BmsMeta.single("#m", BmsType.FLOAT, "0", 0, false), 0, null)).isBase16Type());
		assertFalse((new BmsMetaElement(BmsMeta.single("#m", BmsType.STRING, "", 0, false), 0, null)).isBase16Type());
		assertTrue((new BmsMetaElement(BmsMeta.single("#m", BmsType.BASE16, "00", 0, false), 0, null)).isBase16Type());
		assertFalse((new BmsMetaElement(BmsMeta.single("#m", BmsType.BASE36, "00", 0, false), 0, null)).isBase16Type());
		assertFalse((new BmsMetaElement(BmsMeta.single("#m", BmsType.ARRAY16, "00", 0, false), 0, null)).isBase16Type());
		assertFalse((new BmsMetaElement(BmsMeta.single("#m", BmsType.ARRAY36, "00", 0, false), 0, null)).isBase16Type());
		assertFalse((new BmsMetaElement(BmsMeta.object("#m", BmsUnit.SINGLE), 0, null)).isBase16Type());
	}

	// isBase36Type()
	// 36進数値の時trueを返し、それ以外の時falseを返すこと
	@Test
	public void testIsBase36Type() {
		assertFalse((new BmsMetaElement(BmsMeta.single("#m", BmsType.INTEGER, "0", 0, false), 0, null)).isBase36Type());
		assertFalse((new BmsMetaElement(BmsMeta.single("#m", BmsType.FLOAT, "0", 0, false), 0, null)).isBase36Type());
		assertFalse((new BmsMetaElement(BmsMeta.single("#m", BmsType.STRING, "", 0, false), 0, null)).isBase36Type());
		assertFalse((new BmsMetaElement(BmsMeta.single("#m", BmsType.BASE16, "00", 0, false), 0, null)).isBase36Type());
		assertTrue((new BmsMetaElement(BmsMeta.single("#m", BmsType.BASE36, "00", 0, false), 0, null)).isBase36Type());
		assertFalse((new BmsMetaElement(BmsMeta.single("#m", BmsType.ARRAY16, "00", 0, false), 0, null)).isBase36Type());
		assertFalse((new BmsMetaElement(BmsMeta.single("#m", BmsType.ARRAY36, "00", 0, false), 0, null)).isBase36Type());
		assertFalse((new BmsMetaElement(BmsMeta.object("#m", BmsUnit.SINGLE), 0, null)).isBase36Type());
	}

	// isArray16Type()
	// 16進数値配列の時trueを返し、それ以外の時falseを返すこと
	@Test
	public void testIsArray16Type() {
		assertFalse((new BmsMetaElement(BmsMeta.single("#m", BmsType.INTEGER, "0", 0, false), 0, null)).isArray16Type());
		assertFalse((new BmsMetaElement(BmsMeta.single("#m", BmsType.FLOAT, "0", 0, false), 0, null)).isArray16Type());
		assertFalse((new BmsMetaElement(BmsMeta.single("#m", BmsType.STRING, "", 0, false), 0, null)).isArray16Type());
		assertFalse((new BmsMetaElement(BmsMeta.single("#m", BmsType.BASE16, "00", 0, false), 0, null)).isArray16Type());
		assertFalse((new BmsMetaElement(BmsMeta.single("#m", BmsType.BASE36, "00", 0, false), 0, null)).isArray16Type());
		assertTrue((new BmsMetaElement(BmsMeta.single("#m", BmsType.ARRAY16, "00", 0, false), 0, null)).isArray16Type());
		assertFalse((new BmsMetaElement(BmsMeta.single("#m", BmsType.ARRAY36, "00", 0, false), 0, null)).isArray16Type());
		assertFalse((new BmsMetaElement(BmsMeta.object("#m", BmsUnit.SINGLE), 0, null)).isArray16Type());
	}

	// isArray36Type()
	// 36進数値配列の時trueを返し、それ以外の時falseを返すこと
	@Test
	public void testIsArray36Type() {
		assertFalse((new BmsMetaElement(BmsMeta.single("#m", BmsType.INTEGER, "0", 0, false), 0, null)).isArray36Type());
		assertFalse((new BmsMetaElement(BmsMeta.single("#m", BmsType.FLOAT, "0", 0, false), 0, null)).isArray36Type());
		assertFalse((new BmsMetaElement(BmsMeta.single("#m", BmsType.STRING, "", 0, false), 0, null)).isArray36Type());
		assertFalse((new BmsMetaElement(BmsMeta.single("#m", BmsType.BASE16, "00", 0, false), 0, null)).isArray36Type());
		assertFalse((new BmsMetaElement(BmsMeta.single("#m", BmsType.BASE36, "00", 0, false), 0, null)).isArray36Type());
		assertFalse((new BmsMetaElement(BmsMeta.single("#m", BmsType.ARRAY16, "00", 0, false), 0, null)).isArray36Type());
		assertTrue((new BmsMetaElement(BmsMeta.single("#m", BmsType.ARRAY36, "00", 0, false), 0, null)).isArray36Type());
		assertFalse((new BmsMetaElement(BmsMeta.object("#m", BmsUnit.SINGLE), 0, null)).isArray36Type());
	}

	// isObjectType()
	// 任意型の時trueを返し、それ以外の時falseを返すこと
	@Test
	public void testIsObjectType() {
		assertFalse((new BmsMetaElement(BmsMeta.single("#m", BmsType.INTEGER, "0", 0, false), 0, null)).isObjectType());
		assertFalse((new BmsMetaElement(BmsMeta.single("#m", BmsType.FLOAT, "0", 0, false), 0, null)).isObjectType());
		assertFalse((new BmsMetaElement(BmsMeta.single("#m", BmsType.STRING, "", 0, false), 0, null)).isObjectType());
		assertFalse((new BmsMetaElement(BmsMeta.single("#m", BmsType.BASE16, "00", 0, false), 0, null)).isObjectType());
		assertFalse((new BmsMetaElement(BmsMeta.single("#m", BmsType.BASE36, "00", 0, false), 0, null)).isObjectType());
		assertFalse((new BmsMetaElement(BmsMeta.single("#m", BmsType.ARRAY16, "00", 0, false), 0, null)).isObjectType());
		assertFalse((new BmsMetaElement(BmsMeta.single("#m", BmsType.ARRAY36, "00", 0, false), 0, null)).isObjectType());
		assertTrue((new BmsMetaElement(BmsMeta.object("#m", BmsUnit.SINGLE), 0, null)).isObjectType());
	}

	// isNumberType()
	// 数値型の時trueを返し、それ以外の時falseを返すこと
	@Test
	public void testIsNumberType() {
		assertTrue((new BmsMetaElement(BmsMeta.single("#m", BmsType.INTEGER, "0", 0, false), 0, null)).isNumberType());
		assertTrue((new BmsMetaElement(BmsMeta.single("#m", BmsType.FLOAT, "0", 0, false), 0, null)).isNumberType());
		assertFalse((new BmsMetaElement(BmsMeta.single("#m", BmsType.STRING, "", 0, false), 0, null)).isNumberType());
		assertTrue((new BmsMetaElement(BmsMeta.single("#m", BmsType.BASE16, "00", 0, false), 0, null)).isNumberType());
		assertTrue((new BmsMetaElement(BmsMeta.single("#m", BmsType.BASE36, "00", 0, false), 0, null)).isNumberType());
		assertFalse((new BmsMetaElement(BmsMeta.single("#m", BmsType.ARRAY16, "00", 0, false), 0, null)).isNumberType());
		assertFalse((new BmsMetaElement(BmsMeta.single("#m", BmsType.ARRAY36, "00", 0, false), 0, null)).isNumberType());
		assertFalse((new BmsMetaElement(BmsMeta.object("#m", BmsUnit.SINGLE), 0, null)).isNumberType());
	}

	// isValueType()
	// 値型の時trueを返し、それ以外の時falseを返すこと
	@Test
	public void testIsValueType() {
		assertTrue((new BmsMetaElement(BmsMeta.single("#m", BmsType.INTEGER, "0", 0, false), 0, null)).isValueType());
		assertTrue((new BmsMetaElement(BmsMeta.single("#m", BmsType.FLOAT, "0", 0, false), 0, null)).isValueType());
		assertTrue((new BmsMetaElement(BmsMeta.single("#m", BmsType.STRING, "", 0, false), 0, null)).isValueType());
		assertTrue((new BmsMetaElement(BmsMeta.single("#m", BmsType.BASE16, "00", 0, false), 0, null)).isValueType());
		assertTrue((new BmsMetaElement(BmsMeta.single("#m", BmsType.BASE36, "00", 0, false), 0, null)).isValueType());
		assertFalse((new BmsMetaElement(BmsMeta.single("#m", BmsType.ARRAY16, "00", 0, false), 0, null)).isValueType());
		assertFalse((new BmsMetaElement(BmsMeta.single("#m", BmsType.ARRAY36, "00", 0, false), 0, null)).isValueType());
		assertFalse((new BmsMetaElement(BmsMeta.object("#m", BmsUnit.SINGLE), 0, null)).isValueType());
	}

	// isArrayType()
	// 配列型の時trueを返し、それ以外の時falseを返すこと
	@Test
	public void testIsArrayType() {
		assertFalse((new BmsMetaElement(BmsMeta.single("#m", BmsType.INTEGER, "0", 0, false), 0, null)).isArrayType());
		assertFalse((new BmsMetaElement(BmsMeta.single("#m", BmsType.FLOAT, "0", 0, false), 0, null)).isArrayType());
		assertFalse((new BmsMetaElement(BmsMeta.single("#m", BmsType.STRING, "", 0, false), 0, null)).isArrayType());
		assertFalse((new BmsMetaElement(BmsMeta.single("#m", BmsType.BASE16, "00", 0, false), 0, null)).isArrayType());
		assertFalse((new BmsMetaElement(BmsMeta.single("#m", BmsType.BASE36, "00", 0, false), 0, null)).isArrayType());
		assertTrue((new BmsMetaElement(BmsMeta.single("#m", BmsType.ARRAY16, "00", 0, false), 0, null)).isArrayType());
		assertTrue((new BmsMetaElement(BmsMeta.single("#m", BmsType.ARRAY36, "00", 0, false), 0, null)).isArrayType());
		assertFalse((new BmsMetaElement(BmsMeta.object("#m", BmsUnit.SINGLE), 0, null)).isArrayType());
	}

	// isNormalType()
	// 通常型の時trueを返し、それ以外の時falseを返すこと
	@Test
	public void testIsNormalType() {
		assertTrue((new BmsMetaElement(BmsMeta.single("#m", BmsType.INTEGER, "0", 0, false), 0, null)).isNormalType());
		assertTrue((new BmsMetaElement(BmsMeta.single("#m", BmsType.FLOAT, "0", 0, false), 0, null)).isNormalType());
		assertTrue((new BmsMetaElement(BmsMeta.single("#m", BmsType.STRING, "", 0, false), 0, null)).isNormalType());
		assertTrue((new BmsMetaElement(BmsMeta.single("#m", BmsType.BASE16, "00", 0, false), 0, null)).isNormalType());
		assertTrue((new BmsMetaElement(BmsMeta.single("#m", BmsType.BASE36, "00", 0, false), 0, null)).isNormalType());
		assertTrue((new BmsMetaElement(BmsMeta.single("#m", BmsType.ARRAY16, "00", 0, false), 0, null)).isNormalType());
		assertTrue((new BmsMetaElement(BmsMeta.single("#m", BmsType.ARRAY36, "00", 0, false), 0, null)).isNormalType());
		assertFalse((new BmsMetaElement(BmsMeta.object("#m", BmsUnit.SINGLE), 0, null)).isNormalType());
	}
}
