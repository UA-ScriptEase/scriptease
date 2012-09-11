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

import scriptease.controller.observer.graph.SEGraphObserver;
import scriptease.gui.SETree.ui.ScriptEaseUI;
import scriptease.gui.action.ToolBarButtonAction;
import scriptease.gui.graph.SEGraphModel.GraphEvent;
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

	/*
	 * Note: If we ever have to set builders and renderers, just remove the
	 * final modifier from these, build a new constructor that builds default
	 * builders/renderers, and create setters. Note that the setters will have
	 * to redraw the graph (which should be as simple as calling repaint and
	 * revalidate).
	 */
	private final SEGraphNodeBuilder<E> builder;
	private final SEGraphNodeRenderer<E> renderer;

	private E selectedNode;

	private Point mousePosition;

	/**
	 * Builds a new graph with the passed in start point, builder, and renderer.
	 * 
	 * @param start
	 *            The start point of the graph. Graphs must have a start point.
	 * @param builder
	 *            The node builder for the graph.
	 * @param renderer
	 *            The renderer for the graph.
	 */
	public SEGraph(E start, SEGraphNodeBuilder<E> builder,
			SEGraphNodeRenderer<E> renderer) {

		this.selectedNode = start;
		this.renderer = renderer;
		this.builder = builder;
		this.mousePosition = new Point();
		this.model = new SEGraphModel<E>(start, builder);

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
	 * Adds a node onto an existing node.
	 * 
	 * @param node
	 *            The node to add
	 * @param existingNode
	 *            The existing node
	 * @return true if the addition was successful
	 * @see SEGraphModel#addNodeTo(Object, Object)
	 */
	public void addNodeTo(E node, E existingNode) {
		this.model.addNodeTo(node, existingNode);

		this.repaint();
		this.revalidate();
	}

	/**
	 * Adds a node between two existing nodes. Order of the two nodes does not
	 * matter; this method figures out which node is above the other.
	 * 
	 * @param node
	 *            The new node.
	 * @param existingNode1
	 *            The first selected existing node.
	 * @param existingNode2
	 *            The second selected existing node.
	 * @return true if the addition was successful
	 * @see SEGraphModel#addNodeBetween(Object, Object, Object)
	 */
	public void addNodeBetween(E node, E existingNode1, E existingNode2) {
		this.model.addNodeBetween(node, existingNode1, existingNode2);

		this.repaint();
		this.revalidate();
	}

	/**
	 * Removes the node from the graph.
	 * 
	 * @param node
	 *            The node to be removed.
	 * @see SEGraphModel#removeNode(Object)
	 */
	public void removeNode(E node) {
		this.model.removeNode(node);

		this.repaint();
		this.revalidate();
	}

	/**
	 * Connects two nodes together. This method checks which node is further
	 * from the other and adds the appropriate node as a parent or a child.
	 * 
	 * @param node1
	 * @param node2
	 * @return True if the nodes were successfully connected.
	 * @see SEGraphModel#connectNodes(Object, Object)
	 */
	public boolean connectNodes(E node1, E node2) {
		final boolean result;

		result = this.model.connectNodes(node1, node2);

		this.repaint();
		this.revalidate();

		return result;
	}

	/**
	 * Disconnects two nodes. If the node had no other connections, this will
	 * result in a deletion.
	 * 
	 * @param node1
	 * @param node2
	 * @return True if the nodes were successfully disconnected.
	 * @see SEGraphModel#disconnectNodes(Object, Object)
	 */
	public boolean disconnectNodes(E node1, E node2) {
		final boolean result;

		result = this.model.disconnectNodes(node1, node2);

		this.repaint();
		this.revalidate();

		return result;
	}

	/**
	 * Adds an observer to the Graph to observe changes to the children and
	 * parents of nodes.
	 * 
	 * @param observer
	 *            The observer to be added
	 * @see SEGraphObserver
	 */
	public void addSEGraphObserver(SEGraphObserver<E> observer) {
		this.model.addSEGraphObserver(observer);
	}

	/**
	 * Removes an observer from the Graph.
	 * 
	 * @param observer
	 *            The observer to be removed
	 * @see SEGraphObserver
	 */
	public void removeSEGraphObserver(SEGraphObserver<E> observer) {
		this.model.removeSEGraphObserver(observer);
	}

	/**
	 * Sets the current selected node.
	 * 
	 * @param node
	 */
	public void setSelectedNode(E node) {
		this.selectedNode = node;
		this.renderer.setSelectedNode(node);
		this.model.notifyObservers(GraphEvent.NODE_SELECTED, node);
	}

	/**
	 * Returns the current selected node.
	 * 
	 * @return
	 */
	public E getSelectedNode() {
		return this.selectedNode;
	}

	/**
	 * Returns all nodes in the graph after and including the start node.
	 * 
	 * @return
	 */
	public Collection<E> getNodes() {
		return this.model.getNodes();
	}

	/**
	 * Returns the start node.
	 * 
	 * @return
	 */
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

		@Override
		public void addLayoutComponent(String name, Component comp) {
			// No extra registration of components is needed.
		}

		@Override
		public void removeLayoutComponent(Component comp) {
			// No extra de-registration of components is needed.
		}

		@Override
		public Dimension preferredLayoutSize(Container parent) {
			return minimumLayoutSize(parent);
		}

		@Override
		public Dimension minimumLayoutSize(Container parent) {
			// TODO THIS SEEMS WRONG? It's duplicate!
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

					if (component.getMouseListeners().length <= 1) {
						component.addMouseListener(new NodeMouseAdapter(node));
					}

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

	/**
	 * UI used to draw lines between each node.
	 * 
	 * @author kschenk
	 * 
	 */
	public class SEGraphUI extends ComponentUI {
		@Override
		public void paint(Graphics g, JComponent c) {
			g.setColor(SEGraph.this.getBackground());
			g.fillRect(0, 0, SEGraph.this.getWidth(), SEGraph.this.getHeight());

			// final Graphics2D g2 = (Graphics2D) g.create();
			/*
			 * if (SEGraph.this.previousSelectedNode != null &&
			 * SEGraph.this.mousePosition != null) { g2.setColor(Color.gray);
			 * g2.setStroke(new BasicStroke(1.5f)); GUIOp.paintArrow( g2,
			 * GUIOp.getMidRight(SEGraph.this.renderer
			 * .getComponentForNode(SEGraph.this.previousSelectedNode)),
			 * SEGraph.this.mousePosition); }
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

	/**
	 * Mouse adapter that gets added to every node. This is where the model gets
	 * updated based on the current tool selected in the Tool Bar.<br>
	 * <br>
	 * For graphical changes, see {@link SEGraphNodeRenderer}.
	 * 
	 * @author kschenk
	 * 
	 */
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
		public void mouseReleased(MouseEvent e) {
			switch (ToolBarButtonAction.getMode()) {
			case SELECT_GRAPH_NODE:
			case DELETE_GRAPH_NODE:
				final JComponent src = (JComponent) e.getSource();
				final Point mouseLoc = MouseInfo.getPointerInfo().getLocation();

				/*
				 * Only respond to releases that happen over this component. The
				 * default is to respond to releases if the press occurred in
				 * this component. This seems to be a Java bug, but I can't find
				 * any kind of complaint for it. Either way, we want this
				 * behaviour, not the default. - remiller
				 */
				if (!src.contains(mouseLoc.x - src.getLocationOnScreen().x,
						mouseLoc.y - src.getLocationOnScreen().y))
					return;
			}
			switch (ToolBarButtonAction.getMode()) {
			case SELECT_GRAPH_NODE:
				SEGraph.this.setSelectedNode(this.node);
				break;
			case INSERT_GRAPH_NODE:
				E newNode = SEGraph.this.builder.buildNewNode();

				if (SEGraph.this.lastEnteredNode != null)
					if (SEGraph.this.lastEnteredNode == this.node)
						SEGraph.this.addNodeTo(newNode, this.node);
					else
						SEGraph.this.addNodeBetween(newNode,
								SEGraph.this.lastEnteredNode, this.node);
				break;
			case DELETE_GRAPH_NODE:
				SEGraph.this.removeNode(this.node);
				SEGraph.this.validateSelectedNode();
				break;
			case CONNECT_GRAPH_NODE:
				if (SEGraph.this.lastEnteredNode != null
						&& SEGraph.this.lastEnteredNode != this.node)
					SEGraph.this.connectNodes(SEGraph.this.lastEnteredNode,
							this.node);
				break;
			case DISCONNECT_GRAPH_NODE:
				if (SEGraph.this.lastEnteredNode != null
						&& SEGraph.this.lastEnteredNode != this.node) {
					SEGraph.this.disconnectNodes(SEGraph.this.lastEnteredNode,
							this.node);
					SEGraph.this.validateSelectedNode();
				}
				break;
			}
		}
	}

	private void validateSelectedNode() {
		if (this.model.getStartNode() == this.getSelectedNode())
			return;

		if (this.model.getParents(this.getSelectedNode()).size() == 0)
			this.setSelectedNode(null);
		else
			this.renderer.setSelectedNode(this.getSelectedNode());
	}
}
