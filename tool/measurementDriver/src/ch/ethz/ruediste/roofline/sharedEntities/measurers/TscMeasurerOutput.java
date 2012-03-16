package ch.ethz.ruediste.roofline.sharedEntities.measurers;

import ch.ethz.ruediste.roofline.sharedEntities.MeasurerOutputBase;

public class TscMeasurerOutput extends TscMeasurerOutputData {

	@Override
	protected void combineImp(MeasurerOutputBase a, MeasurerOutputBase b) {
		TscMeasurerOutput outA = (TscMeasurerOutput) a;
		TscMeasurerOutput outB = (TscMeasurerOutput) b;
		setTics(outA.getTics().add(outB.getTics()));
	}

}
