package com.lmt.lib.bms;

import static com.lmt.lib.bms.internal.Assertion.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.lmt.lib.bms.internal.Utility;

/**
 * BMSコンテンツを標準フォーマットで出力するセーバーです。
 *
 * <p>当クラスを通してBMSコンテンツを外部データ出力すると、一般的なBMS仕様に基づく形式となります。
 * 具体的な出力内容については{@link #onWrite(BmsContent, OutputStream)}を参照してください。</p>
 */
public class BmsStandardSaver extends BmsSaver {
	/** 配列データの最大分解能デフォルト値 */
	public static final int PRECISION_DEFAULT = 9600;
	/** 配列データの最大分解能に指定可能な最小の値 */
	public static final int PRECISION_MIN = 192;
	/** 配列データの最大分解能に指定可能な最大の値 */
	public static final int PRECISION_MAX = 76800;

	/** 標準フォーマットで対応する小節数の最大値 */
	public static final int MEASURE_COUNT_MAX = 1000;
	/** 標準フォーマットで対応する索引付きメタ情報のインデックス値の最大値 */
	public static final int META_INDEX_MAX = 1295;

	/** 16進配列の値が標準フォーマットで表現可能な範囲を超過しているかをチェックするテスター */
	private static final BmsNote.Tester TESTER_OVERFLOW_A16 = n -> !BmsSpec.isNoteValue16WithinRange(n.getValue());
	/** 36進配列の値が標準フォーマットで表現可能な範囲を超過しているかをチェックするテスター */
	private static final BmsNote.Tester TESTER_OVERFLOW_A36 = n -> !BmsSpec.isNoteValue36WithinRange(n.getValue());

	/** 小数部を持つ刻み位置が全て整数値になる拡張倍率検索時の最終値 */
	private static final double FIND_INT_MAX = 768.0;

	/** 小数部を持つ刻み位置が存在する場合の配列データ最大分解能 */
	private int mMaxPrecision = PRECISION_DEFAULT;
	/** メタ情報のコメント */
	private List<String> mMetaComments = Collections.emptyList();
	/** チャンネルのコメント */
	private List<String> mChannelComments = Collections.emptyList();
	/** フッターコメント */
	private List<String> mFooterComments = Collections.emptyList();
	/** エンコード時の文字セットリスト(リストの先頭から順にエンコードが試みられる) */
	private List<Charset> mCharsets = Collections.emptyList();

	/**
	 * 小数部を持つ刻み位置が存在する場合の配列データ最大分解能を設定します。
	 * <p>小数部を持つ刻み位置が存在すると最大公約数によるチャンネルの配列データ分割数の計算ができなくなり、
	 * 分割数を容易に決定できなくなります。小数部の値次第ではどの位置にノートの値を記録しても元の値の復元が困難になり、
	 * 最悪の場合、配列の長さが無限個数になっても元の値が復元不可能になることがあります。</p>
	 * <p>当メソッドで指定する配列データの最大分解能は、配列データの最大長を決定するために使用されます。
	 * 指定された値がそのまま実際の配列データの最大要素数になります。
	 * ノートの値は実際の刻み位置に最も近い場所に記録されますが、出力されたBMSコンテンツを再度読み込んでも、
	 * ノートの値は完全に同じ値にはなりません。</p>
	 * <p>分解能を高くすると出力されるBMSコンテンツのサイズが大きくなる可能性がありますが、
	 * 再度読み込んだ時の刻み位置の再現度は高くなります。一方、分解能を低くするとBMSコンテンツのサイズは小さくなりますが、
	 * 刻み位置の再現度が低くなり、刻み位置の近いノート同士の記録場所が競合してしまう危険性が上がります。</p>
	 * <p>分解能のデフォルト値は{@link #PRECISION_DEFAULT}に規定されており、分解能を設定しない場合この値が使用されます。
	 * 分解能の指定可能範囲は{@link #PRECISION_MIN}, {@link #PRECISION_MAX}を参照してください。</p>
	 * @param maxPrecision 配列データ最大分解能
	 * @return このオブジェクトのインスタンス
	 * @exception IllegalArgumentException maxPrecisionが{@link #PRECISION_MIN}未満、または{@link #PRECISION_MAX}超過
	 * @see #PRECISION_DEFAULT
	 * @see #PRECISION_MIN
	 * @see #PRECISION_MAX
	 */
	public final BmsStandardSaver setMaxPrecision(int maxPrecision) {
		assertArgRange(maxPrecision, PRECISION_MIN, PRECISION_MAX, "maxPrecision");
		mMaxPrecision = maxPrecision;
		return this;
	}

	/**
	 * メタ情報コメントを設定します。
	 * <p>コメントの内容は1行につき1文字列、複数行記述する場合は複数の文字列をCollectionに設定して渡してください。</p>
	 * <p>文字列中の改行コード&lt;CR&gt;、&lt;LF&gt;、複数行コメントの開始文字、および末尾の空白文字は消去したうえで出力されます。</p>
	 * <p>コメントが空文字列、または先頭文字がコメント行を表す文字(&quot;;&quot;, &quot;*&quot;, &quot;//&quot;)の場合、
	 * その行にはコメント行を表す文字は付加されません。</p>
	 * @param metaComments メタ情報コメント
	 * @return このオブジェクトのインスタンス
	 * @exception NullPointerException metaCommentsがnull
	 * @exception NullPointerException metaCommentsの中にnullが含まれる
	 */
	public final BmsStandardSaver setMetaComments(Collection<String> metaComments) {
		assertArgNotNull(metaComments, "metaComments");
		mMetaComments = comments(metaComments);
		return this;
	}

	/**
	 * チャンネルコメントを設定します。
	 * <p>コメントの内容は1行につき1文字列、複数行記述する場合は複数の文字列をCollectionに設定して渡してください。</p>
	 * <p>文字列中の改行コード&lt;CR&gt;、&lt;LF&gt;、複数行コメントの開始文字、および末尾の半角スペースとタブは
	 * 消去したうえで出力されます。</p>
	 * <p>コメントが空文字列、または先頭文字がコメント行を表す文字(&quot;;&quot;, &quot;*&quot;, &quot;//&quot;)の場合、
	 * その行にはコメント行を表す文字は付加されません。</p>
	 * @param channelComments チャンネルコメント
	 * @return このオブジェクトのインスタンス
	 * @exception NullPointerException channelCommentsがnull
	 * @exception NullPointerException channelCommentsの中にnullが含まれる
	 */
	public final BmsStandardSaver setChannelComments(Collection<String> channelComments) {
		assertArgNotNull(channelComments, "channelComments");
		mChannelComments = comments(channelComments);
		return this;
	}

	/**
	 * フッターコメントを設定します。
	 * <p>コメントの内容は1行につき1文字列、複数行記述する場合は複数の文字列をCollectionに設定して渡してください。</p>
	 * <p>文字列中の改行コード&lt;CR&gt;、&lt;LF&gt;、複数行コメントの開始文字、および末尾の半角スペースとタブは
	 * 消去したうえで出力されます。</p>
	 * <p>コメントが空文字列、または先頭文字がコメント行を表す文字(&quot;;&quot;, &quot;*&quot;, &quot;//&quot;)の場合、
	 * その行にはコメント行を表す文字は付加されません。</p>
	 * @param footerComments フッターコメント
	 * @return このオブジェクトのインスタンス
	 * @exception NullPointerException footerCommentsがnull
	 * @exception NullPointerException footerCommentsの中にnullが含まれる
	 */
	public final BmsStandardSaver setFooterComments(Collection<String> footerComments) {
		assertArgNotNull(footerComments, "footerComments");
		mFooterComments = comments(footerComments);
		return this;
	}

	/**
	 * BMS書き込み時、BMSコンテンツのエンコードに使用する文字セットを設定します。
	 * <p>同じ文字セットを複数追加しても意味はありません。後方で指定したほうの同一の文字セットが無視されます。</p>
	 * <p>文字セットの登録は省略可能です。省略した場合{@link BmsLibrary#getDefaultCharsets()}を呼び出し、
	 * BMSライブラリのデフォルト文字セットリストを使用してエンコード処理が行われます。
	 * これは、当メソッドで文字セットを1個も指定しなかった場合も同様です。</p>
	 * @param charsets BMSコンテンツのエンコード処理時に使用する文字セットリスト
	 * @return このオブジェクトのインスタンス
	 * @exception NullPointerException charsetsにnullが含まれている
	 * @see BmsLibrary#setDefaultCharsets(Charset...)
	 */
	public final BmsStandardSaver setCharsets(Charset...charsets) {
		mCharsets = Stream.of(charsets)
				.peek(cs -> assertArgNotNull(cs, "charsets[?]"))
				.distinct()
				.collect(Collectors.toList());
		return this;
	}

	/**
	 * 指定された出力ストリームへ標準フォーマットでBMSコンテンツを出力します。
	 * <p>当クラスによる標準フォーマットのBMSコンテンツ出力では、以下の順に各要素が出力されます。<br>
	 * 1. BMS宣言(存在する場合のみ)<br>
	 * 2. メタ情報コメント(指定した場合のみ)<br>
	 * 3. メタ情報<br>
	 * 4. チャンネルコメント(指定した場合のみ)<br>
	 * 5. チャンネルデータ<br>
	 * 6. フッターコメント(指定した場合のみ)</p>
	 * <p>指定BMSコンテンツに1件でもBMS宣言が存在する場合、BMSの先頭行には";?bms"が出力されます。</p>
	 * <p>メタ情報は原則としてBMS仕様で規定されたメタ情報のソート順で出力されますが、
	 * 構成単位が単体・複数のメタ情報が先に出力され、その後で残りの索引付きメタ情報が出力されます。</p>
	 * <p>出力するBMSコンテンツは{@link #setCharsets(Charset...)}で指定した順の文字セットでテキストをエンコードします。
	 * 文字セットを指定しなかった場合は{@link BmsLibrary#getDefaultCharsets()}
	 * でBMSライブラリのデフォルト文字セットリストを取得し使用します。全ての文字セットでテキストのエンコードに失敗した場合、
	 * BMSコンテンツの出力は中止され{@link BmsCompatException}をスローします。</p>
	 * <p>小数点以下の刻み位置を持つノートが含まれるチャンネルデータで出力位置に端数が発生する場合、
	 * 配列データの最大分解能の範囲で最も近い場所にノートの値を出力します。
	 * 分解能が不足していると、刻み位置の値が近いノート同士が同じ場所に出力しようとする現象が発生する場合があります。
	 * その場合先に出力したほうの値が上書きされてしまい、データの欠落が発生することになりますので、
	 * 上書きを検出した時点でBMSコンテンツの出力は中止され{@link BmsCompatException}をスローします。</p>
	 * <p>標準フォーマットのBMSでは小節数、索引付きメタ情報のインデックス値などで上限値の制約が厳しく、
	 * 楽曲の構成次第では標準フォーマットでは全ての情報を完全に出力できない場合があります。
	 * そのようなケースでは、出力できない情報を欠落させ不完全な状態で出力するようなことはせず、
	 * 出力を中止して{@link BmsCompatException}をスローします。</p>
	 * @param content 出力対象のBMSコンテンツ
	 * @param dst 出力先ストリーム
	 * @exception IOException dstへのBMSコンテンツ出力時に入出力エラーが発生した
	 * @exception BmsException BMSに関連する要因、または出力処理で例外がスローされエラー終了した
	 * @exception BmsCompatException 指定された全ての文字セットでテキストのエンコードが失敗した
	 * @exception BmsCompatException 配列データの分解能不足によりチャンネルデータの欠落が発生した
	 * @exception BmsCompatException 楽曲の構成が標準フォーマットのデータ表現可能範囲を超えた
	 */
	protected void onWrite(BmsContent content, OutputStream dst) throws IOException, BmsException {
		// 指定BMSコンテンツを標準フォーマットで完全に表現可能かをチェックする
		checkContentCompatible(content);

		// 書き込み用のWriterを生成する
		var sw = new StringWriter();
		var pw = new PrintWriter(sw);
		var needEmptyLine = false;

		// BMS宣言を出力する
		var decls = content.getDeclarations();
		var outDecls = (decls.size() > 0);
		if (outDecls) {
			// BMS宣言が存在する場合のみ出力する
			var sb = new StringBuilder();
			sb.append(";?bms");
			decls.forEach((k, v) -> sb.append(String.format(" %s=\"%s\"", k, v)));
			pw.println(sb.toString());
			needEmptyLine = true;
		}

		// メタ情報の並び順を決定する
		// メタ情報は原則、ソート順に従って出力するが、構成単位が単体・複数のものを先に出力し、
		// その後で索引付きメタ情報を出力するように並び順を調整する。
		var metas = content.getSpec().getMetas();
		var orderedMetas = new ArrayList<BmsMeta>(metas.size());
		orderedMetas.addAll(metas.stream().filter(s -> s.getUnit() != BmsUnit.INDEXED).collect(Collectors.toList()));
		orderedMetas.addAll(metas.stream().filter(s -> s.getUnit() == BmsUnit.INDEXED).collect(Collectors.toList()));

		// メタ情報を出力する
		var outMetaFirst = true;
		for (var meta : orderedMetas) {
			var name = meta.getName();
			var unit = meta.getUnit();
			var type = meta.getType();
			if (content.getMetaCount(name, unit) == 0) {
				continue;
			}

			// 空行を出力する
			if ((outMetaFirst && needEmptyLine) || (!outMetaFirst && (meta.getUnit() == BmsUnit.INDEXED))) {
				pw.println();
			}

			// メタ情報コメントを出力する
			if (outMetaFirst) {
				outMetaFirst = false;
				mMetaComments.forEach(c -> writeComment(pw, c));
			}

			// 構成単位ごとのメタ情報の出力処理
			var outName = name.toUpperCase();
			switch (unit) {
			case SINGLE:  // 単一データ
				pw.println(String.format("%s %s",
						outName, bmsValueToString(content.getSingleMeta(name), type)));
				break;
			case MULTIPLE:  // 複数データ
				content.getMultipleMetas(name).forEach(v -> {
					pw.println(String.format("%s %s", outName, bmsValueToString(v, type)));
				});
				break;
			case INDEXED:  // 索引付きデータ
				content.getIndexedMetas(name).forEach((i, v) -> {
					String index = BmsInt.to36s(i);
					String value = bmsValueToString(v, type);
					pw.println(String.format("%s%s %s", outName, index, value));
				});
				break;
			default:  // 不明
				// コーディングの便宜上defaultを記述しているが、ここを通ることは想定しない
				break;
			}

			// チャンネル出力時に空行が必要
			needEmptyLine = true;
		}

		// チャンネルデータを出力する
		var outChFirst = true;
		var channelSpecs = content.getSpec().getChannels();
		var measureCount = content.getMeasureCount();
		for (var i = 0; i < measureCount; i++) {
			// BMS仕様に定義済みの全チャンネルを出力対象とする
			final var measure = i;
			var outMeasureFirst = true;
			for (var channelSpec : channelSpecs) {
				var channel = channelSpec.getNumber();
				var dataCount = content.getChannelDataCount(channel, measure);
				if (dataCount == 0) {
					continue;
				}

				// チャンネル出力初回で必要時、2小節目以降の小節単位での初回時に空行を出力する
				if ((outChFirst && needEmptyLine) || (!outChFirst && outMeasureFirst)) {
					pw.println();
				}

				// チャンネルコメントを出力する
				if (outChFirst) {
					outChFirst = false;
					mChannelComments.forEach(c -> writeComment(pw, c));
				}

				// チャンネルのデータ型ごとに処理を分岐
				var chType = channelSpec.getType();
				var channelStr = BmsInt.to36s(channel);
				for (var j = 0; j < dataCount; j++) {
					String value;
					if (chType.isValueType()) {
						// 値型の場合、定義値の文字列表現を生成する
						value = bmsValueToString(content.getMeasureValue(channel, j, measure), chType);
					} else if (chType.isArrayType()) {
						// 配列型の場合、ノートの配置から配列データの文字列表現を生成する
						value = notesToString(content, channel, j, measure, chType.getRadix());
					} else {
						// Don't care
						value = "";
					}
					pw.println(String.format("#%03d%s:%s", measure, channelStr, value));
				}

				needEmptyLine = true;  // フッターコメント出力時に空行が必要
				outMeasureFirst = false;
			}
		}

		// フッターコメントを出力する
		if (needEmptyLine && (mFooterComments.size() > 0)) {
			pw.println();
		}
		mFooterComments.forEach(c -> writeComment(pw, c));

		// 出力完了
		pw.close();

		// 指定文字セットでのテキストのエンコードを試行する
		var charsets = mCharsets.isEmpty() ? BmsLibrary.getDefaultCharsets() : mCharsets;
		var bmsText = sw.toString();
		var outBytes = (byte[])null;
		for (var i = 0; (outBytes == null) && (i < charsets.size()); i++) {
			// 優先順位の高い文字セットから順にエンコードを試行する
			outBytes = encodeText(bmsText, charsets.get(i));
		}
		if (outBytes == null) {
			// 全ての文字セットでのエンコードに失敗した
			var csListStr = charsets.stream().map(cs -> cs.name()).collect(Collectors.joining(", ", "[", "]"));
			var msg = String.format("Could not encode text with %s", csListStr);
			throw new BmsCompatException(msg);
		}

		// エンコードされたテキストをストリームへ出力する
		dst.write(outBytes);
	}

	/**
	 * チャンネルの配列データを文字列に変換する(Base16/36両対応)
	 * @param content 配列データが格納されたBMSコンテンツ
	 * @param channel 配列データが格納されたチャンネル番号
	 * @param index チャンネルのインデックス
	 * @param measure 配列データを取り出す小節番号
	 * @param radix 基数(16/36)
	 * @return 文字列に変換された配列データ
	 * @exception BmsCompatException 分解能不足によるノートの上書き発生
	 */
	private String notesToString(BmsContent content, int channel, int index, int measure, int radix)
			throws BmsCompatException {
		// 使用する配列データ変換関数を準備しておく
		IntFunction<String> converter;
		if (radix == 36) {
			converter = BmsInt::to36s;
		} else if (radix == 16) {
			converter = BmsInt::to16s;
		} else {
			converter = null;
		}

		// 当該小節・チャンネル・インデックスの全ノートを抽出する
		var notes = content.listNotes(channel, channel + 1, measure, 0, measure + 1, 0, n -> n.getIndex() == index);
		var noteCount = notes.size();
		if (noteCount == 0) {
			// ノートが存在しない場合は空小節を出力する
			return "00";
		} else if ((noteCount == 1) && (notes.get(0).getTick() == 0.0)) {
			// ノートが1件で刻み位置が0の場合、1要素分の配列データを出力する
			return converter.apply(notes.get(0).getValue());
		} else {
			// Do nothing
		}

		// 小数部を持つ刻み位置の有無によって処理内容を選択する
		var result = "";
		var tickCount = content.getMeasureTickCount(measure);
		var isTickCountDec = Utility.hasDecimal(tickCount);
		var decNotes = notes.stream().filter(n -> Utility.hasDecimal(n.getTick())).collect(Collectors.toList());
		if (decNotes.isEmpty() && !isTickCountDec) {
			// 整数の刻み位置のみの場合、刻み位置の最大公約数によって配列データの長さを決定する
			var division = tickCount;
			for (var note : notes) {
				division = Utility.gcd((int)division, (int)note.getTick());
			}

			// 刻み間隔単位で配列データを出力していく
			var sb = new StringBuilder((int)tickCount / (int)division * 2);
			var nextTick = 0;
			for (var t = 0; t < tickCount; t += division) {
				if (nextTick >= notes.size()) {
					// 全ノート出力済み
					sb.append("00");
				} else {
					var note = notes.get(nextTick);
					if (t == note.getTick()) {
						// 該当する刻み位置のデータを出力する
						sb.append(converter.apply(note.getValue()));
						nextTick++;
					} else {
						// この位置にはノートが存在しない
						sb.append("00");
					}
				}
			}
			result = sb.toString();
		} else {
			// 小数部を持つ刻み位置が1件でも存在する場合、配列データの最大分解能を上限にした長いデータが必要になる
			// 小数部を持つ刻み位置が全て整数になる拡張倍率を算出する
			final var delta = 0.00000001;
			var arraySize = 0;
			var numDecNotes = decNotes.size();
			var expand = 1.0;
			for (; expand <= FIND_INT_MAX; expand++) {
				// 刻み位置の拡張を試行し、全ての刻み位置で整数になることを確認する
				var hasDecimal = Utility.hasDecimal(tickCount * expand, delta);
				for (var i = 0; (i < numDecNotes) && !hasDecimal; i++) {
					hasDecimal = Utility.hasDecimal(decNotes.get(i).getTick() * expand, delta);
				}
				// 小数部なしになる倍率が見つかったら、その時点で処理を停止する(倍率確定)
				if (!hasDecimal) {
					arraySize = (int)Math.round(tickCount * expand);
					break;
				}
			}
			if (arraySize == 0) {
				// どの拡張倍率でも整数に変換することができなかった場合は最大分解能の配列サイズにする
				// この値だと、分解能の値によってはデータ上書きエラーが発生する危険性がある(仕方ないけど)
				arraySize = mMaxPrecision;
			} else {
				// 整数にした全ての刻み位置の最大公約数を算出し、配列サイズの圧縮を試みる
				// 圧縮した結果それでも最大分解能を超えるようなら、配列サイズを最大分解能範囲に収める
				var gcd = (long)Math.round(tickCount * expand);
				for (var note : notes) {
					var expandedTick = Math.round(note.getTick() * expand);
					gcd = Utility.gcd(gcd, expandedTick);
				}
				arraySize = Math.min((int)(arraySize / gcd), mMaxPrecision);
			}

			// 確定した配列サイズでノートの値を設定する
			var maxPos = arraySize - 1;
			var arrayScale = arraySize / tickCount;
			var noteIndex = 0;
			var pos = Math.min(maxPos, (int)Math.round(notes.get(0).getTick() * arrayScale));
			var sb = new StringBuilder(arraySize * 2);
			for (var i = 0; i < arraySize; i++) {
				if (pos == i) {
					// ノートが該当する場所にデータを出力する
					sb.append(converter.apply(notes.get(noteIndex).getValue()));
					if ((++noteIndex) < noteCount) {
						pos = Math.min(maxPos, (int)Math.round(notes.get(noteIndex).getTick() * arrayScale));
						if (pos == i) {
							// 次のノートの出力位置が同じ位置になった場合、上書きでデータ欠落になってしまう
							// よって指定分解能での出力はデータの互換性を保てないのでエラーとして処理する
							var def = String.format("#%03d%s:", measure, BmsInt.to36s(channel));
							var msg = String.format("%s Detected overwrite by due to lack of precision", def);
							throw new BmsCompatException(msg);
						}
					}
				} else {
					// この位置にはノートが存在しない
					sb.append("00");
				}
			}
			result = sb.toString();
		}

		return result;
	}

	/**
	 * BMS上の値を文字列表現に変換する。
	 * @param value 変換対象の値
	 * @param type 変換対象の値のデータ型
	 * @return 文字列表現に変換された値
	 */
	private static String bmsValueToString(Object value, BmsType type) {
		if (type.equals(BmsType.INTEGER) || type.equals(BmsType.STRING) || type.isArrayType()) {
			return value.toString();
		} else if (type.equals(BmsType.NUMERIC)) {
			var num = (double)value;
			return Utility.hasDecimal(num) ? BigDecimal.valueOf(num).toPlainString() : String.format("%.0f", num);
		} else if (type.equals(BmsType.BASE16)) {
			return BmsInt.to16s((int)(long)value);
		} else if (type.equals(BmsType.BASE36)) {
			return BmsInt.to36s((int)(long)value);
		} else {
			return value.toString();
		}
	}

	/**
	 * エラーチェック、正規化済みのコメントリスト生成
	 * @param commentList 処理前のコメントリスト
	 * @return エラーチェック、正規化済みのコメントリスト
	 * @exception NullPointerException commentList内にnullがある
	 */
	private static List<String> comments(Collection<String> commentList) {
		return commentList.stream()
				.peek(c -> assertArgNotNull(c, "comments(?)"))
				.map(BmsStandardSaver::comment)
				.collect(Collectors.toList());
	}

	/**
	 * コメント文字列の正規化
	 * <p>厳密には、改行コード、複数行コメント開始文字を空白に変換し、末尾の空白文字を消去する。</p>
	 * @param orgComment 正規化前のコメント文字列
	 * @return 正規化後のコメント
	 */
	private static String comment(String orgComment) {
		return orgComment
				.replace('\r', ' ')
				.replace('\n', ' ')
				.replace("/*", " ")
				.stripTrailing();
	}

	/**
	 * テキストのエンコード処理
	 * @param text エンコード対象テキスト
	 * @param cs エンコード文字セット
	 * @return エンコード後バイト配列。エンコードエラー発生時はnull。
	 */
	private static byte[] encodeText(String text, Charset cs) {
		// エンコード用バッファとエンコーダを準備する
		final var INITIAL_CAPACITY_BYTES = 256 * 1024;
		final var TEMP_BUFFER_BYTES = 8 * 1024;
		var in = CharBuffer.wrap(text);
		var out = new ByteArrayOutputStream(INITIAL_CAPACITY_BYTES);
		var temp = ByteBuffer.allocate(TEMP_BUFFER_BYTES);
		var encoder = cs.newEncoder()
				.onMalformedInput(CodingErrorAction.REPORT)
				.onUnmappableCharacter(CodingErrorAction.REPORT);

		// テキストのエンコード処理
		var processing = true;
		while (processing) {
			temp.position(0);
			var result = encoder.encode(in, temp, true);
			if (result.isError()) {
				// 1文字でもエンコードに失敗した場合は続行せずエンコード失敗とする
				return null;
			} else {
				// エンコードした分を出力バッファに転送する
				out.write(temp.array(), 0, temp.position());
				processing = !result.isUnderflow();
			}
		}

		// 最後までエンコードの完了したバイトデータを返す
		return out.toByteArray();
	}

	/**
	 * コメント1行を出力
	 * @param pw 出力先のWriter
	 * @param comment コメント文
	 */
	private void writeComment(PrintWriter pw, String comment) {
		var forJudge = comment.stripLeading();
		if (forJudge.isEmpty()) {
			// コメントが空文字列(空白文字のみで構成されている場合も含む)の場合は空行を出力する
			pw.println();
		} else if (forJudge.startsWith(";") || forJudge.startsWith("*") || forJudge.startsWith("//")) {
			// 先頭文字がコメント開始文字の場合はコメントをそのまま出力する
			pw.println(comment);
		} else {
			// 先頭文字がコメント開始文字ではない場合はコメント開始文字を付加して出力する
			pw.println(String.format("; %s", comment));
		}
	}

	/**
	 * BMSコンテンツの標準フォーマット互換性チェック
	 * @param content BMSコンテンツ
	 * @throws BmsCompatException 標準フォーマットとの互換性がない
	 */
	private static void checkContentCompatible(BmsContent content) throws BmsCompatException {
		checkMeasureCountCompatible(content);
		checkIndexedMetaCompatible(content);
		checkNoteValueCompatible(content);
	}

	/**
	 * 最大小節数の互換性チェック
	 * @param content BMSコンテンツ
	 * @throws BmsCompatException 小節数が標準フォーマットで表現可能な範囲を超過している
	 */
	private static void checkMeasureCountCompatible(BmsContent content) throws BmsCompatException {
		// 小節数が上限を超過しているかをチェックする
		var measureCount = content.getMeasureCount();
		if (measureCount > MEASURE_COUNT_MAX) {
			var msg = String.format("Max measure count is %d, but %d", MEASURE_COUNT_MAX, measureCount);
			throw new BmsCompatException(msg);
		}
	}

	/**
	 * 索引付きメタ情報の互換性チェック
	 * @param content BMSコンテンツ
	 * @throws BmsCompatException 索引付きメタ情報のインデックス値に標準フォーマットで表現可能な範囲を超過しているものがある
	 */
	private static void checkIndexedMetaCompatible(BmsContent content) throws BmsCompatException {
		// 索引付きメタ情報のインデックス値が上限を超過しているかをチェックする
		var indexedMetas = content.getSpec().getMetas().stream()
				.filter(m -> m.getUnit() == BmsUnit.INDEXED)
				.map(m -> m.getName())
				.collect(Collectors.toList());
		for (var metaName : indexedMetas) {
			// 索引付きメタ情報が1件でも存在すれば、インデックス値の最大値をチェックする
			if (content.getIndexedMetaCount(metaName) > 0) {
				var max = content.getIndexedMetas(metaName).keySet().stream().max(Comparator.naturalOrder()).orElse(0);
				if (max > META_INDEX_MAX) {
					var msg = String.format("In '%s', incompatible index '%d'", metaName.toUpperCase(), max);
					throw new BmsCompatException(msg);
				}
			}
		}
	}

	/**
	 * ノートの値の互換性チェック
	 * @param content BMSコンテンツ
	 * @throws BmsCompatException ノートの値に標準フォーマットで表現可能な範囲を超過しているものがある
	 */
	private static void checkNoteValueCompatible(BmsContent content) throws BmsCompatException {
		// 仕様チャンネルの配列データの値に上限を超過しているものがあるかをチェックする
		var measureCount = content.getMeasureCount();
		var arrayChannels = content.getSpec().getChannels().stream()
				.filter(c -> c.isArrayType())
				.collect(Collectors.toMap(c -> BmsInt.box(c.getNumber()), c -> c));
		for (var measure = 0; measure < measureCount; measure++) {
			for (var e : arrayChannels.entrySet()) {
				var number = e.getKey().intValue();
				if (content.getChannelDataCount(number, measure) > 0) {
					// ノートが存在する小節・チャンネルの全ノートの値が上限を超過していないかチェックする
					var cFrom = number;
					var cTo = number + 1;
					var mFrom = measure;
					var mTo = measure + 1;
					var t = (e.getValue().getType() == BmsType.ARRAY36) ? TESTER_OVERFLOW_A36 : TESTER_OVERFLOW_A16;
					var count = content.countNotes(cFrom, cTo, mFrom, 0.0, mTo, 0.0, t);
					if (count > 0) {
						var msg = String.format("In '#%03d%s', incompatible note value", measure, BmsInt.to36s(number));
						throw new BmsCompatException(msg);
					}
				}
			}
		}
	}
}
