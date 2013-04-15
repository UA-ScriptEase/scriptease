package scriptease.translator.codegenerator.code.fragments;

import java.util.List;

import scriptease.controller.StoryAdapter;
import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.Note;
import scriptease.model.complex.AskIt;
import scriptease.model.complex.ControlIt;
import scriptease.model.complex.ScriptIt;
import scriptease.translator.codegenerator.CodeGenerationConstants.FormatReferenceType;
import scriptease.translator.codegenerator.code.contexts.Context;
import scriptease.translator.codegenerator.code.contexts.StoryComponentContext;

/**
 * Represents a code location where another format from the format dictionary is
 * to be inserted.
 * 
 * @author remiller
 * @author kschenk
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
		final String formatID = this.getDirectiveText().toUpperCase();
		final TypeChecker typeChecker = new TypeChecker(context);

		if (typeChecker.getResult()) {
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

	/**
	 * Checks if the context is of the same type.
	 */
	private class TypeChecker extends StoryAdapter {
		private boolean typeMatches;
		private FormatReferenceType type;

		private TypeChecker(Context context) {
			this.type = FormatReferenceFragment.this.type;
			this.typeMatches = this.type == FormatReferenceType.NONE;

			if (!this.typeMatches && context instanceof StoryComponentContext) {
				((StoryComponentContext) context).getComponent().process(this);
			}

		}

		@Override
		public void processAskIt(AskIt questionIt) {
			this.typeMatches = this.type == FormatReferenceType.ASKIT;
		}

		@Override
		public void processNote(Note note) {
			this.typeMatches = this.type == FormatReferenceType.NOTE;
		}

		@Override
		public void processControlIt(ControlIt controlIt) {
			this.typeMatches = this.type == FormatReferenceType.CONTROLIT;
		}

		@Override
		public void processScriptIt(ScriptIt scriptIt) {
			this.typeMatches = this.type == FormatReferenceType.SCRIPTIT;
		}

		@Override
		public void processKnowIt(KnowIt knowIt) {
			this.typeMatches = this.type == FormatReferenceType.KNOWIT;
		}

		private boolean getResult() {
			return this.typeMatches;
		}
	}
}
