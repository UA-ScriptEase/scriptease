package scriptease.controller.observer;

import scriptease.gui.graph.nodes.GraphNode;

public final class GraphNodeEvent {
	private final GraphNode source;
	private final short eventType;

	public static final short CLICKED = 0;
	public static final short CONNECTION_ADDED = 1;
	public static final short CONNECTION_REMOVED = 2;
	public static final short APPEARANCE_CHANGED = 3;

	public GraphNodeEvent(GraphNode source, short type) {
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

	public short getEventType() {
		return this.eventType;
	}

	@Override
	public String toString() {
		return "GraphNodeEvent [" + this.source + ", " + this.eventType;
	}
}
