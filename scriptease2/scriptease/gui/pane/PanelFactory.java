package scriptease.gui.pane;

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
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicSplitPaneDivider;

import scriptease.ScriptEase;
import scriptease.controller.ModelAdapter;
import scriptease.controller.observer.SEModelEvent;
import scriptease.controller.observer.SEModelObserver;
import scriptease.controller.observer.StatusObserver;
import scriptease.controller.observer.TranslatorObserver;
import scriptease.controller.observer.library.LibraryManagerEvent;
import scriptease.controller.observer.library.LibraryManagerObserver;
import scriptease.controller.observer.storycomponent.StoryComponentEvent;
import scriptease.controller.observer.storycomponent.StoryComponentEvent.StoryComponentChangeEnum;
import scriptease.controller.observer.storycomponent.StoryComponentObserver;
import scriptease.gui.ComponentFactory;
import scriptease.gui.StatusManager;
import scriptease.gui.SEGraph.SEGraph;
import scriptease.gui.SEGraph.SEGraphFactory;
import scriptease.gui.SEGraph.observers.SEGraphAdapter;
import scriptease.gui.action.graphs.GraphToolBarModeAction;
import scriptease.gui.action.typemenus.TypeAction;
import scriptease.gui.filters.CategoryFilter;
import scriptease.gui.filters.CategoryFilter.Category;
import scriptease.gui.internationalization.Il8nResources;
import scriptease.gui.libraryeditor.LibraryEditorPanelFactory;
import scriptease.gui.storycomponentpanel.StoryComponentPanelJList;
import scriptease.gui.storycomponentpanel.StoryComponentPanelTree;
import scriptease.gui.ui.ScriptEaseUI;
import scriptease.model.LibraryManager;
import scriptease.model.LibraryModel;
import scriptease.model.SEModel;
import scriptease.model.SEModelManager;
import scriptease.model.StoryModel;
import scriptease.model.complex.StoryPoint;
import scriptease.translator.Translator;
import scriptease.translator.TranslatorManager;

/**
 * A factory class for different panels. All major panel construction should go
 * in here. This class implements the singleton design pattern.
 * 
 * @author kschenk
 * 
 */
public final class PanelFactory {
	private static PanelFactory instance = new PanelFactory();

	/**
	 * Gets the instance of PanelFactory.
	 * 
	 * @return
	 */
	public static PanelFactory getInstance() {
		return PanelFactory.instance;
	}

	private PanelFactory() {
	}

	/**
	 * Builds a panel for a StoryModel. This panel includes an {@link SEGraph}
	 * and a {@link StoryComponentPanelTree}.
	 * 
	 * @param model
	 * @param start
	 * @return
	 */
	public JSplitPane buildStoryPanel(StoryModel model, final StoryPoint start) {
		final JSplitPane storyPanel;
		final JToolBar graphToolBar;

		final SEGraph<StoryPoint> storyGraph;
		final StoryComponentPanelTree storyComponentTree;
		final StoryComponentObserver graphRedrawer;
		final JPanel storyGraphPanel;

		final JScrollPane storyGraphScrollPane;

		storyPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		graphToolBar = ComponentFactory.buildGraphEditorToolBar();

		storyGraph = SEGraphFactory.buildStoryGraph(start);

		storyComponentTree = new StoryComponentPanelTree(start);
		graphRedrawer = new StoryComponentObserver() {
			@Override
			public void componentChanged(StoryComponentEvent event) {
				final StoryComponentChangeEnum type;

				type = event.getType();

				if (type == StoryComponentChangeEnum.STORY_POINT_SUCCESSOR_ADDED) {
					event.getSource().addStoryComponentObserver(this);
					storyGraph.repaint();
					storyGraph.revalidate();
				} else if (type == StoryComponentChangeEnum.CHANGE_FAN_IN
						|| type == StoryComponentChangeEnum.CHANGE_TEXT_NAME) {
					storyGraph.repaint();
					storyGraph.revalidate();
				} else if (type == StoryComponentChangeEnum.STORY_POINT_SUCCESSOR_REMOVED) {
					storyGraph.repaint();
					storyGraph.revalidate();

					// Set root to start node if we remove the selected node.
					if (event.getSource() == storyComponentTree.getRoot()) {
						final Collection<StoryPoint> nodes;

						nodes = new ArrayList<StoryPoint>();

						nodes.add(storyGraph.getStartNode());

						storyGraph.setSelectedNodes(nodes);
						storyComponentTree.setRoot(storyGraph.getStartNode());
					}
				}

			}
		};
		storyGraphPanel = new JPanel();

		storyGraphScrollPane = new JScrollPane(storyGraph);

		for (StoryPoint point : start.getDescendants()) {
			point.addStoryComponentObserver(graphRedrawer);
		}

		// Put the new pane to the map

		// TODO This ugly.
		ModelTabPanel.getInstance().getModelsToComponents()
				.put(model, storyPanel);

		// Set up the Story Graph
		storyGraph.addSEGraphObserver(new SEGraphAdapter<StoryPoint>() {

			@Override
			public void nodesSelected(final Collection<StoryPoint> nodes) {
				SEModelManager.getInstance().getActiveModel()
						.process(new ModelAdapter() {
							@Override
							public void processStoryModel(StoryModel storyModel) {
								storyComponentTree.setRoot(nodes.iterator()
										.next());
							}
						});
			}

			@Override
			public void nodeOverwritten(StoryPoint node) {
				node.revalidateKnowItBindings();
			}

			@Override
			public void nodeRemoved(StoryPoint removedNode) {
				start.revalidateKnowItBindings();
			}

		});

		start.addStoryComponentObserver(graphRedrawer);

		storyGraphPanel.setLayout(new BorderLayout());

		// Reset the ToolBar to select and add the Story Graph to it.
		GraphToolBarModeAction.setMode(GraphToolBarModeAction.getMode());

		final String orientation = ScriptEase.getInstance().getPreference(
				ScriptEase.PREFERRED_ORIENTATION_KEY);

		if (orientation != null
				&& orientation.equalsIgnoreCase(ScriptEase.HORIZONTAL_TOOLBAR)) {
			storyGraphScrollPane.setBorder(BorderFactory
					.createEtchedBorder(EtchedBorder.LOWERED));

			storyGraphPanel.add(graphToolBar, BorderLayout.PAGE_START);
		} else {// if toolbar is vertical
			storyGraphScrollPane.setBorder(BorderFactory.createEmptyBorder());
			storyGraphPanel.setBorder(BorderFactory
					.createEtchedBorder(EtchedBorder.LOWERED));

			graphToolBar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1,
					Color.LIGHT_GRAY));

			storyGraphPanel.add(graphToolBar, BorderLayout.WEST);
		}

		storyGraphPanel.add(storyGraphScrollPane, BorderLayout.CENTER);

		storyComponentTree.setBorder(BorderFactory
				.createEtchedBorder(EtchedBorder.LOWERED));

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

	public ModelTabPanel buildModelTabPanel() {
		return ModelTabPanel.getInstance();
	}

	public void createTabForModel(SEModel model) {
		ModelTabPanel.getInstance().createTabForModel(model);
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

		// TODO This seems weird
		scbPanel = LibraryEditorPanelFactory.getInstance()
				.buildLibraryEditorPanel(LibraryPanel.getInstance());
		scbScrollPane = new JScrollPane(scbPanel);

		ModelTabPanel.getInstance().getModelsToComponents()
				.put(model, scbScrollPane);
		SEModelManager.getInstance().add(model);

		return scbScrollPane;
	}

	/**
	 * Builds a JSplitPane that is used for pattern models which contains a
	 * LibraryPanel and a GameConstantPanel. The GameConstantPanel is hidden
	 * when a non-StoryModel PatternModel is opened up.
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

		final SEModelObserver storyLibraryPaneObserver;
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
		if (!(SEModelManager.getInstance().getActiveModel() instanceof StoryModel)) {
			for (JComponent component : storyJComponents)
				component.setVisible(false);
		}
		librarySplitPane.setResizeWeight(0.5);

		for (LibraryModel libraryModel : LibraryManager.getInstance()
				.getLibraries())
			noteList.addStoryComponents(libraryModel.getNoteContainer()
					.getChildren());

		storyLibraryPaneObserver = new SEModelObserver() {
			public void modelChanged(SEModelEvent event) {

				if (event.getEventType() == SEModelEvent.Type.ACTIVATED) {
					event.getPatternModel().process(new ModelAdapter() {

						@Override
						public void processLibraryModel(
								LibraryModel libraryModel) {
							for (JComponent component : storyJComponents)
								component.setVisible(false);
						}

						@Override
						public void processStoryModel(StoryModel storyModel) {
							for (JComponent component : storyJComponents)
								component.setVisible(true);
							SwingUtilities.invokeLater(new Runnable() {
								@Override
								public void run() {
									librarySplitPane.setDividerLocation(0.5);
								}
							});
						}
					});
				} else if (event.getEventType() == SEModelEvent.Type.REMOVED) {
					if (SEModelManager.getInstance().getActiveModel() == null) {
						for (JComponent component : storyJComponents)
							component.setVisible(false);
					}
				}
			}
		};

		SEModelManager.getInstance().addPatternModelObserver(librarySplitPane,
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

		final ResourcePanel tree;
		final JTextField searchField;

		final TypeAction typeFilter;

		final SEModelObserver modelObserver;
		final LibraryManagerObserver libraryObserver;

		gameConstantPane = new JPanel();
		filterPane = new JPanel();
		searchFilterPane = new JPanel();

		tree = new ResourcePanel(SEModelManager.getInstance().getActiveModel());
		searchField = ComponentFactory.buildJTextFieldWithTextBackground(20,
				"Game Objects", "");

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
				tree.filterByTypes(typeFilter.getSelectedTypes());
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
				tree.filterByText(searchField.getText());
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
					tree.fillTree(event.getSource());
					tree.filterByTypes(typeFilter.getSelectedTypes());
				}
			}
		};

		modelObserver = new SEModelObserver() {
			public void modelChanged(SEModelEvent event) {
				if (event.getEventType() == SEModelEvent.Type.ACTIVATED) {
					tree.fillTree(event.getPatternModel());
					tree.filterByTypes(typeFilter.getSelectedTypes());
				} else if (event.getEventType() == SEModelEvent.Type.REMOVED
						&& SEModelManager.getInstance().getActiveModel() == null) {
					tree.fillTree(null);
				}
			}
		};

		LibraryManager.getInstance().addLibraryManagerObserver(
				gameConstantPane, libraryObserver);
		SEModelManager.getInstance().addPatternModelObserver(gameConstantPane,
				modelObserver);

		return gameConstantPane;
	}

	/**
	 * Builds a panel that displays the status of the game based on what has
	 * been passed to the {@link StatusManager}.
	 * 
	 * @return
	 */
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

		translatorObserver = new TranslatorObserver() {
			@Override
			public void translatorLoaded(Translator newTranslator) {
				if (newTranslator != null) {
					currentTranslatorNameLabel.setText(newTranslator.getName());
					currentTranslatorNameLabel.setEnabled(true);
					currentTranslatorNameLabel.setIcon(newTranslator.getIcon());
				} else {
					currentTranslatorNameLabel.setText("-None-");
					currentTranslatorNameLabel.setEnabled(false);
					currentTranslatorNameLabel.setIcon(null);
				}
			}
		};

		statusObserver = new StatusObserver() {
			@Override
			public void statusChanged(String newText) {
				timedLabel.setText(newText);
			}
		};

		TranslatorManager.getInstance().addTranslatorObserver(statusPanel,
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