package ch.ethz.ruediste.roofline.measurementDriver.dom;

import java.util.ArrayList;

import ch.ethz.ruediste.roofline.dom.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.quantities.Quantity;
import ch.ethz.ruediste.roofline.measurementDriver.util.*;

public abstract class TerminalQuantityCalculator<T extends Quantity<T>> extends
		QuantityCalculator<T> {

	protected final ArrayList<MeasurerSet> requiredMeasurerSets = new ArrayList<MeasurerSet>();
	protected final ArrayList<MeasurerSetOutput> outputs = new ArrayList<MeasurerSetOutput>();

	public void addRequiredMeasurerSet(MeasurerSet measurerSet) {
		requiredMeasurerSets.add(measurerSet);
	}

	public void addOutput(MeasurerSetOutput measurerSetOutput) {
		outputs.add(measurerSetOutput);
	}

	public MeasurerSetOutput getOutput(final MeasurerSet set) {
		return IterableUtils.single(outputs,
				new IUnaryPredicate<MeasurerSetOutput>() {

					public Boolean apply(MeasurerSetOutput output) {
						return output.getSetId() == set.getId();
					}
				});
	}

	@Override
	public ArrayList<MeasurerSet> getRequiredMeasurerSets() {
		return requiredMeasurerSets;
	}
}
