package ch.ethz.ruediste.roofline.measurementDriver.dom.entities.QuantityCalculator;

import java.util.List;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

import ch.ethz.ruediste.roofline.measurementDriver.dom.quantities.Quantity;
import ch.ethz.ruediste.roofline.measurementDriver.util.IUnaryPredicate;
import ch.ethz.ruediste.roofline.sharedEntities.*;

public abstract class QuantityCalculator<TQuantity extends Quantity<TQuantity>> {

	public enum Combination {
		Min,
		Max,
		Mean,
		Median,
		Sum
	}

	private Combination combination = Combination.Sum;

	abstract public TQuantity getResult(Iterable<MeasurerOutputBase> outputs);

	public abstract List<MeasurerBase> getRequiredMeasurers();

	public IUnaryPredicate<MeasurerOutputBase> isFromRequiredMeasurer() {
		return new IUnaryPredicate<MeasurerOutputBase>() {

			public Boolean apply(MeasurerOutputBase output) {
				// iterate over required measurers
				for (MeasurerBase measurer : getRequiredMeasurers()) {
					// is the ouput from the measurer?
					if (output.isFrom(measurer))
						return true;
				}

				return false;
			}
		};
	}

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
