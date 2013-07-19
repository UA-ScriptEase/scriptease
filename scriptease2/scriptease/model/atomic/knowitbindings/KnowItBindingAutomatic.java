package scriptease.model.atomic.knowitbindings;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import scriptease.controller.BindingVisitor;
import scriptease.translator.io.model.GameModule;
import scriptease.translator.io.model.Resource;

/**
 * This class represents a <b>automatic</b> binding for a <code>KnowIt</code>.
 * It is intended to be used as an automatic binding for Causes without
 * subjects, in order to attach the script to the resource associated with this
 * binding.
 * 
 * @author jyuen
 * @see {@link scriptease.model.atomic.KnowIt}
 */
public class KnowItBindingAutomatic extends KnowItBinding {
	private final Resource automatic;

	public KnowItBindingAutomatic(GameModule module) {
		final List<Resource> automaticHandlers = new ArrayList<Resource>();

		for (Resource resource : module.getAutomaticHandlers())
			automaticHandlers.add(resource);

		this.automatic = automaticHandlers.get(0);
	}

	@Override
	public String getScriptValue() {
		return "<automatic!>";
	}

	@Override
	public Resource getValue() {
		return automatic;
	}

	@Override
	public Collection<String> getTypes() {
		return automatic.getTypes();
	}

	@Override
	public boolean equals(Object other) {
		return other instanceof KnowItBindingAutomatic;
	}

	@Override
	public KnowItBinding resolveBinding() {
		return this;
	}

	@Override
	public void process(BindingVisitor processController) {
		processController.processAutomatic(this);
	}

	@Override
	public String toString() {
		return "KnowItBindingAutomatic : " + this.automatic.toString();
	}
}
