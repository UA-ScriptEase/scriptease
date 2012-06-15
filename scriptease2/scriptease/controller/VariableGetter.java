package scriptease.controller;

import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.knowitbindings.KnowItBindingFunction;
import scriptease.model.atomic.knowitbindings.KnowItBindingReference;
import scriptease.model.complex.AskIt;
import scriptease.model.complex.ScriptIt;

public class VariableGetter extends TypeGetterVisitor<KnowIt> {
	/**
	 * Get dependant variables. Process bindings before processing dependant
	 * KnowIts, so the order of resolution is correct
	 */
	@Override
	public void processKnowIt(KnowIt knowIt) {
		knowIt.getBinding().process(new AbstractNoOpBindingVisitor() {

			@Override
			public void processReference(KnowItBindingReference reference) {
				// implicits
				// final KnowIt value = reference.getValue();
				// value.getBinding().process(this); 
				// if (!objects.contains(value))
				// objects.add(value);
			}

			@Override
			public void processFunction(KnowItBindingFunction function) {
				final ScriptIt referenced = function.getValue();

				for (KnowIt parameter : referenced.getParameters()) {
					parameter.getBinding().process(this);
				//	if (!objects.contains(parameter))
						objects.add(parameter);
				}
			}
		});
		//if (!objects.contains(knowIt))
			objects.add(knowIt);
	}

	@Override
	public void processAskIt(AskIt questionIt) {
		KnowIt condition = questionIt.getCondition();
		objects.add(condition);
	}

	@Override
	public void processScriptIt(ScriptIt scriptIt) {
		scriptIt.processParameters(this);
	}
}
