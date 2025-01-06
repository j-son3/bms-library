package com.lmt.lib.bms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

// JUnit4にて、指定した順にテストメソッドを実行させるためのクラス。
// テストクラスに @RunWith(OrderedRunner.class) を指定することでそのクラスのメソッドに @Order(order=順) を指定できる。
// orderの値が小さい順にテストが実施される。@Order未指定のメソッドは先に実行される。
public class OrderedRunner extends BlockJUnit4ClassRunner {
	public OrderedRunner(Class<?> testClass) throws InitializationError {
		super(testClass);
	}

	@Override
	protected List<FrameworkMethod> computeTestMethods() {
		var list = new ArrayList<>(super.computeTestMethods());
		Collections.sort(list, (f1, f2) -> {
			Order o1 = f1.getAnnotation(Order.class), o2 = f2.getAnnotation(Order.class);
			return ((o1 == null) || (o2 == null)) ? -1 : (o1.order() - o2.order());
		});
		return list;
	}
}
