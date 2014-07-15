package scriptease.translator.codegenerator.code.contexts.knowitbinding;

import scriptease.model.atomic.knowitbindings.KnowItBinding;
import scriptease.model.atomic.knowitbindings.KnowItBindingReference;
import scriptease.translator.codegenerator.code.contexts.Context;
import scriptease.translator.codegenerator.code.contexts.ContextFactory;

/**
 * KnowItBindingReferenceContext is Context for a KnowItBindingReference object.
 * 
 * @see Context
 * @author mfchurch
 * 
 */
public class KnowItBindingReferenceContext extends KnowItBindingContext {
	public KnowItBindingReferenceContext(Context other, KnowItBinding source) {
		super(other, source);
	}

	/**
	 * Get the KnowItBinding's Reference Name
	 */
	@Override
	public String getValue() {
		Context knowItContext = ContextFactory.getInstance().createContext(
				this, ((KnowItBindingReference) this.binding).getValue());
		return knowItContext.getName();
	}
}