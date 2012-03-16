package ch.ethz.ruediste.roofline.sharedEntities.measurers;

import java.io.PrintStream;
import java.math.BigInteger;

import ch.ethz.ruediste.roofline.sharedEntities.MeasurerOutputBase;

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

	@Override
	protected void combineImp(MeasurerOutputBase a, MeasurerOutputBase b) {
		PerfEventMeasurerOutput outA = (PerfEventMeasurerOutput) a;
		PerfEventMeasurerOutput outB = (PerfEventMeasurerOutput) b;

		for (PerfEventCount countA : outA.getEventCounts()) {
			PerfEventCount count = null;
			for (PerfEventCount countB : outB.getEventCounts()) {
				if (!countA.getDefinition().getDefinition()
						.equals(countB.getDefinition().getDefinition()))
					continue;
				if (!countA.getDefinition().getName()
						.equals(countB.getDefinition().getName()))
					continue;

				count = new PerfEventCount();
				count.setDefinition(countA.getDefinition());
				count.setTimeEnabled(BigInteger.ONE);
				count.setTimeRunning(BigInteger.ONE);
				count.setRawCount(BigInteger.valueOf((long) (countA
						.getScaledCount() + countB.getScaledCount())));
			}
			if (count == null) {
				throw new Error(
						"no count found in other measurer output for event "
								+ countA.getDefinition().getDefinition());
			}
			getEventCounts().add(count);
		}
	}
}
