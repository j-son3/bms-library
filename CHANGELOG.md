# Change Log
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/)
and this project adheres to [Semantic Versioning](http://semver.org/).

## [0.1.0] - 2022-08-05
### Added
- Java標準のストリームAPIでタイムライン上のノート・小節データ等を走査する BmsContent#timeline を追加しました。
- タイムラインAPI対応に伴い、BmsAt, BmsPoint, BmsNote の現状機能を維持しつつ仕様追加・変更を行いました。また、同API関連で BmsAddress, BmsChx, BmsElement クラスを新規追加しました。
- Java標準のストリームAPIで楽曲位置情報を走査する BeMusicScore#stream を追加しました。
- Be-Musicサブセットに、BMSファイルからBe-Music用BMSコンテンツを読み込む処理を簡略化できる BeMusicSpec#loadContentFrom を追加しました。
- Be-Musicサブセットに、ノート配置を仮想的に変更する機能を持つ BeMusicNoteLayout クラスを新規追加しました。これによりRANDOM/MIRROR等の実装が可能になりました。
- Be-Music用BMSコンテンツのヘッダ・譜面データから譜面統計情報を集計する機能を持つ BeMusicStatisticsBuilder, BeMusicStatistics, BeMusicTimeSpan クラスを新規追加しました。
- BMSコンテンツ読み込み時の、より具体的なエラー内容を BmsAbortException#getError で取得できるようにしました。

### Changed
- BMSコンテンツ読み込みハンドラ(BmsLoadHandler)の、BMSコンテンツ読み込み完了通知メソッドを finishLoad から testContent へ変更しました。
- BMSコンテンツ読み込みハンドラ(BmsLoadHandler)の、BMSコンテンツ読み込み開始通知メソッドで通知される情報を BmsSpec から BmsLoaderSettings へ変更しました。
- BmsLoader#setSyntaxErrorEnable で構文エラーを無効化すると、複数行コメントを閉じずにBMS読み込みが完了した場合と、BmsLoadHandler#testContent が検査失敗を返した場合もエラー通知しないように変更しました。
- BeMusicLoadHandler のメソッド setIgnoreUnknownMeta, setIgnoreUnknownChannel, setIgnoreWrongData を BmsLoader クラスに移動しました。

### Removed
- Be-Musicサブセットから BeMusic, BeMusicContent クラスを削除しました。

### Fixed
- BMSコンテンツ読み込み時、不正データ検出エラーを無視する設定でデータ重複が不可能なチャンネルでの重複を検出した時に想定外の例外がスローされる問題を修正しました。

## [0.0.1] - 2022-07-06
### Added
- 新規作成。
