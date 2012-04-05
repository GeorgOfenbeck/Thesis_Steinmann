package ch.ethz.ruediste.roofline.measurementDriver.measurementControllers;

import java.io.IOException;

import ch.ethz.ruediste.roofline.measurementDriver.baseClasses.IMeasurementController;
import ch.ethz.ruediste.roofline.measurementDriver.controllers.RooflineController;
import ch.ethz.ruediste.roofline.measurementDriver.dom.services.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.services.QuantityMeasuringService.MemoryTransferBorder;
import ch.ethz.ruediste.roofline.sharedEntities.Operation;
import ch.ethz.ruediste.roofline.sharedEntities.kernels.WhtKernel;

import com.google.inject.Inject;

public class WhtMeasurementController implements IMeasurementController {

	public String getName() {
		return "wht";
	}

	public String getDescription() {
		return "starts a WHT measurement";
	}

	@Inject
	public QuantityMeasuringService quantityMeasuringService;

	@Inject
	public RooflineController rooflineController;

	public void measure(String outputName) throws IOException {
		rooflineController.addDefaultPeaks();
		rooflineController.setTitle("WHT");
		rooflineController.setOutputName(outputName);

		for (int size = 5; size < 20; size++) {
			WhtKernel kernel = new WhtKernel();
			kernel.setBufferSizeExp(size);
			rooflineController.addRooflinePoint("WHT", Integer.toString(size),
					kernel, Operation.CompInstr, MemoryTransferBorder.LlcRamBus);
		}
		rooflineController.plot();
	}

}
