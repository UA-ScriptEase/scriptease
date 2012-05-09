package scriptease.gui.quests;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.JComponent;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JToolBar;

import scriptease.controller.AbstractNoOpGraphNodeVisitor;
import scriptease.controller.observer.GraphNodeEvent;
import scriptease.gui.SEFrame;
import scriptease.gui.ToolBarFactory;
import scriptease.gui.WindowManager;
import scriptease.gui.graph.GraphPanel;
import scriptease.gui.graph.editor.GraphEditor;
import scriptease.gui.graph.editor.GraphEditorButton;
import scriptease.gui.graph.nodes.GraphNode;
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
	private final String INSERT_QUESTPOINT_BETWEEN_TEXT = "<html>Insert Between</html>";
	private final String INSERT_QUESTPOINT_ALTERNATE_TEXT = "<html>Insert Alternate</html>";
	private final String QUESTPOINT_PROPERTIES = "<html>Properties</html>";
	private final String EDIT_QUESTPOINT_TEXT = "<html>Open</html>";
	private final String CREATE_QUEST_TEXT = "<html>Start Quest</html>";

	private int questPointCounter = 0;
	
	private final JToolBar buttonToolBar = ToolBarFactory.buildQuestEditorToolBar();


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

		for (GraphEditorButton a : ToolBarFactory.getGraphEditorToolButtons()) {
			a.addActionListener(this);
		}

		// default active tool
		ToolBarFactory.propButton.addActionListener(this);
		setActiveTool(GraphTool.SELECT_NODE_TOOL);
	}

	/**
	 * Highlights the quest point that is represented by the given GraphNode in
	 * the graph.
	 * 
	 * @param graphNode
	 */
	private void highlightQuestPointAtGraphNode(GraphNode graphNode) {
		final GraphNode questPointNode = graphNode;

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
		this.setGraphPanel(graphPanel);
	}

	/**
	 * Returns the collection of buttons that represent the node creation tools
	 * available in the editor. The buttons only set the active tool; the tool
	 * logic is centralized in the nodeChanged method.
	 */
	@Override
	protected Collection<AbstractButton> getNodeButtons() {
		Collection<AbstractButton> buttons = new ArrayList<AbstractButton>();

		AbstractButton insertQuestPointNodeBetweenButton = new JRadioButtonMenuItem(
				new AbstractAction() {
					@Override
					public void actionPerformed(ActionEvent e) {
						setActiveTool(GraphTool.INSERT_QUESTPOINTNODE_BETWEEN_TOOL);
					}
				});
		insertQuestPointNodeBetweenButton
				.setText(INSERT_QUESTPOINT_BETWEEN_TEXT);
		buttons.add(insertQuestPointNodeBetweenButton);

		AbstractButton insertQuestPointNodeAlternateButton = new JRadioButtonMenuItem(
				new AbstractAction() {
					@Override
					public void actionPerformed(ActionEvent e) {
						setActiveTool(GraphTool.INSERT_QUESTPOINTNODE_ALTERNATE_TOOL);
					}
				});
		insertQuestPointNodeAlternateButton
				.setText(INSERT_QUESTPOINT_ALTERNATE_TEXT);
		buttons.add(insertQuestPointNodeAlternateButton);

		return buttons;
	}

	/**
	 * Returns the collection of buttons that represent the node selection tools
	 * available in the editor.
	 * 
	 * NOTE: This method is not used!
	 */
	@Override
	protected Collection<AbstractButton> getSelectButtons() {
		Collection<AbstractButton> buttons = new ArrayList<AbstractButton>();
		// Rename QuestPoint Button
		AbstractButton renameQuestPointButton = new JRadioButtonMenuItem(
				new AbstractAction() {
					@Override
					public void actionPerformed(ActionEvent e) {
						setActiveTool(GraphTool.QUESTPOINT_PROPERTIES_TOOL);
					}
				});
		renameQuestPointButton.setText(QUESTPOINT_PROPERTIES);

		// Edit QuestPoint Button
		AbstractButton editQuestPointButton = new JRadioButtonMenuItem(
				new AbstractAction() {
					@Override
					public void actionPerformed(ActionEvent e) {
						setActiveTool(GraphTool.OPEN_QUESTPOINT_TOOL);
					}
				});
		editQuestPointButton.setText(EDIT_QUESTPOINT_TEXT);

		// add the buttons
		buttons.add(editQuestPointButton);
		// buttons.add(createQuestButton);
		buttons.add(renameQuestPointButton);

		return buttons;
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
	private void insertQuestPoint(GraphNode node, boolean removeConnection) {
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

				if (removeConnection) {
					// Remove the old connection between the parent and child:
					closerToStartNode.removeChild(furtherFromStartNode, false);
				}

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
			switch (this.getActiveTool()) {
			case INSERT_QUESTPOINTNODE_BETWEEN_TOOL:
				// Insert a new QuestPoint between two nodes.
				insertQuestPoint(sourceNode, true);
				break;
			case INSERT_QUESTPOINTNODE_ALTERNATE_TOOL:
				// Insert a new QuestPoint on an alternate path between two
				// nodes.
				insertQuestPoint(sourceNode, false);
				break;
			case OPEN_QUESTPOINT_TOOL:
				// Open the QuestPoint for editing in a new tab. The arrow tool.
				highlightQuestPointAtGraphNode(node);
				
				node.process(new AbstractNoOpGraphNodeVisitor() {
					@Override
					public void processQuestPointNode(
							QuestPointNode questPointNode) {
						
						QuestPoint questPoint = questPointNode.getQuestPoint();
						
						ToolBarFactory.updateQuestPointNameField(questPoint.getDisplayText());
						//ToolBarFactory.questPointNameField().setText(questPoint.getDisplayText());
						// Force the graph to rebuild.
						//setHeadNode(headNode);
					}
				});
				
				
				break;
			case QUESTPOINT_PROPERTIES_TOOL:
				// Show a modal properties dialog that includes options to
				// change fanIn, committing status, and name textbox.
				node.process(new AbstractNoOpGraphNodeVisitor() {
					@Override
					public void processQuestPointNode(
							QuestPointNode questPointNode) {

						WindowManager.getInstance()
								.showQuestPointPropertiesDialog(questPointNode);
						// Force the graph to rebuild.
						setHeadNode(headNode);
					}
				});

				this.setActiveTool(GraphTool.OPEN_QUESTPOINT_TOOL);
				break;

			case CREATE_QUEST_TOOL:
				List<GraphNode> sourceNodeParents = sourceNode.getParents();
				List<GraphNode> sourceNodeChildren = sourceNode.getChildren();

				if (!sourceNodeParents.isEmpty()
						&& !sourceNodeChildren.isEmpty()) {
					try {
						final QuestNode questNode = new QuestNode("New Quest",
								sourceNode, false);
						final GraphNode startPoint = questNode.getStartPoint();
						final GraphNode endPoint = questNode.getEndPoint();

						// Add this Quest to the parents of the START
						Collection<GraphNode> parents = startPoint.getParents();
						for (GraphNode parent : parents) {
							parent.addChild(questNode);
						}
						// remove the START as a child of it's parents,
						// essentially
						// replacing the START with this questNode
						startPoint.removeParents();

						// Add this Quest to the children of the END
						Collection<GraphNode> children = endPoint.getChildren();
						for (GraphNode child : children) {
							child.addParent(questNode);
						}
						// remove the END as a parent of it's children,
						// essentially
						// replacing the END with this questNode
						endPoint.removeChildren();
					} catch (IllegalArgumentException e) {
						// cannot make a Quest out of the given sourceNode, so
						// don't
						System.err.println("Cannot make a Quest start at "
								+ sourceNode);
					}
				}
				break;
			case DELETE_TOOL:
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
								}
							}
						}
					}
				});
				return;
			}
		}
		// Note: the (dis)connect tool is in GraphEditor because it is common to
		// both DescribeItGraphEditor and QuestPanelEditor.
		super.nodeChanged(node, event);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		// NEW_TEXTNODE_TOOL, NEW_KNOWITNODE_TOOL, CONNECT_TOOL, DELETE_TOOL,
		// SELECT_NODE_TOOL, SELECT_PATH_TOOL,
		// INSERT_QUESTPOINTNODE_BETWEEN_TOOL,
		// INSERT_QUESTPOINTNODE_ALTERNATE_TOOL, RENAME_QUESTPOINT_TOOL,
		// CREATE_QUEST_TOOL, OPEN_QUESTPOINT_TOOL, QUESTPOINT_PROPERTIES_TOOL

		if (e.getSource() instanceof GraphEditorButton) {
			switch (((GraphEditorButton) e.getSource()).getQuestButtonType()) {
			case SELECT:
				System.out.println("Select");
				SEFrame.getInstance().changeCursor(SEFrame.SYSTEM_CURSOR);
				
				// this.setActiveTool(GraphTool.SELECT_NODE_TOOL);
				this.setActiveTool(GraphTool.OPEN_QUESTPOINT_TOOL);
				return;

			case INSERT:
				System.out.println("insert");
				SEFrame.getInstance().changeCursor(SEFrame.ADD_NODE_CURSOR);
				
				this.setActiveTool(GraphTool.INSERT_QUESTPOINTNODE_BETWEEN_TOOL);
				return;

			case CONNECT:
				System.out.println("connect");
				SEFrame.getInstance().changeCursor(SEFrame.DRAW_PATH_CURSOR);

				this.setActiveTool(GraphTool.CONNECT_TOOL);
				return;

			case DISCONNECT:
				System.out.println("disconnect");
				SEFrame.getInstance().changeCursor(SEFrame.ERASE_PATH_CURSOR);
				
				this.setActiveTool(GraphTool.CONNECT_TOOL);
				return;

			case DELETE:
				System.out.println("delete");
				SEFrame.getInstance().changeCursor(SEFrame.DELETE_NODE_CURSOR);

				this.setActiveTool(GraphTool.DELETE_TOOL);
				return;

			default:
				break;
			}
		}

		if (e.getActionCommand().equals("Properties")) {
			this.setActiveTool(GraphTool.QUESTPOINT_PROPERTIES_TOOL);
		}
	}
}
