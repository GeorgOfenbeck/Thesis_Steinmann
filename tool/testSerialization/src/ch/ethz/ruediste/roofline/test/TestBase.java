package ch.ethz.ruediste.roofline.test;

import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Before;

import ch.ethz.ruediste.roofline.measurementDriver.MainModule;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class TestBase {
	protected Injector injector;

	protected Mockery context = new Mockery() {
		{
			setImposteriser(ClassImposteriser.INSTANCE);
		}
	};

	@Before
	public void setup() {
		injector = Guice.createInjector(new MainModule());
	}
}
