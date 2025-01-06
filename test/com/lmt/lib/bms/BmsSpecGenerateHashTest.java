package com.lmt.lib.bms;

import static org.junit.Assert.*;

import org.junit.Test;

import com.lmt.lib.bms.internal.Utility;

public class BmsSpecGenerateHashTest {
	// メタ情報：名称を変更するとハッシュ値が変化すること
	@Test
	public void testMetas_ChangedName() {
		// {"metas":[{"n":"#a","u":"s","t":"i","d":0,"q":false},{"n":"#bpm","u":"s","t":"f","d":130,"q":true}],"channels":[{"n":"ZZ","t":"a","r":null,"d":"00","m":false,"q":true}],"length":null,"bpms":[],"stops":[]}
		assertHash("2dcb34841d7faf5f15514eaae536f5cd250f4022af89587d5d28821b357a0145", minSpec()
				.addMeta(BmsMeta.single("#a", BmsType.INTEGER, "0", 0, false)));
		// {"metas":[{"n":"#b","u":"s","t":"i","d":0,"q":false},{"n":"#bpm","u":"s","t":"f","d":130,"q":true}],"channels":[{"n":"ZZ","t":"a","r":null,"d":"00","m":false,"q":true}],"length":null,"bpms":[],"stops":[]}
		assertHash("ed4ae3b30a670227476861c11b8c080f19cd9c9ac3f9f1639e2dd24e9afccbfd", minSpec()
				.addMeta(BmsMeta.single("#b", BmsType.INTEGER, "0", 0, false)));
	}

	// メタ情報：構成単位を変更するとハッシュ値が変化すること
	@Test
	public void testMetas_ChangedUnit() {
		// {"metas":[{"n":"#a","u":"s","t":"i","d":0,"q":false},{"n":"#bpm","u":"s","t":"f","d":130,"q":true}],"channels":[{"n":"ZZ","t":"a","r":null,"d":"00","m":false,"q":true}],"length":null,"bpms":[],"stops":[]}
		assertHash("2dcb34841d7faf5f15514eaae536f5cd250f4022af89587d5d28821b357a0145", minSpec()
				.addMeta(BmsMeta.single("#a", BmsType.INTEGER, "0", 0, false)));
		// {"metas":[{"n":"#a","u":"m","t":"i","d":0,"q":false},{"n":"#bpm","u":"s","t":"f","d":130,"q":true}],"channels":[{"n":"ZZ","t":"a","r":null,"d":"00","m":false,"q":true}],"length":null,"bpms":[],"stops":[]}
		assertHash("e88005d429c6f2b01e74298fd43d1a5890da59bae6269f999dd8306b134629d4", minSpec()
				.addMeta(BmsMeta.multiple("#a", BmsType.INTEGER, "0", 0, false)));
		// {"metas":[{"n":"#a","u":"i","t":"i","d":0,"q":false},{"n":"#bpm","u":"s","t":"f","d":130,"q":true}],"channels":[{"n":"ZZ","t":"a","r":null,"d":"00","m":false,"q":true}],"length":null,"bpms":[],"stops":[]}
		assertHash("76a62548db6359cd9dc40539a4575228719a48a92f2a496cf8e314406a7341ba", minSpec()
				.addMeta(BmsMeta.indexed("#a", BmsType.INTEGER, "0", 0, false)));
	}

	// メタ情報：データ型を変更するとハッシュ値が変化すること
	@Test
	public void testMetas_ChangedType() {
		// {"metas":[{"n":"#a","u":"s","t":"i","d":1,"q":false},{"n":"#bpm","u":"s","t":"f","d":130,"q":true}],"channels":[{"n":"ZZ","t":"a","r":null,"d":"00","m":false,"q":true}],"length":null,"bpms":[],"stops":[]}
		assertHash("3b2c3ebfab6cfdeb811521d7924ec997bc572560409645abda50302c9371c3e6", minSpec()
				.addMeta(BmsMeta.single("#a", BmsType.INTEGER, "01", 0, false)));
		// {"metas":[{"n":"#a","u":"s","t":"f","d":1,"q":false},{"n":"#bpm","u":"s","t":"f","d":130,"q":true}],"channels":[{"n":"ZZ","t":"a","r":null,"d":"00","m":false,"q":true}],"length":null,"bpms":[],"stops":[]}
		assertHash("fbfe604f01e1c5dc2e4a1e126861f1c6508b63aa4ddc58946c8b4641a9db4c31", minSpec()
				.addMeta(BmsMeta.single("#a", BmsType.FLOAT, "01", 0, false)));
		// {"metas":[{"n":"#a","u":"s","t":"s","d":"01","q":false},{"n":"#bpm","u":"s","t":"f","d":130,"q":true}],"channels":[{"n":"ZZ","t":"a","r":null,"d":"00","m":false,"q":true}],"length":null,"bpms":[],"stops":[]}
		assertHash("43e04dcbe344688b236ad565ffaf343f5fc3104a2c9f950762face6b4f1ef467", minSpec()
				.addMeta(BmsMeta.single("#a", BmsType.STRING, "01", 0, false)));
		// {"metas":[{"n":"#a","u":"s","t":"b","d":1,"q":false},{"n":"#bpm","u":"s","t":"f","d":130,"q":true}],"channels":[{"n":"ZZ","t":"a","r":null,"d":"00","m":false,"q":true}],"length":null,"bpms":[],"stops":[]}
		assertHash("0afa0adb88412cbaef16ad289ea2947a6816b1d68edaf00291b09f4de4ea7cdc", minSpec()
				.addMeta(BmsMeta.single("#a", BmsType.BASE, "01", 0, false)));
		// {"metas":[{"n":"#a","u":"s","t":"b16","d":1,"q":false},{"n":"#bpm","u":"s","t":"f","d":130,"q":true}],"channels":[{"n":"ZZ","t":"a","r":null,"d":"00","m":false,"q":true}],"length":null,"bpms":[],"stops":[]}
		assertHash("324c5ad0490e8babb3b67359c96ee654d54c8661adf2590e7bdcf6ffd2a37cac", minSpec()
				.addMeta(BmsMeta.single("#a", BmsType.BASE16, "01", 0, false)));
		// {"metas":[{"n":"#a","u":"s","t":"b36","d":1,"q":false},{"n":"#bpm","u":"s","t":"f","d":130,"q":true}],"channels":[{"n":"ZZ","t":"a","r":null,"d":"00","m":false,"q":true}],"length":null,"bpms":[],"stops":[]}
		assertHash("95ff692be978e36fcbad7a216740fa410a6145a8486d721514c66bc5b9b821b2", minSpec()
				.addMeta(BmsMeta.single("#a", BmsType.BASE36, "01", 0, false)));
		// {"metas":[{"n":"#a","u":"s","t":"b62","d":1,"q":false},{"n":"#bpm","u":"s","t":"f","d":130,"q":true}],"channels":[{"n":"ZZ","t":"a","r":null,"d":"00","m":false,"q":true}],"length":null,"bpms":[],"stops":[]}
		assertHash("a8aba4f3c8d739eeebaa4004e8afc61b7785ea82f1fa16a0cd787b1caeb7bbcf", minSpec()
				.addMeta(BmsMeta.single("#a", BmsType.BASE62, "01", 0, false)));
		// {"metas":[{"n":"#a","u":"s","t":"a","d":"01","q":false},{"n":"#bpm","u":"s","t":"f","d":130,"q":true}],"channels":[{"n":"ZZ","t":"a","r":null,"d":"00","m":false,"q":true}],"length":null,"bpms":[],"stops":[]}
		assertHash("ed5038d25bfa1d0a7ae1bdda4d1625bbd86c1e2c30243d087e05c677111e7c48", minSpec()
				.addMeta(BmsMeta.single("#a", BmsType.ARRAY, "01", 0, false)));
		// {"metas":[{"n":"#a","u":"s","t":"a16","d":"01","q":false},{"n":"#bpm","u":"s","t":"f","d":130,"q":true}],"channels":[{"n":"ZZ","t":"a","r":null,"d":"00","m":false,"q":true}],"length":null,"bpms":[],"stops":[]}
		assertHash("355f30d0d1ee3a01dfc7e15f39d54c59e7683ad1814b258274cde0611fd22a8e", minSpec()
				.addMeta(BmsMeta.single("#a", BmsType.ARRAY16, "01", 0, false)));
		// {"metas":[{"n":"#a","u":"s","t":"a36","d":"01","q":false},{"n":"#bpm","u":"s","t":"f","d":130,"q":true}],"channels":[{"n":"ZZ","t":"a","r":null,"d":"00","m":false,"q":true}],"length":null,"bpms":[],"stops":[]}
		assertHash("b0b7d77bc6cd16abfe82ccd228cbfdc17945de6c4f5d2035a5e8c80f52eae9d7", minSpec()
				.addMeta(BmsMeta.single("#a", BmsType.ARRAY36, "01", 0, false)));
		// {"metas":[{"n":"#a","u":"s","t":"a62","d":"01","q":false},{"n":"#bpm","u":"s","t":"f","d":130,"q":true}],"channels":[{"n":"ZZ","t":"a","r":null,"d":"00","m":false,"q":true}],"length":null,"bpms":[],"stops":[]}
		assertHash("7d1c2db4104f9fd07749dad4f9130012efd396490ecf6f5ecd21bdb6881ec6d6", minSpec()
				.addMeta(BmsMeta.single("#a", BmsType.ARRAY62, "01", 0, false)));
	}

	// メタ情報：デフォルト値を変更するとハッシュ値が変化すること
	@Test
	public void testMetas_ChangedDefaultValue() {
		// {"metas":[{"n":"#a","u":"s","t":"i","d":1,"q":false},{"n":"#bpm","u":"s","t":"f","d":130,"q":true}],"channels":[{"n":"ZZ","t":"a","r":null,"d":"00","m":false,"q":true}],"length":null,"bpms":[],"stops":[]}
		assertHash("3b2c3ebfab6cfdeb811521d7924ec997bc572560409645abda50302c9371c3e6", minSpec()
				.addMeta(BmsMeta.single("#a", BmsType.INTEGER, "1", 0, false)));
		// {"metas":[{"n":"#a","u":"s","t":"i","d":2,"q":false},{"n":"#bpm","u":"s","t":"f","d":130,"q":true}],"channels":[{"n":"ZZ","t":"a","r":null,"d":"00","m":false,"q":true}],"length":null,"bpms":[],"stops":[]}
		assertHash("6f6d821598c85dae1e332072f6b9a2243e588d7a1e68a8e4831426d5c712c932", minSpec()
				.addMeta(BmsMeta.single("#a", BmsType.INTEGER, "2", 0, false)));
	}

	// メタ情報：同一性チェックを変更するとハッシュ値が変化すること
	@Test
	public void testMetas_ChangedUniqueness() {
		// {"metas":[{"n":"#a","u":"s","t":"f","d":1,"q":false},{"n":"#bpm","u":"s","t":"f","d":130,"q":true}],"channels":[{"n":"ZZ","t":"a","r":null,"d":"00","m":false,"q":true}],"length":null,"bpms":[],"stops":[]}
		assertHash("fbfe604f01e1c5dc2e4a1e126861f1c6508b63aa4ddc58946c8b4641a9db4c31", minSpec()
				.addMeta(BmsMeta.single("#a", BmsType.FLOAT, "1.0", 0, false)));
		// {"metas":[{"n":"#a","u":"s","t":"f","d":1,"q":true},{"n":"#bpm","u":"s","t":"f","d":130,"q":true}],"channels":[{"n":"ZZ","t":"a","r":null,"d":"00","m":false,"q":true}],"length":null,"bpms":[],"stops":[]}
		assertHash("47399f9f7f8c7cef4351b59dd9418ab8a0bbd85a99e3c50738e0bc2a36276d21", minSpec()
				.addMeta(BmsMeta.single("#a", BmsType.FLOAT, "1.0", 0, true)));
	}

	// メタ情報：メタ情報の順番が入れ替わるとハッシュ値が変化すること
	@Test
	public void testMetas_ChangedSortOrder() {
		// {"metas":[{"n":"#a","u":"s","t":"i","d":0,"q":false},{"n":"#b","u":"m","t":"f","d":1,"q":true},{"n":"#bpm","u":"s","t":"f","d":130,"q":true}],"channels":[{"n":"ZZ","t":"a","r":null,"d":"00","m":false,"q":true}],"length":null,"bpms":[],"stops":[]}
		assertHash("ff3bcf6746b1119e4f05d22154a08ecd7e73e68cd371d7469cd4c23fa9264ec6", minSpec()
				.addMeta(BmsMeta.single("#a", BmsType.INTEGER, "0", 0, false))
				.addMeta(BmsMeta.multiple("#b", BmsType.FLOAT, "1", 1, true)));
		// {"metas":[{"n":"#b","u":"m","t":"f","d":1,"q":true},{"n":"#a","u":"s","t":"i","d":0,"q":false},{"n":"#bpm","u":"s","t":"f","d":130,"q":true}],"channels":[{"n":"ZZ","t":"a","r":null,"d":"00","m":false,"q":true}],"length":null,"bpms":[],"stops":[]}
		assertHash("3844d0d3f3a874fac81cb785d11ff9a0fa77d47866d685a37fbf8a0fd855e924", minSpec()
				.addMeta(BmsMeta.single("#a", BmsType.INTEGER, "0", 1, false))
				.addMeta(BmsMeta.multiple("#b", BmsType.FLOAT, "1", 0, true)));
	}

	// メタ情報：任意型のメタ情報が追加されてもハッシュ値は変化しないこと
	@Test
	public void testMetas_AddedObjectType() {
		// {"metas":[{"n":"#a","u":"s","t":"s","d":"a","q":false},{"n":"#bpm","u":"s","t":"f","d":130,"q":true}],"channels":[{"n":"ZZ","t":"a","r":null,"d":"00","m":false,"q":true}],"length":null,"bpms":[],"stops":[]}
		assertHash("fa3df9bd21e30f512f341d98170ea53a4950b9d44e267efeb596ee3a4ab75479", minSpec()
				.addMeta(BmsMeta.single("#a", BmsType.STRING, "a", 0, false)));
		// {"metas":[{"n":"#a","u":"s","t":"s","d":"a","q":false},{"n":"#bpm","u":"s","t":"f","d":130,"q":true}],"channels":[{"n":"ZZ","t":"a","r":null,"d":"00","m":false,"q":true}],"length":null,"bpms":[],"stops":[]}
		assertHash("fa3df9bd21e30f512f341d98170ea53a4950b9d44e267efeb596ee3a4ab75479", minSpec()
				.addMeta(BmsMeta.single("#a", BmsType.STRING, "a", 0, false))
				.addMeta(BmsMeta.object("#o", BmsUnit.INDEXED)));
	}

	// チャンネル：チャンネル番号を変更するとハッシュ値が変化すること
	@Test
	public void testChannels_ChangedNumber() {
		// {"metas":[{"n":"#bpm","u":"s","t":"f","d":130,"q":true}],"channels":[{"n":"01","t":"a","r":null,"d":"00","m":false,"q":false},{"n":"ZZ","t":"a","r":null,"d":"00","m":false,"q":true}],"length":null,"bpms":[],"stops":[]}
		assertHash("d18452f8be47f73992341f43b9d47b22076dd4820448ba5e4f656a5cc81e4cdd", minSpec()
				.addChannel(BmsChannel.spec(BmsInt.to36i("01"), BmsType.ARRAY, null, "00", false, false)));
		// {"metas":[{"n":"#bpm","u":"s","t":"f","d":130,"q":true}],"channels":[{"n":"ZX","t":"a","r":null,"d":"00","m":false,"q":false},{"n":"ZZ","t":"a","r":null,"d":"00","m":false,"q":true}],"length":null,"bpms":[],"stops":[]}
		assertHash("80d2f11bf02894e1009aa860221774916ec1f6e14fc612afb449d2c2f249a6bb", minSpec()
				.addChannel(BmsChannel.spec(BmsInt.to36i("ZX"), BmsType.ARRAY, null, "00", false, false)));
	}

	// チャンネル：データ型を変更するとハッシュ値が変化すること
	@Test
	public void testChannels_ChangedType() {
		// {"metas":[{"n":"#bpm","u":"s","t":"f","d":130,"q":true}],"channels":[{"n":"01","t":"i","r":null,"d":1,"m":false,"q":false},{"n":"ZZ","t":"a","r":null,"d":"00","m":false,"q":true}],"length":null,"bpms":[],"stops":[]}
		assertHash("1fab7e2813ca34176cac9602d0d21beff0daebca50db03ed92ad680755b4d42b", minSpec()
				.addChannel(BmsChannel.spec(BmsInt.to36i("01"), BmsType.INTEGER, null, "01", false, false)));
		// {"metas":[{"n":"#bpm","u":"s","t":"f","d":130,"q":true}],"channels":[{"n":"01","t":"f","r":null,"d":1,"m":false,"q":false},{"n":"ZZ","t":"a","r":null,"d":"00","m":false,"q":true}],"length":null,"bpms":[],"stops":[]}
		assertHash("a49903b45769f08470e78b72ff43b0e42926b6ab1049cd53b8efb7f7753e6873", minSpec()
				.addChannel(BmsChannel.spec(BmsInt.to36i("01"), BmsType.FLOAT, null, "01", false, false)));
		// {"metas":[{"n":"#bpm","u":"s","t":"f","d":130,"q":true}],"channels":[{"n":"01","t":"s","r":null,"d":"01","m":false,"q":false},{"n":"ZZ","t":"a","r":null,"d":"00","m":false,"q":true}],"length":null,"bpms":[],"stops":[]}
		assertHash("8369fba9e93337128c35be1681ec2acf9a3af4fd8ae0e2c67cfdefc2ab045e00", minSpec()
				.addChannel(BmsChannel.spec(BmsInt.to36i("01"), BmsType.STRING, null, "01", false, false)));
		// {"metas":[{"n":"#bpm","u":"s","t":"f","d":130,"q":true}],"channels":[{"n":"01","t":"b","r":null,"d":1,"m":false,"q":false},{"n":"ZZ","t":"a","r":null,"d":"00","m":false,"q":true}],"length":null,"bpms":[],"stops":[]}
		assertHash("0dfb58515ba10be9d59a8fb77e8bbd2de8c05f36aee03e4a9856ed7402344fec", minSpec()
				.addChannel(BmsChannel.spec(BmsInt.to36i("01"), BmsType.BASE, null, "01", false, false)));
		// {"metas":[{"n":"#bpm","u":"s","t":"f","d":130,"q":true}],"channels":[{"n":"01","t":"b16","r":null,"d":1,"m":false,"q":false},{"n":"ZZ","t":"a","r":null,"d":"00","m":false,"q":true}],"length":null,"bpms":[],"stops":[]}
		assertHash("f7ce228d2aab5821200caf63b8df5330fe64794e64aa0db3dca2b272d62fb91a", minSpec()
				.addChannel(BmsChannel.spec(BmsInt.to36i("01"), BmsType.BASE16, null, "01", false, false)));
		// {"metas":[{"n":"#bpm","u":"s","t":"f","d":130,"q":true}],"channels":[{"n":"01","t":"b36","r":null,"d":1,"m":false,"q":false},{"n":"ZZ","t":"a","r":null,"d":"00","m":false,"q":true}],"length":null,"bpms":[],"stops":[]}
		assertHash("bce12578e5557c7293bf1f37c06e94b99a80bbefcbdef5431ddd6f251ea7bd8c", minSpec()
				.addChannel(BmsChannel.spec(BmsInt.to36i("01"), BmsType.BASE36, null, "01", false, false)));
		// {"metas":[{"n":"#bpm","u":"s","t":"f","d":130,"q":true}],"channels":[{"n":"01","t":"b62","r":null,"d":1,"m":false,"q":false},{"n":"ZZ","t":"a","r":null,"d":"00","m":false,"q":true}],"length":null,"bpms":[],"stops":[]}
		assertHash("867b0c75d6e66651313567287c396721cab76e999e9ffa487c717c4d36e18f42", minSpec()
				.addChannel(BmsChannel.spec(BmsInt.to36i("01"), BmsType.BASE62, null, "01", false, false)));
		// {"metas":[{"n":"#bpm","u":"s","t":"f","d":130,"q":true}],"channels":[{"n":"01","t":"a","r":null,"d":"01","m":false,"q":false},{"n":"ZZ","t":"a","r":null,"d":"00","m":false,"q":true}],"length":null,"bpms":[],"stops":[]}
		assertHash("0a8cd6475e543e3890c542853e285f41bfbc76934608a9167802f97ceacd09dc", minSpec()
				.addChannel(BmsChannel.spec(BmsInt.to36i("01"), BmsType.ARRAY, null, "01", false, false)));
		// {"metas":[{"n":"#bpm","u":"s","t":"f","d":130,"q":true}],"channels":[{"n":"01","t":"a16","r":null,"d":"01","m":false,"q":false},{"n":"ZZ","t":"a","r":null,"d":"00","m":false,"q":true}],"length":null,"bpms":[],"stops":[]}
		assertHash("69b1390cbec6452ea8e0d2b7e1130b87ad5d90c42ad281af6882f04df40bb891", minSpec()
				.addChannel(BmsChannel.spec(BmsInt.to36i("01"), BmsType.ARRAY16, null, "01", false, false)));
		// {"metas":[{"n":"#bpm","u":"s","t":"f","d":130,"q":true}],"channels":[{"n":"01","t":"a36","r":null,"d":"01","m":false,"q":false},{"n":"ZZ","t":"a","r":null,"d":"00","m":false,"q":true}],"length":null,"bpms":[],"stops":[]}
		assertHash("4f9ba42780adb34cfc6423d14c69b5c13dccce4200c8db30721edd3dc8eeff1c", minSpec()
				.addChannel(BmsChannel.spec(BmsInt.to36i("01"), BmsType.ARRAY36, null, "01", false, false)));
		// {"metas":[{"n":"#bpm","u":"s","t":"f","d":130,"q":true}],"channels":[{"n":"01","t":"a62","r":null,"d":"01","m":false,"q":false},{"n":"ZZ","t":"a","r":null,"d":"00","m":false,"q":true}],"length":null,"bpms":[],"stops":[]}
		assertHash("2e7ff6913bf60d44c9e0878960bbcbed0086e121212e131bd77a0d05cbc0b1ca", minSpec()
				.addChannel(BmsChannel.spec(BmsInt.to36i("01"), BmsType.ARRAY62, null, "01", false, false)));
	}

	// チャンネル：参照先メタ情報を変更するとハッシュ値が変化すること
	@Test
	public void testChannels_ChangedRef() {
		// {"metas":[{"n":"#a","u":"i","t":"f","d":0,"q":false},{"n":"#b","u":"i","t":"f","d":0,"q":false},{"n":"#bpm","u":"s","t":"f","d":130,"q":true}],"channels":[{"n":"01","t":"a","r":"#a","d":"00","m":false,"q":false},{"n":"ZZ","t":"a","r":null,"d":"00","m":false,"q":true}],"length":null,"bpms":[],"stops":[]}
		assertHash("53dbf85db89aa9f57a5148f3b3ac41ea4967bb33d6f5717559624d3cdbf741d8", minSpec()
				.addMeta(BmsMeta.indexed("#a", BmsType.FLOAT, "0", 0, false))
				.addMeta(BmsMeta.indexed("#b", BmsType.FLOAT, "0", 0, false))
				.addChannel(BmsChannel.spec(BmsInt.to36i("01"), BmsType.ARRAY, "#a", "00", false, false)));
		// {"metas":[{"n":"#a","u":"i","t":"f","d":0,"q":false},{"n":"#b","u":"i","t":"f","d":0,"q":false},{"n":"#bpm","u":"s","t":"f","d":130,"q":true}],"channels":[{"n":"01","t":"a","r":"#b","d":"00","m":false,"q":false},{"n":"ZZ","t":"a","r":null,"d":"00","m":false,"q":true}],"length":null,"bpms":[],"stops":[]}
		assertHash("903383c1d6682ee09612602ff6797aad738b04be95db165f00ffc82c8ef7a72a", minSpec()
				.addMeta(BmsMeta.indexed("#a", BmsType.FLOAT, "0", 0, false))
				.addMeta(BmsMeta.indexed("#b", BmsType.FLOAT, "0", 0, false))
				.addChannel(BmsChannel.spec(BmsInt.to36i("01"), BmsType.ARRAY, "#b", "00", false, false)));
	}

	// チャンネル：デフォルト値を変更するとハッシュ値が変化すること
	@Test
	public void testChannels_ChangedDefaultValue() {
		// {"metas":[{"n":"#bpm","u":"s","t":"f","d":130,"q":true}],"channels":[{"n":"01","t":"i","r":null,"d":1,"m":false,"q":false},{"n":"ZZ","t":"a","r":null,"d":"00","m":false,"q":true}],"length":null,"bpms":[],"stops":[]}
		assertHash("1fab7e2813ca34176cac9602d0d21beff0daebca50db03ed92ad680755b4d42b", minSpec()
				.addChannel(BmsChannel.spec(BmsInt.to36i("01"), BmsType.INTEGER, null, "1", false, false)));
		// {"metas":[{"n":"#bpm","u":"s","t":"f","d":130,"q":true}],"channels":[{"n":"01","t":"i","r":null,"d":2,"m":false,"q":false},{"n":"ZZ","t":"a","r":null,"d":"00","m":false,"q":true}],"length":null,"bpms":[],"stops":[]}
		assertHash("94dc64b5aa41dd8217777891c451304239f9867370eb4cbbeef38477d76789bb", minSpec()
				.addChannel(BmsChannel.spec(BmsInt.to36i("01"), BmsType.INTEGER, null, "2", false, false)));
	}

	// チャンネル：複数データ可否を変更するとハッシュ値が変化すること
	@Test
	public void testChannels_ChangedMultiple() {
		// {"metas":[{"n":"#bpm","u":"s","t":"f","d":130,"q":true}],"channels":[{"n":"01","t":"f","r":null,"d":0,"m":false,"q":false},{"n":"ZZ","t":"a","r":null,"d":"00","m":false,"q":true}],"length":null,"bpms":[],"stops":[]}
		assertHash("923834c7499067d48baa606f45a15b5383153c232823287e92b1edcd88dd957a", minSpec()
				.addChannel(BmsChannel.spec(BmsInt.to36i("01"), BmsType.FLOAT, null, "0", false, false)));
		// {"metas":[{"n":"#bpm","u":"s","t":"f","d":130,"q":true}],"channels":[{"n":"01","t":"f","r":null,"d":0,"m":true,"q":false},{"n":"ZZ","t":"a","r":null,"d":"00","m":false,"q":true}],"length":null,"bpms":[],"stops":[]}
		assertHash("a5eaf09e13cfd40c2eeb99fcde7f9b0598624124d231870bda29c9cbef6e4556", minSpec()
				.addChannel(BmsChannel.spec(BmsInt.to36i("01"), BmsType.FLOAT, null, "0", true, false)));
	}

	// チャンネル：同一性チェックを変更するとハッシュ値が変化すること
	@Test
	public void testChannels_ChangedUniqueness() {
		// {"metas":[{"n":"#bpm","u":"s","t":"f","d":130,"q":true}],"channels":[{"n":"01","t":"s","r":null,"d":"","m":false,"q":false},{"n":"ZZ","t":"a","r":null,"d":"00","m":false,"q":true}],"length":null,"bpms":[],"stops":[]}
		assertHash("54a0a6fcbb4f7480cf50daa9db5a0fc5b1b9faa13730708aca1f7bb4e6868edb", minSpec()
				.addChannel(BmsChannel.spec(BmsInt.to36i("01"), BmsType.STRING, null, "", false, false)));
		// {"metas":[{"n":"#bpm","u":"s","t":"f","d":130,"q":true}],"channels":[{"n":"01","t":"s","r":null,"d":"","m":false,"q":true},{"n":"ZZ","t":"a","r":null,"d":"00","m":false,"q":true}],"length":null,"bpms":[],"stops":[]}
		assertHash("8ab19486182528b34e214db58f54f34dfcd9fac7fcbce6e2a8d3e32f4f5fcf58", minSpec()
				.addChannel(BmsChannel.spec(BmsInt.to36i("01"), BmsType.STRING, null, "", false, true)));
	}

	// チャンネル：ユーザーチャンネルが追加されてもハッシュ値は変化しないこと
	@Test
	public void testChannels_AddedUserChannel() {
		// {"metas":[{"n":"#bpm","u":"s","t":"f","d":130,"q":true}],"channels":[{"n":"01","t":"a","r":null,"d":"00","m":false,"q":false},{"n":"ZZ","t":"a","r":null,"d":"00","m":false,"q":true}],"length":null,"bpms":[],"stops":[]}
		assertHash("d18452f8be47f73992341f43b9d47b22076dd4820448ba5e4f656a5cc81e4cdd", minSpec()
				.addChannel(BmsChannel.spec(BmsInt.to36i("01"), BmsType.ARRAY, null, "00", false, false)));
		// {"metas":[{"n":"#bpm","u":"s","t":"f","d":130,"q":true}],"channels":[{"n":"01","t":"a","r":null,"d":"00","m":false,"q":false},{"n":"ZZ","t":"a","r":null,"d":"00","m":false,"q":true}],"length":null,"bpms":[],"stops":[]}
		assertHash("d18452f8be47f73992341f43b9d47b22076dd4820448ba5e4f656a5cc81e4cdd", minSpec()
				.addChannel(BmsChannel.spec(BmsInt.to36i("01"), BmsType.ARRAY, null, "00", false, false))
				.addChannel(BmsChannel.user(BmsSpec.USER_CHANNEL_MIN, BmsType.INTEGER, null, "0", false)));
	}

	// 小節長変更：チャンネル番号を変更するとハッシュ値が変化すること
	@Test
	public void testLength_ChangedNumber() {
		// {"metas":[{"n":"#bpm","u":"s","t":"f","d":130,"q":true}],"channels":[{"n":"01","t":"f","r":null,"d":1,"m":false,"q":true},{"n":"ZX","t":"f","r":null,"d":1,"m":false,"q":true},{"n":"ZZ","t":"a","r":null,"d":"00","m":false,"q":true}],"length":"01","bpms":[],"stops":[]}
		assertHash("5289e7c8b56662eff2e5ff1edb239442b1511472c55fd5f25c4d045f11ae2284", minSpec()
				.addChannel(BmsChannel.spec(BmsInt.to36i("01"), BmsType.FLOAT, null, "1", false, true))
				.addChannel(BmsChannel.spec(BmsInt.to36i("ZX"), BmsType.FLOAT, null, "1", false, true))
				.setLengthChannel(BmsInt.to36i("01")));
		// {"metas":[{"n":"#bpm","u":"s","t":"f","d":130,"q":true}],"channels":[{"n":"01","t":"f","r":null,"d":1,"m":false,"q":true},{"n":"ZX","t":"f","r":null,"d":1,"m":false,"q":true},{"n":"ZZ","t":"a","r":null,"d":"00","m":false,"q":true}],"length":"ZX","bpms":[],"stops":[]}
		assertHash("e35634205b232dad182777efe7f607930c067c6984bdaa57ec5a23ee2a606fcc", minSpec()
				.addChannel(BmsChannel.spec(BmsInt.to36i("01"), BmsType.FLOAT, null, "1", false, true))
				.addChannel(BmsChannel.spec(BmsInt.to36i("ZX"), BmsType.FLOAT, null, "1", false, true))
				.setLengthChannel(BmsInt.to36i("ZX")));
	}

	// BPM変更：チャンネル番号を変更するとハッシュ値が変化すること
	@Test
	public void testBpms_ChangedNumber() {
		// {"metas":[{"n":"#bpm","u":"s","t":"f","d":130,"q":true}],"channels":[{"n":"01","t":"a62","r":null,"d":"00","m":false,"q":true},{"n":"ZX","t":"a62","r":null,"d":"00","m":false,"q":true},{"n":"ZZ","t":"a","r":null,"d":"00","m":false,"q":true}],"length":null,"bpms":["01"],"stops":[]}
		assertHash("b84b6e51aa207c24aea410b8b7a6b8dc792433a1520ce40d5420635f0f61336b", minSpec()
				.addChannel(BmsChannel.spec(BmsInt.to36i("01"), BmsType.ARRAY62, null, "00", false, true))
				.addChannel(BmsChannel.spec(BmsInt.to36i("ZX"), BmsType.ARRAY62, null, "00", false, true))
				.setBpmChannel(BmsInt.to36i("01")));
		// {"metas":[{"n":"#bpm","u":"s","t":"f","d":130,"q":true}],"channels":[{"n":"01","t":"a62","r":null,"d":"00","m":false,"q":true},{"n":"ZX","t":"a62","r":null,"d":"00","m":false,"q":true},{"n":"ZZ","t":"a","r":null,"d":"00","m":false,"q":true}],"length":null,"bpms":["ZX"],"stops":[]}
		assertHash("173b33927a6493836e07cc7213cdab977b5a9017c60641b27d01ef4d9a6d7d5f", minSpec()
				.addChannel(BmsChannel.spec(BmsInt.to36i("01"), BmsType.ARRAY62, null, "00", false, true))
				.addChannel(BmsChannel.spec(BmsInt.to36i("ZX"), BmsType.ARRAY62, null, "00", false, true))
				.setBpmChannel(BmsInt.to36i("ZX")));
	}

	// BPM変更：チャンネルを追加するとハッシュ値が変化すること
	@Test
	public void testBpms_AddedChannel() {
		// {"metas":[{"n":"#bpm","u":"s","t":"f","d":130,"q":true}],"channels":[{"n":"01","t":"a36","r":null,"d":"00","m":false,"q":true},{"n":"ZX","t":"a36","r":null,"d":"00","m":false,"q":true},{"n":"ZZ","t":"a","r":null,"d":"00","m":false,"q":true}],"length":null,"bpms":["01"],"stops":[]}
		assertHash("0ee07a050410f989d9a840d4180b81673dfad02f47ff8d3c0c3351d5d6cc99a1", minSpec()
				.addChannel(BmsChannel.spec(BmsInt.to36i("01"), BmsType.ARRAY36, null, "00", false, true))
				.addChannel(BmsChannel.spec(BmsInt.to36i("ZX"), BmsType.ARRAY36, null, "00", false, true))
				.setBpmChannel(BmsInt.to36i("01")));
		// {"metas":[{"n":"#bpm","u":"s","t":"f","d":130,"q":true}],"channels":[{"n":"01","t":"a36","r":null,"d":"00","m":false,"q":true},{"n":"ZX","t":"a36","r":null,"d":"00","m":false,"q":true},{"n":"ZZ","t":"a","r":null,"d":"00","m":false,"q":true}],"length":null,"bpms":["01","ZX"],"stops":[]}
		assertHash("3fd485b9c1f4c6c988322b58f814ce688c3e421e7337561dabf59d61c61523b2", minSpec()
				.addChannel(BmsChannel.spec(BmsInt.to36i("01"), BmsType.ARRAY36, null, "00", false, true))
				.addChannel(BmsChannel.spec(BmsInt.to36i("ZX"), BmsType.ARRAY36, null, "00", false, true))
				.setBpmChannel(BmsInt.to36i("01"), BmsInt.to36i("ZX")));
	}

	// 譜面停止：チャンネル番号を変更するとハッシュ値が変化すること
	@Test
	public void testStops_ChangedNumber() {
		// {"metas":[{"n":"#bpm","u":"s","t":"f","d":130,"q":true}],"channels":[{"n":"01","t":"a62","r":null,"d":"00","m":false,"q":true},{"n":"ZX","t":"a62","r":null,"d":"00","m":false,"q":true},{"n":"ZZ","t":"a","r":null,"d":"00","m":false,"q":true}],"length":null,"bpms":[],"stops":["01"]}
		assertHash("848dc3d6bebf97f752c4f4186be445d0a3a2831f1c65ea8fdd48b703e74d5d44", minSpec()
				.addChannel(BmsChannel.spec(BmsInt.to36i("01"), BmsType.ARRAY62, null, "00", false, true))
				.addChannel(BmsChannel.spec(BmsInt.to36i("ZX"), BmsType.ARRAY62, null, "00", false, true))
				.setStopChannel(BmsInt.to36i("01")));
		// {"metas":[{"n":"#bpm","u":"s","t":"f","d":130,"q":true}],"channels":[{"n":"01","t":"a62","r":null,"d":"00","m":false,"q":true},{"n":"ZX","t":"a62","r":null,"d":"00","m":false,"q":true},{"n":"ZZ","t":"a","r":null,"d":"00","m":false,"q":true}],"length":null,"bpms":[],"stops":["ZX"]}
		assertHash("d002c1c18a4fa3829327cee307c8edb49855c77050bc0ab1b534853093da99ea", minSpec()
				.addChannel(BmsChannel.spec(BmsInt.to36i("01"), BmsType.ARRAY62, null, "00", false, true))
				.addChannel(BmsChannel.spec(BmsInt.to36i("ZX"), BmsType.ARRAY62, null, "00", false, true))
				.setStopChannel(BmsInt.to36i("ZX")));
	}

	// 譜面停止：チャンネルを追加するとハッシュ値が変化すること
	@Test
	public void testStops_AddedChannel() {
		// {"metas":[{"n":"#bpm","u":"s","t":"f","d":130,"q":true}],"channels":[{"n":"01","t":"a36","r":null,"d":"00","m":false,"q":true},{"n":"ZX","t":"a36","r":null,"d":"00","m":false,"q":true},{"n":"ZZ","t":"a","r":null,"d":"00","m":false,"q":true}],"length":null,"bpms":[],"stops":["01"]}
		assertHash("0e8181d3677bdd9627ef82661e286a29684a8827d1b22170aec5cfe0046ea5c9", minSpec()
				.addChannel(BmsChannel.spec(BmsInt.to36i("01"), BmsType.ARRAY36, null, "00", false, true))
				.addChannel(BmsChannel.spec(BmsInt.to36i("ZX"), BmsType.ARRAY36, null, "00", false, true))
				.setStopChannel(BmsInt.to36i("01")));
		// {"metas":[{"n":"#bpm","u":"s","t":"f","d":130,"q":true}],"channels":[{"n":"01","t":"a36","r":null,"d":"00","m":false,"q":true},{"n":"ZX","t":"a36","r":null,"d":"00","m":false,"q":true},{"n":"ZZ","t":"a","r":null,"d":"00","m":false,"q":true}],"length":null,"bpms":[],"stops":["01","ZX"]}
		assertHash("1b5f17c3b89b6a7a59a34e7fc3d16521ef32e0a34da409944c67609f2770f6b7", minSpec()
				.addChannel(BmsChannel.spec(BmsInt.to36i("01"), BmsType.ARRAY36, null, "00", false, true))
				.addChannel(BmsChannel.spec(BmsInt.to36i("ZX"), BmsType.ARRAY36, null, "00", false, true))
				.setStopChannel(BmsInt.to36i("01"), BmsInt.to36i("ZX")));
	}

	private static BmsSpecBuilder minSpec() {
		return new BmsSpecBuilder()
				.addMeta(BmsMeta.single("#bpm", BmsType.FLOAT, "130", Integer.MAX_VALUE, true))
				.addChannel(BmsChannel.spec(BmsInt.to36i("ZZ"), BmsType.ARRAY, null, "00", false, true))
				.setInitialBpmMeta("#bpm");
	}

	private static void assertHash(String expectedHashStr, BmsSpecBuilder builder) {
		var spec = builder.create();
		var hash = spec.generateHash();
		var hashStr = Utility.byteArrayToString(hash);
		var seed = spec.generateHashSeed();
		var seedStr = seed.toString();
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
