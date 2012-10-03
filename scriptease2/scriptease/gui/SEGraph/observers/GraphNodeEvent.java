package scriptease.gui.SEGraph.observers;

import scriptease.gui.SEGraph.nodes.GraphNode;

/**
 * @deprecated This needs to be removed. We have SEGraph now, which is more
 *             coder-friendly and does more things.
 */
public final class GraphNodeEvent {
	private final GraphNode source;
	private final GraphNodeEventType eventType;
	private boolean isShiftDown;

	/**
	 * @deprecated This needs to be removed. We have SEGraph now, which is more
	 *             coder-friendly and does more things.
	 */
	public static enum GraphNodeEventType {
		SELECTED,
		CONNECTION_ADDED,
		CONNECTION_REMOVED
	}

	/**
	 * @deprecated This needs to be removed. We have SEGraph now, which is more
	 *             coder-friendly and does more things.
	 * @param source
	 * @param type
	 */
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
