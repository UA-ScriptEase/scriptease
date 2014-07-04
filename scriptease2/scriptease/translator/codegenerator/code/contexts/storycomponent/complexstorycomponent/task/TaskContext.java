package scriptease.translator.codegenerator.code.contexts.storycomponent.complexstorycomponent.task;

import scriptease.model.complex.behaviours.Task;
import scriptease.translator.codegenerator.code.contexts.Context;
import scriptease.translator.codegenerator.code.contexts.storycomponent.complexstorycomponent.ComplexStoryComponentContext;

public class TaskContext extends ComplexStoryComponentContext {
	public TaskContext(Context other, Task source) {
		super(other, source);
	}

	@Override
	public Task getComponent() {

		return (Task) super.getComponent();
	};
}
