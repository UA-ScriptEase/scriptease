package scriptease.translator.codegenerator.code.contexts.knowitbindingcontext;

import java.util.List;

import scriptease.model.atomic.knowitbindings.KnowItBinding;
import scriptease.model.atomic.knowitbindings.KnowItBindingFunction;
import scriptease.translator.LanguageDictionary;
import scriptease.translator.codegenerator.CodeGenerationConstants;
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
	public KnowItBindingFunctionContext(Context other, KnowItBinding source) {
		super(other, source);
	}

	/**
	 * Get the KnowItBinding's ScriptIt Value
	 */
	@Override
	public String getValue() {
		final Context scriptItContext = ContextFactory.getInstance()
				.createContext(this,
						((KnowItBindingFunction) this.binding).getValue());
		final LanguageDictionary languageDictionary;
		final List<AbstractFragment> format;
		final String value;

		languageDictionary = this.getTranslator().getLanguageDictionary();
		/*
		 * TODO 'functionHeader' should not be hardcoded here. Figure out why it
		 * is, and get it out of here.
		 * 
		 * Ticket: 32213279
		 */
		format = languageDictionary
				.getFormat(CodeGenerationConstants.FunctionConstants.FUNCTION_AS_VALUE
						.name());
		value = AbstractFragment.resolveFormat(format, scriptItContext);

		return value;
	}
}
