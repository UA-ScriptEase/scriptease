package scriptease.translator.codegenerator.code.contexts.storycomponent.complexstorycomponent.task;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import scriptease.model.StoryComponent;
import scriptease.model.complex.behaviours.Behaviour;
import scriptease.model.complex.behaviours.Task;
import scriptease.translator.codegenerator.code.contexts.Context;
import scriptease.translator.codegenerator.code.contexts.storycomponent.complexstorycomponent.ComplexStoryComponentContext;

public class TaskContext extends ComplexStoryComponentContext {
	private static List<Task> currentChildren = null;

	public TaskContext(Context other, Task source) {
		super(other, source);
	}

	@Override
	public Task getComponent() {

		return (Task) super.getComponent();
	};

	@Override
	public Collection<Task> getTaskChildren() {
		final List<Task> children;

		children = new ArrayList<Task>(this.getComponent().getSuccessors());

		Collections.sort(children);

		currentChildren = children;

		return children;
	}

	@Override
	public String getProbabilityCount() {
		int totalProbability = 0;

		for (Task child : this.getTaskChildren()) {
			totalProbability += child.getChance();
		}

		return "" + totalProbability;
	}

	@Override
	public boolean isLastTask() {
		return super.isLastTask();
	}

	@Override
	public String getUniqueID() {
		return "task" + this.getComponent().getUniqueID();
	}

	@Override
	public String getTaskProbabilityLowerBound() {
		if (currentChildren == null)
			return null;

		int lowerBound = 0;
		final Task thisTask = this.getComponent();

		// Loop through every child before this one and add up their
		// probabilities.
		for (Task child : currentChildren) {
			if (child == thisTask)
				break;

			lowerBound += child.getChance();
		}

		return Integer.toString(lowerBound);
	}

	@Override
	public String getTaskProbabilityUpperBound() {
		if (currentChildren == null)
			return null;

		final int lowerBound = Integer.parseInt(this
				.getTaskProbabilityLowerBound());

		return Integer.toString(this.getComponent().getChance() + lowerBound);
	}

	@Override
	public Behaviour getBehaviour() {
		StoryComponent owner = this.getOwner();

		while (owner != null)
			if (owner instanceof Behaviour)
				return (Behaviour) owner;
			else
				owner = owner.getOwner();

		return null;
	}
}
