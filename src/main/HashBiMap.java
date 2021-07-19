package main;

import java.util.Collection;
import java.util.HashMap;

public class HashBiMap<K, V> {
	
	private HashMap<V, K> keyMap;
	private HashMap<K, V> valueMap;
	
	public HashBiMap() {
		keyMap = new HashMap<>();
		valueMap = new HashMap<>();
	}
	
	public void put(K key, V value) {
		valueMap.put(key, value);
		keyMap.put(value, key);
	}
	
	public void removeKey(V value) {
		valueMap.remove(keyMap.get(value));
		keyMap.remove(value);
	}
	public void removeValue(K key) {
		keyMap.remove(valueMap.get(key));
		valueMap.remove(key);
	}
	
	public V getValue(K key) {
		return valueMap.get(key);
	}
	public Collection<V> getValues() {
		return valueMap.values();
	}
	
	public K getKey(V value) {
		return keyMap.get(value);
	}
	public Collection<K> getKeys() {
		return keyMap.values();
	}
	
	public void clear() {
		keyMap.clear();
		valueMap.clear();
	}
}
