package scriptease.controller;

import scriptease.gui.graph.nodes.KnowItNode;
import scriptease.gui.graph.nodes.TextNode;
import scriptease.gui.quests.QuestNode;
import scriptease.gui.quests.QuestPointNode;

/**
 * Visitor for GraphNodes
 * 
 * @author mfchurch
 * 
 */
public interface GraphNodeVisitor {
	
	public void processTextNode(TextNode textNode); 

	public void processKnowItNode(KnowItNode knowItNode);
	
	public void processQuestPointNode(QuestPointNode questPointNode);
	
	public void processQuestNode(QuestNode questNode);
	
}
