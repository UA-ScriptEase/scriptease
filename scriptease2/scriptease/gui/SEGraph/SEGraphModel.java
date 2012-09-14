package scriptease.gui.SEGraph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import scriptease.controller.observer.graph.SEGraphObserver;
import scriptease.gui.SEGraph.builders.SEGraphNodeBuilder;

/**
 * Model class for SEGraph. This stores and handles all of the nodes in the
 * Graph.
 * 
 * @author kschenk
 * 
 * @param <E>
 */
public class SEGraphModel<E> {
	private final ReferenceNode start;
	private final SEGraphNodeBuilder<E> builder;
	private final List<SEGraphObserver<E>> observers;

	protected static enum GraphEvent {
		CHILD_ADDED,
		CHILD_REMOVED,
		PARENT_ADDED,
		PARENT_REMOVED,
		NODE_SELECTED
	}

	/**
	 * Creates a new model for an SEGraph with the passed in node as the start
	 * node.
	 * 
	 * @param start
	 */
	public SEGraphModel(E start, SEGraphNodeBuilder<E> builder) {
		this.observers = new ArrayList<SEGraphObserver<E>>();
		this.start = new ReferenceNode(start);
		this.builder = builder;

		for (E child : builder.getChildren(start)) {
			this.addNodeTo(child, start);
		}
	}

	/**
	 * Adds a node onto an existing node.
	 * 
	 * @param node
	 *            The node to add
	 * @param existingNode
	 *            The existing node
	 * @return true if the addition was successful
	 */
	public boolean addNodeTo(E node, E existingNode) {
		for (ReferenceNode referenceNode : this.getReferenceNodes()) {
			if (referenceNode.getObject() == existingNode) {
				referenceNode.addChild(new ReferenceNode(node));

				for (E child : this.builder.getChildren(node)) {
					this.addNodeTo(child, node);
				}
				return true;
			}
		}

		return false;
	}

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
	public boolean addNodeBetween(E node, E existingNode1, E existingNode2) {
		final ReferenceNode newReferenceNode;

		ReferenceNode firstNode = null;
		ReferenceNode secondNode = null;

		newReferenceNode = new ReferenceNode(node);

		for (ReferenceNode referenceNode : this.getReferenceNodes()) {
			if (referenceNode.getObject() == existingNode1) {
				firstNode = referenceNode;
			} else if (referenceNode.getObject() == existingNode2) {
				secondNode = referenceNode;
			}
		}

		if (firstNode == null || secondNode == null)
			return false;

		if (firstNode.getDescendants().contains(secondNode)) {
			firstNode.addChild(newReferenceNode);
			secondNode.addParent(newReferenceNode);

			return true;
		} else if (firstNode.getAncestors().contains(secondNode)) {
			secondNode.addChild(newReferenceNode);
			firstNode.addParent(newReferenceNode);

			return true;
		} else {
			final Map<ReferenceNode, Integer> depthMap = this.start
					.getDepthMap();
			final int firstNodeDepth = depthMap.get(firstNode);
			final int secondNodeDepth = depthMap.get(secondNode);

			if (firstNodeDepth > secondNodeDepth) {
				secondNode.addChild(newReferenceNode);
				firstNode.addParent(newReferenceNode);

				return true;
			} else {
				firstNode.addChild(newReferenceNode);
				secondNode.addParent(newReferenceNode);

				return true;
			}
		}
	}

	/**
	 * Removes the passed in node from the graph.
	 * 
	 * @param node
	 *            The node to be removed.
	 */
	public void removeNode(E node) {
		if (node == this.start.getObject())
			return;

		for (ReferenceNode descendant : this.getReferenceNodes()) {
			if (descendant.getObject() == node) {
				// We need to work with copies to avoid concurrent modifications
				final Collection<ReferenceNode> parentsCopy;

				parentsCopy = new HashSet<ReferenceNode>(
						descendant.getParents());

				for (ReferenceNode parent : parentsCopy)
					parent.removeChild(descendant);
				break;
			}
		}
	}

	/**
	 * Connects two nodes together. This method checks which node is further
	 * from the other, and adds the appropriate node as a parent or a child.
	 * 
	 * @param node1
	 * @param node2
	 * @return True if the nodes were successfully connected.
	 */
	public boolean connectNodes(E node1, E node2) {
		ReferenceNode firstNode = null;
		ReferenceNode secondNode = null;

		for (ReferenceNode referenceNode : this.getReferenceNodes()) {
			if (referenceNode.getObject() == node1) {
				firstNode = referenceNode;
			} else if (referenceNode.getObject() == node2) {
				secondNode = referenceNode;
			}
		}

		if (firstNode == null || secondNode == null)
			return false;

		if (firstNode.getDescendants().contains(secondNode)) {
			firstNode.addChild(secondNode);

			return true;
		} else if (firstNode.getAncestors().contains(secondNode)) {
			firstNode.addParent(secondNode);

			return true;
		} else {
			final Map<ReferenceNode, Integer> depthMap = this.start
					.getDepthMap();
			final int firstNodeDepth = depthMap.get(firstNode);
			final int secondNodeDepth = depthMap.get(secondNode);

			if (firstNodeDepth > secondNodeDepth) {
				firstNode.addParent(secondNode);

				return true;
			} else {
				firstNode.addChild(secondNode);

				return true;
			}
		}
	}

	/**
	 * Disconnects two nodes. If the node had no other connections, this will
	 * result in a deletion.
	 * 
	 * @param node1
	 * @param node2
	 * @return True if the nodes were successfully disconnected.
	 */
	public boolean disconnectNodes(E node1, E node2) {
		ReferenceNode firstNode = null;
		ReferenceNode secondNode = null;

		for (ReferenceNode referenceNode : this.getReferenceNodes()) {
			if (firstNode != null && secondNode != null)
				break;
			else if (referenceNode.getObject() == node1) {
				firstNode = referenceNode;
			} else if (referenceNode.getObject() == node2) {
				secondNode = referenceNode;
			}
		}

		if (firstNode == null || secondNode == null)
			return false;
		if (firstNode.getChildren().contains(secondNode)) {
			firstNode.removeChild(secondNode);
			return true;
		} else if (firstNode.getParents().contains(secondNode)) {
			firstNode.removeParent(secondNode);
			return true;
		} else {
			// This means the two nodes weren't connected at all.
			return false;
		}
	}

	/**
	 * Returns all nodes after and including the start node.
	 * 
	 * @return All nodes in the graph.
	 */
	public Collection<E> getNodes() {
		final Collection<E> nodes;
		nodes = new HashSet<E>();

		for (ReferenceNode child : this.getReferenceNodes()) {
			nodes.add(child.getObject());
		}

		return nodes;
	}

	/**
	 * Returns all graph nodes in the graph including the start node.
	 * 
	 * @return All Node nodes in the graph.
	 */
	private Collection<ReferenceNode> getReferenceNodes() {
		final Collection<ReferenceNode> nodes;
		nodes = new HashSet<ReferenceNode>();

		nodes.add(this.start);
		nodes.addAll(this.start.getDescendants());

		return nodes;
	}

	/**
	 * Returns the children for the passed in node.
	 * 
	 * @param node
	 * @return
	 */
	public Collection<E> getChildren(E node) {
		final Collection<E> children;
		children = new HashSet<E>();

		for (ReferenceNode referenceNode : this.getReferenceNodes()) {
			if (referenceNode.getObject() == node) {
				for (ReferenceNode child : referenceNode.getChildren()) {
					children.add(child.getObject());
				}
				break;
			}
		}
		return children;
	}

	/**
	 * Returns the parents for the passed in node.
	 * 
	 * @param node
	 * @return
	 */
	public Collection<E> getParents(E node) {
		final Collection<E> parents;
		parents = new HashSet<E>();

		for (ReferenceNode referenceNode : this.getReferenceNodes()) {
			if (referenceNode.getObject() == node) {
				for (ReferenceNode parent : referenceNode.getParents()) {
					parents.add(parent.getObject());
				}
				break;
			}
		}
		return parents;
	}

	/**
	 * Returns all ancestors for the passed in node, with ancestors being the
	 * node's parents, the parents' parents, and so forth.
	 * 
	 * @param node
	 * @return
	 */
	public Collection<E> getAncestors(E node) {
		final Collection<E> ancestors;
		ancestors = new HashSet<E>();

		for (ReferenceNode referenceNode : this.getReferenceNodes()) {
			if (referenceNode.getObject() == node) {
				for (ReferenceNode ancestor : referenceNode.getAncestors()) {
					ancestors.add(ancestor.getObject());
				}
				break;
			}
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
	public Collection<E> getDescendants(E node) {
		final Collection<E> descendants;
		descendants = new HashSet<E>();

		for (ReferenceNode referenceNode : this.getReferenceNodes()) {
			if (referenceNode.getObject() == node) {
				for (ReferenceNode descendant : referenceNode.getDescendants()) {
					descendants.add(descendant.getObject());
				}
				break;
			}
		}

		return descendants;
	}

	/**
	 * Returns a mapping of nodes with how deep they are in the graph.
	 * 
	 * @return
	 */
	public Map<E, Integer> getDepthMap() {
		final Map<E, Integer> depthMap;

		depthMap = new HashMap<E, Integer>();

		for (Entry<ReferenceNode, Integer> entry : this.start.getDepthMap()
				.entrySet()) {

			depthMap.put(entry.getKey().getObject(), entry.getValue());
		}

		return depthMap;
	}

	/**
	 * Returns the start node.
	 * 
	 * @return
	 */
	public E getStartNode() {
		return this.start.getObject();
	}

	/**
	 * Registers an observer to be notified when a graph node is changed.
	 * 
	 * @param observer
	 *            the observer to register
	 */
	public void addSEGraphObserver(SEGraphObserver<E> observer) {
		this.observers.add(observer);
	}

	/**
	 * Unregisters an observer to be notified when the Graph Node is changed.
	 * 
	 * @param observer
	 *            the observer to register
	 */
	public void removeSEGraphObserver(SEGraphObserver<E> observer) {
		this.observers.remove(observer);
	}

	/**
	 * Notifies the observers about a change to one of the nodes.
	 * 
	 * @param event
	 * @param node
	 */
	protected void notifyObservers(GraphEvent event, E node) {
		for (SEGraphObserver<E> observer : this.observers) {
			switch (event) {
			case NODE_SELECTED:
				observer.nodeSelected(node);
			}
		}
	}

	/**
	 * Notifies the observers about a change to two of the nodes.
	 * 
	 * @param event
	 * @param child
	 * @param parent
	 */
	private void notifyObservers(GraphEvent event, E parent, E child) {
		for (SEGraphObserver<E> observer : this.observers) {
			switch (event) {
			case CHILD_ADDED:
				observer.childAdded(child, parent);
				break;
			case CHILD_REMOVED:
				observer.childRemoved(child, parent);
				break;
			case PARENT_ADDED:
				observer.parentAdded(child, parent);
				break;
			case PARENT_REMOVED:
				observer.parentRemoved(child, parent);
				break;
			}
		}
	}

	/**
	 * A class for nodes in the graph that know about their parents, children,
	 * and reference some object that is used as the node in the graph.
	 * 
	 * @author kschenk
	 * 
	 */
	private class ReferenceNode {
		private final E object;
		private final Set<ReferenceNode> parents;
		private final Set<ReferenceNode> children;

		/**
		 * Creates a new reference node with the passed in object.
		 * 
		 * @param object
		 */
		private ReferenceNode(E object) {
			this.object = object;
			this.parents = new HashSet<ReferenceNode>();
			this.children = new HashSet<ReferenceNode>();
		}

		/**
		 * Adds a new parent and adds this node to it as a child.
		 * 
		 * @param parent
		 *            The new parent to add to this node.
		 */
		private void addParent(ReferenceNode parent) {
			if (this.parents.add(parent)) {
				SEGraphModel.this.notifyObservers(GraphEvent.PARENT_ADDED,
						parent.getObject(), this.getObject());

				parent.addChild(this);
			}
		}

		/**
		 * Removes the specified parent.
		 * 
		 * @param parent
		 *            The parent to remove from this node.
		 */
		private void removeParent(ReferenceNode parent) {
			if (this.parents.remove(parent)) {
				SEGraphModel.this.notifyObservers(GraphEvent.PARENT_REMOVED,
						parent.getObject(), this.getObject());

				parent.removeChild(this);
			}
		}

		/**
		 * Adds a new child and adds this node to it as a parent.
		 * 
		 * @param child
		 *            The new child to add to this node.
		 */
		private void addChild(ReferenceNode child) {
			if (this.children.add(child)) {
				SEGraphModel.this.notifyObservers(GraphEvent.CHILD_ADDED,
						this.getObject(), child.getObject());

				child.addParent(this);
			}
		}

		/**
		 * Removes the specified child.
		 * 
		 * @param child
		 *            The child to remove from this node.
		 */
		private void removeChild(ReferenceNode child) {
			if (this.children.remove(child)) {
				SEGraphModel.this.notifyObservers(GraphEvent.CHILD_REMOVED,
						this.getObject(), child.getObject());

				child.removeParent(this);
			}
		}

		/**
		 * Returns a set of all nodes below this node. That is, the children,
		 * the children's children, and so on.
		 * 
		 * @return The child graph
		 */
		private Set<ReferenceNode> getDescendants() {
			Set<ReferenceNode> childGraph;
			childGraph = new HashSet<ReferenceNode>();

			childGraph.addAll(this.children);

			for (ReferenceNode child : this.children) {
				childGraph.addAll(child.getDescendants());
			}
			return childGraph;
		}

		/**
		 * Returns a set of all nodes above this node. That is, the parents, the
		 * parents' parents, and so on.
		 * 
		 * @return The parent graph
		 */
		private Set<ReferenceNode> getAncestors() {
			Set<ReferenceNode> parentGraph;
			parentGraph = new HashSet<ReferenceNode>();

			parentGraph.addAll(this.parents);

			for (ReferenceNode parent : this.parents)
				parentGraph.addAll(parent.getAncestors());

			return parentGraph;
		}

		/**
		 * Returns the immediate children of the node.
		 * 
		 * @return The immediate children of the node.
		 */
		private Collection<ReferenceNode> getChildren() {
			return this.children;
		}

		/**
		 * Returns the immediate parents of the node.
		 * 
		 * @return The immediate parents of the node.
		 */
		public Collection<ReferenceNode> getParents() {
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
		 * Returns a map containing all descendants and the distance of each
		 * from this node.
		 * 
		 * @return
		 */
		private Map<ReferenceNode, Integer> getDepthMap() {
			Map<ReferenceNode, Integer> nodes = new IdentityHashMap<ReferenceNode, Integer>();

			for (ReferenceNode child : this.getChildren()) {
				Map<ReferenceNode, Integer> childNodes = child.getDepthMap();
				for (Entry<ReferenceNode, Integer> entry : childNodes
						.entrySet()) {
					final ReferenceNode childNode = entry.getKey();
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
