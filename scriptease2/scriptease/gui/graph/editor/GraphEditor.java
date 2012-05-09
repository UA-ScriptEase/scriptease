package scriptease.gui.graph.editor;

import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;

import scriptease.controller.GraphNodeObserverAdder;
import scriptease.controller.observer.GraphNodeEvent;
import scriptease.controller.observer.GraphNodeObserver;
import scriptease.gui.ToolBarFactory;
import scriptease.gui.graph.GraphPanel;
import scriptease.gui.graph.GraphPanel.GraphPanelUI;
import scriptease.gui.graph.nodes.GraphNode;
import scriptease.gui.storycomponentpanel.StoryComponentPanel;
import scriptease.util.GUIOp;

/**
 * GraphEditor provides a common interface for editing Graphs containing
 * GraphNodes in ScriptEase. Used to back DescribeIts and Quests, and re-use
 * common code.
 * 
 * Users select the active tool, which dictates what actions should be taken
 * when a GraphNode is clicked.
 * 
 * Observes all of the GraphNodes in the graph in order to know when nodes are
 * clicked.
 * 
 * @author mfchurch
 * 
 */
@SuppressWarnings("serial")
public abstract class GraphEditor extends JSplitPane implements
		GraphNodeObserver, ActionListener {

	// Enum for the possible tools supported in the graph
	protected enum GraphTool {
		NEW_TEXTNODE_TOOL, NEW_KNOWITNODE_TOOL, CONNECT_TOOL, DELETE_TOOL, SELECT_NODE_TOOL, SELECT_PATH_TOOL, INSERT_QUESTPOINTNODE_BETWEEN_TOOL, INSERT_QUESTPOINTNODE_ALTERNATE_TOOL, RENAME_QUESTPOINT_TOOL, CREATE_QUEST_TOOL, OPEN_QUESTPOINT_TOOL, QUESTPOINT_PROPERTIES_TOOL
	}

	private Point mousePosition = new Point();
	protected GraphNode headNode;
	protected GraphNode oldSelectedNode;
	private GraphTool activeTool;
	protected JSplitPane editingPanel;
	private AbstractAction saveAction;

	// //This is a HAck

	public GraphEditor(AbstractAction saveAction) {
		super(JSplitPane.VERTICAL_SPLIT, true);
		this.saveAction = saveAction;
		this.setOpaque(true);
		this.setBackground(StoryComponentPanel.UNSELECTED_COLOUR);
	}

	private void initialize() {
		// Observe nodes
		GraphNodeObserverAdder adder = new GraphNodeObserverAdder();
		adder.observeDepthMap(this, this.headNode);

		this.buildPanels();
	}

	/**
	 * JButtons which represent the types of nodes that can be created
	 */
	protected abstract Collection<AbstractButton> getNodeButtons();

	/**
	 * JButtons which represent the types of selection that can be made
	 */
	protected abstract Collection<AbstractButton> getSelectButtons();

	/**
	 * Sets the activeTool to the given tool. Clears the oldSelectedNode.
	 * 
	 * @param tool
	 */
	protected void setActiveTool(GraphTool tool) {
		this.activeTool = tool;
		this.oldSelectedNode = null;
	}

	protected void setEditingPanel(JPanel panel) {
		if (this.editingPanel != null)
			this.editingPanel.setBottomComponent(panel);
	}

	/**
	 * Gets the current active tool
	 * 
	 * @return
	 */
	protected GraphTool getActiveTool() {
		return this.activeTool;
	}

	/**
	 * Sets the given GraphPanel as the TopComponent in the editingPanel, and
	 * registers the appropriate listeners
	 * 
	 * @param panel
	 */
	protected void setGraphPanel(GraphPanel panel) {
		// Listener for the connect arrow
		MouseAdapter connectArrowListener = new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				oldSelectedNode = null;
				repaint();
			}

			@Override
			public void mouseDragged(MouseEvent e) {
				mousePosition.setLocation(e.getPoint());
				repaint();
			}

			@Override
			public void mouseMoved(MouseEvent e) {
				mousePosition.setLocation(e.getPoint());
				repaint();
			}
		};
		panel.addMouseListener(connectArrowListener);
		panel.addMouseMotionListener(connectArrowListener);
		// override the UI with the GraphEditors UI
		panel.setUI(new GraphEditorUI(panel));

		// set the graphPanel as the top component
		this.editingPanel.setTopComponent(new JScrollPane(panel));
	}
	
	/**
	 * Public method to add a toolbar to the graph editor.
	 * Use ToolBarFactory class for an appropriate toolbar, or 
	 * make your own.
	 * 
	 * @param toolBar
	 */
	public void addToolBar(JToolBar toolBar) {
		this.setLeftComponent(toolBar);
	}

	private void buildPanels() {

		//final JToolBar buttonToolBar = ToolBarFactory.buildQuestEditorToolBar();

		//this.setLeftComponent(buttonToolBar);

		this.editingPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true);
		this.setRightComponent(this.editingPanel);

		/**
		 * builds the default GraphPanel, can be override by calling
		 * setGraphPanel in the constructor of subclasses
		 */
		GraphPanel graphPanel = new GraphPanel(this.headNode);
		setGraphPanel(graphPanel);
	}

	public void setHeadNode(GraphNode graphNode) {
		if (graphNode == null)
			throw new IllegalArgumentException("Cannot set head node to null");
		this.headNode = graphNode;
		initialize();
	}

	public GraphNode getHeadNode() {
		return this.headNode;
	}

	@Override
	public void nodeChanged(GraphNode node, GraphNodeEvent event) {
		final GraphNode sourceNode = event.getSource();
		final short type = event.getEventType();

		// only process clicked actions if you are contained in the active tab
		if (type == GraphNodeEvent.CLICKED
				) {
			// Determine what the active tool is
			switch (this.activeTool) {
			case CONNECT_TOOL:
				if (oldSelectedNode != null) {
					// Determine which node is shallower in the graph, and which
					// is deeper.
					GraphNode shallowerNode = sourceNode
							.isDescendant(oldSelectedNode) ? oldSelectedNode
							: sourceNode;
					GraphNode deeperNode = sourceNode
							.isDescendant(oldSelectedNode) ? sourceNode
							: oldSelectedNode;

					// connect the nodes if not connected
					if (!shallowerNode.addChild(deeperNode)) {
						// otherwise break the connection, as long as it doesn't
						// break the graph.

						// Check that both nodes will still have at least one
						// parent and one child after the disconnect.
						if (shallowerNode.getChildren().size() > 1
								&& deeperNode.getParents().size() > 1) {
							shallowerNode.removeChild(deeperNode, false);
						}
					}
					// Reset the tool.
					oldSelectedNode = null;
				}
				// update the last selected node
				else
					oldSelectedNode = sourceNode;
				break;
			case DELETE_TOOL:
				List<GraphNode> parents = sourceNode.getParents();
				List<GraphNode> children = sourceNode.getChildren();

				// Remove the node from its parents.
				sourceNode.removeParents();

				// Remove the node from its children.
				sourceNode.removeChildren();

				// Re-connect each parent with each child.
				for (GraphNode parent : parents) {
					for (GraphNode child : children) {
						parent.addChild(child);
					}
				}
				break;
			}
		} else if (type == GraphNodeEvent.CONNECTION_ADDED) {
			// Observe nodes
			GraphNodeObserverAdder adder = new GraphNodeObserverAdder();
			adder.observeDepthMap(this, sourceNode);
		}
	}

	/**
	 * UI class which allows an arrow to be traced from clicked nodes to the
	 * cursor
	 * 
	 * @author mfchurch
	 * 
	 */
	private class GraphEditorUI extends GraphPanelUI {
		public GraphEditorUI(GraphPanel panel) {
			panel.super();
		}

		@Override
		public void paint(Graphics g, JComponent c) {
			// Clone the graphics context.
			final Graphics2D g2 = (Graphics2D) g.create();

			if (oldSelectedNode != null && mousePosition != null) {
				g2.setColor(oldSelectedNode.getUnselectedColour());
				g2.setStroke(new BasicStroke(1.5f));
				GUIOp.paintArrow(g2, GUIOp.getMidRight(componentBuilder
						.getComponentForNode(oldSelectedNode)), mousePosition);
			}
			super.paint(g, c);
		}
	}
}