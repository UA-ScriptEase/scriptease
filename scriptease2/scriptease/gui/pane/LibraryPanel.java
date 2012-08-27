package scriptease.gui.pane;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseListener;
import java.util.ArrayList;
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

import scriptease.controller.observer.LibraryManagerEvent;
import scriptease.controller.observer.LibraryManagerObserver;
import scriptease.controller.observer.PatternModelEvent;
import scriptease.controller.observer.PatternModelObserver;
import scriptease.gui.SETree.filters.CategoryFilter;
import scriptease.gui.SETree.filters.CategoryFilter.Category;
import scriptease.gui.SETree.filters.StoryComponentSearchFilter;
import scriptease.gui.SETree.filters.TranslatorFilter;
import scriptease.gui.SETree.filters.TypeFilter;
import scriptease.gui.SETree.filters.VisibilityFilter;
import scriptease.gui.action.typemenus.TypeSelectionAction;
import scriptease.gui.internationalization.Il8nResources;
import scriptease.gui.storycomponentpanel.StoryComponentPanelJList;
import scriptease.model.LibraryManager;
import scriptease.model.LibraryModel;
import scriptease.model.PatternModel;
import scriptease.model.PatternModelManager;
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
public class LibraryPanel extends JPanel implements LibraryManagerObserver,
		PatternModelObserver {

	private final JTabbedPane listTabs;

	private final List<StoryComponentPanelJList> storyComponentPanelJLists;

	/**
	 * Creates a new LibraryPane with default filters, and configures its
	 * display.
	 * 
	 * @param hideInvisible
	 *            Whether to show invisible story components or not.
	 */
	public LibraryPanel(boolean hideInvisible) {
		this.storyComponentPanelJLists = new ArrayList<StoryComponentPanelJList>();

		final JComponent filterPane;
		final JComponent searchFilterPane;
		final JTextField searchField;

		final TypeSelectionAction typeFilter;

		final StoryComponentPanelJList causesList;
		final StoryComponentPanelJList effectsList;
		final StoryComponentPanelJList descriptionsList;
		final StoryComponentPanelJList foldersList;

		listTabs = new JTabbedPane();
		filterPane = new JPanel();
		searchFilterPane = new JPanel();
		searchField = new JTextField(20);

		typeFilter = new TypeSelectionAction();

		// Create the Tree with the root and the default filter
		causesList = new StoryComponentPanelJList(new CategoryFilter(
				Category.CAUSES), hideInvisible);
		effectsList = new StoryComponentPanelJList(new CategoryFilter(
				Category.EFFECTS), hideInvisible);
		descriptionsList = new StoryComponentPanelJList(new CategoryFilter(
				Category.DESCRIPTIONS), hideInvisible);
		foldersList = new StoryComponentPanelJList(new CategoryFilter(
				Category.FOLDERS), hideInvisible);

		this.storyComponentPanelJLists.add(causesList);
		this.storyComponentPanelJLists.add(effectsList);
		this.storyComponentPanelJLists.add(descriptionsList);
		this.storyComponentPanelJLists.add(foldersList);

		// Set up the listeners
		searchField.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				for (StoryComponentPanelJList list : storyComponentPanelJLists)
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
				for (StoryComponentPanelJList list : storyComponentPanelJLists)
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
		listTabs.add("Folders", new JScrollPane(foldersList));

		// SearchFilterPane
		searchFilterPane.add(searchField);
		searchFilterPane.add(new JButton(typeFilter));
		searchFilterPane.setLayout(new BoxLayout(searchFilterPane,
				BoxLayout.LINE_AXIS));

		// FilterPane Layout
		filterPane.setLayout(new BoxLayout(filterPane, BoxLayout.PAGE_AXIS));
		filterPane.add(searchFilterPane);
		filterPane.setMaximumSize(new Dimension(2400, 50));

		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		this.add(filterPane);
		this.add(Box.createVerticalStrut(5));
		this.add(listTabs);

		// Configure the displaying of the pane
		this.updateLists();

		LibraryManager.getInstance().addLibraryManagerObserver(this);
		PatternModelManager.getInstance().addPatternModelPoolObserver(this);
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
	 * Updates the lists based on their filters. Works by removing and adding
	 * back all components in the list panes.
	 */
	private void updateLists() {
		for (StoryComponentPanelJList list : this.storyComponentPanelJLists) {
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

			list.updateFilter(new TranslatorFilter(TranslatorManager
					.getInstance().getActiveTranslator()));
			list.updateFilter(new VisibilityFilter(hideInvisible));

			list.removeAllStoryComponents();

			if (activeTranslator != null)
				for (LibraryModel libraryModel : TranslatorManager
						.getInstance().getActiveTranslator().getLibraries()) {
					list.addStoryComponents(libraryModel
							.getMainStoryComponents());
				}
			
		}
	}

	/**
	 * This listener checks for when the model is changed. This usually happens
	 * when you load a model, or when you switch them by switching tabs.
	 */
	@Override
	public void modelChanged(PatternModelEvent event) {
		if (event.getEventType() == PatternModelEvent.PATTERN_MODEL_ACTIVATED)
			updateLists();
	}

	/**
	 * Keep the display of the library up to date with the changes to Libraries.
	 * This listener is important for the Story Component Builder, so that
	 * changes made there will apply to the library view as well.
	 */
	@Override
	public void modelChanged(LibraryManagerEvent event) {
		if (event.getEventType() == LibraryManagerEvent.LIBRARYMODEL_CHANGED)
			updateLists();
	}
}
