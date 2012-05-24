package scriptease.gui.pane;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

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
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeSelectionModel;

import scriptease.gui.SETree.GameObjectMultiSelector;
import scriptease.gui.SETree.GameObjectPanelTree;
import scriptease.gui.SETree.GameObjectTreeModel;
import scriptease.gui.SETree.GameObjectTree;
import scriptease.gui.SETree.cell.BindingWidget;
import scriptease.gui.SETree.transfer.DefaultPickerTransferHandlerExportOnly;
import scriptease.gui.control.FilterableSearchField;
import scriptease.model.atomic.knowitbindings.KnowItBindingConstant;
import scriptease.translator.codegenerator.GameObjectPicker;

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

	//private JPanel buildFilterPane(GameObjectTreeModel model) {
	private JPanel buildFilterPane(GameObjectTree model) {
		final JPanel filterPane;

		final JTextField searchField = new FilterableSearchField(model, 20);
		filterPane = new JPanel();
		filterPane.setBorder(BorderFactory.createTitledBorder("Filter"));

		JComponent searchFilterPane = new JPanel();

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
		filterPane.add(searchFilterPane);
		filterPane.setMinimumSize(searchFilterPane.getPreferredSize());
		
		//filterPane.add(new JLabel(""))

		return filterPane;
	}

	public JPanel getPickerPanel() {
		// Configure the panel.
		final JPanel objectPickerPane = new JPanel();

		//final JTree tree = buildGameObjectTree();
		//GameObjectPanelTree tree = new GameObjectPanelTree();
		
		//JScrollPane tree = new JScrollPane();
		//tree.add(gameObjectTree);
		
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
		//---OLD BALLS
		//JComponent filterPane = this.buildFilterPane((GameObjectTreeModel) tree.getModel());
		JComponent filterPane = this.buildFilterPane(tree.getTreeModel());
		//filterPane.setLayout(new L)
		JPanel categoryFilter = new JPanel();
		
		BoxLayout typeFilterPaneLayout = new BoxLayout(categoryFilter,
				BoxLayout.LINE_AXIS);
		
		categoryFilter.setLayout(typeFilterPaneLayout);
		categoryFilter.add(new JLabel("Category Filter: "));
		
		//rootCategoryTypes = tree.getStringTypes();
		//GameObjectMultiSelector multiSelector = new GameObjectMultiSelector(rootCategories);
		//multiSelector.addObserver(tree);
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
		
		objectPickerPane.setBackground(Color.WHITE);

		return objectPickerPane;
	}

	private JTree buildGameObjectTree() {
		final GameObjectTreeModel treeModel;

		treeModel = new GameObjectTreeModel();
		
		//My Fail Injections, blarg blarg blarg
		//SETreeModelGameObject a = new SETreeModelGameObject();
		//a.getTree();
		//GameObjectPanelTree a = new GameObjectPanelTree();
		
		//System.out.println("TREE" + a.getTree().toString());
		//How do you error....

		// Create a JTree to hold the GameObjects.
		final JTree tree = new JTree(treeModel);

		// Set the model's tree so that it can expand rows.. damnit SWING
		treeModel.setTree(tree);

		tree.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				System.out.println(e.getSource());				
				
				if(e.getClickCount() > 1){
					return;
					//TreePath path = tree.getPathForLocation(e.getX(), e.getY());
					//System.out.println(path.toString());
				}
				//Why do we need double clicking enabled?
				
				/*if (e.getClickCount() > 1) {
					TreePath path = tree.getPathForLocation(e.getX(), e.getY());
					if (path != null) {
						DefaultMutableTreeNode comp = ((DefaultMutableTreeNode) path
								.getLastPathComponent());
						KnowItBindingConstant binding = (KnowItBindingConstant) ((BindingWidget) comp
								.getUserObject()).getBinding();
						if (customPicker != null) {
							customPicker.onWidgetClicked(binding);
						}
					}
				}*/
			}
		});

		// Configure behavior/appearance of the tree.
		tree.setRowHeight(0); 
		tree.getSelectionModel().setSelectionMode(
				TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.setDragEnabled(true);
		
		/*******************************************************************************************/
		tree.setTransferHandler(DefaultPickerTransferHandlerExportOnly
				.getInstance());
		/*******************************************************************************************/
		
		tree.setCellRenderer(new GameObjectTreeCellRenderer());

		// Expand all rows.
		// TODO bug where this will cause an infinite loop when trying to expand dialogues
		// int rowCount = tree.getRowCount();
		// for (int i = 0; i < rowCount; i++) {
		// tree.expandRow(i);
		// }
		return tree;
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