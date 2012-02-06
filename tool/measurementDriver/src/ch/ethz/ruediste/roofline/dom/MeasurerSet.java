package ch.ethz.ruediste.roofline.dom;

public class MeasurerSet<T extends MeasurerDescriptionBase> extends
		MeasurerSetBase {

	public MeasurerSet() {
	}

	public MeasurerSet(T measurer) {
		this();
		setMainMeasurerUntyped(measurer);
	}

	@SuppressWarnings("unchecked")
	public T getMainMeasurer() {
		return (T) getMainMeasurerUntyped();
	}

	public void setMainMeasurer(T measurer) {
		setMainMeasurerUntyped(measurer);
	}
}
