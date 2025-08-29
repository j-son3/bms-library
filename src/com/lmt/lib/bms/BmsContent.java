package com.lmt.lib.bms;

import static com.lmt.lib.bms.internal.Assertion.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntPredicate;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.lmt.lib.bms.internal.Utility;

/**
 * 1個のBMSデータを表すBMSライブラリのメインオブジェクトです。
 *
 * <p>BMSで記述された1個のデータを格納するコンテナであり、そのデータに対して参照・編集を行うためのあらゆる機能を提供します。</p>
 *
 * <p>当クラスが提供するAPIは、大きく分けて以下の利用シーンを想定しています。</p>
 *
 * <ul>
 * <li>BMSコンテンツを参照し、メタ情報や時間軸を解析してBMSコンテンツを再生するアプリケーション(BMSプレイヤー)</li>
 * <li>BMSコンテンツを作成・編集し、その工程を直感的・視覚的にプレゼンテーションするアプリケーション(BMSエディタ)</li>
 * </ul>
 *
 * <p>上記利用シーンにおいて操作しやすい機能を提供し、BMSコンテンツの参照・編集を統一的なアプローチで管理します。<br>
 * BMSプレイヤーのようにリアルタイム性を求められるシーンでは頻繁なデータ参照が必要になります。当クラスのデータ参照
 * においては、API呼び出し時の動的なメモリの確保・解放を極力行わず、高速なアプリケーションが開発することができる
 * ようになっています。</p>
 *
 * <p><strong>データ構成について</strong><br>
 * BMSコンテンツが保有するデータは、「BMS宣言」「メタ情報」「チャンネル(時間軸上のデータ)」から成り立っています。
 * BMS宣言を除くデータにおいては、そのデータ書式を{@link BmsSpec}によって決定付けられたBMS仕様に沿うように強制されます。
 * BMS仕様に違反するデータはBMSコンテンツ内には受け入れられないため、データの整合性は保証されます。</p>
 *
 * <p><strong>BMS宣言</strong><br>
 * BMS宣言は当該BMSコンテンツに付与される付加的な情報です。メタ情報とは異なりこの情報の内容には特に決まったルールは無く、
 * 特定のアプリケーションに対して独自の解釈を指示したりする自由な記述として活用することができます。アプリケーションには
 * BMS宣言を考慮する義務はないため、BMSコンテンツの制作者は不特定多数のアプリケーションに何らかの振る舞いを期待する目的で
 * BMS宣言を活用するべきではありません。<br>
 *
 * <p>BMS宣言の参照・編集には以下のAPIを利用します。</p>
 *
 * <ul>
 * <li>BMS宣言を追加する：{@link #putDeclaration(String, String)}</li>
 * <li>BMS宣言を消去する：{@link #removeDeclaration(String)}</li>
 * <li>BMS宣言の存在確認：{@link #containsDeclaration(String)}</li>
 * <li>BMS宣言を取得する：{@link #getDeclaration(String)}, {@link #getDeclarations()}</li>
 * </ul>
 *
 * <p><strong>メタ情報</strong><br>
 * メタ情報は当該BMSコンテンツの内容・属性・状態等を表す様々な情報です。メタ情報の定義内容はBMS仕様によって決定されており、
 * BMSコンテンツへの情報登録は、このBMS仕様に基づいて行われなければなりません。BMSの特性上、登録義務のあるメタ情報は
 * 存在せず、未登録の情報については全て規定の初期値が設定されているものと見なされます。</p>
 *
 * <p>メタ情報の参照・編集には以下のAPIを利用します。</p>
 *
 * <ul>
 * <li>メタ情報を取得する：{@link #getMeta(String, BmsUnit, int)}, {@link #getSingleMeta(String)},
 *                         {@link #getMultipleMeta(String, int)}, {@link #getIndexedMeta(String, int)}</li>
 * <li>メタ情報を設定する：{@link #setMeta(String, BmsUnit, int, Object)}, {@link #setSingleMeta(String, Object)},
 *                         {@link #setMultipleMeta(String, int, Object)}, {@link #setIndexedMeta(String, int, Object)}</li>
 * <li>メタ情報の件数取得：{@link #getMetaCount(String, BmsUnit)}, {@link #getSingleMetaCount(String)},
 *                         {@link #getMultipleMetaCount(String)}, {@link #getIndexedMetaCount(String)}</li>
 * <li>メタ情報の登録確認：{@link #containsMeta(String, BmsUnit, int)}, {@link #containsSingleMeta(String)},
 *                         {@link #containsMultipleMeta(String, int)}, {@link #containsIndexedMeta(String, int)}</li>
 * <li>複数のメタ情報取得：{@link #getMultipleMetas(String)}, {@link #getIndexedMetas(String)}</li>
 * <li>初期BPMの取得・設定：{@link #getInitialBpm()}, {@link #setInitialBpm(double)}</li>
 * </ul>
 *
 * <p><strong>チャンネル</strong><br>
 * チャンネルは当該BMSコンテンツの情報のうち、時間軸上に配置される情報を指します。チャンネルにはそれぞれ番号が割り当てられ、
 * データ型等の属性が付与されています。時間軸は「小節番号」と「小節の刻み位置」によって管理されます。BMSコンテンツでは
 * チャンネル上のデータを「小節データ(MeasureValue)」および「ノート(Note)」という種類のデータに分解します。小節データは
 * 1チャンネル1小節について1個だけ設定可能なデータで、チャンネルのデータ型が配列型ではないデータが対象になります。
 * 小節データを特定するのに必要な情報は「チャンネル番号」「小節番号」であり、小節の刻み位置は必要ありません。
 * 一方、ノートはチャンネルのデータ型が配列型のものが対象になり、こちらはデータの特定に「小節の刻み位置」を必要とします。<br>
 * 但し「重複可能チャンネル」の場合、データを解決するのに「チャンネルインデックス」が必要になってきます。これは、
 * 重複データを一次元配列として扱っているためです。</p>
 *
 * <p>小節データ・ノートを扱うには、それぞれ別のAPIが必要になります。必要APIの一覧は下記を参照してください。<br>
 * <strong>小節データを扱うAPI</strong></p>
 *
 * <ul>
 * <li>小節データを取得する：{@link #getMeasureValue(int, int, int)}</li>
 * <li>小節データの登録確認：{@link #containsMeasureValue(int, int, int)}</li>
 * <li>小節データを設定する：{@link #setMeasureValue(int, int, Object)}</li>
 * </ul>
 *
 * <p><strong>ノートを扱うAPI</strong></p>
 *
 * <ul>
 * <li>ノートを取得する(位置指定)：{@link #getNote(int, int, int, double)}</li>
 * <li>後退位置のノートを取得する：{@link #getPreviousNote(int, int, int, double, boolean)}</li>
 * <li>前進位置のノートを取得する：{@link #getNextNote(int, int, int, double, boolean)}</li>
 * <li>ノートから解決済みメタ情報を取得：{@link #getResolvedNoteValue(int, int, int, double)}</li>
 * <li>ノートを検索する：{@link #pointOf(int, double, Predicate)}</li>
 * <li>ノートを列挙する：{@link #enumNotes(int, int, int, double, int, double, Consumer)}</li>
 * <li>ノート一覧を取得：{@link #listNotes(int, int, int, double, int, double, Predicate)}</li>
 * <li>ノート数を数える：{@link #countNotes(int, int, int, double, int, double, Predicate)}</li>
 * <li>ノートを追加する：{@link #putNote(int, int, int, double, int, Supplier)}</li>
 * <li>ノートを消去する：{@link #removeNote(int, int, int, int, Predicate)}</li>
 * </ul>
 *
 * <p>時間軸そのものの編集、および時間軸に関する情報の参照を行うためのAPIについては下記を参照してください。
 * これには、時間軸の計算や変換処理等のユーティリティAPIも含みます。</p>
 *
 * <ul>
 * <li>小節数を取得する：{@link #getMeasureCount()}</li>
 * <li>小節の刻み数を取得する：{@link #getMeasureTickCount(int)}</li>
 * <li>チャンネルのデータ数取得：{@link #getChannelDataCount(int, int)}</li>
 * <li>小節を挿入する：{@link #insertMeasure(int, int)}</li>
 * <li>小節を消去する：{@link #removeMeasure(int, int)}</li>
 * <li>チャンネルのデータ入れ替え：{@link #swapChannel(int, int, int, int)}</li>
 * <li>楽曲位置を移動する：{@link #seekPoint(int, double, double, BmsPoint)}</li>
 * <li>楽曲位置を時間に変換：{@link #pointToTime(int, double)}</li>
 * <li>時間を楽曲位置に変換：{@link #timeToPoint(double, BmsPoint)}</li>
 * </ul>
 *
 * <p><strong>タイムライン要素編集ポリシーについて</strong><br>
 * BMSコンテンツの小節データ・ノートのようなタイムライン要素の編集は、そのデータ構造の特性上、データの内容によっては
 * 時間に関する内部データの再計算が必要になる場合があります。例えば、全時間軸のうち、真ん中付近の小節の小節長を
 * 変更したとします。そうすると、その小節以降のタイムライン要素への到達時間が変更されることになり、内部で管理している
 * 時間に関するデータの再計算が発生します。<br>
 * 再計算が完了するまでの間、時間に関するデータに不整合が発生している状態となりますので、BMSコンテンツではその間は
 * 時間に関係するAPIへのアクセスが禁止されます。このアクセスが禁止されている状態のことを「編集モード」と呼称します。
 * </p>
 *
 * <p><strong>動作モードについて</strong><br>
 * BMSコンテンツでは、上述した事情により動作モードを2つ持っています。</p>
 *
 * <ul>
 * <li>参照モード<br>
 * このモードではタイムライン要素の全ての読み取りAPIへのアクセスを許可します。時間に関する内部データの計算が
 * 完了している状態を指し、時間計算を行うAPIも不整合を起こすことなく動作できます。逆に、このモードでは全ての
 * 「編集を伴うAPI」が禁止されます。参照モードでこれらのAPIにアクセスすると例外をスローするため注意が必要です。<br><br></li>
 * <li>編集モード<br>
 * データの編集を行う場合に必要なモードです。編集モードの間は「時間に関するデータを必要とするデータ読み取りAPI」への
 * アクセスを全て禁止状態にします。そうすることでBMSコンテンツから、データ不整合のある情報を提供することを防止し、
 * アプリケーションへの情報提供の質を高めます。このモードでは、参照モードではできなかった全ての「データ編集を伴うAPI」
 * へのアクセスを可能とします。</li>
 * </ul>
 *
 * <p>上述した動作モードへの遷移を行う専用のAPIが用意されています。{@link #beginEdit}を呼び出すと「編集モード」へ遷移します。
 * このAPIを呼び出した直後から全ての「データ編集を伴うAPI」へのアクセスが可能となります。編集モードを完了し「参照モード」へ
 * 戻すには{@link #endEdit}を呼び出します。このAPIを呼び出すと、編集モードの間に行われた変更内容から時間に関する内部データの
 * 再計算が開始され、それが完了すると参照モードに遷移します。<br>
 * BMSコンテンツのデフォルトの動作モードは参照モードです。すぐにデータの編集を行いたい場合は{@link #beginEdit}を
 * 呼び出してください。</p>
 *
 * <p><strong>マルチスレッド対策について</strong><br>
 * BMSコンテンツは、高速なデータアクセスを必要とするアプリケーションに配慮するため、BMSライブラリの設計ポリシーとして
 * マルチスレッドへの対策を行っていません。そのため、BMSコンテンツを含め単一のオブジェクトへ複数のスレッドから
 * アクセスした場合には、ドキュメントに記載の動作内容およびその後の動作の整合性を保証対象外としています。
 * 複数のスレッドから単一のオブジェクトを操作する予定がある場合には、個々のアプリケーションの責任でマルチスレッド対策を
 * 行うようにしてください。</p>
 *
 * <p><strong>データサイズについて</strong><br>
 * BMSコンテンツは複数の要素から成る多くのデータ同士が複雑にリンクして構成されています。
 * 楽曲の長さ・構成内容によっては数万～数十万オブジェクトが生成され、BMSコンテンツのサイズが数MBから数十MBになる可能性があります。
 * また、BMSコンテンツを扱うアプリケーションの特性上、同時に複数の画像や音声データがメモリ上に展開されることが予想されます。
 * そのため、複数のBMSコンテンツをメモリ上に展開することはアプリケーションの省メモリ設計の観点から推奨されません。
 * BMSコンテンツから必要な情報を取り出した後は速やかにオブジェクトを破棄してください。</p>
 *
 * @since 0.0.1
 */
public class BmsContent {
	/** BMS宣言のキー名書式の正規表現パターン */
	private static final Pattern PATTERN_DECL_NAME = Pattern.compile(
			"^[a-zA-Z_][a-zA-Z0-9_]*$");
	/** BMS宣言の値書式の正規表現パターン */
	private static final Pattern PATTERN_DECL_VALUE = Pattern.compile(
			"^([^\\\\\\\"]|(\\\\[\\\\\"]))*$");

	/**
	 * 編集モードから参照モードへ戻る際、BPM変更と譜面停止の統計情報を収集するためのテスター。
	 */
	private class BpmStopStatistics implements Consumer<BmsNote> {
		/** 最小BPM */
		double minBpm;
		/** 最大BPM */
		double maxBpm;
		/** BPM変更回数 */
		long changeBpmCount;
		/** 譜面停止回数 */
		long stopCount;

		/**
		 * コンストラクタ
		 * @param currentInitialBpm 現在の初期BPM
		 */
		BpmStopStatistics(double currentInitialBpm) {
			// 収集対象データを初期化する
			minBpm = currentInitialBpm;
			maxBpm = currentInitialBpm;
			changeBpmCount = 0;
			stopCount = 0;
		}

		/** {@inheritDoc} */
		@Override
		public void accept(BmsNote note) {
			var chNum = note.getChannel();
			if (mSpec.getBpmChannel(chNum) != null) {
				// BPM変更チャンネル
				var newBpm = mMetas.getIndexedMetaBpm(note, -1.0);
				if (newBpm >= BmsSpec.BPM_MIN) {
					changeBpmCount++;
					minBpm = Math.min(minBpm, newBpm);
					maxBpm = Math.max(maxBpm, newBpm);
				}
			} else if (mSpec.getStopChannel(chNum) != null) {
				// 譜面停止チャンネル
				var stopTick = mMetas.getIndexedMetaStop(note, 0);
				if (stopTick > 0) {
					stopCount++;
				}
			} else {
				// その他のチャンネルは無視する
			}
		}
	}

	/** このBMSコンテンツで扱うBMS仕様 */
	private BmsSpec mSpec;
	/** BMS宣言一覧 */
	private Map<String, BmsDeclarationElement> mBmsDeclarations = new LinkedHashMap<>();
	/** メタ情報コンテナ */
	private MetaContainer mMetas = null;
	/** タイムラインアクセッサ */
	private TimelineAccessor mTlAccessor = null;

	/** 現在のモードが編集モードかどうか */
	private boolean mIsEditMode = false;
	/** モード変更をロックしているかどうか */
	private boolean mIsLockChangeMode = false;
	/** 最小BPM */
	private double mMinBpm = 0.0;
	/** 最大BPM */
	private double mMaxBpm = 0.0;
	/** BPM変化回数 */
	private long mChangeBpmCount = 0L;
	/** 譜面停止回数 */
	private long mStopCount = 0L;

	/**
	 * 新しいBMSコンテンツオブジェクトを構築します。
	 * <p>BMSコンテンツ構築直後のデータは空になっています。データを構築するには本クラスの各種メソッドを使用しますが、
	 * BMSファイルを読み込んでBMSコンテンツを生成する場合は{@link BmsLoader}を使用することを推奨します。</p>
	 * @param spec コンテンツに関連付けるBMS仕様
	 * @throws NullPointerException specがnull
	 */
	public BmsContent(BmsSpec spec) {
		initialSetup(spec);
	}

	/**
	 * 指定されたBMSコンテンツと同じ内容の新しいBMSコンテンツオブジェクトを構築します。
	 * <p>入力BMSコンテンツから全てのBMS宣言、メタ情報、小節データ、及びノートをコピーし、
	 * 指定されたBMSコンテンツと同じ内容になるように初期化します。</p>
	 * <p>入力BMSコンテンツは参照モードでなければなりません。編集モードのBMSコンテンツを指定すると例外をスローします。</p>
	 * @param src コピー元BMSコンテンツ
	 * @throws NullPointerException srcがnull
	 * @throws IllegalArgumentException srcが編集モード
	 * @since 0.8.0
	 */
	public BmsContent(BmsContent src) {
		this(src, null, null, null, null);
	}

	/**
	 * 指定されたBMSコンテンツをコピー元とした新しいBMSコンテンツオブジェクトを構築します。
	 * <p>このコンストラクタではコピー元BMSコンテンツの各値を変換しながら新しいBMSコンテンツオブジェクトを構築する機能を提供します。
	 * 変換処理は引数にてデータコンバータ関数として渡します。変換の必要がないデータのコンバータにはnullを指定してください。</p>
	 * <p>BMS宣言、メタ情報、小節データのコンバータでは入力として各要素データを受け取り、変換後の値を返します。
	 * メタ情報、小節データでは各要素のデータ型に適合しないデータを返すと例外がスローされるので注意してください。
	 * コピーしたくない要素はnullを返すことで当該要素のコピーをスキップできます。</p>
	 * <p>メタ情報に限り、値の設定されていない単体メタ情報もコンバータ関数に入力されます。
	 * この動作仕様は、値の設定されていない単体メタ情報への値の設定を行いたいケースへ対応するためです。
	 * 値の設定有無は{@link BmsMetaElement#isContain()}で判定してください。</p>
	 * <p>ノートのコンバータではノートのアドレス、値の全てを変換対象とすることができます。
	 * 変換を行う場合は{@link BmsNote#newNote(int, int, int, double, int)}で新しいノートのインスタンスを生成してください。
	 * チャンネル番号には配列型を指定してください。それ以外の型のチャンネルを指定すると例外がスローされます。
	 * また、重複可能チャンネルから重複不可チャンネルへの変換を行うと、チャンネルインデックスが1以上のノートを転送する際にエラーとなります。</p>
	 * <p>入力BMSコンテンツは参照モードでなければなりません。編集モードのBMSコンテンツを指定すると例外をスローします。</p>
	 * @param src コピー元BMSコンテンツ
	 * @param cnvDecl BMS宣言変換関数またはnull
	 * @param cnvMeta メタ情報変換関数またはnull
	 * @param cnvMv 小節データ変換関数またはnull
	 * @param cnvNote ノート変換関数またはnull
	 * @throws NullPointerException srcがnull
	 * @throws IllegalArgumentException srcが編集モード
	 * @throws ClassCastException メタ情報・小節データの変換で、当該要素のデータ型に適合しないデータを返した
	 * @throws IllegalArgumentException メタ情報の変換で、初期BPMメタ情報に{@link BmsSpec#BPM_MIN}未満または{@link BmsSpec#BPM_MAX}超過の値を設定しようとした
	 * @throws IllegalArgumentException メタ情報の変換で、BPM変更メタ情報に{@link BmsSpec#BPM_MIN}未満または{@link BmsSpec#BPM_MAX}超過の値を設定しようとした
	 * @throws IllegalArgumentException メタ情報の変換で、譜面停止時間メタ情報に{@link BmsSpec#STOP_MIN}未満または{@link BmsSpec#STOP_MAX}超過の値を設定しようとした
	 * @throws IllegalArgumentException 小節データの変換で、小節長に{@link BmsSpec#LENGTH_MIN}未満または{@link BmsSpec#LENGTH_MAX}超過の値を設定しようとした
	 * @throws IllegalArgumentException ノートの変換で、{@link BmsSpec#CHANNEL_MIN}未満または{@link BmsSpec#CHANNEL_MAX}超過のチャンネル番号を返した
	 * @throws IllegalArgumentException ノートの変換で、BMS仕様に存在しないチャンネル番号を返した
	 * @throws IllegalArgumentException ノートの変換で、配列型以外のチャンネル番号を返した
	 * @throws IndexOutOfBoundsException ノートの変換で、コピー先の重複不可チャンネルへチャンネルインデックスが1以上のノートをコピーしようとした
	 * @throws IllegalArgumentException ノートの変換で、小節番号が{@link BmsSpec#MEASURE_MIN}未満または{@link BmsSpec#MEASURE_MAX}超過
	 * @throws IllegalArgumentException ノートの変換で、小節の刻み位置がマイナス値または当該小節の刻み数以上
	 * @throws IllegalArgumentException ノートの変換で、ノートの値に0を指定した
	 * @throws IllegalArgumentException ノートの変換で、ノートの値に{@link BmsSpec#VALUE_MIN}未満または{@link BmsSpec#VALUE_MAX}超過の値を指定した
	 * @since 0.8.0
	 */
	public BmsContent(BmsContent src,
			Function<BmsDeclarationElement, String> cnvDecl, Function<BmsMetaElement, Object> cnvMeta,
			Function<BmsMeasureValue, Object> cnvMv, UnaryOperator<BmsNote> cnvNote) {
		assertArgNotNull(src, "src");
		assertArg(src.isReferenceMode(), "Argument 'src' is NOT reference mode");

		// コピーに必要なデータを準備する
		initialSetup(src.getSpec());
		Function<BmsDeclarationElement, String> declConverter = (cnvDecl != null) ? cnvDecl :  e -> e.getValue();
		Function<BmsMetaElement, Object> metaConverter = (cnvMeta != null) ? cnvMeta : e -> e.getRawValue();
		Function<BmsMeasureValue, Object> mvConverter = (cnvMv != null) ? cnvMv : e -> e.getValueAsObject();
		UnaryOperator<BmsNote> noteConverter = (cnvNote != null) ? cnvNote : UnaryOperator.identity();
		beginEdit();

		// BMS宣言を全てコピー・変換する
		src.declarations().forEach(d -> {
			var value = declConverter.apply(d);
			if (value != null) {
				putDeclaration(d.getName(), value);
			}
		});

		// メタ情報を全てコピー・変換する
		src.metas().forEach(m -> {
			// 空の複数・索引付きメタ情報を表す要素はコピー・変換処理をスキップする
			var index = m.getIndex();
			if (index >= 0) {
				var value = metaConverter.apply(m);
				if (value != null) {
					mMetas.setMeta(m.getName(), m.getUnit(), index, value);
				}
			}
		});

		// タイムライン要素を全てコピー・変換する
		src.timeline().forEach(t -> {
			if (t.isNoteElement()) {
				// ノートの場合の処理
				var newNote = noteConverter.apply((BmsNote)t);
				if (newNote != null) {
					mTlAccessor.putTimeline(newNote);
				}
			} else if (t.isMeasureValueElement()) {
				// 小節データの場合
				var value = mvConverter.apply((BmsMeasureValue)t);
				if (value != null) {
					mTlAccessor.setValue(t.getChannel(), t.getIndex(), t.getMeasure(), value);
				}
			} else {
				// Do nothing
			}
		});
		endEdit();
	}

	/**
	 * BMSコンテンツに関連付けたBMS仕様を取得します。
	 * @return BMS仕様
	 */
	public BmsSpec getSpec() {
		return mSpec;
	}

	/**
	 * 指定した名前のBMS宣言を追加します。
	 * <p>同じ名前のBMS宣言が存在する場合は新しい値で上書きします。</p>
	 * <p>名前はアルファベットの大文字と小文字を区別して扱います。</p>
	 * <p>名前は1文字目が英字かアンダースコア、2文字目以降は英数字かアンダースコアでなければなりません。
	 * 値に使用する文字で、必要なものはエスケープしなければなりません。(例：\")</p>
	 * @param name BMS宣言の名称
	 * @param value 値
	 * @throws IllegalStateException 動作モードが編集モードではない
	 * @throws NullPointerException nameがnull
	 * @throws NullPointerException valueがnull
	 * @throws IllegalArgumentException nameの書式が不正
	 * @throws IllegalArgumentException valueの書式が不正
	 */
	public void putDeclaration(String name, String value) {
		assertIsEditMode();
		assertArgNotNull(name, "name");
		assertArg(PATTERN_DECL_NAME.matcher(name).matches(), "Wrong declaration name. name='%s'", name);
		assertArgNotNull(value, "value");
		assertArg(PATTERN_DECL_VALUE.matcher(value).matches(), "Wrong declaration value. name='%s', value='%s'", name, value);
		mBmsDeclarations.put(name, new BmsDeclarationElement(name, value));
	}

	/**
	 * 指定したBMS宣言を追加します。
	 * <p>同じ名前のBMS宣言が存在する場合は新しい値で上書きします。</p>
	 * @param declaration 追加対象のBMS宣言
	 * @throws IllegalStateException 動作モードが編集モードではない
	 * @throws NullPointerException declarationがnull
	 * @since 0.8.0
	 */
	public void putDeclaration(BmsDeclarationElement declaration) {
		assertIsEditMode();
		assertArgNotNull(declaration, "declaration");
		mBmsDeclarations.put(declaration.getName(), declaration);
	}

	/**
	 * 指定した名前のBMS宣言を消去します。
	 * @param name BMS宣言の名称
	 * @return 該当するBMS宣言を消去した場合はtrue、BMS宣言が存在しない場合はfalse
	 * @throws IllegalStateException 動作モードが編集モードではない
	 * @throws NullPointerException nameがnull
	 */
	public boolean removeDeclaration(String name) {
		assertIsEditMode();
		assertArgNotNull(name, "name");
		return mBmsDeclarations.remove(name) != null;
	}

	/**
	 * 指定した名前のBMS宣言が存在するかを判定します。
	 * @param name BMS宣言の名称
	 * @return 該当するBMS宣言が存在する場合はtrue、存在しない場合はfalse
	 * @throws NullPointerException nameがnull
	 */
	public boolean containsDeclaration(String name) {
		assertArgNotNull(name, "name");
		return mBmsDeclarations.containsKey(name);
	}

	/**
	 * 指定した名前のBMS宣言の値を取得します。
	 * @param name BMS宣言の名称
	 * @return 該当するBMS宣言の値、存在しない場合はnull
	 * @throws NullPointerException nameがnull
	 */
	public String getDeclaration(String name) {
		assertArgNotNull(name, "name");
		var element = mBmsDeclarations.get(name);
		return (element == null) ? null : element.getValue();
	}

	/**
	 * 全てのBMS宣言の名前と値の一覧を取得します。
	 * <p>当メソッドはBMSコンテンツが保有する全てのBMS宣言マップのコピーを返します。
	 * 当メソッド呼び出し後にBMSコンテンツにBMS宣言を追加しても返されたマップには反映されません。</p>
	 * <p>BMS宣言は{@link #putDeclaration(String, String)}等で登録された順で一覧化されます。</p>
	 * @return BMS宣言の名前と値のマップ
	 */
	public Map<String, String> getDeclarations() {
		return mBmsDeclarations.values().stream().collect(Collectors.toMap(
				e -> e.getName(), e -> e.getValue(), (o, n) -> n, LinkedHashMap::new));
	}

	/**
	 * BMSコンテンツに登録されたBMS宣言を登録順に走査するストリームを返します。
	 * @return BMS宣言を走査するストリーム
	 * @since 0.8.0
	 */
	public Stream<BmsDeclarationElement> declarations() {
		return mBmsDeclarations.values().stream();
	}

	/**
	 * 単体メタ情報に値を設定します。
	 * <p>値にnullを設定することで当該メタ情報の値を消去します。これは、値を指定しないことを意味し、
	 * {@link #getSingleMeta}で値を取得した場合には、そのメタ情報の初期値を返します。
	 * しかし、値は消去されているため{@link #containsSingleMeta}はfalseを返すことに注意してください。</p>
	 * <p>指定のメタ情報が基数選択メタ情報の場合、設定可能な値はnull, 16, 36, 62のいずれかのみになります。
	 * それ以外の値を指定しようとすると例外がスローされます。また、基数選択メタ情報を更新したとしても、
	 * 基数選択が影響するデータが自動的に範囲内に収められる等の処理は行われません。
	 * 選択された基数が作用するのはBMSコンテンツを標準フォーマットで出力しようとする時です。
	 * 詳細な動作仕様については{@link BmsStandardSaver}を参照してください。</p>
	 * @param name メタ情報の名称
	 * @param value 設定する値
	 * @throws IllegalStateException 動作モードが編集モードではない
	 * @throws NullPointerException nameがnull
	 * @throws IllegalArgumentException 名称に合致するメタ情報が存在しない
	 * @throws ClassCastException valueが設定先メタ情報のデータ型に変換できない
	 * @throws IllegalArgumentException 初期BPMメタ情報に{@link BmsSpec#BPM_MIN}未満または{@link BmsSpec#BPM_MAX}超過の値を設定しようとした
	 * @throws IllegalArgumentException 基数選択メタ情報にnull, 16, 36, 62以外の値を設定しようとした
	 */
	public void setSingleMeta(String name, Object value) {
		mMetas.setSingleMeta(name, value);
	}

	/**
	 * 重複可能メタ情報に値を設定します。
	 * <p>重複可能メタ情報には値を複数登録できます。メタ情報は可変長リストで管理されており、リストのどの部分に
	 * 値を設定するかはインデックス値で決定します。インデックス値までの領域に値が存在しない場合、歯抜けの領域には
	 * 当該メタ情報の初期値が自動で設定されます。</p>
	 * <p>値を消去するには、値にnullを指定します。消去した値より後ろの値は左詰めされます。</p>
	 * <p>重複可能メタ情報のリスト末尾に値を挿入したい場合は{@link #putMultipleMeta}
	 * を使用することを推奨します。</p>
	 * @param name メタ情報の名称
	 * @param index 設定位置を示すインデックス
	 * @param value 設定する値
	 * @throws IllegalStateException 動作モードが編集モードではない
	 * @throws NullPointerException nameがnull
	 * @throws IllegalArgumentException 名称に合致するメタ情報が存在しない
	 * @throws IndexOutOfBoundsException indexが0～{@link BmsSpec#MULTIPLE_META_INDEX_MAX}の範囲外
	 * @throws ClassCastException valueが設定先メタ情報のデータ型に変換できない
	 */
	public void setMultipleMeta(String name, int index, Object value) {
		mMetas.setMultipleMeta(name, index, value);
	}

	/**
	 * 索引付きメタ情報に値を設定します。
	 * <p>索引付きメタ情報とは、メタ情報の名称の後ろに00～ZZのインデックス値を付加したメタ情報のことを指します。</p>
	 * <p>値を消去するには、値にnullを指定します。重複可能メタ情報とは異なり、リストの左詰めは行われません。</p>
	 * @param name メタ情報の名称
	 * @param index 設定箇所を示すインデックス
	 * @param value 設定する値
	 * @throws IllegalStateException 動作モードが編集モードではない
	 * @throws NullPointerException nameがnull
	 * @throws IllegalArgumentException 名称に合致するメタ情報が存在しない
	 * @throws IndexOutOfBoundsException indexが0～{@link BmsSpec#INDEXED_META_INDEX_MAX}の範囲外
	 * @throws ClassCastException valueが設定先メタ情報のデータ型に変換できない
	 * @throws IllegalArgumentException BPM変更メタ情報に{@link BmsSpec#BPM_MIN}未満または{@link BmsSpec#BPM_MAX}超過の値を設定しようとした
	 * @throws IllegalArgumentException 譜面停止時間メタ情報に{@link BmsSpec#STOP_MIN}未満または{@link BmsSpec#STOP_MAX}超過の値を設定しようとした
	 */
	public void setIndexedMeta(String name, int index, Object value) {
		mMetas.setIndexedMeta(name, index, value);
	}

	/**
	 * 指定したメタ情報キーに該当するメタ情報に値を設定します。
	 * <p>設定の処理内容については、単位ごとのメタ情報設定メソッドの説明を参照してください。</p>
	 * <p>複数・索引付きメタ情報の場合、インデックス0の値を設定します。</p>
	 * @see #setSingleMeta
	 * @see #setMultipleMeta
	 * @see #setIndexedMeta
	 * @param key メタ情報キー
	 * @param value 設定する値
	 * @throws IllegalStateException 動作モードが編集モードではない
	 * @throws NullPointerException keyがnull
	 * @throws IllegalArgumentException メタ情報キーに合致するメタ情報が存在しない
	 * @throws ClassCastException valueが設定先メタ情報のデータ型に変換できない
	 * @throws IllegalArgumentException 初期BPMメタ情報に{@link BmsSpec#BPM_MIN}未満または{@link BmsSpec#BPM_MAX}超過の値を設定しようとした
	 * @throws IllegalArgumentException BPM変更メタ情報に{@link BmsSpec#BPM_MIN}未満または{@link BmsSpec#BPM_MAX}超過の値を設定しようとした
	 * @throws IllegalArgumentException 譜面停止時間メタ情報に{@link BmsSpec#STOP_MIN}未満または{@link BmsSpec#STOP_MAX}超過の値を設定しようとした
	 */
	public void setMeta(BmsMetaKey key, Object value) {
		assertArgNotNull(key, "key");
		mMetas.setMeta(key.getName(), key.getUnit(), 0, value);
	}

	/**
	 * 指定したメタ情報キーに該当するメタ情報に値を設定します。
	 * <p>設定の処理内容については、単位ごとのメタ情報設定メソッドの説明を参照してください。</p>
	 * <p>単体メタ情報に値を設定する場合、インデックスには0を指定してください。</p>
	 * @see #setSingleMeta
	 * @see #setMultipleMeta
	 * @see #setIndexedMeta
	 * @param key メタ情報キー
	 * @param index 設定先のインデックス
	 * @param value 設定する値
	 * @throws IllegalStateException 動作モードが編集モードではない
	 * @throws NullPointerException keyがnull
	 * @throws IllegalArgumentException メタ情報キーに合致するメタ情報が存在しない
	 * @throws IndexOutOfBoundsException 構成単位が単体の時、indexが0以外
	 * @throws IndexOutOfBoundsException 構成単位が複数の時、indexが0～{@link BmsSpec#MULTIPLE_META_INDEX_MAX}の範囲外
	 * @throws IndexOutOfBoundsException 構成単位が索引付きの時、indexが0～{@link BmsSpec#INDEXED_META_INDEX_MAX}の範囲外
	 * @throws ClassCastException valueが設定先メタ情報のデータ型に変換できない
	 * @throws IllegalArgumentException 初期BPMメタ情報に{@link BmsSpec#BPM_MIN}未満または{@link BmsSpec#BPM_MAX}超過の値を設定しようとした
	 * @throws IllegalArgumentException BPM変更メタ情報に{@link BmsSpec#BPM_MIN}未満または{@link BmsSpec#BPM_MAX}超過の値を設定しようとした
	 * @throws IllegalArgumentException 譜面停止時間メタ情報に{@link BmsSpec#STOP_MIN}未満または{@link BmsSpec#STOP_MAX}超過の値を設定しようとした
	 */
	public void setMeta(BmsMetaKey key, int index, Object value) {
		assertArgNotNull(key, "key");
		mMetas.setMeta(key.getName(), key.getUnit(), index, value);
	}

	/**
	 * 指定した名称・単位のメタ情報に値を設定します。
	 * <p>設定の処理内容については、単位ごとのメタ情報設定メソッドの説明を参照してください。</p>
	 * <p>単体メタ情報に値を設定する場合、インデックスには0を指定してください。</p>
	 * @see #setSingleMeta
	 * @see #setMultipleMeta
	 * @see #setIndexedMeta
	 * @param name メタ情報の名称
	 * @param unit メタ情報の単位
	 * @param index 設定先のインデックス
	 * @param value 設定する値
	 * @throws IllegalStateException 動作モードが編集モードではない
	 * @throws NullPointerException nameがnull
	 * @throws NullPointerException unitがnull
	 * @throws IllegalArgumentException name, unitに合致するメタ情報が存在しない
	 * @throws IndexOutOfBoundsException unitが{@link BmsUnit#SINGLE SINGLE}の時、indexが0以外
	 * @throws IndexOutOfBoundsException unitが{@link BmsUnit#MULTIPLE MULTIPLE}の時、indexが0～{@link BmsSpec#MULTIPLE_META_INDEX_MAX}の範囲外
	 * @throws IndexOutOfBoundsException unitが{@link BmsUnit#INDEXED INDEXED}の時、indexが0～{@link BmsSpec#INDEXED_META_INDEX_MAX}の範囲外
	 * @throws ClassCastException valueが設定先メタ情報のデータ型に変換できない
	 * @throws IllegalArgumentException 初期BPMメタ情報に{@link BmsSpec#BPM_MIN}未満または{@link BmsSpec#BPM_MAX}超過の値を設定しようとした
	 * @throws IllegalArgumentException BPM変更メタ情報に{@link BmsSpec#BPM_MIN}未満または{@link BmsSpec#BPM_MAX}超過の値を設定しようとした
	 * @throws IllegalArgumentException 譜面停止時間メタ情報に{@link BmsSpec#STOP_MIN}未満または{@link BmsSpec#STOP_MAX}超過の値を設定しようとした
	 */
	public void setMeta(String name, BmsUnit unit, int index, Object value) {
		mMetas.setMeta(name, unit, index, value);
	}

	/**
	 * 指定したメタ情報要素をBMSコンテンツに追加します。
	 * <p>当メソッドは同一、または互換性のあるBMS仕様を持つ別のBMSコンテンツで走査したメタ情報を追加する時に使用することを想定しています。
	 * 互換性のないBMS仕様を持つBMSコンテンツを走査した結果取得したメタ情報要素を指定するとBMS仕様違反によりメタ情報を追加できない可能性があり、
	 * 多くの場合例外がスローされる結果になります。原則として同一のBMS仕様を持つBMSコンテンツ間のメタ情報の転送に使用することを強く推奨します。</p>
	 * <p>データが未登録のメタ情報要素を指定した場合、BMSコンテンツからそのメタ情報のデータを消去します。
	 * また、複数・索引付きメタ情報で、当該メタ情報のデータが0個を表すメタ情報要素({@link BmsMetaElement#getIndex()}がマイナス値を示す)
	 * を指定した場合、当メソッドは何も行いません。</p>
	 * @param meta メタ情報要素
	 * @throws IllegalStateException 動作モードが編集モードではない
	 * @throws NullPointerException metaがnull
	 * @throws ClassCastException メタ情報の値が設定先メタ情報のデータ型に変換できない
	 * @since 0.8.0
	 */
	public void putMeta(BmsMetaElement meta) {
		assertArgNotNull(meta, "meta");
		if (meta.getIndex() >= 0) {
			mMetas.setMeta(meta.getName(), meta.getUnit(), meta.getIndex(), meta.getRawValue());
		}
	}

	/**
	 * 指定した重複可能メタ情報のリスト末尾に値を挿入します。
	 * <p>このメソッドでは、値にnullを指定することによる消去は行えません。</p>
	 * @param name メタ情報の名称
	 * @param value 設定する値
	 * @throws IllegalStateException 動作モードが編集モードではない
	 * @throws NullPointerException nameがnull
	 * @throws NullPointerException valueがnull
	 * @throws IllegalArgumentException 名称に合致するメタ情報が存在しない
	 * @throws IndexOutOfBoundsException リストが一杯でこれ以上値を挿入できない
	 * @throws ClassCastException valueが設定先メタ情報のデータ型に変換できない
	 */
	public void putMultipleMeta(String name, Object value) {
		assertArgNotNull(value, "value");
		mMetas.setMultipleMeta(name, mMetas.getMultipleMetaCount(name), value);
	}

	/**
	 * 単体メタ情報の値を取得します。
	 * <p>メタ情報に値が設定されていない場合、そのメタ情報の初期値を返します。値が設定されているかどうかを
	 * 判定したい場合は{@link #containsSingleMeta}を使用してください。</p>
	 * <p>取得した値のデータ型を判別したい場合はメタ情報のデータ型を参照し、{@link BmsType#getNativeType}が
	 * 返す値を調べます。メタ情報のデータ型が明確に判明している場合は直接キャストして構いません。</p>
	 * @param <T> メタ情報のデータ型
	 * @param name メタ情報の名称
	 * @return メタ情報の値
	 * @throws NullPointerException nameがnull
	 * @throws IllegalArgumentException 名称に合致するメタ情報が存在しない
	 */
	@SuppressWarnings("unchecked")
	public <T> T getSingleMeta(String name) {
		return (T)mMetas.getSingleMeta(name);
	}

	/**
	 * 重複可能メタ情報の値を取得します。
	 * <p>メタ情報に値が設定されていない場合、そのメタ情報の初期値を返します。値が設定されているかどうかを
	 * 判定したい場合は{@link #containsMultipleMeta}を使用してください。</p>
	 * <p>取得した値のデータ型を判別したい場合はメタ情報のデータ型を参照し、{@link BmsType#getNativeType}が
	 * 返す値を調べます。メタ情報のデータ型が明確に判明している場合は直接キャストして構いません。</p>
	 * @param <T> メタ情報のデータ型
	 * @param name メタ情報の名称
	 * @param index インデックス
	 * @return メタ情報の値
	 * @throws NullPointerException nameがnull
	 * @throws IndexOutOfBoundsException indexが0～{@link BmsSpec#MULTIPLE_META_INDEX_MAX}の範囲外
	 * @throws IllegalArgumentException 名称に合致するメタ情報が存在しない
	 */
	@SuppressWarnings("unchecked")
	public <T> T getMultipleMeta(String name, int index) {
		return (T)mMetas.getMultipleMeta(name, index);
	}

	/**
	 * 索引付き可能メタ情報の値を取得します。
	 * <p>メタ情報に値が設定されていない場合、そのメタ情報の初期値を返します。値が設定されているかどうかを
	 * 判定したい場合は{@link #containsIndexedMeta}を使用してください。</p>
	 * <p>取得した値のデータ型を判別したい場合はメタ情報のデータ型を参照し、{@link BmsType#getNativeType}が
	 * 返す値を調べます。メタ情報のデータ型が明確に判明している場合は直接キャストして構いません。</p>
	 * @param <T> メタ情報のデータ型
	 * @param name メタ情報の名称
	 * @param index インデックス
	 * @return メタ情報の値
	 * @throws NullPointerException nameがnull
	 * @throws IndexOutOfBoundsException indexが0～{@link BmsSpec#INDEXED_META_INDEX_MAX}の範囲外
	 * @throws IllegalArgumentException 名称に合致するメタ情報が存在しない
	 */
	@SuppressWarnings("unchecked")
	public <T> T getIndexedMeta(String name, int index) {
		return (T)mMetas.getIndexedMeta(name, index);
	}

	/**
	 * 指定したメタ情報キーに該当するメタ情報の値を取得します。
	 * <p>取得の処理内容については、単位ごとのメタ情報取得メソッドの説明を参照してください。</p>
	 * <p>複数・索引付きメタ情報の場合、インデックス0の値を取得します。</p>
	 * @see #getSingleMeta
	 * @see #getMultipleMeta
	 * @see #getIndexedMeta
	 * @param <T> メタ情報のデータ型
	 * @param key メタ情報キー
	 * @return メタ情報の値
	 * @throws NullPointerException keyがnull
	 * @throws IllegalArgumentException メタ情報キーに合致するメタ情報が存在しない
	 */
	@SuppressWarnings("unchecked")
	public <T> T getMeta(BmsMetaKey key) {
		assertArgNotNull(key, "key");
		return (T)mMetas.getMeta(key.getName(), key.getUnit(), 0);
	}

	/**
	 * 指定したメタ情報キーに該当するメタ情報の値を取得します。
	 * <p>取得の処理内容については、単位ごとのメタ情報取得メソッドの説明を参照してください。</p>
	 * <p>単体メタ情報に値を設定する場合、インデックスには0を指定してください。</p>
	 * @see #getSingleMeta
	 * @see #getMultipleMeta
	 * @see #getIndexedMeta
	 * @param <T> メタ情報のデータ型
	 * @param key メタ情報キー
	 * @param index 設定先のインデックス
	 * @return メタ情報の値
	 * @throws NullPointerException keyがnull
	 * @throws IllegalArgumentException メタ情報キーに合致するメタ情報が存在しない
	 * @throws IndexOutOfBoundsException 構成単位が単体の時、indexが0以外
	 * @throws IndexOutOfBoundsException 構成単位が複数の時、indexが0～{@link BmsSpec#MULTIPLE_META_INDEX_MAX}の範囲外
	 * @throws IndexOutOfBoundsException 構成単位が索引付きの時、indexが0～{@link BmsSpec#INDEXED_META_INDEX_MAX}の範囲外
	 */
	@SuppressWarnings("unchecked")
	public <T> T getMeta(BmsMetaKey key, int index) {
		assertArgNotNull(key, "key");
		return (T)mMetas.getMeta(key.getName(), key.getUnit(), index);
	}

	/**
	 * 指定した名称・単位のメタ情報の値を取得します。
	 * <p>取得の処理内容については、単位ごとのメタ情報取得メソッドの説明を参照してください。</p>
	 * <p>単体メタ情報に値を設定する場合、インデックスには0を指定してください。</p>
	 * @see #getSingleMeta
	 * @see #getMultipleMeta
	 * @see #getIndexedMeta
	 * @param <T> メタ情報のデータ型
	 * @param name メタ情報の名称
	 * @param unit メタ情報の単位
	 * @param index 設定先のインデックス
	 * @return メタ情報の値
	 * @throws NullPointerException nameがnull
	 * @throws NullPointerException unitがnull
	 * @throws IllegalArgumentException name, unitに合致するメタ情報が存在しない
	 * @throws IndexOutOfBoundsException unitが{@link BmsUnit#SINGLE SINGLE}の時、indexが0以外
	 * @throws IndexOutOfBoundsException unitが{@link BmsUnit#MULTIPLE MULTIPLE}の時、indexが0～{@link BmsSpec#MULTIPLE_META_INDEX_MAX}の範囲外
	 * @throws IndexOutOfBoundsException unitが{@link BmsUnit#INDEXED INDEXED}の時、indexが0～{@link BmsSpec#INDEXED_META_INDEX_MAX}の範囲外
	 */
	@SuppressWarnings("unchecked")
	public <T> T getMeta(String name, BmsUnit unit, int index) {
		return (T)mMetas.getMeta(name, unit, index);
	}

	/**
	 * 重複可能メタ情報の値リストを取得します。
	 * @param <T> メタ情報のデータ型
	 * @param name メタ情報の名称
	 * @return 重複可能メタ情報の値リスト
	 * @throws NullPointerException nameがnull
	 * @throws IllegalArgumentException 名称に合致するメタ情報が存在しない
	 */
	@SuppressWarnings("unchecked")
	public <T> List<T> getMultipleMetas(String name) {
		return (List<T>)mMetas.getMultipleMetas(name);
	}

	/**
	 * 索引付きメタ情報の値マップを取得します。
	 * <p>このメソッドで取得できるマップに格納されるのは明示的に値を設定された索引のみです。
	 * {@link #getIndexedMeta}では未設定の索引の値は初期値を返しますが、返されるマップに同じ索引を指定しても
	 * nullが返ることに注意してください。</p>
	 * @param <T> メタ情報のデータ型
	 * @param name メタ情報の名称
	 * @return 索引付きメタ情報の値マップ
	 * @throws NullPointerException nameがnull
	 * @throws IllegalArgumentException 名称に合致するメタ情報が存在しない
	 */
	@SuppressWarnings("unchecked")
	public <T> Map<Integer, T> getIndexedMetas(String name) {
		return (Map<Integer, T>)mMetas.getIndexedMetas(name);
	}

	/**
	 * 単体メタ情報の数を取得します。
	 * <p>単体メタ情報は1件のデータからなる情報です。値が設定されている場合に1を返します。</p>
	 * @param name メタ情報の名称
	 * @return メタ情報の数
	 * @throws NullPointerException nameがnull
	 * @throws IllegalArgumentException 名称に合致するメタ情報が存在しない
	 */
	public int getSingleMetaCount(String name) {
		return mMetas.getSingleMetaCount(name);
	}

	/**
	 * 重複可能メタ情報の数を取得します。
	 * @param name メタ情報の名称
	 * @return メタ情報の数
	 * @throws NullPointerException nameがnull
	 * @throws IllegalArgumentException 名称に合致するメタ情報が存在しない
	 */
	public int getMultipleMetaCount(String name) {
		return mMetas.getMultipleMetaCount(name);
	}

	/**
	 * 索引付きメタ情報の数を取得します。
	 * @param name メタ情報の名称
	 * @return メタ情報の数
	 * @throws NullPointerException nameがnull
	 * @throws IllegalArgumentException 名称に合致するメタ情報が存在しない
	 */
	public int getIndexedMetaCount(String name) {
		return mMetas.getIndexedMetaCount(name);
	}

	/**
	 * 指定したメタ情報キーに該当するメタ情報の数を取得します。
	 * <p>取得の処理内容については、単位ごとのメタ情報件数取得メソッドの説明を参照してください。</p>
	 * @see #getSingleMetaCount
	 * @see #getMultipleMetaCount
	 * @see #getIndexedMetaCount
	 * @param key メタ情報キー
	 * @return メタ情報の数
	 * @throws NullPointerException keyがnull
	 * @throws IllegalArgumentException メタ情報キーに合致するメタ情報が存在しない
	 */
	public int getMetaCount(BmsMetaKey key) {
		assertArgNotNull(key, "key");
		return mMetas.getMetaCount(key.getName(), key.getUnit());
	}

	/**
	 * 指定した名称・単位のメタ情報の数を取得します。
	 * <p>取得の処理内容については、単位ごとのメタ情報件数取得メソッドの説明を参照してください。</p>
	 * @see #getSingleMetaCount
	 * @see #getMultipleMetaCount
	 * @see #getIndexedMetaCount
	 * @param name メタ情報の名称
	 * @param unit メタ情報の単位
	 * @return メタ情報の数
	 * @throws NullPointerException nameがnull
	 * @throws NullPointerException unitがnull
	 * @throws IllegalArgumentException 名称に合致するメタ情報が存在しない
	 */
	public int getMetaCount(String name, BmsUnit unit) {
		return mMetas.getMetaCount(name, unit);
	}

	/**
	 * 単体メタ情報に値が設定されているかどうかを判定します。
	 * @param name メタ情報の名称
	 * @return 値が設定されている場合true
	 * @throws NullPointerException nameがnull
	 * @throws IllegalArgumentException 名称に合致するメタ情報が存在しない
	 */
	public boolean containsSingleMeta(String name) {
		return mMetas.containsSingleMeta(name);
	}

	/**
	 * 重複可能メタ情報に値が設定されているかどうか判定します。
	 * @param name メタ情報の名称
	 * @param index インデックス
	 * @return 値が設定されている場合true
	 * @throws NullPointerException nameがnull
	 * @throws IllegalArgumentException 名称に合致するメタ情報が存在しない
	 * @throws IndexOutOfBoundsException indexが0～{@link BmsSpec#MULTIPLE_META_INDEX_MAX}の範囲外
	 */
	public boolean containsMultipleMeta(String name, int index) {
		return mMetas.containsMultipleMeta(name, index);
	}

	/**
	 * 索引付きメタ情報に値が設定されているかどうか判定します。
	 * @param name メタ情報の名称
	 * @param index インデックス
	 * @return 値が設定されている場合true
	 * @throws NullPointerException nameがnull
	 * @throws IllegalArgumentException 名称に合致するメタ情報が存在しない
	 * @throws IndexOutOfBoundsException indexが0～{@link BmsSpec#INDEXED_META_INDEX_MAX}の範囲外
	 */
	public boolean containsIndexedMeta(String name, int index) {
		return mMetas.containsIndexedMeta(name, index);
	}

	/**
	 * 指定したメタ情報キーに該当するメタ情報に1件以上値が設定されているかどうか判定します。
	 * <p>単体メタ情報の場合、そのメタ情報に値が設定されていればtrueを返します。複数・索引付きメタ情報の場合、そのメタ情報に1件でも
	 * 値が設定されていればtrueを返します。登録先インデックスの値は問いません。</p>
	 * @param key メタ情報キー
	 * @return 値が設定されている場合true
	 * @throws NullPointerException keyがnull
	 * @throws IllegalArgumentException メタ情報キーに合致するメタ情報が存在しない
	 */
	public boolean containsMeta(BmsMetaKey key) {
		assertArgNotNull(key, "key");
		return mMetas.containsMeta(key.getName(), key.getUnit());
	}

	/**
	 * 指定したメタ情報キー・インデックスに該当するメタ情報に値が設定されているかどうか判定します。
	 * <p>判定の処理内容については、単位ごとの判定メソッドの説明を参照してください。</p>
	 * <p>単体メタ情報に値を設定する場合、インデックスには0を指定してください。</p>
	 * @see #containsSingleMeta
	 * @see #containsMultipleMeta
	 * @see #containsIndexedMeta
	 * @param key メタ情報キー
	 * @param index インデックス
	 * @return 値が設定されている場合true
	 * @throws NullPointerException keyがnull
	 * @throws IllegalArgumentException メタ情報キーに合致するメタ情報が存在しない
	 * @throws IndexOutOfBoundsException 構成単位が単体の時、indexが0以外
	 * @throws IndexOutOfBoundsException 構成単位が複数の時、indexが0～{@link BmsSpec#MULTIPLE_META_INDEX_MAX}の範囲外
	 * @throws IndexOutOfBoundsException 構成単位が索引付きの時、indexが0～{@link BmsSpec#INDEXED_META_INDEX_MAX}の範囲外
	 */
	public boolean containsMeta(BmsMetaKey key, int index) {
		assertArgNotNull(key, "key");
		return mMetas.containsMeta(key.getName(), key.getUnit(), index);
	}

	/**
	 * 指定した名称・単位・インデックスのメタ情報に値が設定されているかどうか判定します。
	 * <p>判定の処理内容については、単位ごとの判定メソッドの説明を参照してください。</p>
	 * <p>単体メタ情報に値を設定する場合、インデックスには0を指定してください。</p>
	 * @see #containsSingleMeta
	 * @see #containsMultipleMeta
	 * @see #containsIndexedMeta
	 * @param name メタ情報の名称
	 * @param unit メタ情報の単位
	 * @param index インデックス
	 * @return 値が設定されている場合true
	 * @throws NullPointerException nameがnull
	 * @throws NullPointerException unitがnull
	 * @throws IllegalArgumentException 名称・単位に合致するメタ情報が存在しない
	 * @throws IndexOutOfBoundsException unitが{@link BmsUnit#SINGLE SINGLE}の時、indexが0以外
	 * @throws IndexOutOfBoundsException unitが{@link BmsUnit#MULTIPLE MULTIPLE}の時、indexが0～{@link BmsSpec#MULTIPLE_META_INDEX_MAX}の範囲外
	 * @throws IndexOutOfBoundsException unitが{@link BmsUnit#INDEXED INDEXED}の時、indexが0～{@link BmsSpec#INDEXED_META_INDEX_MAX}の範囲外
	 */
	public boolean containsMeta(String name, BmsUnit unit, int index) {
		return mMetas.containsMeta(name, unit, index);
	}

	/**
	 * 指定した名称・単位のメタ情報に1件以上値が設定されているかどうか判定します。
	 * <p>単体メタ情報の場合、そのメタ情報に値が設定されていればtrueを返します。複数・索引付きメタ情報の場合、そのメタ情報に1件でも
	 * 値が設定されていればtrueを返します。登録先インデックスの値は問いません。</p>
	 * @param name メタ情報の名称
	 * @param unit メタ情報の単位
	 * @return 値が設定されている場合true
	 * @throws NullPointerException nameがnull
	 * @throws NullPointerException unitがnull
	 * @throws IllegalArgumentException 名称・単位に合致するメタ情報が存在しない
	 */
	public boolean containsMeta(String name, BmsUnit unit) {
		return mMetas.containsMeta(name, unit);
	}

	/**
	 * このBMSコンテンツのメタ情報を走査します。
	 * <p>当メソッドはBMSコンテンツに設定されたBMS仕様に基づき、全てのメタ情報を走査します。
	 * データの登録有無に関わらず、BMS仕様に登録された全てのメタ情報が走査対象になります。
	 * 走査されたメタ情報にデータが登録されているかどうかは{@link BmsMetaElement#isContain()}で調べてください。</p>
	 * <p>当メソッドでメタ情報を走査すると、単体・複数・索引付きの順でメタ情報の名称順に昇順で走査されます。
	 * 特定の構成単位のみ走査したい場合は{@link #singleMetas()}, {@link #multipleMetas()}, {@link #indexedMetas()}
	 * を使用するほうがより効率的に操作することができます。</p>
	 * <p>複数・索引付きメタ情報では、当該メタ情報のデータが0件の時にインデックスが-1のメタ情報要素を走査します。
	 * この要素には値は格納されておらず、{@link BmsMetaElement#getValue()}等で値を取得しても当該メタ情報の初期値を返します。</p>
	 * <p>当メソッドは編集モード時には使用できません。使用すると例外をスローします。
	 * また、走査を行っている最中に編集モードに遷移してメタ情報を更新した場合、走査中の結果がどのようになるかは未定義です。</p>
	 * @return メタ情報を走査するストリーム
	 * @throws IllegalStateException 動作モードが編集モード
	 * @since 0.8.0
	 */
	public Stream<BmsMetaElement> metas() {
		return mMetas.metas();
	}

	/**
	 * このBMSコンテンツの単体メタ情報を走査します。
	 * <p>当メソッドはBMSコンテンツに設定されたBMS仕様に基づき、全ての単体メタ情報を走査します。
	 * データの登録有無に関わらず、BMS仕様に登録された全ての単体メタ情報が走査対象になります。
	 * 走査された単体メタ情報にデータが登録されているかどうかは{@link BmsMetaElement#isContain()}で調べてください。</p>
	 * <p>当メソッドで単体メタ情報を走査すると、メタ情報の名称順に昇順で走査されます。</p>
	 * <p>当メソッドは編集モード時には使用できません。使用すると例外をスローします。
	 * また、走査を行っている最中に編集モードに遷移して単体メタ情報を更新した場合、走査中の結果がどのようになるかは未定義です。</p>
	 * @return 単体メタ情報を走査するストリーム
	 * @throws IllegalStateException 動作モードが編集モード
	 * @since 0.8.0
	 */
	public Stream<BmsMetaElement> singleMetas() {
		return mMetas.singleMetas();
	}

	/**
	 * このBMSコンテンツの複数メタ情報を走査します。
	 * <p>当メソッドはBMSコンテンツに設定されたBMS仕様に基づき、全ての複数メタ情報を走査します。
	 * データの登録有無に関わらず、BMS仕様に登録された全ての複数メタ情報が走査対象になります。
	 * 走査された複数メタ情報にデータが登録されているかどうかは{@link BmsMetaElement#isContain()}で調べてください。</p>
	 * <p>当メソッドで複数メタ情報を走査すると、メタ情報の名称順に昇順で走査されます。</p>
	 * <p>複数メタ情報では、当該メタ情報のデータが0件の時にインデックスが-1のメタ情報要素を走査します。
	 * この要素には値は格納されておらず、{@link BmsMetaElement#getValue()}等で値を取得しても当該メタ情報の初期値を返します。</p>
	 * <p>当メソッドは編集モード時には使用できません。使用すると例外をスローします。
	 * また、走査を行っている最中に編集モードに遷移して複数メタ情報を更新した場合、走査中の結果がどのようになるかは未定義です。</p>
	 * @return 複数メタ情報を走査するストリーム
	 * @throws IllegalStateException 動作モードが編集モード
	 * @since 0.8.0
	 */
	public Stream<BmsMetaElement> multipleMetas() {
		return mMetas.multipleMetas();
	}

	/**
	 * 指定した名前の複数メタ情報を走査します。
	 * <p>当メソッドは指定した名前の複数メタ情報が走査対象になります。
	 * 走査された複数メタ情報にデータが登録されているかどうかは{@link BmsMetaElement#isContain()}で調べてください。</p>
	 * <p>当該メタ情報のデータが0件の時にインデックスが-1のメタ情報要素を走査します。
	 * この要素には値は格納されておらず、{@link BmsMetaElement#getValue()}等で値を取得しても当該メタ情報の初期値を返します。</p>
	 * <p>当メソッドは編集モード時には使用できません。使用すると例外をスローします。
	 * また、走査を行っている最中に編集モードに遷移して複数メタ情報を更新した場合、走査中の結果がどのようになるかは未定義です。</p>
	 * @param name 走査対象の複数メタ情報の名前
	 * @return 複数メタ情報を走査するストリーム
	 * @throws IllegalStateException 動作モードが編集モード
	 * @throws NullPointerException nameがnull
	 * @throws IllegalArgumentException nameに該当する複数メタ情報がBMS仕様に存在しない
	 * @since 0.8.0
	 */
	public Stream<BmsMetaElement> multipleMetas(String name) {
		return mMetas.multipleMetas(name);
	}

	/**
	 * このBMSコンテンツの索引付きメタ情報を走査します。
	 * <p>当メソッドはBMSコンテンツに設定されたBMS仕様に基づき、全ての索引付きメタ情報を走査します。
	 * データの登録有無に関わらず、BMS仕様に登録された全ての索引付きメタ情報が走査対象になります。
	 * 走査された索引付きメタ情報にデータが登録されているかどうかは{@link BmsMetaElement#isContain()}で調べてください。</p>
	 * <p>当メソッドで索引付きメタ情報を走査すると、メタ情報の名称順に昇順で走査されます。</p>
	 * <p>索引付きメタ情報では、当該メタ情報のデータが0件の時にインデックスが-1のメタ情報要素を走査します。
	 * この要素には値は格納されておらず、{@link BmsMetaElement#getValue()}等で値を取得しても当該メタ情報の初期値を返します。</p>
	 * <p>当メソッドは編集モード時には使用できません。使用すると例外をスローします。
	 * また、走査を行っている最中に編集モードに遷移して索引付きメタ情報を更新した場合、走査中の結果がどのようになるかは未定義です。</p>
	 * @return 索引付きメタ情報を走査するストリーム
	 * @throws IllegalStateException 動作モードが編集モード
	 * @since 0.8.0
	 */
	public Stream<BmsMetaElement> indexedMetas() {
		return mMetas.indexedMetas();
	}

	/**
	 * 指定した名前の索引付きメタ情報を走査します。
	 * <p>当メソッドは指定した名前の索引付きメタ情報が走査対象になります。
	 * 走査された索引付きメタ情報にデータが登録されているかどうかは{@link BmsMetaElement#isContain()}で調べてください。</p>
	 * <p>当該メタ情報のデータが0件の時にインデックスが-1のメタ情報要素を走査します。
	 * この要素には値は格納されておらず、{@link BmsMetaElement#getValue()}等で値を取得しても当該メタ情報の初期値を返します。</p>
	 * <p>当メソッドは編集モード時には使用できません。使用すると例外をスローします。
	 * また、走査を行っている最中に編集モードに遷移して索引付きメタ情報を更新した場合、走査中の結果がどのようになるかは未定義です。</p>
	 * @param name 走査対象の索引付きメタ情報の名前
	 * @return 索引付きメタ情報を走査するストリーム
	 * @throws IllegalStateException 動作モードが編集モード
	 * @throws NullPointerException nameがnull
	 * @throws IllegalArgumentException nameに該当する索引付きメタ情報がBMS仕様に存在しない
	 * @since 0.8.0
	 */
	public Stream<BmsMetaElement> indexedMetas(String name) {
		return mMetas.indexedMetas(name);
	}

	/**
	 * 初期BPMを取得します。
	 * <p>初期BPMはBMSの共通仕様で、どのようなBMS仕様にも必ずメタ情報に存在します。このメソッドを呼び出すと
	 * {@link #getSingleMeta(String)}を呼び出して初期BPMメタ情報から値を取得した時と同等の処理を行います。</p>
	 * @return 初期BPM
	 */
	public double getInitialBpm() {
		return mMetas.getInitialBpm();
	}

	/**
	 * 初期BPMを設定します。
	 * <p>初期BPMはBMSの共通仕様で、どのようなBMS仕様にも必ずメタ情報に存在します。このメソッドを呼び出すと
	 * {@link #setSingleMeta(String, Object)}を呼び出して初期BPMメタ情報に値を設定した時と同等の
	 * 処理を行います。</p>
	 * @param bpm 初期BPM
	 * @throws IllegalStateException 動作モードが編集モードではない
	 * @throws IllegalArgumentException bpmが{@link BmsSpec#BPM_MIN}未満、または{@link BmsSpec#BPM_MAX}超過
	 */
	public void setInitialBpm(double bpm) {
		mMetas.setInitialBpm(bpm);
	}

	/**
	 * 最小BPMを取得します。
	 * <p>この値は参照モードの場合のみ取得可能です。</p>
	 * @return 最小BPM
	 * @throws IllegalStateException 動作モードが参照モードではない
	 */
	public double getMinBpm() {
		assertIsReferenceMode();
		return mMinBpm;
	}

	/**
	 * 最大BPMを取得します。
	 * <p>この値は参照モードの場合のみ取得可能です。</p>
	 * @return 最大BPM
	 * @throws IllegalStateException 動作モードが参照モードではない
	 */
	public double getMaxBpm() {
		assertIsReferenceMode();
		return mMaxBpm;
	}

	/**
	 * BPM変化回数を取得します。
	 * <p>この値は参照モードの場合のみ取得可能です。</p>
	 * @return BPM変化回数
	 * @throws IllegalStateException 動作モードが参照モードではない
	 */
	public long getChangeBpmCount() {
		assertIsReferenceMode();
		return mChangeBpmCount;
	}

	/**
	 * 譜面停止回数を取得します。
	 * <p>この値は参照モードの場合のみ取得可能です。</p>
	 * @return 譜面停止回数
	 * @throws IllegalStateException 動作モードが参照モードではない
	 */
	public long getStopCount() {
		assertIsReferenceMode();
		return mStopCount;
	}

	/**
	 * BMSコンテンツを編集モードに変更します。
	 * <p>このメソッドを呼び出すことでBMSコンテンツに対するデータの更新が行えるようになります。それと同時に、
	 * いくつかのメソッドの呼び出しが制限されます。</p>
	 * <p>既に編集モードになっている状態でこのメソッドを呼び出してはいけません。</p>
	 * @throws IllegalStateException 動作モードが参照モードではない
	 */
	public void beginEdit() {
		// 動作モード変更が禁止されている場合は何もしない
		if (mIsLockChangeMode) {
			return;
		}

		// 編集モードへ遷移する
		assertIsReferenceMode();
		mIsEditMode = true;

		// 編集開始を通知する
		try {
			mIsLockChangeMode = true;
			onBeginEdit();
		} catch (Throwable e) {
			throw e;
		} finally {
			mIsLockChangeMode = false;
		}
	}

	/**
	 * BMSコンテンツを参照モードに変更します。
	 * <p>このメソッドを呼び出すことでBMSコンテンツに対するデータの更新を終了します。編集モードで変更された
	 * メタ情報とノートから楽曲の時間とBPMに関する情報の再計算が行われ、呼び出し制限のかかっていたメソッドが
	 * 再び呼び出せるようになります。
	 * <p>既に参照モードになっている状態でこのメソッドを呼び出してはいけません。</p>
	 * @throws IllegalStateException 動作モードが編集モードではない
	 */
	public void endEdit() {
		// 動作モード変更が禁止されている場合は何もしない
		if (mIsLockChangeMode) {
			return;
		}

		// 時間とBPMに関する情報の再計算を行い、参照モードへ遷移する
		assertIsEditMode();
		var recalculate = mTlAccessor.recalculateTime();
		mIsEditMode = false;

		// 編集終了を通知する
		try {
			mIsLockChangeMode = true;
			onEndEdit(recalculate);
		} catch (Throwable e) {
			throw e;
		} finally {
			mIsLockChangeMode = false;
		}
	}

	/**
	 * BMSコンテンツを編集モードに変更し、指定したエディタ関数を実行します。
	 * <p>当メソッドは{@link #beginEdit()}～編集～{@link #endEdit()}の操作を簡略化するためのものです。
	 * この一連の手順をメソッド内で実行します。</p>
	 * <p>エディタ関数内で例外がスローされると、それをスローする前に自動的に{@link #endEdit()}を実行し編集を終了します。
	 * ただしError系(OutOfMemoryError等)はキャッチされません。Error系がスローされた後で{@link #endEdit()}
	 * が呼ばれても、複数の理由により処理中に再度Error系がスローされる可能性が高いためです。</p>
	 * <p>エディタ関数内で{@link #endEdit()}を呼び出すと、当メソッド終了時には呼び出されません。</p>
	 * <p>当メソッドはBMSコンテンツに対して軽微な編集を1～2個行いたい場合に利用することを推奨します。
	 * 例えば、以下のような編集を行いたい場合に有効な選択肢となります。</p>
	 * <pre>c.edit(() -&gt; c.setSingleMeta("#TITLE", "My Love Song"));</pre>
	 * @param editor 編集を行うエディタ関数
	 * @throws IllegalStateException 動作モードが参照モードではない
	 * @throws NullPointerException editorがnull
	 * @since 0.8.0
	 */
	public void edit(Runnable editor) {
		assertIsReferenceMode();
		assertArgNotNull(editor, "editor");
		var thrownError = false;
		try {
			// 編集を開始しエディタ関数を実行する
			beginEdit();
			editor.run();
		} catch (Exception e) {
			// スローされた例外は呼び出し元へ丸投げする
			throw e;
		} catch (Error e) {
			// Error系の場合も呼び出し元への丸投げを行うが、編集は終了させない
			thrownError = true;
			throw e;
		} finally {
			// 編集中の場合に限り編集を終了して処理完了とする
			// ※エディタ関数内で編集が終了されることを考慮する
			if (isEditMode() && !thrownError) { endEdit(); }
		}
	}

	/**
	 * 現在の動作モードが「参照モード」であるかどうかを返します。
	 * @return 参照モードである場合true
	 * @see #beginEdit
	 * @see #endEdit
	 */
	public boolean isReferenceMode() {
		return (mIsEditMode == false);
	}

	/**
	 * 現在の動作モードが「編集モード」であるかどうかを返します。
	 * @return 編集モードである場合true
	 * @see #beginEdit
	 * @see #endEdit
	 */
	public boolean isEditMode() {
		return (mIsEditMode == true);
	}

	/**
	 * {@link #beginEdit}が呼ばれ、動作モードが編集モードに遷移した時に呼ばれます。
	 * <p>当メソッドは、編集モードに遷移した時、継承先のクラスにて特別な処理を行う場合のトリガとして使用することを想定しています。</p>
	 * <p>当メソッドの実行中は動作モードを変更できません。{@link #beginEdit}, {@link #endEdit}を呼び出しても何も起こりません。</p>
	 */
	protected void onBeginEdit() {
		// Do nothing
	}

	/**
	 * {@link #endEdit}が呼ばれ、動作モードが参照モードに遷移した時に呼ばれます。
	 * <p>当メソッドは、参照モードに遷移した時、継承先のクラスにて特別な処理を行う場合のトリガとして使用することを想定しています。当メソッドが
	 * 呼ばれるのは、小節ごとの時間とBPMに関する情報の再計算が完了した後です。</p>
	 * <p>当メソッドの実行中は動作モードを変更できません。{@link #beginEdit}, {@link #endEdit}を呼び出しても何も起こりません。</p>
	 * @param isRecalculateTime 時間の再計算が発生したかどうか。
	 */
	protected void onEndEdit(boolean isRecalculateTime) {
		// BPM変更と譜面停止の統計情報を収集する
		collectBpmStopStatistics();
	}

	/**
	 * 小節に設定された小節データを取得します。
	 * <p>チャンネルインデックスには0を指定します。<br>
	 * それ以外の処理は{@link #getMeasureValue(int, int, int)}と同じです。</p>
	 * @param <T> 小節データのデータ型
	 * @param channel チャンネル番号
	 * @param measure 小節番号
	 * @return 小節データ
	 */
	@SuppressWarnings("unchecked")
	public <T> T getMeasureValue(int channel, int measure) {
		return (T)mTlAccessor.getValue(channel, 0, measure, null, false);
	}

	/**
	 * 小節に設定された小節データを取得します。
	 * <p>指定するチャンネルのデータ型は値型でなければなりません。</p>
	 * <p>小節にデータが設定されていない場合、指定チャンネルの初期値を返します。
	 * また、返された小節データの型は指定チャンネルのデータ型に対応したネイティブ型(実際のデータ型)になるので、ネイティブ型に
	 * キャストしてください。ネイティブ型については{@link BmsType}を参照してください。</p>
	 * @param <T> 小節データのデータ型
	 * @param channel チャンネル番号
	 * @param index チャンネルインデックス
	 * @param measure 小節番号
	 * @return 小節データ
	 * @throws IllegalArgumentException BMS仕様にないチャンネル番号を指定した
	 * @throws IndexOutOfBoundsException チャンネルインデックスがマイナス値
	 * @throws IndexOutOfBoundsException 重複不許可のチャンネルで0以外のインデックスを指定した
	 * @throws IllegalArgumentException 指定チャンネルのデータ型が値型ではない
	 * @throws IllegalArgumentException 小節番号が{@link BmsSpec#MEASURE_MIN}未満または{@link BmsSpec#MEASURE_MAX}超過
	 */
	@SuppressWarnings("unchecked")
	public <T> T getMeasureValue(int channel, int index, int measure) {
		return (T)mTlAccessor.getValue(channel, index, measure, null, false);
	}

	/**
	 * 小節に値を設定します。
	 * <p>チャンネルインデックスには0を指定します。<br>
	 * それ以外の処理は{@link #setMeasureValue(int, int, int, Object)}と同じです。</p>
	 * @param channel チャンネル番号
	 * @param measure 小節番号
	 * @param value 設定値
	 */
	public void setMeasureValue(int channel, int measure, Object value) {
		mTlAccessor.setValue(channel, 0, measure, value);
	}

	/**
	 * 小節に値を設定します。
	 * <p>設定値にnullを指定すると、その小節から値を削除します。</p>
	 * <p>複数のデータを保有可能なチャンネルに対してのみ、チャンネルインデックスに1以上の値を指定できます。
	 * 複数データに非対応のチャンネルには必ず0を指定してください。</p>
	 * <p>小節に値を設定する前に、設定値をチャンネルに規定されているデータ型に変換します。但し、チャンネルのデータ型が任意型の場合は
	 * 設定値の参照をそのまま設定します。</p>
	 * <p>当メソッドを使用して小節長を変更しようとする場合、その値はBMSライブラリで定める範囲内の値でなければなりません。
	 * 範囲外の値を指定すると例外がスローされます。</p>
	 * @param channel チャンネル番号
	 * @param index チャンネルインデックス
	 * @param measure 小節番号
	 * @param value 設定値
	 * @throws IllegalStateException 動作モードが編集モードではない
	 * @throws IllegalArgumentException BMS仕様にないチャンネル番号を指定した
	 * @throws IndexOutOfBoundsException チャンネルインデックスがマイナス値
	 * @throws IndexOutOfBoundsException データ重複不許可のチャンネルで0以外のインデックスを指定した
	 * @throws IllegalArgumentException 小節番号が{@link BmsSpec#MEASURE_MIN}未満または{@link BmsSpec#MEASURE_MAX}超過
	 * @throws IllegalArgumentException チャンネルのデータ型が値型、任意型ではない
	 * @throws ClassCastException valueをチャンネルのデータ型に変換できない
	 * @throws IllegalArgumentException 小節長に{@link BmsSpec#LENGTH_MIN}未満または{@link BmsSpec#LENGTH_MAX}超過の値を設定しようとした
	 */
	public void setMeasureValue(int channel, int index, int measure, Object value) {
		mTlAccessor.setValue(channel, index, measure, value);
	}

	/**
	 * 小節に小節データが登録されているかどうかを返します。
	 * <p>チャンネルインデックスには0を指定します。<br>
	 * それ以外の処理は{@link #containsMeasureValue(int, int, int)}と同じです。</p>
	 * @param channel チャンネル番号
	 * @param measure 小節番号
	 * @return 指定小節に小節データが存在する場合true
	 */
	public boolean containsMeasureValue(int channel, int measure) {
		return mTlAccessor.getValue(channel, 0, measure, null, true) != null;
	}

	/**
	 * 小節に小節データが登録されているかどうかを返します。
	 * <p>指定するチャンネルのデータ型は値型でなければなりません。</p>
	 * @param channel チャンネル番号
	 * @param index チャンネルインデックス
	 * @param measure 小節番号
	 * @return 指定小節に小節データが存在する場合true
	 * @throws IllegalArgumentException BMS仕様にないチャンネル番号を指定した
	 * @throws IndexOutOfBoundsException チャンネルインデックスがマイナス値
	 * @throws IndexOutOfBoundsException 重複不許可のチャンネルで0以外のインデックスを指定した
	 * @throws IllegalArgumentException 指定チャンネルのデータ型が値型ではない
	 * @throws IllegalArgumentException 小節番号が{@link BmsSpec#MEASURE_MIN}未満または{@link BmsSpec#MEASURE_MAX}超過
	 */
	public boolean containsMeasureValue(int channel, int index, int measure) {
		return mTlAccessor.getValue(channel, index, measure, null, true) != null;
	}

	/**
	 * 1個のノートを取得します。
	 * <p>チャンネルインデックスには0を指定します。<br>
	 * 引数atから小節番号・刻み位置を取り出します。<br>
	 * それ以外の処理は{@link #getNote(int, int, int, double)}と同じです。</p>
	 * @param channel チャンネル番号
	 * @param at 楽曲位置
	 * @return ノート
	 * @throws NullPointerException atがnull
	 */
	public BmsNote getNote(int channel, BmsAt at) {
		return mTlAccessor.getNote(channel, 0, at.getMeasure(), at.getTick());
	}

	/**
	 * 1個のノートを取得します。
	 * <p>引数atから小節番号・刻み位置を取り出します。<br>
	 * それ以外の処理は{@link #getNote(int, int, int, double)}と同じです。</p>
	 * @param channel チャンネル番号
	 * @param index チャンネルインデックス
	 * @param at 楽曲位置
	 * @return ノート
	 * @throws NullPointerException atがnull
	 */
	public BmsNote getNote(int channel, int index, BmsAt at) {
		return mTlAccessor.getNote(channel, index, at.getMeasure(), at.getTick());
	}

	/**
	 * 1個のノートを取得します。
	 * <p>チャンネルインデックスには0を指定します。<br>
	 * それ以外の処理は{@link #getNote(int, int, int, double)}と同じです。</p>
	 * @param channel チャンネル番号
	 * @param measure 小節番号
	 * @param tick 小節の刻み位置
	 * @return ノート
	 */
	public BmsNote getNote(int channel, int measure, double tick) {
		return mTlAccessor.getNote(channel, 0, measure, tick);
	}

	/**
	 * 1個のノートを取得します。指定位置にノートが存在しない場合はnullを返します。
	 * @param channel チャンネル番号
	 * @param index チャンネルインデックス
	 * @param measure 小節番号
	 * @param tick 小節の刻み位置
	 * @return ノート
	 * @throws IllegalArgumentException BMS仕様にないチャンネル番号を指定した
	 * @throws IndexOutOfBoundsException チャンネルインデックスにマイナス値を指定した
	 * @throws IndexOutOfBoundsException 重複不許可のチャンネルで0以外のチャンネルインデックスを指定した
	 * @throws IllegalArgumentException 小節番号が{@link BmsSpec#MEASURE_MIN}未満または{@link BmsSpec#MEASURE_MAX}超過
	 * @throws IllegalArgumentException 小節の刻み位置にマイナス値または当該小節の刻み数以上の値を指定した
	 * @throws IllegalArgumentException 指定チャンネルのデータ型が配列型ではない
	 */
	public BmsNote getNote(int channel, int index, int measure, double tick) {
		return mTlAccessor.getNote(channel, index, measure, tick);
	}

	/**
	 * 指定位置から後退方向に存在する最初のノートを取得します。
	 * <p>チャンネルインデックスには0を指定します。<br>
	 * 引数atから小節番号・刻み位置を取り出します。<br>
	 * それ以外の処理は{@link #getPreviousNote(int, int, int, double, boolean)}と同じです。</p>
	 * @param channel チャンネル番号
	 * @param at 楽曲位置
	 * @param inclusiveFrom 指定位置を検索対象に含めるかどうか
	 * @return 見つかったノート
	 * @throws NullPointerException atがnull
	 */
	public BmsNote getPreviousNote(int channel, BmsAt at, boolean inclusiveFrom) {
		return mTlAccessor.getNearerNote(channel, 0, at.getMeasure(), at.getTick(), -1, inclusiveFrom);
	}

	/**
	 * 指定位置から後退方向に存在する最初のノートを取得します。
	 * <p>引数atから小節番号・刻み位置を取り出します。<br>
	 * それ以外の処理は{@link #getPreviousNote(int, int, int, double, boolean)}と同じです。</p>
	 * @param channel チャンネル番号
	 * @param index チャンネルインデックス
	 * @param at 楽曲位置
	 * @param inclusiveFrom 指定位置を検索対象に含めるかどうか
	 * @return 見つかったノート
	 * @throws NullPointerException atがnull
	 */
	public BmsNote getPreviousNote(int channel, int index, BmsAt at, boolean inclusiveFrom) {
		return mTlAccessor.getNearerNote(channel, index, at.getMeasure(), at.getTick(), -1, inclusiveFrom);
	}

	/**
	 * 指定位置から後退方向に存在する最初のノートを取得します。
	 * <p>チャンネルインデックスには0を指定します。<br>
	 * それ以外の処理は{@link #getPreviousNote(int, int, int, double, boolean)}と同じです。</p>
	 * @param channel チャンネル番号
	 * @param measure 小節番号
	 * @param tick 小節の刻み位置
	 * @param inclusiveFrom 指定位置を検索対象に含めるかどうか
	 * @return 見つかったノート
	 */
	public BmsNote getPreviousNote(int channel, int measure, double tick, boolean inclusiveFrom) {
		return mTlAccessor.getNearerNote(channel, 0, measure, tick, -1, inclusiveFrom);
	}

	/**
	 * 指定位置から後退方向に存在する最初のノートを取得します。存在しない場合はnullを返します。
	 * @param channel チャンネル番号
	 * @param index チャンネルインデックス
	 * @param measure 小節番号
	 * @param tick 小節の刻み位置
	 * @param inclusiveFrom 指定位置を検索対象に含めるかどうか
	 * @return 見つかったノート
	 * @throws IllegalArgumentException BMS仕様にないチャンネル番号を指定した
	 * @throws IndexOutOfBoundsException チャンネルインデックスにマイナス値を指定した
	 * @throws IndexOutOfBoundsException 重複不許可のチャンネルで0以外のチャンネルインデックスを指定した
	 * @throws IllegalArgumentException 小節番号が{@link BmsSpec#MEASURE_MIN}未満または小節数以上
	 * @throws IllegalArgumentException 小節の刻み位置にマイナス値または当該小節の刻み数以上を指定した
	 * @throws IllegalArgumentException 指定チャンネルのデータ型が配列型ではない
	 */
	public BmsNote getPreviousNote(int channel, int index, int measure, double tick, boolean inclusiveFrom) {
		return mTlAccessor.getNearerNote(channel, index, measure, tick, -1, inclusiveFrom);
	}

	/**
	 * 指定位置から進行方向に存在する最初のノートを取得します。
	 * <p>チャンネルインデックスには0を指定します。<br>
	 * 引数atから小節番号・刻み位置を取り出します。<br>
	 * それ以外の処理は{@link #getNextNote(int, int, int, double, boolean)}と同じです。</p>
	 * @param channel チャンネル番号
	 * @param at 楽曲位置
	 * @param inclusiveFrom 指定位置を検索対象に含めるかどうか
	 * @return 見つかったノート
	 * @throws NullPointerException atがnull
	 */
	public BmsNote getNextNote(int channel, BmsAt at, boolean inclusiveFrom) {
		return mTlAccessor.getNearerNote(channel, 0, at.getMeasure(), at.getTick(), 1, inclusiveFrom);
	}

	/**
	 * 指定位置から進行方向に存在する最初のノートを取得します。
	 * <p>引数atから小節番号・刻み位置を取り出します。<br>
	 * それ以外の処理は{@link #getNextNote(int, int, int, double, boolean)}と同じです。</p>
	 * @param channel チャンネル番号
	 * @param index チャンネルインデックス
	 * @param at 楽曲位置
	 * @param inclusiveFrom 指定位置を検索対象に含めるかどうか
	 * @return 見つかったノート
	 * @throws NullPointerException atがnull
	 */
	public BmsNote getNextNote(int channel, int index, BmsAt at, boolean inclusiveFrom) {
		return mTlAccessor.getNearerNote(channel, index, at.getMeasure(), at.getTick(), 1, inclusiveFrom);
	}

	/**
	 * 指定位置から進行方向に存在する最初のノートを取得します。
	 * <p>チャンネルインデックスには0を指定します。<br>
	 * それ以外の処理は{@link #getNextNote(int, int, int, double, boolean)}と同じです。</p>
	 * @param channel チャンネル番号
	 * @param measure 小節番号
	 * @param tick 小節の刻み位置
	 * @param inclusiveFrom 指定位置を検索対象に含めるかどうか
	 * @return 見つかったノート
	 */
	public BmsNote getNextNote(int channel, int measure, double tick, boolean inclusiveFrom) {
		return mTlAccessor.getNearerNote(channel, 0, measure, tick, 1, inclusiveFrom);
	}

	/**
	 * 指定位置から進行方向に存在する最初のノートを取得します。存在しない場合はnullを返します。
	 * @param channel チャンネル番号
	 * @param index チャンネルインデックス
	 * @param measure 小節番号
	 * @param tick 小節の刻み位置
	 * @param inclusiveFrom 指定位置を検索対象に含めるかどうか
	 * @return 見つかったノート
	 * @throws IllegalArgumentException BMS仕様にないチャンネル番号を指定した
	 * @throws IndexOutOfBoundsException チャンネルインデックスにマイナス値を指定した
	 * @throws IndexOutOfBoundsException 重複不許可のチャンネルで0以外のチャンネルインデックスを指定した
	 * @throws IllegalArgumentException 小節番号が{@link BmsSpec#MEASURE_MIN}未満または小節数以上
	 * @throws IllegalArgumentException 小節の刻み位置にマイナス値または当該小節の刻み数以上を指定した
	 * @throws IllegalArgumentException 指定チャンネルのデータ型が配列型ではない
	 */
	public BmsNote getNextNote(int channel, int index, int measure, double tick, boolean inclusiveFrom) {
		return mTlAccessor.getNearerNote(channel, index, measure, tick, 1, inclusiveFrom);
	}

	/**
	 * 指定位置のノートの値から参照メタ情報のデータを照合し、照合結果を返します。
	 * <p>引数noteからチャンネル番号・インデックス、小節番号・刻み位置を取り出します。<br>
	 * それ以外の処理は{@link #getResolvedNoteValue(int, int, int, double)}と同じです。</p>
	 * @param note ノート
	 * @return 参照先メタ情報から取り出したデータ
	 * @throws NullPointerException noteがnull
	 */
	public Object getResolvedNoteValue(BmsNote note) {
		return mTlAccessor.getResolvedNoteValue(note);
	}

	/**
	 * 指定位置のノートの値から参照メタ情報のデータを照合し、照合結果を返します。
	 * <p>チャンネルインデックスには0を指定します<br>
	 * 引数atから小節番号・刻み位置を取り出します。<br>
	 * それ以外の処理は{@link #getResolvedNoteValue(int, int, int, double)}と同じです。</p>
	 * @param channel チャンネル番号
	 * @param at 楽曲位置
	 * @return 参照先メタ情報から取り出したデータ
	 * @throws NullPointerException atがnull
	 */
	public Object getResolvedNoteValue(int channel, BmsAt at) {
		assertArgNotNull(at, "at");
		return mTlAccessor.getResolvedNoteValue(channel, 0, at.getMeasure(), at.getTick());
	}

	/**
	 * 指定位置のノートの値から参照メタ情報のデータを照合し、照合結果を返します。
	 * <p>引数atから小節番号・刻み位置を取り出します。<br>
	 * それ以外の処理は{@link #getResolvedNoteValue(int, int, int, double)}と同じです。</p>
	 * @param channel チャンネル番号
	 * @param index チャンネルインデックス
	 * @param at 楽曲位置
	 * @return 参照先メタ情報から取り出したデータ
	 * @throws NullPointerException atがnull
	 */
	public Object getResolvedNoteValue(int channel, int index, BmsAt at) {
		assertArgNotNull(at, "at");
		return mTlAccessor.getResolvedNoteValue(channel, index, at.getMeasure(), at.getTick());
	}

	/**
	 * 指定位置のノートの値から参照メタ情報のデータを照合し、照合結果を返します。
	 * <p>チャンネルインデックスには0を指定します<br>
	 * それ以外の処理は{@link #getResolvedNoteValue(int, int, int, double)}と同じです。</p>
	 * @param channel チャンネル番号
	 * @param measure 小節番号
	 * @param tick 小節の刻み位置
	 * @return 参照先メタ情報から取り出したデータ
	 */
	public Object getResolvedNoteValue(int channel, int measure, double tick) {
		return mTlAccessor.getResolvedNoteValue(channel, 0, measure, tick);
	}

	/**
	 * 指定位置のノートの値から参照メタ情報のデータを照合し、照合結果を返します。
	 * <p>参照先メタ情報のないチャンネルを指定した場合、そのノートの値をLong型に変換して返します。</p>
	 * <p>参照先メタ情報のインデックス(=ノートの値)に割り当てられたデータが存在しない場合、そのメタ情報の
	 * 初期値を返します。</p>
	 * @param channel チャンネル番号
	 * @param index チャンネルインデックス
	 * @param measure 小節番号
	 * @param tick 小節の刻み位置
	 * @return 参照先メタ情報から取り出したデータ
	 * @throws IllegalArgumentException BMS仕様にないチャンネル番号を指定した
	 * @throws IndexOutOfBoundsException チャンネルインデックスがマイナス値
	 * @throws IndexOutOfBoundsException 重複不許可のチャンネルで0以外のチャンネルインデックスを指定した
	 * @throws IllegalArgumentException 小節番号が{@link BmsSpec#MEASURE_MIN}未満または{@link BmsSpec#MEASURE_MAX}超過
	 * @throws IllegalArgumentException マイナス値または当該小節の刻み数以上の刻み位置を指定した
	 * @throws IllegalArgumentException 指定チャンネルのデータ型が配列型ではない
	 */
	public Object getResolvedNoteValue(int channel, int index, int measure, double tick) {
		return mTlAccessor.getResolvedNoteValue(channel, index, measure, tick);
	}

	/**
	 * 条件に該当するノートを検索し、最初に見つかったノートを返します。
	 * <p>引数atFromから検索開始位置の小節番号・刻み位置を取り出します。<br>
	 * それ以外の処理は{@link #pointOf(int, double, Predicate)}と同じです。</p>
	 * @param atFrom 楽曲位置
	 * @param judge 条件一致判定を行うテスター
	 * @return 最初に条件に一致したノート。見つからなかった場合はnull。
	 * @throws NullPointerException atFromがnull
	 */
	public BmsNote pointOf(BmsAt atFrom, Predicate<BmsNote> judge) {
		return mTlAccessor.pointOf(atFrom.getMeasure(), atFrom.getTick(), judge);
	}

	/**
	 * 条件に該当するノートを検索し、最初に見つかったノートを返します。
	 * <p>検索は、原点の楽曲位置から進行方向に向かって行われます。</p>
	 * @param measureFrom 検索開始位置を示す小節番号
	 * @param tickFrom 検索開始位置を示す刻み位置
	 * @param judge 条件一致判定を行うテスター
	 * @return 最初に条件に一致したノート。見つからなかった場合はnull。
	 * @throws IllegalArgumentException 検索開始位置の小節番号にノート・小節データが存在し得ない値を指定した
	 * @throws IllegalArgumentException 検索開始位置の刻み位置にマイナス値または当該小節の刻み数以上の値を指定した
	 * @throws NullPointerException judgeがnull
	 */
	public BmsNote pointOf(int measureFrom, double tickFrom, Predicate<BmsNote> judge) {
		return mTlAccessor.pointOf(measureFrom, tickFrom, judge);
	}

	/**
	 * 指定範囲のノートを列挙します。
	 * <p>全てのノートを列挙対象とします。<br>
	 * それ以外の処理は{@link #enumNotes(int, int, int, double, int, double, Consumer)}と同じです。</p>
	 * @param enumNote ノートを通知する関数
	 * @throws NullPointerException enumNoteがnull
	 */
	public void enumNotes(Consumer<BmsNote> enumNote) {
		mTlAccessor.enumNotes(
				BmsSpec.CHANNEL_MIN, BmsSpec.CHANNEL_MAX + 1,
				0, 0,
				mTlAccessor.getCount(), 0,
				enumNote);
	}

	/**
	 * 指定楽曲位置のノートを列挙します。
	 * <p>引数atから小節番号、刻み位置を取り出します。<br>
	 * それ以外の処理は{@link #enumNotes(int, double, Consumer)}と同じです。</p>
	 * @param at 楽曲位置
	 * @param enumNote ノートを通知する関数
	 * @throws NullPointerException atがnull
	 */
	public void enumNotes(BmsAt at, Consumer<BmsNote> enumNote) {
		assertArgNotNull(at, "at");
		mTlAccessor.enumNotes(at.getMeasure(), at.getTick(), enumNote);
	}

	/**
	 * 指定楽曲位置のノートを列挙します。
	 * <p>当メソッドは指定された小節番号、刻み位置のノートをピンポイントで列挙します。楽曲位置が少しでもずれている
	 * ノートは列挙対象とはなりません。範囲列挙を行いたい場合は他のオーバーロードメソッドを使用してください。</p>
	 * @param measure 列挙対象とする小節番号
	 * @param tick 列挙対象とする刻み位置
	 * @param enumNote ノートを通知する関数
	 * @throws IllegalArgumentException measureがマイナス値または小節数以上
	 * @throws IllegalArgumentException tickがマイナス値または当該小節の刻み数以上
	 * @throws NullPointerException enumNoteがnull
	 */
	public void enumNotes(int measure, double tick, Consumer<BmsNote> enumNote) {
		mTlAccessor.enumNotes(measure, tick, enumNote);
	}

	/**
	 * 指定範囲のノートを列挙します。
	 * <p>全チャンネルを列挙対象とします。<br>
	 * それ以外の処理は{@link #enumNotes(int, int, int, double, int, double, Consumer)}と同じです。</p>
	 * @param measureBegin 列挙範囲を示す最小の小節番号
	 * @param tickBegin 列挙範囲を示す最小の小節番号の刻み位置
	 * @param measureEnd 列挙範囲を示す最大の小節番号
	 * @param tickEnd 列挙範囲を示す最大の小節番号の刻み位置(この位置を含まない)
	 * @param enumNote ノートを通知する関数
	 */
	public void enumNotes(int measureBegin, double tickBegin, int measureEnd, double tickEnd,
			Consumer<BmsNote> enumNote) {
		mTlAccessor.enumNotes(
				BmsSpec.CHANNEL_MIN, BmsSpec.CHANNEL_MAX,
				measureBegin, tickBegin,
				measureEnd, tickEnd,
				enumNote);
	}

	/**
	 * 指定範囲のノートを列挙します。
	 * <p>全チャンネルを列挙対象とします。<br>
	 * 引数atBegin, atEndから小節番号・刻み位置を取り出します。<br>
	 * それ以外の処理は{@link #enumNotes(int, int, int, double, int, double, Consumer)}と同じです。</p>
	 * @param atBegin 列挙範囲を示す最小の楽曲位置
	 * @param atEnd 列挙範囲を示す最大の楽曲位置(この位置を含まない)
	 * @param enumNote ノートを通知する関数
	 * @throws NullPointerException atBeginがnull
	 * @throws NullPointerException atEndがnull
	 */
	public void enumNotes(BmsAt atBegin, BmsAt atEnd, Consumer<BmsNote> enumNote) {
		assertArgNotNull(atBegin, "atBegin");
		assertArgNotNull(atEnd, "atEnd");
		mTlAccessor.enumNotes(
				BmsSpec.CHANNEL_MIN, BmsSpec.CHANNEL_MAX,
				atBegin.getMeasure(), atBegin.getTick(),
				atEnd.getMeasure(), atEnd.getTick(),
				enumNote);
	}

	/**
	 * 指定範囲のノートを列挙します。
	 * <p>引数atBegin, atEndから小節番号・刻み位置を取り出します。<br>
	 * それ以外の処理は{@link #enumNotes(int, int, int, double, int, double, Consumer)}と同じです。</p>
	 * @param channelBegin 列挙範囲を示す最小のチャンネル番号
	 * @param channelEnd 列挙範囲を示す最大のチャンネル番号(この番号を含まない)
	 * @param atBegin 列挙範囲を示す最小の楽曲位置
	 * @param atEnd 列挙範囲を示す最大の楽曲位置(この位置を含まない)
	 * @param enumNote ノートを通知する関数
	 * @throws NullPointerException atBeginがnull
	 * @throws NullPointerException atEndがnull
	 */
	public void enumNotes(int channelBegin, int channelEnd, BmsAt atBegin, BmsAt atEnd,
			Consumer<BmsNote> enumNote) {
		assertArgNotNull(atBegin, "atBegin");
		assertArgNotNull(atEnd, "atEnd");
		mTlAccessor.enumNotes(
				channelBegin, channelEnd,
				atBegin.getMeasure(), atBegin.getTick(),
				atEnd.getMeasure(), atEnd.getTick(),
				enumNote);
	}

	/**
	 * 指定範囲のノートを列挙します。
	 * <p>見つかったノートは指定の関数に通知されます。</p>
	 * <p>小節数が0(ノートが全く追加されていない)のコンテンツに対して当メソッドを呼び出すと何も行われません。</p>
	 * @param channelBegin 列挙範囲を示す最小のチャンネル番号
	 * @param channelEnd 列挙範囲を示す最大のチャンネル番号(この番号を含まない)
	 * @param measureBegin 列挙範囲を示す最小の小節番号
	 * @param tickBegin 列挙範囲を示す最小の小節番号の刻み位置
	 * @param measureEnd 列挙範囲を示す最大の小節番号
	 * @param tickEnd 列挙範囲を示す最大の小節番号の刻み位置(この位置を含まない)
	 * @param enumNote ノートを通知する関数
	 * @throws IllegalArgumentException channelBeginに小節データ・ノートを登録できないチャンネル番号を指定した
	 * @throws IllegalArgumentException channelEndに{@link BmsSpec#CHANNEL_MAX}を超える値を指定した
	 * @throws IllegalArgumentException measureBeginに{@link BmsSpec#MEASURE_MIN}未満または{@link BmsSpec#MEASURE_MAX}超過の値を指定した
	 * @throws IllegalArgumentException tickBeginにマイナス値または当該小節の刻み数以上の値を指定した
	 * @throws IllegalArgumentException measureEndに{@link BmsSpec#MEASURE_MIN}未満または{@link BmsSpec#MEASURE_MAX}超過の値を指定した
	 * @throws IllegalArgumentException tickEndにマイナス値、当該小節の刻み数以上、または小節番号==小節数の時に0以外の値を指定した
	 * @throws NullPointerException enumNoteがnull
	 */
	public void enumNotes(int channelBegin, int channelEnd,
			int measureBegin, double tickBegin, int measureEnd, double tickEnd, Consumer<BmsNote> enumNote) {
		mTlAccessor.enumNotes(
				channelBegin, channelEnd,
				measureBegin, tickBegin,
				measureEnd, tickEnd,
				enumNote);
	}

	/**
	 * 複数のノートをリスト形式で取得します。
	 * <p>全てのノートを取得対象とします。<br>
	 * それ以外の処理は{@link #listNotes(int, int, int, double, int, double, Predicate)}と同じです。</p>
	 * @param isCollect 取得有無を決定するテスター
	 * @return ノートのリスト
	 */
	public List<BmsNote> listNotes(Predicate<BmsNote> isCollect) {
		assertArgNotNull(isCollect, "isCollect");
		var measureCount = mTlAccessor.getCount();
		return (measureCount == 0) ? Collections.emptyList() : mTlAccessor.listNotes(
				BmsSpec.CHANNEL_MIN, BmsSpec.CHANNEL_MAX + 1,
				0, 0,
				measureCount, 0,
				isCollect);
	}

	/**
	 * 複数のノートをリスト形式で取得します。
	 * <p>引数atから小節番号、刻み位置を取り出します。<br>
	 * それ以外の処理は{@link #listNotes(int, double, Predicate)}と同じです。</p>
	 * @param at 楽曲位置
	 * @param isCollect 取得有無を決定するテスター
	 * @return ノートのリスト
	 * @throws NullPointerException atがnull
	 */
	public List<BmsNote> listNotes(BmsAt at, Predicate<BmsNote> isCollect) {
		assertArgNotNull(at, "at");
		return mTlAccessor.listNotes(at.getMeasure(), at.getTick(), isCollect);
	}

	/**
	 * 複数のノートをリスト形式で取得します。
	 * <p>当メソッドは指定された小節番号、刻み位置のノートをピンポイントで取得します。楽曲位置が少しでもずれている
	 * ノートは取得対象とはなりません。範囲取得を行いたい場合は他のオーバーロードメソッドを使用してください。</p>
	 * <p>ノートは指定のテスターに通知されます。テスターの戻り値にfalseを指定すると取得対象から除外できます。</p>
	 * @param measure 取得対象とする小節番号
	 * @param tick 取得対象とする刻み位置
	 * @param isCollect 取得有無を決定するテスター
	 * @return ノートのリスト
	 * @throws IllegalArgumentException measureがマイナス値または小節数以上
	 * @throws IllegalArgumentException tickがマイナス値または当該小節の刻み数以上
	 * @throws NullPointerException isCollectがnull
	 */
	public List<BmsNote> listNotes(int measure, double tick, Predicate<BmsNote> isCollect) {
		return mTlAccessor.listNotes(measure, tick, isCollect);
	}

	/**
	 * 複数のノートをリスト形式で取得します。
	 * <p>全チャンネルを取得対象とします。<br>
	 * 引数atBegin, atEndから小節番号・刻み位置を取り出します。<br>
	 * それ以外の処理は{@link #listNotes(int, int, int, double, int, double, Predicate)}と同じです。</p>
	 * @param atBegin 取得対象範囲を示す最小の楽曲位置
	 * @param atEnd 取得対象範囲を示す最大の楽曲位置(この位置を含まない)
	 * @param isCollect 取得有無を決定するテスター
	 * @return ノートのリスト
	 * @throws NullPointerException atBeginがnull
	 * @throws NullPointerException atEndがnull
	 */
	public List<BmsNote> listNotes(BmsAt atBegin, BmsAt atEnd, Predicate<BmsNote> isCollect) {
		return mTlAccessor.listNotes(
				BmsSpec.CHANNEL_MIN, BmsSpec.CHANNEL_MAX,
				atBegin.getMeasure(), atBegin.getTick(),
				atEnd.getMeasure(), atEnd.getTick(),
				isCollect);
	}

	/**
	 * 複数のノートをリスト形式で取得します。
	 * <p>引数atBegin, atEndから小節番号・刻み位置を取り出します。<br>
	 * それ以外の処理は{@link #listNotes(int, int, int, double, int, double, Predicate)}と同じです。</p>
	 * @param channelBegin 取得対象範囲を示す最小のチャンネル番号
	 * @param channelEnd 取得対象範囲を示す最大のチャンネル番号(この番号を含まない)
	 * @param atBegin 取得対象範囲を示す最小の楽曲位置
	 * @param atEnd 取得対象範囲を示す最大の楽曲位置(この位置を含まない)
	 * @param isCollect 取得有無を決定するテスター
	 * @return ノートのリスト
	 * @throws NullPointerException atBeginがnull
	 * @throws NullPointerException atEndがnull
	 */
	public List<BmsNote> listNotes(int channelBegin, int channelEnd, BmsAt atBegin, BmsAt atEnd,
			Predicate<BmsNote> isCollect) {
		return mTlAccessor.listNotes(
				channelBegin, channelEnd,
				atBegin.getMeasure(), atBegin.getTick(),
				atEnd.getMeasure(), atEnd.getTick(),
				isCollect);
	}

	/**
	 * 複数のノートをリスト形式で取得します。
	 * <p>全チャンネルを取得対象とします。<br>
	 * それ以外の処理は{@link #listNotes(int, int, int, double, int, double, Predicate)}と同じです。</p>
	 * @param measureBegin 取得対象範囲を示す最小の小節番号
	 * @param tickBegin 取得対象範囲を示す最小の小節番号の刻み位置
	 * @param measureEnd 取得対象範囲を示す最大の小節番号
	 * @param tickEnd 取得対象範囲を示す最大の小節番号の刻み位置(この位置を含まない)
	 * @param isCollect 取得有無を決定するテスター
	 * @return ノートのリスト
	 */
	public List<BmsNote> listNotes(int measureBegin, double tickBegin, int measureEnd, double tickEnd,
			Predicate<BmsNote> isCollect) {
		return mTlAccessor.listNotes(
				BmsSpec.CHANNEL_MIN, BmsSpec.CHANNEL_MAX,
				measureBegin, tickBegin,
				measureEnd, tickEnd,
				isCollect);
	}

	/**
	 * 複数のノートをリスト形式で取得します。
	 * <p>ノートは指定のテスターに通知されます。テスターの戻り値にfalseを指定すると取得対象から除外できます。</p>
	 * @param channelBegin 取得対象範囲を示す最小のチャンネル番号
	 * @param channelEnd 取得対象範囲を示す最大のチャンネル番号(この番号を含まない)
	 * @param measureBegin 取得対象範囲を示す最小の小節番号
	 * @param tickBegin 取得対象範囲を示す最小の小節番号の刻み位置
	 * @param measureEnd 取得対象範囲を示す最大の小節番号
	 * @param tickEnd 取得対象範囲を示す最大の小節番号の刻み位置(この位置を含まない)
	 * @param isCollect 取得有無を決定するテスター
	 * @return ノートのリスト
	 * @throws IllegalArgumentException channelBeginに{@link BmsSpec#CHANNEL_MIN}未満または{@link BmsSpec#CHANNEL_MAX}を超える値を指定した
	 * @throws IllegalArgumentException channelEndに{@link BmsSpec#CHANNEL_MIN}未満または{@link BmsSpec#CHANNEL_MAX}+1を超える値を指定した
	 * @throws IllegalArgumentException measureBeginに{@link BmsSpec#MEASURE_MIN}未満または{@link BmsSpec#MEASURE_MAX}超過の値を指定した
	 * @throws IllegalArgumentException tickBeginにマイナス値または当該小節の刻み数以上の値を指定した
	 * @throws IllegalArgumentException measureEndに{@link BmsSpec#MEASURE_MIN}未満または{@link BmsSpec#MEASURE_MAX}超過の値を指定した
	 * @throws IllegalArgumentException tickEndにマイナス値、当該小節の刻み数以上、または小節番号==小節数の時に0以外の値を指定した
	 * @throws NullPointerException isCollectがnull
	 */
	public List<BmsNote> listNotes(int channelBegin, int channelEnd,
			int measureBegin, double tickBegin, int measureEnd, double tickEnd, Predicate<BmsNote> isCollect) {
		return mTlAccessor.listNotes(
				channelBegin, channelEnd,
				measureBegin, tickBegin,
				measureEnd, tickEnd,
				isCollect);
	}

	/**
	 * 条件に一致するノートの数を数えます。
	 * <p>全ノートをカウント対象にします。<br>
	 * それ以外の処理は{@link #countNotes(int, int, int, double, int, double, Predicate)}と同じです。</p>
	 * @param isCounting カウント有無を決定するテスター
	 * @return 条件に一致したノートの数
	 */
	public int countNotes(Predicate<BmsNote> isCounting) {
		assertArgNotNull(isCounting, "isCounting");
		var measureCount = mTlAccessor.getCount();
		return (measureCount == 0) ? 0 : mTlAccessor.countNotes(
				BmsSpec.CHANNEL_MIN, BmsSpec.CHANNEL_MAX,
				0, 0,
				measureCount, 0,
				isCounting);
	}

	/**
	 * 指定楽曲位置のノートの数を数えます。
	 * <p>引数atから小節番号、刻み位置を取り出します。<br>
	 * それ以外の処理は{@link #countNotes(int, double, Predicate)}と同じです。</p>
	 * @param at 楽曲位置
	 * @param isCounting カウント有無を決定するテスター
	 * @return 条件に一致したノートの数
	 * @throws NullPointerException atがnull
	 */
	public int countNotes(BmsAt at, Predicate<BmsNote> isCounting) {
		assertArgNotNull(at, "at");
		return mTlAccessor.countNotes(at.getMeasure(), at.getTick(), isCounting);
	}

	/**
	 * 指定楽曲位置のノートの数を数えます。
	 * <p>ノートは指定のテスターに通知されます。テスターでtrueを返すとそのノートがカウント対象になります。</p>
	 * @param measure カウント対象とする小節番号
	 * @param tick カウント対象とする刻み位置
	 * @param isCounting カウント有無を決定するテスター
	 * @return 条件に一致したノートの数
	 * @throws IllegalArgumentException measureがマイナス値または小節数以上
	 * @throws IllegalArgumentException tickがマイナス値または当該小節の刻み数以上
	 * @throws NullPointerException isCountingがnull
	 */
	public int countNotes(int measure, double tick, Predicate<BmsNote> isCounting) {
		return mTlAccessor.countNotes(measure, tick, isCounting);
	}

	/**
	 * 条件に一致するノートの数を数えます。
	 * <p>全チャンネルをカウント対象にします。<br>
	 * 引数atBegin, atEndから小節番号・刻み位置を取り出します。<br>
	 * それ以外の処理は{@link #countNotes(int, int, int, double, int, double, Predicate)}と同じです。</p>
	 * @param atBegin カウント範囲を示す最小の楽曲位置
	 * @param atEnd カウント範囲を示す最大の楽曲位置(この位置を含まない)
	 * @param isCounting カウント有無を決定するテスター
	 * @return 条件に一致したノートの数
	 * @throws NullPointerException atBeginがnull
	 * @throws NullPointerException atEndがnull
	 */
	public int countNotes(BmsAt atBegin, BmsAt atEnd, Predicate<BmsNote> isCounting) {
		return mTlAccessor.countNotes(
				BmsSpec.CHANNEL_MIN, BmsSpec.CHANNEL_MAX,
				atBegin.getMeasure(), atBegin.getTick(),
				atEnd.getMeasure(), atEnd.getTick(),
				isCounting);
	}

	/**
	 * 条件に一致するノートの数を数えます。
	 * <p>引数atBegin, atEndから小節番号・刻み位置を取り出します。<br>
	 * それ以外の処理は{@link #countNotes(int, int, int, double, int, double, Predicate)}と同じです。</p>
	 * @param channelBegin カウント範囲を示す最小のチャンネル番号
	 * @param channelEnd カウント範囲を示す最大のチャンネル番号(この番号を含まない)
	 * @param atBegin カウント範囲を示す最小の楽曲位置
	 * @param atEnd カウント範囲を示す最大の楽曲位置(この位置を含まない)
	 * @param isCounting カウント有無を決定するテスター
	 * @return 条件に一致したノートの数
	 * @throws NullPointerException atBeginがnull
	 * @throws NullPointerException atEndがnull
	 */
	public int countNotes(int channelBegin, int channelEnd, BmsAt atBegin, BmsAt atEnd,
			Predicate<BmsNote> isCounting) {
		return mTlAccessor.countNotes(
				channelBegin, channelEnd,
				atBegin.getMeasure(), atBegin.getTick(),
				atEnd.getMeasure(), atEnd.getTick(),
				isCounting);
	}

	/**
	 * 条件に一致するノートの数を数えます。
	 * <p>全チャンネルをカウント対象にします。<br>
	 * それ以外の処理は{@link #countNotes(int, int, int, double, int, double, Predicate)}と同じです。</p>
	 * @param measureBegin カウント範囲を示す最小の小節番号
	 * @param tickBegin カウント範囲を示す最小の小節番号の刻み位置
	 * @param measureEnd カウント範囲を示す最大の小節番号
	 * @param tickEnd カウント範囲を示す最大の小節番号の刻み位置(この位置を含まない)
	 * @param isCounting カウント有無を決定するテスター
	 * @return 条件に一致したノートの数
	 */
	public int countNotes(int measureBegin, double tickBegin, int measureEnd, double tickEnd,
			Predicate<BmsNote> isCounting) {
		return mTlAccessor.countNotes(
				BmsSpec.CHANNEL_MIN, BmsSpec.CHANNEL_MAX,
				measureBegin, tickBegin,
				measureEnd, tickEnd,
				isCounting);
	}

	/**
	 * 条件に一致するノートの数を数えます。
	 * <p>ノートは指定のテスターに通知されます。テスターでtrueを返すとそのノートがカウント対象になります。</p>
	 * @param channelBegin カウント範囲を示す最小のチャンネル番号
	 * @param channelEnd カウント範囲を示す最大のチャンネル番号(この番号を含まない)
	 * @param measureBegin カウント範囲を示す最小の小節番号
	 * @param tickBegin カウント範囲を示す最小の小節番号の刻み位置
	 * @param measureEnd カウント範囲を示す最大の小節番号
	 * @param tickEnd カウント範囲を示す最大の小節番号の刻み位置(この位置を含まない)
	 * @param isCounting カウント有無を決定するテスター
	 * @return 条件に一致したノートの数
	 * @throws IllegalArgumentException channelBeginに{@link BmsSpec#CHANNEL_MIN}未満または{@link BmsSpec#CHANNEL_MAX}を超える値を指定した
	 * @throws IllegalArgumentException channelEndに{@link BmsSpec#CHANNEL_MIN}未満または{@link BmsSpec#CHANNEL_MAX}+1を超える値を指定した
	 * @throws IllegalArgumentException measureBeginに{@link BmsSpec#MEASURE_MIN}未満または{@link BmsSpec#MEASURE_MAX}超過の値を指定した
	 * @throws IllegalArgumentException tickBeginにマイナス値または当該小節の刻み数以上の値を指定した
	 * @throws IllegalArgumentException measureEndに{@link BmsSpec#MEASURE_MIN}未満または{@link BmsSpec#MEASURE_MAX}超過の値を指定した
	 * @throws IllegalArgumentException tickEndにマイナス値、当該小節の刻み数以上、または小節番号==小節数の時に0以外の値を指定した
	 * @throws NullPointerException isCountingがnull
	 */
	public int countNotes(int channelBegin, int channelEnd,
			int measureBegin, double tickBegin, int measureEnd, double tickEnd, Predicate<BmsNote> isCounting) {
		return mTlAccessor.countNotes(
				channelBegin, channelEnd,
				measureBegin, tickBegin,
				measureEnd, tickEnd,
				isCounting);
	}

	/**
	 * 譜面に新しいノートを追加します。
	 * <p>チャンネルインデックスは0固定です。<br>
	 * ノートオブジェクトはBmsNoteを使用します。<br>
	 * それ以外の処理は{@link #putNote(int, int, int, double, int, Supplier)}と同じです。 </p>
	 * @param channel チャンネル番号
	 * @param measure 小節番号
	 * @param tick 小節の刻み位置
	 * @param value ノートの値
	 * @return 譜面に追加された新しいノートオブジェクト
	 * @see #putNote(int, int, int, double, int, Supplier)
	 */
	public BmsNote putNote(int channel, int measure, double tick, int value) {
		return mTlAccessor.putNote(channel, 0, measure, tick, value, BmsNote.DEFAULT_CREATOR);
	}

	/**
	 * 譜面に新しいノートを追加します。
	 * <p>チャンネルインデックスは0固定です。<br>
	 * 引数atから小節番号・小節の刻み位置を取り出します。<br>
	 * ノートオブジェクトはBmsNoteを使用します。<br>
	 * それ以外の処理は{@link #putNote(int, int, int, double, int, Supplier)}と同じです。 </p>
	 * @param channel チャンネル番号
	 * @param at 楽曲位置
	 * @param value ノートの値
	 * @return 譜面に追加された新しいノートオブジェクト
	 * @see #putNote(int, int, int, double, int, Supplier)
	 */
	public BmsNote putNote(int channel, BmsAt at, int value) {
		return mTlAccessor.putNote(channel, 0, at.getMeasure(), at.getTick(), value, BmsNote.DEFAULT_CREATOR);
	}

	/**
	 * 譜面に新しいノートを追加します。
	 * <p>ノートオブジェクトはBmsNoteを使用します。<br>
	 * それ以外の処理は{@link #putNote(int, int, int, double, int, Supplier)}と同じです。 </p>
	 * @param channel チャンネル番号
	 * @param index チャンネルインデックス
	 * @param measure 小節番号
	 * @param tick 小節の刻み位置
	 * @param value ノートの値
	 * @return 譜面に追加された新しいノートオブジェクト
	 * @see #putNote(int, int, int, double, int, Supplier)
	 */
	public BmsNote putNote(int channel, int index, int measure, double tick, int value) {
		return mTlAccessor.putNote(channel, index, measure, tick, value, BmsNote.DEFAULT_CREATOR);
	}

	/**
	 * 譜面に新しいノートを追加します。
	 * <p>引数atから小節番号・小節の刻み位置を取り出します。<br>
	 * ノートオブジェクトはBmsNoteを使用します。<br>
	 * それ以外の処理は{@link #putNote(int, int, int, double, int, Supplier)}と同じです。 </p>
	 * @param channel チャンネル番号
	 * @param index チャンネルインデックス
	 * @param at 楽曲位置
	 * @param value ノートの値
	 * @return 譜面に追加された新しいノートオブジェクト
	 * @see #putNote(int, int, int, double, int, Supplier)
	 */
	public BmsNote putNote(int channel, int index, BmsAt at, int value) {
		return mTlAccessor.putNote(channel, index, at.getMeasure(), at.getTick(), value, BmsNote.DEFAULT_CREATOR);
	}

	/**
	 * 譜面に新しいノートを追加します。
	 * <p>チャンネルインデックスは0固定です。<br>
	 * それ以外の処理は{@link #putNote(int, int, int, double, int, Supplier)}と同じです。 </p>
	 * @param <T> 追加するノートのデータ型
	 * @param channel チャンネル番号
	 * @param measure 小節番号
	 * @param tick 小節の刻み位置
	 * @param value ノートの値
	 * @param createNote ノートオブジェクトを生成する関数
	 * @return 譜面に追加された新しいノートオブジェクト
	 * @see #putNote(int, int, int, double, int, Supplier)
	 */
	public <T extends BmsNote> T putNote(int channel, int measure, double tick, int value,
			Supplier<BmsNote> createNote) {
		return mTlAccessor.putNote(channel, 0, measure, tick, value, createNote);
	}

	/**
	 * 譜面に新しいノートを追加します。
	 * <p>チャンネルインデックスは0固定です。<br>
	 * 引数atから小節番号・小節の刻み位置を取り出します。<br>
	 * それ以外の処理は{@link #putNote(int, int, int, double, int, Supplier)}と同じです。 </p>
	 * @param <T> 追加するノートのデータ型
	 * @param channel チャンネル番号
	 * @param at 楽曲位置
	 * @param value ノートの値
	 * @param createNote ノートオブジェクトを生成する関数
	 * @return 譜面に追加された新しいノートオブジェクト
	 * @see #putNote(int, int, int, double, int, Supplier)
	 */
	public <T extends BmsNote> T putNote(int channel, BmsAt at, int value, Supplier<BmsNote> createNote) {
		return mTlAccessor.putNote(channel, 0, at.getMeasure(), at.getTick(), value, createNote);
	}

	/**
	 * 譜面に新しいノートを追加します。
	 * <p>引数atから小節番号・小節の刻み位置を取り出します。<br>
	 * それ以外の処理は{@link #putNote(int, int, int, double, int, Supplier)}と同じです。 </p>
	 * @param <T> 追加するノートのデータ型
	 * @param channel チャンネル番号
	 * @param index チャンネルインデックス
	 * @param at 楽曲位置
	 * @param value ノートの値
	 * @param createNote ノートオブジェクトを生成する関数
	 * @return 譜面に追加された新しいノートオブジェクト
	 * @see #putNote(int, int, int, double, int, Supplier)
	 */
	public <T extends BmsNote> T putNote(int channel, int index, BmsAt at, int value, Supplier<BmsNote> createNote) {
		return mTlAccessor.putNote(channel, index, at.getMeasure(), at.getTick(), value, createNote);
	}

	/**
	 * 譜面に新しいノートを追加します。
	 * <p>指定した場所に既にノートが存在する場合は新しいノートで上書きします。</p>
	 * <p>ノートを追加できるチャンネルは、データ型が配列型であるチャンネルのみです。</p>
	 * <p>複数のデータを保有可能なチャンネルに対してのみ、チャンネルインデックスに1以上の値を指定できます。
	 * 複数データに非対応のチャンネルには必ず0を指定してください。</p>
	 * <p>ノートに拡張データを持たせる場合は、createNote関数からBmsNoteを継承したオブジェクトを返し、
	 * そのオブジェクトに対して拡張データを設定してください。オブジェクトのインスタンスはBMSコンテンツ内部でも
	 * 管理されるようになります。</p>
	 * <p>ノートに設定可能な値の範囲は{@link BmsSpec#VALUE_MIN}～{@link BmsSpec#VALUE_MAX}になります。
	 * これは、チャンネルのデータ型が{@link BmsType#ARRAY16}・{@link BmsType#ARRAY36}のどちらであっても同様です。
	 * ただし、それぞれのデータ型で以下の範囲外の値を持つノートが1個でも存在すると、
	 * BMSコンテンツを標準フォーマットで出力できなくなります。</p>
	 * <ul>
	 * <li>{@link BmsType#ARRAY16}の場合: {@link BmsSpec#VALUE_16_MIN}～{@link BmsSpec#VALUE_16_MAX}</li>
	 * <li>{@link BmsType#ARRAY36}の場合: {@link BmsSpec#VALUE_36_MIN}～{@link BmsSpec#VALUE_36_MAX}</li>
	 * </ul>
	 * <p>また、ノートの値に0を指定することはできません。値0は「ノートとして存在しない」ことを示す特殊な値として扱われます。
	 * そのためBMSライブラリでは、明確に存在するノートに対して値0を設定することを許可していません。</p>
	 * @param <T> 追加するノートのデータ型
	 * @param channel チャンネル番号
	 * @param index チャンネルインデックス
	 * @param measure 小節番号
	 * @param tick 小節の刻み位置
	 * @param value ノートの値
	 * @param createNote ノートオブジェクトを生成する関数
	 * @return 譜面に追加された新しいノートオブジェクト
	 * @throws IllegalStateException 動作モードが編集モードではない
	 * @throws IllegalArgumentException BMS仕様にないチャンネル番号を指定した
	 * @throws IndexOutOfBoundsException チャンネルインデックスがマイナス値
	 * @throws IndexOutOfBoundsException 重複不許可のチャンネルで0以外のチャンネルインデックスを指定した
	 * @throws IllegalArgumentException 小節番号が{@link BmsSpec#MEASURE_MIN}未満または{@link BmsSpec#MEASURE_MAX}超過
	 * @throws IllegalArgumentException 小節の刻み位置がマイナス値または当該小節の刻み数以上
	 * @throws IllegalArgumentException 指定チャンネルのデータ型が配列型ではない
	 * @throws IllegalArgumentException ノートの値に0を指定した
	 * @throws NullPointerException createNoteがnull
	 * @throws IllegalArgumentException createNoteの結果がnull
	 */
	public <T extends BmsNote> T putNote(int channel, int index, int measure, double tick, int value,
			Supplier<BmsNote> createNote) {
		return mTlAccessor.putNote(channel, index, measure, tick, value, createNote);
	}

	/**
	 * ノートを消去します。
	 * <p>チャンネルインデックスには0を指定します。<br>
	 * それ以外の処理は{@link #removeNote(int, int, int, double)}と同じです。</p>
	 * @param channel チャンネル番号
	 * @param measure 小節番号
	 * @param tick 小節の刻み位置
	 * @return 指定位置にノートが存在し消去した場合はtrue、それ以外はfalse。
	 */
	public boolean removeNote(int channel, int measure, double tick) {
		return mTlAccessor.removeNote(channel, 0, measure, tick);
	}

	/**
	 * ノートを消去します。
	 * <p>チャンネルインデックスには0を指定します。<br>
	 * 引数atから小節番号・小節の刻み位置を取り出します。<br>
	 * それ以外の処理は{@link #removeNote(int, int, int, double)}と同じです。</p>
	 * @param channel チャンネル番号
	 * @param at 楽曲位置
	 * @return 指定位置にノートが存在し消去した場合はtrue、それ以外はfalse。
	 * @throws NullPointerException atがnull
	 */
	public boolean removeNote(int channel, BmsAt at) {
		return mTlAccessor.removeNote(channel, 0, at.getMeasure(), at.getTick());
	}

	/**
	 * ノートを消去します。
	 * <p>引数atから小節番号・小節の刻み位置を取り出します。<br>
	 * それ以外の処理は{@link #removeNote(int, int, int, double)}と同じです。</p>
	 * @param channel チャンネル番号
	 * @param index チャンネルインデックス
	 * @param at 楽曲位置
	 * @return 指定位置にノートが存在し消去した場合はtrue、それ以外はfalse。
	 * @throws NullPointerException atがnull
	 */
	public boolean removeNote(int channel, int index, BmsAt at) {
		return mTlAccessor.removeNote(channel, index, at.getMeasure(), at.getTick());
	}

	/**
	 * ノートを消去します。
	 * <p>指定できるチャンネルは、データ型が配列型であるチャンネルのみです。</p>
	 * <p>複数のデータを保有可能なチャンネルに対してのみ、チャンネルインデックスに1以上の値を指定できます。
	 * 複数データに非対応のチャンネルには必ず0を指定してください。</p>
	 * @param channel チャンネル番号
	 * @param index チャンネルインデックス
	 * @param measure 小節番号
	 * @param tick 小節の刻み位置
	 * @return 指定位置にノートが存在し消去した場合はtrue、それ以外はfalse。
	 * @throws IllegalStateException 動作モードが編集モードではない
	 * @throws IllegalArgumentException BMS仕様にないチャンネル番号を指定した
	 * @throws IndexOutOfBoundsException チャンネルインデックスがマイナス値
	 * @throws IndexOutOfBoundsException 重複不許可のチャンネルで0以外のインデックスを指定した
	 * @throws IllegalArgumentException 小節番号が{@link BmsSpec#MEASURE_MIN}未満または{@link BmsSpec#MEASURE_MAX}超過
	 * @throws IllegalArgumentException 小節の刻み位置がマイナス値または当該小節の刻み数以上
	 * @throws IllegalArgumentException 指定チャンネルのデータ型が配列型ではない
	 */
	public boolean removeNote(int channel, int index, int measure, double tick) {
		return mTlAccessor.removeNote(channel, index, measure, tick);
	}

	/**
	 * 選択されたノートを消去します。
	 * @param isRemoveTarget 消去対象ノートを選択するテスター
	 * @return 消去されたノートの個数
	 * @throws IllegalStateException 動作モードが編集モードではない
	 * @throws NullPointerException isRemoveTargetがnull
	 */
	public int removeNote(Predicate<BmsNote> isRemoveTarget) {
		return mTlAccessor.removeNote(1, BmsSpec.CHANNEL_MAX + 1, 0, mTlAccessor.getCount(), isRemoveTarget);
	}

	/**
	 * 指定範囲の選択されたノートを消去します。
	 * <p>チャンネルの選択範囲は全チャンネルになります。<br>
	 * それ以外の処理は{@link #removeNote(int, int, int, int, Predicate)}と同じです。</p>
	 * @param measureBegin 消去対象の最小小節番号
	 * @param measureEnd 消去対象の最大小節番号(この小節を含まない)
	 * @param isRemoveTarget 指定範囲で消去対象を選択するテスター
	 * @return 消去されたノートの個数
	 */
	public int removeNote(int measureBegin, int measureEnd, Predicate<BmsNote> isRemoveTarget) {
		return mTlAccessor.removeNote(BmsSpec.CHANNEL_MIN, BmsSpec.CHANNEL_MAX, measureBegin, measureEnd, isRemoveTarget);
	}

	/**
	 * 指定範囲の選択されたノートを消去します。
	 * @param channelBegin 消去対象の最小チャンネル番号
	 * @param channelEnd 消去対象の最大チャンネル番号(この番号を含まない)
	 * @param measureBegin 消去対象の最小小節番号
	 * @param measureEnd 消去対象の最大小節番号(この小節を含まない)
	 * @param isRemoveTarget 指定範囲で消去対象を選択するテスター
	 * @return 消去されたノートの個数
	 * @throws IllegalStateException 動作モードが編集モードではない
	 * @throws IllegalArgumentException channelBeginに小節データ・ノートを登録できないチャンネル番号を指定した
	 * @throws IllegalArgumentException channelEndに{@link BmsSpec#CHANNEL_MAX}より大きいチャンネル番号を指定した
	 * @throws IllegalArgumentException measureBeginにノート・小節データの存在し得ない小節番号を指定した
	 * @throws IllegalArgumentException measureEndに現在の小節数より大きい小節番号を指定した
	 * @throws NullPointerException isRemoveTargetがnull
	 */
	public int removeNote(int channelBegin, int channelEnd, int measureBegin, int measureEnd,
			Predicate<BmsNote> isRemoveTarget) {
		return mTlAccessor.removeNote(channelBegin, channelEnd, measureBegin, measureEnd, isRemoveTarget);
	}

	/**
	 * 楽曲の小節数を取得します。
	 * @return 小節数
	 */
	public int getMeasureCount() {
		return mTlAccessor.getCount();
	}

	/**
	 * 小節の刻み数を取得します。
	 * <p>小節番号には{@link BmsSpec#MEASURE_MIN}～{@link BmsSpec#MEASURE_MAX}を指定することができます。</p>
	 * <p>小節の刻み数は、{@link BmsSpec#TICK_COUNT_DEFAULT}にその小節の小節長を乗算した値となります。
	 * 小節長に極端に小さい値を設定すると計算上の小節の刻み数が1未満になることがあります。</p>
	 * @param measure 小節番号
	 * @return 小節の刻み数
	 * @throws IllegalArgumentException 小節番号が{@link BmsSpec#MEASURE_MIN}未満または{@link BmsSpec#MEASURE_MAX}超過
	 */
	public double getMeasureTickCount(int measure) {
		return mTlAccessor.getTickCount(measure);
	}

	/**
	 * 指定小節に格納されたデータの数を取得します。
	 * <p>小節番号には{@link BmsSpec#MEASURE_MIN}～{@link BmsSpec#MEASURE_MAX}を指定することができます。</p>
	 * <p>データは通常1個まで格納可能ですが、重複可能チャンネルにすることで小節に複数のデータを
	 * 格納することができるようになります。</p>
	 * @param channel チャンネル番号
	 * @param measure 小節番号
	 * @return データの数
	 * @throws IllegalArgumentException 小節データ・ノートを登録できないチャンネル番号を指定した
	 * @throws IllegalArgumentException 小節番号が{@link BmsSpec#MEASURE_MIN}未満または{@link BmsSpec#MEASURE_MAX}超過
	 */
	public int getChannelDataCount(int channel, int measure) {
		return mTlAccessor.getChannelDataCount(channel, measure);
	}

	/**
	 * 指定位置に小節を挿入します。
	 * <p>挿入位置に1個の小節を挿入します。<br>
	 * それ以外の処理は{@link #insertMeasure(int, int)}と同じです。</p>
	 * @param measureWhere 挿入位置の小節番号
	 */
	public void insertMeasure(int measureWhere) {
		mTlAccessor.insert(measureWhere, 1);
	}

	/**
	 * 指定位置に小節を挿入します。
	 * <p>小節を挿入すると、挿入位置よりも後ろの小節データは後ろにずれます。</p>
	 * <p>挿入された小節は、小節データが未設定でノートがない状態になります。また、小節長は4/4拍子になります。</p>
	 * <p>挿入の結果、小節の数が{@link BmsSpec#MEASURE_MAX_COUNT}を超えないように注意してください。</p>
	 * @param measureWhere 挿入位置の小節番号
	 * @param count 挿入する小節数
	 * @throws IllegalStateException 動作モードが編集モードではない
	 * @throws IllegalArgumentException 挿入位置に現在の小節数より大きい小節番号を指定した
	 * @throws IllegalArgumentException 挿入する小節数にマイナス値を指定した
	 * @throws IllegalArgumentException 挿入により小節数が{@link BmsSpec#MEASURE_MAX_COUNT}を超える
	 */
	public void insertMeasure(int measureWhere, int count) {
		mTlAccessor.insert(measureWhere, count);
	}

	/**
	 * 指定位置の小節を消去します。
	 * <p>消去位置の小節を1個消去します。<br>
	 * それ以外の処理は{@link #removeMeasure(int, int)}と同じです。</p>
	 * @param measureWhere 消去位置の小節番号
	 */
	public void removeMeasure(int measureWhere) {
		mTlAccessor.remove(measureWhere, 1);
	}

	/**
	 * 指定位置の小節を消去します。
	 * <p>小節を消去すると、消去位置よりも後ろの小節データは手前にずれます。</p>
	 * <p>小節の存在しない領域を巻き込んで消去しないように注意してください。</p>
	 * @param measureWhere 消去位置の小節番号
	 * @param count 消去する小節数
	 * @throws IllegalStateException 動作モードが編集モードではない
	 * @throws IllegalArgumentException 消去位置に現在の小節数より大きい小節番号を指定した
	 * @throws IllegalArgumentException 消去する小節数にマイナス値を指定した
	 * @throws IllegalArgumentException 存在しない小節を消去しようとした
	 */
	public void removeMeasure(int measureWhere, int count) {
		mTlAccessor.remove(measureWhere, count);
	}

	/**
	 * 指定チャンネル同士のデータ内容を入れ替えます。
	 * <p>チャンネルインデックスは入れ替え対象チャンネル1,2共に0を指定します。<br>
	 * それ以外の処理は{@link #swapChannel(int, int, int, int)}と同じです。</p>
	 * @param channel1 入れ替え対象チャンネル1の番号
	 * @param channel2 入れ替え対象チャンネル2の番号
	 */
	public void swapChannel(int channel1, int channel2) {
		mTlAccessor.swapChannel(channel1, 0, channel2, 0);
	}

	/**
	 * 指定チャンネル同士のデータ内容を入れ替えます。
	 * <p>入れ替え対象のチャンネルのデータ型は完全に一致していなければなりません。また、小節長変更、
	 * BPM変更、譜面停止チャンネルを入れ替え対象として指定することはできません。</p>
	 * @param channel1 入れ替え対象チャンネル1の番号
	 * @param index1 入れ替え対象チャンネル1のインデックス
	 * @param channel2 入れ替え対象チャンネル2の番号
	 * @param index2 入れ替え対象チャンネル2のインデックス
	 * @throws IllegalStateException 動作モードが編集モードではない
	 * @throws IllegalArgumentException BMS仕様にないチャンネル番号を指定した
	 * @throws IndexOutOfBoundsException チャンネルインデックスがマイナス値
	 * @throws IndexOutOfBoundsException 重複不許可のチャンネルで0以外のインデックスを指定した
	 * @throws IllegalArgumentException 指定チャンネルが小節長変更・BPM変更・譜面停止のいずれかだった
	 * @throws IllegalArgumentException チャンネル1,2のデータ型が一致しない
	 */
	public void swapChannel(int channel1, int index1, int channel2, int index2) {
		mTlAccessor.swapChannel(channel1, index1, channel2, index2);
	}


	/**
	 * 原点の楽曲位置から指定刻み数分だけ移動した楽曲位置を計算します。
	 * <p>引数atFromから原点の小節番号・刻み位置を取り出します。<br>
	 * それ以外の処理は{@link #seekPoint(int, double, double, BmsPoint)}と同じです。</p>
	 * @param atFrom 原点の楽曲位置
	 * @param offsetTick 移動量を示す刻み数
	 * @param outPoint 移動後の楽曲位置を格納する楽曲位置オブジェクト
	 * @return 引数で指定した楽曲位置オブジェクト
	 * @throws NullPointerException atFromがnull
	 */
	public BmsPoint seekPoint(BmsAt atFrom, double offsetTick, BmsPoint outPoint) {
		assertArgNotNull(outPoint, "outPoint");
		return mTlAccessor.seekPoint(atFrom.getMeasure(), atFrom.getTick(), offsetTick, outPoint);
	}

	/**
	 * 原点の楽曲位置から指定刻み数分だけ移動した楽曲位置を計算します。
	 * <p>進行方向への移動での最大値は、小節番号=小節数, 刻み位置=0となります。<br>
	 * 後退方向への移動での最小値は、小節番号=0, 刻み位置=0となります。<br>
	 * いずれの場合も有効な楽曲位置を超えた位置を示すことはありません。</p>
	 * <p>刻み位置による移動では、小節長の設定値の影響を受けますが、BPMと譜面停止時間の影響は受けません。</p>
	 * <p>計算結果は引数outPointに対して小節番号・刻み位置を設定して返します。</p>
	 * @param measureFrom 原点の小節番号
	 * @param tickFrom 原点の刻み位置
	 * @param offsetTick 移動量を示す刻み数
	 * @param outPoint 移動後の楽曲位置を格納する楽曲位置オブジェクト
	 * @return 引数で指定した楽曲位置オブジェクト
	 * @throws IllegalArgumentException 原点の小節番号にノート・小節データが存在し得ない値を指定した
	 * @throws IllegalArgumentException 原点の刻み位置にマイナス値または当該小節の刻み数以上の値を指定した
	 * @throws NullPointerException outPointがnull
	 */
	public BmsPoint seekPoint(int measureFrom, double tickFrom, double offsetTick, BmsPoint outPoint) {
		assertArgNotNull(outPoint, "outPoint");
		return mTlAccessor.seekPoint(measureFrom, tickFrom, offsetTick, outPoint);
	}

	/**
	 * 指定楽曲位置より後の位置に存在する直近のノートを検索し、その楽曲位置を返します。
	 * <p>引数atから小節番号、刻み位置を取り出します。<br>
	 * 全てのチャンネルを検索対象とします。<br>
	 * それ以外の処理は{@link #seekNextPoint(int, double, boolean, IntPredicate, BmsPoint)}と同じです。</p>
	 * @param at 検索を開始する楽曲位置
	 * @param inclusiveFrom 指定楽曲位置を検索対象に含めるかどうか
	 * @param outPoint 直近のノートの楽曲位置
	 * @return 引数outPointが示すBmsPointオブジェクトの参照
	 * @throws NullPointerException atがnull
	 */
	public BmsPoint seekNextPoint(BmsAt at, boolean inclusiveFrom, BmsPoint outPoint) {
		assertArgNotNull(at, "at");
		return mTlAccessor.seekNextPoint(at.getMeasure(), at.getTick(), inclusiveFrom, c -> true, outPoint);
	}

	/**
	 * 指定楽曲位置より後の位置に存在する直近のノートを検索し、その楽曲位置を返します。
	 * <p>全てのチャンネルを検索対象とします。<br>
	 * それ以外の処理は{@link #seekNextPoint(int, double, boolean, IntPredicate, BmsPoint)}と同じです。</p>
	 * @param measure 検索を開始する小節番号
	 * @param tick 検索を開始する刻み位置
	 * @param inclusiveFrom 指定楽曲位置を検索対象に含めるかどうか
	 * @param outPoint 直近のノートの楽曲位置
	 * @return 引数outPointが示すBmsPointオブジェクトの参照
	 */
	public BmsPoint seekNextPoint(int measure, double tick, boolean inclusiveFrom, BmsPoint outPoint) {
		return mTlAccessor.seekNextPoint(measure, tick, inclusiveFrom, c -> true, outPoint);
	}

	/**
	 * 指定楽曲位置より後の位置に存在する直近のノートを検索し、その楽曲位置を返します。
	 * <p>引数atから小節番号、刻み位置を取り出します。<br>
	 * それ以外の処理は{@link #seekNextPoint(int, double, boolean, IntPredicate, BmsPoint)}と同じです。</p>
	 * @param at 検索を開始する楽曲位置
	 * @param inclusiveFrom 指定楽曲位置を検索対象に含めるかどうか
	 * @param chTester チャンネルを検索対象とするかを判定するテスター
	 * @param outPoint 直近のノートの楽曲位置
	 * @return 引数outPointが示すBmsPointオブジェクトの参照
	 * @throws NullPointerException atがnull
	 */
	public BmsPoint seekNextPoint(BmsAt at, boolean inclusiveFrom, IntPredicate chTester, BmsPoint outPoint) {
		assertArgNotNull(at, "at");
		return mTlAccessor.seekNextPoint(at.getMeasure(), at.getTick(), inclusiveFrom, chTester, outPoint);
	}

	/**
	 * 指定楽曲位置より後の位置に存在する直近のノート、小節データ、または小節線を検索し、その楽曲位置を返します。
	 * <p>特定のチャンネルのノートを検索対象にしたくない場合は引数chTesterでfalseを返すことで検索対象外に
	 * することができます。但しこのテスターは頻繁に呼び出される可能性があるのでできるだけ軽い処理とするべきです。</p>
	 * <p>楽曲の末端まで対象のノートが存在しなかった場合、楽曲位置は小節番号=小節数、刻み位置0を示します。</p>
	 * <p>当メソッドでは、検索する過程で小節番号が変化した際に次の小節を表す小節線を、特別なチャンネル番号である
	 * {@link BmsSpec#CHANNEL_MEASURE}としてチャンネルのテスターに渡します。小節が変化した際の小節先頭を検知したい
	 * 場合は先述のチャンネル番号を検査合格にすることでそれが可能になります。</p>
	 * <p>ノート、小節データのない空の楽曲に対して当メソッドを呼び出した場合、検索開始楽曲位置のアサーションは
	 * 行われず、小節番号0、刻み位置0の楽曲位置を返します。</p>
	 * <p>処理結果は引数outPointで指定されたBmsPointオブジェクトに格納されます。また、メソッドの戻り値として
	 * outPointが示す参照を返します。</p>
	 * @param measure 検索を開始する小節番号
	 * @param tick 検索を開始する刻み位置
	 * @param inclusiveFrom 指定楽曲位置を検索対象に含めるかどうか
	 * @param chTester チャンネルを検索対象とするかを判定するテスター
	 * @param outPoint 直近のノートの楽曲位置
	 * @return 引数outPointが示すBmsPointオブジェクトの参照
	 * @throws IllegalArgumentException 小節番号がマイナス値または小節数以上
	 * @throws IllegalArgumentException 小節の刻み位置がマイナス値または当該小節の刻み数以上
	 * @throws NullPointerException chTesterがnull
	 * @throws NullPointerException outPointがnull
	 */
	public BmsPoint seekNextPoint(int measure, double tick, boolean inclusiveFrom, IntPredicate chTester,
			BmsPoint outPoint) {
		return mTlAccessor.seekNextPoint(measure, tick, inclusiveFrom, chTester, outPoint);
	}

	/**
	 * タイムラインの指定楽曲位置のみを走査するストリームを返します。
	 * <p>返されたタイムラインストリームは楽曲位置の前方から後方に向かってタイムラインを走査し、タイムライン要素を
	 * 小節線・小節データ・ノートの順で列挙します。列挙されたタイムライン要素はJava標準のストリームAPIによって
	 * 様々な処理を行うことができるようになっています。当メソッドでは指定された楽曲位置のみを走査します。</p>
	 * <p>ストリームの利用方法についてはJavaリファレンスのストリームAPI(java.util.stream)を参照してください。</p>
	 * @param at 楽曲位置
	 * @return 指定楽曲位置のみを走査するストリーム
	 * @throws NullPointerException atがnull
	 * @throws IllegalArgumentException 小節番号がマイナス値
	 * @throws IllegalArgumentException 小節の刻み位置が{@link BmsSpec#TICK_MIN}以外の時、小節番号が小節数以上
	 * @throws IllegalArgumentException 小節の刻み位置が{@link BmsSpec#TICK_MIN}の時、小節番号が小節数超過
	 * @throws IllegalArgumentException 小節の刻み位置が{@link BmsSpec#TICK_MIN}未満、または{@link BmsSpec#TICK_MAX}超過
	 * @since 0.1.0
	 */
	public Stream<BmsTimelineElement> timeline(BmsAt at) {
		assertArgNotNull(at, "at");
		return mTlAccessor.timeline(at.getMeasure(), at.getTick());
	}

	/**
	 * タイムラインの指定楽曲位置のみを走査するストリームを返します。
	 * <p>返されたタイムラインストリームは楽曲位置の前方から後方に向かってタイムラインを走査し、タイムライン要素を
	 * 小節線・小節データ・ノートの順で列挙します。列挙されたタイムライン要素はJava標準のストリームAPIによって
	 * 様々な処理を行うことができるようになっています。当メソッドでは指定された楽曲位置のみを走査します。</p>
	 * <p>ストリームの利用方法についてはJavaリファレンスのストリームAPI(java.util.stream)を参照してください。</p>
	 * @param measure 小節番号
	 * @param tick 小節の刻み位置
	 * @return 指定楽曲位置のみを走査するストリーム
	 * @throws IllegalArgumentException 小節番号がマイナス値
	 * @throws IllegalArgumentException 小節の刻み位置が{@link BmsSpec#TICK_MIN}以外の時、小節番号が小節数以上
	 * @throws IllegalArgumentException 小節の刻み位置が{@link BmsSpec#TICK_MIN}の時、小節番号が小節数超過
	 * @throws IllegalArgumentException 小節の刻み位置が{@link BmsSpec#TICK_MIN}未満、または{@link BmsSpec#TICK_MAX}超過
	 * @since 0.1.0
	 */
	public Stream<BmsTimelineElement> timeline(int measure, double tick) {
		return mTlAccessor.timeline(measure, tick);
	}

	/**
	 * タイムライン全体を操作するストリームを返します。
	 * <p>返されたタイムラインストリームは楽曲位置の前方から後方に向かってタイムラインを走査し、タイムライン要素を
	 * 小節線・小節データ・ノートの順で列挙します。列挙されたタイムライン要素はJava標準のストリームAPIによって
	 * 様々な処理を行うことができるようになっています。</p>
	 * <p>当メソッドが返すストリームは、タイムライン全体から全チャンネルのタイムライン要素を列挙します。
	 * ストリームの利用方法についてはJavaリファレンスのストリームAPI(java.util.stream)を参照してください。</p>
	 * @return タイムライン全体を操作するストリーム
	 * @since 0.1.0
	 */
	public Stream<BmsTimelineElement> timeline() {
		return mTlAccessor.timeline(BmsSpec.MEASURE_MIN, BmsSpec.TICK_MIN, mTlAccessor.getCount(), BmsSpec.TICK_MIN);
	}

	/**
	 * タイムラインの指定楽曲位置の範囲を走査するストリームを返します。
	 * <p>返されたタイムラインストリームは楽曲位置の前方から後方に向かってタイムラインを走査し、タイムライン要素を
	 * 小節線・小節データ・ノートの順で列挙します。列挙されたタイムライン要素はJava標準のストリームAPIによって
	 * 様々な処理を行うことができるようになっています。</p>
	 * <p>ストリームの利用方法についてはJavaリファレンスのストリームAPI(java.util.stream)を参照してください。</p>
	 * @param atBegin 走査開始楽曲位置
	 * @param atEnd 走査終了楽曲位置(この楽曲位置の小節の刻み位置を含まない)
	 * @return タイムラインの指定楽曲位置の範囲を走査するストリーム
	 * @throws NullPointerException atBeginまたはatEndがnull
	 * @throws IllegalArgumentException 走査開始/終了楽曲位置の小節番号がマイナス値
	 * @throws IllegalArgumentException 走査終了楽曲位置の小節の刻み位置が{@link BmsSpec#TICK_MIN}以外の時、小節番号が小節数以上
	 * @throws IllegalArgumentException 走査終了楽曲位置の小節の刻み位置が{@link BmsSpec#TICK_MIN}の時、小節番号が小節数超過
	 * @throws IllegalArgumentException 走査開始/終了楽曲位置の楽曲位置の小節の刻み位置が{@link BmsSpec#TICK_MIN}未満、または{@link BmsSpec#TICK_MAX}超過
	 * @throws IllegalArgumentException atEndがatBeginと同じまたは手前の楽曲位置を示している
	 * @since 0.1.0
	 */
	public Stream<BmsTimelineElement> timeline(BmsAt atBegin, BmsAt atEnd) {
		assertArgNotNull(atBegin, "atBegin");
		assertArgNotNull(atEnd, "atEnd");
		var measureBegin = atBegin.getMeasure();
		var tickBegin = atBegin.getTick();
		var measureEnd = atEnd.getMeasure();
		var tickEnd = atEnd.getTick();
		return mTlAccessor.timeline(measureBegin, tickBegin, measureEnd, tickEnd);
	}

	/**
	 * タイムラインの指定楽曲位置の範囲を走査するストリームを返します。
	 * <p>返されたタイムラインストリームは楽曲位置の前方から後方に向かってタイムラインを走査し、タイムライン要素を
	 * 小節線・小節データ・ノートの順で列挙します。列挙されたタイムライン要素はJava標準のストリームAPIによって
	 * 様々な処理を行うことができるようになっています。</p>
	 * <p>ストリームの利用方法についてはJavaリファレンスのストリームAPI(java.util.stream)を参照してください。</p>
	 * @param measureBegin 走査開始楽曲位置の小節番号
	 * @param tickBegin 走査開始楽曲位置の小節の刻み位置
	 * @param measureEnd 走査終了楽曲位置の小節番号
	 * @param tickEnd 走査終了楽曲位置の小節の刻み位置(この位置を含まない)
	 * @return タイムラインの指定楽曲位置の範囲を走査するストリーム
	 * @throws IllegalArgumentException 走査開始/終了楽曲位置の小節番号がマイナス値
	 * @throws IllegalArgumentException 走査楽曲位置の小節の刻み位置が{@link BmsSpec#TICK_MIN}以外の時、小節番号が小節数以上
	 * @throws IllegalArgumentException 走査楽曲位置の小節の刻み位置が{@link BmsSpec#TICK_MIN}の時、小節番号が小節数超過
	 * @throws IllegalArgumentException 走査開始/終了楽曲位置の小節の刻み位置が{@link BmsSpec#TICK_MIN}未満、または{@link BmsSpec#TICK_MAX}超過
	 * @throws IllegalArgumentException 走査終了楽曲位置が走査開始楽曲位置と同じまたは手前の楽曲位置を示している
	 * @since 0.1.0
	 */
	public Stream<BmsTimelineElement> timeline(int measureBegin, double tickBegin, int measureEnd, double tickEnd) {
		return mTlAccessor.timeline(measureBegin, tickBegin, measureEnd, tickEnd);
	}

	/**
	 * 指定したメタ情報要素をBMSコンテンツに追加します。
	 * <p>当メソッドは同一、または互換性のあるBMS仕様を持つ別のBMSコンテンツで走査したタイムライン要素を追加する時に使用することを想定しています。
	 * 互換性のないBMS仕様を持つBMSコンテンツを走査した結果取得したタイムライン要素を指定すると、
	 * BMS仕様違反によりタイムライン要素を追加できない可能性があり多くの場合例外がスローされる結果になります。
	 * 原則として同一のBMS仕様を持つBMSコンテンツ間のタイムライン要素の転送に使用することを強く推奨します。</p>
	 * <p>当メソッドで小節線を表すタイムライン要素入力しても何も行いません。追加する要素は小節データ、ノートです。
	 * それぞれの要素追加の仕様については関連項目に記載のメソッドを参照してください。
	 * 基本的には例外も前述のメソッドに準拠した形でスローされます。</p>
	 * @param timeline タイムライン要素
	 * @throws IllegalStateException 動作モードが編集モードではない
	 * @throws NullPointerException timelineがnull
	 * @see #setMeasureValue(int, int, int, Object)
	 * @see #putNote(int, int, int, double, int, Supplier)
	 * @since 0.8.0
	 */
	public void putTimeline(BmsTimelineElement timeline) {
		mTlAccessor.putTimeline(timeline);
	}

	/**
	 * 小節番号・刻み位置を時間(秒)に変換します。
	 * <p>引数atから小節番号・刻み位置を取り出します。<br>
	 * それ以外の処理は{@link #pointToTime(int, double)}と同じです。</p>
	 * @param at 楽曲位置
	 * @return 時間(秒)
	 * @throws NullPointerException atがnull
	 */
	public double pointToTime(BmsAt at) {
		assertArgNotNull(at, "at");
		return mTlAccessor.pointToTime(at.getMeasure(), at.getTick());
	}

	/**
	 * 小節番号・刻み位置を時間(秒)に変換します。
	 * <p>このメソッドは参照モードの場合のみ呼び出すことができます。</p>
	 * <p>小節長が極端に短く、BPMが極端に高い譜面では浮動小数点演算の精度上の理由により、tickと(tick+1)の時間が
	 * 全く同じ値を示すことがあります(小節をまたぐ場合も同様)。一般的な小節長・BPMではこのような問題は発生しません。</p>
	 * @param measure 小節番号
	 * @param tick 小節の刻み位置
	 * @return 時間(秒)
	 * @throws IllegalStateException 動作モードが参照モードではない
	 * @throws IllegalArgumentException 小節番号が{@link BmsSpec#MEASURE_MIN}未満または{@link BmsSpec#MEASURE_MAX}超過
	 * @throws IllegalArgumentException 小節番号が{@link BmsSpec#MEASURE_MAX_COUNT}の時、小節の刻み位置に0以外を指定した
	 * @throws IllegalArgumentException 小節の刻み位置にマイナス値または当該小節の刻み数以上の値を指定した
	 */
	public double pointToTime(int measure, double tick) {
		return mTlAccessor.pointToTime(measure, tick);
	}

	/**
	 * 時間(秒)を楽曲位置(小節番号/刻み位置)に変換します。
	 * <p>このメソッドは参照モードの場合のみ呼び出すことができます。</p>
	 * <p>指定した時間が楽曲の総時間を超える場合、返される楽曲位置は小節番号が小節数、刻み位置が0になります。</p>
	 * @param timeSec 時間(秒)
	 * @param outPoint 楽曲位置を格納する楽曲位置オブジェクト
	 * @return 引数で指定した楽曲位置オブジェクト
	 * @throws IllegalStateException 動作モードが参照モードではない
	 * @throws IllegalArgumentException 時間にマイナス値を指定した
	 * @throws NullPointerException outPointがnull
	 */
	public BmsPoint timeToPoint(double timeSec, BmsPoint outPoint) {
		assertArgNotNull(outPoint, "outPoint");
		return mTlAccessor.timeToPoint(timeSec, outPoint);
	}

	/**
	 * BMSコンテンツの内容からハッシュ値を生成し、結果を返します。
	 * <p>当メソッドの目的は「プレーに影響する変更が行われたかどうかを検出すること」にあります。
	 * この目的を果たすために、BMSコンテンツの基準となるBMS仕様に登録されたメタ情報とチャンネルは
	 * 「同一性チェックを行うかどうか」の情報を保有しています。
	 * 同一性チェックを行うメタ情報・チャンネルのデータをハッシュ値の入力データとして抽出しハッシュ値を計算します。</p>
	 * <p>プレーに影響しない(とBMS仕様で定められた)情報は、どのように変更されてもハッシュ値が変化することはありません。
	 * 例えば、プレーする譜面に直接関係のないバックグラウンドアニメーションなどは後から差し替えてもハッシュ値が変化する
	 * (別の譜面として認識される) べきではありません。
	 * この仕組みにより制作者側に対してプレーする譜面以外の箇所に対する更新の自由度が与えられます。</p>
	 * <p>逆に、全く同じタイトル、アーティスト、BPM等でも、プレーに関わるノートを移動するような修正が加えられた場合、
	 * 別譜面であると検知されるべきであり、そのような情報が「同一性チェック対象の情報」となり得ることになります。</p>
	 *
	 * <strong>BMS仕様の改変検知</strong>
	 * <p>includeSpec を true にするとBMSコンテンツの内容に変化がなくてもBMS仕様が改変された時にハッシュ値が変化します。
	 * これによりBMS仕様に僅かでも変更が加わると改変が検知されるので、譜面のプレー内容の一貫性が強力に保証されます。
	 * ただし、BMS仕様が完全な後方互換性を保ったまま仕様拡張された場合でも改変が検知されるデメリットがあるので、
	 * アプリケーション側の仕様を十分に検討したうえで本オプションの有無を決定してください。</p>
	 * <p>当オプションを false にするとBMS仕様へのメタ情報・チャンネル追加に対しては寛容的になりますが、
	 * 既存のメタ情報・チャンネルの仕様を変更すると非常に高い確率でBMSコンテンツとしての改変が検知されます。
	 * よって、既存のメタ情報・チャンネルの仕様を変更した場合、
	 * 当オプションに関わらず原則として改変が検知されるものと考えて差し支えありません。</p>
	 *
	 * <strong>メタ情報・タイムラインの改変検知</strong>
	 * <p>ハッシュ値を生成するにあたり、入力データとしてメタ情報またはタイムライン、
	 * 或いはその両方を入力データとして使用するかを決定することができます。includeMetas, includeTimeline
	 * はそれぞれの情報を含めるかどうかを決定するために指定します。ただし、両方を false にすることはできません。</p>
	 *
	 * <p>当メソッドの処理には時間がかかります。また、処理中は多くのメモリ領域を使用する可能性があるため、
	 * 何度も呼び出すとアプリケーションのパフォーマンスが大幅に低下する危険性があります。
	 * 当メソッドの呼び出し回数は極力減らすようアプリケーションを設計してください。</p>
	 * @param includeSpec BMS仕様を含めるかどうか
	 * @param includeMetas メタ情報を含めるかどうか
	 * @param includeTimeline タイムラインを含めるかどうか
	 * @return このBMSコンテンツのハッシュ値
	 * @throws IllegalStateException 動作モードが参照モードではない
	 * @throws IllegalArgumentException includeMetas と includeTimeline の両方が false
	 * @since 0.9.0
	 */
	public byte[] generateHash(boolean includeSpec, boolean includeMetas, boolean includeTimeline) {
		return generateHashSeed(includeSpec, includeMetas, includeTimeline).toHash();
	}

	/**
	 * BMSコンテンツの内容からハッシュ値を生成し、結果を返します。
	 * <p>当メソッドは generateHash(true, true, true) と等価です。</p>
	 * <p><strong>当メソッドはバージョン0.9.0以前から使用できますが、0.9.0以降と以前でハッシュ値の互換性がありません。
	 * 0.9.0以降のハッシュ値が正式版となります。</strong></p>
	 * @return このBMSコンテンツのハッシュ値
	 * @throws IllegalStateException 動作モードが参照モードではない
	 * @see #generateHash(boolean, boolean, boolean)
	 * @since 0.9.0
	 */
	public byte[] generateHash() {
		return generateHashSeed(true, true, true).toHash();
	}

	/**
	 * BMSコンテンツのハッシュ値出力用入力データ生成
	 * @param includeSpec BMS仕様を含めるかどうか
	 * @param includeMetas メタ情報を含めるかどうか
	 * @param includeTimeline タイムラインを含めるかどうか
	 * @return ハッシュ値出力用の入力データ
	 * @throws IllegalStateException 動作モードが参照モードではない
	 * @throws IllegalArgumentException includeMetas と includeTimeline の両方が false
	 */
	HashSeed generateHashSeed(boolean includeSpec, boolean includeMetas, boolean includeTimeline) {
		assertArg(includeMetas || includeTimeline, "Must include metas or timeline");
		assertIsReferenceMode();

		var seed = new HashSeed();
		seed.beginObject(false);
		if (includeSpec) { editHashSeedForSpec(seed); }
		if (includeMetas) { editHashSeedForMetas(seed); }
		if (includeTimeline) { editHashSeedForTimeline(seed); }
		seed.endObject();

		return seed;
	}

	/**
	 * BMS仕様のハッシュ値出力用入力データ編集
	 * @param seed ハッシュ値出力用入力データ
	 */
	private void editHashSeedForSpec(HashSeed seed) {
		var hash = mSpec.generateHash();
		seed.put("spec", Utility.byteArrayToString(hash));
	}

	/**
	 * メタ情報のハッシュ値出力用入力データ編集
	 * @param seed ハッシュ値出力用入力データ
	 */
	private void editHashSeedForMetas(HashSeed seed) {
		// メタ情報の書き出し開始
		seed.beginObject("metas", false);

		// 同一性チェックONの全メタ情報を、ソートキー昇順(キーが同じ場合はBMS仕様への追加が先のもの)で書き出す
		var metas = mSpec.metas().filter(BmsMeta::isUniqueness).collect(Collectors.toList());
		for (var meta : metas) {
			// 値が未設定(複数・索引付きの場合は0件)のメタ情報は入力データに書き出さない
			var name = meta.getName();
			var unit = meta.getUnit();
			var type = meta.getType();
			if (!mMetas.containsMeta(name, unit)) {
				continue;
			}

			// メタ情報1件の書き出し開始
			seed.beginObject(String.format("%s:%s", name, unit.shortName), false)
					.put("t", type.getShortName())
					.beginArray("d", false);

			// 構成単位ごとに処理を振り分ける
			if (unit == BmsUnit.SINGLE) {
				// 単体メタ情報
				seed.beginObject(false)
						.put("i", 0)
						.put("v", type, mMetas.getSingleMeta(name))
						.endObject();
			} else if (unit == BmsUnit.MULTIPLE) {
				// 複数メタ情報
				var values = mMetas.getMultipleMetas(name);
				var count = values.size();
				for (var i = 0; i < count; i++) {
					seed.beginObject(false)
							.put("i", i)
							.put("v", type, values.get(i))
							.endObject();
				}
			} else if (unit == BmsUnit.INDEXED) {
				// 索引付きメタ情報
				for (var entry : mMetas.getIndexedMetas(name).entrySet()) {
					seed.beginObject(false)
							.put("i", entry.getKey())
							.put("v", type, entry.getValue())
							.endObject();
				}
			} else {
				// Don't care
			}

			// メタ情報1件の書き出し終了
			seed.endArray().endObject();
		}

		// メタ情報の書き出し終了
		seed.endObject();
	}

	/**
	 * タイムラインのハッシュ値出力用入力データ編集
	 * @param seed ハッシュ値出力用入力データ
	 */
	private void editHashSeedForTimeline(HashSeed seed) {
		// タイムラインの書き出し開始
		seed.beginObject("timeline", false);

		// 同一性チェックONの全チャンネルをチャンネル番号昇順で書き出す
		var channels = mSpec.channels().filter(BmsChannel::isUniqueness).collect(Collectors.toList());
		var measureCount = mTlAccessor.getCount();
		var notes = new ArrayList<BmsNote>();
		for (var channel : channels) {
			var chNum = channel.getNumber();
			var chType = channel.getType();
			seed.beginObject(String.format("%s:%s", BmsInt.to36s(chNum), chType.getShortName()), true);
			if (channel.isValueType()) {
				// 値型の場合
				// "<CH番号>:<型>": { "<小節番号>": [ データ, ... ], ... }
				for (var m = 0; m < measureCount; m++) {
					seed.beginArray(String.valueOf(m), true);
					var dataCount = mTlAccessor.getChannelDataCount(chNum, m);
					for (var d = 0; d < dataCount; d++) {
						seed.put(chType, mTlAccessor.getValue(chNum, d, m, null, false));
					}
					seed.endArray();
				}
			} else if (channel.isArrayType()) {
				// 配列型の場合
				// "<CH番号>:<型>": { "<小節番号>": [ [ { "t": <刻み位置>, "v": <値> }, ... ], ... ], ... }
				for (var m = 0; m < measureCount; m++) {
					seed.beginArray(String.valueOf(m), true);
					var dataCount = mTlAccessor.getChannelDataCount(chNum, m);
					for (var d = 0; d < dataCount; d++) {
						seed.beginArray(false);
						mTlAccessor.listNotes(chNum, d, m, notes);
						for (var n : notes) {
							seed.beginObject(false)
									.put("t", n.getTick())
									.put("v", n.getValue())
									.endObject();
						}
						seed.endArray();
					}
					seed.endArray();
				}
			} else {
				// Don't care
			}
			seed.endObject();
		}

		// タイムラインの書き出し終了
		seed.endObject();
	}

	/**
	 * BMSコンテンツ初期化処理
	 * @param spec BMS仕様
	 */
	private void initialSetup(BmsSpec spec) {
		assertArgNotNull(spec, "spec");
		mSpec = spec;
		mMetas = new MetaContainer(spec, this::isEditMode, () -> mTlAccessor.updateRecalcRange(0, BmsSpec.MEASURE_MAX));
		mTlAccessor = new TimelineAccessor(spec, mMetas, this::isEditMode);

		// BPM変更と譜面停止の統計情報を収集する
		collectBpmStopStatistics();
	}

	/**
	 * BPM変更と譜面停止の統計情報を収集する。
	 */
	private void collectBpmStopStatistics() {
		// BPM変更と譜面停止の統計情報を収集する
		var bpmStopStat = new BpmStopStatistics(mMetas.getInitialBpm());
		var measureCount = mTlAccessor.getCount();
		if ((measureCount > 0) && (mSpec.hasBpmChannel() || mSpec.hasStopChannel())) {
			// 列挙するチャンネルの範囲を求める
			var channelBegin = BmsSpec.CHANNEL_MAX;
			var channelEnd = BmsSpec.CHANNEL_MIN;
			var channelCountBpm = mSpec.getBpmChannelCount();
			var channelCountStop = mSpec.getStopChannelCount();
			var channels = new int[channelCountBpm + channelCountStop];
			for (var i = 0; i < channelCountBpm; i++) {
				channels[i] = mSpec.getBpmChannel(i, true).getNumber();
			}
			for (var i = 0; i < mSpec.getStopChannelCount(); i++) {
				channels[channelCountBpm + i] = mSpec.getStopChannel(i, true).getNumber();
			}
			for (var i = 0; i < channels.length; i++) {
				channelBegin = Math.min(channelBegin, channels[i]);
				channelEnd = Math.max(channelEnd, channels[i] + 1);
			}

			// BPM変更と譜面停止の統計収集開始
			mTlAccessor.enumNotes(channelBegin, channelEnd, BmsSpec.MEASURE_MIN, 0, measureCount, 0, bpmStopStat);
		}

		// 収集したBPM変更と統計情報を反映する
		mMinBpm = bpmStopStat.minBpm;
		mMaxBpm = bpmStopStat.maxBpm;
		mChangeBpmCount = bpmStopStat.changeBpmCount;
		mStopCount = bpmStopStat.stopCount;
	}

	/**
	 * 動作モードが参照モードかどうかをテストするアサーション
	 * @throws IllegalStateException 動作モードが参照モードではない
	 */
	protected void assertIsReferenceMode() {
		if (mIsEditMode) {
			throw new IllegalStateException("Now is NOT reference mode.");
		}
	}

	/**
	 * 動作モードが編集モードかどうかをテストするアサーション
	 * @throws IllegalStateException 動作モードが編集モードではない
	 */
	protected void assertIsEditMode() {
		if (!mIsEditMode) {
			throw new IllegalStateException("Now is NOT edit mode.");
		}
	}
}
