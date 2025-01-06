package com.lmt.lib.bms.bemusic;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.spec.SecretKeySpec;

import com.lmt.lib.bms.BmsContent;
import com.lmt.lib.bms.BmsException;
import com.lmt.lib.bms.BmsStandardLoader;

/**
 * Delta Systemのテストデータを格納するパッケージファイルを制御するクラスです。
 *
 * <p>当クラスはDelta Systemの処理内容の妥当性を検証する目的のみに使用されるBMSコンテンツをパッケージングし、
 * Delta Systemのテストコードにテストデータを提供します。</p>
 *
 * <p>当クラスがサポートするパッケージ内に格納されたBMSコンテンツは上記以外の目的で使用することを許可しておらず、
 * 各BMSコンテンツは暗号化されています。従ってDelta Systemの検証目的以外でパッケージからBMSコンテンツを読み出し、
 * 使用・解析する行為は如何なる場合でも禁止とし、格納されているBMSコンテンツの内容についてのお問い合わせには
 * 一切回答しないものとします。</p>
 *
 * <p>※オープンソースの性質上、データフォーマットは容易に特定できますが目的外でのパッケージの使用はご遠慮ください</p>
 */
public class DeltaSystemTestPackage implements Closeable {
	/** 暗号化アルゴリズム */
	private static final String ALGORITHM = "AES";
	/** インデックスファイル用キー */
	private static final String K = "mN8$z#bD2@tK%1L&G7^p*Xj6!Fq4+W2ZR3%n#Y5B!H2$u8J@k4&L*6mT@9P^7C1xW8$y%2N^b5*3F@RkQ1*X^z@7$K4&B8pMd6!F@2R^n8%3L*jPT5#Y&7^H@2m3!W4b";

	/** パッケージファイルパス */
	private Path mPackagePath;
	/** テストデータパッケージ */
	private ZipFile mPackage;
	/** テストデータIDをファイル名に変換するマップ */
	private Map<String, String> mIdMap = new LinkedHashMap<>();

	/** テストデータ */
	public class Data {
		/** テストデータID */
		public String id;
		/** テストデータファイル名 */
		public String fileName;
		/** テストデータ */
		public BmsContent content;
		/** テストデータのヘッダ情報 */
		public BeMusicHeader header;
		/** テストデータの譜面 */
		public BeMusicChart chart;
	}

	/**
	 * Delta Systemテスト用パッケージを開きます。
	 * @param packagePath パッケージファイルパス
	 * @throws IOException パッケージファイルのオープン失敗(フォーマット異常等含む)
	 */
	public DeltaSystemTestPackage(Path packagePath) throws IOException {
		mPackagePath = packagePath;
		mPackage = new ZipFile(packagePath.toFile());

		var indexData = decodeBytes(mPackage.getInputStream(mPackage.getEntry("index")), indexKey());
		try (var indexStream = new ByteArrayInputStream(indexData);
				var reader = new BufferedReader(new InputStreamReader(indexStream))) {
			reader.lines().map(l -> l.split(",")).forEach(c -> mIdMap.put(c[0], c[1]));
		}
	}

	/**
	 * テストデータを読み込みます。
	 * @param id テストデータID
	 * @return テストデータ
	 * @throws IOException データ読み込みエラー
	 * @throws BmsException BMSライブラリエラー
	 */
	public Data load(String id) throws IOException, BmsException {
		return load(id, false);
	}

	/**
	 * テストデータを読み込みます。
	 * @param id テストデータID
	 * @param includeOutOfScope Delta Systemと関連しないチャンネルを譜面データに含めるかどうか
	 * @return テストデータ
	 * @throws IOException データ読み込みエラー
	 * @throws BmsException BMSライブラリエラー
	 */
	public Data load(String id, boolean includeOutOfScope) throws IOException, BmsException {
		// IDから対応するファイル名へ変換する
		var fileName = mIdMap.get(id);
		if (fileName == null) {
			var msg = String.format("%s: In this package, there is no BMS with '%s'", mPackagePath, id);
			throw new IOException(msg);
		}

		// BMSコンテンツのエントリ情報を取得する
		var bmsEntry = mPackage.getEntry(fileName);
		if (bmsEntry == null) {
			var msg = String.format("%s: BROKEN!! In this package, there is no file with '%s'", mPackagePath, fileName);
			throw new IOException(msg);
		}

		// 平文のBMSコンテンツを読み込む
		var bmsBytes = decodeBytes(mPackage.getInputStream(bmsEntry), contentKey(id));

		// テストデータを生成する
		var data = new Data();
		data.id = id;
		data.fileName = fileName;
		data.content = new BmsStandardLoader()
				.setSpec(BeMusicSpec.LATEST)
				.setHandler(BeMusicLoadHandler.withControlFlow(1L))
				.setSyntaxErrorEnable(false)
				.setFixSpecViolation(true)
				.setAllowRedefine(true)
				.setIgnoreUnknownMeta(true)
				.setIgnoreUnknownChannel(true)
				.setIgnoreWrongData(true)
				.setCharsets(StandardCharsets.UTF_8, Charset.forName("MS932"))
				.load(bmsBytes);
		data.header = BeMusicHeader.of(data.content);
		data.chart = new BeMusicChartBuilder(data.content)
				.setSeekBga(includeOutOfScope)
				.setSeekBgm(includeOutOfScope)
				.setSeekInvisible(includeOutOfScope)
				.setSeekMine(true)
				.setSeekMeasureLine(true)
				.setSeekText(includeOutOfScope)
				.setSeekVisible(true)
				.createChart();

		return data;
	}

	@Override
	public void close() throws IOException {
		mIdMap.clear();
		mPackage.close();
	}

	/**
	 * インデックスファイル用キー生成
	 * @return インデックスファイル用キー
	 */
	private static SecretKeySpec indexKey() {
		return new SecretKeySpec(new String(new char[] {
				K.charAt(0x6b), K.charAt(0x13), K.charAt(0x2f), K.charAt(0x7a),
				K.charAt(0x38), K.charAt(0x4c), K.charAt(0x03), K.charAt(0x0f),
				K.charAt(0x77), K.charAt(0x61), K.charAt(0x25), K.charAt(0x37),
				K.charAt(0x21), K.charAt(0x05), K.charAt(0x4e), K.charAt(0x1d)
		}).getBytes(), ALGORITHM);
	}

	/**
	 * BMSコンテンツ用キー生成
	 * @param id テストデータID
	 * @return BMSコンテンツ用キー
	 */
	private static SecretKeySpec contentKey(String id) {
		var key = id + " ".repeat(16 - id.length());
		return new SecretKeySpec(key.getBytes(), ALGORITHM);
	}


	/**
	 * 暗号機能生成
	 * @param opmode モード
	 * @param key キー
	 * @return 暗号機能オブジェクト
	 * @throws IOException 暗号機能オブジェクト生成失敗
	 */
	private static Cipher cipher(int opmode, SecretKeySpec key) throws IOException {
		try {
			var cipher = Cipher.getInstance(ALGORITHM);
			cipher.init(opmode, key);
			return cipher;
		} catch (Exception e) {
			throw new IOException(e);
		}
	}

	/**
	 * データ復号化＆解凍
	 * @param data 対象入力ストリーム
	 * @param key キー
	 * @return 解凍後データ
	 * @throws IOException 解凍失敗
	 */
	private static byte[] decodeBytes(InputStream data, SecretKeySpec key) throws IOException {
		try (var decryptStream = new CipherInputStream(data, cipher(Cipher.DECRYPT_MODE, key));
				var decompressStream = new InflaterInputStream(decryptStream)) {
			return decompressStream.readAllBytes();
		}
	}

	/**
	 * データ圧縮＆暗号化
	 * @param data 対象データ
	 * @param key キー
	 * @return 圧縮後データ
	 * @throws IOException 圧縮失敗
	 */
	private static byte[] encodeBytes(byte[] data, SecretKeySpec key) throws IOException {
		// データを圧縮する
		var compressedStream = new ByteArrayOutputStream(data.length / 2);
		var deflater = new Deflater(Deflater.BEST_COMPRESSION);
		var deflateStream = new DeflaterOutputStream(compressedStream, deflater);
		deflateStream.write(data);
		deflateStream.close();

		// 圧縮データを暗号化する
		try {
			var compressedBytes = compressedStream.toByteArray();
			var encrypter = cipher(Cipher.ENCRYPT_MODE, key);
			var encryptedBytes = encrypter.doFinal(compressedBytes);
			return encryptedBytes;
		} catch (BadPaddingException | IllegalBlockSizeException e) {
			throw new IOException(e);
		}
	}

	/**
	 * Delta System検証用BMSコンテンツパッケージを生成します。
	 * @param args プログラムの引数<br>
	 * [0] BMSコンテンツが格納されたフォルダのパス<br>
	 * [1] Delta System検証用BMSコンテンツパッケージのファイルパス
	 */
	public static void main(String[] args) {
		// 入出力パスを展開する
		var inPath = (Path)null;
		var outPath = (Path)null;
		if (args.length != 2) {
			System.out.println("===== USAGE =====");
			System.out.println("<arg1> Path of folder containing BMS files.");
			System.out.println("<arg2> Path of package file.");
			System.out.println();
			System.out.println("NOTICE:");
			System.out.println("- Only files with the extension 'bms' are scanned.");
			System.out.println("- The test data ID must be written in the #COMMENT header.");
			System.out.println("- Processing will stop if any errors are detected.");
			System.exit(-1);
		}
		try {
			// プログラム引数からパスを取り出す
			inPath = Path.of(args[0]);
			outPath = Path.of(args[1]);
		} catch (InvalidPathException e) {
			// パスとして認識できない文字列を入れるとここに入る
			System.out.println("*** BAD PATH");
			e.printStackTrace();
			System.exit(-1);
		}

		// 指定フォルダからBMSファイルを収集する
		var bmsFilePaths = (List<Path>)null;
		try {
			bmsFilePaths = Files.walk(inPath)
					.filter(Files::isRegularFile)
					.filter(p -> p.getFileName().toString().endsWith(".bms"))
					.collect(Collectors.toList());
		} catch (IOException e) {
			System.out.println("*** FAILED TO SCAN BMS FILES");
			e.printStackTrace();
			System.exit(-1);
		}

		// BMSファイルが1件もない場合はパッケージ作成を中止する
		if (bmsFilePaths.isEmpty()) {
			System.out.printf("%s: In this folder, there is no BMS files.\n", inPath);
			System.exit(-1);
		}

		// Delta System検証用BMSコンテンツパッケージを作成していく
		var fileCount = bmsFilePaths.size();
		System.out.printf("Found %d BMS files\n", fileCount);
		System.out.println("====================");
		try (var fos = new FileOutputStream(outPath.toFile());
				var zos = new ZipOutputStream(fos)) {
			// 初期設定
			zos.setLevel(Deflater.NO_COMPRESSION);

			// スキャンした全てのBMSファイルを処理対象とする
			var idMap = new TreeMap<String, String>();
			for (var i = 0; i < fileCount; i++) {
				var bmsFilePath = bmsFilePaths.get(i);
				var bmsFileRel = inPath.relativize(bmsFilePath);
				var fileName = String.format("%03d", i + 1);
				System.out.printf("%s: %s ... ", bmsFileRel, fileName);

				// BMSコンテンツを読み込む
				var content = (BmsContent)null;
				try {
					content = BeMusic.loadContentFrom(bmsFilePath, 1L, false);
				} catch (IOException | BmsException e) {
					System.out.println("ERROR");
					e.printStackTrace();
					System.exit(-1);
				}

				// BMSコンテンツのIDを特定する
				var id = BeMusicMeta.getComment(content);
				if (id.isEmpty()) {
					// IDが未定義: テストデータを識別できなくなるためエラーとする
					System.out.println("NO ID");
					System.exit(-1);
				} else if (id.length() > 16) {
					// IDが長すぎる: キーを128ビットに設定しているため16文字を超過するIDはエラーとする
					System.out.println("TOO LONG ID");
					System.exit(-1);
				} else if (idMap.containsKey(id)) {
					// IDが競合する: テストデータを一意にすることができないためエラーとする
					System.out.println("CONFLICT ID");
					System.exit(-1);
				}

				// BMSコンテンツのエントリを追加する
				try {
					// 圧縮したBMSコンテンツを暗号化してBMSコンテンツを追加する
					var entry = new ZipEntry(fileName);
					zos.putNextEntry(entry);
					zos.write(encodeBytes(Files.readAllBytes(bmsFilePath), contentKey(id)));
					zos.closeEntry();

					// 追加したIDとファイル名の関係を記憶しておく
					idMap.put(id, fileName);
					System.out.println("DONE");
				} catch (IOException e) {
					// BMSコンテンツの追加に失敗
					System.out.println("FAILED");
					e.printStackTrace();
					System.exit(-1);
				}
			}

			// IDとファイル名を関連付けたインデックスファイルを生成する
			System.out.print("Creating index file ... ");
			try {
				// インデックスデータを生成する
				var indexText = idMap.entrySet().stream()
						.map(e -> String.format("%s,%s", e.getKey(), e.getValue()))
						.collect(Collectors.joining("\n"));
				var indexBytes = indexText.getBytes();
				var encodedBytes = encodeBytes(indexBytes, indexKey());

				// インデックスファイルを追加する
				var entry = new ZipEntry("index");
				zos.putNextEntry(entry);
				zos.write(encodedBytes);
				zos.closeEntry();
				System.out.println("DONE");
			} catch (IOException e) {
				// インデックスファイル生成失敗
				System.out.println("FAILED");
				e.printStackTrace();
				System.exit(-1);
			}
		} catch (IOException e) {
			// パッケージ作成失敗
			System.out.println("*** FAILED TO CREATE PACKAGE");
			e.printStackTrace();
			System.exit(-1);
		}

		// 正常終了
		System.out.println("====================");
		System.out.println("Completed");
		System.exit(0);
	}
}
