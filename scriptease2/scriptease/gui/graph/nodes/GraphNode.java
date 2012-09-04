package scriptease.gui.graph.nodes;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import scriptease.controller.GraphNodeVisitor;
import scriptease.controller.observer.GraphNodeEvent;
import scriptease.controller.observer.GraphNodeEvent.GraphNodeEventType;
import scriptease.controller.observer.GraphNodeObserver;
import sun.awt.util.IdentityArrayList;

/**
 * GraphNode represents a node in a Graph. The implementation is similar to a
 * multi-linked list, with each node containing references to its parents and
 * children. The actual nodes used should be subclasses of GraphNode (such as
 * TextNode), and should provide any node specific logic.
 * 
 * @author mfchurch
 * @author graves (refactored)
 */
public abstract class GraphNode implements Cloneable {
	protected IdentityArrayList<GraphNode> parents;
	protected IdentityArrayList<GraphNode> children;
	protected Collection<WeakGraphNodeObserverReference<GraphNodeObserver>> observers;

	protected boolean isBeingUsed;
	protected boolean selected;
	protected boolean terminal;

	// Upper bound on longest path
	private final int INF_PATH_LENGTH = 100;
	// default size for number of parents and children
	private final int INIT_CAPACITY = 2;

	public GraphNode() {
		this.init();
	}

	public GraphNode(List<GraphNode> children, boolean isTerminal) {
		this.init();
		if (children != null && !children.isEmpty())
			this.children.addAll(children);
		this.terminal = isTerminal;
	}

	/**
	 * Returns if the GraphNode represents the given object. Must be implemented
	 * by subclass
	 * 
	 * @param object
	 * @return
	 */
	public abstract boolean represents(Object object);

	/**
	 * Checks descendants for a GraphNode which represents the given Object.
	 * Returns null if not found.
	 * 
	 * @param object
	 * @return
	 */
	public GraphNode getRepresentingGraphNode(Object object) {
		// check if you represent the object
		if (this.represents(object))
			return this;
		else {
			// otherwise check the children
			for (GraphNode child : this.children) {
				if (child.represents(object))
					return child;
				else
					return child.getRepresentingGraphNode(object);
			}
		}
		return null;
	}

	private void init() {
		this.parents = new IdentityArrayList<GraphNode>(this.INIT_CAPACITY);
		this.children = new IdentityArrayList<GraphNode>(this.INIT_CAPACITY);
		this.observers = new ArrayList<WeakGraphNodeObserverReference<GraphNodeObserver>>();
	}

	/**
	 * When cloning GraphNodes, it is possible to break the graph when nodes fan
	 * in because more than one clone of the child can be created by the cloning
	 * process. Thus you _must_ use GraphNodeReference resolver after cloning in
	 * order to repair these duplications in the graph.
	 */
	@Override
	public GraphNode clone() {
		GraphNode clone = null;
		try {
			clone = (GraphNode) super.clone();
		} catch (CloneNotSupportedException e) {
			// I can't think of a better way to deal with this
			Thread.getDefaultUncaughtExceptionHandler().uncaughtException(
					Thread.currentThread(), e);
		}
		// reset the clone
		clone.init();

		clone.setSelected(this.selected);
		clone.setTerminal(this.terminal);

		// clone all of the children
		for (GraphNode child : this.children) {
			GraphNode childClone = child.clone();
			clone.addChild(childClone);
		}
		return clone;
	}

	public void addGraphNodeObserver(GraphNodeObserver observer) {
		Collection<WeakGraphNodeObserverReference<GraphNodeObserver>> observersCopy = new ArrayList<WeakGraphNodeObserverReference<GraphNodeObserver>>(
				this.observers);

		for (WeakGraphNodeObserverReference<GraphNodeObserver> observerRef : observersCopy) {
			GraphNodeObserver graphNodeObserver = observerRef.get();
			if (graphNodeObserver != null && graphNodeObserver == observer)
				return;
			else if (graphNodeObserver == null)
				this.observers.remove(observerRef);
		}

		this.observers
				.add(new WeakGraphNodeObserverReference<GraphNodeObserver>(
						observer));
	}

	public void removeGraphNodeObserver(GraphNodeObserver observer) {
		for (WeakGraphNodeObserverReference<GraphNodeObserver> reference : this.observers) {
			if (reference.get() == observer) {
				this.observers.remove(reference);
				return;
			}
		}
	}

	// Note: This is public so that the mouseListener attached to the JComponent
	// that represents the GraphNode can call it. TODO: refactor this.
	public void notifyObservers(final GraphNodeEvent event) {
		Collection<WeakGraphNodeObserverReference<GraphNodeObserver>> observersCopy = new ArrayList<WeakGraphNodeObserverReference<GraphNodeObserver>>(
				this.observers);

		for (WeakGraphNodeObserverReference<GraphNodeObserver> observerRef : observersCopy) {
			GraphNodeObserver graphNodeObserver = observerRef.get();
			if (graphNodeObserver != null)
				graphNodeObserver.nodeChanged(event);
			else
				this.observers.remove(observerRef);
		}
	}

	/**
	 * Sets whether the GraphNode is terminal or not.
	 * 
	 * @param isTerminal
	 */
	public void setTerminal(boolean isTerminal) {
		this.terminal = isTerminal;
	}

	/**
	 * Sets whether the GraphNode is selected or not.
	 * 
	 * @param isSelected
	 */
	public void setSelected(Boolean isSelected) {
		this.selected = isSelected;
	}

	/**
	 * Returns whether the GraphNode is selected or not.
	 * 
	 * @return
	 */
	public boolean isSelected() {
		return this.selected;
	}

	/**
	 * Returns whether the GraphNode is deletable or not. Determined by if the
	 * GraphNode is a start or end node. Does not need to be called in order to
	 * delete the node, but it is useful if you need to check both.
	 * 
	 * @return
	 */
	public boolean isDeletable() {
		if (this.isEndNode() || this.isStartNode())
			return false;
		else
			return true;
	}

	/**
	 * Returns true if the current node is the start node. Otherwise, returns
	 * false.
	 * 
	 * @return
	 */
	public boolean isStartNode() {
		List<GraphNode> parents = this.getParents();

		if (parents.size() > 0)
			return false;
		else
			return true;
	}

	/**
	 * Returns true if the current node is the end node. Otherwise, returns
	 * false.
	 * 
	 * @return
	 */

	public boolean isEndNode() {
		List<GraphNode> children = this.getChildren();

		if (children.size() > 0)
			return false;
		else
			return true;
	}

	/**
	 * Get a copy of the children of the GraphNode. Returning a copy of the List
	 * of children instead of a reference to the List itself ensures that the
	 * List can't be modified or changed via the getChildren() method. The
	 * individual GraphNodes inside the List of children can still be changed,
	 * however.
	 * 
	 * @return
	 */
	public List<GraphNode> getChildren() {
		IdentityArrayList<GraphNode> childrenCopy = new IdentityArrayList<GraphNode>(
				this.children.size());
		for (GraphNode child : this.children) {
			childrenCopy.add(child);
		}
		return childrenCopy;
	}

	/**
	 * Get a copy of the parents of the GraphNode. Returning a copy of the List
	 * of parents instead of a reference to the List itself ensures that the
	 * List can't be modified or changed via the getParents() method. The
	 * individual GraphNodes inside the List of parents can still be changed,
	 * however.
	 * 
	 * @return
	 */
	public List<GraphNode> getParents() {
		IdentityArrayList<GraphNode> parentsCopy = new IdentityArrayList<GraphNode>(
				this.parents.size());
		for (GraphNode parent : this.parents) {
			parentsCopy.add(parent);
		}

		return parentsCopy;
	}

	/**
	 * Adds the given GraphNode child iff (if and only if) it is not an ancestor
	 * or equal to the GraphNode (maintains directed acyclic state)
	 * 
	 * @param child
	 */
	public boolean addChild(GraphNode child) {
		// check for loops
		if (!this.children.contains(child) && !isDescendant(child)
				&& this.children.add(child)) {
			this.notifyObservers(new GraphNodeEvent(child,
					GraphNodeEventType.CONNECTION_ADDED));

			// add yourself as a parent of the child
			child.addParent(this);
			return true;
		}
		return false;
	}

	/**
	 * Adds the given GraphNode parent iff (if and only if) it is not a
	 * descendant or equal to the GraphNode (maintains directed acyclic state)
	 * 
	 * @param parent
	 * @return
	 */
	public boolean addParent(GraphNode parent) {
		if (!this.parents.contains(parent) && !parent.isDescendant(this)
				&& this.parents.add(parent)) {
			this.notifyObservers(new GraphNodeEvent(parent,
					GraphNodeEventType.CONNECTION_ADDED));

			// add yourself as a child of that parent
			parent.addChild(this);
			return true;
		}
		return false;
	}

	/**
	 * Removes this GraphNode from its parents.
	 * 
	 * @return
	 */
	public boolean removeParents() {
		boolean success = true;

		// Remove this node from each parent.
		List<GraphNode> parents = this.getParents();
		for (GraphNode parent : parents) {
			success &= this.removeFromParent(parent, false);
		}
		return success;
	}

	/**
	 * Removes each child from this GraphNode.
	 * 
	 * @return
	 */
	public boolean removeChildren() {
		boolean success = true;

		// Remove this node from each child.
		List<GraphNode> children = this.getChildren();
		for (GraphNode child : children) {
			success &= this.removeChild(child, false);
		}
		return success;
	}

	/**
	 * Removes the given GraphNode child from the list of children. Optionally
	 * reparents the child's children to have this GraphNode as a parent.
	 * 
	 * @param child
	 * @param reparentChildren
	 */
	public boolean removeChild(GraphNode child, boolean reparentChildren) {
		// remove the child
		if (this.children.remove(child)) {
			this.notifyObservers(new GraphNodeEvent(child,
					GraphNodeEventType.CONNECTION_REMOVED));

			// remove yourself as the parent of the child.
			child.removeFromParent(this, false);

			// optionally reparent the children.
			if (reparentChildren) {
				// Get a copy of the List that holds the child's children.
				List<GraphNode> oldChildren = child.getChildren();

				// Remove the child's children.
				child.removeChildren();

				// Add the child's old children to this node.
				this.addChildren(oldChildren);
			}
			return true;
		}
		return false;
	}

	/**
	 * Removes this GraphNode from a parent node, optionally reparenting this
	 * node's children to the parent.
	 * 
	 * @param parent
	 * @param reparentChildren
	 * @return
	 */
	public boolean removeFromParent(GraphNode parent, boolean reparentChildren) {
		if (this.parents.remove(parent)) {
			this.notifyObservers(new GraphNodeEvent(parent,
					GraphNodeEventType.CONNECTION_REMOVED));

			// remove yourself as a child of the parent.
			parent.removeChild(this, false);

			// optionally reparent children
			if (reparentChildren) {
				// Get a copy of the List that holds this node's children.
				List<GraphNode> oldChildren = this.getChildren();

				// Remove this node's children.
				this.removeChildren();

				// Add this node's old children to the parent.
				parent.addChildren(oldChildren);
			}
			return true;
		}
		return false;
	}

	/**
	 * Removes the given descendant GraphNode from the GraphNode, and optionally
	 * reparents it's children to the descendant's parent. Returns if the
	 * descendant was removed.
	 * 
	 * @param descendant
	 * @param reparentChildren
	 * @return
	 */
	public boolean removeDescendant(GraphNode descendant,
			boolean reparentChildren) {
		List<GraphNode> children = this.getChildren();
		for (GraphNode child : children) {
			if (child == descendant) {
				return removeChild(child, reparentChildren);
			} else {
				if (child.removeDescendant(descendant, reparentChildren))
					return true;
			}
		}
		return false;
	}

	/**
	 * Replaces the given childNode by the given alternateNode Returns whether
	 * or not the operation was successful.
	 * 
	 * @param childNode
	 * @param alternateNode
	 * @return
	 */
	public boolean replaceChild(GraphNode childNode, GraphNode alternateNode) {
		boolean success;
		if (success = (this.children.contains(childNode) && !isDescendant(alternateNode))) {
			success &= this.removeChild(childNode, false);
			success &= this.addChild(alternateNode);
			return success;
		}
		return success;
	}

	@Override
	public boolean equals(Object other) {
		return other.hashCode() == this.hashCode();
	}

	@Override
	public int hashCode() {
		int hash = 0;
		for (GraphNode child : this.getChildren()) {
			hash += child.hashCode();
		}
		return hash;
	}

	public abstract void process(GraphNodeVisitor processController);

	public void addChildren(Collection<GraphNode> children) {
		for (GraphNode child : children) {
			this.addChild(child);
		}
	}

	/**
	 * Returns whether this GraphNode is terminal or not.
	 * 
	 * @return
	 */
	public boolean isTerminalNode() {
		// if the node has no child, it's terminal otherwise check if it is set
		// to be terminal
		return this.children.isEmpty() ? true : this.terminal;
	}

	/**
	 * Finds the best path from this node, to the given end node. Criteria being
	 * the most selected nodes (from previous selection), otherwise if no
	 * selection, take the shortest path.
	 * 
	 * @param end
	 * @return
	 */
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

	/**
	 * Get all paths from this GraphNode to the given GraphNode.
	 * 
	 * @param end
	 * @return
	 */
	public Collection<List<GraphNode>> getPathsTo(GraphNode end) {
		Collection<List<GraphNode>> paths = new ArrayList<List<GraphNode>>();

		if (end != null) {
			if (end != this) {
				for (GraphNode child : this.getChildren()) {
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
	}

	/**
	 * Checks if the given child node is a descendant or equal to the graphNode.
	 * Maintains the directed acyclic state of the GraphNode. Example usage:
	 * 
	 * descendant.isDescendant(node) == true node.isDescendant(descendant) ==
	 * false
	 * 
	 * @param child
	 * @return
	 */
	public boolean isDescendant(GraphNode child) {
		if (child == this)
			return true;
		for (GraphNode subChild : child.getChildren()) {
			if (isDescendant(subChild))
				return true;
		}
		return false;
	}

	/**
	 * Returns a map containing each sub node and the distance from this node.
	 * This can be computational expensive on large graphs, recommend storing
	 * when the graph isn't changing
	 * 
	 * @return
	 */
	public Map<GraphNode, Integer> getNodeDepthMap() {
		Map<GraphNode, Integer> nodes = new IdentityHashMap<GraphNode, Integer>();

		List<GraphNode> children = this.getChildren();
		for (GraphNode child : children) {
			Map<GraphNode, Integer> childNodes = child.getNodeDepthMap();
			for (Entry<GraphNode, Integer> entry : childNodes.entrySet()) {
				final GraphNode childNode = entry.getKey();
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

	/**
	 * 
	 * @param observer
	 * @param observable
	 */
	public static void observeDepthMap(GraphNodeObserver observer,
			GraphNode observable) {
		Collection<GraphNode> nodes = observable.getNodeDepthMap().keySet();
		for (GraphNode childNode : nodes) {
			childNode.addGraphNodeObserver(observer);
		}
	}

	/**
	 * WeakReference wrapper used to track how many WeakReferences of each type
	 * are generated. This class provides no functionality, but it does make it
	 * easier for us to see where memory leaks may be occuring.
	 * 
	 * @author kschenk
	 * 
	 * @param <T>
	 */
	private class WeakGraphNodeObserverReference<T> extends WeakReference<T> {
		public WeakGraphNodeObserverReference(T referent) {
			super(referent);
		}
	}
}