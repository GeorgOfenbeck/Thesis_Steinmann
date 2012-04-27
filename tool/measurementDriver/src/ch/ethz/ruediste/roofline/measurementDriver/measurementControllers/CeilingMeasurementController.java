package ch.ethz.ruediste.roofline.measurementDriver.measurementControllers;

import java.io.IOException;

import ch.ethz.ruediste.roofline.measurementDriver.baseClasses.IMeasurementController;
import ch.ethz.ruediste.roofline.measurementDriver.dom.quantities.Performance;
import ch.ethz.ruediste.roofline.measurementDriver.dom.services.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.services.RooflineService.PeakAlgorithm;
import ch.ethz.ruediste.roofline.sharedEntities.*;

import com.google.inject.Inject;

public class CeilingMeasurementController implements IMeasurementController {

	public String getName() {
		return "ceiling";
	}

	public String getDescription() {
		return "measures ceilings";
	}

	@Inject
	RooflineService rooflineService;

	public void measure(String outputName) throws IOException {
		measure(InstructionSet.x87);
		measure(InstructionSet.SSE);
		measure(InstructionSet.SSEScalar);
	}

	private void measure(InstructionSet instSet) throws Error {
		{
			Performance perf = rooflineService.measurePeakPerformance(
					PeakAlgorithm.Add,
					instSet, ClockType.CoreCycles);
			System.out.println("Add perf: " + perf.getValue());
		}

		{
			Performance perf = rooflineService.measurePeakPerformance(
					PeakAlgorithm.Mul,
					instSet, ClockType.CoreCycles);
			System.out.println("Mul perf: " + perf.getValue());
		}

		{
			Performance perf = rooflineService.measurePeakPerformance(
					PeakAlgorithm.ArithBalanced,
					instSet, ClockType.CoreCycles);
			System.out.println("AddMul perf: " + perf.getValue());
		}
	}

}
