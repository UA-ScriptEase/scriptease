package scriptease.model.atomic.knowitbindings;

import java.util.Collection;

import scriptease.controller.BindingVisitor;
import scriptease.model.complex.ScriptIt;

/**
 * This class represents a <b>Function</b> binding for a <code>KnowIt</code>.
 * 
 * @author graves
 * @see {@link scriptease.model.atomic.KnowIt}
 */
public class KnowItBindingFunction extends KnowItBinding {
	private final ScriptIt functionValue;

	public KnowItBindingFunction(ScriptIt value) {
		this.functionValue = value;
	}

	@Override
	public String getScriptValue() {
		return "<NO SCRIPT VALUE WITHOUT SLOT>";
	}

	@Override
	public ScriptIt getValue() {
		return this.functionValue;
	}

	@Override
	public Collection<String> getTypes() {
		return this.functionValue.getTypes();
	}

	@Override
	public boolean equals(Object other) {
		return (other instanceof KnowItBindingFunction)
				&& ((KnowItBindingFunction) other).functionValue
						.equals(this.functionValue);
	}

	@Override
	public String toString() {
		return "KnowItBindingFunction : " + this.functionValue.toString();
	}

	@Override
	public KnowItBinding resolveBinding() {
		return this;
	}

	/**
	 * Return a new KnowItBindingFunction bound to a clone of its DoIt
	 */
	@Override
	public KnowItBinding clone() {
		return new KnowItBindingFunction(this.functionValue.clone());
	}

	@Override
	public void process(BindingVisitor processController) {
		processController.processFunction(this);
	}
}
