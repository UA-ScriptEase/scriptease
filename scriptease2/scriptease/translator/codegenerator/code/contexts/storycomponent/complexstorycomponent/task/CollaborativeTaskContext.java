package scriptease.translator.codegenerator.code.contexts.storycomponent.complexstorycomponent.task;

import scriptease.model.complex.behaviours.CollaborativeTask;
import scriptease.model.complex.behaviours.IndependentTask;
import scriptease.translator.codegenerator.code.contexts.Context;

public class CollaborativeTaskContext extends TaskContext {

	public CollaborativeTaskContext(Context other, CollaborativeTask task) {
		super(other, task);
	}

	@Override
	public IndependentTask getComponent() {
		return (IndependentTask) super.getComponent();
	}
}
