package scriptease.model.atomic.knowitbindings;

import java.util.ArrayList;
import java.util.Collection;

import scriptease.controller.BindingVisitor;
import scriptease.model.atomic.KnowIt;
import scriptease.translator.io.model.Resource;

/**
 * This class represents a <b>null</b> binding for a <code>KnowIt</code>.
 * 
 * @author graves
 * @see {@link scriptease.model.atomic.KnowIt}
 */
public class KnowItBindingNull extends KnowItBinding {
	public KnowItBindingNull() {
	}

	@Override
	public String getScriptValue() {
		return "<unbound!>";
	}

	/**
	 * Getting the value of a KnowItBindingNull is an invalid operation, but
	 * this method must be implemented because of the interface.
	 * 
	 * @throws UnsupportedOperationException
	 *             whenever this method is called.
	 */
	@Override
	public Resource getValue() {
		throw new UnsupportedOperationException(
				"Cannot get the value for an unbound KnowIt.");
	}

	/**
	 * A KnowItBindingNull is not bound to anything this replaces the need for
	 * instanceof checks
	 */
	@Override
	public boolean isBound() {
		return false;
	}

	@Override
	public String toString() {
		return "None";
	}

	@Override
	public Collection<String> getTypes() {
		return new ArrayList<String>(0);
	}

	@Override
	public boolean equals(Object other) {
		return other instanceof KnowItBindingNull;
	}

	@Override
	public int hashCode() {
		return 0;
	}

	/**
	 * KnowItBindingNull are always compatible
	 */
	@Override
	public boolean compatibleWith(KnowIt knowIt) {
		return true;
	}

	/**
	 * KnowItBindingNull always type match
	 */
	protected boolean typeMatches(Collection<String> knowItTypes) {
		return true;
	}

	@Override
	public KnowItBinding resolveBinding() {
		return this;
	}

	/**
	 * No need to clone KnowItBindingNulls, they aren't bound to anything
	 */
	@Override
	public KnowItBinding clone() {
		return this;
	}

	@Override
	public void process(BindingVisitor processController) {
		processController.processNull(this);
	}
}
