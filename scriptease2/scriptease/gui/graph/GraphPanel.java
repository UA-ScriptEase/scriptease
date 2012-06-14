package scriptease.gui.graph;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
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
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.plaf.PanelUI;

import scriptease.controller.GraphNodeVisitor;
import scriptease.controller.observer.GraphNodeEvent;
import scriptease.controller.observer.GraphNodeEvent.GraphNodeEventType;
import scriptease.controller.observer.GraphNodeObserver;
import scriptease.gui.SETree.cell.ScriptWidgetFactory;
import scriptease.gui.SETree.cell.TypeWidget;
import scriptease.gui.SETree.ui.ScriptEaseUI;
import scriptease.gui.action.ToolBarButtonAction;
import scriptease.gui.action.ToolBarButtonAction.ToolBarButtonMode;
import scriptease.gui.graph.nodes.GraphNode;
import scriptease.gui.graph.nodes.KnowItNode;
import scriptease.gui.graph.nodes.TextNode;
import scriptease.gui.quests.QuestNode;
import scriptease.gui.quests.QuestPointNode;
import scriptease.gui.storycomponentpanel.StoryComponentPanel;
import scriptease.model.atomic.KnowIt;
import scriptease.util.GUIOp;
import sun.awt.util.IdentityArrayList;

/**
 * GraphPanel is a JPanel used for drawing graphs from GraphNodes. It contains a
 * headNode which is the first node in the graph, and from that node it draws
 * the graph. All Graph drawing logic should be dealt with in this class.
 * 
 * @author mfchurch
 * @author graves (refactored)
 * @author kschenk
 */
@SuppressWarnings("serial")
public class GraphPanel extends JPanel implements GraphNodeObserver {
	private static final int HORIZONTAL_INDENT = 20;
	private static final int NODE_Y_INDENT = 10;
	private GraphNode headNode;
	private GraphNodeComponentBuilder builder;
	private Map<GraphNode, Integer> nodeDepthMap;
	private Point mousePosition = new Point();

	private GraphNode oldSelectedNode;

	public GraphPanel(GraphNode headNode) {
		this.setHeadNode(headNode);

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

		this.addMouseListener(connectArrowListener);
		this.addMouseMotionListener(connectArrowListener);

		// Initialize the builder.
		this.builder = new GraphNodeComponentBuilder();

		// layout manager
		this.setLayout(new GraphPanelLayoutManager());

		// ui manager
		this.setUI(new GraphPanelUI());

		this.setOpaque(true);
		this.setBackground(StoryComponentPanel.UNSELECTED_COLOUR);
	}

	/**
	 * Store the node depth map until a change has occurred, so we don't have to
	 * recalculate it unnecessarily
	 * 
	 * @return
	 */
	private Map<GraphNode, Integer> getNodeDepthMap() {
		if (this.nodeDepthMap == null && headNode != null)
			this.nodeDepthMap = headNode.getNodeDepthMap();
		return this.nodeDepthMap;
	}

	@Override
	public void nodeChanged(GraphNodeEvent event) {
		final GraphNode sourceNode = event.getSource();
		final GraphNodeEventType eventType = event.getEventType();

		if (eventType == GraphNodeEventType.CONNECTION_ADDED) {
			// observer the graph nodes
			GraphNode.observeDepthMap(this, sourceNode);
		}
		if (eventType == GraphNodeEventType.CONNECTION_ADDED
				|| eventType == GraphNodeEventType.CONNECTION_REMOVED) {
			this.nodeDepthMap = null;
		}

		this.repaint();
		this.revalidate();
	}

	/**
	 * Sets the head node for the graph panel.
	 * 
	 * @param graphNode
	 */
	public void setHeadNode(GraphNode graphNode) {
		if (graphNode == null)
			throw new IllegalArgumentException("Cannot set head node to null");
		this.headNode = graphNode;

		GraphNode.observeDepthMap(this, this.headNode);

		this.getHeadNode().setSelected(true);
	}

	/**
	 * Returns the head node of the graph.
	 * 
	 * @return
	 */
	public GraphNode getHeadNode() {
		return this.headNode;
	}

	/**
	 * Returns the old selected node, which is used to draw paths between nodes.
	 * 
	 * @return
	 */
	public GraphNode getOldSelectedNode() {
		return this.oldSelectedNode;
	}

	/**
	 * Sets the old selected node, which is used to draw paths between nodes.
	 * 
	 * @param oldNode
	 */
	public void setOldSelectedNode(GraphNode oldNode) {
		this.oldSelectedNode = oldNode;
	}

	/**
	 * A class that creates and configures a JComponent to represent a GraphNode
	 * in the GraphEditor. Private to GraphPanel, since all drawing code should
	 * be in GraphPanel.
	 * 
	 * @author Graves
	 * @author mfchurch
	 */
	public class GraphNodeComponentBuilder implements GraphNodeVisitor {
		private Map<GraphNode, JComponent> componentMap = new IdentityHashMap<GraphNode, JComponent>();
		private JComponent component;

		@Override
		public void processTextNode(TextNode textNode) {
			component = new JLabel(textNode.getText());
			configureListeners(textNode, component);
			configureAppearance(textNode, component);
			component.setFont(new Font(component.getFont().getName(), textNode
					.getBoldStatus(), component.getFont().getSize()));
		}

		@Override
		public void processKnowItNode(KnowItNode knowItNode) {
			component = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 0));

			configureListeners(knowItNode, component);
			configureAppearance(knowItNode, component);

			KnowIt knowIt = knowItNode.getKnowIt();
			if (knowIt != null) {
				JPanel typePanel = new JPanel(new FlowLayout(FlowLayout.CENTER,
						0, 0));
				typePanel.setOpaque(false);
				for (String type : knowIt.getAcceptableTypes()) {
					TypeWidget typeWidget = ScriptWidgetFactory
							.buildTypeWidget(type);
					typeWidget.setSelected(true);
					typeWidget.setBackground(ScriptEaseUI.COLOUR_BOUND);
					typePanel.add(typeWidget);
				}
				component.add(typePanel);
				component.add(new JLabel(knowIt.getDisplayText()));
			}
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					validate();
				}
			});
		}

		@Override
		public void processQuestPointNode(QuestPointNode questPointNode) {
			component = new JPanel();
			configureListeners(questPointNode, component);
			configureAppearance(questPointNode, component);
			component.setLayout(new FlowLayout(FlowLayout.LEADING, 5, 5));
			component.add(ScriptWidgetFactory.buildFanInPanel(questPointNode
					.getQuestPoint().getFanIn()));
			component.add(new JLabel(questPointNode.getQuestPoint()
					.getDisplayText()));
		}

		@Override
		public void processQuestNode(QuestNode questNode) {

		}

		/**
		 * Sets up the listeners.
		 * 
		 * @param node
		 * @param component
		 */
		private void configureListeners(final GraphNode node,
				final JComponent component) {
			if (component != null) {
				/*
				 * When a component is clicked, forward the click to the
				 * GraphNode, and its observers.
				 */
				final MouseAdapter mouseAdapter = new MouseAdapter() {
					@Override
					public void mouseReleased(MouseEvent e) {
						final JComponent src = (JComponent) e.getSource();
						final Point mouseLoc = MouseInfo.getPointerInfo()
								.getLocation();

						/*
						 * Only respond to releases that happen over this
						 * component. The default is to respond to releases if
						 * the press occurred in this component. This seems to
						 * be a Java bug, but I can't find any kind of complaint
						 * for it. Either way, we want this behaviour, not the
						 * default. - remiller
						 */
						if (!src.contains(mouseLoc.x - src.getLocationOnScreen().x, mouseLoc.y
								- src.getLocationOnScreen().y))
							return;

						GraphNodeEvent event = new GraphNodeEvent(node,
								GraphNodeEventType.SELECTED);

						event.setShiftDown(e.isShiftDown());
						node.notifyObservers(event);
					}

					@Override
					public void mouseEntered(MouseEvent e) {

						JComponent nodeComponent = getComponentForNode(node);

						if (ToolBarButtonAction.getMode() == ToolBarButtonMode.DELETE_GRAPH_NODE) {
							if (node.isDeletable()) {
								nodeComponent.setBackground(GUIOp.scaleWhite(
										Color.RED, 2.4));
							}
						} else if (ToolBarButtonAction.getMode() == ToolBarButtonMode.DISCONNECT_GRAPH_NODE) {
							nodeComponent.setBackground(GUIOp.scaleWhite(
									Color.RED, 2.4));
						} else if (!node.isSelected()) {
							if (ToolBarButtonAction.getMode() != ToolBarButtonMode.SELECT_GRAPH_NODE) {
								Color selectionColour = ScriptEaseUI.SELECTED_GAME_OBJECT;
								nodeComponent.setBackground(GUIOp.scaleWhite(
										selectionColour, 3.1));
							} else {
								nodeComponent.setBackground(GUIOp.scaleWhite(
										Color.yellow, 1.2));
							}
						}

					}

					@Override
					public void mouseExited(MouseEvent e) {
						JComponent nodeComponent = getComponentForNode(node);

						if (!node.isSelected()) {
							nodeComponent.setBackground(Color.WHITE);
						} else {
							Color selectionColour = ScriptEaseUI.SELECTED_GAME_OBJECT;
							nodeComponent.setBackground(GUIOp.scaleWhite(
									selectionColour, 2.1));
						}
					}
				};

				component.addMouseListener(mouseAdapter);
				component.addMouseMotionListener(mouseAdapter);
			}
		}

		/**
		 * Creates and returns the appropriate JComponent for the given
		 * GraphNode.
		 * 
		 * @param node
		 * @return The appropriate JComponent to represent <code>node</code>.
		 */
		public JComponent getComponentForNode(GraphNode node) {
			// check if the node already has a component
			final JComponent storedComponent = this.componentMap.get(node);
			if (storedComponent != null) {
				this.component = storedComponent;
			} else {
				// otherwise build it and store it
				node.process(this);
				this.componentMap.put(node, this.component);
			}

			// return the component for the node
			return this.component;
		}
	}

	/**
	 * Method to configure the component's appearance. Based on code from
	 * mfchurch.
	 * 
	 * @param node
	 */
	public void configureAppearance(final GraphNode node,
			final JComponent component) {
		if (component != null) {
			final Color selectionColour;
			final Color backgroundColour;

			if (node.isSelected()) {
				selectionColour = ScriptEaseUI.SELECTED_GAME_OBJECT;
				backgroundColour = GUIOp.scaleWhite(selectionColour, 2.1);
			} else {
				selectionColour = Color.GRAY;
				backgroundColour = Color.white;
			}

			// Double lined border on terminal nodes
			Border lineBorder = BorderFactory.createBevelBorder(
					BevelBorder.RAISED, selectionColour,
					selectionColour.darker());
			if (node.isTerminalNode()) {
				Border inner = BorderFactory.createCompoundBorder(lineBorder,
						BorderFactory.createEmptyBorder(1, 1, 1, 1));
				Border outer = BorderFactory.createCompoundBorder(inner, inner);
				component.setBorder(outer);
			} else {
				component
						.setBorder(BorderFactory.createCompoundBorder(
								lineBorder,
								BorderFactory.createEmptyBorder(3, 3, 3, 3)));
			}
			component.setBackground(backgroundColour);
			component.setOpaque(true);
		}
	}

	/**
	 * The class that handles the actual laying out of GraphNodes. The logic is
	 * fairly basic, and should probably be updated to handle more cases.
	 * 
	 * @author graves
	 */
	private class GraphPanelLayoutManager implements LayoutManager {
		protected void clearDisplayPanel() {
			GraphPanel.this.removeAll();
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
			final Insets insets = parent.getInsets();
			return minimumGraphLayoutSize(insets.left + insets.right,
					insets.top + insets.bottom);
		}

		/**
		 * Calculates the minimum layout size when the panel is expanded (graph)
		 * 
		 * @param parent
		 * @return
		 */
		private Dimension minimumGraphLayoutSize(int xSize, int ySize) {
			// Get the nodes level map
			Map<GraphNode, Integer> nodeMap = getNodeDepthMap();

			// Get the number of levels in the graph.
			int numberOfLevels = Collections.max(nodeMap.values());

			// For each level in the graph,
			for (int currentLevel = 0; currentLevel <= numberOfLevels; currentLevel++) {

				final List<GraphNode> currentNodes = new ArrayList<GraphNode>();
				// extract nodes for the level
				for (Entry<GraphNode, Integer> entry : nodeMap.entrySet()) {
					if (entry.getValue() == currentLevel)
						currentNodes.add(entry.getKey());
				}

				// Get the width of the widest JComponent in the level.
				xSize += this.getMaxWidth(currentNodes) + HORIZONTAL_INDENT;

				int yNodeSize = NODE_Y_INDENT;
				for (GraphNode node : currentNodes) {
					JComponent component = builder.getComponentForNode(node);
					yNodeSize += NODE_Y_INDENT
							+ component.getPreferredSize().getHeight();
				}
				ySize = Math.max(ySize, yNodeSize);
			}
			return new Dimension(xSize, ySize);
		}

		/**
		 * Layout the graph panel in it's expanded form (graph)
		 * 
		 * @param parent
		 */
		private void layoutGraph(int xLocation, int yLocation) {
			// Remove the current contents of the panel.
			clearDisplayPanel();

			// Get the nodes level map
			Map<GraphNode, Integer> nodeMap = getNodeDepthMap();

			// Get the number of levels in the graph.
			int numberOfLevels = Collections.max(nodeMap.values());

			// Preferred size of the graph
			final Dimension preferredSize = GraphPanel.this.getPreferredSize();

			// For each level in the graph,
			List<GraphNode> currentNodes = null;
			List<GraphNode> previousNodes = null;
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
						/ ((double) numberOfNodes);

				// For each node at that level,
				for (int currentNode = 0; currentNode < numberOfNodes; currentNode++) {
					// Get a reference to the current node.
					final GraphNode node = currentNodes.get(currentNode);

					// Get the JComponent associated with the node.
					final JComponent component = builder
							.getComponentForNode(node);

					// update the component appearence to the state of the node
					configureAppearance(node, component);

					// Get the JComponent preferred width
					final int nodeWidth = (int) component.getPreferredSize()
							.getWidth();

					// Compute the y Location for the node.
					double yNodeLocation = yLocation
							+ (pixelsPerNode * (currentNode + 1) - 0.5
									* pixelsPerNode - 0.5 * component
									.getPreferredSize().getHeight());

					// Add the component to the panel, so it will draw
					GraphPanel.this.add(component);

					// Center smaller nodes according to the biggest
					int xOffset = 0;
					if (nodeWidth != maxWidth) {
						xOffset = (int) ((maxWidth - nodeWidth) / 2);
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
		private List<GraphNode> sortChildrenByParent(
				List<GraphNode> parentNodes, List<GraphNode> childNodes) {
			if (parentNodes == null || parentNodes.isEmpty()
					|| childNodes == null || childNodes.isEmpty())
				return childNodes;

			final List<GraphNode> sortedChildren = new IdentityArrayList<GraphNode>(
					childNodes.size());
			int newIndex = 0;
			for (GraphNode parentNode : parentNodes) {
				int parentIndex = newIndex;
				List<GraphNode> children = parentNode.getChildren();
				for (GraphNode childNode : childNodes) {
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

		private List<GraphNode> getNodesForLevel(
				Map<GraphNode, Integer> nodeMap, int level) {
			List<GraphNode> currentNodes = new ArrayList<GraphNode>();
			// extract nodes for the level
			for (Entry<GraphNode, Integer> entry : nodeMap.entrySet()) {
				if (entry.getValue() == level)
					currentNodes.add(entry.getKey());
			}
			return currentNodes;
		}

		@Override
		public void layoutContainer(Container parent) {
			// clear the display panel
			clearDisplayPanel();

			// layout the graph
			layoutGraph(HORIZONTAL_INDENT, 0);
		}

		/**
		 * Returns the width of the widest JComponent in the collection of
		 * nodes.
		 * 
		 * @param nodes
		 * @return
		 */
		private int getMaxWidth(Collection<GraphNode> nodes) {
			int maxWidth = 0;

			for (GraphNode node : nodes) {
				// Get the component for the node.
				JComponent component = builder.getComponentForNode(node);

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
	 * UI Class for managing drawing lines between nodes in the graph
	 * 
	 * @author mfchurch
	 * @author graves
	 * 
	 */
	public class GraphPanelUI extends PanelUI {
		protected boolean paintLines = true;
		protected GraphNodeComponentBuilder componentBuilder = builder;

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

			if (paintLines)
				connectNodes(g, headNode);
		}

		private void connectNodes(Graphics g, GraphNode headNode) {
			// Get the nodes level map
			Map<GraphNode, Integer> nodeMap = headNode.getNodeDepthMap();

			// Clone the graphics context.
			final Graphics2D g2 = (Graphics2D) g.create();

			// Get the number of levels in the graph.
			int numberOfLevels = Collections.max(nodeMap.values());

			// For each level in the graph
			for (int currentLevel = 0; currentLevel <= numberOfLevels; currentLevel++) {
				// boolean so only a single line will be coloured
				boolean noOtherSelection = true;

				final List<GraphNode> currentNodes = new ArrayList<GraphNode>();
				// extract nodes for the level
				for (Entry<GraphNode, Integer> entry : nodeMap.entrySet()) {
					if (entry.getValue() == currentLevel)
						currentNodes.add(entry.getKey());
				}

				// for each node in the level
				for (GraphNode parent : currentNodes) {
					// Get the children of the node.
					List<GraphNode> children = parent.getChildren();

					// For each child,
					for (GraphNode child : children) {
						// Is the line selected?
						boolean isLineSelected = child.isSelected()
								&& parent.isSelected() && noOtherSelection;
						if (isLineSelected) {
							// Only allow one line to be selected
							noOtherSelection = false;
						}

						// Set the line stroke
						BasicStroke stroke = getLineStroke(isLineSelected);
						g2.setStroke(stroke);

						// Set color of line based on selection
						Color lineColour;
						if (isLineSelected)
							lineColour = ScriptEaseUI.SELECTED_GAME_OBJECT;
						else
							lineColour = Color.GRAY;
						g2.setColor(lineColour);

						// Draw an arrow pointing towards the child.
						GUIOp.paintArrow(g2, GUIOp.getMidRight(componentBuilder
								.getComponentForNode(parent)), GUIOp
								.getMidLeft(componentBuilder
										.getComponentForNode(child)));
					}
				}
			}
			// clean up
			g.dispose();
		}

		private BasicStroke getLineStroke(boolean isLineSelected) {
			final BasicStroke stroke;
			// if selected, thicker line
			float lineThickness;
			if (isLineSelected) {
				lineThickness = 2.0f;
			} else
				lineThickness = 1.5f;
			stroke = new BasicStroke(lineThickness);
			return stroke;
		}
	}
}