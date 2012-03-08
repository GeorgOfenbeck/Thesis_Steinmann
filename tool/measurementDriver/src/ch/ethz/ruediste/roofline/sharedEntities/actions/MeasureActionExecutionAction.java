package ch.ethz.ruediste.roofline.sharedEntities.actions;

import ch.ethz.ruediste.roofline.sharedEntities.*;

public class MeasureActionExecutionAction extends
		MeasureActionExecutionActionData {
	public MeasureActionExecutionAction() {
	}

	public MeasureActionExecutionAction(ActionBase action, MeasurerSet set) {
		this();
		setAction(action);
		setMeasurerSet(set);
	}

}
