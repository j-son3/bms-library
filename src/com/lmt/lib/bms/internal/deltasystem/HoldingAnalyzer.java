package com.lmt.lib.bms.internal.deltasystem;

import java.util.Arrays;
import java.util.List;

import com.lmt.lib.bms.bemusic.BeMusicDevice;
import com.lmt.lib.bms.bemusic.BeMusicLane;
import com.lmt.lib.bms.bemusic.BeMusicLongNoteMode;
import com.lmt.lib.bms.bemusic.BeMusicPoint;
import com.lmt.lib.bms.bemusic.BeMusicRatingType;

/**
 * 譜面傾向「HOLDING」の分析処理クラス
 */
public class HoldingAnalyzer extends RatingAnalyzer {
	/** ロングノートなしを示す値 */
	private static final int NO_LN = -1;

	/** 分析対象入力デバイスリスト */
	private List<BeMusicDevice> mDevs;
	/** HOLDING用運指データ */
	private Fingering mFingering;
	/** HCNモードかどうか */
	private boolean mIsHcn;
	/** コンフィグ */
	private HoldingConfig mConfig;

	/**
	 * コンストラクタ
	 */
	public HoldingAnalyzer() {
		super(BeMusicRatingType.HOLDING);
		clearContext();
	}

	/** {@inheritDoc} */
	@Override
	protected void compute(DsContext cxt) {
		// プレーモードごとの算出処理を実行する
		var holding = -1;
		if (!cxt.dpMode) {
			// シングルプレー
			var score = computeTimeline(cxt, BeMusicLane.PRIMARY);
			holding = score.score;
			debugOut(cxt, score.scoreOrg, score.score, score.elems, score);
		} else {
			// ダブルプレー
			// TODO ダブルプレーのHOLDING値算出に対応する
		}
		clearContext();

		cxt.stat.setRating(getRatingType(), holding);
	}

	/**
	 * タイムライン分析処理
	 * @param cxt コンテキスト
	 * @param lane 分析対象レーン
	 * @return 分析結果データ
	 */
	private HoldingScore computeTimeline(DsContext cxt, BeMusicLane lane) {
		// 初期化処理
		setupContext(cxt, lane);
		var tlScore = new HoldingScore(lane);

		// 長押しノートが全く存在しない譜面では評価を実行しない
		if (!cxt.score.hasLongNote()) {
			tlScore.numNotesTotal = cxt.score.getNoteCount();
			tlScore.playTime = cxt.score.getPlayTime();
			return tlScore;
		}

		// 動作のある楽曲位置を特定し、その楽曲位置に対する要素のリストを構築する
		// (BGAやBPM変化、譜面停止のみの楽曲位置等、入力デバイス操作を伴わない楽曲位置を除外したリスト)
		var elems = RatingElement.listElements(cxt, HoldingElement::new, BeMusicPoint::hasMovementNote);
		var countElem = elems.size();

		// 長押しの存在する範囲をグループ化する
		// このループでは範囲情報だけ設定し、実際の評価は後のステップで行う
		var rangeTop = (HoldingRange)null;
		var rangePrev = (HoldingRange)null;
		for (var i = 0; i < countElem;) {
			// この楽曲位置に長押しオブジェが存在しなければ何もしない
			if (!elems.get(i).getPoint().hasLongNoteType()) {
				i++;
				continue;
			}

			// 長押し範囲を解析する
			var j = i;
			for (; j < countElem; j++) {
				var p = elems.get(j).getPoint();
				if (!p.hasHolding() && !p.hasLongNoteHead()) { break; }
			}

			// 楽曲位置情報に範囲データをセットする
			var range = new HoldingRange(elems, i, Math.min(j, countElem - 1));
			for (j = range.first; j <= range.last; j++) {
				elems.get(j).setRange(range);
			}

			// 次の長押し範囲を解析する準備
			if (rangeTop == null) {
				rangeTop = range;
			}
			if (rangePrev != null) {
				rangePrev.next = range;
			}
			rangePrev = range;
			i = range.last + 1;
		}

		// グループ化された範囲で個別のノートに対してノート評価点を付ける
		var lastOps = new int[BeMusicDevice.COUNT];
		var lnHeads = new int[BeMusicDevice.COUNT];
		var selfResist = new double[BeMusicDevice.COUNT];
		for (var range = rangeTop; range != null; range = range.next) {
			Arrays.fill(lastOps, 0);
			Arrays.fill(lnHeads, NO_LN);

			// 長押し範囲のノートに抵抗値で影響する最小の楽曲位置を検索する
			var s = range.first - 1;
			for (; (s >= 0) && (RatingElement.timeDelta(elems, s, range.first) <= mConfig.maxResistRange); s--) {}

			// 長押し範囲の手前を走査し、操作状態の初期状態を設定する
			for (var pos = Math.max(0, s); pos < range.first; pos++) {
				var elem = elems.get(pos);
				for (var dev : mDevs) {
					var ntype = elem.getNoteType(dev);
					if (ntype.hasDownAction()) {
						lastOps[dev.getIndex()] = pos;
					}
				}
			}

			for (var pos = range.first; pos <= range.last; pos++) {
				// 操作状態、長押し状態を更新する
				var elem = elems.get(pos);
				for (var dev : mDevs) {
					var devIndex = dev.getIndex();
					var ntype = elem.getNoteType(dev);

					// 自デバイスの抵抗値を考慮する
					if (ntype.hasMovement()) {
						var resistRate = mConfig.resistRate(ntype);
						var timeDelta = RatingElement.timeDelta(elems, lastOps[devIndex], pos);
						selfResist[devIndex] = mConfig.ipfnResist.compute(timeDelta) * resistRate;
					} else {
						selfResist[devIndex] = 0.0;
					}

					// 押下操作を伴うノートに最終操作楽曲位置を設定する
					if (ntype.hasDownAction()) {
						lastOps[devIndex] = pos;
					}

					// 長押し押下・解放の状態を設定する
					if (ntype.isLongNoteHead()) {
						lnHeads[dev.getIndex()] = pos;
					}
				}

				// ノート数としてカウントされるノートに対して評価点を付ける
				for (var dev : mDevs) {
					var ntype = elem.getNoteType(dev);
					if (ntype.hasMovement()) {
						// 入力操作に伴う周囲指の抵抗を算出する
						var resistSum = selfResist[dev.getIndex()] * mConfig.resistRate(ntype);
						var finger = mFingering.getFinger(dev);
						for (var r : mFingering.getResists(finger)) {
							var resist = r.getValue();
							var resistDev = mFingering.getDevice(r.getFinger());
							var headPos = lnHeads[resistDev.getIndex()];
							if (headPos != NO_LN) {
								// 抵抗を受ける対象の入力デバイスが長押し継続中であれば基本抵抗値を補正する
								// 長押し継続は指に対して強烈な抵抗を与えるため、通常よりも強い抵抗を受ける。
								if (mIsHcn) {
									// HCNの場合は長押しの長さに限らず長押し時の倍率をそのまま適用する
									resist *= mConfig.resistRateHolding;
								} else {
									// LN/CNの場合はLNの長さに応じた抵抗値の減衰を行う
									var timeDelta = RatingElement.timeDelta(elems, headPos, pos);
									var decay = mConfig.ipfnDecay.compute(timeDelta);
									resist *= (mConfig.resistRateHolding * decay);
								}
							} else {
								// 抵抗を受ける対象の入力デバイスが長押し継続中でなければ評価対象と抵抗を受ける
								// 入力デバイスの時間差分による抵抗値の補正を行う。
								// 抵抗を受ける入力デバイスの最終操作間隔が長いほど抵抗が弱まるためこの処理を行う。
								var timeDelta = RatingElement.timeDelta(elems, lastOps[resistDev.getIndex()], pos);
								var adjustValue = mConfig.ipfnResist.compute(timeDelta);
								resist *= (mConfig.resistRateBeat * adjustValue);
							}
							resistSum += resist;
						}

						// ノート評価点を計算する
						var base = mConfig.noteScoreBase(ntype);
						var rate = mConfig.noteScoreRate(ntype);
						var score = (base + resistSum) * rate;
						range.putNoteScore(pos, dev, ntype.isCountNotes(), score);
					}
				}

				// 長押し終了を長押し状態に反映する
				// この処理はノート評価点の計算後に行う必要がある。長押し終了と同じ楽曲位置のノート評価でも
				// LN/CNのLN長さによる減衰を適用する必要があるため。
				for (var dev : mDevs) {
					if (elem.getNoteType(dev).isLongNoteTail()) {
						lnHeads[dev.getIndex()] = NO_LN;
					}
				}
			}
		}

		// 長押し範囲に関するサマリを行う
		var notesInLn = 0;
		var lnTime = 0.0;
		var noteScoreSummarizer = new ScoreSummarizer(mConfig.satulateNoteScore);
		for (var range = rangeTop; range != null; range = range.next) {
			// 長押し範囲内のノート数、長押し総時間
			notesInLn += range.notes;
			lnTime += range.time;

			// ノート評価点のサマリ
			for (var pos = range.first; pos <= range.last; pos++) {
				for (var dev : mDevs) {
					var score = range.getNoteScore(pos, dev);
					if (score != null) {
						noteScoreSummarizer.put(elems.get(pos).getTime(), score);
					}
				}
			}
		}

		// タイムラインの評価結果を設定する
		tlScore.elems = elems;
		tlScore.numNotesTotal = cxt.score.getNoteCount();
		tlScore.numNotesInLn = notesInLn;
		tlScore.numLn = cxt.score.getLongNoteCount();
		tlScore.playTime = RatingElement.computeTimeOfPointRange(elems);
		tlScore.lnTime = lnTime;
		tlScore.noteScoreSummary = noteScoreSummarizer;

		// 採取したノート評価点を基に譜面全体に対する評価点を加味し最終評価点・HOLDING値を算出する
		tlScore.scoreUnitNotes = noteScoreSummarizer.summary();
		tlScore.scoreInLnNotes = mConfig.ipfnInLnNotes.compute(tlScore.notesInLnRate());
		tlScore.scoreLnRate = mConfig.ipfnLnCount.compute(tlScore.lnRate());
		tlScore.scoreLnTime = mConfig.ipfnLnTime.compute(tlScore.lnTimeRate());
		var scoreOrg = tlScore.scoreUnitNotes * tlScore.scoreInLnNotes * tlScore.scoreLnRate * tlScore.scoreLnTime;
		tlScore.scoreOrg = computeRatingValue(scoreOrg, tlScore.playTime, 1.0);
		tlScore.score = (int)mConfig.ipfnHolding.compute(tlScore.scoreOrg);

		return tlScore;
	}

	/** {@inheritDoc} */
	@Override
	protected void dumpSummary(DsContext cxt, double org, int rating, Object... values) {
		var sb = new StringBuilder();
		var tlScore = (HoldingScore)values[0];
		sb.append(cxt.header.getComment());
		sb.append("\t").append(String.format("%s %s", cxt.header.getTitle(), cxt.header.getSubTitle()).strip());
		sb.append("\t").append(tlScore.numNotesTotal);
		sb.append("\t").append(tlScore.numNotesInLn);
		sb.append("\t").append(String.format("%.4f", tlScore.notesInLnRate()));
		sb.append("\t").append(tlScore.numLn);
		sb.append("\t").append(String.format("%.4f", tlScore.lnRate()));
		sb.append("\t").append(String.format("%.2f", tlScore.playTime));
		sb.append("\t").append(String.format("%.2f", tlScore.lnTime));
		sb.append("\t").append(String.format("%.4f", tlScore.lnTimeRate()));
		sb.append("\t").append(String.format("%.3f", tlScore.scoreUnitNotes));
		sb.append("\t").append(String.format("%.3f", tlScore.scoreInLnNotes));
		sb.append("\t").append(String.format("%.3f", tlScore.scoreLnRate));
		sb.append("\t").append(String.format("%.3f", tlScore.scoreLnTime));
		sb.append("\t").append(String.format("%.3f", tlScore.scoreOrg));
		sb.append("\t").append(String.format("%.2f", getRatingType().toValue(tlScore.score)));
		Ds.debug(sb);
	}

	/** {@inheritDoc} */
	@Override
	protected void dumpDetail(DsContext cxt, double org, int rating, List<? extends RatingElement> elems,
			Object... values) {
		super.dumpDetail(cxt, org, rating, elems, values);
		var s = (HoldingScore)values[0];
		Ds.debug("UNIT-SC: %s", s.noteScoreSummary);
		Ds.debug("NOTES  : Total=%d, InLn=%d(%.2f%%), Ln=%d(%.2f%%)",
				s.numNotesTotal, s.numNotesInLn, s.notesInLnRatePer(), s.numLn, s.lnRatePer());
		Ds.debug("TIME   : Total=%.2f, LnTime=%.2f(%.2f%%)", s.playTime, s.lnTime, s.lnTimeRatePer());
		Ds.debug("SCORE  : Org=%.4f, UnitNotes=%.4f, InLnNotes=%.4f, LnRate=%.4f, LnTime=%.4f",
				s.scoreOrg, s.scoreUnitNotes, s.scoreInLnNotes, s.scoreLnRate, s.scoreLnTime);
		Ds.debug("HOLDING: %.2f", getRatingType().toValue(s.score));
	}

	/**
	 * 分析用データのセットアップ
	 * @param cxt コンテキスト
	 * @param lane 分析対象レーン
	 */
	private void setupContext(DsContext cxt, BeMusicLane lane) {
		mDevs = BeMusicDevice.getDevices(lane);
		mFingering = Fingering.SP_HOLDING;  // TODO DPモードの運指に対応する
		mIsHcn = (cxt.header.getLnMode() == BeMusicLongNoteMode.HCN);
		mConfig = HoldingConfig.getInstance();
	}

	/**
	 * 分析用データのクリア
	 */
	private void clearContext() {
		mDevs = null;
		mFingering = null;
		mIsHcn = false;
		mConfig = null;
	}
}
