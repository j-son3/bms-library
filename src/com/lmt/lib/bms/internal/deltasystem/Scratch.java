package com.lmt.lib.bms.internal.deltasystem;

import java.util.List;
import java.util.Map;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;

import com.lmt.lib.bms.bemusic.BeMusicDevice;
import com.lmt.lib.bms.bemusic.BeMusicLane;
import com.lmt.lib.bms.bemusic.BeMusicLongNoteMode;
import com.lmt.lib.bms.bemusic.BeMusicNoteType;

/**
 * SCRATCH分析用の各種列挙型、クラスの定義
 */
class Scratch {
	/** スクラッチ範囲種別 */
	static enum Type {
		/** 単体操作 */
		SINGLE("SINGLE"),
		/** 連続操作 */
		PULSE("PULSE "),
		/** 往復操作 */
		ROUND_TRIP("R-TRIP"),
		/** 回転操作 */
		SPINNING("SPIN  ");

		/** スクラッチ範囲の短い名前 */
		final String shortName;

		/**
		 * コンストラクタ
		 * @param shortName スクラッチ範囲の短い名前
		 */
		private Type(String shortName) {
			this.shortName = shortName;
		}
	}

	/** スクラッチの操作方向 */
	static enum Direction {
		/** 外回り(押し操作) */
		OUTER('O', c -> c.addScoreOuter),
		/** 内回り(引き操作) */
		INNER('I', c -> c.addScoreInner);

		/** 操作方向の短い名前 */
		final char shortName;
		/** 操作方向ごとの加点量 */
		final ToDoubleFunction<ScratchConfig> addScore;

		/**
		 * コンストラクタ
		 * @param shortName 操作方向の短い名前
		 * @param addScore 操作方向ごとの加点量取得関数
		 */
		private Direction(char shortName, ToDoubleFunction<ScratchConfig> addScore) {
			this.shortName = shortName;
			this.addScore = addScore;
		}

		/**
		 * 反対操作取得
		 * @return この操作方向と反対のオブジェクト
		 */
		Direction reverse() {
			return (this == OUTER) ? INNER : OUTER;
		}
	}

	/** スイッチの領域 */
	static enum Area {
		/** スクラッチに最も近いスイッチ */
		NEAR("NEAR", 0, ScratchConfig.getInstance()::addScoreNear),
		/** スクラッチから遠い位置のスイッチ */
		FAR("FAR ", 3, ScratchConfig.getInstance()::addScoreFar),
		/** スクラッチをどちらの手でも操作可能な位置のスイッチ */
		BORDER("BDR ", 6, ScratchConfig.getInstance()::addScoreBorder),
		/** スクラッチを操作する反対の手で操作するスイッチ */
		OPPOSITE("OPP ", 9, ScratchConfig.getInstance()::addScoreOpposite),
		/** スクラッチと同時に操作できないスイッチ */
		IMPOSSIBLE("IMP ", 12, ScratchConfig.getInstance()::addScoreImpossible);

		/** 全領域リスト */
		static final Area[] VALUES = values();
		/** 状態データの合計ビット数 */
		static final int TOTAL_BITS = 15;

		/** 領域の短い名前 */
		final String shortName;
		/** 領域データにアクセスするためのビットシフト量 */
		final int shift;
		/** 領域ごとの加点量取得関数 */
		final ToDoubleFunction<DsContext> addScore;

		/**
		 * コンストラクタ
		 * @param shortName 領域の短い名前
		 * @param shift 領域データにアクセスするためのビットシフト量
		 * @param addScore 領域ごとの加点量取得関数
		 */
		private Area(String shortName, int shift, ToDoubleFunction<DsContext> addScore) {
			this.shortName = shortName;
			this.shift = shift;
			this.addScore = addScore;
		}
	}

	/** 評価で使用するノート種別 */
	static enum Note {
		/** 短押し */
		BEAT('B', 0, c -> c.addScoreBeat),
		/** 長押し開始 */
		LONG_ON('L', 1, c -> c.addScoreLongOn),
		/** 長押し継続 */
		LONG('H', 2, c -> c.addScoreLong);

		/** 標準ノート種別から当列挙型への変換マップ */
		private static final Map<BeMusicNoteType, Note> TYPE_MAP = Map.ofEntries(
				Map.entry(BeMusicNoteType.BEAT, BEAT),
				Map.entry(BeMusicNoteType.LONG_ON, LONG_ON),
				Map.entry(BeMusicNoteType.LONG, LONG));
		/** 全ノート種別リスト */
		static final Note[] VALUES = values();

		/** ノート種別の短い名前 */
		final char shortName;
		/** ノート種別にアクセスするためのビットシフト量 */
		final int shift;
		/** ノート種別ごとの加点量取得関数 */
		final ToDoubleFunction<ScratchConfig> addScore;

		/**
		 * コンストラクタ
		 * @param shortName ノート種別の短い名前
		 * @param shift ノート種別にアクセスするためのビットシフト量
		 * @param addScore ノート種別ごとの加点量取得関数
		 */
		private Note(char shortName, int shift, ToDoubleFunction<ScratchConfig> addScore) {
			this.shortName = shortName;
			this.shift = shift;
			this.addScore = addScore;
		}

		/**
		 * 標準ノート種別から対応する当列挙型取得
		 * @param ntype 標準ノート種別
		 * @return ノート種別。対応するノート種別がなければnull。
		 */
		static Note fromNoteType(BeMusicNoteType ntype) {
			return TYPE_MAP.get(ntype);
		}
	}

	/** 各種マッピング */
	static enum Mapping {
		/** シングルプレー用 */
		SP(Map.ofEntries(
				Map.entry(BeMusicDevice.SWITCH11, Area.NEAR),
				Map.entry(BeMusicDevice.SWITCH12, Area.FAR),
				Map.entry(BeMusicDevice.SWITCH13, Area.BORDER),
				Map.entry(BeMusicDevice.SWITCH14, Area.OPPOSITE),
				Map.entry(BeMusicDevice.SWITCH15, Area.OPPOSITE),
				Map.entry(BeMusicDevice.SWITCH16, Area.OPPOSITE),
				Map.entry(BeMusicDevice.SWITCH17, Area.OPPOSITE))),
		/** ダブルプレー：左側レーン用 */
		DP_PRIMARY(Map.ofEntries(
				Map.entry(BeMusicDevice.SWITCH11, Area.NEAR),
				Map.entry(BeMusicDevice.SWITCH12, Area.FAR),
				Map.entry(BeMusicDevice.SWITCH13, Area.BORDER),
				Map.entry(BeMusicDevice.SWITCH14, Area.IMPOSSIBLE),
				Map.entry(BeMusicDevice.SWITCH15, Area.IMPOSSIBLE),
				Map.entry(BeMusicDevice.SWITCH16, Area.IMPOSSIBLE),
				Map.entry(BeMusicDevice.SWITCH17, Area.IMPOSSIBLE),
				Map.entry(BeMusicDevice.SWITCH21, Area.OPPOSITE),
				Map.entry(BeMusicDevice.SWITCH22, Area.OPPOSITE),
				Map.entry(BeMusicDevice.SWITCH23, Area.OPPOSITE),
				Map.entry(BeMusicDevice.SWITCH24, Area.OPPOSITE),
				Map.entry(BeMusicDevice.SWITCH25, Area.OPPOSITE),
				Map.entry(BeMusicDevice.SWITCH26, Area.OPPOSITE),
				Map.entry(BeMusicDevice.SWITCH27, Area.OPPOSITE),
				Map.entry(BeMusicDevice.SCRATCH2, Area.OPPOSITE))),
		/** ダブルプレー：右側レーン用 */
		DP_SECONDARY(Map.ofEntries(
				Map.entry(BeMusicDevice.SCRATCH1, Area.OPPOSITE),
				Map.entry(BeMusicDevice.SWITCH11, Area.OPPOSITE),
				Map.entry(BeMusicDevice.SWITCH12, Area.OPPOSITE),
				Map.entry(BeMusicDevice.SWITCH13, Area.OPPOSITE),
				Map.entry(BeMusicDevice.SWITCH14, Area.OPPOSITE),
				Map.entry(BeMusicDevice.SWITCH15, Area.OPPOSITE),
				Map.entry(BeMusicDevice.SWITCH16, Area.OPPOSITE),
				Map.entry(BeMusicDevice.SWITCH17, Area.OPPOSITE),
				Map.entry(BeMusicDevice.SWITCH21, Area.IMPOSSIBLE),
				Map.entry(BeMusicDevice.SWITCH22, Area.IMPOSSIBLE),
				Map.entry(BeMusicDevice.SWITCH23, Area.IMPOSSIBLE),
				Map.entry(BeMusicDevice.SWITCH24, Area.IMPOSSIBLE),
				Map.entry(BeMusicDevice.SWITCH25, Area.BORDER),
				Map.entry(BeMusicDevice.SWITCH26, Area.FAR),
				Map.entry(BeMusicDevice.SWITCH27, Area.NEAR)));

		/** デバイスと領域マップ */
		final Map<BeMusicDevice, Area> areaMap;

		/**
		 * コンストラクタ
		 * @param areaMap デバイスと領域マップ
		 */
		private Mapping(Map<BeMusicDevice, Area> areaMap) {
			this.areaMap = areaMap;
		}

		/**
		 * レーンからマッピングデータ取得
		 * @param lane レーン
		 * @param dpMode ダブルプレーかどうか
		 * @return マッピングデータ
		 */
		static Mapping fromLane(BeMusicLane lane, boolean dpMode) {
			return dpMode ? (lane == BeMusicLane.PRIMARY ? DP_PRIMARY : DP_SECONDARY) : SP;
		}
	}

	/** SCRATCH用コンテキスト(分析に必要なデータの定義) */
	static class Context {
		/** Delta System用コンテキスト */
		DsContext ctx;
		/** 分析対象レーン */
		BeMusicLane lane;
		/** LNモード */
		BeMusicLongNoteMode lnMode;
		/** 回転操作の終点で逆回転操作が必要かどうか */
		boolean spinTurn;
		/** 回転操作の全体が評価対象かどうか(LN/CNは一部、HCNは全体) */
		boolean spinFull;
		/** マッピングデータ */
		Scratch.Mapping mapping;
		/** 分析対象レーンのスクラッチデバイス以外の評価対象デバイスリスト */
		List<BeMusicDevice> devsNotScr;
		/** 分析対象レーンのスクラッチデバイス */
		BeMusicDevice devScr;
		/** SCRATCHコンフィグ */
		ScratchConfig config;

		/**
		 * コンストラクタ
		 * @param ctx Delta System用コンテキスト
		 * @param lane 分析対象レーン
		 */
		Context(DsContext ctx, BeMusicLane lane) {
			this.ctx = ctx;
			this.lane = lane;
			this.lnMode = ctx.header.getLnMode();
			this.spinTurn = (this.lnMode != BeMusicLongNoteMode.LN);
			this.spinFull = (this.lnMode == BeMusicLongNoteMode.HCN);
			this.mapping = Scratch.Mapping.fromLane(lane, ctx.dpMode);
			this.devsNotScr = this.mapping.areaMap.keySet().stream().collect(Collectors.toList());
			this.devScr = BeMusicDevice.getScratch(lane);
			this.config = ScratchConfig.getInstance();
		}
	}

	/** 分析結果データ */
	static class Score {
		/** 単体操作スクラッチ範囲の数 */
		int numSingle = 0;
		/** 連続操作スクラッチ範囲の数 */
		int numPulse = 0;
		/** 往復操作スクラッチ範囲の数 */
		int numRtrip = 0;
		/** 回転操作スクラッチ範囲の数 */
		int numSpin = 0;
		/** スイッチのノート数 */
		int numSwitch = 0;
		/** スクラッチのノート数 */
		int numScratch = 0;
		/** 演奏時間 */
		double playTime = 0.0;
		/** スクラッチ操作時間 */
		double scratchingTime = 0.0;
		/** 操作難易度評価点 */
		double scoreDifficulty = 0.0;
		/** リズム変化頻度による評価点増加倍率 */
		double scoreChangeRate = 0.0;
		/** スクラッチ操作時間比率による評価点増加倍率 */
		double scoreTimeRate = 0.0;
		/** 最終評価点 */
		double scratchOrg = 0.0;

		/** 操作難易度評価点のサマリデータ */
		ScoreSummarizer scrDifficulty = null;

		/**
		 * スクラッチ範囲数取得
		 * @return スクラッチ範囲数
		 */
		int numRange() {
			return this.numSingle + this.numPulse + this.numRtrip + this.numSpin;
		}

		/**
		 * 総ノート数取得
		 * @return 総ノート数
		 */
		int numNotes() {
			return this.numSwitch + this.numScratch;
		}

		/**
		 * 総ノート数のうちスクラッチが占める割合取得
		 * @return 総ノート数のうちスクラッチが占める割合
		 */
		double numScratchRate() {
			var numNotes = (double)numNotes();
			return (numNotes == 0.0) ? 0.0 : (this.numScratch / numNotes);
		}

		/**
		 * 総ノート数のうちスクラッチが占める割合のパーセンテージ取得
		 * @return 総ノート数のうちスクラッチが占める割合のパーセンテージ
		 */
		double numScratchRatePer() {
			return numScratchRate() * 100.0;
		}

		/**
		 * リズム変化頻度(回/秒)取得
		 * @return リズム変化頻度(回/秒)
		 */
		double changePerSec() {
			return numRange() / this.playTime;
		}

		/**
		 * スクラッチ操作時間比率取得
		 * @return スクラッチ操作時間比率
		 */
		double scratchingRate() {
			return (this.playTime == 0.0) ? 0.0 : (this.scratchingTime / this.playTime);
		}

		/**
		 * スクラッチ操作時間比率のパーセンテージ取得
		 * @return スクラッチ操作時間比率のパーセンテージ
		 */
		double scratchingRatePer() {
			return scratchingRate() * 100.0;
		}
	}
}
