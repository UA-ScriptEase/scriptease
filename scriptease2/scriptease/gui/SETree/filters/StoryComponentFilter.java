package scriptease.gui.SETree.filters;

import scriptease.translator.io.model.GameConstant;

/**
 * Superclass for all filters that are capable of filtering StoryComponents.
 * 
 * @author mfchurch
 * @author remiller
 */
public abstract class StoryComponentFilter extends Filter {
	@Override
	public void addRule(Filter newRule) {
		if (newRule instanceof StoryComponentFilter) {
			super.addRule(newRule);
		} else if (newRule != null)
			System.err
					.println("Can only update a StoryComponentFilter with another StoryComponentFilter");
	}

	@Override
	public String toString() {
		return "StoryComponentFilter" + super.toString();
	}

	@Override
	protected int getMatchCount(GameConstant element) {
		return 0;
	}
}
