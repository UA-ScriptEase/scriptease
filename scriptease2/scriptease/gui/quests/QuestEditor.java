package scriptease.gui.quests;

import java.awt.BorderLayout;
import java.util.List;

import javax.swing.JToolBar;

import scriptease.controller.observer.GraphNodeEvent;
import scriptease.controller.observer.GraphNodeEvent.GraphNodeEventType;
import scriptease.gui.SEFrame;
import scriptease.gui.ToolBarFactory;
import scriptease.gui.action.ToolBarButtonAction;
import scriptease.gui.action.ToolBarButtonAction.ToolBarButtonMode;
import scriptease.gui.graph.editor.GraphEditor;
import scriptease.gui.graph.nodes.GraphNode;

/**
 * Editor used for building Quests. Users select the active tool, which dictates
 * what actions should be taken when a GraphNode is clicked. Observes all of the
 * GraphNodes in the graph in order to know when nodes are clicked.
 * 
 * @author mfchurch
 * @author graves (refactored)
 * @author kschenk (refactored further)
 */
@SuppressWarnings("serial")
public class QuestEditor extends GraphEditor {

	private JToolBar questToolBar; 

	public QuestEditor(final GraphNode start) {
		super();
		this.setHeadNode(start);
		this.buildPanels();

		ToolBarFactory factory = new ToolBarFactory();
		
		questToolBar = factory.buildQuestEditorToolBar(this);

		this.add((ToolBarFactory.buildGraphEditorToolBar(this).add(
				questToolBar)), BorderLayout.PAGE_START);

		ToolBarButtonAction.setMode(ToolBarButtonMode.SELECT_GRAPH_NODE);

	}

	/**
	 * Method to abstract commonalities from the Insert QuestPoint between and
	 * alternate tools.
	 * 
	 * @param node
	 * 
	 * @author graves
	 */
	private void insertQuestPoint(GraphNode node) {
		// if this is the second click,

		if (oldSelectedNode != null) {

			// create a new node to insert:
			QuestPoint newQuestPoint = new QuestPoint("", 1, false);
			QuestPointNode newQuestPointNode = new QuestPointNode(newQuestPoint);

			// Cases for clicking the same node.
			if (oldSelectedNode == node) {

				if (oldSelectedNode == headNode) {
					// Get the children of the start node.
					List<GraphNode> startNodeChildren = oldSelectedNode
							.getChildren();

					// Remove them all.
					oldSelectedNode.removeChildren();

					// Add the new node to the start node as a child.
					oldSelectedNode.addChild(newQuestPointNode);

					// Add the old children to the new node.
					newQuestPointNode.addChildren(startNodeChildren);
				}

				else if (oldSelectedNode.isTerminalNode()) {
					// Get the parents of the end node.
					List<GraphNode> endNodeParents = oldSelectedNode
							.getParents();

					// Remove them all.
					oldSelectedNode.removeParents();

					// Add the end node to the new node as a child.
					newQuestPointNode.addChild(oldSelectedNode);

					// Add the old parents to the new node.
					for (GraphNode parent : endNodeParents) {
						parent.addChild(newQuestPointNode);
					}
				}
				// double clicking any other node does nothing.

				// Cases for clicking a new node
			} else {
				// determine which node is closer to the startNode in the graph
				// (the parent) and which is further from the startNode (the
				// child).
				GraphNode closerToStartNode = node
						.isDescendant(oldSelectedNode) ? oldSelectedNode : node;
				GraphNode furtherFromStartNode = node
						.isDescendant(oldSelectedNode) ? node : oldSelectedNode;

				// Remove the old connection between the parent and child:
				closerToStartNode.removeChild(furtherFromStartNode, false);

				// Add the new node to the shallower node as a child (addChild
				// automatically adds shallower as parent):
				closerToStartNode.addChild(newQuestPointNode);

				// Add the deeper node to the new node as a child.
				newQuestPointNode.addChild(furtherFromStartNode);
			}
			// Reset the tool:
			oldSelectedNode = null;
		} else {
			// otherwise this is the first click, so store the node for
			// later:
			oldSelectedNode = node;
		}

	}

	/**
	 * This method handles all of the logic for the quest tools. It is called
	 * whenever an observed GraphNode is clicked.
	 */
	@Override
	public void nodeChanged(GraphNodeEvent event) {
		final GraphNode sourceNode = event.getSource();

		final GraphNodeEventType type = event.getEventType();

		// only process clicked actions if you are contained in the active tab
		if (type == GraphNodeEventType.SELECTED
				&& SEFrame.getInstance().getActiveStory().contains(this)) {

			// Determine the active tool
			switch (ToolBarButtonAction.getMode()) {
			case INSERT_GRAPH_NODE:
				insertQuestPoint(sourceNode);
				break;
			}
		}
		super.nodeChanged(event);
	}
}
