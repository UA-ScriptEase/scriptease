package scriptease.gui.graph.editor;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

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
public abstract class GraphEditor extends JPanel {

	private Point mousePosition = new Point();
	protected GraphNode headNode;
	protected static GraphNode oldSelectedNode;

	public static GraphNode getOldSelectedNode(){
		return GraphEditor.oldSelectedNode;
	}
	
	public static void setOldSelectedNode(GraphNode oldNode) {
		GraphEditor.oldSelectedNode = oldNode;
	}
	
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
				GraphEditor.oldSelectedNode = null;
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
		
	//	GraphNode.observeDepthMap(this, this.headNode);

		this.getHeadNode().setSelected(true);
	}
	
	public GraphNode getHeadNode() {
		return this.headNode;
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

			if (GraphEditor.oldSelectedNode != null && mousePosition != null) {
				g2.setColor(Color.GRAY);
				g2.setStroke(new BasicStroke(1.5f));
				GUIOp.paintArrow(g2, GUIOp.getMidRight(componentBuilder
						.getComponentForNode(GraphEditor.oldSelectedNode)), mousePosition);
			}
			super.paint(g, c);
		}
	}
}