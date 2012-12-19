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
	 * <code>{@link #getChildren()}.size() == 0</code>
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

	/**
	 * Returns a string representing the speaker. Depending on the translator,
	 * this may be the speaker's actual name, or another String that represents
	 * the speaker.
	 * 
	 * @return A String representing the speaker.
	 */
	public String getSpeaker();
}
