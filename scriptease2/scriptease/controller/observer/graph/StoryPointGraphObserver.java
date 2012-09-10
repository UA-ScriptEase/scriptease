package scriptease.controller.observer.graph;

import scriptease.gui.quests.StoryPoint;

/**
 * Observer for graphs that acts on Story Points when they have children or
 * parents added or removed from them in the Graph
 * 
 * @author kschenk
 * 
 */
public class StoryPointGraphObserver implements SEGraphObserver<StoryPoint> {
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
		parent.addSuccessor(child);
	}

	@Override
	public void parentRemoved(StoryPoint child, StoryPoint parent) {
		parent.removeSuccessor(child);
	}
}
