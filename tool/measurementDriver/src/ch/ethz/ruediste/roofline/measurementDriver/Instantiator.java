package ch.ethz.ruediste.roofline.measurementDriver;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.inject.Binding;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Singleton;

@Singleton
public class Instantiator {
	private Injector injector;

	public Instantiator() {
	}

	public <T> T getInstance(Class<T> type) {
		return injector.getInstance(type);
	}

	public <T> T getInstance(Key<T> key) {
		return injector.getInstance(key);
	}

	@SuppressWarnings("unchecked")
	public <T> List<Class<? extends T>> getBoundClasses(Class<T> baseClass) {
		Map<Key<?>, Binding<?>> bindings = injector.getBindings();
		ArrayList<Class<? extends T>> result = new ArrayList<Class<? extends T>>();

		for (Entry<Key<?>, Binding<?>> entry : bindings.entrySet()) {

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
}
