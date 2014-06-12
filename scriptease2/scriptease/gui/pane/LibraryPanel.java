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
import scriptease.gui.storycomponentpanel.StoryComponentPanel;
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
import scriptease.util.ListOp;

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
	private final Collection<LibraryModel> libraries;

	private static final LibraryPanel mainLibraryPanel;

	static {
		mainLibraryPanel = new LibraryPanel();

		final SEModelObserver modelObserver;
		final TranslatorObserver translatorObserver;

		modelObserver = new SEModelObserver() {
			/**
			 * This listener checks for when the model is changed. This usually
			 * happens when you load a model, or when you switch them by
			 * switching tabs.
			 */
			@Override
			public void modelChanged(SEModelEvent event) {
				switch (event.getEventType()) {
				case ADDED:
					final SEModel model = event.getPatternModel();

					model.process(new ModelAdapter() {
						@Override
						public void processLibraryModel(LibraryModel library) {
							mainLibraryPanel.setLibraries(library);
						}

						@Override
						public void processStoryModel(StoryModel story) {
							mainLibraryPanel.setLibraries(story.getLibraries());

							story.addStoryModelObserver(new StoryModelAdapter() {
								@Override
								public void libraryAdded(LibraryModel library) {
									mainLibraryPanel.updateLists();
								}

								@Override
								public void libraryRemoved(LibraryModel library) {
									mainLibraryPanel.updateLists();
								}
							});
						}
					});
					break;
				case REMOVED:
					if (SEModelManager.getInstance().getActiveModel() != null)
						break;
				case ACTIVATED:
					mainLibraryPanel.updateLists();
				default:
					break;
				}
			}
		};
		translatorObserver = new TranslatorObserver() {

			@Override
			public void translatorLoaded(Translator newTranslator) {
				if (newTranslator == null) {
					mainLibraryPanel.updateLists();
				}
			}
		};

		SEModelManager.getInstance().addSEModelObserver(mainLibraryPanel,
				modelObserver);
		TranslatorManager.getInstance().addTranslatorObserver(mainLibraryPanel,
				translatorObserver);
	}

	/**
	 * Returns the only instance of LibraryPanel.
	 * 
	 * @return
	 */
	public static LibraryPanel getMainLibraryPanel() {
		return mainLibraryPanel;
	}

	/**
	 * Creates a new LibraryPane with default filters, and configures its
	 * display. Also configures its listeners.
	 * 
	 */
	public LibraryPanel() {
		this.storyComponentPanelJLists = new ArrayList<StoryComponentPanelJList>();
		this.libraries = new ArrayList<LibraryModel>();

		final StoryComponentPanelJList causesList;
		final StoryComponentPanelJList effectsList;
		final StoryComponentPanelJList descriptionsList;
		final StoryComponentPanelJList behavioursList;
		final StoryComponentPanelJList controlsList;
		final StoryComponentPanelJList blocksList;
		final StoryComponentPanelJList activitiesList;

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
		activitiesList = new StoryComponentPanelJList(new CategoryFilter(
				Category.ACTIVITIES));
		blocksList = new StoryComponentPanelJList(new CategoryFilter(
				Category.BLOCKS));

		this.storyComponentPanelJLists.add(causesList);
		this.storyComponentPanelJLists.add(effectsList);
		this.storyComponentPanelJLists.add(descriptionsList);
		this.storyComponentPanelJLists.add(behavioursList);
		this.storyComponentPanelJLists.add(controlsList);
		this.storyComponentPanelJLists.add(activitiesList);
		this.storyComponentPanelJLists.add(blocksList);

		this.add("Causes", this.createTab(causesList));
		this.add("Effects", this.createTab(effectsList));
		this.add("Descriptions", this.createTab(descriptionsList));
		this.add("Behaviours", this.createTab(behavioursList));
		this.add("Controls", this.createTab(controlsList));
		this.add("Activities", this.createTab(activitiesList));
		this.add("Blocks", this.createTab(blocksList));

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
	}

	public void clearLibraries() {
		this.libraries.clear();
		this.updateLists();
	}

	public Collection<LibraryModel> getLibraries() {
		return this.libraries;
	}

	public void setLibraries(LibraryModel... libraries) {
		this.setLibraries(ListOp.createList(libraries));
	}

	public void setLibraries(Collection<LibraryModel> libraries) {
		this.libraries.clear();
		this.libraries.addAll(libraries);

		final LibraryObserver libraryObserver;

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

				// TODO
				// remove no results panel.
			}
		};

		for (LibraryModel library : libraries) {
			library.addLibraryChangeListener(library, libraryObserver);
		}

		this.updateLists();
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

		final TypeAction typeAction;

		tabPanel = new JPanel();
		listScroll = new JScrollPane(list);
		filterPane = new JPanel();
		searchFilterPane = new JPanel();
		searchField = ComponentFactory.buildJTextFieldWithTextBackground(20,
				"Search Library", "");

		typeAction = new TypeAction();

		// 300 is the best time. Trust me, I've tried different ones many times
		searchFieldTimer = new Timer(300, new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				list.updateFilter(new StoryComponentSearchFilter(searchField
						.getText()));

				list.removeAllStoryComponents();

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

				list.removeAllStoryComponents();

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

		typeAction.setAction(new Runnable() {
			@Override
			public void run() {
				list.updateFilter(new TypeFilter(typeAction.getSelectedTypes()));

				updateList(list);
			}
		});

		// SearchFilterPane
		searchFilterPane.add(searchField);
		searchFilterPane.add(ComponentFactory.buildFlatButton(typeAction));
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
				.createLineBorder(ScriptEaseUI.SE_BLACK));

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

	public void addStoryComponentPanelJListObserver(Object object,
			StoryComponentPanelJListObserver observer) {
		for (StoryComponentPanelJList list : this.storyComponentPanelJLists) {
			list.addObserver(object, observer);
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
	private void navigateToComponent(StoryComponent component) {
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
			this.navigateToComponent(storyComponent);
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

					String k1Widget = k1Library.getType(k1Type).getWidgetName();
					String k2Widget = k2Library.getType(k2Type).getWidgetName();

					if (k1Widget == null || k1Widget.isEmpty()) {
						if (!k1Library.getType(k1Type).getEnumMap().isEmpty()) {
							k1Widget = GameType.DEFAULT_LIST_WIDGET;
						}
					}
					if (k2Widget == null || k2Widget.isEmpty()) {
						if (!k2Library.getType(k2Type).getEnumMap().isEmpty()) {
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

		final Collection<LibraryModel> libraries = new ArrayList<LibraryModel>();
		final boolean hideInvisible;
		final Translator translator;

		if (model != null) {
			translator = model.getTranslator();
			// Show invisible components if we're editing a library model.
			if (model instanceof LibraryModel) {
				hideInvisible = false;
				libraries.add(LibraryModel.getCommonLibrary());
				libraries.add((LibraryModel) model);
				// Adds the default library for use but not editing.
				libraries.add(translator.getLibrary());
			} else {
				hideInvisible = true;
				libraries.addAll(((StoryModel) model).getLibraries());
			}

		} else if (!this.libraries.isEmpty()) {
			hideInvisible = false;

			// TODO can we just use the this.libraries instead of the collection
			// in here?

			translator = ListOp.head(this.libraries).getTranslator();

			libraries.addAll(this.libraries);
			// TODO libraries here... Maybe change the libraries up there, too.
		} else
			return;

		list.updateFilter(new TranslatorFilter(translator));
		list.updateFilter(new VisibilityFilter(hideInvisible));

		final int index = this.storyComponentPanelJLists.indexOf(list);

		for (LibraryModel libraryModel : libraries) {
			final List<StoryComponent> components;

			if (index == 0) {
				components = libraryModel.getCausesCategory().getChildren();
			} else if (index == 1) {
				components = libraryModel.getEffectsCategory().getChildren();
			} else if (index == 2) {
				components = libraryModel.getDescriptionsCategory()
						.getChildren();
			} else if (index == 3) {
				components = libraryModel.getBehavioursCategory().getChildren();
			} else if (index == 4) {
				components = libraryModel.getControllersCategory()
						.getChildren();
			} else if (index == 5) {
				components = libraryModel.getActivitysCategory().getChildren();
			} else if (index == 6) {
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

	/**
	 * Updates the lists based on their filters. Works by removing and adding
	 * back all components in the list panes.
	 */
	private void updateLists() {
		for (StoryComponentPanelJList list : this.storyComponentPanelJLists) {
			list.removeAllStoryComponents();
			this.updateList(list);
		}
	}

	/**
	 * Returns all the story components that are selected
	 * 
	 * @return panels
	 */
	public Collection<StoryComponentPanel> getSelected() {
		final Collection<StoryComponentPanel> panels = new ArrayList<StoryComponentPanel>();
		for (StoryComponentPanelJList list : this.storyComponentPanelJLists) {
			Object[] objects = list.getSelectedValues();
			for (int i = 0; i < objects.length; i++) {
				final Object obj = objects[i];

				if (obj instanceof StoryComponentPanel)
					panels.add((StoryComponentPanel) obj);
			}
		}
		return panels;
	}

}
