package scriptease.model.atomic.knowitbindings;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import scriptease.controller.BindingVisitor;
import scriptease.model.atomic.KnowIt;
import scriptease.model.semodel.SEModel;
import scriptease.model.semodel.SEModelManager;
import scriptease.model.semodel.StoryModel;
import scriptease.translator.io.model.Resource;
import scriptease.translator.io.model.SimpleResource;

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

	public KnowItBindingAutomatic() {
	}

	@Override
	public String getScriptValue() {
		return "<automatic!>";
	}

	/**
	 * The value of a KnowItBindingAutomatic is the automatic specific to the
	 * module. In the case of NwN it would be the module.IFO resource. In the
	 * case of Unity it would be the first ScriptEase object encountered.
	 * 
	 * In the scenario where this method is called before there is an active
	 * model, we return a new placeholder SimpleResource that doesn't actually
	 * do anything.
	 */
	@Override
	public Resource getValue() {
		final List<Resource> automatics = new ArrayList<Resource>();

		final SEModel seModel = SEModelManager.getInstance().getActiveModel();

		if (seModel == null)
			return new SimpleResource(new ArrayList<String>(), "");

		if (seModel instanceof StoryModel) {
			StoryModel storyModel = (StoryModel) seModel;
			automatics.addAll(storyModel.getModule().getAutomaticHandlers());
		}

		final Resource automatic = automatics.get(0);

		return automatic;
	}

	@Override
	public String toString() {
		return "automatic";
	}

	@Override
	public Collection<String> getTypes() {
		return new ArrayList<String>(0);
	}

	@Override
	public boolean equals(Object other) {
		return other instanceof KnowItBindingAutomatic;
	}

	@Override
	public int hashCode() {
		return 0;
	}

	/**
	 * KnowItBindingAutomatics are always compatible
	 */
	@Override
	public boolean compatibleWith(KnowIt knowIt) {
		return true;
	}

	/**
	 * KnowItBindingAutomatics always type match
	 */
	protected boolean typeMatches(Collection<String> knowItTypes) {
		return true;
	}

	@Override
	public KnowItBinding resolveBinding() {
		return this;
	}

	/**
	 * No need to clone KnowItBindingAutomatics, they aren't bound to anything
	 * until code generation
	 */
	@Override
	public KnowItBinding clone() {
		return this;
	}

	@Override
	public void process(BindingVisitor processController) {
		processController.processAutomatic(this);
	}
}
