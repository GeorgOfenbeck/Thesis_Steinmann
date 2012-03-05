package ch.ethz.ruediste.roofline.sharedEntities.kernels;

import static ch.ethz.ruediste.roofline.sharedEntities.Axes.matrixSizeAxis;
import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.Coordinate;

public class DaxpyKernel extends DaxpyKernelData {
	private boolean useMkl;

	@Override
	public void initialize(Coordinate coordinate) {
		super.initialize(coordinate);
		if (coordinate.contains(matrixSizeAxis)) {
			setVectorSize(coordinate.get(matrixSizeAxis));
		}
	}

	@Override
	public String getAdditionalLibraries() {
		if (useMkl)
			return "-L/opt/intel/mkl/lib/ia32 -lmkl_intel -lmkl_sequential -lmkl_core";
		return "-lblas";
	}

	public boolean isUseMkl() {
		return useMkl;
	}

	public void setUseMkl(boolean useMkl) {
		this.useMkl = useMkl;
	}
}
