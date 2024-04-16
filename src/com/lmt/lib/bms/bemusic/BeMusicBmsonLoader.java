package com.lmt.lib.bms.bemusic;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.lmt.lib.bms.BmsChannel;
import com.lmt.lib.bms.BmsCompatException;
import com.lmt.lib.bms.BmsErrorType;
import com.lmt.lib.bms.BmsInt;
import com.lmt.lib.bms.BmsLoader;
import com.lmt.lib.bms.BmsLoaderSettings;
import com.lmt.lib.bms.BmsMeta;
import com.lmt.lib.bms.BmsScriptError;
import com.lmt.lib.bms.BmsSpec;
import com.lmt.lib.bms.internal.Utility;
import com.lmt.lib.bms.parse.BmsErrorParsed;
import com.lmt.lib.bms.parse.BmsMeasureValueParsed;
import com.lmt.lib.bms.parse.BmsMetaParsed;
import com.lmt.lib.bms.parse.BmsNoteParsed;
import com.lmt.lib.bms.parse.BmsParsed;
import com.lmt.lib.bms.parse.BmsParsedType;
import com.lmt.lib.bms.parse.BmsSource;

/**
 * bmson形式のJSONからBMSコンテンツを生成するBMSローダクラスです。
 *
 * <p>当クラスでサポートするbmsonは、下記外部サイトにて規定されているbmson仕様に準拠したJSON形式のフォーマットを読み込みます。
 * ※表記のURLは外部サイトへのリンクのため予期せず更新・移動・削除される場合があります</p>
 *
 * <p>bmsonの標準的な仕様：<a href="https://bmson-spec.readthedocs.io/en/master/index.html">https://bmson-spec.readthedocs.io/en/master/index.html</a><br>
 * beatorajaのBMSON拡張定義：<a href="https://github.com/exch-bms2/beatoraja/wiki/%E6%A5%BD%E6%9B%B2%E8%A3%BD%E4%BD%9C%E8%80%85%E5%90%91%E3%81%91%E8%B3%87%E6%96%99">https://github.com/exch-bms2/beatoraja/wiki/楽曲製作者向け資料</a><br></p>
 *
 * <p>具体的には、以下のようなフォーマットで記述されたJSONオブジェクトの読み込みに対応しています。</p>
 *
 * <pre>
 * {
 *     "version": "1.0.0",
 *     "info": {
 *         "title": "#TITLE",
 *         "subtitle": "#SUBTITLE",
 *         "artist": "#ARTIST",
 *         "subartists": [
 *             "#SUBARTIST",
 *             "#SUBARTIST(2件目)",
 *             ...
 *         ],
 *         "genre": "#GENRE",
 *         "mode_hint": "beat-7k",  #PLAYER
 *         "chart_name": "#CHARTNAME",
 *         "level": 3,  #PLAYLEVEL
 *         "init_bpm": 150,  #BPM
 *         "judge_rank": 100,  #DEFEXRANK
 *         "total": 300,  #TOTAL
 *         "back_image": "#BACKBMP",
 *         "eyecatch_image": "#EYECATCH",
 *         "title_image": "#STAGEFILE",
 *         "banner_image": "#BANNER",
 *         "preview_music": "#PREVIEW",
 *         "resolution": 240
 *     },
 *     "lines": [  #Ch:02(4/4拍子でない小節のみ)
 *         { "y": 0 },
 *         { "y": 960 },
 *         { "y": 1920 },
 *         ...
 *     ],
 *     "bpm_events": [  #BPMxx, Ch:08
 *         { "y": 480, "bpm": 180 },
 *         ...
 *     ],
 *     "scroll_events": [  #SCROLLxx, Ch:SC
 *         { "y": 960, "rate": 0.5 },
 *         { "y": 1440, "rate": 1.0 },
 *         ...
 *     ],
 *     "stop_events": [  #STOPxx, Ch:09
 *         { "y": 960, "duration": 120 },
 *         ...
 *     ],
 *     "sound_channels": [  #WAVxx, Ch:01,1x,2x,5x,6x
 *         {
 *             "name": "track01.wav",
 *             "notes": [
 *                 { "x": 1, "y": 0, "l": 0, "c": false, "t": 0, "up": false },
 *                 { "x": 2, "y": 240, "l": 480, "c": true", "t": 2, "up": false },
 *                 ...
 *             ]
 *         },
 *         {
 *             "name": "track02.wav",
 *             "notes": [ ... ]
 *         },
 *         ...
 *     ],
 *     "mine_channels": [  #WAV00, #Ch:Dx,Ex
 *         {
 *             "name": "mine.wav",
 *             "notes": [
 *                 { "x": 7, "y": 240, "damage": 1 },
 *                 { "x": 6, "y": 240, "damage": 2 },
 *                 ...
 *             ]
 *         },
 *         ...
 *     ],
 *     "key_channels": [  #WAVxx, Ch:3x,4x
 *         {
 *             "track91.wav",
 *             "notes": [
 *                 { "x": 3, "y": 960 },
 *                 { "x": 5, "y": 1200 },
 *                 { "x": 7, "y": 1200 },
 *                 ...
 *             ]
 *         },
 *         ...
 *     ],
 *     "bga": {
 *         "bga_header": [  #BMPxx
 *             { "id": 1, "name": "image01.bmp" },
 *             { "id": 2, "name": "image02.bmp" },
 *             { "id": 100, "name": "layer.bmp" },
 *             { "id": 101, "name": "miss.bmp" },
 *             ...
 *         ],
 *         "bga_events": [  #Ch:04
 *             { "y": 0, "id": 1 },
 *             { "y": 960, "id": 2 },
 *             ...
 *         ],
 *         "layer_events": [  #Ch:07
 *             { "y": 0, "id": 100 },
 *             ...
 *         ],
 *         "poor_events": [  #Ch:06
 *             { "y": 0, "id": 101 },
 *             ...
 *         ]
 *     }
 * }
 * </pre>
 *
 * <p>当クラスでのbmsonの読み込み仕様については以下の記述を参照してください。
 * 記述のない動作については冒頭の外部リンクの仕様に準拠し、非準拠の箇所が検出されると原則的に読み込みエラーとなります。
 * ただし、読み込みが続行可能であればエラー報告のみとなる場合があります。</p>
 *
 * <ul>
 * <li>対応するbmsonのバージョンは "1.0.0" です。それ以外のバージョンのbmsonは読み込みエラーとなります。</li>
 * <li>対応する "mode_hint" の値は "beat-5k", "beat-7k", "beat-10k", "beat-14k" です。それ以外の値は読み込みエラーとなります。</li>
 * <li>"chart_name" の値が{@link BeMusicDifficulty}の定義と同じ場合、値は{@link BeMusicMeta#DIFFICULTY}に設定されます。</li>
 * <li>"judge_rank" の値は{@link BeMusicMeta#DEFEXRANK}に設定され、{@link BeMusicMeta#RANK}には値は設定されません。</li>
 * <li>4/4拍子ではない小節は小節長変更チャンネル{@link BeMusicChannel#LENGTH}が出力されます。</li>
 * <li>"sound_channels" のノート定義では、beatoraja固有の定義 "t", "up" は省略可能です。</li>
 * <li>"sound_channels" で定義されたロングノートはMGQ形式({@link BeMusicChannel#LONG_1P_01}参照}に出力されます。</li>
 * <li>"mine_channels" で複数トラック定義されても音声ファイルは全て #WAV00 にアサインされ、後発の定義が優先されます。</li>
 * <li>{@link BmsLoader#setSkipReadTimeline(boolean)}でタイムライン読み込みを無効化している場合、全てのチャンネルは出力されません。</li>
 * <li>索引付きメタ情報のインデックス値が{@link BmsSpec#INDEXED_META_INDEX_MAX}を超えるような定義は全て
 * {@link BmsErrorType#WRONG_DATA}として処理されます。この制限事項の影響を受けるのは
 * "bpm_events", "scroll_events", "stop_events", "sound_channels", "key_channels", "bga_header" です。</li>
 * </ul>
 */
public class BeMusicBmsonLoader extends BmsLoader {
	/** bmsonローダで対応するバージョン一覧 */
	private static final List<String> COMPATIBLE_VERSIONS = List.of("1.0.0");

	/** "mode_hit"から#PLAYERへの変換マップ */
	private static final Map<String, BeMusicPlayer> PLAYER_MAP = Map.ofEntries(
			Map.entry("beat-5k", BeMusicPlayer.SINGLE),
			Map.entry("beat-7k", BeMusicPlayer.SINGLE),
			Map.entry("beat-10k", BeMusicPlayer.DOUBLE),
			Map.entry("beat-14k", BeMusicPlayer.DOUBLE));

	/** "chart_name"から#DIFFICULTYへの変換マップ */
	private static final Map<String, BeMusicDifficulty> DIFFICULTY_MAP = Map.ofEntries(
			Map.entry("beginner", BeMusicDifficulty.BEGINNER),
			Map.entry("normal", BeMusicDifficulty.NORMAL),
			Map.entry("hyper", BeMusicDifficulty.HYPER),
			Map.entry("another", BeMusicDifficulty.ANOTHER),
			Map.entry("insane", BeMusicDifficulty.INSANE));

	/** "ln_type"の有効な値一覧(値0は含まない) */
	private static final Set<Long> LNMODES = Stream.of(BeMusicLongNoteMode.values())
			.map(m -> m.getNativeValue())
			.collect(Collectors.toSet());

	/** 処理フェーズマップ */
	private static final Map<Integer, ParserPhase> PHASE_MAP = Stream.of(ParserPhase.values())
			.collect(Collectors.toMap(p -> p.getNumber(), p -> p));

	/** 処理フェーズとその処理を行うメソッドのマップ */
	private Map<ParserPhase, ParserMethod> mParsers;

	/** bmsonのルートオブジェクト */
	private JSONObject mRoot;
	/** "bga"のオブジェクト(未定義の場合はnullになる) */
	private JSONObject mBga;
	/** 処理フェーズ */
	private ParserPhase mPhase;
	/** 出力する要素リスト */
	private Deque<BmsParsed> mElements;
	/** 4/4拍子時の1小節あたりのパルス数("resolution"の4倍の値) */
	private long mPulsePerMeasure;
	/** 当該小節の初期位置の絶対パルス数をキーにした小節情報マップ */
	private TreeMap<Long, MeasureInfo> mMeasures;
	/** 音声リソースの名前をキーにしたトラックID(索引付きメタ情報のインデックス値)へのマップ */
	private Map<String, Integer> mWavs;

	/** JSONオブジェクトにおけるデータ型 */
	private static enum JsonType {
		/** 文字列 */
		STR,
		/** 整数 */
		INT,
		/** 実数 */
		NUM,
		/** 配列 */
		ARY,
		/** オブジェクト */
		OBJ,
	}

	/** 処理フェーズ */
	private static enum ParserPhase {
		/** "info"解析 */
		INFO(1, "info"),
		/** "lines"解析 */
		LINES(2, "lines"),
		/** "bpm_events"解析 */
		BPM_EVENTS(3, "bpm_events"),
		/** "scroll_events"解析 */
		SCROLL_EVENTS(4, "scroll_events"),
		/** "stop_events"解析 */
		STOP_EVENTS(5, "stop_events"),
		/** "sound_channels"解析 */
		SOUND_CHANNELS(6, "sound_channels"),
		/** "mine_channels"解析 */
		MINE_CHANNELS(7, "mine_channels"),
		/** "key_channels"解析 */
		KEY_CHANNELS(8, "key_channels"),
		/** "bga"解析 */
		BGA(9, "bga"),
		/** "bga_header"解析 */
		BGA_HEADER(10, "bga_header"),
		/** "bga_events"解析 */
		BGA_EVENTS(11, "bga_events"),
		/** "layer_events"解析 */
		LAYER_EVENTS(12, "layer_events"),
		/** "poor_events"解析 */
		POOR_EVENTS(13, "poor_events"),
		/** 処理終了 */
		DONE(14, "");

		/** フェーズ番号 */
		private int mNumber;
		/** 解析対象アイテムの名前 */
		private String mRootName;

		/**
		 * コンストラクタ
		 * @param number フェーズ番号
		 * @param rootName 解析対象アイテムの名前
		 */
		private ParserPhase(int number, String rootName) {
			mNumber = number;
			mRootName = rootName;
		}

		/**
		 * フェーズ番号取得
		 * @return フェーズ番号
		 */
		final int getNumber() {
			return mNumber;
		}

		/**
		 * 解析対象アイテムの名前
		 * @return 解析対象アイテムの名前
		 */
		final String getRootName() {
			return mRootName;
		}
	}

	/** 解析処理メソッド */
	@FunctionalInterface
	private static interface ParserMethod {
		/**
		 * 解析処理実行
		 * @throws BmsCompatException 互換性エラー発生時
		 */
		void parse() throws BmsCompatException;
	}

	/** 小節情報 */
	private static class MeasureInfo {
		/** 小節番号 */
		int measure;
		/** 小節先頭位置の絶対パルス番号 */
		long pulseNumber;
		/** 小節のパルス数 */
		long pulseCount;
		/** 小節長(4/4拍子を1とする) */
		double length;

		/**
		 * コンストラクタ
		 * @param measure 小節番号
		 * @param pulseNumber 小節先頭位置の絶対パルス番号
		 * @param pulseCount 小節のパルス数
		 * @param pulsePerMeasure 1小節あたりのパルス数
		 */
		MeasureInfo(int measure, long pulseNumber, long pulseCount, long pulsePerMeasure) {
			this.measure = measure;
			this.pulseNumber = pulseNumber;
			this.pulseCount = pulseCount;
			this.length = (pulseCount == 0L) ? 1.0 : (double)pulseCount / pulsePerMeasure;
		}

		/**
		 * 指定絶対パルス番号の刻み位置計算
		 * @param absPulseNumber 絶対パルス番号
		 * @return 刻み位置
		 */
		final double computeTick(long absPulseNumber) {
			// 小節の長さから刻み位置を計算し、刻み位置に変換する
			var relPulse = absPulseNumber - this.pulseNumber;
			var posValue = (this.pulseCount == 0L) ? 0.0 : (double)relPulse / this.pulseCount;
			var tick = BmsSpec.TICK_COUNT_DEFAULT * this.length * posValue;
			return tick;
		}
	}

	/** チャンネルデータ単位 */
	private static class ChannelUnit {
		/** チャンネル番号 */
		int channel;
		/** 小節番号 */
		int measure;
		/** 小節長(この情報は付属情報としてのみ使用する) */
		double length;

		/**
		 * コンストラクタ
		 * @param channel チャンネル番号
		 * @param measure 小節番号
		 */
		ChannelUnit(int channel, int measure) {
			set(channel, measure);
		}

		/**
		 * コンストラクタ
		 * @param channel チャンネル番号
		 * @param measure 小節番号
		 * @param length 小節長
		 */
		ChannelUnit(int channel, int measure, double length) {
			set(channel, measure);
			this.length = length;
		}

		@Override
		public boolean equals(Object obj) {
			if ((obj != null) && (obj instanceof ChannelUnit)) {
				var u = (ChannelUnit)obj;
				return (this.channel == u.channel) && (this.measure == u.measure);
			} else {
				return false;
			}
		}

		@Override
		public int hashCode() {
			return Objects.hash(BmsInt.box(this.channel), BmsInt.box(this.measure));
		}

		@Override
		public String toString() {
			return String.format("#%05d%s:", this.measure, BmsInt.to36s(this.channel));
		}

		/**
		 * キー情報設定
		 * @param channel チャンネル番号
		 * @param measure 小節番号
		 * @return このオブジェクトのインスタンス
		 */
		final ChannelUnit set(int channel, int measure) {
			this.channel = channel;
			this.measure = measure;
			return this;
		}
	}

	/** チャンネルデータ */
	private static abstract class ChannelData {
		/** 重複不可チャンネルのチャンネルデータ */
		private static class Single extends ChannelData {
			/** 刻み位置をキーにしたチャンネルデータマップ */
			private Map<Double, Integer> mData = new TreeMap<>();

			@Override
			Integer get(double tick) {
				return mData.get(tick);
			}

			@Override
			void put(double tick, int value) {
				mData.put(Double.valueOf(tick), BmsInt.box(value));
			}

			@Override
			List<Map<Double, Integer>> data() {
				return List.of(mData);
			}
		}

		/** 重複可能チャンネルのチャンネルデータ */
		private static class Multiple extends ChannelData {
			/** 刻み位置をキーにしたチャンネルデータマップ(重複した位置への登録はマップを複数生成する) */
			List<Map<Double, Integer>> mData = new ArrayList<>();

			/** コンストラクタ */
			private Multiple() {
				mData.add(new TreeMap<>());
			}

			@Override
			Integer get(double tick) {
				// ※このクラスの実装では、リストの最初から検索を行い、最初に見つかったノートの値を返す
				for (var map : mData) {
					var value = map.get(tick);
					if (value != null) { return value; }
				}
				return null;
			}

			@Override
			void put(double tick, int value) {
				var boxTick = Double.valueOf(tick);
				var boxValue = BmsInt.box(value);
				for (var map : mData) { if (!map.containsKey(boxTick)) { map.put(boxTick, boxValue); return; } }
				var newMap = new TreeMap<Double, Integer>();
				newMap.put(boxTick, boxValue);
				mData.add(newMap);
			}

			@Override
			List<Map<Double, Integer>> data() {
				return mData;
			}
		}

		/**
		 * チャンネルデータ生成
		 * @param multiple 重複可否フラグ
		 * @return チャンネルデータ
		 */
		static ChannelData create(boolean multiple) {
			return multiple ? new Multiple() : new Single();
		}

		/**
		 * 刻み位置に対応するノートの値取得
		 * @param tick 刻み位置
		 * @return ノートの値。なければnull。
		 */
		abstract Integer get(double tick);

		/**
		 * チャンネルデータ登録
		 * @param tick 刻み位置
		 * @param value ノートの値
		 */
		abstract void put(double tick, int value);

		/**
		 * チャンネルデータリスト取得
		 * @return チャンネルデータリスト
		 */
		abstract List<Map<Double, Integer>> data();
	}

	/**
	 * タイムラインの定義をBMSライブラリの形式に変換するクラスのベースクラス
	 * @param <NDT> 1ノートのデータ変換後の型
	 */
	private abstract class TimelineExchanger<NDT> {
		/** 解析対象配列データを持つ親オブジェクトの参照 */
		protected JSONObject mParent;
		/** 配列データの名前 */
		protected String mArrayName;
		/** 対象の処理フェーズ */
		protected ParserPhase mTargetPhase;
		/** 当該タイムラインに関連するメタ情報(関連するものがなければnull) */
		protected BmsMeta mMeta;
		/** 変換処理で発見されたエラー一覧(キーはエラーの種類を表す通し番号) */
		protected Map<Integer, BmsParsed> mErrors = new TreeMap<>();

		/**
		 * 配列データの定義が必須かどうか取得
		 * @return 配列データの定義が必須であればtrue
		 */
		protected boolean isDefineMandatory() {
			return false;
		}

		/**
		 * 指定ノートオブジェクトの取り込みをスキップするかどうかの判定
		 * @param o 判定対象のノートオブジェクト
		 * @param pulseNumber ノートオブジェクトの絶対パルス番号
		 * @param objectIndex ノートオブジェクトの0から始まるインデックス値
		 * @return 指定ノートオブジェクトの取り込みをスキップする場合true
		 */
		protected boolean isSkipNote(JSONObject o, long pulseNumber, int objectIndex) {
			return false;
		}

		/**
		 * ノートオブジェクトから解析処理で使用する形式に変換する
		 * @param o 変換対象のノートオブジェクト
		 * @return 変換後のノートオブジェクト
		 */
		protected NDT castNoteData(JSONObject o) {
			return null;
		}

		/**
		 * 変換後ノートオブジェクトの検査を行う
		 * @param pulseNumber 検査対象ノートオブジェクトの絶対パルス番号
		 * @param noteData 検査対象の変換後ノートオブジェクト
		 * @return 検査合格時はnull、不合格時は不合格理由を表すエラー種別
		 */
		protected BmsErrorType testNoteData(long pulseNumber, NDT noteData) {
			return null;
		}

		/**
		 * 変換後ノートオブジェクトをメタ情報として追記する
		 * <p>追記が不要(既に同じデータが存在する)の場合には何もしなくて良い。その際はtrueを返すこと。</p>
		 * @param noteData 変換後ノートオブジェクト
		 * @return 追記成功・不要時はtrue、メタ情報数過多により追記不可の場合のみfalseが返る
		 */
		protected boolean putMetaData(NDT noteData) {
			return true;
		}

		/**
		 * ノートオブジェクトをチャンネルデータへ追記する
		 * @param pulseNumber 追記対象ノートオブジェクトの絶対パルス番号
		 * @param noteData 追記対象ノートオブジェクト
		 */
		protected void putNoteData(long pulseNumber, NDT noteData) {
			// Do nothing
		}

		/**
		 * 変換処理完了時の処理
		 */
		protected void finishExchange() {
			// Do nothing
		}

		/**
		 * コンストラクタ
		 * @param parent 解析対象配列データを持つ親オブジェクトの参照
		 * @param arrayName 配列データの名前
		 * @param targetPhase 対象の処理フェーズ
		 * @param meta 当該タイムラインに関連するメタ情報
		 */
		protected TimelineExchanger(JSONObject parent, String arrayName, ParserPhase targetPhase, BmsMeta meta) {
			mParent = parent;
			mArrayName = (arrayName == null) ? targetPhase.getRootName() : arrayName;
			mTargetPhase = targetPhase;
			mMeta = meta;
		}

		/**
		 * 変換処理で発見されたエラーを出力
		 */
		protected final void publishErrors() {
			publishElements(mErrors.values());
		}

		/**
		 * 指定メタ情報を出力
		 * @param <VT> メタ情報の値の型(これがメタ情報マップのキーになる)
		 * @param metas メタ情報の値をキーにした索引付きメタ情報のインデックス値マップ
		 */
		protected final <VT> void publishMetaData(Map<VT, Integer> metas) {
			publishMaterialMetas(mTargetPhase, mMeta, metas, e -> e.getValue(), e -> e.getKey().toString());
		}

		/**
		 * 指定チャンネルデータを出力
		 * <p>キーはチャンネル番号・小節番号をキーとする。つまり、「#111AA:」の単位ごとにデータを持っている。</p>
		 * @param chDataMap チャンネルデータマップ
		 */
		protected final void publishChannelData(Map<ChannelUnit, ChannelData> chDataMap) {
			var phaseNumber = mTargetPhase.getNumber();
			for (var entry : chDataMap.entrySet()) {
				var unit = entry.getKey();
				var tickCount = (int)Math.round(BmsSpec.TICK_COUNT_DEFAULT * unit.length);
				var dataList = entry.getValue().data();
				for (var data : dataList) {
					// 刻み位置の最大公約数を算出する
					var division = tickCount;
					for (var tick : data.keySet()) { division = Utility.gcd(division, tick.intValue()); }

					// 配列データを生成する
					var arrayLength = (int)(tickCount / division);
					var array = new ArrayList<Integer>(arrayLength);
					for (var i = 0; i < arrayLength; i++) { array.add(0); }
					for (var e : data.entrySet()) { array.set(e.getKey().intValue() / division, e.getValue()); }
					var element = new BmsNoteParsed(phaseNumber, unit, unit.measure, unit.channel, array);
					publishElement(element);
				}
			}
		}

		/**
		 * 変換処理実行
		 * @throws BmsCompatException 互換性エラー発生時
		 */
		void exchange() throws BmsCompatException {
			try {
				if (mParent == null) {
					// 親オブジェクトが未指定の場合は何もしない
					return;
				}
				var noteArray = mParent.optJSONArray(mArrayName);
				if (noteArray == null) {
					if (mParent.has(mArrayName)) {
						// 定義があり、且つ配列の取得に失敗した場合は構文エラーとする
						var line = String.format("\"%s\": ???", mArrayName);
						var msg = String.format("'%s' wrong format.", mArrayName);
						mErrors.put(1, achSyntax(mTargetPhase, line, msg));
					} else if (isDefineMandatory()) {
						// 定義必須であるにも関わらず未定義の場合は構文エラーとする
						var line = "";
						var msg = String.format("'%s' undefined.", mArrayName);
						mErrors.put(1, achSyntax(mTargetPhase, line, msg));
					} else {
						// 定義が任意であれば未定義時はエラーを発出しない
						// Do nothing
					}
					return;
				}

				// ノートの情報を構築する
				var rootName = mTargetPhase.getRootName();
				var noteCount = noteArray.length();
				for (var i = 0; i < noteCount; i++) {
					// ノートの情報を取得する
					var dataObj = noteArray.optJSONObject(i);
					if (dataObj == null) {
						// ノートの情報が取得できない(意図する形式ではない)場合は構文エラー
						if (!mErrors.containsKey(2)) {
							var line = String.format("%s[%d]", mArrayName, i);
							var msg = String.format("'%s' information is in un-expected format.", rootName);
							mErrors.put(2, achSyntax(mTargetPhase, line, msg));

						}
						continue;
					}

					// ノートの情報のパルス番号の存在をチェックする
					if (!dataObj.has("y")) {
						// パルス番号が未定義の場合は構文エラー
						if (!mErrors.containsKey(3)) {
							mErrors.put(3, achSyntax(mTargetPhase, dataObj, "Undefined pulse number 'y'."));
						}
						continue;
					}

					// ノートの情報のデータ内容をチェックする
					var pulseNumber = dataObj.optLong("y", -1L);
					if (pulseNumber < 0L) {
						// パルス番号が形式不正、負の値の場合はデータ不正
						if (!mErrors.containsKey(4)) {
							mErrors.put(4, achWrong(mTargetPhase, dataObj, "Wrong pulse number 'y'."));
						}
						continue;
					}
					if (isSkipNote(dataObj, pulseNumber, i)) {
						// このノートの情報をスキップする場合は以降の処理は行わない
						continue;
					}
					var noteData = castNoteData(dataObj);
					var testDataErr = testNoteData(pulseNumber, noteData);
					if (testDataErr != null) {
						// データが形式不明の場合はデータ不正
						if (!mErrors.containsKey(5)) {
							var msg = String.format("'%s' data verification error.", rootName);
							mErrors.put(5, achError(testDataErr, mTargetPhase, dataObj, msg));
						}
						continue;
					}

					// メタ情報を構築する
					if (mMeta != null) {
						if (!putMetaData(noteData)) {
							// メタ情報の数がライブラリで扱える上限を超過する場合はデータ不正とする
							if (!mErrors.containsKey(6)) {
								var msg = String.format("Too many '%s' data.", rootName);
								mErrors.put(6, achWrong(mTargetPhase, dataObj, msg));
							}
							continue;
						}
					}

					// チャンネルデータを構築する
					putNoteData(pulseNumber, noteData);
				}
			} finally {
				// タイムラインデータ変換終了
				finishExchange();
			}
		}
	}

	/** "lines"の解析処理 */
	private class BarLineExchanger extends TimelineExchanger<Void> {
		/** 1個前に解析した小節情報の絶対パルス番号 */
		private long mPrevPulseNumber;

		/**
		 * コンストラクタ
		 * @param root "lines"の親オブジェクト
		 */
		BarLineExchanger(JSONObject root) {
			super(root, null, ParserPhase.LINES, null);
			mPrevPulseNumber = 0L;
		}

		@Override
		protected boolean isSkipNote(JSONObject o, long pulseNumber, int objectIndex) {
			// 初回のパルス番号0は省略する
			return (pulseNumber == 0L) && (objectIndex == 0);
		}

		@Override
		protected BmsErrorType testNoteData(long pulseNumber, Void noteData) {
			var pulseCount = pulseNumber - mPrevPulseNumber;
			return (pulseCount <= 0L) ? BmsErrorType.WRONG_DATA : null;
		}

		@Override
		protected void putNoteData(long pulseNumber, Void noteData) {
			var pulseCount = pulseNumber - mPrevPulseNumber;
			var info = new MeasureInfo(mMeasures.size(), mPrevPulseNumber, pulseCount, mPulsePerMeasure);
			mMeasures.put(mPrevPulseNumber, info);
			mPrevPulseNumber = pulseNumber;
		}

		@Override
		protected void finishExchange() {
			// 発生したエラーを出力する
			publishErrors();

			// 小節情報のうち、4/4拍子(小節長が1.0)ではない小節を小節長変更として出力する。
			// 4/4拍子はデフォルト値であるため、出力する必要はない。
			var phaseNumber = mPhase.getNumber();
			var chNum = BeMusicChannel.LENGTH.getNumber();
			var o = mMeasures.entrySet().stream().filter(e -> e.getValue().length != 1.0).collect(Collectors.toList());
			for (var e : o) {
				var minfo = e.getValue();
				var line = String.format("{ \"y\": %d }", e.getKey());
				var value = String.valueOf(minfo.length);
				var element = new BmsMeasureValueParsed(phaseNumber, line, minfo.measure, chNum, value);
				publishElement(element);
			}

			// 最終小節の小節情報を登録する
			if (mMeasures.isEmpty()) {
				// 小節情報が1件もない場合は4/4拍子1小節分を登録しておく
				// "y":0 が 1件のみだと登録が省略されるので、後の処理に不都合がないようここで解決しておく
				var minfo = new MeasureInfo(0, 0, mPulsePerMeasure, mPulsePerMeasure);
				mMeasures.put(0L, minfo);
			} else {
				// 最終小節は特殊な小節情報とする
				// パルス数は0で、小節長は1.0となり、この小節にノートを配置すると必ず刻み位置が0になる
				var minfo = new MeasureInfo(mMeasures.size(), mPrevPulseNumber, 0, 0);
				mMeasures.put(mPrevPulseNumber, minfo);
			}
		}
	}

	/** チャンネルデータの解析処理ベース */
	private abstract class ChannelDataExchanger<NDT> extends TimelineExchanger<NDT> {
		/** チャンネルデータ */
		private Map<ChannelUnit, ChannelData> mChDataMap = new HashMap<>();
		/** 既存チャンネルデータ検索用の一時キー */
		private ChannelUnit mChUnit = new ChannelUnit(0, 0);

		/**
		 * コンストラクタ
		 * @param parent 解析対象配列データを持つ親オブジェクトの参照
		 * @param arrayName 配列データの名前
		 * @param targetPhase 対象の処理フェーズ
		 * @param meta 当該タイムラインに関連するメタ情報
		 */
		protected ChannelDataExchanger(JSONObject parent, String arrayName, ParserPhase targetPhase, BmsMeta meta) {
			super(parent, arrayName, targetPhase, meta);
		}

		@Override
		protected void putNoteData(long pulseNumber, NDT noteData) {
			// ベースクラスでは単純な追記処理をデフォルト実装する
			putNoteData(pulseNumber, getChannel(noteData), BmsInt.box(getNoteValue(noteData)));
		}

		@Override
		protected void finishExchange() {
			publishErrors();
			publishChannelData();
		}

		/**
		 * 既存のノートの値取得
		 * @param measure 小節番号
		 * @param tick 小節の刻み位置
		 * @param channel チャンネル
		 * @return 該当するノートの値。なければnull。
		 */
		protected final Integer getNoteData(int measure, double tick, BmsChannel channel) {
			var chData = mChDataMap.get(mChUnit.set(channel.getNumber(), measure));
			return (chData == null) ? null : chData.get(tick);
		}

		/**
		 * ノートオブジェクトをチャンネルデータへ追記する
		 * @param pulseNumber 絶対パルス番号
		 * @param channel チャンネル
		 * @param noteValue ノートの値
		 */
		protected final void putNoteData(long pulseNumber, BmsChannel channel, Integer noteValue) {
			// 絶対パルス番号から小節番号・刻み位置を計算後、その位置へ追記する
			var minfo = getMeasureInfo(pulseNumber);
			var tick = minfo.computeTick(pulseNumber);
			putNoteData(minfo.measure, tick, minfo.length, channel, noteValue);
		}

		/**
		 * ノートオブジェクトをチャンネルデータへ追記する
		 * @param measure 小節番号
		 * @param tick 小節の刻み位置
		 * @param length 小節長(補足情報として必要)
		 * @param channel チャンネル
		 * @param noteValue ノートの値
		 */
		protected final void putNoteData(int measure, double tick, double length, BmsChannel channel, Integer noteValue) {
			var chNum = channel.getNumber();
			var chData = mChDataMap.get(mChUnit.set(chNum, measure));
			if (chData == null) {
				// 新しいデータ単位であれば新規作成する
				chData = ChannelData.create(channel.isMultiple());
				mChDataMap.put(new ChannelUnit(chNum, measure, length), chData);
			}
			chData.put(tick, noteValue);
		}

		/**
		 * ノートオブジェクトに該当するチャンネル取得
		 * <p>primary=trueを指定する。</p>
		 * @param noteData ノートオブジェクト
		 * @return ノートオブジェクトに該当するチャンネル。該当なければnull。
		 */
		protected final BmsChannel getChannel(NDT noteData) {
			return getChannel(noteData, true);
		}

		/**
		 * このオブジェクトが保有するチャンネルデータを出力
		 */
		protected final void publishChannelData() {
			publishChannelData(mChDataMap);
		}

		/**
		 * ノートオブジェクトに該当するチャンネル取得
		 * @param noteData ノートオブジェクト
		 * @param primary 主チャンネル使用フラグ
		 * @return ノートオブジェクトに該当するチャンネル。該当なければnull。
		 */
		protected abstract BmsChannel getChannel(NDT noteData, boolean primary);

		/**
		 * ノートオブジェクトからノートの値取得
		 * @param noteData ノートオブジェクト
		 * @return ノートの値
		 */
		protected abstract int getNoteValue(NDT noteData);
	}

	/** 時間・スクロールに関する解析処理ベース */
	private class TimeEventsExchanger extends ChannelDataExchanger<Double> {
		/** 対象チャンネル */
		private BmsChannel mChannel;
		/** メタ情報マップ */
		private Map<Double, Integer> mMetas = new LinkedHashMap<>();

		/**
		 * コンストラクタ
		 * @param parent 解析対象配列データを持つ親オブジェクトの参照
		 * @param phase 対象の処理フェーズ
		 * @param meta 対象のメタ情報
		 * @param channel 対象のチャンネル
		 */
		protected TimeEventsExchanger(JSONObject parent, ParserPhase phase, BmsMeta meta, BmsChannel channel) {
			super(parent, null, phase, meta);
			mChannel = channel;
		}

		@Override
		protected boolean putMetaData(Double noteData) {
			// 初出のメタ情報があればメタ情報マップに登録しておく
			// ここで登録されたメタ情報は解析処理終了時に出力する
			if (mMetas.containsKey(noteData)) {
				// 既に同じ値を登録済み
				return true;
			} else if (mMetas.size() >= BmsSpec.INDEXED_META_INDEX_MAX) {
				// メタ情報の登録条件を超過するためエラー
				return false;
			} else {
				// 初出のメタ情報。インデックス値は1から始まる連番とする。
				mMetas.put(noteData, mMetas.size() + 1);
				return true;
			}
		}

		@Override
		protected void finishExchange() {
			publishErrors();
			publishMetaData(mMetas);
			publishChannelData();
		}

		@Override
		protected BmsChannel getChannel(Double noteData, boolean primary) {
			// 時間・スクロールに関するタイムラインでは1個のチャンネルしか扱わない
			return mChannel;
		}

		@Override
		protected int getNoteValue(Double noteData) {
			// ノートの値はメタ情報のインデックス値
			return mMetas.get(noteData);
		}
	}

	/** "bpm_events"の解析処理 */
	private class BpmEventsExchanger extends TimeEventsExchanger {
		/**
		 * コンストラクタ
		 * @param root "bpm_events"の親オブジェクト
		 */
		BpmEventsExchanger(JSONObject root) {
			super(root, ParserPhase.BPM_EVENTS, BeMusicMeta.BPM, BeMusicChannel.BPM);
		}

		@Override
		protected Double castNoteData(JSONObject o) {
			// "bpm"の値をそのまま返す
			return o.has("bpm") ? o.optDouble("bpm") : null;
		}

		@Override
		protected BmsErrorType testNoteData(long pulseNumber, Double noteData) {
			if (noteData == null) {
				// "bpm"が未定義の場合は構文エラーとする
				return BmsErrorType.SYNTAX;
			} else if (noteData.isNaN() || (noteData < 0.0)) {
				// "bpm"が配列・オブジェクト等の数値ではない、または負の値の場合はエラーとして扱う
				return BmsErrorType.WRONG_DATA;
			} else {
				// OK(BPMの値の範囲チェックは読み込み処理部で行うためここではエラーとして扱わない)
				return null;
			}
		}
	}

	/** "scroll_events"の解析処理 */
	private class ScrollEventsExchanger extends TimeEventsExchanger {
		/**
		 * コンストラクタ
		 * @param root "scroll_events"の親オブジェクト
		 */
		ScrollEventsExchanger(JSONObject root) {
			super(root, ParserPhase.SCROLL_EVENTS, BeMusicMeta.SCROLL, BeMusicChannel.SCROLL);
		}

		@Override
		protected Double castNoteData(JSONObject o) {
			// "rate"の値をそのまま返す
			return o.has("rate") ? o.optDouble("rate") : null;
		}

		@Override
		protected BmsErrorType testNoteData(long pulseNumber, Double noteData) {
			if (noteData == null) {
				// "rate"が未定義の場合は構文エラーとする
				return BmsErrorType.SYNTAX;
			} else if (noteData.isNaN()) {
				// "rate"が配列・オブジェクト等の数値ではない場合はエラーとして扱う
				return BmsErrorType.WRONG_DATA;
			} else {
				// OK(スクロール速度は0や負の値もあり得るため、上記以外のエラーチェックは行わない)
				return null;
			}
		}
	}

	/** "stop_events"の解析処理 */
	private class StopEventsExchanger extends TimeEventsExchanger {
		/**
		 * コンストラクタ
		 * @param root "stop_events"の親オブジェクト
		 */
		StopEventsExchanger(JSONObject root) {
			super(root, ParserPhase.STOP_EVENTS, BeMusicMeta.STOP, BeMusicChannel.STOP);
		}

		@Override
		protected Double castNoteData(JSONObject o) {
			// "duration"の値をそのまま返す
			return o.has("duration") ? o.optDouble("duration") : null;
		}

		@Override
		protected BmsErrorType testNoteData(long pulseNumber, Double noteData) {
			if (noteData == null) {
				// "duration"が未定義の場合は構文エラーとする
				return BmsErrorType.SYNTAX;
			} else if (noteData.isNaN() || (noteData < 0.0)) {
				// "duration"が配列・オブジェクト等の数値ではない、または負の値の場合はエラーとして扱う
				return BmsErrorType.WRONG_DATA;
			} else {
				// OK(停止時間の値の範囲チェックは読み込み処理部で行うためここではエラーとして扱わない)
				return null;
			}
		}
	}

	/** 複数チャンネルへのマッピングが必要なタイムラインの解析処理ベース */
	private abstract class TrackExchanger extends ChannelDataExchanger<JSONObject> {
		/** 第1チャンネルリスト */
		private BmsChannel[] mPrimary;
		/** 第2チャンネルリスト */
		private BmsChannel[] mSecondary;
		/** トラックID */
		private int mTrackId;

		/**
		 * コンストラクタ
		 * @param targetPhase 対象の処理フェーズ
		 * @param primary 第1チャンネルリスト
		 * @param secondary 第2チャンネルリスト
		 */
		protected TrackExchanger(ParserPhase targetPhase, BmsChannel[] primary, BmsChannel[] secondary) {
			super(null, "notes", targetPhase, null);
			mPrimary = primary;
			mSecondary = secondary;
			mTrackId = 0;
		}

		@Override
		protected JSONObject castNoteData(JSONObject o) {
			// キャストは行わず、ノートオブジェクトをそのまま返す
			return o;
		}

		@Override
		protected BmsChannel getChannel(JSONObject noteData, boolean primary) {
			// マッピング先チャンネルリストは第1、第2がある
			var channels = primary ? mPrimary : mSecondary;
			var x = noteData.optInt("x", 0);  // "x": null を許容するチャンネルでは0として扱う
			return ((x >= 0) && (x < channels.length)) ? channels[x] : null;
		}

		@Override
		protected void finishExchange() {
			// 変換終了時には出力しない。
			// 音声チャンネルは複数のトラックを全て溜め込んだ後に出力しないと完全な楽曲データにならないため。
		}

		@Override
		void exchange() throws BmsCompatException {
			// exchange(JSONObject, int) を使用すること
			throw new UnsupportedOperationException();
		}

		/**
		 * 変換処理実行
		 * @param parent 解析対象配列データを持つ親オブジェクトの参照
		 * @param trackId トラックID
		 * @throws BmsCompatException 互換性エラー発生時
		 */
		final void exchange(JSONObject parent, int trackId) throws BmsCompatException {
			this.mParent = parent;
			mTrackId = trackId;
			super.exchange();
		}

		/**
		 * トラックID取得
		 * @return トラックID
		 */
		protected final int getTrackId() {
			return mTrackId;
		}

		/**
		 * 全てのデータを出力
		 */
		final void publishAll() {
			publishErrors();
			publishChannelData();
		}
	}

	/** "sound_channels"解析処理 */
	private class SoundExchanger extends TrackExchanger {
		/** コンストラクタ */
		protected SoundExchanger() {
			super(ParserPhase.SOUND_CHANNELS,
					channels(BeMusicChannel.BGM, BeMusicDevice::getVisibleChannel),
					channels(null, BeMusicDevice::getLongChannel));
		}

		@Override
		protected BmsErrorType testNoteData(long pulseNumber, JSONObject noteData) {
			if (!noteData.has("x") || !noteData.has("l") || !noteData.has("c")) {
				// 必須項目が存在しない場合は構文エラーとする
				return BmsErrorType.SYNTAX;
			}
			var l = noteData.optInt("l", -1);
			if (l < 0) {
				// ノート長の定義に誤りがある
				return BmsErrorType.WRONG_DATA;
			}
			var x = noteData.isNull("x") ? 0 : noteData.optInt("x", -1);
			if (x < 0) {
				// ノート番号が負の値の場合は不正データ
				return BmsErrorType.WRONG_DATA;
			}
			if (getChannel(noteData, (l == 0)) == null) {
				// ノートをチャンネルへマッピングできない場合は不明チャンネル
				return BmsErrorType.UNKNOWN_CHANNEL;
			}
			if (!(noteData.opt("c") instanceof Boolean)) {
				// 音声継続フラグの定義に誤りがある
				return BmsErrorType.WRONG_DATA;
			}
			if (noteData.has("t")) {
				var lnType = noteData.optLong("t", -1L);
				if ((lnType != 0L) && !LNMODES.contains(lnType)) {
					// LN種別指定時、定義誤り・定義値不正
					return BmsErrorType.WRONG_DATA;
				}
			}
			if (noteData.has("up") && !(noteData.opt("up") instanceof Boolean)) {
				// 長押し終了の定義フラグの定義内容に誤りがある
				return BmsErrorType.WRONG_DATA;
			}

			return null;
		}

		@Override
		protected void putNoteData(long pulseNumber, JSONObject noteData) {
			var lnLength = noteData.optLong("l");
			var channel = getChannel(noteData, (lnLength == 0L));
			var value = getNoteValue(noteData);
			var isUp = noteData.optBoolean("up", false);
			if (isUp) {
				// ロングノート終了のトラックID更新指示あり
				var minfo = getMeasureInfo(pulseNumber);
				var measure = minfo.measure;
				var tick = minfo.computeTick(pulseNumber);
				var channelLn = getChannel(noteData, false);
				var valueLnOff = getNoteData(measure, tick, channelLn);
				if (valueLnOff == null) {
					// ロングノート終了が未存在の場合はこの位置のノートデータを追加する
					putNoteData(pulseNumber, channel, value);
				} else {
					// 長押し終了時の定義で上書きする場合、トラックIDのみ更新する
					var lnMode = BeMusicSound.getLongNoteMode(valueLnOff, null);
					var restart = BeMusicSound.isRestartTrack(valueLnOff);
					var newValue = BeMusicSound.makeValue(getTrackId(), restart, lnMode);
					putNoteData(measure, tick, minfo.length, channelLn, newValue);
				}
			} else {
				// この位置のノートデータを追加する
				putNoteData(pulseNumber, channel, value);
			}

			// ロングノート終点を追加する
			if (lnLength > 0L) {
				var pnLnOff = pulseNumber + lnLength;
				var minfo = getMeasureInfo(pnLnOff);
				var measure = minfo.measure;
				var tick = minfo.computeTick(pnLnOff);
				var length = minfo.length;
				var valueLnOff = getNoteData(measure, tick, channel);
				if (valueLnOff == null) {
					// 同じ位置にまだノートがない場合は全情報を追記する
					putNoteData(measure, tick, length, channel, value);
				} else {
					// 長押し終了本体の定義で上書きする場合、トラックID以外を更新する
					var lnType = noteData.optLong("t", 0L);
					var lnMode = LNMODES.contains(lnType) ? BeMusicLongNoteMode.fromNative(lnType) : null;
					var restart = noteData.optBoolean("c", false) ? false : true;
					var trackId = BeMusicSound.getTrackId(valueLnOff);
					var newValue = BeMusicSound.makeValue(trackId, restart, lnMode);
					putNoteData(measure, tick, length, channel, newValue);
				}
			}
		}

		@Override
		protected int getNoteValue(JSONObject noteData) {
			var lnType = noteData.optLong("t", 0L);
			return BeMusicSound.makeValue(
					getTrackId(),
					noteData.optBoolean("c", false) ? false : true,
					LNMODES.contains(lnType) ? BeMusicLongNoteMode.fromNative(lnType) : null);
		}
	}

	/** "mine_channels"解析処理 */
	private class MineExchanger extends TrackExchanger {
		/** コンストラクタ */
		protected MineExchanger() {
			super(ParserPhase.MINE_CHANNELS, channels(null, BeMusicDevice::getMineChannel), null);
		}

		@Override
		protected BmsErrorType testNoteData(long pulseNumber, JSONObject noteData) {
			if (!noteData.has("x") || !noteData.has("damage")) {
				// 必須項目が存在しない場合は構文エラーとする
				return BmsErrorType.SYNTAX;
			}
			var x = noteData.optInt("x", -1);
			if (x < 0) {
				// ノート番号が負の値の場合は不正データ
				return BmsErrorType.WRONG_DATA;
			}
			if (getChannel(noteData, true) == null) {
				// ノートをチャンネルへマッピングできない場合は不明チャンネル
				return BmsErrorType.UNKNOWN_CHANNEL;
			}
			var damage = noteData.optDouble("damage", -1.0);
			if (damage <= 0.0) {
				// ダメージ量が0以下の場合は不正データ
				return BmsErrorType.WRONG_DATA;
			}

			return null;
		}

		@Override
		protected int getNoteValue(JSONObject noteData) {
			// ダメージ量の範囲は1～0x7fffffffまで。
			// bmson(oraja)では小数部指定可能なようだが、BMSライブラリでは整数指定とする。
			return (int)Math.max(1.0, Math.min((double)0x7fffffff, noteData.optDouble("damage")));
		}
	}

	/** "key_channels"解析処理 */
	private class KeyExchanger extends TrackExchanger {
		/** コンストラクタ */
		protected KeyExchanger() {
			super(ParserPhase.KEY_CHANNELS, channels(null, BeMusicDevice::getInvisibleChannel), null);
		}

		@Override
		protected BmsErrorType testNoteData(long pulseNumber, JSONObject noteData) {
			if (!noteData.has("x")) {
				// 必須項目が存在しない場合は構文エラーとする
				return BmsErrorType.SYNTAX;
			}
			var x = noteData.optInt("x", -1);
			if (x < 0) {
				// ノート番号が負の値の場合は不正データ
				return BmsErrorType.WRONG_DATA;
			}
			if (getChannel(noteData, true) == null) {
				// ノートをチャンネルへマッピングできない場合は不明チャンネル
				return BmsErrorType.UNKNOWN_CHANNEL;
			}

			return null;
		}

		@Override
		protected int getNoteValue(JSONObject noteData) {
			// 不可視ノートは長押し、音切り情報等は含まず、トラックID(インデックス値)のみとする
			return getTrackId();
		}
	}

	/** "bga_events", "layer_events", "poor_events"解析処理 */
	private class BgaExchanger extends ChannelDataExchanger<JSONObject> {
		/** 対象チャンネル */
		private BmsChannel mChannel;

		/**
		 * コンストラクタ
		 * @param parent 解析対象配列データを持つ親オブジェクトの参照
		 * @param targetPhase 対象の処理フェーズ
		 * @param channel 対象チャンネル
		 */
		BgaExchanger(JSONObject parent, ParserPhase targetPhase, BmsChannel channel) {
			super(parent, targetPhase.getRootName(), targetPhase, null);
			mChannel = channel;
		}

		@Override
		protected boolean isDefineMandatory() {
			// "bga"内の定義は全て定義必須とする
			return true;
		}

		@Override
		protected JSONObject castNoteData(JSONObject o) {
			// 変換せずそのまま返す
			return o;
		}

		@Override
		protected BmsErrorType testNoteData(long pulseNumber, JSONObject noteData) {
			if (!noteData.has("id")) {
				// ID未存在時は構文エラーとする
				return BmsErrorType.SYNTAX;
			}
			var id = noteData.optInt("id", -1);
			if ((id <= 0) || (id > BmsSpec.INDEXED_META_INDEX_MAX)) {
				// IDの値が不正
				// ※ID==0も不正値として判定する
				return BmsErrorType.WRONG_DATA;
			}

			return null;
		}

		@Override
		protected BmsChannel getChannel(JSONObject noteData, boolean primary) {
			// 指定チャンネルをそのまま返す
			return mChannel;
		}

		@Override
		protected int getNoteValue(JSONObject noteData) {
			// IDの値をそのままノートの値にする
			return noteData.optInt("id");
		}
	}

	/**
	 * bmson形式のJSONからのBMSローダオブジェクトを構築します。
	 */
	public BeMusicBmsonLoader() {
		super(false, false);
		mParsers = Map.ofEntries(
				Map.entry(ParserPhase.INFO, this::parseInfo),
				Map.entry(ParserPhase.LINES, this::parseLines),
				Map.entry(ParserPhase.BPM_EVENTS, this::parseBpmEvents),
				Map.entry(ParserPhase.SCROLL_EVENTS, this::parseScrollEvents),
				Map.entry(ParserPhase.STOP_EVENTS, this::parseStopEvents),
				Map.entry(ParserPhase.SOUND_CHANNELS, this::parseSoundChannels),
				Map.entry(ParserPhase.MINE_CHANNELS, this::parseMineChannels),
				Map.entry(ParserPhase.KEY_CHANNELS, this::parseKeyChannels),
				Map.entry(ParserPhase.BGA, this::parseBga),
				Map.entry(ParserPhase.BGA_HEADER, this::parseBgaHeader),
				Map.entry(ParserPhase.BGA_EVENTS, this::parseBgaEvents),
				Map.entry(ParserPhase.LAYER_EVENTS, this::parseLayerEvents),
				Map.entry(ParserPhase.POOR_EVENTS, this::parsePoorEvents));
		endParse();
	}

	/** {@inheritDoc} */
	@Override
	protected BmsErrorParsed beginParse(BmsLoaderSettings settings, BmsSource source) throws BmsCompatException {
		try {
			// 入力されたbmsonを解析する
			mRoot = new JSONObject(source.getAsScript());
		} catch (JSONException e) {
			// bmsonにエラーがある場合はこれ以上続行しない
			var msg = String.format("JSON parse error. '%s'", e.getMessage());
			throw incompatible(msg, e);
		}

		mBga = mRoot.optJSONObject("bga");
		mPhase = ParserPhase.INFO;
		mElements = new ArrayDeque<>();
		mPulsePerMeasure = 0L;
		mMeasures = new TreeMap<>();
		mWavs = new LinkedHashMap<>();
		source = null;

		// bmsonのバージョンをチェックする
		if (!mRoot.has("version")) {
			// バージョンが存在しない場合はエラー
			throw incompatible("No 'version' defined.");
		}
		try {
			// バージョンが対応する値かを確認する
			var version = mRoot.getString("version");
			if (!COMPATIBLE_VERSIONS.contains(version)) {
				// 非対応バージョンの場合は解析を中止する
				throw incompatible(String.format("'%s' this version is incompatible.", version));
			}
		} catch (JSONException e) {
			// バージョンの定義が不正
			throw incompatible("Wrong 'version' value.");
		}

		return BmsErrorParsed.PASS;
	}

	/** {@inheritDoc} */
	@Override
	protected BmsErrorParsed endParse() {
		mRoot = null;
		mBga = null;
		mPhase = null;
		mElements = null;
		mPulsePerMeasure = 0L;
		mMeasures = null;
		mWavs = null;
		return BmsErrorParsed.PASS;
	}

	/**
	 * BMSの解析によって得られたBMSコンテンツの要素を1件返します。
	 * <p><strong>※当メソッドはBMSライブラリの一般利用者が参照する必要はありません。</strong></p>
	 * <p>当メソッドでは、bmson仕様に規定されている要素をJSONオブジェクトから読み取り、
	 * 標準フォーマットのメタ情報、チャンネルに変換して返します。具体的なbmsonの読み取り手順については、
	 * {@link BeMusicBmsonLoader}のドキュメントを参照してください。</p>
	 * @return BMSコンテンツの要素、またはエラー要素。これ以上要素がない場合はnull。
	 * @exception BmsCompatException BMSライブラリで表現できない定義を検出した時
	 */
	@Override
	protected BmsParsed nextElement() throws BmsCompatException {
		var element = (BmsParsed)null;
		while (true) {
			// 解析によって蓄積された要素を1件取り出す
			element = mElements.pollLast();
			if (element != null) {
				// 蓄積された要素が存在する場合、取り出した要素を返す
				break;
			}

			// 現在のフェーズの解析処理内容を解決する
			var method = mParsers.get(mPhase);
			if (method == null) {
				// 解析処理は全て完了している
				break;
			}

			// 現在の解析処理を実行する
			method.parse();

			// 次の解析フェーズへ遷移する
			var nextPhase = PHASE_MAP.get(mPhase.getNumber() + 1);
			mPhase = (nextPhase != null) ? nextPhase : ParserPhase.DONE;
		}

		return element;
	}

	/**
	 * "info"解析処理
	 * @throws BmsCompatException 互換性エラー発生時
	 */
	private void parseInfo() throws BmsCompatException {
		// Information Objectを取得する
		var info = mRoot.optJSONObject("info");
		if (info == null) {
			if (mRoot.has("info")) {
				// "info"がオブジェクトではない場合はエラーとする
				throw incompatible("'info' is wrong definition.");
			} else {
				// "info"が存在しない場合は不正形式と見なしエラーとする
				throw incompatible("'info' is not defined.");
			}
		}

		var elements = (List<BmsParsed>)null;

		// モード
		elements = getMeta(info, JsonType.STR, "mode_hint", "beat-7k", BeMusicMeta.PLAYER);
		if (elements.get(0).isErrorType()) {
			// モードの解決に失敗した場合は不正形式と見なしエラーとする
			throw incompatible("Could not resolve 'mode_hint'.");
		}
		var modeHint = (BmsMetaParsed)elements.get(0);
		var modeName = modeHint.value;
		var player = PLAYER_MAP.get(modeName);
		if (player == null) {
			// 非対応のモードが設定されていた場合は非互換bmsonのためエラーとする
			throw incompatible(String.format("Incompatible mode '%s'.", modeName));
		} else {
			// モードの値を#PLAYERの定義値に変換後に出力する
			modeHint.value = String.valueOf(player.getNativeValue());
			publishElement(modeHint);
		}

		// 分解能
		elements = getMeta(info, JsonType.INT, "resolution", BmsInt.box(240), null);
		if (elements.get(0).isErrorType()) {
			// 分解能が解決できないと以後の読み込みが続行できないためエラーとする
			throw incompatible("Could not read 'resolution'.");
		}
		var resolution = Math.abs(Long.parseLong(((BmsMetaParsed)elements.get(0)).value));
		if (resolution == 0) {
			// 分解能に0を設定することはできないためエラーとする
			throw incompatible("Cannot set 0 to 'resolution'.");
		} else {
			// 1小節あたりのパルス数を計算しておく
			mPulsePerMeasure = resolution * 4L;
		}

		// #GENRE
		elements = getMeta(info, JsonType.STR, "genre", null, BeMusicMeta.GENRE);
		publishMetaIfNotEmpty(elements.get(0));

		// #TITLE
		elements = getMeta(info, JsonType.STR, "title", null, BeMusicMeta.TITLE);
		publishMetaOrDefaultIfError(elements.get(0), "title", BeMusicMeta.TITLE, "", s -> true);

		// #SUBTITLE
		elements = getMeta(info, JsonType.STR, "subtitle", null, BeMusicMeta.SUBTITLE);
		publishMetaIfNotEmpty(elements.get(0));

		// #ARTIST
		elements = getMeta(info, JsonType.STR, "artist", null, BeMusicMeta.ARTIST);
		publishMetaOrDefaultIfError(elements.get(0), "artist", BeMusicMeta.ARTIST, "", s -> true);

		// #SUBARTIST
		if (info.has("subartists")) {
			// サブアーティストは定義されている場合のみ取り込もうとする
			elements = getMeta(info, JsonType.ARY, "subartists", null, BeMusicMeta.SUBARTIST);
			publishElements(elements);
		}

		// #CHARTNAME
		elements = getMeta(info, JsonType.STR, "chart_name", null, BeMusicMeta.CHARTNAME);
		var maybeChartName = elements.get(0);
		if (maybeChartName.isErrorType()) {
			// エラー時はエラー内容を設定する
			publishElement(maybeChartName);
		} else {
			// bmson側の定義内容により、定義するメタ情報を決定する
			var chartName = ((BmsMetaParsed)maybeChartName).value.strip();
			var difficulty = (BeMusicDifficulty)null;
			if (chartName.isEmpty()) {
				// 空文字列なら#CHARTNAME, #DIFFICULTYの両方を未定義とする
				// Do nothing
			} else if ((difficulty = DIFFICULTY_MAP.get(chartName.toLowerCase())) != null) {
				// 定義済みの#DIFFICULTYと同じ文字列なら#DIFFICULTYのみ定義する
				var value = String.valueOf(difficulty.getNativeValue());
				publishElement(new BmsMetaParsed(0, "", BeMusicMeta.DIFFICULTY, 0, value));
			} else {
				// #CHARTNAMEのみを定義する
				publishElement(maybeChartName);
			}
		}

		// #PLAYLEVEL
		elements = getMeta(info, JsonType.INT, "level", null, BeMusicMeta.PLAYLEVEL);
		publishMetaOrDefaultIfError(elements.get(0), "level", BeMusicMeta.PLAYLEVEL, "0", s -> true);

		// #BPM
		elements = getMeta(info, JsonType.NUM, "init_bpm", null, BeMusicMeta.INITIAL_BPM);
		publishMetaOrDefaultIfError(elements.get(0), "init_bpm", BeMusicMeta.INITIAL_BPM, "130", s -> true);

		// #DEFEXRANK
		elements = getMeta(info, JsonType.NUM, "judge_rank", null, BeMusicMeta.DEFEXRANK);
		publishMetaOrDefaultIfError(elements.get(0), "judge_rank", BeMusicMeta.DEFEXRANK, "100",
				s -> Double.parseDouble(s) >= 0.0);

		// #TOTAL
		elements = getMeta(info, JsonType.NUM, "total", null, BeMusicMeta.TOTAL);
		publishMetaOrDefaultIfError(elements.get(0), "total", BeMusicMeta.TOTAL, "100",
				s -> Double.parseDouble(s) >= 0.0);

		// #STAGEFILE
		elements = getMeta(info, JsonType.STR, "title_image", "", BeMusicMeta.STAGEFILE);
		publishMetaIfNotEmpty(elements.get(0));

		// #BACKBMP
		elements = getMeta(info, JsonType.STR, "back_image", "", BeMusicMeta.BACKBMP);
		publishMetaIfNotEmpty(elements.get(0));

		// #EYECATCH
		elements = getMeta(info, JsonType.STR, "eyecatch_image", "", BeMusicMeta.EYECATCH);
		publishMetaIfNotEmpty(elements.get(0));

		// #BANNER
		elements = getMeta(info, JsonType.STR, "banner_image", "", BeMusicMeta.BANNER);
		publishMetaIfNotEmpty(elements.get(0));

		// #PREVIEW
		elements = getMeta(info, JsonType.STR, "preview_music", "", BeMusicMeta.PREVIEW);
		publishMetaIfNotEmpty(elements.get(0));

		// #LNMODE
		if (info.has("ln_type")) {
			// ロングノート種別指定時のみ取り込もうとする
			elements = getMeta(info, JsonType.INT, "ln_type", null, BeMusicMeta.LNMODE);
			var lnType = elements.get(0);
			if (!lnType.isErrorType() && ((BmsMetaParsed)lnType).value.equals("0")) {
				// ロングノート種別を0で指定していた場合は"ln_type"未指定時と同様の振る舞いとする
				// Do nothing
			} else {
				publishMetaOrDefaultIfError(elements.get(0), "ln_type", null, null,
						s -> LNMODES.contains(Long.parseLong(s)));
			}
		}
	}

	/**
	 * "lines"解析処理
	 * @throws BmsCompatException 互換性エラー発生時
	 */
	private void parseLines() throws BmsCompatException {
		// 小節線情報を取得する
		var rootName = mPhase.getRootName();
		var lines = mRoot.optJSONArray(rootName);
		if (lines == null) {
			// 小節線情報がない場合、全ての小節を4/4拍子とする
			var maxPulseNumber = getMaxPulseNumber();
			var measureCount = (maxPulseNumber / mPulsePerMeasure) + 1;
			for (var i = 0; i < measureCount; i++) {
				var pulseNumber = i * mPulsePerMeasure;
				var minfo = new MeasureInfo(i, pulseNumber, mPulsePerMeasure, mPulsePerMeasure);
				mMeasures.put(pulseNumber, minfo);
			}

			// 小節線情報の定義が不正な場合はエラー出力する
			if (mRoot.has(rootName)) {
				// 要素が存在する場合、定義なしではなく定義型不正のためエラー出力
				var line = mRoot.get(rootName);
				var msg = "Wrong 'lines' definition.";
				var err = error(BmsParsedType.MEASURE_VALUE, BmsErrorType.SYNTAX, mPhase, line, msg, null);
				publishElement(err);
			}

			return;
		} else if (lines.length() == 0) {
			// 小節線情報が1件もない場合、1小節のみとする
			// 小節長が4/4拍子ではないので先頭小節の小節長を出力する
			var maxPulseNumber = getMaxPulseNumber();
			var pulseCount = ((maxPulseNumber / mPulsePerMeasure) * mPulsePerMeasure) + mPulsePerMeasure;
			var minfo = new MeasureInfo(0, 0L, pulseCount, mPulsePerMeasure);
			var chNum = BeMusicChannel.LENGTH.getNumber();
			var value = String.valueOf(minfo.length);
			var element = new BmsMeasureValueParsed(mPhase.getNumber(), "[]", minfo.measure, chNum, value);
			mMeasures.put(0L, minfo);
			publishElement(element);
		} else {
			// 小節線情報を1件ずつ解析する
			new BarLineExchanger(mRoot).exchange();
		}
	}

	/**
	 * "bpm_events"解析処理
	 * @throws BmsCompatException 互換性エラー発生時
	 */
	private void parseBpmEvents() throws BmsCompatException {
		new BpmEventsExchanger(mRoot).exchange();
	}

	/**
	 * "scroll_events"解析処理
	 * @throws BmsCompatException 互換性エラー発生時
	 */
	private void parseScrollEvents() throws BmsCompatException {
		new ScrollEventsExchanger(mRoot).exchange();
	}

	/**
	 * "stop_events"解析処理
	 * @throws BmsCompatException 互換性エラー発生時
	 */
	private void parseStopEvents() throws BmsCompatException {
		new StopEventsExchanger(mRoot).exchange();
	}

	/**
	 * "sound_channels"解析処理
	 * @throws BmsCompatException 互換性エラー発生時
	 */
	private void parseSoundChannels() throws BmsCompatException {
		collectMaterialMetas(mRoot, mPhase, false, o -> BmsInt.box(mWavs.size() + 1), this::putWavs);
		exchangeTracks(() -> new SoundExchanger(), false, false);
	}

	/**
	 * "mine_channels"解析処理
	 * @throws BmsCompatException 互換性エラー発生時
	 */
	private void parseMineChannels() throws BmsCompatException {
		collectMaterialMetas(mRoot, mPhase, false, o -> 0, this::putWavs);
		exchangeTracks(() -> new MineExchanger(), true, true);
	}

	/**
	 * "key_channels"解析処理
	 * @throws BmsCompatException 互換性エラー発生時
	 */
	private void parseKeyChannels() throws BmsCompatException {
		collectMaterialMetas(mRoot, mPhase, false, o -> BmsInt.box(mWavs.size() + 1), this::putWavs);
		exchangeTracks(() -> new KeyExchanger(), true, false);
		publishMaterialMetas(mPhase, BeMusicMeta.WAV, mWavs, e -> e.getValue(), e -> e.getKey());
	}

	/**
	 * "bga"解析処理
	 * @throws BmsCompatException 互換性エラー発生時
	 */
	private void parseBga() throws BmsCompatException {
		if (mBga == null) {
			var rootName = mPhase.getRootName();
			if (mRoot.has(rootName)) {
				// BGAの定義内容が不正
				var line = String.format("{ \"%s\": ??? }", rootName);
				var msg = "Wrong BGA definition.";
				publishElement(error(BmsParsedType.META, BmsErrorType.SYNTAX, mPhase, line, msg, null));
			} else {
				// BGAの定義未存在時はエラーにはしない
				// Do nothing
			}
		}
	}

	/**
	 * "bga_header"解析処理
	 * @throws BmsCompatException 互換性エラー発生時
	 */
	private void parseBgaHeader() throws BmsCompatException {
		var bmps = new LinkedHashMap<Integer, String>();
		collectMaterialMetas(mBga, mPhase, true, o -> o.optInt("id", -1), bmps::put);
		publishMaterialMetas(mPhase, BeMusicMeta.BMP, bmps, e -> e.getKey(), e -> e.getValue());
	}

	/**
	 * "bga_events"解析処理
	 * @throws BmsCompatException 互換性エラー発生時
	 */
	private void parseBgaEvents() throws BmsCompatException {
		new BgaExchanger(mBga, mPhase, BeMusicChannel.BGA).exchange();
	}

	/**
	 * "layer_events"解析処理
	 * @throws BmsCompatException 互換性エラー発生時
	 */
	private void parseLayerEvents() throws BmsCompatException {
		new BgaExchanger(mBga, mPhase, BeMusicChannel.BGA_LAYER).exchange();
	}

	/**
	 * "poor_events"解析処理
	 * @throws BmsCompatException 互換性エラー発生時
	 */
	private void parsePoorEvents() throws BmsCompatException {
		new BgaExchanger(mBga, mPhase, BeMusicChannel.BGA_MISS).exchange();
	}

	/**
	 * 1個の要素データを出力する
	 * @param element 要素データ
	 */
	private void publishElement(BmsParsed element) {
		mElements.push(element);
	}

	/**
	 * 複数の要素データを出力する
	 * @param elements 要素データ
	 */
	private void publishElements(Collection<BmsParsed> elements) {
		for (var element : elements) {
			publishElement(element);
		}
	}

	/**
	 * 指定メタ情報を出力する。エラー時は当該エラーと代替値を出力する。
	 * @param element 出力対象要素データ(メタ情報またはエラー要素であること)
	 * @param key bmson側の元情報キー名
	 * @param meta エラー・検査失敗時に出力するメタ情報
	 * @param def エラー・検査失敗時に出力する代替値の文字列表現
	 * @param tester 検査処理
	 */
	private void publishMetaOrDefaultIfError(BmsParsed element, String key, BmsMeta meta, String def, Predicate<String> tester) {
		var needDefault = false;
		if (element.isErrorType()) {
			// エラー時はエラーと指定のデフォルト値も出力する
			publishElement(element);
			needDefault = true;
		} else {
			var elemMeta = (BmsMetaParsed)element;
			if (tester.test(elemMeta.value)) {
				// 正常値の場合は解析結果をそのまま出力する
				publishElement(element);
			} else {
				// 検査失敗の場合はエラーとデフォルト値を出力する
				var msg = String.format("Wrong '%s' value.", key);
				publishElement(metaError(BmsErrorType.WRONG_DATA, key, elemMeta.value, msg, null));
				needDefault = true;
			}
		}

		// デフォルト値が必要であれば出力する
		if (needDefault && (meta != null)) {
			publishElement(new BmsMetaParsed(0, "", meta, 0, def));
		}
	}

	/**
	 * 指定メタ情報が空文字列でなければ出力する
	 * @param element 出力対象要素データ(メタ情報またはエラー要素であること)
	 */
	private void publishMetaIfNotEmpty(BmsParsed element) {
		if (element.isErrorType()) {
			// エラー時はエラー内容を設定する
			publishElement(element);
		} else if (!((BmsMetaParsed)element).value.isEmpty()) {
			// 値が空文字列でない場合のみ出力する
			publishElement(element);
		} else {
			// 値が空文字列の場合は出力しない
			// Do nothing
		}
	}

	/**
	 * 音声・画像等リソースの索引付きメタ情報を出力する
	 * <p>当メソッドでは、索引が最大値を超過するメタ情報は出力しない。</p>
	 * @param <K> リソースデータの型
	 * @param phase 対象の処理フェーズ
	 * @param meta 出力先メタ情報
	 * @param materials メタ情報マップ(キーがリソースデータ、値が索引)
	 */
	private <K, V> void publishMaterialMetas(ParserPhase phase, BmsMeta meta, Map<K, V> materials,
			ToIntFunction<Map.Entry<K, V>> toIndex, Function<Map.Entry<K, V>, String> toValue) {
		var phaseNumber = phase.getNumber();
		var metaName = meta.getName().toUpperCase();
		for (var entry : materials.entrySet()) {
			var index = toIndex.applyAsInt(entry);
			if ((index >= 0) && (index <= BmsSpec.INDEXED_META_INDEX_MAX)) {
				var value = toValue.apply(entry);
				var line = String.format("%s[%d] %s", metaName, index, value);
				var element = new BmsMetaParsed(phaseNumber, line, meta, index, value);
				publishElement(element);
			}
		}
	}

	/**
	 * bmsonのJSONオブジェクトからメタ情報取得
	 * @param o メタ情報を持つ親オブジェクト
	 * @param t JSON側の元データ型
	 * @param key JSON側のキー名
	 * @param def 指定キー未存在時のデフォルト値。必須項目の場合はnullを指定し、ない場合にエラーとなる。
	 * @param meta 取り込み先のメタ情報
	 * @return メタ情報要素データのリスト(複数メタ情報時に複数出力される場合があるため)
	 */
	private List<BmsParsed> getMeta(JSONObject o, JsonType t, String key, Object def, BmsMeta meta) {
		// 項目の有無による処理の分岐
		if (def == null) {
			// 必須項目の場合、指定キーの項目が未存在であればエラーとする
			if (!o.has(key)) {
				var msg = String.format("'%s' undefined.", key);
				return List.of(metaError(BmsErrorType.UNKNOWN_META, key, null, msg, null));
			}
		} else {
			// 任意項目の場合、指定キーの項目が未存在であればデフォルト値を採用する
			if (!o.has(key)) {
				return List.of(new BmsMetaParsed(mPhase.getNumber(), key, meta, 0, def.toString()));
			}
		}

		// データ型ごとのデータ変換処理を実行する
		var result = (List<BmsParsed>)null;
		try {
			switch (t) {
			case STR: {
				// 文字列型：項目を文字列表記でそのまま使用する
				var value = o.getString(key);
				result = List.of(new BmsMetaParsed(mPhase.getNumber(), key, meta, 0, value));
				break;
			}
			case INT: {
				// 整数型：long型に変換後、その値を文字列に変換して使用する
				var value = String.valueOf(o.getLong(key));
				result = List.of(new BmsMetaParsed(mPhase.getNumber(), key, meta, 0, value));
				break;
			}
			case NUM: {
				// 実数型：double型に変換後、その値を文字列に変換して使用する
				var value = String.valueOf(o.getDouble(key));
				result = List.of(new BmsMetaParsed(mPhase.getNumber(), key, meta, 0, value));
				break;
			}
			case ARY: {
				// 配列型：全ての項目を文字列として扱い、定義順に複数のメタ情報を出力する
				var foundWrong = false;
				var array = o.getJSONArray(key);
				var count = array.length();
				var list = new ArrayList<BmsParsed>(count);
				for (var i = 0; i < count; i++) {
					try {
						// 配列要素を文字列として読み込み、リストに追記する
						var value = array.getString(i);
						list.add(new BmsMetaParsed(mPhase.getNumber(), key, meta, 0, value));
					} catch (JSONException e) {
						// 文字列以外で定義されていた場合はデータ不正とする
						if (!foundWrong) {
							foundWrong = true;
							var keyName = String.format("%s[%d]", key, i);
							var valueStr = array.optString(i, "");
							var msg = "Found a wrong array element.";
							list.add(metaError(BmsErrorType.WRONG_DATA, keyName, valueStr, msg, e));
						}
					}
				}
				result = list;
				break;
			}
			default:
				// それ以外の型は何もしない
				result = Collections.emptyList();
				break;
			}
		} catch (Exception e) {
			// ここでの例外ハンドリングは、意図しない型でデータが格納されていたケースを想定
			var value = o.optString(key, "");
			var msg = "Wrong value.";
			result = List.of(metaError(BmsErrorType.WRONG_DATA, key, value, msg, e));
		}

		return result;
	}

	/**
	 * 最大の絶対パルス番号取得
	 * @return 最大の絶対パルス番号
	 */
	private long getMaxPulseNumber() {
		var max = 0L;

		// 時間に関連するノート
		max = Math.max(max, getMaxPulseNumberSub(mRoot, ParserPhase.BPM_EVENTS.getRootName()));
		max = Math.max(max, getMaxPulseNumberSub(mRoot, ParserPhase.SCROLL_EVENTS.getRootName()));
		max = Math.max(max, getMaxPulseNumberSub(mRoot, ParserPhase.STOP_EVENTS.getRootName()));

		// トラック
		var tracks = Stream.of(
				mRoot.optJSONArray(ParserPhase.SOUND_CHANNELS.getRootName()),
				mRoot.optJSONArray(ParserPhase.MINE_CHANNELS.getRootName()),
				mRoot.optJSONArray(ParserPhase.KEY_CHANNELS.getRootName()))
				.filter(a -> a != null).toArray(JSONArray[]::new);
		for (var track : tracks) {
			var count = track.length();
			for (var i = 0; i < count; i++) {
				max = Math.max(max, getMaxPulseNumberSub(track.optJSONObject(i), "notes"));
			}
		}

		// BGAノート
		var bga = mRoot.optJSONObject("bga");
		if (bga != null) {
			max = Math.max(max, getMaxPulseNumberSub(bga, ParserPhase.BGA_EVENTS.getRootName()));
			max = Math.max(max, getMaxPulseNumberSub(bga, ParserPhase.LAYER_EVENTS.getRootName()));
			max = Math.max(max, getMaxPulseNumberSub(bga, ParserPhase.POOR_EVENTS.getRootName()));
		}

		return max;
	}

	/**
	 * 最大の絶対パルス番号取得処理のサブ処理
	 * @param root 処理対象のルートオブジェクト
	 * @param key 配列データのキー名
	 * @return 最大の絶対パルス番号
	 */
	private long getMaxPulseNumberSub(JSONObject root, String key) {
		// 配列がnull(未存在、不正形式)の場合は何もしない
		var a = (root == null) ? null : root.optJSONArray(key);
		if (a == null) {
			return 0L;
		}

		// 指定配列内にあるノート情報の最大パルス番号を探す
		var max = 0L;
		var count = a.length();
		for (var i = 0; i < count; i++) {
			var o = a.optJSONObject(i);
			max = (o == null) ? max : Math.max(max, o.optLong("y", -1L));
		}

		return max;
	}

	/**
	 * 指定の絶対パルス番号が属する小節の小節情報取得
	 * @param absPulseNumber 絶対パルス番号
	 * @return 絶対パルス番号が属する小節の小節情報
	 */
	private MeasureInfo getMeasureInfo(long absPulseNumber) {
		var entry = mMeasures.floorEntry(absPulseNumber);
		return (entry == null) ? null : entry.getValue();
	}

	/**
	 * 音声・画像等リソースのメタ情報収集
	 * @param parent 収集対象配列データの格納先オブジェクト
	 * @param phase 処理フェーズ(フェーズに関連付いた配列データが収集対象の配列になる)
	 * @param outErr エラー発生時にエラー出力するかどうか
	 * @param getId 1個のノートオブジェクトからID(インデックス値)を抽出する関数(関数がnullを返すとマップの件数+1がID)になる
	 * @param putter 収集したメタ情報を登録する関数
	 */
	private void collectMaterialMetas(JSONObject parent, ParserPhase phase, boolean outErr,
			Function<JSONObject, Integer> getId, BiConsumer<Integer, String> putter) {
		// 収集対象の配列データを取得する
		if (parent == null) {
			// 親オブジェクトが存在しない場合は何もしない
			return;
		}
		var rootName = phase.getRootName();
		var array = parent.optJSONArray(rootName);
		if (array == null) {
			// 親オブジェクトに処理フェーズに該当する配列データが存在しない場合は何もしない
			if (outErr) {
				if (parent.has(rootName)) {
					// 配列データの定義が不正
					var line = String.format("{ \"%s\": ??? }", rootName);
					var msg = String.format("'%s' wrong definition.", rootName);
					publishElement(error(BmsParsedType.META, BmsErrorType.SYNTAX, phase, line, msg, null));
				} else {
					// 配列データが未定義
					var line = "";
					var msg = String.format("'%s' undefined.", rootName);
					publishElement(error(BmsParsedType.META, BmsErrorType.SYNTAX, phase, line, msg, null));
				}
			}
			return;
		}

		// 配列データから1個ずつノートオブジェクトを走査してメタ情報を収集する
		var errors = new TreeMap<Integer, BmsParsed>();
		var count = array.length();
		for (var i = 0; i < count; i++) {
			// ノートオブジェクトを取得する
			var o = array.optJSONObject(i);
			if (o == null) {
				// ノートオブジェクトがJSONオブジェクト形式で取得できない場合はスキップ
				if (outErr && !errors.containsKey(1)) {
					var line = String.format("%s[%d]", rootName, i);
					var msg = "Wrong definition.";
					errors.put(1, error(BmsParsedType.META, BmsErrorType.SYNTAX, phase, line, msg, null));
				}
				continue;
			}

			// ノートオブジェクトからID(インデックス値)を取得する
			var id = getId.apply(o);
			if (id == null) {
				// nullを返した場合は不正値として処理する
				if (outErr && !errors.containsKey(2)) {
					var line = String.format("%s[%d] { ... }", rootName, i);
					var msg = "Wrong value.";
					errors.put(2, error(BmsParsedType.META, BmsErrorType.WRONG_DATA, phase, line, msg, null));
				}
				continue;
			} else if (id < 0) {
				// 負の値はIDとして使用できないためスキップ
				if (outErr && !errors.containsKey(3)) {
					var line = String.format("%s[%d] { ... }", rootName, i);
					var msg = "Wrong definition.";
					errors.put(3, error(BmsParsedType.META, BmsErrorType.WRONG_DATA, phase, line, msg, null));
				}
				continue;
			}
			if (id > BmsSpec.INDEXED_META_INDEX_MAX) {
				// IDがメタ情報インデックス値上限を超える場合はデータ不正
				if (outErr && !errors.containsKey(4)) {
					var line = String.format("%s[%d] { ... }", rootName, i);
					var msg = "Too many (or large) identifier.";
					errors.put(4, error(BmsParsedType.META, BmsErrorType.WRONG_DATA, phase, line, msg, null));
				}
				//continue;  // IDがインデックス値上限を超過してもデータ収集は行う
			}

			// ノートオブジェクトから名称を取得する
			var name = "";
			try {
				// オブジェクト内の"name"要素から文字列を取得する
				// ※どのパターンでも"name"要素の文字列型で格納されているため
				name = o.getString("name");
			} catch (JSONException e) {
				// 形式不正の場合はエラーとする
				name = null;
			}
			if ((name == null) || name.isEmpty()) {
				// 名称が取得できない、空文字列の場合はスキップ
				if (outErr && !errors.containsKey(5)) {
					var line = String.format("%s[%d] { \"name\": \"%s\" }", rootName, i, name);
					var msg = "Wrong name.";
					errors.put(5, error(BmsParsedType.META, BmsErrorType.WRONG_DATA, phase, line, msg, null));
				}
				continue;
			}

			// 名称とIDを登録する
			// 既に同一名称が登録されている場合は新しいIDでは登録せず既存IDを優先する
			putter.accept(id, name);
		}

		// 発生したエラーを全て出力する
		publishElements(errors.values());
	}

	/**
	 * 複数のトラック定義をチャンネルデータ定義に変換する処理
	 * @param <E> Exchangerオブジェクトの型
	 * @param exgCreator Exchangerオブジェクト生成関数
	 * @param isOptional トラック定義自体が省略可能かどうか
	 * @param allowEmptyTrack トラック名が空文字であることを許可するか
	 * @throws BmsCompatException 互換性エラー発生時
	 */
	private <E extends TrackExchanger> void exchangeTracks(Supplier<E> exgCreator, boolean isOptional,
			boolean allowEmptyTrack)
			throws BmsCompatException {
		// 変換対象のトラック定義配列を取得する
		var rootName = mPhase.getRootName();
		var trackArray = mRoot.optJSONArray(rootName);
		if (trackArray == null) {
			if (mRoot.has(rootName)) {
				// 配列ではない場合は構文エラーとする
				var line = String.format("\"%s\": ???", rootName);
				var msg = "Wrong channel definition.";
				publishElement(achSyntax(mPhase, line, msg));
			} else if (!isOptional) {
				// 省略不可で未定義の場合は構文エラーとする
				var line = String.format("\"%s\": ???", rootName);
				var msg = String.format("'%s' is not defined.", rootName);
				publishElement(achSyntax(mPhase, line, msg));
			} else {
				// それ以外の場合は何も行わない
				// Do nothing
			}
			return;
		}

		// 全トラックを解析する
		var errors = new TreeMap<Integer, BmsParsed>();
		var exchanger = exgCreator.get();
		var trackCount = trackArray.length();
		for (var i = 0; i < trackCount; i++) {
			// トラックの定義を取得する
			var trackObj = trackArray.optJSONObject(i);
			if (trackObj == null) {
				// トラックの定義はJSONオブジェクトである必要があるが、オブジェクトではない場合はエラー出力する
				if (!errors.containsKey(1)) {
					var line = String.format("%s[%d]", rootName, i);
					var msg = "Wrong track definition.";
					errors.put(1, achWrong(mPhase, line, msg));
				}
				continue;
			}

			// トラックの音声ファイル名を取得する
			var trackName = "";
			if (!trackObj.has("name")) {
				// 音声ファイル名が未定義の場合は構文エラーとする
				if (!errors.containsKey(2)) {
					var line = String.format("%s[%d] { ... }", rootName, i);
					var msg = "Track file name is not defined.";
					errors.put(2, achSyntax(mPhase, line, msg));
				}
				if (!allowEmptyTrack) {
					// 空トラック不許可の場合は処理を中止する
					continue;
				}
			} else {
				try {
					// 型チェック付きでトラック名取得を試行する
					trackName = trackObj.getString("name");
					trackName = (trackName.isEmpty() && !allowEmptyTrack) ? null : trackName;
				} catch (JSONException e) {
					// 型不正時はnullをセットし下のエラーチェックに引っ掛かるようにする
					trackName = null;
				}
			}
			if (trackName == null) {
				// トラック名定義不正、空トラック名で空トラック不許可の場合はエラーを出力する
				if (!errors.containsKey(3)) {
					var line = String.format("%s[%d] { \"name\": ??? }", rootName, i);
					var msg = "Wrong track name.";
					errors.put(3, achWrong(mPhase, line, msg));
				}
				if (allowEmptyTrack) {
					// 空トラック許可の場合は空トラック名として処理を続行する
					trackName = "";
				} else {
					// 空トラック不許可の場合は処理を中止する
					continue;
				}
			}

			// トラックのノート定義を取得する(定義確認のため)
			var notes = trackObj.optJSONArray("notes");
			if (notes == null) {
				if (trackObj.has("notes")) {
					// ノート定義の配列データが型不正の場合は不正データとしてエラー出力する
					if (!errors.containsKey(4)) {
						var line = String.format("%s[%d] { \"name\": \"%s\", \"notes\": ??? }", rootName, i, trackName);
						var msg = "Wrong note definition.";
						errors.put(4, achWrong(mPhase, line, msg));
					}
				} else {
					// ノート定義がない場合は構文エラーとする
					if (!errors.containsKey(5)) {
						var line = String.format("%s[%d] { \"name\": \"%s\" }", rootName, i, trackName);
						var msg = "'notes' is not defined.";
						errors.put(5, achSyntax(mPhase, line, msg));
					}
				}
				continue;
			}

			// チャンネルデータを構築する
			// 地雷チャンネルではトラックのファイル名を指定しないことがあるので#WAV一覧にIDがなくnullになることがある。
			// 地雷チャンネル以外は必ずトラックIDが取得できることが前提なので、その時にnullが返るのは !!!BUG!!!
			var trackId = Objects.requireNonNullElse(mWavs.get(trackName), 0);
			if ((trackId >= 0) && (trackId <= BmsSpec.INDEXED_META_INDEX_MAX)) {
				// トラックIDが有効範囲内であるチャンネルデータのみ出力する
				exchanger.exchange(trackObj, trackId);
			} else {
				// トラックIDが有効範囲外の場合は不正データとしてエラー出力する
				if (!errors.containsKey(6)) {
					var line = String.format("%s[%d] { \"name\": \"%s\", \"notes\": [...] }", rootName, i, trackName);
					var msg = "Overflow track ID.";
					errors.put(6, achWrong(mPhase, line, msg));
				}
			}
		}

		// 全トラック解析中に発生したエラーを出力する
		publishElements(errors.values());

		// 構築したチャンネルデータを全件出力する
		exchanger.publishAll();
	}

	/**
	 * メタ情報エラー要素生成
	 * @param type エラー種別
	 * @param key JSONオブジェクト側のキー名
	 * @param value エラーになった値
	 * @param reason エラー理由
	 * @param cause エラーの元になった例外
	 * @return メタ情報エラー要素
	 */
	private BmsErrorParsed metaError(BmsErrorType type, String key, Object value, String reason, Throwable cause) {
		var line = String.format("\"%s\": { \"%s\": \"%s\" }", mPhase.getRootName(), key, value);
		var err = new BmsScriptError(type, mPhase.getNumber(), line, reason, null);
		return new BmsErrorParsed(BmsParsedType.META, err);
	}

	/**
	 * #WAVマップへのデータ登録
	 * @param index メタ情報インデックス値
	 * @param name 音声ファイル名
	 */
	private void putWavs(Integer index, String name) {
		// #WAVの要素は先発データを優先する
		if (!mWavs.containsKey(name)) {
			mWavs.put(name, index);
		}
	}

	/**
	 * 入力デバイスに関連付いたチャンネルリスト生成
	 * @param first リストの先頭要素のチャンネル(対応するものがなければnullを指定)
	 * @param getter 入力デバイスからチャンネルを取得する関数
	 * @return チャンネルリスト
	 */
	private static BmsChannel[] channels(BmsChannel first, Function<BeMusicDevice, BmsChannel> getter) {
		return new BmsChannel[] {
				first,
				getter.apply(BeMusicDevice.SWITCH11), getter.apply(BeMusicDevice.SWITCH12),
				getter.apply(BeMusicDevice.SWITCH13), getter.apply(BeMusicDevice.SWITCH14),
				getter.apply(BeMusicDevice.SWITCH15), getter.apply(BeMusicDevice.SWITCH16),
				getter.apply(BeMusicDevice.SWITCH17), getter.apply(BeMusicDevice.SCRATCH1),
				getter.apply(BeMusicDevice.SWITCH21), getter.apply(BeMusicDevice.SWITCH22),
				getter.apply(BeMusicDevice.SWITCH23), getter.apply(BeMusicDevice.SWITCH24),
				getter.apply(BeMusicDevice.SWITCH25), getter.apply(BeMusicDevice.SWITCH26),
				getter.apply(BeMusicDevice.SWITCH27), getter.apply(BeMusicDevice.SCRATCH2),
		};
	}

	/**
	 * 配列型チャンネルのエラー要素生成
	 * @param type エラー種別
	 * @param phase 処理フェーズ
	 * @param line 行オブジェクト
	 * @param reason エラー理由
	 * @return 配列型チャンネルのエラー要素
	 */
	private static BmsErrorParsed achError(BmsErrorType type, ParserPhase phase, Object line, String reason) {
		return error(BmsParsedType.NOTE, type, phase, line, reason, null);
	}

	/**
	 * 配列型チャンネルの構文エラー生成
	 * @param phase 処理フェーズ
	 * @param line 行オブジェクト
	 * @param reason エラー理由
	 * @return 配列型チャンネルの構文エラー
	 */
	private static BmsErrorParsed achSyntax(ParserPhase phase, Object line, String reason) {
		return error(BmsParsedType.NOTE, BmsErrorType.SYNTAX, phase, line, reason, null);
	}

	/**
	 * 配列型チャンネルのデータ不正エラー生成
	 * @param phase 処理フェーズ
	 * @param line 行オブジェクト
	 * @param reason エラー理由
	 * @return 配列型チャンネルのデータ不正エラー
	 */
	private static BmsErrorParsed achWrong(ParserPhase phase, Object line, String reason) {
		return error(BmsParsedType.NOTE, BmsErrorType.WRONG_DATA, phase, line, reason, null);
	}

	/**
	 * エラー要素生成
	 * @param where エラー要素の発生源
	 * @param type エラー種別
	 * @param phase 処理フェーズ
	 * @param line 行オブジェクト
	 * @param reason エラー理由
	 * @param cause エラーの原因となった例外
	 * @return エラー要素
	 */
	private static BmsErrorParsed error(BmsParsedType where, BmsErrorType type, ParserPhase phase, Object line,
			String reason, Throwable cause) {
		var err = new BmsScriptError(type, phase.getNumber(), line.toString(), reason, cause);
		return new BmsErrorParsed(where, err);
	}

	/**
	 * 互換性エラー例外生成
	 * @param msg メッセージ
	 * @return 互換性エラー例外
	 */
	private static BmsCompatException incompatible(String msg) {
		return new BmsCompatException(msg);
	}

	/**
	 * 互換性エラー例外生成
	 * @param msg メッセージ
	 * @param cause エラーの原因となった例外
	 * @return 互換性エラー例外
	 */
	private static BmsCompatException incompatible(String msg, Throwable cause) {
		return new BmsCompatException(msg, cause);
	}
}
