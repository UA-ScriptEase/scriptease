package scriptease.gui.SEGraph.builders;

import java.util.Collection;

/**
 * A builder can build new nodes and get the children and parents of nodes.
 * 
 * @author kschenk
 * 
 * @param <E>
 */
public abstract class SEGraphNodeBuilder<E> {

	/**
	 * Builds a new node.
	 * 
	 * @return
	 */
	public abstract E buildNewNode();

	/**
	 * Gets the node's children.
	 * 
	 * @param node
	 * @return The node's children
	 */
	public abstract Collection<E> getChildren(E node);

	/**
	 * Gets the node's parents.
	 * 
	 * @param node
	 * @return The node's parents
	 */
	public abstract Collection<E> getParents(E node);

}
