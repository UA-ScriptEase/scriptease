package scriptease.gui.SEGraph.models;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import scriptease.model.StoryComponent;
import scriptease.model.complex.StoryPoint;

/**
 * A graph model for Story Points. Shows them with binding widgets and fan in
 * spinners.
 * 
 * @author kschenk
 * 
 */
public class StoryPointGraphModel extends SEGraphModel<StoryPoint> {
	public StoryPointGraphModel(StoryPoint start) {
		super(start);
	}

	@Override
	public StoryPoint createNewNode() {
		return new StoryPoint("");
	}

	@Override
	public boolean overwriteNodeData(StoryPoint existingNode, StoryPoint node) {
		if (existingNode == node)
			return false;

		existingNode.setDisplayText(node.getDisplayText());
		existingNode.removeStoryChildren(existingNode.getChildren());

		for (StoryComponent child : node.getChildren()) {
			existingNode.addStoryChild(child.clone());
		}

		return true;
	}

	@Override
	public boolean addChild(StoryPoint child, StoryPoint existingNode) {
		final boolean added = existingNode.addSuccessor(child);

		if (added) {
			child.setFanIn(child.getFanIn());
		}

		return added;
	}

	@Override
	public boolean removeChild(StoryPoint child, StoryPoint existingNode) {
		final boolean removed = existingNode.removeSuccessor(child);

		if (removed) {
			final int initialFanIn = child.getFanIn();

			if (initialFanIn > 1)
				child.setFanIn(initialFanIn - 1);
		}

		return removed;
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
