package ch.ethz.ruediste.roofline.statistics;

import java.util.Arrays;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

public class Histogram implements IAddValue {
	DescriptiveStatistics statistics = new DescriptiveStatistics();

	public void addValue(double d) {
		statistics.addValue(d);
	}

	public DescriptiveStatistics getStatistics() {
		return statistics;
	}

	public int[] getCounts(int binCount) {
		return getCounts(binCount, statistics.getMin(), statistics.getMax());
	}

	public double[] getBinCenters(int binCount) {
		return getBinCenters(binCount, statistics.getMin(), statistics.getMax());
	}

	public int[] getCounts(int binCount, double min, double max) {
		int[] counts = new int[binCount];

		// clear counts;
		Arrays.fill(counts, 0);

		double[] dataArray = statistics.getValues();

		double span = max - min;
		// process the data
		for (int i = 0; i < dataArray.length; i++) {
			// measurements which exactly hit the max boundary are included
			// as are measurements which exactly hit the min boundary
			if (dataArray[i] == max) {
				counts[binCount - 1]++;
			}
			else {
				double doubleBin = (dataArray[i] - min) / span;
				int bin = (int) Math.floor(doubleBin * binCount);
				if (bin >= 0 && bin < binCount)
					counts[bin]++;
			}
		}

		return counts;
	}

	public double[] getBinCenters(int binCount, double min, double max) {
		// determine min and max
		double span = max - min;

		double[] binCenters = new double[binCount];

		for (int i = 0; i < binCount; i++) {
			binCenters[i] = min + span * (0.5 + (double) i) / (binCount);
		}

		return binCenters;
	}
}
