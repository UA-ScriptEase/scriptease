package scriptease.gui.SEGraph.observers;

import java.util.Collection;

/**
 * Adapter of {@link SEGraphObserver} so that we do not need to implement all of
 * the methods every time.
 * 
 * @author kschenk
 * @author jyuen
 */
public abstract class SEGraphAdapter<E> implements SEGraphObserver<E> {

	@Override
	public void nodesConnected(E child, E parent) {
	}

	@Override
	public void nodesDisconnected(E child, E parent) {
	}

	@Override
	public void nodeOverwritten(E overwittenNode) {
	}

	@Override
	public void nodeAdded(E newNode, Collection<E> children,
			Collection<E> parents) {
	}

	@Override
	public void nodeRemoved(E removedNode) {
	}

	@Override
	public void nodesSelected(Collection<E> nodes) {
	}

	@Override
	public void nodesGrouped(Collection<E> nodes) {
	}

	@Override
	public void nodesUngrouped(Collection<E> nodes) {
	}
}
