package scriptease.controller.observer.graph;

import scriptease.gui.quests.StoryPoint;

/**
 * Observer for graphs that acts on Story Points when they have children or
 * parents added or removed from them in the Graph.
 * 
 * @author kschenk
 * 
 */
public class StoryPointGraphObserver implements SEGraphObserver<StoryPoint> {

	private final StoryPoint start;

	/**
	 * Creates a new StoryPointGraphObserver with the root StoryPoint.
	 * 
	 * @param start
	 */
	public StoryPointGraphObserver(StoryPoint start) {
		this.start = start;
	}

	@Override
	public void childAdded(StoryPoint child, StoryPoint parent) {
		parent.addSuccessor(child);
	}

	@Override
	public void childRemoved(StoryPoint child, StoryPoint parent) {
		parent.removeSuccessor(child);
	}

	@Override
	public void parentAdded(StoryPoint child, StoryPoint parent) {
		for (StoryPoint descendant : this.start.getDescendants()) {
			for (StoryPoint successor : descendant.getSuccessors()) {
				if (successor == child) {
					parent.addSuccessor(child);
					return;
				}
			}
		}
	}

	@Override
	public void parentRemoved(StoryPoint child, StoryPoint parent) {
		for (StoryPoint descendant : this.start.getDescendants()) {
			for (StoryPoint successor : descendant.getSuccessors()) {
				if (successor == child) {
					parent.removeSuccessor(child);
					return;
				}
			}
		}
	}
}
