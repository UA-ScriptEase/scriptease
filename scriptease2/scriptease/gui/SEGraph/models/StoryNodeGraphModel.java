package scriptease.gui.SEGraph.models;

import java.util.Collection;
import java.util.List;

import scriptease.model.StoryComponent;
import scriptease.model.complex.StoryNode;
import scriptease.model.complex.StoryPoint;

/**
 * A graph model for Story Nodes.
 * 
 * Story Points are shown with binding widgets and fan in spinners. Story Groups
 * are shown with expand and collapse buttons.
 * 
 * @author kschenk
 * @author jyuen
 * 
 */
public class StoryNodeGraphModel extends SEGraphModel<StoryNode> {

	public StoryNodeGraphModel(StoryNode storyNode) {
		super(storyNode);
	}

	@Override
	public StoryNode createNewNode() {
		return new StoryPoint("");
	}

	@Override
	public boolean overwriteNodeData(StoryNode toNode, StoryNode fromNode) {
		if (toNode == fromNode)
			return false;

		final List<StoryComponent> children = toNode.getChildren();
		
		// remove existing children
		toNode.removeStoryChildren(children);
		
		// add new children
		for (StoryComponent child : fromNode.getChildren()) {
			if (!toNode.addStoryChild(child.clone())) {
				return false;
			}
		}

		return true;
	}

	@Override
	public boolean addChild(StoryNode child, StoryNode existingNode) {
		final boolean added = existingNode.addSuccessor(child);

		if (added && child instanceof StoryPoint) {
			((StoryPoint) child).setFanIn(((StoryPoint) child).getFanIn());
		}

		return added;
	}

	@Override
	public boolean removeChild(StoryNode child, StoryNode existingNode) {
		final boolean removed = existingNode.removeSuccessor(child);

		if (removed && child instanceof StoryPoint) {

			final int initialFanIn = ((StoryPoint) child).getFanIn();

			if (initialFanIn > 1)
				((StoryPoint) child).setFanIn(initialFanIn - 1);
		}

		return removed;
	}

	@Override
	public Collection<StoryNode> getChildren(StoryNode node) {
		return node.getSuccessors();
	}

	@Override
	public Collection<StoryNode> getParents(StoryNode node) {
		return node.getParents();
	}
}
