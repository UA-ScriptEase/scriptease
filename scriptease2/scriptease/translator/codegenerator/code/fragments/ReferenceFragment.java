package scriptease.translator.codegenerator.code.fragments;

import scriptease.translator.codegenerator.CharacterRange;
import scriptease.translator.codegenerator.code.contexts.Context;

/**
 * Represents a code location where another format from the format dictionary is
 * to be inserted.
 * 
 * @author remiller
 */
public class ReferenceFragment extends FormatFragment {
	/**
	 * See:
	 * {@link FormatFragment#FormatFragment(String, CharacterRange, char[])}
	 * 
	 * @param text
	 *            The format reference label.
	 */
	public ReferenceFragment(String text) {
		super(text);
	}

	/**
	 * Retrieves the format that this fragment represents and forwards the call
	 * to it.
	 */
	@Override
	public String resolve(Context context) {
		final String format = this.getDirectiveText();
		return FormatFragment.resolveFormat(context.getTranslator()
				.getLanguageDictionary().getFormat(format.toUpperCase()),
				context);
	}

	@Override
	public String toString() {
		return this.getDirectiveText();
	}
}
