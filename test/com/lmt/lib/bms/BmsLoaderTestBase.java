package com.lmt.lib.bms;

import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

public abstract class BmsLoaderTestBase implements UseTestData {
	protected BmsLoader loader() {
		return loader(e -> false, new Charset[0]);
	}

	protected BmsLoader loader(Predicate<BmsScriptError> errHandler, Charset[] charsets) {
		var handler = new BmsLoadHandler() {
			@Override public BmsContent createContent(BmsSpec spec) {
				return new BmsContent(spec);
			}
			@Override public boolean parseError(BmsScriptError error) {
				return errHandler.test(error);
			}
		};
		return new BmsStandardLoader()
				.setSpec(BmsTest.createTestSpec())
				.setHandler(handler)
				.setSyntaxErrorEnable(true)
				.setFixSpecViolation(false)
				.setAllowRedefine(false)
				.setIgnoreUnknownMeta(false)
				.setIgnoreUnknownChannel(false)
				.setIgnoreWrongData(false)
				.setCharsets(charsets);
	}

	protected BmsContent load(String...bms) throws Exception {
		return BmsTest.loadContent(bms);
	}

	protected BmsContent load(UnaryOperator<BmsLoader> loaderConfig, Consumer<BmsScriptError> errAction,
			String...bms) throws Exception {
		return BmsTest.loadContent(loaderConfig, errAction, bms);
	}

	protected BmsContent load() {
		// このメソッドを使用するローダではエラー発生を許容しない(当然、エラー発生はテストNG)
		return loadCore(e -> {
			fail("On this error handler, An error is NOT allowed.");
			return false;
		}, new Charset[0]);
	}

	protected BmsContent load(Predicate<BmsScriptError> errHandler) {
		return loadCore(errHandler, new Charset[0]);
	}

	protected BmsContent load(Charset primary, Charset...secondaryAndMore) {
		var charsets = new ArrayList<Charset>();
		charsets.add(primary);
		for (var cs : secondaryAndMore) { charsets.add(cs); }
		return loadCore(e -> { fail(); return false; }, charsets.toArray(Charset[]::new));
	}

	private BmsContent loadCore(Predicate<BmsScriptError> errHandler, Charset[] charsets) {
		try {
			var path = testDataPath(2);
			return loader(errHandler, charsets).load(path);
		} catch (IOException e) {
			e.printStackTrace();
			fail("Failed to load test bms.");
			return null;
		}
	}
}
