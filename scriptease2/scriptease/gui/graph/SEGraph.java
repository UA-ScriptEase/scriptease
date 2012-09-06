package scriptease.gui.graph;

import java.util.Collection;

import javax.swing.JComponent;

/**
 * Builds a directed, acyclic graph that must have a start node.
 * 
 * @author kschenk
 * 
 * @param <E>
 */
@SuppressWarnings("serial")
public class SEGraph<E> extends JComponent {
	private final SEGraphModel<E> model;

	public SEGraph() {
		this.model = new SEGraphModel<E>();
	}

	public void addNode(E node) {
		this.model.addNode(node);
	}

	public void removeNode(E node) {
		this.model.removeNode(node);
	}

	public E getSelectedNode() {
		return this.model.getSelectedNode();
	}

	public Collection<E> getNodes() {
		return this.model.getNodes();
	}

}
