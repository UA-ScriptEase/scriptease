package scriptease.gui.graph.builders;

/**
 * Builds a new node for the SEGraph. This class must be subclassed, and the
 * {@link #buildNewNode()} method overidden for new nodes to properly be added
 * to the graph.
 * 
 * @author kschenk
 * 
 * @param <E>
 */
public class SEGraphNodeBuilder<E> {
	
	/**
	 * Builds a new node. By default, this returns null and must be overidden.
	 * @return
	 */
	public E buildNewNode() {
		return null;
	}
}
