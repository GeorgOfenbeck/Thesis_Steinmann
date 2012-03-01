package ch.ethz.ruediste.roofline.sharedEntities.kernels;

import static ch.ethz.ruediste.roofline.sharedEntities.Axes.bufferSizeAxis;
import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.ParameterSpace.Coordinate;

public class FFTKernelBase extends FFTKernelBaseData {

	@Override
	public void initialize(Coordinate coordinate) {
		super.initialize(coordinate);
		if (coordinate.contains(bufferSizeAxis)) {
			setBufferSize(coordinate.get(bufferSizeAxis));
		}
	}

}
