package ch.ethz.ruediste.roofline.sharedEntities.kernels;

import ch.ethz.ruediste.roofline.sharedEntities.SystemInformation;

public class LibraryHelper {
	public static String getMklLibs() {
		String libraryPath;
		String mklIntel;
		if (SystemInformation.Is64Bit) {
			libraryPath = "-L/opt/intel/mkl/lib/intel64";
			mklIntel = "-lmkl_intel_lp64";
		}

		else {
			libraryPath = "-L/opt/intel/mkl/lib/ia32";
			mklIntel = "-lmkl_intel";
		}

		return String.format("%s %s -lmkl_sequential -lmkl_core", libraryPath,
				mklIntel);
	}
}
