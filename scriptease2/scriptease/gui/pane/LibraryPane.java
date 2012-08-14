package scriptease.gui.pane;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.event.TreeSelectionListener;

import scriptease.controller.observer.LibraryManagerEvent;
import scriptease.controller.observer.LibraryManagerObserver;
import scriptease.controller.observer.TranslatorObserver;
import scriptease.gui.SETree.filters.StoryComponentFilter;
import scriptease.gui.SETree.filters.TranslatorFilter;
import scriptease.gui.SETree.filters.TypeFilter;
import scriptease.gui.action.typemenus.TypeSelectionAction;
import scriptease.gui.control.FilterableSearchField;
import scriptease.gui.internationalization.Il8nResources;
import scriptease.gui.storycomponentlist.StoryComponentPanelList;
import scriptease.gui.storycomponentpanel.setting.$StoryComponentPanelLibrarySetting;
import scriptease.gui.storycomponentpanel.setting.$StoryComponentPanelSetting;
import scriptease.model.LibraryManager;
import scriptease.model.LibraryModel;
import scriptease.model.complex.StoryComponentContainer;
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

	private FilterableSearchField searchField;

	private final JTabbedPane listTabs;

	private final StoryComponentPanelList causesList;
	private final StoryComponentPanelList effectsList;
	private final StoryComponentPanelList descriptionsList;
	private final StoryComponentPanelList foldersList;

	public LibraryPane(boolean showInvisible) {
		this(new $StoryComponentPanelLibrarySetting(), showInvisible);
	}

	/**
	 * Creates a new LibraryPane with default filters, and configures its
	 * display.
	 * 
	 * @param librarySettings
	 */
	public LibraryPane($StoryComponentPanelSetting librarySettings,
			boolean showInvisible) {
		final LibraryManager libManager = LibraryManager.getInstance();
		this.listTabs = new JTabbedPane();

		// Create the Tree with the root and the default filter
		this.causesList = new StoryComponentPanelList(showInvisible);
		this.effectsList = new StoryComponentPanelList(showInvisible);
		this.descriptionsList = new StoryComponentPanelList(showInvisible);
		this.foldersList = new StoryComponentPanelList(showInvisible);

		this.addStoryComponentsToLists();

		// Configure the displaying of the pane
		this.configurePane();

		// Listen for changes to the Libraries and Translator
		libManager.addLibraryManagerListener(this);
		TranslatorManager.getInstance().addTranslatorObserver(this);
	}

	/**
	 * Adds story components from to the associated StoryComponentLists to
	 * represent them visually.
	 */
	private void addStoryComponentsToLists() {
		final Translator activeTranslator;

		activeTranslator = TranslatorManager.getInstance()
				.getActiveTranslator();

		for (StoryComponentPanelList list : getListOfListTabs()) {
			list.removeAllStoryComponents();
		}

		if (activeTranslator != null) {
			for (LibraryModel library : activeTranslator.getLibraries()) {
				this.causesList.addStoryComponents(library.getCausesCategory()
						.getChildren());
				this.effectsList.addStoryComponents(library
						.getEffectsCategory().getChildren());
				this.descriptionsList.addStoryComponents(library
						.getDescriptionsCategory().getChildren());
				this.foldersList.addStoryComponents(library
						.getFoldersCategory().getChildren());
			}
		}
	}

	/**
	 * Builds a pane that allows users to drag across any pattern (including
	 * atoms) from any library into their Story.
	 * 
	 */
	private void configurePane() {
		final JComponent filterPane;
		final JComponent searchFilterPane;
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

		searchField = new FilterableSearchField(this.causesList, 20);

		searchField.addFilter(this.effectsList);
		searchField.addFilter(this.descriptionsList);
		searchField.addFilter(this.foldersList);

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

					// FIXME Find out what happened when this was called on
					// StoryComponentPanelTree and do same in the list
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
	 * Adds a tree selection listener to each of the tabs.
	 * 
	 * @param listener
	 */
	public void addTreeSelectionListener(TreeSelectionListener listener) {
		// FIXME So we can't really add tree selection listeners to a list,
		// obviously. Figure this out.

		for (StoryComponentPanelList list : getListOfListTabs()) {
			// tree.addTreeSelectionListener(listener);
		}

	}

	/**
	 * Returns all tabs that are StoryComponentPanelTrees as a list, in the same
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
	private StoryComponentFilter buildLibraryFilter(StoryComponentFilter filter) {
		// Filter by translator
		filter.addRule(new TranslatorFilter(TranslatorManager.getInstance()
				.getActiveTranslator()));

		return filter;
	}

	/**
	 * Keep the display of the library up to date with the changes to Libraries
	 * 
	 * TODO This seems to be broken when two modules are loaded. Figure out why!
	 */
	@Override
	public void modelChanged(final LibraryManagerEvent managerEvent) {
		this.addStoryComponentsToLists();

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				// invoke later after the translator has finished loading
				for (StoryComponentPanelList list : getListOfListTabs()) {
					list.filterList();
				}
			}
		});
	}

	/**
	 * Update the translator filter and redraw the tree.
	 */
	@Override
	public void translatorLoaded(Translator newTranslator) {
		this.addStoryComponentsToLists();

		for (StoryComponentPanelList list : getListOfListTabs()) {
			list.updateFilter(new TranslatorFilter(newTranslator));
			list.filterList();
		}
	}
}
