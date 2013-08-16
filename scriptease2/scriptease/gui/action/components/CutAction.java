package scriptease.gui.action.components;

import java.awt.Component;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.TransferHandler;

import scriptease.controller.observer.SEFocusObserver;
import scriptease.gui.SEFocusManager;
import scriptease.gui.action.ActiveModelSensitiveAction;
import scriptease.gui.storycomponentpanel.StoryComponentPanel;
import scriptease.model.semodel.SEModel;
import scriptease.model.semodel.SEModelManager;

/**
 * Represents and performs the Cut command, as well as encapsulates its enabled
 * and name display state.
 * 
 * @author kschenk
 */
@SuppressWarnings("serial")
public final class CutAction extends ActiveModelSensitiveAction {
	private static final String CUT_TEXT = "Cut";

	private static final Action instance = new CutAction();

	/**
	 * Gets the sole instance of this particular type of Action
	 * 
	 * @return The sole instance of this particular type of Action
	 */
	public static Action getInstance() {
		return CutAction.instance;
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
	private CutAction() {
		super(CutAction.CUT_TEXT);

		this.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_X);
		this.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
				KeyEvent.VK_X, InputEvent.CTRL_DOWN_MASK));

		SEFocusManager.getInstance().addSEFocusObserver(new SEFocusObserver() {

			@Override
			public void gainFocus(Component oldFocus) {
				CutAction.this.updateEnabledState();
			}

			@Override
			public void loseFocus(Component oldFocus) {
				CutAction.this.updateEnabledState();
			}
		});
	}

	/**
	 * Cuts the passed in component to the system clipboard.
	 * 
	 * @param component
	 */
	private void cutComponent(StoryComponentPanel component) {
		component.getTransferHandler().exportToClipboard(component,
				Toolkit.getDefaultToolkit().getSystemClipboard(),
				TransferHandler.MOVE);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		final Component focusOwner;

		focusOwner = SEFocusManager.getInstance().getFocus();

		if (focusOwner instanceof StoryComponentPanel) {
			this.cutComponent((StoryComponentPanel) focusOwner);
		}
	}
}
