package scriptease.model.complex;

import java.util.Collection;
import java.util.HashSet;

import scriptease.controller.StoryVisitor;

/**
 * A story group represents a collection of StoryNodes {@link StoryNode} using
 * the Composite Design Pattern. In other words, a story group is a collection
 * of story points {@link StoryPoint} and/or other story groups.
 * 
 * @author jyuen
 */
public class StoryGroup extends StoryNode {

	public static final String STORY_GROUP_TYPE = "storyGroup";

	private static final String NEW_STORY_GROUP = "New Story Group";

	private StoryNode startNode;
	private StoryNode exitNode;

	public StoryGroup(String name, StoryNode startNode, StoryNode exitNode) {
		this(name, new HashSet<StoryNode>(), startNode, exitNode);
	}

	public StoryGroup(String name, Collection<StoryNode> storyNodes,
			StoryNode startNode, StoryNode exitNode) {
		super();

		this.startNode = startNode;
		this.exitNode = exitNode;

		for (StoryNode storyNode : storyNodes) {
			this.addStoryChild(storyNode);
		}

		this.successors = new HashSet<StoryNode>();
		this.parents = new HashSet<StoryNode>();
		this.uniqueID = this.getNextStoryNodeCounter();

		if (!this.getChildren().contains(this.startNode)
				|| !this.getChildren().contains(this.exitNode))
			throw new IllegalStateException(
					"The start node and the exit node must be a part of the story group!");

		this.registerChildType(StoryPoint.class,
				ComplexStoryComponent.MAX_NUM_OF_ONE_TYPE);
		this.registerChildType(StoryGroup.class,
				ComplexStoryComponent.MAX_NUM_OF_ONE_TYPE);

		if (name.equals("") || name == null) {
			name = StoryGroup.NEW_STORY_GROUP;
		}

		this.setDisplayText(name);
	}

	public StoryNode getStartNode() {
		return this.startNode;
	}

	public StoryNode getExitNode() {
		return this.exitNode;
	}

	public void setStartNode(StoryNode startNode) {
		this.startNode = startNode;
	}

	public void setExitNode(StoryNode exitNode) {
		this.exitNode = exitNode;
	}

	@Override
	public StoryGroup clone() {
		return (StoryGroup) super.clone();
	}

	@Override
	public String toString() {
		return "StoryGroup (\"" + this.getDisplayText() + "\")";
	}

	@Override
	public void process(StoryVisitor visitor) {
		visitor.processStoryGroup(this);
	}

	@Override
	public void addSuccessors(Collection<StoryNode> successors) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean addSuccessor(StoryNode successor) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean removeSuccessor(StoryNode successor) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected <E extends Collection<StoryNode>> E addDescendants(E descendants) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public StoryGroup shallowClone() {
		final StoryGroup clone = this.clone();

		clone.uniqueID = this.uniqueID;
		clone.successors = new HashSet<StoryNode>();
		clone.parents = new HashSet<StoryNode>();

		return clone;
	}
}
