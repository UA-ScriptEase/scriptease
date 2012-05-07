package scriptease.gui.SETree.filters;

import scriptease.controller.VisibilityManager;
import scriptease.model.StoryComponent;

/**
 * VisibilityFilter simple checks with the VisibilityManager to verify if the
 * component is acceptable.
 * 
 * @author mfchurch
 */
public class VisibilityFilter extends StoryComponentFilter {
	@Override
	public int getMatchCount(StoryComponent component) {
		return VisibilityManager.getInstance().isVisible(component) ? 1 : 0;
	}

	@Override
	public void addRule(Filter newFilter) {
		/*
		 * Ignore incoming VisibilityFilters. We do nothing with them since
		 * there is nothing to update.
		 */
		if (!(newFilter instanceof VisibilityFilter)) {
			super.addRule(newFilter);
		}
	}
}
