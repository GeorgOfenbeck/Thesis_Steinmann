package ch.ethz.ruediste.roofline.sharedEntities;


public class MeasurementHash extends HashBase {

	public MeasurementHash(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return "hash:" + value;
	}
}