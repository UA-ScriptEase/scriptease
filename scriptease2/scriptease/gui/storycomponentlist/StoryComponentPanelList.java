package scriptease.gui.storycomponentlist;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;

import scriptease.controller.VisibilityManager;
import scriptease.gui.SETree.filters.Filter;
import scriptease.gui.SETree.filters.Filterable;
import scriptease.gui.SETree.transfer.StoryComponentPanelTransferHandler;
import scriptease.gui.storycomponentpanel.StoryComponentPanel;
import scriptease.gui.storycomponentpanel.StoryComponentPanelFactory;
import scriptease.model.StoryComponent;

/**
 * LISTS OF THINGS!!! =O
 * 
 * TODO Write better javadoc comments
 * 
 * @author kschenk
 * 
 */
@SuppressWarnings("serial")
public class StoryComponentPanelList extends JScrollPane implements Filterable {

	private final DefaultListModel listModel;
	private final JList list;

	/**
	 * Constructor.
	 * 
	 * @param storyComponentList
	 *            The list of StoryComponents to display in the list.
	 * 
	 * @param showInvisible
	 *            If true, invisible components will be shown as well.
	 */
	public StoryComponentPanelList(boolean showInvisible) {
		super();

		this.listModel = new DefaultListModel();
		this.list = new JList(this.listModel);

		this.list.setCellRenderer(new StoryComponentPanelListRenderer(
				showInvisible));
		// TODO Might be necessary to implement this:
		// this.list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		this.list.setLayoutOrientation(JList.VERTICAL);

		this.list.setSelectionBackground(Color.LIGHT_GRAY);
		this.list.setBackground(Color.WHITE);

		// FIXME Likely doesn't work. If it does, awesome! But check it out
		this.list.setDragEnabled(true);
		this.list.setTransferHandler(StoryComponentPanelTransferHandler
				.getInstance());

		this.setViewportView(this.list);
	}

	/**
	 * Set the elements of the tree to the passed story components.
	 * 
	 * @param storyComponentList
	 */
	public void addStoryComponents(List<StoryComponent> storyComponentList) {
		for (StoryComponent component : storyComponentList) {
			this.listModel.addElement(StoryComponentPanelFactory.getInstance()
					.buildPanel(component));
		}

		for (int i = 0; i < listModel.size(); i++)
			this.listModel.setElementAt(listModel.getElementAt(i), i);

		this.list.repaint();
		this.revalidate();
	}

	/**
	 * Removes all Story Components from the list.
	 */
	public void removeAllStoryComponents() {
		this.listModel.removeAllElements();
	}

	@Override
	public void updateFilter(Filter newFilter) {
		this.list.repaint();
		this.revalidate();
	}

	public void filterList() {

		this.list.repaint();
		this.revalidate();
		/*
		 * if (this.filterRule == null || root == null) return;
		 */

	}

	/**
	 * Renders StoryComponentPanels in the StoryComponentPanelList.
	 * 
	 * @author kschenk
	 * 
	 */
	private class StoryComponentPanelListRenderer implements ListCellRenderer {

		private final boolean showInvisible;

		public StoryComponentPanelListRenderer(boolean showInvisble) {
			this.showInvisible = showInvisble;
		}

		@Override
		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {

			if (value instanceof StoryComponentPanel) {
				final StoryComponentPanel valuePanel;
				final Boolean isVisible;

				valuePanel = (StoryComponentPanel) value;
				isVisible = VisibilityManager.getInstance().isVisible(
						valuePanel.getStoryComponent());

				if (isSelected)
					valuePanel.setBackground(list.getSelectionBackground());
				else
					valuePanel.setBackground(list.getBackground());

				valuePanel.setBorder(BorderFactory.createMatteBorder(1, 2, 0,
						1, Color.LIGHT_GRAY));

				if (isVisible)
					return valuePanel;
				else {
					if (this.showInvisible) {
						if (!isSelected)
							valuePanel.setBackground(Color.DARK_GRAY);
						return valuePanel;
					} else {
						return new JPanel();
					}
				}

			} else
				return new JLabel("Error: Not a story component: "
						+ value.toString());
		}
	}
}
