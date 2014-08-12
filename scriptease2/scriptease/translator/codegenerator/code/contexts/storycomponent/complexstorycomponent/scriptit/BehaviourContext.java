package scriptease.translator.codegenerator.code.contexts.storycomponent.complexstorycomponent.scriptit;

import java.util.Collection;

import scriptease.model.complex.ScriptIt;
import scriptease.model.complex.behaviours.Behaviour;
import scriptease.model.complex.behaviours.Task;
import scriptease.translator.codegenerator.code.contexts.Context;

public class BehaviourContext extends ScriptItContext {
	private static int uniqueIDCounter = 0;
	private final int uniqueID;

	public BehaviourContext(Context other, ScriptIt source) {
		super(other, source);
		this.uniqueID = uniqueIDCounter++;
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
	public String getProbabilityCount() {
		// TODO implement behaviour prob count
		return super.getProbabilityCount();
	}

	@Override
	public String getUniqueID() {
		return "" + uniqueID;
	}
}
