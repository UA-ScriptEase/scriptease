package scriptease.translator.codegenerator.code.fragments;

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
	 * Retrieves the format that this fragment represents and forwards the call
	 * to it.
	 */
	@Override
	public String resolve(Context context) {
		final String format = this.getDirectiveText();

		if ((this.type == FormatReferenceType.NONE)
				|| (this.type == FormatReferenceType.ASKIT && context instanceof AskItContext)
				|| (this.type == FormatReferenceType.SCRIPTIT
						&& context instanceof ScriptItContext && !(context instanceof ControlItContext))
				|| (this.type == FormatReferenceType.KNOWIT && context instanceof KnowItContext)
				|| (this.type == FormatReferenceType.NOTE && context instanceof NoteContext)
				|| (this.type == FormatReferenceType.CONTROLIT && context instanceof ControlItContext)) {

			return AbstractFragment.resolveFormat(context.getTranslator()
					.getLanguageDictionary().getFormat(format.toUpperCase()),
					context);
		} else
			return "";
	}

	@Override
	public String toString() {
		return this.getDirectiveText();
	}
}
