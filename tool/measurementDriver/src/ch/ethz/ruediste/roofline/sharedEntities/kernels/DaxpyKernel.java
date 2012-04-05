package ch.ethz.ruediste.roofline.sharedEntities.kernels;

import static ch.ethz.ruediste.roofline.sharedEntities.Axes.matrixSizeAxis;
import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.Coordinate;
import ch.ethz.ruediste.roofline.sharedEntities.Operation;

public class DaxpyKernel extends DaxpyKernelData {
	@Override
	public void initialize(Coordinate coordinate) {
		super.initialize(coordinate);
		if (coordinate.contains(matrixSizeAxis)) {
			setVectorSize(coordinate.get(matrixSizeAxis));
		}
	}

	@Override
	public String getLabel() {
		return "VVM" + getLabelSuffix();
	}

	@Override
	public Operation getSuggestedOperation() {
		return Operation.DoublePrecisionFlop;
	}
}
