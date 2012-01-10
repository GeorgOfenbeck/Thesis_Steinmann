package ch.ethz.ruediste.roofline.measurementDriver.services;

import ch.ethz.ruediste.roofline.dom.KernelDescriptionBase;
import ch.ethz.ruediste.roofline.dom.MeasurementDescription;
import ch.ethz.ruediste.roofline.dom.MeasurementResult;
import ch.ethz.ruediste.roofline.dom.PerfEventMeasurerDescription;
import ch.ethz.ruediste.roofline.dom.PerfEventMeasurerOutput;
import ch.ethz.ruediste.roofline.dom.SimpleMeasurementSchemeDescription;
import ch.ethz.ruediste.roofline.measurementDriver.dom.Bandwidth;
import ch.ethz.ruediste.roofline.measurementDriver.dom.Performance;
import ch.ethz.ruediste.roofline.measurementDriver.dom.RooflinePoint;
import ch.ethz.ruediste.roofline.measurementDriver.repositories.MeasurementRepository;

import com.google.inject.Inject;

public class RooflineService {
	@Inject
	MeasurementRepository measurementRepository;

	public Performance getPerformance(String name,
			KernelDescriptionBase kernel) {
		System.out.printf("Measuring Performance of %s\n", name);
		double operations = measureEvent("coreduo::FP_COMPS_OP_EXE", kernel);
		double time = measureEvent("coreduo::UNHALTED_CORE_CYCLES", kernel);

		return new Performance(name, operations, time);
	}

	public Bandwidth getMemoryBandwidth(String name,
			KernelDescriptionBase kernel) {
		System.out.printf("Measuring MemoryBandwidth of %s\n", name);

		double bytes = measureEvent("coreduo::BUS_TRANS_MEM", kernel) * 64;
		double time = measureEvent("coreduo::UNHALTED_CORE_CYCLES", kernel);

		return new Bandwidth(name, bytes, time);
	}

	public RooflinePoint getRooflinePoint(String name,
			KernelDescriptionBase kernel) {
		System.out.printf("Measuring Roofline Point of %s\n", name);
		Bandwidth bandwidth = getMemoryBandwidth(name, kernel);
		Performance performance = getPerformance(name, kernel);

		return new RooflinePoint(name, performance.getOperations(),
				bandwidth.getTransferredBytes(), performance.getTime());
	}

	private double measureEvent(String event,
			KernelDescriptionBase kernel) {
		PerfEventMeasurerDescription measurer = new PerfEventMeasurerDescription();
		measurer.addEvent("event", event);

		MeasurementDescription measurement = new MeasurementDescription();
		measurement.setKernel(kernel);
		measurement.setMeasurer(measurer);
		measurement.setScheme(new SimpleMeasurementSchemeDescription());

		MeasurementResult result = measurementRepository.getMeasurementResults(
				measurement, 10);

		PerfEventMeasurerOutput.printRaw("event", result, System.out);

		return PerfEventMeasurerOutput.getStatistics("event", result)
				.getMin();

	}
}