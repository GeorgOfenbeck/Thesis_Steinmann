package ch.ethz.ruediste.roofline.dom;

import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.ParameterSpace.Coordinate;

/** Kernel just loading a memory block into memory */
public class MemoryLoadKernelDescription extends
		MemoryLoadKernelDescriptionData {

	@Override
	public void initialize(Coordinate coordinate) {
		super.initialize(coordinate);
		setBufferSize(coordinate.get(MeasurementDescription.bufferSizeAxis));
	}
}