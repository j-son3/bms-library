# BMS Library's tutorial - Stat

このチュートリアルでは、譜面の統計情報の集計方法について解説します。譜面の統計機能を使用することで、譜面のより詳細な分析情報を得ることができます。分析系アプリケーションで必要になるBMS Libraryの操作について学ぶことができます。

## 事前準備

事前にBMS Libraryのビルドを完了させておいてください。

## ビルド方法

このドキュメントがあるディレクトリで以下のコマンドを実行してください。

```
mvn clean package
```

## 実行方法

チュートリアルのビルド完了後、以下のコマンドで実行できます。<br>
※以下のコマンドはWindowsのコマンドプロンプトから実行することを想定して記載しています<br>
※2～3番目のパラメータ(配置)は省略でき、省略すると正規譜面が分析対象になります

```
java -cp target\jars\* com.lmt.app.blt.Stat <分析対象BMSファイルパス> <配置(SP/DP左)> <配置(DP右)>
```
