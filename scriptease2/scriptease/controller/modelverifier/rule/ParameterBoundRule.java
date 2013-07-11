package scriptease.controller.modelverifier.rule;

import java.util.ArrayList;
import java.util.Collection;

import scriptease.controller.BindingAdapter;
import scriptease.controller.StoryAdapter;
import scriptease.controller.modelverifier.problem.StoryProblem;
import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.knowitbindings.KnowItBinding;
import scriptease.model.atomic.knowitbindings.KnowItBindingFunction;
import scriptease.model.atomic.knowitbindings.KnowItBindingNull;
import scriptease.model.atomic.knowitbindings.KnowItBindingReference;
import scriptease.model.complex.AskIt;
import scriptease.model.complex.ComplexStoryComponent;
import scriptease.model.complex.ScriptIt;
import scriptease.model.complex.StoryComponentContainer;

/**
 * Checks if the parameters of the given source are bound (not
 * KnowItBindingNull);
 * 
 * @author mfchurch
 * @author jyuen
 * 
 */
public class ParameterBoundRule extends StoryAdapter implements
		StoryRule {
	private Collection<StoryProblem> problems;

	@Override
	public Collection<StoryProblem> validate(ComplexStoryComponent root,
			StoryComponent source) {
		this.problems = new ArrayList<StoryProblem>();
		source.process(this);
		return this.problems;
	}

	private void processCodeBlockStoryComponent(ScriptIt component) {
		for (KnowIt param : component.getParameters()) {
			param.process(this);
		}
	}

	@Override
	public void processScriptIt(ScriptIt doIt) {
		processCodeBlockStoryComponent(doIt);
	}

	/**
	 * If the KnowIt is unbound, we have a problem
	 */
	@Override
	public void processKnowIt(final KnowIt knowIt) {
		final KnowItBinding binding = knowIt.getBinding();
		binding.process(new BindingAdapter() {

			@Override
			public void processNull(KnowItBindingNull nullBinding) {
				ParameterBoundRule.this.addProblem(knowIt);
			}

			@Override
			public void processFunction(KnowItBindingFunction function) {
				function.getValue().process(ParameterBoundRule.this);
			}

			@Override
			public void processReference(KnowItBindingReference reference) {
				reference.getValue().process(ParameterBoundRule.this);
			}
		});
	}
	
	@Override
	public void processAskIt(final AskIt askIt) {
		final KnowIt condition;
		
		condition = askIt.getCondition();
		condition.process(this);
	}

	/**
	 * Constructs and adds a StoryProblem to the list of problems
	 * 
	 * @param component
	 */
	private void addProblem(final KnowIt component) {
		final String description = "Parameter is unbound";
		StoryProblem problem = new StoryProblem(component, description);
		this.problems.add(problem);
	}

	@Override
	public void processStoryComponentContainer(
			StoryComponentContainer storyComponentContainer) {
	}

	@Override
	public String toString() {
		return "ParameterBoundRule";
	}
}
