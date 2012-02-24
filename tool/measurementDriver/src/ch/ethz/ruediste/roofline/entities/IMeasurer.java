package ch.ethz.ruediste.roofline.entities;

import java.util.UUID;

public interface IMeasurer<TOutput> {
	UUID getUid();

	void validate(TOutput output, MeasurementResult measurementResult);

}
