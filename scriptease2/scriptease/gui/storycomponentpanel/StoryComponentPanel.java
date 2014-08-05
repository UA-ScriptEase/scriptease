package scriptease.gui.storycomponentpanel;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.TransferHandler;
import javax.swing.event.MouseInputListener;

import scriptease.controller.observer.storycomponent.StoryComponentEvent;
import scriptease.controller.observer.storycomponent.StoryComponentEvent.StoryComponentChangeEnum;
import scriptease.controller.observer.storycomponent.StoryComponentObserver;
import scriptease.gui.SEFocusManager;
import scriptease.gui.component.ExpansionButton;
import scriptease.gui.component.ScriptWidgetFactory;
import scriptease.gui.pane.DescribeItPanel;
import scriptease.gui.ui.ScriptEaseUI;
import scriptease.model.StoryComponent;
import scriptease.model.complex.StoryPoint;
import scriptease.model.semodel.librarymodel.LibraryModel;
import scriptease.util.GUIOp;
import scriptease.util.StringOp;

/**
 * JPanel used to properly observe and redraw its component. Can be editable and
 * extendible to be shown/hidden.
 * 
 * @author mfchurch
 * @author kschenk
 * @author jyuen
 */
@SuppressWarnings("serial")
public class StoryComponentPanel extends JPanel implements
		StoryComponentObserver {

	private final StoryComponentPanelLayoutManager layout = new StoryComponentPanelLayoutManager();
	// Component
	private final StoryComponent component;
	// Panel Properties
	private boolean editable;
	private boolean selectable;
	private boolean removable;

	private ExpansionButton expansionButton;

	public StoryComponentPanel(StoryComponent component) {
		// State of Panel
		this.setOpaque(true);
		this.component = component;
		this.editable = true;

		if (this.component == null)
			return;

		final MouseInputListener mouseListener = this.mouseListener();
		// Action Listeners
		this.addMouseMotionListener(mouseListener);
		this.addMouseListener(mouseListener);

		this.component.addStoryComponentObserver(this);

		// Layout
		this.setLayout(this.layout);

		this.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
				SEFocusManager.getInstance().setFocus(StoryComponentPanel.this);
			}

			@Override
			public void focusLost(FocusEvent e) {
			}
		});

		this.setVisible(component.isVisible());
	}

	@Override
	public StoryComponentPanelLayoutManager getLayout() {
		return this.layout;
	}

	public boolean showChildren() {
		return this.layout.showChildren();
	}

	public void setShowChildren(boolean showChildren) {
		this.layout.setShowChildren(showChildren);
	}

	public boolean isEditable() {
		return this.editable;
	}

	public boolean isRemovable() {
		return this.removable;
	}

	public boolean isSelectable() {
		return this.selectable;
	}

	public void setExpansionButton(ExpansionButton expansionButton) {
		this.expansionButton = expansionButton;
	}

	public ExpansionButton getExpansionButton() {
		return this.expansionButton;
	}

	/**
	 * Sets whether the current component is allowed to be edited.
	 * 
	 * @param editable
	 */
	public void setEditable(boolean editable) {
		this.editable = editable;
		final Collection<Component> components = GUIOp
				.getContainerComponents(this);
		for (Component component : components) {
			if (!(component instanceof JLabel))
				component.setEnabled(editable);
		}
	}

	public void setSelectable(boolean selectable) {
		this.selectable = selectable;
	}

	public void setRemovable(boolean removable) {
		this.removable = removable;
	}

	/**
	 * Get the StoryComponent being represented by the StoryComponentPanel
	 * 
	 * @return
	 */
	public StoryComponent getStoryComponent() {
		return this.component;
	}

	/**
	 * Get the parent StoryComponentPanelTree of the StoryComponentPanel, if
	 * none exists, returns null
	 * 
	 * @return
	 */
	public StoryComponentPanelTree getParentTree() {
		Container parent = this.getParent();
		while (parent != null) {
			if (parent instanceof StoryComponentPanelTree)
				return (StoryComponentPanelTree) parent;
			else
				parent = parent.getParent();
		}
		return null;
	}

	/**
	 * Get the closest ancestor StoryComponentPanel of this StoryComponentPanel.
	 * if none exists, returns null
	 * 
	 * @return
	 */
	public StoryComponentPanel getParentStoryComponentPanel() {
		Container parent = this.getParent();
		while (parent != null) {
			if (parent instanceof StoryComponentPanel)
				return (StoryComponentPanel) parent;
			else
				parent = parent.getParent();
		}
		return null;
	}

	/**
	 * Get the immediate children of the StoryComponentPanel
	 * 
	 * @return
	 */
	public List<StoryComponentPanel> getChildrenPanels() {
		return this.layout.getChildrenPanels();
	}

	/**
	 * Returns a Collection containing the descendants of the
	 * StoryComponentPanel
	 * 
	 * @return {@link #getChildrenPanels()}
	 */
	public List<StoryComponentPanel> getDescendants() {
		// Create a new List to contain the descendants of this
		// StoryComponentPanel.
		final List<StoryComponentPanel> descendants = new ArrayList<StoryComponentPanel>();
		// Add the children of this StoryComponentPanel.
		final List<StoryComponentPanel> childrenPanels;

		childrenPanels = this.getChildrenPanels();

		// Recursively call this method of the child StoryComponentPanels to add
		// their descendants.
		descendants.add(this);
		for (StoryComponentPanel child : childrenPanels) {
			descendants.addAll(child.getDescendants());
		}

		// Return the list of descendants.
		return descendants;
	}

	@Override
	public void setToolTipText(String text) {
		final String libraryName = this.component.getLibrary().getTitle();
		final String toolTip;

		if (StringOp.exists(text))
			if (libraryName.equals(LibraryModel.NON_LIBRARY_NAME)
					|| libraryName.equals(LibraryModel.COMMON_LIBRARY_NAME))
				toolTip = text;
			else
				toolTip = libraryName + ": " + text;
		else
			toolTip = libraryName;

		super.setToolTipText(toolTip);
	}

	/**
	 * StoryComponentPanel is listening to the model for changes such as:
	 * adding, removing, binding and changes in text for potential adding,
	 * removing resizing and redrawing of panels.
	 */
	@Override
	public void componentChanged(StoryComponentEvent event) {
		final StoryComponentChangeEnum type = event.getType();
		final StoryComponent component = event.getSource();

		if (type.equals(StoryComponentChangeEnum.CHANGE_CHILD_ADDED)) {
			if (component.getOwner() == this.component) {
				StoryComponentPanelFactory.getInstance().addChild(this,
						component);
			}
		} else if (type.equals(StoryComponentChangeEnum.CHANGE_CHILD_REMOVED)) {
			if (component.getOwner() == null
					|| component.getOwner() == this.component
					&& component instanceof StoryComponent) {
				component.removeStoryComponentObserverFromChildren(this);
				StoryComponentPanelFactory.getInstance().removeChild(this,
						component);
			}
		} else if (type.equals(StoryComponentChangeEnum.CHANGE_VISIBILITY)) {
			this.setVisible(component.isVisible());
		} else if (type.equals(StoryComponentChangeEnum.CHANGE_LABELS_CHANGED)
				|| type.equals(StoryComponentChangeEnum.CHANGE_PROBLEMS_SET)) {
			StoryComponentPanelFactory.getInstance().rebuildLabels(this);
		} else if (type
				.equals(StoryComponentChangeEnum.CHANGE_TEXT_DESCRIPTION)) {
			this.setToolTipText(component.getDescription());
		} else if (type.equals(StoryComponentChangeEnum.CHANGE_DISABILITY)) {

			// Change the font color to orange if disabled
			final JPanel mainPanel = this.getLayout().getMainPanel();
			final Component[] children = mainPanel.getComponents();

			for (Component child : children) {
				if (child instanceof JLabel) {
					final JLabel label = (JLabel) child;

					// Get all the JLabels i.e. not Labels.
					if (!label.getBackground().equals(
							ScriptEaseUI.COLOUR_DISABLED)
							&& !label
									.getBackground()
									.equals(ScriptWidgetFactory.LABEL_BACKGROUND_COLOUR)) {

						if (this.component.isEnabled())
							label.setForeground(Color.BLACK);
						else
							label.setForeground(ScriptEaseUI.COLOUR_DISABLED);

						label.repaint();
					}
				} else if (child instanceof DescribeItPanel) {
					final DescribeItPanel describeItPanel = (DescribeItPanel) child;

					final Component[] describeItPanels = describeItPanel
							.getScriptItPanel().getComponents();

					for (Component panel : describeItPanels) {
						if (panel instanceof JLabel) {
							final JLabel label = (JLabel) panel;
							if (component.isEnabled())
								label.setForeground(Color.BLACK);
							else
								label.setForeground(ScriptEaseUI.COLOUR_DISABLED);

							label.repaint();
						}
					}
				}
			}
		}

		// revalidate the panel
		this.revalidate();
		this.repaint();
	}

	@Override
	public String toString() {
		return "StoryComponentPanel [" + this.component + "]";
	}

	/**
	 * Get the StoryComponentPanelManager which is keeping track of the panel's
	 * selection
	 * 
	 * @return
	 */
	public StoryComponentPanelManager getSelectionManager() {
		final StoryComponentPanelTree parentTree = this.getParentTree();

		if (parentTree != null)
			return parentTree.getSelectionManager();

		return null;
	}

	/**
	 * Method forwarded to the selection manager.
	 */
	public void updatePanelBackgrounds() {
		final StoryComponentPanelManager manager = this.getSelectionManager();
		if (manager != null)
			manager.updatePanelBackgrounds();
		else
			this.setBackground(ScriptEaseUI.UNSELECTED_COLOUR);
	}

	/**
	 * Makes every StoryComponentPanel editable, and any non-root
	 * StoryComponentPanels selectable and removable
	 */
	public void updateComplexSettings() {
		if (this.component != null) {
			for (StoryComponentPanel panel : this.getDescendants()) {
				panel.setSelectable(true);
				panel.setRemovable(true);
				panel.setEditable(true);
			}
		}
	}

	/**
	 * Creates a new mouse input listener for the story component panel that
	 * handles drag and click events.
	 * 
	 * @return
	 */
	private MouseInputListener mouseListener() {
		return new MouseInputListener() {
			private StoryComponentPanel panel = StoryComponentPanel.this;
			private Color previousColor = StoryComponentPanel.this
					.getBackground();

			private Point dragStart;

			/**
			 * Toggle a drag event manually
			 */
			@Override
			public void mouseDragged(MouseEvent e) {
				final StoryComponentPanelManager selectionManager;
				final Point point;

				selectionManager = panel.getSelectionManager();
				point = e.getPoint();

				if (selectionManager != null && dragStart != null
						&& point != null && point.distance(dragStart) > 20) {
					final boolean clearSelection;

					clearSelection = !(selectionManager.getSelectedPanels()
							.contains(StoryComponentPanel.this));
					selectionManager.setSelection(panel, true, clearSelection);

					JComponent component = (JComponent) e.getSource();

					// Determine the type of action
					final int action;
					if (panel.removable)
						action = TransferHandler.MOVE;
					else
						action = TransferHandler.COPY;

					component.getTransferHandler().exportAsDrag(
							(JComponent) e.getSource(), e, action);

				}
				e.consume();
			}

			/**
			 * Toggle panel selection when clicked, then redraw with a grey
			 * selection tint
			 */
			@Override
			public void mouseClicked(MouseEvent e) {
				final StoryComponentPanelManager selectionManager = panel
						.getSelectionManager();
				if (selectionManager != null)
					selectionManager.toggleSelection(panel, e);

				e.consume();
			}

			@Override
			public void mouseMoved(MouseEvent e) {
				e.consume();
			}

			@Override
			public void mousePressed(MouseEvent e) {
				dragStart = e.getPoint();

				e.consume();
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				e.consume();
			}

			private final Timer hoverTimer = new Timer(150,
					new ActionListener() {
						public void actionPerformed(ActionEvent arg0) {

							previousColor = panel.getBackground();

							final Color hoverColor;

							hoverColor = GUIOp.scaleWhite(previousColor, 0.9);

							for (StoryComponentPanel descendant : panel
									.getDescendants()) {
								descendant.setBackground(hoverColor);
							}

							hoverTimer.stop();
						};
					});

			@Override
			public void mouseEntered(MouseEvent e) {
				if (!(panel.getStoryComponent() instanceof StoryPoint)) {
					hoverTimer.restart();
				}

				e.consume();
			}

			@Override
			public void mouseExited(MouseEvent e) {
				hoverTimer.stop();

				if (!(panel.getStoryComponent() instanceof StoryPoint)) {
					final StoryComponentPanelManager manager;

					manager = this.panel.getSelectionManager();

					if (manager != null)
						manager.updatePanelBackgrounds();
					else {
						panel.setBackground(Color.WHITE);

						System.out
								.println("Attempted to change UI of panel "
										+ "with null selection manager for "
										+ "StoryComponent "
										+ panel.getStoryComponent());
					}
				}
				e.consume();
			}
		};
	}
}