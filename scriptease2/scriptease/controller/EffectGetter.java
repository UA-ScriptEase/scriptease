package scriptease.controller;

import scriptease.model.StoryComponent;
import scriptease.model.complex.AskIt;
import scriptease.model.complex.ScriptIt;

public class EffectGetter extends TypeGetterVisitor<StoryComponent> {
	@Override
	public void processAskIt(AskIt askIt) {
		objects.add(askIt);
	}

	@Override
	public void processScriptIt(ScriptIt doIt) {
		objects.add(doIt);
	}
}
