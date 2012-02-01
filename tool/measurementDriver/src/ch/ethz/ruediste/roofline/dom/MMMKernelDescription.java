package ch.ethz.ruediste.roofline.dom;

import ch.ethz.ruediste.roofline.measurementDriver.MacroKey;
import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.ParameterSpace.Coordinate;

public class MMMKernelDescription extends MMMKernelDescriptionData {
	private static final MacroKey operationMacro = MacroKey
			.Create("RMT_MMM_BLOCK",
					"specifies the block size to be used for MMM", "8");

	public long getBlockSize() {
		return Long.parseLong(getMacroDefinition(operationMacro));
	}

	public void setBlockSize(long size) {
		setMacroDefinition(operationMacro, Long.toString(size));
	}

	@Override
	public void initialize(Coordinate coordinate) {
		super.initialize(coordinate);

		if (coordinate.contains(Axes.matrixSizeAxis))
			setMatrixSize(coordinate.get(Axes.matrixSizeAxis));
	}
}
