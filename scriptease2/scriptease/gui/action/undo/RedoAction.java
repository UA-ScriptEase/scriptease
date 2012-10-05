/**
 * 
 */
package scriptease.gui.action.undo;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.KeyStroke;

import scriptease.controller.observer.UndoManagerObserver;
import scriptease.controller.undo.UndoManager;
import scriptease.gui.WindowFactory;
import scriptease.gui.action.ActiveModelSensitiveAction;

@SuppressWarnings("serial")
public final class RedoAction extends ActiveModelSensitiveAction implements
		UndoManagerObserver {
	private static final String REDO = "Redo";

	private static final Action instance = new RedoAction();

	/**
	 * Gets the sole instance of this particular type of Action
	 * 
	 * @return The sole instance of this particular type of Action
	 */
	public static Action getInstance() {
		return RedoAction.instance;
	}

	/**
	 * Updates the action to either be enabled or disabled depending on the
	 * definition of {@link #canUndo()}.
	 */
	protected boolean isLegal() {
		return (UndoManager.getInstance().canRedo());
	}

	/**
	 * Defines a <code>UndoAction</code> object with no icon.
	 */
	private RedoAction() {
		super(RedoAction.REDO);

		this.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_R);
		this.putValue(Action.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_Y, ActionEvent.CTRL_MASK));
		UndoManager.getInstance().addUndoManagerObserver(this);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		WindowFactory.getInstance().getCurrentFrame().requestFocusInWindow();
		
		UndoManager.getInstance().redo();
	}

	@Override
	public void stackChanged() {
		this.updateEnabledState();
		this.putValue(Action.NAME, RedoAction.REDO + " "
				+ UndoManager.getInstance().getLastRedoName());
	}
}
