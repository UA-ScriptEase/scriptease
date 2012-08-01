package scriptease.translator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import scriptease.translator.codegenerator.code.fragments.Fragment;
import scriptease.translator.codegenerator.code.fragments.FormatDefinitionFragment;
import scriptease.translator.io.model.GameMap;

/**
 * Manages the description of the language grammar. 
 * 
 * @author remiller
 * @author jtduncan
 * @author mfchurch
 */
public class LanguageDictionary {

	private final Map<String, FormatDefinitionFragment> formatMap;
	private final Collection<String> reservedWords;
	private final Map<String, GameMap> maps;
	private String indentString;
	private String name;

	/**
	 * Builds a new LanguageDictionary to represent the given data.
	 * 
	 * @param name
	 *            Name of the dictionary.
	 * @param indentString
	 *            String to be used for indenting code.
	 * @param reservedWords
	 *            List of strings to be avoided when generating names in code
	 *            generation.
	 * @param formatMap
	 * @param maps
	 */
	public LanguageDictionary(String name, String indentString,
			Collection<String> reservedWords,
			Map<String, FormatDefinitionFragment> formatMap, Map<String, GameMap> maps) {
		this.name = name;
		this.indentString = indentString;
		this.reservedWords = new HashSet<String>(reservedWords);
		this.formatMap = new HashMap<String, FormatDefinitionFragment>(formatMap);
		this.maps = new HashMap<String, GameMap>(maps);
	}

	public String getName() {
		return this.name;
	}

	public String getIndent() {
		return this.indentString;
	}

	public Collection<String> getReservedWords() {
		return this.reservedWords;
	}

	public boolean isReservedWord(String word) {
		return this.reservedWords.contains(word);
	}

	/**
	 * Get's the GameMap with the given name,
	 * 
	 * Returns null if the GameMap is not found.
	 * 
	 * @param name
	 * @param keyword
	 * @return
	 */
	public GameMap getGameMap(String name) {
		return this.maps.get(name);
	}

	public Map<String, GameMap> getMaps() {
		return new HashMap<String, GameMap>(this.maps);
	}

	/**
	 * Get the Collection<FormatFragment> representing the given Format
	 * 
	 * @param formatID
	 * @return
	 */
	public List<Fragment> getFormat(String formatID) {
		final List<Fragment> format;
		final FormatDefinitionFragment formatIDFragment;
		format = new ArrayList<Fragment>();
		formatIDFragment = this.formatMap.get(formatID);

		if (formatIDFragment != null) {
			format.addAll(formatIDFragment.getSubFragments());
		} else
			throw new IllegalArgumentException("Unable to resolve formatID "
					+ formatID);

		return format;
	}

	public Collection<FormatDefinitionFragment> getFormats() {
		return this.formatMap.values();
	}

	@Override
	public String toString() {
		return "LanguageDictionary [" + this.getName() + "]";
	}
}
