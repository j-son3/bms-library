package com.lmt.lib.bms;

import java.nio.file.Path;
import java.nio.file.Paths;

public interface UseTestData {
	Path testDataDirectory();

	default String testDataExtension() {
		return "";
	}

	default Path testDataPath() {
		return testDataPath(1);
	}

	default Path testDataPath(int numGoBack) {
		String name = Thread.currentThread().getStackTrace()[numGoBack + 2].getMethodName();
		String ext = testDataExtension();
		return testDataPath(String.format("%s%s%s", name, (ext.isEmpty() ? "" : "."), (ext.isEmpty() ? "" : ext)));
	}

	default Path testDataPath(String fileName) {
		Path base = Paths.get(System.getProperty("user.dir")).resolve(testDataDirectory());
		return base.resolve(fileName);
	}
}
