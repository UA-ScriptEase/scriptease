/**
 * 
 */
package scriptease.gui.action.system;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import scriptease.ScriptEase;
import scriptease.gui.internationalization.Il8nResources;

/**
 * Represents and performs the Exit command, as well as encapsulates its enabled
 * and name display state. <br>
 * <br>
 * Exit entails calling ScriptEase's exit method with no error state.
 * 
 * @author remiller
 */
@SuppressWarnings("serial")
public final class ExitScriptEaseAction extends AbstractAction {
	private static final String EXIT = Il8nResources.getString("Exit");

	private static final Action instance = new ExitScriptEaseAction();

	/**
	 * Gets the sole instance of this particular type of Action
	 * 
	 * @return The sole instance of this particular type of Action
	 */
	public static Action getInstance() {
		return ExitScriptEaseAction.instance;
	}

	/**
	 * Defines an <code>ExitScriptEaseAction</code> object with a mnemonic.
	 */
	private ExitScriptEaseAction() {
		super(ExitScriptEaseAction.EXIT);

		this.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_X);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		ScriptEase.getInstance().exit();
	}
}
