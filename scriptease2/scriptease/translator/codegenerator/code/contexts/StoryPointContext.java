package scriptease.translator.codegenerator.code.contexts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Pattern;

import scriptease.model.StoryComponent;
import scriptease.model.complex.StoryPoint;
import scriptease.translator.codegenerator.code.fragments.AbstractFragment;

/**
 * Context representing a StoryPoint
 * 
 * @author mfchurch
 * @author kschenk
 * 
 */
public class StoryPointContext extends ComplexStoryComponentContext {

	/**
	 * Creates a new StoryPointContext with a previous context and the
	 * {@link StoryPoint} source.
	 * 
	 * @param other
	 * @param source
	 */
	public StoryPointContext(Context other, StoryPoint source) {
		super(other, source);
	}

	@Override
	public Collection<StoryPoint> getStoryPointChildren() {
		return this.getComponent().getSuccessors();
	}

	@Override
	public Collection<StoryPoint> getStoryPointParents() {
		final Collection<StoryPoint> parents;

		parents = new ArrayList<StoryPoint>();

		for (StoryPoint point : this.getStartStoryPoint().getDescendants()) {
			if (point.getSuccessors().contains(this.getComponent())) {
				parents.add(point);
			}
		}

		return parents;
	}

	@Override
	public String getName() {
		return this.getNameOf(this.getComponent());
	}

	@Override
	public String getNameOf(StoryComponent component) {
		return this.getNamifier().getUniqueName(component, null);
	}

	@Override
	public String getUniqueID() {
		return this.getComponent().getUniqueID().toString();
	}

	@Override
	public String getUnique32CharName() {
		return this.getComponent().getUnique32CharName();
	}

	@Override
	public String getUniqueName(Pattern legalFormat) {
		return this.getNamifier().getUniqueName(this.getComponent(),
				legalFormat);
	}

	@Override
	public String getFormattedValue() {
		final Collection<AbstractFragment> typeFormat;

		typeFormat = this.translator.getGameTypeManager().getFormat(
				StoryPoint.STORY_POINT_TYPE);
		if (typeFormat == null || typeFormat.isEmpty())
			return this.getValue();

		return AbstractFragment.resolveFormat(typeFormat, this);
	}

	@Override
	public String getValue() {
		return this.getName();
	}

	@Override
	public String getFanIn() {
		return this.getComponent().getFanIn().toString();
	}

	@Override
	public StoryPoint getComponent() {
		return (StoryPoint) super.getComponent();
	}
}
