package scriptease.gui.SEGraph.observers;

/**
 * Gets the event and determines what to do.
 * 
 * 
 * @author mfchurch
 * @deprecated THIS IS OLD!
 */

public interface GraphNodeObserver {
	/**
	 * Note: node is the caller of the nodeChanged function, not necessarily the
	 * source node contained
	 * 
	 * @param node
	 * @param event
	 * 
	 */
	public void nodeChanged(GraphNodeEvent event);
}
