package ch.ethz.ruediste.roofline.sharedEntities.measurers;

import java.io.PrintStream;
import java.math.BigInteger;
import java.util.ArrayList;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.apache.log4j.Logger;

import ch.ethz.ruediste.roofline.entities.*;
import ch.ethz.ruediste.roofline.measurementDriver.*;
import ch.ethz.ruediste.roofline.measurementDriver.util.*;

public class PerfEventMeasurer extends PerfEventMeasurerData implements
		IMeasurer<PerfEventMeasurerOutput> {

	private static Logger log = Logger.getLogger(PerfEventMeasurer.class);

	public static ConfigurationKey<Boolean> validateEventWasRunningKey = ConfigurationKey
			.Create(Boolean.class,
					"validation.perfEventRunning",
					"if true, check that the performance event was running during the measurement run",
					true);

	public static ConfigurationKey<Boolean> validateEventNotMultiplexedKey = ConfigurationKey
			.Create(Boolean.class,
					"validation.eventNotMultiplexed",
					"if true, check that the performance event was not multiplexed",
					true);

	public PerfEventMeasurer() {
	}

	public PerfEventMeasurer(String name, String definition) {
		addEvent(name, definition);
	}

	public PerfEventMeasurer(Pair<String, String>... events) {
		for (Pair<String, String> event : events) {
			addEvent(event.getLeft(), event.getRight());
		}
	}

	public void addEvent(String name, String definition) {
		PerfEventDefinition def = new PerfEventDefinition();
		def.setName(name);
		def.setDefinition(definition);
		getEvents().add(def);
	}

	/**
	 * prints a raw value dump into the specified stream
	 */
	public void printRaw(String name, MeasurementResult result, PrintStream out) {
		out.printf("Event: %s, <raw> <enabled> <running> <scaled>\n", name);

		// iterate over all outputs
		for (PerfEventMeasurerOutput output : result.getMeasurerOutputs(this)) {
			output.printRaw(name, out);
		}
	}

	public void addValues(String name, MeasurementResult result,
			IUnaryAction<Double> addValue) {
		// iterate over all outputs
		for (PerfEventMeasurerOutput output : result.getMeasurerOutputs(this)) {
			addValue.apply(output.getEventCount(name).getScaledCount());
		}
	}

	public void addValues(String name, MeasurementResult result,
			final DescriptiveStatistics statistics) {
		addValues(name, result, new IUnaryAction<Double>() {
			public void apply(Double v) {
				statistics.addValue(v);
			}
		});
	}

	/**
	 * creates statistics of all event counts in the given measurement result
	 */
	public DescriptiveStatistics getStatistics(String name,
			MeasurementResult result) {
		final DescriptiveStatistics statistics = new DescriptiveStatistics();

		addValues(name, result, statistics);

		return statistics;
	}

	public Iterable<Double> getDoubles(String name,
			MeasurementResult meaurementResult) {
		ArrayList<Double> result = new ArrayList<Double>();
		// iterate over all outputs
		for (PerfEventMeasurerOutput output : meaurementResult
				.getMeasurerOutputs(this)) {
			PerfEventCount eventCount = output.getEventCount(name);
			result.add(eventCount.getScaledCount());
		}
		return result;
	}

	public Iterable<BigInteger> getBigIntegers(String name,
			MeasurementResult meaurementResult) {
		ArrayList<BigInteger> result = new ArrayList<BigInteger>();
		// iterate over all outputs
		for (PerfEventMeasurerOutput output : meaurementResult
				.getMeasurerOutputs(this)) {
			PerfEventCount eventCount = output.getEventCount(name);
			result.add(eventCount.getRawCount());
		}
		return result;
	}

	public void validate(PerfEventMeasurerOutput output,
			MeasurementResult measurementResult) {

		for (PerfEventCount eventCount : output.getEventCounts()) {
			validate(eventCount, measurementResult);
		}

	}

	public void validate(PerfEventCount eventCount,
			MeasurementResult measurementResult) throws Error {

		// get the validation configuration
		ValidationData validationData = measurementResult.getMeasurement()
				.getValidationData();
		if (validationData == null) {
			return;
		}
		Configuration validationConfiguration = validationData
				.getConfiguration();

		// chech that the event was running
		if (validationConfiguration.get(validateEventWasRunningKey)
				&& eventCount.getTimeRunning().equals(BigInteger.ZERO)) {
			log.warn(String.format("event %s=%s was never enabled", eventCount
					.getDefinition().getName(), eventCount.getDefinition()
					.getDefinition()));
		}

		// chech that the event was not multiplexed
		if (validationConfiguration.get(validateEventNotMultiplexedKey)
				&& eventCount.isMultiplexed()) {
			log.warn("event was multiplexed");
		}
	}

	public BigInteger getMinBigInteger(String name, MeasurementResult result) {

		PerfEventCount eventCount = getMinOutput(name, result).getEventCount(
				name);

		if (eventCount.isMultiplexed()) {
			throw new Error("Result is multiplexed");
		}

		return eventCount.getRawCount();
	}

	public double getMinDouble(String name, MeasurementResult measurementResult) {
		return getMinOutput(name, measurementResult).getEventCount(name)
				.getScaledCount();
	}

	public PerfEventMeasurerOutput getMinOutput(final String name,
			MeasurementResult measurementResult) {
		PerfEventMeasurerOutput min = IterableUtils
				.foldl(measurementResult.getMeasurerOutputs(this),
						null,
						new IBinaryFunction<PerfEventMeasurerOutput, PerfEventMeasurerOutput, PerfEventMeasurerOutput>() {

							public PerfEventMeasurerOutput apply(
									PerfEventMeasurerOutput result,
									PerfEventMeasurerOutput item) {

								if (result == null
										|| item.getEventCount(name)
												.getScaledCount() < result
												.getEventCount(name)
												.getScaledCount()) {
									return item;
								}

								return result;
							}
						});
		validate(min, measurementResult);
		return min;
	}

}
