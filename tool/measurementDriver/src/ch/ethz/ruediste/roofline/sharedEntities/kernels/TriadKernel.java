package ch.ethz.ruediste.roofline.sharedEntities.kernels;

import static ch.ethz.ruediste.roofline.sharedEntities.Axes.bufferSizeAxis;
import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.Coordinate;
import ch.ethz.ruediste.roofline.measurementDriver.dom.quantities.TransferredBytes;

public class TriadKernel extends TriadKernelData {

	@Override
	public void initialize(Coordinate coordinate) {
		super.initialize(coordinate);
		if (coordinate.contains(bufferSizeAxis))
			setBufferSize(coordinate.get(bufferSizeAxis));
	}

	@Override
	public TransferredBytes getExpectedTransferredBytes() {
		// 8: for double
		// 4: two buffers reading, one buffer writing, which involves read+write
		return new TransferredBytes(getBufferSize() * 8 * 4);
	}
}
