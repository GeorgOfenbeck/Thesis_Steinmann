package ch.ethz.ruediste.roofline.measurementDriver;

import java.io.InputStream;

import org.apache.commons.configuration.CombinedConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

import ch.ethz.ruediste.roofline.dom.MultiLanguageSerializationService;
import ch.ethz.ruediste.roofline.measurementDriver.appControllers.MeasurementAppController;
import ch.ethz.ruediste.roofline.measurementDriver.baseClasses.IMeasurement;
import ch.ethz.ruediste.roofline.measurementDriver.measurements.RawDataMeasurement;
import ch.ethz.ruediste.roofline.measurementDriver.measurements.VarianceHistogramMeasurement;
import ch.ethz.ruediste.roofline.measurementDriver.measurements.VarianceMeasurement;
import ch.ethz.ruediste.roofline.measurementDriver.services.MeasurementCacheService;
import ch.ethz.ruediste.roofline.measurementDriver.services.MeasurementService;

import com.google.inject.AbstractModule;
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
			defaultConfiguration.load(configStream);
		} catch (ConfigurationException e) {
			throw new Error(e);
		}

		CombinedConfiguration combinedConfiguration = new CombinedConfiguration();
		combinedConfiguration.addConfiguration(defaultConfiguration);

		bind(Configuration.class).toInstance(combinedConfiguration);

		// setup services
		bind(MeasurementService.class);
		bind(MultiLanguageSerializationService.class);
		bind(MeasurementCacheService.class);

		// setup app controllers
		bind(MeasurementAppController.class);

		// setup XStream
		XStream xStream = new XStream(new DomDriver());
		bind(XStream.class).toInstance(xStream);

		// setup measurements
		bindMeasurement(VarianceMeasurement.class, "variance");
		bindMeasurement(VarianceHistogramMeasurement.class, "varianceHist");
		bindMeasurement(RawDataMeasurement.class, "raw");
	}

	private <T extends IMeasurement> void bindMeasurement(
			Class<T> measurementClass,
			String name) {
		bind(IMeasurement.class)
				.annotatedWith(Names.named(name))
				.to(measurementClass);
	}

}
