package scriptease.controller.modelverifier.problem;

import scriptease.model.StoryComponent;

/**
 * A ModelProblem represents a problem identified in the model. It extends the
 * StoryProblem class by adding a runnable solution to the problem.
 * ModelProblem's also have a priority which specifies in which order the
 * problems should be dealt with (higher means dealt with first).
 * 
 * @author mfchurch
 * 
 */
public class ModelProblem extends StoryProblem {
	private Runnable solution;
	private int priority;

	public ModelProblem(StoryComponent component, String description,
			Runnable solution, int priority) {
		super(component, description);
		this.solution = solution;
		this.priority = priority;
	}

	public int getPriority() {
		return this.priority;
	}

	public void solve() {
		this.solution.run();
	}

	@Override
	public String toString() {
		return "ModelProblem[" + this.component + " : " + this.description
				+ "]";
	}
}
