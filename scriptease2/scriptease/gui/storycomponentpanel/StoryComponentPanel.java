package scriptease.gui.storycomponentpanel;

import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.TransferHandler;
import javax.swing.event.MouseInputListener;

import scriptease.controller.ContainerCollector;
import scriptease.controller.StoryAdapter;
import scriptease.controller.observer.storycomponent.StoryComponentEvent;
import scriptease.controller.observer.storycomponent.StoryComponentEvent.StoryComponentChangeEnum;
import scriptease.controller.observer.storycomponent.StoryComponentObserver;
import scriptease.gui.SEFocusManager;
import scriptease.gui.control.ExpansionButton;
import scriptease.model.StoryComponent;
import scriptease.model.complex.ComplexStoryComponent;
import scriptease.model.complex.StoryItemSequence;

/**
 * JPanel used to properly observe and redraw its component. Can be editable and
 * extendible to be shown/hidden.
 * 
 * @author mfchurch
 * @author kschenk
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

		if (this.component == null)
			return;

		final MouseInputListener mouseListener = this.mouseListener();
		// Action Listeners
		this.addMouseMotionListener(mouseListener);
		this.addMouseListener(mouseListener);
		InputMap input = this.getInputMap(WHEN_FOCUSED);
		input.put(
				KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_DOWN_MASK),
				"Copy");
		input.put(
				KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.CTRL_DOWN_MASK),
				"Cut");
		input.put(
				KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.CTRL_DOWN_MASK),
				"Paste");

		ActionMap am = new ActionMap();
		am.put("Copy", TransferHandler.getCopyAction());
		am.put("Cut", TransferHandler.getCutAction());
		am.put("Paste", TransferHandler.getPasteAction());
		this.setActionMap(am);

		// Observer the panel and its children
		this.component.observeRelated(this);

		// Layout
		this.setLayout(this.layout);

		this.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
				SEFocusManager.getInstance().setFocus(StoryComponentPanel.this);
			}

			@Override
			public void focusLost(FocusEvent e) {
				if (e.getOppositeComponent() instanceof StoryComponentPanel)
					return;

				final StoryComponentPanelManager selectionManager;

				selectionManager = StoryComponentPanel.this
						.getSelectionManager();
				if (selectionManager != null)
					selectionManager.clearSelection();
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

	public void setEditable(boolean editable) {
		this.editable = editable;
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

	private boolean drawLine;

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		if (this.drawLine) {
			final Graphics2D g2 = (Graphics2D) g.create();

			g2.drawLine(this.getX(), this.getY(),
					this.getPreferredSize().width, this.getY());
		}
	}

	public void setDrawLine(boolean drawLine) {
		this.drawLine = drawLine;

		this.repaint();
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
			component.observeRelated(this);
			if (component.getOwner() == this.component) {
				StoryComponentPanelFactory.getInstance().addChild(this,
						component);
			}
		} else if (type.equals(StoryComponentChangeEnum.CHANGE_CHILD_REMOVED)) {
			component.removeStoryComponentObserverFromChildren(this);
			if (component.getOwner() == null
					|| component.getOwner() == this.component) {
				StoryComponentPanelFactory.getInstance().removeChild(this,
						component);
			}
		} else if (type.equals(StoryComponentChangeEnum.CHANGE_VISIBILITY)) {
			this.setVisible(component.isVisible());
		} else {
			StoryComponentPanelFactory.getInstance()
					.refreshStoryComponentPanel(this);
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
	 * Makes every StoryComponentPanel editable, and any non-root
	 * StoryComponentPanels selectable and removable
	 */
	public void updateComplexSettings() {
		if (this.component != null) {
			updateSettings(this);
			for (StoryComponentPanel panel : this
					.getDescendantStoryComponentPanels()) {
				updateSettings(panel);
			}
		}
	}

	private void updateSettings(final StoryComponentPanel panel) {
		panel.getStoryComponent().process(new StoryAdapter() {
			@Override
			protected void defaultProcess(StoryComponent component) {
				panel.setSelectable(true);
				panel.setRemovable(true);
			}

			/**
			 * Everything but the root is selectable
			 */
			@Override
			protected void defaultProcessComplex(ComplexStoryComponent complex) {
				boolean notRoot = (complex.getOwner() != null);
				panel.setSelectable(notRoot);
				panel.setRemovable(notRoot);
			}

			@Override
			public void processStoryItemSequence(StoryItemSequence sequence) {
				panel.setSelectable(true);
				panel.setRemovable(true);
			}
		});

		panel.setEditable(true);
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

			/**
			 * Toggle a drag event manually
			 */
			@Override
			public void mouseDragged(MouseEvent e) {
				final StoryComponentPanelManager selectionManager;

				selectionManager = panel.getSelectionManager();

				if (selectionManager != null) {
					boolean clearSelection = !(selectionManager
							.getSelectedPanels().contains(this));
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

					panel.requestFocusInWindow();
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
		};
	}
}