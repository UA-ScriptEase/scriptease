package scriptease.controller.observer.graph;

import java.util.Collection;

/**
 * Adapter of {@link SEGraphObserver} so that we do not need to implement all of
 * the methods every time.
 * 
 * @author kschenk
 * 
 */
public abstract class SEGraphAdapter implements SEGraphObserver {

	@Override
	public void nodesConnected(Object child, Object parent) {
	}

	@Override
	public void nodesDisconnected(Object child, Object parent) {
	}

	@Override
	public void nodeAdded(Object newNode, Collection<?> children,
			Collection<?> parents) {
	}

	@Override
	public void nodeRemoved(Object removedNode) {
	}

	@Override
	public void nodeSelected(Object node) {
	}
}
