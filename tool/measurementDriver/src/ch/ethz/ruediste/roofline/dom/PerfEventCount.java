package ch.ethz.ruediste.roofline.dom;

public class PerfEventCount extends PerfEventCountData {
	public double getScaledCount() {
		return getRawCount().doubleValue()
				* getTimeEnabled().doubleValue()
				/ getTimeRunning().doubleValue();
	}

	public boolean isMultiplexed() {
		return !getTimeEnabled()
				.equals(getTimeRunning());
	}
}
