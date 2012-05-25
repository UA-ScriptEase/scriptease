/**
 * 
 */
package scriptease.gui.action.file;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.KeyStroke;

import scriptease.gui.SEFrame;
import scriptease.gui.action.ActiveModelSensitiveAction;
import scriptease.gui.internationalization.Il8nResources;
import scriptease.gui.pane.StoryPanel;
import scriptease.model.StoryModel;
import scriptease.model.StoryModelPool;

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
public class CloseTabAction extends ActiveModelSensitiveAction {
	private static final String CLOSE_MODULE = Il8nResources
			.getString("Close_Model");
	private final StoryPanel panel;

	/**
	 * Defines an <code>CloseModelAction</code> object with a mnemonic.
	 */
	public CloseTabAction(StoryPanel panel) {
		super(CloseTabAction.CLOSE_MODULE);
		this.panel = panel;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		SEFrame.getInstance().removeStoryPanelTab(panel);
	}
}
