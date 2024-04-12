package com.lmt.lib.bms.bemusic;

import static com.lmt.lib.bms.internal.Assertion.*;

import java.util.Arrays;
import java.util.List;

import com.lmt.lib.bms.internal.deltasystem.Ds;

/**
 * 譜面統計情報を表します。
 *
 * <p>当クラスでは、譜面を一定時間ごとに分割し、その時間の範囲(「期間」または「期間統計情報」と呼称します)
 * で譜面がどのような構成になっているかをその期間ごとに集計した情報、およびそれらの期間全体を集計した情報を保有します。
 * その期間1個分の情報は{@link BeMusicTimeSpan}によって表され、当クラス内でそのリストを保有しています。</p>
 *
 * <p>期間統計情報は、ある一点の時間から統計情報構築の際に指定した期間統計情報1件分の長さの集計情報を保有します。
 * 次の期間統計情報は、期間統計情報1件分の長さの半分だけ時間を進め、同様の集計情報を保有します。それを当該譜面の
 * 演奏時間全体を含むようにリスト化されます。具体的には以下の図を参照してください。</p>
 *
 * <pre>
 *           +-------+
 *           |       |
 *           | 期間3 |
 *           | +-----+-+
 * +------+  | |     | |
 * |      |  | |期間2| |
 * |      |  +-+-----+ |
 * |      |  | |     | |
 * | 楽曲 |  | |     | |
 * |      |  | +-----+-+
 * |      |  | 期間1 |
 * |      |  |       |
 * +------+  +-------+
 * </pre>
 *
 * <p>上記のように、期間2は期間1の後半と期間3の前半を含む形で統計情報を集計します。
 * これにより平均値などの統計情報の精度をできるだけ高められるようにすることを意図しています。
 * 当クラスからこれらの期間統計情報を個別に参照できますが、それらの情報にアクセスする際は上で述べたような意図で
 * 統計情報が集計されていることに留意してください。</p>
 *
 * <p>どのような統計情報が集計されるかについては、各統計情報の取得メソッドに記載の解説を参照してください。
 * また、集計される統計情報の内容はBMSライブラリの更新によって追加・更新される場合があります。</p>
 *
 * @see BeMusicStatisticsBuilder
 * @see BeMusicTimeSpan
 */
public class BeMusicStatistics {
	/** 期間統計情報リスト */
	private List<BeMusicTimeSpan> mTimeSpanList;
	/** 期間統計情報1件の長さ */
	private double mSpanLength;
	/** 統計対象ノートレイアウト */
	private BeMusicNoteLayout mLayout;
	/** 平均ノート密度 */
	private double mAvgDensity = 0.0;
	/** 最大ノート密度 */
	private double mMaxDensity = 0.0;
	/** 無操作期間比率 */
	private double mNoPlayingRatio = 1.0;
	/** 視覚効果なし期間比率 */
	private double mNoVisualEffectRatio = 1.0;
	/** 注視点傾向(左スクラッチ/DP) */
	private double mAvgGazePoint = 0.0;
	/** 注視点傾向(右スクラッチ) */
	private double mAvgGazePointR = 0.0;
	/** 平均視野幅(左スクラッチ/DP) */
	private double mAvgViewWidth = 0.0;
	/** 平均視野幅(右スクラッチ) */
	private double mAvgViewWidthR = 0.0;
	/** 注視点変動係数(左スクラッチ/DP) */
	private double mAvgGazeSwingley = 0.0;
	/** 注視点変動係数(右スクラッチ) */
	private double mAvgGazeSwingleyR = 0.0;
	/** 視野幅変動係数(左スクラッチ/DP) */
	private double mAvgViewSwingley = 0.0;
	/** 視野幅変動係数(右スクラッチ */
	private double mAvgViewSwingleyR = 0.0;
	/** Delta Systemのアルゴリズムバージョン */
	private String mDsAlgoVer = null;
	/** Delta Systemのレーティング値一覧 */
	private int[] mRatings = new int[BeMusicRatingType.COUNT];
	/** 譜面主傾向 */
	private BeMusicRatingType mPrimaryTendency = null;
	/** 譜面副次傾向 */
	private BeMusicRatingType mSecondaryTendency = null;

	/**
	 * コンストラクタ
	 */
	BeMusicStatistics() {
		Arrays.fill(mRatings, -1);
	}

	/**
	 * 期間統計情報の数を取得します。
	 * @return 期間統計情報の数
	 */
	public final int getSpanCount() {
		return mTimeSpanList.size();
	}

	/**
	 * 指定された位置の期間統計情報を取得します。
	 * @param index インデックス
	 * @return 期間統計情報
	 * @exception IndexOutOfBoundsException indexが負の値または{@link #getSpanCount()}以上
	 */
	public final BeMusicTimeSpan getSpan(int index) {
		return mTimeSpanList.get(index);
	}

	/**
	 * 期間統計情報リスト設定
	 * @param list 期間統計情報リスト
	 */
	final void setTimeSpanList(List<BeMusicTimeSpan> list) {
		mTimeSpanList = list;
	}

	/**
	 * 期間統計情報1件の長さを秒単位で取得します。
	 * <p>この値は{@link BeMusicStatisticsBuilder#setSpanLength(double)}で指定した値と等価になります。</p>
	 * @return 統計情報1件の長さ
	 */
	public final double getSpanLength() {
		return mSpanLength;
	}

	/**
	 * 期間統計情報1件の長さ設定
	 * @param length 期間統計情報1件の長さ
	 */
	final void setSpanLength(double length) {
		mSpanLength = length;
	}

	/**
	 * この統計情報の対象となったノートレイアウトを取得します。
	 * <p>このオブジェクトが示す統計情報は、当メソッドで取得できるノートレイアウトで計算したものとなります。
	 * 具体的には{@link BeMusicStatisticsBuilder#setNoteLayout(BeMusicNoteLayout)}で指定したレイアウトと等価になります。</p>
	 * @return この統計情報の対象となったノートレイアウト
	 */
	public final BeMusicNoteLayout getNoteLayout() {
		return mLayout;
	}

	/**
	 * この統計情報の対象となったノートレイアウト設定
	 * @param layout この統計情報の対象となったノートレイアウト
	 */
	final void setNoteLayout(BeMusicNoteLayout layout) {
		mLayout = layout;
	}

	/**
	 * 平均ノート密度(Notes/sec)を取得します。
	 * <p>平均ノート密度は、操作可能ノートが存在する期間の総ノート数({@link BeMusicTimeSpan#getNoteCount()})から
	 * 1秒間のノート密度を計算した値の平均値になります。ノートの存在しない期間は平均ノート密度の計算対象外になる点に
	 * 注意してください。</p>
	 * <p>この値は期間統計情報1件の長さの設定({@link BeMusicStatisticsBuilder#setSpanLength(double)})により値が変動します。
	 * 長さが短すぎても長すぎても精度の高い値は得られません。長さ0.5～1秒程度が最も現実的な精度が得られる目安としてください。</p>
	 * @return 平均ノート密度(Notes/sec)
	 */
	public final double getAverageDensity() {
		return mAvgDensity;
	}

	/**
	 * 平均ノート密度設定
	 * @param density 平均ノート密度
	 */
	final void setAverageDensity(double density) {
		mAvgDensity = density;
	}

	/**
	 * 最大ノート密度(Notes/sec)を取得します。
	 * <p>最大ノート密度は、全期間のうち総ノート数({@link BeMusicTimeSpan#getNoteCount()})が最も多い値から1秒間の
	 * ノート密度を計算した値と等価になります。</p>
	 * <p>この値は期間統計情報1件の長さの設定({@link BeMusicStatisticsBuilder#setSpanLength(double)})により値が変動します。
	 * 長さが短すぎても長すぎても精度の高い値は得られません。長さ0.5～1秒程度が最も現実的な精度が得られる目安としてください。</p>
	 * @return 最大ノート密度
	 */
	public final double getMaxDensity() {
		return mMaxDensity;
	}

	/**
	 * 最大ノート密度設定
	 * @param density 最大ノート密度
	 */
	final void setMaxDensity(double density) {
		mMaxDensity = density;
	}

	/**
	 * 無操作期間の比率を取得します。
	 * <p>値の範囲は0～1の間になります。従って、ノート数としてカウントされるノートが全く存在しない場合は1、
	 * 全期間で1件以上のノート数としてカウントされるノートが存在する場合は0を示します。</p>
	 * @return 無操作期間の比率
	 */
	public final double getNoPlayingRatio() {
		return mNoPlayingRatio;
	}

	/**
	 * 無操作期間の比率設定
	 * @param ratio 無操作期間の比率
	 */
	final void setNoPlayingRatio(double ratio) {
		mNoPlayingRatio = ratio;
	}

	/**
	 * 視覚効果のない期間の比率を取得します。
	 * <p>値の範囲は0～1の間になります。従って、視覚効果が全くない場合は1、全期間で何らかの視覚効果がある場合は0を示します。</p>
	 * <p>この値は操作可能ノートが存在しない期間({@link #getNoPlayingRatio()})と似ていますが、操作可能でない、つまり
	 * ノート数としてカウントされないノートが存在する場合も視覚効果ありと認識されます。例えば、ある期間にロングノートの
	 * 操作継続中({@link BeMusicNoteType#LONG})しか存在しない場合などがこれに該当します。</p>
	 * @return 視覚効果のない期間の比率
	 */
	public final double getNoVisualEffectRatio() {
		return mNoVisualEffectRatio;
	}

	/**
	 * 視覚効果のない期間の比率設定
	 * @param ratio 視覚効果のない期間の比率
	 */
	final void setNoVisualEffectRatio(double ratio) {
		mNoVisualEffectRatio = ratio;
	}

	/**
	 * 注視点傾向の値を取得します。
	 * <p>当メソッドで取得できる値はスクラッチがスイッチの左側、またはダブルプレーの注視点傾向の値です。
	 * この値は視覚効果のある期間の注視点({@link BeMusicTimeSpan#getGazePoint()})の平均値になります。
	 * 全期間を通しての注視点の平均値となるため、取得できる値はあくまで当該譜面における平均的な注視点の目安と考えてください。
	 * 多くの一般的な譜面では、注視点は時間の経過と共に左右に大きく振れます。</p>
	 * @return 注視点傾向の値
	 */
	public final double getAverageGazePoint() {
		return mAvgGazePoint;
	}

	/**
	 * 注視点傾向の値を取得します。
	 * <p>当メソッドで取得できる値はスクラッチがスイッチの右側ある場合の値です。
	 * この値は視覚効果のある期間の注視点({@link BeMusicTimeSpan#getGazePointR()})の平均値になります。
	 * 全期間を通しての注視点の平均値となるため、取得できる値はあくまで当該譜面における平均的な注視点の目安と考えてください。
	 * 多くの一般的な譜面では、注視点は時間の経過と共に左右に大きく振れます。</p>
	 * @return 注視点傾向の値
	 */
	public final double getAverageGazePointR() {
		return mAvgGazePointR;
	}

	/**
	 * 注視点傾向の値設定
	 * @param gazeL 注視点(スクラッチ左側、またはダブルプレー
	 * @param gazeR 注視点(スクラッチ右側)
	 */
	final void setAverageGazePoint(double gazeL, double gazeR) {
		mAvgGazePoint = gazeL;
		mAvgGazePointR = gazeR;
	}

	/**
	 * 平均視野幅を取得します。
	 * <p>当メソッドで取得できる値はスクラッチがスイッチの左側、またはダブルプレーの平均視野幅です。
	 * この値は視覚効果のある期間のの視野幅({@link BeMusicTimeSpan#getViewWidth()})の平均値になります。</p>
	 * @return 平均視野幅
	 */
	public final double getAverageViewWidth() {
		return mAvgViewWidth;
	}

	/**
	 * 平均視野幅を取得します。
	 * <p>当メソッドで取得できる値はスクラッチがスイッチの右側にある場合の平均視野幅です。
	 * この値は視覚効果のある期間のの視野幅({@link BeMusicTimeSpan#getViewWidthR()})の平均値になります。</p>
	 * @return 平均視野幅
	 */
	public final double getAverageViewWidthR() {
		return mAvgViewWidthR;
	}

	/**
	 * 平均視野幅設定
	 * @param widthL 平均視野幅(スクラッチ左側、またはダブルプレー)
	 * @param widthR 平均視野幅(スクラッチ右側)
	 */
	final void setAverageViewWidth(double widthL, double widthR) {
		mAvgViewWidth = widthL;
		mAvgViewWidthR = widthR;
	}

	/**
	 * 注視点変動係数の平均値を取得します。
	 * <p>注視点変動係数とは、前後期間からの注視点の移動量を数値化した値で0～1の範囲を示します。
	 * 0は注視点が全く移動せず、1は注視点が水平方向に最大限振れることを表します。</p>
	 * <p>当メソッドで取得できる値はスクラッチがスイッチの左側、またはダブルプレーの注視点変動係数です。
	 * この値は視覚効果のある期間の注視点変動係数({@link BeMusicTimeSpan#getGazeSwingley()})の平均値になります。</p>
	 * @return 注視点変動係数の平均値
	 */
	public final double getAverageGazeSwingley() {
		return mAvgGazeSwingley;
	}

	/**
	 * 注視点変動係数の平均値を取得します。
	 * <p>注視点変動係数とは、前後期間からの注視点の移動量を数値化した値で0～1の範囲を示します。
	 * 0は注視点が全く移動せず、1は注視点が水平方向に最大限振れることを表します。</p>
	 * <p>当メソッドで取得できる値はスクラッチがスイッチの右側にある場合の注視点変動係数です。
	 * この値は視覚効果のある期間の注視点変動係数({@link BeMusicTimeSpan#getGazeSwingleyR()})の平均値になります。</p>
	 * @return 注視点変動係数の平均値
	 */
	public final double getAverageGazeSwingleyR() {
		return mAvgGazeSwingleyR;
	}

	/**
	 * 注視点変動係数設定
	 * @param swingleyL 注視点変動係数(スクラッチ左側、またはダブルプレー)
	 * @param swingleyR 注視点変動係数(スクラッチ右側)
	 */
	final void setAverageGazeSwingley(double swingleyL, double swingleyR) {
		mAvgGazeSwingley = swingleyL;
		mAvgGazeSwingleyR = swingleyR;
	}

	/**
	 * 視野幅変動係数の平均値を取得します。
	 * <p>視野幅変動係数とは、前後期間からの視野幅の変動量を数値化した値で0～1の範囲を示します。
	 * 0は視野幅が全く変化せず、1は視野幅が最大限振れることを表します。</p>
	 * <p>当メソッドで取得できる値はスクラッチがスイッチの左側、またはダブルプレーの視野幅変動係数です。
	 * この値は視覚効果のある期間の視野幅変動係数({@link BeMusicTimeSpan#getViewSwingley()})の平均値になります。</p>
	 * @return 視野幅変動係数の平均値
	 */
	public final double getAverageViewSwingley() {
		return mAvgViewSwingley;
	}

	/**
	 * 視野幅変動係数の平均値を取得します。
	 * <p>視野幅変動係数とは、前後期間からの視野幅の変動量を数値化した値で0～1の範囲を示します。
	 * 0は視野幅が全く変化せず、1は視野幅が最大限振れることを表します。</p>
	 * <p>当メソッドで取得できる値はスクラッチがスイッチの右側にある場合の視野幅変動係数です。
	 * この値は視覚効果のある期間の視野幅変動係数({@link BeMusicTimeSpan#getViewSwingleyR()})の平均値になります。</p>
	 * @return 視野幅変動係数の平均値
	 */
	public final double getAverageViewSwingleyR() {
		return mAvgViewSwingleyR;
	}

	/**
	 * 視野幅変動係数設定
	 * @param swingleyL 視野幅変動係数(スクラッチ左側、またはダブルプレー)
	 * @param swingleyR 視野幅変動係数(スクラッチ右側)
	 */
	final void setAverageViewSwingley(double swingleyL, double swingleyR) {
		mAvgViewSwingley = swingleyL;
		mAvgViewSwingleyR = swingleyR;
	}

	/**
	 * Delta Systemのアルゴリズムバージョンを取得します。
	 * <p>当メソッドは、この譜面統計情報オブジェクトが保持するレーティング値を算出した段階でのDelta Systemの
	 * アルゴリズムバージョンを取得します。アルゴリズムバージョンの書式については{@link #getDeltaSystemVersion()}
	 * を参照してください。</p>
	 * <p>どのレーティング値も算出しなかった場合、当メソッドはnullを返します。譜面統計情報オブジェクトがレーティング値を
	 * 算出しているかどうかが定かではない場合には必ずnullチェックを行うようにしてください。</p>
	 * @return Delta Systemのアルゴリズムバージョン
	 */
	public final String getRatingAlgorithmVersion() {
		return mDsAlgoVer;
	}

	/**
	 * Delta Systemのアルゴリズムバージョン設定
	 * @param version Delta Systemのアルゴリズムバージョン
	 */
	final void setRatingAlgorithmVersion(String version) {
		mDsAlgoVer = version;
	}

	/**
	 * Delta Systemによって算出されたレーティング値を取得します。
	 * <p>レーティング値の算出はオプション機能です。レーティング値を算出するには
	 * {@link BeMusicStatisticsBuilder#addRating(BeMusicRatingType...)}により算出したいレーティング種別を追加して
	 * 譜面統計情報の集計を行う必要があります。算出対象外のレーティング値を指定すると負の値を返します。</p>
	 * <p>Delta Systemのレーティング値は整数値で返されますが、実際にユーザーにプレゼンテーションする際には
	 * 返されたレーティング値を100で除算し小数点第2位までを示すことを想定しています。プレゼンテーション用に
	 * 文字列を生成する際は{@link BeMusicRatingType#toString(int)}を使用してください。</p>
	 * @param ratingType レーティング種別
	 * @return Delta Systemによって算出されたレーティング値
	 * @exception NullPointerException ratingTypeがnull
	 */
	public final int getRating(BeMusicRatingType ratingType) {
		assertArgNotNull(ratingType, "ratingType");
		var rating = mRatings[ratingType.getIndex()];
		if (ratingType.isUnknown(rating)) {
			return -1;
		} else {
			var max = ratingType.getMax();
			return Math.min(max, Math.max(0, rating));
		}
	}

	/**
	 * レーティング値設定
	 * @param ratingType レーティング種別
	 * @param rating レーティング値
	 */
	final void setRating(BeMusicRatingType ratingType, int rating) {
		mRatings[ratingType.getIndex()] = rating;
	}

	/**
	 * Delta Systemのレーティング値の中で最も値の大きいレーティング種別を取得します。
	 * <p>当メソッドが返すレーティング種別は、当該譜面の「主傾向」を表します。</p>
	 * <p>主傾向は、全てのレーティング値を算出対象として譜面統計情報を集計した場合に最も有効な状態となります。
	 * 算出対象外となったレーティング種別が最も大きいレーティング値になる可能性があるためです。</p>
	 * <p>どのレーティング値も算出しなかった場合、当メソッドはnullを返します。譜面統計情報オブジェクトがレーティング値を
	 * 算出しているかどうかが定かではない場合には必ずnullチェックを行うようにしてください。</p>
	 * <p>当メソッドが扱うレーティング種別には{@link BeMusicRatingType#DELTA}は含まれないことに注意してください。</p>
	 * @return レーティング値が最も大きいレーティング種別
	 */
	public final BeMusicRatingType getPrimaryTendency() {
		return mPrimaryTendency;
	}

	/**
	 * 譜面主傾向設定
	 * @param ratingType レーティング種別
	 */
	final void setPrimaryTendency(BeMusicRatingType ratingType) {
		mPrimaryTendency = ratingType;
	}

	/**
	 * Delta Systemのレーティング値の中で2番目に値の大きいレーティング種別を取得します。
	 * <p>当メソッドが返すレーティング種別は、当該譜面の「副次傾向」を表します。</p>
	 * <p>主傾向と同様に、全てのレーティング値を算出対象として譜面統計情報を集計した場合に最も有効な状態となります。
	 * また、レーティング値を1つしか算出しなかった場合、そのレーティング値の種別を返します。
	 * つまり、副次傾向は主傾向と同じ値を示すことになります。</p>
	 * <p>どのレーティング値も算出しなかった場合、当メソッドはnullを返します。譜面統計情報オブジェクトがレーティング値を
	 * 算出しているかどうかが定かではない場合には必ずnullチェックを行うようにしてください。</p>
	 * <p>当メソッドが扱うレーティング種別には{@link BeMusicRatingType#DELTA}は含まれないことに注意してください。</p>
	 * @return レーティング値が2番目に大きいレーティング種別
	 */
	public final BeMusicRatingType getSecondaryTendency() {
		return mSecondaryTendency;
	}

	/**
	 * 譜面副次傾向設定
	 * @param ratingType レーティング種別
	 */
	final void setSecondaryTendency(BeMusicRatingType ratingType) {
		mSecondaryTendency = ratingType;
	}

	/**
	 * Delta Systemのアルゴリズムバージョンを取得します。
	 * <p>Delta SystemはBMSライブラリ本体とは別のバージョンを保有しています。BMSライブラリのアップデートによって
	 * Delta Systemが必ずしも更新されるとは限らないためです。</p>
	 * <p>また、アルゴリズムバージョンはDelta Systemのレーティング値分析アルゴリズムがどのような状態であるかを
	 * 確認するのに役立ちます。アルゴリズムバージョンの見かたについては以下を参照してください。</p>
	 * <p><strong>書式</strong><br>
	 * [メジャーバージョン].[リビジョン番号]-[アルゴリズム状態][パラメータ変更状態]</p>
	 * <p><strong>メジャーバージョン</strong><br>
	 * Delta Systemそのもののバージョンを表します。この値が増加すると分析アルゴリズムの内容が1から見直され、
	 * 分析方法が根本的に変化することを示します。レーティング値は楽曲ごとに大幅に変動する場合があり、
	 * 今までの基準が全て新しいものに更新されると考えて差し支えありません(基本的には分析結果がより良く改善されます)。</p>
	 * <p><strong>リビジョン番号</strong><br>
	 * 同一メジャーバージョンの分析アルゴリズムにおいて、パラメータの調整・不具合の修正・分析内容の僅かな見直しを行い、
	 * 分析結果の改善を行う度に値が増加します。この値が変わると特定の特徴を有する楽曲のレーティング値のみが大幅に変動するか、
	 * 全体的に僅かに変動(大抵は1～2%前後)する場合があります。分析アルゴリズムの基本的な処理内容に変更はないため
	 * レーティング値の全体的な推移が大幅に変動することはありません(リビジョン番号の更新ではそのような変更は行いません)。</p>
	 * <p><strong>アルゴリズム状態</strong><br>
	 * 分析アルゴリズムが現在どのような位置付けであるかを端的に表す1文字で、'D','R','F'の3種類があります。<br>
	 * 'D' (Draft) は分析アルゴリズムのベースは完成しているものの、重大な欠陥が潜んでいる確率が高めであることを表します。
	 * 「重大な欠陥」とは具体的に、特定の特徴を有する楽曲で想定外に高い、または低いレーティング値を出力したり、
	 * 低難易度の譜面で恣意的にレーティング値を吊り上げようとする悪意のある楽曲への耐性が欠落していたり等が挙げられます。
	 * 分析アルゴリズムの品質が担保できていない段階では、開発者の判断により状態 'D' を付与します。<br>
	 * 'R' (Reviewing) は 'D' で挙げたような重大な欠陥を認知できる限り改善し、レーティング値のユーザーレビューを
	 * 実施している段階であることを表します。この段階では開発者側としての調整は完了しており、ユーザーからの報告・意見等が
	 * ある可能性を踏まえ、報告・意見等をレーティング値に反映する余地を残した状態となります。この状態以降はレーティング値の
	 * 大幅な変動は予定されませんが、一般的な譜面には含まれないような配置が多数使用されるような個性的な譜面ではやや大きめの
	 * 変動が生じる可能性があります。<br>
	 * 'F' (Fixed) はユーザーレビューも完了し、当該メジャーバージョンでのレーティング値が確定した状態を表します。
	 * これ以降のアップデートでは、メジャーバージョンの更新が行われない限りレーティング値が変動することはありません。
	 * ただし、Delta Systemの不具合修正等で動作改善が行われ、制作者の意図する譜面を正確に読み込めるようになると
	 * 値が変動する可能性はあります。通常、そのような不具合は状態 'R' の段階までに修正されます。</p>
	 * <p><strong>パラメータ変更状態</strong><br>
	 * Delta Systemは、開発者向けにソースコードを変更することなくレーティングパラメータを調整する機能が備わっています
	 * (その機能は非公開となっています)。開発者はレーティング結果の精度向上のためにレーティングパラメータを調整することがあり、
	 * 調整を行った状態でレーティングを行うと、その事実が分かるようアルゴリズムの末尾に 'C' の文字を付加します。
	 * この文字が付加されたアルゴリズムバージョンで得られたレーティング値に信頼性はありません。</p>
	 * <p><strong>表記例</strong><br>
	 * 1.15-R<br>
	 * 上記の例ではアルゴリズムバージョン 1 が 15回更新され、ユーザーレビュー中であることを示します。</p>
	 * @return Delta Systemのアルゴリズムバージョン
	 */
	public static String getDeltaSystemVersion() {
		var ver = String.format("%d.%d-%c%s",
				Ds.ALGORITHM_MAJOR_VERSION,
				Ds.ALGORITHM_REVISION_NUMBER,
				Ds.ALGORITHM_STATUS_CHAR,
				Ds.isConfigChanged() ? Character.toString(Ds.ALGORITHM_CONFIG_CHANGED) : "");
		return ver;
	}
}
