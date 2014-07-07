package scriptease.model.complex;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import scriptease.controller.StoryVisitor;
import scriptease.controller.observer.storycomponent.StoryComponentEvent;
import scriptease.controller.observer.storycomponent.StoryComponentEvent.StoryComponentChangeEnum;
import scriptease.model.StoryComponent;
import scriptease.model.semodel.librarymodel.LibraryModel;

/**
 * A story node represents a node in the story graph. A story node can either
 * consist of a story point {@link StoryPoint} or a story group
 * {@link StoryGroup}.
 * 
 * @author jyuen
 */
public abstract class StoryNode extends ComplexStoryComponent {
	private static int storyNodeCounter = 1;

	/**
	 * StoryNodes must be unique. This uniqueID helps maintain uniqueness. It
	 * only gets saved to the model, not written to any files. So it must get
	 * generated whenever we create a new StoryNode.
	 */
	protected int uniqueID;

	protected Set<StoryNode> successors;
	protected Set<StoryNode> parents;

	public StoryNode() {
		super(LibraryModel.getNonLibrary());
	}

	/**
	 * Adds a successor to the StoryNode
	 * 
	 * @param successor
	 */
	public boolean addSuccessor(StoryNode successor) {
		if (successor != this && !successor.getSuccessors().contains(this)) {
			if (this.successors.add(successor)) {
				successor.parents.add(this);

				this.notifyObservers(new StoryComponentEvent(successor,
						StoryComponentChangeEnum.STORY_NODE_SUCCESSOR_ADDED));
				return true;
			}
		}
		return false;
	}

	/**
	 * Removes a successor from the StoryMpde
	 * 
	 * @param successor
	 */
	public boolean removeSuccessor(StoryNode successor) {
		if (this.successors.remove(successor)) {
			successor.parents.remove(this);

			this.notifyObservers(new StoryComponentEvent(successor,
					StoryComponentChangeEnum.STORY_NODE_SUCCESSOR_REMOVED));
			return true;
		}
		return false;
	}


	/**
	 * Adds the descendants to the passed in collection. You should use
	 * {@link #getDescendants()} or {@link #getOrderedDescendants()} instead of
	 * this method. Having this method separate lets us keep our descendant
	 * collecting code in one method for different types of collections. It also
	 * lets us avoid going through the graph multiple times in cases where we
	 * branch around.
	 * 
	 * @see #getDescendants()
	 * @see #getOrderedDescendants()
	 * @param descendants
	 *            The collection to add to and to return.
	 * @return The same collection that was passed in, but with descendants
	 *         added.
	 */
	protected <E extends Collection<StoryNode>> E addDescendants(E descendants) {
		descendants.add(this);

		for (StoryNode successor : this.getSuccessors()) {
			/*
			 * This check prevents us from going over paths twice, which saves a
			 * ton of time in complex stories. Note that the contains
			 * implementation in Sets is much faster, which is why
			 * getDescendants is faster than getOrderedDescendants.
			 */
			if (!descendants.contains(successor))
				successor.addDescendants(descendants);
		}

		return descendants;
	}

	/**
	 * Gets all descendants of the StoryNode in an unordered set, including the
	 * StoryNode itself. That is, the successors, the successors of the
	 * successors, etc. This is computationally expensive, and should thus be
	 * used carefully. Try to cache the descendants somewhere if they need to be
	 * accessed more than once. If order matters, use
	 * {@link #getOrderedDescendants()}. If you are trying to get all story
	 * point descendants, use {@link #getStoryPointDescendants}.
	 * 
	 * @see #getOrderedDescendants()
	 * @return An unordered set of the story node's descendants.
	 */
	public Set<StoryNode> getDescendants() {
		return this.addDescendants(new HashSet<StoryNode>());
	}

	/**
	 * Gets all descendants of the StoryNode, including the StoryNode itself.
	 * This is much slower than {@link #getDescendants()}, so it should be used
	 * sparingly.
	 * 
	 * @see #getDescendants()
	 * @return An ordered list of the story node's descendants.
	 */
	public List<StoryNode> getOrderedDescendants() {
		return this.addDescendants(new ArrayList<StoryNode>());
	}

	/**
	 * Gets all descendants of the StoryNode that are StoryPoints. If a
	 * StoryGroup descendant is encountered, all the StoryPoints in that group
	 * is added instead.
	 * 
	 * @return
	 */
	public Set<StoryPoint> getStoryPointDescendants() {
		final Set<StoryPoint> descendants = new HashSet<StoryPoint>();

		for (StoryNode descendant : this.getDescendants()) {
			if (descendant instanceof StoryPoint)
				descendants.add((StoryPoint) descendant);
			else if (descendant instanceof StoryGroup)
				descendants.addAll(((StoryGroup) descendant)
						.getAllStoryPoints());
		}

		return descendants;
	}

	/**
	 * Adds multiple successors to the StoryPoint.
	 * 
	 * @param successors
	 */
	public void addSuccessors(Collection<StoryNode> successors) {
		for (StoryNode successor : successors) {
			this.addSuccessor(successor);
		}
	}

	/**
	 * Gets a mapping of the depth of each StoryNode. The depth corresponds to
	 * the longest path it will take to get to the Story Node at the highest
	 * level. i.e. Story Groups and Story Points within the highest level Story
	 * Group will be ignored. This is very computationally expensive, so it
	 * should not be used too often.
	 * 
	 * @return
	 */
	public final Map<StoryNode, Integer> createDepthMap() {
		final Map<StoryNode, Integer> depthMap = new IdentityHashMap<StoryNode, Integer>();

		// Goes through every successor of the node
		for (StoryNode successor : this.getSuccessors()) {

			// Gets the depth map of every successor.
			final Map<StoryNode, Integer> childDepthMap = successor
					.createDepthMap();

			for (Entry<StoryNode, Integer> entry : childDepthMap.entrySet()) {
				final StoryNode childNode = entry.getKey();
				final Integer depth = entry.getValue() + 1;

				// If the node is already in the depthMap and the new depth is
				// greater, use the greater depth value.
				if (depthMap.containsKey(childNode)) {
					if (depth > depthMap.get(childNode))
						depthMap.put(childNode, depth);
				} else
					depthMap.put(childNode, depth);
			}
		}

		if (!depthMap.containsKey(this))
			depthMap.put(this, 0);

		return depthMap;
	}

	public int getNextStoryNodeCounter() {
		return StoryNode.storyNodeCounter++;
	}

	@Override
	public void revalidateKnowItBindings() {
		for (StoryComponent child : this.getChildren())
			child.revalidateKnowItBindings();
	}

	public Collection<StoryNode> getSuccessors() {
		return this.successors;
	}

	public Collection<StoryNode> getParents() {
		return this.parents;
	}

	@Override
	public void setEnabled(Boolean isDisabled) {
		// Do nothing - don't want to be able to disable story nodes
	}

	@Override
	public void process(StoryVisitor visitor) {
		visitor.processStoryNode(this);
	}

	/**
	 * Returns the unique ID. Unique IDs are generated on ScriptEase startup and
	 * not saved to file, so remember not to implement code that requires unique
	 * IDs to be persistent across different saves.
	 * 
	 * @return
	 */
	public Integer getUniqueID() {
		return this.uniqueID;
	}
}
