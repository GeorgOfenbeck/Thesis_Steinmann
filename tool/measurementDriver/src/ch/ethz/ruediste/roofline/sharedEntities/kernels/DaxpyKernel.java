package ch.ethz.ruediste.roofline.sharedEntities.kernels;

import static ch.ethz.ruediste.roofline.sharedEntities.Axes.matrixSizeAxis;
import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.*;
import ch.ethz.ruediste.roofline.sharedEntities.*;

public class DaxpyKernel extends DaxpyKernelData {
	public static final Axis<Boolean> useMklAxis = new Axis<Boolean>(
			"651cc552-2772-4d29-969a-dad85024e965", "useMKL");

	public static final Axis<Integer> numThreadsAxis = new Axis<Integer>(
			"a4a46f4b-6a0d-468a-98a2-bdf4edad2ff3", "numThreads");

	private static final MacroKey useMklMacro = MacroKey.Create(
			"RMT_DAXPY_USEMKL",
			"if set to 1, the mkl library is used", "0");

	@Override
	public void initialize(Coordinate coordinate) {
		super.initialize(coordinate);
		if (coordinate.contains(matrixSizeAxis)) {
			setVectorSize(coordinate.get(matrixSizeAxis));
		}

		if (coordinate.contains(useMklAxis)) {
			setUseMkl(coordinate.get(useMklAxis));
		}

		if (coordinate.contains(numThreadsAxis)) {
			setNumThreads(coordinate.get(numThreadsAxis));
		}
	}

	@Override
	public String getAdditionalLibraries(SystemInformation systemInformation) {
		if (isUseMkl())
			return LibraryHelper.getMklLibs(false, systemInformation);
		return "-lblas";
	}

	public boolean isUseMkl() {
		return getMacroDefinition(useMklMacro).equals("1");
	}

	public void setUseMkl(boolean useMkl) {
		setMacroDefinition(useMklMacro, useMkl ? "1" : "0");
	}

	@Override
	public String getLabel() {
		String label = "VVM";
		if (isUseMkl())
			label += " Mkl";
		else
			label += " OpenBlas";

		if (getNumThreads() > 1)
			label += " Threaded";

		return label;
	}
}
