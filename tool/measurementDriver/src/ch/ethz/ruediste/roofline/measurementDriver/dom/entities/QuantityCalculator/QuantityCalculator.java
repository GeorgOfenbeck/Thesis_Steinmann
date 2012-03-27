package ch.ethz.ruediste.roofline.measurementDriver.dom.entities.QuantityCalculator;

import java.util.List;

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
	 * when called with the outputs of the required measurers, returns the
	 * quantity
	 */
	abstract public TQuantity getResult(Iterable<MeasurerOutputBase> outputs);

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
