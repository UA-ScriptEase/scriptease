package scriptease.controller.get;

import scriptease.model.complex.ComplexStoryComponent;
import scriptease.model.complex.ScriptIt;

public class ScriptItGetter extends TypeGetterVisitor<ScriptIt> {

	@Override
	public void processScriptIt(ScriptIt doIt) {
		objects.add(doIt);
	}

	@Override
	protected void defaultProcessComplex(ComplexStoryComponent complex) {
	}

}
