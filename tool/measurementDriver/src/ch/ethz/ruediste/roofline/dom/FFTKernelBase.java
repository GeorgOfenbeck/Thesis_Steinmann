package ch.ethz.ruediste.roofline.dom;

import static ch.ethz.ruediste.roofline.dom.Axes.bufferSizeAxis;
import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.ParameterSpace.Coordinate;

public class FFTKernelBase extends FFTKernelBaseData {

	public void initialize(Coordinate coordinate) {
		if (coordinate.contains(bufferSizeAxis)) {
			setBufferSize(coordinate.get(bufferSizeAxis));
		}
	}

}
