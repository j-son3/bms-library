package com.lmt.lib.bms;

import static com.lmt.lib.bms.BmsAssertion.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * BMSコンテンツを外部データへ出力するセーバーです。
 *
 * <p>当クラスではBMSコンテンツの内容を外部データとして出力する機能を提供します。ここで言う「外部データ」とは、
 * BMSで記述されたテキストデータのことを指します。外部データはファイル等の何らかの形式で記録されていることを
 * 想定しており、Java言語の{@link java.io.InputStream}で読み取ることが出来るもの全てを指します。</p>
 *
 * <p><b>出力されるデータ</b>
 * 外部データとして出力されるデータには、以下のものが存在します。</p>
 *
 * <ul>
 * <li>BMS宣言。出力対象のBMSコンテンツにBMS宣言が1件でも存在する場合、外部データの1行目にBMS宣言の一覧を出力します。B</li>
 * <li>メタ情報。BMSコンテンツに設定された全てのメタ情報を出力します。メタ情報の値が初期値と同じ場合でも、明示的に
 * 設定されたメタ情報は外部データに出力されます。メタ情報の出力順は{@link BmsMeta#getOrder}の値に準拠します。</li>
 * <li>チャンネル。BMSコンテンツに記録されたチャンネルのうち、チャンネル番号が{@link BmsSpec#SPEC_CHANNEL_MIN}～{@link BmsSpec#SPEC_CHANNEL_MAX}
 * のチャンネルを外部データとして出力します。ユーザーチャンネルのデータは出力されません。チャンネルデータの並び順は
 * 小節番号、チャンネル番号で昇順になります。</li>
 * </ul>
 *
 * <p>また、外部データにはコメントを出力することができます。コメントは以下のメソッドで登録出来ます。<br>
 * メタ情報の先頭：{@link #setMetasComment(String...)}<br>
 * チャンネルの先頭：{@link #setChannelsComment(String...)}<br>
 * 外部データの行末：{@link #setFooterComment(String...)}<br>
 * </p>
 *
 * <p><b>文字コードについて</b><br>
 * 当クラスで出力する外部データの文字コードは、標準動作では{@link BmsSpec#getStandardCharset()}の返す文字セットに従います。
 * この動作を変更したい場合は{@link BmsSpec#setStandardCharset(Charset)}で標準の文字セットを明示的に指定するか、BMSコンテンツの
 * BMS宣言に、使用する文字セットを記述します。例えば、以下のようにBMS宣言を登録すると外部データはUTF-8で出力されます。<br>
 * <br>
 * bmsContent.{@link BmsContent#addDeclaration(String, String) addDeclaration}(&quot;encoding&quot;, &quot;UTF-8&quot;);<br>
 * <br>
 * このBMS宣言を追加したBMSコンテンツは、BMSライブラリで外部データとして出力する限りにおいてはUTF-8でエンコーディング
 * されて出力されます。</p>
 *
 * <p><b>UTF-8で出力する場合のBOM(Byte Order Mark)について</b><br>
 * 外部データをUTF-8でエンコーディングする場合において、BOMは付与されません。</p>
 */
public final class BmsSaver {
	/** メタ情報のコメント */
	private ArrayList<String> mMetasComment = new ArrayList<>();
	/** チャンネルのコメント */
	private ArrayList<String> mChannelsComment = new ArrayList<>();
	/** フッターコメント */
	private ArrayList<String> mFooterComment = new ArrayList<>();

	/**
	 * BMSセーバのインスタンスを構築します。
	 */
	public BmsSaver() {
	}

	/**
	 * メタ情報コメントを設定します。
	 * <p>コメントの内容は1行につき1文字列、複数行記述する場合は複数の文字列をCollectionに設定して渡してください。</p>
	 * <p>文字列中の改行コード&lt;CR&gt;、&lt;LF&gt;は消去したうえで出力されます。</p>
	 * @param metasComment メタ情報コメント
	 * @return BMSセーバのインスタンス
	 * @exception NullPointerException metasCommentがnull
	 */
	public BmsSaver setMetasComment(Collection<String> metasComment) {
		assertArgNotNull(metasComment, "metasComment");
		putComments(mMetasComment, metasComment);
		return this;
	}

	public BmsSaver setMetasComment(String ...metasComment) {
		return setMetasComment(Arrays.asList(metasComment));
	}

	/**
	 * チャンネルコメントを設定します。
	 * <p>コメントの内容は1行につき1文字列、複数行記述する場合は複数の文字列をCollectionに設定して渡してください。</p>
	 * <p>文字列中の改行コード&lt;CR&gt;、&lt;LF&gt;は消去したうえで出力されます。</p>
	 * @param channelsComment チャンネルコメント
	 * @return BMSセーバのインスタンス
	 * @exception NullPointerException channelsCommentがnull
	 */
	public BmsSaver setChannelsComment(Collection<String> channelsComment) {
		assertArgNotNull(channelsComment, "channelsComment");
		putComments(mChannelsComment, channelsComment);
		return this;
	}

	public BmsSaver setChannelsComment(String ...channelsComment) {
		return setChannelsComment(Arrays.asList(channelsComment));
	}

	/**
	 * フッターコメントを設定します。
	 * <p>コメントの内容は1行につき1文字列、複数行記述する場合は複数の文字列をCollectionに設定して渡してください。</p>
	 * <p>文字列中の改行コード&lt;CR&gt;、&lt;LF&gt;は消去したうえで出力されます。</p>
	 * @param footerComment フッターコメント
	 * @return BMSセーバのインスタンス
	 * @exception NullPointerException footerCommentがnull
	 */
	public BmsSaver setFooterComment(Collection<String> footerComment) {
		assertArgNotNull(footerComment, "footerComment");
		putComments(mFooterComment, footerComment);
		return this;
	}

	public BmsSaver setFooterComment(String ...footerComment) {
		return setFooterComment(Arrays.asList(footerComment));
	}

	/**
	 * BMSを出力します。
	 * <p>・dstにはBMSファイルのパスを指定します。<br>
	 * 上記以外は{@link #save(BmsContent, OutputStream)}と同じです。</p>
	 * @param content 出力対象のBMSコンテンツ
	 * @param dst 出力先パス
	 * @exception BmsException dstがnull: Cause NullPointerException
	 * @exception BmsException {@link #save(BmsContent, OutputStream)}を参照
	 */
	public void save(BmsContent content, String dst) throws BmsException {
		try {
			save(content, new FileOutputStream(dst));
		} catch (BmsException e) {
			throw e;
		} catch (Exception e) {
			throw new BmsException(e);
		}
	}

	/**
	 * BMSを出力します。
	 * <p>・dstにはBMSファイルのパスを指定します。<br>
	 * 上記以外は{@link #save(BmsContent, OutputStream)}と同じです。</p>
	 * @param content 出力対象のBMSコンテンツ
	 * @param dst 出力先パス
	 * @exception BmsException dstがnull: Cause NullPointerException
	 * @exception BmsException {@link #save(BmsContent, OutputStream)}を参照
	 */
	public void save(BmsContent content, Path dst) throws BmsException {
		try {
			save(content, new FileOutputStream(dst.toFile()));
		} catch (BmsException e) {
			throw e;
		} catch (Exception e) {
			throw new BmsException(e);
		}
	}

	/**
	 * BMSを出力します。
	 * <p>・dstにはBMSファイルのパスを指定します。<br>
	 * 上記以外は{@link #save(BmsContent, OutputStream)}と同じです。</p>
	 * @param content 出力対象のBMSコンテンツ
	 * @param dst 出力先パス
	 * @exception BmsException dstがnull: Cause NullPointerException
	 * @exception BmsException {@link #save(BmsContent, OutputStream)}を参照
	 */
	public void save(BmsContent content, File dst) throws BmsException {
		try (var fos = new FileOutputStream(dst)) {
			save(content, fos);
		} catch (BmsException e) {
			throw e;
		} catch (Exception e) {
			throw new BmsException(e);
		}
	}

	/**
	 * BMSを出力します。
	 * <p>dstにはクローズされていない出力可能なストリームを指定してください。出力中のエラーに関しては
	 * {@link java.io.OutputStream}の仕様に準拠します。</p>
	 * <p>指定BMSコンテンツに1件でもBMS宣言が存在する場合、BMSの先頭行には";?bms"が出力されます。</p>
	 * <p>BMS宣言の"encoding"要素は、出力するBMSの文字セットを表します。この要素の値には文字セットの正規名が
	 * 出力されます。一方、文字セットは別名でも指定可能であることから、必ずしもプログラム上で指定した文字セット名が
	 * そのまま出力されるとは限りません。</p>
	 * <p>BMS宣言にて"encoding"要素が指定されている場合、その要素の値に記述されている
	 * 文字セットを用いてBMSを出力しようとします。<br>
	 * 文字セットの名称が未知の場合、BmsExceptionをスローします。<br>
	 * BMSをUTF-8で出力しようとした場合のBOM(Byte Order Mark)は付加されません。<br>
	 * 文字セットを指定しない場合、{@link BmsSpec#setStandardCharset}で指定した文字セットが使用されます。
	 * 通常、BMSの標準文字セットはShift-JISです。</p>
	 * <p>BMSには、以下の順に各要素が出力されます。<br>
	 * 1.BMS宣言(存在する場合のみ)<br>
	 * 2.メタ情報コメント(指定した場合のみ)<br>
	 * 3.メタ情報<br>
	 * 4.チャンネルコメント(指定した場合のみ)<br>
	 * 5.チャンネルデータ<br>
	 * 6.フッターコメント(指定した場合のみ)</p>
	 * @param content 出力対象のBMSコンテンツ
	 * @param dst 出力ストリーム
	 * @exception BmsException contentがnull: Cause NullPointerException
	 * @exception BmsException dstがnull: Cause NullPointerException
	 * @exception BmsException BMS宣言で指定の文字セットが未知
	 * @exception BmsException BMS出力中にストリームが例外をスローした
	 */
	public void save(BmsContent content, OutputStream dst) throws BmsException {
		// アサーション
		try {
			assertArgNotNull(content, "content");
			assertArgNotNull(dst, "dst");
		} catch (Exception e) {
			throw new BmsException(e);
		}

		// 文字セットを決定する
		Charset charset;
		try {
			charset = content.getEncoding();
		} catch (IllegalStateException e) {
			throw new BmsException(e);
		}

		try {
			// 書き込み用のWriterを生成する
			var osw = new OutputStreamWriter(dst, charset);
			var bw = new BufferedWriter(osw);
			var writer = new PrintWriter(bw);
			var needEmptyLine = false;

			// BMS宣言を出力する
			var sb = new StringBuilder();
			var decls = content.getDeclarations();
			var outDecls = (decls.size() > 0);
			if (outDecls) {
				// BMS宣言が存在する場合のみ出力する
				sb.append(";?bms");
				decls.forEach((k, v) -> sb.append(String.format(" %s=\"%s\"", k, v)));
				writer.println(sb.toString());
				needEmptyLine = true;
			}

			// メタ情報を出力する
			var outMetaFirst = true;
			for (var metaSpec : content.getSpec().getMetas()) {
				var name = metaSpec.getName();
				var unit = metaSpec.getUnit();
				var type = metaSpec.getType();

				// メタ情報が存在する場合のみ処理する
				if (content.getMetaCount(name, unit) > 0) {
					// 最初の出力の場合の処理
					if (outMetaFirst) {
						outMetaFirst = false;

						// 空行を出力する
						if (needEmptyLine) {
							writer.println("");
						}

						// メタ情報コメントを出力する
						mMetasComment.forEach(c -> writer.println(String.format("; %s", c)));
					}

					// メタ情報を出力する
					var outName = name.toUpperCase();
					switch (unit) {
					case SINGLE:  // 単一データ
						writer.println(String.format("%s %s",
								outName, bmsValueToString(content.getSingleMeta(name), type)));
						break;
					case MULTIPLE:  // 複数データ
						content.getMultipleMetas(name).forEach(v -> {
							writer.println(String.format("%s %s", outName, bmsValueToString(v, type)));
						});
						break;
					case INDEXED:  // 索引付きデータ
						content.getIndexedMetas(name).forEach((i, v) -> {
							String index = intToBaseX(i, 36);
							String value = bmsValueToString(v, type);
							writer.println(String.format("%s%s %s", outName, index, value));
						});
						break;
					default:  // 不明
						// コーディングの便宜上defaultを記述しているが、ここを通ることは想定しない
						break;
					}

					// チャンネル出力時に空行が必要
					needEmptyLine = true;
				}
			}

			// チャンネルデータを出力する
			var outChannelFirst = true;
			var channelSpecs = content.getSpec().getChannels();
			var measureCount = content.getMeasureCount();
			for (var i = 0; i < measureCount; i++) {
				// BMS仕様に定義済みの全チャンネルを出力対象とする
				final var measure = i;
				for (var channelSpec : channelSpecs) {
					var channel = channelSpec.getNumber();
					var dataCount = content.getChannelDataCount(channel, measure);

					// チャンネルデータが未登録の場合は何もしない
					if (dataCount == 0) {
						continue;
					}

					// 最初の出力の場合の処理
					if (outChannelFirst) {
						outChannelFirst = false;

						// 空行を出力する
						if (needEmptyLine) {
							writer.println("");
						}

						// チャンネルコメントを出力する
						mChannelsComment.forEach(c -> writer.println(String.format("; %s", c)));
					}

					// チャンネルのデータ型ごとに処理を分岐
					var chType = channelSpec.getType();
					var channelStr = intToBaseX(channel, 36);
					for (var j = 0; j < dataCount; j++) {
						String value;
						if (chType.isValueType()) {
							value = bmsValueToString(content.getMeasureValue(channel, j, measure), chType);
						} else if (chType.isArrayType()) {
							value = notesToString(content, channel, j, measure, chType.getRadix());
						} else {
							value = "";
						}
						writer.println(String.format("#%03d%s:%s", measure, channelStr, value));
					}

					// フッターコメント出力時に空行が必要
					needEmptyLine = true;
				}
			}

			// フッターコメントを出力する
			if (needEmptyLine && (mFooterComment.size() > 0)) {
				writer.println("");
			}
			mFooterComment.forEach(c -> writer.println(String.format("; %s", c)));

			// 出力完了
			writer.close();
			if (writer.checkError()) {
				// エラーを検出した場合は入出力エラーとして扱う
				throw new IOException("Failed to save BMS.");
			}
		} catch (Exception e) {
			throw new BmsException(e);
		}
	}

	/**
	 * 指定コメント配列に修正コメントを追加する。
	 * <p>改行コードを消去してコメント配列に追加する。</p>
	 * @param outComments 登録先コメント配列
	 * @param inComments 登録元コメントコレクション
	 */
	private static void putComments(ArrayList<String> outComments, Collection<String> inComments) {
		outComments.clear();
		inComments.forEach(str -> outComments.add(str.replace("\r", "").replace("\n", "")));
	}

	/**
	 * チャンネルの配列データを文字列に変換する(Base16/36両対応)
	 * @param content 配列データが格納されたBMSコンテンツ
	 * @param channel 配列データが格納されたチャンネル番号
	 * @param index チャンネルのインデックス
	 * @param measure 配列データを取り出す小節番号
	 * @param radix 基数(16/36)
	 * @return 文字列に変換された配列データ
	 */
	private static String notesToString(BmsContent content, int channel, int index, int measure, int radix) {
		// 当該小節・チャンネル・インデックスの全Noteを抽出する
		var notes = content.listNotes(channel, channel + 1, measure, 0, measure + 1, 0, note -> true);
		if (notes.size() == 0) {
			// Noteが存在しない場合は空小節を出力する
			return "00";
		}

		// 出力する配列のインデックス刻み間隔を計算する
		// Noteが存在する刻み位置の最大公約数が刻み間隔となる
		var tickEnd = content.getMeasureTickCount(measure);
		var tick = tickEnd;
		// TODO 下記、代替として小数点対応のGCD計算処理の実装を行わない限りBmsSaverは動作しない
		//for (var note : notes) tick = gcd(tick, note.getTick());

		// 刻み間隔単位で配列データを出力していく
		var sb = new StringBuilder(tickEnd / tick * 2);
		var nextTick = 0;
		for (var t = 0; t < tickEnd; t += tick) {
			if (nextTick >= notes.size()) {
				// 全Note出力済み
				sb.append("00");
			} else {
				var note = notes.get(nextTick);
				if (t == note.getTick()) {
					// 該当する刻み位置のデータを出力する
					sb.append(intToBaseX(note.getValue(), radix));
					nextTick++;
				} else {
					// この位置にはNoteが存在しない
					sb.append("00");
				}
			}
		}

		return sb.toString();
	}

	/**
	 * 値A,Bの最大公約数を計算する
	 * @param a 値A
	 * @param b 値B
	 * @return 最大公約数
	 */
	private static int gcd(int a, int b) {
		return (b == 0) ? a : gcd(b, a % b);
	}

	/**
	 * 整数値を指定基数の文字列に変換する
	 * @param value 変換対象の値
	 * @param radix 基数(16/36)
	 * @return 変換後文字列
	 */
	private static String intToBaseX(int value, int radix) {
		return String.format("%s%s", ((value < radix) ? "0" : ""), Integer.toString(value, radix).toUpperCase());
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
			return BigDecimal.valueOf((double) value).toPlainString();
		} else if (type.equals(BmsType.BASE16) || type.equals(BmsType.BASE36)) {
			return intToBaseX((int)(long) value, type.getRadix());
		} else {
			return value.toString();
		}
	}
}
