package scriptease.gui.SEGraph.observers;

import java.util.Collection;

/**
 * Allows implementers to be notified of changes to the graph. Use
 * {@link SEGraphAdapter} to only override certain methods.
 * 
 * @author kschenk
 * @author jyuen
 */
public interface SEGraphObserver<E> {

	/**
	 * Called when two nodes have been connected.
	 * 
	 * @param child
	 *            The new child
	 * @param parent
	 *            The new parent
	 */
	public void nodesConnected(E child, E parent);

	/**
	 * Called when two nodes have been disconnected.
	 * 
	 * @param child
	 *            The old child
	 * @param parent
	 *            The old parent
	 */
	public void nodesDisconnected(E child, E parent);

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
	public void nodeAdded(E newNode, Collection<E> children,
			Collection<E> parents);

	/**
	 * Called when a node has been removed from the graph.
	 * 
	 * @param removedNode
	 *            The node that was removed from the graph.
	 */
	public void nodeRemoved(E removedNode);

	/**
	 * Called when a node has been overwritten.
	 * 
	 * @param overwittenNode
	 */
	public void nodeOverwritten(E overwittenNode);

	/**
	 * Called when nodes have been selected. In a graph with single node
	 * selection mode enabled, the nodes only contain one node.
	 * 
	 * @param nodes
	 *            The selected nodes, in order from parents to children.
	 */
	public void nodesSelected(Collection<E> nodes);

	/**
	 * Called when a set of nodes have been grouped together.
	 * 
	 * @param nodes
	 */
	public void nodesGrouped(Collection<E> nodes);

	/**
	 * Called when a set of nodes have been un-grouped.
	 * 
	 * @param nodes
	 */
	public void nodesUngrouped(Collection<E> nodes);
}
