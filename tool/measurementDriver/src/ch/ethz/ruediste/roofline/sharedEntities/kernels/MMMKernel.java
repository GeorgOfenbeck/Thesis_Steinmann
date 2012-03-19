package ch.ethz.ruediste.roofline.sharedEntities.kernels;

import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.Coordinate;
import ch.ethz.ruediste.roofline.sharedEntities.*;

public class MMMKernel extends MMMKernelData {
	private static final MacroKey algorithmMacro = MacroKey.Create(
			"RMT_MMM_Algorithm", "specifies the algorithm to be used",
			"MMMAlgorithm_TripleLoop");

	public enum MMMAlgorithm {
		MMMAlgorithm_TripleLoop, MMMAlgorithm_Blocked, MMMAlgorithm_Blocked_Restrict, MMMAlgorithm_Blas_Openblas, MMMAlgorithm_Blas_Mkl
	}

	@Override
	public String getAdditionalLibraries() {
		if (getAlgorithm() == MMMAlgorithm.MMMAlgorithm_Blas_Mkl) {
			return LibraryHelper.getMklLibs(getMultiThreaded());
		}

		if (getAlgorithm() == MMMAlgorithm.MMMAlgorithm_Blas_Openblas) {
			return "-lblas";
		}

		return super.getAdditionalLibraries();
	}

	public void setAlgorithm(MMMAlgorithm algorithm) {
		setMacroDefinition(algorithmMacro, algorithm.toString());
	}

	public MMMAlgorithm getAlgorithm() {
		return MMMAlgorithm.valueOf(getMacroDefinition(algorithmMacro));
	}

	private static final MacroKey nbMacro = MacroKey.Create("RMT_MMM_Nb",
			"specifies Nb", "8");

	public long getNb() {
		return Long.parseLong(getMacroDefinition(nbMacro));
	}

	public void setNb(long size) {
		setMacroDefinition(nbMacro, Long.toString(size));
	}

	private static final MacroKey muMacro = MacroKey.Create("RMT_MMM_Mu",
			"specifies Mu", "2");

	public long getMu() {
		return Long.parseLong(getMacroDefinition(muMacro));
	}

	public void setMu(long size) {
		setMacroDefinition(muMacro, Long.toString(size));
	}

	private static final MacroKey nuMacro = MacroKey.Create("RMT_MMM_Nu",
			"specifies Nu", "2");

	public long getNu() {
		return Long.parseLong(getMacroDefinition(nuMacro));
	}

	public void setNu(long size) {
		setMacroDefinition(nuMacro, Long.toString(size));
	}

	private static final MacroKey kuMacro = MacroKey.Create("RMT_MMM_Ku",
			"specifies Ku", "2");

	public long getKu() {
		return Long.parseLong(getMacroDefinition(kuMacro));
	}

	public void setKu(long size) {
		setMacroDefinition(kuMacro, Long.toString(size));
	}

	@Override
	public void initialize(Coordinate coordinate) {
		super.initialize(coordinate);

		if (coordinate.contains(Axes.matrixSizeAxis))
			setMatrixSize(coordinate.get(Axes.matrixSizeAxis));
		if (coordinate.contains(Axes.blockSizeAxis))
			setNb(coordinate.get(Axes.blockSizeAxis));
	}

	@Override
	public Operation getSuggestedOperation() {
		switch (getAlgorithm()) {
		case MMMAlgorithm_Blas_Mkl:
			return Operation.DoublePrecisionFlop;
		case MMMAlgorithm_Blas_Openblas:
		case MMMAlgorithm_Blocked:
		case MMMAlgorithm_Blocked_Restrict:
		case MMMAlgorithm_TripleLoop:
			return Operation.CompInstr;

		}
		throw new Error("should not happen");
	}
}
