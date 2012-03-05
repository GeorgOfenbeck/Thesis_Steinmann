package ch.ethz.ruediste.roofline.measurementDriver.configuration;

import java.util.*;
import java.util.Map.Entry;

import com.google.inject.Singleton;

/**
 * Maps configuration keys to values. Can be nested using parent configurations
 */
@Singleton
public class Configuration {
	final private Object unset = new Object();
	final private Stack<HashMap<ConfigurationKeyBase, Object>> oldValuesStack = new Stack<HashMap<ConfigurationKeyBase, Object>>();
	final private HashMap<ConfigurationKeyBase, Object> data = new HashMap<ConfigurationKeyBase, Object>();

	private Configuration parentConfiguration;

	public Object getUntyped(ConfigurationKeyBase key) {
		if (data.containsKey(key)) {
			return data.get(key);
		}

		if (parentConfiguration != null) {
			return parentConfiguration.getUntyped(key);
		}

		return key.getDefaultValue();
	}

	@SuppressWarnings("unchecked")
	public <T> T get(ConfigurationKey<T> key) {
		return (T) getUntyped((ConfigurationKeyBase) key);
	}

	/**
	 * Set the parent configuration. If a key is not found within this
	 * configuration and a parent configuration is present, the parent
	 * configuration is searched for the key (recursively)
	 */
	public void setParentConfiguration(Configuration configuration) {
		this.parentConfiguration = configuration;
	}

	public Configuration getParentConfiguration() {
		return parentConfiguration;
	}

	public void parseAndSet(ConfigurationKeyBase key, String value) {
		setUntyped(key, parse(key.getValueType(), value));
	}

	public void setUntyped(ConfigurationKeyBase key, Object value) {
		// check the data type of the provided value
		if (value != null
				&& !key.getValueType().isAssignableFrom(value.getClass())) {
			throw new Error("wrong data type");
		}

		// is there an old value map?
		if (!oldValuesStack.isEmpty()
		// haven't we put an old value in the stack before?
				&& !oldValuesStack.peek().containsKey(key)) {
			// we have to keep the current value in the old value stack
			if (data.containsKey(key))
				oldValuesStack.peek().put(key, data.get(key));
			else
				// remember that there was no value
				oldValuesStack.peek().put(key, unset);
		}

		data.put(key, value);
	}

	/**
	 * clear the stack for key and push value as the single element
	 */
	public <T> void set(ConfigurationKey<T> key, T value) {
		setUntyped(key, value);
	}

	public Set<ConfigurationKeyBase> getKeySet() {
		return data.keySet();
	}

	public void push() {
		oldValuesStack.push(new HashMap<ConfigurationKeyBase, Object>());
	}

	public void pop() {
		// check if there are old values to pop
		if (oldValuesStack.isEmpty())
			throw new Error("Cannot pop configuration, stack is empty");

		// pop the old values
		HashMap<ConfigurationKeyBase, Object> oldValues = oldValuesStack.pop();

		// restore the old values
		for (Entry<ConfigurationKeyBase, Object> pair : oldValues.entrySet()) {
			// was the value unset?
			if (pair.getValue() == unset) {
				// if the old value was unset, remove the current mapping from the data
				data.remove(pair.getKey());
			}
			else {
				// restore the old value
				data.put(pair.getKey(), pair.getValue());
			}
		}
	}

	@SuppressWarnings("unchecked")
	public <T> T parse(Class<T> clazz, String value) {
		if (Double.class == clazz) {
			return (T) (Double) Double.parseDouble(value);
		}

		if (Integer.class == clazz) {
			return (T) (Integer) Integer.parseInt(value);
		}

		if (Long.class == clazz) {
			return (T) (Long) Long.parseLong(value);
		}

		if (String.class == clazz) {
			return (T) value;
		}

		if (Boolean.class == clazz) {
			return (T) (Boolean) Boolean.parseBoolean(value);
		}

		throw new Error("Unsupported type " + clazz.getSimpleName());
	}

}
