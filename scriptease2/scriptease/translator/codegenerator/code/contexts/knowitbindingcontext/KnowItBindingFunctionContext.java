package scriptease.translator.codegenerator.code.contexts.knowitbindingcontext;

import scriptease.model.atomic.knowitbindings.KnowItBinding;
import scriptease.model.atomic.knowitbindings.KnowItBindingFunction;
import scriptease.model.complex.StoryPoint;
import scriptease.translator.LanguageDictionary;
import scriptease.translator.Translator;
import scriptease.translator.codegenerator.CodeGenerationConstants;
import scriptease.translator.codegenerator.LocationInformation;
import scriptease.translator.codegenerator.code.CodeGenerationNamifier;
import scriptease.translator.codegenerator.code.contexts.Context;
import scriptease.translator.codegenerator.code.contexts.ContextFactory;
import scriptease.translator.codegenerator.code.fragments.AbstractFragment;

/**
 * KnowItBindingFunctionContext is Context for a KnowItBindingFunction object.
 * 
 * @see Context
 * @author mfchurch
 * 
 */
public class KnowItBindingFunctionContext extends KnowItBindingContext {

	public KnowItBindingFunctionContext(StoryPoint model,
			String indent, CodeGenerationNamifier existingNames,
			Translator translator, LocationInformation locationInfo) {
		super(model, indent, existingNames, translator, locationInfo);
	}

	public KnowItBindingFunctionContext(Context other) {
		this(other.getStartStoryPoint(), other.getIndent(), other.getNamifier(),
				other.getTranslator(), other.getLocationInfo());
	}

	public KnowItBindingFunctionContext(Context other, KnowItBinding source) {
		this(other);
		this.binding = source;
	}

	/**
	 * Get the KnowItBinding's ScriptIt Value
	 */
	@Override
	public String getValue() {
		final Context scriptItContext = ContextFactory.getInstance()
				.createContext(this,
						((KnowItBindingFunction) this.binding).getValue());
		final LanguageDictionary languageDictionary = this.getTranslator()
				.getLanguageDictionary();
		// TODO 'functionHeader' should not be hardcoded here. Figure out
		// why it is, and get it out of here.
		return AbstractFragment.resolveFormat(languageDictionary
				.getFormat(CodeGenerationConstants.FunctionConstants.FUNCTION_AS_VALUE.name()),
				scriptItContext);
	}
}
