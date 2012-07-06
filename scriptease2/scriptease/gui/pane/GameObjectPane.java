package scriptease.gui.pane;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SpringLayout;
import javax.swing.ToolTipManager;
import javax.swing.border.TitledBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import scriptease.gui.SETree.GameObjectMultiSelector;
import scriptease.gui.SETree.GameObjectPanelTree;
import scriptease.gui.SETree.GameObjectTree;
import scriptease.gui.SETree.cell.BindingWidget;
import scriptease.gui.control.FilterableSearchField;
import scriptease.gui.internationalization.Il8nResources;
import scriptease.model.atomic.knowitbindings.KnowItBindingConstant;
import scriptease.translator.codegenerator.GameObjectPicker;

/**
 * The pane containing game objects, such as objects, sounds, etc. that are used
 * in stories.
 * 
 */
public class GameObjectPane implements GameObjectPicker {
	// Although the default picker will be used, a customPicker can define
	// certain behavior of the default one.
	GameObjectPicker customPicker;
	private GameObjectPanelTree tree = new GameObjectPanelTree();
	private GameObjectMultiSelector multiSelector;

	public GameObjectPane() {
		this(null);
		multiSelector = new GameObjectMultiSelector(tree.getStringTypes());
		multiSelector.addObserver(tree);
	}

	public GameObjectPane(GameObjectPicker customPicker) {
		this.customPicker = customPicker;
	}

	private JPanel buildFilterPane(GameObjectTree model) {
		final JPanel filterPane;

		//TODO SearchField does not actually search anything. Figure out why this is.
		final JTextField searchField = new FilterableSearchField(model, 20);
		filterPane = new JPanel();
		filterPane.setBorder(BorderFactory.createTitledBorder("Filter"));

		JComponent searchFilterPane = new JPanel();

		// SearchFilterPane
		searchFilterPane.add(searchField);
		searchFilterPane.setBorder(BorderFactory.createTitledBorder(BorderFactory
				.createLineBorder(Color.gray), Il8nResources
				.getString("Search_Filter_"),
				TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.TOP, new Font(
						"SansSerif", Font.PLAIN, 12), Color.black));
		
		//TODO Set this up to look like it does in LibraryPane, which is much more minimalistic.
		BoxLayout searchFilterPaneLayout = new BoxLayout(searchFilterPane,
				BoxLayout.X_AXIS);
		searchFilterPane.setLayout(searchFilterPaneLayout);

		// FilterPane Layout
		BoxLayout filterPaneLayout = new BoxLayout(filterPane, BoxLayout.Y_AXIS);
		filterPane.setLayout(filterPaneLayout);
		filterPane.add(searchFilterPane);
		filterPane.setMinimumSize(searchFilterPane.getPreferredSize());

		// filterPane.add(new JLabel(""))

		return filterPane;
	}

	public JPanel getPickerPanel() {
		// Configure the panel.
		final JPanel objectPickerPane = new JPanel();

		// final JTree tree = buildGameObjectTree();
		// GameObjectPanelTree tree = new GameObjectPanelTree();

		// JScrollPane tree = new JScrollPane();
		// tree.add(gameObjectTree);

		// Register for tool tips
		ToolTipManager.sharedInstance().registerComponent(tree);

		tree.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		tree.setBackground(Color.WHITE);

		// Add the tree to the pane.
		JScrollPane treeScrollPane = new JScrollPane(tree,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		treeScrollPane.setBackground(Color.WHITE);

		// build the filter
		// ---OLD BALLS
		// JComponent filterPane = this.buildFilterPane((GameObjectTreeModel)
		// tree.getModel());
		JComponent filterPane = this.buildFilterPane(tree.getTreeModel());
		// filterPane.setLayout(new L)
		JPanel categoryFilter = new JPanel();

		BoxLayout typeFilterPaneLayout = new BoxLayout(categoryFilter,
				BoxLayout.LINE_AXIS);

		categoryFilter.setLayout(typeFilterPaneLayout);
		categoryFilter.add(new JLabel("Category Filter: "));

		// rootCategoryTypes = tree.getStringTypes();
		// GameObjectMultiSelector multiSelector = new
		// GameObjectMultiSelector(rootCategories);
		// multiSelector.addObserver(tree);
		categoryFilter.add(multiSelector.getRootButton());
		categoryFilter.add(Box.createHorizontalGlue());

		filterPane.add(categoryFilter);

		objectPickerPane.setPreferredSize(new Dimension(
				tree.getPreferredSize().width, 0));

		SpringLayout pickerPaneLayout = new SpringLayout();

		// Spring filterPane
		pickerPaneLayout.putConstraint(SpringLayout.WEST, filterPane, 5,
				SpringLayout.WEST, objectPickerPane);
		pickerPaneLayout.putConstraint(SpringLayout.NORTH, filterPane, 5,
				SpringLayout.NORTH, objectPickerPane);
		pickerPaneLayout.putConstraint(SpringLayout.EAST, filterPane, -5,
				SpringLayout.EAST, objectPickerPane);
		// Spring pickerTree
		pickerPaneLayout.putConstraint(SpringLayout.WEST, treeScrollPane, 5,
				SpringLayout.WEST, objectPickerPane);
		pickerPaneLayout.putConstraint(SpringLayout.EAST, treeScrollPane, -5,
				SpringLayout.EAST, objectPickerPane);
		pickerPaneLayout.putConstraint(SpringLayout.SOUTH, treeScrollPane, -5,
				SpringLayout.SOUTH, objectPickerPane);
		pickerPaneLayout.putConstraint(SpringLayout.NORTH, treeScrollPane, 5,
				SpringLayout.SOUTH, filterPane);
		objectPickerPane.setLayout(pickerPaneLayout);

		objectPickerPane.add(filterPane);
		objectPickerPane.add(treeScrollPane);
		
		return objectPickerPane;
	}

	public void onWidgetClicked(KnowItBindingConstant object) {
		// Not used here. The custom picker, if it exists, handles this.
	}

	@Override
	public void onWidgetHovered(BindingWidget widget) {

	}

	@Override
	public void onWidgetUnHovered() {
	}
}

/**
 * A TreeCellRenderer for the Game Object panel. This renderer returns the
 * JLabel (or BindingWidget) stored in the given node (<code>value</code> ) of
 * the given <code>tree</code>. The returned JLabel is only used for drawing,
 * and does not receive events, which necessitates a custom TransferHandler for
 * the tree.
 * 
 * @author graves
 */
@SuppressWarnings("serial")
class GameObjectTreeCellRenderer extends DefaultTreeCellRenderer {

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean sel, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {
		JComponent cell = (JComponent) ((DefaultMutableTreeNode) value)
				.getUserObject();
		return cell;
	}
}