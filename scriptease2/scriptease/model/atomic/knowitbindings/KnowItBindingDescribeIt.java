package scriptease.model.atomic.knowitbindings;

import java.util.Collection;

import scriptease.controller.BindingVisitor;
import scriptease.model.atomic.describeits.DescribeIt;
import scriptease.model.complex.ScriptIt;

public class KnowItBindingDescribeIt extends KnowItBinding {
	private final DescribeIt describeIt;

	public KnowItBindingDescribeIt(DescribeIt describeIt) {
		this.describeIt = describeIt;
	}

	@Override
	public DescribeIt getValue() {
		return this.describeIt;
	}

	@Override
	public Collection<String> getTypes() {
		return this.describeIt.getTypes();
	}

	@Override
	public boolean equals(Object other) {
		return (other instanceof KnowItBindingDescribeIt)
				&& ((KnowItBindingDescribeIt) other).describeIt
						.equals(this.describeIt);
	}

	@Override
	public KnowItBinding resolveBinding() {
		return this;
	}

	/**
	 * Return a new KnowItBindingDescribeIt bound to a clone of it's DescribeIt
	 */
	@Override
	public KnowItBinding clone() {
		return new KnowItBindingDescribeIt(this.describeIt.clone());
	}

	@Override
	public String getScriptValue() {
		final ScriptIt resolvedDoIt = this.describeIt.getResolvedScriptIt();
		// handle it the same way as a KnowItBindingFunction
		if (resolvedDoIt != null) {
			KnowItBindingFunction function = new KnowItBindingFunction(
					resolvedDoIt);
			return function.getScriptValue();
		}
		// otherwise, the describeIt has not been completed and cannot be
		// resolved
		return "";
	}

	@Override
	public void process(BindingVisitor processController) {
		processController.processDescribeIt(this);
	}
	
	@Override
	public String toString() {
		return "KnowItBindingDescribeIt [" + this.describeIt + "]";
	}
}
