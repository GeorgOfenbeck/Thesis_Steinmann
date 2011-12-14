package ch.ethz.ruediste.roofline.measurementDriver;

import java.io.InputStream;
import java.util.List;

import org.apache.commons.configuration.CombinedConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

import ch.ethz.ruediste.roofline.measurementDriver.baseClasses.ICommand;
import ch.ethz.ruediste.roofline.measurementDriver.baseClasses.IMeasurement;
import ch.ethz.ruediste.roofline.measurementDriver.baseClasses.INamed;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.name.Names;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class MainModule extends AbstractModule {

	@Override
	protected void configure() {
		// setup configuration
		PropertiesConfiguration defaultConfiguration = new PropertiesConfiguration();
		try {
			InputStream configStream = ClassLoader
					.getSystemResourceAsStream("defaultConfiguration.config");
			if (configStream == null) {
				throw new Error(
						"could not load <defaultConfiguration.config>. Does not seem to be in the class path. Is it compiled into the .jar?");
			}
			defaultConfiguration.load(configStream);
		} catch (ConfigurationException e) {
			throw new Error(e);
		}

		CombinedConfiguration combinedConfiguration = new CombinedConfiguration();
		combinedConfiguration.addConfiguration(defaultConfiguration);

		bind(Configuration.class).toInstance(combinedConfiguration);

		bind(Instantiator.class).in(Singleton.class);

		// setup XStream
		XStream xStream = new XStream(new DomDriver());
		bind(XStream.class).toInstance(xStream);

		// setup services
		bindAllAsSingletons(Object.class,
				"ch.ethz.ruediste.roofline.measurementDriver.services");

		// setup app controllers
		bindAllAsSingletons(Object.class,
				"ch.ethz.ruediste.roofline.measurementDriver.appControllers");

		// setup measurements and commands
		bindNamed(IMeasurement.class,
				"ch.ethz.ruediste.roofline.measurementDriver.measurements");

		bindNamed(ICommand.class,
				"ch.ethz.ruediste.roofline.measurementDriver.commands");

	}

	private <T> void bindAllAsSingletons(Class<T> baseType, String basePackage) {
		List<Class<? extends T>> classes = ClassFinder
				.getClassesImplementing(
						baseType, basePackage);
		// bind the classes found
		for (Class<? extends T> clazz : classes) {
			bind(clazz).in(Singleton.class);
		}
	}

	private <T extends INamed> void bindNamed(Class<T> baseType,
			String basePackage) {
		// get all classes implementing the given base type
		List<Class<? extends T>> measurementClasses = ClassFinder
				.getClassesImplementing(
						baseType, basePackage);

		// bind the classes found
		for (Class<? extends T> clazz : measurementClasses) {
			try {
				INamed measurement = (INamed) clazz.newInstance();
				bind(baseType)
						.annotatedWith(Names.named(measurement.getName()))
						.to(clazz);

			} catch (Exception e) {
				// rethrow the exception
				throw new Error(e);
			}

		}
	}
}
