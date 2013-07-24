package scriptease.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * A class that creates a bidirectional hash map. Since it uses hash maps, core
 * functionality should be roughly the same as in <tt>HashMap</tt>.<br>
 * <br>
 * 
 * However, this type of map allows the user to search up keys by values and
 * values by keys. So unlike HashMap, you cannot have one value with multiple
 * keys. Both keys and values must be unique. Of course, the BiHashMap checks
 * for this automatically.
 * 
 * @author kschenk
 * 
 * @see HashMap
 * 
 * @param <K>
 *            Key
 * @param <V>
 *            Value
 */
public class BiHashMap<K, V> {
	private final Map<K, V> mainMap;

	/**
	 * Creates a bidirectional hash map where keys and values are unique. This
	 * means both can be accessed by each other. Comparisons and storage of
	 * references is managed in <tt>HashMap</tt>s.
	 */
	public BiHashMap() {
		this.mainMap = new IdentityHashMap<K, V>();
	}

	/**
	 * Returns the size of the bidirectional hash map.
	 * 
	 * @return The size of the bidirectional hash map
	 * @see HashMap#size()
	 */
	public int size() {
		return this.mainMap.size();
	}

	/**
	 * Returns true if the bidirectional hash map is empty.
	 * 
	 * @return <tt>true</tt> if the bidirectional hash map is empty
	 * @see HashMap#isEmpty()
	 */
	public boolean isEmpty() {
		return this.mainMap.isEmpty();
	}

	/**
	 * Returns true if the bidirectional hash map contains the key.
	 * 
	 * @param key
	 *            The key to search for
	 * @return <tt>true</tt> if the bidirectional hash map contains the key
	 * @see HashMap#containsKey(Object)
	 */
	public boolean containsKey(K key) {
		return this.mainMap.containsKey(key);
	}

	/**
	 * Returns true if the bidirectional hash map contains the value.
	 * 
	 * @param value
	 *            The key to search for
	 * @return <tt>true</tt> if the bidirectional hash map contains the value
	 * @see HashMap#containsValue(Object)
	 */
	public boolean containsValue(V value) {
		return this.mainMap.containsValue(value);
	}

	/**
	 * Returns the value mapped to the passed in key. Returns null if the map
	 * does not contain the key.
	 * 
	 * @param key
	 *            The key for which a value is returned.
	 * @return The value mapped to the key or null if key does not exist
	 */
	public V getValue(K key) {
		if (this.containsKey(key))
			return this.mainMap.get(key);
		else
			return null;
	}

	/**
	 * Returns the key mapped to the passed in value. Returns null if the map
	 * does not contain the value.
	 * 
	 * @param value
	 *            The value for which a key is returned.
	 * @return The key mapped to the value or null if value does not exist
	 */
	public K getKey(V value) {
		if (this.containsValue(value))
			for (Entry<K, V> entry : this.mainMap.entrySet()) {
				if (entry.getValue() == value) {
					return entry.getKey();
				}
			}

		return null;
	}

	/**
	 * Adds the key and value pair to the BiHashMap. If the key or value already
	 * exists, then the old entry is replaced.
	 * 
	 * @param key
	 * @param value
	 */
	public void put(K key, V value) {
		this.removeKey(key);
		this.removeValue(value);

		this.mainMap.put(key, value);
	}

	/**
	 * Removes the passed in key from the BiHashMap.
	 * 
	 * @param key
	 *            The key to remove
	 */
	public boolean removeKey(final K key) {
		return this.mainMap.remove(key) != null;
	}

	/**
	 * Removes the passed in value from the BiHashMap.
	 * 
	 * @param value
	 *            The value to remove
	 */
	public boolean removeValue(final V value) {
		K toRemove = null;

		for (Entry<K, V> entry : this.mainMap.entrySet()) {
			if (entry.getValue() == value) {
				toRemove = entry.getKey();
				break;
			}
		}

		return toRemove != null && this.removeKey(toRemove);
	}

	/**
	 * Empties the BiHashMap. The keys are torn from their values and thrown
	 * into a fiery vortex of doom. As the values cry out for their lost
	 * companions, the ground falls out from underneath them, revealing a lake
	 * full of dark and evil things. Nothing remains inside the BiHashMap.
	 * Nothing.
	 * 
	 * @see HashMap#clear()
	 */
	public void clear() {
		this.mainMap.clear();
	}

	/**
	 * Returns a Collection of keys of the BiHashMap. This is a cloned
	 * collection so that modifications to it will not affect the maps. If we
	 * returned pointers like a regular map, then modifications to the list
	 * would only apply to the reverse map list, eventually leading to a
	 * BidirectionalityViolatedException.
	 * 
	 * @return
	 */
	public Collection<K> getKeys() {
		final Collection<K> keyList;
		keyList = new ArrayList<K>();

		keyList.addAll(this.mainMap.keySet());

		return keyList;
	}

	/**
	 * Returns a Collection of values of the BiHashMap. This is a cloned
	 * collection so that modifications to it will not affect the map. If we
	 * returned pointers like a regular map, then modifications to the list
	 * would only apply to the reverse map list, eventually leading to a
	 * BidirectionalityViolatedException.
	 * 
	 * @return
	 */
	public Collection<V> getValues() {
		final Collection<V> valueList;
		valueList = new ArrayList<V>();

		valueList.addAll(this.mainMap.values());

		return valueList;
	}

	/**
	 * Returns a Set of entries of the BiHashMap. This is a cloned set so that
	 * modifications to it will not affect the map. If we returned pointers like
	 * a regular map, then modifications to the list would only apply to the
	 * reverse map list, eventually leading to a
	 * BidirectionalityViolatedException. Since it's a cloned map, using
	 * "SetValue" on any Entry will not do anything and just return null.
	 * 
	 * @return
	 */
	public Set<Entry<K, V>> getEntrySet() {
		final Set<Entry<K, V>> entrySet;

		entrySet = new HashSet<Entry<K, V>>();

		/*
		 * I know what you're thinking. Why didn't Kevin just write
		 * "entrySet.addAll(this.mainMap.entrySet();"? Well, it doesn't work.
		 * I'm not sure if it's a bug with entries, or if it's some unintended
		 * behaviour.
		 * 
		 * Say the mainMap.entrySet() had three entries, <A, 1>, <B, 2>, and <C,
		 * 3>. AddAll iterates over all of these entries and adds each one.
		 * 
		 * The first iteration would return a Set with <A, 1>. But the second
		 * set returns a Set containing <B, 2> and <B, 2>! The final result will
		 * be a set with 3 <C, 3>s. I can't find anything on Google about this,
		 * but our workaround works.
		 */
		for (Entry<K, V> entry : this.mainMap.entrySet()) {
			final K key;
			final V value;

			key = entry.getKey();
			value = entry.getValue();

			entrySet.add(new Entry<K, V>() {
				@Override
				public K getKey() {
					return key;
				}

				@Override
				public V getValue() {
					return value;
				}

				@Deprecated
				@Override
				public V setValue(V value) {
					// Does nothing because it would be pointless.
					return null;
				}
			});
		}

		return entrySet;
	}
}
