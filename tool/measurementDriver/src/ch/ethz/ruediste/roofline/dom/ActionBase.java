package ch.ethz.ruediste.roofline.dom;

import java.util.*;

public abstract class ActionBase extends ActionBaseData {

	public Collection<? extends KernelBase> getKernels() {
		return new ArrayList<KernelBase>();
	}

	public Collection<? extends MeasurerSet> getMeasurerSets() {
		return new ArrayList<MeasurerSet>();
	}

	public Collection<? extends MeasurerBase> getMeasurers() {
		return new ArrayList<MeasurerBase>();
	}

}
