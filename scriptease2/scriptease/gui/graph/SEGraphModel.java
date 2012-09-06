package scriptease.gui.graph;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Model class for SEGraph. This stores and handles all of the nodes in the
 * Graph.
 * 
 * @author kschenk
 * 
 * @param <E>
 */
public class SEGraphModel<E> {
	private final Node start;

	/**
	 * Creates a new model for an SEGraph with the passed in node as the start
	 * node.
	 * 
	 * @param start
	 */
	public SEGraphModel(E start) {
		this.start = new Node(start);
	}

	/**
	 * Adds a new node onto an existing node.
	 * 
	 * @param node
	 *            The new node to add
	 * @param existingNode
	 *            The existing node
	 * @return true if the addition was successful
	 */
	public boolean addNodeTo(E node, E existingNode) {
		for (Node descendant : this.start.getDescendantGraph()) {
			if (descendant.getObject() == existingNode) {
				descendant.addChild(new Node(node));
				return true;
			}
		}

		return false;
	}

	/**
	 * Adds a new node between two existing nodes. Order of the two nodes does
	 * not matter; this method figures out which node is above the other.
	 * 
	 * @param node
	 *            The new node.
	 * @param existingNode1
	 *            The first selected existing node.
	 * @param existingNode2
	 *            The second selected existing node.
	 * @return true if the addition was successful
	 */
	public boolean addNodeBetween(E node, E existingNode1, E existingNode2) {
		final Node newNode;

		Node firstNode = null;
		Node secondNode = null;

		newNode = new Node(node);

		for (Node descendant : this.start.getDescendantGraph()) {
			if (descendant.getObject() == existingNode1) {
				firstNode = descendant;
			} else if (descendant.getObject() == existingNode2) {
				secondNode = descendant;
			}
		}

		if (firstNode == null || secondNode == null)
			return false;

		if (firstNode.getDescendantGraph().contains(secondNode)) {
			firstNode.addChild(newNode);
			secondNode.addParent(newNode);

			return true;
		} else if (firstNode.getAncestorGraph().contains(secondNode)) {
			secondNode.addChild(newNode);
			firstNode.addParent(newNode);

			return true;
		}

		return false;
	}

	/**
	 * Removes the passed in node from the graph.
	 * 
	 * @param node
	 *            The node to be removed.
	 */
	public void removeNode(E node) {
		for (Node descendant : this.start.getDescendantGraph()) {
			if (descendant.getObject() == node) {
				for (Node child : descendant.getChildren())
					child.removeParent(descendant);
				for (Node parent : descendant.getParents())
					parent.removeChild(descendant);
				break;
			}
		}
	}

	public boolean connectNodes(E node1, E node2) {
		// TODO Add child to node1 or node2
		return false;
	}

	public boolean disconnectNodes(E node1, E node2) {
		// TODO Remove child from node1 or node2
		return false;
	}

	/**
	 * Returns all nodes after and including the start node.
	 * 
	 * @return All nodes in the graph.
	 */
	public Collection<E> getNodes() {
		final Collection<E> nodes;
		nodes = new HashSet<E>();

		nodes.add(this.start.getObject());

		for (Node child : this.start.getDescendantGraph()) {
			nodes.add(child.getObject());
		}

		return nodes;
	}

	/**
	 * Gets the children for the passed in node.
	 * 
	 * @param node
	 * @return
	 */
	public Collection<E> getChildren(E node) {
		final Collection<E> children;
		children = new HashSet<E>();

		for (Node descendant : this.start.getDescendantGraph()) {
			if (descendant.getObject() == node) {
				for (Node child : descendant.getChildren()) {
					children.add(child.getObject());
				}
				break;
			}
		}
		return children;
	}

	/**
	 * Gets the parents for the passed in node.
	 * 
	 * @param node
	 * @return
	 */
	public Collection<E> getParents(E node) {
		final Collection<E> parents;
		parents = new HashSet<E>();

		for (Node descendant : this.start.getDescendantGraph()) {
			if (descendant.getObject() == node) {
				for (Node parent : descendant.getParents()) {
					parents.add(parent.getObject());
				}
				break;
			}
		}
		return parents;
	}

	/**
	 * @return
	 */
	public Map<E, Integer> getDepthMap() {
		final Map<E, Integer> depthMap;

		depthMap = new HashMap<E, Integer>();

		for (Entry<Node, Integer> entry : this.start.getDepthMap()
				.entrySet()) {

			depthMap.put(entry.getKey().getObject(), entry.getValue());
		}

		return depthMap;
	}

	/**
	 * Class for nodes in a graph which may have children.
	 * 
	 * @author kschenk
	 * 
	 */
	private class Node {
		private final E object;
		private final Set<Node> parents;
		private final Set<Node> children;

		private Node(E object) {
			this.object = object;
			this.parents = new HashSet<Node>();
			this.children = new HashSet<Node>();
		}

		/**
		 * Adds a new parent and adds this node to it as a child.
		 * 
		 * @param parent
		 *            The new parent to add to this node.
		 */
		private void addParent(Node parent) {
			this.parents.add(parent);
			parent.children.add(this);
		}

		/**
		 * Removes the specified parent.
		 * 
		 * @param parent
		 *            The parent to remove from this node.
		 */
		private void removeParent(Node parent) {
			this.parents.remove(parent);
		}

		/**
		 * Adds a new child and adds this node to it as a parent.
		 * 
		 * @param child
		 *            The new child to add to this node.
		 */
		private void addChild(Node child) {
			this.children.add(child);
			child.parents.add(this);
		}

		/**
		 * Removes the specified child.
		 * 
		 * @param child
		 *            The child to remove from this node.
		 */
		private void removeChild(Node child) {
			this.children.remove(child);
		}

		/**
		 * Returns a set of all nodes below this node. That is, the children,
		 * the children's children, and so on.
		 * 
		 * @return The child graph
		 */
		private Set<Node> getDescendantGraph() {
			Set<Node> childGraph;
			childGraph = new HashSet<Node>();

			for (Node child : this.children)
				childGraph.addAll(child.getDescendantGraph());

			return childGraph;
		}

		/**
		 * Returns a set of all nodes above this node. That is, the parents, the
		 * parents' parents, and so on.
		 * 
		 * @return The parent graph
		 */
		private Set<Node> getAncestorGraph() {
			Set<Node> parentGraph;
			parentGraph = new HashSet<Node>();

			for (Node parent : this.parents)
				parentGraph.addAll(parent.getAncestorGraph());

			return parentGraph;
		}

		/**
		 * Returns the immediate children of the node.
		 * 
		 * @return The immediate children of the node.
		 */
		private Collection<Node> getChildren() {
			return this.children;
		}

		/**
		 * Returns the immediate parents of the node.
		 * 
		 * @return The immediate parents of the node.
		 */
		public Collection<Node> getParents() {
			return this.parents;
		}

		/**
		 * Returns the object for which this node was created.
		 * 
		 * @return
		 */
		private E getObject() {
			return this.object;
		}

		/**
		 * Returns a map containing each sub node and the distance from this
		 * node. This can be computational expensive on large graphs, recommend
		 * storing when the graph isn't changing
		 * 
		 * @return
		 */
		private Map<Node, Integer> getDepthMap() {
			Map<Node, Integer> nodes = new IdentityHashMap<Node, Integer>();

			for (Node child : this.getChildren()) {
				Map<Node, Integer> childNodes = child.getDepthMap();
				for (Entry<Node, Integer> entry : childNodes.entrySet()) {
					final Node childNode = entry.getKey();
					final Integer depth = entry.getValue() + 1;
					if (nodes.containsKey(childNode)) {
						if (depth > nodes.get(childNode))
							nodes.put(childNode, depth);
					} else
						nodes.put(childNode, depth);
				}
			}
			if (!nodes.containsKey(this))
				nodes.put(this, 0);
			return nodes;
		}
	}
}
