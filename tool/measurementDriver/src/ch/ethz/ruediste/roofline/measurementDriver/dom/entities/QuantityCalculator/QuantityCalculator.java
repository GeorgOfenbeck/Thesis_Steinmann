package ch.ethz.ruediste.roofline.measurementDriver.dom.entities.QuantityCalculator;

import java.util.*;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

import ch.ethz.ruediste.roofline.measurementDriver.dom.quantities.Quantity;
import ch.ethz.ruediste.roofline.measurementDriver.util.IUnaryPredicate;
import ch.ethz.ruediste.roofline.sharedEntities.*;

/**
 * A quantity calculator provides a collection of measurers, which have to be
 * measured in order to measure a quantity. If outputs are present from all
 * required measurers, the getResult() method can be used to calculate a
 * quantity.
 */
public abstract class QuantityCalculator<TQuantity extends Quantity<TQuantity>> {

	/**
	 * Returns the quantity for each run, given the outputs.
	 * 
	 * @param runOutputs
	 *            for each measurement run, the measurer outputs for the
	 *            calculator
	 * @return the resulting quantity for each run
	 */
	public List<TQuantity> getResult(
			Iterable<Iterable<MeasurerOutputBase>> runOutputs) {
		ArrayList<TQuantity> result = new ArrayList<TQuantity>();
		for (Iterable<MeasurerOutputBase> outputs : runOutputs) {
			result.add(getSingleResult(outputs));
		}
		return result;
	}

	public DescriptiveStatistics getStatistics(
			Iterable<Iterable<MeasurerOutputBase>> runOutputs) {
		DescriptiveStatistics result = new DescriptiveStatistics();
		for (TQuantity value : getResult(runOutputs)) {
			result.addValue(value.getValue());
		}
		return result;
	}

	abstract public TQuantity getSingleResult(
			Iterable<MeasurerOutputBase> runOutputs);

	abstract public TQuantity getBestResult(
			Iterable<Iterable<MeasurerOutputBase>> runOutputs);

	/**
	 * returns the list of measurers which have to be measured in order to
	 * calculate the quantity.
	 */
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

}
