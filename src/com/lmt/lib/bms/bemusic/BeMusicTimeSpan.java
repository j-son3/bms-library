package com.lmt.lib.bms.bemusic;

/**
 * 時間範囲内の譜面統計情報を表します。
 *
 * <p>譜面統計情報を集計するにあたり、譜面を一定時間ごとに分割し、その時間の範囲で譜面がどのような構成になっているかを
 * その期間ごとに集計します。その時間の範囲内の統計情報のことを「期間」または「期間統計情報」と呼称します。期間統計情報は
 * {@link BeMusicStatistics}によって演奏時間分の範囲で集計が行われ、時系列のリストで管理されます。</p>
 *
 * <p>当クラスは時間範囲での統計情報、および譜面統計情報と統計情報生成元譜面とのリンクを可能にする様々な情報を
 * 保持しており、必要に応じてそれらの情報を参照できるようになっています。</p>
 *
 * <p>また、当クラスのインスタンスは{@link BeMusicStatisticsBuilder}によって譜面統計情報が集計される時にのみ
 * 生成されることを想定しているため、BMSライブラリユーザーが独自でインスタンスを生成することは考慮されていません。</p>
 *
 * @see BeMusicStatistics
 * @since 0.1.0
 */
public class BeMusicTimeSpan {
	/** 期間統計情報の最小長(秒単位) */
	public static final double MIN_SPAN = 0.1;
	/** 期間統計情報の最大長(秒単位) */
	public static final double MAX_SPAN = 3.0;
	/** 期間統計情報の長さ推奨値(秒単位) */
	public static final double RECOMMENDED_SPAN = 0.7;

	/** この期間統計情報の前の期間統計情報 */
	private BeMusicTimeSpan mPrevious;
	/** この期間統計情報の後の期間統計情報 */
	private BeMusicTimeSpan mNext;
	/** この期間統計情報のリストインデックス */
	private int mIndex;
	/** 期間開始時間 */
	private double mBeginTime;
	/** 期間終了時間(この時間を含まない) */
	private double mEndTime;
	/** 期間統計情報の元になった譜面の楽曲位置情報最初のインデックス */
	private int mFirstIndex;
	/** 期間統計情報の元になった譜面の楽曲位置情報最後のインデックス */
	private int mLastIndex;
	/** 注視点(左スクラッチ、またはダブルプレー) */
	private double mGazePoint;
	/** 注視点(右スクラッチ) */
	private double mGazePointR;
	/** 注視点変動係数(左スクラッチ、またはダブルプレー) */
	private double mGazeSwingley;
	/** 注視点変動係数(右スクラッチ) */
	private double mGazeSwingleyR;
	/** 視野幅(左スクラッチ、またはダブルプレー) */
	private double mViewWidth;
	/** 視野幅(右スクラッチ) */
	private double mViewWidthR;
	/** 視野幅変動係数(左スクラッチ、またはダブルプレー) */
	private double mViewSwingley;
	/** 視野幅変動係数(右スクラッチ) */
	private double mViewSwingleyR;
	/** 総ノート数 */
	private int mNoteCount;
	/** ロングノート数 */
	private int mLnCount;
	/** 地雷数 */
	private int mMineCount;

	/**
	 * コンストラクタ
	 */
	BeMusicTimeSpan() {
		// Do nothing
	}

	/**
	 * 1つ前の期間統計情報を取得します。
	 * <p>この期間が先頭である場合はこの期間の参照を返します。従って、当メソッドでnullが返ることはありません。</p>
	 * @return 1つ前の期間統計情報
	 */
	public final BeMusicTimeSpan getPrevious() {
		return mPrevious;
	}

	/**
	 * 1つ前の期間統計情報設定
	 * @param previous 1つ前の期間統計情報
	 */
	void setPrevious(BeMusicTimeSpan previous) {
		mPrevious = previous;
	}

	/**
	 * 1つ後の期間統計情報を取得します。
	 * <p>この期間が末尾である場合はこの期間の参照を返します。従って、当メソッドでnullが返ることはありません。</p>
	 * @return 1つ後の期間統計情報
	 */
	public final BeMusicTimeSpan getNext() {
		return mNext;
	}

	/**
	 * 1つ後の期間統計情報設定
	 * @param next 1つ後の期間統計情報
	 */
	void setNext(BeMusicTimeSpan next) {
		mNext = next;
	}

	/**
	 * リスト上のインデックス値を取得します。
	 * <p>当メソッドで取得できるインデックス値は、この期間のオーナーである譜面統計情報({@link BeMusicStatistics})
	 * が内部で保有するリストのインデックス値となります。</p>
	 * @return リスト上のインデックス値
	 */
	public final int getIndex() {
		return mIndex;
	}

	/**
	 * リスト上のインデックス値設定
	 * @param index リスト上のインデックス値
	 */
	void setIndex(int index) {
		mIndex = index;
	}

	/**
	 * 開始時間を秒単位で取得します。
	 * @return この期間の開始時間
	 */
	public final double getBeginTime() {
		return mBeginTime;
	}

	/**
	 * 開始時間設定
	 * @param time この期間の開始時間
	 */
	void setBeginTime(double time) {
		mBeginTime = time;
	}

	/**
	 * 終了時間を秒単位で取得します。
	 * <p>この値は{@link #getBeginTime()}に{@link BeMusicStatisticsBuilder#setSpanLength(double)}で設定した
	 * 期間の長さを加算した値になります。</p>
	 * <p>この期間には終了時間の値に該当する時間の楽曲位置情報は含まれません。</p>
	 * @return 終了時間
	 */
	public final double getEndTime() {
		return mEndTime;
	}

	/**
	 * 終了時間設定
	 * @param time この期間の終了時間
	 */
	void setEndTime(double time) {
		mEndTime = time;
	}

	/**
	 * 統計情報の元になった譜面でこの期間に含まれる楽曲位置情報の最初のインデックス値を取得します。
	 * <p>当メソッドで取得できる値は、統計情報の元になった{@link BeMusicChart}オブジェクトから楽曲位置情報を
	 * 取得する際に使用することができます。この期間内に楽曲位置情報が1件も含まれない場合は負の値を返します。</p>
	 * @return 楽曲位置情報の最初のインデックス値
	 */
	public final int getFirstPointIndex() {
		return mFirstIndex;
	}

	/**
	 * 楽曲位置情報の最初のインデックス値設定
	 * @param index 楽曲位置情報の最初のインデックス値
	 */
	void setFirstPointIndex(int index) {
		mFirstIndex = index;
	}

	/**
	 * 統計情報の元になった譜面でこの期間に含まれる楽曲位置情報の最後のインデックス値を取得します。
	 * <p>当メソッドで取得できる値は、統計情報の元になった{@link BeMusicChart}オブジェクトから楽曲位置情報を
	 * 取得する際に使用することができます。この期間内に楽曲位置情報が1件も含まれない場合は負の値を返します。</p>
	 * @return 楽曲位置情報の最後のインデックス値
	 */
	public final int getLastPointIndex() {
		return mLastIndex;
	}

	/**
	 * 楽曲位置情報の最後のインデックス値設定
	 * @param index 楽曲位置情報の最後のインデックス値
	 */
	void setLastPointIndex(int index) {
		mLastIndex = index;
	}

	/**
	 * 注視点を取得します。
	 * <p>注視点とは、期間内に存在する視覚効果のある左端と右端のノートの中心点を表す数値です。
	 * レーンの左端を-1、中央を0、右端を1と定義した値を示します。期間内に視覚効果のあるノートが存在しない場合、
	 * 注視点は0を示します。</p>
	 * <p>期間内で算出する注視点は、当該期間内に存在する全ノートからおおよその点を割り出します。
	 * 算出アルゴリズムはBMSライブラリのアップデートにより改良される場合があり、その関係で同じ配置のノートでも
	 * 異なる値が報告される可能性があることに留意してください。</p>
	 * <p>当メソッドで取得できる値はスクラッチがスイッチの左側、またはダブルプレーの注視点の値です。
	 * ダブルプレーの場合は左側レーンの左端を-1、右側レーンの右端を1とする値を返します。</p>
	 * @return 注視点
	 */
	public final double getGazePoint() {
		return mGazePoint;
	}

	/**
	 * 注視点を取得します。
	 * <p>注視点とは、期間内に存在する視覚効果のある左端と右端のノートの中心点を表す数値です。
	 * レーンの左端を-1、中央を0、右端を1と定義した値を示します。期間内に視覚効果のあるノートが存在しない場合、
	 * 注視点は0を示します。</p>
	 * <p>当メソッドで取得できる値はスクラッチがスイッチの右側にある場合の注視点の値です。</p>
	 * @return 注視点
	 */
	public final double getGazePointR() {
		return mGazePointR;
	}

	/**
	 * 注視点設定
	 * @param gazeL 注視点(左スクラッチ、またはダブルプレー)
	 * @param gazeR 注視点(右スクラッチ)
	 */
	void setGazePoint(double gazeL, double gazeR) {
		mGazePoint = gazeL;
		mGazePointR = gazeR;
	}

	/**
	 * 注視点変動係数を取得します。
	 * <p>注視点変動係数とは、前後期間からの注視点の移動量を数値化した値で0～1の範囲を示します。
	 * 0は注視点が全く移動せず、1は注視点が水平方向に最大限振れることを表します。</p>
	 * <p>当メソッドで取得できる値はスクラッチがスイッチの左側、またはダブルプレーの注視点変動係数です。</p>
	 * @return 注視点変動係数
	 */
	public final double getGazeSwingley() {
		return mGazeSwingley;
	}

	/**
	 * 注視点変動係数を取得します。
	 * <p>注視点変動係数とは、前後期間からの注視点の移動量を数値化した値で0～1の範囲を示します。
	 * 0は注視点が全く移動せず、1は注視点が水平方向に最大限振れることを表します。</p>
	 * <p>当メソッドで取得できる値はスクラッチがスイッチの右側にある場合の注視点変動係数です。</p>
	 * @return 注視点変動係数
	 */
	public final double getGazeSwingleyR() {
		return mGazeSwingleyR;
	}

	/**
	 * 注視点変動係数設定
	 * @param swingleyL 注視点変動係数(左スクラッチ、またはダブルプレー)
	 * @param swingleyR 注視点変動係数(右スクラッチ)
	 */
	void setGazeSwingley(double swingleyL, double swingleyR) {
		mGazeSwingley = swingleyL;
		mGazeSwingleyR = swingleyR;
	}

	/**
	 * 視野幅を取得します。
	 * <p>視野幅とは、期間内に存在する視覚効果のある左端と右端のノート間の距離を表す数値です。
	 * レーンの左端と右端に視覚効果のあるノートが存在する場合最大値の1を示し、
	 * 1点の水平位置にのみ視覚効果のあるノートが存在する場合、シングルプレーでは0.07、ダブルプレーは0.03を示します。
	 * ただし、期間内に視覚効果のあるノートが全く存在しない場合は視野幅は0となります。</p>
	 * <p>期間内で算出する視野幅は、当該期間内に存在する全ノートからおおよその幅を割り出します。
	 * 算出アルゴリズムはBMSライブラリのアップデートにより改良される場合があり、その関係で同じ配置のノートでも
	 * 異なる値が報告される可能性があることに留意してください。</p>
	 * <p>当メソッドで取得できる値はスクラッチがスイッチの左側、またはダブルプレーの視野幅の値です。
	 * ダブルプレーの場合最大値1を示すのは左側レーンの左端と右側レーンの右端に視覚効果のあるノートが存在する場合で、
	 * 最小値0.1はシングルプレーと同様の定義となります。</p>
	 * @return 視野幅
	 */
	public final double getViewWidth() {
		return mViewWidth;
	}

	/**
	 * 視野幅を取得します。
	 * <p>視野幅とは、期間内に存在する視覚効果のある左端と右端のノート間の距離を表す数値です。
	 * レーンの左端と右端に視覚効果のあるノートが存在する場合最大値の1を示し、
	 * 1点の水平位置にのみ視覚効果のあるノートが存在する場合、シングルプレーでは0.07、ダブルプレーは0.03を示します。
	 * ただし、期間内に視覚効果のあるノートが全く存在しない場合は視野幅は0となります。</p>
	 * <p>当メソッドで取得できる値はスクラッチがスイッチの右側にある場合の視野幅の値です。</p>
	 * @return 視野幅
	 */
	public final double getViewWidthR() {
		return mViewWidthR;
	}

	/**
	 * 視野幅設定
	 * @param swingleyL 視野幅(左スクラッチ、またはダブルプレー)
	 * @param swingleyR 視野幅(右スクラッチ)
	 */
	void setViewWidth(double widthL, double widthR) {
		mViewWidth = widthL;
		mViewWidthR = widthR;
	}

	/**
	 * 視野幅変動係数を取得します。
	 * <p>視野幅変動係数とは、前後期間からの視野幅の変動量を数値化した値で0～1の範囲を示します。
	 * 0は視野幅が全く変化せず、1は視野幅が最大限振れることを表します。</p>
	 * <p>当メソッドで取得できる値はスクラッチがスイッチの左側、またはダブルプレーの視野幅変動係数です。</p>
	 * @return 視野幅変動係数
	 */
	public final double getViewSwingley() {
		return mViewSwingley;
	}

	/**
	 * 視野幅変動係数を取得します。
	 * <p>視野幅変動係数とは、前後期間からの視野幅の変動量を数値化した値で0～1の範囲を示します。
	 * 0は視野幅が全く変化せず、1は視野幅が最大限振れることを表します。</p>
	 * <p>当メソッドで取得できる値はスクラッチがスイッチの右側にある場合の視野幅変動係数です。</p>
	 * @return 視野幅変動係数
	 */
	public final double getViewSwingleyR() {
		return mViewSwingleyR;
	}

	/**
	 * 視野幅変動係数設定
	 * @param swingleyL 視野幅変動係数(左スクラッチ、またはダブルプレー)
	 * @param swingleyR 視野幅変動係数(右スクラッチ)
	 */
	void setViewSwingley(double swingleyL, double swingleyR) {
		mViewSwingley = swingleyL;
		mViewSwingleyR = swingleyR;
	}

	/**
	 * 総ノート数を取得します。
	 * <p>この値は、期間内の{@link BeMusicNoteType#isCountNotes()}に該当するノートの総数となります。</p>
	 * @return 総ノート数
	 */
	public final int getNoteCount() {
		return mNoteCount;
	}

	/**
	 * 総ノート数設定
	 * @param count 総ノート数
	 */
	void setNoteCount(int count) {
		mNoteCount = count;
	}

	/**
	 * 総ロングノート数を取得します。
	 * <p>この値は、期間内の{@link BeMusicNoteType#LONG_ON}に該当するノートの総数となります。
	 * 長押し継続、終了はカウントの対象とはなりません。</p>
	 * @return 総ロングノート数
	 */
	public final int getLongNoteCount() {
		return mLnCount;
	}

	/**
	 * 総ロングノート数設定
	 * @param count 総ロングノート数
	 */
	void setLongNoteCount(int count) {
		mLnCount = count;
	}

	/**
	 * 地雷オブジェ数を取得します。
	 * @return 地雷オブジェ数
	 */
	public final int getMineCount() {
		return mMineCount;
	}

	/**
	 * 地雷オブジェ数設定
	 * @param count 地雷オブジェ数
	 */
	void setMineCount(int count) {
		mMineCount = count;
	}

	/**
	 * この期間が最初の期間統計情報かどうかを返します。
	 * <p>当メソッドの戻り値がtrueの時、{@link #getIndex()}は0を示します。</p>
	 * @return この期間が最初の期間統計情報であればtrue
	 */
	public final boolean isFirstSpan() {
		return this == mPrevious;
	}

	/**
	 * この期間が最後の期間統計情報かどうかを返します。
	 * @return この期間が最後の期間統計情報であればtrue
	 */
	public final boolean isLastSpan() {
		return this == mNext;
	}

	/**
	 * この期間内に楽曲位置情報が存在するかを返します。
	 * <p>当メソッドがtrueを返す時、{@link #getFirstPointIndex()}および{@link #getLastPointIndex()}
	 * の戻り値は0以上の値を示します。</p>
	 * @return この期間内に楽曲位置情報が存在する場合true
	 */
	public final boolean hasPoint() {
		return mFirstIndex >= 0;
	}

	/**
	 * この期間内にノート数としてカウントされるノートが存在するかを返します。
	 * <p>当メソッドがtrueを返す時、{@link #getNoteCount()}の戻り値は1以上の値を示します。</p>
	 * @return この期間内にノート数としてカウントされるノートが存在する場合true
	 */
	public final boolean hasCountNote() {
		return mNoteCount > 0;
	}

	/**
	 * この期間内に視覚効果のあるノートが存在するかを返します。
	 * <p>当メソッドがtrueを返す時、期間内に{@link BeMusicNoteType#hasVisualEffect()}
	 * がtrueを返すノートが1件以上存在します。</p>
	 * @return この期間内に視覚効果のあるノートが存在する場合true
	 */
	public final boolean hasVisualEffect() {
		return (mViewWidth > 0.0) || (mViewWidthR > 0.0);
	}
}
