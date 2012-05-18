package scriptease.gui.graph.editor;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;

import scriptease.controller.observer.GraphNodeEvent;
import scriptease.controller.observer.GraphNodeEvent.GraphNodeEventType;
import scriptease.controller.observer.GraphNodeObserver;
import scriptease.gui.action.ToolBarButtonAction;
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
public abstract class GraphEditor extends JPanel implements
		GraphNodeObserver{

	// Enum for the possible tools supported in the graph
	protected enum GraphTool {
		NEW_TEXTNODE_TOOL, NEW_KNOWITNODE_TOOL, CONNECT_TOOL, 
		DELETE_TOOL, SELECT_NODE_TOOL, SELECT_PATH_TOOL, 
		QUESTPOINT_PROPERTIES_TOOL
	}

	private Point mousePosition = new Point();
	protected GraphNode headNode;
	protected GraphNode oldSelectedNode;
	private GraphTool activeTool;
	protected JPanel editingPanel;

	/**
	 * Constructor.
	 * 
	 * @param saveAction
	 */
	public GraphEditor() {
		super(new BorderLayout(), true);
		this.setOpaque(true);
		this.setBackground(StoryComponentPanel.UNSELECTED_COLOUR);
	}

	private void initialize() {
		// Observe nodes
		GraphNode.observeDepthMap(this, this.headNode);

		this.buildPanels();
	}

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
			this.editingPanel.add(panel, BorderLayout.SOUTH);
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
	protected void setGraphPanel(GraphPanel panel, Point position) {
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
		JScrollPane graphPanel = new JScrollPane(panel);
		graphPanel.getViewport().setViewPosition(position);
		this.editingPanel.add(new JScrollPane(panel), BorderLayout.CENTER);
	}
	
	/**
	 * Public method to add a toolbar to the graph editor.
	 * Use ToolBarFactory class for an appropriate toolbar, or 
	 * make your own.
	 * 
	 * @param toolBar
	 */
	public void addToolBar(JToolBar toolBar) {
		this.add(toolBar, BorderLayout.PAGE_START);
	}

	private void buildPanels() {

		//final JToolBar buttonToolBar = ToolBarFactory.buildQuestEditorToolBar();

		//this.setLeftComponent(buttonToolBar);

		this.editingPanel = new JPanel(new BorderLayout(), true);
		this.add(editingPanel, BorderLayout.CENTER);

		/**
		 * builds the default GraphPanel, can be override by calling
		 * setGraphPanel in the constructor of subclasses
		 */
		GraphPanel graphPanel = new GraphPanel(this.headNode);
		Point position = new Point(0,0);
		setGraphPanel(graphPanel, position);
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
	public void nodeChanged(GraphNodeEvent event) {
		final GraphNode sourceNode = event.getSource();
		final GraphNodeEventType type = event.getEventType();

		// only process clicked actions if you are contained in the active tab
		if (type == GraphNodeEventType.SELECTED
				) {
			// Determine what the active tool is
			switch (ToolBarButtonAction.getMode()) {
			case CONNECT_GRAPH_NODE:
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
					shallowerNode.addChild(deeperNode);
					
					// Reset the tool.
					oldSelectedNode = null;
				}
				// update the last selected node
				else
					oldSelectedNode = sourceNode;
				break;

			case DISCONNECT_GRAPH_NODE:
				if (oldSelectedNode != null) {
					// Determine which node is shallower in the graph, and which
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
					oldSelectedNode = null;
				}
				// update the last selected node
				else
					oldSelectedNode = sourceNode;
				break;
				
			case DELETE_GRAPH_NODE:
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
		} else if (type == GraphNodeEventType.CONNECTION_ADDED) {
			// Observe nodes
			GraphNode.observeDepthMap(this, sourceNode);
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