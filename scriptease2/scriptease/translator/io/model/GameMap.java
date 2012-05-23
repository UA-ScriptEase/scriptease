package scriptease.translator.io.model;

import java.util.HashMap;
import java.util.Map;

/**
 * GameMap represents a generic String to String mapping that is used in the
 * LanguageDictionary to allow greater flexibility.
 * 
 * @author mfchurch
 * 
 */
public class GameMap {
	private String id;
	private Map<String, String> entryMap;
	private String defaultValue;

	public GameMap(String id, String defaultValue, Map<String, String> entryMap) {
		this.id = id;
		this.defaultValue = defaultValue;
		this.entryMap = new HashMap<String, String>(entryMap);
	}

	public String getID() {
		return this.id;
	}

	public boolean hasEntry(String key) {
		return this.entryMap.containsKey(key);
	}

	public String getDefault() {
		return this.defaultValue;
	}

	/**
	 * Get's the entry with the given key from the map. If it doesn't exist, the
	 * default map value is returned.
	 * 
	 * @param key
	 * @return
	 */
	public String resolveEntry(String key) {
		final String value;
		String entry = this.entryMap.get(key);
		if (entry != null)
			value = entry;
		else
			value = this.defaultValue;
		return value;
	}

	/**
	 * Returns a copy of the entry map
	 * 
	 * @return
	 */
	public Map<String, String> getEntryMap() {
		return new HashMap<String, String>(this.entryMap);
	}
}
