package scriptease.controller.observer.graph;

import scriptease.gui.graph.nodes.GraphNode;

public final class GraphNodeEvent {
	private final GraphNode source;
	private final GraphNodeEventType eventType;
	private boolean isShiftDown;

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
	
	public Boolean isShiftDown() {
		return this.isShiftDown;
	}
	
	public void setShiftDown(boolean isShiftDown) {
		this.isShiftDown = isShiftDown;
	}
	@Override
	public String toString() {
		return "GraphNodeEvent [" + this.source + ", " + this.eventType;
	}
}