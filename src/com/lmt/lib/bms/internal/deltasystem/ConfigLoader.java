package com.lmt.lib.bms.internal.deltasystem;

import java.io.IOException;
import java.util.Properties;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

import com.lmt.lib.bms.bemusic.BeMusicRatingType;
import com.lmt.lib.bms.internal.LinearInterpolateFunction;
import com.lmt.lib.bms.internal.LogInterpolateFunction;

/**
 * Delta System専用の設定ファイル読み込みクラス
 */
class ConfigLoader {
	/** 設定読み込み・変換用I/F */
	@FunctionalInterface
	private interface Deserializer<R> {
		R deserialize(String in) throws IOException;
	}

	/** タグ */
	private String mTag;
	/** 設定データ */
	private Properties mProperties;

	/**
	 * コンストラクタ
	 * @param ratingType 読み込み対象レーティング種別
	 * @param properties 設定データ
	 */
	ConfigLoader(BeMusicRatingType ratingType, Properties properties) {
		this(ratingType.name().toLowerCase(), properties);
	}

	/**
	 * コンストラクタ
	 * @param tag タグ
	 * @param properties 設定データ
	 */
	ConfigLoader(String tag, Properties properties) {
		mTag = tag;
		mProperties = properties;
	}

	/**
	 * 数値データ読み込み
	 * @param name 設定値名称
	 * @param current 現在値
	 * @return 数値データ
	 */
	double numeric(String name, double current) {
		return read(name, current, Double::parseDouble);
	}

	/**
	 * 整数データ読み込み
	 * @param name 設定値名称
	 * @param current 現在値
	 * @return 整数データ
	 */
	int integer(String name, int current) {
		return read(name, current, Integer::parseInt);
	}

	/**
	 * 線形補間データ読み込み
	 * @param name 設定値名称
	 * @param current 現在値
	 * @return 線形補間データ
	 */
	LinearInterpolateFunction ipfnLinear(String name, LinearInterpolateFunction current) {
		return read(name, current, v -> {
			var values = Stream.of(v.split(",")).mapToDouble(Double::parseDouble).toArray();
			var points = DoubleStream.of(values).skip(2).toArray();
			return LinearInterpolateFunction.create(values[0], values[1], points);
		});
	}

	/**
	 * 対数補間データ読み込み
	 * @param name 設定値名称
	 * @param current 現在値
	 * @return 対数補間データ
	 */
	LogInterpolateFunction ipfnLog(String name, LogInterpolateFunction current) {
		return read(name, current, v -> {
			var values = Stream.of(v.split(",")).mapToDouble(Double::parseDouble).toArray();
			if (values.length != 4) {
				throw new IOException("Illegal number of arguments (inRange, outRange, strength, offset)");
			} else {
				return LogInterpolateFunction.create(values[0], values[1], values[2], values[3]);
			}
		});
	}

	/**
	 * 設定読み込み
	 * @param <R> 返却データの型
	 * @param name 設定値名称
	 * @param current 現在値
	 * @param deserializer 設定読み込み・変換用I/F
	 * @return 読み込んだ設定値
	 */
	private <R> R read(String name, R current, Deserializer<R> deserializer) {
		var str = mProperties.get(String.format("%s.%s", mTag, name));
		if (str == null) {
			Ds.debug("%s.%s: Not found", mTag, name);
			return current;
		}
		try {
			var value = deserializer.deserialize(str.toString());
			return value;
		} catch (Exception e) {
			Ds.debug("%s.%s: %s: Value=%s", mTag, name, e, str);
			return current;
		}
	}
}
