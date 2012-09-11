package scriptease.controller.observer.graph;

/**
 * Adapter class for {@link SEGraphObserver} so that we do not need to have
 * every method created for our listeners.
 * 
 * @author kschenk
 * 
 * @param <E>
 */
public abstract class SEGraphAdapter<E> implements SEGraphObserver<E> {

	/**
	 * {@inheritDoc}
	 */
	public void childAdded(E child, E parent) {
	};

	/**
	 * {@inheritDoc}
	 */
	public void childRemoved(E child, E parent) {
	};

	/**
	 * {@inheritDoc}
	 */
	public void nodeSelected(E node) {
	};

	/**
	 * {@inheritDoc}
	 */
	public void parentAdded(E child, E parent) {
	};

	/**
	 * {@inheritDoc}
	 */
	public void parentRemoved(E child, E parent) {
	};
}
