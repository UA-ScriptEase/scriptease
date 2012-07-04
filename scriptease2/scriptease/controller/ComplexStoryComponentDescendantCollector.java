package scriptease.controller;

import java.util.ArrayList;
import java.util.Collection;

import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.knowitbindings.KnowItBindingDescribeIt;
import scriptease.model.atomic.knowitbindings.KnowItBindingFunction;
import scriptease.model.atomic.knowitbindings.KnowItBindingReference;
import scriptease.model.complex.AskIt;
import scriptease.model.complex.ComplexStoryComponent;
import scriptease.model.complex.ScriptIt;

/**
 * Visitor which collects all of the decendants of a given ComplexStoryComponent
 * 
 * @author mfchurch
 * 
 */
public class ComplexStoryComponentDescendantCollector extends
		AbstractNoOpStoryVisitor {
	private Collection<StoryComponent> children;

	public Collection<StoryComponent> collectDescendants(
			ComplexStoryComponent complex) {
		children = new ArrayList<StoryComponent>();
		/**
		 * process the immediate children, so that we don't add the complex as a
		 */
		for (StoryComponent child : complex.getChildren())
			child.process(this);
		return children;
	}

	@Override
	protected void defaultProcessComplex(ComplexStoryComponent complex) {
		children.add(complex);
		for (StoryComponent child : complex.getChildren())
			child.process(this);
	}

	@Override
	public void processScriptIt(ScriptIt scriptIt) {
		scriptIt.processParameters(this);
		defaultProcessComplex(scriptIt);
	}

	@Override
	public void processKnowIt(KnowIt knowIt) {
		children.add(knowIt);
		knowIt.getBinding().process(new AbstractNoOpBindingVisitor() {
			@Override
			public void processDescribeIt(KnowItBindingDescribeIt described) {
				ScriptIt resolvedScriptIt = described.getValue()
						.getResolvedScriptIt();
				if (resolvedScriptIt != null)
					resolvedScriptIt
							.process(ComplexStoryComponentDescendantCollector.this);
			}

			@Override
			public void processFunction(KnowItBindingFunction function) {
				ScriptIt scriptIt = function.getValue();
				scriptIt.process(ComplexStoryComponentDescendantCollector.this);
			}

			@Override
			public void processReference(KnowItBindingReference reference) {
				KnowIt value = reference.getValue();
				value.process(ComplexStoryComponentDescendantCollector.this);
			}
		});
	}

	@Override
	public void processAskIt(AskIt questionIt) {
		children.add(questionIt);
		questionIt.getIfBlock().process(this);
		questionIt.getElseBlock().process(this);
	}
}
