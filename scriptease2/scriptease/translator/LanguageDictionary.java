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
 * @author kschenk
 */
public class LanguageDictionary {

	private final Collection<FormatDefinitionFragment> formats;
	private final Collection<String> reservedWords;
	private final String indentString;
	private final String lineBreak;
	private final String name;
	private final String functionCallFormat;

	/**
	 * Builds a new LanguageDictionary to represent the given data.
	 * 
	 * @param name
	 *            Name of the dictionary.
	 * @param indentString
	 *            String to be used for indenting code.
	 * @param lineBreak
	 *            String used to add line breaks to code.
	 * @param functionCallFormat
	 *            The name of the format used for function calls.
	 * @param reservedWords
	 *            List of strings to be avoided when generating names in code
	 *            generation.
	 * @param formatMap
	 * @param maps
	 */
	public LanguageDictionary(String name, String indentString,
			String lineBreak, String functionCallFormat,
			Collection<String> reservedWords,
			Collection<FormatDefinitionFragment> formats) {
		this.name = name;
		this.indentString = indentString;
		this.lineBreak = lineBreak;
		this.functionCallFormat = functionCallFormat;
		this.reservedWords = new HashSet<String>(reservedWords);
		this.formats = new ArrayList<FormatDefinitionFragment>(formats);
	}

	/**
	 * Returns the name of the format used for function calls.
	 * 
	 * @return
	 */
	public String getFunctionCallFormatName() {
		return functionCallFormat;
	}

	/**
	 * Returns the format of function calls.
	 * 
	 * @return
	 */
	public List<AbstractFragment> getFunctionCallFormat() {
		return this.getFormat(this.functionCallFormat);
	}

	/**
	 * Returns the translator dependent line break character.
	 * 
	 * @return
	 */
	public String getLineBreak() {
		return lineBreak;
	}

	/**
	 * Returns the name of the language dictionary.
	 * 
	 * @return
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Returns the translator dependent indent character.
	 * 
	 * @return
	 */
	public String getIndent() {
		return this.indentString;
	}

	/**
	 * Returns the reserved words of the translator, i.e. those we shouldn't use
	 * in code gen.
	 * 
	 * @return
	 */
	public Collection<String> getReservedWords() {
		return this.reservedWords;
	}

	/**
	 * Returns true if a word is reserved.
	 * 
	 * @param word
	 * @return
	 */
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

	/**
	 * Returns all of the possible formats.
	 * 
	 * @return
	 */
	public Collection<FormatDefinitionFragment> getFormats() {
		return this.formats;
	}

	@Override
	public String toString() {
		return "LanguageDictionary [" + this.getName() + "]";
	}
}
