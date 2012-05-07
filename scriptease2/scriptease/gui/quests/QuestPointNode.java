package scriptease.gui.quests;

import scriptease.controller.GraphNodeVisitor;
import scriptease.gui.graph.nodes.GraphNode;

/**
 * QuestPointNode is a GraphNode used to represent a QuestPoint. The
 * QuestPointNode contains graph structure information such as parents and
 * children, whereas the QuestPoint itself contains information about the model
 * and code to generate.
 * 
 * @author mfchurch
 * 
 */
public class QuestPointNode extends GraphNode {
	private QuestPoint questPoint;

	public QuestPointNode() {
		super();
	}

	public QuestPointNode(QuestPoint questPoint) {
		super();
		setQuestPoint(questPoint);
	}

	@Override
	public void process(GraphNodeVisitor processController) {
		processController.processQuestPointNode(this);
	}

	@Override
	public boolean isTerminalNode() {
		return this.children.isEmpty();
	}

	public QuestPoint getQuestPoint() {
		return this.questPoint;
	}

	@Override
	public String toString() {
		return "QuestPointNode [" + this.questPoint + "]";
	}

	public void setQuestPoint(QuestPoint questPoint) {
		this.questPoint = questPoint;
	}

	@Override
	public boolean equals(Object other) {
		if (super.equals(other) && other instanceof QuestPointNode) {
			return ((QuestPointNode) other).getQuestPoint().equals(
					this.getQuestPoint());
		}
		return false;
	}

	@Override
	public boolean represents(Object object) {
		return object == this.questPoint;
	}
}
