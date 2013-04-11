/**
 * 
 */
package scriptease.gui.action.file;

import java.awt.event.ActionEvent;

import scriptease.controller.FileManager;
import scriptease.gui.action.ActiveModelSensitiveAction;
import scriptease.gui.internationalization.Il8nResources;
import scriptease.model.SEModel;

/**
 * Represents and performs the Close Tab command, as well as encapsulates its
 * enabled and name display state. <br>
 * <br>
 * Close Tab entails closing the currently active tab. This may also remove the
 * model from the model pool if it is the last tab open for that model.
 * 
 * @author remiller
 */
@SuppressWarnings("serial")
public class CloseModelAction extends ActiveModelSensitiveAction {
	private static final String CLOSE_MODEL = Il8nResources
			.getString("Close_Model");
	private final SEModel model;

	/**
	 * Defines an <code>CloseModelAction</code> object with a mnemonic.
	 */
	public CloseModelAction(SEModel model) {
		super(CloseModelAction.CLOSE_MODEL);
		this.model = model;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		FileManager.getInstance().close(this.model);
	}
}
