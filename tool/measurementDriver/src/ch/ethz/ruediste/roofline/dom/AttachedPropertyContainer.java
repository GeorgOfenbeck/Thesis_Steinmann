package ch.ethz.ruediste.roofline.dom;

import java.util.HashMap;
import java.util.Map;

import com.thoughtworks.xstream.annotations.XStreamOmitField;

public class AttachedPropertyContainer {
	@XStreamOmitField
	private Map<PropertyKey<?>, Object> propertyMap = new HashMap<PropertyKey<?>, Object>();

	@SuppressWarnings("unchecked")
	public <T> T getValue(PropertyKey<T> key) {
		if (propertyMap.containsKey(key)) {
			return (T) propertyMap.get(key);
		}
		return key.getDefaultValue();
	}

	public <T> void setValue(PropertyKey<T> key, T value) {
		propertyMap.put(key, value);
	}
}
