package scriptease.controller;

import scriptease.gui.SEGraph.nodes.KnowItNode;
import scriptease.gui.SEGraph.nodes.TextNode;

/**
 * Visitor for GraphNodes
 * 
 * @author mfchurch
 * 
 */
public interface GraphNodeVisitor {
	
	public void processTextNode(TextNode textNode); 

	public void processKnowItNode(KnowItNode knowItNode);
	
}
