package scriptease.translator.codegenerator.code.contexts.knowitbindingcontext;

import java.util.Collection;

import scriptease.model.atomic.knowitbindings.KnowItBinding;
import scriptease.model.atomic.knowitbindings.KnowItBindingStoryPoint;
import scriptease.model.complex.StoryPoint;
import scriptease.translator.codegenerator.code.contexts.Context;
import scriptease.translator.codegenerator.code.contexts.ContextFactory;
import scriptease.translator.codegenerator.code.fragments.AbstractFragment;

/**
 * Code generation Context for a KnowItBindingStoryPoint object.
 * 
 * @see Context
 * @see KnowItBindingContext
 * @author remiller
 */
public class KnowItBindingStoryPointContext extends KnowItBindingContext {
	public KnowItBindingStoryPointContext(Context other, KnowItBinding source) {
		super(other, source);
	}

	@Override
	public String getUnique32CharName() {
		final StoryPoint qp = ((KnowItBindingStoryPoint) this.binding)
				.getValue();

		return qp.getUnique32CharName();
	}

	@Override
	public String getFormattedValue() {
		final Collection<AbstractFragment> typeFormat;

		typeFormat = this.getTranslator().getLibrary()
				.getType(StoryPoint.STORY_POINT_TYPE).getFormat();
		if (typeFormat == null || typeFormat.isEmpty())
			return this.getValue();

		return AbstractFragment.resolveFormat(typeFormat, this);
	}

	/**
	 * Get the KnowItBinding's StoryPoint Name
	 */
	@Override
	public String getValue() {
		final StoryPoint qp = ((KnowItBindingStoryPoint) this.binding)
				.getValue();
		final Context knowItContext;

		knowItContext = ContextFactory.getInstance().createContext(this, qp);

		return knowItContext.getName();
	}
}