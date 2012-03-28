package ch.ethz.ruediste.roofline.sharedEntities.kernels;

import static ch.ethz.ruediste.roofline.sharedEntities.Axes.bufferSizeAxis;
import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.Coordinate;
import ch.ethz.ruediste.roofline.measurementDriver.dom.quantities.TransferredBytes;
import ch.ethz.ruediste.roofline.sharedEntities.SystemInformation;

public class TriadKernel extends TriadKernelData {

	@Override
	public void initialize(Coordinate coordinate) {
		super.initialize(coordinate);
		if (coordinate.contains(bufferSizeAxis))
			setBufferSize(coordinate.get(bufferSizeAxis));
	}

	@Override
	public TransferredBytes getExpectedTransferredBytes(
			SystemInformation systemInformation) {
		// 8: for double

		long bufferSizeBytes = getBufferSize() * 8;

		// divide by three, since we have three buffers
		double cacheSize = systemInformation.L2CacheSize / 3.;

		// 4: two buffers reading, one buffer writing, which involves read+write
		return new TransferredBytes(bufferSizeBytes * 2 // two buffers reading
				+ bufferSizeBytes // one buffer writing, which requires reading it first
				+ Math.max(0, bufferSizeBytes - cacheSize) // take write-back into account
		);
	}

	@Override
	public String getLabel() {
		return "Triad";
	}
}
