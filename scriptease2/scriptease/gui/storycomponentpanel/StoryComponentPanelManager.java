package scriptease.gui.storycomponentpanel;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.BorderFactory;

import scriptease.controller.StoryAdapter;
import scriptease.controller.undo.UndoManager;
import scriptease.gui.SEFocusManager;
import scriptease.gui.ui.ScriptEaseUI;
import scriptease.model.StoryComponent;
import scriptease.model.complex.AskIt;
import scriptease.model.complex.CauseIt;
import scriptease.model.complex.ComplexStoryComponent;
import scriptease.model.complex.ControlIt;
import scriptease.model.complex.StoryComponentContainer;
import scriptease.model.complex.StoryPoint;
import scriptease.util.GUIOp;

/**
 * 
 * @author mfchurch
 * @author jyuen
 */
public class StoryComponentPanelManager {
	private Map<StoryComponentPanel, Boolean> selected;
	private StoryComponentPanel lastSelected;

	public StoryComponentPanelManager() {
		this.selected = new HashMap<StoryComponentPanel, Boolean>();
	}

	/**
	 * Sets the given panel and any children to the selected value
	 * 
	 * @param panel
	 * @param selected
	 */
	public void addComplexPanel(StoryComponentPanel panel, boolean selected) {
		this.addSimplePanel(panel, selected);
		for (StoryComponentPanel aPanel : panel
				.getDescendantStoryComponentPanels()) {
			this.addComplexPanel(aPanel, selected);
		}
	}

	/**
	 * Sets the given panel to the selected value, then displays it's selection
	 * 
	 * @param panel
	 * @param selected
	 */
	private void addSimplePanel(StoryComponentPanel panel, boolean selected) {
		this.selected.put(panel, selected);
		this.displayPanelSelection(panel, selected);
	}

	/**
	 * Removes observers from the sub panels and removes them from the
	 * collection of panels held in the factory
	 * 
	 * @param aPanel
	 */
	public void cleanUpPanel(StoryComponentPanel aPanel) {
		if (aPanel.getStoryComponent() != null) {
			aPanel.getStoryComponent()
					.removeStoryComponentObserverFromChildren(aPanel);
			Collection<StoryComponentPanel> descendantPanels = aPanel
					.getDescendantStoryComponentPanels();
			this.selected.remove(aPanel);
			for (StoryComponentPanel child : descendantPanels) {
				cleanUpPanel(child);
			}
		}
	}

	/**
	 * Clears selection if desired, then sets the selection of the given panel
	 * (and it's children) to the given value. Requests focus on the panel and
	 * updates the background colour.
	 * 
	 * @param panel
	 * @param isSelected
	 * @param clearSelection
	 */
	public void setSelection(StoryComponentPanel panel, boolean isSelected,
			boolean clearSelection) {
		final boolean selectable = panel.isSelectable();
		if (clearSelection)
			clearSelection();
		if (selectable) {

			if (!(panel.getStoryComponent() instanceof StoryPoint))
				for (StoryComponentPanel subPanel : panel
						.getDescendantStoryComponentPanels()) {
					this.selected.put(subPanel, isSelected);
				}

			this.selected.put(panel, isSelected);

			panel.requestFocusInWindow();
			updatePanelBackgrounds();
		} else {
			final StoryComponentPanel parent;

			parent = panel.getParentStoryComponentPanel();

			if (parent != null)
				this.setSelection(parent, parent.isSelectable(), false);
		}
	}

	/**
	 * Deletes the selected removable StoryComponentPanels
	 */
	public void deleteSelected() {
		final UndoManager undoManager = UndoManager.getInstance();

		List<StoryComponentPanel> toDelete = this.getSelectedParents();

		// We don't want to remove the StoryComponentPanel if it belongs to
		// a StoryComponentContainer - only remove the contents.
		for (StoryComponentPanel parent : toDelete) {
			if (parent.getStoryComponent() instanceof StoryComponentContainer) {
				toDelete = this.getSelectedPanels();
				toDelete.removeAll(this.getSelectedParents());
				break;
			}
		}

		// Start a new UndoableAction.
		if (!undoManager.hasOpenUndoableAction()) {
			undoManager.startUndoableAction("Delete");
		}

		// Delete all selected storyComponents
		for (StoryComponentPanel panel : toDelete) {
			// only delete removable panels
			if (panel.isRemovable()) {
				final StoryComponent child = panel.getStoryComponent();
				final StoryComponent owner = child.getOwner();
				// if the owner is its parent, remove the child
				if (owner instanceof ComplexStoryComponent
						&& ((ComplexStoryComponent) owner).hasChild(child)) {
					((ComplexStoryComponent) owner).removeStoryChild(child);
					this.setSelection(panel, false, false);
				}
			}
		}

		// End the UndoableAction.
		if (undoManager.hasOpenUndoableAction()) {
			undoManager.endUndoableAction();
		}
	}

	/**
	 * Duplicates the selected StoryComponentPanels
	 */
	public void duplicateSelected() {
		List<StoryComponentPanel> toDuplicate = getSelectedParents();

		// Get the UndoManager.
		UndoManager undoManager = UndoManager.getInstance();

		// Start a new UndoableAction.
		if (!undoManager.hasOpenUndoableAction()) {
			undoManager.startUndoableAction("Duplicate");
		}

		// Duplicate all selected storyComponents
		for (StoryComponentPanel panel : toDuplicate) {
			final StoryComponent child = panel.getStoryComponent();
			final StoryComponent owner = child.getOwner();
			// if the owner is its parent, remove the child
			if (owner instanceof ComplexStoryComponent
					&& ((ComplexStoryComponent) owner).hasChild(child)) {
				final StoryComponent clone = child.clone();
				((ComplexStoryComponent) owner).addStoryChild(clone);
				this.setSelection(panel, true, false);
			}
		}

		// End the UndoableAction.
		if (undoManager.hasOpenUndoableAction()) {
			undoManager.endUndoableAction();
		}
	}

	/**
	 * Clear the selection then select all of the StoryComponentPanels from
	 * 'from' until 'to'. If 'from' is null, it will select all panels until
	 * 'to' starting from the first child of 'to's parent. If they don't share
	 * the same parent, do nothing.
	 * 
	 * @param from
	 * @param to
	 */
	private void shiftSelection(StoryComponentPanel from, StoryComponentPanel to) {
		// CHECK: to is required when shift selecting, this case should never be
		// met
		if (to == null)
			return;
		final StoryComponentPanel toParent = to.getParentStoryComponentPanel();

		// CHECK: can only shift select when a parent is present
		if (toParent == null)
			return;

		// Get the ordered child panels of the parent
		final List<StoryComponentPanel> childrenPanels = toParent
				.getChildrenPanels();

		// if from is not null
		if (from != null) {
			// if they have a common parent
			if (from.getParentStoryComponentPanel() == toParent) {
				// clear selection
				this.clearSelection();

				boolean selected = false;
				for (StoryComponentPanel child : childrenPanels) {
					// toggle selected on either 'to' or 'from'
					if (child == from || child == to) {
						selected = !selected;
						// if toggled off, select and return
						if (selected == false) {
							setSelection(child, true, false);
							return;
						}
					}
					setSelection(child, selected, false);
				}
			}
			// otherwise do nothing
			else
				return;
		}
		// otherwise default selecting from the first element on toParent
		else {
			// clear selection
			this.clearSelection();

			boolean selected = true;
			for (StoryComponentPanel child : childrenPanels) {
				setSelection(child, selected, false);
				if (child == to)
					return;
			}
		}
	}

	/**
	 * Keeps track of the selected panels, should be called whenever a panel is
	 * clicked (or attempted to be selected)
	 * 
	 * @param panel
	 * @param e
	 */
	public void toggleSelection(StoryComponentPanel panel, MouseEvent e) {
		final Boolean isSelected = this.selected.get(panel);

		// sanity check
		if (isSelected == null)
			return;

		final boolean shiftPressed = e.isShiftDown();
		// if shift is pressed
		if (shiftPressed) {
			shiftSelection(this.lastSelected, panel);
		}
		// otherwise worry about ctrl and single selection
		else {
			final boolean ctrlPressed = e.isControlDown();
			final boolean clearSelection;
			boolean newValue = isSelected;
			// if ctrl is pressed
			if (ctrlPressed) {
				clearSelection = false;
				final StoryComponentPanel parent = panel
						.getParentStoryComponentPanel();
				// if the parent is selected, you cannot unselect the panel
				if (parent != null && this.selected.containsKey(parent)
						&& !this.selected.get(parent))
					newValue = !isSelected;
			}
			// otherwise do a single selection
			else {
				clearSelection = true;
				newValue = true;
			}
			this.setSelection(panel, newValue, clearSelection);
			this.lastSelected = panel;
		}
	}

	/**
	 * Clears the selection by setting all panel's selection to false.
	 * this.selected.clear() should not be used because that removes all
	 * entries, and will no longer reflect the selection state of the model
	 */
	public void clearSelection() {
		// clear selection
		for (StoryComponentPanel key : this.selected.keySet()) {
			this.selected.put(key, false);
		}
		updatePanelBackgrounds();
	}

	/**
	 * Sets the panels background to reflect if they are selected or not
	 */
	public void updatePanelBackgrounds() {
		for (Entry<StoryComponentPanel, Boolean> entry : this.selected
				.entrySet()) {
			final StoryComponentPanel panel = entry.getKey();
			final Boolean isSelected = entry.getValue();

			displayPanelSelection(panel, isSelected);
		}
	}

	/**
	 * Updates the selection appearance of the given panel to the given
	 * isSelected
	 * 
	 * @param panel
	 * @param isSelected
	 */
	private void displayPanelSelection(final StoryComponentPanel panel,
			final Boolean isSelected) {
		if (panel.getStoryComponent() instanceof StoryPoint) {
			panel.setBackground(ScriptEaseUI.UNSELECTED_COLOUR);
			return;
		}

		if (isSelected) {
			final boolean focusOnAPanel;

			focusOnAPanel = SEFocusManager.getInstance().getFocus() instanceof StoryComponentPanel;

			if (focusOnAPanel)
				panel.setBackground(ScriptEaseUI.SELECTED_COLOUR);
			else {
				panel.setBackground(GUIOp.scaleWhite(
						ScriptEaseUI.SELECTED_COLOUR, 1.15));
			}
			final StoryComponentPanel parent;

			parent = panel.getParentStoryComponentPanel();

			// If the parent is selected, don't draw a box around the child
			if (selected.containsKey(parent) && !this.selected.get(parent)
					&& focusOnAPanel)
				panel.setBorder(ScriptEaseUI.SELECTED_BORDER);
			else
				panel.setBorder(ScriptEaseUI.UNSELECTED_BORDER);
		} else {
			panel.setBackground(ScriptEaseUI.UNSELECTED_COLOUR);
			final StoryComponent panelComponent;

			panelComponent = panel.getStoryComponent();

			panelComponent.process(new StoryAdapter() {
				@Override
				protected void defaultProcessComplex(
						ComplexStoryComponent complex) {
					this.defaultProcess(complex);
				}

				@Override
				protected void defaultProcess(StoryComponent component) {
					panel.setBorder(ScriptEaseUI.UNSELECTED_BORDER);
				}

				public void processCauseIt(CauseIt causeIt) {
					panel.setBorder(BorderFactory
							.createLineBorder(Color.LIGHT_GRAY));
				}

				public void processControlIt(ControlIt controlIt) {
					panel.setBorder(BorderFactory
							.createLineBorder(Color.LIGHT_GRAY));
				}

				public void processAskIt(AskIt askIt) {
					panel.setBorder(BorderFactory
							.createLineBorder(Color.LIGHT_GRAY));
				}

				@Override
				protected void defaultProcessAtomic(StoryComponent atom) {
					this.defaultProcess(atom);
				}
			});
		}
	}

	/**
	 * Gets a list of all the selected StoryComponentPanels, excluding selected
	 * parents which also have a selected parent. This has room for optimization
	 * if/when we decided to sort the list of selected parents, as currently
	 * each panel just adds it's highest selected parent (which may turn out to
	 * be itself, it nothing higher than it is selected) to the selected
	 * 
	 * @author mfchurch
	 * @return
	 */
	public List<StoryComponentPanel> getSelectedParents() {
		List<StoryComponentPanel> selectedPanels = new ArrayList<StoryComponentPanel>();
		for (Entry<StoryComponentPanel, Boolean> entry : this.selected
				.entrySet()) {
			if (entry.getValue()) {
				StoryComponentPanel panel = entry.getKey();
				StoryComponentPanel selectedParent = getHighestSelectedParent(panel);
				if (!selectedPanels.contains(selectedParent))
					selectedPanels.add(selectedParent);
			}
		}
		return selectedPanels;
	}

	/**
	 * Get's the highest selected parent StoryComponentPanel of the given
	 * selected StoryComponentPanel. Returns null if the child, nor it's
	 * immediate parent are not selected
	 * 
	 * @param child
	 * @return
	 */
	public StoryComponentPanel getHighestSelectedParent(
			StoryComponentPanel child) {
		final StoryComponentPanel parent = child.getParentStoryComponentPanel();
		// if the parent is selected, recurse
		if (this.selected.containsKey(parent) && this.selected.get(parent))
			return getHighestSelectedParent(parent);
		// otherwise if the child is selected, return it
		else if (this.selected.containsKey(child) && this.selected.get(child))
			return child;
		// otherwise return null
		else
			return null;
	}

	/**
	 * Gets a list of all the selected StoryComponentPanels
	 * 
	 * @return
	 */
	public List<StoryComponentPanel> getSelectedPanels() {
		List<StoryComponentPanel> selectedPanels = new ArrayList<StoryComponentPanel>();
		for (Entry<StoryComponentPanel, Boolean> entry : this.selected
				.entrySet()) {
			if (entry.getValue())
				selectedPanels.add(entry.getKey());
		}
		return selectedPanels;
	}
}
