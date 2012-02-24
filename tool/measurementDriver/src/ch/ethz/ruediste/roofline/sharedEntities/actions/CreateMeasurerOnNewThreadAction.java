package ch.ethz.ruediste.roofline.sharedEntities.actions;

import java.util.*;

import ch.ethz.ruediste.roofline.sharedEntities.*;

public class CreateMeasurerOnNewThreadAction extends
		CreateMeasurerOnNewThreadActionData {

	@Override
	public Collection<? extends KernelBase> getKernels() {
		return Collections.emptyList();
	}

	@Override
	public Collection<? extends MeasurerSet> getMeasurerSets() {
		return Collections.emptyList();
	}

	@Override
	public Collection<? extends MeasurerBase> getMeasurers() {
		return Collections.emptyList();
	}

}
