package ch.ethz.ruediste.roofline.dom;

import static ch.ethz.ruediste.roofline.dom.Axes.bufferSizeAxis;
import ch.ethz.ruediste.roofline.measurementDriver.MacroKey;
import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.ParameterSpace.Coordinate;

public class FFTKernel extends FFTKernelData {
	private static final MacroKey algorithmMacro = MacroKey.Create(
			"RMT_FFT_Algorithm", "specifies the algorithm to be used",
			"FFTAlgorithm_NR");

	public enum FFTAlgorithm {
		FFTAlgorithm_NR, FFTAlgorithm_MKL
	}

	public void setAlgorithm(FFTAlgorithm algorithm) {
		setMacroDefinition(algorithmMacro, algorithm.toString());
	}

	public FFTAlgorithm getAlgorithm() {
		return FFTAlgorithm.valueOf(getMacroDefinition(algorithmMacro));
	}

	public void initialize(Coordinate coordinate) {
		if (coordinate.contains(bufferSizeAxis)) {
			setBufferSize(coordinate.get(bufferSizeAxis));
		}
	}

}
