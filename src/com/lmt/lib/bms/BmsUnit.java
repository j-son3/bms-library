package com.lmt.lib.bms;

/**
 * メタ情報における情報の構成単位を表します。
 *
 * <p>構成単位とは、メタ情報に登録したデータがどのような方式で管理されるかを表します。</p>
 *
 * @see BmsMeta [BmsMeta] メタ情報
 */
public enum BmsUnit {
	/**
	 * 単体メタ情報
	 * <p>1つのメタ情報につき1つのデータのみを保有できる構成です。最も単純かつ一般的な構成です。</p>
	 */
	SINGLE,

	/**
	 * 複数メタ情報
	 * <p>1つのメタ情報に複数のデータを保有できる構成です。多くの情報を1つのメタ情報に登録できますが、
	 * その分構造が複雑になりBMS仕様の複雑化を招きますので多用は避けるべきです。</p>
	 */
	MULTIPLE,

	/**
	 * 索引付きメタ情報
	 *
	 * <p>1つのメタ情報において、36進数整数値00～ZZのインデックス値で管理する構成です。この構成単位は、チャンネルからの
	 * 参照メタ情報として用いるのが一般的です。チャンネルが配列型で、1個の配列要素がメタ情報のインデックス値を表すことで
	 * 体系的なデータ参照方法を提供します。</p>
	 *
	 * <p>この構成単位は「複数メタ情報」よりも更に複雑な構成であるため、上述した用途以外での使用は推奨されません。</p>
	 */
	INDEXED,
}
