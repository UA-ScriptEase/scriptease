package scriptease.gui.SEGraph;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import scriptease.model.complex.StoryPoint;

public class StoryPointGraphModel extends SEGraphModel<StoryPoint> {
	public StoryPointGraphModel(StoryPoint start) {
		super(start);
	}

	@Override
	public StoryPoint createNewNode() {
		return new StoryPoint("");
	}

	@Override
	public void addChild(StoryPoint child, StoryPoint existingNode) {
		existingNode.addSuccessor(child);
	}

	@Override
	public void removeChild(StoryPoint child, StoryPoint existingNode) {
		final int initialFanIn;

		initialFanIn = child.getFanIn();

		if (initialFanIn > 1)
			child.setFanIn(initialFanIn - 1);

		existingNode.removeSuccessor(child);
	}

	@Override
	public Collection<StoryPoint> getChildren(StoryPoint node) {
		return node.getSuccessors();
	}

	@Override
	public Collection<StoryPoint> getParents(StoryPoint node) {
		final Set<StoryPoint> parents;

		parents = new HashSet<StoryPoint>();

		for (StoryPoint storyPoint : this.getNodes()) {
			for (StoryPoint successor : storyPoint.getSuccessors())
				if (successor == node) {
					parents.add(storyPoint);
				}
		}
		return parents;
	}

}
