package scriptease.translator.codegenerator.code.contexts;

import java.util.Collection;

import scriptease.model.atomic.KnowIt;
import scriptease.model.complex.ControlIt;
import scriptease.model.complex.ControlIt.ControlItFormat;
import scriptease.translator.codegenerator.CodeGenerationKeywordConstants.FormatReferenceType;
import scriptease.translator.codegenerator.code.fragments.FormatReferenceFragment;

/**
 * Context for {@link ControlIt}s. This overrides some methods of
 * {@link ScriptItContext} that act differently for {@link ControlIt}s.
 * 
 * @author kschenk
 * 
 */
public class ControlItContext extends ScriptItContext {

	/**
	 * Creates a new {@link ControlItContext} from a previous Context and a
	 * source {@link ControlIt}.
	 * 
	 * @param other
	 * @param source
	 */
	public ControlItContext(Context other, ControlIt source) {
		super(other, source);
	}

	/**
	 * In {@link ScriptItContext}, only used implicits are returned. With
	 * ControlIts, we return all implicits.
	 */
	@Override
	public Collection<KnowIt> getImplicits() {
		return this.getCause().getImplicits();
	}

	/**
	 * ControlIts have some special parameters in them so they need to return
	 * those as well.
	 */
	@Override
	public Collection<KnowIt> getParameters() {
		return this.getComponent().getRequiredParameters();
	}

	/**
	 * A ControlIt has a special format in the Language Dictionary. This is one
	 * of {@link ControlItFormat}. This method returns the
	 * {@link ControlItFormat} as a String.
	 */
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
