package scriptease.model.atomic.knowitbindings;

import java.util.Collection;

import scriptease.controller.BindingVisitor;
import scriptease.model.atomic.KnowIt;

/**
 * KnowItBindingUninitialized is used to represent a reference to a unbound
 * KnowIt (KnowIt with a null binding) that can be dragged and dropped.
 * 
 * @author jyuen
 * 
 */
public class KnowItBindingUninitialized extends KnowItBinding {
	private final KnowIt referenceValue;

	/**
	 * Builds a reference to the given KnowIt.
	 * 
	 * @param value
	 *            the referent.
	 */
	public KnowItBindingUninitialized(KnowIt value) {
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
		return referenceValue.getAcceptableTypes();
	}

	@Override
	public boolean equals(Object other) {
		return (other instanceof KnowItBindingUninitialized)
				&& ((KnowItBindingUninitialized) other).referenceValue
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
		return "KnowItBindingUninitialized : " + this.referenceValue.toString();
	}

	@Override
	public boolean isBound() {
		return true;
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
	public void process(BindingVisitor processController) {
		processController.processUninitialized(this);
	}
}
