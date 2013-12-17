package scriptease.translator.codegenerator.code.contexts.knowitbindingcontext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import scriptease.model.atomic.knowitbindings.KnowItBinding;
import scriptease.translator.codegenerator.code.contexts.Context;
import scriptease.translator.codegenerator.code.fragments.AbstractFragment;
import scriptease.translator.io.model.GameType;
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
		final String typeKeyword;
		final GameType type;

		typeKeyword = this.binding.getFirstType();
		type = this.getModel().getType(typeKeyword);

		if (type != null) {
			final Collection<AbstractFragment> typeFormat;

			typeFormat = type.getFormat();

			if (typeFormat != null && !typeFormat.isEmpty())
				return AbstractFragment.resolveFormat(typeFormat, this);
		}
		return this.getValue();
	}

	/**
	 * Get the KnowItBinding's value
	 */
	@Override
	public String getValue() {
		final List<Resource> automatics = new ArrayList<Resource>();

		automatics.addAll(this.getModel().getModule().getAutomaticHandlers()
				.get("automatic"));

		if (!automatics.isEmpty())
			return automatics.get(0).getCodeText();
		else
			return "No automatics found";
	}
}
