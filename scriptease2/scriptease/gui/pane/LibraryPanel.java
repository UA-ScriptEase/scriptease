package scriptease.gui.pane;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import scriptease.controller.ModelAdapter;
import scriptease.controller.observer.SEModelEvent;
import scriptease.controller.observer.SEModelObserver;
import scriptease.controller.observer.StoryComponentPanelJListObserver;
import scriptease.controller.observer.StoryModelAdapter;
import scriptease.controller.observer.TranslatorObserver;
import scriptease.controller.observer.library.LibraryEvent;
import scriptease.controller.observer.library.LibraryObserver;
import scriptease.gui.action.typemenus.TypeAction;
import scriptease.gui.component.ComponentFactory;
import scriptease.gui.filters.CategoryFilter;
import scriptease.gui.filters.CategoryFilter.Category;
import scriptease.gui.filters.StoryComponentSearchFilter;
import scriptease.gui.filters.TranslatorFilter;
import scriptease.gui.filters.TypeFilter;
import scriptease.gui.filters.VisibilityFilter;
import scriptease.gui.storycomponentpanel.StoryComponentPanelJList;
import scriptease.gui.ui.ScriptEaseUI;
import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.model.complex.CauseIt;
import scriptease.model.complex.ScriptIt;
import scriptease.model.semodel.SEModel;
import scriptease.model.semodel.SEModelManager;
import scriptease.model.semodel.StoryModel;
import scriptease.model.semodel.librarymodel.LibraryModel;
import scriptease.translator.Translator;
import scriptease.translator.TranslatorManager;
import scriptease.translator.io.model.GameType;

/**
 * LibraryPane represents the JPanel used for managing, filtering and choosing
 * Patterns from the loaded Libraries. It appears in the top left corner of the
 * main ScriptEase window.
 * 
 * @author mfchurch
 * @author kschenk
 * @author jyuen
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
		final LibraryObserver libraryObserver;
		final TranslatorObserver translatorObserver;

		final StoryComponentPanelJList causesList;
		final StoryComponentPanelJList effectsList;
		final StoryComponentPanelJList descriptionsList;
		final StoryComponentPanelJList behavioursList;
		final StoryComponentPanelJList controlsList;
		final StoryComponentPanelJList blocksList;
		final StoryComponentPanelJList containersList;

		// Create the Tree with the root and the default filter
		causesList = new StoryComponentPanelJList(new CategoryFilter(
				Category.CAUSES));
		effectsList = new StoryComponentPanelJList(new CategoryFilter(
				Category.EFFECTS));
		descriptionsList = new StoryComponentPanelJList(new CategoryFilter(
				Category.DESCRIPTIONS));
		behavioursList = new StoryComponentPanelJList(new CategoryFilter(
				Category.BEHAVIOURS));
		controlsList = new StoryComponentPanelJList(new CategoryFilter(
				Category.CONTROLS));
		blocksList = new StoryComponentPanelJList(new CategoryFilter(
				Category.BLOCKS));
		containersList = new StoryComponentPanelJList(new CategoryFilter(
				Category.CONTAINERS));

		libraryObserver = new LibraryObserver() {
			/**
			 * Keep the display of the library up to date with the changes to
			 * Libraries. This listener is updates the library view when changes
			 * are made in the Library Editor.
			 */
			@Override
			public void modelChanged(LibraryModel changed, LibraryEvent event) {
				final StoryComponent storyComponent = event.getSource();

				if (event.getEventType() == LibraryEvent.Type.CHANGE) {
					updateElement(storyComponent);
				} else if (event.getEventType() == LibraryEvent.Type.ADDITION) {
					addElement(storyComponent);
				} else if (event.getEventType() == LibraryEvent.Type.REMOVAL) {
					removeElement(storyComponent);
				}
			}
		};

		modelObserver = new SEModelObserver() {
			/**
			 * This listener checks for when the model is changed. This usually
			 * happens when you load a model, or when you switch them by
			 * switching tabs.
			 */
			@Override
			public void modelChanged(SEModelEvent event) {
				final SEModel model = event.getPatternModel();

				if (event.getEventType() == SEModelEvent.Type.ADDED) {
					model.process(new ModelAdapter() {
						@Override
						public void processLibraryModel(LibraryModel library) {
							library.addLibraryChangeListener(library,
									libraryObserver);
						}

						@Override
						public void processStoryModel(StoryModel story) {
							for (LibraryModel library : story.getLibraries()) {
								library.addLibraryChangeListener(library,
										libraryObserver);
							}

							story.addStoryModelObserver(new StoryModelAdapter() {
								@Override
								public void libraryAdded(LibraryModel library) {
									LibraryPanel.this.updateLists();
								}

								@Override
								public void libraryRemoved(LibraryModel library) {
									LibraryPanel.this.updateLists();
								}
							});
						}
					});
				} else if (event.getEventType() == SEModelEvent.Type.ACTIVATED)
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
				if (newTranslator == null) {
					updateLists();
				}
			}
		};

		this.storyComponentPanelJLists.add(causesList);
		this.storyComponentPanelJLists.add(effectsList);
		this.storyComponentPanelJLists.add(descriptionsList);
		this.storyComponentPanelJLists.add(behavioursList);
		this.storyComponentPanelJLists.add(controlsList);
		this.storyComponentPanelJLists.add(blocksList);
		this.storyComponentPanelJLists.add(containersList);

		this.add("Causes", this.createTab(causesList));
		this.add("Effects", this.createTab(effectsList));
		this.add("Descriptions", this.createTab(descriptionsList));
		this.add("Behaviours", this.createTab(behavioursList));
		this.add("Controls", this.createTab(controlsList));
		this.add("Blocks", this.createTab(blocksList));
		this.add("Functions", this.createTab(containersList));

		// Set up Hotkeys
		this.setMnemonicAt(0, KeyEvent.VK_1);
		this.setMnemonicAt(1, KeyEvent.VK_2);
		this.setMnemonicAt(2, KeyEvent.VK_3);
		this.setMnemonicAt(3, KeyEvent.VK_4);
		this.setMnemonicAt(4, KeyEvent.VK_5);
		this.setMnemonicAt(5, KeyEvent.VK_6);
		this.setMnemonicAt(6, KeyEvent.VK_7);

		this.setUI(ComponentFactory.buildFlatTabUI());

		this.setBackground(ScriptEaseUI.SECONDARY_UI);
		SEModelManager.getInstance().addSEModelObserver(this, modelObserver);
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
		final JScrollPane listScroll;

		final Timer searchFieldTimer;

		final JComponent filterPane;
		final JComponent searchFilterPane;
		final JTextField searchField;

		final TypeAction typeFilter;

		tabPanel = new JPanel();
		listScroll = new JScrollPane(list);
		filterPane = new JPanel();
		searchFilterPane = new JPanel();
		searchField = ComponentFactory.buildJTextFieldWithTextBackground(20,
				"Search Library", "");

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

		searchField.setEnabled(TranslatorManager.getInstance()
				.getActiveTranslator() != null);

		SEModelManager.getInstance().addSEModelObserver(searchField,
				new SEModelObserver() {
					@Override
					public void modelChanged(SEModelEvent event) {
						if (event.getEventType() == SEModelEvent.Type.ACTIVATED) {
							final SEModel model = event.getPatternModel();

							searchField.setEnabled(model != null
									&& model.getTranslator() != null);
						}
					}
				});

		typeFilter.setAction(new Runnable() {
			@Override
			public void run() {
				list.updateFilter(new TypeFilter(typeFilter
						.getTypeSelectionDialogBuilder()
						.getSelectedTypeKeywords()));

				updateList(list);
			}
		});

		// SearchFilterPane
		searchFilterPane.add(searchField);
		searchFilterPane.add(ComponentFactory.buildFlatButton(typeFilter));
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
		listScroll.setBorder(BorderFactory.createEmptyBorder());
		tabPanel.add(listScroll);
		tabPanel.setBorder(BorderFactory
				.createLineBorder(ScriptEaseUI.BUTTON_BLACK));

		// Configure the displaying of the pane
		this.updateList(list);

		return tabPanel;
	}

	public void addStoryComponentPanelJListObserver(
			StoryComponentPanelJListObserver observer) {
		for (StoryComponentPanelJList list : this.storyComponentPanelJLists) {
			list.addObserver(observer);
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
	 * Navigates to the StoryComponentPanelJList tab, selects the component in
	 * the list, and scrolls to it's position.
	 * 
	 * @param component
	 */
	public void navigateToComponent(StoryComponent component) {
		for (final StoryComponentPanelJList list : this.storyComponentPanelJLists) {
			final int listIndex = list.getIndexOfStoryComponent(component);
			if (listIndex != -1) {
				final int tabIndex = this.storyComponentPanelJLists
						.indexOf(list);
				this.setSelectedIndex(tabIndex);
				list.setSelectedIndex(listIndex);
				list.ensureIndexIsVisible(listIndex);
				break;
			}
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
					final LibraryModel k1Library = k1.getLibrary();
					final LibraryModel k2Library = k2.getLibrary();

					String k1Widget = k1Library.getTypeWidgetName(k1Type);
					String k2Widget = k2Library.getTypeWidgetName(k2Type);

					if (k1Widget == null || k1Widget.isEmpty()) {
						if (!k1Library.getTypeEnumeratedValues(k1Type)
								.isEmpty()) {
							k1Widget = GameType.DEFAULT_LIST_WIDGET;
						}
					}
					if (k2Widget == null || k2Widget.isEmpty()) {
						if (!k2Library.getTypeEnumeratedValues(k2Type)
								.isEmpty()) {
							k2Widget = GameType.DEFAULT_LIST_WIDGET;
						}
					}

					if (k1Widget == null)
						k1Widget = "";

					if (k2Widget == null)
						k2Widget = "";

					compare = k1Widget.compareTo(k2Widget);

				} else if (c1 instanceof CauseIt && c2 instanceof CauseIt) {
					final ScriptIt s1 = (ScriptIt) c1;
					final ScriptIt s2 = (ScriptIt) c2;

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
		final SEModel model = SEModelManager.getInstance().getActiveModel();

		list.removeAllStoryComponents();

		if (model != null
				&& TranslatorManager.getInstance().getActiveTranslator() != null) {
			final Translator translator = model.getTranslator();

			final Collection<LibraryModel> libraries = new ArrayList<LibraryModel>();
			final boolean hideInvisible;

			// Show invisible components if we're editing a library model.
			if (model instanceof LibraryModel) {
				hideInvisible = false;
				libraries.add((LibraryModel) model);
			} else {
				hideInvisible = true;
				libraries.addAll(((StoryModel) model).getLibraries());
			}

			list.updateFilter(new TranslatorFilter(translator));
			list.updateFilter(new VisibilityFilter(hideInvisible));

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
					components = libraryModel.getBehavioursCategory()
							.getChildren();
				} else if (index == 4) {
					components = libraryModel.getControllersCategory()
							.getChildren();
				} else if (index == 5) {
					components = libraryModel.getControllersCategory()
							.getChildren();
				} else if (index == 6) {
					components = libraryModel.getFunctionsCategory()
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
