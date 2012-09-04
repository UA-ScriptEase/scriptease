package scriptease.translator.io.model;

import java.util.List;

/**
 * Representation of a node in a specific game dialogue.
 * 
 * @author mfchurch
 */
public interface GameConversationNode extends GameConstant {
	/**
	 * Get the children (replies) to this conversation node
	 */
	public List<? extends GameConversationNode> getChildren();

	/**
	 * Determines if this node is a terminal node. This is identical to
	 * <code>{@link #getChildren()} == 0</code>
	 * 
	 * @return <code>true</code> if the node is terminal.
	 */
	public boolean isTerminal();

	/**
	 * Determines if this node is a link back to another location in the
	 * dialogue graph.
	 * 
	 * @return <code>true</code> if the node is a link.
	 */
	public boolean isLink();
}
