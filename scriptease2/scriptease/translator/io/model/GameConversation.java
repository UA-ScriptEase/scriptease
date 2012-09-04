package scriptease.translator.io.model;

import java.util.List;

/**
 * GameConversation represents a game independent conversation graph.
 * 
 * @author mfchurch
 * 
 */
public interface GameConversation extends GameConstant {

	/**
	 * Returns a list of all the possible conversation roots
	 * 
	 * @return 
	 */
	public List<GameConversationNode> getConversationRoots();
}
