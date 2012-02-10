package ch.ethz.ruediste.roofline.dom;

import java.util.Collection;

public abstract class ActionBase extends ActionBaseData {

	public abstract Collection<? extends KernelBase> getKernels();

}
