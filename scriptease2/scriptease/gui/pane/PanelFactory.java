package scriptease.gui.pane;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Collection;

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
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import scriptease.controller.ModelAdapter;
import scriptease.controller.observer.SEModelEvent;
import scriptease.controller.observer.SEModelObserver;
import scriptease.controller.observer.StatusObserver;
import scriptease.controller.observer.TranslatorObserver;
import scriptease.gui.StatusManager;
import scriptease.gui.action.typemenus.TypeAction;
import scriptease.gui.component.ComponentFactory;
import scriptease.gui.filters.CategoryFilter;
import scriptease.gui.filters.CategoryFilter.Category;
import scriptease.gui.internationalization.Il8nResources;
import scriptease.gui.storycomponentpanel.StoryComponentPanelJList;
import scriptease.gui.ui.ScriptEaseUI;
import scriptease.model.LibraryManager;
import scriptease.model.LibraryModel;
import scriptease.model.SEModelManager;
import scriptease.model.StoryModel;
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

	public ModelTabPanel buildModelTabPanel() {
		return new ModelTabPanel();
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

		SEModelManager.getInstance().addPatternModelObserver(gameConstantPane,
				new SEModelObserver() {

					public void modelChanged(SEModelEvent event) {
						if (event.getEventType() == SEModelEvent.Type.ACTIVATED) {
							tree.fillTree(event.getPatternModel());
							tree.filterByTypes(typeFilter.getSelectedTypes());
						} else if (event.getEventType() == SEModelEvent.Type.REMOVED
								&& SEModelManager.getInstance()
										.getActiveModel() == null) {
							tree.fillTree(null);
						}
					}
				});

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