package scriptease.translator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import scriptease.translator.codegenerator.code.fragments.AbstractFragment;
import scriptease.translator.codegenerator.code.fragments.container.FormatDefinitionFragment;

/**
 * Manages the description of the language grammar.
 * 
 * @author remiller
 * @author jtduncan
 * @author mfchurch
 */
public class LanguageDictionary {

	private final Collection<FormatDefinitionFragment> formats;
	private final Collection<String> reservedWords;
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
			Collection<FormatDefinitionFragment> formats) {
		this.name = name;
		this.indentString = indentString;
		this.reservedWords = new HashSet<String>(reservedWords);
		this.formats = new ArrayList<FormatDefinitionFragment>(formats);
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
	 * Get the Collection<FormatFragment> representing the given Format
	 * 
	 * @param formatID
	 * @return
	 */
	public List<AbstractFragment> getFormat(String formatID) {
		final List<AbstractFragment> format = new ArrayList<AbstractFragment>();

		boolean containsFragment = false;

		for (FormatDefinitionFragment formatFragment : this.formats) {
			if (formatFragment.getDirectiveText().equalsIgnoreCase(formatID)) {
				format.addAll(formatFragment.getSubFragments());
				containsFragment = true;
				break;
			}
		}

		if (!containsFragment)
			throw new IllegalArgumentException("Unable to resolve formatID "
					+ formatID);

		return format;
	}

	public Collection<FormatDefinitionFragment> getFormats() {
		return this.formats;
	}

	@Override
	public String toString() {
		return "LanguageDictionary [" + this.getName() + "]";
	}
}
