package scriptease.translator.codegenerator.code.contexts.storycomponent.complexstorycomponent.scriptit;

import scriptease.model.complex.ScriptIt;
import scriptease.model.complex.behaviours.Behaviour;
import scriptease.translator.codegenerator.code.contexts.Context;

public class BehaviourContext extends ScriptItContext {

	public BehaviourContext(Context other, ScriptIt source) {
		super(other, source);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Behaviour getComponent() {
		return (Behaviour) super.getComponent();
	}
}
