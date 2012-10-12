package scriptease.gui.SEGraph.observers;

import java.util.Collection;

/**
 * Allows implementers to be notified of changes to the graph. Use
 * {@link SEGraphAdapter} to only override certain methods.
 * 
 * @author kschenk
 * 
 */
public interface SEGraphObserver {

	/**
	 * Called when two nodes have been connected.
	 * 
	 * @param child
	 *            The new child
	 * @param parent
	 *            The new parent
	 */
	public void nodesConnected(Object child, Object parent);

	/**
	 * Called when two nodes have been disconnected.
	 * 
	 * @param child
	 *            The old child
	 * @param parent
	 *            The old parent
	 */
	public void nodesDisconnected(Object child, Object parent);

	/**
	 * Called when a node has been added to the graph.
	 * 
	 * @param newNode
	 *            The new node added to the graph
	 * @param children
	 *            The children of the new node
	 * @param parents
	 *            The parents of the new node
	 */
	public void nodeAdded(Object newNode, Collection<?> children,
			Collection<?> parents);

	/**
	 * Called when a node has been removed from the graph.
	 * 
	 * @param removedNode
	 *            The node that was removed from the graph.
	 */
	public void nodeRemoved(Object removedNode);

	/**
	 * Called when a node has been overwritten.
	 * 
	 * @param overwittenNode
	 */
	public void nodeOverwritten(Object overwittenNode);

	/**
	 * Called when a node has been selected.
	 * 
	 * @param node
	 *            The selected node.
	 */
	public void nodeSelected(Object node);

}
