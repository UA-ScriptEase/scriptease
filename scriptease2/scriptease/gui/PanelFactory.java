package scriptease.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.border.EtchedBorder;
import javax.swing.plaf.basic.BasicSplitPaneDivider;

import scriptease.controller.ModelAdapter;
import scriptease.controller.ObservedJPanel;
import scriptease.controller.observer.graph.SEGraphAdapter;
import scriptease.controller.observer.storycomponent.StoryComponentEvent;
import scriptease.controller.observer.storycomponent.StoryComponentEvent.StoryComponentChangeEnum;
import scriptease.controller.observer.storycomponent.StoryComponentObserver;
import scriptease.gui.SEGraph.GraphPanel;
import scriptease.gui.SEGraph.SEGraph;
import scriptease.gui.SEGraph.StoryPointGraphModel;
import scriptease.gui.SEGraph.nodes.GraphNode;
import scriptease.gui.SEGraph.renderers.StoryPointNodeRenderer;
import scriptease.gui.action.graphs.GraphToolBarModeAction;
import scriptease.gui.action.graphs.GraphToolBarModeAction.ToolBarMode;
import scriptease.gui.libraryeditor.LibraryEditorPanelFactory;
import scriptease.gui.pane.GameObjectPane;
import scriptease.gui.pane.LibraryPanel;
import scriptease.gui.storycomponentpanel.StoryComponentPanelTree;
import scriptease.model.LibraryModel;
import scriptease.model.PatternModel;
import scriptease.model.PatternModelManager;
import scriptease.model.StoryModel;
import scriptease.model.atomic.DescribeIt;
import scriptease.model.complex.StoryPoint;
import scriptease.translator.Translator;
import scriptease.translator.codegenerator.GameObjectPicker;
import scriptease.util.BiHashMap;

/**
 * A factory class for different panels. All major panel construction should go
 * in here.
 * 
 * @author kschenk
 * 
 */
public class PanelFactory {
	private static PanelFactory instance = new PanelFactory();
	private final static BiHashMap<PatternModel, List<JComponent>> modelsToComponents = new BiHashMap<PatternModel, List<JComponent>>();

	public static PanelFactory getInstance() {
		return PanelFactory.instance;
	}

	/**
	 * Creates a panel for editing DescribeIts.
	 * 
	 * @param start
	 *            Start Point of the graph
	 * @return
	 */
	public JPanel buildDescribeItPanel(final GraphNode start,
			final DescribeIt describeIt) {
		final JPanel describeItPanel = new JPanel(new BorderLayout(), true);
		final GraphPanel graphPanel = new GraphPanel(start);

		DescribeIt editedDescribeIt = describeIt.clone();
		editedDescribeIt.clearSelection();

		graphPanel.setHeadNode(editedDescribeIt.getHeadNode());

		GraphToolBarModeAction.addJComponent(graphPanel);

		final JToolBar graphToolBar = ToolBarFactory.getInstance()
				.buildGraphEditorToolBar();

		final JToolBar describeItToolBar = ToolBarFactory.getInstance()
				.buildDescribeItToolBar(editedDescribeIt, graphPanel);

		describeItPanel.add(graphToolBar.add(describeItToolBar),
				BorderLayout.PAGE_START);

		GraphToolBarModeAction.setMode(ToolBarMode.SELECT);

		describeItPanel.add(new JScrollPane(graphPanel), BorderLayout.CENTER);

		return describeItPanel;
	}

	/**
	 * Builds a pane containing all game objects in the active module, organized
	 * by category, allowing the user to drag them onto bindings in a Story.
	 * 
	 * @return A JPanel GameObject picker.
	 */
	public JPanel buildGameObjectPane(StoryModel storyModel) {
		GameObjectPicker picker;

		if (storyModel != null) {
			Translator translator = storyModel.getTranslator();
			if (translator != null) {
				// Get the picker
				if ((picker = translator.getCustomGameObjectPicker()) == null) {
					picker = new GameObjectPane();
				}
				return picker.getPickerPanel();
			}
		}
		// otherwise return an empty hidden JPanel
		JPanel jPanel = new JPanel();
		jPanel.setVisible(false);
		jPanel.setSize(new Dimension(0, 0));
		return jPanel;
	}

	/**
	 * Builds a panel for a StoryModel. This panel includes an {@link SEGraph}
	 * and a {@link StoryComponentPanelTree}.
	 * 
	 * @param model
	 * @param start
	 * @return
	 */
	public JSplitPane buildStoryPanel(StoryModel model, StoryPoint start) {
		final JSplitPane storyPanel;
		final JToolBar graphToolBar;

		final SEGraph<StoryPoint> storyGraph;
		final StoryPointGraphModel storyGraphModel;
		final StoryPointNodeRenderer storyNodeRenderer;

		final StoryComponentPanelTree storyComponentTree;
		final StoryComponentObserver graphRedrawer;
		final ObservedJPanel storyGraphPanel;

		final JScrollPane storyGraphScrollPane;

		storyPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		graphToolBar = ToolBarFactory.getInstance().buildGraphEditorToolBar();

		storyGraphModel = new StoryPointGraphModel(start);
		storyGraph = new SEGraph<StoryPoint>(storyGraphModel);
		storyNodeRenderer = new StoryPointNodeRenderer(storyGraph);

		storyComponentTree = new StoryComponentPanelTree(start);
		graphRedrawer = new StoryComponentObserver() {
			@Override
			public void componentChanged(StoryComponentEvent event) {
				if (event.getType() == StoryComponentChangeEnum.STORY_POINT_SUCCESSOR_ADDED) {
					event.getSource().addStoryComponentObserver(this);
					storyGraph.repaint();
					storyGraph.revalidate();
				} else if (event.getType() == StoryComponentChangeEnum.STORY_POINT_SUCCESSOR_REMOVED
						|| event.getType() == StoryComponentChangeEnum.CHANGE_FAN_IN
						|| event.getType() == StoryComponentChangeEnum.CHANGE_TEXT_NAME) {
					storyGraph.repaint();
					storyGraph.revalidate();
				}
			}
		};
		storyGraphPanel = new ObservedJPanel(storyGraph, graphRedrawer);

		storyGraphScrollPane = new JScrollPane(storyGraph);

		for (StoryPoint point : start.getDescendants()) {
			point.addStoryComponentObserver(graphRedrawer);
		}

		// Put the new pane to the map
		List<JComponent> panes;
		panes = PanelFactory.modelsToComponents.getValue(model);
		if (panes == null) {
			panes = new ArrayList<JComponent>();
		}
		panes.add(storyPanel);
		PanelFactory.modelsToComponents.put(model, panes);

		// Set up the Story Graph
		storyGraph.setNodeRenderer(storyNodeRenderer);
		storyGraph.addSEGraphObserver(new SEGraphAdapter() {

			@Override
			public void nodeSelected(final Object node) {
				if (!(node instanceof StoryPoint))
					return;

				final PatternModel activeModel;

				activeModel = PatternModelManager.getInstance()
						.getActiveModel();

				activeModel.process(new ModelAdapter() {
					@Override
					public void processStoryModel(StoryModel storyModel) {
						List<JComponent> components = PanelFactory
								.getInstance()
								.getComponentsForModel(storyModel);

						for (JComponent component : components)
							PanelFactory.getInstance()
									.setRootForTreeInComponent(component,
											(StoryPoint) node);
					}
				});
			}
		});

		start.addStoryComponentObserver(graphRedrawer);

		storyGraphScrollPane.setBorder(BorderFactory
				.createEtchedBorder(EtchedBorder.LOWERED));

		storyGraph.setBackground(Color.WHITE);

		// Reset the ToolBar to select and add the Story Graph to it.
		GraphToolBarModeAction.addJComponent(storyGraph);
		GraphToolBarModeAction.setMode(ToolBarMode.SELECT);

		// Set up the JPanel containing the graph
		storyGraphPanel.setLayout(new BorderLayout());
		storyGraphPanel.add(graphToolBar, BorderLayout.PAGE_START);
		storyGraphPanel.add(storyGraphScrollPane, BorderLayout.CENTER);

		storyComponentTree.setBorder(null);

		// Set up the split pane
		storyPanel.setBorder(null);
		storyPanel.setOpaque(true);
		storyPanel.setTopComponent(storyGraphPanel);
		storyPanel.setBottomComponent(storyComponentTree);

		// Set up the divider
		for (Component component : storyPanel.getComponents()) {
			if (component instanceof BasicSplitPaneDivider) {
				final BasicSplitPaneDivider divider;

				divider = (BasicSplitPaneDivider) component;
				divider.setBackground(Color.WHITE);
				divider.setBorder(null);

				break;
			}
		}

		PanelFactory.componentsToTrees.put(storyPanel, storyComponentTree);

		return storyPanel;
	}

	/**
	 * Builds a panel for a LibraryModel. This panel allows one to edit the
	 * Library.
	 * 
	 * @param model
	 * @return
	 */
	public JScrollPane buildLibraryEditorPanel(LibraryModel model) {
		final JPanel scbPanel;
		final JScrollPane scbScrollPane;

		List<JComponent> components;

		components = PanelFactory.modelsToComponents.getValue(model);
		scbPanel = LibraryEditorPanelFactory.getInstance()
				.buildLibraryEditorPanel(
						PanelFactory.getInstance().getMainLibraryPane());
		scbScrollPane = new JScrollPane(scbPanel);

		if (components == null) {
			components = new ArrayList<JComponent>();
			components.add(scbScrollPane);
			PanelFactory.modelsToComponents.put(model, components);
		}
		components.add(scbScrollPane);
		PatternModelManager.getInstance().add(model);

		return scbScrollPane;
	}

	/*
	 * TODO See notes in individual methods, but I think we should get rid of
	 * this somehow. I just haven't thought of a better way to do it yet.
	 */
	private final static Map<JComponent, StoryComponentPanelTree> componentsToTrees = new IdentityHashMap<JComponent, StoryComponentPanelTree>();

	/**
	 * Sets the root for a StoryComponentPanelTree that was created from
	 * {@link PanelFactory#buildStoryPanel(StoryModel, StoryPoint)}.
	 * 
	 * TODO I still feel like this is hackish, not to mention we could get an
	 * eventual memory leak if someone keeps closing and opening stories... We
	 * should have some other way of doing this. -kschenk
	 * 
	 * @param component
	 * @param storyPoint
	 */
	public void setRootForTreeInComponent(JComponent component,
			StoryPoint storyPoint) {

		PanelFactory.componentsToTrees.get(component).setRoot(storyPoint);
	}

	/**
	 * NOTE: Methods that call this method should always either check if null is
	 * returned, or use {@link #getModelForPanel(JComponent)} to check if the
	 * panel passed in represents a StoryModel. Only Story Model panels are
	 * added to the map, so if you attempt to use a different kind of
	 * PatternModel, this method will just return null.
	 * 
	 * TODO Again, this seems hackish and if we need a note like the above,
	 * we're just asking for future problems. -kschenk
	 * 
	 * @param component
	 * @return
	 */
	public StoryComponentPanelTree getTreeForComponent(JComponent component) {
		return PanelFactory.componentsToTrees.get(component);
	}

	/**
	 * Returns the model represented by the passed in component.
	 * 
	 * @param modelComponent
	 * @return
	 */
	public PatternModel getModelForComponent(JComponent modelComponent) {
		for (List<JComponent> jComponentList : PanelFactory.modelsToComponents
				.getValues())
			if (jComponentList.contains(modelComponent))
				return PanelFactory.modelsToComponents.getKey(jComponentList);

		throw new IllegalStateException(
				"Encountered null model when attempting to get model for "
						+ modelComponent.getName());
	}

	/**
	 * Gets the collection of panes that are currently displaying the given
	 * model. Cannot be null.
	 * 
	 * @param model
	 * @return
	 */
	public List<JComponent> getComponentsForModel(PatternModel model) {
		final List<JComponent> panels = PanelFactory.modelsToComponents
				.getValue(model);

		if (panels == null) {
			System.out
					.println("WARNING: Encountered null list of model display "
							+ "panels when attempting to get panels for "
							+ model.getName());

			return new ArrayList<JComponent>();
		}

		return panels;
	}

	/**
	 * Removes the given component from the list of the component's associated
	 * with the given model.
	 * 
	 * @param model
	 * @param component
	 */
	public void removeComponentForModel(PatternModel model, JComponent component) {
		final List<JComponent> components = new ArrayList<JComponent>();

		if (PanelFactory.modelsToComponents.getValue(model) == null)
			throw new IllegalStateException(
					"Encountered null list of model display panels "
							+ "when attempting to remove panels for "
							+ model.getName());

		components.addAll(PanelFactory.modelsToComponents.getValue(model));

		components.remove(component);

		if (!components.isEmpty())
			PanelFactory.modelsToComponents.put(model, components);
		else
			PanelFactory.modelsToComponents.removeKey(model);
	}

	private static LibraryPanel mainLibraryPane = new LibraryPanel(true);

	public LibraryPanel getMainLibraryPane() {
		return PanelFactory.mainLibraryPane;
	}
}