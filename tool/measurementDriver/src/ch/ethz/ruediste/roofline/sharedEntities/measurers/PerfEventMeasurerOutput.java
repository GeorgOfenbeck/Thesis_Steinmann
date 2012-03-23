package ch.ethz.ruediste.roofline.sharedEntities.measurers;

import java.io.PrintStream;

public class PerfEventMeasurerOutput extends PerfEventMeasurerOutputData {
	/** get the event count with the given name */
	public PerfEventCount getEventCount(String name) {
		for (PerfEventCount count : getEventCounts()) {
			if (count.getDefinition().getName().equals(name)) {
				return count;
			}
		}
		throw new Error("no event count for event named <" + name + "> found!");
	}

	public void printRaw(String name, PrintStream out) {
		PerfEventCount count = getEventCount(name);

		out.printf("%s %s %s %g\n", count.getRawCount(),
				count.getTimeEnabled(), count.getTimeRunning(),
				count.getScaledCount());
	}

}
