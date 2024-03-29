package ch.ethz.ruediste.roofline.measurementDriver.measurementControllers;

import java.io.IOException;

import org.apache.log4j.Logger;

import ch.ethz.ruediste.roofline.measurementDriver.baseClasses.IMeasurementController;
import ch.ethz.ruediste.roofline.measurementDriver.dom.services.SystemInfoService;
import ch.ethz.ruediste.roofline.measurementDriver.util.Instantiator;

import com.google.inject.Inject;

public class ReportMeasurementController implements IMeasurementController {
	private static final Logger log = Logger
			.getLogger(ReportMeasurementController.class);

	public String getName() {
		return "report";
	}

	public String getDescription() {
		return "generates all plots required for the report";
	}

	@Inject
	Instantiator instantiator;

	@Inject
	SystemInfoService systemInfoService;

	public void measure(String outputName) throws IOException {
		measure(ValidateTimeMeasurementController.class);
		measure(ValidateTransferredBytesMeasurementController.class);
		measure(ValidateOpCountMeasurementController.class);
		measure(DgemvMeasurementController.class);
	}

	private void measure(Class<? extends IMeasurementController> clazz) {
		IMeasurementController measurementController = instantiator
				.getInstance(clazz);
		String outputName = systemInfoService.getCpuType().toString()
				.toLowerCase()
				+ "/" + measurementController.getName();

		log.info("measuring " + outputName);

		try {

			measurementController.measure(outputName);
		}
		catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
