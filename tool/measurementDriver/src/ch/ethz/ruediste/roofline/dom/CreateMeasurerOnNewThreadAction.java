package ch.ethz.ruediste.roofline.dom;

import java.util.*;

public class CreateMeasurerOnNewThreadAction extends
		CreateMeasurerOnNewThreadActionData {

	@Override
	public Collection<? extends KernelBase> getKernels() {
		return Collections.emptyList();
	}

}
