package ch.ethz.ruediste.roofline.dom;

public interface IMeasurer<TOutput> {
	int getId();

	void validate(TOutput output, MeasurementResult measurementResult);

}
