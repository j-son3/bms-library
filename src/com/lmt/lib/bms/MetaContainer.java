package com.lmt.lib.bms;

import static com.lmt.lib.bms.internal.Assertion.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * メタ情報を格納するコンテナの集合
 *
 * <p>構成単位ごとに別々のデータ型でリスト化し、メタ情報のデータを管理している。基本的にはメタ情報の名称を
 * キーにして情報を検索できるようになっている。その他、チャンネル番号による参照先メタ情報の検索も行うことが
 * できる。</p>
 */
class MetaContainer {
	/** BMS仕様 */
	private BmsSpec mSpec;
	/** 編集モードかどうかを取得する関数 */
	private BooleanSupplier mFnIsEditMode;
	/** 時間に関連するメタ情報が更新されたことを通知するイベントハンドラ */
	private Runnable mFnOnUpdateTimeRelated;

	/** 単体メタ情報マップ */
	private Map<String, BmsMetaElement> mSingleMetas = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
	/** 複数メタ情報マップ */
	private Map<String, List<BmsMetaElement>> mMultipleMetas = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
	/** 索引付きメタ情報マップ */
	private Map<String, Map<Integer, BmsMetaElement>> mIndexedMetas = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
	/** 参照メタ情報マップ(Valueに入るインスタンスは名称によるマップと共有する) */
	private Map<Integer, ReferenceMetaInfo> mReferenceMetas = new HashMap<>();

	/** BPM変更用のメタ情報マップ */
	private Map<Integer, Map<Integer, BmsMetaElement>> mBpmMetas = new HashMap<>();
	/** 譜面停止用のメタ情報マップ */
	private Map<Integer, Map<Integer, BmsMetaElement>> mStopMetas = new HashMap<>();

	/** 単体メタ情報のデータなし要素マップ */
	private Map<String, BmsMetaElement> mSingleEmpties = new HashMap<>();
	/** 複数メタ情報の空リスト時要素マップ */
	private Map<String, BmsMetaElement> mMultipleEmpties = new HashMap<>();
	/** 索引付きメタ情報の空マップ時要素マップ */
	private Map<String, BmsMetaElement> mIndexedEmpties = new HashMap<>();

	/** 参照先メタ情報に関する情報を表す。 */
	static class ReferenceMetaInfo {
		/** メタ情報(必ず索引付き)の仕様 */
		BmsMeta meta;
		/** このメタ情報の値リスト */
		Map<Integer, BmsMetaElement> valueList;
	}

	/**
	 * コンストラクタ
	 * @param spec BMS仕様
	 * @param fnIsEditMode 編集モードかどうかを取得する関数
	 * @param fnOnUpdateTimeRelated 時間に関連するメタ情報が更新されたことを通知するイベントハンドラ
	 */
	MetaContainer(BmsSpec spec, BooleanSupplier fnIsEditMode, Runnable fnOnUpdateTimeRelated) {
		mSpec = spec;
		mFnIsEditMode = fnIsEditMode;
		mFnOnUpdateTimeRelated = fnOnUpdateTimeRelated;

		// メタ情報マップに空コンテナを生成する
		mSpec.metas().forEach(meta -> {
			var emptyElement = new BmsMetaElement(meta, (meta.isSingleUnit() ? 0 : -1), null);
			switch (meta.getUnit()) {
			case SINGLE:
				mSingleMetas.put(meta.getName(), emptyElement);
				mSingleEmpties.put(meta.getName(), emptyElement);
				break;
			case MULTIPLE:
				mMultipleMetas.put(meta.getName(), new ArrayList<>());
				mMultipleEmpties.put(meta.getName(), emptyElement);
				break;
			case INDEXED:
				mIndexedMetas.put(meta.getName(), new HashMap<>());
				mIndexedEmpties.put(meta.getName(), emptyElement);
				break;
			}
		});

		// チャンネル番号による参照先メタ情報のマップを生成する
		mSpec.getChannels().forEach(channel -> {
			var ref = channel.getRef();
			if (ref != null) {
				var info = new ReferenceMetaInfo();
				info.meta = mSpec.getIndexedMeta(ref);
				info.valueList = mIndexedMetas.get(ref);
				mReferenceMetas.put(BmsInt.box(channel.getNumber()), info);
			}
		});

		// BPM変更用メタ情報の参照を取り出しておく
		for (var i = 0; i < mSpec.getBpmChannelCount(); i++) {
			var bpmChannel = mSpec.getBpmChannel(i, true);
			var refMeta = mReferenceMetas.get(BmsInt.box(bpmChannel.getNumber()));
			if (refMeta != null) { mBpmMetas.put(BmsInt.box(bpmChannel.getNumber()), refMeta.valueList); }
		}

		// 譜面停止用メタ情報の参照を取り出しておく
		for (var i = 0; i < mSpec.getStopChannelCount(); i++) {
			var stopChannel = mSpec.getStopChannel(i, true);
			var refMeta = mReferenceMetas.get(BmsInt.box(stopChannel.getNumber()));
			if (refMeta != null) { mStopMetas.put(BmsInt.box(stopChannel.getNumber()), refMeta.valueList); }
		}
	}

	/**
	 * 単体メタ情報の値を設定する
	 * @param name メタ情報の名称
	 * @param value 設定する値。nullを指定するとその名前のメタ情報を削除する。
	 * @throws IllegalStateException 動作モードが編集モードではない
	 * @throws NullPointerException nameがnull
	 * @throws IllegalArgumentException 名称に合致するメタ情報が存在しない
	 * @throws IllegalArgumentException 初期BPM指定時、BPMが{@link BmsSpec#BPM_MIN}～{@link BmsSpec#BPM_MAX}の範囲外
	 * @throws ClassCastException valueが設定先メタ情報のデータ型に変換できない
	 */
	void setSingleMeta(String name, Object value) {
		// アサーション
		assertIsEditMode();
		assertArgMetaName(name);

		// メタ情報のアサーション
		var meta = assertMetaSpec(name, BmsUnit.SINGLE);

		// メタ情報の追加・削除を行う(valueがnullの場合"未設定"として扱う)
		if (value == null) {
			// 削除
			mSingleMetas.put(name, mSingleEmpties.get(name));
		} else {
			// 追加
			var castedValue = meta.getType().cast(value);
			assertValueWithinSpec(meta, 0, castedValue);

			// 基数選択メタ情報への設定値が不正な場合はエラーとする
			if (meta.isBaseChanger()) {
				var supportBase = BmsInt.isSupportBase(((Number)castedValue).intValue());
				assertArg(supportBase, "Wrong base-changer(%s) value. [%s]", meta.getName(), castedValue);
			}

			mSingleMetas.put(name, new BmsMetaElement(meta, 0, castedValue));
		}

		// 初期BPMを更新した場合は譜面全体を時間再計算対象とする
		if (meta.isInitialBpm()) {
			mFnOnUpdateTimeRelated.run();
		}
	}

	/**
	 * 重複可能メタ情報の値を設定する。
	 * @param name メタ情報の名称
	 * @param index インデックス
	 * @param value 設定する値。nullを指定するとその名前のメタ情報を削除する。
	 * @throws IllegalStateException 動作モードが編集モードではない
	 * @throws NullPointerException nameがnull
	 * @throws IllegalArgumentException 名称に合致するメタ情報が存在しない
	 * @throws IndexOutOfBoundsException indexが0～{@link BmsSpec#MULTIPLE_META_INDEX_MAX}の範囲外
	 * @throws ClassCastException valueが設定先メタ情報のデータ型に変換できない
	 */
	void setMultipleMeta(String name, int index, Object value) {
		// アサーション
		assertIsEditMode();
		assertArgMetaName(name);
		assertArgMultipleMetaIndex(index);

		// メタ情報の追加・削除を行う
		var meta = assertMetaSpec(name, BmsUnit.MULTIPLE);
		var metaList = mMultipleMetas.get(name);
		if (value == null) {
			// 削除
			if (index < metaList.size()) {
				// リストから当該データを削除する
				metaList.remove(index);
			}
		} else {
			// 追加
			var castedValue = meta.getType().cast(value);
			if (index < metaList.size()) {
				// 既存要素を更新する
				metaList.set(index, new BmsMetaElement(meta, index, castedValue));
			} else {
				// 新規領域に値を登録する(登録領域までの埋め合わせは初期値を使用する)
				for (var i = metaList.size(); i <= index; i++) {
					var addValue = (i == index) ? castedValue : null;
					metaList.add(new BmsMetaElement(meta, i, addValue));
				}
			}
		}

		// 重複可能メタ情報では時間再計算対象として使われるデータが存在しないため
		// 何もチェックしない
	}

	/**
	 * 索引付きメタ情報の値を設定する。
	 * @param name メタ情報の名称
	 * @param index インデックス
	 * @param value 設定する値。nullを指定するとその名前のメタ情報を削除する。
	 * @throws IllegalStateException 動作モードが編集モードではない
	 * @throws NullPointerException nameがnull
	 * @throws IllegalArgumentException 名称に合致するメタ情報が存在しない
	 * @throws IndexOutOfBoundsException indexが0～{@link BmsSpec#INDEXED_META_INDEX_MAX}の範囲外
	 * @throws ClassCastException valueが設定先メタ情報のデータ型に変換できない
	 */
	void setIndexedMeta(String name, int index, Object value) {
		// アサーション
		assertIsEditMode();
		assertArgMetaName(name);
		assertArgIndexedMetaIndex(index);

		// メタ情報の追加・削 除を行う
		var meta = assertMetaSpec(name, BmsUnit.INDEXED);
		var metaMap = mIndexedMetas.get(name);
		if (value == null) {
			// 削除
			metaMap.remove(BmsInt.box(index));
		} else {
			// 追加
			var castedValue = meta.getType().cast(value);
			assertValueWithinSpec(meta, index, castedValue);
			metaMap.put(BmsInt.box(index), new BmsMetaElement(meta, index, castedValue));
		}

		// BPMまたはSTOPのデータ更新を行った場合は譜面全体を時間再計算対象とする
		if (meta.isReferenceBpm() || meta.isReferenceStop()) {
			// BPM変更または譜面停止のデータ更新を行ったので譜面全体を時間再計算対象とする
			mFnOnUpdateTimeRelated.run();
		}
	}

	/**
	 * 指定名称・単位・インデックスに該当するメタ情報の値を設定する。
	 * @param name メタ情報の名称
	 * @param unit メタ情報の単位
	 * @param index インデックス
	 * @param value 設定する値。nullを指定するとその名前のメタ情報を削除する。
	 * @throws IllegalStateException 動作モードが編集モードではない
	 * @throws NullPointerException nameがnull
	 * @throws NullPointerException unitがnull
	 * @throws IllegalArgumentException name, unitに合致するメタ情報が存在しない
	 * @throws IndexOutOfBoundsException unitが{@link BmsUnit#SINGLE SINGLE}の時、indexが0以外
	 * @throws IndexOutOfBoundsException unitが{@link BmsUnit#MULTIPLE MULTIPLE}の時、indexが0～{@link BmsSpec#MULTIPLE_META_INDEX_MAX}の範囲外
	 * @throws IndexOutOfBoundsException unitが{@link BmsUnit#INDEXED INDEXED}の時、indexが0～{@link BmsSpec#INDEXED_META_INDEX_MAX}の範囲外
	 * @throws ClassCastException valueが設定先メタ情報のデータ型に変換できない
	 */
	void setMeta(String name, BmsUnit unit, int index, Object value) {
		// 単位により処理を分岐
		assertArgMetaUnit(unit);
		switch (unit) {
		case SINGLE:
			assertArgSingleMetaIndex(index);
			setSingleMeta(name, value);
			break;
		case MULTIPLE:
			setMultipleMeta(name, index, value);
			break;
		case INDEXED:
			setIndexedMeta(name, index, value);
			break;
		default:
			throw new IllegalArgumentException("Wrong meta unit.");
		}
	}

	/**
	 * 単体メタ情報の値を取得する。
	 * @param name メタ情報の名称
	 * @return メタ情報の値。値が未設定の場合、そのメタ情報の初期値。
	 * @throws NullPointerException nameがnull
	 * @throws IllegalArgumentException 名称に合致するメタ情報が存在しない
	 */
	Object getSingleMeta(String name) {
		assertArgMetaName(name);
		assertMetaSpec(name, BmsUnit.SINGLE);
		return mSingleMetas.get(name).getValue();
	}

	/**
	 * 重複可能メタ情報の値を取得する。
	 * @param name メタ情報の名称
	 * @param index インデックス
	 * @return メタ情報の値。値が未設定の場合、そのメタ情報の初期値。
	 * @throws NullPointerException nameがnull
	 * @throws IndexOutOfBoundsException indexが0～{@link BmsSpec#MULTIPLE_META_INDEX_MAX}の範囲外
	 */
	Object getMultipleMeta(String name, int index) {
		// アサーション
		assertArgMetaName(name);
		assertArgMultipleMetaIndex(index);

		// メタ情報を取得する
		var meta = assertMetaSpec(name, BmsUnit.MULTIPLE);
		var valueList = mMultipleMetas.get(name);
		return (index < valueList.size()) ? valueList.get(index).getValue() : meta.getDefaultValue();
	}

	/**
	 * 索引付きメタ情報の値を取得する。
	 * @param name メタ情報の名称
	 * @param index インデックス
	 * @return メタ情報の値。値が未設定の場合、そのメタ情報の初期値。
	 * @throws NullPointerException nameがnull
	 * @throws IndexOutOfBoundsException indexが0～{@link BmsSpec#INDEXED_META_INDEX_MAX}の範囲外
	 * @throws IllegalArgumentException 名称に合致するメタ情報が存在しない
	 */
	Object getIndexedMeta(String name, int index) {
		// アサーション
		assertArgMetaName(name);
		assertArgIndexedMetaIndex(index);

		// メタ情報を取得する
		var meta = assertMetaSpec(name, BmsUnit.INDEXED);
		var valueMap = mIndexedMetas.get(name);
		var element = valueMap.get(BmsInt.box(index));
		return (element == null) ? meta.getDefaultValue() : element.getValue();
	}

	/**
	 * 索引付きメタ情報からBPM変更で使用するBPMの値を取得する。
	 * @param note 取得対象のノート
	 * @param bpmIfNotExist ノートの値に該当するBPMが見つからなかった時に返す代替値
	 * @return BPM
	 */
	double getIndexedMetaBpm(BmsNote note, double bpmIfNotExist) {
		if (mSpec.getBpmChannel(note.getChannel()) == null) {
			// BPM変更チャンネル非対応
			return bpmIfNotExist;
		}
		var bpmMetas = mBpmMetas.get(BmsInt.box(note.getChannel()));
		if (bpmMetas == null) {
			// 参照先メタ情報なしの場合はノートの値をBPMとする
			return note.getValue();
		}

		// 参照先メタ情報から値を取得する
		var element = bpmMetas.get(BmsInt.box(note.getValue()));
		if (element == null) {
			// 参照先メタ情報に定義がない
			return bpmIfNotExist;
		} else {
			// 参照先メタ情報の値を返す
			return element.getValueAsDouble();
		}
	}

	/**
	 * 索引付きメタ情報から譜面停止で使用する停止時間の値を取得する。
	 * @param note 取得対象のノート
	 * @param stopIfNotExist ノートの値に該当する停止時間が見つからなかった時に返す代替値
	 * @return 停止時間
	 */
	double getIndexedMetaStop(BmsNote note, double stopIfNotExist) {
		if (mSpec.getStopChannel(note.getChannel()) == null) {
			// 譜面停止チャンネル非対応
			return stopIfNotExist;
		}
		var stopMetas = mStopMetas.get(BmsInt.box(note.getChannel()));
		if (stopMetas == null) {
			// 参照先メタ情報なしの場合はNoteの値を停止時間とする
			return note.getValue();
		}

		// 参照先メタ情報から値を取得する
		var element = stopMetas.get(BmsInt.box(note.getValue()));
		if (element == null) {
			// 参照先メタ情報に定義がない
			return stopIfNotExist;
		} else {
			// 参照先メタ情報の値を返す
			return element.getValueAsDouble();
		}
	}

	/**
	 * 指定した名称・単位・インデックスに該当するメタ情報の値を取得する。
	 * @param name メタ情報の名称
	 * @param unit メタ情報の単位
	 * @param index インデックス
	 * @return メタ情報の値。値が未設定の場合、そのメタ情報の初期値。
	 * @throws NullPointerException nameがnull
	 * @throws NullPointerException unitがnull
	 * @throws IllegalArgumentException name, unitに合致するメタ情報が存在しない
	 * @throws IndexOutOfBoundsException unitが{@link BmsUnit#SINGLE SINGLE}の時、indexが0以外
	 * @throws IndexOutOfBoundsException unitが{@link BmsUnit#MULTIPLE MULTIPLE}の時、indexが0～{@link BmsSpec#MULTIPLE_META_INDEX_MAX}の範囲外
	 * @throws IndexOutOfBoundsException unitが{@link BmsUnit#INDEXED INDEXED}の時、indexが0～{@link BmsSpec#INDEXED_META_INDEX_MAX}の範囲外
	 */
	Object getMeta(String name, BmsUnit unit, int index) {
		// 単位により処理を分岐
		assertArgMetaUnit(unit);
		switch (unit) {
		case SINGLE:
			assertArgSingleMetaIndex(index);
			return getSingleMeta(name);
		case MULTIPLE:
			return getMultipleMeta(name, index);
		case INDEXED:
			return getIndexedMeta(name, index);
		default:
			throw new IllegalArgumentException("Wrong meta unit.");
		}
	}

	/**
	 * 指定した重複可能メタ情報の全ての値を取得する。
	 * @param name メタ情報の名称
	 * @return 全ての値のリスト
	 * @throws NullPointerException nameがnull
	 * @throws IllegalArgumentException 名称に合致するメタ情報が存在しない
	 */
	List<Object> getMultipleMetas(String name) {
		assertArgMetaName(name);
		assertMetaSpec(name, BmsUnit.MULTIPLE);
		return mMultipleMetas.get(name).stream().map(BmsMetaElement::getValue).collect(Collectors.toList());
	}

	/**
	 * 指定した索引付きメタ情報の全ての値を取得する。
	 * @param name メタ情報の名称
	 * @return 全ての値のリスト
	 * @throws NullPointerException nameがnull
	 * @throws IllegalArgumentException 名称に合致するメタ情報が存在しない
	 */
	Map<Integer, Object> getIndexedMetas(String name) {
		assertArgMetaName(name);
		assertMetaSpec(name, BmsUnit.INDEXED);
		return mIndexedMetas.get(name).entrySet().stream().collect(Collectors.toMap(
				e -> e.getKey(), e -> e.getValue().getValue(), (o, n) -> n, TreeMap::new));
	}

	/**
	 * 指定した単体メタ情報の数を取得する。
	 * @param name メタ情報の名称
	 * @return メタ情報に値が設定されていれば1、なければ0
	 * @throws NullPointerException nameがnull
	 * @throws IllegalArgumentException 名称に合致するメタ情報が存在しない
	 */
	int getSingleMetaCount(String name) {
		assertArgMetaName(name);
		assertMetaSpec(name, BmsUnit.SINGLE);
		return mSingleMetas.get(name).isContain() ? 1 : 0;
	}

	/**
	 * 指定した重複可能メタ情報の数を取得する。
	 * @param name メタ情報の名称
	 * @return メタ情報の数
	 * @throws NullPointerException nameがnull
	 * @throws IllegalArgumentException 名称に合致するメタ情報が存在しない
	 */
	int getMultipleMetaCount(String name) {
		assertArgMetaName(name);
		assertMetaSpec(name, BmsUnit.MULTIPLE);
		return mMultipleMetas.get(name).size();
	}

	/**
	 * 指定した索引付きメタ情報の数を取得する。
	 * @param name メタ情報の名称
	 * @return メタ情報の数
	 * @throws NullPointerException nameがnull
	 * @throws IllegalArgumentException 名称に合致するメタ情報が存在しない
	 */
	int getIndexedMetaCount(String name) {
		assertArgMetaName(name);
		assertMetaSpec(name, BmsUnit.INDEXED);
		return mIndexedMetas.get(name).size();
	}

	/**
	 * 指定した名称・単位のメタ情報の数を取得する。
	 * @param name メタ情報の名称
	 * @param unit メタ情報の単位
	 * @return メタ情報の数
	 * @throws NullPointerException nameがnull
	 * @throws NullPointerException unitがnull
	 * @throws IllegalArgumentException 名称に合致するメタ情報が存在しない
	 */
	int getMetaCount(String name, BmsUnit unit) {
		assertArgMetaUnit(unit);
		switch (unit) {
		case SINGLE:
			return getSingleMetaCount(name);
		case MULTIPLE:
			return getMultipleMetaCount(name);
		case INDEXED:
			return getIndexedMetaCount(name);
		default:
			throw new IllegalArgumentException("Wrong meta unit.");
		}
	}

	/**
	 * 指定した名称に該当する単体メタ情報に値が設定されているか判定する。
	 * @param name メタ情報の名称
	 * @return 値が設定されていればtrue
	 * @throws NullPointerException nameがnull
	 * @throws IllegalArgumentException 名称に合致するメタ情報が存在しない
	 */
	boolean containsSingleMeta(String name) {
		assertArgMetaName(name);
		assertMetaSpec(name, BmsUnit.SINGLE);
		return mSingleMetas.get(name).isContain();
	}

	/**
	 * 指定した名称に該当する重複可能メタ情報に値が設定されているか判定する。
	 * @param name メタ情報の名称
	 * @param index インデックス
	 * @return 値が設定されていればtrue
	 * @throws NullPointerException nameがnull
	 * @throws IllegalArgumentException 名称に合致するメタ情報が存在しない
	 * @throws IndexOutOfBoundsException indexが0～{@link BmsSpec#MULTIPLE_META_INDEX_MAX}の範囲外
	 */
	boolean containsMultipleMeta(String name, int index) {
		assertArgMetaName(name);
		assertMetaSpec(name, BmsUnit.MULTIPLE);
		assertArgMultipleMetaIndex(index);
		return index < mMultipleMetas.get(name).size();
	}

	/**
	 * 指定した名称に該当する索引付きメタ情報に値が設定されているか判定する。
	 * @param name メタ情報の名称
	 * @param index インデックス
	 * @return 値が設定されていればtrue
	 * @throws NullPointerException nameがnull
	 * @throws IllegalArgumentException 名称に合致するメタ情報が存在しない
	 * @throws IndexOutOfBoundsException indexが0～{@link BmsSpec#INDEXED_META_INDEX_MAX}の範囲外
	 */
	boolean containsIndexedMeta(String name, int index) {
		assertArgMetaName(name);
		assertMetaSpec(name, BmsUnit.INDEXED);
		assertArgIndexedMetaIndex(index);
		return mIndexedMetas.get(name).containsKey(BmsInt.box(index));
	}

	/**
	 * 指定した名称・単位に該当するメタ情報に値が設定されているか判定する。
	 * @param name メタ情報の名称
	 * @param unit メタ情報の単位
	 * @param index インデックス
	 * @return 値が設定されていればtrue
	 * @throws NullPointerException nameがnull
	 * @throws NullPointerException unitがnull
	 * @throws IllegalArgumentException 名称・単位に合致するメタ情報が存在しない
	 * @throws IndexOutOfBoundsException unitが{@link BmsUnit#SINGLE SINGLE}の時、indexが0以外
	 * @throws IndexOutOfBoundsException unitが{@link BmsUnit#MULTIPLE MULTIPLE}の時、indexが0～{@link BmsSpec#MULTIPLE_META_INDEX_MAX}の範囲外
	 * @throws IndexOutOfBoundsException unitが{@link BmsUnit#INDEXED INDEXED}の時、indexが0～{@link BmsSpec#INDEXED_META_INDEX_MAX}の範囲外
	 */
	boolean containsMeta(String name, BmsUnit unit, int index) {
		assertArgMetaUnit(unit);
		switch (unit) {
		case SINGLE:
			assertArgSingleMetaIndex(index);
			return containsSingleMeta(name);
		case MULTIPLE:
			return containsMultipleMeta(name, index);
		case INDEXED:
			return containsIndexedMeta(name, index);
		default:
			throw new IllegalArgumentException("Wrong meta unit.");
		}
	}

	/**
	 * 指定した名称・単位に該当するメタ情報に値が1件でも設定されているか判定する。
	 * @param name メタ情報の名称
	 * @param unit メタ情報の単位
	 * @return 当該メタ情報に値が1件でも設定されている場合true
	 * @throws NullPointerException nameがnull
	 * @throws NullPointerException unitがnull
	 * @throws IllegalArgumentException 名称・単位に合致するメタ情報が存在しない
	 */
	boolean containsMeta(String name, BmsUnit unit) {
		assertArgNotNull(name, "name");
		assertArgNotNull(unit, "unit");
		switch (unit) {
		case SINGLE:
			if (!mSingleMetas.containsKey(name)) {
				throw new IllegalArgumentException(name + ": No such single meta.");
			} else {
				return mSingleMetas.get(name).isContain();
			}
		case MULTIPLE: {
			var list = mMultipleMetas.get(name);
			if (list == null) {
				throw new IllegalArgumentException(name + ": No such multiple meta.");
			} else {
				return (list.size() > 0);
			}
		}
		case INDEXED: {
			var map = mIndexedMetas.get(name);
			if (map == null) {
				throw new IllegalArgumentException(name + ": No suc indexed meta.");
			} else {
				return (map.size() > 0);
			}
		}
		default:
			throw new IllegalArgumentException("Wrong meta unit.");
		}
	}

	/**
	 * 全メタ情報走査ストリームを返す
	 * @return 全メタ情報走査ストリーム
	 * @throws IllegalStateException 動作モードが編集モード
	 */
	Stream<BmsMetaElement> metas() {
		assertIsReferenceMode();
		return Stream.concat(Stream.concat(singleMetas(), multipleMetas()), indexedMetas());
	}

	/**
	 * 単体メタ情報走査ストリームを返す
	 * @return 単体メタ情報走査ストリーム
	 * @throws IllegalStateException 動作モードが編集モード
	 */
	Stream<BmsMetaElement> singleMetas() {
		assertIsReferenceMode();
		return mSingleMetas.values().stream();
	}

	/**
	 * 複数メタ情報走査ストリームを返す
	 * @return 複数メタ情報走査ストリーム
	 * @throws IllegalStateException 動作モードが編集モード
	 */
	Stream<BmsMetaElement> multipleMetas() {
		assertIsReferenceMode();
		return mMultipleMetas.entrySet().stream().flatMap(e -> multipleStream(e.getKey(), e.getValue()));
	}

	/**
	 * 指定した名前の複数メタ情報走査ストリームを返す
	 * @param name 複数メタ情報の名前
	 * @return 複雑メタ情報走査ストリーム
	 * @throws IllegalStateException 動作モードが編集モード
	 * @throws NullPointerException nameがnull
	 * @throws IllegalArgumentException nameに該当する複数メタ情報がBMS仕様に存在しない
	 */
	Stream<BmsMetaElement> multipleMetas(String name) {
		assertIsReferenceMode();
		assertArgMetaName(name);
		assertMetaSpec(name, BmsUnit.MULTIPLE);
		return multipleStream(name, mMultipleMetas.get(name));
	}

	/**
	 * 索引付きメタ情報走査ストリームを返す
	 * @return 索引付きメタ情報走査ストリーム
	 * @throws IllegalStateException 動作モードが編集モード
	 */
	Stream<BmsMetaElement> indexedMetas() {
		assertIsReferenceMode();
		return mIndexedMetas.entrySet().stream().flatMap(e -> indexedStream(e.getKey(), e.getValue()));
	}

	/**
	 * 指定した名前の索引付きメタ情報走査ストリームを返す
	 * @param name 索引付きメタ情報の名前
	 * @return 索引付きメタ情報走査ストリーム
	 * @throws IllegalStateException 動作モードが編集モード
	 * @throws NullPointerException nameがnull
	 * @throws IllegalArgumentException nameに該当する索引付きメタ情報がBMS仕様に存在しない
	 */
	Stream<BmsMetaElement> indexedMetas(String name) {
		assertIsReferenceMode();
		assertArgMetaName(name);
		assertMetaSpec(name, BmsUnit.INDEXED);
		return indexedStream(name, mIndexedMetas.get(name));
	}

	/**
	 * 初期BPMを設定する。
	 * @param bpm 初期BPM
	 * @throws IllegalStateException 動作モードが編集モードではない
	 * @throws IllegalArgumentException bpmが{@link BmsSpec#BPM_MIN}未満、または{@link BmsSpec#BPM_MAX}超過
	 */
	void setInitialBpm(double bpm) {
		setSingleMeta(mSpec.getInitialBpmMeta().getName(), bpm);
	}

	/**
	 * 初期BPMを取得する。
	 * @return 初期BPM
	 */
	double getInitialBpm() {
		return (Double)getSingleMeta(mSpec.getInitialBpmMeta().getName());
	}

	/**
	 * 参照先メタ情報の情報を取得する。
	 * @param channel チャンネル番号
	 * @return 参照先メタ情報の情報。該当チャンネルに参照先メタ情報がない場合はnull。
	 */
	ReferenceMetaInfo getReferenceMeta(int channel) {
		return mReferenceMetas.get(BmsInt.box(channel));
	}

	/**
	 * 指定した名前の複数メタ情報走査ストリームを返す。
	 * <p>リストが空の場合は空要素を返すストリームを返す。</p>
	 * @param name 複数メタ情報の名前
	 * @param list 複数メタ情報データリスト
	 * @return 複数メタ情報走査ストリーム
	 */
	private Stream<BmsMetaElement> multipleStream(String name, List<BmsMetaElement> list) {
		return list.isEmpty() ? Stream.of(mMultipleEmpties.get(name)) : list.stream();
	}

	/**
	 * 指定した名前の索引付きメタ情報走査ストリームを返す。
	 * <p>マップが空の場合は空要素を返すストリームを返す。</p>
	 * @param name 索引付きメタ情報の名前
	 * @param map 索引付きメタ情報データマップ
	 * @return 複数メタ情報走査ストリーム
	 */
	private Stream<BmsMetaElement> indexedStream(String name, Map<Integer, BmsMetaElement> map) {
		return map.isEmpty() ? Stream.of(mIndexedEmpties.get(name)) : map.values().stream();
	}

	/**
	 * 指定名称・単位に合致するメタ情報が存在することをテストするアサーション。
	 * @param name メタ情報の名称
	 * @param unit メタ情報の単位
	 * @return 名称・単位に合致したメタ情報
	 * @throws IllegalArgumentException 名称・単位に合致するメタ情報が存在しない
	 */
	private BmsMeta assertMetaSpec(String name, BmsUnit unit) {
		var meta = mSpec.getMeta(name, unit);
		if (meta == null) {
			throw new IllegalArgumentException(String.format("%s: No such meta in spec.", name));
		} else {
			return meta;
		}
	}

	/**
	 * 動作モードが編集モードかどうかをテストするアサーション
	 * @throws IllegalStateException 動作モードが編集モードではない
	 */
	private void assertIsEditMode() {
		if (!mFnIsEditMode.getAsBoolean()) {
			throw new IllegalStateException("Now is NOT edit mode.");
		}
	}

	/**
	 * 動作モードが参照モードかどうかをテストするアサーション
	 * @throws IllegalStateException 動作モードが参照モードではない
	 */
	private void assertIsReferenceMode() {
		if (mFnIsEditMode.getAsBoolean()) {
			throw new IllegalStateException("Now is NOT reference mode.");
		}
	}

	/**
	 * メタ情報の名称をテストするアサーション。
	 * @param name メタ情報の名称。
	 * @throws NullPointerException nameがnull
	 */
	private static void assertArgMetaName(String name) {
		assertArgNotNull(name, "name");
	}

	/**
	 * メタ情報の単位をテストするアサーション。
	 * @param unit メタ情報の単位
	 * @throws NullPointerException unitがnull
	 */
	private static void assertArgMetaUnit(BmsUnit unit) {
		assertArgNotNull(unit, "unit");
	}

	/**
	 * 単体メタ情報のインデックスをテストするアサーション。
	 * @param index インデックス値
	 * @throws IndexOutOfBoundsException indexが0以外
	 */
	private static void assertArgSingleMetaIndex(int index) {
		if (index != 0) {
			var msg = String.format("At the single unit meta, index can specify 0 only. index=%d", index);
			throw new IndexOutOfBoundsException(msg);
		}
	}

	/**
	 * 複数メタ情報のインデックスをテストするアサーション。
	 * @param index インデックス値
	 * @throws IndexOutOfBoundsException indexが0～{@link BmsSpec#MULTIPLE_META_INDEX_MAX}の範囲外
	 */
	private static void assertArgMultipleMetaIndex(int index) {
		assertArgIndexRange(index, BmsSpec.MULTIPLE_META_INDEX_MAX + 1, "index");
	}

	/**
	 * 索引付きメタ情報のインデックスをテストするアサーション。
	 * @param index インデックス値
	 * @throws IndexOutOfBoundsException indexが0～{@link BmsSpec#INDEXED_META_INDEX_MAX}の範囲外
	 */
	private static void assertArgIndexedMetaIndex(int index) {
		assertArgIndexRange(index, BmsSpec.INDEXED_META_INDEX_MAX + 1, "index");
	}

	/**
	 * BMSライブラリ仕様違反有無をテストするアサーション。
	 * @param meta メタ情報
	 * @param index メタ情報インデックス
	 * @param value 変換後設定値
	 * @throws IllegalArgumentException メタ情報への設定値がBMSライブラリ仕様違反
	 */
	private static void assertValueWithinSpec(BmsMeta meta, int index, Object value) {
		if (meta.isInitialBpm() || meta.isReferenceBpm()) {
			// 初期BPM、BPM変更
			var bpm = ((Number)value).doubleValue();
			if (!BmsSpec.isBpmWithinRange(bpm)) {
				var msg = String.format(
						"BPM is library spec violation. meta=%s, index=%d, expected-bpm=(%.16g-%.16g), actual-bpm=%.16g",
						meta, index, BmsSpec.BPM_MIN, BmsSpec.BPM_MAX, bpm);
				throw new IllegalArgumentException(msg);
			}
		} else if (meta.isReferenceStop()) {
			// 譜面停止時間
			var stop = ((Number)value).doubleValue();
			if (!BmsSpec.isStopWithinRange(stop)) {
				var msg = String.format(
						"Stop tick is library spec violation. meta=%s, index=%d, expected-stop=(%.16g-%.16g), actuao-stop=%.16g",
						meta, index, (double)BmsSpec.STOP_MIN, (double)BmsSpec.STOP_MAX, stop);
				throw new IllegalArgumentException(msg);
			}
		}
	}
}
