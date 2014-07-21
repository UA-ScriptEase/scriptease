package scriptease.translator.codegenerator.code.contexts.knowitbinding;

import scriptease.model.atomic.knowitbindings.KnowItBinding;
import scriptease.model.atomic.knowitbindings.KnowItBindingUninitialized;
import scriptease.translator.codegenerator.code.contexts.Context;
import scriptease.translator.codegenerator.code.contexts.ContextFactory;

/**
 * KnowItBindingUninitializedContext is a Context for a
 * KnowItBindingUninitialized object.
 * 
 * @see Context
 * @author jyuen
 */
public class KnowItBindingUninitializedContext extends KnowItBindingContext {

	public KnowItBindingUninitializedContext(Context other, KnowItBinding source) {
		super(other, source);
	}

	/**
	 * Get the KnowItBinding's Reference Name
	 */
	@Override
	public String getValue() {
		Context knowItContext = ContextFactory.getInstance().createContext(
				this, ((KnowItBindingUninitialized) this.binding).getValue());
		final String value = knowItContext.getName();

		if (value.equals("Initiator_0")) {
			final String value2 = knowItContext.getName();

		}
		
		
		return value;
	}
}
