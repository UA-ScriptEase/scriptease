package scriptease.model.atomic.describeits;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CopyOnWriteArraySet;

import scriptease.model.atomic.KnowIt;
import scriptease.model.complex.ScriptIt;
import scriptease.util.ListOp;
import sun.awt.util.IdentityArrayList;

/**
 * A DescribeIt is a class that contains a graph of {@link DescribeItNode}s and
 * a mapping of paths of these nodes to {@link ScriptIt}s.
 * 
 * @author mfchurch
 * @author kschenk
 */
public class DescribeIt implements Cloneable {
	// TODO Whoa there. What in the world is this public string? Get rid of it!
	public static String DESCRIBES = "describes";
	private DescribeItNode startNode;
	private Map<Collection<DescribeItNode>, ScriptIt> paths;
	// Longest path length used for calculating shortest path
	private final int INF_PATH_LENGTH = 100;

	private Collection<DescribeItNode> selectedPath;

	public DescribeIt(DescribeItNode startNode) {
		this(startNode, null, null);
	}

	public DescribeIt(DescribeItNode startNode,
			Map<Collection<DescribeItNode>, ScriptIt> paths,
			Collection<DescribeItNode> selectedPath) {
		// assure the startNode is valid
		if (startNode != null)
			this.startNode = startNode;
		else
			throw new IllegalStateException(
					"Cannot initialize DescribeIt with a null StartNode");
		// calculate paths if not given any
		if (paths != null && !paths.isEmpty()) {
			this.paths = new HashMap<Collection<DescribeItNode>, ScriptIt>(
					paths);
		} else {
			this.paths = new HashMap<Collection<DescribeItNode>, ScriptIt>();
		}

		// assure the selected path is valid, otherwise use defaultPath
		if (selectedPath != null && this.containsPath(selectedPath))
			this.setSelectedPath(selectedPath);
		else
			this.setSelectedPath(this.getShortestPath());
	}

	/**
	 * Get the scriptIt value associated with the given path key. Returns null
	 * if no value is associated with that path
	 * 
	 * @param bPath
	 * @return
	 */
	public ScriptIt getScriptItForPath(Collection<DescribeItNode> bPath) {
		if (bPath != null && this.containsPath(bPath)) {
			for (Entry<Collection<DescribeItNode>, ScriptIt> entry : this.paths
					.entrySet()) {
				Collection<DescribeItNode> aPath = entry.getKey();

				List<DescribeItNode> aList = new ArrayList<DescribeItNode>(
						aPath);
				List<DescribeItNode> bList = new ArrayList<DescribeItNode>(
						bPath);

				if (ListOp.identityEqualLists(aList, bList))
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
	public Collection<Collection<DescribeItNode>> getPaths() {
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
	private boolean containsPath(Collection<DescribeItNode> path) {
		if (this.paths != null) {
			for (Collection<DescribeItNode> existingPath : this.paths.keySet()) {
				if (existingPath.containsAll(path)
						&& existingPath.size() == path.size())
					return true;
			}
		}

		return false;
	}

	@Override
	public DescribeIt clone() {
		DescribeIt clone = null;

		try {
			clone = (DescribeIt) super.clone();

		} catch (CloneNotSupportedException e) {
			Thread.getDefaultUncaughtExceptionHandler().uncaughtException(
					Thread.currentThread(), e);
		}

		clone.startNode = this.startNode.clone();
		// FIXME: We're making new DescribeItNodes and adding them to paths when
		// we should be creating paths based on existing nodes from cloning the
		// start node. This breaks stuff.

		// We need to find out if nodes are equal. Let's override the hashcode
		// method in DescribeItNode, and then we can add the correct
		// describeitnode to paths instead of cloning them again.
		clone.paths = new HashMap<Collection<DescribeItNode>, ScriptIt>();

		for (Entry<Collection<DescribeItNode>, ScriptIt> entry : this.paths
				.entrySet()) {
			final List<DescribeItNode> describeItNodes;

			describeItNodes = new ArrayList<DescribeItNode>();

			for (DescribeItNode node : entry.getKey()) {
				// FIXME: here we should be finding existing clones, not
				// creating new ones.
				for (DescribeItNode existingNode : clone.startNode
						.getDescendants()) {
					if (existingNode.equals(node)) {
						describeItNodes.add(existingNode);
						break;
					}
				}
			}

			clone.paths.put(describeItNodes, entry.getValue().clone());
		}

		clone.selectedPath = new ArrayList<DescribeItNode>();

		for (DescribeItNode selected : this.getSelectedPath()) {
			clone.selectedPath.add(selected.clone());
		}

		return clone;
	}

	/**
	 * Returns a copy of the currently committed selectedPath
	 * 
	 * @return
	 */
	public Collection<DescribeItNode> getSelectedPath() {
		if (this.selectedPath != null)
			return new IdentityArrayList<DescribeItNode>(this.selectedPath);
		return new IdentityArrayList<DescribeItNode>();
	}

	public boolean setSelectedPath(Collection<DescribeItNode> path) {
		if (!(path == null || path.isEmpty()) && this.paths.get(path) != null) {
			// clear bindings on the previous selectedPath
			final ScriptIt scriptIt;

			scriptIt = this.getScriptItForPath(path);

			if (scriptIt != null) {
				for (KnowIt parameter : scriptIt.getParameters())
					parameter.clearBinding();
			}

			this.selectedPath = path;

			return true;
		}

		return false;
	}

	public void clearSelection() {
		this.setSelectedPath(new ArrayList<DescribeItNode>());
	}

	/**
	 * Returns the possible types that the DescribeIt can resolve to, based on
	 * it's available scriptIts
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
	 * Get the startNode of the DescribeIt's graph
	 * 
	 * @return
	 */
	public DescribeItNode getStartNode() {
		return this.startNode;
	}

	/**
	 * Calculates the shortest path in the path map, returns an empty path if no
	 * paths exist or no paths exist which are shorter than INF_PATH_LENGTH
	 * 
	 * @return
	 */
	public Collection<DescribeItNode> getShortestPath() {
		Collection<DescribeItNode> shortestPath;

		shortestPath = new IdentityArrayList<DescribeItNode>();

		int shortestPathSize = this.INF_PATH_LENGTH;
		for (Collection<DescribeItNode> path : this.paths.keySet()) {
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
	 * scriptIt has a matching KnowItNode. If the path does not exist yet, adds
	 * it to the map.
	 * 
	 * @param path
	 * @param scriptIt
	 */
	public void assignScriptItToPath(Collection<DescribeItNode> path,
			ScriptIt scriptIt) {
		this.paths.put(path, scriptIt);
	}

	@Override
	public String toString() {
		return "DescribeIt [" + this.startNode + "]";
	}
}
