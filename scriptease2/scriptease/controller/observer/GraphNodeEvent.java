package scriptease.controller.observer;

import scriptease.gui.graph.nodes.GraphNode;

public final class GraphNodeEvent {
	private final GraphNode source;
	private final GraphNodeEventType eventType;

	public static enum GraphNodeEventType {
		SELECTED, CONNECTION_ADDED, CONNECTION_REMOVED
	}

	public GraphNodeEvent(GraphNode source, GraphNodeEventType type) {
		this.source = source;
		this.eventType = type;
	}

	/**
	 * 
	 * @return
	 */
	public GraphNode getSource() {
		return this.source;
	}

	public GraphNodeEventType getEventType() {
		return this.eventType;
	}

	@Override
	public String toString() {
		return "GraphNodeEvent [" + this.source + ", " + this.eventType;
	}
}
