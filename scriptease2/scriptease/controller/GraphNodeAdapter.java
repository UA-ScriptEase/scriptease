package scriptease.controller;

import scriptease.gui.SEGraph.nodes.GraphNode;
import scriptease.gui.SEGraph.nodes.KnowItNode;
import scriptease.gui.SEGraph.nodes.TextNode;

/**
 * Default implementation of BindingVisitor that does nothing. Ever. <br>
 * <br>
 * It is <b>stylistically required</b> that all other GraphNodeVisitor
 * implementations extend this class, allowing us to avoid having to update all
 * of the visitors whenever the interface changes. Subclasses also get the perk
 * of only having to override the methods they <i>do</i> support.<br>
 * <br>
 * Subclasses that wish to provide default behaviour for processing can override
 * {@link #defaultProcess(GraphNode)}. <br>
 * <br>
 * AbstractNoOpGraphNodeVisitor is an Adapter (of the Adapter design pattern) to
 * GraphNodeVisitor - based off AbstractNoOpStoryVisitor.
 * 
 * @author mfchurch
 * 
 */
public abstract class GraphNodeAdapter implements GraphNodeVisitor {

	public void processTextNode(TextNode textNode) {
		this.defaultProcess(textNode);
	}

	public void processKnowItNode(KnowItNode knowItNode) {
		this.defaultProcess(knowItNode);
	}

	/**
	 * The default process method that is called by every
	 * process<i>Z</i>(<i>Z</i> <i>z</i>) method in this class' standard
	 * methods. <br>
	 * <br>
	 * Override this method if you want to provide a non-null default behaviour
	 * for every non-overridden process<i>Z</i> method. Unless it is overridden,
	 * it does nothing.
	 * 
	 * @param node
	 *            The GraphNode to process with a default behaviour.
	 */
	protected void defaultProcess(GraphNode node) {
	}
}
