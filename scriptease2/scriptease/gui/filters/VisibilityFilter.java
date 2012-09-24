package scriptease.gui.filters;

import scriptease.model.StoryComponent;

/**
 * VisibilityFilter simple checks with the VisibilityManager to verify if the
 * component is acceptable.
 * 
 * @author mfchurch
 * @author kschenk
 */
public class VisibilityFilter extends StoryComponentFilter {
	private boolean hideInvisible;

	public VisibilityFilter(boolean hideInvisible) {
		this.hideInvisible = hideInvisible;
	}

	@Override
	public int getMatchCount(StoryComponent component) {
		if (this.hideInvisible)
			return component.isVisible() ? 1 : 0;
		else
			return 1;
	}

	@Override
	public void addRule(Filter newFilter) {
		if (newFilter instanceof VisibilityFilter) {
			this.hideInvisible = ((VisibilityFilter) newFilter).hideInvisible;
		} else
			super.addRule(newFilter);
	}
}
