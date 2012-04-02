package ch.ethz.ruediste.roofline.sharedEntities.actions;

public class WaitForPressureBarrierAction extends
		WaitForPressureBarrierActionData {
	public WaitForPressureBarrierAction() {

	}

	public WaitForPressureBarrierAction(PressureBarrier barrier, int pressure) {
		setBarrier(barrier);
		setPressure(pressure);
	}
}
