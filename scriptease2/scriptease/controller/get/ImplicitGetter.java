package scriptease.controller.get;

import scriptease.controller.BindingAdapter;
import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.knowitbindings.KnowItBindingReference;
import scriptease.model.complex.ScriptIt;

public class ImplicitGetter extends TypeGetterVisitor<KnowIt> {
	/**
	 * Get implicit variables. Process bindings before processing dependant
	 * KnowIts, so the order of resolution is correct
	 */
	@Override
	public void processKnowIt(KnowIt knowIt) {
		knowIt.getBinding().process(new BindingAdapter() {

			@Override
			public void processReference(KnowItBindingReference reference) {
				// implicits
				final KnowIt value = reference.getValue();
				value.getBinding().process(this);
				if (!ImplicitGetter.this.objects.contains(value))
					ImplicitGetter.this.objects.add(value);
			}
		});
	}

	@Override
	public void processScriptIt(ScriptIt scriptIt) {
		scriptIt.processParameters(this);
	}
}
