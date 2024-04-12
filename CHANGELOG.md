# Change Log
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/)
and this project adheres to [Semantic Versioning](http://semver.org/).

## [0.7.0 / 1.1-D] - 2023-10-25
### Added
- BmsStandardSaver クラスが新設され、標準フォーマットでのBMSコンテンツ保存に対応しました。
- BMSコンテンツ読み込み機能の機能追加・見直しが行われ、以下の機能追加・変更が行われました。
    - BeMusicBmsonLoader クラスを新設し、bmson形式からのBMSコンテンツ読み込みに対応しました。
    - BmsStandardLoader クラスを新設し、標準フォーマットのBMS読み込み処理が見直されました。
    - BeMusic クラスを新設し、BMSコンテンツ読み込みに関連する高レベルAPIを追加しました。
    - BmsLibrary にデフォルトの文字セットを設定する setDefaultCharsets を追加しました。
    - BmsLibrary にデフォルトの文字セットを取得する getDefaultCharsets を追加しました。
    - BmsLibrary に最優先文字セットを取得する getPrimaryCharset を追加しました。
    - BmsLoader にBMS読み込み時に使用する文字セットを設定する setCharsets を追加しました。
- bmson形式サポートに伴い、コアライブラリ・Be-Musicサブセットへ以下の仕様調整が行われました。
    - BeMusicSpec に #CHARTNAME ヘッダを追加しました。(※1)
    - BeMusicSpec に #EYECHATCH ヘッダを追加しました。(※1)
    - BeMusicSoundNote クラスを新設し、bmson形式で必要になるノートの値の生成・取得に対応しました。<br><br>
※1 bmson独自の情報を保持するためのライブラリ独自のヘッダです<br><br>

- BOM付きの UTF-16LE, UTF-16BE でエンコードされたBMSファイルの読み込みに対応しました。
- BmsLibrary クラスを新設し、ライブラリバージョンを取得する getVersion を追加しました。
- BeMusicStatistics にDelta Systemのバージョンを取得する getDeltaSystemVersion を追加しました。
- BmsChannel に仕様チャンネルを生成する spec を追加しました。
- BmsLoader に厳格なフォーマットチェック有無を一括設定する setStrictly を追加しました。
- BeMusicSpec に最新のBe-Music用BMS仕様を生成する createLatest を追加しました。
- BeMusicScore にスクラッチモードを取得する getScratchMode を追加しました。
- Be-Musicサブセットに #SCROLL ヘッダの情報を取得する機能を追加しました。

### Changed
- BmsLoader は抽象クラスになり、インスタンス生成はできなくなりました。
- BmsSaver は抽象クラスになり、インスタンス生成はできなくなりました。
- BMSファイルの読み込みは、指定された文字セットで順番にデコードする方式に変更されました。
- #DEFEXRANK ヘッダをBe-Musicサブセットで正式にサポートしました。
- 取り扱い可能な小節番号上限を 999 から 65535 へ引き上げました。
- 取り扱い可能な索引付きメタ情報のインデックス値上限を 1295 から 65535 へ引き上げました。
- ノートに設定可能な値の範囲を0以外の全ての値(int型)に変更しました。
- BmsLoader#load では IOException の例外ハンドリングが義務付けられるようになりました。
- BeMusicPoint で扱われる小節長、スクロール速度、BPM、譜面停止時間のデータ精度が向上しました。
- 配列型チャンネルのデータ長が奇数の時、データ次第では自動修正しエラーにならないようにしました。
- 同一小節の配列型チャンネルが再定義された時、重複定義同士を合成するようになりました。
- 重複可能な配列型チャンネルで、空定義(00)の配列データは空配列として読み込まれるようになりました。

### Deprecated
- BeMusic クラスの新設に伴い、BeMusicSpec#loadContentFrom は非推奨になりました。
- BmsSpec の getStandardCharset, setStandardCharset は非推奨になりました。

### Removed
- BMS宣言に &quot;encoding&quot; を記述し文字セットを指定する機能は廃止されました。

### Fixed
- BPMの途中変更、譜面停止を多用するBMSの読み込みが極端に遅くなる問題を改善しました。
- BmsContent#putNote で指定小節・刻み位置に既にノートが存在する時、古いノートが新しいノートで上書きされない不具合を修正しました。
- #RANDOM による乱数の分岐判定が適切に動作しないことがある不具合を修正しました。
- タイムラインが空の状態で特定の BmsContent のメソッドを実行すると想定外の IllegalArgumentException がスローされる不具合を修正しました。
- 不可視オブジェ(31-3Z, 41-4Z)が重複可能チャンネルになってしまっている誤りを修正しました。
- 特定条件を満たすBMSコンテンツでDelta Systemによるレーティング値計算を行うと、ごく稀に想定外の実行時例外がスローされることがある不具合を修正しました。

## [0.6.0 / 1.0-D] - 2023-08-01
### Added
- Delta Systemによる譜面のレーティング機能を更新しました。具体的には以下の更新が含まれます。<br>※SP譜面のみ対応、BeMusicRatingType#DELTAは非対応です
    - BeMusicRatingType#SCRATCH の出力に対応しました。
    - BeMusicRatingType#HOLDING の出力に対応しました。
    - BeMusicRatingType#GIMMICK の出力に対応しました。
- スクロール速度変更(#SCROLL, SCチャンネル)への対応に伴い、以下の機能追加を行いました。
    - BeMusicPoint に現在のスクロール速度を取得する getCurrentScroll を追加しました。
    - BeMusicPoint にスクロール速度変更有無を取得する hasScroll を追加しました。
    - BeMusicScore にスクロール速度変更回数を取得する getChangeScrollCount を追加しました。
    - BeMusicScore にスクロール速度変化有無を取得する hasChangeScroll を追加しました。
- その他、以下のAPI追加・修正を行いました。
    - BeMusicDevice にレーンに対応する入力デバイスを取得する getDevices を追加しました。
    - BeMusicDevice にレーンに対応するスイッチを取得する getSwitches を追加しました。
    - BeMusicDevice にレーンに対応するスクラッチを取得する getScratch を追加しました。
    - BeMusicNoteType に長押し開始かを判定する isLongNoteHead を追加しました。
    - BeMusicNoteType に長押し関連のノート種別かを判定する isLongNoteType を追加しました。
    - BeMusicPoint に長押し継続有無を判定する hasHolding を追加しました。
    - BeMusicPoint に長押し開始有無を判定する hasLongNoteHead を追加しました。
    - BeMusicPoint に長押し終了有無を判定する hasLongNoteTail を追加しました。
    - BeMusicPoint に長押し関連ノート有無を判定する hasLongNoteType を追加しました。
    - BeMusicPoint に速度変更有無を判定する hasChangeSpeed を追加しました。
    - BeMusicPoint にギミック要素有無を判定する hasGimmick を追加しました。
    - BeMusicPoint に現在の譜面速度を取得する getCurrentSpeed を追加しました。
    - BeMusicPoint に地雷オブジェ有無を取得する hasLandmine を追加しました。
    - BeMusicScore に速度変更有無を取得する hasChangeSpeed を追加しました。
    - BeMusicScore にギミック要素有無を取得する hasGimmick を追加しました。

### Changed
- BeMusicRatingType#COMPLEX のレーティング値で以下のバランス調整を行いました。
    - HOLDING の実装に伴い、ロングノート主体の譜面での出力値を下方修正しました。
    - 同じ配置が連続して登場する譜面での出力値を下方修正しました。
    - 上記の下方修正を加味しつつ、全体的に0.5～3%程度の上方修正を行いました。
- BeMusicRatingType#RHYTHM のレーティング値で以下のバランス調整を行いました。
    - 総ノート数0の空譜面を入力した時の出力値が1だったものを0に修正しました。
    - 長い同一間隔のリズムキープがある譜面での出力値を上方修正しました。

## [0.5.0 / 0.0-D] - 2023-05-14
### Added
- Delta Systemによる譜面のレーティング機能(※)を追加しました。具体的には以下の機能追加を行っています。
    - レーティングの種別を表す BeMusicRatingType を追加しました。
    - レーティングに関する機能・定義を有する BeMusicRatings を追加しました。
    - 譜面統計情報生成時にレーティングを行う種別を設定する BeMusicStatisticsBuilder#addRating を追加しました。
    - BeMusicStatisticsに、Delta Systemのバージョンを取得する getRatingAlgorithmVersion を追加しました。
    - BeMusicStatisticsに、レーティング値を取得する getRating を追加しました。
    - BeMusicStatisticsに、譜面主傾向を取得する getPrimaryTendency を追加しました。
    - BeMusicStatisticsに、譜面副次傾向を取得する getSecondaryTendency を追加しました。
- BeMusicPointに、楽曲位置の属性を取得する getVisualEffectCount, hasVisualEffect, hasMovementNote を追加しました。
- BeMusicNoteTypeに、ノートの属性を取得する isHolding, hasUpAction, hasDownAction, hasMovement を追加しました。
- BeMusicMetaに、#COMMENTの取得・設定を行う getComment, setComment を追加しました。
- BeMusicHeaderに、#COMMENTの定義内容を取得する getComment を追加しました。
- BeMusicDeviceに、入力デバイスのスイッチ番号・種類を取得する getSwitchNumber, isSwitch, isScratch を追加しました。

※Delta Systemの詳細については[こちら](https://www.lm-t.com/content/bmslibrary/doc/deltasystem/)を参照してください。Delta Systemは本バージョンリリース時点では開発中となっています。現在対応しているレーティング種別は COMPLEX, POWER, RHYTHMの3種類となっており、シングルプレー譜面のみレーティング値を出力できます。非対応のレーティング種別、またはダブルプレー譜面を入力してもレーティング値は出力されません。また、本バージョンに搭載のDelta Systemはドラフト版となっており、レーティング値の精度はあまり高くありません。今後のアップデートにより同じ譜面で大きく異なるレーティング値を出力するようになる可能性があることに留意してください。

### Changed
- MGQ形式(#LNTYPE 2)のロングノート定義の読み込みに対応しました。

### Fixed
- BeMusicScoreにて、長押し継続中(BeMusicNoteType#LONG)が正しく設定されない不具合を修正しました。

## [0.4.0] - 2022-12-11
### Added
- タイムライン読み込みをスキップする機能を実装しました。 BmsLoader#setSkipReadTimeline() で機能のON/OFFを切り替えられます。

### Changed
- BMS読み込みエラーに関連するクラスを再構成しました。構成内容の詳細は以下を参照してください。
    - BmsLoadError.Kind を BmsErrorType へ移動しました。
    - BmsLoadError を BmsError, BmsScriptError に分割しました。
    - BmsAbortException を BmsLoadException へ名称変更しました。
    - BmsError を継承し、BMS関連のアプリケーション固有エラーを実装できる仕組みにしました。

## [0.3.0] - 2022-08-22
### Added
- #LNMODE ヘッダの読み込みに正式対応しました。
- 譜面の総ノート数から推奨するTOTAL値を取得する BeMusicScore#getRecommendTotal1/2 を追加しました。
- #ENDRANDOM の読み込みに対応しました。

### Removed
- BMSライブラリとして正式公開する予定のないクラスを Javadoc ドキュメントから削除しました。

### Fixed
- #RANDOM によるフロー制御が一部正常に動作しないケースがあったのを修正しました。

## [0.2.0] - 2022-08-17
### Added
- #SCROLL ヘッダとチャンネル &nbsp;SC&nbsp; の読み込みに対応しました。(※)
- #LNMODE ヘッダの読み込みに対応しました。(※)
- #PREVIEW ヘッダの読み込みに正式対応しました。
- BMSコンテンツ読み込み時の単体メタ情報・重複不可チャンネルの再定義許可を設定する BmsLoader#setAllowRedefine を追加しました。
- BMSライブラリ、およびBMS関連処理向けのキャッシュされたInteger型整数値を取得する BmsInt#box とキャッシュ制御用の各種APIを追加しました。

※読み込みエラーにはならなくなりましたが、Be-Musicサブセットの各オブジェクトへのデータ反映等は別途対応する予定です

### Changed
- BMSがBOM付きUTF-8でエンコードされている場合は、BMS宣言のencoding要素の定義状況に関わらずUTF-8でデコードするようになりました。

### Fixed
- BMSコンテンツ読み込み時にBMS仕様にないメタ情報を検出した時、スローされる例外に含まれるメッセージ内のメタ情報名称末尾が途切れている不具合を修正しました。

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
