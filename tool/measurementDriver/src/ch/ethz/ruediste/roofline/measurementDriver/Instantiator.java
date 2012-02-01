package ch.ethz.ruediste.roofline.measurementDriver;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import ch.ethz.ruediste.roofline.measurementDriver.baseClasses.INamed;

import com.google.inject.Binding;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Singleton;

/**
 * The instantiator allows to instantiate classes with dependency injection
 * 
 */
@Singleton
public class Instantiator {
	private Injector injector;

	/**
	 * return an instance of the specified class
	 */
	public <T> T getInstance(Class<T> type) {
		return injector.getInstance(type);
	}

	/**
	 * return an instance for the specified key
	 */
	public <T> T getInstance(Key<T> key) {
		return injector.getInstance(key);
	}

	/**
	 * return a list of all classes which are explicitely bound by the injector
	 */
	@SuppressWarnings("unchecked")
	public <T> List<Class<? extends T>> getBoundClasses(Class<T> baseClass) {
		Map<Key<?>, Binding<?>> bindings = injector.getBindings();
		ArrayList<Class<? extends T>> result = new ArrayList<Class<? extends T>>();

		for (Entry<Key<?>, Binding<?>> entry : bindings.entrySet()) {
			// check if the bound class derives from the base class
			if (baseClass.isAssignableFrom(entry.getKey().getTypeLiteral()
					.getRawType())) {
				result.add((Class<? extends T>) entry.getValue().getProvider()
						.get().getClass());
			}
		}
		return result;
	}

	public void setInjector(Injector injector) {
		this.injector = injector;
	}

	/**
	 * List all named classes driving from baseClass
	 */
	public <T extends INamed> void listNamed(Class<T> baseClass) {
		for (Class<? extends T> namedClass : getBoundClasses(baseClass)) {
			INamed named = getInstance(namedClass);
			System.out.printf("%s\n\t%s\n", named.getName(), named
					.getDescription().replace("\n", "\n\t"));
		}
	}
}
