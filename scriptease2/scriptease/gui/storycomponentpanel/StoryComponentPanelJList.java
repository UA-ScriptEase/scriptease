package scriptease.gui.storycomponentpanel;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Collection;
import java.util.Map;
import java.util.WeakHashMap;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;

import scriptease.controller.observer.SEFocusObserver;
import scriptease.gui.SEFocusManager;
import scriptease.gui.filters.Filter;
import scriptease.gui.filters.Filterable;
import scriptease.gui.filters.StoryComponentFilter;
import scriptease.gui.filters.VisibilityFilter;
import scriptease.gui.transfer.StoryComponentPanelTransferHandler;
import scriptease.gui.ui.ScriptEaseUI;
import scriptease.model.StoryComponent;
import scriptease.model.complex.CauseIt;
import scriptease.util.GUIOp;

/**
 * Creates a JList that is able to render Story Component Panels as items. The
 * JList also has a transfer handler attached that gives the ability to drag and
 * drop the Panels.
 * 
 * Create a list with the constructor, then add story components with the
 * addStoryComponents method.
 * 
 * @author kschenk
 * 
 */
@SuppressWarnings("serial")
public class StoryComponentPanelJList extends JList implements Filterable {
	private Filter filterRule;

	// Store a weak map of panels to story components so that we do not have to
	// redraw them every single time
	private final Map<StoryComponent, StoryComponentPanel> panelMap;

	/*
	 * We only need one "no results panel" instead of generating a new one each
	 * time.
	 */
	private static final JPanel noResultsPanel = new JPanel();
	static {
		noResultsPanel.setOpaque(false);
		noResultsPanel.add(new JLabel("No Results Found"));
	}

	/**
	 * Creates a JList that is able to render Story Component Panels as items.
	 * The JList also has a transfer handler attached that gives the ability to
	 * drag and drop the Panels. <br>
	 * <br>
	 * Panels should be added using {@link #addStoryComponents(Collection)}
	 * 
	 */
	public StoryComponentPanelJList() {
		this(null, true);
	}

	/**
	 * Creates a JList that is able to render Story Component Panels as items.
	 * The JList also has a transfer handler attached that gives the ability to
	 * drag and drop the Panels. <br>
	 * <br>
	 * Panels should be added using {@link #addStoryComponents(Collection)}
	 * 
	 * @param filter
	 */
	public StoryComponentPanelJList(StoryComponentFilter filter) {
		this(filter, true);
	}

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

		final DefaultListModel listModel = new DefaultListModel();

		this.setModel(listModel);

		this.panelMap = new WeakHashMap<StoryComponent, StoryComponentPanel>();
		this.filterRule = new VisibilityFilter(hideInvisible);

		if (filter != null)
			this.updateFilter(filter);

		this.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
				SEFocusManager.getInstance().setFocus(
						StoryComponentPanelJList.this);
			}

			@Override
			public void focusLost(FocusEvent e) {
			}
		});

		SEFocusManager.getInstance().addSEFocusObserver(this,
				new SEFocusObserver() {
					@Override
					public void gainFocus(Component oldFocus) {
						setSelectionBackground(ScriptEaseUI.SELECTED_COLOUR);
					}

					@Override
					public void loseFocus(Component oldFocus) {
						setSelectionBackground(GUIOp.scaleWhite(
								ScriptEaseUI.SELECTED_COLOUR, 1.15));
					}
				});

		this.setCellRenderer(new StoryComponentListRenderer());
		this.setLayoutOrientation(JList.VERTICAL);

		this.setSelectionBackground(ScriptEaseUI.SELECTED_COLOUR);
		this.setBackground(ScriptEaseUI.UNSELECTED_COLOUR);

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
		final DefaultListModel listModel = (DefaultListModel) this.getModel();
		listModel.removeElement(noResultsPanel);

		for (StoryComponent component : storyComponentList) {
			this.addStoryComponent(component);
		}

		if (listModel.isEmpty())
			listModel.addElement(noResultsPanel);
	}

	public void addStoryComponent(StoryComponent component) {
		if ((this.filterRule == null)
				|| ((this.filterRule != null) && (this.filterRule
						.isAcceptable(component)))) {
			// Check if the element is already part of the list
			if (getIndexOfStoryComponent(component) == -1) {
				final DefaultListModel listModel;
				final StoryComponentPanel panel;

				listModel = (DefaultListModel) this.getModel();
				panel = panelMap.get(component);

				listModel.removeElement(noResultsPanel);

				if (panel == null) {
					final StoryComponentPanel newPanel;

					newPanel = StoryComponentPanelFactory.getInstance()
							.buildStoryComponentPanel(component);

					panelMap.put(component, newPanel);

					listModel.addElement(newPanel);
				} else {
					listModel.addElement(panel);
				}
			} else {
				System.err.println("StoryComponent " + component
						+ " already exists in StoryComponentPanelJList");
			}
		}
	}

	/**
	 * Returns the index in the StoryComponentPanelJList where the
	 * StoryComponentPanel representing the StoryComponent resides.
	 * 
	 * returns -1 if no StoryComponentPanel is found to represent the given
	 * StoryComponent
	 * 
	 * @param component
	 * @return
	 */
	private int getIndexOfStoryComponent(StoryComponent component) {
		final DefaultListModel listModel = (DefaultListModel) this.getModel();
		int returnIndex = -1;
		for (int panelIndex = 0; panelIndex < listModel.size(); panelIndex++) {
			final Object element = listModel.elementAt(panelIndex);
			if (element instanceof StoryComponentPanel) {
				final StoryComponentPanel panel = (StoryComponentPanel) element;
				final StoryComponent panelComponent = panel.getStoryComponent();
				if (panelComponent == component) {
					returnIndex = panelIndex;
					break;
				}
			}
		}
		return returnIndex;
	}

	public void removeStoryComponent(StoryComponent component) {
		final int panelIndex = getIndexOfStoryComponent(component);
		if (panelIndex != -1) {
			((DefaultListModel) this.getModel()).removeElementAt(panelIndex);
		}
	}

	/**
	 * Replaced the StoryComponentPanel for the StoryComponent with a new one in
	 * the StoryComponentPanelJList
	 * 
	 * @param component
	 */
	public void updateStoryComponentPanel(StoryComponent component) {
		final DefaultListModel listModel = (DefaultListModel) this.getModel();
		final int panelIndex = getIndexOfStoryComponent(component);
		if (panelIndex != -1) {
			final StoryComponentPanel panel;

			panel = StoryComponentPanelFactory.getInstance()
					.buildStoryComponentPanel(component);

			listModel.set(panelIndex, panel);

			panelMap.put(component, panel);

		}
	}

	/**
	 * Removes all Story Components from the list.
	 */
	public void removeAllStoryComponents() {
		this.panelMap.clear();
		((DefaultListModel) this.getModel()).clear();
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
	private class StoryComponentListRenderer extends DefaultListCellRenderer {
		@Override
		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {

			if (value instanceof StoryComponentPanel) {
				final StoryComponentPanel valuePanel;
				final StoryComponent valueComponent;
				final Boolean isVisible;

				valuePanel = (StoryComponentPanel) value;

				valueComponent = valuePanel.getStoryComponent();
				isVisible = valueComponent.isVisible();

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

				if (valueComponent instanceof CauseIt) {
					valuePanel.setShowChildren(false);
					valuePanel.getExpansionButton().setCollapsed(true);
				}

				valuePanel.setToolTipText(valueComponent.getDisplayText());

				return valuePanel;
			} else if (value instanceof JPanel) {
				return (JPanel) value;
			} else
				return new JLabel("Error: Not a story component: "
						+ value.toString());
		}
	}
}
