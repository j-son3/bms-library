package com.lmt.lib.bms.bemusic;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * スクラッチのモードを表します。
 *
 * <p>スクラッチは通常、2方向への操作指示が可能な入力デバイスですがノートの配置方法により様々な表現が可能になります。
 * Be-Musicではロングノートと組み合わせて特殊な操作指示を必要とする配置が行われることがあります。
 * 当列挙型は体系的に名前の付いた配置内容を「スクラッチモード」として分類し、
 * プレイヤーにスクラッチの種別を通知することを目的としています。</p>
 *
 * @since 0.7.0
 */
public enum BeMusicScratchMode {
	/**
	 * 通常のスクラッチモードです。
	 * <p>スクラッチは通常の2方向への単純な操作指示のみであることを表します。</p>
	 */
	NORMAL(0),
	/**
	 * 任意方向への回転操作の後、逆方向へ返す操作のあるスクラッチモードです。
	 * <p>スクラッチモードがこの値を示すと、上記ような操作が楽曲中に1回以上登場します。</p>
	 * <p>ロングノートモードが{@link BeMusicLongNoteMode#LN}の場合は長押しノートの終端が現れた後、
	 * 規定時間以内に通常のスクラッチ操作が現れた時に実質的に上記で示す操作が求められるため、
	 * このモードであると判定されます。
	 * それ以外のロングノートモードでは単純にスクラッチデバイスに長押しノートが割り当てられた時にこのモードであると判定されます。</p>
	 */
	BACKSPIN(1),
	/**
	 * 任意方向への回転操作の後、逆方向への回転操作があり、その回転の返し操作が1回以上あるスクラッチモードです。
	 * <p>スクラッチモードがこの値を示すと、上記ような操作が楽曲中に1回以上登場します。</p>
	 * <p>当モードを示す時、楽曲中に{@link #BACKSPIN}が含まれる場合があります。
	 * つまり、当モードは{@link #BACKSPIN}の上位モードであることを示します。</p>
	 * <p>ロングノートモードが{@link BeMusicLongNoteMode#LN}以外の時、当モードには成り得ません。
	 * 現在(2023年8月)のBe-Musicでは当モードに該当する配置を表現する正式なBMSとしての仕様がないためです。
	 * 当モードは{@link BeMusicLongNoteMode#LN}において長押しノートの配置内容を工夫することでのみ表現が可能となっています。</p>
	 */
	MULTISPIN(2);

	/** インデックスとスクラッチモードのマップ */
	private static Map<Integer, BeMusicScratchMode> INDEX_MAP = Stream.of(values())
			.collect(Collectors.toMap(e -> e.getIndex(), e -> e));

	/** インデックス */
	private int mIndex;

	/**
	 * コンストラクタ
	 * @param index インデックス
	 */
	private BeMusicScratchMode(int index) {
		mIndex = index;
	}

	/**
	 * インデックス値を取得します。
	 * @return インデックス値
	 */
	public int getIndex() {
		return mIndex;
	}

	/**
	 * インデックス値からスクラッチモードを取得します。
	 * @param index インデックス値
	 * @return インデックス値に該当するスクラッチモード。該当するモードがない場合は{@link #NORMAL}。
	 */
	public static BeMusicScratchMode fromIndex(int index) {
		var scrMode = INDEX_MAP.get(index);
		return Objects.requireNonNullElse(scrMode, NORMAL);
	}
}
