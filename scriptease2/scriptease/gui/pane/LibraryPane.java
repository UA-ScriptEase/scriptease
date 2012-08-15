package scriptease.gui.pane;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import scriptease.controller.observer.LibraryManagerEvent;
import scriptease.controller.observer.LibraryManagerObserver;
import scriptease.controller.observer.TranslatorObserver;
import scriptease.gui.SETree.filters.CategoryFilter;
import scriptease.gui.SETree.filters.CategoryFilter.Category;
import scriptease.gui.SETree.filters.StoryComponentFilter;
import scriptease.gui.SETree.filters.StoryComponentSearchFilter;
import scriptease.gui.SETree.filters.TranslatorFilter;
import scriptease.gui.SETree.filters.TypeFilter;
import scriptease.gui.action.typemenus.TypeSelectionAction;
import scriptease.gui.internationalization.Il8nResources;
import scriptease.gui.storycomponentpanel.StoryComponentPanelList;
import scriptease.model.LibraryManager;
import scriptease.translator.Translator;
import scriptease.translator.TranslatorManager;

@SuppressWarnings("serial")
/**
 * TODO So this shouldn't exist. At all. It should be in the Panel Factory, but without the useless junk. 
 * 
 * LibraryPane represents the JPanel used for managing, filtering and choosing
 * Patterns from the loaded Libraries. It appears in the top left corner of
 * the main ScriptEase window.
 * 
 * @author mfchurch
 * @author kschenk
 */
public class LibraryPane extends JPanel implements LibraryManagerObserver,
		TranslatorObserver {

	private final JTabbedPane listTabs;

	private final StoryComponentPanelList causesList;
	private final StoryComponentPanelList effectsList;
	private final StoryComponentPanelList descriptionsList;
	private final StoryComponentPanelList foldersList;

	/**
	 * Creates a new LibraryPane with default filters, and configures its
	 * display.
	 * 
	 * @param showInvisible
	 *            Whether to show invisible story components or not.
	 */
	public LibraryPane(boolean showInvisible) {
		final LibraryManager libManager = LibraryManager.getInstance();

		final StoryComponentFilter causesFilter;
		final StoryComponentFilter effectsFilter;
		final StoryComponentFilter descriptionsFilter;
		final StoryComponentFilter foldersFilter;

		this.listTabs = new JTabbedPane();

		// Create the default filter.
		causesFilter = this.buildLibraryFilter(new CategoryFilter(
				Category.CAUSES));
		effectsFilter = this.buildLibraryFilter(new CategoryFilter(
				Category.EFFECTS));
		descriptionsFilter = this.buildLibraryFilter(new CategoryFilter(
				Category.DESCRIPTIONS));
		foldersFilter = this.buildLibraryFilter(new CategoryFilter(
				Category.FOLDERS));

		// Create the Tree with the root and the default filter
		this.causesList = new StoryComponentPanelList(causesFilter,
				showInvisible);
		this.effectsList = new StoryComponentPanelList(effectsFilter,
				showInvisible);
		this.descriptionsList = new StoryComponentPanelList(descriptionsFilter,
				showInvisible);
		this.foldersList = new StoryComponentPanelList(foldersFilter,
				showInvisible);

		// Configure the displaying of the pane
		this.configurePane();

		// Listen for changes to the Libraries and Translator
		libManager.addLibraryManagerListener(this);
		TranslatorManager.getInstance().addTranslatorObserver(this);
	}

	/**
	 * Builds a pane that allows users to drag across any pattern (including
	 * atoms) from any library into their Story.
	 * 
	 */
	private void configurePane() {
		final JComponent filterPane;
		final JComponent searchFilterPane;
		final JTextField searchField;
		final TypeSelectionAction typeFilter;
		final BoxLayout filterPaneLayout;
		final BoxLayout searchFilterPaneLayout;
		final SpringLayout pickerPaneLayout;

		filterPane = new JPanel();
		searchFilterPane = new JPanel();
		typeFilter = new TypeSelectionAction();
		filterPaneLayout = new BoxLayout(filterPane, BoxLayout.Y_AXIS);
		searchFilterPaneLayout = new BoxLayout(searchFilterPane,
				BoxLayout.X_AXIS);
		pickerPaneLayout = new SpringLayout();

		/*
		 * searchField = new FilterableSearchField(this.causesList, 20);
		 * searchField.addFilter(this.effectsList);
		 * searchField.addFilter(this.descriptionsList);
		 * searchField.addFilter(this.foldersList);
		 */

		searchField = new JTextField(20);

		searchField.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void insertUpdate(DocumentEvent e) {
				for (StoryComponentPanelList list : getListOfListTabs())
					list.updateFilter(new StoryComponentSearchFilter(
							searchField.getText()));
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				insertUpdate(e);
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
			}
		});

		/*
		 * XXX or how about when a search is run, we get the text, and update
		 * all the lists based on if the text even appears there. Like type
		 * selection filter.
		 * 
		 * Don't even need a separate class, likely.
		 */

		listTabs.add("Causes", causesList);
		listTabs.add("Effects", effectsList);
		listTabs.add("Descriptions", descriptionsList);
		listTabs.add("Folders", foldersList);

		filterPane.setBorder(BorderFactory.createTitledBorder(BorderFactory
				.createLineBorder(Color.gray), Il8nResources
				.getString("Search_Filter_"),
				TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.TOP, new Font(
						"SansSerif", Font.PLAIN, 12), Color.black));

		// Sets up the type filter.
		typeFilter.setAction(new Runnable() {
			@Override
			public void run() {
				for (StoryComponentPanelList list : getListOfListTabs())
					list.updateFilter(new TypeFilter(typeFilter
							.getTypeSelectionDialogBuilder().getSelectedTypes()));
			}
		});

		// SearchFilterPane
		searchFilterPane.add(searchField);
		searchFilterPane.add(new JButton(typeFilter));
		searchFilterPane.setLayout(searchFilterPaneLayout);

		// FilterPane Layout
		filterPane.setLayout(filterPaneLayout);
		filterPane.add(searchFilterPane);
		this.add(filterPane);

		this.add(listTabs);
		this.setPreferredSize(filterPane.getPreferredSize());

		// Spring filterPane
		// TODO Change this to group layout.
		pickerPaneLayout.putConstraint(SpringLayout.WEST, filterPane, 5,
				SpringLayout.WEST, this);
		pickerPaneLayout.putConstraint(SpringLayout.NORTH, filterPane, 5,
				SpringLayout.NORTH, this);
		pickerPaneLayout.putConstraint(SpringLayout.EAST, filterPane, -5,
				SpringLayout.EAST, this);
		// Spring pickerTree
		pickerPaneLayout.putConstraint(SpringLayout.WEST, listTabs, 5,
				SpringLayout.WEST, this);
		pickerPaneLayout.putConstraint(SpringLayout.EAST, listTabs, -5,
				SpringLayout.EAST, this);
		pickerPaneLayout.putConstraint(SpringLayout.SOUTH, listTabs, -5,
				SpringLayout.SOUTH, this);
		pickerPaneLayout.putConstraint(SpringLayout.NORTH, listTabs, 5,
				SpringLayout.SOUTH, filterPane);
		this.setLayout(pickerPaneLayout);
	}

	/**
	 * Adds a list mouse listener to each of the tabs.
	 * 
	 * @param listener
	 */
	public void addListMouseListener(MouseListener listener) {
		for (StoryComponentPanelList list : getListOfListTabs()) {
			list.addListMouseListener(listener);
		}
	}

	/**
	 * Returns all tabs that are StoryComponentPanelLists as a list, in the same
	 * order as they are in the tabs.
	 * 
	 * @return
	 */
	private List<StoryComponentPanelList> getListOfListTabs() {
		final List<StoryComponentPanelList> listList;

		listList = new ArrayList<StoryComponentPanelList>();

		for (Component listTab : this.listTabs.getComponents())
			if (listTab instanceof StoryComponentPanelList)
				listList.add((StoryComponentPanelList) listTab);

		return listList;
	}

	/**
	 * Creates the default Filter to be used by the LibraryTreeModel
	 * 
	 * @return
	 */
	private StoryComponentFilter buildLibraryFilter(CategoryFilter filter) {
		// Filter by translator
		filter.addRule(new TranslatorFilter(TranslatorManager.getInstance()
				.getActiveTranslator()));

		return filter;
	}

	/**
	 * Keep the display of the library up to date with the changes to Libraries
	 * 
	 * TODO This seems to be broken when two modules are loaded. Figure out why!
	 * Note: that may be fixed now that we're using lists and stuff.
	 * Note 2: this used to "invokelater", but that caused issues. Removed it.
	 */
	@Override
	public void modelChanged(final LibraryManagerEvent managerEvent) {
		for (StoryComponentPanelList list : getListOfListTabs())
			list.filterList();
	}

	/**
	 * Update the translator filter and redraw the list.
	 */
	@Override
	public void translatorLoaded(Translator newTranslator) {
		for (StoryComponentPanelList list : getListOfListTabs()) {
			list.updateFilter(new TranslatorFilter(newTranslator));
		}
	}
}
