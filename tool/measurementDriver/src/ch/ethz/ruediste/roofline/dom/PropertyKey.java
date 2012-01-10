package ch.ethz.ruediste.roofline.dom;

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
