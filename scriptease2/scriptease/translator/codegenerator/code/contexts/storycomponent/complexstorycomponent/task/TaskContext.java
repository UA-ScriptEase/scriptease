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
import scriptease.util.ListOp;

public class TaskContext extends ComplexStoryComponentContext {
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
	public String getTaskProbabilityUpperBound() {
		return Integer.toString(this.getComponent().getChance());
	}

	@Override
	public String getFirstTaskProbabilityUpperBound() {
		final Task firstChild = ListOp.head(this.getTaskChildren());

		if (firstChild != null)
			return Integer.toString(firstChild.getChance());

		return "-1";
	}

	@Override
	public boolean hasMultipleChildren() {
		return this.getComponent().getSuccessors().size() > 1;
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
