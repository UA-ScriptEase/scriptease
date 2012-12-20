package scriptease.gui.pane;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import scriptease.controller.observer.PatternModelEvent;
import scriptease.controller.observer.PatternModelObserver;
import scriptease.controller.observer.library.LibraryEvent;
import scriptease.controller.observer.library.LibraryManagerEvent;
import scriptease.controller.observer.library.LibraryManagerObserver;
import scriptease.gui.action.typemenus.TypeAction;
import scriptease.gui.filters.CategoryFilter;
import scriptease.gui.filters.CategoryFilter.Category;
import scriptease.gui.filters.StoryComponentSearchFilter;
import scriptease.gui.filters.TranslatorFilter;
import scriptease.gui.filters.TypeFilter;
import scriptease.gui.filters.VisibilityFilter;
import scriptease.gui.internationalization.Il8nResources;
import scriptease.gui.storycomponentpanel.StoryComponentPanelJList;
import scriptease.gui.ui.ScriptEaseUI;
import scriptease.model.LibraryManager;
import scriptease.model.LibraryModel;
import scriptease.model.PatternModel;
import scriptease.model.PatternModelManager;
import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.model.complex.ScriptIt;
import scriptease.translator.Translator;
import scriptease.translator.TranslatorManager;
import scriptease.translator.apimanagers.GameTypeManager;

@SuppressWarnings("serial")
/**
 * LibraryPane represents the JPanel used for managing, filtering and choosing
 * Patterns from the loaded Libraries. It appears in the top left corner of
 * the main ScriptEase window.
 * 
 * @author mfchurch
 * @author kschenk
 */
public class LibraryPanel extends JPanel {
	private static final Comparator<StoryComponent> STORY_COMPONENT_COMPARATOR = LibraryPanel
			.storyComponentSorter();

	private final Timer searchFieldTimer;
	private final LibraryManagerObserver libraryManagerObserver;
	private final List<StoryComponentPanelJList> storyComponentPanelJLists;

	/**
	 * Creates a new LibraryPane with default filters, and configures its
	 * display.
	 * 
	 */
	public LibraryPanel() {
		this.storyComponentPanelJLists = new ArrayList<StoryComponentPanelJList>();
		this.libraryManagerObserver = new LibraryManagerObserver() {
			/**
			 * Keep the display of the library up to date with the changes to
			 * Libraries. This listener is important for the Story Component
			 * Builder, so that changes made there will apply to the library
			 * view as well.
			 */
			@Override
			public void modelChanged(LibraryManagerEvent event) {
				if (event.getEventType() == LibraryManagerEvent.LIBRARYMODEL_CHANGED) {
					final LibraryEvent libraryEvent = event.getEvent();
					final StoryComponent storyComponent = libraryEvent
							.getEvent().getSource();
					if (libraryEvent.getEventType() == LibraryEvent.STORYCOMPONENT_CHANGED) {
						updateElement(storyComponent);
					} else if (libraryEvent.getEventType() == LibraryEvent.STORYCOMPONENT_ADDED) {
						addElement(storyComponent);
					} else if (libraryEvent.getEventType() == LibraryEvent.STORYCOMPONENT_REMOVED) {
						removeElement(storyComponent);
					}
				}
			}
		};

		final JTabbedPane listTabs;

		final JComponent filterPane;
		final JComponent searchFilterPane;
		final JTextField searchField;

		final TypeAction typeFilter;

		final StoryComponentPanelJList causesList;
		final StoryComponentPanelJList effectsList;
		final StoryComponentPanelJList descriptionsList;
		final StoryComponentPanelJList controlsList;

		final PatternModelObserver observer;

		listTabs = new JTabbedPane();

		filterPane = new JPanel();
		searchFilterPane = new JPanel();
		searchField = new JTextField(20);

		typeFilter = new TypeAction();

		// Create the Tree with the root and the default filter
		causesList = new StoryComponentPanelJList(new CategoryFilter(
				Category.CAUSES));
		effectsList = new StoryComponentPanelJList(new CategoryFilter(
				Category.EFFECTS));
		descriptionsList = new StoryComponentPanelJList(new CategoryFilter(
				Category.DESCRIPTIONS));
		controlsList = new StoryComponentPanelJList(new CategoryFilter(
				Category.CONTROLS));

		observer = new PatternModelObserver() {
			/**
			 * This listener checks for when the model is changed. This usually
			 * happens when you load a model, or when you switch them by
			 * switching tabs.
			 */
			@Override
			public void modelChanged(PatternModelEvent event) {
				if (event.getEventType() == PatternModelEvent.PATTERN_MODEL_ACTIVATED)
					updateLists();
				else if (event.getEventType() == PatternModelEvent.PATTERN_MODEL_REMOVED
						&& PatternModelManager.getInstance().getActiveModel() == null) {
					updateLists();
				}
			}
		};

		this.storyComponentPanelJLists.add(causesList);
		this.storyComponentPanelJLists.add(effectsList);
		this.storyComponentPanelJLists.add(descriptionsList);
		this.storyComponentPanelJLists.add(controlsList);

		this.searchFieldTimer = new Timer(1000, new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				for (StoryComponentPanelJList list : LibraryPanel.this.storyComponentPanelJLists)
					list.updateFilter(new StoryComponentSearchFilter(
							searchField.getText()));
				updateLists();
				searchFieldTimer.stop();
			};
		});

		// Set up the listeners
		searchField.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				searchFieldTimer.restart();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				insertUpdate(e);
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
			}
		});

		searchField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				searchFieldTimer.stop();
				for (StoryComponentPanelJList list : LibraryPanel.this.storyComponentPanelJLists)
					list.updateFilter(new StoryComponentSearchFilter(
							searchField.getText()));
				updateLists();
			}
		});

		typeFilter.setAction(new Runnable() {
			@Override
			public void run() {
				for (StoryComponentPanelJList list : LibraryPanel.this.storyComponentPanelJLists)
					list.updateFilter(new TypeFilter(typeFilter
							.getTypeSelectionDialogBuilder().getSelectedTypes()));
				updateLists();
			}
		});

		filterPane.setBorder(BorderFactory.createTitledBorder(BorderFactory
				.createLineBorder(Color.gray), Il8nResources
				.getString("Search_Filter_"),
				TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.TOP, new Font(
						"SansSerif", Font.PLAIN, 12), Color.black));

		listTabs.add("Causes", new JScrollPane(causesList));
		listTabs.add("Effects", new JScrollPane(effectsList));
		listTabs.add("Descriptions", new JScrollPane(descriptionsList));
		listTabs.add("Controls", new JScrollPane(controlsList));

		// SearchFilterPane
		searchFilterPane.add(searchField);
		searchFilterPane.add(new JButton(typeFilter));
		searchFilterPane.setLayout(new BoxLayout(searchFilterPane,
				BoxLayout.LINE_AXIS));

		// FilterPane Layout
		filterPane.setLayout(new BoxLayout(filterPane, BoxLayout.PAGE_AXIS));
		filterPane.add(searchFilterPane);
		filterPane.setMaximumSize(new Dimension(ScriptEaseUI.MAX_SCREEN_WIDTH,
				50));

		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		this.add(filterPane);
		this.add(Box.createVerticalStrut(5));
		this.add(listTabs);

		// Configure the displaying of the pane
		this.updateLists();

		LibraryManager.getInstance().addLibraryManagerObserver(
				this.libraryManagerObserver);
		PatternModelManager.getInstance().addPatternModelObserver(this,
				observer);
	}

	/**
	 * Adds a list mouse listener to each of the tabs. This listens on the
	 * entire list, not just individual cells, so cells will have to be dealt
	 * with separately.
	 * 
	 * @param listener
	 * @see StoryComponentPanelJList#addMouseListener(MouseListener)
	 */
	public void addListMouseListener(MouseListener listener) {
		for (StoryComponentPanelJList list : this.storyComponentPanelJLists) {
			list.addListMouseListener(listener);
		}
	}

	/**
	 * Finds and updates the StoryComponentPanel of the changed StoryComponent
	 * in each StoryComponentPanelJList
	 * 
	 * @param changed
	 */
	private void updateElement(StoryComponent changed) {
		for (StoryComponentPanelJList list : this.storyComponentPanelJLists) {
			list.updateStoryComponentPanel(changed);
		}
	}

	private void addElement(StoryComponent storyComponent) {
		for (StoryComponentPanelJList list : this.storyComponentPanelJLists) {
			list.addStoryComponent(storyComponent);
		}
	}

	private void removeElement(StoryComponent storyComponent) {
		for (StoryComponentPanelJList list : this.storyComponentPanelJLists) {
			list.removeStoryComponent(storyComponent);
		}
	}

	private static Comparator<StoryComponent> storyComponentSorter() {
		return new Comparator<StoryComponent>() {
			@Override
			public int compare(StoryComponent c1, StoryComponent c2) {
				int compare = 0;

				if (c1 instanceof KnowIt && c2 instanceof KnowIt) {
					final KnowIt k1 = (KnowIt) c1;
					final KnowIt k2 = (KnowIt) c2;
					final String k1Type = k1.getDefaultType();
					final String k2Type = k2.getDefaultType();

					final GameTypeManager typeManager = TranslatorManager
							.getInstance().getActiveGameTypeManager();

					String k1Widget = typeManager.getWidgetName(k1Type);
					String k2Widget = typeManager.getWidgetName(k2Type);

					if (k1Widget == null || k1Widget.isEmpty()) {
						if (typeManager.hasEnum(k1Type) == true) {
							k1Widget = GameTypeManager.DEFAULT_LIST_WIDGET;
						}
					}
					if (k2Widget == null || k2Widget.isEmpty()) {
						if (typeManager.hasEnum(k2Type) == true) {
							k2Widget = GameTypeManager.DEFAULT_LIST_WIDGET;
						}
					}

					compare = k1Widget.compareTo(k2Widget);

				} else if (c1 instanceof ScriptIt && c2 instanceof ScriptIt) {
					final ScriptIt s1 = (ScriptIt) c1;
					final ScriptIt s2 = (ScriptIt) c2;

					if (!s1.isCause() || !s2.isCause())
						return 0;

					final Collection<KnowIt> params1 = s1.getParameters();
					final Collection<KnowIt> params2 = s2.getParameters();

					if (params1.isEmpty() && params2.isEmpty()) {
						compare = 0;
					} else if (params1.isEmpty()) {
						compare = 1;
					} else if (params2.isEmpty()) {
						compare = -1;
					} else {
						final KnowIt p1 = params1.iterator().next();
						final KnowIt p2 = params2.iterator().next();

						if (p1 != null && p2 != null) {
							compare = p1.getDefaultType().compareTo(
									p2.getDefaultType());
						}
					}
				}

				if (compare == 0) {
					compare = c1.getDisplayText()
							.compareTo(c2.getDisplayText());
				}
				return compare;
			}
		};
	}

	/**
	 * Updates the lists based on their filters. Works by removing and adding
	 * back all components in the list panes.
	 */
	private void updateLists() {
		final PatternModel model;
		final Translator activeTranslator;
		final boolean hideInvisible;

		model = PatternModelManager.getInstance().getActiveModel();
		activeTranslator = TranslatorManager.getInstance()
				.getActiveTranslator();

		if (model instanceof LibraryModel)
			hideInvisible = false;
		else
			hideInvisible = true;

		for (StoryComponentPanelJList list : this.storyComponentPanelJLists) {
			list.updateFilter(new TranslatorFilter(activeTranslator));
			list.updateFilter(new VisibilityFilter(hideInvisible));
			list.removeAllStoryComponents();

			if (activeTranslator != null) {
				final Collection<LibraryModel> libraries;

				libraries = activeTranslator.getLibraries();

				for (LibraryModel libraryModel : libraries) {
					final List<StoryComponent> components;

					components = libraryModel.getAllStoryComponents();

					Collections.sort(components,
							LibraryPanel.STORY_COMPONENT_COMPARATOR);

					list.addStoryComponents(components);
				}
			}
		}
	}
}
