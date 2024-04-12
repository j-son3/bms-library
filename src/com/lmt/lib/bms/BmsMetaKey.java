package com.lmt.lib.bms;

import static com.lmt.lib.bms.BmsAssertion.*;

import java.util.Objects;

/**
 * メタ情報のキーを表します。
 *
 * <p>メタ情報を一意に特定するには、「名称」と「構成単位({@link BmsUnit})」を必要とします。メタ情報には、名称が同じで構成単位が
 * 異なるものが一部存在するからです。</p>
 */
public class BmsMetaKey {
	/** メタ情報の名称(先頭の#,%を含み、小文字で表現された文字列) */
	private String mName;
	/** メタ情報の情報単位 */
	private BmsUnit mUnit;

	/**
	 * 新しいメタ情報キーを生成します。
	 * @param name メタ情報の名称
	 * @param unit 構成単位
	 * @exception NullPointerException nameがnull
	 * @exception NullPointerException unitがnull
	 */
	public BmsMetaKey(String name, BmsUnit unit) {
		assertArgNotNull(name, "name");
		assertArgNotNull(unit, "unit");
		mName = name;
		mUnit = unit;
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof BmsMetaKey) {
			var key = (BmsMetaKey)obj;
			return (mUnit == key.mUnit) && mName.equals(key.mName);
		} else {
			return false;
		}
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		return Objects.hash(mUnit, mName);
	}

	/**
	 * メタ情報の名称と構成単位が分かる形式の文字列を返します。
	 * @return メタ情報の名称と構成単位が分かる形式の文字列
	 */
	@Override
	public String toString() {
		return String.format("{%s:%s}", mName, mUnit);
	}

	/**
	 * メタ情報の名称を取得します。
	 * <p>名称は先頭の「#」「%」の文字も含まれ、全てアルファベットの小文字で表現されます。</p>
	 * @return メタ情報の名称
	 */
	public String getName() {
		return mName;
	}

	/**
	 * メタ情報のデータ単位を取得します。
	 * @return メタ情報のデータ単位
	 */
	public BmsUnit getUnit() {
		return mUnit;
	}
}
