package ch.ethz.ruediste.roofline.dom;

import java.util.*;

public class RuleBase extends RuleBaseData {

	public Collection<? extends KernelBase> getKernels() {
		ArrayList<KernelBase> result = new ArrayList<KernelBase>();
		if (getAction() != null) {
			result.addAll(getAction().getKernels());
		}
		return result;
	}

}
