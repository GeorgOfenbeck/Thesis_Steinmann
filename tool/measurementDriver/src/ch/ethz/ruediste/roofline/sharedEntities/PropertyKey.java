package ch.ethz.ruediste.roofline.sharedEntities;

public class PropertyKey<T> {

	private T defaultValue;

	public PropertyKey(T defaultValue) {
		super();
		this.defaultValue = defaultValue;
	}

	public T getDefaultValue() {
		return defaultValue;
	}
}
