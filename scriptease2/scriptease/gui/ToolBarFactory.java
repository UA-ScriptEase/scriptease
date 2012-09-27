package scriptease.gui;

import java.awt.CardLayout;
import java.awt.Color;
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
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import scriptease.controller.GraphNodeAdapter;
import scriptease.controller.observer.graph.GraphNodeEvent;
import scriptease.controller.observer.graph.GraphNodeEvent.GraphNodeEventType;
import scriptease.controller.observer.graph.GraphNodeObserver;
import scriptease.gui.SEGraph.GraphPanel;
import scriptease.gui.SEGraph.editor.KnowItNodeEditor;
import scriptease.gui.SEGraph.editor.PathAssigner;
import scriptease.gui.SEGraph.editor.TextNodeEditor;
import scriptease.gui.SEGraph.nodes.GraphNode;
import scriptease.gui.SEGraph.nodes.KnowItNode;
import scriptease.gui.SEGraph.nodes.TextNode;
import scriptease.gui.action.graphs.ConnectModeAction;
import scriptease.gui.action.graphs.DeleteModeAction;
import scriptease.gui.action.graphs.DisconnectModeAction;
import scriptease.gui.action.graphs.GraphToolBarModeAction;
import scriptease.gui.action.graphs.InsertModeAction;
import scriptease.gui.action.graphs.SelectModeAction;
import scriptease.model.atomic.DescribeIt;
import scriptease.model.atomic.KnowIt;

/**
 * ToolBarFactory is responsible for creating all JToolBars.
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
	 * 
	 * @deprecated Once we move DescribeIt stuff out of this factory, we will be
	 *             able to delete this, too.
	 */
	private Map<JToolBar, GraphNodeObserver> observerMap = new LinkedHashMap<JToolBar, GraphNodeObserver>();

	/**
	 * Builds a ToolBar to edit graphs with. Includes buttons for selecting
	 * nodes, adding and deleting nodes, and adding and deleting paths. The
	 * ToolBar buttons only set the mode; the graph itself contains the specific
	 * actions that should happen.
	 * 
	 * @return
	 */
	public JToolBar buildGraphEditorToolBar() {
		final JToolBar graphEditorToolBar = new JToolBar();

		final ButtonGroup graphEditorButtonGroup = new ButtonGroup();

		final ArrayList<JToggleButton> buttonList = new ArrayList<JToggleButton>();

		final JToggleButton selectNodeButton = new JToggleButton(
				SelectModeAction.getInstance());

		final JToggleButton insertNodeButton = new JToggleButton(
				InsertModeAction.getInstance());

		final JToggleButton deleteNodeButton = new JToggleButton(
				DeleteModeAction.getInstance());

		final JToggleButton connectNodeButton = new JToggleButton(
				ConnectModeAction.getInstance());

		final JToggleButton disconnectNodeButton = new JToggleButton(
				DisconnectModeAction.getInstance());

		graphEditorToolBar.setLayout(new BoxLayout(graphEditorToolBar,
				BoxLayout.LINE_AXIS));
		graphEditorToolBar.setRollover(true);
		graphEditorToolBar.setFloatable(false);
		graphEditorToolBar.setBackground(Color.WHITE);

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
				switch (GraphToolBarModeAction.getMode()) {

				case SELECT:
					graphEditorButtonGroup.setSelected(
							selectNodeButton.getModel(), true);
					break;
				case DELETE:
					graphEditorButtonGroup.setSelected(
							deleteNodeButton.getModel(), true);
					break;
				case INSERT:
					graphEditorButtonGroup.setSelected(
							insertNodeButton.getModel(), true);
					break;
				case CONNECT:
					graphEditorButtonGroup.setSelected(
							connectNodeButton.getModel(), true);
					break;
				case DISCONNECT:
					graphEditorButtonGroup.setSelected(
							disconnectNodeButton.getModel(), true);
					break;
				}
			}
		};

		PanelFactory.getInstance().getModelTabPane()
				.addChangeListener(graphEditorListener);

		return graphEditorToolBar;
	}

	/**
	 * Creates a JToolBar for editing DescribeIts. Uses the same buttons as
	 * graph editor toolbar.
	 * 
	 * @param gPanel
	 * @deprecated This needs to be refactored to work with SEGraph instead.
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
	 * Private observer for the DescribeIt ToolBar
	 * 
	 * @author kschenk
	 * @deprecated We need to get rid of this class.
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
				switch (GraphToolBarModeAction.getMode()) {
				case INSERT:
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
				case SELECT:

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
				default:
					break;
				}
			} else if (type == GraphNodeEventType.CONNECTION_ADDED) {
				GraphNode.observeDepthMap(this, sourceNode);
			}
		}
	}
}
