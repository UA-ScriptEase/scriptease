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

import scriptease.controller.observer.SEModelEvent;
import scriptease.controller.observer.SEModelObserver;
import scriptease.controller.observer.TranslatorObserver;
import scriptease.controller.observer.library.LibraryEvent;
import scriptease.controller.observer.library.LibraryManagerEvent;
import scriptease.controller.observer.library.LibraryManagerObserver;
import scriptease.gui.ComponentFactory;
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
import scriptease.model.SEModel;
import scriptease.model.SEModelManager;
import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.model.complex.ScriptIt;
import scriptease.translator.Translator;
import scriptease.translator.TranslatorManager;
import scriptease.translator.apimanagers.GameTypeManager;

/**
 * LibraryPane represents the JPanel used for managing, filtering and choosing
 * Patterns from the loaded Libraries. It appears in the top left corner of the
 * main ScriptEase window.
 * 
 * @author mfchurch
 * @author kschenk
 */
@SuppressWarnings("serial")
public class LibraryPanel extends JTabbedPane {
	private static final Comparator<StoryComponent> STORY_COMPONENT_COMPARATOR = LibraryPanel
			.storyComponentSorter();

	private final List<StoryComponentPanelJList> storyComponentPanelJLists;

	private static final LibraryPanel instance = new LibraryPanel();

	/**
	 * Returns the only instance of LibraryPanel.
	 * 
	 * @return
	 */
	public static LibraryPanel getInstance() {
		return instance;
	}

	/**
	 * Creates a new LibraryPane with default filters, and configures its
	 * display. Also configures its listeners.
	 * 
	 */
	private LibraryPanel() {
		this.storyComponentPanelJLists = new ArrayList<StoryComponentPanelJList>();

		final SEModelObserver modelObserver;
		final LibraryManagerObserver libraryManagerObserver;
		final TranslatorObserver translatorObserver;

		final StoryComponentPanelJList causesList;
		final StoryComponentPanelJList effectsList;
		final StoryComponentPanelJList descriptionsList;
		final StoryComponentPanelJList controlsList;

		// Create the Tree with the root and the default filter
		causesList = new StoryComponentPanelJList(new CategoryFilter(
				Category.CAUSES));
		effectsList = new StoryComponentPanelJList(new CategoryFilter(
				Category.EFFECTS));
		descriptionsList = new StoryComponentPanelJList(new CategoryFilter(
				Category.DESCRIPTIONS));
		controlsList = new StoryComponentPanelJList(new CategoryFilter(
				Category.CONTROLS));

		modelObserver = new SEModelObserver() {
			/**
			 * This listener checks for when the model is changed. This usually
			 * happens when you load a model, or when you switch them by
			 * switching tabs.
			 */
			@Override
			public void modelChanged(SEModelEvent event) {
				if (event.getEventType() == SEModelEvent.Type.ACTIVATED)
					updateLists();
				else if (event.getEventType() == SEModelEvent.Type.REMOVED
						&& SEModelManager.getInstance().getActiveModel() == null) {
					updateLists();
				}
			}
		};
		translatorObserver = new TranslatorObserver() {

			@Override
			public void translatorLoaded(Translator newTranslator) {
				if (newTranslator == null)
					updateLists();
			}
		};

		this.storyComponentPanelJLists.add(causesList);
		this.storyComponentPanelJLists.add(effectsList);
		this.storyComponentPanelJLists.add(descriptionsList);
		this.storyComponentPanelJLists.add(controlsList);

		libraryManagerObserver = new LibraryManagerObserver() {
			/**
			 * Keep the display of the library up to date with the changes to
			 * Libraries. This listener is updates the library view when changes
			 * are made in the Library Editor.
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

		this.add("Causes", this.createTab(causesList));
		this.add("Effects", this.createTab(effectsList));
		this.add("Descriptions", this.createTab(descriptionsList));
		this.add("Controls", this.createTab(controlsList));

		LibraryManager.getInstance().addLibraryManagerObserver(this,
				libraryManagerObserver);
		SEModelManager.getInstance().addPatternModelObserver(this,
				modelObserver);
		TranslatorManager.getInstance().addTranslatorObserver(this,
				translatorObserver);
	}

	/**
	 * Creates a tab for a list with a search field and type filter.
	 * 
	 * @param list
	 * @return
	 */
	private JPanel createTab(final StoryComponentPanelJList list) {
		final JPanel tabPanel;
		final Timer searchFieldTimer;

		final JComponent filterPane;
		final JComponent searchFilterPane;
		final JTextField searchField;

		final TypeAction typeFilter;

		tabPanel = new JPanel();
		filterPane = new JPanel();
		searchFilterPane = new JPanel();
		searchField = ComponentFactory.buildJTextFieldWithTextBackground(20,
				"Library", "");

		typeFilter = new TypeAction();

		searchFieldTimer = new Timer(300, new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				list.updateFilter(new StoryComponentSearchFilter(searchField
						.getText()));
				updateList(list, (Timer) arg0.getSource());
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
				list.updateFilter(new StoryComponentSearchFilter(searchField
						.getText()));

				updateList(list, searchFieldTimer);
			}
		});

		TranslatorManager.getInstance().addTranslatorObserver(searchField,
				new TranslatorObserver() {
					@Override
					public void translatorLoaded(Translator newTranslator) {
						searchField.setEnabled(TranslatorManager.getInstance()
								.getActiveTranslator() != null);
					}
				});
		searchField.setEnabled(TranslatorManager.getInstance()
				.getActiveTranslator() != null);

		typeFilter.setAction(new Runnable() {
			@Override
			public void run() {
				list.updateFilter(new TypeFilter(typeFilter
						.getTypeSelectionDialogBuilder().getSelectedTypes()));

				updateList(list);
			}
		});

		filterPane.setBorder(BorderFactory.createTitledBorder(BorderFactory
				.createLineBorder(Color.gray), Il8nResources
				.getString("Search_Filter_"),
				TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.TOP, new Font(
						"SansSerif", Font.PLAIN, 12), Color.black));

		// SearchFilterPane
		searchFilterPane.add(searchField);
		searchFilterPane.add(new JButton(typeFilter));
		searchFilterPane.setLayout(new BoxLayout(searchFilterPane,
				BoxLayout.LINE_AXIS));
		searchFilterPane.setOpaque(false);

		// FilterPane Layout
		filterPane.setLayout(new BoxLayout(filterPane, BoxLayout.PAGE_AXIS));
		filterPane.add(searchFilterPane);
		filterPane.setMaximumSize(new Dimension(ScriptEaseUI.MAX_SCREEN_WIDTH,
				50));
		filterPane.setOpaque(false);

		tabPanel.setLayout(new BoxLayout(tabPanel, BoxLayout.PAGE_AXIS));
		tabPanel.add(filterPane);
		tabPanel.add(Box.createVerticalStrut(5));
		tabPanel.add(new JScrollPane(list));

		// Configure the displaying of the pane
		this.updateList(list);

		return tabPanel;
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
			list.addMouseListener(listener);
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

	/**
	 * Adds the story component to every list.
	 * 
	 * @param storyComponent
	 */
	private void addElement(StoryComponent storyComponent) {
		for (StoryComponentPanelJList list : this.storyComponentPanelJLists) {
			list.addStoryComponent(storyComponent);
		}
	}

	/**
	 * Removes the story component from every list.
	 * 
	 * @param storyComponent
	 */
	private void removeElement(StoryComponent storyComponent) {
		for (StoryComponentPanelJList list : this.storyComponentPanelJLists) {
			list.removeStoryComponent(storyComponent);
		}
	}

	/**
	 * Sorts story components in a list.
	 * 
	 * @return
	 */
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

					if (k1Widget == null)
						k1Widget = "";

					if (k2Widget == null)
						k2Widget = "";

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
	 * Updates a list and stops a timer at the same time.
	 * 
	 * @param list
	 * @param timer
	 */
	private void updateList(StoryComponentPanelJList list, Timer timer) {
		timer.stop();
		updateList(list);
	}

	/**
	 * Updates a list according to its filters and the active translators.
	 * 
	 * @param list
	 */
	private void updateList(StoryComponentPanelJList list) {
		final SEModel model;
		final Translator activeTranslator;
		final boolean hideInvisible;

		model = SEModelManager.getInstance().getActiveModel();
		activeTranslator = TranslatorManager.getInstance()
				.getActiveTranslator();

		// Show invisible components if we're editing a library model.
		if (model instanceof LibraryModel)
			hideInvisible = false;
		else
			hideInvisible = true;

		list.updateFilter(new TranslatorFilter(activeTranslator));
		list.updateFilter(new VisibilityFilter(hideInvisible));
		list.removeAllStoryComponents();

		if (activeTranslator != null && model != null) {
			final Collection<LibraryModel> libraries;

			libraries = activeTranslator.getLibraries();

			final int index = this.storyComponentPanelJLists.indexOf(list);

			for (LibraryModel libraryModel : libraries) {
				final List<StoryComponent> components;

				if (index == 0) {
					components = libraryModel.getCausesCategory().getChildren();
				} else if (index == 1) {
					components = libraryModel.getEffectsCategory()
							.getChildren();
				} else if (index == 2) {
					components = libraryModel.getDescriptionsCategory()
							.getChildren();
				} else if (index == 3) {
					components = libraryModel.getControllersCategory()
							.getChildren();
				} else {
					throw new IllegalArgumentException(
							"Invalid list in LibraryPanel: " + list);
				}

				Collections.sort(components,
						LibraryPanel.STORY_COMPONENT_COMPARATOR);

				list.addStoryComponents(components);
			}
		}
	}

	/**
	 * Updates the lists based on their filters. Works by removing and adding
	 * back all components in the list panes.
	 */
	private void updateLists() {
		for (StoryComponentPanelJList list : this.storyComponentPanelJLists) {
			this.updateList(list);
		}
	}
}
