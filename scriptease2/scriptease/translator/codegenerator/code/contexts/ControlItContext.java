package scriptease.translator.codegenerator.code.contexts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import scriptease.model.atomic.KnowIt;
import scriptease.model.complex.ControlIt;
import scriptease.model.complex.ScriptIt;
import scriptease.translator.codegenerator.CodeGenerationKeywordConstants.FormatReferenceType;
import scriptease.translator.codegenerator.code.fragments.FormatReferenceFragment;

public class ControlItContext extends ScriptItContext {

	public ControlItContext(Context other, ControlIt source) {
		super(other, source);
	}

	@Override
	public Iterator<KnowIt> getImplicits() {
		final ScriptIt cause;

		cause = this.getComponent().getMainCodeBlock().getCause();

		return cause.getImplicits().iterator();
	}

	@Override
	public Iterator<KnowIt> getParameters() {
		return this.getComponent().getRequiredParameters().iterator();
	}

	@Override
	public Iterator<KnowIt> getParametersWithSlot() {
		final Collection<KnowIt> parameters = new ArrayList<KnowIt>();

		parameters.addAll(this.getSlotParameterCollection());
		parameters.addAll(this.getComponent().getRequiredParameters());

		return parameters.iterator();
	}

	@Override
	public String getControlItFormat() {
		final String reference;
		final FormatReferenceFragment fragment;

		reference = this.getComponent().getFormat().name();
		fragment = new FormatReferenceFragment(reference,
				FormatReferenceType.CONTROLIT);

		return fragment.resolve(this);
	}

	@Override
	protected ControlIt getComponent() {
		return (ControlIt) super.getComponent();
	}
}
