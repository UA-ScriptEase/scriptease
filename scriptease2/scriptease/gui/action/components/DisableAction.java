package scriptease.gui.action.components;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.KeyStroke;

import scriptease.ScriptEase;
import scriptease.controller.observer.SEFocusObserver;
import scriptease.gui.SEFocusManager;
import scriptease.gui.action.ActiveModelSensitiveAction;
import scriptease.gui.storycomponentpanel.StoryComponentPanel;
import scriptease.model.StoryComponent;
import scriptease.model.semodel.SEModel;
import scriptease.model.semodel.SEModelManager;

/**
 * Represents and performs a disable action on a story component. Disabling a
 * story component is equivalent to commenting it out in code.
 * 
 * @author jyuen
 */
@SuppressWarnings("serial")
public class DisableAction extends ActiveModelSensitiveAction implements
		SEFocusObserver {

	private static final String DISABLE_TEXT = "Disable";

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
	protected boolean isLegal() {
		final SEModel activeModel;
		final Component focusOwner;
		final boolean isLegal;

		focusOwner = SEFocusManager.getInstance().getFocus();
		activeModel = SEModelManager.getInstance().getActiveModel();

		if (focusOwner instanceof StoryComponentPanel) {
			isLegal = ((StoryComponentPanel) focusOwner).isRemovable();
		} else
			isLegal = false;

		return activeModel != null && isLegal;
	}

	/**
	 * Defines a <code>CopyAction</code> object with no icon.
	 */
	private DisableAction() {
		super(DisableAction.DISABLE_TEXT);

		this.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_SPACE);
		this.putValue(Action.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0));

		SEFocusManager.getInstance().addSEFocusObserver(this);
	}

	/**
	 * Disables <code>components</code> visually by graying out the bindings,
	 * text, etc.
	 * 
	 * Also signifies to code gen to add comments to the piece of code.
	 * 
	 * @param component
	 */
	private void disableComponent(StoryComponentPanel componentPanel) {
		System.out.println("DEBUG - story component panel "
				+ componentPanel.toString()
				+ " is about to be disabled/enabled");

		final StoryComponent component = componentPanel.getStoryComponent();

		final Font disabledFont = new Font(Font.MONOSPACED, Font.ITALIC,
				Integer.parseInt(ScriptEase.getInstance().getPreference(
						ScriptEase.FONT_SIZE_KEY)));

		final Font regularFont = new Font(Font.MONOSPACED, Font.PLAIN,
				Integer.parseInt(ScriptEase.getInstance().getPreference(
						ScriptEase.FONT_SIZE_KEY)));

		if (component.isDisabled()) {
			component.setDisabled(false);

			for (Component childComponent : componentPanel.getComponents()) {
				childComponent.setFont(regularFont);
			}

		} else {
			component.setDisabled(true);

			for (Component childComponent : componentPanel.getComponents()) {
				childComponent.setFont(disabledFont);
				childComponent.setBackground(Color.black);
			}
		}

		componentPanel.repaint();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		final Component focusOwner;

		focusOwner = SEFocusManager.getInstance().getFocus();

		if (focusOwner instanceof StoryComponentPanel) {
			this.disableComponent((StoryComponentPanel) focusOwner);
		}
	}

	@Override
	public void gainFocus(Component oldFocus) {
		this.updateEnabledState();
	}

	@Override
	public void loseFocus(Component oldFocus) {
		this.updateEnabledState();
	}
}
