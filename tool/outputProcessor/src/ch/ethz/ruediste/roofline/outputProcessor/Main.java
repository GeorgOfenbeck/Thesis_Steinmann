package ch.ethz.ruediste.roofline.outputProcessor;

import java.io.File;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math.stat.descriptive.rank.Median;

import ch.ethz.ruediste.roofline.sharedDOM.ExecutionTimeMeasurerOutput;
import ch.ethz.ruediste.roofline.sharedDOM.MeasurementResult;
import ch.ethz.ruediste.roofline.sharedDOM.MeasurementResultCollection;
import ch.ethz.ruediste.roofline.sharedDOM.MeasurerOutputBase;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		XStream xStream = new XStream(new DomDriver());

		MeasurementResultCollection results = (MeasurementResultCollection) xStream
				.fromXML(new File("measurementResults.xml"));

		for (MeasurementResult result : results.getResults()) {
			DescriptiveStatistics summary = new DescriptiveStatistics();
			Median median = new Median();
			for (MeasurerOutputBase output : result.getOutputs()) {
				ExecutionTimeMeasurerOutput et = (ExecutionTimeMeasurerOutput) output;
				summary.addValue(et.getUSecs());

			}
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
}
