package ch.ethz.ruediste.roofline.sharedEntities.kernels;

import ch.ethz.ruediste.roofline.sharedEntities.SystemInformation;

public class SfoKernel extends SfoKernelData {
	@Override
	public String getAdditionalLibraries(SystemInformation systemInformation) {
		return super.getAdditionalLibraries(systemInformation)
				+ "~/svn/azuagarg_thesis/build/libbenchmark.a "
				+ "~/svn/azuagarg_thesis/build/libsatoru_func_fw.a "
				+ "~/svn/azuagarg_thesis/build/libsubmodularity.a "
				+ "~/svn/azuagarg_thesis/build/libqr_updates.a "
				+ "-L/opt/intel/mkl/lib/ia32 -lrt -lmkl_rt /opt/intel/lib/ia32/libiomp5.so /opt/intel/lib/ia32/libimf.so -lgfortran -lquadmath -Wl,-rpath,/opt/intel/mkl/lib/ia32:/opt/intel/lib/ia32";

	}
}
