package scriptease.gui.SEGraph;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import scriptease.model.atomic.describeits.DescribeItNode;

public class DescribeItNodeGraphModel extends SEGraphModel<DescribeItNode> {

	public DescribeItNodeGraphModel(DescribeItNode start) {
		super(start);
	}

	@Override
	public DescribeItNode createNewNode() {
		return new DescribeItNode("", null);
	}

	@Override
	public boolean addChild(DescribeItNode child, DescribeItNode existingNode) {
		return existingNode.addSuccessor(child);
	}

	@Override
	public boolean removeChild(DescribeItNode child, DescribeItNode existingNode) {
		return existingNode.removeSuccessor(child);
	}

	@Override
	public Collection<DescribeItNode> getChildren(DescribeItNode node) {
		return node.getSuccessors();
	}

	@Override
	public Collection<DescribeItNode> getParents(DescribeItNode node) {
		final Set<DescribeItNode> parents;

		parents = new HashSet<DescribeItNode>();

		for (DescribeItNode storyPoint : this.getNodes()) {
			for (DescribeItNode successor : storyPoint.getSuccessors())
				if (successor == node) {
					parents.add(storyPoint);
				}
		}
		return parents;
	}

	@Override
	public boolean overwriteNodeData(DescribeItNode existingNode,
			DescribeItNode node) {
		if (existingNode == node)
			return false;

		existingNode.setName(node.getName());
		existingNode.setKnowIt(node.getKnowIt());

		return true;
	}
}
