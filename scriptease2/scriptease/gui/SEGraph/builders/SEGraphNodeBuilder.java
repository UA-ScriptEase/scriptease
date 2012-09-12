package scriptease.gui.SEGraph.builders;

import java.util.Collection;

/**
 * A builder can build new nodes and get the children and parents of nodes.
 * 
 * @author kschenk
 * 
 * @param <E>
 */
public interface SEGraphNodeBuilder<E> {

	/**
	 * Builds a new node.
	 * 
	 * @return
	 */
	public E buildNewNode();

	/**
	 * Adds a listener to the node. Be sure to check types before casting.
	 */
	public void addNodeObserver(E node, Object observer);

	/**
	 * Removes a listener from the node. Be sure to check types before casting.
	 */
	public void removeNodeObserver(E node, Object observer);

	/**
	 * Gets the node's children.
	 * 
	 * @param node
	 * @return The node's children
	 */
	public Collection<E> getChildren(E node);

	/**
	 * Gets the node's parents.
	 * 
	 * @param node
	 * @return The node's parents
	 */
	public Collection<E> getParents(E node);

}
