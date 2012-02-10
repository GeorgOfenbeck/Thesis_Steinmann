package ch.ethz.ruediste.roofline.dom;

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
