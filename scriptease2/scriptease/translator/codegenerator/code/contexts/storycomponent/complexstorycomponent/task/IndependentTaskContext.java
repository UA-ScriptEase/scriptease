package scriptease.translator.codegenerator.code.contexts.storycomponent.complexstorycomponent.task;

import java.util.Collection;

import scriptease.model.CodeBlock;
import scriptease.model.atomic.KnowIt;
import scriptease.model.complex.behaviours.IndependentTask;
import scriptease.translator.codegenerator.code.contexts.Context;

public class IndependentTaskContext extends TaskContext {

	public IndependentTaskContext(Context other, IndependentTask source) {
		super(other, source);
	}

	@Override
	public IndependentTask getComponent() {
		return (IndependentTask) super.getComponent();
	}
	


}
