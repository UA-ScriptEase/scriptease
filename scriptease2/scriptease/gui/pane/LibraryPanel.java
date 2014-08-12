package scriptease.gui.pane;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import scriptease.ScriptEase;
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
import scriptease.gui.filters.StoryComponentFilter;
import scriptease.gui.filters.StoryComponentSearchFilter;
import scriptease.gui.filters.TranslatorFilter;
import scriptease.gui.filters.TypeFilter;
import scriptease.gui.filters.VisibilityFilter;
import scriptease.gui.storycomponentpanel.StoryComponentPanel;
import scriptease.gui.storycomponentpanel.StoryComponentPanelJList;
import scriptease.gui.ui.ScriptEaseUI;
import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.model.semodel.SEModel;
import scriptease.model.semodel.SEModelManager;
import scriptease.model.semodel.StoryModel;
import scriptease.model.semodel.librarymodel.LibraryModel;
import scriptease.translator.Translator;
import scriptease.translator.TranslatorManager;
import scriptease.translator.io.model.GameType;
import scriptease.util.GUIOp;
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

	private final Map<StoryComponent.Type, StoryComponentPanelJList> categoryLists;
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
				case ACTIVATED:
					final SEModel model = event.getPatternModel();

					model.process(new ModelAdapter() {
						@Override
						public void processLibraryModel(LibraryModel library) {
							mainLibraryPanel.setLibraries(ListOp.createList(
									LibraryModel.getCommonLibrary(),
									(LibraryModel) model,
									// Adds the default library for use but not
									// editing.
									model.getTranslator().getLibrary()));
						}

						@Override
						public void processStoryModel(final StoryModel story) {
							mainLibraryPanel.setLibraries(story.getLibraries());

							story.addStoryModelObserver(new StoryModelAdapter() {
								@Override
								public void libraryAdded(LibraryModel library) {
									mainLibraryPanel.setLibraries(story
											.getLibraries());
								}

								@Override
								public void libraryRemoved(LibraryModel library) {
									mainLibraryPanel.setLibraries(story
											.getLibraries());
								}
							});
						}

						@Override
						public void processTranslator(Translator model) {
							// We don't do anything for this because it gets
							// hidden.
						}
					});
					break;
				case REMOVED:
					if (SEModelManager.getInstance().getActiveModel() != null)
						break;
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
		this.categoryLists = new LinkedHashMap<StoryComponent.Type, StoryComponentPanelJList>();
		this.libraries = new ArrayList<LibraryModel>();

		final StoryComponentPanelJList causesList;
		final StoryComponentPanelJList effectsList;
		final StoryComponentPanelJList descriptionsList;

		final StoryComponentPanelJList controlsList;
		final StoryComponentPanelJList blocksList;
		final StoryComponentPanelJList activitiesList;
		final StoryComponentPanelJList behavioursList;

		// Create the Tree with the root and the default filter
		causesList = new StoryComponentPanelJList(new CategoryFilter(
				StoryComponent.Type.CAUSE));
		effectsList = new StoryComponentPanelJList(new CategoryFilter(
				StoryComponent.Type.EFFECT));
		descriptionsList = new StoryComponentPanelJList(new CategoryFilter(
				StoryComponent.Type.DESCRIPTION));
		controlsList = new StoryComponentPanelJList(new CategoryFilter(
				StoryComponent.Type.CONTROL));
		activitiesList = new StoryComponentPanelJList(new CategoryFilter(
				StoryComponent.Type.ACTIVITY));
		blocksList = new StoryComponentPanelJList(new CategoryFilter(
				StoryComponent.Type.BLOCK));
		behavioursList = new StoryComponentPanelJList(new CategoryFilter(
				StoryComponent.Type.BEHAVIOUR));

		this.categoryLists.put(StoryComponent.Type.CAUSE, causesList);
		this.categoryLists.put(StoryComponent.Type.EFFECT, effectsList);
		this.categoryLists.put(StoryComponent.Type.DESCRIPTION,
				descriptionsList);
		this.categoryLists.put(StoryComponent.Type.ACTIVITY, activitiesList);
		if (!ScriptEase.is250Release())
			this.categoryLists.put(StoryComponent.Type.BEHAVIOUR,
					behavioursList);
		this.categoryLists.put(StoryComponent.Type.CONTROL, controlsList);
		this.categoryLists.put(StoryComponent.Type.BLOCK, blocksList);

		for (StoryComponent.Type type : this.categoryLists.keySet()) {
			this.add(type.getReadableNamePlural(), this.createTab(type));
		}

		// Set up Hotkeys
		this.setMnemonicAt(0, KeyEvent.VK_1);
		this.setMnemonicAt(1, KeyEvent.VK_2);
		this.setMnemonicAt(2, KeyEvent.VK_3);
		this.setMnemonicAt(3, KeyEvent.VK_4);
		this.setMnemonicAt(4, KeyEvent.VK_5);
		this.setMnemonicAt(5, KeyEvent.VK_6);
		if (!ScriptEase.is250Release())
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
			}
		};

		for (LibraryModel library : libraries) {
			library.addLibraryChangeListener(library, libraryObserver);
		}

		// This makes sure the library pane actually loads. Otherwise it
		// sometimes refreshes to early or something.
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				updateLists();
			}
		});
	}

	/**
	 * Creates a tab for a list with a search field and type filter.
	 * 
	 * @param list
	 * @return
	 */
	private JPanel createTab(final StoryComponent.Type type) {
		final JPanel tabPanel;
		final JScrollPane listScroll;
		final StoryComponentPanelJList list;

		final Timer searchFieldTimer;

		final JComponent filterPane;
		final JComponent searchFilterPane;
		final JTextField searchField;

		final TypeAction typeAction;

		list = this.categoryLists.get(type);
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

				updateList(type, (Timer) arg0.getSource());
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

				updateList(type, searchFieldTimer);
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
							final boolean enable = model != null
									&& model.getTranslator() != null;

							searchField.setEnabled(enable);
						}
					}
				});

		typeAction.setAction(new Runnable() {
			@Override
			public void run() {
				list.updateFilter(new TypeFilter(typeAction.getSelectedTypes()));

				list.removeAllStoryComponents();

				updateList(type);
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
		this.updateList(type);

		return tabPanel;
	}

	public void addStoryComponentPanelJListObserver(
			StoryComponentPanelJListObserver observer) {
		for (StoryComponentPanelJList list : this.categoryLists.values()) {
			list.addObserver(observer);
		}
	}

	public void addStoryComponentPanelJListObserver(Object object,
			StoryComponentPanelJListObserver observer) {
		for (StoryComponentPanelJList list : this.categoryLists.values()) {
			list.addObserver(object, observer);
		}
	}

	public void updateFilter(StoryComponentFilter filter) {
		for (Entry<StoryComponent.Type, StoryComponentPanelJList> entry : this.categoryLists
				.entrySet()) {
			entry.getValue().updateFilter(filter);
			entry.getValue().removeAllStoryComponents();

			updateList(entry.getKey());
		}
	}

	/**
	 * Finds and updates the StoryComponentPanel of the changed StoryComponent
	 * in each StoryComponentPanelJList
	 * 
	 * @param changed
	 */
	private void updateElement(StoryComponent changed) {
		final StoryComponentPanelJList list = this.getList(changed);

		if (list != null)
			list.updateStoryComponentPanel(changed);
	}

	/**
	 * Navigates to the StoryComponentPanelJList tab, selects the component in
	 * the list, and scrolls to it's position.
	 * 
	 * @param component
	 */
	private void navigateToComponent(StoryComponent component) {
		final StoryComponentPanelJList list = this.getList(component);

		int index = 0;
		for (StoryComponentPanelJList list2 : this.categoryLists.values()) {
			if (list2 == list)
				break;
			index++;
		}

		final int listIndex = list.getIndexOfStoryComponent(component);

		this.setSelectedIndex(index);
		list.setSelectedIndex(listIndex);
		list.ensureIndexIsVisible(listIndex);
	}

	private StoryComponentPanelJList getList(StoryComponent component) {
		return this.categoryLists.get(StoryComponent.Type.getType(component));
	}

	/**
	 * Adds the story component to every list.
	 * 
	 * @param storyComponent
	 */
	private void addElement(StoryComponent component) {
		this.getList(component).addStoryComponent(component);
		this.navigateToComponent(component);
	}

	/**
	 * Removes the story component from every list.
	 * 
	 * @param storyComponent
	 */
	private void removeElement(StoryComponent component) {
		final StoryComponentPanelJList list = this.getList(component);
		if (list != null)
			list.removeStoryComponent(component);
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
	private void updateList(StoryComponent.Type type, Timer timer) {
		timer.stop();
		updateList(type);
	}

	/**
	 * Updates a list according to its filters and the active translators.
	 * 
	 * @param list
	 */
	private void updateList(StoryComponent.Type type) {
		final SEModel model = SEModelManager.getInstance().getActiveModel();
		final StoryComponentPanelJList list = this.categoryLists.get(type);

		final boolean hideInvisible;
		final Translator translator;

		if (model != null) {
			translator = model.getTranslator();
			// Show invisible components if we're editing a library model.
			if (model instanceof LibraryModel || model instanceof Translator) {
				hideInvisible = false;
			} else if (model instanceof StoryModel) {
				hideInvisible = true;
			} else
				throw new IllegalArgumentException(
						"Undefined behaviour for model " + model + " of class "
								+ model.getClass());
		} else
			return;

		list.updateFilter(new TranslatorFilter(translator));
		list.updateFilter(new VisibilityFilter(hideInvisible));

		for (LibraryModel libraryModel : this.libraries) {
			final List<StoryComponent> components;

			if (type == StoryComponent.Type.CAUSE) {
				components = libraryModel.getCausesCategory().getChildren();
			} else if (type == StoryComponent.Type.EFFECT) {
				components = libraryModel.getEffectsCategory().getChildren();
			} else if (type == StoryComponent.Type.DESCRIPTION) {
				components = libraryModel.getDescriptionsCategory()
						.getChildren();
			} else if (type == StoryComponent.Type.BEHAVIOUR) {
				components = libraryModel.getBehavioursCategory().getChildren();
			} else if (type == StoryComponent.Type.CONTROL) {
				components = libraryModel.getControllersCategory()
						.getChildren();
			} else if (type == StoryComponent.Type.ACTIVITY) {
				components = libraryModel.getActivitysCategory().getChildren();
			} else if (type == StoryComponent.Type.BLOCK) {
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
		for (Entry<StoryComponent.Type, StoryComponentPanelJList> entry : this.categoryLists
				.entrySet()) {
			entry.getValue().removeAllStoryComponents();
			this.updateList(entry.getKey());
		}
	}

	/**
	 * Returns all the story components that are selected
	 * 
	 * @return panels
	 */
	public Collection<StoryComponentPanel> getSelected() {
		final Collection<StoryComponentPanel> panels = new ArrayList<StoryComponentPanel>();
		for (StoryComponentPanelJList list : this.categoryLists.values()) {
			Object[] objects = list.getSelectedValues();
			for (int i = 0; i < objects.length; i++) {
				final Object obj = objects[i];

				if (obj instanceof StoryComponentPanel)
					panels.add((StoryComponentPanel) obj);
			}
		}
		return panels;
	}

	/**
	 * Returns all the story components that are selected in the selected tab
	 * 
	 * @return panels
	 */
	public Collection<StoryComponentPanel> getSelectedInActiveTab() {
		final Collection<StoryComponentPanel> panels = new ArrayList<StoryComponentPanel>();
		for (StoryComponentPanelJList list : this.categoryLists.values()) {
			for (Component comp : GUIOp.getContainerComponents((Container) this
					.getSelectedComponent())) {
				if (comp == list) {
					Object[] objects = list.getSelectedValues();
					for (int i = 0; i < objects.length; i++) {
						final Object obj = objects[i];

						if (obj instanceof StoryComponentPanel)
							panels.add((StoryComponentPanel) obj);
					}
					break;
				}
			}
		}
		return panels;
	}
}
