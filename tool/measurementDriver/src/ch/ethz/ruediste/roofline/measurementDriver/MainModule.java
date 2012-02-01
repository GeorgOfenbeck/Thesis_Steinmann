package ch.ethz.ruediste.roofline.measurementDriver;

import java.util.List;

import ch.ethz.ruediste.roofline.measurementDriver.appControllers.*;
import ch.ethz.ruediste.roofline.measurementDriver.baseClasses.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.AxisConverter;

import com.google.inject.*;
import com.google.inject.name.Names;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class MainModule extends AbstractModule {

	@Override
	protected void configure() {
		// setup XStream
		XStream xStream = new XStream(new DomDriver());
		xStream.registerConverter(new AxisConverter());
		xStream.processAnnotations(ClassFinder.getClasses(
				"ch.ethz.ruediste.roofline").toArray(new Class[] {}));
		bind(XStream.class).toInstance(xStream);

		bind(IMeasurementFacilility.class).to(MeasurementAppController.class);

		// setup services
		bindAllAsSingletons(Object.class,
				"ch.ethz.ruediste.roofline.measurementDriver.services");

		// setup app controllers
		bindAllAsSingletons(Object.class,
				"ch.ethz.ruediste.roofline.measurementDriver.appControllers");

		// setup repositories
		bindAllAsSingletons(Object.class,
				"ch.ethz.ruediste.roofline.measurementDriver.repositories");

		// setup measurements and commands
		bindNamed(IMeasurementController.class,
				"ch.ethz.ruediste.roofline.measurementDriver.measurementControllers");

		bindNamed(ICommandController.class,
				"ch.ethz.ruediste.roofline.measurementDriver.commandControllers");

	}

	private <T> void bindAllAsSingletons(Class<T> baseType, String basePackage) {
		List<Class<? extends T>> classes = ClassFinder.getClassesImplementing(
				baseType, basePackage);
		// bind the classes found
		for (Class<? extends T> clazz : classes) {
			if (clazz.isInterface())
				continue;
			bind(clazz).in(Singleton.class);
		}
	}

	private <T extends INamed> void bindNamed(Class<T> baseType,
			String basePackage) {
		// get all classes implementing the given base type
		List<Class<? extends T>> measurementClasses = ClassFinder
				.getClassesImplementing(baseType, basePackage);

		// bind the classes found
		for (Class<? extends T> clazz : measurementClasses) {
			try {
				INamed measurement = (INamed) clazz.newInstance();
				bind(baseType)
						.annotatedWith(Names.named(measurement.getName())).to(
								clazz);

			}
			catch (Exception e) {
				// rethrow the exception
				throw new Error(e);
			}

		}
	}
}
