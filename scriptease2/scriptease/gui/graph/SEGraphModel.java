package scriptease.gui.graph;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Model class for SEGraph. This stores and handles all of the nodes in the
 * Graph.
 * 
 * @author kschenk
 * 
 * @param <E>
 */
public class SEGraphModel<E> {

	private E selectedNode;
	private final Collection<E> nodes;

	public SEGraphModel() {
		this.nodes = new ArrayList<E>();
	}

	public void addNode(E node) {
		this.nodes.add(node);
	}

	public void removeNode(E node) {
		this.nodes.remove(node);
	}

	public E getSelectedNode() {
		return this.selectedNode;
	}

	public Collection<E> getNodes() {
		return this.nodes;
	}
}
