package scriptease.translator.codegenerator.code.contexts.knowitbinding;

import java.util.List;

import scriptease.model.atomic.knowitbindings.KnowItBinding;
import scriptease.translator.codegenerator.code.contexts.Context;
import scriptease.translator.codegenerator.code.contexts.ContextFactory;
import scriptease.translator.codegenerator.code.fragments.AbstractFragment;

/**
 * KnowItBindingFunctionContext is Context for a KnowItBindingFunction object.
 * 
 * @see Context
 * @author mfchurch
 * @author kschenk
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
				.createContext(this, this.binding.getValue());

		final List<AbstractFragment> format;
		final String value;

		format = this.getTranslator().getLanguageDictionary()
				.getFunctionCallFormat();
		value = AbstractFragment.resolveFormat(format, scriptItContext);

		return value;
	}
}
