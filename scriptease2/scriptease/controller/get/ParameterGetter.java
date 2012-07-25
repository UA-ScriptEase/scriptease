package scriptease.controller.get;

import scriptease.model.atomic.KnowIt;
import scriptease.model.complex.ScriptIt;

public final class ParameterGetter extends TypeGetterVisitor<KnowIt> {

	@Override
	public void processScriptIt(ScriptIt doIt) {
		this.objects.addAll(doIt.getParameters());
	}

	@Override
	public void processKnowIt(KnowIt knowIt) {
		this.objects.add(knowIt);
	}
}
