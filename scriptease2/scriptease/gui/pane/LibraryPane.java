package scriptease.gui.pane;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;

import scriptease.controller.observer.LibraryManagerEvent;
import scriptease.controller.observer.LibraryManagerObserver;
import scriptease.controller.observer.TranslatorObserver;
import scriptease.gui.SETree.filters.CategoryFilter;
import scriptease.gui.SETree.filters.CategoryFilter.Category;
import scriptease.gui.SETree.filters.StoryComponentFilter;
import scriptease.gui.SETree.filters.TranslatorFilter;
import scriptease.gui.SETree.filters.TypeFilter;
import scriptease.gui.action.view.ShowFilterMenuAction;
import scriptease.gui.control.FilterableSearchField;
import scriptease.gui.internationalization.Il8nResources;
import scriptease.gui.storycomponentpanel.StoryComponentPanelTree;
import scriptease.gui.storycomponentpanel.setting.StoryComponentPanelLibrarySetting;
import scriptease.gui.storycomponentpanel.setting.StoryComponentPanelSetting;
import scriptease.model.LibraryManager;
import scriptease.model.complex.StoryComponentContainer;
import scriptease.translator.Translator;
import scriptease.translator.TranslatorManager;
import scriptease.util.StringOp;

@SuppressWarnings("serial")
/**
 * LibraryPane represents the JPanel used for managing, filtering and choosing Patterns from the loaded Libraries
 * 
 * @author mfchurch
 */
public class LibraryPane extends JPanel implements LibraryManagerObserver,
		TranslatorObserver {
	private final String TYPE_FILTER_LABEL = Il8nResources
			.getString("Type_Filter_");
	private final StoryComponentPanelTree tree;

	public LibraryPane() {
		this(new StoryComponentPanelLibrarySetting());
	}

	public LibraryPane(StoryComponentPanelSetting librarySettings) {
		final LibraryManager libManager = LibraryManager.getInstance();
		final StoryComponentContainer root;
		final StoryComponentFilter filter;

		// Construct the Root for the Pane to display
		root = libManager.getLibraryMasterRoot();

		// Create the default filter.
		filter = this.buildLibraryFilter();

		// Create the Tree with the root and the default filter
		this.tree = new StoryComponentPanelTree(root, librarySettings, filter);
		
	
		// Configure the displaying of the pane
		this.configurePane();

		// Listen for changes to the Libraries and Translator
		libManager.addLibraryManagerListener(this);
		TranslatorManager.getInstance().addTranslatorObserver(this);
	}

	public StoryComponentPanelTree getSCPTree() {
		return tree;
	}

	/**
	 * Creates the default Filter to be used by the LibraryTreeModel
	 * 
	 * @return
	 */
	private StoryComponentFilter buildLibraryFilter() {
		// Filter by category
		CategoryFilter filter = new CategoryFilter(Category.CAUSES);
		// Filter by translator
		filter.addRule(new TranslatorFilter(TranslatorManager.getInstance()
				.getActiveTranslator()));

		return filter;
	}

	/**
	 * Builds a pane that allows users to drag across any pattern (including
	 * atoms) from any library into their Story.
	 * 
	 * @return A Pattern Picker
	 */
	private void configurePane() {
		final JComponent filterPane;
		final JComboBox categoryFilterCombo;

		// Build the Category Filter Combo Box
		categoryFilterCombo = buildCategoryFilterComboBox();

		final JTextField searchField = new FilterableSearchField(this.tree, 20);

		filterPane = new JPanel();
		filterPane.setBorder(BorderFactory.createTitledBorder("Filter"));

		JComponent typeFilterPane = new JPanel();
		JComponent searchFilterPane = new JPanel();

		// TypeFilterPane Layout
		BoxLayout typeFilterPaneLayout = new BoxLayout(typeFilterPane,
				BoxLayout.LINE_AXIS);
		typeFilterPane.setLayout(typeFilterPaneLayout);
		typeFilterPane.add(Box.createHorizontalGlue());
		typeFilterPane.add(Box.createHorizontalStrut(8));
		typeFilterPane.add(new JLabel("Category:"));
		typeFilterPane.add(categoryFilterCombo);
		categoryFilterCombo.setMaximumSize(categoryFilterCombo
				.getPreferredSize());
		typeFilterPane.add(Box.createHorizontalStrut(10));
		typeFilterPane.add(new JLabel(TYPE_FILTER_LABEL));

		final ShowFilterMenuAction typeFilter = new ShowFilterMenuAction();
		typeFilter.setSelectionChangedAction(new Runnable() {
			@Override
			public void run() {
				tree.updateFilter(new TypeFilter(typeFilter.getAcceptedTypes()));
			}
		});

		typeFilterPane.add(new JButton(typeFilter));
		typeFilterPane.add(Box.createHorizontalStrut(8));
		typeFilterPane.add(Box.createHorizontalGlue());

		// SearchFilterPane
		searchFilterPane.add(new JLabel(
				FilterableSearchField.SEARCH_FILTER_LABEL));
		searchFilterPane.add(searchField);
		BoxLayout searchFilterPaneLayout = new BoxLayout(searchFilterPane,
				BoxLayout.X_AXIS);
		searchFilterPane.setLayout(searchFilterPaneLayout);

		// FilterPane Layout
		BoxLayout filterPaneLayout = new BoxLayout(filterPane, BoxLayout.Y_AXIS);
		filterPane.setLayout(filterPaneLayout);
		filterPane.add(typeFilterPane);
		typeFilterPane.add(Box.createHorizontalStrut(8));
		filterPane.add(searchFilterPane);
		filterPane.setMinimumSize(typeFilterPane.getPreferredSize());

		this.add(filterPane);
		this.add(tree);
		this.setPreferredSize(filterPane.getPreferredSize());
		SpringLayout pickerPaneLayout = new SpringLayout();

		// Spring filterPane
		pickerPaneLayout.putConstraint(SpringLayout.WEST, filterPane, 5,
				SpringLayout.WEST, this);
		pickerPaneLayout.putConstraint(SpringLayout.NORTH, filterPane, 5,
				SpringLayout.NORTH, this);
		pickerPaneLayout.putConstraint(SpringLayout.EAST, filterPane, -5,
				SpringLayout.EAST, this);
		// Spring pickerTree
		pickerPaneLayout.putConstraint(SpringLayout.WEST, tree, 5,
				SpringLayout.WEST, this);
		pickerPaneLayout.putConstraint(SpringLayout.EAST, tree, -5,
				SpringLayout.EAST, this);
		pickerPaneLayout.putConstraint(SpringLayout.SOUTH, tree, -5,
				SpringLayout.SOUTH, this);
		pickerPaneLayout.putConstraint(SpringLayout.NORTH, tree, 5,
				SpringLayout.SOUTH, filterPane);
		this.setLayout(pickerPaneLayout);

		// --- listeners ---
		// Changes in Category Filter combo:
		categoryFilterCombo.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					final String selectedCategory = (String) e.getItem();

					tree.updateFilter(new CategoryFilter(Category
							.valueOf(selectedCategory.toUpperCase())));
				}
			}
		});
	}

	/**
	 * Builds the Category Filter with the possible Library Categories. These
	 * are built from the Proper Case names of the values in {@link Category}
	 * 
	 * @return The combobox for choosing the category.
	 */
	private JComboBox buildCategoryFilterComboBox() {
		final List<String> labels = new ArrayList<String>();

		for (Category c : Category.values()) {
			labels.add(StringOp.toProperCase(c.toString()));
		}

		Collections.sort(labels);
		return new JComboBox(labels.toArray());
	}

	/**
	 * Keep the display of the library up to date with the changes to Libraries
	 */
	@Override
	public void modelChanged(final LibraryManagerEvent managerEvent) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				// invoke later after the translator has finished loading
				tree.filterTree();
			}
		});
	}

	/**
	 * Update the translator filter and redraw the tree
	 */
	@Override
	public void translatorLoaded(Translator newTranslator) {
		this.tree.updateFilter(new TranslatorFilter(newTranslator));
		this.tree.filterTree();
	}
}
