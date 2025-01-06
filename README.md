# BMS Library
このライブラリはBMS(Be-Music Source File)フォーマットで記述された楽曲データの制御を行います。楽曲データの読み込み・書き出し・分析・再生制御など多岐に渡るデータ操作を提供することで、Be-Musicに関連する様々なアプリケーション開発のあらゆるコストを大幅に削減することができます。また、ライブラリが提供する各種機能にはあらゆる形式での拡張性を持たせており、新しい音楽シミュレーションアプリケーションの開発にも利用することが可能となっています。

## 事前準備
ライブラリを使用するにあたり、以下のソフトウェアが必要となります。

### Java Development Kit (JDK)
BMS LibraryはJavaで記述されていますのでビルドにはJDKが必要になります。ライブラリ本体はJava11で記述されていますが、チュートリアル等をビルドするためにJava17以降を使用することを推奨します。JDKは[こちら(例)](https://www.oracle.com/jp/java/technologies/downloads/)から入手してください。

### Apache Maven (ビルドツール)
BMS LibraryのビルドにはMavenを使用します。Mavenは[こちら](https://maven.apache.org/download.cgi)から入手できます。尚、インストール・設定の手順についてはインターネット等で検索してください。

## ビルド方法
ビルドはコマンドプロンプト等から行います。<br>
このドキュメントが格納されたディレクトリから以下のコマンドを実行し、Mavenのローカルリポジトリにライブラリをインストールしてください。ビルドが成功すると他プロジェクトからライブラリを参照できるようになります。

```
mvn clean install
```

## 使用方法
### 他のMavenプロジェクトから使用する
ライブラリを他のMavenプロジェクトから使用したい場合は、当該プロジェクトのpom.xmlの&lt;dependencies&gt;に以下を追加してください。

```
<dependency>
    <groupId>com.lmt</groupId>
    <artifactId>bms-library</artifactId>
    <version>0.9.0</version>
</dependency>
```

### ライブラリのリファレンス(Javadoc)
最新版のリファレンスは以下を参照してください。<br>
[https://www.lm-t.com/content/bmslibrary/doc/latest/index.html](https://www.lm-t.com/content/bmslibrary/doc/latest/index.html)

過去バージョンのリファレンスが必要な場合は以下から入手可能です。<br>
[https://www.lm-t.com/content/bmslibrary/](https://www.lm-t.com/content/bmslibrary/)

## チュートリアル
|チュートリアル内容       |リンク|
|-------------------------|------|
|BMSファイル読み込み      |[https://github.com/j-son3/bms-library/tree/main/tutorial/read](https://github.com/j-son3/bms-library/tree/main/tutorial/read)|
|BMSファイル編集・書き出し|[https://github.com/j-son3/bms-library/tree/main/tutorial/write](https://github.com/j-son3/bms-library/tree/main/tutorial/write)|
|譜面統計情報の集計       |[https://github.com/j-son3/bms-library/tree/main/tutorial/stat](https://github.com/j-son3/bms-library/tree/main/tutorial/stat)|
|簡易楽曲ビューア         |[https://github.com/j-son3/bms-library/tree/main/tutorial/view](https://github.com/j-son3/bms-library/tree/main/tutorial/view)|

## 変更履歴
[CHANGELOG.md](https://github.com/j-son3/bms-library/blob/main/CHANGELOG.md)を参照してください。

## 当ライブラリの扱いについて
### 開発状況
当ライブラリは現時点では未実装の機能もあり開発中となっています。また、実装済みの機能においても実際のユースケースでは利用契機がなかったり、機能が不十分であったりする場合もあります。そのため、当面の間はバージョン間で使用方法の互換性が損なわれる破壊的変更(※)がしばしば発生することを想定しています。この状況はメジャーバージョンが0の間は継続するものとしますので、当ライブラリを利用するにあたってはその点に留意してください。

※以下のいずれか1つ以上の性質を持つ変更を指します
- 既存機能、定数値が削除される
- 既存機能のAPIインターフェイスが変更される
- 既存機能の挙動が変更される
- 既存の定数値が変更される

### テストコード
当ライブラリのテストデータには「sp.bin」「dp.bin」が含まれています。これらのファイルにはテスト用のBMSファイルが格納されていますが、当ライブラリのテスト以外の用途では使用しないでください。