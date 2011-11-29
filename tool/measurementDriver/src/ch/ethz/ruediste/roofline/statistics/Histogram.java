package ch.ethz.ruediste.roofline.statistics;

import java.util.Arrays;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

public class Histogram implements IAddValue {
	DescriptiveStatistics statistics = new DescriptiveStatistics();

	public void addValue(double d) {
		statistics.addValue(d);
	}

	public int[] getCounts(int binCount) {
		int[] counts = new int[binCount];

		// clear counts;
		Arrays.fill(counts, 0);

		double[] dataArray = statistics.getValues();

		// determine min and max
		double min, max;
		{
			min = getMin(binCount);
			max = getMax(binCount);
		}

		double span = max - min;
		// process the data
		for (int i = 0; i < dataArray.length; i++) {
			double doubleBin = (dataArray[i] - min) / span;
			int bin = (int) Math.floor(doubleBin * (binCount - 2));
			if (bin < 0)
				counts[0]++;
			else if (bin >= binCount - 2)
				counts[binCount - 1]++;
			else
				counts[bin + 1]++;
		}

		return counts;
	}

	private double getMax(int binCount) {
		// return statistics.getPercentile(100 - 100 / binCount);
		return statistics.getMax();
	}

	private double getMin(int binCount) {
		// return statistics.getPercentile(100 / binCount);
		return statistics.getMin();
	}

	public String[] getBinLabels(int binCount) {
		// determine min and max
		double min, max;
		{
			min = getMin(binCount);
			max = getMax(binCount);
		}
		double span = max - min;

		String[] binLabels = new String[binCount];
		binLabels[0] = "" + min;
		binLabels[binCount - 1] = "" + max;

		for (int i = 0; i < binCount - 2; i++) {
			double center = min + span * (0.5 + (double) i) / (binCount - 2);
			binLabels[i + 1] = String.format("%e", center);
		}

		return binLabels;
	}
}
