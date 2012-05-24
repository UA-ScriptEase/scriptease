package scriptease.gui.graph.editor;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

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
public abstract class GraphEditor extends JPanel implements GraphNodeObserver {

	private Point mousePosition = new Point();
	protected GraphNode headNode;
	protected GraphNode oldSelectedNode;

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
	

	/**
	 * Builds the panels for the GraphEditor.
	 */
	protected void buildPanels() {
		GraphPanel graphPanel = new GraphPanel(this.getHeadNode());
	
		ToolBarButtonAction.addJComponent(graphPanel);

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
		graphPanel.addMouseListener(connectArrowListener);
		graphPanel.addMouseMotionListener(connectArrowListener);
		// override the UI with the GraphEditors UI
		graphPanel.setUI(new GraphEditorUI(graphPanel));

		this.add(new JScrollPane(graphPanel), BorderLayout.CENTER);
		
	}

	public void setHeadNode(GraphNode graphNode) {
		if (graphNode == null)
			throw new IllegalArgumentException("Cannot set head node to null");
		this.headNode = graphNode;
		
		GraphNode.observeDepthMap(this, this.headNode);

		this.getHeadNode().setSelected(true);
	}
	
	public GraphNode getHeadNode() {
		return this.headNode;
	}

	@Override
	public void nodeChanged(GraphNodeEvent event) {
		final GraphNode sourceNode = event.getSource();
		final GraphNodeEventType type = event.getEventType();

		// only process clicked actions if you are contained in the active tab
		if (type == GraphNodeEventType.SELECTED) {

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
			}
		} else if (type == GraphNodeEventType.CONNECTION_ADDED) {
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

			final Graphics2D g2 = (Graphics2D) g.create();

			if (oldSelectedNode != null && mousePosition != null) {
				g2.setColor(Color.GRAY);
				g2.setStroke(new BasicStroke(1.5f));
				GUIOp.paintArrow(g2, GUIOp.getMidRight(componentBuilder
						.getComponentForNode(oldSelectedNode)), mousePosition);
			}
			super.paint(g, c);
		}
	}
}