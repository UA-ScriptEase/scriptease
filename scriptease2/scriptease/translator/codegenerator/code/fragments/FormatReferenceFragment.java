package scriptease.translator.codegenerator.code.fragments;

import java.util.List;

import scriptease.translator.codegenerator.CharacterRange;
import scriptease.translator.codegenerator.CodeGenerationKeywordConstants.FormatReferenceType;
import scriptease.translator.codegenerator.code.contexts.AskItContext;
import scriptease.translator.codegenerator.code.contexts.Context;
import scriptease.translator.codegenerator.code.contexts.ControlItContext;
import scriptease.translator.codegenerator.code.contexts.KnowItContext;
import scriptease.translator.codegenerator.code.contexts.NoteContext;
import scriptease.translator.codegenerator.code.contexts.ScriptItContext;

/**
 * Represents a code location where another format from the format dictionary is
 * to be inserted.
 * 
 * @author remiller
 */
public class FormatReferenceFragment extends AbstractFragment {
	private final FormatReferenceType type;

	public FormatReferenceFragment(String text) {
		this(text, FormatReferenceType.NONE);
	}

	/**
	 * See:
	 * {@link AbstractFragment#FormatFragment(String, CharacterRange, char[])}
	 * 
	 * @param text
	 *            The format reference label.
	 */
	public FormatReferenceFragment(String text, FormatReferenceType type) {
		super(text);
		this.type = type;
	}

	public FormatReferenceType getType() {
		return this.type;
	}

	/**
	 * Checks if the context is of the same type.
	 * 
	 * @param context
	 * @return
	 */
	private boolean typeMatchesContext(Context context) {
		switch (this.type) {
		case NONE:
			return true;
		case ASKIT:
			return context instanceof AskItContext;
		case SCRIPTIT:
			return context instanceof ScriptItContext
					&& !(context instanceof ControlItContext);
		case KNOWIT:
			return context instanceof KnowItContext;
		case NOTE:
			return context instanceof NoteContext;
		case CONTROLIT:
			return context instanceof ControlItContext;
		default:
			throw new IllegalStateException(this + " has " + this.type
					+ " type, which is unsupported.");
		}
	}

	/**
	 * Retrieves the format that this fragment represents and forwards the call
	 * to it.
	 */
	@Override
	public String resolve(Context context) {
		final String formatID = this.getDirectiveText().toUpperCase();

		if (this.typeMatchesContext(context)) {
			final List<AbstractFragment> format;

			format = context.getTranslator().getLanguageDictionary()
					.getFormat(formatID);

			return AbstractFragment.resolveFormat(format, context);
		} else
			// If the type of the context doesn't match the format, we just
			// return nothing.
			return "";
	}

	@Override
	public String toString() {
		return this.getDirectiveText();
	}
}
