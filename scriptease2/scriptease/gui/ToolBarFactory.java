package scriptease.gui;

import java.awt.CardLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.PlainDocument;

import scriptease.controller.GraphNodeAdapter;
import scriptease.controller.observer.GraphNodeEvent;
import scriptease.controller.observer.GraphNodeEvent.GraphNodeEventType;
import scriptease.controller.observer.GraphNodeObserver;
import scriptease.gui.action.ToolBarButtonAction;
import scriptease.gui.action.ToolBarButtonAction.ToolBarButtonMode;
import scriptease.gui.action.story.graphs.ConnectGraphPointAction;
import scriptease.gui.action.story.graphs.DeleteGraphNodeAction;
import scriptease.gui.action.story.graphs.DisconnectGraphPointAction;
import scriptease.gui.action.story.graphs.InsertGraphNodeAction;
import scriptease.gui.action.story.graphs.SelectGraphNodeAction;
import scriptease.gui.graph.GraphPanel;
import scriptease.gui.graph.SEGraph;
import scriptease.gui.graph.editor.KnowItNodeEditor;
import scriptease.gui.graph.editor.PathAssigner;
import scriptease.gui.graph.editor.TextNodeEditor;
import scriptease.gui.graph.nodes.GraphNode;
import scriptease.gui.graph.nodes.KnowItNode;
import scriptease.gui.graph.nodes.TextNode;
import scriptease.gui.internationalization.Il8nResources;
import scriptease.gui.quests.QuestPoint;
import scriptease.model.PatternModel;
import scriptease.model.PatternModelManager;
import scriptease.model.StoryModel;
import scriptease.model.atomic.DescribeIt;
import scriptease.model.atomic.KnowIt;

/**
 * ToolBarFactory is responsible for creating JToolBars, most importantly the
 * toolbars for editing graphs. A specialized Quest Editor Toolbar can also be
 * created.<br>
 * <br>
 * The class also determines toolbar functionality. These toolbars are used to
 * act upon Graph Panels, whether they be general Graph functions, Quest
 * functions, or DescribeIts.
 * 
 * @author kschenk
 * 
 */
public class ToolBarFactory {
	private static final String KNOW_IT_EDITOR = "Know It Node Editing Bar";
	private static final String TEXT_NODE_EDITOR = "Text Node Editing Bar";
	private static final String PATH_EDITOR = "Path Editing Bar";
	private static final String NO_EDITOR = "No Editor";

	private static ToolBarFactory instance = new ToolBarFactory();

	/**
	 * Returns the sole instance of ToolBarFactory.
	 * 
	 * @return
	 */
	public static ToolBarFactory getInstance() {
		return ToolBarFactory.instance;
	}

	/**
	 * By "putting" each observer to its respective JToolBar when it is created,
	 * the Map prevents Java from garbage collecting all of the weak references
	 * we create to observer graph nodes. - kschenk
	 * 
	 * Also, this is an ugly hack while we figure out a better way to keep
	 * strong references. - remiller
	 */
	@Deprecated
	private Map<JToolBar, GraphNodeObserver> observerMap = new LinkedHashMap<JToolBar, GraphNodeObserver>();

	/**
	 * Builds a toolbar to edit graphs with. Includes buttons for selecting
	 * nodes, adding and deleting nodes, and adding and deleting paths.
	 * 
	 * @return
	 */
	public JToolBar buildGraphEditorToolBar(SEGraph seGraph) {
		final JToolBar graphEditorToolBar = new JToolBar();

		final ButtonGroup graphEditorButtonGroup = new ButtonGroup();

		final ArrayList<JToggleButton> buttonList = new ArrayList<JToggleButton>();

		final JToggleButton selectNodeButton = new JToggleButton(
				SelectGraphNodeAction.getInstance());

		final JToggleButton insertNodeButton = new JToggleButton(
				InsertGraphNodeAction.getInstance());

		final JToggleButton deleteNodeButton = new JToggleButton(
				DeleteGraphNodeAction.getInstance());

		final JToggleButton connectNodeButton = new JToggleButton(
				ConnectGraphPointAction.getInstance());

		final JToggleButton disconnectNodeButton = new JToggleButton(
				DisconnectGraphPointAction.getInstance());

		graphEditorToolBar.setLayout(new BoxLayout(graphEditorToolBar,
				BoxLayout.LINE_AXIS));
		graphEditorToolBar.setRollover(true);
		graphEditorToolBar.setFloatable(false);

		buttonList.add(selectNodeButton);
		buttonList.add(insertNodeButton);
		buttonList.add(deleteNodeButton);
		buttonList.add(connectNodeButton);
		buttonList.add(disconnectNodeButton);

		for (JToggleButton toolBarButton : buttonList) {
			toolBarButton.setHideActionText(true);
			toolBarButton.setFocusable(false);
			graphEditorButtonGroup.add(toolBarButton);
			graphEditorToolBar.add(toolBarButton);
		}

		ChangeListener graphEditorListener = new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent arg0) {
				switch (ToolBarButtonAction.getMode()) {

				case SELECT_GRAPH_NODE:
					graphEditorButtonGroup.setSelected(
							selectNodeButton.getModel(), true);
					break;
				case DELETE_GRAPH_NODE:
					graphEditorButtonGroup.setSelected(
							deleteNodeButton.getModel(), true);
					break;
				case INSERT_GRAPH_NODE:
					graphEditorButtonGroup.setSelected(
							insertNodeButton.getModel(), true);
					break;
				case CONNECT_GRAPH_NODE:
					graphEditorButtonGroup.setSelected(
							connectNodeButton.getModel(), true);
					break;
				case DISCONNECT_GRAPH_NODE:
					graphEditorButtonGroup.setSelected(
							disconnectNodeButton.getModel(), true);
					break;
				}
			}
		};

		SEFrame.getInstance().getStoryTabPane()
				.addChangeListener(graphEditorListener);

		/*GraphNodeObserver graphBarObserver = new GraphToolBarObserver(gPanel);

		GraphNode.observeDepthMap(graphBarObserver, gPanel.getHeadNode());

		this.observerMap.put(graphEditorToolBar, graphBarObserver);*/

		return graphEditorToolBar;
	}

	/**
	 * Creates a JToolBar for the quest editor. It adds all of the graph editor
	 * buttons from the GraphEditorToolbar, and then adds Quest specific options
	 * for the user after a separator.
	 * 
	 * @return
	 */
	public JToolBar buildQuestEditorToolBar(SEGraph<QuestPoint> gPanel) {

		final JToolBar questEditorToolBar = this
				.buildGraphEditorToolBar(gPanel);

		final int TOOL_BAR_HEIGHT = 32;
		final int FAN_IN_SPINNER_LENGTH = 50;
		final int NAME_FIELD_LENGTH = 150;

		final JLabel nameLabel = new JLabel(Il8nResources.getString("Name")
				+ ":");
		final JLabel fanInLabel = new JLabel("Fan In:");

		final JTextField nameField = this.buildNameField(new Dimension(
				NAME_FIELD_LENGTH, TOOL_BAR_HEIGHT));

		final JSpinner fanInSpinner = this.buildFanInSpinner(new Dimension(
				FAN_IN_SPINNER_LENGTH, TOOL_BAR_HEIGHT), fanInLabel);

		this.updateQuestToolBar(nameField, fanInSpinner, nameLabel, fanInLabel,
				gPanel.getStartNode());

		final Dimension minSize = new Dimension(15, TOOL_BAR_HEIGHT);
		final Dimension prefSize = new Dimension(15, TOOL_BAR_HEIGHT);
		final Dimension maxSize = new Dimension(15, TOOL_BAR_HEIGHT);

		questEditorToolBar.add(new Box.Filler(minSize, prefSize, maxSize));
		questEditorToolBar.addSeparator();
		questEditorToolBar.add(new Box.Filler(minSize, prefSize, maxSize));

		questEditorToolBar.add(fanInLabel);
		fanInLabel.setLabelFor(fanInSpinner);
		questEditorToolBar.add(fanInSpinner);

		questEditorToolBar.add(nameLabel);
		nameLabel.setLabelFor(nameField);
		questEditorToolBar.add(nameField);

		GraphNodeObserver questBarObserver = new QuestToolBarObserver(
				nameField, fanInSpinner, nameLabel, fanInLabel, gPanel);

		GraphNode.observeDepthMap(questBarObserver, gPanel.getHeadNode());

		this.observerMap.put(questEditorToolBar, questBarObserver);

		return questEditorToolBar;
	}

	/**
	 * Creates a JToolBar for editing DescribeIts. Uses the same buttons as
	 * graph editor toolbar.
	 * 
	 * @param gPanel
	 * @return
	 */
	public JToolBar buildDescribeItToolBar(DescribeIt editedDescribeIt,
			GraphPanel gPanel) {
		final JToolBar describeItToolBar = this.buildGraphEditorToolBar(gPanel);
		// final GraphNode headNode = gPanel.getHeadNode();

		final int TOOL_BAR_HEIGHT = 32;

		final Dimension minSize = new Dimension(15, TOOL_BAR_HEIGHT);
		final Dimension prefSize = new Dimension(15, TOOL_BAR_HEIGHT);
		final Dimension maxSize = new Dimension(15, TOOL_BAR_HEIGHT);

		describeItToolBar.add(new Box.Filler(minSize, prefSize, maxSize));
		describeItToolBar.addSeparator();
		describeItToolBar.add(new Box.Filler(minSize, prefSize, maxSize));

		JComponent describeItEditBar = new JPanel();

		describeItEditBar.setLayout(new CardLayout());

		KnowItNodeEditor knowItEditor = new KnowItNodeEditor();
		TextNodeEditor textNodeEditor = new TextNodeEditor();
		PathAssigner pathEditor = new PathAssigner();

		describeItEditBar.add(knowItEditor, ToolBarFactory.KNOW_IT_EDITOR);
		describeItEditBar.add(textNodeEditor, ToolBarFactory.TEXT_NODE_EDITOR);
		describeItEditBar.add(pathEditor, ToolBarFactory.PATH_EDITOR);

		JPanel noEditorPanel = new JPanel();
		noEditorPanel.add(new JLabel("Path does not have an end point."));
		describeItEditBar.add(noEditorPanel, ToolBarFactory.NO_EDITOR);

		describeItToolBar.add(describeItEditBar);

		CardLayout cl = (CardLayout) describeItEditBar.getLayout();
		cl.show(describeItEditBar, ToolBarFactory.TEXT_NODE_EDITOR);

		GraphNodeObserver describeItBarObserver = new DescribeItToolBarObserver(
				editedDescribeIt, cl, describeItEditBar, knowItEditor,
				textNodeEditor, pathEditor);

		GraphNode.observeDepthMap(describeItBarObserver, gPanel.getHeadNode());

		this.observerMap.put(describeItToolBar, describeItBarObserver);

		return describeItToolBar;
	}

	/**
	 * Updates the entire quest tool bar, including the name field, the fan in
	 * spinner, and both of the labels.
	 * 
	 * @param nameField
	 *            The field for editing the quest point name.
	 * @param fanInSpinner
	 *            The spinner for editing the fan in value.
	 * @param nameLabel
	 *            The JLabel associated with the nameField
	 * @param fanInLabel
	 *            The JLabel associated with the fanInSpinner
	 * @param questNode
	 *            The quest node to edit.
	 */
	private void updateQuestToolBar(JTextField nameField,
			JSpinner fanInSpinner, JLabel nameLabel, JLabel fanInLabel,
			QuestPoint questNode) {

		this.updateFanInSpinner(fanInSpinner, fanInLabel, questNode);
		this.updateNameField(nameField, nameLabel, questNode);

	}

	/**
	 * Returns a name field JTextField with the proper appearance.
	 * 
	 * @param maxSize
	 * @return
	 */
	private JTextField buildNameField(final Dimension maxSize) {
		JTextField nameField = new JTextField(10);
		nameField.setMaximumSize(maxSize);

		return nameField;
	}

	/**
	 * Creates an DocumentListener for the TextField.
	 * 
	 * @return
	 */
	private DocumentListener nameFieldListener(final JTextField nameField,
			final QuestPoint questNode) {

		DocumentListener nameFieldListener = new DocumentListener() {

			@Override
			public void insertUpdate(DocumentEvent e) {
				String text = nameField.getText();
				questNode.setDisplayText(text);
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				this.insertUpdate(e);
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				this.insertUpdate(e);
			}
		};

		return nameFieldListener;
	}

	/**
	 * Updates the name field for the quest node passed in.
	 * 
	 * @param nameField
	 * @param questNode
	 */
	private void updateNameField(JTextField nameField, JLabel nameLabel,
			QuestPoint questNode) {
		if (questNode != null) {

			String displayText = questNode.getDisplayText();

			nameField.setDocument(new PlainDocument());
			nameField.getDocument().addDocumentListener(
					this.nameFieldListener(nameField, questNode));

			nameField.setText(displayText);
			if (((StoryModel) PatternModelManager.getInstance()
					.getActiveModel()).getRoot() != questNode) {
				nameLabel.setEnabled(true);
				nameField.setEnabled(true);

			} else {
				nameLabel.setEnabled(false);
				nameField.setEnabled(false);
			}

		} else {
			nameField.setText("");
			nameLabel.setEnabled(false);
			nameField.setEnabled(false);
		}
	}

	/**
	 * Returns a fanInSpinner, which has an attached listener that updates the
	 * model if its value is changed. This is used for quest fan in, i.e. how
	 * many preceding tests need to be finished before starting the selected
	 * one.
	 * 
	 * @return
	 */
	private JSpinner buildFanInSpinner(final Dimension maxSize,
			final JLabel fanInLabel) {
		final JSpinner fanInSpinner = new JSpinner();
		fanInSpinner.setMaximumSize(maxSize);

		fanInSpinner.setEnabled(false);
		fanInLabel.setEnabled(false);

		return fanInSpinner;
	}

	/**
	 * Creates a ChangeListener for a JSpinner. Returns listener. When the
	 * Spinner value changes, the listener updates the model and updates the fan
	 * in spinner itself to show the change.
	 * 
	 * @param fanInSpinner
	 * @param questPoint
	 * @return
	 */
	private ChangeListener fanInSpinnerListener(final JSpinner fanInSpinner,
			final QuestPoint questNode) {

		ChangeListener fanInSpinnerListener = new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				SpinnerModel spinnerModel = fanInSpinner.getModel();
				Integer spinnerValue = (Integer) spinnerModel.getValue();

				questNode.setFanIn(spinnerValue);
			}
		};

		return fanInSpinnerListener;
	}

	/**
	 * Creates a SpinnerModel for the FanIn function based on the current quest
	 * point, then sets the FanIn Spinner Model to it.
	 * 
	 * If there is no Quest Point selected, the SpinnerModel is a spinner set to
	 * 1.
	 * 
	 * @param fanInSpinner
	 *            FanInSpinner to be passed
	 * @param questNode
	 *            QuestPointNode that the spinner is operating on. Pass in null
	 *            if there is none.
	 * 
	 * @return The SpinnerModel
	 */
	private void updateFanInSpinner(JSpinner fanInSpinner, JLabel fanInLabel,
			QuestPoint questNode) {

		if (questNode != null) {
			int maxFanIn = questNode.getParents().size();

			// If maxFanIn >1, maxFanIn. Otherwise, 1.
			maxFanIn = maxFanIn > 1 ? maxFanIn : 1;

			if (questNode.getFanIn() > maxFanIn) {
				questNode.setFanIn(1);
			}

			final SpinnerModel fanInSpinnerModel = new SpinnerNumberModel(
					questNode.getFanIn(), new Integer(1),
					new Integer(maxFanIn), new Integer(1));

			fanInSpinner.setModel(fanInSpinnerModel);

			if (fanInSpinner.getChangeListeners().length > 1) {
				fanInSpinner.removeChangeListener(fanInSpinner
						.getChangeListeners()[1]);
			}

			fanInSpinner.addChangeListener(this.fanInSpinnerListener(
					fanInSpinner, questNode));

			if (!questNode.isStartNode()) {
				fanInLabel.setEnabled(true);
				fanInSpinner.setEnabled(true);
			} else {
				fanInLabel.setEnabled(false);
				fanInSpinner.setEnabled(false);
			}

		} else {
			final SpinnerModel fanInSpinnerModel = new SpinnerNumberModel(
					new Integer(1), new Integer(1), new Integer(1),
					new Integer(1));

			fanInSpinner.setModel(fanInSpinnerModel);
			fanInLabel.setEnabled(false);
			fanInSpinner.setEnabled(false);
		}
	}

	/**
	 * Private observer for the Graph ToolBar
	 * 
	 * @author kschenk
	 * 
	 */
	private class GraphToolBarObserver implements GraphNodeObserver {
		/**
		 * Adds the node to the graph bar observer if a new one is added.
		 */
		GraphPanel gPanel;

		public GraphToolBarObserver(GraphPanel gPanel) {
			this.gPanel = gPanel;
		}

		@Override
		public void nodeChanged(GraphNodeEvent event) {
			final GraphNode sourceNode = event.getSource();
			final GraphNodeEventType type = event.getEventType();

			GraphNode oldSelectedNode = this.gPanel.getOldSelectedNode();

			if (type == GraphNodeEventType.SELECTED) {
				switch (ToolBarButtonAction.getMode()) {
				case DELETE_GRAPH_NODE:
					if (sourceNode.isDeletable()) {
						List<GraphNode> parents = sourceNode.getParents();
						List<GraphNode> children = sourceNode.getChildren();

						sourceNode.removeParents();

						sourceNode.removeChildren();

						// Re-connect each parent with each child.
						for (GraphNode parent : parents) {
							for (GraphNode child : children) {
								parent.addChild(child);
							}
						}
					}
					break;

				case CONNECT_GRAPH_NODE:
					if (oldSelectedNode != null) {
						// Determine which node is shallower in the graph, and
						// which
						// is deeper.
						GraphNode shallowerNode = sourceNode
								.isDescendant(oldSelectedNode) ? oldSelectedNode
								: sourceNode;
						GraphNode deeperNode = sourceNode
								.isDescendant(oldSelectedNode) ? sourceNode
								: oldSelectedNode;

						// connect the nodes if not connected
						shallowerNode.addChild(deeperNode);

						// Reset the tool.
						this.gPanel.setOldSelectedNode(null);
					}
					// update the last selected node
					else
						this.gPanel.setOldSelectedNode(sourceNode);
					break;

				case DISCONNECT_GRAPH_NODE:
					if (oldSelectedNode != null) {
						// Determine which node is shallower in the graph, and
						// which
						// is deeper.
						GraphNode shallowerNode = sourceNode
								.isDescendant(oldSelectedNode) ? oldSelectedNode
								: sourceNode;
						GraphNode deeperNode = sourceNode
								.isDescendant(oldSelectedNode) ? sourceNode
								: oldSelectedNode;

						// Check that both nodes will still have at least one
						// parent and one child after the disconnect.
						if (shallowerNode.getChildren().size() > 1
								&& deeperNode.getParents().size() > 1) {
							shallowerNode.removeChild(deeperNode, false);
						}

						// Reset the tool.
						this.gPanel.setOldSelectedNode(null);
					}
					// update the last selected node
					else
						this.gPanel.setOldSelectedNode(sourceNode);
					break;
				}

			} else if (type == GraphNodeEventType.CONNECTION_ADDED) {
				GraphNode.observeDepthMap(this, sourceNode);
			}
		}
	}

	/**
	 * Private observer for the QuestToolBar. Also provides quest specific
	 * actions for the graph editor toolbar buttons.
	 * 
	 * @author kschenk
	 */
	private class QuestToolBarObserver implements GraphNodeObserver {
		final private JTextField nameField;
		final private JSpinner fanInSpinner;
		final private JLabel nameLabel;
		final private JLabel fanInLabel;

		private GraphNode previousNode;
		final private GraphPanel gPanel;

		/**
		 * Creates the observer for the quest toolbar. It requires the name
		 * textfield, a fan in spinner, and a quest editor.
		 * 
		 * @param nameField
		 *            The text field that edits the quest point's name.
		 * @param fanInSpinner
		 *            The spinner to edit the fan-in value.
		 * @param editor
		 */
		public QuestToolBarObserver(JTextField nameField,
				JSpinner fanInSpinner, JLabel nameLabel, JLabel fanInLabel,
				GraphPanel gPanel) {
			this.nameField = nameField;
			this.fanInSpinner = fanInSpinner;
			this.nameLabel = nameLabel;
			this.fanInLabel = fanInLabel;
			this.previousNode = gPanel.getHeadNode();
			this.gPanel = gPanel;
		}

		/**
		 * Swaps the selected state of the newly selected node.
		 * 
		 * @param previousNode
		 * @param currentNode
		 */
		protected void swapSelected(GraphNode previousNode,
				GraphNode currentNode) {
			if (previousNode != null)
				previousNode.setSelected(false);
			currentNode.setSelected(true);
		}

		/**
		 * Specialty method for deleting the quest node, rather than just a
		 * graph node.
		 * 
		 * @param sourceNode
		 * @param questPointNode
		 */
		private void deleteQuestNode(final GraphNode sourceNode,
				QuestPoint questPoint) {

			if (sourceNode.isDeletable()) {
				Collection<QuestPoint> successors = questPoint.getSuccessors();
				 List<GraphNode> parents = questPoint.getParents();

				// Only delete the node if there are parents and
				// children to repair the graph with.
				if (!parents.isEmpty() && !successors.isEmpty()) {

					
					// Remove the node from its parents.
					questPoint.removeParents();

					// Remove the node from its children.
					questPoint.removeChildren();

					// Re-connect each parent with each child.
					for (QuestPoint parent : parents) {
						for (QuestPoint child : children) {
							parent.addSuccessor(child);
						}
					}
				}
				// Subtracts 1 from fan in of all children.
				for (QuestPoint child : successors) {

					int fanIn = questPoint.getFanIn();

					if (fanIn > 1)
						questPoint.setFanIn(fanIn - 1);
				}
			}

			ToolBarFactory.this.updateQuestToolBar(
					QuestToolBarObserver.this.nameField,
					QuestToolBarObserver.this.fanInSpinner,
					QuestToolBarObserver.this.nameLabel,
					QuestToolBarObserver.this.fanInLabel, questPoint);

		}

		/**
		 * Method to abstract commonalities from the Insert QuestPoint between
		 * and alternate tools.
		 * 
		 * @param node
		 * 
		 * @author graves
		 */
		private void insertQuestPoint(GraphNode node) {
			// if this is the second click,
			GraphNode oldSelectedNode = this.gPanel.getOldSelectedNode();

			if (oldSelectedNode != null) {

				// create a new node to insert:
				QuestPoint newQuestPoint = new QuestPoint("");
				QuestPointNode newQuestPointNode = new QuestPointNode(
						newQuestPoint);

				// Cases for clicking the same node.
				if (oldSelectedNode == node) {

					if (oldSelectedNode == this.gPanel.getHeadNode()) {
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
					// determine which node is closer to the startNode in the
					// graph
					// (the parent) and which is further from the startNode (the
					// child).
					GraphNode closerToStartNode = node
							.isDescendant(oldSelectedNode) ? oldSelectedNode
							: node;
					GraphNode furtherFromStartNode = node
							.isDescendant(oldSelectedNode) ? node
							: oldSelectedNode;

					// Remove the old connection between the parent and child:
					closerToStartNode.removeChild(furtherFromStartNode, false);

					// Add the new node to the shallower node as a child
					// (addChild
					// automatically adds shallower as parent):
					closerToStartNode.addChild(newQuestPointNode);

					// Add the deeper node to the new node as a child.
					newQuestPointNode.addChild(furtherFromStartNode);
				}
				// Reset the tool:
				this.gPanel.setOldSelectedNode(null);

			} else {
				// otherwise this is the first click, so store the node for
				// later:
				this.gPanel.setOldSelectedNode(node);
			}
		}

		@Override
		public void nodeChanged(GraphNodeEvent event) {
			final GraphNode sourceNode = event.getSource();
			final GraphNodeEventType type = event.getEventType();

			if (type == GraphNodeEventType.SELECTED) {
				if (ToolBarButtonAction.getMode() != ToolBarButtonMode.DELETE_GRAPH_NODE) {
					this.swapSelected(this.previousNode, sourceNode);
					this.previousNode = sourceNode;
				}

				sourceNode.process(new GraphNodeAdapter() {
					@Override
					public void processQuestPointNode(
							QuestPointNode questPointNode) {
						switch (ToolBarButtonAction.getMode()) {

						case DISCONNECT_GRAPH_NODE:
							QuestPoint questPoint = questPointNode
									.getQuestPoint();
							int fanIn = questPoint.getFanIn();

							if (fanIn > 1)
								questPoint.setFanIn(fanIn - 1);

						case SELECT_GRAPH_NODE:

							final PatternModel model = PatternModelManager
									.getInstance().getActiveModel();

							if (model != null && model instanceof StoryModel) {
								sourceNode.process(new GraphNodeAdapter() {
									@Override
									public void processQuestPointNode(
											QuestPointNode questPointNode) {

										QuestPoint questPoint = questPointNode
												.getQuestPoint();

										List<JComponent> components = PanelFactory
												.getInstance()
												.getComponentsForModel(model);

										for (JComponent component : components)
											PanelFactory.getInstance()
													.setRootForTreeInComponent(
															component,
															questPoint);
									}
								});
							}
							ToolBarFactory.this.updateQuestToolBar(
									QuestToolBarObserver.this.nameField,
									QuestToolBarObserver.this.fanInSpinner,
									QuestToolBarObserver.this.nameLabel,
									QuestToolBarObserver.this.fanInLabel,
									questPointNode);
							break;
						case INSERT_GRAPH_NODE:
							QuestToolBarObserver.this
									.insertQuestPoint(sourceNode);
							ToolBarFactory.this.updateQuestToolBar(
									QuestToolBarObserver.this.nameField,
									QuestToolBarObserver.this.fanInSpinner,
									QuestToolBarObserver.this.nameLabel,
									QuestToolBarObserver.this.fanInLabel,
									questPointNode);
							break;
						case CONNECT_GRAPH_NODE:
							ToolBarFactory.this.updateQuestToolBar(
									QuestToolBarObserver.this.nameField,
									QuestToolBarObserver.this.fanInSpinner,
									QuestToolBarObserver.this.nameLabel,
									QuestToolBarObserver.this.fanInLabel,
									questPointNode);
							QuestToolBarObserver.this.previousNode = sourceNode;
							break;

						case DELETE_GRAPH_NODE:
							QuestToolBarObserver.this.deleteQuestNode(
									sourceNode, questPointNode);
							break;
						}
					}
				});

			} else if (type == GraphNodeEventType.CONNECTION_ADDED) {
				GraphNode.observeDepthMap(this, sourceNode);
			}
		}
	}

	/**
	 * Private observer for the DescribeIt ToolBar
	 * 
	 * @author kschenk
	 * 
	 */
	private static class DescribeItToolBarObserver implements GraphNodeObserver {
		/**
		 * Adds the node to the describe it observer if a new one is added.
		 */
		DescribeIt editedDescribeIt;
		CardLayout cardLayout;
		KnowItNodeEditor knowItEditor;
		TextNodeEditor textNodeEditor;
		JComponent describeItEditBar;
		PathAssigner pathEditor;

		public DescribeItToolBarObserver(DescribeIt editedDescribeIt,
				CardLayout cardLayout, JComponent describeItEditBar,
				KnowItNodeEditor knowItBar, TextNodeEditor textNodeEditor,
				PathAssigner pathEditor) {
			this.editedDescribeIt = editedDescribeIt;
			this.cardLayout = cardLayout;
			this.knowItEditor = knowItBar;
			this.textNodeEditor = textNodeEditor;
			this.describeItEditBar = describeItEditBar;
			this.pathEditor = pathEditor;
		}

		@Override
		public void nodeChanged(GraphNodeEvent event) {
			final GraphNode sourceNode = event.getSource();
			final GraphNodeEventType type = event.getEventType();

			if (type == GraphNodeEventType.SELECTED) {
				switch (ToolBarButtonAction.getMode()) {
				case INSERT_GRAPH_NODE:
					if (event.isShiftDown()) {
						TextNode textNode = new TextNode("New Text Node");
						sourceNode.addChild(textNode);
					} else {
						KnowItNode knowItNode = new KnowItNode(new KnowIt(
								"New Option Node"));
						sourceNode.addChild(knowItNode);
						break;
					}
					break;
				case SELECT_GRAPH_NODE:

					if (event.isShiftDown()) {
						this.editedDescribeIt.selectFromHeadToNode(sourceNode);
						/*
						 * only allow for path assigning on complete paths
						 * (finish with a terminal)
						 */
						if (sourceNode.isTerminalNode()) {

							System.out.println("Path Editor");
							this.pathEditor.setNode(this.editedDescribeIt);
							this.cardLayout.show(this.describeItEditBar,
									ToolBarFactory.PATH_EDITOR);
						} else {
							this.cardLayout.show(this.describeItEditBar,
									ToolBarFactory.NO_EDITOR);
						}
					} else {
						this.editedDescribeIt.clearSelection();
						sourceNode.setSelected(true);
						sourceNode.process(new GraphNodeAdapter() {

							@Override
							public void processTextNode(TextNode textNode) {
								DescribeItToolBarObserver.this.textNodeEditor
										.setNode(textNode);
								DescribeItToolBarObserver.this.cardLayout
										.show(DescribeItToolBarObserver.this.describeItEditBar,
												ToolBarFactory.TEXT_NODE_EDITOR);

							}

							@Override
							public void processKnowItNode(KnowItNode knowItNode) {
								DescribeItToolBarObserver.this.knowItEditor
										.setNode(knowItNode);
								DescribeItToolBarObserver.this.cardLayout
										.show(DescribeItToolBarObserver.this.describeItEditBar,
												ToolBarFactory.KNOW_IT_EDITOR);
							}
						});
					}
					break;
				}
			} else if (type == GraphNodeEventType.CONNECTION_ADDED) {
				GraphNode.observeDepthMap(this, sourceNode);
			}
		}
	}
}
