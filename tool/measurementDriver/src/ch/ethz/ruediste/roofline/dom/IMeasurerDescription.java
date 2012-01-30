package ch.ethz.ruediste.roofline.dom;


public interface IMeasurerDescription<TOutput> {

	void validate(TOutput output,
			MeasurementResult measurementResult);

}
