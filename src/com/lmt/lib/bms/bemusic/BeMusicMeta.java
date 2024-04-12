package com.lmt.lib.bms.bemusic;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.lmt.lib.bms.BmsContent;
import com.lmt.lib.bms.BmsMeta;
import com.lmt.lib.bms.BmsSpec;
import com.lmt.lib.bms.BmsType;
import com.lmt.lib.bms.BmsUnit;

/**
 * Be-MusicのBMS仕様に含まれるメタ情報を表します。
 *
 * <p>当クラスは、Be-MusicのBMS仕様に含まれるメタ情報に関する定義のプレースホルダの役割を果たします。
 * そのため、インスタンスを生成することを想定していません。</p>
 *
 * <p><strong>メタ情報の説明について</strong><br>
 * 当クラスで定義するメタ情報の説明の各項目については以下を参照してください。</p>
 *
 * <ul>
 * <li>構成単位：メタ情報の定義構造を示します。詳細は{@link BmsUnit}を参照してください。</li>
 * <li>データ型：メタ情報の値のデータ型を示します。詳細は{@link BmsType}を参照してください。</li>
 * <li>初期値：メタ情報が定義されなかった場合の値を示します。</li>
 * <li>同一性チェック：BMSコンテンツからハッシュ値を生成する際、当該メタ情報を参考値として使用するかどうかを示します。</li>
 * <li>ライブラリ対応：Be-Musicサブセットが当該メタ情報を正式に対応するかどうかを示します。(※)</li>
 * <li>説明：メタ情報の概要を示します。</li>
 * </ul>
 *
 * <p>※Be-Musicが正式に対応していなくても、BMSコンテンツ内に定義値は格納されています。ただし、それらの定義値に
 * アクセスするには{@link BmsContent}が提供する低レベルAPIを使用する必要があります。「ライブラリが対応している」
 * というのは、当該メタ情報にアクセスするためのAPIをBe-Musicサブセットが用意していることを示します。</p>
 */
public class BeMusicMeta {
	/**
	 * #PLAYER
	 * <table><caption>&nbsp;</caption>
	 * <tr><th style="text-align:left;">構成単位</th><td>単体</td></tr>
	 * <tr><th style="text-align:left;">データ型</th><td>INTEGER</td></tr>
	 * <tr><th style="text-align:left;">初期値</th><td>1</td></tr>
	 * <tr><th style="text-align:left;">同一性チェック</th><td>YES</td></tr>
	 * <tr><th style="text-align:left;">ライブラリ対応</th><td>YES</td></tr>
	 * <tr><th style="text-align:left;">説明</th>
	 * <td>プレイヤーの数を表します。値の詳細については{@link BeMusicPlayer}を参照してください。</td></tr>
	 * </table>
	 */
	public static final BmsMeta PLAYER = BmsMeta.single("#player", BmsType.INTEGER, "1", 0, true);
	/**
	 * #GENRE
	 * <table><caption>&nbsp;</caption>
	 * <tr><th style="text-align:left;">構成単位</th><td>単体</td></tr>
	 * <tr><th style="text-align:left;">データ型</th><td>STRING</td></tr>
	 * <tr><th style="text-align:left;">初期値</th><td>""</td></tr>
	 * <tr><th style="text-align:left;">同一性チェック</th><td>YES</td></tr>
	 * <tr><th style="text-align:left;">ライブラリ対応</th><td>YES</td></tr>
	 * <tr><th style="text-align:left;">説明</th>
	 * <td>楽曲のジャンルを表します。</td></tr>
	 * </table>
	 */
	public static final BmsMeta GENRE = BmsMeta.single("#genre", BmsType.STRING, "", 0, true);
	/**
	 * #TITLE
	 * <table><caption>&nbsp;</caption>
	 * <tr><th style="text-align:left;">構成単位</th><td>単体</td></tr>
	 * <tr><th style="text-align:left;">データ型</th><td>STRING</td></tr>
	 * <tr><th style="text-align:left;">初期値</th><td>""</td></tr>
	 * <tr><th style="text-align:left;">同一性チェック</th><td>YES</td></tr>
	 * <tr><th style="text-align:left;">ライブラリ対応</th><td>YES</td></tr>
	 * <tr><th style="text-align:left;">説明</th>
	 * <td>楽曲名を表します。</td></tr>
	 * </table>
	 */
	public static final BmsMeta TITLE = BmsMeta.single("#title", BmsType.STRING, "", 0, true);
	/**
	 * #SUBTITLE
	 * <table><caption>&nbsp;</caption>
	 * <tr><th style="text-align:left;">構成単位</th><td>単体</td></tr>
	 * <tr><th style="text-align:left;">データ型</th><td>STRING</td></tr>
	 * <tr><th style="text-align:left;">初期値</th><td>""</td></tr>
	 * <tr><th style="text-align:left;">同一性チェック</th><td>YES</td></tr>
	 * <tr><th style="text-align:left;">ライブラリ対応</th><td>YES</td></tr>
	 * <tr><th style="text-align:left;">説明</th>
	 * <td>楽曲の副題を表します。</td></tr>
	 * </table>
	 */
	public static final BmsMeta SUBTITLE = BmsMeta.single("#subtitle", BmsType.STRING, "", 0, true);
	/**
	 * #ARTIST
	 * <table><caption>&nbsp;</caption>
	 * <tr><th style="text-align:left;">構成単位</th><td>単体</td></tr>
	 * <tr><th style="text-align:left;">データ型</th><td>STRING</td></tr>
	 * <tr><th style="text-align:left;">初期値</th><td>""</td></tr>
	 * <tr><th style="text-align:left;">同一性チェック</th><td>YES</td></tr>
	 * <tr><th style="text-align:left;">ライブラリ対応</th><td>YES</td></tr>
	 * <tr><th style="text-align:left;">説明</th>
	 * <td>アーティスト名を表します。</td></tr>
	 * </table>
	 */
	public static final BmsMeta ARTIST = BmsMeta.single("#artist", BmsType.STRING, "", 0, true);
	/**
	 * #SUBARTIST
	 * <table><caption>&nbsp;</caption>
	 * <tr><th style="text-align:left;">構成単位</th><td>複数</td></tr>
	 * <tr><th style="text-align:left;">データ型</th><td></td></tr>
	 * <tr><th style="text-align:left;">初期値</th><td></td></tr>
	 * <tr><th style="text-align:left;">同一性チェック</th><td>YES</td></tr>
	 * <tr><th style="text-align:left;">ライブラリ対応</th><td>YES</td></tr>
	 * <tr><th style="text-align:left;">説明</th>
	 * <td>サブアーティスト名一覧を表します。</td></tr>
	 * </table>
	 */
	public static final BmsMeta SUBARTIST = BmsMeta.multiple("#subartist", BmsType.STRING, "", 0, true);
	/**
	 * #BPM
	 * <table><caption>&nbsp;</caption>
	 * <tr><th style="text-align:left;">構成単位</th><td>単体</td></tr>
	 * <tr><th style="text-align:left;">データ型</th><td>NUMERIC</td></tr>
	 * <tr><th style="text-align:left;">初期値</th><td>130</td></tr>
	 * <tr><th style="text-align:left;">同一性チェック</th><td>YES</td></tr>
	 * <tr><th style="text-align:left;">ライブラリ対応</th><td>YES</td></tr>
	 * <tr><th style="text-align:left;">説明</th>
	 * <td>楽曲の初期BPMを表します。</td></tr>
	 * </table>
	 */
	public static final BmsMeta INITIAL_BPM = BmsMeta.single("#bpm", BmsType.NUMERIC, "130.0", 0, true);
	/**
	 * #BASEBPM
	 * <table><caption>&nbsp;</caption>
	 * <tr><th style="text-align:left;">構成単位</th><td>単体</td></tr>
	 * <tr><th style="text-align:left;">データ型</th><td>NUMERIC</td></tr>
	 * <tr><th style="text-align:left;">初期値</th><td>0.0</td></tr>
	 * <tr><th style="text-align:left;">同一性チェック</th><td>YES</td></tr>
	 * <tr><th style="text-align:left;">ライブラリ対応</th><td>NO</td></tr>
	 * <tr><th style="text-align:left;">説明</th>
	 * <td>譜面のスクロール速度の標準値として使われるBPMを表します。</td></tr>
	 * </table>
	 */
	public static final BmsMeta BASEBPM = BmsMeta.single("#basebpm", BmsType.NUMERIC, "0.0", 1, true);
	/**
	 * #CDDA
	 * <table><caption>&nbsp;</caption>
	 * <tr><th style="text-align:left;">構成単位</th><td>単体</td></tr>
	 * <tr><th style="text-align:left;">データ型</th><td>INTEGER</td></tr>
	 * <tr><th style="text-align:left;">初期値</th><td>0</td></tr>
	 * <tr><th style="text-align:left;">同一性チェック</th><td>NO</td></tr>
	 * <tr><th style="text-align:left;">ライブラリ対応</th><td>NO</td></tr>
	 * <tr><th style="text-align:left;">説明</th>
	 * <td>BGMとして使用されるCD-DAのトラック番号を表します。</td></tr>
	 * </table>
	 */
	public static final BmsMeta CDDA = BmsMeta.single("#cdda", BmsType.INTEGER, "0", 1, false);
	/**
	 * #MIDIFILE
	 * <table><caption>&nbsp;</caption>
	 * <tr><th style="text-align:left;">構成単位</th><td>単体</td></tr>
	 * <tr><th style="text-align:left;">データ型</th><td>STRING</td></tr>
	 * <tr><th style="text-align:left;">初期値</th><td>""</td></tr>
	 * <tr><th style="text-align:left;">同一性チェック</th><td>NO</td></tr>
	 * <tr><th style="text-align:left;">ライブラリ対応</th><td>NO</td></tr>
	 * <tr><th style="text-align:left;">説明</th>
	 * <td>BGMとして再生するMIDIファイルを表します。MIDIはハードウェア構成によって発音が非常に異なり、且つソフトウェアシンセサイザなどは
	 * 発音に遅延があることから、使用することは推奨されません。</td></tr>
	 * </table>
	 */
	public static final BmsMeta MIDIFILE = BmsMeta.single("#midifile", BmsType.STRING, "", 1, false);
	/**
	 * #DIFFICULTY
	 * <table><caption>&nbsp;</caption>
	 * <tr><th style="text-align:left;">構成単位</th><td>単体</td></tr>
	 * <tr><th style="text-align:left;">データ型</th><td>INTEGER</td></tr>
	 * <tr><th style="text-align:left;">初期値</th><td>0 (OTHER)</td></tr>
	 * <tr><th style="text-align:left;">同一性チェック</th><td>NO</td></tr>
	 * <tr><th style="text-align:left;">ライブラリ対応</th><td>YES</td></tr>
	 * <tr><th style="text-align:left;">説明</th>
	 * <td>譜面の難易度を表します。値の詳細については{@link BeMusicDifficulty}を参照してください。</td></tr>
	 * </table>
	 */
	public static final BmsMeta DIFFICULTY = BmsMeta.single("#difficulty", BmsType.INTEGER, "0", 0, false);
	/**
	 * #PLAYLEVEL
	 * <table><caption>&nbsp;</caption>
	 * <tr><th style="text-align:left;">構成単位</th><td>単体</td></tr>
	 * <tr><th style="text-align:left;">データ型</th><td>STRING</td></tr>
	 * <tr><th style="text-align:left;">初期値</th><td>3</td></tr>
	 * <tr><th style="text-align:left;">同一性チェック</th><td>NO</td></tr>
	 * <tr><th style="text-align:left;">ライブラリ対応</th><td>YES</td></tr>
	 * <tr><th style="text-align:left;">説明</th>
	 * <td>選曲時に表示されるべき譜面の難易度を表した数値です。</td></tr>
	 * </table>
	 */
	public static final BmsMeta PLAYLEVEL = BmsMeta.single("#playlevel", BmsType.STRING, "3.0", 0, false);
	/**
	 * #RANK
	 * <table><caption>&nbsp;</caption>
	 * <tr><th style="text-align:left;">構成単位</th><td>単体</td></tr>
	 * <tr><th style="text-align:left;">データ型</th><td>INTEGER</td></tr>
	 * <tr><th style="text-align:left;">初期値</th><td>2 (NORMAL)</td></tr>
	 * <tr><th style="text-align:left;">同一性チェック</th><td>YES</td></tr>
	 * <tr><th style="text-align:left;">ライブラリ対応</th><td>YES</td></tr>
	 * <tr><th style="text-align:left;">説明</th>
	 * <td>判定難易度を表します。詳細については{@link BeMusicRank}を参照してください。</td></tr>
	 * </table>
	 */
	public static final BmsMeta RANK = BmsMeta.single("#rank", BmsType.INTEGER, "2", 0, true);
	/**
	 * #DEFEXRANK
	 * <table><caption>&nbsp;</caption>
	 * <tr><th style="text-align:left;">構成単位</th><td>単体</td></tr>
	 * <tr><th style="text-align:left;">データ型</th><td>INTEGER</td></tr>
	 * <tr><th style="text-align:left;">初期値</th><td>100</td></tr>
	 * <tr><th style="text-align:left;">同一性チェック</th><td>YES</td></tr>
	 * <tr><th style="text-align:left;">ライブラリ対応</th><td>NO</td></tr>
	 * <tr><th style="text-align:left;">説明</th>
	 * <td>{@link #RANK}よりも詳細な判定難易度値を表します。この定義は昨今(2020年前半)のBMSプレーヤーでも非サポートである場合があることから、
	 * 当ライブラリでの対応ポリシーから外れるため、ライブラリでの正式な対応は行っていません。</td></tr>
	 * </table>
	 */
	public static final BmsMeta DEFEXRANK = BmsMeta.single("#defexrank", BmsType.INTEGER, "100", 1, true);
	/**
	 * #TOTAL
	 * <table><caption>&nbsp;</caption>
	 * <tr><th style="text-align:left;">構成単位</th><td>単体</td></tr>
	 * <tr><th style="text-align:left;">データ型</th><td>NUMERIC</td></tr>
	 * <tr><th style="text-align:left;">初期値</th><td>160</td></tr>
	 * <tr><th style="text-align:left;">同一性チェック</th><td>YES</td></tr>
	 * <tr><th style="text-align:left;">ライブラリ対応</th><td>YES</td></tr>
	 * <tr><th style="text-align:left;">説明</th>
	 * <td>プレー判定が最良だった場合のゲージの増加率を表します。</td></tr>
	 * </table>
	 */
	public static final BmsMeta TOTAL = BmsMeta.single("#total", BmsType.NUMERIC, "160", 0, true);
	/**
	 * #VOLWAV
	 * <table><caption>&nbsp;</caption>
	 * <tr><th style="text-align:left;">構成単位</th><td>単体</td></tr>
	 * <tr><th style="text-align:left;">データ型</th><td>INTEGER</td></tr>
	 * <tr><th style="text-align:left;">初期値</th><td>100</td></tr>
	 * <tr><th style="text-align:left;">同一性チェック</th><td>NO</td></tr>
	 * <tr><th style="text-align:left;">ライブラリ対応</th><td>NO</td></tr>
	 * <tr><th style="text-align:left;">説明</th>
	 * <td>譜面が使用する音の再生音量を表します。</td></tr>
	 * </table>
	 */
	public static final BmsMeta VOLWAV = BmsMeta.single("#volwav", BmsType.INTEGER, "100", 1, false);
	/**
	 * #CHARFILE
	 * <table><caption>&nbsp;</caption>
	 * <tr><th style="text-align:left;">構成単位</th><td>単体</td></tr>
	 * <tr><th style="text-align:left;">データ型</th><td>STRING</td></tr>
	 * <tr><th style="text-align:left;">初期値</th><td>""</td></tr>
	 * <tr><th style="text-align:left;">同一性チェック</th><td>NO</td></tr>
	 * <tr><th style="text-align:left;">ライブラリ対応</th><td>NO</td></tr>
	 * <tr><th style="text-align:left;">説明</th>
	 * <td>キャラクターファイルを表します。</td></tr>
	 * </table>
	 */
	public static final BmsMeta CHARFILE = BmsMeta.single("#charfile", BmsType.STRING, "", 1, false);
	/**
	 * #COMMENT
	 * <table><caption>&nbsp;</caption>
	 * <tr><th style="text-align:left;">構成単位</th><td>単体</td></tr>
	 * <tr><th style="text-align:left;">データ型</th><td>STRING</td></tr>
	 * <tr><th style="text-align:left;">初期値</th><td>""</td></tr>
	 * <tr><th style="text-align:left;">同一性チェック</th><td>NO</td></tr>
	 * <tr><th style="text-align:left;">ライブラリ対応</th><td>NO</td></tr>
	 * <tr><th style="text-align:left;">説明</th>
	 * <td>選曲中に表示される楽曲のコメント内容を表します。</td></tr>
	 * </table>
	 */
	public static final BmsMeta COMMENT = BmsMeta.single("#comment", BmsType.STRING, "", 1, false);
	/**
	 * #BANNER
	 * <table><caption>&nbsp;</caption>
	 * <tr><th style="text-align:left;">構成単位</th><td>単体</td></tr>
	 * <tr><th style="text-align:left;">データ型</th><td>STRING</td></tr>
	 * <tr><th style="text-align:left;">初期値</th><td>""</td></tr>
	 * <tr><th style="text-align:left;">同一性チェック</th><td>NO</td></tr>
	 * <tr><th style="text-align:left;">ライブラリ対応</th><td>YES</td></tr>
	 * <tr><th style="text-align:left;">説明</th>
	 * <td>選曲時に表示する横長の画像ファイル名を表します。</td></tr>
	 * </table>
	 */
	public static final BmsMeta BANNER = BmsMeta.single("#banner", BmsType.STRING, "", 0, false);
	/**
	 * #STAGEFILE
	 * <table><caption>&nbsp;</caption>
	 * <tr><th style="text-align:left;">構成単位</th><td>単体</td></tr>
	 * <tr><th style="text-align:left;">データ型</th><td>STRING</td></tr>
	 * <tr><th style="text-align:left;">初期値</th><td>""</td></tr>
	 * <tr><th style="text-align:left;">同一性チェック</th><td>NO</td></tr>
	 * <tr><th style="text-align:left;">ライブラリ対応</th><td>YES</td></tr>
	 * <tr><th style="text-align:left;">説明</th>
	 * <td>楽曲のロード中に表示する画像ファイル名を表します。</td></tr>
	 * </table>
	 */
	public static final BmsMeta STAGEFILE = BmsMeta.single("#stagefile", BmsType.STRING, "", 0, false);
	/**
	 * #BACKBMP
	 * <table><caption>&nbsp;</caption>
	 * <tr><th style="text-align:left;">構成単位</th><td>単体</td></tr>
	 * <tr><th style="text-align:left;">データ型</th><td>STRING</td></tr>
	 * <tr><th style="text-align:left;">初期値</th><td>""</td></tr>
	 * <tr><th style="text-align:left;">同一性チェック</th><td>NO</td></tr>
	 * <tr><th style="text-align:left;">ライブラリ対応</th><td>YES</td></tr>
	 * <tr><th style="text-align:left;">説明</th>
	 * <td>楽曲のプレイ画面の背景として表示する画像ファイル名を表します。</td></tr>
	 * </table>
	 */
	public static final BmsMeta BACKBMP = BmsMeta.single("#backbmp", BmsType.STRING, "", 0, false);
	/**
	 * #PREVIEW
	 * <table><caption>&nbsp;</caption>
	 * <tr><th style="text-align:left;">構成単位</th><td>単体</td></tr>
	 * <tr><th style="text-align:left;">データ型</th><td>STRING</td></tr>
	 * <tr><th style="text-align:left;">初期値</th><td>""</td></tr>
	 * <tr><th style="text-align:left;">同一性チェック</th><td>NO</td></tr>
	 * <tr><th style="text-align:left;">ライブラリ対応</th><td>YES</td></tr>
	 * <tr><th style="text-align:left;">説明</th>
	 * <td>楽曲のプレビュー音源のファイルパスを表します。</td></tr>
	 * </table>
	 */
	public static final BmsMeta PREVIEW = BmsMeta.single("#preview", BmsType.STRING, "", 0, false);
	/**
	 * #POORBGA
	 * <table><caption>&nbsp;</caption>
	 * <tr><th style="text-align:left;">構成単位</th><td>単体</td></tr>
	 * <tr><th style="text-align:left;">データ型</th><td>INTEGER</td></tr>
	 * <tr><th style="text-align:left;">初期値</th><td>0</td></tr>
	 * <tr><th style="text-align:left;">同一性チェック</th><td>NO</td></tr>
	 * <tr><th style="text-align:left;">ライブラリ対応</th><td>NO</td></tr>
	 * <tr><th style="text-align:left;">説明</th>
	 * <td>ミス時のイメージ表示方式を表します。BeMusicライブラリとしては正式にサポートしないため、定義値は定数化していません。</td></tr>
	 * </table>
	 */
	public static final BmsMeta POORBGA = BmsMeta.single("#poorbga", BmsType.INTEGER, "0", 1, false);
	/**
	 * #MOVIE
	 * <table><caption>&nbsp;</caption>
	 * <tr><th style="text-align:left;">構成単位</th><td>単体</td></tr>
	 * <tr><th style="text-align:left;">データ型</th><td>STRING</td></tr>
	 * <tr><th style="text-align:left;">初期値</th><td>""</td></tr>
	 * <tr><th style="text-align:left;">同一性チェック</th><td>NO</td></tr>
	 * <tr><th style="text-align:left;">ライブラリ対応</th><td>NO</td></tr>
	 * <tr><th style="text-align:left;">説明</th>
	 * <td>BGAとして使用する動画のファイル名を表します。</td></tr>
	 * </table>
	 */
	public static final BmsMeta MOVIE = BmsMeta.single("#movie", BmsType.STRING, "", 1, false);
	/**
	 * #VIDEOFILE
	 * <table><caption>&nbsp;</caption>
	 * <tr><th style="text-align:left;">構成単位</th><td>単体</td></tr>
	 * <tr><th style="text-align:left;">データ型</th><td>STRING</td></tr>
	 * <tr><th style="text-align:left;">初期値</th><td>""</td></tr>
	 * <tr><th style="text-align:left;">同一性チェック</th><td>NO</td></tr>
	 * <tr><th style="text-align:left;">ライブラリ対応</th><td>NO</td></tr>
	 * <tr><th style="text-align:left;">説明</th>
	 * <td>BGAとして使用する動画のファイル名を表します。</td></tr>
	 * </table>
	 */
	public static final BmsMeta VIDEOFILE = BmsMeta.single("#videofile", BmsType.STRING, "", 1, false);
	/**
	 * #VIDEOf/s
	 * <table><caption>&nbsp;</caption>
	 * <tr><th style="text-align:left;">構成単位</th><td>単体</td></tr>
	 * <tr><th style="text-align:left;">データ型</th><td>INTEGER</td></tr>
	 * <tr><th style="text-align:left;">初期値</th><td>0</td></tr>
	 * <tr><th style="text-align:left;">同一性チェック</th><td>NO</td></tr>
	 * <tr><th style="text-align:left;">ライブラリ対応</th><td>NO</td></tr>
	 * <tr><th style="text-align:left;">説明</th>
	 * <td>動画のフレームレート(FPS)を表します。</td></tr>
	 * </table>
	 */
	public static final BmsMeta VIDEOFPS = BmsMeta.single("#videof/s", BmsType.INTEGER, "0", 1, false);
	/**
	 * #VIDEOCOLORS
	 * <table><caption>&nbsp;</caption>
	 * <tr><th style="text-align:left;">構成単位</th><td>単体</td></tr>
	 * <tr><th style="text-align:left;">データ型</th><td>INTEGER</td></tr>
	 * <tr><th style="text-align:left;">初期値</th><td>16</td></tr>
	 * <tr><th style="text-align:left;">同一性チェック</th><td>NO</td></tr>
	 * <tr><th style="text-align:left;">ライブラリ対応</th><td>NO</td></tr>
	 * <tr><th style="text-align:left;">説明</th>
	 * <td>動画のカラーパレットを表します。</td></tr>
	 * </table>
	 */
	public static final BmsMeta VIDEOCOLORS = BmsMeta.single("#videocolors", BmsType.INTEGER, "16", 1, false);
	/**
	 * #VIDEODLY
	 * <table><caption>&nbsp;</caption>
	 * <tr><th style="text-align:left;">構成単位</th><td>単体</td></tr>
	 * <tr><th style="text-align:left;">データ型</th><td>INTEGER</td></tr>
	 * <tr><th style="text-align:left;">初期値</th><td>0</td></tr>
	 * <tr><th style="text-align:left;">同一性チェック</th><td>NO</td></tr>
	 * <tr><th style="text-align:left;">ライブラリ対応</th><td>NO</td></tr>
	 * <tr><th style="text-align:left;">説明</th>
	 * <td>動画がどのフレームから再生されるかを表します。</td></tr>
	 * </table>
	 */
	public static final BmsMeta VIDEODLY = BmsMeta.single("#videodly", BmsType.INTEGER, "0", 1, false);
	/**
	 * #PATH_WAV
	 * <table><caption>&nbsp;</caption>
	 * <tr><th style="text-align:left;">構成単位</th><td>単体</td></tr>
	 * <tr><th style="text-align:left;">データ型</th><td>STRING</td></tr>
	 * <tr><th style="text-align:left;">初期値</th><td>""</td></tr>
	 * <tr><th style="text-align:left;">同一性チェック</th><td>NO</td></tr>
	 * <tr><th style="text-align:left;">ライブラリ対応</th><td>NO</td></tr>
	 * <tr><th style="text-align:left;">説明</th>
	 * <td>当該BMSが使用する音声・画像ファイルなどのリソースが存在するファイルパスを表します。</td></tr>
	 * </table>
	 */
	public static final BmsMeta PATH_WAV = BmsMeta.single("#path_wav", BmsType.STRING, "", 1, false);
	/**
	 * #MATERIALSWAV
	 * <table><caption>&nbsp;</caption>
	 * <tr><th style="text-align:left;">構成単位</th><td>単体</td></tr>
	 * <tr><th style="text-align:left;">データ型</th><td>STRING</td></tr>
	 * <tr><th style="text-align:left;">初期値</th><td>""</td></tr>
	 * <tr><th style="text-align:left;">同一性チェック</th><td>NO</td></tr>
	 * <tr><th style="text-align:left;">ライブラリ対応</th><td>NO</td></tr>
	 * <tr><th style="text-align:left;">説明</th>
	 * <td>Materialsフォルダを起点とする相対パスを表します。</td></tr>
	 * </table>
	 */
	public static final BmsMeta MATERIALSWAV = BmsMeta.single("#materialswav", BmsType.STRING, "", 1, false);
	/**
	 * #MATERIALSBMP
	 * <table><caption>&nbsp;</caption>
	 * <tr><th style="text-align:left;">構成単位</th><td>単体</td></tr>
	 * <tr><th style="text-align:left;">データ型</th><td>STRING</td></tr>
	 * <tr><th style="text-align:left;">初期値</th><td>""</td></tr>
	 * <tr><th style="text-align:left;">同一性チェック</th><td>NO</td></tr>
	 * <tr><th style="text-align:left;">ライブラリ対応</th><td>NO</td></tr>
	 * <tr><th style="text-align:left;">説明</th>
	 * <td>Materialsフォルダを起点とする相対パスを表します。</td></tr>
	 * </table>
	 */
	public static final BmsMeta MATERIALSBMP = BmsMeta.single("#materialsbmp", BmsType.STRING, "", 1, false);
	/**
	 * #DIVIDEPROP
	 * <table><caption>&nbsp;</caption>
	 * <tr><th style="text-align:left;">構成単位</th><td>単体</td></tr>
	 * <tr><th style="text-align:left;">データ型</th><td>INTEGER</td></tr>
	 * <tr><th style="text-align:left;">初期値</th><td>960</td></tr>
	 * <tr><th style="text-align:left;">同一性チェック</th><td>YES</td></tr>
	 * <tr><th style="text-align:left;">ライブラリ対応</th><td>NO</td></tr>
	 * <tr><th style="text-align:left;">説明</th>
	 * <td>小節の分解能を表します。</td></tr>
	 * </table>
	 */
	public static final BmsMeta DIVIDEPROP = BmsMeta.single("#divideprop", BmsType.INTEGER, "960", 1, true);
	/**
	 * #CHARSET
	 * <table><caption>&nbsp;</caption>
	 * <tr><th style="text-align:left;">構成単位</th><td>単体</td></tr>
	 * <tr><th style="text-align:left;">データ型</th><td>STRING</td></tr>
	 * <tr><th style="text-align:left;">初期値</th><td>""</td></tr>
	 * <tr><th style="text-align:left;">同一性チェック</th><td>NO</td></tr>
	 * <tr><th style="text-align:left;">ライブラリ対応</th><td>NO</td></tr>
	 * <tr><th style="text-align:left;">説明</th>
	 * <td>BMSがエンコードされた文字セットを表します。</td></tr>
	 * </table>
	 */
	public static final BmsMeta CHARSET = BmsMeta.single("#charset", BmsType.STRING, "", 1, false);
	/**
	 * #OCT/FP
	 * <table><caption>&nbsp;</caption>
	 * <tr><th style="text-align:left;">構成単位</th><td>単体</td></tr>
	 * <tr><th style="text-align:left;">データ型</th><td>STRING</td></tr>
	 * <tr><th style="text-align:left;">初期値</th><td>""</td></tr>
	 * <tr><th style="text-align:left;">同一性チェック</th><td>YES</td></tr>
	 * <tr><th style="text-align:left;">ライブラリ対応</th><td>NO</td></tr>
	 * <tr><th style="text-align:left;">説明</th>
	 * <td>オクターブモード/フットペダルモードであることを表します。このメタ情報は値を持ちません。何らかの文字列を指定してもBeMusicライブラリでは
	 * エラーを生成しませんが、アプリケーション側でエラーになる場合があるかもしれません。</td></tr>
	 * </table>
	 */
	public static final BmsMeta OCT_FP = BmsMeta.single("#oct/fp", BmsType.STRING, "", 1, true);
	/**
	 * #LNTYPE
	 * <table><caption>&nbsp;</caption>
	 * <tr><th style="text-align:left;">構成単位</th><td>単体</td></tr>
	 * <tr><th style="text-align:left;">データ型</th><td>INTEGER</td></tr>
	 * <tr><th style="text-align:left;">初期値</th><td>1</td></tr>
	 * <tr><th style="text-align:left;">同一性チェック</th><td>NO</td></tr>
	 * <tr><th style="text-align:left;">ライブラリ対応</th><td>NO</td></tr>
	 * <tr><th style="text-align:left;">説明</th>
	 * <td>ロングノートの記法を表します。</td></tr>
	 * </table>
	 */
	public static final BmsMeta LNTYPE = BmsMeta.single("#lntype", BmsType.INTEGER, "1", 1, false);
	/**
	 * #LNOBJ
	 * <table><caption>&nbsp;</caption>
	 * <tr><th style="text-align:left;">構成単位</th><td>複数</td></tr>
	 * <tr><th style="text-align:left;">データ型</th><td>BASE36</td></tr>
	 * <tr><th style="text-align:left;">初期値</th><td>00</td></tr>
	 * <tr><th style="text-align:left;">同一性チェック</th><td>YES</td></tr>
	 * <tr><th style="text-align:left;">ライブラリ対応</th><td>YES</td></tr>
	 * <tr><th style="text-align:left;">説明</th>
	 * <td>ロングノートの終端を表すノートの値を表します。この値は複数記述することが出来ます。</td></tr>
	 * </table>
	 */
	public static final BmsMeta LNOBJ = BmsMeta.multiple("#lnobj", BmsType.BASE36, "00", 0, true);
	/**
	 * #LNMODE
	 * <table><caption>&nbsp;</caption>
	 * <tr><th style="text-align:left;">構成単位</th><td>単体</td></tr>
	 * <tr><th style="text-align:left;">データ型</th><td>INTEGER</td></tr>
	 * <tr><th style="text-align:left;">初期値</th><td>1</td></tr>
	 * <tr><th style="text-align:left;">同一性チェック</th><td>YES</td></tr>
	 * <tr><th style="text-align:left;">ライブラリ対応</th><td>YES</td></tr>
	 * <tr><th style="text-align:left;">説明</th>
	 * <td>ロングノートの種類を表します。</td></tr>
	 * </table>
	 */
	public static final BmsMeta LNMODE = BmsMeta.single("#lnmode", BmsType.INTEGER, "1", 0, true);
	/**
	 * #MAKER
	 * <table><caption>&nbsp;</caption>
	 * <tr><th style="text-align:left;">構成単位</th><td>単体</td></tr>
	 * <tr><th style="text-align:left;">データ型</th><td>STRING</td></tr>
	 * <tr><th style="text-align:left;">初期値</th><td>""</td></tr>
	 * <tr><th style="text-align:left;">同一性チェック</th><td>NO</td></tr>
	 * <tr><th style="text-align:left;">ライブラリ対応</th><td>NO</td></tr>
	 * <tr><th style="text-align:left;">説明</th>
	 * <td>BMS制作者を表します。</td></tr>
	 * </table>
	 */
	public static final BmsMeta MAKER = BmsMeta.single("#maker", BmsType.STRING, "", 1, false);
	/**
	 * %URL
	 * <table><caption>&nbsp;</caption>
	 * <tr><th style="text-align:left;">構成単位</th><td>単体</td></tr>
	 * <tr><th style="text-align:left;">データ型</th><td>STRING</td></tr>
	 * <tr><th style="text-align:left;">初期値</th><td>""</td></tr>
	 * <tr><th style="text-align:left;">同一性チェック</th><td>NO</td></tr>
	 * <tr><th style="text-align:left;">ライブラリ対応</th><td>YES</td></tr>
	 * <tr><th style="text-align:left;">説明</th>
	 * <td>BMS作成者のウェブページのURLを表します。</td></tr>
	 * </table>
	 */
	public static final BmsMeta URL = BmsMeta.single("%url", BmsType.STRING, "", 0, false);
	/**
	 * %EMAIL
	 * <table><caption>&nbsp;</caption>
	 * <tr><th style="text-align:left;">構成単位</th><td>単体</td></tr>
	 * <tr><th style="text-align:left;">データ型</th><td>STRING</td></tr>
	 * <tr><th style="text-align:left;">初期値</th><td>""</td></tr>
	 * <tr><th style="text-align:left;">同一性チェック</th><td>NO</td></tr>
	 * <tr><th style="text-align:left;">ライブラリ対応</th><td>YES</td></tr>
	 * <tr><th style="text-align:left;">説明</th>
	 * <td>BMS作成者のEメールアドレスを表します。</td></tr>
	 * </table>
	 */
	public static final BmsMeta EMAIL = BmsMeta.single("%email", BmsType.STRING, "", 0, false);
	/**
	 * #RANDOM
	 * <table><caption>&nbsp;</caption>
	 * <tr><th style="text-align:left;">構成単位</th><td>単体</td></tr>
	 * <tr><th style="text-align:left;">データ型</th><td>INTEGER</td></tr>
	 * <tr><th style="text-align:left;">初期値</th><td>0</td></tr>
	 * <tr><th style="text-align:left;">同一性チェック</th><td>NO</td></tr>
	 * <tr><th style="text-align:left;">ライブラリ対応</th><td>YES</td></tr>
	 * <tr><th style="text-align:left;">説明</th>
	 * <td>乱数を生成します。</td></tr>
	 * </table>
	 */
	public static final BmsMeta RANDOM = BmsMeta.single("#random", BmsType.INTEGER, "0", 1, false);
	/**
	 * #IF
	 * <table><caption>&nbsp;</caption>
	 * <tr><th style="text-align:left;">構成単位</th><td>単体</td></tr>
	 * <tr><th style="text-align:left;">データ型</th><td>INTEGER</td></tr>
	 * <tr><th style="text-align:left;">初期値</th><td>0</td></tr>
	 * <tr><th style="text-align:left;">同一性チェック</th><td>NO</td></tr>
	 * <tr><th style="text-align:left;">ライブラリ対応</th><td>YES</td></tr>
	 * <tr><th style="text-align:left;">説明</th>
	 * <td>直前に生成した乱数の値と指定値を評価します。値がマッチした場合、次に#ELSEIF, #ELSE, #ENDIFが登場するまでの間の定義を有効にします。</td></tr>
	 * </table>
	 */
	public static final BmsMeta IF = BmsMeta.single("#if", BmsType.INTEGER, "0", 1, false);
	/**
	 * #ELSEIF
	 * <table><caption>&nbsp;</caption>
	 * <tr><th style="text-align:left;">構成単位</th><td>単体</td></tr>
	 * <tr><th style="text-align:left;">データ型</th><td>INTEGER</td></tr>
	 * <tr><th style="text-align:left;">初期値</th><td>0</td></tr>
	 * <tr><th style="text-align:left;">同一性チェック</th><td>NO</td></tr>
	 * <tr><th style="text-align:left;">ライブラリ対応</th><td>YES</td></tr>
	 * <tr><th style="text-align:left;">説明</th>
	 * <td>直前に生成した乱数の値と指定値を評価します。値がマッチした場合、次に#ELSEIF, #ELSE, #ENDIFが登場するまでの間の
	 * 定義を有効にします。このメタ情報は#IFの後で使用しなければなりません。</td></tr>
	 * </table>
	 */
	public static final BmsMeta ELSEIF = BmsMeta.single("#elseif", BmsType.INTEGER, "0", 1, false);
	/**
	 * #ELSE
	 * <table><caption>&nbsp;</caption>
	 * <tr><th style="text-align:left;">構成単位</th><td>単体</td></tr>
	 * <tr><th style="text-align:left;">データ型</th><td>STRING</td></tr>
	 * <tr><th style="text-align:left;">初期値</th><td>""</td></tr>
	 * <tr><th style="text-align:left;">同一性チェック</th><td>NO</td></tr>
	 * <tr><th style="text-align:left;">ライブラリ対応</th><td>YES</td></tr>
	 * <tr><th style="text-align:left;">説明</th>
	 * <td>乱数の値が#IF, #ELSEIFの指定値のいずれにも該当しない場合に、#ENDIFが登場するまでの間の定義を有効にします。</td></tr>
	 * </table>
	 */
	public static final BmsMeta ELSE = BmsMeta.single("#else", BmsType.STRING, "", 1, false);
	/**
	 * #ENDIF
	 * <table><caption>&nbsp;</caption>
	 * <tr><th style="text-align:left;">構成単位</th><td>単体</td></tr>
	 * <tr><th style="text-align:left;">データ型</th><td>STRING</td></tr>
	 * <tr><th style="text-align:left;">初期値</th><td>""</td></tr>
	 * <tr><th style="text-align:left;">同一性チェック</th><td>NO</td></tr>
	 * <tr><th style="text-align:left;">ライブラリ対応</th><td>YES</td></tr>
	 * <tr><th style="text-align:left;">説明</th>
	 * <td>IFブロックの終了を表します。</td></tr>
	 * </table>
	 */
	public static final BmsMeta ENDIF = BmsMeta.single("#endif", BmsType.STRING, "", 1, false);
	/**
	 * #ENDRANDOM
	 * <table><caption>&nbsp;</caption>
	 * <tr><th style="text-align:left;">構成単位</th><td>単体</td></tr>
	 * <tr><th style="text-align:left;">データ型</th><td>STRING</td></tr>
	 * <tr><th style="text-align:left;">初期値</th><td>""</td></tr>
	 * <tr><th style="text-align:left;">同一性チェック</th><td>NO</td></tr>
	 * <tr><th style="text-align:left;">ライブラリ対応</th><td>YES</td></tr>
	 * <tr><th style="text-align:left;">説明</th>
	 * <td>直前の乱数終了を表します。この定義はBMSライブラリではBMSコンテンツ読み込みエラー回避のためだけに存在し、
	 * 定義しても何の作用もありません。ただし、#IFブロック内で使用するとエラーになります。</td></tr>
	 * </table>
	 */
	public static final BmsMeta ENDRANDOM = BmsMeta.single("#endrandom", BmsType.STRING, "", 1, false);
	/**
	 * #WAV
	 * <table><caption>&nbsp;</caption>
	 * <tr><th style="text-align:left;">構成単位</th><td>索引付き</td></tr>
	 * <tr><th style="text-align:left;">データ型</th><td>STRING</td></tr>
	 * <tr><th style="text-align:left;">初期値</th><td>""</td></tr>
	 * <tr><th style="text-align:left;">同一性チェック</th><td>NO</td></tr>
	 * <tr><th style="text-align:left;">ライブラリ対応</th><td>YES</td></tr>
	 * <tr><th style="text-align:left;">説明</th>
	 * <td>音声ファイルを表します。</td></tr>
	 * </table>
	 */
	public static final BmsMeta WAV = BmsMeta.indexed("#wav", BmsType.STRING, "", 0, false);
	/**
	 * #EXWAV
	 * <table><caption>&nbsp;</caption>
	 * <tr><th style="text-align:left;">構成単位</th><td>索引付き</td></tr>
	 * <tr><th style="text-align:left;">データ型</th><td>STRING</td></tr>
	 * <tr><th style="text-align:left;">初期値</th><td>""</td></tr>
	 * <tr><th style="text-align:left;">同一性チェック</th><td>NO</td></tr>
	 * <tr><th style="text-align:left;">ライブラリ対応</th><td>NO</td></tr>
	 * <tr><th style="text-align:left;">説明</th>
	 * <td>音量・周波数・左右バランスのエフェクトを適用した音声ファイルを表します。</td></tr>
	 * </table>
	 */
	public static final BmsMeta EXWAV = BmsMeta.indexed("#exwav", BmsType.STRING, "", 1, false);
	/**
	 * #WAVCMD
	 * <table><caption>&nbsp;</caption>
	 * <tr><th style="text-align:left;">構成単位</th><td>複数</td></tr>
	 * <tr><th style="text-align:left;">データ型</th><td>STRING</td></tr>
	 * <tr><th style="text-align:left;">初期値</th><td>""</td></tr>
	 * <tr><th style="text-align:left;">同一性チェック</th><td>NO</td></tr>
	 * <tr><th style="text-align:left;">ライブラリ対応</th><td>NO</td></tr>
	 * <tr><th style="text-align:left;">説明</th>
	 * <td>#WAVを再生する時のピッチ、ボリューム、オーディオ再生時間を表します。</td></tr>
	 * </table>
	 */
	public static final BmsMeta WAVCMD = BmsMeta.multiple("#wavcmd", BmsType.STRING, "", 1, false);
	/**
	 * #BMP
	 * <table><caption>&nbsp;</caption>
	 * <tr><th style="text-align:left;">構成単位</th><td>索引付き</td></tr>
	 * <tr><th style="text-align:left;">データ型</th><td>STRING</td></tr>
	 * <tr><th style="text-align:left;">初期値</th><td>""</td></tr>
	 * <tr><th style="text-align:left;">同一性チェック</th><td>NO</td></tr>
	 * <tr><th style="text-align:left;">ライブラリ対応</th><td>YES</td></tr>
	 * <tr><th style="text-align:left;">説明</th>
	 * <td>BGAで使用する画像・動画ファイルを表します。</td></tr>
	 * </table>
	 */
	public static final BmsMeta BMP = BmsMeta.indexed("#bmp", BmsType.STRING, "", 0, false);
	/**
	 * #EXBMP
	 * <table><caption>&nbsp;</caption>
	 * <tr><th style="text-align:left;">構成単位</th><td>索引付き</td></tr>
	 * <tr><th style="text-align:left;">データ型</th><td>STRING</td></tr>
	 * <tr><th style="text-align:left;">初期値</th><td>""</td></tr>
	 * <tr><th style="text-align:left;">同一性チェック</th><td>NO</td></tr>
	 * <tr><th style="text-align:left;">ライブラリ対応</th><td>NO</td></tr>
	 * <tr><th style="text-align:left;">説明</th>
	 * <td>透明色を調整した画像ファイルを表します。</td></tr>
	 * </table>
	 */
	public static final BmsMeta EXBMP = BmsMeta.indexed("#exbmp", BmsType.STRING, "", 1, false);
	/**
	 * #BGA
	 * <table><caption>&nbsp;</caption>
	 * <tr><th style="text-align:left;">構成単位</th><td>索引付き</td></tr>
	 * <tr><th style="text-align:left;">データ型</th><td>STRING</td></tr>
	 * <tr><th style="text-align:left;">初期値</th><td>""</td></tr>
	 * <tr><th style="text-align:left;">同一性チェック</th><td>NO</td></tr>
	 * <tr><th style="text-align:left;">ライブラリ対応</th><td>NO</td></tr>
	 * <tr><th style="text-align:left;">説明</th>
	 * <td>画像の一部をトリムして表示する定義を表します。</td></tr>
	 * </table>
	 */
	public static final BmsMeta BGA = BmsMeta.indexed("#bga", BmsType.STRING, "", 1, false);
	/**
	 * #SWBGA
	 * <table><caption>&nbsp;</caption>
	 * <tr><th style="text-align:left;">構成単位</th><td>索引付き</td></tr>
	 * <tr><th style="text-align:left;">データ型</th><td>STRING</td></tr>
	 * <tr><th style="text-align:left;">初期値</th><td>""</td></tr>
	 * <tr><th style="text-align:left;">同一性チェック</th><td>NO</td></tr>
	 * <tr><th style="text-align:left;">ライブラリ対応</th><td>NO</td></tr>
	 * <tr><th style="text-align:left;">説明</th>
	 * <td>BGAスイッチングの定義を表します。</td></tr>
	 * </table>
	 */
	public static final BmsMeta SWBGA = BmsMeta.indexed("#swbga", BmsType.STRING, "", 1, false);
	/**
	 * #ARGB
	 * <table><caption>&nbsp;</caption>
	 * <tr><th style="text-align:left;">構成単位</th><td>索引付き</td></tr>
	 * <tr><th style="text-align:left;">データ型</th><td>STRING</td></tr>
	 * <tr><th style="text-align:left;">初期値</th><td>""</td></tr>
	 * <tr><th style="text-align:left;">同一性チェック</th><td>NO</td></tr>
	 * <tr><th style="text-align:left;">ライブラリ対応</th><td>NO</td></tr>
	 * <tr><th style="text-align:left;">説明</th>
	 * <td>BGAの各層に適用されるべきARGBの定義を表します。</td></tr>
	 * </table>
	 */
	public static final BmsMeta ARGB = BmsMeta.indexed("#argb", BmsType.STRING, "", 1, false);
	/**
	 * #ExtChr
	 * <table><caption>&nbsp;</caption>
	 * <tr><th style="text-align:left;">構成単位</th><td>複数</td></tr>
	 * <tr><th style="text-align:left;">データ型</th><td>STRING</td></tr>
	 * <tr><th style="text-align:left;">初期値</th><td>""</td></tr>
	 * <tr><th style="text-align:left;">同一性チェック</th><td>NO</td></tr>
	 * <tr><th style="text-align:left;">ライブラリ対応</th><td>NO</td></tr>
	 * <tr><th style="text-align:left;">説明</th>
	 * <td>プレイ画面のUIカスタマイズ内容を表します。</td></tr>
	 * </table>
	 */
	public static final BmsMeta EXTCHR = BmsMeta.multiple("#extchr", BmsType.STRING, "", 1, false);
	/**
	 * #BPM
	 * <table><caption>&nbsp;</caption>
	 * <tr><th style="text-align:left;">構成単位</th><td>索引付き</td></tr>
	 * <tr><th style="text-align:left;">データ型</th><td>NUMERIC</td></tr>
	 * <tr><th style="text-align:left;">初期値</th><td>{@link BmsSpec#BPM_DEFAULT}</td></tr>
	 * <tr><th style="text-align:left;">同一性チェック</th><td>YES</td></tr>
	 * <tr><th style="text-align:left;">ライブラリ対応</th><td>YES</td></tr>
	 * <tr><th style="text-align:left;">説明</th>
	 * <td>BPM変更で使用するBPMの値を表します。</td></tr>
	 * </table>
	 */
	public static final BmsMeta BPM = BmsMeta.indexed("#bpm", BmsType.NUMERIC, String.valueOf(BmsSpec.BPM_DEFAULT), 0, true);
	/**
	 * #EXBPM
	 * <table><caption>&nbsp;</caption>
	 * <tr><th style="text-align:left;">構成単位</th><td>索引付き</td></tr>
	 * <tr><th style="text-align:left;">データ型</th><td>NUMERIC</td></tr>
	 * <tr><th style="text-align:left;">初期値</th><td>0</td></tr>
	 * <tr><th style="text-align:left;">同一性チェック</th><td>YES</td></tr>
	 * <tr><th style="text-align:left;">ライブラリ対応</th><td>NO</td></tr>
	 * <tr><th style="text-align:left;">説明</th>
	 * <td>BPM変更で使用するBPMの値を表します。</td></tr>
	 * </table>
	 */
	public static final BmsMeta EXBPM = BmsMeta.indexed("#exbpm", BmsType.NUMERIC, "0.0", 1, true);
	/**
	 * #STOP
	 * <table><caption>&nbsp;</caption>
	 * <tr><th style="text-align:left;">構成単位</th><td>索引付き</td></tr>
	 * <tr><th style="text-align:left;">データ型</th><td>NUMERIC</td></tr>
	 * <tr><th style="text-align:left;">初期値</th><td>{@link BmsSpec#STOP_MIN}</td></tr>
	 * <tr><th style="text-align:left;">同一性チェック</th><td>YES</td></tr>
	 * <tr><th style="text-align:left;">ライブラリ対応</th><td>YES</td></tr>
	 * <tr><th style="text-align:left;">説明</th>
	 * <td>譜面停止で使用する譜面停止時間(刻み数)を表します。</td></tr>
	 * </table>
	 */
	public static final BmsMeta STOP = BmsMeta.indexed("#stop", BmsType.NUMERIC, String.valueOf(BmsSpec.STOP_MIN), 0, true);
	/**
	 * #STP
	 * <table><caption>&nbsp;</caption>
	 * <tr><th style="text-align:left;">構成単位</th><td>複数</td></tr>
	 * <tr><th style="text-align:left;">データ型</th><td>STRING</td></tr>
	 * <tr><th style="text-align:left;">初期値</th><td>""</td></tr>
	 * <tr><th style="text-align:left;">同一性チェック</th><td>YES</td></tr>
	 * <tr><th style="text-align:left;">ライブラリ対応</th><td>NO</td></tr>
	 * <tr><th style="text-align:left;">説明</th>
	 * <td>bemaniaDXタイプの譜面停止シーケンスの定義を表します。</td></tr>
	 * </table>
	 */
	public static final BmsMeta STP = BmsMeta.multiple("#stp", BmsType.STRING, "", 1, true);
	/**
	 * #SCROLL
	 * <table><caption>&nbsp;</caption>
	 * <tr><th style="text-align:left;">構成単位</th><td>索引付き</td></tr>
	 * <tr><th style="text-align:left;">データ型</th><td>NUMERIC</td></tr>
	 * <tr><th style="text-align:left;">初期値</th><td>1</td></tr>
	 * <tr><th style="text-align:left;">同一性チェック</th><td>YES</td></tr>
	 * <tr><th style="text-align:left;">ライブラリ対応</th><td>NO TODO:ライブラリ対応する</td></tr>
	 * <tr><th style="text-align:left;">説明</th>
	 * <td>譜面のスクロール速度(倍率)を表します。</td></tr>
	 * </table>
	 */
	public static final BmsMeta SCROLL = BmsMeta.indexed("#scroll", BmsType.NUMERIC, "1", 0, true);
	/**
	 * #EXRANK
	 * <table><caption>&nbsp;</caption>
	 * <tr><th style="text-align:left;">構成単位</th><td>索引付き</td></tr>
	 * <tr><th style="text-align:left;">データ型</th><td>INTEGER</td></tr>
	 * <tr><th style="text-align:left;">初期値</th><td>100</td></tr>
	 * <tr><th style="text-align:left;">同一性チェック</th><td>YES</td></tr>
	 * <tr><th style="text-align:left;">ライブラリ対応</th><td>NO</td></tr>
	 * <tr><th style="text-align:left;">説明</th>
	 * <td>判定ランク(詳細)の値を表します。</td></tr>
	 * </table>
	 */
	public static final BmsMeta EXRANK = BmsMeta.indexed("#exrank", BmsType.INTEGER, "100", 1, true);
	/**
	 * #OPTION
	 * <table><caption>&nbsp;</caption>
	 * <tr><th style="text-align:left;">構成単位</th><td>複数</td></tr>
	 * <tr><th style="text-align:left;">データ型</th><td>STRING</td></tr>
	 * <tr><th style="text-align:left;">初期値</th><td>""</td></tr>
	 * <tr><th style="text-align:left;">同一性チェック</th><td>YES</td></tr>
	 * <tr><th style="text-align:left;">ライブラリ対応</th><td>NO</td></tr>
	 * <tr><th style="text-align:left;">説明</th>
	 * <td>プレイオプションを表します。</td></tr>
	 * </table>
	 */
	public static final BmsMeta OPTION = BmsMeta.multiple("#option", BmsType.STRING, "", 1, true);
	/**
	 * #CHANGEOPTION
	 * <table><caption>&nbsp;</caption>
	 * <tr><th style="text-align:left;">構成単位</th><td>索引付き</td></tr>
	 * <tr><th style="text-align:left;">データ型</th><td>STRING</td></tr>
	 * <tr><th style="text-align:left;">初期値</th><td>""</td></tr>
	 * <tr><th style="text-align:left;">同一性チェック</th><td>YES</td></tr>
	 * <tr><th style="text-align:left;">ライブラリ対応</th><td>NO</td></tr>
	 * <tr><th style="text-align:left;">説明</th>
	 * <td>プレイオプションの変更を表します。</td></tr>
	 * </table>
	 */
	public static final BmsMeta CHANGEOPTION = BmsMeta.indexed("#changeoption", BmsType.STRING, "", 1, true);
	/**
	 * #SEEK
	 * <table><caption>&nbsp;</caption>
	 * <tr><th style="text-align:left;">構成単位</th><td>索引付き</td></tr>
	 * <tr><th style="text-align:left;">データ型</th><td>INTEGER</td></tr>
	 * <tr><th style="text-align:left;">初期値</th><td>0</td></tr>
	 * <tr><th style="text-align:left;">同一性チェック</th><td>NO</td></tr>
	 * <tr><th style="text-align:left;">ライブラリ対応</th><td>NO</td></tr>
	 * <tr><th style="text-align:left;">説明</th>
	 * <td>動画の再生位置(ミリ秒)を表します。</td></tr>
	 * </table>
	 */
	public static final BmsMeta SEEK = BmsMeta.indexed("#seek", BmsType.INTEGER, "0", 1, false);
	/**
	 * #TEXT
	 * <table><caption>&nbsp;</caption>
	 * <tr><th style="text-align:left;">構成単位</th><td>索引付き</td></tr>
	 * <tr><th style="text-align:left;">データ型</th><td>STRING</td></tr>
	 * <tr><th style="text-align:left;">初期値</th><td>""</td></tr>
	 * <tr><th style="text-align:left;">同一性チェック</th><td>NO</td></tr>
	 * <tr><th style="text-align:left;">ライブラリ対応</th><td>NO</td></tr>
	 * <tr><th style="text-align:left;">説明</th>
	 * <td>任意のタイミングで表示されるテキストの内容を表します。</td></tr>
	 * </table>
	 */
	public static final BmsMeta TEXT = BmsMeta.indexed("#text", BmsType.STRING, "", 1, false);
	/**
	 * #SONG
	 * <table><caption>&nbsp;</caption>
	 * <tr><th style="text-align:left;">構成単位</th><td>索引付き</td></tr>
	 * <tr><th style="text-align:left;">データ型</th><td>STRING</td></tr>
	 * <tr><th style="text-align:left;">初期値</th><td>""</td></tr>
	 * <tr><th style="text-align:left;">同一性チェック</th><td>NO</td></tr>
	 * <tr><th style="text-align:left;">ライブラリ対応</th><td>NO</td></tr>
	 * <tr><th style="text-align:left;">説明</th>
	 * <td>任意のタイミングで表示されるテキストの内容を表します。このメタ情報は旧式のため、#TEXTを使用することが推奨されます。</td></tr>
	 * </table>
	 */
	public static final BmsMeta SONG = BmsMeta.indexed("#song", BmsType.STRING, "", 1, false);

	// マイナーなCONTROL FLOWは非対応とする
//	public static final BmsMeta SETRANDOM = BmsMeta.single("#setrandom", BmsType.INTEGER, "0", 1, false);
//	public static final BmsMeta ENDRANDOM = BmsMeta.single("#endrandom", BmsType.STRING, "", 1, false);
//	public static final BmsMeta SWITCH = BmsMeta.single("#switch", BmsType.INTEGER, "0", 1, false);
//	public static final BmsMeta CASE = BmsMeta.single("#case", BmsType.INTEGER, "1", 1, false);
//	public static final BmsMeta SKIP = BmsMeta.single("#skip", BmsType.STRING, "", 1, false);
//	public static final BmsMeta DEF = BmsMeta.single("#def", BmsType.STRING, "", 1, false);
//	public static final BmsMeta SETSWITCH = BmsMeta.single("#setswitch", BmsType.INTEGER, "0", 1, false);
//	public static final BmsMeta ENDSW = BmsMeta.single("#endsw", BmsType.STRING, "", 1, false);

	/**
	 * #GENREを設定します。
	 * @param content BMSコンテンツ
	 * @param genre #GENREの値
	 * @exception NullPointerException contentがnull
	 * @see #GENRE
	 */
	public static void setGenre(BmsContent content, String genre) {
		content.setSingleMeta(BeMusicMeta.GENRE.getName(), genre);
	}

	/**
	 * #GENREを取得します。
	 * @param content BMSコンテンツ
	 * @return #GENREの値
	 * @exception NullPointerException contentがnull
	 * @see #GENRE
	 */
	public static String getGenre(BmsContent content) {
		return (String)content.getSingleMeta(BeMusicMeta.GENRE.getName());
	}

	/**
	 * #TITLEを設定します。
	 * @param content BMSコンテンツ
	 * @param title #TITLEの値
	 * @exception NullPointerException contentがnull
	 * @see #TITLE
	 */
	public static void setTitle(BmsContent content, String title) {
		content.setSingleMeta(BeMusicMeta.TITLE.getName(), title);
	}

	/**
	 * #TITLEを取得します。
	 * @param content BMSコンテンツ
	 * @return #TITLEの値
	 * @exception NullPointerException contentがnull
	 * @see #TITLE
	 */
	public static String getTitle(BmsContent content) {
		return (String)content.getSingleMeta(BeMusicMeta.TITLE.getName());
	}

	/**
	 * #SUBTITLEを設定します。
	 * @param content BMSコンテンツ
	 * @param subTitle #SUBTITLEの値
	 * @exception NullPointerException contentがnull
	 * @see #SUBTITLE
	 */
	public static void setSubTitle(BmsContent content, String subTitle) {
		content.setSingleMeta(BeMusicMeta.SUBTITLE.getName(), subTitle);
	}

	/**
	 * #SUBTITLEを取得します。
	 * @param content BMSコンテンツ
	 * @return #SUBTITLEの値
	 * @exception NullPointerException contentがnull
	 * @see #SUBTITLE
	 */
	public static String getSubTitle(BmsContent content) {
		return (String)content.getSingleMeta(BeMusicMeta.SUBTITLE.getName());
	}

	/**
	 * #ARTISTを設定します。
	 * @param content BMSコンテンツ
	 * @param artist #ARTISTの値
	 * @exception NullPointerException contentがnull
	 * @see #ARTIST
	 */
	public static void setArtist(BmsContent content, String artist) {
		content.setSingleMeta(BeMusicMeta.ARTIST.getName(), artist);
	}

	/**
	 * #ARTISTを取得します。
	 * @param content BMSコンテンツ
	 * @return #ARTISTの値
	 * @exception NullPointerException contentがnull
	 * @see #ARTIST
	 */
	public static String getArtist(BmsContent content) {
		return (String)content.getSingleMeta(BeMusicMeta.ARTIST.getName());
	}

	/**
	 * #SUBARTISTを設定します。
	 * @param content BMSコンテンツ
	 * @param index インデックス
	 * @param subArtist #SUBARTISTの値
	 * @exception NullPointerException contentがnull
	 * @see #SUBARTIST
	 */
	public static void setSubArtist(BmsContent content, int index, String subArtist) {
		content.setMultipleMeta(BeMusicMeta.SUBARTIST.getName(), index, subArtist);
	}

	/**
	 * #SUBARTISTのリストを取得します。
	 * @param content BMSコンテンツ
	 * @return #SUBARTISTのリスト
	 * @exception NullPointerException contentがnull
	 * @see #SUBARTIST
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static List<String> getSubArtists(BmsContent content) {
		var subArtists = content.getMultipleMetas(BeMusicMeta.SUBARTIST.getName());
		return new ArrayList(subArtists);
	}

	/**
	 * #SUBARTISTを取得します。
	 * @param content BMSコンテンツ
	 * @param index インデックス
	 * @return #SUBARTISTの値
	 * @exception NullPointerException contentがnull
	 * @see #SUBARTIST
	 */
	public static String getSubArtist(BmsContent content, int index) {
		return (String)content.getMultipleMeta(BeMusicMeta.SUBARTIST.getName(), index);
	}

	/**
	 * #PLAYERを設定します。
	 * @param content BMSコンテンツ
	 * @param player #PLAYERの値
	 * @see BeMusicPlayer
	 * @exception NullPointerException contentがnull
	 * @see #PLAYER
	 */
	public static void setPlayer(BmsContent content, BeMusicPlayer player) {
		content.setSingleMeta(BeMusicMeta.PLAYER.getName(), (player == null) ? null : player.getNativeValue());
	}

	/**
	 * #PLAYERを取得します。
	 * @param content BMSコンテンツ
	 * @return #PLAYERの値
	 * @see BeMusicPlayer
	 * @exception NullPointerException contentがnull
	 * @see #PLAYER
	 */
	public static BeMusicPlayer getPlayer(BmsContent content) {
		return BeMusicPlayer.fromNativeValue(content.getSingleMeta(BeMusicMeta.PLAYER.getName()));
	}

	/**
	 * #RANKを設定します。
	 * @param content BMSコンテンツ
	 * @param rank #RANKの値
	 * @see BeMusicRank
	 * @exception NullPointerException contentがnull
	 * @see #RANK
	 */
	public static void setRank(BmsContent content, BeMusicRank rank) {
		content.setSingleMeta(BeMusicMeta.RANK.getName(), (rank == null) ? null : rank.getNativeValue());
	}

	/**
	 * #RANKを取得します。
	 * @param content BMSコンテンツ
	 * @return #RANKの値
	 * @see BeMusicRank
	 * @exception NullPointerException contentがnull
	 * @see #RANK
	 */
	public static BeMusicRank getRank(BmsContent content) {
		return BeMusicRank.fromNativeValue(content.getSingleMeta(BeMusicMeta.RANK.getName()));
	}

	/**
	 * #TOTALを設定します。
	 * @param content BMSコンテンツ
	 * @param total #TOTALの値
	 * @exception NullPointerException contentがnull
	 * @see #TOTAL
	 */
	public static void setTotal(BmsContent content, Double total) {
		content.setSingleMeta(BeMusicMeta.TOTAL.getName(), total);
	}

	/**
	 * #TOTALを取得します。
	 * @param content BMSコンテンツ
	 * @return #TOTALの値
	 * @exception NullPointerException contentがnull
	 * @see #TOTAL
	 */
	public static double getTotal(BmsContent content) {
		return (double)content.getSingleMeta(BeMusicMeta.TOTAL.getName());
	}

	/**
	 * #STAGEFILEを設定します。
	 * @param content BMSコンテンツ
	 * @param stageFile #STAGEFILEの値
	 * @exception NullPointerException contentがnull
	 * @see #STAGEFILE
	 */
	public static void setStageFile(BmsContent content, String stageFile) {
		content.setSingleMeta(BeMusicMeta.STAGEFILE.getName(), stageFile);
	}

	/**
	 * #STAGEFILEを取得します。
	 * @param content BMSコンテンツ
	 * @return #STAGEFILEの値
	 * @exception NullPointerException contentがnull
	 * @see #STAGEFILE
	 */
	public static String getStageFile(BmsContent content) {
		return (String)content.getSingleMeta(BeMusicMeta.STAGEFILE.getName());
	}

	/**
	 * #BANNERを設定します。
	 * @param content BMSコンテンツ
	 * @param banner #BANNERの値
	 * @exception NullPointerException contentがnull
	 * @see #BANNER
	 */
	public static void setBanner(BmsContent content, String banner) {
		content.setSingleMeta(BeMusicMeta.BANNER.getName(), banner);
	}

	/**
	 * #BANNERを取得します。
	 * @param content BMSコンテンツ
	 * @return #BANNERの値
	 * @exception NullPointerException contentがnull
	 * @see #BANNER
	 */
	public static String getBanner(BmsContent content) {
		return (String)content.getSingleMeta(BeMusicMeta.BANNER.getName());
	}

	/**
	 * #BACKBMPを設定します。
	 * @param content BMSコンテンツ
	 * @param backBmp #BACKBMPの値
	 * @exception NullPointerException contentがnull
	 * @see #BACKBMP
	 */
	public static void setBackBmp(BmsContent content, String backBmp) {
		content.setSingleMeta(BeMusicMeta.BACKBMP.getName(), backBmp);
	}

	/**
	 * #BACKBMPを取得します。
	 * @param content BMSコンテンツ
	 * @return #BACKBMPの値
	 * @exception NullPointerException contentがnull
	 * @see #BACKBMP
	 */
	public static String getBackBmp(BmsContent content) {
		return (String)content.getSingleMeta(BeMusicMeta.BACKBMP.getName());
	}

	/**
	 * #PREVIEWを設定します。
	 * @param content BMSコンテンツ
	 * @param preview #PREVIEWの値
	 * @exception NullPointerException contentがnull
	 * @see #PREVIEW
	 */
	public static void setPreview(BmsContent content, String preview) {
		content.setSingleMeta(BeMusicMeta.PREVIEW.getName(), preview);
	}

	/**
	 * #PREVIEWを取得します。
	 * @param content BMSコンテンツ
	 * @return #PREVIEWの値
	 * @exception NullPointerException contentがnull
	 * @see #PREVIEW
	 */
	public static String getPreview(BmsContent content) {
		return content.getSingleMeta(BeMusicMeta.PREVIEW.getName());
	}

	/**
	 * #PLAYLEVELを設定します。
	 * @param content BMSコンテンツ
	 * @param playLevel #PLAYLEVELの値
	 * @exception NullPointerException contentがnull
	 * @see #PLAYLEVEL
	 */
	public static void setPlayLevel(BmsContent content, Double playLevel) {
		content.setSingleMeta(BeMusicMeta.PLAYLEVEL.getName(), playLevel);
	}

	/**
	 * #PLAYLEVELを取得します。
	 * @param content BMSコンテンツ
	 * @return #PLAYLEVELの値
	 * @exception NullPointerException contentがnull
	 * @see #PLAYLEVEL
	 */
	public static String getPlayLevelRaw(BmsContent content) {
		return content.getSingleMeta(BeMusicMeta.PLAYLEVEL.getName());
	}

	/**
	 * #PLAYLEVELの数値を取得します。
	 * <p>元の値が数値書式でない場合、取得される値は0になります。</p>
	 * @param content BMSコンテンツ
	 * @return #PLAYLEVELを数値に変換した値
	 */
	public static double getPlayLevel(BmsContent content) {
		var playLevel = (String)content.getSingleMeta(BeMusicMeta.PLAYLEVEL.getName());
		return BmsType.NUMERIC.test(playLevel) ? Double.parseDouble(playLevel) : 0.0;
	}

	/**
	 * #DIFFICULTYを設定します。
	 * @param content BMSコンテンツ
	 * @param difficulty #DIFFICULTYの値
	 * @see BeMusicDifficulty
	 * @exception NullPointerException contentがnull
	 * @see #DIFFICULTY
	 */
	public static void setDifficulty(BmsContent content, BeMusicDifficulty difficulty) {
		content.setSingleMeta(BeMusicMeta.DIFFICULTY.getName(), (difficulty == null) ? null : difficulty.getNativeValue());
	}

	/**
	 * #DIFFICULTYを取得します。
	 * @param content BMSコンテンツ
	 * @return #DIFFICULTYの値
	 * @exception NullPointerException contentがnull
	 * @see #DIFFICULTY
	 */
	public static BeMusicDifficulty getDifficulty(BmsContent content) {
		return BeMusicDifficulty.fromNativeValue(content.getSingleMeta(BeMusicMeta.DIFFICULTY.getName()));
	}

	/**
	 * #LNOBJを設定します。
	 * @param content BMSコンテンツ
	 * @param index インデックス
	 * @param lnObj #LNOBJの値
	 * @exception NullPointerException contentがnull
	 * @exception IndexOutOfBoundsException indexが0～BmsSpec.MULTIPLE_META_INDEX_MAXの範囲外
	 * @see #LNOBJ
	 */
	public static void setLnObj(BmsContent content, int index, Long lnObj) {
		content.setMultipleMeta(BeMusicMeta.LNOBJ.getName(), index, lnObj);
	}

	/**
	 * #LNOBJのリストを取得します。
	 * @param content BMSコンテンツ
	 * @return #LNOBJのリスト
	 * @exception NullPointerException contentがnull
	 * @see #LNOBJ
	 */
	public static List<Long> getLnObjs(BmsContent content) {
		return content.getMultipleMetas(BeMusicMeta.LNOBJ.getName());
	}

	/**
	 * #LNOBJを取得します。
	 * @param content BMSコンテンツ
	 * @param index インデックス
	 * @return #LNOBJの値
	 * @exception NullPointerException contentがnull
	 * @exception IndexOutOfBoundsException indexが0～BmsSpec.MULTIPLE_META_INDEX_MAXの範囲外
	 * @see #LNOBJ
	 */
	public static long getLnObj(BmsContent content, int index) {
		return (long)content.getMultipleMeta(BeMusicMeta.LNOBJ.getName(), index);
	}

	/**
	 * #LNMODEを設定します。
	 * @param content BMSコンテンツ
	 * @param lnMode #LNMODEの値
	 * @exception NullPointerException contentがnull
	 * @see #LNMODE
	 */
	public static void setLnMode(BmsContent content, BeMusicLongNoteMode lnMode) {
		content.setSingleMeta(BeMusicMeta.LNMODE.getName(), (lnMode == null) ? null : lnMode.getNativeValue());
	}

	/**
	 * #LNMODEを取得します。
	 * @param content BMSコンテンツ
	 * @return #LNMODEの値
	 * @exception NullPointerException contentがnull
	 * @see #LNMODE
	 */
	public static BeMusicLongNoteMode getLnMode(BmsContent content) {
		return BeMusicLongNoteMode.fromNative((long)content.getSingleMeta(BeMusicMeta.LNMODE.getName()));
	}

	/**
	 * %URLを設定します。
	 * @param content BMSコンテンツ
	 * @param url %URLの値
	 * @exception NullPointerException contentがnull
	 * @see #URL
	 */
	public static void setUrl(BmsContent content, String url) {
		content.setSingleMeta(BeMusicMeta.URL.getName(), url);
	}

	/**
	 * %URLを取得します。
	 * @param content BMSコンテンツ
	 * @return %URLの値
	 * @exception NullPointerException contentがnull
	 * @see #URL
	 */
	public static String getUrl(BmsContent content) {
		return (String)content.getSingleMeta(BeMusicMeta.URL.getName());
	}

	/**
	 * %EMAILを設定します。
	 * @param content BMSコンテンツ
	 * @param email %EMAILの値
	 * @exception NullPointerException contentがnull
	 * @see #EMAIL
	 */
	public static void setEmail(BmsContent content, String email) {
		content.setSingleMeta(BeMusicMeta.EMAIL.getName(), email);
	}

	/**
	 * %EMAILを取得します。
	 * @param content BMSコンテンツ
	 * @return %EMAILの値
	 * @exception NullPointerException contentがnull
	 * @see #EMAIL
	 */
	public static String getEmail(BmsContent content) {
		return (String)content.getSingleMeta(BeMusicMeta.EMAIL.getName());
	}

	/**
	 * #BPMxxを設定します。
	 * @param content BMSコンテンツ
	 * @param index インデックス
	 * @param bpm #BPMxxの値
	 * @exception NullPointerException contentがnull
	 * @exception IndexOutOfBoundsException indexが0～BmsSpec.INDEXED_META_INDEX_MAXの範囲外
	 * @see #BPM
	 */
	public static void setBpm(BmsContent content, int index, Double bpm) {
		content.setIndexedMeta(BeMusicMeta.BPM.getName(), index, bpm);
	}

	/**
	 * #BPMxxを取得します。
	 * @param content BMSコンテンツ
	 * @param index インデックス
	 * @return #BPMxxの値
	 * @exception NullPointerException contentがnull
	 * @exception IndexOutOfBoundsException indexが0～BmsSpec.INDEXED_META_INDEX_MAXの範囲外
	 * @see #BPM
	 */
	public static double getBpm(BmsContent content, int index) {
		return content.getIndexedMeta(BeMusicMeta.BPM.getName(), index);
	}

	/**
	 * #BPMxxのリストを取得します。
	 * @param content BMSコンテンツ
	 * @return #BPMxxのリスト
	 * @exception NullPointerException contentがnull
	 * @see #BPM
	 */
	public static Map<Integer, Double> getBpms(BmsContent content) {
		return content.getIndexedMetas(BeMusicMeta.BPM.getName());
	}

	/**
	 * #STOPxxを設定します。
	 * @param content BMSコンテンツ
	 * @param index インデックス
	 * @param stop #STOPの値
	 * @exception NullPointerException contentがnull
	 * @exception IndexOutOfBoundsException indexが0～BmsSpec.INDEXED_META_INDEX_MAXの範囲外
	 * @see #STOP
	 */
	public static void setStop(BmsContent content, int index, Double stop) {
		content.setIndexedMeta(BeMusicMeta.STOP.getName(), index, stop);
	}

	/**
	 * #STOPxxを取得します。
	 * @param content BMSコンテンツ
	 * @param index インデックス
	 * @return #STOPの値
	 * @exception NullPointerException contentがnull
	 * @exception IndexOutOfBoundsException indexが0～BmsSpec.INDEXED_META_INDEX_MAXの範囲外
	 * @see #STOP
	 */
	public static double getStop(BmsContent content, int index) {
		return content.getIndexedMeta(BeMusicMeta.STOP.getName(), index);
	}

	/**
	 * #STOPxxのリストを取得します。
	 * @param content BMSコンテンツ
	 * @return #STOPxxのリスト
	 * @exception NullPointerException contentがnull
	 * @see #STOP
	 */
	public static Map<Integer, Double> getStops(BmsContent content) {
		return content.getIndexedMetas(BeMusicMeta.STOP.getName());
	}

	/**
	 * #WAVxxを設定します。
	 * @param content BMSコンテンツ
	 * @param index インデックス
	 * @param wav #WAVの値
	 * @exception NullPointerException contentがnull
	 * @exception IndexOutOfBoundsException indexが0～BmsSpec.INDEXED_META_INDEX_MAXの範囲外
	 * @see #WAV
	 */
	public static void setWav(BmsContent content, int index, String wav) {
		content.setIndexedMeta(BeMusicMeta.WAV.getName(), index, wav);
	}

	/**
	 * #WAVxxを取得します。
	 * @param content BMSコンテンツ
	 * @param index インデックス
	 * @return #WAVの値
	 * @exception NullPointerException contentがnull
	 * @exception IndexOutOfBoundsException indexが0～BmsSpec.INDEXED_META_INDEX_MAXの範囲外
	 * @see #WAV
	 */
	public static String getWav(BmsContent content, int index) {
		return content.getIndexedMeta(BeMusicMeta.WAV.getName(), index);
	}

	/**
	 * #WAVxxのリストを取得します。
	 * @param content BMSコンテンツ
	 * @return #WAVxxのリスト
	 * @exception NullPointerException contentがnull
	 * @see #WAV
	 */
	public static Map<Integer, String> getWavs(BmsContent content) {
		return content.getIndexedMetas(BeMusicMeta.WAV.getName());
	}

	/**
	 * #BMPxxを設定します。
	 * @param content BMSコンテンツ
	 * @param index インデックス
	 * @param bmp #BMPxxの値
	 * @exception NullPointerException contentがnull
	 * @exception IndexOutOfBoundsException indexが0～BmsSpec.INDEXED_META_INDEX_MAXの範囲外
	 * @see #BMP
	 */
	public static void setBmp(BmsContent content, int index, String bmp) {
		content.setIndexedMeta(BeMusicMeta.BMP.getName(), index, bmp);
	}

	/**
	 * #BMPxxを取得します。
	 * @param content BMSコンテンツ
	 * @param index インデックス
	 * @return #BMPxxの値
	 * @exception NullPointerException contentがnull
	 * @see #BMP
	 */
	public static String getBmp(BmsContent content, int index) {
		return content.getIndexedMeta(BeMusicMeta.BMP.getName(), index);
	}

	/**
	 * #BMPxxのリストを取得します。
	 * @param content BMSコンテンツ
	 * @return #BMPxxのリスト
	 * @exception NullPointerException contentがnull
	 * @see #BMP
	 */
	public static Map<Integer, String> getBmps(BmsContent content) {
		return content.getIndexedMetas(BeMusicMeta.BMP.getName());
	}

	/**
	 * #TEXTxxを設定します。
	 * @param content BMSコンテンツ
	 * @param index インデックス
	 * @param text #TEXTxxの値
	 * @exception NullPointerException contentがnull
	 * @exception IndexOutOfBoundsException indexが0～BmsSpec.INDEXED_META_INDEX_MAXの範囲外
	 * @see #TEXT
	 */
	public static void setText(BmsContent content, int index, String text) {
		content.setIndexedMeta(BeMusicMeta.TEXT.getName(), index, text);
	}

	/**
	 * #TEXTxxを取得します。
	 * @param content BMSコンテンツ
	 * @param index インデックス
	 * @return #TEXTxxの値
	 * @exception NullPointerException contentがnull
	 * @see #TEXT
	 */
	public static String getText(BmsContent content, int index) {
		return content.getIndexedMeta(BeMusicMeta.TEXT.getName(), index);
	}

	/**
	 * #TEXTxxのリストを取得します。
	 * @param content BMSコンテンツ
	 * @return #TEXTxxのリスト
	 * @exception NullPointerException contentがnull
	 * @see #TEXT
	 */
	public static Map<Integer, String> getTexts(BmsContent content) {
		return content.getIndexedMetas(BeMusicMeta.TEXT.getName());
	}
}
