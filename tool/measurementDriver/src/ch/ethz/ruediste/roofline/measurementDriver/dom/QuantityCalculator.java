package ch.ethz.ruediste.roofline.measurementDriver.dom;

import java.util.List;

import ch.ethz.ruediste.roofline.dom.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.quantities.Quantity;

public abstract class QuantityCalculator<TQuantity extends Quantity<TQuantity>> {

	abstract public TQuantity getResult(Iterable<MeasurerSetOutput> outputs);

	public abstract List<MeasurerSet> getRequiredMeasurerSets();
}
