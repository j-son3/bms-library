package com.lmt.lib.bms;

import static com.lmt.lib.bms.internal.Assertion.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * BMSライブラリが出力するハッシュ値の入力データのジェネレータ
 *
 * <p>{@link BmsSpec} や {@link BmsContent} が生成するハッシュ値の元となる入力データを生成するクラス。
 * 入力データの基本フォーマットはUTF-8にエンコードされたJSON文字列となっており、
 * 当クラスではJSON文字列を生成するためのヘルパーメソッド、およびハッシュ値の生成アルゴリズムを内包する。</p>
 *
 * <p>ハッシュ値の特性上、1文字でも値が変化するとハッシュ値が大きく変わってしまうため、
 * 一度入力データの生成アルゴリズムを定義した後は原則としてアルゴリズムを変えてはならない。</p>
 */
class HashSeed {
	/** 現在の階層コンテキスト */
	private LayerContext mCurrentLayer;
	/** 階層コンテキストリスト */
	private List<LayerContext> mLayers = new ArrayList<>();
	/** 編集中の入力データバッファ */
	private StringBuilder mBuffer = new StringBuilder();

	/** 整数値用のフォーマッタ */
	private NumberFormat mLongFormat;
	/** 実数値用のフォーマッタ */
	private DecimalFormat mDoubleFormat;

	/** 階層種別 */
	private static enum LayerType {
		/** ルート */
		ROOT(' ', ' '),
		/** JSON Object */
		OBJECT('{', '}'),
		/** JSON Array */
		ARRAY('[', ']');

		/** 開始括弧 */
		final char leftBracket;
		/** 終了括弧 */
		final char rightBracket;

		/**
		 * コンストラクタ
		 * @param leftBracket 開始括弧
		 * @param rightBracket 終了括弧
		 */
		private LayerType(char leftBracket, char rightBracket) {
			this.leftBracket = leftBracket;
			this.rightBracket = rightBracket;
		}
	}

	/** 階層のコンテキスト */
	private static class LayerContext {
		/** 階層の種別 */
		final LayerType type;
		/** この階層のキー名 */
		final String key;
		/** 階層の深さ */
		final int depth;
		/** この階層が省略可能かどうか */
		final boolean omittable;
		/** この階層で開始括弧を書き込み済みかどうか */
		boolean wroteBracket = false;
		/** この階層で1個以上の値を書き込んだかどうか */
		boolean wroteValue = false;

		/**
		 * コンストラクタ
		 * @param type 階層の種別
		 * @param key この階層のキー名(親階層が配列の場合はnull)
		 * @param depth 階層の深さ
		 * @param omittable この階層が省略可能かどうか
		 */
		LayerContext(LayerType type, String key, int depth, boolean omittable) {
			this.type = type;
			this.key = key;
			this.depth = depth;
			this.omittable = omittable;
		}
	}

	/** コンストラクタ */
	HashSeed() {
		mCurrentLayer = new LayerContext(LayerType.ROOT, null, 0, false);
		mLayers.add(mCurrentLayer);
		mLongFormat = NumberFormat.getInstance(Locale.US);
		mLongFormat.setMaximumFractionDigits(0);
		mLongFormat.setGroupingUsed(false);
		mDoubleFormat = (DecimalFormat)NumberFormat.getNumberInstance(Locale.US);
		mDoubleFormat.applyPattern("0.################");
	}

	/**
	 * 生成したハッシュ値の入力データ生成
	 * <p>入力データは余分な空白・改行のないJSON文字列として返す。</p>
	 * @return ハッシュ値の入力データ
	 * @exception IllegalStateException 入力データの編集が完了していない
	 */
	@Override
	public String toString() {
		assertIsRoot();
		return mBuffer.toString();
	}

	/**
	 * JSON Array内でのJSON Object型データの開始宣言
	 * @param omittable 開始したデータの省略可否
	 * @return このオブジェクトのインスタンス
	 * @exception IllegalStateException JSON Object型データの編集中
	 */
	HashSeed beginObject(boolean omittable) {
		assertIsNotEditingObject();
		processBegin(LayerType.OBJECT, null, omittable);
		return this;
	}

	/**
	 * JSON Object内でのJSON Object型データの開始宣言
	 * @param key 開始するデータのキー名
	 * @param omittable 開始したデータの省略可否
	 * @return このオブジェクトのインスタンス
	 * @exception NullPointerException keyがnull
	 * @exception IllegalStateException 配列データの編集中
	 */
	HashSeed beginObject(String key, boolean omittable) {
		assertArgNotNull(key, "key");
		assertIsNotEditingArray();
		processBegin(LayerType.OBJECT, key, omittable);
		return this;
	}

	/**
	 * JSON Object型データの終了宣言
	 * <p>編集対象のJSON Object型データが省略可能で且つ値が存在しない場合、入力データへの書き出しは行われない。</p>
	 * @return このオブジェクトのインスタンス
	 * @exception IllegalStateException JSON Object型データの編集中ではない
	 */
	HashSeed endObject() {
		assertIsEditingObject();
		processEnd();
		return this;
	}

	/**
	 * JSON Array内でのJSON Array型データの開始宣言
	 * @param omittable 開始したデータの省略可否
	 * @return このオブジェクトのインスタンス
	 * @exception IllegalStateException JSON Object型データの編集中
	 */
	HashSeed beginArray(boolean omittable) {
		assertIsNotEditingObject();
		processBegin(LayerType.ARRAY, null, omittable);
		return this;
	}

	/**
	 * JSON Object内でのJSON Array型データの開始宣言
	 * @param key 開始するデータのキー名
	 * @param omittable 開始したデータの省略可否
	 * @return このオブジェクトのインスタンス
	 * @exception NullPointerException keyがnull
	 * @exception IllegalStateException 配列データの編集中
	 */
	HashSeed beginArray(String key, boolean omittable) {
		assertArgNotNull(key, "key");
		assertIsNotEditingArray();
		processBegin(LayerType.ARRAY, key, omittable);
		return this;
	}

	/**
	 * JSON Array型データの終了宣言
	 * <p>編集対象のJSON Array型データが省略可能で且つ値が存在しない場合、入力データへの書き出しは行われない。</p>
	 * @return このオブジェクトのインスタンス
	 * @exception IllegalStateException JSON Array型データの編集中ではない
	 */
	HashSeed endArray() {
		assertIsEditingArray();
		processEnd();
		return this;
	}

	/**
	 * JSON Arrayへのlong型データ追加
	 * @param value long型データ
	 * @return このオブジェクトのインスタンス
	 * @exception IllegalStateException JSON Array型データの編集中ではない
	 */
	HashSeed put(long value) {
		assertIsEditingArray();
		processPut(null, mLongFormat.format(value));
		return this;
	}

	/**
	 * JSON Arrayへのdouble型データ追加
	 * @param value double型データ
	 * @return このオブジェクトのインスタンス
	 * @exception IllegalStateException JSON Array型データの編集中ではない
	 */
	HashSeed put(double value) {
		assertIsEditingArray();
		processPut(null, mDoubleFormat.format(value));
		return this;
	}

	/**
	 * JSON Arrayへのboolean型データ追加
	 * @param value boolean型データ
	 * @return このオブジェクトのインスタンス
	 * @exception IllegalStateException JSON Array型データの編集中ではない
	 */
	HashSeed put(boolean value) {
		assertIsEditingArray();
		processPut(null, Boolean.toString(value));
		return this;
	}

	/**
	 * JSON Arrayへの文字列型データ追加
	 * @param value 文字列型データ
	 * @return このオブジェクトのインスタンス
	 * @exception IllegalStateException JSON Array型データの編集中ではない
	 */
	HashSeed put(String value) {
		assertIsEditingArray();
		processPut(null, formatString(value));
		return this;
	}

	/**
	 * JSON Arrayへのデータ追加
	 * @param type データ型
	 * @param value データ
	 * @return このオブジェクトのインスタンス
	 * @exception NullPointerException typeがnull
	 * @exception IllegalStateException JSON Array型データの編集中ではない
	 */
	HashSeed put(BmsType type, Object value) {
		assertArgNotNull(type, "type");
		assertIsEditingArray();
		processPut(null, formatObject(type, value));
		return this;
	}

	/**
	 * JSON Objectへのlong型データ追加
	 * @param key キー名
	 * @param value long型データ
	 * @return このオブジェクトのインスタンス
	 * @exception NullPointerException keyがnull
	 * @exception IllegalStateException JSON Object型データの編集中ではない
	 */
	HashSeed put(String key, long value) {
		assertArgNotNull(key, "key");
		assertIsEditingObject();
		processPut(key, mLongFormat.format(value));
		return this;
	}

	/**
	 * JSON Objectへのdouble型データ追加
	 * @param key キー名
	 * @param value double型データ
	 * @return このオブジェクトのインスタンス
	 * @exception NullPointerException keyがnull
	 * @exception IllegalStateException JSON Object型データの編集中ではない
	 */
	HashSeed put(String key, double value) {
		assertArgNotNull(key, "key");
		assertIsEditingObject();
		processPut(key, mDoubleFormat.format(value));
		return this;
	}

	/**
	 * JSON Objectへのboolean型データ追加
	 * @param key キー名
	 * @param value boolean型データ
	 * @return このオブジェクトのインスタンス
	 * @exception NullPointerException keyがnull
	 * @exception IllegalStateException JSON Object型データの編集中ではない
	 */
	HashSeed put(String key, boolean value) {
		assertArgNotNull(key, "key");
		assertIsEditingObject();
		processPut(key, Boolean.toString(value));
		return this;
	}

	/**
	 * JSON Objectへの文字列型データ追加
	 * @param key キー名
	 * @param value 文字列型データ
	 * @return このオブジェクトのインスタンス
	 * @exception NullPointerException keyがnull
	 * @exception IllegalStateException JSON Object型データの編集中ではない
	 */
	HashSeed put(String key, String value) {
		assertArgNotNull(key, "key");
		assertIsEditingObject();
		processPut(key, formatString(value));
		return this;
	}

	/**
	 * JSON Objectへのデータ追加
	 * @param key キー名
	 * @param type データ型
	 * @param value データ
	 * @return このオブジェクトのインスタンス
	 * @exception NullPointerException keyがnull
	 * @exception NullPointerException typeがnull
	 * @exception IllegalStateException JSON Object型データの編集中ではない
	 */
	HashSeed put(String key, BmsType type, Object value) {
		assertArgNotNull(key, "key");
		assertArgNotNull(type, "type");
		assertIsEditingObject();
		processPut(key, formatObject(type, value));
		return this;
	}

	/**
	 * ハッシュ値生成
	 * @return ハッシュ値のバイト配列
	 * @exception IllegalStateException 入力データの編集が完了していない
	 * @see #toHash(String)
	 */
	byte[] toHash() {
		return toHash(toString());
	}

	/**
	 * ハッシュ値生成
	 * <p>入力文字列をUTF-8へエンコードし、そのバイトデータを入力データとしてSHA-256のハッシュ値を生成する。</p>
	 * @param seed 入力データ
	 * @return ハッシュ値のバイト配列
	 * @exception NullPointerException seedがnull
	 */
	static byte[] toHash(String seed) {
		assertArgNotNull(seed, "seed");
		try {
			var seedBytes = seed.getBytes(StandardCharsets.UTF_8);
			return MessageDigest.getInstance("SHA-256").digest(seedBytes);
		} catch (NoSuchAlgorithmException e) {
			return new byte[0];  // Don't care
		}
	}

	/**
	 * キー名書き出し
	 * @param key キー名
	 */
	private void writeKey(String key) {
		if (key != null) {
			mBuffer.append('\"');
			mBuffer.append(key);
			mBuffer.append("\":");
		}
	}

	/**
	 * JSON内文字列データへのエンコード
	 * @param input エンコード対象文字列
	 * @return エンコード後文字列(前後の二重引用符付き)
	 */
	private static String formatString(String input) {
		if (input == null) {
			return "null";
		}
		var sb = new StringBuilder();
		var length = input.length();
		sb.append("\"");
		for (var i = 0; i < length; i++) {
			var c = input.charAt(i);
			switch (c) {
				case '\"': sb.append("\\\""); break; // ダブルクオート
				case '\\': sb.append("\\\\"); break; // バックスラッシュ
				case '\b': sb.append("\\b"); break;  // バックスペース
				case '\f': sb.append("\\f"); break;  // フォームフィード
				case '\n': sb.append("\\n"); break;  // 改行
				case '\r': sb.append("\\r"); break;  // 復帰
				case '\t': sb.append("\\t"); break;  // タブ
				default: sb.append((c < ' ') ? String.format("\\u%04x", (int)c) : c); break;
			}
		}
		sb.append("\"");
		return sb.toString();
	}

	/**
	 * データの文字列変換
	 * @param type データ型
	 * @param value データ
	 * @return 文字列変換後のデータ
	 */
	private String formatObject(BmsType type, Object value) {
		if (value == null) {
			return "null";
		}
		switch (type.getNativeType()) {
		case BmsType.NTYPE_LONG: return mLongFormat.format((Long)value);
		case BmsType.NTYPE_DOUBLE: return mDoubleFormat.format((Double)value);
		default: return formatString(value.toString());
		}
	}

	/**
	 * 階層開始時の共通処理
	 * @param newType 開始する階層の種別
	 * @param key 階層のキー名(配列の場合はnull)
	 * @param omittable 階層の省略可否
	 */
	private void processBegin(LayerType newType, String key, boolean omittable) {
		var newContext = new LayerContext(newType, key, mCurrentLayer.depth + 1, omittable);
		mLayers.add(newContext);
		mCurrentLayer = newContext;
		if (!newContext.omittable) {
			processBracket(newContext.depth);
		}
	}

	/**
	 * 階層終了時の共通処理
	 */
	private void processEnd() {
		var wroteBracket = (!mCurrentLayer.omittable || mCurrentLayer.wroteBracket);
		if (wroteBracket) {
			mBuffer.append(mCurrentLayer.type.rightBracket);
		}
		mLayers.remove(mCurrentLayer.depth);
		mCurrentLayer = mLayers.get(mCurrentLayer.depth - 1);
		mCurrentLayer.wroteValue = (mCurrentLayer.wroteValue || wroteBracket);
	}

	/**
	 * データ追加時の共通処理
	 * @param key キー名(配列へのデータ追加時はnull)
	 * @param valueRaw 値
	 */
	private void processPut(String key, String valueRaw) {
		processBracket(mCurrentLayer.depth);
		processComma(mCurrentLayer.depth);
		writeKey(key);
		mBuffer.append(valueRaw);
		mCurrentLayer.wroteValue = true;
	}

	/**
	 * カンマの書き出し処理
	 * @param depth 処理対象の階層の深さ
	 */
	private void processComma(int depth) {
		if (depth > 0) {
			var layer = mLayers.get(depth);
			if (layer.wroteValue) {
				mBuffer.append(',');
			}
		}
	}

	/**
	 * 開始括弧の書き出し処理
	 * @param depth 処理対象の階層の深さ
	 */
	private void processBracket(int depth) {
		if (depth > 0) {
			var layer = mLayers.get(depth);
			if (!layer.wroteBracket) {
				var parentDepth = depth - 1;
				processBracket(parentDepth);
				processComma(parentDepth);
				writeKey(layer.key);
				mBuffer.append(layer.type.leftBracket);
				layer.wroteBracket = true;
			}
		}
	}

	/**
	 * 現在の階層がルートかどうかを確認するアサーション
	 * @exception IllegalStateException 現在の階層がルートではない
	 */
	private void assertIsRoot() {
		assertField(mCurrentLayer.type == LayerType.ROOT, "BUG! Now is editing yet");
	}

	/**
	 * 現在の階層がJSON Objectかどうかを確認するアサーション
	 * @exception IllegalStateException 現在の階層がJSON Objectではない
	 */
	private void assertIsEditingObject() {
		assertField(mCurrentLayer.type == LayerType.OBJECT, "BUG! Now is not editing an object");
	}

	/**
	 * JSON Objectを編集していないことを確認するアサーション
	 * @exception IllegalStateException JSON Object編集中
	 */
	private void assertIsNotEditingObject() {
		assertField(mCurrentLayer.type != LayerType.OBJECT, "BUG! Now is editing an object");
	}

	/**
	 * 現在の階層がJSON Arrayかどうかを確認するアサーション
	 * @exception IllegalStateException 現在の階層がJSON Arrayではない
	 */
	private void assertIsEditingArray() {
		assertField(mCurrentLayer.type == LayerType.ARRAY, "BUG! Now is not editing an array");
	}

	/**
	 * JSON Arrayを編集していないことを確認するアサーション
	 * @exception IllegalStateException JSON Array編集中
	 */
	private void assertIsNotEditingArray() {
		assertField(mCurrentLayer.type != LayerType.ARRAY, "BUG! Now is not editing an array");
	}
}
