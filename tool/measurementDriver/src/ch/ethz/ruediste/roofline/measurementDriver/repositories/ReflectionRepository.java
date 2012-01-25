package ch.ethz.ruediste.roofline.measurementDriver.repositories;

import java.util.*;

import org.apache.commons.lang3.tuple.Pair;

import ch.ethz.ruediste.roofline.measurementDriver.*;

/**
 * A repository providing reflection information of the measurement driver.
 * Reflection does not stand for Java reflection in this context, but for
 * information about the measurement driver itself. Coincidentally, the easiest
 * way to get that information is by reflection ;-)
 */
public class ReflectionRepository {
	private List<Pair<Class<?>, ConfigurationKeyBase>> configurationKeyPairs;
	private Map<String, ConfigurationKeyBase> configurationKeyMap;

	public List<Pair<Class<?>, ConfigurationKeyBase>> getConfigurationKeyPairs() {
		if (configurationKeyPairs == null) {
			configurationKeyPairs = ClassFinder.getStaticFieldValues(
					ConfigurationKeyBase.class,
					"ch.ethz.ruediste.roofline.measurementDriver");
		}
		return configurationKeyPairs;
	}

	public Map<String, ConfigurationKeyBase> getConfigurationKeyMap() {
		if (configurationKeyMap == null) {
			configurationKeyMap = new HashMap<String, ConfigurationKeyBase>();
			for (Pair<Class<?>, ConfigurationKeyBase> pair : getConfigurationKeyPairs()) {
				configurationKeyMap.put(pair.getRight().getKey(),
						pair.getRight());
			}
		}
		return configurationKeyMap;
	}
}
