package scriptease.gui.filters;

import scriptease.model.StoryComponent;

public abstract class GameConstantFilter extends Filter {
	@Override
	public void addRule(Filter newRule) {
		if (newRule instanceof GameConstantFilter)
			super.addRule(newRule);
		else if (newRule != null)
			System.err
					.println("Can only update a GameObjectFilter with another GameObjectFilter");
	}

	@Override
	protected int getMatchCount(StoryComponent element) {
		return 0;
	}

	@Override
	public String toString() {
		return "GameObjectFilter" + super.toString();
	}
}
