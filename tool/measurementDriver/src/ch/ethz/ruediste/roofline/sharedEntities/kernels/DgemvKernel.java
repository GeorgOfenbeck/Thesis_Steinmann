package ch.ethz.ruediste.roofline.sharedEntities.kernels;

import static ch.ethz.ruediste.roofline.sharedEntities.Axes.matrixSizeAxis;
import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.Coordinate;

public class DgemvKernel extends DgemvKernelData {

	private boolean useMkl;

	@Override
	public void initialize(Coordinate coordinate) {
		super.initialize(coordinate);
		if (coordinate.contains(matrixSizeAxis)) {
			setMatrixSize(coordinate.get(matrixSizeAxis));
		}
	}

	@Override
	public String getAdditionalLibraries() {
		if (useMkl)
			return LibraryHelper.getMklLibs(false);
		return "-lblas";
	}

	public boolean isUseMkl() {
		return useMkl;
	}

	public void setUseMkl(boolean useMkl) {
		this.useMkl = useMkl;
	}
}
