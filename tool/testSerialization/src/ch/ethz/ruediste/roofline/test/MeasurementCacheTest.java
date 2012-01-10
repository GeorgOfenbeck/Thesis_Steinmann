package ch.ethz.ruediste.roofline.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.jmock.Expectations;
import org.jmock.Sequence;
import org.junit.Test;

import ch.ethz.ruediste.roofline.dom.DummyKernelDescription;
import ch.ethz.ruediste.roofline.dom.ExecutionTimeMeasurerOutput;
import ch.ethz.ruediste.roofline.dom.MeasurementCommand;
import ch.ethz.ruediste.roofline.dom.MeasurementDescription;
import ch.ethz.ruediste.roofline.dom.MeasurementResult;
import ch.ethz.ruediste.roofline.dom.PerfEventMeasurerOutput;
import ch.ethz.ruediste.roofline.measurementDriver.Configuration;
import ch.ethz.ruediste.roofline.measurementDriver.repositories.MeasurementRepository;
import ch.ethz.ruediste.roofline.measurementDriver.services.MeasurementCacheService;
import ch.ethz.ruediste.roofline.measurementDriver.services.MeasurementService;

public class MeasurementCacheTest extends TestBase {

	@Test
	public void testKeyGeneration() {
		MeasurementDescription measurement = new MeasurementDescription();

		MeasurementCacheService service = injector
				.getInstance(MeasurementCacheService.class);

		String key = service.getCacheKey(measurement);
		System.out.printf("Key: %s", key);

		assertTrue(key.length() >= 8);
	}

	@Test
	public void testCRUD() {
		// setup cache service
		MeasurementCacheService service = injector
				.getInstance(MeasurementCacheService.class);

		// setup measurements
		MeasurementDescription measurement1 = new MeasurementDescription();
		MeasurementDescription measurement2 = new MeasurementDescription();
		measurement2.setKernel(new DummyKernelDescription());

		// delete existing cache entry if present
		service.deleteFromCache(measurement1);

		// check if nothing is present yet
		assertNull(service.loadFromCache(measurement1));
		assertNull(service.loadFromCache(measurement2));

		// setup results
		MeasurementResult result1 = new MeasurementResult();
		result1.setMeasurement(measurement1);

		MeasurementResult result2 = new MeasurementResult();
		result2.setMeasurement(measurement1);
		result2.getOutputs().add(new ExecutionTimeMeasurerOutput());

		// store first result
		service.storeInCache(result1);

		// check
		assertNotNull(service.loadFromCache(measurement1));
		assertNull(service.loadFromCache(measurement2));
		assertEquals(0, service.loadFromCache(measurement1).getOutputs().size());

		// store second result
		service.storeInCache(result2);

		// check
		assertNotNull(service.loadFromCache(measurement1));
		assertEquals(1, service.loadFromCache(measurement1).getOutputs().size());

		// delete
		service.deleteFromCache(measurement1);

		// assert that nothing is present
		assertNull(service.loadFromCache(measurement1));
		assertNull(service.loadFromCache(measurement2));
	}

	@Test
	public void testMeasurementAppController() {
		final MeasurementCacheService cacheService = context
				.mock(MeasurementCacheService.class);
		final MeasurementService measurementService = context
				.mock(MeasurementService.class);

		MeasurementRepository controller = new MeasurementRepository();
		controller.cacheService = cacheService;
		controller.measurementService = measurementService;
		controller.configuration = injector.getInstance(Configuration.class);

		final Sequence seq = context.sequence("sequence");

		final MeasurementDescription measurement = new MeasurementDescription();
		final MeasurementResult result = new MeasurementResult();
		for (int i = 0; i < 10; i++) {
			result.getOutputs().add(new PerfEventMeasurerOutput());
		}
		result.setMeasurement(measurement);

		context.checking(new Expectations() {
			{
				oneOf(cacheService).loadFromCache(with(same(measurement)));
				will(returnValue(null));

				oneOf(cacheService).storeInCache(
						with(any(MeasurementResult.class)));

				oneOf(measurementService).measure(
						with(any(MeasurementCommand.class)));
				will(returnValue(result));
			}
		});

		controller.getMeasurementResults(measurement, 10);

		context.assertIsSatisfied();

	}
}
