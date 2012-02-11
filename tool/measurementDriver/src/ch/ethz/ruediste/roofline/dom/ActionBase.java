package ch.ethz.ruediste.roofline.dom;

import java.util.Collection;

public abstract class ActionBase extends ActionBaseData {

	public abstract Collection<? extends KernelBase> getKernels();

	public abstract Collection<? extends MeasurerSet> getMeasurerSets();

	public abstract Collection<? extends MeasurerBase> getMeasurers();

}
