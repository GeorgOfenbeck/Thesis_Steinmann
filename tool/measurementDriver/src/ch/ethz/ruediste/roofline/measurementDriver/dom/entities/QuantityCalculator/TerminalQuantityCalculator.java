package ch.ethz.ruediste.roofline.measurementDriver.dom.entities.QuantityCalculator;

import java.util.*;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

import ch.ethz.ruediste.roofline.measurementDriver.dom.quantities.Quantity;
import ch.ethz.ruediste.roofline.sharedEntities.MeasurerBase;

public abstract class TerminalQuantityCalculator<T extends Quantity<T>> extends
		QuantityCalculator<T> {

	protected final MeasurerBase requiredMeasurer;

	public TerminalQuantityCalculator(MeasurerBase requiredMeasurer) {
		this.requiredMeasurer = requiredMeasurer;

	}

	@Override
	public List<MeasurerBase> getRequiredMeasurers() {
		return Collections.<MeasurerBase> singletonList(requiredMeasurer);
	}

	/**
	 * If multiple measurerOutputs are present for a single required measurer,
	 * specifies how they should be combined.
	 */
	public enum Combination {
		Min,
		Max,
		Mean,
		Median,
		Sum
	}

	/**
	 * If multiple measurerOutputs are present for a single required measurer,
	 * specifies how they should be combined.
	 */
	private Combination combination = Combination.Sum;

	/**
	 * If multiple measurerOutputs are present for a single required measurer,
	 * specifies how they should be combined.
	 */
	public Combination getCombination() {
		return combination;
	}

	public void setCombination(Combination combination) {
		this.combination = combination;
	}

	protected double getValueRespectingCombination(DescriptiveStatistics stats) {
		switch (combination) {
		case Max:
			return stats.getMax();
		case Mean:
			return stats.getMean();
		case Median:
			return stats.getPercentile(50);
		case Min:
			return stats.getMin();
		case Sum:
			return stats.getSum();
		default:
			throw new Error("should not happen");
		}
	}
}
