package ch.ethz.ruediste.roofline.outputProcessor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

import ch.ethz.ruediste.roofline.sharedDOM.ExecutionTimeMeasurerOutput;
import ch.ethz.ruediste.roofline.sharedDOM.MeasurementResult;
import ch.ethz.ruediste.roofline.sharedDOM.MeasurementResultCollection;
import ch.ethz.ruediste.roofline.sharedDOM.MeasurerOutputBase;
import ch.ethz.ruediste.roofline.sharedDOM.MemoryLoadKernelDescription;
import ch.ethz.ruediste.roofline.sharedDOM.PerfEventCount;
import ch.ethz.ruediste.roofline.sharedDOM.PerfEventMeasurerOutput;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class Main {

	static private class DoubleValue {
		private double value;
		private double stdev;
		private double min;
		private double max;

		public DoubleValue(double value, double stdev, double min, double max) {
			super();
			this.value = value;
			this.stdev = stdev;
			this.min = min;
			this.max = max;
		}

		public DoubleValue(DescriptiveStatistics summary) {
			value = summary.getMean();
			stdev = summary.getStandardDeviation();
			min = summary.getMin();
			max = summary.getMax();
		}

		public DoubleValue(double value) {
			this.value = value;
			stdev = 0;
			min = value;
			max = value;
		}

		public double getValue() {
			return value;
		}

		public double getStdev() {
			return stdev;
		}

		public double getMin() {
			return min;
		}

		public double getMax() {
			return max;
		}

		public DoubleValue Minus(DoubleValue other) {
			double min =
					Math.min(
							Math.min(this.min - other.min, this.max - other.min),
							Math.min(this.min - other.max, this.max - other.max)
							);
			double max = Math.max(
					Math.max(this.min - other.min, this.max - other.min),
					Math.max(this.min - other.max, this.max - other.max)
					);
			return new DoubleValue(
					value - other.value,
					SumDev(other), min, max);
		}

		public DoubleValue Plus(DoubleValue other) {
			double min =
					Math.min(
							Math.min(this.min + other.min, this.max + other.min),
							Math.min(this.min + other.max, this.max + other.max)
							);
			double max = Math.max(
					Math.max(this.min + other.min, this.max + other.min),
					Math.max(this.min + other.max, this.max + other.max)
					);
			return new DoubleValue(
					value + other.value,
					SumDev(other), min, max);
		}

		private double SumDev(DoubleValue other) {
			return Math.sqrt(
					Math.pow(value * stdev, 2)
							+ Math.pow(other.value * other.stdev, 2)
					);
		}

		public DoubleValue Multiply(DoubleValue other) {
			double min =
					Math.min(
							Math.min(this.min * other.min, this.max * other.min),
							Math.min(this.min * other.max, this.max * other.max)
							);
			double max = Math.max(
					Math.max(this.min * other.min, this.max * other.min),
					Math.max(this.min * other.max, this.max * other.max)
					);
			double newValue = value * other.value;
			return new DoubleValue(newValue, MulDev(newValue, other), min, max);
		}

		public DoubleValue Divide(DoubleValue other) {
			double min =
					Math.min(
							Math.min(this.min / other.min, this.max / other.min),
							Math.min(this.min / other.max, this.max / other.max)
							);
			double max = Math.max(
					Math.max(this.min / other.min, this.max / other.min),
					Math.max(this.min / other.max, this.max / other.max)
					);
			double newValue = value / other.value;
			return new DoubleValue(newValue, MulDev(newValue, other), min, max);
		}

		private double MulDev(double newValue, DoubleValue other) {
			return Math.abs(newValue) * Math.sqrt(
					Math.pow(stdev / value, 2)
							+ Math.pow(other.stdev / other.value, 2));
		}
	}

	static private class BlockSizeResult {
		DoubleValue perfEventValue;
		DoubleValue etValue;
	}

	/**
	 * @param args
	 * @throws FileNotFoundException
	 */
	public static void main(String[] args) throws FileNotFoundException {
		XStream xStream = new XStream(new DomDriver());

		MeasurementResultCollection results = (MeasurementResultCollection) xStream
				.fromXML(new File("measurementResults.xml"));

		HashMap<Long, BlockSizeResult> map = new HashMap<Long, Main.BlockSizeResult>();

		for (MeasurementResult result : results.getResults()) {
			DescriptiveStatistics summaryPerf = new DescriptiveStatistics();
			DescriptiveStatistics summaryEt = new DescriptiveStatistics();

			for (MeasurerOutputBase output : result.getOutputs()) {
				if (output instanceof ExecutionTimeMeasurerOutput) {
					ExecutionTimeMeasurerOutput et = (ExecutionTimeMeasurerOutput) output;
					summaryEt.addValue(et.getUSecs());
				}
				if (output instanceof PerfEventMeasurerOutput) {
					PerfEventMeasurerOutput out = (PerfEventMeasurerOutput) output;
					PerfEventCount count = out.getEventCounts().get(0);
					double rawCount = count.getRawCount().doubleValue();
					double timeEnabled = count.getTimeEnabled().doubleValue();
					double timeRunning = count.getTimeRunning().doubleValue();
					summaryPerf.addValue(rawCount * timeEnabled / timeRunning);
				}

			}

			MemoryLoadKernelDescription kernel = (MemoryLoadKernelDescription) result
					.getMeasurement().getKernel();
			if (!map.containsKey(kernel.getBlockSize())) {
				map.put(kernel.getBlockSize(), new BlockSizeResult());
			}
			BlockSizeResult tmp = map.get(kernel.getBlockSize());
			if (summaryEt.getN() > 0) {
				tmp.etValue = new DoubleValue(summaryEt);

			}
			if (summaryPerf.getN() > 0) {
				tmp.perfEventValue = new DoubleValue(summaryPerf);
			}
		}

		ArrayList<Long> keyList = new ArrayList<Long>(map.keySet());
		Collections.sort(keyList);

		File dataFile = new File("plot.data");
		PrintStream dataPrintStream = new PrintStream(
				dataFile);

		dataPrintStream.println("# [ET]");
		for (long key : keyList) {
			BlockSizeResult tmp = map.get(key);
			if (tmp.etValue == null)
				continue;
			dataPrintStream.printf("%d %f %f\n",
					key, tmp.etValue.getValue(), tmp.etValue.getStdev());
		}

		dataPrintStream.println();
		dataPrintStream.println();
		dataPrintStream.println("# [PerfEvents]");

		for (long key : keyList) {
			BlockSizeResult tmp = map.get(key);
			if (tmp.perfEventValue == null)
				continue;
			dataPrintStream.printf("%d %f %f\n",
					key, tmp.perfEventValue.getValue(),
					tmp.perfEventValue.getStdev());
		}

		// calculate cycles per second
		dataPrintStream.println();
		dataPrintStream.println();
		dataPrintStream.println("# [cps]");

		for (long key : keyList) {
			BlockSizeResult tmp = map.get(key);
			if (tmp.perfEventValue == null || tmp.etValue == null)
				continue;
			DoubleValue cps = tmp.perfEventValue.Divide(tmp.etValue
					.Divide(new DoubleValue(1e6)));

			dataPrintStream.printf("%e %e %e %e %e %e %e\n",
					tmp.etValue.getValue() * 1e-6,
					cps.getValue(),
					cps.getValue() - cps.getStdev(),
					cps.getValue() + cps.getStdev(),
					cps.getMin(),
					cps.getMax(),
					cps.getStdev() / cps.getValue());
		}

		dataPrintStream.close();
	}

	static void printSummary(DescriptiveStatistics summary) {
		System.out.println("Measurement");
		System.out.print("number of outputs: ");
		System.out.println(summary.getN());
		System.out.print("mean:");
		System.out.println(summary.getMean());
		System.out.print("stddev:");
		System.out.println(summary.getStandardDeviation());
		System.out.print("relative:");
		System.out.println(summary.getStandardDeviation()
				/ summary.getMean());
		System.out.print("median:");
		System.out.println(summary.getPercentile(50));
		System.out.print("min:");
		System.out.println(summary.getMin());
		System.out.print("max:");
		System.out.println(summary.getMax());
		System.out.println();
	}
}
