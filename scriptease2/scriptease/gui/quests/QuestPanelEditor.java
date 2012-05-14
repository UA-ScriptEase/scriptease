package scriptease.gui.quests;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;

import scriptease.controller.AbstractNoOpGraphNodeVisitor;
import scriptease.controller.observer.GraphNodeEvent;
import scriptease.gui.SEFrame;
import scriptease.gui.ToolBarFactory;
import scriptease.gui.action.ToolBarButtonAction;
import scriptease.gui.action.ToolBarButtonAction.ToolBarButtonMode;
import scriptease.gui.graph.GraphPanel;
import scriptease.gui.graph.editor.GraphEditor;
import scriptease.gui.graph.nodes.GraphNode;
import scriptease.model.StoryModel;
import scriptease.model.StoryModelPool;
import scriptease.util.GUIOp;

/**
 * Editor used for building Quests. Users select the active tool, which dictates
 * what actions should be taken when a GraphNode is clicked. Observes all of the
 * GraphNodes in the graph in order to know when nodes are clicked.
 * 
 * @author mfchurch
 * @author graves (refactored)
 */
@SuppressWarnings("serial")
public class QuestPanelEditor extends GraphEditor {
	private final String NEW_QUEST_POINT = "New Quest Point";
	private int questPointCounter = 0;
	
	private final JToolBar buttonToolBar = ToolBarFactory
			.buildQuestEditorToolBar();

	public QuestPanelEditor(final GraphNode start) {
		super(new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO move save action from GraphEditor to DescribeItEditor
				// since it doesn't make sense in QuestPanelEditor.
			}
		});
		
		addToolBar(buttonToolBar);

		// Set the headNode to be the start node of the graph.
		this.setHeadNode(start);

		ToolBarButtonAction.setMode(ToolBarButtonMode.SELECT_QUEST_POINT);
	}
		
	/**
	 * Highlights the quest point that is represented by the given GraphNode in
	 * the graph.
	 * 
	 * @param graphNode
	 */
	private void highlightQuestPointAtGraphNode(GraphNode graphNode) {
		final GraphNode questPointNode = graphNode;
		
		JScrollPane currentPanel = (JScrollPane) this.editingPanel.getTopComponent();
		Point position = currentPanel.getViewport().getViewPosition();
	
		final GraphPanel graphPanel = new GraphPanel(this.headNode) {
			@Override
			public void configureAppearance(GraphNode node, JComponent component) {
				super.configureAppearance(node, component);
				// Highlight the questPointNode
				if (node == questPointNode) {
					Color selectedColour = node.getSelectedColour();
					component.setBackground(GUIOp.scaleWhite(selectedColour,
							2.1));
				}
			}
		};
		this.setGraphPanel(graphPanel, position);
	}

	/**
	 * Method to abstract commonalities from the Insert QuestPoint between and
	 * alternate tools.
	 * 
	 * @param node
	 * @param removeConnection
	 * 
	 * @author graves
	 */
	private void insertQuestPoint(GraphNode node) {
		// if this is the second click,
		if (oldSelectedNode != null) {

			// create a new node to insert:
			questPointCounter++;
			QuestPoint newQuestPoint = new QuestPoint(NEW_QUEST_POINT + " "
					+ questPointCounter, 1, false);
			QuestPointNode newQuestPointNode = new QuestPointNode(newQuestPoint);

			// Cases for clicking the same node.
			if (oldSelectedNode == node) {
				// Special case: double-clicking the start node adds a new node
				// after the start node.
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
				// Special case: double-clicking a terminal node adds a new node
				// before the terminal node.
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
	public void nodeChanged(GraphNode node, GraphNodeEvent event) {
		final GraphNode sourceNode = event.getSource();
		final short type = event.getEventType();

		// only process clicked actions if you are contained in the active tab
		if (type == GraphNodeEvent.CLICKED
				&& SEFrame.getInstance().getActiveTab().contains(this)) {
			
			// Determine the active tool
			switch (ToolBarButtonAction.getMode()) {
			case INSERT_QUEST_POINT:
				insertQuestPoint(sourceNode);
				
				ToolBarFactory.updateFanInSpinner();
				
				break;

			case SELECT_QUEST_POINT:
				highlightQuestPointAtGraphNode(node);

				final StoryModel model = StoryModelPool.getInstance()
						.getActiveModel();

				if (model != null) {
					node.process(new AbstractNoOpGraphNodeVisitor() {
						@Override
						public void processQuestPointNode(
								QuestPointNode questPointNode) {

							QuestPoint questPoint = questPointNode
									.getQuestPoint();

							SEFrame.getInstance().activatePanelForQuestPoint(
									model, questPoint);
							
							ToolBarFactory.setCurrentQuestPointNode(questPointNode);							

							// Force the graph to rebuild.
							// setHeadNode(headNode);
						}
					});

					break;
				}

			case DELETE_QUEST_POINT:
				sourceNode.process(new AbstractNoOpGraphNodeVisitor() {
					@Override
					public void processQuestNode(QuestNode questNode) {
						// Remove the Quest
						questNode.removeParents();

						// Add startPoint to parents of QuestNode
						GraphNode startPoint = questNode.getStartPoint();
						List<GraphNode> parents = sourceNode.getParents();
						for (GraphNode parent : parents) {
							parent.addChild(startPoint);
						}

						// Add children of QuestNode to endPoint
						GraphNode endPoint = questNode.getEndPoint();
						endPoint.addChildren(questNode.getChildren());

					}

					@Override
					public void processQuestPointNode(
							QuestPointNode questPointNode) {
						List<GraphNode> parents = questPointNode.getParents();
						List<GraphNode> children = questPointNode.getChildren();

						// Only delete the node if there are parents and
						// children to repair the graph with.
						if (!parents.isEmpty() && !children.isEmpty()) {

							// Remove the node from its parents.
							questPointNode.removeParents();

							// Remove the node from its children.
							questPointNode.removeChildren();

							// Re-connect each parent with each child.
							for (GraphNode parent : parents) {
								for (GraphNode child : children) {
									parent.addChild(child);
									child.process(new AbstractNoOpGraphNodeVisitor() {
										public void processQuestPointNode(
												QuestPointNode questPointNode) {

											QuestPoint questPoint = questPointNode
													.getQuestPoint();
											int fanIn = questPoint.getFanIn();

											if (fanIn > 1)
												questPoint.setFanIn(fanIn - 1);

										}
									});
								}
							}
						}

						ToolBarFactory.updateFanInSpinner();
					}
				});

				return;
			}
		}
	
		// Note: the (dis)connect tool is in GraphEditor due to commonalities
		super.nodeChanged(node, event);
	}
}
