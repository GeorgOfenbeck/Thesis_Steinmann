package ch.ethz.ruediste.roofline.measurementDriver.repositories;

import java.util.List;

import ch.ethz.ruediste.roofline.dom.*;
import ch.ethz.ruediste.roofline.measurementDriver.services.MeasurementService;
import ch.ethz.ruediste.roofline.measurementDriver.util.*;

import com.google.inject.Inject;

/**
 * Repository giving access to the descriptions of the Performance Measuring
 * Units (PMUs) available on the system
 */
public class PmuRepository {
	@Inject
	MeasurementService measurementService;

	private List<PmuDescription> allPmusImp;
	private Iterable<PmuDescription> presentPmusImp;

	public String getAvailableEvent(String... events) {
		return IterableUtils.single(events, new IUnaryPredicate<String>() {

			public Boolean apply(String event) {
				String[] eventParts = event.split("::");
				return getPresentPMU(eventParts[0]) != null;
			}
		});
	}

	public List<PmuDescription> getAllPmus() {
		if (allPmusImp == null) {
			allPmusImp = readPMUs(false);
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

	public PmuDescription getPresentPMU(final String pmuName) {
		return IterableUtils.singleOrDefault(getAllPmus(),
				new IUnaryPredicate<PmuDescription>() {
					public Boolean apply(PmuDescription pmu) {
						return pmu.getIsPresent()
								&& pmu.getPmuName().equals(pmuName);
					}
				});
	}

	private List<PmuDescription> readPMUs(Boolean onlyPresent) {
		// list all available performance counters
		ListEventsMeasurerDescription measurer = new ListEventsMeasurerDescription();
		measurer.setOnlyPresent(onlyPresent);

		MeasurementDescription measurement = new MeasurementDescription();
		measurement.setKernel(new DummyKernelDescription());
		measurement.setMeasurer(measurer);
		measurement.setScheme(new SimpleMeasurementSchemeDescription());

		MeasurementResult result = measurementService.measure(
				measurement, 1);

		ListEventsMeasurerOutput output = IterableUtils.single(result
				.getMeasurerOutputs(measurer));

		return output.getPmus();
	}

	public Iterable<PmuDescription> getPresentPmus() {
		// have the present pmus been accessed already?
		if (presentPmusImp == null) {
			// are all pmus loaded already?
			if (allPmusImp != null) {
				// get list of present pmus from list of all pmus
				presentPmusImp = IterableUtils.where(getAllPmus(),
						new IUnaryPredicate<PmuDescription>() {
							public Boolean apply(PmuDescription pmu) {
								return pmu.getIsPresent();
							}
						});
			}
			else {
				// only retrieve the present pmus
				presentPmusImp = readPMUs(true);
			}
		}

		// return the list of present pmus
		return presentPmusImp;
	}
}
