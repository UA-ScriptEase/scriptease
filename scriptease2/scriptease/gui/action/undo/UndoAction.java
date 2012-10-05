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
public final class UndoAction extends ActiveModelSensitiveAction implements
		UndoManagerObserver {
	private static final String UNDO = "Undo";

	private static final Action instance = new UndoAction();

	/**
	 * Gets the sole instance of this particular type of Action
	 * 
	 * @return The sole instance of this particular type of Action
	 */
	public static Action getInstance() {
		return UndoAction.instance;
	}

	/**
	 * Updates the action to either be enabled or disabled depending on the
	 * definition of {@link #canUndo()}.
	 */
	protected boolean isLegal() {
		return (UndoManager.getInstance().canUndo());
	}

	/**
	 * Defines a <code>UndoAction</code> object with no icon.
	 */
	private UndoAction() {
		super(UndoAction.UNDO);

		this.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_Z);
		this.putValue(Action.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_Z, ActionEvent.CTRL_MASK));
		UndoManager.getInstance().addUndoManagerObserver(this);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		WindowFactory.getInstance().getCurrentFrame().requestFocusInWindow();
		
		UndoManager.getInstance().undo();
	}

	@Override
	public void stackChanged() {
		this.updateEnabledState();
		this.putValue(Action.NAME, UndoAction.UNDO + " "
				+ UndoManager.getInstance().getLastUndoName());
	}
}
