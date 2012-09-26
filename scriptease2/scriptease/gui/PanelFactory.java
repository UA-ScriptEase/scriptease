package scriptease.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.basic.BasicSplitPaneDivider;

import scriptease.controller.ModelAdapter;
import scriptease.controller.ObservedJPanel;
import scriptease.controller.observer.PatternModelEvent;
import scriptease.controller.observer.PatternModelObserver;
import scriptease.controller.observer.graph.SEGraphAdapter;
import scriptease.controller.observer.library.LibraryManagerEvent;
import scriptease.controller.observer.library.LibraryManagerObserver;
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
import scriptease.gui.filters.CategoryFilter;
import scriptease.gui.filters.TranslatorFilter;
import scriptease.gui.filters.CategoryFilter.Category;
import scriptease.gui.internationalization.Il8nResources;
import scriptease.gui.libraryeditor.LibraryEditorPanelFactory;
import scriptease.gui.pane.GameConstantTree;
import scriptease.gui.pane.LibraryPanel;
import scriptease.gui.storycomponentpanel.StoryComponentPanelJList;
import scriptease.gui.storycomponentpanel.StoryComponentPanelTree;
import scriptease.gui.ui.ScriptEaseUI;
import scriptease.model.LibraryManager;
import scriptease.model.LibraryModel;
import scriptease.model.PatternModel;
import scriptease.model.PatternModelManager;
import scriptease.model.StoryModel;
import scriptease.model.atomic.DescribeIt;
import scriptease.model.complex.StoryPoint;
import scriptease.translator.Translator;
import scriptease.translator.TranslatorManager;
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
		storyGraphPanel = new ObservedJPanel(storyGraph);
		storyGraphPanel.addObserver(graphRedrawer);

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
				.buildLibraryEditorPanel(LibraryPanel.getInstance());
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

	private PatternModelObserver storyLibraryPaneObserver;

	/**
	 * Builds a JSplitPane that is used for pattern models which contains a
	 * LibraryPanel and a GameConstatPanel. The GameConstantPanel is hidden when
	 * a non-StoryModel PatternModel is opened up.
	 * 
	 * @return
	 */
	public JSplitPane buildLibrarySplitPane() {
		final int HEIGHT_OF_NOTE = 31;
		final JSplitPane librarySplitPane;
		final JPanel libraryPanel;
		final JPanel gameConstantPane;
		final StoryComponentPanelJList noteList;
		final JScrollPane notePane;

		librarySplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		gameConstantPane = this.buildGameConstantPane();
		libraryPanel = new JPanel();
		noteList = new StoryComponentPanelJList(new CategoryFilter(
				Category.NOTE));
		notePane = new JScrollPane(noteList);

		notePane.setPreferredSize(new Dimension(0, HEIGHT_OF_NOTE));

		libraryPanel
				.setLayout(new BoxLayout(libraryPanel, BoxLayout.PAGE_AXIS));
		libraryPanel.add(LibraryPanel.getInstance());

		libraryPanel.add(notePane);

		librarySplitPane.setTopComponent(libraryPanel);

		librarySplitPane.setBottomComponent(gameConstantPane);
		if (!(PatternModelManager.getInstance().getActiveModel() instanceof StoryModel)) {
			notePane.setVisible(false);
			gameConstantPane.setVisible(false);
		}
		librarySplitPane.setResizeWeight(0.5);

		this.storyLibraryPaneObserver = new PatternModelObserver() {
			public void modelChanged(PatternModelEvent event) {

				if (event.getEventType() == PatternModelEvent.PATTERN_MODEL_ACTIVATED) {
					event.getPatternModel().process(new ModelAdapter() {

						@Override
						public void processLibraryModel(
								LibraryModel libraryModel) {
							notePane.setVisible(false);
							gameConstantPane.setVisible(false);
						}

						@Override
						public void processStoryModel(StoryModel storyModel) {
							final Translator activeTranslator;
							activeTranslator = TranslatorManager.getInstance()
									.getActiveTranslator();

							noteList.updateFilter(new TranslatorFilter(
									activeTranslator));

							noteList.removeAllStoryComponents();

							if (activeTranslator != null)
								for (LibraryModel libraryModel : TranslatorManager
										.getInstance().getActiveTranslator()
										.getLibraries()) {
									noteList.addStoryComponents(libraryModel
											.getNoteContainer().getChildren());
								}
							notePane.setVisible(true);
							gameConstantPane.setVisible(true);
							SwingUtilities.invokeLater(new Runnable() {
								@Override
								public void run() {
									librarySplitPane.setDividerLocation(0.5);
								}
							});
						}
					});
				}
			}
		};

		PatternModelManager.getInstance().addPatternModelObserver(
				this.storyLibraryPaneObserver);

		return librarySplitPane;
	}

	/**
	 * Builds a pane containing game objects that are used in stories. These can
	 * be dragged into story components as parameters. This pane is based on the
	 * active story and gets updated accordingly.
	 * 
	 * @return
	 */
	public JPanel buildGameConstantPane() {
		final JPanel gameConstantPane;
		final JPanel filterPane;
		final JPanel searchFilterPane;

		final ObservedJPanel observedPanel;

		final GameConstantTree tree;
		// TODO Search Field does nothing right now. Implement it.
		final JTextField searchField = new JTextField(20);

		final PatternModelObserver modelObserver;
		final LibraryManagerObserver libraryObserver;

		gameConstantPane = new JPanel();
		filterPane = new JPanel();
		searchFilterPane = new JPanel();

		tree = new GameConstantTree(PatternModelManager.getInstance()
				.getActiveModel());

		tree.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		tree.setBackground(Color.WHITE);

		// Add the tree to the pane.
		JScrollPane treeScrollPane = new JScrollPane(tree,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		treeScrollPane.setBackground(Color.WHITE);
		treeScrollPane.getVerticalScrollBar().setUnitIncrement(
				ScriptEaseUI.VERTICAL_SCROLLBAR_INCREMENT);

		filterPane.setBorder(BorderFactory.createTitledBorder(BorderFactory
				.createLineBorder(Color.gray), Il8nResources
				.getString("Search_Filter_"),
				TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.TOP, new Font(
						"SansSerif", Font.PLAIN, 12), Color.black));

		// Sets up the type filter.
		// TODO Make types work!
		searchFilterPane.add(searchField);
		searchFilterPane.add(new JButton("Types"));
		searchFilterPane.setLayout(new BoxLayout(searchFilterPane,
				BoxLayout.LINE_AXIS));

		// FilterPane Layout
		BoxLayout filterPaneLayout = new BoxLayout(filterPane, BoxLayout.Y_AXIS);
		filterPane.setLayout(filterPaneLayout);
		filterPane.add(searchFilterPane);
		filterPane.setMaximumSize(new Dimension(2400, 50));

		gameConstantPane.setPreferredSize(new Dimension(
				tree.getPreferredSize().width, 0));

		gameConstantPane.setLayout(new BoxLayout(gameConstantPane,
				BoxLayout.PAGE_AXIS));

		gameConstantPane.add(filterPane);
		gameConstantPane.add(Box.createVerticalStrut(5));
		gameConstantPane.add(treeScrollPane);

		libraryObserver = new LibraryManagerObserver() {
			@Override
			public void modelChanged(LibraryManagerEvent event) {
				/*
				 * Keep the display of the library up to date with the changes
				 * to Libraries. This listener is important for the Story
				 * Component Builder, so that changes made there will apply to
				 * the library view as well.
				 */
				if (event.getEventType() == LibraryManagerEvent.LIBRARYMODEL_CHANGED) {
					tree.drawTree(event.getSource());
				}
			}
		};

		modelObserver = new PatternModelObserver() {
			public void modelChanged(PatternModelEvent event) {
				if (event.getEventType() == PatternModelEvent.PATTERN_MODEL_ACTIVATED) {
					tree.drawTree(event.getPatternModel());
				}
			}
		};

		LibraryManager.getInstance().addLibraryManagerObserver(libraryObserver);
		PatternModelManager.getInstance()
				.addPatternModelObserver(modelObserver);

		// This is very, very hack. But it's the only way our observers are
		// stored for the lifetime of the game constant pane. The sizes get
		// messed up if we just add gameConstantPane to observedPanel and that
		observedPanel = new ObservedJPanel(new JPanel());
		observedPanel.addObserver(libraryObserver);
		observedPanel.addObserver(modelObserver);
		observedPanel.setVisible(false);
		gameConstantPane.add(observedPanel);

		return gameConstantPane;
	}
}