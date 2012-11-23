package scriptease.model.atomic.describeits;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import scriptease.model.TypedComponent;
import scriptease.model.complex.ScriptIt;
import scriptease.util.ListOp;
import sun.awt.util.IdentityArrayList;

/**
 * A DescribeIt is a class that contains a graph of {@link DescribeItNode}s and
 * a mapping of paths of these nodes to {@link ScriptIt}s. DescribeIts cannot be
 * cloned, as they are merely maps of paths.
 * 
 * @author mfchurch
 * @author kschenk
 */
public class DescribeIt implements TypedComponent {
	private DescribeItNode startNode;
	private String name;
	private Map<Collection<DescribeItNode>, ScriptIt> paths;
	private Collection<String> types;
	// Longest path length used for calculating shortest path
	private final int INF_PATH_LENGTH = 100;

	public DescribeIt(String name, DescribeItNode startNode) {
		this(name, startNode, null, new ArrayList<String>());
	}

	public DescribeIt(String name, DescribeItNode startNode,
			Map<Collection<DescribeItNode>, ScriptIt> paths,
			Collection<String> types) {
		// assure the startNode is valid

		this.name = name;

		if (startNode != null)
			this.startNode = startNode;
		else
			throw new IllegalStateException(
					"Cannot initialize DescribeIt with a null StartNode");

		if (types == null)
			throw new NullPointerException(
					"Cannot initialize DescribeIt with null types.");

		this.types = types;

		// calculate paths if not given any
		if (paths != null && !paths.isEmpty()) {
			this.paths = new HashMap<Collection<DescribeItNode>, ScriptIt>(
					paths);
		} else {
			this.paths = new HashMap<Collection<DescribeItNode>, ScriptIt>();
		}
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
	 * Returns a path of DescribeItNodes for the passed in ScriptIt. If a path
	 * does not exist, an empty collection is returned.
	 * 
	 * @param scriptIt
	 * @return
	 */
	public Collection<DescribeItNode> getPath(ScriptIt scriptIt) {
		for (Entry<Collection<DescribeItNode>, ScriptIt> entry : this.paths
				.entrySet()) {
			if (entry.getValue().getDisplayText()
					.equals(scriptIt.getDisplayText())) {
				return entry.getKey();
			}
		}

		return new ArrayList<DescribeItNode>();
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

	/**
	 * Returns the name of the DescribeIt.
	 * 
	 * @return
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Sets the name of the DescribeIt.
	 * 
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Returns the possible types that the DescribeIt can resolve to, based on
	 * it's available scriptIts
	 */
	@Override
	public Collection<String> getTypes() {
		return this.types;
	}

	public void setTypes(Collection<String> types) {
		this.types.removeAll(this.types);
		this.types.addAll(types);
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
