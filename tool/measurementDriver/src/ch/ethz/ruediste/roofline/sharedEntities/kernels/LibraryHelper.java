package ch.ethz.ruediste.roofline.sharedEntities.kernels;

import ch.ethz.ruediste.roofline.sharedEntities.SystemInformation;


public class LibraryHelper {
	public static String getMklLibs(boolean threaded, SystemInformation systemInformation) {
		String libraryPath;
		String mklIntel;
		if (systemInformation.Is64Bit) {
			libraryPath = "-L/opt/intel/mkl/lib/intel64";
			mklIntel = "-lmkl_intel_lp64";
		}

		else {
			libraryPath = "-L/opt/intel/mkl/lib/ia32";
			mklIntel = "-lmkl_intel";
		}

		String mkl;
		if (threaded) {
			mkl = "-lmkl_gnu_thread";
		}
		else {
			mkl = "-lmkl_sequential";
		}

		String additional = "";
		if (threaded) {
			additional = "-fopenmp -lpthread";
		}

		return String.format("%s %s %s -lmkl_core %s", libraryPath,
				mklIntel, mkl, additional);
	}

}
