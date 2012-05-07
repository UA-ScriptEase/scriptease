package scriptease.controller;

import java.util.Collection;

import scriptease.controller.observer.GraphNodeObserver;
import scriptease.gui.graph.nodes.GraphNode;

/*****************************************************
 * Helper Class used for applying different levels of GraphNode Observation on a
 * given observable
 * 
 * @author mfchurch
 *****************************************************/

public class GraphNodeObserverAdder {

	public void observeDepthMap(GraphNodeObserver observer,
			GraphNode observable) {
		Collection<GraphNode> nodes = observable.getNodeDepthMap().keySet();
		for (GraphNode childNode : nodes) {
			childNode.addGraphNodeObserver(observer);
		}
	}
}
