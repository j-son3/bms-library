package com.lmt.lib.bms;

import static com.lmt.lib.bms.BmsAssertion.*;

import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.stream.Stream;

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
 * においては、API呼び出し時の動的なメモリの確保・解放を極力行わず、高速なアプリケーションが開発することが出来る
 * ようになっています。</p>
 *
 * <p><b>データ構成について</b><br>
 * BMSコンテンツが保有するデータは、「BMS宣言」「メタ情報」「チャンネル(時間軸上のデータ)」から成り立っています。
 * BMS宣言を除くデータにおいては、そのデータ書式を{@link BmsSpec}によって決定付けられたBMS仕様に沿うように強制されます。
 * BMS仕様に違反するデータはBMSコンテンツ内には受け入れられないため、データの整合性は保証されます。</p>
 *
 * <p><b>BMS宣言</b><br>
 * BMS宣言は当該BMSコンテンツに付与される付加的な情報です。メタ情報とは異なりこの情報の内容には特に決まったルールは無く、
 * 特定のアプリケーションに対して独自の解釈を指示したりする自由な記述として活用することが出来ます。アプリケーションには
 * BMS宣言を考慮する義務は無いため、BMSコンテンツの制作者は不特定多数のアプリケーションに何らかの振る舞いを期待する目的で
 * BMS宣言を活用するべきではありません。<br>
 * 唯一の例外は&quot;encoding&quot;要素です。この要素はBMSライブラリにおいて当該コンテンツの外部データへの入出力において、
 * 指定した文字セットでBMSコンテンツをエンコード・デコードすることを強制します。この要素はBMSライブラリが提唱する唯一の
 * 共通要素であり、BMSライブラリを利用する全てのアプリケーションに対して利用用途を強制します。</p>
 *
 * <p>BMS宣言の参照・編集には以下のAPIを利用します。</p>
 *
 * <ul>
 * <li>BMS宣言を追加する：{@link #addDeclaration(String, String)}</li>
 * <li>BMS宣言を消去する：{@link #removeDeclaration(String)}</li>
 * <li>BMS宣言の存在確認：{@link #containsDeclaration(String)}</li>
 * <li>BMS宣言を取得する：{@link #getDeclaration(String)}, {@link #getDeclarations()}</li>
 * <li>BMS宣言に記述された文字セットを取得する：{@link #getEncoding()}</li>
 * </ul>
 *
 * <p><b>メタ情報</b><br>
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
 * <p><b>チャンネル</b><br>
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
 * <b>小節データを扱うAPI</b></p>
 *
 * <ul>
 * <li>小節データを取得する：{@link #getMeasureValue(int, int, int)}</li>
 * <li>小節データの登録確認：{@link #containsMeasureValue(int, int, int)}</li>
 * <li>小節データを設定する：{@link #setMeasureValue(int, int, Object)}</li>
 * </ul>
 *
 * <p><b>ノートを扱うAPI</b></p>
 *
 * <ul>
 * <li>ノートを取得する(位置指定)：{@link #getNote(int, int, int, double)}</li>
 * <li>後退位置のノートを取得する：{@link #getPreviousNote(int, int, int, double, boolean)}</li>
 * <li>前進位置のノートを取得する：{@link #getNextNote(int, int, int, double, boolean)}</li>
 * <li>ノートから解決済みメタ情報を取得：{@link #getResolvedNoteValue(int, int, int, double)}</li>
 * <li>ノートを検索する：{@link #pointOf(int, double, BmsNote.Tester)}</li>
 * <li>ノートを列挙する：{@link #enumNotes(int, int, int, double, int, double, BmsNote.Tester)}</li>
 * <li>ノート一覧を取得：{@link #listNotes(int, int, int, double, int, double, BmsNote.Tester)}</li>
 * <li>ノート数を数える：{@link #countNotes(int, int, int, double, int, double, BmsNote.Tester)}</li>
 * <li>ノートを追加する：{@link #putNote(int, int, int, double, int, BmsNote.Creator)}</li>
 * <li>ノートを消去する：{@link #removeNote(int, int, int, int, BmsNote.Tester)}</li>
 * </ul>
 *
 * <p>時間軸そのものの編集、および時間軸に関する情報の参照を行うためのAPIについては下記を参照してください。
 * これには、時間軸の計算や変換処理等のユーティリティAPIも含みます。</p>
 *
 * <ul>
 * <li>小節数を取得する：{@link #getMeasureCount()}</li>
 * <li>小節の刻み数を取得する：{@link #getMeasureTickCount(int)}</li>
 * <li>チャンネルデータ数取得：{@link #getChannelDataCount(int, int)}</li>
 * <li>小節を挿入する：{@link #insertMeasure(int, int)}</li>
 * <li>小節を消去する：{@link #removeMeasure(int, int)}</li>
 * <li>チャンネルデータの入れ替え：{@link #swapChannel(int, int, int, int)}</li>
 * <li>楽曲位置を移動する：{@link #seekPoint(int, double, double, BmsPoint)}</li>
 * <li>楽曲位置を時間に変換：{@link #pointToTime(int, double)}</li>
 * <li>時間を楽曲位置に変換：{@link #timeToPoint(double, BmsPoint)}</li>
 * </ul>
 *
 * <p><b>チャンネルデータ編集ポリシーについて</b><br>
 * BMSコンテンツの小節データ・ノートのようなチャンネルデータの編集は、そのデータ構造の特性上、データの内容によっては
 * 時間に関する内部データの再計算が必要になる場合があります。例えば、全時間軸のうち、真ん中付近の小節の小節長を
 * 変更したとします。そうすると、その小節以降のチャンネルデータへの到達時間が変更されることになり、内部で管理している
 * 時間に関するデータの再計算が発生します。<br>
 * 再計算が完了するまでの間、時間に関するデータに不整合が発生している状態となりますので、BMSコンテンツではその間は
 * 時間に関係するAPIへのアクセスが禁止されます。このアクセスが禁止されている状態のことを「編集モード」と呼称します。
 * </p>
 *
 * <p><b>動作モードについて</b><br>
 * BMSコンテンツでは、上述した事情により動作モードを2つ持っています。</p>
 *
 * <ul>
 * <li>参照モード<br>
 * このモードではチャンネルデータの全ての読み取りAPIへのアクセスを許可します。時間に関する内部データの計算が
 * 完了している状態を指し、時間計算を行うAPIも不整合を起こすことなく動作できます。逆に、このモードでは全ての
 * 「編集を伴うAPI」が禁止されます。参照モードでこれらのAPIにアクセスすると例外をスローするため注意が必要です。<br><br></li>
 * <li>編集モード<br>
 * データの編集を行う場合に必要なモードです。編集モードの間は「時間に関するデータを必要とするデータ読み取りAPI」への
 * アクセスを全て禁止状態にします。そうすることでBMSコンテンツから、データ不整合のある情報を提供することを防止し、
 * アプリケーションへの情報提供の質を高めます。このモードでは、参照モードでは出来なかった全ての「データ編集を伴うAPI」
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
 * <p><b>マルチスレッド対策について</b><br>
 * BMSコンテンツは、高速なデータアクセスを必要とするアプリケーションに配慮するため、BMSライブラリの設計ポリシーとして
 * マルチスレッドへの対策を行っていません。そのため、BMSコンテンツを含め単一のオブジェクトへ複数のスレッドから
 * アクセスした場合には、ドキュメントに記載の動作内容およびその後の動作の整合性を保証対象外としています。
 * 複数のスレッドから単一のオブジェクトを操作する予定がある場合には、個々のアプリケーションの責任でマルチスレッド対策を
 * 行うようにしてください。</p>
 */
public class BmsContent {
	/** BMS宣言のキー名書式の正規表現パターン */
	private static final Pattern PATTERN_DECL_NAME = Pattern.compile(
			"^[a-zA-Z_][a-zA-Z0-9_]*$");
	/** BMS宣言の値書式の正規表現パターン */
	private static final Pattern PATTERN_DECL_VALUE = Pattern.compile(
			"^([^\\\\\\\"]|(\\\\[\\\\\"]))*$");

	/** BMSコンテンツを生成するI/Fを提供します。 */
	@FunctionalInterface
	public interface Creator {
		/**
		 * BMSコンテンツオブジェクトのインスタンスを生成します。
		 * <p>BMSコンテンツオブジェクトは、パラメータで渡されたBMS仕様を入力として生成するべきです。BMSライブラリの実装には
		 * パラメータのBMS仕様以外を使用するとエラーとなるものも存在します。</p>
		 * @param spec BMS仕様
		 * @return BMSコンテンツオブジェクト
		 */
		BmsContent createContent(BmsSpec spec);
	}

	/**
	 * 小節全体データの実装
	 */
	private static class BmsMeasureElement extends MeasureElement {
		/**
		 * コンストラクタ
		 * @param measure 小節番号
		 */
		BmsMeasureElement(int measure) {
			super(measure);
		}

		/** {@inheritDoc} */
		@Override
		protected void recalculateTimeInfo(Object userParam,
				MutableDouble outBaseTime, MutableDouble outLengthSec, MutableDouble outBeginBpm, MutableDouble outEndBpm) {
			// 小節に対して時間・BPMの情報を反映する
			var tla = (TimelineAccessor)userParam;
			tla.calculateMeasureTimeInfo(getMeasure(), getTickCount(), outBaseTime, outLengthSec, outBeginBpm, outEndBpm);
		}
	}

	/**
	 * BMSコンテンツを構成するタイムラインのアクセッサ。
	 * <p>外部に公開するAPIのメソッドから直接アクセスするのではなく、
	 * このクラスを介してタイムラインにアクセスするように設計されている。</p>
	 * <p>上記の事情により、このクラスではメソッド単位で引数やデータ整合性のアサーションを行っている。
	 * このクラスよりも下層のメソッドには原則としてアサーションが存在しない。全てのタイムラインの操作は
	 * このクラスを介して行うこと。</p>
	 */
	private class TimelineAccessor {
		/** calculateMeasureTimeInfo()用：小節の基準時間(sec) */
		MutableDouble mTempBaseTime = new MutableDouble();
		/** calculateMeasureTimeInfo()用：小節の先頭からtickCalcToまでの時間(sec) */
		MutableDouble mTempLengthSec = new MutableDouble();
		/** calculateMeasureTimeInfo()用：小節開始時のBPM */
		MutableDouble mTempBeginBpm = new MutableDouble();
		/** calculateMeasureTimeInfo()用：小節終了時のBPM */
		MutableDouble mTempEndBpm = new MutableDouble();

		/**
		 * 指定チャンネル番号・小節番号に存在するチャンネルデータの件数を取得する。
		 * @param channel チャンネル番号
		 * @param measure 小節番号
		 * @return
		 * @exception IllegalArgumentException 小節データ・ノートを登録できないチャンネル番号
		 * @exception IllegalArgumentException 小節番号が{@link BmsSpec#MEASURE_MIN}未満または{@link BmsSpec#MEASURE_MAX}超過
		 */
		final int getChannelDataCount(int channel, int measure) {
			// アサーション
			assertChannelIndex(channel, 0);
			assertArgMeasureWithinRange(measure);

			// チャンネルに登録されているデータの件数を返す
			var channelSpec = mSpec.getChannel(channel);
			if (measure >= mTl.getMeasureCount()) {
				// データのない小節アクセス時は0とする
				return 0;
			} else if (channelSpec.isArrayType()) {
				// ノートの件数を返す
				return mTl.getNoteChannelDataCount(channel, measure);
			} else if (channelSpec.isValueType()) {
				// 小節データの件数を返す
				return mTl.getValueChannelDataCount(channel, measure);
			} else {
				// Don't care
				return 0;
			}
		}

		/**
		 * 指定の小節番号の位置に空の小節データを挿入する。
		 * <p>このメソッドによる挿入操作が完了すると、挿入位置(も含む)以降の小節データが挿入件数分だけ
		 * 後ろにずれることになる。</p>
		 * @param measureWhere 挿入位置の小節番号
		 * @param count 挿入小節数
		 * @exception IllegalStateException 動作モードが編集モードではない
		 * @exception IllegalArgumentException ノート・小節データが存在し得ない小節番号
		 * @exception IllegalArgumentException countがマイナス値
		 * @exception IllegalArgumentException 挿入処理により小節数が{@link BmsSpec#MEASURE_MAX_COUNT}を超過する
		 */
		final void insert(int measureWhere, int count) {
			// アサーション
			assertIsEditMode();
			assertArgMeasureWithinRange(measureWhere, mTl.getMeasureCount());
			assertArg(count >= 0, "Argument 'count' is minus value. count=%d", count);
			var beforeMeasureCount = mTl.getMeasureCount();
			assertArgRange(beforeMeasureCount + count, 0, BmsSpec.MEASURE_MAX_COUNT, "beforeMeasureCount + count");

			// 挿入件数が0の場合は何もしない(ていうか呼ぶなや)
			if (count == 0) {
				return;
			}

			// 末尾小節よりも後ろを挿入位置にした場合は何もしない(なんで効果があると思ったの？？)
			if (measureWhere >= beforeMeasureCount) {
				return;
			}

			// 小節を挿入する
			mTl.insertMeasure(measureWhere, count);
		}

		/**
		 * 指定の小節番号の小節から指定個数の小節データを消去し、それ以降の小節データを詰める。
		 * @param measureWhere 消去開始位置の小節番号
		 * @param count 消去する小節データの件数
		 * @exception IllegalStateException 動作モードが編集モードではない
		 * @exception IllegalArgumentException ノート・小節データが存在し得ない小節番号
		 * @exception IllegalArgumentException countがマイナス値
		 * @exception IllegalArgumentException 存在する小節データを超えて小節データを消去しようとした
		 */
		final void remove(int measureWhere, int count) {
			// アサーション
			assertIsEditMode();
			assertArgMeasureWithinRange(measureWhere, mTl.getMeasureCount() - 1);
			assertArg(count >= 0, "Argument 'count' is minus value. count=%d", count);
			assertArgRange(measureWhere + count, 0, mTl.getMeasureCount() - 1, "measureWhere + count");

			// 削除件数が0の場合は何もしない(ていうか呼ぶなや)
			if (count == 0) {
				return;
			}

			// 小節を消去する
			mTl.removeMeasure(measureWhere, count);
		}

		/**
		 * 指定位置にノートを登録する。同じ場所にノートが存在する場合は新しいノートで上書きする。
		 * @param channel チャンネル番号
		 * @param index チャンネルインデックス
		 * @param measure 小節番号
		 * @param tick 小節の刻み位置
		 * @param value ノートが持つ値
		 * @param createNote ノートオブジェクトを生成するサプライヤ
		 * @return 登録されたノートオブジェクト
		 * @exception IllegalStateException 動作モードが編集モードではない
		 * @exception IllegalArgumentException BMS仕様に無いチャンネル番号
		 * @exception IndexOutOfBoundsException チャンネルインデックスがマイナス値
		 * @exception IndexOutOfBoundsException チャンネルデータ重複不許可のチャンネルで0以外のインデックスを指定
		 * @exception IllegalArgumentException ノート・小節データが存在し得ない小節番号
		 * @exception IllegalArgumentException 小節番号がBMS仕様の許容範囲外
		 * @exception IllegalArgumentException 小節の刻み位置がマイナス値または当該小節の刻み数以上
		 * @exception IllegalArgumentException 指定チャンネルのデータ型が配列型ではない
		 * @exception IllegalArgumentException 16進配列チャンネル指定時、ノートの値に{@link BmsSpec#VALUE_16_MIN}未満、{@link BmsSpec#VALUE_16_MAX}超過の値を指定した
		 * @exception IllegalArgumentException 36進配列チャンネル指定時、ノートの値に{@link BmsSpec#VALUE_MIN}未満、{@link BmsSpec#VALUE_MAX}超過の値を指定した
		 * @exception NullPointerException createNoteがnull
		 * @exception IllegalArgumentException createNoteの結果がnull
		 */
		@SuppressWarnings("unchecked")
		final <T extends BmsNote> T putNote(int channel, int index, int measure, double tick, int value,
				BmsNote.Creator createNote) {
			// アサーション
			assertIsEditMode();
			assertChannelIndex(channel, index);
			assertPointAllowOverMeasureCount(measure, tick);
			var ch = mSpec.getChannel(channel);
			assertChannelArrayType(channel);
			assertValue(ch.getType(), value);
			assertArgNotNull(createNote, "createNote");

			// Creatorを使用して登録対象のNoteオブジェクトを生成する
			var note = createNote.createNote();
			assertArg(note != null, "Failed to create note.");

			// ノートを追加する
			note.setup(channel, index, measure, tick, value);
			mTl.putNote(note);

			return (T)note;
		}

		/**
		 * 指定位置のノートを消去する。
		 * @param channel チャンネル番号
		 * @param index チャンネルインデックス
		 * @param measure 小節番号
		 * @param tick 小節の刻み位置
		 * @return 消去した場合true、消去しなかった場合false
		 * @exception IllegalStateException 動作モードが編集モードではない
		 * @exception IllegalArgumentException BMS仕様に無いチャンネル番号
		 * @exception IndexOutOfBoundsException チャンネルインデックスがマイナス値
		 * @exception IndexOutOfBoundsException チャンネルデータ重複不許可のチャンネルで0以外のインデックスを指定
		 * @exception IllegalArgumentException 小節番号が{@link BmsSpec#MEASURE_MIN}未満または{@link BmsSpec#MEASURE_MAX}超過
		 * @exception IllegalArgumentException 小節の刻み位置がマイナス値または当該小節の刻み数以上
		 * @exception IllegalArgumentException 指定チャンネルのデータ型が配列型ではない
		 */
		final boolean removeNote(int channel, int index, int measure, double tick) {
			// アサーション
			assertIsEditMode();
			assertChannelIndex(channel, index);
			assertPointAllowOverMeasureCount(measure, tick);
			assertChannelArrayType(channel);

			return mTl.removeNote(channel, index, measure, tick);
		}

		/**
		 * 指定範囲のノートを消去する。
		 * @param channelBegin 消去開始チャンネル番号
		 * @param channelEnd 消去終了チャンネル番号(このチャンネルを含まない)
		 * @param measureBegin 消去開始小節番号
		 * @param measureEnd 消去終了小節番号(この小節を含まない)
		 * @param isRemoveTarget ノート消去の是非を判定する述語
		 * @return 消去されたノートの個数
		 * @exception IllegalStateException 動作モードが編集モードではない
		 * @exception IllegalArgumentException {@link TimelineAccessor#enumNotes}
		 * @exception NullPointerException isRemoveTargetがnull
		 */
		final int removeNote(int channelBegin, int channelEnd, int measureBegin, int measureEnd,
				BmsNote.Tester isRemoveTarget) {
			// アサーション
			assertIsEditMode();
			assertChannelRange(channelBegin);
			assertEndChannelRange(channelEnd);
			assertPoint(measureBegin, 0);
			assertPointAllowTerm(measureEnd, 0);
			assertArgNotNull(isRemoveTarget, "isRemoveTarget");

			// 削除対象Noteをリストアップする
			final var removeTargets = new ArrayList<BmsNote>();
			enumNotes(channelBegin, channelEnd, measureBegin, 0, measureEnd, 0, note -> {
				if (isRemoveTarget.testNote(note)) { removeTargets.add(note); }
				return true;
			});

			// リストアップしたNoteを削除する
			removeTargets.forEach(n -> removeNote(n.getChannel(), n.getIndex(), n.getMeasure(), n.getTick()));

			return removeTargets.size();
		}

		/**
		 * 指定小節のチャンネルに単一データを設定または破棄する。
		 * @param channel チャンネル番号
		 * @param index チャンネルインデックス
		 * @param measure 小節番号
		 * @param value 設定値。nullを指定した場合は破棄。
		 * @exception IllegalStateException 動作モードが編集モードではない
		 * @exception IllegalArgumentException BMS仕様に無いチャンネル番号を指定した
		 * @exception IndexOutOfBoundsException チャンネルインデックスがマイナス値
		 * @exception IndexOutOfBoundsException チャンネルデータ重複不許可のチャンネルで0以外のインデックスを指定した
		 * @exception IllegalArgumentException チャンネルのデータ型が値型ではない
		 * @exception ClassCastException valueをチャンネルのデータ型に変換出来ない
		 * @exception IllegalArgumentException 小節長に{@link BmsSpec#LENGTH_MIN}未満または{@link BmsSpec#LENGTH_MAX}超過の値を設定しようとした
		 */
		final void setValue(int channel, int index, int measure, Object value) {
			// アサーション
			assertIsEditMode();
			assertChannelIndex(channel, index);
			assertArgMeasureWithinRange(measure);
			assertChannelNotArrayType(channel);

			// 値の設定か破棄かで処理を分岐
			if (value == null) {
				// 破棄の場合
				mTl.removeMeasureValue(channel, index, measure);
			} else {
				// 設定の場合
				var ch = mSpec.getChannel(channel);
				var convertedValue = ch.getType().cast(value);
				if (ch.isLength()) {
					// 小節長変更チャンネルの範囲外チェック
					var length = ((Number)convertedValue).doubleValue();
					if (!BmsSpec.isLengthWithinRange(length)) {
						var msg = String.format(
								"Measure length is out of range. channel=%s, measure=%d, expect-length=(%.16g-%.16g), actual-length=%.16g",
								ch, measure, BmsSpec.LENGTH_MIN, BmsSpec.LENGTH_MAX, length);
						throw new IllegalArgumentException(msg);
					}
				}
				mTl.putMeasureValue(channel, index, measure, convertedValue);
			}
		}

		/**
		 * 指定小節のチャンネルデータから単一データを取得する。
		 * @param channel チャンネル番号
		 * @param index チャンネルインデックス
		 * @param measure 小節番号
		 * @param requiredType 要求するデータ型。型変換不要の場合はnull。
		 * @param nullIfNotExist 単一データ未登録の場合に初期値ではなくnullを返す場合はtrue
		 * @return 要求データ型に変換された単一データ。当該小節のチャンネルデータに単一データ未登録の場合はそのチャンネルの初期値を返す。
		 * @exception IllegalArgumentException BMS仕様に無いチャンネル番号
		 * @exception IndexOutOfBoundsException チャンネルインデックスがマイナス値
		 * @exception IndexOutOfBoundsException チャンネルデータ重複不許可のチャンネルで0以外のインデックスを指定
		 * @exception IllegalArgumentException 指定チャンネルのデータ型が値型ではない
		 * @exception IllegalArgumentException 小節番号が{@link BmsSpec#MEASURE_MIN}未満または{@link BmsSpec#MEASURE_MAX}超過
		 * @exception ClassCastException データを要求型に変換出来ない
		 */
		final Object getValue(int channel, int index, int measure, BmsType requiredType, boolean nullIfNotExist) {
			// アサーション
			assertChannelIndex(channel, index);
			assertChannelNotArrayType(channel);
			assertArgMeasureWithinRange(measure);

			// 取得元のチャンネルデータを参照する
			var elem = (measure >= mTl.getMeasureCount()) ? null : mTl.getMeasureValue(channel, index, measure);
			if (elem == null) {
				// チャンネルデータ無しまたは値未設定の場合は当該チャンネルの初期値を返す
				return nullIfNotExist ? null : mSpec.getChannel(channel).getDefaultValue();
			} else if (requiredType == null) {
				// チャンネルデータ有りで型変換無し
				return elem.getValueAsObject();
			} else {
				// チャンネルデータ有りで型変換有りの場合は要求型への変換を行い返す
				return requiredType.cast(elem.getValueAsObject());
			}
		}

		/**
		 * 指定範囲の小節データを時間・BPM再計算対象に設定する。
		 * @param first 対象の最小小節番号
		 * @param last 対象の最大小節番号
		 */
		final void updateRecalcRange(int first, int last) {
			mTl.updateRecalcRange(first, last);
		}

		/**
		 * 時間・BPM再計算対象の小節データの再計算処理。
		 * @return 小節データの再計算を行った場合true
		 */
		final boolean recalculateTime() {
			return mTl.recalculateTime(this);
		}

		/**
		 * 指定された2つのチャンネルデータを入れ替える。
		 * @param channel1 入れ替え対象1のチャンネル番号
		 * @param index1 入れ替え対象1のチャンネルインデックス
		 * @param channel2 入れ替え対象2のチャンネル番号
		 * @param index2 入れ替え対象2のチャンネルインデックス
		 * @exception IllegalStateException 動作モードが編集モードではない
		 * @exception IllegalArgumentException BMS仕様に無いチャンネル番号
		 * @exception IndexOutOfBoundsException チャンネルインデックスがマイナス値
		 * @exception IndexOutOfBoundsException チャンネルデータ重複不許可のチャンネルで0以外のインデックスを指定
		 * @exception IllegalArgumentException 指定チャンネルが小節長変更・BPM変更・譜面停止のいずれか
		 * @exception IllegalArgumentException チャンネル1,2のデータ型が一致しない
		 */
		final void swapChannel(int channel1, int index1, int channel2, int index2) {
			// アサーション
			assertIsEditMode();
			assertChannelIndex(channel1, index1);
			assertChannelIndex(channel2, index2);
			var ch1 = mSpec.getChannel(channel1);
			var ch2 = mSpec.getChannel(channel2);
			assertArg(!ch1.isRelatedToTime(), "%d: This channel cannot swap.", channel1);
			assertArg(!ch2.isRelatedToTime(), "%d: This channel cannot swap.", channel2);
			assertArg(ch1.getType() == ch2.getType(), "Type mismatch chnnels. ch1=%s, ch2=%s", channel1, channel2);

			// 同じチャンネル・インデックスを示している場合は何もしない(ていうか呼ぶなや)
			if ((channel1 == channel2) && (index1 == index2)) {
				return;
			}

			// 全小節のチャンネルを入れ替える
			if (ch1.isArrayType()) {
				// 配列型チャンネル同士の入れ替え
				mTl.swapNoteChannel(channel1, index1, channel2, index2);
			} else if (ch2.isValueType()) {
				// 値型チャンネル同士の入れ替え
				mTl.swapValueChannel(channel1, index1, channel2, index2);
			} else {
				// Don't care
			}
		}

		/**
		 * 指定位置のノートを取得する。
		 * @param channel チャンネル番号
		 * @param index チャンネルインデックス
		 * @param measure 小節番号
		 * @param tick 小節の刻み位置
		 * @return 指定位置のノートオブジェクト。ノートが存在しない場合はnull。
		 * @exception IllegalArgumentException BMS仕様に無いチャンネル番号
		 * @exception IndexOutOfBoundsException チャンネルインデックスがマイナス値
		 * @exception IndexOutOfBoundsException チャンネルデータ重複不許可のチャンネルで0以外のインデックスを指定
		 * @exception IllegalArgumentException 小節番号が{@link BmsSpec#MEASURE_MIN}未満または{@link BmsSpec#MEASURE_MAX}超過
		 * @exception IllegalArgumentException 小節の刻み位置がマイナス値または当該小節の刻み数以上
		 * @exception IllegalArgumentException 指定チャンネルのデータ型が配列型ではない
		 */
		final BmsNote getNote(int channel, int index, int measure, double tick) {
			// アサーション
			assertChannelIndex(channel, index);
			assertPointAllowOverMeasureCount(measure, tick);
			assertChannelArrayType(channel);

			// ノートを取得する
			return mTl.getNote(channel, index, measure, tick);
		}

		/**
		 * 指定範囲のノートを取得する。
		 * @param channelBegin 取得開始チャンネル番号
		 * @param channelEnd 取得終了チャンネル番号(このチャンネルを含まない)
		 * @param measureBegin 取得開始小節番号
		 * @param tickBegin 取得開始刻み位置
		 * @param measureEnd 取得終了小節番号
		 * @param tickEnd 取得終了刻み位置(この位置を含まない)
		 * @param isCollect 当該ノートの取得是非を判定する述語
		 * @return 取得したノートオブジェクトのリスト。取得結果が0件でもnullにはならない。
		 * @exception IllegalArgumentException {@link TimelineAccessor#enumNotes}
		 * @exception NullPointerException isCollectがnull
		 */
		final List<BmsNote> listNotes(int channelBegin, int channelEnd,
				int measureBegin, double tickBegin, int measureEnd, double tickEnd, BmsNote.Tester isCollect) {
			// アサーション
			assertChannelRange(channelBegin);
			assertEndChannelRange(channelEnd);
			assertPoint(measureBegin, tickBegin);
			assertPointAllowTerm(measureEnd, tickEnd);
			assertArgNotNull(isCollect, "isCollect");

			// 指定範囲のノートリストを取得する
			return mTl.listNotes(channelBegin, channelEnd, measureBegin, tickBegin, measureEnd, tickEnd, isCollect);
		}

		/**
		 * 指定位置のノートを取得する。
		 * @param measure 取得対象の小節番号
		 * @param tick 取得対象の刻み位置
		 * @param isCollect ノートの取得是非を判定するテスター
		 * @return 取得したノートオブジェクトのリスト。取得結果が0件でもnullにはならない。
		 * @exception NullPointerException isCollectがnull
		 * @exception * {@link #enumNotes(int, int, BmsNote.Tester)}を参照
		 */
		final List<BmsNote> listNotes(int measure, double tick, BmsNote.Tester isCollect) {
			assertPoint(measure, tick);
			assertArgNotNull(isCollect, "isCollect");

			// 終点を計算する
			var point = seekPoint(measure, tick, 1, new BmsPoint());
			var mEnd = point.getMeasure();
			var tEnd = point.getTick();

			// 指定位置のノートリストを取得する
			return mTl.listNotes(BmsSpec.CHANNEL_MIN, BmsSpec.CHANNEL_MAX + 1, measure, tick, mEnd, tEnd, isCollect);
		}

		/**
		 * 指定位置から最も近い位置にあるノートを検索する。検索開始位置も検索対象になる。
		 * @param channel チャンネル番号
		 * @param index チャンネルインデックス
		 * @param measure 小節番号
		 * @param tick 小節の刻み位置
		 * @param direction 検索方向。1以上で前方検索、0以下で後方検索。
		 * @param inclusive 指定位置を検索対象に含めるかどうか
		 * @return 見つかったノートオブジェクト。見つからなかった場合はnull。
		 * @exception IllegalArgumentException BMS仕様に無いチャンネル番号
		 * @exception IndexOutOfBoundsException チャンネルインデックスがマイナス値
		 * @exception IndexOutOfBoundsException チャンネルデータ重複不許可のチャンネルで0以外のインデックスを指定
		 * @exception IllegalArgumentException 小節番号がマイナス値または小節数以上
		 * @exception IllegalArgumentException 小節の刻み位置がマイナス値または当該小節の刻み数以上
		 * @exception IllegalArgumentException 指定チャンネルのデータ型が配列型ではない
		 */
		final BmsNote getNearerNote(int channel, int index, int measure, double tick, int direction, boolean inclusive) {
			// アサーション
			assertChannelIndex(channel, index);
			assertPoint(measure, tick);
			assertChannelArrayType(channel);

			// 近隣を検索する処理
			if (direction > 0) {
				return mTl.getNextNote(channel, index, measure, tick, inclusive);
			} else {
				return mTl.getPreviousNote(channel, index, measure, tick, inclusive);
			}
		}

		/**
		 * 述語のテストを通過するノートの数を取得する。
		 * @param channelBegin カウント対象の開始チャンネル番号
		 * @param channelEnd カウント対象の終了チャンネル番号(このチャンネルを含まない)
		 * @param measureBegin カウント対象の開始小節番号
		 * @param tickBegin カウント対象の開始刻み位置
		 * @param measureEnd カウント対象の終了小節番号
		 * @param tickEnd カウント対象の終了刻み位置(この位置を含まない)
		 * @param isCounting ノートをテストする述語
		 * @return テストを通過したノートの数
		 * @exception IllegalArgumentException {@link TimelineAccessor#enumNotes}
		 */
		final int countNotes(int channelBegin, int channelEnd, int measureBegin,
				double tickBegin, int measureEnd, double tickEnd, BmsNote.Tester isCounting) {
			// アサーション
			assertChannelRange(channelBegin);
			assertEndChannelRange(channelEnd);
			assertPoint(measureBegin, tickBegin);
			assertPointAllowTerm(measureEnd, tickEnd);
			assertArgNotNull(isCounting, "isCounting");

			return mTl.countNotes(channelBegin, channelEnd, measureBegin, tickBegin, measureEnd, tickEnd, isCounting);
		}

		/**
		 * ノートの数を取得する。
		 * @param measure カウント対象の小節番号
		 * @param tick カウント対象の刻み位置
		 * @param isCounting カウント対象のノートを判定するテスター
		 * @return 判定に合格したノートの数
		 * @exception NullPointerException isCountingがnull
		 * @exception * {@link #enumNotes(int, int, BmsNote.Tester)}を参照
		 */
		final int countNotes(int measure, double tick, BmsNote.Tester isCounting) {
			assertPoint(measure, tick);
			assertArgNotNull(isCounting, "isCounting");

			// 終点を計算する
			var point = seekPoint(measure, tick, 1, new BmsPoint());
			var mEnd = point.getMeasure();
			var tEnd = point.getTick();

			// ノートの数を取得する
			return mTl.countNotes(BmsSpec.CHANNEL_MIN, BmsSpec.CHANNEL_MAX + 1, measure, tick, mEnd, tEnd, isCounting);
		}

		/**
		 * 指定範囲のノートを列挙する。
		 * @param channelBegin 列挙開始チャンネル番号
		 * @param channelEnd 列挙終了チャンネル番号(このチャンネルを含まない)
		 * @param measureBegin 列挙開始小節番号
		 * @param tickBegin 列挙開始刻み位置
		 * @param measureEnd 列挙終了小節番号
		 * @param tickEnd 列挙終了刻み位置(この位置を含まない)
		 * @param enumNote 列挙されたノートの通知を受ける述語
		 * @exception IllegalArgumentException channelBeginが小節データ・ノートを登録できないチャンネル番号
		 * @exception IllegalArgumentException channelEnd - 1が小節データ・ノートを登録できないチャンネル番号
		 * @exception IllegalArgumentException measureBeginがノート・小節データの存在し得ない小節番号
		 * @exception IllegalArgumentException tickBeginがマイナス値または当該小節の刻み数以上
		 * @exception IllegalArgumentException measureEndがノート・小節データが存在し得ない小節番号
		 * @exception IllegalArgumentException tickEndがマイナス値、当該小節の刻み数以上、または最終小節+1の時に0以外
		 * @exception NullPointerException enumNoteがnull
		 */
		final void enumNotes(int channelBegin, int channelEnd,
				int measureBegin, double tickBegin, int measureEnd, double tickEnd, BmsNote.Tester enumNote) {
			// アサーション
			assertChannelRange(channelBegin);
			assertEndChannelRange(channelEnd);
			if (mTl.getMeasureCount() > 0) {
				assertPoint(measureBegin, tickBegin);
				assertPointAllowTerm(measureEnd, tickEnd);
			}
			assertArgNotNull(enumNote, "enumNote");

			// 列挙範囲を決定する
			mTl.enumNotes(channelBegin, channelEnd, measureBegin, tickBegin, measureEnd, tickEnd, enumNote);
		}

		/**
		 * 指定楽曲位置のノートを全て列挙する。
		 * @param measure 小節番号
		 * @param tick 刻み位置
		 * @param enumNote 列挙されたノートの通知を受ける述語
		 * @exception IllegalArgumentException measureがマイナス値または小節数以上
		 * @exception IllegalArgumentException tickがマイナス値または当該小節の刻み数以上
		 * @exception NullPointerException enumNoteがnull
		 */
		final void enumNotes(int measure, double tick, BmsNote.Tester enumNote) {
			assertPoint(measure, tick);
			assertArgNotNull(enumNote, "enumNote");

			// 終点を計算する
			var point = seekPoint(measure, tick, 1, new BmsPoint());
			var mEnd = point.getMeasure();
			var tEnd = point.getTick();

			// 引数で指定位置のノートのみを列挙する
			enumNotes(BmsSpec.CHANNEL_MIN, BmsSpec.CHANNEL_MAX + 1, measure, tick, mEnd, tEnd, enumNote);
		}

		/**
		 * 楽曲位置をオフセットする。
		 * <p>前方移動ではMeasure=小節データ数,Tick=0、後方移動ではMeasure=0,Tick=0を超える位置には移動しない。</p>
		 * @param measure 基点の小節番号
		 * @param tick 基点の刻み位置
		 * @param offsetTick オフセットする刻み数。プラス値で前方、マイナス値で後方へオフセットする。
		 * @param outPoint オフセット後の楽曲位置を格納する楽曲位置オブジェクト
		 * @return 引数で指定した楽曲位置オブジェクト
		 * @exception IllegalArgumentException 小節番号がマイナス値または小節数以上
		 * @exception IllegalArgumentException 小節の刻み位置がマイナス値または当該小節の刻み数以上
		 */
		final BmsPoint seekPoint(int measure, double tick, double offsetTick, BmsPoint outPoint) {
			// アサーション
			assertPoint(measure, tick);

			// 進行方向により処理を振り分ける
			if (offsetTick >= 0) {
				// 前進
				return forwardPoint(measure, tick, offsetTick, outPoint);
			} else {
				// 後退
				return backwardPoint(measure, tick, -offsetTick, outPoint);
			}
		}

		/**
		 * 指定楽曲位置より後でノートの存在する楽曲位置を検索する。
		 * @param measure 検索を開始する小節番号
		 * @param tick 検索を開始する刻み位置
		 * @param inclusiveFrom 指定楽曲位置を検索対象に含めるかどうか
		 * @param chTester 検索対象のチャンネルを判定するテスター
		 * @param outPoint 検索した楽曲位置を格納する{@link BmsPoint}の参照
		 * @return outPointと同じ参照
		 * @exception IllegalArgumentException 小節番号がマイナス値または小節数以上
		 * @exception IllegalArgumentException 小節の刻み位置がマイナス値または当該小節の刻み数以上
		 * @exception NullPointerException chTesterがnull
		 * @exception NullPointerException outPointがnull
		 */
		final BmsPoint seekNextPoint(int measure, double tick, boolean inclusiveFrom, BmsChannel.Tester chTester,
				BmsPoint outPoint) {
			assertArgNotNull(chTester, "chTester");
			assertArgNotNull(outPoint, "outPoint");
			if (mTl.getMeasureCount() > 0) {
				assertPoint(measure, tick);
			} else {
				outPoint.setMeasure(0);
				outPoint.setTick(0);
				return outPoint;
			}

			// 指定小節から進行方向へ全ての小節を走査する
			return mTl.seekNextPoint(measure, tick, inclusiveFrom, chTester, outPoint);
		}

		/**
		 * タイムラインの指定楽曲位置を走査するストリームを返す
		 * @param measure 走査楽曲位置の小節番号
		 * @param tick 走査楽曲位置の小節の刻み位置
		 * @return タイムラインの指定楽曲位置を走査するストリーム
		 * @exception IllegalArgumentException 楽曲位置の小節番号がマイナス値
		 * @exception IllegalArgumentException 楽曲位置の小節の刻み位置が{@link BmsSpec#TICK_MIN}以外の時、小節番号が小節数以上
		 * @exception IllegalArgumentException 楽曲位置の小節の刻み位置が{@link BmsSpec#TICK_MIN}未満、または{@link BmsSpec#TICK_MAX}超過
		 */
		final Stream<BmsElement> timeline(int measure, double tick) {
			if ((mTl.getMeasureCount() == 0) && (measure == BmsSpec.MEASURE_MIN) && (tick == BmsSpec.TICK_MIN)) {
				// 空タイムラインでタイムライン先頭指定時は空ストリームを返す
				return Stream.empty();
			} else {
				// 楽曲位置のピンポイント指定でストリームを返す
				assertPoint(measure, tick);
				return mTl.timeline(measure, tick, measure, Math.nextUp(tick));
			}
		}

		/**
		 * タイムラインの指定楽曲位置の範囲を走査するストリームを返す
		 * @param measureBegin 走査開始楽曲位置の小節番号
		 * @param tickBegin 走査開始楽曲位置の小節の刻み位置
		 * @param measureEnd 走査終了楽曲位置の小節番号
		 * @param tickEnd 走査終了楽曲位置の小節の刻み位置(この位置を含まない)
		 * @return タイムラインの指定楽曲位置の範囲を走査するストリーム
		 * @exception IllegalArgumentException 走査開始/終了楽曲位置の小節番号がマイナス値
		 * @exception IllegalArgumentException 走査楽曲位置の小節の刻み位置が{@link BmsSpec#TICK_MIN}以外の時、小節番号が小節数以上
		 * @exception IllegalArgumentException 走査楽曲位置の小節の刻み位置が{@link BmsSpec#TICK_MIN}の時、小節番号が小節数超過
		 * @exception IllegalArgumentException 走査開始/終了楽曲位置の小節の刻み位置が{@link BmsSpec#TICK_MIN}未満、または{@link BmsSpec#TICK_MAX}超過
		 * @exception IllegalArgumentException 走査終了楽曲位置が走査開始楽曲位置と同じまたは手前の楽曲位置を示している
		 */
		final Stream<BmsElement> timeline(int measureBegin, double tickBegin, int measureEnd, double tickEnd) {
			// 空タイムラインで走査開始/終了楽曲位置がタイムライン先頭を指している場合は空ストリームを返す
			if (mTl.getMeasureCount() == 0) {
				if ((measureBegin == BmsSpec.MEASURE_MIN) && (tickBegin == BmsSpec.TICK_MIN) &&
						(measureEnd == BmsSpec.MEASURE_MIN) && (tickEnd == BmsSpec.TICK_MIN)) {
					return Stream.empty();
				}
			}

			// アサーション
			assertPoint(measureBegin, tickBegin);
			assertPointAllowTerm(measureEnd, tickEnd);
			if (BmsAt.compare2(measureBegin, tickBegin, measureEnd, tickEnd) >= 0) {
				var msg = String.format(
						"Wrong point range. beginAt=%s, endAt=%s",
						BmsPoint.of(measureBegin, tickBegin), BmsPoint.of(measureEnd, tickEnd));
				throw new IllegalArgumentException(msg);
			}

			// タイムライン走査ストリームを返す
			return mTl.timeline(measureBegin, tickBegin, measureEnd, tickEnd);
		}

		/**
		 * 楽曲位置を前方へオフセットする。
		 * @param measure 基点の小節番号
		 * @param tick 基点の刻み位置
		 * @param offset オフセットする刻み数
		 * @param outPoint オフセット後の楽曲位置を格納する楽曲位置オブジェクト
		 * @return 引数で指定した楽曲位置オブジェクト
		 */
		private BmsPoint forwardPoint(int measure, double tick, double offset, BmsPoint outPoint) {
			var curMeasure = measure;
			var curTick = tick;
			var remain = offset;
			while ((remain > 0) && (curMeasure < mTl.getMeasureCount())) {
				// 刻み位置の前進処理
				var measureData = mTl.getMeasureData(curMeasure);
				var tickCount = measureData.getTickCount();
				var forward = Math.min(tickCount - curTick, remain);
				remain -= forward;
				curTick += forward;

				// 当該小節の終端に到達した場合は次の小節へ移動する
				if (curTick >= tickCount) {
					curMeasure++;
					curTick = 0;
				}
			}

			outPoint.set(curMeasure, curTick);
			return outPoint;
		}

		/**
		 * 楽曲位置を後方へオフセットする。
		 * @param measure 基点の小節番号
		 * @param tick 基点の刻み位置
		 * @param offset オフセットする刻み数
		 * @param outPoint オフセット後の楽曲位置を格納する楽曲位置オブジェクト
		 * @return 引数で指定した楽曲位置オブジェクト
		 */
		private BmsPoint backwardPoint(int measure, double tick, double offset, BmsPoint outPoint) {
			var curMeasure = measure;
			var curTick = tick;
			var remain = offset;
			while (remain > 0) {
				// 当該小節の終端に到達している場合は次の小節へ移動する
				if (curTick <= 0) {
					if (curMeasure <= 0) {
						// 小節の先端に到達済み
						break;
					} else {
						// 前の小節へ移動する
						curMeasure--;
						curTick = mTl.getMeasureTickCount(curMeasure);
					}
				}

				// 刻み位置の後退処理
				var backward = Math.min(curTick, remain);
				remain -= backward;
				curTick -= backward;
			}

			outPoint.set(curMeasure, curTick);
			return outPoint;
		}

		/**
		 * 述語のテストが通過する最初のノートを検索する。
		 * @param measureFrom 検索開始小節番号
		 * @param tickFrom 検索開始刻み位置
		 * @param judge ノートをテストする述語
		 * @return 検索で見つかった最初のノート。見つからなかった場合はnull。
		 * @exception IllegalArgumentException ノート・小節データが存在し得ない小節番号
		 * @exception IllegalArgumentException 小節の刻み位置がマイナス値または当該小節の刻み数以上
		 * @exception NullPointerException judgeがnull
		 */
		final BmsNote pointOf(int measureFrom, double tickFrom, BmsNote.Tester judge) {
			assertPoint(measureFrom, tickFrom);
			assertArgNotNull(judge, "judge");

			// 判定処理がOKを返すNoteを探す処理
			return mTl.pointOf(measureFrom, tickFrom, judge);
		}

		/**
		 * 小節データ数を取得する。
		 * @return 小節データ数
		 */
		final int getCount() {
			return mTl.getMeasureCount();
		}

		/**
		 * 指定小節番号の小節の刻み数を取得する。
		 * @param measure 小節番号
		 * @return 当該小節の刻み数
		 * @exception IllegalArgumentException 小節番号が{@link BmsSpec#MEASURE_MIN}未満または{@link BmsSpec#MEASURE_MAX}超過
		 */
		final int getTickCount(int measure) {
			assertArgMeasureWithinRange(measure);
			return mTl.getMeasureTickCount(measure);
		}

		/**
		 * 時間(sec)を楽曲位置に変換する。
		 * <p>時間(sec)が総時間を超える場合はMeasure=小節数,Tick=0を返す。</p>
		 * @param timeSec 時間(sec)
		 * @param outPoint 変換後の楽曲位置を格納する楽曲位置オブジェクト
		 * @return 引数で指定した楽曲位置オブジェクト
		 * @exception IllegalStateException 動作モードが参照モードではない
		 * @exception IllegalArgumentException 指定時間がマイナス値
		 */
		final BmsPoint timeToPoint(double timeSec, BmsPoint outPoint) {
			// アサーション
			assertIsReferenceMode();
			assertArg(timeSec >= 0.0, "Argument 'timeSec' is minus value. actual=%f", timeSec);

			// 当該時間が何小節目の範囲にあるかを検出する
			var measureCount = mTl.getMeasureCount();
			MeasureElement measureElem = null;
			for (int i = 0; i < measureCount; i++) {
				var m = mTl.getMeasureData(i);
				var baseTime = m.getBaseTime();
				if ((timeSec >= baseTime) && (timeSec < (baseTime + m.getLength()))) {
					// 当該時間の収まる小節を発見した
					measureElem = m;
					break;
				}
			}
			if (measureElem == null) {
				// 範囲外の時間を入力した場合は最終小節+1, tikc=0の位置を返す
				outPoint.set(measureCount, 0);
				return outPoint;
			}

			// 小節長の倍率を計算する
			var measure = measureElem.getMeasure();
			var lengthRatio = 1.0;
			var lengthChannel = mSpec.getLengthChannel();
			if (lengthChannel != null) {
				lengthRatio = (double)getValue(lengthChannel.getNumber(), 0, measure, BmsType.NUMERIC, false);
			}

			// データ無し小節の場合の処理
			var tickCount = (double)measureElem.getTickCount();
			var actualTickCount = BmsSpec.computeTickCount(lengthRatio, false);
			var pointTime = timeSec - measureElem.getBaseTime();
			if (mTl.isMeasureEmptyNotes(measure)) {
				if (actualTickCount >= 1.0) {
					// 通常の長さの小節の場合
					var tick = calculateTick(pointTime, measureElem.getBeginBpm());
					outPoint.set(measure, tick);
					return outPoint;
				} else {
					// 計算上の刻み数が1を下回る極小小節の場合
					var tick = pointTime / measureElem.getLength();
					outPoint.set(measure, tick);
					return outPoint;
				}
			}

			// データ有り小節の場合の処理
			var tickFindStart = 0.0;
			var currentTick = 0.0;
			var tickCur2Pt = 0.0;
			var currentBpm = measureElem.getBeginBpm();
			var currentTime = 0.0;
			var areaTime = 0.0;

			while (currentTick < tickCount) {
				// 現在位置を基点にして次のBPM変更Noteを取得する
				var nextBpmNote = getNextBpmNoteInMeasure(measure, tickFindStart);
				var nextBpmTick = (nextBpmNote == null) ? tickCount : nextBpmNote.getTick();

				// 現在位置を基点にして次の譜面停止Noteを取得する
				var nextStopNote = getNextStopNoteInMeasure(measure, tickFindStart);
				var nextStopTick = (nextStopNote == null) ? tickCount : nextStopNote.getTick();

				// 時間計算処理
				if ((nextBpmTick <= nextStopTick) && (nextBpmNote != null)) {
					// BPM変更を検出した場合は、現在位置からBPM変更位置までを現在BPMで時間計算する
					areaTime = calculateTime(nextBpmTick - currentTick, currentBpm);
					if ((pointTime >= currentTime) && (pointTime < (currentTime + areaTime))) {
						// 指定位置までの刻み数を計算する
						tickCur2Pt = calculateTick(pointTime - currentTime, currentBpm);
						break;
					} else {
						// BPMの現在値を更新する
						currentBpm = mMetas.getIndexedMetaBpm(nextBpmNote, currentBpm);
						currentTime += areaTime;
					}

					// 同じ位置に譜面停止が存在する場合は、更新後のBPMで譜面停止分の時間を計算する
					if ((nextBpmTick == nextStopTick) && (nextStopNote != null)) {
						var stopTick = mMetas.getIndexedMetaStop(nextStopNote, 0);
						areaTime = calculateTime(stopTick, currentBpm);
						if ((pointTime >= currentTime) && (pointTime < (currentTime + areaTime))) {
							// 指定位置までの刻み数を計算する
							tickCur2Pt = calculateTick(pointTime - currentTime, currentBpm);
							break;
						} else {
							currentTime += areaTime;
						}
					}

					// 現在位置を更新する
					currentTick = nextBpmTick;
				} else if (nextStopNote != null) {
					// 譜面停止を検出した場合は、現在位置から譜面停止位置までを現在BPMで時間計算する
					areaTime = calculateTime(nextStopTick - currentTick, currentBpm);
					if ((pointTime >= currentTime) && (pointTime < (currentTime + areaTime))) {
						// 指定位置までの刻み数を計算する
						tickCur2Pt = calculateTick(pointTime - currentTime, currentBpm);
						break;
					} else {
						currentTime += areaTime;
					}

					// 譜面停止分の時間を現在BPMで計算する
					var stopTick = mMetas.getIndexedMetaStop(nextStopNote, 0);
					areaTime = calculateTime(stopTick, currentBpm);
					if ((pointTime >= currentTime) && (pointTime < (currentTime + areaTime))) {
						// 指定位置までの刻み数を計算する
						tickCur2Pt = calculateTick(pointTime - currentTime, currentBpm);
						break;
					} else {
						currentTime += areaTime;
					}

					// 現在位置を更新する
					currentTick = nextStopTick;
				} else {
					// 現在位置からBPM変更・譜面停止のいずれも見つからなかった場合は、現在位置から小節の最後まで時間計算する
					areaTime = calculateTime(actualTickCount - currentTick, currentBpm);
					if ((pointTime >= currentTime) && (pointTime < (currentTime + areaTime))) {
						// 指定位置までの刻み数を計算する
						if (actualTickCount >= 1.0) {
							// 通常の長さの小節の場合
							tickCur2Pt = calculateTick(pointTime - currentTime, currentBpm);
						} else {
							// 計算上の刻み数が1を下回る極小小節の場合
							tickCur2Pt = pointTime / measureElem.getLength();
						}
						break;
					} else {
						currentTime += areaTime;
					}

					// 現在位置を更新する
					currentTick = tickCount;
				}

				// 次の検索開始位置を設定する
				tickFindStart = Math.nextUp(currentTick);
			}

			outPoint.set(measure, (double)currentTick + tickCur2Pt);
			return outPoint;
		}

		/**
		 * 楽曲位置を時間(sec)に変換する。
		 * @param point 楽曲位置
		 * @return 時間(sec)
		 * @exception IllegalStateException 動作モードが参照モードではない
		 * @exception NullPointerException pointがnull
		 * @exception IllegalArgumentException 小節番号がマイナス値、小節数以上、または小節数と同値で刻み位置が0以外
		 * @exception IllegalArgumentException 小節の刻み位置がマイナス値または当該小節の刻み数以上
		 */
		final double pointToTime(BmsAt point) {
			// アサーション
			assertIsReferenceMode();
			assertArgNotNull(point, "point");
			assertPointAllowTerm(point.getMeasure(), point.getTick());

			// 小節の末端の場合は総時間を返す
			var measureCount = mTl.getMeasureCount();
			if (point.getMeasure() == measureCount) {
				if (measureCount == 0) {
					// データ無し
					return 0.0;
				} else {
					// 最終小節の開始時間＋長さを返す
					var lastMeasure = mTl.getMeasureData(measureCount - 1);
					return lastMeasure.getBaseTime() + lastMeasure.getLength();
				}
			}

			// 小節・刻み位置から時間への変換を行う
			calculateMeasureTimeInfo(point.getMeasure(), point.getTick(), mTempBaseTime, mTempLengthSec, mTempBeginBpm, mTempEndBpm);
			return mTempBaseTime.get() + mTempLengthSec.get();
		}

		/**
		 * 指定ノートに該当するメタ情報の値を取得する。
		 * @param channel チャンネル番号
		 * @param index チャンネルインデックス
		 * @param measure 小節番号
		 * @param tick 小節の刻み位置
		 * @return ノートに該当するメタ情報の値。チャンネルに参照先メタ情報の無い場合はノートの値をINTEGERで返す。
		 * @exception IllegalArgumentException BMS仕様に無いチャンネル番号
		 * @exception IndexOutOfBoundsException チャンネルインデックスがマイナス値
		 * @exception IndexOutOfBoundsException チャンネルデータ重複不許可のチャンネルで0以外のインデックスを指定
		 * @exception IllegalArgumentException 小節番号が{@link BmsSpec#MEASURE_MIN}未満または{@link BmsSpec#MEASURE_MAX}超過
		 * @exception IllegalArgumentException 小節の刻み位置がマイナス値または当該小節の刻み数以上
		 * @exception IllegalArgumentException 指定チャンネルのデータ型が配列型ではない
		 */
		final Object getResolvedNoteValue(int channel, int index, int measure, double tick) {
			var note = getNote(channel, index, measure, tick);
			if (note != null) {
				// ノートを取得できた場合はそのノートの値を使用してメタ情報の値を抽出する
				return getResolvedNoteValueCore(note.getChannel(), note.getValue());
			} else {
				// ノートを取得できなかった場合は指定チャンネルの初期値を返す
				var info = mMetas.getReferenceMeta(channel);
				return (info == null) ? (Long)0L : info.meta.getDefaultValue();
			}
		}

		/**
		 * 指定ノートに該当するメタ情報の値を取得する。
		 * @param note ノート
		 * @return ノートに該当するメタ情報の値。チャンネルに参照先メタ情報の無い場合はノートの値をINTEGERで返す。
		 * @exception NullPointerException noteがnull
		 */
		final Object getResolvedNoteValue(BmsNote note) {
			assertArgNotNull(note, "note");
			return getResolvedNoteValueCore(note.getChannel(), note.getValue());
		}

		/**
		 * 指定小節の基準時間(sec)、小節時間(sec)、開始時BPM、終了時BPMを算出する。
		 * <p>このメソッドでは当該小節の諸々の情報を計算するにあたり、指定小節の1つ前の小節の情報を参照するため、
		 * 前の小節の情報は予め計算しておくこと。</p>
		 * @param measure 小節番号
		 * @param tickCalcTo どの刻み位置までの時間を計算するか(この位置は含まない)
		 * @param outBaseTime 小節の基準時間(sec)
		 * @param outLengthSec 小節の先頭からtickCalcToまでの時間(sec)
		 * @param outBeginBpm 小節開始時のBPM
		 * @param outEndBpm 小節終了時のBPM
		 */
		void calculateMeasureTimeInfo(int measure, double tickCalcTo,
				MutableDouble outBaseTime, MutableDouble outLengthSec, MutableDouble outBeginBpm, MutableDouble outEndBpm) {
			// 指定小節における基準時間、初期BPMを決定する
			var baseTimeSec = 0.0;
			var currentBpm = 0.0;
			var prevMeasureData = (measure == 0) ? null : mTl.getMeasureData(measure - 1);
			if (prevMeasureData == null) {
				// 先頭小節の場合
				baseTimeSec = 0.0;
				currentBpm = getInitialBpm();
			} else {
				// 先頭小節以降の場合
				baseTimeSec = prevMeasureData.getBaseTime() + prevMeasureData.getLength();
				currentBpm = prevMeasureData.getEndBpm();
			}

			// 小節長の倍率を計算する
			var lengthRatio = 1.0;
			var lengthChannel = mSpec.getLengthChannel();
			if (lengthChannel != null) {
				lengthRatio = (double)getValue(lengthChannel.getNumber(), 0, measure, BmsType.NUMERIC, false);
			}

			// データの無い小節では単純に小節の長さと小節開始時のBPMから時間を算出する
			var tickCount = BmsSpec.computeTickCount(lengthRatio, true);
			var actualTickCount = BmsSpec.computeTickCount(lengthRatio, false);
			if (mTl.isMeasureEmptyNotes(measure)) {
				outBaseTime.set(baseTimeSec);
				outLengthSec.set(calculateTime(Math.min(tickCalcTo, actualTickCount), currentBpm));
				outBeginBpm.set(currentBpm);
				outEndBpm.set(currentBpm);
				return;
			}

			// 譜面データから具体的な時間を計算する
			var currentTick = 0.0;
			var beginBpm = currentBpm;
			var totalTime = 0.0;

			var tickFindStart = 0.0;
			var tickEnd = Math.min(tickCalcTo, tickCount);
			var actualTickEnd = Math.min(tickCalcTo, actualTickCount);
			while (currentTick < tickEnd) {
				// 現在位置を基点にして次のBPM変更Noteを取得する
				var nextBpmNote = getNextBpmNoteInMeasure(measure, tickFindStart);
				var nextBpmTick = (nextBpmNote == null) ? tickEnd : nextBpmNote.getTick();

				// 現在位置を基点にして次の譜面停止Noteを取得する
				var nextStopNote = getNextStopNoteInMeasure(measure, tickFindStart);
				var nextStopTick = (nextStopNote == null) ? tickEnd : nextStopNote.getTick();

				// 時間計算処理
				if ((nextBpmTick <= nextStopTick) && (nextBpmNote != null) && (nextBpmTick < tickEnd)) {
					// BPM変更を検出した場合は、現在位置からBPM変更位置までを現在BPMで時間計算する
					totalTime += calculateTime(nextBpmTick - currentTick, currentBpm);
					currentBpm = mMetas.getIndexedMetaBpm(nextBpmNote, currentBpm);

					// 同じ位置に譜面停止が存在する場合は、更新後のBPMで譜面停止分の時間を計算する
					if ((nextBpmTick == nextStopTick) && (nextStopNote != null)) {
						var stopTick = mMetas.getIndexedMetaStop(nextStopNote, 0);
						totalTime += calculateTime(stopTick, currentBpm);
					}

					// 現在位置を更新する
					currentTick = nextBpmTick;
				} else if ((nextStopNote != null) && (nextStopTick < tickEnd)) {
					// 譜面停止を検出した場合は、現在位置から譜面停止位置までを現在BPMで時間計算する
					// 但し、刻み位置が走査停止位置と同じ以上の場合はこの限りではない
					totalTime += calculateTime(nextStopTick - currentTick, currentBpm);

					// 譜面停止分の時間を現在BPMで計算する
					var stopTick = mMetas.getIndexedMetaStop(nextStopNote, 0);
					totalTime += calculateTime(stopTick, currentBpm);

					// 現在位置を更新する
					currentTick = nextStopTick;
				} else {
					// 現在位置からBPM変更・譜面停止のいずれも見つからなかった場合は、現在位置から計算位置まで時間計算する
					totalTime += calculateTime(actualTickEnd - currentTick, currentBpm);
					currentTick = tickEnd;
				}

				// 次の検索開始位置を設定する
				tickFindStart = Math.nextUp(currentTick);
			}

			// 計算結果を出力する
			outBaseTime.set(baseTimeSec);
			outLengthSec.set(totalTime);
			outBeginBpm.set(beginBpm);
			outEndBpm.set(currentBpm);
		}

		/**
		 * 指定BPMの時、指定刻みだけ進行するのに必要になる時間(sec)を計算する。
		 * @param tick 刻み数
		 * @param bpm BPM
		 * @return 計算結果
		 */
		private double calculateTime(double tick, double bpm) {
			return 1.25 * tick / bpm;

			// 計算式の解説
			// time(sec) = 1.25 * tick / bpm  ※bpm > 0
			// bpm = (1.25 * tick) / time(sec)
		}

		/**
		 * 指定BPMの時、指定時間(sec)進行した際の刻み数を計算する。(無段階の値として返す)
		 * @param timeSec 時間(sec)
		 * @param bpm BPM
		 * @return 計算結果
		 */
		private double calculateTick(double timeSec, double bpm) {
			// 注意！：bpm > 0
			// 間違い：return timeSec * (1.0 / 48.0) * (60.0 / bpm);  // time(sc), bpmからtickへ変換
			return 0.8 * timeSec * bpm;

			// 計算式の解説
			// time = (tick / 48) * (60 / bpm)
			// time = (60 * tick) / (48 * bpm)
			// time = (5 * tick) / (4 * bpm)
			// (5 * tick) = time * (4 * bpm)
			// tick = (4 * time * bpm) / 5
			// tick = 0.8 * time * bpm ★
		}

		/**
		 * 指定小節内で刻み位置より後ろにあるBPM変更ノート取得
		 * @param measure 小節番号
		 * @param tickFrom 検索開始小節の刻み位置
		 * @return 次のBPM変更ノート、なければnull
		 */
		private BmsNote getNextBpmNoteInMeasure(int measure, double tickFrom) {
			// 指定楽曲位置より後ろで同一小節内にあるBPM変更ノートを返す
			var bpmNote = (BmsNote)null;
			var channelCount = mSpec.getBpmChannelCount();
			for (var i = 0; i < channelCount; i++) {
				var bpmChannel = mSpec.getBpmChannel(i, true);
				var nextNote = mTl.getNextNote(bpmChannel.getNumber(), 0, measure, tickFrom, true);
				if ((nextNote != null) && (nextNote.getMeasure() == measure)) {
					// 同一小節内のBPM変更ノートを発見した
					if (bpmNote == null) {
						// 最初に見つかったノート
						bpmNote = nextNote;
					} else {
						// 2個目以降に見つかった場合、より手前で見つかったノートを採用する
						// 同一刻み位置だった場合は後で追加されたチャンネルを優先して採用する
						bpmNote = (nextNote.getTick() <= bpmNote.getTick()) ? nextNote : bpmNote;
					}
				}
			}
			return bpmNote;
		}

		/**
		 * 指定小節内で刻み位置より後ろにある譜面停止ノート取得
		 * @param measure 小節番号
		 * @param tickFrom 検索開始小節の刻み位置
		 * @return 次の譜面停止ノート、なければnull
		 */
		private BmsNote getNextStopNoteInMeasure(int measure, double tickFrom) {
			// 指定楽曲位置より後ろで同一小節内にある譜面停止ノートを返す
			var stopNote = (BmsNote)null;
			var channelCount = mSpec.getStopChannelCount();
			for (var i = 0; i < channelCount; i++) {
				var stopChannel = mSpec.getStopChannel(i, true);
				var nextNote = mTl.getNextNote(stopChannel.getNumber(), 0, measure, tickFrom, true);
				if ((nextNote != null) && (nextNote.getMeasure() == measure)) {
					// 同一小節内の譜面停止ノートを発見した
					if (stopNote == null) {
						// 最初に見つかったノート
						stopNote = nextNote;
					} else {
						// 2個目以降に見つかった場合、より手前で見つかったノートを採用する
						// 同一刻み位置だった場合は後で追加されたチャンネルを優先して採用する
						stopNote = (nextNote.getTick() <= stopNote.getTick()) ? nextNote : stopNote;
					}
				}
			}
			return stopNote;
		}

		/**
		 * 指定ノートに該当するメタ情報の値を取得する。
		 * @param channel チャンネル番号
		 * @param value ノートの値
		 * @return 指定ノートに該当するメタ情報の値。参照先メタ情報の該当インデックスに値がない場合、
		 *          そのメタ情報の初期値を返し、参照先メタ情報のないチャンネルの場合Long型のノートの値。
		 */
		private Object getResolvedNoteValueCore(int channel, int value) {
			// 参照先メタ情報が存在しないチャンネルの場合は当該ノートの値をINTEGER形式で返す
			var valObj = BmsInt.box(value);
			var info = mMetas.getReferenceMeta(channel);
			if (info == null) {
				return BmsType.INTEGER.cast(valObj);
			}

			// 参照先メタ情報に当該データが存在する場合はその値を、無い場合はメタ情報の初期値を返す
			var resolvedValue = info.valueList.get(valObj);
			if (resolvedValue == null) {
				resolvedValue = info.meta.getDefaultValue();
			}

			return resolvedValue;
		}

		/**
		 * チャンネル番号とチャンネルインデックスの整合性をテストするアサーション
		 * @param channel チャンネル番号
		 * @param index チャンネルインデックス
		 * @exception IllegalArgumentException BMS仕様に無いチャンネル番号
		 * @exception IndexOutOfBoundsException チャンネルインデックスがマイナス値
		 * @exception IndexOutOfBoundsException チャンネルデータ重複不許可のチャンネルで0以外のインデックスを指定
		 */
		private void assertChannelIndex(int channel, int index) {
			var channelSpec = mSpec.getChannel(channel);
			if (channelSpec == null) {
				throw new IllegalArgumentException(String.format("Channel number %d no such channel.", channel));
			}
			var isMultiple = channelSpec.isMultiple();
			if (!BmsSpec.isChIndexWithinRange(index, isMultiple)) {
				var msg = String.format(
						"Channel index is out of range. channel=%d, index=%d, multiple=%s",
						channel, index, isMultiple);
				throw new IndexOutOfBoundsException(msg);
			}
		}

		/**
		 * チャンネルが配列型であることをテストするアサーション
		 * @param channel チャンネル番号
		 * @exception  チャンネルが配列型ではない
		 */
		private void assertChannelArrayType(int channel) {
			var ch = mSpec.getChannel(channel);
			if (!ch.isArrayType()) {
				var msg = String.format("Channel is NOT array type. channel=%s", ch);
				throw new IllegalArgumentException(msg);
			}
		}

		/**
		 * チャンネルが配列型ではないことをテストするアサーション
		 * @param channel チャンネル番号
		 * @exception IllegalArgumentException チャンネルが配列型
		 */
		private void assertChannelNotArrayType(int channel) {
			var ch = mSpec.getChannel(channel);
			if (ch.isArrayType()) { // Value/ObjectであればOK
				var msg = String.format("Channel is NOT value type. channel=%s", ch);
				throw new IllegalArgumentException(msg);
			}
		}

		/**
		 * 小節番号と小節の刻み位置の整合性をテストするアサーション。
		 * <p>当メソッドでのアサーションは、小節番号が小節数未満であることを要求する。</p>
		 * @param measure 小節番号
		 * @param tick 小節の刻み位置
		 * @exception IllegalArgumentException 小節番号がマイナス値または小節数以上
		 * @exception IllegalArgumentException 小節の刻み位置がマイナス値または当該小節の刻み数以上
		 */
		private void assertPoint(int measure, double tick) {
			assertArgMeasureWithinRange(measure, mTl.getMeasureCount() - 1, tick);
			assertArgTickWithinRange(tick, mTl.getMeasureTickMax(measure), measure);
		}

		/**
		 * 小節番号と小節の刻み位置の整合性をテストするアサーション。
		 * <p>小節番号が小節数と同じ場合、刻み位置が0であることを要求する。</p>
		 * <p>当メソッドでのアサーションは、小節番号が小節数の範囲内であることを要求する。</p>
		 * @param measure 小節番号
		 * @param tick 小節の刻み位置
		 * @exception IllegalArgumentException 小節番号がマイナス値、小節数以上、または小節数と同値で刻み位置が0以外
		 * @exception IllegalArgumentException 小節の刻み位置がマイナス値または当該小節の刻み数以上
		 */
		private void assertPointAllowTerm(int measure, double tick) {
			var measureMax = mTl.getMeasureCount();
			assertArgMeasureWithinRange(measure, measureMax, tick);
			var tickMax = (measure == measureMax) ? BmsSpec.TICK_MIN : mTl.getMeasureTickMax(measure);
			assertArgTickWithinRange(tick, tickMax, measure);
		}

		/**
		 * 小節番号と小節の刻み位置の整合性をテストするアサーション。
		 * @param measure 小節番号
		 * @param tick 小節の刻み位置
		 * @exception IllegalArgumentException 小節番号が{@link BmsSpec#MEASURE_MIN}未満または{@link BmsSpec#MEASURE_MAX}超過
		 * @exception IllegalArgumentException 小節の刻み位置がマイナス値または当該小節の刻み数以上
		 */
		private void assertPointAllowOverMeasureCount(int measure, double tick) {
			assertArgMeasureWithinRange(measure, BmsSpec.MEASURE_MAX, tick);
			assertArgTickWithinRange(tick, mTl.getMeasureTickMax(measure), measure);
		}
	}

	/**
	 * 参照先メタ情報に関する情報を表す。
	 */
	private static class ReferenceMetaInfo {
		/** メタ情報(必ず索引付き)の仕様 */
		BmsMeta meta;
		/** このメタ情報の値リスト */
		HashMap<Integer, Object> valueList;
	}

	/**
	 * メタ情報を格納するコンテナの集合
	 * <p>データ単位ごとに別々のデータ型でリスト化し、メタ情報のデータを管理している。基本的にはメタ情報の名称を
	 * キーにして情報を検索できるようになっている。その他、チャンネル番号による参照先メタ情報の検索も行うことが
	 * できる。</p>
	 */
	private class MetaContainer {
		/** 単体メタ情報マップ */
		private TreeMap<String, Object> mSingleMetas = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		/** 複数メタ情報マップ */
		private TreeMap<String, ArrayList<Object>> mMultipleMetas = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		/** 索引付きメタ情報マップ */
		private TreeMap<String, HashMap<Integer, Object>> mIndexedMetas = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		/** 参照メタ情報マップ(Valueに入るインスタンスは名称によるマップと共有する) */
		private HashMap<Integer, ReferenceMetaInfo> mReferenceMetas = new HashMap<>();

		/** BPM変更用のメタ情報マップ */
		private HashMap<Integer, HashMap<Integer, Object>> mBpmMetas = new HashMap<>();
		/** 譜面停止用のメタ情報マップ */
		private HashMap<Integer, HashMap<Integer, Object>> mStopMetas = new HashMap<>();

		/**
		 * メタ情報コンテナをセットアップする。
		 */
		final void setup() {
			// メタ情報マップに空コンテナを生成する
			mSpec.getMetas(m -> true, BmsMeta.COMPARATOR_BY_ORDER).forEach(meta -> {
				switch (meta.getUnit()) {
				case SINGLE:
					mSingleMetas.put(meta.getName(), null);
					break;
				case MULTIPLE:
					mMultipleMetas.put(meta.getName(), new ArrayList<>());
					break;
				case INDEXED:
					mIndexedMetas.put(meta.getName(), new HashMap<>());
					break;
				}
			});

			// チャンネル番号による参照先メタ情報のマップを生成する
			mSpec.getChannels().forEach(channel -> {
				var ref = channel.getRef();
				if (ref != null) {
					var info = new ReferenceMetaInfo();
					info.meta = mSpec.getIndexedMeta(ref);
					info.valueList = mIndexedMetas.get(ref);
					mReferenceMetas.put(BmsInt.box(channel.getNumber()), info);
				}
			});

			// BPM変更用メタ情報の参照を取り出しておく
			for (var i = 0; i < mSpec.getBpmChannelCount(); i++) {
				var bpmChannel = mSpec.getBpmChannel(i, true);
				var refMeta = mReferenceMetas.get(BmsInt.box(bpmChannel.getNumber()));
				if (refMeta != null) { mBpmMetas.put(BmsInt.box(bpmChannel.getNumber()), refMeta.valueList); }
			}

			// 譜面停止用メタ情報の参照を取り出しておく
			for (var i = 0; i < mSpec.getStopChannelCount(); i++) {
				var stopChannel = mSpec.getStopChannel(i, true);
				var refMeta = mReferenceMetas.get(BmsInt.box(stopChannel.getNumber()));
				if (refMeta != null) { mStopMetas.put(BmsInt.box(stopChannel.getNumber()), refMeta.valueList); }
			}
		}

		/**
		 * 単体メタ情報の値を設定する
		 * @param name メタ情報の名称
		 * @param value 設定する値。nullを指定するとその名前のメタ情報を削除する。
		 * @exception IllegalStateException 動作モードが編集モードではない
		 * @exception NullPointerException nameがnull
		 * @exception IllegalArgumentException 名称に合致するメタ情報が存在しない
		 * @exception IllegalArgumentException 初期BPM指定時、BPMが{@link BmsSpec#BPM_MIN}～{@link BmsSpec#BPM_MAX}の範囲外
		 * @exception ClassCastException valueが設定先メタ情報のデータ型に変換できない
		 */
		final void setSingleMeta(String name, Object value) {
			// アサーション
			assertIsEditMode();
			assertArgMetaName(name);

			// メタ情報のアサーション
			var meta = assertMetaSpec(name, BmsUnit.SINGLE);

			// メタ情報の追加・削除を行う(valueがnullの場合"未設定"として扱う)
			if (value == null) {
				// 削除
				mSingleMetas.put(name, null);
			} else {
				// 追加
				var castedValue = meta.getType().cast(value);
				assertValueWithinSpec(meta, 0, castedValue);
				mSingleMetas.put(name, castedValue);
			}

			// 初期BPMを更新した場合は譜面全体を時間再計算対象とする
			if (meta.isInitialBpm()) {
				mTlAccessor.updateRecalcRange(0, BmsSpec.MEASURE_MAX);
			}
		}

		/**
		 * 重複可能メタ情報の値を設定する。
		 * @param name メタ情報の名称
		 * @param index インデックス
		 * @param value 設定する値。nullを指定するとその名前のメタ情報を削除する。
		 * @exception IllegalStateException 動作モードが編集モードではない
		 * @exception NullPointerException nameがnull
		 * @exception IllegalArgumentException 名称に合致するメタ情報が存在しない
		 * @exception IndexOutOfBoundsException indexが0～{@link BmsSpec#MULTIPLE_META_INDEX_MAX}の範囲外
		 * @exception ClassCastException valueが設定先メタ情報のデータ型に変換できない
		 */
		final void setMultipleMeta(String name, int index, Object value) {
			// アサーション
			assertIsEditMode();
			assertArgMetaName(name);
			assertArgMultipleMetaIndex(index);

			// メタ情報の追加・削除を行う
			var meta = assertMetaSpec(name, BmsUnit.MULTIPLE);
			var metaList = mMultipleMetas.get(name);
			if (value == null) {
				// 削除
				if (index < metaList.size()) {
					// リストから当該データを削除する
					metaList.remove(index);
				}
			} else {
				// 追加
				var castedValue = meta.getType().cast(value);
				if (index < metaList.size()) {
					// 既存要素を更新する
					metaList.set(index, castedValue);
				} else {
					// 新規領域に値を登録する(登録領域までの埋め合わせは初期値を使用する)
					var defaultValue = meta.getDefaultValue();
					for (var i = metaList.size(); i <= index; i++) {
						metaList.add((i == index) ? castedValue : defaultValue);
					}
				}
			}

			// 重複可能メタ情報では時間再計算対象として使われるデータが存在しないため
			// 何もチェックしない
		}

		/**
		 * 索引付きメタ情報の値を設定する。
		 * @param name メタ情報の名称
		 * @param index インデックス
		 * @param value 設定する値。nullを指定するとその名前のメタ情報を削除する。
		 * @exception IllegalStateException 動作モードが編集モードではない
		 * @exception NullPointerException nameがnull
		 * @exception IllegalArgumentException 名称に合致するメタ情報が存在しない
		 * @exception IndexOutOfBoundsException indexが0～{@link BmsSpec#INDEXED_META_INDEX_MAX}の範囲外
		 * @exception ClassCastException valueが設定先メタ情報のデータ型に変換できない
		 */
		final void setIndexedMeta(String name, int index, Object value) {
			// アサーション
			assertIsEditMode();
			assertArgMetaName(name);
			assertArgIndexedMetaIndex(index);

			// メタ情報の追加・削 除を行う
			var meta = assertMetaSpec(name, BmsUnit.INDEXED);
			var metaMap = mIndexedMetas.get(name);
			if (value == null) {
				// 削除
				metaMap.remove(index);
			} else {
				// 追加
				var castedValue = meta.getType().cast(value);
				assertValueWithinSpec(meta, index, castedValue);
				metaMap.put(index, castedValue);
			}

			// BPMまたはSTOPのデータ更新を行った場合は譜面全体を時間再計算対象とする
			if (meta.isReferenceBpm() || meta.isReferenceStop()) {
				// BPM変更または譜面停止のデータ更新を行ったので譜面全体を時間再計算対象とする
				mTlAccessor.updateRecalcRange(0, BmsSpec.MEASURE_MAX);
			}
		}

		/**
		 * 指定名称・単位・インデックスに該当するメタ情報の値を設定する。
		 * @param name メタ情報の名称
		 * @param unit メタ情報の単位
		 * @param index インデックス
		 * @param value 設定する値。nullを指定するとその名前のメタ情報を削除する。
		 * @exception IllegalStateException 動作モードが編集モードではない
		 * @exception NullPointerException nameがnull
		 * @exception NullPointerException unitがnull
		 * @exception IllegalArgumentException name, unitに合致するメタ情報が存在しない
		 * @exception IndexOutOfBoundsException unitが{@link BmsUnit#SINGLE SINGLE}の時、indexが0以外
		 * @exception IndexOutOfBoundsException unitが{@link BmsUnit#MULTIPLE MULTIPLE}の時、indexが0～{@link BmsSpec#MULTIPLE_META_INDEX_MAX}の範囲外
		 * @exception IndexOutOfBoundsException unitが{@link BmsUnit#INDEXED INDEXED}の時、indexが0～{@link BmsSpec#INDEXED_META_INDEX_MAX}の範囲外
		 * @exception ClassCastException valueが設定先メタ情報のデータ型に変換できない
		 */
		final void setMeta(String name, BmsUnit unit, int index, Object value) {
			// 単位により処理を分岐
			assertArgMetaUnit(unit);
			switch (unit) {
			case SINGLE:
				assertArgSingleMetaIndex(index);
				setSingleMeta(name, value);
				break;
			case MULTIPLE:
				setMultipleMeta(name, index, value);
				break;
			case INDEXED:
				setIndexedMeta(name, index, value);
				break;
			default:
				throw new IllegalArgumentException("Wrong meta unit.");
			}
		}

		/**
		 * 単体メタ情報の値を取得する。
		 * @param name メタ情報の名称
		 * @return メタ情報の値。値が未設定の場合、そのメタ情報の初期値。
		 * @exception NullPointerException nameがnull
		 * @exception IllegalArgumentException 名称に合致するメタ情報が存在しない
		 */
		final Object getSingleMeta(String name) {
			// アサーション
			assertArgMetaName(name);

			// メタ情報を取得する
			var meta = assertMetaSpec(name, BmsUnit.SINGLE);
			var value = mSingleMetas.get(name);
			return (value == null) ? meta.getDefaultValue() : value;
		}

		/**
		 * 重複可能メタ情報の値を取得する。
		 * @param name メタ情報の名称
		 * @param index インデックス
		 * @return メタ情報の値。値が未設定の場合、そのメタ情報の初期値。
		 * @exception NullPointerException nameがnull
		 * @exception IndexOutOfBoundsException indexが0～{@link BmsSpec#MULTIPLE_META_INDEX_MAX}の範囲外
		 */
		final Object getMultipleMeta(String name, int index) {
			// アサーション
			assertArgMetaName(name);
			assertArgMultipleMetaIndex(index);

			// メタ情報を取得する
			var meta = assertMetaSpec(name, BmsUnit.MULTIPLE);
			var valueList = mMultipleMetas.get(name);
			var value = (index < valueList.size()) ? valueList.get(index) : null;
			return (value == null) ? meta.getDefaultValue() : value;
		}

		/**
		 * 索引付きメタ情報の値を取得する。
		 * @param name メタ情報の名称
		 * @param index インデックス
		 * @return メタ情報の値。値が未設定の場合、そのメタ情報の初期値。
		 * @exception NullPointerException nameがnull
		 * @exception IndexOutOfBoundsException indexが0～{@link BmsSpec#INDEXED_META_INDEX_MAX}の範囲外
		 * @exception IllegalArgumentException 名称に合致するメタ情報が存在しない
		 */
		final Object getIndexedMeta(String name, int index) {
			// アサーション
			assertArgMetaName(name);
			assertArgIndexedMetaIndex(index);

			// メタ情報を取得する
			var meta = assertMetaSpec(name, BmsUnit.INDEXED);
			var valueMap = mIndexedMetas.get(name);
			var value = valueMap.get(index);
			return (value == null) ? meta.getDefaultValue() : value;
		}

		/**
		 * 索引付きメタ情報からBPM変更で使用するBPMの値を取得する。
		 * @param note 取得対象のノート
		 * @param bpmIfNotExist ノートの値に該当するBPMが見つからなかった時に返す代替値
		 * @return BPM
		 */
		final double getIndexedMetaBpm(BmsNote note, double bpmIfNotExist) {
			if (mSpec.getBpmChannel(note.getChannel()) == null) {
				// BPM変更チャンネル非対応
				return bpmIfNotExist;
			}
			var bpmMetas = mBpmMetas.get(BmsInt.box(note.getChannel()));
			if (bpmMetas == null) {
				// 参照先メタ情報無しの場合はノートの値をBPMとする
				return note.getValue();
			}

			// 参照先メタ情報から値を取得する
			var value = bpmMetas.get(BmsInt.box(note.getValue()));
			if (value == null) {
				// 参照先メタ情報に定義が無い
				return bpmIfNotExist;
			} else {
				// 参照先メタ情報の値を返す
				return ((Number)value).doubleValue();
			}
		}

		/**
		 * 索引付きメタ情報から譜面停止で使用する停止時間の値を取得する。
		 * @param note 取得対象のノート
		 * @param stopIfNotExist ノートの値に該当する停止時間が見つからなかった時に返す代替値
		 * @return 停止時間
		 */
		final double getIndexedMetaStop(BmsNote note, double stopIfNotExist) {
			if (mSpec.getStopChannel(note.getChannel()) == null) {
				// 譜面停止チャンネル非対応
				return stopIfNotExist;
			}
			var stopMetas = mStopMetas.get(BmsInt.box(note.getChannel()));
			if (stopMetas == null) {
				// 参照先メタ情報無しの場合はNoteの値を停止時間とする
				return note.getValue();
			}

			// 参照先メタ情報から値を取得する
			var value = stopMetas.get(BmsInt.box(note.getValue()));
			if (value == null) {
				// 参照先メタ情報に定義が無い
				return stopIfNotExist;
			} else {
				// 参照先メタ情報の値を返す
				return ((Number)value).doubleValue();
			}
		}

		/**
		 * 指定した名称・単位・インデックスに該当するメタ情報の値を取得する。
		 * @param name メタ情報の名称
		 * @param unit メタ情報の単位
		 * @param index インデックス
		 * @return メタ情報の値。値が未設定の場合、そのメタ情報の初期値。
		 * @exception NullPointerException nameがnull
		 * @exception NullPointerException unitがnull
		 * @exception IllegalArgumentException name, unitに合致するメタ情報が存在しない
		 * @exception IndexOutOfBoundsException unitが{@link BmsUnit#SINGLE SINGLE}の時、indexが0以外
		 * @exception IndexOutOfBoundsException unitが{@link BmsUnit#MULTIPLE MULTIPLE}の時、indexが0～{@link BmsSpec#MULTIPLE_META_INDEX_MAX}の範囲外
		 * @exception IndexOutOfBoundsException unitが{@link BmsUnit#INDEXED INDEXED}の時、indexが0～{@link BmsSpec#INDEXED_META_INDEX_MAX}の範囲外
		 */
		final Object getMeta(String name, BmsUnit unit, int index) {
			// 単位により処理を分岐
			assertArgMetaUnit(unit);
			switch (unit) {
			case SINGLE:
				assertArgSingleMetaIndex(index);
				return getSingleMeta(name);
			case MULTIPLE:
				return getMultipleMeta(name, index);
			case INDEXED:
				return getIndexedMeta(name, index);
			default:
				throw new IllegalArgumentException("Wrong meta unit.");
			}
		}

		/**
		 * 指定した重複可能メタ情報の全ての値を取得する。
		 * @param name メタ情報の名称
		 * @return 全ての値のリスト
		 * @exception NullPointerException nameがnull
		 * @exception IllegalArgumentException 名称に合致するメタ情報が存在しない
		 */
		final List<Object> getMultipleMetas(String name) {
			assertArgMetaName(name);
			assertMetaSpec(name, BmsUnit.MULTIPLE);
			return new ArrayList<>(mMultipleMetas.get(name));
		}

		/**
		 * 指定した索引付きメタ情報の全ての値を取得する。
		 * @param name メタ情報の名称
		 * @return 全ての値のリスト
		 * @exception NullPointerException nameがnull
		 * @exception IllegalArgumentException 名称に合致するメタ情報が存在しない
		 */
		final Map<Integer, Object> getIndexedMetas(String name) {
			assertArgMetaName(name);
			assertMetaSpec(name, BmsUnit.INDEXED);
			return new TreeMap<>(mIndexedMetas.get(name));
		}

		/**
		 * 指定した単体メタ情報の数を取得する。
		 * @param name メタ情報の名称
		 * @return メタ情報に値が設定されていれば1、なければ0
		 * @exception NullPointerException nameがnull
		 * @exception IllegalArgumentException 名称に合致するメタ情報が存在しない
		 */
		final int getSingleMetaCount(String name) {
			assertArgMetaName(name);
			assertMetaSpec(name, BmsUnit.SINGLE);
			return (mSingleMetas.get(name) != null) ? 1 : 0;
		}

		/**
		 * 指定した重複可能メタ情報の数を取得する。
		 * @param name メタ情報の名称
		 * @return メタ情報の数
		 * @exception NullPointerException nameがnull
		 * @exception IllegalArgumentException 名称に合致するメタ情報が存在しない
		 */
		final int getMultipleMetaCount(String name) {
			assertArgMetaName(name);
			assertMetaSpec(name, BmsUnit.MULTIPLE);
			return mMultipleMetas.get(name).size();
		}

		/**
		 * 指定した索引付きメタ情報の数を取得する。
		 * @param name メタ情報の名称
		 * @return メタ情報の数
		 * @exception NullPointerException nameがnull
		 * @exception IllegalArgumentException 名称に合致するメタ情報が存在しない
		 */
		final int getIndexedMetaCount(String name) {
			assertArgMetaName(name);
			assertMetaSpec(name, BmsUnit.INDEXED);
			return mIndexedMetas.get(name).size();
		}

		/**
		 * 指定した名称・単位のメタ情報の数を取得する。
		 * @param name メタ情報の名称
		 * @param unit メタ情報の単位
		 * @return メタ情報の数
		 * @exception NullPointerException nameがnull
		 * @exception NullPointerException unitがnull
		 * @exception IllegalArgumentException 名称に合致するメタ情報が存在しない
		 */
		final int getMetaCount(String name, BmsUnit unit) {
			assertArgMetaUnit(unit);
			switch (unit) {
			case SINGLE:
				return getSingleMetaCount(name);
			case MULTIPLE:
				return getMultipleMetaCount(name);
			case INDEXED:
				return getIndexedMetaCount(name);
			default:
				throw new IllegalArgumentException("Wrong meta unit.");
			}
		}

		/**
		 * 指定した名称に該当する単体メタ情報に値が設定されているか判定する。
		 * @param name メタ情報の名称
		 * @return 値が設定されていればtrue
		 * @exception NullPointerException nameがnull
		 * @exception IllegalArgumentException 名称に合致するメタ情報が存在しない
		 */
		final boolean containsSingleMeta(String name) {
			assertArgMetaName(name);
			assertMetaSpec(name, BmsUnit.SINGLE);
			return mSingleMetas.get(name) != null;
		}

		/**
		 * 指定した名称に該当する重複可能メタ情報に値が設定されているか判定する。
		 * @param name メタ情報の名称
		 * @param index インデックス
		 * @return 値が設定されていればtrue
		 * @exception NullPointerException nameがnull
		 * @exception IllegalArgumentException 名称に合致するメタ情報が存在しない
		 * @exception IndexOutOfBoundsException indexが0～{@link BmsSpec#MULTIPLE_META_INDEX_MAX}の範囲外
		 */
		final boolean containsMultipleMeta(String name, int index) {
			assertArgMetaName(name);
			assertMetaSpec(name, BmsUnit.MULTIPLE);
			assertArgMultipleMetaIndex(index);
			return index < mMultipleMetas.get(name).size();
		}

		/**
		 * 指定した名称に該当する索引付きメタ情報に値が設定されているか判定する。
		 * @param name メタ情報の名称
		 * @param index インデックス
		 * @return 値が設定されていればtrue
		 * @exception NullPointerException nameがnull
		 * @exception IllegalArgumentException 名称に合致するメタ情報が存在しない
		 * @exception IndexOutOfBoundsException indexが0～{@link BmsSpec#INDEXED_META_INDEX_MAX}の範囲外
		 */
		final boolean containsIndexedMeta(String name, int index) {
			assertArgMetaName(name);
			assertMetaSpec(name, BmsUnit.INDEXED);
			assertArgIndexedMetaIndex(index);
			return mIndexedMetas.get(name).containsKey(index);
		}

		/**
		 * 指定した名称・単位に該当するメタ情報に値が設定されているか判定する。
		 * @param name メタ情報の名称
		 * @param unit メタ情報の単位
		 * @param index インデックス
		 * @return 値が設定されていればtrue
		 * @exception NullPointerException nameがnull
		 * @exception NullPointerException unitがnull
		 * @exception IllegalArgumentException 名称・単位に合致するメタ情報が存在しない
		 * @exception IndexOutOfBoundsException unitが{@link BmsUnit#SINGLE SINGLE}の時、indexが0以外
		 * @exception IndexOutOfBoundsException unitが{@link BmsUnit#MULTIPLE MULTIPLE}の時、indexが0～{@link BmsSpec#MULTIPLE_META_INDEX_MAX}の範囲外
		 * @exception IndexOutOfBoundsException unitが{@link BmsUnit#INDEXED INDEXED}の時、indexが0～{@link BmsSpec#INDEXED_META_INDEX_MAX}の範囲外
		 */
		final boolean containsMeta(String name, BmsUnit unit, int index) {
			assertArgMetaUnit(unit);
			switch (unit) {
			case SINGLE:
				assertArgSingleMetaIndex(index);
				return containsSingleMeta(name);
			case MULTIPLE:
				return containsMultipleMeta(name, index);
			case INDEXED:
				return containsIndexedMeta(name, index);
			default:
				throw new IllegalArgumentException("Wrong meta unit.");
			}
		}

		/**
		 * 指定した名称・単位に該当するメタ情報に値が1件でも設定されているか判定する。
		 * @param name メタ情報の名称
		 * @param unit メタ情報の単位
		 * @return 当該メタ情報に値が1件でも設定されている場合true
		 * @exception NullPointerException nameがnull
		 * @exception NullPointerException unitがnull
		 * @exception IllegalArgumentException 名称・単位に合致するメタ情報が存在しない
		 */
		final boolean containsMeta(String name, BmsUnit unit) {
			assertArgNotNull(name, "name");
			assertArgNotNull(unit, "unit");
			switch (unit) {
			case SINGLE:
				if (!mSingleMetas.containsKey(name)) {
					throw new IllegalArgumentException(name + ": No such single meta.");
				} else {
					return (mSingleMetas.get(name) != null);
				}
			case MULTIPLE: {
				var list = mMultipleMetas.get(name);
				if (list == null) {
					throw new IllegalArgumentException(name + ": No such multiple meta.");
				} else {
					return (list.size() > 0);
				}
			}
			case INDEXED: {
				var map = mIndexedMetas.get(name);
				if (map == null) {
					throw new IllegalArgumentException(name + ": No suc indexed meta.");
				} else {
					return (map.size() > 0);
				}
			}
			default:
				throw new IllegalArgumentException("Wrong meta unit.");
			}
		}

		/**
		 * 初期BPMを設定する。
		 * @param bpm 初期BPM
		 * @exception IllegalStateException 動作モードが編集モードではない
		 * @exception IllegalArgumentException bpmが{@link BmsSpec#BPM_MIN}未満、または{@link BmsSpec#BPM_MAX}超過
		 */
		final void setInitialBpm(double bpm) {
			setSingleMeta(mSpec.getInitialBpmMeta().getName(), bpm);
		}

		/**
		 * 初期BPMを取得する。
		 * @return 初期BPM
		 */
		final double getInitialBpm() {
			return (Double)getSingleMeta(mSpec.getInitialBpmMeta().getName());
		}

		/**
		 * 参照先メタ情報の情報を取得する。
		 * @param channel チャンネル番号
		 * @return 参照先メタ情報の情報。該当チャンネルに参照先メタ情報が無い場合はnull。
		 */
		final ReferenceMetaInfo getReferenceMeta(int channel) {
			return mReferenceMetas.get(BmsInt.box(channel));
		}

		/**
		 * 指定名称・単位に合致するメタ情報が存在することをテストするアサーション。
		 * @param name メタ情報の名称
		 * @param unit メタ情報の単位
		 * @return 名称・単位に合致したメタ情報
		 * @exception IllegalArgumentException 名称・単位に合致するメタ情報が存在しない
		 */
		private BmsMeta assertMetaSpec(String name, BmsUnit unit) {
			var meta = mSpec.getMeta(name, unit);
			if (meta == null) {
				throw new IllegalArgumentException(String.format("%s: No such meta in spec.", name));
			} else {
				return meta;
			}
		}

		/**
		 * メタ情報の名称をテストするアサーション。
		 * @param name メタ情報の名称。
		 * @exception NullPointerException metaNameがnull
		 */
		private void assertArgMetaName(String name) {
			assertArgNotNull(name, "metaName");
		}

		/**
		 * 単体メタ情報のインデックスをテストするアサーション。
		 * @param index インデックス値
		 * @exception IndexOutOfBoundsException indexが0以外
		 */
		private void assertArgSingleMetaIndex(int index) {
			if (index != 0) {
				var msg = String.format("At the single unit meta, index can specify 0 only. index=%d", index);
				throw new IndexOutOfBoundsException(msg);
			}
		}

		/**
		 * 複数メタ情報のインデックスをテストするアサーション。
		 * @param index インデックス値
		 * @exception IndexOutOfBoundsException indexが0～{@link BmsSpec#MULTIPLE_META_INDEX_MAX}の範囲外
		 */
		private void assertArgMultipleMetaIndex(int index) {
			assertArgIndexRange(index, BmsSpec.MULTIPLE_META_INDEX_MAX + 1, "index");
		}

		/**
		 * 索引付きメタ情報のインデックスをテストするアサーション。
		 * @param index インデックス値
		 * @exception IndexOutOfBoundsException indexが0～{@link BmsSpec#INDEXED_META_INDEX_MAX}の範囲外
		 */
		private void assertArgIndexedMetaIndex(int index) {
			assertArgIndexRange(index, BmsSpec.INDEXED_META_INDEX_MAX + 1, "index");
		}

		/**
		 * BMSライブラリ仕様違反有無をテストするアサーション。
		 * @param meta メタ情報
		 * @param index メタ情報インデックス
		 * @param value 変換後設定値
		 * @exception IllegalArgumentException メタ情報への設定値がBMSライブラリ仕様違反
		 */
		private void assertValueWithinSpec(BmsMeta meta, int index, Object value) {
			if (meta.isInitialBpm() || meta.isReferenceBpm()) {
				// 初期BPM、BPM変更
				var bpm = ((Number)value).doubleValue();
				if (!BmsSpec.isBpmWithinRange(bpm)) {
					var msg = String.format(
							"BPM is library spec violation. meta=%s, index=%d, expected-bpm=(%.16g-%.16g), actual-bpm=%.16g",
							meta, index, BmsSpec.BPM_MIN, BmsSpec.BPM_MAX, bpm);
					throw new IllegalArgumentException(msg);
				}
			} else if (meta.isReferenceStop()) {
				// 譜面停止時間
				var stop = ((Number)value).doubleValue();
				if (!BmsSpec.isStopWithinRange(stop)) {
					var msg = String.format(
							"Stop tick is library spec violation. meta=%s, index=%d, expected-stop=(%.16g-%.16g), actuao-stop=%.16g",
							meta, index, (double)BmsSpec.STOP_MIN, (double)BmsSpec.STOP_MAX, stop);
					throw new IllegalArgumentException(msg);
				}
			}
		}
	}

	/**
	 * 編集モードから参照モードへ戻る際、BPM変更と譜面停止の統計情報を収集するためのテスター。
	 */
	private class BpmStopStatistics implements BmsNote.Tester {
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
		public boolean testNote(BmsNote note) {
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
			return true;
		}
	}

	/** このBMSコンテンツで扱うBMS仕様 */
	private BmsSpec mSpec;
	/** BMS宣言一覧 */
	private ArrayList<String> mBmsDeclKeys = new ArrayList<>();
	private ArrayList<String> mBmsDeclValues = new ArrayList<>();

	/** メタ情報コンテナ */
	private MetaContainer mMetas = new MetaContainer();
	/** タイムライン */
	private Timeline<MeasureElement> mTl = null;
	/** タイムラインアクセッサ */
	private TimelineAccessor mTlAccessor = new TimelineAccessor();

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
	/** API処理用の楽曲位置1 */
	private BmsPoint mTempAt1 = new BmsPoint();

	/**
	 * 新しいBMSコンテンツオブジェクトを構築します。
	 * <p>BMSコンテンツ構築直後のデータは空になっています。データを構築するには本クラスの各種メソッドを使用しますが、
	 * BMSファイルを読み込んでBMSコンテンツを生成する場合は{@link BmsLoader}を使用することを推奨します。</p>
	 * @param spec コンテンツに関連付けるBMS仕様
	 * @exception NullPointerException specがnull
	 */
	public BmsContent(BmsSpec spec) {
		assertArgNotNull(spec, "spec");
		mSpec = spec;
		mTl = new Timeline<>(spec, m -> new BmsMeasureElement(m));
		mMetas.setup();

		// BPM変更と譜面停止の統計情報を収集する
		collectBpmStopStatistics();
	}

	/**
	 * BMSコンテンツに関連付けたBMS仕様を取得します。
	 * @return BMS仕様
	 */
	public final BmsSpec getSpec() {
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
	 * @exception IllegalStateException 動作モードが編集モードではない
	 * @exception NullPointerException nameがnull
	 * @exception NullPointerException valueがnull
	 * @exception IllegalArgumentException nameの書式が不正
	 * @exception IllegalArgumentException valueの書式が不正
	 */
	public final void addDeclaration(String name, String value) {
		assertIsEditMode();
		assertArgDeclarationName(name);
		assertArg(PATTERN_DECL_NAME.matcher(name).matches(), "Wrong declaration name. name='%s'", name);
		assertArgNotNull(value, "value");
		assertArg(PATTERN_DECL_VALUE.matcher(value).matches(), "Wrong declaration value. name='%s', value='%s'", name, value);
		var index = mBmsDeclKeys.indexOf(name);
		if (index != -1) {
			mBmsDeclValues.set(index, value);
		} else {
			mBmsDeclKeys.add(name);
			mBmsDeclValues.add(value);
		}
	}

	/**
	 * 指定した名前のBMS宣言を消去します。
	 * @param name BMS宣言の名称
	 * @return 該当するBMS宣言を消去した場合はtrue、BMS宣言が存在しない場合はfalse
	 * @exception IllegalStateException 動作モードが編集モードではない
	 * @exception NullPointerException nameがnull
	 */
	public final boolean removeDeclaration(String name) {
		assertIsEditMode();
		assertArgDeclarationName(name);
		var index = mBmsDeclKeys.indexOf(name);
		if (index == -1) {
			return false;
		} else {
			mBmsDeclKeys.remove(index);
			mBmsDeclValues.remove(index);
			return true;
		}
	}

	/**
	 * 指定した名前のBMS宣言が存在するかを判定します。
	 * @param name BMS宣言の名称
	 * @return 該当するBMS宣言が存在する場合はtrue、存在しない場合はfalse
	 * @exception NullPointerException nameがnull
	 */
	public final boolean containsDeclaration(String name) {
		assertArgDeclarationName(name);
		return mBmsDeclKeys.contains(name);
	}

	/**
	 * 指定した名前のBMS宣言の値を取得します。
	 * @param name BMS宣言の名称
	 * @return 該当するBMS宣言の値、存在しない場合はnull
	 * @exception NullPointerException nameがnull
	 */
	public final String getDeclaration(String name) {
		assertArgDeclarationName(name);
		var index = mBmsDeclKeys.indexOf(name);
		return (index == -1) ? null : mBmsDeclValues.get(index);
	}

	/**
	 * 全てのBMS宣言の名前と値の一覧を取得します。
	 * <p>BMS宣言は{@link #addDeclaration}で登録された順で一覧化されます。</p>
	 * @return BMS宣言の名前と値のマップ
	 */
	public final Map<String, String> getDeclarations() {
		var decls = new LinkedHashMap<String, String>();
		for (var i = 0; i < mBmsDeclKeys.size(); i++) {
			decls.put(mBmsDeclKeys.get(i), mBmsDeclValues.get(i));
		}
		return decls;
	}

	/**
	 * BMSコンテンツをBMSフォーマットとして入出力する際の文字セットを取得します。
	 * <p>文字セットはBMS宣言の&quot;encoding&quot;に指定します。</p>
	 * <p>文字セットの指定がない場合は{@link BmsSpec#getStandardCharset}で返される文字セットを返します。</p>
	 * <p>&quot;encoding&quot;に認識できない文字セット名を指定して本メソッドを呼び出すとIllegalStateExceptionをスローします。</p>
	 * @return 文字セット
	 * @exception IllegalStateException BMS宣言の&quot;encoding&quot;に認識できない文字セット名を指定している
	 */
	public final Charset getEncoding() {
		var encoding = getDeclaration("encoding");
		if (encoding != null) {
			// encoding有り
			try {
				// encodingに指定の文字コードで文字セットを取得しようとする
				return Charset.forName(encoding);
			} catch (Exception e) {
				// 指定文字セットが使えない
				throw new IllegalStateException(String.format("%s: Unrecognized charset name.", encoding), e);
			}
		} else {
			// encoding無しの場合は標準文字セットを使用する
			return BmsSpec.getStandardCharset();
		}
	}

	/**
	 * 単体メタ情報に値を設定します。
	 * <p>値にnullを設定することで当該メタ情報の値を消去します。これは、値を指定しないことを意味し、
	 * {@link #getSingleMeta}で値を取得した場合には、そのメタ情報の初期値を返します。
	 * しかし、値は消去されているため{@link #containsSingleMeta}はfalseを返すことに
	 * 注意してください。</p>
	 * @param name メタ情報の名称
	 * @param value 設定する値
	 * @exception IllegalStateException 動作モードが編集モードではない
	 * @exception NullPointerException nameがnull
	 * @exception IllegalArgumentException 名称に合致するメタ情報が存在しない
	 * @exception ClassCastException valueが設定先メタ情報のデータ型に変換できない
	 * @exception IllegalArgumentException 初期BPMメタ情報に{@link BmsSpec#BPM_MIN}未満または{@link BmsSpec#BPM_MAX}超過の値を設定しようとした
	 */
	public final void setSingleMeta(String name, Object value) {
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
	 * @exception IllegalStateException 動作モードが編集モードではない
	 * @exception NullPointerException nameがnull
	 * @exception IllegalArgumentException 名称に合致するメタ情報が存在しない
	 * @exception IndexOutOfBoundsException indexが0～{@link BmsSpec#MULTIPLE_META_INDEX_MAX}の範囲外
	 * @exception ClassCastException valueが設定先メタ情報のデータ型に変換できない
	 */
	public final void setMultipleMeta(String name, int index, Object value) {
		mMetas.setMultipleMeta(name, index, value);
	}

	/**
	 * 索引付きメタ情報に値を設定します。
	 * <p>索引付きメタ情報とは、メタ情報の名称の後ろに00～ZZのインデックス値を付加したメタ情報のことを指します。</p>
	 * <p>値を消去するには、値にnullを指定します。重複可能メタ情報とは異なり、リストの左詰めは行われません。</p>
	 * @param name メタ情報の名称
	 * @param index 設定箇所を示すインデックス
	 * @param value 設定する値
	 * @exception IllegalStateException 動作モードが編集モードではない
	 * @exception NullPointerException nameがnull
	 * @exception IllegalArgumentException 名称に合致するメタ情報が存在しない
	 * @exception IndexOutOfBoundsException indexが0～{@link BmsSpec#INDEXED_META_INDEX_MAX}の範囲外
	 * @exception ClassCastException valueが設定先メタ情報のデータ型に変換できない
	 * @exception IllegalArgumentException BPM変更メタ情報に{@link BmsSpec#BPM_MIN}未満または{@link BmsSpec#BPM_MAX}超過の値を設定しようとした
	 * @exception IllegalArgumentException 譜面停止時間メタ情報に{@link BmsSpec#STOP_MIN}未満または{@link BmsSpec#STOP_MAX}超過の値を設定しようとした
	 */
	public final void setIndexedMeta(String name, int index, Object value) {
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
	 * @exception IllegalStateException 動作モードが編集モードではない
	 * @exception NullPointerException keyがnull
	 * @exception IllegalArgumentException メタ情報キーに合致するメタ情報が存在しない
	 * @exception ClassCastException valueが設定先メタ情報のデータ型に変換できない
	 * @exception IllegalArgumentException 初期BPMメタ情報に{@link BmsSpec#BPM_MIN}未満または{@link BmsSpec#BPM_MAX}超過の値を設定しようとした
	 * @exception IllegalArgumentException BPM変更メタ情報に{@link BmsSpec#BPM_MIN}未満または{@link BmsSpec#BPM_MAX}超過の値を設定しようとした
	 * @exception IllegalArgumentException 譜面停止時間メタ情報に{@link BmsSpec#STOP_MIN}未満または{@link BmsSpec#STOP_MAX}超過の値を設定しようとした
	 */
	public final void setMeta(BmsMetaKey key, Object value) {
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
	 * @exception IllegalStateException 動作モードが編集モードではない
	 * @exception NullPointerException keyがnull
	 * @exception IllegalArgumentException メタ情報キーに合致するメタ情報が存在しない
	 * @exception IndexOutOfBoundsException 構成単位が単体の時、indexが0以外
	 * @exception IndexOutOfBoundsException 構成単位が複数の時、indexが0～{@link BmsSpec#MULTIPLE_META_INDEX_MAX}の範囲外
	 * @exception IndexOutOfBoundsException 構成単位が索引付きの時、indexが0～{@link BmsSpec#INDEXED_META_INDEX_MAX}の範囲外
	 * @exception ClassCastException valueが設定先メタ情報のデータ型に変換できない
	 * @exception IllegalArgumentException 初期BPMメタ情報に{@link BmsSpec#BPM_MIN}未満または{@link BmsSpec#BPM_MAX}超過の値を設定しようとした
	 * @exception IllegalArgumentException BPM変更メタ情報に{@link BmsSpec#BPM_MIN}未満または{@link BmsSpec#BPM_MAX}超過の値を設定しようとした
	 * @exception IllegalArgumentException 譜面停止時間メタ情報に{@link BmsSpec#STOP_MIN}未満または{@link BmsSpec#STOP_MAX}超過の値を設定しようとした
	 */
	public final void setMeta(BmsMetaKey key, int index, Object value) {
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
	 * @exception IllegalStateException 動作モードが編集モードではない
	 * @exception NullPointerException nameがnull
	 * @exception NullPointerException unitがnull
	 * @exception IllegalArgumentException name, unitに合致するメタ情報が存在しない
	 * @exception IndexOutOfBoundsException unitが{@link BmsUnit#SINGLE SINGLE}の時、indexが0以外
	 * @exception IndexOutOfBoundsException unitが{@link BmsUnit#MULTIPLE MULTIPLE}の時、indexが0～{@link BmsSpec#MULTIPLE_META_INDEX_MAX}の範囲外
	 * @exception IndexOutOfBoundsException unitが{@link BmsUnit#INDEXED INDEXED}の時、indexが0～{@link BmsSpec#INDEXED_META_INDEX_MAX}の範囲外
	 * @exception ClassCastException valueが設定先メタ情報のデータ型に変換できない
	 * @exception IllegalArgumentException 初期BPMメタ情報に{@link BmsSpec#BPM_MIN}未満または{@link BmsSpec#BPM_MAX}超過の値を設定しようとした
	 * @exception IllegalArgumentException BPM変更メタ情報に{@link BmsSpec#BPM_MIN}未満または{@link BmsSpec#BPM_MAX}超過の値を設定しようとした
	 * @exception IllegalArgumentException 譜面停止時間メタ情報に{@link BmsSpec#STOP_MIN}未満または{@link BmsSpec#STOP_MAX}超過の値を設定しようとした
	 */
	public final void setMeta(String name, BmsUnit unit, int index, Object value) {
		mMetas.setMeta(name, unit, index, value);
	}

	/**
	 * 指定した重複可能メタ情報のリスト末尾に値を挿入します。
	 * <p>このメソッドでは、値にnullを指定することによる消去は行えません。</p>
	 * @param name メタ情報の名称
	 * @param value 設定する値
	 * @exception IllegalStateException 動作モードが編集モードではない
	 * @exception NullPointerException nameがnull
	 * @exception NullPointerException valueがnull
	 * @exception IllegalArgumentException 名称に合致するメタ情報が存在しない
	 * @exception IndexOutOfBoundsException リストが一杯でこれ以上値を挿入できない
	 * @exception ClassCastException valueが設定先メタ情報のデータ型に変換できない
	 */
	public final void putMultipleMeta(String name, Object value) {
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
	 * @exception NullPointerException nameがnull
	 * @exception IllegalArgumentException 名称に合致するメタ情報が存在しない
	 */
	@SuppressWarnings("unchecked")
	public final <T> T getSingleMeta(String name) {
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
	 * @exception NullPointerException nameがnull
	 * @exception IndexOutOfBoundsException indexが0～{@link BmsSpec#MULTIPLE_META_INDEX_MAX}の範囲外
	 * @exception IllegalArgumentException 名称に合致するメタ情報が存在しない
	 */
	@SuppressWarnings("unchecked")
	public final <T> T getMultipleMeta(String name, int index) {
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
	 * @exception NullPointerException nameがnull
	 * @exception IndexOutOfBoundsException indexが0～{@link BmsSpec#INDEXED_META_INDEX_MAX}の範囲外
	 * @exception IllegalArgumentException 名称に合致するメタ情報が存在しない
	 */
	@SuppressWarnings("unchecked")
	public final <T> T getIndexedMeta(String name, int index) {
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
	 * @exception NullPointerException keyがnull
	 * @exception IllegalArgumentException メタ情報キーに合致するメタ情報が存在しない
	 */
	@SuppressWarnings("unchecked")
	public final <T> T getMeta(BmsMetaKey key) {
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
	 * @exception NullPointerException keyがnull
	 * @exception IllegalArgumentException メタ情報キーに合致するメタ情報が存在しない
	 * @exception IndexOutOfBoundsException 構成単位が単体の時、indexが0以外
	 * @exception IndexOutOfBoundsException 構成単位が複数の時、indexが0～{@link BmsSpec#MULTIPLE_META_INDEX_MAX}の範囲外
	 * @exception IndexOutOfBoundsException 構成単位が索引付きの時、indexが0～{@link BmsSpec#INDEXED_META_INDEX_MAX}の範囲外
	 */
	@SuppressWarnings("unchecked")
	public final <T> T getMeta(BmsMetaKey key, int index) {
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
	 * @exception NullPointerException nameがnull
	 * @exception NullPointerException unitがnull
	 * @exception IllegalArgumentException name, unitに合致するメタ情報が存在しない
	 * @exception IndexOutOfBoundsException unitが{@link BmsUnit#SINGLE SINGLE}の時、indexが0以外
	 * @exception IndexOutOfBoundsException unitが{@link BmsUnit#MULTIPLE MULTIPLE}の時、indexが0～{@link BmsSpec#MULTIPLE_META_INDEX_MAX}の範囲外
	 * @exception IndexOutOfBoundsException unitが{@link BmsUnit#INDEXED INDEXED}の時、indexが0～{@link BmsSpec#INDEXED_META_INDEX_MAX}の範囲外
	 */
	@SuppressWarnings("unchecked")
	public final <T> T getMeta(String name, BmsUnit unit, int index) {
		return (T)mMetas.getMeta(name, unit, index);
	}

	/**
	 * 重複可能メタ情報の値リストを取得します。
	 * @param <T> メタ情報のデータ型
	 * @param name メタ情報の名称
	 * @return 重複可能メタ情報の値リスト
	 * @exception NullPointerException nameがnull
	 * @exception IllegalArgumentException 名称に合致するメタ情報が存在しない
	 */
	@SuppressWarnings("unchecked")
	public final <T> List<T> getMultipleMetas(String name) {
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
	 * @exception NullPointerException nameがnull
	 * @exception IllegalArgumentException 名称に合致するメタ情報が存在しない
	 */
	@SuppressWarnings("unchecked")
	public final <T> Map<Integer, T> getIndexedMetas(String name) {
		return (Map<Integer, T>)mMetas.getIndexedMetas(name);
	}

	/**
	 * 単体メタ情報の数を取得します。
	 * <p>単体メタ情報は1件のデータからなる情報です。値が設定されている場合に1を返します。</p>
	 * @param name メタ情報の名称
	 * @return メタ情報の数
	 * @exception NullPointerException nameがnull
	 * @exception IllegalArgumentException 名称に合致するメタ情報が存在しない
	 */
	public final int getSingleMetaCount(String name) {
		return mMetas.getSingleMetaCount(name);
	}

	/**
	 * 重複可能メタ情報の数を取得します。
	 * @param name メタ情報の名称
	 * @return メタ情報の数
	 * @exception NullPointerException nameがnull
	 * @exception IllegalArgumentException 名称に合致するメタ情報が存在しない
	 */
	public final int getMultipleMetaCount(String name) {
		return mMetas.getMultipleMetaCount(name);
	}

	/**
	 * 索引付きメタ情報の数を取得します。
	 * @param name メタ情報の名称
	 * @return メタ情報の数
	 * @exception NullPointerException nameがnull
	 * @exception IllegalArgumentException 名称に合致するメタ情報が存在しない
	 */
	public final int getIndexedMetaCount(String name) {
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
	 * @exception NullPointerException keyがnull
	 * @exception IllegalArgumentException メタ情報キーに合致するメタ情報が存在しない
	 */
	public final int getMetaCount(BmsMetaKey key) {
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
	 * @exception NullPointerException nameがnull
	 * @exception NullPointerException unitがnull
	 * @exception IllegalArgumentException 名称に合致するメタ情報が存在しない
	 */
	public final int getMetaCount(String name, BmsUnit unit) {
		return mMetas.getMetaCount(name, unit);
	}

	/**
	 * 単体メタ情報に値が設定されているかどうかを判定します。
	 * @param name メタ情報の名称
	 * @return 値が設定されている場合true
	 * @exception NullPointerException nameがnull
	 * @exception IllegalArgumentException 名称に合致するメタ情報が存在しない
	 */
	public final boolean containsSingleMeta(String name) {
		return mMetas.containsSingleMeta(name);
	}

	/**
	 * 重複可能メタ情報に値が設定されているかどうか判定します。
	 * @param name メタ情報の名称
	 * @param index インデックス
	 * @return 値が設定されている場合true
	 * @exception NullPointerException nameがnull
	 * @exception IllegalArgumentException 名称に合致するメタ情報が存在しない
	 * @exception IndexOutOfBoundsException indexが0～{@link BmsSpec#MULTIPLE_META_INDEX_MAX}の範囲外
	 */
	public final boolean containsMultipleMeta(String name, int index) {
		return mMetas.containsMultipleMeta(name, index);
	}

	/**
	 * 索引付きメタ情報に値が設定されているかどうか判定します。
	 * @param name メタ情報の名称
	 * @param index インデックス
	 * @return 値が設定されている場合true
	 * @exception NullPointerException nameがnull
	 * @exception IllegalArgumentException 名称に合致するメタ情報が存在しない
	 * @exception IndexOutOfBoundsException indexが0～{@link BmsSpec#INDEXED_META_INDEX_MAX}の範囲外
	 */
	public final boolean containsIndexedMeta(String name, int index) {
		return mMetas.containsIndexedMeta(name, index);
	}

	/**
	 * 指定したメタ情報キーに該当するメタ情報に1件以上値が設定されているかどうか判定します。
	 * <p>単体メタ情報の場合、そのメタ情報に値が設定されていればtrueを返します。複数・索引付きメタ情報の場合、そのメタ情報に1件でも
	 * 値が設定されていればtrueを返します。登録先インデックスの値は問いません。</p>
	 * @param key メタ情報キー
	 * @return 値が設定されている場合true
	 * @exception NullPointerException keyがnull
	 * @exception IllegalArgumentException メタ情報キーに合致するメタ情報が存在しない
	 */
	public final boolean containsMeta(BmsMetaKey key) {
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
	 * @exception NullPointerException keyがnull
	 * @exception IllegalArgumentException メタ情報キーに合致するメタ情報が存在しない
	 * @exception IndexOutOfBoundsException 構成単位が単体の時、indexが0以外
	 * @exception IndexOutOfBoundsException 構成単位が複数の時、indexが0～{@link BmsSpec#MULTIPLE_META_INDEX_MAX}の範囲外
	 * @exception IndexOutOfBoundsException 構成単位が索引付きの時、indexが0～{@link BmsSpec#INDEXED_META_INDEX_MAX}の範囲外
	 */
	public final boolean containsMeta(BmsMetaKey key, int index) {
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
	 * @exception NullPointerException nameがnull
	 * @exception NullPointerException unitがnull
	 * @exception IllegalArgumentException 名称・単位に合致するメタ情報が存在しない
	 * @exception IndexOutOfBoundsException unitが{@link BmsUnit#SINGLE SINGLE}の時、indexが0以外
	 * @exception IndexOutOfBoundsException unitが{@link BmsUnit#MULTIPLE MULTIPLE}の時、indexが0～{@link BmsSpec#MULTIPLE_META_INDEX_MAX}の範囲外
	 * @exception IndexOutOfBoundsException unitが{@link BmsUnit#INDEXED INDEXED}の時、indexが0～{@link BmsSpec#INDEXED_META_INDEX_MAX}の範囲外
	 */
	public final boolean containsMeta(String name, BmsUnit unit, int index) {
		return mMetas.containsMeta(name, unit, index);
	}

	/**
	 * 指定した名称・単位のメタ情報に1件以上値が設定されているかどうか判定します。
	 * <p>単体メタ情報の場合、そのメタ情報に値が設定されていればtrueを返します。複数・索引付きメタ情報の場合、そのメタ情報に1件でも
	 * 値が設定されていればtrueを返します。登録先インデックスの値は問いません。</p>
	 * @param name メタ情報の名称
	 * @param unit メタ情報の単位
	 * @return 値が設定されている場合true
	 * @exception NullPointerException nameがnull
	 * @exception NullPointerException unitがnull
	 * @exception IllegalArgumentException 名称・単位に合致するメタ情報が存在しない
	 */
	public final boolean containsMeta(String name, BmsUnit unit) {
		return mMetas.containsMeta(name, unit);
	}

	/**
	 * 初期BPMを取得します。
	 * <p>初期BPMはBMSの共通仕様で、どのようなBMS仕様にも必ずメタ情報に存在します。このメソッドを呼び出すと
	 * {@link #getSingleMeta(String)}を呼び出して初期BPMメタ情報から値を取得した時と同等の処理を行います。</p>
	 * @return 初期BPM
	 */
	public final double getInitialBpm() {
		return mMetas.getInitialBpm();
	}

	/**
	 * 初期BPMを設定します。
	 * <p>初期BPMはBMSの共通仕様で、どのようなBMS仕様にも必ずメタ情報に存在します。このメソッドを呼び出すと
	 * {@link #setSingleMeta(String, Object)}を呼び出して初期BPMメタ情報に値を設定した時と同等の
	 * 処理を行います。</p>
	 * @param bpm 初期BPM
	 * @exception IllegalStateException 動作モードが編集モードではない
	 * @exception IllegalArgumentException bpmが{@link BmsSpec#BPM_MIN}未満、または{@link BmsSpec#BPM_MAX}超過
	 */
	public final void setInitialBpm(double bpm) {
		mMetas.setInitialBpm(bpm);
	}

	/**
	 * 最小BPMを取得します。
	 * <p>この値は参照モードの場合のみ取得可能です。</p>
	 * @return 最小BPM
	 * @exception IllegalStateException 動作モードが参照モードではない
	 */
	public final double getMinBpm() {
		assertIsReferenceMode();
		return mMinBpm;
	}

	/**
	 * 最大BPMを取得します。
	 * <p>この値は参照モードの場合のみ取得可能です。</p>
	 * @return 最大BPM
	 * @exception IllegalStateException 動作モードが参照モードではない
	 */
	public final double getMaxBpm() {
		assertIsReferenceMode();
		return mMaxBpm;
	}

	/**
	 * BPM変化回数を取得します。
	 * <p>この値は参照モードの場合のみ取得可能です。</p>
	 * @return BPM変化回数
	 * @exception IllegalStateException 動作モードが参照モードではない
	 */
	public final long getChangeBpmCount() {
		assertIsReferenceMode();
		return mChangeBpmCount;
	}

	/**
	 * 譜面停止回数を取得します。
	 * <p>この値は参照モードの場合のみ取得可能です。</p>
	 * @return 譜面停止回数
	 * @exception IllegalStateException 動作モードが参照モードではない
	 */
	public final long getStopCount() {
		assertIsReferenceMode();
		return mStopCount;
	}

	/**
	 * BMSコンテンツを編集モードに変更します。
	 * <p>このメソッドを呼び出すことでBMSコンテンツに対するデータの更新が行えるようになります。それと同時に、
	 * いくつかのメソッドの呼び出しが制限されます。</p>
	 * <p>既に編集モードになっている状態でこのメソッドを呼び出してはいけません。</p>
	 * @exception IllegalStateException 動作モードが参照モードではない
	 */
	public final void beginEdit() {
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
	 * @exception IllegalStateException 動作モードが編集モードではない
	 */
	public final void endEdit() {
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
	 * 現在の動作モードが「参照モード」であるかどうかを返します。
	 * @return 参照モードである場合true
	 * @see #beginEdit
	 * @see #endEdit
	 */
	public final boolean isReferenceMode() {
		return (mIsEditMode == false);
	}

	/**
	 * 現在の動作モードが「編集モード」であるかどうかを返します。
	 * @return 編集モードである場合true
	 * @see #beginEdit
	 * @see #endEdit
	 */
	public final boolean isEditMode() {
		return (mIsEditMode == true);
	}

	/**
	 * {@link #beginEdit}が呼ばれ、動作モードが編集モードに遷移した時に呼ばれます。
	 * <p>当メソッドは、編集モードに遷移した時、継承先のクラスにて特別な処理を行う場合のトリガとして使用することを想定しています。</p>
	 * <p>当メソッドの実行中は動作モードを変更出来ません。{@link #beginEdit}, {@link #endEdit}を呼び出しても何も起こりません。</p>
	 */
	protected void onBeginEdit() {
		// Do nothing
	}

	/**
	 * {@link #endEdit}が呼ばれ、動作モードが参照モードに遷移した時に呼ばれます。
	 * <p>当メソッドは、参照モードに遷移した時、継承先のクラスにて特別な処理を行う場合のトリガとして使用することを想定しています。当メソッドが
	 * 呼ばれるのは、小節ごとの時間とBPMに関する情報の再計算が完了した後です。</p>
	 * <p>当メソッドの実行中は動作モードを変更出来ません。{@link #beginEdit}, {@link #endEdit}を呼び出しても何も起こりません。</p>
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
	public final <T> T getMeasureValue(int channel, int measure) {
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
	 * @exception IllegalArgumentException BMS仕様に無いチャンネル番号を指定した
	 * @exception IndexOutOfBoundsException チャンネルインデックスがマイナス値
	 * @exception IndexOutOfBoundsException 重複不許可のチャンネルで0以外のインデックスを指定した
	 * @exception IllegalArgumentException 指定チャンネルのデータ型が値型ではない
	 * @exception IllegalArgumentException 小節番号が{@link BmsSpec#MEASURE_MIN}未満または{@link BmsSpec#MEASURE_MAX}超過
	 */
	@SuppressWarnings("unchecked")
	public final <T> T getMeasureValue(int channel, int index, int measure) {
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
	public final void setMeasureValue(int channel, int measure, Object value) {
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
	 * @exception IllegalStateException 動作モードが編集モードではない
	 * @exception IllegalArgumentException BMS仕様に無いチャンネル番号を指定した
	 * @exception IndexOutOfBoundsException チャンネルインデックスがマイナス値
	 * @exception IndexOutOfBoundsException データ重複不許可のチャンネルで0以外のインデックスを指定した
	 * @exception IllegalArgumentException 小節番号が{@link BmsSpec#MEASURE_MIN}未満または{@link BmsSpec#MEASURE_MAX}超過
	 * @exception IllegalArgumentException チャンネルのデータ型が値型、任意型ではない
	 * @exception ClassCastException valueをチャンネルのデータ型に変換出来ない
	 * @exception IllegalArgumentException 小節長に{@link BmsSpec#LENGTH_MIN}未満または{@link BmsSpec#LENGTH_MAX}超過の値を設定しようとした
	 */
	public final void setMeasureValue(int channel, int index, int measure, Object value) {
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
	public final boolean containsMeasureValue(int channel, int measure) {
		return mTlAccessor.getValue(channel, 0, measure, null, true) != null;
	}

	/**
	 * 小節に小節データが登録されているかどうかを返します。
	 * <p>指定するチャンネルのデータ型は値型でなければなりません。</p>
	 * @param channel チャンネル番号
	 * @param index チャンネルインデックス
	 * @param measure 小節番号
	 * @return 指定小節に小節データが存在する場合true
	 * @exception IllegalArgumentException BMS仕様に無いチャンネル番号を指定した
	 * @exception IndexOutOfBoundsException チャンネルインデックスがマイナス値
	 * @exception IndexOutOfBoundsException 重複不許可のチャンネルで0以外のインデックスを指定した
	 * @exception IllegalArgumentException 指定チャンネルのデータ型が値型ではない
	 * @exception IllegalArgumentException 小節番号が{@link BmsSpec#MEASURE_MIN}未満または{@link BmsSpec#MEASURE_MAX}超過
	 */
	public final boolean containsMeasureValue(int channel, int index, int measure) {
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
	 * @exception NullPointerException atがnull
	 */
	public final BmsNote getNote(int channel, BmsAt at) {
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
	 * @exception NullPointerException atがnull
	 */
	public final BmsNote getNote(int channel, int index, BmsAt at) {
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
	public final BmsNote getNote(int channel, int measure, double tick) {
		return mTlAccessor.getNote(channel, 0, measure, tick);
	}

	/**
	 * 1個のノートを取得します。指定位置にノートが存在しない場合はnullを返します。
	 * @param channel チャンネル番号
	 * @param index チャンネルインデックス
	 * @param measure 小節番号
	 * @param tick 小節の刻み位置
	 * @return ノート
	 * @exception IllegalArgumentException BMS仕様に無いチャンネル番号を指定した
	 * @exception IndexOutOfBoundsException チャンネルインデックスにマイナス値を指定した
	 * @exception IndexOutOfBoundsException 重複不許可のチャンネルで0以外のチャンネルインデックスを指定した
	 * @exception IllegalArgumentException 小節番号が{@link BmsSpec#MEASURE_MIN}未満または{@link BmsSpec#MEASURE_MAX}超過
	 * @exception IllegalArgumentException 小節の刻み位置にマイナス値または当該小節の刻み数以上の値を指定した
	 * @exception IllegalArgumentException 指定チャンネルのデータ型が配列型ではない
	 */
	public final BmsNote getNote(int channel, int index, int measure, double tick) {
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
	 * @exception NullPointerException atがnull
	 */
	public final BmsNote getPreviousNote(int channel, BmsAt at, boolean inclusiveFrom) {
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
	 * @exception NullPointerException atがnull
	 */
	public final BmsNote getPreviousNote(int channel, int index, BmsAt at, boolean inclusiveFrom) {
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
	public final BmsNote getPreviousNote(int channel, int measure, double tick, boolean inclusiveFrom) {
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
	 * @exception IllegalArgumentException BMS仕様に無いチャンネル番号を指定した
	 * @exception IndexOutOfBoundsException チャンネルインデックスにマイナス値を指定した
	 * @exception IndexOutOfBoundsException 重複不許可のチャンネルで0以外のチャンネルインデックスを指定した
	 * @exception IllegalArgumentException 小節番号が{@link BmsSpec#MEASURE_MIN}未満または小節数以上
	 * @exception IllegalArgumentException 小節の刻み位置にマイナス値または当該小節の刻み数以上を指定した
	 * @exception IllegalArgumentException 指定チャンネルのデータ型が配列型ではない
	 */
	public final BmsNote getPreviousNote(int channel, int index, int measure, double tick, boolean inclusiveFrom) {
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
	 * @exception NullPointerException atがnull
	 */
	public final BmsNote getNextNote(int channel, BmsAt at, boolean inclusiveFrom) {
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
	 * @exception NullPointerException atがnull
	 */
	public final BmsNote getNextNote(int channel, int index, BmsAt at, boolean inclusiveFrom) {
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
	public final BmsNote getNextNote(int channel, int measure, double tick, boolean inclusiveFrom) {
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
	 * @exception IllegalArgumentException BMS仕様に無いチャンネル番号を指定した
	 * @exception IndexOutOfBoundsException チャンネルインデックスにマイナス値を指定した
	 * @exception IndexOutOfBoundsException 重複不許可のチャンネルで0以外のチャンネルインデックスを指定した
	 * @exception IllegalArgumentException 小節番号が{@link BmsSpec#MEASURE_MIN}未満または小節数以上
	 * @exception IllegalArgumentException 小節の刻み位置にマイナス値または当該小節の刻み数以上を指定した
	 * @exception IllegalArgumentException 指定チャンネルのデータ型が配列型ではない
	 */
	public final BmsNote getNextNote(int channel, int index, int measure, double tick, boolean inclusiveFrom) {
		return mTlAccessor.getNearerNote(channel, index, measure, tick, 1, inclusiveFrom);
	}

	/**
	 * 指定位置のノートの値から参照メタ情報のデータを照合し、照合結果を返します。
	 * <p>引数noteからチャンネル番号・インデックス、小節番号・刻み位置を取り出します。<br>
	 * それ以外の処理は{@link #getResolvedNoteValue(int, int, int, double)}と同じです。</p>
	 * @param note ノート
	 * @return 参照先メタ情報から取り出したデータ
	 * @exception NullPointerException noteがnull
	 */
	public final Object getResolvedNoteValue(BmsNote note) {
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
	 * @exception NullPointerException atがnull
	 */
	public final Object getResolvedNoteValue(int channel, BmsAt at) {
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
	 * @exception NullPointerException atがnull
	 */
	public final Object getResolvedNoteValue(int channel, int index, BmsAt at) {
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
	public final Object getResolvedNoteValue(int channel, int measure, double tick) {
		return mTlAccessor.getResolvedNoteValue(channel, 0, measure, tick);
	}

	/**
	 * 指定位置のノートの値から参照メタ情報のデータを照合し、照合結果を返します。
	 * <p>参照先メタ情報の無いチャンネルを指定した場合、そのノートの値をLong型に変換して返します。</p>
	 * <p>参照先メタ情報のインデックス(=ノートの値)に割り当てられたデータが存在しない場合、そのメタ情報の
	 * 初期値を返します。</p>
	 * @param channel チャンネル番号
	 * @param index チャンネルインデックス
	 * @param measure 小節番号
	 * @param tick 小節の刻み位置
	 * @return 参照先メタ情報から取り出したデータ
	 * @exception IllegalArgumentException BMS仕様に無いチャンネル番号を指定した
	 * @exception IndexOutOfBoundsException チャンネルインデックスがマイナス値
	 * @exception IndexOutOfBoundsException 重複不許可のチャンネルで0以外のチャンネルインデックスを指定した
	 * @exception IllegalArgumentException 小節番号が{@link BmsSpec#MEASURE_MIN}未満または{@link BmsSpec#MEASURE_MAX}超過
	 * @exception IllegalArgumentException マイナス値または当該小節の刻み数以上の刻み位置を指定した
	 * @exception IllegalArgumentException 指定チャンネルのデータ型が配列型ではない
	 */
	public final Object getResolvedNoteValue(int channel, int index, int measure, double tick) {
		return mTlAccessor.getResolvedNoteValue(channel, index, measure, tick);
	}

	/**
	 * 条件に該当するノートを検索し、最初に見つかったノートを返します。
	 * <p>引数atFromから検索開始位置の小節番号・刻み位置を取り出します。<br>
	 * それ以外の処理は{@link #pointOf(int, double, BmsNote.Tester)}と同じです。</p>
	 * @param atFrom 楽曲位置
	 * @param judge 条件一致判定を行う述語
	 * @return 最初に条件に一致したノート。見つからなかった場合はnull。
	 * @exception NullPointerException atFromがnull
	 */
	public final BmsNote pointOf(BmsAt atFrom, BmsNote.Tester judge) {
		return mTlAccessor.pointOf(atFrom.getMeasure(), atFrom.getTick(), judge);
	}

	/**
	 * 条件に該当するノートを検索し、最初に見つかったノートを返します。
	 * <p>検索は、原点の楽曲位置から進行方向に向かって行われます。</p>
	 * @param measureFrom 検索開始位置を示す小節番号
	 * @param tickFrom 検索開始位置を示す刻み位置
	 * @param judge 条件一致判定を行う述語
	 * @return 最初に条件に一致したノート。見つからなかった場合はnull。
	 * @exception IllegalArgumentException 検索開始位置の小節番号にノート・小節データが存在し得ない値を指定した
	 * @exception IllegalArgumentException 検索開始位置の刻み位置にマイナス値または当該小節の刻み数以上の値を指定した
	 * @exception NullPointerException judgeがnull
	 */
	public final BmsNote pointOf(int measureFrom, double tickFrom, BmsNote.Tester judge) {
		return mTlAccessor.pointOf(measureFrom, tickFrom, judge);
	}

	/**
	 * 指定範囲のノートを列挙します。
	 * <p>全てのノートを列挙対象とします。<br>
	 * それ以外の処理は{@link #enumNotes(int, int, int, double, int, double, BmsNote.Tester)}と同じです。</p>
	 * @param enumNote ノートを通知するテスター
	 * @exception NullPointerException enumNoteがnull
	 */
	public final void enumNotes(BmsNote.Tester enumNote) {
		mTlAccessor.enumNotes(
				BmsSpec.CHANNEL_MIN, BmsSpec.CHANNEL_MAX + 1,
				0, 0,
				mTlAccessor.getCount(), 0,
				enumNote);
	}

	/**
	 * 指定楽曲位置のノートを列挙します。
	 * <p>引数atから小節番号、刻み位置を取り出します。<br>
	 * それ以外の処理は{@link #enumNotes(int, double, BmsNote.Tester)}と同じです。</p>
	 * @param at 楽曲位置
	 * @param enumNote ノートを通知するテスター
	 * @exception NullPointerException atがnull
	 */
	public final void enumNotes(BmsAt at, BmsNote.Tester enumNote) {
		assertArgNotNull(at, "at");
		mTlAccessor.enumNotes(at.getMeasure(), at.getTick(), enumNote);
	}

	/**
	 * 指定楽曲位置のノートを列挙します。
	 * <p>当メソッドは指定された小節番号、刻み位置のノートをピンポイントで列挙します。楽曲位置が少しでもずれている
	 * ノートは列挙対象とはなりません。範囲列挙を行いたい場合は他のオーバーロードメソッドを使用してください。</p>
	 * <p>テスターの戻り値にfalseを指定することで列挙を中断することが出来ます。</p>
	 * @param measure 列挙対象とする小節番号
	 * @param tick 列挙対象とする刻み位置
	 * @param enumNote ノートを通知するテスター
	 * @exception IllegalArgumentException measureがマイナス値または小節数以上
	 * @exception IllegalArgumentException tickがマイナス値または当該小節の刻み数以上
	 * @exception NullPointerException enumNoteがnull
	 */
	public final void enumNotes(int measure, double tick, BmsNote.Tester enumNote) {
		mTlAccessor.enumNotes(measure, tick, enumNote);
	}

	/**
	 * 指定範囲のノートを列挙します。
	 * <p>全チャンネルを列挙対象とします。<br>
	 * それ以外の処理は{@link #enumNotes(int, int, int, double, int, double, BmsNote.Tester)}と同じです。</p>
	 * @param measureBegin 列挙範囲を示す最小の小節番号
	 * @param tickBegin 列挙範囲を示す最小の小節番号の刻み位置
	 * @param measureEnd 列挙範囲を示す最大の小節番号
	 * @param tickEnd 列挙範囲を示す最大の小節番号の刻み位置(この位置を含まない)
	 * @param enumNote ノートを通知するテスター
	 */
	public final void enumNotes(int measureBegin, double tickBegin, int measureEnd, double tickEnd,
			BmsNote.Tester enumNote) {
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
	 * それ以外の処理は{@link #enumNotes(int, int, int, double, int, double, BmsNote.Tester)}と同じです。</p>
	 * @param atBegin 列挙範囲を示す最小の楽曲位置
	 * @param atEnd 列挙範囲を示す最大の楽曲位置(この位置を含まない)
	 * @param enumNote ノートを通知するテスター
	 * @exception NullPointerException atBeginがnull
	 * @exception NullPointerException atEndがnull
	 */
	public final void enumNotes(BmsAt atBegin, BmsAt atEnd, BmsNote.Tester enumNote) {
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
	 * それ以外の処理は{@link #enumNotes(int, int, int, double, int, double, BmsNote.Tester)}と同じです。</p>
	 * @param channelBegin 列挙範囲を示す最小のチャンネル番号
	 * @param channelEnd 列挙範囲を示す最大のチャンネル番号(この番号を含まない)
	 * @param atBegin 列挙範囲を示す最小の楽曲位置
	 * @param atEnd 列挙範囲を示す最大の楽曲位置(この位置を含まない)
	 * @param enumNote ノートを通知するテスター
	 * @exception NullPointerException atBeginがnull
	 * @exception NullPointerException atEndがnull
	 */
	public final void enumNotes(int channelBegin, int channelEnd, BmsAt atBegin, BmsAt atEnd,
			BmsNote.Tester enumNote) {
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
	 * <p>見つかったノートは指定のテスターに通知されます。テスターの戻り値にfalseを指定すると列挙を中断します。</p>
	 * <p>小節数が0(ノートが全く追加されていない)のコンテンツに対して当メソッドを呼び出すと何も行われません。</p>
	 * @param channelBegin 列挙範囲を示す最小のチャンネル番号
	 * @param channelEnd 列挙範囲を示す最大のチャンネル番号(この番号を含まない)
	 * @param measureBegin 列挙範囲を示す最小の小節番号
	 * @param tickBegin 列挙範囲を示す最小の小節番号の刻み位置
	 * @param measureEnd 列挙範囲を示す最大の小節番号
	 * @param tickEnd 列挙範囲を示す最大の小節番号の刻み位置(この位置を含まない)
	 * @param enumNote ノートを通知するテスター
	 * @exception IllegalArgumentException channelBeginに小節データ・ノートを登録できないチャンネル番号を指定した
	 * @exception IllegalArgumentException channelEndに{@link BmsSpec#CHANNEL_MAX}を超える値を指定した
	 * @exception IllegalArgumentException measureBeginに{@link BmsSpec#MEASURE_MIN}未満または{@link BmsSpec#MEASURE_MAX}超過の値を指定した
	 * @exception IllegalArgumentException tickBeginにマイナス値または当該小節の刻み数以上の値を指定した
	 * @exception IllegalArgumentException measureEndに{@link BmsSpec#MEASURE_MIN}未満または{@link BmsSpec#MEASURE_MAX}超過の値を指定した
	 * @exception IllegalArgumentException tickEndにマイナス値、当該小節の刻み数以上、または小節番号==小節数の時に0以外の値を指定した
	 * @exception NullPointerException enumNoteがnull
	 */
	public final void enumNotes(int channelBegin, int channelEnd,
			int measureBegin, double tickBegin, int measureEnd, double tickEnd, BmsNote.Tester enumNote) {
		mTlAccessor.enumNotes(
				channelBegin, channelEnd,
				measureBegin, tickBegin,
				measureEnd, tickEnd,
				enumNote);
	}

	/**
	 * 複数のノートをリスト形式で取得します。
	 * <p>全てのノートを取得対象とします。<br>
	 * それ以外の処理は{@link #listNotes(int, int, int, double, int, double, BmsNote.Tester)}と同じです。</p>
	 * @param isCollect 取得有無を決定する述語
	 * @return ノートのリスト
	 */
	public final List<BmsNote> listNotes(BmsNote.Tester isCollect) {
		return mTlAccessor.listNotes(
				BmsSpec.CHANNEL_MIN, BmsSpec.CHANNEL_MAX + 1,
				0, 0,
				mTlAccessor.getCount(), 0,
				isCollect);
	}

	/**
	 * 複数のノートをリスト形式で取得します。
	 * <p>引数atから小節番号、刻み位置を取り出します。<br>
	 * それ以外の処理は{@link #listNotes(int, double, BmsNote.Tester)}と同じです。</p>
	 * @param at 楽曲位置
	 * @param isCollect 取得有無を決定するテスター
	 * @return ノートのリスト
	 * @exception NullPointerException atがnull
	 */
	public final List<BmsNote> listNotes(BmsAt at, BmsNote.Tester isCollect) {
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
	 * @exception IllegalArgumentException measureがマイナス値または小節数以上
	 * @exception IllegalArgumentException tickがマイナス値または当該小節の刻み数以上
	 * @exception NullPointerException isCollectがnull
	 */
	public final List<BmsNote> listNotes(int measure, double tick, BmsNote.Tester isCollect) {
		return mTlAccessor.listNotes(measure, tick, isCollect);
	}

	/**
	 * 複数のノートをリスト形式で取得します。
	 * <p>全チャンネルを取得対象とします。<br>
	 * 引数atBegin, atEndから小節番号・刻み位置を取り出します。<br>
	 * それ以外の処理は{@link #listNotes(int, int, int, double, int, double, BmsNote.Tester)}と同じです。</p>
	 * @param atBegin 取得対象範囲を示す最小の楽曲位置
	 * @param atEnd 取得対象範囲を示す最大の楽曲位置(この位置を含まない)
	 * @param isCollect 取得有無を決定する述語
	 * @return ノートのリスト
	 * @exception NullPointerException atBeginがnull
	 * @exception NullPointerException atEndがnull
	 */
	public final List<BmsNote> listNotes(BmsAt atBegin, BmsAt atEnd, BmsNote.Tester isCollect) {
		return mTlAccessor.listNotes(
				BmsSpec.CHANNEL_MIN, BmsSpec.CHANNEL_MAX,
				atBegin.getMeasure(), atBegin.getTick(),
				atEnd.getMeasure(), atEnd.getTick(),
				isCollect);
	}

	/**
	 * 複数のノートをリスト形式で取得します。
	 * <p>引数atBegin, atEndから小節番号・刻み位置を取り出します。<br>
	 * それ以外の処理は{@link #listNotes(int, int, int, double, int, double, BmsNote.Tester)}と同じです。</p>
	 * @param channelBegin 取得対象範囲を示す最小のチャンネル番号
	 * @param channelEnd 取得対象範囲を示す最大のチャンネル番号(この番号を含まない)
	 * @param atBegin 取得対象範囲を示す最小の楽曲位置
	 * @param atEnd 取得対象範囲を示す最大の楽曲位置(この位置を含まない)
	 * @param isCollect 取得有無を決定する述語
	 * @return ノートのリスト
	 * @exception NullPointerException atBeginがnull
	 * @exception NullPointerException atEndがnull
	 */
	public final List<BmsNote> listNotes(int channelBegin, int channelEnd, BmsAt atBegin, BmsAt atEnd,
			BmsNote.Tester isCollect) {
		return mTlAccessor.listNotes(
				channelBegin, channelEnd,
				atBegin.getMeasure(), atBegin.getTick(),
				atEnd.getMeasure(), atEnd.getTick(),
				isCollect);
	}

	/**
	 * 複数のノートをリスト形式で取得します。
	 * <p>全チャンネルを取得対象とします。<br>
	 * それ以外の処理は{@link #listNotes(int, int, int, double, int, double, BmsNote.Tester)}と同じです。</p>
	 * @param measureBegin 取得対象範囲を示す最小の小節番号
	 * @param tickBegin 取得対象範囲を示す最小の小節番号の刻み位置
	 * @param measureEnd 取得対象範囲を示す最大の小節番号
	 * @param tickEnd 取得対象範囲を示す最大の小節番号の刻み位置(この位置を含まない)
	 * @param isCollect 取得有無を決定する述語
	 * @return ノートのリスト
	 */
	public final List<BmsNote> listNotes(int measureBegin, double tickBegin, int measureEnd, double tickEnd,
			BmsNote.Tester isCollect) {
		return mTlAccessor.listNotes(
				BmsSpec.CHANNEL_MIN, BmsSpec.CHANNEL_MAX,
				measureBegin, tickBegin,
				measureEnd, tickEnd,
				isCollect);
	}

	/**
	 * 複数のノートをリスト形式で取得します。
	 * <p>ノートは指定の述語に通知されます。述語の戻り値にfalseを指定すると取得対象から除外できます。</p>
	 * @param channelBegin 取得対象範囲を示す最小のチャンネル番号
	 * @param channelEnd 取得対象範囲を示す最大のチャンネル番号(この番号を含まない)
	 * @param measureBegin 取得対象範囲を示す最小の小節番号
	 * @param tickBegin 取得対象範囲を示す最小の小節番号の刻み位置
	 * @param measureEnd 取得対象範囲を示す最大の小節番号
	 * @param tickEnd 取得対象範囲を示す最大の小節番号の刻み位置(この位置を含まない)
	 * @param isCollect 取得有無を決定する述語
	 * @return ノートのリスト
	 * @exception IllegalArgumentException channelBeginに{@link BmsSpec#CHANNEL_MIN}未満または{@link BmsSpec#CHANNEL_MAX}を超える値を指定した
	 * @exception IllegalArgumentException channelEndに{@link BmsSpec#CHANNEL_MIN}未満または{@link BmsSpec#CHANNEL_MAX}+1を超える値を指定した
	 * @exception IllegalArgumentException measureBeginに{@link BmsSpec#MEASURE_MIN}未満または{@link BmsSpec#MEASURE_MAX}超過の値を指定した
	 * @exception IllegalArgumentException tickBeginにマイナス値または当該小節の刻み数以上の値を指定した
	 * @exception IllegalArgumentException measureEndに{@link BmsSpec#MEASURE_MIN}未満または{@link BmsSpec#MEASURE_MAX}超過の値を指定した
	 * @exception IllegalArgumentException tickEndにマイナス値、当該小節の刻み数以上、または小節番号==小節数の時に0以外の値を指定した
	 * @exception NullPointerException isCollectがnull
	 */
	public final List<BmsNote> listNotes(int channelBegin, int channelEnd,
			int measureBegin, double tickBegin, int measureEnd, double tickEnd, BmsNote.Tester isCollect) {
		return mTlAccessor.listNotes(
				channelBegin, channelEnd,
				measureBegin, tickBegin,
				measureEnd, tickEnd,
				isCollect);
	}

	/**
	 * 条件に一致するノートの数を数えます。
	 * <p>全ノートをカウント対象にします。<br>
	 * それ以外の処理は{@link #countNotes(int, int, int, double, int, double, BmsNote.Tester)}と同じです。</p>
	 * @param isCounting カウント有無を決定する述語
	 * @return 条件に一致したノートの数
	 */
	public final int countNotes(BmsNote.Tester isCounting) {
		return mTlAccessor.countNotes(
				BmsSpec.CHANNEL_MIN, BmsSpec.CHANNEL_MAX,
				0, 0,
				mTlAccessor.getCount(), 0,
				isCounting);
	}

	/**
	 * 指定楽曲位置のノートの数を数えます。
	 * <p>引数atから小節番号、刻み位置を取り出します。<br>
	 * それ以外の処理は{@link #countNotes(int, double, BmsNote.Tester)}と同じです。</p>
	 * @param at 楽曲位置
	 * @param isCounting カウント有無を決定するテスター
	 * @return 条件に一致したノートの数
	 * @exception NullPointerException atがnull
	 */
	public final int countNotes(BmsAt at, BmsNote.Tester isCounting) {
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
	 * @exception IllegalArgumentException measureがマイナス値または小節数以上
	 * @exception IllegalArgumentException tickがマイナス値または当該小節の刻み数以上
	 * @exception NullPointerException isCountingがnull
	 */
	public final int countNotes(int measure, double tick, BmsNote.Tester isCounting) {
		return mTlAccessor.countNotes(measure, tick, isCounting);
	}

	/**
	 * 条件に一致するノートの数を数えます。
	 * <p>全チャンネルをカウント対象にします。<br>
	 * 引数atBegin, atEndから小節番号・刻み位置を取り出します。<br>
	 * それ以外の処理は{@link #countNotes(int, int, int, double, int, double, BmsNote.Tester)}と同じです。</p>
	 * @param atBegin カウント範囲を示す最小の楽曲位置
	 * @param atEnd カウント範囲を示す最大の楽曲位置(この位置を含まない)
	 * @param isCounting カウント有無を決定する述語
	 * @return 条件に一致したノートの数
	 * @exception NullPointerException atBeginがnull
	 * @exception NullPointerException atEndがnull
	 */
	public final int countNotes(BmsAt atBegin, BmsAt atEnd, BmsNote.Tester isCounting) {
		return mTlAccessor.countNotes(
				BmsSpec.CHANNEL_MIN, BmsSpec.CHANNEL_MAX,
				atBegin.getMeasure(), atBegin.getTick(),
				atEnd.getMeasure(), atEnd.getTick(),
				isCounting);
	}

	/**
	 * 条件に一致するノートの数を数えます。
	 * <p>引数atBegin, atEndから小節番号・刻み位置を取り出します。<br>
	 * それ以外の処理は{@link #countNotes(int, int, int, double, int, double, BmsNote.Tester)}と同じです。</p>
	 * @param channelBegin カウント範囲を示す最小のチャンネル番号
	 * @param channelEnd カウント範囲を示す最大のチャンネル番号(この番号を含まない)
	 * @param atBegin カウント範囲を示す最小の楽曲位置
	 * @param atEnd カウント範囲を示す最大の楽曲位置(この位置を含まない)
	 * @param isCounting カウント有無を決定する述語
	 * @return 条件に一致したノートの数
	 * @exception NullPointerException atBeginがnull
	 * @exception NullPointerException atEndがnull
	 */
	public final int countNotes(int channelBegin, int channelEnd, BmsAt atBegin, BmsAt atEnd,
			BmsNote.Tester isCounting) {
		return mTlAccessor.countNotes(
				channelBegin, channelEnd,
				atBegin.getMeasure(), atBegin.getTick(),
				atEnd.getMeasure(), atEnd.getTick(),
				isCounting);
	}

	/**
	 * 条件に一致するノートの数を数えます。
	 * <p>全チャンネルをカウント対象にします。<br>
	 * それ以外の処理は{@link #countNotes(int, int, int, double, int, double, BmsNote.Tester)}と同じです。</p>
	 * @param measureBegin カウント範囲を示す最小の小節番号
	 * @param tickBegin カウント範囲を示す最小の小節番号の刻み位置
	 * @param measureEnd カウント範囲を示す最大の小節番号
	 * @param tickEnd カウント範囲を示す最大の小節番号の刻み位置(この位置を含まない)
	 * @param isCounting カウント有無を決定する述語
	 * @return 条件に一致したノートの数
	 */
	public final int countNotes(int measureBegin, double tickBegin, int measureEnd, double tickEnd,
			BmsNote.Tester isCounting) {
		return mTlAccessor.countNotes(
				BmsSpec.CHANNEL_MIN, BmsSpec.CHANNEL_MAX,
				measureBegin, tickBegin,
				measureEnd, tickEnd,
				isCounting);
	}

	/**
	 * 条件に一致するノートの数を数えます。
	 * <p>ノートは指定の述語に通知されます。述語でtrueを返すとそのノートがカウント対象になります。</p>
	 * @param channelBegin カウント範囲を示す最小のチャンネル番号
	 * @param channelEnd カウント範囲を示す最大のチャンネル番号(この番号を含まない)
	 * @param measureBegin カウント範囲を示す最小の小節番号
	 * @param tickBegin カウント範囲を示す最小の小節番号の刻み位置
	 * @param measureEnd カウント範囲を示す最大の小節番号
	 * @param tickEnd カウント範囲を示す最大の小節番号の刻み位置(この位置を含まない)
	 * @param isCounting カウント有無を決定する述語
	 * @return 条件に一致したノートの数
	 * @exception IllegalArgumentException channelBeginに{@link BmsSpec#CHANNEL_MIN}未満または{@link BmsSpec#CHANNEL_MAX}を超える値を指定した
	 * @exception IllegalArgumentException channelEndに{@link BmsSpec#CHANNEL_MIN}未満または{@link BmsSpec#CHANNEL_MAX}+1を超える値を指定した
	 * @exception IllegalArgumentException measureBeginに{@link BmsSpec#MEASURE_MIN}未満または{@link BmsSpec#MEASURE_MAX}超過の値を指定した
	 * @exception IllegalArgumentException tickBeginにマイナス値または当該小節の刻み数以上の値を指定した
	 * @exception IllegalArgumentException measureEndに{@link BmsSpec#MEASURE_MIN}未満または{@link BmsSpec#MEASURE_MAX}超過の値を指定した
	 * @exception IllegalArgumentException tickEndにマイナス値、当該小節の刻み数以上、または小節番号==小節数の時に0以外の値を指定した
	 * @exception NullPointerException isCountingがnull
	 */
	public final int countNotes(int channelBegin, int channelEnd,
			int measureBegin, double tickBegin, int measureEnd, double tickEnd, BmsNote.Tester isCounting) {
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
	 * それ以外の処理は{@link #putNote(int, int, int, double, int, BmsNote.Creator)}と同じです。 </p>
	 * @param channel チャンネル番号
	 * @param measure 小節番号
	 * @param tick 小節の刻み位置
	 * @param value ノートの値
	 * @return 譜面に追加された新しいノートオブジェクト
	 */
	public final BmsNote putNote(int channel, int measure, double tick, int value) {
		return mTlAccessor.putNote(channel, 0, measure, tick, value, BmsNote.DEFAULT_CREATOR);
	}

	/**
	 * 譜面に新しいノートを追加します。
	 * <p>チャンネルインデックスは0固定です。<br>
	 * 引数atから小節番号・小節の刻み位置を取り出します。<br>
	 * ノートオブジェクトはBmsNoteを使用します。<br>
	 * それ以外の処理は{@link #putNote(int, int, int, double, int, BmsNote.Creator)}と同じです。 </p>
	 * @param channel チャンネル番号
	 * @param at 楽曲位置
	 * @param value ノートの値
	 * @return 譜面に追加された新しいノートオブジェクト
	 */
	public final BmsNote putNote(int channel, BmsAt at, int value) {
		return mTlAccessor.putNote(channel, 0, at.getMeasure(), at.getTick(), value, BmsNote.DEFAULT_CREATOR);
	}

	/**
	 * 譜面に新しいノートを追加します。
	 * <p>ノートオブジェクトはBmsNoteを使用します。<br>
	 * それ以外の処理は{@link #putNote(int, int, int, double, int, BmsNote.Creator)}と同じです。 </p>
	 * @param channel チャンネル番号
	 * @param index チャンネルインデックス
	 * @param measure 小節番号
	 * @param tick 小節の刻み位置
	 * @param value ノートの値
	 * @return 譜面に追加された新しいノートオブジェクト
	 */
	public final BmsNote putNote(int channel, int index, int measure, double tick, int value) {
		return mTlAccessor.putNote(channel, index, measure, tick, value, BmsNote.DEFAULT_CREATOR);
	}

	/**
	 * 譜面に新しいノートを追加します。
	 * <p>引数atから小節番号・小節の刻み位置を取り出します。<br>
	 * ノートオブジェクトはBmsNoteを使用します。<br>
	 * それ以外の処理は{@link #putNote(int, int, int, double, int, BmsNote.Creator)}と同じです。 </p>
	 * @param channel チャンネル番号
	 * @param index チャンネルインデックス
	 * @param at 楽曲位置
	 * @param value ノートの値
	 * @return 譜面に追加された新しいノートオブジェクト
	 */
	public final BmsNote putNote(int channel, int index, BmsAt at, int value) {
		return mTlAccessor.putNote(channel, index, at.getMeasure(), at.getTick(), value, BmsNote.DEFAULT_CREATOR);
	}

	/**
	 * 譜面に新しいノートを追加します。
	 * <p>チャンネルインデックスは0固定です。<br>
	 * それ以外の処理は{@link #putNote(int, int, int, double, int, BmsNote.Creator)}と同じです。 </p>
	 * @param <T> 追加するノートのデータ型
	 * @param channel チャンネル番号
	 * @param measure 小節番号
	 * @param tick 小節の刻み位置
	 * @param value ノートの値
	 * @param createNote ノートオブジェクトを生成するサプライヤ
	 * @return 譜面に追加された新しいノートオブジェクト
	 */
	public final <T extends BmsNote> T putNote(int channel, int measure, double tick, int value, BmsNote.Creator createNote) {
		return mTlAccessor.putNote(channel, 0, measure, tick, value, createNote);
	}

	/**
	 * 譜面に新しいノートを追加します。
	 * <p>チャンネルインデックスは0固定です。<br>
	 * 引数atから小節番号・小節の刻み位置を取り出します。<br>
	 * それ以外の処理は{@link #putNote(int, int, int, double, int, BmsNote.Creator)}と同じです。 </p>
	 * @param <T> 追加するノートのデータ型
	 * @param channel チャンネル番号
	 * @param at 楽曲位置
	 * @param value ノートの値
	 * @param createNote ノートオブジェクトを生成するサプライヤ
	 * @return 譜面に追加された新しいノートオブジェクト
	 */
	public final <T extends BmsNote> T putNote(int channel, BmsAt at, int value, BmsNote.Creator createNote) {
		return mTlAccessor.putNote(channel, 0, at.getMeasure(), at.getTick(), value, createNote);
	}

	/**
	 * 譜面に新しいノートを追加します。
	 * <p>引数atから小節番号・小節の刻み位置を取り出します。<br>
	 * それ以外の処理は{@link #putNote(int, int, int, double, int, BmsNote.Creator)}と同じです。 </p>
	 * @param <T> 追加するノートのデータ型
	 * @param channel チャンネル番号
	 * @param index チャンネルインデックス
	 * @param at 楽曲位置
	 * @param value ノートの値
	 * @param createNote ノートオブジェクトを生成するサプライヤ
	 * @return 譜面に追加された新しいノートオブジェクト
	 */
	public final <T extends BmsNote> T putNote(int channel, int index, BmsAt at, int value, BmsNote.Creator createNote) {
		return mTlAccessor.putNote(channel, index, at.getMeasure(), at.getTick(), value, createNote);
	}

	/**
	 * 譜面に新しいノートを追加します。
	 * <p>指定した場所に既にノートが存在する場合は新しいノートで上書きします。</p>
	 * <p>ノートを追加できるチャンネルは、データ型が配列型であるチャンネルのみです。</p>
	 * <p>複数のデータを保有可能なチャンネルに対してのみ、チャンネルインデックスに1以上の値を指定できます。
	 * 複数データに非対応のチャンネルには必ず0を指定してください。</p>
	 * <p>ノートに拡張データを持たせる場合は、createNoteサプライヤからBmsNoteを継承したオブジェクトを返し、
	 * そのオブジェクトに対して拡張データを設定してください。オブジェクトのインスタンスはBMSコンテンツ内部でも
	 * 管理されるようになります。</p>
	 * <p>ノートの値に設定可能な値の範囲は16進配列の場合は1～255、36進配列の場合は1～1295になります。</p>
	 * <p>ノートの値に0を指定することは出来ません。値0は「ノートとして存在しない」ことを示す特殊な値として扱われます。
	 * そのためBMSライブラリでは、明確に存在するノートに対して値0を設定することを許可していません。</p>
	 * @param <T> 追加するノートのデータ型
	 * @param channel チャンネル番号
	 * @param index チャンネルインデックス
	 * @param measure 小節番号
	 * @param tick 小節の刻み位置
	 * @param value ノートの値
	 * @param createNote ノートオブジェクトを生成するサプライヤ
	 * @return 譜面に追加された新しいノートオブジェクト
	 * @exception IllegalStateException 動作モードが編集モードではない
	 * @exception IllegalArgumentException BMS仕様に無いチャンネル番号を指定した
	 * @exception IndexOutOfBoundsException チャンネルインデックスがマイナス値
	 * @exception IndexOutOfBoundsException チャンネルデータ重複不許可のチャンネルで0以外のチャンネルインデックスを指定した
	 * @exception IllegalArgumentException 小節番号が{@link BmsSpec#MEASURE_MIN}未満または{@link BmsSpec#MEASURE_MAX}超過
	 * @exception IllegalArgumentException 小節の刻み位置がマイナス値または当該小節の刻み数以上
	 * @exception IllegalArgumentException 指定チャンネルのデータ型が配列型ではない
	 * @exception IllegalArgumentException 16進配列チャンネル指定時、ノートの値に{@link BmsSpec#VALUE_16_MIN}未満、{@link BmsSpec#VALUE_16_MAX}超過の値を指定した
	 * @exception IllegalArgumentException 36進配列チャンネル指定時、ノートの値に{@link BmsSpec#VALUE_MIN}未満、{@link BmsSpec#VALUE_MAX}超過の値を指定した
	 * @exception NullPointerException createNoteがnull
	 * @exception IllegalArgumentException createNoteの結果がnull
	 */
	public final <T extends BmsNote> T putNote(int channel, int index, int measure, double tick, int value,
			BmsNote.Creator createNote) {
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
	public final boolean removeNote(int channel, int measure, double tick) {
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
	 * @exception NullPointerException atがnull
	 */
	public final boolean removeNote(int channel, BmsAt at) {
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
	 * @exception NullPointerException atがnull
	 */
	public final boolean removeNote(int channel, int index, BmsAt at) {
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
	 * @exception IllegalStateException 動作モードが編集モードではない
	 * @exception IllegalArgumentException BMS仕様に無いチャンネル番号を指定した
	 * @exception IndexOutOfBoundsException チャンネルインデックスがマイナス値
	 * @exception IndexOutOfBoundsException 重複不許可のチャンネルで0以外のインデックスを指定した
	 * @exception IllegalArgumentException 小節番号が{@link BmsSpec#MEASURE_MIN}未満または{@link BmsSpec#MEASURE_MAX}超過
	 * @exception IllegalArgumentException 小節の刻み位置がマイナス値または当該小節の刻み数以上
	 * @exception IllegalArgumentException 指定チャンネルのデータ型が配列型ではない
	 */
	public final boolean removeNote(int channel, int index, int measure, double tick) {
		return mTlAccessor.removeNote(channel, index, measure, tick);
	}

	/**
	 * 選択されたノートを消去します。
	 * @param isRemoveTarget 消去対象ノートを選択する述語
	 * @return 消去されたノートの個数
	 * @exception IllegalStateException 動作モードが編集モードではない
	 * @exception NullPointerException isRemoveTargetがnull
	 */
	public final int removeNote(BmsNote.Tester isRemoveTarget) {
		return mTlAccessor.removeNote(1, BmsSpec.CHANNEL_MAX + 1, 0, mTlAccessor.getCount(), isRemoveTarget);
	}

	/**
	 * 指定範囲の選択されたノートを消去します。
	 * <p>チャンネルの選択範囲は全チャンネルになります。<br>
	 * それ以外の処理は{@link #removeNote(int, int, int, int, BmsNote.Tester)}と同じです。</p>
	 * @param measureBegin 消去対象の最小小節番号
	 * @param measureEnd 消去対象の最大小節番号(この小節を含まない)
	 * @param isRemoveTarget 指定範囲で消去対象を選択する述語
	 * @return 消去されたノートの個数
	 */
	public final int removeNote(int measureBegin, int measureEnd, BmsNote.Tester isRemoveTarget) {
		return mTlAccessor.removeNote(BmsSpec.CHANNEL_MIN, BmsSpec.CHANNEL_MAX, measureBegin, measureEnd, isRemoveTarget);
	}

	/**
	 * 指定範囲の選択されたノートを消去します。
	 * @param channelBegin 消去対象の最小チャンネル番号
	 * @param channelEnd 消去対象の最大チャンネル番号(この番号を含まない)
	 * @param measureBegin 消去対象の最小小節番号
	 * @param measureEnd 消去対象の最大小節番号(この小節を含まない)
	 * @param isRemoveTarget 指定範囲で消去対象を選択する述語
	 * @return 消去されたノートの個数
	 * @exception IllegalStateException 動作モードが編集モードではない
	 * @exception IllegalArgumentException channelBeginに小節データ・ノートを登録できないチャンネル番号を指定した
	 * @exception IllegalArgumentException channelEndに{@link BmsSpec#CHANNEL_MAX}より大きいチャンネル番号を指定した
	 * @exception IllegalArgumentException measureBeginにノート・小節データの存在し得ない小節番号を指定した
	 * @exception IllegalArgumentException measureEndに現在の小節数より大きい小節番号を指定した
	 * @exception NullPointerException isRemoveTargetがnull
	 */
	public final int removeNote(int channelBegin, int channelEnd, int measureBegin, int measureEnd,
			BmsNote.Tester isRemoveTarget) {
		return mTlAccessor.removeNote(channelBegin, channelEnd, measureBegin, measureEnd, isRemoveTarget);
	}

	/**
	 * 楽曲の小節数を取得します。
	 * @return 小節数
	 */
	public final int getMeasureCount() {
		return mTlAccessor.getCount();
	}

	/**
	 * 小節の刻み数を取得します。
	 * <p>小節番号には{@link BmsSpec#MEASURE_MIN}～{@link BmsSpec#MEASURE_MAX}を指定することができます。</p>
	 * <p>小節の刻み数は、{@link BmsSpec#TICK_COUNT_DEFAULT}にその小節の小節長を乗算した値となります。小節長に極端に
	 * 小さい値を設定すると計算上の小節の刻み数が1未満になることがありますが、当メソッドが返す最小の刻み数は
	 * 1 になります。</p>
	 * @param measure 小節番号
	 * @return 小節の刻み数
	 * @exception IllegalArgumentException 小節番号が{@link BmsSpec#MEASURE_MIN}未満または{@link BmsSpec#MEASURE_MAX}超過
	 */
	public final int getMeasureTickCount(int measure) {
		return mTlAccessor.getTickCount(measure);
	}

	/**
	 * 指定小節に格納されたチャンネルデータの数を取得します。
	 * <p>小節番号には{@link BmsSpec#MEASURE_MIN}～{@link BmsSpec#MEASURE_MAX}を指定することができます。</p>
	 * <p>チャンネルデータは通常1個まで格納可能ですが、重複可能チャンネルにすることで小節に複数のチャンネルデータを
	 * 格納することができるようになります。</p>
	 * @param channel チャンネル番号
	 * @param measure 小節番号
	 * @return チャンネルデータの数
	 * @exception IllegalArgumentException 小節データ・ノートを登録できないチャンネル番号を指定した
	 * @exception IllegalArgumentException 小節番号が{@link BmsSpec#MEASURE_MIN}未満または{@link BmsSpec#MEASURE_MAX}超過
	 */
	public final int getChannelDataCount(int channel, int measure) {
		return mTlAccessor.getChannelDataCount(channel, measure);
	}

	/**
	 * 指定位置に小節を挿入します。
	 * <p>挿入位置に1個の小節を挿入します。<br>
	 * それ以外の処理は{@link #insertMeasure(int, int)}と同じです。</p>
	 * @param measureWhere 挿入位置の小節番号
	 */
	public final void insertMeasure(int measureWhere) {
		mTlAccessor.insert(measureWhere, 1);
	}

	/**
	 * 指定位置に小節を挿入します。
	 * <p>小節を挿入すると、挿入位置よりも後ろの小節データは後ろにずれます。</p>
	 * <p>挿入された小節は、小節データが未設定でノートが無い状態になります。また、小節長は4/4拍子になります。</p>
	 * <p>挿入の結果、小節の数が{@link BmsSpec#MEASURE_MAX_COUNT}を超えないように注意してください。</p>
	 * @param measureWhere 挿入位置の小節番号
	 * @param count 挿入する小節数
	 * @exception IllegalStateException 動作モードが編集モードではない
	 * @exception IllegalArgumentException 挿入位置に現在の小節数より大きい小節番号を指定した
	 * @exception IllegalArgumentException 挿入する小節数にマイナス値を指定した
	 * @exception IllegalArgumentException 挿入により小節数が{@link BmsSpec#MEASURE_MAX_COUNT}を超える
	 */
	public final void insertMeasure(int measureWhere, int count) {
		mTlAccessor.insert(measureWhere, count);
	}

	/**
	 * 指定位置の小節を消去します。
	 * <p>消去位置の小節を1個消去します。<br>
	 * それ以外の処理は{@link #removeMeasure(int, int)}と同じです。</p>
	 * @param measureWhere 消去位置の小節番号
	 */
	public final void removeMeasure(int measureWhere) {
		mTlAccessor.remove(measureWhere, 1);
	}

	/**
	 * 指定位置の小節を消去します。
	 * <p>小節を消去すると、消去位置よりも後ろの小節データは手前にずれます。</p>
	 * <p>小節の存在しない領域を巻き込んで消去しないように注意してください。</p>
	 * @param measureWhere 消去位置の小節番号
	 * @param count 消去する小節数
	 * @exception IllegalStateException 動作モードが編集モードではない
	 * @exception IllegalArgumentException 消去位置に現在の小節数より大きい小節番号を指定した
	 * @exception IllegalArgumentException 消去する小節数にマイナス値を指定した
	 * @exception IllegalArgumentException 存在しない小節を消去しようとした
	 */
	public final void removeMeasure(int measureWhere, int count) {
		mTlAccessor.remove(measureWhere, count);
	}

	/**
	 * 指定チャンネル同士のデータ内容を入れ替えます。
	 * <p>チャンネルインデックスは入れ替え対象チャンネル1,2共に0を指定します。<br>
	 * それ以外の処理は{@link #swapChannel(int, int, int, int)}と同じです。</p>
	 * @param channel1 入れ替え対象チャンネル1の番号
	 * @param channel2 入れ替え対象チャンネル2の番号
	 */
	public final void swapChannel(int channel1, int channel2) {
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
	 * @exception IllegalStateException 動作モードが編集モードではない
	 * @exception IllegalArgumentException BMS仕様に無いチャンネル番号を指定した
	 * @exception IndexOutOfBoundsException チャンネルインデックスがマイナス値
	 * @exception IndexOutOfBoundsException 重複不許可のチャンネルで0以外のインデックスを指定した
	 * @exception IllegalArgumentException 指定チャンネルが小節長変更・BPM変更・譜面停止のいずれかだった
	 * @exception IllegalArgumentException チャンネル1,2のデータ型が一致しない
	 */
	public final void swapChannel(int channel1, int index1, int channel2, int index2) {
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
	 * @exception NullPointerException atFromがnull
	 */
	public final BmsPoint seekPoint(BmsAt atFrom, double offsetTick, BmsPoint outPoint) {
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
	 * @exception IllegalArgumentException 原点の小節番号にノート・小節データが存在し得ない値を指定した
	 * @exception IllegalArgumentException 原点の刻み位置にマイナス値または当該小節の刻み数以上の値を指定した
	 * @exception NullPointerException outPointがnull
	 */
	public final BmsPoint seekPoint(int measureFrom, double tickFrom, double offsetTick, BmsPoint outPoint) {
		assertArgNotNull(outPoint, "outPoint");
		return mTlAccessor.seekPoint(measureFrom, tickFrom, offsetTick, outPoint);
	}

	/**
	 * 指定楽曲位置より後の位置に存在する直近のノートを検索し、その楽曲位置を返します。
	 * <p>引数atから小節番号、刻み位置を取り出します。<br>
	 * 全てのチャンネルを検索対象とします。<br>
	 * それ以外の処理は{@link #seekNextPoint(int, double, boolean, BmsChannel.Tester, BmsPoint)}と同じです。</p>
	 * @param at 検索を開始する楽曲位置
	 * @param inclusiveFrom 指定楽曲位置を検索対象に含めるかどうか
	 * @param outPoint 直近のノートの楽曲位置
	 * @return 引数outPointが示すBmsPointオブジェクトの参照
	 * @exception NullPointerException atがnull
	 */
	public final BmsPoint seekNextPoint(BmsAt at, boolean inclusiveFrom, BmsPoint outPoint) {
		assertArgNotNull(at, "at");
		return mTlAccessor.seekNextPoint(at.getMeasure(), at.getTick(), inclusiveFrom, c -> true, outPoint);
	}

	/**
	 * 指定楽曲位置より後の位置に存在する直近のノートを検索し、その楽曲位置を返します。
	 * <p>全てのチャンネルを検索対象とします。<br>
	 * それ以外の処理は{@link #seekNextPoint(int, double, boolean, BmsChannel.Tester, BmsPoint)}と同じです。</p>
	 * @param measure 検索を開始する小節番号
	 * @param tick 検索を開始する刻み位置
	 * @param inclusiveFrom 指定楽曲位置を検索対象に含めるかどうか
	 * @param outPoint 直近のノートの楽曲位置
	 * @return 引数outPointが示すBmsPointオブジェクトの参照
	 */
	public final BmsPoint seekNextPoint(int measure, double tick, boolean inclusiveFrom, BmsPoint outPoint) {
		return mTlAccessor.seekNextPoint(measure, tick, inclusiveFrom, c -> true, outPoint);
	}

	/**
	 * 指定楽曲位置より後の位置に存在する直近のノートを検索し、その楽曲位置を返します。
	 * <p>引数atから小節番号、刻み位置を取り出します。<br>
	 * それ以外の処理は{@link #seekNextPoint(int, double, boolean, BmsChannel.Tester, BmsPoint)}と同じです。</p>
	 * @param at 検索を開始する楽曲位置
	 * @param inclusiveFrom 指定楽曲位置を検索対象に含めるかどうか
	 * @param chTester チャンネルを検索対象とするかを判定するテスター
	 * @param outPoint 直近のノートの楽曲位置
	 * @return 引数outPointが示すBmsPointオブジェクトの参照
	 * @exception NullPointerException atがnull
	 */
	public final BmsPoint seekNextPoint(BmsAt at, boolean inclusiveFrom, BmsChannel.Tester chTester, BmsPoint outPoint) {
		assertArgNotNull(at, "at");
		return mTlAccessor.seekNextPoint(at.getMeasure(), at.getTick(), inclusiveFrom, chTester, outPoint);
	}

	/**
	 * 指定楽曲位置より後の位置に存在する直近のノート、小節データ、または小節線を検索し、その楽曲位置を返します。
	 * <p>特定のチャンネルのノートを検索対象にしたくない場合は引数chTesterでfalseを返すことで検索対象外に
	 * することが出来ます。但しこのテスターは頻繁に呼び出される可能性があるのでできるだけ軽い処理とするべきです。</p>
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
	 * @exception IllegalArgumentException 小節番号がマイナス値または小節数以上
	 * @exception IllegalArgumentException 小節の刻み位置がマイナス値または当該小節の刻み数以上
	 * @exception NullPointerException chTesterがnull
	 * @exception NullPointerException outPointがnull
	 */
	public final BmsPoint seekNextPoint(int measure, double tick, boolean inclusiveFrom, BmsChannel.Tester chTester,
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
	 * @exception NullPointerException atがnull
	 * @exception IllegalArgumentException 小節番号がマイナス値
	 * @exception IllegalArgumentException 小節の刻み位置が{@link BmsSpec#TICK_MIN}以外の時、小節番号が小節数以上
	 * @exception IllegalArgumentException 小節の刻み位置が{@link BmsSpec#TICK_MIN}の時、小節番号が小節数超過
	 * @exception IllegalArgumentException 小節の刻み位置が{@link BmsSpec#TICK_MIN}未満、または{@link BmsSpec#TICK_MAX}超過
	 */
	public final Stream<BmsElement> timeline(BmsAt at) {
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
	 * @exception IllegalArgumentException 小節番号がマイナス値
	 * @exception IllegalArgumentException 小節の刻み位置が{@link BmsSpec#TICK_MIN}以外の時、小節番号が小節数以上
	 * @exception IllegalArgumentException 小節の刻み位置が{@link BmsSpec#TICK_MIN}の時、小節番号が小節数超過
	 * @exception IllegalArgumentException 小節の刻み位置が{@link BmsSpec#TICK_MIN}未満、または{@link BmsSpec#TICK_MAX}超過
	 */
	public final Stream<BmsElement> timeline(int measure, double tick) {
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
	 */
	public final Stream<BmsElement> timeline() {
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
	 * @exception NullPointerException atBeginまたはatEndがnull
	 * @exception IllegalArgumentException 走査開始/終了楽曲位置の小節番号がマイナス値
	 * @exception IllegalArgumentException 走査終了楽曲位置の小節の刻み位置が{@link BmsSpec#TICK_MIN}以外の時、小節番号が小節数以上
	 * @exception IllegalArgumentException 走査終了楽曲位置の小節の刻み位置が{@link BmsSpec#TICK_MIN}の時、小節番号が小節数超過
	 * @exception IllegalArgumentException 走査開始/終了楽曲位置の楽曲位置の小節の刻み位置が{@link BmsSpec#TICK_MIN}未満、または{@link BmsSpec#TICK_MAX}超過
	 * @exception IllegalArgumentException atEndがatBeginと同じまたは手前の楽曲位置を示している
	 */
	public final Stream<BmsElement> timeline(BmsAt atBegin, BmsAt atEnd) {
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
	 * @exception IllegalArgumentException 走査開始/終了楽曲位置の小節番号がマイナス値
	 * @exception IllegalArgumentException 走査楽曲位置の小節の刻み位置が{@link BmsSpec#TICK_MIN}以外の時、小節番号が小節数以上
	 * @exception IllegalArgumentException 走査楽曲位置の小節の刻み位置が{@link BmsSpec#TICK_MIN}の時、小節番号が小節数超過
	 * @exception IllegalArgumentException 走査開始/終了楽曲位置の小節の刻み位置が{@link BmsSpec#TICK_MIN}未満、または{@link BmsSpec#TICK_MAX}超過
	 * @exception IllegalArgumentException 走査終了楽曲位置が走査開始楽曲位置と同じまたは手前の楽曲位置を示している
	 */
	public final Stream<BmsElement> timeline(int measureBegin, double tickBegin, int measureEnd, double tickEnd) {
		return mTlAccessor.timeline(measureBegin, tickBegin, measureEnd, tickEnd);
	}

	/**
	 * 小節番号・刻み位置を時間(秒)に変換します。
	 * <p>引数atから小節番号・刻み位置を取り出します。<br>
	 * それ以外の処理は{@link #pointToTime(int, double)}と同じです。</p>
	 * @param at 楽曲位置
	 * @return 時間(秒)
	 * @exception NullPointerException atがnull
	 */
	public final double pointToTime(BmsAt at) {
		return mTlAccessor.pointToTime(at);
	}

	/**
	 * 小節番号・刻み位置を時間(秒)に変換します。
	 * <p>このメソッドは参照モードの場合のみ呼び出すことができます。</p>
	 * <p>小節長が極端に短く、BPMが極端に高い譜面では浮動小数点演算の精度上の理由により、tickと(tick+1)の時間が
	 * 全く同じ値を示すことがあります(小節をまたぐ場合も同様)。一般的な小節長・BPMではこのような問題は発生しません。</p>
	 * @param measure 小節番号
	 * @param tick 小節の刻み位置
	 * @return 時間(秒)
	 * @exception IllegalStateException 動作モードが参照モードではない
	 * @exception IllegalArgumentException 小節番号が{@link BmsSpec#MEASURE_MIN}未満または{@link BmsSpec#MEASURE_MAX}超過
	 * @exception IllegalArgumentException 小節番号が{@link BmsSpec#MEASURE_MAX_COUNT}の時、小節の刻み位置に0以外を指定した
	 * @exception IllegalArgumentException 小節の刻み位置にマイナス値または当該小節の刻み数以上の値を指定した
	 */
	public final double pointToTime(int measure, double tick) {
		mTempAt1.set(measure, tick);
		return mTlAccessor.pointToTime(mTempAt1);
	}

	/**
	 * 時間(秒)を楽曲位置(小節番号/刻み位置)に変換します。
	 * <p>このメソッドは参照モードの場合のみ呼び出すことができます。</p>
	 * <p>指定した時間が楽曲の総時間を超える場合、返される楽曲位置は小節番号が小節数、刻み位置が0になります。</p>
	 * @param timeSec 時間(秒)
	 * @param outPoint 楽曲位置を格納する楽曲位置オブジェクト
	 * @return 引数で指定した楽曲位置オブジェクト
	 * @exception IllegalStateException 動作モードが参照モードではない
	 * @exception IllegalArgumentException 時間にマイナス値を指定した
	 * @exception NullPointerException outPointがnull
	 */
	public final BmsPoint timeToPoint(double timeSec, BmsPoint outPoint) {
		assertArgNotNull(outPoint, "outPoint");
		return mTlAccessor.timeToPoint(timeSec, outPoint);
	}

	/**
	 * BMSコンテンツの内容からハッシュ値を計算し、結果を返します。
	 * <p>当メソッドの目的は「プレーに影響する変更が行われたかどうかを検出すること」にあります。この目的を果たすために、BMSコンテンツの基準となる
	 * BMS仕様に登録されたメタ情報とチャンネルは「同一性チェックを行うかどうか」の情報を保有しています。同一性チェックを行うメタ情報・チャンネルの
	 * データ内容をハッシュ値計算の要素として抽出し、その要素からハッシュ値を計算します。</p>
	 * <p>プレーに影響しない(とBMS仕様で定められた)情報は、どのように修正されてもハッシュ値が変化することはありません。例えば、譜面には
	 * 直接関係の無いバックグラウンドアニメーションなどは後から差し替えてもハッシュ値が変化する(別の譜面として認識される)べきではありません。
	 * このように、制作者側に対してプレーする譜面以外の箇所に対して後から更新する自由度が割り当てられます。</p>
	 * <p>逆に、全く同じタイトル、アーティスト、BPM等でも、プレー可能なチャンネルのノートを移動するような修正が加えられた場合当然ながら別譜面と
	 * 認識されるべきであり、そのような情報が「同一性チェック対象の情報」となり得ることになります。</p>
	 * <p>当メソッドの処理には時間がかかります。また、処理中は多くのメモリ領域を使用することになるため、何度も呼び出すとアプリケーションの
	 * パフォーマンスが大幅に低下する可能性があります。当メソッドの呼び出し回数は極力減らすようアプリケーションを設計してください。</p>
	 * @return BMSコンテンツから算出されたハッシュ値
	 * @exception IllegalStateException 動作モードが参照モードではない
	 */
	public final byte[] generateHash() {
		assertIsReferenceMode();

		// ハッシュ値計算用データを生成する
		var hashInput = createHashSeed().getBytes(StandardCharsets.UTF_8);

		// ハッシュ値を生成する
		try {
			var msgDigest = MessageDigest.getInstance("SHA-1");
			return msgDigest.digest(hashInput);
		} catch (NoSuchAlgorithmException e) {
			// MessageDigestオブジェクト生成失敗は想定しない
			return new byte[0];
		}
	}

	/**
	 * ハッシュ値計算用データを出力する。
	 * @param os 出力先ストリーム
	 */
	final void printHashSeed(OutputStream os) {
		var ps = (os instanceof PrintStream) ? (PrintStream) os : new PrintStream(os);
		ps.println(createHashSeed());
	}

	/**
	 * ハッシュ値計算用データを生成する
	 * @return ハッシュ値計算用データ
	 */
	private String createHashSeed() {
		// ハッシュ値計算用データ
		var data = new StringBuilder(1024 * 256);
		data.append("BMS");

		// 同一性チェック対象のメタ情報からハッシュ値計算用データを収集する
		data.append("META");
		var metas = mSpec.getMetas(m -> m.isUniqueness(), BmsMeta.COMPARATOR_BY_ORDER);
		for (var meta : metas) {
			// 構成単位ごとに処理を振り分ける
			var name = meta.getName();
			var unit = meta.getUnit();
			if (unit == BmsUnit.SINGLE) {
				// 単一メタ情報
				if (mMetas.containsSingleMeta(name)) {
					data.append("S");
					data.append(name);
					data.append("V");
					data.append(mMetas.getSingleMeta(name).toString());
				}
			} else if (unit == BmsUnit.MULTIPLE) {
				// 複数メタ情報
				var values = mMetas.getMultipleMetas(name);
				var count = values.size();
				if (count > 0) {
					data.append("M");
					data.append(name);
					data.append("C");
					data.append(String.valueOf(count));
					for (var i = 0; i < count; i++) {
						data.append("I");
						data.append(String.valueOf(i));
						data.append("V");
						data.append(values.get(i).toString());
					}
				}
			} else if (unit == BmsUnit.INDEXED) {
				// 索引付きメタ情報
				var entries = mMetas.getIndexedMetas(name);
				if (entries.size() > 0) {
					data.append("I");
					data.append(name);
					data.append("C");
					data.append(String.valueOf(entries.size()));
					for (var entry : entries.entrySet()) {
						data.append("K");
						data.append(entry.getKey().toString());
						data.append("V");
						data.append(entry.getValue().toString());
					}
				}
			} else {
				// 想定しない
			}
		}

		// 同一性チェック対象のチャンネルを収集する
		var channels = mSpec.getChannels(
				c -> c.isUniqueness(),
				(c1, c2) -> Integer.compare(c1.getNumber(), c2.getNumber()));

		// 同一性チェック対象のチャンネルからハッシュ値計算用データを収集する
		data.append("CHANNEL");
		var notes = new ArrayList<BmsNote>();
		var measureCount = mTlAccessor.getCount();
		for (var channel : channels) {
			// データ型による分岐
			var chNum = channel.getNumber();
			var outChno = false;
			if (channel.isValueType()) {
				// 値型の場合
				for (var measure = 0; measure < measureCount; measure++) {
					var dataCount = mTlAccessor.getChannelDataCount(chNum, measure);
					if (dataCount > 0) {
						// 追記するのは、その小節にチャンネルデータが存在する場合のみ
						if (!outChno) {
							outChno = true;
							data.append(String.format("C%d", chNum));
						}
						data.append("M");
						data.append(String.valueOf(measure));
						for (var index = 0; index < dataCount; index++) {
							// チャンネルデータを追記する
							data.append("I");
							data.append(String.valueOf(index));
							data.append("V");
							data.append(mTlAccessor.getValue(chNum, index, measure, null, false).toString());
						}
					}
				}
			} else if (channel.isArrayType()) {
				// 配列型の場合
				for (var measure = 0; measure < measureCount; measure++) {
					var dataCount = mTlAccessor.getChannelDataCount(chNum, measure);
					if (dataCount > 0) {
						// 追記するのは、その小節にチャンネルデータが存在する場合のみ
						if (!outChno) {
							outChno = true;
							data.append(String.format("C%d", chNum));
						}
						data.append("M");
						data.append(String.valueOf(measure));
						for (var index = 0; index < dataCount; index++) {
							// チャンネルデータを追記する
							data.append("I");
							data.append(String.valueOf(index));
							data.append("V");
							mTl.listNotes(chNum, index, measure, notes);
							for (var note : notes) {
								data.append("N");
								data.append("T");
								data.append(String.valueOf(note.getTick()));
								data.append("V");
								data.append(String.valueOf(note.getValue()));
							}
						}
					}
				}
			} else {
				// データ型不明のケースは想定しない
			}
		}

		return data.toString();
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
	 * @exception IllegalStateException 動作モードが参照モードではない
	 */
	protected void assertIsReferenceMode() {
		if (mIsEditMode) {
			throw new IllegalStateException("Now is NOT reference mode.");
		}
	}

	/**
	 * 動作モードが編集モードかどうかをテストするアサーション
	 * @exception IllegalStateException 動作モードが編集モードではない
	 */
	protected void assertIsEditMode() {
		if (!mIsEditMode) {
			throw new IllegalStateException("Now is NOT edit mode.");
		}
	}

	/**
	 * BMS宣言のキー名をテストするアサーション。
	 * @param declName BMS宣言のキー名
	 * @exception NullPointerException declNameがnull
	 */
	private static void assertArgDeclarationName(String declName) {
		assertArgNotNull(declName, "declName");
	}

	/**
	 * メタ情報の単位をテストするアサーション。
	 * @param unit メタ情報の単位
	 * @exception NullPointerException unitがnull
	 */
	private static void assertArgMetaUnit(BmsUnit unit) {
		assertArgNotNull(unit, "unit");
	}
}
