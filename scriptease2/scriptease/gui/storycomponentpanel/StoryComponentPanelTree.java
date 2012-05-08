package scriptease.gui.storycomponentpanel;

import javax.swing.JScrollPane;
import javax.swing.event.TreeSelectionListener;

import scriptease.gui.SETree.filters.Filter;
import scriptease.gui.SETree.filters.Filterable;
import scriptease.gui.SETree.filters.StoryComponentFilter;
import scriptease.gui.SETree.filters.VisibilityFilter;
import scriptease.gui.storycomponentpanel.setting.StoryComponentPanelSetting;
import scriptease.model.StoryComponent;
import scriptease.model.complex.ComplexStoryComponent;

/**
 * Tree which contains a root StoryComponentPanel, a StoryComponentPanelManager
 * which manages selection for the entire tree, and StoryComponentPanelSetting
 * for defining tree drag and drag and selection behavior.
 * 
 * @author mfchurch
 * @author lari
 */
@SuppressWarnings("serial")
public class StoryComponentPanelTree extends JScrollPane implements Filterable {
	private StoryComponentPanelManager selectionManager;
	private StoryComponentPanel rootPanel;
	private StoryComponentPanelSetting settings;
	// Default to a VisibilityFilter
	private Filter filterRule = new VisibilityFilter();

	public StoryComponentPanelTree(StoryComponent root,
			StoryComponentPanelSetting settings) {
		this(root, settings, null);
	}

	public StoryComponentPanelTree(StoryComponent root,
			StoryComponentPanelSetting settings, Filter filter) {
		super(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		this.selectionManager = new StoryComponentPanelManager();
		this.settings = settings;
		this.setRoot(root);
		if (filter != null)
			this.updateFilter(filter);

		// Good unit for scrolling vertically using the mouse wheel
		this.getVerticalScrollBar().setUnitIncrement(16);
		
	}

	private void setRoot(StoryComponent root) {
		final StoryComponentPanel rootPanel = StoryComponentPanelFactory
				.getInstance().buildPanel(root);
		if (this.settings != null)
			this.settings.updateComplexSettings(rootPanel);
		this.selectionManager.clearSelection();
		this.selectionManager.addComplexPanel(rootPanel, false);
		this.rootPanel = rootPanel;
		this.setViewportView(this.rootPanel);
		this.filterTree(this.rootPanel);
	}

	/**
	 * Amends the filter to the new desired filter. If the given type of filter
	 * already exists, it replaces it. Otherwise it adds the newFilter as an
	 * additional filter on top of the other filter types.
	 * 
	 * @param newFilterRule
	 *            The new filter rule to add. If <code>null</code> or not an
	 *            instance of <code>StoryComponentFilter</code>, then this
	 *            method will have no effect.
	 */
	@Override
	public void updateFilter(Filter newFilterRule) {
		if (newFilterRule == null
				|| !(newFilterRule instanceof StoryComponentFilter))
			return;

		if (this.filterRule == null)
			this.filterRule = newFilterRule;
		else
			this.filterRule.addRule(newFilterRule);

		this.filterTree(this.rootPanel);
		
	}

	/**
	 * Filter the StoryComponentPanelTree immediate children, does nothing if no
	 * filter is applied
	 */
	public void filterTree() {
		this.filterTree(this.rootPanel);
	}

	/**
	 * Recursively filters the tree that starts at the given root.
	 * 
	 * @param root, -> The root to start filtering from.
	 *     
	 */
	private void filterTree(StoryComponentPanel root) {

		
	

		if (this.filterRule == null || root == null)
			return;

		
	
		
		for(int i=0; i < root.getChildrenPanels().size(); i++){
			boolean accepted = this.filterRule.isAcceptable(root.getChildrenPanels().get(i).getStoryComponent());
			root.getChildrenPanels().get(i).setVisible(accepted);
			this.filterTree(root.getChildrenPanels().get(i));
		}
	}
	

	public StoryComponentPanelManager getSelectionManager() {
		return this.selectionManager;
	}

	/**
	 * Gets the StoryComponentPanelTree's settings
	 * 
	 * @return
	 */
	public StoryComponentPanelSetting getSettings() {
		return this.settings;
	}

	/**
	 * Sets the StoryComponentPanelTree's settings to the given settings, and
	 * updates the root to reflect them
	 * 
	 * @param settings
	 */
	public void setSettings(StoryComponentPanelSetting settings) {
		this.settings = settings;
		if (this.rootPanel != null)
			this.settings.updateComplexSettings(this.rootPanel);
	}

	public void setSelectionManager(StoryComponentPanelManager selectionManager) {
		this.selectionManager = selectionManager;
	}

	public ComplexStoryComponent getRootComponent() {
		return (ComplexStoryComponent) this.rootPanel.getStoryComponent();
	}

	@Override
	public String toString() {
		return "StoryComponentPanelTree [" + rootPanel.getStoryComponent()
				+ "]";
	}

	/**
	 * Registers a new listener to be notified of tree selection changes.
	 * 
	 * @param listener
	 *            The listener to register.
	 */
	public void addTreeSelectionListener(TreeSelectionListener listener) {
		this.selectionManager.addTreeSelectionListener(listener);
	}

	/**
	 * Unregisters a listener to be no longer notified of tree selection
	 * changes.
	 * 
	 * @param listener
	 *            The listener to unregister.
	 */
	public void removeTreeSelectionListener(TreeSelectionListener listener) {
		this.selectionManager.removeTreeSelectionListener(listener);
	}
}
