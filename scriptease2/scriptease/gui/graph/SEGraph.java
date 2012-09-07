package scriptease.gui.graph;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;

import scriptease.gui.SETree.ui.ScriptEaseUI;
import scriptease.gui.action.ToolBarButtonAction;
import scriptease.gui.graph.builders.SEGraphNodeBuilder;
import scriptease.gui.graph.renderers.SEGraphNodeRenderer;
import scriptease.util.GUIOp;
import sun.awt.util.IdentityArrayList;

/**
 * Builds a directed, acyclic graph that must have a start node.
 * 
 * @author kschenk
 * 
 * @param <E>
 */
@SuppressWarnings("serial")
public class SEGraph<E> extends JComponent {
	private final SEGraphModel<E> model;

	private SEGraphNodeRenderer<E> renderer;
	private SEGraphNodeBuilder<E> builder;

	private E selectedNode;
	private E previousSelectedNode;

	private Point mousePosition;

	/**
	 * Builds a new SEGraph.
	 * 
	 * @param start
	 */
	public SEGraph(E start) {
		this.model = new SEGraphModel<E>(start);

		this.selectedNode = start;
		this.previousSelectedNode = null;
		this.renderer = new SEGraphNodeRenderer<E>(this);
		this.builder = new SEGraphNodeBuilder<E>();
		this.mousePosition = new Point();

		this.setLayout(new SEGraphLayoutManager());

		this.setUI(new SEGraphUI());
		this.setOpaque(true);
		this.setBackground(ScriptEaseUI.UNSELECTED_COLOUR);

		this.addMouseMotionListener(new MouseMotionListener() {
			@Override
			public void mouseDragged(MouseEvent e) {
				SEGraph.this.mousePosition.setLocation(e.getPoint());
			}

			@Override
			public void mouseMoved(MouseEvent e) {
				SEGraph.this.mousePosition.setLocation(e.getPoint());
			}
		});
	}

	/**
	 * Set the node renderer.
	 * 
	 * @param renderer
	 *            The node renderer.
	 * @see SEGraphNodeRenderer
	 */
	public void setRenderer(SEGraphNodeRenderer<E> renderer) {
		this.renderer = renderer;
	}

	/**
	 * Sets the node builder. This must be set in order for new nodes to be
	 * added.
	 * 
	 * @param builder
	 *            The node builder.
	 * @see SEGraphNodeBuilder
	 */
	public void setBuilder(SEGraphNodeBuilder<E> builder) {
		this.builder = builder;
	}

	public void addNodeTo(E node, E existingNode) {
		this.model.addNodeTo(node, existingNode);

		this.repaint();
		this.revalidate();
	}

	public void addNodeBetween(E node, E existingNode1, E existingNode2) {
		this.model.addNodeBetween(node, existingNode1, existingNode2);

		this.repaint();
		this.revalidate();
	}

	public void removeNode(E node) {
		this.model.removeNode(node);

		this.repaint();
		this.revalidate();
	}

	public boolean connectNodes(E node1, E node2) {
		return this.model.connectNodes(node1, node2);
	}

	public boolean disconnectNodes(E node1, E node2) {
		return this.model.disconnectNodes(node1, node2);
	}

	public void setSelectedNode(E node) {
		this.selectedNode = node;
	}

	public E getSelectedNode() {
		return this.selectedNode;
	}

	public Collection<E> getNodes() {
		return this.model.getNodes();
	}

	public E getStartNode() {
		return this.model.getStartNode();
	}

	/**
	 * The class that handles the actual laying out of GraphNodes. The logic is
	 * fairly basic, and should probably be updated to handle more cases.
	 * 
	 * @author graves
	 * @author kschenk
	 */
	private class SEGraphLayoutManager implements LayoutManager {
		private static final int HORIZONTAL_INDENT = 20;
		private static final int NODE_Y_INDENT = 10;

		protected void clearDisplayPanel() {
			SEGraph.this.removeAll();
		}

		// No extra registration of components is needed.
		@Override
		public void addLayoutComponent(String name, Component comp) {
		}

		// No extra de-registration of components is needed.
		@Override
		public void removeLayoutComponent(Component comp) {
		}

		@Override
		public Dimension preferredLayoutSize(Container parent) {
			return minimumLayoutSize(parent);
		}

		@Override
		public Dimension minimumLayoutSize(Container parent) {
			// TODO THIS SEEMS WRONG. It's duplicate!
			final Insets insets = parent.getInsets();
			int xSize = insets.left + insets.right;
			int ySize = insets.top + insets.bottom; // Get the nodes level map
			Map<E, Integer> nodeMap = SEGraph.this.model.getDepthMap();

			// Get the number of levels in the graph.
			int numberOfLevels = Collections.max(nodeMap.values());

			// For each level in the graph,
			for (int currentLevel = 0; currentLevel <= numberOfLevels; currentLevel++) {

				final List<E> currentNodes = new ArrayList<E>();
				// extract nodes for the level
				for (Entry<E, Integer> entry : nodeMap.entrySet()) {
					if (entry.getValue() == currentLevel)
						currentNodes.add(entry.getKey());
				}

				// Get the width of the widest JComponent in the level.
				xSize += this.getMaxWidth(currentNodes) + HORIZONTAL_INDENT;

				int yNodeSize = NODE_Y_INDENT;
				for (E node : currentNodes) {
					JComponent component = SEGraph.this.renderer
							.getComponentForNode(node);
					yNodeSize += NODE_Y_INDENT
							+ component.getPreferredSize().getHeight();
				}
				ySize = Math.max(ySize, yNodeSize);
			}
			return new Dimension(xSize, ySize);

		}

		@Override
		public void layoutContainer(Container parent) {
			// Remove the current contents of the panel.
			clearDisplayPanel();

			int xLocation = HORIZONTAL_INDENT;
			int yLocation = 0;

			// Get the nodes level map
			Map<E, Integer> nodeMap = SEGraph.this.model.getDepthMap();

			// Get the number of levels in the graph.
			int numberOfLevels = Collections.max(nodeMap.values());

			// Preferred size of the graph
			final Dimension preferredSize = SEGraph.this.getPreferredSize();

			// For each level in the graph,
			List<E> currentNodes = null;
			List<E> previousNodes = null;
			for (int currentLevel = 0; currentLevel <= numberOfLevels; currentLevel++) {
				currentNodes = sortChildrenByParent(previousNodes,
						getNodesForLevel(nodeMap, currentLevel));

				// Get the width of the widest JComponent in the level.
				final int maxWidth = this.getMaxWidth(currentNodes);

				// Get the number of nodes in the level to compute the y
				// location for each node.
				final int numberOfNodes = currentNodes.size();

				// Compute the number of y pixels each node is allocated
				double pixelsPerNode = preferredSize.getHeight()
						/ numberOfNodes;

				// For each node at that level,
				for (int currentNode = 0; currentNode < numberOfNodes; currentNode++) {
					// Get a reference to the current node.
					final E node = currentNodes.get(currentNode);

					// Get the JComponent associated with the node.
					final JComponent component = SEGraph.this.renderer
							.getComponentForNode(node);

					if (component.getMouseListeners().length <= 1)
						component.addMouseListener(new NodeMouseAdapter(node));

					// Get the JComponent preferred width
					final int nodeWidth = (int) component.getPreferredSize()
							.getWidth();

					// Compute the y Location for the node.
					double yNodeLocation = yLocation
							+ (pixelsPerNode * (currentNode + 1) - 0.5
									* pixelsPerNode - 0.5 * component
									.getPreferredSize().getHeight());

					// Add the component to the panel, so it will draw
					SEGraph.this.add(component);

					// Center smaller nodes according to the biggest
					int xOffset = 0;
					if (nodeWidth != maxWidth) {
						xOffset = (maxWidth - nodeWidth) / 2;
					}

					// Set the size and location of the component
					component.setLocation(xOffset + xLocation,
							(int) yNodeLocation);
					component.setSize(new Dimension(nodeWidth, (int) component
							.getPreferredSize().getHeight()));
				}

				// Update the x location for the next level.
				xLocation = xLocation + maxWidth + HORIZONTAL_INDENT;
				previousNodes = currentNodes;
			}
		}

		/**
		 * Sorts the given children in a fashion that has overlapping lines
		 * 
		 * @param parentNodes
		 * @param childNodes
		 * @return
		 */
		private List<E> sortChildrenByParent(List<E> parentNodes,
				List<E> childNodes) {
			if (parentNodes == null || parentNodes.isEmpty()
					|| childNodes == null || childNodes.isEmpty())
				return childNodes;

			final List<E> sortedChildren = new IdentityArrayList<E>(
					childNodes.size());
			int newIndex = 0;
			for (E parentNode : parentNodes) {
				int parentIndex = newIndex;
				Collection<E> children = SEGraph.this.model
						.getChildren(parentNode);
				for (E childNode : childNodes) {
					if (children.contains(childNode)) {
						// if the child has already been organized, move it to
						// the first element of the current parent
						if (sortedChildren.contains(childNode)) {
							sortedChildren.remove(childNode);
							sortedChildren.add(parentIndex - 1, childNode);
						} else {
							// insert the childNode near the parent's index
							sortedChildren.add(newIndex, childNode);
							newIndex++;
						}
					}
				}
			}
			return sortedChildren;
		}

		private List<E> getNodesForLevel(Map<E, Integer> nodeMap, int level) {
			List<E> currentNodes = new ArrayList<E>();
			// extract nodes for the level
			for (Entry<E, Integer> entry : nodeMap.entrySet()) {
				if (entry.getValue() == level)
					currentNodes.add(entry.getKey());
			}
			return currentNodes;
		}

		/**
		 * Returns the width of the widest JComponent in the collection of
		 * nodes.
		 * 
		 * @param nodes
		 * @return
		 */
		private int getMaxWidth(Collection<E> nodes) {
			int maxWidth = 0;

			for (E node : nodes) {
				// Get the component for the node.
				JComponent component = SEGraph.this.renderer
						.getComponentForNode(node);

				// Get the size of the JComponent.
				Dimension componentSize = component.getPreferredSize();

				// Check for a new maximum.
				if (componentSize.getWidth() > maxWidth) {
					maxWidth = (int) componentSize.getWidth();
				}
			}
			// Return the maximum width.
			return maxWidth;
		}
	}

	public class SEGraphUI extends ComponentUI {

		@Override
		public void paint(Graphics g, JComponent c) {
			g.setColor(SEGraph.this.getBackground());
			g.fillRect(0, 0, SEGraph.this.getWidth(), SEGraph.this.getHeight());

			final Graphics2D g2 = (Graphics2D) g.create();
/*
			if (SEGraph.this.previousSelectedNode != null
					&& SEGraph.this.mousePosition != null) {
				g2.setColor(Color.gray);
				g2.setStroke(new BasicStroke(1.5f));
				GUIOp.paintArrow(
						g2,
						GUIOp.getMidRight(SEGraph.this.renderer
								.getComponentForNode(SEGraph.this.previousSelectedNode)),
						SEGraph.this.mousePosition);
			}
*/
			connectNodes(g);
		}

		private void connectNodes(Graphics g) {
			// Get the nodes level map
			Map<E, Integer> nodeMap = SEGraph.this.model.getDepthMap();

			// Clone the graphics context.
			final Graphics2D g2 = (Graphics2D) g.create();

			// Get the number of levels in the graph.
			int numberOfLevels = Collections.max(nodeMap.values());

			// For each level in the graph
			for (int currentLevel = 0; currentLevel <= numberOfLevels; currentLevel++) {
				final List<E> currentNodes = new ArrayList<E>();
				// extract nodes for the level
				for (Entry<E, Integer> entry : nodeMap.entrySet()) {
					if (entry.getValue() == currentLevel)
						currentNodes.add(entry.getKey());
				}

				// for each node in the level
				for (E parent : currentNodes) {
					// Get the children of the node.
					Collection<E> children = SEGraph.this.model
							.getChildren(parent);

					// For each child,
					for (E child : children) {

						// Set the line stroke
						BasicStroke stroke = new BasicStroke(1.5f);
						g2.setStroke(stroke);

						// Set color of line based on selection
						Color lineColour;
						lineColour = Color.GRAY;
						g2.setColor(lineColour);

						// Draw an arrow pointing towards the child.
						GUIOp.paintArrow(g2, GUIOp
								.getMidRight(SEGraph.this.renderer
										.getComponentForNode(parent)), GUIOp
								.getMidLeft(SEGraph.this.renderer
										.getComponentForNode(child)));
					}
				}
			}
			// clean up
			g.dispose();
		}
	}

	private E lastEnteredNode = null;

	private class NodeMouseAdapter extends MouseAdapter {
		private final E node;

		private NodeMouseAdapter(E node) {
			this.node = node;
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			SEGraph.this.lastEnteredNode = this.node;
		}

		@Override
		public void mousePressed(MouseEvent e) {
			SEGraph.this.previousSelectedNode = this.node;
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			switch (ToolBarButtonAction.getMode()) {

			case SELECT_GRAPH_NODE:
				SEGraph.this.setSelectedNode(this.node);
				break;
			case INSERT_GRAPH_NODE:
				E newNode = SEGraph.this.builder.buildNewNode();

				if (newNode != null) {
					if (SEGraph.this.lastEnteredNode != null)
						if (SEGraph.this.lastEnteredNode == this.node)
							SEGraph.this.addNodeTo(newNode, this.node);
						else
							SEGraph.this.addNodeBetween(newNode,
									SEGraph.this.lastEnteredNode,
									this.node);
				}
				break;
			case DELETE_GRAPH_NODE:
				SEGraph.this.removeNode(this.node);
				break;
			case CONNECT_GRAPH_NODE:
				if (SEGraph.this.previousSelectedNode != null)
					SEGraph.this.connectNodes(this.node,
							SEGraph.this.previousSelectedNode);
				break;
			case DISCONNECT_GRAPH_NODE:
				if (SEGraph.this.previousSelectedNode != null)
					SEGraph.this.disconnectNodes(this.node,
							SEGraph.this.previousSelectedNode);
			}
		}
	}
}
