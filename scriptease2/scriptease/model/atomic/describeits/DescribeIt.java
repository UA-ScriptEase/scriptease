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
	public static String DESCRIBES = "describes";
	private DescribeItNode headNode;
	private List<DescribeItNode> selectedPath;
	private Map<List<DescribeItNode>, ScriptIt> paths;
	// Longest path length used for calculating shortest path
	private int INF_PATH_LENGTH = 100;

	public DescribeIt(DescribeItNode headNode) {
		this(headNode, null, null, null);
	}

	public DescribeIt(DescribeItNode headNode,
			Map<List<DescribeItNode>, ScriptIt> paths,
			List<DescribeItNode> defaultPath, List<DescribeItNode> selectedPath) {
		// assure the headNode is valid
		if (headNode != null)
			this.headNode = headNode;
		else
			throw new IllegalStateException(
					"Cannot initialize DescribeIt with a null HeadNode");
		// calculate paths if not given any
		if (paths != null && !paths.isEmpty()) {
			this.paths = new HashMap<List<DescribeItNode>, ScriptIt>(paths);
		} else {
			this.paths = new HashMap<List<DescribeItNode>, ScriptIt>();
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
	public ScriptIt getScriptItForPath(List<DescribeItNode> bPath) {
		if (bPath != null) {
			for (Entry<List<DescribeItNode>, ScriptIt> entry : this.paths
					.entrySet()) {
				List<DescribeItNode> aPath = entry.getKey();
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
	public Collection<List<DescribeItNode>> getPaths() {
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
	private boolean containsPath(List<DescribeItNode> path) {
		if (this.paths != null)
			return ListOp.identityContains(this.paths.keySet(), path);
		return false;
	}

	@Override
	public DescribeIt clone() {
		// TODO Need to clone these somehow.
		return this;
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

	public boolean setSelectedPath(List<DescribeItNode> path) {
		if (this.selectedPath == null
				|| (!this.selectedPath.isEmpty() && this.paths
						.get(this.selectedPath) != null)) {
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

	// TODO Rename start node because head node sounds stupid
	/**
	 * Get the headNode of the DescribeIt's graph
	 * 
	 * @return
	 */
	public DescribeItNode getHeadNode() {
		return this.headNode;
	}

	/**
	 * Calculates the shortest path in the path map, returns an empty path if no
	 * paths exist or no paths exist which are shorter than INF_PATH_LENGTH
	 * 
	 * @return
	 */
	public List<DescribeItNode> getShortestPath() {
		List<DescribeItNode> shortestPath = new IdentityArrayList<DescribeItNode>();
		int shortestPathSize = this.INF_PATH_LENGTH;
		for (List<DescribeItNode> path : this.paths.keySet()) {
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
	public void assignScriptItToPath(List<DescribeItNode> path,
			ScriptIt scriptIt) {
		if (this.containsPath(path))
			this.paths.put(path, scriptIt);
	}

	@Override
	public String toString() {
		return "DescribeIt [" + this.headNode + "]";
	}
}
