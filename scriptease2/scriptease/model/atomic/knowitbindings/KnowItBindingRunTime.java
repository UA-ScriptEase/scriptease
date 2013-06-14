package scriptease.model.atomic.knowitbindings;

import java.util.Collection;

import scriptease.controller.BindingVisitor;

/**
 * This class represents a <b>Runtime</b> binding for a <code>KnowIt</code>.
 * Runtime bindings are values that can be resolved during runtime and are not
 * know at compile time.
 * 
 * @author mfchurch
 * @see {@link scriptease.model.atomic.KnowIt}
 */
public class KnowItBindingRunTime extends KnowItBinding {
	private Collection<String> types;

	public KnowItBindingRunTime(Collection<String> types) {
		this.types = types;
	}

	@Override
	public boolean equals(Object other) {
		return other instanceof KnowItBindingRunTime;
	}

	@Override
	public String getScriptValue() {
		return "";
	}

	@Override
	public Collection<String> getTypes() {
		return this.types;
	}

	@Override
	public Object getValue() {
		return null;
	}

	@Override
	public KnowItBinding resolveBinding() {
		return this;
	}

	@Override
	public String toString() {
		return "KnowItBindingRunTime : "
				+ this.types.iterator().next().toString();
	}

	@Override
	public KnowItBinding clone() {
		return new KnowItBindingRunTime(this.types);
	}

	@Override
	public void process(BindingVisitor processController) {
		processController.processRunTime(this);
	}
}
