package scriptease.translator.codegenerator.code.contexts;

import java.util.Collection;

import scriptease.model.complex.StoryGroup;
import scriptease.model.complex.StoryPoint;
import scriptease.translator.codegenerator.code.fragments.AbstractFragment;

/**
 * Context representing a StoryGroup
 * 
 * @author jyuen
 */
public class StoryGroupContext extends StoryNodeContext {

	/**
	 * Creates a new StoryGroupContext with a previous context and the
	 * {@link StoryGroup} source.
	 * 
	 * @param other
	 * @param source
	 */
	public StoryGroupContext(Context other, StoryPoint source) {
		super(other, source);
	}

	@Override
	public String getFormattedValue() {
		final Collection<AbstractFragment> typeFormat;

		typeFormat = this.getTranslator().getLibrary()
				.getType(StoryGroup.STORY_GROUP_TYPE).getFormat();
		if (typeFormat == null || typeFormat.isEmpty())
			return this.getValue();

		return AbstractFragment.resolveFormat(typeFormat, this);
	}

	@Override
	public StoryGroup getComponent() {
		return (StoryGroup) super.getComponent();
	}
}
