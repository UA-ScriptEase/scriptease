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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JComponent;
import javax.swing.plaf.PanelUI;

import scriptease.gui.SETree.ui.ScriptEaseUI;
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
	private final SEGraphNodeRenderer<E> builder;
	private E selectedNode;

	// TODO Need to figure out what we can do with the SEGraphNodeRenderer..
	public SEGraph(E start, SEGraphNodeRenderer renderer) {
		this.model = new SEGraphModel<E>(start);
		this.builder = renderer;

		this.setLayout(new SEGraphLayoutManager());

		this.setOpaque(true);
		this.setBackground(ScriptEaseUI.UNSELECTED_COLOUR);

	}

	public void addNodeTo(E node, E existingNode) {
		this.model.addNodeTo(node, existingNode);
	}

	public void removeNode(E node) {
		this.model.removeNode(node);
	}

	public boolean connectNodes(E node1, E node2) {
		return this.model.connectNodes(node1, node2);
	}

	public boolean disconnectNodes(E node1, E node2) {
		return this.model.disconnectNodes(node1, node2);
	}

	public E getSelectedNode() {
		return this.selectedNode;
	}

	public Collection<E> getNodes() {
		return this.model.getNodes();
	}

	/**
	 * The class that handles the actual laying out of GraphNodes. The logic is
	 * fairly basic, and should probably be updated to handle more cases.
	 * 
	 * @author graves
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
			final Insets insets = parent.getInsets();
			int xSize = insets.left + insets.right;
			int ySize = insets.top + insets.bottom;
			// Get the nodes level map
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
					JComponent component = SEGraph.this.builder
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
					final JComponent component = SEGraph.this.builder
							.getComponentForNode(node);

					// update the component appearence to the state of the node
					SEGraph.this.builder.configureAppearance(component, node);

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
				JComponent component = SEGraph.this.builder
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
	 * UI Class for managing drawing lines between nodes in the graph
	 * 
	 * @author mfchurch
	 * @author graves
	 * 
	 */

	//@formatter:off
/*	public class GraphPanelUI extends PanelUI {
		protected boolean paintLines = true;

		@Override
		public void paint(Graphics g, JComponent c) {

			final Graphics2D g2 = (Graphics2D) g.create();

			if (SEGraph.this.oldSelectedNode != null
					&& SEGraph.this.mousePosition != null) {
				g2.setColor(Color.GRAY);
				g2.setStroke(new BasicStroke(1.5f));
				GUIOp.paintArrow(g2, GUIOp.getMidRight(SEGraph.this.builder
						.getComponentForNode(SEGraph.this.oldSelectedNode)),
						SEGraph.this.mousePosition);
			}

			super.paint(g, c);

			if (this.paintLines)
				connectNodes(g, SEGraph.this.headNode);
		}

		private void connectNodes(Graphics g, E headNode) {
			// Get the nodes level map
			Map<E, Integer> nodeMap = SEGraph.this.model.getDepthMap();

			// Clone the graphics context.
			final Graphics2D g2 = (Graphics2D) g.create();

			// Get the number of levels in the graph.
			int numberOfLevels = Collections.max(nodeMap.values());

			// For each level in the graph
			for (int currentLevel = 0; currentLevel <= numberOfLevels; currentLevel++) {
				// boolean so only a single line will be coloured
				boolean noOtherSelection = true;

				final List<E> currentNodes = new ArrayList<E>();
				// extract nodes for the level
				for (Entry<E, Integer> entry : nodeMap.entrySet()) {
					if (entry.getValue() == currentLevel)
						currentNodes.add(entry.getKey());
				}

				// for each node in the level
				for (E parent : currentNodes) {
					// For each child,
					for (E child : SEGraph.this.model.getChildren(parent)) {
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
							lineColour = GUIOp.scaleColour(
									ScriptEaseUI.SELECTED_GRAPH_NODE, 0.7);
						else
							lineColour = Color.GRAY;
						g2.setColor(lineColour);

						// Draw an arrow pointing towards the child.
						GUIOp.paintArrow(g2, GUIOp
								.getMidRight(SEGraph.this.builder
										.getComponentForNode(parent)), GUIOp
								.getMidLeft(SEGraph.this.builder
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
*/
}
