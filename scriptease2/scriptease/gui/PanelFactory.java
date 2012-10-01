package scriptease.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicSplitPaneDivider;

import scriptease.controller.FileManager;
import scriptease.controller.ModelAdapter;
import scriptease.controller.ObservedJPanel;
import scriptease.controller.observer.ObserverFactory;
import scriptease.controller.observer.PatternModelEvent;
import scriptease.controller.observer.PatternModelObserver;
import scriptease.controller.observer.StatusObserver;
import scriptease.controller.observer.TranslatorObserver;
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
import scriptease.gui.action.typemenus.TypeAction;
import scriptease.gui.filters.CategoryFilter;
import scriptease.gui.filters.CategoryFilter.Category;
import scriptease.gui.internationalization.Il8nResources;
import scriptease.gui.libraryeditor.LibraryEditorPanelFactory;
import scriptease.gui.pane.CloseableModelTab;
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
import scriptease.translator.TranslatorManager;
import scriptease.util.BiHashMap;

/**
 * A factory class for different panels. All major panel construction should go
 * in here. This class implements the singleton design pattern.
 * 
 * @author kschenk
 * 
 */
public class PanelFactory {
	private static PanelFactory instance = new PanelFactory();

	// The tabbed pane for all of the models
	private final JTabbedPane modelTabs;

	// A mapping of models to components represented by the models
	private final BiHashMap<PatternModel, List<JComponent>> modelsToComponents;

	/**
	 * Gets the instance of PanelFactory.
	 * 
	 * @return
	 */
	public static PanelFactory getInstance() {
		return PanelFactory.instance;
	}

	private PanelFactory() {
		this.modelTabs = new JTabbedPane();
		this.modelsToComponents = new BiHashMap<PatternModel, List<JComponent>>();

		// Register a change listener
		this.modelTabs.addChangeListener(new ChangeListener() {
			// This method is called whenever the selected tab changes
			public void stateChanged(ChangeEvent evt) {
				final JComponent tab;

				JTabbedPane pane = (JTabbedPane) evt.getSource();
				// Get the activated frame
				tab = (JComponent) pane.getSelectedComponent();

				if (tab != null) {
					PatternModel model = PanelFactory.getInstance()
							.getModelForComponent(tab);
					PatternModelManager.getInstance().activate(model);
				}
			}
		});
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
		panes = this.modelsToComponents.getValue(model);
		if (panes == null) {
			panes = new ArrayList<JComponent>();
		}
		panes.add(storyPanel);
		this.modelsToComponents.put(model, panes);

		// Set up the Story Graph
		storyGraph.setNodeRenderer(storyNodeRenderer);
		storyGraph.addSEGraphObserver(new SEGraphAdapter() {

			@Override
			public void nodeSelected(final Object node) {
				if (!(node instanceof StoryPoint))
					return;

				PatternModelManager.getInstance().getActiveModel()
						.process(new ModelAdapter() {
							@Override
							public void processStoryModel(StoryModel storyModel) {
								storyComponentTree.setRoot((StoryPoint) node);
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

		components = this.modelsToComponents.getValue(model);
		scbPanel = LibraryEditorPanelFactory.getInstance()
				.buildLibraryEditorPanel(LibraryPanel.getInstance());
		scbScrollPane = new JScrollPane(scbPanel);

		if (components == null) {
			components = new ArrayList<JComponent>();
			components.add(scbScrollPane);
			this.modelsToComponents.put(model, components);
		}
		components.add(scbScrollPane);
		PatternModelManager.getInstance().add(model);

		return scbScrollPane;
	}

	/**
	 * Returns the model represented by the passed in component.
	 * 
	 * @param modelComponent
	 * @return
	 */
	public PatternModel getModelForComponent(JComponent modelComponent) {
		for (List<JComponent> jComponentList : this.modelsToComponents
				.getValues())
			if (jComponentList.contains(modelComponent))
				return this.modelsToComponents.getKey(jComponentList);

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
		final List<JComponent> panels = this.modelsToComponents.getValue(model);

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

		if (this.modelsToComponents.getValue(model) == null)
			throw new IllegalStateException(
					"Encountered null list of model display panels "
							+ "when attempting to remove panels for "
							+ model.getName());

		components.addAll(this.modelsToComponents.getValue(model));

		components.remove(component);

		if (!components.isEmpty())
			this.modelsToComponents.put(model, components);
		else
			this.modelsToComponents.removeKey(model);
	}

	/**
	 * Builds a JSplitPane that is used for pattern models which contains a
	 * LibraryPanel and a GameConstatPanel. The GameConstantPanel is hidden when
	 * a non-StoryModel PatternModel is opened up.
	 * 
	 * @return
	 */
	public JSplitPane buildLibrarySplitPane() {
		final int HEIGHT_OF_NOTE = 40;
		final Dimension notePaneSize = new Dimension(0, HEIGHT_OF_NOTE);

		final JSplitPane librarySplitPane;
		final JPanel libraryPanel;
		final JPanel gameConstantPane;
		final StoryComponentPanelJList noteList;
		final JScrollPane notePane;

		final PatternModelObserver storyLibraryPaneObserver;
		final Collection<JComponent> storyJComponents;

		librarySplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		gameConstantPane = this.buildGameConstantPane();
		libraryPanel = new JPanel();
		noteList = new StoryComponentPanelJList(new CategoryFilter(
				Category.NOTE));
		notePane = new JScrollPane(noteList);

		storyJComponents = new ArrayList<JComponent>();

		notePane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
		notePane.setPreferredSize(notePaneSize);
		notePane.setMinimumSize(notePaneSize);
		notePane.setMaximumSize(new Dimension(ScriptEaseUI.MAX_SCREEN_WIDTH,
				HEIGHT_OF_NOTE));

		libraryPanel
				.setLayout(new BoxLayout(libraryPanel, BoxLayout.PAGE_AXIS));
		libraryPanel.add(LibraryPanel.getInstance());

		libraryPanel.add(notePane);

		librarySplitPane.setTopComponent(libraryPanel);

		storyJComponents.add(notePane);
		storyJComponents.add(gameConstantPane);

		librarySplitPane.setBottomComponent(gameConstantPane);
		if (!(PatternModelManager.getInstance().getActiveModel() instanceof StoryModel)) {
			for (JComponent component : storyJComponents)
				component.setVisible(false);
		}
		librarySplitPane.setResizeWeight(0.5);

		for (LibraryModel libraryModel : LibraryManager.getInstance()
				.getLibraries())
			noteList.addStoryComponents(libraryModel.getNoteContainer()
					.getChildren());

		storyLibraryPaneObserver = ObserverFactory.getInstance()
				.buildStoryLibraryPaneObserver(librarySplitPane,
						storyJComponents);

		PatternModelManager.getInstance().addPatternModelObserver(
				storyLibraryPaneObserver);

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

		final JScrollPane treeScrollPane;

		final ObservedJPanel observedPanel;

		final GameConstantTree tree;
		final JTextField searchField;

		final TypeAction typeFilter;

		final PatternModelObserver modelObserver;
		final LibraryManagerObserver libraryObserver;

		gameConstantPane = new JPanel();
		filterPane = new JPanel();
		searchFilterPane = new JPanel();

		tree = new GameConstantTree(PatternModelManager.getInstance()
				.getActiveModel());
		searchField = new JTextField(20);

		typeFilter = new TypeAction();

		treeScrollPane = new JScrollPane(tree,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		tree.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		tree.setBackground(Color.WHITE);

		treeScrollPane.setBackground(Color.WHITE);
		treeScrollPane.getVerticalScrollBar().setUnitIncrement(
				ScriptEaseUI.VERTICAL_SCROLLBAR_INCREMENT);

		filterPane.setBorder(BorderFactory.createTitledBorder(BorderFactory
				.createLineBorder(Color.gray), Il8nResources
				.getString("Search_Filter_"),
				TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.TOP, new Font(
						"SansSerif", Font.PLAIN, 12), Color.black));

		typeFilter.setAction(new Runnable() {
			@Override
			public void run() {
				tree.drawTree(PatternModelManager.getInstance()
						.getActiveModel(), searchField.getText(), typeFilter
						.getSelectedTypes());
			}
		});

		// Sets up the type filter.
		searchFilterPane.add(searchField);
		searchFilterPane.add(new JButton(typeFilter));
		searchFilterPane.setLayout(new BoxLayout(searchFilterPane,
				BoxLayout.LINE_AXIS));

		// FilterPane Layout
		BoxLayout filterPaneLayout = new BoxLayout(filterPane, BoxLayout.Y_AXIS);
		filterPane.setLayout(filterPaneLayout);
		filterPane.add(searchFilterPane);
		filterPane.setMaximumSize(new Dimension(ScriptEaseUI.MAX_SCREEN_WIDTH,
				50));

		gameConstantPane.setPreferredSize(new Dimension(
				tree.getPreferredSize().width, 0));

		gameConstantPane.setLayout(new BoxLayout(gameConstantPane,
				BoxLayout.PAGE_AXIS));

		gameConstantPane.add(filterPane);
		gameConstantPane.add(Box.createVerticalStrut(5));
		gameConstantPane.add(treeScrollPane);

		searchField.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				tree.drawTree(PatternModelManager.getInstance()
						.getActiveModel(), searchField.getText(), typeFilter
						.getSelectedTypes());
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				insertUpdate(e);
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				insertUpdate(e);
			}
		});

		libraryObserver = new LibraryManagerObserver() {
			@Override
			public void modelChanged(LibraryManagerEvent event) {
				if (event.getEventType() == LibraryManagerEvent.LIBRARYMODEL_CHANGED) {
					tree.drawTree(event.getSource(), searchField.getText(),
							typeFilter.getSelectedTypes());
				}
			}
		};

		modelObserver = new PatternModelObserver() {
			public void modelChanged(PatternModelEvent event) {
				if (event.getEventType() == PatternModelEvent.PATTERN_MODEL_ACTIVATED) {
					tree.drawTree(event.getPatternModel(),
							searchField.getText(),
							typeFilter.getSelectedTypes());
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

	public JTabbedPane getModelTabPane() {
		return this.modelTabs;
	}

	/**
	 * Creates a tab for the given model.
	 * 
	 * @param model
	 */
	public void createTabForModel(PatternModel model) {
		final JTabbedPane modelTabs = this.modelTabs;

		final Icon icon;

		if (model.getTranslator() != null)
			icon = model.getTranslator().getIcon();
		else
			icon = null;

		model.process(new ModelAdapter() {
			@Override
			public void processLibraryModel(LibraryModel libraryModel) {
				// Creates a Library Editor panel
				final JScrollPane scbScrollPane;
				final CloseableModelTab newTab;

				scbScrollPane = PanelFactory.getInstance()
						.buildLibraryEditorPanel(libraryModel);
				newTab = new CloseableModelTab(modelTabs, scbScrollPane,
						libraryModel, icon);

				scbScrollPane.getVerticalScrollBar().setUnitIncrement(
						ScriptEaseUI.VERTICAL_SCROLLBAR_INCREMENT);

				modelTabs.addTab(libraryModel.getName() + "[Editor]", icon,
						scbScrollPane);
				modelTabs.setTabComponentAt(
						modelTabs.indexOfComponent(scbScrollPane), newTab);
			}

			@Override
			public void processStoryModel(final StoryModel storyModel) {
				// Creates a story editor panel with a story graph
				final StoryPoint startStoryPoint;
				final JSplitPane newPanel;
				final CloseableModelTab newTab;
				final String title;
				String modelTitle;

				startStoryPoint = storyModel.getRoot();
				newPanel = PanelFactory.getInstance().buildStoryPanel(
						storyModel, startStoryPoint);
				newTab = new CloseableModelTab(modelTabs, newPanel, storyModel,
						icon);
				modelTitle = storyModel.getTitle();

				if (modelTitle == null || modelTitle.equals(""))
					modelTitle = "<Untitled>";

				title = modelTitle + "("
						+ storyModel.getModule().getLocation().getName() + ")";

				modelTabs.addTab(title, icon, newPanel);
				modelTabs.setTabComponentAt(
						modelTabs.indexOfComponent(newPanel), newTab);
				modelTabs.setSelectedComponent(newPanel);

				/*
				 * Setting the divider needs to occur here because the
				 * JSplitPane needs to actually be drawn before this works.
				 * According to Sun, this is WAD. I would tend to disagree, but
				 * at least this is nicer than subclassing JSplitPane.
				 */
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						newPanel.setDividerLocation(0.3);
					}
				});
			}
		});
	}

	/**
	 * Removes the given model component from list of ModelTabs and the list of
	 * model components for the given model. modelTabs.remove should not be
	 * called outside of this method.
	 * 
	 * @param component
	 * @param model
	 */
	public void removeModelComponent(JComponent component, PatternModel model) {
		// remove the panel
		PanelFactory.getInstance().removeComponentForModel(model, component);

		this.modelTabs.remove(component);

		// check if there are any unsaved changes
		if (FileManager.getInstance().hasUnsavedChanges(model)) {
			// otherwise, close the StoryModel

			model.process(new ModelAdapter() {
				@Override
				public void processLibraryModel(LibraryModel libraryModel) {
					// TODO Should close the translator if it's not open
					// anywhere else. We can use the usingTranslator in
					// PatternModelPool to check for this.
				};

				@Override
				public void processStoryModel(StoryModel storyModel) {
					FileManager.getInstance().close(storyModel);
				}
			});
		}
	}

	public JPanel buildStatusPanel() {
		final String transPrefix = "Game: ";

		final JPanel statusPanel;
		final JLabel timedLabel;
		final JLabel currentTranslatorLabel;
		final JLabel currentTranslatorNameLabel;

		final TranslatorObserver translatorObserver;
		final StatusObserver statusObserver;

		statusPanel = new JPanel();
		timedLabel = new JLabel();
		currentTranslatorLabel = new JLabel(transPrefix);
		currentTranslatorNameLabel = new JLabel("-None-");

		translatorObserver = ObserverFactory.getInstance()
				.buildStatusBarTranslatorObserver(currentTranslatorLabel);

		statusObserver = new StatusObserver() {
			@Override
			public void statusChanged(String newText) {
				timedLabel.setText(newText);
			}
		};

		TranslatorManager.getInstance().addTranslatorObserver(
				translatorObserver);
		StatusManager.getInstance().addStatusObserver(statusObserver);

		currentTranslatorNameLabel.setEnabled(false);
		currentTranslatorNameLabel.setBorder(BorderFactory.createEmptyBorder(0,
				5, 0, 5));

		statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.LINE_AXIS));

		statusPanel.add(timedLabel);
		statusPanel.add(Box.createGlue());
		statusPanel.add(currentTranslatorLabel);
		statusPanel.add(currentTranslatorNameLabel);

		statusPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

		return statusPanel;
	}
}