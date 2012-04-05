package ch.ethz.ruediste.roofline.sharedEntities.kernels;

import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.*;
import ch.ethz.ruediste.roofline.sharedEntities.*;

public abstract class BlasKernelBase extends BlasKernelBaseData {
	public static final Axis<Boolean> useMklAxis = new Axis<Boolean>(
			"651cc552-2772-4d29-969a-dad85024e965", "useMKL");

	public static final Axis<Integer> numThreadsAxis = new Axis<Integer>(
			"a4a46f4b-6a0d-468a-98a2-bdf4edad2ff3", "numThreads");

	private static final MacroKey useMklMacro = MacroKey.Create(
			"RMT_BLAS_KERNEL_BASE_USEMKL",
			"if set to 1, the mkl library is used", "0");

	private static final MacroKey blasKernelBaseUsedMacro = MacroKey.Create(
			"RMT_BLAS_KERNEL_BASE_USED",
			"if set to 1, a kernel deriving from BlasKernelBase is present",
			"0");

	public BlasKernelBase() {
		setMacroDefinition(blasKernelBaseUsedMacro, "1");
	}

	@Override
	public void initialize(Coordinate coordinate) {
		super.initialize(coordinate);

		if (coordinate.contains(useMklAxis)) {
			setUseMkl(coordinate.get(useMklAxis));
		}

		if (coordinate.contains(numThreadsAxis)) {
			setNumThreads(coordinate.get(numThreadsAxis));
		}
	}

	public boolean isUseMkl() {
		return getMacroDefinition(useMklMacro).equals("1");
	}

	public void setUseMkl(boolean useMkl) {
		setMacroDefinition(useMklMacro, useMkl ? "1" : "0");
	}

	@Override
	public String getAdditionalLibraries(SystemInformation systemInformation) {
		if (isUseMkl())
			return LibraryHelper.getMklLibs(true, systemInformation);
		return "-lblas";
	}

	@Override
	public String getLabelSuffix() {
		String labelSuffix = "";
		if (isUseMkl())
			labelSuffix += "-Mkl";
		else
			labelSuffix += "-OpenBlas";

		if (getNumThreads() > 1)
			labelSuffix += "-Threaded";

		return labelSuffix + super.getLabelSuffix();
	}
}
