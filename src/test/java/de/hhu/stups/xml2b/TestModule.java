package de.hhu.stups.xml2b;

import com.google.inject.Guice;
import com.google.inject.Injector;
import de.prob.MainModule;

public final class TestModule {
	private static final Injector injector = Guice.createInjector(new MainModule());
	
	private TestModule() {
		throw new AssertionError("Utility class");
	}
	
	public static Injector getInjector() {
		return injector;
	}
}
