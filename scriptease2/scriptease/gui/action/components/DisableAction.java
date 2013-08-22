package scriptease.gui.action.components;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.Action;
import javax.swing.KeyStroke;

import scriptease.controller.observer.SEFocusObserver;
import scriptease.controller.undo.UndoManager;
import scriptease.gui.SEFocusManager;
import scriptease.gui.action.ActiveModelSensitiveAction;
import scriptease.gui.storycomponentpanel.StoryComponentPanel;
import scriptease.gui.storycomponentpanel.StoryComponentPanelManager;
import scriptease.model.StoryComponent;
import scriptease.model.complex.StoryPoint;

/**
 * Represents and performs a disable action on a story component. Disabling a
 * story component is equivalent to removing it from code generation.
 * 
 * @author jyuen
 */
@SuppressWarnings("serial")
public class DisableAction extends ActiveModelSensitiveAction {

	private static final String DISABLE_TEXT = "Enable / Disable";

	private static final Action instance = new DisableAction();

	/**
	 * Gets the sole instance of this particular type of Action
	 * 
	 * @return The sole instance of this particular type of Action
	 */
	public static Action getInstance() {
		return DisableAction.instance;
	}

	/**
	 * Updates the action to either be enabled or disabled depending on the
	 * current selection.
	 */
	@Override
	protected boolean isLegal() {
		final Component focusOwner;

		focusOwner = SEFocusManager.getInstance().getFocus();

		if (focusOwner instanceof StoryComponentPanel)
			return super.isLegal();
		else
			return false;
	}

	/**
	 * Defines a <code>DisableAction</code> object with a mnemonic and
	 * accelerator.
	 */
	private DisableAction() {
		super(DisableAction.DISABLE_TEXT);

		this.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_E);
		this.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
				KeyEvent.VK_E, InputEvent.CTRL_DOWN_MASK));

		SEFocusManager.getInstance().addSEFocusObserver(new SEFocusObserver() {

			@Override
			public void gainFocus(Component oldFocus) {
				DisableAction.this.updateEnabledState();
			}

			@Override
			public void loseFocus(Component oldFocus) {
				DisableAction.this.updateEnabledState();
			}
		});
	}

	/**
	 * Disables <code>components</code> visually by graying out the bindings,
	 * text, etc. Enables them again if they are already disabled.
	 * 
	 * Also signifies to code gen to add comments to the piece of code.
	 * 
	 * @param component
	 */
	public void disableComponent(StoryComponentPanel componentPanel) {
		final StoryComponent component = componentPanel.getStoryComponent();

		final boolean enabled = component.isEnabled();

		if (component instanceof StoryPoint)
			return;

		component.setEnabled(!enabled);

		// Enable / Disable all child components
		for (StoryComponentPanel childPanel : componentPanel.getDescendants()) {
			childPanel.getStoryComponent().setEnabled(!enabled);
			childPanel.repaint();
		}
		
		// Re-enable all successive owners if the component is enabled.
		StoryComponent owner = component.getOwner();
		while (owner != null
				&& !(owner instanceof StoryPoint)
				&& !owner.isEnabled()
				&& component.isEnabled()) {
			owner.setEnabled(true);
			owner = owner.getOwner();
		}

		componentPanel.repaint();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		final Component focusOwner;

		focusOwner = SEFocusManager.getInstance().getFocus();

		if (focusOwner instanceof StoryComponentPanel) {
			final StoryComponentPanel panel;
			final StoryComponentPanelManager manager;

			panel = (StoryComponentPanel) focusOwner;
			manager = panel.getSelectionManager();

			if (manager != null) {
				final List<StoryComponentPanel> toDisable = manager
						.getSelectedParents();

				if (!UndoManager.getInstance().hasOpenUndoableAction())
					UndoManager.getInstance().startUndoableAction("Disable");

				for (StoryComponentPanel comp : toDisable) {
					this.disableComponent(comp);
				}

				if (UndoManager.getInstance().hasOpenUndoableAction())
					UndoManager.getInstance().endUndoableAction();
			}
		}
	}
}
