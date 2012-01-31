package ch.ethz.ruediste.roofline.measurementDriver;

import java.util.*;

import org.apache.log4j.Logger;

import com.google.inject.Singleton;

@Singleton
public class Configuration {
	private static final Logger log = Logger.getLogger(Configuration.class);

	final private HashMap<ConfigurationKeyBase, Stack<Object>> data = new HashMap<ConfigurationKeyBase, Stack<Object>>();

	private Configuration defaultConfiguration;

	public Object getUntyped(ConfigurationKeyBase key) {
		Stack<Object> stack = data.get(key);

		if (stack != null && !stack.isEmpty()) {
			return stack.peek();
		}

		if (defaultConfiguration != null) {
			return defaultConfiguration.getUntyped(key);
		}

		return key.getDefaultValue();
	}

	@SuppressWarnings("unchecked")
	public <T> T get(ConfigurationKey<T> key) {
		return (T) getUntyped((ConfigurationKeyBase) key);
	}

	public <T> Iterable<T> get(ConfigurationKey<T>... keys) {
		ArrayList<T> result = new ArrayList<T>();
		for (ConfigurationKey<T> key : keys) {
			result.add(get(key));
		}
		return result;
	}

	public void setDefaultConfiguration(Configuration configuration) {
		this.defaultConfiguration = configuration;
	}

	public Configuration getDefaultConfiguration() {
		return defaultConfiguration;
	}

	public void parseAndSet(ConfigurationKeyBase key, String value) {
		setUntyped(key, parse(key.getValueType(), value));
	}

	public void setUntyped(ConfigurationKeyBase key, Object value) {
		log.debug("setting " + key.getKey() + " to " + value);
		if (value != null
				&& !key.getValueType().isAssignableFrom(value.getClass())) {
			throw new Error("wrong data type");
		}

		Stack<Object> stack = data.get(key);

		// make sure there is a cleared stack
		if (stack == null) {
			stack = new Stack<Object>();
			data.put(key, stack);
		}
		else {
			stack.clear();
		}

		stack.push(value);
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

	public <T> void push(ConfigurationKey<T> key, T value) {
		log.debug("pushing " + key.getKey() + " = " + value);
		Stack<Object> stack = data.get(key);

		// make sure there is a stack
		if (stack == null) {
			stack = new Stack<Object>();
			data.put(key, stack);
		}

		stack.push(value);
	}

	public void pop(ConfigurationKeyBase key) {
		log.debug("popping " + key.getKey());
		data.get(key).pop();
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
