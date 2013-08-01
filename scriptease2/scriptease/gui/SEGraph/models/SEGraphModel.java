package scriptease.gui.SEGraph.models;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;

/**
 * Model class for SEGraph. This stores and handles all of the nodes in the
 * Graph. A model must be created for an SEGraph. <br>
 * <br>
 * Note that all classes used as nodes for a graph must implement the
 * .equals(Object) method, or else the graph will not draw correctly.
 * 
 * @author kschenk
 * 
 * @param <E>
 */
public abstract class SEGraphModel<E> {
	// The depthmap is cached so that large graphs do not constantly have to
	// calculate it. This was added in due to an existing performance issue.
	private final Map<E, Integer> depthMap;
	private final E start;

	/**
	 * Creates a new model for an SEGraph with the passed in node as the start
	 * node.
	 * 
	 * @param start
	 */
	public SEGraphModel(E start) {
		this.depthMap = new IdentityHashMap<E, Integer>();
		this.start = start;

		this.recalculateDepthMap();
	}

	/**
	 * Creates a new node.
	 * 
	 * @return the new node
	 */
	public abstract E createNewNode();

	/**
	 * Adds a child to an existing node.
	 * 
	 * @param child
	 *            The child to be added
	 * @param existingNode
	 *            The existing node that will receive the child
	 */
	public abstract boolean addChild(E child, E existingNode);

	/**
	 * Removes an existing child from an existing node.
	 * 
	 * @param child
	 *            The child to be removed
	 * @param existingNode
	 *            The existing node that the child will be removed from
	 */
	public abstract boolean removeChild(E child, E existingNode);

	/**
	 * Returns the children for the passed in node.
	 * 
	 * @param node
	 * @return
	 */
	public abstract Collection<E> getChildren(E node);

	/**
	 * Returns the parents for the passed in node.
	 * 
	 * @param node
	 * @return
	 */
	public abstract Collection<E> getParents(E node);

	/**
	 * Replaces the existing node with a new node. The new node passed in is
	 * cloned without any parents or successors.
	 * 
	 * @param existingNode
	 * @param newNode
	 * @return
	 */
	public abstract boolean overwriteNodeData(E existingNode, E node);

	/**
	 * Adds a node between two existing nodes. Order of the two nodes does not
	 * matter; this method figures out which node is above the other.
	 * 
	 * @param node
	 *            The new node.
	 * @param existingNode1
	 *            The first selected existing node.
	 * @param existingNode2
	 *            The second selected existing node.
	 * @return true if the addition was successful
	 */
	public final boolean addNodeBetween(E node, E firstNode, E secondNode) {
		final boolean added;

		if (firstNode == null || secondNode == null)
			added = false;
		else if (this.getDescendants(firstNode).contains(secondNode)) {
			this.addChild(node, firstNode);
			this.addChild(secondNode, node);
			added = true;
		} else if (this.getDescendants(secondNode).contains(firstNode)) {
			this.addChild(node, secondNode);
			this.addChild(firstNode, node);
			added = true;
		} else {
			final int firstNodeDepth = this.getDepthMap().get(firstNode);
			final int secondNodeDepth = this.getDepthMap().get(secondNode);

			if (firstNodeDepth > secondNodeDepth) {
				this.addChild(node, secondNode);
				this.addChild(firstNode, node);
			} else {
				this.addChild(node, firstNode);
				this.addChild(secondNode, node);
			}
			added = true;
		}

		if (added)
			this.recalculateDepthMap();

		return added;
	}

	/**
	 * Removes the passed in node from the graph.
	 * 
	 * @param node
	 *            The node to be removed.
	 */
	public final boolean removeNode(E node) {
		boolean removed = false;

		if (node == this.start)
			removed = false;
		else
			for (E nodeInModel : this.getNodes()) {
				if (nodeInModel == node) {
					// We need to work with copies to avoid concurrent
					// modifications
					final Collection<E> parentsCopy;

					parentsCopy = new HashSet<E>(this.getParents(nodeInModel));

					for (E parent : parentsCopy)
						this.removeChild(nodeInModel, parent);

					removed = true;
					break;
				}
			}

		if (removed)
			this.recalculateDepthMap();

		return removed;
	}

	/**
	 * Connects two nodes together if the child is further down the graph from
	 * the parent.
	 * 
	 * @param child
	 * @param parent
	 * @return True if the nodes were successfully connected.
	 */
	public final boolean connectNodes(E child, E parent) {
		final boolean connected;

		if (child == null || parent == null)
			connected = false;
		else if (this.getDescendants(child).contains(parent)) {
			connected = false;
		} else if (this.getDescendants(parent).contains(child)) {
			connected = this.addChild(child, parent);
		} else {
			final int childDepth = this.getDepthMap().get(child);
			final int parentDepth = this.getDepthMap().get(parent);

			if (childDepth >= parentDepth) {
				connected = this.addChild(child, parent);
			} else {
				connected = false;
			}
		}

		if (connected)
			this.recalculateDepthMap();

		return connected;
	}

	/**
	 * Disconnects two nodes. If the node had no other connections, this will
	 * result in a deletion.
	 * 
	 * @param child
	 * @param parent
	 * @return True if the nodes were successfully disconnected.
	 */
	public final boolean disconnectNodes(E child, E parent) {
		final boolean disconnected;

		if (child == null || parent == null)
			disconnected = false;
		else
			disconnected = this.removeChild(child, parent);

		if (disconnected)
			this.recalculateDepthMap();

		return disconnected;
	}

	/**
	 * Returns all nodes after and including the start node.
	 * 
	 * @return All nodes in the graph.
	 */
	public final Collection<E> getNodes() {
		final Collection<E> nodes;
		nodes = new HashSet<E>();

		nodes.add(this.start);
		nodes.addAll(this.getDescendants(this.start));

		return nodes;
	}

	/**
	 * Returns all ancestors for the passed in node, with ancestors being the
	 * node's parents, the parents' parents, and so forth.
	 * 
	 * @param node
	 * @return
	 */
	public final Set<E> getAncestors(E node) {
		final Set<E> ancestors;
		ancestors = new HashSet<E>();

		for (E parent : this.getParents(node)) {
			ancestors.add(parent);
			ancestors.addAll(this.getAncestors(parent));
		}

		return ancestors;
	}

	/**
	 * Returns all descendants for the passed in node, with descendants being
	 * the node's children, the children's children, and so forth.
	 * 
	 * @param node
	 * @return
	 */
	public final Set<E> getDescendants(E node) {
		final Set<E> descendants;
		descendants = new HashSet<E>();

		for (E child : this.getChildren(node)) {
			descendants.add(child);
			descendants.addAll(this.getDescendants(child));
		}

		return descendants;
	}

	/**
	 * Checks whether the node is a valid group start
	 * 
	 * @param node
	 */
	public boolean isValidGroupStart(E node) {
		final Queue<E> nodes = new LinkedList<E>();

		boolean notDone = true;
		boolean result = false;

		for (E child : this.getChildren(node)) {
			nodes.add(child);
		}

		while (notDone && nodes.size() >= 1) {
			final E currentNode = nodes.poll();
			
			Set<E> group = new HashSet<E>();

			// Must always include start in set
			group.add(node);
			group = this.findGroupPaths(currentNode, group, null);

			result = this.isGroup(group);

			if (result) {
				notDone = false;
			} else {
				for (E temp : this.getChildren(currentNode)) {
					nodes.add(temp);
				}
			}
		}

		return result;
	}

	/**
	 * A nodes should be visited when: 1. All of it's children are visited. 2.
	 * It's been used to find a path to the goal.
	 * 
	 * Retreat once we find a child that is either the end or part of the group.
	 * Retreat when there are no children. When retreating, go back one, see if
	 * there are other children to visit.
	 * 
	 * @param endNode
	 * @param group
	 * @return
	 */
	public Set<E> findGroupPaths(E endNode, Set<E> group, E startNode) {
		final Stack<E> nodes = new Stack<E>();
		final Set<E> visited = new HashSet<E>();

		nodes.push(endNode);

		boolean notDone = true;
		boolean found = false;

		while (!nodes.isEmpty() && notDone) {
			E currentNode = nodes.peek();

			boolean foundChildInGroup = false;
			if (currentNode == startNode || group.contains(currentNode)
					&& currentNode != startNode) {

				// We found an end point, back up one

				visited.add(currentNode);
				nodes.pop();
				group.add(currentNode);

			} else if (this.getParents(currentNode).size() > 0) {
				found = false;

				for (E child : this.getParents(currentNode)) {
					if (group.contains(child)) {
						foundChildInGroup = true;
					} else if (visited.contains(child)) {
						// ignore
					} else {
						// not done yet add child
						found = true;
						nodes.push(child);
					}

					if (found)
						break;
				}

				if (!found) {
					// no children were found
					final E node = nodes.pop();
					visited.add(node);
					// if one child is in group, add parent...
					if (foundChildInGroup) {
						group.add(node);
					}
				}

			} else {
				// no children, so back up one
				visited.add(nodes.pop());
			}

		}

		return group;
	}

	/**
	 * Checks whether <code>nodes</code> is a valid group.
	 * 
	 * @param nodes
	 * @return
	 */
	public boolean isGroup(Set<E> nodes) {
		int numChild = 0;
		int numParents = 0;
		
		// Can't group only one node.
		if (nodes.size() <= 1)
			return false;

		for (E node : nodes) {
			for (E child : this.getChildren(node)) {
				if (!nodes.contains(child)) {
					numChild++;
					break;
				}
			}

			for (E parent : this.getParents(node)) {
				if (!nodes.contains(parent)) {
					numParents++;
					break;
				}
			}

			if (numParents > 1 || numChild > 1) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Returns the depth map. This map is saved in memory. If the model has
	 * changed, use {@link #recalculateDepthMap()} to update the map.
	 * 
	 * @return
	 */
	public Map<E, Integer> getDepthMap() {
		return this.depthMap;
	}

	/**
	 * Returns all of the nodes for the specific level.
	 * 
	 * @param level
	 * @return
	 */
	public Collection<E> getNodesForLevel(int level) {
		final Collection<E> nodes = new ArrayList<E>();

		// extract nodes for the level
		for (Entry<E, Integer> entry : this.depthMap.entrySet()) {
			if (entry.getValue() == level)
				nodes.add(entry.getKey());
		}

		return nodes;
	}

	/**
	 * Recalculates the depth map. Should only be used when the graph model is
	 * changed, as it is performance intensive.
	 */
	public final void recalculateDepthMap() {
		this.depthMap.clear();
		this.depthMap.putAll(createDepthMap(this.start));
	}

	/**
	 * Returns a map of all of the nodes after and including the node passed in
	 * to how far away they are from the passed in node. Naturally, the passed
	 * in node will have a value of "0".
	 * 
	 * @param node
	 * @return
	 */
	private final Map<E, Integer> createDepthMap(E node) {
		final Map<E, Integer> depthMap = new IdentityHashMap<E, Integer>();

		// Goes through every child of the node
		for (E child : this.getChildren(node)) {
			// Gets the depth map of every child
			final Map<E, Integer> childDepthMap = this.createDepthMap(child);

			for (Entry<E, Integer> entry : childDepthMap.entrySet()) {
				final E childNode = entry.getKey();
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

		if (!depthMap.containsKey(node))
			depthMap.put(node, 0);

		return depthMap;
	}

	/**
	 * Returns the start node.
	 * 
	 * @return
	 */
	public final E getStartNode() {
		return this.start;
	}

	/**
	 * Returns the path between the start and end node. Recursively adds the
	 * paths between each child of the start node and the end node to create one
	 * path. If there are multiple path, this gets a path on a depth first
	 * basis.
	 * 
	 * @param start
	 *            The start node for the path
	 * @param end
	 *            The end node for the path
	 * @return An ordered, LinkedHashSet of nodes.
	 */
	public Collection<E> getPathBetweenNodes(E start, E end) {
		final Collection<E> path;

		path = new LinkedHashSet<E>();

		if (start == null || end == null
				|| !this.getDescendants(start).contains(end))
			return path;

		path.add(start);

		if (!this.getChildren(start).contains(end)) {
			final Collection<E> innerPath;

			innerPath = new LinkedHashSet<E>();

			for (E child : this.getChildren(start)) {
				final Collection<E> childPath;

				childPath = getPathBetweenNodes(child, end);

				// We swap out selected paths if a child has a shorter path than
				// the current selected path.
				if (childPath.size() > 0
						&& (innerPath.size() == 0 || childPath.size() < innerPath
								.size())) {
					innerPath.removeAll(innerPath);
					innerPath.addAll(childPath);
				}
			}
			path.addAll(innerPath);
		}

		path.add(end);

		return path;
	}
}
