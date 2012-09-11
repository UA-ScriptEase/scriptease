package scriptease.gui;

import java.awt.CardLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import scriptease.controller.GraphNodeAdapter;
import scriptease.controller.observer.graph.GraphNodeEvent;
import scriptease.controller.observer.graph.GraphNodeEvent.GraphNodeEventType;
import scriptease.controller.observer.graph.GraphNodeObserver;
import scriptease.gui.action.ToolBarButtonAction;
import scriptease.gui.action.story.graphs.ConnectGraphPointAction;
import scriptease.gui.action.story.graphs.DeleteGraphNodeAction;
import scriptease.gui.action.story.graphs.DisconnectGraphPointAction;
import scriptease.gui.action.story.graphs.InsertGraphNodeAction;
import scriptease.gui.action.story.graphs.SelectGraphNodeAction;
import scriptease.gui.graph.GraphPanel;
import scriptease.gui.graph.editor.KnowItNodeEditor;
import scriptease.gui.graph.editor.PathAssigner;
import scriptease.gui.graph.editor.TextNodeEditor;
import scriptease.gui.graph.nodes.GraphNode;
import scriptease.gui.graph.nodes.KnowItNode;
import scriptease.gui.graph.nodes.TextNode;
import scriptease.gui.quests.StoryPoint;
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
	public JToolBar buildGraphEditorToolBar() {
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

		SEFrame.getInstance().getModelTabPane()
				.addChangeListener(graphEditorListener);

		return graphEditorToolBar;
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
		final JToolBar describeItToolBar = this.buildGraphEditorToolBar();
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
			StoryPoint questNode) {

		if (questNode != null) {
			// XXX Max Fan In = questPoint parents size!

			int maxFanIn = 0;
			// questNode.getParents().size();

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
	 * Private observer for the QuestToolBar. Also provides quest specific
	 * actions for the graph editor toolbar buttons.
	 * 
	 * @author kschenk
	 */
	private class QuestToolBarObserver implements GraphNodeObserver {
		@Override
		public void nodeChanged(GraphNodeEvent event) {
			final GraphNodeEventType type = event.getEventType();

			// XXX Temporary Quest Point so this class doesn't generate a
			// billion errors. XXX
			StoryPoint questPoint = new StoryPoint("");

			if (type == GraphNodeEventType.SELECTED) {
				switch (ToolBarButtonAction.getMode()) {

				case DISCONNECT_GRAPH_NODE:
					int fanIn = questPoint.getFanIn();

					if (fanIn > 1)
						questPoint.setFanIn(fanIn - 1);
				}
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
