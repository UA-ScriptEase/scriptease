package scriptease.model.atomic.knowitbindings;

import java.util.ArrayList;
import java.util.Collection;

import scriptease.controller.BindingVisitor;
import scriptease.controller.ScopeVisitor;
import scriptease.model.atomic.KnowIt;

/**
 * This class represents a <b>Reference</b> binding for a <code>KnowIt</code>.
 * 
 * @author graves
 * @author mfchurch
 * @see {@link scriptease.model.atomic.KnowIt}
 */
public class KnowItBindingReference extends KnowItBinding {
	private final KnowIt referenceValue;

	/**
	 * Builds a reference to the given KnowIt.
	 * 
	 * @param value
	 *            the referent.
	 */
	public KnowItBindingReference(KnowIt value) {
		this.referenceValue = value;
	}

	@Override
	public String getScriptValue() {
		return this.referenceValue.getScriptValue();
	}

	@Override
	public KnowIt getValue() {
		return this.referenceValue;
	}

	@Override
	public Collection<String> getTypes() {
		KnowItBinding binding = resolveBinding();
		if (binding instanceof KnowItBindingNull) {
			ArrayList<String> typeList = new ArrayList<String>(1);
			typeList.add(this.referenceValue.getDefaultType());
			return typeList;
		} else {
			return binding.getTypes();
		}
	}

	@Override
	public boolean equals(Object other) {
		return (other instanceof KnowItBindingReference)
				&& ((KnowItBindingReference) other).referenceValue
						.equals(this.referenceValue);
	}

	/**
	 * Determines the deepest non-reference KnowItBinding of the
	 * KnowItBindingReference chain.
	 * 
	 * @return
	 */
	@Override
	public KnowItBinding resolveBinding() {
		KnowIt reference = this.referenceValue;
		while (reference.getBinding() instanceof KnowItBindingReference) {
			reference = (KnowIt) reference.getBinding().getValue();
		}
		return reference.getBinding();
	}

	@Override
	public String toString() {
		return "KnowItBindingReference : " + this.referenceValue.toString();
	}

	@Override
	public boolean isBound() {
		return this.referenceValue.getBinding().isBound();
	}

	@Override
	protected boolean typeMatches(Collection<String> knowItTypes) {
		if (super.typeMatches(knowItTypes)) {
			KnowIt value = this.getValue();
			if (value != null) {
				Collection<String> types = value.getAcceptableTypes();
				// return true if they share at least one matching type
				for (String type : types) {
					if (knowItTypes.contains(type))
						return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean compatibleWith(KnowIt knowIt) {
		if (typeMatches(knowIt.getAcceptableTypes()) && !hasBindingLoop(knowIt)) {
			KnowItBinding value = this.resolveBinding();
			if ((value instanceof KnowItBindingFunction || value instanceof KnowItBindingDescribeIt)
					&& knowIt.getOwner() != null)
				return ScopeVisitor.getScope(knowIt).contains(this.getValue());
			else
				return true;
		}
		return false;
	}

	/**
	 * Verifies if the given KnowIt is eventually resolved to by this
	 * KnowItBindingReference
	 * 
	 * @param value
	 * @return
	 */
	private boolean hasBindingLoop(KnowIt knowIt) {
		KnowIt value = this.getValue();
		KnowItBinding binding = value.getBinding();
		while (binding instanceof KnowItBindingReference) {
			if (value.equals(knowIt)) {
				System.err
						.println("KnowItBindingReference loop detected, binding was not set");
				return true;
			}
			value = (KnowIt) binding.getValue();
			binding = value.getBinding();
		}
		return false;
	}

	/**
	 * No need to clone references, the point is that they stay the same
	 */
	@Override
	public KnowItBinding clone() {
		return this;
	}

	@Override
	public void process(BindingVisitor processController) {
		processController.processReference(this);
	}
}
