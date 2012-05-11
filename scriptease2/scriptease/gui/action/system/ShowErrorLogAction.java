/**
 * 
 */
package scriptease.gui.action.system;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import scriptease.gui.internationalization.Il8nResources;

/**
 * Represents and performs the Show Error Log command, as well as encapsulates its enabled
 * and name display state. <br>
 * <br>
 * Show Error Log entails showing a window with ScriptEase's current error log output.
 * 
 * @author remiller
 */
@SuppressWarnings("serial")
public final class ShowErrorLogAction extends AbstractAction {
	private static final String SHOW_ERROR_LOG = Il8nResources
			.getString("Show_Error_Log");

	private static final Action instance = new ShowErrorLogAction();

	/**
	 * Gets the sole instance of this particular type of Action
	 * 
	 * @return The sole instance of this particular type of Action
	 */
	public static Action getInstance() {
		return ShowErrorLogAction.instance;
	}

	/**
	 * Defines an <code>CloseModelAction</code> object with a mnemonic.
	 */
	private ShowErrorLogAction() {
		super(ShowErrorLogAction.SHOW_ERROR_LOG);

		this.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_L);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		System.err.println("SHOW_ERROR_LOG is not functioning yet.");
	}
}
