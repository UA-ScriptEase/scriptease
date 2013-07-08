package scriptease.gui.SEGraph;

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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.plaf.ComponentUI;

import scriptease.controller.observer.SEFocusObserver;
import scriptease.controller.observer.SEGraphToolBarObserver;
import scriptease.controller.undo.UndoManager;
import scriptease.gui.SEFocusManager;
import scriptease.gui.SEGraph.SEGraphToolBar.Mode;
import scriptease.gui.SEGraph.models.SEGraphModel;
import scriptease.gui.SEGraph.observers.SEGraphObserver;
import scriptease.gui.SEGraph.renderers.SEGraphNodeRenderer;
import scriptease.gui.ui.ScriptEaseUI;
import scriptease.util.BiHashMap;
import scriptease.util.GUIOp;
import sun.awt.util.IdentityArrayList;

/**
 * Builds a directed, acyclic graph that must have a start node. Each graph must
 * be created with a model. Depending on the mode chosen, the graph can be set
 * to only select individual nodes, or entire paths by holding shift. Graphs
 * should be created using the {@link SEGraphFactory}.
 * 
 * @author mfchurch
 * @author kschenk
 * 
 * @param <E>
 * 
 */
@SuppressWarnings("serial")
public class SEGraph<E> extends JComponent {
	/**
	 * The selection mode for the graph. We can either select nodes or paths. If
	 * node selection is enabled, we fire
	 * {@link SEGraphObserver#nodeSelected(Object)} events. If multiple node
	 * selection is enabled, we fire
	 * {@link SEGraphObserver#nodesSelected(Collection)} events.
	 * 
	 * @author kschenk
	 * 
	 */
	protected static enum SelectionMode {
		SELECT_NODE, SELECT_PATH, SELECT_PATH_FROM_START
	}

	private final SEGraphModel<E> model;

	private SEGraphNodeRenderer<E> renderer;

	private final SEGraphNodeTransferHandler<E> transferHandler;

	private final BiHashMap<E, JComponent> nodesToComponents;
	private final NodeMouseAdapter mouseAdapter;
	private final List<SEGraphObserver<E>> observers;
	private final LinkedHashSet<E> selectedNodes;

	private Point mousePosition;

	private SelectionMode selectionMode;
	private boolean isReadOnly;

	private final SEGraphToolBar toolBar;

	/**
	 * Builds a new graph with the passed in model and single node selection
	 * enabled.
	 * 
	 * @param model
	 *            The model used for the Graph.
	 */
	protected SEGraph(SEGraphModel<E> model) {
		this(model, SelectionMode.SELECT_NODE, false);
	}

	/**
	 * Builds a new graph with the passed in model and passed in selection mode.
	 * 
	 * @param model
	 *            The model used for the Graph.
	 * 
	 * @param selectionMode
	 *            The selection mode for the graph.
	 * 
	 * @param isReadOnly
	 *            If the graph is read only, only selection will be allowed.
	 */
	protected SEGraph(SEGraphModel<E> model, SelectionMode selectionMode,
			boolean isReadOnly) {
		this.selectionMode = selectionMode;
		this.model = model;
		this.selectionMode = selectionMode;
		this.isReadOnly = isReadOnly;

		this.toolBar = new SEGraphToolBar();
		this.selectedNodes = new LinkedHashSet<E>();
		this.mousePosition = new Point();
		this.nodesToComponents = new BiHashMap<E, JComponent>();
		this.mouseAdapter = new NodeMouseAdapter();
		this.observers = new ArrayList<SEGraphObserver<E>>();

		this.transferHandler = new SEGraphNodeTransferHandler<E>(this);

		this.setLayout(this.new SEGraphLayoutManager());

		this.selectedNodes.add(model.getStartNode());

		this.setUI(this.new SEGraphUI());
		this.setOpaque(true);
		this.setBackground(Color.white);

		SEFocusManager.getInstance().addSEFocusObserver(this,
				new SEFocusObserver() {
					@Override
					public void gainFocus(Component oldFocus) {
					}

					@Override
					public void loseFocus(Component oldFocus) {
						if (oldFocus instanceof SEGraph) {
							for (JComponent selected : SEGraph.this
									.getSelectedComponents()) {
								if (selected == null)
									continue;

								Color background = selected.getBackground();

								selected.setBackground(GUIOp.scaleWhite(
										background, 1.2));

							}
						}
					}
				});

		this.toolBar.addObserver(new SEGraphToolBarObserver() {
			@Override
			public void modeChanged(Mode mode) {
				SEGraph.this.setCursor(mode.getCursor());
			}
		});
	}

	/**
	 * Sets the node renderer to the passed in renderer. This will redraw the
	 * graph.
	 * 
	 * @param renderer
	 */
	protected void setNodeRenderer(SEGraphNodeRenderer<E> renderer) {
		this.renderer = renderer;

		this.repaint();
		this.revalidate();
	}

	/**
	 * Set the selection mode to path or node.
	 * 
	 * @param mode
	 *            Either SELECT_PATH or SELECT_NODE from {@link SelectionMode}.
	 */
	public void setSelectionMode(SelectionMode mode) {
		this.selectionMode = mode;
	}

	/**
	 * Adds a node onto an existing node. Fires a
	 * {@link SEGraphObserver#nodeAdded(Object, Collection, Collection)} event
	 * if the addition was successful.
	 * 
	 * @param node
	 *            The node to add
	 * @param parent
	 *            The existing node
	 * @return true if the addition was successful
	 * @see SEGraphModel#addNode(Object, Object)
	 */
	private boolean addNewNodeTo(E parent) {
		final E newNode;

		newNode = this.model.createNewNode();

		if (this.model.addChild(newNode, parent)) {
			final Collection<E> parents;
			final Collection<E> children;

			parents = this.model.getParents(newNode);
			children = this.model.getChildren(newNode);

			for (SEGraphObserver<E> observer : this.observers) {
				observer.nodeAdded(newNode, children, parents);
			}

			this.repaint();
			this.revalidate();

			return true;
		}

		return false;
	}

	/**
	 * Adds a node between two existing nodes. Order of the two nodes does not
	 * matter; this method figures out which node is above the other. Fires a
	 * {@link SEGraphObserver#nodeAdded(Object, Collection, Collection)} event
	 * if the addition was successful.
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
	private boolean addNewNodeBetween(E existingNode1, E existingNode2) {
		final E newNode;

		newNode = this.model.createNewNode();

		if (this.model.addNodeBetween(newNode, existingNode1, existingNode2)) {
			final Collection<E> parents;
			final Collection<E> children;

			parents = this.model.getParents(newNode);
			children = this.model.getChildren(newNode);

			for (SEGraphObserver<E> observer : this.observers) {
				observer.nodeAdded(newNode, children, parents);
			}

			this.repaint();
			this.revalidate();

			return true;
		}
		return false;
	}

	/**
	 * Replaces an existing node with a new node. The new node is actually
	 * cloned without any of its parents or successors, only its data.
	 * 
	 * @param existingNode
	 *            The node that already existed.
	 * @param newNode
	 *            The new node
	 * @return True if the replacement was successful
	 */
	protected boolean replaceNode(E existingNode, E newNode) {
		if (this.model.overwriteNodeData(existingNode, newNode)) {
			for (SEGraphObserver<E> observer : this.observers) {
				observer.nodeOverwritten(existingNode);
			}

			this.repaint();
			this.revalidate();

			return true;
		}
		return false;
	}

	/**
	 * Removes the node from the graph. Fires a
	 * {@link SEGraphObserver#nodeRemoved(Object)} event if the removal was
	 * successful.
	 * 
	 * @param node
	 *            The node to be removed.
	 * @return true if the removal was successful
	 * @see SEGraphModel#removeNode(Object)
	 */
	public boolean removeNode(E node) {
		if (this.model.removeNode(node)) {
			for (SEGraphObserver<E> observer : this.observers)
				observer.nodeRemoved(node);

			nodesToComponents.removeKey(node);

			this.validateSelectedNode();
			this.repaint();
			this.revalidate();

			return true;
		}
		return false;
	}

	/**
	 * Connects two nodes together. Fires a
	 * {@link SEGraphObserver#nodesConnected(Object, Object)} event if the
	 * connection was successful.
	 * 
	 * @param child
	 * @param parent
	 * @return True if the nodes were successfully connected.
	 * @see SEGraphModel#connectNodes(Object, Object)
	 */
	private boolean connectNodes(E child, E parent) {
		if (this.model.connectNodes(child, parent)) {
			for (SEGraphObserver<E> observer : this.observers) {
				observer.nodesConnected(child, parent);
			}

			this.repaint();
			this.revalidate();

			return true;
		}
		return false;
	}

	/**
	 * Disconnects two nodes. If the node had no other connections, this will
	 * result in the node getting removed from the graph. Always fires a
	 * {@link SEGraphObserver#nodesDisconnected(Object, Object)} event if the
	 * disconnection was successful.
	 * 
	 * @param child
	 * @param parent
	 * @return True if the nodes were successfully disconnected.
	 * @see SEGraphModel#disconnectNodes(Object, Object)
	 */
	private boolean disconnectNodes(E child, E parent) {
		if (this.model.disconnectNodes(child, parent)) {
			for (SEGraphObserver<E> observer : this.observers) {
				observer.nodesDisconnected(child, parent);
			}

			this.validateSelectedNode();

			this.repaint();
			this.revalidate();

			return true;
		}
		return false;
	}

	/**
	 * Sets the current selected nodes. Fires a
	 * {@link SEGraphObserver#nodesSelected(Collection)} event if the selection
	 * was successful.
	 * 
	 * @param nodes
	 *            The new collection of nodes to select.
	 */
	public boolean setSelectedNodes(Collection<E> nodes) {
		if (!this.selectedNodes.equals(nodes)) {
			this.selectedNodes.removeAll(this.selectedNodes);
			this.selectedNodes.addAll(nodes);

			for (SEGraphObserver<E> observer : this.observers) {
				observer.nodesSelected(this.selectedNodes);
			}

			this.renderer.resetAppearances();

			for (E node : this.selectedNodes) {
				this.renderer.reconfigureAppearance(this
						.getNodesToComponentsMap().getValue(node), node);
			}

			return true;
		}
		return false;
	}

	/**
	 * Selects the nodes from the start selected node up until the node. This
	 * method identifies if a previous path existed that may be contained in the
	 * new path. If so, it adds a new path onto the existing path.
	 * 
	 * @param node
	 * @return
	 */
	private boolean selectNodesUntil(E node) {
		final E start;
		final E end;

		// Check if Start Node selected.
		if (node == this.getStartNode()) {
			final Collection<E> startList;

			startList = new ArrayList<E>();
			startList.add(this.getStartNode());

			return this.setSelectedNodes(startList);
		}

		start = this.getFirstSelectedNode();

		// Immediately return false if there is no possible path.
		if (!this.model.getDescendants(start).contains(node))
			return false;

		end = this.getLastSelectedNode();

		if (start == end)
			// If the start node is the only node previously selected
			return this.setSelectedNodes(this.model.getPathBetweenNodes(start,
					node));

		final Collection<E> newSubPath;

		newSubPath = this.model.getPathBetweenNodes(end, node);

		if (!newSubPath.isEmpty()) {
			final Collection<E> path;

			path = new LinkedHashSet<E>(this.selectedNodes);

			path.addAll(newSubPath);
			return this.setSelectedNodes(path);
		}

		return this.setSelectedNodes(this.model
				.getPathBetweenNodes(start, node));
	}

	/**
	 * Adds an observer to the Graph to observe changes to the children and
	 * parents of nodes. Note that these observers are strongly referenced. One
	 * does not simply get garbage collected when the caller is removed. They
	 * must be removed manually with
	 * {@link #removeSEGraphObserver(SEGraphObserver)}.
	 * 
	 * @param observer
	 *            The observer to be added
	 * @see SEGraphObserver
	 */
	public void addSEGraphObserver(SEGraphObserver<E> observer) {
		this.observers.add(observer);
	}

	/**
	 * Removes an observer from the Graph.
	 * 
	 * @param observer
	 *            The observer to be removed
	 * @see SEGraphObserver
	 */
	public void removeSEGraphObserver(SEGraphObserver<E> observer) {
		this.observers.remove(observer);
	}

	/**
	 * Returns a collection of all SEGraphObservers attached to the graph.
	 * 
	 * @return
	 */
	public Collection<SEGraphObserver<E>> getSEGraphObservers() {
		return this.observers;
	}

	/**
	 * Returns a list of the current selected nodes. For single node selected
	 * graphs, it is recommended to use {@link #getFirstSelectedNode()} for
	 * convenience.
	 * 
	 * @return
	 */
	public LinkedHashSet<E> getSelectedNodes() {
		return this.selectedNodes;
	}

	/**
	 * Returns the first node in the selected path.
	 * 
	 * @return
	 */
	public E getFirstSelectedNode() {
		for (E node : this.selectedNodes)
			return node;

		return null;
	}

	/**
	 * Returns the last node in the selected path.
	 * 
	 * @return
	 */
	public E getLastSelectedNode() {
		E lastNode = this.getFirstSelectedNode();

		for (E node : this.selectedNodes) {
			lastNode = node;
		}

		return lastNode;
	}

	/**
	 * Returns the current selected components.
	 * 
	 * @return A collection of selected components
	 */
	public Collection<JComponent> getSelectedComponents() {
		final Collection<JComponent> selectedComponents;

		selectedComponents = new ArrayList<JComponent>();

		for (E node : this.selectedNodes) {
			selectedComponents.add(this.nodesToComponents.getValue(node));
		}
		return selectedComponents;
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
	 * Returns the children of the node.
	 * 
	 * @param node
	 * @return
	 */
	public Collection<E> getChildren(E node) {
		return this.model.getChildren(node);
	}

	/**
	 * Returns the parents of the node.
	 * 
	 * @param node
	 * @return
	 */
	public Collection<E> getParents(E node) {
		return this.model.getParents(node);
	}

	/**
	 * Returns true if the graph is read only.
	 * 
	 * @return
	 */
	public boolean isReadOnly() {
		return this.isReadOnly;
	}

	/**
	 * Makes sure the selected node is still in the graph. Otherwise, it sets it
	 * to null.
	 */
	private void validateSelectedNode() {
		for (E node : this.selectedNodes) {
			if (node == this.getStartNode())
				continue;

			if (this.model.getParents(node).size() == 0) {
				// If any node doesn't have parents, it's no longer in the
				// graph. So we set the selected nodes to just the start node.

				final List<E> startNodeList;

				startNodeList = new ArrayList<E>();

				startNodeList.add(this.getStartNode());

				this.setSelectedNodes(startNodeList);
				return;
			}
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

	public BiHashMap<E, JComponent> getNodesToComponentsMap() {
		return this.nodesToComponents;
	}

	/**
	 * Returns the {@link Mode} of the {@link SEGraphToolBar}.
	 * 
	 * @return
	 */
	public Mode getToolBarMode() {
		return this.toolBar.getMode();
	}

	/**
	 * Returns the {@link SEGraphToolBar} linked to the graph. If you are
	 * getting the mode, you should use {@link #getToolBarMode()} since it will
	 * return the default value of {@link Mode#SELECT} if the toolbar equals
	 * null.
	 * 
	 * @return
	 */
	public SEGraphToolBar getToolBar() {
		return this.toolBar;
	}

	/**
	 * Returns a component for the passed in node. This method creates a
	 * component for the node if none is found.
	 * 
	 * @param node
	 * @return
	 */
	private JComponent createComponentForNode(E node) {
		final JComponent component;
		final JComponent storedComponent;

		storedComponent = this.nodesToComponents.getValue(node);

		if (storedComponent != null) {
			component = storedComponent;
		} else if (this.renderer != null) {
			component = this.renderer.createComponentForNode(node);
			component.setTransferHandler(this.transferHandler);
		} else {
			component = new JLabel(node.toString());
		}
		return component;
	}

	/**
	 * Creates a group of nodes in the graph from @param startingNode to @param
	 * endingNode.
	 * 
	 * @param startingNode
	 *            The node at the start of the group
	 * @param endingNode
	 *            The ending node of the group
	 */
	private void createNodeGroupAt(E startingNode, E endingNode) {
		final Collection<E> nodeGroup;

		nodeGroup = this.model.getPathBetweenNodes(startingNode, endingNode);

		for (SEGraphObserver<E> observer : this.observers) {
			observer.nodesGrouped(nodeGroup);
		}

		this.repaint();
		this.revalidate();
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
		private static final int VERTICAL_INDENT = 15;

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
			final Insets insets;
			final Map<E, Integer> nodeMap;
			final int numberOfLevels;

			insets = parent.getInsets();
			nodeMap = SEGraph.this.model.getDepthMap(SEGraph.this
					.getStartNode());
			numberOfLevels = Collections.max(nodeMap.values());

			int xSize = insets.left + insets.right;
			int ySize = insets.top + insets.bottom;

			for (int currentLevel = 0; currentLevel <= numberOfLevels; currentLevel++) {
				final List<E> currentNodes = new ArrayList<E>();
				// extract nodes for the level
				for (Entry<E, Integer> entry : nodeMap.entrySet()) {
					if (entry.getValue() == currentLevel)
						currentNodes.add(entry.getKey());
				}

				// Get the width of the widest JComponent in the level.
				xSize += this.getMaxWidth(currentNodes) + HORIZONTAL_INDENT;

				int yNodeSize = VERTICAL_INDENT;
				for (E node : currentNodes) {
					JComponent component = SEGraph.this
							.createComponentForNode(node);
					yNodeSize += VERTICAL_INDENT
							+ component.getPreferredSize().height;
				}
				ySize = Math.max(ySize, yNodeSize);
			}
			return new Dimension(xSize, ySize);

		}

		@Override
		public void layoutContainer(Container parent) {
			// Remove the current contents of the panel.
			SEGraph.this.removeAll();

			final Map<E, Integer> nodeMap;
			final int numberOfLevels;
			final double graphHeight;

			nodeMap = SEGraph.this.model.getDepthMap(SEGraph.this
					.getStartNode());
			numberOfLevels = Collections.max(nodeMap.values());
			graphHeight = SEGraph.this.getPreferredSize().getHeight();

			int xLocation = HORIZONTAL_INDENT;
			List<E> previousNodes = null;
			// For each level in the graph,
			for (int currentLevel = 0; currentLevel <= numberOfLevels; currentLevel++) {
				final List<E> currentNodes;

				final int maxWidth;
				final int numberOfNodes;
				final double pixelsPerNode;

				currentNodes = this.sortChildrenByParent(previousNodes,
						this.getNodesForLevel(nodeMap, currentLevel));
				// Width of the widest JComponent in the level.
				maxWidth = this.getMaxWidth(currentNodes);
				numberOfNodes = currentNodes.size();
				// Number of y pixels each node is allocated
				pixelsPerNode = graphHeight / numberOfNodes;

				// For each node at that level,
				for (int currentNode = 0; currentNode < numberOfNodes; currentNode++) {
					final E node;
					final JComponent component;
					final Dimension componentSize;
					final int nodeWidth;
					final int yNodeLocation;

					node = currentNodes.get(currentNode);
					component = SEGraph.this.createComponentForNode(node);
					// JComponent preferred width
					componentSize = component.getPreferredSize();
					nodeWidth = componentSize.width;
					// The y Location for the node.
					yNodeLocation = (int) (pixelsPerNode * (currentNode + 1)
							- 0.5 * pixelsPerNode - 0.5 * componentSize.height);

					if (component.getMouseListeners().length <= 1) {
						component.addMouseListener(SEGraph.this.mouseAdapter);
						component
								.addMouseMotionListener(SEGraph.this.mouseAdapter);
					}

					// Add the component to the panel, so it will draw
					SEGraph.this.add(component);
					SEGraph.this.nodesToComponents.put(node, component);

					// Center smaller nodes according to the biggest
					int xOffset = 0;
					if (nodeWidth != maxWidth) {
						xOffset = (maxWidth - nodeWidth) / 2;
					}

					// Set the size and location of the component
					component.setLocation(xOffset + xLocation, yNodeLocation);
					component.setSize(new Dimension(nodeWidth,
							componentSize.height));
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
				final JComponent component = SEGraph.this
						.createComponentForNode(node);

				// Get the size of the JComponent.
				final Dimension componentSize = component.getPreferredSize();

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
				final Mode mode = SEGraph.this.getToolBarMode();

				if (mode == Mode.INSERT || mode == Mode.CONNECT)
					g2.setColor(GUIOp.scaleColour(
							ScriptEaseUI.COLOUR_INSERT_NODE, 0.8));
				else if (mode == Mode.DISCONNECT)
					g2.setColor(ScriptEaseUI.COLOUR_DELETE_NODE);

				g2.setStroke(new BasicStroke(1.5f));

				final List<Point> points;

				points = new ArrayList<Point>();

				points.add(GUIOp.getMidRight(SEGraph.this
						.createComponentForNode(SEGraph.this.draggedFromNode)));

				points.add(SEGraph.this.mousePosition);
				GUIOp.paintArrow(g2, points);

			}
			connectNodes(g);
		}

		private void connectNodes(Graphics g) {
			// Get the nodes level map
			Map<E, Integer> nodeMap = SEGraph.this.model
					.getDepthMap(SEGraph.this.getStartNode());

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

						final List<Point> points;
						final Point start;
						final Point end;
						points = new ArrayList<Point>();
						start = GUIOp.getMidRight(SEGraph.this
								.createComponentForNode(parent));
						end = GUIOp.getMidLeft(SEGraph.this
								.createComponentForNode(child));

						points.add(start);

						// Add points between

						points.add(end);

						// Draw an arrow pointing towards the child.
						GUIOp.paintArrow(g2, points);
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

			Container parent = SEGraph.this.getParent();
			while (parent != null) {
				if (parent instanceof JScrollPane) {
					GUIOp.scrollJScrollPaneToMousePosition((JScrollPane) parent);
					break;
				}
				parent = parent.getParent();
			}

			final Point mousePosition;

			mousePosition = SEGraph.this.getMousePosition();

			if (mousePosition != null)
				SEGraph.this.mousePosition.setLocation(mousePosition);

			SEGraph.this.repaint();
		}

		@Override
		public void mousePressed(MouseEvent e) {
			final Mode mode = SEGraph.this.getToolBarMode();

			final E draggedFrom;

			if (mode == Mode.INSERT || mode == Mode.CONNECT
					|| mode == Mode.DISCONNECT) {
				draggedFrom = SEGraph.this.nodesToComponents
						.getKey((JComponent) e.getSource());
			} else
				draggedFrom = null;

			SEGraph.this.draggedFromNode = draggedFrom;
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			final JComponent entered = (JComponent) e.getSource();
			final Mode mode = SEGraph.this.getToolBarMode();

			this.lastEnteredNode = SEGraph.this.nodesToComponents
					.getKey(entered);

			if (this.lastEnteredNode == SEGraph.this.getStartNode())
				if (mode == Mode.DELETE) {
					// Make the cursor appear unavailable for start node
					// deletion
					entered.setCursor(ScriptEaseUI.CURSOR_UNAVAILABLE);
				} else
					entered.setCursor(null);
			else if (SEGraph.this.isReadOnly)
				if (mode != Mode.SELECT) {
					entered.setCursor(ScriptEaseUI.CURSOR_UNAVAILABLE);
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

			final Mode mode = SEGraph.this.getToolBarMode();

			if (mode == Mode.SELECT || mode == Mode.DELETE) {
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

			if (mode == Mode.SELECT) {
				if (selectionMode == SelectionMode.SELECT_PATH_FROM_START
						|| (selectionMode == SelectionMode.SELECT_PATH && e
								.isShiftDown())) {
					SEGraph.this.selectNodesUntil(node);

					for (E selectedNode : SEGraph.this.getSelectedNodes()) {
						SEGraph.this.renderer.reconfigureAppearance(
								SEGraph.this.nodesToComponents
										.getValue(selectedNode), selectedNode);
					}
				} else {
					// Default behaviour is to select individual nodes
					final Collection<E> nodes;

					nodes = new ArrayList<E>();

					nodes.add(node);

					SEGraph.this.setSelectedNodes(nodes);
				}
				source.requestFocusInWindow();
			} else if (mode == Mode.INSERT) {
				if (this.lastEnteredNode != null)
					if (this.lastEnteredNode == node) {
						if (!UndoManager.getInstance().hasOpenUndoableAction())
							UndoManager.getInstance().startUndoableAction(
									"Add new node to " + node);
						SEGraph.this.addNewNodeTo(node);

						UndoManager.getInstance().endUndoableAction();
					} else {
						if (!UndoManager.getInstance().hasOpenUndoableAction())
							UndoManager.getInstance().startUndoableAction(
									"Add " + node + " between "
											+ this.lastEnteredNode + " and "
											+ node);

						SEGraph.this.addNewNodeBetween(this.lastEnteredNode,
								node);

						UndoManager.getInstance().endUndoableAction();
					}

				SEGraph.this.getNodesToComponentsMap()
						.getValue(this.lastEnteredNode).requestFocusInWindow();
			} else if (mode == Mode.DELETE) {
				if (!UndoManager.getInstance().hasOpenUndoableAction())
					UndoManager.getInstance().startUndoableAction(
							"Remove " + node);

				SEGraph.this.removeNode(node);

				UndoManager.getInstance().endUndoableAction();

			} else if (mode == Mode.CONNECT) {
				if (this.lastEnteredNode != null
						&& this.lastEnteredNode != node) {
					if (!UndoManager.getInstance().hasOpenUndoableAction())
						UndoManager.getInstance().startUndoableAction(
								"Connect " + this.lastEnteredNode + " to "
										+ node);

					SEGraph.this.connectNodes(this.lastEnteredNode, node);

					UndoManager.getInstance().endUndoableAction();
				}
			} else if (mode == Mode.DISCONNECT) {
				if (this.lastEnteredNode != null
						&& this.lastEnteredNode != node) {
					if (!UndoManager.getInstance().hasOpenUndoableAction())
						UndoManager.getInstance().startUndoableAction(
								"Disconnect " + this.lastEnteredNode + " from "
										+ node);

					SEGraph.this.disconnectNodes(this.lastEnteredNode, node);

					UndoManager.getInstance().endUndoableAction();
				}
			} else if (mode == Mode.GROUP) {
				if (this.lastEnteredNode != null
						&& this.lastEnteredNode != node) {
//					if (!UndoManager.getInstance().hasOpenUndoableAction())
//						UndoManager.getInstance().startUndoableAction(
//								"Creating group from "
//										+ node + " to "
//										+ this.secondLastEnteredNode);
//
//					SEGraph.this.createNodeGroupAt(node, this.secondLastEnteredNode);
//					
//					System.out.println("DEBUG: start node: " + node);
//					System.out.println("DEBUG: ending node: " + this.secondLastEnteredNode);
//
//					System.out.println("Debug: children node " + node.);
//					UndoManager.getInstance().endUndoableAction();
				}
			} else if (mode == Mode.UNGROUP) {
				if (!UndoManager.getInstance().hasOpenUndoableAction())
					UndoManager.getInstance().startUndoableAction(
							"Ungrouping at " + node);

				UndoManager.getInstance().endUndoableAction();
			}

			SEFocusManager.getInstance().setFocus(SEGraph.this);

			SEGraph.this.renderer.reconfigureAppearance(
					SEGraph.this.nodesToComponents.getValue(node), node);
			SEGraph.this.draggedFromNode = null;

			SEGraph.this.repaint();
		}
	}
}
