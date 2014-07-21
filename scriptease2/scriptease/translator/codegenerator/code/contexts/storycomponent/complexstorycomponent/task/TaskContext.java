package scriptease.translator.codegenerator.code.contexts.storycomponent.complexstorycomponent.task;

import java.util.Collection;

import scriptease.model.CodeBlock;
import scriptease.model.StoryComponent;
import scriptease.model.complex.behaviours.Behaviour;
import scriptease.model.complex.behaviours.Task;
import scriptease.translator.codegenerator.code.contexts.Context;
import scriptease.translator.codegenerator.code.contexts.storycomponent.complexstorycomponent.ComplexStoryComponentContext;

public class TaskContext extends ComplexStoryComponentContext {
	private static int uniqueIDCounter = 0;
	private final int uniqueID;

	public TaskContext(Context other, Task source) {
		super(other, source);
		this.uniqueID = uniqueIDCounter++;
	}

	@Override
	public Task getComponent() {

		return (Task) super.getComponent();
	};

	@Override
	public Collection<Task> getTaskChildren() {
		return this.getComponent().getSuccessors();
	}

	@Override
	public String getProbabilityCount() {
		return "" + this.getTaskChildren().size();
	}

	@Override
	public String getUniqueID() {
		return "" + uniqueID;
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
