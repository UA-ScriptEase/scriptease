package scriptease.gui.storycomponentlist;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;

import scriptease.controller.VisibilityManager;
import scriptease.gui.SETree.transfer.StoryComponentPanelTransferHandler;
import scriptease.gui.storycomponentpanel.StoryComponentPanel;

/**
 * 
 * @author kschenk
 * 
 */
@SuppressWarnings("serial")
public class StoryComponentPanelList extends JScrollPane {

	/*
	 * Parameters: List of story component panels List of filters
	 * 
	 * Need a way to update filters/check for updates to filters.
	 */

	/**
	 * Constructor.
	 * 
	 * @param storyComponentPanelList
	 *            The list of StoryComponentPanels to display in the list.
	 * 
	 * @param showInvisible
	 *            If true, invisible components will be shown as well.
	 */
	public StoryComponentPanelList(
			List<StoryComponentPanel> storyComponentPanelList,
			boolean showInvisible) {
		super();

		final JList list;
		final DefaultListModel listModel;

		listModel = new DefaultListModel();
		list = new JList(listModel);

		list.setCellRenderer(new StoryComponentPanelListRenderer());
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.setLayoutOrientation(JList.VERTICAL);

		list.setSelectionBackground(Color.LIGHT_GRAY);
		list.setBackground(Color.WHITE);

		// FIXME Likely doesn't work. Check it out
		
		// TODO Should also only be applied if necessary. i.e. StoryCompBuilder
		// does not need drag and dropiness
		list.setTransferHandler(StoryComponentPanelTransferHandler
				.getInstance());

		for (StoryComponentPanel panel : storyComponentPanelList) {
			if (showInvisible)
				listModel.addElement(panel);
			else if (VisibilityManager.getInstance().isVisible(
					panel.getStoryComponent())) {
				listModel.addElement(panel);
			}
		}

		this.setViewportView(list);
		this.setPreferredSize(new Dimension(250, 80));
	}

	/**
	 * Renders StoryComponentPanels in the StoryComponentPanelList.
	 * 
	 * @author kschenk
	 * 
	 */
	private class StoryComponentPanelListRenderer implements ListCellRenderer {

		@Override
		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {

			if (value instanceof StoryComponentPanel) {
				final StoryComponentPanel valuePanel;

				valuePanel = (StoryComponentPanel) value;

				if (isSelected)
					valuePanel.setBackground(list.getSelectionBackground());
				else
					valuePanel.setBackground(list.getBackground());

				valuePanel.setBorder(BorderFactory.createMatteBorder(1, 2, 0,
						1, Color.black));

				return valuePanel;
			} else
				return new JLabel(value.toString());
		}
	}
}
