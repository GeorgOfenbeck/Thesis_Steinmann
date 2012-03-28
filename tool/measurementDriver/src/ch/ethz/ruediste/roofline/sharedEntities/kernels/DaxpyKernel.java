package ch.ethz.ruediste.roofline.sharedEntities.kernels;

import static ch.ethz.ruediste.roofline.sharedEntities.Axes.matrixSizeAxis;
import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.Coordinate;
import ch.ethz.ruediste.roofline.sharedEntities.SystemInformation;

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
	public String getAdditionalLibraries(SystemInformation systemInformation) {
		if (useMkl)
			return LibraryHelper.getMklLibs(false, systemInformation);
		return "-lblas";
	}

	public boolean isUseMkl() {
		return useMkl;
	}

	public void setUseMkl(boolean useMkl) {
		this.useMkl = useMkl;
	}

	@Override
	public String getLabel() {
		if (useMkl) {
			return "VVM Mkl";
		}
		return "VVM OpenBlas";
	}
}
