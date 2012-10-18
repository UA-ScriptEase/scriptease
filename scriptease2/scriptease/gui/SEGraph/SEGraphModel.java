package scriptease.gui.SEGraph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Model class for SEGraph. This stores and handles all of the nodes in the
 * Graph. A model must be created for an SEGraph.
 * 
 * @author kschenk
 * 
 * @param <E>
 */
public abstract class SEGraphModel<E> {
	private final E start;

	/**
	 * Creates a new model for an SEGraph with the passed in node as the start
	 * node.
	 * 
	 * @param start
	 */
	public SEGraphModel(E start) {
		this.start = start;
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
		if (firstNode == null || secondNode == null)
			return false;

		if (this.getDescendants(firstNode).contains(secondNode)) {
			this.addChild(node, firstNode);
			this.addChild(secondNode, node);
		} else if (this.getDescendants(secondNode).contains(firstNode)) {
			this.addChild(node, secondNode);
			this.addChild(firstNode, node);
		} else {
			final Map<E, Integer> depthMap = this.getDepthMap(this.start);

			final int firstNodeDepth = depthMap.get(firstNode);
			final int secondNodeDepth = depthMap.get(secondNode);

			if (firstNodeDepth > secondNodeDepth) {
				this.addChild(node, secondNode);
				this.addChild(firstNode, node);
			} else {
				this.addChild(node, firstNode);
				this.addChild(secondNode, node);
			}
		}
		return true;
	}

	/**
	 * Removes the passed in node from the graph.
	 * 
	 * @param node
	 *            The node to be removed.
	 */
	public final boolean removeNode(E node) {
		if (node == this.start)
			return false;

		for (E nodeInModel : this.getNodes()) {
			if (nodeInModel == node) {
				// We need to work with copies to avoid concurrent modifications
				final Collection<E> parentsCopy;

				parentsCopy = new HashSet<E>(this.getParents(nodeInModel));

				for (E parent : parentsCopy)
					this.removeChild(nodeInModel, parent);

				return true;
			}
		}

		return false;
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
		if (child == null || parent == null)
			return false;

		if (this.getDescendants(child).contains(parent)) {
			return false;
		} else if (this.getDescendants(parent).contains(child)) {
			this.addChild(child, parent);
			return true;
		} else {
			final Map<E, Integer> depthMap = this.getDepthMap(this.start);

			final int childDepth = depthMap.get(child);
			final int parentDepth = depthMap.get(parent);

			if (childDepth >= parentDepth) {
				this.addChild(child, parent);
				return true;
			} else {
				return false;
			}
		}
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
		if (child == null || parent == null)
			return false;
		else
			return this.removeChild(child, parent);
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
	 * Returns a map of all of the nodes after and including the node passed in
	 * to how far away they are from the passed in node. Naturally, the passed
	 * in node will have a value of "0".
	 * 
	 * @param node
	 * @return
	 */
	public final Map<E, Integer> getDepthMap(E node) {
		final Map<E, Integer> nodes;

		nodes = new IdentityHashMap<E, Integer>();

		for (E child : this.getChildren(node)) {
			Map<E, Integer> childNodes = this.getDepthMap(child);
			for (Entry<E, Integer> entry : childNodes.entrySet()) {
				final E childNode = entry.getKey();
				final Integer depth = entry.getValue() + 1;
				if (nodes.containsKey(childNode)) {
					if (depth > nodes.get(childNode))
						nodes.put(childNode, depth);
				} else
					nodes.put(childNode, depth);
			}
		}

		if (!nodes.containsKey(node))
			nodes.put(node, 0);
		return nodes;
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
	 * Returns the path between the start and end node.
	 * 
	 * @param start
	 * @param end
	 * @return
	 */
	public Collection<E> getPathBetweenNodes(E start, E end) {
		final Collection<E> path;

		path = new ArrayList<E>();

		if (start == null || end == null
				|| !this.getDescendants(start).contains(end))
			return path;

		if (this.getChildren(start).contains(end)) {
			path.add(start);

			return path;
		}

		// TODO This may or may not work.
		for (E child : this.getChildren(start))
			path.addAll(getPathBetweenNodes(child, end));

		return path;
	}
	
	// XXX Old Get Path Code from GraphNode.
	
	/*
	*//**
	 * Finds the best path from this node, to the given end node. Criteria being
	 * the most selected nodes (from previous selection), otherwise if no
	 * selection, take the shortest path.
	 * 
	 * @param end
	 * @return
	 *//*
	public List<GraphNode> getBestPathToNode(GraphNode end) {
		List<GraphNode> bestPath = new ArrayList<GraphNode>();
		if (this != null && end != null && this != end) {
			// Get all paths to that node
			Collection<List<GraphNode>> paths = this.getPathsTo(end);
			int mostSelected = 0;
			// For each path, rank which one is best
			for (List<GraphNode> path : paths) {
				int selected = 0;
				for (GraphNode node : path) {
					if (node.isSelected())
						selected++;
					// break when we hit a non-selected node
					else
						break;
				}
				// if the new path is better, pick it
				if (selected > mostSelected) {
					mostSelected = selected;
					bestPath = path;
				}
			}
			// if nothing was found, pick the shortest path
			if (bestPath.isEmpty() && !paths.isEmpty()) {
				int shortestPath = this.INF_PATH_LENGTH;
				for (List<GraphNode> path : paths) {
					final int size = path.size();
					if (size < shortestPath) {
						shortestPath = size;
						bestPath = path;
					}
				}
			}
		}
		return bestPath;
	}

	*//**
	 * Get all paths from this GraphNode to the given GraphNode.
	 * 
	 * @param end
	 * @return
	 *//*
	public Collection<List<GraphNode>> getPathsTo(GraphNode end) {
		Collection<List<GraphNode>> paths = new ArrayList<List<GraphNode>>();
		if (end != null) {
			if (end != this) {
				for (GraphNode child : this.children) {
					if (end.isDescendant(child)) {
						// Calculate subPaths to end
						Collection<List<GraphNode>> subPaths = child
								.getPathsTo(end);

						// For each subPath, append yourself to it and add it to
						// paths
						for (List<GraphNode> subPath : subPaths) {
							subPath.add(0, this);
							paths.add(subPath);
						}
					}
				}
			} else {
				List<GraphNode> path = new ArrayList<GraphNode>();
				path.add(this);
				paths.add(path);
			}
		}
		return paths;
	}*/
}
