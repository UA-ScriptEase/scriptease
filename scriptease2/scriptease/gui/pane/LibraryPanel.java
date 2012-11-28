package scriptease.gui.pane;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collection;
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
import scriptease.translator.Translator;
import scriptease.translator.TranslatorManager;

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
	private final LibraryManagerObserver libraryManagerObserver;
	private final PatternModelObserver modelObserver;
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

		this.modelObserver = new PatternModelObserver() {
			/**
			 * This listener checks for when the model is changed. This usually
			 * happens when you load a model, or when you switch them by
			 * switching tabs.
			 */
			@Override
			public void modelChanged(PatternModelEvent event) {
				if (event.getEventType() == PatternModelEvent.PATTERN_MODEL_ACTIVATED)
					updateLists();
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

		this.storyComponentPanelJLists.add(causesList);
		this.storyComponentPanelJLists.add(effectsList);
		this.storyComponentPanelJLists.add(descriptionsList);
		this.storyComponentPanelJLists.add(controlsList);

		// Set up the listeners
		searchField.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				for (StoryComponentPanelJList list : LibraryPanel.this.storyComponentPanelJLists)
					list.updateFilter(new StoryComponentSearchFilter(
							searchField.getText()));
				updateLists();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				insertUpdate(e);
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
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
		PatternModelManager.getInstance().addPatternModelObserver(
				this.modelObserver);
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
				final Collection<LibraryModel> libraries = activeTranslator
						.getLibraries();
				for (LibraryModel libraryModel : libraries) {
					list.addStoryComponents(libraryModel
							.getMainStoryComponents());
				}
			}
		}
	}
}
