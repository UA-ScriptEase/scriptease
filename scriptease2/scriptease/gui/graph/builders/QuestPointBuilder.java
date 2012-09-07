package scriptease.gui.graph.builders;

import scriptease.gui.quests.QuestPoint;

/**
 * Builds a new Quest Point.
 * 
 * @author kschenk
 *
 */
public class QuestPointBuilder extends SEGraphNodeBuilder<QuestPoint> {
	@Override
	public QuestPoint buildNewNode() {
		return new QuestPoint("");
	}
}
