package scriptease.model.complex.storygraph;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import scriptease.controller.StoryVisitor;
import scriptease.model.complex.ComplexStoryComponent;

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

	private Set<StoryNode> storyNodes;
	private StoryNode startNode;
	private StoryNode exitNode;

	public StoryGroup(String name, Set<StoryNode> storyNodes,
			StoryNode startNode, StoryNode exitNode) {
		super();

		this.storyNodes = storyNodes;
		this.startNode = startNode;
		this.exitNode = exitNode;

		this.successors = new HashSet<StoryNode>();
		this.parents = new HashSet<StoryNode>();
		this.uniqueID = this.getNextStoryNodeCounter();

		if (!this.storyNodes.contains(this.startNode)
				|| !this.storyNodes.contains(this.exitNode))
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

	public Set<StoryNode> getStoryNodes() {
		return this.storyNodes;
	}

	public StoryNode getStartNode() {
		return this.startNode;
	}

	public StoryNode getExitNode() {
		return this.exitNode;
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
	public StoryNode shallowClone() {
		// TODO Auto-generated method stub
		return null;
	}
}
