package scriptease.model.atomic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CopyOnWriteArraySet;

import scriptease.controller.GraphNodeReferenceResolver;
import scriptease.controller.observer.GraphNodeEvent;
import scriptease.controller.observer.GraphNodeEvent.GraphNodeEventType;
import scriptease.controller.observer.GraphNodeObserver;
import scriptease.gui.graph.nodes.GraphNode;
import scriptease.gui.graph.nodes.TextNode;
import scriptease.model.complex.ScriptIt;
import scriptease.util.ListOp;
import sun.awt.util.IdentityArrayList;

/**
 * This class represents a <code>DescribeIt</code>. <code>DescribeIt</code>s
 * provide a selection interface for a collection of <code>ScriptIt</code>s.
 * They can only resolve to a single <code>ScriptIt</code> at one time, but
 * provide a graphical mechanism for selecting which <code>ScriptIt</code> it
 * represents.
 * 
 * @author mfchurch
 * @see {@link scriptease.model.atomic.KnowIt}
 * @see {@link scriptease.model.complex.ScriptIt}
 */
public class DescribeIt implements Cloneable, GraphNodeObserver {
	public static String DESCRIBES = "describes";
	private GraphNode headNode;
	private List<GraphNode> selectedPath;
	private List<GraphNode> defaultPath;
	private Map<List<GraphNode>, ScriptIt> paths;
	// Longest path length used for calculating shortest path
	private int INF_PATH_LENGTH = 100;

	public DescribeIt(GraphNode headNode) {
		this(headNode, null, null, null);
	}

	public DescribeIt(GraphNode headNode, Map<List<GraphNode>, ScriptIt> paths,
			List<GraphNode> defaultPath, List<GraphNode> selectedPath) {
		// assure the headNode is valid
		if (headNode != null)
			this.headNode = headNode;
		else
			throw new IllegalStateException(
					"Cannot initialize DescribeIt with a null HeadNode");
		// calculate paths if not given any
		if (paths != null && !paths.isEmpty()) {
			this.paths = new HashMap<List<GraphNode>, ScriptIt>(paths);
		} else {
			this.paths = new HashMap<List<GraphNode>, ScriptIt>();
			this.calculatePaths();
		}

		// assure default path is valid, otherwise take the shortest path
		if (defaultPath != null && this.containsPath(defaultPath))
			this.defaultPath = defaultPath;
		else
			this.defaultPath = this.getShortestPath();

		// assure the selected path is valid, otherwise use defaultPath
		if (selectedPath != null && this.containsPath(selectedPath))
			this.selectPath(selectedPath);
		else
			this.selectPath(this.defaultPath);
		this.commitSelection();

		// Observe nodes
		GraphNode.observeDepthMap(this, this.headNode);
	}

	/**
	 * Initialization method for cloning the DescribeIt
	 */
	private void init() {
		this.headNode = null;
		this.defaultPath = null;
		this.selectedPath = null;
		this.paths = new HashMap<List<GraphNode>, ScriptIt>();
	}

	/**
	 * Get the scriptIt value associated with the given path key. Returns null
	 * if no value is associated with that path
	 * 
	 * @param bPath
	 * @return
	 */
	public ScriptIt getScriptItForPath(List<GraphNode> bPath) {
		if (bPath != null) {
			for (Entry<List<GraphNode>, ScriptIt> entry : this.paths.entrySet()) {
				List<GraphNode> aPath = entry.getKey();
				if (ListOp.identityEqualLists(aPath, bPath))
					return entry.getValue();
			}
		}
		return null;
	}

	/**
	 * Returns the keySet of paths in the path map
	 * 
	 * @return
	 */
	public Collection<List<GraphNode>> getPaths() {
		return this.paths.keySet();
	}

	/**
	 * Returns the collection of non-null scriptIt values in path map
	 * 
	 * @return
	 */
	public Collection<ScriptIt> getScriptIts() {
		// filter out null values
		ArrayList<ScriptIt> scriptIts = new ArrayList<ScriptIt>();
		for (ScriptIt scriptIt : this.paths.values())
			if (scriptIt != null)
				scriptIts.add(scriptIt);
		return scriptIts;
	}

	/**
	 * Replacement for this.paths.containsKey(path) since IdentityArrays don't
	 * actual work with object ids...
	 * 
	 * @param path
	 * @return
	 */
	private boolean containsPath(List<GraphNode> path) {
		if (this.paths != null)
			return ListOp.identityContains(this.paths.keySet(), path);
		return false;
	}

/*	TODO: Selection colour should be done in GraphPanel
 * 
 * 
 * private Color getSelectionColourForPath(List<GraphNode> path) {
		if (this.containsPath(path))
			return ScriptEaseUI.COLOUR_GAME_OBJECT;
		else
			return ScriptEaseUI.COLOUR_UNBOUND;
	}*/

	/**
	 * Selects the nodes from the head to the provided end node using the best
	 * possible path to node.
	 * 
	 * @see getBestPathToNode()
	 * @param selectedNode
	 */
	public void selectFromHeadToNode(GraphNode endNode) {
		// Find the best path based on the current selection state
		List<GraphNode> newSelection = this.headNode.getBestPathToNode(endNode);

		selectPath(newSelection);
	}

	/**
	 * Selects the nodes from the provided path. Assumes all nodes are
	 * descendants of the headNode.
	 * 
	 * @param path
	 */
	public void selectPath(List<GraphNode> path) {
		// Clear the selection
		this.clearSelection();
		// Select the found path
		for (GraphNode node : path) {
	//TODO: Set selection Colour
			//node.setSelectedColour(getSelectionColourForPath(path));
			node.setSelected(true);
		}
	}

	/**
	 * Commits the path built from selected nodes as a the selected path. A
	 * valid commit must have nodes selected and exist in the path map, and have
	 * a valid scriptIt value mapped to the path key. Returns true if the commit
	 * was successful
	 * 
	 * @param selectedPath
	 */
	public boolean commitSelection() {
		List<GraphNode> selectedNodes = this.buildPathFromSelectedNodes();
		if (!selectedNodes.isEmpty()
				&& !selectedNodes.equals(this.selectedPath)
				&& this.paths.get(selectedNodes) != null) {
			// clear bindings on the previous selectedPath
			this.clearBindingsOnPath(this.selectedPath);
			// update the selected binding
			this.setSelectedPath(selectedNodes);
			return true;
		}
		return false;
	}

	public void setSelectedPath(List<GraphNode> path) {
		selectPath(path);
		this.selectedPath = path;
	}

	/**
	 * Clears the given Path's scriptIt's parameters
	 * 
	 * @param path
	 */
	private void clearBindingsOnPath(List<GraphNode> path) {
		final ScriptIt scriptIt = this.getScriptItForPath(path);
		if (scriptIt != null) {
			for (KnowIt parameter : scriptIt.getParameters())
				parameter.clearBinding();
		}
	}

	/**
	 * Constructs a single path from the selected nodes in the graph. Assumes
	 * only a single path is selected, and will return the first path found.
	 * 
	 * @return
	 */
	public List<GraphNode> buildPathFromSelectedNodes() {
		final List<GraphNode> selected = new IdentityArrayList<GraphNode>();
		GraphNode currentNode = this.headNode;
		while (currentNode.isSelected()) {
			final List<GraphNode> children = currentNode.getChildren();
			selected.add(currentNode);
			if (children.isEmpty())
				break;

			for (GraphNode child : children) {
				currentNode = child;
				if (child.isSelected()) {
					break;
				}
			}
		}
		return selected;
	}

	/**
	 * Returns the possible types that the DescribeIt can resolve to, based on
	 * it's avaliable scriptIts
	 */
	public Collection<String> getTypes() {
		Collection<String> types = new CopyOnWriteArraySet<String>();
		final Collection<ScriptIt> scriptIts = this.getScriptIts();
		for (ScriptIt scriptIt : scriptIts) {
			types.addAll(scriptIt.getTypes());
		}
		return types;
	}

	/**
	 * Returns the resolved ScriptIt value for the current selectedPath key in
	 * the paths map
	 * 
	 * @return
	 */
	public ScriptIt getResolvedScriptIt() {
		if (this.selectedPath != null && this.containsPath(this.selectedPath))
			return this.paths.get(this.selectedPath);
		else
			return null;
	}

	/**
	 * Get the headNode of the DescribeIt's graph
	 * 
	 * @return
	 */
	public GraphNode getHeadNode() {
		return this.headNode;
	}

	public void setDefaultPath(List<GraphNode> defaultPath) {
		if (this.containsPath(defaultPath))
			this.defaultPath = defaultPath;
		else
			System.err.println("Cannot set default path to non-valid path ["
					+ defaultPath + "]");
	}

	public List<GraphNode> getDefaultPath() {
		return this.defaultPath;
	}

	/**
	 * Calculates the shortest path in the path map, returns an empty path if no
	 * paths exist or no paths exist which are shorter than INF_PATH_LENGTH
	 * 
	 * @return
	 */
	private List<GraphNode> getShortestPath() {
		List<GraphNode> shortestPath = new IdentityArrayList<GraphNode>();
		int shortestPathSize = this.INF_PATH_LENGTH;
		for (List<GraphNode> path : this.paths.keySet()) {
			int size = path.size();
			if (size < shortestPathSize) {
				shortestPathSize = size;
				shortestPath = path;
			}
		}
		return shortestPath;
	}

	/**
	 * Assigns the given scriptIt to the given path iff every parameter in the
	 * scriptIt has a matching KnowItNode, and the path exists in the path map
	 * 
	 * @param path
	 * @param scriptIt
	 */
	public void assignScriptItToPath(List<GraphNode> path, ScriptIt scriptIt) {
		if (this.containsPath(path))
			this.paths.put(path, scriptIt);
	}

	/**
	 * Populates the paths maps based on the current HeadNode
	 */
	private void calculatePaths() {
		if (this.headNode != null) {
			Collection<List<GraphNode>> newPaths = calculatePathsFromNode(this.headNode);

			// remove old paths from the map
			Collection<List<GraphNode>> oldPaths = new IdentityArrayList<List<GraphNode>>();
			for (List<GraphNode> path : this.paths.keySet()) {
				// if the path is no longer valid, queue for removal
				if (!ListOp.identityContains(newPaths, path))
					oldPaths.add(path);
			}
			for (List<GraphNode> oldPath : oldPaths) {
				this.paths.remove(oldPath);
			}

			// add new paths
			for (List<GraphNode> newPath : newPaths) {
				// if the path is already in the map, keep it's assigned
				// scriptIt
				boolean contains = ListOp.identityContains(this.paths.keySet(),
						newPath);
				if (!contains)
					this.paths.put(newPath, null);
			}

			// bold path name before it splits (needs to be done when ever the
			// paths change)
			for (List<GraphNode> path : this.paths.keySet()) {
				// clear each path
				setBoldPath(path, false);  
			}
			setBoldPath(getPathBeforeBranch(this.headNode), true);
		}
	} 

	/**
	 * Gets the path from the headNode to the first descendant which has more
	 * than a single child
	 * 
	 * @param headNode
	 * @return
	 */
	private List<GraphNode> getPathBeforeBranch(GraphNode headNode) {
		final List<GraphNode> path = new IdentityArrayList<GraphNode>();
		path.add(headNode);

		GraphNode child = headNode;
		while (child.getChildren().size() == 1) {
			child = child.getChildren().iterator().next();
			path.add(child);
		}
		return path;
	}

	/**
	 * Sets the textNodes on the given path to the given bold setting.
	 * 
	 * @param path
	 * @param shouldBold
	 */
	private void setBoldPath(List<GraphNode> path, boolean shouldBold) {
		for (GraphNode node : path) {
			if (node instanceof TextNode)
				((TextNode) node).setBoldStatus(shouldBold);
		}
	}

	/**
	 * Calculates all of the paths from the given node to the tail of the graph
	 * and returns them as a Collection of single child head DescribeItNodes.
	 * 
	 * @param node
	 * @return TODO:: this can be swapped out for GraphNode's
	 *         getPathsTo(GraphNode) method after calculating all of the tail
	 *         nodes
	 */
	private Collection<List<GraphNode>> calculatePathsFromNode(GraphNode node) {
		Collection<List<GraphNode>> paths = new ArrayList<List<GraphNode>>();
		// safety
		if (node != null) {
			final Collection<GraphNode> children = node.getChildren();
			// tail of graph
			if (children.isEmpty()) {
				// create a one node path with the tail
				List<GraphNode> tailPath = new IdentityArrayList<GraphNode>();
				tailPath.add(node);
				// add the tailPath as the only path
				paths.add(tailPath);
			} else {
				// for each child node
				for (GraphNode child : children) {
					// recursively build path to tail from children
					Collection<List<GraphNode>> subPaths = calculatePathsFromNode(child);

					// for each subPath
					for (List<GraphNode> subPath : subPaths) {
						// if the node is terminal and has children, there are
						// two distinct paths
						if (node.isTerminalNode()) {
							List<GraphNode> newPath = new IdentityArrayList<GraphNode>();
							newPath.add(node);
							paths.add(newPath);
						}

						// add the path to be returned
						subPath.add(0, node);
						paths.add(subPath);
					}
				}
			}
		}
		return paths;
	}

	@Override
	public DescribeIt clone() {
		DescribeIt clone = null;
		try {
			clone = (DescribeIt) super.clone();
		} catch (CloneNotSupportedException e) {
			// I can't think of a better way to deal with this
			Thread.getDefaultUncaughtExceptionHandler().uncaughtException(
					Thread.currentThread(), e);
		}

		// initialize the clone
		clone.init();

		// clone the graph
		clone.headNode = this.headNode.clone();

		// resolve references in the clone graph
		GraphNodeReferenceResolver resolver = new GraphNodeReferenceResolver();
		resolver.resolveReferences(clone.headNode);

		// clone the paths
		clone.calculatePaths();
		for (List<GraphNode> key : this.paths.keySet()) {
			final ScriptIt scriptIt = this.getScriptItForPath(key);
			if (scriptIt != null) {
				for (List<GraphNode> cloneKey : clone.paths.keySet()) {
					if (cloneKey.equals(key)) {
						clone.assignScriptItToPath(cloneKey,
								scriptIt.clone());
						break;
					}
				}
			}
		}

		// set the default path
		if (this.defaultPath != null && !this.defaultPath.isEmpty()) {
			for (List<GraphNode> cloneKey : clone.paths.keySet()) {
				if (cloneKey.equals(this.defaultPath)) {
					clone.defaultPath = cloneKey;
					break;
				}
			}
		}

		// set the selected path
		if (this.selectedPath != null && !this.selectedPath.isEmpty()) {
			for (List<GraphNode> cloneKey : clone.paths.keySet()) {
				if (cloneKey.equals(this.selectedPath)) {
					clone.selectedPath = cloneKey;
					break;
				}
			}
		}

		// Observe nodes
		GraphNode.observeDepthMap(clone, clone.headNode);

		return clone;
	}

	@Override
	public void nodeChanged(GraphNodeEvent event) {
		final GraphNode sourceNode = event.getSource();
		final GraphNodeEventType type = event.getEventType();
		// TODO: optimize so it doesn't recalculate all paths, only those
		// affected by the node changes
		if (type == GraphNodeEventType.CONNECTION_ADDED) {
			sourceNode.addGraphNodeObserver(this);
			this.calculatePaths();
		} else if (type == GraphNodeEventType.CONNECTION_REMOVED) {
			sourceNode.removeGraphNodeObserver(this);
			this.calculatePaths();
		}
	}

	@Override
	public String toString() {
		return "DescribeIt [" + this.headNode + "]";
	}

	/**
	 * Returns a copy of the currently committed selectedPath
	 * 
	 * @return
	 */
	public List<GraphNode> getSelectedPath() {
		if (this.selectedPath != null)
			return new IdentityArrayList<GraphNode>(this.selectedPath);
		return new IdentityArrayList<GraphNode>();
	}

	public void clearSelection() {
		this.headNode.setSelected(false);
		Collection<GraphNode> nodes = this.headNode.getNodeDepthMap().keySet();
		for (GraphNode node : nodes) {
			node.setSelected(false);
		}
	}
}
