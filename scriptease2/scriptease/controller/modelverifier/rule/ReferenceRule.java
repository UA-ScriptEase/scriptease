package scriptease.controller.modelverifier.rule;

import java.util.ArrayList;
import java.util.Collection;

import scriptease.controller.AbstractNoOpBindingVisitor;
import scriptease.controller.AbstractNoOpStoryVisitor;
import scriptease.controller.ScopeVisitor;
import scriptease.controller.modelverifier.problem.ModelProblem;
import scriptease.controller.modelverifier.problem.StoryProblem;
import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.knowitbindings.KnowItBinding;
import scriptease.model.atomic.knowitbindings.KnowItBindingFunction;
import scriptease.model.atomic.knowitbindings.KnowItBindingReference;
import scriptease.model.complex.AskIt;
import scriptease.model.complex.ComplexStoryComponent;
import scriptease.model.complex.ScriptIt;
import scriptease.model.complex.StoryComponentContainer;
import scriptease.model.complex.StoryItemSequence;

/**
 * Checks the if all KnowIts in the model are in proper scope
 * 
 * @author mfchurch
 * 
 */
public class ReferenceRule extends AbstractNoOpStoryVisitor implements
		StoryRule {
	Collection<StoryProblem> rebindings;

	@Override
	public Collection<StoryProblem> validate(ComplexStoryComponent root,
			StoryComponent source) {
		this.rebindings = new ArrayList<StoryProblem>();
		root.process(this);
		return this.rebindings;
	}

	@Override
	public void processScriptIt(ScriptIt scriptIt) {
		scriptIt.processSubjects(this);
		scriptIt.processParameters(this);
		processComplexStoryComponent(scriptIt);
	}

	@Override
	public void processAskIt(AskIt questionIt) {
		// Check the askIt's condition
		questionIt.getCondition().process(this);
		// Check the children
		processComplexStoryComponent(questionIt);
	}

	@Override
	public void processStoryItemSequence(StoryItemSequence sequence) {
		// Check the children
		processComplexStoryComponent(sequence);
	}

	@Override
	public void processKnowIt(final KnowIt knowIt) {
		final KnowItBinding binding = knowIt.getBinding();

		// Check the knowIt's binding to see if it has an equivalent reference
		// in scope
		binding.process(new AbstractNoOpBindingVisitor() {

			@Override
			public void processReference(KnowItBindingReference reference) {
				KnowIt referenced = reference.getValue();
				KnowIt comparableReference = null;
				final Collection<KnowIt> inScope = ScopeVisitor
						.getScope(knowIt);

				for (KnowIt scoped : inScope) {
					// if the original reference is found, do nothing
					if (scoped == referenced)
						return;
					else if (scoped.equals(referenced)) {
						comparableReference = scoped;
						break;
					}
				}
				// if an equal reference is found, rebind the knowIt
				if (comparableReference != null)
					ReferenceRule.this.rebindReference(knowIt,
							comparableReference);
				// otherwise the reference could not be found, and should be
				// unbound
				else {
					System.err.println("Reference broken!");
					// ReferenceRule.this.unbindReference(knowIt);
				}
			}

			@Override
			public void processFunction(KnowItBindingFunction function) {
				ScriptIt referenced = function.getValue();
				referenced.process(ReferenceRule.this);
			}
		});
	}

	@Override
	public void processStoryComponentContainer(
			StoryComponentContainer storyComponentContainer) {
		// Check the children
		processComplexStoryComponent(storyComponentContainer);
	}

	private void processComplexStoryComponent(ComplexStoryComponent component) {
		component.processChildren(this);
	}

	private void rebindReference(final KnowIt knowIt,
			final KnowIt comparableReference) {
		final String description = "The reference will be rebound to an equivalent KnowIt, since the original was not found";
		final int priority = 0;
		final Runnable solution = new Runnable() {
			@Override
			public void run() {
				knowIt.setBinding(comparableReference);
			}
		};
		ModelProblem problem = new ModelProblem(knowIt, description, solution,
				priority);
		/*
		 * do not notify the user of this problem, it will be fixed behind the
		 * scenes
		 */
		problem.setNotify(false);
		this.rebindings.add(problem);
	}

	@Override
	public String toString() {
		return "ReferenceRule";
	}
}
