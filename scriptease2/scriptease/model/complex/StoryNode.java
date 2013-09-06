package scriptease.model.complex;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import scriptease.model.StoryComponent;

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

	/**
	 * Clones the current story node with no successors and parents and retains
	 * the existing unique id. Unless you plan on deleting the existing
	 * StoryNode, you should use the <code>clone</code> method or possibly run
	 * into many issues.
	 * 
	 * @return
	 */
	public abstract StoryNode shallowClone();
	
	public abstract void addSuccessors(Collection<StoryNode> successors);

	public abstract boolean addSuccessor(StoryNode successor);

	public abstract boolean removeSuccessor(StoryNode successor);

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
	protected abstract <E extends Collection<StoryNode>> E addDescendants(
			E descendants);

	/**
	 * Gets all descendants of the StoryNode in an unordered set, including the
	 * StoryNode itself. That is, the successors, the successors of the
	 * successors, etc. This is computationally expensive, and should thus be
	 * used carefully. Try to cache the descendants somewhere if they need to be
	 * accessed more than once. If order matters, use
	 * {@link #getOrderedDescendants()}.
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
