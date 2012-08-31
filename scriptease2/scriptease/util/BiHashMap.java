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
	private final Map<V, K> reverseMap;

	/**
	 * Creates a bidirectional hash map where keys and values are unique. This
	 * means both can be accessed by each other. Comparisons and storage of
	 * references is managed in <tt>HashMap</tt>s.
	 */
	public BiHashMap() {
		this.mainMap = new IdentityHashMap<K, V>();
		this.reverseMap = new IdentityHashMap<V, K>();
	}

	/**
	 * Returns the size of the bidirectional hash map.
	 * 
	 * @return The size of the bidirectional hash map
	 * @see HashMap#size()
	 */
	public int size() {
		this.checkBidirectionality();
		return this.mainMap.size();
	}

	/**
	 * Returns true if the bidirectional hash map is empty.
	 * 
	 * @return <tt>true</tt> if the bidirectional hash map is empty
	 * @see HashMap#isEmpty()
	 */
	public boolean isEmpty() {
		this.checkBidirectionality();
		return (this.mainMap.isEmpty());
	}

	/**
	 * Returns true if the bidirectional hash map contains the key.
	 * 
	 * @param key
	 *            The key to search for
	 * @return <tt>true</tt> if the bidirectional hash map contains the key
	 * @see HashMap#containsKey(Object)
	 */
	public boolean containsKey(Object key) {
		this.checkBidirectionality();
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
	public boolean containsValue(Object value) {
		this.checkBidirectionality();
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
			return (this.reverseMap.get(value));
		else
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
		this.removeValue(this.getValue(key));
		this.mainMap.put(key, value);
		this.reverseMap.put(value, key);
		this.checkBidirectionality();
	}

	/**
	 * Removes the passed in key from the BiHashMap.
	 * 
	 * @param key
	 *            The key to remove
	 */
	public void removeKey(final K key) {
		this.checkBidirectionality();
		for (Entry<V, K> entry : this.reverseMap.entrySet()) {
			if (entry.getValue() == key) {
				this.reverseMap.remove(entry.getKey());
				break;
			}
		}
		this.mainMap.remove(key);
	}

	/**
	 * Removes the passed in value from the BiHashMap.
	 * 
	 * @param value
	 *            The value to remove
	 */
	public void removeValue(final V value) {
		this.checkBidirectionality();
		for (Entry<K, V> entry : this.mainMap.entrySet()) {
			if (entry.getKey() == value) {
				this.mainMap.remove(entry.getValue());
			}
		}
		this.reverseMap.remove(value);
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
		this.reverseMap.clear();
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
		this.checkBidirectionality();

		final Collection<K> keyList;
		keyList = new ArrayList<K>();

		keyList.addAll(this.reverseMap.values());

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
		this.checkBidirectionality();

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
	 * BidirectionalityViolatedException.
	 * 
	 * @return
	 */
	public Set<java.util.Map.Entry<K, V>> getEntrySet() {
		this.checkBidirectionality();

		final Set<java.util.Map.Entry<K, V>> entrySet;
		entrySet = new HashSet<java.util.Map.Entry<K, V>>();

		entrySet.addAll(this.mainMap.entrySet());

		return entrySet;
	}

	/**
	 * Checks if the two hashmaps are truly bidirectional. This should be
	 * checked every time we do something with a HashBiMap. A violation of
	 * bidirectionality most likely indicates that something is wrong with the
	 * HashBiMap class itself.
	 * 
	 * @return
	 */
	private void checkBidirectionality() {
		if (this.mainMap.size() != this.reverseMap.size())
			throw new BidirectionalityViolatedException(
					"Sizes of maps in HashBiMap are not equal.");

		if (this.mainMap.isEmpty() != this.reverseMap.isEmpty())
			throw new BidirectionalityViolatedException(
					"One map in HashBiMap is empty while other is not.");

		for (Entry<K, V> mainMapEntry : this.mainMap.entrySet()) {
			boolean keyFound = false;
			for (Entry<V, K> reverseMapEntry : this.reverseMap.entrySet()) {
				if (mainMapEntry.getKey() == reverseMapEntry.getValue()) {
					if (mainMapEntry.getValue() != reverseMapEntry.getKey())
						throw new BidirectionalityViolatedException("Value "
								+ mainMapEntry.getValue()
								+ " is present in only one map.");
					keyFound = true;
				}
			}
			if (!keyFound)
				throw new BidirectionalityViolatedException("Key "
						+ mainMapEntry.getKey()
						+ " is present in only one map.");
		}
	}
}
