package scriptease.gui.graph;

import java.lang.ref.WeakReference;
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
import scriptease.gui.graph.builders.SEGraphNodeBuilder;

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
	private final List<WeakReference<SEGraphObserver<E>>> observers;

	private static enum GraphEvent {
		CHILD_ADDED,
		CHILD_REMOVED,
		PARENT_ADDED,
		PARENT_REMOVED
	}

	/**
	 * Creates a new model for an SEGraph with the passed in node as the start
	 * node.
	 * 
	 * @param start
	 */
	public SEGraphModel(E start, SEGraphNodeBuilder<E> builder) {
		this.observers = new ArrayList<WeakReference<SEGraphObserver<E>>>();
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

		if (firstNode.getDescendantGraph().contains(secondNode)) {
			firstNode.addChild(newReferenceNode);
			secondNode.addParent(newReferenceNode);

			return true;
		} else if (firstNode.getAncestorGraph().contains(secondNode)) {
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
				final Collection<ReferenceNode> childrenCopy;
				final Collection<ReferenceNode> parentsCopy;

				childrenCopy = new HashSet<ReferenceNode>(
						descendant.getChildren());
				parentsCopy = new HashSet<ReferenceNode>(
						descendant.getParents());

				for (ReferenceNode child : childrenCopy)
					child.removeParent(descendant);
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

		if (firstNode.getDescendantGraph().contains(secondNode)) {
			firstNode.addChild(secondNode);

			return true;
		} else if (firstNode.getAncestorGraph().contains(secondNode)) {
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
			if (referenceNode.getObject() == node1) {
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
	 * Gets all graph nodes.
	 * 
	 * @return All Node nodes in the graph.
	 */
	private Collection<ReferenceNode> getReferenceNodes() {
		final Collection<ReferenceNode> nodes;
		nodes = new HashSet<ReferenceNode>();

		nodes.add(this.start);
		nodes.addAll(this.start.getDescendantGraph());

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

		for (ReferenceNode descendant : this.getReferenceNodes()) {
			if (descendant.getObject() == node) {
				for (ReferenceNode child : descendant.getChildren()) {
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

		for (ReferenceNode descendant : this.getReferenceNodes()) {
			if (descendant.getObject() == node) {
				for (ReferenceNode parent : descendant.getParents()) {
					parents.add(parent.getObject());
				}
				break;
			}
		}
		return parents;
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
		Collection<WeakReference<SEGraphObserver<E>>> observersCopy = new ArrayList<WeakReference<SEGraphObserver<E>>>(
				this.observers);

		for (WeakReference<SEGraphObserver<E>> observerRef : observersCopy) {
			SEGraphObserver<E> storyComponentObserver = observerRef.get();
			if (storyComponentObserver != null
					&& storyComponentObserver == observer)
				return;
			else if (storyComponentObserver == null)
				this.observers.remove(observerRef);
		}

		this.observers.add(new WeakReference<SEGraphObserver<E>>(observer));
	}

	/**
	 * Unregisters an observer to be notified when the Graph Node is changed.
	 * 
	 * @param observer
	 *            the observer to register
	 */
	public void removeSEGraphObserver(SEGraphObserver<E> observer) {
		for (WeakReference<SEGraphObserver<E>> reference : this.observers) {
			if (reference.get() == observer) {
				this.observers.remove(reference);
				return;
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
		Collection<WeakReference<SEGraphObserver<E>>> observersCopy = new ArrayList<WeakReference<SEGraphObserver<E>>>(
				this.observers);

		for (WeakReference<SEGraphObserver<E>> observerRef : observersCopy) {
			SEGraphObserver<E> graphNodeObserver = observerRef.get();
			if (graphNodeObserver != null)
				switch (event) {
				case CHILD_ADDED:
					graphNodeObserver.childAdded(child, parent);
					break;
				case CHILD_REMOVED:
					graphNodeObserver.childRemoved(child, parent);
					break;
				case PARENT_ADDED:
					graphNodeObserver.parentAdded(child, parent);
					break;
				case PARENT_REMOVED:
					graphNodeObserver.parentRemoved(child, parent);
					break;
				}
			else
				this.observers.remove(observerRef);
		}
	}

	/**
	 * Class for nodes in a graph which may have children.
	 * 
	 * @author kschenk
	 * 
	 */
	private class ReferenceNode {
		private final E object;
		private final Set<ReferenceNode> parents;
		private final Set<ReferenceNode> children;

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
		private Set<ReferenceNode> getDescendantGraph() {
			Set<ReferenceNode> childGraph;
			childGraph = new HashSet<ReferenceNode>();

			childGraph.addAll(this.children);

			for (ReferenceNode child : this.children) {
				childGraph.addAll(child.getDescendantGraph());
			}
			return childGraph;
		}

		/**
		 * Returns a set of all nodes above this node. That is, the parents, the
		 * parents' parents, and so on.
		 * 
		 * @return The parent graph
		 */
		private Set<ReferenceNode> getAncestorGraph() {
			Set<ReferenceNode> parentGraph;
			parentGraph = new HashSet<ReferenceNode>();

			parentGraph.addAll(this.parents);

			for (ReferenceNode parent : this.parents)
				parentGraph.addAll(parent.getAncestorGraph());

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
