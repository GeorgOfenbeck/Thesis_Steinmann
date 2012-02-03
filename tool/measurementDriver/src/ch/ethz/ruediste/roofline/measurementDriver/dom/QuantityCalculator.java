package ch.ethz.ruediste.roofline.measurementDriver.dom;

import java.util.ArrayList;

import ch.ethz.ruediste.roofline.dom.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.quantities.Quantity;

public abstract class QuantityCalculator<TQuantity extends Quantity<TQuantity>> {
	abstract public TQuantity getResult();

	public abstract ArrayList<MeasurerSet> getRequiredMeasurerSets();

	public abstract void addOutput(MeasurerSetOutput measurerSetOutput);
}
