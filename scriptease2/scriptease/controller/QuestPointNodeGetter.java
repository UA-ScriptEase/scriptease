package scriptease.controller;

import java.util.ArrayList;
import java.util.Collection;

import scriptease.gui.graph.nodes.GraphNode;
import scriptease.gui.quests.QuestNode;
import scriptease.gui.quests.QuestPoint;
import scriptease.gui.quests.QuestPointNode;

/**
 * QuestPointGetter recursively extracts all of the QuestPointNodes from the
 * given QuestNode
 * 
 * @author mfchurch
 * 
 */
public class QuestPointNodeGetter {

	/**
	 * Recursively extracts all of the QuestPointNodes from the given QuestNode
	 * 
	 * @param quest
	 * @return
	 */
	public static Collection<QuestPointNode> getQuestPointNodes(QuestNode quest) {
		final Collection<QuestPointNode> questPointNodes = new ArrayList<QuestPointNode>();
		final GraphNode startPoint = quest.getStartPoint();
		final GraphNode endPoint = quest.getEndPoint();
		startPoint.process(new AbstractNoOpGraphNodeVisitor() {

			@Override
			public void processQuestNode(QuestNode questNode) {
				// recursively deal with quests
				questPointNodes.addAll(getQuestPointNodes(questNode));
			}

			@Override
			public void processQuestPointNode(QuestPointNode questPointNode) {
				// add the questpoint
				questPointNodes.add(questPointNode);
				// process children if we aren't at the end of the quest
				if (questPointNode != endPoint) {
					for (GraphNode child : questPointNode.getChildren()) {
						child.process(this);
					}
				}
			}
		});
		return questPointNodes;
	}

	public static Collection<QuestPoint> getQuestPoints(QuestNode quest) {
		Collection<QuestPointNode> nodes = getQuestPointNodes(quest);
		Collection<QuestPoint> questPoints = new ArrayList<QuestPoint>(
				nodes.size());
		for (QuestPointNode node : nodes) {
			questPoints.add(node.getQuestPoint());
		}
		return questPoints;
	}
}
