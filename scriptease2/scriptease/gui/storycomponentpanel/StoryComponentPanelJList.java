package scriptease.gui.storycomponentpanel;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseListener;
import java.util.Collection;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import scriptease.controller.VisibilityManager;
import scriptease.gui.ComponentFocusManager;
import scriptease.gui.SETree.filters.Filter;
import scriptease.gui.SETree.filters.Filterable;
import scriptease.gui.SETree.filters.StoryComponentFilter;
import scriptease.gui.SETree.filters.VisibilityFilter;
import scriptease.gui.SETree.transfer.StoryComponentPanelTransferHandler;
import scriptease.model.StoryComponent;

/**
 * Creates a JList that is able to render Story Component Panels as items. The
 * JList also has a transfer handler attached that gives the ability to drag and
 * drop the Panels.
 * 
 * At the moment, this List is used to display the Library. However, it should
 * be very easy to use it elsewhere if necessary. Just create a list with the
 * constructor, then add story components with the addStoryComponents method.
 * 
 * @author kschenk
 * 
 */
@SuppressWarnings("serial")
public class StoryComponentPanelJList extends JList implements Filterable {
	private Filter filterRule;

	/**
	 * Creates a JList that is able to render Story Component Panels as items.
	 * The JList also has a transfer handler attached that gives the ability to
	 * drag and drop the Panels. <br>
	 * <br>
	 * Panels should be added using {@link #addStoryComponents(Collection)}
	 * 
	 * @param filter
	 * 
	 * @param storyComponentList
	 *            The list of StoryComponents to display in the list.
	 * 
	 * @param hideInvisible
	 *            If true, invisible components will be shown as well.
	 */
	public StoryComponentPanelJList(StoryComponentFilter filter,
			boolean hideInvisible) {
		super();

		DefaultListModel listModel = new DefaultListModel();

		this.setModel(listModel);

		filterRule = new VisibilityFilter(hideInvisible);

		if (filter != null)
			this.updateFilter(filter);

		this.addFocusListener(ComponentFocusManager.getInstance()
				.defaultFocusListener(this));

		this.setCellRenderer(new StoryComponentListRenderer());
		this.setLayoutOrientation(JList.VERTICAL);

		this.setSelectionBackground(Color.LIGHT_GRAY);
		this.setBackground(Color.WHITE);

		this.setDragEnabled(true);
		this.setTransferHandler(StoryComponentPanelTransferHandler
				.getInstance());
	}

	/**
	 * Generates panels for the passed in list of Story Components and adds them
	 * to the list. <br>
	 * <br>
	 * Note: This does not remove any Story Component Panels. Call
	 * {@link #removeAllStoryComponents()} first, as needed.
	 * 
	 * @param storyComponentList
	 */
	public void addStoryComponents(Collection<StoryComponent> storyComponentList) {
		final DefaultListModel listModel;

		listModel = (DefaultListModel) this.getModel();

		for (StoryComponent component : storyComponentList) {
			if (!(filterRule == null)) {
				if (!filterRule.isAcceptable(component))
					continue;
			}

			listModel.addElement(StoryComponentPanelFactory.getInstance()
					.buildStoryComponentPanel(component));
		}

		for (int i = 0; i < listModel.size(); i++)
			listModel.setElementAt(listModel.getElementAt(i), i);
	}

	/**
	 * Removes all Story Components from the list.
	 */
	public void removeAllStoryComponents() {
		final DefaultListModel listModel;
		listModel = (DefaultListModel) this.getModel();

		listModel.clear();
	}

	/**
	 * Adds a mouse listener to the list. This method needs to exist because
	 * using the {@link #addMouseListener(MouseListener)} method would just add
	 * a mouse listener to the JScrollPane.
	 * 
	 * @param mouseListener
	 */
	public void addListMouseListener(MouseListener mouseListener) {
		this.addMouseListener(mouseListener);
	}

	@Override
	public void updateFilter(Filter newFilterRule) {
		if (newFilterRule == null
				|| !(newFilterRule instanceof StoryComponentFilter))
			return;

		if (this.filterRule == null)
			this.filterRule = newFilterRule;
		else
			this.filterRule.addRule(newFilterRule);
	}

	/**
	 * Renders StoryComponentPanels in the StoryComponentPanelList.
	 * 
	 * @author kschenk
	 * 
	 */
	private class StoryComponentListRenderer implements ListCellRenderer {
		@Override
		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {

			if (value instanceof StoryComponentPanel) {
				final StoryComponentPanel valuePanel;
				final StoryComponent valueComponent;
				final Boolean isVisible;

				valuePanel = (StoryComponentPanel) value;

				valueComponent = valuePanel.getStoryComponent();
				isVisible = VisibilityManager.getInstance().isVisible(
						valueComponent);

				valuePanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1,
						0, Color.LIGHT_GRAY));

				if (isSelected && isVisible)
					valuePanel.setBackground(list.getSelectionBackground());
				else if (isSelected && !isVisible)
					valuePanel.setBackground(Color.GRAY);
				else if (!isSelected && !isVisible)
					valuePanel.setBackground(Color.DARK_GRAY);
				else if (!isSelected && isVisible)
					valuePanel.setBackground(list.getBackground());
				return valuePanel;
			} else
				return new JLabel("Error: Not a story component: "
						+ value.toString());
		}
	}
}
