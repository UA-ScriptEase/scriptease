package scriptease.gui.SEGraph;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.plaf.ComponentUI;

import scriptease.controller.observer.graph.SEGraphObserver;
import scriptease.gui.ComponentFocusManager;
import scriptease.gui.SEGraph.SEGraphModel.GraphEvent;
import scriptease.gui.SEGraph.builders.SEGraphNodeBuilder;
import scriptease.gui.SEGraph.renderers.SEGraphNodeRenderer;
import scriptease.gui.SETree.ui.ScriptEaseUI;
import scriptease.gui.action.graphs.GraphToolBarModeAction;
import scriptease.gui.action.graphs.GraphToolBarModeAction.ToolBarMode;
import scriptease.util.BiHashMap;
import scriptease.util.GUIOp;
import sun.awt.util.IdentityArrayList;

/**
 * Builds a directed, acyclic graph that must have a start node. Each graph must
 * be created with an {@link SEGraphNodeBuilder}.
 * 
 * @author kschenk
 * 
 * @param <E>
 */
@SuppressWarnings("serial")
public class SEGraph<E> extends JComponent {
	private final SEGraphModel<E> model;
	private final SEGraphNodeBuilder<E> builder;
	private final SEGraphObserver<E> modelObserver;

	private SEGraphNodeRenderer<E> renderer;

	private final BiHashMap<E, JComponent> nodesToComponents;
	private final NodeMouseAdapter mouseAdapter;

	private E selectedNode;
	private Point mousePosition;

	/**
	 * Builds a new graph with the passed in start point and a builder.
	 * 
	 * @param start
	 *            The start point of the graph. Graphs must have a start point.
	 * @param builder
	 *            The {@link SEGraphNodeBuilder} for the graph.
	 */
	public SEGraph(E start, SEGraphNodeBuilder<E> builder) {
		this.selectedNode = start;
		this.builder = builder;

		this.model = new SEGraphModel<E>(start, builder);
		this.mousePosition = new Point();
		this.nodesToComponents = new BiHashMap<E, JComponent>();
		this.modelObserver = new ModelCleaner();
		this.mouseAdapter = new NodeMouseAdapter();

		this.model.addSEGraphObserver(this.modelObserver);

		this.setLayout(new SEGraphLayoutManager());

		this.setUI(new SEGraphUI());
		this.setOpaque(true);
		this.setBackground(ScriptEaseUI.UNSELECTED_COLOUR);
	}

	/**
	 * Sets the node renderer to the passed in renderer. This will redraw the
	 * graph.
	 * 
	 * @param renderer
	 */
	public void setNodeRenderer(SEGraphNodeRenderer<E> renderer) {
		this.renderer = renderer;

		this.repaint();
		this.revalidate();
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

		this.cleanup(node);
		this.validateSelectedNode();
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

		this.cleanup(node1);
		this.cleanup(node2);

		this.validateSelectedNode();

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
	 * Returns the current selected component.
	 * 
	 * @return
	 */
	public JComponent getSelectedComponent() {
		return this.nodesToComponents.getValue(this.selectedNode);
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
	 * Makes sure the selected node is still in the graph. Otherwise, it sets it
	 * to null.
	 */
	private void validateSelectedNode() {
		if (SEGraph.this.model.getStartNode() == SEGraph.this.getSelectedNode())
			return;

		if (SEGraph.this.model.getParents(SEGraph.this.getSelectedNode())
				.size() == 0)
			SEGraph.this.setSelectedNode(null);
	}

	/**
	 * Removes the node from the graph's memory. This should be called when we
	 * disconnect a node, as that alone will not delete its children from
	 * memory. They might still be referenced by the graph. <br>
	 * <br>
	 * Note that this does not mean the node is not referenced somewhere else.
	 * 
	 * @param node
	 */
	private void cleanup(E node) {
		if (node == null)
			return;

		for (E child : this.builder.getChildren(node)) {
			if (this.builder.getParents(child).isEmpty()) {
				final JComponent component;
				component = this.nodesToComponents.getValue(child);
				if (component != null)
					this.remove(component);
				this.nodesToComponents.removeKey(child);
			}
		}

		if (this.builder.getParents(node).isEmpty()) {
			final JComponent component;
			component = this.nodesToComponents.getValue(node);
			if (component != null)
				this.remove(component);
			this.nodesToComponents.removeKey(node);
		}
	}

	/**
	 * Returns all node components in the graph.
	 * 
	 * @return
	 */
	public Collection<JComponent> getNodeComponents() {
		return this.nodesToComponents.getValues();
	}

	/**
	 * Gets a component for the passed in node.
	 * 
	 * @param node
	 * @return
	 */
	private JComponent getComponentForNode(E node) {
		final JComponent component;

		final JComponent storedComponent = this.nodesToComponents
				.getValue(node);

		if (storedComponent != null) {
			component = storedComponent;
		} else if (this.renderer != null) {
			component = this.renderer.createComponentForNode(node);
		} else {
			component = new JLabel(node.toString());
		}
		return component;
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
					JComponent component = SEGraph.this
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
					final JComponent component = SEGraph.this
							.getComponentForNode(node);

					if (component.getMouseListeners().length <= 1) {
						component.addMouseListener(SEGraph.this.mouseAdapter);
						component
								.addMouseMotionListener(SEGraph.this.mouseAdapter);
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
					SEGraph.this.nodesToComponents.put(node, component);

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
				JComponent component = SEGraph.this.getComponentForNode(node);

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

			final Graphics2D g2 = (Graphics2D) g.create();

			if (SEGraph.this.draggedFromNode != null
					&& SEGraph.this.mousePosition != null) {
				final ToolBarMode mode = GraphToolBarModeAction.getMode();

				// TODO These colours should be their own colours, linked to the
				// colours used in SEGraphNodeRenderer.
				if (mode == ToolBarMode.INSERT || mode == ToolBarMode.CONNECT)
					g2.setColor(ScriptEaseUI.COLOUR_KNOWN_OBJECT);
				else if (mode == ToolBarMode.DISCONNECT)
					g2.setColor(ScriptEaseUI.COLOUR_UNBOUND);

				g2.setStroke(new BasicStroke(1.5f));
				GUIOp.paintArrow(g2, GUIOp.getMidRight(SEGraph.this
						.getComponentForNode(SEGraph.this.draggedFromNode)),
						SEGraph.this.mousePosition);

			}

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
						GUIOp.paintArrow(g2, GUIOp.getMidRight(SEGraph.this
								.getComponentForNode(parent)), GUIOp
								.getMidLeft(SEGraph.this
										.getComponentForNode(child)));
					}
				}
			}
			// clean up
			g.dispose();
		}
	}

	private E draggedFromNode = null;

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
		private E lastEnteredNode = null;

		@Override
		public void mouseDragged(MouseEvent e) {
			SEGraph.this.mousePosition.setLocation(SEGraph.this
					.getMousePosition());
			SEGraph.this.repaint();
		}

		@Override
		public void mousePressed(MouseEvent e) {
			switch (GraphToolBarModeAction.getMode()) {
			case INSERT:
			case CONNECT:
			case DISCONNECT:
				SEGraph.this.draggedFromNode = SEGraph.this.nodesToComponents
						.getKey((JComponent) e.getSource());
				break;
			default:
				SEGraph.this.draggedFromNode = null;
				break;
			}
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			final JComponent entered;

			entered = (JComponent) e.getSource();

			this.lastEnteredNode = SEGraph.this.nodesToComponents
					.getKey(entered);

			if (this.lastEnteredNode == SEGraph.this.getStartNode())
				if (GraphToolBarModeAction.getMode() == ToolBarMode.DELETE) {
					// TODO This cursor should be an "Unavailable" cursor
					// instead of the default.
					entered.setCursor(Cursor
							.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				} else
					entered.setCursor(null);
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			final JComponent source;
			final E node;

			source = (JComponent) e.getSource();
			node = SEGraph.this.nodesToComponents.getKey((JComponent) e
					.getSource());

			ComponentFocusManager.getInstance().setFocus(SEGraph.this);

			switch (GraphToolBarModeAction.getMode()) {
			case SELECT:
			case DELETE:
				final Point mouseLoc = MouseInfo.getPointerInfo().getLocation();

				/*
				 * Only respond to releases that happen over this component. The
				 * default is to respond to releases if the press occurred in
				 * this component. This seems to be a Java bug, but I can't find
				 * any kind of complaint for it. Either way, we want this
				 * behaviour, not the default. - remiller
				 */
				if (!source.contains(mouseLoc.x
						- source.getLocationOnScreen().x,
						mouseLoc.y - source.getLocationOnScreen().y))
					return;
			}

			switch (GraphToolBarModeAction.getMode()) {
			case SELECT:
				SEGraph.this.setSelectedNode(node);
				source.requestFocusInWindow();
				break;
			case INSERT:
				E newNode = SEGraph.this.builder.buildNewNode();

				if (this.lastEnteredNode != null)
					if (this.lastEnteredNode == node)
						SEGraph.this.addNodeTo(newNode, node);
					else
						SEGraph.this.addNodeBetween(newNode,
								this.lastEnteredNode, node);
				break;
			case DELETE:
				SEGraph.this.removeNode(node);
				break;
			case CONNECT:
				if (this.lastEnteredNode != null
						&& this.lastEnteredNode != node)
					SEGraph.this.connectNodes(this.lastEnteredNode, node);

				break;
			case DISCONNECT:
				if (this.lastEnteredNode != null
						&& this.lastEnteredNode != node)
					SEGraph.this.disconnectNodes(this.lastEnteredNode, node);
				break;
			}

			SEGraph.this.draggedFromNode = null;
		}
	}

	/**
	 * Cleans up the model when children and parents are removed.
	 * 
	 * @author kschenk
	 * 
	 */
	private class ModelCleaner implements SEGraphObserver<E> {

		@Override
		public void nodeSelected(E node) {
		}

		@Override
		public void childAdded(E child, E parent) {
		}

		@Override
		public void childRemoved(E child, E parent) {
			SEGraph.this.cleanup(parent);
			SEGraph.this.cleanup(child);
		}

		@Override
		public void parentAdded(E child, E parent) {
		}

		@Override
		public void parentRemoved(E child, E parent) {
			SEGraph.this.cleanup(parent);
			SEGraph.this.cleanup(child);
		}
	}
}
