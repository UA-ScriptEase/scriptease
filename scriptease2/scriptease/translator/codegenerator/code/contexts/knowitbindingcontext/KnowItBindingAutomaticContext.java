package scriptease.translator.codegenerator.code.contexts.knowitbindingcontext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import scriptease.model.atomic.knowitbindings.KnowItBinding;
import scriptease.translator.codegenerator.code.contexts.Context;
import scriptease.translator.codegenerator.code.fragments.AbstractFragment;
import scriptease.translator.io.model.Resource;

public class KnowItBindingAutomaticContext extends KnowItBindingContext {

	public KnowItBindingAutomaticContext(Context other, KnowItBinding source) {
		super(other, source);
	}

	/**
	 * Get the KnowItBinding's Type Formatted GameConstant Value, if no format
	 * is specified the ScriptValue of the binding is returned
	 */
	@Override
	public String getFormattedValue() {
		final Collection<AbstractFragment> typeFormat;
		final String type;

		type = this.binding.getFirstType();

		typeFormat = this.getModel().getTypeFormat(type);

		if (typeFormat == null || typeFormat.isEmpty()) {
			return this.getValue();
		}

		return AbstractFragment.resolveFormat(typeFormat, this);
	}

	/**
	 * Get the KnowItBinding's value
	 */
	@Override
	public String getValue() {
		final List<Resource> automatics = new ArrayList<Resource>();
		
		automatics.addAll(this.getModel().getModule().getAutomaticHandlers());

		final Resource automatic = automatics.get(0);
		
		return automatic.getCodeText();
	}

}
