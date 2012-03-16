package ch.ethz.ruediste.roofline.sharedEntities.measurers;

import ch.ethz.ruediste.roofline.sharedEntities.MeasurerOutputBase;

public class ExecutionTimeMeasurerOutput extends
		ExecutionTimeMeasurerOutputData {

	@Override
	public void combineImp(MeasurerOutputBase a, MeasurerOutputBase b) {
		ExecutionTimeMeasurerOutput outA = (ExecutionTimeMeasurerOutput) a;
		ExecutionTimeMeasurerOutput outB = (ExecutionTimeMeasurerOutput) b;

		setUSecs(outA.getUSecs() + outB.getUSecs());
	}

}
