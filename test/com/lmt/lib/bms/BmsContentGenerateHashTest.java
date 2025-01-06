package com.lmt.lib.bms;

import static org.junit.Assert.*;

import java.util.function.Consumer;

import org.junit.Test;

import com.lmt.lib.bms.internal.Utility;

public class BmsContentGenerateHashTest {
	// ※注記：テストの期待結果を記載するコメントは、特別な記述がない限り以下の前提での記述となっている。
	// ・同一性チェックがONのメタ情報・チャンネルを編集することを前提とする
	// ・BMS仕様は含まないものとする

	// 基本フォーマット：BMS仕様を含めるとBMS仕様の変化でハッシュ値が変化すること
	@Test
	public void testBasic_IncludeSpec() {
		var spec1 = genericSpec().create();
		var spec2 = genericSpec().addMeta(BmsMeta.single("#x", BmsType.INTEGER, "0", -1, true)).create();
		// {"spec":"d19de56ff9f04b190db738c63b9dbc07a7b5820a1bd3feed2b5739dc9e34fa45","metas":{},"timeline":{}}
		assertHash("eaf9ca8a43c76e949db44347ddaee1ded6a667916b84e52f8034b6795afec2e3", new BmsContent(spec1),
				true, true, true);
		// {"spec":"766c2dabca883df2dc529663ee5096ebebd50d515f4bdda16f2a1dc3e95ea178","metas":{},"timeline":{}}
		assertHash("45eb31d781c40ebe1b9466a96b0ed1957f4c204814668621e30e9aaa76ac44d9", new BmsContent(spec2),
				true, true, true);
	}

	// 基本フォーマット：BMS仕様を含めないとBMS仕様の変化ではハッシュ値は変化しないこと
	@Test
	public void testBasic_ExcludeSpec() {
		var spec1 = genericSpec().create();
		var spec2 = genericSpec().addMeta(BmsMeta.single("#x", BmsType.INTEGER, "0", -1, true)).create();
		// {"metas":{},"timeline":{}}
		assertHash("bbb7e24f07f191543958278acceee161ab1c7989929fec6c68036560c11ebfa4", new BmsContent(spec1),
				false, true, true);
		// {"metas":{},"timeline":{}}
		assertHash("bbb7e24f07f191543958278acceee161ab1c7989929fec6c68036560c11ebfa4", new BmsContent(spec2),
				false, true, true);
	}

	// 基本フォーマット：メタ情報を含めるとメタ情報の変化でハッシュ値が変化すること
	@Test
	public void testBasic_IncludeMetas() {
		// {"metas":{"#is:s":{"t":"i","d":[{"i":0,"v":100}]}},"timeline":{}}
		assertHash("510ef3f5c2f7cdc0021da3c0e19e78496c1bb939eab8ebafe9adcc318eb97f91", c -> {
			c.setSingleMeta("#is", 100L);
		});
		// {"metas":{"#is:s":{"t":"i","d":[{"i":0,"v":200}]}},"timeline":{}}
		assertHash("7b17dcc5ffb1be4fcc901c4dcef8562ff6a62d0b0eedbf4adea356f6d10ff0bb", c -> {
			c.setSingleMeta("#is", 200L);
		});
		// {"metas":{"#im:m":{"t":"i","d":[{"i":0,"v":0},{"i":1,"v":300}]}},"timeline":{}}
		assertHash("cd11e48e9042af392dc76cdbced3d5619afc65d69c9e6bec167392c1829fa6a5", c -> {
			c.setMultipleMeta("#im", 1, 300L);
		});
		// {"metas":{"#im:m":{"t":"i","d":[{"i":0,"v":0},{"i":1,"v":400}]}},"timeline":{}}
		assertHash("b7e86f46e8d3687e2bd1c2d7d9fe0626fe5c9facd6ce8eec0766fba016f2ae60", c -> {
			c.setMultipleMeta("#im", 1, 400L);
		});
		// {"metas":{"#ii:i":{"t":"i","d":[{"i":2,"v":500}]}},"timeline":{}}
		assertHash("bf5504eb42938668fd20a68c8ef97b59e774116525139652a05dc3c957a62c83", c -> {
			c.setIndexedMeta("#ii", 2, 500L);
		});
		// {"metas":{"#ii:i":{"t":"i","d":[{"i":2,"v":600}]}},"timeline":{}}
		assertHash("1df77587eca1c909e68b903a5c40e3b1d71a178b7a5f13d664b66e17cfb52265", c -> {
			c.setIndexedMeta("#ii", 2, 600L);
		});
	}

	// 基本フォーマット：メタ情報を含めないとメタ情報の変化ではハッシュ値は変化しないこと
	@Test
	public void testBasic_ExcludeMetas() {
		// {"timeline":{}}
		assertHash("b7ede707f499dfb248b7c676ee0bb8781904a991e414d78f401c86aea1ded11b", false, false, true, c -> {
			c.setSingleMeta("#is", 100L);
		});
		// {"timeline":{}}
		assertHash("b7ede707f499dfb248b7c676ee0bb8781904a991e414d78f401c86aea1ded11b", false, false, true, c -> {
			c.setSingleMeta("#is", 200L);
		});
		// {"timeline":{}}
		assertHash("b7ede707f499dfb248b7c676ee0bb8781904a991e414d78f401c86aea1ded11b", false, false, true, c -> {
			c.setMultipleMeta("#im", 1, 300L);
		});
		// {"timeline":{}}
		assertHash("b7ede707f499dfb248b7c676ee0bb8781904a991e414d78f401c86aea1ded11b", false, false, true, c -> {
			c.setMultipleMeta("#im", 1, 400L);
		});
		// {"timeline":{}}
		assertHash("b7ede707f499dfb248b7c676ee0bb8781904a991e414d78f401c86aea1ded11b", false, false, true, c -> {
			c.setIndexedMeta("#ii", 2, 500L);
		});
		// {"timeline":{}}
		assertHash("b7ede707f499dfb248b7c676ee0bb8781904a991e414d78f401c86aea1ded11b", false, false, true, c -> {
			c.setIndexedMeta("#ii", 2, 600L);
		});
	}

	// 基本フォーマット：タイムラインを含めるとタイムラインの変化でハッシュ値が変化すること
	@Test
	public void testBasic_IncludeTimeline() {
		// {"metas":{},"timeline":{"11:i":{"10":[100]}}}
		assertHash("c63b27f76eed3b5f00405f1e220c46b4027380b1f710aa45786b84401e095277", c -> {
			c.setMeasureValue(CH_I, 10, 100L);
		});
		// {"metas":{},"timeline":{"11:i":{"10":[200]}}}
		assertHash("449fbb0e35841bb286b9ac570182b6a053a794f1da5387295b0e55f8a534bad5", c -> {
			c.setMeasureValue(CH_I, 10, 200L);
		});
		// {"metas":{},"timeline":{"18:a":{"20":[[{"t":96,"v":300}]]}}}
		assertHash("c15ea776ca4997d8e53c41914bd4e831c7d957d122529feacfa514048f1645d2", c -> {
			c.putNote(CH_A, 20, 96.0, 300);
		});
		// {"metas":{},"timeline":{"18:a":{"20":[[{"t":96,"v":400}]]}}}
		assertHash("1b5b727917008e5f719fc49994238b437d661fc04b2e37b13b1cbff4a6cc7bb5", c -> {
			c.putNote(CH_A, 20, 96.0, 400);
		});
	}

	// 基本フォーマット：タイムラインを含めないとタイムラインの変化ではハッシュ値は変化しないこと
	@Test
	public void testBasic_ExcludeTimeline() {
		// {"metas":{}}
		assertHash("767ff1ffad023b7fd98b7af83e321b488e35f9f44a316bd317a0084a7955406c", false, true, false, c -> {
			c.setMeasureValue(CH_I, 10, 100L);
		});
		// {"metas":{}}
		assertHash("767ff1ffad023b7fd98b7af83e321b488e35f9f44a316bd317a0084a7955406c", false, true, false, c -> {
			c.setMeasureValue(CH_I, 10, 200L);
		});
		// {"metas":{}}
		assertHash("767ff1ffad023b7fd98b7af83e321b488e35f9f44a316bd317a0084a7955406c", false, true, false, c -> {
			c.putNote(CH_A, 20, 96.0, 300);
		});
		// {"metas":{}}
		assertHash("767ff1ffad023b7fd98b7af83e321b488e35f9f44a316bd317a0084a7955406c", false, true, false, c -> {
			c.putNote(CH_A, 20, 96.0, 400);
		});
	}

	// メタ情報：名称を変更するとハッシュ値が変化すること
	@Test
	public void testMetas_ChangedName() {
		// {"metas":{"#z0:s":{"t":"i","d":[{"i":0,"v":100}]}},"timeline":{}}
		assertHash("e730c03802632a8f53daa48eb0b77c65472f78c885a92a82c769fc41459b0893",
				genericSpec().addMeta(BmsMeta.single("#z0", BmsType.INTEGER, "0", -1, true)).create(),
				c -> c.setSingleMeta("#z0", 100L));
		// {"metas":{"#z1:s":{"t":"i","d":[{"i":0,"v":100}]}},"timeline":{}}
		assertHash("10691461e70726b74e926ac950becc7acf16ad5e11d882ae8644497c654481e2",
				genericSpec().addMeta(BmsMeta.single("#z1", BmsType.INTEGER, "0", -1, true)).create(),
				c -> c.setSingleMeta("#z1", 100L));
		// {"metas":{"#z0:m":{"t":"i","d":[{"i":0,"v":200}]}},"timeline":{}}
		assertHash("7c7adb7bcbe8137aaaf02af3cc53c5aaee9c78d57dacac6ca6fe850693dae18a",
				genericSpec().addMeta(BmsMeta.multiple("#z0", BmsType.INTEGER, "0", -1, true)).create(),
				c -> c.setMultipleMeta("#z0", 0, 200L));
		// {"metas":{"#z1:m":{"t":"i","d":[{"i":0,"v":200}]}},"timeline":{}}
		assertHash("b655042e4346e14b16dd3ed2341ff268e7c8457c9c7fdf1e9d0492cbf4219d27",
				genericSpec().addMeta(BmsMeta.multiple("#z1", BmsType.INTEGER, "0", -1, true)).create(),
				c -> c.setMultipleMeta("#z1", 0, 200L));
		// {"metas":{"#z0:i":{"t":"i","d":[{"i":0,"v":300}]}},"timeline":{}}
		assertHash("5b6399562cac36f0299373839a6b35bd80e40d2642eeda020aeb26bb1f8fbfd8",
				genericSpec().addMeta(BmsMeta.indexed("#z0", BmsType.INTEGER, "0", -1, true)).create(),
				c -> c.setIndexedMeta("#z0", 0, 300L));
		// {"metas":{"#z1:i":{"t":"i","d":[{"i":0,"v":300}]}},"timeline":{}}
		assertHash("21017abb2174af0b2016bd2c9252b974d5630cb3dfdc7c7a3afce5a1d5b04906",
				genericSpec().addMeta(BmsMeta.indexed("#z1", BmsType.INTEGER, "0", -1, true)).create(),
				c -> c.setIndexedMeta("#z1", 0, 300L));
	}

	// メタ情報：構成単位を変更するとハッシュ値が変化すること
	@Test
	public void testMetas_ChangedUnit() {
		// {"metas":{"#z:s":{"t":"i","d":[{"i":0,"v":100}]}},"timeline":{}}
		assertHash("1e842d102291e2e663c16c3dbb0d62e058d4ed13bae6fa88cd66b93390b7ed5e",
				genericSpec().addMeta(BmsMeta.single("#z", BmsType.INTEGER, "0", -1, true)).create(),
				c -> c.setSingleMeta("#z", 100L));
		// {"metas":{"#z:m":{"t":"i","d":[{"i":0,"v":100}]}},"timeline":{}}
		assertHash("5bff2bb7ddee4b2f808adff6b3fbe2f8042786ed67a7b71155e8ca31d7852082",
				genericSpec().addMeta(BmsMeta.multiple("#z", BmsType.INTEGER, "0", -1, true)).create(),
				c -> c.setMultipleMeta("#z", 0, 100L));
		// {"metas":{"#z:i":{"t":"i","d":[{"i":0,"v":100}]}},"timeline":{}}
		assertHash("8b623a2eabb607e3703b7d482100473c9e63211bd3ed3ade94e7630a1b000183",
				genericSpec().addMeta(BmsMeta.indexed("#z", BmsType.INTEGER, "0", -1, true)).create(),
				c -> c.setIndexedMeta("#z", 0, 100L));
	}

	// メタ情報：データ型を変更するとハッシュ値が変化すること
	@Test
	public void testMetas_ChangedType() {
		// {"metas":{"#z:s":{"t":"i","d":[{"i":0,"v":0}]}},"timeline":{}}
		assertHash("28fab1fb3f59e97dc989c70d8282dda2ba8d64e826a641154f1c5b9ad3b9f488",
				genericSpec().addMeta(BmsMeta.single("#z", BmsType.INTEGER, "0", -1, true)).create(),
				c -> c.setSingleMeta("#z", 0L));
		// {"metas":{"#z:s":{"t":"f","d":[{"i":0,"v":0}]}},"timeline":{}}
		assertHash("f6ea4592468bc74a67e41b25d66aaf6b60e52bbf1b30c5799533f746075b14b6",
				genericSpec().addMeta(BmsMeta.single("#z", BmsType.FLOAT, "0", -1, true)).create(),
				c -> c.setSingleMeta("#z", 0.0));
		// {"metas":{"#z:s":{"t":"s","d":[{"i":0,"v":"0"}]}},"timeline":{}}
		assertHash("5ff5f7683cbd54cadf6c030306a51039de928936ab2dcd311149bae0645db061",
				genericSpec().addMeta(BmsMeta.single("#z", BmsType.STRING, "0", -1, true)).create(),
				c -> c.setSingleMeta("#z", "0"));
		// {"metas":{"#z:s":{"t":"b","d":[{"i":0,"v":0}]}},"timeline":{}}
		assertHash("3e8889a74759774c88cfda4abd4e78525a8c5565a20d7f0972c4a69cf3e88876",
				genericSpec().addMeta(BmsMeta.single("#z", BmsType.BASE, "00", -1, true)).create(),
				c -> c.setSingleMeta("#z", 0L));
		// {"metas":{"#z:s":{"t":"b16","d":[{"i":0,"v":0}]}},"timeline":{}}
		assertHash("a31cd32e148633086ea0c5cfecde091ac58c23a9ef3cd1adf7d079c030b25c86",
				genericSpec().addMeta(BmsMeta.single("#z", BmsType.BASE16, "00", -1, true)).create(),
				c -> c.setSingleMeta("#z", 0L));
		// {"metas":{"#z:s":{"t":"b36","d":[{"i":0,"v":0}]}},"timeline":{}}
		assertHash("4939c2ebebcc9cc85741f33d7c68d6dbf28c2cbba787e10d0d6a98530ff953a7",
				genericSpec().addMeta(BmsMeta.single("#z", BmsType.BASE36, "00", -1, true)).create(),
				c -> c.setSingleMeta("#z", 0L));
		// {"metas":{"#z:s":{"t":"b62","d":[{"i":0,"v":0}]}},"timeline":{}}
		assertHash("c5531f835e4bf83aca65c7d9549ff46262c2f1762ef9c885bf66bdc19a8cab05",
				genericSpec().addMeta(BmsMeta.single("#z", BmsType.BASE62, "00", -1, true)).create(),
				c -> c.setSingleMeta("#z", 0L));
		// {"metas":{"#z:s":{"t":"a","d":[{"i":0,"v":"00"}]}},"timeline":{}}
		assertHash("723d6c50b1cf59c4bf2927ce58f9294d30cac393681defe5279648a3762a93d7",
				genericSpec().addMeta(BmsMeta.single("#z", BmsType.ARRAY, "00", -1, true)).create(),
				c -> c.setSingleMeta("#z", new BmsArray("00", 36)));
		// {"metas":{"#z:s":{"t":"a16","d":[{"i":0,"v":"00"}]}},"timeline":{}}
		assertHash("7aab8701bdb46b4431d8741634ae1f406de297445c61d2cd416c96ff7bbc7e7e",
				genericSpec().addMeta(BmsMeta.single("#z", BmsType.ARRAY16, "00", -1, true)).create(),
				c -> c.setSingleMeta("#z", new BmsArray("00", 16)));
		// {"metas":{"#z:s":{"t":"a36","d":[{"i":0,"v":"00"}]}},"timeline":{}}
		assertHash("b9dd105cd15bcdcb3a4cb2d36c54a62b7333ed9a4a6251d4bcb39a684e28657e",
				genericSpec().addMeta(BmsMeta.single("#z", BmsType.ARRAY36, "00", -1, true)).create(),
				c -> c.setSingleMeta("#z", new BmsArray("00", 36)));
		// {"metas":{"#z:s":{"t":"a62","d":[{"i":0,"v":"00"}]}},"timeline":{}}
		assertHash("b10bc79f4d06c8d97f9457f09624ec232462da5128174a9301d6a40d90f70162",
				genericSpec().addMeta(BmsMeta.single("#z", BmsType.ARRAY62, "00", -1, true)).create(),
				c -> c.setSingleMeta("#z", new BmsArray("00", 62)));
	}

	// メタ情報：単体メタ情報の値を変更するとハッシュ値が変化すること
	@Test
	public void testMetas_Single_ChangedValue() {
		// {"metas":{"#is:s":{"t":"i","d":[{"i":0,"v":1}]}},"timeline":{}}
		assertHash("8056b543f8470ddb4a88d899a92fa60073bae5f1559727701be914965d799af1",
				c -> c.setSingleMeta("#is", 1L));
		// {"metas":{"#is:s":{"t":"i","d":[{"i":0,"v":2}]}},"timeline":{}}
		assertHash("839d9f4ac9dad7f58e8c803e5ff7b0efba9e3bb2d1abfe7c28562fa5dd488341",
				c -> c.setSingleMeta("#is", 2L));
		// {"metas":{"#fs:s":{"t":"f","d":[{"i":0,"v":1.1}]}},"timeline":{}}
		assertHash("81b738e15438cc5bba737f0efc1db34e145ae73388a82dea27fb6c85bd593826",
				c -> c.setSingleMeta("#fs", 1.1));
		// {"metas":{"#fs:s":{"t":"f","d":[{"i":0,"v":1.2}]}},"timeline":{}}
		assertHash("18e31fe299c13330d113540a8bd68830dc305e385f9db651820172e54b1baf3f",
				c -> c.setSingleMeta("#fs", 1.2));
		// {"metas":{"#ss:s":{"t":"s","d":[{"i":0,"v":"str1"}]}},"timeline":{}}
		assertHash("caba76b6d1e41282fb16713cced9abcae80591b54178a63079a6030c12b80c90",
				c -> c.setSingleMeta("#ss", "str1"));
		// {"metas":{"#ss:s":{"t":"s","d":[{"i":0,"v":"str2"}]}},"timeline":{}}
		assertHash("a2a107d06143632c343af8bac83cd390b468fc5674a201a56335379eb24300de",
				c -> c.setSingleMeta("#ss", "str2"));
		// {"metas":{"#bs:s":{"t":"b","d":[{"i":0,"v":1}]}},"timeline":{}}
		assertHash("701b7e5586423e2421d2bdbe5dbe5210b11fe54adf4635d24066f5b64473397b",
				c -> c.setSingleMeta("#bs", 1L));
		// {"metas":{"#bs:s":{"t":"b","d":[{"i":0,"v":2}]}},"timeline":{}}
		assertHash("f39d9598dc7323bad12e65a8fd82cb5ab54f810a2bad435009fe12c7df1ce801",
				c -> c.setSingleMeta("#bs", 2L));
		// {"metas":{"#b16s:s":{"t":"b16","d":[{"i":0,"v":1}]}},"timeline":{}}
		assertHash("2e36b4eed7b9395fb5e5258fe9a0eb090889fdeff6d811316fa7babeaea2e88c",
				c -> c.setSingleMeta("#b16s", 1L));
		// {"metas":{"#b16s:s":{"t":"b16","d":[{"i":0,"v":2}]}},"timeline":{}}
		assertHash("e97877fc915d909177c61266ca7207851342ffe43880d74ccc2d317078b28b04",
				c -> c.setSingleMeta("#b16s", 2L));
		// {"metas":{"#b36s:s":{"t":"b36","d":[{"i":0,"v":1}]}},"timeline":{}}
		assertHash("515945d3376287e93caff592ba27320c5a90627d2f9d4aced31ffe2f5cd63abb",
				c -> c.setSingleMeta("#b36s", 1L));
		// {"metas":{"#b36s:s":{"t":"b36","d":[{"i":0,"v":2}]}},"timeline":{}}
		assertHash("92eb47f47ac2d470e99d56994178684b3eefaa69af3c5a74ba0d2e11ae85b1ef",
				c -> c.setSingleMeta("#b36s", 2L));
		// {"metas":{"#b62s:s":{"t":"b62","d":[{"i":0,"v":1}]}},"timeline":{}}
		assertHash("f8d8082ae361ce22a7a3db692dddeb7dc6c5b680cddc90f1aa8425581e1675be",
				c -> c.setSingleMeta("#b62s", 1L));
		// {"metas":{"#b62s:s":{"t":"b62","d":[{"i":0,"v":2}]}},"timeline":{}}
		assertHash("779fb420276aab6778da260ecd672409a1f2952ec527b1ba354e4f9b0c29350f",
				c -> c.setSingleMeta("#b62s", 2L));
		// {"metas":{"#as:s":{"t":"a","d":[{"i":0,"v":"01"}]}},"timeline":{}}
		assertHash("0df9a27ce905368245710a26ffd199594f36cd8a64c94561eb3f10dd093aaf45",
				c -> c.setSingleMeta("#as", new BmsArray("01", 36)));
		// {"metas":{"#as:s":{"t":"a","d":[{"i":0,"v":"02"}]}},"timeline":{}}
		assertHash("442fea035f554ba51a976acee0dbcf62b523a5413a0c8adc6fdb61e9b570abac",
				c -> c.setSingleMeta("#as", new BmsArray("02", 36)));
		// {"metas":{"#a16s:s":{"t":"a16","d":[{"i":0,"v":"01"}]}},"timeline":{}}
		assertHash("c5a3f964acc27a6541e63ba294c34e8a3b4df0156213bab48f3e7939ddb4d1c1",
				c -> c.setSingleMeta("#a16s", new BmsArray("01", 16)));
		// {"metas":{"#a16s:s":{"t":"a16","d":[{"i":0,"v":"02"}]}},"timeline":{}}
		assertHash("b7d285cb16f09de151f0fea3c2c7029f5506dccd8edd41628f3f746c505e1b6c",
				c -> c.setSingleMeta("#a16s", new BmsArray("02", 16)));
		// {"metas":{"#a36s:s":{"t":"a36","d":[{"i":0,"v":"01"}]}},"timeline":{}}
		assertHash("2b71c64b47b5b993c8b80f871d2e6ae9749efbca6fcfee2e96158b23a380a43a",
				c -> c.setSingleMeta("#a36s", new BmsArray("01", 36)));
		// {"metas":{"#a36s:s":{"t":"a36","d":[{"i":0,"v":"02"}]}},"timeline":{}}
		assertHash("88525651f80e5210920705b9795b3a29cda29b7da93b8d85125e7c44d6724f44",
				c -> c.setSingleMeta("#a36s", new BmsArray("02", 36)));
		// {"metas":{"#a62s:s":{"t":"a62","d":[{"i":0,"v":"01"}]}},"timeline":{}}
		assertHash("3c6ba835e04de6b2d2c8c655af2e6d389916d51d8e64860373bf80f79aa5e8e5",
				c -> c.setSingleMeta("#a62s", new BmsArray("01", 62)));
		// {"metas":{"#a62s:s":{"t":"a62","d":[{"i":0,"v":"02"}]}},"timeline":{}}
		assertHash("72cfef7e556ed51d6467d91614b59933b989b748c9d2f161e9bbd5557eee8e63",
				c -> c.setSingleMeta("#a62s", new BmsArray("02", 62)));
	}

	// メタ情報：複数メタ情報の値を変更するとハッシュ値が変化すること
	@Test
	public void testMetas_Multiple_ChangedValue() {
		// {"metas":{"#im:m":{"t":"i","d":[{"i":0,"v":1}]}},"timeline":{}}
		assertHash("39dd95319de62d9bbab22e9032255b47ed9b94d59f1f3f7b9ee7199c3c531813",
				c -> c.setMultipleMeta("#im", 0, 1L));
		// {"metas":{"#im:m":{"t":"i","d":[{"i":0,"v":2}]}},"timeline":{}}
		assertHash("e46cef7fc4249da5605d25d2d1ec0e950d7d56574a544d4303d23c77b3971753",
				c -> c.setMultipleMeta("#im", 0, 2L));
		// {"metas":{"#fm:m":{"t":"f","d":[{"i":0,"v":1.1}]}},"timeline":{}}
		assertHash("86909e4f499661639578d4c0ac7c0254c9dead9cdc7f83bf1e7e2c3a75356a1f",
				c -> c.setMultipleMeta("#fm", 0, 1.1));
		// {"metas":{"#fm:m":{"t":"f","d":[{"i":0,"v":1.2}]}},"timeline":{}}
		assertHash("c32143a18be17d626c8db30b4ba1b2812c70fa419252958140291858e9ebc212",
				c -> c.setMultipleMeta("#fm", 0, 1.2));
		// {"metas":{"#sm:m":{"t":"s","d":[{"i":0,"v":"str1"}]}},"timeline":{}}
		assertHash("cf45a5fe87ed0a1139d11d532e5ecc51412f852559b808fcd8fc05d774e093c1",
				c -> c.setMultipleMeta("#sm", 0, "str1"));
		// {"metas":{"#sm:m":{"t":"s","d":[{"i":0,"v":"str2"}]}},"timeline":{}}
		assertHash("9287f59cb2c6cad9b61d1713c1078272f720ddce0a1adc4b60b6c414102d7735",
				c -> c.setMultipleMeta("#sm", 0, "str2"));
		// {"metas":{"#bm:m":{"t":"b","d":[{"i":0,"v":1}]}},"timeline":{}}
		assertHash("01c7f677adc2574783d2868fa1d88150f078639f5a7429ec2dae1d40ce3a973c",
				c -> c.setMultipleMeta("#bm", 0, 1L));
		// {"metas":{"#bm:m":{"t":"b","d":[{"i":0,"v":2}]}},"timeline":{}}
		assertHash("02fc0ac2c88e8943bd7c0d9c6050e44fd8fcc0bc9edd440c2a76de3fa093748d",
				c -> c.setMultipleMeta("#bm", 0, 2L));
		// {"metas":{"#b16m:m":{"t":"b16","d":[{"i":0,"v":1}]}},"timeline":{}}
		assertHash("c9c949f2b07a17fe4d6df64165fad5bf24ca71acaabb97b61d07741f0f8857a5",
				c -> c.setMultipleMeta("#b16m", 0, 1L));
		// {"metas":{"#b16m:m":{"t":"b16","d":[{"i":0,"v":2}]}},"timeline":{}}
		assertHash("d93aec0eaa311367d7fa2311c4d043d11536c895fc3e346a2dafb2b9143ba1a8",
				c -> c.setMultipleMeta("#b16m", 0, 2L));
		// {"metas":{"#b36m:m":{"t":"b36","d":[{"i":0,"v":1}]}},"timeline":{}}
		assertHash("f88d2d1292d07b505004cedca59c1e4662d8400f38a17b5968de2f735ecd3364",
				c -> c.setMultipleMeta("#b36m", 0, 1L));
		// {"metas":{"#b36m:m":{"t":"b36","d":[{"i":0,"v":2}]}},"timeline":{}}
		assertHash("cce105aebcc5e37273ddb1c04793b7a47330c0a137ad5ddc295d4536ef030f62",
				c -> c.setMultipleMeta("#b36m", 0, 2L));
		// {"metas":{"#b62m:m":{"t":"b62","d":[{"i":0,"v":1}]}},"timeline":{}}
		assertHash("d702c6fd8f758a4b829b1d7fe88a917c3d787849993fce3f3d2c92dd80db22a3",
				c -> c.setMultipleMeta("#b62m", 0, 1L));
		// {"metas":{"#b62m:m":{"t":"b62","d":[{"i":0,"v":2}]}},"timeline":{}}
		assertHash("86217b1c42af0996ffe6159d22fb63ea01ebd38e5877bec184ef612452cd931b",
				c -> c.setMultipleMeta("#b62m", 0, 2L));
		// {"metas":{"#am:m":{"t":"a","d":[{"i":0,"v":"01"}]}},"timeline":{}}
		assertHash("871b6a29cab7f832f1645c91ea8dc43ee34a16c5d8d7a6aef37afcbe026efa94",
				c -> c.setMultipleMeta("#am", 0, new BmsArray("01", 36)));
		// {"metas":{"#am:m":{"t":"a","d":[{"i":0,"v":"02"}]}},"timeline":{}}
		assertHash("91873cbaacd596659878b62ca6f980fe5108382184a7d0d609ae30b911d4508c",
				c -> c.setMultipleMeta("#am", 0, new BmsArray("02", 36)));
		// {"metas":{"#a16m:m":{"t":"a16","d":[{"i":0,"v":"01"}]}},"timeline":{}}
		assertHash("4a34bea90cc70c47abf0f6b688a63539d4625cfd5c90dbe9aa54d29549c630bb",
				c -> c.setMultipleMeta("#a16m", 0, new BmsArray("01", 16)));
		// {"metas":{"#a16m:m":{"t":"a16","d":[{"i":0,"v":"02"}]}},"timeline":{}}
		assertHash("84b01dec23b6933a9d66b9ef5223f53a363a9265cf87ed81ff63a75522d87ae6",
				c -> c.setMultipleMeta("#a16m", 0, new BmsArray("02", 16)));
		// {"metas":{"#a36m:m":{"t":"a36","d":[{"i":0,"v":"01"}]}},"timeline":{}}
		assertHash("4382200f6cdad67c920439f988e59af2865bbaa4d6969a38799055550d32d36c",
				c -> c.setMultipleMeta("#a36m", 0, new BmsArray("01", 36)));
		// {"metas":{"#a36m:m":{"t":"a36","d":[{"i":0,"v":"02"}]}},"timeline":{}}
		assertHash("9dd63ec2d89f0944aa4f9cd8082a4522ea704949085e5f772f73ed345ff9471f",
				c -> c.setMultipleMeta("#a36m", 0, new BmsArray("02", 36)));
		// {"metas":{"#a62m:m":{"t":"a62","d":[{"i":0,"v":"01"}]}},"timeline":{}}
		assertHash("f8c876af04c7b994b519e1612909216d49ebfdaec241ffe877edd26c8893f5a0",
				c -> c.setMultipleMeta("#a62m", 0, new BmsArray("01", 62)));
		// {"metas":{"#a62m:m":{"t":"a62","d":[{"i":0,"v":"02"}]}},"timeline":{}}
		assertHash("d10c3a30e67acf29f199bdac55395e142fde91bf57025430dc48b4dabc4134df",
				c -> c.setMultipleMeta("#a62m", 0, new BmsArray("02", 62)));
	}

	// メタ情報：複数メタ情報で値が複数あり、2つの値の位置が入れ替わってもハッシュ値が変化すること
	@Test
	public void testMetas_Multiple_SwappedValue() {
		// {"metas":{"#im:m":{"t":"i","d":[{"i":0,"v":100},{"i":1,"v":0},{"i":2,"v":200}]}},"timeline":{}}
		assertHash("4ae110a2984528328cf79ae4828ecc9707e37649c4a344cdf3144375779ae73e", c -> {
			c.setMultipleMeta("#im", 0, 100L);
			c.setMultipleMeta("#im", 2, 200L);
		});
		// {"metas":{"#im:m":{"t":"i","d":[{"i":0,"v":200},{"i":1,"v":0},{"i":2,"v":100}]}},"timeline":{}}
		assertHash("03491eeedd8fa5d07c645941ae1b2676f951eab3e73799db9c83d64b58cbf999", c -> {
			c.setMultipleMeta("#im", 0, 200L);
			c.setMultipleMeta("#im", 2, 100L);
		});
	}

	// メタ情報：索引付きメタ情報の値を変更するとハッシュ値が変化すること
	@Test
	public void testMetas_Indexed_ChangedValue() {
		// {"metas":{"#ii:i":{"t":"i","d":[{"i":0,"v":1}]}},"timeline":{}}
		assertHash("265260bdcf0b9e1dc0b9606c7dce26e259db63b4c2bf0fb640f512a1adc07a76",
				c -> c.setIndexedMeta("#ii", 0, 1L));
		// {"metas":{"#ii:i":{"t":"i","d":[{"i":0,"v":2}]}},"timeline":{}}
		assertHash("f214a9bccc74fab36b9965f139f3762ca554ecc15b7e8602b3622c8e410676f6",
				c -> c.setIndexedMeta("#ii", 0, 2L));
		// {"metas":{"#fi:i":{"t":"f","d":[{"i":0,"v":1.1}]}},"timeline":{}}
		assertHash("dcdc82eb8468e58f40c943701158a8b387ec48d3b231c413228017e8e5c1a405",
				c -> c.setIndexedMeta("#fi", 0, 1.1));
		// {"metas":{"#fi:i":{"t":"f","d":[{"i":0,"v":1.2}]}},"timeline":{}}
		assertHash("6e18618e0241b2bf1ebeff0d84d9bb0d1aae3c9c5a8b57b33d0fb4d9bf4888ca",
				c -> c.setIndexedMeta("#fi", 0, 1.2));
		// {"metas":{"#si:i":{"t":"s","d":[{"i":0,"v":"str1"}]}},"timeline":{}}
		assertHash("f8be3891e9f46385e49a9356b3ca167688a53b454a6c7754f5276a0d9a77cc47",
				c -> c.setIndexedMeta("#si", 0, "str1"));
		// {"metas":{"#si:i":{"t":"s","d":[{"i":0,"v":"str2"}]}},"timeline":{}}
		assertHash("54608c5bfd14246899e2407d489310f47dcfd3fcef82f8e24860a8f6dad8d304",
				c -> c.setIndexedMeta("#si", 0, "str2"));
		// {"metas":{"#bi:i":{"t":"b","d":[{"i":0,"v":1}]}},"timeline":{}}
		assertHash("bbdc732d1141c1a33cfd8a40b17451a744bd32fd4b9bb587e0baa1872bcf90e6",
				c -> c.setIndexedMeta("#bi", 0, 1L));
		// {"metas":{"#bi:i":{"t":"b","d":[{"i":0,"v":2}]}},"timeline":{}}
		assertHash("36263aafc96fab125a352c749f59216675f43dd569ccb3b41167766ba057ea56",
				c -> c.setIndexedMeta("#bi", 0, 2L));
		// {"metas":{"#b16i:i":{"t":"b16","d":[{"i":0,"v":1}]}},"timeline":{}}
		assertHash("5b44eb9b046d6bf4d771d0f10ba964043cacb316f317d35c71a0171d68d2bc0a",
				c -> c.setIndexedMeta("#b16i", 0, 1L));
		// {"metas":{"#b16i:i":{"t":"b16","d":[{"i":0,"v":2}]}},"timeline":{}}
		assertHash("81b7ee6d40b6d6a36ff43ff70b8f97597d2b782d52b0ddb720f77affdaec8e62",
				c -> c.setIndexedMeta("#b16i", 0, 2L));
		// {"metas":{"#b36i:i":{"t":"b36","d":[{"i":0,"v":1}]}},"timeline":{}}
		assertHash("a96cb1104feaf5d1a1e2a22d6534fbe843511c07a427e6625fadd062a4abad3c",
				c -> c.setIndexedMeta("#b36i", 0, 1L));
		// {"metas":{"#b36i:i":{"t":"b36","d":[{"i":0,"v":2}]}},"timeline":{}}
		assertHash("bfc3a79e748d6ec177492b6c83d66f1a504a5867dcf094b5363e4db51c5cf707",
				c -> c.setIndexedMeta("#b36i", 0, 2L));
		// {"metas":{"#b62i:i":{"t":"b62","d":[{"i":0,"v":1}]}},"timeline":{}}
		assertHash("9c935f09b37ca916c4ccd72c8e3308252403817e2cbf8b517aff46e2b2162e1c",
				c -> c.setIndexedMeta("#b62i", 0, 1L));
		// {"metas":{"#b62i:i":{"t":"b62","d":[{"i":0,"v":2}]}},"timeline":{}}
		assertHash("de95dc9a37e38fb4f4489fc4e23c3d8c425ae2dfdf7fa2495080030b0d29e91d",
				c -> c.setIndexedMeta("#b62i", 0, 2L));
		// {"metas":{"#ai:i":{"t":"a","d":[{"i":0,"v":"01"}]}},"timeline":{}}
		assertHash("2d0c931969f1b6e26fb12300017fa3e582cf262a049df2fbb3262458797efe1b",
				c -> c.setIndexedMeta("#ai", 0, new BmsArray("01", 36)));
		// {"metas":{"#ai:i":{"t":"a","d":[{"i":0,"v":"02"}]}},"timeline":{}}
		assertHash("c336440b9d780dc12e139b7ff3562d9d15d42a0175a585d2bb39f30a043ad70a",
				c -> c.setIndexedMeta("#ai", 0, new BmsArray("02", 36)));
		// {"metas":{"#a16i:i":{"t":"a16","d":[{"i":0,"v":"01"}]}},"timeline":{}}
		assertHash("4267bc143d9903e83f4f3bfd08ff3e9bffbc6e8062343a2ed739ef392a033e0e",
				c -> c.setIndexedMeta("#a16i", 0, new BmsArray("01", 16)));
		// {"metas":{"#a16i:i":{"t":"a16","d":[{"i":0,"v":"02"}]}},"timeline":{}}
		assertHash("6a4d6ab2b64414c1378a48579b53d341d6f04675dfe61e9d0e3af2cf596fce3f",
				c -> c.setIndexedMeta("#a16i", 0, new BmsArray("02", 16)));
		// {"metas":{"#a36i:i":{"t":"a36","d":[{"i":0,"v":"01"}]}},"timeline":{}}
		assertHash("9e4324b10ddba12e20c0de49c272b8d0a00fadbbdd3ec91f772b5822bde995fd",
				c -> c.setIndexedMeta("#a36i", 0, new BmsArray("01", 36)));
		// {"metas":{"#a36i:i":{"t":"a36","d":[{"i":0,"v":"02"}]}},"timeline":{}}
		assertHash("70fbd03d21a3ceefa6526cab76425d1877bb42d06fb3c17576f0a12955b921d4",
				c -> c.setIndexedMeta("#a36i", 0, new BmsArray("02", 36)));
		// {"metas":{"#a62i:i":{"t":"a62","d":[{"i":0,"v":"01"}]}},"timeline":{}}
		assertHash("102738276839fd2d63b9b8f9f09fb4b29bebb87b80aedd21a5193134aaa16ba0",
				c -> c.setIndexedMeta("#a62i", 0, new BmsArray("01", 62)));
		// {"metas":{"#a62i:i":{"t":"a62","d":[{"i":0,"v":"02"}]}},"timeline":{}}
		assertHash("1608d535d7e436278cb27abc719ea76615bcce040094601bda096a203898aa50",
				c -> c.setIndexedMeta("#a62i", 0, new BmsArray("02", 62)));
	}

	// メタ情報：索引付きメタ情報の1つの値のインデックス値が異なるだけでハッシュ値が変化すること
	@Test
	public void testMetas_Indexed_ChangedIndex() {
		// {"metas":{"#fi:i":{"t":"f","d":[{"i":0,"v":1.234},{"i":3,"v":5.678}]}},"timeline":{}}
		assertHash("27f910ba19b4609cc5c2417f94ad09072e9017e22e0e8a5d05c7345435c0bfd5", c -> {
			c.setIndexedMeta("#fi", 0, 1.234);
			c.setIndexedMeta("#fi", 3, 5.678);
		});
		// {"metas":{"#fi:i":{"t":"f","d":[{"i":0,"v":1.234},{"i":5,"v":5.678}]}},"timeline":{}}
		assertHash("041dab2cff242ebde620f95ad07dafac623108a7928f75f46099de1723569224", c -> {
			c.setIndexedMeta("#fi", 0, 1.234);
			c.setIndexedMeta("#fi", 5, 5.678);
		});
	}

	// メタ情報：索引付きメタ情報で値が複数あり、2つの値の位置が入れ替わってもハッシュ値が変化すること
	@Test
	public void testMetas_Indexed_SwappedValue() {
		// {"metas":{"#si:i":{"t":"s","d":[{"i":1,"v":"str1"},{"i":3,"v":"str2"}]}},"timeline":{}}
		assertHash("58757f7b5a930287cd7dc471fde6aedc3301fb72849cc67d827aee9135299ec2", c -> {
			c.setIndexedMeta("#si", 1, "str1");
			c.setIndexedMeta("#si", 3, "str2");
		});
		// {"metas":{"#si:i":{"t":"s","d":[{"i":1,"v":"str2"},{"i":3,"v":"str1"}]}},"timeline":{}}
		assertHash("5f9e67f66fe8c2095ae3a7421d8a6bcc38417d8c732952ec73333638333f1f20", c -> {
			c.setIndexedMeta("#si", 1, "str2");
			c.setIndexedMeta("#si", 3, "str1");
		});
	}

	// メタ情報：メタ情報のソートキーが変化し順序が入れ替わるとハッシュ値が変化すること
	@Test
	public void testMetas_ChangedSortKey() {
		// {"metas":{"#z:s":{"t":"i","d":[{"i":0,"v":100}]},"#is:s":{"t":"i","d":[{"i":0,"v":100}]}},"timeline":{}}
		var spec1 = genericSpec().addMeta(BmsMeta.single("#z", BmsType.INTEGER, "0", -1, true)).create();
		assertHash("3b3cbfca95f459b40955c7eb2bc89d6f0e76af18d7fc635ec18c8f0ce2c1e586", spec1, c -> {
			c.setSingleMeta("#is", 100L);
			c.setSingleMeta("#z", 100L);
		});
		// {"metas":{"#is:s":{"t":"i","d":[{"i":0,"v":100}]},"#z:s":{"t":"i","d":[{"i":0,"v":100}]}},"timeline":{}}
		var spec2 = genericSpec().addMeta(BmsMeta.single("#z", BmsType.INTEGER, "0", Integer.MAX_VALUE, true)).create();
		assertHash("c84c7dca610f91072d30f7c314182fbaa5d501a33641f97d2f9c89e59920608f", spec2, c -> {
			c.setSingleMeta("#is", 100L);
			c.setSingleMeta("#z", 100L);
		});
	}

	// メタ情報：メタ情報の同一性チェックがOFF→ONになり値が設定されるとハッシュ値が変化すること
	@Test
	public void testMetas_ChangedUniqueness() {
		// {"metas":{"#is:s":{"t":"i","d":[{"i":0,"v":100}]}},"timeline":{}}
		var spec1 = genericSpec().addMeta(BmsMeta.single("#z", BmsType.INTEGER, "0", -1, false)).create();
		assertHash("510ef3f5c2f7cdc0021da3c0e19e78496c1bb939eab8ebafe9adcc318eb97f91", spec1, c -> {
			c.setSingleMeta("#is", 100L);
			c.setSingleMeta("#z", 100L);
		});
		// {"metas":{"#z:s":{"t":"i","d":[{"i":0,"v":100}]},"#is:s":{"t":"i","d":[{"i":0,"v":100}]}},"timeline":{}}
		var spec2 = genericSpec().addMeta(BmsMeta.single("#z", BmsType.INTEGER, "0", -1, true)).create();
		assertHash("3b3cbfca95f459b40955c7eb2bc89d6f0e76af18d7fc635ec18c8f0ce2c1e586", spec2, c -> {
			c.setSingleMeta("#is", 100L);
			c.setSingleMeta("#z", 100L);
		});
	}

	// メタ情報：同一性チェックがOFFのメタ情報を変更してもハッシュ値は変化しないこと
	@Test
	public void testMetas_UniquenessOff() {
		// {"metas":{"#fs:s":{"t":"f","d":[{"i":0,"v":1.0123456789}]}},"timeline":{}}
		var spec = genericSpec().addMeta(BmsMeta.single("#z", BmsType.STRING, "", -1, false)).create();
		assertHash("df3e5f9d50b5d215b851d44bb3c18c0bc321dd04051b581721422a21da80944b", spec, c -> {
			c.setSingleMeta("#fs", 1.0123456789);
			c.setSingleMeta("#z", "str1");
		});
		// {"metas":{"#fs:s":{"t":"f","d":[{"i":0,"v":1.0123456789}]}},"timeline":{}}
		assertHash("df3e5f9d50b5d215b851d44bb3c18c0bc321dd04051b581721422a21da80944b", spec, c -> {
			c.setSingleMeta("#fs", 1.0123456789);
			c.setSingleMeta("#z", "str2");
		});
	}

	// メタ情報：任意型メタ情報の値を変更してもハッシュ値は変化しないこと
	@Test
	public void testMetas_ObjectMeta() {
		// {"metas":{"#bs:s":{"t":"b","d":[{"i":0,"v":1}]}},"timeline":{}}
		assertHash("701b7e5586423e2421d2bdbe5dbe5210b11fe54adf4635d24066f5b64473397b", c -> {
			c.setSingleMeta("#bs", 1L);
			c.setSingleMeta("#osx", "obj1");
		});
		// {"metas":{"#bs:s":{"t":"b","d":[{"i":0,"v":1}]}},"timeline":{}}
		assertHash("701b7e5586423e2421d2bdbe5dbe5210b11fe54adf4635d24066f5b64473397b", c -> {
			c.setSingleMeta("#bs", 1L);
			c.setSingleMeta("#osx", "obj2");
		});
	}

	// タイムライン：チャンネル番号を変更するとハッシュ値が変化すること
	@Test
	public void testTimeline_ChangedNumber() {
		// {"metas":{},"timeline":{"ZY:i":{"50":[100]}}}
		assertHash("10981c3ed7668ca718277172f5063f3c65657073b925c50aa5ccb5ec9dae7c55",
				genericSpec().addChannel(BmsChannel.spec(CH_ZY, BmsType.INTEGER, null, "0", false, true)).create(),
				c -> c.setMeasureValue(CH_ZY, 50, 100L));
		// {"metas":{},"timeline":{"ZZ:i":{"50":[100]}}}
		assertHash("c1c58613f855146a24e38cf6794309a12e1cd499de8522c995849fc3bb05d711",
				genericSpec().addChannel(BmsChannel.spec(CH_ZZ, BmsType.INTEGER, null, "0", false, true)).create(),
				c -> c.setMeasureValue(CH_ZZ, 50, 100L));
		// {"metas":{},"timeline":{"ZY:a":{"50":[[{"t":96,"v":100}]]}}}
		assertHash("204ca75d73644fabbc45e8221695f80bbcb26a408acce16762d700e5d707071e",
				genericSpec().addChannel(BmsChannel.spec(CH_ZY, BmsType.ARRAY, null, "00", false, true)).create(),
				c -> c.putNote(CH_ZY, 50, 96.0, 100));
		// {"metas":{},"timeline":{"ZZ:a":{"50":[[{"t":96,"v":100}]]}}}
		assertHash("95213985c7c3307f71f749ab6c25242de6bae873e891eebdbd631df3efedbbc1",
				genericSpec().addChannel(BmsChannel.spec(CH_ZZ, BmsType.ARRAY, null, "00", false, true)).create(),
				c -> c.putNote(CH_ZZ, 50, 96.0, 100));
	}

	// タイムライン：値型の値を変更するとハッシュ値が変化すること
	@Test
	public void testTimeline_ValueType_ChangedValue() {
		// {"metas":{},"timeline":{"11:i":{"10":[1]}}}
		assertHash("6b4e71a8a2cedfa1d760d8e9b002fb54d276edea0365d5822fd15fa1434f71ff",
				c -> c.setMeasureValue(CH_I, 10, 1L));
		// {"metas":{},"timeline":{"11:i":{"10":[2]}}}
		assertHash("f614c6afb1258e4b8a4c6f454a996ad1229e46089825b36e3beaaa0efb2cd07d",
				c -> c.setMeasureValue(CH_I, 10, 2L));
		// {"metas":{},"timeline":{"12:f":{"20":[1.1]}}}
		assertHash("beecdace4f138aaf17c84bbb5382f2497004b045c95e7f39cf1cb166815095e0",
				c -> c.setMeasureValue(CH_F, 20, 1.1));
		// {"metas":{},"timeline":{"12:f":{"20":[1.2]}}}
		assertHash("3c7a48ae3dafa53c28925f779b74f67bc3c70f5ab8301f379b8adc72547d60de",
				c -> c.setMeasureValue(CH_F, 20, 1.2));
		// {"metas":{},"timeline":{"13:s":{"30":["str1"]}}}
		assertHash("7c9ae00cc5422f51f3108f4b7ed647b9983dc11314eff0cc5ee6044b4253ba42",
				c -> c.setMeasureValue(CH_S, 30, "str1"));
		// {"metas":{},"timeline":{"13:s":{"30":["str2"]}}}
		assertHash("909169610c12fde391fb81535568983582e46e3b8b816a5761d8adc1cb227d4d",
				c -> c.setMeasureValue(CH_S, 30, "str2"));
		// {"metas":{},"timeline":{"14:b":{"40":[1]}}}
		assertHash("e524c19514ad91f7a130dbd99d9ecbed9e2945b72a123fa6bb97777ab482d22c",
				c -> c.setMeasureValue(CH_B, 40, 1L));
		// {"metas":{},"timeline":{"14:b":{"40":[2]}}}
		assertHash("97c74a51f7a97db3530c453977302297b8afd5fb14d510f7aae15a4f49475639",
				c -> c.setMeasureValue(CH_B, 40, 2L));
		// {"metas":{},"timeline":{"15:b16":{"50":[1]}}}
		assertHash("6f9a2d642a8452584f12f9e5f2448756e4739498a5b63116c865a4d4c710d276",
				c -> c.setMeasureValue(CH_B16, 50, 1L));
		// {"metas":{},"timeline":{"15:b16":{"50":[2]}}}
		assertHash("31533305be603be215af3587a0fbc2e33db55cdf467b1929804b312c6de205e1",
				c -> c.setMeasureValue(CH_B16, 50, 2L));
		// {"metas":{},"timeline":{"16:b36":{"60":[1]}}}
		assertHash("e044a3483a148e6232b6e419740d7071d2e851b656457bdca1d124aa4c2a8853",
				c -> c.setMeasureValue(CH_B36, 60, 1L));
		// {"metas":{},"timeline":{"16:b36":{"60":[2]}}}
		assertHash("16dd2c76ec73f8052157ed9b3bf7b843a7090d9077365653c45717ef89c182ab",
				c -> c.setMeasureValue(CH_B36, 60, 2L));
		// {"metas":{},"timeline":{"17:b62":{"70":[1]}}}
		assertHash("196019a94d9ab5e68e4c2801d3268f7b545d66973fffd1f5e16d822ce58ad20d",
				c -> c.setMeasureValue(CH_B62, 70, 1L));
		// {"metas":{},"timeline":{"17:b62":{"70":[2]}}}
		assertHash("79f8bbe3ad95f470827447fe04e2f4dbe7939941df54f6e361ac9adfb1460e86",
				c -> c.setMeasureValue(CH_B62, 70, 2L));
	}

	// タイムライン：値型の値のインデックスを変更するとハッシュ値が変化すること
	@Test
	public void testTimeline_ValueType_ChangedIndex() {
		// {"metas":{},"timeline":{"21:i":{"10":[0,1]}}}
		assertHash("5998071e786b3a4591b9b755db07469827c2aebf56b0760fd0ed1d25938c25da",
				c -> c.setMeasureValue(CH_IM, 1, 10, 1L));
		// {"metas":{},"timeline":{"21:i":{"10":[0,0,1]}}}
		assertHash("00e49653608878bba6b68859910222428dae6f9b8d326b76b30853371b051f77",
				c -> c.setMeasureValue(CH_IM, 2, 10, 1L));
		// {"metas":{},"timeline":{"22:f":{"20":[0,1.1]}}}
		assertHash("be87c27b11402ce22e56dea011cb20e4a9f65f5d95fbfc6fbacb0bf5b2ea6fcc",
				c -> c.setMeasureValue(CH_FM, 1, 20, 1.1));
		// {"metas":{},"timeline":{"22:f":{"20":[0,0,1.1]}}}
		assertHash("59c3475452ff9526a0a8349435dc3fa7f9fa8db3c3543ac4b6c6a874d5c81b9c",
				c -> c.setMeasureValue(CH_FM, 2, 20, 1.1));
		// {"metas":{},"timeline":{"23:s":{"30":["","str"]}}}
		assertHash("8ebf65ca8b7a45c0e329aa1dc89e4e554245775fcd89e09eb3057736582e8eef",
				c -> c.setMeasureValue(CH_SM, 1, 30, "str"));
		// {"metas":{},"timeline":{"23:s":{"30":["","","str"]}}}
		assertHash("4e70b49a94fbbaa69d5bc0f16947be10880ffbc11e8b5c1caee5ca25865eb96e",
				c -> c.setMeasureValue(CH_SM, 2, 30, "str"));
		// {"metas":{},"timeline":{"24:b":{"40":[0,1]}}}
		assertHash("9c18fa95ac940209a671a9411f531e7b4647842f42af1f781c3cd8e552f7cb06",
				c -> c.setMeasureValue(CH_BM, 1, 40, 1L));
		// {"metas":{},"timeline":{"24:b":{"40":[0,0,1]}}}
		assertHash("7338e8bf9541a0653fab61ffcae81244641aca587869c6038cfbde510482e93b",
				c -> c.setMeasureValue(CH_BM, 2, 40, 1L));
		// {"metas":{},"timeline":{"25:b16":{"50":[0,1]}}}
		assertHash("a6549b3744f8c248b7f60feede1c026c669a9255e082522f5abd60a7478a5371",
				c -> c.setMeasureValue(CH_B16M, 1, 50, 1L));
		// {"metas":{},"timeline":{"25:b16":{"50":[0,0,1]}}}
		assertHash("dff5b26f0e973c043b357c9a224b14d19e05bcc046b54561f6fda23ed01778a3",
				c -> c.setMeasureValue(CH_B16M, 2, 50, 1L));
		// {"metas":{},"timeline":{"26:b36":{"60":[0,1]}}}
		assertHash("ed683bcce51fb3ce8cfd018314ef99dea5285726a2febe5ca031d3f00d2b87e3",
				c -> c.setMeasureValue(CH_B36M, 1, 60, 1L));
		// {"metas":{},"timeline":{"26:b36":{"60":[0,0,1]}}}
		assertHash("4f60e438859a1b7adbfc25e3fc7234e3f47a111d8597568c1750c56f83b8104a",
				c -> c.setMeasureValue(CH_B36M, 2, 60, 1L));
		// {"metas":{},"timeline":{"27:b62":{"70":[0,1]}}}
		assertHash("9c47ad6857146070b9343f7197ac48706920a477ae6233f5517a132275de019a",
				c -> c.setMeasureValue(CH_B62M, 1, 70, 1L));
		// {"metas":{},"timeline":{"27:b62":{"70":[0,0,1]}}}
		assertHash("eb5a6677a7be8b573a3ba3c5dbc8016dd5e19c6946ae9905215eb957c6a93595",
				c -> c.setMeasureValue(CH_B62M, 2, 70, 1L));
	}

	// タイムライン：値型の2つの値の位置が入れ替わるとハッシュ値が変化すること
	@Test
	public void testTimeline_ValueType_SwappedValue() {
		// {"metas":{},"timeline":{"21:i":{"10":[0,1,0,2]}}}
		assertHash("8f80d9ebc64608c94187e80314a673c0baee6da448f78783e0bdd39affbbe932", c -> {
			c.setMeasureValue(CH_IM, 1, 10, 1L);
			c.setMeasureValue(CH_IM, 3, 10, 2L);
		});
		// {"metas":{},"timeline":{"21:i":{"10":[0,2,0,1]}}}
		assertHash("eb4920ebcf835061df721788889a463a9c7fc908e45c284a1ffd771640513951", c -> {
			c.setMeasureValue(CH_IM, 1, 10, 2L);
			c.setMeasureValue(CH_IM, 3, 10, 1L);
		});
		// {"metas":{},"timeline":{"22:f":{"20":[0,1.1,0,1.2]}}}
		assertHash("5b2b392009699e9c337ec092768a9aba718d3b13e8272233a50a664515bca52c", c -> {
			c.setMeasureValue(CH_FM, 1, 20, 1.1);
			c.setMeasureValue(CH_FM, 3, 20, 1.2);
		});
		// {"metas":{},"timeline":{"22:f":{"20":[0,1.2,0,1.1]}}}
		assertHash("638fc73be707fbcbc47aa3aca7d887d4f397ec08e97d0e54947bbaa7a95eae23", c -> {
			c.setMeasureValue(CH_FM, 1, 20, 1.2);
			c.setMeasureValue(CH_FM, 3, 20, 1.1);
		});
		// {"metas":{},"timeline":{"23:s":{"30":["","str1","","str2"]}}}
		assertHash("fc415937b9ff10f0aa296f3c40aab75380d2ca38c42c6aabed224192b2b07abd", c -> {
			c.setMeasureValue(CH_SM, 1, 30, "str1");
			c.setMeasureValue(CH_SM, 3, 30, "str2");
		});
		// {"metas":{},"timeline":{"23:s":{"30":["","str2","","str1"]}}}
		assertHash("e0799fc012ac9ffafd1e314720009a5dd4efbd7ebc09e1661794b9dd209976bc", c -> {
			c.setMeasureValue(CH_SM, 1, 30, "str2");
			c.setMeasureValue(CH_SM, 3, 30, "str1");
		});
		// {"metas":{},"timeline":{"24:b":{"40":[0,1,0,2]}}}
		assertHash("0b450852f189b1eed765e499d4bc42b1b9bf7bc07399cd22d03675bcc5c5ce34", c -> {
			c.setMeasureValue(CH_BM, 1, 40, 1L);
			c.setMeasureValue(CH_BM, 3, 40, 2L);
		});
		// {"metas":{},"timeline":{"24:b":{"40":[0,2,0,1]}}}
		assertHash("5de301c35c9c54c466c1670b887295f96b9051040681a8e56635d2615c4dc926", c -> {
			c.setMeasureValue(CH_BM, 1, 40, 2L);
			c.setMeasureValue(CH_BM, 3, 40, 1L);
		});
		// {"metas":{},"timeline":{"25:b16":{"50":[0,1,0,2]}}}
		assertHash("277d5c81dd2e5cb84207a6561c644936a254692587c254b9d6e3a72d846f50ec", c -> {
			c.setMeasureValue(CH_B16M, 1, 50, 1L);
			c.setMeasureValue(CH_B16M, 3, 50, 2L);
		});
		// {"metas":{},"timeline":{"25:b16":{"50":[0,2,0,1]}}}
		assertHash("3e6b47792d6009e206d599184d6935f81938976d45ca07450167d370ddccd060", c -> {
			c.setMeasureValue(CH_B16M, 1, 50, 2L);
			c.setMeasureValue(CH_B16M, 3, 50, 1L);
		});
		// {"metas":{},"timeline":{"26:b36":{"60":[0,1,0,2]}}}
		assertHash("cfd2e7ba203feb29b4d698165f66864d7c8ac8cbddf7a528a26bb7275aa1da9a", c -> {
			c.setMeasureValue(CH_B36M, 1, 60, 1L);
			c.setMeasureValue(CH_B36M, 3, 60, 2L);
		});
		// {"metas":{},"timeline":{"26:b36":{"60":[0,2,0,1]}}}
		assertHash("e49f29d08d3a61814ca38be25014d1aea21f174f4e1f0d7c5908dba180a4a329", c -> {
			c.setMeasureValue(CH_B36M, 1, 60, 2L);
			c.setMeasureValue(CH_B36M, 3, 60, 1L);
		});
		// {"metas":{},"timeline":{"27:b62":{"70":[0,1,0,2]}}}
		assertHash("69381e6093004f46c7dcd1d0bac56a09fe5e888bb99d03e1f76bbdd5c38754ee", c -> {
			c.setMeasureValue(CH_B62M, 1, 70, 1L);
			c.setMeasureValue(CH_B62M, 3, 70, 2L);
		});
		// {"metas":{},"timeline":{"27:b62":{"70":[0,2,0,1]}}}
		assertHash("7f7fe144237a775d54788b295db23b623791f04e17927bf679e65faa0f187476", c -> {
			c.setMeasureValue(CH_B62M, 1, 70, 2L);
			c.setMeasureValue(CH_B62M, 3, 70, 1L);
		});
	}

	// タイムライン：値型の小節番号がずれるとハッシュ値が変化すること
	@Test
	public void testTimeline_ValueType_OffsetMeasure() {
		// {"metas":{},"timeline":{"11:i":{"10":[1]}}}
		assertHash("6b4e71a8a2cedfa1d760d8e9b002fb54d276edea0365d5822fd15fa1434f71ff",
				c -> c.setMeasureValue(CH_I, 10, 1L));
		// {"metas":{},"timeline":{"11:i":{"15":[1]}}}
		assertHash("dc3009f2f3b53be0bba9b75167269535e6829dfafa690bc15597f8bb258b9f43",
				c -> c.setMeasureValue(CH_I, 15, 1L));
		// {"metas":{},"timeline":{"12:f":{"20":[1.1]}}}
		assertHash("beecdace4f138aaf17c84bbb5382f2497004b045c95e7f39cf1cb166815095e0",
				c -> c.setMeasureValue(CH_F, 20, 1.1));
		// {"metas":{},"timeline":{"12:f":{"25":[1.1]}}}
		assertHash("b8ce2c681ce5240d05be00ed00ae4a3281586a4a8066c7e23540338895b56f1f",
				c -> c.setMeasureValue(CH_F, 25, 1.1));
		// {"metas":{},"timeline":{"13:s":{"30":["str"]}}}
		assertHash("fcd5f6fe98cce36e0423e2ed9c185dbaf9b0fcb66750b7324877e9c568af2ccf",
				c -> c.setMeasureValue(CH_S, 30, "str"));
		// {"metas":{},"timeline":{"13:s":{"35":["str"]}}}
		assertHash("196a8fd55172a08a88fb57b02794079129769480228010e6d3535fb8243eb4cd",
				c -> c.setMeasureValue(CH_S, 35, "str"));
		// {"metas":{},"timeline":{"14:b":{"40":[1]}}}
		assertHash("e524c19514ad91f7a130dbd99d9ecbed9e2945b72a123fa6bb97777ab482d22c",
				c -> c.setMeasureValue(CH_B, 40, 1L));
		// {"metas":{},"timeline":{"14:b":{"45":[1]}}}
		assertHash("18f3c060389ced0f9c9dab6476986a008496537a1f001c43c34fa47a739dbb2c",
				c -> c.setMeasureValue(CH_B, 45, 1L));
		// {"metas":{},"timeline":{"15:b16":{"50":[1]}}}
		assertHash("6f9a2d642a8452584f12f9e5f2448756e4739498a5b63116c865a4d4c710d276",
				c -> c.setMeasureValue(CH_B16, 50, 1L));
		// {"metas":{},"timeline":{"15:b16":{"55":[1]}}}
		assertHash("6c2067f906f43b236fa17de831cae15649b9d0875974670b25bbd055223deecb",
				c -> c.setMeasureValue(CH_B16, 55, 1L));
		// {"metas":{},"timeline":{"16:b36":{"60":[1]}}}
		assertHash("e044a3483a148e6232b6e419740d7071d2e851b656457bdca1d124aa4c2a8853",
				c -> c.setMeasureValue(CH_B36, 60, 1L));
		// {"metas":{},"timeline":{"16:b36":{"65":[1]}}}
		assertHash("255615098695e44c93731ab0b9de51da8c6b26d1f64c125914010ca76de74a5f",
				c -> c.setMeasureValue(CH_B36, 65, 1L));
		// {"metas":{},"timeline":{"17:b62":{"70":[1]}}}
		assertHash("196019a94d9ab5e68e4c2801d3268f7b545d66973fffd1f5e16d822ce58ad20d",
				c -> c.setMeasureValue(CH_B62, 70, 1L));
		// {"metas":{},"timeline":{"17:b62":{"75":[1]}}}
		assertHash("19bf6501a4735f6caf94d89ccd448253542bc35e3dd0f2cde508483876ccb4d3",
				c -> c.setMeasureValue(CH_B62, 75, 1L));
	}

	// タイムライン：配列型の値を変更するとハッシュ値が変化すること
	@Test
	public void testTimeline_ArrayType_ChangedValue() {
		// {"metas":{},"timeline":{"18:a":{"10":[[{"t":48,"v":10},{"t":96,"v":20}]],"20":[[{"t":144,"v":30}]]}}}
		assertHash("28a09d6475ecb8652d00cd8d408f26dfd96ec755c8174ab3bec7a02363470a25", c -> {
			c.putNote(CH_A, 10, 48.0, 10);
			c.putNote(CH_A, 10, 96.0, 20);
			c.putNote(CH_A, 20, 144.0, 30);
		});
		// {"metas":{},"timeline":{"18:a":{"10":[[{"t":48,"v":10},{"t":96,"v":29}]],"20":[[{"t":144,"v":30}]]}}}
		assertHash("9f0da2c2a767acbfc9620cbd63658d5f591e8174649cd834c3b4024896c7f219", c -> {
			c.putNote(CH_A, 10, 48.0, 10);
			c.putNote(CH_A, 10, 96.0, 29);
			c.putNote(CH_A, 20, 144.0, 30);
		});
		// {"metas":{},"timeline":{"19:a16":{"10":[[{"t":48,"v":10},{"t":96,"v":20}]],"20":[[{"t":144,"v":30}]]}}}
		assertHash("7123959841b733ba3ee5ab9f061691395258ce743f3a915d7464c12e4650354d", c -> {
			c.putNote(CH_A16, 10, 48.0, 10);
			c.putNote(CH_A16, 10, 96.0, 20);
			c.putNote(CH_A16, 20, 144.0, 30);
		});
		// {"metas":{},"timeline":{"19:a16":{"10":[[{"t":48,"v":10},{"t":96,"v":29}]],"20":[[{"t":144,"v":30}]]}}}
		assertHash("8bbb0972d39bad7eb090b32a78d057a11327c0364725bbca5ce0fd155fe5e6e7", c -> {
			c.putNote(CH_A16, 10, 48.0, 10);
			c.putNote(CH_A16, 10, 96.0, 29);
			c.putNote(CH_A16, 20, 144.0, 30);
		});
		// {"metas":{},"timeline":{"1A:a36":{"10":[[{"t":48,"v":10},{"t":96,"v":20}]],"20":[[{"t":144,"v":30}]]}}}
		assertHash("0f5816049f22ff043f6db9d0ba20a645c77a78e37e97959060e7fd60a645b0ce", c -> {
			c.putNote(CH_A36, 10, 48.0, 10);
			c.putNote(CH_A36, 10, 96.0, 20);
			c.putNote(CH_A36, 20, 144.0, 30);
		});
		// {"metas":{},"timeline":{"1A:a36":{"10":[[{"t":48,"v":10},{"t":96,"v":29}]],"20":[[{"t":144,"v":30}]]}}}
		assertHash("794079bddb9628d3d03858b5c5b4e6fa1dc150494a99faa42a73ef6dc2260948", c -> {
			c.putNote(CH_A36, 10, 48.0, 10);
			c.putNote(CH_A36, 10, 96.0, 29);
			c.putNote(CH_A36, 20, 144.0, 30);
		});
		// {"metas":{},"timeline":{"1B:a62":{"10":[[{"t":48,"v":10},{"t":96,"v":20}]],"20":[[{"t":144,"v":30}]]}}}
		assertHash("9a546c51481900d271502fee35a6575e7d057d4cb2db39aecd155697807c69c6", c -> {
			c.putNote(CH_A62, 10, 48.0, 10);
			c.putNote(CH_A62, 10, 96.0, 20);
			c.putNote(CH_A62, 20, 144.0, 30);
		});
		// {"metas":{},"timeline":{"1B:a62":{"10":[[{"t":48,"v":10},{"t":96,"v":29}]],"20":[[{"t":144,"v":30}]]}}}
		assertHash("898af97120fa33d40ec718e95b5a5de9732bf6e869823ac58ffef2d31d46e854", c -> {
			c.putNote(CH_A62, 10, 48.0, 10);
			c.putNote(CH_A62, 10, 96.0, 29);
			c.putNote(CH_A62, 20, 144.0, 30);
		});
	}

	// タイムライン：配列型の値の刻み位置を変更するとハッシュ値が変化すること
	@Test
	public void testTimeline_ArrayType_ChangedTick() {
		// {"metas":{},"timeline":{"18:a":{"10":[[{"t":48,"v":10}]],"20":[[{"t":96,"v":20}]]}}}
		assertHash("e67feddd3b93fce4a6e54fa7d5dec3228b0c72ede9ae0b0eacc72ae39bdc96a1", c -> {
			c.putNote(CH_A, 10, 48.0, 10);
			c.putNote(CH_A, 20, 96.0, 20);
		});
		// {"metas":{},"timeline":{"18:a":{"10":[[{"t":48,"v":10}]],"20":[[{"t":96.1,"v":20}]]}}}
		assertHash("f08c4b5d939044d06e69a890fa4ba842723a7ba82736dc4354fcd6b3ef8954ed", c -> {
			c.putNote(CH_A, 10, 48.0, 10);
			c.putNote(CH_A, 20, 96.1, 20);
		});
		// {"metas":{},"timeline":{"19:a16":{"10":[[{"t":48,"v":10}]],"20":[[{"t":96,"v":20}]]}}}
		assertHash("4044e26ded545127ccfd589153280629a1436656f401c48b11a082dd9ba1437f", c -> {
			c.putNote(CH_A16, 10, 48.0, 10);
			c.putNote(CH_A16, 20, 96.0, 20);
		});
		// {"metas":{},"timeline":{"19:a16":{"10":[[{"t":48,"v":10}]],"20":[[{"t":96.1,"v":20}]]}}}
		assertHash("eba6c24a6dde7ee7f88b270dadfa19cdff270c72d7c2d624ab69e6d4a1d24ee2", c -> {
			c.putNote(CH_A16, 10, 48.0, 10);
			c.putNote(CH_A16, 20, 96.1, 20);
		});
		// {"metas":{},"timeline":{"1A:a36":{"10":[[{"t":48,"v":10}]],"20":[[{"t":96,"v":20}]]}}}
		assertHash("7684b1dd1518a5aeb68b3484315c05ec6635277ef319d9f57ef62f6f0a4e8218", c -> {
			c.putNote(CH_A36, 10, 48.0, 10);
			c.putNote(CH_A36, 20, 96.0, 20);
		});
		// {"metas":{},"timeline":{"1A:a36":{"10":[[{"t":48,"v":10}]],"20":[[{"t":96.1,"v":20}]]}}}
		assertHash("080f66f9de2d6cf65167d4c9c7aeddee24b7d7ab7c4eb3113039e8b85776c9c7", c -> {
			c.putNote(CH_A36, 10, 48.0, 10);
			c.putNote(CH_A36, 20, 96.1, 20);
		});
		// {"metas":{},"timeline":{"1B:a62":{"10":[[{"t":48,"v":10}]],"20":[[{"t":96,"v":20}]]}}}
		assertHash("1ac398e4b0514877a6ffdc3b88b06e52c5e5ca9f031661104e52983888a9b3d5", c -> {
			c.putNote(CH_A62, 10, 48.0, 10);
			c.putNote(CH_A62, 20, 96.0, 20);
		});
		// {"metas":{},"timeline":{"1B:a62":{"10":[[{"t":48,"v":10}]],"20":[[{"t":96.1,"v":20}]]}}}
		assertHash("6029ac77d07f3c04eaebdd1089506e30041cd0978f9474ccb30677b2f21f6573", c -> {
			c.putNote(CH_A62, 10, 48.0, 10);
			c.putNote(CH_A62, 20, 96.1, 20);
		});
	}

	// タイムライン：配列型の値のインデックスを変更するとハッシュ値が変化すること
	@Test
	public void testTimeline_ArrayType_ChangedIndex() {
		// {"metas":{},"timeline":{"28:a":{"10":[[],[{"t":48,"v":10},{"t":96,"v":20}]],"20":[[],[{"t":144,"v":30}]]}}}
		assertHash("9f4171125d78bd95a1d7790de00cf96533342077cfae5f1beecf3e3a21c850c9", c -> {
			c.putNote(CH_AM, 1, 10, 48.0, 10);
			c.putNote(CH_AM, 1, 10, 96.0, 20);
			c.putNote(CH_AM, 1, 20, 144.0, 30);
		});
		// {"metas":{},"timeline":{"28:a":{"10":[[],[{"t":48,"v":10},{"t":96,"v":20}]],"20":[[],[],[{"t":144,"v":30}]]}}}
		assertHash("1229cc84f91e431ad3e996a9f34bdc3fce6ec27dca9b2409cd980d23dd18f434", c -> {
			c.putNote(CH_AM, 1, 10, 48.0, 10);
			c.putNote(CH_AM, 1, 10, 96.0, 20);
			c.putNote(CH_AM, 2, 20, 144.0, 30);
		});
		// {"metas":{},"timeline":{"29:a16":{"10":[[],[{"t":48,"v":10},{"t":96,"v":20}]],"20":[[],[{"t":144,"v":30}]]}}}
		assertHash("231bf6ea138c2f9a3d501f15b2e9f1cc29cfc585d1e86b9d987b06f658afbc44", c -> {
			c.putNote(CH_A16M, 1, 10, 48.0, 10);
			c.putNote(CH_A16M, 1, 10, 96.0, 20);
			c.putNote(CH_A16M, 1, 20, 144.0, 30);
		});
		// {"metas":{},"timeline":{"29:a16":{"10":[[],[{"t":48,"v":10},{"t":96,"v":20}]],"20":[[],[],[{"t":144,"v":30}]]}}}
		assertHash("a852dfaf15c8bc03ae8ae1b7393e25e61cf4a715905b158000905341dc0e1a72", c -> {
			c.putNote(CH_A16M, 1, 10, 48.0, 10);
			c.putNote(CH_A16M, 1, 10, 96.0, 20);
			c.putNote(CH_A16M, 2, 20, 144.0, 30);
		});
		// {"metas":{},"timeline":{"2A:a36":{"10":[[],[{"t":48,"v":10},{"t":96,"v":20}]],"20":[[],[{"t":144,"v":30}]]}}}
		assertHash("af44dc6cd73604c9b8ba1a7fa27f30ade834689b737e5046395d426063c4802e", c -> {
			c.putNote(CH_A36M, 1, 10, 48.0, 10);
			c.putNote(CH_A36M, 1, 10, 96.0, 20);
			c.putNote(CH_A36M, 1, 20, 144.0, 30);
		});
		// {"metas":{},"timeline":{"2A:a36":{"10":[[],[{"t":48,"v":10},{"t":96,"v":20}]],"20":[[],[],[{"t":144,"v":30}]]}}}
		assertHash("bc6f662148c59cf8d089fe3afc0ab15119445cda66158059756029fb85148b40", c -> {
			c.putNote(CH_A36M, 1, 10, 48.0, 10);
			c.putNote(CH_A36M, 1, 10, 96.0, 20);
			c.putNote(CH_A36M, 2, 20, 144.0, 30);
		});
		// {"metas":{},"timeline":{"2B:a62":{"10":[[],[{"t":48,"v":10},{"t":96,"v":20}]],"20":[[],[{"t":144,"v":30}]]}}}
		assertHash("944b58def553e7526d206db1479986333c69e33447017538f10c734cbec4eb20", c -> {
			c.putNote(CH_A62M, 1, 10, 48.0, 10);
			c.putNote(CH_A62M, 1, 10, 96.0, 20);
			c.putNote(CH_A62M, 1, 20, 144.0, 30);
		});
		// {"metas":{},"timeline":{"2B:a62":{"10":[[],[{"t":48,"v":10},{"t":96,"v":20}]],"20":[[],[],[{"t":144,"v":30}]]}}}
		assertHash("23e9c0791431a4c2cf918a8c46ae61946e20971e66ad5b717d969b83b931acbd", c -> {
			c.putNote(CH_A62M, 1, 10, 48.0, 10);
			c.putNote(CH_A62M, 1, 10, 96.0, 20);
			c.putNote(CH_A62M, 2, 20, 144.0, 30);
		});
	}

	// タイムライン：配列型の2つの配列の位置が入れ替わるとハッシュ値が変化すること
	@Test
	public void testTimeline_ArrayType_SwappedArray() {
		// {"metas":{},"timeline":{"28:a":{"10":[[{"t":0,"v":10},{"t":48,"v":20},{"t":96,"v":30}],[{"t":24,"v":40},{"t":72,"v":50}]]}}}
		assertHash("9cfa97569ac1b47cbcd227530a1b8bdd7e3d8a8fac36e007ed228344fc8b05bb", c -> {
			c.putNote(CH_AM, 0, 10, 0.0, 10);
			c.putNote(CH_AM, 0, 10, 48.0, 20);
			c.putNote(CH_AM, 0, 10, 96.0, 30);
			c.putNote(CH_AM, 1, 10, 24.0, 40);
			c.putNote(CH_AM, 1, 10, 72.0, 50);
		});
		// {"metas":{},"timeline":{"28:a":{"10":[[{"t":24,"v":40},{"t":72,"v":50}],[{"t":0,"v":10},{"t":48,"v":20},{"t":96,"v":30}]]}}}
		assertHash("1b70eba893c4aee3f3baf92df319bf68f068b97372777dca87568b98274c4448", c -> {
			c.putNote(CH_AM, 1, 10, 0.0, 10);
			c.putNote(CH_AM, 1, 10, 48.0, 20);
			c.putNote(CH_AM, 1, 10, 96.0, 30);
			c.putNote(CH_AM, 0, 10, 24.0, 40);
			c.putNote(CH_AM, 0, 10, 72.0, 50);
		});
		// {"metas":{},"timeline":{"29:a16":{"10":[[{"t":0,"v":10},{"t":48,"v":20},{"t":96,"v":30}],[{"t":24,"v":40},{"t":72,"v":50}]]}}}
		assertHash("36bf6b3e63d2a8ab40e7b5deb44cbeb93e00b0f454c092f1e644c52e164d39a3", c -> {
			c.putNote(CH_A16M, 0, 10, 0.0, 10);
			c.putNote(CH_A16M, 0, 10, 48.0, 20);
			c.putNote(CH_A16M, 0, 10, 96.0, 30);
			c.putNote(CH_A16M, 1, 10, 24.0, 40);
			c.putNote(CH_A16M, 1, 10, 72.0, 50);
		});
		// {"metas":{},"timeline":{"29:a16":{"10":[[{"t":24,"v":40},{"t":72,"v":50}],[{"t":0,"v":10},{"t":48,"v":20},{"t":96,"v":30}]]}}}
		assertHash("96cb25e57fe92d902ec59aa0cd67fb085c4eafe8d8dd698f0f8481b7e2c72fde", c -> {
			c.putNote(CH_A16M, 1, 10, 0.0, 10);
			c.putNote(CH_A16M, 1, 10, 48.0, 20);
			c.putNote(CH_A16M, 1, 10, 96.0, 30);
			c.putNote(CH_A16M, 0, 10, 24.0, 40);
			c.putNote(CH_A16M, 0, 10, 72.0, 50);
		});
		// {"metas":{},"timeline":{"2A:a36":{"10":[[{"t":0,"v":10},{"t":48,"v":20},{"t":96,"v":30}],[{"t":24,"v":40},{"t":72,"v":50}]]}}}
		assertHash("059e10901eb2d862c9d3026a7904ae9bb60bd0da087b423ca76f0741baebb525", c -> {
			c.putNote(CH_A36M, 0, 10, 0.0, 10);
			c.putNote(CH_A36M, 0, 10, 48.0, 20);
			c.putNote(CH_A36M, 0, 10, 96.0, 30);
			c.putNote(CH_A36M, 1, 10, 24.0, 40);
			c.putNote(CH_A36M, 1, 10, 72.0, 50);
		});
		// {"metas":{},"timeline":{"2A:a36":{"10":[[{"t":24,"v":40},{"t":72,"v":50}],[{"t":0,"v":10},{"t":48,"v":20},{"t":96,"v":30}]]}}}
		assertHash("1dd3e576c981fefda603c4d8a0bc75c30392d8a6c94860b29eac991c5a45c6d4", c -> {
			c.putNote(CH_A36M, 1, 10, 0.0, 10);
			c.putNote(CH_A36M, 1, 10, 48.0, 20);
			c.putNote(CH_A36M, 1, 10, 96.0, 30);
			c.putNote(CH_A36M, 0, 10, 24.0, 40);
			c.putNote(CH_A36M, 0, 10, 72.0, 50);
		});
		// {"metas":{},"timeline":{"2B:a62":{"10":[[{"t":0,"v":10},{"t":48,"v":20},{"t":96,"v":30}],[{"t":24,"v":40},{"t":72,"v":50}]]}}}
		assertHash("5d0fc3a0d26e09c2b21e736fd3730090053679c29f92df9448b7e6b7b5bbc6ff", c -> {
			c.putNote(CH_A62M, 0, 10, 0.0, 10);
			c.putNote(CH_A62M, 0, 10, 48.0, 20);
			c.putNote(CH_A62M, 0, 10, 96.0, 30);
			c.putNote(CH_A62M, 1, 10, 24.0, 40);
			c.putNote(CH_A62M, 1, 10, 72.0, 50);
		});
		// {"metas":{},"timeline":{"2B:a62":{"10":[[{"t":24,"v":40},{"t":72,"v":50}],[{"t":0,"v":10},{"t":48,"v":20},{"t":96,"v":30}]]}}}
		assertHash("032b1cdd985f635c5a9e3d9fa74d03d9f02e2d45a0bd5ef88a6693253cf83472", c -> {
			c.putNote(CH_A62M, 1, 10, 0.0, 10);
			c.putNote(CH_A62M, 1, 10, 48.0, 20);
			c.putNote(CH_A62M, 1, 10, 96.0, 30);
			c.putNote(CH_A62M, 0, 10, 24.0, 40);
			c.putNote(CH_A62M, 0, 10, 72.0, 50);
		});
	}

	// タイムライン：配列型の小節番号がずれるとハッシュ値が変化すること
	@Test
	public void testTimeline_ArrayType_OffsetMeasure() {
		// {"metas":{},"timeline":{"18:a":{"10":[[{"t":96,"v":10}]]}}}
		assertHash("630bb63313920a169da4fe1868c8975b2edcd25afbee8b806814f2f6418b6e80",
				c -> c.putNote(CH_A, 10, 96.0, 10));
		// {"metas":{},"timeline":{"18:a":{"15":[[{"t":96,"v":10}]]}}}
		assertHash("502336bcbd05bdfe8a2dfd6607613101faa3ce8ded5c2846b42b55d1a74d0a79",
				c -> c.putNote(CH_A, 15, 96.0, 10));
		// {"metas":{},"timeline":{"19:a16":{"10":[[{"t":96,"v":10}]]}}}
		assertHash("b0ce25740f6b658e4654fc26dda0bbd16da2f9429f4b7f29c898dde86a9e62ef",
				c -> c.putNote(CH_A16, 10, 96.0, 10));
		// {"metas":{},"timeline":{"19:a16":{"15":[[{"t":96,"v":10}]]}}}
		assertHash("4322e567685520c81a110ec8a1a6224f3b687c846ae60dbf82d11baedf03397d",
				c -> c.putNote(CH_A16, 15, 96.0, 10));
		// {"metas":{},"timeline":{"1A:a36":{"10":[[{"t":96,"v":10}]]}}}
		assertHash("f8361c69bb401e88a3b51c8316b97e36ffc77c639ab1a8e0d1f2507961fddfb0",
				c -> c.putNote(CH_A36, 10, 96.0, 10));
		// {"metas":{},"timeline":{"1A:a36":{"15":[[{"t":96,"v":10}]]}}}
		assertHash("82930dbea446672afe03008f48a8bd9750fe5c3440de03fcd598ce6716203feb",
				c -> c.putNote(CH_A36, 15, 96.0, 10));
		// {"metas":{},"timeline":{"1B:a62":{"10":[[{"t":96,"v":10}]]}}}
		assertHash("9b32089a50565d0d9a2be9d70597e26aa58fd359b8968fc73593bc279204de2d",
				c -> c.putNote(CH_A62, 10, 96.0, 10));
		// {"metas":{},"timeline":{"1B:a62":{"15":[[{"t":96,"v":10}]]}}}
		assertHash("2dc333a0cbdc438f98e5f9bf5cc0d713f9fce3e5ebd244c03de1101ec850a192",
				c -> c.putNote(CH_A62, 15, 96.0, 10));
	}

	// タイムライン：同一性チェックがOFFのチャンネルの値を変更してもハッシュ値は変化しないこと
	@Test
	public void testTimeline_UniquenessOff() {
		// {"metas":{},"timeline":{"11:i":{"1":[100]}}}
		var s1 = genericSpec().addChannel(BmsChannel.spec(CH_ZZ, BmsType.INTEGER, null, "0", true, false)).create();
		assertHash("36a6b0b52e1b273e63e317416c522f21f057a9c166788d69b7982a8ec89450dc", s1, c -> {
			c.setMeasureValue(CH_I, 1, 100L);
			c.setMeasureValue(CH_ZZ, 0, 2, 200L);
		});
		// {"metas":{},"timeline":{"11:i":{"1":[100]}}}
		assertHash("36a6b0b52e1b273e63e317416c522f21f057a9c166788d69b7982a8ec89450dc", s1, c -> {
			c.setMeasureValue(CH_I, 1, 100L);
			c.setMeasureValue(CH_ZZ, 1, 3, 300L);
		});
		// {"metas":{},"timeline":{"12:f":{"1":[1.1]}}}
		var s2 = genericSpec().addChannel(BmsChannel.spec(CH_ZZ, BmsType.FLOAT, null, "0", true, false)).create();
		assertHash("8e59b8ef7b824d3db27305edb777bfeaba48351a5eca6120d5384c6351e42791", s2, c -> {
			c.setMeasureValue(CH_F, 1, 1.1);
			c.setMeasureValue(CH_ZZ, 0, 2, 1.2);
		});
		// {"metas":{},"timeline":{"12:f":{"1":[1.1]}}}
		assertHash("8e59b8ef7b824d3db27305edb777bfeaba48351a5eca6120d5384c6351e42791", s2, c -> {
			c.setMeasureValue(CH_F, 1, 1.1);
			c.setMeasureValue(CH_ZZ, 1, 3, 1.3);
		});
		// {"metas":{},"timeline":{"13:s":{"1":["str1"]}}}
		var s3 = genericSpec().addChannel(BmsChannel.spec(CH_ZZ, BmsType.STRING, null, "", true, false)).create();
		assertHash("bcdf08069786055ba7cd7bdf4aad274a068d94b4c49f980141e91ab4fcb678fd", s3, c -> {
			c.setMeasureValue(CH_S, 1, "str1");
			c.setMeasureValue(CH_ZZ, 0, 2, "str2");
		});
		// {"metas":{},"timeline":{"13:s":{"1":["str1"]}}}
		assertHash("bcdf08069786055ba7cd7bdf4aad274a068d94b4c49f980141e91ab4fcb678fd", s3, c -> {
			c.setMeasureValue(CH_S, 1, "str1");
			c.setMeasureValue(CH_ZZ, 1, 3, "str3");
		});
		// {"metas":{},"timeline":{"14:b":{"1":[10]}}}
		var s4 = genericSpec().addChannel(BmsChannel.spec(CH_ZZ, BmsType.BASE, null, "00", true, false)).create();
		assertHash("afc4e51b367a44d128f0ff48f4fdfa586558852f72acfd0bd952df5bce1a5c71", s4, c -> {
			c.setMeasureValue(CH_B, 1, 10L);
			c.setMeasureValue(CH_ZZ, 0, 2, 20L);
		});
		// {"metas":{},"timeline":{"14:b":{"1":[10]}}}
		assertHash("afc4e51b367a44d128f0ff48f4fdfa586558852f72acfd0bd952df5bce1a5c71", s4, c -> {
			c.setMeasureValue(CH_B, 1, 10L);
			c.setMeasureValue(CH_ZZ, 1, 3, 30L);
		});
		// {"metas":{},"timeline":{"15:b16":{"1":[10]}}}
		var s5 = genericSpec().addChannel(BmsChannel.spec(CH_ZZ, BmsType.BASE16, null, "00", true, false)).create();
		assertHash("64311e8d21e3187b5d107d3b32fbcfd816c221ab2c08ed8ec86795f31f0d63b7", s5, c -> {
			c.setMeasureValue(CH_B16, 1, 10L);
			c.setMeasureValue(CH_ZZ, 0, 2, 20L);
		});
		// {"metas":{},"timeline":{"15:b16":{"1":[10]}}}
		assertHash("64311e8d21e3187b5d107d3b32fbcfd816c221ab2c08ed8ec86795f31f0d63b7", s5, c -> {
			c.setMeasureValue(CH_B16, 1, 10L);
			c.setMeasureValue(CH_ZZ, 1, 3, 30L);
		});
		// {"metas":{},"timeline":{"16:b36":{"1":[10]}}}
		var s6 = genericSpec().addChannel(BmsChannel.spec(CH_ZZ, BmsType.BASE36, null, "00", true, false)).create();
		assertHash("68922c227dbe8439a1da7e1d6045d1adc9a9970821a74ecd77b56d1d93feed58", s6, c -> {
			c.setMeasureValue(CH_B36, 1, 10L);
			c.setMeasureValue(CH_ZZ, 0, 2, 20L);
		});
		// {"metas":{},"timeline":{"16:b36":{"1":[10]}}}
		assertHash("68922c227dbe8439a1da7e1d6045d1adc9a9970821a74ecd77b56d1d93feed58", s6, c -> {
			c.setMeasureValue(CH_B36, 1, 10L);
			c.setMeasureValue(CH_ZZ, 1, 3, 30L);
		});
		// {"metas":{},"timeline":{"17:b62":{"1":[10]}}}
		var s7 = genericSpec().addChannel(BmsChannel.spec(CH_ZZ, BmsType.BASE62, null, "00", true, false)).create();
		assertHash("b7f7cbdadd7157143df479f83acf411858f65befd8570af0a7ce480ff13350dc", s7, c -> {
			c.setMeasureValue(CH_B62, 1, 10L);
			c.setMeasureValue(CH_ZZ, 0, 2, 20L);
		});
		// {"metas":{},"timeline":{"17:b62":{"1":[10]}}}
		assertHash("b7f7cbdadd7157143df479f83acf411858f65befd8570af0a7ce480ff13350dc", s7, c -> {
			c.setMeasureValue(CH_B62, 1, 10L);
			c.setMeasureValue(CH_ZZ, 1, 3, 30L);
		});
		// {"metas":{},"timeline":{"18:a":{"1":[[{"t":48,"v":10}]]}}}
		var s8 = genericSpec().addChannel(BmsChannel.spec(CH_ZZ, BmsType.ARRAY, null, "00", true, false)).create();
		assertHash("bd8dd98ff7c74d4b2636529e130862697a388715ce70c0bef372fcbde9c94103", s8, c -> {
			c.putNote(CH_A, 1, 48.0, 10);
			c.putNote(CH_ZZ, 0, 1, 96.0, 20);
		});
		// {"metas":{},"timeline":{"18:a":{"1":[[{"t":48,"v":10}]]}}}
		assertHash("bd8dd98ff7c74d4b2636529e130862697a388715ce70c0bef372fcbde9c94103", s8, c -> {
			c.putNote(CH_A, 1, 48.0, 10);
			c.putNote(CH_ZZ, 1, 2, 144.0, 30);
		});
		// {"metas":{},"timeline":{"19:a16":{"1":[[{"t":48,"v":10}]]}}}
		var s9 = genericSpec().addChannel(BmsChannel.spec(CH_ZZ, BmsType.ARRAY16, null, "00", true, false)).create();
		assertHash("68d59ef9369e58e3ca40ef7ec913ec0ee1b479686ae1ff545897953877309cfa", s9, c -> {
			c.putNote(CH_A16, 1, 48.0, 10);
			c.putNote(CH_ZZ, 0, 1, 96.0, 20);
		});
		// {"metas":{},"timeline":{"19:a16":{"1":[[{"t":48,"v":10}]]}}}
		assertHash("68d59ef9369e58e3ca40ef7ec913ec0ee1b479686ae1ff545897953877309cfa", s9, c -> {
			c.putNote(CH_A16, 1, 48.0, 10);
			c.putNote(CH_ZZ, 1, 2, 144.0, 30);
		});
		// {"metas":{},"timeline":{"1A:a36":{"1":[[{"t":48,"v":10}]]}}}
		var s10 = genericSpec().addChannel(BmsChannel.spec(CH_ZZ, BmsType.ARRAY36, null, "00", true, false)).create();
		assertHash("30edcd3e2c116f788c822e909f1920df107c8fdd5cbda160d02577ad370da3f0", s10, c -> {
			c.putNote(CH_A36, 1, 48.0, 10);
			c.putNote(CH_ZZ, 0, 1, 96.0, 20);
		});
		// {"metas":{},"timeline":{"1A:a36":{"1":[[{"t":48,"v":10}]]}}}
		assertHash("30edcd3e2c116f788c822e909f1920df107c8fdd5cbda160d02577ad370da3f0", s10, c -> {
			c.putNote(CH_A36, 1, 48.0, 10);
			c.putNote(CH_ZZ, 1, 2, 144.0, 30);
		});
		// {"metas":{},"timeline":{"1B:a62":{"1":[[{"t":48,"v":10}]]}}}
		var s11 = genericSpec().addChannel(BmsChannel.spec(CH_ZZ, BmsType.ARRAY62, null, "00", true, false)).create();
		assertHash("a9c4d62e564106f7afadb2202a621bef6a3275679fb9a73f94555b6a8b5c4902", s11, c -> {
			c.putNote(CH_A62, 1, 48.0, 10);
			c.putNote(CH_ZZ, 0, 1, 96.0, 20);
		});
		// {"metas":{},"timeline":{"1B:a62":{"1":[[{"t":48,"v":10}]]}}}
		assertHash("a9c4d62e564106f7afadb2202a621bef6a3275679fb9a73f94555b6a8b5c4902", s11, c -> {
			c.putNote(CH_A62, 1, 48.0, 10);
			c.putNote(CH_ZZ, 1, 2, 144.0, 30);
		});
	}

	// タイムライン：ユーザーチャンネルの値を変更してもハッシュ値は変化しないこと
	@Test
	public void testTimeline_UserChannel() {
		// {"metas":{},"timeline":{"11:i":{"1":[100]},"12:f":{"2":[1.1]},"13:s":{"3":["str"]},"14:b":{"4":[10]},"15:b16":{"5":[20]},"16:b36":{"6":[30]},"17:b62":{"7":[40]},"18:a":{"8":[[{"t":0,"v":10}]]},"19:a16":{"9":[[{"t":48,"v":20}]]},"1A:a36":{"10":[[{"t":96,"v":30}]]},"1B:a62":{"8":[[{"t":144,"v":40}]]}}}
		var spec = genericSpec().addChannel(BmsChannel.user(CH_USR, BmsType.STRING, null, "", true)).create();
		assertHash("2639f878b979c21dd8708a8efe57ac5838fc5a8eb64cde068654d88335130bc4", spec, c -> {
			c.setMeasureValue(CH_I, 1, 100L);
			c.setMeasureValue(CH_F, 2, 1.1);
			c.setMeasureValue(CH_S, 3, "str");
			c.setMeasureValue(CH_B, 4, 10L);
			c.setMeasureValue(CH_B16, 5, 20L);
			c.setMeasureValue(CH_B36, 6, 30L);
			c.setMeasureValue(CH_B62, 7, 40L);
			c.putNote(CH_A, 8, 0.0, 10);
			c.putNote(CH_A16, 9, 48.0, 20);
			c.putNote(CH_A36, 10, 96.0, 30);
			c.putNote(CH_A62, 8, 144.0, 40);
			c.setMeasureValue(CH_USR, 999, "user1");
		});
		// {"metas":{},"timeline":{"11:i":{"1":[100]},"12:f":{"2":[1.1]},"13:s":{"3":["str"]},"14:b":{"4":[10]},"15:b16":{"5":[20]},"16:b36":{"6":[30]},"17:b62":{"7":[40]},"18:a":{"8":[[{"t":0,"v":10}]]},"19:a16":{"9":[[{"t":48,"v":20}]]},"1A:a36":{"10":[[{"t":96,"v":30}]]},"1B:a62":{"8":[[{"t":144,"v":40}]]}}}
		assertHash("2639f878b979c21dd8708a8efe57ac5838fc5a8eb64cde068654d88335130bc4", spec, c -> {
			c.setMeasureValue(CH_I, 1, 100L);
			c.setMeasureValue(CH_F, 2, 1.1);
			c.setMeasureValue(CH_S, 3, "str");
			c.setMeasureValue(CH_B, 4, 10L);
			c.setMeasureValue(CH_B16, 5, 20L);
			c.setMeasureValue(CH_B36, 6, 30L);
			c.setMeasureValue(CH_B62, 7, 40L);
			c.putNote(CH_A, 8, 0.0, 10);
			c.putNote(CH_A16, 9, 48.0, 20);
			c.putNote(CH_A36, 10, 96.0, 30);
			c.putNote(CH_A62, 8, 144.0, 40);
			c.setMeasureValue(CH_USR, 999, "user2");
		});
	}

	// その他：BMS宣言の有無がハッシュ値に影響しないこと
	@Test
	public void testMisc_Declaration() {
		// {"metas":{"#is:s":{"t":"i","d":[{"i":0,"v":100}]},"#fm:m":{"t":"f","d":[{"i":0,"v":1.2}]},"#si:i":{"t":"s","d":[{"i":0,"v":"str"}]}},"timeline":{"11:i":{"1":[100]},"18:a":{"2":[[{"t":96,"v":200}]]}}}
		assertHash("e992d0d8a97d095c17d7324039e8c04b33b25afbb3a378c74d70784ee31bdecb", c -> {
			c.setSingleMeta("#is", 100L);
			c.setMultipleMeta("#fm", 0, 1.2);
			c.setIndexedMeta("#si", 0, "str");
			c.setMeasureValue(CH_I, 1, 100L);
			c.putNote(CH_A, 2, 96.0, 200);
		});
		// {"metas":{"#is:s":{"t":"i","d":[{"i":0,"v":100}]},"#fm:m":{"t":"f","d":[{"i":0,"v":1.2}]},"#si:i":{"t":"s","d":[{"i":0,"v":"str"}]}},"timeline":{"11:i":{"1":[100]},"18:a":{"2":[[{"t":96,"v":200}]]}}}
		assertHash("e992d0d8a97d095c17d7324039e8c04b33b25afbb3a378c74d70784ee31bdecb", c -> {
			c.putDeclaration("key", "value");
			c.setSingleMeta("#is", 100L);
			c.setMultipleMeta("#fm", 0, 1.2);
			c.setIndexedMeta("#si", 0, "str");
			c.setMeasureValue(CH_I, 1, 100L);
			c.putNote(CH_A, 2, 96.0, 200);
		});
	}

	// その他：IllegalStateException 動作モードが参照モードではない
	@Test
	public void testMisc_NotReferenceMode() {
		var ex = IllegalStateException.class;
		var c = content();
		c.beginEdit();
		assertThrows(ex, () -> c.generateHash());
		assertThrows(ex, () -> c.generateHash(true, true, true));
	}

	// その他：IllegalArgumentException includeMetas と includeTimeline の両方が false
	@Test
	public void testMisc_ExcludeMetasAndTimeline() {
		var c = content();
		assertThrows(IllegalArgumentException.class, () -> c.generateHash(true, false, false));
	}

	private static final int CH_I = BmsInt.to36i("11");
	private static final int CH_F = BmsInt.to36i("12");
	private static final int CH_S = BmsInt.to36i("13");
	private static final int CH_B = BmsInt.to36i("14");
	private static final int CH_B16 = BmsInt.to36i("15");
	private static final int CH_B36 = BmsInt.to36i("16");
	private static final int CH_B62 = BmsInt.to36i("17");
	private static final int CH_A = BmsInt.to36i("18");
	private static final int CH_A16 = BmsInt.to36i("19");
	private static final int CH_A36 = BmsInt.to36i("1A");
	private static final int CH_A62 = BmsInt.to36i("1B");

	private static final int CH_IM = BmsInt.to36i("21");
	private static final int CH_FM = BmsInt.to36i("22");
	private static final int CH_SM = BmsInt.to36i("23");
	private static final int CH_BM = BmsInt.to36i("24");
	private static final int CH_B16M = BmsInt.to36i("25");
	private static final int CH_B36M = BmsInt.to36i("26");
	private static final int CH_B62M = BmsInt.to36i("27");
	private static final int CH_AM = BmsInt.to36i("28");
	private static final int CH_A16M = BmsInt.to36i("29");
	private static final int CH_A36M = BmsInt.to36i("2A");
	private static final int CH_A62M = BmsInt.to36i("2B");

	private static final int CH_ZY = BmsInt.to36i("ZY");
	private static final int CH_ZZ = BmsInt.to36i("ZZ");
	private static final int CH_USR = BmsSpec.USER_CHANNEL_MIN;

	private static BmsSpecBuilder genericSpec() {
		return new BmsSpecBuilder()
				.addMeta(BmsMeta.single("#is", BmsType.INTEGER, "0", 1010, true))
				.addMeta(BmsMeta.single("#fs", BmsType.FLOAT, "0", 1020, true))
				.addMeta(BmsMeta.single("#ss", BmsType.STRING, "", 1030, true))
				.addMeta(BmsMeta.single("#bs", BmsType.BASE, "00", 1040, true))
				.addMeta(BmsMeta.single("#b16s", BmsType.BASE16, "00", 1050, true))
				.addMeta(BmsMeta.single("#b36s", BmsType.BASE36, "00", 1060, true))
				.addMeta(BmsMeta.single("#b62s", BmsType.BASE62, "00", 1070, true))
				.addMeta(BmsMeta.single("#as", BmsType.ARRAY, "00", 1080, true))
				.addMeta(BmsMeta.single("#a16s", BmsType.ARRAY16, "00", 1090, true))
				.addMeta(BmsMeta.single("#a36s", BmsType.ARRAY36, "00", 1100, true))
				.addMeta(BmsMeta.single("#a62s", BmsType.ARRAY62, "00", 1110, true))
				.addMeta(BmsMeta.multiple("#im", BmsType.INTEGER, "0", 2010, true))
				.addMeta(BmsMeta.multiple("#fm", BmsType.FLOAT, "0", 2020, true))
				.addMeta(BmsMeta.multiple("#sm", BmsType.STRING, "", 2030, true))
				.addMeta(BmsMeta.multiple("#bm", BmsType.BASE, "00", 2040, true))
				.addMeta(BmsMeta.multiple("#b16m", BmsType.BASE16, "00", 2050, true))
				.addMeta(BmsMeta.multiple("#b36m", BmsType.BASE36, "00", 2060, true))
				.addMeta(BmsMeta.multiple("#b62m", BmsType.BASE62, "00", 2070, true))
				.addMeta(BmsMeta.multiple("#am", BmsType.ARRAY, "00", 2080, true))
				.addMeta(BmsMeta.multiple("#a16m", BmsType.ARRAY16, "00", 2090, true))
				.addMeta(BmsMeta.multiple("#a36m", BmsType.ARRAY36, "00", 2100, true))
				.addMeta(BmsMeta.multiple("#a62m", BmsType.ARRAY62, "00", 2110, true))
				.addMeta(BmsMeta.indexed("#ii", BmsType.INTEGER, "0", 3010, true))
				.addMeta(BmsMeta.indexed("#fi", BmsType.FLOAT, "0", 3020, true))
				.addMeta(BmsMeta.indexed("#si", BmsType.STRING, "", 3030, true))
				.addMeta(BmsMeta.indexed("#bi", BmsType.BASE, "00", 3040, true))
				.addMeta(BmsMeta.indexed("#b16i", BmsType.BASE16, "00", 3050, true))
				.addMeta(BmsMeta.indexed("#b36i", BmsType.BASE36, "00", 3060, true))
				.addMeta(BmsMeta.indexed("#b62i", BmsType.BASE62, "00", 3070, true))
				.addMeta(BmsMeta.indexed("#ai", BmsType.ARRAY, "00", 3080, true))
				.addMeta(BmsMeta.indexed("#a16i", BmsType.ARRAY16, "00", 3090, true))
				.addMeta(BmsMeta.indexed("#a36i", BmsType.ARRAY36, "00", 3100, true))
				.addMeta(BmsMeta.indexed("#a62i", BmsType.ARRAY62, "00", 3110, true))
				.addMeta(BmsMeta.object("#osx", BmsUnit.SINGLE))
				.addMeta(BmsMeta.object("#omx", BmsUnit.MULTIPLE))
				.addMeta(BmsMeta.object("#oix", BmsUnit.INDEXED))
				.addMeta(BmsMeta.single("#bpm", BmsType.FLOAT, "130", Integer.MAX_VALUE, true))
				.setInitialBpmMeta("#bpm")
				.addChannel(BmsChannel.spec(CH_I, BmsType.INTEGER, null, "0", false, true))
				.addChannel(BmsChannel.spec(CH_F, BmsType.FLOAT, null, "0", false, true))
				.addChannel(BmsChannel.spec(CH_S, BmsType.STRING, null, "", false, true))
				.addChannel(BmsChannel.spec(CH_B, BmsType.BASE, null, "00", false, true))
				.addChannel(BmsChannel.spec(CH_B16, BmsType.BASE16, null, "00", false, true))
				.addChannel(BmsChannel.spec(CH_B36, BmsType.BASE36, null, "00", false, true))
				.addChannel(BmsChannel.spec(CH_B62, BmsType.BASE62, null, "00", false, true))
				.addChannel(BmsChannel.spec(CH_A, BmsType.ARRAY, "#ii", "00", false, true))
				.addChannel(BmsChannel.spec(CH_A16, BmsType.ARRAY16, "#fi", "00", false, true))
				.addChannel(BmsChannel.spec(CH_A36, BmsType.ARRAY36, "#si", "00", false, true))
				.addChannel(BmsChannel.spec(CH_A62, BmsType.ARRAY62, "#bi", "00", false, true))
				.addChannel(BmsChannel.spec(CH_IM, BmsType.INTEGER, null, "0", true, true))
				.addChannel(BmsChannel.spec(CH_FM, BmsType.FLOAT, null, "0", true, true))
				.addChannel(BmsChannel.spec(CH_SM, BmsType.STRING, null, "", true, true))
				.addChannel(BmsChannel.spec(CH_BM, BmsType.BASE, null, "00", true, true))
				.addChannel(BmsChannel.spec(CH_B16M, BmsType.BASE16, null, "00", true, true))
				.addChannel(BmsChannel.spec(CH_B36M, BmsType.BASE36, null, "00", true, true))
				.addChannel(BmsChannel.spec(CH_B62M, BmsType.BASE62, null, "00", true, true))
				.addChannel(BmsChannel.spec(CH_AM, BmsType.ARRAY, "#ii", "00", true, true))
				.addChannel(BmsChannel.spec(CH_A16M, BmsType.ARRAY16, "#fi", "00", true, true))
				.addChannel(BmsChannel.spec(CH_A36M, BmsType.ARRAY36, "#si", "00", true, true))
				.addChannel(BmsChannel.spec(CH_A62M, BmsType.ARRAY62, "#bi", "00", true, true))
				.addChannel(BmsChannel.spec(BmsInt.to36i("YX"), BmsType.FLOAT, null, "1", false, true))
				.addChannel(BmsChannel.spec(BmsInt.to36i("YY"), BmsType.ARRAY62, null, "00", false, true))
				.addChannel(BmsChannel.spec(BmsInt.to36i("YZ"), BmsType.ARRAY62, null, "00", false, true))
				.setLengthChannel(BmsInt.to36i("YX"))
				.setBpmChannel(BmsInt.to36i("YY"))
				.setStopChannel(BmsInt.to36i("YZ"));
	}

	private static BmsContent content() {
		return new BmsContent(genericSpec().create());
	}

	private static void assertHash(String expectedHashStr, Consumer<BmsContent> contentEditor) {
		assertHash(expectedHashStr, genericSpec().create(), contentEditor);
	}

	private static void assertHash(String expectedHashStr, BmsSpec spec, Consumer<BmsContent> contentEditor) {
		var content = new BmsContent(spec);
		content.beginEdit();
		contentEditor.accept(content);
		content.endEdit();
		assertHash(expectedHashStr, content, false, true, true);
	}

	private static void assertHash(String expectedHashStr, boolean includeSpec, boolean includeMetas,
			boolean includeTimeline, Consumer<BmsContent> contentEditor) {
		var content = content();
		content.beginEdit();
		contentEditor.accept(content);
		content.endEdit();
		assertHash(expectedHashStr, content, includeSpec, includeMetas, includeTimeline);
	}

	private static void assertHash(String expectedHashStr, BmsContent content, boolean includeSpec,
			boolean includeMetas, boolean includeTimeline) {
		var seed = content.generateHashSeed(includeSpec, includeMetas, includeTimeline);
		var hash = content.generateHash(includeSpec, includeMetas, includeTimeline);
		var seedStr = seed.toString();
		var hashStr = Utility.byteArrayToString(hash);
		// 以下、テストコード作成時の実データ出力処理。リリース時はコメントアウトすること。
		//System.out.printf("%s\n", seedStr);
		//System.out.printf("%s\n\n", hashStr);
		// 以下、ハッシュ値の検証
		if (!hashStr.equals(expectedHashStr)) {
			var msg = String.format("Expected %s but %s (%s)", expectedHashStr, hashStr, seedStr);
			fail(msg);
		}
	}
}
