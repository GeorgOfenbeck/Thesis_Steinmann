package ch.ethz.ruediste.roofline.sharedEntities.measurers;

import ch.ethz.ruediste.roofline.sharedEntities.measurers.PerfEventCountData;

public class PerfEventCount extends PerfEventCountData {
	public double getScaledCount() {
		return getRawCount().doubleValue() * getTimeEnabled().doubleValue()
				/ getTimeRunning().doubleValue();
	}

	public boolean isMultiplexed() {
		return !getTimeEnabled().equals(getTimeRunning());
	}

	@Override
	public String toString() {
		return String.format("raw:%s running:%s enabled:%s scaled:%e",
				getRawCount(), getTimeRunning(), getTimeEnabled(),
				getScaledCount());
	}
}
