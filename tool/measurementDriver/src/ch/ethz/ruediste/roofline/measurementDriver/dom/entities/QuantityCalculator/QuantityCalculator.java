package ch.ethz.ruediste.roofline.measurementDriver.dom.entities.QuantityCalculator;

import java.util.List;

import ch.ethz.ruediste.roofline.measurementDriver.dom.quantities.Quantity;
import ch.ethz.ruediste.roofline.measurementDriver.util.IUnaryPredicate;
import ch.ethz.ruediste.roofline.sharedEntities.*;

public abstract class QuantityCalculator<TQuantity extends Quantity<TQuantity>> {

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

}
