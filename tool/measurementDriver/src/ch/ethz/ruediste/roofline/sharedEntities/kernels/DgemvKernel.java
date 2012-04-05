package ch.ethz.ruediste.roofline.sharedEntities.kernels;

import static ch.ethz.ruediste.roofline.sharedEntities.Axes.matrixSizeAxis;
import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.Coordinate;
import ch.ethz.ruediste.roofline.sharedEntities.Operation;

public class DgemvKernel extends DgemvKernelData {
	@Override
	public void initialize(Coordinate coordinate) {
		super.initialize(coordinate);
		if (coordinate.contains(matrixSizeAxis)) {
			setMatrixSize(coordinate.get(matrixSizeAxis));
		}
	}

	@Override
	public String getLabel() {
		return "MVM" + getLabelSuffix();
	}

	@Override
	public Operation getSuggestedOperation() {
		if (isUseMkl())
			return Operation.DoublePrecisionFlop;
		else
			return Operation.CompInstr;
	}
}
