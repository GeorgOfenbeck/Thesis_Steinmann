package ch.ethz.ruediste.roofline.measurementDriver.dom;

import java.util.List;

import ch.ethz.ruediste.roofline.measurementDriver.dom.quantities.Quantity;
import ch.ethz.ruediste.roofline.sharedEntities.*;

public abstract class QuantityCalculator<TQuantity extends Quantity<TQuantity>> {

	abstract public TQuantity getResult(Iterable<MeasurerOutputBase> outputs);

	public abstract List<MeasurerBase> getRequiredMeasurers();
}
