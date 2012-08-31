package scriptease.controller.get;

import scriptease.model.StoryComponent;
import scriptease.model.complex.AskIt;
import scriptease.model.complex.ScriptIt;

public class EffectGetter extends TypeGetterVisitor<StoryComponent> {
	@Override
	public void processAskIt(AskIt askIt) {
		this.objects.add(askIt);
	}

	@Override
	public void processScriptIt(ScriptIt doIt) {
		this.objects.add(doIt);
	}
}
