package scriptease.controller.modelverifier.rule;

import java.util.Collection;

import scriptease.controller.modelverifier.problem.StoryProblem;
import scriptease.model.StoryComponent;
import scriptease.model.complex.ComplexStoryComponent;

/**
 * A StoryRule is used with ModelVerifier to identify problems in
 * the model. When a StoryRule's validModel method is called, it returns
 * ModelProblems for each component which does not follow the StoryRule's
 * criteria.
 * 
 * @author mfchurch
 * 
 */
public interface StoryRule {

	/**
	 * Notifies the StoryRule of a change in the model (given by root).
	 * StoryRule then validates the model, and returns any StoryComponent's
	 * which are not correct according to it's heuristic.
	 * 
	 * @return - Map of StoryComponents which have failed the rule's check, to
	 *         Strings which describe the problem.
	 */
	public Collection<StoryProblem> validate(ComplexStoryComponent root,
			StoryComponent source);
}
