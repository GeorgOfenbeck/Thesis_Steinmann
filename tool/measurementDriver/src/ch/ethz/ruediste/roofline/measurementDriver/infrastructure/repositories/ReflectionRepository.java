package ch.ethz.ruediste.roofline.measurementDriver.infrastructure.repositories;

import java.util.*;

import org.apache.commons.lang3.tuple.Pair;

import ch.ethz.ruediste.roofline.measurementDriver.*;
import ch.ethz.ruediste.roofline.measurementDriver.configuration.ConfigurationKeyBase;
import ch.ethz.ruediste.roofline.measurementDriver.util.ClassFinder;
import ch.ethz.ruediste.roofline.sharedEntities.MacroKey;

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
					ConfigurationKeyBase.class, "ch.ethz.ruediste.roofline");
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

	public Set<MacroKey> getMacroKeys() {
		TreeSet<MacroKey> result = new TreeSet<MacroKey>();

		List<Pair<Class<?>, MacroKey>> macroList = ClassFinder
				.getStaticFieldValues(MacroKey.class,
						"ch.ethz.ruediste.roofline");

		for (Pair<Class<?>, MacroKey> pair : macroList) {
			MacroKey macro = pair.getRight();
			if (result.contains(macro)) {
				throw new Error("Macro named " + macro.getMacroName()
						+ " defined multiple times");
			}
			result.add(macro);
		}

		return result;
	}
}
