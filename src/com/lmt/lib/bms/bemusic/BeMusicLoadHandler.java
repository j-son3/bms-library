package com.lmt.lib.bms.bemusic;

import java.util.Random;

import com.lmt.lib.bms.BmsChannel;
import com.lmt.lib.bms.BmsContent;
import com.lmt.lib.bms.BmsLoadHandler;
import com.lmt.lib.bms.BmsLoaderSettings;
import com.lmt.lib.bms.BmsMeta;
import com.lmt.lib.bms.BmsMetaKey;

/**
 * Be-Music用BMSコンテンツ読み込み時のハンドラです。
 *
 * <p>当ハンドラでは、Be-Music特有の仕様をBMSコンテンツのローダーに付与します。
 * 具体的には「CONTROL FLOW機能」を提供します。これは「#RANDOM」による乱数の生成と「#IF」等によるコンテンツ読み込みの
 * ランダム化を実現するものとなっており、CONTROL FLOWを記述したBe-Music用BMSコンテンツでは読み込み毎に異なるデータを
 * 読み込むことが可能となるものです。</p>
 */
public class BeMusicLoadHandler implements BmsLoadHandler {
	/** CONTROL FLOWのコマンド */
	private enum ControlFlow {
		/** #RANDOM */
		RANDOM(BeMusicMeta.RANDOM),
		/** #IF */
		IF(BeMusicMeta.IF),
		/** #ELSEIF */
		ELSEIF(BeMusicMeta.ELSEIF),
		/** #ELSE */
		ELSE(BeMusicMeta.ELSE),
		/** #ENDIF */
		ENDIF(BeMusicMeta.ENDIF);

		/** メタ情報名称 */
		private BmsMetaKey mMetaKey;

		/**
		 * コンストラクタ
		 * @param metaName メタ情報名称
		 */
		private ControlFlow(BmsMetaKey metaKey) {
			mMetaKey = metaKey;
		}

		/**
		 * メタ情報名称からCONTROL FLOWを取得
		 * @param metaName メタ情報名称
		 * @return CONTROL FLOW
		 */
		static ControlFlow fromMeta(BmsMetaKey metaKey) {
			for (var cf : values()) { if (cf.mMetaKey.equals(metaKey)) { return cf; } }
			return null;
		}
	}

	/** CONTROL FLOW定義状態 */
	private enum RandomDefineStatus {
		/** 初期状態 */
		NEUTRAL(true, true, false, false, false),
		/** #RANDOM宣言 */
		RANDOM(true, true, false, false, false),
		/** #IFブロック */
		IF(false, false, true, true, true),
		/** #ELSEIFブロック */
		ELSEIF(false, false, true, true, true),
		/** #ELSEブロック */
		ELSE(false, false, false, false, true);

		/** #RANDOM有効状態 */
		private boolean mEnableRandom;
		/** #IF有効状態 */
		private boolean mEnableIf;
		/** #ELSEIF有効状態 */
		private boolean mEnableElseIf;
		/** #ELSE有効状態 */
		private boolean mEnableElse;
		/** #ENDIF有効状態 */
		private boolean mEnableEndIf;

		/**
		 * コンストラクタ
		 * @param enRan #RANDOM有効状態
		 * @param enIf #IF有効状態
		 * @param enElif #ELSEIF有効状態
		 * @param enElse #ELSE有効状態
		 * @param enEnd #ENDIF有効状態
		 */
		private RandomDefineStatus(boolean enRan, boolean enIf, boolean enElif, boolean enElse, boolean enEnd) {
			mEnableRandom = enRan;
			mEnableIf = enIf;
			mEnableElseIf = enElif;
			mEnableElse = enElse;
			mEnableEndIf = enEnd;
		}

		/**
		 * CONTROL FLOW有効状態取得
		 * @param flow CONTROL FLOW
		 * @return 有効であればtrue
		 */
		boolean isEnableControlFlow(ControlFlow flow) {
			switch (flow) {
			case RANDOM: return mEnableRandom;
			case IF: return mEnableIf;
			case ELSEIF: return mEnableElseIf;
			case ELSE: return mEnableElse;
			case ENDIF: return mEnableEndIf;
			default: return false;
			}
		}
	}

	/** CONTROL FLOW真偽状態 */
	private enum RandomBooleanStatus {
		/** 初期状態 */
		NEUTRAL,
		/** 偽(真未検出) */
		FALSE_MOVEABLE,
		/** 真 */
		TRUE,
		/** 偽 */
		FALSE;
	}

	/** CONTROL FLOW有効状態 */
	private boolean mRandomEnable = false;
	/** 乱数指定値 */
	private Long mRandomValueForce = null;
	/** 現在の乱数値 */
	private long mRandomValue = 0L;
	/** CONTROL FLOW定義状態 */
	private RandomDefineStatus mRandomDefineStatus = RandomDefineStatus.NEUTRAL;
	/** CONTROL FLOW真偽状態 */
	private RandomBooleanStatus mRandomBooleanStatus = RandomBooleanStatus.NEUTRAL;
	/** 乱数オブジェクト */
	private Random mRandom = new Random();
	/** 検査結果 */
	private TestResult mCurrentTestResult = TestResult.OK;

	/**
	 * CONTROL FLOWの有効状態を設定します。
	 * <p>有効にすると、#RANDOMによる乱数生成と#IF～#ELSEIF～#ELSE～#ENDIFによるフロー制御が可能になります。
	 * 無効にするとこれらのフロー制御は非対応になり、解析エラーになります。</p>
	 * <p>デフォルトではCONTROL FLOWは無効に設定されています。</p>
	 * @param isEnable CONTROL FLOW有効状態
	 * @return このオブジェクトのインスタンス
	 */
	public final BeMusicLoadHandler setEnableControlFlow(boolean isEnable) {
		mRandomEnable = isEnable;
		return this;
	}

	/**
	 * CONTROL FLOWにおける乱数生成の値を強制します。
	 * <p>1以上の値を指定すると#RANDOMの実行結果が必ず指定値となります。</p>
	 * <p>0を指定すると全ての#IF, #ELSEIFが「真」を示さなくなり、必ず#ELSEブロックが処理されるようになります。</p>
	 * <p>マイナス値またはnullを指定すると値の強制はOFFになり、ランダムな値が生成されるようになります。</p>
	 * <p>デフォルトでは乱数値の強制はOFFになっています。</p>
	 * <p>この値はCONTROL FLOWが有効になっている場合のみ使用されます。無効の場合、BMS読み込みに対しての作用はありません。</p>
	 * @param value 強制する乱数の値
	 * @return このオブジェクトのインスタンス
	 */
	public final BeMusicLoadHandler setForceRandomValue(Long value) {
		mRandomValueForce = value;
		return this;
	}

	/** {@inheritDoc} */
	@Override
	public void startLoad(BmsLoaderSettings spec) {
		initializeControlFlow(0L, new Random());
		return;
	}

	// Be-MusicではBMS宣言をサポートしない
//	@Override
//	public TestResult testDeclaration(String key, String value) {
//		return TestResult.OK;
//	}

	/** {@inheritDoc} */
	@Override
	public TestResult testMeta(BmsMeta meta, int index, Object value) {
		processControlFlow(meta, value);
		return mCurrentTestResult;
	}

	/** {@inheritDoc} */
	@Override
	public TestResult testChannel(BmsChannel channel, int index, int measure, Object value) {
		return mCurrentTestResult;
	}

	/** {@inheritDoc} */
	@Override
	public TestResult testContent(BmsContent content) {
		// 読み込まれたコンテンツの受け入れ可否を決定する
		// CONTROL FLOWの定義が不完全な場合にはコンテンツを破棄するように指示する
		if ((mRandomDefineStatus != RandomDefineStatus.NEUTRAL) && (mRandomDefineStatus != RandomDefineStatus.RANDOM)) {
			var msg = "#IF block is NOT finished";
			return TestResult.fail(msg);
		}

		// CONTROL FLOWのメタ情報はコンテンツから取り除く
		content.beginEdit();
		content.setSingleMeta(BeMusicMeta.RANDOM.getName(), null);
		content.setSingleMeta(BeMusicMeta.IF.getName(), null);
		content.setSingleMeta(BeMusicMeta.ELSEIF.getName(), null);
		content.setSingleMeta(BeMusicMeta.ELSE.getName(), null);
		content.setSingleMeta(BeMusicMeta.ENDIF.getName(), null);
		content.endEdit();

		// CONTROL FLOWで使用したデータを初期値に戻す
		initializeControlFlow(mRandomValue, null);

		return TestResult.OK;
	}

	/**
	 * CONTROL FLOW初期化
	 * @param random Randomオブジェクト
	 */
	private void initializeControlFlow(long randomValue, Random random) {
		mRandomValue = randomValue;
		mRandomDefineStatus = RandomDefineStatus.NEUTRAL;
		mRandomBooleanStatus = RandomBooleanStatus.NEUTRAL;
		mRandom = random;
		mCurrentTestResult = TestResult.OK;
	}

	/**
	 * CONTROL FLOW制御を行う
	 * @param meta メタ情報
	 * @param value 解析後の値
	 */
	private void processControlFlow(BmsMeta meta, Object value) {
		// RANDOMが無効にされている場合は無視する
		if (!mRandomEnable) {
			return;
		}

		// CONTROL FLOWの種類を特定する
		var ctrlFlow = ControlFlow.fromMeta(meta);
		if (ctrlFlow == null) {
			return;
		}

		// 現在の定義状態で定義可能なCONTROL FLOWかをチェックする
		if (!mRandomDefineStatus.isEnableControlFlow(ctrlFlow)) {
			mCurrentTestResult = TestResult.fail("Wrong control flow");
			return;
		}

		// 定義状態と真偽状態の遷移を行う
		switch (ctrlFlow) {
		case RANDOM: {  // #RANDOM
			mRandomValue = generateRandomValue((Long)value);
			mRandomDefineStatus = RandomDefineStatus.RANDOM;
			mRandomBooleanStatus = RandomBooleanStatus.NEUTRAL;
			mCurrentTestResult = TestResult.OK;
			break;
		}
		case IF: {      // #IF
			var isTrue = (mRandomValue == (long)value);
			mRandomDefineStatus = RandomDefineStatus.IF;
			mRandomBooleanStatus = isTrue ? RandomBooleanStatus.TRUE : RandomBooleanStatus.FALSE_MOVEABLE;
			mCurrentTestResult = isTrue ? TestResult.OK : TestResult.THROUGH;
			break;
		}
		case ELSEIF: {  // #ELSEIF
			var isTrue = (mRandomValue == (long)value) && (mRandomBooleanStatus == RandomBooleanStatus.FALSE_MOVEABLE);
			mRandomDefineStatus = RandomDefineStatus.ELSEIF;
			mRandomBooleanStatus = isTrue ? RandomBooleanStatus.TRUE : RandomBooleanStatus.FALSE;
			mCurrentTestResult = isTrue ? TestResult.OK : TestResult.THROUGH;
			break;
		}
		case ELSE: {    // #ELSE
			var isTrue = (mRandomBooleanStatus != RandomBooleanStatus.TRUE);
			mRandomDefineStatus = RandomDefineStatus.ELSE;
			mRandomBooleanStatus = isTrue ? RandomBooleanStatus.TRUE : RandomBooleanStatus.FALSE;
			mCurrentTestResult = isTrue ? TestResult.OK : TestResult.THROUGH;
			break;
		}
		case ENDIF: {   // #ENDIF
			mRandomDefineStatus = RandomDefineStatus.NEUTRAL;
			mRandomBooleanStatus = RandomBooleanStatus.NEUTRAL;
			mCurrentTestResult = TestResult.OK;
			break;
		}
		default:        // Don't care
			break;
		}
	}

	/**
	 * 乱数値の生成
	 * @param value #RANDOMに指定の値
	 * @return 生成された乱数値
	 */
	private long generateRandomValue(long value) {
		if ((mRandomValueForce != null) && (mRandomValueForce > 0)) {
			// 乱数値強制の場合は必ず指定値とする
			return mRandomValueForce;
		} else {
			// 乱数を生成する
			return (mRandom.nextLong() % value) + 1;
		}
	}
}
