# Change Log
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/)
and this project adheres to [Semantic Versioning](http://semver.org/).

## [0.8.0 / 1.2-D] - 2024-04-16
### Added
- BeMusicSequencer クラスを追加しました。時間経過によるBMS譜面のシーケンス処理を行うクラスです。
- BeMusicRatings に定数 TENDENCY_MIN/DELTA_ZERO/DELTA_MAX を追加しました。
- BeMusicRatings にDELTAのレーティング値変換を行う deltaAsDouble/deltaAsString を追加しました。
- BeMusicRatings に譜面傾向のレーティング値変換を行う tendencyAsDouble/tendencyAsString を追加しました。
- BeMusicStatisticsBuilder に addRating(Collection) を追加しました。
- BeMusicStatisticsBuilder にBMSコンテンツを直接指定するコンストラクタを追加しました。
- BeMusicChart に範囲を指定して楽曲位置情報をStreamで走査する points(int, int) を追加しました。
- BeMusicSound にノートの生値から各種情報を抽出する getNoteType/getDevice/hasDevice/hasTrack/hasExtended/isVisible/isInvisible/isBgm を追加しました。
- BeMusicPoint に各種ノートの生値をStreamで走査する visibles/invisibles/bgms/sounds を追加しました。
- BeMusicPoint に各種ノートの生値を列挙する enumVisibles/enumInvisibles/enumBgms/enumSounds を追加しました。
- BeMusicChartBuilder に簡易的にBMS譜面を生成する createChart を追加しました。
- BeMusicChartBuilder に拡張BMS譜面/楽曲位置情報を生成する setChartCreator/setPointCreator を追加しました。
- BeMusicChartBuilder に全情報のシーク有無を決定するコンストラクタを追加しました。
- BeMusicHeader にヘッダオブジェクトを構築する of(複数) を追加しました。
- BeMusicDevice にチャンネル番号を入力デバイスに変換する fromChannel を追加しました。
- BeMusicDevice に指定レーンのダブルプレー時入力デバイスリストを取得する orderedByDpList を追加しました。
- BeMusicDevice に全入力デバイスをStreamで走査する all を追加しました。
- BeMusicDevice にレーンの種別を取得する isPrimary/isSecondary を追加しました。
- BeMusicDevice に定数 COUNT_PER_LANE/PRIMARY_BASE/SECONDARY_BASE を追加しました。
- BeMusicDevice に指定レーンのベースインデックスを取得する getBaseIndex を追加しました。
- BmsStandardSaver に保存するBMSコンテンツにBOMを付加するかを設定する setAddBom を追加しました。
- BmsStandardSaver に最後に保存したBMSコンテンツの文字セットを取得する getLastProcessedCharset を追加しました。
- BmsSource クラスを追加しました。これはBMSローダのパーサ部に渡す入力データとして使用されます。
- BmsLoader への入力データとしてバイナリデータを指定できるようにしました。
- BmsLoader のバイナリフォーマット対応に伴い isBinaryFormat を追加しました。
- BmsLoader に最後に読み込んだBMSコンテンツの文字セット、BOM有無を取得する getLastProcessedCharset/getLastProcessedHasBom を追加しました。
- BmsLoader に標準フォーマット用のBMSローダかどうかを判定する isStandard を追加しました。
- BmsNote に新しいノートオブジェクトを構築する newNote を追加しました。
- BmsType に各データ型を判定するメソッド isXXXType を追加しました。(XXXはデータ型名)
- BmsMeasureValue(小節データのタイムライン要素) クラスを追加しました。
- BmsMetaKey/BmsMeta に構成単位/データ型を調べるメソッド(複数)を追加しました。
- BmsSpec にメタ情報とチャンネルをStreamで走査する metas/channels を追加しました。
- BmsSpec にメタ情報、チャンネル一覧を取得する getMetas(boolean)/getChannels(boolean) を追加しました。
- BmsContent に指定したBMSコンテンツのコピーを生成するコンストラクタを追加しました。
- BmsContent に簡易的に編集モードに遷移する edit を追加しました。
- BmsContent にタイムライン要素からデータを追加する putTimeline を追加しました。
- BmsContent にメタ情報要素からメタ情報を追加する putMeta を追加しました。
- BmsContent にメタ情報をStreamで走査する metas/singleMetas/multipleMetas/indexedMetas を追加しました。
- BmsContent にBMS宣言要素からBMS宣言を追加する putDeclarations を追加しました。
- BmsContent にBMS宣言をStreamで走査する declarations を追加しました。
- BmsLibrary にBMS Libraryのロゴを表示する機能 printLogo を追加しました。
- BMS Library全体を62進数データ型に対応したことに伴い、以下の追加・修正を行いました。
    - BmsInt が基数ごとにインスタンス化できるようになりました。
    - BmsInt のオブジェクトを取得する base16/base36/base62/of を追加しました。
    - BmsInt に62進数用メソッド to62i/to62s/to62ia/to62sa を追加しました。
    - BmsArray 生成時の基数に62を指定できるようになりました。
    - BmsArray#getRadix を getBase に名称変更しました。
    - BmsType にデータ型 BASE62/BASE/ARRAY62/ARRAY を追加しました。
    - BmsType にデータ型判定を行う isBase62/isArray62/isSelectableBaseType/isSelectableArrayType/isSelectable を追加しました。
    - BmsMeta にデータ型判定を行う isBase62/isArray62/isSelectableBaseType/isSelectableArrayType/isSelectableType を追加しました。
    - BmsSpecBuilder にメタ情報を基数選択メタ情報に設定する setBaseChangerMeta を追加しました。
    - BmsSpec に基数選択メタ情報を取得する getBaseChangerMeta/hasBaseChanger を追加しました。
    - BmsMeta に基数選択メタ情報かどうかを取得する isBaseChanger を追加しました。
    - BmsStandardLoader を基数選択メタ情報に対応しました。
    - BmsStandardSaver を基数選択メタ情報に対応しました。
    - BmsStandardSaver に標準フォーマットでの出力可否を判定する isCompatible を追加しました。
    - BeMusicMeta に基数選択メタ情報の #BASE を追加しました。
    - BeMusicMeta に #BASE への値の読み書きを行う getBase/setBase を追加しました。
    - BeMusicChannel に定義されたARRAY36型のチャンネルをARRAY型に変更しました。(一部のチャンネルを除く)
    - BeMusicSpec#createV1 で生成されるBMS仕様に #BASE を追加しました。
    - BeMusicSpec#LATEST に #BASE を追加しました。
    - BeMusicHeader に #BASE の値を取得する getBase を追加しました。

### Changed
- 各定数名・メソッド名に使用していた「Landmine」の単語を「Mine」に変更しました。
- BeMusicChart#stream を points に名称変更しました。
- BeMusicSoundNote を BeMusicSound に名称変更しました。
- BeMusicPoint の内部データ構造を見直し、メモリ使用量を削減しました。
- BeMusicPoint#getInvisible を getInvisibleValue に名称変更しました。
- BeMusicPoint#getNoteValue を getVisibleValue に名称変更しました。
- BeMusicPoint#getNoteType を getVisibleNoteType に名称変更しました。
- BeMusicScore を BeMusicChart に名称変更しました。
- BeMusicScoreBuilder を BeMusicChartBuilder に名称変更しました。
- BeMusicDevice#orderedByRightScratchList を orderedByRightList に名称変更しました。
- BeMusicDevice#orderedByLeftScratchList を orderedByLeftList に名称変更しました。
- BeMusicDevice#getLegacyLongChannel を getLongChannel に名称変更しました。
- BmsLoader#beginParse に渡すソースデータをString型からBmsSource型へ変更しました。
- BmsElement を BmsTimelineElement に名称変更しました。
- BmsType#NUMERIC を FLOAT に名称変更しました。
- BmsContent#enumNotes の列挙関数を BmsNote#Tester から Consumer&lt;BmsNote&gt; に変更しました。
- BmsContent#pointToTime をスレッドセーフで動作するように改良しました。
- BmsContent#addDeclaration を putDeclaration に名称変更しました。
- 以下のBMSコンテンツ解析関連クラスを名称変更し、com.lmt.lib.bms.parse パッケージに移動しました。
    - BmsLoader.ParsedElementType → BmsParsedType
    - BmsLoader.ParsedElement → BmsParsed
    - BmsLoader.DeclarationParsedElement → BmsDeclarationParsed
    - BmsLoader.MetaParsedElement → BmsMetaParsed
    - BmsLoader.ChannelParsedElement → BmsTimelineParsed
    - BmsLoader.ValueChannelParsedElement → BmsMeasureValueParsed
    - BmsLoader.ArrayChannelParsedElement → BmsNoteParsed
    - BmsLoadHandler.TestResult → BmsTestResult
- BMS Libraryで使用する関数インターフェイスを以下のように標準型化しました。
    - BmsContent#Creator → 廃止
    - BmsChannel#Tester → IntPredicate
    - BmsNote#Creator → Supplier&lt;BmsNote&gt;
    - BmsNote#Tester → Predicate&lt;BmsNote&gt;

### Removed
- BeMusicChart#getLastPlayablePoint を削除しました。
- BeMusicPoint#getDisplayPosition を削除しました。

### Fixed
- BeMusicChart からforeachで楽曲位置情報を走査すると無限ループになる不具合を修正しました。
- BmsContent#enumNotes/listNotes/countNotes において、チャンネル番号1が重複可能チャンネルだった場合に予期しないノートが列挙関数またはテスター関数に通知されることがある不具合を修正しました。

## [0.7.1 / 1.2-D] - 2024-02-18
### Changed
- BmsStandardSaver でコメント文出力が一般のBMS編集ソフトと同等になるように修正しました。
- Delta Systemの各テーマのレーティング値算出を以下のように調整しました。
    - COMPLEX：配置ごとの評価基準を見直し、配置の違いによる値の高低が顕著化しました。
    - POWER：指の動かしにくい配置では、より値が高くなるように調整されました。
    - POWER：出力される値を最大で約2.5%下方修正しました。
    - SCRATCH：スクラッチに近いKEYの配置による値の高低がより詳細に計算されるようになりました。
    - HOLDING：長押し操作が非常に少ない楽曲での値を大幅に下方修正(最大約50%減)しました。

### Fixed
- 非常に短い小節長を持つ楽曲が意図通りに構成されないことがある不具合を修正しました。
- BeMusicScore の楽曲位置情報検索処理がマルチスレッドで正常に動作しない不具合を修正しました。

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
    - BeMusicSpec に #EYECHATCH ヘッダを追加しました。(※1)<br>※1 bmson独自の情報を保持するためのライブラリ独自のヘッダです
    - BeMusicSoundNote クラスを新設し、bmson形式で必要になるノートの値の生成・取得に対応しました。

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
