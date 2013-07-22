package scriptease.model.atomic.knowitbindings;

import java.util.ArrayList;
import java.util.Collection;

import scriptease.controller.BindingVisitor;
import scriptease.model.atomic.KnowIt;
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
	public KnowItBindingAutomatic() {
	}

	@Override
	public String getScriptValue() {
		return "<automatic!>";
	}

	/**
	 * Getting the value of a KnowItBindingAutomatic is an invalid operation,
	 * but this method must be implemented because of the interface.
	 * 
	 * @throws UnsupportedOperationException
	 *             whenever this method is called.
	 */
	@Override
	public Resource getValue() {
		throw new UnsupportedOperationException(
				"Cannot get the value for an automatic KnowIt.");
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
