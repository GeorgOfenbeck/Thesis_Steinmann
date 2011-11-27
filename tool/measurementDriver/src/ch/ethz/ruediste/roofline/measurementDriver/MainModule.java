package ch.ethz.ruediste.roofline.measurementDriver;

import ch.ethz.ruediste.roofline.dom.MultiLanguageSerializationService;
import ch.ethz.ruediste.roofline.measurementDriver.appControllers.MeasurementAppController;
import ch.ethz.ruediste.roofline.measurementDriver.measurements.VarianceMeasurement;
import ch.ethz.ruediste.roofline.measurementDriver.services.MeasurementService;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class MainModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(MeasurementService.class);
		bind(MeasurementAppController.class);
		bind(MultiLanguageSerializationService.class);
		bind(VarianceMeasurement.class)
				.annotatedWith(Names.named("variance"));

		XStream xStream = new XStream(new DomDriver());
		bind(XStream.class).toInstance(xStream);
	}

}
