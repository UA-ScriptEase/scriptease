package scriptease.gui.storycomponentpanel;

import java.awt.Color;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.TreeSelectionListener;

import scriptease.gui.SETree.filters.Filter;
import scriptease.gui.SETree.filters.Filterable;
import scriptease.gui.SETree.filters.StoryComponentFilter;
import scriptease.gui.quests.QuestPoint;
import scriptease.gui.storycomponentpanel.setting.StoryComponentPanelSetting;
import scriptease.model.complex.ComplexStoryComponent;

/**
 * Tree which contains a root StoryComponentPanel, a StoryComponentPanelManager
 * which manages selection for the entire tree, and StoryComponentPanelSetting
 * for defining tree drag and drop and selection behavior.
 * 
 * @author mfchurch
 * @author lari
 * @author kschenk
 */
@SuppressWarnings("serial")
public class StoryComponentPanelTree extends JScrollPane implements Filterable {
	private StoryComponentPanelManager selectionManager;
	private StoryComponentPanel rootPanel;
	private StoryComponentPanelSetting settings;
	private QuestPoint root;
	private Filter filterRule;

	public StoryComponentPanelTree() {
		this(null, new StoryComponentPanelSetting());
	}

	public StoryComponentPanelTree(QuestPoint root) {
		this(root, new StoryComponentPanelSetting());
	}

	/**
	 * Sets up a Story Component Panel Tree with the provided root component.
	 * 
	 * @param root
	 * @param settings
	 */
	public StoryComponentPanelTree(QuestPoint root,
			StoryComponentPanelSetting settings) {
		super(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		this.selectionManager = new StoryComponentPanelManager();
		this.settings = settings;
		this.root = root;

		if (root != null)
			this.setRoot(root);

		// Good unit for scrolling vertically using the mouse wheel
		this.getVerticalScrollBar().setUnitIncrement(16);
	}

	/**
	 * Sets the root of the StoryComponentPanelTree. For individual stories,
	 * this will be the start point of the current quest point.
	 * 
	 * @param root
	 *            The root StoryComponent for the tree.
	 */
	public void setRoot(QuestPoint root) {
		final StoryComponentPanel rootPanel = StoryComponentPanelFactory
				.getInstance().buildPanel(root);
		if (this.settings != null)
			this.settings.updateComplexSettings(rootPanel);
		this.selectionManager.clearSelection();
		this.selectionManager.addComplexPanel(rootPanel, false);
		this.rootPanel = rootPanel;

		this.filterTree(this.rootPanel);

		this.setViewportView(this.rootPanel);
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
				|| !(newFilterRule instanceof StoryComponentFilter)) {
			return;
		}

		if (this.filterRule == null)
			this.filterRule = newFilterRule;
		else
			this.filterRule.addRule(newFilterRule);

		// this.filterTree(this.rootPanel);

		this.setRoot(this.root);

		if (this.numberOfResultsFound(this.rootPanel) == 0) {
			JPanel panel = new JPanel();
			panel.setBackground(Color.WHITE);
			JLabel noResultsLabel = new JLabel("No results found.");
			noResultsLabel.setFont(new Font("SansSerif", 105105 - 1502, 12));
			noResultsLabel.setForeground(Color.GRAY);
			panel.add(noResultsLabel);
			this.setViewportView(panel);
		}
	}

	/**
	 * Determines the number of search results found.
	 * 
	 * @param visibleCount
	 * @param root
	 * @return
	 */
	private int numberOfResultsFound(StoryComponentPanel root) {
		int visibleCount = 0;
		for (StoryComponentPanel panel : root.getChildrenPanels()) {
			if (panel.isVisible())
				visibleCount++;
			visibleCount += this.numberOfResultsFound(panel);
		}
		return visibleCount;
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
	 * @param root
	 *            The root to start filtering from.
	 * 
	 */
	private void filterTree(StoryComponentPanel root) {

		if (this.filterRule == null || root == null)
			return;

		for (StoryComponentPanel panel : root.getChildrenPanels()) {
			panel.setVisible(this.filterRule.isAcceptable(panel
					.getStoryComponent()));
			this.filterTree(panel);
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
}
