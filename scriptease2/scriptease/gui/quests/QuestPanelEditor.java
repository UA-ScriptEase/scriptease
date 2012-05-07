package scriptease.gui.quests;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.JComponent;
import javax.swing.JRadioButtonMenuItem;

import scriptease.controller.AbstractNoOpGraphNodeVisitor;
import scriptease.controller.observer.GraphNodeEvent;
import scriptease.gui.SEFrame;
import scriptease.gui.WindowManager;
import scriptease.gui.graph.GraphPanel;
import scriptease.gui.graph.editor.GraphEditor;
import scriptease.gui.graph.nodes.GraphNode;
import scriptease.gui.quests.toolbarButtons.QuestButton;
import scriptease.gui.quests.toolbarButtons.QuestButton.QuestButtonType;
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
	private final String INSERT_QUESTPOINT_BETWEEN_TEXT = "<html>Insert Between</html>";
	private final String INSERT_QUESTPOINT_ALTERNATE_TEXT = "<html>Insert Alternate</html>";
	private final String QUESTPOINT_PROPERTIES = "<html>Properties</html>";
	private final String EDIT_QUESTPOINT_TEXT = "<html>Open</html>";
	private final String CREATE_QUEST_TEXT = "<html>Start Quest</html>";

	private int questPointCounter = 0;

	public QuestPanelEditor(final GraphNode start, final QuestPoint questPoint) {
		super(new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO move save action from GraphEditor to DescribeItEditor
				// since it doesn't make sense in QuestPanelEditor.
			}
		});

		// Set the headNode to be the start node of the graph.
		this.setHeadNode(start);

		// Highlight the active questPoint.
		highLightQuestPoint(questPoint);
		
		for(QuestButton a: getQuestToolButtons()){
			a.addActionListener(this);
		}
		
		//default active tool
		propButton.addActionListener(this);
		setActiveTool(GraphTool.SELECT_NODE_TOOL);
	}

	/**
	 * Highlights the node that represents the given questPoint in the graph.
	 * @param questPoint
	 */
	private void highLightQuestPoint(QuestPoint questPoint) {
		// Highlight the questPointNode
		final GraphNode questPointNode = this.headNode
				.getRepresentingGraphNode(questPoint);
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

		// Create Quest Button TODO fix up Quest logic
		/*
		 * AbstractButton createQuestButton = new JRadioButtonMenuItem( new
		 * AbstractAction() {
		 * 
		 * @Override public void actionPerformed(ActionEvent e) {
		 * setActiveTool(GraphTool.CREATE_QUEST_TOOL); } });
		 * createQuestButton.setText(CREATE_QUEST_TEXT);
		 */

		// add the buttons
		buttons.add(editQuestPointButton);
		// buttons.add(createQuestButton);
		buttons.add(renameQuestPointButton);
		
		/*QuestButton select = new QuestButton(QuestButtonType.SELECT);
		QuestButton insert = new QuestButton(QuestButtonType.INSERT);
		QuestButton delete = new QuestButton(QuestButtonType.DELETE);
		QuestButton connect = new QuestButton(QuestButtonType.CONNECT);
		QuestButton disconnect = new QuestButton(QuestButtonType.DISCONNECT);
		
		buttons.add(select);
		buttons.add(insert);
		buttons.add(delete);
		buttons.add(connect);
		buttons.add(disconnect);*/
		
		
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
				// Open the QuestPoint for editing in a new tab.
				final StoryModel model = StoryModelPool.getInstance()
						.getActiveModel();
				if (model != null) {
					node.process(new AbstractNoOpGraphNodeVisitor() {
						@Override
						public void processQuestPointNode(
								QuestPointNode questPointNode) {
							final QuestPoint questPoint = questPointNode
									.getQuestPoint();
							// check if a tab already exists for the given model
							// and questPoint
							if (SEFrame.getInstance().hasTabForQuestPoint(
									model, questPoint)) {
								// if so, switch to that tab
								SEFrame.getInstance().activateTabForQuestPoint(
										model, questPoint);
							} else {
								// otherwise make a new tab
								SEFrame.getInstance().createTabForQuestPoint(
										model, questPoint);
							}
						}
					});
				}
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

						// Highlight the active questPoint.
						highLightQuestPoint(questPointNode.getQuestPoint());
					}
				});
				
				this.setActiveTool(GraphTool.OPEN_QUESTPOINT_TOOL);
				
				for(QuestButton a: getQuestToolButtons()){
					a.addActionListener(this);
				}
				
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
	
	private void setOtherOff(QuestButton toCheck, boolean togs){
		for(QuestButton a: getQuestToolButtons()){
			if(a != toCheck){
				a.setBoolState(togs);
			}
		}
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		//NEW_TEXTNODE_TOOL, NEW_KNOWITNODE_TOOL, CONNECT_TOOL, DELETE_TOOL, SELECT_NODE_TOOL, SELECT_PATH_TOOL, INSERT_QUESTPOINTNODE_BETWEEN_TOOL, INSERT_QUESTPOINTNODE_ALTERNATE_TOOL, RENAME_QUESTPOINT_TOOL, CREATE_QUEST_TOOL, OPEN_QUESTPOINT_TOOL, QUESTPOINT_PROPERTIES_TOOL
		
		
			
		if(e.getSource() instanceof QuestButton){
			switch(((QuestButton)e.getSource()).getQuestButtonType()){
			case SELECT:
				System.out.println("Select");
				//this.setActiveTool(GraphTool.SELECT_NODE_TOOL);
				this.setActiveTool(GraphTool.OPEN_QUESTPOINT_TOOL);
				if(((QuestButton)e.getSource()).getState() == true){
					((QuestButton)e.getSource()).toggleState();
					setOtherOff(((QuestButton)e.getSource()), true);
				}
				else{
					((QuestButton)e.getSource()).setBoolState(false);
					setOtherOff(((QuestButton)e.getSource()), true);
				}
				return;
				
			case INSERT:
				System.out.println("insert");
				this.setActiveTool(GraphTool.INSERT_QUESTPOINTNODE_BETWEEN_TOOL);
				//((QuestButton)e.getSource()).toggleState();
				if(((QuestButton)e.getSource()).getState() == true){
					((QuestButton)e.getSource()).toggleState();
					setOtherOff(((QuestButton)e.getSource()), true);
				}
				else{
					((QuestButton)e.getSource()).setBoolState(false);
					setOtherOff(((QuestButton)e.getSource()), true);
				}
				return;
			
			case CONNECT:
				System.out.println("connect");
				this.setActiveTool(GraphTool.CONNECT_TOOL);
				if(((QuestButton)e.getSource()).getState() == true){
					((QuestButton)e.getSource()).toggleState();
					setOtherOff(((QuestButton)e.getSource()), true);
				}
				else{
					((QuestButton)e.getSource()).setBoolState(false);
					setOtherOff(((QuestButton)e.getSource()), true);
				}
				return;
			
			case DISCONNECT:
				System.out.println("disconnect");
				this.setActiveTool(GraphTool.CONNECT_TOOL);
				if(((QuestButton)e.getSource()).getState() == true){
					((QuestButton)e.getSource()).toggleState();
					setOtherOff(((QuestButton)e.getSource()), true);
				}
				else{
					((QuestButton)e.getSource()).setBoolState(false);
					setOtherOff(((QuestButton)e.getSource()), true);
				}
				return;
				
			case DELETE:
				System.out.println("delete");
				this.setActiveTool(GraphTool.DELETE_TOOL);
				if(((QuestButton)e.getSource()).getState() == true){
					((QuestButton)e.getSource()).toggleState();
					setOtherOff(((QuestButton)e.getSource()), true);
				}
				else{
					((QuestButton)e.getSource()).setBoolState(false);
					setOtherOff(((QuestButton)e.getSource()), true);
				}
				return;
				
			default:
				break;
			}
		}
		
		if(e.getActionCommand().equals("Properties")){
			this.setActiveTool(GraphTool.QUESTPOINT_PROPERTIES_TOOL);
		}
	}
}
