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
import scriptease.gui.SETree.filters.CategoryFilter;
import scriptease.gui.SETree.filters.CategoryFilter.Category;
import scriptease.gui.SETree.filters.StoryComponentFilter;
import scriptease.gui.SETree.filters.TranslatorFilter;
import scriptease.gui.SETree.filters.TypeFilter;
import scriptease.gui.action.typemenus.TypeSelectionAction;
import scriptease.gui.control.FilterableSearchField;
import scriptease.gui.internationalization.Il8nResources;
import scriptease.gui.storycomponentpanel.StoryComponentPanel;
import scriptease.gui.storycomponentpanel.StoryComponentPanelTree;
import scriptease.gui.storycomponentpanel.setting.StoryComponentPanelLibrarySetting;
import scriptease.gui.storycomponentpanel.setting.StoryComponentPanelSetting;
import scriptease.model.LibraryManager;
import scriptease.model.complex.StoryComponentContainer;
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
public class LibraryPane extends JPanel implements LibraryManagerObserver,
		TranslatorObserver {

	private FilterableSearchField searchField;

	private final JTabbedPane treeTabs;

	private final StoryComponentPanelTree causesTree;
	private final StoryComponentPanelTree effectsTree;
	private final StoryComponentPanelTree descriptionsTree;
	private final StoryComponentPanelTree foldersTree;

	public LibraryPane() {
		this(new StoryComponentPanelLibrarySetting());
	}

	/**
	 * Creates a new LibraryPane with default filters, and configures its
	 * display.
	 * 
	 * @param librarySettings
	 */
	public LibraryPane(StoryComponentPanelSetting librarySettings) {
		final LibraryManager libManager = LibraryManager.getInstance();
		final StoryComponentContainer root;
		final StoryComponentFilter causesFilter;
		final StoryComponentFilter effectsFilter;
		final StoryComponentFilter descriptionsFilter;
		final StoryComponentFilter foldersFilter;

		// Construct the Root for the Pane to display
		root = libManager.getLibraryMasterRoot();

		// Create the default filter.
		causesFilter = this.buildLibraryFilter(new CategoryFilter(
				Category.CAUSES));
		effectsFilter = this.buildLibraryFilter(new CategoryFilter(
				Category.EFFECTS));
		descriptionsFilter = this.buildLibraryFilter(new CategoryFilter(
				Category.DESCRIPTIONS));
		foldersFilter = this.buildLibraryFilter(new CategoryFilter(
				Category.FOLDERS));

		this.treeTabs = new JTabbedPane();

		// Create the Tree with the root and the default filter
		this.causesTree = new StoryComponentPanelTree(root, librarySettings,
				causesFilter);
		this.effectsTree = new StoryComponentPanelTree(root, librarySettings,
				effectsFilter);
		this.descriptionsTree = new StoryComponentPanelTree(root,
				librarySettings, descriptionsFilter);
		this.foldersTree = new StoryComponentPanelTree(root, librarySettings,
				foldersFilter);

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

		searchField = new FilterableSearchField(this.causesTree, 20);

		searchField.addFilter(this.effectsTree);
		searchField.addFilter(this.descriptionsTree);
		searchField.addFilter(this.foldersTree);

		treeTabs.add("Causes", causesTree);
		treeTabs.add("Effects", effectsTree);
		treeTabs.add("Descriptions", descriptionsTree);
		treeTabs.add("Folders", foldersTree);

		filterPane.setBorder(BorderFactory.createTitledBorder(BorderFactory
				.createLineBorder(Color.gray), Il8nResources
				.getString("Search_Filter_"),
				TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.TOP, new Font(
						"SansSerif", Font.PLAIN, 12), Color.black));

		// Sets up the type filter.
		typeFilter.setAction(new Runnable() {
			@Override
			public void run() {
				for (StoryComponentPanelTree tree : getListOfTreeTabs())
					tree.updateFilter(new TypeFilter(typeFilter
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

		this.add(treeTabs);
		this.setPreferredSize(filterPane.getPreferredSize());

		// Spring filterPane
		pickerPaneLayout.putConstraint(SpringLayout.WEST, filterPane, 5,
				SpringLayout.WEST, this);
		pickerPaneLayout.putConstraint(SpringLayout.NORTH, filterPane, 5,
				SpringLayout.NORTH, this);
		pickerPaneLayout.putConstraint(SpringLayout.EAST, filterPane, -5,
				SpringLayout.EAST, this);
		// Spring pickerTree
		pickerPaneLayout.putConstraint(SpringLayout.WEST, treeTabs, 5,
				SpringLayout.WEST, this);
		pickerPaneLayout.putConstraint(SpringLayout.EAST, treeTabs, -5,
				SpringLayout.EAST, this);
		pickerPaneLayout.putConstraint(SpringLayout.SOUTH, treeTabs, -5,
				SpringLayout.SOUTH, this);
		pickerPaneLayout.putConstraint(SpringLayout.NORTH, treeTabs, 5,
				SpringLayout.SOUTH, filterPane);
		this.setLayout(pickerPaneLayout);
	}

	/**
	 * Adds a tree selection listener to each of the tabs.
	 * 
	 * @param listener
	 */
	public void addTreeSelectionListener(TreeSelectionListener listener) {
		for (StoryComponentPanelTree tree : getListOfTreeTabs()) {
			tree.addTreeSelectionListener(listener);
		}
	}
	
	/**
	 * Returns all tabs that are StoryComponentPanelTrees as a list, in the same
	 * order as they are in the tabs.
	 * 
	 * @return
	 */
	private List<StoryComponentPanelTree> getListOfTreeTabs() {
		final List<StoryComponentPanelTree> treeList;

		treeList = new ArrayList<StoryComponentPanelTree>();

		for (Component treeTab : this.treeTabs.getComponents())
			if (treeTab instanceof StoryComponentPanelTree)
				treeList.add((StoryComponentPanelTree) treeTab);

		return treeList;
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
	 */
	@Override
	public void modelChanged(final LibraryManagerEvent managerEvent) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				// invoke later after the translator has finished loading
				for (StoryComponentPanelTree tree : getListOfTreeTabs())
					tree.filterTree();
			}
		});
	}

	/**
	 * Update the translator filter and redraw the tree.
	 */
	@Override
	public void translatorLoaded(Translator newTranslator) {
		for (StoryComponentPanelTree tree : getListOfTreeTabs()) {
			tree.updateFilter(new TranslatorFilter(newTranslator));
			tree.filterTree();
		}
	}
}
