package scriptease.controller.modelverifier.problem;

import scriptease.model.StoryComponent;

/**
 * A StoryProblem represents a problem identified in the story by a StoryRule.
 * It contains the problemed component, and a description of the solution.
 * Notify specifies if the user should be notified about the problem (Defaults
 * to true).
 * 
 * @author mfchurch
 * 
 */
public class StoryProblem {
	protected StoryComponent component;
	protected String description;
	protected boolean notify;

	public StoryProblem(StoryComponent component, String description) {
		this.component = component;
		this.description = description;
		this.notify = true;
	}

	public StoryComponent getComponent() {
		return this.component;
	}

	public String getDescription() {
		return this.description;
	}

	public void setNotify(boolean shouldNotify) {
		this.notify = shouldNotify;
	}

	public boolean shouldNotify() {
		return this.notify;
	}

	@Override
	public String toString() {
		return "StoryProblem[" + this.component + " : " + this.description
				+ "]";
	}
}
