package scriptease.gui.SEGraph;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import scriptease.model.StoryComponent;
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
	public StoryPoint createNewNode(StoryPoint node) {
		final StoryPoint newPoint = (StoryPoint) node.clone();
		
		newPoint.setOwner(null);
		newPoint.setFanIn(1);
		
		for(StoryPoint successor : node.getSuccessors()) {
			newPoint.removeSuccessor(successor);
		}

		return newPoint;
	}

	@Override
	public boolean addChild(StoryPoint child, StoryPoint existingNode) {
		if (existingNode.addSuccessor(child)) {
			child.setFanIn(child.getFanIn());
			return true;
		}
		return false;
	}

	@Override
	public boolean removeChild(StoryPoint child, StoryPoint existingNode) {
		if (existingNode.removeSuccessor(child)) {
			final int initialFanIn;

			initialFanIn = child.getFanIn();

			if (initialFanIn > 1)
				child.setFanIn(initialFanIn - 1);

			return true;
		}
		return false;
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
