package com.lmt.lib.bms.internal.deltasystem;

import static com.lmt.lib.bms.internal.Assertion.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.lmt.lib.bms.bemusic.BeMusicDevice;
import com.lmt.lib.bms.bemusic.BeMusicPoint;

/**
 * Delta System制御用クラス
 *
 * <p>BMSライブラリの一般ユーザーがDelta Systemにアクセスできるのは、Be Musicサブセットを通してのみとなる。
 * このクラスは開発者がデバッグを行う際に必要となる機能をまとめ、非公式にDelta Systemの動作を制御することを
 * 目的としている。そのためこのクラスはドキュメント化の対象外となり、一般ユーザーが使用した結果不都合な動作を
 * したとしてもBMSライブラリ開発者はそのことについて関知しない。</p>
 */
public class Ds {
	/** アルゴリズムのメジャーバージョン */
	public static final int ALGORITHM_MAJOR_VERSION = 1;
	/** アルゴリズムのリビジョン番号 */
	public static final int ALGORITHM_REVISION_NUMBER = 3;
	/** アルゴリズムのステータス D=Draft / R=Reviewing / F=Fixed */
	public static final char ALGORITHM_STATUS_CHAR = 'D';
	/** アルゴリズムパラメータ変更ステータス ''=変更なし / C=変更あり */
	public static final char ALGORITHM_CONFIG_CHANGED = 'C';

	/** 設定情報一式 */
	private static final List<? extends RatingConfig> CONFIGS = List.of(
			CommonConfig.getInstance(),
			ComplexConfig.getInstance(),
			PowerConfig.getInstance(),
			RhythmConfig.getInstance(),
			ScratchConfig.getInstance(),
			HoldingConfig.getInstance(),
			GimmickConfig.getInstance());
	/** 空デバッグハンドラ */
	private static final Consumer<String> EMPTY_DEBUG_HANDLER = o -> {};

	/** デバッグモード */
	private static DsDebugMode sDebugMode = DsDebugMode.NONE;
	/** デバッグハンドラ */
	private static Consumer<String> sDebugHandler = EMPTY_DEBUG_HANDLER;
	/** パラメータ変更状態 */
	private static boolean sConfigChanged = false;

	/**
	 * デバッグモード設定
	 * @param debugMode デバッグモード
	 */
	public static void setDebugMode(DsDebugMode debugMode) {
		sDebugMode = debugMode;
	}

	/**
	 * デバッグモード取得
	 * @return デバッグモード
	 */
	static DsDebugMode getDebugMode() {
		return sDebugMode;
	}

	/**
	 * デバッグハンドラ設定
	 * <p>デバッグ出力の際、ハンドラにデバッグ出力するべき情報が渡されてくる。
	 * ハンドラではprintln()等の出力機能を用いてデバッグ情報を出力すること。</p>
	 * @param handler デバッグハンドラ
	 */
	public static void setDebugHandler(Consumer<String> handler) {
		sDebugHandler = Objects.requireNonNullElse(handler, EMPTY_DEBUG_HANDLER);
	}

	/**
	 * アルゴリズムパラメータ変更状態取得
	 * <p>この状態は{@link #loadConfig(Path)}によってパラメータ変更を行った場合にtrueとなる。
	 * 実際にパラメータが変更されたかどうかではなく、前述のメソッドが実行されたかどうかを表す。</p>
	 * @return パラメータ変更が行われていた場合true
	 */
	public static boolean isConfigChanged() {
		return sConfigChanged;
	}

	/**
	 * デバッグ機能の有効状態判定
	 * @return デバッグモードが{@link DsDebugMode#NONE}以外であればtrue
	 */
	static boolean isDebugEnable() {
		return sDebugMode != DsDebugMode.NONE;
	}

	/**
	 * デバッグ出力
	 * @param msg メッセージ
	 */
	static void debug(Object msg) {
		if (isDebugEnable()) {
			sDebugHandler.accept(msg.toString());
		}
	}

	/**
	 * デバッグ出力
	 * <p>String.format()を利用してメッセージを生成して出力する。</p>
	 * @param format 出力形式
	 * @param args 出力引数
	 */
	static void debug(String format, Object...args) {
		if (isDebugEnable()) {
			sDebugHandler.accept(String.format(format, args));
		}
	}

	/**
	 * デバッグ出力(long型引数)
	 * @param format 出力形式
	 * @param a1 出力引数1
	 */
	static void debug(String format, long a1) {
		debug(format, a1, 0, 0);
	}

	/**
	 * デバッグ出力(long型引数)
	 * @param format 出力形式
	 * @param a1 出力引数1
	 * @param a2 出力引数2
	 */
	static void debug(String format, long a1, long a2) {
		debug(format, a1, a2, 0);
	}

	/**
	 * デバッグ出力(long型引数)
	 * @param format 出力形式
	 * @param a1 出力引数1
	 * @param a2 出力引数2
	 * @param a3 出力引数3
	 */
	static void debug(String format, long a1, long a2, long a3) {
		if (isDebugEnable()) {
			sDebugHandler.accept(String.format(format, a1, a2, a3));
		}
	}

	/**
	 * デバッグ出力(double型引数)
	 * @param format 出力形式
	 * @param a1 出力引数1
	 */
	static void debug(String format, double a1) {
		debug(format, a1, 0, 0, 0, 0);
	}

	/**
	 * デバッグ出力(double型引数)
	 * @param format 出力形式
	 * @param a1 出力引数1
	 * @param a2 出力引数2
	 */
	static void debug(String format, double a1, double a2) {
		debug(format, a1, a2, 0, 0, 0);
	}

	/**
	 * デバッグ出力(double型引数)
	 * @param format 出力形式
	 * @param a1 出力引数1
	 * @param a2 出力引数2
	 * @param a3 出力引数3
	 */
	static void debug(String format, double a1, double a2, double a3) {
		if (isDebugEnable()) {
			sDebugHandler.accept(String.format(format, a1, a2, a3));
		}
	}

	/**
	 * Delta Systemの設定を設定ファイルから読み込む
	 * @param configFilePath 設定ファイルパス(Java標準の.properties形式)
	 * @throws IOException 設定ファイル読み込み失敗時
	 */
	public static void loadConfig(Path configFilePath) throws IOException {
		assertArgNotNull(configFilePath, "configFilePath");
		try (var reader = Files.newBufferedReader(configFilePath, StandardCharsets.UTF_8)) {
			// 設定ファイルを読み込む
			var config = new Properties();
			config.load(reader);

			// 全てのレーティング種別の設定を展開する
			CONFIGS.stream().forEach(c -> c.load(config));
			sConfigChanged = true;
		}
	}

	/**
	 * Delta Systemの全設定をデバッグ出力
	 * <p>ただし、出力するのはデバッグモードが{@link DsDebugMode#DETAIL}時のみ。</p>
	 */
	public static void printAllSettings() {
		if (getDebugMode() == DsDebugMode.DETAIL) {
			Ds.debug("Delta System all settings ----------");
			Finger.print();
			Fingering.print();
			CONFIGS.stream().forEach(c -> c.print());
		}
	}

	/**
	 * レーティング値分析に必要な楽曲位置を抽出し、その楽曲位置情報を包括したレーティング要素データのリストを生成する
	 * <p>当メソッドでは、フィルタリング関数によって抽出された必要な楽曲位置情報のみに絞り込み、その楽曲位置情報を
	 * 包括するレーティング要素データを、指定のコンストラクタによって生成する。</p>
	 * <p>生成されたレーティング要素データには、1つ前の楽曲位置との時間差分とノートレイアウトを適用した状態のノート種別を
	 * 設定して返される。返したレーティング要素リストはレーティング値分析に使用されることを想定している。</p>
	 * @param <T> レーティング要素データの型
	 * @param ctx Delta System用のコンテキスト
	 * @param constructor レーティング要素データを生成するコンストラクタ
	 * @param filter 楽曲位置情報の絞り込みを行うフィルタリング関数
	 * @return レーティング要素リスト
	 */
	static <T extends RatingElement> List<T> listElements(DsContext ctx,
			BiFunction<DsContext, BeMusicPoint, T> constructor, Predicate<BeMusicPoint> filter) {
		// 条件に合致する楽曲位置のみでレーティング要素のリストを構築する
		var elems = ctx.chart.points().filter(filter).map(p -> constructor.apply(ctx, p)).collect(Collectors.toList());
		var countElem = elems.size();

		// レーティング要素に必要情報を設定する
		var layout = ctx.layout;
		var countDev = BeMusicDevice.COUNT;
		for (var i = 0; i < countElem; i++) {
			// 1つ前の楽曲位置との時間差分を設定する
			var elem = elems.get(i);
			var timeDelta = (i == 0) ? 0.0 : Math.min(RatingElement.MAX_TIME_DELTA, elem.getTime() - elems.get(i - 1).getTime());
			elem.setTimeDelta(timeDelta);

			// ノート配置レイアウトを適用したノート種別を設定する
			var point = elem.getPoint();
			for (var j = 0; j < countDev; j++) {
				var dev = BeMusicDevice.fromIndex(j);
				var ntype = point.getVisibleNoteType(layout.get(dev));
				elem.setNoteType(dev, ntype);
			}
		}

		return elems;
	}

	/**
	 * レーティング要素同士の時間差分計算
	 * @param elems レーティング要素リスト
	 * @param from 要素インデックス始点
	 * @param to 要素インデックス終点
	 * @return fromとtoの時間差分
	 */
	static double timeDelta(List<? extends RatingElement> elems, int from, int to) {
		return elems.get(to).getTime() - elems.get(from).getTime();
	}

	/**
	 * レーティング要素同士の時間差分計算
	 * @param <T> レーティング要素型
	 * @param from 始点レーティング要素
	 * @param to 終点レーティング要素
	 * @return fromとtoの時間差分
	 */
	static <T extends RatingElement> double timeDelta(T from, T to) {
		return to.getTime() - from.getTime();
	}

	/**
	 * 演奏時間取得
	 * <p>当メソッドで言うところの「演奏時間」とは、最後の楽曲位置と最初の楽曲位置の時間差分を指す。</p>
	 * @param elems レーティング要素リスト
	 * @return 演奏時間
	 */
	static double computeTimeOfPointRange(List<? extends RatingElement> elems) {
		return elems.isEmpty() ? 0.0 : (elems.get(elems.size() - 1).getTime() - elems.get(0).getTime());
	}

	/**
	 * レーティング値補正処理
	 * <p>「一般的ではない構成の譜面」に対して評価点の補正(基本的には減点)を行う。ここで言うところの一般的ではないとは、
	 * 「演奏時間が極端に短い」「演奏内容(※)が極端に薄い」等を表す。前述したような譜面は、Delta Systemによる
	 * レーティング値の意義の崩壊を招きかねない要素となる可能性があり、そのような譜面に対してはレーティング値の減点を
	 * 行うことを仕様として規定することでレーティング値の意義を担保しようとする。</p>
	 * <p>Delta Systemでの適正なレーティングを行う譜面は「可能な限り一般的」であることを求めるものとする。</p>
	 * <p>※楽曲位置の数が異常に少ない譜面のこと</p>
	 * @param orgRating 算出したレーティング値のオリジナル値
	 * @param playTime 演奏時間(最初の楽曲位置から最後の楽曲位置までの時間範囲)
	 * @param customRatio レーティング種別ごとに定義される補正倍率(例えば、最低演奏内容に対する実際の演奏内容の比率等)
	 * @return 補正されたレーティング値
	 */
	static double computeRatingValue(double orgRating, double playTime, double customRatio) {
		var config = CommonConfig.getInstance();
		var playTimeRatio = config.ipfnPlayTime.compute(playTime);
		var customRatioAdjust = Math.max(0.0, Math.min(1.0, customRatio));
		return orgRating * playTimeRatio * customRatioAdjust;
	}

	/**
	 * 最終評価点合成における評価点影響度の調整
	 * <p>片側レーンに極端にノートが偏っている場合、両レーンの最終評価点の合成により意図しないレベルで
	 * 最終評価点の低下が発生することがある。そうなると実際には片側レーンで相当に高い数値を示したとしても
	 * 妥当ではない低いレーティング値が出力されるので、最終評価点合成の影響度係数の調整を行いこれを防止する。</p>
	 * <p>当メソッドはダブルプレー専用の機能であり、シングルプレーの譜面では使用されない。</p>
	 * @param v1 評価点影響度の調整要否を判定する値1
	 * @param v2 評価点影響度の調整要否を判定する値2
	 * @param high 値が大きい側の評価点影響度
	 * @param low 値が小さい側の評価点影響度
	 * @param adjustRate 評価点影響度の調整を実施する最大の[低い値 / 高い値]の比率値
	 * @param maxStrength 評価点影響度の最大調整値(比率値が0を示す時の調整値)
	 * @return 調整後の評価点影響度の配列:[0]=高いほうの評価点影響度, [1]=低いほうの評価点影響度
	 */
	static double[] adjustInfluenceForDp(double v1, double v2, double high, double low,
			double adjustRate, double maxStrength) {
		var influenceHigh = high;
		var influenceLow = low;
		var valueLow = Math.min(v1, v2);
		var valueHigh = Math.max(v1, v2);
		var valueRate = (valueHigh == 0) ? 0.0 : (valueLow / valueHigh);
		if (valueRate < adjustRate) {
			var strengthRate = valueRate / adjustRate;
			var adjustStrength = maxStrength - (maxStrength * strengthRate);
			influenceHigh += adjustStrength;
			influenceLow -= adjustStrength;
		}
		return new double[] { influenceHigh, influenceLow };
	}

	/**
	 * 2つの値のマージ処理
	 * <p>指定した2つの値を比較し、高い値、低い値にそれぞれ影響係数を与え、以下の式で合成した結果を返す。</p>
	 * <p>mergedValue = (highValue * influenceHigh) + (lowValue * influenceLow)</p>
	 * @param v1 値1
	 * @param v2 値2
	 * @param influenceHigh 高い値の影響係数
	 * @param influenceLow 低い値の影響係数
	 * @return マージ後の値
	 */
	static double mergeValue(double v1, double v2, double influenceHigh, double influenceLow) {
		var high = Math.max(v1, v2);
		var low = Math.min(v1, v2);
		return (high * influenceHigh) + (low * influenceLow);
	}
}
