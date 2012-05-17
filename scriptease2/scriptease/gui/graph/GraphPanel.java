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
import scriptease.gui.graph.nodes.GraphNode;
import scriptease.gui.graph.nodes.KnowItNode;
import scriptease.gui.graph.nodes.TextNode;
import scriptease.gui.quests.QuestNode;
import scriptease.gui.quests.QuestNodePanel;
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
 */
@SuppressWarnings("serial")
public class GraphPanel extends JPanel implements GraphNodeObserver {
	private static final int HORIZONTAL_INDENT = 20;
	private static final int NODE_Y_INDENT = 10;
	private GraphNode headNode;
	private GraphNodeComponentBuilder builder;
	private Map<GraphNode, Integer> nodeDepthMap;

	public GraphPanel(GraphNode headNode) {
		this.headNode = headNode;

		// Initialize the builder.
		this.builder = new GraphNodeComponentBuilder();

		// layout manager
		this.setLayout(new GraphPanelLayoutManager());

		// ui manager
		this.setUI(new GraphPanelUI());

		// observe the graph nodes
		GraphNode.observeDepthMap(this, this.headNode);

		this.setOpaque(true);
		this.setBackground(StoryComponentPanel.UNSELECTED_COLOUR);
	}

	/**
	 * Store the node depth map until a change has occured, so we don't have to
	 * recalculate it unneccessarily
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
			component = new QuestNodePanel(questNode);
			configureAppearance(questNode, component);
		}

		/**
		 * A convenience method to re-use code.
		 * 
		 * @param node
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
					public void mouseClicked(MouseEvent e) {
						node.notifyObservers(new GraphNodeEvent(node,
								GraphNodeEventType.SELECTED));
					}
					
					//TODO Hover functionality goes here! Refactor this stuff tomorrow.
					@Override
					public void mouseEntered(MouseEvent e) {
						JComponent nodeComponent = getComponentForNode(node);
						nodeComponent.setBackground(Color.ORANGE);
					}
					
					@Override
					public void mouseExited(MouseEvent e) {
						JComponent nodeComponent = getComponentForNode(node);
						nodeComponent.setBackground(Color.WHITE);
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
				selectionColour = node.getSelectedColour();
				backgroundColour = GUIOp.scaleWhite(selectionColour, 2.1);
			} else {
				selectionColour = node.getUnselectedColour();
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
							lineColour = child.getSelectedColour();
						else
							lineColour = child.getUnselectedColour();
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
