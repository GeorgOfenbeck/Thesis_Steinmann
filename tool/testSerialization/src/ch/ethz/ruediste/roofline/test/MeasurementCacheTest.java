package ch.ethz.ruediste.roofline.test;

import static org.junit.Assert.*;

import java.io.IOException;

import org.jmock.Expectations;
import org.junit.Test;

import ch.ethz.ruediste.roofline.dom.*;
import ch.ethz.ruediste.roofline.measurementDriver.Configuration;
import ch.ethz.ruediste.roofline.measurementDriver.appControllers.MeasurementAppController;
import ch.ethz.ruediste.roofline.measurementDriver.repositories.MeasurementResultRepository;
import ch.ethz.ruediste.roofline.measurementDriver.services.*;

public class MeasurementCacheTest extends TestBase {

	@Test
	public void testKeyGeneration() {
		Measurement measurement = new Measurement();

		HashService service = injector.getInstance(HashService.class);

		MeasurementHash key = service.getMeasurementHash(measurement);
		System.out.printf("Key: %s", key);

		assertTrue(key.getValue().length() >= 8);
	}

	@Test
	public void testCRUD() {
		// setup cache service
		MeasurementResultRepository measurementResultRepository = injector
				.getInstance(MeasurementResultRepository.class);

		// setup cache service
		HashService hashService = injector.getInstance(HashService.class);

		// setup measurements
		Measurement measurement1 = new Measurement();
		Measurement measurement2 = new Measurement();
		measurement2.setKernel(new DummyKernelDescription());

		// delete existing cache entry if present
		measurementResultRepository.delete(hashService
				.getMeasurementHash(measurement1));
		measurementResultRepository.delete(hashService
				.getMeasurementHash(measurement2));

		// check if nothing is present yet
		assertNull(measurementResultRepository.getMeasurementResult(hashService
				.getMeasurementHash(measurement1)));
		assertNull(measurementResultRepository.getMeasurementResult(hashService
				.getMeasurementHash(measurement2)));

		// setup results
		MeasurementResult result1 = new MeasurementResult();
		result1.setMeasurement(measurement1);

		MeasurementResult result2 = new MeasurementResult();
		result2.setMeasurement(measurement1);
		{
			MeasurementRunOutput output = new MeasurementRunOutput();
			output.setMainMeasurerOutput(new ExecutionTimeMeasurerOutput());
			result2.getOutputs().add(output);
		}

		// store first result
		measurementResultRepository.store(result1,
				hashService.getMeasurementHash(measurement1));

		// check
		assertNotNull(measurementResultRepository
				.getMeasurementResult(hashService
						.getMeasurementHash(measurement1)));
		assertNull(measurementResultRepository.getMeasurementResult(hashService
				.getMeasurementHash(measurement2)));
		assertEquals(
				0,
				measurementResultRepository
						.getMeasurementResult(
								hashService.getMeasurementHash(measurement1))
						.getOutputs().size());

		// store second result
		measurementResultRepository.store(result2,
				hashService.getMeasurementHash(measurement2));

		// check
		assertNotNull(measurementResultRepository
				.getMeasurementResult(hashService
						.getMeasurementHash(measurement1)));
		assertEquals(
				0,
				measurementResultRepository
						.getMeasurementResult(
								hashService.getMeasurementHash(measurement1))
						.getOutputs().size());
		assertNotNull(measurementResultRepository
				.getMeasurementResult(hashService
						.getMeasurementHash(measurement2)));

		// delete
		measurementResultRepository.delete(hashService
				.getMeasurementHash(measurement1));
		measurementResultRepository.delete(hashService
				.getMeasurementHash(measurement2));

		// assert that nothing is present
		assertNull(measurementResultRepository.getMeasurementResult(hashService
				.getMeasurementHash(measurement1)));
		assertNull(measurementResultRepository.getMeasurementResult(hashService
				.getMeasurementHash(measurement2)));

	}

	@Test
	public void testMeasurementAppController() throws IOException {
		final MeasurementResultRepository measurementResultRepository = context
				.mock(MeasurementResultRepository.class);
		final MeasurementService measurementService = context
				.mock(MeasurementService.class);

		final Configuration configuration = context.mock(Configuration.class);

		final HashService hashService = context.mock(HashService.class);

		final MeasurementAppController controller = new MeasurementAppController();
		controller.measurementResultRepository = measurementResultRepository;
		controller.measurementService = measurementService;
		controller.configuration = configuration;
		controller.hashService = hashService;

		// setup a measurement
		final Measurement measurement = new Measurement();

		// setup a measurement result
		final MeasurementResult result = new MeasurementResult();
		for (int i = 0; i < 10; i++) {
			MeasurementRunOutput output = new MeasurementRunOutput();
			output.setMainMeasurerOutput(new PerfEventMeasurerOutput());
			result.getOutputs().add(output);
		}
		result.setMeasurement(measurement);

		// calculate measurement hash
		final MeasurementHash measurementHash = new MeasurementHash("abc");
		final CoreHash coreHash = new CoreHash("def");

		context.checking(new Expectations() {
			{

				oneOf(measurementResultRepository).getMeasurementResult(
						with(equal(measurementHash)));
				will(returnValue(null));

				oneOf(measurementResultRepository).store(
						with(any(MeasurementResult.class)),
						with(equal(measurementHash)));

				oneOf(measurementService).prepareMeasuringCoreBuilding(
						with(same(measurement)));
				will(returnValue(false));

				oneOf(measurementService).compilePreparedMeasuringCore(
						with(same(measurement)));

				oneOf(hashService).getMeasurementHash(with(same(measurement)));
				will(returnValue(measurementHash));

				oneOf(configuration).get(
						MeasurementAppController.useCachedResultsKey);
				will(returnValue(true));

				oneOf(hashService).hashCurrentlyCompiledMeasuringCore();
				will(returnValue(coreHash));

				oneOf(measurementService).runMeasuringCore(
						with(any(MeasurementCommand.class)));
				will(returnValue(result));
			}
		});

		controller.measure(measurement, 10);

		context.assertIsSatisfied();

	}
}
