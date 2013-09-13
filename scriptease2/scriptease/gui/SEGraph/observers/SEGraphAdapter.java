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
	
	public void defaultHandler() {
	}

	@Override
	public void nodesConnected(E child, E parent) {
		this.defaultHandler();
	}

	@Override
	public void nodesDisconnected(E child, E parent) {
		this.defaultHandler();
	}

	@Override
	public void nodeOverwritten(E overwittenNode) {
		this.defaultHandler();
	}

	@Override
	public void nodeAdded(E newNode, Collection<E> children,
			Collection<E> parents) {
		this.defaultHandler();
	}

	@Override
	public void nodeRemoved(E removedNode) {
		this.defaultHandler();
	}

	@Override
	public void nodesSelected(Collection<E> nodes) {
		this.defaultHandler();
	}

	@Override
	public void nodesGrouped(Collection<E> nodes) {
		this.defaultHandler();
	}

	@Override
	public void nodesUngrouped(Collection<E> nodes) {
		this.defaultHandler();
	}
}
