package ch.ethz.ruediste.roofline.measurementDriver.repositories;

import java.util.List;

import ch.ethz.ruediste.roofline.dom.*;
import ch.ethz.ruediste.roofline.measurementDriver.services.MeasurementService;
import ch.ethz.ruediste.roofline.measurementDriver.util.*;

import com.google.inject.Inject;

public class PmuRepository {
	@Inject
	MeasurementService measurementService;

	private List<PmuDescription> allPmusImp;

	public String getAvailableEvent(String... events) {
		return IterableUtils.single(events, new IUnaryPredicate<String>() {

			public Boolean apply(String event) {
				String[] eventParts = event.split("::");
				return getPMU(eventParts[0]) != null;
			}
		});
	}

	public List<PmuDescription> getAllPmus() {
		if (allPmusImp == null) {
			allPmusImp = readPMUs();
		}
		return allPmusImp;
	}

	public PmuDescription getPMU(final String pmuName) {
		return IterableUtils.singleOrDefault(getAllPmus(),
				new IUnaryPredicate<PmuDescription>() {
					public Boolean apply(PmuDescription pmu) {
						return pmu.getPmuName().equals(pmuName);
					}
				});
	}

	private List<PmuDescription> readPMUs() {
		// list all available performance counters
		ListEventsMeasurerDescription measurer = new ListEventsMeasurerDescription();

		MeasurementDescription measurement = new MeasurementDescription();
		measurement.setKernel(new DummyKernelDescription());
		measurement.setMeasurer(measurer);
		measurement.setScheme(new SimpleMeasurementSchemeDescription());

		MeasurementResult result = measurementService.measure(
				measurement, 1);

		ListEventsMeasurerOutput output = (ListEventsMeasurerOutput) result
				.getOutputs().get(0);

		return output.getPmus();
	}

	public Iterable<PmuDescription> getPresentPmus() {
		return IterableUtils.where(getAllPmus(),
				new IUnaryPredicate<PmuDescription>() {
					public Boolean apply(PmuDescription pmu) {
						return pmu.getIsPresent();
					}
				});
	}
}
