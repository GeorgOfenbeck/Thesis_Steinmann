package ch.ethz.ruediste.roofline.measurementDriver.services;

import ch.ethz.ruediste.roofline.dom.KernelDescriptionBase;
import ch.ethz.ruediste.roofline.measurementDriver.dom.*;

import com.google.inject.Inject;

public class RooflineService {
	// private static final String memEvent = "coreduo::BUS_TRANS_MEM";
	// private static final String cycleEvent = "coreduo::UNHALTED_CORE_CYCLES";
	// private static final String operationEvent =
	// "coreduo::FP_COMP_INSTR_RET";

	private static final String memEvent = "core::BUS_TRANS_MEM";
	private static final String cycleEvent = "core::UNHALTED_CORE_CYCLES";
	private static final String operationEvent = "core::FP_COMP_OPS_EXE";

	@Inject
	MeasurementService measurementService;

	public Performance getPerformance(String name, KernelDescriptionBase kernel) {
		System.out.printf("Measuring Performance of %s\n", name);
		double operations = measureEvent(operationEvent, kernel);
		double time = measureEvent(cycleEvent, kernel);

		return new Performance(name, operations, time);
	}

	public Bandwidth getMemoryBandwidth(String name,
			KernelDescriptionBase kernel) {
		System.out.printf("Measuring MemoryBandwidth of %s\n", name);

		double bytes = measureEvent(memEvent, kernel) * 64;
		double time = measureEvent(cycleEvent, kernel);

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

	private double measureEvent(String event, KernelDescriptionBase kernel) {
		return measurementService.getStatistics(event, kernel, 10).getMin();

	}
}
