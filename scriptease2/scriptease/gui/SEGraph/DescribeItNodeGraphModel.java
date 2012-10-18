package scriptease.gui.SEGraph;

import java.util.Collection;

import scriptease.model.atomic.describeits.DescribeItNode;

public class DescribeItNodeGraphModel extends SEGraphModel<DescribeItNode>{

	public DescribeItNodeGraphModel(DescribeItNode start) {
		super(start);
		// TODO Auto-generated constructor stub
	}

	@Override
	public DescribeItNode createNewNode() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean addChild(DescribeItNode child, DescribeItNode existingNode) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean removeChild(DescribeItNode child, DescribeItNode existingNode) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Collection<DescribeItNode> getChildren(DescribeItNode node) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<DescribeItNode> getParents(DescribeItNode node) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean overwriteNodeData(DescribeItNode existingNode,
			DescribeItNode node) {
		// TODO Auto-generated method stub
		return false;
	}

}
