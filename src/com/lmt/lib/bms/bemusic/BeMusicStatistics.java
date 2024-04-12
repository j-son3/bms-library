package com.lmt.lib.bms.bemusic;

import java.util.List;

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
	// ↓TimeSpanとは関係ない統計情報
	/*
	private double mPower; // 地力
	private double mMaximum; // 最大密度
	private double mBeatTech;  // 打鍵難易度
	private double mLongTech;  // 長押し難易度
	private double mScrTech;  // スクラッチ難易度
	private double mGimmic;  // ギミック
	private double mDelta;  // Δ難易度
	*/

	/**
	 * コンストラクタ
	 */
	BeMusicStatistics() {
		// Do nothing
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
}
