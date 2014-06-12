package scriptease.gui.storycomponentpanel;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.Map;
import java.util.WeakHashMap;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListModel;

import scriptease.ScriptEase;
import scriptease.controller.observer.ObserverManager;
import scriptease.controller.observer.SEFocusObserver;
import scriptease.controller.observer.StoryComponentPanelJListObserver;
import scriptease.gui.SEFocusManager;
import scriptease.gui.filters.Filter;
import scriptease.gui.filters.Filterable;
import scriptease.gui.filters.StoryComponentFilter;
import scriptease.gui.filters.VisibilityFilter;
import scriptease.gui.transfer.StoryComponentPanelTransferHandler;
import scriptease.gui.ui.ScriptEaseUI;
import scriptease.model.StoryComponent;
import scriptease.model.complex.ActivityIt;
import scriptease.model.complex.CauseIt;
import scriptease.model.semodel.SEModelManager;
import scriptease.model.semodel.librarymodel.LibraryModel;
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
	private ObserverManager<StoryComponentPanelJListObserver> observerManager;
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
	 * @param filter
	 */
	public StoryComponentPanelJList(StoryComponentFilter filter) {
		this(filter, true, true);
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
	 * @param addToSEFocus
	 *            Determines whether we should manage focus on this.
	 */
	public StoryComponentPanelJList(StoryComponentFilter filter,
			boolean addToSEFocus) {
		this(filter, true, addToSEFocus);
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
	 * 
	 * @param addToSEFocus
	 *            Determines whether we should manage focus on this.
	 */
	public StoryComponentPanelJList(StoryComponentFilter filter,
			boolean hideInvisible, boolean addToSEFocus) {
		super();
		this.setModel(new DefaultListModel());

		this.panelMap = new WeakHashMap<StoryComponent, StoryComponentPanel>();
		this.filterRule = new VisibilityFilter(hideInvisible);

		if (filter != null)
			this.updateFilter(filter);

		this.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				final JList componentList;
				final Object[] selectedValues;
				StoryComponentPanel panel;
				StoryComponent selectedComponent;
				componentList = (JList) e.getSource();

				// When a user clicks, ctrl-clicks, shift-clicks, etc in the
				// Library panel, we want to notify observers of all the
				// selected StoryComponentPanels so they do not lose their
				// selected border.
				selectedValues = componentList.getSelectedValues();
				for (Object selectedValue : selectedValues) {
					if (selectedValue instanceof StoryComponentPanel) {
						panel = (StoryComponentPanel) selectedValue;
						selectedComponent = panel.getStoryComponent();
						notifyObservers(selectedComponent);
					}
				}
			}
		});

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

		if (addToSEFocus)
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

		final Color selectBg;

		if (addToSEFocus)
			selectBg = ScriptEaseUI.SELECTED_COLOUR;
		else
			selectBg = Color.WHITE;

		this.setSelectionBackground(selectBg);
		this.setBackground(ScriptEaseUI.UNSELECTED_COLOUR);

		this.setDragEnabled(true);
		this.setTransferHandler(StoryComponentPanelTransferHandler
				.getInstance());

		this.observerManager = new ObserverManager<StoryComponentPanelJListObserver>();
	}

	@Override
	public void setSelectedIndex(int index) {
		super.setSelectedIndex(index);
		final StoryComponent component = this.getStoryComponentForIndex(index);
		this.notifyObservers(component);
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

		if (listModel.isEmpty()) {
			final DefaultListModel newModel = new DefaultListModel();

			for (StoryComponent component : storyComponentList) {
				this.addStoryComponent(component, newModel);
			}

			this.setModel(newModel);
		} else
			for (StoryComponent component : storyComponentList) {
				this.addStoryComponent(component, listModel);
			}

		final DefaultListModel currentModel;

		currentModel = (DefaultListModel) this.getModel();

		if (currentModel.isEmpty())
			currentModel.addElement(noResultsPanel);
	}

	/**
	 * Adds a single story component to the model. If adding multiple, it's
	 * recommended to use {@link #addStoryComponents(Collection)} for
	 * efficiency.
	 * 
	 * @param component
	 */
	public void addStoryComponent(StoryComponent component) {
		final DefaultListModel listModel = (DefaultListModel) this.getModel();

		listModel.removeElement(noResultsPanel);

		this.addStoryComponent(component, listModel);

		final DefaultListModel currentModel;

		currentModel = (DefaultListModel) this.getModel();

		if (currentModel.isEmpty())
			currentModel.addElement(noResultsPanel);
	}

	private void addStoryComponent(StoryComponent component,
			DefaultListModel listModel) {
		if ((this.filterRule == null)
				|| ((this.filterRule != null) && (this.filterRule
						.isAcceptable(component))))
			// Check if the element is already part of the list
			if (getIndexOfStoryComponent(component) == -1) {
				final StoryComponentPanel panel;

				panel = panelMap.get(component);

				if (panel == null) {
					final StoryComponentPanel newPanel;

					newPanel = StoryComponentPanelFactory.getInstance()
							.buildStoryComponentPanel(component);

					panelMap.put(component, newPanel);

					listModel.addElement(newPanel);
				} else
					listModel.addElement(panel);
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
	public int getIndexOfStoryComponent(StoryComponent component) {
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

	public StoryComponent getStoryComponentForIndex(int index) {
		StoryComponentPanel panel = getStoryComponentPanelForIndex(index);
		if (panel != null) {
			return panel.getStoryComponent();
		} else {
			return null;
		}
	}

	public StoryComponentPanel getStoryComponentPanelForIndex(int index) {
		final ListModel model = this.getModel();
		if (index >= 0 && index < model.getSize()) {
			return (StoryComponentPanel) model.getElementAt(index);
		}
		return null;
	}

	public void removeStoryComponent(StoryComponent component) {
		final int panelIndex = getIndexOfStoryComponent(component);
		if (panelIndex != -1) {
			((DefaultListModel) this.getModel()).removeElementAt(panelIndex);
			this.panelMap.remove(component);
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

	public void addObserver(StoryComponentPanelJListObserver observer) {
		this.observerManager.addObserver(this, observer);
	}

	/**
	 * 
	 * @param observer
	 */
	public void addObserver(Object object,
			StoryComponentPanelJListObserver observer) {
		this.observerManager.addObserver(object, observer);
	}

	private void notifyObservers(StoryComponent component) {
		final Collection<StoryComponentPanelJListObserver> observers = this.observerManager
				.getObservers();
		for (StoryComponentPanelJListObserver observer : observers) {
			observer.componentSelected(component);
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

				if (isVisible
						&& valueComponent.getLibrary().getReadOnly()
						&& !isSelected
						&& SEModelManager.getInstance().getActiveModel() instanceof LibraryModel
						&& !ScriptEase.DEBUG_MODE)
					valuePanel.setBackground(ScriptEaseUI.TERTIARY_UI);
				else if (isSelected && isVisible)
					valuePanel.setBackground(list.getSelectionBackground());
				else if (isSelected && !isVisible)
					valuePanel.setBackground(ScriptEaseUI.SELECTED_COLOUR);
				else if (!isSelected && !isVisible)
					valuePanel.setBackground(Color.DARK_GRAY);
				else if (!isSelected && isVisible)
					valuePanel.setBackground(list.getBackground());

				if (valueComponent instanceof CauseIt || valueComponent instanceof ActivityIt) {
					valuePanel.setShowChildren(false);
					valuePanel.getExpansionButton().setCollapsed(true);
				}
				
				valuePanel.setToolTipText(valueComponent.getLibrary() + " : "
						+ valueComponent.getDisplayText());

				return valuePanel;
			} else if (value instanceof JPanel) {
				return (JPanel) value;
			} else
				return new JLabel("Error: Not a story component: "
						+ value.toString());
		}

	}
}