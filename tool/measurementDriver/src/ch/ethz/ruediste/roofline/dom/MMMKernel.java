package ch.ethz.ruediste.roofline.dom;

import ch.ethz.ruediste.roofline.measurementDriver.MacroKey;
import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.ParameterSpace.Coordinate;

public class MMMKernel extends MMMKernelData {
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
}
