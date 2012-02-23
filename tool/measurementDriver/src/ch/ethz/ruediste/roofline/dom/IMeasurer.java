package ch.ethz.ruediste.roofline.dom;

import java.util.UUID;

public interface IMeasurer<TOutput> {
	UUID getUid();

	void validate(TOutput output, MeasurementResult measurementResult);

}
