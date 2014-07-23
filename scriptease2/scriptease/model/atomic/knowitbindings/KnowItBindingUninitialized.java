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
	private final KnowItBindingReference reference;
	/**
	 * Builds a reference to the given KnowIt.
	 * 
	 * @param reference
	 *            the referent.
	 */
	public KnowItBindingUninitialized(KnowItBindingReference reference) {
		this.reference = reference;
		
	}

	@Override
	public String getScriptValue() {
		return this.reference.getScriptValue();
	}

	@Override
	public KnowIt getValue() {
		return this.reference.getValue();
	}

	@Override
	public Collection<String> getTypes() {
		return this.reference.getValue().getAcceptableTypes();
	}

	@Override
	public boolean equals(Object other) {
		return (other instanceof KnowItBindingUninitialized)
				&& ((KnowItBindingUninitialized) other).reference
						.equals(this.reference);
	}

	/**
	 * Determines the deepest non-reference KnowItBinding of the
	 * KnowItBindingReference chain.
	 * 
	 * @return
	 */
	@Override
	public KnowItBinding resolveBinding() {
		return this.reference.resolveBinding();
	}

	@Override
	public String toString() {
		return "KnowItBindingUninitialized : " + this.reference.toString();
	}

	@Override
	public boolean isBound() {
		return true;
	}

	@Override
	public KnowItBinding clone() {
		return this;
	}

	@Override
	protected boolean typeMatches(Collection<String> knowItTypes) {
		return this.reference.typeMatches(knowItTypes);
	}

	@Override
	public void process(BindingVisitor processController) {
		processController.processUninitialized(this);
	}
}
