package scriptease.gui.storycomponentpanel;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseListener;
import java.util.Collection;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.DropMode;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;

import scriptease.controller.VisibilityManager;
import scriptease.gui.SETree.filters.Filter;
import scriptease.gui.SETree.filters.Filterable;
import scriptease.gui.SETree.filters.StoryComponentFilter;
import scriptease.gui.SETree.filters.VisibilityFilter;
import scriptease.gui.SETree.transfer.StoryComponentPanelTransferHandler;
import scriptease.model.LibraryModel;
import scriptease.model.StoryComponent;
import scriptease.translator.Translator;
import scriptease.translator.TranslatorManager;

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
	private final static JPanel blankPanel = new JPanel();

	private final DefaultListModel listModel;
	private final JList list;
	private Filter filterRule;

	/**
	 * Constructor.
	 * 
	 * @param filter
	 * 
	 * @param storyComponentList
	 *            The list of StoryComponents to display in the list.
	 * 
	 * @param showInvisible
	 *            If true, invisible components will be shown as well.
	 */
	public StoryComponentPanelList(StoryComponentFilter filter,
			boolean showInvisible) {
		super();

		this.listModel = new DefaultListModel();
		this.list = new JList(this.listModel);

		blankPanel.setSize(new Dimension(0, 0));

		if (showInvisible)
			filterRule = null;
		else
			filterRule = new VisibilityFilter();

		if (filter != null)
			this.updateFilter(filter);

		this.list.setCellRenderer(new StoryComponentPanelListRenderer());
		// TODO Might be necessary to implement this:
		// this.list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		this.list.setLayoutOrientation(JList.VERTICAL);

		this.list.setSelectionBackground(Color.LIGHT_GRAY);
		this.list.setBackground(Color.WHITE);

		// FIXME Doesn't work
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
	public void addStoryComponents(Collection<StoryComponent> storyComponentList) {
		for (StoryComponent component : storyComponentList) {
			if (!(filterRule == null)) {
				if (!filterRule.isAcceptable(component))
					continue;
			}
			this.listModel.addElement(StoryComponentPanelFactory.getInstance()
					.buildPanel(component));
		}

		for (int i = 0; i < listModel.size(); i++)
			this.listModel.setElementAt(listModel.getElementAt(i), i);
	}

	/**
	 * Removes all Story Components from the list.
	 */
	public void removeAllStoryComponents() {
		this.listModel.removeAllElements();
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

		filterList();
	}

	public void filterList() {
		if (this.filterRule == null)
			return;

		final Translator activeTranslator;

		activeTranslator = TranslatorManager.getInstance()
				.getActiveTranslator();

		this.removeAllStoryComponents();

		if (activeTranslator != null)
			for (LibraryModel libraryModel : TranslatorManager.getInstance()
					.getActiveTranslator().getLibraries()) {
				this.addStoryComponents(libraryModel.getMainStoryComponents());
			}

		this.list.repaint();
		this.revalidate();
	}

	/**
	 * Adds a mouse listener to the list.
	 * 
	 * @param mouseListener
	 */
	public void addListMouseListener(MouseListener mouseListener) {
		this.list.addMouseListener(mouseListener);
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
				final StoryComponent valueComponent;

				valuePanel = (StoryComponentPanel) value;
				valueComponent = valuePanel.getStoryComponent();

				if (isSelected)
					valuePanel.setBackground(list.getSelectionBackground());
				else
					valuePanel.setBackground(list.getBackground());

				valuePanel.setBorder(BorderFactory.createMatteBorder(1, 2, 0,
						1, Color.LIGHT_GRAY));

				final Boolean isVisible;

				isVisible = VisibilityManager.getInstance().isVisible(
						valueComponent);

				if (isVisible || isSelected)
					return valuePanel;
				else {
					valuePanel.setBackground(Color.DARK_GRAY);
					return valuePanel;
				}
			} else
				return new JLabel("Error: Not a story component: "
						+ value.toString());
		}
	}
}
