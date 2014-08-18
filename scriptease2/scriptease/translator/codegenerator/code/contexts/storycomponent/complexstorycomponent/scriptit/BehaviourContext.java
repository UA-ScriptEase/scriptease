package scriptease.translator.codegenerator.code.contexts.storycomponent.complexstorycomponent.scriptit;

import java.util.Collection;

import scriptease.model.complex.ScriptIt;
import scriptease.model.complex.behaviours.Behaviour;
import scriptease.model.complex.behaviours.Task;
import scriptease.translator.codegenerator.code.contexts.Context;

public class BehaviourContext extends ScriptItContext {

	public BehaviourContext(Context other, ScriptIt source) {
		super(other, source);
	}

	@Override
	public Behaviour getComponent() {
		return (Behaviour) super.getComponent();
	}

	@Override
	public Task getStartTask() {
		return this.getComponent().getStartTask();
	}

	@Override
	public Collection<Task> getTasks() {
		return this.getStartTask().getDescendants();
	}

	@Override
	public String getPriority() {
		return String.valueOf(this.getComponent().getPriority());
	}

	@Override
	public String getUniqueID() {
		return String.valueOf(this.getComponent().getUniqueID());
	}
}
