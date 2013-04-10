/**
 * 
 */
package scriptease.gui.action.file;

import java.awt.event.ActionEvent;

import javax.swing.JComponent;

import scriptease.controller.FileManager;
import scriptease.gui.action.ActiveModelSensitiveAction;
import scriptease.gui.internationalization.Il8nResources;
import scriptease.gui.pane.ModelTabPanel;
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
public class CloseModelTabAction extends ActiveModelSensitiveAction {
	private static final String CLOSE_MODULE = Il8nResources
			.getString("Close_Model");
	private final JComponent component;
	private final SEModel model;

	/**
	 * Defines an <code>CloseModelAction</code> object with a mnemonic.
	 */
	public CloseModelTabAction(JComponent component, SEModel model) {
		super(CloseModelTabAction.CLOSE_MODULE);
		this.component = component;
		this.model = model;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
	//	ModelTabPanel.getInstance().removeModelComponent(this.component,
	//			this.model);
		
		FileManager.getInstance().close(this.model);

	}
}
