package com.lmt.lib.bms.bemusic;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * ロングノートのモードを表します。
 *
 * <p>Be-Musicサブセットでは、複数のロングノートモードをサポートします。各モードはそれぞれ異なる振る舞いをするため、
 * 当列挙型によってモードごとに異なる情報・振る舞いを管理します。</p>
 *
 * <p>ロングノートモードはメタ情報 {@link BeMusicMeta#LNMODE} の値を設定することで変更できます。</p>
 *
 * @since 0.3.0
 */
public enum BeMusicLongNoteMode {
	/**
	 * ロングノート先端のみ判定のあるロングノートです。
	 * <p>このモードでは終端はロングノートとしてカウントされず、終端を超えるまで入力デバイスを押さえ続ける必要があります。
	 * 終端に近い位置か、終端を超えるまで押さえ続けることで先端分のノート数がカウントされます。終端よりも手前で
	 * デバイスを離したり、先端で入力デバイスを押さえなかった場合はミスとなりノート数はカウントされません。</p>
	 * <p>このモードがBe-Musicでのデフォルトのロングノートモードとなります。</p>
	 */
	LN(1L, BeMusicNoteType.LONG_OFF, false),
	/**
	 * ロングノートの先端と終端に判定のあるロングノートです。
	 * <p>このモードではロングノート先端付近で入力デバイスを押さえ、終端付近で離すことでノート数がカウントされます。
	 * つまり、先端と終端で2個分のノートがカウントされることになります。ただし、先端で入力デバイスを押さえなかった場合、
	 * 先端と終端の両方がミスとなります。また、先端で入力デバイスを押さえ、終端よりもかなり手前で離したり終端を大きく越えて
	 * 離さなかった場合は終端の判定がミスとなります。</p>
	 */
	CN(2L, BeMusicNoteType.CHARGE_OFF, false),
	/**
	 * ロングノートの先端と終端に判定があり、更にロングノート継続中に定期的な押下判定があるロングノートです。
	 * <p>このモードは基本的には{@link #CN}と同じ振る舞いを行います。異なるのは、ロングノート継続中に入力デバイスを離すと
	 * 定期的な押下判定によりミスになる点です。定期的な押下判定はノート数としてカウントされません。
	 * また、入力デバイスを離したままだと押下判定分だけミス数が増加することになります。</p>
	 */
	HCN(3L, BeMusicNoteType.CHARGE_OFF, true);

	/** ネイティブ値とロングノートモードのマップ */
	private static final Map<Long, BeMusicLongNoteMode> NATIVE_MAP = Stream.of(values())
			.collect(Collectors.toMap(e -> e.getNativeValue(), e -> e));

	/** ネイティブ値 */
	private long mNativeValue;
	/** ロングモード終端のノート種別 */
	private BeMusicNoteType mTailType;
	/** 定期的な押下判定を行うかどうか */
	private boolean mJudgeRegularly;

	/**
	 * コンストラクタ
	 * @param nativeValue ネイティブ値
	 * @param tailType ロングノート終端のノート種別
	 * @param judgeRegularly 定期的な押下判定を行うかどうか
	 */
	private BeMusicLongNoteMode(long nativeValue, BeMusicNoteType tailType, boolean judgeRegularly) {
		mNativeValue = nativeValue;
		mTailType = tailType;
		mJudgeRegularly = judgeRegularly;
	}

	/**
	 * ネイティブ値を取得します。
	 * <p>この値はメタ情報に設定する値を表します。</p>
	 * @return ネイティブ値
	 */
	public final long getNativeValue() {
		return mNativeValue;
	}

	/**
	 * ロングノート終端のノート種別を取得します。
	 * @return ロングノート終端のノート種別
	 */
	public final BeMusicNoteType getTailType() {
		return mTailType;
	}

	/**
	 * ロングノート終端をノート数としてカウントするかどうかを判定します。
	 * @return ロングノート終端をノート数としてカウントする場合はtrue、そうでない場合はfalse
	 */
	public final boolean isCountTail() {
		return mTailType.isCountNotes();
	}

	/**
	 * ロングノート継続中に定期的な押下判定を行うかどうかを取得します。
	 * @return 定期的な押下判定を行う場合はtrue、そうでない場合はfalse
	 */
	public final boolean isJudgeRegularly() {
		return mJudgeRegularly;
	}

	/**
	 * ネイティブ値からロングノートモードを取得します。
	 * @param nativeValue ネイティブ値
	 * @return ネイティブ値に対応するロングノートモード。該当するモードがない場合は{@link #LN}
	 */
	public static BeMusicLongNoteMode fromNative(long nativeValue) {
		var lnMode = NATIVE_MAP.get(nativeValue);
		return Objects.requireNonNullElse(lnMode, LN);
	}
}
