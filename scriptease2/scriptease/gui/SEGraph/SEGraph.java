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
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.plaf.ComponentUI;

import scriptease.controller.observer.SEFocusObserver;
import scriptease.controller.observer.SEGraphToolBarObserver;
import scriptease.controller.undo.UndoManager;
import scriptease.gui.SEFocusManager;
import scriptease.gui.SEGraph.SEGraphToolBar.Mode;
import scriptease.gui.SEGraph.controllers.GraphGroupController;
import scriptease.gui.SEGraph.models.SEGraphModel;
import scriptease.gui.SEGraph.observers.SEGraphObserver;
import scriptease.gui.SEGraph.renderers.SEGraphNodeRenderer;
import scriptease.gui.ui.ScriptEaseUI;
import scriptease.model.complex.StoryGroup;
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
 * @author jyuen
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

	public final SEGraphModel<E> model;

	private SEGraphNodeRenderer<E> renderer;

	private final SEGraphNodeTransferHandler<E> transferHandler;

	public final BiHashMap<E, JComponent> nodesToComponents;
	private final NodeMouseAdapter mouseAdapter;

	private final List<SEGraphObserver<E>> observers;
	private final LinkedHashSet<E> selectedNodes;

	private Point mousePosition;

	private SelectionMode selectionMode;
	private boolean isReadOnly;

	private SEGraphToolBar toolBar;

	private GraphGroupController<E> groupController;

	/**
	 * Builds a new graph with the passed in model and single node selection
	 * enabled.
	 * 
	 * @param model
	 *            The model used for the Graph.
	 */
	protected SEGraph(SEGraphModel<E> model) {
		this(model, SelectionMode.SELECT_NODE, false, true, false);
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
			boolean isReadOnly, boolean disableGroupTool, boolean isShared) {
		this.selectionMode = selectionMode;
		this.model = model;
		this.selectionMode = selectionMode;
		this.isReadOnly = isReadOnly;

		if (isShared)
			this.toolBar = SharedSEGraphToolBar.getInstance();
		else 
			this.toolBar = new SEGraphToolBar(disableGroupTool);

		this.selectedNodes = new LinkedHashSet<E>();
		this.mousePosition = new Point();
		this.nodesToComponents = new BiHashMap<E, JComponent>();
		this.mouseAdapter = new NodeMouseAdapter();
		this.observers = new ArrayList<SEGraphObserver<E>>();

		this.groupController = new GraphGroupController<E>(this);

		this.transferHandler = new SEGraphNodeTransferHandler<E>(this);

		this.setLayout(this.new SEGraphLayoutManager());

		this.selectedNodes.add(model.getStartNode());

		this.setUI(this.new SEGraphArrowUI());
		// Setting opacity to false speeds up rendering.
		this.setOpaque(false);

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
				SEGraph.this.getGroupController().resetGroup();
				SEGraph.this.renderer.resetAppearances();
				SEGraph.this.setUI(SEGraph.this.new SEGraphArrowUI());
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
	 * Forwards the depth map recalculation to the model. Should only be used
	 * when the graph model is changed, as it is performance intensive.
	 */
	public void recalculateDepthMap() {
		this.model.recalculateDepthMap();
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
			this.model.recalculateDepthMap();

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
	 * Returns the ancestors of the node.
	 * 
	 * @param node
	 * @return
	 */
	public Collection<E> getAncestors(E node) {
		return this.model.getAncestors(node);
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

	public void setToolBar(SEGraphToolBar toolbar) {
		this.toolBar = toolbar;
	}

	/**
	 * Returns the {@link GraphGroupController} linked to the graph.
	 * 
	 * @return
	 */
	public GraphGroupController<E> getGroupController() {
		return this.groupController;
	}

	/**
	 * Returns a component for the passed in node. This method creates a
	 * component for the node if none is found.
	 * 
	 * @param node
	 * @return
	 */
	private JComponent getComponentForNode(E node) {
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
	 * The class that handles the actual laying out of GraphNodes. The logic is
	 * fairly basic, and should probably be updated to handle more cases.
	 * 
	 * @author graves
	 * @author kschenk
	 */
	private class SEGraphLayoutManager implements LayoutManager {
		private static final int HORIZONTAL_GAP = 60;
		private static final int VERTICAL_GAP = 15;

		@Override
		public Dimension minimumLayoutSize(Container parent) {
			final SEGraph<E> graph = SEGraph.this;

			final Insets insets;
			final int numberOfLevels;
			final Collection<Integer> levels;

			insets = parent.getInsets();
			levels = graph.model.getDepthMap().values();

			if (levels.isEmpty())
				numberOfLevels = 0;
			else
				numberOfLevels = Collections.max(levels);

			int xSize = insets.left + insets.right;
			int ySize = insets.top + insets.bottom;

			for (int currentLevel = 0; currentLevel <= numberOfLevels; currentLevel++) {
				final Collection<E> currentNodes;

				currentNodes = graph.model.getNodesForLevel(currentLevel);

				// Get the width of the widest JComponent in the level.

				int yNodeSize = 0;
				int maxWidth = 0;
				for (E node : currentNodes) {
					final Dimension size;
					size = graph.getComponentForNode(node).getPreferredSize();

					yNodeSize += VERTICAL_GAP + size.height;
					maxWidth = Math.max(maxWidth, size.width);
				}

				xSize += maxWidth + HORIZONTAL_GAP;
				ySize = Math.max(ySize, yNodeSize);
			}
			return new Dimension(xSize, ySize);
		}

		@Override
		public void layoutContainer(Container parent) {
			final SEGraph<E> graph = SEGraph.this;
			// Remove the current contents of the panel.
			graph.removeAll();

			final List<E> previousNodes = new ArrayList<E>();

			final Map<E, Integer> nodeMap;
			final int numberOfLevels;
			final double graphHeight;

			nodeMap = graph.model.getDepthMap();
			numberOfLevels = Collections.max(nodeMap.values());
			graphHeight = graph.getPreferredSize().getHeight();

			int xLocation = 10;

			// Going through each level is faster than going through each node.
			for (int currentLevel = 0; currentLevel <= numberOfLevels; currentLevel++) {
				final Collection<E> currentNodes;

				final int numberOfNodes;
				final double pixelsPerNode;

				currentNodes = this.sortChildrenByParent(previousNodes,
						graph.model.getNodesForLevel(currentLevel));
				// Width of the widest JComponent in the level.
				numberOfNodes = currentNodes.size();
				// Number of y pixels each node is allocated
				pixelsPerNode = graphHeight / numberOfNodes;

				int maxWidth = 0;
				int position = 0;
				// For each node at that level,
				for (E node : currentNodes) {
					final JComponent component;
					final Dimension componentSize;
					final int yNodeLocation;

					component = graph.getComponentForNode(node);
					// JComponent preferred width
					componentSize = component.getPreferredSize();

					// The y Location for the node.
					yNodeLocation = (int) (pixelsPerNode * (position + 0.5) - componentSize.height / 2);

					if (component.getMouseListeners().length <= 1) {
						component.addMouseListener(graph.mouseAdapter);
						component.addMouseMotionListener(graph.mouseAdapter);
					}

					// Add the component to the panel, so it will draw
					graph.add(component);
					graph.nodesToComponents.put(node, component);

					// Set the size and location of the component
					component.setLocation(xLocation, yNodeLocation);
					component.setSize(componentSize);

					maxWidth = Math.max(maxWidth, componentSize.width);

					position++;
				}

				// Update the x location for the next level.
				xLocation += maxWidth + HORIZONTAL_GAP;

				previousNodes.clear();
				previousNodes.addAll(currentNodes);
			}
		}

		/**
		 * Sorts the given children in a fashion that has overlapping lines
		 * 
		 * @param parents
		 * @param children
		 * @return
		 */
		private Collection<E> sortChildrenByParent(Collection<E> parents,
				Collection<E> children) {
			if (parents == null || parents.isEmpty() || children == null
					|| children.isEmpty())
				return children;

			final List<E> sortedChildren;

			sortedChildren = new IdentityArrayList<E>(children.size());

			int newIndex = 0;
			for (E parent : parents) {
				int parentIndex = newIndex;
				for (E child : children) {
					if (SEGraph.this.model.getChildren(parent).contains(child)) {
						// if the child has already been organized, move it to
						// the first element of the current parent
						if (sortedChildren.contains(child)) {
							sortedChildren.remove(child);
							sortedChildren.add(parentIndex - 1, child);
						} else {
							// insert the childNode near the parent's index
							sortedChildren.add(newIndex, child);
							newIndex++;
						}
					}
				}
			}
			return sortedChildren;
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
	}

	/**
	 * UI used to draw lines between each node.
	 * 
	 * @author previous devs
	 * @author kschenk
	 * @author jyuen
	 * 
	 */
	private class SEGraphArrowUI extends ComponentUI {
		@Override
		public void paint(Graphics g, JComponent c) {
			final float ARROW_WIDTH = 3.0f;
			final int SPACER_FACTOR = 5;

			final SEGraph<E> graph = SEGraph.this;

			final Collection<E> selected;
			final Map<E, Integer> nodeMap;
			final int numberOfLevels;

			final Graphics2D g2;
			final BasicStroke lineStroke;

			selected = graph.getSelectedNodes();
			nodeMap = graph.model.getDepthMap();
			numberOfLevels = Collections.max(nodeMap.values());

			g2 = (Graphics2D) g.create();
			lineStroke = new BasicStroke(ARROW_WIDTH);

			// Paint the graph's background.
			g.setColor(graph.getBackground());
			g.fillRect(0, 0, graph.getWidth(), graph.getHeight());

			// Dragging behaviour for arrows, if we're currently dragging
			if (graph.draggedFromNode != null && graph.mousePosition != null) {
				final Mode mode = graph.getToolBarMode();

				final Point start;
				final Point end;
				final Color lineColor;

				start = GUIOp.getMidRight(graph
						.getComponentForNode(graph.draggedFromNode));
				end = graph.mousePosition;

				if (mode == Mode.INSERT || mode == Mode.CONNECT)
					lineColor = ScriptEaseUI.COLOUR_INSERT_NODE;
				else if (mode == Mode.DISCONNECT)
					lineColor = ScriptEaseUI.COLOUR_DELETE_NODE;
				else
					lineColor = Color.GRAY;

				g2.setColor(lineColor);
				g2.setStroke(lineStroke);

				GUIOp.paintArrow(g2, start, end, 0);
			}

			// For each level in the graph
			for (int currentLevel = 0; currentLevel <= numberOfLevels; currentLevel++) {
				// for each node in the level
				for (E parent : graph.model.getNodesForLevel(currentLevel)) {
					// Get the children of the node.
					final Collection<E> children;
					final JComponent parentComponent;

					final int parentY;
					final int childrenOffset;

					final boolean parentSelected;

					children = graph.getChildren(parent);
					parentComponent = graph.getComponentForNode(parent);

					parentY = parentComponent.getY();
					childrenOffset = (children.size() - 1) * SPACER_FACTOR;

					parentSelected = selected.contains(parent);

					// For each child,
					int previousLevelOffset = -1;
					for (E child : children) {
						final Integer childLevel = nodeMap.get(child);
						if (childLevel == null)
							// We are painting a blank graph. Return.
							return;

						final JComponent childComponent;
						final Color lineColor;

						final Point arrowStart;
						final Point arrowEnd;

						final int childY;
						final int levelOffset;
						final int curveFactor;

						final boolean childSelected;

						childComponent = graph.getComponentForNode(child);

						arrowStart = GUIOp.getMidRight(parentComponent);
						arrowEnd = GUIOp.getMidLeft(childComponent);

						childY = childComponent.getY();
						levelOffset = (childLevel - currentLevel)
								* SPACER_FACTOR;
						curveFactor = childLevel - currentLevel - 1;

						childSelected = selected.contains(child);

						if (SEGraph.this.toolBar.getMode() == Mode.GROUP) {
							final GraphGroupController<E> groupController = SEGraph.this
									.getGroupController();

							final Set<E> group = groupController
									.getCurrentGroup();

							if (group.contains(child) && group.contains(parent)
									&& child != groupController.getStartNode()) {
								if (SEGraph.this.getGroupController().isGroup())
									lineColor = ScriptEaseUI.COLOUR_GROUPABLE_END_NODE;
								else
									lineColor = ScriptEaseUI.COLOUR_GROUPABLE_NODE;
							} else
								lineColor = Color.LIGHT_GRAY;
						} else {
							if (parentSelected && childSelected) {
								lineColor = ScriptEaseUI.COLOUR_SELECTED_NODE;
							} else if (parentSelected) {
								lineColor = ScriptEaseUI.COLOUR_CHILD_NODE
										.darker();
							} else if (childSelected) {
								lineColor = ScriptEaseUI.COLOUR_PARENT_NODE
										.darker();
							} else
								lineColor = Color.LIGHT_GRAY;
						}

						// Move the arrows up if there are more children and
						// the previous level's offset is different.
						if (previousLevelOffset != levelOffset) {
							arrowStart.y -= childrenOffset;
							arrowEnd.y -= childrenOffset;
						}

						// Don't let the arrows start higher than their node.
						if (arrowStart.y < parentY)
							arrowStart.y = parentY;
						if (arrowEnd.y < childY)
							arrowEnd.y = childY;

						if (childrenOffset > 0) {
							if (previousLevelOffset != levelOffset) {
								arrowStart.y += levelOffset;
								arrowEnd.y += levelOffset;

							}
						}
						previousLevelOffset = levelOffset;

						g2.setStroke(lineStroke);
						g2.setColor(lineColor);

						// Draw an arrow pointing towards the child.
						GUIOp.paintArrow(g2, arrowStart, arrowEnd, curveFactor);
					}
				}
			}
			// clean up
			g.dispose();
			g2.dispose();
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
	 * @author jyuen
	 */
	private class NodeMouseAdapter extends MouseAdapter {
		private E lastEnteredNode = null;
		private E lastExitedNode = null;

		@Override
		public void mouseDragged(MouseEvent e) {
			final SEGraph<E> graph = SEGraph.this;

			Container parent = graph.getParent();
			while (parent != null) {
				if (parent instanceof JScrollPane) {
					GUIOp.scrollJScrollPaneToMousePosition((JScrollPane) parent);
					break;
				}
				parent = parent.getParent();
			}

			final Point mousePosition = graph.getMousePosition();

			if (mousePosition != null)
				graph.mousePosition.setLocation(mousePosition);

			graph.repaint();
		}

		@Override
		public void mousePressed(MouseEvent e) {
			final SEGraph<E> graph = SEGraph.this;
			final Mode mode = graph.getToolBarMode();

			final E source;

			source = graph.nodesToComponents.getKey((JComponent) e.getSource());

			if (mode == Mode.INSERT || mode == Mode.CONNECT
					|| mode == Mode.DISCONNECT) {
				graph.draggedFromNode = source;
				SEGraph.this.groupController.resetGroup();
			} else if (mode == Mode.GROUP) {
				if (e.getButton() == MouseEvent.BUTTON3) {
					SEGraph.this.groupController.formGroup();
				} else {
					SEGraph.this.groupController.addNodeToGroup(source);
				}
			} else {
				SEGraph.this.groupController.resetGroup();
			}
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			final SEGraph<E> graph = SEGraph.this;
			final JComponent entered = (JComponent) e.getSource();
			final Mode mode = graph.getToolBarMode();

			this.lastExitedNode = null;
			this.lastEnteredNode = graph.nodesToComponents.getKey(entered);

			if (mode == Mode.GROUP) {
				// Make the cursor appear unavailable for nodes that can't be a
				// part of the current group.
				if (!SEGraph.this.getGroupController().isNodeLegal(
						this.lastEnteredNode)) {
					entered.setCursor(ScriptEaseUI.CURSOR_UNAVAILABLE);
				} else
					entered.setCursor(null);
			} else if (mode == Mode.DELETE) {
				if (this.lastEnteredNode == graph.getStartNode())
					// Make the cursor appear unavailable for start node
					// deletion.
					entered.setCursor(ScriptEaseUI.CURSOR_UNAVAILABLE);
				else if (this.lastEnteredNode instanceof StoryGroup)
					entered.setCursor(ScriptEaseUI.CURSOR_UNGROUP);
				else
					entered.setCursor(null);
			} else if (graph.isReadOnly) {
				if (mode != Mode.SELECT) {
					entered.setCursor(ScriptEaseUI.CURSOR_UNAVAILABLE);
				} else
					entered.setCursor(null);
			} else
				entered.setCursor(null);
		}

		@Override
		public void mouseExited(MouseEvent e) {
			final JComponent exited = (JComponent) e.getSource();

			this.lastExitedNode = SEGraph.this.nodesToComponents.getKey(exited);
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			final SEGraph<E> graph = SEGraph.this;

			final JComponent source;
			final E node;

			source = (JComponent) e.getSource();
			node = graph.nodesToComponents.getKey((JComponent) e.getSource());

			final Mode mode = graph.getToolBarMode();

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
					graph.selectNodesUntil(node);

					for (E selectedNode : graph.getSelectedNodes()) {
						graph.renderer.reconfigureAppearance(
								graph.nodesToComponents.getValue(selectedNode),
								selectedNode);
					}
				} else {
					// Default behaviour is to select individual nodes
					final Collection<E> nodes = new ArrayList<E>();

					nodes.add(node);

					graph.setSelectedNodes(nodes);
				}
				source.requestFocusInWindow();
			} else if (mode == Mode.INSERT) {
				if (this.lastEnteredNode != null)
					if (this.lastEnteredNode == node) {
						if (!UndoManager.getInstance().hasOpenUndoableAction()) {
							UndoManager.getInstance().startUndoableAction(
									"Add new node to " + node);

							graph.addNewNodeTo(node);

							UndoManager.getInstance().endUndoableAction();
						}
					} else {
						if (!UndoManager.getInstance().hasOpenUndoableAction()) {
							UndoManager.getInstance().startUndoableAction(
									"Add " + node + " between "
											+ this.lastEnteredNode + " and "
											+ node);

							graph.addNewNodeBetween(this.lastEnteredNode, node);

							UndoManager.getInstance().endUndoableAction();
						}
					}

				graph.getNodesToComponentsMap().getValue(this.lastEnteredNode)
						.requestFocusInWindow();
			} else if (mode == Mode.DELETE) {
				if (node instanceof StoryGroup) {
					SEGraph.this.groupController.resetGroup();
					if (node instanceof StoryGroup) {
						SEGraph.this.groupController
								.unformGroup((StoryGroup) node);
					}
				} else {
					if (!UndoManager.getInstance().hasOpenUndoableAction())
						UndoManager.getInstance().startUndoableAction(
								"Remove " + node);

					graph.removeNode(node);
					graph.repaint();

					UndoManager.getInstance().endUndoableAction();
				}
			} else if (mode == Mode.CONNECT) {
				if (this.lastEnteredNode != null
						&& this.lastEnteredNode != node
						&& this.lastExitedNode != this.lastEnteredNode) {
					if (!UndoManager.getInstance().hasOpenUndoableAction())
						UndoManager.getInstance().startUndoableAction(
								"Connect " + this.lastEnteredNode + " to "
										+ node);

					graph.connectNodes(this.lastEnteredNode, node);

					UndoManager.getInstance().endUndoableAction();
				}
			} else if (mode == Mode.DISCONNECT) {
				if (this.lastEnteredNode != null
						&& this.lastEnteredNode != node) {
					if (!UndoManager.getInstance().hasOpenUndoableAction())
						UndoManager.getInstance().startUndoableAction(
								"Disconnect " + this.lastEnteredNode + " from "
										+ node);

					graph.disconnectNodes(this.lastEnteredNode, node);

					UndoManager.getInstance().endUndoableAction();
				}
			}

			SEFocusManager.getInstance().setFocus(graph);

			graph.renderer.reconfigureAppearance(
					graph.nodesToComponents.getValue(node), node);
			graph.draggedFromNode = null;

			graph.repaint();
		}
	}
}
