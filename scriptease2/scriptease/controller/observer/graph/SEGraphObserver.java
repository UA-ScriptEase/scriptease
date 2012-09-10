package scriptease.controller.observer.graph;

/**
 * Allows implementers to be notified of changes to Graph NOdes.
 * 
 * @author kschenk
 * 
 * @param <E>
 */
public interface SEGraphObserver<E> {

	/**
	 * Called when a node has been selected.
	 * 
	 * @param node
	 *            The selected node.
	 */
	public void nodeSelected(E node);

	/**
	 * Called when a child has been added to a node.
	 * 
	 * @param child
	 *            The child added.
	 * @param parent
	 *            The parent.
	 */
	public void childAdded(E child, E parent);

	/**
	 * Called when a child has been removed from a node.
	 * 
	 * @param child
	 *            The child removed.
	 * @param parent
	 *            The parent.
	 */
	public void childRemoved(E child, E parent);

	/**
	 * Called when a parent has been added to a node.
	 * 
	 * @param child
	 *            The child.
	 * @param parent
	 *            The parent added.
	 */
	public void parentAdded(E child, E parent);

	/**
	 * Called when a parent has been removed from a node.
	 * 
	 * @param child
	 *            The child.
	 * @param parent
	 *            The parent removed.
	 */
	public void parentRemoved(E child, E parent);

}
