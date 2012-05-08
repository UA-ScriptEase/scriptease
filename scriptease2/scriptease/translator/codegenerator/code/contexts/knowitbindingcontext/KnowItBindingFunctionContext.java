package scriptease.translator.codegenerator.code.contexts.knowitbindingcontext;

import scriptease.gui.quests.QuestNode;
import scriptease.model.atomic.knowitbindings.KnowItBinding;
import scriptease.model.atomic.knowitbindings.KnowItBindingFunction;
import scriptease.translator.LanguageDictionary;
import scriptease.translator.Translator;
import scriptease.translator.codegenerator.LocationInformation;
import scriptease.translator.codegenerator.TranslatorKeywordManager;
import scriptease.translator.codegenerator.code.CodeGenerationNamifier;
import scriptease.translator.codegenerator.code.contexts.Context;
import scriptease.translator.codegenerator.code.contexts.ContextFactory;
import scriptease.translator.codegenerator.code.fragments.FormatFragment;

/**
 * KnowItBindingFunctionContext is Context for a KnowItBindingFunction object.
 * 
 * @see Context
 * @author mfchurch
 * 
 */
public class KnowItBindingFunctionContext extends KnowItBindingContext {

	public KnowItBindingFunctionContext(QuestNode model,
			String indent, CodeGenerationNamifier existingNames,
			Translator translator, LocationInformation locationInfo) {
		super(model, indent, existingNames, translator, locationInfo);
	}

	public KnowItBindingFunctionContext(Context other) {
		this(other.getModel(), other.getIndent(), other.getNamifier(),
				other.getTranslator(), other.getLocationInfo());
	}

	public KnowItBindingFunctionContext(Context other, KnowItBinding source) {
		this(other);
		binding = source;
	}

	/**
	 * Get the KnowItBinding's ScriptIt Value
	 */
	@Override
	public String getValue() {
		final Context scriptItContext = ContextFactory.getInstance()
				.createContext(this,
						((KnowItBindingFunction) binding).getValue());
		final LanguageDictionary languageDictionary = this.getTranslator()
				.getLanguageDictionary();

		// Umm, why is 'functionHeader' hardcoded here? Do we require that
		// translator authors implement a 'functionHeader' format? -jtduncan
		// TODO: LOOK INTO THIS
		return FormatFragment.resolveFormat(languageDictionary
				.getFormat(TranslatorKeywordManager.FUNCTION_HEADER),
				scriptItContext);
	}
}
