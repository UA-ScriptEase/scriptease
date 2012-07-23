package scriptease.gui.storycomponentpanel;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.TransferHandler;
import javax.swing.border.CompoundBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.MouseInputListener;

import scriptease.controller.ContainerCollector;
import scriptease.controller.VisibilityManager;
import scriptease.controller.observer.StoryComponentEvent;
import scriptease.controller.observer.StoryComponentEvent.StoryComponentChangeEnum;
import scriptease.controller.observer.StoryComponentObserver;
import scriptease.controller.observer.StoryComponentObserverAdder;
import scriptease.controller.observer.StoryComponentObserverRemover;
import scriptease.controller.observer.VisibilityManagerEvent;
import scriptease.controller.observer.VisibilityManagerObserver;
import scriptease.model.StoryComponent;

/**
 * JPanel used to properly observe and redraw its component. Can be editable and
 * extendable to be shown/hidden.
 * 
 * @author mfchurch
 * 
 */
@SuppressWarnings("serial")
public class StoryComponentPanel extends JPanel implements
		StoryComponentObserver, MouseInputListener, VisibilityManagerObserver {
	// Colour and Border
	public static final Color UNSELECTED_COLOUR = Color.WHITE;
	public static final Color SELECTED_COLOUR = new Color(220, 220, 220);
	public static final MatteBorder SELECTED_BORDER = BorderFactory
			.createMatteBorder(1, 1, 1, 1, Color.black);
	public static final CompoundBorder UNSELECTED_BORDER = BorderFactory
			.createCompoundBorder(
					BorderFactory.createMatteBorder(0, 1, 0, 0, Color.gray),
					BorderFactory.createEmptyBorder(1, 0, 1, 1));
	private final StoryComponentPanelLayoutManager layout = new StoryComponentPanelLayoutManager();

	// Component
	private final StoryComponent component;
	// Panel Properties
	private boolean editable;
	private boolean selectable;
	private boolean removable;
	private boolean isVisible;

	public StoryComponentPanel(StoryComponent component) {
		// State of Panel
		this.setOpaque(true);
		this.component = component;

		// Action Listeners
		this.addMouseMotionListener(this);
		this.addMouseListener(this);
		InputMap input = this.getInputMap(WHEN_FOCUSED);
		input.put(
				KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_DOWN_MASK),
				"COPY");
		input.put(
				KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.CTRL_DOWN_MASK),
				"CUT");
		input.put(
				KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.CTRL_DOWN_MASK),
				"PASTE");

		ActionMap am = new ActionMap();
		am.put("COPY", TransferHandler.getCopyAction());
		am.put("CUT", TransferHandler.getCutAction());
		am.put("PASTE", TransferHandler.getPasteAction());
		this.setActionMap(am);

		// Observer the panel and its children
		StoryComponentObserverAdder adder = new StoryComponentObserverAdder();
		adder.observeRelated(this, this.component);

		// Layout
		this.setLayout(layout);

		// Visibility Listener
		VisibilityManager.getInstance().addVisibilityManagerListener(this);
		// Set Initial visibility to reflect the VisibilityManager
		this.setVisible(VisibilityManager.getInstance().isVisible(component));
	}

	@Override
	public StoryComponentPanelLayoutManager getLayout() {
		return layout;
	}

	public boolean showChildren() {
		return layout.showChildren();
	}

	public void setShowChildren(boolean showChildren) {
		layout.setShowChildren(showChildren);
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


	/**
	 * Returns the visible state of the component.
	 * @return
	 */
	public boolean isVisible() {
		return this.isVisible;
	}
	
	
	@Override
	public void setVisible(boolean isVisible) {
		final StoryComponentPanel parent = this.getParentStoryComponentPanel();
		this.isVisible = isVisible;
		super.setVisible(isVisible);
		
		// also show all parents so that I can be shown
		if (isVisible && parent != null) {
			parent.setVisible(isVisible);
		}
	}

	public void setEditable(boolean editable) {
		this.editable = editable;
		// super.setEnabled(editable);
		Collection<Component> components = new ContainerCollector()
				.getAllComponents(this);
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
		return component;
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
	 * @see #getDescendantStoryComponentPanels()
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
	public List<StoryComponentPanel> getDescendantStoryComponentPanels() {
		// Create a new List to contain the descendants of this
		// StoryComponentPanel.
		List<StoryComponentPanel> descendants = new ArrayList<StoryComponentPanel>();

		// Add the children of this StoryComponentPanel.
		List<StoryComponentPanel> childrenPanels = this.getChildrenPanels();
		descendants.addAll(childrenPanels);

		// Recursively call this method of the child StoryComponentPanels to add
		// their descendants.
		for (StoryComponentPanel child : childrenPanels) {
			descendants.addAll(child.getDescendantStoryComponentPanels());
		}

		// Return the list of descendants.
		return descendants;
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
			StoryComponentObserverAdder adder = new StoryComponentObserverAdder();
			adder.observeRelated(this, component);
			if (component.getOwner() == this.component) {
				StoryComponentPanelFactory.getInstance().addChild(this,
						component);
			}
		} else if (type.equals(StoryComponentChangeEnum.CHANGE_CHILD_REMOVED)) {
			StoryComponentObserverRemover.removeObservers(this, component);
			if (component.getOwner() == null || component.getOwner() == this.component) {
				StoryComponentPanelFactory.getInstance().removeChild(this,
						component);
			}
		} else if (type.equals(StoryComponentChangeEnum.CHANGE_KNOW_IT_BOUND)
				|| type.equals(StoryComponentChangeEnum.CHANGE_TEXT_NAME)
				|| type.equals(StoryComponentChangeEnum.LABEL_SET)
				|| type.equals(StoryComponentChangeEnum.CHANGE_START_IT_SLOT)
				|| type.equals(StoryComponentChangeEnum.CHANGE_CODEBLOCK_ADDED)) {
			StoryComponentPanelFactory.getInstance().refreshMain(this);
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
		StoryComponentPanelTree parentTree = this.getParentTree();
		if (parentTree != null)
			return parentTree.getSelectionManager();
		return null;
	}

	/**
	 * Toggle a drag event manually
	 */
	@Override
	public void mouseDragged(MouseEvent e) {
		StoryComponentPanelManager selectionManager = this
				.getSelectionManager();
		if (selectionManager != null) {
			boolean clearSelection = !(selectionManager.getSelectedPanels()
					.contains(this));
			selectionManager.setSelection(this, true, clearSelection);
			JComponent component = (JComponent) e.getSource();

			// Determine the type of action
			final int action;
			if (this.removable)
				action = TransferHandler.MOVE;
			else
				action = TransferHandler.COPY;

			component.getTransferHandler().exportAsDrag(
					(JComponent) e.getSource(), e, action);
		}
		e.consume();
	}

	/**
	 * Toggle panel selection when clicked, then redraw with a grey selection
	 * tint
	 */
	@Override
	public void mouseClicked(MouseEvent e) {
		final StoryComponentPanelManager selectionManager = this
				.getSelectionManager();
		if (selectionManager != null)
			selectionManager.toggleSelection(this, e);
		e.consume();
	}

	@Override
	public void visibilityChanged(VisibilityManagerEvent event) {
		final short type = event.getEventType();
		final StoryComponent changed = event.getSource();
		if (changed == this.component) {
			if (type == VisibilityManagerEvent.VISIBILITY_ADDED)
				this.setVisible(true);
			else if (type == VisibilityManagerEvent.VISIBILITY_REMOVED)
				this.setVisible(false);
		}
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		e.consume();
	}

	@Override
	public void mousePressed(MouseEvent e) {
		e.consume();
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		e.consume();
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		e.consume();
	}

	@Override
	public void mouseExited(MouseEvent e) {
		e.consume();
	}
}