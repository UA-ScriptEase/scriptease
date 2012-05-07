package scriptease.controller.observer;

import scriptease.gui.graph.nodes.GraphNode;

/**
 * 
 * @author mfchurch
 *
 */
public interface GraphNodeObserver {
	/**
	 * Note: node is the caller of the nodeChanged function, not necessarily the source node contained 
	 * @param node
	 * @param event
	 */
	public void nodeChanged(GraphNode node, GraphNodeEvent event);
}
