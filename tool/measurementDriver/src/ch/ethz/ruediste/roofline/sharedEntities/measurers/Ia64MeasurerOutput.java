package ch.ethz.ruediste.roofline.sharedEntities.measurers;

import org.apache.commons.lang.NotImplementedException;

import ch.ethz.ruediste.roofline.sharedEntities.MeasurerOutputBase;

public class Ia64MeasurerOutput extends Ia64MeasurerOutputData {

	@Override
	protected void combineImp(MeasurerOutputBase a, MeasurerOutputBase b) {
		throw new NotImplementedException();
	}

}
