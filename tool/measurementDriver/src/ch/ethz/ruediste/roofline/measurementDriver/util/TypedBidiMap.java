package ch.ethz.ruediste.roofline.measurementDriver.util;

import java.util.*;

import org.apache.commons.collections.BidiMap;
import org.apache.commons.collections.bidimap.TreeBidiMap;

@SuppressWarnings("unchecked")
public class TypedBidiMap<K, V> implements Map<K, V> {
	private final BidiMap map;

	public TypedBidiMap(BidiMap map) {
		super();
		this.map = map;
	}

	public TypedBidiMap() {
		this(new TreeBidiMap());
	}

	public int size() {
		return map.size();
	}

	public boolean isEmpty() {
		return map.isEmpty();
	}

	public boolean containsKey(Object key) {
		return map.containsKey(key);
	}

	public boolean containsValue(Object value) {
		return map.containsValue(value);
	}

	public V get(Object key) {
		return (V) map.get(key);
	}

	public V put(K key, V value) {
		return (V) map.put(key, value);
	}

	public V remove(Object key) {
		return (V) map.remove(key);
	}

	public void putAll(Map<? extends K, ? extends V> m) {
		map.putAll(m);

	}

	public void clear() {
		map.clear();
	}

	public Set<K> keySet() {
		return map.keySet();
	}

	public Collection<V> values() {
		return map.values();
	}

	public Set<java.util.Map.Entry<K, V>> entrySet() {
		return entrySet();
	}

	public K getKey(V object) {
		return (K) map.getKey(object);
	}

}
