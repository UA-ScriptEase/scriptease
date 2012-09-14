package scriptease.controller.observer.graph;

/**
 * Allows implementers to be notified of selection of nodes.
 * 
 * @author kschenk
 * 
 * @param <E>
 */
public interface SEGraphSelectionObserver {

	/**
	 * Called when a node has been selected.
	 * 
	 * @param node
	 *            The selected node.
	 */
	public void nodeSelected(Object node);

}
