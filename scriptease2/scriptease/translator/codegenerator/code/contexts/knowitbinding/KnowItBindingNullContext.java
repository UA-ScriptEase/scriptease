package scriptease.translator.codegenerator.code.contexts.knowitbinding;

import scriptease.model.atomic.knowitbindings.KnowItBinding;
import scriptease.model.atomic.knowitbindings.KnowItBindingNull;
import scriptease.translator.codegenerator.code.contexts.Context;

/**
 * KnowItBindingNullContext is Context for a KnowItBindingNull object.
 * 
 * @see Context
 * @author mfchurch
 * 
 */
public class KnowItBindingNullContext extends KnowItBindingContext {
	public KnowItBindingNullContext(Context other, KnowItBinding source) {
		super(other, source);
	}

	/**
	 * Get the KnowItBinding's GameConstant Value
	 */
	@Override
	public String getValue() {
		return ((KnowItBindingNull) this.binding).getScriptValue();
	}

}
